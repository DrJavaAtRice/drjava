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

import java.io.*;
import java.util.*;

import koala.dynamicjava.classinfo.*;
import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.parser.wrapper.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;

/**
 * This class contains methods to manage the creation of classes.
 *
 * @author  Stephane Hillion
 * @version 1.1 - 1999/11/28
 */

public class TreeCompiler {
    /**
     * The interpreter
     */
    protected Interpreter interpreter;

    /**
     * The classloader
     */
    protected TreeClassLoader classLoader;

    /**
     * The class info loader
     */
    protected ClassInfoLoader classInfoLoader;

    /**
     * The class pool
     */
    protected ClassPool classPool = new ClassPool();

    /**
     * Creates a new compiler
     * @param i the current interpreter
     */
    public TreeCompiler(Interpreter i) {
	interpreter     = i;
	classLoader     = (TreeClassLoader)interpreter.getClassLoader();
	classInfoLoader = new ClassInfoLoader();
    }

    /**
     * Compiles a compilation unit
     * @param name the name of the class to compile
     */
    public Class compile(String name) throws ClassNotFoundException {
	loadClass(name);
	return compileClasses(name);
    }

    /**
     * Compiles all the classes in the class pool
     * @param name the name of the class to return
     */
    public Class compileClasses(String name) throws ClassNotFoundException {
	Class result = null;
	if (classPool.contains(name)) {
	    ClassInfo ci;
	    while ((ci = classPool.getFirstCompilable()) != null) {
		if (!classExists(ci.getName())) {
		    Class c = compileClass(ci, name);
		    if (c != null) {
			result = c;
		    }
		} else {
		    ci.setCompilable(false);
		}
	    }
	}

	if (result == null) {
	    throw new ClassNotFoundException(name);
	}
	return result;
    }

    /**
     * Compiles a single class
     * @param td the type declaration
     * @param im the importation manager
     */
    public Class compileTree(Context ctx, TypeDeclaration td) {
	ClassFinder cf = new TreeClassFinder(ctx, interpreter, classPool);
	ClassInfo   ci = new TreeClassInfo(td, cf);
	classPool.add(ci.getName(), ci);
	try {
	    return compileClasses(ci.getName());
	} catch (ClassNotFoundException e) {
	    td.setProperty(NodeProperties.ERROR_STRINGS,
			   new String[] { td.getName() });
           throw new ExecutionError("undefined.or.defined.class", td);
	}
    }

    /**
     * Compiles the given class info
     * @param ci   the class info to compile
     * @param name the name of the class to return
     */
    protected Class compileClass(ClassInfo ci, String name) {
	Class result = null;
	
	// Compile first the superclass and interfaces if needed
	ClassInfo t = ci.getSuperclass();
	if (t.isCompilable() && !classExists(t.getName())) {
	    Class c = compileClass(t, name);
	    if (c != null) {
		result = c;
	    }
	}
	
	ClassInfo [] ti = ci.getInterfaces();
	for (int i = 0; i < ti.length; i++) {
	    t = ti[i];
	    if (t.isCompilable() && !classExists(t.getName())) {
		Class c = compileClass(t, name);
		if (c != null) {
		    result = c;
		}
	    }
	}

	// Then compile the class
	Class c = new ClassInfoCompiler(ci).compile();
	ci.setCompilable(false);
	if (name.equals(c.getName())) {
	    result = c;
	}

	return result;
    }

    /**
     * Whether a class exists in a compiled form
     */
    protected boolean classExists(String name) {
	return classLoader.hasDefined(name);
    }

    /**
     * Searches for a class, loads its class info structure
     */
    protected void loadClass(String name) throws ClassNotFoundException {
	if (classPool.contains(name)) {
	    return;
	}

	// Is there a tree associated with this name ?
	TypeDeclaration td = classLoader.getTree(name);
	if (td != null) {
	    ImportationManager im = (ImportationManager)td.getProperty
		(NodeProperties.IMPORTATION_MANAGER);
	    Context ctx = new GlobalContext(interpreter, classInfoLoader);
	    im.setClassLoader(classInfoLoader);
	    ctx.setImportationManager(im);
	    ClassFinder cfinder = new TreeClassFinder(ctx,
						      interpreter,
						      classPool);
	    classPool.add(name, new TreeClassInfo(td, cfinder));
	    return;
	}

	// Is the class tree already loaded ?
	LibraryFinder lf = interpreter.getLibraryFinder();
	try {
	    String cun = lf.findCompilationUnitName(name);
	    td = classLoader.getTree(cun);
	    if (td != null) {
		ImportationManager im = (ImportationManager)td.getProperty
		    (NodeProperties.IMPORTATION_MANAGER);
		Context ctx = new GlobalContext(interpreter, classInfoLoader);
		im.setClassLoader(classInfoLoader);
		ctx.setImportationManager(im);
		ClassFinder cfinder = new TreeClassFinder(ctx,
							  interpreter,
							  classPool);
		classPool.add(cun, new TreeClassInfo(td, cfinder));
		return;
	    }
	} catch (ClassNotFoundException e) {
	}

	try {
	    File f = lf.findCompilationUnit(name);
	    FileInputStream fis = new FileInputStream(f);

	    ParserFactory pf = interpreter.getParserFactory();
	    SourceCodeParser p = pf.createParser(fis, f.getCanonicalPath());
	    List stmts = p.parseCompilationUnit();

	    Iterator it = stmts.iterator();
	    Visitor  v  = new CompilationUnitVisitor();
	    while (it.hasNext()) {
		((Node)it.next()).acceptVisitor(v);
	    }
	} catch (IOException e) {
	    throw new ClassNotFoundException(name);
	}
    }

    /**
     * To create the class infos for a compilation unit
     */
    protected class CompilationUnitVisitor extends VisitorObject {
	/**
	 * The context
	 */
	protected Context context = new GlobalContext(interpreter, classInfoLoader);

	/**
	 * The class finder
	 */
	protected ClassFinder classFinder = new TreeClassFinder(context,
								interpreter,
								classPool);

	/**
	 * Visits a PackageDeclaration
	 * @param node the node to visit
	 * @return null
	 */
	public Object visit(PackageDeclaration node) {
	    context.setCurrentPackage(node.getName());
	    return null;
	}

	/**
	 * Visits an ImportDeclaration
	 * @param node the node to visit
	 */
	public Object visit(ImportDeclaration node) {
	    // Declare the package or class importation
	    if (node.isPackage()) {
		context.declarePackageImport(node.getName());
	    } else {
		try {
		    context.declareClassImport(node.getName());
		} catch (ClassNotFoundException e) {
		    throw new CatchedExceptionError(e, node);
		} catch (PseudoError e) {
		}
	    }
	    return null;
	}

	/**
	 * Visits a ClassDeclaration
	 * @param node the node to visit
	 */
	public Object visit(ClassDeclaration node) {
	    return visitType(node);
	}

	/**
	 * Visits an InterfaceDeclaration
	 * @param node the node to visit
	 */
	public Object visit(InterfaceDeclaration node) {
	    return visitType(node);
	}

	/**
	 * Visits a type declaration
	 * @param node the node to visit
	 */
	protected Object visitType(TypeDeclaration node) {
	    String cname = classFinder.getCurrentPackage();
	    cname = ((cname.equals("")) ? "" : cname + "." ) + node.getName();
	    classPool.add(cname, new TreeClassInfo(node, classFinder));
	    return null;
	}
    }

    /**
     * To load class infos instead of classes
     */
    protected class ClassInfoLoader extends ClassLoader {
	/**
	 * Finds the specified class.
	 * @param  name the name of the class
	 * @return the resulting <code>Class</code> object
	 * @exception ClassNotFoundException if the class could not be find
	 */
	protected Class findClass(String name) throws ClassNotFoundException {
	    TreeCompiler.this.loadClass(name);
	    if (classPool.contains(name)) {
		throw new PseudoError(classPool.get(name));
	    } else {
		throw new ClassNotFoundException(name);
	    }
	}
    }

    /**
     * To test the existance of a class without loading it
     */
    public class PseudoError extends Error {
	/**
	 * The exception content
	 */
	protected ClassInfo classInfo;

	/**
	 * Creates a new error
	 */
	PseudoError(ClassInfo ci) {
	    classInfo = ci;
	}

	/**
	 * Returns the class info
	 */
	public ClassInfo getClassInfo() {
	    return classInfo;
	}
    }
}
