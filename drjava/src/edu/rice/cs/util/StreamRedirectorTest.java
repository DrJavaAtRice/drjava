/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util;

import edu.rice.cs.drjava.DrJavaTestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Test suite over InputStreamRedirector.
 */
public class StreamRedirectorTest extends DrJavaTestCase {
  /** Tests that an InputStreamRedirector correctly interprets empty input as end of stream. */
  public void testEmptyInput() throws IOException {
    InputStreamRedirector isr = new InputStreamRedirector() {
      protected String _getInput() {
        return "";
      }
    };
    assertEquals("Should return -1 to indicate end of stream", -1, isr.read());
  }

  /** Tests that an InputStreamRedirector correctly redirects input that is static.
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

  /** Tests that an InputStreamRedirector correctly redirects input that changes.
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

  /** Tests that an InputStreamRedirector correctly calls _getInput() only
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