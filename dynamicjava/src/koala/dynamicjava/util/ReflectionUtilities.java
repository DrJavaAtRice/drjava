
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
 * Extends TigerUtilities so that it may use its protected methods
 * 
 * @author  Stephane Hillion
 * @version 1.3 - 1999/11/28
 */

public class ReflectionUtilities {
  /**
   * Looks for a constructor in the given class or in super classes of this class.
   * @param cl   the class of which the constructor is a member
   * @param ac   the arguments classes (possibly not the exact declaring classes)
   * @return The constructor with which to instantiate the declaring class given 
   *   the given types of the arguments being given.
   */
  public static Constructor lookupConstructor(Class<?> cl, Class<?> [] ac)
    throws NoSuchMethodException {
    List<Constructor> all = getConstructors(cl, ac.length);
    List<Constructor> compatible = new LinkedList<Constructor>();
    
    // Search for the methods with good parameter types and
    // put them in 'compatible'
    Iterator<Constructor> it = all.iterator();
    while (it.hasNext()) {
      Constructor c = it.next();
      if (hasCompatibleSignatures(c.getParameterTypes(), ac)) {
        compatible.add(c);
      }
    }
    
    // Select the most specific method if any were found
    if (!compatible.isEmpty()) {
      return selectTheMostSpecificConstructor(compatible);
    }
    
    // Do a second pass to search for methods with autoboxing
    
    it = all.iterator();
    while (it.hasNext()) {
      Constructor c = it.next();
      TigerUsage tu = new TigerUsage();
      if (hasAutoBoxingCompatibleSignatures(c.getParameterTypes(), ac, tu)) {
        tu.checkForCompatibleUsage();
        compatible.add(c);
      }
    }
      
    if (!compatible.isEmpty()) {
      return selectTheMostSpecificBoxingConstructor(compatible);
    }
    
    // Do third pass for finding a varargs method that matches given method call
    
    it = all.iterator();
    while (it.hasNext()) {
      Constructor c = it.next();
      TigerUsage tu = new TigerUsage();
      if (hasVarArgsCompatibleSignatures(c.getParameterTypes(), ac, tu)) {
        tu.checkForCompatibleUsage();
        compatible.add(c);
      }
    }
    
    if(compatible.isEmpty()){
      throw new NoSuchMethodException(generateNotFoundMsg("constructor", cl.getName(), ac));
    }
    else if (compatible.size() > 1) {
      // It is ambiguous if more than one variable-argument 
      // method matches the given parameter type list.
      throw new AmbiguousMethodException("both constructors match: " + 
                                         compatible.get(0) + ", and " +
                                         compatible.get(1));
    }
    else {
      return compatible.get(0);
    }
  }
  
  /**
   * Gets all the constructors in the given class or super classes,
   * even the redefined constructors are returned.
   * @param cl     the class where the constructor was declared
   * @param params the number of parameters
   * @return a list that contains the found constructors, an empty list if no
   *         matching constructor was found.
   */
  public static List<Constructor> getConstructors(Class<?> cl, int params) {
    List<Constructor>  result = new LinkedList<Constructor>();
    Constructor[] ms = cl.getDeclaredConstructors();
    
    for (int i = 0; i < ms.length; i++) {
      if (ms[i].getParameterTypes().length <= (params + 1)) {
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
   * @retun the method that should be invoked with arguments of the given types
   */
  public static Method lookupMethod(Class<?> cl, String name, List<Class> ac)
    throws NoSuchMethodException {
    return lookupMethod(cl, name, ac.toArray(new Class[0]));  
  }
  
  /**
   * Looks for a method in the given class or in super classes of this class.
   * @param cl   the class of which the method is a member
   * @param name the name of the method
   * @param ac   the arguments classes (possibly not the exact declaring classes)
   * @return the method that should be invoked with arguments of the given types
   */
  public static Method lookupMethod(Class<?> cl, String name, Class<?>[] ac)
    throws NoSuchMethodException {
    List<Method> all = getMethods(cl, name, ac.length);
    List<Method> compatible = new LinkedList<Method>();
    
    // Search for the methods with good parameter types and
    // put them in 'compatible'
    Iterator<Method> it = all.iterator();
    while (it.hasNext()) {
      Method m = it.next();
      if (hasCompatibleSignatures(m.getParameterTypes(), ac)) {
        compatible.add(m);
      }
    }
    
    // Select the most specific method if any were found
    if (!compatible.isEmpty()) {
      return selectTheMostSpecificMethod(compatible);
    }
    
    // Do a second pass to search for methods with autoboxing
    
    it = all.iterator();
    while (it.hasNext()) {
      Method m = it.next();
      TigerUsage tu = new TigerUsage();
      if (hasAutoBoxingCompatibleSignatures(m.getParameterTypes(), ac, tu)) {
        tu.checkForCompatibleUsage();
        compatible.add(m);
      }
    }
      
    if (!compatible.isEmpty()) {
      return selectTheMostSpecificBoxingMethod(compatible);
    }
    
    // Do third pass for finding a varargs method that matches given method call
    
    it = all.iterator();
    while (it.hasNext()) {
      Method m = it.next();
      TigerUsage tu = new TigerUsage();
      if (hasVarArgsCompatibleSignatures(m.getParameterTypes(), ac, tu)) {
        tu.checkForCompatibleUsage();
        compatible.add(m);
      }
    }
    
    if(compatible.isEmpty()){
      throw new NoSuchMethodException(generateNotFoundMsg("method", cl.getName()+"."+name, ac));
    }
    else if (compatible.size() == 1) {
      return compatible.get(0); 
    }
    else {
      // It is ambiguous if more than one variable-argument 
      // method matches the given parameter type list.
      throw new AmbiguousMethodException(compatible.get(0), compatible.get(1));
    }
  
  } // end method: lookupMethod 
  
  /**
   * Gets all the methods with the given name in the given class or super classes.
   * Even the redefined methods are returned. (Even methods of different parameter
   * lengths are selected due to the introduction of variable arguments in 1.5)
   * @param cl     the class where the method was declared
   * @param name   the name of the method
   * @param params the number of parameters 
   * @return a list that contains the found methods, an empty list if no
   *         matching method was found.
   */
  public static List<Method> getMethods(Class<?> cl, String name, int params) {
    List<Method>  result = new LinkedList<Method>();
    
    if (cl.isInterface()) {
      Method[] ms = cl.getDeclaredMethods();
      for (int i = 0; i < ms.length; i++) {
        if (ms[i].getName().equals(name) &&
            ms[i].getParameterTypes().length <= (params + 1)) {
          result.add(ms[i]);
        }
      }
      Class<?>[] cs = cl.getInterfaces();
      for (int i = 0; i < cs.length; i++) {
        result.addAll(getMethods(cs[i], name, params));
      }
      if (cs.length == 0) {
        result.addAll(getMethods(Object.class, name, params));
      }
    }
    else {
      Class<?> c = cl;
      while (c != null) {
        Method[] ms = c.getDeclaredMethods();
        
        for (int i = 0; i < ms.length; i++) {
          if (ms[i].getName().equals(name) &&
              ms[i].getParameterTypes().length <= (params + 1)) {
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
  public static Method lookupOuterMethod(Class<?> cl, String name, Class<?>[] ac)
    throws NoSuchMethodException {
    boolean sc = Modifier.isStatic(cl.getModifiers());
    Class<?> c = (cl != null) ? cl.getDeclaringClass() : null;
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
    throw new NoSuchMethodException(generateNotFoundMsg("method", name, ac));
  }
  
  /**
   * Returns a field with the given name declared in the given
   * class or in the superclasses of the given class
   * @param cl   the class where the field must look for the field
   * @param name the name of the field
   */
  public static Field getField(Class<?> cl, String name)
    throws NoSuchFieldException, AmbiguousFieldException {
    Class<?> c = cl;
    while (c != null) {
      try {
        return c.getDeclaredField(name);
      } catch(NoSuchFieldException e) {
        Class<?>[] ints = c.getInterfaces();
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
  public static Field getOuterField(Class<?> cl, String name)
    throws NoSuchFieldException, AmbiguousFieldException {
    boolean sc = Modifier.isStatic(cl.getModifiers());
    Class<?>   c  = (cl != null) ? cl.getDeclaringClass() : null;
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
   * Selects the method with the most specific signature.  This assumes that 
   * each method does not require the application of any 1.5+ specific features.
   * @param list The list of methods used 
   * @return the most specific method among the methods in the given list
   */
  protected static Method selectTheMostSpecificMethod(List<Method> list) {
    if (list.isEmpty()) return null;
    
    Iterator<Method> it = list.iterator();
    Method best = it.next();
    Method ambiguous = null; // there is no ambiguous other method at first
    while (it.hasNext()) {
      Method curr = it.next();
      Class<?>[] a1 = best.getParameterTypes();
      Class<?>[] a2 = curr.getParameterTypes();
      
      boolean better1 = false; // whether 'best' is better than 'curr'
      boolean better2 = false; // whether 'curr' is better than 'best'
      for (int i = 0; i < a1.length; i++) {
        boolean from2to1 = isCompatible(a1[i], a2[i]);
        boolean from1to2 = isCompatible(a2[i], a1[i]);
        
        if (from1to2 && !from2to1) {// best's parameter[i] is more specific than curr's
          better1 = true; // so best is better than curr
        }
        if (from2to1 && !from1to2) {// curr's parameter[i] is more specific than best's
          better2 = true; // so curr is better than best
        }
      }
      
      // decide which is more specific or whether they are ambiguous
      if ( !(better1 ^ better2) ) { // neither is better than the other
        // Handle overridden methods
        if (Arrays.equals(a1, a2)) {
          Class<?> c1 = best.getDeclaringClass();
          Class<?> c2 = curr.getDeclaringClass();
          boolean c1IsSuperOrSame = c1.isAssignableFrom(c2);
          boolean c2IsSuperOrSame = c2.isAssignableFrom(c1);
          if (c1IsSuperOrSame && !c2IsSuperOrSame) { // c2 is more specific
            best = curr;
            continue;
          }
          else if (c2IsSuperOrSame && !c1IsSuperOrSame) { // c1 is more specific
            continue;
          }
        }
        ambiguous = curr;
      }
      else if (better2) {
        best = curr;
        ambiguous = null; // no more ambiguity
      }
    }
    if (ambiguous != null) {
      boolean bestBridge = TigerUtilities.isBridge(best);
      boolean ambiBridge = TigerUtilities.isBridge(ambiguous);
      if (bestBridge && !ambiBridge) {
        return ambiguous;
      }
      else if (!bestBridge && ambiBridge) {
        return best;
      }
      throw new AmbiguousMethodException(best, ambiguous);
    }
    return best;
  }
  
  /**
   * Selects the constructor with the most specific signature.  This assumes that 
   * each constructor does not require the use of any 1.5+ specific features.
   * @param list The list of constructor used 
   * @return the most specific constructor among the constructors in the given list
   */
  protected static Constructor selectTheMostSpecificConstructor(List<Constructor> list) {
    if (list.isEmpty()) return null;
    
    Iterator<Constructor> it = list.iterator();
    Constructor best = it.next();
    Constructor ambiguous = null; // there is no ambiguous other method at first
    while (it.hasNext()) {
      Constructor curr = it.next();
      Class<?>[] a1 = best.getParameterTypes();
      Class<?>[] a2 = curr.getParameterTypes();
      
      boolean better1 = false; // whether 'best' is better than 'curr'
      boolean better2 = false; // whether 'curr' is better than 'best'
      for (int i = 0; i < a1.length; i++) {
        boolean from2to1 = isCompatible(a1[i], a2[i]);
        boolean from1to2 = isCompatible(a2[i], a1[i]);
        
        if (from1to2 && !from2to1) {// best's parameter[i] is more specific than curr's
          better1 = true; // so best is better than curr
        }
        if (from2to1 && !from1to2) {// curr's parameter[i] is more specific than best's
          better2 = true; // so curr is better than best
        }
      }
      
      // decide which is more specific or whether they are ambiguous
      if ( !(better1 ^ better2) ) { // neither is better than the other
        // Handle overridden methods
        if (Arrays.equals(a1, a2)) {
          Class<?> c1 = best.getDeclaringClass();
          Class<?> c2 = curr.getDeclaringClass();
          boolean c1IsSuperOrSame = c1.isAssignableFrom(c2);
          boolean c2IsSuperOrSame = c2.isAssignableFrom(c1);
          if (c1IsSuperOrSame && !c2IsSuperOrSame) { // c2 is more specific
            best = curr;
            continue;
          }
          else if (c2IsSuperOrSame && !c1IsSuperOrSame) { // c1 is more specific
            continue;
          }
        }
        ambiguous = curr;
      }
      else if (better2) {
        best = curr;
        ambiguous = null; // no more ambiguity
      }
    }
    if (ambiguous != null) {
      throw new AmbiguousMethodException("Both constructors match: " + best + ", and " + ambiguous);
    }
    return best;
  }
  
  /**
   * Selects the method with the most specific signature including autoboxing.
   * It is assumed that m1 and m2 have the same number of parameters, but
   * also assumes that there will be autoboxing required in at least one of the 
   * parameter types.
   * @param list The list of methods used 
   * @return the most specific method among the methods in the given list
   */
  protected static Method selectTheMostSpecificBoxingMethod(List<Method> list) {
    if (list.isEmpty()) return null;
    TigerUsage tu = new TigerUsage(); // needed for the calls to isBoxCompatible
    
    Iterator<Method> it = list.iterator();
    Method best = it.next();
    Method ambiguous = null; // there is no ambiguous other method at first
    while (it.hasNext()) {
      Method curr = it.next();
      Class<?>[] a1 = best.getParameterTypes();
      Class<?>[] a2 = curr.getParameterTypes();
      
      boolean better1 = false; // whether 'best' is better than 'curr'
      boolean better2 = false; // whether 'curr' is better than 'best'
      for (int i = 0; i < a1.length; i++) {
        boolean from2to1 = isBoxCompatible(a1[i], a2[i], tu);
        boolean from1to2 = isBoxCompatible(a2[i], a1[i], tu);
        
        if (from1to2 && !from2to1) {// best's parameter[i] is more specific than curr's
          better1 = true; // so best is better than curr
        }
        if (from2to1 && !from1to2) {// curr's parameter[i] is more specific than best's
          better2 = true; // so curr is better than best
        }
      }
      
      // decide which is more specific or whether they are ambiguous
      if ( !(better1 ^ better2) ) { // neither is better than the other
        // Handle overridden methods
        if (Arrays.equals(a1, a2)) {
          Class<?> c1 = best.getDeclaringClass();
          Class<?> c2 = curr.getDeclaringClass();
          boolean c1IsSuperOrSame = c1.isAssignableFrom(c2);
          boolean c2IsSuperOrSame = c2.isAssignableFrom(c1);
          if (c1IsSuperOrSame && !c2IsSuperOrSame) { // c2 is more specific
            best = curr;
            continue;
          }
          else if (c2IsSuperOrSame && !c1IsSuperOrSame) { // c1 is more specific
            continue;
          }
        }
        ambiguous = curr;
      }
      else if (better2) {
        best = curr;
        ambiguous = null; // no more ambiguity
      }
    }
    if (ambiguous != null) {
      throw new AmbiguousMethodException(best, ambiguous);
    }
    return best;
  }
   
    
  /**
   * Selects the constructor with the most specific signature including autoboxing.
   * It is assumed that m1 and m2 have the same number of parameters.
   * @param list The list of constructors used 
   * @return the most specific constructor among the constructors in the given list
   */
  protected static Constructor selectTheMostSpecificBoxingConstructor(List<Constructor> list) {
    if (list.isEmpty()) return null;
    TigerUsage tu = new TigerUsage(); // needed for the calls to isBoxCompatible
    
    Iterator<Constructor> it = list.iterator();
    Constructor best = it.next();
    Constructor ambiguous = null; // there is no ambiguous other method at first
    while (it.hasNext()) {
      Constructor curr = it.next();
      Class<?>[] a1 = best.getParameterTypes();
      Class<?>[] a2 = curr.getParameterTypes();
      
      boolean better1 = false; // whether 'best' is better than 'curr'
      boolean better2 = false; // whether 'curr' is better than 'best'
      for (int i = 0; i < a1.length; i++) {
        boolean from2to1 = isBoxCompatible(a1[i], a2[i], tu);
        boolean from1to2 = isBoxCompatible(a2[i], a1[i], tu);
        
        if (from1to2 && !from2to1) {// best's parameter[i] is more specific than curr's
          better1 = true; // so best is better than curr
        }
        if (from2to1 && !from1to2) {// curr's parameter[i] is more specific than best's
          better2 = true; // so curr is better than best
        }
      }
      
      // decide which is more specific or whether they are ambiguous
      if ( !(better1 ^ better2) ) { // neither is better than the other
        // Handle overridden methods
        if (Arrays.equals(a1, a2)) {
          Class<?> c1 = best.getDeclaringClass();
          Class<?> c2 = curr.getDeclaringClass();
          boolean c1IsSuperOrSame = c1.isAssignableFrom(c2);
          boolean c2IsSuperOrSame = c2.isAssignableFrom(c1);
          if (c1IsSuperOrSame && !c2IsSuperOrSame) { // c2 is more specific
            best = curr;
            continue;
          }
          else if (c2IsSuperOrSame && !c1IsSuperOrSame) { // c1 is more specific
            continue;
          }
        }
        ambiguous = curr;
      }
      else if (better2) {
        best = curr;
        ambiguous = null; // no more ambiguity
      }
    }
    if (ambiguous != null) {
      throw new AmbiguousMethodException("Both constructors match: " + best + ", and " + ambiguous);
    }
    return best;
  }
  
  /**
   * For each element (class) of the given arrays, tests if the first array
   * element is assignable from the second array element. The two arrays are
   * assumed to have the same length.
   * @param a1 The parameter types of the method being checked for compatibility
   * @param a2 The types of the arguments that are to be given to the method found
   * @return whether the method with parameter types <code>a1</code> can be
   *   legitimately called usining arguments of the types in <code>a2</code>
   *   without applying any of the new features introduced in java 2 v1.5.0
   */
  public static boolean hasCompatibleSignatures(Class<?>[] a1, Class<?>[] a2) {
    if (a1.length != a2.length) {
      return false;
    }
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
   * assumed to have the same length, but the elements may be the boxing 
   * equivalents of eachother.
   * @param a1 The parameter types of the method being checked for compatibility
   * @param a2 The types of the arguments that are to be given to the method found
   * @return whether the method with parameter types <code>a1</code> can be
   *   legitimately called usining arguments of the types in <code>a2</code> 
   *   using autoboxing but not using variabled arguments
   */
  public static boolean hasAutoBoxingCompatibleSignatures(Class<?>[] a1, Class<?>[] a2, TigerUsage tu) {
    if (a1.length != a2.length) {
      return false;
    }
    
    // Now we know that a1.length > 0;
    for (int i = 0; i < a1.length; i++) { //  a2 can have length larger than or equal to a1
      if (!isBoxCompatible(a1[i], a2[i], tu)) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Tests if the first array element is assignable from the second array element 
   * for each element except the last.  The last element of <code>a1</code> is
   * expected to be an array while the last element(s) of <code>a2</code> may have 
   * more (only all extra parameter types are equal to the element type of the 
   * varargs array in <code>a1</code>
   * @param a1 The parameter types of the method being checked for compatibility
   * @param a2 The types of the arguments that are to be given to the method found
   * @return whether the method with parameter types <code>a1</code> can be
   *   legitimately called usining arguments of the types in <code>a2</code> using
   *   both autoboxing and variable arguments
   */
  public static boolean hasVarArgsCompatibleSignatures(Class<?>[] a1, Class<?>[] a2, TigerUsage tu) {
    if (a1.length == 0) {
      return a2.length == 0;
    }
    
    if (a1.length > (a2.length + 1)) {
      return false;
    }
    
    // Now we know that a1.length > 0;
    for (int i = 0; i < a1.length-1; i++) { //  a2 can have length larger than or equal to a1
      if (!isBoxCompatible(a1[i], a2[i], tu)) {
        return false;
      }
    }
    int lastIdx1 = a1.length - 1;
    Class<?> lastElt1 = a1[lastIdx1];
    if(lastElt1.isArray() && (a2.length == a1.length - 1)) {
      tu.varArgsAreUsed();
      return true; // No varargs given.
    }
    else if(lastElt1.isArray() && !a2[lastIdx1].isArray()){
      tu.varArgsAreUsed();
      Class<?> varArgsType = lastElt1.getComponentType(); // Get the element type of the array
      for( int i = lastIdx1; i < a2.length; i++ ){
        if(!isBoxCompatible(varArgsType, a2[i], tu)){
          return false;
        }
      }
    }
    else { // if equal lengths, then check the last one for compatibility
      if ((a1.length != a2.length) || !isBoxCompatible(lastElt1, a2[lastIdx1], tu)) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Tests whether the Class c1 is assignable from c2.  If c1 and c2 are
   * reference types, this method returns whether an argument of type c2
   * can be passed to a parameter of type c1 without applying any features
   * introduced in java 2 v1.5.0.
   * @param c1 the type of the parameter being tested
   * @param c2 the type of the argument being passed to the parameter
   * @return whether c2 can be passed to c1 in a method invokation without
   *   the features introduced in java 2 v1.5.0
   */
  public static boolean isCompatible(Class<?> c1, Class<?> c2) {
    if (c1.isPrimitive()) {
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
      return (c2 == null) ? true : c1.isAssignableFrom(c2);
    }
  }
  
  /**
   * Tests whether the Class c1 is assignable from c2.  If c1 and c2 are
   * reference types, this method returns whether an argument of type c2
   * can be passed to a parameter of type c1 BY APPLYING the features
   * introduced in java 2 v1.5.0.
   * @param c1 the type of the parameter being tested
   * @param c2 the type of the argument being passed to the parameter
   * @return whether c2 can be passed to c1 in a method invokation using
   *   autoboxing
   */
  public static boolean isBoxCompatible(Class<?> c1, Class<?> c2, TigerUsage tu) {
    if (c1.isPrimitive()) {
      
      if (!c2.isPrimitive()) {
        tu.autoBoxingIsUsed();
        //        throw new RuntimeException("autoboxing used1:" + tu);
      }
      
      // unbox the second type (may not change)
      c2 = TigerUtilities.correspondingPrimType(c2);
      
      
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
        tu.autoBoxingIsUsed();
        c2 = TigerUtilities.correspondingBoxingType(c2);
        //        throw new RuntimeException("autoboxing 2:" + tu);
      }
      return (c2 == null) ? true : c1.isAssignableFrom(c2);
    }
  }
  
  /**
   * Generates a message string stating that one method is not found.  It 
   * puts the given information into the message so as to make the message
   * sufficiently descriptive (as much like javac as possible).
   * @param methodType either "constructor" or "method"
   * @param cl The class in which the method/constructor is being searched
   * @param mName the name of the method being looked for
   * @param ac The types of the expected parameters
   * @return the message that should be given to the NoSuchMethodException
   */
  protected static String generateNotFoundMsg(String methodType, String mName, Class<?>[] ac) {
    String msg = methodType + " " + mName + "(";
    if (ac.length > 0) {
      msg += ac[0].getName();
    }
    for (int i=1; i < ac.length; i++) {
      msg += "," + ac[i].getName();
    }
    msg += ")";
    return msg;
  }
  
  /**
   * An object that can be passed from getMethod and getConstructor methods to
   * certain helper functions.  It keeps track of whether 1.5 features were needed to
   * search for the correct signatures.  The main reasong for this class's existence
   * is that when a search resorts to checking for variable arguments, the user
   * may or may not be using autoboxing.  By putting the error checking handler
   * in this class, we are able to taylor the error message to reflect exactly which
   * features were used in the method/constructor lookup that may not be supported
   * in versions less than 1.5.
   */
  static class TigerUsage {
    private boolean _autoBox = false;
    private boolean _varArgs = false;
    
    public TigerUsage() {
    }
    
    public void autoBoxingIsUsed() { _autoBox = true; }
    
    public boolean isAutoBoxingUsed() { return _autoBox; }
    
    public void varArgsAreUsed() { _varArgs = true; }
    
    public boolean areVarArgsUsed() { return _varArgs; }
    
    public void checkForCompatibleUsage() {
      String msg = "only allowed in Java 2 v1.5 or better";
      if (_autoBox && _varArgs) {
        TigerUtilities.assertTigerEnabled("Auto-boxing and variable arguments are" + msg);
      }
      else if (_varArgs) {
        TigerUtilities.assertTigerEnabled("Variable arguments are" + msg);
      }
      else if (_autoBox) {
        TigerUtilities.assertTigerEnabled("Auto-boxing is" + msg);
      }
    }
    
    public String toString() { return "TigerUsage: {Boxing:" + _autoBox + ", VarArgs:" + _varArgs + "}"; }
  }
  
  /**
   * This class contains only static methods, so it is not useful
   * to create instances of it.
   */
  protected ReflectionUtilities() {
  }
}
