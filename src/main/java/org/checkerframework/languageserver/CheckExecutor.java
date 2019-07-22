package org.checkerframework.languageserver;

import javax.tools.*;
import java.util.List;
import java.util.logging.Logger;


/**
 * Used to run the checkerframework and collect results.
 */
public class CheckExecutor {

    private static final Logger logger = Logger.getLogger(CheckExecutor.class.getName());

    private final String jdkPath;
    private final String checkerPath;
    private final List<String> checkers;
    private final List<String> commandLineOptions;

    private final JavaCompiler compiler;
    private StandardJavaFileManager fileManager;

    CheckExecutor(String jdkPath, String checkerPath, List<String> checkers, List<String> commandLineOptions) {
        this.jdkPath = jdkPath;
        this.checkerPath = checkerPath;
        this.checkers = checkers;
        this.commandLineOptions = commandLineOptions;
        this.compiler = ToolProvider.getSystemJavaCompiler();
        this.fileManager = compiler.getStandardFileManager(null, null, null);
    }
}
