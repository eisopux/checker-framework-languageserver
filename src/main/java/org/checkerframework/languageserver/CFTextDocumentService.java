package org.checkerframework.languageserver;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;

import javax.tools.JavaFileObject;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CFTextDocumentService implements TextDocumentService {

    private static final Logger logger = Logger.getLogger(CFTextDocumentService.class.getName());

    private final CFLanguageServer server;
    private CheckExecutor executor;

    CFTextDocumentService(CFLanguageServer server) {
        this.server = server;
    }

    /**
     * Accepts a new configuration from {@link CFLanguageServer}.
     *
     * @param config the new configuration, containing parameters for the underlying checker.
     */
    public void didChangeConfiguration(Settings.Config config) {
        executor = new CheckExecutor(
                config.getJdkPath(),
                config.getCheckerPath(),
                config.getCheckers(),
                config.getCommandLineOptions()
        );
    }

    private Diagnostic convert(javax.tools.Diagnostic<? extends JavaFileObject> diagnostic) {
        return new Diagnostic(
                new Range(
                        new Position(
                                (int)diagnostic.getLineNumber() - 1,
                                (int)diagnostic.getColumnNumber() - 1
                        ),
                        new Position(
                                (int)diagnostic.getLineNumber() - 1,
                                (int)(diagnostic.getColumnNumber() + diagnostic.getEndPosition() - diagnostic.getStartPosition() - 1)
                        )
                ),
                diagnostic.getMessage(null),
                DiagnosticSeverity.Error,
                "checker-framework",
                diagnostic.getCode()
        );
    }

    private void checkAndPublish(List<File> files) {
        Map<JavaFileObject, List<javax.tools.Diagnostic<? extends JavaFileObject>>> result = executor.compile(files);
        for (Map.Entry<JavaFileObject, List<javax.tools.Diagnostic<? extends JavaFileObject>>> entry: result.entrySet()) {
            server.publishDiagnostics(new PublishDiagnosticsParams(
                    entry.getKey().toUri().toString(),
                    entry.getValue().stream().map(this::convert).collect(Collectors.toList())
            ));
        }
    }

    /**
     * The document open notification is sent from the client to the server to
     * signal newly opened text documents. The document's truth is now managed
     * by the client and the server must not try to read the document's truth
     * using the document's uri.
     * <p>
     * Registration Options: TextDocumentRegistrationOptions
     *
     * @param params
     */
    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        logger.info(params.toString());
        checkAndPublish(Collections.singletonList(new File(URI.create(params.getTextDocument().getUri()))));
    }

    /**
     * The document change notification is sent from the client to the server to
     * signal changes to a text document.
     * <p>
     * Registration Options: TextDocumentChangeRegistrationOptions
     *
     * @param params
     */
    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        logger.info("didChange called");
    }

    /**
     * The document close notification is sent from the client to the server
     * when the document got closed in the client. The document's truth now
     * exists where the document's uri points to (e.g. if the document's uri is
     * a file uri the truth now exists on disk).
     * <p>
     * Registration Options: TextDocumentRegistrationOptions
     *
     * @param params
     */
    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        logger.info("didClose called");
    }

    /**
     * The document save notification is sent from the client to the server when
     * the document for saved in the client.
     * <p>
     * Registration Options: TextDocumentSaveRegistrationOptions
     *
     * @param params
     */
    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        logger.info("didSave called");
    }
}
