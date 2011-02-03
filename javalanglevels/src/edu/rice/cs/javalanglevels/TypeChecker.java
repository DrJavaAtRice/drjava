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
import edu.rice.cs.javalanglevels.util.Log;
import edu.rice.cs.javalanglevels.util.UnexpectedException;
import edu.rice.cs.javalanglevels.util.Utilities;
import java.util.*;
import java.io.File;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.iter.*;

import junit.framework.TestCase;

/** Does Type Checking that is not dependent on the enclosing body.  Also does top level type checking.  Common
  * to all langauge levels. */
public class TypeChecker extends JExpressionIFDepthFirstVisitor<TypeData> implements JExpressionIFVisitor<TypeData> {
  
  public static final SourceInfo NONE = SourceInfo.NONE;
  public static final NullLiteral NULL_LITERAL = new NullLiteral(NONE);
  
  public static final ModifiersAndVisibility _packageMav  = new ModifiersAndVisibility(NONE, new String[0]);
  public static final ModifiersAndVisibility _publicMav   = new ModifiersAndVisibility(NONE, new String[] {"public"});
  public static final ModifiersAndVisibility _protectedMav = 
    new ModifiersAndVisibility(NONE, new String[] {"protected"});
  public static final ModifiersAndVisibility _privateMav = new ModifiersAndVisibility(NONE, new String[] {"private"});

  public static final ModifiersAndVisibility _abstractMav = new ModifiersAndVisibility(NONE, new String[] {"abstract"});
  public static final ModifiersAndVisibility _finalMav    = new ModifiersAndVisibility(NONE, new String[] {"final"});
  public static final ModifiersAndVisibility _finalPublicMav =
    new ModifiersAndVisibility(NONE, new String[] {"final", "public"});
  public static final ModifiersAndVisibility _publicAbstractMav =
    new ModifiersAndVisibility(NONE, new String[] {"public", "abstract"});
  public static final ModifiersAndVisibility _publicStaticMav =
    new ModifiersAndVisibility(NONE, new String[] {"public", "static"});
  
  protected static final Log _log = new Log("LLConverter.txt", false);

  /** Holds any errors that are encountered during TypeChecking */
  static LinkedList<Pair<String, JExpressionIF>> errors;
  
  /** Holds the information about any classes/interfaces that have been resolved */
  static final Symboltable symbolTable = LanguageLevelConverter.symbolTable;
  
  /**True if we have an error we can't recover from*/
  static boolean _errorAdded;
  
  /** The source file we are type checking */
  File _file;
  
  /** The package of the source file. */
  String _package;
  
  /** Holds the names of the specifically imported classes. */
  LinkedList<String> _importedFiles;  
  
  /**Holds the names of packages that are imported in the source file*/
  LinkedList<String> _importedPackages;
  
  /** The normal constructor.  Called to begin type checking
    * @param file  The source file being type checked
    * @param packageName  The name of the package of the source file
    * @param errors  The list of errors that are encountered during type checking
    * @param importedFiles  The specific classes imported in the source file
    * @param importedPackages  The list of package names that are imported
    */
  public TypeChecker(File file, String packageName, LinkedList<Pair<String, JExpressionIF>> errors,
                     Symboltable symbolTable, LinkedList<String> importedFiles,
                     LinkedList<String> importedPackages) {
    _file = file;
    _package = packageName;
    this.errors = errors;
//    this.symbolTable = symbolTable;
    this._importedFiles = importedFiles;
    this._importedPackages = importedPackages;
  }
  
  /** Called by the subclasses.
    * @param file  The Source File being checked
    * @param packageName  The package the source file is in
    * @param importedFiles  The specific classes imported in the source file
    * @param importedPackages  The list of package names that are imported
    */
  public TypeChecker(File file, String packageName, LinkedList<String> importedFiles, 
                     LinkedList<String> importedPackages) {
    _file = file;
    _package = packageName;
    _importedFiles = importedFiles;
    _importedPackages = importedPackages;
  }

  /**The top level type checker does not have a data */
  protected Data _getData() {
    throw new RuntimeException("Internal Program Error: _getData() shouldn't get called from TypeChecker.  " +
                               "Please report this bug.");
  }
  
  /** Adds an appropriate definition of junit.framework.TestCase to symbolTable. */
  protected static SymbolData defineTestCaseClass() {
    SymbolData testCase = new SymbolData("junit.framework.TestCase");
    testCase.setIsContinuation(false);
    testCase.setMav(_publicMav);
    testCase.setPackage("junit.framework");
    LanguageLevelConverter.symbolTable.put("junit.framework.TestCase", testCase);
    return testCase;
  }
  
  /** Returns the SymbolData corresponding to the name className.  Checks 1) unqualified or partially-qualified inner
    * classes visible in this scope, 2) fully-qualified inner classes, 3) primitives, 4) array types, 5) fully
    * qualified top-level classes, 6) imported classes, 7) a top-level class within this package, 8) imported packages,
    * and 9) java.lang classes.  Assumes that an error should be generated if the class is not found, and that
    * classes are not allowed to extend java.lang.Runnable.
    * Will always check accessiblity since giveException is assumed to be true.
    * @param className    The class name to look up which may or may not be fully qualified.
    * @param enclosingData  The enclosing data -- either a MethodData or a ClassData for this reference
    * @param jexpr        The AST of the phrase containing the reference.
    * */ 
  public SymbolData getSymbolData(String className, Data enclosingData, JExpression jexpr) {
    return getSymbolData(className, enclosingData, jexpr, true);
  }
  
  /** Call the next version of GetSymbolData, but pass it giveException as the flag for whether or not to
    * give ambigException.
    */
  public SymbolData getSymbolData(String className, Data enclosingData, JExpression jexpr, boolean giveException) {
    return getSymbolData(giveException, className, enclosingData, jexpr, giveException);
  }
  
  /** Returns the SymbolData corresponding to the name className.  Checks 1) unqualified or partially-qualified inner
    * classes visible in this scope, 2) fully-qualified inner classes, 3) primitives, 4) array types, 5) fully 
    * qualified top-level classes, 6) imported classes, 7) a top-level class within this package, 8) imported packages,
    * and 9) java.lang classes.  Assumes that classes are not allowed to extend java.lang.Runnable.
    * Will only check accessibility if giveException is true.
    *  */
  public SymbolData getSymbolData(boolean giveAmbigException, String className, Data enclosingData, JExpression jexpr, 
                                  boolean giveException) {
    Data d = enclosingData;
    SymbolData result = null;
    while (d != null && result == null) {
      result = enclosingData.getInnerClassOrInterface(className);
      d = d.getOuterData();
    }
      
    if (result == null) result = getSymbolData(className, jexpr, giveException, true);
    
    else if (result == SymbolData.AMBIGUOUS_REFERENCE) {
      if (giveAmbigException) {
        _addError("Ambiguous reference to class or interface " + className, jexpr); 
        return SymbolData.AMBIGUOUS_REFERENCE;
      }
      return null;  // return null to indicate we were unable to resolve this.
    }
    if (result == null || ! giveException) return result;
    if (checkAccess(jexpr, result.getMav(), className, result, enclosingData.getSymbolData(), "class or interface")) {
      return result;
    }
    else return result;

  }

  /** Returns the SymbolData corresponding to the name className, assuming that className
    * does not refer to an unqualified or partially-qualified inner class.
    * */
  public SymbolData getSymbolData(String className, JExpression jexpr, boolean giveException, boolean runnableNotOkay) {
    // Check qualified class name (which is no different at elementary level)
    SourceInfo si = jexpr.getSourceInfo();
    
    // Create a dummy LLV; this seems awkward.  TODO:  refactor
    LanguageLevelVisitor llv = 
      new LanguageLevelVisitor(_file, 
                               _package,
                               null,  // enclosing class for top level traversal
                               _importedFiles, 
                               _importedPackages, 
                               new HashSet<String>(), 
                               new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                               new LinkedList<Command>(),
                               new HashMap<String, SymbolData>());

    LanguageLevelConverter._newSDs.clear();
    assert LanguageLevelConverter.symbolTable.containsKey("java.lang.Object");
    SymbolData sd = llv.getSymbolData(className, si, false, true); // TODO: Is this right?
//    if (sd == null) {
//      System.err.println("***ALARM*** The following symbol was not found in symbolTable: " + className);
//    }
//    else if (sd.getName().equals("java.lang.Throwable"))
//          System.err.println("*** Package for retrieved java.lang.Throwable is " + sd.getPackage());
    if (sd == null || sd.isContinuation()) {
      if (giveException) { _addError("Class or variable " + className + " not found.", jexpr); }
      return null;
    }
    else {
      //Check to see that sd is really in the package it should be in.
      if (notRightPackage(sd)) {
        _addError("The class " + sd.getName() + " is not in the right package. Perhaps you meant to package it?", 
                  jexpr);
      }
      
      if (runnableNotOkay) {
        if (sd.implementsRunnable()) {
          _addError(sd.getName() + " implements the Runnable interface, which is not allowed at any language level", 
                    jexpr);
          return null;
        }
      }
      return sd;
    }
  }
   
  /** Return false if the symbolData is in the wrong package.
    * @param sd  The SymbolData to check.
    */
  protected boolean notRightPackage(SymbolData sd) {
    if (sd.getOuterData() != null) { // if this is an inner class, check its outer data.
      return notRightPackage(sd.getOuterData().getSymbolData());
    }
    return (sd.getPackage().equals("") && sd.getName().lastIndexOf('.') != -1) || 
      ! sd.getName().startsWith(sd.getPackage());
  }
  
  /** Create a clone of the linked list of Variable Data. */
  public LinkedList<VariableData> cloneVariableDataList(LinkedList<VariableData> vars) {
    LinkedList<VariableData> nv = new LinkedList<VariableData>();
    for (int i = 0; i<vars.size(); i++) {
      VariableData old = vars.get(i);
      nv.addLast(old);
    }
    return nv;
  }
  
  /** The Qualified Class Name is the package, followed by a dot, followed by the rest of the class name.
    * If the provided className is already qualified, just return it.  If the package is not empty,
    * and the className does not start with the package, append the package name onto the className, and return it.
    * @param className  The className to qualify.
    */
  protected String getQualifiedClassName(String className) {
    if (! _package.equals("") && ! className.startsWith(_package)) { return _package + '.' + className;}
    else return className;
  }
  
  /** Check to see if the two symbol datas are in the same package.*/
  private static boolean _areInSamePackage(SymbolData enclosingSD, SymbolData thisSD) {
    String enclosingSDName = enclosingSD.getName();
    int lastIndexOfDot = enclosingSDName.lastIndexOf('.');
    if (lastIndexOfDot != -1) {

      // Check for inner class names:
      if (enclosingSD.getOuterData() != null) {
        return _areInSamePackage(enclosingSD.getOuterData().getSymbolData(), thisSD);
      }
      
      enclosingSDName = enclosingSDName.substring(0, lastIndexOfDot);
    }
    else { enclosingSDName = ""; }
    String thisSDName = thisSD.getName();
    lastIndexOfDot = thisSDName.lastIndexOf('.');
    if (lastIndexOfDot != -1) {
      
      //check for inner class names:
      if (thisSD.getOuterData() != null) {
          return _areInSamePackage(enclosingSD, thisSD.getOuterData().getSymbolData());
      }

      thisSDName = thisSDName.substring(0, lastIndexOfDot);
    }
    else { thisSDName = ""; }
    return enclosingSDName.equals(thisSDName);
  }
  

  /** Pass a default value*/
  protected MethodData _lookupMethodHelper(String methodName, SymbolData enclosingSD, InstanceData[] arguments, 
                                           JExpression jexpr, boolean isConstructor, SymbolData thisSD) {
    return _lookupMethodHelper(methodName, enclosingSD, arguments, jexpr, isConstructor, thisSD, 
                               new LinkedList<MethodData>());
  }
  
  /** Finds and returns all matching methods.  Assumes that the correct SymbolData is passed in.
    * @param methodName  The name of the method.
    * @param enclosingSD  The SymbolData we're currently searching for the method.
    * @param arguments  The types of the arguments to the method.
    * @param jexpr  The JExpression for the method invocation used in the error message.
    * @param isConstructor  True if this method is a constructor.  If so, we know it must be in the initial SymbolData.
    * @param thisSD  The SymbolData whence the method is invoked.
    * @return  The pair of matching MethodData lists: without autoboxing and with autoboxing.
    * An error is added if it is not found.
    */  
  protected Pair<LinkedList<MethodData>, LinkedList<MethodData>> 
    _getMatchingMethods(String methodName, SymbolData enclosingSD, InstanceData[] arguments, JExpression jexpr, 
                        boolean isConstructor, SymbolData thisSD) {
    LinkedList<MethodData> mds = enclosingSD.getMethods();
    Iterator<MethodData> iter = mds.iterator();
    LinkedList<MethodData> matching = new LinkedList<MethodData>();
    LinkedList<MethodData> matchingWithAutoBoxing = new LinkedList<MethodData>();
//    if (enclosingSD.getName().equals("NonEmpty"))
//      System.err.println("Starting search for method " + methodName + " in " + enclosingSD);
    while (iter.hasNext()) {
      MethodData md = iter.next();
//      System.err.println("Testing method " + md.getName());
//      if (md.getName().equals("NonEmpty")) {
//        System.err.println("*** for NonEmpty(), params length = " + md.getParams().length + "; args length = " 
//                             + arguments.length);
//      }
      // Check that the names match.
      if (md.getName().equals(methodName) && md.getParams().length == arguments.length) {
        VariableData[] vds = md.getParams();
        int i;
        boolean matches = true;
        
        // First check to see if any methods exist that match the invocation without using autoboxing
        for (i = 0; i < vds.length && i < arguments.length; i++) {
          matches = matches && 
            _isAssignableFromWithoutAutoboxing(vds[i].getType().getSymbolData(), arguments[i].getSymbolData());
          if (matches == false) break;
        }
        
        // if all arguments checked out, check the access stuff.
        if (matches && checkAccess(jexpr, md.getMav(), md.getName(), enclosingSD, thisSD, "method")) {
          matching.addLast(md);
        }

        if (matches == false) { // Didn't match the method directly; try to match it with autoboxing
//          if (enclosingSD.getName().equals("NonEmpty")) {
//            System.err.println("*** Looking for autoboxing match for NonEmpty");
//            System.err.println("vds = " + Arrays.toString(vds) + " arguments = " + Arrays.toString(arguments));
//          }
          matches = true;
          // Now check to see if any methods exist that match the invocation while using autoboxing.
          for (i = 0; i < vds.length && i < arguments.length; i++) {
            if  (enclosingSD.getName().equals("NonEmpty")) {
              SymbolData parmSD = vds[i].getType().getSymbolData();
//              System.err.println("vds[" + i + "].getType().getSymbolData() = " + parmSD);
              SymbolData argSD = arguments[i].getSymbolData();
//              System.err.println("arguments[" + i + "].getSymbolData() = " + argSD);
              if (argSD.equals(SymbolData.INT_TYPE) && parmSD.equals(symbolTable.get("java.lang.Object")))
                assert _isAssignableFrom(parmSD, argSD);
            }
            matches = matches && _isAssignableFrom(vds[i].getType().getSymbolData(), arguments[i].getSymbolData());
            if (matches == false) {
              if (enclosingSD.getName().equals("NonEmpty"))
//                System.err.println("No match found for NonEmpty using autoboxing");
              break;
            }
          }

          // if all arguments checked out, check the access stuff.
          if (matches && checkAccess(jexpr, md.getMav(), md.getName(), enclosingSD, thisSD, "method")) {
            matchingWithAutoBoxing.addLast(md);
          }
        }
      }
    }

    if (! isConstructor) {
      for (SymbolData sup : enclosingSD.getInterfaces()) {
        Pair<LinkedList<MethodData>, LinkedList<MethodData>> p = 
          _getMatchingMethods(methodName, sup, arguments, jexpr, isConstructor, thisSD);
        matching.addAll(p.getFirst());
        matchingWithAutoBoxing.addAll(p.getSecond());
      }
      if (enclosingSD.getSuperClass() != null) {
        Pair<LinkedList<MethodData>, LinkedList<MethodData>> p = 
          _getMatchingMethods(methodName, enclosingSD.getSuperClass(), arguments, jexpr, isConstructor, thisSD);
        matching.addAll(p.getFirst());
        matchingWithAutoBoxing.addAll(p.getSecond());
      }
    }
//    if (methodName.equals("NonEmpty")) {
//      System.err.println("***** enclosingSD = " + enclosingSD + "; thisSD = " + thisSD + "; matching methods: " 
//                           + matching);
//      System.err.println("***** matching methods with autoboxing: " + matchingWithAutoBoxing);
//    }
    
    return new Pair<LinkedList<MethodData>, LinkedList<MethodData>> (matching, matchingWithAutoBoxing);
  }
      
  /** Finds which SymbolData this method is in, beginning at this SymbolData and recursively visiting super classes.
    * @param methodName  The name of the method.
    * @param enclosingSD  The SymbolData we're currently searching for the method.
    * @param arguments  The types of the arguments to the method.
    * @param jexpr  The JExpression for the method invocation used in the error message.
    * @param isConstructor  Tells us if this method is a constructor. If so, we know it must be in the initial SymbolData.
    *                       We assume that the correct SymbolData was passed in.
    * @param thisSD  The SymbolData whence the method is invoked.
    * @return  The SymbolData where we find the method of null if it was not found.  An error is added if it is not found.
    */  
  protected MethodData _lookupMethodHelper(String methodName, SymbolData enclosingSD, InstanceData[] arguments, 
                                           JExpression jexpr, boolean isConstructor, SymbolData thisSD, 
                                           LinkedList<MethodData> matchingMethods) {
    Pair<LinkedList<MethodData>, LinkedList<MethodData>> p = _getMatchingMethods(methodName, enclosingSD, arguments, 
                                                                                 jexpr, isConstructor, thisSD);
    LinkedList<MethodData> matching = p.getFirst();
    LinkedList<MethodData> matchingWithAutoBoxing = p.getSecond();
    
    // if this is not a constructor, matching and matchingWithAutoBoxing are both empty, and there is a outer data,
    // then look at outer data
    SymbolData currData = enclosingSD;
    while (!isConstructor && matching.isEmpty() && matchingWithAutoBoxing.isEmpty() && 
           currData.getOuterData() != null) {
      currData = currData.getOuterData().getSymbolData();
      p = _getMatchingMethods(methodName, currData, arguments, jexpr, isConstructor, thisSD);
      matching = p.getFirst();
      matchingWithAutoBoxing = p.getSecond();
    }
    
    if (matching.size() == 1) return matching.getFirst();
    if (matching.size() > 1)  return selectMostSpecificMethod(matching, arguments, jexpr);
    if (matchingWithAutoBoxing.size() == 1) return matchingWithAutoBoxing.getFirst();
    if (matchingWithAutoBoxing.size() > 1) return selectMostSpecificMethod(matchingWithAutoBoxing, arguments, jexpr);

    //if nothing matched at all, return null
    return null;
  }
  
  /** Finds which SymbolData this method is in, beginning at this SymbolData and recursively visiting super classes.
    * Adds an error if the method is not found.
    * @param methodName  The name of the method.
    * @param enclosingSD  The SymbolData we're currently searching for the method.
    * @param arguments  The instance types of the arguments to the method.
    * @param jexpr  The JExpression for the method invocation used in the error message.
    * @param errorMsg  The error to be displayed if the method cannot be found.
    * @param isConstructor  True if this method is a constructor. If so, we know it must be in the initial SymbolData.
    *                       We assume that the correct SymbolData was passed in.
    * @param thisSD  The SymbolData of the initial SymbolData.
    * @return  The SymbolData containging the method of null if it was not found.  An error is added if it is not found.
    */
  protected MethodData _lookupMethod(String methodName, SymbolData enclosingSD, InstanceData[] arguments, 
                                     JExpression jexpr, String errorMsg, boolean isConstructor, SymbolData thisSD) {
//    if (enclosingSD.isContinuation() && jexpr != null) {
//      SymbolData newSD = getSymbolData(enclosingSD.getName(), enclosingSD, jexpr);
//      if (newSD != null) {
//        System.err.println("Replacing " + enclosingSD + " by " + newSD);
//        if (methodName == "getName()" || methodName == "getName") {
//          System.err.println("The methods of " + enclosingSD + " are: \n" + enclosingSD.getMethods());
//          throw new UnexpectedException("Computation aborted");
////          System.err.println("The methods of " + newSD + " are: \n" + newSD.getMethods());
//        }
//        enclosingSD = newSD;
//      }
//    }
    if (! isConstructor && methodName.equals(LanguageLevelVisitor.getUnqualifiedClassName(enclosingSD.getName()))) {
      _addError("The keyword 'new' is required to invoke a constructor", jexpr);
    }
    
    MethodData md = _lookupMethodHelper(methodName, enclosingSD, arguments, jexpr, isConstructor, thisSD);
    if (md != null) {
      return md;
    }
    // None of the methods matched the signature.
    StringBuffer message = new StringBuffer(errorMsg + methodName);
    message.append("(");
    if (arguments.length > 0) {
      message.append(arguments[0].getName());
      for (int i = 1; i < arguments.length; i++) {
        message.append(", " + arguments[i].getName());
      }
    }
    message.append(").");
    _addError(message.toString(), jexpr);
    // If not found, return null and have the caller check for it.
    return null;
  }
  
  /** Assumes that all methods in list have the same name and the same number of arguments.  Selects the method with 
    * the most specific signature.  Assumes that each method does not require the application of any 1.5+ specific 
    * features.
    * @param list  The list of methods used 
    * @return  The most specific method among the methods in the given list
    */
  private static MethodData selectMostSpecificMethod(List<MethodData> list, InstanceData[] arguments, 
                                                         JExpression jexpr) {
    if (list.isEmpty()) return null;
    Iterator<MethodData> it = list.iterator();
    MethodData best = it.next();
    MethodData ambiguous = null; // there is no ambiguous other method at first
    while (it.hasNext()) {
      MethodData curr = it.next();
      SymbolData[] bestParams = new SymbolData[best.getParams().length];
      SymbolData[] currParams = new SymbolData[curr.getParams().length];
      
      boolean better1 = false; // whether 'best' is better than 'curr'
      boolean better2 = false; // whether 'curr' is better than 'best'
      for (int i = 0; i < bestParams.length; i++) {
        SymbolData bp = best.getParams()[i].getType().getSymbolData();
        SymbolData cp = curr.getParams()[i].getType().getSymbolData();
        boolean fromCurrToBest = cp.isAssignableTo(bp, LanguageLevelConverter.OPT.javaVersion());
        boolean fromBestToCurr = bp.isAssignableTo(cp, LanguageLevelConverter.OPT.javaVersion());
        bestParams[i] = bp;
        currParams[i] = cp;
                  
        if (fromBestToCurr && ! fromCurrToBest) { // best's parameter[i] is more specific than curr's
          better1 = true; // so best is better than curr
        }
        if (fromCurrToBest && ! fromBestToCurr) { // curr's parameter[i] is more specific than best's
          better2 = true; // so curr is better than best
        }
      }
      
      // decide which is more specific or whether they are ambiguous
      if (better1 == better2) { // neither is better than the other
        // Handle overridden methods
        if (Arrays.equals(bestParams, currParams)) {
          SymbolData c1 = best.getSymbolData();
          SymbolData c2 = curr.getSymbolData();
          boolean c1IsSuperOrSame = c2.isAssignableTo(c1, LanguageLevelConverter.OPT.javaVersion());
          boolean c2IsSuperOrSame = c1.isAssignableTo(c2, LanguageLevelConverter.OPT.javaVersion());
          if (c1IsSuperOrSame && !c2IsSuperOrSame) { // c2 is more specific
            best = curr;
            continue;
          }
          else if (c2IsSuperOrSame && !c1IsSuperOrSame) { // c1 is more specific
            continue;
          }
        }
        ambiguous = curr;
      }
      else if (better2) {
        best = curr;
        ambiguous = null; // no more ambiguity
      }
    }
    if (ambiguous != null) {
      StringBuffer invokeArgs = new StringBuffer("(");
      StringBuffer ambigArgs = new StringBuffer("(");
      StringBuffer bestArgs = new StringBuffer("(");
      for (int i = 0; i<arguments.length; i++) {
        if (i>0) {
          invokeArgs.append(", ");
          ambigArgs.append(", ");
          bestArgs.append(", ");
        }
        invokeArgs.append(arguments[i].getSymbolData().getName());
        ambigArgs.append(ambiguous.getParams()[i].getType().getSymbolData().getName());
        bestArgs.append(best.getParams()[i].getType().getSymbolData().getName());
      }
      invokeArgs.append(")");
      ambigArgs.append(")");
      bestArgs.append(")");
      _addError(best.getName() + invokeArgs.toString() + " is an ambiguous invocation.  It matches both " 
                  + best.getName() + bestArgs.toString() + " and " + ambiguous.getName() + ambigArgs.toString(), 
                jexpr);
    }
    return best;
  }
  
  
  /**
   * Checks that the method is accessible given the SymbolData it's in and the current SymbolData.
   * We have already checked the modifiers for validity.
   * This will also work when checking the accessibility of static inner classes by passing in the
   * static inner class for enclosingSD.
   * @param mav  The modifiers and visibility of the thing we're checking
   * @param name  The name of the thing we're checking
   * @param enclosingSD  The SymbolData which encloses name
   * @param thisSD  The SymbolData that is being type checked (from which we're referencing name)
   * @param dataType  The String that tells us whether name is a field, method, or class.
   */
  public static boolean checkAccess(JExpression piece, ModifiersAndVisibility mav, String name, 
                                    SymbolData enclosingSD, SymbolData thisSD, String dataType) {
    return checkAccess(piece, mav, name, enclosingSD, thisSD, dataType, true);
  }


  /** Call this version when you don't want to add an error--only take in what is necessary to do the check, give default
    * values for anything that will not be used.
    */
  public static boolean checkAccess(ModifiersAndVisibility mav, SymbolData enclosingSD, SymbolData thisSD) { 
    return checkAccess(NULL_LITERAL, mav, "", enclosingSD, thisSD, "", false);
  }

  /** Determines if thisSD can reference specified name defined in enclosingSD.  Empty symbol is OK. See preceding 
    * method body. 
    * @param mav ??
    */
  public static boolean checkAccess(JExpression piece, ModifiersAndVisibility mav, String name, 
                                    SymbolData enclosingSD, SymbolData thisSD, String dataType, boolean addError) {
//    if (piece instanceof VariableReference) System.err.println("**** Checking access of " + piece + "\nin " + enclosingSD);
    if (thisSD.isOuterData(enclosingSD) || enclosingSD.isOuterData(thisSD) || thisSD.equals(enclosingSD)) {
      return true;
    }

    // Check for the public modifier.
    if (Utilities.isPublic(mav)) {
      Data enclosingOuter = enclosingSD.getOuterData();
      if (enclosingOuter == null) return true;
      if (enclosingOuter instanceof SymbolData) {
        return checkAccess(piece, mav, name, enclosingSD.getOuterData().getSymbolData(), thisSD, dataType, addError); 
      }
      throw new RuntimeException("Internal Program Error: Trying to reference " + name + 
                                 "which is a member of something other than a class from outside of that thing.  " +
                                 "Please report this bug.");
    }
    
    // Check for the private modifier.
    if (Utilities.isPrivate(mav)) {
      /* As long as one of the symbol datas is the outer data of the other one (at some point down the data chain), 
       * private classes/methods/fields can be seen. Since we would have already returned, just throw the error and 
       * return false. */
//      Utilities.show(thisSD + " cannot access symbol '"  + name + "' within " + enclosingSD + " from '" + thisSD 
//                       + "' in file " + piece.getSourceInfo().getFile());
//      if (name.equals("evalVisitor")) throw new UnexpectedException("evalVisitor BUG");
      String nameWithDots = Data.dollarSignsToDots(name);
      String enclosingWithDots = Data.dollarSignsToDots(enclosingSD.getName());
      
      // The following is a kludge to eliminate SOME spurious results.
      if (nameWithDots.equals(enclosingWithDots) && enclosingSD.isPrimitiveType()) return true;
          
      String siteName = Data.dollarSignsToDots(thisSD.getName());
      if (addError) { 
        _addError("The " + dataType + " " + nameWithDots + " in " + enclosingWithDots +
                  " is private and cannot be accessed from " + siteName, 
                  piece);
//        throw new UnexpectedException("Generate Debugging Trace for access failure to " + nameWithDots + " in " 
//                                        + enclosingWithDots + " from " + siteName);  // DEBUG
      }
//      Utilities.show("enclosingSD = " + enclosingSD + " thisSD = " + thisSD);
      return false;
    }
    
    // Check for the protected modifier.
    if (Utilities.isProtected(mav)) {
      // Compare the package names. Remember that inner classes have '$' in their names.  NO! TODO: Fix this !!!
      if (_areInSamePackage(enclosingSD, thisSD)) {
        return true;
      }
      // Check if thisSD is a subclass of enclosingSD.
      // If they are in the same file, they are in the same package, so this does not need to check outer classes, 
      // only super classes and interfaces.
      if (thisSD.isSubClassOf(enclosingSD)) {
        return true;
      }
      else {
        if (addError) _addError("The " + dataType + " " + Data.dollarSignsToDots(name) + 
                                " is protected and cannot be accessed from " + Data.dollarSignsToDots(thisSD.getName()), 
                                piece);
        return false;
      }
    }

    // Check the default "package modifier"
    if (_areInSamePackage(enclosingSD, thisSD)) return true;
    else {
      if (addError) _addError("The " + dataType + " " + Data.dollarSignsToDots(name) + 
                              " is package protected because there is no access specifier and cannot be accessed from " 
                                + Data.dollarSignsToDots(thisSD.getName()), 
                              piece); 
      return false;
    }
  }
  
  /** Return the field or variable with the name text inside of data.  (Referenced from thisSD) */
  public static VariableData getFieldOrVariable(String text, Data data, SymbolData thisSD, JExpression piece) {
    if (data == null) return null;
    return getFieldOrVariable(text, data, thisSD, piece, data.getVars(), true, true);
  }
  
  public static VariableData getFieldOrVariable(String text, Data data, SymbolData thisSD, JExpression piece, 
                                                LinkedList<VariableData> vars) {
    return getFieldOrVariable(text, data, thisSD, piece, vars, false, true);
  }  
  
  
  public static VariableData getFieldOrVariable(String text, Data data, SymbolData thisSD, JExpression piece, 
                                                LinkedList<VariableData> vars, boolean shouldRecur) {
    return getFieldOrVariable(text, data, thisSD, piece, vars, shouldRecur, true);
  }
  
  /** This method checks if the identifier called text is a field or variable visible from the context of the param 
    * "data" and is accessible from the context of thisSD.  This will search through all of "data"'s enclosing classes 
    * (outer class, superclass, interfaces) recursively.  This could mean a lot of recursion...
    * Returns null if the field or variable is not found.
    */
  public static VariableData getFieldOrVariable(String text, Data data, SymbolData thisSD, JExpression piece, 
                                                LinkedList<VariableData> vars, boolean shouldRecur, boolean addError) {
    VariableData vd = null;
    if (data == null) return null;
    
    Iterator<VariableData> iter = vars.iterator();
    while (iter.hasNext()) {
      vd = iter.next();
      if (vd != null) {
        if (vd.getName().equals(text)) {
          if (vd.getEnclosingData() instanceof MethodData && addError) {  // was BodyData
            /* If vd is defined in a method and thisSD is a local class or anonymous inner class defined in data, then 
             * vd must be final to be used.  Note: the enclosingData of formal parameters is the enclosing class not
             * the corresponding method.
             */
            if (thisSD.isOuterData(vd.getEnclosingData())) {
              if (! vd.isFinal() && addError) {
                _addError("Local variable " + vd.getName() 
                            + " is accessed from within an inner class; must be declared final", piece);
              }
            }
          }
          if (addError) {
            checkAccess(piece, vd.getMav(), vd.getName(), vd.getEnclosingData().getSymbolData(), thisSD, 
                        "field or variable");
          }
          return vd;
        }
      }
    }
    
    if (shouldRecur) {
      /* The enclosingData should contain the super class, then the interfaces, and then the
       * outer class. */
      
      Iterator<Data> enclosingData = data.getEnclosingData().iterator();
      while (enclosingData.hasNext()) {
        Data outerData = enclosingData.next();
        if (outerData == null) return null;
        vd = getFieldOrVariable(text, outerData, thisSD, piece, outerData.getVars(), true, addError);
        if (vd != null) return vd;
      }
    }
    return null;
  }

  /** Return true iff type is an instance type.  If not, add an error. */
  public boolean assertInstanceType(TypeData type, String errorMsg, JExpression expression) {
    if (! type.isInstanceType()) {
      _addError(errorMsg + ".  Perhaps you meant to create a new instance of " + type.getName(), expression);
      return false;
    }
    return true;
  }
  
  /** If type is a PackageData, then it could not be resolved.  Give an error*/
  public boolean assertFound(TypeData type, JExpressionIF expression) {
    if (type instanceof PackageData) {
      _addError("Could not resolve symbol " + type.getName(), expression);
      return false;
    }
    return true;
  }

  /** Adds an error pair consisting of the specified String message and JExpression. Sets _errorAdded to true.
    * @param message  Error message
    * @param that  The JExpression corresponding to where the error is.
    */
  protected static void _addError(String message, JExpressionIF that) {
    _errorAdded = true;
    Pair<String, JExpressionIF> p = new Pair<String, JExpressionIF>(message, that);
    if (! errors.contains(p)) errors.addLast(p);
  }
  
  /** Return a TypeData array of the specified size. */
  protected TypeData[] makeArrayOfRetType(int len) { return new TypeData[len]; }

  /** This method is called by default from cases that do not 
    * override forCASEOnly.
    **/
  protected TypeData defaultCase(JExpressionIF that) { return null; }

  public TypeData forClassDefOnly(ClassDef that, TypeData mavRes, TypeData nameRes, TypeData[] typeParametersRes,
                                  TypeData superRes, TypeData[] interfacesRes, TypeData bodyRes) {
    return forJExpressionOnly(that);//forTypeDefBaseOnly(that, mavRes, nameRes, typeParametersRes);
  }

  public TypeData forInnerClassDefOnly(InnerClassDef that, TypeData mavRes, TypeData nameRes, TypeData[] typeParamRes, 
                                       TypeData superClassRes, TypeData[] interfacesRes, TypeData bodyRes) {
    return forJExpressionOnly(that);
    // replaces forClassDefOnly(that, mavRes, nameRes, typeParamRes, superClassRes, interfacesRes, 
    //                          bodyRes);
  }

  public TypeData forInterfaceDefOnly(InterfaceDef that, TypeData mavRes, TypeData nameRes, 
                                      TypeData[] typeParamRes, TypeData[] superinterfacesRes, TypeData bodyRes) {
    return forJExpressionOnly(that); //forTypeDefBaseOnly(that, mavRes, nameRes, typeParamRes);
  }
  
  public TypeData forInnerInterfaceDefOnly(InnerInterfaceDef that, TypeData mavRes, TypeData nameRes, 
                                           TypeData[] typeParamRes, TypeData[] superinterfacesRes, TypeData bodyRes) {
    return forJExpressionOnly(that);
    // replaces forInterfaceDefOnly(that, mavRes, nameRes, typeParamRes, superinterfacesRes, bodyRes);
  }

  public TypeData forInstanceInitializerOnly(InstanceInitializer that, TypeData codeRes) {
    return forJExpressionOnly(that);//forInitializerOnly(that, codeRes);
  }

  public TypeData forStaticInitializerOnly(StaticInitializer that, TypeData codeRes) {
    return forJExpressionOnly(that);//InitializerOnly(that, codeRes);
  }

  public TypeData forLabeledStatementOnly(LabeledStatement that, TypeData statementRes) {
    return forJExpressionOnly(that);//StatementOnly(that);
  }

  public TypeData forBlockOnly(Block that, TypeData[] statementsRes) {
    return forJExpressionOnly(that);//StatementOnly(that);
  }

  public TypeData forExpressionStatementOnly(ExpressionStatement that, TypeData exprRes) {
    return forJExpressionOnly(that);//StatementOnly(that);
  }

  public TypeData forSwitchStatementOnly(SwitchStatement that, TypeData testRes, TypeData[] casesRes) {
    return forJExpressionOnly(that);//tatementOnly(that);
  }
  
  public TypeData forBreakStatementOnly(BreakStatement that) {
    // returns a sentinel value that tells us that this is a break or continue statement.
    return SymbolData.NOT_FOUND;
  }

  public TypeData forContinueStatementOnly(ContinueStatement that) {
    // returns a sentinel value that tells us that this is a break or continue statement.
    return SymbolData.NOT_FOUND;
  }

  public TypeData forReturnStatementOnly(ReturnStatement that) {
    return forJExpressionOnly(that);//StatementOnly(that);
  }

  public TypeData forTryCatchStatementOnly(TryCatchStatement that, TypeData tryBlockRes, 
                                           TypeData[] catchBlocksRes) {
    return forJExpressionOnly(that);//StatementOnly(that);
  }

  public TypeData forMethodDefOnly(MethodDef that, TypeData mavRes, TypeData[] typeParamsRes, 
                                   TypeData resRes, TypeData nameRes, TypeData paramsRes, 
                                   TypeData[] throwsRes) {
    return resRes; //forJExpressionOnly(that);
  }
  public TypeData forConcreteMethodDefOnly(ConcreteMethodDef that, TypeData mavRes, TypeData[] typeParamsRes, 
                                           TypeData resRes, TypeData nameRes, TypeData paramsRes, 
                                           TypeData[] throwsRes, TypeData bodyRes) {
    return forMethodDefOnly(that, mavRes, typeParamsRes, resRes, nameRes, paramsRes, 
                            throwsRes);
  }
  
  /** Returns the common super type of the two types provided.*/
  protected SymbolData getCommonSuperTypeBaseCase(SymbolData sdLeft, SymbolData sdRight) {
    if (sdLeft == SymbolData.EXCEPTION) { return sdRight; }
    if (sdRight == SymbolData.EXCEPTION) { return sdLeft; }
    if (_isAssignableFrom(sdLeft, sdRight)) {return sdLeft;}
    if (_isAssignableFrom(sdRight, sdLeft)) {return sdRight;}
    return null;
  }
  
  /** Return whether the value on the right can be assigned to the value on the left. 
    * Uses autoboxing if the user has java 1.5.
    */
  protected static boolean _isAssignableFrom(SymbolData sdLeft, SymbolData sdRight) {
    if (sdRight == null) return false;
//    System.err.println("Java Version = " + LanguageLevelConverter.OPT.javaVersion());
    assert LanguageLevelConverter.versionSupportsAutoboxing(LanguageLevelConverter.OPT.javaVersion());
    return sdRight.isAssignableTo(sdLeft, LanguageLevelConverter.OPT.javaVersion());
  }
  
  /** Return whether the value on the right can be assigned to the value on the left.  Does not use autoboxing. */
  protected boolean _isAssignableFromWithoutAutoboxing(SymbolData sdLeft, SymbolData sdRight) {
    
    if (sdRight == null) { return false; }
    return sdRight.isAssignableTo(sdLeft, JavaVersion.JAVA_1_4);  // Version 1.4 used to turn off autoboxing
  }

  /**
   * The method will add an error for each abstract method in the current SymbolData's inheritance hierarchy
   * that does not have a concrete implementation.
   * @param sd  The SymbolData for the current class definition
   * @param classDef  The current ClassDef
   */
  protected void _checkAbstractMethods(SymbolData sd, JExpression classDef) {
    if (sd.hasModifier("abstract") || sd.isInterface()) {
      // Our work here is done because an abstract class has no constraints to implement.
      return;
    }
    LinkedList<MethodData> mds = sd.getMethods();
    
    //add all concrete methods from our super classes--this is to fix the case where you and your super class extend
    //the same interface, and your super class concretely implements the methods--you do not have to also implement them.
    //there are other generalizations of this case.
    //TODO: Consider optimizing this--it may be slow.
    LinkedList<MethodData> cmds = _cloneMethodDataList(mds);
    SymbolData superD = sd.getSuperClass();
    while (superD != null && !superD.getName().equals("java.lang.Object")) {
      LinkedList<MethodData> smds = superD.getMethods();
      for (MethodData md: smds) {
        if (!md.hasModifier("abstract")) {// && SymbolData.repeatedSignature(cmds, md, false) == null) {
          cmds.addLast(md);
        }
      }
      superD = superD.getSuperClass();
    }
    
    // TODO: Check all enclosingDatas (interfaces too).
    _checkAbstractMethodsHelper(sd, sd.getSuperClass(), cmds, classDef);
    //check interfaces as well:
    for (SymbolData iSD: sd.getInterfaces()) {
      _checkAbstractMethodsHelper(sd, iSD, cmds, classDef);
    }
  }
  
  /**
   * This checks a superclass of the original SymbolData.  If this class is not abstract, then we are
   * done because this concrete class must have implemented all the abstract methods above it.
   * @param origSd  The SymbolData for the current class definition
   * @param sd  The SymbolData for the class in origSd's superclass hierarchy that we're currently examining
   * @param concreteMds  The list of MethodDatas of concrete methods that we've seen so far
   * @param classDef  The original ClassDef
   */
  private void _checkAbstractMethodsHelper(SymbolData origSd, SymbolData sd, LinkedList<MethodData> concreteMds, 
                                           JExpression classDef) {
    if (sd == null || (!sd.hasModifier("abstract") && !sd.isInterface())) {
      return;
    }
    // Gets its abstract methods and make sure they're in concreteMds.
    LinkedList<MethodData> mds = sd.getMethods();
    Iterator<MethodData> iter = mds.iterator();
    while (iter.hasNext()) {
      MethodData md = iter.next();
      if (md.hasModifier("abstract")) {
        // Check if this md is overridden by one of the MethodDatas in the list of concreteMds.
        MethodData matchingMd = SymbolData.repeatedSignature(concreteMds, md);
        if (matchingMd == null) {
          StringBuffer message;
          if (classDef instanceof AnonymousClassInstantiation) {
            message = new StringBuffer("This anonymous inner class must override the abstract method: " + md.getName());
          }
          else {
            message = new StringBuffer(origSd.getName() 
                                         + " must be declared abstract or must override the abstract method: " 
                                         + md.getName());
          }
          VariableData[] params = md.getParams();
          InstanceData[] arguments = new InstanceData[params.length];
          for (int i = 0; i < params.length; i++) {
            arguments[i] = params[i].getType().getInstanceData();
          }
          message.append("(");
          if (arguments.length > 0) {
            message.append(arguments[0].getName());
            for (int i = 1; i < arguments.length; i++) {
              message.append(", " + arguments[i].getName());
            }
          }
          message.append(") in " + sd.getName());
          _addError(message.toString(), classDef);
        }
      }
      else {
        // Update the list of concrete methods.
        concreteMds.addLast(md);
      }
    }
    //check the super class
    _checkAbstractMethodsHelper(origSd, sd.getSuperClass(), _cloneMethodDataList(concreteMds), classDef);
    
    //check the interfaces
    for (SymbolData iSD: sd.getInterfaces()) {
      _checkAbstractMethodsHelper(origSd, iSD, _cloneMethodDataList(concreteMds), classDef);
    }
  }
  
  /**Create a new LinkedList that holds the method datas in mds*/
  private LinkedList<MethodData> _cloneMethodDataList(LinkedList<MethodData> mds) {
    LinkedList<MethodData> toReturn = new LinkedList<MethodData>();
    for (int i = 0; i<mds.size(); i++) {
      toReturn.addLast(mds.get(i));
    }
    return toReturn;
  }
  
  /**
   * Make a copy of the provided symbol data list, and return that copy.
   * @param l  The LinkedList of SymbolDatas to copy.
   * @return  A copy of l.
   */
  private LinkedList<SymbolData> cloneSDList(LinkedList<SymbolData> l) {
    LinkedList<SymbolData> l2 = new LinkedList<SymbolData>();
    for (int i = 0; i< l.size(); i++) {
      l2.addLast(l.get(i));
    }
    return l2;
  }
  
  /**
   * Checks for cyclic inheritance by traversing sd's list of superclasses and interfaces and checking if we've
   * seen them before.  We accumulate the list of classes and interfaces we've seen before in hierarchy.
   * @param sd  The SymbolData we're currently checking
   * @param hierarchy  The list of classes and interfaces we've seen before.
   * @return  Whether there is cyclic inheritance
   */
  protected boolean checkForCyclicInheritance(SymbolData sd, LinkedList<SymbolData> hierarchy, TypeDefBase tdb) {
    if (sd==null || LanguageLevelVisitor.isJavaLibraryClass(sd.getName())) {
      // We can stop checking because these can't have cyclic inheritance
      return false;
    }

    
    if (hierarchy.contains(sd)) {
      _addError("Cyclic inheritance involving " + sd.getName(), tdb);
      return true;
    }
    
    //add current symbol data to the hierarchy
    hierarchy.addLast(sd);
    
    //Also add inner classes
    LinkedList<SymbolData> innerClasses = sd.getInnerClasses();
    for (int i = 0; i < innerClasses.size(); i++) {
      hierarchy.addLast(innerClasses.get(i));
    }
    
    LinkedList<SymbolData> clonedHierarchy = cloneSDList(hierarchy);
    boolean doReturn = checkForCyclicInheritance(sd.getSuperClass(), clonedHierarchy, tdb);
    
    //check the interfaces
    for (SymbolData iSD: sd.getInterfaces()) {
      doReturn |= checkForCyclicInheritance(iSD, clonedHierarchy, tdb);
    }
    
    return doReturn;
  }
  
  /**Do what is necessary to handle a class def */
  public TypeData forClassDef(ClassDef that) {
    
    SymbolData objectSD = getSymbolData("java.lang.Object", that, true, false);
    assert SymbolData.INT_TYPE.isAssignableTo(objectSD, LanguageLevelConverter.OPT.javaVersion());
  
    String className = getQualifiedClassName(that.getName().getText());
    SymbolData sd = getSymbolData(className, that, true, false);
    if (sd == null) {
//      System.err.println("****DISASTER****  sd is null for ClassDef " + className);
      _addError("The class " + className + " was never defined", that);
      _log.log("*********DISASTER****  sd is null for ClassDef " + className);
      return null;
    }
    // Check for cyclic inheritance
    if (checkForCyclicInheritance(sd, new LinkedList<SymbolData>(), that)) {
      return null;
    }
    
    // See if sd is public.  If so, the filename must match sd's name.
    if (sd.hasModifier("public")) {
      String fileName = className.replace('.', System.getProperty("file.separator").charAt(0));
      if (!_file.getAbsolutePath().endsWith(fileName + ".dj") && 
          !_file.getAbsolutePath().endsWith(fileName + ".dj0") && 
          !_file.getAbsolutePath().endsWith(fileName + ".dj1") && 
          !_file.getAbsolutePath().endsWith(fileName + ".dj2")) {
        _addError(className + " is public thus must be defined in a file with the same name.", that.getName());
      }
    }

    // Reset sd's anonymous inner class count so we can count again during this second pass.
    sd.setAnonymousInnerClassNum(0);

    // Make sure this class does not implement the Runnable interface
    if (sd.hasInterface(getSymbolData("java.lang.Runnable", that, false, false))) {
      _addError(sd.getName() + " implements the Runnable interface, which is not allowed at any language level", that);
    }

    SymbolData superClass = sd.getSuperClass();
    // make sure this class can see its super class
    if (superClass != null) {
      checkAccess(that.getSuperclass(), superClass.getMav(), superClass.getName(), superClass, sd, "class");
      //Also make sure that the superClass is not an interface!
      if (superClass.isInterface()) {
        _addError(superClass.getName() + " is an interface and thus cannot appear after the keyword 'extends' here.  "
                    + "Perhaps you meant to say 'implements'?", that);
      }
    }
    
    //make sure that the class def does not extend a final class.
    if (superClass != null && superClass.hasModifier("final")) {
      _addError("Class " + sd.getName() + " cannot extend the final class " + superClass.getName(), that);
      return sd;
    }
    
    final TypeData mavRes = that.getMav().visit(this);
    final TypeData nameRes = that.getName().visit(this);
    final TypeData[] typeParamRes = makeArrayOfRetType(that.getTypeParameters().length);
    for (int i = 0; i < that.getTypeParameters().length; i++) {
      typeParamRes[i] = that.getTypeParameters()[i].visit(this);
    }
    final TypeData superClassRes = that.getSuperclass().visit(this);
    final SymbolData[] interfacesRes = new SymbolData[that.getInterfaces().length];
    for (int i = 0; i < that.getInterfaces().length; i++) {
      interfacesRes[i] = getSymbolData(that.getInterfaces()[i].getName(), that.getInterfaces()[i], true, true);
      if (interfacesRes[i] != null) {
        //make sure this class can see the interfaces it implements
        checkAccess(that.getInterfaces()[i], interfacesRes[i].getMav(), interfacesRes[i].getName(), interfacesRes[i], sd, 
                    "interface");
        //Make sure that all of these are actually interfaces.
        if (!interfacesRes[i].isInterface()) {
          _addError(interfacesRes[i].getName() + 
                    " is not an interface and thus cannot appear after the keyword 'implements' here.  " +
                    "Perhaps you meant to say 'extends'?", that);
        }
        
      }
      else {
        throw new RuntimeException("Internal Program Error: getSymbolData( " + that.getInterfaces()[i].getName() + 
                                   ") returned null.  Please report this bug.");
      }
    }
    
    //See if sd is a test class.  If so, it must be public.
    SymbolData testSd = getSymbolData("junit.framework.TestCase", NULL_LITERAL, false, true);
    if (testSd != null && sd.isSubClassOf(testSd)) { 
      if (! sd.hasModifier("public")) {
        _addError(sd.getName() + " extends TestCase and thus must be explicitly declared public", that);
      }
      boolean foundOne = false;
      for (int i = 0; i < sd.getMethods().size(); i++) {
        MethodData myMd = sd.getMethods().get(i);
        if (myMd.getName().startsWith("test") && (myMd.getReturnType() == SymbolData.VOID_TYPE) && 
            myMd.hasModifier("public")) {
          foundOne = true;
          break;
        }
      }
      if (! foundOne) {
        _addError("Class " + sd.getName() + " does not have any valid test methods.  " +
                    "Test methods must be declared public, must return void, and must start with the word \"test\"", 
                  that); 
      }
    }
    
    ClassBodyTypeChecker cbtc = 
      new ClassBodyTypeChecker(sd, _file, _package, _importedFiles, _importedPackages, new LinkedList<VariableData>(), 
                               new LinkedList<Pair<SymbolData, JExpression>>());
    

    final TypeData bodyRes = that.getBody().visit(cbtc);
    
    // Make sure that sd's methods override all of its hierarchy's abstract methods.
    _checkAbstractMethods(sd, that);
    
    
    //only do this if there were no constructors: make sure that all final fields were given a value.
    if (! cbtc.hasConstructor) {
      LinkedList<VariableData> sdVars = sd.getVars();
      for (int i = 0; i<sdVars.size(); i++) {
        if (!sdVars.get(i).hasValue()) {
          _addError("The final field " + sdVars.get(i).getName() + " has not been initialized", that);
          return null;
        }
      }
    }

    // Unassign anything that got assigned in the scope of the class def.
    unassignVariableDatas(cbtc.thingsThatHaveBeenAssigned);
    
    return forClassDefOnly(that, mavRes, nameRes, typeParamRes, superClassRes, interfacesRes, bodyRes);
  }

  /**Do everything necessary to handle an interface*/
  public TypeData forInterfaceDef(InterfaceDef that) {
    String interfaceName = getQualifiedClassName(that.getName().getText());
    SymbolData sd = getSymbolData(interfaceName, that, true, false);
    
//    //See if sd is public.  If so, the filename must match sd's name.
    if (sd.hasModifier("public")) {
      String fileName = interfaceName.replace('.', System.getProperty("file.separator").charAt(0));
      if (!_file.getAbsolutePath().endsWith(fileName + ".dj") &&
          !_file.getAbsolutePath().endsWith(fileName + ".dj0") &&
          !_file.getAbsolutePath().endsWith(fileName + ".dj1") &&
          !_file.getAbsolutePath().endsWith(fileName + ".dj2")) {
        _addError(interfaceName + " is public thus must be defined in a file with the same name.", that.getName());
      }
    }
    
    // Check for cyclic inheritance
    if (checkForCyclicInheritance(sd, new LinkedList<SymbolData>(), that)) {
      return null;
    }
    
//    // Make sure that this does not extend the runnable interface
//    if (sd.hasInterface(getSymbolData("java.lang.Runnable", that, false, false))) {
//      _addError(sd.getName() + " extends the Runnable interface, which is not allowed at any language level", that);
//    }
    
    final TypeData mavRes = that.getMav().visit(this);
    final TypeData nameRes = that.getName().visit(this);
    final TypeData[] typeParamRes = makeArrayOfRetType(that.getTypeParameters().length);
    for (int i = 0; i < that.getTypeParameters().length; i++) {
      typeParamRes[i] = that.getTypeParameters()[i].visit(this);
    }
    final SymbolData[] interfacesRes = new SymbolData[that.getInterfaces().length];
    for (int i = 0; i < that.getInterfaces().length; i++) {
      //superinterfacesRes[i] = that.getInterfaces()[i].visit(this);
      interfacesRes[i]=getSymbolData(that.getInterfaces()[i].getName(), that.getInterfaces()[i], true, true);
      if (interfacesRes[i] != null) {
        //make sure this class can see the interfaces it implements
        checkAccess(that.getInterfaces()[i], interfacesRes[i].getMav(), interfacesRes[i].getName(), interfacesRes[i], sd, 
                    "interface");
        if (!interfacesRes[i].isInterface()) {
          _addError(interfacesRes[i].getName() + 
                    " is not an interface and thus cannot appear after the keyword 'extends' here", 
                    that);
        }
      }
      else {
        throw new RuntimeException("Internal Program Error: getSymbolData( " + that.getInterfaces()[i].getName() + 
                                   ") returned null.  Please report this bug.");
      }

    }
    InterfaceBodyTypeChecker ibtc = 
      new InterfaceBodyTypeChecker(sd, _file, _package, _importedFiles, _importedPackages, 
                                   new LinkedList<VariableData>(), new LinkedList<Pair<SymbolData, JExpression>>());
    final TypeData bodyRes = that.getBody().visit(ibtc);
    return forInterfaceDefOnly(that, mavRes, nameRes, typeParamRes, interfacesRes, bodyRes);
  }
  
  /** Retrieve the class being imported from the Symbol table, and make sure it is public. */
  public TypeData forClassImportStatement(ClassImportStatement that) {
    CompoundWord cWord = that.getCWord();
    Word[] words = cWord.getWords();
    
    StringBuffer name = new StringBuffer(words[0].getText());
    for (int i = 1; i < words.length; i++) {name.append('.' + words[i].getText());}
    //This makes sure the class can be imported and is in the right package.
    final TypeData cWordRes = getSymbolData(name.toString(), that, true, true);
    if (cWordRes != null) { 
      if (! cWordRes.hasModifier("public")) {
        _addError(cWordRes.getName() + " is not public, and thus cannot be seen here", that);
      }
    }
    return forClassImportStatementOnly(that, cWordRes);
  }
  
  /**
   * Make sure that the package being imported does not conflict with another class in the symbol table, since
   * we will not allow a package to be imported if it has the same name as a class.
   */
  public TypeData forPackageStatement(PackageStatement that) {
    //final SymbolData cWordRes = that.getCWord().visit(this);
    CompoundWord cWord = that.getCWord();
    Word[] words = cWord.getWords();
    
    StringBuffer nameBuff = new StringBuffer(words[0].getText());
    for (int i = 1; i < words.length; i++) nameBuff.append('.' + words[i].getText());
    String name = nameBuff.toString();

    /* It is sufficient to just use "get" directly from the symbolTable, becuase the first pass has resolved all 
     * package names as symbols (??), so if we were successful, they will already be in symbol table.  It is not 
     * necessary to check accessibility, because somewhere along the line, the package must have conflicted with
     * a visible class--all top level classes must be public or package protected. */
    if (symbolTable.get(name) != null) {
      _addError(name + " is not a allowable package name, because it conflicts with a class you have already defined", 
                that);
    }
    return null;
  }

  /** @return the appropriate type for a primitive type.*/
  public TypeData forPrimitiveType(PrimitiveType that) {
    String text = that.getName();
    if (text.equals("boolean")) {
      return SymbolData.BOOLEAN_TYPE;
    }
    else if (text.equals("char")) {
      return SymbolData.CHAR_TYPE;
    }
    else if (text.equals("byte")) {
      return SymbolData.BYTE_TYPE;
    }
    else if (text.equals("short")) {
      return SymbolData.SHORT_TYPE;
    }
    else if (text.equals("int")) {
      return SymbolData.INT_TYPE;
    }
    else if (text.equals("long")) {
      return SymbolData.LONG_TYPE;
    }
    else if (text.equals("float")) {
      return SymbolData.FLOAT_TYPE;
    }
    else if (text.equals("double")) {
      return SymbolData.DOUBLE_TYPE;
    }
    else {
      throw new RuntimeException("Internal Program Error: Not a legal primitive type: " + text 
                                   + ".  Please report this bug");
    }
  }  
  
  /**Visit the type of the cast expression as well as the value being cast*/
  public TypeData forCastExpression(CastExpression that) {
    final TypeData typeRes = that.getType().visit(this);
    final TypeData valueRes = that.getValue().visit(this);
    if (valueRes == null) {
      // An error occurred type-checking the value; return the expected type to
      // allow type-checking to continue.
      return typeRes;
    }
    return forCastExpressionOnly(that, typeRes, valueRes);
  }
    
  /**
   * Walk the provided list of VariableDatas, and set each one back to unassigned.
   * This is used to correct the changes to VariableDatas made when we change scope.
   * VariableDatas will only be in this list if they are assigned for the first time
   * in a context that we are now leaving.
   * @param toUnassign  A LinkedList of VariableDatas to set back to unassigned.
   */
  void unassignVariableDatas(LinkedList<VariableData> toUnassign) {
    for (int i = 0; i<toUnassign.size(); i++) {
      toUnassign.get(i).lostValue();
    }
  }
  
   /** Test the methods defined in the above class. */
  public static class TypeCheckerTest extends TestCase {
    
    private TypeChecker _btc;
    
    private SymbolData _sd1;
    private SymbolData _sd2;
    private SymbolData _sd3;
    private SymbolData _sd4;
    private SymbolData _sd5;
    private SymbolData _sd6;
    private SymbolData _object;
    
    public TypeCheckerTest() { this(""); }
    public TypeCheckerTest(String name) { super(name); }
    
    public void setUp() {
      errors = new LinkedList<Pair<String, JExpressionIF>>();
      LanguageLevelConverter.symbolTable.clear();
      LanguageLevelConverter.loadSymbolTable();

      _btc = new TypeChecker(new File(""), "", new LinkedList<String>(), new LinkedList<String>());
      LanguageLevelConverter.OPT = new Options(JavaVersion.JAVA_5, EmptyIterable.<File>make());
      _btc._importedPackages.addFirst("java.lang");
      _errorAdded = false;
      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _sd3 = new SymbolData("zebra");
      _sd4 = new SymbolData("u.like.emu");
      _sd5 = new SymbolData("");
      _sd6 = new SymbolData("cebu");
      
      _object = symbolTable.get("java.lang.Object");
      assert _object != null;
//      o.setIsContinuation(false);
//      o.setMav(_publicMav);
//      symbolTable.put("java.lang.Object", o);
      
      assert symbolTable.get("java.lang.Double") != null;
//      Double.setIsContinuation(false);
//      Double.setMav(_publicMav);
//      Double.setSuperClass(o);   // a white lie for this test
//      symbolTable.put("java.lang.Double", Double);
      
      assert symbolTable.get("java.lang.Float") != null;
//      Float.setIsContinuation(false);
//      Float.setMav(_publicMav);
//      Float.setSuperClass(o);    // a white lie for this test
//      symbolTable.put("java.lang.Float", Float);
      
      assert symbolTable.get("java.lang.Long") != null;
//      Long.setIsContinuation(false);
//      Long.setMav(_publicMav);
//      Long.setSuperClass(o);     // a white lie for this test
//      symbolTable.put("java.lang.Long", Long);
            
      assert symbolTable.get("java.lang.Integer") != null;
//      Integer.setIsContinuation(false);
//      Integer.setMav(_publicMav);
//      Integer.setSuperClass(o);   // a white lie for this test
//      symbolTable.put("java.lang.Integer", Integer);
            
      assert symbolTable.get("java.lang.Short") != null;
//      Short.setIsContinuation(false);
//      Short.setMav(_publicMav);
//      Short.setSuperClass(o);   // a white lie for this test
//      symbolTable.put("java.lang.Short", Short);
      
      assert symbolTable.get("java.lang.Character") != null;
//      Character.setIsContinuation(false);
//      Character.setMav(_publicMav);
//      Character.setSuperClass(o);   // a white lie for this test     
//      symbolTable.put("java.lang.Character", Character);
      
      assert symbolTable.get("java.lang.Byte") != null;
//      Byte.setIsContinuation(false);
//      Byte.setMav(_publicMav);
//      Byte.setSuperClass(o);   // a white lie for this test        
//      symbolTable.put("java.lang.Byte", Byte);
      
      assert symbolTable.get("java.lang.Boolean") != null;
//      Boolean.setIsContinuation(false);
//      Boolean.setMav(_publicMav);
//      Boolean.setSuperClass(o);   // a white lie for this test   
//      symbolTable.put("java.lang.Boolean", Boolean);
         
      assert symbolTable.get("java.lang.String") != null;
//      SymbolData string = new SymbolData("java.lang.String");
//      string.setIsContinuation(false);
//      string.setMav(_publicMav);
//      string.setSuperClass(_object);   // a white lie for this test   
//      symbolTable.put("java.lang.String", string);
    }
    
    public void test_getData() {
      try {
        _btc._getData();
        fail("Should have thrown a RuntimeException");
      }
      catch (RuntimeException re) {
      }
    }
    
    public void testGetSymbolData() {
      symbolTable.put("zebra", _sd3);
      _sd3.setIsContinuation(false);
      SymbolData sd = symbolTable.get("java.lang.Object");
      sd.setPackage("java.lang");
      assertEquals("Should get _sd3 from the Symboltable.", _sd3, 
                   _btc.getSymbolData("zebra", NULL_LITERAL, true, true));
      assertEquals("Should get sd from the Symboltable.", sd, 
                   _btc.getSymbolData("java.lang.Object", NULL_LITERAL, true, true));
      _btc.getSymbolData("koala", NULL_LITERAL, true, true);
      
      assertEquals("Should be one error", 1, errors.size());
      assertEquals("ERror message should be correct ", "Class or variable koala not found.", errors.get(0).getFirst());
      
      errors = new LinkedList<Pair<String, JExpressionIF>>();
      
      sd.setPackage("notRightPackage");
      _btc.getSymbolData("java.lang.Object", NULL_LITERAL, true, true);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "The class java.lang.Object is not in the right package. Perhaps you meant to package it?",
                   errors.get(0).getFirst());
      
      //looking up a class that implements runnable will give an error:
      SymbolData sdThread = new SymbolData("java.lang.Thread");
      sdThread.setIsContinuation(false);
      sdThread.setPackage("java.lang");
      symbolTable.put("java.lang.Thread", sdThread);
      assertEquals("Should return null", null, _btc.getSymbolData("Thread", NULL_LITERAL, true, 
                                                                  true));
      
      assertEquals("Should now be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", "java.lang.Thread implements the Runnable interface, " +
                   "which is not allowed at any language level", errors.get(1).getFirst());

      // a class that implements Runnable, but was not user defined and is not one of our specific classes known to 
      // extend Runnable will not give an error
      SymbolData sdOther = new SymbolData("myClass");
      sdOther.setIsContinuation(false);
      SymbolData run = new SymbolData("java.lang.Runnable");
      run.setIsContinuation(false);
      run.setInterface(true);
      run.setPackage("java.lang");
      sdOther.addInterface(run);
      
      symbolTable.put("myClass", sdOther);
      symbolTable.put("java.lang.Runnable", run);
      
      assertEquals("Should return sdOther", sdOther, _btc.getSymbolData("myClass", NULL_LITERAL, 
                                                                        true, true));
      assertEquals("Should still just be 2 errors", 2, errors.size());
      
      //Test an inner class from the context of the outer class
      SymbolData sdInner = new SymbolData("outer.inner");
      sdInner.setIsContinuation(false);
      SymbolData sdOuter = new SymbolData("outer");
      sd.setIsContinuation(false);
      sdOuter.addInnerClass(sdInner);
      sdInner.setOuterData(sdOuter);
      assertEquals("Should return sdInner", sdInner, _btc.getSymbolData("inner", sdOuter, 
                                                                        NULL_LITERAL));
      
      //Test an inner interface from the context of the outer class
      sdInner.setInterface(true);
      sdOuter = new SymbolData("outer");
      sd.setIsContinuation(false);
      sdOuter.addInnerInterface(sdInner);
      assertEquals("Should return sdInner", sdInner, _btc.getSymbolData("inner", sdOuter, 
                                                                        NULL_LITERAL));
      
      
      //Test that the correct inner class is returned
      //      
      //      C {
      //        A {
      //          
      //          D{}
      //        }
      //        B{
      //          D{}
      //        }
      //      }
      
      SymbolData sd1 = new SymbolData("C.A");
      SymbolData sd2 = new SymbolData("C.B");
      SymbolData sd3 = new SymbolData("C");
      SymbolData sd4 = new SymbolData("C.A.D");
      SymbolData sd5 = new SymbolData("C.B.D");
      sd1.setIsContinuation(false);
      sd2.setIsContinuation(false);
      sd3.setIsContinuation(false);
      sd4.setIsContinuation(false);
      sd5.setIsContinuation(false);
      
      sd1.addInnerClass(sd4);
      sd4.setOuterData(sd1);
      sd2.addInnerClass(sd5);
      sd5.setOuterData(sd2);
      sd3.addInnerClass(sd1);
      sd1.setOuterData(sd3);
      sd3.addInnerClass(sd2);
      sd2.setOuterData(sd3);
      
      assertEquals("Should return A.D", sd4, _btc.getSymbolData("A.D", sd3, NULL_LITERAL));
      assertEquals("Should return B.D", sd5, _btc.getSymbolData("B.D", sd3, NULL_LITERAL));
      assertEquals("Should return null", null, _btc.getSymbolData("D", sd3, NULL_LITERAL));
      assertEquals("Should return B", sd2, _btc.getSymbolData("B", sd1, NULL_LITERAL));
      assertEquals("Should return C.A", sd1, _btc.getSymbolData("A", sd5, NULL_LITERAL));
      //Test a class defined in the context of a method
      MethodData md = new MethodData("myMethod", _publicMav, new TypeParameter[0], 
                    SymbolData.INT_TYPE, new VariableData[0], new String[0], sdOuter, 
                    NULL_LITERAL);
      md.addInnerClass(sd3);
      assertEquals("Should return sd3", sd3, _btc.getSymbolData("C", md, NULL_LITERAL));
    }
   
    public void testGetQualifiedClassName() {
      //first test when the package is empty:
      _btc._package = "";
      assertEquals("Should not change qualified name.", "simpson.Bart", _btc.getQualifiedClassName("simpson.Bart"));
      assertEquals("Should not change unqualified name.", "Lisa", _btc.getQualifiedClassName("Lisa"));
      
      //now test when package is not empty.
      _btc._package="myPackage";
      assertEquals("Should not change properly packaged qualified name.", "myPackage.Snowball", 
                   _btc.getQualifiedClassName("myPackage.Snowball"));
      assertEquals("Should append package to front of not fully packaged name", "myPackage.simpson.Snowball", 
                   _btc.getQualifiedClassName("simpson.Snowball"));
      assertEquals("Should append package to front of unqualified class name.", "myPackage.Grandpa", 
                   _btc.getQualifiedClassName("Grandpa"));
    }
    
    public void test_lookupMethodHelper() {
      VariableData[] vds = 
        new VariableData[] { new VariableData("field1", _finalMav, SymbolData.DOUBLE_TYPE, true, null),
                             new VariableData("field2", _finalMav, SymbolData.INT_TYPE, true, null) };
      VariableData[] vds2 = 
        new VariableData[] { new VariableData("field3", _finalMav, SymbolData.DOUBLE_TYPE, true, null) };
      VariableData[] vds3 = 
        new VariableData[] { new VariableData("field1", _finalMav, SymbolData.CHAR_TYPE, true, null) };
      InstanceData[] sds = 
        new InstanceData[] { SymbolData.DOUBLE_TYPE.getInstanceData(), SymbolData.INT_TYPE.getInstanceData()};
      InstanceData[] sds2 = new InstanceData[] { SymbolData.DOUBLE_TYPE.getInstanceData() };
      InstanceData[] sds3 = new InstanceData[] { SymbolData.CHAR_TYPE.getInstanceData()};
      MethodData md = 
        new MethodData("Bart", _publicMav, new TypeParameter[0], _sd1, vds, new String[0], _sd1, null);
      vds[0].setEnclosingData(md);
      vds[1].setEnclosingData(md);
      _sd1.addMethod(md);
      MethodData md2 = 
        new MethodData("Homer", _publicMav, new TypeParameter[0], _sd2, vds2, new String[0], _sd2, null);
      vds2[0].setEnclosingData(md2);
      _sd2.addMethod(md2);
      _sd1.setSuperClass(_sd2);                               
      MethodData constructor = 
        new MethodData("monkey", _publicMav, new TypeParameter[0], _sd1, vds3, new String[0], _sd1, null);
      vds3[0].setEnclosingData(constructor);
      _sd1.addMethod(constructor);
      assertEquals("Should return md.", md, _btc._lookupMethodHelper("Bart", _sd1, sds, null, false, _sd3));
      assertEquals("Should return md2.", md2, _btc._lookupMethodHelper("Homer", _sd1, sds2, null, false, _sd3));
      assertEquals("Should return constructor.", constructor, _btc._lookupMethodHelper("monkey", _sd1, sds3, null, true,
                                                                                       _sd3));
      assertEquals("Should return null.", null, _btc._lookupMethodHelper("Homer", _sd1, sds2, null, true, _sd3));
      assertEquals("Should return null.", null, _btc._lookupMethodHelper("Lenny", _sd1, sds, null, false, _sd3));
    }
    
    public void testLookupMethod() {
      VariableData[] vds = 
        new VariableData[] { new VariableData("field1", _finalMav, SymbolData.DOUBLE_TYPE, true, null),
                             new VariableData("field2", _finalMav, SymbolData.INT_TYPE, true, null) };
      InstanceData[] sds = 
        new InstanceData[] { SymbolData.DOUBLE_TYPE.getInstanceData(), SymbolData.INT_TYPE.getInstanceData() };
      MethodData md = new MethodData("Bart", _publicMav, new TypeParameter[0], _sd1, vds, new String[0], _sd1, null);
      vds[0].setEnclosingData(md);
      vds[1].setEnclosingData(md);
      _sd1.addMethod(md);
      
      assertEquals("Should return md.", md, _btc._lookupMethod("Bart", _sd1, sds, null, "Error message:", true, _sd3));
      assertEquals("Should be no errors.", 0, errors.size());
      assertEquals("Should return null.", null, _btc._lookupMethod("Lenny", _sd1, sds, null, "Lenny Error message: ", 
                                                                   true, _sd3));
      assertEquals("Should be one error.", 1, errors.size());
      assertEquals("Newest error should have correct message", "Lenny Error message: Lenny(double, int).", 
                   errors.getLast().getFirst());
   }
    
    public void testAreInSamePackage() {
      assertTrue("areInSamePackage with same qualified names", _btc._areInSamePackage(_sd1, _sd2));
      assertFalse("areInSamePackage with unqualified and qualified names", _btc._areInSamePackage(_sd1, _sd3));
      assertFalse("areInSamePackage with different qualified names", _btc._areInSamePackage(_sd1, _sd4));
      assertFalse("areInSamePackage with qualified and empty names", _btc._areInSamePackage(_sd4, _sd5));
      assertTrue("areInSamePackage with different unqualified names", _btc._areInSamePackage(_sd3, _sd6));
    }
    
    public void testCheckAccessibility() {
      _sd6.setSuperClass(_sd3);
      _sd6.setOuterData(_sd2);
      _sd2.addInnerClass(_sd6);
      String sd3Name = _sd3.getName();
      String sd6Name = _sd6.getName();

      // public
      assertTrue("checkAccess with public mav and same package", 
                 _btc.checkAccess(NULL_LITERAL, _publicMav, "fieldOfDreams", _sd1, _sd2, "field"));
      assertTrue("checkAccess with public mav and different packages", 
                 _btc.checkAccess(NULL_LITERAL, _publicMav, "fieldOfDreams", _sd1, _sd4, "field"));
      assertTrue("checkAccess with public mav and is outer class", 
                 _btc.checkAccess(NULL_LITERAL, _publicMav, "fieldOfDreams", _sd3, _sd6, "field"));
      assertTrue("checkAccess for a class and its outer class", 
                 _btc.checkAccess(NULL_LITERAL, _publicMav, sd6Name, _sd2, _sd6, "class"));
      assertTrue("checkAccess for a class and a class it is not related to", 
                 _btc.checkAccess(NULL_LITERAL, _publicMav, sd6Name, _sd2, _sd4, "class"));
      
      // protected
      assertTrue("checkAccess with protected mav and same package", 
                 _btc.checkAccess(NULL_LITERAL, _protectedMav, "fieldOfDreams", _sd1, _sd2, "field"));
      assertFalse("checkAccess with protected mav and different package", 
                  _btc.checkAccess(NULL_LITERAL, _protectedMav, "fieldOfDreams", _sd1, _sd4, "field"));
      assertTrue("checkAccess with protected mav and is outer class", 
                 _btc.checkAccess(NULL_LITERAL, _protectedMav, "fieldOfDreams", _sd2, _sd6, "field"));
      assertTrue("checkAccess with protected mav and is super class", 
                 _btc.checkAccess(NULL_LITERAL, _protectedMav, "fieldOfDreams", _sd3, _sd6, "field"));
      assertTrue("checkAccess for a class and its outer class", 
                 _btc.checkAccess(NULL_LITERAL, _protectedMav, sd6Name, _sd2, _sd6, "class"));
      assertFalse("checkAccess for a class and a class it is not related to", 
                  _btc.checkAccess(NULL_LITERAL, _protectedMav, sd6Name, _sd2, _sd4, "class"));

      // private
      assertFalse("checkAccess with private mav and same package", 
                  _btc.checkAccess(NULL_LITERAL, _privateMav, "fieldOfDreams", _sd1, _sd2, "field"));
      assertFalse("checkAccess with private mav and different package", 
                  _btc.checkAccess(NULL_LITERAL, _privateMav, "fieldOfDreams", _sd1, _sd4, "field"));
      assertTrue("checkAccess with private mav and is outer class", 
                 _btc.checkAccess(NULL_LITERAL, _privateMav, "fieldOfDreams", _sd2, _sd6, "field"));
      assertFalse("checkAccess with private mav and is super class",
                  _btc.checkAccess(NULL_LITERAL, _privateMav, "fieldOfDreams", _sd3, _sd6, "field"));
      assertEquals("There should be 5 errors", 5, errors.size());
      assertEquals("The last error message should be correct", 
                   "The field fieldOfDreams in " + sd3Name + " is private and cannot be accessed from " + sd6Name, 
                   errors.getLast().getFirst());
      assertTrue("checkAccess with private mav and same file",
                 _btc.checkAccess(NULL_LITERAL, _privateMav, "fieldOfDreams", _sd3, _sd3, "field"));      
      assertTrue("checkAccess for a class and its outer class", 
                 _btc.checkAccess(NULL_LITERAL, _privateMav, sd6Name, _sd2, _sd6, "class"));
      assertFalse("checkAccess for a class and a class it is not related to", 
                  _btc.checkAccess(NULL_LITERAL, _privateMav, sd6Name, _sd2, _sd4, "class"));

      // package
      assertTrue("checkAccess with package mav and same package", 
                 _btc.checkAccess(NULL_LITERAL, _packageMav, "fieldOfDreams", _sd1, _sd2, "field"));
      assertFalse("checkAccess with package mav and different package", 
                  _btc.checkAccess(NULL_LITERAL, _packageMav, "fieldOfDreams", _sd1, _sd4, "field"));
      assertTrue("checkAccess with package mav and is outer class", 
                 _btc.checkAccess(NULL_LITERAL, _packageMav, "fieldOfDreams", _sd2, _sd6, "field"));
      assertTrue("checkAccess with package mav and is super class", 
                 _btc.checkAccess(NULL_LITERAL, _packageMav, "fieldOfDreams", _sd3, _sd6, "field"));
      assertTrue("checkAccess for a class and its outer class", 
                 _btc.checkAccess(NULL_LITERAL, _packageMav, sd6Name, _sd2, _sd6, "class"));
      assertFalse("checkAccess for a class and a class it is not related to", 
                  _btc.checkAccess(NULL_LITERAL, _packageMav, sd6Name, _sd2, _sd4, "class"));

    }
    
    public void testGetFieldOrVariable() {
      _sd6.setSuperClass(_sd3);
      _sd6.setOuterData(_sd2);
      VariableData vd0 = new VariableData("field0", _privateMav, _sd1, true, _sd6);
      VariableData vd1 = new VariableData("field1", _publicMav, _sd1, true, _sd3);
      VariableData vd2 = new VariableData("field2", _privateMav, _sd2, true, _sd2);
      VariableData vd3 = 
        new VariableData("variable1", new ModifiersAndVisibility(NONE, new String[0]), _sd3, true, _sd3);
      MethodData md = new MethodData("method1", _protectedMav, null, null, null, null, _sd3, null);
      md.addVar(vd3);
      _sd6.addVar(vd0);
      _sd3.addVar(vd1);
      _sd2.addVar(vd2);
      _sd3.addMethod(md);
      assertEquals("Should find field0", vd0, _btc.getFieldOrVariable("field0", _sd6, _sd6, NULL_LITERAL));
      assertEquals("Should find field1", vd1, _btc.getFieldOrVariable("field1", _sd6, _sd6, NULL_LITERAL));
      assertEquals("Should find field2", vd2, _btc.getFieldOrVariable("field2", _sd6, _sd6, NULL_LITERAL));
      assertEquals("Should not find field7", null, _btc.getFieldOrVariable("field7", _sd6, _sd6, NULL_LITERAL));
      assertEquals("Should find variable1", vd3, _btc.getFieldOrVariable("variable1", md, _sd3, NULL_LITERAL));
      
      // test that passing in a list of variables to use will not recur (if called from the type checker)
      assertEquals("Should not find field1", null, 
                   _btc.getFieldOrVariable("field1", _sd6, _sd6, NULL_LITERAL, new LinkedList<VariableData>()));
      assertEquals("There should be no errors", 0, errors.size());
    }
    
    public void test_addError() {
      LinkedList<Pair<String, JExpressionIF>> e = new LinkedList<Pair<String, JExpressionIF>>();
      
      NullLiteral nl = NULL_LITERAL;
      NullLiteral nl2 = NULL_LITERAL;
      
      e.addLast(new Pair<String,JExpressionIF>("Boy, is this an error!", nl));
      _addError("Boy, is this an error!", nl);
     
      assertTrue("An error should have been added.", _errorAdded);
      assertEquals("The errors list should be correct.", e, errors);
      
      e.addLast(new Pair<String,JExpressionIF>("Error again!", nl2));
      _addError("Error again!", nl2);
     
      assertTrue("Another error should have been aded.", _errorAdded);
      assertEquals("The new errors list should be correct.", e, errors);
    }
    
    
    public void testCheckAbstractMethodsHelper() {
      /* 
       * This should work.
       * _sd1 will be abstract.
       * _sd2 will be concrete and extend _sd1.
       * _sd3 will be abstract and extend _sd2.
       * _sd4 will be abstract and extend _sd3.
       * _sd5 will be concrete and extend _sd4.
       */
       
      VariableData[] vds = new VariableData[] { new VariableData("field1", _finalMav, SymbolData.DOUBLE_TYPE, true, null),
        new VariableData("field2", _finalMav, SymbolData.INT_TYPE, true, null) };
      MethodData md1 = new MethodData("Abe", _abstractMav, new TypeParameter[0], _sd1, 
                                      vds, 
                                      new String[0], 
                                      _sd1,
                                      null);
      vds[0].setEnclosingData(md1);
      vds[1].setEnclosingData(md1);
      _sd1.addMethod(md1);
      _sd1.setMav(_abstractMav);
      _sd1.setSuperClass(_sd6);
      _sd2.setSuperClass(_sd1);
      MethodData md2 = new MethodData("Homer", _abstractMav, new TypeParameter[0], _sd1, 
                                      vds, 
                                      new String[0], 
                                      _sd3,
                                      null);
      MethodData md3 = new MethodData("Marge", _publicMav, new TypeParameter[0], _sd1, 
                                      vds, 
                                      new String[0], 
                                      _sd3,
                                      null);
      MethodData md123 = new MethodData("Apu", _abstractMav, new TypeParameter[0], _sd1,
                                        vds,
                                        new String[0],
                                        _sd3,
                                        null);
      _sd3.addMethod(md2);
      _sd3.addMethod(md3);
      _sd3.addMethod(md123);
      _sd3.setMav(_abstractMav);
      _sd3.setSuperClass(_sd2);
      MethodData md4 = new MethodData("Bart", _abstractMav, new TypeParameter[0], _sd1, 
                                      vds, 
                                      new String[0], 
                                      _sd4,
                                      null);
      MethodData md5 = new MethodData("Lisa", _abstractMav, new TypeParameter[0], _sd1, 
                                      vds, 
                                      new String[0], 
                                      _sd4,
                                      null);
      MethodData md1234 = new MethodData("Apu", _publicMav, new TypeParameter[0], _sd1,
                                         vds, new String[0],
                                         _sd4,
                                         null);
      _sd4.addMethod(md4);
      _sd4.addMethod(md5);
      _sd4.addMethod(md1234);
      _sd4.setMav(_abstractMav);
      _sd4.setSuperClass(_sd3);
      MethodData md6 = new MethodData("Bart", _abstractMav, new TypeParameter[0], _sd1, 
                                      vds, 
                                      new String[0], 
                                      _sd5,
                                      null);
      MethodData md7 = new MethodData("Homer", _abstractMav, new TypeParameter[0], _sd1, 
                                      vds, 
                                      new String[0], 
                                      _sd5,
                                      null);
      MethodData md8 = new MethodData("Lisa", _abstractMav, new TypeParameter[0], _sd1, 
                                      vds, 
                                      new String[0], 
                                      _sd5,
                                      null);
      _sd5.addMethod(md6);
      _sd5.addMethod(md7);
      _sd5.addMethod(md8);
      _sd5.setSuperClass(_sd4);
      LinkedList<MethodData> mds = _sd5.getMethods();
      // TODO: Check all enclosingDatas (interfaces too)
      _btc._checkAbstractMethodsHelper(_sd5, _sd5.getSuperClass(), mds, null);
      assertEquals("There should be no errors.", 0, errors.size());
//       must be declared abstract or must override the abstract method: " + md.getName()
      // Test one that doesn't work.
      mds = _sd2.getMethods();
      _btc._checkAbstractMethodsHelper(_sd2, _sd2.getSuperClass(), mds, null);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "i.like.giraffe must be declared abstract or must override the abstract method: Abe(double, int) in " 
                     + _sd2.getSuperClass().getName(),
                   errors.get(0).getFirst());
      
      //check that an interface's abstract methods are recognized.
      _sd3.setInterface(true);
      _sd3.setMethods(new LinkedList<MethodData>());
      _sd3.addMethod(md7);
      _sd2.addEnclosingData(_sd3);
      SymbolData sd = symbolTable.get("java.lang.Object");
//      sd.setIsContinuation(false);
      _sd2.setSuperClass(sd);
      _btc._checkAbstractMethodsHelper(_sd2, _sd3, mds, null);
      assertEquals("There should be two errors.", 2, errors.size());
      assertEquals("The error message should be correct.", 
                   "i.like.giraffe must be declared abstract or must override the abstract method: Homer(double, int) in " 
                     + _sd3.getName(), errors.get(1).getFirst());
    }
    
    public void testForPrimitiveType() {
      assertEquals("Should return SymbolData.INT_TYPE.", 
                   SymbolData.INT_TYPE, 
                   _btc.forPrimitiveType(new PrimitiveType(NONE, "int")));
      try {
        _btc.forPrimitiveType(new PrimitiveType(NONE, "Maggie"));
        fail("Should have thrown an exception.");
      }
      catch(RuntimeException re) {
      }
    }
  
    public void test_isAssignableFrom() {
      assertTrue("Should be assignable.", 
                 _btc._isAssignableFrom(SymbolData.DOUBLE_TYPE, symbolTable.get("java.lang.Double")));
      assertFalse("Should not be assignable.", 
                  _btc._isAssignableFrom(symbolTable.get("java.lang.Double"), SymbolData.FLOAT_TYPE));
      assertTrue("Should be assignable.", 
                 _btc._isAssignableFrom(SymbolData.DOUBLE_TYPE, symbolTable.get("java.lang.Long")));
      assertFalse("Should not be assignable.", 
                  _btc._isAssignableFrom(symbolTable.get("java.lang.Double"), SymbolData.INT_TYPE));
      assertTrue("Should be assignable.", 
                 _btc._isAssignableFrom(SymbolData.DOUBLE_TYPE, symbolTable.get("java.lang.Short")));
      assertFalse("Should not be assignable.", _btc._isAssignableFrom(symbolTable.get("java.lang.Double"), symbolTable.get("java.lang.Character")));
      assertTrue("Should be assignable.", _btc._isAssignableFrom(SymbolData.DOUBLE_TYPE, SymbolData.BYTE_TYPE));
      assertTrue("Should be assignable.", _btc._isAssignableFrom(symbolTable.get("java.lang.Integer"), symbolTable.get("java.lang.Integer")));
      assertFalse("Should not be assignable.", _btc._isAssignableFrom(symbolTable.get("java.lang.Integer"), symbolTable.get("java.lang.Character")));
      assertTrue("Should be assignable.", _btc._isAssignableFrom(symbolTable.get("java.lang.Character"), SymbolData.CHAR_TYPE));
      assertFalse("Should not be assignable.", _btc._isAssignableFrom(symbolTable.get("java.lang.Boolean"), SymbolData.INT_TYPE));
      
      assertTrue("Should be assignable", _btc._isAssignableFrom(symbolTable.get("java.lang.Object"), SymbolData.INT_TYPE));
      
      _sd2.setSuperClass(_sd1);
      assertTrue("Should be assignable.", _btc._isAssignableFrom(_sd1, _sd1));
      assertTrue("Should be assignable.", _btc._isAssignableFrom(_sd1, _sd2));
      
      //test 1.5/1.4 auto-boxing and unboxing
      assertFalse("Should not be assignable.", _btc._isAssignableFrom(symbolTable.get("java.lang.Double"), SymbolData.INT_TYPE));
      assertTrue("Should be assignable.", _btc._isAssignableFrom(SymbolData.DOUBLE_TYPE, symbolTable.get("java.lang.Short")));
      assertFalse("Should not be assignable.", _btc._isAssignableFrom(symbolTable.get("java.lang.Double"), symbolTable.get("java.lang.Character")));
      assertTrue("Should be assignable.", _btc._isAssignableFrom(SymbolData.DOUBLE_TYPE, SymbolData.BYTE_TYPE));
      assertTrue("Should be assignable.", _btc._isAssignableFrom(symbolTable.get("java.lang.Integer"), symbolTable.get("java.lang.Integer")));
      assertFalse("Should not be assignable.", _btc._isAssignableFrom(symbolTable.get("java.lang.Integer"), symbolTable.get("java.lang.Character")));
      assertTrue("Should be assignable.", _btc._isAssignableFrom(symbolTable.get("java.lang.Object"), SymbolData.INT_TYPE));
      
      LanguageLevelVisitor llv = 
        new LanguageLevelVisitor(_btc._file, 
                                 _btc._package,
                                 null, // enclosingClassName for top level traversal
                                 _btc._importedFiles, 
                                 _btc._importedPackages, 
                                 new HashSet<String>(), 
                                 new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                                 new LinkedList<Command>());
      
//      LanguageLevelConverter.symbolTable = llv.symbolTable = _btc.symbolTable;
      LanguageLevelConverter._newSDs.clear();
      
      SourceInfo si = NONE;
      
      ArrayData intArray = new ArrayData(SymbolData.INT_TYPE, llv, si);
      assertTrue("Should be able to assign an array to Object", 
                 _btc._isAssignableFrom(symbolTable.get("java.lang.Object"), intArray));
      
      ArrayData doubleArray = new ArrayData(SymbolData.DOUBLE_TYPE, llv, si);
      assertFalse("Should not be able to assign an int[] to a double[]", 
                  _btc._isAssignableFrom(doubleArray, intArray));
      
      ArrayData integerArray = new ArrayData(symbolTable.get("java.lang.Integer"), llv, si);
      assertFalse("Should not be able to assign an array of ints to an array of Integers", 
                  _btc._isAssignableFrom(integerArray, intArray));
      assertFalse("Should not be able to assign an array of Integers to an array of ints", 
                  _btc._isAssignableFrom(intArray, integerArray));
      
      assertTrue("Should be able to assign an array to an interface of java.io.Serializable", 
                 _btc._isAssignableFrom(symbolTable.get("java.io.Serializable"), integerArray));

      LanguageLevelConverter.OPT = new Options(JavaVersion.JAVA_5, EmptyIterable.<File>make());
      assertFalse("Should not be assignable.", 
                  _btc._isAssignableFrom(symbolTable.get("java.lang.Double"), SymbolData.INT_TYPE));
      assertFalse("Should not be assignable.", 
                  _btc._isAssignableFrom(symbolTable.get("java.lang.Double"), symbolTable.get("java.lang.Character")));
      assertTrue("Should be assignable.", _btc._isAssignableFrom(SymbolData.DOUBLE_TYPE, SymbolData.BYTE_TYPE));
      assertTrue("Should be assignable.", 
                  _btc._isAssignableFrom(SymbolData.DOUBLE_TYPE, symbolTable.get("java.lang.Short")));
      assertTrue("Should be assignable.", 
                 _btc._isAssignableFrom(symbolTable.get("java.lang.Integer"), symbolTable.get("java.lang.Integer")));
      assertFalse("Should not be assignable.", 
                  _btc._isAssignableFrom(symbolTable.get("java.lang.Integer"), symbolTable.get("java.lang.Character")));
      assertTrue("Should not assignable.", 
                  _btc._isAssignableFrom(SymbolData.INT_TYPE, symbolTable.get("java.lang.Character")));
      assertTrue("Should be assignable.", 
                  _btc._isAssignableFrom(symbolTable.get("java.lang.Object"), SymbolData.INT_TYPE));
      assertTrue("Should be able to assign an array to Object", 
                 _btc._isAssignableFrom(symbolTable.get("java.lang.Object"), intArray));

      
      //check that classes are know to be subclasses of their interfaces.
      SymbolData myData = new SymbolData("yes");
      SymbolData yourData = new SymbolData("interface");
      yourData.setInterface(true);
      myData.setIsContinuation(false);
      myData.addInterface(yourData);
      yourData.setIsContinuation(false);
      assertTrue("Should be assignable", _btc._isAssignableFrom(yourData, myData));
    }
    
    
    public void test_isAssignableFromWithoutAutoboxing() {
      assertTrue("Should be assignable.", 
                 _btc._isAssignableFromWithoutAutoboxing(SymbolData.DOUBLE_TYPE, SymbolData.DOUBLE_TYPE));
      assertTrue("Should be assignable.", 
                 _btc._isAssignableFromWithoutAutoboxing(SymbolData.DOUBLE_TYPE, SymbolData.FLOAT_TYPE));
      assertTrue("Should be assignable.", 
                 _btc._isAssignableFromWithoutAutoboxing(SymbolData.DOUBLE_TYPE, SymbolData.LONG_TYPE));
      assertTrue("Should be assignable.", 
                 _btc._isAssignableFromWithoutAutoboxing(SymbolData.DOUBLE_TYPE, SymbolData.INT_TYPE));
      assertTrue("Should be assignable.", 
                 _btc._isAssignableFromWithoutAutoboxing(SymbolData.DOUBLE_TYPE, SymbolData.SHORT_TYPE));
      assertTrue("Should be assignable.", 
                 _btc._isAssignableFromWithoutAutoboxing(SymbolData.DOUBLE_TYPE, SymbolData.CHAR_TYPE));
      assertTrue("Should be assignable.", 
                 _btc._isAssignableFromWithoutAutoboxing(SymbolData.DOUBLE_TYPE, SymbolData.BYTE_TYPE));
      assertTrue("Should be assignable.", 
                 _btc._isAssignableFromWithoutAutoboxing(SymbolData.INT_TYPE, SymbolData.INT_TYPE));
      assertTrue("Should be assignable.", 
                 _btc._isAssignableFromWithoutAutoboxing(SymbolData.INT_TYPE, SymbolData.CHAR_TYPE));
      assertTrue("Should be assignable.", 
                 _btc._isAssignableFromWithoutAutoboxing(SymbolData.CHAR_TYPE, SymbolData.CHAR_TYPE));
      
      _sd2.setSuperClass(_sd1);
      assertTrue("Should be assignable.", _btc._isAssignableFromWithoutAutoboxing(_sd1, _sd1));
      assertTrue("Should be assignable.", _btc._isAssignableFromWithoutAutoboxing(_sd1, _sd2));
    }
    
    public void testCheckForCyclicInheritance() {
      _sd1.setSuperClass(_sd2);
      _sd2.addInterface(_sd3);
      _sd2.setSuperClass(new SymbolData("java.lang.String"));
      _sd3.addInterface(symbolTable.get("java.lang.Object"));
      _sd1.addInnerClass(_sd4);
      
      //no cyclic inheritance
      InterfaceDef nl = 
        new InterfaceDef(NONE, _publicMav, new Word(NONE, "name"), new TypeParameter[0], new ReferenceType[0], 
                         new BracedBody(NONE, new BodyItemI[0]));
      assertFalse("Should not be cyclic inheritance", 
                  _btc.checkForCyclicInheritance(_sd1, new LinkedList<SymbolData>(), nl));
      
      //if you and your super interface implement the same interface, it's okay
      _sd4.addInterface(_sd5);
      _sd4.addInterface(_sd6);
      _sd5.addInterface(_sd6);
      assertFalse("Should not be cyclic inheritance", 
                  _btc.checkForCyclicInheritance(_sd4, new LinkedList<SymbolData>(), nl));

      _sd3.addInterface(_sd2);
      //cyclic inheritance
      assertTrue("Should be cyclic inheritance", 
                 _btc.checkForCyclicInheritance(_sd1, new LinkedList<SymbolData>(), nl));
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "Cyclic inheritance involving " + _sd2.getName(), 
                   errors.get(0).getFirst());
      
      //if your super class implements your inner class, there is cyclic inheritance
      ArrayList<SymbolData> temp = _sd3.getInterfaces();
      _sd3.getInterfaces().remove(_sd2);
      _sd3.setInterfaces(temp);
      _sd2.setSuperClass(_sd6);
      _sd6.setSuperClass(_sd4);
      _sd4.setSuperClass(symbolTable.get("java.lang.Object"));
      assertTrue("Should be cyclic inheritance", _btc.checkForCyclicInheritance(_sd1, new LinkedList<SymbolData>(), nl));
      assertEquals("Should now be 2 errors", 2, errors.size());
      assertEquals("The error message should be correct", "Cyclic inheritance involving " + _sd4.getName(), 
                   errors.get(1).getFirst());
    }
    
    public void testForClassDef() {
      
      ClassDef cd = 
        new ClassDef(NONE, _publicMav, new Word(NONE, "Lisa"),
                     new TypeParameter[0], 
                     new ClassOrInterfaceType(NONE, "java.lang.Object", new Type[0]), new ReferenceType[0], 
                     new BracedBody(NONE, new BodyItemI[0]));

     //Test that no cyclic inheritance goes okay
      SymbolData Lisa = new SymbolData("Lisa");
      Lisa.setSuperClass(symbolTable.get("java.lang.Object"));
      
      Lisa.setIsContinuation(false);
      Lisa.setMav(_publicMav);
      _btc._file=new File("Lisa.dj1");
      _btc.symbolTable.put("Lisa", Lisa);
      TypeData result = cd.visit(_btc);


      assertEquals("There should be no errors", 0, errors.size());
      
      //Test that cyclic inheritance throws an error
      Lisa.setSuperClass(Lisa);
      result = cd.visit(_btc);
      
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("Error message should be correct", "Cyclic inheritance involving Lisa", errors.get(0).getFirst());
      
      //Test that if you extend a final class, an error is thrown
      ClassDef cd2 = 
        new ClassDef(NONE, _publicMav, new Word(NONE, "Me"),
                     new TypeParameter[0], new ClassOrInterfaceType(NONE, "Parent", new Type[0]), new ReferenceType[0], 
                     new BracedBody(NONE, new BodyItemI[0]));
      SymbolData parent = new SymbolData("Parent");
      parent.setMav(_finalMav);
      parent.setSuperClass(symbolTable.get("java.lang.Object"));
      _btc.symbolTable.put("Parent", parent);
      
      SymbolData me = new SymbolData("Me");
      
      me.setSuperClass(parent);
      me.setIsContinuation(false);
      _btc.symbolTable.put("Me", me);
      
      result = cd2.visit(_btc);
      
      
      assertEquals("There should be 2 errors", 2, errors.size());
      assertEquals("2nd Error message should be correct", "Class Me cannot extend the final class Parent", 
                   errors.get(1).getFirst());

      
      //Test that if you extend a class you cannot see, an error is thrown
      ClassDef cd3 = 
        new ClassDef(NONE, _publicMav, new Word(NONE, "somewhereElse.Lisa"),
                     new TypeParameter[0], new ClassOrInterfaceType(NONE, "java.lang.Object", new Type[0]), 
                     new ReferenceType[0], 
                     new BracedBody(NONE, new BodyItemI[0]));

      Lisa = new SymbolData("somewhereElse.Lisa");
      Lisa.setSuperClass(new SymbolData("hungry"));
      Lisa.setPackage("somewhereElse");
      Lisa.setIsContinuation(false);
      Lisa.getSuperClass().setIsContinuation(false);
      
      _btc.symbolTable.put("somewhereElse.Lisa", Lisa);
      result = cd3.visit(_btc);
      
      assertEquals("There should be three errors", 3, errors.size());
      assertEquals("3rd error message should be correct", 
                   "The class hungry is package protected because there is no access specifier and cannot be "
                     + "accessed from somewhereElse.Lisa", 
                   errors.get(2).getFirst());

      //Test that if you implement an interface you cannot see, an error is thrown
      SymbolData superI = new SymbolData("superI");
      superI.setInterface(true);
      superI.setIsContinuation(false);
      Lisa.setSuperClass(new SymbolData("super", _publicMav, new TypeParameter[0], null, new ArrayList<SymbolData>(), null));

      Lisa.addInterface(superI);
      _btc.symbolTable.put("superI", superI);

      ClassDef cd4 = 
        new ClassDef(NONE, _publicMav, new Word(NONE, "somewhereElse.Lisa"),
                     new TypeParameter[0], new TypeVariable(NONE, "super"), 
                     new ReferenceType[] {new ClassOrInterfaceType(NONE, "superI",  new Type[0])},
                     new BracedBody(NONE, new BodyItemI[0]));
                                  
    
                                  
      cd4.visit(_btc);
      assertEquals("There should be four errors", 4, errors.size());
      assertEquals("The 4th error message should be correct", 
                   "The interface superI is package protected because there is no access specifier and cannot be "
                     + "accessed from somewhereElse.Lisa", 
                   errors.get(3).getFirst());
      
      // Test that if a class has a final field and a constructor that sets the value, it won't throw an error
      VariableDeclarator vdec = 
        new UninitializedVariableDeclarator(NONE, new PrimitiveType(NONE, "int"), new Word(NONE, "i"));
      VariableDeclaration vd = new VariableDeclaration(NONE, _finalMav, new VariableDeclarator[] {vdec});
      ExpressionStatement se = 
        new ExpressionStatement(NONE, 
                                new SimpleAssignmentExpression(NONE, new SimpleNameReference(NONE, new Word(NONE, "i")), 
                                                                     new IntegerLiteral(NONE, 1)));      
      BracedBody cbb = new BracedBody(NONE, new BodyItemI[] {se});
      ConstructorDef consD = 
        new ConstructorDef(NONE, new Word(NONE, "Jimes"), _publicMav, new FormalParameter[0], new ReferenceType[0], cbb);
      BracedBody b = new BracedBody(NONE, new BodyItemI[] {vd, consD});
      ClassDef cd5 = 
        new ClassDef(NONE, _publicMav, new Word(NONE, "Jimes"),
                     new TypeParameter[0], new ClassOrInterfaceType(NONE, "java.lang.Object", new Type[0]), 
                     new ReferenceType[0], new BracedBody(NONE, new BodyItemI[] {vd, consD}));

      SymbolData sd = new SymbolData("Jimes");
      VariableData vData = new VariableData("i", _finalMav, SymbolData.INT_TYPE, false, sd);
      sd.setIsContinuation(false);
      sd.addVar(vData);
      SymbolData sd2 = symbolTable.get("java.lang.Object");
//      sd2.setIsContinuation(false);
//      sd2.setMav(_publicMav);
//      sd2.setPackage("java.lang");
      MethodData objMd = 
        new MethodData("java.lang.Object", _publicMav, new TypeParameter[0], sd2, new VariableData[0], new String[0], 
                       sd2, cd);
      sd2.addMethod(objMd);
      
      sd.setSuperClass(sd2);
      symbolTable.put("Jimes", sd);
      MethodData md = new MethodData("Jimes", _publicMav, new TypeParameter[0], sd, new VariableData[0], new String[0], 
                                     sd, cd);
      sd.addMethod(md);
      
      cd5.visit(_btc);
      assertEquals("There should still be four errors", 4, errors.size());
      
      // Test that if a class has a final field, that if there are no constructors, an error is thrown since the value 
      // of the field cannot be set.
      vData.lostValue();
      b = new BracedBody(NONE, new BodyItemI[] {vd});
      cd5 = new ClassDef(NONE, _publicMav, new Word(NONE, "Jimes"), new TypeParameter[0], 
                         new ClassOrInterfaceType(NONE, "java.lang.Object", new Type[0]), 
                         new ReferenceType[0], b);
 
      cd5.visit(_btc);
      assertEquals("There should be 5 errors now", 5, errors.size());
      assertEquals("The error message should be correct", "The final field i has not been initialized", 
                   errors.get(4).getFirst());
      
      //Test that if the class implements java.lang.Runnable, then an error is thrown.
      ClassDef cd6 = 
        new ClassDef(NONE, _publicMav, new Word(NONE, "JimesH"),
                     new TypeParameter[0], 
                     new ClassOrInterfaceType(NONE, "java.lang.Object", new Type[0]), 
                     new ReferenceType[] {new ClassOrInterfaceType(NONE, "java.lang.Runnable", new Type[0])}, 
                     new BracedBody(NONE, new BodyItemI[0]));
      sd = new SymbolData("JimesH");
      sd.setIsContinuation(false);
      
      symbolTable.clear();
      SymbolData runnableSd = new SymbolData("java.lang.Runnable");
      runnableSd.setMav(_publicMav);
      runnableSd.setIsContinuation(false);
      runnableSd.setPackage("java.lang");
      runnableSd.setInterface(true);
      sd.addInterface(runnableSd);
      symbolTable.put("JimesH", sd);
      symbolTable.remove("java.lang.Runnable");
      symbolTable.put("java.lang.Runnable", runnableSd);

      cd6.visit(_btc);
      assertEquals("There should be 6 errors now", 6, errors.size());
      assertEquals("The error message should be correct", 
                   "JimesH implements the Runnable interface, which is not allowed at any language level", 
                   errors.get(5).getFirst());

      //Test that if a class implements another class, an error is thrown
      ClassDef cd7 = 
        new ClassDef(NONE, _publicMav, new Word(NONE, "Hspia"),
                     new TypeParameter[0], new ClassOrInterfaceType(NONE, "superSD", new Type[0]), 
                     new ReferenceType[] {new ClassOrInterfaceType(NONE, "java.lang.String", new Type[0])}, 
                     new BracedBody(NONE, new BodyItemI[0]));
      sd = new SymbolData("Hspia");
      sd.setIsContinuation(false);
      
      symbolTable.clear();
      SymbolData stringSd = new SymbolData("java.lang.String");
      stringSd.setMav(_publicMav);
      stringSd.setIsContinuation(false);
      stringSd.setPackage("java.lang");
      stringSd.setInterface(false);
      sd.addInterface(stringSd);
      symbolTable.put("Hspia", sd);
      symbolTable.put("java.lang.String", stringSd);

      cd7.visit(_btc);
      assertEquals("There should be 7 errors now", 7, errors.size());
      assertEquals("The error message should be correct", 
                   "java.lang.String is not an interface and thus cannot appear after the keyword 'implements' here."
                     + "  Perhaps you meant to say 'extends'?" , 
                   errors.get(6).getFirst());

      
      //Test that if a class extends an interface, an error is thrown

      stringSd.setInterface(true);
      SymbolData superSD = new SymbolData("SuperSD");
      superSD.setInterface(true);
      sd.setSuperClass(superSD);
      sd.setInterfaces(new ArrayList<SymbolData>());
      symbolTable.put("superSD", superSD);
      cd7.visit(_btc);
      assertEquals("There should be 8 errors now", 8, errors.size());
      assertEquals("The error message should be correct", 
                   "SuperSD is an interface and thus cannot appear after the keyword 'extends' here." + 
                   "  Perhaps you meant to say 'implements'?", errors.get(7).getFirst());
     
      //Test that if a public class is not in a file of the same name, an error is thrown
      superSD.setInterface(false);
      sd.addModifier("public");
      cd7.visit(_btc);
      assertEquals("There should now be 9 errors", 9, errors.size());
      assertEquals("The error message should be correct", 
                   "Hspia is public thus must be defined in a file with the same name.", errors.get(8).getFirst());
      
      //Test that if a public class is in a file of the same name, no error is thrown.
      _btc._file = new File("Hspia.dj1");
      cd7.visit(_btc);
      assertEquals("There should still be just 9 errors", 9, errors.size());
      
      //Test that if a class that is not public extends test case, an error is thrown.
      //Also check that if a test class doesn't have any test methods, an error is thrown.
      sd.setMav(_privateMav);
      symbolTable.remove("Hspia");
      symbolTable.put("Hspia", sd);
      
      SymbolData testCase = defineTestCaseClass();
      sd.setSuperClass(testCase);
      
      cd7.visit(_btc);

      assertEquals("There should now be 11 errors", 11, errors.size());
      assertEquals("The tenth error message should be correct", 
                   "Hspia extends TestCase and thus must be explicitly declared public" , 
                   errors.get(9).getFirst());
      assertEquals("The eleventh error message should be correct", 
                   "Class Hspia does not have any valid test methods.  Test methods must be declared public, " +
                   "must return void, and must start with the word \"test\"" , 
                   errors.get(10).getFirst());
      
      //Test that if a class that is not public extends test case, an error is thrown.
      //Also check that if a test class doesn't have any test methods, an error is thrown.
      _btc._file = new File("Hspia.dj0");
      sd.setMav(_publicMav);
      symbolTable.remove("Hspia");
      symbolTable.put("Hspia", sd);
      
//      SymbolData testCase = defineTestCase();
//      symbolTable.put("junit.framework.TestCase", testCase);
//      sd.setSuperClass(testCase);
      
      cd7.visit(_btc);
      
      assertEquals("There should still be 11 errors", 11, errors.size());  // Generated duplicate error message
      assertEquals("The 12th error message should be correct", 
                   "Class Hspia does not have any valid test methods.  Test methods must be declared public, " +
                   "must return void, and must start with the word \"test\"" , 
                   errors.get(10).getFirst());

    }
    
    public void testForInterfaceDef() {
      InterfaceDef id = new InterfaceDef(NONE, _publicMav, new Word(NONE, "Lisa"),
                                         new TypeParameter[0], 
                                         new ReferenceType[] {new ClassOrInterfaceType(NONE, "superI", new Type[0])}, 
                                         new BracedBody(NONE, new BodyItemI[0]));

      //Test that no cyclic inheritance goes okay
      SymbolData Lisa = new SymbolData("Lisa");
      Lisa.setInterface(true);
      SymbolData superI = new SymbolData("superI");
      superI.setInterface(true);
      Lisa.addInterface(superI);
      Lisa.setIsContinuation(false);
      superI.setIsContinuation(false);
      _btc.symbolTable.put("Lisa", Lisa);
      _btc.symbolTable.put("superI", superI);

      TypeData result = id.visit(_btc);
      
      
      assertEquals("There should be no errors", 0, errors.size());
      
      //Test that cyclic inheritance throws an error
      Lisa.addInterface(Lisa);
      result = id.visit(_btc);
      
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("Error message should be correct", "Cyclic inheritance involving Lisa", errors.get(0).getFirst());
      
      //Test that if you implement an interface you cannot see, an error is thrown:
      InterfaceDef id2 = new InterfaceDef(NONE, _publicMav, new Word(NONE, "somewhereElse.Lisa"),
                                          new TypeParameter[0], 
                                          new ReferenceType[] {new ClassOrInterfaceType(NONE, "superI",  new Type[0])},
                                          new BracedBody(NONE, new BodyItemI[0]));                          
      Lisa = new SymbolData("somewhereElse.Lisa");
      Lisa.setIsContinuation(false);
      Lisa.addInterface(superI);
      Lisa.setPackage("somewhereElse");
      Lisa.setInterface(true);
      _btc.symbolTable.put("somewhereElse.Lisa", Lisa);
      superI.setMav(_privateMav);
      
      id2.visit(_btc);
      
      assertEquals("There should be 2 errors", 2, errors.size());
      assertEquals("The error message should be correct", 
                   "The interface superI in superI is private and cannot be accessed from somewhereElse.Lisa", 
                   errors.get(1).getFirst());

      /* The Runnable restriction has been dropped. */
//      //Test that if the interface extends java.lang.Runnable, then an error is thrown.
//      InterfaceDef id3 = 
//        new InterfaceDef(NONE, _publicMav, new Word(NONE, "JimesH"),
//                         new TypeParameter[0], 
//                         new ReferenceType[] {new ClassOrInterfaceType(NONE, "java.lang.Runnable", new Type[0])},
//                         new BracedBody(NONE, new BodyItemI[0]));
//      SymbolData sd = new SymbolData("JimesH");
//      sd.setIsContinuation(false);
//      sd.setInterface(true);
//      
//      symbolTable.clear();
//      SymbolData runnableSd = new SymbolData("java.lang.Runnable");
//      runnableSd.setMav(_publicMav);
//      runnableSd.setIsContinuation(false);
//      runnableSd.setPackage("java.lang");
//      runnableSd.setInterface(true);
//      sd.addInterface(runnableSd);
//      symbolTable.put("JimesH", sd);
//      symbolTable.remove("java.lang.Runnable");
//      symbolTable.put("java.lang.Runnable", runnableSd);
//
//      id3.visit(_btc);
//      assertEquals("There should be 3 errors now", 3, errors.size());
//      assertEquals("The error message should be correct", 
//                   "JimesH extends the Runnable interface, which is not allowed at any language level", 
//                   errors.get(2).getFirst());

      // Test that an error is thrown if you implement a class

      InterfaceDef id4 = 
        new InterfaceDef(NONE, _publicMav, new Word(NONE, "Bart"),
                         new TypeParameter[0], 
                         new ReferenceType[] {new ClassOrInterfaceType(NONE, "superC", new Type[0])}, 
                         new BracedBody(NONE, new BodyItemI[0])); 
      // Test that no cyclic inheritance goes okay
      SymbolData me = new SymbolData("Bart");
      me.setInterface(true);
      SymbolData superC = new SymbolData("superC");
      superC.setInterface(false);
      //Lisa.setSuperClass(superI);
      me.addInterface(superC);
      me.setIsContinuation(false);
      superC.setIsContinuation(false);
      _btc.symbolTable.put("Bart", me);
      _btc.symbolTable.put("superC", superC);

      result = id4.visit(_btc);
      
      
      assertEquals("There should be 3 errors now ", 3, errors.size());
      assertEquals("The error message should be correct", 
                   "superC is not an interface and thus cannot appear after the keyword 'extends' here", 
                   errors.getLast().getFirst());

      // Test that no error is thrown if you implement an interface
      superC.setInterface(true);
      result = id4.visit(_btc);
      assertEquals("There should still just be 3 errors", 3, errors.size());
      
      // Test that if a public interface is in a file of the wrong name, an error is thrown.
      me.addModifier("public");
      result = id4.visit(_btc);
      assertEquals("There should be 4 errorrs", 4, errors.size());
      assertEquals("The error message should be correct", 
                   "Bart is public thus must be defined in a file with the same name.", 
                   errors.getLast().getFirst());
      
      // Test that if a public interface is in a file of the right name, no error is thrown.
      _btc._file = new File("Bart.dj1");
      result = id4.visit(_btc);
      assertEquals("There should still just be 4 errors", 4, errors.size());
    }
    
    public void testForClassImportStatement() {
      Word[] words = new Word[] {
        new Word(NONE, "alpha"),
        new Word(NONE, "beta")};
      CompoundWord cw = new CompoundWord(NONE, words);
      ClassImportStatement cis = new ClassImportStatement(NONE, cw);
      SymbolData sd = new SymbolData("alpha.beta");
      //Test one that will work
      sd.setPackage("alpha");
      sd.setIsContinuation(false);
      sd.setMav(_publicMav);
      symbolTable.put("alpha.beta", sd);
      
      cis.visit(_btc);
      assertEquals("There should be no errors", 0, errors.size());
      
      //Test one that does not work (file is not in the right package)
      sd.setPackage("");
      cis.visit(_btc);

      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "The class alpha.beta is not in the right package. Perhaps you meant to package it?", 
                   errors.get(0).getFirst()); 
    }

    public void testForPackageStatement() {
      Word[] badWords = new Word[] { 
        new Word(NONE, "java"), 
        new Word(NONE, "lang"), 
        new Word(NONE, "Object")};
      Word[] okWords = new Word[] { new Word(NONE, "java"), new Word(NONE, "lang")};

      PackageStatement badPackage = 
        new PackageStatement(NONE, new CompoundWord(NONE, badWords));
      PackageStatement okPackage = 
        new PackageStatement(NONE, new CompoundWord(NONE, okWords));
      
//      SymbolData object = new SymbolData("java.lang.Object");
//      object.setPackage("java.lang");
//      object.setIsContinuation(false);
//      symbolTable.put("java.lang.Object", object);
      
      okPackage.visit(_btc);
      assertEquals("Should be 0 errors", 0, errors.size());
      
      badPackage.visit(_btc);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "java.lang.Object is not a allowable package name, because it conflicts with a class you have"
                     + " already defined", 
                   errors.getLast().getFirst());
    }
    
    public void testAutoBoxingAndUnboxing() {
      //Test that autoboxing works for a method invocation going from java.lang.Integer to int
      Expression e = new SimpleMethodInvocation(NONE,
                                                new Word(NONE, "myMethod"),
                                                new ParenthesizedExpressionList(NONE, new Expression[]{
        new SimpleNameReference(NONE, new Word(NONE, "i"))}));
      BodyItemI[] bii = new BodyItemI[] {new ExpressionStatement(NONE, e)};
      BracedBody b = new BracedBody(NONE, bii);
      
      VariableData vd1 = new VariableData("i", _publicMav, symbolTable.get("java.lang.Integer"), true, _sd1);
      VariableData vd2 = new VariableData("i", _publicMav, SymbolData.INT_TYPE, true, null);
      _sd1.addVar(vd1);
      MethodData md = 
        new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, new VariableData[] {vd2},
                       new String[0], null, NULL_LITERAL);
      vd2.setEnclosingData(md);
      _sd1.addMethod(md);
      
      b.visit(new ClassBodyTypeChecker(_sd1, _btc._file, "", new LinkedList<String>(), new LinkedList<String>(), 
                                       _sd1.getVars(), new LinkedList<Pair<SymbolData, JExpression>>()));

      assertEquals("There should be no errors", 0, errors.size());

      //Test that autoboxing works for a method invocation going from double java.lang.Double
      bii = new BodyItemI[] {new ExpressionStatement(NONE, e)};
      b = new BracedBody(NONE, bii);
      
      
      vd1 = new VariableData("i", _publicMav, symbolTable.get("java.lang.Double"), true, null);
      vd2 = new VariableData("i", _publicMav, SymbolData.DOUBLE_TYPE, true, _sd1);
      _sd1.setVars(new LinkedList<VariableData>());
      _sd1.addVar(vd2);
      _sd1.setMethods(new LinkedList<MethodData>());
      md = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, new VariableData[] {vd1}, 
                          new String[0], null, NULL_LITERAL);
      vd1.setEnclosingData(md);
      _sd1.addMethod(md);
      
      b.visit(new ClassBodyTypeChecker(_sd1, _btc._file, "", new LinkedList<String>(), new LinkedList<String>(), 
                                       _sd1.getVars(), new LinkedList<Pair<SymbolData, JExpression>>()));


      assertEquals("There should be no errors", 0, errors.size());

      
      //Test that autoboxing can find the correct method def.
      _sd1.setVars(new LinkedList<VariableData>());
      _sd1.addVar(vd2);
      _sd1.addVar(new VariableData("t", _publicMav, SymbolData.BOOLEAN_TYPE, false, _sd1));
      _sd1.addMethod(new MethodData("myMethod", _publicMav, new TypeParameter[0], 
                                    SymbolData.BOOLEAN_TYPE, new VariableData[] {vd2}, new String[0], null, 
                                    NULL_LITERAL));
      
      Expression e2 = new SimpleAssignmentExpression(NONE,
                                                     new SimpleNameReference(NONE, new Word(NONE, "t")),
                                                     e);
                  
      BodyItemI[] bii2 = new BodyItemI[] {new ExpressionStatement(NONE, e2)};
      BracedBody b2 = new BracedBody(NONE, bii2);

      b2.visit(new ClassBodyTypeChecker(_sd1, _btc._file, "", new LinkedList<String>(), new LinkedList<String>(), 
                                        _sd1.getVars(), new LinkedList<Pair<SymbolData, JExpression>>()));
      
      assertEquals("There should be no errors", 0, errors.size());
      
      //Test that autoboxing works for a constructor invocation
      _sd1.setIsContinuation(false);
      symbolTable.put("i.like.monkey", _sd1);
      _sd1.setPackage("i.like");
      Expression[] expr1 = new Expression[] {new SimpleNameReference(NONE, new Word(NONE, "i"))};
      e = new SimpleNamedClassInstantiation(NONE,
                                            new ClassOrInterfaceType(NONE, "i.like.monkey", new Type[0]),
                                            new ParenthesizedExpressionList(NONE, expr1));
      
      bii = new BodyItemI[] {new ExpressionStatement(NONE, e)};
      b = new BracedBody(NONE, bii);
      
      _sd1.setVars(new LinkedList<VariableData>());
      
      vd1 = new VariableData("i", _publicMav, symbolTable.get("java.lang.Integer"), true, _sd1);
      vd2 = new VariableData("j", _publicMav, SymbolData.INT_TYPE, true, null);

      _sd1.addVar(vd1);
      _sd1.setMethods(new LinkedList<MethodData>());
      md = new MethodData("monkey", _publicMav, new TypeParameter[0], _sd1, new VariableData[]{vd2}, new String[0], 
                          _sd1, NULL_LITERAL);
      vd2.setEnclosingData(md);
      _sd1.addMethod(md);
      
      b.visit(new ClassBodyTypeChecker(_sd1, _btc._file, "", new LinkedList<String>(), new LinkedList<String>(), 
                                       _sd1.getVars(), new LinkedList<Pair<SymbolData, JExpression>>()));
      
      assertEquals("There should be no errors", 0, errors.size());
      
      
      //Test that autoboxing works for an assignment from int to Integer
      ExpressionStatement se = new ExpressionStatement(NONE,
                                   new PlusAssignmentExpression(NONE,
                                       new SimpleNameReference(NONE, new Word(NONE, "i")),
                                       new SimpleNameReference(NONE, new Word(NONE, "j"))));
      
      
      _sd1.addVar(vd2);
      se.visit(new ClassBodyTypeChecker(_sd1, _btc._file, "", new LinkedList<String>(), new LinkedList<String>(), 
                                        _sd1.getVars(), new LinkedList<Pair<SymbolData, JExpression>>()));
//      System.err.println("*******The errant error message is: \n" + errors.get(0).getFirst());
      assertEquals("There should be no errors", 0, errors.size());
      
      //Test that an error is thrown when there is an ambiguous method.
      VariableData vd1Obj = new VariableData("i", _publicMav, symbolTable.get("java.lang.Integer"), true, null);
      VariableData vd1Prim = new VariableData("i", _publicMav, SymbolData.INT_TYPE, true, null);
      VariableData vd2Obj = new VariableData("j", _publicMav, symbolTable.get("java.lang.Short"), true, null);
      VariableData vd2Prim = new VariableData("j", _publicMav, SymbolData.SHORT_TYPE, true, null);
      MethodData md1 = 
        new MethodData("myMethod2", _publicMav, new TypeParameter[0], SymbolData.BOOLEAN_TYPE, 
                       new VariableData[] {vd1Prim, vd2Prim}, new String[0], _sd1, NULL_LITERAL);
      MethodData md2 = 
        new MethodData("myMethod2", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, 
                       new VariableData[] {vd1Obj, vd2Obj}, new String[0], _sd1, NULL_LITERAL);
      vd1Obj.setEnclosingData(md2);
      vd1Prim.setEnclosingData(md1);
      vd2Obj.setEnclosingData(md2);
      vd2Prim.setEnclosingData(md1);
      
      _sd1.setMethods(new LinkedList<MethodData>());
      _sd1.addMethod(md1);
      _sd1.addMethod(md2);
      _sd1.setVars(new LinkedList<VariableData>());
      _sd1.addVar(vd1Prim);
      _sd1.addVar(vd2Obj);
      Expression[] expr2 = 
        new Expression[]{new SimpleNameReference(NONE, new Word(NONE, "i")), 
                         new SimpleNameReference(NONE, new Word(NONE, "j"))};
      e = new SimpleMethodInvocation(NONE,
                                     new Word(NONE, "myMethod2"),
                                     new ParenthesizedExpressionList(NONE, expr2));
      bii = new BodyItemI[] {new ExpressionStatement(NONE, e)};
      b = new BracedBody(NONE, bii);
      
      b.visit(new ClassBodyTypeChecker(_sd1, _btc._file, "", new LinkedList<String>(), new LinkedList<String>(), 
                                       _sd1.getVars(), new LinkedList<Pair<SymbolData, JExpression>>()));
      
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "myMethod2(int, java.lang.Short) is an ambiguous invocation.  " +
                   "It matches both myMethod2(int, short) and myMethod2(java.lang.Integer, java.lang.Short)", 
                   errors.get(0).getFirst());
      
      // test that if a method is overridden that it still works
      SymbolData subSd = new SymbolData("sub");
      subSd.setSuperClass(_sd1);
      subSd.setIsContinuation(false);
      
      _sd1.setMethods(new LinkedList<MethodData>());
      _sd1.addMethod(md1);      
      MethodData md3 = new MethodData("myMethod2", _publicMav, new TypeParameter[0], SymbolData.BOOLEAN_TYPE, 
                                      new VariableData[] {vd1Prim, vd2Prim}, new String[0], _sd1, 
                                      NULL_LITERAL);
      b.visit(new ClassBodyTypeChecker(subSd, _btc._file, "", new LinkedList<String>(), new LinkedList<String>(), 
                                       _sd1.getVars(), new LinkedList<Pair<SymbolData, JExpression>>()));
      assertEquals("There should still be one error", 1, errors.size());  // Generated a duplicate error
      
      // test that if a sd1 has something that's ambiguous, so the superclass is ambiguous, the error is only thrown 
      // once when calling the method in the subclass.
      subSd.setMethods(new LinkedList<MethodData>());
      _sd1.addMethod(md2);
      b.visit(new ClassBodyTypeChecker(subSd, _btc._file, "", new LinkedList<String>(), new LinkedList<String>(), 
                                       _sd1.getVars(), new LinkedList<Pair<SymbolData, JExpression>>()));
      assertEquals("There should still be one error", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "myMethod2(int, java.lang.Short) is an ambiguous invocation.  It matches both " +
                   "myMethod2(int, short) and myMethod2(java.lang.Integer, java.lang.Short)", 
                   errors.get(0).getFirst());
    }
    
    public void testForInnerClassDef() {
      //TODO: implement this test (for advanced level)
    }
  }
}