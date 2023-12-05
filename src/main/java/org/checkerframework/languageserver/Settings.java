package org.checkerframework.languageserver;

import com.google.gson.annotations.SerializedName;

import java.nio.file.Paths;
import java.util.List;

/** Setting class for language server setting and used in {@link CFLanguageServer}. */
class Settings {
    /** The configuration. */
    @SerializedName(CFLanguageServer.SERVER_NAME)
    final Config config;

    /** Default constructor for language server settings. */
    Settings(Config config) {
        this.config = config;
    }

    /**
     * This class is used for converting from/to JSON as all settings will be under {@link
     * CFLanguageServer.SERVER_NAME}.
     */
    static class Config {
        /** The framework path. */
        final String frameworkPath;

        /** The checker list has been activated for the language server. */
        final List<String> checkers;

        /** The commandLine options has been added for the language server. */
        final List<String> commandLineOptions;

        /** Default constructor for language server configuration. */
        Config(String frameworkPath, List<String> checkers, List<String> commandLineOptions) {
            this.frameworkPath = frameworkPath;
            this.checkers = checkers;
            this.commandLineOptions = commandLineOptions;
        }
    }

    /** Getter for jdk path. */
    String getJdkPath() {
        return Paths.get(config.frameworkPath, "checker/dist/jdk8.jar").toString();
    }

    /** Getter for checker path. */
    String getCheckerPath() {
        return Paths.get(config.frameworkPath, "checker/dist/checker.jar").toString();
    }

    /** Getter for checkers in config. */
    List<String> getCheckers() {
        return config.checkers;
    }

    /** Getter for commandline options. */
    List<String> getCommandLineOptions() {
        return config.commandLineOptions;
    }
}
