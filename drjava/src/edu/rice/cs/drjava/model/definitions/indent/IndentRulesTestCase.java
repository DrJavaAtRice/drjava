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

package edu.rice.cs.drjava.model.definitions.indent;

import junit.framework.*;
import junit.extensions.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;

/**
 * Superclass for all test classes for the indentation decision tree.
 * @version $Id$
 */
public abstract class IndentRulesTestCase extends TestCase {

  protected DefinitionsDocument _doc;
  private String _indent;
  
  /**
   * Tests the indentation decision tree.
   * @param     String name
   */
  public IndentRulesTestCase(String name) {
    super(name);
  }

  /**
   * Sets up the test environment.
   */
  public void setUp() {
    _doc = new DefinitionsDocument();
  }
  
  /**
   * Clears the text of the _doc field and sets it to the
   * given string.
   */
  protected final void _setDocText(String text)
    throws BadLocationException
  {
    _doc.remove(0, _doc.getLength());
    _doc.insertString(0, text, null);
  }
  
  /**
   * Sets the number of spaces to include in the indent string.
   *
  protected final void _setIndentSize(int size) {
    _indent = "";
    for (int i=0; i < size; i++) {
      _indent = _indent + " ";
    }
  }*/
  
  /**
   * Gets the length of the indent string.
   * @return Number of spaces in the indent string.
   *
  protected final int _getIndentSize() {
    return _indent.length();
  }*/
  
  /**
   * Get a string containing the specified number of indents.
   * @param numLevels Number of indent strings to return
   *
  protected String _getIndentString(int numLevels) {
    String indent = "";
    for (int i=0; i < numLevels; i++) {
      indent += _indent;
    }
    return indent;
  }*/

  /**
   * Inserts an indent of the specificed number of levels at the given
   * index in the string.
   * @param text String to insert indent into
   * @param index Position in string to add indent
   * @param numLevels Number of indents to insert
   *
  protected String _addIndent(String text, int index, int numLevels) {
    String start = text.substring(0, index);
    String end = text.substring(index);
    String indent = _getIndentString(numLevels);
    return start.concat(indent).concat(end);
  }*/
  
  /**
   * Asserts that the document contains the expected text.
   * @param expected what text of document should be
   */
  protected void _assertContents(String expected) throws BadLocationException {
    assertEquals("document contents", 
                 expected, 
                 _doc.getText(0, _doc.getLength()));
  }

}
