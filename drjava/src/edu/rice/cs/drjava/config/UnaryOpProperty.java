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
import edu.rice.cs.util.Lambda;

import java.util.HashSet;
import java.util.Iterator;

/** Class representing unary operations that can be inserted as variables in external processes.
  * @version $Id$
  */
public class UnaryOpProperty<P,R> extends EagerProperty {
  /** Operation to perform. */
  protected Lambda<R,P> _op;
  /** Operator name */
  protected String _op1Name;
  /** Operator default */
  protected String _op1Default;
  /** Lambda to turn a string into the operand. */
  protected Lambda<P,String> _parse;
  /** Lambda to format the result. */
  protected Lambda<String,R> _format;
  
  /** Create an eager property. */
  public UnaryOpProperty(String name,
                         String help,
                         Lambda<R,P> op,
                         String op1Name,
                         String op1Default,
                         Lambda<P,String> parse,
                         Lambda<String,R> format) {
    super(name, help);
    _op = op;
    _op1Name = op1Name;
    _op1Default = op1Default;
    _parse = parse;
    _format = format;
    resetAttributes();
  }

  /** Create an eager property. */
  public UnaryOpProperty(String name,
                         String help,
                         Lambda<R,P> op,
                         Lambda<P,String> parse,
                         Lambda<String,R> format) {
    this(name, help, op, "op", null, parse, format);
  }
  
  /** Update the property so the value is current. */
  public void update() {
    P op;
    if (_attributes.get(_op1Name)==null) {
      _value = "("+_name+" Error...)";
      return;
    }
    else {
      try {
        op = _parse.apply(_attributes.get(_op1Name));
      }
      catch(Exception e) {
        _value = "("+_name+" Error...)";
        return;
      }
    }
    _value = _format.apply(_op.apply(op));
  }
  
  public void resetAttributes() {
    _attributes.clear();
    _attributes.put(_op1Name, _op1Default);
  }
  
  /** @return true if the specified property is equal to this one. */
  public boolean equals(Object other) {
    if (other == null || other.getClass() != this.getClass()) return false;
    UnaryOpProperty o = (UnaryOpProperty)other;
    return _name.equals(o._name) && _value.equals(o._value) && (_isCurrent == o._isCurrent) && _op.equals(o._op)
      && _parse.equals(o._parse) && _format.equals(o._format);
  }
  
  /** @return the hash code. */
  public int hashCode() { return _name.hashCode() ^ _value.hashCode() ^ _op.hashCode() ^ _parse.hashCode() ^ 
    _format.hashCode() ^ (_isCurrent ? 1 : 0);
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

  /** Formatter for Strings. */
  public static final Lambda<String,String> FORMAT_STRING = 
    new edu.rice.cs.util.Lambda<String,String>() {
    public String apply(String s) { return s; }
  };
} 
