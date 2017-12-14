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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import com.ibm.ctools.ls.xml.models.XMLAssociation;

public class XMLLanguageServer implements LanguageServer {

    private final XMLTextDocumentService xmlTextDocumentService;
    private final XMLWorkspaceService xmlWorkspaceService;
    private CatalogResolver catalogResolver;
    private List<XMLAssociation> xmlAssociations;
    private LanguageClient languageClient;

    public XMLLanguageServer() {
        xmlTextDocumentService = new XMLTextDocumentService(this);
        xmlWorkspaceService = new XMLWorkspaceService(this);
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        InitializeResult result = new InitializeResult(capabilities);
        return CompletableFuture.completedFuture(result);
    }

    public void setClient(LanguageClient client) {
        this.languageClient = client;
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return null;
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return xmlTextDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return xmlWorkspaceService;
    }

    public LanguageClient getLanguageClient() {
        return this.languageClient;
    }

    public CatalogResolver getCatalogResolver() {
        return catalogResolver;
    }

    public void setupXMLCatalog(String xmlCatalogFiles) {
        if (xmlCatalogFilesValid(xmlCatalogFiles)) {
            CatalogManager catalogManager = new CatalogManager();
            catalogManager.setUseStaticCatalog(false);
            catalogManager.setIgnoreMissingProperties(true);
            catalogManager.setCatalogFiles(xmlCatalogFiles);
            catalogResolver = new CatalogResolver(catalogManager);
        } else {
            languageClient.showMessage(new MessageParams(MessageType.Error, XMLMessages.CATALOG_LOAD_ERROR));
            catalogResolver = null;
        }
    }

    public void clearXMLCatalog() {
        catalogResolver = null;
    }

    public void setupXMLAssociations(List<XMLAssociation> xmlAssociations) {
        this.xmlAssociations = xmlAssociations;
        if (!xmlAssociationsValid(xmlAssociations)) {
            languageClient.showMessage(new MessageParams(MessageType.Warning, XMLMessages.ASSOCIATIONS_ERROR));
        }
    }

    public void clearXMLAssociations() {
        this.xmlAssociations = null;
    }

    public String getAssociatedSchema(String xmlFileUri) {
        xmlFileUri = xmlFileUri.replace('\\', '/');
        if (xmlAssociations != null) {
            Iterator<XMLAssociation> iterator = xmlAssociations.iterator();
            while (iterator.hasNext()) {
                XMLAssociation xmlAssociation = iterator.next();
                String fileNamePattern = xmlAssociation.getFileNamePattern();

                if (fileNamePattern != null) {

                    fileNamePattern = fileNamePattern.replace('\\', '/');

                    if (fileNamePattern.lastIndexOf('/') == -1) {
                        xmlFileUri = xmlFileUri.substring(xmlFileUri.lastIndexOf('/') + 1);
                    }

                    try {
                        String updatedFileNamePattern = fileNamePattern.replaceAll("\\*", ".*").replaceAll("\\?", ".");
                        Pattern pattern = Pattern.compile(updatedFileNamePattern);
                        Matcher matcher = pattern.matcher(xmlFileUri);
                        if (matcher.matches()) {
                            return xmlAssociation.getSystemId();
                        }
                    } catch (Exception exception) {
                    }
                }
            }
        }
        return null;
    }

    private boolean xmlCatalogFilesValid(String xmlCatalogFiles) {
        String[] paths = xmlCatalogFiles.split(";");
        for (int i = 0; i < paths.length; i++) {
            String currentPath = paths[i];
            File file = new File(currentPath);
            if (!file.exists()) {
                return false;
            }
        }
        return true;
    }

    private boolean xmlAssociationsValid(List<XMLAssociation> xmlAssociations) {
        Iterator<XMLAssociation> iterator = xmlAssociations.iterator();
        while (iterator.hasNext()) {
            XMLAssociation xmlAssociation = iterator.next();
            String fileNamePattern = xmlAssociation.getFileNamePattern();
            String systemId = xmlAssociation.getSystemId();
            if (fileNamePattern == null || fileNamePattern.length() == 0 || systemId == null || systemId.length() == 0) {
                return false;
            }
            if (catalogResolver != null) {
                String entity = catalogResolver.getResolvedEntity(null, systemId);
                if (entity != null) {
                    try {
                        URL url = new URL(entity);
                        systemId = url.getFile();
                    } catch (MalformedURLException malformedURLException) {
                    }
                }
            }
            File file = new File(systemId);
            if (!file.exists()) {
                return false;
            }
        }
        return true;
    }

}
