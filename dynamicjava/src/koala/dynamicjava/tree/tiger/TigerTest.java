package koala.dynamicjava.tree.tiger;

import java.util.*;
import junit.framework.*;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.SourceInfo;

import java.io.StringReader;
import java.util.List;
import koala.dynamicjava.parser.wrapper.ParserFactory;
import koala.dynamicjava.parser.wrapper.JavaCCParserFactory;


public class TigerTest extends TestCase {
  private TreeInterpreter astInterpreter;
  private TreeInterpreter strInterpreter;
  
  private ParserFactory parserFactory;
  private String testString;
  
  public TigerTest(String name) {
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
//    List<Node> exps = strInterpreter.buildStatementList(new java.io.StringReader(testString), "Unit Test");
//    return astInterpreter.interpret(exps);
  }
  
  // Interpreting static import. Static import of fields not yet supported
  public void testStaticImport(){
//    //STATIC FIELD
//    testString =
//      "import static java.lang.Integer.MAX_VALUE;\n"+
//      "class A{\n"+
//      "  int m(){return MAX_VALUE;}\n"+
//      "}\n"+
//      "A a = new A(); a.m();\n";
//    
//    assertEquals(new Integer(java.lang.Integer.MAX_VALUE), interpret(testString));
    
    //STATIC METHOD
    testString = 
      "import static java.lang.Math.abs;\n"+
      "class B{\n"+
      "   int m(){return abs(-2);}\n"+
      "}\n"+
      "B b = new B(); b.m();\n";
    assertEquals(new Integer(2), interpret(testString));
    
  }
  
  public void testStaticImportOfStaticInnerClass(){
    testString = 
      "package P;\n"+
      "public class A { \n"+
      "  public static class B {\n"+
      "    public static int m(){ return 0; }\n"+
      "  }\n"+
      "}\n"+
      "package Q;\n"+
      "import static P.A.B;\n"+
      "B.m();\n";
    assertEquals(0,interpret(testString));
    
    testString = 
      "package R;\n"+
      "public class C { \n"+
      "  public static class D {\n"+
      "    public static int m(){ return 0; }\n"+
      "  }\n"+
      "}\n"+
      "package S;\n"+
      "import static R.C.*;\n"+
      "D.m();\n";
    assertEquals(0,interpret(testString));
    
  }
  
  public void testStaticImportOfMethods(){
    
    //no parameters
    testString = 
      "package Pack;\n"+
      "public class Aclass { \n"+
      "  public static int m() { return 0; }\n"+
      "}\n"+
      "package Q;\n"+
      "import static Pack.Aclass.m;\n"+
      "m();\n";
    assertEquals(0,interpret(testString));
    
    testString = 
      "package R;\n"+
      "public class C { \n"+
      "  public static int m2(){ return 0; }\n"+
      "}\n"+
      "package S;\n"+
      "import static R.C.*;\n"+
      "m2();\n";
    assertEquals(0,interpret(testString));
    
    //With a parameter
    
    testString =
      "package T;\n"+
      "public class D { \n"+
      "  public static String m3(String s) { return s; }\n"+
      "}\n"+
      "package U;\n"+
      "import static T.D.m3;\n"+
      "m3(\"5\");\n";
    assertEquals("5",interpret(testString));
    
    testString =
      "package V;\n"+
      "public class E { \n"+
      "  public static String m4(String s, String s2) { return s; }\n"+
      "}\n"+
      "package W;\n"+
      "import static V.E.*;\n"+
      "m4(\"5\",\"6\");\n";
    assertEquals("5",interpret(testString));
    
    
    testString = 
      "package AA;\n"+
      "public class BB {"+
      "  public static int m5(int i) { return i; }\n"+
      "}\n"+
      "package CC;\n"+
      "import static AA.BB.m5;"+
      "m5(1+2+3+4+5);\n";
    assertEquals(15,interpret(testString));
    
    
    //With multiple parameters of different types
    
    testString =
      "package X;\n"+
      "public class F { \n"+
      "  public static String m6(String s, Class c) { return s; }\n"+
      "}\n"+
      "package Y;\n"+
      "import static X.F.*;\n"+
      "m6(\"5\",Integer.class);\n";
    assertEquals("5",interpret(testString));
    
    
    //With unequal parameters
    testString =
      "package DD;\n"+
      "public class EE { \n"+
      "  public static int m7(int i) { return i; }\n"+
      "}\n"+
      "package FF;\n"+
      "import static DD.EE.*;\n"+
      "m7(\"5\");\n";
    try {
      assertEquals(5,interpret(testString));
      fail("Method parameter types String and int are not equal!");
    }
    catch(Error e) {
      //Expected to fail
    }
    
    //Test for context shift
    //In other words, because we run a TypeChecker on the arguments, make sure that it doesn't actually add 
    // the element 5 to the list when it is checking its type for the name visitor.
    testString = 
      "package GG;\n"+
      "public class HH {\n"+
      "  public static int m8(int i) { return i; } \n" +
      "}\n"+
      "package II;\n"+
      "import static GG.HH.m8;\n"+
      "int i = 0;"+
      "m8(i);";
    assertEquals(0,interpret(testString));
    testString =
      "m8(i++);";
    assertEquals(0,interpret(testString));
    testString =
      "m8(++i);";
    assertEquals(2,interpret(testString));
    
    
    
    testString = 
      "package KK;\n"+
      "import static java.lang.Math.*;\n"+
      "abs(-4);";
    assertEquals(4,interpret(testString));
    testString = 
      "sqrt(4);";
    assertEquals(2.0,interpret(testString));
    testString =
      "sqrt(abs(-4));";
    assertEquals(2.0,interpret(testString));
    
    
    
  }
      
  
  public void testParseStaticImport(){
    testString =
      "import static java.lang.Integer.MAX_VALUE;";
    
    ImportDeclaration id = new ImportDeclaration("java.lang.Integer.MAX_VALUE", false, true, null, 0, 0, 0, 0);
    assertEquals(id, parse(testString).get(0));
    
  }
  
  public void testParseStaticImportStar(){
    testString =
      "import static java.lang.Integer.*;";
    
    ImportDeclaration id = new ImportDeclaration("java.lang.Integer", true, true, null, 0, 0, 0, 0);
    assertEquals(id, parse(testString).get(0));
  }
  
  public void testNodeEquals(){
    ImportDeclaration id1 = new ImportDeclaration("java.lang.Integer.MAX_VALUE", false, true, null, 0, 0, 0, 0);
    ImportDeclaration id2 = new ImportDeclaration("java.lang.Integer.MAX_VALUE", false, true, null, 0, 0, 0, 0);
    
    assertEquals(id1, id2);
  }
  
  public void testParseVarArgs(){
    testString =
      "public void someMethod(int ... i){}";
    
    LinkedList<FormalParameter> params = new LinkedList<FormalParameter>();
    FormalParameter fp = new FormalParameter(false,new ArrayType(new IntType(),1) ,"i");
    params.add(fp);
    List<Node> statements = new LinkedList<Node>();
    //statements.add(new EmptyStatement());
    BlockStatement body = new BlockStatement(statements);
    
    MethodDeclaration md = new MethodDeclaration(
                                                 java.lang.reflect.Modifier.PUBLIC | 0x00000080, // java.lang.reflect.Modifier.VARARGS == 0x00000080 /**/
                                                 new VoidType(),"someMethod",params,new LinkedList<ReferenceType>(),body,null, 0, 0, 0, 0);
    assertEquals(md, parse(testString).get(0));
  }
  
  public void testInterpretPrimitiveVarArgs(){
    testString =
      "public class C {\n"+
      "  public int someMethod(int ... i){\n"+
      "    return i[3];\n"+
      "  }\n"+
      "}\n"+
      "new C().someMethod(0,1,2,3);";
    
    assertEquals(new Integer(3), interpret(testString));
  }
  
    public void testInterpretObjectVarArgs(){
    testString =
      "public class C {\n"+
      "  public String someMethod(String ... s){\n"+
      "    String returnStr=\"\";\n"+
      "    for(int i=0;i<s.length;i++) {\n"+
      "      returnStr = returnStr+s[i];\n"+
      "    }\n"+
      "    return returnStr;\n"+
      "  }\n"+
      "}\n"+
      "new C().someMethod(\"Str1\", \"Str2\", \"Str3\");\n";
    
    assertEquals("Str1Str2Str3", interpret(testString));
  }
    
    
    //Testing Constructor with varargs
    public void testInterpretConstructorVarArgs(){
      testString =
        "public class C {\n"+
        "  String str = \"\";\n"+
        "  public C(String ... s){\n"+
        "    for(int i=0;i<s.length;i++) {\n"+
        "      str = str+s[i];\n"+
        "    }\n"+
        "  }\n"+
        "  public String getStr(){\n"+
        "    return str;\n"+
        "  }\n"+
        "}\n"+
        "new C(\"Str1\",\"Str2\",\"Str3\").getStr();\n";
    
      assertEquals("Str1Str2Str3", interpret(testString));
    }
    
    // Testing constructor of an inner class with Varargs
    public void testInterpretInnerClassConstructorVarArgs(){
      testString =
        "public class B {\n"+
        "  public class C {\n"+
        "    String str = \"\";\n"+
        "    public C(String ... s){\n"+
        "      for(int i=0;i<s.length;i++) {\n"+
        "        str = str+s[i];\n"+
        "      }\n"+
        "    }\n"+
        "    public String getStr(){\n"+
        "      return str;\n"+
        "    }\n"+
        "  }\n"+
        "}\n"+
        "B b = new B();\n"+
        "b.new C(\"Str1\",\"Str2\",\"Str3\").getStr();\n";
    
      assertEquals("Str1Str2Str3", interpret(testString));
    }
    
    // Testing static method with varargs
    public void testInterpretStaticMethodVarArgs(){
      testString =
        "public class C {\n"+
        "  public static String someMethod(String ... s){\n"+
        "    String returnStr=\"\";\n"+
        "    for(int i=0;i<s.length;i++) {\n"+
        "      returnStr = returnStr+s[i];\n"+
        "    }\n"+
        "    return returnStr;\n"+
        "  }\n"+
        "}\n"+
        "C.someMethod(\"Str1\", \"Str2\", \"Str3\");\n";
      
      assertEquals("Str1Str2Str3", interpret(testString));
    }
        
    //This fails until autoboxing works.
    //Using ByteArrayOutputStream to avoid printing to console (and ByteArrayOutputStream is a non abstract subclass of OutputStream(
    public void testInterpretPrimitivePrintf(){
      testString =
        "import java.io.PrintStream;\n"+
        "import java.io.ByteArrayOutputStream;\n"+
        "PrintStream ps = new PrintStream(new ByteArrayOutputStream());\n"+
        "ps.printf(\"SomeStr %d somestr\",2652)\n;";
      interpret(testString);
    }
      
    //Using ByteArrayOutputStream to avoid printing to console (and ByteArrayOutputStream is a non abstract subclass of OutputStream(
    public void testInterpretMultiplePrintf(){
      testString =
        "import java.io.PrintStream;\n"+
        "import java.io.ByteArrayOutputStream;\n"+
        "PrintStream ps = new PrintStream(new ByteArrayOutputStream());\n"+
        "ps.printf(\"SomeStr %d somestr\",new Integer(2))\n;"+
        "ps.printf(\"SomeStr %s somestr\",\"str\")\n;"+
        "ps.printf(\"SomeStr\",null)\n;"+
        "ps.printf(\"SomeStr %d %s somestr\",new Integer(26),\"str\")\n;\n"+
        "ps.printf(\"SomeStr\");";
        
        interpret(testString);
        
        /** 
         * This test was originally expected to fail, but we found that 
         * this actually was an acceptable varargs behavior
         */
//        try {
//          testString = 
//            "ps.printf(\"SomeStr\")\n;";
//          interpret(testString);
//          fail("Should have failed, as Printf needs some parameters");
//        }
//        catch(InterpreterException ie){
//          //Expected to fail.
//        }
    }
    
}