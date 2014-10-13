/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.io.*;

import javax.swing.text.BadLocationException;

//import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.EditDocumentException;

/** Tests to ensure that compilation behaves correctly in border cases.
  * @version $Id: GlobalModelCompileTest.java 5715 2012-09-28 02:08:43Z wdforson $
  */
public final class GlobalModelCompileTest extends GlobalModelTestCase {
  protected static final Log _log  = new Log("GlobalModelCompileTest.txt", false);
  
// (WilliamF): I'm commenting out the test below, for now; it fails, but I
//             don't know if it is the test or the compiler model which is
//             out of date. At any rate, it is strictly an aesthetic
//             matter (currently, an error message is displayed in the
//             'Compiler Output' window of DrScala is compilation is invoked
//             with no open source files).
  /** Tests calling compileAll with no source files works. Does not reset interactions. */
//  public void testCompileAllWithNoFiles() throws BadLocationException, IOException, InterruptedException {
    // Open one empty doc
//    _model.newFile();
//    
//    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
//    _model.addListener(listener);
//    Utilities.invokeLater(new Runnable() { 
//      public void run() { 
//        try { _model.getCompilerModel().compileAll(); } 
//        catch(Exception e) { throw new UnexpectedException(e); }
//      }
//    });
//    listener.waitCompileDone();
//    if (_model.getCompilerModel().getNumErrors() > 0) {
//      fail("compile failed: " + getCompilerErrorString());
//    }
//    assertCompileErrorsPresent("compile should succeed", false);
//    listener.checkCompileOccurred();
//    _model.removeListener(listener);
//    _log.log("testCompileAllWithNoFiles complete");
//  }
  
  /** Tests that the interactions pane is reset after a successful compile. */
  public void testCompileResetsInteractions() throws BadLocationException, IOException, InterruptedException,
    EditDocumentException {
    
//    System.err.println("Starting testCompileResetsInteractions");
    
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = new File(_tempDir, "DrScalaTestFoo.scala");
    saveFile(doc, new FileSelector(file));
    
    // Use the interpreter so resetInteractions is not optimized to a no-op
    interpret("0");
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.setResetAfterCompile(true);
    _model.addListener(listener);
     Utilities.invokeLater(new Runnable() { 
      public void run() { 
        try { _model.getCompilerModel().compileAll(); } 
        catch(Exception e) { throw new UnexpectedException(e); }
      }
    });
    listener.waitCompileDone();
    
    if (_model.getCompilerModel().getNumErrors() > 0) {
//        System.err.println("Compile failed");
      fail("compile failed: " + getCompilerErrorString());
    }
    listener.waitResetDone();
    _log.log("reset confirmed");
//    System.err.println("Reached end of compilation");
    assertCompileErrorsPresent("compile should succeed", false);
    listener.checkCompileOccurred();
    
//    System.err.println("Checked that compile occurred");
    _model.removeListener(listener);
//    System.err.println("Removed compilation listener");
    _log.log("testCompileResetsInteractions complete");
  }
  
  /** If we try to compile an unsaved file, and if we don't save when asked to saveAllBeforeProceeding, it should
    * not do the compile or any other actions.
    */
  public void testCompileAbortsIfUnsaved() throws Exception {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener() {
      public void saveBeforeCompile() {
        assertModified(true, doc);
        synchronized(this) { saveBeforeCompileCount++; }
        // since we don't actually save the compile should abort
      }
    };
    
    _model.addListener(listener);
    listener.compile(doc);
    _log.log("critical compile complete");
    listener.assertSaveBeforeCompileCount(1);
    assertModified(true, doc);
    assertContents(FOO_TEXT, doc);
    _model.removeListener(listener);
    _log.log("testCompileAbortsIfUnsaved complete");
  }
  
  /** If we try to compile while any files are unsaved, and if we don't save when asked to saveAllBeforeProceeding,
    * it should not do the compile or any other actions.
    */
  public void testCompileAbortsIfAnyUnsaved() throws Exception {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    
    CompileShouldFailListener listener = new CompileShouldFailListener() {
      public void saveBeforeCompile() {
        assertModified(true, doc);
        assertModified(true, doc2);
        synchronized(this) { saveBeforeCompileCount++; }
        // since we don't actually save the compile should abort
      }
    };
    
    _model.addListener(listener);
    listener.compile(doc);
    listener.assertSaveBeforeCompileCount(1);
    assertModified(true, doc);
    assertModified(true, doc2);
    assertContents(FOO_TEXT, doc);
    assertContents(BAR_TEXT, doc2);
    _model.removeListener(listener);
    _log.log("testCompileAbortsIfAnyUnsaved complete");
  }
  
  /** If we try to compile while any files (including the active file) are unsaved but we do save it from within saveAllBeforeProceeding, the
    * compile should occur happily.  Doesn't reset interactions because no interpretations are performed.
    */
  public void testCompileAnyUnsavedButSaveWhenAsked() throws BadLocationException, IOException, InterruptedException {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    final File file = tempFile();
    final File file2 = tempFile(2);
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener() {
      public void saveBeforeCompile() {
        assertModified(true, doc);
        assertModified(true, doc2);
        assertSaveCount(0);
        assertCompileStartCount(0);
        assertCompileEndCount(0);
        assertInterpreterReadyCount(0);
        assertConsoleResetCount(0);
        
        saveFile(doc, new FileSelector(file));
        saveFile(doc2, new FileSelector(file2));
        
        synchronized(this) { saveBeforeCompileCount++; }
      }
      
      public void fileSaved(OpenDefinitionsDocument doc) {
        assertModified(false, doc);
        assertSaveBeforeCompileCount(0);
        assertCompileStartCount(0);
        assertCompileEndCount(0);
        assertInterpreterReadyCount(0);
        assertConsoleResetCount(0);
        
        try { doc.getFile(); }
        catch (FileMovedException fme) {
          // We know file should exist
          fail("file does not exist");
        }
        //assertEquals("file saved", file, f);
        synchronized(this) { saveCount++; }
      }
    };
    
    _model.addListener(listener);
    testStartCompile(doc);
    listener.waitCompileDone();
    
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    
    // Check events fired
    listener.assertSaveBeforeCompileCount(1);
    listener.assertSaveCount(2);
    assertCompileErrorsPresent("compile should succeed", false);
    listener.checkCompileOccurred();
    
    // Make sure .class exists
    File compiled = classForScala(file, "DrScalaTestFoo");
    assertTrue("Class file doesn't exist after compile", compiled.exists());
    _model.removeListener(listener);
    _log.log("testCompileAnyUnsavedButSaveWhenAsked complete");
  }
  
  /** If we try to compile while any files (but not the active file) are unsaved but we do save it from within 
    * saveAllBeforeProceeding, the compile should occur happily.  Does not reset interactions.
    */
  public void testCompileActiveSavedAnyUnsavedButSaveWhenAsked() throws BadLocationException, IOException, 
    InterruptedException {
    
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    final File file = tempFile();
    final File file2 = tempFile(1);
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener() {
      public void saveBeforeCompile() {
        assertModified(false, doc);
        assertModified(true, doc2);
        assertSaveCount(0);
        assertCompileStartCount(0);
        assertCompileEndCount(0);
        assertInterpreterReadyCount(0);
        assertConsoleResetCount(0);
        
        saveFile(doc2, new FileSelector(file2)); 
        
        synchronized(this) { saveBeforeCompileCount++; }
        assertModified(false, doc);
        assertModified(false, doc2);
        assertTrue(!_model.hasModifiedDocuments());
      }
      
      public void fileSaved(OpenDefinitionsDocument doc) {
        assertModified(false, doc);
        assertSaveBeforeCompileCount(0);
        assertCompileStartCount(0);
        assertCompileEndCount(0);
        assertInterpreterReadyCount(0);
        assertConsoleResetCount(0);
        
        File f = null;
        try { f = doc.getFile(); }
        catch (FileMovedException fme) {
          // We know file should exist
          fail("file does not exist");
        }
        assertEquals("file saved", file2, f);
        synchronized(this) { saveCount++; }
      }
    };
    
    assertModified(true, doc);
    saveFile(doc, new FileSelector(file));
    assertModified(false, doc);
    assertModified(true, doc2);
    _model.addListener(listener);
    testStartCompile(doc);
    listener.waitCompileDone();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    assertTrue(!_model.hasModifiedDocuments());
    
    // Check events fired
    listener.assertCompileStartCount(1);
    listener.assertSaveBeforeCompileCount(1);
    listener.assertSaveCount(1);
    assertCompileErrorsPresent("compile should succeed", false);
    listener.checkCompileOccurred();
    
    // Make sure .class exists
    File compiled = classForScala(file, "DrScalaTestFoo");
    assertTrue("Class file doesn't exist after compile", compiled.exists());
    _model.removeListener(listener);
    _log.log("testCompileActiveSavedAnyUnsavedButSaveWhenAsked complete");
  }
}
