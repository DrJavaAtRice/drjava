/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 *
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.StringTokenizer;

import edu.rice.cs.util.UnexpectedException;

/**
 * This class is an extension to DynamicJavaAdapter that allows us to 
 * process expressions involving the "this" keyword correctly in the 
 * current debug interpreter context. This allows users to debug outer 
 * classes and their fields using the usual Java syntax of outerclass.this. 
 * This is done by holding on to the class name of "this" and by translating 
 * references to outer instance classes to field accesses in the form 
 * "this.this$N.this$N-1...".
 * 
 * @version $Id$
 */
public class JavaDebugInterpreter extends DynamicJavaAdapter {
  // TODO: support methods  
  
  /**
   * This interpreter's name.
   */
  protected final String _name;
  
  /**
   * The class name of the "this" object for the currently
   * suspended thread.
   */
  protected String _thisClassName;
  
  /**
   * The name of the package containing _this, if any.
   */
  protected String _thisPackageName;
  
  /**
   * Extends IdentityVisitor to convert all instances
   * of ThisExpressions in the tree to either 
   * QualifiedName or an ObjectFieldAccess
   */
  protected Visitor _translationVisitor;
  
  /**
   * Creates a new debug interpreter.
   * @param name the name of the interpreter
   * @param className the class name of the current context of "this"
   */
  public JavaDebugInterpreter(String name, String className) {
    _name = name;
    setClassName(className);
    _translationVisitor = makeTranslationVisitor();
  }
  
  /**
   * Processes the tree before evaluating it.
   * The translation visitor visits each node in the tree
   * for the given statement or expression and converts
   * the necessary nodes.
   * @param node Tree to process
   */
  public Node processTree(Node node) {
    return (Node) node.acceptVisitor(_translationVisitor);
  }  
  
  public GlobalContext makeGlobalContext(TreeInterpreter i) {
    return new GlobalContext(i) {  
      public boolean exists(String name) {
        return (super.exists(name)) || 
          (_getObjectFieldAccessForField(name, this) != null) ||
          (_getStaticFieldAccessForField(name, this) != null) ||
          (_getReferenceTypeForField(name, this) != null);
      }
    };
  }
  
  /**
   * Given a field, looks at enclosing classes until it finds
   * one that contains the field. It returns the ObjectFieldAccess
   * that represents the field.
   * @param field the name of the field
   * @param context the context
   * @return the ObjectFieldAccess that represents the field or null
   * if it cannot find the field in any enclosing class.
   */
  protected ObjectFieldAccess _getObjectFieldAccessForField(String field, Context context) {
    TypeChecker tc = makeTypeChecker(context);
    int numDollars = _getNumDollars(_thisClassName);
    Expression expr = null;
    for (int i = 0; i <= numDollars; i++) {          
      expr = _buildObjectFieldAccess(i, numDollars);
      expr = new ObjectFieldAccess(expr, field);
      try {
        // the type checker will tell us if it's a field
        tc.visit((ObjectFieldAccess)expr);
        return (ObjectFieldAccess)expr;
      }
      catch (ExecutionError e2) {
        // do nothing, try an outer class
      }
    }
    return null;
  }
  
  /**
   * Given a method, looks at enclosing classes until it finds
   * one that contains the field. It returns the ObjectMethodCall
   * that represents the method.
   * @param method the method
   * @param context the context
   * @return the ObjectMethodCall that represents the method or null
   * if it cannot find the method in any enclosing class.
   */
  protected ObjectMethodCall _getObjectMethodCallForFunction(MethodCall method, Context context) {
    TypeChecker tc = makeTypeChecker(context);
    int numDollars = _getNumDollars(_thisClassName);
    String methodName = method.getMethodName();
    List args = method.getArguments();
    Expression expr = null;
    for (int i = 0; i <= numDollars; i++) {          
      expr = _buildObjectFieldAccess(i, numDollars);
      expr = new ObjectMethodCall(expr, methodName, args, null, 0, 0, 0, 0);
      try {
        // the type checker will tell us if it's a field
        tc.visit((ObjectMethodCall)expr);
        return (ObjectMethodCall)expr;
      }
      catch (ExecutionError e2) {
        // do nothing, try an outer class
      }
    }
    return null;
  } 
  
  /**
   * Given a field in a static context, looks at enclosing classes until it 
   * finds one that contains the field. It returns the StaticFieldAccess
   * that represents the field.
   * @param field the name of the field
   * @param context the context
   * @return the StaticFieldAccess that represents the field or null
   * if it cannot find the field in any enclosing class.
   */  
  protected StaticFieldAccess _getStaticFieldAccessForField(String field, Context context) {
    TypeChecker tc = makeTypeChecker(context);
    int numDollars = _getNumDollars(_thisClassName);
    StaticFieldAccess expr = null;
    String currClass = _thisClassName;
    int index = currClass.length();
    // iterate outward trying to find the field
    for (int i = 0; i <= numDollars; i++) {
      currClass = currClass.substring(0, index);
      ReferenceType rt = new ReferenceType(currClass);
      expr = new StaticFieldAccess(rt, field);
      try {
        // the type checker will tell us if it's a field
        tc.visit(expr);
        return expr;
      }
      catch (ExecutionError e2) {
        // try an outer class
        index = currClass.lastIndexOf("$");       
      }
    }
    return null;
  }  
  
  /**
   * Given a method in a static context, looks at enclosing classes until it 
   * finds one that contains the method. It returns the StaticMethodCall
   * that represents the method.
   * @param method the method
   * @param context the context
   * @return the StaticMethodCall that represents the method or null
   * if it cannot find the method in any enclosing class.
   */  
  protected StaticMethodCall _getStaticMethodCallForFunction(MethodCall method, Context context) {
    TypeChecker tc = makeTypeChecker(context);
    int numDollars = _getNumDollars(_thisClassName);
    String methodName = method.getMethodName();
    List args = method.getArguments();
    StaticMethodCall expr = null;
    String currClass = _thisClassName;
    int index = currClass.length();
    // iterate outward trying to find the method
    for (int i = 0; i <= numDollars; i++) {
      currClass = currClass.substring(0, index);
      ReferenceType rt = new ReferenceType(currClass);
      expr = new StaticMethodCall(rt, methodName, args);
      try {
        // the type checker will tell us if it's a field
        tc.visit(expr);
        return expr;
      }
      catch (ExecutionError e2) {
        // try an outer class
        index = currClass.lastIndexOf("$");       
      }
    }
    return null;
  }
  
  /**
   * Given the name of an not fully qualified outer class, return the fully qualified
   * ReferenceType that corresponds to that class. This is called when the user
   * references a static field of an outer class.
   * @param field the name of the not fully qualified outer class
   * @param context the context
   * @return the ReferenceType that represents the field(in this case, 
   * really a class) or null if it cannot load the corresponding class in the
   * class loader.
   */  
  protected ReferenceType _getReferenceTypeForField(String field, Context context) {
    TypeChecker tc = makeTypeChecker(context);
    int index = _indexOfWithinBoundaries(_thisClassName, field);
    if (index != -1) {
      // field may be of form outerClass$innerClass. 
      // We want to match the inner most class.
      int lastDollar = field.lastIndexOf("$");
      if (lastDollar != -1) {
        field = field.substring(lastDollar + 1, field.length());
      }
      LinkedList list = new LinkedList();
      StringTokenizer st = new StringTokenizer(_thisClassName, "$");
      String currString = st.nextToken();
      while (!currString.equals(field)) {
        list.add(new Identifier(currString));
        currString = st.nextToken();
      }
      list.add(new Identifier(field));
      ReferenceType rt = new ReferenceType(list);
      try {
        // the type checker will tell us if it's a class
        tc.visit(rt);
        return rt;
      }
      catch (ExecutionError e) {
        return null;
      }
    }
    else {
      return null;
    }
  }
  
  
  /**
   * Sets the class name of "this", parsing out the package name.
   */
  protected void setClassName(String className) {
    int indexLastDot = className.lastIndexOf(".");
    if (indexLastDot == -1) {
      _thisPackageName = "";
    }
    else {
      _thisPackageName = className.substring(0,indexLastDot);
    }
    _thisClassName = className.substring(indexLastDot + 1, className.length());
  }
  
  /**
   * Helper method to convert a ThisExpression to a QualifiedName.
   * Allows us to redefine "this" in a debug interpreter.
   * @param node ThisExpression
   * @return corresponding QualifiedName
   */
  protected QualifiedName _convertThisToName(ThisExpression node) {
    List ids = new LinkedList();
    ids.add(new Identifier("this", node.getBeginLine(), node.getBeginColumn(),
                           node.getEndLine(), node.getEndColumn()));
    return new QualifiedName(ids, node.getFilename(),
                             node.getBeginLine(), node.getBeginColumn(),
                             node.getEndLine(), node.getEndColumn());
  }
  
  /**
   * Helper method to convert a ThisExpression to a FieldAccess.
   * Allows us to access fields of outer classes in a debug interpreter.
   * @param node ThisExpression
   * @return corresponding FieldAccess
   */
  protected Expression _convertThisToObjectFieldAccess(ThisExpression node) {
    String className = node.getClassName();
    int numToWalk = verifyClassName(className);
    int numDollars = _getNumDollars(_thisClassName);
    // if numToWalk == 0, just return "this"
    if (numToWalk == -1) {
      throw new ExecutionError("malformed.expression");
    }
    else {
      return _buildObjectFieldAccess(numToWalk, numDollars);
    }
  }
  
  /**
   * Builds a ThisExpression that has no class name.
   * @return an unqualified ThisExpression
   */
  protected ThisExpression buildUnqualifiedThis() {
    LinkedList ids = new LinkedList();
    return new ThisExpression(ids, "", 0, 0, 0, 0);
  }
  
  /**
   * Helper method to build an ObjectFieldAccess for a ThisExpression
   * given the number of classes to walk and the number of dollars.
   * @param numToWalk number of outer classes to walk through
   * @param numDollars numer of dollars in _thisClassName
   * @return a QualifiedName is numtoWalk is zero or an ObjectFieldAccess
   */
  private Expression _buildObjectFieldAccess(int numToWalk, int numDollars) {     
    if (numToWalk == 0) {
      return _convertThisToName(buildUnqualifiedThis());
    }
    else {
      return new ObjectFieldAccess(_buildObjectFieldAccess(numToWalk - 1, numDollars), "this$" + (numDollars - numToWalk));
    }
  }
  
  /**
   * Returns the index of subString within string if the substring is
   * either bounded by the ends of string or by $'s.
   * @param string the super string
   * @param subString the subString
   * @return the index of string that subString begins at or -1
   * if subString is not in string or is not validly bounded
   */
  private int _indexOfWithinBoundaries(String string, String subString) {
    int index = string.indexOf(subString);
    if (index == -1) {
      return index;
    }
    // subString is somewhere in string
    else {
      // ends at legal boundary
      if (((string.length() == subString.length() + index) ||
           (string.charAt(subString.length() + index) == '$'))
            &&
          // begins at legal boundary
          ((index == 0) ||
           (string.charAt(index-1) == '$'))) {
        return index;
      }
      else {
        return -1;
      }
    }
  }
  
  /**
   * Returns the number of dollar characters in
   * a given String.
   * @param classname the string to be examined
   * @return the number of dollars in the string
   */
  private int _getNumDollars(String className) {
    int numDollars = 0;
    int index = className.indexOf("$");
    while (index != -1) {
      numDollars++;
      index = className.indexOf("$", index + 1);
    }
    return numDollars;
  }
  
  /**
   * Checks if the className passed in is a valid className.
   * @param classname the className of the ThisExpression
   * @return the number of outer classes to walk out to
   */
  protected int verifyClassName(String className) {
    boolean hasPackage = false;
    if (!_thisPackageName.equals("")) {
      int index = className.indexOf(_thisPackageName);
      if (index == 0) {
        hasPackage = true;
        // className begins with the package name
        index = _thisPackageName.length() + 1;
        if (index >= className.length()) {
          return -1;
        }
        // strip off the package
        className = className.substring(index, className.length());
      }
    }
    
    className = className.replace('.', '$');
    int indexWithBoundaries = _indexOfWithinBoundaries(_thisClassName, className);
    if ((hasPackage && indexWithBoundaries != 0) ||
        (indexWithBoundaries == -1)) {
      return -1;
    }
    else {
      return _getNumDollars(_thisClassName.substring(indexWithBoundaries + className.length()));      
    }
  }
  
  /**
   * Converts the ThisExpression to a QualifiedName
   * if it has no class name or an ObjectFieldAccess
   * if it does.
   * @param node the expression to visit
   * @return the converted form of the node
   */
  protected Expression visitThis(ThisExpression node) {
    if (node.getClassName().equals("")) {
      return _convertThisToName(node);
    }
    else {      
      return _convertThisToObjectFieldAccess(node);
    }
  }
  
  /**
   * Makes an anonymous IdentityVisitor that overrides
   * visit for a ThisExpresssion to convert it to
   * either a QualifiedName or an ObjectFieldAccess
   */
  public Visitor makeTranslationVisitor() {
    return new IdentityVisitor() {
      public Object visit(ThisExpression node) {
        Expression e = visitThis(node);
        if (e instanceof QualifiedName) {
          return visit((QualifiedName)e);
        }
        else if (e instanceof ObjectFieldAccess) {
          return visit((ObjectFieldAccess)e);
        }
        else {
          throw new UnexpectedException(new IllegalArgumentException("Illegal type of Expression"));
        }
      }
    };
  }
  
  /**
   * Factory method to make a new NameChecker that tries to find the
   * right scope for QualifiedNames.
   * @param context the context
   * @return visitor the visitor
   */
  public NameVisitor makeNameVisitor(final Context context) {
    return new NameVisitor(context) {
      public Object visit(QualifiedName node) {
        try {
          return super.visit(node);
        }
        catch(ExecutionError e) {
          List ids = node.getIdentifiers();
          Iterator iter = ids.iterator();
          String field = ((IdentifierToken)iter.next()).image();
          while (iter.hasNext()) {
            IdentifierToken t = (IdentifierToken)iter.next();
            field += "$" + t.image();
          }
          // This error is thrown only if this QualifiedName is not 
          // a local variable or a class
          if (context.isDefined("this")) {
            // Check if it's a field or outer field if we're not in a
            // static context.
            ObjectFieldAccess ofa = _getObjectFieldAccessForField(field, context);
            if (ofa != null) {
              return ofa;
            }
          }
          else {
            // We're in a static context.
            StaticFieldAccess sfa = _getStaticFieldAccessForField(field, context);
            if (sfa != null) {
              return sfa;
            }
            else {
              // This is possibly a substring of our current context's class name.
              // (e.g. The user is trying to evaluate MonkeyOuter.someField and we
              // have to convert MonkeyOuter to MonkeyMostOuter$MonkeyOuter)
              // Try qualifying it.
              ReferenceType rt = _getReferenceTypeForField(field, context);
              if (rt != null) {
                return rt;
              }
            }
          }
          // Didn't find this field in any outer class. Throw original exception.
          throw e;          
        }
      }
      public Object visit(ObjectMethodCall node) {
//        try {
          MethodCall method = (MethodCall) super.visit(node);
          // if (method != null) this object method call is either a method with no
          // class before it or is a static method call
          if (method != null) {
            if (method instanceof StaticMethodCall) {
              return method;
            }
            // now we know that method is a FunctionCall
            else if (context.isDefined("this")) {
              ObjectMethodCall omc = _getObjectMethodCallForFunction(method, context);
              if (omc != null) {
                return omc;
              }
              else {
                return method;
              }
            }
            // it's a FunctionCall from a static context
            else {
              StaticMethodCall smc = _getStaticMethodCallForFunction(method, context);
              if (smc != null) {
                return smc;
              }
              else {
                return method;                  
              }
            }
          }
          else {
            return null;
          }
//        }
//        catch (ExecutionError e) {
//          // see if the expression is a static inner class
//          
//        }
      }
    };
  }
  
  /**
   * Factory method to make a new TypeChecker that treats "this" as a variable.
   * @param context the context
   * @return visitor the visitor
   */
  public TypeChecker makeTypeChecker(final Context context) {
    return new TypeChecker(context) {
      /*
      public Object visit(ThisExpression node) {
        Expression e = visitThis(node);
        if (e instanceof QualifiedName) {
          return visit((QualifiedName)e);
        }
        else if (e instanceof ObjectFieldAccess) {
          return visit((ObjectFieldAccess)e);
        }
        else {
          throw new UnexpectedException(new IllegalArgumentException("Illegal type of Expression"));
        }
      }*/
      /**
       * Visits a QualifiedName, returning our class if it is "this"
       * @param node the node to visit
       */
      public Object visit(QualifiedName node) {
        String var = node.getRepresentation();
        if ("this".equals(var)) {
          try {
            String cName = _thisClassName.replace('$', '.');
            if (!_thisPackageName.equals("")) {
              cName = _thisPackageName + "." + cName;
            }
            Class c = context.lookupClass(cName);
            node.setProperty(NodeProperties.TYPE, c);
            node.setProperty(NodeProperties.MODIFIER, context.getModifier(node));
            return c;
          }
          catch (ClassNotFoundException cnfe) {
            throw new ExecutionError("undefined.class", node);
          }
        }
        else return super.visit(node);
      }

    };
  }
}