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

package koala.dynamicjava.interpreter.context;

import java.lang.reflect.*;
import java.util.*;

import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.interpreter.modifier.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.util.*;

/**
 * The classes that implements this interface represent
 * contexts of execution
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/07/03
 */

public interface Context extends SimpleContext {
    /**
     * Sets the additional class loader container
     */
    void setAdditionalClassLoaderContainer(ClassLoaderContainer clc);

    /**
     * Allows the scripts to access private fields.
     */
    void setAccessible(boolean accessible);
 
    /**
     * Returns the accessibility state of this context.
     */
    boolean getAccessible();

   /**
     * Sets the defined functions
     */
    void setFunctions(List<MethodDeclaration> l);

    /**
     * Returns the defined functions
     */
    List<MethodDeclaration> getFunctions();

    /**
     * Returns the current interpreter
     */
    Interpreter getInterpreter();

    /**
     * Returns the importation manager
     */
    ImportationManager getImportationManager();

    /**
     * Sets the importation manager
     */
    void setImportationManager(ImportationManager im);

    /**
     * Whether a simple identifier represents an existing
     * variable or field in this context
     * @param name the identifier
     */
    boolean exists(String name);

    /**
     * Whether a simple identifier is a class
     * @param name the identifier
     */
    boolean classExists(String name);

    /**
     * Tests whether a variable or a field is defined in this context
     * @param name the name of the entry
     * @return false if the variable is undefined
     */
    boolean isDefined(String name);

    /**
     * Sets the current package
     * @param pkg the package name
     */
    void setCurrentPackage(String pkg);

    /**
     * Returns the current package
     */
    String getCurrentPackage();

    /**
     * Declares a new import-on-demand clause
     * @param pkg the package name
     */
    void declarePackageImport(String pkg);

    /**
     * Declares a new single-type-import clause
     * @param cname the fully qualified class name
     * @exception ClassNotFoundException if the class cannot be found
     */
    void declareClassImport(String cname) throws ClassNotFoundException;
    
    /**
     * Declares a new import-on-demand clause for the importation of the static methods and fields of a class
     * @param cname the fully qualified class name
     */
    void declareClassStaticImport(String cname) throws ClassNotFoundException;
    
    /**
     * Declares a new single-type-import clause for the importation of a static member
     * @param member the method or field name
     */
    void declareMemberStaticImport(String member) throws ClassNotFoundException;

    /**
     * Returns the fully qualified class name that wraps the given staticly imported method
     * @param methodName the method name
     * @param args the argument list for the method
     */
    List<IdentifierToken> getQualifiedName(String methodName, Class<?>[] args) throws NoSuchMethodException;
    
    /**
     * Returns the fully qualified class name that wraps the given staticly imported field
     * @param fieldName the field name
     */
    List<IdentifierToken> getQualifiedName(String fieldName) throws NoSuchFieldException;
    
    /**
     * Returns true iff the field has been staticly imported
     */
    boolean isFieldImported(String name);
    
    /**
     * Returns the default qualifier for this context
     * @param node the current node
     */
    Node getDefaultQualifier(Node node);

    /**
     * Returns the default qualifier for this context
     * @param node the current node
     * @param tname the qualifier of 'this'
     */
    Node getDefaultQualifier(Node node, String tname);

    /**
     * Returns the modifier that match the given node
     * @param node a tree node
     */
    LeftHandSideModifier getModifier(QualifiedName node);

    /**
     * Returns the modifier that match the given node
     * @param node a tree node
     */
    LeftHandSideModifier getModifier(StaticFieldAccess node);

    /**
     * Returns the modifier that match the given node
     * @param node a tree node
     */
    LeftHandSideModifier getModifier(ObjectFieldAccess node);

    /**
     * Returns the modifier that match the given node
     * @param node a tree node
     */
    LeftHandSideModifier getModifier(SuperFieldAccess node);

    /**
     * Returns the default argument to pass to methods in this context
     */
    Object getHiddenArgument();

    /**
     * Creates the tree that is associated with the given name
     * @param node the current node
     * @param name the variable name
     * @exception IllegalStateException if the variable is not defined
     */
    Expression createName(Node node, IdentifierToken name);

    /**
     * Defines a MethodDeclaration as a function
     * @param node the function declaration
     */
    void defineFunction(MethodDeclaration node);

    /**
     * Defines a class from its syntax tree
     * @param node the class declaration
     */
    void defineClass(TypeDeclaration node);

    /**
     * Looks for a class
     * @param cname the class name
     * @exception ClassNotFoundException if the class cannot be found
     */
    Class<?> lookupClass(String cname) throws ClassNotFoundException;

    /**
     * Looks for a class (context-free lookup)
     * @param cname the class name
     * @param ccname the fully qualified name of the context class
     * @exception ClassNotFoundException if the class cannot be found
     */
    Class<?> lookupClass(String cname, String ccname) throws ClassNotFoundException;

    /**
     * Sets the properties of a SimpleAllocation node
     * @param node the allocation node
     * @param c the class of the constructor
     * @param args the classes of the arguments of the constructor
     */
    Class<?> setProperties(SimpleAllocation node, Class<?> c, Class<?>[] args);

    /**
     * Sets the properties of a ClassAllocation node
     * @param node the allocation node
     * @param c the class of the constructor
     * @param args the classes of the arguments of the constructor
     * @param memb the class members
     */
    Class<?> setProperties(ClassAllocation node, Class<?> c, Class<?>[] args, List<Node> memb);

    /**
     * Looks for a constructor
     * @param c  the class of the constructor
     * @param params the parameter types
     * @exception NoSuchMethodException if the constructor cannot be found
     */
    Constructor lookupConstructor(Class<?> c, Class<?>[] params)
 throws NoSuchMethodException;

    /**
     * Invokes a constructor
     * @param node the SimpleAllocation node
     * @param args the arguments
     */
    Object invokeConstructor(SimpleAllocation node, Object[] args);

    /**
     * Invokes a constructor
     * @param node the ClassAllocation node
     * @param args the arguments
     */
    Object invokeConstructor(ClassAllocation node, Object[] args);

    /**
     * Looks for a method
     * @param prefix the method prefix
     * @param mname  the method name
     * @param params the parameter types
     * @exception NoSuchMethodException if the method cannot be found
     */
    Method lookupMethod(Node prefix, String mname, Class<?>[] params)
 throws NoSuchMethodException;

   
    /**
     * Looks for a function
     * @param mname  the function name
     * @param params the parameter types
     * @exception NoSuchFunctionException if the function cannot be found
     */
    MethodDeclaration lookupFunction(String mname, Class<?>[] params)
 throws NoSuchFunctionException;

    /**
     * Looks for a super method
     * @param node the current node
     * @param mname  the method name
     * @param params the parameter types
     * @exception NoSuchMethodException if the method cannot be found
     */
    Method lookupSuperMethod(Node node, String mname, Class<?>[] params)
 throws NoSuchMethodException;

    /**
     * Looks for a field
     * @param fc the field class
     * @param fn the field name
     * @exception NoSuchFieldException if the field cannot be found
     * @exception AmbiguousFieldException if the field is ambiguous
     */
    Field getField(Class<?> fc, String fn) throws NoSuchFieldException,
                                               AmbiguousFieldException;

    /**
     * Looks for a field in the super class
     * @param node the current node
     * @param fn the field name
     * @exception NoSuchFieldException if the field cannot be found
     * @exception AmbiguousFieldException if the field is ambiguous
     */
    Field getSuperField(Node node, String fn) throws NoSuchFieldException,
                                                     AmbiguousFieldException;
}
