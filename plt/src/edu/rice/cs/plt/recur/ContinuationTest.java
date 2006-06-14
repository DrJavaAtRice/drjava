package edu.rice.cs.plt.recur;

import junit.framework.TestCase;

public class ContinuationTest extends TestCase {
  
  /** A simple recursive function.  Should cause a stack overflow on large inputs. */
  public static boolean isEven(int x) {
    if (x == 0) { return true; }
    if (x == 1) { return false; }
    else { return isEven(x - 2); }
  }
  
  /** Continuation-based version of {@code isEven}.  Should be able to handle large inputs. */
  public static Continuation<Boolean> safeIsEven(final int x) {
    if (x == 0) { return ValueContinuation.make(true); }
    if (x == 1) { return ValueContinuation.make(false); }
    else {
      return new TailContinuation<Boolean>() {
        public Continuation<? extends Boolean> step() { return safeIsEven(x - 2); }
      };
    }
  }
  
  public void testIsEven() {
    // make sure isEven is defined correctly
    assertTrue(isEven(0));
    assertFalse(isEven(1));
    assertTrue(isEven(6));
    assertFalse(isEven(7));
    
    // make sure safeIsEven is defined correctly
    assertTrue(safeIsEven(0).value());
    assertFalse(safeIsEven(1).value());
    assertTrue(safeIsEven(6).value());
    assertFalse(safeIsEven(7).value());

    // this probably causes a stack overflow
    //assertTrue(isEven(300000));
    //assertFalse(isEven(300001));
    
    // this should execute without an overflow
    assertTrue(safeIsEven(300000).value());
    assertFalse(safeIsEven(300001).value());
  }
}
