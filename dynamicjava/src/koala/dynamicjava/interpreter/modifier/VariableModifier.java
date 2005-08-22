/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.interpreter.modifier;

import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;

/**
 * This interface represents objets that modify a variable
 *
 * @author Stephane Hillion
 * @version 1.1 - 1999/11/28
 */

public class VariableModifier extends LeftHandSideModifier {
  
  /**The name of the variable. */
  protected QualifiedName name;
  
  /** The type of this variable. */
  protected Class<?> type;
  
  /** The representation of the variable. */
  protected String representation;
  
  /** Creates a new variable modifier
   *  @param name the node of that represents this variable
   *  @param type the declared type of the variable
   */
  public VariableModifier(QualifiedName name, Class<?> type) {
    this.name = name;
    this.type = type;
    representation = name.getRepresentation();
  }
  
  /** Prepares the modifier for modification */
  public Object prepare(Visitor<Object> v, Context<Object> ctx) { return ctx.get(representation); }
  
  /** Sets the value of the underlying left hand side expression. */
  public void modify(Context<Object> ctx, Object value) {
    if (type.isPrimitive() || value == null || type.isAssignableFrom(value.getClass())) {
      ctx.set(representation, value);
    } 
    else {
      ClassCastException cce = new ClassCastException(representation);
      throw new CatchedExceptionError(cce, name);
    }
  }
}
