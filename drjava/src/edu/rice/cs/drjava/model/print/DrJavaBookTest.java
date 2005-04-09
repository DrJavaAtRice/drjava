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

package edu.rice.cs.drjava.model.print;

import  java.awt.print.*;
import  java.awt.*;
import  java.awt.image.*;
import  junit.framework.*;
import  junit.extensions.*;

/**
 * Test functions of DrJavaBook
 *
 */
public final class DrJavaBookTest extends TestCase {
  /** The DrJavaBook instance we will be testing. */
  private DrJavaBook book = null;
  
  /** Standard constructor.
   * @param name name of this DrJavaBook test
   */
  public DrJavaBookTest(String name) { super(name); }
  
  /** Creates a test suite for JUnit to run.
   *  @return a test suite based on the methods in this class
   */
  public static Test suite() { return  new TestSuite(DrJavaBookTest.class); }
  
  public void setUp() { 
    book = new DrJavaBook("import java.io.*;", "simple_file.java", new PageFormat());
  }
  
  public void tearDown() { book = null; }
  
  public void testGetNumberOfPages() {
    assertEquals("testGetNumberOfPages:", new Integer(1), new Integer(book.getNumberOfPages()));
  }
  
  public void testGetPageFormat() {
    assertEquals("testGetPageFormat:", PageFormat.PORTRAIT, book.getPageFormat(0).getOrientation());
  }
  
  public void testGetPrintable() { 
    Graphics g = (new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)).getGraphics();
    Printable p = book.getPrintable(0);
    try { 
      assertEquals("testGetPrintable:", new Integer(Printable.PAGE_EXISTS), 
                   new Integer(p.print(g, new PageFormat(), 0)));
    }
    catch(Exception e){ fail("testGetPrintable: Unexpected exception!\n" + e); }
    
    try {
      p = book.getPrintable(99);
      fail("previous operation should throw an IndexOutOfBoundsException");
    }
    catch(IndexOutOfBoundsException e) {
      /* test succeeded */
    }
  }
}