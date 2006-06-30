/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2006 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
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

package edu.rice.cs.util;

import java.io.*;
import java.util.ArrayList;

/**
 * Redirects requests for input through the abstract method _getInput().
 * @version $Id$
 */
public abstract class InputStreamRedirector extends InputStream {
  /**
   * Buffer that stores the current set of bytes.
   * TODO: perhaps this should use an array for efficiency
   * This is only used as a char queue.
   */
  protected ArrayList<Character> _buffer;

  /**
   * constructs a new InputStreamRedirector.
   */
  public InputStreamRedirector() {
    _buffer = new ArrayList<Character>(60);
  }

  /** This method gets called whenever input is requested from the stream and
   *  nothing is currently available.  Subclasses should return the appropriate
   *  input to feed to the input stream.  When using a readLine() method, be sure
   *  to append a newline to the end of the input.
   *  @return the input to the stream, not the empty string
   */
  protected abstract String _getInput() throws IOException;

  /**
   * Reads a single "line" of input into the buffer, i.e. makes a single call
   * to _getInput() and puts the result into the buffer.
   * @throws IOException if _getInput() returns the empty string
   */
  private void _readInputIntoBuffer() throws IOException {
    String input = _getInput();
    if (input.equals("")) {
      throw new IOException("_getInput() must return non-empty input!");
    }
    for(int i = 0; i < input.length(); i++) {
      _buffer.add(new Character(input.charAt(i)));
    }
  }

  /**
   * tries to fill b with bytes from the user, prompting for input only
   * if the stream is already empty.
   * @param b the byte array to fill
   * @return the number of bytes successfully read
   */
  public synchronized int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  /**
   * tries to fill b with bytes from the user, prompting for input only
   * if the stream is already empty.
   * @param b the byte array to fill
   * @param off the offset in the byte array
   * @param len the number of characters to try to read
   * @return the number of bytes successfully read
   */
  public synchronized int read(byte[] b, int off, int len) throws IOException {
    int numRead = 0;
    if (available() == 0) {
      _readInputIntoBuffer();
    }
    for(int i = off; i < off + len; i++) {
      if (available() == 0) {
        break;
      }
      else {
        b[i] = (byte) _buffer.remove(0).charValue();
        numRead++;
      }
    }
    return numRead;
  }

  /**
   * overrides the read() in PipedInputStream so that if the stream is empty
   * it will ask for more input from _getInput().
   * @return the next character in the stream
   * @throws IOException if an I/O exception
   */
  public synchronized int read() throws IOException {
    if (available() == 0) {
      _readInputIntoBuffer();
    }
    return _buffer.remove(0).charValue();
  }

  /**
   * @return the number of characters available in this stream.
   */
  public int available() {
    return _buffer.size();
  }
}

