package org.checkerframework.languageserver;

import java.util.Map;

/** This class is for storing the type information from Checker Framework. */
public class CheckerTypeKind {
    /** The string stores checker name e.g. Nullness, KeyFor. */
    private final String checkerName;

    /**
     * The map stores type information in the key of the map and type kind information e.g.
     * used/declared in the value of the map.
     */
    private final Map<String, String> typeToKindMap;

    /**
     * Constructor for creating the CheckerTypeKind class
     *
     * @param checkerName checker's name
     * @param typeToKindMap type and kind pair
     */
    CheckerTypeKind(String checkerName, Map<String, String> typeToKindMap) {
        this.checkerName = checkerName;
        this.typeToKindMap = typeToKindMap;
    }

    /**
     * Return the name of the checker
     *
     * @return name of the checker
     */
    /*package-private*/ String getCheckername() {
        return this.checkerName;
    }

    /**
     * Return the type and kind map
     *
     * @return type and kind map
     */
    Map<String, String> getTypeToKindMap() {
        return this.typeToKindMap;
    }
}
