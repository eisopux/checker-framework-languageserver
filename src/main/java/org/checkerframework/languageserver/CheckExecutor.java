package org.checkerframework.languageserver;

import javax.tools.*;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import com.google.gson.Gson;
import org.checkerframework.framework.util.CheckerMain;

/**
 * Used to run the checker framework and collect results.
 */
class CheckExecutor {

    private static final Logger logger = Logger.getLogger(CheckExecutor.class.getName());

    private final List<String> options;
    private final Gson gson;

    CheckExecutor(String jdkPath, String checkerPath, List<String> checkers, List<String> commandLineOptions) {
        List<String> opts = new ArrayList<>();
        // adapted from checker-framework/framework-test/src/main/java/org/checkerframework/framework/test/TypecheckExecutor.java
        // Even though the method compiler.getTask takes a list of processors, it fails if
        // processors are passed this way with the message:
        // error: Class names, 'org.checkerframework.checker.interning.InterningChecker', are only
        // accepted if annotation processing is explicitly requested
        // Therefore, we now add them to the beginning of the options list.
        opts.add("-processor");
        opts.add(String.join(",", checkers));
        opts.add("-Xbootclasspath/p:" + jdkPath);
        opts.add("-processorpath");
        opts.add(checkerPath);
        opts.add("-proc:only");
        opts.addAll(commandLineOptions);

        CheckerMain cm = new CheckerMain(new File(checkerPath), opts);
        options = new ArrayList<>();
        boolean sawClasspath = false;
        Iterator<String> it = cm.getExecArguments().iterator();
        while (it.hasNext()) {
            String o = it.next();
            if (o.equals("com.sun.tools.javac.Main")) {
                options.add(JavacWrapper.class.getCanonicalName());
            } else if (!sawClasspath && (o.equals("-cp") || o.equals("-classpath"))) {
                sawClasspath = true;
                options.add(o);
                o = it.next();
                options.add(o + File.pathSeparator + JavacWrapper.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            } else {
                options.add(o);
            }
        }

        gson = new Gson();
    }

    /**
     * Run type check against source files.
     *
     * @param files the files to be checked
     * @return raw diagnostics grouped by JavaFileObject
     */
    Map<String, List<Diagnostic>> compile(List<File> files) {
        if (files.isEmpty())
            return Collections.emptyMap();

        files.forEach(f -> logger.info("checking: " + f));

        List<String> opts = new ArrayList<>(options);
        Map<String, List<Diagnostic>> ret = null;

        try {
            for (File f: files) {
                opts.add(f.getCanonicalPath());
            }
            StringBuilder sb = new StringBuilder();
            for (String s: opts) {
                sb.append(s).append(" ");
            }
            logger.info(this.getClass().getName() + " javac options: " + sb.toString());

            Process proc = Runtime.getRuntime().exec(opts.toArray(new String[0]));
            proc.waitFor();
            DiagnosticList diags = gson.fromJson(new InputStreamReader(proc.getInputStream()), DiagnosticList.class);

            ret = new HashMap<>();
            for (CFDiagnostic d: diags.diags) {
                String s = (String)d.getSource();
                if (!ret.containsKey(s)) {
                    ret.put(s, new ArrayList<>());
                }
                ret.get(s).add(d);
            }
        } catch (Exception e) {
            logger.info(e.toString());
        }

        return ret;
    }
}
