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

package edu.rice.cs.drjava.config;

import edu.rice.cs.drjava.DrJavaTestCase;

import java.awt.*;

/** Class according to the JUnit protocol. Tests the proper functionality of the class ColorOption.
  * @version $Id: ColorOptionTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public final class ColorOptionTest extends DrJavaTestCase {
  
  /** @param name The name of this test case.*/
  public ColorOptionTest(String name) { super(name); }
  
  public void testGetName() {
    ColorOption io1 = new ColorOption("indent_size",null);
    ColorOption io2 = new ColorOption("max_files",null);
    
    assertEquals("indent_size", io1.getName());
    assertEquals("max_files",   io2.getName());
  }
  
  public void testParse() {
    ColorOption io = new ColorOption("max_files",null);
    
    assertEquals(Color.black, io.parse("0x000000"));
    assertEquals(Color.green, io.parse("0x00ff00"));
    
    try { io.parse("true"); fail(); }
    catch (OptionParseException e) { }
    
    try { io.parse("black"); fail(); }
    catch (OptionParseException e) { }
  }
  
 /** Test the format() method of ColorOption class. */
  public void testFormat() {
    ColorOption io1 = new ColorOption("max_files",null);
    ColorOption io2 = new ColorOption("indent_size",null);
    
    assertEquals("#000000", io1.format(Color.black));
    assertEquals("#ff00ff",  io2.format(Color.magenta));
    assertEquals("#ffffff", io1.format(Color.white));

    ColorOption c = new ColorOption("blue", Color.blue);
    assertEquals("testFormat:", "#000000", c.format(Color.black));
    assertEquals("testFormat:", "#0000ff", c.format(Color.blue));
    assertEquals("testFormat:", "#00ff00", c.format(Color.green));
    assertEquals("testFormat:", "#ff0000", c.format(Color.red));  
  }
}
