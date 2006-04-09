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

import edu.rice.cs.drjava.model.definitions.InvalidPackageException; 

import java.io.*;

import javax.swing.text.BadLocationException;

/**
 * Tests to ensure that compilation succeeds when expected.
 * 
 * Every test in this class is run for *each* of the compilers that is available.
 *
 * @version $Id$
 */
public final class GlobalModelCompileSuccessTest extends GlobalModelCompileSuccessTestCase {

  /**
   * Tests calling compileAll with different source roots works.
   */
  public void testCompileAllDifferentSourceRoots()
    throws BadLocationException, IOException, InterruptedException
  {
//    System.out.println("testCompileAllDifferentSourceRoots()");
    File aDir = new File(_tempDir, "a");
    File bDir = new File(_tempDir, "b");
    aDir.mkdir();
    bDir.mkdir();
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = new File(aDir, "DrJavaTestFoo.java");
    doc.saveFile(new FileSelector(file));
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    final File file2 = new File(bDir, "DrJavaTestBar.java");
    doc2.saveFile(new FileSelector(file2));
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    _model.addListener(listener);
    _model.getCompilerModel().compileAll();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    assertCompileErrorsPresent(_name(), false);
    listener.checkCompileOccurred();

    // Make sure .class exists for both files
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertTrue(_name() + "Foo Class file doesn't exist after compile",
               compiled.exists());
    File compiled2 = classForJava(file2, "DrJavaTestBar");
    assertTrue(_name() + "Bar Class file doesn't exist after compile",
               compiled2.exists());
    _model.removeListener(listener);
  }
  

  /**
   * Test that one compiled file can depend on the other and that when a keyword
   * is part of a field name, the file will compile.
   * We compile DrJavaTestFoo and then DrJavaTestFoo2 (which extends
   * DrJavaTestFoo). This shows that the compiler successfully found
   * DrJavaTestFoo2 when compiling DrJavaTestFoo.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testCompileClassPathOKDefaultPackage()
    throws BadLocationException, IOException, InterruptedException
  {
//    System.out.println("testCompileClasspathOKDefaultPackage()");
    // Create/compile foo, assuming it works
    OpenDefinitionsDocument doc1 = setupDocument(FOO_PACKAGE_AS_PART_OF_FIELD);
    final File fooFile = new File(_tempDir, "DrJavaTestFoo.java");
    
    doc1.saveFile(new FileSelector(fooFile));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    _model.addListener(listener);
    doc1.startCompile();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    listener.checkCompileOccurred();
    _model.removeListener(listener);

    OpenDefinitionsDocument doc2 = setupDocument(FOO2_EXTENDS_FOO_TEXT);
    final File foo2File = new File(_tempDir, "DrJavaTestFoo2.java");
    doc2.saveFile(new FileSelector(foo2File));

    CompileShouldSucceedListener listener2 = new CompileShouldSucceedListener(false);
    _model.addListener(listener2);
    doc2.startCompile();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    assertCompileErrorsPresent(_name(), false);
    listener2.checkCompileOccurred();

    // Make sure .class exists
    File compiled = classForJava(foo2File, "DrJavaTestFoo2");
    assertTrue(_name() + "Class file doesn't exist after compile",
               compiled.exists());
    _model.removeListener(listener2);
  }

  /**
   * Test that one compiled file can depend on the other.
   * We compile a.DrJavaTestFoo and then b.DrJavaTestFoo2 (which extends
   * DrJavaTestFoo). This shows that the compiler successfully found
   * DrJavaTestFoo2 when compiling DrJavaTestFoo.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testCompileClassPathOKDifferentPackages() throws BadLocationException, IOException, InterruptedException,
    InvalidPackageException {
//    System.out.println("testCompileClasspathOKDifferentPackages()");
    File aDir = new File(_tempDir, "a");
    File bDir = new File(_tempDir, "b");
    aDir.mkdir();
    bDir.mkdir();

    // Create/compile foo, assuming it works
    // foo must be public and in DrJavaTestFoo.java!
    OpenDefinitionsDocument doc1 = setupDocument("package a;\n" + "public " + FOO_TEXT);
    final File fooFile = new File(aDir, "DrJavaTestFoo.java");
//    System.err.println("fooFile = " + fooFile.getCanonicalPath());
    doc1.saveFile(new FileSelector(fooFile));
    // _packageName must be updated on save
    assertEquals("Check package name of doc1", "a", ((AbstractGlobalModel.ConcreteOpenDefDoc) doc1)._packageName); 
//    System.err.println("doc1 = " + doc1);
//    System.err.println("doc1 has source root " + doc1.getSourceRoot());
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    _model.addListener(listener);
    
    doc1.startCompile();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    listener.checkCompileOccurred();
    _model.removeListener(listener);
    
    OpenDefinitionsDocument doc2 =
      setupDocument("package b;\nimport a.DrJavaTestFoo;\n" + FOO2_EXTENDS_FOO_TEXT);
    final File foo2File = new File(bDir, "DrJavaTestFoo2.java");
//    System.err.println("foo2File = " + foo2File.getCanonicalPath());
    doc2.saveFile(new FileSelector(foo2File));
    // _packageName must be updated on save
    assertEquals("Check packangeName of doc2", "b", ((AbstractGlobalModel.ConcreteOpenDefDoc) doc2)._packageName); 
//    System.err.println("doc2 = " + doc2);

    CompileShouldSucceedListener listener2 = new CompileShouldSucceedListener(false);
    _model.addListener(listener2);
    
    doc2.startCompile();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    assertCompileErrorsPresent(_name(), false);
    listener2.checkCompileOccurred();
    _model.removeListener(listener2);

    // Make sure .class exists
    File compiled = classForJava(foo2File, "DrJavaTestFoo2");
    assertTrue(_name() + "Class file doesn't exist after compile",
               compiled.exists());
  }
}
