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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.apache.xml.resolver.tools.CatalogResolver;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

public class XMLTextDocumentService implements TextDocumentService {

    private final XMLLanguageServer xmlLanguageServer;
    private final HashMap<String, String> openXMLDocuments = new HashMap<String, String>();

    public XMLTextDocumentService(XMLLanguageServer xmlLanguageServer) {
        this.xmlLanguageServer = xmlLanguageServer;
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(TextDocumentPositionParams position) {
        return null;
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
        return null;
    }

    @Override
    public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
        return null;
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams position) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams position) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(DocumentSymbolParams params) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        return null;
    }

    @Override
    public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
        return null;
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
        return null;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        TextDocumentItem textDocument = params.getTextDocument();
        String uri = textDocument.getUri();
        List<Diagnostic> diagnostics = validateXMLDocument(uri, textDocument.getText());
        openXMLDocuments.put(uri, textDocument.getText());
        xmlLanguageServer.getLanguageClient().publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = params.getTextDocument();
        String uri = versionedTextDocumentIdentifier.getUri();
        Iterator<TextDocumentContentChangeEvent> textDocumentContentChangeEventIterator = params.getContentChanges().iterator();
        List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
        while (textDocumentContentChangeEventIterator.hasNext()) {
            TextDocumentContentChangeEvent textDocumentContentChangeEvent = textDocumentContentChangeEventIterator.next();
            String text = textDocumentContentChangeEvent.getText();
            openXMLDocuments.put(uri, text);
            List<Diagnostic> currentDiagnostics = validateXMLDocument(uri, text);
            diagnostics.addAll(currentDiagnostics);
        }
        xmlLanguageServer.getLanguageClient().publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        xmlLanguageServer.getLanguageClient().publishDiagnostics(new PublishDiagnosticsParams(uri, new ArrayList<Diagnostic>()));
        openXMLDocuments.remove(uri);
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {}

    private List<Diagnostic> validateXMLDocument(String xmlDocumentUri, String xmlDocumentContent) {
        List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();

        String associatedSchema = xmlLanguageServer.getAssociatedSchema(xmlDocumentUri);
        CatalogResolver catalogResolver = xmlLanguageServer.getCatalogResolver();

        if (catalogResolver != null) {
            String resolvedEntity = catalogResolver.getResolvedEntity(null, associatedSchema);
            if (resolvedEntity != null) {
                try {
                    URL url = new URL(resolvedEntity);
                    associatedSchema = url.getFile();
                } catch (MalformedURLException malformedURLException) {
                }
            }
        }

        List<XMLIssue> xmlIssues = XMLValidator.validateXML(xmlDocumentUri, xmlDocumentContent, associatedSchema, catalogResolver);
        Iterator<XMLIssue> xmlIssueIterator = xmlIssues.iterator();

        List<Diagnostic> misplacedDiagnostics = new ArrayList<Diagnostic>();
        List<String> missingSchemas = new ArrayList<String>();

        while (xmlIssueIterator.hasNext()) {
            XMLIssue xmlIssue = xmlIssueIterator.next();

            if (xmlIssue.getDescription().startsWith("schema_reference.4")) {
                if (missingSchemas.contains(xmlIssue.getDescription())) {
                    continue;
                } else {
                    missingSchemas.add(xmlIssue.getDescription());
                }
            }

            Diagnostic diagnostic = XMLIssueTranslator.translateXMLIssue(xmlIssue, xmlDocumentContent);
            diagnostics.add(diagnostic);

            int start = diagnostic.getRange().getStart().getCharacter();
            int end = diagnostic.getRange().getEnd().getCharacter();
            int column = xmlIssue.getColumn() - 1;
            if (start == end && start == column) {
                misplacedDiagnostics.add(diagnostic);
            } else {
                Iterator<Diagnostic> misplacedDiagnosticsIterator = misplacedDiagnostics.iterator();
                while (misplacedDiagnosticsIterator.hasNext()) {
                    Diagnostic misplacedDiagnostic = misplacedDiagnosticsIterator.next();
                    misplacedDiagnostic.setRange(diagnostic.getRange());
                }
                misplacedDiagnostics.clear();
            }

        }
        return diagnostics;
    }

    public void validateOpenDocuments() {
        Set<String> keySet = openXMLDocuments.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String uri = iterator.next();
            String xmlDocumentContent = openXMLDocuments.get(uri);
            List<Diagnostic> diagnostics = validateXMLDocument(uri, xmlDocumentContent);
            xmlLanguageServer.getLanguageClient().publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
        }
    }

}
