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

import koala.dynamicjava.classinfo.*;
import koala.dynamicjava.tree.*;

/**
 * The instances of this class provides informations about
 * class fields not yet compiled to JVM bytecode.
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/29
 */

public class TreeFieldInfo implements FieldInfo {
    /**
     * The abstract syntax tree of this field
     */
    private FieldDeclaration fieldTree;

    /**
     * The class finder for this class
     */
    private ClassFinder classFinder;

    /**
     * The type of this field
     */
    private ClassInfo type;

    /**
     * The declaring class
     */
    private ClassInfo declaringClass;

    /**
     * A visitor to load type infos
     */
    private TypeVisitor typeVisitor;

    /**
     * Creates a new class info
     * @param f  the field tree
     * @param cf the class finder
     * @param dc the declaring class
     */
    public TreeFieldInfo(FieldDeclaration f, ClassFinder cf, ClassInfo dc) {
        fieldTree      = f;
        classFinder    = cf;
	declaringClass = dc;
        typeVisitor    = new TypeVisitor(classFinder, declaringClass);
    }

    /**
     * Returns the field declaration
     */
    public FieldDeclaration getFieldDeclaration() {
	return fieldTree;
    }

    /**
     * Returns the modifiers for the field represented by this object
     */
    public int getModifiers() {
        return fieldTree.getAccessFlags();
    }

    /**
     * Returns the type of the underlying field
     */
    public ClassInfo getType() {
	if (type == null) {
	    type = (ClassInfo)fieldTree.getType().acceptVisitor(typeVisitor);
	}
        return type;
    }

    /**
     * Returns the fully qualified name of the underlying field
     */
    public String getName() {
        return fieldTree.getName();
    }

    /**
     * Indicates whether some other object is "equal to" this one
     */
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TreeFieldInfo)) {
            return false;
        }
        return fieldTree.equals(((TreeFieldInfo)obj).fieldTree);
    }
}
