package org.checkerframework.languageserver;

import com.google.gson.Gson;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.logging.Logger;

/** This class is for workspace service and used in {@link CFLanguageServer}. */
public class CFWorkspaceService implements WorkspaceService {
    /** The logger for checker framework workspace service. */
    private static final Logger logger = Logger.getLogger(CFWorkspaceService.class.getName());

    /** The checker framework language server for workspace service. */
    private final CFLanguageServer server;

    /** The gson for checker framework workspace service. */
    private final Gson gson;

    /** Default constructor for checker framework workspace service. */
    CFWorkspaceService(CFLanguageServer server) {
        this.server = server;
        this.gson = new Gson();
    }

    /**
     * A notification sent from the client to the server to signal the change of configuration
     * settings.
     *
     * @see <a
     *     href="https://microsoft.github.io/language-server-protocol/specification#workspace_didChangeConfiguration">specification</a>
     */
    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        logger.info(params.toString());
        server.didChangeConfiguration(
                gson.fromJson(gson.toJson(params.getSettings()), Settings.class));
    }

    /**
     * The watched files notification is sent from the client to the server when the client detects
     * changes to file watched by the language client.
     *
     * @see <a
     *     href="https://microsoft.github.io/language-server-protocol/specification#workspace_didChangeWatchedFiles">specification</a>
     */
    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {}
}
