package edu.rice.cs.plt.lambda;

/**
 * <p>A wrapper for checked exceptions, allowing them to be thrown by lambda code (and in other situations 
 * in which an interface does not allow checked exceptions to be thrown).</p>
 * 
 * <p>Clients should check for the expected exception type with code like the following:
 * <blockquote><pre>
 * try { ... }
 * catch (WrappedException e) { 
 *   if (e.getCause() instanceof SomeCheckedException) { throw (SomeCheckedException) e.getCause(); }
 *   else { throw e; }
 * }
 * </pre></blockquote>
 * Unfortunately, the Java language does not allow abstraction over exception types &mdash; a method cannot
 * be declared to {@code throw} a variable type &mdash; so a generic version of the above block of code
 * cannot be included as a method here.
 * </p>
 */
public class WrappedException extends RuntimeException {
  
  public WrappedException(Throwable cause) { super(cause); }
  
}
