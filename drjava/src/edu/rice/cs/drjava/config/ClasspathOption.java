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

package edu.rice.cs.drjava.config;
import java.io.File;
import java.util.Vector;
import java.awt.Color;
import java.awt.Font;
import edu.rice.cs.drjava.DrJava;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.Toolkit;
import edu.rice.cs.drjava.CodeStatus;


/**
 * Generate vector options separately to appease javadoc.
 * (It didn't like anonymous inner classes with generics in interfaces in Java 1.3.)
 */
class ClasspathOption {
  private String warning =
    "WARNING: Configurability interface only supports path separators"+
    " of maximum length 1 character as of this moment.";
  
  public VectorOption<File> evaluate(String optionName) {
    // system path separator
    String ps = System.getProperty("path.separator");
    if(ps.length() > 1) {
      // spit out warning if it's more than one character.
      System.err.println(warning);
      System.err.println("using '"+ps.charAt(0)+
                         "' for delimiter.");
    }
    FileOption fop = new FileOption("",FileOption.NULL_FILE);
    //String name = "extra.classpath";
    char delim = ps.charAt(0);
    return new VectorOption<File>(optionName,fop,"",delim,"",new Vector<File>());
  }
}
