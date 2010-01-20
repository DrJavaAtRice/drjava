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

package koala.dynamicjava.tree.tiger;

import edu.rice.cs.plt.tuple.Option;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.Visitor;

/**
 * This class represents the HookTypeName (?) nodes of the syntax tree
 *
 */

public class HookTypeName extends ReferenceTypeName {
  
  private final Option<TypeName> upperBound;
  private final Option<TypeName> lowerBound;
  
  /**
   * Initializes the type
   * @param up the upper bound
   * @param low the lower bound
   * @exception IllegalArgumentException if type is null
   */
  public HookTypeName(Option<TypeName> up, Option<TypeName> low) {
    this(up, low, SourceInfo.NONE);
  }

  /**
   * Initializes the type
   * @param up the upper bound
   * @param low the lower bound
   * @exception IllegalArgumentException if type is null
   */
  public HookTypeName(Option<TypeName> up, Option<TypeName> low,
                      SourceInfo si) {
    super("?");

    if (up == null) throw new IllegalArgumentException("up == null");
    if (low == null) throw new IllegalArgumentException("low == null");
    upperBound = up;
    lowerBound = low;
  }

  public Option<TypeName> getUpperBound() { return upperBound; }
  public Option<TypeName> getLowerBound() { return lowerBound; }
  
  /**
   * Allows a visitor to traverse the tree
   * @param visitor the visitor to accept
   */
  public <T> T acceptVisitor(Visitor<T> visitor) {
    return visitor.visit(this);
  }
    /**
   * Implementation of toString for use in unit testing
   */
  public String toString() {
    return "("+getClass().getName()+": "+toStringHelper()+")";
  }

  protected String toStringHelper() {
   return upperBound + " " + lowerBound;
  }
}
