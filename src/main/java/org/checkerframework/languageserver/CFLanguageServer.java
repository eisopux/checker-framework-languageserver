package org.checkerframework.languageserver;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * The actual language server, responsible for communicating with the client (editor).
 */
public class CFLanguageServer implements LanguageServer, LanguageClientAware {

    private static final Logger logger = Logger.getLogger(CFLanguageServer.class.getName());

    private LanguageClient client;

    private final CFTextDocumentService textDocumentService;
    private final CFWorkspaceService workspaceService;

    public CFLanguageServer() {
        this.client = null;
        this.textDocumentService = new CFTextDocumentService(this);
        this.workspaceService = new CFWorkspaceService(this);
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }

    /**
     * The initialize request is sent as the first request from the client to
     * the server.
     * <p>
     * If the server receives request or notification before the initialize request it should act as follows:
     * - for a request the respond should be errored with code: -32001. The message can be picked by the server.
     * - notifications should be dropped, except for the exit notification. This will allow the exit a server without an initialize request.
     * <p>
     * Until the server has responded to the initialize request with an InitializeResult
     * the client must not sent any additional requests or notifications to the server.
     * <p>
     * During the initialize request the server is allowed to sent the notifications window/showMessage,
     * window/logMessage and telemetry/event as well as the window/showMessageRequest request to the client.
     *
     * @see <a href="https://microsoft.github.io/language-server-protocol/specification#initialize">specification</a>
     */
    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }

    /**
     * The shutdown request is sent from the client to the server. It asks the
     * server to shutdown, but to not exit (otherwise the response might not be
     * delivered correctly to the client). There is a separate exit notification
     * that asks the server to exit.
     */
    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * A notification to ask the server to exit its process.
     */
    @Override
    public void exit() {
        // no operation
        logger.info("Terminating");
        System.exit(0);
    }

    /**
     * Provides access to the textDocument services.
     */
    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }

    /**
     * Provides access to the workspace services.
     */
    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    /**
     * Accepts a new configuration set by the user (called from {@link CFWorkspaceService}).
     * The new configuration is then passed to {@link CFTextDocumentService}.
     *
     * @param config the new configuration
     */
    void didChangeConfiguration(Settings.Config config) {
        textDocumentService.didChangeConfiguration(config);
    }

    /**
     * Publish diagnostics from {@link CFTextDocumentService} to the client.
     *
     * @param params the diagnostics
     */
    void publishDiagnostics(PublishDiagnosticsParams params) {
        logger.info(params.toString());
        client.publishDiagnostics(params);
    }
}
