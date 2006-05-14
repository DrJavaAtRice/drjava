/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import  junit.framework.*;

import java.io.*;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import edu.rice.cs.drjava.model.compiler.*;

/** Tests to ensure that compilation fails when expected, and that the errors
 *  are reported correctly.
 *
 *  Every test in this class is run for *each* of the compilers that is available.
 *
 *  @version $Id$
 */
public final class GlobalModelCompileErrorsTest extends GlobalModelTestCase {
  
  private static final String FOO_MISSING_CLOSE_TEXT = "class DrJavaTestFoo {";
  private static final String BAR_MISSING_SEMI_TEXT = "class DrJavaTestBar { int x }";
  private static final String FOO_PACKAGE_AFTER_IMPORT = "import java.util.*;\npackage a;\n" + FOO_TEXT;
  private static final String FOO_PACKAGE_INSIDE_CLASS = "class DrJavaTestFoo { package a; }";
  private static final String FOO_PACKAGE_AS_FIELD = "class DrJavaTestFoo { int package; }";
  private static final String FOO_PACKAGE_AS_FIELD_2 = "class DrJavaTestFoo { int package = 5; }";
  private static final String BAR_MISSING_SEMI_TEXT_MULTIPLE_LINES =
    "class DrJavaTestFoo {\n  int a = 5;\n  int x\n }";
  
//  /** Overrides setUp in order to save the Untitled file that resides in the model currently, so that saveBeforeCompile will not cause a failure*/
//  public void setUp() throws IOException{
//    super.setUp();
//    _model.getOpenDefinitionsDocuments().get(0).saveFile(new FileSelector(new File(_tempDir, "blank document")));
//  }
  
//  /** Overrides {@link TestCase#runBare} to interatively run this test case for each compiler, without resetting
//   *  the interactions JVM.  This method is called once per test method, and it magically invokes the method.
//   */
//  public void runBare() throws Throwable {
//    CompilerInterface[] compilers = CompilerRegistry.ONLY.getAvailableCompilers();
//    for (int i = 0; i < compilers.length; i++) {
//      //System.out.println("Run " + i + ": " + compilers[i]);
//      setUp();
//      _model.getCompilerModel().setActiveCompiler(compilers[i]);
//      try { runTest();  }
//      finally { tearDown(); }
//    }
//  }

  /** Gets the name of the compiler.
   *  @return the string representation of the active compiler
   */
  private String _name() { 
    return "compiler=" + _model.getCompilerModel().getActiveCompiler().getName() + ": "; 
  }

  /** Tests calling compileAll with different source roots works if the files have errors in them.  (Each file
   *  has 1 error.)
   * Note that this testcase will fail if several compilers can be found through the .drjava file.
   * As the test is then run one time per compiler it can find. 
   */
  public void testCompileAllFailsDifferentSourceRoots() throws BadLocationException, IOException, InterruptedException {
    
    File aDir = new File(_tempDir, "a");
    File bDir = new File(_tempDir, "b");
    aDir.mkdir();
    bDir.mkdir();
    
    OpenDefinitionsDocument doc = setupDocument(FOO_MISSING_CLOSE_TEXT);
    final File file = new File(aDir, "DrJavaTestFoo.java");
    doc.saveFile(new FileSelector(file));
                      
    OpenDefinitionsDocument doc2 = setupDocument(BAR_MISSING_SEMI_TEXT);
    final File file2 = new File(bDir, "DrJavaTestBar.java");
    doc2.saveFile(new FileSelector(file2));
    
    CompileShouldFailListener listener = new CompileShouldFailListener();

    _model.addListener(listener);
    
    CompilerModel cm = _model.getCompilerModel();    
    cm.compileAll();
    listener.waitCompileDone();

    assertCompileErrorsPresent(_name(), true);
    assertEquals("Should have 2 compiler errors", 2, _model.getCompilerModel().getNumErrors());
    listener.checkCompileOccurred();

    // Make sure .class does not exist for both files
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertEquals(_name() + "Class file exists after failing compile (1)", false, compiled.exists());
    File compiled2 = classForJava(file2, "DrJavaTestBar");
    assertEquals(_name() + "Class file exists after failing compile (2)", false, compiled2.exists());
    _model.removeListener(listener);
  }

  /** Creates a source file with "package" as a field name and ensures that compile starts but fails due to 
   *  the invalid field name.
   */
  public void testCompilePackageAsField() throws BadLocationException, IOException, InterruptedException {
    OpenDefinitionsDocument doc = setupDocument(FOO_PACKAGE_AS_FIELD);
    final File file = tempFile();
    doc.saveFile(new FileSelector(file));
    
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    
    doc.startCompile();
    listener.waitCompileDone();
    listener.checkCompileOccurred();

    // There better be an error since "package" can not be an identifier!
    assertCompileErrorsPresent(_name(), true);

    File compiled = classForJava(file, "DrJavaTestFoo");
    assertEquals(_name() + "Class file exists after failing compile", false, compiled.exists());
    _model.removeListener(listener);
  }

  /** Creates a source file with "package" as a field name and ensures that compile starts but fails due to the
   *  invalid field name. This is different from {@link #testCompilePackageAsField} as it initializes the field. 
   */
  public void testCompilePackageAsField2() throws BadLocationException, IOException, InterruptedException {
    OpenDefinitionsDocument doc = setupDocument(FOO_PACKAGE_AS_FIELD_2);
    final File file = tempFile();
    doc.saveFile(new FileSelector(file));
    
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    
    doc.startCompile();
    listener.waitCompileDone();
    listener.checkCompileOccurred();

    // There better be an error since "package" can not be an identifier!
    assertCompileErrorsPresent(_name(), true);

    File compiled = classForJava(file, "DrJavaTestFoo");
    assertEquals(_name() + "Class file exists after failing compile", false, compiled.exists());
    _model.removeListener(listener);
  }

  /** Tests compiling an invalid file and checks to make sure the class file was not created.  */
  public void testCompileMissingCloseSquiggly() throws BadLocationException, IOException, InterruptedException {
    OpenDefinitionsDocument doc = setupDocument(FOO_MISSING_CLOSE_TEXT);
    final File file = tempFile();
    doc.saveFile(new FileSelector(file));
   
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    
    doc.startCompile();
    listener.waitCompileDone();
    assertCompileErrorsPresent(_name(), true);
    listener.checkCompileOccurred();

    File compiled = classForJava(file, "DrJavaTestFoo");
    assertTrue(_name() + "Class file exists after compile?!", !compiled.exists());
    _model.removeListener(listener);
  }

  /** Puts an otherwise valid package statement inside a class declaration. This better not work! */
  public void testCompileWithPackageStatementInsideClass() throws BadLocationException, IOException, 
    InterruptedException {
    
    // Create temp file
    File baseTempDir = tempDirectory();
    File subdir = new File(baseTempDir, "a");
    File fooFile = new File(subdir, "DrJavaTestFoo.java");
    File compiled = classForJava(fooFile, "DrJavaTestFoo");

    // Now make subdirectory a
    subdir.mkdir();

    // Save the footext to DrJavaTestFoo.java in the subdirectory
    OpenDefinitionsDocument doc = setupDocument(FOO_PACKAGE_INSIDE_CLASS);
    doc.saveFileAs(new FileSelector(fooFile));

    // do compile -- should fail since package decl is not valid!
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    
    doc.startCompile();
    listener.waitCompileDone();

    listener.checkCompileOccurred();
    assertCompileErrorsPresent(_name(), true);
    assertTrue(_name() + "Class file exists after failed compile", !compiled.exists());

    // check that model.resetCompilerErrors works
    _model.getCompilerModel().resetCompilerErrors();
    CompilerErrorModel cem = _model.getCompilerModel().getCompilerErrorModel();
    assertEquals("CompilerErrorModel has errors after reset", 0, cem.getNumErrors());
    _model.removeListener(listener);
  }
  
   

  /** Tests the compiler errors have the correct line numbers.
   *  TODO: rewrite this test for the new error model interface
   */
  public void testCompileFailsCorrectLineNumbers() throws BadLocationException, IOException, InterruptedException {
    File aDir = new File(_tempDir, "a");
    File bDir = new File(_tempDir, "b");
    aDir.mkdir();
    bDir.mkdir();
    OpenDefinitionsDocument doc = setupDocument(FOO_PACKAGE_AFTER_IMPORT);
    final File file = new File(aDir, "DrJavaTestFoo.java");
    doc.saveFile(new FileSelector(file));
    OpenDefinitionsDocument doc2 = setupDocument(BAR_MISSING_SEMI_TEXT_MULTIPLE_LINES);
    final File file2 = new File(bDir, "DrJavaTestBar.java");
    doc2.saveFile(new FileSelector(file2));
    
   
    // do compile -- should fail since package decl is not valid!
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);

    CompilerModel cm = _model.getCompilerModel();
    cm.compileAll();
    listener.waitCompileDone();
    
    assertCompileErrorsPresent(_name(), true);
    assertEquals("Should have 2 compiler errors", 2, _model.getCompilerModel().getNumErrors());
    listener.checkCompileOccurred();
    _model.removeListener(listener);

    CompilerErrorModel cme = cm.getCompilerErrorModel();
    assertEquals("Should have had two errors", 2, cme.getNumErrors());

    CompilerError ce1 = cme.getError(0);
    CompilerError ce2 = cme.getError(1);
    assertEquals("first doc should have an error", file.getCanonicalFile(), ce1.file().getCanonicalFile());
    assertEquals("second doc should have an error", file2.getCanonicalFile(), ce2.file().getCanonicalFile());

    Position p1 = cme.getPosition(ce1);
    Position p2 = cme.getPosition(ce2);
    assertTrue("location of first error should be between 20 and 29 inclusive (line 2), but was " + p1.getOffset(),
               p1.getOffset() <= 20 && p1.getOffset() <= 29);
    assertTrue("location of error should be after 34 (line 3 or 4)", p2.getOffset() >= 34);
  }
}
