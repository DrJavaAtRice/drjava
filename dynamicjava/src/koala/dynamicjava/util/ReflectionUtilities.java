/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.util;

import koala.dynamicjava.interpreter.throwable.WrongVersionException;
  
import java.lang.reflect.*;
import java.util.*;

/**
 * This class contains a collection of utility methods for reflection.
 *
 * @author  Stephane Hillion
 * @version 1.3 - 1999/11/28
 */

public class ReflectionUtilities {
  /**
   * Looks for a constructor in the given class or in super classes of this class.
   * @param cl   the class of which the constructor is a member
   * @param ac   the arguments classes (possibly not the exact declaring classes)
   */
  public static Constructor lookupConstructor(Class cl, Class [] ac)
    throws NoSuchMethodException {
    List<Constructor> ms = getConstructors(cl, ac.length);
    List<Constructor> mm = new LinkedList<Constructor>();
    
    // Search for the constructors with good parameter types and
    // put them in 'mm'
    Iterator<Constructor> it = ms.iterator();
    while (it.hasNext()) {
      Constructor m = it.next();
      if (hasCompatibleSignatures(m.getParameterTypes(), ac)) {
        mm.add(m);
      }
    }
    
    if (mm.isEmpty()) {
      boolean compatibleVersion = Float.valueOf(System.getProperty("java.specification.version")) >= 1.5;
      
      // Autoboxing handled in the isCompatible method called by the hasVarArgsCompatibleSignatures method
      
      // Do second pass for finding a varargs method that matches given method call
      
      ms = getVarArgsConstructors(cl, ac.length);
      
      // Search for the methods with good parameter types and
      // put them in 'mm'
      it = ms.iterator();
      while (it.hasNext()) {
        Constructor m = it.next();
        if (hasVarArgsCompatibleSignatures(m.getParameterTypes(), ac)) {
          if (!compatibleVersion) {
            throw new WrongVersionException("Variable arguments are only supported in Java 1.5 or better");
          }
          mm.add(m);
        }
      }
      
      if(mm.isEmpty()){
        throw new NoSuchMethodException(cl.getName()+" constructor");
      }
    }
    
    // Select the most specific constructor
    it = mm.iterator();
    Constructor result = it.next();
    
    while (it.hasNext()) {
      result = selectTheMostSpecificConstructor(result, it.next());
    }
    
    return result;
  }
  
  /**
   * Gets all the constructors in the given class or super classes,
   * even the redefined constructors are returned.
   * @param cl     the class where the constructor was declared
   * @param params the number of parameters
   * @return a list that contains the found constructors, an empty list if no
   *         matching constructor was found.
   */
  public static List<Constructor> getConstructors(Class cl, int params) {
    List<Constructor>  result = new LinkedList<Constructor>();
    Constructor[] ms = cl.getDeclaredConstructors();
    
    for (int i = 0; i < ms.length; i++) {
      if (ms[i].getParameterTypes().length == params) {
        result.add(ms[i]);
      }
    }
    return result;
  }
  
  /**
   * Gets all the variable arguments constructors in the given class or super classes,
   * even the redefined constructors are returned.
   * @param cl     the class where the varargs constructor was declared
   * @param params the number of parameters
   * @return a list that contains the found constructors, an empty list if no
   *         matching constructor was found.
   */
  public static List<Constructor> getVarArgsConstructors(Class cl, int params) {
    List<Constructor>  result = new LinkedList<Constructor>();
    Constructor[] ms = cl.getDeclaredConstructors();
    
    for (int i = 0; i < ms.length; i++) {
      if (ms[i].isVarArgs() && ms[i].getParameterTypes().length <= params) {
        result.add(ms[i]);
      }
    }
    return result;
  }
  
  /**
   * Looks for a method in the given class or in super classes of this class.
   * @param cl   the class of which the method is a member
   * @param name the name of the method
   * @param ac   the arguments classes (possibly not the exact declaring classes)
   */
  public static Method lookupMethod(Class cl, String name, List<Class> ac)
    throws NoSuchMethodException {
    return lookupMethod(cl, name, ac.toArray(new Class[0]));  
  }
  
  /**
   * Looks for a method in the given class or in super classes of this class.
   * @param cl   the class of which the method is a member
   * @param name the name of the method
   * @param ac   the arguments classes (possibly not the exact declaring classes)
   */
  public static Method lookupMethod(Class cl, String name, Class[] ac)
    throws NoSuchMethodException {
    List<Method> ms = getMethods(cl, name, ac.length);
    List<Method> mm = new LinkedList<Method>();
    
    // Search for the methods with good parameter types and
    // put them in 'mm'
    Iterator<Method> it = ms.iterator();
    while (it.hasNext()) {
      Method m = it.next();
      if (hasCompatibleSignatures(m.getParameterTypes(), ac)) {
        mm.add(m);
      }
    }
    
    if (mm.isEmpty()) {
      boolean compatibleVersion = Float.valueOf(System.getProperty("java.specification.version")) >= 1.5;
      
      // Autoboxing handled in the isCompatible method called by the hasVarArgsCompatibleSignatures method
      
      // Do second pass for finding a varargs method that matches given method call
      
      ms = getVarArgsMethods(cl, name, ac.length);
      
      // Search for the methods with good parameter types and
      // put them in 'mm'
      it = ms.iterator();
      while (it.hasNext()) {
        Method m = it.next();
        
        if (hasVarArgsCompatibleSignatures(m.getParameterTypes(), ac)) {
          if (!compatibleVersion) {
            throw new WrongVersionException("Variable arguments are only supported in Java 1.5 or better");
          }
          mm.add(m);
        }
      }
      
      if(mm.isEmpty()){
        throw new NoSuchMethodException(name);
      }
    }
    
    // Select the most specific method
    it = mm.iterator();
    Method result = it.next();
    
    while (it.hasNext()) {
      result = selectTheMostSpecificMethod(result, it.next()); /**/// may be confused by mm having varargs and need to be changed
    }
    
    return result;
  }
  
  /**
   * Gets all the methods with the given name in the given class or super classes.
   * Even the redefined methods are returned.
   * @param cl     the class where the method was declared
   * @param name   the name of the method
   * @param params the number of parameters
   * @return a list that contains the found methods, an empty list if no
   *         matching method was found.
   */
  public static List<Method> getMethods(Class cl, String name, int params) {
    List<Method>  result = new LinkedList<Method>();
    
    if (cl.isInterface()) {
      Method[] ms = cl.getDeclaredMethods();
      for (int i = 0; i < ms.length; i++) {
        if (ms[i].getName().equals(name) &&
            ms[i].getParameterTypes().length == params) {
          result.add(ms[i]);
        }
      }
      Class[] cs = cl.getInterfaces();
      for (int i = 0; i < cs.length; i++) {
        result.addAll(getMethods(cs[i], name, params));
      }
      if (cs.length == 0) {
        result.addAll(getMethods(Object.class, name, params));
      }
    } 
    else {
      Class c = cl;
      while (c != null) {
        Method[] ms = c.getDeclaredMethods();
        
        for (int i = 0; i < ms.length; i++) {
          if (ms[i].getName().equals(name) &&
              ms[i].getParameterTypes().length == params) {
            result.add(ms[i]);
          }
        }
        c = c.getSuperclass();
      }
    }
    return result;
  }
  
  /**
   * Gets all the varargs methods with the given name in the given class or super classes.
   * Even the redefined methods are returned.
   * @param cl     the class where the method was declared
   * @param name   the name of the method
   * @param params the number of parameters
   * @return a list that contains the found methods, an empty list if no
   *         matching method was found.
   */
  public static List<Method> getVarArgsMethods(Class cl, String name, int params) {
    List<Method>  result = new LinkedList<Method>();
    
    if (cl.isInterface()) {
      Method[] ms = cl.getDeclaredMethods();
      for (int i = 0; i < ms.length; i++) {
        if (ms[i].getName().equals(name) &&
            ms[i].isVarArgs() &&  // Use new 1.5 API
            ms[i].getParameterTypes().length <= params) {
          result.add(ms[i]);
        }
      }
      Class[] cs = cl.getInterfaces();
      for (int i = 0; i < cs.length; i++) {
        result.addAll(getVarArgsMethods(cs[i], name, params));
      }
      if (cs.length == 0) {
        result.addAll(getVarArgsMethods(Object.class, name, params));
      }
    } 
    else {
      Class c = cl;
      while (c != null) {
        Method[] ms = c.getDeclaredMethods();
        
        for (int i = 0; i < ms.length; i++) {
          if (ms[i].getName().equals(name) &&
              ms[i].isVarArgs() &&  // Use new 1.5 API
              ms[i].getParameterTypes().length <= params) {
            result.add(ms[i]);
          }
        }
        c = c.getSuperclass();
      }
    }
    return result;
  }
  
  /**
   * Looks up for a method in an outer classes of this class.
   * @param cl   the inner class 
   * @param name the name of the method
   * @param ac   the arguments classes (possibly not the exact declaring classes)
   */
  public static Method lookupOuterMethod(Class cl, String name, Class[] ac)
    throws NoSuchMethodException {
    boolean sc = Modifier.isStatic(cl.getModifiers());
    Class c = (cl != null) ? cl.getDeclaringClass() : null;
    while (c != null) {
      sc |= Modifier.isStatic(c.getModifiers());
      try {
        Method m = lookupMethod(c, name, ac);
        if (!sc || Modifier.isStatic(m.getModifiers())) {
          return m;
        }
      } catch (NoSuchMethodException e) {
      }
      c = c.getDeclaringClass();
    }
    throw new NoSuchMethodException(name);
  }
  
  /**
   * Returns a field with the given name declared in the given
   * class or in the superclasses of the given class
   * @param cl   the class where the field must look for the field
   * @param name the name of the field
   */
  public static Field getField(Class cl, String name)
    throws NoSuchFieldException, AmbiguousFieldException {
    Class c = cl;
    while (c != null) {
      try {
        return c.getDeclaredField(name);
      } catch(NoSuchFieldException e) {
        Class[] ints = c.getInterfaces();
        Field f = null;
        for (int i = 0; i < ints.length; i++) {
          Field tmp = null;
          try {
            tmp = getField(ints[i], name);
          } catch(NoSuchFieldException ex) {
          }
          if (tmp != null) {
            if (f != null && !f.equals(tmp)) {
              throw new AmbiguousFieldException(name);
            }
            f = tmp;
          }
        }
        if (f != null) {
          return f;
        }
      }
      c = c.getSuperclass();
    }
    throw new NoSuchFieldException(name);
  }
  
  /**
   * Returns a field with the given name declared in one of the outer
   * classes of the given class
   * @param cl   the inner class
   * @param name the name of the field
   */
  public static Field getOuterField(Class cl, String name)
    throws NoSuchFieldException, AmbiguousFieldException {
    boolean sc = Modifier.isStatic(cl.getModifiers());
    Class   c  = (cl != null) ? cl.getDeclaringClass() : null;
    while (c != null) {
      sc |= Modifier.isStatic(c.getModifiers());
      try {
        Field f = getField(c, name);
        if (!sc || Modifier.isStatic(f.getModifiers())) {
          return f;
        }
      } catch (NoSuchFieldException e) {
      }
      c = c.getDeclaringClass();
    }
    throw new NoSuchFieldException(name);
  }
  
  /**
   * Returns the method with the most specific signature.
   * It is assumed that m1 and m2 have the same number of parameters.
   */
  protected static Method selectTheMostSpecificMethod(Method m1, Method m2) {
    Class [] a1 = m1.getParameterTypes();
    Class [] a2 = m2.getParameterTypes();
    
    for (int i = 0; i < a1.length; i++) {
      if (a1[i] != a2[i]) {
        return (isCompatible(a1[i], a2[i])) ? m2 : m1;
      }
    }
    return m1;
  }
  
  /**
   * Returns the constructor with the most specific signature.
   * It is assumed that m1 and m2 have the same number of parameters.
   */
  protected static Constructor selectTheMostSpecificConstructor(Constructor c1,
                                                                Constructor c2) {
    Class [] a1 = c1.getParameterTypes();
    Class [] a2 = c2.getParameterTypes();
    
    for (int i = 0; i < a1.length; i++) {
      if (a1[i] != a2[i]) {
        if (isCompatible(a1[i], a2[i])) {
          return c2;
        } else {
          return c1;
        }
      }
    }
    
    return c1;
  }
  
  /**
   * For each element (class) of the given arrays, tests if the first array
   * element is assignable from the second array element. The two arrays are
   * assumed to have the same length.
   */
  public static boolean hasCompatibleSignatures(Class[] a1, Class[] a2) {
    for (int i = 0; i < a1.length; i++) {
      if (!isCompatible(a1[i], a2[i])) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * For each element (class) of the given arrays, tests if the first array
   * element is assignable from the second array element. The two arrays are
   * assumed to have the same length.
   */
  public static boolean hasVarArgsCompatibleSignatures(Class[] a1, Class[] a2) {
    for (int i = 0; i < a1.length-1; i++) { //  a2 can have length larger than or equal to a1
      if (!isCompatible(a1[i], a2[i])) {
        return false;
      }
    }
    if(!a1[a1.length-1].isArray()){
      return false; 
      // in fact it indicates a more serious error that should be reported to DynamicJava 
      // developers. That's, for the time being, US!
    }
    Class VarArgsType = a1[a1.length-1].getComponentType(); // Get the element type of the array
    for( int i = a1.length-1; i < a2.length; i++ ){
      if(!isCompatible(VarArgsType, a2[i])){
        return false;
      }
    }
    return true;
  }
  
  /**
   * Whether 'c1' is assignable from 'c2'
   */
  public static boolean isCompatible(Class c1, Class c2) {
    return isBoxCompatible(c1, c2, Float.valueOf(System.getProperty("java.specification.version")) >= 1.5);
    
    /** Commented by Jonathan Lugo 2004-05-18.  Code moved to isBoxCompatible**/
//    if (c1.isPrimitive()) {
//      if (c1 != c2) {
//        if (c1 == int.class) {
//          return (c2 == byte.class  ||
//                  c2 == short.class ||
//                  c2 == char.class);
//        } 
//        else if (c1 == long.class) {
//          return (c2 == byte.class  ||
//                  c2 == short.class ||
//                  c2 == int.class);
//        } 
//        else if (c1 == short.class) {
//          return c2 == byte.class;
//        } 
//        else if (c1 == float.class) {
//          return (c2 == byte.class  ||
//                  c2 == short.class ||
//                  c2 == int.class   ||
//                  c2 == long.class);
//        } 
//        else if (c1 == double.class) {
//          return (c2 == byte.class  ||
//                  c2 == short.class ||
//                  c2 == int.class   ||
//                  c2 == long.class  ||
//                  c2 == float.class);
//        } 
//        else { // it's a boolean && c1 != c2
//          return false;
//        }
//      }
//      else { // c1 == c2
//        return true;
//      }
//    } 
//    else { // It's a reference type
//      return (c2 == null) ? true : c1.isAssignableFrom(c2);
//    }
  }
  
  private static boolean _isBoxingType(Class c) {
    return (c == Integer.class   || c == Long.class   ||
            c == Boolean.class   || c == Double.class ||
            c == Character.class || c == Short.class  ||
            c == Byte.class      || c == Float.class );
  }
  
  /**
   * Returns the reference type that corresponds to the given primitive type.
   * @param primType the primitive type
   * @return the corresponding reference type
   */
  protected static Class _correspondingBoxingType(Class primType) {
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
  protected static Class _correspondingPrimType(Class refType) {
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
  public static boolean isBoxCompatible(Class c1, Class c2, boolean autoBoxEnabled) {
    if (c1.isPrimitive()) {
      
        
      if (!c2.isPrimitive() && !autoBoxEnabled) {
        // We know autoboxing/unboxing is required but
        // the version of java doesn't support it
        throw new WrongVersionException("Auto-unboxing only supported in Java 1.5 or better");
      }
      else {
        // unbox the second type (may not change)
        c2 = _correspondingPrimType(c2);
      }
      
      if (c1 != c2) {
        if (c1 == int.class) {
          return (c2 == byte.class  ||
                  c2 == short.class ||
                  c2 == char.class);
        } 
        else if (c1 == long.class) {
          return (c2 == byte.class  ||
                  c2 == short.class ||
                  c2 == int.class);
        } 
        else if (c1 == short.class) {
          return c2 == byte.class;
        } 
        else if (c1 == float.class) {
          return (c2 == byte.class  ||
                  c2 == short.class ||
                  c2 == int.class   ||
                  c2 == long.class);
        } 
        else if (c1 == double.class) {
          return (c2 == byte.class  ||
                  c2 == short.class ||
                  c2 == int.class   ||
                  c2 == long.class  ||
                  c2 == float.class);
        } 
        else { // it's a boolean && c1 != c2
          return false;
        }
      }
      else { // c1 == c2
        return true;
      }
    }
    else { // It's a reference type
      if (c2 != null && c2.isPrimitive()) {
        if (!autoBoxEnabled) {
           throw new WrongVersionException("Auto-boxing/unboxing is only supported in Java 1.5 or better");
        }
        c2 = _correspondingBoxingType(c2);
      }
      return (c2 == null) ? true : c1.isAssignableFrom(c2);
    }
  }
  
  /**
   * This class contains only static methods, so it is not useful
   * to create instances of it.
   */
  protected ReflectionUtilities() {
  }
}
