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

/**
 * @version $Id$
 *
 * Tests ActionStartPrevLinePlus(String)
 * (see http://www.owlnet.rice.edu/~creis/comp312/indentrules2.html)
 */

package edu.rice.cs.drjava.model.definitions.indent;

import junit.framework.*;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;

public class ActionStartPrevLinePlusTest extends IndentRulesTestCase {
  private String _suffix;
  public ActionStartPrevLinePlusTest(String name) {
    super(name);
  }

  public void testDummy() {
  }

  /*
  public void testLeaveBe() throws javax.swing.text.BadLocationException {
    _setDocText("foo\nbar");
    _doc.setCurrentLocation(4);
    new ActionStartPrevLinePlus("").indentLine(_doc);
    assertEquals(7, _doc.getLength());
    assertEquals("foo\nbar", _doc.getText(0, 7));
  }
  public void testLeaveBeMidLine() throws javax.swing.text.BadLocationException {
    _setDocText("foo\nbar");
    _doc.setCurrentLocation(6);
    new ActionStartPrevLinePlus("").indentLine(_doc);
    assertEquals(7, _doc.getLength());
    assertEquals("foo\nbar", _doc.getText(0, 7));
  }
  public void testAddSpaces() throws javax.swing.text.BadLocationException {
    _setDocText("foo\nbar");
    _doc.setCurrentLocation(4);
    new ActionStartPrevLinePlus("   ").indentLine(_doc);  // three spaces
    assertEquals(10, _doc.getLength());
    assertEquals("foo\n   bar", _doc.getText(0, 10));
  }
  public void testAddSpacesMidLine() throws javax.swing.text.BadLocationException {
    _setDocText("foo\nbar");
    _doc.setCurrentLocation(6);
    new ActionStartPrevLinePlus("   ").indentLine(_doc);  // three spaces
    assertEquals(10, _doc.getLength());
    assertEquals("foo\n   bar", _doc.getText(0, 10));
  }
  public void testBothIndented() throws javax.swing.text.BadLocationException {
    _setDocText("  foo\n  bar");
    _doc.setCurrentLocation(9);
    new ActionStartPrevLinePlus("").indentLine(_doc);
    assertEquals(11, _doc.getLength());
    assertEquals("  foo\n  bar", _doc.getText(0, 11));
  }
  public void testBothIndentedAddSpaces() throws javax.swing.text.BadLocationException {
    _setDocText("  foo\n  bar");
    _doc.setCurrentLocation(9);
    new ActionStartPrevLinePlus("   ").indentLine(_doc);
    assertEquals(11, _doc.getLength());
    assertEquals("  foo\n     bar", _doc.getText(0, 11));
  }
  public void testBothIndentedAddStuff() throws javax.swing.text.BadLocationException {
    _setDocText("  foo\n  bar");
    _doc.setCurrentLocation(9);
    new ActionStartPrevLinePlus("abc").indentLine(_doc);
    assertEquals(11, _doc.getLength());
    assertEquals("  foo\n  abcbar", _doc.getText(0, 11));
  }
  public void testSecondLineMisindented() throws javax.swing.text.BadLocationException {
    _setDocText("  foo\n bar");
    _doc.setCurrentLocation(9);
    new ActionStartPrevLinePlus("abc").indentLine(_doc);
    assertEquals(11, _doc.getLength());
    assertEquals("  foo\n  abcbar", _doc.getText(0, 11));
  }
  public void testLeavesOtherLinesAlone() throws javax.swing.text.BadLocationException {
    _setDocText("foo\nbar\nblah");
    _doc.setCurrentLocation(10);
    new ActionStartPrevLinePlus("   ").indentLine(_doc);  // three spaces
    assertEquals(12, _doc.getLength());
    assertEquals("foo\nbar\nblah", _doc.getText(0, 12));
  }
  public void testOtherLinesIndented() throws javax.swing.text.BadLocationException {
    _setDocText(" foo\n  bar\n   blah");
    _doc.setCurrentLocation(15);
    new ActionStartPrevLinePlus("   ").indentLine(_doc);  // three spaces
    assertEquals(20, _doc.getLength());
    assertEquals(" foo\n  bar\n     blah", _doc.getText(0, 20));
  }
  */
}
