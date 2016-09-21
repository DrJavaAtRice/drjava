/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2015, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.util.sexp;

/**
 * There are different kinds of atoms: text, boolean, number.
 * Therefore they should have a visitor interface to support 
 * algorithms on them.
 */
public interface Atom extends SExp {
  /** Visitor hook for the different kinds of Atom
    * @param v the visitor
    * @return result of the given algorithm
    */
  public <Ret> Ret accept(SExpVisitor<Ret> v);
  
  /* Static inner classes can be public. */
  public class Text implements Atom {
    protected String _text;
    
    public Text(String text) { _text = text; }
    
    public String getText() { return _text; }
    
    /** Visitor hook for Atom.Text
      * @param v the visitor
      * @return result of the given algorithm
      */
    public <Ret> Ret accept(SExpVisitor<Ret> v){
      return v.forTextAtom(this);
    }
    
    /** If the given text was a quoted string, the text returned
      * excludes the quotes around the string.
      * @return the text that went into making this atom.
      */
    public String toString() { return _text; }  
  }
  
  /** This type of text atom is similar to its super class except (i) its string representation includes the surrounding 
    * quotes and (ii) instances of characters: \ " etc are turned into their corresponding escape character sequences.
    */
  public class QuotedText extends Text {
    
    public QuotedText(String text) { super(text); }
    
    public String toString() { 
      return edu.rice.cs.util.StringOps.convertToLiteral(_text);
    }
  }
  
  public class Bool implements Atom {
    public static final Bool TRUE = new Bool(true);
    public static final Bool FALSE = new Bool(false);
    
    private boolean _bool;
    private Bool(boolean bool) { _bool = bool; }
    
    /** @return which type of BoolAtom this is */
    public boolean getValue() { return _bool; }
    
    /** Visitor hook for the BoolAtom
      * @param v the visitor
      * @return result of the given algorithm
      */
    public <Ret> Ret accept(SExpVisitor<Ret> v){ return v.forBoolAtom(this); }
    
    public String toString() { return "" + _bool; }
  }
  
  public class Number implements Atom {
    private double _num;
    private boolean _hasDecimals;
    public Number(int num){ 
      _num = num;
      _hasDecimals = false;
    }
    public Number(double num){
      _num = num;
      _hasDecimals = (num % 1 < 1e-12);
    }
    public boolean hasDecimals() { return _hasDecimals; }
    public int intValue() { return (int)_num; }
    public double doubleValue() { return _num; }
    
    /** Visitor hook for the Atom.Number
      * @param v the visitor
      * @return result of the given algorithm
      */
    public <Ret> Ret accept(SExpVisitor<Ret> v) { return v.forNumberAtom(this); }
    
    public String toString() { 
      if (_hasDecimals) return "" + doubleValue();
      else return "" + intValue();
    }
  }  
}