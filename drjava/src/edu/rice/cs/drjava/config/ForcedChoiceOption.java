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
import java.awt.*;
import java.util.Collection;
import java.util.Iterator;


/**
 * Class defining a configuration option that requires a choice between
 * mutually-exclusive possible values.  Values are stored as Strings, though
 * this could be extended to any type with a fairly simple refactoring.
 * @version $Id$
 */
public class ForcedChoiceOption extends Option<String>
{
  private Collection<String> _choices;
  
  /**
   * @param key The name of this option.
   * @param def The default value of the option.
   * @param choices A collection of all possible values of this Option, as Strings.
   */
  public ForcedChoiceOption(String key, String def, Collection<String> choices) {
    super(key,def);
    _choices = choices;
  }
  
  /**
   * Checks whether the parameter String is a legal value for this option.
   * The input String must be formatted exactly like the original, as defined
   * by String.equals(String).
   * @param s the value to check
   * @return true if s is legal, false otherwise
   */
  public boolean isLegal(String s) {
    return _choices.contains(s);
  }
  
  /**
   * Gets all legal values of this option.
   * @return an Iterator containing the set of all Strings for which isLegal returns true.
   */
  public Iterator<String> getLegalValues() {
    return _choices.iterator();
  }
  
  /**
   * Gets the number of legal values for this option.
   * @return an int indicating the number of legal values.
   */
  public int getNumValues() {
    return _choices.size();
  }
  
  /**
   * Parses an arbitrary String into an acceptable value for this option.
   * @param s The String to be parsed.
   * @return s, if s is a legal value of this option.
   * @exception IllegalArgumentException if "s" is not one of the allowed values.
   */
  public String parse(String s) 
  {
    if (isLegal(s)) {
      return s;
    }
    else {
      throw new OptionParseException(name, s,
                                     "Value is not an acceptable choice " +
                                     "for this option.");
    }
  }
  
  /**
   * @param s The String to be formatted.
   * @return "s", no actual formatting is performed.
   */
  public String format(String s) { return s; }
}
