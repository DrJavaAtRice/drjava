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

import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This parser is not meant to be instantiated.  It has
 * static methods that do all the work for you.  These
 * parse methods take in the data that is to be parsed
 * and simply returns an s-expression abstract syntax.
 * @author Jonathan Lugo, PLT Group
 */
public class SExpParser {
  
  public static List<SEList> parse(File f) throws SExpParseException, IOException{
    return parse(new FileReader(f));
  }
  
  public static List<SEList> parse(String s) throws SExpParseException {
    return parse(new StringReader(s));
  }
  
  public static List<SEList> parse(Reader r) throws SExpParseException {
    try {
      return new ParseHelper(r).parseMultiple();
    }
    catch(LexingException e) {
      throw new SExpParseException(e.getMessage());
    }
    catch(PrivateParseException e) {
      throw new SExpParseException(e.getMessage());
    }
  }
  
  /**
   * A new helper is instantiated for each time
   * the user wants to parse data.  This is not
   * reused.  The instances of the ParseHelpers are
   * handled solely in the outer class SExpParser.
   */
  private static class ParseHelper {
    
    private Lexer _lex;
    
    public ParseHelper(Reader r) {
      _lex = new Lexer(r);
    }
    
    /**
     * Parse a forest of top-level s-expressions
     * @param a list of top-level s-expressions
     */
    public List<SEList> parseMultiple() {
      ArrayList<SEList> l = new ArrayList<SEList>();
      SEList exp;
      while ( (exp = parseTopLevelExp()) != null) {
        l.add(exp);
      }
      return l;
    }
    
    /**
     * A top-level s-expression is simply a non-empty list.  Our s-expression files
     * can be a forest of several trees, but the Atomic values are not allowed
     * at the top level, only lists.
     * @return the top-level list s-expression
     */
    public SEList parseTopLevelExp() {
      SExpToken t = _lex.readToken();
      if (t == LeftParenToken.ONLY) {
        return parseList();
      }
      else if (t == null) {
        return null;
      }
      else {
        throw new PrivateParseException("A top-level s-expression must be a list. "+
                                        "Invalid start of list: " + t);
      }
    }
    
    /**
     * Parses the next s-expression in the lexer's buffer.
     * This may be either a cons or an atom
     * @return the next s-expression in the read buffer.
     */
    public SExp parseExp() {
      SExpToken t = _lex.readToken();
      assertNotEOF(t);
      if (t == LeftParenToken.ONLY) {
        return parseList();
      }
      else {
        return parseAtom(t);
      }
    }
    
    /**
     * The left paren has already been read. This starts
     * building up the recursive list structure
     * @return the parsed recursive s-expression list
     */
    private SEList parseList() {
      LinkedList<SExp> list = new LinkedList<SExp>();
      SExpToken t = _lex.peek();
      assertNotEOF(t);
      
      while (t != RightParenToken.ONLY) {
        list.addFirst(parseExp());
        t = _lex.peek();
      }
      
      // t has to be a RightParenToken at this point.
      // simply eat the token
      _lex.readToken();
      
      // Compile the cons structure from the list of exps
      SEList cons = Empty.ONLY;
      for (SExp exp : list) {
        cons = new Cons(exp, cons);
      }
      return cons;
    }
    
    /**
     * Parses an atom.  The token was already read and
     * found not to start a list, this method interprets
     * what is given.  This method chooses which type of
     * atom the token represents and creates the atom.
     * @param t the token to interpret
     * @return the correct corresponding atom
     */
    private Atom parseAtom(SExpToken t) {
      if (t instanceof BooleanToken) {
        if (((BooleanToken)t).getValue())
          return BoolAtom.TRUE;
        else 
          return BoolAtom.FALSE;
      }
      else if (t instanceof NumberToken) {
        return new NumberAtom(((NumberToken)t).getValue());
      }
      else if (t instanceof QuotedTextToken) {
        return new QuotedTextAtom(t.getText());
      }
      else {
        return new TextAtom(t.getText());
      }
    }
    
    /**
     * Throws the EOF exception if the given token is the end of file
     * @param t the token to check
     */
    private void assertNotEOF(SExpToken t) {
      if (t == null) {
        throw new PrivateParseException("Unexpected <EOF> at line " + _lex.lineno());
      }
    }
  }
  
  /**
   * This runtime exception makes it easier to write the parser since
   * the methods of the helper class won't need to constantly declare
   * the SExpParseException to be thrown.
   */
  private static class PrivateParseException extends RuntimeException {
    /**
     * Creates a runtime exception with the message that is desired for
     * the eventual checked exception
     * @param the message to display
     */
    public PrivateParseException(String msg) { super(msg); }
  }
}