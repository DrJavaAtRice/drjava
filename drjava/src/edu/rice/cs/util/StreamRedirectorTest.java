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
import java.io.*;

/**
 * Test suite over InputStreamRedirector.
 */
public class StreamRedirectorTest extends TestCase {
  /**
   * Tests that an InputStreamRedirector correctly rejects empty input.
   */
  public void testEmptyInput() throws IOException {
    InputStreamRedirector isr = new InputStreamRedirector() {
      protected String _getInput() {
        return "";
      }
    };
    try {
      isr.read();
      fail("Should have thrown IOException on empty input!");
    }
    catch (IOException ioe) {
      // correct behavior
    }
  }

  /**
   * Tests that an InputStreamRedirector correctly redirects input that is static.
   */
  public void testStaticInput() throws IOException {
    InputStreamRedirector isr = new InputStreamRedirector() {
      protected String _getInput() {
        return "Hello World!\n";
      }
    };
    BufferedReader br = new BufferedReader(new InputStreamReader(isr));
    assertEquals("First read", "Hello World!", br.readLine());
    assertEquals("Second read", "Hello World!", br.readLine());  //behavior should be consistent
  }

  /**
   * Tests that an InputStreamRedirector correctly redirects input that changes.
   */
  public void testDynamicInput() throws IOException {
    InputStreamRedirector isr = new InputStreamRedirector() {
      int x = -1;
      protected String _getInput() {
        x++;
        return x + "\n";
      }
    };
    BufferedReader br = new BufferedReader(new InputStreamReader(isr));
    assertEquals("First read", "0", br.readLine());
    // x should get incremented on each call
    assertEquals("Second read", "1", br.readLine());
    assertEquals("Third read", "2", br.readLine());
  }

  /**
   * Tests that an InputStreamRedirector correctly calls _getInput() only
   * when it is needed.
   */
  public void testMultiLineInput() throws IOException {
    InputStreamRedirector isr = new InputStreamRedirector() {
      private boolean alreadyCalled = false;

      protected String _getInput() {
        if (alreadyCalled) {
          throw new RuntimeException("_getInput() has already been called!");
        }
        alreadyCalled = true;
        return "Line 1\nLine 2\n";
      }
    };
    BufferedReader br = new BufferedReader(new InputStreamReader(isr));
    assertEquals("First read calls _getInput()", "Line 1", br.readLine());
    assertEquals("First read does not call _getInput()", "Line 2", br.readLine());
    try {
      br.readLine();
      fail("_getInput() should be called again!");
    }
    catch(RuntimeException re) {
      assertEquals("Should have thrown correct exception.",
                   "_getInput() has already been called!", re.getMessage());
    }
  }
}