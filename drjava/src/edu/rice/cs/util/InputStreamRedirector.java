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

package edu.rice.cs.util;

import java.io.*;
import java.util.ArrayList;

/** Redirects requests for input through the abstract method _getInput().
  * @version $Id: InputStreamRedirector.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class InputStreamRedirector extends InputStream {
  /** Buffer that stores the current set of bytes.
    * TODO: perhaps this should use an array for efficiency
    * This is only used as a char queue.
    */
  protected volatile ArrayList<Character> _buffer;

  /** Constructs a new InputStreamRedirector. */
  public InputStreamRedirector() { _buffer = new ArrayList<Character>(60); }

  /** This method gets called whenever input is requested from the stream and
    * nothing is currently available.  Subclasses should return the appropriate
    * input to feed to the input stream.  When using a readLine() method, be sure
    * to append a newline to the end of the input.
    * @return the input to the stream, empty string to indicate end of stream
    */
  protected abstract String _getInput() throws IOException;

  /** Reads a single "line" of input into the buffer, i.e. makes a single call
    * to _getInput() and puts the result into the buffer.
    */
  private void _readInputIntoBuffer() throws IOException {
    String input = _getInput();

    for(int i = 0; i < input.length(); i++) {
      _buffer.add(new Character(input.charAt(i)));
    }
  }

  /** Tries to fill b with bytes from the user, prompting for input only if the stream is already empty.
    * @param b the byte array to fill
    * @return the number of bytes successfully read
    */
  public synchronized int read(byte[] b) throws IOException { return read(b, 0, b.length); }

  /** Tries to fill b with bytes from the user, prompting for input only if the stream is already empty.
    * @param b the byte array to fill
    * @param off the offset in the byte array
    * @param len the number of characters to try to read
    * @return the number of bytes successfully read
    */
  public synchronized int read(byte[] b, int off, int len) throws IOException {
    int numRead = 0;
    if (available() == 0) {
      _readInputIntoBuffer();
      if (available() == 0) return -1;
    }

    for(int i = off; i < off + len; i++) {
      if (available() == 0) break;
      else {
        b[i] = (byte) _buffer.remove(0).charValue();
        numRead++;
      }
    }
    return numRead;
  }

  /** Overrides the read() in PipedInputStream so that if the stream is empty, it asks for more input from _getInput().
    * @return the next character in the stream
    * @throws IOException if an I/O exception
    */
  public synchronized int read() throws IOException {
    if (available() == 0) {
      _readInputIntoBuffer();
      if (available() == 0) return -1;
    }
    return _buffer.remove(0).charValue();
  }

  /** @return the number of characters available in this stream. */
  public int available() { return _buffer.size(); }
}

