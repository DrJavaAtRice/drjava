package edu.rice.cs.drjava;

import  junit.framework.*;
import  java.util.Vector;
import  junit.extensions.*;

public abstract class BraceReductionTestCase extends TestCase {
  
  public BraceReductionTestCase(String name) {
    super(name);
  }

  /**
   * Convenience function to get state of the current token.
   * @param rmc the reduced model in question
   * @return the state of the current token
   */
  ReducedModelState stateOfCurrentToken(BraceReduction br) {
    return  br.currentToken().getState();
  }
}