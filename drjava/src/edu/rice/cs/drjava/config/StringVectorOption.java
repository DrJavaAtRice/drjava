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

import gj.util.Vector;

/**
 * Class defining all configuration options with values of type Vector<String>.
 */
public class StringVectorOption extends VectorOption<String>
{  
  /**
   * @param key The name of this Option.
   */
  public StringVectorOption(String key) { super(key); }
  
  /**
   * @param s String representation of an instance of class Vector<String>.
   * The format of this String is determined by the method Vector<T>.toString().
   * @param i The index of the first character of a distinct String in "s".
   * "Distinct" meaning separated by comma-space, or by a bracket.
   * @param v The instance of class Vector<String> to which
   * the parsed new element is being added.
   * @return The index of the first character of the next
   * distinct String in "s", or the index of the last character in "s"
   * if this call parses the last distinct String in "s".
   * @exception IllegalArgumentException "s" is not a legal String
   * representation of an instance of Vector<String>.
   */
  int parseElement(String s, int i, Vector<String> v)
  {
    // Note that this implementation allows to 
    // parse empty Strings, and it will consider
    // the String [a,b,c] as a Vector with ONE element:
    // it has no comma-space delimiters.
    
    char c;
    
    // Find the end of the current substring.
    for (String current = ""; i < s.length(); i++)
    {
      c = s.charAt(i);
      if (c == ']') 
      {
        v.addElement(current); return i;
      }
      else if (c == ',' && s.charAt(i+1) == ' ') 
      {
        v.addElement(current); return i + 2;
      }
      else current += c;
    }
    // Signal error if no delimiter is found.
    throw new IllegalArgumentException("Input must be String representaion " +
                                         "of a Vector of Integer objects.");    
  }
  
  /**
   * @param s The String to be formatted.
   * @return "s": no actual formatting is necessary for String objects.
   */
  String formatElement(String s) { return s; }
}

