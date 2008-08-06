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
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.Lambda2;
import edu.rice.cs.plt.lambda.Thunk;

import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.TypeSystem.*;
import edu.rice.cs.dynamicjava.symbol.type.*;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.tiger.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.interpreter.error.ExecutionError;
import koala.dynamicjava.interpreter.TypeUtil;

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
  private final Options opt;
  private final TypeNameVisitor visitor; // singleton per outer instance

  public TypeNameChecker(TypeContext ctx, Options options) {
    context = ctx;
    ts = options.typeSystem();
    opt = options;
    visitor = new TypeNameVisitor();
  }
  
  public Type check(TypeName t) {
    return t.acceptVisitor(visitor);
  }
  
  public Iterable<Type> checkList(Iterable<? extends TypeName> l) {
    return IterUtil.mapSnapshot(l, visitor);
  }
  
  public void setTypeParameterBounds(TypeParameter[] tparams) {
    TypeNameVisitor v = new TypeNameVisitor();
    for (TypeParameter param : tparams) {
      Type firstBound = param.getBound().acceptVisitor(v);
      Iterable<Type> restBounds = checkList(param.getInterfaceBounds());
      BoundedSymbol bounds = getTypeVariable(param).symbol();
      if (IterUtil.isEmpty(restBounds)) { bounds.initializeUpperBound(firstBound); }
      else {
        bounds.initializeUpperBound(new IntersectionType(IterUtil.compose(firstBound, restBounds)));
      }
      bounds.initializeLowerBound(TypeSystem.NULL);
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
        catch (InvalidTargetException e) { throw new RuntimeException("context produced bad type"); }
        catch (InvalidTypeArgumentException e) { throw new ExecutionError("type.argument.arity", node); }
        catch (UnmatchedLookupException e) {
          if (e.matches() == 0) { throw new ExecutionError("undefined.name.noinfo", node); }
          else { throw new ExecutionError("ambiguous.name", node); }
        }
      }
      
      while (ids.hasNext()) {
        try { t = ts.lookupClass(t, ids.next().image(), IterUtil.<Type>empty()); }
        catch (InvalidTargetException e) { throw new RuntimeException("lookup produced bad type"); }
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
        Iterable<Type> targs = checkList(targsNames);
        
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
        catch (InvalidTargetException e) { throw new RuntimeException("context produced bad type"); }
        catch (InvalidTypeArgumentException e) { throw new ExecutionError("type.argument", node); }
        catch (UnmatchedLookupException e) {
          if (e.matches() == 0) { throw new ExecutionError("undefined.name.noinfo", node); }
          else { throw new ExecutionError("ambiguous.name", node); }
        }
      }
      
      while (ids.hasNext()) {
        try {
          Iterable<Type> targs = checkList(allTargs.next());
          t = ts.lookupClass(t, ids.next().image(), targs);
        }
        catch (InvalidTargetException e) { throw new RuntimeException("lookup produced bad type"); }
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
        upper = check(node.getUpperBound().unwrap());
        if (!ts.isReference(upper)) {
          setErrorStrings(node, ts.userRepresentation(upper));
          throw new ExecutionError("wildcard.bound", node);
        }
      }
      
      Type lower = TypeSystem.NULL;
      if (node.getLowerBound().isSome()) {
        lower = check(node.getLowerBound().unwrap());
        if (!ts.isReference(lower)) {
          setErrorStrings(node, ts.userRepresentation(lower));
          throw new ExecutionError("wildcard.bound", node);
        }
      }

      if (!ts.isSubtype(lower, upper)) {
        setErrorStrings(node, ts.userRepresentation(upper), ts.userRepresentation(lower));
        throw new ExecutionError("wildcard.bounds", node);
      }
      
      return setType(node, new Wildcard(new BoundedSymbol(node, upper, lower)));
    }
    
    /**
     * Visits an ArrayTypeName
     * @return  The type of the TypeName
     */
    @Override public Type visit(ArrayTypeName node) {
      Type elementType = check(node.getElementType());
      Type arrayT = node.isVararg() ? new VarargArrayType(elementType) :
        new SimpleArrayType(elementType);
      return setType(node, arrayT);
    }
    
  }
  
}
