/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.recur;

import junit.framework.TestCase;

public class ContinuationTest extends TestCase {
  
  /** Simulates a small bounded stack by failing if the stack exceeds a certain size. */
  private static void checkStack() {
    // stack trace is automatically filled in by the constructor
    RuntimeException e = new RuntimeException();
    if (e.getStackTrace().length > 100) { throw e; }
  }
    
  
  /** A simple recursive function.  Should cause a stack overflow on large inputs. */
  public static boolean isEven(int x) {
    checkStack();
    if (x == 0) { return true; }
    if (x == 1) { return false; }
    else { return isEven(x - 2); }
  }
  
  /** Continuation-based version of {@code isEven}.  Should be able to handle large inputs. */
  public static Continuation<Boolean> safeIsEven(final int x) {
    checkStack();
    if (x == 0) { return ValueContinuation.make(true); }
    if (x == 1) { return ValueContinuation.make(false); }
    else {
      return new PendingContinuation<Boolean>() {
        public Continuation<Boolean> step() { return safeIsEven(x - 2); }
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

    // this should cause a stack overflow
    try { isEven(500); fail("isEven(500) did not overflow the stack"); }
    catch (RuntimeException e) { /* expected behavior */ }
    
    // this should execute without a stack overflow
    assertTrue(safeIsEven(500).value());
  }
  
  
  public static long sum(int n) {
    checkStack();
    if (n == 0) return 0l;
    else return n + sum(n-1);
  }
  
  /** Continuation-based version of {@code sum}.  Should be able to handle large inputs. */
  public static Continuation<Long> safeSum(final int n) {
    checkStack();
    if (n == 0) { return ValueContinuation.make(0l); }
    else {
      return new ArgContinuation<Long, Long>() {
        public Continuation<Long> arg() { return safeSum(n-1); }
        public Continuation<Long> apply(Long arg) { return ValueContinuation.make(arg + n); }
      };
    }
  }
          
  public void testSum() {
    // make sure sum is defined correctly
    assertEquals(0l, sum(0));
    assertEquals(1l, sum(1));
    assertEquals(3l, sum(2));
    assertEquals(6l, sum(3));
    assertEquals(10l, sum(4));
    assertEquals(15l, sum(5));
    assertEquals(21l, sum(6));

    // make sure safeSum is defined correctly
    assertEquals(0l, (long) safeSum(0).value());
    assertEquals(1l, (long) safeSum(1).value());
    assertEquals(3l, (long) safeSum(2).value());
    assertEquals(6l, (long) safeSum(3).value());
    assertEquals(10l, (long) safeSum(4).value());
    assertEquals(15l, (long) safeSum(5).value());
    assertEquals(21l, (long) safeSum(6).value());

    // this should cause a stack overflow
    try { sum(500); fail("sum(500) did not overflow the stack"); }
    catch (RuntimeException e) { /* expected behavior */ }
    
    // this should execute without a stack overflow
    long bigResult = safeSum(500).value();
    assertTrue(bigResult > 500l);
    assertTrue(bigResult < (500l * 500l));
  }
  
  
  public static double fib(int n) {
    return fibHelp(n, new double[n+1]);
  }
  
  private static double fibHelp(int n, double[] results) {
    checkStack();
    if (results[n] != 0) { return results[n]; }
    else {
      double result;
      if (n == 0) { result = 0.0; }
      else if (n == 1) { result = 1.0; }
      else { result = fibHelp(n-2, results) + fibHelp(n-1, results); }
      results[n] = result;
      return result;
    }
  }
  
  public static double safeFib(int n) {
    return safeFibHelp(n, new double[n+1]).value();
  }
  
  private static Continuation<Double> safeFibHelp(final int n, final double[] results) {
    checkStack();
    if (results[n] != 0.0) { return ValueContinuation.make(results[n]); }
    else {
      if (n == 0) { results[0] = 0.0; return ValueContinuation.make(0.0); }
      else if (n == 1) { results[1] = 1.0; return ValueContinuation.make(1.0); }
      else {
        return new BinaryArgContinuation<Double, Double, Double>() {
          public Continuation<Double> arg1() { return safeFibHelp(n-2, results); }
          public Continuation<Double> arg2() { return safeFibHelp(n-1, results); }
          public Continuation<Double> apply(Double arg1, Double arg2) {
            results[n] = arg1 + arg2;
            return ValueContinuation.make(results[n]);
          }
        };
      }
    }
  }
  
  public void testFib() {
    // make sure fib is defined correctly
    assertEquals(0.0, fib(0));
    assertEquals(1.0, fib(1));
    assertEquals(1.0, fib(2));
    assertEquals(2.0, fib(3));
    assertEquals(3.0, fib(4));
    assertEquals(5.0, fib(5));
    assertEquals(8.0, fib(6));

    // make sure safeFib is defined correctly
    assertEquals(0.0, safeFib(0));
    assertEquals(1.0, safeFib(1));
    assertEquals(1.0, safeFib(2));
    assertEquals(2.0, safeFib(3));
    assertEquals(3.0, safeFib(4));
    assertEquals(5.0, safeFib(5));
    assertEquals(8.0, safeFib(6));

  
    // this should cause a stack overflow
    try { fib(500); fail("fib(500) did not overflow the stack"); }
    catch (RuntimeException e) { /* expected behavior */ }
    
    // this should execute without a stack overflow
    assertTrue(safeFib(500) > 500.0);
  }
  
}
