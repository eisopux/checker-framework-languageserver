package org.checkerframework.languageserver;

import org.eclipse.lsp4j.Position;

class ComparablePosition implements Comparable<ComparablePosition> {
    // use lsp4j's Position internally
    private final Position position;

    public ComparablePosition(Position position) {
        this.position = position;
    }

    @Override
    public int compareTo(ComparablePosition o) {
        // compare the two positions
        if (position.getLine() != o.position.getLine())
            return position.getLine() - o.position.getLine();

        return position.getCharacter() - o.position.getCharacter();
    }
}
