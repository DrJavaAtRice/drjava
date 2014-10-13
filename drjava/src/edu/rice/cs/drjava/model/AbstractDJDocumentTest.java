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

import javax.swing.text.BadLocationException;
import edu.rice.cs.drjava.model.definitions.indent.IndentRulesTestCase;

/** This class contains tests for AbstractDJDocument.
  */
public class AbstractDJDocumentTest extends IndentRulesTestCase {
  
  public void testIsNum() {
    assertTrue(AbstractDJDocument._isNum("0"));
    assertTrue(AbstractDJDocument._isNum("1"));
    assertTrue(AbstractDJDocument._isNum("12"));
    
    assertTrue(AbstractDJDocument._isNum("0l"));
    assertTrue(AbstractDJDocument._isNum("1l"));
    assertTrue(AbstractDJDocument._isNum("12l"));
    assertTrue(AbstractDJDocument._isNum("0L"));
    assertTrue(AbstractDJDocument._isNum("1L"));
    assertTrue(AbstractDJDocument._isNum("12L"));
    
    assertTrue(AbstractDJDocument._isNum("00"));
    assertTrue(AbstractDJDocument._isNum("01"));
    assertTrue(AbstractDJDocument._isNum("012"));
    assertTrue(AbstractDJDocument._isNum("00l"));
    assertTrue(AbstractDJDocument._isNum("01l"));
    assertTrue(AbstractDJDocument._isNum("012l"));
    assertTrue(AbstractDJDocument._isNum("00L"));
    assertTrue(AbstractDJDocument._isNum("01L"));
    assertTrue(AbstractDJDocument._isNum("012L"));
    
    assertTrue(AbstractDJDocument._isNum("0X0"));
    assertTrue(AbstractDJDocument._isNum("0X1"));
    assertTrue(AbstractDJDocument._isNum("0X12"));
    assertTrue(AbstractDJDocument._isNum("0Xff"));
    assertTrue(AbstractDJDocument._isNum("0XFF"));
    assertTrue(AbstractDJDocument._isNum("0XFFFFFFFF"));
    assertFalse(AbstractDJDocument._isNum("0XFFFFFFFFF"));
    assertFalse(AbstractDJDocument._isNum("0Xg"));
    assertTrue(AbstractDJDocument._isNum("0X0l"));
    assertTrue(AbstractDJDocument._isNum("0X1l"));
    assertTrue(AbstractDJDocument._isNum("0X12l"));
    assertTrue(AbstractDJDocument._isNum("0Xffl"));
    assertTrue(AbstractDJDocument._isNum("0XFFl"));
    assertTrue(AbstractDJDocument._isNum("0XFFFFFFFFFFFFFFFFl"));
    assertFalse(AbstractDJDocument._isNum("0XFFFFFFFFFFFFFFFFFl"));
    assertFalse(AbstractDJDocument._isNum("0Xgl"));
    assertTrue(AbstractDJDocument._isNum("0X0L"));
    assertTrue(AbstractDJDocument._isNum("0X1L"));
    assertTrue(AbstractDJDocument._isNum("0X12L"));
    assertTrue(AbstractDJDocument._isNum("0XffL"));
    assertTrue(AbstractDJDocument._isNum("0XFFL"));
    assertFalse(AbstractDJDocument._isNum("0XgL"));
    assertTrue(AbstractDJDocument._isNum("0XFFFFFFFFFFFFFFFFL"));
    assertFalse(AbstractDJDocument._isNum("0XFFFFFFFFFFFFFFFFFL"));
    
    assertTrue(AbstractDJDocument._isNum("0x0"));
    assertTrue(AbstractDJDocument._isNum("0x1"));
    assertTrue(AbstractDJDocument._isNum("0x12"));
    assertTrue(AbstractDJDocument._isNum("0xff"));
    assertTrue(AbstractDJDocument._isNum("0xFF"));
    assertTrue(AbstractDJDocument._isNum("0xFFFFFFFF"));
    assertFalse(AbstractDJDocument._isNum("0xFFFFFFFFF"));
    assertFalse(AbstractDJDocument._isNum("0xg"));
    assertTrue(AbstractDJDocument._isNum("0x0l"));
    assertTrue(AbstractDJDocument._isNum("0x1l"));
    assertTrue(AbstractDJDocument._isNum("0x12l"));
    assertTrue(AbstractDJDocument._isNum("0xffl"));
    assertTrue(AbstractDJDocument._isNum("0xFFl"));
    assertTrue(AbstractDJDocument._isNum("0xFFFFFFFFFFFFFFFFl"));
    assertFalse(AbstractDJDocument._isNum("0xFFFFFFFFFFFFFFFFFl"));
    assertFalse(AbstractDJDocument._isNum("0xgl"));
    assertTrue(AbstractDJDocument._isNum("0x0L"));
    assertTrue(AbstractDJDocument._isNum("0x1L"));
    assertTrue(AbstractDJDocument._isNum("0x12L"));
    assertTrue(AbstractDJDocument._isNum("0xffL"));
    assertTrue(AbstractDJDocument._isNum("0xFFL"));
    assertFalse(AbstractDJDocument._isNum("0xgL"));
    assertTrue(AbstractDJDocument._isNum("0xFFFFFFFFFFFFFFFFL"));
    assertFalse(AbstractDJDocument._isNum("0xFFFFFFFFFFFFFFFFFL"));
    
    assertTrue(AbstractDJDocument._isNum("1.0"));
    assertTrue(AbstractDJDocument._isNum("12.0"));
    assertTrue(AbstractDJDocument._isNum("12.3"));
    assertTrue(AbstractDJDocument._isNum("12.34"));
    
    assertTrue(AbstractDJDocument._isNum("1.0f"));
    assertTrue(AbstractDJDocument._isNum("12.0f"));
    assertTrue(AbstractDJDocument._isNum("12.3f"));
    assertTrue(AbstractDJDocument._isNum("12.34f"));
    assertTrue(AbstractDJDocument._isNum("1.0F"));
    assertTrue(AbstractDJDocument._isNum("12.0F"));
    assertTrue(AbstractDJDocument._isNum("12.3F"));
    assertTrue(AbstractDJDocument._isNum("12.34F"));
    
    assertTrue(AbstractDJDocument._isNum("1.0d"));
    assertTrue(AbstractDJDocument._isNum("12.0d"));
    assertTrue(AbstractDJDocument._isNum("12.3d"));
    assertTrue(AbstractDJDocument._isNum("12.34d"));
    assertTrue(AbstractDJDocument._isNum("1.0D"));
    assertTrue(AbstractDJDocument._isNum("12.0D"));
    assertTrue(AbstractDJDocument._isNum("12.3D"));
    assertTrue(AbstractDJDocument._isNum("12.34D"));
    
    assertTrue(AbstractDJDocument._isNum("1.0e2"));
    assertTrue(AbstractDJDocument._isNum("12.0e2"));
    assertTrue(AbstractDJDocument._isNum("12.3e2"));
    assertTrue(AbstractDJDocument._isNum("12.34e2"));
    
    assertTrue(AbstractDJDocument._isNum("1.0e2f"));
    assertTrue(AbstractDJDocument._isNum("12.0e2f"));
    assertTrue(AbstractDJDocument._isNum("12.3e2f"));
    assertTrue(AbstractDJDocument._isNum("12.34e2f"));
    assertTrue(AbstractDJDocument._isNum("1.0e2F"));
    assertTrue(AbstractDJDocument._isNum("12.0e2F"));
    assertTrue(AbstractDJDocument._isNum("12.3e2F"));
    assertTrue(AbstractDJDocument._isNum("12.34e2F"));
    
    assertTrue(AbstractDJDocument._isNum("1.0e2d"));
    assertTrue(AbstractDJDocument._isNum("12.0e2d"));
    assertTrue(AbstractDJDocument._isNum("12.3e2d"));
    assertTrue(AbstractDJDocument._isNum("12.34e2d"));
    assertTrue(AbstractDJDocument._isNum("1.0e2D"));
    assertTrue(AbstractDJDocument._isNum("12.0e2D"));
    assertTrue(AbstractDJDocument._isNum("12.3e2D"));
    assertTrue(AbstractDJDocument._isNum("12.34e2D"));
  }
  
  public void testInBlockComment() throws BadLocationException {
    String text = 
      "/* This is a block comment */\n" +
      "    //comment\n" +  
      "    /*commment\n" +
      "     *again;{}\n" +
      "     */\n" +
      "     foo();\n";
    _setDocText(text);
    
//    System.err.println("text = \n" + _doc.getText());
    
    // Looking at "/* This is a block comment ...
    assertFalse("position of opening of block is not 'in'",  _doc._insideBlockComment(0));
    assertFalse("position following '/' at opening is not 'in'", _doc._insideBlockComment(1));
    assertTrue("position following '/*' at opening is 'in'", _doc._insideBlockComment(2));
    assertTrue("position just after 'comment' is 'in'",  _doc._insideBlockComment(26));
    assertTrue("position just before closing '*/' is 'in'",  _doc._insideBlockComment(27));
    assertTrue("position just before closing '/' is 'in'",  _doc._insideBlockComment(28));
    assertFalse("position just after closing '/' is not 'in'", _doc._insideBlockComment(29));

    // Looking at "    //comment ...
    assertFalse("position following block comment is not 'in'",   _doc._insideBlockComment(30));
    assertFalse("position at beginning of next line is not 'in'",  _doc._insideBlockComment(31));
    
    // Looking at "    /*comment ...
    assertTrue("position preceding block is not 'in'",  ! _doc._insideBlockComment(44));
    assertTrue("position immediately before '/*' not 'in'",  ! _doc._insideBlockComment(48));
    assertTrue("position following '/' is not 'in'",  ! _doc._insideBlockComment(49));
    assertTrue("position following '/*' is 'in'",  _doc._insideBlockComment(50));
  }   
  
  public void testIsPrevLineComment() throws BadLocationException {
    String text = 
      "    bump().\n" +  
      "    //comment\n" +  
      "    /*commment\n" +
      "     *again;{}\n" +
      "     */\n" +
      "     foo();\n";
    _setDocText(text);
    
//    System.err.println("text = \n" + text);
    
//    _doc.setCurrentLocation(13);  // Looking at "    // comment ..."
    assertTrue("line preceding //comment line is NOT a pure line comment",  ! _doc.isPrevLineNewComment(13));
    assertTrue("line preceding //comment line is NOT ignorable", ! _doc.isPrevLineIgnorable(13));
    
//    _doc.setCurrentLocation(27);  // Looking at "    /*comment ..."
    assertTrue("line preceding /*comment line is a pure line comment", _doc.isPrevLineNewComment(27));
    assertTrue("line preceding /*comment line is ignorable", _doc.isPrevLineIgnorable(27));
    
//    _doc.setCurrentLocation(42);  // Looking at "     *again ..."
    assertTrue("line preceding *again line is a new comment line", _doc.isPrevLineNewComment(42));
    assertTrue("line preceding *again line is a not within a block comment", ! _doc.isPrevLineInBlockComment(42));
    assertTrue("line preceding *again line is ignorable", _doc.isPrevLineIgnorable(42));
    
    _doc.setCurrentLocation(57);  // Looking at "     *again ..."
    assertTrue("/* line preceding '*/' line is a NOT pure line comment", ! _doc.isPrevLineNewComment(57));
    assertTrue("line preceding *again line is a within a block comment", _doc.isPrevLineInBlockComment(57));
    assertTrue("/* line preceding '*/' line is ignorable", _doc.isPrevLineIgnorable(57));
  }
  
  public void testShadowingOfComments() throws BadLocationException {
    String text = 
      " //comment\n" +  
      " /*commment\n" +
      "  *again;{}\n" +
      "  */\n";
    _setDocText(text);
    
    assertFalse("before beginning of line comment", _doc.isWeaklyShadowed(0));
    assertTrue("weak shadowing of beginning of line comment", _doc.isWeaklyShadowed(1));
    assertFalse("shadowing of beginning of line comment", _doc.isShadowed(1));
    
    assertTrue("end of line comment", _doc.isWeaklyShadowed(9));
    assertTrue("end of block comment", _doc.isWeaklyShadowed(38));
    
    assertFalse("before beginning of block comment", _doc.isWeaklyShadowed(11));
    assertTrue("weak shadowing of beginning of block comment", _doc.isWeaklyShadowed(12));
    assertFalse("shadowing of beginning of block comment", _doc.isShadowed(12));
  }
}
