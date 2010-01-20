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

import java.util.*;
import java.io.*;

class Lexer extends StreamTokenizer {
  
  public HashMap<String,Tokens.SExpToken> wordTable = new HashMap<String,Tokens.SExpToken>();
  
  private Tokens.SExpToken buffer;
  
  public Lexer(File file) throws FileNotFoundException{
    this(new BufferedReader(new FileReader(file)));
  }
  
  public Lexer(Reader reader) {
    super(new BufferedReader(reader));
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
  
  /** Performs a nextToken() operation from StreamTokenizer except for throwing an unchecked LexingException instead of
    * a checked IOException */
  private int getToken() {
    try {
      int tokenType = nextToken();
      return tokenType;
    } catch(IOException e) {
      throw new LexingException("Unable to read the data from the given input");
    }
  }
  
  /** Returns the next Tokens.SExpToken without consuming it */
  public Tokens.SExpToken peek() {
    if (buffer == null) buffer = readToken();
    return buffer;
  }
  
  /** Reads the next Tokens.SExpToken from the input stream and consumes it.
    * @return the Tokens.SExpToken object representing this Tokens.SExpToken */
  public Tokens.SExpToken readToken() {
    
    if (buffer != null) {
      Tokens.SExpToken token = buffer;
      buffer = null;          // clear buffer
      return token;
    }
    
    int tokenType = getToken();
    // Process the Tokens.SExpToken returned by StreamTokenizer
    switch (tokenType) {
      case TT_NUMBER: 
        return new Tokens.NumberToken(nval);
        
      case TT_WORD:
        String s = sval.toLowerCase();
        Tokens.SExpToken regToken = wordTable.get(s);
        if (regToken == null) return new Tokens.WordToken(sval);
        
        return regToken;
        
      case TT_EOF: return null;
      case '(': return Tokens.LeftParenToken.ONLY;
      case ')': return Tokens.RightParenToken.ONLY;
      case '"': return new Tokens.QuotedTextToken(sval);
      case '\\': 
//        int t = getToken();
//        if (t == '"') {
//          return new Tokens.WordToken("\"");
//        }
//        else if (t == '\\') {
//          return new Tokens.WordToken("\\");
//        }
//        else if (t == ' ') {
//          return new Tokens.WordToken(" ");
//        }
//        else if (t == 'n') {
//          return new Tokens.WordToken("\n");
//        }
//        else if (t == 't') {
//          return new Tokens.WordToken("\t");
//        }
//        else {
//          pushBack();
        return Tokens.BackSlashToken.ONLY;
//          throw new SExpParseException("Invalid escape sequence: \\" + (char)t);
        
      default:
        return new Tokens.WordToken("" + (char)tokenType);
    }
  }
  
  
  /** Initialize the word table used by the lexer to classify Tokens */
  private void initWordTable() {
    // initialize wordTable
    wordTable.put("true", Tokens.BooleanToken.TRUE);
    wordTable.put("false", Tokens.BooleanToken.FALSE);
  }
}