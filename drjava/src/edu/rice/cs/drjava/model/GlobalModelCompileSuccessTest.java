/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 *
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
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
public final class GlobalModelCompileSuccessTest extends GlobalModelTestCase {

  private static final String FOO_PACKAGE_AS_PART_OF_FIELD =
    "class DrJavaTestFoo { int cur_package = 5; }";

  private static final String FOO2_EXTENDS_FOO_TEXT =
    "class DrJavaTestFoo2 extends DrJavaTestFoo {}";
  
  private static final String FOO_NON_PUBLIC_CLASS_TEXT =
    "class DrJavaTestFoo {} class Foo{}";
  
  private static final String FOO2_REFERENCES_NON_PUBLIC_CLASS_TEXT =
    "class DrJavaTestFoo2 extends Foo{}";
  
  private static final String FOO_WITH_ASSERT =
    "class DrJavaTestFoo { void foo() { assert true; } }";

  private static final String FOO_WITH_GENERICS =
    "class DrJavaTestFooGenerics<T> {}";
  
  /**
   * Constructor.
   * @param  String name
   */
  public GlobalModelCompileSuccessTest(String name) {
    super(name);
  }


  /**
   * Overrides {@link TestCase#runBare} to interactively run this
   * test case for each compiler, without resetting the interactions JVM.
   * This method is called once per test method, and it magically
   * invokes the method.
   */
  public void runBare() throws Throwable {
    CompilerInterface[] compilers = CompilerRegistry.ONLY.getAvailableCompilers();
    for (int i = 0; i < compilers.length; i++) {
      //System.out.println("Run " + i + ": " + compilers[i]);
      setUp();
      _model.setActiveCompiler(compilers[i]);

      try {
        runTest();
      }
      finally {
        tearDown();
      }
    }
  }

  private String _name() {
    return "compiler=" + _model.getActiveCompiler().getName() + ": ";
  }
  

  /**
   * Tests calling compileAll with different source roots works.
   */
  public void testCompileAllDifferentSourceRoots()
    throws BadLocationException, IOException, InterruptedException
  {
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
    int numErrors = _model.getNumErrors();
    _model.compileAll();
    numErrors = _model.getNumErrors();
    if (_model.getNumErrors() > 0) {
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
  public void testCompileClasspathOKDefaultPackage()
    throws BadLocationException, IOException, InterruptedException
  {
    // Create/compile foo, assuming it works
    OpenDefinitionsDocument doc1 = setupDocument(FOO_PACKAGE_AS_PART_OF_FIELD);
    final File fooFile = new File(_tempDir, "DrJavaTestFoo.java");
    
    doc1.saveFile(new FileSelector(fooFile));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    _model.addListener(listener);
    doc1.startCompile();
    if (_model.getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    _model.removeListener(listener);

    OpenDefinitionsDocument doc2 = setupDocument(FOO2_EXTENDS_FOO_TEXT);
    final File foo2File = new File(_tempDir, "DrJavaTestFoo2.java");
    doc2.saveFile(new FileSelector(foo2File));

    CompileShouldSucceedListener listener2 = new CompileShouldSucceedListener(false);
    _model.addListener(listener2);
    doc2.startCompile();
    if (_model.getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    assertCompileErrorsPresent(_name(), false);
    listener.checkCompileOccurred();

    // Make sure .class exists
    File compiled = classForJava(foo2File, "DrJavaTestFoo2");
    assertTrue(_name() + "Class file doesn't exist after compile",
               compiled.exists());
    _model.removeListener(listener);
  }

  /**
   * Test that one compiled file can depend on the other.
   * We compile a.DrJavaTestFoo and then b.DrJavaTestFoo2 (which extends
   * DrJavaTestFoo). This shows that the compiler successfully found
   * DrJavaTestFoo2 when compiling DrJavaTestFoo.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testCompileClasspathOKDifferentPackages()
    throws BadLocationException, IOException, InterruptedException
  {
    File aDir = new File(_tempDir, "a");
    File bDir = new File(_tempDir, "b");
    aDir.mkdir();
    bDir.mkdir();

    // Create/compile foo, assuming it works
    // foo must be public and in DrJavaTestFoo.java!
    OpenDefinitionsDocument doc1 =
      setupDocument("package a;\n" + "public " + FOO_TEXT);
    final File fooFile = new File(aDir, "DrJavaTestFoo.java");
    doc1.saveFile(new FileSelector(fooFile));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    _model.addListener(listener);
    doc1.startCompile();
    if (_model.getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    _model.removeListener(listener);
    
    OpenDefinitionsDocument doc2 =
      setupDocument("package b;\nimport a.DrJavaTestFoo;\n" + FOO2_EXTENDS_FOO_TEXT);
    final File foo2File = new File(bDir, "DrJavaTestFoo2.java");
    doc2.saveFile(new FileSelector(foo2File));

    CompileShouldSucceedListener listener2 = new CompileShouldSucceedListener(false);
    _model.addListener(listener2);
    doc2.startCompile();
    if (_model.getNumErrors() > 0) {
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

  
  /**
   * Tests a compile on a file that references a non-public class defined in
   * another class with a name different than the non-public class.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testCompileReferenceToNonPublicClass() 
    throws BadLocationException, IOException, InterruptedException
  {
    OpenDefinitionsDocument doc = setupDocument(FOO_NON_PUBLIC_CLASS_TEXT);
    OpenDefinitionsDocument doc2 = setupDocument(FOO2_REFERENCES_NON_PUBLIC_CLASS_TEXT);
    final File file = tempFile();
    final File file2 = tempFile(1);
    doc.saveFile(new FileSelector(file));
    doc2.saveFile(new FileSelector(file2));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    _model.addListener(listener);
    doc.startCompile();
    if (_model.getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    _model.removeListener(listener);
    CompileShouldSucceedListener listener2 = new CompileShouldSucceedListener(false);
    _model.addListener(listener2);
    doc2.startCompile();
    if (_model.getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    
    listener.checkCompileOccurred();
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
    // No assert support by default (or in 1.3)
    OpenDefinitionsDocument doc = setupDocument(FOO_WITH_ASSERT);
    final File file = tempFile();
    doc.saveFile(new FileSelector(file));
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    
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
      if (_model.getNumErrors() > 0) {
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
    // Only run this test if using a compiler with generics
    if (_isGenericCompiler()) {
      
      OpenDefinitionsDocument doc = setupDocument(FOO_WITH_GENERICS);
      final File file = new File(_tempDir, "DrJavaTestFooGenerics.java");
      doc.saveFile(new FileSelector(file));
      
      CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
      _model.addListener(listener);
      _model.compileAll();
      if (_model.getNumErrors() > 0) {
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
  
  /**
   * Returns whether the currently active compiler supports generics.
   */
  protected boolean _isGenericCompiler() {
    String name = _model.getActiveCompiler().getClass().getName();
    for (int i=0; i < CompilerRegistry.GENERIC_JAVA_COMPILERS.length; i++) {
      if (name.equals(CompilerRegistry.GENERIC_JAVA_COMPILERS[i])) {
        //System.out.println(name + " supports generics");
        return true;
      }
    }
    //System.out.println(name + " doesn't support generics");
    return false;
  }
  
}
