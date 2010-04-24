/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.config;

import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

/** The association of an OptionName with the ability to parse something to type T; the intended type 
  * parameterization is covariant: if U extends T, then OptionParser<U> extends OptionParser<T>.
  */
public abstract class OptionParser<T> implements ParseStrategy<T> {
  
  /** The logical name of this configurable option (i.e. "indent.size") public because it's final, 
    * and a String is immutable.
    */
  public final String name;
  protected final T defaultValue;
  
  /** An inner hashtable that maps DefaultOptionMaps to value T's. Part of the magic inner workings of this package. */
  final HashMap<DefaultOptionMap,T> map = new HashMap<DefaultOptionMap,T>();
  
  /** Constructor that takes in a name
    * @param name the name of this option (i.e. "indent.level");
    * @param def default value
    */
  public OptionParser(String name, T def) {
      this.name = name; defaultValue = def;
  }
  
  /** Accessor for name option
    * @return name of this option (i.e. "indent.level")
    */
  public String getName() { return name; }
  
  /** @return the default value */
  public T getDefault() { return defaultValue; }
  
  /** @return the default value as a string */
  public abstract String getDefaultString();
  
  /** The ability to parse a string to an object of type T.  All concrete versions of this class must override this
    * method to provide some sort of parser implementation.
    * @param value a String to parse
    * @return the statically-typed representation of the string value.
    */
  public abstract T parse(String value);
  
  /** Returns a string representation of this OptionParser/Option suitable for debugging. */
  public String toString() { return "Option<" + name + ", " + defaultValue + ">"; }
  
  /* PACKAGE PRIVATE MAGIC STUFF
   * This package-private magic stuff makes all of the config "magic" types work. Basically, it's achieved via a 
   * double-dispatch stunt, so that the type information is saved. */
  
  abstract String getString(DefaultOptionMap om);
  
  /** Uses parse() and setOption() so that any changes in parsing will automatically be applied to setString(). */
  T setString(DefaultOptionMap om, String val) { return setOption(om,parse(val)); }
  
  /** The accessor for the magic-typed hashtable stunt. */
  T getOption(DefaultOptionMap om) { return map.get(om); }
  
  /** The mutator for the magic-typed hashtable stunt.
    * @return the previous value associated with key, or null if there was no mapping for key.
    * (A null return can also indicate that the map previously associated null with key.) */
  T setOption(DefaultOptionMap om, T val) { return map.put(om,val); }
  
  /** The destructor for a mapping in the magic-typed hashtable. */
  T remove(DefaultOptionMap om) { return map.remove(om); }
}
