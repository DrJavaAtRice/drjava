/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util;

import junit.framework.TestCase;

import java.io.StringReader;
import java.io.IOException;

/**
 * Tests for a class to tokenize a stream while balancing quoting characters.
 * @author Mathias Ricken
 * @version $Id: BalancingStreamTokenizerTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */

public class BalancingStreamTokenizerTest extends TestCase {
  BalancingStreamTokenizer make(String s) {
    return new BalancingStreamTokenizer(new StringReader(s));
  }
  BalancingStreamTokenizer make(String s, Character c) {
    return new BalancingStreamTokenizer(new StringReader(s),c);
  }
  
  public void testCopyConstructor() throws IOException{
    BalancingStreamTokenizer tok = make("abc def\\ ghi 123\n456");
    tok.defaultWhitespaceSetup();
    String s1 = tok.getNextToken();
    assertEquals("abc", s1);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    
    BalancingStreamTokenizer copyTok = make("abc def\\ ghi 123\n456");
    copyTok.setState(new BalancingStreamTokenizer.State(tok.getState()));
    String s2 = copyTok.getNextToken();
    assertEquals("abc", s2);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, copyTok.token());    
  }
  
  public void testSimple() throws IOException {
    BalancingStreamTokenizer tok = make("abc def\\ ghi 123\n456");
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("def\\", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("ghi", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc \"def ghi\" 123\n456 'abc def' 789");
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());        
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\"def ghi\"", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc def'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testNestedQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc \"def ghi 'abc'\" 123\n456 'abc def \"xxx '111' yyy\"' 789");
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\"def ghi 'abc'\"", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc def \"xxx '111' yyy\"'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testQuotedNW() throws IOException {
    BalancingStreamTokenizer tok = make("abc\"def ghi\"123\n456'abc def'789");
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\"def ghi\"", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc def'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testNestedQuotedNW() throws IOException {
    BalancingStreamTokenizer tok = make("abc\"def ghi 'abc'\"123\n456'abc def \"xxx '111' yyy\"'789");
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\"def ghi 'abc'\"", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc def \"xxx '111' yyy\"'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testThreeQuoteSetup() throws IOException {
    BalancingStreamTokenizer tok = make("abc\"def ghi 'abc'\"123\n456'abc `def` \"xxx '111' yyy\"'789");
    tok.defaultThreeQuoteSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\"def ghi 'abc'\"", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc `def` \"xxx '111' yyy\"'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testTwoQuoteCurlySetup() throws IOException {
    BalancingStreamTokenizer tok = make("abc\"def {ghi} 'abc'\"123\n456'abc def \"xxx '111' yyy\"'789");
    tok.defaultTwoQuoteCurlySetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\"def {ghi} 'abc'\"", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc def \"xxx '111' yyy\"'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testThreeQuoteCurlySetup() throws IOException {
    BalancingStreamTokenizer tok = make("abc\"def {ghi} 'abc'\"123\n456'abc `def` \"xxx '111' yyy\"'789");
    tok.defaultThreeQuoteCurlySetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\"def {ghi} 'abc'\"", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc `def` \"xxx '111' yyy\"'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testDollarQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc ${def ghi} 123\n456 `abc def` 789");
    tok.defaultThreeQuoteDollarCurlySetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def ghi}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("`abc def`", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testWordRange() throws IOException {
    BalancingStreamTokenizer tok = make("Hello World");
    tok.defaultThreeQuoteDollarCurlySetup();
    tok.whitespaceRange(0,255);
    tok.wordRange(97,122);
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("ello", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("orld", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
  }
  
  public void testWordRangeKeywordsQuotes() throws IOException {
    BalancingStreamTokenizer tok = make("Hello World");
    tok.defaultThreeQuoteDollarCurlySetup();
    tok.whitespaceRange(0,96);
    tok.addKeyword("hello");
    tok.addKeyword("world");
    tok.addQuotes("a:",":a");
    tok.wordRange(97,122);
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("ello", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("orld", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
  }
  
  public void testWhitespaceKeywordsQuotes() throws IOException {
    BalancingStreamTokenizer tok = make("Hello World");
    tok.defaultThreeQuoteDollarCurlySetup();
    tok.wordRange(0,96);
    tok.addKeyword("hello");
    tok.addKeyword("world");
    tok.addQuotes("a:",":a");
    tok.whitespaceRange(97,122);
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("H", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(" W", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
  }
  
  public void testWordCharKeyword() throws IOException{
    BalancingStreamTokenizer tok = make("Hello World anthem banana");
    tok.defaultThreeQuoteDollarCurlySetup();
    tok.addKeyword("apple");
    tok.addKeyword("alabama");
    tok.addKeyword("anthem");
    tok.addKeyword("banana");
    tok.addQuotes("a:",":a");
    tok.addQuotes("b:",":b");
    tok.wordChars(97);
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("Hello", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("World", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("anthem", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
     s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("banana", s);
    assertEquals(BalancingStreamTokenizer.Token.KEYWORD, tok.token());
  }
  
  public void testWhitespaceKeyword() throws IOException{
    BalancingStreamTokenizer tok = make("abc apple");
    tok.defaultThreeQuoteDollarCurlySetup();
    tok.addKeyword("apple");
    tok.addKeyword("alabama");
    tok.addKeyword("anthem");
    tok.addKeyword("banana");
    tok.addQuotes("a:",":a");
    tok.addQuotes("a","-a");
    tok.addQuotes("b:",":b");
    tok.whitespace(97);
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("bc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("pple", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
  }
  
  public void testFindMatchKeywords() throws IOException{
    BalancingStreamTokenizer tok = make("abc");
    tok.defaultThreeQuoteDollarCurlySetup();
    tok.addKeyword("apple");
    tok.addKeyword("alabama");
    tok.addKeyword("anthem");
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
  }
  
  public void testEscapeWhitespaceRange() throws IOException{
    BalancingStreamTokenizer tok = make("aBc",'a');
    tok.defaultThreeQuoteDollarCurlySetup();
    tok.whitespaceRange(97,122);
    String s = tok.getNextToken();
    assertEquals("aB", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
  }
  
  public void testEscapeWhitespace() throws IOException{
    BalancingStreamTokenizer tok = make("aBc",'a');
    tok.defaultThreeQuoteDollarCurlySetup();
    tok.whitespace(97);
    String s = tok.getNextToken();
    assertEquals("aBc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
  }
  
  public void testNonMatchingQuotes() throws IOException{
    BalancingStreamTokenizer tok = make("'abc}");
    tok.defaultThreeQuoteDollarCurlySetup();
    String s = tok.getNextToken();
    assertEquals("'abc}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
  }
  
  public void testDollarNestedQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc ${def ghi 'abc'} 123\n456 ${abc def \"xxx '111' yyy\"} 789");
    tok.defaultThreeQuoteDollarCurlySetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def ghi 'abc'}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testDollarNestedQuotedKeywords() throws IOException {
    BalancingStreamTokenizer tok = make("abc=${def;ghi='abc'};123\n456 ${abc def \"xxx '111' yyy\"} 789");
    tok.defaultThreeQuoteDollarCurlySetup();
    tok.addKeyword(";");
    tok.addKeyword("=");
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("=", s);
    assertEquals(BalancingStreamTokenizer.Token.KEYWORD, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def;ghi='abc'}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(";", s);
    assertEquals(BalancingStreamTokenizer.Token.KEYWORD, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
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
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("=", s);
    assertEquals(BalancingStreamTokenizer.Token.KEYWORD, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def;ghi='abc'}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(";", s);
    assertEquals(BalancingStreamTokenizer.Token.KEYWORD, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123\n456 ", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(" 789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  /* Tests for making a keyword */
  public void testAddKeywordWhitespace() {
    BalancingStreamTokenizer tok = make("abc def\\ ghi 123\n456");
    tok.defaultWhitespaceSetup();
    try { tok.addKeyword("   key"); } 
    catch (Exception e) { return; }
    fail("Expected Exception");
  }
  
  public void testAddKeywordQuotes() {
    BalancingStreamTokenizer tok = make("abc def\\ ghi `key 123\n456");
    tok.defaultThreeQuoteDollarCurlySetup();
    try { tok.addKeyword("`"); } 
    catch (Exception e) { return; }
    fail("Expected Exception");
  }
  
  /* testing whitespace function */
  public void testWhitespaceFunction() throws IOException {
    BalancingStreamTokenizer tok = make("Hello@World");
    tok.defaultThreeQuoteDollarCurlySetup();
    tok.whitespace(64);
    String s = tok.getNextToken();
    assertEquals("Hello",s);
    s = tok.getNextToken();
    assertEquals("World",s);
  }
  
  /* testing wordChars function */
  public void testWordCharsFunction() throws IOException {
    BalancingStreamTokenizer tok = make("Hel lo\tWor ld");
    tok.defaultThreeQuoteDollarCurlySetup();
    tok.wordChars(32);
    String s = tok.getNextToken();
    assertEquals("Hel lo",s);
    s = tok.getNextToken();
    assertEquals("Wor ld",s);
  }
  
  /* Testing adding new quotes */
  public void testAddQuotesWhitespace() {
    BalancingStreamTokenizer tok = make("abc def\\ ghi 123\n456");
    tok.defaultThreeQuoteDollarCurlySetup();
    try { tok.addQuotes(" ","#"); } 
    catch (Exception e) { return; }
    fail("Expected Exception");
  }
  
  public void testAddQuotesRepeat() {
    BalancingStreamTokenizer tok = make("abc def\\ ghi 123\n456");
    tok.defaultThreeQuoteDollarCurlySetup();
    try { tok.addQuotes("#","'"); } 
    catch (Exception e) { return; }
    fail("Expected Exception");
  }
  
  public void testAddNewQuotes() throws IOException{
    BalancingStreamTokenizer tok = make("abc ${def ghi 'abc#} 123\n456 ${abc def \"xxx '111# yyy\"} 789");
    tok.defaultThreeQuoteDollarCurlySetup();
    try{
      tok.addQuotes("'","#");
    } catch (Exception e) { }
    
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def ghi 'abc#}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${abc def \"xxx '111# yyy\"}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  /* Escape tests */
  public void testEscapeSimple() throws IOException {
    BalancingStreamTokenizer tok = make("abc def\\ ghi 123\n456", '\\');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("def ghi", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapeSimple2() throws IOException {
    BalancingStreamTokenizer tok = make("abc def\\\\ ghi 123\n456", '\\');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("def\\", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("ghi", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
    
  public void testEscapeSimple3() throws IOException {
    BalancingStreamTokenizer tok = make("foo \\ bar", '\\');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("foo", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(" bar", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
    
  public void testEscapeSimple4() throws IOException {
    BalancingStreamTokenizer tok = make("foo\\bar", '\\');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("foo\\bar", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
 
  public void testEscapeAtFrontBeforeWS() throws IOException {
    BalancingStreamTokenizer tok = make("\\ abc def", '\\');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(" abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("def", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  public void testEscapeAtFrontBeforeQuote() throws IOException {
    BalancingStreamTokenizer tok = make("\\${foobar} abc", '\\');
    tok.defaultThreeQuoteDollarCurlySetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${foobar}", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapeAtFrontBeforeKeyword() throws IOException {
    BalancingStreamTokenizer tok = make("\\=foobar abc", '\\');
    tok.defaultWhitespaceSetup();
    tok.addKeyword("=");
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("=foobar", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapeAtFrontBeforeNormal() throws IOException {
    BalancingStreamTokenizer tok = make("\\abc def", '\\');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\\abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("def", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapeQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc \\\"def ghi\\\" 123\n456 'abc def\\' xxx' 789", '\\');
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.err.println("testEscapeQuoted: " + s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\"def", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("ghi\"", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc def' xxx'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapeNestedQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc \\\"def ghi 'abc'\\\" 123\n456 'abc def \\\"xxx \\'111\\' yyy\\\"' 789", '\\');
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.err.println("testEscapeNestedQuoted: " + s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\"def", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("ghi", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\"", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc def \"xxx '111' yyy\"'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapeQuotedNW() throws IOException {
    BalancingStreamTokenizer tok = make("abc\\\"def ghi\\\"123\n456'abc def'789", '\\');
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc\"def", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("ghi\"123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc def'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapeNestedQuotedNW() throws IOException {
    BalancingStreamTokenizer tok = make("abc\\\"def ghi 'abc'\\\"123\n456'abc def \\\\\"xxx \\\\'111\\\\' yyy\\\\\"'789", '\\');
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc\"def", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("ghi", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\"123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc def \\\"xxx \\'111\\' yyy\\\"'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapeDollarQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc \\${def ghi} 123\n\\\\${xxx yyy}456 `abc def` 789", '\\');
    tok.defaultThreeQuoteDollarCurlySetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("ghi}", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\\", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${xxx yyy}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("`abc def`", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapeDollarNestedQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc \\${def ghi 'abc'} 123\n456 \\\\${abc def \"xxx '111' yyy\"} 789", '\\');
    tok.defaultThreeQuoteDollarCurlySetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("ghi", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("}", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\\", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapeDollarNestedQuotedKeywords() throws IOException {
    BalancingStreamTokenizer tok = make("abc\\=${def;ghi='abc'}\\;123\n456 ${abc def \"xxx '111' yyy\"} 789", '\\');
    tok.defaultThreeQuoteDollarCurlySetup();
    tok.addKeyword(";");
    tok.addKeyword("=");
    String s = tok.getNextToken();
    // System.err.println("testEscapeDollarNestedQuotedKeywords: " + s);
    assertEquals("abc=", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def;ghi='abc'}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(";123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
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
    // System.err.println(s);
    assertEquals("abc\\=", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def;ghi='abc'}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(";", s);
    assertEquals(BalancingStreamTokenizer.Token.KEYWORD, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123\n456 ", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(" 789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }

  public void testEscapeDollarNestedQuotedKeywordsWithEscapeWSSignificant2() throws IOException {
    BalancingStreamTokenizer tok = make("abc\\=${def;ghi='abc'};123\n456 ${abc def \"xxx '111' yyy\"} 789",'\\');
    tok.wordRange(0,255);
    tok.addQuotes("\"", "\"");
    tok.addQuotes("'", "'");
    tok.addQuotes("`", "`");
    tok.addQuotes("${", "}");
    tok.addKeyword(";");
    tok.addKeyword("\\=");
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\\=", s);
    assertEquals(BalancingStreamTokenizer.Token.KEYWORD, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def;ghi='abc'}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(";", s);
    assertEquals(BalancingStreamTokenizer.Token.KEYWORD, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123\n456 ", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(" 789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
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
    // System.err.println(s);
    assertEquals("abc=", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def;ghi='abc'}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\\", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(";", s);
    assertEquals(BalancingStreamTokenizer.Token.KEYWORD, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123\n456 ", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(" 789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  /* Escape tests with a different character: |*/
  public void testEscapePipeSimple() throws IOException {
    BalancingStreamTokenizer tok = make("abc def| ghi 123\n456", '|');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    s = tok.getNextToken();
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    // System.err.println(s);
    assertEquals("def ghi", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapePipeSimple2() throws IOException {
    BalancingStreamTokenizer tok = make("abc def|| ghi 123\n456", '|');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("def|", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("ghi", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
    
  public void testEscapePipeSimple3() throws IOException {
    BalancingStreamTokenizer tok = make("foo | bar", '|');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("foo", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(" bar", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
    
  public void testEscapePipeSimple4() throws IOException {
    BalancingStreamTokenizer tok = make("foo|bar", '|');
    tok.defaultWhitespaceSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("foo|bar", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapePipeQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc |\"def ghi|\" 123\n456 'abc def|' xxx' 789", '|');
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\"def", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("ghi\"", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc def' xxx'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapePipeNestedQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc |\"def ghi 'abc'|\" 123\n456 'abc def |\"xxx |'111|' yyy|\"' 789", '|');
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\"def", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("ghi", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\"", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc def \"xxx '111' yyy\"'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapePipeQuotedNW() throws IOException {
    BalancingStreamTokenizer tok = make("abc|\"def ghi|\"123\n456'abc def'789", '|');
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc\"def", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("ghi\"123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc def'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapePipeNestedQuotedNW() throws IOException {
    BalancingStreamTokenizer tok = make("abc|\"def ghi 'abc'|\"123\n456'abc def ||\"xxx ||'111||' yyy||\"'789", '|');
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc\"def", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("ghi", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\"123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc def |\"xxx |'111|' yyy|\"'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapePipeDollarQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc |${def ghi} 123\n||${xxx yyy}456 `abc def` 789", '|');
    tok.defaultThreeQuoteDollarCurlySetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("ghi}", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("|", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${xxx yyy}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("`abc def`", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapePipeDollarNestedQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("abc |${def ghi 'abc'} 123\n456 ||${abc def \"xxx '111' yyy\"} 789", '|');
    tok.defaultThreeQuoteDollarCurlySetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("ghi", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'abc'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("}", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("|", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapePipeDollarNestedQuotedKeywords() throws IOException {
    BalancingStreamTokenizer tok = make("abc|=${def;ghi='abc'}|;123\n456 ${abc def \"xxx '111' yyy\"} 789", '|');
    tok.defaultThreeQuoteDollarCurlySetup();
    tok.addKeyword(";");
    tok.addKeyword("=");
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc=", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def;ghi='abc'}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(";123", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("456", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
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
    // System.err.println(s);
    assertEquals("abc|=", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def;ghi='abc'}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(";", s);
    assertEquals(BalancingStreamTokenizer.Token.KEYWORD, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123\n456 ", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(" 789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }

  public void testEscapePipeDollarNestedQuotedKeywordsWithEscapeWSSignificant2() throws IOException {
    BalancingStreamTokenizer tok = make("abc|=${def;ghi='abc'};123\n456 ${abc def \"xxx '111' yyy\"} 789",'|');
    tok.wordRange(0,255);
    tok.addQuotes("\"", "\"");
    tok.addQuotes("'", "'");
    tok.addQuotes("`", "`");
    tok.addQuotes("${", "}");
    tok.addKeyword(";");
    tok.addKeyword("|=");
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("|=", s);
    assertEquals(BalancingStreamTokenizer.Token.KEYWORD, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def;ghi='abc'}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(";", s);
    assertEquals(BalancingStreamTokenizer.Token.KEYWORD, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123\n456 ", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(" 789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
  
  public void testEscapePipeDollarNestedQuotedKeywordsWithEscape2WSSignificant() throws IOException {
    BalancingStreamTokenizer tok = make("abc|=||${def;ghi='abc'};123\n456 ${abc def \"xxx '111' yyy\"} 789",'|');
    tok.wordRange(0,255);
    tok.addQuotes("\"", "\"");
    tok.addQuotes("'", "'");
    tok.addQuotes("`", "`");
    tok.addQuotes("${", "}");
    tok.addKeyword(";");
    tok.addKeyword("=|");
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc=|", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def;ghi='abc'}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(";", s);
    assertEquals(BalancingStreamTokenizer.Token.KEYWORD, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123\n456 ", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(" 789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }

  public void testEscapePipeDollarNestedQuotedKeywordsWithEscape2WSSignificant2() throws IOException {
    BalancingStreamTokenizer tok = make("abc=||${def;ghi='abc'};123\n456 ${abc def \"xxx '111' yyy\"} 789",'|');
    tok.wordRange(0,255);
    tok.addQuotes("\"", "\"");
    tok.addQuotes("'", "'");
    tok.addQuotes("`", "`");
    tok.addQuotes("${", "}");
    tok.addKeyword(";");
    tok.addKeyword("=|");
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("=|", s);
    assertEquals(BalancingStreamTokenizer.Token.KEYWORD, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def;ghi='abc'}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(";", s);
    assertEquals(BalancingStreamTokenizer.Token.KEYWORD, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123\n456 ", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(" 789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
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
    // System.err.println(s);
    assertEquals("abc=", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${def;ghi='abc'}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("|", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(";", s);
    assertEquals(BalancingStreamTokenizer.Token.KEYWORD, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("123\n456 ", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${abc def \"xxx '111' yyy\"}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(" 789", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }

  public void testDollarEscapeQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("${user.dir}", '$');
    tok.wordRange(0,255);
    tok.addQuotes("${", "}");
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${user.dir}", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }

  public void testQuoteEscapeQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("'test'", '\'');
    tok.defaultTwoQuoteSetup();
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("'test'", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }

  public void testDollarEscapedQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("$${user.dir}", '$');
    tok.wordRange(0,255);
    tok.addQuotes("${", "}");
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("${user.dir}", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }

  public void testDollarEscapedQuoted2() throws IOException {
    BalancingStreamTokenizer tok = make("abc $${xxx}xyz", '$');
    tok.wordRange(0,255);
    tok.addQuotes("${", "}");
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("abc ${xxx}xyz", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }

  // I don't know how to make this one work with the others
//  public void testQuoteEscapedQuoted() throws IOException {
//    BalancingStreamTokenizer tok = make("''test''", '\'');
//    tok.defaultTwoQuoteSetup();
//    String s = tok.getNextToken();
//    // System.err.println(s);
//    assertEquals("'test'", s);
//    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
//    s = tok.getNextToken();
//    // System.err.println(s);
//    assertEquals(null, s);
//    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
//  }

  public void testEscapedQuoted() throws IOException {
    BalancingStreamTokenizer tok = make("name=\"$\"text$\"\"", '$');
    tok.wordRange(0,255);
    tok.whitespaceRange(0,32); 
    tok.addQuotes("\"", "\"");
    tok.addQuotes("${", "}");
    tok.addKeyword(";");
    tok.addKeyword("=");
    String s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("name", s);
    assertEquals(BalancingStreamTokenizer.Token.NORMAL, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("=", s);
    assertEquals(BalancingStreamTokenizer.Token.KEYWORD, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals("\"\"text\"\"", s);
    assertEquals(BalancingStreamTokenizer.Token.QUOTED, tok.token());
    s = tok.getNextToken();
    // System.err.println(s);
    assertEquals(null, s);
    assertEquals(BalancingStreamTokenizer.Token.END, tok.token());
  }
}
