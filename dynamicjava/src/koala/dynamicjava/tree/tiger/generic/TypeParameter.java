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
import koala.dynamicjava.tree.IdentifierToken;
import koala.dynamicjava.tree.TreeUtilities;
import koala.dynamicjava.SourceInfo;
import java.util.List;

/**
 * Class TypeParameter, a component of the DynamicJava composite hierarchy.
 * Note: null is not allowed as a value for any field.
 */
public class TypeParameter extends ReferenceType {
  private final ReferenceType _bound;
  
  /**
   * Constructs a TypeParameter.
   * @throw IllegalArgumentException if any parameter to the constructor is null.
   */
  public TypeParameter(SourceInfo in_sourceInfo, List<IdentifierToken> ids, ReferenceType in_bound) {
    this(in_sourceInfo, TreeUtilities.listToName(ids), in_bound);
  }
  
  /**
   * Constructs a TypeParameter.
   * @throw IllegalArgumentException if any parameter to the constructor is null.
   */
  public TypeParameter(SourceInfo in_sourceInfo, String rep, ReferenceType in_bound) {
    super(rep, in_sourceInfo.getFilename(), in_sourceInfo.getStartLine(),
          in_sourceInfo.getStartColumn(), in_sourceInfo.getEndLine(), in_sourceInfo.getEndColumn());
    
    if (in_bound == null) {
      throw new IllegalArgumentException("Parameter 'bound' to the TypeParameter constructor was null.");
    }
    _bound = in_bound;
  }
  
  public ReferenceType getBound() { return _bound; }
  
  public String getRepresentation(){
    return _bound.getRepresentation(); // coerce to bound's type
  }
  
  public String getName(){
    return super.getRepresentation();
  }
  
  public String toString() {
    return "("+getClass().getName()+": "+toStringHelper()+")";
  }
  
  protected String toStringHelper() {
    return getName()+" "+getBound();
  }
}
