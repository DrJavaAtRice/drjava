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
  private TreeInterpreter astInterpreter;
  private TreeInterpreter strInterpreter;
  
  private ParserFactory parserFactory;
  private String testString;
  
  public GenericTreeTest(String name) {
    super(name);
  }
  
  public void setUp(){
    parserFactory = new JavaCCParserFactory();
    astInterpreter = new TreeInterpreter(null); // No ParserFactory needed to interpret an AST
    strInterpreter = new TreeInterpreter(parserFactory); // ParserFactory is needed to interpret a string
  }
  
  public List<Node> parse(String testString){
    List<Node> retval = parserFactory.createParser(new StringReader(testString),"UnitTest").parseStream();
    return retval;
  }
  
  public Object interpret(String testString) throws InterpreterException {
    return strInterpreter.interpret(new StringReader(testString), "Unit Test");
  }
  
  public void testParserBasics() {
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
    List<List<? extends Type>> alltargs = new LinkedList<List<? extends Type>>();
    alltargs.add(new LinkedList<ReferenceType>());
    alltargs.add(new LinkedList<ReferenceType>());
    alltargs.add(targs);
    
    Type genericListType = new GenericReferenceType(ids, alltargs);
    SimpleAllocation sa = new SimpleAllocation(genericListType, null); // Call parameters-less constructor of LinkedList
    
    Object result = astInterpreter.interpret(sa);
    assertEquals("java.util.LinkedList", result.getClass().getName());
  }
  
  public void testHandParsedGenericClass(){
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
    ConstructorDeclaration c = new ConstructorDeclaration(accessFlags, "C", cparams, new LinkedList<ReferenceType>(), null, cstmts, false);
    body.add(c);
    List<Node> mstmts = new LinkedList<Node>();
    ReturnStatement mstmt = new ReturnStatement(nName);
    mstmts.add(mstmt);
    List<FormalParameter> mparams = new LinkedList<FormalParameter>();
    List<ReferenceType> mexcepts = new LinkedList<ReferenceType>();
    MethodDeclaration m = new MethodDeclaration(accessFlags, t, "m", mparams, mexcepts, new BlockStatement(mstmts));
    body.add(m);
    GenericClassDeclaration cls = new GenericClassDeclaration(accessFlags, "C", null, null, body, typeParams);
    
    astInterpreter.interpret(cls); // set the context for the following method call
    
    
    
    IdentifierToken cId = new Identifier("C");
    List<IdentifierToken> ids = new LinkedList<IdentifierToken>();
    ids.add(cId);
    
    IdentifierToken iId = new Identifier("Integer");
    List<IdentifierToken> iIds = new LinkedList<IdentifierToken>();
    iIds.add(iId);
    
    List<ReferenceType> targs = new LinkedList<ReferenceType>();
    targs.add(new ReferenceType(iIds));
    List<List<? extends Type>> alltargs = new LinkedList<List<? extends Type>>();
    alltargs.add(new LinkedList<ReferenceType>());
    alltargs.add(new LinkedList<ReferenceType>());
    alltargs.add(targs);
    
    Type genericCType = new GenericReferenceType(ids, alltargs);
    List<Expression> mccargs = new LinkedList<Expression>(); // method call constructor args
    List<Expression> fiveExps = new LinkedList<Expression>();
    fiveExps.add(new IntegerLiteral("5"));
    mccargs.add(new SimpleAllocation(new ReferenceType(iIds), fiveExps));
    SimpleAllocation sa = new SimpleAllocation(genericCType, mccargs);
    MethodCall mc = new ObjectMethodCall(sa, "m", new LinkedList<Expression>(), "", 0, 0, 0, 0);
    
    Object result = astInterpreter.interpret(mc);
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
    Object result = astInterpreter.interpret(AST.get(0));
    
    testString = "new C<Integer>(new Integer(5)).m();\n";
    AST = parse(testString);
    result = astInterpreter.interpret(AST.get(0));
    
    assertEquals("java.lang.Integer", result.getClass().getName());
    assertEquals(new Integer(5), (Integer)result);
  }
  
  public void testAnotherGenericClassWithUpgradedParser(){
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
    Object result = astInterpreter.interpret(AST.get(0));
    result = astInterpreter.interpret(AST.get(1));
    result = astInterpreter.interpret(AST.get(2));
    result = astInterpreter.interpret(AST.get(3));
    assertEquals("java.lang.Integer",result.getClass().getName());
    assertEquals(new Integer(5), (Integer)result);
    
    result = astInterpreter.interpret(AST.get(4));
    result = astInterpreter.interpret(AST.get(5));
    assertEquals("java.lang.Double",result.getClass().getName());
    assertEquals(new Double(3.3), (Double)result);
  }
  
  
  
  
  
  
  public void testInterpreterBasics() {
    testString = "Integer x = new Integer(5); x;";
    assertEquals("Evaluation of Integer(5)", new Integer(5), interpret(testString));
    
    testString = "Boolean y = Boolean.FALSE; y;";
    assertEquals("Evaluation of Boolean.FALSE", Boolean.FALSE, interpret(testString));
    
    testString = "String z = \"FOO\" + \"BAR\"; z;";
    assertEquals("String concatenation", "FOOBAR", interpret(testString));
    
    testString = "int[] a = new int[]{1,2,3}; \"\"+a[0]+a[1]+a[2];";
    assertEquals("Anonymous array", "123", interpret(testString));
    
    testString = "int[][] b = new int[][]{{12, 0}, {1, -15}}; b[0][0] + b[1][1];";
    assertEquals("2D Anonymous array", new Integer(-3), interpret(testString));
    
    testString = "(Number) new Integer(12);";
    assertEquals("Successful cast test", new Integer(12), interpret(testString));
  }
  
  public void testMultiTypeNonGenericList(){
    testString =
      "import java.util.*;"+
      "List l = new LinkedList();"+
      "l.add(new Float(5.5));"+
      "l.add(new Integer(7));"+
      "String s = l.toString();"+
      "s;";
    
    assertEquals("s should be [5.5, 7]", "[5.5, 7]", interpret(testString));
    
    testString = "Float f = (Float)l.get(0); f;";
    assertEquals("f should be 5.5", new Float(5.5), interpret(testString));
    
    testString = "l.remove(0); Integer i = (Integer)l.get(0); i;";
    assertEquals("i should be 7", new Integer(7), interpret(testString));
  }
  
  public void testSingleTypeNonGenerifiedList(){
    testString =
      "import java.util.*;"+
      "List l = new LinkedList();"+
      "l.add(new Float(5.5));"+
      "l.add(new Float(7.3));"+  // note that this is a float as well
      "String s = l.toString();"+
      "s;";
    
    assertEquals("s should be [5.5, 7.3]", "[5.5, 7.3]", interpret(testString));
    
    testString = "Float f = (Float)l.get(0); f;";
    assertEquals("f should be 5.5", new Float(5.5), interpret(testString));
    
    testString = "l.remove(0); Float f2 = (Float)l.get(0); f2;";
    assertEquals("f2 should be 7.3", new Float(7.3), interpret(testString));
  }
  
  //Test with Generics.
  public void testGenericList(){
    testString =
      "import java.util.*;"+
      "List<Float> l = new LinkedList<Float>();"+
      "l.add(new Float(5.5));"+
      "l.add(new Float(7.3));"+
      "String s = l.toString();"+
      "s;";
    
    assertEquals("s should be [5.5, 7.3]", "[5.5, 7.3]", interpret(testString));
    
    testString = "Float f = l.get(0); f;";
    assertEquals("f should be 5.5", new Float(5.5), interpret(testString));
    
    testString = "l.remove(0); Float f2 = l.get(0); f2;";
    assertEquals("f2 should be 7.3", new Float(7.3), interpret(testString));
  }
  
  // More complex test with visitors.
  // The types for the AbstractShapeVisitor interface methods
  // have been weakened to Object because DynamicJava does not
  // support forward class references
  
  public void testVisitorsNonGeneric() {
    testString = stringHelper()+
      "AbstractShape a;"+
      "a = new Box(2,2,2);";
    //    List<Node> l = parse(testString);
    //    for(int ll = 0; ll < l.size(); ll++ )
    //      System.out.println("Non-generic["+ll+"]="+l.get(ll)+"\n");
    interpret(testString);
    testString = ""+
      "AbstractShapeVisitor v = new VolumeCalculator();\n";
    //    System.out.println("Non-generic="+parse(testString).get(0)+"\n");
    interpret(testString);
    testString = ""+
      "Integer result = (Integer)a.accept(v);\n"+
      "result;";
    assertEquals("Result should be 8", new Integer(8), interpret(testString));
    testString =
      "AbstractShape b;"+
      "b = new Sphere(3);"+
      "result = (Integer)b.accept(new VolumeCalculator());"+
      "result;";
    assertEquals("Result should be 81", new Integer(81), interpret(testString));
  }
  
  private String stringHelper(){
    return
      "interface AbstractShapeVisitor{"+
      "Object forBox(Object b);"+
      "Object forSphere(Object s);}"+
      
      "interface AbstractShape{"+
      "Object accept(AbstractShapeVisitor v);}"+
      
      "class Box implements AbstractShape{"+
      "private int _length;"+
      "private int _width;"+
      "private int _height;"+
      
      "Box(int l,int w,int h){"+
      "_length=l;"+
      "_width=w;"+
      "_height=h;}"+
      
      "int getLength(){"+
      "return _length;}"+
      
      "int getWidth(){"+
      "return _length;}"+
      
      "int getHeight(){"+
      "return _length;}"+
      
      "public Object accept(AbstractShapeVisitor v){return v.forBox(this);}}"+
      
      "class Sphere implements AbstractShape{"+
      "private int _radius;"+
      
      "Sphere(int r){ _radius=r; }"+
      
      "int getRadius(){ return _radius; }"+
      
      "public Object accept(AbstractShapeVisitor v){ return v.forSphere(this); }}"+
      
      "public class VolumeCalculator implements AbstractShapeVisitor{"+
      
      "public Object forBox(Object a){"+
      "Box b = (Box) a;" +
      "return new Integer(b.getLength()*b.getWidth()*b.getHeight());}"+
      
      "public Object forSphere(Object a){"+
      "Sphere s = (Sphere) a;" +
      "int rad = s.getRadius();"+
      "return new Integer(rad*rad*rad*3*1);}}";
  }
  
  public void testPolymorphicMethods(){
    testString = ""+
      "public class C{\n"+
      "  public <T> T m() { return new Float(6.7); }\n"+
      "}\n"+
      "Float f = new C().m(); f;";
    Object result = interpret(testString);
    assertEquals("Result should be 6.7", new Float(6.7), result);
    
    testString = ""+
      "public class D{\n"+
      "  public <T> T m(T param) { return param; }\n"+
      "}\n"+
      "Float ff = new Float(3.3);\n"+
      "Float fff = new D().m(ff); fff;";
    
    result = interpret(testString);
    assertEquals("Result should be 3.3", new Float(3.3), result);
  }
  
  // More complex test with *generic* visitors.
  // The types for the AbstractShapeVisitor interface methods
  // have been weakened to Object because DynamicJava does not
  // support forward class references
  
  public void testVisitorsGeneric() {
    testString = ""+
      "interface AbstractShapeVisitor<T>{\n"+
      "T forBox(Object b);\n"+
      "T forSphere(Object s);}\n"+
      
      "interface AbstractShape{"+
      "<T> T accept(AbstractShapeVisitor<T> v);}\n"+
      
      "class Box implements AbstractShape{\n"+
      "private int _length;\n"+
      "private int _width;\n"+
      "private int _height;\n"+
      
      "Box(int l,int w,int h){\n"+
      "_length=l;\n"+
      "_width=w;\n"+
      "_height=h;}\n"+
      
      "int getLength(){\n"+
      "return _length;}\n"+
      
      "int getWidth(){\n"+
      "return _length;}\n"+
      
      "int getHeight(){\n"+
      "return _length;}\n"+
      
      "public <T> T accept(AbstractShapeVisitor<T> v){return v.forBox(this);}}\n"+
      
      "class Sphere implements AbstractShape{\n"+
      "private int _radius;\n"+
      
      "Sphere(int r){ _radius=r; }\n"+
      
      "int getRadius(){ return _radius; }\n"+
      
      "public <T> T accept(AbstractShapeVisitor<T> v){ return v.forSphere(this); }}\n"+
      
      "public class VolumeCalculator implements AbstractShapeVisitor<Integer>{\n"+
      
      "public Object forBox(Object a){ return (Integer)forBox(a); }\n"+
      /**/// MANUALLY WRITTEN BRIDGE METHOD -- SHOULD BE AUTOMATICALLY GENERATED BY
      // DynamicJava, or taken care of by the underlying JVM??
      
      "public Integer forBox(Object a){\n"+
      //      "Box b = (Box) a;\n"+
      "Box b = a;\n"+
      "return new Integer(b.getLength()*b.getWidth()*b.getHeight());}\n"+
      
      "public Object forSphere(Object a){ return (Integer)forSphere(a); }\n"+
      /**/// MANUALLY WRITTEN BRIDGE METHOD -- SHOULD BE AUTOMATICALLY GENERATED BY
      // DynamicJava, or taken care of by the underlying JVM??
      
      "public Integer forSphere(Object a){\n"+
      //      "Sphere s = (Sphere) a;\n" +
      "Sphere s = a;\n" +
      "int rad = s.getRadius();\n"+
      "return new Integer(rad*rad*rad*3*1);}}\n";
    
    testString = testString +
      "AbstractShape a;\n"+
      "a = new Box(2,2,2);\n";
    //    List<Node> l = parse(testString);
    //    for(int ll = 0; ll < l.size(); ll++ )
    //      System.out.println("Generic["+ll+"]="+l.get(ll)+"\n");
    interpret(testString);
    testString = ""+
      "AbstractShapeVisitor<Integer> v = new VolumeCalculator(); v;\n";
    //    System.out.println("Generic="+parse(testString).get(0)+"\n");
    interpret(testString);
    testString = ""+
      "Integer result = a.accept(v);\n"+
      "result;\n";
    assertEquals("Result should be 8", new Integer(8), interpret(testString));
    testString =
      "AbstractShape b;\n"+
      "b = new Sphere(3);\n"+
      "result = b.accept(new VolumeCalculator());\n"+
      "result;\n";
    assertEquals("Result should be 81", new Integer(81), interpret(testString));
  }
  
  public void testMethodOver1oading(){
    // Note the name of this method. If u realize the problem u'd know why
    // the test below used to fail, if we use lower case variable names,
    // ... and a sloppy typer (who presses the key for 'one' instead of the
    // key for 'L')!
    //
    testString =
      "import java.util.LinkedList;\n"+
      "//class C<T>{}\n"+
      "class C{\n"+
      "String m(LinkedList<String> l){\n"+
      "return \"LLWithString\";}\n"+
      
      "String m(LinkedList<Integer> l){\n"+
      "return \"LLWithInteger\";}}\n"+
      "LinkedList<String> LS = new LinkedList<String>();"+
      " // Don't use 'ls' as variable name, because small l looks exactly "+
      " as 1 (one) on many PC and many fonts!\n"+
      "LS.add(\"SomeString\");\n"+
      "String str1 = new C().m(LS);\n"+
      "LinkedList<Integer> LI = new LinkedList<Integer>();\n"+
      "LI.add(new Integer(3));\n"+
      "String str2 = new C().m(LI);\n";
    
    try{
      interpret(testString);
      testString = "str1;";
      assertEquals("str1 should be CWithString", new String("CWithString"), interpret(testString));
      
      testString = "str2;";
      assertEquals("str2 should be CWithInteger", new String("CWithInteger"), interpret(testString));
      fail("In JDK 1.5 generic type info is erased, and not "+
           "available at runtime.\nLinkedList<String> and LinkedList<Integer> "+
           "are thus the same at runtime,\nand methods that take them as "+
           "parameter types are essentially the same (duplicate) methods.");
    } catch (ClassFormatError e){
    }
  }
  
  public void testBrackets(){
    
    // right shift
    assertEquals(new Integer(400 >> 5),interpret("400 >> 5;"));
    
    // unsigned right shift
    assertEquals(new Integer(400 >>> 5), interpret("400 >>> 5;"));
    
    
    // left shift
    assertEquals(new Integer(400 << 5),interpret("400 << 5;"));
    
    // less than
    assertEquals(new Boolean(5 < 4), interpret("5 < 4;"));
    
    // less than or equal to
    assertEquals(new Boolean(4 <= 4), interpret("4 <= 4;"));
    assertEquals(new Boolean(4 <= 5), interpret("4 <= 5;"));
    
    // greater than
    assertEquals(new Boolean(5 > 4), interpret("5 > 4;"));
    assertEquals(new Boolean(5 > 5), interpret("5 > 5;"));
    
    // greater than or equal to
    assertEquals(new Boolean(5 >= 4), interpret("5 >= 4;"));
    assertEquals(new Boolean(5 >= 5), interpret("5 >= 5;"));
    
    //_interpreter.interpret("(false) ? 2/0 : 1 ");
  }
  
  public void testStaticInnerParameterizedClass(){
    testString = 
      "public class A{\n"+
      "  public static class B<T>{\n"+
      "    public String toString(){return \"fooo\";}\n"+
      "  }\n"+
      "}\n"+
      
      "A.B<String> b = new A.B<String>(); b.toString();\n";
    assertEquals("fooo", interpret(testString));
  }
  
  public void testInnerParameterizedClass(){
    testString = 
      "public class A{\n"+
      "  public class B<T>{\n"+
      "    public String toString(){return \"fooo\";}\n"+
      "  }\n"+
      "}\n"+
      
      "A a = new A();\n"+
      "A.B<String> b = a.new B<String>(); b.toString();\n";
    assertEquals("fooo", interpret(testString));
  }
  
  // SHOULD BE FIXED TO PASS!
  public void testStaticInnerParameterizedClassInParameterizedClass(){
    testString = 
      "public class A<S>{\n"+
      "  public static class B<T>{\n"+
      "    public String toString(){return \"fooo\";}\n"+
      "  }\n"+
      "}\n"+
      "A<String>.B<String> b; b = new A<String>.B<String>(); b.toString();\n";
    assertEquals("fooo", interpret(testString));
  }
 
    // SHOULD BE FIXED TO PASS!
  public void testInnerParameterizedClassInParameterizedClass(){
    testString = 
      "public class A<S>{\n"+
      "  public class B<T>{\n"+
      "    public String toString(){return \"fooo\";}\n"+
      "  }\n"+
      "}\n"+
      "A<String> a = new A<String>();\n"+
      "A<String>.B<String> b = a.new B<String>(); b.toString();\n";
    assertEquals("fooo", interpret(testString));
  }
 
  
  // SHOULD BE FIXED TO PASS!
  
  public void testInnerClassInParameterizedClass(){
    testString = 
      "public class A<T>{\n"+
      "  public static class B{\n"+
      "    public String toString(){return \"fooo\";}\n"+
      "  }\n"+
      "}\n"+
      
      "A<String>.B b = new A<String>.B(); b.toString();\n";
    assertEquals("fooo", interpret(testString));
  }
  
  
  // wildcards
  public void testWildcards(){
    testString =
      "import java.util.LinkedList;\n"+
      "import java.util.List;\n"+
      "class A{\n"+
      "  LinkedList<String> l;\n"+
      "  LinkedList<LinkedList<String>> ll;\n"+
      "  A(){\n"+
      "    l = new LinkedList<String>();\n"+
      "    ll = new LinkedList<LinkedList<String>>();\n"+
      "    l.add(\"String1\");\n"+
      "    ll.add(l);\n"+
      "  }\n"+
      "  String someMethod(LinkedList<? extends List<String>> list){\n"+ //Note that when you have the LinkedList<? extends List>
      "    return ((LinkedList<String>)list.get(0)).get(0);\n"+ //instead of LinkedList<LinkedList<String>> you have to do a cast.
      "  }\n"+
      "}\n"+
      
      "A a = new A(); a.someMethod(a.ll);\n";
    assertEquals("String1", interpret(testString));
  }
  
  
  // wildcards /**/
  // Type checker should catch this error, as you are not allowed to call the add method on list, as list is read only  
  // Error: cannot find symbol
  // symbol  : method add(java.util.LinkedList<java.lang.String>)
  // location: class java.util.LinkedList<? extends java.util.List<java.lang.String>>
  public void testWildcards2(){
    testString =
      "import java.util.LinkedList;\n"+
      "import java.util.List;\n"+
      "class A{\n"+
      "  LinkedList<String> l;\n"+
      "  LinkedList<LinkedList<String>> ll;\n"+
      "  A(){\n"+
      "    l = new LinkedList<String>();\n"+
      "    ll = new LinkedList<LinkedList<String>>();\n"+
      "    l.add(\"String1\");\n"+
      "    ll.add(l);\n"+
      "  }\n"+
      "  String someMethod(LinkedList<? extends List<String>> list){\n"+ //Note that when you have the LinkedList<? extends List>
      "    LinkedList<String> list2 = new LinkedList<String>();\n"+
      "    list2.add(\"String2\");\n"+
      "    list.add(list2);\n"+
      "    return ((LinkedList<String>)list.get(0)).get(0);\n"+ //instead of LinkedList<LinkedList<String>> you have to do a cast.
      "  }\n"+
      "}\n"+
      
      "A a = new A(); a.someMethod(a.ll);\n";
    assertEquals("String1", interpret(testString));
  }
  
  // wildcards, ? alone (which means extends Object)
  public void testSingleWildcard(){
    testString =
      "import java.util.LinkedList;\n"+
      "import java.util.List;\n"+
      "class A{\n"+
      "  LinkedList<String> l;\n"+
      "  LinkedList<LinkedList<String>> ll;\n"+
      "  A(){\n"+
      "    l = new LinkedList<String>();\n"+
      "    ll = new LinkedList<LinkedList<String>>();\n"+
      "    l.add(\"String1\");\n"+
      "    ll.add(l);\n"+
      "  }\n"+
      "  String someMethod(LinkedList<?> list){\n"+ 
      "    return ((LinkedList<String>)list.get(0)).get(0);\n"+
      "  }\n"+
      "}\n"+
      
      "A a = new A(); a.someMethod(a.ll);\n";
    assertEquals("String1", interpret(testString));
  }  
  
  // Wildcards, testing super, the lower bounds
  public void testWildcardSuper(){
    testString =
      "import java.util.List;\n"+
      "import java.util.LinkedList;\n"+
      
      "class TestSuper {\n"+
      "  Integer n;\n"+
      "  TestSuper(List<? super Integer> li){\n"+
      "    Object o = li.get(0);\n"+
      "    if(o instanceof Integer){\n"+
      "      n =(Integer) o;\n"+
      "    }\n"+
      "  }\n"+
      "}\n"+
      "List<? super Number> ln = new LinkedList<Number>();\n"+
      "ln.add(new Integer(3));\n"+
      "TestSuper ts = new TestSuper(ln);\n ts.n;";
    
    assertEquals("Expect to be allowed to read from the list, but only as objects.",new Integer(3), interpret(testString));
  }
  
  
  //Wildcards
  public void testWildcardSuper2(){
    testString =
      "import java.util.List;\n"+
      "import java.util.LinkedList;\n"+
      
      "class TestSuper {\n"+
      "  Integer n;\n"+
      "  TestSuper(List<? super Integer> li){\n"+
      "    Object o = li.get(0);\n"+
      "    if(o instanceof Integer){\n"+
      "      n =(Integer) o;\n"+
      "    }\n"+
      "  }\n"+
      "}\n"+
      "List<? super Number> ln = new LinkedList<Number>();\n"+
      "ln.add(new Integer(3));\n"+
      "Integer i = ln.get(0);i;"; //should fail, as you can only read as objects when using super
    
    
    assertEquals("Expect to be allowed to read from the list, but only as objects.",new Integer(3), interpret(testString));
  }
  
  //test added as these differences break in the interactions pane of DrJava, most likely caused by one of DrJava's extensions.  
  public void testInnerClasses(){
    testString =
      
      "class C{\n"+
      "  C(){\n"+
      "  }\n"+
      
      "  class D{\n"+
      "  String getSomeStr(){return \"someString\";}\n"+
      "  }\n"+
      "}\n"+
      
      "class E{\n"+
      "  String getStr(){\n"+
      "    C c = new C();\n"+
      "    C.D d = c.new D();\n"+
      "    return d.getSomeStr();\n"+
      "  }\n"+
      "}\n"+
      "new E().getStr();\n";
    
    assertEquals("Expect to get the String \"someString\"","someString", interpret(testString));

   //Same code from the method getStr taken from class E.
    testString = 
      "C c = new C();\n"+
      "C.D d = c.new D();\n"+
      "d.getSomeStr();\n";
    assertEquals("Expect to get the String \"someString\"","someString", interpret(testString));
  }  
  
  
     //Testing Polymorph Constructors
    public void xtestInterpretPolyConstructors(){
      testString =
        "public class C {\n"+
        "  String str = \"\";\n"+
        "  public <String> C(String s){\n"+ //or <String[]>
        "      str = s;\n"+
        "    }\n"+
        "  }\n"+
        "  public String getStr(){\n"+
        "    return str;\n"+
        "  }\n"+
        "}\n"+
        "new C(\"Str1\",\"Str2\",\"Str3\").getStr();\n";
    
      assertEquals("Str1Str2Str3", interpret(testString));
    }
}