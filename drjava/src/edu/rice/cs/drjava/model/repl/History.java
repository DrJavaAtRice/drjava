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

package edu.rice.cs.drjava.model.repl;

import java.util.ArrayList;
import java.util.HashMap;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.util.OperationCanceledException;

import java.io.Serializable;
import java.io.IOException;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.BufferedWriter;

/** History class that records what has been typed in the interactions pane.  This class is not thread safe;
  * it is only accessed from InteractionsDocument which takes responsibility for synchronization.
  * @version $Id: History.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class History implements OptionConstants, Serializable {

  public static final String INTERACTION_SEPARATOR = "//End of Interaction//";

  // Not final because it may be updated by config
  private volatile int _maxSize;

  /** Version flag at the beginning of saved history file format
   * If this is not present in a saved history, it is assumed to be the original format.
   */
  public static final String HISTORY_FORMAT_VERSION_2 = "// DrJava saved history v2" + StringOps.EOL;

  private final ArrayList<String> _vector = new ArrayList<String>();
  private volatile int _cursor = -1;

  /** A hashmap for edited entries in the history. */
  private final HashMap<Integer, String> _editedEntries = new HashMap<Integer, String>();

  /** A placeholder for the current search string. */
  private volatile String _currentSearchString = "";
  
   /** The OptionListener for HISTORY_MAX_SIZE */
  public final OptionListener<Integer> historyOptionListener = new OptionListener<Integer>() {
    public void optionChanged (OptionEvent<Integer> oce) {
      int newSize = oce.value;
//      System.err.println("optionChanged called for historyOptionListener; newSize = " + newSize);
      setMaxSize(newSize);
    }
    public String toString() { return "HISTORY_MAX_SIZE OptionListener #" + hashCode(); }
  };
    

  /** Constructor, so we can add a listener to the Config item being used. */
  public History() {
    this(DrJava.getConfig().getSetting(HISTORY_MAX_SIZE));
    // the reference to historyOptionListener below is delicate
    DrJava.getConfig().addOptionListener(HISTORY_MAX_SIZE, historyOptionListener);  
  }

  /** Creates a new History with the given size.  An option listener is not added for the config framework.
    * @param maxSize Number of lines to remember in the history.
    */
  public History(int maxSize) {
    if (maxSize < 0) maxSize = 0;   // Sanity check on _maxSize
    _maxSize = maxSize;
  }
  
  /* Getter for historyOptionListener. */  
  public OptionListener<Integer> getHistoryOptionListener() { return historyOptionListener; }

  /** Sets the edited entry to the given value.
    * @param entry the string to set
    */
  public void setEditedEntry(String entry) {
    if (! entry.equals(getCurrent())) _editedEntries.put(Integer.valueOf(_cursor), entry);
  }

  /** Adds an item to the history and moves the cursor to point to the place after it.
    * Note: Items are not inserted if they are empty. (This is in accordance with
    * bug #522123, but in divergence from feature #522213 which originally excluded
    * sequential duplicate entries from ever being stored.)
    *
    * Thus, to access the newly inserted item, you must movePrevious first.
    */
  public void add(String item) {
    // for consistency in saved History files, WILL save sequential duplicate entries
    if (item.trim().length() > 0) {
      _vector.add(item);
      // If max size of _vector is exceeded, spill the oldest element out of the History.
      if (_vector.size() > _maxSize) _vector.remove(0);

      moveEnd();
      _editedEntries.clear();
    }
  }
  
  /** Returns the last element and removes it, or returns null if the history is empty.
    * @return last element before it was removed, or null if history is empty
    */
  public String removeLast() {
    if (_vector.size() == 0) { return null; }
    String last = _vector.remove(_vector.size()-1);
    if (_cursor > _vector.size()) { _cursor = _vector.size()-1; }
    return last;
  }

  /** Move the cursor to just past the end. Thus, to access the last element, you must movePrevious. */
  public void moveEnd() { _cursor = _vector.size(); }

  /** Moves cursor back 1, or throws exception if there is none.
    * @param entry the current entry (perhaps edited from what is in history)
    */
  public void movePrevious(String entry) {
    if (! hasPrevious()) throw new ArrayIndexOutOfBoundsException();
    setEditedEntry(entry);
    _cursor--;
  }
  
  /** Returns the last entry from the history. Throw array indexing exception if no such entry. */
  public String lastEntry() { return _vector.get(_cursor - 1); }

  /** Moves cursor forward 1, or throws exception if there is none.
    * @param entry the current entry (perhaps edited from what is in history)
    */
  public void moveNext(String entry) {
    if (! hasNext()) throw  new ArrayIndexOutOfBoundsException();
    setEditedEntry(entry);
    _cursor++;
  }

  /** Returns whether moveNext() would succeed right now. */
  public boolean hasNext() { return  _cursor < (_vector.size()); }

  /** Returns whether movePrevious() would succeed right now. */
  public boolean hasPrevious() { return  _cursor > 0; }

  /** Returns item in history at current position; returns "" if no current item exists. */
  public String getCurrent() {
    Integer cursor = Integer.valueOf(_cursor);
    if (_editedEntries.containsKey(cursor))  return _editedEntries.get(cursor);

    if (hasNext()) return _vector.get(_cursor);
    return "";
  }

  /** Returns the number of items in this History. */
  public int size() { return _vector.size(); }

  /** Clears the vector */
  public void clear() { _vector.clear(); }

  /** Returns the history as a string by concatenating each string in the vector separated by the delimiting
    * character. A semicolon is added to the end of every statement that didn't already end with one.
    */
  public String getHistoryAsStringWithSemicolons() {
    final StringBuilder s = new StringBuilder();
    final String delimiter = INTERACTION_SEPARATOR + StringOps.EOL;
    for (int i = 0; i < _vector.size(); i++) {
      String nextLine = _vector.get(i);
//      int nextLength = nextLine.length();
//      if ((nextLength > 0) && (nextLine.charAt(nextLength-1) != ';')) {
//        nextLine += ";";
//      }
//      s += nextLine + delimiter;
      s.append(nextLine);
      s.append(delimiter);
    }
    return s.toString();
  }

  /** Returns the history as a string by concatenating the lines in _vector with EOL as separator. */
  public String getHistoryAsString() {
    final StringBuilder sb = new StringBuilder();
    final String delimiter = StringOps.EOL;
    for (String s: _vector) sb.append(s).append(delimiter);
    return sb.toString();
  }

  /** Writes this (unedited) History to the file selected in the FileSaveSelector.
    * @param selector File to save to
    */
  public void writeToFile(FileSaveSelector selector) throws IOException {
    writeToFile(selector, getHistoryAsStringWithSemicolons());
  }

  /** Writes this History to the file selected in the FileSaveSelector. The saved file will still include
    * any tags or extensions needed to recognize it as a saved interactions file.
    * @param selector File to save to
    * @param editedVersion The edited version of the text to be saved (which already uses proper EOL string)
    */
  public static void writeToFile(FileSaveSelector selector, final String editedVersion) throws IOException {
    File c;
    
    try { c = selector.getFile(); }
    catch (OperationCanceledException oce) { return; }
    
    // Make sure we ask before overwriting
    if (c != null) {
      if (! c.exists() || selector.verifyOverwrite(c)) {
        FileOps.DefaultFileSaver saver = new FileOps.DefaultFileSaver(c) {
          public void saveTo(OutputStream os) throws IOException {

            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            String file = HISTORY_FORMAT_VERSION_2 + editedVersion;
            bw.write(file, 0, file.length());
            bw.close();
          }
        };
        FileOps.saveFile(saver);
      }
    }
  }

  /** Changes the maximum number of interactions remembered by this History.
    * @param newSize New number of interactions to remember.
    */
  public void setMaxSize(int newSize) {
    if (newSize < 0) newSize = 0;    // Sanity check

    // Remove old elements if the new size is less than current size
    if (size() > newSize) {

      int numToDelete = size() - newSize;
      for (int i = 0; i < numToDelete; i++) { _vector.remove(0); }

      moveEnd();
    }
    _maxSize = newSize;
  }

  /** Reverse-searches the history for the previous matching string.
    * @param currentInteraction the current interaction
    */
  public void reverseSearch(String currentInteraction) {
    if (_currentSearchString.equals("") || ! currentInteraction.startsWith(_currentSearchString))
      _currentSearchString = currentInteraction;

    setEditedEntry(currentInteraction);
    while (hasPrevious()) {
      movePrevious(getCurrent());
      if (getCurrent().startsWith(_currentSearchString, 0)) break;
    }
    
    if (! getCurrent().startsWith(_currentSearchString, 0))  moveEnd();
  }

  /** Forward-searches the history for the next matching string.
    * @param currentInteraction the current interaction
    */
  public void forwardSearch(String currentInteraction) {
    if (_currentSearchString.equals("") || ! currentInteraction.startsWith(_currentSearchString))
      _currentSearchString = currentInteraction;

    setEditedEntry(currentInteraction);
    while (hasNext()) {
      moveNext(getCurrent());
      if (getCurrent().startsWith(_currentSearchString, 0))  break;
    }
    
    if (! getCurrent().startsWith(_currentSearchString, 0)) moveEnd();
  }
}
