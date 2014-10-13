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

package edu.rice.cs.drjava.model;


import java.io.*;
import java.awt.print.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.util.docnavigation.*;
import edu.rice.cs.drjava.model.debug.Breakpoint;
import edu.rice.cs.drjava.model.Finalizable;
import edu.rice.cs.drjava.model.definitions.*;

/** Interface for an open document in DrScala. It may not be resident (in DocumentCache).
  * @version $Id: OpenDefinitionsDocument.java 5727 2012-09-30 03:58:32Z rcartwright $
  */
public interface OpenDefinitionsDocument extends DJDocument, Finalizable<DefinitionsDocument>,
  Comparable<OpenDefinitionsDocument>, INavigatorItem {
  
  //----- Forwarding Methods -----/

  /** The following methods are forwarding methods required by the rest of the program in order for the 
   *  OpenDefinitionsDocument to handle DefinitionsDocuments */
//  public int id();
  public int commentLines(int selStart, int selEnd);
  public int uncommentLines(int selStart, int selEnd);
  /** Gets the associated DefinitionsDocument which may force materializing it. */
  public DefinitionsDocument getDocument();
  public boolean getClassFileInSync();
  public void setClassFileInSync(boolean val);
  public int getCurrentLine();
  public int getCurrentCol();
  public int _getOffset(int lineNum);
  public String getQualifiedClassName() throws ClassNameNotFoundException;
  public String getQualifiedClassName(int pos) throws ClassNameNotFoundException;
  public String getLexiName();
  public CompoundUndoManager getUndoManager();
  public void resetUndoManager();
  public File getCachedClassFile();
  public void setCachedClassFile(File f);
  public DocumentListener[] getDocumentListeners();
  public UndoableEditListener[] getUndoableEditListeners();
  
  //----- Regular methods -----/
  
  //----- Getters and Setters -----/
  
  /** Returns the file for this document; does not check whether the file exists. */
  public File getRawFile();
  
  /** Returns the file for this document. 
   *  @return the file for this document
   *  @throws FileMovedException if the document's file no longer exists
   */
  public File getFile() throws FileMovedException;

  /** Sets this document's file
   *  @param file the file that this OpenDefinitionsDocument is associated with
   */
  public void setFile(File file);
  
  /** Returns the name of this file, or "(Untitled)" if no file. */
  public String getFileName();
  
  /** Returns canonical path for well-formed file, "(Untitled)" if no file exists, and absolute path if ill-formed. */
  public String getCanonicalPath(); 

  /** Returns canonical path (as defined above) followed by " *" if modified. */
  public String getCompletePath(); 

  /** Returns the parent directory of this file, null if it has none. */
  public File getParentDirectory();

  public Pageable getPageable() throws IllegalStateException;
  
  //----- Simple Predicates -----//
  
  /** @return whether the undo manager can perform any undos. */
  public boolean undoManagerCanUndo();
  
  /** @return whether the undo manager can perform any redos. */
  public boolean undoManagerCanRedo();
  
  /** Determines if this document in the file system tree below the active project root. */
  public boolean inProjectPath();
  
  /** Determines if this document in the file system tree below the specified root. */
  public boolean inNewProjectPath(File root);
  
  /** Determines if the document is empty. */
  public boolean isEmpty();
  
  /** @return true if the document's file is a project auxiliary file. */
  public boolean isAuxiliaryFile();
  
  /** @return true if the document's filename ends with the extension ".java", ".dj", ".dj0", "dj1", or "dj2". */
  public boolean isSourceFile();
  
  /** @return true if the documents file is saved in the current project file. */
  public boolean inProject();
  
  /** Returns whether this document is resident in memory. */ 
  public boolean isReady();
  
  /** Returns whether this document is currently untitled (indicating whether it has a file yet or not).
   *  @return true if the document is untitled and has no file
   */
  public boolean isUntitled();
 
  /** Returns true if the file exists on disk, or if the user has located it on disk. Returns false if the 
   *  file has been moved or deleted
   */
  public boolean fileExists();
  
  /** Determines if this definitions document has changed since the last save.
   *  @return true if the document has been modified
   */
  public boolean modifiedOnDisk();
  
  /** Resets the document to be unmodified. */
  public void resetModification();
  
  /** Returns the date that this document was last modified. */
  public long getTimestamp();
  
  
  //----- Major Operations -----//
  
  /** Adds region r to the browserRegions collection for this */
  public void addBrowserRegion(BrowserDocumentRegion r);
  
  /** Removes region r from the browserRegions collection */
  public void removeBrowserRegion(BrowserDocumentRegion r);
  
  /** Returns the name of the top level class, if any.
    * @throws ClassNameNotFoundException if no top level class name found.
    */
  public String getFirstTopLevelClassName() throws ClassNameNotFoundException;
  
  /** If the file exists, returns true. If it does not exist, prompts the user to look it up.  If the user
    * chooses a file, returns true, false otherwise. */
  public boolean verifyExists();  
  
  /** Saves the document with a FileWriter.  If the file name is already set, the method will use that name 
    * instead of whatever selector is passed in.
    * @param com a selector that picks the file name
    * @exception IOException
    * @return true if the file was saved, false if the operation was canceled
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
   *  Fires the appropriate events as the compiliation proceeds and finishes.  Only executes in the event thread.
   *  @exception IOException if a file with errors cannot be opened
   */
  public void startCompile() throws IOException;

  /** Runs the main method in this document in the interactions pane. Demands that the definitions be saved 
   *  and compiled before proceeding. Fires an event to signal when execution is about to begin.
   * 
   *  @param qualifiedClassName - the fully qualified name of the class contained in this document to run the main(String[]) method of
   *  @exception ClassNameNotFoundException propagated from getFirstTopLevelClass()
   *  @exception IOException propagated from GlobalModel.compileAll()
   */
  public void runMain(String qualifiedClassName) throws ClassNameNotFoundException, IOException;

  /** Runs this document as applet in the interactions pane. Demands that the definitions be saved 
   *  and compiled before proceeding. Fires an event to signal when execution is about to begin.
   *  The class that will be run must have a default (zero-argument) constructor.
   * 
   *  @param qualifiedClassName - the fully qualified name of the class contained in this document that will be run as applet
   *  @exception ClassNameNotFoundException propagated from getFirstTopLevelClass()
   *  @exception IOException propagated from GlobalModel.compileAll()
   */
  public void runApplet(String qualifiedClassName) throws ClassNameNotFoundException, IOException;

  /** Runs this document, and tries to be smart about it. It detects if the class is a regular Java class with a
    * main method, if it is an applet, or if it is an ACM Java Task Force program. It runs the program appropriately
    * in the interactions pane after resetting interactions with the source root for this document as the
    * working directory.  Warns the use if the class files for the doucment are not up to date.
    * Fires an event to signal when execution is about to begin.
    * NOTE: this code normally runs in the event thread; it cannot block waiting for an event that is triggered by
    * event thread execution!
    * 
    * @param qualifiedClassName  the qualified name of the class (in this document) to run.  If NULL, it is the name
    *                            of the top level class.
    * 
    * @exception ClassNameNotFoundException propagated from getFirstTopLevelClass()
    * @exception IOException propagated from GlobalModel.compileAll()
    */
  public void runSmart(String qualifiedClassName) throws ClassNameNotFoundException, IOException;

  /** Starts testing the source using JUnit.  Demands that the definitions be saved and compiled before proceeding
   *  with testing.  Fires the appropriate events as the testing proceeds and finishes.
   *  TODO: this method is redundant and should be deprecated
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

  /** Asks the GlobalModel if it can revert current definitions to version on disk. If ok, it reverts the file 
   *  to the version on disk.
   *  @return true if the document has been reverted
   */
  public boolean revertIfModifiedOnDisk() throws IOException;

  /** Returns whether the GlobalModel can abandon this document, asking listeners if isModifiedSinceSave() is true.
   *  @return true if this document can be abandoned
   */
  public boolean canAbandonFile();
  
  /** Saves file at user's discretion before quitting.
   *  @return true if quitting should continue, false if the user cancelled */
  public boolean quitFile();

  /** Moves the definitions document to the given line, and returns the resulting character position.
   *  @param line Destination line number. If line exceeds the number of lines in the document, it is interpreted 
   *         as the last line.
   *  @return Index into document of where it moved
   */
  public int gotoLine(int line);

  /** Finds the root directory of the source files.
   *  @return The root directory of the source files, based on the package statement.
   *  @throws InvalidPackageException If the package statement is invalid, or if it does not match
   *          up with the location of the source file.
   */
  public File getSourceRoot() throws InvalidPackageException;
  
  /**  @return the name of the package currently embedded in document. Forwards to wrapped DefinitionsDocument. */
  public String getPackageNameFromDocument();

  /**  @return the name of the package at the time of the most recent save or load operation. */
  public String getPackageName();
  
  /** Sets the cached package name returned by getPackageName(); */
  public void setPackage(String s);
  
  /** Searching backwards finds the name of the enclosing named class or interface. NB: ignores comments.
   *  WARNING: In long source files and when contained in anonymous inner classes, this function might take a LONG time.
   *  @param pos Position to start from
   *  @param qual true to find the fully qualified class name
   *  @return name of the enclosing named class or interface
   */
  public String getEnclosingClassName(int pos, boolean qual) throws BadLocationException, ClassNameNotFoundException;

  public void preparePrintJob() throws BadLocationException, FileMovedException;

  public void print() throws PrinterException, BadLocationException, FileMovedException;

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
    
  /** @return the breakpoint region manager. */
  public RegionManager<Breakpoint> getBreakpointManager();
    
  /** @return the bookmark region manager. */
  public RegionManager<MovingDocumentRegion> getBookmarkManager();
    
//  /** @return managers for find result regions. */
//  public List<RegionManager<MovingDocumentRegion>> getFindResultsManagers();
  
//  /** Add a region manager for find results to this document.
//   *  @param rm the global model's region manager */
//  public void addFindResultsManager(RegionManager<MovingDocumentRegion> rm);
  
//  /** Remove a manager for find results from this document.
//   *  @param rm the global model's region manager. */
//  public void removeFindResultsManager(RegionManager<MovingDocumentRegion> rm);
  
  /** Remove the browser regions for this document from the browser history manager. */
  public void clearBrowserRegions();

  /** Called when this document is being closed, removing related state from the debug manager. */
  public void removeFromDebugger();
  
  /** Sets the document as modified. */
  public void updateModifiedSinceSave();

  /** Should be called when closing an ODD to let the ODD clean up after itself. */
  public void close();
  
  /** @return the initial vertical scroll the pane should use when initialized. */
  public int getInitialVerticalScroll();
  
  /** @return the initial vertical scroll the pane should use when initialized. */
  public int getInitialHorizontalScroll();
  
  /** @return the starting location of the cursor selection that should be set in the pane when initialized. */
  public int getInitialSelectionStart();
  
  /** @return the final location of the cursor selection that should be set in the pane when it is initialized. */
  public int getInitialSelectionEnd();
  
  /** @return the number of lines in this document. */
  public int getNumberOfLines();
  
  /** Translates an offset into the components text to a line number.
   *  @param offset the offset >= 0
   *  @return the line number >= 0 */
  public int getLineOfOffset(int offset);
  
  /** Translates a line number into an offset.
    * @param line number >= 0
    * @return offset >= 0 
    */
  public int getOffsetOfLine(int line);
  
  /** @return the caret position as set by the view. */
  public int getCaretPosition();
  
  /** Creates a WrappedPosition in the document. */
  public Position createUnwrappedPosition(int offs) throws BadLocationException;
  
  /** Determines if pos in document is inside a comment or a string. */
  public boolean isShadowed(int pos);

  /** Returns true if one of the words 'class', 'interface' or 'enum' is found
    * in non-comment text. */
  public boolean containsSource() throws BadLocationException;
  
  /** Update the syntax highlighting for the file type. */
  public void updateSyntaxHighlighting();
}
