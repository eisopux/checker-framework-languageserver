# Checker Framework Language Server

This is the language server for [Checker Framework](https://github.com/typetools/checker-framework) implemented with [LSP4J](https://github.com/eclipse/lsp4j), which means it can be used for all editors/IDEs that support the [Language Server Protocol](https://microsoft.github.io/language-server-protocol/).

## How to Build

```shell
./gradlew shadowJar
```

and then `checker-framework-languageserver-all.jar` will be generated under `build/libs`.

## How to Use

TODO

## Settings

TODO

## Notes for Development

As LSP4J lacks documentation, it's suggested that the developer of this project use an IDE supporting auto-completion so the methods/interfaces/classes of LSP4J can be easily inspected to figure out what are actually given/expected.

## Acknowledgement

- [checkerframework-lsp](https://github.com/adamyy/checkerframework-lsp)
- [vscode-languageserver-java-example](https://github.com/adamvoss/vscode-languageserver-java-example)
