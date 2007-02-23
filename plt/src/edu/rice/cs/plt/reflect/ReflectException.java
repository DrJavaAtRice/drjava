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
