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

package koala.dynamicjava.classinfo;

import java.lang.reflect.*;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;

/**
 * The instances of this class are used to get the ClassInfo
 * that match a type node of a syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/06/03
 */

public class TypeVisitor extends VisitorObject {
    /**
     * The class finder for this class
     */
    private ClassFinder classFinder;

    /**
     * The context
     */
    private ClassInfo context;

    /**
     * Creates a new type visitor
     * @param cf  the class finder
     * @param ctx the context
     */
    public TypeVisitor(ClassFinder cf, ClassInfo ctx) {
	classFinder = cf;
	context     = ctx;
    }

    /**
     * Visits a PrimitiveType
     * @param node the node to visit
     * @return the representation of the visited type
     */
    public Object visit(PrimitiveType node) {
	return new JavaClassInfo(node.getValue());
    }

    /**
     * Visits a ReferenceType
     * @param node the node to visit
     * @return the representation of the visited type
     * @exception NoClassDefFoundError if the class cannot be loaded
     */
    public Object visit(ReferenceType node) {
	return lookupClass(node.getRepresentation(), context);
    }

    /**
     * Visits a ArrayType
     * @param node the node to visit
     * @return the representation of the visited type
     * @exception NoClassDefFoundError if the class cannot be loaded
     */
    public Object visit(ArrayType node) {
	ClassInfo ci = (ClassInfo)node.getElementType().acceptVisitor(this);
	return ci.getArrayType();
    }

    /**
     * Looks for a class from its name
     * @param s the name of the class to find
     * @param c the context
     * @exception NoClassDefFoundError if the class cannot be loaded
     */
    private ClassInfo lookupClass(String s, ClassInfo c) {
	try {
	    if (c != null) {
		return classFinder.lookupClass(s, c);
	    } else {
		return classFinder.lookupClass(s);
	    }
	} catch (ClassNotFoundException e) {
	    throw new NoClassDefFoundError(e.getMessage());
	}
    }
}
