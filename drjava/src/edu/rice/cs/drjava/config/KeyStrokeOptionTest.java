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

import javax.swing.KeyStroke; 
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import junit.framework.*;
import edu.rice.cs.drjava.CodeStatus;

/**
 * Class according to the JUnit protocol. Tests
 * the proper functionality of the class KeyStrokeOption.
 */
public class KeyStrokeOptionTest extends TestCase
{
  /**
   * @param name The name of this test case.
   */
  public KeyStrokeOptionTest(String name) { super(name); }
  
  public void setUp() {}
  
  public void testGetName()
  {
    KeyStrokeOption io1 = new KeyStrokeOption("indent_size",null);
    KeyStrokeOption io2 = new KeyStrokeOption("max_files",null);
    
    assertEquals("indent_size", io1.getName());
    assertEquals("max_files",   io2.getName());
  }
  
  public void testParse()
  {
    KeyStrokeOption io = new KeyStrokeOption("max_files",null);
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                                        InputEvent.CTRL_MASK),
                 io.parse("ctrl ENTER"));
    assertEquals(KeyStrokeOption.NULL_KEYSTROKE,
                 io.parse("<none>"));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_NUM_LOCK,
                                        InputEvent.ALT_MASK | InputEvent.SHIFT_MASK,
                                        true),
                 io.parse("alt shift released NUM_LOCK"));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,
                                        InputEvent.ALT_MASK | InputEvent.SHIFT_MASK,
                                        false),
                 io.parse("alt shift COMMA"));
    assertEquals(KeyStroke.getKeyStroke('%'),
                 io.parse("typed %"));
    // behaves correctly in 1.3, but the test will not work for some reason
    /*assertEquals(KeyStroke.getKeyStroke(new Character('%'),
     InputEvent.ALT_MASK | InputEvent.CTRL_MASK),
     io.parse("ctrl alt typed %"));*/
    
    try { io.parse("true"); fail(); }
    catch (IllegalArgumentException e) {}
    
    try { io.parse(".33"); fail(); }
    catch (IllegalArgumentException e) {}
    
    try { io.parse("Alt Z"); fail(); }
    catch (IllegalArgumentException e) {}
    
    try { io.parse("ctrl alt shift typed F1"); fail(); }
    catch (IllegalArgumentException e) {}
  }
  
  /** 
   * Test the format method by comparing a KeyStroke object to itself after it
   * has been formatted to a string and parsed back into a KeyStroke object.
   * We cannot compare strings because format always puts the modifiers in the
   * same order which could be a different order than the user specifies.
   */
  public void testFormat()
  {
    KeyStrokeOption io = new KeyStrokeOption("max_files",null);
    KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                                          InputEvent.ALT_MASK | InputEvent.META_MASK);
    assertEquals(ks, io.parse(io.format(ks)));
    ks = KeyStroke.getKeyStroke(KeyEvent.VK_NUMBER_SIGN,
                                InputEvent.ALT_MASK | InputEvent.CTRL_MASK);
    assertEquals(ks, io.parse(io.format(ks)));
    // behaves correctly in 1.3, but the test will not work for some reason
    /*ks = KeyStroke.getKeyStroke(new Character('!'),
     InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);
     assertEquals(ks, io.parse(io.format(ks)));*/
    ks = KeyStroke.getKeyStroke('!');
    assertEquals(ks, io.parse(io.format(ks)));
    ks = KeyStroke.getKeyStroke(KeyEvent.VK_F10,
                                InputEvent.ALT_MASK | InputEvent.SHIFT_MASK,
                                true);
    assertEquals(ks, io.parse(io.format(ks)));
  }
}
