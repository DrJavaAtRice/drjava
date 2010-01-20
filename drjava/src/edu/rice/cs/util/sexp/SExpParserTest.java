/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.util.sexp;

import edu.rice.cs.drjava.DrJavaTestCase;

import java.io.*;
import java.util.List;

/** A JUnit test case class. Every method starting with the word "test" will be called when running
 *  the test with JUnit.
 */
public class SExpParserTest extends DrJavaTestCase {
  
  /** Creates a temporary file and writes the given string to that file
   * @param fname the name of the file to create
   * @param text the text to write to the file
   * @return the File that was created
   */
  private File _fillTempFile(String fname, String text) {
    File f = null;
    try {
      f = File.createTempFile(fname, null).getCanonicalFile();
      FileWriter fw = new FileWriter(f);
      fw.write(text, 0, text.length());
      fw.close();
    }
    catch (IOException e) {
      throw new RuntimeException("IOException thrown while writing to temp file");
    }
    return f;
  }
  
  /** There are three ways to input the data to a parse.
   * this tests to make sure they all three return the same
   * thing.
   */
  public void testDifferentInputs() throws SExpParseException, IOException{
    String text = "()";
    File f = _fillTempFile("temp1",text);
    char[] ca = new char[text.length()];
    text.getChars(0, text.length(), ca, 0);
    Reader r = new CharArrayReader(ca);
    
    SExp sa1 = SExpParser.parse(text).get(0);
    SExp sa2 = SExpParser.parse(f).get(0);
    SExp sa3 = SExpParser.parse(r).get(0);
    
    SExp ans = Empty.ONLY;
    
    assertEquals("the 1st parse wasn't right", ans, sa1);
    assertEquals("the 2nd parse wasn't right", ans, sa2);
    assertEquals("the 3rd parse wasn't right", ans, sa3);
  }
  
  /** Tests to make sure that multiple top-level s-exps 
   * are parsed separately and in tact
   */
  public void testParseMultiple() throws SExpParseException{
    String text = "(abcdefg)(hijklmnop)";
    List<? extends SExp> exps = SExpParser.parse(text);
    SExp exp1 = exps.get(0);
    SExp exp2 = exps.get(1);
    
    
    // use a few visitors to test the instances
    final SExpVisitor<String> innerVisitor = new SExpVisitor<String>() {
      private String _failMe(String who) {
        fail("The inside was " +  who  + " but should have been text");
        return "";
      }
      public String forEmpty(Empty e){ return _failMe("an empty list"); }
      public String forCons(Cons c){ return _failMe("an empty list"); }
      public String forBoolAtom(BoolAtom b){ return _failMe("a boolean"); }
      public String forNumberAtom(NumberAtom n) { return _failMe("a number"); }
      public String forTextAtom(TextAtom t) { return t.getText(); }
    };
    
    final SExpVisitor<String> outerVisitor = new SExpVisitor<String>() {
      private String _failMe(String who) {
        fail("The top-level was " +  who  + " but should have been a cons");
        return "";
      }
      public String forEmpty(Empty e){ return _failMe("an empty list"); }
      public String forCons(Cons c){ return c.getFirst().accept(innerVisitor); }
      public String forBoolAtom(BoolAtom b){ return _failMe("a boolean"); }
      public String forNumberAtom(NumberAtom n) { return _failMe("a number"); }
      public String forTextAtom(TextAtom t) { return _failMe("text"); }
    };
    
    assertEquals("wrong text in 1st s-expression", "abcdefg",  exp1.accept(outerVisitor));
    assertEquals("wrong text in 2nd s-expression", "hijklmnop",exp2.accept(outerVisitor));
  }
  
  public void testTopLevel() throws SExpParseException {
    // Test an illegal top-level s-exp
    String text = "true";
    try {
      SExpParser.parse(text).get(0);
      fail("Didn't throw a parse exception");
    }catch(SExpParseException e) {
      assertEquals("Incorrect exception message", 
                   "A top-level s-expression must be a list. " + 
                   "Invalid start of list: true",
                   e.getMessage());
    }
    text = "123 ((help) me)";
    try {
      SExpParser.parse(text).get(0);
      fail("Didn't throw a parse exception");
    }catch(SExpParseException e) {
      assertEquals("Incorrect exception message", 
                   "A top-level s-expression must be a list. " + 
                   "Invalid start of list: 123",
                   e.getMessage());
    }
    text = "[help me]"; // right now, I haven't allowed other brace types
    try {
      SExpParser.parse(text).get(0);
      fail("Didn't throw a parse exception");
    }catch(SExpParseException e) {
      assertEquals("Incorrect exception message", 
                   "A top-level s-expression must be a list. " + 
                   "Invalid start of list: [help",
                   e.getMessage());
    }
  }
  
  public void testInvalidLowerLevel() {
    
    String text = "(abcdefg";
    try {
      SExpParser.parse(text).get(0);
      fail("Didn't throw a parse exception");
    }catch(SExpParseException e) {
      assertEquals("Incorrect exception message", 
                   "Unexpected <EOF> at line 1",
                   e.getMessage());
    }
    
    text = "(ab\ncdefg";
    try {
      SExpParser.parse(text).get(0);
      fail("Didn't throw a parse exception");
    }catch(SExpParseException e) {
      assertEquals("Incorrect exception message", 
                   "Unexpected <EOF> at line 2",
                   e.getMessage());
    }
    
    text = "(ab\ncdefg))";
    try {
      SExpParser.parse(text).get(0);
      fail("Didn't throw a parse exception");
    }catch(SExpParseException e) {
      assertEquals("Incorrect exception message", 
                   "A top-level s-expression must be a list. " + 
                   "Invalid start of list: )",
                   e.getMessage());
    }
    
    text = "(\")";  //  (") <-- unclosed string
    try {
      SExpParser.parse(text).get(0);
      fail("Didn't throw a parse exception");
    }catch(SExpParseException e) {
      assertEquals("Incorrect exception message", 
                   "Unexpected <EOF> at line 1",
                   e.getMessage());
    }
    
    
    text = "(;)";  // <-- last ) is commented out
    try {
      SExpParser.parse(text).get(0);
      fail("Didn't throw a parse exception");
    }catch(SExpParseException e) {
      assertEquals("Incorrect exception message", 
                   "Unexpected <EOF> at line 1",
                   e.getMessage());
    }
  }
  
  public void testCorrectParse() throws SExpParseException {
    String n = "\n";
    String text = 
      "; this is a comment line                      " + n +
      "; this is another comment line                " + n +
      "(Source                                       " + n +
      "  (/sexp/Atom.java)                           " + n +
      "  (/sexp/Cons.java)                           " + n +
      "  (/sexp/Empty.java)                          " + n +
      "  (/sexp/Lexer.java)                          " + n +
      "  (/sexp/SExp.java)                           " + n +
      "  (/sexp/SExpParser.java)                     " + n +
      "  (/sexp/SExpVisitor.java)                    " + n +
      "  (/sexp/Tokens.java)                         " + n +
      ")                                             " + n +
      "; This is the build directory.  Absolute path " + n +
      "(BuildDir \"/home/javaplt/drjava/built\")     " + n +
      "(MainFile \"/sexp/SExpParser.java\")          " + n +
      "(Included                                     " + n +
      ")";
    
    List<SEList> res = SExpParser.parse(text);
    assertEquals("Should have four trees in forest", 4, res.size());
  }
}
