/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.config;

import edu.rice.cs.util.swing.Utilities;

import java.util.HashMap;
import java.util.Vector;
// TODO: Change the usage of these classes to Collections style.
// TODO: Do these need to be synchronized?

/** An instance of this class represents a configurable option in DrJava that has static type T.  Classes can extend
  * this class and the rest of the Configuration typing framework will work for it.  Named subclasses aren't even 
  * necessary -- but may be convenient in order to re-use code.  For example, to make an anonymous class that handles
  * options of static type Integer, with the name "indent.level", you could use the following code:
  * <pre>
  * Option&lt;Integer&gt; INDENT_LEVEL = new Option&lt;Integer&gt;("indent.level") {
  *         public Integer parse(String s) {
  *             return new Integer(s);
  *         }
  *     };
  * </pre>
  * The precedinjg example is simple because Integers (like most data-type classes defined in the Java
  * libraries) have handy toString() / parsing methods/constructors.
  *
  * @version $Id: Option.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class Option<T> extends OptionParser<T> implements FormatStrategy<T> {
  /** A hashtable that maps Configuration objects to a list of listeners for this particular option.  Part of the magic
    * inner workings of this package.
    */
  final HashMap<Configuration,Vector<OptionListener<T>>> listeners =
    new HashMap<Configuration,Vector<OptionListener<T>>>();
  
  /** Constructor that takes in a name and default value
    * @param name the name of this option (eg. "indent.level");
    * @param def the default value for this option (eg. "2")
    */
  public Option(String name, T def) { super(name,def); }
  
  /** Formats a statically typed T value as a String.  The default implementation uses the toString() method.
    * @param value the statically-typed value to format into a String
    * @throws NullPointerException if value is null
    */
  public String format(T value) { return value.toString(); }
  
  public String getDefaultString() { return format(getDefault()); }
  
  /* PACKAGE PRIVATE MAGIC STUFF
   * This package-private magic stuff makes all of the config "magic" types work. Basically, it's achieved using 
   * double-dispatch, so that the type information is saved. 
   */
  
  /** Uses format() and getOption() so that any changes in format will automatically be applied to getString(). */
  String getString(DefaultOptionMap om) { return format(getOption(om)); }
  
  /** Sends an OptionEvent to all OptionListeners who have registered on this Option. */
  synchronized void notifyListeners(Configuration config, T val) {
    final Vector<OptionListener<T>> v = listeners.get(config);
//    System.err.println("Notifying " + v + " with value " + val);
    if (v == null) return; // no listeners
    final OptionEvent<T> e = new OptionEvent<T>(this, val);
//    System.err.println("OptionEvent = " + e);
    Utilities.invokeLater(new Runnable() { 
      public void run() {
        for (int i = 0; i < v.size(); ++i) v.get(i).optionChanged(e);
      }
    });
  }
  
  /** Magic listener-bag adder */
  synchronized void addListener(Configuration c, OptionListener<T> l) {
    Vector<OptionListener<T>> v = listeners.get(c);
    if (v == null) {
      v = new Vector<OptionListener<T>>();
      listeners.put(c,v);
    }
    v.add(l);
  }
  
  /** Magic listener-bag remover */
  synchronized void removeListener(Configuration c, OptionListener<T> l) {
    Vector<OptionListener<T>> v = listeners.get(c);
    if (v != null && v.remove(l) && v.size() == 0) listeners.remove(c);  // v.remove(l) has a side effect!
  }
}




