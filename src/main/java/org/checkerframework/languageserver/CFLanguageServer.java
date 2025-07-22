package org.checkerframework.languageserver;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/** The actual language server, responsible for communicating with the client (editor). */
public class CFLanguageServer implements LanguageServer, LanguageClientAware {
    /** The logger for issuing information in the language server. */
    private static final Logger logger = Logger.getLogger(CFLanguageServer.class.getName());

    /** Name of the server and settings block. */
    public static final String SERVER_NAME = "checker-framework";

    /** The language client. */
    private LanguageClient client;

    /** The settings instance. */
    private Settings settings;

    /** The Checker Framework executor. */
    private final CheckExecutor executor;

    /** The Checker Framework document service. */
    private final CFTextDocumentService textDocumentService;

    /** The Checker Framework workspace service. */
    private final CFWorkspaceService workspaceService;

    /** Default constructor for Checker Framework language server. */
    CFLanguageServer(Settings settings) throws IOException {
        this.settings = settings;
        this.textDocumentService = new CFTextDocumentService(this);
        this.executor = buildExecutor();
        this.textDocumentService.setExecutor(this.executor);
        this.client = null;
        this.workspaceService = new CFWorkspaceService(this);
    }

    /** The function for building the executor for checks. */
    private CheckExecutor buildExecutor() throws IOException {
        String checker = settings.getCheckerPath();
        logger.info("Launching CheckExecutor using " + checker);
        return new CheckExecutor(
                this.textDocumentService,
                settings.getJdkPath(),
                settings.getCheckerPath(),
                settings.getCheckers(),
                settings.getCommandLineOptions());
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }

    /**
     * The initialize request is sent as the first request from the client to the server.
     *
     * <p>If the server receives request or notification before the initialize request it should act
     * as follows: - for a request the response should be errored with code: -32001. The message can
     * be picked by the server. - notifications should be dropped, except for the exit notification.
     * This will allow the exit a server without an initialize request.
     *
     * <p>Until the server has responded to the initialize request with an InitializeResult the
     * client must not send any additional requests or notifications to the server.
     *
     * <p>During the initialize request the server is allowed to send the notifications
     * window/showMessage, window/logMessage and telemetry/event as well as the
     * window/showMessageRequest request to the client.
     *
     * @see <a
     *     href="https://microsoft.github.io/language-server-protocol/specification#initialize">specification</a>
     */
    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        capabilities.setHoverProvider(true);
        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }

    /**
     * The shutdown request is sent from the client to the server. It asks the server to shutdown,
     * but to not exit (otherwise the response might not be delivered correctly to the client).
     * There is a separate exit notification that asks the server to exit.
     */
    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(null);
    }

    /** A notification to ask the server to exit its process. */
    @Override
    public void exit() {
        // no operation
        logger.info("Terminating");
        System.exit(0);
    }

    /** Provides access to the textDocument services. */
    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }

    /** Provides access to the workspace services. */
    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    /**
     * Accepts a new configuration set by the user (called from {@link CFWorkspaceService}). The new
     * configuration is then passed to {@link CFTextDocumentService}.
     *
     * @param settings the new settings
     */
    void didChangeConfiguration(Settings settings) {
        this.settings = settings;
        try {
            textDocumentService.setExecutor(buildExecutor());
        } catch (IOException e) {
            logger.severe("Failed to change configuration: " + e.toString());
        }
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
