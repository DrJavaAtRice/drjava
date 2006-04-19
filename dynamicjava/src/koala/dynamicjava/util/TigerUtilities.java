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



package koala.dynamicjava.util;


import java.lang.reflect.*;
import java.util.*;
import junit.framework.*;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.SourceInfo;
import koala.dynamicjava.util.*;
import koala.dynamicjava.interpreter.error.*;

import java.io.StringReader;
import java.util.List;
import koala.dynamicjava.parser.wrapper.ParserFactory;
import koala.dynamicjava.parser.wrapper.JavaCCParserFactory;

/**
 * Common utilities of DynamicJava for implementing features of 1.5.
 */
public class TigerUtilities {

  // The following constants are pulled from
  // java.lang.reflect.Modifier. This way, 
  // the utility can run under 1.4
  
  public static final int BRIDGE     = 0x00000040;
  public static final int VARARGS    = 0x00000080;
  public static final int SYNTHETIC  = 0x00001000;
  public static final int ANNOTATION = 0x00002000;
  public static final int ENUM       = 0x00004000;
  
  /**
   * The version of the java runtime environment that is in use.
   */
  public static final float VERSION = Float.parseFloat(System.getProperty("java.specification.version"));

  /**
   * Field that is set to determine whether or not features of 1.5 are enabled.
   * Disabling this field and then attempting to use a feature of 1.5 will cause a
   * WrongVersionException to be thrown
   */
  private static boolean _tigerEnabled;
  
  static {
    resetVersion();
  }
  
  
  /**
   * Resets _tigerEnabled based upon the version of the runtime environment that is being used.
   */
  public static void resetVersion() {
    _tigerEnabled = (VERSION > 1.49); // We don't use >= since we're dealing with a float
  }

  /**
   * Accessor method for _tigerEnabled. Returns true if the features in 1.5 are enabled currently.
   * @return boolean - true if 1.5 features are enabled
   */
  public static boolean isTigerEnabled() { return _tigerEnabled; }

  /**
   * Allows the features in 1.5 to be enabled or disabled. Used only in test cases.
   * @param enabled - a boolean that specifies whether or not Tiger features are to be enabled
   */
  public static void setTigerEnabled(boolean enabled) {
    _tigerEnabled = enabled;
  }

  /**
   * To be called before a feature of 1.5 is used. Asserts that Tiger features are enabled.
   * @param msg The error message if 1.5 is not enabled
   */
  public static void assertTigerEnabled(String msg) {
    if(!_tigerEnabled)
      throw new WrongVersionException(msg);
  }

  /**
   * @return @{code true} iff m has the vararg modifier set.
   */
  public static boolean isVarArgs(Method m) {
    return _tigerEnabled && ((m.getModifiers() & VARARGS) != 0);
  }

  /**
   * @return @{code true} iff c has the vararg modifier set.
   */
  public static boolean isVarArgs(Constructor c) {
    return _tigerEnabled && ((c.getModifiers() & VARARGS) != 0);
  }
  
  /**
   * @return @{code true} iff m has the bridge modifier set.
   */
  public static boolean isBridge(Method m) { 
    return _tigerEnabled && ((m.getModifiers() & BRIDGE) != 0);
  }

  /**
   * @return @{code true} iff c has the enum modifier set.
   */
  public static boolean isEnum(Class<?> c) {
    // Since the DynamicJava implementation of EnumDeclaration relies on the presence of constructors,
    // and the restrictions of the JVM won't allow constructors in classes flagged as ENUM,
    // we can't use the ENUM modifier flag here.
    
    // Note: we use "Class.forName" instead of "Enum.class" to avoid Retroweaver conversion of Enum.class
    // to its own Enum implementation.
    try {
      return _tigerEnabled && (c.getSuperclass() != null) && 
        (c.getSuperclass().equals(Class.forName("java.lang.Enum")));
    }
    catch (ClassNotFoundException e) { return false; }
  }

  /**
   * @return @{code true} iff f has the enum modifier set.
   */
  public static boolean isEnumConstant(Field f) {
    return _tigerEnabled && ((f.getModifiers() & ENUM) != 0);
  }

  /**
   * Returns the reference type that corresponds to the given primitive type.
   * If the type given is not primitive, it returns the given type.
   * @param primType the primitive type
   * @return the corresponding reference type
   */
  public static Class<?> correspondingBoxingType(Class<?> primType) {
    if (primType == boolean.class) { return Boolean.class; }
    else if (primType == byte.class) { return Byte.class; }
    else if (primType == char.class) { return Character.class; }
    else if (primType == short.class) { return Short.class; }
    else if (primType == int.class) { return Integer.class; }
    else if (primType == long.class) { return Long.class; }
    else if (primType == float.class) { return Float.class; }
    else if (primType == double.class) { return Double.class; }
    else {
      return primType; // It's already a reference type
    }
  }

  /**
   * Returns the primitive type that corresponds to the given reference type.
   * If the given type is not a boxing type, it returns the given type.
   * @param refType the reference type
   * @return the corresponding primitive type
   */
  public static Class<?> correspondingPrimType(Class<?> refType) {
    if (refType == Boolean.class) { return boolean.class; }
    else if (refType == Byte.class) { return byte.class; }
    else if (refType == Character.class) { return char.class; }
    else if (refType == Short.class) { return short.class; }
    else if (refType == Integer.class) { return int.class; }
    else if (refType == Long.class) { return long.class; }
    else if (refType == Float.class) { return float.class; }
    else if (refType == Double.class) { return double.class; }
    else {
      return refType; // It's already a primitive type
    }
  }

  /**
   * Returns if the given class is a reference type
   * @param c the class that may be a reference type
   * @return boolean that is true if the input class is a boxing type
   */
  public static boolean isBoxingType(Class<?> c) {
    return (c == Integer.class   || c == Long.class   ||
            c == Boolean.class   || c == Double.class ||
            c == Character.class || c == Short.class  ||
            c == Byte.class      || c == Float.class );
  }

    /**
   * Returns true iff the given class is an integral type
   * This includes both primitive and boxing integral types.<br><br>
   * Allowed primitives: byte, char, short, int, long
   * Allowed Refrence: Byte, Character, Short, Integer, Long
   * @param c The class to check
   * @return true iff the given class is an integral type
   */
  public static boolean isIntegralType(Class<?> c) {
    return (c == int.class   || c == Integer.class   ||
            c == long.class  || c == Long.class      ||
            c == byte.class  || c == Byte.class      ||
            c == char.class  || c == Character.class ||
            c == short.class || c == Short.class);
  }

  /**
   * Returns true iff the given primitive class can be boxed
   * to the given reference class.
   * @param prim - the primitive class being boxed
   * @param ref - the reference class being boxed to
   * @return true iff the given primitive class can be boxed to the given reference class
   */
  public static boolean boxesTo(Class<?> prim, Class<?> ref) {
    return
      ((prim == int.class    && ref == Integer.class)     ||
      (prim == long.class    && ref == Long.class)        ||
      (prim == byte.class    && ref == Byte.class)        ||
      (prim == char.class    && ref == Character.class)   ||
      (prim == short.class   && ref == Short.class)       ||
      (prim == boolean.class && ref == Boolean.class)     ||
      (prim == float.class   && ref == Float.class)       ||
      (prim == double.class  && ref == Double.class));
  }
}
