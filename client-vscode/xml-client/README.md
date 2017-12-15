# XML Language Support for Visual Studio Code

Features
--------
 - Structural XML validation
 - XML Schema Definition (XSD) validation
 - OASIS XML Catalog support
 - File name based schema association

Getting Started
---------------
 - Java 8 must be installed.
 - The JAVA_HOME environment variable must be set. Alternatively xmlLang.javaHome in .vscode/settings.json must be set.
 - Install the extension
 - Download and open the sample project below

# Sample Project
The theme for the sample project consists of an invoice. For each scenario a schema and a sample XML file with errors is provided. The goal is to use the XML schema validation feature to implement the corrections necessary to fix the document. For each scenario a second XML file with all fixes in place is provided to verify results. The sample project can be downloaded here:

[examples.zip](https://raw.githubusercontent.com/microclimate-devops/xml-language-server/master/client-vscode/xml-client/readme-resources/examples.zip)

Extract the archive into a directory and open the directory with Visual Studio Code. Each example in the project is contained within a subdirectory. The steps below described each scenario in detail.

Direct Reference
----------------
This example shows the simplest scenario where an XML file directly references a schema file.
 - Open nonamespace/invoice_with_errors.xml
 - Notice the "invoice.xsd" value in the "xsi:noNamespaceSchemaLocation" attribute. This reference is used to validate the document against the invoice.xsd schema.
 - Hover over the squiggles to see the schema errors
 - Use this information to fix the issues in the document
 - The final result should be similar to that of nonamespace/invoice_fixed.xml

 This example can also be explored with namespaces in namespace/invoice_with_errors.xml and with prefixes in namepsace/invoice_prefix_with_errors.xml. The XML files with all fixes in place are namespace/invoice_fixed.xml and namespace_invoice_prefix_fixed.xml respectively.

XML Catalog
-----------
This example shows how schemas can be resolved using the XML Catalog.
 - Open .vscode/settings.json
 - Notice the "catalog/catalog.xml" value in "xmlLang.xmlCatalogFiles". This tells the extension to load the XML Catalog. If multiple catalogs are required, they should be separated by semicolons.
 - Open catalog/catalog.xml
 - Notice systemId "http://invoice.xsd" resolves to "invoice.xsd"
 - Open catalog/invoice_with_errors.xml
 - Notice the "http://invoice.xsd" value in "xsi:noNamespaceSchemaLocation". This reference is resolved to the invoice.xsd schema file through the XML catalog.
 - The same schema validation squiggles shown in the example above should appear
 - Use this information to fix the issues in the document
 - The final result should be similar to that of catalog/invoice_fixed.xml

File Association
----------------
This example shows how to create file name based associations between XML and XSD documents. This is particularly useful when working with XML files that do not contain references to schema or catalog entries.
 - Open .vscode/settings.json
 - Notice the "\*/file_association/invoice_\*.xml" pattern value associated to the systemId "file_association/invoice.xsd". This will cause any file with its name starting with "invoice_" in the file_association directory to be validated against file_association/invoice.xsd schema.
 - Open file_association/invoice.xml
 - Notice there is no schema reference
 - The same schema validation squiggles shown in the example above should appear
 - Use this information to fix the issues in the document
 - The final result should be similar to that of file_association/invoice_fixed.xml

(file associations can optionally be combined with XML Catalogs via systemIds)

# Validation Examples

Schema Validation - Invalid data type
-------------------------------------
![Fixing a data type error](https://raw.githubusercontent.com/microclimate-devops/xml-language-server/master/client-vscode/xml-client/readme-resources/error-datatype.gif)

Schema Validation - Invalid enumeration
---------------------------------------
![Fixing an enumeration error](https://raw.githubusercontent.com/microclimate-devops/xml-language-server/master/client-vscode/xml-client/readme-resources/error-enumeration.gif)

Schema Validation - Missing content
-----------------------------------
![Fixing missing content](https://raw.githubusercontent.com/microclimate-devops/xml-language-server/master/client-vscode/xml-client/readme-resources/error-missing-content.gif)

Schema Validation - Invalid content
-----------------------------------
![Fixing invalid content](https://raw.githubusercontent.com/microclimate-devops/xml-language-server/master/client-vscode/xml-client/readme-resources/error-invalid-content.gif)

Structural Error - Invalid attribute
------------------------------------
![Fixing an invalid attribute](https://raw.githubusercontent.com/microclimate-devops/xml-language-server/master/client-vscode/xml-client/readme-resources/error-structure-attribute.gif)

Structural Error - Invalid element
----------------------------------
![Fixing an invalid element](https://raw.githubusercontent.com/microclimate-devops/xml-language-server/master/client-vscode/xml-client/readme-resources/error-structure-element.gif)
