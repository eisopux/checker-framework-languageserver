package org.checkerframework.languageserver;

/**
 * Represents the position of a character in a file. This class is comparable, so it can be used as
 * the key of a map for indexing and querying positions in files.
 */
class ComparablePosition implements Comparable<ComparablePosition> {
    /** The line number. */
    private final int line;

    /** The column number. */
    private final int col;

    /** Default constructor for comparable position. */
    public ComparablePosition(int line, int col) {
        this.line = line;
        this.col = col;
    }

    @Override
    public int compareTo(ComparablePosition o) {
        // compare the two positions
        if (this.line != o.line) {
            return this.line - o.line;
        }

        return this.col - o.col;
    }
}
