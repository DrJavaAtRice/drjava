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

import java.lang.reflect.*;
import java.util.*;

import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.modifier.*;
import koala.dynamicjava.interpreter.throwable.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;

/**
 * This tree visitor checks the typing rules and loads
 * the classes, fields and methods
 *
 * @author Stephane Hillion
 * @version 1.2 - 1999/11/20
 */

public class TypeChecker extends VisitorObject {
    /**
     * The context
     */
    private Context context;

    /**
     * Creates a new name visitor
     * @param ctx the context
     */
    public TypeChecker(Context ctx) {
	context = ctx;
    }

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
            }
        }
        return null;
    }

    /**
     * Visits a WhileStatement
     * @param node the node to visit
     */
    public Object visit(WhileStatement node) {
        // Check the condition
        if (node.getCondition().acceptVisitor(this) != boolean.class) {
            throw new ExecutionError("condition.type", node);
        }

        node.getBody().acceptVisitor(this);
        return null;
    }

    /**
     * Visits a ForStatement
     * @param node the node to visit
     */
    public Object visit(ForStatement node) {
        // Enter a new scope
        context.enterScope();

        List l;
        // Check the statements
        if ((l = node.getInitialization()) != null) {
            checkList(l);
        }

	Node cond = node.getCondition();
        if (cond != null) {
            // Check the condition
            if (cond.acceptVisitor(this) != boolean.class) {
                throw new ExecutionError("condition.type", node);
            }
        }

        if ((l = node.getUpdate()) != null) {
            checkList(l);
        }

        node.getBody().acceptVisitor(this);

        // Leave the current scope and store the defined variables
        // (a map of String-Class mappings) in the "variables" property
        node.setProperty(NodeProperties.VARIABLES, context.leaveScope());
        return null;
    }

    /**
     * Visits a DoStatement
     * @param node the node to visit
     */
    public Object visit(DoStatement node) {
        node.getBody().acceptVisitor(this);

        // Check the condition
        if (node.getCondition().acceptVisitor(this) != boolean.class) {
            throw new ExecutionError("condition.type", node);
        }
        return null;
    }

    /**
     * Visits a SwitchStatement
     * @param node the node to visit
     */
    public Object visit(SwitchStatement node) {
        // Visits the components of this node
        Class c = (Class)node.getSelector().acceptVisitor(this);
        if (c != char.class && c != byte.class && c != short.class && c != int.class) {
	    node.setProperty(NodeProperties.ERROR_STRINGS,
			     new String[] { c.getName() });
            throw new ExecutionError("selector.type", node);
        }

        // Check the type of the case labels
        Iterator it = node.getBindings().iterator();
        while (it.hasNext()) {
	    SwitchBlock sb = (SwitchBlock)it.next();
	    sb.acceptVisitor(this);
            Expression exp = sb.getExpression();
            if (exp != null) {
                Class lc = NodeProperties.getType(exp);
                if (lc != char.class &&  lc != byte.class &&
                    lc != short.class && lc != int.class) {
		    node.setProperty(NodeProperties.ERROR_STRINGS,
				     new String[] { lc.getName() });
                    throw new ExecutionError("switch.label.type", node);
                }
                if (c != lc) {
                    Number n = null;
                    if (exp.hasProperty(NodeProperties.VALUE)) {
                        Object cst = exp.getProperty(NodeProperties.VALUE);
                        if (lc == char.class) {
                            n = new Integer(((Character)cst).charValue());
                        } else {
                            n = (Number)cst;
                        }
                    }
                    if (c == byte.class) {
                        if (exp.hasProperty(NodeProperties.VALUE)) {
                            if (n.byteValue() != n.intValue()) {
				node.setProperty(NodeProperties.ERROR_STRINGS,
						 new String[] { c.getName() });
				throw new ExecutionError
                                    ("switch.label.type", node);
                            }
                        } else {
                            throw new ExecutionError("switch.label.type", node);
                        }
                    } else if (c == short.class || c == char.class) {
                        if (exp.hasProperty(NodeProperties.VALUE)) {
                            if (n.shortValue() != n.intValue()) {
				node.setProperty(NodeProperties.ERROR_STRINGS,
						 new String[] { c.getName() });
                                throw new ExecutionError
                                    ("switch.label.type", node);
                            }
                        } else if (lc == int.class) {
			    node.setProperty(NodeProperties.ERROR_STRINGS,
					     new String[] { c.getName() });
                            throw new ExecutionError("switch.label.type", node);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Visits a SwitchBlock
     * @param node the node to visit
     */
    public Object visit(SwitchBlock node) {
	Expression exp = node.getExpression();
	if (exp != null) {
	    exp.acceptVisitor(this);
	}
	List l;
	if ((l = node.getStatements()) != null) {
	    checkList(l);
	}
	return null;
    }

    /**
     * Visits a LabeledStatement
     * @param node the node to visit
     */
    public Object visit(LabeledStatement node) {
        node.getStatement().acceptVisitor(this);
        return null;
    }

    /**
     * Visits a TryStatement
     * @param node the node to visit
     */
    public Object visit(TryStatement node) {
        node.getTryBlock().acceptVisitor(this);
        Iterator it = node.getCatchStatements().iterator();
        while (it.hasNext()) {
            ((Node)it.next()).acceptVisitor(this);
        }
        Node n;
        if ((n = node.getFinallyBlock()) != null) {
            n.acceptVisitor(this);
        }
        return null;
    }

    /**
     * Visits a CatchStatement
     * @param node the node to visit
     */
    public Object visit(CatchStatement node) {
	// Enter a new scope
	context.enterScope();

	Class c = (Class)node.getException().acceptVisitor(this);
	if (!Throwable.class.isAssignableFrom(c)) {
	    node.setProperty(NodeProperties.ERROR_STRINGS,
			     new String[] { c.getName() });
	    throw new ExecutionError("catch.type", node);
	}

	node.getBlock().acceptVisitor(this);

	// Leave the current scope
	context.leaveScope();
	node.setProperty(NodeProperties.TYPE, c);
	return null;
    }

    /**
     * Visits a ThrowStatement
     * @param node the node to visit
     */
    public Object visit(ThrowStatement node) {
        Class c = (Class)node.getExpression().acceptVisitor(this);
        if (!Throwable.class.isAssignableFrom(c)) {
	    node.setProperty(NodeProperties.ERROR_STRINGS,
			     new String[] { c.getName() });
            throw new ExecutionError("throw.type", node);
        }
        return null;
    }

    /**
     * Visits a ReturnStatement
     * @param node the node to visit
     */
    public Object visit(ReturnStatement node) {
        Expression e = node.getExpression();
        if (e != null) {
            e.acceptVisitor(this);
        }
        return null;
    }

    /**
     * Visits an IfThenStatement
     * @param node the node to visit
     */
    public Object visit(IfThenStatement node) {
        // Check the condition
        if (node.getCondition().acceptVisitor(this) != boolean.class) {
            throw new ExecutionError("condition.type", node);
        }

        node.getThenStatement().acceptVisitor(this);
        return null;
    }

    /**
     * Visits an IfThenElseStatement
     * @param node the node to visit
     */
    public Object visit(IfThenElseStatement node) {
        // Check the condition
        if (node.getCondition().acceptVisitor(this) != boolean.class) {
            throw new ExecutionError("condition.type", node);
        }

        node.getThenStatement().acceptVisitor(this);
        node.getElseStatement().acceptVisitor(this);
        return null;
    }

    /**
     * Visits a SynchronizedStatement
     * @param node the node to visit
     */
    public Object visit(SynchronizedStatement node) {
        // Check the lock
        if (((Class)node.getLock().acceptVisitor(this)).isPrimitive()) {
            throw new ExecutionError("lock.type", node);
        }

        node.getBody().acceptVisitor(this);
        return null;
    }

    /**
     * Visits a Literal
     * @param node the node to visit
     */
    public Object visit(Literal node) {
        // Set the properties of the node 
	Class c = node.getType();
        node.setProperty(NodeProperties.VALUE, node.getValue());
        node.setProperty(NodeProperties.TYPE, c);
        return c;
    }

    /**
     * Visits a VariableDeclaration
     * @param node the node to visit
     */
    public Object visit(VariableDeclaration node) {
        // Define the variable
        Class lc = (Class)node.getType().acceptVisitor(this);
        if (node.isFinal()) {
            context.defineConstant(node.getName(), lc);
        } else {
            context.define(node.getName(), lc);
        }

	Node init = node.getInitializer();
        if (init != null) {
            Class rc = (Class)init.acceptVisitor(this);
            checkAssignmentStaticRules(lc, rc, node, init);
        }
        return null;
    }

    /**
     * Visits a BlockStatement
     * @param node the node to visit
     */
    public Object visit(BlockStatement node) {
        // Enter a new scope
        context.enterScope();

        // Do the type checking of the nested statements
        checkList(node.getStatements());

        // Leave the current scope and store the defined variables
        // (a map of String-Class mappings) in the "variables" property
        node.setProperty(NodeProperties.VARIABLES, context.leaveScope());
        
        return null;
    }

    /**
     * Visits an ObjectFieldAccess
     * @param node the node to visit
     */
    public Object visit(ObjectFieldAccess node) {
        // Visit the expression
        Class c = (Class)node.getExpression().acceptVisitor(this);
	
        // Load the field object
        if (!c.isArray()) {
            Field f = null;
            try {
                f = context.getField(c, node.getFieldName());
            } catch (Exception e) {
                throw new CatchedExceptionError(e, node);
            }

	    // Set the node properties
	    node.setProperty(NodeProperties.FIELD, f);
	    node.setProperty(NodeProperties.TYPE,  c = f.getType());

	    node.setProperty(NodeProperties.MODIFIER, context.getModifier(node));
	    return c;
	} else {
            if (!node.getFieldName().equals("length")) {
		String s0 = "length";
		String s1 = c.getComponentType().getName() + " array";
		node.setProperty(NodeProperties.ERROR_STRINGS, new String[] { s0, s1 });
                throw new ExecutionError("no.such.field", node);
            }
            node.setProperty(NodeProperties.TYPE,  int.class);
            node.setProperty(NodeProperties.MODIFIER, new InvalidModifier(node));
	    return int.class;
	}
    }

    /**
     * Visits a SuperFieldAccess
     * @param node the node to visit
     */
    public Object visit(SuperFieldAccess node) {
	Field f = null;
	try {
	    f = context.getSuperField(node, node.getFieldName());
	} catch (Exception e) {
	    throw new CatchedExceptionError(e, node);
	}

	// Set the node properties
	node.setProperty(NodeProperties.FIELD, f);
	Class c;
	node.setProperty(NodeProperties.TYPE,  c = f.getType());
	node.setProperty(NodeProperties.MODIFIER, context.getModifier(node));
	return c;
    }

    /**
     * Visits a StaticFieldAccess
     * @param node the node to visit
     */
    public Object visit(StaticFieldAccess node) {
        // Visit the field type
        Class c = (Class)node.getFieldType().acceptVisitor(this);

        // Load the field object
        Field f = null;
        try {
            f = context.getField(c, node.getFieldName());
        } catch (Exception e) {
            throw new CatchedExceptionError(e, node);
        }

        // Set the node properties
        node.setProperty(NodeProperties.FIELD, f);
        node.setProperty(NodeProperties.TYPE,  c = f.getType());

	node.setProperty(NodeProperties.MODIFIER, context.getModifier(node));
	return c;
    }

    /**
     * Visits an ObjectMethodCall
     * @param node the node to visit
     */
    public Object visit(ObjectMethodCall node) {
        // Check the receiver
	Expression exp = node.getExpression();
	Class      c   = (Class)exp.acceptVisitor(this);
	String     mn  = node.getMethodName();

        if (!c.isArray() || (c.isArray() && !mn.equals("clone"))) {
            // Do the type checking of the arguments
            List args = node.getArguments();
            Class[] cargs = Constants.EMPTY_CLASS_ARRAY;
            if (args != null) {
                cargs = new Class[args.size()];
                Iterator it = args.iterator();
                int i  = 0;
                while (it.hasNext()) {
                    cargs[i++] = (Class)((Node)it.next()).acceptVisitor(this);
                }
            }
            Method m = null;
            try {
                m = context.lookupMethod(exp, mn, cargs);
            } catch (NoSuchMethodException e) {
		String s = c.getName();
		node.setProperty(NodeProperties.ERROR_STRINGS, new String[] { mn, s });
                throw new ExecutionError("no.such.method", node);
	    } catch (MethodModificationError e) {
		Expression expr = e.getExpression();
		expr.acceptVisitor(this);
		node.setExpression(expr);
		m = e.getMethod();
	    }

            // Set the node properties
            node.setProperty(NodeProperties.METHOD, m);
            node.setProperty(NodeProperties.TYPE,   c = m.getReturnType());
	    return c;
	} else {
            if (!mn.equals("clone") || node.getArguments() != null) {
		String s0 = "clone";
		String s1 = c.getComponentType().getName() + " array";
		node.setProperty(NodeProperties.ERROR_STRINGS, new String[] { s0, s1 });
                throw new ExecutionError("no.such.method", node);
            }
            node.setProperty(NodeProperties.TYPE, c = Object.class);
	    return c;
	}
    }

    /**
     * Visits a MethodDeclaration
     * @param node the node to visit
     */
    public Object visit(MethodDeclaration node) {
	context.defineFunction(node);

	node.setProperty(NodeProperties.TYPE, node.getReturnType().acceptVisitor(this));
	node.setProperty(NodeProperties.FUNCTIONS, context.getFunctions());
	node.setProperty(NodeProperties.IMPORTATION_MANAGER,
			 context.getImportationManager().clone());

	context.enterScope();
	checkList(node.getParameters());
	context.leaveScope();
	return null;
    }

    /**
     * Visits a FunctionCall
     * @param node the node to visit
     */
    public Object visit(FunctionCall node) {
	// Do the type checking of the arguments
	List args = node.getArguments();
	Class[] cargs = Constants.EMPTY_CLASS_ARRAY;
	if (args != null) {
	    cargs = new Class[args.size()];
	    Iterator it = args.iterator();
	    int i  = 0;
	    while (it.hasNext()) {
		cargs[i++] = (Class)((Node)it.next()).acceptVisitor(this);
	    }
	}
	MethodDeclaration f = null;
	try {
	    f = context.lookupFunction(node.getMethodName(), cargs);
	} catch (NoSuchFunctionException e) {
	    String s = node.getMethodName();
	    node.setProperty(NodeProperties.ERROR_STRINGS, new String[] { s });
	    throw new ExecutionError("no.such.function", node);
	}

	// Set the node properties
	Class c;
	node.setProperty(NodeProperties.FUNCTION, f);
	node.setProperty(NodeProperties.TYPE,  c = NodeProperties.getType(f));
	return c;
    }

    /**
     * Visits a SuperMethodCall
     * @param node the node to visit
     */
    public Object visit(SuperMethodCall node) {
	// Do the type checking of the arguments
        List args = node.getArguments();
        Class[] pt = Constants.EMPTY_CLASS_ARRAY;
        if (args != null) {
            pt = new Class[args.size()];
            Iterator it = args.iterator();
	    int i = 0;
            while (it.hasNext()) {
                pt[i++] = (Class)((Node)it.next()).acceptVisitor(this);
            }
        }
        Method m = null;
        try {
            m = context.lookupSuperMethod(node, node.getMethodName(), pt);
        } catch (Exception e) {
            throw new CatchedExceptionError(e, node);
        }
        
        // Set the node properties
	Class c;
        node.setProperty(NodeProperties.METHOD, m);
        node.setProperty(NodeProperties.TYPE,   c = m.getReturnType());
        return c;
    }

    /**
     * Visits a StaticMethodCall
     * @param node the node to visit
     */
    public Object visit(StaticMethodCall node) {
        // Do the type checking of the arguments
        List args = node.getArguments();
        Class[] cargs = Constants.EMPTY_CLASS_ARRAY;
        if (args != null) {
            cargs = new Class[args.size()];
            Iterator it = args.iterator();
            int      i  = 0;
            while (it.hasNext()) {
                cargs[i++] = (Class)((Node)it.next()).acceptVisitor(this);
            }
        }
        Method m = null;
        Node   n = node.getMethodType();
	Class  c = (Class)n.acceptVisitor(this);

        try {
            m = context.lookupMethod(n, node.getMethodName(), cargs);
        } catch (NoSuchMethodException e) {
	    String s0 = node.getMethodName();
	    String s1 = c.getName();
	    node.setProperty(NodeProperties.ERROR_STRINGS, new String[] { s0, s1 });
	    throw new ExecutionError("no.such.method", node);
        }

        // Set the node properties
        node.setProperty(NodeProperties.METHOD, m);
        node.setProperty(NodeProperties.TYPE,   c = m.getReturnType());
        return c;
    }

    /**
     * Visits a SimpleAssignExpression
     * @param node the node to visit
     */
    public Object visit(SimpleAssignExpression node) {
        Expression left  = node.getLeftExpression();
        Expression right = node.getRightExpression();
        Class rc = (Class)right.acceptVisitor(this);

        // Perhaps is this assignment a variable declaration ?
        if (left instanceof QualifiedName) {
            String var = ((QualifiedName)left).getRepresentation();
            if (!context.exists(var)) {
		context.define(var, (rc == null) ? Object.class : rc);
	    }
	}

        Class lc = (Class)left.acceptVisitor(this);

	// The left subexpression must be a variable
        if (!left.hasProperty(NodeProperties.MODIFIER)) {
            throw new ExecutionError("left.expression", node);
        }
	
        // Check the validity of the assignment
        checkAssignmentStaticRules(lc, rc, node, right);

        node.setProperty(NodeProperties.TYPE, lc);
	return lc;
    }

    /**
     * Visits a QualifiedName
     * @param node the node to visit
     */
    public Object visit(QualifiedName node) {
	String var = node.getRepresentation();
	
	// Set the modifier
	Class c = (Class)context.get(var);
	node.setProperty(NodeProperties.TYPE, c);
	
	node.setProperty(NodeProperties.MODIFIER, context.getModifier(node));
	return c;
    }

    /**
     * Visits a SimpleAllocation
     * @param node the node to visit
     */
    public Object visit(SimpleAllocation node) {
        // Check the type to declare
        Node type = node.getCreationType();
        Class c  = (Class)type.acceptVisitor(this);

        // Do the type checking of the arguments
        List args = node.getArguments();
        Class[] cargs = Constants.EMPTY_CLASS_ARRAY;

	if (args != null) {
	    cargs = new Class[args.size()];

	    ListIterator it = args.listIterator();
	    int i  = 0;
	    while (it.hasNext()) {
		cargs[i++] = (Class)((Node)it.next()).acceptVisitor(this);
	    }
	}

	return context.setProperties(node, c, cargs);
    }

    /**
     * Visits a InnerAllocation
     * @param node the node to visit
     */
    public Object visit(InnerAllocation node) {
        // Visit the expression
        Class ec = (Class)node.getExpression().acceptVisitor(this);

        // Check the type to declare
        Node type = node.getCreationType();
        if (type instanceof ReferenceType) {
            ReferenceType rt = (ReferenceType)type;
            rt.setRepresentation(ec.getName() + "$" + rt.getRepresentation());
        } else {
            throw new ExecutionError("allocation.type", node);
        }
        Class c = (Class)type.acceptVisitor(this);
	Class dc = InterpreterUtilities.getDeclaringClass(c);

        // Do the type checking of the arguments
        List args = node.getArguments();
        Class[] cargs = null;

        if (dc != null && dc.isAssignableFrom(ec)) {
            // Adds an argument if the class to build is an innerclass
            if (args != null) {
                cargs = new Class[args.size() + 1];
                
                cargs[0] = ec;
                ListIterator it = args.listIterator();
                int i  = 1;
                while (it.hasNext()) {
                    cargs[i++] = (Class)((Node)it.next()).acceptVisitor(this);
                }
            } else {
                cargs = new Class[] { ec };
            }
        } else {
            throw new ExecutionError("allocation.type", node);
        }
        Constructor cons = null;
        try {
            cons = context.lookupConstructor(c, cargs);
        } catch (Exception e) {
            throw new CatchedExceptionError(e, node);
        }
        
        // Set the properties of this node
        node.setProperty(NodeProperties.TYPE,        c);
        node.setProperty(NodeProperties.CONSTRUCTOR, cons);

        return c;
    }

    /**
     * Visits a ClassAllocation
     * @param node the node to visit
     */
    public Object visit(ClassAllocation node) {
	// If the class allocation is the initializer of a field,
	// it is possible that it has already been visited
	if (node.hasProperty(NodeProperties.TYPE)) {
	    return NodeProperties.getType(node);
	} else {
	    // Get the class to allocate
	    Node   ctn   = node.getCreationType();
	    Class   ct   = (Class)ctn.acceptVisitor(this);
	    List   largs = node.getArguments();
	    Class[] args = Constants.EMPTY_CLASS_ARRAY;

	    if (largs != null) {
		args = new Class[largs.size()];
		Iterator it = largs.iterator();
		int i = 0;
		while (it.hasNext()) {
		    args[i++] = (Class)((Node)it.next()).acceptVisitor(this);
		}
	    }
	    return context.setProperties(node, ct, args, node.getMembers());
	}
    }

    /**
     * Visits an ArrayAllocation
     * @param node the node to visit
     */
    public Object visit(ArrayAllocation node) {
        // Do the checking of the size expressions
        Iterator it = node.getSizes().iterator();
        while (it.hasNext()) {
            Class c = (Class)((Node)it.next()).acceptVisitor(this);
            // Dimension expression must be of an integral type, but not long
            if (c != byte.class && c != short.class && c != int.class) {
                throw new ExecutionError("array.dimension.type", node);
            }
        }
        
        Class c = (Class)node.getCreationType().acceptVisitor(this);

        // Visits the initializer if one
        if (node.getInitialization() != null) {
            node.getInitialization().acceptVisitor(this);
        }

        // Set the type properties of this node
        Class  ac = Array.newInstance(c, new int[node.getDimension()]).getClass();

        node.setProperty(NodeProperties.TYPE, ac);
        node.setProperty(NodeProperties.COMPONENT_TYPE, c);
        return ac;
    }

    /**
     * Visits a ArrayInitializer
     * @param node the node to visit
     */
    public Object visit(ArrayInitializer node) {
        node.getElementType().acceptVisitor(this);

        checkList(node.getCells());
        return null;
    }

    /**
     * Visits an ArrayAccess
     * @param node the node to visit
     */
    public Object visit(ArrayAccess node) {
        // Visits the expression on which this array access applies
        Class c = (Class)node.getExpression().acceptVisitor(this);

        if (!c.isArray()) {
	    node.setProperty(NodeProperties.ERROR_STRINGS,
			     new String[] { c.getName() });
            throw new ExecutionError("array.required", node);
        }
        
        // Sets the properties of this node
	Class result;
        node.setProperty(NodeProperties.TYPE, result = c.getComponentType());
        node.setProperty(NodeProperties.MODIFIER, new ArrayModifier(node));

        // Visits the cell number expression
        c = (Class)node.getCellNumber().acceptVisitor(this);

        // The index must be of an integral type, but not a long
        if (c != char.class && c != byte.class && c != short.class && c != int.class) {
            throw new ExecutionError("array.index.type", node);
        }
        return result;
    }

    /**
     * Visits a PrimitiveType
     * @param node the node to visit
     */
    public Object visit(PrimitiveType node) {
	Object o = node.getValue();
        node.setProperty(NodeProperties.TYPE, o);
        return o;
    }

    /**
     * Visits a ReferenceType
     * @param node the node to visit
     */
    public Object visit(ReferenceType node) {
	Class c = null;
	try {
	    c = context.lookupClass(node.getRepresentation());
	} catch (ClassNotFoundException e) {
	    node.setProperty(NodeProperties.ERROR_STRINGS,
			     new String[] { node.getRepresentation() });
	    throw new ExecutionError("undefined.class", node);
	}
	node.setProperty(NodeProperties.TYPE, c);
	return c;
    }
	
    /**
     * Visits a ArrayType
     * @param node the node to visit
     */
    public Object visit(ArrayType node) {
        Node eType = node.getElementType();
        Class c = (Class)eType.acceptVisitor(this);
        Class ac = Array.newInstance(c, 0).getClass();
        
        // Set the type property of this node
        node.setProperty(NodeProperties.TYPE, ac);
        return ac;
    }

    /**
     * Visits a TypeExpression
     * @param node the node to visit
     */
    public Object visit(TypeExpression node) {
	Class c = (Class)node.getType().acceptVisitor(this);
        node.setProperty(NodeProperties.TYPE, Class.class);
        node.setProperty(NodeProperties.VALUE, c);
	return Class.class;
    }

    /**
     * Visits a NotExpression
     * @param node the node to visit
     */
    public Object visit(NotExpression node) {
        // Check the type
        Node  n = node.getExpression();
        Class c = (Class)n.acceptVisitor(this);

        if (c != boolean.class) {
            throw new ExecutionError("not.expression.type", node);
        }
        node.setProperty(NodeProperties.TYPE, c);

        // Compute the expression if it is constant
        if (n.hasProperty(NodeProperties.VALUE)) {
            if (((Boolean)n.getProperty(NodeProperties.VALUE)).booleanValue()) {
                node.setProperty(NodeProperties.VALUE, Boolean.FALSE);
            } else {
                node.setProperty(NodeProperties.VALUE, Boolean.TRUE);
            }
        }
        return c;
    }

    /**
     * Visits a ComplementExpression
     * @param node the node to visit
     */
    public Object visit(ComplementExpression node) {
        // Check the type
        Node  n = node.getExpression();
        Class c = (Class)n.acceptVisitor(this);

        if (c == char.class || c == byte.class || c == short.class) {
            node.setProperty(NodeProperties.TYPE, c = int.class);
        } else if (c == int.class  || c == long.class) {
            node.setProperty(NodeProperties.TYPE, c);
        } else {
            throw new ExecutionError("complement.expression.type", node);
        }

        // Compute the expression if it is constant
        if (n.hasProperty(NodeProperties.VALUE)) {
            Object o = n.getProperty(NodeProperties.VALUE);
            if (o instanceof Character) {
                o = new Integer(((Character)o).charValue());
            }
            if (c == int.class) {
		o = new Integer(~((Number)o).intValue());
            } else {
		o = new Long(~((Number)o).longValue());
            }
	    node.setProperty(NodeProperties.VALUE, o);
        }
        return c;
    }

    /**
     * Visits a PlusExpression
     * @param node the node to visit
     */
    public Object visit(PlusExpression node) {
        Class c = visitUnaryOperation(node, "plus.expression.type");

        // Compute the expression if it is constant
        Node  n = node.getExpression();
        if (n.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.plus(c, n.getProperty(NodeProperties.VALUE)));
        }
        return c;
    }

    /**
     * Visits a MinusExpression
     * @param node the node to visit
     */
    public Object visit(MinusExpression node) {
        Class c = visitUnaryOperation(node, "minus.expression.type");

        // Compute the expression if it is constant
        Node  n = node.getExpression();
        if (n.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.minus(c, n.getProperty(NodeProperties.VALUE)));
        }
        return c;
    }

    /**
     * Visits an AddExpression
     * @param node the node to visit
     */
    public Object visit(AddExpression node) {
        // Check the types
        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();
        Class lc = (Class)ln.acceptVisitor(this);
        Class rc = (Class)rn.acceptVisitor(this);
	Class c  = String.class;

	if (lc != String.class && rc != String.class) {
	    c = visitNumericExpression(node, "addition.type");
	} else {
	    node.setProperty(NodeProperties.TYPE, c = String.class);
	}

        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.add(c,
                                          ln.getProperty(NodeProperties.VALUE),
                                          rn.getProperty(NodeProperties.VALUE)));
        }
	return c;
    }

    /**
     * Visits an AddAssignExpression
     * @param node the node to visit
     */
    public Object visit(AddAssignExpression node) {
        // Check the types
	Node  ln = node.getLeftExpression();
        Class lc = (Class)ln.acceptVisitor(this);
        Class rc = (Class)node.getRightExpression().acceptVisitor(this);

        if (lc != String.class) {
            if (lc == null || rc == null ||
                lc == void.class || rc == void.class ||
                rc == boolean.class || !rc.isPrimitive()) {
                throw new ExecutionError("addition.type", node);
            }
        }
        
        // The left subexpression must be a variable
        if (!ln.hasProperty(NodeProperties.MODIFIER)) {
            throw new ExecutionError("left.expression", node);
        }

        // Sets the type property of this node
        node.setProperty(NodeProperties.TYPE, lc);
        return lc;
    }

    /**
     * Visits a SubtractExpression
     * @param node the node to visit
     */
    public Object visit(SubtractExpression node) {
        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();
        ln.acceptVisitor(this);
        rn.acceptVisitor(this);
        Class c = visitNumericExpression(node, "subtraction.type");

        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.subtract(c,
                                               ln.getProperty(NodeProperties.VALUE),
                                               rn.getProperty(NodeProperties.VALUE)));
        }
        return c;
    }

    /**
     * Visits an SubtractAssignExpression
     * @param node the node to visit
     */
    public Object visit(SubtractAssignExpression node) {
        // Check the types
	Node  ln = node.getLeftExpression();
        Class lc = (Class)ln.acceptVisitor(this);
        Class rc = (Class)node.getRightExpression().acceptVisitor(this);

        if (lc == null          || rc == null          ||
            lc == void.class    || rc == void.class    ||
            lc == boolean.class || rc == boolean.class ||
            !lc.isPrimitive()   || !rc.isPrimitive()) {
            throw new ExecutionError("subtraction.type", node);
        }
        
        // The left subexpression must be a variable
        if (!ln.hasProperty(NodeProperties.MODIFIER)) {
            throw new ExecutionError("left.expression", node);
        }

        // Sets the type property of this node
        node.setProperty(NodeProperties.TYPE, lc);
        return lc;
    }

    /**
     * Visits a MultiplyExpression
     * @param node the node to visit
     */
    public Object visit(MultiplyExpression node) {
        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();
        ln.acceptVisitor(this);
        rn.acceptVisitor(this);
        Class c = visitNumericExpression(node, "multiplication.type");

        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.multiply(c,
                                               ln.getProperty(NodeProperties.VALUE),
                                               rn.getProperty(NodeProperties.VALUE)));
        }
        return c;
    }

    /**
     * Visits an MultiplyAssignExpression
     * @param node the node to visit
     */
    public Object visit(MultiplyAssignExpression node) {
        // Check the types
	Node  ln = node.getLeftExpression();
        Class lc = (Class)ln.acceptVisitor(this);
        Class rc = (Class)node.getRightExpression().acceptVisitor(this);

        if (lc == null          || rc == null          ||
            lc == void.class    || rc == void.class    ||
            lc == boolean.class || rc == boolean.class ||
            !lc.isPrimitive()   || !rc.isPrimitive()) {
            throw new ExecutionError("multiplication.type", node);
        }
        
        // The left subexpression must be a variable
        if (!ln.hasProperty(NodeProperties.MODIFIER)) {
            throw new ExecutionError("left.expression", node);
        }

        // Sets the type property of this node
        node.setProperty(NodeProperties.TYPE, lc);
        return lc;
    }

    /**
     * Visits a DivideExpression
     * @param node the node to visit
     */
    public Object visit(DivideExpression node) {
        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();
        ln.acceptVisitor(this);
        rn.acceptVisitor(this);
        Class c = visitNumericExpression(node, "division.type");

        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.divide(c,
                                             ln.getProperty(NodeProperties.VALUE),
                                             rn.getProperty(NodeProperties.VALUE)));
        }
        return c;
    }

    /**
     * Visits an DivideAssignExpression
     * @param node the node to visit
     */
    public Object visit(DivideAssignExpression node) {
        // Check the types
 	Node  ln = node.getLeftExpression();
        Class lc = (Class)ln.acceptVisitor(this);
        Class rc = (Class)node.getRightExpression().acceptVisitor(this);

        if (lc == null          || rc == null          ||
            lc == void.class    || rc == void.class    ||
            lc == boolean.class || rc == boolean.class ||
            !lc.isPrimitive()   || !rc.isPrimitive()) {
            throw new ExecutionError("division.type", node);
        }
        
        // The left subexpression must be a variable
        if (!ln.hasProperty(NodeProperties.MODIFIER)) {
            throw new ExecutionError("left.expression", node);
        }

        // Sets the type property of this node
        node.setProperty(NodeProperties.TYPE, lc);
        return lc;
    }

    /**
     * Visits a RemainderExpression
     * @param node the node to visit
     */
    public Object visit(RemainderExpression node) {
        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();
        ln.acceptVisitor(this);
        rn.acceptVisitor(this);
        Class c = visitNumericExpression(node, "remainder.type");

        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.remainder(c,
						ln.getProperty(NodeProperties.VALUE),
						rn.getProperty(NodeProperties.VALUE)));
        }
        return c;
    }

    /**
     * Visits an RemainderAssignExpression
     * @param node the node to visit
     */
    public Object visit(RemainderAssignExpression node) {
        // Check the types
 	Node  ln = node.getLeftExpression();
        Class lc = (Class)ln.acceptVisitor(this);
        Class rc = (Class)node.getRightExpression().acceptVisitor(this);

        if (lc == null          || rc == null          ||
            lc == void.class    || rc == void.class    ||
            lc == boolean.class || rc == boolean.class ||
            !lc.isPrimitive()   || !rc.isPrimitive()) {
            throw new ExecutionError("remainder.type", node);
        }
        
        // The left subexpression must be a variable
        if (!ln.hasProperty(NodeProperties.MODIFIER)) {
            throw new ExecutionError("left.expression", node);
        }

        // Sets the type property of this node
        node.setProperty(NodeProperties.TYPE, lc);
        return lc;
    }

    /**
     * Visits an EqualExpression
     * @param node the node to visit
     */
    public Object visit(EqualExpression node) {
        // Check the types
        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();
        Class lc = (Class)ln.acceptVisitor(this);
        Class rc = (Class)rn.acceptVisitor(this);

        checkEqualityStaticRules(lc, rc, node);
                                 
        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.equalTo(lc, rc,
                                              ln.getProperty(NodeProperties.VALUE),
                                              rn.getProperty(NodeProperties.VALUE)));
        }

        // Set the type property
        node.setProperty(NodeProperties.TYPE, boolean.class);
        return boolean.class;
    }

    /**
     * Visits an NotEqualExpression
     * @param node the node to visit
     */
    public Object visit(NotEqualExpression node) {
        // Check the types
        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();
        Class lc = (Class)ln.acceptVisitor(this);
        Class rc = (Class)rn.acceptVisitor(this);

        checkEqualityStaticRules(lc, rc, node);
                                 
        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.notEqualTo(lc, rc,
						 ln.getProperty(NodeProperties.VALUE),
						 rn.getProperty(NodeProperties.VALUE)));
        }

        // Set the type property
        node.setProperty(NodeProperties.TYPE, boolean.class);
        return boolean.class;
    }

    /**
     * Visits a LessExpression
     * @param node the node to visit
     */
    public Object visit(LessExpression node) {
        Class c = visitRelationalExpression(node);

        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();

        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.lessThan(ln.getProperty(NodeProperties.VALUE),
                                               rn.getProperty(NodeProperties.VALUE)));
        }
        return c;
    }

    /**
     * Visits a LessOrEqualExpression
     * @param node the node to visit
     */
    public Object visit(LessOrEqualExpression node) {
        Class c = visitRelationalExpression(node);

        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();

        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.lessOrEqual(ln.getProperty(NodeProperties.VALUE),
						  rn.getProperty(NodeProperties.VALUE)));
        }
        return c;
    }

    /**
     * Visits a GreaterExpression
     * @param node the node to visit
     */
    public Object visit(GreaterExpression node) {
        Class c = visitRelationalExpression(node);

        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();

        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.greaterThan(ln.getProperty(NodeProperties.VALUE),
                                               rn.getProperty(NodeProperties.VALUE)));
        }
        return c;
    }

    /**
     * Visits a GreaterOrEqualExpression
     * @param node the node to visit
     */
    public Object visit(GreaterOrEqualExpression node) {
        Class c = visitRelationalExpression(node);

        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();

        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.greaterOrEqual(ln.getProperty(NodeProperties.VALUE),
						  rn.getProperty(NodeProperties.VALUE)));
        }
        return c;
    }

    /**
     * Visits a BitAndExpression
     * @param node the node to visit
     */
    public Object visit(BitAndExpression node) {
        Class c = visitBitwiseExpression(node);

        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();

        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.bitAnd
                     (c,
                      ln.getProperty(NodeProperties.VALUE),
                      rn.getProperty(NodeProperties.VALUE)));
        }
        return c;
    }

    /**
     * Visits a BitAndAssignExpression
     * @param node the node to visit
     */
    public Object visit(BitAndAssignExpression node) {
        return visitBitwiseAssign(node);
    }

    /**
     * Visits a BitOrExpression
     * @param node the node to visit
     */
    public Object visit(BitOrExpression node) {
        Class c = visitBitwiseExpression(node);

        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();

        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.bitOr
                     (c,
                      ln.getProperty(NodeProperties.VALUE),
                      rn.getProperty(NodeProperties.VALUE)));
        }
        return c;
    }

    /**
     * Visits a BitOrAssignExpression
     * @param node the node to visit
     */
    public Object visit(BitOrAssignExpression node) {
        return visitBitwiseAssign(node);
    }

    /**
     * Visits a ExclusiveOrExpression
     * @param node the node to visit
     */
    public Object visit(ExclusiveOrExpression node) {
        Class c = visitBitwiseExpression(node);

        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();

        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.xOr
                     (c,
                      ln.getProperty(NodeProperties.VALUE),
                      rn.getProperty(NodeProperties.VALUE)));
        }
        return c;
    }

    /**
     * Visits a ExclusiveOrAssignExpression
     * @param node the node to visit
     */
    public Object visit(ExclusiveOrAssignExpression node) {
        return visitBitwiseAssign(node);
    }

    /**
     * Visits a ShiftLeftExpression
     * @param node the node to visit
     */
    public Object visit(ShiftLeftExpression node) {
        Class c = visitShiftExpression(node);

        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();

        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.shiftLeft
                     (NodeProperties.getType(node),
                      ln.getProperty(NodeProperties.VALUE),
                      rn.getProperty(NodeProperties.VALUE)));
        }
        return c;
    }

    /**
     * Visits a ShiftLeftAssignExpression
     * @param node the node to visit
     */
    public Object visit(ShiftLeftAssignExpression node) {
        Class c = visitShiftExpression(node);

        // The left subexpression must be a variable
        if (!node.getLeftExpression().hasProperty(NodeProperties.MODIFIER)) {
            throw new ExecutionError("shift.left.type", node);
        }

        return c;
    }

    /**
     * Visits a ShiftRightExpression
     * @param node the node to visit
     */
    public Object visit(ShiftRightExpression node) {
        Class c = visitShiftExpression(node);

        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();

        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.shiftRight
                     (NodeProperties.getType(node),
                      ln.getProperty(NodeProperties.VALUE),
                      rn.getProperty(NodeProperties.VALUE)));
        }
        return c;
    }

    /**
     * Visits a ShiftRightAssignExpression
     * @param node the node to visit
     */
    public Object visit(ShiftRightAssignExpression node) {
        Class c = visitShiftExpression(node);

        // The left subexpression must be a variable
        if (!node.getLeftExpression().hasProperty(NodeProperties.MODIFIER)) {
            throw new ExecutionError("shift.right.type", node);
        }

        return c;
    }

    /**
     * Visits a UnsignedShiftRightExpression
     * @param node the node to visit
     */
    public Object visit(UnsignedShiftRightExpression node) {
        Class c = visitShiftExpression(node);

        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();

        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 InterpreterUtilities.unsignedShiftRight
                     (NodeProperties.getType(node),
                      ln.getProperty(NodeProperties.VALUE),
                      rn.getProperty(NodeProperties.VALUE)));
        }
        return c;
    }

    /**
     * Visits a UnsignedShiftRightAssignExpression
     * @param node the node to visit
     */
    public Object visit(UnsignedShiftRightAssignExpression node) {
        Class c = visitShiftExpression(node);

        // The left subexpression must be a variable
        if (!node.getLeftExpression().hasProperty(NodeProperties.MODIFIER)) {
            throw new ExecutionError("unsigned.shift.right.type", node);
        }

        return c;
    }

    /**
     * Visits an AndExpression
     * @param node the node to visit
     */
    public Object visit(AndExpression node) {
        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();
        Class lc = (Class)ln.acceptVisitor(this);
        Class rc = (Class)rn.acceptVisitor(this);

        // Check the types of the operands
        if (lc != boolean.class || rc != boolean.class) {
            throw new ExecutionError("and.type", node);
        }

        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 (((Boolean)ln.getProperty(NodeProperties.VALUE)).booleanValue() &&
                  ((Boolean)rn.getProperty(NodeProperties.VALUE)).booleanValue())
                 ? Boolean.TRUE : Boolean.FALSE);
	}

        // Set the type property
        node.setProperty(NodeProperties.TYPE, boolean.class);
        return boolean.class;
    }

    /**
     * Visits an OrExpression
     * @param node the node to visit
     */
    public Object visit(OrExpression node) {
        Node  ln = node.getLeftExpression();
        Node  rn = node.getRightExpression();
        Class lc = (Class)ln.acceptVisitor(this);
        Class rc = (Class)rn.acceptVisitor(this);

        // Check the types of the operands
        if (lc != boolean.class || rc != boolean.class) {
            throw new ExecutionError("or.type", node);
        }

        // Compute the expression if it is constant
        if (ln.hasProperty(NodeProperties.VALUE) &&
            rn.hasProperty(NodeProperties.VALUE)) {
            node.setProperty
                (NodeProperties.VALUE,
                 (((Boolean)ln.getProperty(NodeProperties.VALUE)).booleanValue() ||
                  ((Boolean)rn.getProperty(NodeProperties.VALUE)).booleanValue())
                 ? Boolean.TRUE : Boolean.FALSE);
	}

        // Set the type property
        node.setProperty(NodeProperties.TYPE, boolean.class);
        return boolean.class;
    }

    /**
     * Visits a InstanceOfExpression
     * @param node the node to visit
     */
    public Object visit(InstanceOfExpression node) {
        node.getReferenceType().acceptVisitor(this);

        // The expression must not have a primitive type
        if (((Class)node.getExpression().acceptVisitor(this)).isPrimitive()) {
            throw new ExecutionError("left.expression", node);
        }

        // Set the type property
        node.setProperty(NodeProperties.TYPE, boolean.class);
        return boolean.class;
    }

    /**
     * Visits a ConditionalExpression
     * @param node the node to visit
     */
    public Object visit(ConditionalExpression node) {
        // Check the condition
        if (node.getConditionExpression().acceptVisitor(this) != boolean.class) {
            throw new ExecutionError("condition.type", node);
        }

        // Determine the type of the expression
        Node  n1 = node.getIfTrueExpression();
        Node  n2 = node.getIfFalseExpression();
        Class c1 = (Class)n1.acceptVisitor(this);
        Class c2 = (Class)n2.acceptVisitor(this);
        Class ec = null;

        if (c1 == c2) {
            ec = c1;
        } else if (c1 == null) {
            ec = c2;
        } else if (c2 == null) {
            ec = c1;
        } else if (!c1.isPrimitive() && !c2.isPrimitive()) {
            if (c1.isAssignableFrom(c2)) {
                ec = c1;
            } else if (c2.isAssignableFrom(c1)) {
                ec = c2;
            } else {
                throw new ExecutionError("incompatible.types", node);
            }
        } else if (c1 == boolean.class || c2 == boolean.class ||
                   c1 == void.class    || c2 == void.class) {
            throw new ExecutionError("incompatible.types", node);
        } else if ((c1 == short.class && c2 == byte.class) ||
                   (c1 == byte.class  && c2 == short.class)) {
            ec = short.class;
        } else if ((c2 == byte.class || c2 == short.class || c2 == char.class) &&
                   n1.hasProperty(NodeProperties.VALUE) && c1 == int.class) {
            Number n = (Number)n1.getProperty(NodeProperties.VALUE);
            if (c2 == byte.class) {
                if (n.intValue() == n.byteValue()) {
                    ec = byte.class;
                } else {
                    ec = int.class;
                }
            } else if (n.intValue() == n.shortValue()) {
                ec = (c2 == char.class) ? char.class : short.class;
            } else {
                ec = int.class;
            }
        } else if ((c1 == byte.class || c1 == short.class || c1 == char.class) &&
                   n2.hasProperty(NodeProperties.VALUE) && c2 == int.class) {
            Number n = (Number)n2.getProperty(NodeProperties.VALUE);
            if (c1 == byte.class) {
                if (n.intValue() == n.byteValue()) {
                    ec = byte.class;
                } else {
                    ec = int.class;
                }
            } else if (n.intValue() == n.shortValue()) {
                ec = (c1 == char.class) ? char.class : short.class;
            } else {
                ec = int.class;
            }       
        } else if (c1 == double.class || c2 == double.class) {
            ec = double.class;
        } else if (c1 == float.class || c2 == float.class) {
            ec = float.class;
        } else if (c1 == long.class || c2 == long.class) {
            ec = long.class;
        } else {
            ec = int.class;
        }
        node.setProperty(NodeProperties.TYPE, ec);

        return ec;
    }

    /**
     * Visits a FormalParameter
     * @param node the node to visit
     * @return the class of the parameter
     */
    public Object visit(FormalParameter node) {
        Class c = (Class)node.getType().acceptVisitor(this);

        if (node.isFinal()) {
            context.defineConstant(node.getName(), c);
        } else {
            context.define(node.getName(), c);
        }
        node.setProperty(NodeProperties.TYPE, c);
	
        return c;
    }

    /**
     * Visits a PostIncrement
     * @param node the node to visit
     */
    public Object visit(PostIncrement node) {
        Node exp = node.getExpression();
        Class c  = (Class)exp.acceptVisitor(this);

        // The type of the subexpression must be numeric
        if (!c.isPrimitive()   ||
            c == void.class    ||
            c == boolean.class) {
            throw new ExecutionError("post.increment.type", node);
        }

        // The subexpression must be a variable
        if (!exp.hasProperty(NodeProperties.MODIFIER)) {
            throw new ExecutionError("post.increment.type", node);
        }

        node.setProperty(NodeProperties.TYPE, c);
        return c;
    }

     /**
     * Visits a PreIncrement
     * @param node the node to visit
     */
    public Object visit(PreIncrement node) {
        Node exp = node.getExpression();
        Class c  = (Class)exp.acceptVisitor(this);

        // The type of the subexpression must be numeric
        if (!c.isPrimitive()   ||
            c == void.class    ||
            c == boolean.class) {
            throw new ExecutionError("pre.increment.type", node);
        }

        // The subexpression must be a variable
        if (!exp.hasProperty(NodeProperties.MODIFIER)) {
            throw new ExecutionError("pre.increment.type", node);
        }

        node.setProperty(NodeProperties.TYPE, c);
        return c;
    }

    /**
     * Visits a PostDecrement
     * @param node the node to visit
     */
    public Object visit(PostDecrement node) {
        Node exp = node.getExpression();
        Class c  = (Class)exp.acceptVisitor(this);

        // The type of the subexpression must be numeric
        if (!c.isPrimitive()   ||
            c == void.class    ||
            c == boolean.class) {
            throw new ExecutionError("post.decrement.type", node);
        }

        // The subexpression must be a variable
        if (!exp.hasProperty(NodeProperties.MODIFIER)) {
            throw new ExecutionError("post.decrement.type", node);
        }

        node.setProperty(NodeProperties.TYPE, c);
        return c;
    }

     /**
     * Visits a PreDecrement
     * @param node the node to visit
     */
    public Object visit(PreDecrement node) {
        Node exp = node.getExpression();
        Class c  = (Class)exp.acceptVisitor(this);

        // The type of the subexpression must be numeric
        if (!c.isPrimitive()   ||
            c == void.class    ||
            c == boolean.class) {
            throw new ExecutionError("pre.decrement.type", node);
        }

        // The subexpression must be a variable
        if (!exp.hasProperty(NodeProperties.MODIFIER)) {
            throw new ExecutionError("pre.decrement.type", node);
        }

        node.setProperty(NodeProperties.TYPE, c);
        return c;
    }

    /**
     * Visits a CastExpression
     * @param node the node to visit
     */
    public Object visit(CastExpression node) {
        Class c = (Class)node.getTargetType().acceptVisitor(this);
        checkCastStaticRules(c, (Class)node.getExpression().acceptVisitor(this), node);

        node.setProperty(NodeProperties.TYPE, c);
        return c;
    }

   /**
     * Visits an unary operation
     */
    private Class visitUnaryOperation(UnaryExpression node, String s) {
        Class c = (Class)node.getExpression().acceptVisitor(this);

        if (c == char.class || c == byte.class || c == short.class || c == int.class) {
            node.setProperty(NodeProperties.TYPE, int.class);
        } else if (c == long.class || c == float.class || c == double.class) {
            node.setProperty(NodeProperties.TYPE, c);
        } else {
            throw new ExecutionError(s, node);
        }
	return c;
    }

    /**
     * Visits a numeric expression
     */
    private static Class visitNumericExpression(BinaryExpression node, String s) {
        // Set the type property of the given node
        Class lc = NodeProperties.getType(node.getLeftExpression());
        Class rc = NodeProperties.getType(node.getRightExpression());
	Class c  = null;

        if (lc == null           || rc == null          ||
            lc == boolean.class  || rc == boolean.class ||
            !lc.isPrimitive()    || !rc.isPrimitive()   ||
            lc == void.class     || rc == void.class) {
            throw new ExecutionError(s, node);
        } else if (lc == double.class || rc == double.class) {
            node.setProperty(NodeProperties.TYPE, c = double.class);
        } else if (lc == float.class || rc == float.class) {
            node.setProperty(NodeProperties.TYPE, c = float.class);
        } else if (lc == long.class || rc == long.class) {
            node.setProperty(NodeProperties.TYPE, c = long.class);
        } else {
            node.setProperty(NodeProperties.TYPE, c = int.class);
        }
	return c;
    }

    /**
     * Checks the typing rules for an assignment
     * @param lc   the class of the left part of an assignment
     * @param rc   the class of the right part of an assignment
     * @param node the current node
     */
    private static void checkAssignmentStaticRules(Class lc, Class rc,
                                                   Node node, Node v) {
        if (lc != null) {
            if (lc.isPrimitive()) {
                if (lc == boolean.class && rc != boolean.class) {
                    throw new ExecutionError("assignment.types", node);
                } else if (lc == byte.class && rc != byte.class) {
                    if (rc == int.class && v.hasProperty(NodeProperties.VALUE)) {
                        Number n = (Number)v.getProperty(NodeProperties.VALUE);
                        if (n.intValue() == n.byteValue()) {
                            return;
                        }
                    }
                    throw new ExecutionError("assignment.types", node);
                } else if ((lc == short.class || rc == char.class) &&
                           (rc != byte.class && rc != short.class && rc != char.class)) {
                    if (rc == int.class && v.hasProperty(NodeProperties.VALUE)) {
                        Number n = (Number)v.getProperty(NodeProperties.VALUE);
                        if (n.intValue() == n.shortValue()) {
                            return;
                        }
                    }
                    throw new ExecutionError("assignment.types", node);
                } else if (lc == int.class    &&
                           (rc != byte.class  &&
                            rc != short.class &&
                            rc != char.class  &&
                            rc != int.class)) {
                    throw new ExecutionError("assignment.types", node);
                } else if (lc == long.class   &&
                           (rc == null          ||
			    !rc.isPrimitive()   ||
                            rc == void.class    ||
                            rc == boolean.class ||
                            rc == float.class   ||
                            rc == double.class)) {
                    throw new ExecutionError("assignment.types", node);
                } else if (lc == float.class  && 
                           (rc == null          ||
			    !rc.isPrimitive()   ||
                            rc == void.class    ||
                            rc == boolean.class ||
                            rc == double.class)) {
                    throw new ExecutionError("assignment.types", node);
                } else if (lc == double.class && 
                           (rc == null        ||
			    !rc.isPrimitive() ||
                            rc == void.class  ||
                            rc == boolean.class)) {
                    throw new ExecutionError("assignment.types", node);
                }
            } else if (rc != null) {
                if (!lc.isAssignableFrom(rc) && !rc.isAssignableFrom(lc)) {
                    throw new ExecutionError("assignment.types", node);
                }
            }
        }
    }

    /**
     * Checks the typing rules in an equality operation
     * @param lc the class of the left operand
     * @param rc the class of the right operand
     * @param s  the error message
     * @param n  the current node
     */
    private static void checkEqualityStaticRules(Class lc, Class rc, Node n) {
        if (lc != rc || lc == void.class) {
            if (lc == void.class    || rc == void.class     ||
                lc == boolean.class || rc == boolean.class) {
                throw new ExecutionError("compare.type", n);
            } else if ((lc == null && rc.isPrimitive()) ||
                       (rc == null && lc.isPrimitive())) {
                throw new ExecutionError("compare.type", n);
            } else if (lc != null && rc != null) {
                if (lc.isPrimitive() ^ rc.isPrimitive()) {
                    throw new ExecutionError("compare.type", n);
                } else if (!lc.isPrimitive() && !rc.isPrimitive()) {
                    if (!lc.isAssignableFrom(rc) && !rc.isAssignableFrom(lc)) {
                        throw new ExecutionError("compare.type", n);
                    }
                }
            }
        }
    }

    /**
     * Visits a relational expression
     */
    private Class visitRelationalExpression(BinaryExpression node) {
        // Check the types
        Class lc = (Class)node.getLeftExpression().acceptVisitor(this);
        Class rc = (Class)node.getRightExpression().acceptVisitor(this);

        if (lc == null          || rc == null           ||
            lc == void.class    || rc == void.class     ||
            lc == boolean.class || rc == boolean.class  ||
            !lc.isPrimitive()   || !rc.isPrimitive()) {
            throw new ExecutionError("relational.expression.type", node);
        }
        
        // The type of the expression is always boolean
        node.setProperty(NodeProperties.TYPE, boolean.class);
	return boolean.class;
    }

    /**
     * Visits a bitwise expression
     */
     private Class visitBitwiseExpression(BinaryExpression node) {
        // Check the types
        Class lc = (Class)node.getLeftExpression().acceptVisitor(this);
        Class rc = (Class)node.getRightExpression().acceptVisitor(this);
	Class c = null;

        if (lc == null           || rc == null           ||
            lc == void.class     || rc == void.class     ||
            lc == float.class    || rc == float.class    ||
            lc == double.class   || rc == double.class   ||
            (lc == boolean.class ^ rc == boolean.class)  ||
            !lc.isPrimitive()    || !rc.isPrimitive()) {
            throw new ExecutionError("bitwise.expression.type", node);
        } else if (lc == long.class || rc == long.class) {
            node.setProperty(NodeProperties.TYPE, c = long.class);
        } else if (lc == boolean.class) {
            node.setProperty(NodeProperties.TYPE, c = boolean.class);
        } else {
            node.setProperty(NodeProperties.TYPE, c = int.class);
        }
	return c;
    }

    /**
     * Checks a bitwise expression
     */
    private Class visitBitwiseAssign(BinaryExpression node) {
        // Check the types
	Node  ln = node.getLeftExpression();
        Class lc = (Class)ln.acceptVisitor(this);
        Class rc = (Class)node.getRightExpression().acceptVisitor(this);

        if (lc == null           || rc == null           ||
            lc == void.class     || rc == void.class     ||
            lc == float.class    || rc == float.class    ||
            lc == double.class   || rc == double.class   ||
            (lc == boolean.class ^ rc == boolean.class)  ||
            !lc.isPrimitive()    || !rc.isPrimitive()) {
            throw new ExecutionError("bitwise.expression.type", node);
        }

        // The left subexpression must be a variable
        if (!ln.hasProperty(NodeProperties.MODIFIER)) {
            throw new ExecutionError("left.expression", node);
        }
 
       // Sets the type property of this node
        node.setProperty(NodeProperties.TYPE, lc);
	return lc;
     }

    /**
     * Visits a shift expression
     */
    private Class visitShiftExpression(BinaryExpression node) {
        // Check the types
        Class lc = (Class)node.getLeftExpression().acceptVisitor(this);
        Class rc = (Class)node.getRightExpression().acceptVisitor(this);
	Class c  = null;

        if (lc == null          || rc == null          ||
            lc == boolean.class || rc == boolean.class ||
            lc == void.class    || rc == void.class    ||
            lc == float.class   || rc == float.class   ||
            lc == double.class  || rc == double.class  ||
            !lc.isPrimitive()   || !rc.isPrimitive()) {
            throw new ExecutionError("shift.expression.type", node);
        } else if (lc == long.class) {
            node.setProperty(NodeProperties.TYPE, c = long.class);
        } else {
            node.setProperty(NodeProperties.TYPE, c = int.class);
        }
	return c;
    }

    /**
     * Checks the typing rules in a cast expression
     * @param tc the target class
     * @param ec the expression class
     */
    private static void checkCastStaticRules(Class tc, Class ec, Node n) {
        if (tc != ec) {
            if (tc.isPrimitive()) {
                if (tc == null || !ec.isPrimitive() ||
                    ec == boolean.class || ec == void.class) {
                    throw new ExecutionError("cast", n);
                }
            } else if (ec != null) {
                if (ec.isArray()) {
                    if (tc.isArray()) {
                        Class tec = tc.getComponentType();
                        Class eec = ec.getComponentType();
                        if (tec.isPrimitive() && eec.isPrimitive()) {
                            if (tec != eec) {
                                throw new ExecutionError("cast", n);
                            }
                        } else {
                            checkCastStaticRules(tec, eec, n);
                        }
                    } else if (tc.isInterface() && tc != Cloneable.class) {
                        throw new ExecutionError("cast", n);
                    } else if (tc != Object.class) {
                        throw new ExecutionError("cast", n);
                    }
                } else if (ec.isInterface()) {
                    if (tc.isInterface()) {
                        // !!! TODO : tests the signatures ?
                    } else if (tc.isArray()) {
                        if (!Cloneable.class.isAssignableFrom(ec)) {
                            throw new ExecutionError("cast", n);
                        }
                    } else if (Modifier.isFinal(tc.getModifiers())) {
                        if (!tc.isAssignableFrom(ec)) {
                            throw new ExecutionError("cast", n);
                        }
                    }
                } else if (tc.isInterface()) {
                    if (Modifier.isFinal(tc.getModifiers())) {
                        if (!tc.isAssignableFrom(ec)) {
                            throw new ExecutionError("cast", n);
                        }
                    }
                } else if (!ec.isAssignableFrom(tc) && !tc.isAssignableFrom(ec)) {
                    throw new ExecutionError("cast", n);
                }
            }
        }
    }

    /**
     * Check a list of node
     */
    private void checkList(List l) {
        ListIterator it = l.listIterator();
        while (it.hasNext()) {
            ((Node)it.next()).acceptVisitor(this);
        }
    }

}
