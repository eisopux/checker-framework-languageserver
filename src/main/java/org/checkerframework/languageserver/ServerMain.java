package org.checkerframework.languageserver;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * This class is the launcher of the language server.
 */
public class ServerMain {

    private static final Logger logger = Logger.getLogger(ServerMain.class.getName());
    private static final String OPT_FRAMEWORKPATH = "frameworkPath";
    private static final String OPT_CHECKERS = "checkers";
    private static final String OPT_COMMANDLINEOPTIONS = "commandLineOptions";


    /**
     * The entry point of application. Sets up and launches {@link CFLanguageServer}.
     *
     * @param args the input arguments
     * @see <a href="https://github.com/eclipse/lsp4j/blob/7fd8daebc61c1e8c83a0a3f016563e1896582db2/documentation/README.md#launch-and-connect-with-a-languageclient">Launch a language server</a>
     */
    public static void main(String[] args) {
        try {
            Settings settings = getSettings(args);
            logger.info("Launching checker framework languageserver");
            CFLanguageServer server = new CFLanguageServer(settings);
            Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
            LanguageClient client = launcher.getRemoteProxy();
            server.connect(client);
            launcher.startListening();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            System.exit(1);
        }
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addRequiredOption(OPT_FRAMEWORKPATH, OPT_FRAMEWORKPATH, true, "Absolute path of the Checker Framework");
        options.addRequiredOption(OPT_CHECKERS, OPT_CHECKERS, true, "List of checkers enabled for compilation");
        options.addOption(OPT_COMMANDLINEOPTIONS, OPT_COMMANDLINEOPTIONS, true, "List of command line options that gets passed in to javac");
        return options;
    }

    private static Settings getSettings(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(getOptions(), args);
        String fp = "";
        String[] checkers = new String[0];
        String[] cmo = new String[0];
        if (cmd.hasOption(OPT_FRAMEWORKPATH)) {
            fp = cmd.getOptionValue(OPT_FRAMEWORKPATH);
            logger.info("got frameworkPath " + fp);
        }
        if (cmd.hasOption(OPT_CHECKERS)) {
            checkers = cmd.getOptionValues(OPT_CHECKERS);
            logger.info("got checkers " + Arrays.toString(checkers));
        }
        if (cmd.hasOption(OPT_COMMANDLINEOPTIONS)) {
            cmo = cmd.getOptionValues(OPT_COMMANDLINEOPTIONS);
            logger.info("got cliOptions " + Arrays.toString(cmo));
        }
        return new Settings(new Settings.Config(fp, Arrays.asList(checkers), Arrays.asList(cmo)));
    }
}
