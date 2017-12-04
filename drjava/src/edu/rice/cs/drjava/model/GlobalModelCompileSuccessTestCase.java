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

import edu.rice.cs.plt.reflect.JavaVersion;

/** * Tests to ensure that compilation succeeds when expected.
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
//    * interactions JVM.  This method is called once per test method, and it magically invokes the method.
//    */
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

  /** @return whether the currently active compiler supports generics. */
  protected boolean _isGenericCompiler() { return JavaVersion.CURRENT.supports(JavaVersion.JAVA_5); }
}
