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
import gj.util.*;
/**
 * the association of an OptionName with the ability to parse something to type T
 * the special property is that if U extends T, then OptionParser<U> extends OptionParser<T>.
 */
public abstract class OptionParser<T> implements ParseStrategy<T> {
    
    /** 
     * The logical name of this configurable option (i.e. "indent.size")
     * public because it's final, and a String is immutable.
     */
    public final String name;
    
    /** 
     * an inner hashtable that maps DefaultOptionMaps to value T's.
     * part of the magic inner workings of this package.
     */
    final Hashtable<DefaultOptionMap,T> map =
	new Hashtable<DefaultOptionMap,T>();

    /** 
     * constructor that takes in a name
     * @param name the name of this option (i.e. "indent.level");
     */
    public <T> OptionParser(String name) { this.name = name; }
    
    /**
     * accessor for name option
     * @return name of this option (i.e. "indent.level")
     */
    public String getName() { return name; }
  
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











