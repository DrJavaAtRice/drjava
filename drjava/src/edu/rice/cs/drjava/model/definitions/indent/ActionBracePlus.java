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

import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.BraceReduction;
import edu.rice.cs.drjava.model.definitions.reducedmodel.IndentInfo;

/**
 * Aligns the indentation of the current line to the character
 * that opened the most recent block or expression list that contains
 * the beginning of the current line. Optional additional whitespaces 
 * can be passed through the constructor.
 * @version $Id$
 */
public class ActionBracePlus extends IndentRuleAction 
{
  /** String holding the additional whitespaces to be inserted. */
  private String _suffix;
  
  /** @param plus The additional whitespaces to be inserted. */
  public ActionBracePlus(String suffix)
  {
    _suffix = suffix;
  }
  
  /**
   * Properly indents the line that the caret is currently on. 
   * Replaces all whitespace characters at the beginning of the 
   * line with the appropriate spacing or characters.
   * Preconditions: must be inside a brace.
   * @param doc DefinitionsDocument containing the line to be indented.
   */
  public void indentLine(DefinitionsDocument doc)
  {
    super.indentLine(doc);
    int here = doc.getCurrentLocation();
    int startLine = doc.getLineStartPos(here);
    doc.setCurrentLocation(startLine);
    IndentInfo ii = doc.getIndentInformation();
    
    // Check preconditions
    if ((ii.braceType.equals("")) ||
        (ii.distToBrace < 0)) {
      // Can't find brace, so do nothing.
      return;
    }

    // Find length to brace
    int bracePos = startLine - ii.distToBrace;
    int braceNewLine = 0;
    if (ii.distToNewline >=0) {
      braceNewLine = startLine - ii.distToNewline;
    }
    int braceLen = bracePos - braceNewLine;

    // Create tab string
    StringBuffer tab = new StringBuffer(_suffix.length() + braceLen);
    for (int i=0; i < braceLen; i++) {
      tab.append(" ");
    }
    tab.append(_suffix);
    
    if (here > doc.getLength()) {
      here = doc.getLength() - 1;
    }
    doc.setCurrentLocation(here);
    
    doc.setTab(tab.toString(), here);
  }
}
