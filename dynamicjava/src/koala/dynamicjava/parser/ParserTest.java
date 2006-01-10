/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

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

package koala.dynamicjava.parser;

import junit.framework.TestCase;

import java.util.*;
import java.io.*;
import koala.dynamicjava.parser.impl.*;
import koala.dynamicjava.tree.*;

public class ParserTest extends TestCase {
  
  private List<Node> parseText(String txt) throws ParseException {
      Parser p = new Parser(new StringReader(txt));
      return p.parseStream();
  }
  
  public void testVarArgsFormalParameters() throws ParseException {
    List<FormalParameter> fpList = getFormalParameterList("String... s", true);
    assertEquals("should be 1 parameter", 1, fpList.size());
    assertEquals("param should be array", ArrayType.class, fpList.get(0).getType().getClass());
    ArrayType at = (ArrayType)fpList.get(0).getType();
    assertEquals("param should be a string array", "(koala.dynamicjava.tree.ReferenceType: String)", at.getElementType().toString());
    
    fpList = getFormalParameterList("String[] s", false);
    assertEquals("should be 1 parameter", 1, fpList.size());
    assertEquals("param should be array", ArrayType.class, fpList.get(0).getType().getClass());
    at = (ArrayType)fpList.get(0).getType();
    assertEquals("param should be a string array", "(koala.dynamicjava.tree.ReferenceType: String)", at.getElementType().toString());
    
    fpList = getFormalParameterList("", false);
    assertEquals("should be no parameters", 0, fpList.size());
    
    fpList = getFormalParameterList("int i",false);
    assertEquals("should be 1 parameter", 1, fpList.size());
    assertEquals("param should be int", "(koala.dynamicjava.tree.IntType: int)", fpList.get(0).getType().toString());
        
    fpList = getFormalParameterList("int i, String... s", true);
    assertEquals("should be 2 parameters", 2, fpList.size());
    assertEquals("1st param should be int", "(koala.dynamicjava.tree.IntType: int)", fpList.get(0).getType().toString());
    assertEquals("2nd param should be array", ArrayType.class, fpList.get(1).getType().getClass());
    at = (ArrayType)fpList.get(1).getType();
    assertEquals("param should be a string array", "(koala.dynamicjava.tree.ReferenceType: String)", at.getElementType().toString());
        
    fpList = getFormalParameterList("int i, int j, String... s", true);
    assertEquals("should be 3 parameters", 3, fpList.size());
    assertEquals("1st param should be int", "(koala.dynamicjava.tree.IntType: int)", fpList.get(0).getType().toString());
    assertEquals("2nd param should be int", "(koala.dynamicjava.tree.IntType: int)", fpList.get(1).getType().toString());
    assertEquals("3rd param should be array", ArrayType.class, fpList.get(2).getType().getClass());
    at = (ArrayType)fpList.get(2).getType();
    assertEquals("param should be a string array", "(koala.dynamicjava.tree.ReferenceType: String)", at.getElementType().toString());
        
    try {
      getFormalParameterList("int... i, int... j", true);
      fail("Can't have two varargs in same parameter list: (int... i, int... j)");
    } catch (ParseException e) { }
    
    try {
      getFormalParameterList("int... i, String j", false);
      fail("only last one can be varargs: (int... i, String j)");
    } catch (ParseException e) { }
    
  }
  
  private List<FormalParameter> getFormalParameterList(String paramString, boolean isVarArgs) throws ParseException {
    List<Node> list = parseText("public static void m("+ paramString +") { }");
    assertEquals("list should be singleton", 1, list.size());
    Node n = list.get(0);
    assertEquals("Type of node", MethodDeclaration.class, n.getClass());
    MethodDeclaration m = (MethodDeclaration)n;
    
    if (isVarArgs)
      assertTrue("Should be varargs", m.isVarArgs());
    else
      assertFalse("Shouldn't be varargs", m.isVarArgs());
    
    return m.getParameters();
  }
  
  /* This test is added to cover the tests where our parser handles the corner cases of dealing with ints */
  public void testIntMinValue() throws ParseException{
    List<Node> list = parseText("int x = Integer.MIN_VALUE");
    assertEquals("List should be of length one", 1, list.size());
    Node n = list.get(0);
    assertEquals("Type of Node", VariableDeclaration.class, n.getClass());
  }
  public void testIntMaxValue() throws ParseException{
    List<Node> list = parseText("int x = Integer.MAX_VALUE");
    assertEquals("List should be of length one", 1, list.size());
    Node n = list.get(0);
    assertEquals("Type of Node", VariableDeclaration.class, n.getClass());
  }
  /* This test is added to reveal the bug reported with number: 1201685 - Interactions pane rejects minimum int */
  public void testSmallestInt() throws ParseException {
    List<Node> list = parseText("int x = -2147483648");
    assertEquals("List should be of length one", 1, list.size());
    Node n = list.get(0);
    assertEquals("Type of Node", VariableDeclaration.class, n.getClass());
  }
  public void testBiggestIntValue() throws ParseException {
    List<Node> list = parseText("int x = 2147483647");
    assertEquals("List should be of length one", 1, list.size());
    Node n = list.get(0);
    assertEquals("Type of Node", VariableDeclaration.class, n.getClass());
  }
  public void testIntTooBig() throws ParseException {
    try{
      List<Node> list = parseText("int x = 2147483648");
     fail("Should have thrown a NumberFormatException!");
    }
    catch(NumberFormatException nfe){
      //Exception expected.
    }
  }
  
  public void testIntTooSmall() throws ParseException {
    try{
      List<Node> list = parseText("int x = -2147483649");
      fail("Should have thrown a NumberFormatException!");
    }
    catch(NumberFormatException nfe){
      //Exception expected.
    }
  }
  
 /* This test is added to cover the tests where our parser handles the corner cases of dealing with longs */
  public void testLongMinValue() throws ParseException{
    List<Node> list = parseText("long x = Long.MIN_VALUE");
    assertEquals("List should be of length one", 1, list.size());
    Node n = list.get(0);
    assertEquals("Type of Node", VariableDeclaration.class, n.getClass());
  }
  
  /* This test is added to cover the tests where our parser handles the corner cases of dealing with longs */
  public void testSmallestLong() throws ParseException{
    List<Node> list = parseText("long x = -9223372036854775808L");
    assertEquals("List should be of length one", 1, list.size());
    Node n = list.get(0);
    assertEquals("Type of Node", VariableDeclaration.class, n.getClass());
  }
  
  /* This test is added to cover the tests where our parser handles the corner cases of dealing with longs */
  public void testBiggestLongValue() throws ParseException{
    List<Node> list = parseText("long x = 9223372036854775807L");
    assertEquals("List should be of length one", 1, list.size());
    Node n = list.get(0);
    assertEquals("Type of Node", VariableDeclaration.class, n.getClass());
  }
  
  /* This test is added to cover the tests where our parser handles the corner cases of dealing with longs */
  public void testLongMaxValue() throws ParseException{
    List<Node> list = parseText("long x = Long.MAX_VALUE");
    assertEquals("List should be of length one", 1, list.size());
    Node n = list.get(0);
    assertEquals("Type of Node", VariableDeclaration.class, n.getClass());
  }
  public void testLongTooBig() throws ParseException {
    try{
      List<Node> list = parseText("long x = 9223372036854775808L");
     fail("Should have thrown a NumberFormatException!");
    }
    catch(NumberFormatException nfe){
      //Exception expected.
    }
  }
  
  
  
  public void testLongTooSmall() throws ParseException {
    try{
      List<Node> list = parseText("long x = -9223372036854775809L");
      fail("Should have thrown a NumberFormatException!");
    }
    catch(NumberFormatException nfe){
      //Exception expected.
    }
  }
   
//  
//  /* Test which reflect a test where Dynamicjava allows a number to be declared that is too small to be represented */
//  public void testFloatingPointNumberTooSmall() throws ParseException {
//    List<Node> list = parseText("double d = 00000000000000000001E-999");
//    fail("Dynamicjava allowed a declaration it shouldn't, it should have been an error with error message \"floating point number too small\"");
//  }
//  
//  /* Test which reflect a test where Dynamicjava allows a number to be declared that is too small to be represented */
//  public void testFloatingPointNumberTooLarge() throws ParseException {
//    List<Node> list = parseText("double d = 1E999");
//    fail("Dynamicjava allowed a declaration it shouldn't, it should have been an error with error message \"floating point number too large\"");
//  }
}