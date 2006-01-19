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

/** These tokens are designed to be compared using the == operator for (, ), ", and \.  Otherwise,
 *  the tokens may be compared using the .equals() method.  This class is concrete only for testing
 *  purposes.
 */
/* abstract */ class SExpToken {
  protected String _rep;
  
  /** @param rep The string representation of this token */
  public SExpToken(String rep) { _rep = rep; }
  
  /** @return the string representation of this token */
  public String getText() { return _rep; }
  
  public boolean equals(Object o) {
    return (o.getClass() == getClass() && ((SExpToken)o)._rep.equals(_rep));
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
 *  in this QuotedTextToken.
 */
class QuotedTextToken extends SExpToken {
//  private String _txt;
  public QuotedTextToken(String txt) { super(txt); }
  public String getFullText() { return "\"" + _rep + "\""; }
}

/**
 * Words include any text (including symbols) that
 * is not a number, a backslash, or a quote character
 */
class BooleanToken extends SExpToken {
  public static final BooleanToken TRUE = new BooleanToken(true);
  public static final BooleanToken FALSE = new BooleanToken(false);
  
  private boolean _bool;
  private BooleanToken(boolean bool){
    super(""+bool);
    _bool = bool;
  }
  public boolean getValue() { return _bool; }
}

/**
 * Numbers are string s of only digits (0-9)
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