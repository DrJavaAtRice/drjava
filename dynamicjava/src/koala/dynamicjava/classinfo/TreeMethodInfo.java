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
import java.util.*;

import koala.dynamicjava.tree.*;

/**
 * The instances of this class provides informations about
 * class methods not yet compiled to JVM bytecode.
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/29
 */

public class TreeMethodInfo implements MethodInfo {
    /**
     * The abstract syntax tree of this method
     */
    private MethodDeclaration methodTree;

    /**
     * The class finder for this class
     */
    private ClassFinder classFinder;

    /**
     * The parameters types
     */
    private ClassInfo[] parameters;

    /**
     * The exception types
     */
    private ClassInfo[] exceptions;

    /**
     * The declaring class
     */
    private ClassInfo declaringClass;

    /**
     * A visitor to load type infos
     */
    private TypeVisitor typeVisitor;

    /**
     * Creates a new method info
     * @param f the method tree
     * @param cf the class finder
     * @param dc the declaring class
     */
    public TreeMethodInfo(MethodDeclaration f, ClassFinder cf, ClassInfo dc) {
        methodTree     = f;
        classFinder    = cf;
	declaringClass = dc;
        typeVisitor    = new TypeVisitor(classFinder, declaringClass);
    }

    /**
     * Returns the method declaration
     */
    public MethodDeclaration getMethodDeclaration() {
	return methodTree;
    }

    /**
     * Returns the modifiers for the method represented by this object
     */
    public int getModifiers() {
        return methodTree.getAccessFlags();
    }

    /**
     * Returns a Class object that represents the return type
     * of the method represented by this object
     */
    public ClassInfo getReturnType() {
        return (ClassInfo)methodTree.getReturnType().acceptVisitor(typeVisitor);
    }

    /**
     * Returns the name of the underlying method
     */
    public String getName() {
        return methodTree.getName();
    }

    /**
     * Returns an array of class infos that represent the parameter
     * types, in declaration order, of the method represented
     * by this object
     */
    public ClassInfo[] getParameterTypes() {
	if (parameters == null) {
	    List        ls = methodTree.getParameters();
	    Iterator    it = ls.iterator();
	    parameters     = new ClassInfo[ls.size()];
	    int         i  = 0;

	    while (it.hasNext()) {
		FormalParameter fp = (FormalParameter)it.next();
		parameters[i++] = (ClassInfo)fp.getType().acceptVisitor(typeVisitor);
	    }
	}
	return (ClassInfo[])parameters.clone();
    }

    /**
     * Returns an array of Class infos that represent the types of
     * the exceptions declared to be thrown by the underlying method
     */
    public ClassInfo[] getExceptionTypes() {
	if (exceptions == null) {
            List        ls = methodTree.getExceptions();
            Iterator    it = ls.iterator();
            exceptions     = new ClassInfo[ls.size()];
            int         i  = 0;
            while (it.hasNext()) {
                exceptions[i++] = lookupClass((String)it.next(), declaringClass);
            }
        }
	return (ClassInfo[])exceptions.clone();
    }

    /**
     * Indicates whether some other object is "equal to" this one
     */
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TreeMethodInfo)) {
            return false;
        }
        return methodTree.equals(((TreeMethodInfo)obj).methodTree);
    }

    /**
     * Looks for a class from its name
     * @param s the name of the class to find
     * @param c the context
     * @exception NoClassDefFoundError if the class cannot be loaded
     */
    protected ClassInfo lookupClass(String s, ClassInfo c) {
	try {
	    if (c == null) {
		return classFinder.lookupClass(s, c);
	    } else {
		return classFinder.lookupClass(s);
	    }
	} catch (ClassNotFoundException e) {
	    throw new NoClassDefFoundError(e.getMessage());
	}
    }

}
