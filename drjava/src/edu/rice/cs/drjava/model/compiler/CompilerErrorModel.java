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
import java.io.IOException;
import java.util.Arrays;
import javax.swing.text.*;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.OperationCanceledException;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.FileMovedException;
import gj.util.Hashtable;

/**
 * Contains the CompilerErrors for a particular file after
 * a compile has ended.
 * @version $Id$
 */
public class CompilerErrorModel<T extends CompilerError> {
  private T[] _errors;
  private Position[] _positions = null;
  private int _numErrors;
  private Hashtable<File, StartAndEndIndex> _filesToIndexes = new Hashtable<File, StartAndEndIndex>();
  private GlobalModel _model;

  /**
   * Constructs a new CompilerErrorModel to be maintained
   * by a particular OpenDefinitionsDocument.
   * @param errors the list of CompilerError's (or a subclass).
   * @param model is the model to find documents from
   */
  public CompilerErrorModel(T[] errors, GlobalModel model) {
    _model = model;
    _errors = errors;
    // Sort the errors by file and position
    Arrays.sort(_errors);
    _numErrors = errors.length;
    _calculatePositions();
  }

  public T[] getErrors(){
    return _errors;
  }

  /**
   * Returns the position of the given error in the document representing its file
   */
  public Position getPosition(T error) {
    int spot = Arrays.binarySearch(_errors, error);
    return _positions[spot];
  }

  /**
   * Returns the number of CompilerErrors
   */
  public int getNumErrors() {
    return _numErrors;
  }

  /**
   * Prints out this model's errors.
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(this.getClass().toString() + ":\n  ");
    for (int i=0; i < _errors.length; i++) {
      buf.append(_errors[i].toString());
      buf.append("\n  ");
    }
    return buf.toString();
  }

  /**
   * This method finds and returns the error that is at the given offset
   * @param odd the OpenDefinitionsDocument where you want to find the error at the caret
   * @param offset the offset into the document
   * @return the CompilerError at the given offset, null if no error corresponds to this location
   */
  public T getErrorAtOffset(OpenDefinitionsDocument odd, int offset) {
    File file = null;
    try {
      file = odd.getFile();
    } catch (IllegalStateException e) {
      return null;
    } catch (FileMovedException e) {
      file = e.getFile();
    }


    StartAndEndIndex saei = _filesToIndexes.get(file);
    if (saei == null){
      return null;
    }
    int start = saei.getStartPos();
    int end = saei.getEndPos();
    if (start == end){
      return null;
    }

    // check if the dot is on a line with an error.
    // Find the first error that is on or after the dot. If this comes
    // before the newline after the dot, it's on the same line.
    int errorAfter; // index of the first error after the dot
    for (errorAfter = start; errorAfter < end; errorAfter++) {
      if (_positions[errorAfter] == null) {
        //This indicates something wrong, but it was happening before so...
        return null;
      }
      if (_positions[errorAfter].getOffset() >=offset) {
        break;
      }
    }

    // index of the first error before the dot
    int errorBefore = errorAfter - 1;

    // this will be set to what we want to select, or -1 if nothing
    int shouldSelect = -1;

    if (errorBefore >= start) { // there's an error before the dot
      int errPos = _positions[errorBefore].getOffset();
      try {
        String betweenDotAndErr = odd.getDocument().getText(errPos, offset - errPos);

        if (betweenDotAndErr.indexOf('\n') == -1) {
          shouldSelect = errorBefore;
        }
      }
      catch (BadLocationException willNeverHappen) {
        throw new UnexpectedException(willNeverHappen);
      }
    }

    if ((shouldSelect == -1) && (errorAfter != _positions.length)) {
      // we found an error on/after the dot
      // if there's a newline between dot and error,
      // then it's not on this line
      int errPos = _positions[errorAfter].getOffset();
      try {
        String betweenDotAndErr = odd.getDocument().getText(offset, errPos - offset);

        if (betweenDotAndErr.indexOf('\n') == -1) {
          shouldSelect = errorAfter;
        }
      }
      catch (BadLocationException willNeverHappen) {
        throw new UnexpectedException(willNeverHappen);
      }
    }

    if (shouldSelect == -1){
      return null;
    } else {
      return _errors[shouldSelect];
    }
  }

  /**
   * This function tells if there are errors with source locations associated
   * with the given file
   */
  public boolean hasErrorsWithPositions(OpenDefinitionsDocument odd){
    File file = null;
    try {
      file = odd.getFile();
    } catch (IllegalStateException ise) {
      //no associated file, do nothing
    } catch (FileMovedException fme) {
      file = fme.getFile();
    }
    if (file == null){
      return false;
    }

    StartAndEndIndex saei = _filesToIndexes.get(file);
    if (saei == null){
      return false;
    }
    if (saei.getStartPos() == saei.getEndPos()){
      return false;
    }
    return true;
  }

  /**
   * Create array of positions where each error occurred.
   * positions are related to the document that each error came from
   */
  private void _calculatePositions() {
    _positions = new Position[_errors.length];
    try {
      int numProcessed = 0;

      //first skip errors with no file
      while(numProcessed < _errors.length && _errors[numProcessed].file() == null){
        _positions[numProcessed] = null;
        numProcessed++;
      }

      while ((numProcessed < _errors.length)) {
        //skip errors with no position
        while(numProcessed < _errors.length && _errors[numProcessed].hasNoLocation()){
          _positions[numProcessed] = null;
          numProcessed++;
        }
        if (numProcessed >= _errors.length){
          break;
        }

        //Now find the file and document we are working on
        File file = _errors[numProcessed].file();
        Document document = null;
        try {
          document = _model.getDocumentForFile(file).getDocument();
        } catch (IOException e) {
          //skip positions for these errors if the document couldn't be loaded
         do {
            _positions[numProcessed] = null;
            numProcessed++;
          } while(numProcessed < _errors.length && _errors[numProcessed].file().equals(file));
        } catch (OperationCanceledException e) {
          //skip positions for these errors if the document couldn't be loaded
         do {
            _positions[numProcessed] = null;
            numProcessed++;
          } while(numProcessed < _errors.length && _errors[numProcessed].file().equals(file));
        }

        if (numProcessed >= _errors.length){
          break;
        }

        //If the document couldn't be loaded, start the loop over at the top
        if (document == null){
          continue;
        }

        int fileStartIndex = numProcessed;
        String defsText = document.getText(0, document.getLength());
        int curLine = 0;
        int offset = 0; // offset is number of chars from beginning of file

        // offset is always pointing to the first character in a line
        // at the top of the loop
        while(numProcessed < _errors.length &&
          file.equals(_errors[numProcessed].file()) &&
          (offset <= defsText.length())) {

          // first figure out if we need to create any new positions on this line
          for (int i = numProcessed;
               (i < _errors.length) && (_errors[i].lineNumber() == curLine);
               i++){
            _positions[i] = document.createPosition(offset +  _errors[i].startColumn());
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

        //Remember the indexes in the _errors and _positions arrays that are for errors in this file
        int fileEndIndex = numProcessed;
        if (fileEndIndex != fileStartIndex){
          _filesToIndexes.put(file, new StartAndEndIndex(fileStartIndex, fileEndIndex));
        }
      }
    } catch (BadLocationException ble) {
       throw new UnexpectedException(ble);
    }
  }

  /**
   * This class is used only to track where the errors with positions for a file
   * begin and end.  The beginning index is inclusive, the ending index is exclusive.
   */
  private class StartAndEndIndex {
    private int startPos;
    private int endPos;

    public StartAndEndIndex(int startPos, int endPos){
      this.startPos = startPos;
      this.endPos = endPos;
    }
    public int getStartPos() {
      return startPos;
    }

    public int getEndPos() {
      return endPos;
    }

  }
}
