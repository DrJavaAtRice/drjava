package koala.dynamicjava.parser.wrapper;

import junit.framework.*;

import koala.dynamicjava.parser.*;
import koala.dynamicjava.tree.*;

import java.io.StringReader;
import java.util.*;

/**
 * Tests the interactions parser.
 * @version $Id$
 */
public class ParserTest extends TestCase {
  
  List<Node> expectedAST;
      
  public ParserTest( String name ){
    super(name);
  }
  
  public void verifyOutput( String inputString, List<Node> expectedAST ) throws ParseException {
    List<Node> ast = new Parser(new StringReader(inputString)).parseStream();
    assertEquals( "Parsed input AST does not match expected output AST", 
                   ast,
                   expectedAST );

  }  
  
  public void verifyError(String inputString, List<Node> expectedAST,
                          int startLine, int startColumn, 
                          int endLine, int endColumn)
  {
    try {
      verifyOutput(inputString, expectedAST);
      fail("Invalid inputs should cause ParseExceptions");
    }
    catch(ParseException ex) {
      verifyErrorLocation(ex, startLine, startColumn, endLine, endColumn);
    }
  }
  
  public void verifyErrorLocation(ParseException ex,
                                  int startLine, int startColumn, 
                                  int endLine, int endColumn)
  {
    assertEquals("Wrong begin line of error", startLine, ex.getBeginLine());
    assertEquals("Wrong begin column of error", startColumn, ex.getBeginColumn());
    assertEquals("Wrong end line of error", endLine, ex.getEndLine());
    assertEquals("Wrong end column of error", endColumn, ex.getEndColumn());
  }

  protected void setUp(){
    expectedAST = new LinkedList<Node>();
  }
  
  public void testPackageDeclaration() throws ParseException {
    expectedAST.add(new PackageDeclaration("edu.rice.cs.javaast", null, -1, -1, -1, -1));
    verifyOutput("package edu.rice.cs.javaast;", expectedAST);
  }  
  
  public void testPackageImportDeclaration() throws ParseException {
    expectedAST.add(new ImportDeclaration("java.io", true, false, null, -1, -1, -1, -1));
    verifyOutput("import java.io.*;", expectedAST);
  }  
  
  public void testStaticImportDeclaration() throws ParseException {
    expectedAST.add(new ImportDeclaration("java.io", true, true, null, -1, -1, -1, -1));
    verifyOutput("import static java.io.*;", expectedAST);
  }  
  
  public void testClassImportDeclaration() throws ParseException {
    expectedAST.add(new ImportDeclaration("java.lang.String", false, false, null, -1, -1, -1, -1));
    verifyOutput("import java.lang.String;", expectedAST);
  }  
  
  public void testClassDeclaration() throws ParseException {
    int accessFlags = 0;
    accessFlags |= java.lang.reflect.Modifier.PUBLIC;
    accessFlags |= java.lang.reflect.Modifier.ABSTRACT;
    List<Node> body = new LinkedList<Node>();
    expectedAST.add(new ClassDeclaration(accessFlags, "Foo", new ReferenceType("Bar"), null, body, null, -1, -1, -1, -1));
    verifyOutput("public abstract class Foo extends Bar{}", expectedAST);
  }
  
  public void testInterfaceDeclaration() throws ParseException {
    int accessFlags = 0;
    List<Node> body = new LinkedList<Node>();
    expectedAST.add(new InterfaceDeclaration(accessFlags, "i", null, body, null, -1, -1, -1, -1));
    verifyOutput("interface i{}", expectedAST);
  }
  
  public void testMethodDeclaration() throws ParseException {
    int accessFlags = 0;
    accessFlags |= java.lang.reflect.Modifier.STATIC;
    accessFlags |= java.lang.reflect.Modifier.PUBLIC;
    List<FormalParameter> params = new LinkedList<FormalParameter>();
    List<? extends ReferenceType> excepts = new LinkedList<ReferenceType>();
    List<Node> stmts = new LinkedList<Node>();
    stmts.add(new ReturnStatement(new IntegerLiteral("1")));
    BlockStatement body = new BlockStatement(stmts);
    
    expectedAST.add(new MethodDeclaration(accessFlags, new IntType(), "getCount", params, excepts, body));
    
    verifyOutput("static public int getCount(){ return 1; }", expectedAST);
  }
  
  public void testLocalVariableDeclarationList() throws ParseException {
    VariableDeclaration iVD = new VariableDeclaration(false, new IntType(), "i", new IntegerLiteral("0"));
    VariableDeclaration jVD = new VariableDeclaration(false, new IntType(), "j", null);
    VariableDeclaration kVD = new VariableDeclaration(false, new IntType(), "k", new IntegerLiteral("2"));
  
    expectedAST.add(iVD);
    expectedAST.add(jVD);
    expectedAST.add(kVD);
    
    verifyOutput("int i=0, j, k=2;", expectedAST);
  }
  
  public void testLabeledStatement() throws ParseException {
    
    expectedAST.add(new LabeledStatement("v", new SimpleAllocation(new ReferenceType("Object"), null)));
    
    verifyOutput("v : new Object();", expectedAST);
  } 
  
  public void testBlock() throws ParseException {
    List<Node> stmts = new LinkedList<Node> ();
    
    List<IdentifierToken> idnt1  = new LinkedList<IdentifierToken>();
    idnt1.add(new Identifier("i"));
    stmts.add(new SimpleAssignExpression(new QualifiedName(idnt1),new IntegerLiteral("3")));
    
    List<IdentifierToken> idnt2  = new LinkedList<IdentifierToken>();
    idnt2.add(new Identifier("v"));
    stmts.add(new SimpleAssignExpression(new QualifiedName(idnt2),new SimpleAllocation(new ReferenceType("Vector"), null)));
    
    List<IdentifierToken> idnt3  = new LinkedList<IdentifierToken>();
    idnt3.add(new Identifier("i"));
    stmts.add(new PostDecrement(new QualifiedName(idnt3)));
    
    expectedAST.add(new BlockStatement(stmts));
    
    verifyOutput("{ (i=3); (v = new Vector()); (i)--; }", expectedAST);
  }
  
  public void testBlockWithEmptyStatements() throws ParseException {
    List<Node> stmts = new LinkedList<Node> ();
    
    List<IdentifierToken> idnt1  = new LinkedList<IdentifierToken>();
    idnt1.add(new Identifier("i"));
    stmts.add(new SimpleAssignExpression(new QualifiedName(idnt1),new IntegerLiteral("3")));
    
    stmts.add(new EmptyStatement());
    
    List<IdentifierToken> idnt2  = new LinkedList<IdentifierToken>();
    idnt2.add(new Identifier("v"));
    stmts.add(new SimpleAssignExpression(new QualifiedName(idnt2),new SimpleAllocation(new ReferenceType("Vector"), null)));
    
    List<IdentifierToken> idnt3  = new LinkedList<IdentifierToken>();
    idnt3.add(new Identifier("i"));
    stmts.add(new PostDecrement(new QualifiedName(idnt3)));
    
    stmts.add(new EmptyStatement());
    
    expectedAST.add(new BlockStatement(stmts));

    verifyOutput("{ (i=3);; (v = new Vector()); (i)--;; }", expectedAST);
  }
  
  public void testEmptyStatement() throws ParseException {
    expectedAST.add(new EmptyStatement());
    verifyOutput(";", expectedAST);
  }
  
  public void testStatementExpression() throws ParseException {
    List<IdentifierToken> idnt1  = new LinkedList<IdentifierToken>();
    idnt1.add(new Identifier("o"));
    expectedAST.add(new ObjectMethodCall(new QualifiedName(idnt1), "m", null, "", -1, -1, -1, -1));
    verifyOutput("o.m();", expectedAST);
  }  
  
  public void testStatementExpression2() throws ParseException {
    List<Expression> args = new LinkedList<Expression> ();
    args.add(new IntegerLiteral("0"));
    expectedAST.add(new ObjectMethodCall(new SimpleAllocation(new ReferenceType("Integer"), args), "intValue", null, "", -1, -1, -1, -1));
    verifyOutput("(new Integer(0)).intValue();", expectedAST);
  }
  
  public void testSwitchStatement() throws ParseException {
    List<SwitchBlock> cases = new LinkedList<SwitchBlock>();
    
    List<Node> stmts1 = new LinkedList<Node> ();
    List<IdentifierToken> idnto  = new LinkedList<IdentifierToken>();
    idnto.add(new Identifier("o"));
    stmts1.add(new ObjectMethodCall(new QualifiedName(idnto), "m", null, "", -1, -1, -1, -1));
    stmts1.add(new BreakStatement(null));
                 
    List<Node> stmts2 = new LinkedList<Node>();
    stmts2.add(new EmptyStatement());
    
    List<Node> stmts3 = new LinkedList<Node>();
    stmts3.add(new BreakStatement(null));
    
    cases.add(new SwitchBlock(new IntegerLiteral("0"), stmts1));
    cases.add(new SwitchBlock(new IntegerLiteral("1"), stmts2));
    cases.add(new SwitchBlock(new IntegerLiteral("2"), stmts3));    
    
    List<IdentifierToken> idnt1  = new LinkedList<IdentifierToken>();
    idnt1.add(new Identifier("i"));
    expectedAST.add(new SwitchStatement(new QualifiedName(idnt1), cases, "", -1, -1, -1, -1));
    verifyOutput("switch( i ) { case 0: o.m(); break; case 1: ; case 2: break; }", expectedAST);
  }
  
  public void testIfStatement() throws ParseException {
    expectedAST.add(new IfThenElseStatement(new BooleanLiteral(true), new ReturnStatement(new BooleanLiteral(true)), 
                                                                      new ReturnStatement(new BooleanLiteral(false))));
    verifyOutput("if (true) return true; else return false;", expectedAST);
  }
  
  public void testIfStatement2() throws ParseException {
    List<Node> stmts = new LinkedList<Node> ();
    stmts.add(new EmptyStatement());
    
    List<IdentifierToken> idnt1  = new LinkedList<IdentifierToken>();
    idnt1.add(new Identifier("o"));

    List<IdentifierToken> idnt2  = new LinkedList<IdentifierToken>();
    idnt2.add(new Identifier("o"));
    
    expectedAST.add(new IfThenElseStatement(new ObjectMethodCall(new QualifiedName(idnt1), "m", null, "", -1, -1, -1, -1), 
                                            new BlockStatement(stmts), 
                                            new IfThenStatement(new BooleanLiteral(true),new ObjectMethodCall(new QualifiedName(idnt2), "m", null, "", -1, -1, -1, -1))));
    verifyOutput("if (o.m()) { ; } else if (true) o.m();", expectedAST);
  }
  
  public void testIfStatement3() throws ParseException {
    List<IdentifierToken> idnt1  = new LinkedList<IdentifierToken>();
    idnt1.add(new Identifier("o"));
    
    List<IdentifierToken> idnt2  = new LinkedList<IdentifierToken>();
    idnt2.add(new Identifier("SomeClass"));
    
    expectedAST.add(new IfThenStatement(new ObjectFieldAccess(new QualifiedName(idnt2), "CONSTANT"),
                                        new IfThenElseStatement(new ObjectMethodCall(new QualifiedName(idnt1), "m", null, "", -1, -1, -1, -1), 
                                                                new ReturnStatement(new BooleanLiteral(true)), 
                                                                new ReturnStatement(new BooleanLiteral(false)))));
    verifyOutput("if ( SomeClass.CONSTANT ) if ( o.m() ) return true; else return false; ", expectedAST);
  }
  
  public void testWhileStatement() throws ParseException {
    List<Expression> args = new LinkedList<Expression> ();
    args.add(new StringLiteral("\"Infinite Loop\""));
    
    List<IdentifierToken> idnt  = new LinkedList<IdentifierToken>();
    idnt.add(new Identifier("System.out"));

    List<Node> stmts = new LinkedList<Node> ();
    stmts.add(new ObjectMethodCall(new QualifiedName(idnt), "println", args, "", -1, -1, -1, -1));
    
    expectedAST.add(new WhileStatement(new BooleanLiteral(true),new BlockStatement(stmts)));
    verifyOutput("while (true) { System.out.println(\"Infinite Loop\"); }", expectedAST);
  }
  
  public void testDoStatement() throws ParseException {
    List<IdentifierToken> idnt1  = new LinkedList<IdentifierToken>();
    idnt1.add(new Identifier("i"));
    
    List<IdentifierToken> idnt2  = new LinkedList<IdentifierToken>();
    idnt2.add(new Identifier("i"));
    
    List<Node> stmts = new LinkedList<Node> ();
    stmts.add(new PostDecrement(new QualifiedName(idnt2)));
    stmts.add(new EmptyStatement());
       
    expectedAST.add(new DoStatement(new GreaterOrEqualExpression(new QualifiedName(idnt1), new IntegerLiteral("0")), new BlockStatement(stmts)));
    verifyOutput("do {(i)--; ; } while ( (i) >= (0) );", expectedAST);
  }
  
  public void testForStatement() throws ParseException {
    
    List<IdentifierToken> idnt1  = new LinkedList<IdentifierToken>();
    idnt1.add(new Identifier("i"));
    
    List<IdentifierToken> idnt2  = new LinkedList<IdentifierToken>();
    idnt2.add(new Identifier("something"));
    
    List<IdentifierToken> idnt3  = new LinkedList<IdentifierToken>();
    idnt3.add(new Identifier("i"));
    
    List<IdentifierToken> idnt4  = new LinkedList<IdentifierToken>();
    idnt4.add(new Identifier("o"));
    
    List<Expression> args = new LinkedList<Expression> ();
    args.add(new QualifiedName(idnt2));
    
    List<Node> init = new LinkedList<Node> ();
    init.add(new VariableDeclaration(false, new IntType(), "i", new IntegerLiteral("0")));
    
    List<Node> updt = new LinkedList<Node> ();
    updt.add(new PostIncrement(new QualifiedName(idnt3)));
    
    List<Node> stmts = new LinkedList<Node> ();
    stmts.add(new EmptyStatement());
    stmts.add(new EmptyStatement());
    stmts.add(new ObjectMethodCall(new QualifiedName(idnt4), "m", null, "", -1, -1, -1, -1));
    
    expectedAST.add(new ForStatement(init,
                                     new LessExpression(new QualifiedName(idnt1),new ObjectMethodCall(null, "sizeof", args, "", -1, -1, -1, -1)),
                                     updt,
                                     new BlockStatement(stmts)));
    verifyOutput("for( int i = 0; (i)<(sizeof(something)); (i)++ ){ ; ; o.m(); } ", expectedAST);
  }
  
  public void testBreakStatement() throws ParseException {
    expectedAST.add(new BreakStatement(null));
    verifyOutput("break;", expectedAST);
  }
  
  public void testContinueStatement() throws ParseException {
    expectedAST.add(new ContinueStatement(null));
    verifyOutput("continue;", expectedAST);
  }
  
  public void testReturnStatement() throws ParseException {
    expectedAST.add(new ReturnStatement(new BooleanLiteral(true)));
    verifyOutput("return true;", expectedAST);
  }
  
  public void testThrowStatement() throws ParseException {
    expectedAST.add(new ThrowStatement(new SimpleAllocation(new ReferenceType("RuntimeException"), null)));
    verifyOutput("throw new RuntimeException();", expectedAST);
  }
  
  public void testSynchronizedStatement() throws ParseException {
    List<IdentifierToken> idnt1  = new LinkedList<IdentifierToken>();
    idnt1.add(new Identifier("mutex"));
    
    List<IdentifierToken> idnt2  = new LinkedList<IdentifierToken>();
    idnt2.add(new Identifier("i"));
    
    List<IdentifierToken> idnt3 = new LinkedList<IdentifierToken>();
    idnt3.add(new Identifier("lock"));
    
    List<Node> stmts = new LinkedList<Node>();
    stmts.add(new EmptyStatement());
    stmts.add(new PostIncrement(new QualifiedName(idnt2)));
    stmts.add(new ObjectMethodCall(new QualifiedName(idnt3), "release", null, "", -1, -1, -1, -1));
    stmts.add(new EmptyStatement());
    
    expectedAST.add(new SynchronizedStatement(new QualifiedName(idnt1),
                                              new BlockStatement(stmts),
                                              "", -1, -1, -1, -1));
    verifyOutput(" synchronized (mutex) { ; (i)++; lock.release(); ;} ", expectedAST);
  }
  
//  This syntax (a try without a catch) is not allowed in dynamic java
//  ------
//  public void testTryStatement() throws ParseException {    
//    List<Node> stmts = new LinkedList<Node>();
//    stmts.add(new ThrowStatement(new SimpleAllocation(new ReferenceType("RuntimeException"), null)));
//
//    expectedAST.add(new TryStatement(new BlockStatement(stmts),
//                                     new LinkedList<Node> (),
//                                     null,
//                                     "", -1, -1, -1, -1));
//    
//    verifyOutput(" try{ throw new RuntimeException(); } ", expectedAST);                 
//  }
  
  public void testTryStatementWithCatchBlock() throws ParseException {
    List<Node> stmts1 = new LinkedList<Node>();
    stmts1.add(new ThrowStatement(new SimpleAllocation(new ReferenceType("RuntimeException"), null)));
    
    List<IdentifierToken> idnt  = new LinkedList<IdentifierToken>();
    idnt.add(new Identifier("ioe"));
    
    List<Node> stmts2 = new LinkedList<Node>();
    stmts2.add(new EmptyStatement());
    stmts2.add(new EmptyStatement());
    stmts2.add(new ThrowStatement(new QualifiedName(idnt)));

    List<Node> catchSt = new LinkedList<Node>();
    catchSt.add(new CatchStatement(new FormalParameter(false, new ReferenceType("IOException"), "ioe"), new BlockStatement(stmts2), "", -1, -1, -1, -1));
                
    expectedAST.add(new TryStatement(new BlockStatement(stmts1),
                                     catchSt,
                                     null,
                                     "", -1, -1, -1, -1));
  
    verifyOutput(" try{ throw new RuntimeException(); } catch (IOException ioe ){  ; ; throw ioe; }", expectedAST);                 
  }
  
  
  public void testConditionalExpression() throws ParseException {
    expectedAST.add(new ConditionalExpression(new BooleanLiteral(true), new BooleanLiteral(false), new BooleanLiteral(true)));
    
    verifyOutput( "((true)?(false):(true))", expectedAST);
  }
  
  public void testInstanceOfExpression() throws ParseException {
    List<IdentifierToken> idnt  = new LinkedList<IdentifierToken>();
    idnt.add(new Identifier("v"));
    
    expectedAST.add(new InstanceOfExpression(new QualifiedName(idnt), new ReferenceType("String")));
    verifyOutput( "((v) instanceof String)", expectedAST);
  }
  
  public void testCastExpression() throws ParseException {
    expectedAST.add(new CastExpression(new ReferenceType("String"), new SimpleAllocation(new ReferenceType("Object"), null)));
    verifyOutput( "((String) (new Object()))", expectedAST);
  }
  
  public void testQualifiedCastExpression() throws ParseException {
    expectedAST.add(new CastExpression(new ReferenceType("java.lang.String"), new SimpleAllocation(new ReferenceType("java.lang.Object"), null)));
    verifyOutput( "((java.lang.String) (new java.lang.Object()))", expectedAST);
  }
  
  public void testBinaryOpExpression() throws ParseException {
    List<IdentifierToken> idnt  = new LinkedList<IdentifierToken>();
    idnt.add(new Identifier("i"));
    
    expectedAST.add(new ShiftLeftExpression(new QualifiedName(idnt), new IntegerLiteral("5")));

    verifyOutput( " (i) << (5) ", expectedAST);
  }
  
  public void testNormalAssignment() throws ParseException {
    List<IdentifierToken> idnt  = new LinkedList<IdentifierToken>();
    idnt.add(new Identifier("i"));
    
    expectedAST.add(new SimpleAssignExpression(new QualifiedName(idnt), new IntegerLiteral("3")));
    
    verifyOutput( " (i = 3) ", expectedAST);
  }
  
  public void testCompoundAssignment() throws ParseException {
    List<IdentifierToken> idnt1  = new LinkedList<IdentifierToken>();
    idnt1.add(new Identifier("i"));

    List<IdentifierToken> idnt2  = new LinkedList<IdentifierToken>();
    idnt2.add(new Identifier("j"));

    expectedAST.add(new ExclusiveOrAssignExpression(new QualifiedName(idnt1), new QualifiedName(idnt2)));
    
    verifyOutput( " (i ^= j) ", expectedAST);
  }
  
  public void testPreIncrementExpression() throws ParseException {
    List<IdentifierToken> idnt  = new LinkedList<IdentifierToken>();
    idnt.add(new Identifier("x"));

    expectedAST.add(new PreIncrement(new QualifiedName(idnt)));
    
    verifyOutput( "++(x)", expectedAST);
  }
  
  public void testPostDecrementExpression() throws ParseException {
    List<IdentifierToken> idnt  = new LinkedList<IdentifierToken>();
    idnt.add(new Identifier("l"));

    expectedAST.add(new PostDecrement(new QualifiedName(idnt)));
    
    verifyOutput( "(l)--", expectedAST);
  }
  
  public void testIntegerLiteral() throws ParseException {
    expectedAST.add(new IntegerLiteral("3593"));
    
    verifyOutput( "3593", expectedAST);
  }
  
  public void testStringLiteral() throws ParseException {
    expectedAST.add(new StringLiteral("\"big time\""));
    
    verifyOutput( " \"big time\" ", expectedAST);
  }
  
  public void testArrayAllocationExpression() throws ParseException {
    List<IdentifierToken> idnt  = new LinkedList<IdentifierToken>();
    idnt.add(new Identifier("foo"));
    
    List<Expression> sizes = new LinkedList<Expression> ();
    sizes.add(new QualifiedName(idnt));
    
    expectedAST.add(new ArrayAllocation(new IntType(), new ArrayAllocation.TypeDescriptor(sizes, 1, null, -1, -1)));
    
    verifyOutput( "new int[foo]", expectedAST);
  }
  
  public void testArrayAllocationExpression2() throws ParseException {
    List<Expression> cells = new LinkedList<Expression> ();
    cells.add(new IntegerLiteral("0"));
    cells.add(new IntegerLiteral("1"));
    cells.add(new IntegerLiteral("2"));
    cells.add(new IntegerLiteral("3"));
    
    expectedAST.add(new ArrayAllocation(new IntType(), new ArrayAllocation.TypeDescriptor(new LinkedList<Expression>(), 1, new ArrayInitializer(cells), -1, -1)));

    verifyOutput( "new int[]{ 0,1,2,3 }", expectedAST);
  }
  
  public void testInstanceAllocationExpression() throws ParseException {
    List<Expression> args = new LinkedList<Expression> ();
    args.add(new IntegerLiteral("3"));
    
    expectedAST.add(new SimpleAllocation(new ReferenceType("C"), args)); 
                                         
    verifyOutput( " new C(3) ", expectedAST);
  }
  
  public void testInnerInstanceAllocationExpression() throws ParseException {
    List<IdentifierToken> idnt  = new LinkedList<IdentifierToken>();
    idnt.add(new Identifier("list"));
    
    List<Expression> args = new LinkedList<Expression>();
    args.add(new IntegerLiteral("3"));
    
    expectedAST.add(new InnerAllocation(new QualifiedName(idnt), new ReferenceType("Iterator"), args)); 
    verifyOutput( "list.new Iterator(3)", expectedAST);
  }
  
  public void testAnonymousInnerClass() throws ParseException {
    List<Node> members = new LinkedList<Node>();
    int accessFlags = 0;
    List<FormalParameter> params = new LinkedList<FormalParameter>();
    List<? extends ReferenceType> excepts = new LinkedList<ReferenceType>();
    List<Node> stmts = new LinkedList<Node>();
    List<IdentifierToken> idnt1  = new LinkedList<IdentifierToken>();
    idnt1.add(new Identifier("o"));
    stmts.add(new ObjectMethodCall(new QualifiedName(idnt1), "n", null, "", -1, -1, -1, -1));
    BlockStatement body = new BlockStatement(stmts);
    
    members.add(new MethodDeclaration(accessFlags, new VoidType(), "m", params, excepts, body));
    
    expectedAST.add(new ClassAllocation(new ReferenceType("C"), null, members)); 
    verifyOutput(" new C() { void m() { o.n(); } }", expectedAST);
  }
  
  public void testName() throws ParseException {
    List<IdentifierToken> idnt = new LinkedList<IdentifierToken>();
    idnt.add(new Identifier("g"));
    expectedAST.add( new QualifiedName(idnt));
    verifyOutput("g", expectedAST);
  }
  
  public void testQualifiedNameClassField() throws ParseException {
    expectedAST.add( new TypeExpression(new ReferenceType("java.awt.event.ActionEvent")));
    verifyOutput("java.awt.event.ActionEvent.class", expectedAST);
  }
  
  public void testQualifiedNameClass() throws ParseException {
    List<IdentifierToken> idnt = new LinkedList<IdentifierToken>();
    idnt.add(new Identifier("X"));
    idnt.add(new Identifier("java"));
    idnt.add(new Identifier("awt"));
    idnt.add(new Identifier("event"));
    idnt.add(new Identifier("ActionEvent"));
    
    expectedAST.add(new ObjectFieldAccess(new QualifiedName(idnt), "x"));
    verifyOutput("X.java.awt.event.ActionEvent.x", expectedAST);
  }
  
  public void testQualifiedNameFieldAccess() throws ParseException {
    List<IdentifierToken> idnt = new LinkedList<IdentifierToken>();
    idnt.add(new Identifier("java"));
    idnt.add(new Identifier("awt"));
    idnt.add(new Identifier("event"));
    idnt.add(new Identifier("ActionEvent"));

    expectedAST.add(new ObjectFieldAccess(new QualifiedName(idnt), "x"));
    verifyOutput("java.awt.event.ActionEvent.x", expectedAST);
  }
  
  public void testDotThis() throws ParseException {
    List<IdentifierToken> ident = new LinkedList<IdentifierToken>();
    ident.add(new Identifier("List"));
    expectedAST.add(new ThisExpression(ident, null, -1, -1, -1, -1));
    verifyOutput("List.this", expectedAST);
  }
  
  public void testDotClass() throws ParseException {
    expectedAST.add(new TypeExpression(new ReferenceType("List")));
    verifyOutput("List.class", expectedAST);
  }
  
  public void testArrayAccess() throws ParseException {
    List<IdentifierToken> ident = new LinkedList<IdentifierToken>();
    ident.add(new Identifier("v"));
    expectedAST.add(new ArrayAccess(new QualifiedName(ident), new IntegerLiteral("5")));
    verifyOutput("v[5]", expectedAST);
  }
  
  public void testMemberAccess()  throws ParseException {
    List<IdentifierToken> idnt = new LinkedList<IdentifierToken>();
    idnt.add(new Identifier("r"));

    expectedAST.add(new ObjectFieldAccess(new QualifiedName(idnt), "f"));
    verifyOutput("r.f", expectedAST);
  }
  
  public void testSuperMemberAccess()  throws ParseException {
    expectedAST.add(new SuperFieldAccess("d"));
    verifyOutput("super.d", expectedAST);
  }
  
  public void testMethodInvocation() throws ParseException {
    List<IdentifierToken> ident = new LinkedList<IdentifierToken>();
    ident.add(new Identifier("e"));
    
    List<Expression> args = new LinkedList<Expression>();
    args.add(new SimpleAllocation(new ReferenceType("Object"), null));
    args.add(new IntegerLiteral("5"));
    args.add(new BooleanLiteral(false));
    expectedAST.add(new ObjectMethodCall(new QualifiedName(ident),"meth", args, null, -1, -1, -1, -1));
    verifyOutput("e.meth(new Object(), 5, false)", expectedAST);
  }
  
  public void testImplicitMethodInvocation() throws ParseException {
    List<Expression> args = new LinkedList<Expression>();
    args.add(new IntegerLiteral("5"));
    expectedAST.add(new ObjectMethodCall(null,"meth", args, null, -1, -1, -1, -1));
    verifyOutput("meth(5)", expectedAST);
  }  

  
//  /** Multiple input tests. */
//  
//  public void testMultipleInputs0() throws ParseException {
//   //System.err.println(getInteractionsSource(getParser("int x = 0; int y = 1; int z = 2;").InteractionsInput()));
//    verifyOutput("int x = 0; int y = 1; int z = 2;");
//  }
//  
//  public void testMultipleInputs1() throws ParseException {
//    verifyOutput("o.m(); int x = 5; (x) + (y)");
//  }
//  
//  public void testMultipleInputs2() throws ParseException {
//    verifyOutput("package p.q; import p.q.r.*; o.m(); o.m()");
//  }
//  
//  public void testMultipleInputs3() throws ParseException {
//    verifyOutput("int x=5;\nSystem.out.println(x);");
//  }
//  
//  /** 
//   * Syntax error tests. Each test calls "verifyError" will an erroneous
//   * input and the coordinates of the error 
//   * (startLine, startColumn, endLine, endColumn).
//   */
//  
//  public void testBadVarDeclaration() {
//    verifyError("o.m(); int x 5;", 1, 8, 1, 10);
//  }
//  
//  public void testMismatchedBrace() {
//    verifyError("}  {", 1, 1, 1, 1);
//  }
//  
//  public void testTrailingGarbage() {
//    verifyError("int x = 5; :", 1, 12, 1, 12);
//  }
//  
//  public void testBadAssignment() {
//    verifyError("foo = !", 1, 7, 1, 7);
//  }
//  
//  public void testUnmatchedBrace() {
//    verifyError("{int foo; new Object().toString()", 1, 1, 1, 1);
//  }
//  
//  public void testMissingSemicolon() {
//    verifyError("package p.q import p.q.r.*;", 1, 1, 1, 7);
//  }
//  
//  /**
//   * Parses the given String and returns the resulting InteractionsInput.
//   * @param String the String to be interpreted
//   * @return the resulting InteractionsInput
//   */
//  private InteractionsInput _parseInteraction(String inputString) {
//    try {
//      return getParser(inputString).InteractionsInput();
//    }
//    catch (ParseException e) {
//      fail("The parser should not have thrown a ParseException!");
//      return null;
//    }
//  }
//  
//  /**
//   * Tests that the parser will accept statements without trailing semi-colons. The three
//   * statements that were causing problems before 8/28/03 were package, import, and variable
//   * declaration statements. These are the ones that were changed and are therefore being tested.
//   * Although not legal Java code, this allows DrJava's Interactions Pane to accept
//   * individual statements that don't have to end in semi-colons for simplicity's sake.
//   */
//  public void testNoNeedForTrailingSemicolon() {
//    _parseInteraction("package foo");
//    _parseInteraction("import java.io.*");
//    _parseInteraction("int i = 5");
//    try {
//      // We still do need the semi-colon for multiple statements.
//      // Don't call _parseInteraction since it won't throw ParseException.
//      getParser("import java.io.* import java.util.*").InteractionsInput();
//      fail("The parser should not have allowed multiple statements not separated by a semi-colon!");
//    }
//    catch (ParseException e) {
//      // we expect this, do nothing
//    }
//  }
//  
//  /**
//   * Runtime error tests. Each test will cause a RuntimeException to be thrown.
//   */
//  
//  public void testRepeatedAccessSpecifiers() {
//    try {
//      _parseInteraction("public public class ABC {}");
//      fail("The parser should have rejected this class definition!");
//    }
//    catch (RuntimeException e) {
//      // we expect this, do nothing
//    }
//  }
//  
//  public void testMultipleAccessSpecifiers() {
//    try {
//      _parseInteraction("public class ABC { public private int foo = 5;}");
//      fail("The parser should have rejected this class definition!");
//    }
//    catch (RuntimeException e) {
//      // we expect this, do nothing
//    }
//  }
//  
//  public void testRepeatedStaticModifiers() {
//    try {
//      _parseInteraction("static static void foo() {}");
//      fail("The parser should have rejected this method declaration!");
//    }
//    catch (RuntimeException e) {
//      // we expect this, do nothing
//    }
//  }
//  
//  public void testRepeatedFinalModifiers() {
//    try {
//      _parseInteraction("final static final void foo() {}");
//      fail("The parser should have rejected this method declaration!");
//    }
//    catch (RuntimeException e) {
//      // we expect this, do nothing
//    }
//  }
//}
//  
//  
//  
//  
//  
//  
//
//
//
//
//
//import junit.framework.*;
//import edu.rice.cs.javaast.parser.GJParser;
//import edu.rice.cs.javaast.tree.*;
//import java.io.StringReader;
//
///**
// * Tests the parser.
// * @version $Id$
// */
//public class ParserTest extends ASTTestCase {
//  public ParserTest(String name) {
//    super(name);
//  }
//
//  public static Test suite() {
//    return new TestSuite(ParserTest.class);
//  }
//
//  protected void setUp() {
//  }
//
//  public void testMultipleVariablesInForInit() throws Throwable {
//    ForInitI parsed = getParser("int i = 0, j = 0").ForInit();
//
//    assertEquals(FOR_INIT_I_AND_J, parsed);
//  }
//
//  public void testArrayTypeAsTypeArgument() throws Throwable {
//    Type parsed = getParser("Map<String,String[]>").Type();
//
//    assertEquals(MAP_STRING_TO_STRING_ARRAY, parsed);
//  }
//
//  public void testOctalCharLiterals() throws Throwable {
//    char[] cVals = {'\0', '\01', '\012', '\212', '\u0590' };
//    String[] sVals = {"0", "01", "012", "212", "u0590" };
//
//    for (int i = 0; i < cVals.length; i++) {
//      String str = "'\\" + sVals[i] + "'";
//      assertEquals(new CharLiteral(NULL_INFO, cVals[i]), getParser(str).Literal());
//    }
//  }
//
//  public void testFloatLiterals() throws Throwable {
//    float[] vals = {
//      0f, 1f, Float.MAX_VALUE, Float.MIN_VALUE
//    };
//
//    for (int i = 0; i < vals.length; i++) {
//      String str = String.valueOf(vals[i] + "f");
//      FloatLiteral right = new FloatLiteral(NULL_INFO, vals[i]);
//      assertEquals(right, getParser(str).Expression());
//    }
//  }
//
//  public void testDoubleLiterals() throws Throwable {
//    double[] vals = {
//      0d, 1d, Double.MAX_VALUE, Double.MIN_VALUE
//    };
//
//    for (int i = 0; i < vals.length; i++) {
//      String str = String.valueOf(vals[i] + "d");
//      DoubleLiteral right = new DoubleLiteral(NULL_INFO, vals[i]);
//      assertEquals(right, getParser(str).Expression());
//    }
//  }
//
//  public void testIntegerLiterals() throws Throwable {
//    int[] vals = {
//      0, 1, Integer.MAX_VALUE, Integer.MIN_VALUE
//    };
//
//    for (int i = 0; i < vals.length; i++) {
//      String str = String.valueOf(vals[i]);
//      IntegerLiteral right = new IntegerLiteral(NULL_INFO, vals[i]);
//      assertEquals(right, getParser(str).Expression());
//    }
//
//    String str = "0xCAFEBABE";
//    assertEquals(new IntegerLiteral(NULL_INFO, 0xCAFEBABE), getParser(str).Expression());
//  }
//
//  public void testLongLiterals() throws Throwable {
//    long[] vals = {
//      0L, 1L, Long.MAX_VALUE, Long.MIN_VALUE
//    };
//
//    for (int i = 0; i < vals.length; i++) {
//      String str = String.valueOf(vals[i] + "L");
//
//      LongLiteral right = new LongLiteral(NULL_INFO, vals[i]);
//      assertEquals(right, getParser(str).Expression());
//    }
//  }
//
//  public void testPrimitiveDotClass() throws Throwable {
//    getParser("byte.class").Expression();
//  }
}
