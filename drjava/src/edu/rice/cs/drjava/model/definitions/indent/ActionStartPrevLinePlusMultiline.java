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
import javax.swing.text.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

/**
 * Indents the current line in the document to the indent level of the
 * start of the previous line, adds several lines of text at that indent level,
 * and moves the cursor to a particular line and position.
 * @version $Id$
 */
class ActionStartPrevLinePlusMultiline extends IndentRuleAction {
  private String[] _suffices;
  private int _line = 0;
  private int _position = 0;
  private int _offset = 0;

  /**
   * Creates a multiline insert rule.  It should be noted that although the suffices
   * are referred to as "lines", this class simply appends the strings with a
   * number of spaces for padding.  Any newline characters you intend to place
   * in the document must be explicitly placed within the input strings.
   * Typically, all but the last "line" will have a '\n' character at the end.
   * @param suffices the new lines to be added
   * @param line the line on which to place the cursor 
   * @param position the character within the line string before which to place
   * the cursor
   * @throws IllegalArgumentException if the integer params are negative or
   * outside the appropriate bounds
   */
  public ActionStartPrevLinePlusMultiline(String suffices[],
                                          int line, int position) {
    _suffices = suffices;
    
    // do bounds checking up-front
    if ((line >= 0) && (line < suffices.length)) {
      _line = line;
    }
    else {
      throw new IllegalArgumentException
        ("The specified line was outside the bounds of the specified array.");
    }
    
    if ((position >= 0) && (position <= suffices[line].length())) {
      _position = position;
    }
    else {
      throw new IllegalArgumentException
        ("The specified position was not within the bounds of the specified line.");
    }
    
    // pre-compute the relative offset (without indents) of the new position
    for (int i = 0; i < line; i++) {
      _offset += _suffices[i].length();
    }
    _offset += position;
  }
  
  /**
   * Indents the line according to the previous line, with the suffix lines added
   * and the cursor moved to a specific location.
   * If on the first line, indent is set to 0.
   * @param doc DefinitionsDocument containing the line to be indented.
   * @return this is always false, since we are updating the cursor location
   */
  public boolean indentLine(DefinitionsDocument doc, int reason){
    super.indentLine(doc, reason);
    try {
      // Find start of line
      int here = doc.getCurrentLocation();
      int startLine = doc.getLineStartPos(here);

      if (startLine > DefinitionsDocument.DOCSTART) {
        // Find prefix of previous line
        int startPrevLine = doc.getLineStartPos(startLine - 1);
        int firstChar = doc.getLineFirstCharPos(startPrevLine);
        String prefix = doc.getText(startPrevLine, firstChar - startPrevLine);
        
        // indent and add the suffices
        for (int i = 0; i < _suffices.length; i++) {
          doc.setTab(prefix + _suffices[i], here);
          here += prefix.length() + _suffices[i].length();
        }
        
        // move the cursor to the appropriate position
        int newPos = startLine + _offset + (prefix.length() * (_line + 1));
        doc.setCurrentLocation(newPos);
      }
      else {
        // On first line
        for (int i = 0; i < _suffices.length; i++) {
          doc.setTab(_suffices[i], here);
          here += _suffices[i].length();
        }
      }
      return false;
    }
    catch (BadLocationException e) {
      // Shouldn't happen
      throw new UnexpectedException(e);
    }
  }
}
