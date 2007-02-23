package edu.rice.cs.plt.lambda;

import junit.framework.TestCase;

public class LambdaUtilTest extends TestCase {
  
  private void assertEquals(int expected, Integer actual) {
    assertEquals(expected, actual.intValue());
  }
  
  private static final Lambda<Integer, Integer> plus5 = new Lambda<Integer, Integer>() {
    public Integer value(Integer arg) { return arg + 5; }
  };

  private static final Lambda<Integer, Integer> times2 = new Lambda<Integer, Integer>() {
    public Integer value(Integer arg) { return arg * 2; }
  };
  
  private static final Lambda<String, String> prefixFoo = new Lambda<String, String>() {
    public String value(String arg) { return "foo" + arg; }
  };
  
  private static final Lambda2<String, String, String> concat = 
    new Lambda2<String, String, String>() {
      public String value(String a1, String a2) { return a1 + a2; }
    };
  
  private static final Lambda2<Number, Number, Integer> minus = 
    new Lambda2<Number, Number, Integer>() {
      public Integer value(Number a1, Number a2) { return a1.intValue() - a2.intValue(); }
    };
  
  private static final Lambda3<Integer, Integer, Integer, Integer> f =
    new Lambda3<Integer, Integer, Integer, Integer>() {
      public Integer value(Integer x, Integer y, Integer z) { return 3*x - 2*y + z; }
    };
  
  private static final Lambda4<Integer, Integer, Integer, Integer, Integer> g =
    new Lambda4<Integer, Integer, Integer, Integer, Integer>() {
      public Integer value(Integer w, Integer x, Integer y, Integer z) { 
        return 4*w + 3*x - 2*y + z;
      }
    };
  
  
  /*
   * 
   * TEST METHODS
   * 
   */
  
  public void testNullLambda() {
    assertEquals(null, LambdaUtil.<String>nullLambda().value());
    assertEquals(null, LambdaUtil.<Integer>nullLambda().value("foo"));
    assertEquals(null, LambdaUtil.<Throwable>nullLambda().value("foo", 23));
    assertEquals(null, LambdaUtil.<Object>nullLambda().value(null, null, null));
    assertEquals(null, LambdaUtil.<Boolean>nullLambda().value("", 23, true, ""));
  }

  public void testIdentity() {
    Lambda<String, String> idString = LambdaUtil.identity();
    assertEquals("xyz", idString.value("xyz"));
    Lambda<Number, Number> idNum = LambdaUtil.identity();
    assertEquals(12.3, idNum.value(12.3));
    assertEquals(32, idNum.value(32));
  }
  
  public void testCompose() {
    Thunk<String> numToString1 = LambdaUtil.compose((Thunk<Integer>) LambdaUtil.valueLambda(23),
                                                    LambdaUtil.TO_STRING);
    assertEquals("23", numToString1.value());
    Thunk<String> numToString2 = LambdaUtil.compose((Thunk<Float>) LambdaUtil.valueLambda(22.5f),
                                                    LambdaUtil.TO_STRING);
    assertEquals("22.5", numToString2.value());
    
    Lambda<Integer, Integer> plus5Times2 = LambdaUtil.compose(plus5, times2);
    assertEquals(10, plus5Times2.value(0));
    assertEquals(16, plus5Times2.value(3));
    Lambda<Integer, Integer> times2Plus5 = LambdaUtil.compose(times2, plus5);
    assertEquals(5, times2Plus5.value(0));
    assertEquals(11, times2Plus5.value(3));
    
    Lambda2<Number, Number, String> minusString = LambdaUtil.compose(minus, LambdaUtil.TO_STRING);
    assertEquals("-3", minusString.value(2.3, 5));
    assertEquals("4", minusString.value(12, 8.2f));
    Lambda2<String, String, String> fooConcat = LambdaUtil.compose(concat, prefixFoo);
    assertEquals("fooab", fooConcat.value("a", "b"));
    
    Lambda3<Integer, Integer, Integer, Integer> fTimes2 = LambdaUtil.compose(f, times2);
    assertEquals(f.value(12, 20, 5) * 2, fTimes2.value(12, 20, 5));
    assertEquals(f.value(-1, -2, -3) * 2, fTimes2.value(-1, -2, -3));
    
    Lambda4<Integer, Integer, Integer, Integer, Integer> gPlus5 = LambdaUtil.compose(g, plus5);
    assertEquals(g.value(2, 12, 20, 5) + 5, gPlus5.value(2, 12, 20, 5));
    assertEquals(g.value(12, -1, -2, -3) + 5, gPlus5.value(12, -1, -2, -3));
    
  }
}
