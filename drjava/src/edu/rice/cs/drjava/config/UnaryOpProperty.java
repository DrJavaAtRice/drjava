/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.config;

import edu.rice.cs.plt.lambda.Lambda;

/** Class representing unary operations that can be inserted as variables in external processes.
  * @version $Id: UnaryOpProperty.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class UnaryOpProperty<P,R> extends EagerProperty {
  /** Operation to perform. */
  protected Lambda<P,R> _op;
  /** Operator name */
  protected String _op1Name;
  /** Operator default */
  protected String _op1Default;
  /** Lambda to turn a string into the operand. */
  protected Lambda<String,P> _parse;
  /** Lambda to format the result. */
  protected Lambda<R,String> _format;
  
  /** Create an eager property. */
  public UnaryOpProperty(String name,
                         String help,
                         Lambda<P,R> op,
                         String op1Name,
                         String op1Default,
                         Lambda<String,P> parse,
                         Lambda<R,String> format) {
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
                         Lambda<P,R> op,
                         Lambda<String,P> parse,
                         Lambda<R,String> format) {
    this(name, help, op, "op", null, parse, format);
  }
  
  /** Update the property so the value is current. 
    * @param pm PropertyMaps used for substitution when replacing variables */
  public void update(PropertyMaps pm) {
    P op;
    if (_attributes.get(_op1Name) == null) {
      _value = "(" + _name + " Error...)";
      return;
    }
    else {
      try {
        op = _parse.value(_attributes.get(_op1Name));
      }
      catch(Exception e) {
        _value = "(" + _name + " Error...)";
        return;
      }
    }
    _value = _format.value(_op.value(op));
  }
  
  public void resetAttributes() {
    _attributes.clear();
    _attributes.put(_op1Name, _op1Default);
  }
  
  /** Lambda to parse a String into a Double. */
  public static final Lambda<String,Double> PARSE_DOUBLE =
    new Lambda<String,Double>() {
    public Double value(String s) { return new Double(s); }
  };
  
  /** Lambda to parse a String into a Boolean. */
  public static final Lambda<String,Boolean> PARSE_BOOL =
    new Lambda<String,Boolean>() {
    public Boolean value(String s) { return new Boolean(s); }
  };

  /** Lambda to parse a String into a String. */
  public static final Lambda<String,String> PARSE_STRING =
    new Lambda<String,String>() {
    public String value(String s) { return s; }
  };
  
  /** Formatter for Booleans. */
  public static final Lambda<Boolean,String> FORMAT_BOOL = 
    new Lambda<Boolean,String>() {
    public String value(Boolean b) { return b.toString().toLowerCase(); }
  };
  
  /** Formatter for Numbers. */
  public static final Lambda<Double,String> FORMAT_DOUBLE = 
    new Lambda<Double,String>() {
    public String value(Double d) {
      String s = d.toString();
      while(s.endsWith("0")) { s = s.substring(0, s.length()-1); }
      if (s.endsWith(".")) { s = s.substring(0, s.length()-1); }
      return s;
    }
  };    

  /** Formatter for Strings. */
  public static final Lambda<String,String> FORMAT_STRING = 
    new Lambda<String,String>() {
    public String value(String s) { return s; }
  };
} 
