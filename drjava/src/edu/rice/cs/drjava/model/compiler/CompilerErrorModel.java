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

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import javax.swing.*;
import javax.swing.text.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.util.UnexpectedException;


/**
 * Contains the CompilerErrors for a particular file after
 * a compile has ended.
 * @version $Id$
 */
public class CompilerErrorModel {
  private CompilerError[] _errors;
  private CompilerError[] _errorsWithoutPositions;
  private Position[] _positions;
  private Document _document;
  private File _file;

  /**
   * Constructs a new CompilerErrorModel to be maintained
   * by a particular OpenDefinitionsDocument.
   * @param doc Document containing the errors
   * @param file File containing the errors, or null
   */
  public CompilerErrorModel(CompilerError[] errors, Document doc, File file) {
    _document = doc;
    _file = file;
    _groupErrors(errors);
  }

  /**
   * Constructs a CompilerErrorModel with no errors or files.
   */
  public CompilerErrorModel() {
    this(new CompilerError[0], null, null);
  }

  /**
   * Returns the array of errors with positions.
   */
  public CompilerError[] getErrorsWithPositions() {
    return _errors;
  }

  /**
   * Returns the array of errors without positions.
   */
  public CompilerError[] getErrorsWithoutPositions() {
    return _errorsWithoutPositions;
  }

  /**
   * Returns the array of positions.
   */
  public Position[] getPositions() {
    return _positions;
  }

  /**
   * Returns the document associated with this error model.
   */
  public Document getDocument() {
    return _document;
  }

  /**
   * Returns the File associated with this error model.
   */
  public File getFile() {
    return _file;
  }

  /**
   * Groups errors into those with and without positions,
   * and creates the corresponding array of positions.
   */
  private void _groupErrors(CompilerError[] errors) {

    // Filter out errors with invalid source info.
    // They will be first since errors are sorted by line number,
    // and invalid source info is for negative line numbers.
    int numInvalid = 0;
    for (int i = 0; i < errors.length; i++) {
      if (errors[i].lineNumber() < 0) {
        numInvalid++;
      }
      else {
        // Since they were sorted, we must be done looking
        // for invalid source coordinates, since we found this valid one.
        break;
      }
    }

    _errorsWithoutPositions = new CompilerError[numInvalid];
    System.arraycopy(errors,
                     0,
                     _errorsWithoutPositions,
                     0,
                     numInvalid);

    int numValid = errors.length - numInvalid;
    _errors = new CompilerError[numValid];
    System.arraycopy(errors,
                     numInvalid,
                     _errors,
                     0,
                     numValid);

    // Create positions if non-null file
    if (_file != null) {
      _createPositionsArray();
    }
    else {
      _positions = new Position[0];
    }


    // DEBUG:
    /*
    for (int i = 0; i < _errors.length; i++) {
      DrJava.consoleErr().println("errormodel: error #" + i + ": " + _errors[i]);
    }

    DrJava.consoleErr().println();
    for (int i = 0; i < _positions.length; i++) {
      DrJava.consoleErr().println("errormodel: POS #" + i + ": " + _positions[i]);
    }

    DrJava.consoleErr().println();
    for (int i = 0; i < _errorsWithoutPositions.length; i++) {
      DrJava.consoleErr().println("errormode: errorNOP #" + i + ": " + _errorsWithoutPositions[i]);
    }
    */
  }


  /**
   * Create array of positions where each error occurred.
   */
  private void _createPositionsArray() {
    _positions = new Position[_errors.length];
    //DrJava.consoleErr().println("created pos arr: " + _positions.length);

    // don't bother with anything else if there are no errors
    if (_positions.length == 0)
      return;

    try {
      String defsText = _document.getText(0, _document.getLength());
      //DrJava.consoleErr().println("got defs text, len=" + defsText.length());

      int curLine = 0;
      int offset = 0; // offset is number of chars from beginning of file
      int numProcessed = 0;
      
      // offset is always pointing to the first character in a line
      // at the top of the loop
      while ((numProcessed < _errors.length) &&
             (offset <= defsText.length()))
      {
        //DrJava.consoleErr().println("num processed: " + numProcessed);

        // first figure out if we need to create any new positions on this line
        for (int i = numProcessed;
             (i < _errors.length) && (_errors[i].lineNumber() == curLine);
             i++)
        {
          _positions[i] = _document.createPosition(offset +
                                                   _errors[i].startColumn());
          numProcessed++;
        }

        int nextNewline = defsText.indexOf('\n', offset);
        if (nextNewline == -1) {
          break;
        }
        else {
          curLine++;
          offset = nextNewline + 1;
        }
      }
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
  }

  /**
   * Prints out this model's errors.
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("CompilerErrorModel:\n  ");
    for (int i=0; i < _errorsWithoutPositions.length; i++) {
      buf.append(_errorsWithoutPositions[i]);
      buf.append("\n  ");
    }
    for (int i=0; i < _errors.length; i++) {
      buf.append(_errors[i]);
      buf.append("\n  ");
    }
    return buf.toString();
  }
}
