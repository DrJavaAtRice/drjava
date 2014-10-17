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

package edu.rice.cs.drjava.model.repl.newjvm;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import edu.rice.cs.dynamicjava.interpreter.InterpreterException;
import edu.rice.cs.dynamicjava.interpreter.EvaluatorException;

/**
 * Super class for any type of result that can occur from a call to interpret.
 * 
 * @version $Id: InterpretResult.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public abstract class InterpretResult implements Serializable {  
  public abstract <T> T apply(Visitor<T> v);

  public static interface Visitor<T> {
    public T forNoValue();
    public T forStringValue(String val);
    public T forCharValue(Character val);
    public T forNumberValue(Number val);
    public T forBooleanValue(Boolean val);
    public T forObjectValue(String valString, String objTypeStr);
    public T forException(String message);
    public T forEvalException(String message, StackTraceElement[] stackTrace);
    public T forUnexpectedException(Throwable t);
    public T forBusy();
  }
  
  public static InterpretResult busy() { return BusyResult.INSTANCE; }
  
  // This and later classes are declared explicitly rather than anonymously as a
  // serialization best practice.
  private static class BusyResult extends InterpretResult {
    public static final BusyResult INSTANCE = new BusyResult();
    public <T> T apply(Visitor<T> v) { return v.forBusy(); }
  }
  
  
  public static InterpretResult exception(InterpreterException e) { return new ExceptionResult(e); }
  
  private static class ExceptionResult extends InterpretResult {
    private final String _msg;
    private final StackTraceElement[] _stackTrace;
    public ExceptionResult(InterpreterException e) {
      if (e instanceof EvaluatorException) {
        // for EvaluatorException, we want to keep the stack trace
        _msg = e.getMessage();
        _stackTrace = e.getCause().getStackTrace();
      }
      else {
        // for other InterpreterExceptions, we need to convert to a string here
        StringWriter msg = new StringWriter();
        e.printUserMessage(new PrintWriter(msg));
        _msg = msg.toString().trim();
        _stackTrace = null;
      }
    }
    public <T> T apply(Visitor<T> v) {
      if (_stackTrace != null) 
        return v.forEvalException(_msg, _stackTrace);
      else
        return v.forException(_msg);
    }
  }
  

  public static InterpretResult unexpectedException(Throwable t) {
    return new UnexpectedExceptionResult(t);
  }
  
  private static class UnexpectedExceptionResult extends InterpretResult {
    private final Throwable _t;
    public UnexpectedExceptionResult(Throwable t) { _t = t; }
    public <T> T apply(Visitor<T> v) { return v.forUnexpectedException(_t); }
  }
  
  
  public static InterpretResult noValue() { return NoValueResult.INSTANCE; }
  
  private static class NoValueResult extends InterpretResult {
    public static final NoValueResult INSTANCE = new NoValueResult();
    public <T> T apply(Visitor<T> v) { return v.forNoValue(); }
  }
  
  
  public static InterpretResult stringValue(String s) { return new StringValueResult(s); }
  
  private static class StringValueResult extends InterpretResult {
    private final String _val;
    public StringValueResult(String val) { _val = val; }
    public <T> T apply(Visitor<T> v) { return v.forStringValue(_val); }
    public String toString() { return "StringValueResult(" + _val + ")"; }
  }
  
  public static InterpretResult charValue(Character c) { return new CharValueResult(c); }
  
  private static class CharValueResult extends InterpretResult {
    private final Character _val;
    public CharValueResult(Character val) { _val = val; }
    public <T> T apply(Visitor<T> v) { return v.forCharValue(_val); }
  }

  public static InterpretResult numberValue(Number n) { return new NumberValueResult(n); }
  
  private static class NumberValueResult extends InterpretResult {
    private final Number _val;
    public NumberValueResult(Number val) { _val = val; }
    public <T> T apply(Visitor<T> v) { return v.forNumberValue(_val); }
  }
  
  public static InterpretResult booleanValue(Boolean b) { return new BooleanValueResult(b); }
  
  private static class BooleanValueResult extends InterpretResult {
    private final Boolean _val;
    public BooleanValueResult(Boolean val) { _val = val; }
    public <T> T apply(Visitor<T> v) { return v.forBooleanValue(_val); }
  }
  
  public static InterpretResult objectValue(String objS, String objTS) { 
    return new ObjectValueResult(objS, objTS); 
  }

  private static class ObjectValueResult extends InterpretResult {
    private final String _objString;
    private final String _objTypeStr;
    public ObjectValueResult(String objString, String objTypeStr) {
        _objString = objString;
        _objTypeStr = objTypeStr;
    }
    public <T> T apply(Visitor<T> v) { return v.forObjectValue(_objString, _objTypeStr); }
    public String toString() { return "ObjectValueResult(" + _objString + ")"; }
  } 
  
}
