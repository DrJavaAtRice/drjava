package koala.dynamicjava.tree.tiger.generic;

import java.util.*;
import junit.framework.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.SourceInfo;

public class GenericTreeTest extends TestCase {
  private TreeInterpreter interpreter;
  
  public GenericTreeTest(String name) {
    super(name);
  }

  public void setUp(){
    interpreter = new TreeInterpreter(null); // ParserFactory ain't needed
  }
  
  public void testSimpleAllocation() {
    // new java.util.LinkedList<Integer>()
    //
    IdentifierToken javaId = new Identifier("java");
    IdentifierToken utilId = new Identifier("util");
    IdentifierToken listId = new Identifier("LinkedList");
    List<IdentifierToken> ids = new LinkedList<IdentifierToken>();
    ids.add(javaId); ids.add(utilId); ids.add(listId);
    
    IdentifierToken iId = new Identifier("Integer");
    List<IdentifierToken> iIds = new LinkedList<IdentifierToken>();
    iIds.add(iId);
    
    List<ReferenceType> targs = new LinkedList<ReferenceType>();
    targs.add(new ReferenceType(iIds));
    
    Type genericListType = new GenericReferenceType(ids, targs);
    SimpleAllocation sa = new SimpleAllocation(genericListType, null); // Call parameters-less constructor of LinkedList
    
    Object result = interpreter.interpret(sa);
    assertEquals("java.util.LinkedList", result.getClass().getName());
  }
  
  public void testGenericClass(){
    // public class C<T extends Number> {
    //   public T n;
    //   public C(T _n){ n = _n; }
    //   public T m(){ return n; }
    // }
    // 
    // new C<Integer>(new Integer(5)).m()
    //
    //   ==> Should return an Integer(5)
    //
    int accessFlags = java.lang.reflect.Modifier.PUBLIC;
    
    List<IdentifierToken> tIds = new LinkedList<IdentifierToken>();
    tIds.add(new Identifier("T"));
    List<IdentifierToken> numberIds = new LinkedList<IdentifierToken>();
    numberIds.add(new Identifier("Number"));  // "java.lang" is imported automatically by DynamicJava
    ReferenceType numberType = new ReferenceType(numberIds);
    TypeParameter t = new TypeParameter(new SourceInfo(), tIds, numberType); // T extends Number
    TypeParameter[] typeParams = new TypeParameter[1];
    typeParams[0] = t;
    
    List<Node> body = new LinkedList<Node>();
    FieldDeclaration n = new FieldDeclaration(accessFlags, t, "n", null);
    body.add(n);
    FormalParameter param = new FormalParameter(false, t, "_n");
    List<FormalParameter> cparams = new LinkedList<FormalParameter>();
    cparams.add(param);
    
    List<Node> cstmts = new LinkedList<Node>();
    List<IdentifierToken> nIds = new LinkedList<IdentifierToken>();
    nIds.add(new Identifier("n"));
    QualifiedName nName = new QualifiedName(nIds);
    List<IdentifierToken> _nIds = new LinkedList<IdentifierToken>();
    _nIds.add(new Identifier("_n"));
    AssignExpression stmt = new SimpleAssignExpression(nName, new QualifiedName(_nIds));
    cstmts.add(stmt);
    ConstructorDeclaration c = new ConstructorDeclaration(accessFlags, "C", cparams, new LinkedList<List<IdentifierToken>>(), null, cstmts);
    body.add(c);
    List<Node> mstmts = new LinkedList<Node>();
    ReturnStatement mstmt = new ReturnStatement(nName);
    mstmts.add(mstmt);
    List<FormalParameter> mparams = new LinkedList<FormalParameter>();
    List<List<IdentifierToken>> mexcepts = new LinkedList<List<IdentifierToken>>();
    MethodDeclaration m = new MethodDeclaration(accessFlags, t, "m", mparams, mexcepts, new BlockStatement(mstmts));
    body.add(m);
    GenericClassDeclaration cls = new GenericClassDeclaration(accessFlags, "C", null, null, body, typeParams);
    
    interpreter.interpret(cls); // set the context for the following method call
    
    
    
    IdentifierToken cId = new Identifier("C");
    List<IdentifierToken> ids = new LinkedList<IdentifierToken>();
    ids.add(cId);
    
    IdentifierToken iId = new Identifier("Integer");
    List<IdentifierToken> iIds = new LinkedList<IdentifierToken>();
    iIds.add(iId);
    
    List<ReferenceType> targs = new LinkedList<ReferenceType>();
    targs.add(new ReferenceType(iIds));
    
    Type genericCType = new GenericReferenceType(ids, targs);
    List<Expression> mccargs = new LinkedList<Expression>(); // method call constructor args
    List<Expression> fiveExps = new LinkedList<Expression>();
    fiveExps.add(new IntegerLiteral("5"));
    mccargs.add(new SimpleAllocation(new ReferenceType(iIds), fiveExps));
    SimpleAllocation sa = new SimpleAllocation(genericCType, mccargs);
    MethodCall mc = new ObjectMethodCall(sa, "m", null, "", 0, 0, 0, 0);
    
    Object result = interpreter.interpret(mc);
    assertEquals("java.lang.Integer",result.getClass().getName());
    assertEquals(new Integer(5), (Integer)result);
  }
}