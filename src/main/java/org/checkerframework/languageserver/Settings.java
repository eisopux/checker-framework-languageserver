package org.checkerframework.languageserver;

import com.google.gson.annotations.SerializedName;

import java.nio.file.Paths;
import java.util.List;

public class Settings {
    @SerializedName("checker-framework")
    Config config;

    public static class Config {
        private final String frameworkPath;
        private final List<String> checkers;
        private final List<String> commandLintOptions;

        Config(String frameworkPath, List<String> checkers, List<String> commandLineOptions) {
            this.frameworkPath = frameworkPath;
            this.checkers = checkers;
            this.commandLintOptions = commandLineOptions;
        }

        String getJdkPath() {
            return Paths.get(frameworkPath, "checker/dist/jdk8.jar").toString();
        }

        String getCheckerPath() {
            return Paths.get(frameworkPath, "checker/dist/checker.jar").toString();
        }

        List<String> getCheckers() {
            return checkers;
        }

        List<String> getCommandLintOptions() {
            return commandLintOptions;
        }
    }
}

