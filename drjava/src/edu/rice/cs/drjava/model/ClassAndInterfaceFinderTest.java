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
package edu.rice.cs.drjava.model;
import edu.rice.cs.drjava.DrJavaTestCase;

import java.io.Reader;
import java.io.StringReader;

/**
 * ClassAndInterfaceFinderTest for unit testing ClassAndInterfaceFinder.  Uses
 * junit for testing.
 *
 * @author <a href="mailto:jasonbs@rice.edu">Jason Schiller</a>
 * @version $Id: ClassAndInterfaceFinderTest.java 5727 2012-09-30 03:58:32Z rcartwright $
 */

public class ClassAndInterfaceFinderTest extends DrJavaTestCase {
  
  
  /** Tests to see if string input is properly parsed to obtain trait name.
   */
  public void testStringTraitRecognition() {
    try {
      Reader r = new StringReader("//\n /**/Class Trait trait Aa.12_34 {}");
      ClassAndInterfaceFinder finder = new ClassAndInterfaceFinder(r);
      String s = finder.getClassObjectOrTraitName();
      assertEquals("stringInterfaceRecognition","Aa.12_34", s);
    }
    catch (Exception e) {
      fail("stringInterfaceRecognition threw " + e);
    }
  }
  
  
  /** Tests to see if string input is properly parsed to reject trait name.
   */
  public void testStringInterfaceRejection() {
    try {
      Reader r = new StringReader("//\n /**/Class Trait trait Aa.12_34 {}");
      ClassAndInterfaceFinder finder = new ClassAndInterfaceFinder(r);
      String s = finder.getClassName();
      assertEquals("stringInterfaceRejection","", s);
    }
    catch (Exception e) {
      fail("stringInterfaceRejection threw " +  e);
    }
  }
  
  
  /** Tests to see if string input is properly parsed to obtain class name.
   */
  public void testStringClassRecognition() {
    try {
      Reader r = new StringReader("//\n /**/Class Trait class Aa.12_34 {}");
      ClassAndInterfaceFinder finder = new ClassAndInterfaceFinder(r);
      String s = finder.getClassObjectOrTraitName();
      assertEquals("stringNameRecognition","Aa.12_34", s);
    }
    catch (Exception e) {
      fail("stringClassRecognition threw " +e);
    }
  }
  
  /** Tests to see if string input is properly parsed to insert package name.
   */
  public void testStringPackageRecognition() {
    try {
      Reader r = new StringReader("//\n /**/package x trait Aa.12_34 {}");
      ClassAndInterfaceFinder finder = new ClassAndInterfaceFinder(r);
      String s = finder.getClassObjectOrTraitName();
      assertEquals("stringNameRecognition","x.Aa.12_34", s);
    }
    catch (Exception e) {
      fail("stringPackageRecognition threw " + e);
    }
  } 
}
