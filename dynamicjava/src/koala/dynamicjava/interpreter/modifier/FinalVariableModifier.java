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
 * This interface represents objets that modify a final variable
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/11/21
 */

public class FinalVariableModifier extends VariableModifier {
    /**
     * Creates a new final variable modifier
     * @param name the node of that represents this variable
     * @param type the declared type of the variable
     */
    public FinalVariableModifier(QualifiedName name, Class<?> type) {
 super(name, type);
    }

    /**
     * Sets the value of the underlying left hand side expression
     */
    public void modify(Context ctx, Object value) {
 if (type.isPrimitive()                     ||
     value == null                          ||
     type.isAssignableFrom(value.getClass())) {
     if (ctx.get(representation) == UninitializedObject.INSTANCE) {
  ctx.setConstant(representation, value);
     } else {
  throw new ExecutionError("cannot.modify", name);
     }
 } else {
     Exception e = new ClassCastException(name.getRepresentation());
     throw new CatchedExceptionError(e, name);
 }
    }
}
