/**
 * <p>Classes for the type-parameterized representation of tuples.  Since it's
 * not possible to define a type-safe tuple of arbitrary size, a fixed set of 
 * practical tuples is implemented instead.  Each shares the {@link edu.rice.cs.plt.tuple.Tuple} 
 * parent class, which provides facilities for optimized, lazy hash code calculation.  Each n-tuple
 * class provides the following:<ul>
 * <li>A constructor taking n arguments</li>
 * <li>A static {@code make} method that simply calls the constructor, allowing the
 *     type arguments to be inferred</li>
 * <li>{@code first}, {@code second}, ..., {@code nth} accessor methods</li>
 * <li>{@code toString}, which produces ouput like {@code "(1, 2, 3, 4)"}</li>
 * <li>{@code equals} and {@code hashCode} defined in terms of the tuple elements</li>
 * </ul></p>
 * 
 * <p>Each n-tuple class also has an identity-based subclass (such as 
 * {@link edu.rice.cs.plt.tuple.IdentityTriple} that defines {@code equals} and {@code hashCode}
 * in terms of their elements' identity ({@code ==}) instead of equality ({@code equals}).</p>
 * 
 * <p>All tuples implement the {@link java.io.Serializable} interface.  However, as wrappers for 
 * arbitrary objects, tuples will serialize without error only if the wrapped objects are 
 * serializable.</p>
 */
package edu.rice.cs.plt.tuple;
