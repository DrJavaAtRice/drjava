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

package edu.rice.cs.drjava.model.junit;

import java.io.*;
import javax.swing.*;
import javax.swing.text.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.util.UnexpectedException;

import junit.framework.*;
import java.util.Enumeration;

/**
 * Contains the JUnitErrors for a particular file after
 * a test has ended.
 * @version $Id$
 */
public class JUnitErrorModel {
  private JUnitError[] _errors;
  private Position[] _positions;
  private Document _document;
  private File _file;

  /**
   * Constructs a new JUnitErrorModel to be maintained
   * by a particular OpenDefinitionsDocument.
   * @param doc Document containing the errors
   * @param file File containing the errors, or null
   */
  public JUnitErrorModel(Document doc, String theclass, TestResult result) {
    _document = doc;
    JUnitError[] errors = new JUnitError[result.errorCount() + result.failureCount()];

    Enumeration failures = result.failures();

    int i=0;

    while (failures.hasMoreElements()) {
      TestFailure tf = (TestFailure) failures.nextElement();
      TestCase tc = (TestCase) tf.failedTest();

      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);

      tf.thrownException().printStackTrace(pw);

      String classname = theclass + "." + tc.getName();
      String theLine = _substring(sw.toString(), 0, sw.toString().indexOf(classname));
      theLine = _substring(theLine, 0, theLine.indexOf("\n"));

      theLine = _substring(theLine, 0, theLine.indexOf("(") + 1);
      theLine = _substring(theLine, 0, theLine.indexOf(")"));

      String file = _substring(theLine, 0, theLine.indexOf(":"));
      int lineNo = 0;
      try {
        lineNo = new Integer(_substring(theLine, 
                                        theLine.indexOf(":") + 1,
                                        theLine.length()))
          .intValue() - 1;
      } 
      catch (NumberFormatException e) {
        throw new UnexpectedException(e);
      }

      _file = new File(file);

      errors[i] = new JUnitError(_file, lineNo, 0, tf.thrownException().getMessage(),
                                 ! (tf.thrownException() instanceof AssertionFailedError),
                                 tc.getName());
      i++;
    }

    _groupErrors(errors);
  }

  /**
   * Constructs a new JUnitErrorModel to be maintained
   * by a particular OpenDefinitionsDocument.
   * @param doc Document containing the errors
   * @param file File containing the errors, or null
   */
  public JUnitErrorModel() {
    _document = null;
    _file = null;
    _errors = new JUnitError[0];
    _positions = new Position[0];
  }


  /**
   * Returns the array of errors with positions.
   */
  public JUnitError[] getErrors() {
    return _errors;
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
   * Returns a substring, if it exists. Otherwise, it returns "(not applicable)".
   * @ pre start >= 0
   */
  private String _substring(String s, int start, int end) {
    if (end >= 0) {
      return s.substring(start, end);
    }
    else {
      return "0";
    }
  }

  /**
   * Groups errors into those with and without positions,
   * and creates the corresponding array of positions.
   */
  private void _groupErrors(JUnitError[] errors) {
    _errors = errors;

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
             (offset < defsText.length()))
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

}
