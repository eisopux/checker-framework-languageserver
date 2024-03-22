package org.checkerframework.languageserver.quickfix;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Quick fix provider for null pointer dereference diagnostics. */
public class NullPointerDereferenceQuickFixProvider implements QuickFixProvider {
    /**
     * Check if the provider can handle a diagnostic.
     *
     * @param diagnostic the diagnostic
     * @return true if the provider can handle the diagnostic
     */
    @Override
    public boolean canHandle(Diagnostic diagnostic) {
        return diagnostic.getMessage().contains("dereference.of.nullable");
    }

    /**
     * Get the quick fixes for a diagnostic.
     *
     * @param diagnostic the diagnostic
     * @param params the code action parameters
     * @return the quick fixes
     */
    @Override
    public List<Either<Command, CodeAction>> getQuickFixes(
            Diagnostic diagnostic, CodeActionParams params) {
        CodeAction codeAction = new CodeAction("Add null check");
        codeAction.setKind(CodeActionKind.QuickFix);
        String uri = params.getTextDocument().getUri();
        Range range = params.getRange();
        String str = QuickFixProvider.getContentInRange(uri, range);
        List<TextEdit> editList = new ArrayList<>();
        TextEdit edit1 =
                new TextEdit(
                        new Range(
                                new Position(
                                        params.getRange().getStart().getLine(),
                                        params.getRange().getStart().getCharacter()),
                                new Position(
                                        params.getRange().getEnd().getLine(),
                                        params.getRange().getEnd().getCharacter())),
                        " if (" + str + " != null) {\n" + "            " + str + ".toString();\n");
        TextEdit edit2 =
                new TextEdit(
                        new Range(
                                new Position(
                                        params.getRange().getStart().getLine() + 1,
                                        params.getRange().getStart().getCharacter()),
                                new Position(
                                        params.getRange().getEnd().getLine(),
                                        params.getRange().getEnd().getCharacter())),
                        "        } else {\n"
                                + "        //TODO: Implement if the variable is null\n"
                                + "        }\n"
                                + "     }");
        editList.add(edit1);
        editList.add(edit2);
        codeAction.setEdit(new WorkspaceEdit(Collections.singletonMap(uri, editList)));
        codeAction.setDiagnostics(Collections.singletonList(diagnostic));
        List<Either<Command, CodeAction>> code = new ArrayList<>();
        code.add(Either.forRight(codeAction));
        return code;
    }
}
