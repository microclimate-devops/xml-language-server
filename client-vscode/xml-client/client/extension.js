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

const vscode = require('vscode');
const vscode_languageclient = require('vscode-languageclient');
const path = require('path');
const net = require('net');
const cp = require('child_process');
const fs = require('fs');
const vscode_languageserver_protocol = require("vscode-languageserver-protocol");

const XML_CONFIG_PATH = '/.vscode/xmlLang.json';

// Messages to the user
const LANGUAGE_CLIENT_ID = 'xml';
const LANGUAGE_CLIENT_NAME = 'XML Language Client';
const EXTENSION_START_MSG = 'XML Language Client Extension started';
const LANGUAGE_CLIENT_READY_MSG = 'XML language client and server are ready';
const LANGUAGE_CLIENT_JAVA_WARNING = 'Failure to initialize language server: the XML language server requires JDK 1.8 set in the JAVA_HOME or the xmlLang.javaHome VSCode setting';
const LANGUAGE_CLIENT_JAVA_INVALID_VERSION = 'Invalid JDK version';
const LANGUAGE_CLIENT_JAVA_VSCODE_SETTING_NULL = 'xmlLang.javaHome VSCode setting is not set';
const LANGUAGE_CLIENT_JAVA_START_PATH = 'Starting language server with java path: ';

function activate(context){
    console.log(EXTENSION_START_MSG);

    let serverJarPath = context.asAbsolutePath(path.join('server', 'ibm-xml-server-all.jar'));

    // 1. Check if the VSCode settings has defined the "xmlLang.javaHome" property. If the user
    // has not specified it in the workspace, it will look at the global settings after
    // 2. If the VSCode setting is not defined, use the JAVA_HOME argument
    // 3. If neither of the two properties above are set, show a message to the user
    javaVerifierPromise(vscode.workspace.getConfiguration().get('xmlLang.javaHome', null)).then(
        function(res){
            context.subscriptions.push(initLangClient(serverJarPath, res).start());
        }
    ).catch(function(rej){
        console.log(rej);

        javaVerifierPromise(process.env['JAVA_HOME']).then(
            function(res){
                context.subscriptions.push(initLangClient(serverJarPath, res).start());
            }
        ).catch(function(rej){
            console.log(rej);

            vscode.window.showWarningMessage(LANGUAGE_CLIENT_JAVA_WARNING);
        });
    });
}

function initLangClient(serverJarPath, javaPath){
    console.log(LANGUAGE_CLIENT_JAVA_START_PATH + javaPath);
    
        // Create the client options used for the vscode_languageclient.LanguageClient
        let clientOptions = {

            documentSelector: [
                {scheme: 'file', language: 'xml'}
            ],
            synchronize: {
                // The configurationSection needs to match the configuration definition in
                // the package.json
                configurationSection: 'xmlLang',
                fileEvents: vscode.workspace.createFileSystemWatcher('**/*.xml')
            }
        };
    
        // Create the server options used for the vscode_languageclient.LanguageClient
        // Note: the server will be started by the client
        let serverOptions = {
            command: javaPath,
            args: ['-jar', serverJarPath],
            options: {stdio: 'pipe'}
        };
    
        let langClient = new vscode_languageclient.LanguageClient(LANGUAGE_CLIENT_ID,LANGUAGE_CLIENT_NAME, serverOptions, clientOptions);
    
        langClient.onReady().then(function () {
            console.log(LANGUAGE_CLIENT_READY_MSG);
        });

        return langClient;
}

exports.initLangClient = initLangClient;

function javaVerifierPromise(testPath){
    return new Promise(function(resolve, reject){
        if (!testPath){
            reject(LANGUAGE_CLIENT_JAVA_VSCODE_SETTING_NULL);
        } 
        else{
            let pathToJavaExec = testPath + '/bin/java';
            cp.execFile(pathToJavaExec, ['-version'], {}, function (error, stdout, stderr) {
                if (error){
                    reject(error);
                }
                if (stderr){
                    if (stderr.indexOf('1.8') >= 0){
                        resolve(pathToJavaExec);
                    }
                    else {
                        reject(LANGUAGE_CLIENT_JAVA_INVALID_VERSION);
                    }
                }         
            });
        }
    });
}

exports.activate = activate;

function deactivate() {
}
exports.deactivate = deactivate;