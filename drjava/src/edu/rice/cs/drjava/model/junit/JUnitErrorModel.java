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
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.definitions.*;


import junit.framework.*;
import java.util.Enumeration;

/**
 * Contains the JUnitErrors for a particular file after
 * a test has ended.
 * @version $Id$
 */
public class JUnitErrorModel {
  private JUnitError[] _errors;
  private JUnitError[] _errorsWithoutPositions;
  private Position[] _positions;
  private DefinitionsDocument _document;
  private File _file;
  
  private boolean _testsHaveRun = false;
  private int _errorsWithPos = 0;
  
  /**
   * Constructs a new JUnitErrorModel to be maintained
   * by a particular OpenDefinitionsDocument.
   * @param doc Document containing the errors
   * @param file File containing the errors, or null
   */
  public JUnitErrorModel(DefinitionsDocument doc, String theclass, TestResult result) {
    _document = doc;
    _testsHaveRun = true;
        
    JUnitError[] errors = new JUnitError[result.errorCount() + result.failureCount()];
     
    Enumeration failures = result.failures();
    Enumeration errEnum = result.errors(); 
    
    int i=0;
    
    while ( errEnum.hasMoreElements()) {
      TestFailure tErr = (TestFailure) errEnum.nextElement();
      errors[i] = _makeJUnitError(tErr, theclass, true);
      i++;
    }
    
    while (failures.hasMoreElements()) {
      TestFailure tFail = (TestFailure) failures.nextElement();
      errors[i] = _makeJUnitError(tFail, theclass, false);
      i++;
    }
      
    Arrays.sort(errors);
    
    //Create the array of errors and failures, ordered by line number
    
    /* while ( (i < errors.length) && ( (tFail != null) || (tErr != null) ) ) {
     
      if ( (jFail != null) && ( (jErr == null) || (jFail.lineNumber() < jErr.lineNumber()) ) ) {
        errors[i] = jFail;
        System.out.println(jFail.lineNumber());
        if (failures.hasMoreElements()) {
          tFail = (TestFailure)failures.nextElement();
          jFail = _makeJUnitError(tFail, theclass, false);
        }
        else {
          tFail = null;
          jFail = null;
        }
      }
      
      else if ( (jErr != null) && ( (jFail == null) || (jErr.lineNumber() <= jFail.lineNumber()) ) ) {
        errors[i] = jErr;
        System.out.println(jErr.lineNumber());
        if (errEnum.hasMoreElements()) {
          tErr = (TestFailure)errEnum.nextElement();
          jErr = _makeJUnitError(tErr, theclass, true);
        }
        else {
          tErr = null;
          jErr = null;
        }
      }
       
      i++;
    }*/
   
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
    _errorsWithoutPositions = new JUnitError[0];
    _positions = new Position[0];
    _testsHaveRun = false;
    _errorsWithPos = 0;
  }
  
  /**
   * 
   */
  private String _quickParse( String sw, String classname ) {
    String theLine = _substring(sw, sw.toString().indexOf(classname), sw.length());
    theLine = _substring(theLine, theLine.indexOf(classname), theLine.length());
    theLine = _substring(theLine, theLine.indexOf("(") + 1, theLine.length());
    theLine = _substring(theLine, 0, theLine.indexOf(")"));
    return theLine;
  }

  /**
   * 
   */
  private int _lineNumber (String sw, String classname) {
    
    int lineNum;

    int idxClassname = sw.indexOf(classname);
    if (idxClassname == -1) return -1;

    String theLine = sw.substring(idxClassname, sw.length());
    theLine = theLine.substring(theLine.indexOf(classname), theLine.length());
    theLine = theLine.substring(theLine.indexOf("(") + 1, theLine.length());
    theLine = theLine.substring(0, theLine.indexOf(")"));
    
    try {
      lineNum = new Integer(theLine.substring(
                                      theLine.indexOf(":") + 1,
                                      theLine.length())).intValue() - 1;
    } 
    catch (NumberFormatException e) {
      throw new UnexpectedException(e);
    }
    
    return lineNum;
  }
    
  /**
   * Constructs a new JUnitError from a TestFailure
   * @param tF A given TestFailure
   * @param theclass The class that contains the TestFailure
   * @param isError The passed TestFailure may signify either an error or a failure
   * @return JUnitError 
   */
  private JUnitError _makeJUnitError ( TestFailure tF, String theclass, boolean isError) {
   
    TestFailure tFail = tF;
    TestCase tcFail = (TestCase) tFail.failedTest();
    
    StringWriter swFail = new StringWriter();
    PrintWriter pwFail  = new PrintWriter(swFail);
    
    tFail.thrownException().printStackTrace(pwFail);
        
    String classnameFail = theclass + "." + tcFail.getName();
       
    int lineNum = _lineNumber( swFail.toString(), classnameFail);
    if (lineNum > -1) _errorsWithPos++;
    
    _file = _document.getFile();
    
    String exception =  (isError) ? 
      tFail.thrownException().toString() : 
      tFail.thrownException().getMessage();
      
    return new JUnitError(_file, lineNum, 0, exception,
                                 ! (tFail.thrownException() instanceof AssertionFailedError),
                                 tcFail.getName());
  }


  /**
   * Accessor
   * @return whether tests have been run before.
   */
  public boolean haveTestsRun() {
    return _testsHaveRun;
  }

  /**
   * Returns the array of errors with positions.
   */
  public JUnitError[] getErrorsWithPositions() {
    return _errors;
  }

  /**
   * Returns the array of errors without positions.
   */
  public JUnitError[] getErrorsWithoutPositions() {
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
  public DefinitionsDocument getDocument() {
    return _document;
  }

  /**
   * Returns a substring, if it exists. Otherwise, it returns "(not applicable)".
   * @ pre start >= 0
   */
  String _substring(String s, int start, int end) {
    //to do: remove this method and reformat any code depending on it
    
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

    _errorsWithoutPositions = new JUnitError[numInvalid];
    System.arraycopy(errors,
                     0,
                     _errorsWithoutPositions,
                     0,
                     numInvalid);

    int numValid = errors.length - numInvalid;
    _errors = new JUnitError[numValid];
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
             (offset < defsText.length()))
      {
        //DrJava.consoleErr().println("num processed: " + numProcessed);
        
        //System.out.println( _errors[0].lineNumber());
        
        
        // first figure out if we need to create any new positions on this line
        for (int i = numProcessed;
             (i < _errors.length) && (_errors[i].lineNumber() == curLine);
             i++)
        {
          
          //System.out.println("Positions["+i+"]: "+offset+_errors[i].startColumn());
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
