package org.checkerframework.languageserver;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * This class wraps around javac (com.sun.tools.javac) in order to output diagnostics. It passes
 * parameters transparently to javac, and so for other classes it behaves exactly the same as javac
 * and can substitute com.sun.tools.javac.Main.
 *
 * <p>The main reads file paths from standard input and compiles those files.
 *
 * <p>The resulting javac diagnostics are printed to standard output in the {@link CFDiagnosticList}
 * JSON format.
 *
 * <p>{@link CheckExecutor} uses the {@code JavacWrapper} to compile individual files without having
 * to start up a new process.
 */
public class JavacWrapper {

    private final List<String> options;
    private final JavaCompiler javac;
    private final StandardJavaFileManager fileManager;
    private final Gson gson;

    public static void main(String[] args) {
        JavacWrapper javacw = new JavacWrapper(args);
        Scanner input = new Scanner(System.in, UTF_8.name());

        while (input.hasNextLine()) {
            javacw.compile(input.nextLine());
        }
    }

    private JavacWrapper(String[] args) {
        options = new ArrayList<>();
        options.addAll(Arrays.asList(args));
        javac = ToolProvider.getSystemJavaCompiler();
        fileManager = javac.getStandardFileManager(null, null, null);
        gson = new Gson();
        // To make debugging easier, use the following instead:
        // gson = new GsonBuilder().setPrettyPrinting().create();
    }

    private void compile(String f) {
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        Iterable<? extends JavaFileObject> javaFiles =
                fileManager.getJavaFileObjectsFromStrings(Collections.singletonList(f));
        javac.getTask(null, null, diagnostics, options, null, javaFiles).call();

        CFDiagnosticList diags = new CFDiagnosticList(diagnostics.getDiagnostics());
        System.out.println(gson.toJson(diags, CFDiagnosticList.class));
    }
}
