package koala.dynamicjava.interpreter.error;

import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.util.*;

/**
 * This error is thrown when an NameVisitor encounters QualifiedName
 * that denotes ReferenceType.  In most contexts, this is an error
 *
 * @author Java PLT
 * */
public class PossibleExecutionError extends ExecutionError {
  private ReferenceType referenceType;
  
  
  /**
   * Constructs a <code>PossiblexecutionError</code> with the specified 
   * detail message, tree node, and refereence type.
   * @param s  the detail message (a key in a resource file).
   * @param n  the syntax tree node where the error occurs
   * @param rt the ReferenceType returned in this error
   */
  public PossibleExecutionError(String s, Node n, ReferenceType rt) {
    super(s, n);
    referenceType = rt;
  }
  
  public ReferenceType getReferenceType() { return referenceType; }
}