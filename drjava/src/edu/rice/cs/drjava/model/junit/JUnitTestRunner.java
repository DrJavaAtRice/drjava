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

package edu.rice.cs.drjava.model.junit;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.DefaultGlobalModel;

import java.io.PrintStream;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;

import junit.runner.*;
import junit.textui.TestRunner;

/**
 * DrJava's own testrunner. It updates the document in the
 * JUnit pane as error and failure events are fired.
 *
 * @version $Id$
 */
public class JUnitTestRunner extends junit.textui.TestRunner {
  /**
   * The document in the JUnit pane in the UI
   * to write to.
   */
  private StyledDocument _doc;

  /**
   * Used to tie the output of the ui textrunner
   * to our pane.
   */
  private PrintStream _writer;

  /**
   * Class loader that uses DrJava's classpath
   */
  TestSuiteLoader _classLoader;

  /**
   * Constructor
   */
  public JUnitTestRunner(GlobalModel model) {
    super();
    _doc = model.getJUnitDocument();
    _classLoader = new DrJavaTestClassLoader(model);
    _writer =  new PrintStream(System.out) {
      public void print(String s) {
        _docAppend(s);
      }
      public void println(String s) {
        print(s + "\n");
      }
      public void println() {
        print("\n");
      }
    };
  }

  /**
   * Provides our own PrintStream which outputs
   * to the appropriate document;
   */
  protected PrintStream getWriter() {
    return _writer;
  }

  protected PrintStream writer() {
    return getWriter();
  }

  /**
   * Writes text to the document in the JUnit
   * pane.
   */
  private void _docAppend(String text) {
    try {
      _doc.insertString(_doc.getLength(), 
                        text, 
                        DefaultGlobalModel.SYSTEM_OUT_STYLE);
    } 
    catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
  }

  /**
   * Overrides method in super class to always return
   * a reloading test suite loader.
   */
  public TestSuiteLoader getLoader() {
    return _classLoader;
  }
  
  /**
   * Checks whether the given file name corresponds to 
   * a valid JUnit TestCase.
   */
  public boolean isTestCase(String fileName) throws ClassNotFoundException {
    return Class.forName("junit.framework.TestCase")
      .isAssignableFrom(loadSuiteClass(fileName));
  }

  /**
   * Overrides method in super class to print
   * failed message to the JUnit console when
   * a TestSuite could not be loaded.
   * Should this pop up a window instead?
   */
  protected void runFailed(String message) {
    _docAppend(message);
  }
}


