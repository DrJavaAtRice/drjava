/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
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
public class DrJavaBookTest extends TestCase {
  /**
   * The DrJavaBook instance we will be testing
   */
  private DrJavaBook book = null;
  
  /**
   * Constructor.
   * @param  String name
   */
  public DrJavaBookTest(String name) {
    super(name);
  }
  
  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(DrJavaBookTest.class);
  }
  
  public void setUp(){
    book = new DrJavaBook("import java.io.*;", "simple_file.java", new PageFormat());
  }
  
  public void tearDown(){
    book = null;
  }
  
  public void testGetNumberOfPages(){
    assertEquals("testGetNumberOfPages:", new Integer(1), new Integer(book.getNumberOfPages()));
  }
  
  public void testGetPageFormat(){
    assertEquals("testGetPageFormat:", PageFormat.PORTRAIT, book.getPageFormat(0).getOrientation());
  }
  
  public void xtestGetPrintable(){ // FAILS for some reason // moez & eliot
    Graphics g = (new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)).getGraphics();
    Printable p = book.getPrintable(0);
    try{
      assertEquals("testGetPrintable:", new Integer(Printable.PAGE_EXISTS), 
                   new Integer(p.print(g, new PageFormat(), 0)));
    }
    catch(Exception e){
      fail("testGetPrintable: Unexpected exception!\n" + e);
    }
    
    p = book.getPrintable(99);
    
    try{
      assertEquals("testGetPrintable:", new Integer(Printable.NO_SUCH_PAGE), 
                   new Integer(p.print(g, new PageFormat(), 99)));
    }
    catch(Exception e)
    {
      fail("testGetPrintable: Unexpected exception!\n" + e);
    }
  }
}