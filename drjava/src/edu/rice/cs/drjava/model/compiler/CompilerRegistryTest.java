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

package edu.rice.cs.drjava.model.compiler;

import java.io.*;
import junit.framework.*;
import edu.rice.cs.util.classloader.LimitingClassLoader;
import edu.rice.cs.drjava.DrJava;

/**
 * Test cases for {@link CompilerRegistry}.
 * Here we test that the compiler registry correctly finds
 * available compilers.
 *
 * @version $Id$
 */
public final class CompilerRegistryTest extends TestCase {
  private static final CompilerRegistry _registry = CompilerRegistry.ONLY;
  private static final String[][] _defaultCompilers
    = CompilerRegistry.DEFAULT_COMPILERS;

  private static final CompilerInterface[] _allAvailableCompilers
    = _registry.getAvailableCompilers();

  /**
   * Stores the old state of {@link CompilerRegistry#getBaseClassLoader},
   * so it can be reset later.
   */
  private ClassLoader _oldBaseLoader;

  /**
   * Constructor.
   * @param  String name
   */
  public CompilerRegistryTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return new TestSuite(CompilerRegistryTest.class);
  }

  /** Test setup method, which saves the old base class loader. */
  public void setUp() {
    _oldBaseLoader = _registry.getBaseClassLoader();
    _registry.setActiveCompiler(NoCompilerAvailable.ONLY);
  }

  /** Test teardown method, which restores the old base class loader. */
  public void tearDown() {
    _registry.setBaseClassLoader(_oldBaseLoader);
  }

  /**
   * Test that the default compilers are available and what we expect.
   * This requires the environment (CLASSPATH) to have these compilers
   * available. This is OK though, since the build environment needs them!
   *
   * This test is now commented out because it put too must restriction
   * on the developer's build environment. It required them to have all the
   * compilers available, which they may not. Oh well. These matters
   * of configuration are really hard to test nicely.
   */
  /*
  public void testExpectedDefaultCompilers() {
    CompilerInterface[] compilers = _registry.getAvailableCompilers();

    assertEquals("Number of available compilers vs. number of default compilers",
                 _defaultCompilers.length,
                 compilers.length);

    for (int i = 0; i < compilers.length; i++) {
      assertEquals("Name of available compiler #" + i + " is the same as the " +
                     "name of the corresponding default compiler",
                   _defaultCompilers[i],
                   compilers[i].getClass().getName());
    }
  }
  */

  /**
   * Tests that list of available compilers effectively is restricted
   * when the class is not available.
   * Here this is done by limiting the available compilers one at a time.
   */
  public void testLimitOneByOne() {
    for (int i = 0; i < _allAvailableCompilers.length; i++) {
      //CompilerInterface[] compilers = 
      _getCompilersAfterDisablingOne(i);
      // That method includes all the tests we need!
    }
  }

  /**
   * Tests that list of available compilers effectively is restricted
   * when all default compilers are not available.
   */
  public void testLimitAllAtOnce() {
    LimitingClassLoader loader = new LimitingClassLoader(_oldBaseLoader);
    _registry.setBaseClassLoader(loader);

    for (int i = 0; i < _defaultCompilers.length; i++) {
      for (int j = 0; j < _defaultCompilers[i].length; j++) {
        loader.addToRestrictedList(_defaultCompilers[i][j]);
      }
    }

    CompilerInterface[] compilers = _registry.getAvailableCompilers();
    assertEquals("Number of available compilers should be 1 " +
                   "because all real compilers are restricted.",
                 1,
                 compilers.length);

    assertEquals("Only available compiler should be NoCompilerAvailable.ONLY",
                 NoCompilerAvailable.ONLY,
                 compilers[0]);

    assertEquals("Active compiler",
                 NoCompilerAvailable.ONLY,
                 _registry.getActiveCompiler());
    
    assertEquals("DrJava.java should not see an available compiler",
                 false,
                 DrJava.hasAvailableCompiler());
  }
  
  /**
   * Tests that DrJava.java can see whether CompilerRegistry has an
   * available compiler.
   */
  public void testAvailableCompilerSeenByDrJava() {
    assertEquals("DrJava.java should agree with CompilerRegistry",
                 _registry.getActiveCompiler() != NoCompilerAvailable.ONLY,
                 DrJava.hasAvailableCompiler());
  }

  /**
   * Tests that {@link CompilerRegistry#setActiveCompiler} and
   * {@link CompilerRegistry#getActiveCompiler} work.
   */
  public void testActiveCompilerAllAvailable() {
    CompilerInterface[] compilers = _registry.getAvailableCompilers();

    assertEquals("active compiler before any setActive",
                 compilers[0],
                 _registry.getActiveCompiler());

    for (int i = 0; i < compilers.length; i++) {
      // TODO: deal with the problem that sometimes not all compilers avail!
      //if (compilers[i].isAvailable()) {
        _registry.setActiveCompiler(compilers[i]);
        assertEquals("active compiler after setActive",
                     compilers[i],
                     _registry.getActiveCompiler());
      //}
    }
  }

  /**
   * Returns the list of available compilers after disabling one of them.
   * This method includes checks for the correctness of the list
   * after disabling one.
   *
   * @param i Index of default compiler to disable.
   */
  private CompilerInterface[] _getCompilersAfterDisablingOne(int i) {
    return _getCompilersAfterDisablingSome(new int[] { i });
  }

  /**
   * Returns the list of available compilers after disabling some of them.
   * This method includes checks for the correctness of the list
   * after disabling them.
   *
   * @param indices Array of ints signifying which of the default compilers
   *                to disable.
   */
  private CompilerInterface[] _getCompilersAfterDisablingSome(int[] indices) {
    LimitingClassLoader loader = new LimitingClassLoader(_oldBaseLoader);
    _registry.setBaseClassLoader(loader);
    //for (int j = 0; j < _allAvailableCompilers.length; j++) {
    //  System.out.println("all available compilers: " + _allAvailableCompilers[j].getClass().getName());
    //}

    for (int i = 0; i < indices.length; i++) {
      //System.out.println("restricting compiler: " + _allAvailableCompilers[indices[i]].getClass().getName());
      loader.addToRestrictedList(_allAvailableCompilers[indices[i]].getClass().getName());
    }

    CompilerInterface[] compilers = _registry.getAvailableCompilers();
    //for (int j = 0; j < compilers.length; j++) {
    //  System.out.println("available compiler: " + compilers[j].getClass().getName());
    //}
    
    // NOTE: 03.28.2004 We don't know how to check this since making the change 
    // to only display one compiler of each type.  JH & NH
//    assertEquals("Number of available compilers",
//                 _allAvailableCompilers.length - indices.length,
//                 compilers.length);

    int indicesIndex = 0;

    for (int j = 0; j < _allAvailableCompilers.length; j++) {
      if ((indicesIndex < indices.length) && (j == indices[indicesIndex])) {
        // this is an index to skip.
        indicesIndex++;
        continue;
      }

      // Now indicesIndex is at the number of indices to skip!
      int indexInAvailable = j - indicesIndex;

      assertEquals("Class of available compiler #" + indexInAvailable,
                   _allAvailableCompilers[j].getClass().getName(),
                   compilers[indexInAvailable].getClass().getName());
    }

    return compilers;
  }

  /**
   * Ensure that the active compiler in the registry cannot be set to null.
   */
  public void testCannotSetCompilerToNull() {
    try {
      _registry.setActiveCompiler(null);
      fail("Setting active compiler to null should have caused an exception!");
    }
    catch (IllegalArgumentException e) {
      // Good-- exception was thrown.
    }
  }

  static class Without implements CompilerInterface {
    public boolean testField = false;
    public Without()
    {
      testField = true;
    }

     public void addToBootClassPath(File s) {}
     public CompilerError[] compile(File[] sourceRoots, File[] files){ return null; }
     public CompilerError[] compile(File sourceRoot, File[] files) { return null; }
     public String getName() { return "Without"; }
     public boolean isAvailable() { return false; }
     public void setAllowAssertions(boolean allow) {}
     public void setExtraClassPath(String extraClassPath) {}
     public String toString(){ return "Without"; }
  }

  /**
   * Test that createCompiler() does successfully instantiate
   * compilers that do not have the ONLY static field, and those which
   * do have it.
   */
   public void testCreateCompiler(){
     try{
       _registry.createCompiler(Without.class);
     }
     catch(Throwable e){
       e.printStackTrace();
       fail("testCreateCompiler: Unexpected Exception for class without ONLY field\n" + e);
     }

     try{
       _registry.createCompiler(JavacFromClasspath.ONLY.getClass());
     }
     catch(Throwable e2){

        fail("testCreateCompiler: Unexpected Exception for class with ONLY field\n" + e2);
     }
  }
}
