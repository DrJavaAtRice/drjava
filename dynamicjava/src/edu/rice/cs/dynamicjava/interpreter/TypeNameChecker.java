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

/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2008 JavaPLT group at Rice University (drjava@rice.edu)
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
 *       drjava@rice.edu.
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

package edu.rice.cs.dynamicjava.interpreter;

import java.util.*;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Lambda;

import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.TypeSystem.*;
import edu.rice.cs.dynamicjava.symbol.type.*;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.tiger.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.interpreter.error.ExecutionError;

import static koala.dynamicjava.interpreter.NodeProperties.*;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/**
 * This tree visitor checks the typing rules for TypeNames and converts the name to a Type.
 * The following properties (from {@code NodeProperties}) are set:<ul>
 * <li>TYPE on all {@code TypeName}s</li>
 * </ul>
 */
public class TypeNameChecker {
  
  private final TypeContext context;
  private final TypeSystem ts;
  @SuppressWarnings("unused") private final Options opt;
  private final TypeNameVisitor visitor; // singleton per outer instance

  public TypeNameChecker(TypeContext ctx, Options options) {
    context = ctx;
    ts = options.typeSystem();
    opt = options;
    visitor = new TypeNameVisitor();
  }
  
  /** Get the type corresponding to {@code t}; verify that it is well-formed. */
  public Type check(TypeName t) {
    Type result = t.acceptVisitor(visitor);
    ensureWellFormed(t);
    return result;
  }
  
  /**
   * Get the type corresponding to {@code t}; verify that it is structurally well-formed, but
   * delay full well-formedness checking until a later {@link #ensureWellFormed} call. 
   */
  public Type checkStructure(TypeName t) {
    return t.acceptVisitor(visitor);
  }
  
  /**
   * Verify that a TypeName that has already been checked is well-formed (according to
   * {@link TypeSystem#isWellFormed}).
   */
  public void ensureWellFormed(TypeName t) {
    if (!ts.isWellFormed(getType(t))) {
      throw new ExecutionError("malformed.type", t);
    }
  }
  
  /** Invoke {@link #check} on each element of a list. */
  public Iterable<Type> checkList(Iterable<? extends TypeName> l) {
    Iterable<Type> result = IterUtil.mapSnapshot(l, visitor);
    ensureWellFormedList(l);
    return result;
  }
  
  /** Invoke {@link #checkStructure} on each element of a list. */
  public Iterable<Type> checkStructureForList(Iterable<? extends TypeName> l) {
    Iterable<Type> result = IterUtil.mapSnapshot(l, visitor);
    return result;
  }
  
  /** Invoke {@link #ensureWellFormed} on each element of a list. */
  public void ensureWellFormedList(Iterable<? extends TypeName> l) {
    for (TypeName t : l) { ensureWellFormed(t); }
  }
  
  
  /**
   * Tag the given type parameters with a new VariableType, and set the bounds appropriately;
   * verify that the results are well-formed.
   */
  public void checkTypeParameters(TypeParameter[] tparams) {
    checkStructureForTypeParameters(tparams);
    ensureWellFormedTypeParameters(tparams);
  }

  /** Tag the given type parameters with a new VariableType, and set the bounds appropriately. */
  public void checkStructureForTypeParameters(TypeParameter[] tparams) {
    for (TypeParameter tparam : tparams) {
      setTypeVariable(tparam, new VariableType(new BoundedSymbol(tparam, tparam.getRepresentation())));
    }
    for (TypeParameter param : tparams) {
      Type upperBound = checkStructure(param.getBound());
      if (!param.getInterfaceBounds().isEmpty()) {
        // can't use meet because it may involving subtyping on uninitialized variables
        upperBound = new IntersectionType(IterUtil.compose(upperBound, checkList(param.getInterfaceBounds())));
      }
      BoundedSymbol b = getTypeVariable(param).symbol();
      b.initializeUpperBound(upperBound);
      b.initializeLowerBound(TypeSystem.NULL);
    }
  }
  
  /**
   * Verify that the given type parameters (for which {@link #checkStructureForTypeParameters} has
   * already been invoked) are well-formed.
   */
  public void ensureWellFormedTypeParameters(TypeParameter[] tparams) {
    for (TypeParameter tparam : tparams) {
      if (!ts.isWellFormed(getTypeVariable(tparam))) {
        throw new ExecutionError("malformed.type", tparam);
      }
    }
  }
  
  private class TypeNameVisitor extends AbstractVisitor<Type> implements Lambda<TypeName, Type> {
  
    public Type value(TypeName t) { return t.acceptVisitor(this); }
        
    /**
     * Visits a BooleanTypeName
     * @return  The type of the TypeName
     */
    @Override public Type visit(BooleanTypeName node) { return setType(node, TypeSystem.BOOLEAN); }
    
    /**
     * Visits a ByteTypeName
     * @return  The type of the TypeName
     */
    @Override public Type visit(ByteTypeName node) { return setType(node, TypeSystem.BYTE); }
    
    /**
     * Visits a ShortTypeName
     * @return  The type of the TypeName
     */
    @Override public Type visit(ShortTypeName node) { return setType(node, TypeSystem.SHORT); }
    
    /**
     * Visits a CharTypeName
     * @return  The type of the TypeName
     */
    @Override public Type visit(CharTypeName node) { return setType(node, TypeSystem.CHAR); }
    
    /**
     * Visits a IntTypeName
     * @return  The type of the TypeName
     */
    @Override public Type visit(IntTypeName node) { return setType(node, TypeSystem.INT); }
    
    /**
     * Visits a LongTypeName
     * @return  The type of the TypeName
     */
    @Override public Type visit(LongTypeName node) { return setType(node, TypeSystem.LONG); }
    
    /**
     * Visits a FloatTypeName
     * @return  The type of the TypeName
     */
    @Override public Type visit(FloatTypeName node) { return setType(node, TypeSystem.FLOAT); }
    
    /**
     * Visits a DoubleTypeName
     * @return  The type of the TypeName
     */
    @Override public Type visit(DoubleTypeName node) { return setType(node, TypeSystem.DOUBLE); }
    
    /**
     * Visits a VoidTypeName
     * @return  The type of the TypeName
     */
    @Override public Type visit(VoidTypeName node) { return setType(node, TypeSystem.VOID); }
    
    /**
     * Visits a ReferenceTypeName
     * @return  The type of the TypeName
     */
    @Override public Type visit(ReferenceTypeName node) {
      Iterator<? extends IdentifierToken> ids = node.getIdentifiers().iterator();
      String name = "";
      Type t = null;
      
      boolean first = true;
      while (t == null && ids.hasNext()) {
        if (!first) { name += "."; }
        first = false;
        name += ids.next().image();
        
        try {
          DJClass c = context.getTopLevelClass(name, ts);
          if (c != null) { t = ts.makeClassType(c); }
          else {
            t = context.getTypeVariable(name, ts);
            if (t == null) {
              Type outer = context.typeContainingMemberClass(name, ts);
              if (outer != null) { t = ts.lookupClass(outer, name, IterUtil.<Type>empty()); }
            }
          }
        }
        catch (AmbiguousNameException e) { throw new ExecutionError("ambiguous.name", node); }
        catch (InvalidTypeArgumentException e) { throw new ExecutionError("type.argument.arity", node); }
        catch (UnmatchedLookupException e) {
          if (e.matches() == 0) { throw new ExecutionError("undefined.name.noinfo", node); }
          else { throw new ExecutionError("ambiguous.name", node); }
        }
      }
      while (ids.hasNext()) {
        try {
          ClassType memberType = ts.lookupClass(t, ids.next().image(), IterUtil.<Type>empty());
          new ExpressionChecker(context, opt).checkAccessibility(memberType.ofClass(), node);
          t = memberType;
        }
        catch (InvalidTypeArgumentException e) { throw new ExecutionError("type.argument.arity", node); }
        catch (UnmatchedLookupException e) {
          if (e.matches() == 0) { throw new ExecutionError("undefined.name.noinfo", node); }
          else { throw new ExecutionError("ambiguous.name", node); }
        }
      }
      
      if (t == null) { 
        setErrorStrings(node, node.getRepresentation());
        throw new ExecutionError("undefined.class", node);
      }
      return setType(node, t);
    }
    
    /**
     * Visits a GenericReferenceTypeName
     * @return  The type of the TypeName
     */
    @Override public Type visit(GenericReferenceTypeName node) {
      Iterator<? extends IdentifierToken> ids = node.getIdentifiers().iterator();
      Iterator<List<? extends TypeName>> allTargs = node.getTypeArguments().iterator();
      String name = "";
      Type t = null;
      
      boolean first = true;
      while (t == null && ids.hasNext()) {
        if (!first) { name += "."; }
        first = false;
        name += ids.next().image();
        List<? extends TypeName> targsNames = allTargs.next();
        Iterable<Type> targs = checkStructureForList(targsNames);
        
        try {
          DJClass c = context.getTopLevelClass(name, ts);
          t = (c == null) ? null : ts.makeClassType(c, targs);
          if (t == null) {
            Type outer = context.typeContainingMemberClass(name, ts);
            if (outer != null) { t = ts.lookupClass(outer, name, targs); }
          }
          if (t == null) { 
            if (!IterUtil.isEmpty(targs)) {
              setErrorStrings(node, name);
              throw new ExecutionError("undefined.class", node);
            }
            t = context.getTypeVariable(name, ts);
          }
        }
        catch (AmbiguousNameException e) { throw new ExecutionError("ambiguous.name", node); }
        catch (InvalidTypeArgumentException e) { throw new ExecutionError("type.argument.arity", node); }
        catch (UnmatchedLookupException e) {
          if (e.matches() == 0) { throw new ExecutionError("undefined.name.noinfo", node); }
          else { throw new ExecutionError("ambiguous.name", node); }
        }
      }
      
      while (ids.hasNext()) {
        try {
          Iterable<Type> targs = checkStructureForList(allTargs.next());
          ClassType memberType = ts.lookupClass(t, ids.next().image(), targs);
          new ExpressionChecker(context, opt).checkAccessibility(memberType.ofClass(), node);
          t = memberType;
        }
        catch (InvalidTypeArgumentException e) { throw new ExecutionError("type.argument", node); }
        catch (UnmatchedLookupException e) {
          if (e.matches() == 0) { throw new ExecutionError("undefined.name.noinfo", node); }
          else { throw new ExecutionError("ambiguous.name", node); }
        }
      }
      
      if (t == null) { 
        setErrorStrings(node, node.getRepresentation());
        throw new ExecutionError("undefined.class", node);
      }
      return setType(node, t);
    }
    
    /**
     * Visits a HookTypeName
     * @return  The type of the TypeName
     */
    @Override public Type visit(HookTypeName node) {
      Type upper = TypeSystem.OBJECT;
      if (node.getUpperBound().isSome()) {
        upper = checkStructure(node.getUpperBound().unwrap());
        if (!ts.isReference(upper)) {
          setErrorStrings(node, ts.userRepresentation(upper));
          throw new ExecutionError("wildcard.bound", node);
        }
      }
      
      Type lower = TypeSystem.NULL;
      if (node.getLowerBound().isSome()) {
        lower = checkStructure(node.getLowerBound().unwrap());
        if (!ts.isReference(lower)) {
          setErrorStrings(node, ts.userRepresentation(lower));
          throw new ExecutionError("wildcard.bound", node);
        }
      }

      return setType(node, new Wildcard(new BoundedSymbol(node, upper, lower)));
    }
    
    /**
     * Visits an ArrayTypeName
     * @return  The type of the TypeName
     */
    @Override public Type visit(ArrayTypeName node) {
      Type elementType = checkStructure(node.getElementType());
      Type arrayT = node.isVararg() ? new VarargArrayType(elementType) :
        new SimpleArrayType(elementType);
      return setType(node, arrayT);
    }
    
  }
  
}
