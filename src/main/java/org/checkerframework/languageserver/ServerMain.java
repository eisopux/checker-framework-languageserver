package org.checkerframework.languageserver;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * This class is the launcher of the language server.
 */
public class ServerMain {
    /**
     * The entry point of application. Sets up and launches the {@link org.checkerframework.languageserver.CFLanguageServer}.
     *
     * @see <a href="https://github.com/eclipse/lsp4j/blob/7fd8daebc61c1e8c83a0a3f016563e1896582db2/documentation/README.md#launch-and-connect-with-a-languageclient">Launch a language server</a>
     * @param args the input arguments
     */

    private static final Logger logger = Logger.getLogger(ServerMain.class.getName());

    public static void main(String[] args) {
        try {
            CFLanguageServer server = new CFLanguageServer();
            // TODO: socket or stdin/stdout?
            Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
            LanguageClient client = launcher.getRemoteProxy();
            server.connect(client);
            launcher.startListening();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            System.exit(1);
        }
    }
}
