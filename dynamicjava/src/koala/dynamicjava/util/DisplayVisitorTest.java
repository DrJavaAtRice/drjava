package koala.dynamicjava.util;

import koala.dynamicjava.interpreter.AbstractTypeChecker;
import koala.dynamicjava.interpreter.InterpreterException;
import koala.dynamicjava.interpreter.TreeInterpreter;
import koala.dynamicjava.interpreter.context.GlobalContext;
import koala.dynamicjava.parser.wrapper.JavaCCParserFactory;
import koala.dynamicjava.parser.wrapper.SourceCodeParser;
import koala.dynamicjava.tree.Node;
import koala.dynamicjava.tree.visitor.Visitor;

import java.io.*;
import java.util.List;
import java.lang.reflect.Type;

public class DisplayVisitorTest extends DynamicJavaTestCase {
  ////// Internal Initialization ////////////////////////

  /**
   * The global context we are using for the type checker.
   */
  private GlobalContext<Type> _globalContext;

  /**
   * The global context we are using for the name visitor.
   */
  private GlobalContext<Type> _globalNameContext;

  /**
   * The type checker we are testing.
   */
  private AbstractTypeChecker _typeChecker;

  /**
   * The interpreter we are using to test our modifications of the ASTs.
   */
  private TreeInterpreter _interpreter;

  private JavaCCParserFactory parserFactory;

  private static final String VERSION_KEY = "java.specification.version";

  private File _tempFile;
  /**
   * Sets up the tests for execution.
   */
  public void setUp() {
    // This test is dependent on 1.5 since the ObjectMethodCall uses 1.5 reflection methods.
    // If this were run in 1.4 and we faked the version property to 1.5, some methods would
    // not be found during the test and would cause the test case to fail.
//    String version = System.getProperty(VERSION_KEY);
//    if (Float.valueOf(version) < 1.5) {
//      throw new WrongVersionException("This test case requires Java 2 SDK v1.5.0 or better");
//    }
    //This test will fail if not running 1.5 OR 1.4 with jsr14
    setTigerEnabled(true);

    parserFactory = new JavaCCParserFactory();
    _globalContext = new GlobalContext<Type>(new TreeInterpreter(parserFactory));
    _globalContext.define("x", int.class);
    _globalContext.define("X", Integer.class);
    _globalContext.define("b", boolean.class);
    _globalContext.define("B", Boolean.class);
    _globalContext.define("I", int[].class);

    _globalNameContext = new GlobalContext<Type>(new TreeInterpreter(parserFactory));
    _globalNameContext.define("x", int.class);
    _globalNameContext.define("X", Integer.class);
    _globalNameContext.define("B", Boolean.class);
    _globalNameContext.define("b", boolean.class);


    //makeTypeChecker will return the correct type checker depending on the current runtime version of Java
    _typeChecker = AbstractTypeChecker.makeTypeChecker(_globalContext);
    _interpreter = new TreeInterpreter(parserFactory);

    try {
      _interpretText("int x = 0;");
      _interpretText("Integer X = new Integer(0);");
      _interpretText("Boolean B = Boolean.FALSE;");
      _interpretText("boolean b = false;");
      _interpretText("int[] I = {1, 2, 3};");
    }
    catch (InterpreterException ere) {
      fail("Should have been able to declare variables for interpreter.");
    }

    _tempFile = new File("temp.out~");
  }

  public void tearDown() {
    TigerUtilities.resetVersion();
    _tempFile.delete();
  }


  /**
   * Parses the given string and returns the list of Nodes.
   *
   * @param code the code to parse
   * @return the list of Nodes
   */
  private List<Node> _parseCode(String code) {
    SourceCodeParser parser = parserFactory.createParser(new java.io.StringReader(code), "");
    return parser.parseStream();
  }

  private Object _interpretText(String text) throws InterpreterException {
    List<Node> exps = _interpreter.buildStatementList(new java.io.StringReader(text), "Unit Test");
    return _interpreter.interpret(exps);
  }



  /**
   * Takes input of a file name and a string and compares the contents of the file to the contents of the string
   */
  private void compareFileToString(File fileO, String contents) {
    BufferedReader file = null, str = null;
    try {
      file = new BufferedReader(new FileReader(fileO));
      str = new BufferedReader(new StringReader(contents));
      String f, s;
      boolean retVal = true;
      while (retVal) {
        f = file.readLine();
        s = str.readLine();
        assertEquals("strings should match", s, f);
        if( s == null || f == null ) break;
      }
      file.close();
      str.close();
    }
    catch (Exception e) {
      try {
        file.close();
        str.close();
      }
      catch (Exception e1) {
      }
    }
  }

  /**
   * Test the display of do ... while loops
   */
  public void testDoStatement() {
    Visitor<Void> disp = null;
    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    String text = "do { } while( true );";
    Node stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 DoStatement {\n" +
                                      "condition:\n" +
                                      "  l.1 Literal (boolean) <true> {\n" +
                                      "  }\n" +
                                      "body:\n" +
                                      "  l.1 BlockStatement {\n" +
                                      "  statements:\n" +
                                      "  }\n" +
                                      "}");

    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }
    text = "do { System.out.println(\"hello out there\"); } while( i > 1 );";
    stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 DoStatement {\n" +
                                      "condition:\n" +
                                      "  l.1 koala.dynamicjava.tree.GreaterExpression {\n" +
                                      "  leftExpression:\n" +
                                      "    l.1 QualifiedName {\n" +
                                      "    representation:\n" +
                                      "      i\n" +
                                      "    }\n" +
                                      "  rightExpression:\n" +
                                      "    l.1 Literal (int) <1> {\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "body:\n" +
                                      "  l.1 BlockStatement {\n" +
                                      "  statements:\n" +
                                      "    l.1 ObjectMethodCall {\n" +
                                      "    expression:\n" +
                                      "      l.1 QualifiedName {\n" +
                                      "      representation:\n" +
                                      "        System.out\n" +
                                      "      }\n" +
                                      "    methodName:\n" +
                                      "      println\n" +
                                      "    arguments:\n" +
                                      "      l.1 Literal (class java.lang.String) <hello out there> {\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "}");
  }

  /**
   * Test the display of package statements
   */
  public void testPackageDeclaration() {
    Visitor<Void> disp = null;
    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    String text = "package edu.rice.cs.drjava;";
    Node stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 PackageDeclaration edu.rice.cs.drjava {\n}");
  }

  /**
   * Test the display of import statements
   */
  public void testImportDeclaration() {
    Visitor<Void> disp = null;
    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    String text = "import edu.rice.cs.drjava;";
    Node stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 ImportDeclaration edu.rice.cs.drjava {\n}");


    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }
    text = "import java.io.*;";
    stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 ImportDeclaration java.io.* {\n}");
  }

  /**
   * Test the display of do ... while loops
   */
  public void testWhileStatement() {
    Visitor<Void> disp = null;
    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    String text = "while ( a > b ) { System.out.println(\"hello world\"); b++; }";
    Node stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 WhileStatement {\n" +
                                      "condition:\n" +
                                      "  l.1 koala.dynamicjava.tree.GreaterExpression {\n" +
                                      "  leftExpression:\n" +
                                      "    l.1 QualifiedName {\n" +
                                      "    representation:\n" +
                                      "      a\n" +
                                      "    }\n" +
                                      "  rightExpression:\n" +
                                      "    l.1 QualifiedName {\n" +
                                      "    representation:\n" +
                                      "      b\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "body:\n" +
                                      "  l.1 BlockStatement {\n" +
                                      "  statements:\n" +
                                      "    l.1 ObjectMethodCall {\n" +
                                      "    expression:\n" +
                                      "      l.1 QualifiedName {\n" +
                                      "      representation:\n" +
                                      "        System.out\n" +
                                      "      }\n" +
                                      "    methodName:\n" +
                                      "      println\n" +
                                      "    arguments:\n" +
                                      "      l.1 Literal (class java.lang.String) <hello world> {\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "    l.1 koala.dynamicjava.tree.PostIncrement {\n" +
                                      "    expression:\n" +
                                      "      l.1 QualifiedName {\n" +
                                      "      representation:\n" +
                                      "        b\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "}");

    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }
    text = "while( true ) { }";
    stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 WhileStatement {\n" +
                                      "condition:\n" +
                                      "  l.1 Literal (boolean) <true> {\n" +
                                      "  }\n" +
                                      "body:\n" +
                                      "  l.1 BlockStatement {\n" +
                                      "  statements:\n" +
                                      "  }\n" +
                                      "}");
  }

  /**
   * Test the display of do ... while loops
   */
  public void testForStatement() {
    Visitor<Void> disp = null;
    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    String text = "for(int i = 0; i < 5; i++) for(int j = 0; j < i; j++) { System.out.println(i+j); }";
    Node stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 ForStatement {\n" +
                                      "initialization:\n" +
                                      "  l.1 VariableDeclaration {\n" +
                                      "  isFinal:\n" +
                                      "    false\n" +
                                      "  type:\n" +
                                      "    l.1 PrimitiveType <int>\n" +
                                      "  name:\n" +
                                      "    i\n" +
                                      "  initializer:\n" +
                                      "    l.1 Literal (int) <0> {\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "condition:\n" +
                                      "  l.1 koala.dynamicjava.tree.LessExpression {\n" +
                                      "  leftExpression:\n" +
                                      "    l.1 QualifiedName {\n" +
                                      "    representation:\n" +
                                      "      i\n" +
                                      "    }\n" +
                                      "  rightExpression:\n" +
                                      "    l.1 Literal (int) <5> {\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "update:\n" +
                                      "  l.1 koala.dynamicjava.tree.PostIncrement {\n" +
                                      "  expression:\n" +
                                      "    l.1 QualifiedName {\n" +
                                      "    representation:\n" +
                                      "      i\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "body:\n" +
                                      "  l.1 ForStatement {\n" +
                                      "  initialization:\n" +
                                      "    l.1 VariableDeclaration {\n" +
                                      "    isFinal:\n" +
                                      "      false\n" +
                                      "    type:\n" +
                                      "      l.1 PrimitiveType <int>\n" +
                                      "    name:\n" +
                                      "      j\n" +
                                      "    initializer:\n" +
                                      "      l.1 Literal (int) <0> {\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "  condition:\n" +
                                      "    l.1 koala.dynamicjava.tree.LessExpression {\n" +
                                      "    leftExpression:\n" +
                                      "      l.1 QualifiedName {\n" +
                                      "      representation:\n" +
                                      "        j\n" +
                                      "      }\n" +
                                      "    rightExpression:\n" +
                                      "      l.1 QualifiedName {\n" +
                                      "      representation:\n" +
                                      "        i\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "  update:\n" +
                                      "    l.1 koala.dynamicjava.tree.PostIncrement {\n" +
                                      "    expression:\n" +
                                      "      l.1 QualifiedName {\n" +
                                      "      representation:\n" +
                                      "        j\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "  body:\n" +
                                      "    l.1 BlockStatement {\n" +
                                      "    statements:\n" +
                                      "      l.1 ObjectMethodCall {\n" +
                                      "      expression:\n" +
                                      "        l.1 QualifiedName {\n" +
                                      "        representation:\n" +
                                      "          System.out\n" +
                                      "        }\n" +
                                      "      methodName:\n" +
                                      "        println\n" +
                                      "      arguments:\n" +
                                      "        l.1 koala.dynamicjava.tree.AddExpression {\n" +
                                      "        leftExpression:\n" +
                                      "          l.1 QualifiedName {\n" +
                                      "          representation:\n" +
                                      "            i\n" +
                                      "          }\n" +
                                      "        rightExpression:\n" +
                                      "          l.1 QualifiedName {\n" +
                                      "          representation:\n" +
                                      "            j\n" +
                                      "          }\n" +
                                      "        }\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "}");


    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }
    text = "for(;;);";
    stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 ForStatement {\n" +
                                      "initialization:\n" +
                                      "condition:\n" +
                                      "update:\n" +
                                      "body:\n" +
                                      "  l.1 EmptyStatement {\n" +
                                      "  }\n" +
                                      "}");
  }

  /**
   * Test the display empty statements
   */
  public void testEmptyStatement() {
    Visitor<Void> disp = null;
    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    String text = ";";
    Node stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 EmptyStatement {\n}");
  }

  /**
   * Test switch statements
   */
  public void testSwitchStatement() {
    Visitor<Void> disp = null;
    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    String text = "switch( 5 ) { case 1: break; case 2: break; case 5: break; default: i++; }";
    Node stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 SwitchStatement {\n" +
                                      "selector:\n" +
                                      "  l.1 Literal (int) <5> {\n" +
                                      "  }\n" +
                                      "bindings:\n" +
                                      "  l.1 SwitchBlock {\n" +
                                      "  expression:\n" +
                                      "    l.1 Literal (int) <1> {\n" +
                                      "    }\n" +
                                      "  statements:\n" +
                                      "    l.1 BreakStatement {\n" +
                                      "    label:\n" +
                                      "      null\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "  l.1 SwitchBlock {\n" +
                                      "  expression:\n" +
                                      "    l.1 Literal (int) <2> {\n" +
                                      "    }\n" +
                                      "  statements:\n" +
                                      "    l.1 BreakStatement {\n" +
                                      "    label:\n" +
                                      "      null\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "  l.1 SwitchBlock {\n" +
                                      "  expression:\n" +
                                      "    l.1 Literal (int) <5> {\n" +
                                      "    }\n" +
                                      "  statements:\n" +
                                      "    l.1 BreakStatement {\n" +
                                      "    label:\n" +
                                      "      null\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "  l.1 SwitchBlock {\n" +
                                      "  expression:\n" +
                                      "    default\n" +
                                      "  statements:\n" +
                                      "    l.1 koala.dynamicjava.tree.PostIncrement {\n" +
                                      "    expression:\n" +
                                      "      l.1 QualifiedName {\n" +
                                      "      representation:\n" +
                                      "        i\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "}");

    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    text = "switch( 'a' ) { }";
    stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 SwitchStatement {\n" +
                                      "selector:\n" +
                                      "  l.1 Literal (char) <a> {\n" +
                                      "  }\n" +
                                      "bindings:\n" +
                                      "}");
  }

  /**
   * Test the display of labeled statements
   */
  public void testLabeledStatements() {
    Visitor<Void> disp = null;
    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    String text = "lbl: { System.out.println(1); }";
    Node stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 LabeledStatement {\n" +
                                      "label:\n" +
                                      "  lbl\n" +
                                      "statement:\n" +
                                      "  l.1 BlockStatement {\n" +
                                      "  statements:\n" +
                                      "    l.1 ObjectMethodCall {\n" +
                                      "    expression:\n" +
                                      "      l.1 QualifiedName {\n" +
                                      "      representation:\n" +
                                      "        System.out\n" +
                                      "      }\n" +
                                      "    methodName:\n" +
                                      "      println\n" +
                                      "    arguments:\n" +
                                      "      l.1 Literal (int) <1> {\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "}");
  }

  /**
   * Test the display of break statements
   */
  public void testBreakStatements() {
    Visitor<Void> disp = null;
    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    String text = "break;";
    Node stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 BreakStatement {\n" +
                                      "label:\n" +
                                      "  null\n" +
                                      "}");

    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    text = "break lbl;";
    stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 BreakStatement {\n" +
                                      "label:\n" +
                                      "  lbl\n" +
                                      "}");
  }

  /**
   * Tests the display of try statements
   */
  public void testTryStatements() {
    Visitor<Void> disp = null;
    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    String text = "try { throw new FileNotFoundException(\"file not found\"); } catch (FileNotFoundException e) { } finally { System.out.println(\"Finished\"); }";
    Node stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 TryStatement {\n" +
                                      "tryBlock:\n" +
                                      "  l.1 BlockStatement {\n" +
                                      "  statements:\n" +
                                      "    l.1 ThrowStatement {\n" +
                                      "    expression:\n" +
                                      "      l.1 SimpleAllocation {\n" +
                                      "      creationType:\n" +
                                      "        l.1 ReferenceType {\n" +
                                      "        representation:\n" +
                                      "          FileNotFoundException\n" +
                                      "        }\n" +
                                      "      arguments:\n" +
                                      "        l.1 Literal (class java.lang.String) <file not found> {\n" +
                                      "        }\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "catchStatements:\n" +
                                      "  l.1 CatchStatement {\n" +
                                      "  exception:\n" +
                                      "    l.1 FormalParameter {\n" +
                                      "    type:\n" +
                                      "      l.1 ReferenceType {\n" +
                                      "      representation:\n" +
                                      "        FileNotFoundException\n" +
                                      "      }\n" +
                                      "    name:\n" +
                                      "      e\n" +
                                      "    }\n" +
                                      "  block:\n" +
                                      "    l.1 BlockStatement {\n" +
                                      "    statements:\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "finallyBlock:\n" +
                                      "  l.1 BlockStatement {\n" +
                                      "  statements:\n" +
                                      "    l.1 ObjectMethodCall {\n" +
                                      "    expression:\n" +
                                      "      l.1 QualifiedName {\n" +
                                      "      representation:\n" +
                                      "        System.out\n" +
                                      "      }\n" +
                                      "    methodName:\n" +
                                      "      println\n" +
                                      "    arguments:\n" +
                                      "      l.1 Literal (class java.lang.String) <Finished> {\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "}");
  }

  /**
   * Test the display of a return statement
   */
  public void testReturnStatements() {
    Visitor<Void> disp = null;
    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    String text = "return;";
    Node stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 ReturnStatement {\n" +
                                      "expression:\n" +
                                      "  null\n" +
                                      "}");

    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    text = "return 1;";
    stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 ReturnStatement {\n" +
                                      "expression:\n" +
                                      "  l.1 Literal (int) <1> {\n" +
                                      "  }\n" +
                                      "}");
  }

  /**
   * Tests the display of synchronized statements
   */
  public void testSynchronizedStatements() {
    Visitor<Void> disp = null;
    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    String text = "synchronized (obj) { obj.update(); }";
    Node stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 SynchronizedStatement {\n" +
                                      "lock:\n" +
                                      "  l.1 QualifiedName {\n" +
                                      "  representation:\n" +
                                      "    obj\n" +
                                      "  }\n" +
                                      "body:\n" +
                                      "  l.1 BlockStatement {\n" +
                                      "  statements:\n" +
                                      "    l.1 ObjectMethodCall {\n" +
                                      "    expression:\n" +
                                      "      l.1 QualifiedName {\n" +
                                      "      representation:\n" +
                                      "        obj\n" +
                                      "      }\n" +
                                      "    methodName:\n" +
                                      "      update\n" +
                                      "    arguments:\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "}");
  }

  /**
   * Tests the display of continue statements
   */
  public void testContinueStatements() {
    Visitor<Void> disp = null;
    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    String text = "continue;";
    Node stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 ContinueStatement {\n" +
                                      "label:\n" +
                                      "  null\n" +
                                      "}");

    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    text = "continue lbl;";
    stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 ContinueStatement {\n" +
                                      "label:\n" +
                                      "  lbl\n" +
                                      "}");
  }

  /**
   * Tests the display of if-then and it-then-else statements
   */
  public void testIfStatements() {
    Visitor<Void> disp = null;
    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    String text = "if( a == b ) return a;";
    Node stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 IfThenStatement {\n" +
                                      "condition:\n" +
                                      "  l.1 koala.dynamicjava.tree.EqualExpression {\n" +
                                      "  leftExpression:\n" +
                                      "    l.1 QualifiedName {\n" +
                                      "    representation:\n" +
                                      "      a\n" +
                                      "    }\n" +
                                      "  rightExpression:\n" +
                                      "    l.1 QualifiedName {\n" +
                                      "    representation:\n" +
                                      "      b\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "thenStatement:\n" +
                                      "  l.1 ReturnStatement {\n" +
                                      "  expression:\n" +
                                      "    l.1 QualifiedName {\n" +
                                      "    representation:\n" +
                                      "      a\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "}");
    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    text = "if( a == b ) return a; else return b;";
    stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 IfThenElseStatement {\n" +
                                      "condition:\n" +
                                      "  l.1 koala.dynamicjava.tree.EqualExpression {\n" +
                                      "  leftExpression:\n" +
                                      "    l.1 QualifiedName {\n" +
                                      "    representation:\n" +
                                      "      a\n" +
                                      "    }\n" +
                                      "  rightExpression:\n" +
                                      "    l.1 QualifiedName {\n" +
                                      "    representation:\n" +
                                      "      b\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "thenStatement:\n" +
                                      "  l.1 ReturnStatement {\n" +
                                      "  expression:\n" +
                                      "    l.1 QualifiedName {\n" +
                                      "    representation:\n" +
                                      "      a\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "elseStatement:\n" +
                                      "  l.1 ReturnStatement {\n" +
                                      "  expression:\n" +
                                      "    l.1 QualifiedName {\n" +
                                      "    representation:\n" +
                                      "      b\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "}");

    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    text = "if( a == b ) return a; else if( a > b ) return b; else return a-b;";
    stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 IfThenElseStatement {\n" +
                                      "condition:\n" +
                                      "  l.1 koala.dynamicjava.tree.EqualExpression {\n" +
                                      "  leftExpression:\n" +
                                      "    l.1 QualifiedName {\n" +
                                      "    representation:\n" +
                                      "      a\n" +
                                      "    }\n" +
                                      "  rightExpression:\n" +
                                      "    l.1 QualifiedName {\n" +
                                      "    representation:\n" +
                                      "      b\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "thenStatement:\n" +
                                      "  l.1 ReturnStatement {\n" +
                                      "  expression:\n" +
                                      "    l.1 QualifiedName {\n" +
                                      "    representation:\n" +
                                      "      a\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "elseStatement:\n" +
                                      "  l.1 IfThenElseStatement {\n" +
                                      "  condition:\n" +
                                      "    l.1 koala.dynamicjava.tree.GreaterExpression {\n" +
                                      "    leftExpression:\n" +
                                      "      l.1 QualifiedName {\n" +
                                      "      representation:\n" +
                                      "        a\n" +
                                      "      }\n" +
                                      "    rightExpression:\n" +
                                      "      l.1 QualifiedName {\n" +
                                      "      representation:\n" +
                                      "        b\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "  thenStatement:\n" +
                                      "    l.1 ReturnStatement {\n" +
                                      "    expression:\n" +
                                      "      l.1 QualifiedName {\n" +
                                      "      representation:\n" +
                                      "        b\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "  elseStatement:\n" +
                                      "    l.1 ReturnStatement {\n" +
                                      "    expression:\n" +
                                      "      l.1 koala.dynamicjava.tree.SubtractExpression {\n" +
                                      "      leftExpression:\n" +
                                      "        l.1 QualifiedName {\n" +
                                      "        representation:\n" +
                                      "          a\n" +
                                      "        }\n" +
                                      "      rightExpression:\n" +
                                      "        l.1 QualifiedName {\n" +
                                      "        representation:\n" +
                                      "          b\n" +
                                      "        }\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "}");
  }

  /**
   * A Test for some class constructs
   */
  public void testClass1() {
    Visitor<Void> disp = null;
    try {
      disp = new DisplayVisitor(new FileOutputStream(_tempFile, false));
    }
    catch (FileNotFoundException e) {
      fail("couldn't create file");
    }

    String text = "public class Tester extends Object implements Comparable {\n" +
                  "  private int _f1;\n" +
                  "  \n" +
                  "  public static final Tester DEF_INSTANCE = new Tester(0); \n" +
                  "  \n" +
                  "  public Tester() {\n" +
                  "  }\n" +
                  "  /**\n" +
                  "   * Create a new object with the given value\n" +
                  "   * @param f1 the value\n" +
                  "   */ \n" +
                  "  public Tester(int f1) {\n" +
                  "    this._f1 = f1;\n" +
                  "  }\n" +
                  "\n" +
                  "  public int getF1() {\n" +
                  "    return _f1;\n" +
                  "  }\n" +
                  "\n" +
                  "  public void setF1(int f1) {\n" +
                  "    _f1 = f1;\n" +
                  "  }\n" +
                  "}";
    Node stmt = _parseCode(text).get(0);
    stmt.acceptVisitor(disp);
    compareFileToString(_tempFile,  "l.1 ClassDeclaration {\n" +
                                      "name:\n" +
                                      "  Tester\n" +
                                      "superclass:\n" +
                                      "  Object\n" +
                                      "interfaces:\n" +
                                      "  Comparable\n" +
                                      "members:\n" +
                                      "  l.2 FieldDeclaration {\n" +
                                      "  accessFlags:\n" +
                                      "    2\n" +
                                      "  type:\n" +
                                      "    l.2 PrimitiveType <int>\n" +
                                      "  name:\n" +
                                      "    _f1\n" +
                                      "  initializer:\n" +
                                      "  }\n" +
                                      "  l.4 FieldDeclaration {\n" +
                                      "  accessFlags:\n" +
                                      "    25\n" +
                                      "  type:\n" +
                                      "    l.4 ReferenceType {\n" +
                                      "    representation:\n" +
                                      "      Tester\n" +
                                      "    }\n" +
                                      "  name:\n" +
                                      "    DEF_INSTANCE\n" +
                                      "  initializer:\n" +
                                      "    l.4 SimpleAllocation {\n" +
                                      "    creationType:\n" +
                                      "      l.4 ReferenceType {\n" +
                                      "      representation:\n" +
                                      "        Tester\n" +
                                      "      }\n" +
                                      "    arguments:\n" +
                                      "      l.4 Literal (int) <0> {\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "  l.6 ConstructorDeclaration {\n" +
                                      "  accessFlags:\n" +
                                      "    0\n" +
                                      "  name:\n" +
                                      "    Tester\n" +
                                      "  parameters:\n" +
                                      "  exceptions:\n" +
                                      "  constructorInvocation:\n" +
                                      "  statements:\n" +
                                      "  }\n" +
                                      "  l.12 ConstructorDeclaration {\n" +
                                      "  accessFlags:\n" +
                                      "    0\n" +
                                      "  name:\n" +
                                      "    Tester\n" +
                                      "  parameters:\n" +
                                      "    l.12 FormalParameter {\n" +
                                      "    type:\n" +
                                      "      l.12 PrimitiveType <int>\n" +
                                      "    name:\n" +
                                      "      f1\n" +
                                      "    }\n" +
                                      "  exceptions:\n" +
                                      "  constructorInvocation:\n" +
                                      "  statements:\n" +
                                      "    l.13 koala.dynamicjava.tree.SimpleAssignExpression {\n" +
                                      "    leftExpression:\n" +
                                      "      l.13 ObjectFieldAccess {\n" +
                                      "      expression:\n" +
                                      "        l.13 ThisExpression {\n" +
                                      "        className:\n" +
                                      "          \n" +
                                      "        }\n" +
                                      "      fieldName:\n" +
                                      "        _f1\n" +
                                      "      }\n" +
                                      "    rightExpression:\n" +
                                      "      l.13 QualifiedName {\n" +
                                      "      representation:\n" +
                                      "        f1\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "  l.16 MethodDeclaration {\n" +
                                      "  accessFlags:\n" +
                                      "    1\n" +
                                      "  returnType:\n" +
                                      "    l.16 PrimitiveType <int>\n" +
                                      "  name:\n" +
                                      "    getF1\n" +
                                      "  parameters:\n" +
                                      "  exceptions:\n" +
                                      "  body:\n" +
                                      "    l.16 BlockStatement {\n" +
                                      "    statements:\n" +
                                      "      l.17 ReturnStatement {\n" +
                                      "      expression:\n" +
                                      "        l.17 QualifiedName {\n" +
                                      "        representation:\n" +
                                      "          _f1\n" +
                                      "        }\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "  l.20 MethodDeclaration {\n" +
                                      "  accessFlags:\n" +
                                      "    1\n" +
                                      "  returnType:\n" +
                                      "    l.20 PrimitiveType <void>\n" +
                                      "  name:\n" +
                                      "    setF1\n" +
                                      "  parameters:\n" +
                                      "    l.20 FormalParameter {\n" +
                                      "    type:\n" +
                                      "      l.20 PrimitiveType <int>\n" +
                                      "    name:\n" +
                                      "      f1\n" +
                                      "    }\n" +
                                      "  exceptions:\n" +
                                      "  body:\n" +
                                      "    l.20 BlockStatement {\n" +
                                      "    statements:\n" +
                                      "      l.21 koala.dynamicjava.tree.SimpleAssignExpression {\n" +
                                      "      leftExpression:\n" +
                                      "        l.21 QualifiedName {\n" +
                                      "        representation:\n" +
                                      "          _f1\n" +
                                      "        }\n" +
                                      "      rightExpression:\n" +
                                      "        l.21 QualifiedName {\n" +
                                      "        representation:\n" +
                                      "          f1\n" +
                                      "        }\n" +
                                      "      }\n" +
                                      "    }\n" +
                                      "  }\n" +
                                      "}");
  }
}