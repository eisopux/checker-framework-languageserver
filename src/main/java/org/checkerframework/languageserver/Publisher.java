package org.checkerframework.languageserver;

import java.util.List;
import java.util.Map;
import javax.tools.Diagnostic;

/** A publisher for javac diagnostic results. */
public interface Publisher {
    /**
     * Publish diagnostic results.
     *
     * @param diagnostics mapping from resource location to list of javac diagnostics
     */
    void publish(Map<String, List<Diagnostic<?>>> diagnostics);
}
