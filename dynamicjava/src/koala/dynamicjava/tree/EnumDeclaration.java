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

package koala.dynamicjava.tree;

import java.util.*;

import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents an enum declaration
 *
 * @author  Moez, Shankar
 */

public class EnumDeclaration extends ClassDeclaration {
  /**
   * Creates a new enum declaration
   * @param flags the access flags
   * @param name  the name of the enum to declare
   * @param impl  the list of implemented interfaces (a list of list of
   *              Token). Can be null.
   * @param body  the list of members declarations
   */
  public EnumDeclaration(int flags, String name, List<? extends ReferenceType> impl, EnumBody body) {
    this(flags, name, impl, body, null, 0, 0, 0, 0);
  }

//  /** Flag is set if symbol has a synthetic attribute.
//   */
//  public static final int SYNTHETIC    = 1<<16;
//
//  /** Flag is set if symbol is deprecated.
//   */
//  public static final int DEPRECATED   = 1<<17;
//
//  /** Flag is set for a variable symbol if the variable's definit
//   *  has an initializer part.
//   */
//  public static final int HASINIT          = 1<<18;
//
//  /** An enumeration type or an enumeration constant.
//   */
//  public static final int ENUM             = 1<<19;

 /**
   * Creates a new enum declaration
   * @param flags the access flags
   * @param name  the name of the enum to declare
   * @param impl  the list of implemented interfaces (a list of list of
   *              Token). Can be null.
   * @param body  the list of members declarations
   * @param fn    the filename
   * @param bl    the begin line
   * @param bc    the begin column
   * @param el    the end line
   * @param ec    the end column
   */
  public EnumDeclaration(int flags, String name, List<? extends ReferenceType> impl, EnumBody body,
                          String fn, int bl, int bc, int el, int ec) {
    super(flags, /**/// 1<<19 // | 0x4000, // | java.lang.reflect.Modifier.ENUM   NON-WORK! TigerUtilities.isEnum() is not used thus in AbstractTypeChecker
          name, new ReferenceType("java.lang.Enum"), impl,
      AddValues(name,
        HandleConstructors(name,
          makeEnumBodyDeclarationsFromEnumConsts(name, body)),
        body.getConstants()),
      fn, bl, bc, el, ec);

    // Do all Enum checks here? /**/
  }

  static List<Node> AddValues(String enumTypeName, List<Node> body, List<EnumConstant> consts){
    String[] consts_names = new String[consts.size()];
    for(int i = 0; i < consts_names.length; i++)
      consts_names[i] = consts.get(i).getName();

    List<Node> newbody = body;

    int accessFlags  = java.lang.reflect.Modifier.PRIVATE | java.lang.reflect.Modifier.STATIC | java.lang.reflect.Modifier.FINAL;

    ReferenceType enumType = new ReferenceType(enumTypeName);
    Type valuesType = new ArrayType(enumType, 1);
    List<Expression> sizes = new LinkedList<Expression>();
    sizes.add(new IntegerLiteral(String.valueOf(consts_names.length)));
    List<Expression> cells = new LinkedList<Expression>();
    for( int i = 0; i < consts_names.length; i++ )
      cells.add(new StaticFieldAccess(enumType, consts_names[i]));
    ArrayAllocation allocExpr = new ArrayAllocation(enumType, new ArrayAllocation.TypeDescriptor(sizes, 1, new ArrayInitializer(cells), 0, 0));
    newbody.add(new FieldDeclaration(accessFlags, valuesType, "$VALUES", allocExpr));

    accessFlags  = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.STATIC | java.lang.reflect.Modifier.FINAL;
    List<FormalParameter> vparams = new LinkedList<FormalParameter>();
    ///*for testing jlugo code*/vparams.add(new FormalParameter(false, new ReferenceType("String"), "s"));
    List<Node> stmts = new LinkedList<Node>();
    stmts.add(new ReturnStatement(new CastExpression(enumType, new ObjectMethodCall(new StaticFieldAccess(enumType, "$VALUES"), "clone", null))));
    newbody.add(new MethodDeclaration(accessFlags, valuesType, "values", vparams, new LinkedList<ReferenceType>(), new BlockStatement(stmts)));

    List<FormalParameter> voparams = new LinkedList<FormalParameter>();
    voparams.add(new FormalParameter(false, new ReferenceType("String"), "s"));
    accessFlags  = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.STATIC;
    
    //  for( int i = 0; i < $VALUES.length; i++ )
    //    if($VALUES[i].name().equals(s))
    //      return $VALUES[i];
    //  throw new IllegalArgumentException(s);
    List<Node> stmtsOf = new LinkedList<Node>();
    List<Node> init = new LinkedList<Node>();
    init.add(new VariableDeclaration(false, new IntType(), "i", new IntegerLiteral("0")));
    List<IdentifierToken> iIds = new LinkedList<IdentifierToken>();
    iIds.add(new Identifier("i"));
    QualifiedName iId = new QualifiedName(iIds);
    Expression cond = new LessExpression(iId, new ObjectFieldAccess(new StaticFieldAccess(enumType, "$VALUES"), "length"));
    List<Node> updt = new LinkedList<Node>();
    updt.add(new PostIncrement(iId));
    ArrayAccess arrCell = new ArrayAccess(new StaticFieldAccess(enumType, "$VALUES"), iId);
    List<Expression> args = new LinkedList<Expression>();
    List<IdentifierToken> sIds = new LinkedList<IdentifierToken>();
    sIds.add(new Identifier("s"));
    QualifiedName sId = new QualifiedName(sIds);
    args.add(new QualifiedName(sIds));
    IfThenStatement bodyOf = new IfThenStatement(new ObjectMethodCall(new ObjectMethodCall(arrCell, "name", null), "equals", args), new ReturnStatement(arrCell));
    stmtsOf.add(new ForStatement(init, cond, updt, bodyOf));
    stmtsOf.add(new ThrowStatement(new SimpleAllocation(new ReferenceType("IllegalArgumentException"), args)));
    newbody.add(new MethodDeclaration(accessFlags, enumType, "valueOf", voparams, new LinkedList<ReferenceType>(), new BlockStatement(stmtsOf)));

    return newbody;
  }

  static List<Node> HandleConstructors(String name, List<Node> body){
    Iterator<Node> it = body.listIterator();

    List<IdentifierToken> idnt1  = new LinkedList<IdentifierToken>();
    idnt1.add(new Identifier("$1"));
    List<IdentifierToken> idnt2  = new LinkedList<IdentifierToken>();
    idnt2.add(new Identifier("$2"));

    List<FormalParameter> addToConsDeclaration = new LinkedList<FormalParameter>();
    addToConsDeclaration.add(new FormalParameter(false, new ReferenceType("String"), "$1"));
    addToConsDeclaration.add(new FormalParameter(false, new IntType(),               "$2"));

    List<Expression> args = new LinkedList<Expression>();
    args.add(new QualifiedName(idnt1));
    args.add(new QualifiedName(idnt2));

    List<FormalParameter> consParams;
    boolean noConstructor = true;

    while(it.hasNext()) {
      Node current = it.next();
      if (current instanceof ConstructorDeclaration) {
        noConstructor = false;

        consParams = ((ConstructorDeclaration)current).getParameters();
        List<FormalParameter> newConsParam = new LinkedList<FormalParameter>();

        newConsParam.addAll(addToConsDeclaration);
        newConsParam.addAll(consParams);

        ((ConstructorDeclaration)current).setParameters(newConsParam);

        ((ConstructorDeclaration)current).setConstructorInvocation(new ConstructorInvocation(null, args, true));
      }
    }

    if (noConstructor) {
      body.add(new ConstructorDeclaration(java.lang.reflect.Modifier.PRIVATE, name, addToConsDeclaration,
                                          new LinkedList<ReferenceType>(),
                                          new ConstructorInvocation(null, args, true),
                                          new LinkedList<Node>()));
    }
    return body;
  }

  public static class EnumConstant {
    String name;
    List<Expression> args;
    List<Node> classBody;

    public EnumConstant(String _name, List<Expression> _args, List<Node> _classBody) {
      name = _name;
      args = _args;
      classBody = _classBody;
    }

    String           getName() {return name;}
    List<Expression> getArguments() {return args;}
    List<Node>        getClassBody() {return classBody;}
  }

  public static class EnumBody {
    private List<EnumConstant> consts;
    private List<Node> decls;

    public EnumBody(List<EnumConstant> c, List<Node> d){
      consts = c;
      decls = d;
    }

    List<EnumConstant> getConstants() {
      return consts;
    }

    List<Node> getDeclarations(){
      return decls;
    }
  }

  static List<Node> makeEnumBodyDeclarationsFromEnumConsts(String enumTypeName, EnumBody body) {
    List<EnumConstant> consts = body.getConstants();
    List<Node> decls = body.getDeclarations();

    int accessFlags  = java.lang.reflect.Modifier.PUBLIC;
        accessFlags |= java.lang.reflect.Modifier.STATIC;
        accessFlags |= java.lang.reflect.Modifier.FINAL;
        accessFlags |= 0x4000; // java.lang.reflect.Modifier.ENUM; /**/ or ACC_ENUM

    ReferenceType enumType = new ReferenceType(enumTypeName);

    Allocation allocExpr = null;

    Iterator<EnumConstant> it = consts.listIterator();
    int i = 0;
    while(it.hasNext()){
      List<Expression> args = new LinkedList<Expression>();
      EnumConstant ec = it.next();

      args.add(new StringLiteral("\""+ec.getName()+"\""));
      args.add(new IntegerLiteral(String.valueOf(i++)));

      if(ec.getArguments() != null){
        args.addAll(ec.getArguments());
      }

      if (ec.getClassBody() != null){
        allocExpr = new ClassAllocation(enumType, args, ec.getClassBody());
      }
      else {
        allocExpr = new SimpleAllocation(enumType, args);
      }

      decls.add(new FieldDeclaration(accessFlags, enumType, ec.getName(), allocExpr));
    }
    return decls;
  }
}
