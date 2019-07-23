package org.checkerframework.languageserver;

import javax.tools.*;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Used to run the checkerframework and collect results.
 */
class CheckExecutor {

    private static final Logger logger = Logger.getLogger(CheckExecutor.class.getName());

    private final List<String> options;
    private final JavaCompiler compiler;
    private StandardJavaFileManager fileManager;

    CheckExecutor(String jdkPath, String checkerPath, List<String> checkers, List<String> commandLineOptions) {
        compiler = ToolProvider.getSystemJavaCompiler();
        fileManager = compiler.getStandardFileManager(null, null, null);

        // adapted from checker-framework/framework-test/src/main/java/org/checkerframework/framework/test/TypecheckExecutor.java
        options = new ArrayList<>();
        // Even though the method compiler.getTask takes a list of processors, it fails if
        // processors are passed this way with the message:
        // error: Class names, 'org.checkerframework.checker.interning.InterningChecker', are only
        // accepted if annotation processing is explicitly requested
        // Therefore, we now add them to the beginning of the options list.
        options.add("-processor");
        options.add(String.join(",", checkers));
        options.add("-Xbootclasspath/p:" + jdkPath);
        options.add("-processorpath");
        options.add(checkerPath);
        options.add("-proc:only");
        options.addAll(commandLineOptions);
    }

    Map<JavaFileObject, List<Diagnostic<? extends JavaFileObject>>> compile(List<File> files) {
        if (files.isEmpty())
            return Collections.emptyMap();

        files.forEach(f -> logger.info("checking: " + f.getPath()));

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        Iterable<? extends JavaFileObject> javaFiles = fileManager.getJavaFileObjectsFromFiles(files);

        compiler
                .getTask(new StringWriter(), fileManager, diagnostics, options, new ArrayList<String>(), javaFiles)
                .call();

        return diagnostics
                .getDiagnostics()
                .stream()
                .collect(Collectors.groupingBy(Diagnostic::getSource));
    }
}
