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

import java.io.IOException;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

// TODO: remove this gratuitous swing dependency!
import javax.swing.text.StyledDocument;

public interface JUnitModel {

  //-------------------------- Listener Management --------------------------//

  /**
   * Add a JUnitListener to the model.
   * @param listener a listener that reacts to JUnit events
   */
  public void addListener(JUnitListener listener);

  /**
   * Remove a JUnitListener from the model.  If the listener is not currently
   * listening to this model, this method has no effect.
   * @param listener a listener that reacts to JUnit events
   */
  public void removeListener(JUnitListener listener);

  /**
   * Removes all JUnitListeners from this model.
   */
  public void removeAllListeners();

  //-------------------------------- Triggers --------------------------------//

  /**
   * This is used by test cases and perhaps other things.  We should kill it.
   * TODO: remove this gratuitous swing dependency!
   */
  public StyledDocument getJUnitDocument();

  /**
   * Creates a JUnit test suite over all currently open documents and runs it.
   * If the class file associated with a file is not a test case, it will be
   * ignored.  Synchronized against the compiler model to prevent testing and
   * compiling at the same time, which would create invalid results.
   */
  public void junitAll();

  /**
   * Runs JUnit over a single document.  Synchronized against the compiler model
   * to prevent testing and compiling at the same time, which would create
   * invalid results.
   * @param doc the document to be run under JUnit
   */
  public void junit(OpenDefinitionsDocument doc)
      throws ClassNotFoundException, IOException;

  //----------------------------- Error Results -----------------------------//

  /**
   * Gets the JUnitErrorModel, which contains error info for the last test run.
   */
  public JUnitErrorModel getJUnitErrorModel();

  /**
   * Resets the junit error state to have no errors.
   */
  public void resetJUnitErrors();
}
