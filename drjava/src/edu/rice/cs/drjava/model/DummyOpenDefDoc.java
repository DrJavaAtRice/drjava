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

package edu.rice.cs.drjava.model;

import java.util.Vector;
import java.util.List;
import java.io.*;
import java.awt.print.*;
import java.awt.*;
//import javax.swing.ProgressMonitor;
import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.*;
import junit.framework.TestResult;

import edu.rice.cs.util.swing.FindReplaceMachine;
import edu.rice.cs.drjava.model.debug.Breakpoint;
import edu.rice.cs.drjava.model.junit.*;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import edu.rice.cs.drjava.model.Finalizable;
import edu.rice.cs.drjava.model.FinalizationListener;
import edu.rice.cs.drjava.model.FinalizationEvent;
import edu.rice.cs.util.docnavigation.*;

public class DummyOpenDefDoc implements OpenDefinitionsDocument {  
  /**
   * Determines if this definitions document has changed since the
   * last save.
   * @return true if the document has been modified
   */
  public boolean isModifiedOnDisk() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Saves the document with a FileWriter.  If the file name is already
   * set, the method will use that name instead of whatever selector
   * is passed in.
   * @param com a selector that picks the file name
   * @exception IOException
   * @return true if the file was saved, false if the operation was canceled
   */
  public boolean saveFile(FileSaveSelector com) throws IOException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * A forwarding method to un-comment the current line or selection
   * in the definitions.
   */
  public void uncommentLinesInDefinitions(int selStart, int selEnd) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Returns whether the GlobalModel can abandon this document,
   * asking the listeners if isModifiedSinceSave() is true.
   * @return true if this document can be abandoned
   */
  public boolean canAbandonFile() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Forwarding method to sync the definitions with whatever view
   * component is representing them.
   */
  public void setCurrentLocation(int location) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Gets the definitions document being handled.
   * @return document being handled
   */
  protected DefinitionsDocument getDocument() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Determines if this definitions document has changed since the
   * last save.
   * @return true if the document has been modified
   */
  public boolean isModifiedSinceSave() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Forwarding method to find the match for the open brace
   * immediately to the right, assuming there is such a brace.
   * @return the relative distance forwards to the offset after
   *         the matching brace.
   */
  public int balanceForward() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Returns the file for this document.  If the document
   * is untitled and has no file, it throws an IllegalStateException.
   * @return the file for this document
   * @throws IllegalStateException if document never had a file
   * @throws FileMovedException if the document's file no longer exists
   */
  public File getFile() throws IllegalStateException, FileMovedException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Returns true if the file exists on disk. Returns false if the file has been moved or deleted
   */
  public boolean fileExists() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Returns true if the file exists on disk. Prompts the user otherwise
   */
  public boolean verifyExists() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Method cleanUpPrintJob
   *
   */
  public void cleanUpPrintJob() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Returns the name of the top level class, if any.
   * @throws ClassNameNotFoundException if no top level class name found.
   */
  public String getFirstTopLevelClassName() throws ClassNameNotFoundException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Starts compiling the source.  Demands that the definitions be
   * saved before proceeding with the compile.  Fires the appropriate
   * events as the compiliation proceeds and finishes.
   * @exception IOException if a file with errors cannot be opened
   */
  public void startCompile() throws IOException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Runs the main method in this document in the interactions pane.
   * Demands that the definitions be saved and compiled before proceeding.
   * Fires an event to signal when execution is about to begin.
   * @exception IOException propagated from GlobalModel.compileAll()
   */
  public void runMain() throws IOException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * A forwarding method to indent the current line or selection
   * in the definitions.
   */
  public void indentLinesInDefinitions(int selStart, int selEnd,
                                       int reason, ProgressMonitor pm)
      throws OperationCanceledException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Asks the GlobalModel if it can revert current definitions
   * to version on disk. If ok, it reverts the file to the
   * version on disk.
   * @return true if the document has been reverted
   */
  public boolean revertIfModifiedOnDisk() throws IOException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Method getPageable
   *
   * @return   a Pageable
   *
   * @throws   IllegalStateException
   *
   */
  public Pageable getPageable() throws IllegalStateException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Moves the definitions document to the given line, and returns
   * the character position in the document it's gotten to.
   * @param line Number of the line to go to. If line exceeds the number
   *             of lines in the document, it is interpreted as the last line.
   * @return Index into document of where it moved
   */
  public int gotoLine(int line) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Method print
   *
   * @throws   PrinterException
   * @throws   BadLocationException
   * @throws   FileMovedException
   *
   */
  public void print() throws PrinterException, BadLocationException, FileMovedException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Called to indicate the document is being closed, so to remove
   * all related state from the debug manager.
   */
  public void removeFromDebugger() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Remove the given Breakpoint from the hashtable.
   * @param breakpoint the Breakpoint to be removed.
   */
  public void removeBreakpoint(Breakpoint breakpoint) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Returns whether this document is currently untitled
   * (indicating whether it has a file yet or not).
   * @return true if the document is untitled and has no file
   */
  public boolean isUntitled() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Create a find and replace mechanism starting at the current
   * character offset in the definitions.
   * NOT USED.
   */
//  public FindReplaceMachine createFindReplaceMachine() {
//    throw new UnsupportedOperationException("Dummy method");
//  }
  
  /**
   * Finds the root directory of the source files.
   * @return The root directory of the source files,
   *         based on the package statement.
   * @throws InvalidPackageException If the package statement is invalid,
   *                                 or if it does not match up with the
   *                                 location of the source file.
   */
  public File getSourceRoot() throws InvalidPackageException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Returns the Breakpoint in this OpenDefinitionsDocument at the given
   * linenumber, or null if one does not exist.
   * @param lineNumber the line number of the breakpoint
   * @return the Breakpoint at the given lineNumber, or null if it does not exist.
   */
  public Breakpoint getBreakpointAt(int lineNumber) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Returns the name of this file, or "(untitled)" if no file.
   */
  public String getFilename() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Add the supplied Breakpoint to the hashtable, keyed by its BreakpointRequest
   * @param breakpoint the Breakpoint to be inserted into the hashtable
   */
  public void addBreakpoint(Breakpoint breakpoint) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Returns a Vector<Breakpoint> that contains all of the Breakpoint objects that
   * this document contains
   */
  public Vector<Breakpoint> getBreakpoints() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Starts testing the source using JUnit.  Demands that the definitions be
   * saved and compiled before proceeding with testing.  Fires the appropriate
   * events as the testing proceeds and finishes.
   * @exception IOException if a file with errors cannot be opened
   * @exception ClassNotFoundException when the class is compiled to a location
   * not on the classpath.
   */
  public void startJUnit() throws ClassNotFoundException, IOException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Generates Javadoc for this document, saving the output to a temporary
   * directory.  The location is provided to the javadocEnded event on
   * the given listener.
   * @param saver FileSaveSelector for saving the file if it needs to be saved
   */
  public void generateJavadoc(FileSaveSelector saver) throws IOException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Gets the name of the package this source file claims it's in (with the
   * package keyword). It does this by minimally parsing the source file
   * to find the package statement.
   *
   * @return The name of package this source file declares itself to be in,
   *         or the empty string if there is no package statement (and thus
   *         the source file is in the empty package).
   *
   * @exception InvalidPackageException if there is some sort of a
   *                                    <TT>package</TT> statement but it
   *                                    is invalid.
   */
  public String getPackageName() throws InvalidPackageException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Tells the document to remove all breakpoints
   */
  public void clearBreakpoints() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * A forwardidefDocng method to comment out the current line or selection
   * in the definitions.
   */
  public void commentLinesInDefinitions(int selStart, int selEnd) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Saves the document with a FileWriter.  The FileSaveSelector will
   * either provide a file name or prompt the user for one.  It is
   * up to the caller to decide what needs to be done to choose
   * a file to save to.  Once the file has been saved succssfully,
   * this method fires fileSave(File).  If the save fails for any
   * reason, the event is not fired.
   * @param com a selector that picks the file name.
   * @exception IOException
   * @return true if the file was saved, false if the operation was canceled
   */
  public boolean saveFileAs(FileSaveSelector com) throws IOException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   *
   */
  public void preparePrintJob() throws BadLocationException, FileMovedException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Set the indent tab size for this document.
   * @param indent the number of spaces to make per level of indent
   */
  public void setDefinitionsIndent(int indent) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Forwarding method to find the match for the closing brace
   * immediately to the left, assuming there is such a brace.
   * @return the relative distance backwards to the offset before
   *         the matching brace.
   */
  public int balanceBackward() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Revert the document to the version saved on disk.
   */
  public void revertFile() throws IOException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Checks if the document is modified. If not, searches for the class file
   * corresponding to this document and compares the timestamps of the
   * class file to that of the source file.
   * @return is the class file and this OpenDefinitionsDocument are in sync
   */
  public boolean checkIfClassFileInSync() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Get the location of the cursor in the definitions according
   * to the definitions document.
   */
  public int getCurrentLocation() {
    throw new UnsupportedOperationException("Dummy method");
  }
    
  /**
   * @return the INavigatorItem representing this object
   */
  public INavigatorItem getIDoc() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * resets the document to be unmodified
   */
  public void resetModification(){
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * returns the date that this document was last modified
   */
  public long getTimestamp(){
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * sets the document as modified
   */
  public void setModifiedSinceSave(){
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * sets the file for this openDefinitionsDocument
   */
  public void setFile(File file) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * cleans up the doc before closing it
   */
  public void close() {
    // no body
  }
  
  public boolean isInProjectPath(){
    return false;
  }

  public boolean isProjectFile(){
    return false;
  }

  public boolean belongsHuh(Document doc){
    return false;
  }
  
  public int getLineStartPos(int pos){
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public int getLineEndPos(int pos){
    throw new UnsupportedOperationException("Dummy method");
  }
  
  /**
   * Implementation of the javax.swing.text.Document interface
   */
  protected Document _plainDocument = new PlainDocument();
  
  public void addDocumentListener(DocumentListener listener){
    _plainDocument.addDocumentListener(listener);
  }
  
  public void addUndoableEditListener(UndoableEditListener listener){
    _plainDocument.addUndoableEditListener(listener);
  }
  
  public Position createPosition(int offs) throws BadLocationException{
    return _plainDocument.createPosition(offs);
  }
  
  public Element getDefaultRootElement(){
    return _plainDocument.getDefaultRootElement();
  }
  
  public Position getEndPosition() {
    return _plainDocument.getEndPosition();
  }
  
  public int getLength(){
    return _plainDocument.getLength();
  }
  
  public Object getProperty(Object key){
    return _plainDocument.getProperty(key);
  }
  
  public Element[] getRootElements(){
    return _plainDocument.getRootElements();
  }
  
  public Position getStartPosition(){
    return _plainDocument.getStartPosition();
  }
  
  public String getText(int offset, int length) throws BadLocationException{
    return _plainDocument.getText(offset, length);
  }
  
  public void getText(int offset, int length, Segment txt) throws BadLocationException{
    _plainDocument.getText(offset, length, txt);
  }
  
  public void insertString(int offset, String str, AttributeSet a) throws BadLocationException{
    _plainDocument.insertString(offset, str, a);
  }
  
  public void putProperty(Object key, Object value){
    _plainDocument.putProperty(key, value);
  }
  
  public void remove(int offs, int len) throws BadLocationException{
    _plainDocument.remove(offs, len);
  }
  
  public void removeDocumentListener(DocumentListener listener) {
    _plainDocument.removeDocumentListener(listener);
  }
  
  public void removeUndoableEditListener(UndoableEditListener listener){
    _plainDocument.removeUndoableEditListener(listener);
  }
  
  public void render(Runnable r) {
    _plainDocument.render(r);
  }
  
  /**
   * end implementation of javax.swing.text.Document interface
   */
  
  
  
  /**
   * decorater patter for the definitions document
   */
  public CompoundUndoManager getUndoManager(){
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public void commentLines(int selStart, int selEnd){
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public void uncommentLines(int selStart, int selEnd){
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public void indentLines(int selStart, int selEnd){
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public int getCurrentCol() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public boolean getClassFileInSync(){
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public int getIntelligentBeginLinePos(int currPos) throws BadLocationException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public int getOffset(int lineNum) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public String getQualifiedClassName() throws ClassNameNotFoundException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public String getQualifiedClassName(int pos) throws ClassNameNotFoundException {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public ReducedModelState getStateAtCurrent(){
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public void resetUndoManager() {
    throw new UnsupportedOperationException("Dummy method");
  }

  public File getCachedClassFile() {
    throw new UnsupportedOperationException("Dummy method");
  }

  public DocumentListener[] getDocumentListeners() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public UndoableEditListener[] getUndoableEditListeners() {
    throw new UnsupportedOperationException("Dummy method");
  }
    
  public void addFinalizationListener(FinalizationListener<DefinitionsDocument> fl) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public List<FinalizationListener<DefinitionsDocument>> getFinalizationListeners(){
    throw new UnsupportedOperationException("Dummy method");
  }

  public boolean undoManagerCanUndo() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public boolean undoManagerCanRedo() {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  // Styled Document Methods 
  public Font getFont(AttributeSet attr) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public Color getBackground(AttributeSet attr) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public Color getForeground(AttributeSet attr) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public Element getCharacterElement(int pos) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public Element getParagraphElement(int pos) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  
  public Style getLogicalStyle(int p) {
    throw new UnsupportedOperationException("Dummy method");
  }
   
  public void setLogicalStyle(int pos, Style s) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace) {
    throw new UnsupportedOperationException("Dummy method");
  }    
  
  public void setParagraphAttributes(int offset, int length, AttributeSet s, boolean replace) {
    throw new UnsupportedOperationException("Dummy method");
  }    
  
  public Style getStyle(String nm) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public void removeStyle(String nm) {
    throw new UnsupportedOperationException("Dummy method");
  }
  
  public Style addStyle(String nm, Style parent) {
     throw new UnsupportedOperationException("Dummy method");
  }
}
