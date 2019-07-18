package org.checkerframework.lsp;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;

public class CFWorkspaceService implements WorkspaceService {

    private final CFLanguageServer server;

    CFWorkspaceService(CFLanguageServer server) {
        this.server = server;
    }

    /**
     * A notification sent from the client to the server to signal the change of
     * configuration settings.
     *
     * @param params
     */
    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {

    }

    /**
     * The watched files notification is sent from the client to the server when
     * the client detects changes to file watched by the language client.
     *
     * @param params
     */
    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {

    }
}
