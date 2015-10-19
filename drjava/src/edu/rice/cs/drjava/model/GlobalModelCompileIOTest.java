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

package edu.rice.cs.drjava.model;

import java.io.*;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;

import javax.swing.text.BadLocationException;

/** Tests to ensure that compilation interacts with files correctly.
  *
  * @version $Id$
  */
public final class GlobalModelCompileIOTest extends GlobalModelTestCase {
  
  /** 
   * After creating a new file, saving, and compiling it, this test checks that the new document is in sync after
   * compiling and is out of sync after modifying and even saving it.
   * Doesn't reset interactions because no interpretations are performed.
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution is interrupted unexpectedly
   */
  public void testClassFileSynchronization() throws BadLocationException, IOException, InterruptedException {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
//    System.err.println("Temp source file is " + file.getAbsolutePath());
    
    saveFile(doc, new FileSelector(file));
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
//    System.err.println("Cached class file is " + doc.getCachedClassFile().getAbsolutePath());
    assertTrue("Class file should not exist before compile", doc.getCachedClassFile() == FileOps.NULL_FILE);
    assertTrue("should not be in sync before compile", ! doc.checkIfClassFileInSync());
    assertTrue("The state of all open documents should be out of sync", _model.hasOutOfSyncDocuments());
    testStartCompile(doc);
    listener.waitCompileDone();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    _model.removeListener(listener);
    listener.checkCompileOccurred();
    assertTrue("should be in sync after compile", doc.checkIfClassFileInSync());
//    System.err.println(_model.getOpenDefinitionsDocuments());
    assertTrue("The state of all open documents should be in sync", ! _model.hasOutOfSyncDocuments());
    doc.insertString(0, "hi", null);
    assertTrue("should not be in sync after modification", ! doc.checkIfClassFileInSync());
    
    // Have to wait 2 seconds so file will have a different timestamp
    Thread.sleep(2000);
    
    saveFile(doc, new FileSelector(file));
    assertTrue("should not be in sync after save", ! doc.checkIfClassFileInSync());
    
    // Make sure .class exists
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertTrue(" Class file should exist after compile", compiled.exists());
  }
  
  /** 
   * Ensure that renaming a file makes it out of sync with its class file.
   * Doesn't reset interactions because no interpretations are performed.
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws IOException if an IO operation fails
   * @throws IllegalStateException if an illegal state is reached
   * @throws InterruptedException if execution is interrupted unexpectedly
   */
  public void testClassFileSynchronizationAfterRename() throws BadLocationException, IOException, IllegalStateException,
    InterruptedException {
    
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
    final File file2 = tempFile(2);
    
    saveFile(doc, new FileSelector(file));
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
//    System.err.println("cached class file is " + doc.getCachedClassFile());
    assertTrue("Class file should not exist before compile", doc.getCachedClassFile() == FileOps.NULL_FILE);
    assertTrue("should not be in sync before compile", !doc.checkIfClassFileInSync());
    testStartCompile(doc);
    listener.waitCompileDone();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    _model.removeListener(listener);
    listener.checkCompileOccurred();
    assertTrue("should be in sync after compile",
               doc.checkIfClassFileInSync());
    
    // Have to wait 1 second so file will have a different timestamp
    Thread.sleep(2000);
    
    // Rename to a different file
    saveFileAs(doc, new FileSelector(file2));
    assertTrue("should not be in sync after renaming", ! doc.checkIfClassFileInSync());
  }
  
  /** 
   * Tests a compile after a file has unexpectedly been moved or delete. 
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws IOException if an IO operation fails
   */
  public void testCompileAfterFileMoved() throws BadLocationException, IOException {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
    saveFile(doc, new FileSelector(file));
    TestListener listener = new TestListener();
    _model.addListener(listener);
    file.delete();
    Utilities.invokeLater(new Runnable() { 
      public void run() {
        try {
          doc.startCompile();
          fail("Compile should not have begun.");
        }
        catch(FileMovedException e) { /* The expected behavior! */ }
        catch(Exception e) { throw new UnexpectedException(e); }
      }
    });

    assertCompileErrorsPresent("compile should succeed", false);
    
    // Make sure .class exists
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertTrue("Class file shouldn't exist after compile", !compiled.exists());
    _model.removeListener(listener);
  }
  
}
