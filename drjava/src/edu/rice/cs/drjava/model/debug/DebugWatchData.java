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

package edu.rice.cs.drjava.model.debug;

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
    _changed = false;
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
