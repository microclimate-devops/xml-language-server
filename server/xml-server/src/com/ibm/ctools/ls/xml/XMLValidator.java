/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ctools.ls.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;

import org.apache.xml.resolver.tools.CatalogResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class XMLValidator {

    public static List<XMLIssue> validateXML(String xmlFileUri, String xmlFileContent, String xmlSchemaFile, CatalogResolver catalogResolver) {

        List<XMLIssue> xmlIssues = new ArrayList<XMLIssue>();

        // XML parser
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);

        if (xmlSchemaFile != null) {
            try {
                factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new File(xmlSchemaFile)));
            } catch (SAXException saxException) {
                xmlIssues.add(new XMLIssue(XMLIssueSeverity.ERROR, 1, 1, saxException.getMessage()));
                //factory.setValidating(true);
            }
        } else {
            factory.setValidating(xmlFileContent.contains("schemaLocation") || xmlFileContent.contains("noNamespaceSchemaLocation"));
        }

        try {

            SAXParser parser = factory.newSAXParser();

            if (xmlSchemaFile == null) {
                parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
            }

            XMLReader reader = parser.getXMLReader();

            // XML catalog
            if (catalogResolver != null) {
                reader.setEntityResolver(catalogResolver);
            }

            // Error handler
            reader.setErrorHandler(new ErrorHandler() {

                @Override
                public void warning(SAXParseException parseException) throws SAXException {
                    XMLIssue xmlIssue = new XMLIssue(XMLIssueSeverity.WARNING, parseException.getLineNumber(), parseException.getColumnNumber(), parseException.getMessage());
                    xmlIssues.add(xmlIssue);
                }

                @Override
                public void error(SAXParseException parseException) throws SAXException {
                    XMLIssue xmlIssue = new XMLIssue(XMLIssueSeverity.ERROR, parseException.getLineNumber(), parseException.getColumnNumber(), parseException.getMessage());
                    xmlIssues.add(xmlIssue);
                }

                @Override
                public void fatalError(SAXParseException parseException) throws SAXException {
                    XMLIssue xmlIssue = new XMLIssue(XMLIssueSeverity.FATAL, parseException.getLineNumber(), parseException.getColumnNumber(), parseException.getMessage());
                    xmlIssues.add(xmlIssue);
                }

            });

            // Parse
            InputSource inputSource = new InputSource();
            inputSource.setByteStream(new ByteArrayInputStream(xmlFileContent.getBytes(StandardCharsets.UTF_8.name())));
            inputSource.setSystemId(xmlFileUri);
            reader.parse(inputSource);

        } catch (IOException | ParserConfigurationException | SAXException exception) {
        }

        return xmlIssues;
    }

}
