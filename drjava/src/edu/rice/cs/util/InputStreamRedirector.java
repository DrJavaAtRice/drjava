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

/**
 * Redirects requests for input through the abstract method _getInput().
 * @version $Id$
 */
public abstract class InputStreamRedirector extends PipedInputStream {
  /**
   * The piped output stream that will write the output from _getInput()
   * to the actual input stream.
   */
  protected PipedOutputStream _os;

  /**
   * constructs a new InputStreamRedirector.
   * @throws IOException if an I/O error occurs
   */
  public InputStreamRedirector() throws IOException {
    _os = new PipedOutputStream(this);
  }

  /**
   * This method gets called whenever input is requested from the stream and
   * nothing is currently available.  Subclasses should return the appropriate
   * input to feed to the input stream.  When using a readLine() method, be sure
   * to append a newline to the end of the input.
   * @return the input to the stream
   */
  protected abstract String _getInput() throws IOException;

  /**
   * overrides the read() in PipedInputStream so that if the stream is empty
   * it will ask for more input from _getInput().
   * @return the next character in the stream
   * @throws IOException if an I/O exception
   */
  public int read() throws IOException {
    if(available() == 0) {
      String input = _getInput();
      for(int i = 0; i < input.length(); i++) {
        _os.write((int) input.charAt(i));
      }
    }
    return super.read();
  }
}