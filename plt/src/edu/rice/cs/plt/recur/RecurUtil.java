/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.recur;

import java.util.Arrays;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.Lambda2;
import edu.rice.cs.plt.lambda.Predicate2;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.collect.TotalMap;
import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.plt.text.TextUtil;

/**
 * TODO: Is the extra overhead required to check for infinite loops enough to justify non-checking
 * alternatives to safeToString, safeEquals, and safeHashCode?
 */
public final class RecurUtil {
  
  private static final TotalMap<Thread, RecursionStack<Object>> TO_STRING_STACKS;
  private static final TotalMap<Thread, RecursionStack2<Object, Object>> EQUALS_STACKS;
  private static final TotalMap<Thread, RecursionStack<Object>> HASH_CODE_STACKS;
  
  private static final Lambda<ArrayStringMode, Lambda<Object, String>> TO_STRING_GENERATOR;
  private static final Lambda<ArrayStringMode, Lambda<Object, String>> DEFAULT_INF_STRING_GENERATOR;
  private static final Lambda2<Object, Object, Boolean> EQUALS;
  private static final Lambda<Object, Integer> HASH_CODE;
  private static final Lambda<Object, Integer> DEFAULT_INF_HASH_CODE;
  
  static {
    Lambda<Thread, RecursionStack<Object>> makeNew = new Lambda<Thread, RecursionStack<Object>>() {
      public RecursionStack<Object> value(Thread t) { return new RecursionStack<Object>(); }
    };
    Lambda<Thread, RecursionStack2<Object, Object>> makeNew2 = 
      new Lambda<Thread, RecursionStack2<Object, Object>>() {
        public RecursionStack2<Object, Object> value(Thread t) { 
          return new RecursionStack2<Object, Object>();
        }
      };
    
    // caching must be enabled (otherwise, a new stack is produced every time)
    TO_STRING_STACKS = new TotalMap<Thread, RecursionStack<Object>>(makeNew, true);
    EQUALS_STACKS = new TotalMap<Thread, RecursionStack2<Object, Object>>(makeNew2, true);
    HASH_CODE_STACKS = new TotalMap<Thread, RecursionStack<Object>>(makeNew, true);
    
    TO_STRING_GENERATOR = LambdaUtil.curry(new Lambda2<ArrayStringMode, Object, String>() {
      public String value(ArrayStringMode mode, Object obj) {
        if (obj.getClass().isArray()) { return arrayToString(obj, mode); }
        else { return obj.toString(); }
      }
    });

    DEFAULT_INF_STRING_GENERATOR = LambdaUtil.curry(new Lambda2<ArrayStringMode, Object, String>() {
      public String value(ArrayStringMode mode, Object obj) { 
        if (obj.getClass().isArray()) { return mode.prefix() + "..." + mode.suffix(); }
        else { return ReflectUtil.simpleName(obj.getClass()) + "..."; }
      }
    });
    
    EQUALS = new Lambda2<Object, Object, Boolean>() {
      public Boolean value(Object obj1, Object obj2) {
        if (obj1.getClass().isArray()) {
          if (obj2.getClass().isArray()) { return arrayEquals(obj1, obj2); }
          else { return false; }
        }
        else {
          if (obj2.getClass().isArray()) { return false; }
          else { return obj1.equals(obj2); }
        }
      }
    };
    
    HASH_CODE = new Lambda<Object, Integer>() {
      public Integer value(Object obj) {
        if (obj.getClass().isArray()) { return arrayHashCode(obj); }
        else { return obj.hashCode(); }
      }
    };
    
    DEFAULT_INF_HASH_CODE = LambdaUtil.valueLambda(0xB32FC891);
  }
  
  /** Prevents instance creation */
  private RecurUtil() {}
  
  /** 
   * Invokes {@link #safeToString(Object, Lambda, int, ArrayStringMode)} using a default 
   * {@code infiniteString} like {@code "ClassName..."} or <code>"{ ... }"</code>, a default 
   * {@code depth} of {@code 1}, and {@link ArrayStringMode#DEEP_BRACED} as a default array 
   * string mode.
   */
  public static String safeToString(Object obj) {
    return safeToString(obj, DEFAULT_INF_STRING_GENERATOR.value(ArrayStringMode.DEEP_BRACED), 
                        1, ArrayStringMode.DEEP_BRACED);
  }

  /** 
   * Invokes {@link #safeToString(Object, Lambda, int, ArrayStringMode)} using the given string as
   * the {@code infiniteString}, a default {@code depth} of {@code 1}, and 
   * {@link ArrayStringMode#DEEP_BRACED} as a default array string mode.
   */
  public static String safeToString(Object obj, String infiniteString) {
    return safeToString(obj, infiniteString, 1, ArrayStringMode.DEEP_BRACED);
  }

  /** 
   * Invokes {@link #safeToString(Object, Lambda, int, ArrayStringMode)} with a default
   * {@code depth} of {@code 1}, and using {@link ArrayStringMode#DEEP_BRACED} as a default array 
   * string mode.
   */
  public static <T> String safeToString(T obj, Lambda<? super T, String> infiniteString) {
    return safeToString(obj, infiniteString, 1, ArrayStringMode.DEEP_BRACED);
  }

  /** 
   * Invokes {@link #safeToString(Object, Lambda, int, ArrayStringMode)} using a default 
   * {@code infiniteString} like {@code "ClassName..."} or <code>"{ ... }"</code>, and a default 
   * {@code depth} of {@code 1}.
   */
  public static String safeToString(Object obj, ArrayStringMode arrayMode) {
    return safeToString(obj, DEFAULT_INF_STRING_GENERATOR.value(arrayMode), 1, arrayMode);
  }
  
  /** 
   * Invokes {@link #safeToString(Object, Lambda, int, ArrayStringMode)} using the given
   * string as the {@code infiniteString}, and a default {@code depth} of {@code 1}.
   */
  public static String safeToString(Object obj, String infiniteString, ArrayStringMode arrayMode) {
    return safeToString(obj, LambdaUtil.valueLambda(infiniteString), 1, arrayMode);
  }
  
  /**
   * Invokes {@link #safeToString(Object, Lambda, int, ArrayStringMode)} using a default 
   * {@code depth} of {@code 1}.
   */
  public static <T> String safeToString(T obj, Lambda<? super T, String> infiniteString,
                                        ArrayStringMode arrayMode) {
    return safeToString(obj, infiniteString, 1, arrayMode);
  }
  
  /** 
   * Invokes {@link #safeToString(Object, Lambda, int, ArrayStringMode)} using a default 
   * {@code infiniteString} like {@code "ClassName..."} or <code>"{ ... }"</code>, and 
   * {@link ArrayStringMode#DEEP_BRACED} as a default array string mode.
   */
  public static String safeToString(Object obj, int depth) {
    return safeToString(obj, DEFAULT_INF_STRING_GENERATOR.value(ArrayStringMode.DEEP_BRACED), 
                        depth, ArrayStringMode.DEEP_BRACED);
  }

  /** 
   * Invokes {@link #safeToString(Object, Lambda, int, ArrayStringMode)} using the given string as
   * the {@code infiniteString}, and {@link ArrayStringMode#DEEP_BRACED} as a default array string 
   * mode.
   */
  public static String safeToString(Object obj, String infiniteString, int depth) {
    return safeToString(obj, infiniteString, depth, ArrayStringMode.DEEP_BRACED);
  }

  /** 
   * Invokes {@link #safeToString(Object, Lambda, int, ArrayStringMode)} using 
   * {@link ArrayStringMode#DEEP_BRACED} as a default array string mode.
   */
  public static <T> String safeToString(T obj, Lambda<? super T, String> infiniteString, int depth) {
    return safeToString(obj, infiniteString, depth, ArrayStringMode.DEEP_BRACED);
  }

  /** 
   * Invokes {@link #safeToString(Object, Lambda, int, ArrayStringMode)} using a default 
   * {@code infiniteString} like {@code "ClassName..."} or <code>"{ ... }"</code>.
   */
  public static String safeToString(Object obj, int depth, ArrayStringMode arrayMode) {
    return safeToString(obj, DEFAULT_INF_STRING_GENERATOR.value(arrayMode), depth, arrayMode);
  }
  
  /** 
   * Invokes {@link #safeToString(Object, Lambda, int, ArrayStringMode)} using the given
   * string as the {@code infiniteString}.
   */
  public static String safeToString(Object obj, String infiniteString, int depth, 
                                    ArrayStringMode arrayMode) {
    return safeToString(obj, LambdaUtil.valueLambda(infiniteString), depth, arrayMode);
  }
  
  /**
   * <p>Evaluate {@code obj.toString()} under the protection of an infinite-recursion check.
   * If a string for {@code obj} is already in the process of being computed by this method
   * {@code depth} times (that is, this is the {@code depth+1}st nested invocation), 
   * {@code infiniteString} will be applied.  Otherwise, the result is {@code obj.toString()}.</p>
   * 
   * <p>To simplify client code, this method also handles {@code null} values (returning 
   * {@code "null"}) and arrays (calling {@link #arrayToString(Object, ArrayStringMode)}).</p>
   * 
   * @param obj  An object (may be {@code null} or an array)
   * @param infiniteString  A lambda to generate a string for {@code obj} if its {@code toString}
   *                        method has already been invoked {@code depth} times
   * @param depth  The number of times to allow a {@code toString} invocation before using
   *               {@code infiniteString} instead
   * @param arrayMode  The {@link  ArrayStringMode} to use if {@code obj} is an array
   * @return  A string representation of {@code obj}
   */
  public static <T> String safeToString(T obj, Lambda<? super T, String> infiniteString, int depth,
                                        ArrayStringMode arrayMode) {
    if (obj == null) { return "null"; }
    else {
      RecursionStack<Object> stack;
      Thread t = Thread.currentThread();
      synchronized (TO_STRING_STACKS) { stack = TO_STRING_STACKS.get(t); }
      // no synchronization on stack, because it is only used in this thread
      String result = stack.<T, String>apply(TO_STRING_GENERATOR.value(arrayMode), infiniteString, obj);
      if (stack.isEmpty()) {
        synchronized (TO_STRING_STACKS) { TO_STRING_STACKS.revert(t); }
      }
      return result;
    }
  }
  
  /** 
   * Invoke {@link #arrayToString(Object, ArrayStringMode)} with a default string mode
   * {@link ArrayStringMode#DEEP_BRACED}
   */
  public static String arrayToString(Object array) {
    return arrayToString(array, ArrayStringMode.DEEP_BRACED);
  }
  
  /**
   * Generate a string representation of the given array.  If the {@code stringMode} is
   * {@link ArrayStringMode#CLASS_NAME} or {@link ArrayStringMode#TYPE_AND_SIZE}, generate
   * a simple name, as specified; otherwise, generate a list of each of the elements (calling
   * {@link #safeToString(Object, ArrayStringMode)} where applicable, using the mode 
   * produced by {@link ArrayStringMode#nestedMode()}).
   *
   * @throws IllegalArgumentException  If {@code array} is not an array
   */
  public static String arrayToString(Object array, ArrayStringMode stringMode) {
    if (array instanceof Object[]) { return arrayToString((Object[]) array, stringMode); }
    else if (array instanceof int[]) { return arrayToString((int[]) array, stringMode); }
    else if (array instanceof char[]) { return arrayToString((char[]) array, stringMode); }
    else if (array instanceof byte[]) { return arrayToString((byte[]) array, stringMode); }
    else if (array instanceof double[]) { return arrayToString((double[]) array, stringMode); }
    else if (array instanceof boolean[]) { return arrayToString((boolean[]) array, stringMode); }
    else if (array instanceof short[]) { return arrayToString((short[]) array, stringMode); }
    else if (array instanceof long[]) { return arrayToString((long[]) array, stringMode); }
    else if (array instanceof float[]) { return arrayToString((float[]) array, stringMode); }
    else { throw new IllegalArgumentException("Non-array argument"); }
  }
  
  /** 
   * Invoke {@link #arrayToString(Object[], ArrayStringMode)} with a default string mode
   * {@link ArrayStringMode#DEEP_BRACED}
   */
  public static String arrayToString(Object[] array) {
    return arrayToString(array, ArrayStringMode.DEEP_BRACED);
  }
  
  /**
   * Generate a string representation of the given array.  If the {@code stringMode} is
   * {@link ArrayStringMode#CLASS_NAME} or {@link ArrayStringMode#TYPE_AND_SIZE}, generate
   * a simple name, as specified; otherwise call {@link #safeToString(Object, ArrayStringMode)} 
   * on each of the elements, using the mode produced by {@link ArrayStringMode#nestedMode()}.  
   * If {@code stringMode} is {@link ArrayStringMode#SHALLOW_BRACKETED}, the result will match 
   * that of {@link Arrays#toString(Object[])}; if {@code stringMode} is
   * {@link ArrayStringMode#DEEP_BRACKETED}, the result will match that of 
   * {@link Arrays#deepToString}.
   */
  public static String arrayToString(Object[] array, ArrayStringMode stringMode) {
    switch (stringMode) {
      case CLASS_NAME:
        return array.toString();
        
      case TYPE_AND_SIZE:
        return ReflectUtil.simpleName(array.getClass().getComponentType()) + "[" + array.length + "]";

      default:
        StringBuilder result = new StringBuilder();
        result.append(stringMode.prefix());
        boolean first = true;
        for (Object elt : array) {
          if (first) { first = false; }
          else { result.append(stringMode.delimiter()); }
          result.append(safeToString(elt, stringMode.nestedMode()));
        }
        result.append(stringMode.suffix());
        return result.toString();
    }
  }
  
  /** 
   * Invoke {@link #arrayToString(boolean[], ArrayStringMode)} with a default string mode
   * {@link ArrayStringMode#DEEP_BRACED}
   */
  public static String arrayToString(boolean[] array) {
    return arrayToString(array, ArrayStringMode.DEEP_BRACED);
  }
  
  /**
   * Generate a string representation of the given array.  If the {@code stringMode} is
   * {@link ArrayStringMode#CLASS_NAME} or {@link ArrayStringMode#TYPE_AND_SIZE}, generate
   * a simple name, as specified; otherwise, generate a list containing each element.
   */
  public static String arrayToString(boolean[] array, ArrayStringMode stringMode) {
    switch (stringMode) {
      case CLASS_NAME:
        return array.toString();
        
      case TYPE_AND_SIZE:
        return "boolean[" + array.length + "]";

      default:
        StringBuilder result = new StringBuilder();
        result.append(stringMode.prefix());
        boolean first = true;
        for (boolean elt : array) {
          if (first) { first = false; }
          else { result.append(stringMode.delimiter()); }
          result.append(elt);
        }
        result.append(stringMode.suffix());
        return result.toString();
    }
  }
  
  /** 
   * Invoke {@link #arrayToString(char[], ArrayStringMode)} with a default string mode
   * {@link ArrayStringMode#DEEP_BRACED}
   */
  public static String arrayToString(char[] array) {
    return arrayToString(array, ArrayStringMode.DEEP_BRACED);
  }
  
  /**
   * Generate a string representation of the given array.  If the {@code stringMode} is
   * {@link ArrayStringMode#CLASS_NAME} or {@link ArrayStringMode#TYPE_AND_SIZE}, generate
   * a simple name, as specified; otherwise, generate a list containing each element.
   */
  public static String arrayToString(char[] array, ArrayStringMode stringMode) {
    switch (stringMode) {
      case CLASS_NAME:
        return array.toString();
        
      case TYPE_AND_SIZE:
        return "char[" + array.length + "]";

      default:
        StringBuilder result = new StringBuilder();
        result.append(stringMode.prefix());
        boolean first = true;
        for (char elt : array) {
          if (first) { first = false; }
          else { result.append(stringMode.delimiter()); }
          result.append(elt);
        }
        result.append(stringMode.suffix());
        return result.toString();
    }
  }
  
  /** 
   * Invoke {@link #arrayToString(byte[], ArrayStringMode)} with a default string mode
   * {@link ArrayStringMode#DEEP_BRACED}
   */
  public static String arrayToString(byte[] array) {
    return arrayToString(array, ArrayStringMode.DEEP_BRACED);
  }
  
  /**
   * Generate a string representation of the given array.  If the {@code stringMode} is
   * {@link ArrayStringMode#CLASS_NAME} or {@link ArrayStringMode#TYPE_AND_SIZE}, generate
   * a simple name, as specified; otherwise, generate a list containing each element.
   */
  public static String arrayToString(byte[] array, ArrayStringMode stringMode) {
    switch (stringMode) {
      case CLASS_NAME:
        return array.toString();
        
      case TYPE_AND_SIZE:
        return "byte[" + array.length + "]";

      default:
        StringBuilder result = new StringBuilder();
        result.append(stringMode.prefix());
        boolean first = true;
        for (byte elt : array) {
          if (first) { first = false; }
          else { result.append(stringMode.delimiter()); }
          result.append(elt);
        }
        result.append(stringMode.suffix());
        return result.toString();
    }
  }
  
  /** 
   * Invoke {@link #arrayToString(short[], ArrayStringMode)} with a default string mode
   * {@link ArrayStringMode#DEEP_BRACED}
   */
  public static String arrayToString(short[] array) {
    return arrayToString(array, ArrayStringMode.DEEP_BRACED);
  }
  
  /**
   * Generate a string representation of the given array.  If the {@code stringMode} is
   * {@link ArrayStringMode#CLASS_NAME} or {@link ArrayStringMode#TYPE_AND_SIZE}, generate
   * a simple name, as specified; otherwise, generate a list containing each element.
   */
  public static String arrayToString(short[] array, ArrayStringMode stringMode) {
    switch (stringMode) {
      case CLASS_NAME:
        return array.toString();
        
      case TYPE_AND_SIZE:
        return "short[" + array.length + "]";

      default:
        StringBuilder result = new StringBuilder();
        result.append(stringMode.prefix());
        boolean first = true;
        for (short elt : array) {
          if (first) { first = false; }
          else { result.append(stringMode.delimiter()); }
          result.append(elt);
        }
        result.append(stringMode.suffix());
        return result.toString();
    }
  }
  
  /** 
   * Invoke {@link #arrayToString(int[], ArrayStringMode)} with a default string mode
   * {@link ArrayStringMode#DEEP_BRACED}
   */
  public static String arrayToString(int[] array) {
    return arrayToString(array, ArrayStringMode.DEEP_BRACED);
  }
  
  /**
   * Generate a string representation of the given array.  If the {@code stringMode} is
   * {@link ArrayStringMode#CLASS_NAME} or {@link ArrayStringMode#TYPE_AND_SIZE}, generate
   * a simple name, as specified; otherwise, generate a list containing each element.
   */
  public static String arrayToString(int[] array, ArrayStringMode stringMode) {
    switch (stringMode) {
      case CLASS_NAME:
        return array.toString();
        
      case TYPE_AND_SIZE:
        return "int[" + array.length + "]";

      default:
        StringBuilder result = new StringBuilder();
        result.append(stringMode.prefix());
        boolean first = true;
        for (int elt : array) {
          if (first) { first = false; }
          else { result.append(stringMode.delimiter()); }
          result.append(elt);
        }
        result.append(stringMode.suffix());
        return result.toString();
    }
  }
  
  /** 
   * Invoke {@link #arrayToString(long[], ArrayStringMode)} with a default string mode
   * {@link ArrayStringMode#DEEP_BRACED}
   */
  public static String arrayToString(long[] array) {
    return arrayToString(array, ArrayStringMode.DEEP_BRACED);
  }
  
  /**
   * Generate a string representation of the given array.  If the {@code stringMode} is
   * {@link ArrayStringMode#CLASS_NAME} or {@link ArrayStringMode#TYPE_AND_SIZE}, generate
   * a simple name, as specified; otherwise, generate a list containing each element.
   */
  public static String arrayToString(long[] array, ArrayStringMode stringMode) {
    switch (stringMode) {
      case CLASS_NAME:
        return array.toString();
        
      case TYPE_AND_SIZE:
        return "long[" + array.length + "]";

      default:
        StringBuilder result = new StringBuilder();
        result.append(stringMode.prefix());
        boolean first = true;
        for (long elt : array) {
          if (first) { first = false; }
          else { result.append(stringMode.delimiter()); }
          result.append(elt);
        }
        result.append(stringMode.suffix());
        return result.toString();
    }
  }
  
  /** 
   * Invoke {@link #arrayToString(float[], ArrayStringMode)} with a default string mode
   * {@link ArrayStringMode#DEEP_BRACED}
   */
  public static String arrayToString(float[] array) {
    return arrayToString(array, ArrayStringMode.DEEP_BRACED);
  }
  
  /**
   * Generate a string representation of the given array.  If the {@code stringMode} is
   * {@link ArrayStringMode#CLASS_NAME} or {@link ArrayStringMode#TYPE_AND_SIZE}, generate
   * a simple name, as specified; otherwise, generate a list containing each element.
   */
  public static String arrayToString(float[] array, ArrayStringMode stringMode) {
    switch (stringMode) {
      case CLASS_NAME:
        return array.toString();
        
      case TYPE_AND_SIZE:
        return "float[" + array.length + "]";

      default:
        StringBuilder result = new StringBuilder();
        result.append(stringMode.prefix());
        boolean first = true;
        for (float elt : array) {
          if (first) { first = false; }
          else { result.append(stringMode.delimiter()); }
          result.append(elt);
        }
        result.append(stringMode.suffix());
        return result.toString();
    }
  }
  
  /** 
   * Invoke {@link #arrayToString(double[], ArrayStringMode)} with a default string mode
   * {@link ArrayStringMode#DEEP_BRACED}
   */
  public static String arrayToString(double[] array) {
    return arrayToString(array, ArrayStringMode.DEEP_BRACED);
  }
  
  /**
   * Generate a string representation of the given array.  If the {@code stringMode} is
   * {@link ArrayStringMode#CLASS_NAME} or {@link ArrayStringMode#TYPE_AND_SIZE}, generate
   * a simple name, as specified; otherwise, generate a list containing each element.
   */
  public static String arrayToString(double[] array, ArrayStringMode stringMode) {
    switch (stringMode) {
      case CLASS_NAME:
        return array.toString();
        
      case TYPE_AND_SIZE:
        return "double[" + array.length + "]";

      default:
        StringBuilder result = new StringBuilder();
        result.append(stringMode.prefix());
        boolean first = true;
        for (double elt : array) {
          if (first) { first = false; }
          else { result.append(stringMode.delimiter()); }
          result.append(elt);
        }
        result.append(stringMode.suffix());
        return result.toString();
    }
  }
  
  /** 
   * Invokes {@link #safeEquals(Object, Object, Predicate2)} using {@code true} for 
   * {@code infiniteEquals} (note that {@code false} is not a reasonable value for 
   * {@code infiniteEquals}, since an infinite structure must be equal to itself)
   * 
   */
  public static boolean safeEquals(Object obj1, Object obj2) {
    return safeEquals(obj1, obj2, LambdaUtil.TRUE);
  }
  
  /**
   * <p>Evaluate {@code obj1.equals(obj2)} under the protection of an infinite-recursion check.
   * If the equality of {@code obj1} and {@code obj2} is already in the process of being computed 
   * by this method, {@code infiniteEquals} will be applied.  Otherwise, the result is 
   * {@code obj1.equals(obj2)}.</p>
   * 
   * <p>To simplify client code, this method also handles {@code null} values (equal iff both
   * values are {@code null}) and arrays (calling {@link #arrayEquals(Object, Object)}.</p>
   * 
   * @param obj1  An object (may be {@code null} or an array)
   * @param obj2  An object (may be {@code null} or an array)
   * @param infiniteEquals  A predicate to determine equality of {@code obj1.equals(obj2)} has
   *                        already been invoked
   * @return  {@code true} iff the two objects are equal
   */
  public static <T1, T2> boolean safeEquals(T1 obj1, T2 obj2, 
                                            Predicate2<? super T1, ? super T2> infiniteEquals) {
    if (obj1 == null) { return obj2 == null; }
    else if (obj2 == null) { return false; }
    else {
      RecursionStack2<Object, Object> stack;
      Thread t = Thread.currentThread();
      synchronized (EQUALS_STACKS) { stack = EQUALS_STACKS.get(t); }
      // no synchronization on stack, because it is only used in this thread
      boolean result = stack.<T1, T2, Boolean>apply(EQUALS, LambdaUtil.asLambda(infiniteEquals), obj1, obj2);
      if (stack.isEmpty()) {
        synchronized (EQUALS_STACKS) { EQUALS_STACKS.revert(t); }
      }
      return result;
    }
  }
  
  /**
   * Test the equality of the given arrays.  The result is calculated by comparing the lengths,
   * types (in the primitive cases), and corresponding elements of the arguments, recurring on 
   * any nested objects (including arrays) using {@link #safeEquals(Object, Object)}.  Unlike 
   * {@link Arrays#deepEquals}, this method is able to handle an array nested within 
   * itself.
   * 
   * @throws IllegalArgumentException  If {@code array1} or {@code array2} is not an array
   */
  public static boolean arrayEquals(Object a1, Object a2) {
    if (!a1.getClass().isArray() || !a2.getClass().isArray()) { 
      throw new IllegalArgumentException("Non-array argument");
    }
    if (a1 instanceof Object[] && a2 instanceof Object[]) { 
      return arrayEquals((Object[]) a1, (Object[]) a2);
    }
    else if (!a1.getClass().equals(a2.getClass())) { return false; }
    else if (a1 instanceof int[]) { return Arrays.equals((int[]) a1, (int[]) a2); }
    else if (a1 instanceof char[]) { return Arrays.equals((char[]) a1, (char[]) a2); }
    else if (a1 instanceof byte[]) { return Arrays.equals((byte[]) a1, (byte[]) a2); }
    else if (a1 instanceof double[]) { return Arrays.equals((double[]) a1, (double[]) a2); }
    else if (a1 instanceof boolean[]) { return Arrays.equals((boolean[]) a1, (boolean[]) a2); }
    else if (a1 instanceof short[]) { return Arrays.equals((short[]) a1, (short[]) a2); }
    else if (a1 instanceof long[]) { return Arrays.equals((long[]) a1, (long[]) a2); }
    else if (a1 instanceof float[]) { return Arrays.equals((float[]) a1, (float[]) a2); }
    else { throw new IllegalArgumentException("Unrecognized array type"); }
  }
  
  /**
   * Test the equality of the given arrays.  The result is calculated by comparing the lengths
   * and corresponding elements of the arguments, recurring on nested objects (including arrays) 
   * using {@link #safeEquals(Object, Object)}.  Note that arrays of different runtime types
   * may still be equal if their elements are equal.  Unlike {@link Arrays#deepEquals}, this 
   * method is able to handle an array nested within itself.
   */
  public static boolean arrayEquals(Object[] a1, Object[] a2) {
    if (a1.length != a2.length) { return false; }
    for (int i = 0; i < a1.length; i++) { if (!safeEquals(a1[i], a2[i])) { return false; } }
    return true;
  }
  
  
  /** 
   * Invokes {@link #safeHashCode(Object, Lambda)} using an arbitrary default value as the
   * {@code infiniteHashCode}.
   */
  public static int safeHashCode(Object obj) {
    return safeHashCode(obj, DEFAULT_INF_HASH_CODE);
  }
  
  /** 
   * Invokes {@link #safeHashCode(Object, Lambda)} using the given value as the 
   * {@code infiniteHashCode}.
   */
  public static int safeHashCode(Object obj, int infiniteHashCode) {
    return safeHashCode(obj, LambdaUtil.valueLambda(infiniteHashCode));
  }
  
  /**
   * <p>Evaluate {@code obj.hashCode()} under the protection of an infinite-recursion check.
   * If the hash code for {@code obj} is already in the process of being computed by this method, 
   * {@code infiniteHashCode} will be applied.  Otherwise, the result is {@code obj.hashCode()}.</p>
   * 
   * <p>To simplify client code, this method also handles {@code null} values (returning {@code 0})
   * and arrays (calling {@link #arrayHashCode(Object)}).</p>
   * 
   * @param obj  An object (may be {@code null} or an array)
   * @param infiniteHashCode  A lambda to generate a hash code for {@code obj} if its 
   *                          {@code hashCode} method has already been invoked
   * @return  A hash code for of {@code obj}
   */
  public static <T> int safeHashCode(T obj, Lambda<? super T, Integer> infiniteHashCode) {
    if (obj == null) { return 0; }
    else {
      RecursionStack<Object> stack;
      Thread t = Thread.currentThread();
      synchronized (HASH_CODE_STACKS) { stack = HASH_CODE_STACKS.get(t); }
      // no synchronization on stack, because it is only used in this thread
      int result = stack.<T, Integer>apply(HASH_CODE, infiniteHashCode, obj);
      if (stack.isEmpty()) {
        synchronized (HASH_CODE_STACKS) { HASH_CODE_STACKS.revert(t); }
      }
      return result;
    }
  }
  
  /**
   * Generate a hash code for the given array.  The result is calculated as specified by
   * {@link java.util.List#hashCode}, recurring on any nested objects (including arrays)
   * using {@link #safeHashCode(Object)}.  Unlike {@link Arrays#deepHashCode},
   * this method is able to handle an array nested within itself.
   * 
   * @throws IllegalArgumentException  If {@code array} is not an array
   */
  public static int arrayHashCode(Object array) {
    if (array instanceof Object[]) { return arrayHashCode((Object[]) array); }
    else if (array instanceof int[]) { return arrayHashCode((int[]) array); }
    else if (array instanceof char[]) { return arrayHashCode((char[]) array); }
    else if (array instanceof byte[]) { return arrayHashCode((byte[]) array); }
    else if (array instanceof double[]) { return arrayHashCode((double[]) array); }
    else if (array instanceof boolean[]) { return arrayHashCode((boolean[]) array); }
    else if (array instanceof short[]) { return arrayHashCode((short[]) array); }
    else if (array instanceof long[]) { return arrayHashCode((long[]) array); }
    else if (array instanceof float[]) { return arrayHashCode((float[]) array); }
    else { throw new IllegalArgumentException("Non-array argument"); }
  }
  
  /**
   * Generate a hash code for the given array.  The result is calculated as specified by
   * {@link java.util.List#hashCode}, recurring on each element using {@link #safeHashCode(Object)}.
   * Unlike {@link Arrays#deepHashCode}, this method is able to handle an array nested 
   * within itself.
   */
  public static int arrayHashCode(Object[] array) {
    int result = 1;
    for (Object elt : array) { result = result*31 + safeHashCode(elt); }
    return result;
  }
  
  /**
   * Generate a hash code for the given array.  The result is calculated as specified by
   * {@link java.util.List#hashCode}, invoking the {@code Boolean#hashCode} method on
   * each element.  (Note that {@link Arrays#hashCode(boolean[])} implements the
   * same method; this is defined here for compatibility with earlier APIs.)
   */
  public static int arrayHashCode(boolean[] array) {
    int result = 1;
    for (boolean elt : array) { result = result*31 + ((Boolean) elt).hashCode(); }
    return result;
  }

  /**
   * Generate a hash code for the given array.  The result is calculated as specified by
   * {@link java.util.List#hashCode}, invoking the {@code Character#hashCode} method on
   * each element.  (Note that {@link Arrays#hashCode(char[])} implements the
   * same method; this is defined here for compatibility with earlier APIs.)
   */
  public static int arrayHashCode(char[] array) {
    int result = 1;
    for (char elt : array) { result = result*31 + ((Character) elt).hashCode(); }
    return result;
  }

  /**
   * Generate a hash code for the given array.  The result is calculated as specified by
   * {@link java.util.List#hashCode}, invoking the {@code Byte#hashCode} method on
   * each element.  (Note that {@link Arrays#hashCode(byte[])} implements the
   * same method; this is defined here for compatibility with earlier APIs.)
   */
  public static int arrayHashCode(byte[] array) {
    int result = 1;
    for (byte elt : array) { result = result*31 + ((Byte) elt).hashCode(); }
    return result;
  }

  /**
   * Generate a hash code for the given array.  The result is calculated as specified by
   * {@link java.util.List#hashCode}, invoking the {@code Short#hashCode} method on
   * each element.  (Note that {@link Arrays#hashCode(short[])} implements the
   * same method; this is defined here for compatibility with earlier APIs.)
   */
  public static int arrayHashCode(short[] array) {
    int result = 1;
    for (short elt : array) { result = result*31 + ((Short) elt).hashCode(); }
    return result;
  }

  /**
   * Generate a hash code for the given array.  The result is calculated as specified by
   * {@link java.util.List#hashCode}, invoking the {@code Integer#hashCode} method on
   * each element.  (Note that {@link Arrays#hashCode(int[])} implements the
   * same method; this is defined here for compatibility with earlier APIs.)
   */
  public static int arrayHashCode(int[] array) {
    int result = 1;
    for (int elt : array) { result = result*31 + ((Integer) elt).hashCode(); }
    return result;
  }

  /**
   * Generate a hash code for the given array.  The result is calculated as specified by
   * {@link java.util.List#hashCode}, invoking the {@code Long#hashCode} method on
   * each element.  (Note that {@link Arrays#hashCode(long[])} implements the
   * same method; this is defined here for compatibility with earlier APIs.)
   */
  public static int arrayHashCode(long[] array) {
    int result = 1;
    for (long elt : array) { result = result*31 + ((Long) elt).hashCode(); }
    return result;
  }

  /**
   * Generate a hash code for the given array.  The result is calculated as specified by
   * {@link java.util.List#hashCode}, invoking the {@code Float#hashCode} method on
   * each element.  (Note that {@link Arrays#hashCode(float[])} implements the
   * same method; this is defined here for compatibility with earlier APIs.)
   */
  public static int arrayHashCode(float[] array) {
    int result = 1;
    for (float elt : array) { result = result*31 + ((Float) elt).hashCode(); }
    return result;
  }

  /**
   * Generate a hash code for the given array.  The result is calculated as specified by
   * {@link java.util.List#hashCode}, invoking the {@code Double#hashCode} method on
   * each element.  (Note that {@link Arrays#hashCode(double[])} implements the
   * same method; this is defined here for compatibility with earlier APIs.)
   */
  public static int arrayHashCode(double[] array) {
    int result = 1;
    for (double elt : array) { result = result*31 + ((Double) elt).hashCode(); }
    return result;
  }


  /** Defines the representation to be used in array string-generating methods */
  public static enum ArrayStringMode {
    /** Arrays are printed according to the array {@code toString} method */
    CLASS_NAME { 
      protected String prefix() { throw new IllegalArgumentException(); }
      protected String delimiter() { throw new IllegalArgumentException(); }
      protected String suffix() { throw new IllegalArgumentException(); }
      protected ArrayStringMode nestedMode() { throw new IllegalArgumentException(); }
    }, 
    /** Arrays are printed with an element type name and a size, like {@code "String[10]"} */
    TYPE_AND_SIZE {
      protected String prefix() { throw new IllegalArgumentException(); }
      protected String delimiter() { throw new IllegalArgumentException(); }
      protected String suffix() { throw new IllegalArgumentException(); }
      protected ArrayStringMode nestedMode() { throw new IllegalArgumentException(); }
    },
    /**
     * Arrays are printed as in {@code Arrays#toString(Object[])}, like 
     * {@code "[1, 2, 3]"}; nested arrays use {@link #CLASS_NAME}
     */
    SHALLOW_BRACKETED { 
      protected String prefix() { return "["; }
      protected String delimiter() { return ", "; }
      protected String suffix() { return "]"; }
      protected ArrayStringMode nestedMode() { return CLASS_NAME; }
    }, 
    /**
     * Arrays are printed as in {@code Arrays#deepToString}, like {@code "[1, 2, 3]"};
     * nested arrays also use {@code #DEEP_BRACKETED}
     */
    DEEP_BRACKETED { 
      protected String prefix() { return "["; }
      protected String delimiter() { return ", "; }
      protected String suffix() { return "]"; }
      protected ArrayStringMode nestedMode() { return DEEP_BRACKETED; }
    },
    /**
     * Arrays are printed like array initializers, using braces (<code>"{ 1, 2, 3 }"</code>); 
     * nested arrays use {@link #TYPE_AND_SIZE}
     */
    SHALLOW_BRACED { 
      protected String prefix() { return "{ "; }
      protected String delimiter() { return ", "; }
      protected String suffix() { return " }"; }
      protected ArrayStringMode nestedMode() { return TYPE_AND_SIZE; }
    }, 
    /**
     * Arrays are printed like array initializers, using braces (<code>"{ 1, 2, 3 }"</code>); 
     * nested arrays also use {@link #DEEP_BRACED}
     */
    DEEP_BRACED {
      protected String prefix() { return "{ "; }
      protected String delimiter() { return ", "; }
      protected String suffix() { return " }"; }
      protected ArrayStringMode nestedMode() { return DEEP_BRACED; }
    },
    /** Arrays are printed with a single entry on each line; nested arrays use {@link #DEEP_BRACED} */
    DEEP_MULTILINE {
      protected String prefix() { return ""; }
      protected String delimiter() { return TextUtil.NEWLINE; }
      protected String suffix() { return ""; }
      protected ArrayStringMode nestedMode() { return DEEP_BRACED; }
    },
    /** Arrays are printed with a single entry on each line; nested arrays use {@link #SHALLOW_BRACED} */
    SHALLOW_MULTILINE {
      protected String prefix() { return ""; }
      protected String delimiter() { return TextUtil.NEWLINE; }
      protected String suffix() { return ""; }
      protected ArrayStringMode nestedMode() { return SHALLOW_BRACED; }
    }; 
    
    protected abstract String prefix();
    protected abstract String delimiter();
    protected abstract String suffix();
    protected abstract ArrayStringMode nestedMode();
  }

}
