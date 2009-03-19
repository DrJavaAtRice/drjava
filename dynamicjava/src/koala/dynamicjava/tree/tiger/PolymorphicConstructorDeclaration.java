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

import java.util.List;

/**
 * This class represents polymorphic method declarations in an AST
 */

public class PolymorphicConstructorDeclaration extends ConstructorDeclaration {
  
  
  private TypeParameter[] _typeParameters;
  /**
   * Creates a new constructor declaration
   * @exception IllegalArgumentException if name is null or
   *            params is null or excepts is null
   */
  public PolymorphicConstructorDeclaration(ModifierSet mods, String name,
                                      List<FormalParameter> params, List<? extends ReferenceTypeName> excepts, 
                                      ConstructorCall eci, List<Node> stmts, TypeParameter[] typeParams) {
    this(mods, name, params, excepts, eci, stmts, typeParams, SourceInfo.NONE);
  }
  
  /**
   * Creates a new constructor declaration
   * @exception IllegalArgumentException if name is null or
   *            params is null or excepts is null
   */
  public PolymorphicConstructorDeclaration(ModifierSet mods, String name,
                                           List<FormalParameter> params, List<? extends ReferenceTypeName> excepts,
                                           ConstructorCall eci, List<Node> stmts, TypeParameter[] typeParams,
                                           SourceInfo si) {
    super(mods, name, params, excepts, eci, stmts, si);
    
    _typeParameters = typeParams;
  }
  
  public TypeParameter[] getTypeParameters(){ return _typeParameters; }
  
  public String toString() {
    return "("+getClass().getName()+": "+toStringHelper()+")";
  }
  
  public String toStringHelper() {
    TypeParameter[] tp = getTypeParameters();
    String typeParamsS = "";
    if(tp.length>0)
      typeParamsS = ""+tp[0];
    for(int i = 1; i < tp.length; i++)
      typeParamsS = typeParamsS + " " + tp[i];
    
    return typeParamsS+" "+super.toStringHelper();
  }
}