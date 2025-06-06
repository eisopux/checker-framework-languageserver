package org.checkerframework.languageserver.quickfix;

import org.eclipse.lsp4j.Diagnostic;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Registry for quick fix providers. */
public class QuickFixRegistry {
    /** Map from diagnostic code to quick fix provider. */
    private static final Map<String, QuickFixProvider> providers = new HashMap<>();

    /**
     * Register a quick fix provider for a diagnostic code.
     *
     * @param diagnosticCode the diagnostic code
     * @param provider the quick fix provider
     */
    public void registerProvider(String diagnosticCode, QuickFixProvider provider) {
        providers.put(diagnosticCode, provider);
    }

    /**
     * Get the quick fix provider for a diagnostic.
     *
     * @param diagnostic the diagnostic
     * @return the quick fix provider
     */
    public QuickFixProvider getProvider(Diagnostic diagnostic) {
        return providers.get(extractMessage(diagnostic.getMessage()));
    }

    /**
     * Extract the message from a diagnostic. Example: "[dereference.of.nullable] ..." ->
     * "dereference.of.nullable"
     *
     * @param input the diagnostic message
     * @return the extracted message
     */
    private static String extractMessage(String input) {
        Pattern pattern = Pattern.compile("\\[([^]]+)\\]");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1); // Returns the first capturing group
        }

        return "No match found"; // Or any appropriate default/fallback value
    }
}
