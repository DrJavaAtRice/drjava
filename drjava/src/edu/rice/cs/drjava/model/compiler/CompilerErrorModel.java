/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.IOException;
import javax.swing.text.*;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;

import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.drjava.model.DummyGlobalModel;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.FileMovedException;
import edu.rice.cs.drjava.model.DrJavaFileUtils;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;

/** Contains the Errors for a set of compiled/tested filea after a compile or test step has ended.
  * TODO: refactor most of the code in this class into an abstract ErrorModel class.  JUnitErrorModel should inherit
  * from this abstract class not from CompilerErrorModel!  JavadocErrorModel should also inherit from
  * this abstract class instead of decorating a CompilerErrorModel!
  * @version $Id: CompilerErrorModel.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class CompilerErrorModel {
  private static final String newLine = StringOps.EOL;
  /** An array of errors to be displayed in the CompilerErrorPanel associated with this model.  After model
    * construction, this array should be sorted in this order:
    * (i)  Errors with no file.
    * (ii) Errors for each file in path-alphabetical order.
    * Within each file:
    * (i)  Errors with no line number.
    * (ii) Errors with line numbers, in order.
    * In all cases, where all else is equal, warnings are sorted below errors.
    */
  private final DJError[] _errors;
  
  /** An array of file offsets, parallel to the _errors array. NOTE: If there is no position associated with an error,
    * its entry here should be set to null.
    */
  private final Position[] _positions;
  
  /** The size of _errors and _positions.  This should never change after model construction*/
  private final int _numErrors;
  
  /** The number of compile errors. Used for display purposes only.*/
  private volatile int _numCompilerErrors;
  
  /** The number of compile warnings. Used for display purposes only.*/
  private volatile int _numWarnings;
  
  /** Cached result of hasOnlyWarnings.
    * Three-state enum:
    *  -1 => result has not been computed
    *   0 => false
    *   1 => true
    */
  private volatile int _onlyWarnings = -1;
  
  /** Used internally in building _positions.  The file used as the index *must* be a canonical file, or else
    * errors won't always be associated with the right documents.
    */
  private final HashMap<File, StartAndEndIndex> _filesToIndexes = new HashMap<File, StartAndEndIndex>();
  
  /** The global model which created/controls this object. */
  private final GlobalModel _model;
  
  /** Constructs an empty CompilerErrorModel with no errors and a dummy global model.  As a side effect, it 
    * opens any documents containing errors/warnings that are not currently open. */
  public CompilerErrorModel() {
    _model = new DummyGlobalModel() {
      public OpenDefinitionsDocument getDocumentForFile(File file) {
        throw new IllegalStateException("No documents to get!");
      }
      public boolean isAlreadyOpen(File file) { return false; }
      public List<OpenDefinitionsDocument> getOpenDefinitionsDocuments() {
        return new LinkedList<OpenDefinitionsDocument>();
      }
      public boolean hasModifiedDocuments() { return false; }
      public boolean hasUntitledDocuments() { return false; }
    };
    _errors = new DJError[0];
    _numErrors = 0;
    _numWarnings = 0;
    _numCompilerErrors = 0;
    _positions = new Position[0];
  }
  
  /** Constructs a new CompilerErrorModel with specified global model.  Performed in DefaultGlobalModel construction 
    * and after compilation has been performed.
    * @param errors the list of DJError's (or a subclass).
    * @param model is the model to find documents from
    */
  public CompilerErrorModel(DJError[] errors, GlobalModel model) {
    
//    System.err.println("Constructing CompilerErrorModel for errors: " + Arrays.toString(errors));
    _model = model;
       
    // legacy support for old .dj2 language level files:
    // see DrJava feature request 2990660
    // As of revisions 5225-5227, .dj2 files aren't converted by the LanguageLevelConverter anymore,
    // they are just copied. That means the compiler errors now happen in the .java file, not in the
    // .dj2 file anymore. When we get a compiler error in a .java file, and we have a corresponding
    // .dj2 file open, but not the .java file, then we change the error to refer to the .dj2 file
    // instead.
    if (model!=null) {
      HashSet<File> odds = new HashSet<File>();
      for(OpenDefinitionsDocument odd: model.getOpenDefinitionsDocuments()) {
        odds.add(odd.getRawFile());
      }
//      for(int i=0; i < errors.length; ++i) {
//        DJError e = errors[i];
//        if (e.fileName().endsWith(edu.rice.cs.drjava.config.OptionConstants.JAVA_FILE_EXTENSION)) {
//          // only needs to be done for .java files
//          File javaFile = e.file();
//          if (! odds.contains(javaFile)) {
//            // .java file is not open
//            File dj2File = DrJavaFileUtils.getDJ2ForJavaFile(javaFile);
//            if (odds.contains(dj2File)) {
//              // but corresponding .dj2 file is open, change error to refer to .dj2 file
//              errors[i] = new DJError(dj2File, e.lineNumber(), e.startColumn(), e.message(), e.isWarning());
//            }
//          }
//        }
//      }
    }

    
    // TODO: If we move to NextGen-style generics, ensure _errors is non-null.
    _errors = errors;
    
    _numErrors = errors.length;
    _positions = new Position[errors.length];
    
    _numWarnings = 0;
    _numCompilerErrors = 0;
    for (int i =0; i < errors.length; i++) {
      if (errors[i].isWarning()) _numWarnings++;
      else _numCompilerErrors++;
    }
    
    // Sort the errors by file and position
    Arrays.sort(_errors);
    
    // Populates _positions.  Must run in event thread because it may open files.
    Utilities.invokeLater(new Runnable() { public void run() { _calculatePositions(); } });
  }
  
  /** Accessor for errors maintained here.
    * @param idx the index of the error to retrieve
    * @return the error at index idx
    * @throws NullPointerException if this object was improperly initialized
    * @throws ArrayIndexOutOfBoundsException if !(0 <= idx < this.getNumErrors())
    */
  public DJError getError(int idx) { return _errors[idx]; }
  
  /** Returns the position of the given error in the document representing its file. */
  public Position getPosition(DJError error) {
    int spot = Arrays.binarySearch(_errors, error);
    return _positions[spot];
  }
  
  /** Returns the number of CompilerErrors. */
  public int getNumErrors() { return _numErrors; }
  
  /** Returns the number of CompilerErrors that are compiler errors */
  public int getNumCompilerErrors() { return _numCompilerErrors; }
  
  /** Returns the number of CompilerErrors that are warnings */
  public int getNumWarnings() { return _numWarnings; }
  
  /** Prints out this model's errors. */
  public String toString() {
    final StringBuilder buf = new StringBuilder();
    buf.append(this.getClass().toString() + ":\n  ");
    for (int i = 0; i < _numErrors; i++) {
      buf.append(_errors[i].toString());
      buf.append("\n  ");
    }
    return buf.toString();
  }
  
  /** This method finds and returns the error that is at the given offset
    * @param odd the OpenDefinitionsDocument where you want to find the error at the caret
    * @param offset the offset into the document
    * @return the DJError at the given offset, null if no error corresponds to this location
    */
  public DJError getErrorAtOffset(OpenDefinitionsDocument odd, int offset) {
    File file;
    try { 
      file = odd.getFile(); 
      if (file == null) return null;
    }
    catch (FileMovedException e) { file = e.getFile(); }
    
    // Use the canonical file if possible
    try { file = file.getCanonicalFile(); }
    catch (IOException ioe) {
      // Oh well, we'll look for it as is.
    }
    
    StartAndEndIndex saei = _filesToIndexes.get(file);
    if (saei == null) return null;
    int start = saei.getStartPos();
    int end = saei.getEndPos();
    if (start == end) return null;
    
    // check if the dot is on a line with an error.
    // Find the first error that is on or after the dot. If this comes
    // before the newline after the dot, it's on the same line.
    int errorAfter; // index of the first error after the dot
    for (errorAfter = start; errorAfter < end; errorAfter++) {
      if (_positions[errorAfter] == null) {
        //This indicates something wrong, but it was happening before so...
        return null;
      }
      if (_positions[errorAfter].getOffset() >=offset) break;
    }
    
    // index of the first error before the dot
    int errorBefore = errorAfter - 1;
    
    // this will be set to what we want to select, or -1 if nothing
    int shouldSelect = -1;
    
    if (errorBefore >= start) { // there's an error before the dot
      int errPos = _positions[errorBefore].getOffset();
      try {
        String betweenDotAndErr = odd.getText(errPos, offset - errPos);
        if (betweenDotAndErr.indexOf('\n') == -1) shouldSelect = errorBefore;
      }
      catch (BadLocationException e) { /* source document has been edited; fail silently */ }
      catch (StringIndexOutOfBoundsException e) { /* source document has been edited; fail silently */ }
    }
    
    if ((shouldSelect == -1) && (errorAfter < end)) {// (errorAfter != _positions.length)) {
      // we found an error on/after the dot
      // if there's a newline between dot and error,
      // then it's not on this line
      int errPos = _positions[errorAfter].getOffset();
      try {
        String betweenDotAndErr = odd.getText(offset, errPos - offset);
        if (betweenDotAndErr.indexOf('\n') == -1) shouldSelect = errorAfter;
      }
      catch (BadLocationException e) { /* source document has been edited; fail silently */ }
      catch (StringIndexOutOfBoundsException e) { /* source document has been edited; fail silently */ }
    }
    
    if (shouldSelect == -1) return null;
    return _errors[shouldSelect];
  }
  
  /** This function tells if there are errors with source locations associated with the given file. */
  public boolean hasErrorsWithPositions(OpenDefinitionsDocument odd) {
    File file = FileOps.NULL_FILE;
    try { 
      file = odd.getFile();
      if (file == null || file == FileOps.NULL_FILE) return false;
    }
    catch (FileMovedException fme) { file = fme.getFile(); }
    
    // Try to use the canonical file
    try { file = file.getCanonicalFile(); }
    catch (IOException ioe) { /* Oh well, look for the file as is.*/ }
    
    StartAndEndIndex saei = _filesToIndexes.get(file);
    if (saei == null) return false;
    if (saei.getStartPos() == saei.getEndPos()) return false;
    return true;
  }
  
  /** Checks whether all CompilerErrors contained here are actually warnings. This would indicate that there were no
    * "real" errors, so output is valid.
    * @return false if any error contained here is not a warning, true otherwise
    */
  public boolean hasOnlyWarnings() {
    // Check for a cached value.
    if (_onlyWarnings == 0) return false;
    if (_onlyWarnings == 1) return true;
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
  
  /** Create array of positions where each error occurred. Positions are related their corresponding documents. */
  private void _calculatePositions() {
    try {
      int curError = 0;
      
      // for (; numProcessed < _numErrors; numProcessed++) {
      while ((curError < _numErrors)) {
        // find the next error with a line number (skipping others)
        curError = nextErrorWithLine(curError);
        if (curError >= _numErrors) {break;}
        
        //Now find the file and document we are working on
        File file = _errors[curError].file();
        OpenDefinitionsDocument document;
        try { document = _model.getDocumentForFile(file); }
        catch (Exception e) {
          // This is intended to catch IOException or OperationCanceledException
          // skip positions for these errors if the document couldn't be loaded
          if ((e instanceof IOException) || (e instanceof OperationCanceledException)) {
            document = null;
          }
          else throw new UnexpectedException(e);
        }
        if (document==null) {
          do { curError++;}
          while ((curError < _numErrors) && (_errors[curError].file().equals(file)));
          //If the document couldn't be loaded, start the loop over at the top
          continue;
        }
        if (curError >= _numErrors) break;
        
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
          boolean didNotAdvance = false;
          if (_errors[curError].lineNumber() != curLine) {
            // if this happens, then we will not advance to the next error in the loop below.
            // that means we have to advance curError when we reach the end of the document
            // or we get stuck in an infinite loop (bug 1679178)
            // this seems to be a problem with incompatible line endings (Windows vs. Unix)
            didNotAdvance = true;
          }
          else {
            while ((curError < _numErrors) &&
                   file.equals(_errors[curError].file()) &&  // we are still in this file
                   (_errors[curError].lineNumber() == curLine)) {
              _positions[curError] = document.createPosition(offset + _errors[curError].startColumn());
              curError++;
            }
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
              if (nextNewline == -1) nextNewline = defsText.indexOf("\n", offset);
              if (nextNewline != -1) {
                curLine++;
                offset = nextNewline + 1;
              }
              else {
                // we're at the end of the document
                if (didNotAdvance) {
                  // we did not advance to the next error above, so unless we want to
                  // get stuck in an infinite loop (bug 1679178), we have to advance now.
                  // otherwise we would never leave the while loop and keep processing
                  // the same error.
                  // this situation probably means that the line number information of the
                  // compiler is different from our line number information;
                  // probably a Windows vs. Unix line ending problem
                  _positions[curError] = null;
                  curError++;
                }
              }
            }
          }
        }
        //Remember the indexes in the _errors and _positions arrays that
        // are for the errors in this file
        int fileEndIndex = curError;
        if (fileEndIndex != fileStartIndex) {
          // Try to use the canonical file if possible
          try { file = file.getCanonicalFile(); }
          catch (IOException ioe) { /* Oh well, store it as is */ }
          _filesToIndexes.put(file, new StartAndEndIndex(fileStartIndex, fileEndIndex));
        }
      }
    }
    catch (BadLocationException ble) { throw new UnexpectedException(ble); }
    catch (StringIndexOutOfBoundsException e) { throw new UnexpectedException(e); }
  }
  
  /** Finds the first error after numProcessed which has a file and line number.
    * @param idx the starting index of the search
    * @return the index of the found error
    */
  private int nextErrorWithLine(int idx) {
    while (idx < _numErrors && (_errors[idx].hasNoLocation() || _errors[idx].file() == null)) idx++;
    return idx;
  }
  
  /** This class is used only to track where the errors with positions for a file begin and end.  The beginning index 
    * is inclusive, the ending index is exclusive.
    */
  private static class StartAndEndIndex {
    private int startPos;
    private int endPos;
    
    public StartAndEndIndex(int startPos, int endPos) {
      this.startPos = startPos;
      this.endPos = endPos;
    }
    public int getStartPos() { return startPos; }
    public int getEndPos() { return endPos; }
  }
}
