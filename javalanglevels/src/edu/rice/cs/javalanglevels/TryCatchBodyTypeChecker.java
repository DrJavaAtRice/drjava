/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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
import edu.rice.cs.plt.iter.*;

import junit.framework.TestCase;

/**Does TypeChecking for the context of a Try-Catch body.  Common to all LanguageLevels.*/
public class TryCatchBodyTypeChecker extends BodyTypeChecker {


  /* Constructor for TryCatchBodyTypeChecker.  Delegates the initialization to the BodyTypeChecker
   * @param bodyData  The enclosing BodyData for the context we are type checking.
   * @param file  The File corresponding to the source file.
   * @param packageName  The package name from the source file.
   * @param importedFiles  The names of the files that are specifically imported (through a class import statement) in the source file.
   * @param importedPackages  The names of all the packages that are imported through a package import statement in the source file.
   * @param vars  The list of VariableData that have already been defined (used so we can make sure we don't use a variable before it has been defined).
   * @param thrown  The list of exceptions thrown in this body
   */
  public TryCatchBodyTypeChecker(BodyData bodyData, File file, String packageName, LinkedList<String> importedFiles, LinkedList<String> importedPackages, LinkedList<VariableData> vars, LinkedList<Pair<SymbolData, JExpression>> thrown) {
    super(bodyData, file, packageName, importedFiles, importedPackages, vars, thrown);
  }
 
  
  /** Create a new instance of this class for visiting inner bodies. */
  protected BodyTypeChecker createANewInstanceOfMe(BodyData bodyData, File file, String pakage, LinkedList<String> importedFiles, LinkedList<String> importedPackages, LinkedList<VariableData> vars, LinkedList<Pair<SymbolData, JExpression>> thrown) {
    return new TryCatchBodyTypeChecker(bodyData, file, pakage, importedFiles, importedPackages, vars, thrown);
  }
  
  /** Overwritten here, becuase it is okay for there to be thrown exceptions in the middle of a try catch. */
  public TypeData forBracedBody(BracedBody that) {
    final TypeData[] items_result = makeArrayOfRetType(that.getStatements().length);
    for (int i = 0; i < that.getStatements().length; i++) {
      items_result[i] = that.getStatements()[i].visit(this);
    }
    return forBracedBodyOnly(that, items_result);
  }
  
  /** Make sure that every Exception in thrown is either in caught or in the list of what can be thrown from where we are.
    * Also make sure that every Exception that is declared to be thrown or caught is actually thrown.
    * Overrides the same method in BodyTypeChecker.
    * @param that  The TryCatchStatement we are currently working with
    * @param caught_array  The SymbolData[] of exceptions that are explicitely caught.
    * @param thrown  The LinkedList of SymbolData of exceptions that are thrown.  This will be modified.
    */
  protected void compareThrownAndCaught(TryCatchStatement that, SymbolData[] caught_array, 
                                        LinkedList<Pair<SymbolData, JExpression>> thrown) {
    LinkedList<Pair<SymbolData, JExpression>> copyOfThrown = new LinkedList<Pair<SymbolData, JExpression>>();
    for (Pair<SymbolData, JExpression> p : thrown) {
      copyOfThrown.addLast(p);
    }
    //Make sure that every Exception in thrown is either caught or in the list of what can be thrown
    for (Pair<SymbolData, JExpression> p : copyOfThrown) {
      SymbolData sd = p.getFirst();
      // Iterate over the caught array and see if the current thrown exception is a subclass of one of the exceptions.
      for (SymbolData currCaughtSD : caught_array) {
        if (sd.isSubClassOf(currCaughtSD) || (!isUncaughtCheckedException(sd, new NullLiteral(SourceInfo.NONE)))) {
          thrown.remove(p);
        }
      }
    }
    makeSureCaughtStuffWasThrown(that, caught_array, copyOfThrown);
  }
  
   /**
    * Test the methods declared in the above class.
    */
  public static class TryCatchBodyTypeCheckerTest extends TestCase {
    
    private TryCatchBodyTypeChecker _tcbtc;
    
    private BodyData _bd1;
    private BodyData _bd2;
    
    private SymbolData _sd1;
    private SymbolData _sd2;
    private SymbolData _sd3;
    private SymbolData _sd4;
    private SymbolData _sd5;
    private SymbolData _sd6;
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[0]);
    private ModifiersAndVisibility _abstractMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"final"});
    
    
    public TryCatchBodyTypeCheckerTest() { this(""); }
    public TryCatchBodyTypeCheckerTest(String name) { super(name); }
    
    public void setUp() {
      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _sd3 = new SymbolData("zebra");
      _sd4 = new SymbolData("u.like.emu");
      _sd5 = new SymbolData("");
      _sd6 = new SymbolData("cebu");

      _bd1 = new MethodData("monkey", 
                            _packageMav, 
                            new TypeParameter[0], 
                            _sd1, 
                            new VariableData[] { new VariableData("i", _publicMav, SymbolData.INT_TYPE, true, null), new VariableData(SymbolData.BOOLEAN_TYPE) },
                            new String[0],
                            _sd1,
                            null); // no SourceInfo
      ((MethodData) _bd1).getParams()[0].setEnclosingData(_bd1);
      ((MethodData) _bd1).getParams()[1].setEnclosingData(_bd1);
                            
      errors = new LinkedList<Pair<String, JExpressionIF>>();
      LanguageLevelConverter.symbolTable.clear();
      _bd1.addEnclosingData(_sd1);
      _bd1.addFinalVars(((MethodData)_bd1).getParams());
      _tcbtc = new TryCatchBodyTypeChecker(_bd1, new File(""), "", new LinkedList<String>(), new LinkedList<String>(), new LinkedList<VariableData>(), new LinkedList<Pair<SymbolData, JExpression>>());
      LanguageLevelConverter.OPT = new Options(JavaVersion.JAVA_5, EmptyIterable.<File>make());
      _tcbtc._importedPackages.addFirst("java.lang");
    }
    
    
    public void testCreateANewInstanceOfMe() {
      //make sure that the correct visitor is returned from createANewInstanceOfMe
      BodyTypeChecker btc = _tcbtc.createANewInstanceOfMe(_tcbtc._bodyData, _tcbtc._file, _tcbtc._package, _tcbtc._importedFiles, _tcbtc._importedPackages, _tcbtc._vars, _tcbtc._thrown);
      assertTrue("Should be an instance of ConstructorBodyTypeChecker", btc instanceof TryCatchBodyTypeChecker);
    }
    
    public void testForBracedBody() {
      //make sure it is okay to have a uncaught exception in a braced body
      BracedBody bb = new BracedBody(SourceInfo.NONE, 
                                     new BodyItemI[] { 
        new ThrowStatement(SourceInfo.NONE, 
                           new SimpleNamedClassInstantiation(SourceInfo.NONE, 
                                         new ClassOrInterfaceType(SourceInfo.NONE, 
                                                                 "java.util.prefs.BackingStoreException", 
                                                                 new Type[0]), 
                                                             new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[] {new StringLiteral(SourceInfo.NONE, "arg")})))});
      
      LanguageLevelVisitor llv = 
        new LanguageLevelVisitor(new File(""), 
                                 "",
                                 null, // enclosingClassName for top level traversal
                                 new LinkedList<String>(), 
                                 new LinkedList<String>(), 
                                 new HashSet<String>(), 
                                 new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                                 new LinkedList<Command>());
      llv.errors = new LinkedList<Pair<String, JExpressionIF>>();
      llv._errorAdded=false;
      LanguageLevelConverter.symbolTable.clear();
      LanguageLevelConverter._newSDs.clear();
      LanguageLevelConverter.loadSymbolTable();
      llv.continuations = new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>();
      llv.visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
//      llv._hierarchy = new Hashtable<String, TypeDefBase>();
      llv._classesInThisFile = new HashSet<String>();
      
      SymbolData o = symbolTable.get("java.lang.Object");
//      o.setIsContinuation(false);
//      o.setMav(_publicMav);
//      symbolTable.put("java.lang.Object", o);
             
      SymbolData string = new SymbolData("java.lang.String");
      string.setIsContinuation(false);
      string.setMav(_publicMav);
      string.setSuperClass(o);   // a white lie for this test   
      symbolTable.put("java.lang.String", string);

      SymbolData e = llv.getSymbolData("java.util.prefs.BackingStoreException", SourceInfo.NONE, true);
      
      bb.visit(_tcbtc);
      assertEquals("There should be no errors because it's ok to have uncaught exceptions in this visitor", 
                   0, 
                   errors.size());
    }
    
    public void testCompareThrownAndCaught() {
      BracedBody emptyBody = new BracedBody(SourceInfo.NONE, new BodyItemI[0]);
      Block b = new Block(SourceInfo.NONE, emptyBody);

      PrimitiveType intt = new PrimitiveType(SourceInfo.NONE, "int");
      UninitializedVariableDeclarator uvd = 
        new UninitializedVariableDeclarator(SourceInfo.NONE, intt, new Word(SourceInfo.NONE, "i"));
      FormalParameter param = 
        new FormalParameter(SourceInfo.NONE, 
                            new UninitializedVariableDeclarator(SourceInfo.NONE, intt, new Word(SourceInfo.NONE, "j")), 
                            false);

      NormalTryCatchStatement ntcs = new NormalTryCatchStatement(SourceInfo.NONE, b, new CatchBlock[] {new CatchBlock(SourceInfo.NONE,  param, b)});

      SymbolData javaLangThrowable = _tcbtc.getSymbolData("java.lang.Throwable", ntcs, false, true); 
      _tcbtc.symbolTable.put("java.lang.Throwable", javaLangThrowable);
      SymbolData exception = new SymbolData("my.crazy.exception");
      exception.setSuperClass(javaLangThrowable);
      SymbolData exception2 = new SymbolData("A&M.beat.Rice.in.BaseballException");
      exception2.setSuperClass(javaLangThrowable);
      SymbolData exception3 = new SymbolData("aegilha");
      exception3.setSuperClass(exception2);
      SymbolData[] caught_array = new SymbolData[] { exception, exception2 };
      LinkedList<Pair<SymbolData, JExpression>> thrown = new LinkedList<Pair<SymbolData, JExpression>>();
      thrown.addLast(new Pair<SymbolData, JExpression>(exception, ntcs));
      thrown.addLast(new Pair<SymbolData, JExpression>(exception2, ntcs));
      thrown.addLast(new Pair<SymbolData, JExpression>(exception3, ntcs));
      
      _tcbtc.compareThrownAndCaught(ntcs, caught_array, thrown);
//      System.err.println("thrown = " + thrown);
      assertTrue("Thrown should have no elements", thrown.isEmpty());

      
      _tcbtc.compareThrownAndCaught(ntcs, new SymbolData[] {exception2}, thrown);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "The exception A&M.beat.Rice.in.BaseballException is never thrown in the body of the corresponding try block", errors.get(0).getFirst());

    }
  }
}