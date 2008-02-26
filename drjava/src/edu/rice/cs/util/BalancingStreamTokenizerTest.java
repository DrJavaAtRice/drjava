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

import junit.framework.TestCase;

import java.io.StringReader;
import java.io.IOException;

/**
 * Tests for a class to tokenize a stream while balancing quoting characters.
 * @author Mathias Ricken
 * @version $Id$
 */

public class BalancingStreamTokenizerTest extends TestCase {
  BalancingStreamTokenizer make(String s) {
    return new BalancingStreamTokenizer(new StringReader(s));
  }
  BalancingStreamTokenizer make(String s, Character c) {
    return new BalancingStreamTokenizer(new StringReader(s),c);
  }
  
  public void testSimple() throws IOException {
    BalancingStreamTokenizer tok = make("abc def\\ ghi 123\n456");
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("def\\", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("ghi", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc \"def ghi\" 123\n456 'abc def' 789");
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("\"def ghi\"", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc def'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testNestedQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc \"def ghi 'abc'\" 123\n456 'abc def \"xxx '111' yyy\"' 789");
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("\"def ghi 'abc'\"", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc def \"xxx '111' yyy\"'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testQuotedNW() throws IOException {
    BalancingStreamTokenizer tok = make("abc\"def ghi\"123\n456'abc def'789");
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("\"def ghi\"", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc def'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testNestedQuotedNW() throws IOException {
    BalancingStreamTokenizer tok = make("abc\"def ghi 'abc'\"123\n456'abc def \"xxx '111' yyy\"'789");
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("\"def ghi 'abc'\"", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc def \"xxx '111' yyy\"'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testDollarQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc ${def ghi} 123\n456 `abc def` 789");
    tok.defaultThreeQuoteDollarCurlySetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${def ghi}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("`abc def`", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testDollarNestedQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc ${def ghi 'abc'} 123\n456 ${abc def \"xxx '111' yyy\"} 789");
    tok.defaultThreeQuoteDollarCurlySetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${def ghi 'abc'}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testDollarNestedQuotedKeywords() throws IOException {
    BalancingStreamTokenizer tok = make("abc=${def;ghi='abc'};123\n456 ${abc def \"xxx '111' yyy\"} 789");
    tok.defaultThreeQuoteDollarCurlySetup();
    tok.addKeyword(";");
    tok.addKeyword("=");
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("=", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${def;ghi='abc'}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(";", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
 
  public void testDollarNestedQuotedKeywordsWSSignificant() throws IOException {
    BalancingStreamTokenizer tok = make("abc=${def;ghi='abc'};123\n456 ${abc def \"xxx '111' yyy\"} 789");
    tok.wordRange(0,255);
    tok.addQuotes("\"", "\"");
    tok.addQuotes("'", "'");
    tok.addQuotes("`", "`");
    tok.addQuotes("${", "}");
    tok.addKeyword(";");
    tok.addKeyword("=");
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("=", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${def;ghi='abc'}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(";", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123\n456 ", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(" 789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  /* Escape tests */
  public void testEscapeSimple() throws IOException {
    BalancingStreamTokenizer tok = make("abc def\\ ghi 123\n456", '\\');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("def ghi", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapeSimple2() throws IOException {
    BalancingStreamTokenizer tok = make("abc def\\\\ ghi 123\n456", '\\');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("def\\", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("ghi", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
    
  public void testEscapeSimple3() throws IOException {
    BalancingStreamTokenizer tok = make("foo \\ bar", '\\');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("foo", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(" bar", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
    
  public void testEscapeSimple4() throws IOException {
    BalancingStreamTokenizer tok = make("foo\\bar", '\\');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("foobar", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapeQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc \\\"def ghi\\\" 123\n456 'abc def\\' xxx' 789", '\\');
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("\"def", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("ghi\"", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc def' xxx'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapeNestedQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc \\\"def ghi 'abc'\\\" 123\n456 'abc def \\\"xxx \\'111\\' yyy\\\"' 789", '\\');
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("\"def", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("ghi", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("\"", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc def \"xxx '111' yyy\"'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapeQuotedNW() throws IOException {
    BalancingStreamTokenizer tok = make("abc\\\"def ghi\\\"123\n456'abc def'789", '\\');
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc\"def", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("ghi\"123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc def'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapeNestedQuotedNW() throws IOException {
    BalancingStreamTokenizer tok = make("abc\\\"def ghi 'abc'\\\"123\n456'abc def \\\\\"xxx \\\\'111\\\\' yyy\\\\\"'789", '\\');
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc\"def", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("ghi", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("\"123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc def \\\"xxx \\'111\\' yyy\\\"'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapeDollarQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc \\${def ghi} 123\n\\\\${xxx yyy}456 `abc def` 789", '\\');
    tok.defaultThreeQuoteDollarCurlySetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${def", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("ghi}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("\\", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${xxx yyy}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("`abc def`", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapeDollarNestedQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc \\${def ghi 'abc'} 123\n456 \\\\${abc def \"xxx '111' yyy\"} 789", '\\');
    tok.defaultThreeQuoteDollarCurlySetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${def", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("ghi", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("\\", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapeDollarNestedQuotedKeywords() throws IOException {
    BalancingStreamTokenizer tok = make("abc\\=${def\\;ghi='abc'}\\;123\n456 ${abc def \"xxx '111' yyy\"} 789", '\\');
    tok.defaultThreeQuoteDollarCurlySetup();
    tok.addKeyword(";");
    tok.addKeyword("=");
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc=", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${def;ghi='abc'}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(";123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapeDollarNestedQuotedKeywordsWithEscapeWSSignificant() throws IOException {
    BalancingStreamTokenizer tok = make("abc\\\\=${def;ghi='abc'};123\n456 ${abc def \"xxx '111' yyy\"} 789",'\\');
    tok.wordRange(0,255);
    tok.addQuotes("\"", "\"");
    tok.addQuotes("'", "'");
    tok.addQuotes("`", "`");
    tok.addQuotes("${", "}");
    tok.addKeyword(";");
    tok.addKeyword("\\=");
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("\\=", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${def;ghi='abc'}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(";", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123\n456 ", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(" 789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapeDollarNestedQuotedKeywordsWSSignificant() throws IOException {
    BalancingStreamTokenizer tok = make("abc\\=${def;ghi='abc'}\\\\;123\n456 ${abc def \"xxx '111' yyy\"} 789",'\\');
    tok.wordRange(0,255);
    tok.addQuotes("\"", "\"");
    tok.addQuotes("'", "'");
    tok.addQuotes("`", "`");
    tok.addQuotes("${", "}");
    tok.addKeyword(";");
    tok.addKeyword("=");
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc=", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${def;ghi='abc'}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("\\", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(";", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123\n456 ", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(" 789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  /* Escape tests with a different character: |*/
  public void testEscapePipeSimple() throws IOException {
    BalancingStreamTokenizer tok = make("abc def| ghi 123\n456", '|');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("def ghi", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapePipeSimple2() throws IOException {
    BalancingStreamTokenizer tok = make("abc def|| ghi 123\n456", '|');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("def|", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("ghi", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
    
  public void testEscapePipeSimple3() throws IOException {
    BalancingStreamTokenizer tok = make("foo | bar", '|');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("foo", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(" bar", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
    
  public void testEscapePipeSimple4() throws IOException {
    BalancingStreamTokenizer tok = make("foo|bar", '|');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("foobar", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapePipeQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc |\"def ghi|\" 123\n456 'abc def|' xxx' 789", '|');
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("\"def", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("ghi\"", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc def' xxx'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapePipeNestedQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc |\"def ghi 'abc'|\" 123\n456 'abc def |\"xxx |'111|' yyy|\"' 789", '|');
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("\"def", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("ghi", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("\"", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc def \"xxx '111' yyy\"'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapePipeQuotedNW() throws IOException {
    BalancingStreamTokenizer tok = make("abc|\"def ghi|\"123\n456'abc def'789", '|');
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc\"def", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("ghi\"123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc def'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapePipeNestedQuotedNW() throws IOException {
    BalancingStreamTokenizer tok = make("abc|\"def ghi 'abc'|\"123\n456'abc def ||\"xxx ||'111||' yyy||\"'789", '|');
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc\"def", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("ghi", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("\"123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc def |\"xxx |'111|' yyy|\"'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapePipeDollarQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc |${def ghi} 123\n||${xxx yyy}456 `abc def` 789", '|');
    tok.defaultThreeQuoteDollarCurlySetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${def", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("ghi}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("|", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${xxx yyy}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("`abc def`", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapePipeDollarNestedQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc |${def ghi 'abc'} 123\n456 ||${abc def \"xxx '111' yyy\"} 789", '|');
    tok.defaultThreeQuoteDollarCurlySetup();
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${def", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("ghi", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("'abc'", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("|", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapePipeDollarNestedQuotedKeywords() throws IOException {
    BalancingStreamTokenizer tok = make("abc|=${def|;ghi='abc'}|;123\n456 ${abc def \"xxx '111' yyy\"} 789", '|');
    tok.defaultThreeQuoteDollarCurlySetup();
    tok.addKeyword(";");
    tok.addKeyword("=");
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc=", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${def;ghi='abc'}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(";123", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("456", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapePipeDollarNestedQuotedKeywordsWithEscapeWSSignificant() throws IOException {
    BalancingStreamTokenizer tok = make("abc||=${def;ghi='abc'};123\n456 ${abc def \"xxx '111' yyy\"} 789",'|');
    tok.wordRange(0,255);
    tok.addQuotes("\"", "\"");
    tok.addQuotes("'", "'");
    tok.addQuotes("`", "`");
    tok.addQuotes("${", "}");
    tok.addKeyword(";");
    tok.addKeyword("|=");
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("|=", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${def;ghi='abc'}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(";", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123\n456 ", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(" 789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
  
  public void testEscapePipeDollarNestedQuotedKeywordsWithEscape2WSSignificant() throws IOException {
    BalancingStreamTokenizer tok = make("abc=||${def;ghi='abc'};123\n456 ${abc def \"xxx '111' yyy\"} 789",'|');
    tok.wordRange(0,255);
    tok.addQuotes("\"", "\"");
    tok.addQuotes("'", "'");
    tok.addQuotes("`", "`");
    tok.addQuotes("${", "}");
    tok.addKeyword(";");
    tok.addKeyword("=|");
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("=|", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${def;ghi='abc'}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(";", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123\n456 ", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(" 789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }

  public void testEscapePipeDollarNestedQuotedKeywordsWSSignificant() throws IOException {
    BalancingStreamTokenizer tok = make("abc|=${def;ghi='abc'}||;123\n456 ${abc def \"xxx '111' yyy\"} 789",'|');
    tok.wordRange(0,255);
    tok.addQuotes("\"", "\"");
    tok.addQuotes("'", "'");
    tok.addQuotes("`", "`");
    tok.addQuotes("${", "}");
    tok.addKeyword(";");
    tok.addKeyword("=");
    String s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("abc=", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${def;ghi='abc'}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("|", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(";", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("123\n456 ", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(" 789", s);
    s = tok.getNextToken();
    // System.out.println(s);
    assertEquals(null, s);
  }
}
