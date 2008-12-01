/**
 * <p>A collection of interfaces facilitating first-class functions in Java.  Three families
 * of interfaces are defined, each with variants for 0 to 4 arguments:<ul>
 * <li>{@code Lambda}, a standard function definition (0-ary lambdas are called {@code Thunk}s)</li>
 * <li>{@code Runnable}, a void function (0-ary runnables are defined in {@code java.lang})</li>
 * <li>{@code Predicate}, a {@code Lambda} with return type {@code Boolean} (0-ary predicates are called
 *     {@code Condition}s)</li>
 * </ul>
 * Since it's impossible to define a type-safe lambda with an arbitrary number of
 * arguments, a practical limit of 4 is set, and each variation is represented by a
 * distinct interface.</p>
 * 
 * <p>In addition to the above interfaces, a few general-purpose implementations are provided,
 * such as {@link edu.rice.cs.plt.lambda.LazyThunk} and {@link edu.rice.cs.plt.lambda.Box}
 * The {@link edu.rice.cs.plt.lambda.LambdaUtil} class defines static constants and methods that 
 * define and act on lambdas, allowing easy null-valued and literal-valued lambda creation, 
 * composition, currying, negation, conjunction, disjunction, conversion between types, etc.</p>
 */
package edu.rice.cs.plt.lambda;
