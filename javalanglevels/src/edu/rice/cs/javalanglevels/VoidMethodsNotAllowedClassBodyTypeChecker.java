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

package edu.rice.cs.javalanglevels;

import edu.rice.cs.javalanglevels.tree.*;
import edu.rice.cs.javalanglevels.parser.JExprParser;
import java.util.*;
import java.io.*;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.iter.IterUtil;

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
      LanguageLevelConverter.OPT = new Options(JavaVersion.JAVA_5, IterUtil.<File>empty());
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