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

package koala.dynamicjava.tree;

import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents the type expression nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public class TypeExpression extends PrimaryExpression {
    /**
     * The type property name
     */
    public final static String TYPE = "type";

    /**
     * The type represented by this expression
     */
    private Type type;
    
    /**
     * Initializes the expression
     * @param t     the type represented by this expression
     * @exception IllegalArgumentException if t is null
     */
    public TypeExpression(Type t) {
	this(t, null, 0, 0, 0, 0);
    }

    /**
     * Initializes the expression
     * @param t     the type represented by this expression
     * @param fn    the filename
     * @param bl    the begin line
     * @param bc    the begin column
     * @param el    the end line
     * @param ec    the end column
     * @exception IllegalArgumentException if t is null
     */
    public TypeExpression(Type t, String fn, int bl, int bc, int el, int ec) {
	super(fn, bl, bc, el, ec);

	if (t == null) throw new IllegalArgumentException("t == null");

	type = t;
    }

    /**
     * Returns the type represented by this expression
     */
    public Type getType() {
	return type;
    }

    /**
     * Sets the type
     * @exception IllegalArgumentException if t is null
     */
    public void setType(ReferenceType t) {
	if (t == null) throw new IllegalArgumentException("t == null");

	firePropertyChange(TYPE, type, type = t);
    }

    /**
     * Allows a visitor to traverse the tree
     * @param visitor the visitor to accept
     */
    public Object acceptVisitor(Visitor visitor) {
	return visitor.visit(this);
    }
}
