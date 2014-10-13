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

package edu.rice.cs.drjava.model.debug;

/**
 * Class for keeping track of watched fields and variables.
 * @version $Id: DebugWatchData.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class DebugWatchData {
  /** String to display if the value is not in scope.
   */
  public static final String NO_VALUE = "<not found>";

  /** String to display if the type is not in scope.
   */
  public static final String NO_TYPE = "";

  /** String to display if the type is not loaded.
   */
  public static final String NOT_LOADED = "<not loaded>";

  private String _name;
  private String _value;
  private String _type;
  private boolean _showValue;
  private boolean _showType;
  private boolean _changed;

  /** Object to keep track of a watched field or variable.
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

  /** Returns the name of this field or variable. */
  public String getName() { return _name; }

  /** Returns the most recently determined value for this field or variable. */
  public String getValue() { return (_showValue) ? _value : ""; }

  /** Returns the type of this field or variable in the current context.
   */
  public String getType() {
    return (_showType) ? _type : "";
  }

  /** Sets a new name for this field or variable.
   * @param name Name of the field or variable
   */
  public void setName(String name) {
    _name = name;
  }

  /** Sets the most recently determined value for this field or variable.
   * @param value Value of the field or variable
   */
  public void setValue(Object value) {
    _showValue = true;
    String valString = String.valueOf(value);
    if (!valString.equals(_value)) _changed = true;
    else _changed = false;
    _value = valString;
  }

  /** Hides the value for this watch (when no thread is suspended). */
  public void hideValueAndType() {
    _showValue = false;
    _showType = false;
    _changed = false;
  }

  /** Called to indicate that this watch has no value in the current scope. */
  public void setNoValue() {
    _showValue = true;
    _value = NO_VALUE;
    _changed = false;
  }

  /** Sets the most recently determined type of this field or variable.
    * @param type Type of the field or variable
    */
  public void setType(String type) {
    _showType = true;
    _type = type;
  }

  /** Called to indicate that this watch has no type in the current scope. */
  public void setNoType() {
    _showType = true;
    _type = NO_TYPE;
  }

  /** Called to indicate that this watch's type has not been loaded. */
  public void setTypeNotLoaded() {
    _showType = true;
    _type = NOT_LOADED;
  }

  /** Returns whether this value has changed since the last call to setValue. */
  public boolean isChanged() { return _changed; }

  /** Returns a legible representation of the type, name, and value. */
  public String toString() { return _type + " " + _name + ": " + _value; }
}
