/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.model.print;

import edu.rice.cs.drjava.DrJavaTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

/**
 * Test functions of DrJavaBook
 *
 */
public final class DrJavaBookTest extends DrJavaTestCase {
  /** The DrJavaBook instance we will be testing. */
  private DrJavaBook book = null;
  
  /** Standard constructor.
    * @param name name of this DrJavaBook test
    */
  public DrJavaBookTest(String name) { super(name); }
  
  /** Creates a test suite for JUnit to run.
    * @return a test suite based on the methods in this class
    */
  public static Test suite() { return  new TestSuite(DrJavaBookTest.class); }
  
  public void setUp() throws Exception {
    super.setUp();
    book = new DrJavaBook("import java.io.*;", "simple_file.java", new PageFormat());
  }
  
  public void tearDown() throws Exception {
    book = null;
    super.tearDown();
  }
  
  public void testGetNumberOfPages() {
    assertEquals("testGetNumberOfPages:", Integer.valueOf(1), Integer.valueOf(book.getNumberOfPages()));
  }
  
  public void testGetPageFormat() {
    assertEquals("testGetPageFormat:", PageFormat.PORTRAIT, book.getPageFormat(0).getOrientation());
  }
  
  public void testGetPrintable() { 
    Graphics g = (new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)).getGraphics();
    Printable p = book.getPrintable(0);
    try { 
      assertEquals("testGetPrintable:", Integer.valueOf(Printable.PAGE_EXISTS), 
                   Integer.valueOf(p.print(g, new PageFormat(), 0)));
    }
    catch(Exception e) { fail("testGetPrintable: Unexpected exception!\n" + e); }
    
    try {
      p = book.getPrintable(99);
      fail("previous operation should throw an IndexOutOfBoundsException");
    }
    catch(IndexOutOfBoundsException e) {
      /* test succeeded */
    }
  }
}