/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package koala.dynamicjava.util;

import koala.dynamicjava.interpreter.throwable.WrongVersionException;

import junit.framework.TestCase;

/**
 * Tests the utility methods in the ReflectionUtilities class to
 * make sure they are working correctly.
 */
public class ReflectionUtilitiesTest extends TestCase {
  
  /**
   * Tests the isBoxCompatible method (The three following private methods
   * are there to factor out the repetitive code within the actual test method)
   */
  public void testIsBoxCompatible() {
    _assertBoxCompatible(boolean.class, boolean.class, false);
    _assertBoxCompatible(Boolean.class, Boolean.class, false);
    _assertBoxCompatible(boolean.class, Boolean.class, true);
    _exceptionBoxCompatible(boolean.class, Boolean.class, false);
    _assertNotBoxCompatible(boolean.class, int.class, true);
    _assertNotBoxCompatible(int.class, boolean.class, true);
    _assertNotBoxCompatible(int.class, Boolean.class, true);
    
    _assertBoxCompatible(double.class, float.class, true);
    _assertBoxCompatible(double.class, long.class, true);
    _assertBoxCompatible(double.class, int.class, true);
    _assertBoxCompatible(double.class, short.class, true);
    _assertBoxCompatible(double.class, byte.class, true);
    _assertBoxCompatible(double.class, double.class, true);
    _assertNotBoxCompatible(double.class, boolean.class, true);
    
    _assertBoxCompatible(Object.class, boolean.class, true);
    _assertBoxCompatible(Object.class, byte.class, true);
    _assertBoxCompatible(Object.class, char.class, true);
    _assertBoxCompatible(Object.class, short.class, true);
    _assertBoxCompatible(Object.class, int.class, true);
    _assertBoxCompatible(Object.class, long.class, true);
    _assertBoxCompatible(Object.class, float.class, true);
    _assertBoxCompatible(Object.class, double.class, true);
    _assertBoxCompatible(Number.class, int.class, true);
    _assertNotBoxCompatible(Number.class, char.class, true);
    _exceptionBoxCompatible(Number.class, int.class, false);
    _exceptionBoxCompatible(Object.class, long.class, false);
    _assertNotBoxCompatible(char.class, int.class, true); 
    _assertNotBoxCompatible(long.class, float.class, true);
    
    _assertNotBoxCompatible(Integer.class, byte.class, true);
    _assertNotBoxCompatible(Long.class, int.class, true);
    
    _assertBoxCompatible(java.util.List.class, java.util.Vector.class, false);
    _assertBoxCompatible(java.util.List.class, java.util.Vector.class, true);
    _assertBoxCompatible(Object.class, Character.class, false);
  }
  private void _assertBoxCompatible(Class c1, Class c2, boolean boxEnabled) {
    assertTrue("Should have been compatible: " + 
               c1 + " <- " + c2 + " (boxing enabled: " + boxEnabled + ")", 
               ReflectionUtilities.isBoxCompatible(c1, c2, boxEnabled));
  }
  private void _assertNotBoxCompatible(Class c1, Class c2, boolean boxEnabled) {
    assertFalse("Shouldn't have been compatible: " + 
               c1 + " <- " + c2 + " (boxing enabled: " + boxEnabled + ")", 
                ReflectionUtilities.isBoxCompatible(c1, c2, boxEnabled));
  }
  private void _exceptionBoxCompatible(Class c1, Class c2, boolean boxEnabled) {
    try {
      ReflectionUtilities.isBoxCompatible(c1, c2, boxEnabled);
      fail("Should have thrown an exception: " + 
           c1 + " <- " + c2 + " (boxing enabled: " + boxEnabled + ")");
    } 
    catch (WrongVersionException e) { 
      // it's good
    }
  }
}
