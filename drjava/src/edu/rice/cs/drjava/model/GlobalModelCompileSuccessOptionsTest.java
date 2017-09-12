 /*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu).  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
 * following conditions are met:
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *      disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *      following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the names of its contributors may 
 *      be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.io.*;

import javax.swing.text.BadLocationException;

import edu.rice.cs.drjava.DrScala;
import edu.rice.cs.drjava.config.OptionConstants;

import edu.rice.cs.util.Log;
import edu.rice.cs.util.swing.Utilities;

/** Tests to ensure that compilation succeeds when expected.
  * @version $Id: GlobalModelCompileSuccessOptionsTest.java 5708 2012-08-29 23:52:35Z rcartwright $
  */
public final class GlobalModelCompileSuccessOptionsTest extends GlobalModelCompileSuccessTestCase {
  
  public static final Log _log  = new Log("GlobalModel.txt", false);

  /** Tests a compile on a file that references a "non-public" class defined in another class with a name different 
    * than the "non-public" class. Doesn't reset interactions because no interpretation is performed.  
    * NOTE: this is the DrScala conversion of a legacy DrJava test.  All top level classes in Scala are public.
    */
  public void testCompileReferenceToNonPublicClass() throws BadLocationException, IOException, InterruptedException {
    _log.log("+++Starting testCompileReferenceToNonPublicClass()");
    OpenDefinitionsDocument doc = setUpDocument(FOO_NON_PUBLIC_CLASS_TEXT);
    OpenDefinitionsDocument doc2 = setUpDocument(FOO2_REFERENCES_NON_PUBLIC_CLASS_TEXT);
    final File file = tempFile();
    final File file2 = tempFile();
    saveFile(doc, new FileSelector(file));
    saveFile(doc2, new FileSelector(file2));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    listener.compile(doc);
    _log.log("doc compiled");
    if (_model.getCompilerModel().getNumErrors() > 0) { fail("compile failed: " + getCompilerErrorString()); }
    listener.checkCompileOccurred();
    _model.removeListener(listener);
    _log.log("listener removed");
    
    CompileShouldSucceedListener listener2 = new CompileShouldSucceedListener();
    _model.addListener(listener2);
    listener2.compile(doc2);
    _log.log("doc2 compiled");
    if (_model.getCompilerModel().getNumErrors() > 0) { fail("compile failed: " + getCompilerErrorString()); }    
    listener2.checkCompileOccurred();
    _model.removeListener(listener2);
    
    Utilities.clearEventQueue();
    
    assertCompileErrorsPresent(_name(), false);
    
    // Make sure .class exists
    File compiled = classForScala(file, "DrScalaTestFoo");
    File compiled2 = classForScala(file, "DrScalaTestFoo2");
    assertTrue(_name() + "Class file should exist after compile", compiled.exists());
    assertTrue(_name() + "Class file should exist after compile", compiled2.exists());
    _log.log("+++Completing testCompileReferenceToNonPublicClass()");
  }

  /** Tests compiling a file with generics works with generic compilers.
   * (NOTE: this currently tests the GJ compiler, but not JSR-14...
   * JSR-14 is only available if the config option is set, and we clear
   * the config before running the tests.  We have a guess where the jar
   * is -- the lib directory -- but how can we get a URL for that?)
   */
  public void testCompileWithGenerics()throws BadLocationException, IOException, InterruptedException {
    _log.log("+++Starting testCompileWithGenerics()");
    // Only run this test if using a compiler with generics
    if (_isGenericCompiler()) {
      
      OpenDefinitionsDocument doc = setUpDocument(FOO_WITH_GENERICS);
      final File file = new File(_tempDir, "DrScalaTestFooGenerics.scala");
      saveFile(doc, new FileSelector(file));
      
      CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
      _model.addListener(listener);

      listener.compile(doc);

      if (_model.getCompilerModel().getNumErrors() > 0) {
        fail("compile failed: " + getCompilerErrorString());
      }
      assertCompileErrorsPresent(_name(), false);
      listener.checkCompileOccurred();
      _model.removeListener(listener);
      
      // Make sure .class exists
      File compiled = classForScala(file, "DrScalaTestFooGenerics");
      assertTrue(_name() + " FooGenerics Class file doesn't exist after compile", compiled.exists());
      _log.log("+++Completing testCompileWithGenerics()");
    }
  }
  
  /** Confirms that calling compileAll with different source roots succeeds. */
  public void testCompileAllDifferentSourceRoots() throws BadLocationException, IOException, InterruptedException {
    _log.log("+++Starting testCompileAllDifferentSourceRoots");

    File aDir = new File(_tempDir, "a");
    File bDir = new File(_tempDir, "b");
    aDir.mkdir();
    bDir.mkdir();
    OpenDefinitionsDocument doc = setUpDocument(FOO_TEXT);
    final File file1 = new File(aDir, "DrScalaTestFoo.scala");
    saveFile(doc, new FileSelector(file1));
    _log.log("Saved document as file DrScalaTestFoo.scala");
    OpenDefinitionsDocument doc2 = setUpDocument(BAR_TEXT);
    final File file2 = new File(bDir, "DrScalaTestBar.scala");
    saveFile(doc2, new FileSelector(file2));
    _log.log("Saved document as file DrScalaTestBar.scala");
    
    _log.log("Creating CompilerShouldSucceedListener");
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    _log.log("Compiling DrScalaTestFoo and DrScalaTestBar");
    _model.getCompilerModel().compileAll();
    
    Utilities.clearEventQueue();
    
    // Compile should succeed with multiple source roots
    assertEquals("compile succeeded despite multiple source roots", 0, _model.getCompilerModel().getNumErrors());
    assertCompileErrorsPresent(_name(), false);
    listener.checkCompileOccurred();

    // Make sure .class files exist for the first file in expected place
    File compiled = classForScala(file2, "DrScalaTestBar");
    _log.log("Class file for DrScalaTestBar = " + compiled);
    assertTrue(_name() + "Bar Class file exists after compile", compiled.exists());
    // Scalac does not respond to null destination by placing each class file in corresponding source file's folder

    _model.removeListener(listener);
    _log.log("+++Completing testCompileAllDifferentSourceRoots");
  }
}
