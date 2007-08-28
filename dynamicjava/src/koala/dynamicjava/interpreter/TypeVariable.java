/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

package koala.dynamicjava.interpreter;

import koala.dynamicjava.interpreter.type.Type;

/**
 * Represents a type variable.  For simplicity, two kinds of variables may be represented.
 * Declared type variables have a name; generated type variables are unnamed.  In both
 * cases, equality is defined in terms of identity, rather than by equating names or other
 * parameters.
 */
public class TypeVariable {
  
  private final boolean _generated;
  private final String _name;
  private final Type _upperBound;
  private final Type _lowerBound;

  public TypeVariable(Type upperBound, Type lowerBound) { 
    _generated = true;
    _name = null; 
    _upperBound = upperBound;
    _lowerBound = lowerBound;
  }
  
  public TypeVariable(String name, Type upperBound, Type lowerBound) { 
    _generated = false;
    _name = name; 
    _upperBound = upperBound;
    _lowerBound = lowerBound;
  }
  
  public boolean generated() { return _generated; }
  
  public String name() { 
    if (_generated) { throw new IllegalArgumentException("Variable is unnamed"); }
    else { return _name; }
  }
  
  public Type upperBound() { return _upperBound; }
  
  public Type lowerBound() { return _lowerBound; }

}
