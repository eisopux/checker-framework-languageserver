package org.checkerframework.languageserver;

import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * JSON wrapper to communicate javac diagnostics from {@link JavacWrapper} to {@link CheckExecutor}.
 */
public class CFDiagnosticList {
    /** The diagnostics. */
    private final List<CFDiagnostic> diags;

    /** Default constructor for checker framework diagnostics list. */
    public CFDiagnosticList(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        diags = new ArrayList<>(diagnostics.size());
        for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
            diags.add(new CFDiagnostic(d));
        }
    }

    /** Getter for checker framework diagnostics. */
    public List<CFDiagnostic> getDiagnostics() {
        return diags;
    }
}
