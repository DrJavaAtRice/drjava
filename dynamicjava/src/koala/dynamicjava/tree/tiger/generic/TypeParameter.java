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


package koala.dynamicjava.tree.tiger.generic;

import koala.dynamicjava.tree.Node;
import koala.dynamicjava.tree.ReferenceType;
import koala.dynamicjava.SourceInfo;
import koala.dynamicjava.tree.visitor.Visitor;
import koala.dynamicjava.tree.tiger.generic.visitor.GenericVisitor;

/**
 * Class TypeParameter, a component of the DynamicJava composite hierarchy.
 * Note: null is not allowed as a value for any field.
 */
public class TypeParameter extends Node {
  private final TypeVariable _variable;
  private final ReferenceType _bound;
  
  /**
   * Constructs a TypeParameter.
   * @throw IllegalArgumentException if any parameter to the constructor is null.
   */
  public TypeParameter(SourceInfo in_sourceInfo, TypeVariable in_variable, ReferenceType in_bound) {
    super(in_sourceInfo.getFilename(), in_sourceInfo.getStartLine(), 
          in_sourceInfo.getStartColumn(), in_sourceInfo.getEndLine(), in_sourceInfo.getEndColumn());
    
    if (in_variable == null) {
      throw new IllegalArgumentException("Parameter 'variable' to the TypeParameter constructor was null. This class may not have null field values.");
    }
    _variable = in_variable;
    
    if (in_bound == null) {
      throw new IllegalArgumentException("Parameter 'bound' to the TypeParameter constructor was null. This class may not have null field values.");
    }
    _bound = in_bound;
  }
  
  final public TypeVariable getVariable() { return _variable; }
  final public ReferenceType getBound() { return _bound; }
  
  public <T> T acceptVisitor(GenericVisitor<T> visitor) {
    return visitor.visit(this);
  } /**/
  
  public <T> T acceptVisitor(Visitor<T> visitor) {
    if(visitor instanceof GenericVisitor<T>){
      // did static method overloading resolution not work?!
      return acceptVisitor((GenericVisitor<T>)visitor);
    }
    else {
      throw new IllegalArgumentException("*Generic* AST nodes should be visited only by *generic* visitors");
    }
  } 
}
