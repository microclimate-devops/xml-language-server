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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;

import com.google.gson.Gson;
import com.ibm.ctools.ls.xml.models.XMLAssociation;
import com.ibm.ctools.ls.xml.models.XMLClientConfiguration;
import com.ibm.ctools.ls.xml.models.XMLLang;

public class XMLWorkspaceService implements WorkspaceService {

    private final XMLLanguageServer xmlLanguageServer;

    public XMLWorkspaceService(XMLLanguageServer xmlLanguageServer) {
        this.xmlLanguageServer = xmlLanguageServer;
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
        return null;
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        Object obj = params.getSettings();

        // Use GSON to map from JSON to a java objects
        Gson gson = new Gson();
        gson.toJson(obj);
        XMLClientConfiguration configClass = gson.fromJson(gson.toJson(obj), XMLClientConfiguration.class);

        if (configClass != null) {
            XMLLang xmlLang = configClass.getXMLLang();

            if (xmlLang != null) {

                String xmlCatalogFiles = xmlLang.getXmlCatalogFiles();
                if (xmlCatalogFiles != null && !xmlCatalogFiles.trim().equals("")) {
                    xmlLanguageServer.setupXMLCatalog(xmlCatalogFiles);
                } else {
                    xmlLanguageServer.clearXMLCatalog();
                }

                List<XMLAssociation> xmlLAssociations = xmlLang.getXMLAssociations();
                if (xmlLAssociations != null && !xmlLAssociations.isEmpty()) {
                    xmlLanguageServer.setupXMLAssociations(xmlLAssociations);
                } else {
                    xmlLanguageServer.clearXMLAssociations();
                }
                ((XMLTextDocumentService) xmlLanguageServer.getTextDocumentService()).validateOpenDocuments();
            }
        }
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {}

}
