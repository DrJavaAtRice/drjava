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

package edu.rice.cs.util;

import junit.framework.*;
import junit.extensions.*;
import java.io.PrintWriter;

/**
 * Test functions of StringOps.
 */
public class StringOpsTest extends TestCase {
  /**
   *  Test the replace() method of StringOps class
   */
  public void testReplace() {
    String test = "aabbccdd";
    assertEquals("testReplace:", "aab12cdd", StringOps.replace(test, "bc", "12"));
    test = "cabcabc";
    assertEquals("testReplace:", "cabc", StringOps.replace(test, "cabc", "c"));
  }
  
  /**
   *  Test the getOffsetAndLength() method of StringOps class
   */
  public void testGetOffsetAndLength() {
    String test = "123456789\n123456789\n123456789\n";
    Pair<Integer,Integer> oAndL = StringOps.getOffsetAndLength( test, 1, 1, 1, 9 );
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(0), oAndL.getFirst() );
    assertEquals("testGetOffsetAndLength- length:", new Integer(9), oAndL.getSecond() );
   
    oAndL = StringOps.getOffsetAndLength( test, 1, 1, 2, 3 );
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(0), oAndL.getFirst() );
    assertEquals("testGetOffsetAndLength- length:", new Integer(12), oAndL.getSecond() );
    
    oAndL = StringOps.getOffsetAndLength( test, 1, 5, 2, 3 );
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(4), oAndL.getFirst() );
    assertEquals("testGetOffsetAndLength- length:", new Integer(8), oAndL.getSecond() );

    oAndL = StringOps.getOffsetAndLength( test, 1, 1, 1, 1 );
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(0), oAndL.getFirst() );
    assertEquals("testGetOffsetAndLength- length:", new Integer(1), oAndL.getSecond() );
  }

  /**
   * Tests that getting the stack trace of a throwable works correctly.
   */
  public void testGetStackTrace() {
    final String trace = "hello";
    Throwable t = new Throwable() {
      public void printStackTrace(PrintWriter w) {
        w.print(trace);
      }
    };
    assertEquals("Should have returned the correct stack trace!", trace, StringOps.getStackTrace(t));
  }
}