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

import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.util.UnexpectedException;
import java.lang.reflect.Field;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.Event;
import java.util.Hashtable;

/**
 * Class representing all configuration options with values of type KeyStroke.
 */
public class KeyStrokeOption extends Option<KeyStroke> {
  
  static Hashtable keys = new Hashtable();
  public static final KeyStroke NULL_KEYSTROKE = KeyStroke.getKeyStroke(0, 0);
  /**
   * @param key The name of this option.
   */
  public KeyStrokeOption(String key, KeyStroke def) { 
    super(key,def); }
  
  // This sets up the hashtable that has key-value pairs consisting of
  // ascii codes and Strings that describe the ascii character and are
  // in the form that KeyStroke.getKeyStroke(String s) requires.
  static {
    try {
      Field[] fields = KeyEvent.class.getFields();
      for (int i = 0; i < fields.length; i++) {
        Field currfield = fields[i];
        String name = currfield.getName();
        if (name.startsWith("VK_")) {
          keys.put(new Integer(currfield.getInt(null)), name.substring(3));
        }
      }
    }
    catch(IllegalAccessException iae) {
      throw new UnexpectedException(iae);
    }
  }
  
  
  /**
   * @param s The String to be parsed, must be the string representation of 
   * the KeyStroke to be created. Uses the method KeyStroke.getKeyStroke(String s)
   * which returns a KeyStroke if the string is correctly formatted or null
   * otherwise.
   * @return The KeyStroke object corresponding to the input string "s".
   */
  public KeyStroke parse(String s) {
    if (s.equals("<none>")) {
      return NULL_KEYSTROKE;
    }
    
    // Replace "command" with "meta" (OS X)
    int cIndex = s.indexOf("command");
    if (cIndex > -1) {
      StringBuffer sb = new StringBuffer(s.substring(0, cIndex));
      sb.append("meta");
      sb.append(s.substring(cIndex + "command".length(), s.length()));
      s = sb.toString();
    }
    
    // Replace "option" with "alt" (OS X)
    int oIndex = s.indexOf("option");
    if (oIndex > -1) {
      StringBuffer sb = new StringBuffer(s.substring(0, oIndex));
      sb.append("alt");
      sb.append(s.substring(oIndex + "option".length(), s.length()));
      s = sb.toString();
    }
    
    KeyStroke ks = KeyStroke.getKeyStroke(s);
    if (ks == null) {
      throw new OptionParseException(name, s,
                                     "Must be a valid string representation of a Keystroke.");
    }
    return ks;
  }
  
  /**
   * @param k The instance of class KeyStroke to be formatted.
   * @return A String representing the KeyStroke "k".
   */
  public String format(KeyStroke k) {
    if (k == NULL_KEYSTROKE) {
      return "<none>";
    }

    // This code prints out locale specific text, which is bad!
    //  (KeyStroke.getKeystroke(s) can't parse it.)
    /*
    StringBuffer buf = new StringBuffer();
    String s = KeyEvent.getKeyModifiersText(k.getModifiers()).toLowerCase();
    s = s.replace('+', ' ');
    if (!s.equals(""))
      s += " ";
    buf.append(s);
    */

    // Generate modifiers text on our own, since getKeyStroke can't parse
    //  locale-specific modifiers.
    int modifiers = k.getModifiers();
    boolean isMac = _isMacPlatform();
    StringBuffer buf = new StringBuffer();
    if ((modifiers & Event.META_MASK) > 0) {
      String meta = (!isMac) ? "meta " : "command ";
      buf.append(meta);
    }
    if ((modifiers & Event.CTRL_MASK) > 0) {
      buf.append("ctrl ");
    }
    if ((modifiers & Event.ALT_MASK) > 0) {
      String alt = (!isMac) ? "alt " : "option ";
      buf.append(alt);
    }
    if ((modifiers & Event.SHIFT_MASK) > 0) {
      buf.append("shift ");
    }

    // If the key code is undefined, this is a "typed" unicode character
    if (k.getKeyCode() == KeyEvent.VK_UNDEFINED) {
      buf.append("typed ");
      buf.append(k.getKeyChar());
    }
    // else this corresponds to a static KeyEvent constant
    else {
      // defaults to pressed
      if (k.isOnKeyRelease()) {
        buf.append("released ");
      }
      String key = (String) keys.get(new Integer(k.getKeyCode()));
      if (key == null) {
        throw new IllegalArgumentException("Invalid keystroke");
      }
      if (key.equals("CONTROL") || key.equals("ALT") || key.equals("META") ||
          key.equals("SHIFT") || key.equals("ALT_GRAPH")) {
        return buf.toString();
      }
      else {
        buf.append(key);
        return buf.toString();
      }
    }
    return buf.toString();
  }

  /**
   * Returns if the current platform is a Macintosh.
   */
  private boolean _isMacPlatform() {
    String os = System.getProperty("os.name");
    if (os != null) {
      return os.toLowerCase().indexOf("mac") > -1;
    }
    else {
      return false;
    }
  }
}
