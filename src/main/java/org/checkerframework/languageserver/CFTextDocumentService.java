package org.checkerframework.languageserver;

import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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

/** This class does all the dirty works on source files. */
public class CFTextDocumentService implements TextDocumentService, Publisher {

    private static final Logger logger = Logger.getLogger(CFTextDocumentService.class.getName());

    private final CFLanguageServer server;
    private CheckExecutor executor;
    /**
     * Store Type refinement hover information for each file. Map key is file uri, value is a
     * rangemap that stores position of variables and message
     */
    private final Map<String, RangeMap<ComparablePosition, String>> typeRefinementMapping =
            new HashMap<>();

    CFTextDocumentService(CFLanguageServer server) {
        this.server = server;
    }

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
        files.forEach(
                file -> {
                    String fileURI = file.toURI().toString();
                    String typeRefinementKey = convertFileURIToDocURI(fileURI);
                    if (typeRefinementMapping.containsKey(typeRefinementKey)) {
                        typeRefinementMapping.get(typeRefinementKey).clear();
                    }
                    server.publishDiagnostics(
                            new PublishDiagnosticsParams(fileURI, Collections.emptyList()));
                });
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
        clearDiagnostics(
                Collections.singletonList(new File(URI.create(params.getTextDocument().getUri()))));
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
        clearDiagnostics(
                Collections.singletonList(new File(URI.create(params.getTextDocument().getUri()))));
        checkAndPublish(
                Collections.singletonList(new File(URI.create(params.getTextDocument().getUri()))));
    }

    /** Diagnostic Kind NOTE is used for Type Refinements */
    @Override
    public void publish(Map<String, List<javax.tools.Diagnostic<?>>> result) {
        for (Map.Entry<String, List<javax.tools.Diagnostic<?>>> entry : result.entrySet()) {
            server.publishDiagnostics(
                    new PublishDiagnosticsParams(
                            entry.getKey(),
                            entry.getValue().stream()
                                    .filter(x -> x.getKind() != javax.tools.Diagnostic.Kind.NOTE)
                                    .map(this::convertToLSPDiagnostic)
                                    .collect(Collectors.toList())));
            String currentKey = convertEntryKeyToDocURI(entry.getKey());
            entry.getValue().stream()
                    .filter(x -> x.getKind() == javax.tools.Diagnostic.Kind.NOTE)
                    .forEach(
                            i -> {
                                publishTypeRefinementhHelper(i, currentKey);
                            });
        }
    }

    /**
     * The hover request is sent from the client to the server to request hover information at a
     * given text document position.
     *
     * <p>Registration Options: TextDocumentRegistrationOptions
     */
    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        // line 1 on vscode is 0
        int line = params.getPosition().getLine();
        int character = params.getPosition().getCharacter();
        ComparablePosition currentPosition = new ComparablePosition(new Position(line, character));
        String curFile = params.getTextDocument().getUri();

        if (typeRefinementMapping.containsKey(curFile)
                && typeRefinementMapping.get(curFile).get(currentPosition) != null) {
            Hover result =
                    new Hover(
                            new MarkupContent(
                                    MarkupKind.PLAINTEXT,
                                    typeRefinementMapping.get(curFile).get(currentPosition)));
            return CompletableFuture.completedFuture(result);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Add Type Refinement Diagnostics into map. Checks variable name length using following:
     *
     * @see <a href="https://github.com/opprop/checker-framework/pull/178">specification</a>
     */
    private void publishTypeRefinementhHelper(javax.tools.Diagnostic<?> i, String currentKey) {
        ComparablePosition start =
                new ComparablePosition(
                        new Position((int) i.getLineNumber() - 1, (int) i.getColumnNumber() - 1));
        String msg = i.getMessage(Locale.getDefault());
        int nameLength = msg.split("variable=")[1].split("\"")[1].length();
        ComparablePosition end =
                new ComparablePosition(
                        new Position(
                                (int) i.getLineNumber() - 1,
                                ((int) i.getColumnNumber()) + nameLength - 1));
        String displayMsg = msg.substring(msg.indexOf(",") + 1);
        if (!typeRefinementMapping.containsKey(currentKey)) {
            RangeMap<ComparablePosition, String> rangeMap = TreeRangeMap.create();
            typeRefinementMapping.put(currentKey, rangeMap);
        }

        RangeMap<ComparablePosition, String> currentTypeRefinement =
                typeRefinementMapping.get(currentKey);
        if (currentTypeRefinement.get(start) == null) {
            typeRefinementMapping
                    .get(currentKey)
                    .put(com.google.common.collect.Range.closed(start, end), displayMsg);
        } else {
            currentTypeRefinement.put(
                    com.google.common.collect.Range.closed(start, end),
                    currentTypeRefinement.get(start) + "\n" + displayMsg);
        }
    }

    private String convertEntryKeyToDocURI(String entryKey) {
        return entryKey.replace("C:", "c%3A").replace("D:", "d%3A");
    }

    private String convertFileURIToDocURI(String fileURI) {
        return fileURI.replace("file:/", "file:///").replace("c:", "c%3A").replace("d:", "d%3a");
    }
}
