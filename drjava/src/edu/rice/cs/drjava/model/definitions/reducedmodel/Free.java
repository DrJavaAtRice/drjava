package edu.rice.cs.drjava;

/**
 * Shadowing state that indicates normal, unshadowed text.
 * @version$Id$
 */
public class Free extends ReducedModelState {
  public static final Free ONLY = new Free();
  
  private Free() {
  }
}