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

package edu.rice.cs.drjava.config;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.plt.lambda.Lambda2;
import edu.rice.cs.util.Lambda;

import java.util.HashSet;
import java.util.Iterator;

/** Class representing binary operations that can be inserted as variables in external processes.
  * @version $Id$
  */
public class BinaryOpProperty<P,Q,R> extends EagerProperty {
  /** Operation to perform. */
  protected Lambda2<P,Q,R> _op;
  /** Lambda to turn a string into the first operand. */
  protected Lambda<P,String> _parse1;
  /** Lambda to turn a string into the first operand. */
  protected Lambda<Q,String> _parse2;
  /** Lambda to format the result. */
  protected Lambda<String,R> _format;
  
  /** Create an eager property. */
  public BinaryOpProperty(String name,
                          String help,
                          Lambda2<P,Q,R> op,
                          Lambda<P,String> parse1,
                          Lambda<Q,String> parse2,
                          Lambda<String,R> format) {
    super(name, help);
    _op = op;
    _parse1 = parse1;
    _parse2 = parse2;
    _format = format;
    resetAttributes();
  }
  
  /** Update the property so the value is current. */
  public void update() {
    P op1;
    if (_attributes.get("op1")==null) {
      _value = "("+_name+" Error...)";
      return;
    }
    else {
      try {
        op1 = _parse1.apply(_attributes.get("op1"));
      }
      catch(Exception e) {
        _value = "("+_name+" Error...)";
        return;
      }
    }
    Q op2;
    if (_attributes.get("op2")==null) {
      _value = "("+_name+" Error...)";
      return;
    }
    else {
      try {
        op2 = _parse2.apply(_attributes.get("op2"));
      }
      catch(Exception ee) {
        _value = "("+_name+" Error...)";
        return;
      }
    }
    _value = _format.apply(_op.value(op1,op2));
  }
  
  public void resetAttributes() {
    _attributes.clear();
    _attributes.put("op1", null);
    _attributes.put("op2", null);
  }
  
  /** @return true if the specified property is equal to this one. */
  public boolean equals(Object other) {
    if (other == null || other.getClass() != this.getClass()) return false;
    BinaryOpProperty o = (BinaryOpProperty)other;
    return _name.equals(o._name)
      && (_isCurrent == o._isCurrent)
      && _op.equals(o._op)
      && _parse1.equals(o._parse1)
      && _parse2.equals(o._parse2)
      && _format.equals(o._format)
      && _value.equals(o._value);
  }
  
  /** @return the hash code. */
  public int hashCode() {
    int result;
    result = _name.hashCode();
    result = 31 * result + (_op.hashCode());
    result = 31 * result + (_parse1.hashCode());
    result = 31 * result + (_parse2.hashCode());
    result = 31 * result + (_format.hashCode());
    result = 31 * result + (_value.hashCode());
    result = 31 * result + (_isCurrent?1:0);
    return result;
  }

  /** Lambda to parse a String into a Double. */
  public static final Lambda<Double,String> PARSE_DOUBLE =
    new edu.rice.cs.util.Lambda<Double,String>() {
    public Double apply(String s) { return new Double(s); }
  };

  /** Lambda to parse a String into a String. */
  public static final Lambda<String,String> PARSE_STRING =
    new edu.rice.cs.util.Lambda<String,String>() {
    public String apply(String s) { return s; }
  };
  
  /** Formatter for Booleans. */
  public static final Lambda<String,Boolean> FORMAT_BOOL = 
    new edu.rice.cs.util.Lambda<String,Boolean>() {
    public String apply(Boolean b) { return b.toString().toLowerCase(); }
  };
  
  /** Formatter for Numbers. */
  public static final Lambda<String,Double> FORMAT_DOUBLE = 
    new edu.rice.cs.util.Lambda<String,Double>() {
    public String apply(Double d) {
      String s = d.toString();
      while(s.endsWith("0")) { s = s.substring(0, s.length()-1); }
      if (s.endsWith(".")) { s = s.substring(0, s.length()-1); }
      return s;
    }
  };    
} 
