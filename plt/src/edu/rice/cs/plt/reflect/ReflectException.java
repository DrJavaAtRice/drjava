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

package edu.rice.cs.plt.reflect;

import java.lang.reflect.InvocationTargetException;

/**
 * <p>An exception wrapper to simplify interactions with reflection libraries.  Most documented
 * exceptions (but not errors) thrown by reflection methods may be wrapped by a subclass of
 * {@code ReflectException}.  The wrapped exception may be caught and handled directly,
 * or unwrapped with a {@link ReflectExceptionVisitor}.</p>
 * 
 * <p>Like most of the exceptions it wraps, {@code ReflectException} is a checked exception.</p>
 */
public abstract class ReflectException extends Exception {
  
  protected ReflectException(Throwable cause) { super(cause); }
  public abstract <T> T apply(ReflectExceptionVisitor<T> v);
  
  /** Wraps a {@link ClassNotFoundException} */
  public static class ClassNotFoundReflectException extends ReflectException {
    public ClassNotFoundReflectException(ClassNotFoundException e) { super(e); }
    public <T> T apply(ReflectExceptionVisitor<T> v) {
      return v.forClassNotFound((ClassNotFoundException) getCause());
    }
  }
  
  /** Wraps a {@link NoSuchFieldException} */
  public static class NoSuchFieldReflectException extends ReflectException {
    public NoSuchFieldReflectException(NoSuchFieldException e) { super(e); }
    public <T> T apply(ReflectExceptionVisitor<T> v) {
      return v.forNoSuchField((NoSuchFieldException) getCause());
    }
  }
  
  /** Wraps a {@link NoSuchMethodException} */
  public static class NoSuchMethodReflectException extends ReflectException {
    public NoSuchMethodReflectException(NoSuchMethodException e) { super(e); }
    public <T> T apply(ReflectExceptionVisitor<T> v) {
      return v.forNoSuchMethod((NoSuchMethodException) getCause());
    }
  }
  
  /** Wraps a {@link NullPointerException} */
  public static class NullPointerReflectException extends ReflectException {
    public NullPointerReflectException(NullPointerException e) { super(e); }
    public <T> T apply(ReflectExceptionVisitor<T> v) {
      return v.forNullPointer((NullPointerException) getCause());
    }
  }    
  
  /** Wraps an {@link IllegalArgumentException} */
  public static class IllegalArgumentReflectException extends ReflectException {
    public IllegalArgumentReflectException(IllegalArgumentException e) { super(e); }
    public <T> T apply(ReflectExceptionVisitor<T> v) {
      return v.forIllegalArgument((IllegalArgumentException) getCause());
    }
  }
  
  /** Wraps a {@link ClassCastException} */
  public static class ClassCastReflectException extends ReflectException {
    public ClassCastReflectException(ClassCastException e) { super(e); }
    public <T> T apply(ReflectExceptionVisitor<T> v) {
      return v.forClassCast((ClassCastException) getCause());
    }
  }
  
  /** Wraps an {@link InvocationTargetException} */
  public static class InvocationTargetReflectException extends ReflectException {
    public InvocationTargetReflectException(InvocationTargetException e) { super(e); }
    public <T> T apply(ReflectExceptionVisitor<T> v) {
      return v.forInvocationTarget((InvocationTargetException) getCause());
    }
  }
  
  /** Wraps an {@link InstantiationException} */
  public static class InstantiationReflectException extends ReflectException {
    public InstantiationReflectException(InstantiationException e) { super(e); }
    public <T> T apply(ReflectExceptionVisitor<T> v) {
      return v.forInstantiation((InstantiationException) getCause());
    }
  }
  
  /** Wraps an {@link IllegalAccessException} */
  public static class IllegalAccessReflectException extends ReflectException {
    public IllegalAccessReflectException(IllegalAccessException e) { super(e); }
    public <T> T apply(ReflectExceptionVisitor<T> v) {
      return v.forIllegalAccess((IllegalAccessException) getCause());
    }
  }
  
  /** Wraps an {@link SecurityException} */
  public static class SecurityReflectException extends ReflectException {
    public SecurityReflectException(SecurityException e) { super(e); }
    public <T> T apply(ReflectExceptionVisitor<T> v) {
      return v.forSecurity((SecurityException) getCause());
    }
  }
  
}
