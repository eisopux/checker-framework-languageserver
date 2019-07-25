package org.checkerframework.languageserver;

import com.google.gson.annotations.SerializedName;

import java.nio.file.Paths;
import java.util.List;

public class Settings {
    @SerializedName(CFLanguageServer.SERVER_NAME)
    Config config;

    /**
     * This class is used for converting from/to JSON as all settings will be under {@link CFLanguageServer.SERVER_NAME}.
     */
    private static class Config {
        final String frameworkPath;
        final List<String> checkers;
        final List<String> commandLineOptions;

        Config(String frameworkPath, List<String> checkers, List<String> commandLineOptions) {
            this.frameworkPath = frameworkPath;
            this.checkers = checkers;
            this.commandLineOptions = commandLineOptions;
        }
    }

    String getJdkPath() {
        return Paths.get(config.frameworkPath, "checker/dist/jdk8.jar").toString();
    }

    String getCheckerPath() {
        return Paths.get(config.frameworkPath, "checker/dist/checker.jar").toString();
    }

    List<String> getCheckers() {
        return config.checkers;
    }

    List<String> getCommandLineOptions() {
        return config.commandLineOptions;
    }
}

