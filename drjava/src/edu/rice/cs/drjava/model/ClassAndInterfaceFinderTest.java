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

package edu.rice.cs.drjava.model;
import junit.framework.TestCase;
import java.io.*;

/**
 * ClassAndInterfaceFinderTest for unit testing ClassAndInterfaceFinder.  Uses
 * junit for testing.
 *
 * @author <a href="mailto:jasonbs@rice.edu">Jason Schiller</a>
 * @version $Id$
 */

public class ClassAndInterfaceFinderTest extends TestCase {
  
  
  /**
   * Tests to see if string input is properly parsed to obtain interface name.
   */
  public void testStringInterfaceRecognition(){
    try {
      Reader r = new StringReader("//\n /**/public Class Interface interface Aa.12_34 {}");
      ClassAndInterfaceFinder finder = new ClassAndInterfaceFinder(r);
      String s = finder.getClassOrInterfaceName();
      assertEquals("stringInterfaceRecognition","Aa.12_34", s);
    }
    catch (Exception e){
      fail("stringInterfaceRecognition threw "+e);
    }
  }
  
  
  /**
   * Tests to see if string input is properly parsed to reject interface name.
   */
  public void testStringInterfaceRejection(){
    try {
      Reader r = new StringReader("//\n /**/public Class Interface interface Aa.12_34 {}");
      ClassAndInterfaceFinder finder = new ClassAndInterfaceFinder(r);
      String s = finder.getClassName();
      assertEquals("stringInterfaceRejection","", s);
    }
    catch (Exception e) {
      fail("stringInterfaceRejection threw "+ e);
    }
  }
  
  
  /**
   * Tests to see if string input is properly parsed to obtain class name.
   */
  public void testStringClassRecognition(){
    try {
      Reader r = new StringReader("//\n /**/public Class Interface class Aa.12_34 {}");
      ClassAndInterfaceFinder finder = new ClassAndInterfaceFinder(r);
      String s = finder.getClassOrInterfaceName();
      assertEquals("stringNameRecognition","Aa.12_34", s);
    }
    catch (Exception e) {
      fail("stringClassRecognition threw " +e);
    }
  }
  
  /**
   * Tests to see if string input is properly parsed to insert package name.
   */
  public void testStringPackageRecognition(){
    try {
      Reader r = new StringReader("//\n /**/package x public interface Aa.12_34 {}");
      ClassAndInterfaceFinder finder = new ClassAndInterfaceFinder(r);
      String s = finder.getClassOrInterfaceName();
      assertEquals("stringNameRecognition","x.Aa.12_34", s);
    }
    catch (Exception e) {
      fail("stringPackageRecognition threw " + e);
    }
  }
  
  
}
