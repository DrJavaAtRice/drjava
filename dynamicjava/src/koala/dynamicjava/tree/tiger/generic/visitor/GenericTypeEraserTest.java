/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
 END_COPYRIGHT_BLOCK*/

package koala.dynamicjava.tree.tiger.generic.visitor;

import junit.framework.TestCase;
import java.io.StringReader;
import java.util.List;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.parser.wrapper.ParserFactory;
import koala.dynamicjava.parser.wrapper.JavaCCParserFactory;


public class GenericTypeEraserTest extends TestCase {
  
  ParserFactory parserFactory;
  String testString;
  
  public void setUp(){
    parserFactory = new JavaCCParserFactory();
  }
  
  public List<Node> parse(String testString){
    List<Node> retval = parserFactory.createParser(new StringReader(testString),"UnitTest").parseStream();
    return retval;
  }
  
  public void testBasics() {
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
    
    
    
/*
    testString = "Boolean y = Boolean.FALSE;";
    assertEquals("Parse of Boolean.FALSE", Boolean.FALSE, parse(testString));
    
    testString = "String z = \"FOO\" + \"BAR\";";
    assertEquals("Parse of string concatenation", "FOOBAR", parse(testString));
    
    testString = "int[] a = new int[]{1,2,3}; \"\"+a[0]+a[1]+a[2];";
    assertEquals("Parse of anonymous array", "123", parse(testString));
    
    testString = "int[][] b = new int[][]{{12, 0}, {1, -15}}; b[0][0] + b[1][1];";
    assertEquals("Parse of 2D Anonymous array", -3, parse(testString));
    
    testString = "(Number) new Integer(12);";
    assertEquals("Parse of cast", new Integer(12), parse(testString));
*/
  }
}


