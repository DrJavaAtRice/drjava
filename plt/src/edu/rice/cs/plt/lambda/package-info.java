/**
 * <p>A collection of interfaces facilitating first-class functions in Java.  Three families
 * of interfaces are defined, each with variants for 0 to 4 arguments:<ul>
 * <li>{@code Lambda}, a standard function definition (0-ary lambdas are called {@code Thunk}s)</li>
 * <li>{@code Command}, a void function</li>
 * <li>{@code Predicate}, a {@code Lambda} with return type {@code Boolean}</li>
 * </ul>
 * Since it's impossible to define a type-safe lambda with an arbitrary number of
 * arguments, a practical limit of 4 is set, and each variation is represented by a
 * distinct interface.</p>
 * 
 * <p>In addition to the above interfaces, a few general-purpose implementations are provided,
 * such as {@link edu.rice.cs.plt.lambda.LazyThunk} and {@link edu.rice.cs.plt.lambda.Box}, and 
 * some interfaces provide static constants for typical instances (such as the {@code TRUE} 
 * predicate).  The {@link edu.rice.cs.plt.lambda.LambdaUtil} class defines static methods that 
 * define and act on lambdas, allowing easy null-valued and literal-valued lambda creation, 
 * composition, currying, negation, conjunction, disjunction, conversion between types, etc.</p>
 */
package edu.rice.cs.plt.lambda;
