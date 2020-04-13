package org.checkerframework.languageserver;

import java.util.ArrayList;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * JSON wrapper to communicate javac diagnostics from {@link JavacWrapper} to {@link CheckExecutor}.
 */
public class CFDiagnosticList {

    private final List<CFDiagnostic> diags;

    public CFDiagnosticList(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        diags = new ArrayList<>(diagnostics.size());
        for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
            diags.add(new CFDiagnostic(d));
        }
    }

    public List<CFDiagnostic> getDiagnostics() {
        return diags;
    }
}
