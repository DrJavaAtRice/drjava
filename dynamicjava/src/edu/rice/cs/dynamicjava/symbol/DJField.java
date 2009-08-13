package edu.rice.cs.dynamicjava.symbol;

import edu.rice.cs.plt.lambda.Box;

/** Represents a field declaration. */
public interface DJField extends Variable, Access.Limited {
  public boolean isStatic();
  public Access accessibility();
  public Box<Object> boxForReceiver(Object receiver);
}
