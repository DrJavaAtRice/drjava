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
  }
  
  // Interpreting STATIC IMPORT, NOT YET SUPPORTED
  public void xtestStaticImport(){
    testString =
      "import static java.lang.Integer.MAX_VALUE;\n"+
      "class A{\n"+
      "  int m(){return MAX_VALUE;}\n"+
      "}\n"+
      "A a = new A(); a.m();\n";
    
    assertEquals(new Integer(java.lang.Integer.MAX_VALUE), interpret(testString));
    
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
}