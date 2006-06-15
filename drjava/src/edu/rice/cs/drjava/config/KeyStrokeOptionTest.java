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

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.util.newjvm.ExecJVM;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Locale;

/** Class according to the JUnit protocol. Tests the proper functionality of the class KeyStrokeOption.
 *  @version $Id$
 */
public final class KeyStrokeOptionTest extends DrJavaTestCase {
  /** @param name The name of this test case. */
  public KeyStrokeOptionTest(String name) { super(name); }

  public void testGetName() {
    KeyStrokeOption io1 = new KeyStrokeOption("indent_size",null);
    KeyStrokeOption io2 = new KeyStrokeOption("max_files",null);

    assertEquals("indent_size", io1.getName());
    assertEquals("max_files",   io2.getName());
  }

  public void testParse() {
    KeyStrokeOption io = new KeyStrokeOption("max_files",null);
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK),
                 io.parse("ctrl ENTER"));
    assertEquals(KeyStrokeOption.NULL_KEYSTROKE,
                 io.parse("<none>"));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_NUM_LOCK, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK, true),
                 io.parse("alt shift released NUM_LOCK"));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK, false),
                 io.parse("alt shift COMMA"));
    assertEquals(KeyStroke.getKeyStroke('%'),
                 io.parse("typed %"));
    assertEquals(KeyStroke.getKeyStroke(new Character('%'), InputEvent.ALT_MASK | InputEvent.CTRL_MASK),
                 io.parse("ctrl alt typed %"));

    try { io.parse("true"); fail(); }
    catch (IllegalArgumentException e) { }

    try { io.parse(".33"); fail(); }
    catch (IllegalArgumentException e) { }

    try { io.parse("Alt Z"); fail(); }
    catch (IllegalArgumentException e) { }

    try { io.parse("ctrl alt shift typed F1"); fail(); }
    catch (IllegalArgumentException e) { }
  }

  /** Test the format method by comparing a KeyStroke object to itself after it has been formatted to a string and 
   *  parsed back into a KeyStroke object.  We cannot compare strings because format always puts the modifiers in the
   *  same order which could be a different order than the user specifies.
   */
  public void testFormat() {
    KeyStrokeOption io = new KeyStrokeOption("max_files",null);
    KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK | InputEvent.META_MASK);
    assertEquals(ks, io.parse(io.format(ks)));
    ks = KeyStroke.getKeyStroke(KeyEvent.VK_NUMBER_SIGN, InputEvent.ALT_MASK | InputEvent.CTRL_MASK);
    assertEquals(ks, io.parse(io.format(ks)));
   
    ks = KeyStroke.getKeyStroke(new Character('!'), InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);
    assertEquals(ks, io.parse(io.format(ks)));
    ks = KeyStroke.getKeyStroke('!');
    assertEquals(ks, io.parse(io.format(ks)));
    ks = KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK, true);
    assertEquals(ks, io.parse(io.format(ks)));
  }

  /** Tests that key strokes are output in a parseable format even in foreign locales.  The test must be run in a 
   *  separate JVM, because once the locale is set, it cannot be set back.  (If someone can figure out how
   *  to effectively set it back, feel free to remove this hack!)
   */
  public void testLocaleSpecificFormat() throws IOException, InterruptedException {
    String className = "edu.rice.cs.drjava.config.KeyStrokeOptionTest";
    String[] args = new String[0];

    Process process = ExecJVM.runJVMPropagateClassPath(className, args, FileOption.NULL_FILE);
    int status = process.waitFor();
    assertEquals("Local specific keystroke test failed!", 0, status);
  }

  /** Main method to be called by testLocalSpecificFormat.  Runs in a new JVM so as not to affect the locale of other
   *  tests.
   */
  public static void main(String[] args) {
    // Set to German, which has different words for ctrl and shift
    Locale.setDefault(Locale.GERMAN);

    KeyStrokeOption io = new KeyStrokeOption("test",null);
    KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_MASK | InputEvent.CTRL_MASK |
                                          InputEvent.SHIFT_MASK | InputEvent.META_MASK);
    String s = io.format(ks);
    // Test alt/option
    if ((s.indexOf("alt") == -1) && (s.indexOf("option") == -1)) System.exit(1);

    // Test ctrl
    if (s.indexOf("ctrl") == -1) System.exit(2);
    
    // Test shift
    if (s.indexOf("shift") == -1) System.exit(3);

    // Test meta
    if ((s.indexOf("meta") == -1) && (s.indexOf("command") == -1)) System.exit(4);

    // Ok, so exit cleanly
    System.exit(0);
  }
}
