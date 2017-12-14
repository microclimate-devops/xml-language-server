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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ibm.ctools.ls.xml.models.XMLAssociation;

public class XMLLanguageServerTests {

    @Test
    public void nonamespaceSchemaValidationDetection() {
        XMLTestUtils.performValidation("test-resources/schema-validation-nonamespace/team.xml",
            "test-resources/schema-validation-nonamespace/diagnostics.txt", null, null);
    }

    @Test
    public void namespaceSchemaValidationDetection() {
        XMLTestUtils.performValidation("test-resources/schema-validation-namespace/team.xml",
            "test-resources/schema-validation-namespace/diagnostics.txt", null, null);
    }

    @Test
    public void namespaceSchemaPrefixValidationDetection() {
        XMLTestUtils.performValidation("test-resources/schema-validation-namespace-prefix/team.xml",
            "test-resources/schema-validation-namespace-prefix/diagnostics.txt", null, null);
    }

    @Test
    public void prologErrorDetection() {
        XMLTestUtils.performValidation("test-resources/error-prolog/team.xml",
            "test-resources/error-prolog/diagnostics.txt", null, null);
    }

    @Test
    public void trailingErrorDetection() {
        XMLTestUtils.performValidation("test-resources/error-trailing/team.xml",
            "test-resources/error-trailing/diagnostics.txt", null, null);
    }

    @Test
    public void structureErrorDetection() {
        XMLTestUtils.performValidation("test-resources/error-structure/team.xml",
            "test-resources/error-structure/diagnostics.txt", null, null);
    }

    @Test
    public void attributeErrorNameOnly() {
        XMLTestUtils.performValidation("test-resources/error-attribute-name-only/team.xml",
            "test-resources/error-attribute-name-only/diagnostics.txt", null, null);
    }

    @Test
    public void attributeErrorNoQuotes() {
        XMLTestUtils.performValidation("test-resources/error-attribute-no-quotes/team.xml",
            "test-resources/error-attribute-no-quotes/diagnostics.txt", null, null);
    }

    @Test
    public void attributeErrorNoClosingQuote() {
        XMLTestUtils.performValidation("test-resources/error-attribute-no-closing-quote/team.xml",
            "test-resources/error-attribute-no-closing-quote/diagnostics.txt", null, null);
    }

    @Test
    public void elementErrorOpenTag() {
        XMLTestUtils.performValidation("test-resources/error-element-open-tag/team.xml",
            "test-resources/error-element-open-tag/diagnostics.txt", null, null);
    }

    @Test
    public void missingSchemaError() {
        XMLTestUtils.performValidation("test-resources/missing-schema/team.xml",
            "test-resources/missing-schema/diagnostics.txt", null, null);
    }

    @Test
    public void catalogMissingResolve() {
        XMLTestUtils.performValidation("test-resources/catalog-missing/team.xml",
            "test-resources/catalog-missing/diagnostics.txt", "test-resources/catalog-missing/catalog.xml", null);
    }

    @Test
    public void catalogSuccessfulResolve() {
        XMLTestUtils.performValidation("test-resources/catalog-resolve/team.xml",
            "test-resources/catalog-resolve/diagnostics.txt", "test-resources/catalog-resolve/catalog.xml", null);
    }

    @Test
    public void fileAssociation() {
        XMLAssociation xmlAssociation = new XMLAssociation("team.xml", XMLTestUtils.getTestResourceAbsolutePath("test-resources/file-association/team.xsd"));
        List<XMLAssociation> xmlAssociations = new ArrayList<XMLAssociation>();
        xmlAssociations.add(xmlAssociation);

        XMLTestUtils.performValidation("test-resources/file-association/team.xml",
            "test-resources/file-association/diagnostics.txt", null, xmlAssociations);
    }

    @Test
    public void fileAssociationWithCatalog() {
        XMLAssociation xmlAssociation = new XMLAssociation("team.xml", "http://team.xsd");
        List<XMLAssociation> xmlAssociations = new ArrayList<XMLAssociation>();
        xmlAssociations.add(xmlAssociation);

        XMLTestUtils.performValidation("test-resources/file-association-catalog/team.xml",
            "test-resources/file-association-catalog/diagnostics.txt",
            "test-resources/file-association-catalog/catalog.xml", xmlAssociations);
    }

}
