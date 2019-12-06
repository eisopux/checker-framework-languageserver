package org.checkerframework.languageserver;

import java.util.List;
import java.util.Map;
import javax.tools.Diagnostic;

interface Publisher {
    void publish(Map<String, List<Diagnostic>> result);
}
