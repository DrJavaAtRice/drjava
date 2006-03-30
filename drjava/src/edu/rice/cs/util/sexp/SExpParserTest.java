/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.sexp;

import edu.rice.cs.drjava.DrJavaTestCase;

import java.io.*;
import java.util.List;

/** A JUnit test case class. Every method starting with the word "test" will be called when running
 *  the test with JUnit.
 */
public class SExpParserTest extends DrJavaTestCase {
  
  /**
   * Creates a temporary file and writes the given string to that file
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
  
  /**
   * There are three ways to input the data to a parse.
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
  
  /**
   * Tests to make sure that multiple top-level s-exps 
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
        fail("The inside was "+ who +" but should have been text");
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
        fail("The top-level was "+ who +" but should have been a cons");
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
      SExp exp = SExpParser.parse(text).get(0);
      fail("Didn't throw a parse exception");
    }catch(SExpParseException e) {
      assertEquals("Incorrect exception message", 
                   "A top-level s-expression must be a list. "+
                   "Invalid start of list: true",
                   e.getMessage());
    }
    text = "123 ((help) me)";
    try {
      SExp exp = SExpParser.parse(text).get(0);
      fail("Didn't throw a parse exception");
    }catch(SExpParseException e) {
      assertEquals("Incorrect exception message", 
                   "A top-level s-expression must be a list. "+
                   "Invalid start of list: 123",
                   e.getMessage());
    }
    text = "[help me]"; // right now, I haven't allowed other brace types
    try {
      SExp exp = SExpParser.parse(text).get(0);
      fail("Didn't throw a parse exception");
    }catch(SExpParseException e) {
      assertEquals("Incorrect exception message", 
                   "A top-level s-expression must be a list. "+
                   "Invalid start of list: [help",
                   e.getMessage());
    }
  }
  
  public void testInvalidLowerLevel() {
    
    String text = "(abcdefg";
    try {
      SExp exp = SExpParser.parse(text).get(0);
      fail("Didn't throw a parse exception");
    }catch(SExpParseException e) {
      assertEquals("Incorrect exception message", 
                   "Unexpected <EOF> at line 1",
                   e.getMessage());
    }
    
    text = "(ab\ncdefg";
    try {
      SExp exp = SExpParser.parse(text).get(0);
      fail("Didn't throw a parse exception");
    }catch(SExpParseException e) {
      assertEquals("Incorrect exception message", 
                   "Unexpected <EOF> at line 2",
                   e.getMessage());
    }
    
    text = "(ab\ncdefg))";
    try {
      SExp exp = SExpParser.parse(text).get(0);
      fail("Didn't throw a parse exception");
    }catch(SExpParseException e) {
      assertEquals("Incorrect exception message", 
                   "A top-level s-expression must be a list. "+
                   "Invalid start of list: )",
                   e.getMessage());
    }
    
    text = "(\")";  //  (") <-- unclosed string
    try {
      SExp exp = SExpParser.parse(text).get(0);
      fail("Didn't throw a parse exception");
    }catch(SExpParseException e) {
      assertEquals("Incorrect exception message", 
                   "Unexpected <EOF> at line 1",
                   e.getMessage());
    }
    
    
    text = "(;)";  // <-- last ) is commented out
    try {
      SExp exp = SExpParser.parse(text).get(0);
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
