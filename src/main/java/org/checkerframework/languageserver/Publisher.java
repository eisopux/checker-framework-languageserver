package org.checkerframework.languageserver;

import javax.tools.Diagnostic;
import java.util.List;
import java.util.Map;

interface Publisher {
    void publish(Map<String, List<Diagnostic>> result);
}
