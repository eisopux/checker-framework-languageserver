package org.checkerframework.languageserver;

import static org.junit.Assert.*;

import org.junit.Test;

public class ServerMainTest {
    @Test
    public void testServerMainCanBeInstantiated() {
        ServerMain classUnderTest = new ServerMain();
        assertNotNull("ServerMain should be able to be instantiated", classUnderTest);
    }
}
