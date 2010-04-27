/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.junit;

import java.io.IOException;
import java.util.List;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.util.text.SwingDocument;

public interface JUnitModel {
  
  //-------------------------- Field Setters --------------------------------//
  
  
  /** set the forceTestSuffix flag that forces class names in projects to end in "Test */
  public void setForceTestSuffix(boolean b);
  
  //-------------------------- Listener Management --------------------------//
  
  /** Add a JUnitListener to the model.
    * @param listener a listener that reacts to JUnit events
    */
  public void addListener(JUnitListener listener);
  
  /** Removes a JUnitListener from the model.  If the listener is not installed, this method has no effect.
    * @param listener a listener that reacts to JUnit events
    */
  public void removeListener(JUnitListener listener);
  
  /** Removes all JUnitListeners from this model. */
  public void removeAllListeners();
  
  //-------------------------------- Triggers --------------------------------//
  
  /** This is used by test cases and perhaps other things.  We should kill it. */
  public SwingDocument getJUnitDocument();
  
  /** Creates a JUnit test suite over all currently open documents and runs it.  If the class file 
    * associated with a file is not a test case, it will be ignored.  Synchronized against the compiler 
    * model to prevent testing and compiling at the same time, which would create invalid results.
    */
  public void junitAll();
  
  /** Creates a JUnit test suite over all currently open project documents and runs it.  If 
    * the class file associated with a file is not a test case, it will be ignored.  Synchronized 
    * against the compiler model to prevent testing and compiling at the same time, which would 
    * create invalid results.
    */
  public void junitProject();
  
  /** Runs JUnit over a list of documents.  Synchronized against the compiler model to prevent 
    * testing and compiling at the same time, which would create invalid results.
    * @param lod the list of documents that are to be run through JUnit testing.
    */
  public void junitDocs(List<OpenDefinitionsDocument> lod);
  
  /** Runs JUnit over a single document.  Synchronized against the compiler model to prevent testing
    * and compiling at the same time, which would create invalid results.
    * @param doc the document to be run under JUnit
    */
  public void junit(OpenDefinitionsDocument doc) throws ClassNotFoundException, IOException;
  
//  /** Forwards the classnames and files to the test manager to test all of them.
//    * @param qualifiedClassnames a list of all the qualified class names to test.
//    * @param files a list of their source files in the same order as qualified class names.
//    */
//  public void junitClasses(List<String> qualifiedClassnames, List<File> files);
//  
  //---------------------------- Model Callbacks ----------------------------//
  
  /** Cleans up an attempt JUnit test exeuction when suitable test code is not available.
    * @param isTestAll whether or not it was a use of the test all button
    * @param didCompileFail whether or not a compile before this JUnit attempt failed
    */
  public void nonTestCase(boolean isTestAll, boolean didCompileFail);
  
  //----------------------------- Error Results -----------------------------//
  
  /** Gets the JUnitErrorModel, which contains error info for the last test run. */
  public JUnitErrorModel getJUnitErrorModel();
  
  /** Resets the junit error state to have no errors. */
  public void resetJUnitErrors();
  
}
