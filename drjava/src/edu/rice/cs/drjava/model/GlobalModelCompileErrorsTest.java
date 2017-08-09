/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import edu.rice.cs.drjava.model.compiler.*;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Tests to ensure that compilation fails when expected, and that the errors
  * are reported correctly.
  *
  * Every test in this class is run for *each* of the compilers that is available.
  *
  * @version $Id$
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
  protected static final String COMPILER_ERRORS_2872797_TEXT =
    "/**\n"+
    " * This is a simple class that really doesn't do anything.\n"+
    " * We'll use it to explore the kinds of error messages that\n"+
    " * the compiler will report when it encounters errors in \n"+
    " * Java source code.\n"+
    " */\n"+
    "public class CompilerErrors {\n"+
    "\n"+
    "  /**\n"+
    "   * Some shared storage, an instance variable.\n"+
    "   */\n"+
    "  int shared = 5;\n"+
    "  \n"+
    "  /**\n"+
    "   * a sample method. This method has no parameters\n"+
    "   * and no return value.\n"+
    "   */\n"+
    "  public void sampleMethod() {\n"+
    "    int x = 20;\n"+
    "    System.out.println(\"This is sampleMethod. x is \" + x);\n"+
    "  }\n"; // error, end of file, } missing
  
//  /** Overrides setUp in order to save the Untitled file that resides in the model currently, so that saveBeforeCompile will not cause a failure*/
//  public void setUp() throws IOException{
//    super.setUp();
//    _model.getOpenDefinitionsDocuments().get(0).saveFile(new FileSelector(new File(_tempDir, "blank document")));
//  }
  
//  /** Overrides {@link TestCase#runBare} to interatively run this test case for each compiler, without resetting
//   * the interactions JVM.  This method is called once per test method, and it magically invokes the method.
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
    * @return the string representation of the active compiler
    */
  private String _name() { 
    return "compiler=" + _model.getCompilerModel().getActiveCompiler().getName() + ": "; 
  }
  
  /** Tests calling compileAll with different source roots works if the files have errors in them.  (Each file
   * has 1 error.)
   * Note that this testcase will fail if several compilers can be found through the .drjava file.
   * As the test is then run one time per compiler it can find. 
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution is interrupted unexpectedly
   */
  public void testCompileAllFailsDifferentSourceRoots() throws BadLocationException, IOException, InterruptedException {
    debug.logStart();
    
    File aDir = new File(_tempDir, "a");
    File bDir = new File(_tempDir, "b");
    aDir.mkdir();
    bDir.mkdir();
    
    OpenDefinitionsDocument doc1 = setupDocument(FOO_MISSING_CLOSE_TEXT);
    final File file1 = new File(aDir, "DrJavaTestFoo.java");
    saveFile(doc1, new FileSelector(file1));  // runs synchronously in event thread
    
    OpenDefinitionsDocument doc2 = setupDocument(BAR_MISSING_SEMI_TEXT);
    final File file2 = new File(bDir, "DrJavaTestBar.java");
    saveFile(doc2, new FileSelector(file2));  // runs synchronously in event thread
    
    CompileShouldFailListener listener = new CompileShouldFailListener();
    
    _model.addListener(listener);
    
    CompilerModel cm = _model.getCompilerModel();    
    cm.compileAll();
    listener.waitCompileDone();
    
    assertCompileErrorsPresent(_name(), true);
//    System.err.println(cm.getErrorModel());
    assertEquals("Should have 2 compiler errors", 2, cm.getNumErrors());
    listener.checkCompileOccurred();
    
    // Make sure .class does not exist for both files
    File compiled1 = classForJava(file1, "DrJavaTestFoo");
    assertEquals(_name() + "Class file exists after failing compile (1)", false, compiled1.exists());
    File compiled2 = classForJava(file2, "DrJavaTestBar");
    assertEquals(_name() + "Class file exists after failing compile (2)", false, compiled2.exists());
    _model.removeListener(listener);
    
    debug.logEnd();
  }
  
  /** Creates a source file with "package" as a field name and ensures that 
   * compile starts but fails due to the invalid field name.
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution is interrupted unexpectedly
   */
  public void testCompilePackageAsField() throws BadLocationException, IOException, InterruptedException {
    debug.logStart();
    
    OpenDefinitionsDocument doc = setupDocument(FOO_PACKAGE_AS_FIELD);
    final File file = tempFile();
    saveFile(doc,new FileSelector(file));
    
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    
    testStartCompile(doc);
    
    listener.waitCompileDone();
    listener.checkCompileOccurred();
    
    // There better be an error since "package" can not be an identifier!
    assertCompileErrorsPresent(_name(), true);
    
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertEquals(_name() + "Class file exists after failing compile", false, compiled.exists());
    _model.removeListener(listener);
    
    debug.logEnd();
  }
  
  /** Creates a source file with "package" as a field name and ensures that compile starts but fails due to the
   * invalid field name. This is different from {@link #testCompilePackageAsField} as it initializes the field. 
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution is interrupted unexpectedly
   */
  public void testCompilePackageAsField2() throws BadLocationException, IOException, InterruptedException {
    debug.logStart();
    
    final OpenDefinitionsDocument doc = setupDocument(FOO_PACKAGE_AS_FIELD_2);
    final File file = tempFile();
    saveFile(doc, new FileSelector(file));
    
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    
    testStartCompile(doc);

    listener.waitCompileDone();
    listener.checkCompileOccurred();
    
    // There better be an error since "package" can not be an identifier!
    assertCompileErrorsPresent(_name(), true);
    
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertEquals(_name() + "Class file exists after failing compile", false, compiled.exists());
    _model.removeListener(listener);
    
    debug.logEnd();
  }
  
  /** Tests compiling an invalid file and checks to make sure the class file was not created. 
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution is interrupted unexpectedly
   */
  public void testCompileMissingCloseCurly() throws BadLocationException, IOException, InterruptedException {
    debug.logStart();
    
    final OpenDefinitionsDocument doc = setupDocument(FOO_MISSING_CLOSE_TEXT);
    final File file = tempFile();
    saveFile(doc, new FileSelector(file));
    
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    
    testStartCompile(doc);
    
    listener.waitCompileDone();
    assertCompileErrorsPresent(_name(), true);
    listener.checkCompileOccurred();
    
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertTrue(_name() + "Class file exists after compile?!", !compiled.exists());
    _model.removeListener(listener);
    
    debug.logEnd();
  }
  
  /** Puts an otherwise valid package statement inside a class declaration. This better not work! 
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution is interrupted unexpectedly
   */
  public void testCompileWithPackageStatementInsideClass() throws BadLocationException, IOException, 
    InterruptedException {
    debug.logStart();
    
    // Create temp file
    File baseTempDir = tempDirectory();
    File subdir = new File(baseTempDir, "a");
    File fooFile = new File(subdir, "DrJavaTestFoo.java");
    File compiled = classForJava(fooFile, "DrJavaTestFoo");
    
    // Now make subdirectory a
    subdir.mkdir();
    
    // Save the footext to DrJavaTestFoo.java in the subdirectory
    OpenDefinitionsDocument doc = setupDocument(FOO_PACKAGE_INSIDE_CLASS);
    saveFileAs(doc, new FileSelector(fooFile));
    
    // do compile -- should fail since package decl is not valid!
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    
    testStartCompile(doc);
    listener.waitCompileDone();
    
    listener.checkCompileOccurred();
    assertCompileErrorsPresent(_name(), true);
    assertTrue(_name() + "Class file exists after failed compile", !compiled.exists());
    
    // check that model.resetCompilerErrors works
    _model.getCompilerModel().resetCompilerErrors();
    CompilerErrorModel cem = _model.getCompilerModel().getCompilerErrorModel();
    assertEquals("CompilerErrorModel has errors after reset", 0, cem.getNumErrors());
    _model.removeListener(listener);
    
    debug.logEnd();
  }
  
  
  
  /** Tests the compiler errors have the correct line numbers.
   * TODO: rewrite this test for the new error model interface
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution is interrupted unexpectedly
   */
  public void testCompileFailsCorrectLineNumbers() throws BadLocationException, IOException, InterruptedException {
    debug.logStart();
    
    File aDir = new File(_tempDir, "a");
    File bDir = new File(_tempDir, "b");
    aDir.mkdir();
    bDir.mkdir();
    OpenDefinitionsDocument doc = setupDocument(FOO_PACKAGE_AFTER_IMPORT);
    final File file = new File(aDir, "DrJavaTestFoo.java");
    saveFile(doc, new FileSelector(file));
    OpenDefinitionsDocument doc2 = setupDocument(BAR_MISSING_SEMI_TEXT_MULTIPLE_LINES);
    final File file2 = new File(bDir, "DrJavaTestBar.java");
    saveFile(doc2, new FileSelector(file2));
    
    // do compile -- should fail since package decl is not valid!
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    
    CompilerModel cm = _model.getCompilerModel();
    cm.compileAll();
    debug.log("Before wait");
    listener.waitCompileDone();
    debug.log("After wait");
    
    assertCompileErrorsPresent(_name(), true);
    assertEquals("Should have 2 compiler errors", 2, _model.getCompilerModel().getNumErrors());
    listener.checkCompileOccurred();
    _model.removeListener(listener);
    
    CompilerErrorModel cme = cm.getCompilerErrorModel();
    assertEquals("Should have had two errors", 2, cme.getNumErrors());
    
    DJError ce1 = cme.getError(0);
    DJError ce2 = cme.getError(1);
    assertEquals("first doc should have an error", file.getCanonicalFile(), ce1.file().getCanonicalFile());
    assertEquals("second doc should have an error", file2.getCanonicalFile(), ce2.file().getCanonicalFile());
    
    Position p1 = cme.getPosition(ce1);
    Position p2 = cme.getPosition(ce2);
    assertTrue("location of first error should be between 20 and 29 inclusive (line 2), but was " + p1.getOffset(),
               p1.getOffset() <= 20 && p1.getOffset() <= 29);
    assertTrue("location of error should be after 34 (line 3 or 4)", p2.getOffset() >= 34);
    
    debug.logEnd();
  }
  
  /** Tests compiling an invalid file and checks to make sure the class file was not created.  
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution is interrupted unexpectedly
   */
  public void testCompileEndWhileParsing() throws BadLocationException, IOException, InterruptedException {
    debug.logStart();
    
    final OpenDefinitionsDocument doc = setupDocument(COMPILER_ERRORS_2872797_TEXT);
    final File dir = tempDirectory();
    final File file = new File(dir, "CompilerErrors.java");
    saveFile(doc, new FileSelector(file));
    
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    
    testStartCompile(doc);
    
    listener.waitCompileDone();
    assertCompileErrorsPresent(_name(), true);
    listener.checkCompileOccurred();
    
    File compiled = classForJava(file, "CompilerErrors");
    assertTrue(_name() + "Class file exists after compile?!", !compiled.exists());
    _model.removeListener(listener);

    file.delete();
    debug.logEnd();
  }
}
