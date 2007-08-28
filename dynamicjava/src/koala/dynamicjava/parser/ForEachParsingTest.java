/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

package koala.dynamicjava.parser;

import junit.framework.TestCase;


import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.parser.wrapper.*;
import koala.dynamicjava.parser.impl.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;

/**
 * A JUnit test case class.
 * Every method starting with the word "test" will be called when running
 * the test with JUnit.
 */
public class ForEachParsingTest extends TestCase {
  
  /* tests the basic syntax of a foreach statement
   */
  public void testMinimalForEach() throws ParseException {
      Parser p = new Parser(new StringReader("for(Integer i:crazyCollection);"));
      List    statements = p.parseStream();
      ListIterator    it = statements.listIterator();
      Object result = null;
      
      assertEquals("[(koala.dynamicjava.tree.ForEachStatement: (koala.dynamicjava.tree.FormalParameter: false (koala.dynamicjava.tree.ReferenceTypeName: Integer) i) (koala.dynamicjava.tree.QualifiedName: crazyCollection) (koala.dynamicjava.tree.EmptyStatement: ))]", statements.toString());
  }
  
  
  public void testFoo() throws ParseException{
      Parser p = new Parser(new StringReader("import java.lang.reflect.Array;" + "Array.getLength(asdf);"));
      List    statements = p.parseStream();
      ListIterator    it = statements.listIterator();
      
  }

  /* tests the basic syntax of a foreach statement
   * includes a final keyword for the parameter
   */
  public void testFinalParameter() throws ParseException {
      Parser p = new Parser(new StringReader("for(final Integer i:crazyCollection);"));
      List    statements = p.parseStream();
      ListIterator    it = statements.listIterator();
      Object result = null;
      
      assertEquals("[(koala.dynamicjava.tree.ForEachStatement: (koala.dynamicjava.tree.FormalParameter: true (koala.dynamicjava.tree.ReferenceTypeName: Integer) i) (koala.dynamicjava.tree.QualifiedName: crazyCollection) (koala.dynamicjava.tree.EmptyStatement: ))]", statements.toString());
  }

  /* tests the basic syntax of a foreach statement
   * makes sure the parameter cannot be static
   */
  public void testStaticParameter(){
    try{
      Parser p = new Parser(new StringReader("for(static Integer i:crazyCollection);"));
      List    statements = p.parseStream();
      fail("\"static\" is not allowed in parameter of foreach!");
    }catch(ParseException e){
      
    }
  }
  
  public void testCollectionInForeach() throws ParseException{
    Parser p = new Parser(new StringReader("for(final String i:new List());"));
    List statements = p.parseStream();
    
    assertEquals("[(koala.dynamicjava.tree.ForEachStatement: (koala.dynamicjava.tree.FormalParameter: true (koala.dynamicjava.tree.ReferenceTypeName: String) i) (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: List) null) (koala.dynamicjava.tree.EmptyStatement: ))]", statements.toString());
  }
  
  public void testArrayInForeach(){
    try{
    Parser p = new Parser(new StringReader("for(final String i:{\"dsfg\",\"safsa\"});"));
    List statements = p.parseStream();

    }catch(ParseException e){
      
    }
  }
  
  public void testNonTrivialCollectionInForeach() throws ParseException{
    Parser p = new Parser(new StringReader("for(final String i:new List(\"asdf\", \"asfd\"));"));
    List statements = p.parseStream();
    
   
    assertEquals("[(koala.dynamicjava.tree.ForEachStatement: (koala.dynamicjava.tree.FormalParameter: true (koala.dynamicjava.tree.ReferenceTypeName: String) i) (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: List) [(koala.dynamicjava.tree.StringLiteral: \"asdf\" asdf class java.lang.String), (koala.dynamicjava.tree.StringLiteral: \"asfd\" asfd class java.lang.String)]) (koala.dynamicjava.tree.EmptyStatement: ))]", statements.toString());
  }
  
  public void testFor() throws ParseException{
    Parser p = new Parser(new StringReader("for(;;);"));
    List statements = p.parseStream();
    
    assertEquals("[(koala.dynamicjava.tree.ForStatement: null null null (koala.dynamicjava.tree.EmptyStatement: ))]", statements.toString());
  }

  public void testSimpleFor() throws ParseException{
    Parser p = new Parser(new StringReader("for(int i=0;i<10;i++);"));
    List statements = p.parseStream();

    assertEquals("[(koala.dynamicjava.tree.ForStatement: [(koala.dynamicjava.tree.VariableDeclaration: false (koala.dynamicjava.tree.IntTypeName: int) i (koala.dynamicjava.tree.IntegerLiteral: 0 0 int))] (koala.dynamicjava.tree.LessExpression: (koala.dynamicjava.tree.QualifiedName: i) (koala.dynamicjava.tree.IntegerLiteral: 10 10 int)) [(koala.dynamicjava.tree.PostIncrement: (koala.dynamicjava.tree.QualifiedName: i))] (koala.dynamicjava.tree.EmptyStatement: ))]", statements.toString());
  }

  public void testForWithStatement() throws ParseException{
    Parser p = new Parser(new StringReader("for(;;);System.out.println(i);"));
    List statements = p.parseStream();
    
    assertEquals("[(koala.dynamicjava.tree.ForStatement: null null null (koala.dynamicjava.tree.EmptyStatement: )), (koala.dynamicjava.tree.ObjectMethodCall: println [(koala.dynamicjava.tree.QualifiedName: i)] (koala.dynamicjava.tree.QualifiedName: System.out))]", statements.toString());
  }
}
