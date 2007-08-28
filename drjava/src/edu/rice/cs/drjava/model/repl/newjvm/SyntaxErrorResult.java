/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl.newjvm;

//import edu.rice.cs.javaast.parser.*;
import koala.dynamicjava.parser.*;
import koala.dynamicjava.parser.wrapper.*;

/** A syntax error to pass back to the main JVM after a call to interpret.
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

  public SyntaxErrorResult(ParseException pe, String s) {
    _startRow = pe.getBeginLine();
    _startCol = pe.getBeginColumn();
    _endRow = pe.getEndLine();
    _endCol = pe.getEndColumn();
    _errorMessage = pe.getShortMessage();
    _interaction = s;
  }

  public SyntaxErrorResult(ParseError pe, String s) {
    ParseException parseEx = pe.getParseException();
    if (parseEx != null) {
      _startRow = parseEx.getBeginLine();
      _startCol = parseEx.getBeginColumn();
      _endRow = parseEx.getEndLine();
      _endCol = parseEx.getEndColumn();
      _errorMessage = parseEx.getShortMessage();      
    }      
    else {
      _startRow = _endRow = pe.getLine();
      _startCol = _endCol = pe.getColumn();
      _errorMessage = pe.getMessage();
    }
    _interaction = s;
  }

  public SyntaxErrorResult(TokenMgrError pe, String s) {
    _endRow = _startRow = pe.getErrorRow();
    // The lexical error is thrown from the position following the offending character.  Failure to correct this has
    // grave consequences for our error highlighting.
    _endCol = _startCol = pe.getErrorColumn() - 1;
    _errorMessage = pe.getMessage();
    _interaction = s;
  }

  public String getErrorMessage() { return _errorMessage; }

  public String getInteraction() { return _interaction; }

  public int getStartRow() { return _startRow; }
  public int getStartCol() { return _startCol; }
  public int getEndRow() { return _endRow; }
  public int getEndCol() { return _endCol; }

  public <T> T apply(InterpretResultVisitor<T> v) { return v.forSyntaxErrorResult(this); }

  public String toString() { return "(syntax error: " + _errorMessage + ")"; }
}