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

package com.ibm.ctools.ls.xml.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.ibm.ctools.ls.xml.XMLLanguageServer;
import com.ibm.ctools.ls.xml.models.XMLAssociation;

public class XMLTestUtils {

    public static void performValidation(String xmlFilePath, String diagnosticsFilePath, String xmlCatalogFiles, List<XMLAssociation> xmlAssociations) {

        XMLLanguageServer xmlLanguageServer = new XMLLanguageServer();
        XMLTestLanguageClient xmlTestLanguageClient = new XMLTestLanguageClient();
        xmlLanguageServer.setClient(xmlTestLanguageClient);
        TextDocumentService textDocumentService = xmlLanguageServer.getTextDocumentService();

        if (xmlCatalogFiles != null) {
            xmlLanguageServer.setupXMLCatalog(xmlCatalogFiles);
        }

        if (xmlAssociations != null) {
            xmlLanguageServer.setupXMLAssociations(xmlAssociations);
        }

        String filePath = XMLTestUtils.getTestResourceAbsolutePath(xmlFilePath);
        String languageId = "xml";
        String text = XMLTestUtils.readFilecontents(filePath);

        TextDocumentItem textDocumentItem = new TextDocumentItem(filePath, languageId, 1, text);
        DidOpenTextDocumentParams didOpenTextDocumentParams = new DidOpenTextDocumentParams(textDocumentItem);

        textDocumentService.didOpen(didOpenTextDocumentParams);

        List<PublishDiagnosticsParams> diagnosticParamList = xmlTestLanguageClient.getDiagnosticParamList();
        List<Diagnostic> diagnostics = diagnosticParamList.get(0).getDiagnostics();

        String diagnosticsFileAbsolutePath = XMLTestUtils.getTestResourceAbsolutePath(diagnosticsFilePath);
        String expectedDiagnostics = XMLTestUtils.readFilecontents(diagnosticsFileAbsolutePath);

        assertEquals(expectedDiagnostics.trim(), diagnostics.toString().trim());

    }

    public static String readFilecontents(String filePath) {

        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    public static String getTestResourceAbsolutePath(String relativePath) {
        return new File(relativePath).getAbsolutePath();
    }

}
