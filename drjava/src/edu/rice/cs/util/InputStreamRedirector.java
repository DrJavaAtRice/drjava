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
   */
  protected ArrayList<Character> _buffer;

  /**
   * constructs a new InputStreamRedirector.
   */
  public InputStreamRedirector() {
    _buffer = new ArrayList<Character>(60);
  }

  /**
   * This method gets called whenever input is requested from the stream and
   * nothing is currently available.  Subclasses should return the appropriate
   * input to feed to the input stream.  When using a readLine() method, be sure
   * to append a newline to the end of the input.
   * @return the input to the stream, not the empty string
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

