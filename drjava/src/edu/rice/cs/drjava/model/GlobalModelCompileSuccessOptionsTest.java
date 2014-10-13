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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;

import edu.rice.cs.util.swing.Utilities;

/**
 * Tests to ensure that compilation succeeds when expected.
 * 
 * Every test in this class is run for *each* of the compilers that is available.
 *
 * @version $Id: GlobalModelCompileSuccessOptionsTest.java 5708 2012-08-29 23:52:35Z rcartwright $
 */
public final class GlobalModelCompileSuccessOptionsTest extends GlobalModelCompileSuccessTestCase {

  /** Tests a compile on a file that references a "non-public" class defined in another class with a name different 
    * than the "non-public" class. Doesn't reset interactions because no interpretations are performed.  
    * NOTE: this is the DrScala conversion of a legacy DrJava test.  All top level classes in Scala are public.
    */
  public void testCompileReferenceToNonPublicClass() 
    throws BadLocationException, IOException, InterruptedException {
//    System.out.println("testCompileReferenceToNonPublicClass()");
    OpenDefinitionsDocument doc = setupDocument(FOO_NON_PUBLIC_CLASS_TEXT);
    OpenDefinitionsDocument doc2 = setupDocument(FOO2_REFERENCES_NON_PUBLIC_CLASS_TEXT);
    final File file = tempFile();
    final File file2 = tempFile();
    saveFile(doc, new FileSelector(file));
    saveFile(doc2, new FileSelector(file2));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    listener.compile(doc);
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    listener.checkCompileOccurred();
    _model.removeListener(listener);
    CompileShouldSucceedListener listener2 = new CompileShouldSucceedListener();
    _model.addListener(listener2);
    listener2.compile(doc2);
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }    
    
    listener2.checkCompileOccurred();
    _model.removeListener(listener2);
    assertCompileErrorsPresent(_name(), false);
    
    // Make sure .class exists
    File compiled = classForScala(file, "DrScalaTestFoo");
    File compiled2 = classForScala(file, "DrScalaTestFoo2");
    assertTrue(_name() + "Class file should exist after compile", compiled.exists());
    assertTrue(_name() + "Class file should exist after compile", compiled2.exists());
  }
  
  /** Test support for assert keyword if enabled.
   * Note that this test only runs in Java 1.4 or higher.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testCompileWithJavaAssert()
    throws BadLocationException, IOException, InterruptedException {
//    System.out.println("testCompileWithJavaAssert()");
    // No assert support by default (or in 1.3)
    if (Float.valueOf(System.getProperty("java.specification.version")) < 1.5) {
      OpenDefinitionsDocument doc = setupDocument(FOO_WITH_ASSERT);
      final File file = tempFile();
      saveFile(doc, new FileSelector(file));
      CompileShouldFailListener listener = new CompileShouldFailListener();
      _model.addListener(listener);
      
      // This is a CompileShouldFailListener, so we don't need to wait.
      listener.compile(doc);
      
      assertCompileErrorsPresent(_name(), true);
      listener.checkCompileOccurred();
      File compiled = classForScala(file, "DrScalaTestFoo");
      assertTrue(_name() + "Class file exists after compile?!", ! compiled.exists());
      _model.removeListener(listener);
      

      // not releant to DrScala
//      // Only run assertions test in 1.4
//      String version = System.getProperty("java.version");
//      if ((version != null) && ("1.4.0".compareTo(version) <= 0)) {
//        // Turn on assert support
//        DrJava.getConfig().setSetting(OptionConstants.RUN_WITH_ASSERT,
//                                      Boolean.TRUE);
//        
//        CompileShouldSucceedListener listener2 = new CompileShouldSucceedListener();
//        _model.addListener(listener2);
//        listener2.compile(doc);
//        if (_model.getCompilerModel().getNumErrors() > 0) {
//          fail("compile failed: " + getCompilerErrorString());
//        }
//        _model.removeListener(listener2);
//        assertCompileErrorsPresent(_name(), false);
//        listener2.checkCompileOccurred();
//        
//        // Make sure .class exists
//        compiled = classForJava(file, "DrScalaTestFoo");
//        assertTrue(_name() + "Class file doesn't exist after compile",
//                   compiled.exists());
//      }
    }
  }

  /** Tests compiling a file with generics works with generic compilers.
   * (NOTE: this currently tests the GJ compiler, but not JSR-14...
   * JSR-14 is only available if the config option is set, and we clear
   * the config before running the tests.  We have a guess where the jar
   * is -- the lib directory -- but how can we get a URL for that?)
   */
  public void testCompileWithGenerics()throws BadLocationException, IOException, InterruptedException {
//    System.out.println("testCompileWithGenerics()");
    // Only run this test if using a compiler with generics
    if (_isGenericCompiler()) {
      
      OpenDefinitionsDocument doc = setupDocument(FOO_WITH_GENERICS);
      final File file = new File(_tempDir, "DrScalaTestFooGenerics.scala");
      saveFile(doc, new FileSelector(file));
      
      CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
      _model.addListener(listener);
//      _model.getCompilerModel().compileAll();
      listener.compile(doc);

      if (_model.getCompilerModel().getNumErrors() > 0) {
        fail("compile failed: " + getCompilerErrorString());
      }
      assertCompileErrorsPresent(_name(), false);
      listener.checkCompileOccurred();
      _model.removeListener(listener);
      
      // Make sure .class exists
      File compiled = classForScala(file, "DrScalaTestFooGenerics");
      assertTrue(_name() + "FooGenerics Class file doesn't exist after compile", compiled.exists());
    }
  }
}
