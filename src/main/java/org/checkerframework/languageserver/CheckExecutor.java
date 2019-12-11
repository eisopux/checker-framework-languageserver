package org.checkerframework.languageserver;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gson.Gson;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import org.checkerframework.framework.util.CheckerMain;

/** Used to run the checker framework and collect results. */
class CheckExecutor {

    private static final Logger logger = Logger.getLogger(CheckExecutor.class.getName());

    private final Publisher publisher;
    private final List<String> options;
    private final Gson gson;
    private final Process wrapper;

    CheckExecutor(
            Publisher publisher,
            String jdkPath,
            String checkerPath,
            List<String> checkers,
            List<String> commandLineOptions)
            throws IOException {
        this.publisher = publisher;
        List<String> opts = new ArrayList<>();
        // adapted from
        // checker-framework/framework-test/src/main/java/org/checkerframework/framework/test/TypecheckExecutor.java
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

        logger.info("java.version is: " + System.getProperty("java.version"));

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
                options.add(
                        o
                                + File.pathSeparator
                                + JavacWrapper.class
                                        .getProtectionDomain()
                                        .getCodeSource()
                                        .getLocation()
                                        .getPath());
            } else {
                options.add(o);
            }
        }

        gson = new Gson();
        logger.info(String.join(" ", options));
        wrapper = Runtime.getRuntime().exec(options.toArray(new String[0]));
        ;
        new Thread(new Receiver()).start();
    }

    /**
     * Run type check against source files.
     *
     * @param files the files to be checked
     */
    void compile(List<File> files) {
        if (files.isEmpty()) return;

        files.forEach(f -> logger.info("checking: " + f));

        StringBuilder sb = new StringBuilder();
        try {
            for (File f : files) {
                sb.append(f.getCanonicalPath()).append("\n");
            }
            OutputStreamWriter osw = new OutputStreamWriter(wrapper.getOutputStream(), UTF_8);
            osw.write(sb.toString());
            osw.flush();
        } catch (IOException e) {
            logger.warning("Failed to check: " + e.toString());
        }
    }

    // This class runs in the background in a thread and receives the output of JavacWrapper.
    // Diagnostics received by it will then be sent to the editor.
    private class Receiver implements Runnable {
        @Override
        public void run() {
            InputStream stdout = wrapper.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout, UTF_8));
            while (true) {
                try {
                    String diag = br.readLine();
                    logger.info("Got from wrapper: " + diag);
                    DiagnosticList diags = gson.fromJson(diag, DiagnosticList.class);
                    Map<String, List<javax.tools.Diagnostic>> ret = new HashMap<>();
                    for (CFDiagnostic d : diags.diags) {
                        String s = (String) d.getSource();
                        if (!ret.containsKey(s)) {
                            ret.put(s, new ArrayList<>());
                        }
                        ret.get(s).add(d);
                    }

                    publisher.publish(ret);
                } catch (IOException e) {
                    logger.warning("Failed to read the output of wrapper: " + e.toString());
                }
            }
        }
    }
}
