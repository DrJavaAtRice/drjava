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

package edu.rice.cs.drjava.config;

import edu.rice.cs.util.swing.Utilities;

/** Class to store and retrieve all configurable options.
 *  @version $Id$
 */
public class Configuration {  
  
  /** OptionMap used to store all option settings. */
  protected OptionMap map;
  
  /** Any exception that is caught when initializing this Configuration object.
   *  Used later by the UI to display a useful message to the user.
   */
  protected Exception _startupException;
  
  /** Initializes this Configuration object with the given OptionMap.
   *  @param om An empty OptionMap.
   */
  public Configuration(OptionMap om) {
    map = om;
    _startupException = null;
  }
  
  /** Sets the given option to the given value and notifies all listeners of that option of the change.
   *  @param op Option to set
   *  @param value New value for the option
   */
  public <T> T setSetting(final Option<T> op, final T value) {
    T ret = map.setOption(op, value);
    Utilities.invokeLater(new Runnable() { public void run() { op.notifyListeners(Configuration.this, value); } });
    return ret;
  }
  
  /** Gets the current value of the given Option. */
  public <T> T getSetting(Option<T> op) { return map.getOption(op); }
  
  /** Adds an OptionListener to the given Option, to be notified each time the option changes.
   *  @param op Option to listen for changes on
   *  @param l OptionListener wishing to listen
   */
  public <T> void addOptionListener(Option<T> op, OptionListener<T> l) { op.addListener(this,l); }
  
  /** Removes an OptionListener from an Option to which it was listening. */
  public <T> void removeOptionListener(Option<T> op, OptionListener<T> l) { op.removeListener(this,l); }
  
  /** Resets to the default values, overwriting any existing values. */
  public void resetToDefaults() { OptionMapLoader.DEFAULT.loadInto(map); }
  
  /** Returns whether there were any exceptions when starting. */
  public boolean hadStartupException() { return _startupException != null; }
  
  /** Returns the exception caught during startup, or null if none were caught. */
  public Exception getStartupException() { return _startupException; }
  
  /** Stores exception caught during creation of this Configuration object, so it can be displayed later by the UI.
   *  @param e Exception caught during startup
   */
  public void storeStartupException(Exception e) { _startupException = e; }
  
  /** Returns a string representation of the contents of the OptionMap. */
  public String toString() { return map.toString(); }
}
