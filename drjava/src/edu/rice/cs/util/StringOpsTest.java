/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util;

import edu.rice.cs.drjava.DrJavaTestCase;

import java.io.PrintWriter;

/**
 * Test functions of StringOps.
 */
public class StringOpsTest extends DrJavaTestCase {
  /**
   *  Test the replace() method of StringOps class
   */
  public void testReplace() {
    String test = "aabbccdd";
    assertEquals("testReplace:", "aab12cdd", StringOps.replace(test, "bc", "12"));
    test = "cabcabc";
    assertEquals("testReplace:", "cabc", StringOps.replace(test, "cabc", "c"));
  }

  /**
   *  Test the getOffsetAndLength() method of StringOps class
   */
  public void testGetOffsetAndLength() {
    String test = "123456789\n123456789\n123456789\n";

    // The offset is always one less than the first row/col
    // The length includes the start and end positions
    Pair<Integer,Integer> oAndL = StringOps.getOffsetAndLength(test, 1, 1, 1, 9);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(0), oAndL.getFirst());
    assertEquals("testGetOffsetAndLength- length:", new Integer(9), oAndL.getSecond());

    oAndL = StringOps.getOffsetAndLength(test, 1, 1, 2, 3);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(0), oAndL.getFirst());
    assertEquals("testGetOffsetAndLength- length:", new Integer(13), oAndL.getSecond());

    oAndL = StringOps.getOffsetAndLength(test, 1, 5, 2, 3);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(4), oAndL.getFirst());
    assertEquals("testGetOffsetAndLength- length:", new Integer(9), oAndL.getSecond());

    oAndL = StringOps.getOffsetAndLength(test, 1, 1, 1, 1);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(0), oAndL.getFirst());
    assertEquals("testGetOffsetAndLength- length:", new Integer(1), oAndL.getSecond());

    oAndL = StringOps.getOffsetAndLength(test, 3, 5, 3, 5);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(24), oAndL.getFirst());
    assertEquals("testGetOffsetAndLength- length:", new Integer(1), oAndL.getSecond());

    oAndL = StringOps.getOffsetAndLength(test, 2, 3, 3, 6);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(12), oAndL.getFirst());
    assertEquals("testGetOffsetAndLength- length:", new Integer(14), oAndL.getSecond());

    try {
      StringOps.getOffsetAndLength(test, 3, 2, 2, 3);
      fail("Should not have been able to compute offset where startRow > endRow");
    }
    catch (IllegalArgumentException ex) {
      // correct behavior
    }

    try {
      StringOps.getOffsetAndLength(test, 2, 4, 2, 3);
      fail("Should not have been able to compute offset where start > end");
    }
    catch (IllegalArgumentException ex) {
      // correct behavior
    }

    try {
      StringOps.getOffsetAndLength(test, 4, 4, 5, 5);
      fail("Should not have been able to compute offset where the\n" +
           "given coordinates are not contained within the string");
    }
    catch (IllegalArgumentException ex) {
      // correct behavior
    }

    try {
      StringOps.getOffsetAndLength(test, 3, 4, 3, 12);
      fail("Should not have been able to compute offset where the\n" +
           "given coordinates are not contained within the string");
    }
    catch (IllegalArgumentException ex) {
      // correct behavior
    }

    try {
      StringOps.getOffsetAndLength(test, 2, 15, 3, 1);
      fail("Should not have been able to compute offset where the\n" +
           "given coordinates are not contained within the string");
    }
    catch (IllegalArgumentException ex) {
      // correct behavior
    }
  }

  /**
   * Tests that getting the stack trace of a throwable works correctly.
   */
  public void testGetStackTrace() {
    final String trace = "hello";
    Throwable t = new Throwable() {
      public void printStackTrace(PrintWriter w) {
        w.print(trace);
      }
    };
    assertEquals("Should have returned the correct stack trace!", trace, StringOps.getStackTrace(t));
  }

  /**
   * Tests converting a string to a literal
   */
  public void testConvertToLiteral() {
    String toConvert = " a  b  c  d";
    String expResult = "\" a  b  c  d\"";
    String actResult = StringOps.convertToLiteral(toConvert);
    assertEquals("converting "+toConvert+" should yield "+ expResult, expResult, actResult);

    toConvert = "\\ hello world \\";
    expResult = "\"\\\\ hello world \\\\\"";
    actResult = StringOps.convertToLiteral(toConvert);
    assertEquals("converting "+toConvert+" should yield "+ expResult, expResult, actResult);

    toConvert = "\\\n\\n";
    expResult = "\"\\\\\\n\\\\n\"";
    actResult = StringOps.convertToLiteral(toConvert);
    assertEquals("converting "+toConvert+" should yield "+ expResult, expResult, actResult);

    toConvert = "\\\"\t\\t";
    expResult = "\"\\\\\\\"\\t\\\\t\"";
    actResult = StringOps.convertToLiteral(toConvert);
    assertEquals("converting "+toConvert+" should yield "+ expResult, expResult, actResult);

    toConvert = "\"\\\"\t\\n\n\\\n\"";
    expResult = "\"\\\"\\\\\\\"\\t\\\\n\\n\\\\\\n\\\"\"";
    actResult = StringOps.convertToLiteral(toConvert);
    assertEquals("converting "+toConvert+" should yield "+ expResult, expResult, actResult);

    toConvert = "    ";
    expResult = "\"    \"";
    actResult = StringOps.convertToLiteral(toConvert);
    assertEquals("converting "+toConvert+" should yield "+ expResult, expResult, actResult);
  }
  
  private static class TestGetSimpleNameInner {
    public static class Nested {
      public static Class anonClass() {
        java.awt.event.ActionListener l = new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) { }
        };
        return l.getClass();
      }
    }
    public class Inner {
      public Class anonClass() {
        java.awt.event.ActionListener l = new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) { }
        };
        return l.getClass();
      }
    }
    public Inner getInner() {
      return new Inner();
    }
    public static Class anonClass() {
      java.awt.event.ActionListener l = new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) { }
      };
      return l.getClass();
    }
    public static Lambda<Class, Object> getLambda() {
      return new Lambda<Class, Object>() {
        public Class apply(Object param) {
          java.awt.event.ActionListener l = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) { }
          };
          return l.getClass();
        }
      };
    }
  }

  /**
   * Tests for getting the simple name of a class.
   */
  public void testGetSimpleName() {
    String exp = "Integer";
    String act = StringOps.getSimpleName(java.lang.Integer.class);
    assertEquals("Wrong simple name for java.lang.Integer, exp="+exp+", act="+act,
                 exp,
                 act);
    
    exp = "TestGetSimpleNameInner";
    act = StringOps.getSimpleName(TestGetSimpleNameInner.class);
    assertEquals("Wrong simple name for TestGetSimpleNameInner, exp="+exp+", act="+act,
                 exp,
                 act);
    
    exp = "Nested";
    act = StringOps.getSimpleName(TestGetSimpleNameInner.Nested.class);
    assertEquals("Wrong simple name for TestGetSimpleNameInner.Nested, exp="+exp+", act="+act,
                 exp,
                 act);
    
    exp = "Inner";
    act = StringOps.getSimpleName(TestGetSimpleNameInner.Inner.class);
    assertEquals("Wrong simple name for TestGetSimpleNameInner.Inner, exp="+exp+", act="+act,
                 exp,
                 act);
    
    java.awt.event.ActionListener l = new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) { }
    };
    
    exp = "";
    act = StringOps.getSimpleName(l.getClass());
    assertEquals("Wrong simple name for anonymous inner class, exp="+exp+", act="+act,
                 exp,
                 act);
    
    exp = "";
    act = StringOps.getSimpleName(TestGetSimpleNameInner.anonClass());
    assertEquals("Wrong simple name for anonymous inner class, exp="+exp+", act="+act,
                 exp,
                 act);
    
    exp = "";
    act = StringOps.getSimpleName(TestGetSimpleNameInner.Nested.anonClass());
    assertEquals("Wrong simple name for anonymous inner class, exp="+exp+", act="+act,
                 exp,
                 act);
    
    exp = "";
    act = StringOps.getSimpleName((new TestGetSimpleNameInner()).getInner().anonClass());
    assertEquals("Wrong simple name for anonymous inner class, exp="+exp+", act="+act,
                 exp,
                 act);
    
    exp = "";
    act = StringOps.getSimpleName(TestGetSimpleNameInner.getLambda().apply(null));
    assertEquals("Wrong simple name for anonymous inner class, exp="+exp+", act="+act,
                 exp,
                 act);
  }
  
  /** Tests for getting the simple name of a class. Works by comparing with Java 1.5.0's Class.getSimpleName().
   *  This test is commented out to remove it from regular unit testing because it is incompatible with Java 1.4.
   */
//  public void testGetSimpleName15() {
//    String exp = java.lang.Integer.class.getSimpleName();
//    String act = StringOps.getSimpleName(java.lang.Integer.class);
//    assertEquals("Wrong simple name for java.lang.Integer, exp="+exp+", act="+act,
//                 exp,
//                 act);
//    
//    exp = TestGetSimpleNameInner.class.getSimpleName();
//    act = StringOps.getSimpleName(TestGetSimpleNameInner.class);
//    assertEquals("Wrong simple name for TestGetSimpleNameInner, exp="+exp+", act="+act,
//                 exp,
//                 act);
//    
//    exp = TestGetSimpleNameInner.Nested.class.getSimpleName();
//    act = StringOps.getSimpleName(TestGetSimpleNameInner.Nested.class);
//    assertEquals("Wrong simple name for TestGetSimpleNameInner.Nested, exp="+exp+", act="+act,
//                 exp,
//                 act);
//    
//    exp = TestGetSimpleNameInner.Inner.class.getSimpleName();
//    act = StringOps.getSimpleName(TestGetSimpleNameInner.Inner.class);
//    assertEquals("Wrong simple name for TestGetSimpleNameInner.Inner, exp="+exp+", act="+act,
//                 exp,
//                 act);
//    
//    java.awt.event.ActionListener l = new java.awt.event.ActionListener() {
//      public void actionPerformed(java.awt.event.ActionEvent e) { }
//    };
//    
//    exp = l.getClass().getSimpleName();
//    act = StringOps.getSimpleName(l.getClass());
//    assertEquals("Wrong simple name for anonymous inner class, exp="+exp+", act="+act,
//                 exp,
//                 act);
//    
//    exp = TestGetSimpleNameInner.anonClass().getSimpleName();
//    act = StringOps.getSimpleName(TestGetSimpleNameInner.anonClass());
//    assertEquals("Wrong simple name for anonymous inner class, exp="+exp+", act="+act,
//                 exp,
//                 act);
//    
//    exp = TestGetSimpleNameInner.Nested.anonClass().getSimpleName();
//    act = StringOps.getSimpleName(TestGetSimpleNameInner.Nested.anonClass());
//    assertEquals("Wrong simple name for anonymous inner class, exp="+exp+", act="+act,
//                 exp,
//                 act);
//    
//    exp = (new TestGetSimpleNameInner()).getInner().anonClass().getSimpleName();
//    act = StringOps.getSimpleName((new TestGetSimpleNameInner()).getInner().anonClass());
//    assertEquals("Wrong simple name for anonymous inner class, exp="+exp+", act="+act,
//                 exp,
//                 act);
//    
//    exp = TestGetSimpleNameInner.getLambda().apply(null).getSimpleName();
//    act = StringOps.getSimpleName(TestGetSimpleNameInner.getLambda().apply(null));
//    assertEquals("Wrong simple name for anonymous inner class, exp="+exp+", act="+act,
//                 exp,
//                 act);
//  }
  
  public void testToStringLong() {
    long[] a = null;
    assertEquals("null", StringOps.toString(a));
    assertEquals("[]", StringOps.toString(new long[] {}));
    assertEquals("[1]", StringOps.toString(new long[] {1}));
    assertEquals("[1, 2]", StringOps.toString(new long[] {1, 2}));
  }
  
  public void testToStringInt() {
    int[] a = null;
    assertEquals("null", StringOps.toString(a));
    assertEquals("[]", StringOps.toString(new int[] {}));
    assertEquals("[1]", StringOps.toString(new int[] {1}));
    assertEquals("[1, 2]", StringOps.toString(new int[] {1, 2}));
  }
  
  public void testToStringShort() {
    short[] a = null;
    assertEquals("null", StringOps.toString(a));
    assertEquals("[]", StringOps.toString(new short[] {}));
    assertEquals("[1]", StringOps.toString(new short[] {1}));
    assertEquals("[1, 2]", StringOps.toString(new short[] {1, 2}));
  }
  
  public void testToStringChar() {
    char[] a = null;
    assertEquals("null", StringOps.toString(a));
    assertEquals("[]", StringOps.toString(new char[] {}));
    assertEquals("[a]", StringOps.toString(new char[] {'a'}));
    assertEquals("[a, b]", StringOps.toString(new char[] {'a', 'b'}));
  }
  
  public void testToStringByte() {
    byte[] a = null;
    assertEquals("null", StringOps.toString(a));
    assertEquals("[]", StringOps.toString(new byte[] {}));
    assertEquals("[1]", StringOps.toString(new byte[] {1}));
    assertEquals("[1, 2]", StringOps.toString(new byte[] {1, 2}));
  }
  
  public void testToStringBoolean() {
    boolean[] a = null;
    assertEquals("null", StringOps.toString(a));
    assertEquals("[]", StringOps.toString(new boolean[] {}));
    assertEquals("[true]", StringOps.toString(new boolean[] {true}));
    assertEquals("[true, false]", StringOps.toString(new boolean[] {true, false}));
  }
  
  public void testToStringFloat() {
    float[] a = null;
    assertEquals("null", StringOps.toString(a));
    assertEquals("[]", StringOps.toString(new float[] {}));
    assertEquals("[1.23]", StringOps.toString(new float[] {1.23f}));
    assertEquals("[1.23, 4.56]", StringOps.toString(new float[] {1.23f, 4.56f}));
  }
  
  public void testToStringDouble() {
    double[] a = null;
    assertEquals("null", StringOps.toString(a));
    assertEquals("[]", StringOps.toString(new double[] {}));
    assertEquals("[1.23]", StringOps.toString(new double[] {1.23}));
    assertEquals("[1.23, 4.56]", StringOps.toString(new double[] {1.23, 4.56}));
  }
  
  public void testToStringObject() {
    Object[] a = null;
    assertEquals("null", StringOps.toString(a));
    assertEquals("[]", StringOps.toString(new Object[] {}));
    assertEquals("[123]", StringOps.toString(new Object[] {"123"}));
    assertEquals("[123, 123]", StringOps.toString(new Object[] {"123", new Integer(123)}));
  }
  
  public void testMemSizeToString() {
    assertEquals("<1024", "500 bytes", StringOps.memSizeToString(500));
    assertEquals("1KB", "1 kilobyte", StringOps.memSizeToString(1024));
    assertEquals("2KB", "2 kilobytes", StringOps.memSizeToString(1024*2));
    assertEquals("1.5KB", "1.50 kilobytes", StringOps.memSizeToString((long)(1024*1.5)));
    assertEquals("1MB", "1 megabyte", StringOps.memSizeToString((1024*1024)));
    assertEquals("2MB", "2 megabytes", StringOps.memSizeToString((1024*1024*2)));
    assertEquals("1.1MB", "1.10 megabytes", StringOps.memSizeToString((long)(1024*1024*1.1)));
    assertEquals("1GB", "1 gigabyte", StringOps.memSizeToString((1024*1024*1024)));
    assertEquals("1.25GB", "1.25 gigabytes", StringOps.memSizeToString((long)(1024*1024*1024*1.25)));
  }
}
