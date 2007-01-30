/**
 * <p>A collection of implementations of {@link java.lang.Iterable} and {@link java.util.Iterator}.
 * These classes provide a lightweight alternative to the {@link java.util.Collection} classes.
 * The {@code Iterable}s are generally immutable (although many can contain mutable collections), 
 * and define little more than the {@code iterator} and {@code size} methods (as implementations
 * of {@link edu.rice.cs.plt.iter.SizedIterable}).  They allow easy composition (see 
 * {@link edu.rice.cs.plt.iter.ComposedIterable}), mapping with lambdas (see 
 * {@link edu.rice.cs.plt.iter.MappedIterable}, etc.), and interaction with tuples
 * (see {@link edu.rice.cs.plt.iter.IterUtil}).<p>
 */
package edu.rice.cs.plt.iter;
