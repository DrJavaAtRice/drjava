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
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.throwable.*;
import koala.dynamicjava.parser.wrapper.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;

/**
 * This class contains method to interpret the constructs
 * of the language.
 *
 * @author  Stephane Hillion
 * @version 1.6 - 2001/01/23
 */

public class TreeInterpreter implements Interpreter {
    /**
     * The parser
     */
    protected ParserFactory parserFactory;

    /**
     * The library finder
     */
    protected LibraryFinder libraryFinder = new LibraryFinder();

    /**
     * The class loader
     */
    protected TreeClassLoader classLoader;

    /**
     * The methods
     */
    protected static Map methods = new HashMap();
    List localMethods = new LinkedList();

    /**
     * The explicit constructor call parameters
     */
    protected static Map constructorParameters = new HashMap();
    List localConstructorParameters = new LinkedList();

    /**
     * Used to generate classes
     */
    protected static int nClass;

    protected Context nameVisitorContext;
    protected Context checkVisitorContext;
    protected Context evalVisitorContext;

    /**
     * Track the state of calls to 'setAccessible'
     * @see setAccessible(boolean)
     */
    protected boolean accessible;

    /**
     * Creates a new interpreter
     * @param pf the parser factory
     */
    public TreeInterpreter(ParserFactory pf) {
        this(pf, null);
    }

    /**
     * Creates a new interpreter
     * @param pf the parser factory
     * @param cl the auxiliary class loader used to load external classes
     */
    public TreeInterpreter(ParserFactory pf, ClassLoader cl) {
	parserFactory       = pf;
	classLoader         = new TreeClassLoader(this, cl);
	nameVisitorContext  = new GlobalContext(this);
	nameVisitorContext.setAdditionalClassLoaderContainer(classLoader);
	checkVisitorContext = new GlobalContext(this);
	checkVisitorContext.setAdditionalClassLoaderContainer(classLoader);
	evalVisitorContext  = new GlobalContext(this);
	evalVisitorContext.setAdditionalClassLoaderContainer(classLoader);
    }

    /**
     * Runs the interpreter
     * @param is    the reader from which the statements are read
     * @param fname the name of the parsed stream
     * @return the result of the evaluation of the last statement
     */
    public Object interpret(Reader r, String fname) throws InterpreterException {
	try {
	    SourceCodeParser p = parserFactory.createParser(r, fname);
	    List    statements = p.parseStream();
	    ListIterator    it = statements.listIterator();
	    Object result = null;

	    while (it.hasNext()) {
		Node n = (Node)it.next();

		Visitor v = new NameVisitor(nameVisitorContext);
		Object o = n.acceptVisitor(v);
		if (o != null) {
		    n = (Node)o;
		}

		v = new TypeChecker(checkVisitorContext);
		n.acceptVisitor(v);

		evalVisitorContext.defineVariables
		    (checkVisitorContext.getCurrentScopeVariables());

		v = new EvaluationVisitor(evalVisitorContext);
		result = n.acceptVisitor(v);
	    }

	    return result;
	} catch (ExecutionError e) {
	    throw new InterpreterException(e);
	} catch (ParseError e) {
	    throw new InterpreterException(e);
	}
    }
    
    /**
     * Runs the interpreter
     * @param is    the input stream from which the statements are read
     * @param fname the name of the parsed stream
     * @return the result of the evaluation of the last statement
     */
    public Object interpret(InputStream is, String fname) throws InterpreterException {
	return interpret(new InputStreamReader(is), fname);
    }
    
    /**
     * Runs the interpreter
     * @param fname the name of a file to interpret
     * @return the result of the evaluation of the last statement
     */
    public Object interpret(String fname) throws InterpreterException, IOException {
	return interpret(new FileReader(fname), fname);
    }
    
    /**
     * Parses a script and creates the associated syntax trees.
     * @param is    the reader from which the statements are read
     * @param fname the name of the parsed stream
     * @return list of statements 
     */
    public List buildStatementList (Reader r, String fname) throws InterpreterException {
	List resultingList;
	try {
	    SourceCodeParser p = parserFactory.createParser(r, fname);
	    List    statements = p.parseStream();
	    ListIterator    it = statements.listIterator();

            resultingList = new ArrayList();
	    while (it.hasNext()) {
		Node n = (Node)it.next();
		Visitor v = new NameVisitor(nameVisitorContext);
		Object o = n.acceptVisitor(v);
		if (o != null) { 
		    n = (Node)o;
		}
		resultingList.add(n);
		v = new TypeChecker(checkVisitorContext);
		n.acceptVisitor(v);
		
		evalVisitorContext.defineVariables
		    (checkVisitorContext.getCurrentScopeVariables());
	    }

	    return resultingList;
	} catch (ParseError e) {
	    throw new InterpreterException(e);
	}
    }

    /**
     * Runs the interpreter on a statement list.
     * @param statements the statement list to evaluate
     * @param fname the name of the parsed stream
     * @return the result of the evaluation of the last statement
     */
    public Object interpret(List statements) throws InterpreterException {
        try {
	    ListIterator it = statements.listIterator();
            Object   result = null;

            while (it.hasNext()) {
                Node n = (Node)it.next();
                Visitor v = new EvaluationVisitor(evalVisitorContext);
                result = n.acceptVisitor(v);
            }

            return result;
        } catch (ExecutionError e) {
            throw new InterpreterException(e);
        } catch (ParseError e) {
	    throw new InterpreterException(e);
        }
    }
 
    /**
     * Defines a variable in the interpreter environment
     * @param name  the variable's name
     * @param value the initial value of the variable
     * @param c the variable's type.
     * @exception IllegalStateException if name is already defined
     */
    public void defineVariable(String name, Object value, Class c) {
	nameVisitorContext.define(name, c);
	checkVisitorContext.define(name, c);
	evalVisitorContext.define(name, value);
    }

    /**
     * Defines a variable in the interpreter environment
     * @param name  the variable's name
     * @param value the initial value of the variable
     * @exception IllegalStateException if name is already defined
     */
    public void defineVariable(String name, Object value) {
	defineVariable(name, value, (value == null) ? null : value.getClass());
    }

    /**
     * Defines a boolean variable in the interpreter environment
     * @param name  the variable's name
     * @param value the initial value of the variable
     * @exception IllegalStateException if name is already defined
     */
    public void defineVariable(String name, boolean value) {
	Class c = boolean.class;
	nameVisitorContext.define(name, c);
	checkVisitorContext.define(name, c);
	evalVisitorContext.define(name, new Boolean(value));
    }

    /**
     * Defines a byte variable in the interpreter environment
     * @param name  the variable's name
     * @param value the initial value of the variable
     * @exception IllegalStateException if name is already defined
     */
    public void defineVariable(String name, byte value) {
	Class c = byte.class;
	nameVisitorContext.define(name, c);
	checkVisitorContext.define(name, c);
	evalVisitorContext.define(name, new Byte(value));
    }

    /**
     * Defines a short variable in the interpreter environment
     * @param name  the variable's name
     * @param value the initial value of the variable
     * @exception IllegalStateException if name is already defined
     */
    public void defineVariable(String name, short value) {
	Class c = short.class;
	nameVisitorContext.define(name, c);
	checkVisitorContext.define(name, c);
	evalVisitorContext.define(name, new Short(value));
    }

    /**
     * Defines a char variable in the interpreter environment
     * @param name  the variable's name
     * @param value the initial value of the variable
     * @exception IllegalStateException if name is already defined
     */
    public void defineVariable(String name, char value) {
	Class c = char.class;
	nameVisitorContext.define(name, c);
	checkVisitorContext.define(name, c);
	evalVisitorContext.define(name, new Character(value));
    }

    /**
     * Defines an int variable in the interpreter environment
     * @param name  the variable's name
     * @param value the initial value of the variable
     * @exception IllegalStateException if name is already defined
     */
    public void defineVariable(String name, int value) {
	Class c = int.class;
	nameVisitorContext.define(name, c);
	checkVisitorContext.define(name, c);
	evalVisitorContext.define(name, new Integer(value));
    }

    /**
     * Defines an long variable in the interpreter environment
     * @param name  the variable's name
     * @param value the initial value of the variable
     * @exception IllegalStateException if name is already defined
     */
    public void defineVariable(String name, long value) {
	Class c = long.class;
	nameVisitorContext.define(name, c);
	checkVisitorContext.define(name, c);
	evalVisitorContext.define(name, new Long(value));
    }

    /**
     * Defines an float variable in the interpreter environment
     * @param name  the variable's name
     * @param value the initial value of the variable
     * @exception IllegalStateException if name is already defined
     */
    public void defineVariable(String name, float value) {
	Class c = float.class;
	nameVisitorContext.define(name, c);
	checkVisitorContext.define(name, c);
	evalVisitorContext.define(name, new Float(value));
    }

    /**
     * Defines an double variable in the interpreter environment
     * @param name  the variable's name
     * @param value the initial value of the variable
     * @exception IllegalStateException if name is already defined
     */
    public void defineVariable(String name, double value) {
	Class c = double.class;
	nameVisitorContext.define(name, c);
	checkVisitorContext.define(name, c);
	evalVisitorContext.define(name, new Double(value));
    }

    /**
     * Sets the value of a variable
     * @param name  the variable's name
     * @param value the value of the variable
     * @exception IllegalStateException if the assignment is invalid
     */
    public void setVariable(String name, Object value) {
	Class c = (Class)checkVisitorContext.get(name);
	if (InterpreterUtilities.isValidAssignment(c, value)) {
	    evalVisitorContext.set(name, value);
	} else {
	    throw new IllegalStateException(name);
	}
    }

    /**
     * Gets the value of a variable
     * @param name  the variable's name
     * @exception IllegalStateException if the variable do not exist
     */
    public Object getVariable(String name) {
	return evalVisitorContext.get(name);
    }

    /**
     * Gets the class of a variable
     * @param name  the variable's name
     * @exception IllegalStateException if the variable do not exist
     */
    public Class getVariableClass(String name) {
	return (Class)checkVisitorContext.get(name);
    }

    /**
     * Returns the defined variable names
     * @return a set of strings
     */
    public Set getVariableNames() {
	return evalVisitorContext.getCurrentScopeVariableNames();
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
        nameVisitorContext.setAccessible(accessible);
        checkVisitorContext.setAccessible(accessible);
        evalVisitorContext.setAccessible(accessible);
    }
    
    public boolean getAccessible() {
        return accessible;
    }

    /**
     * Returns the defined class names
     * @return a set of strings
     */
    public Set getClassNames() {
	return classLoader.getClassNames();
    }

    /**
     * Adds a class search path
     * @param path the path to add
     */
    public void addClassPath(String path) {
	try {
	    classLoader.addURL(new File(path).toURL());
	} catch (MalformedURLException e) {
	}
    }

    /**
     * Adds a class search URL
     * @param url the url to add
     */
    public void addClassURL(URL url) {
	classLoader.addURL(url);
    }

    /**
     * Adds a library search path
     * @param path the path to add
     */
    public void addLibraryPath(String path) {
	libraryFinder.addPath(path);
    }
    
    /**
     * Adds a library file suffix
     * @param s the suffix to add
     */
    public void addLibrarySuffix(String s) {
	libraryFinder.addSuffix(s);
    }

    /**
     * Loads an interpreted class
     * @param s the fully qualified name of the class to load
     * @exception ClassNotFoundException if the class cannot be find
     */
    public Class loadClass(String name) throws ClassNotFoundException {
	return new TreeCompiler(this).compile(name);
    }

    /**
     * Converts an array of bytes into an instance of the class Class
     * @exception ClassFormatError if the class cannot be defined
     */
    public Class defineClass(String name, byte[] code) {
	return classLoader.defineClass(name, code);
    }

    /**
     * Gets the class loader
     */
    public ClassLoader getClassLoader() {
	return classLoader;
    }

    /**
     * Gets the library finder
     */
    public LibraryFinder getLibraryFinder() {
	return libraryFinder;
    }

    /**
     * Gets the parser factory
     */
    public ParserFactory getParserFactory() {
	return parserFactory;
    }

    /**
     * Returns the class of the execution exception
     */
    public Class getExceptionClass() {
	return CatchedExceptionError.class;
    }

    /**
     * Registers a method.
     * @param sig    the method's signature
     * @param md     the method declaration
     * @param im     the importation manager
     */
    public void registerMethod(String             sig,
			       MethodDeclaration  md,
                               ImportationManager im) {
	localMethods.add(sig);
        methods.put(sig, new MethodDescriptor(md, im));
    }

    /**
     * Interprets the body of a method
     * @param key the key used to find the body of a method
     * @param obj the object (this)
     * @param params the arguments
     */
    public static Object invokeMethod(String key, Object obj, Object[] params) {
	MethodDescriptor md = (MethodDescriptor)methods.get(key);
        Class c = null;
        try {
            c = Class.forName(key.substring(0, key.lastIndexOf('#')),
                              true, md.interpreter.getClassLoader());
        } catch (ClassNotFoundException e) {
	    // Should never append
            e.printStackTrace();
        }

	return md.interpreter.interpretMethod(c, md, obj, params);
    }

    /**
     * Interprets the body of a method
     * @param c the declaring class of the method
     * @param md the method descriptor
     * @param obj the object (this)
     * @param params the arguments
     */
    protected Object interpretMethod(Class c,
				   MethodDescriptor md,
				   Object obj,
				   Object[] params) {
	MethodDeclaration meth    = md.method;
	List              mparams = meth.getParameters();
	List              stmts   = meth.getBody().getStatements();
	String            name    = meth.getName();

	Context           context = null;

	if (Modifier.isStatic(md.method.getAccessFlags())) {
	    if (md.variables == null) {
		md.importationManager.setClassLoader(classLoader);

		// pass 1: names resolution
		Context ctx = new StaticContext(this, c, md.importationManager);
		ctx.setAdditionalClassLoaderContainer(classLoader);
		Visitor v = new NameVisitor(ctx);

		ListIterator it = mparams.listIterator();
		while (it.hasNext()) {
		    ((Node)it.next()).acceptVisitor(v);
		}

		it = stmts.listIterator();
		while (it.hasNext()) {
		    Object o = ((Node)it.next()).acceptVisitor(v);
		    if (o != null) {
			it.set(o);
		    }
		}

		// pass 2: type checking
		ctx = new StaticContext(this, c, md.importationManager);
		ctx.setAdditionalClassLoaderContainer(classLoader);
		v = new TypeChecker(ctx);

		it = mparams.listIterator();
		while (it.hasNext()) {
		    ((Node)it.next()).acceptVisitor(v);
		}

		it = stmts.listIterator();
		while (it.hasNext()) {
		    ((Node)it.next()).acceptVisitor(v);
		}

		md.variables = ctx.getCurrentScopeVariables();

		// Test of the additional context existence
		if (!name.equals("<clinit>") &&
		    !name.equals("<init>")) {
		    try {
			md.contextField = c.getField("local$Variables$Reference$0");
		    } catch (NoSuchFieldException e) {
		    }
		}
	    }

	    // pass 3: evaluation
	    context = new StaticContext(this, c, md.variables);
	} else {
	    if (md.variables == null) {
		md.importationManager.setClassLoader(classLoader);

		// pass 1: names resolution
		Context ctx = new MethodContext(this, c, c, md.importationManager);
		ctx.setAdditionalClassLoaderContainer(classLoader);
		Visitor v = new NameVisitor(ctx);

		Context ctx2 = new MethodContext(this, c, c, md.importationManager);
		ctx2.setAdditionalClassLoaderContainer(classLoader);
		Visitor v2 = new NameVisitor(ctx2);

		// Initializes the context with the outerclass variables
		Object[][] cc = null;
                try {
                    Field f = c.getField("local$Variables$Class$0");
                    cc = (Object[][])f.get(obj);
                    for (int i = 0; i < cc.length; i++) {
                        Object[] cell = cc[i];
			if (!((String)cell[0]).equals("this")) {
			    ctx.defineConstant((String)cell[0], cell[1]);
			}
                    }
                } catch (Exception e) {
                }
 
		// Visit the parameters and the body of the method
		ListIterator it = mparams.listIterator();
		while (it.hasNext()) {
		    ((Node)it.next()).acceptVisitor(v);
		}

		it = stmts.listIterator();
		while (it.hasNext()) {
		    Node n = (Node)it.next();
		    Object o = null;
		    if (n.hasProperty(NodeProperties.INSTANCE_INITIALIZER)) {
			o = n.acceptVisitor(v2);
		    } else {
			o = n.acceptVisitor(v);
		    }
		    if (o != null) {
			it.set(o);
		    }
		}

		// pass 2: type checking
		ctx = new MethodContext(this, c, c, md.importationManager);
		ctx.setAdditionalClassLoaderContainer(classLoader);
		v = new TypeChecker(ctx);

		ctx2 = new MethodContext(this, c, c, md.importationManager);
		ctx2.setAdditionalClassLoaderContainer(classLoader);
		v2 = new TypeChecker(ctx2);

		// Initializes the context with outerclass variables
		if (cc != null) {
                    for (int i = 0; i < cc.length; i++) {
                        Object[] cell = cc[i];
			if (!((String)cell[0]).equals("this")) {
			    ctx.defineConstant((String)cell[0], cell[1]);
			}
                    }
                }
		
		// Visit the parameters and the body of the method
		it = mparams.listIterator();
		while (it.hasNext()) {
		    ((Node)it.next()).acceptVisitor(v);
		}

		it = stmts.listIterator();
		while (it.hasNext()) {
		    Node n = (Node)it.next();
		    if (n.hasProperty(NodeProperties.INSTANCE_INITIALIZER)) {
			n.acceptVisitor(v2);
		    } else {
			n.acceptVisitor(v);
		    }
		}

		md.variables = ctx.getCurrentScopeVariables();

		// Test of the additional context existence
		if (!name.equals("<clinit>") &&
		    !name.equals("<init>")) {
		    try {
			md.contextField = c.getField("local$Variables$Reference$0");
		    } catch (NoSuchFieldException e) {
		    }
		}
	    }

	    // pass 3: evaluation
	    context = new MethodContext(this, c, obj, md.variables);
	}

	context.setAdditionalClassLoaderContainer(classLoader);

	// Set the arguments values
	Iterator it  = mparams.iterator();
	int      i   = 0;
	while (it.hasNext()) {
	    context.set(((FormalParameter)it.next()).getName(), params[i++]);
	}

	// Set the final local variables values
	if (md.contextField != null) {
	    Map vars = null;
	    try {
		vars = (Map)md.contextField.get(obj);
	    } catch (IllegalAccessException e) {
	    }
	    if (vars != null) {
		it = vars.keySet().iterator();
		while (it.hasNext()) {
		    String s = (String)it.next();
		    if (!s.equals("this")) {
			context.setConstant(s, vars.get(s));
		    }
		}
	    }
	}

	Visitor v = new EvaluationVisitor(context);
	it = stmts.iterator();
	    
	try {
	    while (it.hasNext()) {
		((Node)it.next()).acceptVisitor(v);
	    }
	} catch (ReturnException e) {
	    return e.getValue();
	}
	return null;
    }

    /**
     * Registers a constructor arguments
     */
    public void registerConstructorArguments(String             sig,
					     List               params,
					     List               exprs,
					     ImportationManager im) {
	localConstructorParameters.add(sig);
	constructorParameters.put(sig, new ConstructorParametersDescriptor
				  (params, exprs, im));
    }

    /**
     * This method is used to implement constructor invocation.
     * @param key  the key used to find the informations about the constructor
     * @param args the arguments passed to this constructor
     * @return the arguments to give to the 'super' or 'this' constructor
     *         followed by the new values of the constructor arguments
     */
    public static Object[] interpretArguments(String key, Object[] args) {
	ConstructorParametersDescriptor cpd = 
	    (ConstructorParametersDescriptor)constructorParameters.get(key);
	Class c = null;
	try {
	    c = Class.forName(key.substring(0, key.lastIndexOf('#')),
			      true, cpd.interpreter.getClassLoader());
	} catch (ClassNotFoundException e) {
	    // Should never append
	    e.printStackTrace();
	}

	return cpd.interpreter.interpretArguments(c, cpd, args);
    }

     /**
     * This method is used to implement constructor invocation.
     * @param c the declaring class of the constructor
     * @param cpd the parameter descriptor
     * @param args the arguments passed to this constructor
     * @return the arguments to give to the 'super' or 'this' constructor
     *         followed by the new values of the constructor arguments
     */
    protected Object[] interpretArguments(Class c,
					ConstructorParametersDescriptor cpd,
					Object[] args) {
	if (cpd.variables == null) {
	    cpd.importationManager.setClassLoader(classLoader);

	    Context ctx = new StaticContext(this, c, cpd.importationManager);
	    ctx.setAdditionalClassLoaderContainer(classLoader);
	    Visitor nv = new NameVisitor(ctx);
	    Visitor tc = new TypeChecker(ctx);

	    // Check the parameters
	    if (cpd.parameters != null) {
		ListIterator it = cpd.parameters.listIterator();
		while (it.hasNext()) {
		    ((Node)it.next()).acceptVisitor(tc);
		}
	    }

	    if (cpd.arguments != null) {
		ListIterator it = cpd.arguments.listIterator();
		while (it.hasNext()) {
		    Node   root = (Node)it.next();
		    Object res  = root.acceptVisitor(nv);
		    if (res != null) {
			it.set(res);
		    }
		}

		it = cpd.arguments.listIterator();
		while (it.hasNext()) {
		    ((Node)it.next()).acceptVisitor(tc);
		}
	    }
	    cpd.variables = ctx.getCurrentScopeVariables();
	}

	Context ctx = new StaticContext(this, c, cpd.variables);
	ctx.setAdditionalClassLoaderContainer(classLoader);

	// Set the arguments values
	if (cpd.parameters != null) {
	    Iterator it  = cpd.parameters.iterator();
	    int      i   = 0;
	    while (it.hasNext()) {
		ctx.set(((FormalParameter)it.next()).getName(), args[i++]);
	    }
	}
	
	Object[] result = new Object[0];
	
	if (cpd.arguments != null) {
	    Visitor v = new EvaluationVisitor(ctx);
	    ListIterator it = cpd.arguments.listIterator();
	    result = new Object[cpd.arguments.size()];
	    int i = 0;
	    while (it.hasNext()) {
		result[i++] = ((Node)it.next()).acceptVisitor(v);
	    }
	}
	
	return result;
    }

    /**
     * Called before the destruction of the interpreter
     */
    protected void finalize() throws Throwable {
	Iterator it = localMethods.iterator();
	while (it.hasNext()) {
	    methods.remove(it.next());
	}
	it = localConstructorParameters.iterator();
	while (it.hasNext()) {
	    constructorParameters.remove(it.next());
	}
    }

    /**
     * Used to store the informations about dynamically
     * created methods
     */
    protected class MethodDescriptor {
	Set                variables;
	MethodDeclaration  method;
        ImportationManager importationManager;
	TreeInterpreter    interpreter;    
	Field              contextField;

        /**
         * Creates a new descriptor
         */
        MethodDescriptor(MethodDeclaration md, ImportationManager im) {
	    method             = md;
            importationManager = im;
	    interpreter        = TreeInterpreter.this;
        }
    }

    /**
     * Used to store the informations about explicit constructors
     * invocation
     */
    protected class ConstructorParametersDescriptor {
	Set                variables;
	List               parameters;
	List               arguments;
	ImportationManager importationManager;
	TreeInterpreter    interpreter;    

	/**
	 * Creates a new descriptor
	 */
	ConstructorParametersDescriptor(List params, List args, ImportationManager im) {
	    parameters         = params;
	    arguments          = args;
            importationManager = im;
	    interpreter        = TreeInterpreter.this;
	}
    }
}
