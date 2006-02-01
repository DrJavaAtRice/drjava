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
import java.util.Hashtable;
/**
 * the association of an OptionName with the ability to parse something to type T
 * the special property is that if U extends T, then OptionParser<U> extends OptionParser<T>.
 */
public abstract class OptionParser<T> implements ParseStrategy<T> {
    
    /** The logical name of this configurable option (i.e. "indent.size") public because it's final, 
     *  and a String is immutable.
     */
    public final String name;
    private final T defaultValue;

    /** 
     * an inner hashtable that maps DefaultOptionMaps to value T's.
     * part of the magic inner workings of this package.
     */
    final Hashtable<DefaultOptionMap,T> map = new Hashtable<DefaultOptionMap,T>();

    /** 
     * constructor that takes in a name
     * @param name the name of this option (i.e. "indent.level");
     */
    public OptionParser(String name, T def) { this.name = name; defaultValue = def; }
    
    /**
     * accessor for name option
     * @return name of this option (i.e. "indent.level")
     */
    public String getName() { return name; }

    /**
     * @return the default value
     */
    public T getDefault() { return defaultValue; }

    /**
     * @return the default value as a string
     */
    public abstract String getDefaultString();
  
    /**
     * the ability to parse a string to an object of type T.  All concrete versions of this
     * class must override this method to provide some sort of parser implementation.
     * @param value a String to parse
     * @return the statically-typed representation of the string value.
     */
    public abstract T parse(String value);
     
    // PACKAGE PRIVATE MAGIC STUFF
    // this package-private magic stuff makes all of the config "magic" types work.
    // basically, it's achieved via a double-dispatch stunt, so that the type information
    // is saved.

    abstract String getString(DefaultOptionMap om);
    
    /**
     * uses parse() and setOption() so that any changes in parsing will automatically
     * be applied to setString().
     */
    T setString(DefaultOptionMap om, String val) { return setOption(om,parse(val)); }
    
    /** the accessor for the magic-typed hashtable stunt. */
    T getOption(DefaultOptionMap om) { return map.get(om); }

    /** the mutator for the magic-typed hashtable stunt. */
    T setOption(DefaultOptionMap om, T val) { return map.put(om,val); }
    
    /** the destructor for a mapping in the magic-typed hashtable. */
    T remove(DefaultOptionMap om) { return map.remove(om); }
    



}











