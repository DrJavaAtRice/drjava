/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
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
