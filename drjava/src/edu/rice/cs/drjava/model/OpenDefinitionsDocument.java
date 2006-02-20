/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import edu.rice.cs.util.docnavigation.*;
import java.util.Vector;
import java.io.*;
import java.awt.print.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;

import edu.rice.cs.util.text.AbstractDocumentInterface;
import edu.rice.cs.drjava.model.debug.Breakpoint;
import edu.rice.cs.drjava.model.Finalizable;
import edu.rice.cs.drjava.model.definitions.*;

/**
 * Interface for the GlobalModel's handler of an open
 * DefinitionsDocument.  Provides a means to interact with
 * the document.
 *
 * @version $Id$
 */
public interface OpenDefinitionsDocument extends DJDocument, Finalizable<DefinitionsDocument>,
  Comparable<OpenDefinitionsDocument>, INavigatorItem, AbstractDocumentInterface {

  // The following methods are forwarding methods required by the rest of the
  // program in order for the OpenDefinitionsDocument to handle DefinitionsDocuments
  public int id();
  public int commentLines(int selStart, int selEnd);
  public int uncommentLines(int selStart, int selEnd);
  public boolean getClassFileInSync();
  public int getCurrentCol();
  public int getOffset(int lineNum);
  public String getQualifiedClassName() throws ClassNameNotFoundException;
  public String getQualifiedClassName(int pos) throws ClassNameNotFoundException;
  public CompoundUndoManager getUndoManager();
  public void resetUndoManager();
  public File getCachedClassFile();
  public DocumentListener[] getDocumentListeners();
  public UndoableEditListener[] getUndoableEditListeners();
  
  /** @return whether the undo manager can perform any undos. */
  public boolean undoManagerCanUndo();
  
  /** @return whether the undo manager can perform any redos. */
  public boolean undoManagerCanRedo();
  
  /** Returns the name of the top level class, if any.
   *  @throws ClassNameNotFoundException if no top level class name found.
   */
  public String getFirstTopLevelClassName() throws ClassNameNotFoundException;
  
  /** Determines if this document in the file system tree below the active project root. */
  public boolean isInProjectPath();
  
  /** @return true if the document's file is a project auxiliary file. */
  public boolean isAuxiliaryFile();
  
  /** @return true if the documents file is saved in the current project file. */
  public boolean inProject();
  
  /** Returns whether this document is currently untitled (indicating whether it has a file yet or not).
   *  @return true if the document is untitled and has no file
   */
  public boolean isUntitled();
    
  /** Returns the file for this document.  If the document is untitled, it throws an IllegalStateException.
   *  @return the file for this document
   *  @throws IllegalStateException if document never had a file
   *  @throws FileMovedException if the document's file no longer exists
   */
  public File getFile() throws IllegalStateException, FileMovedException;
  
  /** Returns the file for this document; does not check for null. */
  public File file();
  
  /** Sets this document's file
   *  @param file the file that this OpenDefinitionsDocument is associated with
   */
  public void setFile(File file);

  /** Returns true if the file exists on disk, or if the user has located it on disk. Returns false if the 
   *  file has been moved or deleted
   */
  public boolean fileExists();
  
  /** If the file exists, returns true. If it does not exist, prompts the user to look it up.  If the user
   *  chooses a file, returns true, false otherwise. */
  public boolean verifyExists();  
  
  /** Returns the name of this file, or "(untitled)" if no file. */
  public String getFilename();
  
  /** Returns the parent directory of this file, null if it has none. */
  public File getParentDirectory();

  /** Saves the document with a FileWriter.  If the file name is already set, the method will use that name 
   *  instead of whatever selector is passed in.
   *  @param com a selector that picks the file name
   *  @exception IOException
   *  @return true if the file was saved, false if the operation was canceled
   */
  public boolean saveFile(FileSaveSelector com) throws IOException;

  /** Revert the document to the version saved on disk. */
  public void revertFile() throws IOException;

  /** Saves the document with a FileWriter.  The FileSaveSelector will either provide a file name or prompt 
   *  the user for one.  It is up to the caller to decide what needs to be done to choose a file to save to.  
   *  Once the file has been saved succssfully, this method fires fileSave(File).  If the save fails for any
   *  reason, the event is not fired.
   *  @param com a selector that picks the file name.
   *  @exception IOException
   *  @return true if the file was saved, false if the operation was canceled
   */
  public boolean saveFileAs(FileSaveSelector com) throws IOException;

  /** Starts compiling the source.  Demands that the definitions be saved before proceeding with the compile.  
   *  Fires the appropriate events as the compiliation proceeds and finishes.
   *  @exception IOException if a file with errors cannot be opened
   */
  public void startCompile() throws IOException;

  /** Runs the main method in this document in the interactions pane. Demands that the definitions be saved 
   *  and compiled before proceeding. Fires an event to signal when execution is about to begin.
   *  @exception ClassNameNotFoundException propagated from getFirstTopLevelClass()
   *  @exception IOException propagated from GlobalModel.compileAll()
   */
  public void runMain() throws ClassNameNotFoundException, IOException;

  /** Starts testing the source using JUnit.  Demands that the definitions be saved and compiled before proceeding
   *  with testing.  Fires the appropriate events as the testing proceeds and finishes.
   *  @exception IOException if a file with errors cannot be opened
   *  @exception ClassNotFoundException when the class is compiled to a location not on the classpath.
   */
  public void startJUnit() throws ClassNotFoundException, IOException;

  /** Generates Javadoc for this document, saving the output to a temporary directory.  The location is provided
   *  to the javadocEnded event on the given listener.
   *  @param saver FileSaveSelector for saving the file if it needs to be saved
   */
  public void generateJavadoc(FileSaveSelector saver) throws IOException;

  /** Determines if this definitions document has changed since the last save.
   *  @return true if the document has been modified
   */
  public boolean isModifiedSinceSave();

  /** Determines if this definitions document has changed since the last save.
   *  @return true if the document has been modified
   */
  public boolean isModifiedOnDisk();

  /** Asks the GlobalModel if it can revert current definitions to version on disk. If ok, it reverts the file 
   *  to the version on disk.
   *  @return true if the document has been reverted
   */
  public boolean revertIfModifiedOnDisk() throws IOException;

  /** Returns whether the GlobalModel can abandon this document, asking listeners if isModifiedSinceSave() is true.
   *  @return true if this document can be abandoned
   */
  public boolean canAbandonFile();
  
  /** Saves file at user's discretion before quitting. */
  public void quitFile();

  /** Moves the definitions document to the given line, and returns the resulting character position.
   *  @param line Destination line number. If line exceeds the number of lines in the document, it is interpreted 
   *         as the last line.
   *  @return Index into document of where it moved
   */
  public int gotoLine(int line);

  // unnecessary: only called from DefPane and defPane has reference to mainFrame which deals with commenting code
//  /** A forwarding method to comment out the current line or selection in the definitions. */
//  public void commentLinesInDefinitions(int selStart, int selEnd);
//
//  /** A forwarding method to un-comment the current line or selection in the definitions. */
//  public void uncommentLinesInDefinitions(int selStart, int selEnd);

//  /** Create a find and replace mechanism starting at the current character offset in the definitions. */
//  public FindReplaceMachine createFindReplaceMachine();

  /** Finds the root directory of the source files.
   *  @return The root directory of the source files, based on the package statement.
   *  @throws InvalidPackageException If the package statement is invalid, or if it does not match
   *          up with the location of the source file.
   */
  public File getSourceRoot() throws InvalidPackageException;

  /** Gets the name of the package for this source file (as declared in the source text). It does this by 
   *  minimally parsing the source file to find the package statement.
   *
   *  @return The declared name of package for this source file (the empty string if there is no package 
   *          statement).
   *
   * @exception InvalidPackageException if there is some sort of a
   *                                    <TT>package</TT> statement but it
   *                                    is invalid.
   */
  public String getPackageName() throws InvalidPackageException;
  
  public void preparePrintJob() throws BadLocationException, FileMovedException;

  public void print() throws PrinterException, BadLocationException, FileMovedException;

  public Pageable getPageable() throws IllegalStateException;

  public void cleanUpPrintJob();

  /** Checks if the document is modified. If not, searches for the class file
   *  corresponding to this document and compares the timestamps of the
   *  class file to that of the source file.
   *  @return is the class file and this OpenDefinitionsDocument are in sync
   */
  public boolean checkIfClassFileInSync();

  /** Called when this document is saved so it can notify the cache. */
  public void documentSaved();
  
   /** Called when this document is modified so it can notify the cache. */
  public void documentModified();
  
   /** Called when this document is reset so it can notify the cache. */
  public void documentReset();
    
  /** Returns the Breakpoint in this OpenDefinitionsDocument at the given
   *  linenumber, or null if one does not exist.
   *  @param lineNumber the line number of the breakpoint
   *  @return the Breakpoint at the given lineNumber, or null if it does not exist.
   */
  public Breakpoint getBreakpointAt( int lineNumber);

  /** Add the supplied Breakpoint to the hashtable, keyed by its BreakpointRequest
   *  @param breakpoint the Breakpoint to be inserted into the hashtable
   */
  public void addBreakpoint( Breakpoint breakpoint);

  /** Remove the given Breakpoint from the hashtable.
   *  @param breakpoint the Breakpoint to be removed.
   */
  public void removeBreakpoint( Breakpoint breakpoint);

  /** @return a Vector<Breakpoint> containing the Breakpoint objects that this document contains. */
  public Vector<Breakpoint> getBreakpoints();

  /** Tells the document to remove all breakpoints. */
  public void clearBreakpoints();

  /** Called when this document is being closed, removing related state from the debug manager. */
  public void removeFromDebugger();
  
  /** Resets the document to be unmodified. */
  public void resetModification();
  
  /** Returns the date that this document was last modified. */
  public long getTimestamp();
  
  /** Sets the document as modified. */
  public void updateModifiedSinceSave();

  /** Should be called when closing an ODD to let the ODD clean up after itself. */
  public void close();

//  /**
//   * @return true if the document belongs to this opendefintions document
//   * @param doc the document to test
//   */
//  public boolean belongsHuh(Document doc);
  
  /** @return the initial vertical scroll the pane should use when initialized. */
  public int getInitialVerticalScroll();
  
  /** @return the initial vertical scroll the pane should use when initialized. */
  public int getInitialHorizontalScroll();
  
  /** @return the starting location of the cursor selection that should be set in the pane when 
   *  initialized. */
  public int getInitialSelectionStart();
  
  /** @return the final location of the cursor selection that should be set in the pane when it 
   *  is initialized.
   */
  public int getInitialSelectionEnd();
  
//  /** @return a list of all registered undoable edit listeners */
//  public List<UndoableEditListener> getUndoableEditListeners();

}
