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
 * start of the previous line, plus the given suffix, then backs up to a
 * specific cursor position.
 * @version $Id$
 */
class ActionStartPrevLinePlusBackup extends IndentRuleAction {
  private String _suffix;
  private int _position = 0;

  /**
   * Rule that repeats the indentation from the previous line, plus a string,
   * then moves the cursor to a specified location.
   * @param suffix The string to be added
   * @param position the character within the suffix string before which to
   * place the cursor
   * @throws IllegalArgumentException if the position is negative or
   * outside the bounds of the suffix string
   */
  public ActionStartPrevLinePlusBackup(String suffix, int position) {
    _suffix = suffix;
    
    if ((position >= 0) && (position <= suffix.length())) {
      _position = position;
    }
    else {
      throw new IllegalArgumentException
        ("The specified position was not within the bounds of the suffix.");
    }
  }

  /**
   * Indents the line according to the previous line, with the suffix string added,
   * then backs up the cursor position a number of characters.
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
        
        // indent and add the suffix
        doc.setTab(prefix + _suffix, here);
        
        // move the cursor to the new position
        doc.setCurrentLocation(startLine + prefix.length() + _position);
      }
      else {
        // On first line
        doc.setTab(_suffix, here);
        
        // move the cursor to the new position
        doc.setCurrentLocation(here + _position);
      }
      
      return false;
    }
    catch (BadLocationException e) {
      // Shouldn't happen
      throw new UnexpectedException(e);
    }
  }
}
