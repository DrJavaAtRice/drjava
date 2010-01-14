/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package koala.dynamicjava.parser.wrapper;

import junit.framework.*;

import koala.dynamicjava.parser.impl.*;
import koala.dynamicjava.tree.*;

import java.io.File;
import java.io.StringReader;
import java.util.*;

import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.Pair;

import static koala.dynamicjava.tree.ModifierSet.Modifier.*;

/**
 * Tests the interactions parser.
 * @version $Id$
 */
public class ParserTest extends TestCase {
  
  List<Node> expectedAST;
  
  public ParserTest( String name ) {
    super(name);
  }
  
  public static void verifyOutput( String inputString, List<Node> expectedAST ) throws ParseException {
    List<Node> ast = new Parser(new StringReader(inputString)).parseStream();
    assertEquals( "Parsed input AST does not match expected output AST", 
                   expectedAST.toString(),
                   ast.toString() );

  }
  
  public static void verifyExprOutput(String inputString, Expression expectedAST) throws ParseException {
    List<Node> expected = Collections.<Node>singletonList(new ExpressionStatement(expectedAST, false));
    verifyOutput(inputString, expected);
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
    SourceInfo si = new ParseError(ex, (File) null).getSourceInfo();
    assertEquals("Wrong begin line of error", startLine, si.getStartLine());
    assertEquals("Wrong begin column of error", startColumn, si.getStartColumn());
    assertEquals("Wrong end line of error", endLine, si.getEndLine());
    assertEquals("Wrong end column of error", endColumn, si.getEndColumn());
  }

  protected void setUp(){
    expectedAST = new LinkedList<Node>();
  }
  
  public void testPackageDeclaration() throws ParseException {
    expectedAST.add(new PackageDeclaration(ModifierSet.make(), "edu.rice.cs.javaast", SourceInfo.NONE));
    verifyOutput("package edu.rice.cs.javaast;", expectedAST);
  }  
  
  public void testPackageImportDeclaration() throws ParseException {
    expectedAST.add(new ImportDeclaration("java.io", true, false, SourceInfo.NONE));
    verifyOutput("import java.io.*;", expectedAST);
  }  
  
  public void testStaticImportDeclaration() throws ParseException {
    expectedAST.add(new ImportDeclaration("java.io", true, true, SourceInfo.NONE));
    verifyOutput("import static java.io.*;", expectedAST);
  }  
  
  public void testClassImportDeclaration() throws ParseException {
    expectedAST.add(new ImportDeclaration("java.lang.String", false, false, SourceInfo.NONE));
    verifyOutput("import java.lang.String;", expectedAST);
  }  
  
  public void testClassDeclaration() throws ParseException {
    ModifierSet mods = ModifierSet.make(PUBLIC, ABSTRACT);
    List<Node> body = new LinkedList<Node>();
    expectedAST.add(new ClassDeclaration(mods, "Foo", new ReferenceTypeName("Bar"), null, body, SourceInfo.NONE));
    verifyOutput("public abstract class Foo extends Bar{}", expectedAST);
  }
  
  public void testInterfaceDeclaration() throws ParseException {
    List<Node> body = new LinkedList<Node>();
    expectedAST.add(new InterfaceDeclaration(ModifierSet.make(), false, "i", null, body, SourceInfo.NONE));
    verifyOutput("interface i{}", expectedAST);
  }
  
  public void testMethodDeclaration() throws ParseException {
    ModifierSet mods = ModifierSet.make(STRICT, PUBLIC);
    List<FormalParameter> params = new LinkedList<FormalParameter>();
    List<? extends ReferenceTypeName> excepts = new LinkedList<ReferenceTypeName>();
    List<Node> stmts = new LinkedList<Node>();
    stmts.add(new ReturnStatement(new IntegerLiteral("1")));
    BlockStatement body = new BlockStatement(stmts);
    
    expectedAST.add(new MethodDeclaration(mods, new IntTypeName(), "getCount", params, excepts, body));
    
    verifyOutput("strictfp public int getCount(){ return 1; }", expectedAST);
  }
  
  public void testLocalVariableDeclarationList() throws ParseException {
    VariableDeclaration iVD = new VariableDeclaration(ModifierSet.make(),
                                                      new IntTypeName(), "i", new IntegerLiteral("0"));
    VariableDeclaration jVD = new VariableDeclaration(ModifierSet.make(),
                                                      new IntTypeName(), "j", null);
    VariableDeclaration kVD = new VariableDeclaration(ModifierSet.make(),
                                                      new IntTypeName(), "k", new IntegerLiteral("2"));
  
    expectedAST.add(iVD);
    expectedAST.add(jVD);
    expectedAST.add(kVD);
    
    verifyOutput("int i=0, j, k=2;", expectedAST);
  }
  
  private Annotation makeAnnotation(String name, String[] argNames, Expression... argVals) {
    if (argNames.length != argVals.length) { throw new IllegalArgumentException("Mistmatched name/value pairs"); }
    ReferenceTypeName type = new ReferenceTypeName(name);
    List<Pair<String, Expression>> vals =
      CollectUtil.makeList(IterUtil.zip(IterUtil.make(argNames), IterUtil.make(argVals)));
    return new Annotation(type, vals);
  }
  
  public void testMarkerAnnotation() throws ParseException {
    Annotation a = makeAnnotation("Marker", new String[0]);
    expectedAST.add(new VariableDeclaration(ModifierSet.make(a), new IntTypeName(), "x", null));
    verifyOutput("@Marker int x;", expectedAST);
  }

  public void testSingleValueAnnotation() throws ParseException {
    Annotation a = makeAnnotation("Single", new String[]{"value"}, new IntegerLiteral("23"));
    expectedAST.add(new VariableDeclaration(ModifierSet.make(a), new IntTypeName(), "x", null));
    verifyOutput("@Single(23) int x;", expectedAST);
  }

  public void testArrayValueAnnotation() throws ParseException {
    List<Expression> cells = Arrays.<Expression>asList(new IntegerLiteral("1"),
                                                       new IntegerLiteral("2"),
                                                       new IntegerLiteral("3"));
    Annotation a = makeAnnotation("Single", new String[]{"value"}, new ArrayInitializer(cells));
    expectedAST.add(new VariableDeclaration(ModifierSet.make(a), new IntTypeName(), "x", null));
    verifyOutput("@Single({1, 2, 3}) int x;", expectedAST);
  }

  public void testArrayValueWithCommaAnnotation() throws ParseException {
    List<Expression> cells = Arrays.<Expression>asList(new IntegerLiteral("1"),
                                                       new IntegerLiteral("2"),
                                                       new IntegerLiteral("3"));
    Annotation a = makeAnnotation("Single", new String[]{"value"}, new ArrayInitializer(cells));
    expectedAST.add(new VariableDeclaration(ModifierSet.make(a), new IntTypeName(), "x", null));
    verifyOutput("@Single({1, 2, 3, }) int x;", expectedAST);
  }

  public void testEmptyArrayValueAnnotation() throws ParseException {
    List<Expression> cells = Collections.emptyList();
    Annotation a = makeAnnotation("Single", new String[]{"value"}, new ArrayInitializer(cells));
    expectedAST.add(new VariableDeclaration(ModifierSet.make(a), new IntTypeName(), "x", null));
    verifyOutput("@Single({}) int x;", expectedAST);
  }

  public void testAnnotationValueAnnotation() throws ParseException {
    Annotation a = makeAnnotation("Single", new String[]{"value"}, new IntegerLiteral("23"));
    Annotation b = makeAnnotation("Wrapper", new String[]{"value"}, a);
    expectedAST.add(new VariableDeclaration(ModifierSet.make(b), new IntTypeName(), "x", null));
    verifyOutput("@Wrapper(@Single(23)) int x;", expectedAST);
  }

  public void testNormalAnnotation() throws ParseException {
    Annotation a = makeAnnotation("Normal", new String[]{"a", "b", "c"},
                                  new StringLiteral("\"foo\""),
                                  new StringLiteral("\"bar\""),
                                  new IntegerLiteral("15"));
    expectedAST.add(new VariableDeclaration(ModifierSet.make(a), new IntTypeName(), "x", null));
    verifyOutput("@Normal(a=\"foo\", b=\"bar\", c=15) int x;", expectedAST);
  }

  public void testLabeledStatement() throws ParseException {
    
    expectedAST.add(new LabeledStatement("v", new ExpressionStatement(new SimpleAllocation(new ReferenceTypeName("Object"), null), true)));
    
    verifyOutput("v : new Object();", expectedAST);
  } 
  
  public void testBlock() throws ParseException {
    List<Node> stmts = new LinkedList<Node> ();
    stmts.add(new ExpressionStatement(new SimpleAssignExpression(new AmbiguousName("i"),new IntegerLiteral("3")), true));
    stmts.add(new ExpressionStatement(new SimpleAssignExpression(new AmbiguousName("v"),new SimpleAllocation(new ReferenceTypeName("Vector"), null)), true));
    stmts.add(new ExpressionStatement(new PostDecrement(new AmbiguousName("i")), true));
    expectedAST.add(new BlockStatement(stmts));
    
    verifyOutput("{ (i=3); (v = new Vector()); (i)--; }", expectedAST);
  }
  
  public void testBlockWithEmptyStatements() throws ParseException {
    List<Node> stmts = new LinkedList<Node> ();
    stmts.add(new ExpressionStatement(new SimpleAssignExpression(new AmbiguousName("i"),new IntegerLiteral("3")), true));
    stmts.add(new EmptyStatement());
    stmts.add(new ExpressionStatement(new SimpleAssignExpression(new AmbiguousName("v"),new SimpleAllocation(new ReferenceTypeName("Vector"), null)), true));
    stmts.add(new ExpressionStatement(new PostDecrement(new AmbiguousName("i")), true));
    stmts.add(new EmptyStatement());
    expectedAST.add(new BlockStatement(stmts));

    verifyOutput("{ (i=3);; (v = new Vector()); (i)--;; }", expectedAST);
  }
  
  public void testEmptyStatement() throws ParseException {
    expectedAST.add(new EmptyStatement());
    verifyOutput(";", expectedAST);
  }
  
  public void testStatementExpression() throws ParseException {
    expectedAST.add(new ExpressionStatement(new ObjectMethodCall(new AmbiguousName("o"), "m", null, SourceInfo.NONE), true));
    verifyOutput("o.m();", expectedAST);
  }  
  
  public void testStatementExpression2() throws ParseException {
    List<Expression> args = new LinkedList<Expression> ();
    args.add(new IntegerLiteral("0"));
    expectedAST.add(new ExpressionStatement(new ObjectMethodCall(new SimpleAllocation(new ReferenceTypeName("Integer"), args), "intValue", null, SourceInfo.NONE), true));
    verifyOutput("(new Integer(0)).intValue();", expectedAST);
  }
  
  public void testSwitchStatement() throws ParseException {
    List<SwitchBlock> cases = new LinkedList<SwitchBlock>();
    
    List<Node> stmts1 = new LinkedList<Node> ();
    stmts1.add(new ExpressionStatement(new ObjectMethodCall(new AmbiguousName("o"), "m", null, SourceInfo.NONE), true));
    stmts1.add(new BreakStatement(null));
                 
    List<Node> stmts2 = new LinkedList<Node>();
    stmts2.add(new EmptyStatement());
    
    List<Node> stmts3 = new LinkedList<Node>();
    stmts3.add(new BreakStatement(null));
    
    cases.add(new SwitchBlock(new IntegerLiteral("0"), stmts1));
    cases.add(new SwitchBlock(new IntegerLiteral("1"), stmts2));
    cases.add(new SwitchBlock(new IntegerLiteral("2"), stmts3));    
    
    expectedAST.add(new SwitchStatement(new AmbiguousName("i"), cases, SourceInfo.NONE));
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
    
    expectedAST.add(new IfThenElseStatement(new ObjectMethodCall(new AmbiguousName("o"), "m", null, SourceInfo.NONE), 
                                            new BlockStatement(stmts), 
                                            new IfThenStatement(new BooleanLiteral(true),new ExpressionStatement(new ObjectMethodCall(new AmbiguousName("o"), "m", null, SourceInfo.NONE), true))));
    verifyOutput("if (o.m()) { ; } else if (true) o.m();", expectedAST);
  }
  
  public void testIfStatement3() throws ParseException {
    expectedAST.add(new IfThenStatement(new AmbiguousName("SomeClass", "CONSTANT"),
                                        new IfThenElseStatement(new ObjectMethodCall(new AmbiguousName("o"), "m", null, SourceInfo.NONE), 
                                                                new ReturnStatement(new BooleanLiteral(true)), 
                                                                new ReturnStatement(new BooleanLiteral(false)))));
    verifyOutput("if ( SomeClass.CONSTANT ) if ( o.m() ) return true; else return false; ", expectedAST);
  }
  
  public void testWhileStatement() throws ParseException {
    List<Expression> args = new LinkedList<Expression> ();
    args.add(new StringLiteral("\"Infinite Loop\""));
    
    List<Node> stmts = new LinkedList<Node> ();
    stmts.add(new ExpressionStatement(new ObjectMethodCall(new AmbiguousName("System", "out"), "println", args, SourceInfo.NONE), true));
    
    expectedAST.add(new WhileStatement(new BooleanLiteral(true),new BlockStatement(stmts)));
    verifyOutput("while (true) { System.out.println(\"Infinite Loop\"); }", expectedAST);
  }
  
  public void testDoStatement() throws ParseException {
    List<Node> stmts = new LinkedList<Node> ();
    stmts.add(new ExpressionStatement(new PostDecrement(new AmbiguousName("i")), true));
    stmts.add(new EmptyStatement());
       
    expectedAST.add(new DoStatement(new GreaterOrEqualExpression(new AmbiguousName("i"), new IntegerLiteral("0")), new BlockStatement(stmts)));
    verifyOutput("do {(i)--; ; } while ( (i) >= (0) );", expectedAST);
  }
  
  public void testForStatement() throws ParseException {
    List<Expression> args = new LinkedList<Expression> ();
    args.add(new AmbiguousName("something"));
    
    List<Node> init = new LinkedList<Node> ();
    init.add(new VariableDeclaration(ModifierSet.make(), new IntTypeName(), "i", new IntegerLiteral("0")));
    
    List<Node> updt = new LinkedList<Node> ();
    updt.add(new ExpressionStatement(new PostIncrement(new AmbiguousName("i")), true));
    
    List<Node> stmts = new LinkedList<Node> ();
    stmts.add(new EmptyStatement());
    stmts.add(new EmptyStatement());
    stmts.add(new ExpressionStatement(new ObjectMethodCall(new AmbiguousName("o"), "m", null, SourceInfo.NONE), true));
    
    expectedAST.add(new ForStatement(init,
                                     new LessExpression(new AmbiguousName("i"),new SimpleMethodCall("sizeof", args, SourceInfo.NONE)),
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
    expectedAST.add(new ThrowStatement(new SimpleAllocation(new ReferenceTypeName("RuntimeException"), null)));
    verifyOutput("throw new RuntimeException();", expectedAST);
  }
  
  public void testSynchronizedStatement() throws ParseException {
    List<Node> stmts = new LinkedList<Node>();
    stmts.add(new EmptyStatement());
    stmts.add(new ExpressionStatement(new PostIncrement(new AmbiguousName("i")), true));
    stmts.add(new ExpressionStatement(new ObjectMethodCall(new AmbiguousName("lock"), "release", null, SourceInfo.NONE), true));
    stmts.add(new EmptyStatement());
    
    expectedAST.add(new SynchronizedStatement(new AmbiguousName("mutex"),
                                              new BlockStatement(stmts),
                                              SourceInfo.NONE));
    verifyOutput(" synchronized (mutex) { ; (i)++; lock.release(); ;} ", expectedAST);
  }
  
//  This syntax (a try without a catch) is not allowed in dynamicjava
//  And a try without a catch (or finally) is not allowed in regular java either!
//  ------
//  public void testTryStatement() throws ParseException {    
//    List<Node> stmts = new LinkedList<Node>();
//    stmts.add(new ThrowStatement(new SimpleAllocation(new ReferenceTypeName("RuntimeException"), null)));
//
//    expectedAST.add(new TryStatement(new BlockStatement(stmts),
//                                     new LinkedList<Node> (),
//                                     null,
//                                     SourceInfo.NONE));
//    
//    verifyOutput(" try{ throw new RuntimeException(); } ", expectedAST);                 
//  }
  
  public void testTryStatementWithCatchBlock() throws ParseException {
    List<Node> stmts1 = new LinkedList<Node>();
    stmts1.add(new ThrowStatement(new SimpleAllocation(new ReferenceTypeName("RuntimeException"), null)));
    
    List<Node> stmts2 = new LinkedList<Node>();
    stmts2.add(new EmptyStatement());
    stmts2.add(new EmptyStatement());
    stmts2.add(new ThrowStatement(new AmbiguousName("ioe")));

    List<CatchStatement> catchSt = new LinkedList<CatchStatement>();
    catchSt.add(new CatchStatement(new FormalParameter(ModifierSet.make(), new ReferenceTypeName("IOException"), "ioe"),
                                   new BlockStatement(stmts2), SourceInfo.NONE));
                
    expectedAST.add(new TryStatement(new BlockStatement(stmts1),
                                     catchSt,
                                     null,
                                     SourceInfo.NONE));
  
    verifyOutput(" try{ throw new RuntimeException(); } catch (IOException ioe ){  ; ; throw ioe; }", expectedAST);                 
  }
  
  /* * * EXPRESSIONS: * * */
  
  
  public void testConditionalExpression() throws ParseException {
    Expression expected = new ConditionalExpression(new BooleanLiteral(true), new BooleanLiteral(false), new BooleanLiteral(true));
    
    verifyExprOutput( "((true)?(false):(true))", expected);
  }
  
  public void testInstanceOfExpression() throws ParseException {
    Expression expected = new InstanceOfExpression(new AmbiguousName("v"), new ReferenceTypeName("String"));
    verifyExprOutput( "((v) instanceof String)", expected);
  }
  
  public void testCastExpression() throws ParseException {
    Expression expected = new CastExpression(new ReferenceTypeName("String"), new SimpleAllocation(new ReferenceTypeName("Object"), null));
    verifyExprOutput( "((String) (new Object()))", expected);
  }
  
  public void testQualifiedCastExpression() throws ParseException {
    Expression expected = new CastExpression(new ReferenceTypeName("java.lang.String"), new SimpleAllocation(new ReferenceTypeName("java.lang.Object"), null));
    verifyExprOutput( "((java.lang.String) (new java.lang.Object()))", expected);
  }
  
  public void testBinaryOpExpression() throws ParseException {
    Expression expected = new ShiftLeftExpression(new AmbiguousName("i"), new IntegerLiteral("5"));

    verifyExprOutput( " (i) << (5) ", expected);
  }
  
  public void testNormalAssignment() throws ParseException {
    Expression expected = new SimpleAssignExpression(new AmbiguousName("i"), new IntegerLiteral("3"));
    
    verifyExprOutput( " (i = 3) ", expected);
  }
  
  public void testCompoundAssignment() throws ParseException {
    Expression expected = new ExclusiveOrAssignExpression(new AmbiguousName("i"), new AmbiguousName("j"));
    
    verifyExprOutput( " (i ^= j) ", expected);
  }
  
  public void testPreIncrementExpression() throws ParseException {
    Expression expected = new PreIncrement(new AmbiguousName("x"));
    
    verifyExprOutput( "++(x)", expected);
  }
  
  public void testPostDecrementExpression() throws ParseException {
    Expression expected = new PostDecrement(new AmbiguousName("l"));
    
    verifyExprOutput( "(l)--", expected);
  }
  
  public void testIntegerLiteral() throws ParseException {
    Expression expected = new IntegerLiteral("3593");
    
    verifyExprOutput( "3593", expected);
  }
  
  public void testStringLiteral() throws ParseException {
    Expression expected = new StringLiteral("\"big time\"");
    
    verifyExprOutput( " \"big time\" ", expected);
  }
  
  public void testArrayAllocationExpression() throws ParseException {
    List<Expression> sizes = new LinkedList<Expression> ();
    sizes.add(new AmbiguousName("foo"));
    
    Expression expected = new ArrayAllocation(new IntTypeName(),
                                              new ArrayAllocation.TypeDescriptor(sizes, 1, null, SourceInfo.NONE));
    
    verifyExprOutput( "new int[foo]", expected);
  }
  
  public void testArrayAllocationExpression2() throws ParseException {
    List<Expression> cells = new LinkedList<Expression> ();
    cells.add(new IntegerLiteral("0"));
    cells.add(new IntegerLiteral("1"));
    cells.add(new IntegerLiteral("2"));
    cells.add(new IntegerLiteral("3"));
    
    Expression expected = new ArrayAllocation(new IntTypeName(),
                                              new ArrayAllocation.TypeDescriptor(new LinkedList<Expression>(), 1,
                                                                                 new ArrayInitializer(cells),
                                                                                 SourceInfo.NONE));

    verifyExprOutput( "new int[]{ 0,1,2,3 }", expected);
  }
  
  public void testInstanceAllocationExpression() throws ParseException {
    List<Expression> args = new LinkedList<Expression> ();
    args.add(new IntegerLiteral("3"));
    
    Expression expected = new SimpleAllocation(new ReferenceTypeName("C"), args); 
                                         
    verifyExprOutput( " new C(3) ", expected);
  }
  
  public void testInnerInstanceAllocationExpression() throws ParseException {
    List<Expression> args = new LinkedList<Expression>();
    args.add(new IntegerLiteral("3"));
    
    Expression expected = new InnerAllocation(new AmbiguousName("list"), "Iterator", Option.<List<TypeName>>none(), args); 
    verifyExprOutput( "list.new Iterator(3)", expected);
  }
  
  public void testAnonymousInnerClass() throws ParseException {
    List<Node> members = new LinkedList<Node>();
    List<FormalParameter> params = new LinkedList<FormalParameter>();
    List<? extends ReferenceTypeName> excepts = new LinkedList<ReferenceTypeName>();
    List<Node> stmts = new LinkedList<Node>();
    stmts.add(new ExpressionStatement(new ObjectMethodCall(new AmbiguousName("o"), "n", null, SourceInfo.NONE), true));
    BlockStatement body = new BlockStatement(stmts);
    
    members.add(new MethodDeclaration(ModifierSet.make(), new VoidTypeName(), "m", params, excepts, body));
    
    Expression expected = new AnonymousAllocation(new ReferenceTypeName("C"), null, members); 
    verifyExprOutput(" new C() { void m() { o.n(); } }", expected);
  }
  
  public void testName() throws ParseException {
    Expression expected = new AmbiguousName("g");
    verifyExprOutput("g", expected);
  }
  
  public void testQualifiedNameClassField() throws ParseException {
    Expression expected =  new TypeExpression(new ReferenceTypeName("java.awt.event.ActionEvent"));
    verifyExprOutput("java.awt.event.ActionEvent.class", expected);
  }
  
  public void testQualifiedNameClass() throws ParseException {
    List<IdentifierToken> idnt = new LinkedList<IdentifierToken>();
    idnt.add(new Identifier("X"));
    idnt.add(new Identifier("java"));
    idnt.add(new Identifier("awt"));
    idnt.add(new Identifier("event"));
    idnt.add(new Identifier("ActionEvent"));
    idnt.add(new Identifier("x"));
    
    Expression expected = new AmbiguousName(idnt);
    verifyExprOutput("X.java.awt.event.ActionEvent.x", expected);
  }
  
  public void testQualifiedNameFieldAccess() throws ParseException {
    List<IdentifierToken> idnt = new LinkedList<IdentifierToken>();
    idnt.add(new Identifier("java"));
    idnt.add(new Identifier("awt"));
    idnt.add(new Identifier("event"));
    idnt.add(new Identifier("ActionEvent"));
    idnt.add(new Identifier("x"));

    Expression expected = new AmbiguousName(idnt);
    verifyExprOutput("java.awt.event.ActionEvent.x", expected);
  }
  
  public void testDotThis() throws ParseException {
    Expression expected = new ThisExpression(Option.some("List"), SourceInfo.NONE);
    verifyExprOutput("List.this", expected);
  }
  
  public void testDotClass() throws ParseException {
    Expression expected = new TypeExpression(new ReferenceTypeName("List"));
    verifyExprOutput("List.class", expected);
  }
  
  public void testArrayAccess() throws ParseException {
    Expression expected = new ArrayAccess(new AmbiguousName("v"), new IntegerLiteral("5"));
    verifyExprOutput("v[5]", expected);
  }
  
  public void testMemberAccess()  throws ParseException {
    Expression expected = new AmbiguousName("r", "f");
    verifyExprOutput("r.f", expected);
  }
  
  public void testSuperMemberAccess()  throws ParseException {
    Expression expected = new SuperFieldAccess(Option.<String>none(), "d");
    verifyExprOutput("super.d", expected);
  }
  
  public void testMethodInvocation() throws ParseException {
    List<Expression> args = new LinkedList<Expression>();
    args.add(new SimpleAllocation(new ReferenceTypeName("Object"), null));
    args.add(new IntegerLiteral("5"));
    args.add(new BooleanLiteral(false));
    Expression expected = new ObjectMethodCall(new AmbiguousName("e"),"meth", args, SourceInfo.NONE);
    verifyExprOutput("e.meth(new Object(), 5, false)", expected);
  }
  
  public void testImplicitMethodInvocation() throws ParseException {
    List<Expression> args = new LinkedList<Expression>();
    args.add(new IntegerLiteral("5"));
    Expression expected = new SimpleMethodCall("meth", args, SourceInfo.NONE);
    verifyExprOutput("meth(5)", expected);
  }  

  
//  /** Multiple input tests. */
//  
//  public void testMultipleInputs0() throws ParseException {
//   //System.err.println(getInteractionsSource(getParser("int x = 0; int y = 1; int z = 2;").InteractionsInput()));
//    verifyOutput("int x = 0; int y = 1; int z = 2;");
//  }
//  
// public void testMultipleInputs1() throws ParseException {
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
//  public void testRepeatedAccesss() {
//    try {
//      _parseInteraction("public public class ABC {}");
//      fail("The parser should have rejected this class definition!");
//    }
//    catch (RuntimeException e) {
//      // we expect this, do nothing
//    }
//  }
//  
//  public void testMultipleAccesss() {
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
//    TypeName parsed = getParser("Map<String,String[]>").TypeName();
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
  
  
/*  public void testIntegerLiterals() throws ParseException{
    int[] values = {0,1,Integer.MAX_VALUE, Integer.MIN_VALUE};
    
    String str = "";
    Parser p;
    List<Node> statements;
    
    for(int i:values){
      str = String.valueOf(values[i]);
      p = new Parser(new StringReader(str));
      statements = p.parseStream();
      assertEquals("Parser failed to parse the int value correctly.", sizeExpected, actual);

    }
  */  
  
//  public void testIntegerLiterals() throws Throwable {
//    int[] vals = { 0, 1, Integer.MAX_VALUE, Integer.MIN_VALUE };
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
