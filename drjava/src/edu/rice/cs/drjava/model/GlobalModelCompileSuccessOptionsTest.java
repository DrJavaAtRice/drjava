/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import  junit.framework.*;

import java.io.*;

import java.util.LinkedList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Position;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.DocumentAdapterException;

/**
 * Tests to ensure that compilation succeeds when expected.
 * 
 * Every test in this class is run for *each* of the compilers that is available.
 *
 * @version $Id$
 */
public final class GlobalModelCompileSuccessOptionsTest extends GlobalModelCompileSuccessTestCase {

  /**
   * Tests a compile on a file that references a non-public class defined in
   * another class with a name different than the non-public class.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testCompileReferenceToNonPublicClass() 
    throws BadLocationException, IOException, InterruptedException
  {
//    System.out.println("testCompileReferenceToNonPublicClass()");
    OpenDefinitionsDocument doc = setupDocument(FOO_NON_PUBLIC_CLASS_TEXT);
    OpenDefinitionsDocument doc2 = setupDocument(FOO2_REFERENCES_NON_PUBLIC_CLASS_TEXT);
    final File file = tempFile();
    final File file2 = tempFile(1);
    doc.saveFile(new FileSelector(file));
    doc2.saveFile(new FileSelector(file2));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    _model.addListener(listener);
    doc.startCompile();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    listener.checkCompileOccurred();
    _model.removeListener(listener);
    CompileShouldSucceedListener listener2 = new CompileShouldSucceedListener(false);
    _model.addListener(listener2);
    doc2.startCompile();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }    
    
    listener2.checkCompileOccurred();
    _model.removeListener(listener2);
    assertCompileErrorsPresent(_name(), false);
    
    // Make sure .class exists
    File compiled = classForJava(file, "DrJavaTestFoo");
    File compiled2 = classForJava(file, "DrJavaTestFoo2");
    assertTrue(_name() + "Class file should exist after compile", compiled.exists());
    assertTrue(_name() + "Class file should exist after compile", compiled2.exists());
  }
  
  /**
   * Test support for assert keyword if enabled.
   * Note that this test only runs in Java 1.4 or higher.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testCompileWithJavaAssert()
    throws BadLocationException, IOException, InterruptedException
  {
//    System.out.println("testCompileWithJavaAssert()");
    // No assert support by default (or in 1.3)
    if(Float.valueOf(System.getProperty("java.specification.version")) < 1.5) {
      OpenDefinitionsDocument doc = setupDocument(FOO_WITH_ASSERT);
      final File file = tempFile();
      doc.saveFile(new FileSelector(file));
      CompileShouldFailListener listener = new CompileShouldFailListener();
      _model.addListener(listener);
      
      // This is a CompileShouldFailListener, so we don't need to wait.
      doc.startCompile();
      
      assertCompileErrorsPresent(_name(), true);
      listener.checkCompileOccurred();
      File compiled = classForJava(file, "DrJavaTestFoo");
      assertTrue(_name() + "Class file exists after compile?!", !compiled.exists());
      _model.removeListener(listener);
      
      
      // Only run assertions test in 1.4
      String version = System.getProperty("java.version");
      if ((version != null) && ("1.4.0".compareTo(version) <= 0)) {
        // Turn on assert support
        DrJava.getConfig().setSetting(OptionConstants.JAVAC_ALLOW_ASSERT,
                                      Boolean.TRUE);
        
        CompileShouldSucceedListener listener2 = new CompileShouldSucceedListener(false);
        _model.addListener(listener2);
        doc.startCompile();
        if (_model.getCompilerModel().getNumErrors() > 0) {
          fail("compile failed: " + getCompilerErrorString());
        }
        _model.removeListener(listener2);
        assertCompileErrorsPresent(_name(), false);
        listener2.checkCompileOccurred();
        
        // Make sure .class exists
        compiled = classForJava(file, "DrJavaTestFoo");
        assertTrue(_name() + "Class file doesn't exist after compile",
                   compiled.exists());
      }
    }
  }

  /**
   * Tests compiling a file with generics works with generic compilers.
   * (NOTE: this currently tests the GJ compiler, but not JSR-14...
   *  JSR-14 is only available if the config option is set, and we clear
   *  the config before running the tests.  We have a guess where the jar
   *  is -- the lib directory -- but how can we get a URL for that?)
   */
  public void testCompileWithGenerics()
    throws BadLocationException, IOException, InterruptedException
  {
//    System.out.println("testCompileWithGenerics()");
    // Only run this test if using a compiler with generics
    if (_isGenericCompiler()) {
      
      OpenDefinitionsDocument doc = setupDocument(FOO_WITH_GENERICS);
      final File file = new File(_tempDir, "DrJavaTestFooGenerics.java");
      doc.saveFile(new FileSelector(file));
      
      CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
      _model.addListener(listener);
      _model.getCompilerModel().compileAll();
      if (_model.getCompilerModel().getNumErrors() > 0) {
        fail("compile failed: " + getCompilerErrorString());
      }
      assertCompileErrorsPresent(_name(), false);
      listener.checkCompileOccurred();
      _model.removeListener(listener);
      
      // Make sure .class exists
      File compiled = classForJava(file, "DrJavaTestFooGenerics");
      assertTrue(_name() + "FooGenerics Class file doesn't exist after compile", compiled.exists());
    }
  }
}
