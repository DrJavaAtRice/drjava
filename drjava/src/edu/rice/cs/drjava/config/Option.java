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
import java.util.Hashtable;
import java.util.Vector;
// TODO: Change the usage of these classes to Collections style.
// TODO: Do these need to be synchronized?


/**
 * Represents a configurable option in DrJava that has a static (programmatic) type of T.
 * Classes can magically extend this class and the entire rest of the Configuration magic
 * typing framework will work for it.  Named subclasses aren't even necessary -- but 
 * may be convenient in order to re-use code.  For example, to make an anonymous class
 * that handled options of static type Integer, with the name "indent.level", you use the
 * following code:
 * <pre>
 * Option&lt;Integer&gt; INDENT_LEVEL = new Option&lt;Integer&gt;("indent.level") {
 *         public Integer parse(String s) {
 *             return new Integer(s);
 *         }
 *     };
 * </pre>
 * the above example is simple because Integers (like most Java(tm) standard-lib data-type
 * classes) have handy toString() / parsing methods/constructors.
 * 
 * @version $Id$
 */
public abstract class Option<T> extends OptionParser<T> implements FormatStrategy<T> {
  
  
  /**
   * a hashtable that maps Configuration Objects to a list of listeners for this
   * particular option.  Part of the magic inner workings of this package.
   */
  final Hashtable<Configuration,Vector<OptionListener<T>>> listeners=
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
   * @throws NullPointerException if value is null
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
  
  
  /**
   * Sends an OptionEvent to all OptionListeners who have registered on this Option.
   */
  void notifyListeners(Configuration config, T val) {
    Vector<OptionListener<T>> v = listeners.get(config);
    if(v==null) return; // no listeners
    OptionEvent<T> e = new OptionEvent<T>(this,val);
    int size = v.size();
    for(int i = 0; i < size; i++) {
      v.elementAt(i).optionChanged(e);
    }
  }
  
  /** magic listener-bag adder */
  void addListener(Configuration c, OptionListener<T> l) {
    Vector<OptionListener<T>> v = listeners.get(c);
    if(v==null) {
      v = new Vector<OptionListener<T>>();
      listeners.put(c,v);
    }
    v.addElement(l);
  }
  
  /** magic listener-bag remover */
  void removeListener(Configuration c, OptionListener<T> l) {
    Vector<OptionListener<T>> v = listeners.get(c);
    if(v==null) return;
    if(v.removeElement(l) && v.size() == 0) {
      listeners.remove(c);
    }
  }
}




