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
import java.util.Arrays;
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
        int startLine = range.getStart().getLine();
        int startCharacter = range.getStart().getCharacter();
        int endLine = range.getEnd().getLine();
        int endCharacter = range.getEnd().getCharacter();
        TextEdit editBefore =
                new TextEdit(
                        new Range(
                                new Position(startLine, startCharacter),
                                new Position(endLine, endCharacter)),
                        " if (" + str + " != null) {\n" + "            " + str + ".toString();\n");
        TextEdit editAfter =
                new TextEdit(
                        new Range(
                                new Position(startLine + 1, startCharacter),
                                new Position(endLine, endCharacter)),
                        "        } else {\n"
                                + "        //TODO: Implement if the variable is null\n"
                                + "        }\n"
                                + "     }");
        codeAction.setEdit(createWorkspaceEdit(uri, editBefore, editAfter));
        codeAction.setDiagnostics(Collections.singletonList(diagnostic));
        List<Either<Command, CodeAction>> code = new ArrayList<>();
        code.add(Either.forRight(codeAction));
        code.add(Either.forRight(addSuppressWarning(diagnostic, params)));
        return code;
    }

    /**
     * Add a suppress warning quick fix.
     *
     * @param diagnostic the diagnostic
     * @param params the code action parameters
     * @return the code action
     */
    public CodeAction addSuppressWarning(Diagnostic diagnostic, CodeActionParams params) {
        CodeAction codeAction = new CodeAction("Suppress nullness warning");
        codeAction.setKind(CodeActionKind.QuickFix);
        String uri = params.getTextDocument().getUri();
        Range range = params.getRange();
        String str = QuickFixProvider.getContentInRange(uri, range);
        TextEdit suppressWarningEdit = createSuppressWarningEdit(str, params.getRange());
        codeAction.setEdit(createWorkspaceEdit(uri, suppressWarningEdit));
        codeAction.setDiagnostics(Collections.singletonList(diagnostic));
        return codeAction;
    }

    /**
     * Create a SuppressWarningEdit.
     *
     * @param str the string to insert
     * @param range the range
     * @return the text edit
     */
    private TextEdit createSuppressWarningEdit(String str, Range range) {
        return new TextEdit(
                new Range(
                        new Position(range.getStart().getLine(), range.getStart().getCharacter()),
                        new Position(range.getEnd().getLine(), range.getEnd().getCharacter())),
                "@SuppressWarnings(\"nullness\")\n" + "       " + str);
    }

    /**
     * Create a workspace edit.
     *
     * @param uri the URI of the file
     * @param edits the text edits
     * @return the workspace edit
     */
    private WorkspaceEdit createWorkspaceEdit(String uri, TextEdit... edits) {
        return new WorkspaceEdit(Collections.singletonMap(uri, Arrays.asList(edits)));
    }
}
