package koala.dynamicjava.interpreter;

import junit.framework.TestCase;
import koala.dynamicjava.parser.*;


import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.modifier.*;
import koala.dynamicjava.interpreter.throwable.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;
import koala.dynamicjava.parser.wrapper.*;

import junit.framework.TestCase;

import edu.rice.cs.drjava.model.repl.*;

/**
 * A JUnit test case class.
 * Every method starting with the word "test" will be called when running
 * the test with JUnit.
 */
public class ForEachNamingTest extends TestCase {
 
  /*
   * test that a foreach statement creates necessary variables
   */
  public void testMinimalForEach() throws ParseException {
    Context cntxt = new GlobalContext(new TreeInterpreter(new JavaCCParserFactory()));
    cntxt.define("crazyCollection", Collection.class);
    
    NameVisitor nv = new NameVisitor(cntxt);
    Parser p = new Parser(new StringReader("for(Integer i:crazyCollection) i=0;"));
    List<Node>    statements = p.parseStream();
    
    assertEquals("[(koala.dynamicjava.tree.ForEachStatement: (koala.dynamicjava.tree.FormalParameter: false (koala.dynamicjava.tree.ReferenceType: Integer) i) (koala.dynamicjava.tree.QualifiedName: crazyCollection) (koala.dynamicjava.tree.SimpleAssignExpression: (koala.dynamicjava.tree.QualifiedName: i) (koala.dynamicjava.tree.IntegerLiteral: 0 0 int)))]", statements.toString());
      
    for(Node n: statements){
      n.acceptVisitor(nv);
    }
    
    assertEquals("[(koala.dynamicjava.tree.ForEachStatement: (koala.dynamicjava.tree.FormalParameter: false (koala.dynamicjava.tree.ReferenceType: Integer) i) (koala.dynamicjava.tree.QualifiedName: crazyCollection) (koala.dynamicjava.tree.SimpleAssignExpression: (koala.dynamicjava.tree.QualifiedName: i) (koala.dynamicjava.tree.IntegerLiteral: 0 0 int)))]", statements.toString());
    
    for(Node n: statements){
      List vars = ((ForEachStatement)n).getVars();
    
      for(int i=1;i<=vars.size();i++){
        assertEquals("#_foreach_var_" + i, vars.get(i-1));
      }
    }
  }



  /**
   * test that nested for each statements don't create overlapping variables
   */
  public void testSuperForEach() throws ParseException {
    Context cntxt = new GlobalContext(new TreeInterpreter(new JavaCCParserFactory()));
    cntxt.define("crazyCollection", Collection.class);
    
    NameVisitor nv = new NameVisitor(cntxt);
    Parser p = new Parser(new StringReader("for(Integer i:crazyCollection) for(Integer j:crazyCollection) j=0;"));
    List<Node>    statements = p.parseStream();
    
      
    for(Node n: statements){
      n.acceptVisitor(nv);
    }
    
   
    for(Node n: statements){
      List vars = ((ForEachStatement)n).getVars();
      Node body = ((ForEachStatement)n).getBody();
      for(int i=1;i<=vars.size();i++){
        assertEquals("#_foreach_var_" + i, vars.get(i-1));
      }
      vars = ((ForEachStatement)body).getVars();
      for(int i=3;i<=vars.size();i++){
        assertEquals("#_foreach_var_" + i, vars.get(i-1));
      }
    }
    
    
  }
}
