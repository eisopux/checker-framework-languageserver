package org.checkerframework.languageserver;

import java.util.ArrayList;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class DiagnosticList {

    private final List<CFDiagnostic> diags;

    public DiagnosticList(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        diags = new ArrayList<>();
        for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
            diags.add(new CFDiagnostic(d));
        }
    }

    public List<CFDiagnostic> getDiagnostics() {
        return diags;
    }
}
