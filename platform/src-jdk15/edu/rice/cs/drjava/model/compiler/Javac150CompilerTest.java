/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

package edu.rice.cs.drjava.model.compiler;

import java.util.List;
import java.util.LinkedList;
import java.io.File;
import java.util.Collections;
import edu.rice.cs.plt.reflect.JavaVersion;
import junit.framework.TestCase;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

public class Javac150CompilerTest extends TestCase {

  public void testCompile() {
    Javac150Compiler c = new Javac150Compiler(JavaVersion.CURRENT_FULL, "", null);
    assertTrue(c.isAvailable());
    assertCompileSuccess(c, "testFiles/IterableTest.java");
  }
  
  private static void assertCompileSuccess(CompilerInterface c, String... files) {
    List<? extends CompilerError> errors = doCompile(c, files);
    debug.logValue("errors", errors);
    assertEquals(Collections.EMPTY_LIST, errors);
  }
  
  private static List<? extends CompilerError> doCompile(CompilerInterface c, String... files) {
    return c.compile(fileList(files), null, null, null, null, null, true);
  }
      
  private static List<File> fileList(String... files) {
    List<File> result = new LinkedList<File>();
    for (String s : files) { result.add(new File(s)); }
    return result;
  }
  
}
