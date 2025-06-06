package org.checkerframework.languageserver.quickfix;

import com.google.common.base.Splitter;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/** Interface for quick fix providers. */
public interface QuickFixProvider {
    /**
     * Check if the provider can handle a diagnostic.
     *
     * @param diagnostic the diagnostic
     * @return true if the provider can handle the diagnostic
     */
    boolean canHandle(Diagnostic diagnostic);

    /**
     * Get the quick fixes for a diagnostic.
     *
     * @param diagnostic the diagnostic
     * @param params the code action parameters
     * @return the quick fixes
     */
    List<Either<Command, CodeAction>> getQuickFixes(Diagnostic diagnostic, CodeActionParams params);

    /**
     * Get the content in a range.
     *
     * @param uri the URI of the file
     * @param range the range
     * @return the content in the range
     */
    static String getContentInRange(String uri, Range range) {
        try {
            URI uriObject = new URI(uri);
            String filePath = Paths.get(uriObject).toFile().getAbsolutePath();
            String fileContent =
                    new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            int startLine = range.getStart().getLine();
            int startCharacter = range.getStart().getCharacter();
            int endLine = range.getEnd().getLine();
            int endCharacter = range.getEnd().getCharacter();

            List<String> lines =
                    Splitter.onPattern(System.lineSeparator()).splitToList(fileContent);

            StringBuilder textInRange = new StringBuilder();
            for (int i = startLine; i <= endLine; i++) {
                String line = lines.get(i);
                if (i == startLine) {
                    if (i == endLine) {
                        textInRange.append(line.substring(startCharacter, endCharacter));
                    } else {
                        textInRange.append(line.substring(startCharacter));
                        textInRange.append(System.lineSeparator());
                    }
                } else if (i == endLine) {
                    textInRange.append(line.substring(0, endCharacter));
                } else {
                    textInRange.append(line);
                    textInRange.append(System.lineSeparator());
                }
            }

            return textInRange.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
