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

/**
 * For JSON to Java conversion (via GSON) this file
 * represents the JSON objects within the xmlAssociations
 */
public class XMLAssociation {
    String fileNamePattern;
    String systemId;

    public XMLAssociation(String fileNamePattern, String systemId) {
        this.fileNamePattern = fileNamePattern;
        this.systemId = systemId;
    }

    public String getFileNamePattern() {
        return fileNamePattern;
    }

    public String getSystemId() {
        return systemId;
    }
}
