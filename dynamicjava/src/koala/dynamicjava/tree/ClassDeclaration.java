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

import java.util.*;

import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents a class declaration
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/10
 */

public class ClassDeclaration extends TypeDeclaration {
    /**
     * The superclass property name
     */
    public final static String SUPERCLASS = "superclass";

    /**
     * The superclass of this class
     */
    private String superclass;

    /**
     * Creates a new class declaration
     * @param flags the access flags
     * @param name  the name of the class to declare
     * @param ext   the tokens that compose the name of the parent class.
     *              The list can be null. The superclass property is then
     *              set to "java.lang.Object".
     * @param impl  the list of implemented interfaces (a list of list of
     *              Token). Can be null.
     * @param body  the list of members declarations
     */
    public ClassDeclaration(int flags, String name, List ext, List impl, List body) {
	this(flags, name, ext, impl, body, null, 0, 0, 0, 0);
    }

    /**
     * Creates a new class declaration
     * @param flags the access flags
     * @param name  the name of the class to declare
     * @param ext   the tokens that compose the name of the parent class.
     *              The list can be null. The superclass property is then
     *              set to "java.lang.Object".
     * @param impl  the list of implemented interfaces (a list of list of
     *              Token). Can be null.
     * @param body  the list of members declarations
     * @param fn    the filename
     * @param bl    the begin line
     * @param bc    the begin column
     * @param el    the end line
     * @param ec    the end column
     */
    public ClassDeclaration(int flags, String name, List ext, List impl, List body,
			    String fn, int bl, int bc, int el, int ec) {
	super(flags, name, impl, body, fn, bl, bc, el, ec);
	if (ext == null) {
	    superclass = "java.lang.Object";
	} else {
	    superclass = TreeUtilities.listToName(ext);
	}
    }

    /**
     * Returns the name of the superclass of this class
     */
    public String getSuperclass() {
	return superclass;
    }

    /**
     * Sets the superclass name
     * @exception IllegalArgumentException if s is null
     */
    public void setSuperclass(String s) {
	if (s == null) throw new IllegalArgumentException("s == null");

	firePropertyChange(SUPERCLASS, superclass, superclass = s);
    }

    /**
     * Allows a visitor to traverse the tree
     * @param visitor the visitor to accept
     */
    public Object acceptVisitor(Visitor visitor) {
	return visitor.visit(this);
    }
}
