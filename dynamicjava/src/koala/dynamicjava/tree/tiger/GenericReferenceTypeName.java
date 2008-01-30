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

package koala.dynamicjava.tree.tiger;


import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.Visitor;

import java.util.List;

/**
 * This class represents a generic reference type
 */

public class GenericReferenceTypeName extends ReferenceTypeName {
  
  private List<List<? extends TypeName>> _typeArguments;
  
  /**
   * Initializes the type
   * @param ids   the list of the tokens that compose the type name
   * @param typeArgs the type arguments
   * @exception IllegalArgumentException if ids is null
   */
  public GenericReferenceTypeName(List<IdentifierToken> ids, List<List<? extends TypeName>> typeArgs) {
    this(ids, typeArgs, null, 0, 0, 0, 0);
  }
  
//  /**
//   * Initializes the type
//   * @param rep   the type name
//   * @param typeArgs the type arguments
//   * @exception IllegalArgumentException if rep is null
//   */
//  public GenericReferenceTypeName(String rep, List<List<? extends TypeName>> typeArgs) {
//    this(rep, typeArgs, null, 0, 0, 0, 0);
//  }
  
  /**
   * Initializes the type
   * @param ids   the list of the tokens that compose the type name
   * @param fn    the filename
   * @param bl    the begin line
   * @param bc    the begin column
   * @param el    the end line
   * @param ec    the end column
   * @param typeParams the type parameters
   * @exception IllegalArgumentException if ids is null
   */
  public GenericReferenceTypeName(List<IdentifierToken> ids, List<List<? extends TypeName>> typeArgs, String fn,  int bl, int bc, int el, int ec) {
    super(ids, fn, bl, bc, el, ec);
    if (ids.size() != typeArgs.size()) { throw new IllegalArgumentException("ids.size() != typeArgs.size()"); }
    _typeArguments = typeArgs;
  }
  

  public List<List<? extends TypeName>> getTypeArguments(){ return _typeArguments; }
  
  /**
   * Allows a visitor to traverse the tree
   * @param visitor the visitor to accept
   */
  public <T> T acceptVisitor(Visitor<T> visitor) {
    return visitor.visit(this);
  }

  public String toString() {
    return "("+getClass().getName()+": "+toStringHelper()+")";
  }
  
  public String toStringHelper() {
    String typeArgS = "";
    List<List<? extends TypeName>> alltas = getTypeArguments();
    for( List<? extends TypeName> ta : alltas ){
      if(ta.size()>0)
        typeArgS = ""+ta.get(0);
      for(int i = 1; i < ta.size(); i++)
        typeArgS += " " + ta.get(i);
      typeArgS += ":";
    }    
    return super.toStringHelper()+" "+typeArgS;
  }
  
}
