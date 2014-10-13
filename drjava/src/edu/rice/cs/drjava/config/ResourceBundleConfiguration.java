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

import edu.rice.cs.util.swing.Utilities;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.io.*;
import java.util.Enumeration;

/** A configuration in a resource bundle.
  * @version $Id: ResourceBundleConfiguration.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class ResourceBundleConfiguration extends FileConfiguration {
  /** Name of the resource bundle. */
  protected final String _resourceBundleName;
  
  /** Resource bundle containing the configuration. */
  protected final ResourceBundle _bundle;
  
  /** Shadowed configuration used if the resource bundle does not define an option. */
  protected final FileConfiguration _shadowed;
  
  /** Initializes this Configuration object with the given OptionMap.
    * @param resourceBundleName name of the resource bundle
    * @param shadowed configuration that should be used if the resource bundle does not define an option.
    */
  public ResourceBundleConfiguration(String resourceBundleName, FileConfiguration shadowed) {
    super(shadowed.getFile());
    _resourceBundleName = resourceBundleName;
    _bundle = ResourceBundle.getBundle(resourceBundleName);
    _shadowed = shadowed;
    map = new OptionMap() {
      public <T> T getOption(OptionParser<T> o) {
        if (o==null) return _shadowed.getOptionMap().getOption(o);
        try {
          String str = _bundle.getString(o.getName());
          return o.parse(str); // defined in resource bundle
        }
        catch(MissingResourceException mre) {
          // not defined, delegate to shadowed configuration
          return _shadowed.getOptionMap().getOption(o);
        }
      }
      
      public <T> T setOption(Option<T> o, T val) {
        if (o==null) return _shadowed.getOptionMap().setOption(o, val);
        try {
          String str = _bundle.getString(o.getName());
          return null; // defined in resource bundle, can't be set
        }
        catch(MissingResourceException mre) {
          // not defined, delegate to shadowed configuration
          return _shadowed.getOptionMap().setOption(o, val);
        }
      }
      
      public <T> String getString(OptionParser<T> o) {
        if (o==null) return _shadowed.getOptionMap().getString(o);
        try {
          String str = _bundle.getString(o.getName());
          return str; // defined in resource bundle
        }
        catch(MissingResourceException mre) {
          // not defined, delegate to shadowed configuration
          return _shadowed.getOptionMap().getString(o);
        }
      }
      
      public <T> void setString(OptionParser<T> o, String s) {
        if (o==null) _shadowed.getOptionMap().setString(o, s);
        try {
          String str = _bundle.getString(o.getName());
          return; // defined in resource bundle, can't be set
        }
        catch(MissingResourceException mre) {
          // not defined, delegate to shadowed configuration
          _shadowed.getOptionMap().setString(o, s);
        }
      }
      
      public <T> T removeOption(OptionParser<T> o) {
        if (o==null) return _shadowed.getOptionMap().removeOption(o);
        try {
          String str = _bundle.getString(o.getName());
          return null; // defined in resource bundle, can't be removed
        }
        catch(MissingResourceException mre) {
          // not defined, delegate to shadowed configuration
          return _shadowed.getOptionMap().removeOption(o);
        }
      }
      
      public Iterable<OptionParser<?>> keys() {
        // TODO merge with keys from this configuration
        Iterable<OptionParser<?>> shadowedKeys = _shadowed.getOptionMap().keys();
//        HashMap<String,OptionParser<?>> combined = HashMap<String,OptionParser<?>>();
//        Enumeration<String> keyEn = _bundle.getKeys();
//        while(keyEn.hasMoreElements()) {
//          String key = keyEn.nextElement();
//          combined 
//        }
        return shadowedKeys;
      }
    };
  }
  
  /** Sets the given option to the given value and notifies all listeners of that option of the change.
    * @param op Option to set
    * @param value New value for the option
    */
  public <T> T setSetting(final Option<T> op, final T value) {
    if (op==null) return _shadowed.setSetting(op, value);
    try {
      String str = _bundle.getString(op.getName());
      return null; // defined in resource bundle, can't be set
    }
    catch(MissingResourceException mre) {
      // not defined, delegate to shadowed configuration
      return _shadowed.setSetting(op, value);
    }
  }
  
  /** Gets the current value of the given Option. */
  public <T> T getSetting(Option<T> op) {
    if (op==null) return _shadowed.getSetting(op);
    try {
      String str = _bundle.getString(op.getName());
      return op.parse(str); // defined in resource bundle
    }
    catch(MissingResourceException mre) {
      // not defined, delegate to shadowed configuration
      return _shadowed.getSetting(op);
    }
  }

  /** Return true if the option is editable. If it was defined in the resource bundle, it is not editable. */
  public <T> boolean isEditable(Option<T> op) {
    if (op==null) return _shadowed.isEditable(op);
    try {
      String str = _bundle.getString(op.getName());
      return false; // defined, not editable
    }
    catch(MissingResourceException mre) {
      // not defined, delegate to shadowed configuration
      return _shadowed.isEditable(op);
    }
  }
  
  /** Resets to the default values, overwriting any existing values. */
  public void resetToDefaults() {
    // just delegate, our values don't change
    _shadowed.resetToDefaults();
  }
  
  /** Returns a string representation of the contents of the OptionMap. */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("In resource bundle ");
    sb.append(_resourceBundleName);
    sb.append(":\n");
    boolean empty = true;
    Enumeration<String> keyEn = _bundle.getKeys();
    while(keyEn.hasMoreElements()) {
      String key = keyEn.nextElement();
      sb.append(key);
      sb.append(" = ");
      sb.append(_bundle.getString(key));
      sb.append("\n");
      empty = false;
    }
    if (empty) sb.append("\tnothing\n");
    sb.append("\nIn shadowed configuration:\n");
    sb.append(_shadowed);
    return sb.toString();
  }
  
  /** Calls SavableConfiguration.loadConfiguration, which loads all values from the file, based on the defaults in
    * OptionConstants.
    */
  public void loadConfiguration() throws IOException {
    _shadowed.loadConfiguration(); // just delegate, our values won't change
  }
  
  /** Saves the current settings to the stored properties file. */
  public void saveConfiguration() throws IOException {
    _shadowed.saveConfiguration();
  }
  
  /** Saves the current settings to the stored properties file.
    * @param header Description of the properties list
    */
  public void saveConfiguration(final String header) throws IOException {
    _shadowed.saveConfiguration(header);
  }
  
  /** Creates an OptionMapLoader with the values loaded from the InputStream
   * (and defaults where values weren't specified) and loads them into
   * this Configuration's OptionMap.
   * @param is InputStream containing properties-style keys and values
   */
  public void loadConfiguration(InputStream is) throws IOException {
    _shadowed.loadConfiguration(is); // just delegate, our values won't change
  }

  /** Used to save the values from this Configuration into the given OutputStream
   * as a Properties file. The elements weren't ordered, so now the properties
   * are written in the same way as the about dialog.
   * Values equal to their defaults are not written to disk.
   */
  public void saveConfiguration(OutputStream os, String header) throws IOException {
    _shadowed.saveConfiguration(os,header);
  }
  
  /** Adds an OptionListener to the given Option, to be notified each time the option changes.
    * @param op Option to listen for changes on
    * @param l OptionListener wishing to listen
    */
  public <T> void addOptionListener(Option<T> op, OptionListener<T> l) {
    op.addListener(this,l);
    op.addListener(_shadowed,l);
  }
  
  /** Removes an OptionListener from an Option to which it was listening. */
  public <T> void removeOptionListener(Option<T> op, OptionListener<T> l) {
    op.removeListener(this,l);
    op.removeListener(_shadowed,l);
  }
}
