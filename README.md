# Checker Framework Language Server

This is the language server for [Checker Framework](https://github.com/typetools/checker-framework) implemented with [LSP4J](https://github.com/eclipse/lsp4j), which means it can be used for all editors/IDEs that support the [Language Server Protocol](https://microsoft.github.io/language-server-protocol/).

## How to Build

```shell
./gradlew shadowJar
```

and then `checker-framework-languageserver-all.jar` will be generated under `build/libs`.

## How to Use

The language server can be launched in the command line interface, although it's not intended to be launched manually. Normally, you would want to install a dedicated plugin that supports LSP for your editor, and then connect the this language server to that plugin via standard input/output. Input and output are both in [LSP format](https://microsoft.github.io//language-server-protocol/specifications/specification-3-14/).

Note that this project only supports running with Java 8.

```shell
java \
-cp /path/to/checker-framework-languageserver-all.jar:/path/to/checker-framework/checker/dist/checker.jar \
org.checkerframework.languageserver.ServerMain \
--frameworkPath /path/to/checker-framework \
--checkers org.checkerframework.checker.nullness.NullnessChecker \
--checkers some.other.checker \
--commandLineOptions command_line_opt_1 \
--commandLineOptions command_line_opt_2
```

## Editor Supports

### VS Code

Please see [eisopux/checker-framework-vscode](https://github.com/eisopux/checker-framework-vscode).

### IntelliJ IDEA

This language server can be used directly with the help of [gtache/intellij-lsp](https://github.com/gtache/intellij-lsp). Provide a raw command to it to launch this language server and it's done.

## Notes for Development

As LSP4J lacks documentation, it's suggested that the developer of this project uses an IDE supporting auto-completion so the methods/interfaces/classes of LSP4J can be easily inspected to figure out what are actually given/expected.

To format the source code, run `$ ./gradlew goJF`.

## Acknowledgement

- [checkerframework-lsp](https://github.com/adamyy/checkerframework-lsp)
- [vscode-languageserver-java-example](https://github.com/adamvoss/vscode-languageserver-java-example)
