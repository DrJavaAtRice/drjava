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

package koala.dynamicjava.interpreter;

import koala.dynamicjava.classinfo.*;
import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.util.*;

/**
 * The instances of the classes that implements this interface
 * are used to find the fully qualified name of classes and to
 * manage the loading of these classes.
 *
 * @author  Stephane Hillion
 * @version 1.1 - 1999/11/28
 */

public class TreeClassFinder implements ClassFinder {
    /**
     * The context
     */
    protected Context context;

    /**
     * The current interpreter
     */
    protected Interpreter interpreter;

    /**
     * The class pool
     */
    protected ClassPool classPool;

    /**
     * Creates a  new class finder
     * @param ctx the context
     * @param i   the current interpreter
     * @param cp  the class pool
     */
    public TreeClassFinder(Context ctx, Interpreter i, ClassPool cp) {
	context     = ctx;
	interpreter = i;
	classPool   = cp;
    }

    
    /**
     * Returns the current interpreter
     */
    public Interpreter getInterpreter() {
	return interpreter;
    }

    /**
     * Returns the current package
     */
    public String getCurrentPackage() {
	return context.getCurrentPackage();
    }

    /**
     * Returns the importation manager
     */
    public ImportationManager getImportationManager() {
	return context.getImportationManager();
    }

    /**
     * Loads the class info that match the given name in the source file
     * @param  cname the name of the class to find
     * @return the class info
     * @exception ClassNotFoundException if the class cannot be loaded
     */
    public ClassInfo lookupClass(String cname) throws ClassNotFoundException {
	if (classPool.contains(cname)) {
	    return classPool.get(cname);
	}
	try {
	    return new JavaClassInfo(context.lookupClass(cname, null));
	} catch (TreeCompiler.PseudoError e) {
	    return e.getClassInfo();
	}
    }

    /**
     * Loads the class info that match the given name in the source file
     * @param  cname the name of the class to find
     * @param  cinfo the context where 'cname' was found
     * @return the class info
     * @exception ClassNotFoundException if the class cannot be loaded
     */
    public ClassInfo lookupClass(String cname, ClassInfo cinfo)
	throws ClassNotFoundException {
	String name = cinfo.getName();
	if (classPool.contains(cname)) {
	    return classPool.get(cname);
	} else {
	    // cname represents perhaps an inner class
	    String s = name + "$" + cname;
	    if (classPool.contains(s)) {
		return classPool.get(s);
	    }
	}
	try {
	    return new JavaClassInfo(context.lookupClass(cname, name));
	} catch (ClassNotFoundException e) {
	    // look after an inner class of the declaring class
	    ClassInfo ci = cinfo.getDeclaringClass();
	    try {
		if (ci != null) {
		    return new JavaClassInfo
			(context.lookupClass(ci.getName() + "$" + cname));
		}
		throw new ClassNotFoundException(cname);
	    } catch (ClassNotFoundException ex) {
		// Look after an inner class of an ancestor
		ci = cinfo;
		while ((ci = ci.getSuperclass()) != null) {
		    try {
			return new JavaClassInfo
			    (context.lookupClass(ci.getName() + "$" + cname));
		    } catch (ClassNotFoundException e2) {
		    } catch (TreeCompiler.PseudoError e2) {
			return e2.getClassInfo();
		    }
		}
	    } catch (TreeCompiler.PseudoError ex) {
		return ex.getClassInfo();
	    }
	} catch (TreeCompiler.PseudoError e) {
	    return e.getClassInfo();
	}
	throw new ClassNotFoundException(cname);
    }

    /**
     * Adds a type declaration in the class info list
     * @param cname the name of the class
     * @param decl the type declaration
     */
    public ClassInfo addClassInfo(String cname, TypeDeclaration decl) {
	return classPool.add(cname, new TreeClassInfo(decl, this));
    }
}
