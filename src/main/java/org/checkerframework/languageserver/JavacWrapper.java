package org.checkerframework.languageserver;

import com.google.gson.Gson;

import javax.tools.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class wraps around javac (com.sun.tools.javac) in order to output diagnostics.
 * It passes parameters transparently to javac, and so for other classes it behaves exactly the same as javac and can
 * substitute com.sun.tools.javac.Main.
 */
public class JavacWrapper {

    public static void main(String[] args) {
        JavacWrapper javacw = new JavacWrapper();
        javacw.compile(args);
    }

    public void compile(String[] args) {
        List<String> options = new ArrayList<>();
        List<String> sourcefiles = new ArrayList<>();
        separateOptionsAndFiles(args, options, sourcefiles);

        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = javac.getStandardFileManager(null, null, null);
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        Iterable<? extends JavaFileObject> javaFiles = fileManager.getJavaFileObjectsFromStrings(sourcefiles);

        javac
                .getTask(null, null, diagnostics, options, null, javaFiles)
                .call();

        DiagnosticList diags = new DiagnosticList(diagnostics.getDiagnostics());
        System.out.println(new Gson().toJson(diags, DiagnosticList.class));
    }

    private void separateOptionsAndFiles(String[] args, List<String> options, List<String> sourcefiles) {
        for (String a: args) {
            if (a.endsWith(".java"))
                sourcefiles.add(a);
            else options.add(a);
        }
    }
}
