package edu.rice.cs.drjava;

import  junit.framework.*;
import  java.util.Vector;
import  junit.extensions.*;

/**
 * Test cases which test the implementation of BraceReduction
 * may extend this abstract class to acquire a convenience
 * function for determining the state of the current token.
 * @version $Id$
 */
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