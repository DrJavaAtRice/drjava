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

package koala.dynamicjava.interpreter;

import junit.framework.TestCase;
import koala.dynamicjava.parser.impl.*;


import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.modifier.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;
import koala.dynamicjava.parser.wrapper.*;

import junit.framework.TestCase;

/**
 * A JUnit test case class.
 * Every method starting with the word "test" will be called when running
 * the test with JUnit.
 */
public class ForEachTypingTest extends TestCase {
 
  /*
   * test that a foreach statement creates necessary variables
   */
  public void testMinimalForEach() throws ParseException {
    Context cntxt = new GlobalContext(new TreeInterpreter(new JavaCCParserFactory()));
    cntxt.define("crazyCollection", Collection.class);
    
    NameVisitor nv = new NameVisitor(cntxt);
    Parser p = new Parser(new StringReader("for(Integer i:crazyCollection) i=0;"));
    List<Node>    statements = p.parseStream();
    
    assertEquals("[(koala.dynamicjava.tree.ForEachStatement: (koala.dynamicjava.tree.FormalParameter: false (koala.dynamicjava.tree.ReferenceTypeName: Integer) i) (koala.dynamicjava.tree.QualifiedName: crazyCollection) (koala.dynamicjava.tree.SimpleAssignExpression: (koala.dynamicjava.tree.QualifiedName: i) (koala.dynamicjava.tree.IntegerLiteral: 0 0 int)))]", statements.toString());
      
    for(Node n: statements){
      n.acceptVisitor(nv);
    }
    
    assertEquals("[(koala.dynamicjava.tree.ForEachStatement: (koala.dynamicjava.tree.FormalParameter: false (koala.dynamicjava.tree.ReferenceTypeName: Integer) i) (koala.dynamicjava.tree.QualifiedName: crazyCollection) (koala.dynamicjava.tree.SimpleAssignExpression: (koala.dynamicjava.tree.QualifiedName: i) (koala.dynamicjava.tree.IntegerLiteral: 0 0 int)))]", statements.toString());
    
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
