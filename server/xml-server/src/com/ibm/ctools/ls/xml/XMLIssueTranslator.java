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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public class XMLIssueTranslator {

    public static Diagnostic translateXMLIssue(XMLIssue xmlIssue, String xmlDocumentContent) {
        Position start = new Position(xmlIssue.getLine() - 1, xmlIssue.getColumn() - 1);
        Position end = new Position(xmlIssue.getLine() - 1, xmlIssue.getColumn() - 1);

        String xmlDocumentContentSection = xmlDocumentContent.substring(0, getOffsetFromPosition(start, xmlDocumentContent));

        adjustRangeForStructuralIssues(start, end, xmlIssue.getDescription(), xmlDocumentContentSection);
        adjustRangeForSchemaIssues(start, end, xmlIssue.getDescription(), xmlDocumentContentSection);

        return new Diagnostic(new Range(start, end), adjustDescription(xmlIssue.getDescription()), getSeverity(xmlIssue), XMLMessages.LANGUAGE_SERVER_LABEL);
    }

    private static void adjustRangeForStructuralIssues(Position start, Position end, String description, String xmlDocumentContentSection) {
        if ((description.startsWith("Attribute name ") && description.endsWith(" must be followed by the ' = ' character."))
            || description.startsWith("Open quote is expected for attribute ")
            || (description.startsWith("The value of attribute ") && description.endsWith(" must not contain the '<' character."))) {

            String attributeName = extractQuotedWord(description, 0, true);
            int index = xmlDocumentContentSection.lastIndexOf(attributeName);
            Position position = getPositionFromOffset(index, xmlDocumentContentSection);
            start.setLine(position.getLine());
            start.setCharacter(position.getCharacter());
            end.setLine(position.getLine());
            end.setCharacter(position.getCharacter() + attributeName.length());

        } else if (description.startsWith("Element type ") && description.endsWith(" must be followed by either attribute specifications, \">\" or \"/>\".")) {

            String elementName = extractQuotedWord(description, 0, true);
            Position elementOpeningTagPosition = getElementOpeningTagPosition(elementName, xmlDocumentContentSection);
            start.setLine(elementOpeningTagPosition.getLine());
            start.setCharacter(elementOpeningTagPosition.getCharacter());
            end.setLine(elementOpeningTagPosition.getLine());
            end.setCharacter(elementOpeningTagPosition.getCharacter() + elementName.length());

        }
    }

    private static void adjustRangeForSchemaIssues(Position start, Position end, String description, String xmlDocumentContentSection) {
        if (description.startsWith("cvc-complex-type.3.2.2:")) {

            String attributeName = extractQuotedWord(description, 0, false);
            Position attributeNamePosition = getAttributeNamePosition(attributeName, xmlDocumentContentSection);
            start.setLine(attributeNamePosition.getLine());
            end.setLine(attributeNamePosition.getLine());
            start.setCharacter(attributeNamePosition.getCharacter());
            end.setCharacter(attributeNamePosition.getCharacter() + attributeName.length());

        } else if (description.startsWith("cvc-complex-type.4:")) {

            String elementName = extractQuotedWord(description, 1, false);
            Position elementOpeningTagPosition = getElementOpeningTagPosition(elementName, xmlDocumentContentSection);
            start.setLine(elementOpeningTagPosition.getLine());
            start.setCharacter(elementOpeningTagPosition.getCharacter());
            end.setLine(elementOpeningTagPosition.getLine());
            end.setCharacter(elementOpeningTagPosition.getCharacter() + elementName.length());

        } else if (description.startsWith("cvc-attribute.3:") || description.startsWith("cvc-complex-type.3.1:")) {

            String attributeValue = extractQuotedWord(description, 0, false);
            String attributeName = extractQuotedWord(description, 1, false);
            Position attributeValuePosition = getAttributeValuePosition(attributeName, attributeValue, xmlDocumentContentSection);
            start.setLine(attributeValuePosition.getLine());
            end.setLine(attributeValuePosition.getLine());
            start.setCharacter(attributeValuePosition.getCharacter());
            end.setCharacter(attributeValuePosition.getCharacter() + attributeValue.length());

        } else if (description.startsWith("cvc-type.3.1.3:")) {

            String value = extractQuotedWord(description, 0, false);
            Position valuePosition = getElementValuePosition(value, xmlDocumentContentSection);
            start.setLine(valuePosition.getLine());
            end.setLine(valuePosition.getLine());
            start.setCharacter(valuePosition.getCharacter());
            end.setCharacter(valuePosition.getCharacter() + value.length());

        } else if (description.startsWith("cvc-complex-type.2.4.a:") || description.startsWith("cvc-complex-type.2.4.b:")
                   || description.startsWith("cvc-complex-type.2.3:") || description.startsWith("cvc-elt.1:")
                   || description.startsWith("cvc-complex-type.2.1:") || description.startsWith("cvc-complex-type.2.4.d:")) {

            String elementName = extractQuotedWord(description, 0, false);
            Position elementOpeningTagPosition = getElementOpeningTagPosition(elementName, xmlDocumentContentSection);
            start.setLine(elementOpeningTagPosition.getLine());
            start.setCharacter(elementOpeningTagPosition.getCharacter());
            end.setLine(elementOpeningTagPosition.getLine());
            end.setCharacter(elementOpeningTagPosition.getCharacter() + elementName.length());

        }
    }

    private static String extractQuotedWord(String text, int index, boolean doubleQuote) {
        char quote = doubleQuote ? '"' : '\'';
        int currentIndex = text.indexOf(quote);
        for (int i = 0; i < index * 2; i++) {
            currentIndex = text.indexOf(quote, currentIndex + 1);
        }
        return text.substring(currentIndex + 1, text.indexOf(quote, currentIndex + 1));
    }

    private static Position getAttributeNamePosition(String attributeName, String xmlDocumentContentSection) {
        Pattern pattern = Pattern.compile(attributeName + "\\s*=");
        Matcher matcher = pattern.matcher(xmlDocumentContentSection);
        int index = -1;
        while (matcher.find()) {
            index = matcher.start();
        }
        return getPositionFromOffset(index, xmlDocumentContentSection);
    }

    private static Position getAttributeValuePosition(String attributeName, String attributeValue, String xmlDocumentContentSection) {
        Pattern pattern = Pattern.compile(attributeName + "\\s*=\\s*\"" + attributeValue + "\"");
        Matcher matcher = pattern.matcher(xmlDocumentContentSection);
        int index = -1;
        while (matcher.find()) {
            index = matcher.start();
        }
        return getPositionFromOffset(xmlDocumentContentSection.indexOf('"', index) + 1, xmlDocumentContentSection);
    }

    private static Position getElementOpeningTagPosition(String elementName, String xmlDocumentContentSection) {
        Pattern openTagPattern = Pattern.compile("<" + elementName);
        Matcher matcher = openTagPattern.matcher(xmlDocumentContentSection);
        int index = -1;
        while (matcher.find()) {
            index = matcher.start();
        }
        return getPositionFromOffset(index + 1, xmlDocumentContentSection);
    }

    private static Position getElementValuePosition(String value, String xmlDocumentContentSection) {
        String trimmedValue = value.trim();
        Pattern pattern = Pattern.compile(">\\s*" + trimmedValue + "\\s*<");
        Matcher matcher = pattern.matcher(xmlDocumentContentSection);
        int index = -1;
        while (matcher.find()) {
            index = matcher.start();
        }
        return getPositionFromOffset(index + 1, xmlDocumentContentSection);
    }

    private static int getLineOffset(int lineNumber, String xmlDocumentContent) {
        int offset = 0;
        for (int i = 0; i < lineNumber; i++) {
            offset = xmlDocumentContent.indexOf("\n", offset) + 1;
        }
        return offset;
    }

    private static int getLineNumber(int offset, String xmlDocumentContent) {
        int currentOffset = -1;
        int line = 0;
        while (currentOffset < offset) {
            currentOffset = xmlDocumentContent.indexOf("\n", currentOffset + 1);
            line++;
            if (currentOffset == -1) {
                break;
            }
        }
        return line - 1;
    }

    private static Position getPositionFromOffset(int offset, String xmlDocumentContent) {
        int line = getLineNumber(offset, xmlDocumentContent);
        int lineOffset = getLineOffset(line, xmlDocumentContent);
        return new Position(line, offset - lineOffset);
    }

    private static int getOffsetFromPosition(Position position, String xmlDocumentContent) {
        return getLineOffset(position.getLine(), xmlDocumentContent) + position.getCharacter();
    }

    private static String adjustDescription(String description) {
        if (description.startsWith("cvc-")) {
            int index = description.indexOf(':');
            if (index != -1) {
                return description.substring(index + 2, description.length() - 1) + " (" + description.substring(0, index) + ").";
            }
        }
        return description;
    }

    private static DiagnosticSeverity getSeverity(XMLIssue xmlIssue) {
        switch (xmlIssue.getSeverity()) {
            case FATAL:
            case ERROR:
                return DiagnosticSeverity.Error;
            case WARNING:
                return DiagnosticSeverity.Warning;
            default:
                return DiagnosticSeverity.Information;
        }
    }

}
