package edu.rice.cs.drjava.model;

import java.io.*;

import edu.rice.cs.util.swing.FindReplaceMachine;
import edu.rice.cs.drjava.model.definitions.*;

/**
 * Interface for the GlobalModel's handler of an open
 * DefinitionsDocument.  Provides a means to interact with
 * the document.
 * @version: $Id$
 */
public interface OpenDefinitionsDocument {

  /**
   * Gets the definitions document being handled.
   * @return document being handled
   */
  public DefinitionsDocument getDocument();

  /**
   * Returns whether this document is currently untitled
   * (indicating whether it has a file yet or not).
   * @return true if the document is untitled and has no file
   */
  public boolean isUntitled();

  /**
   * Returns the file for this document.  If the document
   * is untitled and has no file, it throws an IllegalStateException.
   * @return the file for this document
   * @exception IllegalStateException if no file exists
   */
  public File getFile() throws IllegalStateException;

  /**
   * Saves the document with a FileWriter.  If the file name is already
   * set, the method will use that name instead of whatever selector
   * is passed in.
   * @param com a selector that picks the file name
   * @exception IOException
   */
  public void saveFile(FileSaveSelector com) throws IOException;

  /**
   * Saves the document with a FileWriter.  The FileSaveSelector will
   * either provide a file name or prompt the user for one.  It is
   * up to the caller to decide what needs to be done to choose
   * a file to save to.  Once the file has been saved succssfully,
   * this method fires fileSave(File).  If the save fails for any
   * reason, the event is not fired.
   * @param com a selector that picks the file name.
   * @exception IOException
   */
  public void saveFileAs(FileSaveSelector com) throws IOException;

  /**
   * Called to demand that one or more listeners saves the
   * definitions document before proceeding.  It is up to the caller
   * of this method to check if the document has been saved.
   * Fires saveBeforeProceeding(SaveReason) if isModifiedSinceSave() is true.
   * @param reason the reason behind the demand to save the file
   */
  public void saveBeforeProceeding(final GlobalModelListener.SaveReason reason);

  /**
   * Starts compiling the source.  Demands that the definitions be
   * saved before proceeding with the compile.  Fires the appropriate
   * events as the compiliation proceeds and finishes.
   */
  public void startCompile();


  /**
   * Determines if this definitions document has changed since the
   * last save.
   * @return true if the document has been modified
   */
  public boolean isModifiedSinceSave();

  /**
   * Returns whether the GlobalModel can abandon this document,
   * asking the listeners if isModifiedSinceSave() is true.
   * @return true if this document can be abandoned
   */
  public boolean canAbandonFile();

  /**
   * Moves the definitions document to the given line, and returns
   * the character position in the document it's gotten to.
   * @param line Number of the line to go to. If line exceeds the number
   *             of lines in the document, it is interpreted as the last line.
   * @return Index into document of where it moved
   */
  public int gotoLine(int line);

  /**
   * Forwarding method to sync the definitions with whatever view
   * component is representing them.
   */
  public void syncCurrentLocationWithDefinitions(int location);

  /**
   * Get the location of the cursor in the definitions according
   * to the definitions document.
   */
  public int getCurrentDefinitionsLocation();

  /**
   * Forwarding method to find the match for the closing brace
   * immediately to the left, assuming there is such a brace.
   * @return the relative distance backwards to the offset before
   *         the matching brace.
   */
  public int balanceBackward();

  /**
   * Set the indent tab size for this document.
   * @param indent the number of spaces to make per level of indent
   */
  public void setDefinitionsIndent(int indent);

  /**
   * A forwarding method to indent the current line or selection
   * in the definitions.
   */
  public void indentLinesInDefinitions(int selStart, int selEnd);

  /**
   * Create a find and replace mechanism starting at the current
   * character offset in the definitions.
   */
  public FindReplaceMachine createFindReplaceMachine();

  /**
   * Finds the root directory of the source files.
   * @return The root directory of the source files,
   *         based on the package statement.
   * @throws InvalidPackageException If the package statement is invalid,
   *                                 or if it does not match up with the
   *                                 location of the source file.
   */
  public File getSourceRoot() throws InvalidPackageException;

}
