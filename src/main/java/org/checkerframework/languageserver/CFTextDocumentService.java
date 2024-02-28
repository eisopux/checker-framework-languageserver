package org.checkerframework.languageserver;

import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import org.checkerframework.javacutil.BugInCF;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** This class does all the dirty works on source files. */
public class CFTextDocumentService implements TextDocumentService, Publisher {
    /** The logger for issuing information in the Checker Framework document service. */
    private static final Logger logger = Logger.getLogger(CFTextDocumentService.class.getName());

    /**
     * The pattern of the range in CF message "lsp.type.information".
     *
     * <p>See the corresponding Checker Framework code:
     *
     * <ul>
     *   <li><a
     *       href="https://github.com/eisop/checker-framework/blob/3ed0c114c3d686eadc803207640487e86d1d086e/framework/src/main/java/org/checkerframework/framework/source/messages.properties#L3">
     *       messages.properties</a>
     *   <li><a
     *       href="https://github.com/eisop/checker-framework/blob/3ed0c114c3d686eadc803207640487e86d1d086e/framework/src/main/java/org/checkerframework/framework/util/TypeInformationPresenter.java#L138">
     *       TypeInformationPresenter.java</a>
     * </ul>
     */
    private static final Pattern rangePattern =
            Pattern.compile("range=\\((\\d+), (\\d+), (\\d+), (\\d+)\\)");

    /** The Checker Framework language server. */
    private final CFLanguageServer server;

    /** The Checker Framework executor. */
    private CheckExecutor executor;

    /**
     * Store hover type information for each file. Map key is file, value is a mapping from a range
     * of positions to the corresponding type messages.
     */
    private final Map<File, RangeMap<ComparablePosition, List<String>>> filesToTypeInfo =
            new HashMap<>();

    /** Default constructor for Checker Framework document service. */
    CFTextDocumentService(CFLanguageServer server) {
        this.server = server;
    }

    /** Setter for the executor field. */
    void setExecutor(CheckExecutor executor) {
        this.executor = executor;
    }

    /**
     * Clear diagnostics of files. This needs to be done explicitly by the server.
     *
     * @param files files whose diagnostics are to be cleared
     * @see <a
     *     href="https://microsoft.github.io/language-server-protocol/specification#textDocument_publishDiagnostics">specification</a>
     */
    private void clearDiagnostics(List<File> files) {
        for (File file : files) {
            filesToTypeInfo.remove(file);

            server.publishDiagnostics(
                    new PublishDiagnosticsParams(file.toURI().toString(), Collections.emptyList()));
        }
    }

    /** Convert raw diagnostics from the compiler to the LSP counterpart. */
    private Diagnostic convertToLSPDiagnostic(javax.tools.Diagnostic<?> diagnostic) {
        DiagnosticSeverity severity;
        switch (diagnostic.getKind()) {
            case ERROR:
                severity = DiagnosticSeverity.Error;
                break;
            case WARNING:
            case MANDATORY_WARNING:
                severity = DiagnosticSeverity.Warning;
                break;
            case NOTE:
            case OTHER:
            default:
                severity = DiagnosticSeverity.Information;
        }

        // Line numbers and column numbers in Diagnostic are 1-based,
        // while LSP clients use 0-based positions.
        int startLine = (int) diagnostic.getLineNumber() - 1;
        int startCol = (int) diagnostic.getColumnNumber() - 1;
        int endCol = (int) (startCol + diagnostic.getEndPosition() - diagnostic.getStartPosition());
        Position startPos = new Position(startLine, startCol);
        Position endPos = new Position(startLine, endCol);

        return new Diagnostic(
                new Range(startPos, endPos),
                diagnostic.getMessage(null),
                severity,
                CFLanguageServer.SERVER_NAME,
                diagnostic.getCode());
    }

    /**
     * Run type check and publish results.
     *
     * @param files source files to be checked
     */
    private void checkAndPublish(List<File> files) {
        executor.compile(files);
    }

    /**
     * The document open notification is sent from the client to the server to signal newly opened
     * text documents. The document's truth is now managed by the client and the server must not try
     * to read the document's truth using the document's uri.
     *
     * <p>Registration Options: TextDocumentRegistrationOptions
     *
     * @see <a
     *     href="https://microsoft.github.io/language-server-protocol/specification#textDocument_didOpen">specifiation</a>
     */
    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        logger.info(params.toString());
        checkAndPublish(
                Collections.singletonList(new File(URI.create(params.getTextDocument().getUri()))));
    }

    /**
     * The document change notification is sent from the client to the server to signal changes to a
     * text document.
     *
     * <p>Registration Options: TextDocumentChangeRegistrationOptions
     */
    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        // change but not saved; ignore since we can only check the actual file
    }

    /**
     * The document close notification is sent from the client to the server when the document got
     * closed in the client. The document's truth now exists where the document's uri points to
     * (e.g. if the document's uri is a file uri the truth now exists on disk).
     *
     * <p>Registration Options: TextDocumentRegistrationOptions
     *
     * @see <a
     *     href="https://microsoft.github.io/language-server-protocol/specification#textDocument_didClose">specification</a>
     */
    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        logger.info(params.toString());
        File f = new File(URI.create(params.getTextDocument().getUri()));
        clearDiagnostics(Collections.singletonList(f));
    }

    /**
     * The document save notification is sent from the client to the server when the document for
     * saved in the client.
     *
     * <p>Registration Options: TextDocumentSaveRegistrationOptions
     *
     * @see <a
     *     href="https://microsoft.github.io/language-server-protocol/specification#textDocument_didSave">specification</a>
     */
    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        logger.info(params.toString());
        File f = new File(URI.create(params.getTextDocument().getUri()));
        List<File> files = Collections.singletonList(f);
        clearDiagnostics(files);
        checkAndPublish(files);
    }

    @Override
    public void publish(Map<String, List<javax.tools.Diagnostic<?>>> result) {
        for (Map.Entry<String, List<javax.tools.Diagnostic<?>>> entry : result.entrySet()) {
            List<Diagnostic> diagnostics = new ArrayList<>();
            Map<String, List<CheckerTypeKind>> uniqueTypeInfo =
                    processDiagnosticString(entry, diagnostics);
            publishTypeMessageWithFilter(entry, uniqueTypeInfo);
            server.publishDiagnostics(new PublishDiagnosticsParams(entry.getKey(), diagnostics));
        }
    }

    /**
     * Process Diagnostic strings to separate type message and error message.
     *
     * @param entry The Diagnostic entry for getting diagnostic message
     * @param diagnostics A list for storing checkerframework issued errors
     * @return TypeMessage processed for non-verbose output
     */
    private Map<String, List<CheckerTypeKind>> processDiagnosticString(
            Map.Entry<String, List<javax.tools.Diagnostic<?>>> entry,
            List<Diagnostic> diagnostics) {
        Map<String, List<CheckerTypeKind>> TypeMessage = new HashMap<>();
        for (javax.tools.Diagnostic<?> diagnostic : entry.getValue()) {
            String message = diagnostic.getMessage(Locale.getDefault());
            if (message != null && message.contains("lsp.type.information")) {
                String checker = getChecker(message);
                String kind = getKind(message);
                String type = getType(message);
                String positionInfo = getPosition(message);
                if (positionInfo != null) {
                    // If the position is not in the map, add a new entry.
                    List<CheckerTypeKind> checkerTypeKinds = TypeMessage.computeIfAbsent(positionInfo, k -> new ArrayList<>());
                    // If the checker is not in the list, add a new entry.
                    CheckerTypeKind checkerTypeKind = checkerTypeKinds.stream()
                            .filter(c -> c.getCheckername().equals(checker))
                            .findFirst()
                            .orElseGet(() -> {
                                CheckerTypeKind newCheckerTypeKind = new CheckerTypeKind(checker, new HashMap<>());
                                checkerTypeKinds.add(newCheckerTypeKind);
                                return newCheckerTypeKind;
                            });
                    // If the type is not in the map, add a new entry.
                    checkerTypeKind.getTypeToKindMap().putIfAbsent(type, kind);
                }
            } else {
                // If is not type message, add to diagnostics.
                diagnostics.add(convertToLSPDiagnostic(diagnostic));
            }
        }
        return TypeMessage;
    }
//    private Map<String, List<CheckerTypeKind>> processDiagnosticString(
//            Map.Entry<String, List<javax.tools.Diagnostic<?>>> entry,
//            List<Diagnostic> diagnostics) {
//        Map<String, List<CheckerTypeKind>> TypeMessage = new HashMap<>();
//        for (javax.tools.Diagnostic<?> diagnostic : entry.getValue()) {
//            String message = diagnostic.getMessage(Locale.getDefault());
//            if (message != null && message.contains("lsp.type.information")) {
//                String checker = getChecker(message);
//                String kind = getKind(message);
//                String type = getType(message);
//                String positionInfo = getPosition(message);
//                if (positionInfo != null) {
//                    if (!TypeMessage.containsKey(positionInfo)) {
//                        List<CheckerTypeKind> checkerTypeKinds = new ArrayList<>();
//                        Map<String, String> kindType = new HashMap<>();
//                        kindType.put(type, kind);
//                        checkerTypeKinds.add(new CheckerTypeKind(checker, kindType));
//                        TypeMessage.put(positionInfo, checkerTypeKinds);
//                    } else {
//                        List<CheckerTypeKind> checkerTypeKinds = TypeMessage.get(positionInfo);
//                        boolean foundChecker = false;
//                        for (CheckerTypeKind checkerTypeKind : checkerTypeKinds) {
//                            if (checkerTypeKind.getCheckername().equals(checker)) {
//                                foundChecker = true;
//                                if (!checkerTypeKind.getTypeToKindMap().containsKey(type)) {
//                                    checkerTypeKind.getTypeToKindMap().put(type, kind);
//                                }
//                                break;
//                            }
//                        }
//                        if (!foundChecker) {
//                            Map<String, String> kindType = new HashMap<>();
//                            kindType.put(type, kind);
//                            checkerTypeKinds.add(new CheckerTypeKind(checker, kindType));
//                        }
//                    }
//                }
//            } else {
//                diagnostics.add(convertToLSPDiagnostic(diagnostic));
//            }
//        }
//        return TypeMessage;
//    }

    /**
     * Publish the given type message for the given file in non-verbose mode.
     *
     * @param entry The Diagnostic entry for getting file name string
     * @param diagnosticString processed for simplicity output
     */
    private void publishTypeMessageWithFilter(
            Map.Entry<String, List<javax.tools.Diagnostic<?>>> entry,
            Map<String, List<CheckerTypeKind>> diagnosticString) {
        for (Map.Entry<String, List<CheckerTypeKind>> typeMessage : diagnosticString.entrySet()) {
            String location = typeMessage.getKey();
            for (CheckerTypeKind checkerTypeKind : typeMessage.getValue()) {
                Map<String, String> typeKinds = checkerTypeKind.getTypeToKindMap();
                for (Map.Entry<String, String> typeKind : typeKinds.entrySet()) {
                    StringBuilder typeMessageBuilder =
                            new StringBuilder(checkerTypeKind.getCheckername());
                    // If there are more than one type message from a checker, add the kind of type
                    // e.g. use/declared. If not, collapse the message.
                    if (typeKinds.size() > 1) {
                        typeMessageBuilder.append(" ").append(typeKind.getValue());
                    }
                    typeMessageBuilder.append(":").append(typeKind.getKey()).append(location);
                    publishTypeMessage(
                            new File(URI.create(entry.getKey())), typeMessageBuilder.toString());
                }
            }
        }
    }

    /**
     * Get checker name from type message in checkerframework.
     *
     * @param typeMessage Type message string from checkerframework
     * @return Checker name from checkerframework e.g. Nullness
     */
    private static String getChecker(String typeMessage) {
        String checkerPrefix = "checker=";
        int checkerStart = typeMessage.indexOf(checkerPrefix);
        if (checkerStart != -1) {
            int checkerEnd = typeMessage.indexOf(";", checkerStart);
            if (checkerEnd != -1) {
                String checker =
                        typeMessage
                                .substring(checkerStart + checkerPrefix.length(), checkerEnd)
                                .trim();
                checker = checker.replace("Subchecker", "").replace("Checker", "").trim();
                return checker;
            }
        }
        return null;
    }

    /**
     * Get message kind from type message in checkerframework.
     *
     * @param typeMessage Type message string from checkerframework
     * @return Lower case message kind from checkerframework e.g. use/declared
     */
    private static String getKind(String typeMessage) {
        String kindPrefix = "kind=";
        int kindStart = typeMessage.indexOf(kindPrefix);
        if (kindStart != -1) {
            int kindEnd = typeMessage.indexOf(";", kindStart);
            if (kindEnd != -1) {
                String kind =
                        typeMessage.substring(kindStart + kindPrefix.length(), kindEnd).trim();
                kind = kind.toLowerCase(Locale.ROOT).replace("_type", "");
                return kind;
            }
        }
        return null;
    }

    /**
     * Get type information from type message in checkerframework.
     *
     * @param typeMessage Type Message String from checkerframework
     * @return Type information from checkerframework e.g. @Nullable Object
     */
    private static String getType(String typeMessage) {
        String typePrefix = "type=";
        int typeStart = typeMessage.indexOf(typePrefix);
        if (typeStart != -1) {
            int typeEnd = typeMessage.indexOf(";", typeStart);
            return typeMessage.substring(typeStart + typePrefix.length(), typeEnd).trim() + "; ";
        }
        return null;
    }

    /**
     * Get position range from type message in checkerframework.
     *
     * @param typeMessage Type Message String from checkerframework
     * @return Position range from checkerframework e.g. range=(11, 8, 11, 9)
     */
    private static String getPosition(String typeMessage) {
        int lastDelimiter = typeMessage.lastIndexOf(';');
        String positionInfo = typeMessage.substring(lastDelimiter + 1).trim();
        return positionInfo;
    }

    /**
     * The hover request is sent from the client to the server to request hover information at a
     * given text document position.
     *
     * <p>Registration Options: TextDocumentRegistrationOptions
     */
    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        int line = params.getPosition().getLine();
        int character = params.getPosition().getCharacter();
        ComparablePosition currentPosition = new ComparablePosition(line, character);
        File curFile = new File(URI.create(params.getTextDocument().getUri()));
        RangeMap<ComparablePosition, List<String>> typeInfo = filesToTypeInfo.get(curFile);

        if (typeInfo != null) {
            List<String> rawTypeInfoForHover = typeInfo.get(currentPosition);
            if (rawTypeInfoForHover != null) {
                MarkupContent typeInfoForHover = new MarkupContent();
                typeInfoForHover.setKind(MarkupKind.PLAINTEXT);
                typeInfoForHover.setValue(String.join("\n", rawTypeInfoForHover));
                Hover result = new Hover(typeInfoForHover);
                return CompletableFuture.completedFuture(result);
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Publish the given type message for the given file.
     *
     * @param file The file to which the type message corresponds.
     * @param msg A type message which contains checker name, message kind, type information, and
     *     the range of this type in the given file, separated by the delimiter ";".
     */
    private void publishTypeMessage(File file, String msg) {
        int lastDelimiter = msg.lastIndexOf(';');
        String typeInfo = msg.substring(0, lastDelimiter);
        String positionInfo = msg.substring(lastDelimiter + 1).trim();
        Matcher rangeMatcher = rangePattern.matcher(positionInfo);
        if (!rangeMatcher.matches()) {
            throw new BugInCF("Failed to parse node position!");
        }

        int startLine = Integer.parseInt(rangeMatcher.group(1));
        int startCol = Integer.parseInt(rangeMatcher.group(2));
        int endLine = Integer.parseInt(rangeMatcher.group(3));
        int endCol = Integer.parseInt(rangeMatcher.group(4));

        ComparablePosition start = new ComparablePosition(startLine, startCol);
        ComparablePosition end = new ComparablePosition(endLine, endCol);

        RangeMap<ComparablePosition, List<String>> currentTypeInfo = filesToTypeInfo.get(file);
        if (currentTypeInfo == null) {
            currentTypeInfo = TreeRangeMap.create();
            filesToTypeInfo.put(file, currentTypeInfo);
        }

        List<String> typeInfoForPosition = currentTypeInfo.get(start);
        if (typeInfoForPosition == null) {
            typeInfoForPosition = new ArrayList<>();
        }

        typeInfoForPosition.add(typeInfo);
        currentTypeInfo.put(
                com.google.common.collect.Range.closed(start, end), typeInfoForPosition);
    }
}
