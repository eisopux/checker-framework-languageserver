package org.checkerframework.languageserver;

import com.google.gson.Gson;
import java.util.*;
import javax.tools.*;

/**
 * This class wraps around javac (com.sun.tools.javac) in order to output diagnostics. It passes
 * parameters transparently to javac, and so for other classes it behaves exactly the same as javac
 * and can substitute com.sun.tools.javac.Main.
 */
public class JavacWrapper {

    private final List<String> options;
    private final JavaCompiler javac;
    private final StandardJavaFileManager fileManager;

    public static void main(String[] args) {
        JavacWrapper javacw = new JavacWrapper(args);
        Scanner input = new Scanner(System.in);

        while (input.hasNextLine()) {
            javacw.compile(input.nextLine());
        }
    }

    JavacWrapper(String[] args) {
        options = new ArrayList<>();
        options.addAll(Arrays.asList(args));
        javac = ToolProvider.getSystemJavaCompiler();
        fileManager = javac.getStandardFileManager(null, null, null);
    }

    void compile(String f) {
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        Iterable<? extends JavaFileObject> javaFiles =
                fileManager.getJavaFileObjectsFromStrings(Collections.singletonList(f));
        javac.getTask(null, null, diagnostics, options, null, javaFiles).call();

        DiagnosticList diags = new DiagnosticList(diagnostics.getDiagnostics());
        System.out.println(new Gson().toJson(diags, DiagnosticList.class));
    }
}
