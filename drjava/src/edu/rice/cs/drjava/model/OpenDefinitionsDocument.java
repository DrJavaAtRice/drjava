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

package edu.rice.cs.drjava.model;

import java.io.*;
import java.awt.print.*;
import javax.swing.text.BadLocationException;
import junit.framework.TestResult;

import edu.rice.cs.util.swing.FindReplaceMachine;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;

/**
 * Interface for the GlobalModel's handler of an open
 * DefinitionsDocument.  Provides a means to interact with
 * the document.
 *
 * @version $Id$
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
   * @exception IOException if a file with errors cannot be opened
   */
  public void startCompile() throws IOException;

  /**
   * Starts testing the source using JUnit.  Demands that the definitions be
   * saved and compiled before proceeding with testing.  Fires the appropriate
   * events as the testing proceeds and finishes.
   * @exception IOException if a file with errors cannot be opened
   * @exception ClassNotFoundException when the class is compiled to a location
   * not on the classpath.
   */
  public TestResult startJUnit() throws ClassNotFoundException, IOException;

  /**
   * Returns the model responsible for maintaining all current errors
   * within this OpenDefinitionsDocument's file.
   */
  public CompilerErrorModel getCompilerErrorModel();

  /**
   * Sets this OpenDefinitionsDocument's notion of all current errors
   * within the corresponding file.
   * @param model CompilerErrorModel containing all errors for this file
   */
  public void setCompilerErrorModel(CompilerErrorModel model);


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

  /**
   *
   */
  public void preparePrintJob() throws BadLocationException;

  public void print() throws PrinterException, BadLocationException;

  public Pageable getPageable() throws IllegalStateException;

  public void cleanUpPrintJob();

}
