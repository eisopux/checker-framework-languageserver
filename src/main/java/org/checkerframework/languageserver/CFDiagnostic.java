package org.checkerframework.languageserver;

import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * JSON wrapper to communicate javac diagnostics from {@link JavacWrapper} to {@link CheckExecutor}.
 */
public class CFDiagnostic implements Diagnostic<Object> {
    /** The file URI. */
    private final String fileUri;

    /** The diagnostics kind. It is one of value in {@link Kind} enum. */
    private final String kind;

    /**
     * The diagnostics position. The position, startPosition, and endPosition satisfies the
     * conditions getStartPosition() <= getPosition() <= getEndPosition().
     */
    private final long position;

    /** The diagnostics start position. */
    private final long startPosition;

    /** The diagnostics end position. */
    private final long endPosition;

    /** The line number. */
    private final long lineNumber;

    /** The column number. */
    private final long columnNumber;

    /** The code indicating the type of diagnostic. */
    private final String code;

    /** The message for the given locale. */
    private final String message;

    /** Default constructor for CFDiagnostic. */
    CFDiagnostic(Diagnostic<? extends JavaFileObject> diagnostic) {
        fileUri = diagnostic.getSource().toUri().toString();
        kind = diagnostic.getKind().name();
        position = diagnostic.getPosition();
        startPosition = diagnostic.getStartPosition();
        endPosition = diagnostic.getEndPosition();
        lineNumber = diagnostic.getLineNumber();
        columnNumber = diagnostic.getColumnNumber();
        code = diagnostic.getCode();
        message = diagnostic.getMessage(null);
    }

    /**
     * Gets the kind of this diagnostic, for example, error or warning.
     *
     * @return the kind of this diagnostic
     */
    @Override
    public Kind getKind() {
        return Kind.valueOf(kind);
    }

    /**
     * Gets the source object associated with this diagnostic.
     *
     * @return the source object associated with this diagnostic. {@code null} if no source object
     *     is associated with the diagnostic.
     */
    @Override
    public Object getSource() {
        return fileUri;
    }

    /**
     * Gets a character offset from the beginning of the source object associated with this
     * diagnostic that indicates the location of the problem. In addition, the following must be
     * true:
     *
     * <p>{@code getStartPostion() <= getPosition()}
     *
     * <p>{@code getPosition() <= getEndPosition()}
     *
     * @return character offset from beginning of source; {@link #NOPOS} if {@link #getSource()}
     *     would return {@code null} or if no location is suitable
     */
    @Override
    public long getPosition() {
        return position;
    }

    /**
     * Gets the character offset from the beginning of the file associated with this diagnostic that
     * indicates the start of the problem.
     *
     * @return offset from beginning of file; {@link #NOPOS} if and only if {@link #getPosition()}
     *     returns {@link #NOPOS}
     */
    @Override
    public long getStartPosition() {
        return startPosition;
    }

    /**
     * Gets the character offset from the beginning of the file associated with this diagnostic that
     * indicates the end of the problem.
     *
     * @return offset from beginning of file; {@link #NOPOS} if and only if {@link #getPosition()}
     *     returns {@link #NOPOS}
     */
    @Override
    public long getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the line number of the character offset returned by {@linkplain #getPosition()}.
     *
     * @return a line number or {@link #NOPOS} if and only if {@link #getPosition()} returns {@link
     *     #NOPOS}
     */
    @Override
    public long getLineNumber() {
        return lineNumber;
    }

    /**
     * Gets the column number of the character offset returned by {@linkplain #getPosition()}.
     *
     * @return a column number or {@link #NOPOS} if and only if {@link #getPosition()} returns
     *     {@link #NOPOS}
     */
    @Override
    public long getColumnNumber() {
        return columnNumber;
    }

    /**
     * Gets a diagnostic code indicating the type of diagnostic. The code is
     * implementation-dependent and might be {@code null}.
     *
     * @return a diagnostic code
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * Gets a localized message for the given locale. The actual message is
     * implementation-dependent. If the locale is {@code null} use the default locale.
     *
     * @param locale a locale; might be {@code null}
     * @return a localized message
     */
    @Override
    public String getMessage(Locale locale) {
        return message;
    }
}
