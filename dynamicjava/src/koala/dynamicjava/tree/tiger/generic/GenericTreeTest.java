package koala.dynamicjava.tree.tiger.generic;

import java.util.*;
import junit.framework.*;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.SourceInfo;

import java.io.StringReader;
import java.util.List;
import koala.dynamicjava.parser.wrapper.ParserFactory;
import koala.dynamicjava.parser.wrapper.JavaCCParserFactory;


public class GenericTreeTest extends TestCase {
  private TreeInterpreter interpreter;

  private ParserFactory parserFactory;
  private String testString;

  public GenericTreeTest(String name) {
    super(name);
  }

  public void setUp(){
    parserFactory = new JavaCCParserFactory();
    interpreter = new TreeInterpreter(null); // No ParserFactory needed to interpret an AST
  }

  public List<Node> parse(String testString){
    List<Node> retval = parserFactory.createParser(new StringReader(testString),"UnitTest").parseStream();
    return retval;
  }

  public void testBasics() {
    testString = "1;";
    List<Node> ast = parse(testString);
    IntegerLiteral il = (IntegerLiteral)ast.get(0);
    assertEquals("1", il.getRepresentation());

    testString = "Integer x = new Integer(5);";
    ast = parse(testString);
    VariableDeclaration vd = (VariableDeclaration)ast.get(0);
    assertEquals("x", vd.getName());
    assertFalse(vd.isFinal());
    ReferenceType t = (ReferenceType)vd.getType();
    SimpleAllocation init = (SimpleAllocation)vd.getInitializer();
    assertEquals("Integer", t.getRepresentation());
    ReferenceType initt = (ReferenceType)init.getCreationType();
    assertEquals("Integer", initt.getRepresentation());
    IntegerLiteral initil = (IntegerLiteral)(init.getArguments().get(0));
    assertEquals("5", initil.getRepresentation());



/*
    testString = "Boolean y = Boolean.FALSE;";
    assertEquals("Parse of Boolean.FALSE", Boolean.FALSE, parse(testString));

    testString = "String z = \"FOO\" + \"BAR\";";
    assertEquals("Parse of string concatenation", "FOOBAR", parse(testString));

    testString = "int[] a = new int[]{1,2,3}; \"\"+a[0]+a[1]+a[2];";
    assertEquals("Parse of anonymous array", "123", parse(testString));

    testString = "int[][] b = new int[][]{{12, 0}, {1, -15}}; b[0][0] + b[1][1];";
    assertEquals("Parse of 2D Anonymous array", -3, parse(testString));

    testString = "(Number) new Integer(12);";
    assertEquals("Parse of cast", new Integer(12), parse(testString));
*/
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

  public void testTypeParameterGetName(){
    ReferenceType r = new ReferenceType("Integer");
    TypeParameter t = new TypeParameter(new SourceInfo(), "T", r);
    assertEquals("Integer", t.getRepresentation());
    assertEquals("T", t.getName());
  }

  public void testGenericClassWithUpgradedParser(){
    // public class C<T extends Number> {
    //   public T n;
    //   public C(T _n){ n = _n; }
    //   public T m(){ return n; }
    // }
    //
    // and then
    //
    // new C<Integer>(new Integer(5)).m()
    //
    //   ==> Should return an Integer(5)
    //

    testString = ""+
      "public class C<T extends Number> {\n"+
      "  public T n;\n"+
      "  public C(T _n){ n = _n; }\n"+
      "  public T m(){ return n; }\n"+
      "}\n";
    
    List<Node> AST = parse(testString);
    Object result = interpreter.interpret(AST.get(0));

    testString = "new C<Integer>(new Integer(5)).m();\n";
    AST = parse(testString);
    result = interpreter.interpret(AST.get(0));
    
    assertEquals("java.lang.Integer",result.getClass().getName());
    assertEquals(new Integer(5), (Integer)result);
  }

  public void testGenericClassWithUpgradedParser2(){
    // public class C<T extends Number, A extends T> { // treated by Java as 'A extends Number'?!
    //   public T m;
    //   public A n;
    //   public C(T _m, A _n){ m = _m; n = _n; }
    //   public T m(){ return m; }
    //   public A n(){ return n; }
    // }
    //
    // C c = new C<Integer, Double>(new Integer(5), new Double(3.3)); 
    // /* Type checker (later) should complain that Double does NOT extend Integer */
    //
    // Integer i = c.m(); i;
    //
    // Double d = c.n(); d;
    //
    //   ==> i should equals() Integer(5), and
    //   ==> d should equals() Float(3.3)
    //

    testString = ""+
      "public class C<T extends Number, A extends T> { // treated by Java as 'A extends Number'?!\n"+
      "  public T m;\n"+
      "  public A n;\n"+
      "  public C(T _m, A _n){ m = _m; n = _n; }\n"+
      "  public T m(){ return m; }\n"+
      "  public A n(){ return n; }\n"+
      "}\n"+
      "\n"+
      "C c = new C<Integer, Double>(new Integer(5), new Double(3.3));\n"+
      "/* Type checker (later) should complain that Double does NOT extend Integer */\n"+
      "\n"+
      "Integer i = c.m(); i;\n"+
      "\n"+
      "Double d = c.n(); d;\n";
                                  
    
    List<Node> AST = parse(testString);
    Object result = interpreter.interpret(AST.get(0));
    result = interpreter.interpret(AST.get(1));
    result = interpreter.interpret(AST.get(2));
    result = interpreter.interpret(AST.get(3));
    assertEquals("java.lang.Integer",result.getClass().getName());
    assertEquals(new Integer(5), (Integer)result);
    
    result = interpreter.interpret(AST.get(4));
    result = interpreter.interpret(AST.get(5));
    assertEquals("java.lang.Double",result.getClass().getName());
    assertEquals(new Double(3.3), (Double)result);    
  }
}