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

package edu.rice.cs.drjava.model.debug;

import com.sun.jdi.*;

/**
 * Class for keeping track of watched fields and variables.
 * @version $Id$
 */
public class DebugWatchData {
  /**
   * String to display if the value is not in scope.
   */
  public static final String NO_VALUE = "<not found>";
  
  /**
   * String to display if the type is not in scope.
   */
  public static final String NO_TYPE = "";
  
  /**
   * String to display if the type is not loaded.
   */
  public static final String NOT_LOADED = "<not loaded>";
  
  private String _name;
  private String _value;
  private String _type;
  private boolean _showValue;
  private boolean _showType;
  private boolean _changed;
  
  /**
   * Object to keep track of a watched field or variable.
   * @param name Name of the field or variable to watch
   */
  public DebugWatchData(String name) {
    _name = name;
    _value = "";
    _type = "";
    _showValue = false;
    _showType = false;
    _changed = false;
  }
  
  /**
   * Returns the name of this field or variable
   */
  public String getName() {
    return _name;
  }
  
  /**
   * Returns the most recently determined value for this field or variable.
   */
  public String getValue() {
    return (_showValue) ? _value : "";
  }
  
  /**
   * Returns the type of this field or variable in the current context.
   */
  public String getType() {
    return (_showType) ? _type : "";
  }
  
  /**
   * Sets a new name for this field or variable.
   * @param name Name of the field or variable
   */
  void setName(String name) {
    _name = name;
  }
  
  /**
   * Sets the most recently determined value for this field or variable.
   * @param value Value of the field or variable
   */
  void setValue(Object value) {
    _showValue = true;
    String valString = String.valueOf(value);
    if (!valString.equals(_value)) {
      _changed = true;
    }
    else {
      _changed = false;
    }
    _value = valString;
  }
  
  /**
   * Hides the value for this watch (when no thread is suspended).
   */
  void hideValueAndType() {
    _showValue = false;
    _showType = false;
    _changed = false;
  }
  
  /**
   * Called to indicate that this watch has no value in the current scope.
   */
  void setNoValue() {
    _showValue = true;
    _value = NO_VALUE;
  }
  
  /**
   * Sets the most recently determined type of this field or variable.
   * @param type Type of the field or variable
   */
  void setType(String type) {
    _showType = true;
    _type = type;
  }
  
  /**
   * Called to indicate that this watch has no type in the current scope.
   */
  void setNoType() {
    _showType = true;
    _type = NO_TYPE;
  }
  
  /**
   * Called to indicate that this watch's type has not been loaded.
   */
  void setTypeNotLoaded() {
    _showType = true;
    _type = NOT_LOADED;
  }
  
  /**
   * Returns whether this value has changed since the last call to setValue.
   */
  public boolean isChanged() {
    return _changed;
  }
  
  /**
   * Returns a legible representation of the type, name, and value.
   */
  public String toString() {
    return _type + " " + _name + ": " + _value;
  }
}
