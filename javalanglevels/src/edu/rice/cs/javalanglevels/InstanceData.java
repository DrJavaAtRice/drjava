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

package edu.rice.cs.javalanglevels;

import edu.rice.cs.javalanglevels.tree.*;
import edu.rice.cs.javalanglevels.parser.JExprParser;
import java.util.*;

import junit.framework.TestCase;

/** Represents the data for an instantiation of a class.  When you actually create an object of some type,
  * an InstanceData represents what you have created.  Each InstanceData has a pointer to the SymbolData of its
  * class type.
  */
public class InstanceData extends TypeData {
  
  /**The class corresponding to this InstanceData*/
  private SymbolData _classSymbolData;
  
  /*@param classSD  The SymbolData this is an instance of*/
  public InstanceData(SymbolData classSD) { 
    super(null);
    _classSymbolData = classSD;
    _name = classSD.getName();
  }
  
  /**@return  true since this is an InstanceData.*/
  public boolean isInstanceType() { return true; }
 
  /**@return  The class SymbolData corresponding to the class of this InstanceData.*/
  public SymbolData getSymbolData() { return _classSymbolData;  }

  /**@return this InstanceData.*/
  public InstanceData getInstanceData() { return this; }
  
  public String toString() { return "An instance of type '" + _classSymbolData +"'"; }
  
  public boolean equals(Object o) { 
    return o != null && o.getClass() == getClass() && ((InstanceData)o)._classSymbolData.equals(_classSymbolData);
  }
  
  public int hashCode() { return _classSymbolData.hashCode(); }
}
