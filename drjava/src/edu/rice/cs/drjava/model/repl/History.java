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

package edu.rice.cs.drjava.model.repl;

import gj.util.Vector;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.ui.InteractionsHistoryFilter;
import java.io.*;
import javax.swing.*;

/**
 * Keeps track of what was typed in the interactions pane.
 * @version $Id$
 */
public class History implements OptionConstants {
  
  // Not final because it may be updated by config
  private static int MAX_SIZE = DrJava.getConfig().getSetting(HISTORY_MAX_SIZE).intValue();
  
  /**
   * Version flag at the beginning of saved history file format
   * If this is not present in a saved history, it is assumed to be the original format.
   */
  public static final String HISTORY_FORMAT_VERSION_2 = "// DrJava saved history v2\n";
    
  private Vector<String> _vector = new Vector<String>();
  private int _cursor = -1;
 
  /**
   * Constructor, so we can add a listener to the Config item being used.
   */ 
  public History() {
    DrJava.getConfig().addOptionListener(HISTORY_MAX_SIZE, new HistorySizeOptionListener());
    
    // Sanity check on MAX_SIZE
    if (MAX_SIZE < 0) MAX_SIZE = 0;
  }
  
  /**
   * Adds an item to the history and moves the cursor to point
   * to the place after it.
   * Note: Items are not inserted if they are empty. (This is in accordance with 
   * bug #522123, but in divergence from feature #522213 which originally excluded
   * sequential duplicate entries from ever being stored.)
   *
   * Thus, to access the newly inserted item, you must movePrevious first.
   */
  public void add(String item) {
    
    // for consistency in saved History files, WILL save sequential duplicate entries
    if (item.trim().length() > 0) {
      //if (_vector.isEmpty() || ! _vector.lastElement().equals(item)) {
      _vector.addElement(item);
        
        // If adding the new element has filled _vector to beyond max 
        // capacity, spill the oldest element out of the History.
      if (_vector.size() > MAX_SIZE) {
        _vector.removeElementAt(0);
        }
      //}
      moveEnd();
    }
  }

  /**
   * Move the cursor to just past the end. Thus, to access the last element,
   * you must movePrevious.
   */
  public void moveEnd() {
    _cursor = _vector.size();
  }

  /** Moves cursor back 1, or throws exception if there is none. */
  public void movePrevious() {
    if (!hasPrevious()) {
      throw  new ArrayIndexOutOfBoundsException();
    }
    _cursor--;
  }

  /** Moves cursor forward 1, or throws exception if there is none. */
  public void moveNext() {
    if (!hasNext()) {
      throw  new ArrayIndexOutOfBoundsException();
    }
    _cursor++;
  }

  /** Returns whether moveNext() would succeed right now. */
  public boolean hasNext() {
    return  _cursor < (_vector.size());
  }

  /** Returns whether movePrevious() would succeed right now. */
  public boolean hasPrevious() {
    return  _cursor > 0;
  }

  /**
   * Returns item in history at current position, or throws exception if none.
   */
  public String getCurrent() {
    if (hasNext()) {
      return  _vector.elementAt(_cursor);
    }
    else {
      return "";
    }
  }
  
  /**
   * Returns the number of items in this History.
   */
  public int size() {
    return _vector.size();
  }
  
  /**
   * Clears the vector
   */
  public void clear() {
    // apparently gj's Vector doesn't have the clear() method
    _vector.setSize(0);
  }
  
  /**
   * Returns the history as a string by concatenating each string in the vector
   * separated by the delimiting character.
   * A semicolon is added to the end of every statement that didn't already
   * end with one.
   */
  public String getHistoryAsStringWithSemicolons() {
    String s = "";
    char delimiter = '\n';
    for (int i = 0; i < _vector.size(); i++) {
      String nextLine = _vector.elementAt(i);
      int nextLength = nextLine.length();
      if ((nextLength > 0) && (nextLine.charAt(nextLength-1) != ';')) {
        nextLine += ";";
      }
      s += nextLine + delimiter;
    }
    return s;
  }
  
  /**
   * Returns the history as a string by concatenating each string in the vector
   * separated by the delimiting character
   */
  public String getHistoryAsString() {
    String s = "";
    char delimiter = '\n';
    for (int i = 0; i < _vector.size(); i++) {
      s +=_vector.elementAt(i) + delimiter;
    }
    return s;
  }
  
  /**
   * Writes this History to a the file selected in the FileSaveSelector
   * @param selector File to save to
   * @param editedVersion If this string is non-null, it will be saved to file
   * instead of the lines saved in the history. The saved file will still include
   * any tags or extensions needed to recognize it as a saved interactions file.
   */
  public void writeToFile(FileSaveSelector selector, String editedVersion) throws IOException {
    
    File c = null;
    
    try {
      c = selector.getFile();
    }
    catch (OperationCanceledException oce) {
      return; // don't need to do anything
    }
    if (c != null) {
	if (c.getName().indexOf('.') == -1)
	    c = new File(c.getAbsolutePath() + "." +
			 InteractionsHistoryFilter.HIST_EXTENSION);
        FileOutputStream fos = new FileOutputStream(c);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osw);
	if (editedVersion == null)
	    editedVersion = getHistoryAsStringWithSemicolons();
 	bw.write(HISTORY_FORMAT_VERSION_2 + editedVersion, 0, HISTORY_FORMAT_VERSION_2.length() + editedVersion.length());
        bw.close();
    }
  }
  
  /**
   * The OptionListener for HISTORY_MAX_SIZE
   */
  private class HistorySizeOptionListener implements OptionListener<Integer> {
   
    public void optionChanged (OptionEvent<Integer> oce) {
      int newSize = oce.value.intValue();
      
      // Sanity check
      if (newSize < 0) newSize = 0;
      
      // Remove old elements if the new size is less than current size
      if (size() > newSize) {
        
        int numToDelete = size() - newSize;
        
        for (int i=0; i< numToDelete; i++) {
          _vector.removeElementAt(0);
        }
        
        moveEnd();
      }
      MAX_SIZE = newSize;
    }
  }
}
