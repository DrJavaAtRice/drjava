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

package edu.rice.cs.drjava.model.repl;

import  junit.framework.*;
import  java.util.Vector;
import  junit.extensions.*;
import  javax.swing.text.BadLocationException;
import  java.io.File;
import  edu.rice.cs.drjava.config.*;
import  edu.rice.cs.drjava.CodeStatus;

/**
 * Tests the functionality of the repl History.
 * @version $Id$
 */
public class HistoryTest extends TestCase implements OptionConstants{
  private History _history;

  /**
   * Create a new instance of this TestCase.
   * @param     String name
   */
  public HistoryTest(String name) {
    super(name);
  }

  /**
   * Initialize fields for each test.
   */
  protected void setUp() {
    _history = new History();
  }

  /**
   * Return a new TestSuite for this class.
   * @return Test
   */
  public static Test suite() {
    return  new TestSuite(HistoryTest.class);
  }

  public void testMultipleInsert() {
    _history.add("new Object()");
    _history.add("new Object()");
    assertEquals("Duplicate elements inserted", 2, _history.size());
  }
  
  public void testCanMoveToEmptyAtEnd() {
    _history.add("some text");
    
    _history.movePrevious();
    assertEquals("Prev did not move to correct item", 
                 "some text", 
                 _history.getCurrent());
    
    _history.moveNext();
    assertEquals("Can't move to blank line at end",
                 "",
                 _history.getCurrent());
  }
  
  /**
   * Ensures that Histories are bound to 500 entries.
   */
  public void testHistoryIsBounded() {
    
    int maxLength = 500;
    CONFIG.setSetting(HISTORY_MAX_SIZE, new Integer(maxLength));
    
    for (int i = 0; i < maxLength + 100; i++) {
      _history.add("testing " + i);
    }
    while(_history.hasPrevious()) {
      _history.movePrevious();
    }

    assertEquals("History length is not bound to " + maxLength,
   maxLength,
   _history.size());
    assertEquals("History elements are not removed in FILO order",
                 "testing 100",
                 _history.getCurrent());
  }
  
  public void testLiveUpdateOfHistoryMaxSize() {
    
    if (CodeStatus.DEVELOPMENT) {
      int maxLength = 20;
      CONFIG.setSetting(HISTORY_MAX_SIZE, new Integer(20));
      
      for (int i = 0; i < maxLength; i++) {
        _history.add("testing " + i);
      }
      
      CONFIG.setSetting(HISTORY_MAX_SIZE, new Integer(10));
      
      assertEquals("History size should be 10",
                   10, _history.size());
      
      CONFIG.setSetting(HISTORY_MAX_SIZE, new Integer(100));
      
      assertEquals("History size should still be 10",
                   10,
                   _history.size());
      
      CONFIG.setSetting(HISTORY_MAX_SIZE, new Integer(0));
      
      assertEquals("History size should be 0",
                   0,
                   _history.size());
      
      CONFIG.setSetting(HISTORY_MAX_SIZE, new Integer(-1));
      
      assertEquals("History size should still be 0",
                   0,
                   _history.size());
    }
  }
  
}



