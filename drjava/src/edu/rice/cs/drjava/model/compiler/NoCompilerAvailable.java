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

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import edu.rice.cs.util.UnexpectedException;

/**
 * A CompilerInterface implementation for signifying that no compiler is
 * available.
 *
 * @version $Id$
 */
public class NoCompilerAvailable implements CompilerInterface {
  public static final CompilerInterface ONLY = new NoCompilerAvailable();
  private static final String MESSAGE = "No compiler is available.";

  private NoCompilerAvailable() {}

  public CompilerError[] compile(File sourceRoot, File[] files) {
    CompilerError error = new CompilerError(files[0],
                                            -1,
                                            -1,
                                            MESSAGE,
                                            false);

    return new CompilerError[] { error };
  }

  public boolean isAvailable() { return true; }

  public String getName() {
    return "(no compiler available)";
  }

  public String toString() {
    return getName();
  }

  /**
   * Allows us to set the extra classpath for the compilers without referencing the
   * config object in a loaded class file
   */ 
  public void setExtraClassPath( String extraClassPath) {
  }
  
  /**
   * This method allows us to set the JSR14 collections path across a class loader.
   * (cannot cast a loaded class to a subclass, so all compiler interfaces must have this method)
   */
  public void addToBootClassPath( String cp) {
    throw new UnexpectedException( new Exception("Method only implemented in JSR14Compiler"));
  }
}



