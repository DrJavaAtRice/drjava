/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import java.lang.reflect.*;
import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.modifier.*;
import koala.dynamicjava.interpreter.throwable.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;

/**
 * A subclass of EvaluationVisitor to do two new things.
 * <OL>
 *   <LI>Check thread interrupted status and throw InterruptedException
 *       if the thread was interrupted.</LI>
 *   <LI>Returns Interpreter.NO_RESULT if the computation
 *       had no result. (This is instead of returning null, which
 *       DynamicJava does.</LI>
 * </OL>
 * 
 * This class is loaded in the Interpreter JVM, not the Main JVM.
 * (Do not use DrJava's config framework here.)
 *
 * @version $Id$
 */

public class EvaluationVisitorExtension extends EvaluationVisitor {
  private Context _context;
  public EvaluationVisitorExtension(Context ctx) {
    super(ctx);
    _context = ctx;
  }

  private void _checkInterrupted(Node node) {
    // An interesting and arcane Thread fact: There are two different
    // methods to check if a Thread is interrupted. (See the javadocs.)
    // Thread.isInterrupted() gets the status but doesn't reset it,
    // while Thread.interrupted() gets the status and resets it.
    // This code did not work when I used isInterrupted.
    if (Thread.currentThread().interrupted()) {
      throw new InterpreterInterruptedException(node.getBeginLine(),
                                                node.getBeginColumn(),
                                                node.getEndLine(),
                                                node.getEndColumn());
    }
  }

  public Object visit(WhileStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return Interpreter.NO_RESULT;
  }

  public Object visit(ForStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return Interpreter.NO_RESULT;
  }

  public Object visit(DoStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return Interpreter.NO_RESULT;
  }

  public Object visit(SwitchStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return Interpreter.NO_RESULT;
  }

  public Object visit(LabeledStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return Interpreter.NO_RESULT;
  }

  public Object visit(SynchronizedStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return Interpreter.NO_RESULT;
  }

  public Object visit(TryStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return Interpreter.NO_RESULT;
  }

  public Object visit(IfThenStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return Interpreter.NO_RESULT;
  }

  public Object visit(IfThenElseStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return Interpreter.NO_RESULT;
  }

  public Object visit(BlockStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return Interpreter.NO_RESULT;
  }

  public Object visit(Literal node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  /**
   * Overrides EvaluationVisitor to enforce a proper type check at
   * runtime. It combines code from the actual visit code in
   * EvaluationVisitor as well as code from the modify method
   * in VariableModifier.
   */
  public Object visit(VariableDeclaration node) {
    _checkInterrupted(node);
    Class c = NodeProperties.getType(node.getType());

    if (node.getInitializer() != null) {
      Object o = performCast(c, node.getInitializer().acceptVisitor(this));

      // Forces a runtime type-check on the cast.
      String name = node.getName();

      if (!(c.isPrimitive()                     ||
            o == null                          ||
            c.isAssignableFrom(o.getClass()))) {
        Exception e = new ClassCastException(name);
        throw new CatchedExceptionError(e, node);
      }

      if (node.isFinal()) {
        _context.setConstant(node.getName(), o);
      } else {
        _context.set(node.getName(), o);
      }
    } else {
      if (node.isFinal()) {
        _context.setConstant(node.getName(), UninitializedObject.INSTANCE);
      } else {
        // Non-final variables have default values, and are not uninitialized.
        // Primitive variables have special default values, Objects default to null.
        // Fixes bug #797515.
//        _context.set(node.getName(), UninitializedObject.INSTANCE);
        Object value = null;
        if (!c.isPrimitive()) {
          value = null;
        }
        else if (c == byte.class) {
          value = new Byte((byte)0);
        }
        else if (c == short.class) {
          value = new Short((short)0);
        }
        else if (c == int.class) {
          value = new Integer(0);
        }
        else if (c == long.class) {
          value = new Long(0L);
        }
        else if (c == float.class) {
          value = new Float(0.0f);
        }
        else if (c == double.class) {
          value = new Double(0.0d);
        }
        else if (c == char.class) {
          value = new Character('\u0000');
        }
        else if (c == boolean.class) {
          value = new Boolean(false);
        }
        _context.set(node.getName(), value);
      }
    }
    return Interpreter.NO_RESULT;
  }

  public Object visit(ObjectFieldAccess node) {
    _checkInterrupted(node);    
    return super.visit(node);
  }

  public Object visit(ObjectMethodCall node) {
    _checkInterrupted(node);
    Method m = (Method) node.getProperty(NodeProperties.METHOD);
//    m.setAccessible(true);
    Object ret = super.visit(node);

    // workaround to not return null for void returns
    if (m.getReturnType().equals(Void.TYPE)) {
      return Interpreter.NO_RESULT;
    }
    else {
      return ret;
    }
  }

  public Object visit(StaticFieldAccess node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(SuperFieldAccess node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(SuperMethodCall node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(StaticMethodCall node) {
    _checkInterrupted(node);
    Method m = (Method) node.getProperty(NodeProperties.METHOD);

    // DynamicJava doesn't check that the method is really static!
    if (! Modifier.isStatic(m.getModifiers())) {
      StringBuffer buf = new StringBuffer();
      buf.append(m.getDeclaringClass());
      buf.append(".");
      buf.append(m.getName());
      buf.append("(");

      boolean first = true;
      Class[] params = m.getParameterTypes();
      for (int i = 0; i < params.length; i++) {
        if (first) {
          first = false;
        }
        else {
          buf.append(", ");
        }

        buf.append(params[i].getName());
      }

      buf.append(")");
      buf.append(" is not a static method.");

      throw new InteractionsException(buf.toString());
    }

    Object ret = super.visit(node);

    // workaround to not return null for void returns
    if (m.getReturnType().equals(Void.TYPE)) {
      return Interpreter.NO_RESULT;
    }
    else {
      return ret;
    }
  }

  public Object visit(SimpleAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(QualifiedName node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(TypeExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(SimpleAllocation node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ArrayAllocation node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ArrayInitializer node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ArrayAccess node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(InnerAllocation node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ClassAllocation node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(NotExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ComplementExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(PlusExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(MinusExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(AddExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(AddAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(SubtractExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(SubtractAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(MultiplyExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(MultiplyAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(DivideExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(DivideAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(RemainderExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(RemainderAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(EqualExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(NotEqualExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(LessExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(LessOrEqualExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(GreaterExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(GreaterOrEqualExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(InstanceOfExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ConditionalExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(PostIncrement node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(PreIncrement node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(PostDecrement node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(PreDecrement node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(CastExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(BitAndExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(BitAndAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ExclusiveOrExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ExclusiveOrAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(BitOrExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(BitOrAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ShiftLeftExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ShiftLeftAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ShiftRightExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ShiftRightAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(UnsignedShiftRightExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(UnsignedShiftRightAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(AndExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(OrExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(FunctionCall node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(PackageDeclaration node) {
    return Interpreter.NO_RESULT;
  }

  public Object visit(ImportDeclaration node) {
    return Interpreter.NO_RESULT;
  }

  public Object visit(EmptyStatement node) {
    return Interpreter.NO_RESULT;
  }

    /**
     * Performs a dynamic cast. This method acts on primitive wrappers.
     * @param tc the target class
     * @param o  the object to cast
     */
  protected static Object performCast(Class tc, Object o) {
    Class ec = (o != null) ? o.getClass() : null;
    
    if (tc != ec && tc.isPrimitive() && ec != null) {
      if (tc != char.class && ec == Character.class) {
        o = new Integer(((Character)o).charValue());
      } else if (tc == byte.class) {
        o = new Byte(((Number)o).byteValue());
      } else if (tc == short.class) {
        o = new Short(((Number)o).shortValue());
      } else if (tc == int.class) {
        o = new Integer(((Number)o).intValue());
      } else if (tc == long.class) {
        o = new Long(((Number)o).longValue());
      } else if (tc == float.class) {
        o = new Float(((Number)o).floatValue());
      } else if (tc == double.class) {
        o = new Double(((Number)o).doubleValue());
      } else if (tc == char.class && ec != Character.class) {
        o = new Character((char)((Number)o).shortValue());
      }
    }
    return o;
  }
}
