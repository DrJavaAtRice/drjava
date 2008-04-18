/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.config.*;

import edu.rice.cs.plt.tuple.Pair;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import java.util.Properties;
import java.util.List;

/** Test functions of StringOps. */
public class StringOpsTest extends DrJavaTestCase {
  /** Test the replace() method of StringOps class. */
  public void testReplace() {
    String test = "aabbccdd";
    assertEquals("testReplace:", "aab12cdd", StringOps.replace(test, "bc", "12"));
    test = "cabcabc";
    assertEquals("testReplace:", "cabc", StringOps.replace(test, "cabc", "c"));
  }
  
  /** Test the getOffsetAndLength() method of StringOps class. */
  public void testGetOffsetAndLength() {
    String test = "123456789\n123456789\n123456789\n";
    
    // The offset is always one less than the first row/col
    // The length includes the start and end positions
    Pair<Integer,Integer> oAndL = StringOps.getOffsetAndLength(test, 1, 1, 1, 9);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(0), oAndL.first());
    assertEquals("testGetOffsetAndLength- length:", new Integer(9), oAndL.second());
    
    oAndL = StringOps.getOffsetAndLength(test, 1, 1, 2, 3);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(0), oAndL.first());
    assertEquals("testGetOffsetAndLength- length:", new Integer(13), oAndL.second());
    
    oAndL = StringOps.getOffsetAndLength(test, 1, 5, 2, 3);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(4), oAndL.first());
    assertEquals("testGetOffsetAndLength- length:", new Integer(9), oAndL.second());
    
    oAndL = StringOps.getOffsetAndLength(test, 1, 1, 1, 1);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(0), oAndL.first());
    assertEquals("testGetOffsetAndLength- length:", new Integer(1), oAndL.second());
    
    oAndL = StringOps.getOffsetAndLength(test, 3, 5, 3, 5);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(24), oAndL.first());
    assertEquals("testGetOffsetAndLength- length:", new Integer(1), oAndL.second());
    
    oAndL = StringOps.getOffsetAndLength(test, 2, 3, 3, 6);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(12), oAndL.first());
    assertEquals("testGetOffsetAndLength- length:", new Integer(14), oAndL.second());
    
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
  
  /** Tests that getting the stack trace of a throwable works correctly.
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
  
  /** Tests converting a string to a literal
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
  
  /** Tests for getting the simple name of a class.
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
    * This test is commented out to remove it from regular unit testing because it is incompatible with Java 1.4.
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
  
  public void testCommandLineToList() {
    List<String> l = StringOps.commandLineToList("a b c");
    // System.err.println("l = "+java.util.Arrays.toString(l.toArray()));
    assertEquals(3, l.size());
    assertEquals("a", l.get(0));
    assertEquals("b", l.get(1));
    assertEquals("c", l.get(2));
    
    l = StringOps.commandLineToList("a'b c'");
    // System.err.println("l = "+java.util.Arrays.toString(l.toArray()));
    assertEquals(1, l.size());
    assertEquals("a'b c'", l.get(0));
    
    l = StringOps.commandLineToList("a\"b c\"");
    // System.err.println("l = "+java.util.Arrays.toString(l.toArray()));
    assertEquals(1, l.size());
    assertEquals("a\"b c\"", l.get(0));
    
    l = StringOps.commandLineToList("a\"b 'c'\"");
    // System.err.println("l = "+java.util.Arrays.toString(l.toArray()));
    assertEquals(1, l.size());
    assertEquals("a\"b 'c'\"", l.get(0));
    
    l = StringOps.commandLineToList("a \"b c\"");
    // System.err.println("l = "+java.util.Arrays.toString(l.toArray()));
    assertEquals(2, l.size());
    assertEquals("a", l.get(0));
    assertEquals("\"b c\"", l.get(1));
    
    l = StringOps.commandLineToList("\u001b");
    // System.err.println("l = "+java.util.Arrays.toString(l.toArray()));
    assertEquals(0, l.size());
    
    l = StringOps.commandLineToList("\u001b\u001b");
    // System.err.println("l = "+java.util.Arrays.toString(l.toArray()));
    assertEquals(1, l.size());
    assertEquals("\u001b", l.get(0));
    
    l = StringOps.commandLineToList("\u001b ");
    // System.err.println("l = "+java.util.Arrays.toString(l.toArray()));
    assertEquals(1, l.size());
    assertEquals(" ", l.get(0));
    
    l = StringOps.commandLineToList("a\u001b b");
    // System.err.println("l = "+java.util.Arrays.toString(l.toArray()));
    assertEquals(1, l.size());
    assertEquals("a b", l.get(0));
  }
  
  public void testReplaceVariables() {
    PropertyMaps props = new PropertyMaps();
    props.setProperty("1", new ConstantProperty("var", "foo"));
    props.setProperty("1", new ConstantProperty("xxx", "bar"));
    
    assertEquals("abcxyz", StringOps.replaceVariables("abcxyz",props,PropertyMaps.TO_STRING));
    assertEquals("abcfooxyz", StringOps.replaceVariables("abc${var}xyz",props,PropertyMaps.TO_STRING));
    assertEquals("abcbarxyz", StringOps.replaceVariables("abc${xxx}xyz",props,PropertyMaps.TO_STRING));
  }
  
  public void testReplaceVariables2() {
    PropertyMaps props = new PropertyMaps();
    props.setProperty("1", new ConstantProperty("var", "foo"));
    props.setProperty("1", new ConstantProperty("xxx", "bar"));
    props.setProperty("2", new ConstantProperty("yyy", "bam"));
    props.setProperty("2", new ConstantProperty("xxx", "new"));
    
    assertEquals("abcxyz", StringOps.replaceVariables("abcxyz",props,PropertyMaps.TO_STRING));
    assertEquals("abcfooxyz", StringOps.replaceVariables("abc${var}xyz",props,PropertyMaps.TO_STRING));
    assertEquals("abcbarxyz", StringOps.replaceVariables("abc${xxx}xyz",props,PropertyMaps.TO_STRING));
    assertEquals("abcbamxyz", StringOps.replaceVariables("abc${yyy}xyz",props,PropertyMaps.TO_STRING));
    assertEquals("abcbarbamxyz", StringOps.replaceVariables("abc${xxx}${yyy}xyz",props,PropertyMaps.TO_STRING));
  }
  
  public void testReplaceVariables3() {
    PropertyMaps props = new PropertyMaps();
    props.setProperty("1", new ConstantProperty("var", "foo"));
    props.setProperty("1", new ConstantProperty("xxx", "bar"));
    
    assertEquals("abcxyz", StringOps.replaceVariables("abcxyz",props,PropertyMaps.TO_STRING));
    assertEquals("fooxyz", StringOps.replaceVariables("${var}xyz",props,PropertyMaps.TO_STRING));
    String source = "abc $${xxx}xyz";
    String actual = StringOps.replaceVariables(source,props,PropertyMaps.TO_STRING);
    String expected = "abc ${xxx}xyz";
//    System.err.println("source  : "+source);
//    System.err.println("expected: "+expected);
//    System.err.println("actual  : "+actual);
    assertEquals(expected, actual);
    assertEquals("${xxx}xyz", StringOps.replaceVariables("$${xxx}xyz",props,PropertyMaps.TO_STRING));
  }
  
}
