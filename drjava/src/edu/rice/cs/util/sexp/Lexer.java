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
 
import java.util.*;
import java.io.*;

class Lexer extends StreamTokenizer {
  
  public HashMap<String,SExpToken> wordTable = new HashMap<String,SExpToken>();
  
  private SExpToken buffer;
  
  public Lexer(File file) throws FileNotFoundException{
    this(new FileReader(file));
  }
  
  public Lexer(Reader reader) {
    super(reader);
    initLexer();
  }
  
  private void initLexer() {
    
    // configure StreamTokenizer portion of this
    resetSyntax();
    parseNumbers();
    slashSlashComments(true);
    wordChars('!','\'');
    wordChars('*','~');
    quoteChar('"');
    ordinaryChars('(',')');
    whitespaceChars(0,' ');
    commentChar(';');
    
    initWordTable();
    buffer = null;  // buffer initially empty
  }
  
  /** Skips through the input stream until an EOL is encountered */
  public void flush() throws IOException {
    eolIsSignificant(true);
    while (nextToken() != TT_EOL) ; // eat tokens until EOL
    eolIsSignificant(false);
  }
    
  /** Performs a nextToken() operation from StreamTokenizer except
   *  for throwing an unchecked LexingException instead of a checked IOException */
  private int getToken() {
    try {
      int tokenType = nextToken();
      return tokenType;
    } catch(IOException e) {
      throw new LexingException("Unable to read the data from the given input");
    }
  }
  
  /** Returns the next SExpToken without consuming it */
  public SExpToken peek() {
    if (buffer == null) buffer = readToken();
    return buffer;
  }
  
  /** Reads the next SExpToken from the input stream and consumes it;
   *  Returns the SExpToken object representing this SExpToken */
  public SExpToken readToken() {
    
    if (buffer != null) {
      SExpToken token = buffer;
      buffer = null;          // clear buffer
      return token;
    }
    
    int tokenType = getToken();
    // Process the SExpToken returned by StreamTokenizer
    switch (tokenType) {
      case TT_NUMBER: 
        return new NumberToken(nval);
        
      case TT_WORD:
        String s = sval.toLowerCase();
        SExpToken regToken = wordTable.get(s);
        if (regToken == null) return new WordToken(sval);
        
        return regToken;
        
      case TT_EOF: return null;
      case '(': return LeftParenToken.ONLY;
      case ')': return RightParenToken.ONLY;
      case '"': return new QuotedTextToken(sval);
      case '\\': 
//        int t = getToken();
//        if (t == '"') {
//          return new WordToken("\"");
//        }
//        else if (t == '\\') {
//          return new WordToken("\\");
//        }
//        else if (t == ' ') {
//          return new WordToken(" ");
//        }
//        else if (t == 'n') {
//          return new WordToken("\n");
//        }
//        else if (t == 't') {
//          return new WordToken("\t");
//        }
//        else {
//          pushBack();
          return BackSlashToken.ONLY;
//          throw new SExpParseException("Invalid escape sequence: \\" + (char)t);
        
      default:
        return new WordToken("" + (char)tokenType);
    }
  }
  
  
  /** Initialize the word table used by the lexer to classify Tokens */
  private void initWordTable() {
    // initialize wordTable
    wordTable.put("true", BooleanToken.TRUE);
    wordTable.put("false", BooleanToken.FALSE);
  }
}