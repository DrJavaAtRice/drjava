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
import koala.dynamicjava.interpreter.modifier.*;
import koala.dynamicjava.tree.*;

/**
 * This interface contains the names of the syntax tree properties
 * defined by the interpretative kernel
 *
 * @author  Stephane Hillion
 * @version 1.1 - 1999/11/20
 */

public class NodeProperties {
    /**
     * The componentType property is defined for each array allocation.
     * It contains a Class object.
     */
    public final static String COMPONENT_TYPE = "componentType";

    /**
     * The constructor property is defined for constructor call
     * It contains a Constructor object
     */
    public final static String CONSTRUCTOR = "constructor";

    /**
     * The field property is defined for field access node
     * It contains a Field object
     */
    public final static String FIELD = "field";

    /**
     * The method property is defined for method access node
     * It contains a Method object
     */
    public final static String METHOD = "method";

    /**
     * The function property is defined for function access node
     * It contains a MethodDeclaration object
     */
    public final static String FUNCTION = "function";

    /**
     * The functions property is defined for function access node
     * It contains a List object
     */
    public final static String FUNCTIONS = "functions";

    /**
     * The modifier property is defined for each variable.
     * It contains a LeftHandSideModifier object.
     */
    public final static String MODIFIER = "modifier";

    /**
     * The type property is defined for each expression
     * It contains a Class object
     */
    public final static String TYPE = "type";

    /**
     * The variables property is defined for each node where
     * a new scope is entered.
     * It contains a set of strings.
     */
    public final static String VARIABLES = "variables";

    /**
     * The value property is defined for each constant expression.
     * It contains a java object.
     */
    public final static String VALUE = "value";

    /**
     * The innerClass property
     */
    public final static String INNER_ALLOCATION = "innerAllocation";

    /**
     * The outerInnerClass property
     */
    public final static String OUTER_INNER_ALLOCATION = "outerInnerAllocation";

    /**
     * The importation manager property
     */
    public final static String IMPORTATION_MANAGER = "importationManager";

    /**
     * The instanceInitializer property
     */
    public final static String INSTANCE_INITIALIZER = "instanceInitializer";

    /**
     * The errorStrings property contains an array of additional messages
     */
    public final static String ERROR_STRINGS = "errorStrings";

    /**
     * Returns the type property of a node
     */
    public static Class getComponentType(Node n) {
        return (Class)n.getProperty(COMPONENT_TYPE);
    }

    /**
     * Returns the modifier property of a node
     */
    public static LeftHandSideModifier getModifier(Node n) {
        return (LeftHandSideModifier)n.getProperty(MODIFIER);
    }

    /**
     * Returns the type property of a node
     */
    public static Class getType(Node n) {
        return (Class)n.getProperty(TYPE);
    }

    /**
     * Returns the type property of a node when it is a class info
     */
    public static ClassInfo getClassInfo(Node n) {
        return (ClassInfo)n.getProperty(TYPE);
    }

    /**
     * This class contains only static method and constants,
     * so it is not useful to create instances of it.
     */
    protected NodeProperties() {
    }
}
