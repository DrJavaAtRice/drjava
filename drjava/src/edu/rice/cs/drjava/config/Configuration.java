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

package edu.rice.cs.drjava.config;

import java.io.StringWriter;
import java.io.PrintWriter;
import edu.rice.cs.util.swing.Utilities;

/** Class to store and retrieve all configurable options.
  * @version $Id: Configuration.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class Configuration {  
  
  /** OptionMap used to store all option settings. */
  protected volatile OptionMap map;
  
  /** Any exception that is caught when initializing this Configuration object.
    * Used later by the UI to display a useful message to the user.
    */
  protected volatile Exception _startupException;
  
  /** Initializes this Configuration object with the given OptionMap.
    * @param om An empty OptionMap.
    */
  public Configuration(OptionMap om) {
    map = om;
    _startupException = null;
  }
  
  /** Sets the given option to the given value and notifies all listeners of that option of the change.
    * @param op Option to set
    * @param value New value for the option
    */
  public <T> T setSetting(final Option<T> op, final T value) {
    T ret = map.setOption(op, value);
//    System.err.println("setSetting(" + op + ", " + value + ") called");
    Utilities.invokeLater(new Runnable() { public void run() { op.notifyListeners(Configuration.this, value); } });
    return ret;
  }
  
  /** Gets the current value of the given Option. */
  public <T> T getSetting(Option<T> op) { return map.getOption(op); }
  
  /** By default, all options are editable. */
  public <T> boolean isEditable(Option<T> op) { return true; }
  
  /** Adds an OptionListener to the given Option, to be notified each time the option changes.
    * @param op Option to listen for changes on
    * @param l OptionListener wishing to listen
    */
  public <T> void addOptionListener(Option<T> op, OptionListener<T> l) { op.addListener(this,l); }
  
  /** Removes an OptionListener from an Option to which it was listening. */
  public <T> void removeOptionListener(Option<T> op, OptionListener<T> l) { op.removeListener(this,l); }
  
  /** Resets to the default values, overwriting any existing values. */
  public void resetToDefaults() { OptionMapLoader.DEFAULT.loadInto(map); }
  
  /** Returns whether there were any exceptions when starting. */
  public boolean hadStartupException() { return _startupException != null; }
  
  /** Returns the exception caught during startUp, or null if none were caught. */
  public Exception getStartupException() { return _startupException; }
  
  /** Stores exception caught during creation of this Configuration object, so it can be displayed later by the UI.
    * @param e Exception caught during startUp
    */
  public void storeStartupException(Exception e) { _startupException = e; }
  
  /** Returns a string representation of the contents of the OptionMap. */
  public String toString() {
    StringWriter sw = new StringWriter();
    PrintWriter w = new PrintWriter(sw);

    // Write each option
    for (OptionParser<?> key : map.keys()) {
      if (!key.getDefault().equals(map.getOption(key))) {
        String tmpString = map.getString(key);
        
        // This replaces all backslashes with two backslashes for windows
        tmpString = tmpString.replaceAll("\\\\", "\\\\\\\\");
        
        w.println(key.getName()+" = "+tmpString);
      }
    }
    w.close();
    
    return sw.toString();
  }
  
  /** Return OptionMap. */
  public OptionMap getOptionMap() { return map; }
}
