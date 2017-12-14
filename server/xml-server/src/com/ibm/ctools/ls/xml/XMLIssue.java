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

public class XMLIssue {

    private XMLIssueSeverity severity = XMLIssueSeverity.UNKNOWN;
    private int line = -1;
    private int column = -1;
    private String description = null;

    public XMLIssue(XMLIssueSeverity severity, int line, int column, String description) {
        this.severity = severity;
        this.line = line;
        this.column = column;
        this.description = description;
    }

    public XMLIssueSeverity getSeverity() {
        return severity;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getDescription() {
        return description;
    }

}
