package org.checkerframework.languageserver;

class ComparablePosition implements Comparable<ComparablePosition> {
    private final int line;
    private final int col;

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
