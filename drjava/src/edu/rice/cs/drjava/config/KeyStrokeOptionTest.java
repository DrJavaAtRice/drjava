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

import edu.rice.cs.drjava.DrJavaTestCase;

import edu.rice.cs.plt.concurrent.JVMBuilder;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Locale;

/** Class according to the JUnit protocol. Tests the proper functionality of the class KeyStrokeOption.
  * @version $Id: KeyStrokeOptionTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public final class KeyStrokeOptionTest extends DrJavaTestCase {
  /** @param name The name of this test case. */
  public KeyStrokeOptionTest(String name) { super(name); }
  
  public void testGetName() {
    KeyStrokeOption io1 = new KeyStrokeOption("indent_size", null);
    KeyStrokeOption io2 = new KeyStrokeOption("max_files", null);
    
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
    assertEquals(KeyStroke.getKeyStroke(Character.valueOf('%'), InputEvent.ALT_MASK | InputEvent.CTRL_MASK),
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
    * parsed back into a KeyStroke object.  We cannot compare strings because format always puts the modifiers in the
    * same order which could be a different order than the user specifies.
    */
  public void testFormat() {
    KeyStrokeOption io = new KeyStrokeOption("max_files",null);
    KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK | InputEvent.META_MASK);
    assertEquals(ks, io.parse(io.format(ks)));
    ks = KeyStroke.getKeyStroke(KeyEvent.VK_NUMBER_SIGN, InputEvent.ALT_MASK | InputEvent.CTRL_MASK);
    assertEquals(ks, io.parse(io.format(ks)));
    
    ks = KeyStroke.getKeyStroke(Character.valueOf('!'), InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);
    assertEquals(ks, io.parse(io.format(ks)));
    ks = KeyStroke.getKeyStroke('!');
    assertEquals(ks, io.parse(io.format(ks)));
    ks = KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK, true);
    assertEquals(ks, io.parse(io.format(ks)));
  }
  
  /** Tests that key strokes are output in a parseable format even in foreign locales.  The test must be run in a 
    * separate JVM, because once the locale is changed, it cannot be restored.  (If someone can figure out how
    * to effectively set it back, feel free to remove this hack!)
    */
  public void testLocaleSpecificFormat() throws IOException, InterruptedException {
    Process process = JVMBuilder.DEFAULT.start(KeyStrokeOptionTest.class.getName()); 
    int status = process.waitFor();
    assertEquals("Local specific keystroke test failed!", 0, status);
  }
  
  /** Main method called by testLocalSpecificFormat.  Runs in new JVM to avoid corrupting the locale of other tests. */
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
