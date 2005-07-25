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

import edu.rice.cs.util.swing.Utilities;

import java.util.Hashtable;
import java.util.Vector;
// TODO: Change the usage of these classes to Collections style.
// TODO: Do these need to be synchronized?

/**
 * An instance of this class represents a configurable option in DrJava that has static type T.
 * Classes can extend this class and the rest of the Configuration typing framework will work 
 * for it.  Named subclasses aren't even necessary -- but may be convenient in order to re-use 
 * code.  For example, to make an anonymous class that handles options of static type Integer, 
 * with the name "indent.level", you could use the following code:
 * <pre>
 * Option&lt;Integer&gt; INDENT_LEVEL = new Option&lt;Integer&gt;("indent.level") {
 *         public Integer parse(String s) {
 *             return new Integer(s);
 *         }
 *     };
 * </pre>
 * the above example is simple because Integers (like most data-type classes defined in the Java
 * ivaewhave handy toString() / parsing methods/constructors.
 *
 * @version $Id$
 */
public abstract class Option<T> extends OptionParser<T> implements FormatStrategy<T> {

  /**
   * a hashtable that maps Configuration Objects to a list of listeners for this
   * particular option.  Part of the magic inner workings of this package.
   */
  final Hashtable<Configuration,Vector<OptionListener<T>>> listeners =
    new Hashtable<Configuration,Vector<OptionListener<T>>>();

  /**
   * constructor that takes in a name and default value
   * @param name the name of this option (eg. "indent.level");
   * @param def the default value for this option (eg. "2")
   */
  public Option(String name, T def) { super(name,def); }

  /**
   * the ability to format a statically typed T value to a String.  Since T is an Object,
   * the default implementation uses the .toString() method.
   * @param value the statically-typed value to format into a String
   * @throws {@link NullPointerException} if value is null
   */
  public String format(T value) { return value.toString(); }

  public String getDefaultString() { return format(getDefault()); }

  // PACKAGE PRIVATE MAGIC STUFF
  // this package-private magic stuff makes all of the config "magic" types work.
  // basically, it's achieved via a double-dispatch stunt, so that the type information
  // is saved.

  /**
   * uses format() and getOption() so that any changes in format will automatically
   * be applied to getString().
   */
  String getString(DefaultOptionMap om) {
    return format(getOption(om));
  }


  /** Sends an OptionEvent to all OptionListeners who have registered on this Option. */
  void notifyListeners(Configuration config, T val) {
    final Vector<OptionListener<T>> v = listeners.get(config);
    if (v == null) return; // no listeners
    final OptionEvent<T> e = new OptionEvent<T>(this,val);
    final int size = v.size();
    Utilities.invokeLater(new Runnable() { 
      public void run() {
        for (int i = 0; i < size; i++) v.get(i).optionChanged(e);
      }
    });
  }

  /** magic listener-bag adder */
  void addListener(Configuration c, OptionListener<T> l) {
    Vector<OptionListener<T>> v = listeners.get(c);
    if (v==null) {
      v = new Vector<OptionListener<T>>();
      listeners.put(c,v);
    }
    v.add(l);
  }

  /** magic listener-bag remover */
  void removeListener(Configuration c, OptionListener<T> l) {
    Vector<OptionListener<T>> v = listeners.get(c);
    if (v==null) return;
    if (v.remove(l) && v.size() == 0) {
      listeners.remove(c);
    }
  }
}




