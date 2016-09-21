package edu.rice.cs.dynamicjava.symbol;

import edu.rice.cs.plt.lambda.Box;
import edu.rice.cs.plt.tuple.Option;

/** Represents a field declaration. */
public interface DJField extends Variable, Access.Limited {
  /** The class declaring this field.  May be null for certain special fields. */
  public DJClass declaringClass();
  public boolean isStatic();
  public Access accessibility();
  public Option<Object> constantValue();
  public Box<Object> boxForReceiver(Object receiver);
}
