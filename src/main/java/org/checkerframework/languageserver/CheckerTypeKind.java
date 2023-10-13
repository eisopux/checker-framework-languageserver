package org.checkerframework.languageserver;

import java.util.Map;

/** This class is for processing type message. */
public class CheckerTypeKind {
    private String checkername;

    private Map<String, String> TypeKind;

    CheckerTypeKind(String checkername, Map<String, String> kindType) {
        this.checkername = checkername;
        this.TypeKind = kindType;
    }

    String getCheckername() {
        return this.checkername;
    }

    Map<String, String> getTypeKind() {
        return this.TypeKind;
    }
}
