# XML Language Server

The XML Language Server is an implementation of the [Language Server Protocol](https://github.com/Microsoft/language-server-protocol). It currently supports complex schema-based XML validation. The server is written in Java and is based on [Eclipse LSP4J](https://github.com/eclipse/lsp4j), the Java binding for the Language Server Protocol.

## Features

* Structural XML validation
* XML Schema Definition (XSD) validation
* OASIS XML Catalog support
* File name based schema association

## Building

```
git clone https://github.com/microclimate-dev2ops/xml-language-server.git
cd xml-language-server/server/xml-server
./mvnw clean package
```

## Contributing

See the [CONTRIBUTING](CONTRIBUTING.md) document for details on submitting pull requests.
