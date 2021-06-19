package org.checkerframework.languageserver;

import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;
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
    private RangeMap<Integer, String> typeRefinementMapping = TreeRangeMap.create();

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
                file ->
                        server.publishDiagnostics(
                                new PublishDiagnosticsParams(
                                        file.toURI().toString(), Collections.emptyList())));
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

    @Override
    public void publish(Map<String, List<javax.tools.Diagnostic<?>>> result) {
        for (Map.Entry<String, List<javax.tools.Diagnostic<?>>> entry : result.entrySet()) {
            server.publishDiagnostics(
                    new PublishDiagnosticsParams(
                            entry.getKey(),
                            entry.getValue().stream()
                                    .map(this::convertToLSPDiagnostic)
                                    .collect(Collectors.toList())));
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
        generateTestData();
        // line 1 on vscode is 0 
        int line = params.getPosition().getLine();
        logger.info(params.toString());

        if(typeRefinementMapping.get(line) != null) {
            Hover result = new Hover(new MarkupContent(MarkupKind.PLAINTEXT, typeRefinementMapping.get(line)));
            return CompletableFuture.completedFuture(result);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    private void generateTestData() {
        typeRefinementMapping.put(com.google.common.collect.Range.closed(1, 2), "type 1");
        typeRefinementMapping.put(com.google.common.collect.Range.closed(3, 4), "type 2");
        typeRefinementMapping.put(com.google.common.collect.Range.closed(7, 9), "type 3");
    }
}
