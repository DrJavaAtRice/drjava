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
 * This class represents the field access nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/01
 */

public class StaticFieldAccess extends FieldAccess {
    /**
     * The fieldType property name
     */
    public final static String FIELD_TYPE = "fieldType";

    /**
     * The type on which this field access applies
     */
    private ReferenceType fieldType;

    /**
     * Creates a new field access node
     * @param typ   the type on which this field access applies
     * @param fln   the field name
     * @exception IllegalArgumentException if typ is null or fln is null
     */
    public StaticFieldAccess(ReferenceType typ, String fln) {
	this(typ, fln, null, 0, 0, 0, 0);
    }

    /**
     * Creates a new field access node
     * @param typ   the type on which this field access applies
     * @param fln   the field name
     * @param fn    the filename
     * @param bl    the begin line
     * @param bc    the begin column
     * @param el    the end line
     * @param ec    the end column
     * @exception IllegalArgumentException if typ is null or fln is null
     */
    public StaticFieldAccess(ReferenceType typ, String fln,
			     String fn, int bl, int bc, int el, int ec) {
	super(fln, fn, bl, bc, el, ec);

	if (typ == null) throw new IllegalArgumentException("typ == null");

	fieldType = typ;
    }

    /**
     * Returns the declaring type of the field
     */
    public ReferenceType getFieldType() {
	return fieldType;
    }

    /**
     * Sets the declaring type of the field
     * @exception IllegalArgumentException if t is null
     */
    public void setFieldType(ReferenceType t) {
	if (t == null) throw new IllegalArgumentException("t == null");

	firePropertyChange(FIELD_TYPE, fieldType, fieldType = t);
    }

    /**
     * Allows a visitor to traverse the tree
     * @param visitor the visitor to accept
     */
    public Object acceptVisitor(Visitor visitor) {
	return visitor.visit(this);
    }
}
