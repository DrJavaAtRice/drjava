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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions;

import junit.framework.TestCase;

/** A JUnit test case class for PackageLexer. Every method starting with the word "test" will be called when running
  * the test with JUnit.
  */
public class PackageLexerTest extends TestCase {
  
  /** Test package extraction from some simple Java files. */
  public void testGetPackageName() {
    String text0 = "package foo1";
    String text1 = "/* This program text has an empty package header */ \n;";
    String text2 = "/* This program text has another form of empty package heaeder */ package;\n";
    String text3 = "/* This program text has a compound package name. */ \n" + 
                   "// followed by a wing comment \n" +
                   "package simple.compound.name$ \n" +
                   "more junk \n";
    String text4 = "/* This program text has a compound package description. */ \n" + 
                   "package simple package compound package _name gibberish \n";
    
    String text5 = "/* This program text has an ill-formed package name. */ \n" + 
                   "package simple.compound.package.\n";
    String text6 = "/* This program text has an ill-formed package name. */ \n" + 
                   "package .simple.compound.package\n";
    String text7 = "/* This program text has an ill-formed package name. */ \n" + 
                   "package ..\n";
    String text8 = "/* This program text has an ill-formed package name. */ \n" + 
                   "package foo..bar;\n"; 
    
    assertEquals("text0 test", "foo1", new PackageLexer(text0).getPackageName());
    assertEquals("text1 test", "", new PackageLexer(text1).getPackageName());
    assertEquals("text2 test", "", new PackageLexer(text2).getPackageName());
    assertEquals("text3 test", "simple.compound.name$", new PackageLexer(text3).getPackageName());
    assertEquals("text4 test", "simple.compound._name", new PackageLexer(text4).getPackageName());
    
    assertEquals("text5 test", "", new PackageLexer(text5).getPackageName());
    assertEquals("text6 test", "", new PackageLexer(text6).getPackageName());
    assertEquals("text7 test", "", new PackageLexer(text7).getPackageName());
    assertEquals("text8 test", "", new PackageLexer(text7).getPackageName());   
  }
  
}
