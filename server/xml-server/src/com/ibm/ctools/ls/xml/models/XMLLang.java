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

package com.ibm.ctools.ls.xml.models;

import java.util.List;

/**
 * For JSON to Java conversion (via GSON) this file
 * represents the JSON object XMLLang
 */
public class XMLLang {
    // The name of these variables MUST match the JSON element
    String xmlCatalogFiles;
    List<XMLAssociation> xmlAssociations;

    public String getXmlCatalogFiles() {
        return xmlCatalogFiles;
    }

    public List<XMLAssociation> getXMLAssociations() {
        return xmlAssociations;
    }
}
