package edu.rice.cs.drjava.model.definitions.reducedmodel;

/**
 * This class represents a special state, passed by some methods that return
 * states, to indicate that it is necessary to back up one position before
 * continuing in an iteration over a TokenList. It probably shouldn't exist,
 * and, hopefully, it would go away if the underlying logic of the reduced 
 * model were refactored.
 *
 * @version $Id$
 */
public class Stutter extends ReducedModelState {
  public static final Stutter ONLY = new Stutter();
  
  private Stutter() {
  }
  
  ReducedModelState update(TokenList.Iterator copyCursor) {
    if (copyCursor.atStart()) {
      copyCursor.next();
    }
    return copyCursor.getStateAtCurrent();
  }
}
