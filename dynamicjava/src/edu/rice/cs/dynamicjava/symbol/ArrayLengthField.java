package edu.rice.cs.dynamicjava.symbol;

import java.lang.reflect.Array;
import edu.rice.cs.plt.lambda.Box;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.dynamicjava.symbol.type.Type;

/** Provides a DJField interface for accessing an array's implicit "length" field. */
public class ArrayLengthField implements DJField {
  
  public static final ArrayLengthField INSTANCE = new ArrayLengthField();
  
  /** Limit access to constructor.  Singleton should be used instead. */
  private ArrayLengthField() {}
  
  public String declaredName() { return "length"; }
  public Type type() { return TypeSystem.INT; }
  public boolean isFinal() { return true; }
  public boolean isStatic() { return false; }
  public Access accessibility() { return Access.PUBLIC; }
  public Access.Module accessModule() { return new TopLevelAccessModule("java.lang"); }
  
  public Option<Object> constantValue() { return Option.none(); }
  public Box<Object> boxForReceiver(final Object receiver) {
    return new Box<Object>() {
      public Object value() { return Array.getLength(receiver); }
      public void set(Object val) {
        throw new RuntimeException(new IllegalAccessException("Can't set an array's length"));
      }
    };
  }

}
