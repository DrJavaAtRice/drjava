/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.IOException;
import javax.swing.text.*;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.lang.reflect.Array;
import javax.swing.text.*;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.IGetDocuments;
import edu.rice.cs.drjava.model.OperationCanceledException;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.FileMovedException;

/**
 * Contains the CompilerErrors for a particular file after
 * a compile has ended.
 * @version $Id$
 */
public class CompilerErrorModel<T extends CompilerError> {
  private static final String newLine = System.getProperty("line.separator");
  /**
   * An array of errors to be displayed in the CompilerErrorPanel associated
   * with this model.  After the constructor, this should be sorted in this order:
   *   Errors with no file.
   *   Errors for each file in path-alphabetical order.
   *     within each file:
   *       errors with no line number
   *       errors with line numbers, in order
   * In all cases, where all else is equal, warnings are sorted below errors.
   */
  private final T[] _errors;
  
  /**
   * An array of file offsets, parallel to the _errors array.
   * NOTE: If there is no position associated with an error, its entry here
   *       should be set to null.
   */
  private final Position[] _positions;
  
  /**
   * The size of _errors and _positions.  This should never change after the constructor!
   */
  private final int _numErrors;
  
  /**
   * Cached result of hasOnlyWarnings.
   * Three-state enum:
   *  -1 => result has not been computed
   *   0 => false
   *   1 => true
   */
  private int _onlyWarnings = -1;
  
  /**
   * Used internally in building _positions.
   * The file used as the index *must* be a canonical file, or else
   * errors won't always be associated with the right documents.
   */
  private final HashMap<File, StartAndEndIndex> _filesToIndexes = 
    new HashMap<File, StartAndEndIndex>();
  
  /**
   * The global model which created/controls this object.
   */
  private final IGetDocuments _model;
  
  /**
   * Constructs an empty CompilerErrorModel.
   * @param empty the empty array of T
   */
  public CompilerErrorModel(T[] empty) {
    _model = new IGetDocuments() {
      public OpenDefinitionsDocument getDocumentForFile(File file) {
        throw new IllegalStateException("No documents to get!");
      }
      public boolean isAlreadyOpen(File file) {
        return false;
      }
      public List<OpenDefinitionsDocument> getDefinitionsDocuments() {
        return new LinkedList<OpenDefinitionsDocument>();
      }
      public boolean hasModifiedDocuments() {
        return false;
      }
    };
    _errors = empty;
    _numErrors = 0;
    _positions = new Position[0];
  }

  /**
   * Constructs a new CompilerErrorModel to be maintained
   * by a particular OpenDefinitionsDocument.
   * @param errors the list of CompilerError's (or a subclass).
   * @param model is the model to find documents from
   */
  public CompilerErrorModel(T[] errors, IGetDocuments model) {
    _model = model;
    
    // TODO: If we move to NextGen-style generics, ensure _errors is non-null.
    _errors = errors;
    
    // Next two lines are order-dependent!
    _numErrors = errors.length;
    _positions = new Position[_numErrors];
    
    // Sort the errors by file and position
    Arrays.sort(_errors);
    
    // Populates _positions.
    _calculatePositions();
  }

  /**
   * Accessor for errors maintained here.
   * @param idx the index of the error to retrieve
   * @returns the error at index idx
   * @throws NullPointerException if this object was improperly initialized
   * @throws ArrayIndexOutOfBoundsException if !(0 <= idx < this.getNumErrors())
   */
  public T getError(int idx) {
    return _errors[idx];
  }

  /**
   * Returns the position of the given error in the document representing its file
   */
  public Position getPosition(CompilerError error) {
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
    for (int i=0; i < _numErrors; i++) {
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
    }
    catch (IllegalStateException e) {
      return null;
    }
    catch (FileMovedException e) {
      file = e.getFile();
    }
    
    // Use the canonical file if possible
    try {
      file = file.getCanonicalFile();
    }
    catch (IOException ioe) {
      // Oh well, we'll look for it as is.
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

    if ((shouldSelect == -1) && (errorAfter < end)) {// (errorAfter != _positions.length)) {
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
    }
    catch (IllegalStateException ise) {
      //no associated file, do nothing
    }
    catch (FileMovedException fme) {
      file = fme.getFile();
    }
    if (file == null) {
      return false;
    }
    
    // Try to use the canonical file
    try {
      file = file.getCanonicalFile();
    }
    catch (IOException ioe) {
      // Oh well, look for the file as is.
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
   * Checks whether all CompilerErrors contained here are actually warnings.
   * This would indicate that there were no "real" errors, so output is valid.
   * @return false if any error contained here is not a warning, true otherwise
   */
  public boolean hasOnlyWarnings() {
    // Check for a cached value.
    if (_onlyWarnings == 0) {
      return false;
    }
    else if (_onlyWarnings == 1) {
      return true;
    }
    else {
      // If there was no cached value, compute it.
      boolean clean = true;
      for (int i = 0; clean && (i < _numErrors); i++) {
        clean = _errors[i].isWarning();
      }
      // Cache the value.
      _onlyWarnings = clean? 1: 0;
      return clean;
    }
  } 

  /**
   * Create array of positions where each error occurred.
   * positions are related to the document that each error came from
   */
  private void _calculatePositions() {
    try {
      int curError = 0;
      
      // for(; numProcessed < _numErrors; numProcessed++) {
      while ((curError < _numErrors)) {
        
        // find the next error with a line number (skipping others)
        curError = nextErrorWithLine(curError);
        if (curError >= _numErrors){
          break;
        }

        //Now find the file and document we are working on
        File file = _errors[curError].file();
        Document document = null;
        try {
          document = _model.getDocumentForFile(file).getDocument();
        } 
        catch (Exception e) {
          // This is intended to catch IOException or OperationCanceledException
          if ((e instanceof IOException) || (e instanceof OperationCanceledException)) {
            // skip positions for these errors if the document couldn't be loaded
            do {
              curError++;
            } while ((curError < _numErrors) && (_errors[curError].file().equals(file)));
            
            //If the document couldn't be loaded, start the loop over at the top
            continue;
          }
          else {
            throw new UnexpectedException(e);
          }
        }

        if (curError >= _numErrors){
          break;
        }

        // curError is the first error in a file, and its document is open.
        final int fileStartIndex = curError;
        final int defsLength = document.getLength();
        final String defsText = document.getText(0, defsLength);
        int curLine = 0;
        int offset = 0; // offset is number of chars from beginning of file

        // offset is always pointing to the first character in the line
        // containing an error (or the last line of the previous file) at the top of this loop
        while ((curError < _numErrors) && // we still have errors to find
               file.equals(_errors[curError].file()) &&  // the next error is in this file
               (offset <= defsLength)) { // we haven't gone past the end of the file

          // create new positions for all errors on this line
          while ((curError < _numErrors) &&
                 file.equals(_errors[curError].file()) &&  // we are still in this file
                 (_errors[curError].lineNumber() == curLine))
          {
            _positions[curError] =
              document.createPosition(offset +  _errors[curError].startColumn());
            curError++;
          }

          // At this point, offset is the starting index of the previous error's line.
          // Update offset to be appropriate for the current error.
          // ... but don't bother looking if it isn't in this file.
          // ... or if we're done with all errors already.
          if (curError < _numErrors) {
            int curErrorLine = _errors[curError].lineNumber();
            int nextNewline = 0;
            while ((curLine != curErrorLine) 
                     && (nextNewline != -1)
                     && (file.equals(_errors[curError].file()))) {
              nextNewline = defsText.indexOf(newLine, offset);
              if (nextNewline == -1) {
                nextNewline = defsText.indexOf("\n", offset);
              }
              if (nextNewline != -1) {
                curLine++;
                offset = nextNewline + 1;
              }
            }
          }
        }

        //Remember the indexes in the _errors and _positions arrays that 
        // are for the errors in this file
        int fileEndIndex = curError;
        if (fileEndIndex != fileStartIndex) {
          // Try to use the canonical file if possible
          try {
            file = file.getCanonicalFile();
          }
          catch (IOException ioe) {
            // Oh well, store it as is
          }
          _filesToIndexes.put(file, new StartAndEndIndex(fileStartIndex, fileEndIndex));
        }
      }
    }
    catch (BadLocationException ble) {
       throw new UnexpectedException(ble);
    }
  }

  /**
   * Finds the first error after numProcessed which has a file and line number.
   * @param start the starting index of the search
   * @return the index of the found error
   */
  private int nextErrorWithLine(int idx) {
    while ((idx < _numErrors)
           && (_errors[idx].hasNoLocation()
               || (_errors[idx].file() == null))) {
      idx++;
    }
    return idx;
  }

  /**
   * This class is used only to track where the errors with positions for a file
   * begin and end.  The beginning index is inclusive, the ending index is exclusive.
   */
  private static class StartAndEndIndex {
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
