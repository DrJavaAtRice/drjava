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

import koala.dynamicjava.tree.*;

import java.util.*;

/**
 * This class represents polymorphic method declarations in an AST
 */

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
  public PolymorphicObjectMethodCall(Expression exp, String mn, List<Expression> args, List<TypeName> targs,
                          String fn, int bl, int bc, int el, int ec) {
    super(exp, mn, args, fn, bl, bc, el, ec);
    _typeArgs = targs;
  }

  /**
   * Creates a new node
   * @param exp   the expression on which this method call applies
   * @param mn    the field name
   * @param args  the arguments. Can be null.
   * @param fn    the filename
   * @param bl    the begin line
   * @param bc    the begin column
   * @param el    the end line
   * @param ec    the end column
   * @exception IllegalArgumentException if mn is null
   */
  public PolymorphicObjectMethodCall(Expression exp, String mn, List<Expression> args, List<TypeName> targs) {
    this(exp, mn, args, targs, null, 0, 0, 0, 0);
  }

  public List<TypeName> getTypeArguments(){ return _typeArgs; }

  public String toStringHelper() {
//    List<TypeName> tp = getTypeArguments();
//    String typeArgsStr = "";
//    if(tp.size()>0)
//      typeArgsStr = ""+tp.get(0);
//    for(int i = 1; i < tp.size(); i++)
//      typeArgsStr = typeArgsStr + " " + tp.get(i);

    return ""+getTypeArguments()+" "+super.toStringHelper();
  }
}