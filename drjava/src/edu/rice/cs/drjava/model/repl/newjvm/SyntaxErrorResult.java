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

package edu.rice.cs.drjava.model.repl.newjvm;

import edu.rice.cs.javaast.parser.*;
import edu.rice.cs.javaast.tree.*;
import edu.rice.cs.javaast.*;

/**
 * A syntax error to pass back to the main JVM after a call
 * to interpret.
 * 
 * @version $Id$
 */
public class SyntaxErrorResult implements InterpretResult {
  private final int _startRow;
  private final int _startCol;
  private final int _endRow;
  private final int _endCol;
  
  private final String _errorMessage;
  private final String _interaction;

  public SyntaxErrorResult(ParseException pe, String s)
  {
    _startRow = pe.getBeginLine();
    _startCol = pe.getBeginColumn();
    _endRow = pe.getEndLine();
    _endCol = pe.getEndColumn();
    _errorMessage = pe.getInteractionsMessage();
    _interaction = s;
  }
  
  public SyntaxErrorResult(TokenMgrError pe, String s)
  {
    _endRow = _startRow = pe.getErrorRow();
    // The lexical error is thrown from the position following
    // the offending character.  Failure to correct this has
    // grave consequences for our error highlighting.
    _endCol = _startCol = pe.getErrorColumn() - 1;
    _errorMessage = pe.getMessage();
    _interaction = s;
  }
  
  public String getErrorMessage() {
    return _errorMessage;
  }
  
  public String getInteraction(){
    return _interaction;
  }

  public int getStartRow(){ return _startRow; }
  public int getStartCol(){ return _startCol; }
  public int getEndRow(){ return _endRow; }
  public int getEndCol(){ return _endCol; }

  public <T> T apply(InterpretResultVisitor<T> v) {
    return v.forSyntaxErrorResult(this);
  }

  public String toString() { return "(syntax error: " + _errorMessage + ")"; }
}