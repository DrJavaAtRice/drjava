/**
 * <p>A collection of implementations of {@link java.lang.Iterable} and {@link java.util.Iterator}.
 * These classes provide a lightweight alternative to the {@link java.util.Collection} classes.
 * The {@code Iterable}s are generally immutable (although many can contain mutable collections), 
 * and define little more than the {@code iterator} and {@code size} methods (as implementations
 * of {@link edu.rice.cs.plt.iter.SizedIterable}).  They allow easy composition (see 
 * {@link edu.rice.cs.plt.iter.ComposedIterable}), mapping with lambdas (see 
 * {@link edu.rice.cs.plt.iter.MappedIterable}, etc.), and interaction with tuples
 * (see {@link edu.rice.cs.plt.iter.IterUtil}).<p>
 * 
 * <p>Important note: If client code will be using Retroweaver to generate Java 1.4-compatible
 * class files, a custom {@code Iterable} class will be defined by Retroweaver (because
 * {@code java.lang.Iterable} doesn't exists in 1.4), and all {@code Collection}s treated
 * as {@code Iterable}s must be wrapped via, for example, 
 * {@link edu.rice.cs.plt.iter.IterUtil#asSizedIterable}.  Otherwise, the {@code Collection}s
 * will not be {@code Iterable}s at runtime, and unexpected errors may occur.</p>
 */
package edu.rice.cs.plt.iter;
