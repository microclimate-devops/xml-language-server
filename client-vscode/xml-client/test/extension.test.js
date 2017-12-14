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

// The module 'assert' provides assertion methods from node
const assert = require('assert');

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
const vscode = require('vscode');

const testUtil = require('./testUtil');
const extension = require('../client/extension');

const extensionLocation = process.env.EXTENSION_WORKSPACE;
const extensionWorkspace = process.env.EXTENSION_WORKSPACE + '/test/workspace';

// The VSCode framework appears to be a bit different than what
// is documented, but the concepts are the same. The async is still
// handled with the done() function, to ensure the async testsing
// can be done. The it() and describe() appears to be replaced with
// test() and suite() in this VSCode test runner framework.
//
// General Notes:
// - setTimeout() is used to allow things like XML language validation to occur, otherwise
// it will not happen as the test thread is blocking it from happening
// - vscode's Position and Ranges all begin at index 0 (even though the line number and 
// column numbers in vscode show as 1)
//
// Reference: http://mochajs.org/#asynchronous-code
suite('Extension Tests', function() {
  
      let langClient = null;

      // Defines a Mocha unit test
      test('Create language client', function(done) {
        // Default timeout in mocha is 2000ms, set to 0 to disable
        this.timeout(0);

        let haveJavaDefined = false;
        if (process.env.JAVA_HOME){
            haveJavaDefined = true;
        }

        assert.equal(haveJavaDefined,true,'JAVA_HOME variable has not been defined');

        let serverJar = extensionLocation + '/server/ibm-xml-server-all.jar';
        let javaPath = process.env.JAVA_HOME + '/bin/java';

        langClient = extension.initLangClient(serverJar, javaPath);
            
        testUtil.log('Start language client');
        langClient.start();

        let isLangClientReady = false;

        langClient.onReady().then(function () {
            isLangClientReady = true;
            testUtil.log('Language client is ready');
        });

        setTimeout(function(){
            assert.equal(isLangClientReady,true,'Language server has started');

            done()
        },1000);
      });

      test('XML validation on open (invalid schema)',function(done){
        this.timeout(0);

        let fileLocation = vscode.Uri.parse('file:' + extensionWorkspace + '/team.xml');
        vscode.workspace.openTextDocument(fileLocation).then((doc) => {
            vscode.window.showTextDocument(doc, 1, true).then(e => {
                e.edit(edit => {
                    // Need to wait for vaidation to happen
                    setTimeout(function(){
                        let currDiagnostics = langClient.diagnostics.get('file://' + extensionWorkspace + '/team.xml');
                        
                        // Check aggregate validation errors
                        assert.equal(currDiagnostics.length,142,'Initial open expected validation errors are not 142');
                        
                        // Check specific validation error
                        let range = testUtil.getValidationRange(currDiagnostics,'Cannot find the declaration of element \'team\' (cvc-elt.1).');
                        
                        assert.equal(range.start.line,1,'Expect message start line 1');
                        assert.equal(range.start.character,1,'Expect message start character 1');
                        
                        done();
                    },1000);
                });
            });
        }, (error) => {
            console.error(error);
            done();
        });
      });


      test('XML validation on edit to valid schema',function(done){
        this.timeout(0);

        let fileLocation = vscode.Uri.parse('file:' + extensionWorkspace + '/team.xml');
        vscode.workspace.openTextDocument(fileLocation).then((doc) => {
            vscode.window.showTextDocument(doc, 1, true).then(e => {
                e.edit(edit => {
                    edit.replace(new vscode.Position(1, 91),"team.xsd");

                    // Need to wait for vaidation to happen
                    setTimeout(function(){
                        let currDiagnostics = langClient.diagnostics.get('file://' + extensionWorkspace + '/team.xml');
                        
                        assert.equal(currDiagnostics.length,2,'Expected validation errors when updating to valid scehma are not 2');
            
                        // Check specific validation error
                        let range = testUtil.getValidationRange(currDiagnostics,'Value \'invalid\' is not facet-valid with respect to enumeration \'[developer, manager, leader, architect]\'. It must be a value from the enumeration (cvc-enumeration-valid).');
                        
                        assert.equal(range.start.line,2,'Expect message start line 2');
                        assert.equal(range.start.character,45,'Expect message start character 45');

                        done();
                    },1000);
                });
            });
        }, (error) => {
            console.error(error);
            done();
        });

      });

      
      test('XML validation fix all validation errors',function(done){
        this.timeout(0);

        let fileLocation = vscode.Uri.parse('file:' + extensionWorkspace + '/team.xml');
        vscode.workspace.openTextDocument(fileLocation).then((doc) => {
            vscode.window.showTextDocument(doc, 1, true).then(e => {
                e.edit(edit => {
                    let range = new vscode.Range(new vscode.Position(2, 45),new vscode.Position(2, 52));
                    edit.replace(range,'architect');

                    // Need to wait for vaidation to happen
                    setTimeout(function(){
                        let currDiagnostics = langClient.diagnostics.get('file://' + extensionWorkspace + '/team.xml');
                        

                        assert.equal(currDiagnostics.length,0,'Expect no validation errors');

                        done();
                    },1000);
                });
            });
        }, (error) => {
            console.error(error);
            done();
        });
      });
});