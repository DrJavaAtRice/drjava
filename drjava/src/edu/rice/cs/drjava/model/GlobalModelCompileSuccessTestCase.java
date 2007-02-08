/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2006 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
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

import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.plt.reflect.JavaVersion;

/**
 * Tests to ensure that compilation succeeds when expected.
 * 
 * Every test in this class is run for *each* of the compilers that is available.
 *
 * @version $Id$
 */
public abstract class GlobalModelCompileSuccessTestCase extends GlobalModelTestCase {

  protected static final String FOO_PACKAGE_AS_PART_OF_FIELD = "class DrJavaTestFoo { int cur_package = 5; }";
  protected static final String FOO2_EXTENDS_FOO_TEXT = "class DrJavaTestFoo2 extends DrJavaTestFoo {}";
  protected static final String FOO_NON_PUBLIC_CLASS_TEXT = "class DrJavaTestFoo {} class Foo{}";
  protected static final String FOO2_REFERENCES_NON_PUBLIC_CLASS_TEXT = "class DrJavaTestFoo2 extends Foo{}";
  protected static final String FOO_WITH_ASSERT = "class DrJavaTestFoo { void foo() { assert true; } }";
  protected static final String FOO_WITH_GENERICS = "class DrJavaTestFooGenerics<T> {}";

//  /** Overrides {@link TestCase#runBare} to interactively run this test case for each compiler, without resetting the 
//   *  interactions JVM.  This method is called once per test method, and it magically invokes the method.
//   */
//  public void runBare() throws Throwable {
//    CompilerInterface[] compilers = CompilerRegistry.ONLY.getAvailableCompilers();
//    for (int i = 0; i < compilers.length; i++) {
//      //System.out.println("Run " + i + ": " + compilers[i]);
//      setUp();
//      _model.getCompilerModel().setActiveCompiler(compilers[i]);
//
//      try { runTest(); }
//      finally { tearDown(); }
//    }
//  }

  protected String _name() { return "compiler=" + _model.getCompilerModel().getActiveCompiler().getName() + ": "; }

  /** Returns whether the currently active compiler supports generics. */
  protected boolean _isGenericCompiler() { return JavaVersion.CURRENT.supports(JavaVersion.JAVA_5); }
}
