/**
 * <p>Classes enabling safe handling of infinite or extremely deep recursion.  
 * {@code RecursionStack}s may be used to detect repetition in recursive invocations,
 * breaking an infinite recursive loop.  {@code Continuation}s allow recursive methods
 * to be processed iteratively, avoiding potential stack overflow errors.  {@code RecurUtil}
 * takes advantage of these tools to define utility functions such as {@code safeToString()}.
 */
package edu.rice.cs.plt.recur;
