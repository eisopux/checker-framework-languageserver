package org.checkerframework.lsp;

public class ServerMain {
    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) {
        System.out.println(new ServerMain().getGreeting());
    }
}
