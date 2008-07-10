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

import koala.dynamicjava.tree.Node;
import koala.dynamicjava.tree.Expression;
import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.Type;
import edu.rice.cs.dynamicjava.symbol.type.VariableType;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.Lambda2;

/**
 * This class provides concrete methods to facilitate attaching and reading auxiliary properties
 * associated with AST nodes.  To encourage safety, the properties defined here should be manipulated
 * exclusively through this interface (a better design would <em>require</em> working through the
 * interface...)
 */
public class NodeProperties {

    /** The Type of an expression */
    public final static String TYPE = "type";

    public static Type getType(Node n) {
        return (Type)n.getProperty(TYPE);
    }
    
    public static Type setType(Node n, Type t) {
      n.setProperty(TYPE, t);
      return t;
    }
    
    public static boolean hasType(Node n) {
      return n.hasProperty(TYPE);
    }
    
    public static final Lambda<Node, Type> NODE_TYPE = new Lambda<Node, Type>() {
      public Type value(Node n) { return getType(n); }
    };
    

    /** The Type of an expression when treated as a variable (an lvalue) */
    public final static String VARIABLE_TYPE = "variableType";

    public static Type getVariableType(Node n) {
        return (Type)n.getProperty(VARIABLE_TYPE);
    }
    
    public static Type setVariableType(Node n, Type t) {
      n.setProperty(VARIABLE_TYPE, t);
      return t;
    }
    
    public static boolean hasVariableType(Node n) {
      return n.hasProperty(VARIABLE_TYPE);
    }
    

    /**
     * The Type extended by an AnonymousInnerAllocation (not necessary for AnonymousAllocations
     * and TypeDeclarations, because the type is expressed in the syntax).
     */
    public final static String SUPER_TYPE = "superType";

    public static Type getSuperType(Node n) {
        return (Type)n.getProperty(SUPER_TYPE);
    }
    
    public static Type setSuperType(Node n, Type t) {
      n.setProperty(SUPER_TYPE, t);
      return t;
    }
    
    public static boolean hasSuperType(Node n) {
      return n.hasProperty(SUPER_TYPE);
    }
    

    /** A Thunk<Class<?>> representing the converted type of a primitive cast */
    public final static String CONVERTED_TYPE = "convertedType";

    @SuppressWarnings("unchecked")
    public static Thunk<Class<?>> getConvertedType(Node n) {
        return (Thunk<Class<?>>) n.getProperty(CONVERTED_TYPE);
    }
    
    public static Thunk<Class<?>> setConvertedType(Node n, Thunk<Class<?>> c) {
      n.setProperty(CONVERTED_TYPE, c);
      return c;
    }
    
    public static boolean hasConvertedType(Node n) {
      return n.hasProperty(CONVERTED_TYPE);
    }
    

    /** A Thunk<Class<?>> representing the checked cast type of a cast, method, or field */
    public final static String CHECKED_TYPE = "checkedType";

    @SuppressWarnings("unchecked")
    public static Thunk<Class<?>> getCheckedType(Node n) {
        return (Thunk<Class<?>>) n.getProperty(CHECKED_TYPE);
    }
    
    public static Thunk<Class<?>> setCheckedType(Node n, Thunk<Class<?>> c) {
      n.setProperty(CHECKED_TYPE, c);
      return c;
    }
    
    public static boolean hasCheckedType(Node n) {
      return n.hasProperty(CHECKED_TYPE);
    }
    

    /** A Thunk<Class<?>> representing the erased type of certain expressions and statements. */
    public final static String ERASED_TYPE = "erasedType";

    @SuppressWarnings("unchecked")
    public static Thunk<Class<?>> getErasedType(Node n) {
        return (Thunk<Class<?>>) n.getProperty(ERASED_TYPE);
    }
    
    public static Thunk<Class<?>> setErasedType(Node n, Thunk<Class<?>> c) {
      n.setProperty(ERASED_TYPE, c);
      return c;
    }
    
    public static boolean hasErasedType(Node n) {
      return n.hasProperty(ERASED_TYPE);
    }
    

    /**
     * An Expression representing the (possibly promoted) left side of an assignment
     * or increment/decrement in which the value of the left expression is used to calculate
     * the new value
     */
    public final static String LEFT_EXPRESSION = "leftExpression";

    public static Expression getLeftExpression(Node n) {
        return (Expression) n.getProperty(LEFT_EXPRESSION);
    }
    
    public static Expression setLeftExpression(Node n, Expression exp) {
      n.setProperty(LEFT_EXPRESSION, exp);
      return exp;
    }
    
    public static boolean hasLeftExpression(Node n) {
      return n.hasProperty(LEFT_EXPRESSION);
    }
    

    /**
     * An Expression representing the translated equivalent of the tagged Expression
     */
    public final static String TRANSLATION = "translation";

    public static Expression getTranslation(Node n) {
        return (Expression) n.getProperty(TRANSLATION);
    }
    
    public static Expression setTranslation(Node n, Expression exp) {
      n.setProperty(TRANSLATION, exp);
      return exp;
    }
    
    public static boolean hasTranslation(Node n) {
      return n.hasProperty(TRANSLATION);
    }
    
    /**
     * A Node representing the translated equivalent of the tagged statement (or declaration)
     */
    public final static String STATEMENT_TRANSLATION = "statementTranslation";

    public static Node getStatementTranslation(Node n) {
        return (Node) n.getProperty(STATEMENT_TRANSLATION);
    }
    
    public static Node setStatementTranslation(Node n, Node s) {
      n.setProperty(STATEMENT_TRANSLATION, s);
      return s;
    }
    
    public static boolean hasStatementTranslation(Node n) {
      return n.hasProperty(STATEMENT_TRANSLATION);
    }
    

    /** An Object value of a constant expression */
    public final static String VALUE = "value";

    public static Object getValue(Node n) {
        return n.getProperty(VALUE);
    }
    
    public static Object setValue(Node n, Object o) {
      n.setProperty(VALUE, o);
      return o;
    }
    
    public static boolean hasValue(Node n) {
      return n.hasProperty(VALUE);
    }
    

    /**
     * The errorStrings property contains an array of additional messages (Strings)
     */
    public final static String ERROR_STRINGS = "errorStrings";

    public static String[] getErrorStrings(Node n) {
      return (String[]) n.getProperty(ERROR_STRINGS);
    }
    
    public static String[] setErrorStrings(Node n, String... strings) {
      n.setProperty(ERROR_STRINGS, strings);
      return strings;
    }
    
    public static boolean hasErrorStrings(Node n) {
      return n.hasProperty(ERROR_STRINGS);
    }


    /** A LocalVariable corresponding to the variable declared by the given node */
    public final static String VARIABLE = "variable";
    
    public static LocalVariable getVariable(Node n) {
      return (LocalVariable) n.getProperty(VARIABLE);
    }
    
    public static LocalVariable setVariable(Node n, LocalVariable v) {
      n.setProperty(VARIABLE, v);
      return v;
    }
    
    public static boolean hasVariable(Node n) {
      return n.hasProperty(VARIABLE);
    }
    
    public static final Lambda<Node, LocalVariable> NODE_VARIABLE = new Lambda<Node, LocalVariable>() {
      public LocalVariable value(Node n) { return getVariable(n); }
    };
    
    
    /** DJConstructor used by a constructor invocation */
    public final static String CONSTRUCTOR = "constructor";

    public static DJConstructor getConstructor(Node n) {
      return (DJConstructor) n.getProperty(CONSTRUCTOR);
    }
    
    public static DJConstructor setConstructor(Node n, DJConstructor c) {
      n.setProperty(CONSTRUCTOR, c);
      return c;
    }
    
    public static boolean hasConstructor(Node n) {
      return n.hasProperty(CONSTRUCTOR);
    }


    /** DJField used by a field access or declared by a field declaration */
    public final static String FIELD = "field";

    public static DJField getField(Node n) {
      return (DJField) n.getProperty(FIELD);
    }
    
    public static DJField setField(Node n, DJField f) {
      n.setProperty(FIELD, f);
      return f;
    }
    
    public static boolean hasField(Node n) {
      return n.hasProperty(FIELD);
    }

    /** Method used by a method invocation or declared by a method declaration */
    public final static String METHOD = "method";
    
    public static DJMethod getMethod(Node n) {
      return (DJMethod) n.getProperty(METHOD);
    }
    
    public static DJMethod setMethod(Node n, DJMethod m) {
      n.setProperty(METHOD, m);
      return m;
    }
    
    public static boolean hasMethod(Node n) {
      return n.hasProperty(METHOD);
    }


    /** DJClass declared by a class declaration or referenced by "this" */
    public final static String DJCLASS = "djclass";
    
    public static DJClass getDJClass(Node n) {
      return (DJClass) n.getProperty(DJCLASS);
    }
    
    public static DJClass setDJClass(Node n, DJClass c) {
      n.setProperty(DJCLASS, c);
      return c;
    }
    
    public static boolean hasDJClass(Node n) {
      return n.hasProperty(DJCLASS);
    }


    /** VariableType declared in a class or method signature */
    public final static String TYPE_VARIABLE = "typeVariable";

    public static VariableType getTypeVariable(Node n) {
      return (VariableType) n.getProperty(TYPE_VARIABLE);
    }
    
    public static VariableType setTypeVariable(Node n, VariableType v) {
      n.setProperty(TYPE_VARIABLE, v);
      return v;
    }
    
    public static boolean hasTypeVariable(Node n) {
      return n.hasProperty(TYPE_VARIABLE);
    }

    public static final Lambda<Node, VariableType> NODE_TYPE_VARIABLE = new Lambda<Node, VariableType>() {
      public VariableType value(Node n) { return getTypeVariable(n); }
    };
    
    
    /**
     * A Lambda2<Object, Object, Object> -- determines the operation to be used where it is 
     * ambiguous (for example, a PlusExpression might require addition or concatenation)
     */
    public final static String OPERATION = "operation";

    @SuppressWarnings("unchecked")
    public static Lambda2<Object, Object, Object> getOperation(Node n) {
      return (Lambda2<Object, Object, Object>) n.getProperty(OPERATION);
    }
    
    public static Lambda2<Object, Object, Object> setOperation(Node n, 
                                                               Lambda2<Object, Object, Object> f) {
      n.setProperty(OPERATION, f);
      return f;
    }
    
    public static boolean hasOperation(Node n) {
      return n.hasProperty(OPERATION);
    }

    /**
     * This class contains only static method and constants,
     * so it is not useful to create instances of it.
     */
    protected NodeProperties() {
    }
}
