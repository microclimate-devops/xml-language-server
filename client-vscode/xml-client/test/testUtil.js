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

 // Add time stamp to log messages
function log(text) {
    let date = new Date();

    let year = date.getFullYear();

    // month starts at 0
    let month = date.getMonth() + 1;
    if (month < 10){ month = '0' + month}

    let day = date.getDate();
    if (day < 10){ day = '0' + day}

    let hours = date.getHours();
    if (hours < 10){ hours = '0' + hours}

    let minutes = date.getMinutes();
    if (minutes < 10){ minutes = '0' + minutes}

    let seconds = date.getSeconds();
    if (seconds < 10){ seconds = '0' + seconds}

    var millseconds = date.getMilliseconds();

    console.log(year + '-' + month + '-' + day + ' ' + hours + ':' + minutes + ':' + seconds + ':' + millseconds + ' - ' + text);
}
exports.log = log;

function getValidationRange(diagnostics, findMessage){
    if (diagnostics){
        let len = diagnostics.length;
        for (let i=0;i<len;i++){
            if (diagnostics[i].message.indexOf(findMessage) > -1){
                return diagnostics[i].range;
            }
        }
    }
    else {
        return null;
    }
}
exports.getValidationRange = getValidationRange;