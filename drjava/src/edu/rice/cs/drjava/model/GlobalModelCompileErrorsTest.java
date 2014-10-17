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
// * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
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
import javax.swing.text.Position;

import edu.rice.cs.drjava.model.compiler.*;

import edu.rice.cs.util.Log;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Tests to ensure that compilation fails when expected, and that the errors
  * are reported correctly.
  *
  * Every test in this class is run for *each* of the compilers that is available.
  *
  * @version $Id: GlobalModelCompileErrorsTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public final class GlobalModelCompileErrorsTest extends GlobalModelTestCase {
  
  public static final Log _log  = new Log("GlobalModel.txt", false);
  
  private static final String FOO_MISSING_VAR_KEYWORD = "class DrScalaTestFoo { yy }";
  private static final String BAR_MISSING_DECLARATION_KEYWORD = "class DrScalaTestBar { zz }";
//  private static final String FOO_PACKAGE_AFTER_IMPORT = "import java.util._ \npackage a\n" + FOO_TEXT;
//  private static final String FOO_PACKAGE_INSIDE_CLASS = "class DrScalaTestFoo { package a }";
//  private static final String FOO_PACKAGE_AS_FIELD = "class DrScalaTestFoo { var package: Int }";
//  private static final String FOO_PACKAGE_AS_FIELD_2 = "class DrScalaTestFoo { val package: Int = 5; }";
  private static final String BAR_MISSING_DECLARATION_KEYWORD_MULTIPLE_LINES =
    "class DrScalaTestBar {\n  val a = 5\n  x: Int\n }";
  protected static final String COMPILER_ERRORS_2872797_TEXT =
    "/**\n" +
    " * This is a simple class that really doesn't do anything.\n" +
    " * We'll use it to explore the kinds of error messages that\n" +
    " * the compiler will report when it encounters errors in \n" +
    " * Scala source code.\n" +
    " */\n" +
    "class CompilerErrors {\n" +
    "\n" +
    "  /**\n" +
    "   * Some shared storage, an instance variable.\n" +
    "   */\n" +
    "  var shared = 5\n" +
    "  \n" +
    "  /**\n" +
    "   * a sample method. This method has no parameters\n" +
    "   * and no return value.\n" +
    "   */\n" +
    "  def sampleMethod() {\n" +
    "    val x = 20;\n" +
    "    println(\"This is sampleMethod. x is \" + x)\n" +
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
    */
  public void testCompileAllFailsDifferentSourceRoots() throws BadLocationException, IOException, InterruptedException {
    debug.logStart();
    
//    System.err.println("Starting testCompileAllFailsDifferentSourceRoots");
    
    File aDir = new File(_tempDir, "a");
    File bDir = new File(_tempDir, "b");
    aDir.mkdir();
    bDir.mkdir();
    
    OpenDefinitionsDocument doc1 = setupDocument(FOO_MISSING_VAR_KEYWORD);
    final File file1 = new File(aDir, "DrScalaTestFoo.scala");
    saveFile(doc1, new FileSelector(file1));  // runs synchronously in event thread
    
    OpenDefinitionsDocument doc2 = setupDocument(BAR_MISSING_DECLARATION_KEYWORD);
    final File file2 = new File(bDir, "DrScalaTestBar.scala");
    saveFile(doc2, new FileSelector(file2));  // runs synchronously in event thread
    
    CompileShouldFailListener listener = new CompileShouldFailListener();
    
    _model.addListener(listener);
    
    CompilerModel cm = _model.getCompilerModel();    
    cm.compileAll();
    listener.waitCompileDone();
    
//    System.err.println("Number of errors = " + cm.getNumErrors());
    
    assertCompileErrorsPresent(_name(), true);
//    System.err.println(cm.getCompilerErrorModel());
    assertEquals("Should have 1 compiler error", 1, cm.getNumErrors());
    listener.checkCompileOccurred();
    
    // Make sure .class does not exist for both files
    File compiled1 = classForScala(file1, "DrScalaTestFoo");
    assertEquals(_name() + "Class file exists after failing compile (1)", false, compiled1.exists());
    File compiled2 = classForScala(file2, "DrScalaTestBar");
    assertEquals(_name() + "Class file exists after failing compile (2)", false, compiled2.exists());
    _model.removeListener(listener);
    
//    System.err.println("testCompileAllFailsDifferentSourceRoots completed");
    
    debug.logEnd();
  }
  
  /** Creates a source file with "package" as a field name and ensures that compile starts but fails due to 
    * the invalid field name.
    */
  public void testCompilePackageAsField() throws BadLocationException, IOException, InterruptedException {
    debug.logStart();
    
//    System.err.println("Starting testCompilePackageAsField");
    
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
    
    File compiled = classForScala(file, "DrScalaTestFoo");
    assertEquals(_name() + "Class file exists after failing compile", false, compiled.exists());
    _model.removeListener(listener);
    
//    System.err.println("testCompilePackageAsField completed");
    
    debug.logEnd();
  }
  
  /** Creates a source file with "package" as a field name and ensures that compile starts but fails due to the
    * invalid field name. This is different from {@link #testCompilePackageAsField} as it initializes the field. 
    */
  public void testCompilePackageAsField2() throws BadLocationException, IOException, InterruptedException {
    debug.logStart();
    
//    System.err.println("Starting testCompilePackageAsField2");
    
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
    
    File compiled = classForScala(file, "DrScalaTestFoo");
    assertEquals(_name() + "Class file exists after failing compile", false, compiled.exists());
    _model.removeListener(listener);
    
    _log.log("testCompilePackageAsField2 completed");
    
    debug.logEnd();
  }
  
  /** Tests compiling an invalid file and checks to make sure the class file was not created.  */
  public void testCompileMissingCloseCurly() throws BadLocationException, IOException, InterruptedException {
    debug.logStart();
    
    _log.log("Starting testCompileMissingCloseCurly");
    
    final OpenDefinitionsDocument doc = setupDocument(FOO_MISSING_VAR_KEYWORD);
    final File file = tempFile();
    saveFile(doc, new FileSelector(file));
    
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    
    testStartCompile(doc);
    
    listener.waitCompileDone();
    assertCompileErrorsPresent(_name(), true);
    listener.checkCompileOccurred();
    
    File compiled = classForScala(file, "DrScalaTestFoo");
    assertTrue(_name() + "Class file exists after compile?!", ! compiled.exists());
    _model.removeListener(listener);
    
    _log.log("testCompileMissingCloseCurly completed");
    
    debug.logEnd();
  }
  
  /** Puts an otherwise valid package statement inside a class declaration. This better not work! */
  public void testCompileWithPackageStatementInsideClass() throws BadLocationException, IOException, 
    InterruptedException {
    debug.logStart();
    
    _log.log("Starting testCompileWithPackageStatementInsideClass");
    
    // Create temp file
    File baseTempDir = tempDirectory();
    File subdir = new File(baseTempDir, "a");
    File fooFile = new File(subdir, "DrScalaTestFoo.scala");
    File compiled = classForScala(fooFile, "DrScalaTestFoo");
    
    // Now make subdirectory a
    subdir.mkdir();
    
    // Save the footext to DrScalaTestFoo.scala in the subdirectory
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
    
    _log.log("testCompileWithPackageStatementInsideClass completed");
    
    debug.logEnd();
  }
  
  
  
  /** Tests the compiler errors have the correct line numbers.
    * TODO: rewrite this test for the new error model interface
    */
  public void testCompileFailsCorrectLineNumbers() throws BadLocationException, IOException, InterruptedException {
    debug.logStart();
    
    _log.log("Starting testCompileFailsCorrectLineNumbers");
//    System.err.println("Starting testCompileFailsCorrectLineNumbers");
    
    File aDir = new File(_tempDir, "a");
    // bDir has been eleted; it is a second source root
//    File bDir = new File(_tempDir, "b");
    aDir.mkdir();
//    Dir.mkdir();
    
    OpenDefinitionsDocument doc = setupDocument(BAR_MISSING_DECLARATION_KEYWORD_MULTIPLE_LINES);
    final File file = new File(aDir, "DrScalaTestBar.scala");
    saveFile(doc, new FileSelector(file));
    
    OpenDefinitionsDocument doc2 = setupDocument(FOO_MISSING_VAR_KEYWORD);
    final File file2 = new File(aDir, "DrScalaTestFoo.scala");
    saveFile(doc2, new FileSelector(file2));
    
    // do compile -- should fail since package decl is not valid!  Note: doc precedes doc2 alphabetically
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
//    assertTrue("location of first error should be between 20 and 29 inclusive (line 2), but was " + p1.getOffset(),
//               p1.getOffset() <= 20 && p1.getOffset() <= 29);
//    assertTrue("location of error should be after 34 (line 3 or 4)", p2.getOffset() >= 34);
    
    _log.log("testCompileFailsCorrectLineNumbers completed");
//    System.err.println("testCompileFailsCorrectLineNumbers completed");
    
    debug.logEnd();
  }
  
  /** Tests compiling an invalid file and checks to make sure the class file was not created.  */
  public void testCompileEndWhileParsing() throws BadLocationException, IOException, InterruptedException {
    debug.logStart();
    
    _log.log("Starting testCompileEndWhileParsing");
    
    final OpenDefinitionsDocument doc = setupDocument(COMPILER_ERRORS_2872797_TEXT);
    final File dir = tempDirectory();
    final File file = new File(dir, "CompilerErrors.scala");
    saveFile(doc, new FileSelector(file));
    
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    
    testStartCompile(doc);
    
    listener.waitCompileDone();
    assertCompileErrorsPresent(_name(), true);
    listener.checkCompileOccurred();
    
    File compiled = classForScala(file, "CompilerErrors");
    assertTrue(_name() + "Class file exists after compile?!", ! compiled.exists());
    _model.removeListener(listener);

    file.delete();
    _log.log("testCompileEndWhileParsing completed");
    debug.logEnd();
  }
}
