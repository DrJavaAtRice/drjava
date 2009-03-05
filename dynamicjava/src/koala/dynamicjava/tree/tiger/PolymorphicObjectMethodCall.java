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

import java.util.*;

public class PolymorphicObjectMethodCall extends ObjectMethodCall {
  /**
   * The type arguments on which this method call applies
   */
  private List<TypeName> _typeArgs;

  /**
   * Creates a new node
   * @param exp   the expression on which this method call applies
   * @param mn    the field name
   * @param args  the arguments. Can be null.
   * @param targs the type arguments
   * @param fn    the filename
   * @param bl    the begin line
   * @param bc    the begin column
   * @param el    the end line
   * @param ec    the end column
   * @exception IllegalArgumentException if mn is null
   */
  public PolymorphicObjectMethodCall(Expression exp, String mn, List<? extends Expression> args, List<TypeName> targs,
                          String fn, int bl, int bc, int el, int ec) {
    super(exp, mn, args, fn, bl, bc, el, ec);
    _typeArgs = targs;
  }

  /**
   * Creates a new node
   * @param exp   the expression on which this method call applies
   * @param mn    the field name
   * @param args  the arguments. Can be null.
   * @exception IllegalArgumentException if mn is null
   */
  public PolymorphicObjectMethodCall(Expression exp, String mn, List<? extends Expression> args, List<TypeName> targs) {
    this(exp, mn, args, targs, null, 0, 0, 0, 0);
  }

  public List<TypeName> getTypeArguments(){ return _typeArgs; }

  public String toStringHelper() {
    return ""+getTypeArguments()+" "+super.toStringHelper();
  }
}
