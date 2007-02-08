/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu)
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

package edu.rice.cs.javalanglevels;

import edu.rice.cs.javalanglevels.tree.*;
import edu.rice.cs.javalanglevels.parser.JExprParser;
import java.util.*;
import java.io.*;
import edu.rice.cs.plt.reflect.JavaVersion;

import junit.framework.TestCase;



/** 
 * This class is the only exception to the TypeChecker is language level independent rule.
 * It is used when void should not be returned from a method.  (This is a problem in the Intermediate
 * language level).
 **/
public class VoidMethodsNotAllowedClassBodyTypeChecker extends ClassBodyTypeChecker {//implements JExpressionIFVisitor<SymbolData> {

  /**The language level of the file being visited*/
  private String _level;
  
  /**
   * @param sd  The SymbolData of the class we are type checking.
   * @param file  The File corresponding to the source file we are checking.
   * @param packageName  The package of the source file.
   * @param importedFiles  A list of the names of the classes that are specifically imported in the source file
   * @param importedPackages  A list of the names of the packages that are imported in the source file.
   * @param vars  A list of the variable datas that can be seen and have been given a value before this context
   * @param thrown  The exceptions that are thrown
   * @param level  The String representing the current language level
   */
  public VoidMethodsNotAllowedClassBodyTypeChecker(SymbolData sd, File file, String packageName, LinkedList<String> importedFiles, LinkedList<String> importedPackages, LinkedList<VariableData> vars, LinkedList<Pair<SymbolData, JExpression>> thrown, String level) {
    super(sd, file, packageName, importedFiles, importedPackages, vars, thrown);
    this._level = level;
  }
    
  /**Give an appropriate error if the method has a void return type*/
  public TypeData forConcreteMethodDef(ConcreteMethodDef that) {
    TypeData result = super.forConcreteMethodDef(that);

    if (result == SymbolData.VOID_TYPE) {
      _addError("The keyword \"void\" is not allowed outside of a TestCase class at the " + _level + " level", that);
    }
    return result;
  }
  
  /**Give an appropriate error if the method has a void return type*/
  public TypeData forAbstractMethodDef(AbstractMethodDef that) {
    TypeData result = super.forAbstractMethodDef(that);
    if (result == SymbolData.VOID_TYPE) {
      _addError("The keyword \"void\" is not allowed outside of a TestCase class at the " + _level + " level", that);
    }
    return result;

  }

  /**
   * Test the two methods defined in the above class
   */
  public static class VoidMethodsNotAllowedClassBodyTypeCheckerTest extends TestCase {
    
    private VoidMethodsNotAllowedClassBodyTypeChecker _cbbtc;
    
    private SymbolData _sd1;
    private SymbolData _sd2;
    private SymbolData _sd3;
    private SymbolData _sd4;
    private SymbolData _sd5;
    private SymbolData _sd6;
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[0]);
    private ModifiersAndVisibility _abstractMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"final"});
    private ModifiersAndVisibility _publicAbstractMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public", "abstract"});
    
    
    public VoidMethodsNotAllowedClassBodyTypeCheckerTest() {
      this("");
    }
    public VoidMethodsNotAllowedClassBodyTypeCheckerTest(String name) {
      super(name);
    }
    
    public void setUp() {
      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _sd3 = new SymbolData("zebra");
      _sd4 = new SymbolData("u.like.emu");
      _sd5 = new SymbolData("");
      _sd6 = new SymbolData("cebu");
      errors = new LinkedList<Pair<String, JExpressionIF>>();
      symbolTable = new Symboltable();
      _cbbtc = new VoidMethodsNotAllowedClassBodyTypeChecker(_sd1, new File(""), "", new LinkedList<String>(), new LinkedList<String>(), new LinkedList<VariableData>(), new LinkedList<Pair<SymbolData, JExpression>>(), "Intermediate");    
      _cbbtc._targetVersion = JavaVersion.JAVA_5;
    }
    
    public void testForConcreteMethodDef() {
      //Test that if a method returns void, an error is thrown
      BracedBody bb = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new VoidReturnStatement(JExprParser.NO_SOURCE_INFO)});

      ConcreteMethodDef cmd = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _publicMav, new TypeParameter[0], 
                                                     new VoidReturn(JExprParser.NO_SOURCE_INFO, "void"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "myMethod3"), 
                                                    new FormalParameter[0], 
                                                    new ReferenceType[0], bb);

      
      MethodData md = new MethodData("myMethod3", _publicMav, new TypeParameter[0], SymbolData.VOID_TYPE, new VariableData[0], new String[0], _sd1, cmd);
      _sd1.setMethods(new LinkedList<MethodData>());
      _sd1.addMethod(md);
      _cbbtc = new VoidMethodsNotAllowedClassBodyTypeChecker(_sd1, _cbbtc._file, _cbbtc._package, _cbbtc._importedFiles, _cbbtc._importedPackages,  new LinkedList<VariableData>(), new LinkedList<Pair<SymbolData, JExpression>>(), "Intermediate");

      cmd.visit(_cbbtc);
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "The keyword \"void\" is not allowed outside of a TestCase class at the Intermediate level", errors.get(0).getFirst());
    }
    
    public void testForAbstractMethodDef() {
      AbstractMethodDef amd = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, _publicAbstractMav, new TypeParameter[0], 
                                                     new VoidReturn(JExprParser.NO_SOURCE_INFO, "void"), new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[0], 
                                                     new ReferenceType[0]);

      
      MethodData md = new MethodData("myMethod", _publicAbstractMav, new TypeParameter[0], SymbolData.VOID_TYPE, new VariableData[0], new String[0], _sd1, amd);
      _sd1.setMethods(new LinkedList<MethodData>());
      _sd1.addMethod(md);
      _cbbtc = new VoidMethodsNotAllowedClassBodyTypeChecker(_sd1, _cbbtc._file, _cbbtc._package, _cbbtc._importedFiles, _cbbtc._importedPackages,  new LinkedList<VariableData>(), new LinkedList<Pair<SymbolData, JExpression>>(), "Intermediate");

      amd.visit(_cbbtc);
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "The keyword \"void\" is not allowed outside of a TestCase class at the Intermediate level", errors.get(0).getFirst());
    }
      
  }
  
}