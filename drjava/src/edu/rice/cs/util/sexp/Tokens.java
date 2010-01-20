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

/** A common namespace for the token classes. */
public interface Tokens {
  
  /** These tokens are designed to be compared using the == operator with (, ), ", and \.  Otherwise, the tokens may be
    * compared using the .equals() method.  This class is concrete only for testing purposes.
    */
  /* abstract */ class SExpToken {
    protected String _rep;
    
    /** @param rep The string representation of this token */
    public SExpToken(String rep) { _rep = rep.intern(); }  // intern() supports use of == for equality testing
    
    /** @return the string representation of this token */
    public String getText() { return _rep; }
    
    public boolean equals(Object o) {
      return (o != null && o.getClass() == getClass() && ((SExpToken)o)._rep == _rep);
    }
    
    public int hashCode() { return _rep.hashCode(); }
    
    public String toString() { return _rep; }
  }
  
  ////////////// Symbol Tokens ///////////////////
  
  class LeftParenToken extends SExpToken {
    public static final LeftParenToken ONLY = new LeftParenToken();
    private LeftParenToken(){ super("("); }
  }
  
  class RightParenToken extends SExpToken {
    public static final RightParenToken ONLY = new RightParenToken();
    private RightParenToken(){ super(")"); }
  }
  
  class BackSlashToken extends SExpToken {
    public static final BackSlashToken ONLY = new BackSlashToken();
    private BackSlashToken(){ super("\\"); }
  }
  
  ////////////// General Tokens //////////////////
  
  /** Words include any text (including symbols) that is not a number, a backslash, or a quote character. */
  class WordToken extends SExpToken { public WordToken(String word) { super(word); } }
  
  /** This token is handled as a unit by the lexer. Any text between the pair of quotes is given
    * in this QuotedTextToken.
    */
  class QuotedTextToken extends SExpToken {
    //  private String _txt;
    public QuotedTextToken(String txt) { super(txt); }
    public String getFullText() { return "\"" + _rep + "\""; }
  }
  
  /** Words include any text (including symbols) that is not a number, a backslash, or a quote character. */
  class BooleanToken extends SExpToken {
    public static final BooleanToken TRUE = new BooleanToken(true);
    public static final BooleanToken FALSE = new BooleanToken(false);
    
    private boolean _bool;
    private BooleanToken(boolean bool){
      super("" + bool);
      _bool = bool;
    }
    public boolean getValue() { return _bool; }
  }
  
  /** Numbers are string s of only digits (0-9)
   */
  class NumberToken extends SExpToken {
    private double _num;
    public NumberToken(double num){
      // If it is a whole number, don't include
      // the decimal in the string representation
      super((num % 1 == 0) ? "" + (int)num : "" + num);
      _num = num;
    }
    public double getValue() { return _num; }
  }
  
}
