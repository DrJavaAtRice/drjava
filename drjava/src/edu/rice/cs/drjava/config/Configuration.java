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

package edu.rice.cs.drjava.config;

/**
 * Class to store and retrieve all configurable options.
 * @version $Id$
 */
public class Configuration {  
  
  /**
   * OptionMap used to store all option settings.
   */
  protected OptionMap map;
  
  /**
   * Any exception that is caught when initializing this Configuration object.
   * Used later by the UI to display a useful message to the user.
   */
  protected Exception _startupException;
  
  /**
   * Initializes this Configuration object with the given OptionMap.
   * @param om An empty OptionMap.
   */
  public Configuration(OptionMap om) {
    map = om;
    _startupException = null;
  }
  
  /**
   * Sets the given option to the given value and notifies all
   * listeners of that option of the change.
   * @param op Option to set
   * @param value New value for the option
   */
  public <T> T setSetting(Option<T> op, T value) {
    T ret = map.setOption(op,value);
    op.notifyListeners(this,value);
    return ret;
  }
  
  /**
   * Gets the current value of the given Option.
   */
  public <T> T getSetting(Option<T> op) {
    return map.getOption(op);
  }
  
  /**
   * Adds an OptionListener to the given Option, to be notified each
   * time the option changes.
   * @param op Option to listen for changes on
   * @param l OptionListener wishing to listen
   */
  public <T> void addOptionListener(Option<T> op, OptionListener<T> l) {
    op.addListener(this,l);
  }
  
  /**
   * Removes an OptionListener from an Option to which it was listening.
   */
  public <T> void removeOptionListener(Option<T> op, OptionListener<T> l) {
    op.removeListener(this,l);
  }
  
  /**
   * Resets to the default values, overwriting any existing values.
   */
  public void resetToDefaults() {
    OptionMapLoader.DEFAULT.loadInto(map);
  }
  
  /**
   * Returns whether there were any exceptions when starting.
   */
  public boolean hadStartupException() {
    return _startupException != null;
  }
  
  /**
   * Returns the exception caught during startup, or null if none were caught.
   */
  public Exception getStartupException() {
    return _startupException;
  }
  
  /**
   * Stores any exception caught during the creation of this Configuration
   * object, so it can be displayed later by the UI.
   * @param e Exception caught during startup
   */
  public void storeStartupException(Exception e) {
    _startupException = e;
  }
  
  /**
   * Returns a string representation of the contents of the OptionMap.
   */
  public String toString() {
    return map.toString();
  }
}
