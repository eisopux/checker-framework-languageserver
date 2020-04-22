# Checker Framework Language Server

This is a language server for the [Checker
Framework](https://github.com/typetools/checker-framework), which makes the
Checker Framework usable by all editors/IDEs that support the [Language Server
Protocol](https://microsoft.github.io/language-server-protocol/). It is
implemented with [LSP4J](https://github.com/eclipse/lsp4j)


## How to Build

```shell
./gradlew shadowJar
```

and then `checker-framework-languageserver-all.jar` will be generated under
`build/libs`.


## How to Use

The language server can be launched in the command line interface, although it's
not intended to be launched manually. Normally, you would want to install a
dedicated plugin that supports LSP for your editor, and then connect this
language server to that plugin via standard input/output. Input and output are
both in the [LSP
format](https://microsoft.github.io//language-server-protocol/specifications/specification-3-14/).

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

## Editor Support

### VS Code

Please see
[eisopux/checker-framework-vscode](https://github.com/eisopux/checker-framework-vscode).

### IntelliJ IDEA

This language server can be used directly with the help of
[gtache/intellij-lsp](https://github.com/gtache/intellij-lsp). Provide a raw
command to it to launch this language server and it's done. Please also refer to
[eisopux/checker-framework-idea](https://github.com/eisopux/checker-framework-idea).

### Eclipse

Please see
[eisopux/checker-framework-eclipse](https://github.com/eisopux/checker-framework-eclipse).


## Notes for Development

As LSP4J lacks documentation, it's suggested that the developer of this project
uses an IDE supporting auto-completion so the methods/interfaces/classes of
LSP4J can be easily inspected to figure out what is actually given/expected.

To format the source code, run `$ ./gradlew goJF`.

## Dependency on Checker Framework

This project has a dependency on the Checker Framework, but it's only
on `org.checkerframework.framework.util.CheckerMain`, which is used by
`CheckExecutor` to generate the arguments for launching the Checker Framework
properly. Therefore it's not necessary to update this dependency very often; it
only needs to be updated if in a future version the interface of `CheckerMain`
changes.

The
[eisopux/checker-framework-languageserver-downloader](https://github.com/eisopux/checker-framework-languageserver-downloader)
project allows plugins to automatically download a Checker Framework release.

## Use editor with a locally-built language server 
The editor will auto download the language server when the plugins get start. 
If you want to run with a locally-built language server instead:
Firstly, build the language server locally, then find the `checker-framework-languageserver-all.jar` in `build/libs`.
Next, drop the jar file to the directory where the plugin will look for the language server.
(This directory is set when the plugin first run. Please see the Readme for each specific plugins.)

## Acknowledgements

- [checkerframework-lsp](https://github.com/adamyy/checkerframework-lsp)
- [vscode-languageserver-java-example](https://github.com/adamvoss/vscode-languageserver-java-example)
