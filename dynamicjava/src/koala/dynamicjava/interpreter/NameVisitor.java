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

import java.util.*;

import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.throwable.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;

/**
 * This tree visitor resolves the ambiguity in identifiers
 * in a syntax tree
 *
 * @author Stephane Hillion
 * @version 1.1 - 1999/10/18
 */

public class NameVisitor extends VisitorObject {
    /**
     * The context
     */
    private Context context;

    /**
     * Creates a new name visitor
     * @param ctx the context
     */
    public NameVisitor(Context ctx) {
	context = ctx;
    }

    /**
     * Sets the context's current package
     * @param node the node to visit
     * @return null
     */
    public Object visit(PackageDeclaration node) {
        context.setCurrentPackage(node.getName());
        return null;
    }

    /**
     * Declares the package or class importation in the context
     * @param node the node to visit
     */
    public Object visit(ImportDeclaration node) {
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
        // Visits the components of this node
	Node   n = node.getCondition();
        Object o = n.acceptVisitor(this);
        if (o != null) {
            if (o instanceof ReferenceType) {
                throw new ExecutionError("malformed.expression", n);
	    }
	    node.setCondition((Expression)o);
        }
	
	n = node.getBody();
        o = n.acceptVisitor(this);
        if (o != null) {
            if (o instanceof ReferenceType) {
                throw new ExecutionError("malformed.expression", n);
	    }
	    node.setBody((Node)o);
        }
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
            visitList(l);
        }

	Node n;
        if ((n = node.getCondition()) != null) {
            Object o = n.acceptVisitor(this);
	    if (o != null) {
		if (o instanceof ReferenceType) {
		    throw new ExecutionError("malformed.expression", n);
		}
		node.setCondition((Expression)o);
	    }
        }

        if ((l = node.getUpdate()) != null) {
            visitList(l);
        }

	n = node.getBody();
        Object o = n.acceptVisitor(this);
        if (o != null) {
            if (o instanceof ReferenceType) {
                throw new ExecutionError("malformed.expression", n);
	    }
	    node.setBody((Node)o);
        }

        // Leave the current scope
        context.leaveScope();
        return null;
    }

    /**
     * Visits a DoStatement
     * @param node the node to visit
     */
    public Object visit(DoStatement node) {
        // Visits the components of this node
	Node   n = node.getCondition();
        Object o = n.acceptVisitor(this);
        if (o != null) {
            if (o instanceof ReferenceType) {
                throw new ExecutionError("malformed.expression", n);
	    }
	    node.setCondition((Expression)o);
        }
	
	n = node.getBody();
        o = n.acceptVisitor(this);
        if (o != null) {
            if (o instanceof ReferenceType) {
                throw new ExecutionError("malformed.expression", n);
	    }
	    node.setBody((Node)o);
        }
        return null;
    }

    /**
     * Visits a SwitchStatement
     * @param node the node to visit
     */
    public Object visit(SwitchStatement node) {
        // Visits the components of this node
	Expression exp = node.getSelector();
        Object o = exp.acceptVisitor(this);
        if (o != null) {
            if (o instanceof ReferenceType) {
                throw new ExecutionError("malformed.expression", exp);
	    }
	    node.setSelector((Expression)o);
        }

        Iterator it = node.getBindings().iterator();
        while (it.hasNext()) {
            ((Node)it.next()).acceptVisitor(this);
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
	    visitExpressionContainer(node);
	}
	List l;
	if ((l = node.getStatements()) != null) {
	    visitList(l);
	}
	return null;
    }

    /**
     * Visits a LabeledStatement
     * @param node the node to visit
     */
    public Object visit(LabeledStatement node) {
	Node n = node.getStatement();
        Object o = n.acceptVisitor(this);
        if (o != null) {
	    if (o instanceof ReferenceType) {
		throw new ExecutionError("malformed.expression", n);
	    }
	    node.setStatement((Expression)o);
        }
        return null;
    }

    /**
     * Visits a ThrowStatement
     * @param node the node to visit
     */
    public Object visit(ThrowStatement node) {
	visitExpressionContainer(node);
        return null;
    }

    /**
     * Visits a SynchronizedStatement
     * @param node the node to visit
     */
    public Object visit(SynchronizedStatement node) {
        // Visits the component of this node
	Node n = node.getLock();
        Object o = n.acceptVisitor(this);
        if (o != null) {
	    if (o instanceof ReferenceType) {
		throw new ExecutionError("malformed.expression", n);
	    }
	    node.setLock((Expression)o);
        }
	
	n = node.getBody();
        o = n.acceptVisitor(this);
        if (o != null) {
	    if (o instanceof ReferenceType) {
		throw new ExecutionError("malformed.expression", n);
	    }
	    node.setBody((Node)o);
        }
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

	node.getException().acceptVisitor(this);
	node.getBlock().acceptVisitor(this);
	// Leave the current scope
	context.leaveScope();
	return null;
    }

    /**
     * Visits a ReturnStatement
     * @param node the node to visit
     */
    public Object visit(ReturnStatement node) {
        Expression e = node.getExpression();
        if (e != null) {
	    visitExpressionContainer(node);
        }
        return null;
    }

    /**
     * Visits an IfThenStatement
     * @param node the node to visit
     */
    public Object visit(IfThenStatement node) {
        // Visits the components of this node
	Node   n = node.getCondition();
        Object o = n.acceptVisitor(this);
        if (o != null) {
            if (o instanceof ReferenceType) {
                throw new ExecutionError("malformed.expression", n);
	    }
	    node.setCondition((Expression)o);
        }
	
	n = node.getThenStatement();
        o = n.acceptVisitor(this);
        if (o != null) {
            if (o instanceof ReferenceType) {
                throw new ExecutionError("malformed.expression", n);
	    }
	    node.setThenStatement((Node)o);
        }
        return null;
    }

    /**
     * Visits an IfThenElseStatement
     * @param node the node to visit
     */
    public Object visit(IfThenElseStatement node) {
        // Visits the components of this node
	Node   n = node.getCondition();
        Object o = n.acceptVisitor(this);
        if (o != null) {
            if (o instanceof ReferenceType) {
                throw new ExecutionError("malformed.expression", n);
	    }
	    node.setCondition((Expression)o);
        }
	
	n = node.getThenStatement();
        o = n.acceptVisitor(this);
        if (o != null) {
            if (o instanceof ReferenceType) {
                throw new ExecutionError("malformed.expression", n);
	    }
	    node.setThenStatement((Node)o);
        }
	
	n = node.getElseStatement();
        o = n.acceptVisitor(this);
        if (o != null) {
            if (o instanceof ReferenceType) {
                throw new ExecutionError("malformed.expression", n);
	    }
	    node.setElseStatement((Node)o);
        }
        return null;
    }

    /**
     * Visits a VariableDeclaration
     * @param node the node to visit
     */
    public Object visit(VariableDeclaration node) {
	// Define the variable
	String s = node.getName();
        if (context.isDefinedVariable(s)) {
	    node.setProperty(NodeProperties.ERROR_STRINGS,
			     new String[] { s });
            throw new ExecutionError("variable.redefinition", node);
        }

        if (node.isFinal()) {
            context.defineConstant(s, null);
        } else {
            context.define(s, null);
        }

	// Visit the initializer
	Node n = node.getInitializer();
        if (n != null) {
            Object o = n.acceptVisitor(this);
            if (o != null) {
		if (o instanceof ReferenceType) {
                    throw new ExecutionError("malformed.expression", n);
		}
		node.setInitializer((Expression)o);
            }
	}	
	return null;
    }

    /**
     * Visits a SimpleAssignExpression
     * @param node the node to visit
     */
    public Object visit(SimpleAssignExpression node) {
        // First, visit the right expression
        Expression right  = node.getRightExpression();
        Object o = right.acceptVisitor(this);
        if (o != null) {
	    if (o instanceof ReferenceType) {
                throw new ExecutionError("right.expression", right);
	    }
	    node.setRightExpression((Expression)o);
        }

        // Perhaps is this assignment a variable declaration ?
        Expression left  = node.getLeftExpression();
        if (left instanceof QualifiedName) {
            List   ids = ((QualifiedName)left).getIdentifiers();
            String var = ((IdentifierToken)ids.get(0)).image();
            if (ids.size() == 1 && !context.exists(var)) {
		context.define(var, null);
	    }
	}	

        // Visit the left expression
	o = left.acceptVisitor(this);
	if (o != null) {
	    if (o instanceof ReferenceType) {
                throw new ExecutionError("left.expression", node);
	    }
	    node.setLeftExpression((Expression)o);
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

        // Visit the nested statements
        visitList(node.getStatements());

        // Leave the current scope
        context.leaveScope();
        
        return null;
    }

    /**
     * Visits an ObjectFieldAccess
     * @param node the node to visit
     */
    public Object visit(ObjectFieldAccess node) {
        // Visit the expression
        Object o = node.getExpression().acceptVisitor(this);
        if (o != null) {
            if (o instanceof ReferenceType) {
                return new StaticFieldAccess((ReferenceType)o,
					     node.getFieldName(),
					     node.getFilename(),
					     node.getBeginLine(),
					     node.getBeginColumn(),
					     node.getEndLine(),
					     node.getEndColumn());
            } else {
                node.setExpression((Expression)o);
            }
        }
	return null;
    }

    /**
     * Visits an ObjectMethodCall
     * @param node the node to visit
     */
    public Object visit(ObjectMethodCall node) {
	// Check the arguments
	List args = node.getArguments();
	if (args != null) {
	    visitList(args);
	}

	// Check the expression
	Expression exp = node.getExpression();
	Object o;
	if (exp == null) {
	    o = context.getDefaultQualifier(node);
	} else {
	    o = exp.acceptVisitor(this);
	    if (o == null) {
		return null;
	    }
	}

	if (o == null) {
	    return new FunctionCall(node.getMethodName(),
				    node.getArguments(),
				    node.getFilename(),
				    node.getBeginLine(),
				    node.getBeginColumn(),
				    node.getEndLine(),
				    node.getEndColumn());
        } else if (o instanceof ReferenceType) {
	    return new StaticMethodCall((ReferenceType)o,
					node.getMethodName(),
					node.getArguments(),
					node.getFilename(),
					node.getBeginLine(),
					node.getBeginColumn(),
					node.getEndLine(),
					node.getEndColumn());
	} else {
	    node.setExpression((Expression)o);
	}
	return null;
    }

    /**
     * Visits a SuperMethodCall
     * @param node the node to visit
     */
    public Object visit(SuperMethodCall node) {
	// Check the arguments
	List args = node.getArguments();
	if (args != null) {
	    visitList(args);
	}
	return null;
    }

    /**
     * Visits a ThisExpression
     * @param node the node to visit
     * @return a qualified name or a field access
     */
    public Object visit(ThisExpression node) {
	return context.getDefaultQualifier(node, node.getClassName());
    }

    /**
     * Visits a QualifiedName
     * @param node the node to visit
     * @return a node that depends of the meaning of this name.
     *         It could be : a QualifiedName, a ReferenceType or a FieldAccess.
     */
    public Object visit(QualifiedName node) {
        List  ids = node.getIdentifiers();
        IdentifierToken t = (IdentifierToken)ids.get(0);

        if (context.isDefined(t.image())) {
            // The name starts with a reference to a local variable,
            // end of the name is a sequence of field access
	    Expression result = context.createName(node, t);
            Iterator it = ids.iterator();
            it.next();

            IdentifierToken t2;
            while (it.hasNext()) {
                result = new ObjectFieldAccess(result,
                                               (t2 = (IdentifierToken)it.next()).image(),
                                               node.getFilename(),
                                               t.beginLine(), t.beginColumn(),
                                               t2.endLine(),  t2.endColumn());
            }
            return result;
        } 
	    
        // The name must be, or starts with, a class name
        List      l = (List)((LinkedList)ids).clone();
	boolean   b = false;

	while (l.size() > 0) {
	    String s = TreeUtilities.listToName(l);
	    if (b = context.classExists(s)) {
		break;
	    }
	    l.remove(l.size()-1);
	}

        if (!b) {
            // It is an error if no matching class or field was found
	    node.setProperty(NodeProperties.ERROR_STRINGS, new String[] { t.image() });
           throw new ExecutionError("undefined.class", node);
        }

        // Creates a ReferenceType node
        IdentifierToken t2 = (IdentifierToken)l.get(l.size()-1);
        ReferenceType rt = new ReferenceType(l,
					     node.getFilename(),
					     t.beginLine(), t.beginColumn(),
					     t2.endLine(),  t2.endColumn());
	
        if (l.size() != ids.size()) {
            // The end of the name is a sequence of field access
            ListIterator it = ids.listIterator(l.size());
            Expression result =
		new StaticFieldAccess(rt,
				      (t2 = (IdentifierToken)it.next()).image(),
				      node.getFilename(),
				      t.beginLine(), t.beginColumn(),
				      t2.endLine(),  t2.endColumn());
            while (it.hasNext()) {
                result = new ObjectFieldAccess(result,
                                               (t2 = (IdentifierToken)it.next()).image(),
                                               node.getFilename(),
                                               t.beginLine(), t.beginColumn(),
                                               t2.endLine(),  t2.endColumn());
            }
            return result;
        } else { 
            return rt;
        }
    }

    /**
     * Visits a SimpleAllocation
     * @param node the node to visit
     */
    public Object visit(SimpleAllocation node) {
        // Visit the arguments
        List args = node.getArguments();

        if (args != null) {
            visitList(args);
	}
        return null;
    }

    /**
     * Visits an ArrayAllocation
     * @param node the node to visit
     */
    public Object visit(ArrayAllocation node) {
        // Do the checking of the size expressions
        visitList(node.getSizes());

        // Visits the initializer if one
        if (node.getInitialization() != null) {
            node.getInitialization().acceptVisitor(this);
        }
	return null;
    }

    /**
     * Visits a InnerAllocation
     * @param node the node to visit
     */
    public Object visit(InnerAllocation node) {
	visitExpressionContainer(node);

        // Do the type checking of the arguments
        List args = node.getArguments();
        if (args != null) {
            visitList(args);
        }
	return null;
    }

    /**
     * Visits a ArrayInitializer
     * @param node the node to visit
     */
    public Object visit(ArrayInitializer node) {
        visitList(node.getCells());
        return null;
    }

    /**
     * Visits an ArrayAccess
     * @param node the node to visit
     */
    public Object visit(ArrayAccess node) {
	visitExpressionContainer(node);

        // Visits the cell number expression
	Node   n = node.getCellNumber();
        Object o = n.acceptVisitor(this);
	if (o != null) {
	    if (o instanceof ReferenceType) {
		throw new ExecutionError("malformed.expression", n);
	    }
	    node.setCellNumber((Expression)o);
	}
	return null;
    }

    /**
     * Visits a NotExpression
     * @param node the node to visit
     */
    public Object visit(NotExpression node) {
        visitExpressionContainer(node);
	return null;
    }

    /**
     * Visits a ComplementExpression
     * @param node the node to visit
     */
    public Object visit(ComplementExpression node) {
        visitExpressionContainer(node);
	return null;
    }

    /**
     * Visits a PlusExpression
     * @param node the node to visit
     */
    public Object visit(PlusExpression node) {
        visitExpressionContainer(node);
	return null;
    }

    /**
     * Visits a MinusExpression
     * @param node the node to visit
     */
    public Object visit(MinusExpression node) {
        visitExpressionContainer(node);
	return null;
    }

    /**
     * Visits an AddExpression
     * @param node the node to visit
     */
    public Object visit(AddExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits an AddAssignExpression
     * @param node the node to visit
     */
    public Object visit(AddAssignExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a SubtractExpression
     * @param node the node to visit
     */
    public Object visit(SubtractExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits an SubtractAssignExpression
     * @param node the node to visit
     */
    public Object visit(SubtractAssignExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a MultiplyExpression
     * @param node the node to visit
     */
    public Object visit(MultiplyExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits an MultiplyAssignExpression
     * @param node the node to visit
     */
    public Object visit(MultiplyAssignExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a DivideExpression
     * @param node the node to visit
     */
    public Object visit(DivideExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits an DivideAssignExpression
     * @param node the node to visit
     */
    public Object visit(DivideAssignExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a RemainderExpression
     * @param node the node to visit
     */
    public Object visit(RemainderExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a RemainderAssignExpression
     * @param node the node to visit
     */
    public Object visit(RemainderAssignExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits an EqualExpression
     * @param node the node to visit
     */
    public Object visit(EqualExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a NotEqualExpression
     * @param node the node to visit
     */
    public Object visit(NotEqualExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a LessExpression
     * @param node the node to visit
     */
    public Object visit(LessExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a LessOrEqualExpression
     * @param node the node to visit
     */
    public Object visit(LessOrEqualExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a GreaterExpression
     * @param node the node to visit
     */
    public Object visit(GreaterExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a GreaterOrEqualExpression
     * @param node the node to visit
     */
    public Object visit(GreaterOrEqualExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a BitAndExpression
     * @param node the node to visit
     */
    public Object visit(BitAndExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a BitAndAssignExpression
     * @param node the node to visit
     */
    public Object visit(BitAndAssignExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a ExclusiveOrExpression
     * @param node the node to visit
     */
    public Object visit(ExclusiveOrExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a ExclusiveOrAssignExpression
     * @param node the node to visit
     */
    public Object visit(ExclusiveOrAssignExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a BitOrExpression
     * @param node the node to visit
     */
    public Object visit(BitOrExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a BitOrAssignExpression
     * @param node the node to visit
     */
    public Object visit(BitOrAssignExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a ShiftLeftExpression
     * @param node the node to visit
     */
    public Object visit(ShiftLeftExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a ShiftLeftAssignExpression
     * @param node the node to visit
     */
    public Object visit(ShiftLeftAssignExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a ShiftRightExpression
     * @param node the node to visit
     */
    public Object visit(ShiftRightExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a ShiftRightAssignExpression
     * @param node the node to visit
     */
    public Object visit(ShiftRightAssignExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a UnsignedShiftRightExpression
     * @param node the node to visit
     */
    public Object visit(UnsignedShiftRightExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a UnsignedShiftRightAssignExpression
     * @param node the node to visit
     */
    public Object visit(UnsignedShiftRightAssignExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits an AndExpression
     * @param node the node to visit
     */
    public Object visit(AndExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits an OrExpression
     * @param node the node to visit
     */
    public Object visit(OrExpression node) {
        visitBinaryExpression(node);
	return null;
    }

    /**
     * Visits a InstanceOfExpression
     * @param node the node to visit
     */
    public Object visit(InstanceOfExpression node) {
	visitExpressionContainer(node);
	return null;
    }

    /**
     * Visits a ConditionalExpression
     * @param node the node to visit
     */
    public Object visit(ConditionalExpression node) {
        // Check each subexpression
	Node   n = node.getConditionExpression();
        Object o = n.acceptVisitor(this);
        if (o != null) {
	    if (o instanceof ReferenceType) {
		throw new ExecutionError("malformed.expression", n);
	    }
	    node.setConditionExpression((Expression)o);
        }

	n = node.getIfTrueExpression();
        o = n.acceptVisitor(this);
        if (o != null) {
	    if (o instanceof ReferenceType) {
		throw new ExecutionError("malformed.second.operand", n);
	    }
	    node.setIfTrueExpression((Expression)o);
        }
	
	n = node.getIfFalseExpression();
        o = n.acceptVisitor(this);
        if (o != null) {
	    if (o instanceof ReferenceType) {
		throw new ExecutionError("malformed.third.operand", n);
	    }
	    node.setIfFalseExpression((Expression)o);
        }
	return null;
    }

    /**
     * Visits a PostIncrement
     * @param node the node to visit
     */
    public Object visit(PostIncrement node) {
	visitExpressionContainer(node);
	return null;
    }

    /**
     * Visits a PreIncrement
     * @param node the node to visit
     */
    public Object visit(PreIncrement node) {
	visitExpressionContainer(node);
	return null;
    }

    /**
     * Visits a PostDecrement
     * @param node the node to visit
     */
    public Object visit(PostDecrement node) {
	visitExpressionContainer(node);
	return null;
    }

    /**
     * Visits a PreDecrement
     * @param node the node to visit
     */
    public Object visit(PreDecrement node) {
	visitExpressionContainer(node);
	return null;
    }

    /**
     * Visits a CastExpression
     * @param node the node to visit
     */
    public Object visit(CastExpression node) {
	visitExpressionContainer(node);
	return null;
    }

    /**
     * Visits a ClassAllocation
     * @param node the node to visit
     */
    public Object visit(ClassAllocation node) {
	List largs = node.getArguments();
	if (largs != null) {
	    visitList(largs);
	}
	return null;
    }

    /**
     * Visits an InnerClassAllocation
     * @param node the node to visit
     */
    public Object visit(InnerClassAllocation node) {
	visitExpressionContainer(node);
	List largs = node.getArguments();
	if (largs != null) {
	    visitList(largs);
	}
	return null;
    }

    /**
     * Visits a FormalParameter
     * @param node the node to visit
     * @return the name of the parameter class
     */
    public Object visit(FormalParameter node) {
        if (node.isFinal()) {
            context.defineConstant(node.getName(), null);
        } else {
            context.define(node.getName(), null);
        }
        return null;
    }

    /**
     * Visits a ClassDeclaration
     * @param node the node to visit
     */
    public Object visit(ClassDeclaration node) {
	context.defineClass(node);
	return null;
    }

    /**
     * Visits an InterfaceDeclaration
     * @param node the node to visit
     */
    public Object visit(InterfaceDeclaration node) {
	context.defineClass(node);
	return null;
    }

    /**
     * Visits an expression container
     */
    private void visitExpressionContainer(ExpressionContainer node) {
	Expression exp = node.getExpression();
	Object o = exp.acceptVisitor(this);
	if (o != null) {
	    if (o instanceof ReferenceType) {
		throw new ExecutionError("malformed.expression", exp);
	    }
	    node.setExpression((Expression)o);
	}
    }

    /**
     * Visits the subexpressions of a BinaryExpression
     */
    private void visitBinaryExpression(BinaryExpression node) {
        // Visit the left expression
        Object o = node.getLeftExpression().acceptVisitor(this);
        if (o != null) {
            if (o instanceof Expression) {
                node.setLeftExpression((Expression)o);
            } else {
                throw new ExecutionError("left.operand", node);
            }
        }

        // Visit the right expression
        o = node.getRightExpression().acceptVisitor(this);
        if (o != null) {
            if (o instanceof Expression) {
                node.setRightExpression((Expression)o);
            } else {
                throw new ExecutionError("right.operand", node);
            }
        }
    }

    /**
     * Visits a list of node
     */
    private void visitList(List l) {
	Node n;
        ListIterator it = l.listIterator();
        while (it.hasNext()) {
            Object o = (n = (Node)it.next()).acceptVisitor(this);
            if (o != null) {
                if (o instanceof ReferenceType) {
                    throw new ExecutionError("malformed.expression", n);
                }
		it.set(o);
            }
        }
    }
}
