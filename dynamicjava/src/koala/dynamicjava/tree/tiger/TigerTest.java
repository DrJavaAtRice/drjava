//package koala.dynamicjava.tree.tiger;
//
//import java.util.*;
//import junit.framework.*;
//
//import koala.dynamicjava.tree.*;
//import koala.dynamicjava.interpreter.*;
//import koala.dynamicjava.util.*;
//import koala.dynamicjava.SourceInfo;
//import koala.dynamicjava.interpreter.error.ThrownException;
//
//import java.io.StringReader;
//import java.util.List;
//import koala.dynamicjava.parser.wrapper.ParserFactory;
//import koala.dynamicjava.parser.wrapper.JavaCCParserFactory;
//
//
//public class TigerTest extends DynamicJavaTestCase {
//  private TreeInterpreter astInterpreter;
//  private TreeInterpreter strInterpreter;
//
//  private ParserFactory parserFactory;
//  private String testString;
//
//  public TigerTest(String name) {
//    super(name);
//  }
//
//  public void setUp(){
//    setTigerEnabled(true);
//    parserFactory = new JavaCCParserFactory();
//    astInterpreter = new TreeInterpreter(null); // No ParserFactory needed to interpret an AST
//    strInterpreter = new TreeInterpreter(parserFactory); // ParserFactory is needed to interpret a string
//  }
//  
//  public void tearDown(){
//   TigerUtilities.resetVersion(); 
//  }
//
//  public List<Node> parse(String testString){
//    List<Node> retval = parserFactory.createParser(new StringReader(testString),"UnitTest").parseStream();
//    return retval;
//  }
//
//  public Object interpret(String testString) throws InterpreterException {
//    return strInterpreter.interpret(new StringReader(testString), "Unit Test");
////    List<Node> exps = strInterpreter.buildStatementList(new java.io.StringReader(testString), "Unit Test");
////    return astInterpreter.interpret(exps);
//  }
//
//  // Interpreting static import.
//  public void testStaticImport(){
//    //STATIC FIELD
//    testString =
//      "import static java.lang.Integer.MAX_VALUE;\n"+
//      "class A{\n"+
//      "  int m(){return MAX_VALUE;}\n"+
//      "}\n"+
//      "A a = new A(); a.m();\n";
//
//    assertEquals(new Integer(java.lang.Integer.MAX_VALUE), interpret(testString));
//
//    //STATIC METHOD
//    testString =
//      "import static java.lang.Math.abs;\n"+
//      "class B{\n"+
//      "   int m(){return abs(-2);}\n"+
//      "}\n"+
//      "B b = new B(); b.m();\n";
//    assertEquals(new Integer(2), interpret(testString));
//
//  }
//
//  public void testStaticImportOfStaticInnerClass(){
//    testString =
//      "package P;\n"+
//      "public class A { \n"+
//      "  public static class B {\n"+
//      "    public static int m(){ return 0; }\n"+
//      "  }\n"+
//      "}\n"+
//      "package Q;\n"+
//      "import static P.A.B;\n"+
//      "B.m();\n";
//    assertEquals(0,interpret(testString));
//
//    testString =
//      "package R;\n"+
//      "public class C { \n"+
//      "  public static class D {\n"+
//      "    public static int m(){ return 0; }\n"+
//      "  }\n"+
//      "}\n"+
//      "package S;\n"+
//      "import static R.C.*;\n"+
//      "D.m();\n";
//    assertEquals(0,interpret(testString));
//
//    //Tests that a non-static inner class cannot be imported
//    testString =
//      "package T;\n"+
//      "public class U {\n"+
//      "  public class V { }\n"+
//      "}\n"+
//      "package W;\n"+
//      "import static T.U.*;\n"+
//      "V v = new V();";
//    try {
//      interpret(testString);
//      fail("Non static member should not be imported");
//    }
//    catch(InterpreterException e) {
//      //Expected to fail
//    }
//
//    //Tests that a non-static inner class cannot be imported
//    testString =
//      "package X;\n"+
//      "public class Y {\n"+
//      "  public class Z { \n"+
//      "    public static int m() { return 5; }\n"+
//      "  }\n"+
//      "}\n"+
//      "package AA;\n"+
//      "import static X.Y.Z;\n"+
//      "Z.m()";
//    try {
//      assertEquals(5,interpret(testString));
//      fail("Non static member should not be imported");
//    }
//    catch(InterpreterException e) {
//      //Expected to fail
//    }
//
//  }
//  /**
//   * Testing various forms of static importation of methods
//   */
//  public void testStaticImportOfMethods(){
//
//    //no parameters
//    testString =
//      "package Pack;\n"+
//      "public class Aclass { \n"+
//      "  public static int m() { return 0; }\n"+
//      "}\n"+
//      "package Q;\n"+
//      "import static Pack.Aclass.m;\n"+
//      "m();\n";
//    assertEquals(0,interpret(testString));
//
//    testString =
//      "package R;\n"+
//      "public class C { \n"+
//      "  public static int m2(){ return 0; }\n"+
//      "}\n"+
//      "package S;\n"+
//      "import static R.C.*;\n"+
//      "m2();\n";
//    assertEquals(0,interpret(testString));
//
//    //With a parameter
//
//    testString =
//      "package T;\n"+
//      "public class D { \n"+
//      "  public static String m3(String s) { return s; }\n"+
//      "}\n"+
//      "package U;\n"+
//      "import static T.D.m3;\n"+
//      "m3(\"5\");\n";
//    assertEquals("5",interpret(testString));
//
//    testString =
//      "package V;\n"+
//      "public class E { \n"+
//      "  public static String m4(String s, String s2) { return s; }\n"+
//      "}\n"+
//      "package W;\n"+
//      "import static V.E.*;\n"+
//      "m4(\"5\",\"6\");\n";
//    assertEquals("5",interpret(testString));
//
//
//    testString =
//      "package AA;\n"+
//      "public class BB {"+
//      "  public static int m5(int i) { return i; }\n"+
//      "}\n"+
//      "package CC;\n"+
//      "import static AA.BB.m5;"+
//      "m5(1+2+3+4+5);\n";
//    assertEquals(15,interpret(testString));
//
//
//    //With multiple parameters of different types
//
//    testString =
//      "package X;\n"+
//      "public class F { \n"+
//      "  public static String m6(String s, Class c) { return s; }\n"+
//      "}\n"+
//      "package Y;\n"+
//      "import static X.F.*;\n"+
//      "m6(\"5\",Integer.class);\n";
//    assertEquals("5",interpret(testString));
//
//
//    //With unequal parameters
//    testString =
//      "package DD;\n"+
//      "public class EE { \n"+
//      "  public static int m7(int i) { return i; }\n"+
//      "}\n"+
//      "package FF;\n"+
//      "import static DD.EE.*;\n"+
//      "m7(\"5\");\n";
//    try {
//      assertEquals(5,interpret(testString));
//      fail("Method parameter types String and int are not equal!");
//    }
//    catch(InterpreterException e) {
//      //Expected to fail
//    }
//
//    //Test for context shift
//    //In other words, because we run a TypeChecker on the arguments, make sure that it doesn't actually add
//    // the element 5 to the list when it is checking its type for the name visitor.
//    testString =
//      "package GG;\n"+
//      "public class HH {\n"+
//      "  public static int m8(int i) { return i; } \n" +
//      "}\n"+
//      "package II;\n"+
//      "import static GG.HH.m8;\n"+
//      "int i = 0;"+
//      "m8(i);";
//    assertEquals(0,interpret(testString));
//    testString =
//      "m8(i++);";
//    assertEquals(0,interpret(testString));
//    testString =
//      "m8(++i);";
//    assertEquals(2,interpret(testString));
//
//
//
//    testString =
//      "package KK;\n"+
//      "import static java.lang.Math.*;\n"+
//      "abs(-4);";
//    assertEquals(4,interpret(testString));
//    testString =
//      "sqrt(4);";
//    assertEquals(2.0,interpret(testString));
//    testString =
//      "sqrt(abs(-4));";
//    assertEquals(2.0,interpret(testString));
//
//    testString =
//      "package LL;\n"+
//      "import static java.lang.Double.*;\n"+
//      "public class MM {\n"+
//      "  public static double parseDouble(String s) {\n"+
//      "    return 0.0; \n"+
//      "  }\n"+
//      "  public static double m() {\n"+
//      "    return parseDouble(\"1.5\");\n"+
//      "  }\n"+
//      "}\n"+
//      "parseDouble(\"1.5\");\n";
//    assertEquals(1.5,interpret(testString));
//    testString = "MM.m();";
//    assertEquals("Member of class should take precedence over staticly imported member",0.0,interpret(testString));
//
//
//
//    testString =
//      "package NN;\n"+
//      "import static java.lang.Math.abs;\n"+
//      "public abstract class OO {\n"+
//      "  public int abs(int i) { return i; }\n"+
//      "}\n"+
//      "public class PP extends OO {\n"+
//      "  public static int m() {\n"+
//      "    return abs(-2);\n"+
//      "  }\n"+
//      "}\n"+
//      "PP.m();";
//    try {
//      interpret(testString);
//      fail("Static method cannot reference non-static members of super class");
//    } catch(InterpreterException e) {
//      //Expected to fail
//    }
//
//
//    testString =
//      "package QQ;\n"+
//      "import static java.lang.Math.abs;\n"+
//      "public abstract class RR {\n"+
//      "  public static int abs(int i) { return i; }\n"+
//      "}\n"+
//      "public class SS extends RR {\n"+
//      "  public static int m() {\n"+
//      "    return abs(-2);\n"+
//      "  }\n"+
//      "}\n"+
//      "SS.m();";
//    assertEquals("Super class method should take precedence over staticly imported member",-2,interpret(testString));
//
//
//    //Tests that a non-static method cannot be imported
//    testString =
//      "package TT;\n"+
//      "public class UU {\n"+
//      "  public int m1() { return 5;}\n"+
//      "}\n"+
//      "package VV;\n"+
//      "import static TT.UU.*;\n"+
//      "public class WW {\n"+
//      "  public int m2() { return m1(); } \n"+
//      "}\n"+
//      "WW ww = new WW(); ww.m2();";
//    try {
//      assertEquals(5,interpret(testString));
//      fail("Non static member should not be imported");
//    }
//    catch(InterpreterException e) {
//      //Expected to fail
//    }
//
//    //Tests that a non-static method cannot be imported
//    testString =
//      "package XX;\n"+
//      "public class YY {\n"+
//      "  public int m() { return 5;}\n"+
//      "}\n"+
//      "package ZZ;\n"+
//      "import static XX.YY.m;\n";
//    try {
//      interpret(testString);
//      fail("Non static member should not be imported");
//    }
//    catch(InterpreterException e) {
//      //Expected to fail
//    }
//
//  }
//
//  /**
//   * Testing various forms of static importation of fields
//   */
//  public void testStaticImportOfFields(){
//    //Tests simple import on demand
//    testString =
//      "package A;\n"+
//      "public class B { \n"+
//      "  public static int C = 5; \n"+
//      "}\n"+
//      "package D;\n"+
//      "import static A.B.*;\n"+
//      "C;";
//    assertEquals(5,interpret(testString));
//
//    //Change packages, should still work
//    testString =
//      "package E;\n"+
//      "C;";
//    assertEquals(5,interpret(testString));
//
//    //tests simple single type import
//    testString =
//      "public class F{\n"+
//      "  public static int C = 3; \n"+
//      "}\n"+
//      "package G;\n"+
//      "import static E.F.C;\n"+
//      "C;";
//    assertEquals(3,interpret(testString));
//
//    //Tests single type import has higher priority than import on demand
//    testString =
//      "public interface H{\n"+
//      "  public static int C = 1; \n"+
//      "}\n"+
//      "public class I implements H {}\n"+
//      "package J;\n"+
//      "import static G.I.*;\n"+
//      "C;";
//    //Should not have changed C - last explicit import holds
//    assertEquals(3,interpret(testString));
//
//    //Tests the import of static member of super class/implemented interface
//    testString =
//      "import static G.I.C;\n"+
//      "C;";
//    assertEquals(1,interpret(testString));
//
//    //Tests the import of static member of interface
//    testString =
//      "import static G.H.C;\n"+
//      "C;";
//    assertEquals(1,interpret(testString));
//
//    //Tests that assignment to final field fails
//    testString =
//      "import static java.lang.Integer.MAX_VALUE;\n"+
//      "MAX_VALUE;";
//    assertEquals(java.lang.Integer.MAX_VALUE,interpret(testString));
//    testString =
//      "MAX_VALUE = 1;"+
//      "MAX_VALUE;";
//    try {
//      assertEquals(1,interpret(testString));
//      fail("Field is final, should not be mutable");
//    }
//    catch(InterpreterException e) {
//      //Expected to fail
//    }
//
//
//    //Tests that the redefinition of static final field succeeds
//    testString =
//      "import static javax.accessibility.AccessibleText.*;"+
//      "CHARACTER;";
//    assertEquals(javax.accessibility.AccessibleText.CHARACTER,interpret(testString));
//    testString =
//      "String CHARACTER = \"BLAH!\";"+
//      "CHARACTER;";
//    try {
//      assertEquals("BLAH!",interpret(testString));
//    }
//    catch(InterpreterException e) {
//      fail("Redefinition of staticly imported field should be allowed!");
//    }
//
//    //Tests that method parameter has preference over staticly imported field
//    testString =
//      "package K;\n"+
//      "public class L {\n"+
//      "  public static int x = 5;\n"+
//      "}\n"+
//      "package M;\n"+
//      "import static K.L.x;\n"+
//      "public class N { \n"+
//      "  public static int m(int x) { return x; } \n"+
//      "}\n"+
//      "N.m(3);";
//    assertEquals(3,interpret(testString));
//
//
//
//    //Tests that a non-static field cannot be imported
//    testString =
//      "package N;\n"+
//      "public class O {\n"+
//      "  public int field = 5;\n"+
//      "}\n"+
//      "package P;\n"+
//      "import static N.O.*;\n"+
//      "public class Q {\n"+
//      "  public int m() { return field; } \n"+
//      "}\n"+
//      "Q q = new Q(); q.m();";
//    try {
//      assertEquals(5,interpret(testString));
//      fail("Non static member should not be imported");
//    }
//    catch(InterpreterException e) {
//      //Expected to fail
//    }
//
//    //Tests that a non-static field cannot be imported
//    testString =
//      "package R;\n"+
//      "public class S {\n"+
//      "  public int field = 5;\n"+
//      "}\n"+
//      "package T;\n"+
//      "import static R.S.field;\n";
//    try {
//      interpret(testString);
//      fail("Non static member should not be imported");
//    }
//    catch(InterpreterException e) {
//      //Expected to fail
//    }
//  }
//
//
//  public void testParseStaticImport(){
//    testString =
//      "import static java.lang.Integer.MAX_VALUE;";
//
//    ImportDeclaration id = new ImportDeclaration("java.lang.Integer.MAX_VALUE", false, true, SourceInfo.NONE);
//    assertEquals(id, parse(testString).get(0));
//
//  }
//
//  public void testParseStaticImportStar(){
//    testString =
//      "import static java.lang.Integer.*;";
//
//    ImportDeclaration id = new ImportDeclaration("java.lang.Integer", true, true, SourceInfo.NONE);
//    assertEquals(id, parse(testString).get(0));
//  }
//
//  public void testNodeEquals(){
//    ImportDeclaration id1 = new ImportDeclaration("java.lang.Integer.MAX_VALUE", false, true, SourceInfo.NONE);
//    ImportDeclaration id2 = new ImportDeclaration("java.lang.Integer.MAX_VALUE", false, true, SourceInfo.NONE);
//
//    assertEquals(id1, id2);
//  }
//
//  public void testParseVarArgs(){
//    testString =
//      "public void someMethod(int ... i){}";
//
//    LinkedList<FormalParameter> params = new LinkedList<FormalParameter>();
//    FormalParameter fp = new FormalParameter(false,new ArrayTypeName(new IntTypeName(),1) ,"i");
//    params.add(fp);
//    List<Node> statements = new LinkedList<Node>();
//    //statements.add(new EmptyStatement());
//    BlockStatement body = new BlockStatement(statements);
//
//    MethodDeclaration md = new MethodDeclaration(
//                                                 java.lang.reflect.Modifier.PUBLIC | 0x00000080, // java.lang.reflect.Modifier.VARARGS == 0x00000080 /**/
//                                                 new VoidTypeName(),"someMethod",params,new LinkedList<ReferenceTypeName>(),body,SourceInfo.NONE);
//    assertEquals(md, parse(testString).get(0));
//  }
//
//  public void testInterpretPrimitiveVarArgs(){
//    testString =
//      "public class C {\n"+
//      "  public int someMethod(int ... i){\n"+
//      "    return i[3];\n"+
//      "  }\n"+
//      "}\n"+
//      "new C().someMethod(0,1,2,3);";
//    assertEquals(new Integer(3), interpret(testString));
//  }
//
//  public void testInterpretObjectVarArgs(){
//    testString = 
//      "public class C {\n"+
//      "  public String someMethod(String ... s){\n"+
//      "    String returnStr=\"\";\n"+
//      "    for(int i=0;i<s.length;i++) {\n"+
//      "      returnStr = returnStr+s[i];\n"+
//      "    }\n"+
//      "    return returnStr;\n"+
//      "  }\n"+
//      "}\n"+
//      "new C().someMethod(\"Str1\", \"Str2\", \"Str3\");\n";
//
//    assertEquals("Str1Str2Str3", interpret(testString));
//  }
//
//
//    //Testing Constructor with varargs
//    public void testInterpretConstructorVarArgs(){
//      testString =
//        "public class C {\n"+
//        "  String str = \"\";\n"+
//        "  public C(String ... s){\n"+
//        "    for(int i=0;i<s.length;i++) {\n"+
//        "      str = str+s[i];\n"+
//        "    }\n"+
//        "  }\n"+
//        "  public String getStr(){\n"+
//        "    return str;\n"+
//        "  }\n"+
//        "}\n"+
//        "new C(\"Str1\",\"Str2\",\"Str3\").getStr();\n";
//
//      assertEquals("Str1Str2Str3", interpret(testString));
//    }
//
//    //Test added to make sure we do not get a null pointer exception when you have (null, new Integer(1)), as parameters to a method taking (Integer...a)
//    //This bug has been fixed by adding an extra check in ReflectionUtilities, fixing the bug reported with number 1151966.
//    public void testVarArgsWithNullParameter(){
//      testString=
//        "class X{\n"+
//        "  String m(Integer ... a){\n"+
//        "    String result = \"\";\n"+
//        "    for(int i =0;i<a.length;i++){\n"+
//        "      if(a[i]==null){\n"+
//        "        result=result+\"null\";\n"+
//        "      }\n"+
//        "      else{\n"+
//        "        result=result+a[i].toString();\n"+
//        "      }\n"+
//        "    }\n"+
//        "    return result;\n"+
//        "  }\n"+
//        "}\n"+
//        "X x = new X();\n"+
//        "String result = x.m(null, new Integer(10), new Integer(20));\n"+
//        "result";
//     
//      assertEquals("null1020", interpret(testString));
//    }
//    // Testing constructor of an inner class with Varargs
//    public void testInterpretInnerClassConstructorVarArgs(){
//      testString =
//        "public class B {\n"+
//        "  public class C {\n"+
//        "    String str = \"\";\n"+
//        "    public C(String ... s){\n"+
//        "      for(int i=0;i<s.length;i++) {\n"+
//        "        str = str+s[i];\n"+
//        "      }\n"+
//        "    }\n"+
//        "    public String getStr(){\n"+
//        "      return str;\n"+
//        "    }\n"+
//        "  }\n"+
//        "}\n"+
//        "B b = new B();\n"+
//        "b.new C(\"Str1\",\"Str2\",\"Str3\",\"Str4\").getStr();\n";
//
//      assertEquals("Str1Str2Str3Str4", interpret(testString));
//    }
//
//    // Testing static method with varargs
//    public void testInterpretStaticMethodVarArgs(){
//      testString =
//        "public class C {\n"+
//        "  public static String someMethod(String ... s){\n"+
//        "    String returnStr=\"\";\n"+
//        "    for(int i=0;i<s.length;i++) {\n"+
//        "      returnStr = returnStr+s[i];\n"+
//        "    }\n"+
//        "    return returnStr;\n"+
//        "  }\n"+
//        "}\n"+
//        "C.someMethod(\"Str1\", \"Str2\", \"Str3\");\n";
//
//      assertEquals("Str1Str2Str3", interpret(testString));
//    }
//
//    //This fails until autoboxing works.
//    //Using ByteArrayOutputStream to avoid printing to console (and ByteArrayOutputStream is a non abstract subclass of OutputStream(
//    public void testInterpretPrimitivePrintf(){
//      testString =
//        "import java.io.PrintStream;\n"+
//        "import java.io.ByteArrayOutputStream;\n"+
//        "PrintStream ps = new PrintStream(new ByteArrayOutputStream());\n"+
//        "ps.printf(\"SomeStr %d somestr\",2652)\n;";
//      interpret(testString);
//    }
//
//    //Using ByteArrayOutputStream to avoid printing to console (and ByteArrayOutputStream is a non abstract subclass of OutputStream(
//    public void testInterpretMultiplePrintf(){
//      testString =
//        "import java.io.PrintStream;\n"+
//        "import java.io.ByteArrayOutputStream;\n"+
//        "PrintStream ps = new PrintStream(new ByteArrayOutputStream());\n"+
//        "ps.printf(\"SomeStr %d somestr\",new Integer(2))\n;"+
//        "ps.printf(\"SomeStr %s somestr\",\"str\")\n;"+
//        "ps.printf(\"SomeStr\",null)\n;"+
//        "ps.printf(\"SomeStr %d %s somestr\",new Integer(26),\"str\")\n;\n"+
//        "ps.printf(\"SomeStr\");";
//
//        interpret(testString);
//
//        /**
//         * This test was originally expected to fail, but we found that
//         * this actually was an acceptable varargs behavior
//         */
////        try {
////          testString =
////            "ps.printf(\"SomeStr\")\n;";
////          interpret(testString);
////          fail("Should have failed, as Printf needs some parameters");
////        }
////        catch(InterpreterException ie){
////          //Expected to fail.
////        }
//    }
//
////  public void xtestParseEnumDeclaration1(){
////    testString =
////      "public enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }";
////
////    ReferenceTypeName enumType = new ReferenceTypeName("Suit");
////
////    List<Expression> args1 = new LinkedList<Expression>();
////    args1.add(new StringLiteral("\"CLUBS\""));
////    args1.add(new IntegerLiteral("0"));
////    Allocation constantInit1 = new SimpleAllocation(enumType, args1);
////
////    List<Expression> args2 = new LinkedList<Expression>();
////    args2.add(new StringLiteral("\"DIAMONDS\""));
////    args2.add(new IntegerLiteral("1"));
////    Allocation constantInit2 = new SimpleAllocation(enumType, args2);
////
////    List<Expression> args3 = new LinkedList<Expression>();
////    args3.add(new StringLiteral("\"HEARTS\""));
////    args3.add(new IntegerLiteral("2"));
////    Allocation constantInit3 = new SimpleAllocation(enumType, args3);
////
////    List<Expression> args4 = new LinkedList<Expression>();
////    args4.add(new StringLiteral("\"SPADES\""));
////    args4.add(new IntegerLiteral("3"));
////    Allocation constantInit4 = new SimpleAllocation(enumType, args4);
////
////    int accessFlags = java.lang.reflect.Modifier.PUBLIC;
////    int fieldAccessFlags = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.STATIC | java.lang.reflect.Modifier.FINAL;
////    List<Node> body = new LinkedList<Node> ();
////
////    body.add(new FieldDeclaration(fieldAccessFlags, enumType, "CLUBS",    constantInit1));
////    body.add(new FieldDeclaration(fieldAccessFlags, enumType, "DIAMONDS", constantInit2));
////    body.add(new FieldDeclaration(fieldAccessFlags, enumType, "HEARTS",   constantInit3));
////    body.add(new FieldDeclaration(fieldAccessFlags, enumType, "SPADES",   constantInit4));
////
////    EnumDeclaration ed = new EnumDeclaration(accessFlags, "Suit", null, new EnumDeclaration.EnumBody(new LinkedList<EnumDeclaration.EnumConstant>(), body));
////    assertEquals(ed, parse(testString).get(0));
////  }
////
////
////  public void xtestParseEnumDeclaration2(){
////    testString =
////      "public enum Suit { \n"+
////      "  CLUBS(23), DIAMONDS(75), HEARTS(11), SPADES(61);\n"+
////      "  Suit(int i) {}\n"+
////      "}";
////
////    List<Expression> args1 = new LinkedList<Expression>();
////    args1.add(new StringLiteral("\"CLUBS\""));
////    args1.add(new IntegerLiteral("0"));
////    args1.add(new IntegerLiteral("23"));
////    List<Expression> args2 = new LinkedList<Expression>();
////    args2.add(new StringLiteral("\"DIAMONDS\""));
////    args2.add(new IntegerLiteral("1"));
////    args2.add(new IntegerLiteral("75"));
////    List<Expression> args3 = new LinkedList<Expression>();
////    args3.add(new StringLiteral("\"HEARTS\""));
////    args3.add(new IntegerLiteral("2"));
////    args3.add(new IntegerLiteral("11"));
////    List<Expression> args4 = new LinkedList<Expression>();
////    args4.add(new StringLiteral("\"SPADES\""));
////    args4.add(new IntegerLiteral("3"));
////    args4.add(new IntegerLiteral("61"));
////
////    ReferenceTypeName enumType = new ReferenceTypeName("Suit");
////    Allocation constantInit1 = new SimpleAllocation(enumType, args1);
////    Allocation constantInit2 = new SimpleAllocation(enumType, args2);
////    Allocation constantInit3 = new SimpleAllocation(enumType, args3);
////    Allocation constantInit4 = new SimpleAllocation(enumType, args4);
////    int accessFlags = java.lang.reflect.Modifier.PUBLIC;
////    int fieldAccessFlags = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.STATIC | java.lang.reflect.Modifier.FINAL;
////    List<Node> body = new LinkedList<Node> ();
////
////    List<FormalParameter> params = new LinkedList<FormalParameter>();
////    params.add(new FormalParameter(false, new IntTypeName(), "i"));
////    body.add(new ConstructorDeclaration(0, "Suit", params, new LinkedList<ReferenceTypeName>(), null, new LinkedList<Node>()));
////
////    body.add(new FieldDeclaration(fieldAccessFlags, enumType, "CLUBS",    constantInit1));
////    body.add(new FieldDeclaration(fieldAccessFlags, enumType, "DIAMONDS", constantInit2));
////    body.add(new FieldDeclaration(fieldAccessFlags, enumType, "HEARTS",   constantInit3));
////    body.add(new FieldDeclaration(fieldAccessFlags, enumType, "SPADES",   constantInit4));
////
////    EnumDeclaration ed = new EnumDeclaration(accessFlags, "Suit", null, new EnumDeclaration.EnumBody(new LinkedList<EnumDeclaration.EnumConstant>(), body));
////    assertEquals(ed, parse(testString).get(0));
////  }
////
////  public void xtestParseEnumDeclaration3(){
////    testString =
////      "public enum Suit { \n"+
////      "  CLUBS(23, null) {void m() {}  void n() {}},\n" +
////      "  DIAMONDS(75, CLUBS) {void m() {}},\n" +
////      "  HEARTS(11, CLUBS) {void m() {}},\n" +
////      "  SPADES(61, HEARTS) {void m() {}};\n"+
////      "  Suit(int i, Suit s) {}\n"+
////      "  abstract void m();\n"+
////      "  void n() {}\n"+
////      "}";
////
////    List<Expression> args1 = new LinkedList<Expression>();
////    args1.add(new StringLiteral("\"CLUBS\""));
////    args1.add(new IntegerLiteral("0"));
////    args1.add(new IntegerLiteral("23"));
////    args1.add(new NullLiteral());
////
////    List<Expression> args2 = new LinkedList<Expression>();
////    args2.add(new StringLiteral("\"DIAMONDS\""));
////    args2.add(new IntegerLiteral("1"));
////    args2.add(new IntegerLiteral("75"));
////    List<IdentifierToken> idnt2  = new LinkedList<IdentifierToken>();
////    idnt2.add(new Identifier("CLUBS"));
////    args2.add(new QualifiedName(idnt2));
////
////    List<Expression> args3 = new LinkedList<Expression>();
////    args3.add(new StringLiteral("\"HEARTS\""));
////    args3.add(new IntegerLiteral("2"));
////    args3.add(new IntegerLiteral("11"));
////    List<IdentifierToken> idnt3  = new LinkedList<IdentifierToken>();
////    idnt3.add(new Identifier("CLUBS"));
////    args3.add(new QualifiedName(idnt3));
////
////    List<Expression> args4 = new LinkedList<Expression>();
////    args4.add(new StringLiteral("\"SPADES\""));
////    args4.add(new IntegerLiteral("3"));
////    args4.add(new IntegerLiteral("61"));
////    List<IdentifierToken> idnt4  = new LinkedList<IdentifierToken>();
////    idnt4.add(new Identifier("HEARTS"));
////    args4.add(new QualifiedName(idnt4));
////
////    ReferenceTypeName enumType = new ReferenceTypeName("Suit");
////
////    List<Node> body1 = new LinkedList<Node>();
////    List<Node> body2 = new LinkedList<Node>();
////    List<Node> body3 = new LinkedList<Node>();
////    List<Node> body4 = new LinkedList<Node>();
////    MethodDeclaration m =   new MethodDeclaration(0, new VoidTypeName(), "m", new LinkedList<FormalParameter>(), new LinkedList<ReferenceTypeName>(), new BlockStatement(new LinkedList<Node>()));
////    MethodDeclaration n =   new MethodDeclaration(0, new VoidTypeName(), "n", new LinkedList<FormalParameter>(), new LinkedList<ReferenceTypeName>(), new BlockStatement(new LinkedList<Node>()));
////
////    body1.add(m);
////    body1.add(n);
////    body2.add(m);
////    body3.add(m);
////    body4.add(m);
////
////    ClassAllocation constantInit1 = new ClassAllocation(enumType, args1, body1);
////    ClassAllocation constantInit2 = new ClassAllocation(enumType, args2, body2);
////    ClassAllocation constantInit3 = new ClassAllocation(enumType, args3, body3);
////    ClassAllocation constantInit4 = new ClassAllocation(enumType, args4, body4);
////    int accessFlags = java.lang.reflect.Modifier.PUBLIC; // | java.lang.reflect.Modifier.ABSTRACT;
////    int fieldAccessFlags = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.STATIC | java.lang.reflect.Modifier.FINAL;
////
////    List<Node> body = new LinkedList<Node>();
////    List<FormalParameter> params = new LinkedList<FormalParameter>();
////    params.add(new FormalParameter(false, new IntTypeName(), "i"));
////    params.add(new FormalParameter(false, new ReferenceTypeName("Suit"), "s"));
////    body.add(new ConstructorDeclaration(0, "Suit", params, new LinkedList<ReferenceTypeName>(), null, new LinkedList<Node>()));
////    MethodDeclaration am = new MethodDeclaration(java.lang.reflect.Modifier.ABSTRACT, new VoidTypeName(), "m", new LinkedList<FormalParameter>(), new LinkedList<ReferenceTypeName>(), null);
////    body.add(am);
////    body.add(n);
////
////    body.add(new FieldDeclaration(fieldAccessFlags, enumType, "CLUBS",    constantInit1));
////    body.add(new FieldDeclaration(fieldAccessFlags, enumType, "DIAMONDS", constantInit2));
////    body.add(new FieldDeclaration(fieldAccessFlags, enumType, "HEARTS",   constantInit3));
////    body.add(new FieldDeclaration(fieldAccessFlags, enumType, "SPADES",   constantInit4));
////
////    EnumDeclaration ed = new EnumDeclaration(accessFlags, "Suit", null, new EnumDeclaration.EnumBody(new LinkedList<EnumDeclaration.EnumConstant>(), body));
////    assertEquals(ed, parse(testString).get(0));
////  }
//
//  public void testParseEnumDeclaration1(){
//    testString =
//      "public enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }";
//
//    ReferenceTypeName enumType = new ReferenceTypeName("Suit");
//
//    int accessFlags = java.lang.reflect.Modifier.PUBLIC;
//
//    List<EnumDeclaration.EnumConstant> consts = new LinkedList<EnumDeclaration.EnumConstant>();
//    consts.add(new EnumDeclaration.EnumConstant("CLUBS", null, null));
//    consts.add(new EnumDeclaration.EnumConstant("DIAMONDS", null, null));
//    consts.add(new EnumDeclaration.EnumConstant("HEARTS", null, null));
//    consts.add(new EnumDeclaration.EnumConstant("SPADES", null, null));
//
//    List<Node> decl = new LinkedList<Node> ();
//
//    EnumDeclaration ed = new EnumDeclaration(accessFlags, "Suit", null, new EnumDeclaration.EnumBody(consts, decl));
//    assertEquals(ed, parse(testString).get(0));
//  }
//
//  public void testParseEnumDeclaration2(){
//    testString =
//      "public enum Suit { \n"+
//      "  CLUBS(23), DIAMONDS(75), HEARTS(11), SPADES(61);\n"+
//      "  Suit(int i) {}\n"+
//      "}";
//
//    List<EnumDeclaration.EnumConstant> consts = new LinkedList<EnumDeclaration.EnumConstant>();
//
//    List<Expression> args1 = new LinkedList<Expression>();
//    args1.add(new IntegerLiteral("23"));
//    consts.add(new EnumDeclaration.EnumConstant("CLUBS", args1, null));
//
//    List<Expression> args2 = new LinkedList<Expression>();
//    args2.add(new IntegerLiteral("75"));
//    consts.add(new EnumDeclaration.EnumConstant("DIAMONDS", args2, null));
//
//    List<Expression> args3 = new LinkedList<Expression>();
//    args3.add(new IntegerLiteral("11"));
//    consts.add(new EnumDeclaration.EnumConstant("HEARTS", args3, null));
//
//    List<Expression> args4 = new LinkedList<Expression>();
//    args4.add(new IntegerLiteral("61"));
//    consts.add(new EnumDeclaration.EnumConstant("SPADES", args4, null));
//
//    int accessFlags = java.lang.reflect.Modifier.PUBLIC;
//
//    List<Node> decl = new LinkedList<Node> ();
//
//    List<FormalParameter> params = new LinkedList<FormalParameter>();
//    params.add(new FormalParameter(false, new IntTypeName(), "i"));
//    decl.add(new ConstructorDeclaration(0, "Suit", params, new LinkedList<ReferenceTypeName>(), null, new LinkedList<Node>()));
//
//    EnumDeclaration ed = new EnumDeclaration(accessFlags, "Suit", null, new EnumDeclaration.EnumBody(consts, decl));
//    assertEquals(ed, parse(testString).get(0));
//  }
//
//  public void testParseEnumDeclaration3(){
//    testString =
//      "public enum Suit { \n"+
//      "  CLUBS(23, null) {void m() {}  void n() {}},\n" +
//      "  DIAMONDS(75, CLUBS) {void m() {}},\n" +
//      "  HEARTS(11, CLUBS) {void m() {}},\n" +
//      "  SPADES(61, HEARTS) {void m() {}};\n"+
//      "  Suit(int i, Suit s) {}\n"+
//      "  abstract void m();\n"+
//      "  void n() {}\n"+
//      "}";
//
//    List<Expression> args1 = new LinkedList<Expression>();
//    args1.add(new IntegerLiteral("23"));
//    args1.add(new NullLiteral());
//
//    List<Expression> args2 = new LinkedList<Expression>();
//    args2.add(new IntegerLiteral("75"));
//    args2.add(new AmbiguousName("CLUBS"));
//
//    List<Expression> args3 = new LinkedList<Expression>();
//    args3.add(new IntegerLiteral("11"));
//    args3.add(new AmbiguousName("CLUBS"));
//
//    List<Expression> args4 = new LinkedList<Expression>();
//    args4.add(new IntegerLiteral("61"));
//    args4.add(new AmbiguousName("HEARTS"));
//
//    ReferenceTypeName enumType = new ReferenceTypeName("Suit");
//
//    List<Node> body1 = new LinkedList<Node>();
//    List<Node> body2 = new LinkedList<Node>();
//    List<Node> body3 = new LinkedList<Node>();
//    List<Node> body4 = new LinkedList<Node>();
//    MethodDeclaration m =   new MethodDeclaration(0, new VoidTypeName(), "m", new LinkedList<FormalParameter>(), new LinkedList<ReferenceTypeName>(), new BlockStatement(new LinkedList<Node>()));
//    MethodDeclaration n =   new MethodDeclaration(0, new VoidTypeName(), "n", new LinkedList<FormalParameter>(), new LinkedList<ReferenceTypeName>(), new BlockStatement(new LinkedList<Node>()));
//
//    body1.add(m);
//    body1.add(n);
//    body2.add(m);
//    body3.add(m);
//    body4.add(m);
//
//    LinkedList<EnumDeclaration.EnumConstant> consts = new LinkedList<EnumDeclaration.EnumConstant>();
//    consts.add(new EnumDeclaration.EnumConstant("CLUBS", args1, body1));
//    consts.add(new EnumDeclaration.EnumConstant("DIAMONDS", args2, body2));
//    consts.add(new EnumDeclaration.EnumConstant("HEARTS", args3, body3));
//    consts.add(new EnumDeclaration.EnumConstant("SPADES", args4, body4));
//
//    int accessFlags = java.lang.reflect.Modifier.PUBLIC;
//
//    List<Node> decl = new LinkedList<Node>();
//    List<FormalParameter> params = new LinkedList<FormalParameter>();
//    params.add(new FormalParameter(false, new IntTypeName(), "i"));
//    params.add(new FormalParameter(false, new ReferenceTypeName("Suit"), "s"));
//    decl.add(new ConstructorDeclaration(0, "Suit", params, new LinkedList<ReferenceTypeName>(), null, new LinkedList<Node>()));
//    MethodDeclaration am = new MethodDeclaration(java.lang.reflect.Modifier.ABSTRACT, new VoidTypeName(), "m", new LinkedList<FormalParameter>(), new LinkedList<ReferenceTypeName>(), null);
//    decl.add(am);
//    decl.add(n);
//
//    EnumDeclaration ed = new EnumDeclaration(accessFlags, "Suit", null, new EnumDeclaration.EnumBody(consts, decl));
//    assertEquals(ed, parse(testString).get(0));
//  }
//
//  void xtestParsingFaultyEnum1() {
//     testString =
//      "private enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }";
//
//  }
//
//  public void testInterpretEnum1() {
//    testString =
//      "public enum Suit { \n"+
//      "  CLUBS(23, 0.1),\n" +
//      "  DIAMONDS(75, 0.2),\n" +
//      "  HEARTS(11, 0.3),\n" +
//      "  SPADES(61, 0.4);\n"+
//      "  Suit(int i, double f) {}\n"+
//      "  int n() { return 0; }\n"+
//      "  static char mn() { return 'a'; }\n"+
//      "}";
//
//    assertEquals(null, interpret(testString));
//
//    testString = "Suit.mn()";
//    assertEquals("Suit.mn() should evaluate to a char 'a'", 'a', interpret(testString));
//
//    testString = "Suit.CLUBS.toString()";
//    assertEquals( "Suit.CLUBS.toString() should evaluate to \"CLUBS\"", "CLUBS", interpret(testString));
//
//    testString = "Suit.SPADES.toString()";
//    assertEquals( "Suit.SPADES.toString() should evaluate to \"SPADES\"", "SPADES", interpret(testString));
//
//    testString = "Suit.CLUBS.mn()";
//    assertEquals( "Suit.CLUBS.mn() should evaluate to 'a'", 'a', interpret(testString));
//
//    testString = "Suit.CLUBS.n()";
//    assertEquals( "Suit.CLUBS.n() should evaluate to 0", 0, interpret(testString));
//
//    testString = "Suit.DIAMONDS.n()";
//    assertEquals( "Suit.DIAMONDS.n() should evaluate to 0", 0, interpret(testString));
//
//    testString = "Suit.SPADES.n()";
//    assertEquals( "Suit.SPADES.n() should evaluate to 0", 0, interpret(testString));
//
//  }
//
//  public void xtestInterpretEnum2() {
//    // Seems DynamicJava does NOT support anonymous class bodies for enums as they contain
//    // "forward references" to their own classes (self-typed consts). Test thus not working.
//    //
//    testString =
//      "public enum Suit { \n"+
//      "  CLUBS(23, 0.1) {String m() { return \"1\"; }  int n() { return 1; }},\n" +
//      "  DIAMONDS(75, 0.2) {String m() { return \"2\"; }},\n" +
//      "  HEARTS(11, 0.3) {String m() { return \"3\"; }},\n" +
//      "  SPADES(61, 0.4) {String m() { return \"4\"; }};\n"+
//      "  Suit(int i, double f) {}\n"+
//      "  //abstract String m();\n"+
//      "  int n() { return 0; }\n"+
//      "  static char mn() { return 'a'; }\n"+
//      "}";
//
//    assertEquals(null, interpret(testString));
//
//    System.out.println(parse(testString).get(0));
//
//    testString =
//      "class Suit extends MyEnum {"+
//      "public static final Suit CLUBS = new Suit(\"CLUBS\", 0, 23, 0.1) { String m() { return \"1\"; } int n() { return 1; }};"+
//      "public static final Suit DIAMONDS = new Suit(\"DIAMONDS\", 1, 75, 0.2) { String m() { return \"2\"; } };"+
//      "public static final Suit HEARTS = new Suit(\"HEARTS\", 2, 11, 0.3) { String m() { return \"3\"; } };"+
//      "public static final Suit SPADES = new Suit(\"SPADES\", 1, 61, 0.4) { String m() { return \"4\"; } };"+
//
//      "Suit(String $1, int $2, int i, double f) { super($1, $2); }"+
//      "int n() { return 0; }"+
//      "static char mn() { return 'a'; }"+
//      "}";
//
//    System.out.println(parse(testString).get(0));
//
//     testString = "Suit.mn()";
//    //assertEquals("Suit.mn() Should evaluate to a char 'a'", 'a', interpret(testString));
////
////    "CLUBS", Suit.CLUBS, "SPADES", Suit.SPADES.toString()
////    'a', Suit.CLUBS.mn()
////    "1", Suit.CLUBS.m()
////    "2", Suit.DIAMONDS.m()
////    "4", Suit.SPADES.m()
////    1, Suit.CLUBS.n()
////    0, Suit.DIAMONDS.n()
////    0, Suit.SPADES.n()
////
//  }
//
//  public void testInterpretEnumValues() {
//    testString =
//      "public enum Suit { \n"+
//      "  CLUBS(23, 0.1),\n" +
//      "  DIAMONDS(75, 0.2),\n" +
//      "  HEARTS(11, 0.3),\n" +
//      "  SPADES(61, 0.4);\n"+
//      "  Suit(int i, double f) {}\n"+
//      "  int n() { return 0; }\n"+
//      "  static char mn() { return 'a'; }\n"+
//      "}";
//
//    assertEquals(null, interpret(testString));
//
//    testString = "String str1 = \"\";\n"+
//      "for( int i = 0; i < Suit.values().length; i++)\n"+
//      "  str1 += Suit.values()[i];\n"+
//      "str1";
//    String forEachTestString = "String str2 = \"\";\n"+
//      "for(Suit s : Suit.values())\n"+
//      "  str2 += s;\n"+
//      "str2";
//
//    assertEquals("Concat of all suits should evaluate to \"CLUBSDIAMONDSHEARTSSPADES\"", "CLUBSDIAMONDSHEARTSSPADES", interpret(testString));
//    assertEquals("Concat, using forEach, of all suits should evaluate to \"CLUBSDIAMONDSHEARTSSPADES\"", "CLUBSDIAMONDSHEARTSSPADES", interpret(forEachTestString));
//  }
//
//  public void testInterpretEnumValueOf() {
//    testString =
//      "public enum Suit { \n"+
//      "  CLUBS(23, 0.1),\n" +
//      "  DIAMONDS(75, 0.2),\n" +
//      "  HEARTS(11, 0.3),\n" +
//      "  SPADES(61, 0.4);\n"+
//      "  Suit(int i, double f) {}\n"+
//      "  int n() { return 0; }\n"+
//      "  static char mn() { return 'a'; }\n"+
//      "}";
//
//    assertEquals(null, interpret(testString));
//
//    testString = "String str = \"\";\n"+
//      "for(Suit s : Suit.values())\n"+
//      "  str += Suit.valueOf(s.name());\n"+
//      "str";
//
//    assertEquals("Concat of all suits should evaluate to \"CLUBSDIAMONDSHEARTSSPADES\"", "CLUBSDIAMONDSHEARTSSPADES", interpret(testString));
//  }
//
//  public void testEnumSwitch(){
//    testString = "enum Suit { CLUBS, HEARTS };\n"+
//      "Suit s = Suit.HEARTS;\n"+
//      "String str;\n"+
//      "switch(s){\n"+
//      "  case Suit.CLUBS: str = \"Case Clubs\"; break;\n"+
//      "  case Suit.HEARTS: str = \"Case Hearts\"; break;\n"+
//      "}\n"+
//      "str";
//    
//    assertEquals("Str should equal \"Case Hearts\"", "Case Hearts", interpret(testString));
//    
//    testString = "s = Suit.CLUBS;\n"+
//      "switch(s){\n"+
//      "  case Suit.CLUBS: str = \"Case Clubs\"; break;\n"+
//      "  case Suit.HEARTS: str = \"Case Hearts\"; break;\n"+
//      "}\n"+
//      "str";
//    
//    assertEquals("Str should equal \"Case Clubs\"", "Case Clubs", interpret(testString));
//  }
//
//  public void testNoNullPointerExceptionForEmptyEnum(){
//    testString =
//      "public enum Suit { ; }";
//    assertEquals("Interpreting empty enum should return a null", null, interpret(testString));
//  }
//
//  public void testExplicitPolymorhicMethodCall(){
//    testString =   
//      "class CC {\n"+
//      "  CC(){\n"+
//      "    <Integer>m(new Integer(6), new Integer(7));\n"+
//      "  }\n"+
//      "  static <S> void m(S s, S t){}\n"+
//      "}\n"+
//      "new CC();\n";
//    assertEquals("Parsing and interpreting a new CC() should throw no exceptions", "CC", interpret(testString).getClass().getName());
//  }
//  
//  public void testExplicitPolymorhicConstructorCall(){
//    testString =   
//      "class CC {\n"+
//      "  <T> CC(){\n"+
//      "  }\n"+
//      "}\n"+
//      "new <Integer>CC();\n";
//    assertEquals("Parsing and interpreting a new <Integer>CC() should throw no exceptions", "CC", interpret(testString).getClass().getName());
//  }
//  
//  public void testEnumConstructorsInvocationsDoNotAcceptTypeInfo_MethodsDo(){
//    testString =   
//      "enum Suit {\n"+
//      "  CLUBS(new Integer(5), new Integer(7)),\n"+
//      "  SPADE(new String(\"hi\"), new String(\"bar\"));\n"+ 
//      "  <T> Suit(T t, T s){\n"+
//      "    <Integer>m(s);\n"+
//      "  }\n"+
//      "  <S> void m(S s){}\n"+
//      "}\n"+
//      "Suit.CLUBS";
//    assertEquals("Parsing and interpreting Suit.CLUBS should throw no exceptions", "CLUBS", interpret(testString).toString());
//  }
//  
////  class CC {
////  <T> CC(T t){
////    CC cc = new <Integer>CC(new Integer(5));
////    cc.m(new Integer(6), new Integer(7));
////  }
////  
////  CC() {<Integer>this(new Integer(7));}
////  
////  <S> void m(S s, S t){}
////}
////
////enum Suit { CLUBS(new Integer(5), new Integer(7)), SPADE(new String("hi"), new String("bar")); <T> Suit(T t, T s){<Integer>m(s);} <S> void m(S s){}}
//}