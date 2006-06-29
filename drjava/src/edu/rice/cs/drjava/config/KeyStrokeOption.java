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

import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.util.UnexpectedException;
import java.lang.reflect.Field;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.Event;
import java.util.Hashtable;

/** Class representing all configuration options with values of type KeyStroke.  This code should only run in the 
 *  event thread, so no synchronization is necessary (or advisable).*/
public class KeyStrokeOption extends Option<KeyStroke> {

  /** Storage for keystrokes.*/
  static Hashtable<Integer, String> keys = new Hashtable<Integer, String>();
  public static final KeyStroke NULL_KEYSTROKE = KeyStroke.getKeyStroke(0, 0);
  /** Standard constructor
   *  @param key The name of this option.
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

    // Generate modifiers text on our own, since getKeyStroke can't parse locale-specific modifiers.
    int modifiers = k.getModifiers();
    boolean isMac = PlatformFactory.ONLY.isMacPlatform();
    StringBuffer buf = new StringBuffer();
    if ((modifiers & Event.META_MASK) > 0) {
      String meta = (! isMac) ? "meta " : "command ";
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
      String key = keys.get(new Integer(k.getKeyCode()));
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
}
