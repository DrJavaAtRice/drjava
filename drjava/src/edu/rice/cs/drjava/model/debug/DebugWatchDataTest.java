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

package edu.rice.cs.drjava.model.debug;

import junit.framework.TestCase;

/**
 * Tests that the DebugWatchData class can display state correctly.
 * @version $Id$
 */
public final class DebugWatchDataTest extends TestCase {
 
  /**
   * Tests the state of a watch after its creation.
   */
  public void testFirstCreation() {
    DebugWatchData data = new DebugWatchData("foo");
    assertEquals("should have a name on startup",
                 "foo", data.getName());
    assertEquals("should have no value on startup",
                 "", data.getValue());
    assertEquals("should have no type on startup",
                 "", data.getType());
    assertTrue("should not be changed on startup", !data.isChanged());
  }
  
  /**
   * Tests that a watch displays its value and type correctly,
   * then hides it when the thread resumes.  Also tests that
   * the changed flag works correctly.
   */
  public void testInScopeThenCleared() {
    DebugWatchData data = new DebugWatchData("foo");

    // Set value and type
    data.setValue(new Integer(7));
    data.setType("java.lang.Integer");
    assertEquals("should have a value", "7", data.getValue());
    assertEquals("should have a type", "java.lang.Integer", data.getType());
    assertTrue("should be changed", data.isChanged());
    
    // Hide value and type
    data.hideValueAndType();
    assertEquals("should have no value after hide",
                 "", data.getValue());
    assertEquals("should have no type after hide",
                 "", data.getType());
    assertTrue("should not be changed after hide", !data.isChanged());
    
    // Make sure using same value doesn't indicate a change
    data.setValue(new Integer(7));
    assertTrue("should not be changed after setting same value",
               !data.isChanged());
    
    // Make sure using a new value indicates a change
    data.setValue(new Integer(8));
    assertTrue("should be changed after setting different value",
               data.isChanged());
  }
  
  /**
   * Tests that a watch displays correctly if it is not in scope.
   */
  public void testNotInScope() {
    DebugWatchData data = new DebugWatchData("bar");
    data.setNoValue();
    data.setNoType();
    
    assertEquals("should not be in scope",
                 DebugWatchData.NO_VALUE, data.getValue());
    assertEquals("should not have a type",
                 DebugWatchData.NO_TYPE, data.getType());
    assertTrue("should not appear changed", !data.isChanged());
  }
  
  
}