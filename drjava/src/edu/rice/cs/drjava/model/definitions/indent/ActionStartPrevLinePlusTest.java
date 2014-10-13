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

package edu.rice.cs.drjava.model.definitions.indent;

/** Tests ActionStartPrevLinePlus(String)
  * @version $Id: ActionStartPrevLinePlusTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class ActionStartPrevLinePlusTest extends IndentRulesTestCase {
  
  /** This is a clever (IMHO) factory trick to reuse these methods in TestCases for logically similar IndentActions.
    * @param suffix the text to be added by this rule after indent padding
    * @see ActionStartPrevLinePlus#ActionStartPrevLinePlus(String)
    */
  private IndentRuleAction makeAction(String suffix) { return new ActionStartPrevLinePlus(suffix); }

  public void testLeaveBe() throws javax.swing.text.BadLocationException {
    _setDocText("foo\nbar");
    _doc.setCurrentLocation(4);
    makeAction("").testIndentLine(_doc, Indenter.IndentReason.OTHER);
    assertEquals(7, _doc.getLength());
    assertEquals("foo\nbar", _doc.getText());
  }
  public void testLeaveBeMidLine() throws javax.swing.text.BadLocationException {
    _setDocText("foo\nbar");
    _doc.setCurrentLocation(6);
    makeAction("").testIndentLine(_doc, Indenter.IndentReason.OTHER);
    assertEquals(7, _doc.getLength());
    assertEquals("foo\nbar", _doc.getText());
  }
  public void testAddSpaces() throws javax.swing.text.BadLocationException {
    _setDocText("foo\nbar");
    _doc.setCurrentLocation(4);
    makeAction("   ").testIndentLine(_doc, Indenter.IndentReason.OTHER);  // three spaces
    assertEquals(10, _doc.getLength());
    assertEquals("foo\n   bar", _doc.getText());
  }
  public void testAddSpacesMidLine() throws javax.swing.text.BadLocationException {
    _setDocText("foo\nbar");
    _doc.setCurrentLocation(6);
    makeAction("   ").testIndentLine(_doc, Indenter.IndentReason.OTHER);  // three spaces
    assertEquals(10, _doc.getLength());
    assertEquals("foo\n   bar", _doc.getText());
  }
  public void testBothIndented() throws javax.swing.text.BadLocationException {
    _setDocText("  foo\n  bar");
    _doc.setCurrentLocation(9);
    makeAction("").testIndentLine(_doc, Indenter.IndentReason.OTHER);
    assertEquals(11, _doc.getLength());
    assertEquals("  foo\n  bar", _doc.getText());
  }
  public void testBothIndentedAddSpaces() throws javax.swing.text.BadLocationException {
    _setDocText("  foo\n  bar");
    _doc.setCurrentLocation(9);
    makeAction("   ").testIndentLine(_doc, Indenter.IndentReason.OTHER);
    assertEquals(14, _doc.getLength());
    assertEquals("  foo\n     bar", _doc.getText());
  }
  public void testBothIndentedAddStuff() throws javax.swing.text.BadLocationException {
    _setDocText("  foo\n  bar");
    _doc.setCurrentLocation(9);
    makeAction("abc").testIndentLine(_doc, Indenter.IndentReason.OTHER);
    assertEquals(14, _doc.getLength());
    assertEquals("  foo\n  abcbar", _doc.getText());
  }
  public void testSecondLineMisindented() throws javax.swing.text.BadLocationException {
    _setDocText("  foo\n bar");
    _doc.setCurrentLocation(9);
    makeAction("abc").testIndentLine(_doc, Indenter.IndentReason.OTHER);
    assertEquals(14, _doc.getLength());
    assertEquals("  foo\n  abcbar", _doc.getText());
  }
  public void testLeavesOtherLinesAlone() throws javax.swing.text.BadLocationException {
    _setDocText("foo\nbar\nblah");
    _doc.setCurrentLocation(10);
    makeAction("   ").testIndentLine(_doc, Indenter.IndentReason.OTHER);  // three spaces
    assertEquals(15, _doc.getLength());
    assertEquals("foo\nbar\n   blah", _doc.getText());
  }
  public void testOtherLinesIndented() throws javax.swing.text.BadLocationException {
    _setDocText(" foo\n  bar\n   blah");
    _doc.setCurrentLocation(15);
    makeAction("   ").testIndentLine(_doc, Indenter.IndentReason.OTHER);  // three spaces
    assertEquals(20, _doc.getLength());
    assertEquals(" foo\n  bar\n     blah", _doc.getText());
  }
}
