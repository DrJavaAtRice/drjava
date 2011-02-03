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
import edu.rice.cs.javalanglevels.util.Utilities;
import java.util.*;
import java.io.File;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.iter.*;

import junit.framework.TestCase;

/** This is a TypeChecker for all Expressions used in the students files.  It is used with every LanguageLevel. */
public class ExpressionTypeChecker extends SpecialTypeChecker {
  
  public static final JavaVersion JAVA_VERSION = LanguageLevelConverter.OPT.javaVersion();
  public static final NullLiteral NULL_LITERAL = new NullLiteral(SourceInfo.NONE);
  
  /** Simply pass the necessary information on to superclass constructor.
    * @param data  The data that represents the context.  TODO: What classes can it be?
    * @param file  The file that corresponds to the source file
    * @param packageName  The string representing the package name
    * @param importedFiles  The list of file names that have been specifically imported
    * @param importedPackages  The list of package names that have been specifically imported
    * @param vars  The list of fields that have been assigned up to the point where SpecialTypeChecker is called.
    * @param thrown  The list of exceptions that the context is declared to throw
    */
  public ExpressionTypeChecker(Data data, File file, String packageName, LinkedList<String> importedFiles, 
                               LinkedList<String> importedPackages, LinkedList<VariableData> vars, 
                               LinkedList<Pair<SymbolData, JExpression>> thrown) {
    super(data, file, packageName, importedFiles, importedPackages, vars, thrown);
    if (vars == null) throw new RuntimeException("vars == null in new ExpressionTypeChecker operation");
  }
  
  
  /** Visit the lhs of this assignment with LValueTypeChecker, which does extra checking.  Visit the rhs of this
    * assignment with the regular expression type checker, since anything normal expression can appear on the right.
    * @param that  The SimpleAssignmentExpression to type check
    * @return  The result of the assignment.
    */
  public TypeData forSimpleAssignmentExpression(SimpleAssignmentExpression that) {
    TypeData valueRes = that.getValue().visit(this);
    TypeData nameRes = that.getName().visit(new LValueTypeChecker(this));
    return forSimpleAssignmentExpressionOnly(that, nameRes, valueRes);
  }
  
  /** A SimpleAssignmentExpression is okay if both lhs and rhs are instances, and rhs is assignable to lhs. Give an 
    * error if these constraints are not met.  Return an instance of the lhs or null if the left or right could not be resolved.
    * @param that  The SimpleAssignmentExpression being typechecked
    * @param nameRes  The TypeData representing the lhs of the assignment
    * @param valueRes  The TypeData representing the rhs of the assignment
    */
  public TypeData forSimpleAssignmentExpressionOnly(SimpleAssignmentExpression that, TypeData nameRes, 
                                                    TypeData valueRes) {
    if (nameRes == null || valueRes == null) {return null;}
    
    //make sure that both lhs and rhs could be resolved (not PackageDatas)
    if (!assertFound(nameRes, that) || !assertFound(valueRes, that)) {
      return null;
    }
    
    // make sure both are instance datas
    if (assertInstanceType(nameRes, "You cannot assign a value to the type " + nameRes.getName(), that) &&
        assertInstanceType(valueRes, "You cannot use the type name " + valueRes.getName() + 
                           " on the right hand side of an assignment", that)) {
      
      // make sure the rhs can be assigned to the lhs
      if (! valueRes.getSymbolData().isAssignableTo(nameRes.getSymbolData(), JAVA_VERSION)) {
        _addError("You cannot assign something of type " + valueRes.getName() + " to something of type " + 
                  nameRes.getName(), that);
      }
    }   
    return nameRes.getInstanceData();
  }
  
  /** Visit the lhs of this assignment with LValueWithValueTypeChecker, which does extra checking.  Visit the rhs of
    * this assignment with regular expression type checker, since anything regular expression can appear on the right.
    * @param that  The PlusAssignmentExpression to type check
    * @return  The result of the assignment.
    */
  public TypeData forPlusAssignmentExpression(PlusAssignmentExpression that) {
    TypeData valueRes = that.getValue().visit(this);
    TypeData nameRes = that.getName().visit(new LValueWithValueTypeChecker(this));
    
    return forPlusAssignmentExpressionOnly(that, nameRes, valueRes);
  }
  
  /** A PlusAssignmentExpression is okay if the lhs and rhs are both instances.  If the lhs is a string, the rhs can
    * be anything.  If the rhs is a string, the lhs had better be a string too.  If neither of them is a string, they 
    * should both be numbers, and the rhs should be assignable to the lhs.
    * @param that  The PlusAssignmentExpression we are typechecking
    * @param nameRes  The TypeData representing the lhs
    * @param valueRes  The TypeData representing the rhs
    * @return  An instance of the result of the lhs, or null if either the right or the left could not be resolved.
    */
  public TypeData forPlusAssignmentExpressionOnly(PlusAssignmentExpression that, TypeData nameRes, 
                                                  TypeData valueRes) {
    if (nameRes == null || valueRes == null) {return null;}
    
    //make sure that both lhs and rhs could be resolved (not PackageDatas)
    if (! assertFound(nameRes, that) || !assertFound(valueRes, that)) {
      return null;
    }
    
    // need to see if rhs is a String.
    SymbolData string = getSymbolData("java.lang.String", that, false, false);
    
    if (nameRes.getSymbolData().isAssignableTo(string, JAVA_VERSION)) {
      //the rhs is a String, so just make sure they are both instance types.
      assertInstanceType(nameRes, "The arguments to a Plus Assignment Operator (+=) must both be instances, " + 
                         "but you have specified a type name", that);
      assertInstanceType(valueRes, "The arguments to a Plus Assignment Operator (+=) must both be instances, " + 
                         "but you have specified a type name", that);
      return string.getInstanceData();
    }
    
    else { // neither is a string, so they must both be numbers
      if (!nameRes.getSymbolData().isNumberType(JAVA_VERSION) ||
          !valueRes.getSymbolData().isNumberType(JAVA_VERSION)) {
        _addError("The arguments to the Plus Assignment Operator (+=) must either include an instance of a String " + 
                  "or both be numbers.  You have specified arguments of type " + nameRes.getName() + " and " + 
                  valueRes.getName(), that);
        return string.getInstanceData(); // return String by default
      }
      
      else if (! valueRes.getSymbolData().isAssignableTo(nameRes.getSymbolData(), 
                                                             JAVA_VERSION)) {
        _addError("You cannot increment something of type " + nameRes.getName() + " with something of type " + 
                  valueRes.getName(), that);
      }
      
      else {
        assertInstanceType(nameRes, "The arguments to the Plus Assignment Operator (+=) must both be instances, " + 
                           "but you have specified a type name", that);
        assertInstanceType(valueRes, "The arguments to the Plus Assignment Operator (+=) must both be instances, " + 
                           "but you have specified a type name", that);
      }
      
      return nameRes.getInstanceData();
    }
  }
  
  /** Visit the lhs of this assignment with the LValueWithValueTypeChecker, which does extra checking for the lhs, 
    * because it needs to be able to be assigned to and already have a value.  Visit the rhs of this assignment 
    * with the regular expression type checker, since any regular expression can appear on the right.
    * @param that  The NumericAssignmentExpression to type check
    * @return  The result of the assignment.
    */
  public TypeData forNumericAssignmentExpression(NumericAssignmentExpression that) {
    TypeData valueRes = that.getValue().visit(this);
    TypeData nameRes = that.getName().visit(new LValueWithValueTypeChecker(this));
    
    return forNumericAssignmentExpressionOnly(that, nameRes, valueRes);
  }
  
  /** Delegate to method for super class. */
  public TypeData forMinusAssignmentExpression(MinusAssignmentExpression that) {
    return forNumericAssignmentExpression(that);
  }
  
  /** Delegate to method for super class. */
  public TypeData forMultiplyAssignmentExpression(MultiplyAssignmentExpression that) {
    return forNumericAssignmentExpression(that);
  }
  
  /** Delegate to method for super class. */
  public TypeData forDivideAssignmentExpression(DivideAssignmentExpression that) {
    return forNumericAssignmentExpression(that);
  }
  
  /** Delegate to method for super class. */ 
  public TypeData forModAssignmentExpression(ModAssignmentExpression that) {
    return forNumericAssignmentExpression(that);
  }
  
  /** A NumericAssignmentExpression is okay if both the lhs and the rhs are instances, both are numbers, and the rhs
    * is assignable to the lhs. Return the lhs, or null
    * @param that  The SimpleAssignmentExpression being typechecked
    * @param nameRes  The TypeData representing the lhs of the assignment
    * @param valueRes  The TypeData representing the rhs of the assignment
    * @return  An instance of the lhs, or null if the lhs or rhs could not be resolved.
    */
  public TypeData forNumericAssignmentExpressionOnly(NumericAssignmentExpression that, TypeData nameRes, 
                                                     TypeData valueRes) {
    if (nameRes == null || valueRes == null) {return null;}
    
    //make sure that both lhs and rhs could be resolved (not PackageDatas)
    if (!assertFound(nameRes, that) || !assertFound(valueRes, that)) {
      return null;
    }
    
    //make sure both are instance datas
    if (assertInstanceType(nameRes, "You cannot use a numeric assignment (-=, %=, *=, /=) on the type " + 
                           nameRes.getName(), that) &&
        assertInstanceType(valueRes, "You cannot use the type name " + valueRes.getName() + 
                           " on the left hand side of a numeric assignment (-=, %=, *=, /=)", that)) {
      
      boolean error = false;
      //make sure that both lhs and rhs are number types:
      if (!nameRes.getSymbolData().isNumberType(JAVA_VERSION)) {
        _addError("The left side of this expression is not a number.  " + 
                  "Therefore, you cannot apply a numeric assignment (-=, %=, *=, /=) to it", that);
        error=true;
      }
      if (!valueRes.getSymbolData().isNumberType(JAVA_VERSION)) {
        _addError("The right side of this expression is not a number.  " + 
                  "Therefore, you cannot apply a numeric assignment (-=, %=, *=, /=) to it", that);
        error = true;
      }
      
      // Make sure the lhs is parent type of rhs  NOTE: technically this is allowable in full java but inconsistent
      // with the fact that you cannot say int i = 0; i = i + 4.2;  To avoid student confusion, we will not allow it.
      if (!error && !valueRes.getSymbolData().isAssignableTo(nameRes.getSymbolData(), 
                                                                 JAVA_VERSION)) {
        _addError("You cannot use a numeric assignment (-=, %=, *=, /=) on something of type " + nameRes.getName() + 
                  " with something of type " + valueRes.getName(), that);
      }
    }  
    return nameRes.getInstanceData();  
  }
  
  /** Not currently supported. */
  public TypeData forShiftAssignmentExpressionOnly(ShiftAssignmentExpression that, TypeData nameRes, 
                                                   TypeData valueRes) {
    throw new RuntimeException ("Internal Program Error: Shift assignment operators are not supported.  " + 
                                "This should have been caught before the TypeChecker.  Please report this bug.");
  }
  
  /** Not currently supported. */
  public TypeData forBitwiseAssignmentExpressionOnly(BitwiseAssignmentExpression that, TypeData nameRes, TypeData valueRes) {
    throw new RuntimeException ("Internal Program Error: Bitwise assignment operators are not supported.  " + 
                                "This should have been caught before the TypeChecker.  Please report this bug.");
  }
  
  /** Checks if this BooleanExpression is well-formed, i.e., that left and right arguments are well-formed boolean
    * expressions.  Throws an appropriate error if ill-formed.  Always returns the boolean instance type.
    * @param that  The BooleanExpression being checked
    * @param left_result  The result from visiting the left side of the BooleanExpression
    * @param right_result  The result from visiting the right side of the BooleanExpression
    * @return  The boolean instance type
    */
  public TypeData forBooleanExpressionOnly(BooleanExpression that, TypeData left_result, TypeData right_result) {
    if (left_result == null || right_result == null)  return null;
    
    // Make sure that both lhs and rhs could be resolved (not PackageDatas)
    if (! assertFound(left_result, that) || ! assertFound(right_result, that)) return null;
    
    if (assertInstanceType(left_result, "The left side of this expression is a type, not an instance", that) &&
        !left_result.getSymbolData().isAssignableTo(SymbolData.BOOLEAN_TYPE, JAVA_VERSION)) {
      
      _addError("The left side of this expression is not a boolean value.  " + 
                "Therefore, you cannot apply a Boolean Operator (&&, ||) to it", that);
    }
    
    if (assertInstanceType(right_result, "The right side of this expression is a type, not an instance", that) &&
        ! right_result.getSymbolData().isAssignableTo(SymbolData.BOOLEAN_TYPE, JAVA_VERSION)) {
      
      _addError("The right side of this expression is not a boolean value.  " + 
                "Therefore, you cannot apply a Boolean Operator (&&, ||) to it", that);
    }
    
    
    return SymbolData.BOOLEAN_TYPE.getInstanceData();
  }
  
  /** Not currently supported. */
  public TypeData forBitwiseBinaryExpressionOnly(BitwiseBinaryExpression that, TypeData left_result, 
                                                 TypeData right_result) {
    throw new RuntimeException ("Internal Program Error: Bitwise operators are not supported.  " + 
                                "This should have been caught before the TypeChecker.  Please report this bug.");
  }
  
  /** This EqualityExpression is badly formed if left_result and right_result have incompatible types.  Both left 
    * and the right should be instance datas.  Throws an error if ill-formed.  Returns the InstanceData corresponding
    * to boolean, the return type from an equality check.
    * @param that  The EqualityExpression being checked
    * @param left_result  The result of visiting the left side of the expression
    * @param right_result  The result of visiting the right side of the expression
    * @return  SymbolData.BOOLEAN_TYPE.getInstanceData()
    */
  public TypeData forEqualityExpressionOnly(EqualityExpression that, TypeData left_result, TypeData right_result) {
    if (left_result == null || right_result == null) return null;
    
    //make sure that both lhs and rhs could be resolved (not PackageDatas)
    if (!assertFound(left_result, that) || !assertFound(right_result, that)) return null;
    
    //if either left or right are primitive, the must either be both numeric or both boolean
    SymbolData left = left_result.getSymbolData();
    SymbolData right = right_result.getSymbolData();
    if (left.isPrimitiveType() || right.isPrimitiveType()) {
      if (!((left.isNumberType(JAVA_VERSION) &&
             right.isNumberType(JAVA_VERSION)) ||
            (left.isAssignableTo(SymbolData.BOOLEAN_TYPE, JAVA_VERSION)
               && right.isAssignableTo(SymbolData.BOOLEAN_TYPE, JAVA_VERSION)))) {
        _addError("At least one of the arguments to this Equality Operator (==, !=) is primitive.  Therefore, they " + 
                  "must either both be number types or both be boolean types.  You have specified expressions with type " +
                  left_result.getName() + " and " + right_result.getName(), that);
      }
    }
    
    //otherwise, anything goes...just check for instance types
    
    assertInstanceType(left_result, "The arguments to this Equality Operator(==, !=) must both be instances.  " +
                       "Instead, you have referenced a type name on the left side", that);
    assertInstanceType(right_result, "The arguments to this Equality Operator(==, !=) must both be instances.  " + 
                       "Instead, you have referenced a type name on the right side", that);
    
    return SymbolData.BOOLEAN_TYPE.getInstanceData();
  }
  
  /** Verify that both the left and right of this comparison expression are number types and InstanceDatas.  Give an
    * error if this is not the case.  Return the InstanceData for boolean, since that is the result of a comparison 
    * expression.
    * @param that  The Comparison expression being type-checked
    * @param left_result  The result of visiting the left side of the expression
    * @param right_result  The result of visiting the right side of the expression
    * @return  SymbolData.BOOLEAN_TYPE.getInstanceData()
    */
  public TypeData forComparisonExpressionOnly(ComparisonExpression that, TypeData left_result, TypeData right_result) {
    if (left_result == null || right_result == null) {return null;}
    
    //make sure that both lhs and rhs could be resolved (not PackageDatas)
    if (!assertFound(left_result, that) || !assertFound(right_result, that)) return null;
    
    if (!left_result.getSymbolData().isNumberType(JAVA_VERSION)) {
      _addError("The left side of this expression is not a number.  Therefore, you cannot apply a Comparison Operator" +
                " (<, >; <=, >=) to it", that);
    }
    else {
      assertInstanceType(left_result, "The left side of this expression is a type, not an instance", that);
    }
    
    if (!right_result.getSymbolData().isNumberType(JAVA_VERSION)) {
      _addError("The right side of this expression is not a number.  Therefore, you cannot apply a Comparison Operator" +
                " (<, >; <=, >=) to it", that);
    }
    else {
      assertInstanceType(right_result, "The right side of this expression is a type, not an instance", that);
    }    
    
    return SymbolData.BOOLEAN_TYPE.getInstanceData();
  }
  
  /**
   * Not currently supported
   */
  public TypeData forShiftBinaryExpressionOnly(ShiftBinaryExpression that, TypeData left_result, TypeData right_result) {
    throw new RuntimeException ("Internal Program Error: BinaryShifts are not supported.  " + 
                                "This should have been caught before the TypeChecker.  Please report this bug.");
  }
  
  
  /**
   * A plus operator can either be used on a string and any other type of object or on two numbers.  If one of the arguments
   * is of String type, check to make sure that both types are InstanceDatas and then return an InstanceData for String.
   * If neither of the arguments are a String type, verify that they are both number types and both InstanceDatas, and return
   * the Instance Data corresponding to their least restrictive type.
   * @param that  The PlusExpression being type-checked.
   * @param left_result  The result of visiting the left side of this plus expression
   * @param right_result  The result of visiting the right side of this plus expression
   */
  public TypeData forPlusExpressionOnly(PlusExpression that, TypeData left_result, TypeData right_result) {
    if (left_result == null || right_result == null) {return null;}
    
    //make sure that both lhs and rhs could be resolved (not PackageDatas)
    if (!assertFound(left_result, that) || !assertFound(right_result, that)) {
      return null;
    }
    
    SymbolData string = getSymbolData("java.lang.String", that, false, false);
    
    if (left_result.getSymbolData().isAssignableTo(string, JAVA_VERSION) ||
        right_result.getSymbolData().isAssignableTo(string, JAVA_VERSION)) {
      //one of these is a String, so just make sure they are both instance types.
      assertInstanceType(left_result, "The arguments to the Plus Operator (+) must both be instances, " + 
                         "but you have specified a type name", that);
      assertInstanceType(right_result, "The arguments to the Plus Operator (+) must both be instances, " + 
                         "but you have specified a type name", that);
      return string.getInstanceData();
    }
    
    else { //neither is a string, so they must both be numbers
      if (!left_result.getSymbolData().isNumberType(JAVA_VERSION) ||
          !right_result.getSymbolData().isNumberType(JAVA_VERSION)) {
        _addError("The arguments to the Plus Operator (+) must either include an instance of a String or both be" + 
                  " numbers.  You have specified arguments of type " + left_result.getName() + " and " + 
                  right_result.getName(), that);
        return string.getInstanceData(); //return String by default
      }
      else {
        assertInstanceType(left_result, "The arguments to the Plus Operator (+) must both be instances, but you have" + 
                           " specified a type name", that);
        assertInstanceType(right_result, "The arguments to the Plus Operator (+) must both be instances, but you have" + 
                           " specified a type name", that);
      }
      
      return _getLeastRestrictiveType(left_result.getSymbolData(), right_result.getSymbolData()).getInstanceData();
      
    }
  }
  
  /**
   * Check if this NumericBinaryExpression was okay.  It is not okay if either the left or the right result are not number types
   * or if they are not instance datas.  Throw an appropriate error if any of these is the case.  Always return the least
   * restrictive subtype of the left and the right.
   * @param that  The NumericBinaryExpression being checked
   * @param left_result  The result from visiting the left side of the NumericBinaryExpression
   * @param right_result  The result from visiting the right side of the NumericBinaryExpression
   * @return  An InstanceData of the least restrictive type of the left and right sides.
   */
  public TypeData forNumericBinaryExpressionOnly(NumericBinaryExpression that, TypeData left_result, TypeData right_result) {
    if (left_result == null || right_result == null) {return null;}
    
    //make sure that both lhs and rhs could be resolved (not PackageDatas)
    if (!assertFound(left_result, that) || !assertFound(right_result, that)) {
      return null;
    }
    
    if (assertInstanceType(left_result, "The left side of this expression is a type, not an instance", that) &&
        !left_result.getSymbolData().isNumberType(JAVA_VERSION)) {
      
      _addError("The left side of this expression is not a number.  Therefore, you cannot apply a Numeric Binary" + 
                " Operator (*, /, -, %) to it", that);
      return right_result.getInstanceData();
    }
    
    if (assertInstanceType(right_result, "The right side of this expression is a type, not an instance", that) &&
        !right_result.getSymbolData().isNumberType(JAVA_VERSION)) {
      
      _addError("The right side of this expression is not a number.  Therefore, you cannot apply a Numeric Binary " + 
                "Operator (*, /, -, %) to it", that);
      return left_result.getInstanceData();
    }
    
    
    return _getLeastRestrictiveType(left_result.getSymbolData(), right_result.getSymbolData()).getInstanceData();
  }
  
  /**
   * This should have been caught in the first pass.  Throw a RuntimeException.
   */
  public TypeData forNoOpExpressionOnly(NoOpExpression that, TypeData left_result, TypeData right_result) {
    throw new RuntimeException("Internal Program Error: The student is missing an operator.  " + 
                               "This should have been caught before the TypeChecker.  Please report this bug.");
  }
  
  
  /**
   * Visit the value of this increment expression with the LValueWithValueTypeChecker, since
   * whatever it represents should already have a value before we try to increment it.
   */
  public TypeData forIncrementExpression(IncrementExpression that) {
    TypeData valueRes = that.getValue().visit(new LValueWithValueTypeChecker(this));
    return forIncrementExpressionOnly(that, valueRes);
  }
  
  
  /**
   * For these concrete instantiations of IncrementExpression, delegate to abstract method
   */
  public TypeData forPositivePrefixIncrementExpression(PositivePrefixIncrementExpression that) {
    return forIncrementExpression(that);
  }
  
  public TypeData forNegativePrefixIncrementExpression(NegativePrefixIncrementExpression that) {
    return forIncrementExpression(that);
  }
  
  public TypeData forPositivePostfixIncrementExpression(PositivePostfixIncrementExpression that) {
    return forIncrementExpression(that);
  }
  
  public TypeData forNegativePostfixIncrementExpression(NegativePostfixIncrementExpression that) {
    return forIncrementExpression(that);
  }
  
  
  /** An IncrementExpression is badly formatted if the thing being incremented is a type (valueRes is not an 
    * InstanceData) or if the value being incremented cannot be assigned to.  Throw an error in either of these cases.
    * @param that  The IncrementExpression that is being type checked.
    * @param valueRes  The result of evaluating the argument to the increment expression.
    * @return  The type of what is being incremented.
    */
  public TypeData forIncrementExpressionOnly(IncrementExpression that, TypeData valueRes) {
    if (valueRes == null) {return null;}
    
    //make sure that lhs could be resolved (not PackageData)
    if (!assertFound(valueRes, that)) {
      return null;
    }
    
    if (assertInstanceType(valueRes, "You cannot increment or decrement " + valueRes.getName() + 
                           ", because it is a class name not an instance", that)) {
      if (!valueRes.getSymbolData().isNumberType(JAVA_VERSION)) {
        _addError("You cannot increment or decrement something that is not a number type." + 
                  "  You have specified something of type " + valueRes.getName(), that);
      }
    }
    return valueRes.getInstanceData();
  }
  
  /** A NumericUnaryExpression was well-formed if its valueRes is an instance type and if its valueRes's symbol 
    * data is a number type (to which a double can be assigned).  If this numeric unary expression was not well formed, 
    * throw an error.
    * @param that  The NumericUnaryExpression being evaluated
    * @param valueRes  The result of evaluating the argument to this expression.
    * @return  The new result of this expression.
    */
  public TypeData forNumericUnaryExpressionOnly(NumericUnaryExpression that, TypeData valueRes) {
    if (valueRes==null) {return null;}
    
    //make sure that lhs could be resolved (not PackageData)
    if (!assertFound(valueRes, that)) {
      return null;
    }
    
    if (assertInstanceType(valueRes, "You cannot use a numeric unary operator (+, -) with " + valueRes.getName() + 
                           ", because it is a class name, not an instance", that) &&
        !valueRes.getSymbolData().isNumberType(JAVA_VERSION)) {
      
      _addError("You cannot apply this unary operator to something of type " + valueRes.getName() + 
                ".  You can only apply it to a numeric type such as double, int, or char", that);
      return valueRes;
    }
    
    //call this so that chars and bytes are widened to an int.
    return _getLeastRestrictiveType(valueRes.getSymbolData(), SymbolData.INT_TYPE).getInstanceData();
  }
  
  /** Not Currently Supported. */
  public TypeData forBitwiseNotExpressionOnly(BitwiseNotExpression that, TypeData valueRes) {
    throw new RuntimeException("Internal Program Error: BitwiseNot is not supported.  " + 
                               "It should have been caught before getting to the TypeChecker.  Please report this bug.");
  }
  
  
  /** A NotExpression is illformed if its argument is not an instance type or its argument is not of type boolean.  Give
    * an error if this is the case.  Always return SymbolData.BOOLEAN_TYPE.getInstanceData() since this is the correct 
    * type for this expression.
    * @param that  The NotExpression being type-checked
    * @param valueRes  The type of the argument to the NotExpression
    * @return  SymbolData.BOOLEAN_TYPE.getInstanceData()
    */
  public TypeData forNotExpressionOnly(NotExpression that, TypeData valueRes) {
    if (valueRes == null) {return null;}
    
    //make sure that lhs could be resolved (not PackageData)
    if (!assertFound(valueRes, that)) {
      return null;
    }
    
    if (assertInstanceType(valueRes, 
                           "You cannot use the not (!) operator with " + valueRes.getName() + 
                           ", because it is a class name, not an instance", that) &&
        ! valueRes.getSymbolData().isAssignableTo(SymbolData.BOOLEAN_TYPE, 
                                                      JAVA_VERSION)) {
      
      _addError("You cannot use the not (!) operator with something of type " + valueRes.getName() + 
                ". Instead, it should be used with an expression of boolean type", that);
    }
    
    return SymbolData.BOOLEAN_TYPE.getInstanceData(); //it should always be a boolean type.
    
  }
  
  /** Not currently supported */
  public TypeData forConditionalExpressionOnly(ConditionalExpression that, TypeData condition_result, 
                                               TypeData forTrue_result, TypeData forFalse_result) {
    throw new RuntimeException ("Internal Program Error: Conditional expressions are not supported.  " + 
                                "This should have been caught before the TypeChecker.  Please report this bug.");
  }
  
  /** Checks to see if this InstanceofExpression is okay.  It is not okay if typeRes is not a SymbolData, 
    * valueRes is not an InstanceData, or if valueRes cannot be cast to typeRes.  If any of these are true,
    * give an appropriate error message.  Return an instance data corresponding to typeRes.
    * @param that  The CastExpression being examined.
    * @param typeRes  The type to be checked
    * @param valueRes  The instance type of what is being checked
    * @return  typeRes's instance data.
    */
  public TypeData forInstanceofExpressionOnly(InstanceofExpression that, TypeData typeRes, TypeData valueRes) {
    if (typeRes == null)  return null; 
    
    // Make sure that lhs could be resolved (not PackageData)
    if (! assertFound(valueRes, that) || ! assertFound(typeRes, that)) return null;
    
    if (typeRes.isInstanceType()) {
      _addError("You are trying to test if an expression value belongs to an instance of a type, which is not allowed."
                  + "  Perhaps you meant to check membership in the type itself, " + typeRes.getName(),
                that);
    }
    
    else if (assertInstanceType(valueRes, "You are trying to test if " + valueRes.getName() + 
                                " belongs to type, but it is a class or interface type, not an instance", that) 
               && ! valueRes.getSymbolData().isCastableTo(typeRes.getSymbolData(), JAVA_VERSION)) {
      
      _addError("You cannot test whether an expression of type " + valueRes.getName() + " belongs to type "
                  + typeRes.getName() + " because they are not related", 
                that);
    }
    
    return SymbolData.BOOLEAN_TYPE.getInstanceData();
  }
  
  /** Checks to see if this CastExpression is okay.  It is not okay if typeRes is not a SymbolData, valueRes is 
    * not an InstanceData, or if valueRes cannot be cast to typeRes.  If any of these are the case, give an
    * appropriate error message.  Return an instance data corresponding to typeRes.
    * @param that  The CastExpression being examined.
    * @param typeRes  The type of the cast expression
    * @param valueRes  The instance type of what is being cast
    * @return  typeRes's instance data.
    */
  public TypeData forCastExpressionOnly(CastExpression that, TypeData typeRes, TypeData valueRes) {
    if (typeRes == null || valueRes == null)  return null; 
    
    //make sure that lhs could be resolved (not PackageData)
    if (! assertFound(valueRes, that) || ! assertFound(typeRes, that)) return null;
    
    if (typeRes.isInstanceType()) {
      _addError("You are trying to cast to an instance of a type, which is not allowed.  " + 
                "Perhaps you meant to cast to the type itself, " + typeRes.getName(), that);
    }
    
    else if (assertInstanceType(valueRes, "You are trying to cast " + valueRes.getName() + 
                                ", which is a class or interface type, not an instance", that) &&
             !valueRes.getSymbolData().isCastableTo(typeRes.getSymbolData(), 
                                                        JAVA_VERSION)) {
      
      _addError("You cannot cast an expression of type " + valueRes.getName() + " to type " + 
                typeRes.getName() + " because they are not related", that);
    }
    
    return typeRes.getInstanceData();
  }
  
  
  /** Gives a Runtime Exception, because the fact that there is an EmptyExpression here should have been caught before 
    * the TypeChecker pass.
    */
  public TypeData forEmptyExpressionOnly(EmptyExpression that) {
    throw new RuntimeException("Internal Program Error: EmptyExpression encountered.  Student is missing something." + 
                               "  Should have been caught before TypeChecker.  Please report this bug.");
  }
  
  /** Visit the ClassInstantiation's arguments.  Lookup the required constructor matching the ClassInstantiation's 
    * argument types.  Check accessibility of the constructor.  In all cases, returns classToInstantiate.getInstanceData.
    */
  public InstanceData classInstantiationHelper(ClassInstantiation that, SymbolData classToInstantiate) {
    if (classToInstantiate == null) {return null;}
    Expression[] expr = that.getArguments().getExpressions();
    InstanceData[] args = new InstanceData[expr.length];
    for (int i = 0; i<expr.length; i++) {
      Expression e = expr[i];
      TypeData type = e.visit(this);
      if (type == null || !assertFound(type, expr[i]) || 
          ! assertInstanceType(type, "Cannot pass a class or interface name as a constructor argument", e)) {
        // by default, return an instance type of context
        return classToInstantiate.getInstanceData();
      }
      args[i] = type.getInstanceData();
    }
    
    MethodData md = 
      _lookupMethod(LanguageLevelVisitor.getUnqualifiedClassName(that.getType().getName()), classToInstantiate, args, 
                    that, "No constructor found in class " + Data.dollarSignsToDots(classToInstantiate.getName()) + 
                    " with signature: ", true, _getData().getSymbolData());
    
    if (md == null) {return classToInstantiate.getInstanceData();}
    
    //if MethodData is declared to throw exceptions, add them to thrown list:
    String[] thrown = md.getThrown();
    for (int i = 0; i<thrown.length; i++) {
      _thrown.addLast(new Pair<SymbolData, JExpression>(getSymbolData(thrown[i], _getData(), that), that));
    }
    
    return classToInstantiate.getInstanceData();
  }
  
  
  
  
  /** Handles a simple class instantiation.  If the type of the instantiation is not resolved, returns null because an
    * error has already been thrown.  Also checks to see if the class being instantiated is non-static, is not a top 
    * level class, and the name used has a dot in it.  If so, then a non-static inner class is being referenced like 
    * a static inner class, and an error is thrown. After performing these checks, delegates to the class Instantion 
    * helper, which will resolve the type of the class.
    * @return  The InstanceData corresponding to the instantiation
    */
  public TypeData forSimpleNamedClassInstantiation(SimpleNamedClassInstantiation that) {
    SymbolData type = getSymbolData(that.getType().getName(), _getData(), that);
    if (type == null) {return null;}
    // Cannot instantiate a non-static inner class from a static context (i.e. new A.B() where B is dynamic).
    // Here, we make sure that if B is non-static, it is not an inner class of anything.
    String name = that.getType().getName();
    int lastIndexOfDot = name.lastIndexOf('.');
    if (!type.hasModifier("static") && (type.getOuterData() != null) && lastIndexOfDot != -1) {
      String firstPart = name.substring(0, lastIndexOfDot);
      String secondPart = name.substring(lastIndexOfDot + 1, name.length()); //skip the dot itself
      _addError(Data.dollarSignsToDots(type.getName()) + " is not a static inner class, and thus cannot be " + 
                "instantiated from this context.  Perhaps you meant to use an instantiation of the form new " + 
                firstPart + "().new " + secondPart + "()", that);
    }
    InstanceData result = classInstantiationHelper(that, type);
    if (result != null && result.getSymbolData().hasModifier("abstract")) {
      _addError(Data.dollarSignsToDots(type.getName()) + " is abstract and thus cannot be instantiated", that);
    }
    return result;
  }
  
  /** Handles this complex named class instantiation.  First, visit the lhs and get the enclosing type.  If the 
    * enclosing type is null, or a PackageData, return null, because an error has already been thrown.  Otherwise, 
    * call the classInstantiationHelper to get a new instance of the rhs, from the context of the lhs.  It is an 
    * error if the class being instantiated is non-static, but it is called from a static context.  It is an error
    * if the class being instantiated is static but it is being called as a.new B();
    * @param that  The ComplexNamedClassInstantiation being created
    * @return  An InstanceData corresponding to the instantiation
    */
  public TypeData forComplexNamedClassInstantiation(ComplexNamedClassInstantiation that) {
    TypeData enclosingType = that.getEnclosing().visit(this);
    if ((enclosingType == null) || ! assertFound(enclosingType, that.getEnclosing())) { return null; }
    
    else {
      //make sure we can see enclosingType
      checkAccess(that, enclosingType.getSymbolData().getMav(), enclosingType.getSymbolData().getName(), 
                         enclosingType.getSymbolData(), _data.getSymbolData(), "class or interface", true);
      
      // TODO: will getSymbolData correctly handle all cases here?
      //TODO: We still do not handle static fields on the lhs correctly.  I think.
      //this call to getSymbolData will throw ambiguous reference error, if appropriate
      SymbolData innerClass = getSymbolData(that.getType().getName(), enclosingType.getSymbolData(), that.getType());
      if (innerClass == null) {return null;}
      
      //make sure we can see inner class
      checkAccess(that, innerClass.getMav(), innerClass.getName(), innerClass, _data.getSymbolData(), 
                         "class or interface", true);
      InstanceData result = classInstantiationHelper(that, innerClass);
      if (result == null) {return null;}
      boolean resultIsStatic = result.getSymbolData().hasModifier("static");
      
      if (!enclosingType.isInstanceType() && !resultIsStatic) {
        _addError ("The constructor of a non-static inner class can only be called on an instance of its" + 
                   " containing class (e.g. new " + Data.dollarSignsToDots(enclosingType.getName()) + "().new " +
                   that.getType().getName() + "())", that);
      }
      else if (resultIsStatic) {
        _addError("You cannot instantiate a static inner class or interface with this syntax.  Instead, try new " + 
                  Data.dollarSignsToDots(result.getName()) + "()", that);
      }
      
      if (result.getSymbolData().hasModifier("abstract")) {
        _addError(Data.dollarSignsToDots(result.getName()) + " is abstract and thus cannot be instantiated", that);
      }
      return result;
    }
  }
  
  
  /** Do the work that is shared between SimpleAnonymousClassInstantiation and ComplexAnonymousClassInstantiation. 
    * Basically, update the anonymous inner class corresponding to the enclosing data and the superC with superC 
    * and accessors, if necessary.
    * @param that  The AnonymousClassInstantiation being processed.
    * @param superC  The SymbolData corresponding to the super class of this instantiation (the type being created)
    */
  public SymbolData handleAnonymousClassInstantiation(AnonymousClassInstantiation that, SymbolData superC) {
//    SymbolData sd = _data.getNextAnonymousInnerClass();
    /* The preceding line changed to following because anonymous class is filed under its enclosing class not 
     * enclosing method. */
    SymbolData sd = superC.getNextAnonymousInnerClass();
//    System.err.println("***** In handleACI(" + that.getType().getName() + ", " + superC + ") sd = " + sd);
//    System.err.println("Inner classes of " + superC + " are: " + superC.getInnerClasses());
    if (sd == null) {
      _addError("Nested anonymous classes are not supported at any language lavel", that);
      return sd;
//      throw new RuntimeException("Internal Program Error: Couldn't find the SymbolData for the anonymous inner class." + 
//                                 "  Please report this bug.");
    }
    if (sd.getSuperClass() == null) {
      if (superC == null) {
        throw new RuntimeException("Internal Program Error:  Superclass data for " + sd + " is null." + 
                                   "  Please report this bug.");
      }
      if (superC.isInterface()) {
        sd.setSuperClass(symbolTable.get("java.lang.Object")); 
        sd.addInterface(superC);
      }
      else { sd.setSuperClass(superC);}
    }
    LanguageLevelVisitor.createAccessors(sd, _file);
    
    return sd;
  }
  
  /** Resolve the type of this anonymous class.  Look it up in the enclosing data, check that
    * it is using a valid constructor through the classInstantiationHelper and visit the body.
    * Make sure that all abstract methods are overwritten.
    * @param that  The SimpleAnonymousClassInstantiation being type-checked
    * @return  The result of type checking the class instantiation.
    */
  public TypeData forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that) {
    /* Note: _data should be the enclosing class. */
//    System.err.println("******** Type-checking the anonymous class " + that);
//    if (! (_data instanceof SymbolData) )
//      System.err.println("********* Type-checking following anon class blows up " + that);
//                                     
//    assert _data instanceof SymbolData;
    
    SymbolData enclosing = _data.getSymbolData();  // grabs the enclosing class if _data not already a SymbolData
    
    if (enclosing.isDoublyAnonymous()) {
      _addError(enclosing + "is a nested anonymous class, which is not supported at any language level", that);
      return null;
    }
//    System.err.println("***** forSACInst called for anon class in " + enclosing);
    final SymbolData superClass = getSymbolData(that.getType().getName(), enclosing, that); // resolve super class
//    System.err.println("**** SuperClass symbol is " + superClass);
    // Get this anonymous inner class's SymbolData, and finish resolving it.
    SymbolData myData = handleAnonymousClassInstantiation(that, enclosing /*.getEnclosingClass() */);
//    System.err.println("This anonymous class's symbol is: " + myData);
    if (myData == null) return null;
    
    // Cannot instantiate a non-static inner class from a static context (i.e. new A.B() where B is dynamic).
    // Here, we make sure that if B is non-static, it is not an inner class of anything.
    String name = that.getType().getName();
    int lastIndexOfDot = name.lastIndexOf('.');
    if (!superClass.hasModifier("static") && !superClass.isInterface() && 
        (superClass.getOuterData() != null) && lastIndexOfDot != -1) {
      String firstPart = name.substring(0, lastIndexOfDot);
      String secondPart = name.substring(lastIndexOfDot + 1, name.length());
      _addError(Data.dollarSignsToDots(superClass.getName()) + 
                " is not a static inner class, and thus cannot be instantiated from this context." + 
                "  Perhaps you meant to use an instantiation of the form new " + Data.dollarSignsToDots(firstPart) + 
                "().new " + Data.dollarSignsToDots(secondPart) + "()", that);
    }
    
    
    //if superClass is an interface, then the constructor that should be used is Object--i.e. no arguments
    if (superClass.isInterface()) {
      Expression[] expr = that.getArguments().getExpressions();
      if (expr.length > 0) { 
        _addError("You are creating an anonymous inner class that directly implements an interface, thus you should" + 
                  " use the Object constructor which takes in no arguments.  However, you have specified " + 
                  expr.length + " arguments", that);}
    }
    
    else classInstantiationHelper(that, superClass); //use super class here, since it has constructors in it
    
    
    //clone the variables and visit the body.
    LinkedList<VariableData> vars = cloneVariableDataList(_vars);
    vars.addAll(myData.getVars());
    final TypeData bodyRes = that.getBody().visit(new ClassBodyTypeChecker(myData, _file, _package, _importedFiles, 
                                                                               _importedPackages, vars, _thrown));
    
    
    _checkAbstractMethods(myData, that);
    return myData.getInstanceData();  //but actually return an instance of the anonymous inner class
  }
  
  /** Resolve the type of this anonymous class.  Look it up in enclosing data, check that it is using a valid constructor
    * through the classInstantiationHelper and visit the body. Make sure that all abstract methods are overwritten.  The
    * enclosing data is found by first resolving the enclosing data.  Make sure that if this is an inner class it is being
    * called from the appropriate static/non-static context (see ComplexNamedClassInstantiation for more details).
    * @param that  The SimpleAnonymousClassInstantiation being type-checked
    * @return  The result of type checking the class instantiation.
    */
  public TypeData forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that) {
    /* Note: _data should be the enclosing class. */
//    System.err.println("******** Type-checking the anonymous class " + that);
//    if (! (_data instanceof SymbolData) )
//      System.err.println("********* Type-checking following anon class blows up " + that);
//                                     
//    assert _data instanceof SymbolData;
    
   
    if (_data.isDoublyAnonymous()) {
      _addError(_data + "is a nested anonymous class, which is not supported at any language level", that);
      return null;
    }
    
    SymbolData lexEnclosing = _data.getSymbolData();  // grabs the enclosing class if _data not already a SymbolData
    
    Expression receiver = that.getEnclosing();
    
    // Get the enclosing type as specified by the "receiver" expression.
    TypeData enclosingType = receiver.visit(this);
    
    if ((enclosingType == null) || ! assertFound(enclosingType, that.getEnclosing())) { return null; }
    
    SymbolData enclosing = enclosingType.getSymbolData();
    
    // Make sure we can see enclosing SymbolData from within lexEnclosing
    checkAccess(that, enclosing.getMav(), enclosing.getName(), enclosing, lexEnclosing, "class or interface", true);
    
    final SymbolData superClass = getSymbolData(that.getType().getName(), enclosing, that.getType());
    
    // Get this anonymous inner class's SymbolData; passing lexEnclosing is a hack.  It almost certainly should be
    // enclosing, but the LLV processing contains the same error.  We need to be consistent.
    SymbolData myData = handleAnonymousClassInstantiation(that, lexEnclosing);  // TODO: the wrong enclosing context?
    if (myData == null) return null;
    
    // TODO: will getSymbolData correctly handle all cases here?
    //TODO: We still do not handle static fields on the lhs correctly.  I think.
    
    boolean resultIsStatic;
    
    if (superClass.isInterface()) {
      Expression[] expr = that.getArguments().getExpressions();
      if (expr.length > 0) { 
        _addError("You are creating an anonymous inner class that directly implements an interface, thus you should" + 
                  " use the Object constructor which takes in no arguments.  However, you have specified " + 
                  expr.length + " arguments", that);
      }
      resultIsStatic = true;
    }
    
    
    else { // superClass is an interface...need to do some extra checking for static types.
      InstanceData result = classInstantiationHelper(that, superClass); //use super class here, since it has constructors in it
      if (result == null) return null;
      
      resultIsStatic = result.getSymbolData().hasModifier("static");
    }
    
    if (!enclosingType.isInstanceType() && !resultIsStatic) {
      _addError ("The constructor of a non-static inner class can only be called on an instance of its containing" + 
                 " class (e.g. new " + Data.dollarSignsToDots(enclosingType.getName()) + "().new " + 
                 that.getType().getName() + "())", that);
    }
    
    else if (enclosingType.isInstanceType() && resultIsStatic) {
      _addError("You cannot instantiate a static inner class or interface with this syntax.  Instead, try new " + 
                Data.dollarSignsToDots(superClass.getName()) + "()", that);
    }
    
    //clone the variables and visit the body.
    LinkedList<VariableData> vars = cloneVariableDataList(_vars);
    vars.addAll(myData.getVars());
    
    final TypeData bodyRes = that.getBody().visit(new ClassBodyTypeChecker(myData, _file, _package, _importedFiles, 
                                                                               _importedPackages, vars, _thrown));
    
    //make sure all abstract super class methods are overwritten
    _checkAbstractMethods(myData, that);
    
    return myData.getInstanceData(); //actually return an intance of the anonymous inner class
  }
  
  
  /** SimpleThisConstructorInvocations are not allowed outside of the first line of a constructor. */
  public TypeData forSimpleThisConstructorInvocation(SimpleThisConstructorInvocation that) {
    _addError("This constructor invocations are only allowed as the first statement of a constructor body", that);
    return null;
  }
  
  /** ComplexThisConstructorInvocations are not ever allowed. */
  public TypeData forComplexThisConstructorInvocation(ComplexThisConstructorInvocation that) {
    _addError("Constructor invocations of this form are never allowed", that);
    return null;
  }
  
  /** Try to resolve this SimpleNameReference.  It is either:
   *    1. a field or variable reference (return the instance type of the field/variable)
   *    2. a class or interface name reference (return the type of the class or interface)
   *    3. part of a package reference or an error (return a new package data corresponding to the reference.
   * No need to call forSimpleNameReference only, since all the checking is done here.
   */
  public TypeData forSimpleNameReference(SimpleNameReference that) {
    Word myWord = that.getName();
    myWord.visit(this);
    
    // first, try to resolve this name as a field or variable reference
    
    VariableData reference = getFieldOrVariable(myWord.getText(), _data, _data.getSymbolData(), that, _vars, true, true);
    if (reference != null) {
      if (! reference.hasValue()) {
        _addError("You cannot use " + reference.getName() + " because it may not have been given a value", that.getName());
      }
      
      // if reference is non-static (and not a local variable), but context is static, give error
      if (inStaticMethod() && ! reference.hasModifier("static")  && ! reference.isLocalVariable()) {
        _addError("Non-static variable or field " + reference.getName() + " cannot be referenced from a static context", that);
      }
//      if (reference.getType() == null || reference.getType().getInstanceData() == null) 
//        System.err.println("Expression type checking in " + _data + " blows up; AST = " + that);
      return reference.getType().getInstanceData();  
    }
    
    //next, try to resolve this name as a class or interface reference
    SymbolData classR = findClassReference(null, myWord.getText(), that);
    if (classR != null && classR != SymbolData.AMBIGUOUS_REFERENCE) {
      //Only return the symbolData if it is accessible--otherwise, return PackageData
      if (checkAccess(that, classR.getMav(), classR.getName(), classR, _data.getSymbolData(), 
                             "class or interface", false)) {
        return classR;
      }
    }
    if (classR == SymbolData.AMBIGUOUS_REFERENCE) {return null;}
    
    PackageData packageD = new PackageData(myWord.getText());
    return packageD;
  }
  
  
  /** To resolve this ComplexNameReference, first visit the lhs with an instance of this visitor in order to get its
    * type.  Then, try to figure out how the name reference on the right fits with the type on the left.
    *   1. If the lhs is a package data, then either the rhs is a class reference, or the whole thing is another 
    *      PackageData.
    *   2. If the rhs is a variable or field visible from the context of the lhs, it must be static if the lhs is a 
    *      SymbolData, and regardless, it must have a value to be referenced here.
    *   3. If the rhs references an inner class of the lhs, the lhs must be a SymbolData if the rhs is a static inner
    *      class, and the rhs must be static if the lhs is a SymbolData.
    *   4. Otherwise, give an error because we couldn't resolve the symbol.
    */
  public TypeData forComplexNameReference(ComplexNameReference that) {
    TypeData lhs = that.getEnclosing().visit(this);
    if (lhs == null) return null;   // defensive code based on NullPointerException that MAY be due to lhs == null
    
    Word myWord = that.getName();
    
    //if lhs is a package data, either we found a class reference or this piece is still part of the package
    if (lhs instanceof PackageData) {
      SymbolData classRef =  findClassReference(lhs, myWord.getText(), that);
      if (classRef != null) { return classRef; }
      return new PackageData((PackageData) lhs, myWord.getText());
    }
    if (_data == null) return null;  // intermittent NullPointerException in next line; lhs == null or _data == null
    checkAccess(that, lhs.getSymbolData().getMav(), lhs.getSymbolData().getName(), lhs.getSymbolData(), 
                       _data.getSymbolData(), "class or interface", true);
    
    // if the word is a variable reference, make sure it can be seen from this context
    VariableData reference = getFieldOrVariable(myWord.getText(), lhs.getSymbolData(), _data.getSymbolData(), that);
    if (reference != null) {
      if (lhs instanceof SymbolData) {
        //does this reference a field? if so, it must be static
        if (! reference.hasModifier("static")) {
          _addError("Non-static variable " + reference.getName() + " cannot be accessed from the static context " + 
                    Data.dollarSignsToDots(lhs.getName()) + ".  Perhaps you meant to instantiate an instance of " + 
                    Data.dollarSignsToDots(lhs.getName()), that);
          return reference.getType().getInstanceData();
        }
      }
      
      //make sure it already had a value
      if (!reference.hasValue()) {
        _addError("You cannot use " + reference.getName() + " here, because it may not have been given a value", 
                  that.getName());
      }
      
      return reference.getType().getInstanceData();
    }
    
    //does this reference an inner class? if so, it must be static
    SymbolData sd = getSymbolData(true, myWord.getText(), lhs.getSymbolData(), that, false); // may report error below
    if (sd != null && sd != SymbolData.AMBIGUOUS_REFERENCE) {
      if (!checkAccess(that, sd.getMav(), sd.getName(), sd, _data.getSymbolData(), "class or interface")) {
        return null;
      }
      if (!sd.hasModifier("static")) {
        _addError("Non-static inner class " + Data.dollarSignsToDots(sd.getName()) + 
                  " cannot be accessed from this context.  Perhaps you meant to instantiate it", that);
      }
      
      //you cannot reference static inner classes from the context of an instantiation of their outer class
      else if (lhs instanceof InstanceData) {
        _addError("You cannot reference the static inner class " + Data.dollarSignsToDots(sd.getName()) + 
                  " from an instance of " + Data.dollarSignsToDots(lhs.getName()) + ".  Perhaps you meant to say " 
                    + Data.dollarSignsToDots(sd.getName()), that);
      }
      return sd;
    }
    
    if (sd != SymbolData.AMBIGUOUS_REFERENCE) { 
      _addError("Could not resolve " + myWord.getText() + " from the context of " + Data.dollarSignsToDots(lhs.getName()),
                that);
    }
    return null;
  }
  
  
  /**
   * Make sure we are in a non-static context.
   * @return an instance data corresponding to the enclosing class of this context.
   */
  public TypeData forSimpleThisReference(SimpleThisReference that) {
    if (inStaticMethod()) {
      _addError("'this' cannot be referenced from within a static method", that);
    }
    return _getData().getSymbolData().getInstanceData();
  }
  
  /**
   * Check to make sure that the enclosing result could be resolved and that it a type name.
   * Insure that an enclosing instance of that name exists in the current (non-static) context.
   * Return the instance data corresponding to its "this" field.
   * @param that  The ComplexThisReference we are type-checking
   * @param enclosing_result  The TypeData whose this field is being referenced
   * @return  An InstanceData corresponding to the enclosing_result.
   */
  public TypeData forComplexThisReferenceOnly(ComplexThisReference that, TypeData enclosing_result) {
    //make sure that enclosingResult is not null and not a PackageData.  If it is, return null
    if ((enclosing_result == null) || ! assertFound(enclosing_result, that.getEnclosing())) { return null; }
    
    if (inStaticMethod()) {
      _addError("'this' cannot be referenced from within a static method", that);
    }
    
    if (enclosing_result.isInstanceType()) {
      _addError("'this' can only be referenced from a type name, but you have specified an instance of that type.", that);
    }
    
    SymbolData myData = _getData().getSymbolData();
    if (!myData.isInnerClassOf(enclosing_result.getSymbolData(), true)) {
      // Test whether myData is an inner class of enclosing_result at all.  Somewhat inefficient, but only happens when errors occur.
      if (myData.isInnerClassOf(enclosing_result.getSymbolData(), false)) {
        _addError("You cannot reference " + enclosing_result.getName() + ".this from here, because " + myData.getName() + 
                  " or one of its enclosing classes " +
                  "is static.  Thus, an enclosing instance of " + enclosing_result.getName() + " does not exist", that);
      }
      else {
        _addError("You cannot reference " + enclosing_result.getName() + ".this from here, because " + enclosing_result.getName() + 
                  " is not an outer class of " + myData.getName(), that);
      }
    }
    
    return enclosing_result.getInstanceData();
  }
  
  /** All classes should have a super class, which is java.lang.Object by default.  Looks up this class's super class.
    * If it is null, generates an error (this should never happen).  Otherwise, returns the instance data corresponding
    * to the super class.
    * @param that  The SimpleSuperReference we are resolving.
    * @return  InstanceData corresponding to the super class
    */
  public TypeData forSimpleSuperReference(SimpleSuperReference that) {
    if (inStaticMethod()) {
      _addError("'super' cannot be referenced from within a static method", that);
    }
    SymbolData superClass = _getData().getSymbolData().getSuperClass();
    if (superClass == null) {  //this should never happen, because all classes should have a super class
      _addError("The class " + _getData().getSymbolData().getName() + " does not have a super class", that);
      return null;
    }
    return superClass.getInstanceData();
  }
  
  /** Makes sure that the enclosing result is not null--if it is, return null.  Insure that an 
    * enclosing instance of that name exists in the current (non-static) context.  Give an error if the enclosing_result
    * is not an instance type.  Get its super class, and return an instance data corresponding to it.
    * @param that  The ComplexSuperReference being typechecked
    * @param enclosing_result  The type of the left hand side of this reference.
    * @return  An InstanceData corresponding to the super class of enclosing_result.
    */
  public TypeData forComplexSuperReferenceOnly(ComplexSuperReference that, TypeData enclosing_result) {
    //make sure that enclosing_result is not null and not a PackageData.  If it is, return null
    if ((enclosing_result == null) || ! assertFound(enclosing_result, that.getEnclosing())) { return null; }
    
    if (inStaticMethod()) {
      _addError("'super' cannot be referenced from within a static method", that);
    }
    if (enclosing_result.isInstanceType()) {
      _addError("'super' can only be referenced from a type name, but you have specified an instance of that type.", that);
    }
    
    SymbolData myData = _getData().getSymbolData();
    if (!myData.isInnerClassOf(enclosing_result.getSymbolData(), true)) {
      // Test whether myData is an inner class of enclosing_result.  Inefficient, but only happens when errors occur.
      if (myData.isInnerClassOf(enclosing_result.getSymbolData(), false)) {
        _addError("You cannot reference " + enclosing_result.getName() + ".super from here, because " + myData.getName() + 
                  " or one of its enclosing classes " +
                  "is static.  Thus, an enclosing instance of " + enclosing_result.getName() + " does not exist", that);
      }
      else {
        _addError("You cannot reference " + enclosing_result.getName() + ".super from here, because " + 
                  enclosing_result.getName() + " is not an outer class of " + myData.getName(), that);
      }
    }
    
    SymbolData superClass = enclosing_result.getSymbolData().getSuperClass();
    if (superClass == null) {  //this should never happen, because all classes should have a super class
      _addError("The class " + enclosing_result.getName() + " does not have a super class", that);
      return null;
    }
    
    return superClass.getInstanceData();
  }
  
  
  
  
  /** Make sure the lhs is actually an array type and that the index is an int. */
  public TypeData forArrayAccessOnly(ArrayAccess that, TypeData lhs, TypeData index) {
    //if either lhs or index is null then an error has already been caught--return null
    if (lhs == null || index == null) {return null;}
    
    //if either lhs or index cannot be resolved, give error
    if (!assertFound(lhs, that) || !assertFound(index, that)) {
      return null;
    }
    
    if (assertInstanceType(lhs, "You cannot access an array element of a type name", that) &&
        ! (lhs.getSymbolData() instanceof ArrayData)) {
      _addError("The variable referred to by this array access is a " + lhs.getSymbolData().getName() + ", not an array",
                that);
      return lhs.getInstanceData();
    }
    
    if (assertInstanceType(index, "You have used a type name in place of an array index", that) &&
        ! index.getSymbolData().isAssignableTo(SymbolData.INT_TYPE, JAVA_VERSION)) {
      _addError("You cannot reference an array element with an index of type " + index.getSymbolData().getName() + 
                ".  Instead, you must use an int", that);
    }
    
    return ((ArrayData)lhs.getSymbolData()).getElementType().getInstanceData();
  }
  
  //*** Primitives and Literals *******//
  public TypeData forStringLiteralOnly(StringLiteral that) {
    assert symbolTable.get("java.lang.String") != null;
    return symbolTable.get("java.lang.String").getInstanceData();
  }
  
  public TypeData forIntegerLiteralOnly(IntegerLiteral that) {
    return SymbolData.INT_TYPE.getInstanceData();//forLiteralOnly(that);
  }
  
  public TypeData forLongLiteralOnly(LongLiteral that) {
    return SymbolData.LONG_TYPE.getInstanceData();
  }
  
  public TypeData forFloatLiteralOnly(FloatLiteral that) {
    return SymbolData.FLOAT_TYPE.getInstanceData();
  }
  
  public TypeData forDoubleLiteralOnly(DoubleLiteral that) {
    return SymbolData.DOUBLE_TYPE.getInstanceData();
  }
  
  public TypeData forCharLiteralOnly(CharLiteral that) {
    return SymbolData.CHAR_TYPE.getInstanceData();
  }
  
  public TypeData forBooleanLiteralOnly(BooleanLiteral that) {
    return SymbolData.BOOLEAN_TYPE.getInstanceData();
  }
  
  public TypeData forNullLiteralOnly(NullLiteral that) {
    return SymbolData.NULL_TYPE.getInstanceData();
  }
  
  public TypeData forClassLiteralOnly(ClassLiteral that) {
    return symbolTable.get("java.lang.Class").getInstanceData();
  }
  
  
  /**
   * Check a few constraints on this Parenthesized
   */
  public TypeData forParenthesizedOnly(Parenthesized that, TypeData valueRes) {
    if (valueRes == null) {return null;}
    
    if (!assertFound(valueRes, that.getValue())) {
      return null;
    }
    
    assertInstanceType(valueRes, "This class or interface name cannot appear in parentheses", that);
    return valueRes.getInstanceData();
  }
  
  /** Look up the method called in the method invocation within the context of the context TypeData.
    * Resolve all arguments to the method, and make sure they are instance datas.
    * If an argument is a type, the method cannot be found, or the method is called from a static context but is
    * not static, give appropriate error.
    * If the method is declared to throw any exceptions, add them to the thrown list.
    * @param that  The MethodInvocation we are type checking
    * @param context  The TypeData that should contain the method being invoked.
    */
  public TypeData methodInvocationHelper(MethodInvocation that, TypeData context) {
    Expression[] exprs = that.getArguments().getExpressions();
    TypeData[] args = new TypeData[exprs.length];
    InstanceData[] newArgs = new InstanceData[exprs.length];
    for (int i = 0; i < exprs.length; i++) {
      args[i] = exprs[i].visit(this);
      if (args[i] == null) {
        return null;
      }
      
      if (! assertFound(args[i], that)) return null;
      if (! args[i].isInstanceType()) {
        _addError("Cannot pass a class or interface name as an argument to a method." +
                  "  Perhaps you meant to create an instance or use " + args[i].getName() + ".class", exprs[i]);
      }
      newArgs[i]=args[i].getInstanceData();
      
    }
    
    // Pass in both sd and the current SymbolData so that lookupMethod can check
    // if we have access to the method from here.
    MethodData md = _lookupMethod(that.getName().getText(), context.getSymbolData(), newArgs, that, 
                                  "No method found in class " + context.getName() + " with signature: ", 
                                  false, _getData().getSymbolData());
    
    if (md == null)  return null;
    
    if (! context.isInstanceType() && ! md.hasModifier("static")) {
      _addError("Cannot access the non-static method " + md.getName() + " from a static context", that);
    }
    
    // If MethodData is declared to throw exceptions, add them to thrown list:
    String[] thrown = md.getThrown();
    for (int i = 0; i < thrown.length; i++) {
      _thrown.addLast(new Pair<SymbolData, JExpression>(getSymbolData(thrown[i], _getData(), that), that));
    }
    
    SymbolData returnType = md.getReturnType();
    if (returnType == null) {
      _addError("Internal error: the returnType for " + md + " is null", that);
//      Utilities.show("****** null return type for " + md + " Receiver type is " + context + " File is " + _file 
//                       + " MethodData is " + md);
//      assert false;
      return null;
    }
    
    return returnType.getInstanceData();
  }
  
  /** Tries to match this method invocation to a method in the context.  Here, the context is the enclosing data for
    * where this is being invoked.
    * @param that  SimpleMethodInvocation we are typechecking
    * @return  The return type of the method, or null if method cannot be seen or found.
    */
  //TODO: We should handle static fields too!
  public TypeData forSimpleMethodInvocation(SimpleMethodInvocation that) {
    TypeData context = _getData().getSymbolData().getInstanceData();
    if (inStaticMethod()) context = context.getSymbolData();  // Need SymbolData for context, not instance data.
    return methodInvocationHelper(that, context);
  }
  
  /** Tries to match this method invocation to a method in the context.  Here, the context is the enclosing field of
    * the method invocation.
    * @param that  ComplexMethodInvocation we are typechecking
    * @return  The return type of the method, or null if method cannot be seen or found.
    */
  //TODO: We should handle static fields too!
  public TypeData forComplexMethodInvocation(ComplexMethodInvocation that) {
    TypeData context = that.getEnclosing().visit(this);
    if (! assertFound(context, that.getEnclosing()) || context == null)  return null;
    
    //make sure we can see enclosingType
    checkAccess(that, context.getSymbolData().getMav(), context.getSymbolData().getName(), 
                       context.getSymbolData(), _data.getSymbolData(), "class or interface", true);
    
    // This check insures that only static methods can be called from static contexts; forces rhs to be a static context.
    // WHICH IS WRONG.  If the method call has an explicit receiver object, this property is IRRELEVANT.g
//    if (inStaticMethod()) { context = context.getSymbolData();}
    return methodInvocationHelper(that, context);
  }
  
  
  /** A variable data can be assigned to if it is not final or it does not have a value.
    * (in other words, only final variables that have already been assigned are the only type that cannot be given a value.
    * @param vd  The VariableData to check.
    */
  protected boolean canBeAssigned(VariableData vd) { return ! vd.isFinal() || ! vd.hasValue(); }

  /** Returns the least restrictive numerical type.  According to the JLS: "If an integer 
    * operator other than a shift operator has at least one operand of type long, then the 
    * operation is carried out using 64-bit precision, and the result of the numerical operator 
    * is of type long. If the other operand is not long, it is first widened (§5.1.4) to type 
    * long by numeric promotion (§5.6). Otherwise, the operation is carried out using 32-bit 
    * precision, and the result of the numerical operator is of type int. If either operand 
    * is not an int, it is first widened to type int by numeric promotion."
    * So, check to see if one fo the SymboLDatas is a type less restrictive than int.  If so, return that type,
    * otherwise return INT_TYPE.
    */
  protected SymbolData _getLeastRestrictiveType(SymbolData sd1, SymbolData sd2) {
    if ((sd1.isDoubleType(JAVA_VERSION) &&
         sd2.isNumberType(JAVA_VERSION)) ||
        (sd2.isDoubleType(JAVA_VERSION) &&
         sd1.isNumberType(JAVA_VERSION))) {
      return SymbolData.DOUBLE_TYPE;
    }
    else if ((sd1.isFloatType(JAVA_VERSION) &&
              sd2.isNumberType(JAVA_VERSION)) ||
             (sd2.isFloatType(JAVA_VERSION) &&
              sd1.isNumberType(JAVA_VERSION))) {
      return SymbolData.FLOAT_TYPE;
    }
    else if ((sd1.isLongType(JAVA_VERSION) &&
              sd2.isNumberType(JAVA_VERSION)) ||
             (sd2.isLongType(JAVA_VERSION) &&
              sd1.isNumberType(JAVA_VERSION))) {
      return SymbolData.LONG_TYPE;
    }
    else if (sd1.isBooleanType(JAVA_VERSION) &&
             sd2.isBooleanType(JAVA_VERSION)) {
      return SymbolData.BOOLEAN_TYPE;
    }
    else return SymbolData.INT_TYPE; // NOTE: It seems like any binary operation on number types with only ints, shorts, chars, or bytes will return an int
  }
  
  
  
  
  /**
   * Throw runtime exception, since conditional expressions are not allowed, and this should have been caught
   * before the TypeChecker.
   */
  public TypeData forConditionalExpression(ConditionalExpression that) {
    throw new RuntimeException("Internal Program Error: Conditional expressions are not supported.  This should have been caught before the Type Checker.  Please report this bug.");
  }
  
  /** Try to look up the type of the instanceof, and visit the expression that is being tested.
    * If the type of the instanceof is null, add an error, and return null.
    * If what is being tested cannot be resolved, just return the type boolean to allow further type checking.
    * If everything is okay, call forInstanceofExpressionOnly to do other checks.
    * @param that  The InstanceofExpression being typeChecked
    * @return  The TypeData for boolean, or null
    */
  public TypeData forInstanceofExpression(InstanceofExpression that) {
    //this call to getSymbolData will not throw any errors, but may return null.  If null is returned, an error needs to be added.
    final SymbolData typeRes = getSymbolData(that.getType().getName(), _data.getSymbolData(), that.getType(), false);
    final TypeData valueRes = that.getValue().visit(this);
    
    if (typeRes == null) {
      _addError(that.getType().getName()
                  + " cannot appear as the type of a instanceof expression because it is not a valid type", 
                that.getType());
      return null;
    }
    
    if (! assertFound(valueRes, that.getValue())) {
      // An error occurred type-checking the value; return the expected type to
      // allow type-checking to continue.
      return SymbolData.BOOLEAN_TYPE.getInstanceData();
    }
    
    // Neither typeRes nor valueRes are null.
    return forInstanceofExpressionOnly(that, typeRes, valueRes);
  }
  
  
  
  /** Try to look up the type of the cast, and visit the expression that is being cast.
    * If the type being cast to is null, add an error, and return null.
    * If what is being cast cannot be resolved, just return the expected result of the cast, to allow type checking.
    * If everything is okay, call forCastExpressionOnly to do other checks.
    * @param that  The CastExpression being typeChecked
    * @return  The TypeData result of the cast, or null
    */
  public TypeData forCastExpression(CastExpression that) {
    //this call to getSymbolData will not throw any errors, but may return null.  If null is returned, an error needs to be added.
    final SymbolData typeRes = getSymbolData(that.getType().getName(), _data.getSymbolData(), that.getType(), false);
    final TypeData valueRes = that.getValue().visit(this);
    
    if (typeRes == null) {
      _addError(that.getType().getName() + " cannot appear as the type of a cast expression because it is not a valid type", that.getType());
      return null;
    }
    
    if (valueRes == null || !assertFound(valueRes, that.getValue())) {
      // An error occurred type-checking the value; return the expected type to
      // allow type-checking to continue.
      return typeRes.getInstanceData();
    }
    
    // Neither typeRes nor valueRes are null.
    return forCastExpressionOnly(that, typeRes, valueRes);
  }
  
  
  /**
   * Make sure the dimensions of the array instantiation are all instances and subtypes of int, and then return
   * an instance of the array.
   * @param that  The UninitializedArrayInstantiation being type checked
   * @param typeRes  The type of the array
   * @param dimensions_result  The array of the result of type-checking all the dimensions of this array.
   * @return an instance of the array.
   */
  public TypeData forUninitializedArrayInstantiationOnly(UninitializedArrayInstantiation that, TypeData typeRes, 
                                                         TypeData[] dimensions_result) {
    //make sure all of the dimensions_result dimensions are instance datas
    Expression[] dims = that.getDimensionSizes().getExpressions();
    for (int i = 0; i<dimensions_result.length; i++) {
      if (dimensions_result[i] != null && assertFound(dimensions_result[i], dims[i])) {
        if (!dimensions_result[i].getSymbolData().isAssignableTo(SymbolData.INT_TYPE, 
                                                                 JAVA_VERSION)) {
          _addError("The dimensions of an array instantiation must all be ints.  You have specified something of type " +
                    dimensions_result[i].getName(), dims[i]);
        }
        else {
          assertInstanceType(dimensions_result[i], "All dimensions of an array instantiation must be instances." + 
                             "  You have specified the type " + dimensions_result[i].getName(), dims[i]);
        }               
      }
    }
    
    if (typeRes instanceof ArrayData) {
      int dim = ((ArrayData) typeRes).getDimensions();
      if (dimensions_result.length > dim) {
        //uh oh!  Dimensions list is too long!
        _addError("You are trying to initialize an array of type " + typeRes.getName() + " which requires " + dim +
                  " dimensions, but you have specified " + dimensions_result.length + " dimensions--the wrong number", 
                  that);
      }
    }
    
    //return an instance of the new type
    if (typeRes == null || !assertFound(typeRes, that)) {return null;}
    return typeRes.getInstanceData();
  }
  
  /**
   * Resolve the type of the array and visit its dimensions.  Call Only method to check instances in dimensions.
   * @param that  The SimpleUninitializedArrayInstantiation being type-checked.
   * @return  The type of the array, or null if there was an error.
   */
  public TypeData forSimpleUninitializedArrayInstantiation(SimpleUninitializedArrayInstantiation that) {
    final SymbolData typeRes = getSymbolData(that.getType().getName(), _data.getSymbolData(), that.getType());
    final TypeData[] dimensions_result = makeArrayOfRetType(that.getDimensionSizes().getExpressions().length);
    
    for (int i = 0; i<that.getDimensionSizes().getExpressions().length; i++) {
      dimensions_result[i] = that.getDimensionSizes().getExpressions()[i].visit(this);
    }
    return forUninitializedArrayInstantiationOnly(that, typeRes, dimensions_result);
  }
  
  
  /**
   * This is not legal java--should have been caught before the TypeChecker.  Give a runtime exception
   */
  public TypeData forComplexUninitializedArrayInstantiation(ComplexUninitializedArrayInstantiation that) {
    throw new RuntimeException("Internal Program Error: Complex Uninitialized Array Instantiations are not legal Java." +
                               "  This should have been caught before the Type Checker.  Please report this bug.");
  }
  
  
  /**
   * The array initializer needs the type of the array to ensure it is properly handled.  Because of this, we use a
   * helper instead of calling this method directly.
   */
  public TypeData forArrayInitializer(ArrayInitializer that) {
    throw new RuntimeException("Internal Program Error: forArrayInitializer should never be called, but it was." + 
                               "  Please report this bug.");
  }
  
  /**
   * Lookup the type of the array instantiation, and if there are any errors with it, give them.
   * Then, check the array initializer.
   * @param that  The SimpleInitializedArrayAllocationInstantiation that is being type-checked
   * @return  An instance of the array
   */
  public TypeData forSimpleInitializedArrayInstantiation(SimpleInitializedArrayInstantiation that) {
    SymbolData typeRes = getSymbolData(that.getType().getName(), _data, that.getType());
    TypeData elementResult = forArrayInitializerHelper(that.getInitializer(), typeRes);
    if (typeRes == null) {return null;}
    return typeRes.getInstanceData();
  }
  
  /**
   * This is not legal java--should have been caught before the TypeChecker.  Give a runtime exception
   */
  public TypeData forComplexInitializedArrayInstantiation(ComplexInitializedArrayInstantiation that) {
    throw new RuntimeException("Internal Program Error: Complex Initialized Array Instantiations are not legal Java." + 
                               "  This should have been caught before the Type Checker.  Please report this bug.");
  }
  
  
  // Moved from TypeChecker
  public TypeData forInnerClassDef(InnerClassDef that) {
    String className = that.getName().getText();
    SymbolData sd = _data.getInnerClassOrInterface(className); // className is always a qualified name
    // Check for cyclic inheritance
    if (checkForCyclicInheritance(sd, new LinkedList<SymbolData>(), that)) {
      return null;
    }
    final TypeData mavRes = that.getMav().visit(this);
    final TypeData nameRes = that.getName().visit(this);
    final TypeData[] typeParamRes = makeArrayOfRetType(that.getTypeParameters().length);
    for (int i = 0; i < that.getTypeParameters().length; i++) {
      typeParamRes[i] = that.getTypeParameters()[i].visit(this);
    }
    final TypeData superClass = that.getSuperclass().visit(this);
    final TypeData[] interfacesRes = makeArrayOfRetType(that.getInterfaces().length);
    for (int i = 0; i < that.getInterfaces().length; i++) {
      interfacesRes[i] = that.getInterfaces()[i].visit(this);
    }
    final TypeData bodyRes = that.getBody().visit(new ClassBodyTypeChecker(sd, _file, _package, _importedFiles, 
                                                                               _importedPackages, _vars, _thrown));
//    return forInnerClassDefOnly(that, mavRes, nameRes, typeParamRes, superClass, 
//                                typeParamRes, bodyRes);
    return null;
  }
  
  /** Compares the two lists of variable datas, and if a data is in both lists, mark it as having been assigned.
    * @param l1  One of the lists of variable datas
    * @param l2  The other list of variable datas.
    */
  void reassignVariableDatas(LinkedList<VariableData> l1, LinkedList<VariableData> l2) {
    for (int i = 0; i<l1.size(); i++) { 
      if (l2.contains(l1.get(i))) l1.get(i).gotValue();
    }
  }
  
  /** Compare a list of variable datas and a list of list of variable datas.  If a variable data is in the list and in each list of the lists of lists, mark it as having been
    * assigned.
    * @param tryBlock  The list of variable datas.
    * @param catchBlocks  The list of list of variable datas.
    */
  void reassignLotsaVariableDatas(LinkedList<VariableData> tryBlock, LinkedList<LinkedList<VariableData>> catchBlocks) {
    for (int i = 0; i<tryBlock.size(); i++) {
      boolean seenIt = true;
      for (int j = 0; j<catchBlocks.size(); i++) {
        if (! catchBlocks.get(j).contains(tryBlock.get(i))) {seenIt = false;}
      }
      
      if (seenIt) {        //find the variable data in vars and give it a value!
        tryBlock.get(i).gotValue();
      }
    }
  }
  
  /**
   * Throw the appropriate error, based on the type of the JExpression where the exception was unchecked
   * @param sd  The SymbolData corresponding to the exception that is thrown
   * @param j  The JExpression corresponding to the context of where the exception is thrown from.
   */
  public void handleUncheckedException(SymbolData sd, JExpression j) {
    if (j instanceof MethodInvocation) {
      _addError("The method " + ((MethodInvocation)j).getName().getText() + " is declared to throw the exception " + sd.getName() + " which needs to be caught or declared to be thrown", j);
    }
    else if (j instanceof ThrowStatement) {
      _addError("This statement throws the exception " + sd.getName() + " which needs to be caught or declared to be thrown", j);
    }
    else if (j instanceof ClassInstantiation) {
      _addError("The constructor for the class " + ((ClassInstantiation)j).getType().getName() + " is declared to throw the exception " + sd.getName() + " which needs to be caught or declared to be thrown.", j);
    }
    else {
      throw new RuntimeException("Internal Program Error: Something besides a method invocation or throw statement threw an exception.  Please report this bug.");
    }
  }
  
  
  /**
   * Returns whether the sd is a checked exception, i.e. one that needs to be caught or declared to be thrown.
   * This is defined as all subclasses of java.lang.Throwable except for subclasses of java.lang.RuntimeException
   */
  public boolean isCheckedException(SymbolData sd, JExpression that) {
    return sd.isSubClassOf(getSymbolData("java.lang.Throwable", _data, that, false)) &&
      ! sd.isSubClassOf(getSymbolData("java.lang.RuntimeException", _data, that, false)) &&
      ! sd.isSubClassOf(getSymbolData("java.lang.Error", _data, that, false));
  }
  
  /**
   * Return true if the Exception is a checked exception yet is not caught or declared to be thrown, and false otherwise.
   * An exception is a checked if it does not extend either java.lang.RuntimeException or java.lang.Error,
   * and is not declared to be thrown by the enclosing method.
   * @param sd  The SymbolData of the Exception we are checking.
   * @param that  The JExpression passed to getSymbolData for error purposes.
   */
  public boolean isUncaughtCheckedException(SymbolData sd, JExpression that) {
    return isCheckedException(sd, that);
  }
  
  //TODO: To optimize this, should 2nd for loop be moved outside of first for loop?
  public TypeData forBracedBody(BracedBody that) {
    final TypeData[] items_result = makeArrayOfRetType(that.getStatements().length);
    for (int i = 0; i < that.getStatements().length; i++) {
      items_result[i] = that.getStatements()[i].visit(this);
      //walk over what has been thrown and throw an error if it contains an unchecked exception
      for (int j = 0; j<this._thrown.size(); j++) {
        if (isUncaughtCheckedException(this._thrown.get(j).getFirst(), that)) {
          handleUncheckedException(this._thrown.get(j).getFirst(), this._thrown.get(j).getSecond());
        }
      }
    }
    
    return forBracedBodyOnly(that, items_result);
  }
  
  /** @return true type by default. */
  public TypeData forEmptyForCondition(EmptyForCondition that) {
    return SymbolData.BOOLEAN_TYPE.getInstanceData();
  }
  
  /** Test class for the methods defined in the above (enclosing) class. */
  public static class ExpressionTypeCheckerTest extends TestCase {
    
    private ExpressionTypeChecker _etc;
    
    private SymbolData _sd1;
    private SymbolData _sd2;
    private SymbolData _sd3;
    private SymbolData _sd4;
    private SymbolData _sd5;
    private SymbolData _sd6;
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[0]);
    private ModifiersAndVisibility _abstractMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"final"});
    private ModifiersAndVisibility _finalPublicMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"final", "public"});
    private ModifiersAndVisibility _publicAbstractMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public", "abstract"});
    private ModifiersAndVisibility _publicStaticMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public", "static"});
    
    
    public ExpressionTypeCheckerTest() { this(""); }
    public ExpressionTypeCheckerTest(String name) { super(name); }
    
    public void setUp() {
      errors = new LinkedList<Pair<String, JExpressionIF>>();
      LanguageLevelConverter.symbolTable.clear();
      LanguageLevelConverter._newSDs.clear();
      LanguageLevelConverter.loadSymbolTable();
      _etc = 
        new ExpressionTypeChecker(null, new File(""), "", new LinkedList<String>(), new LinkedList<String>(), 
                                  new LinkedList<VariableData>(), new LinkedList<Pair<SymbolData, JExpression>>());
      LanguageLevelConverter.OPT = new Options(JavaVersion.JAVA_5, EmptyIterable.<File>make());
      _etc._importedPackages.addFirst("java.lang");
      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _sd3 = new SymbolData("zebra");
      _sd4 = new SymbolData("u.like.emu");
      _sd5 = new SymbolData("");
      _sd6 = new SymbolData("cebu");
      _etc._data = _sd1;
    }
    
    public void testForCastExpression() {
      CastExpression ce = new CastExpression(SourceInfo.NONE, new PrimitiveType(SourceInfo.NONE, "dan"), NULL_LITERAL);
      
      // if cast type is not a valid type, casting should not be allowed
      assertEquals("Should return null", null, ce.visit(_etc));
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "dan cannot appear as the type of a cast expression because it is not a valid type", 
                   errors.getLast().getFirst());
      
      //if cast expression cannot be resolved, return cast type instance to allow type checking to continue
      CastExpression ce2 = 
        new CastExpression(SourceInfo.NONE,
                           new PrimitiveType(SourceInfo.NONE, "int"),
                           new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "notReal")));
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), ce2.visit(_etc));
      assertEquals("There should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", "Could not resolve symbol notReal", errors.getLast().getFirst());
      
      //now, try one that should work
      CastExpression ce3 = new CastExpression(SourceInfo.NONE,
                                              new PrimitiveType(SourceInfo.NONE, "int"),
                                              new DoubleLiteral(SourceInfo.NONE, 5));
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), ce3.visit(_etc));
      assertEquals("There should still be 2 errors", 2, errors.size());
      
      
    }
    
    public void testForCastExpressionOnly() {
      SymbolData sd1 = SymbolData.DOUBLE_TYPE;
      SymbolData sd2 = SymbolData.BOOLEAN_TYPE;
      SymbolData sd3 = SymbolData.INT_TYPE;
      
      CastExpression cd = new CastExpression(SourceInfo.NONE, JExprParser.NO_TYPE, NULL_LITERAL);
      assertEquals("When valueRes is subtype of typeRes, return typeRes.", sd1.getInstanceData(), 
                   _etc.forCastExpressionOnly(cd, sd1, sd3.getInstanceData()));
      assertEquals("Should not throw an error.", 0, errors.size());
      assertEquals("When typeRes is subtype of valueRes, return typeRes.", sd3.getInstanceData(), 
                   _etc.forCastExpressionOnly(cd, sd3, sd1.getInstanceData()));
      assertEquals("Should not throw an error.", 0, errors.size());
      assertEquals("When typeRes and valueRes are not subtypes of each other, return typeRes", 
                   sd2.getInstanceData(), _etc.forCastExpressionOnly(cd, sd2, sd1.getInstanceData()));
      assertEquals("Should now be one error.", 1, errors.size());
      assertEquals("Error message should be correct.", "You cannot cast an expression of type " + sd1.getName() 
                     + " to type " + sd2.getName() + " because they are not related", 
                   errors.getLast().getFirst());     
      SymbolData foo = new SymbolData("Foo");
      SymbolData fooMama = new SymbolData("FooMama");
      foo.setSuperClass(fooMama);
      assertEquals("When valueRes is a SymbolData, return typeRes", fooMama.getInstanceData(), 
                   _etc.forCastExpressionOnly(cd, fooMama, foo));
      assertEquals("There should be 2 errors.", 2, errors.size());
      assertEquals("Error message should be correct.", 
                   "You are trying to cast Foo, which is a class or interface type, not an instance.  " 
                     + "Perhaps you meant to create a new instance of Foo",
                   errors.getLast().getFirst());
    }
    
    public void testForEmptyExpressionOnly() {
      EmptyExpression ee = new EmptyExpression(SourceInfo.NONE);
      try {
        _etc.forEmptyExpressionOnly(ee);
        fail("Should have thrown exception");
      }
      catch (RuntimeException e) {
        assertEquals("Error message should be correct", 
                     "Internal Program Error: EmptyExpression encountered.  Student is missing something.  "
                       + "Should have been caught before TypeChecker.  Please report this bug.", 
                     e.getMessage());
      }
    }
    
    public void test_getLeastRestrictiveType() {
      // Assumes both number types
      assertEquals("Should return double.", SymbolData.FLOAT_TYPE, 
                   _etc._getLeastRestrictiveType(SymbolData.INT_TYPE, SymbolData.FLOAT_TYPE));
      assertEquals("Should return double.", SymbolData.FLOAT_TYPE, 
                   _etc._getLeastRestrictiveType(SymbolData.FLOAT_TYPE, SymbolData.FLOAT_TYPE));
      assertEquals("Should return int.", SymbolData.INT_TYPE, 
                   _etc._getLeastRestrictiveType(SymbolData.INT_TYPE, SymbolData.CHAR_TYPE));
      assertEquals("Should return char.", SymbolData.INT_TYPE, 
                   _etc._getLeastRestrictiveType(SymbolData.CHAR_TYPE, SymbolData.CHAR_TYPE));
    }
    
    public void test_isAssignableFrom() {
      assertTrue("Should be assignable.", _etc._isAssignableFrom(SymbolData.DOUBLE_TYPE, SymbolData.DOUBLE_TYPE));
      assertTrue("Should be assignable.", _etc._isAssignableFrom(SymbolData.DOUBLE_TYPE, SymbolData.INT_TYPE));
      assertTrue("Should be assignable.", _etc._isAssignableFrom(SymbolData.DOUBLE_TYPE, SymbolData.CHAR_TYPE));
      assertTrue("Should be assignable.", _etc._isAssignableFrom(SymbolData.INT_TYPE, SymbolData.INT_TYPE));
      assertTrue("Should be assignable.", _etc._isAssignableFrom(SymbolData.INT_TYPE, SymbolData.CHAR_TYPE));
      assertTrue("Should be assignable.", _etc._isAssignableFrom(SymbolData.CHAR_TYPE, SymbolData.CHAR_TYPE));
      
      _sd2.setSuperClass(_sd1);
      assertTrue("Should be assignable.", _etc._isAssignableFrom(_sd1, _sd1));
      assertTrue("Should be assignable.", _etc._isAssignableFrom(_sd1, _sd2));
    }
    
    
    //for expressions we want to check, but don't fit neatly into a category
    public void testRandomExpressions() {
      //a string of + and - before a number
      PositiveExpression pe = new PositiveExpression(SourceInfo.NONE, new IntegerLiteral(SourceInfo.NONE, 5));
      PositiveExpression pe2 = new PositiveExpression(SourceInfo.NONE, pe);
      NegativeExpression pe3 = new NegativeExpression(SourceInfo.NONE, pe2);
      PositiveExpression pe4 = new PositiveExpression(SourceInfo.NONE, pe3);
      PositiveExpression pe5 = new PositiveExpression(SourceInfo.NONE, pe4);
      NegativeExpression pe6 = new NegativeExpression(SourceInfo.NONE, pe5);
      
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), pe6.visit(_etc));
      assertEquals("Should be no errors", 0, errors.size());
    }
    
    public void testForSimpleUninitializedArrayInstantiation() {
      LanguageLevelVisitor llv = 
        new LanguageLevelVisitor(_etc._file, 
                                 _etc._package,
                                 null, // enclosingClassName for top level traversal
                                 _etc._importedFiles, 
                                 _etc._importedPackages, 
                                 new HashSet<String>(), 
                                 new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                                 new LinkedList<Command>());
//      LanguageLevelConverter.symbolTable = llv.symbolTable = _etc.symbolTable;
//      LanguageLevelConverter._newSDs = new Hashtable<SymbolData, LanguageLevelVisitor>();
      
      SourceInfo si = SourceInfo.NONE;
      
      ArrayData intArray = new ArrayData(SymbolData.INT_TYPE, llv, si);
      intArray.setIsContinuation(false);
      symbolTable.remove("int[]");
      symbolTable.put("int[]", intArray);
      
      ArrayData intArrayArray = new ArrayData(intArray, llv, si);
      intArrayArray.setIsContinuation(false);
      symbolTable.put("int[][]", intArrayArray);
      
      ArrayData intArray3 = new ArrayData(intArrayArray, llv, si);
      intArray3.setIsContinuation(false);
      symbolTable.put("int[][][]", intArray3);
      
      Expression i1 = new IntegerLiteral(si, 5);
      Expression i2 = new PlusExpression(si, new IntegerLiteral(si, 5), new IntegerLiteral(si, 7));
      Expression i3 = new CharLiteral(si, 'c');
      Expression badIndexD = new DoubleLiteral(si, 4.2);
      Expression badIndexL = new LongLiteral(si, 4l);
      
      // Test one that works
      SimpleUninitializedArrayInstantiation sa1 = 
        new SimpleUninitializedArrayInstantiation(si, new ArrayType(si, "int[][][]", 
                                                                    new ArrayType(si, "int[][]", 
                                                                                  new ArrayType(si, "int[]", new PrimitiveType(si, "int")))), 
                                                  new DimensionExpressionList(si, new Expression[] {i1, i2, i3}));
      assertEquals("Should return instance of int[][][]", intArray3.getInstanceData(), sa1.visit(_etc));
      assertEquals("There should be no errors", 0, errors.size());
      
      // Test one with a bad index
      SimpleUninitializedArrayInstantiation sa2 = 
        new SimpleUninitializedArrayInstantiation(si, new ArrayType(si, "int[][][]", 
                                                                    new ArrayType(si, "int[][]", new ArrayType(si, "int[]", new PrimitiveType(si, "int")))), 
                                                  new DimensionExpressionList(si, new Expression[] {i1, i2, badIndexD}));
      assertEquals("Should return instance of int[][][]", intArray3.getInstanceData(), sa2.visit(_etc));
      /* The preceding test only confirms structural equality not identity  of the result.  The TypeData equals method
       * has been overridden to confirm that its argument belongs to the same class as this and then perform an equals 
       * comparison of the only field of the argument and this. */
      
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "The dimensions of an array instantiation must all be ints.  You have specified something of type double", 
                   errors.getLast().getFirst());
      
      //Test one with a bad type
      SimpleUninitializedArrayInstantiation sa3 = 
        new SimpleUninitializedArrayInstantiation(si, new ArrayType(si, "Jonathan[]", 
                                                                    new ClassOrInterfaceType(si, "Jonathan", new Type[0])), 
                                                  new DimensionExpressionList(si, new Expression[]{i1}));
      assertEquals("Should return null", null, sa3.visit(_etc));
      assertEquals("There should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "Class or variable Jonathan[] not found.", 
                   errors.getLast().getFirst());
      // Test one with wrong dimensions--too many
      SimpleUninitializedArrayInstantiation sa4 = 
        new SimpleUninitializedArrayInstantiation(si, new ArrayType(si, "int[][]", 
                                                                    new ArrayType(si, "int[]", new PrimitiveType(si, "int"))), 
                                                  new DimensionExpressionList(si, new Expression[] {i1, i2, i3}));
      assertEquals("Should return instance of int[][]", intArrayArray.getInstanceData(), sa4.visit(_etc));
      assertEquals("There should be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", 
                   "You are trying to initialize an array of type int[][] which requires 2 dimensions, but you have "
                     + "specified 3 dimensions--the wrong number", 
                   errors.getLast().getFirst());
      // Test one with wrong dimensions--too few--should be no additional errors
      SimpleUninitializedArrayInstantiation sa5 = 
        new SimpleUninitializedArrayInstantiation(si, new ArrayType(si, "int[][][]", 
                                                                    new ArrayType(si, "int[][]", new ArrayType(si, "int[]", 
                                                                                                               new PrimitiveType(si, "int")))), 
                                                  new DimensionExpressionList(si, new Expression[] {i1, i2}));
      assertEquals("Should return instance of int[][][]", intArray3.getInstanceData(), sa5.visit(_etc));
      assertEquals("There should still be 3 errors", 3, errors.size());
      
      //Test one where type is not accessible
      intArray3.setMav(_privateMav);
      assertEquals("Should return instance of int[][][]", intArray3.getInstanceData(), sa1.visit(_etc));
      assertEquals("There should be one new error", 4, errors.size());
      assertEquals("Error message should be correct", 
                   "The class or interface int[][][] in int[][][] is private and cannot be accessed from i.like.monkey", 
                   errors.getLast().getFirst());
      intArray3.setMav(_publicMav);
    }

    public void testForComplexUninitializedArrayInstantiation() {
      ComplexUninitializedArrayInstantiation ca1 = new ComplexUninitializedArrayInstantiation(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "my")),
                                                                                              new ArrayType(SourceInfo.NONE, "type[][][]", new ArrayType(SourceInfo.NONE, "type[][]", new ArrayType(SourceInfo.NONE, "type[]", new ClassOrInterfaceType(SourceInfo.NONE, "type", new Type[0])))), 
                                                                                              new DimensionExpressionList(SourceInfo.NONE, new Expression[0]));
      //This should always give a runtime exception
      try {
        ca1.visit(_etc);
        fail("Should have throw runtime exception");
      }
      catch (RuntimeException e) {
        assertEquals("Correct exception should have been thrown","Internal Program Error: Complex Uninitialized Array Instantiations are not legal Java.  This should have been caught before the Type Checker.  Please report this bug." , e.getMessage());
      }
    }    
    
    public void testForUninitializedArrayInstantiationOnly() {
      LanguageLevelVisitor llv = 
        new LanguageLevelVisitor(_etc._file, 
                                 _etc._package,
                                 null, // enclosingClassName for top level traversal
                                 _etc._importedFiles, 
                                 _etc._importedPackages,  
                                 new HashSet<String>(), 
                                 new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                                 new LinkedList<Command>());
      
//      LanguageLevelConverter.symbolTable = llv.symbolTable = _etc.symbolTable;
//      LanguageLevelConverter._newSDs = new Hashtable<SymbolData, LanguageLevelVisitor>();
      
      SourceInfo si = SourceInfo.NONE;
      
      ArrayData intArray = new ArrayData(SymbolData.INT_TYPE, llv, si);
      intArray.setIsContinuation(false);
      symbolTable.remove("int[]");
      symbolTable.put("int[]", intArray);
      
      ArrayData intArrayArray = new ArrayData(intArray, llv, si);
      intArrayArray.setIsContinuation(false);
      symbolTable.put("int[][]", intArrayArray);
      
      ArrayData intArray3 = new ArrayData(intArrayArray, llv, si);
      intArray3.setIsContinuation(false);
      symbolTable.put("int[][][]", intArray3);
      
      // One that works--int instance index
      SimpleUninitializedArrayInstantiation sa1 = 
        new SimpleUninitializedArrayInstantiation(si, new ArrayType(si, "int[][][]", 
                                                                    new ArrayType(si, "int[][]", new ArrayType(si, "int[]", new PrimitiveType(si, "int")))), 
                                                  new DimensionExpressionList(si, new Expression[] {new NullLiteral(si), new NullLiteral(si), new NullLiteral(si)}));
      
      TypeData[] arrayInitTypes1 =
        new TypeData[] { SymbolData.INT_TYPE.getInstanceData(), 
                         SymbolData.INT_TYPE.getInstanceData(), 
                         SymbolData.INT_TYPE.getInstanceData()};
      assertEquals("Should return int[][][] instance", intArray3.getInstanceData(), 
                   _etc.forUninitializedArrayInstantiationOnly(sa1, intArray3, arrayInitTypes1));
      assertEquals("Should be no errors", 0, errors.size());
      
      //one that works--char instance index
      TypeData[] arrayInitTypes2 =
        new TypeData[] { SymbolData.INT_TYPE.getInstanceData(), 
                         SymbolData.INT_TYPE.getInstanceData(), 
                         SymbolData.CHAR_TYPE.getInstanceData()};
      assertEquals("Should return int[][][] instance", intArray3.getInstanceData(), 
                   _etc.forUninitializedArrayInstantiationOnly(sa1, intArray3, arrayInitTypes2));
      assertEquals("Should be no errors", 0, errors.size());
      
      // one with bad index: not instance type
      TypeData[] arrayInitTypes3 =
        new TypeData[] { SymbolData.INT_TYPE.getInstanceData(), 
                         SymbolData.INT_TYPE, 
                         SymbolData.CHAR_TYPE.getInstanceData()};
      assertEquals("Should return int[][][] instance", intArray3.getInstanceData(), 
                   _etc.forUninitializedArrayInstantiationOnly(sa1, intArray3, arrayInitTypes3));
      assertEquals("Should be one error", 1, errors.size());
      assertEquals("Error message should be correct", "All dimensions of an array instantiation must be instances.  You have specified the type int.  Perhaps you meant to create a new instance of int", errors.getLast().getFirst());
      
      // one with bad index: not int type
      assertEquals("Should return int[][][] instance", intArray3.getInstanceData(), _etc.forUninitializedArrayInstantiationOnly(sa1, intArray3, new TypeData[] {SymbolData.INT_TYPE.getInstanceData(), SymbolData.BOOLEAN_TYPE, SymbolData.CHAR_TYPE.getInstanceData()}));
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", "The dimensions of an array instantiation must all be ints.  You have specified something of type boolean" , errors.getLast().getFirst());
      
      
    }
    
    public void testForArrayInitializer() {
      ArrayInitializer ai = new ArrayInitializer(SourceInfo.NONE, new VariableInitializerI[] {new IntegerLiteral(SourceInfo.NONE, 2)});
      try {
        ai.visit(_etc);
        fail("Should have throw runtime exception");
      }
      catch(RuntimeException e) {
        assertEquals("Exception message should be correct", "Internal Program Error: forArrayInitializer should never be called, but it was.  Please report this bug.", e.getMessage());
      }
      
    }
    
    public void testForSimpleInitializedArrayInstantiation() {
      IntegerLiteral e1 = new IntegerLiteral(SourceInfo.NONE, 5);
      IntegerLiteral e2 = new IntegerLiteral(SourceInfo.NONE, 7);
      SimpleNameReference e3 = new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "int"));
      BooleanLiteral e4 = new BooleanLiteral(SourceInfo.NONE, true);
      DoubleLiteral e5 = new DoubleLiteral(SourceInfo.NONE, 4.2);
      CharLiteral e6 = new CharLiteral(SourceInfo.NONE, 'e');
      SimpleNameReference e7 = new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "int"));
      
      ArrayType intArrayType = new ArrayType(SourceInfo.NONE, "int[]", new PrimitiveType(SourceInfo.NONE, "int"));
      
      LanguageLevelVisitor llv = 
        new LanguageLevelVisitor(_etc._file, 
                                 _etc._package, 
                                 null, // enclosingClassName for top level traversal
                                 _etc._importedFiles, 
                                 _etc._importedPackages, 
                                 new HashSet<String>(), 
                                 new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                                 new LinkedList<Command>());
      
      ArrayData intArray = new ArrayData(SymbolData.INT_TYPE, llv, SourceInfo.NONE);
      intArray.setIsContinuation(false);
      symbolTable.remove("int[]");
      symbolTable.put("int[]", intArray);
      
      //try one that should work:
      InitializedArrayInstantiation good = new SimpleInitializedArrayInstantiation(SourceInfo.NONE, intArrayType, new ArrayInitializer(SourceInfo.NONE, new VariableInitializerI[] {e1, e2}));
      assertEquals("Should return int array instance", intArray.getInstanceData(), good.visit(_etc));
      assertEquals("Should be no errors", 0, errors.size());
      
      //char is a subtype of int, so it can be used here
      good = new SimpleInitializedArrayInstantiation(SourceInfo.NONE, intArrayType, new ArrayInitializer(SourceInfo.NONE, new VariableInitializerI[] {e1, e2, e6}));
      assertEquals("Should return int array instance", intArray.getInstanceData(), good.visit(_etc));
      assertEquals("Should be no errors", 0, errors.size());
      
      //lhs is not an array type
      InitializedArrayInstantiation bad = new SimpleInitializedArrayInstantiation(SourceInfo.NONE, new PrimitiveType(SourceInfo.NONE, "int"), new ArrayInitializer(SourceInfo.NONE, new VariableInitializerI[] {e1, e2}));
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), bad.visit(_etc));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "You cannot initialize the non-array type int with an array initializer", errors.getLast().getFirst());
      
      //one of the elements is the wrong type
      //boolean
      bad = new SimpleInitializedArrayInstantiation(SourceInfo.NONE, intArrayType, new ArrayInitializer(SourceInfo.NONE, new VariableInitializerI[] {e1, e4, e2, e6}));
      assertEquals("Should return int array instance", intArray.getInstanceData(), bad.visit(_etc));
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", "The elements of this initializer should have type int but element 1 has type boolean", errors.getLast().getFirst());
      
      //double
      bad = new SimpleInitializedArrayInstantiation(SourceInfo.NONE, intArrayType, new ArrayInitializer(SourceInfo.NONE, new VariableInitializerI[] {e1, e5, e2, e6}));
      assertEquals("Should return int array instance", intArray.getInstanceData(), bad.visit(_etc));
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", "The elements of this initializer should have type int but element 1 has type double", errors.getLast().getFirst());
      
      //cannot resolve lhs
      bad = new SimpleInitializedArrayInstantiation(SourceInfo.NONE, new PrimitiveType(SourceInfo.NONE, "ej"), new ArrayInitializer(SourceInfo.NONE, new VariableInitializerI[] {e1, e2}));
      assertEquals("Should return null", null, bad.visit(_etc));
      assertEquals("Should be 4 error", 4, errors.size());
      assertEquals("Error message should be correct", "Class or variable ej not found.", errors.getLast().getFirst());
      
      //one of the things in the initializer is a type name!
      bad = new SimpleInitializedArrayInstantiation(SourceInfo.NONE, intArrayType, new ArrayInitializer(SourceInfo.NONE, new VariableInitializerI[] {e1, e7}));
      assertEquals("Should return instance of int[]", intArray.getInstanceData(), bad.visit(_etc));
      assertEquals("Should now be 5 error messages", 5, errors.size());
      assertEquals("Error message should be correct", "The elements of this initializer should all be instances, but you have specified the type name int.  Perhaps you meant to create a new instance of int", errors.getLast().getFirst());
      
      
    }
    
    
    
    
    public void testForSimpleAssignmentExpressionOnly() {
      SimpleAssignmentExpression sae = new SimpleAssignmentExpression(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "i")), new IntegerLiteral(SourceInfo.NONE, 5));
      
      //if lhs is assignable to rhs, and both instances, do not give any errors
      assertEquals("Should return double instance", SymbolData.DOUBLE_TYPE.getInstanceData(), _etc.forSimpleAssignmentExpressionOnly(sae, SymbolData.DOUBLE_TYPE.getInstanceData(), SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      //if either input is null, return null
      assertEquals("Should return null", null, _etc.forSimpleAssignmentExpressionOnly(sae, null, SymbolData.INT_TYPE));
      assertEquals("Should return null", null, _etc.forSimpleAssignmentExpressionOnly(sae, SymbolData.INT_TYPE, null));
      assertEquals("Should be no errors", 0, errors.size());
      
      //if lhs is a PackageData, give error and return null
      PackageData pd = new PackageData("bad_reference");
      assertEquals("Should return null", null, _etc.forSimpleAssignmentExpressionOnly(sae, pd, SymbolData.INT_TYPE));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "Could not resolve symbol bad_reference", errors.get(0).getFirst());
      
      
      //if rhs is a PackageData, give an error and return null
      assertEquals("Should return null", null, _etc.forSimpleAssignmentExpressionOnly(sae, SymbolData.INT_TYPE, pd));
      assertEquals("Should only be 1 error", 1, errors.size());  // Generated error is duplicate
      assertEquals("Error message should be correct", "Could not resolve symbol bad_reference", errors.get(0).getFirst());
      
      //if rhs or lhs are not instance datas, give appropriate errors
      assertEquals("Should return double instance", 
                   SymbolData.DOUBLE_TYPE.getInstanceData(), 
                   _etc.forSimpleAssignmentExpressionOnly(sae, 
                                                          SymbolData.DOUBLE_TYPE, 
                                                          SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("Should now be 2 errors", 2, errors.size());  // Generated one new error; one duplicate
      assertEquals("Error message should be correct", 
                   "You cannot assign a value to the type double.  Perhaps you meant to create a new instance of double", 
                   errors.get(1).getFirst());
      
      assertEquals("Should return double instance", 
                   SymbolData.DOUBLE_TYPE.getInstanceData(), 
                   _etc.forSimpleAssignmentExpressionOnly(sae, 
                                                          SymbolData.DOUBLE_TYPE.getInstanceData(), 
                                                          SymbolData.INT_TYPE));
      assertEquals("Should now be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot use the type name int on the right hand side of an assignment.  " +
                   "Perhaps you meant to create a new instance of int", 
                   errors.get(2).getFirst());
      
      //if rhs cannot be assigned to lhs, give error
      assertEquals("Should return int instance", 
                   SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forSimpleAssignmentExpressionOnly(sae, 
                                                          SymbolData.INT_TYPE.getInstanceData(), 
                                                          SymbolData.DOUBLE_TYPE.getInstanceData()));
      assertEquals("Should now be 4 errors", 4, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot assign something of type double to something of type int", 
                   errors.get(3).getFirst());
      
    }
    
    
    public void testForPlusAssignmentExpressionOnly() {
      PlusAssignmentExpression pae = 
        new PlusAssignmentExpression(SourceInfo.NONE, 
                                     new IntegerLiteral(SourceInfo.NONE, 5), new IntegerLiteral(SourceInfo.NONE, 6));
      
      //if lhs is a string, and lhs and rhs both instances, no errors
      SymbolData string = new SymbolData("java.lang.String");
      string.setIsContinuation(false);
      string.setPackage("java.lang");
      string.setMav(_publicMav);
      symbolTable.put("java.lang.String", string);
      
      assertEquals("Should return string instance", 
                   string.getInstanceData(), 
                   _etc.forPlusAssignmentExpressionOnly(pae, string.getInstanceData(), 
                                                        SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      //if both number instances, no errors
      assertEquals("Should return double instance", 
                   SymbolData.DOUBLE_TYPE.getInstanceData(), 
                   _etc.forPlusAssignmentExpressionOnly(pae, 
                                                        SymbolData.DOUBLE_TYPE.getInstanceData(), 
                                                        SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      //if either input is null, return null
      assertEquals("Should return null", null, _etc.forPlusAssignmentExpressionOnly(pae, null, SymbolData.INT_TYPE));
      assertEquals("Should return null", null, _etc.forPlusAssignmentExpressionOnly(pae, SymbolData.INT_TYPE, null));
      assertEquals("Should be no errors", 0, errors.size());
      
      //if lhs is a PackageData, give error and return null
      PackageData pd = new PackageData("bad_reference");
      assertEquals("Should return null", null, _etc.forPlusAssignmentExpressionOnly(pae, pd, SymbolData.INT_TYPE));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "Could not resolve symbol bad_reference", errors.getLast().getFirst());
      
      
      //if rhs is a PackageData, give an error and return null
      assertEquals("Should return null", null, _etc.forPlusAssignmentExpressionOnly(pae, SymbolData.INT_TYPE, pd));
      assertEquals("Should be 1 error", 1, errors.size());  // Generated duplicate error message
      assertEquals("Error message should be correct", "Could not resolve symbol bad_reference", errors.get(0).getFirst());
      
      //if lhs is a string, but not an instance data, give error
      assertEquals("Should return string instance", 
                   string.getInstanceData(), 
                   _etc.forPlusAssignmentExpressionOnly(pae, string, 
                                                        SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("Should now be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct",
                   "The arguments to a Plus Assignment Operator (+=) must both be instances, but you have specified " +
                   "a type name.  Perhaps you meant to create a new instance of java.lang.String", 
                   errors.get(1).getFirst());
      
      //if lhs is a string, but rhs is not an instance, give error
      assertEquals("Should return string instance", string.getInstanceData(), 
                   _etc.forPlusAssignmentExpressionOnly(pae, string.getInstanceData(), 
                                                        SymbolData.INT_TYPE));
      assertEquals("Should now be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct",
                   "The arguments to a Plus Assignment Operator (+=) must both be instances, " +
                   "but you have specified a type name.  Perhaps you meant to create a new instance of int" , 
                   errors.get(2).getFirst());
      
      // if rhs is not a number or string, give error
      assertEquals("Should return string, by default", string.getInstanceData(), 
                   _etc.forPlusAssignmentExpressionOnly(pae, _sd2.getInstanceData(), 
                                                        SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("Should now be 4 errors", 4, errors.size());
      assertEquals("Error message should be correct", 
                   "The arguments to the Plus Assignment Operator (+=) must either include an instance of a String " +
                   "or both be numbers.  You have specified arguments of type " + _sd2.getName() + " and int", 
                   errors.get(3).getFirst());
      
      // if rhs is number but lhs is not, give error
      assertEquals("should return string, by default", string.getInstanceData(),
                   _etc.forPlusAssignmentExpressionOnly(pae, SymbolData.INT_TYPE.getInstanceData(),
                                                        _sd2.getInstanceData()));
      assertEquals("Should now be 5 errors", 5, errors.size());  // Generated slightly different error message
      assertEquals("Error message should be correct", 
                   "The arguments to the Plus Assignment Operator (+=) must either include an instance of a String " +
                   "or both be numbers.  You have specified arguments of type int and " + _sd2.getName(), 
                   errors.get(4).getFirst());
      
      assertEquals("Should return int instance", 
                   SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forPlusAssignmentExpressionOnly(pae, SymbolData.INT_TYPE.getInstanceData(), 
                                                        SymbolData.DOUBLE_TYPE.getInstanceData()));
      assertEquals("Should now be 6 errors", 6, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot increment something of type int with something of type double", 
                   errors.get(5).getFirst());
      
      //if both numbers, but not instances, give errors
      assertEquals("Should return double instance", 
                   SymbolData.DOUBLE_TYPE.getInstanceData(), 
                   _etc.forPlusAssignmentExpressionOnly(pae, SymbolData.DOUBLE_TYPE, SymbolData.INT_TYPE));
      assertEquals("Should now be 8 errors", 8, errors.size());
      assertEquals("Second error message should be new", 
                   "The arguments to the Plus Assignment Operator (+=) must both be instances, but you have specified " +
                   "a type name.  Perhaps you meant to create a new instance of double", 
                   errors.get(6).getFirst());
      assertEquals("First error message should be new", 
                   "The arguments to the Plus Assignment Operator (+=) must both be instances, but you have specified " +
                   "a type name.  Perhaps you meant to create a new instance of int", 
                   errors.get(7).getFirst());
    }
    
    public void testForNumericAssignmentExpressionOnly() {
      NumericAssignmentExpression nae = 
        new MinusAssignmentExpression(SourceInfo.NONE, 
                                      new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "i")),
                                      new IntegerLiteral(SourceInfo.NONE, 5));
      
      //if both lhs and rhs are instances of numbers, and lhs is assignable to rhs, should be no errors
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), _etc.forNumericAssignmentExpressionOnly(nae, SymbolData.INT_TYPE.getInstanceData(), SymbolData.CHAR_TYPE.getInstanceData()));
      assertEquals("Should return double instance", SymbolData.DOUBLE_TYPE.getInstanceData(), _etc.forNumericAssignmentExpressionOnly(nae, SymbolData.DOUBLE_TYPE.getInstanceData(), SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      
      //if either input is null, return null
      assertEquals("Should return null", null, _etc.forNumericAssignmentExpressionOnly(nae, null, SymbolData.INT_TYPE));
      assertEquals("Should return null", null, _etc.forNumericAssignmentExpressionOnly(nae, SymbolData.INT_TYPE, null));
      assertEquals("Should be no errors", 0, errors.size());
      
      //if lhs is a PackageData, give error and return null
      PackageData pd = new PackageData("bad_reference");
      assertEquals("Should return null", null, _etc.forNumericAssignmentExpressionOnly(nae, pd, SymbolData.INT_TYPE));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "Could not resolve symbol bad_reference", errors.get(0).getFirst());
      
      //if rhs is a PackageData, give an error and return null
      assertEquals("Should return null", null, _etc.forNumericAssignmentExpressionOnly(nae, SymbolData.INT_TYPE, pd));
      assertEquals("Should still be 1 error", 1, errors.size());  // Generated duplicate error message
      assertEquals("Error message should be correct", "Could not resolve symbol bad_reference", errors.get(0).getFirst());
      
      //if lhs not an instance data, give error
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), _etc.forNumericAssignmentExpressionOnly(nae, SymbolData.INT_TYPE, SymbolData.CHAR_TYPE.getInstanceData()));
      assertEquals("Should be 2 errors", 2, errors.size());  // Generated a duplicate error message
      assertEquals("Error message should be correct", 
                   "You cannot use a numeric assignment (-=, %=, *=, /=) on the type int.  Perhaps you meant to create " +
                   "a new instance of int", 
                   errors.get(1).getFirst());
      // if rhs not instance data, give error
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), _etc.forNumericAssignmentExpressionOnly(nae, SymbolData.INT_TYPE.getInstanceData(), SymbolData.CHAR_TYPE));
      assertEquals("Should now be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot use the type name char on the left hand side of a numeric assignment (-=, %=, *=, /=)." +
                   "  Perhaps you meant to create a new instance of char", 
                   errors.get(2).getFirst());
      
      //if lhs not a number type, give error
      assertEquals("Should return sd2 instance", _sd2.getInstanceData(), 
                   _etc.forNumericAssignmentExpressionOnly(nae, _sd2.getInstanceData(), 
                                                           SymbolData.CHAR_TYPE.getInstanceData()));
      assertEquals("Should now be 4 errors", 4, errors.size());
      assertEquals("Error message should be correct", 
                   "The left side of this expression is not a number.  Therefore, you cannot apply " + 
                   "a numeric assignment (-=, %=, *=, /=) to it", 
                   errors.get(3).getFirst());
      
      //if rhs is not a number type, give error
      assertEquals("Should return int instance", 
                   SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forNumericAssignmentExpressionOnly(nae, 
                                                           SymbolData.INT_TYPE.getInstanceData(), 
                                                           _sd2.getInstanceData()));
      assertEquals("Should still be 5 errors", 5, errors.size());  // Generated a duplicate error message
      assertEquals("Error message should be correct", 
                   "The right side of this expression is not a number.  Therefore, you cannot apply " +
                   "a numeric assignment (-=, %=, *=, /=) to it", 
                   errors.get(4).getFirst());
      
      //if rhs is not assignable to lhs, give error
      assertEquals("Should return int instance", 
                   SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forNumericAssignmentExpressionOnly(nae, 
                                                           SymbolData.INT_TYPE.getInstanceData(), 
                                                           SymbolData.DOUBLE_TYPE.getInstanceData()));
      assertEquals("Should be 6 errors", 6, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot use a numeric assignment (-=, %=, *=, /=) on something of type int with something of " +
                   "type double", 
                   errors.get(5).getFirst());
    }
    
    
    public void testForShiftAssignmentExpressionOnly() {
      ShiftAssignmentExpression sae = new LeftShiftAssignmentExpression(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "j")), new IntegerLiteral(SourceInfo.NONE, 2));
      try {
        _etc.forShiftAssignmentExpressionOnly(sae, _sd1, _sd2);
        fail("forShiftAssignmentExpressionOnly should have thrown a runtime exception");
      }
      catch (RuntimeException e) {
        assertEquals("Exception message should be correct", "Internal Program Error: Shift assignment operators are not supported.  This should have been caught before the TypeChecker.  Please report this bug.", e.getMessage());
      }
    }
    
    public void testForBitwiseAssignmentExpressionOnly() {
      BitwiseAssignmentExpression bae = new BitwiseXorAssignmentExpression(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "j")), new IntegerLiteral(SourceInfo.NONE, 2));
      try {
        _etc.forBitwiseAssignmentExpressionOnly(bae, _sd1, _sd2);
        fail("forBitwiseAssignmentExpressionOnly should have thrown a runtime exception");
      }
      catch (RuntimeException e) {
        assertEquals("Exception message should be correct", "Internal Program Error: Bitwise assignment operators are not supported.  This should have been caught before the TypeChecker.  Please report this bug.", e.getMessage());
      }
    }
    
    
    public void testForBooleanExpressionOnly() {
      BooleanExpression be = new OrExpression(SourceInfo.NONE, new BooleanLiteral(SourceInfo.NONE, true), new BooleanLiteral(SourceInfo.NONE, false));
      
      //if both left and right are boolean instance types, everything is good
      
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), _etc.forBooleanExpressionOnly(be, SymbolData.BOOLEAN_TYPE.getInstanceData(), SymbolData.BOOLEAN_TYPE.getInstanceData()));
      assertEquals("There should be no errors", 0, errors.size());
      
      //if the left type is not an instance type, give an error
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), _etc.forBooleanExpressionOnly(be, SymbolData.BOOLEAN_TYPE, SymbolData.BOOLEAN_TYPE.getInstanceData()));
      assertEquals("There should now be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "The left side of this expression is a type, not an instance.  Perhaps you meant to create a new instance of boolean", errors.getLast().getFirst());
      
      //if the left type is an instance type but not a boolean type, give an error
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), _etc.forBooleanExpressionOnly(be, SymbolData.INT_TYPE.getInstanceData(), SymbolData.BOOLEAN_TYPE.getInstanceData()));
      assertEquals("There should now be 2 errors", 2, errors.size());
      assertEquals("The error message should be correct", "The left side of this expression is not a boolean value.  Therefore, you cannot apply a Boolean Operator (&&, ||) to it", errors.getLast().getFirst());
      
      //if the right type is not an instance type, give an error
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), _etc.forBooleanExpressionOnly(be, SymbolData.BOOLEAN_TYPE.getInstanceData(), SymbolData.BOOLEAN_TYPE));
      assertEquals("There should now be 3 errors", 3, errors.size());
      assertEquals("The error message should be correct", "The right side of this expression is a type, not an instance.  Perhaps you meant to create a new instance of boolean", errors.getLast().getFirst());
      
      //if the right type is an instance type but not a boolean give an error
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), _etc.forBooleanExpressionOnly(be, SymbolData.BOOLEAN_TYPE.getInstanceData(), SymbolData.DOUBLE_TYPE.getInstanceData()));
      assertEquals("There should now be 4 errors", 4, errors.size());
      assertEquals("The error message should be correct", 
                   "The right side of this expression is not a boolean value.  Therefore, you cannot apply a Boolean Operator (&&, ||) to it", errors.getLast().getFirst());
      
    }
    
    public void testForBitwiseBinaryExpressionOnly() {
      BitwiseBinaryExpression bbe = 
        new BitwiseAndExpression(SourceInfo.NONE, 
                                 new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "j")), 
                                 new IntegerLiteral(SourceInfo.NONE, 2));
      try {
        _etc.forBitwiseBinaryExpressionOnly(bbe, _sd2, _sd3);
        fail("forBitwiseBinaryExpressionOnly should have thrown a runtime exception");
      }
      catch (RuntimeException e) {
        assertEquals("Exception message should be correct", 
                     "Internal Program Error: Bitwise operators are not supported.  This should have been caught "
                       + "before the TypeChecker.  Please report this bug.", e.getMessage());
      }
    }
    
    
    public void testForEqualityExpressionOnly() {
      EqualityExpression ee = new EqualsExpression(SourceInfo.NONE, NULL_LITERAL, NULL_LITERAL);
      
      //left and right are both primitive and both boolean type--should work
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forEqualityExpressionOnly(ee, SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                                                  SymbolData.BOOLEAN_TYPE.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      //left and right are both primitive and both int type--should work
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forEqualityExpressionOnly(ee, SymbolData.INT_TYPE.getInstanceData(), 
                                                  SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      
      //left and right are both number types, only left is primitive--should work
      SymbolData integer = new SymbolData("java.lang.Integer");
      integer.setIsContinuation(false);
      symbolTable.put("java.lang.Integer", integer);
      
      SymbolData bool = new SymbolData("java.lang.Boolean");
      bool.setIsContinuation(false);
      symbolTable.put("java.lang.Boolean", bool);
      
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forEqualityExpressionOnly(ee, SymbolData.INT_TYPE.getInstanceData(), integer.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      //left and right are both number types, only right is primitive--should work
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forEqualityExpressionOnly(ee, integer.getInstanceData(), SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      //left and right are both boolean types, only left is primitive--should work
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forEqualityExpressionOnly(ee, SymbolData.BOOLEAN_TYPE.getInstanceData(), bool.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      //left and right are both boolean types, only right is primitive--should work
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forEqualityExpressionOnly(ee, bool.getInstanceData(), SymbolData.BOOLEAN_TYPE.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      
      //left and right are both instances of reference types--should work
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forEqualityExpressionOnly(ee, _sd1.getInstanceData(), _sd2.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      //left and right are both primitive, but one is int and one is boolean--does not work
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forEqualityExpressionOnly(ee, SymbolData.INT_TYPE.getInstanceData(), 
                                                  SymbolData.BOOLEAN_TYPE.getInstanceData()));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "At least one of the arguments to this Equality Operator (==, !=) is primitive.  Therefore, "
                     + "they must either both be number types or both be boolean types.  You have specified "
                     + "expressions with type int and boolean", 
                   errors.getLast().getFirst());
      
      //left is primitive, right is not, not both number types--does not work
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), _etc.forEqualityExpressionOnly(ee, SymbolData.INT_TYPE.getInstanceData(), _sd1.getInstanceData()));
      assertEquals("There should now be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "At least one of the arguments to this Equality Operator (==, !=) is primitive.  Therefore, "
                     + "they must either both be number types or both be boolean types.  You have specified "
                     + "expressions with type int and i.like.monkey", errors.getLast().getFirst());
      
      //left is not primitive, right is, not both primitives--does not work
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forEqualityExpressionOnly(ee, _sd1.getInstanceData(), SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("There should now be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", 
                   "At least one of the arguments to this Equality Operator (==, !=) is primitive.  Therefore, they "
                     + "must either both be number types or both be boolean types.  You have specified expressions "
                     + "with type i.like.monkey and int", 
                   errors.getLast().getFirst());
      
      //neither left nor right are primitive, but left side not an instance type
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forEqualityExpressionOnly(ee, _sd1, _sd2.getInstanceData()));
      assertEquals("There should now be 4 errors", 4, errors.size());
      assertEquals("Error message should be correct", 
                   "The arguments to this Equality Operator(==, !=) must both be instances.  Instead, you have "
                     +"referenced a type name on the left side.  Perhaps you meant to create a new instance of " + 
                   _sd1.getName(), 
                   errors.getLast().getFirst());
      
      //neither left nor right are primitive, but right side not an instance type
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forEqualityExpressionOnly(ee, _sd1.getInstanceData(), _sd2));
      assertEquals("There should now be 5 errors", 5, errors.size());
      assertEquals("Error message should be correct", "The arguments to this Equality Operator(==, !=) must both "
                     + "be instances.  Instead, you have referenced a type name on the right side.  Perhaps you "
                     + "meant to create a new instance of " + _sd2.getName(), 
                   errors.getLast().getFirst());
    }
    
    public void testForComparisonExpressionOnly() {
      ComparisonExpression ce = new LessThanExpression(SourceInfo.NONE, NULL_LITERAL, NULL_LITERAL);
      
      //does not throw an error if both expressions are numbers and instance types
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forComparisonExpressionOnly(ce, SymbolData.DOUBLE_TYPE.getInstanceData(), 
                                                    SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("There should be no errors", 0, errors.size());
      
      //gives an error if left side is not a number
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(),
                   _etc.forComparisonExpressionOnly(ce, SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                                                    SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "The left side of this expression is not a number.  Therefore, you cannot apply a Comparison "
                     + "Operator (<, >; <=, >=) to it", 
                   errors.getLast().getFirst());
      
      //gives an error if left side is not an instance type
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forComparisonExpressionOnly(ce, SymbolData.DOUBLE_TYPE, SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("There should be two errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "The left side of this expression is a type, not an instance.  Perhaps you meant to create a "
                     + "new instance of double", 
                   errors.getLast().getFirst());
      
      //gives an error if right side is not a number
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forComparisonExpressionOnly(ce, SymbolData.DOUBLE_TYPE.getInstanceData(), 
                                                    _sd1.getInstanceData()));
      assertEquals("There should be three errors", 3, errors.size());
      assertEquals("Error message should be correct", 
                   "The right side of this expression is not a number.  Therefore, you cannot apply a Comparison "
                     + "Operator (<, >; <=, >=) to it", 
                   errors.getLast().getFirst());
      
      // Gives an error if right side is not an instance type
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forComparisonExpressionOnly(ce, SymbolData.DOUBLE_TYPE.getInstanceData(), SymbolData.INT_TYPE));
      assertEquals("There should be four errors", 4, errors.size());
      assertEquals("Error message should be correct", "The right side of this expression is a type, not an instance.  "
                     + "Perhaps you meant to create a new instance of int", 
                   errors.getLast().getFirst());
    }
    
    
    public void testForShiftBinaryExpressionOnly() {
      ShiftBinaryExpression sbe = 
        new LeftShiftExpression(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "j")), 
                                new IntegerLiteral(SourceInfo.NONE, 42));
      try {
        _etc.forShiftBinaryExpressionOnly(sbe, _sd2, _sd3);
        fail("forShiftBinaryExpressionOnly should have thrown a runtime exception");
      }
      catch (RuntimeException e) {
        assertEquals("Exception message should be correct", 
                     "Internal Program Error: BinaryShifts are not supported.  This should have been caught before "
                       + "the TypeChecker.  Please report this bug.", e.getMessage());
      }
    }
    
    
    public void testForPlusExpressionOnly() {
      PlusExpression pe = new PlusExpression(SourceInfo.NONE, NULL_LITERAL, NULL_LITERAL);
      SymbolData string = new SymbolData("java.lang.String");
      string.setPackage("java.lang");
      string.setIsContinuation(false);
      symbolTable.put("java.lang.String", string);
      
      //left side is a string instance data and right side is some other instance data
      assertEquals("Should return String instance", string.getInstanceData(), 
                   _etc.forPlusExpressionOnly(pe, string.getInstanceData(), _sd1.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      //right side is a string instance data and left side is some other instance data
      assertEquals("Should return String instance", string.getInstanceData(), 
                   _etc.forPlusExpressionOnly(pe, _sd1.getInstanceData(), string.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      //both left and right are string instance datas
      assertEquals("Should return String instance", string.getInstanceData(), 
                   _etc.forPlusExpressionOnly(pe, string.getInstanceData(), string.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      //both left and right are numbers
      assertEquals("Should return Double instance", SymbolData.DOUBLE_TYPE.getInstanceData(), 
                   _etc.forPlusExpressionOnly(pe, SymbolData.DOUBLE_TYPE.getInstanceData(), 
                                              SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      //one side is a string instance data, but the other is not an instance data
      assertEquals("Should return String instance", string.getInstanceData(), 
                   _etc.forPlusExpressionOnly(pe, string.getInstanceData(), _sd1));
      assertEquals("Should be one error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "The arguments to the Plus Operator (+) must both be instances, but you have specified a type "
                     + "name.  Perhaps you meant to create a new instance of " + _sd1.getName(), 
                   errors.getLast().getFirst());
      
      // One side is a string, not a string instance data
      assertEquals("Should return String instance", string.getInstanceData(), 
                   _etc.forPlusExpressionOnly(pe, string, string.getInstanceData()));
      assertEquals("Should be two errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "The arguments to the Plus Operator (+) must both be instances, but you have specified a type "
                     + "name.  Perhaps you meant to create a new instance of java.lang.String", 
                   errors.getLast().getFirst());
      
      // One side is a number, the other is not
      assertEquals("Should return String instance", string.getInstanceData(), 
                   _etc.forPlusExpressionOnly(pe, SymbolData.INT_TYPE.getInstanceData(), 
                                              SymbolData.BOOLEAN_TYPE.getInstanceData()));
      assertEquals("Should be three errors", 3, errors.size());
      assertEquals("Error message should be correct", 
                   "The arguments to the Plus Operator (+) must either include an instance of a String or both "
                     + "be numbers.  You have specified arguments of type int and boolean", 
                   errors.getLast().getFirst());
      
      //both sides are numbers, but the left side is not an instance data
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forPlusExpressionOnly(pe, SymbolData.INT_TYPE, SymbolData.CHAR_TYPE.getInstanceData()));
      assertEquals("Should be 4 errors", 4, errors.size());
      assertEquals("Error message should be correct", 
                   "The arguments to the Plus Operator (+) must both be instances, but you have specified a type "
                     + "name.  Perhaps you meant to create a new instance of int", 
                   errors.getLast().getFirst());

      // Both sides are numbers, but the right side is not an instance data
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forPlusExpressionOnly(pe, SymbolData.INT_TYPE.getInstanceData(), SymbolData.CHAR_TYPE));
      assertEquals("Should be 5 errors", 5, errors.size());
      assertEquals("Error message should be correct", 
                   "The arguments to the Plus Operator (+) must both be instances, but you have specified a type "
                     + "name.  Perhaps you meant to create a new instance of char", 
                   errors.getLast().getFirst());
      
      
    }
    
    
    public void testForNumericBinaryExpressionOnly() {
      NumericBinaryExpression nbe = new ModExpression(SourceInfo.NONE, NULL_LITERAL, NULL_LITERAL);
      
      //two number instance expressions work--returns least restrictive type
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forNumericBinaryExpressionOnly(nbe, SymbolData.INT_TYPE.getInstanceData(), 
                                                       SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("There should be no errors", 0, errors.size());
      
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forNumericBinaryExpressionOnly(nbe, SymbolData.INT_TYPE.getInstanceData(), 
                                                       SymbolData.CHAR_TYPE.getInstanceData()));
      assertEquals("There should be no errors", 0, errors.size());
      
      assertEquals("Should return double instance", SymbolData.DOUBLE_TYPE.getInstanceData(), 
                   _etc.forNumericBinaryExpressionOnly(nbe, SymbolData.INT_TYPE.getInstanceData(), 
                                                       SymbolData.DOUBLE_TYPE.getInstanceData()));
      assertEquals("There should be no errors", 0, errors.size());
      
      //left not an instance data
      assertEquals("Should return double instance", SymbolData.DOUBLE_TYPE.getInstanceData(), 
                   _etc.forNumericBinaryExpressionOnly(nbe, SymbolData.INT_TYPE, 
                                                       SymbolData.DOUBLE_TYPE.getInstanceData()));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "The left side of this expression is a type, not an instance.  "
                     + "Perhaps you meant to create a new instance of int", errors.getLast().getFirst());
      
      //left not a number
      assertEquals("Should return double instance", SymbolData.DOUBLE_TYPE.getInstanceData(), 
                   _etc.forNumericBinaryExpressionOnly(nbe, SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                                                       SymbolData.DOUBLE_TYPE.getInstanceData()));
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", "The left side of this expression is not a number.  "
                     + "Therefore, you cannot apply a Numeric Binary Operator (*, /, -, %) to it", 
                   errors.getLast().getFirst());
      
      // right not an instance data
      assertEquals("Should return double instance", SymbolData.DOUBLE_TYPE.getInstanceData(), 
                   _etc.forNumericBinaryExpressionOnly(nbe, SymbolData.INT_TYPE.getInstanceData(), 
                                                       SymbolData.DOUBLE_TYPE));
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", "The right side of this expression is a type, not an instance.  "
                     + "Perhaps you meant to create a new instance of double", errors.getLast().getFirst());

      // right not a number
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forNumericBinaryExpressionOnly(nbe, SymbolData.INT_TYPE.getInstanceData(), 
                                                       SymbolData.BOOLEAN_TYPE.getInstanceData()));
      assertEquals("Should be 4 errors", 4, errors.size());
      assertEquals("Error message should be correct", 
                   "The right side of this expression is not a number.  Therefore, you cannot apply a "
                     + "Numeric Binary Operator (*, /, -, %) to it", errors.getLast().getFirst());
    }
    
    public void testForNoOpExpressionOnly() {
      NoOpExpression noe = new NoOpExpression(SourceInfo.NONE, NULL_LITERAL, NULL_LITERAL);
      try {
        _etc.forNoOpExpressionOnly(noe, null, null);
        fail("Should have thrown runtime exception");
      }
      catch (RuntimeException e) {
        assertEquals("Error message should be correct", "Internal Program Error: The student is missing an operator.  This should have been caught before the TypeChecker.  Please report this bug.", e.getMessage());
      }
    }
    
    public void testForIncrementExpressionOnly() {
      IncrementExpression ie = 
        new PositivePrefixIncrementExpression(SourceInfo.NONE, 
                                              new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "i")));
      
      //if value result is a number instance, should work fine
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forIncrementExpressionOnly(ie, SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      
      //if valueRes is null, return null but do not give error
      assertEquals("Should return null", null, _etc.forIncrementExpressionOnly(ie, null));
      assertEquals("Should be no errors", 0, errors.size());
      
      //if valueRes is PackageData, give error and return null
      PackageData pd = new PackageData("bad_reference");
      assertEquals("Should return null", null, _etc.forIncrementExpressionOnly(ie, pd));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "Could not resolve symbol bad_reference", errors.getLast().getFirst());
      
      // if valueRes is not an instance type, give an error
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forIncrementExpressionOnly(ie, SymbolData.INT_TYPE));
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot increment or decrement int, because it is a class name not an instance.  "
                     + "Perhaps you meant to create a new instance of int", errors.getLast().getFirst());
      
      // if value result is not a number type, give an error
      assertEquals("Should return sd2 instance", _sd2.getInstanceData(), 
                   _etc.forIncrementExpressionOnly(ie, _sd2.getInstanceData()));
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot increment or decrement something that is not a number type.  You have specified "
                     + "something of type " + _sd2.getName(), 
                   errors.getLast().getFirst());
    }
    
    
    
    public void testForNumericUnaryExpressionOnly() {
      NumericUnaryExpression nue = new PositiveExpression(SourceInfo.NONE, new IntegerLiteral(SourceInfo.NONE, 5));
      //number types like char and byte should be widened to int
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forNumericUnaryExpressionOnly(nue, SymbolData.CHAR_TYPE.getInstanceData()));
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forNumericUnaryExpressionOnly(nue, SymbolData.BYTE_TYPE.getInstanceData()));
      assertEquals("There should be no errors", 0, errors.size());
      
      //double type should be kept the same
      assertEquals("Should return double instance", SymbolData.DOUBLE_TYPE.getInstanceData(), 
                   _etc.forNumericUnaryExpressionOnly(nue, SymbolData.DOUBLE_TYPE.getInstanceData()));
      assertEquals("There should be no errors", 0, errors.size());
      
      //not an instance type
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forNumericUnaryExpressionOnly(nue, SymbolData.INT_TYPE));
      assertEquals("Should be one error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot use a numeric unary operator (+, -) with int, because it is a class name, "
                     + "not an instance.  Perhaps you meant to create a new instance of int", 
                   errors.getLast().getFirst());
      
      //not a number type
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forNumericUnaryExpressionOnly(nue, SymbolData.BOOLEAN_TYPE.getInstanceData()));
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot apply this unary operator to something of type boolean.  You can only apply it "
                     + "to a numeric type such as double, int, or char", errors.getLast().getFirst());
    }

    public void testForBitwiseNotExpressionOnly() {
      BitwiseNotExpression bne = 
        new BitwiseNotExpression(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "t")));
      try {
        _etc.forBitwiseNotExpressionOnly(bne, _sd3);
        fail("forBitwiseNotExpressionOnly should have thrown a runtime exception");
      }
      catch (RuntimeException e) {
        assertEquals("Exception message should be correct", "Internal Program Error: BitwiseNot is not supported.  It should have been caught before getting to the TypeChecker.  Please report this bug.", e.getMessage());
      }
    }
    
    
    public void testForNotExpressionOnly() {
      NotExpression ne = new NotExpression(SourceInfo.NONE, NULL_LITERAL);
      
      //should work with a boolean instance
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forNotExpressionOnly(ne, SymbolData.BOOLEAN_TYPE.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      //not an instance type
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forNotExpressionOnly(ne, SymbolData.BOOLEAN_TYPE));
      assertEquals("Should be one error", 1, errors.size());
      assertEquals("Error message should be correct",
                   "You cannot use the not (!) operator with boolean, because it is a class name, not an instance.  "
                     + "Perhaps you meant to create a new instance of boolean", errors.getLast().getFirst());
      
      //not a boolean type
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forNotExpressionOnly(ne, SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("Should be two errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot use the not (!) operator with something of type int. Instead, it should be used "
                     + "with an expression of boolean type", errors.getLast().getFirst());
    }
    
    
    public void testForConditionalExpressionOnly() {
      SymbolData sd1 = SymbolData.DOUBLE_TYPE;
      SymbolData sd2 = SymbolData.BOOLEAN_TYPE;
      SymbolData sd3 = SymbolData.INT_TYPE;
      ConditionalExpression cd = new ConditionalExpression(SourceInfo.NONE, 
                                                           new BooleanLiteral(SourceInfo.NONE, true),
                                                           new IntegerLiteral(SourceInfo.NONE, 5),
                                                           new IntegerLiteral(SourceInfo.NONE, 79));
      
      try {
        _etc.forConditionalExpressionOnly(cd, _sd3, _sd2, _sd1);
        fail("Should have thrown an exception.");
      }
      catch (Exception e) {
        assertEquals("Exception message should be correct", 
                     "Internal Program Error: Conditional expressions are not supported.  This should have been "
                       + "caught before the TypeChecker.  Please report this bug.", e.getMessage());
        
      }
    }  
    
    public void testForInstanceOfExpressionOnly() {
      SymbolData sd1 = SymbolData.DOUBLE_TYPE;
      SymbolData sd2 = SymbolData.BOOLEAN_TYPE;
      SymbolData sd3 = SymbolData.INT_TYPE;
      InstanceofExpression ioe = new InstanceofExpression(SourceInfo.NONE, NULL_LITERAL, JExprParser.NO_TYPE);
      assertEquals("When valueRes is subtype of typeRes, return BOOLEAN typeRes.", sd2.getInstanceData(), 
                   _etc.forInstanceofExpressionOnly(ioe, sd1, sd3.getInstanceData()));
      assertEquals("Should not throw an error.", 0, errors.size());
      assertEquals("When typeRes is subtype of valueRes, return BOOLEAN typeRes.", sd2.getInstanceData(), 
                   _etc.forInstanceofExpressionOnly(ioe, sd3, sd1.getInstanceData()));
      assertEquals("Should not throw an error.", 0, errors.size());
      assertEquals("When typeRes and valueRes are not subtypes of each other, return BOOLEAN typeRes", 
                   sd2.getInstanceData(),
                   _etc.forInstanceofExpressionOnly(ioe, sd2, sd1.getInstanceData()));
      assertEquals("Should now be one error.", 1, errors.size());
      assertEquals("Error message should be correct.", "You cannot test whether an expression of type " + sd1.getName() 
                     + " belongs to type " + sd2.getName() + " because they are not related", 
                   errors.getLast().getFirst());     
      SymbolData foo = new SymbolData("Foo");
      SymbolData fooMama = new SymbolData("FooMama");
      foo.setSuperClass(fooMama);
      assertEquals("When valueRes is a SymbolData, return BOOLEAN typeRes",  sd2.getInstanceData(), 
                   _etc.forInstanceofExpressionOnly(ioe, foo, fooMama));
      assertEquals("There should be 2 errors.", 2, errors.size());
      assertEquals("Error message should be correct.", 
                   "You are trying to test if FooMama belongs to type, but it is a class or interface type, "
                     + "not an instance.  Perhaps you meant to create a new instance of FooMama",
                   errors.getLast().getFirst());
    }
    
    public void testClassInstantiationHelper() {
      ClassInstantiation simpleCI = 
        new SimpleNamedClassInstantiation(SourceInfo.NONE, 
                                          new ClassOrInterfaceType(SourceInfo.NONE, "testClass", new Type[0]),
                                          new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]));
      ClassInstantiation complexCI = 
        new ComplexNamedClassInstantiation(SourceInfo.NONE,
                                           new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "Outer")),
                                           new ClassOrInterfaceType(SourceInfo.NONE, "Inner", new Type[0]),
                                           new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]));
      
      ParenthesizedExpressionList pel = 
        new ParenthesizedExpressionList(SourceInfo.NONE, 
                                        new Expression[] {new SimpleNameReference(SourceInfo.NONE, 
                                                                                  new Word(SourceInfo.NONE, "int"))});
      ClassInstantiation badArgs =  
        new SimpleNamedClassInstantiation(SourceInfo.NONE,
                                          new ClassOrInterfaceType(SourceInfo.NONE, "anotherClass", new Type[0]),
                                          pel);
      
      SymbolData testClass = new SymbolData("testClass");
      SymbolData outer = new SymbolData("Outer");
      SymbolData outerInner = new SymbolData("Outer.Inner");
      outer.addInnerClass(outerInner);
      outerInner.setOuterData(outer);
      
      //classToInstantiate==null
      assertEquals("Should return null", null, _etc.classInstantiationHelper(simpleCI, null));
      assertEquals("Should be no errors", 0, errors.size());
      
      
      //if arg is a type instead of an instance, throw an error
      assertEquals("Should return double instance", SymbolData.DOUBLE_TYPE.getInstanceData(), 
                   _etc.classInstantiationHelper(badArgs, SymbolData.DOUBLE_TYPE));
      assertEquals("Should be one error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "Cannot pass a class or interface name as a constructor argument.  Perhaps you meant to create a "
                     + "new instance of int", errors.getLast().getFirst());
      
      //if no matching constructor, give error
      assertEquals("Should return instance of testClass", testClass.getInstanceData(), 
                   _etc.classInstantiationHelper(simpleCI, testClass));
      assertEquals("Should be two errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "No constructor found in class testClass with signature: testClass().", errors.getLast().getFirst());
      
      assertEquals("Should return instance of Outer.Inner", outerInner.getInstanceData(), 
                   _etc.classInstantiationHelper(complexCI, outerInner));
      assertEquals("Should be three errors", 3, errors.size());
      assertEquals("Error message should be correct", 
                   "No constructor found in class Outer.Inner with signature: Inner().", errors.getLast().getFirst());
      
      
      // if everything is in order, just return
      MethodData md = new MethodData("testClass", _publicMav, new TypeParameter[0], testClass, 
                                     new VariableData[0], 
                                     new String[0], 
                                     testClass,
                                     null);
      testClass.addMethod(md);
      assertEquals("Should return instance of testClass", testClass.getInstanceData(), 
                   _etc.classInstantiationHelper(simpleCI, testClass));
      assertEquals("Should still be just three errors", 3, errors.size());
    }
    
    
    public void testForSimpleNamedClassInstantiation() { 
      ParenthesizedExpressionList pel1 = 
        new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[] {new IntegerLiteral(SourceInfo.NONE, 5)});
      SimpleNamedClassInstantiation ci1 = 
        new SimpleNamedClassInstantiation(SourceInfo.NONE, 
                                          new ClassOrInterfaceType(SourceInfo.NONE, "simpleClass", new Type[0]), 
                                          pel1); 
      SimpleNamedClassInstantiation ci3 = 
        new SimpleNamedClassInstantiation(SourceInfo.NONE, 
                                          new ClassOrInterfaceType(SourceInfo.NONE, "simpleClass", new Type[0]), 
                                          new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]));
      
      // if the type is not in the symbolTable, an error should be added on lookup, and null should be returned:
      assertEquals("Should return null, since simpleClass is not in symbol table", null, ci1.visit(_etc));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "Class or variable simpleClass not found.", errors.getLast().getFirst());
      
      //if class is in symbol table, but not visible from this context, should give an error but still return instance of type
      SymbolData simpleClass = new SymbolData("simpleClass");
      simpleClass.setIsContinuation(false);
      MethodData cons1 = new MethodData("simpleClass", _publicMav, new TypeParameter[0], simpleClass, 
                                        new VariableData[0], 
                                        new String[0], 
                                        simpleClass,
                                        null);
      simpleClass.addMethod(cons1);
      symbolTable.put("simpleClass", simpleClass);
      
      assertEquals("Should return simpleClass even though it could not really access it", 
                   simpleClass.getInstanceData(), ci3.visit(_etc));
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "The class or interface simpleClass is package protected because there is no access specifier and "
                     + "cannot be accessed from i.like.monkey", 
                   errors.getLast().getFirst());
      
      // if class is in symbol table and visible, but there is not a matching constructor, should give an error 
      // but still return instance of type
      simpleClass.setMav(new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"}));
      
      assertEquals("Should return simpleClass even though it could not find constructor", simpleClass.getInstanceData(), 
                   ci1.visit(_etc));
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", 
                   "No constructor found in class simpleClass with signature: simpleClass(int).", 
                   errors.getLast().getFirst());
      
      //if class is in symbol table, and there is a matching constructor, should not give any errors
      MethodData cons2 = new MethodData("simpleClass", _publicMav, new TypeParameter[0], simpleClass, 
                                        new VariableData[] {new VariableData(SymbolData.INT_TYPE)}, 
                                        new String[0], 
                                        simpleClass,
                                        null);
      simpleClass.addMethod(cons2);                                   
      assertEquals("Should return simpleClass", simpleClass.getInstanceData(), ci1.visit(_etc));
      assertEquals("Should still be 3 errors", 3, errors.size());
      
      
      //if class is abstract, cannot be instantiated
      simpleClass.addModifier("abstract");
      assertEquals("Should return simpleClass even though it cannot really be instantiated", 
                   simpleClass.getInstanceData(), ci1.visit(_etc));
      assertEquals("Should be 4 errors", 4, errors.size());
      assertEquals("Error message should be correct", "simpleClass is abstract and thus cannot be instantiated", 
                   errors.getLast().getFirst());
      
      
      //now, what if we are dealing with an inner class?
      SimpleNamedClassInstantiation ci2 = 
        new SimpleNamedClassInstantiation(SourceInfo.NONE, 
                                          new ClassOrInterfaceType(SourceInfo.NONE, "A.B", new Type[0]), 
                                          new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]));
      
      
      SymbolData a = new SymbolData("A");
      a.setIsContinuation(false);
      SymbolData b = new SymbolData("A$B");
      b.setIsContinuation(false);
      b.setOuterData(a);
      a.addInnerClass(b);
      MethodData consb = new MethodData("B", _publicMav, new TypeParameter[0], b, 
                                        new VariableData[0], 
                                        new String[0], 
                                        b,
                                        null);
      b.addMethod(consb);
      symbolTable.put("A", a);
      a.setMav(new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"}));
      b.setMav(new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"}));

      //if inner part is not static, give error
      assertEquals("Should return A.B", b.getInstanceData(), ci2.visit(_etc));
      assertEquals("Should be 5 errors", 5, errors.size());
      assertEquals("Error message should be correct", 
                   "A.B is not a static inner class, and thus cannot be instantiated from this context.  Perhaps "
                     +"you meant to use an instantiation of the form new A().new B()", 
                   errors.getLast().getFirst());
      //if inner part is static, no problem
      b.addModifier("static");
      assertEquals("Should return A.B", b.getInstanceData(), ci2.visit(_etc));
      assertEquals("Should still be just 5 errors", 5, errors.size()); 
    }
    
    public void testForComplexNamedClassInstantiation() {
      ParenthesizedExpressionList pel1 = 
        new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[] { new IntegerLiteral(SourceInfo.NONE, 5)});
      ComplexNamedClassInstantiation ci1 = 
        new ComplexNamedClassInstantiation(SourceInfo.NONE, 
                                           new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "o")), 
                                           new ClassOrInterfaceType(SourceInfo.NONE, "innerClass", new Type[0]),                                  
                                           pel1);
      
      ComplexNamedClassInstantiation ci2 = 
        new ComplexNamedClassInstantiation(SourceInfo.NONE, 
                                           new SimpleNameReference(SourceInfo.NONE, 
                                                                   new Word(SourceInfo.NONE, "o")), 
                                           new ClassOrInterfaceType(SourceInfo.NONE, "innerClass", new Type[0]), 
                                           new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]));
      
      //if outer type is not in vars list, give appropriate error
      assertEquals("Should return null", null, ci1.visit(_etc));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "Could not resolve symbol o", errors.getLast().getFirst());
      
      // if outer class is in symbol table and visible, but there is not a matching inner constructor, should give 
      // an error but still return instance of type
      SymbolData outerClass = new SymbolData("outer");
      outerClass.setIsContinuation(false);
      SymbolData innerClass = new SymbolData("outer$innerClass");
      innerClass.setIsContinuation(false);
      outerClass.addInnerClass(innerClass);
      innerClass.setOuterData(outerClass);
      MethodData cons1 = new MethodData("innerClass", _publicMav, new TypeParameter[0], innerClass, 
                                        new VariableData[0], 
                                        new String[0], 
                                        innerClass,
                                        null);
      innerClass.addMethod(cons1);
      symbolTable.put("outer", outerClass);
      _etc._vars.addLast(new VariableData("o", _publicMav, outerClass, true, _etc._data));
      outerClass.setMav(new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"}));
      innerClass.setMav(new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"}));
      
      assertEquals("Should return innerClass even though it could not find constructor", innerClass.getInstanceData(), 
                   ci1.visit(_etc));
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct",
                   "No constructor found in class outer.innerClass with signature: innerClass(int).", 
                   errors.getLast().getFirst());
      
      //if class is in symbol table, and there is a matching constructor, should not give any errors
      MethodData cons2 = new MethodData("innerClass", _publicMav, new TypeParameter[0], innerClass, 
                                        new VariableData[] {new VariableData(SymbolData.INT_TYPE)}, 
                                        new String[0], 
                                        innerClass,
                                        null);
      innerClass.addMethod(cons2);                                   
      assertEquals("Should return innerClass", innerClass.getInstanceData(), ci1.visit(_etc));
      assertEquals("Should still be 2 errors", 2, errors.size());
      
      //if class is abstract, cannot be instantiated
      innerClass.addModifier("abstract");
      assertEquals("Should return innerClass even though it cannot really be instantiated", innerClass.getInstanceData(), 
                   ci1.visit(_etc));
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", "outer.innerClass is abstract and thus cannot be instantiated", 
                   errors.getLast().getFirst());              
      
      //if enclosingType is not an instance, and result is not static, give error
      ComplexNamedClassInstantiation ci3 = 
        new ComplexNamedClassInstantiation(SourceInfo.NONE, 
                                           new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "outer")), 
                                           new ClassOrInterfaceType(SourceInfo.NONE, "innerClass", new Type[0]), 
                                           new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]));             
      outerClass.setMav(new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"}));
      innerClass.setMav(new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"}));
      assertEquals("Should return innerClass even though the syntax was wrong", innerClass.getInstanceData(), 
                   ci3.visit(_etc));
      assertEquals("Should be 4 errors", 4, errors.size());
      assertEquals("Error message should be correct", 
                   "The constructor of a non-static inner class can only be called on an instance of its containing "
                     + "class (e.g. new outer().new innerClass())", 
                   errors.getLast().getFirst());
      
      //if result is static, give appropriate error:
      innerClass.addModifier("static");
      assertEquals("Should return innerClass even though the syntax was wrong", 
                   innerClass.getInstanceData(), ci1.visit(_etc));
      assertEquals("Should be 5 errors", 5, errors.size());
      
      assertEquals("Error message should be correct", 
                   "You cannot instantiate a static inner class or interface with this syntax.  Instead, "
                     + "try new outer.innerClass()",
                   errors.getLast().getFirst());
      
      
      //if inner class of that name does not exist, give an error
      innerClass.setMav(_publicMav);
      ParenthesizedExpressionList pel2 = 
        new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[] {new IntegerLiteral(SourceInfo.NONE, 5)});
      ComplexNamedClassInstantiation ci4 = 
        new ComplexNamedClassInstantiation(SourceInfo.NONE, 
                                           new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "o")), 
                                           new ClassOrInterfaceType(SourceInfo.NONE, "notInnerClass", new Type[0]), 
                                           pel2);
      assertEquals("Should return null", null, ci4.visit(_etc));
      assertEquals("Should be 6 errors", 6, errors.size());
      assertEquals("Error message should be correct", "Class or variable notInnerClass not found.", 
                   errors.getLast().getFirst());
      
      
      //if inner class is private, give error
      innerClass.setMav(_privateMav);
      assertEquals("Should return inner class", innerClass.getInstanceData(), ci1.visit(_etc));
      assertEquals("Should be 7 errors", 7, errors.size());
      assertEquals("Error message should be correct", 
                   "The class or interface outer.innerClass in outer.innerClass is private and cannot be accessed from i.like.monkey", 
                   errors.getLast().getFirst());
      
      //if outer class is private, give error
      outerClass.setMav(_privateMav);
      innerClass.setMav(_publicMav);
      assertEquals("Should return inner class", innerClass.getInstanceData(), ci1.visit(_etc));
      assertEquals("Should be 8 errors", 8, errors.size());
      assertEquals("Error message should be correct", 
                   "The class or interface outer in outer is private and cannot be accessed from i.like.monkey", 
                   errors.getLast().getFirst());
    }
    
    public void testForSimpleThisConstructorInvocation() {
      //this should always add an error:
      SimpleThisConstructorInvocation stci = 
        new SimpleThisConstructorInvocation(SourceInfo.NONE, 
                                            new ParenthesizedExpressionList(SourceInfo.NONE, 
                                                                            new Expression[0]));
      assertEquals("Should return null", null, stci.visit(_etc));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "This constructor invocations are only allowed as the first statement of a constructor body", 
                   errors.getLast().getFirst());
    }
    
    public void testForComplexThisConstructorInvocation() {
      //this should always add an error
      ComplexThisConstructorInvocation ctci = 
        new ComplexThisConstructorInvocation(SourceInfo.NONE, 
                                             new SimpleNameReference(SourceInfo.NONE,
                                                                     new Word(SourceInfo.NONE, "something")), 
                                             new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]));
      assertEquals("Should return null", null, ctci.visit(_etc));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "Constructor invocations of this form are never allowed", 
                   errors.getLast().getFirst());
    }
    
    public void testForSimpleNameReference() {
      //first, consider the case where what we have is a variable reference:
      SimpleNameReference var = new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "variable1"));
      VariableData varData = new VariableData("variable1", _publicMav, SymbolData.INT_TYPE, false, _etc._data);
      _etc._vars.add(varData);
      
      //in this case, it has not been initialized--should throw error
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), var.visit(_etc));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot use variable1 because it may not have been given a value", 
                   errors.getLast().getFirst());
      
      // if it has been initialized, do not give an error
      varData.gotValue();
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), var.visit(_etc));
      assertEquals("Should still be 1 error", 1, errors.size());
      
      // if variable is non-static, but you are in static context, cannot reference it. Should give error
      MethodData newContext =
        new MethodData("method", _publicStaticMav, new TypeParameter[0], SymbolData.INT_TYPE, new VariableData[0], 
                       new String[0], _sd1, NULL_LITERAL); 
      _etc._data = newContext;
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), var.visit(_etc));
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "Non-static variable or field variable1 cannot be referenced from a static context", 
                   errors.getLast().getFirst());
      
      // Test reference to private local variable in a method
      SimpleNameReference var2 = new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "variable1"));
      MethodData newContext2 = 
        new MethodData("method2", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, new VariableData[0], 
                       new String[0], _sd1, NULL_LITERAL);
      VariableData varData2 = new VariableData("variable2", _privateMav, SymbolData.INT_TYPE, false, newContext2);
      newContext2.addVar(varData2);
      
      varData2.gotValue();  // Give the private variable a value
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), var2.visit(_etc));
      assertEquals("Should be still be 2 errors", 2, errors.size());
      
      _etc._data = _sd1;
        
      // if it is a variable of your super class, it won't be in _vars.  Check this case.
      _etc._vars = new LinkedList<VariableData>();
      _sd1.setSuperClass(_sd2);
      _sd2.addVar(varData);
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), var.visit(_etc));
      assertEquals("Should still be 2 errors", 2, errors.size());
      
      // now, consider the case where what we have is a class reference:
      SimpleNameReference className = new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "Frog"));
      SymbolData frog = new SymbolData("Frog");
      frog.setIsContinuation(false);
      symbolTable.put("Frog", frog);
      
      //if it is not visibile from this context, return package data
      TypeData result = className.visit(_etc);
      assertTrue("Result should be a PackageData since Frog is not accessible", result instanceof PackageData);
      assertEquals("Should have correct name", "Frog", result.getName());
      assertEquals("Should still be 2 errors", 2, errors.size());
      
      // if it is visibile from this context, no error
      frog.setMav(_publicMav);
      assertEquals("Should return Frog", frog, className.visit(_etc));
      assertEquals("Should still be 2 errors", 2, errors.size());
      
      // Finally, if the name cannot be resolved, simply return a packageData.
      SimpleNameReference fake = new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "notRealReference"));
      assertEquals("Should return package data", "notRealReference", (fake.visit(_etc)).getName());
      assertEquals("Should still be just 2 errors", 2, errors.size());
      
      // if the reference is ambiguous (matches both an interface and a class) give an error
      SimpleNameReference ambigRef = new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "ambigThing"));
      
      SymbolData interfaceD = new SymbolData("interface");
      interfaceD.setIsContinuation(false);
      interfaceD.setInterface(true);
      interfaceD.setMav(_publicMav);
      
      SymbolData classD = new SymbolData("superClass");
      classD.setIsContinuation(false);
      classD.setMav(_publicMav);
      
      SymbolData ambigThingI = new SymbolData("ambigThing");
      ambigThingI.setIsContinuation(false);
      ambigThingI.setInterface(true);
      interfaceD.addInnerInterface(ambigThingI);
      ambigThingI.setOuterData(interfaceD);
      ambigThingI.setMav(_publicStaticMav);
      
      SymbolData ambigThingC = new SymbolData("ambigThing");
      ambigThingC.setIsContinuation(false);
      classD.addInnerClass(ambigThingC);
      ambigThingC.setOuterData(classD);
      ambigThingC.setMav(_publicStaticMav);
      
      _sd6.addInterface(interfaceD);
      _sd6.setSuperClass(classD);
      
      _sd6.setMav(_publicMav);
      _sd6.setIsContinuation(false);
      
      _etc._data = _sd6;
      
      assertEquals("Should return null", null, ambigRef.visit(_etc));
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", 
                   "Ambiguous reference to class or interface ambigThing", 
                   errors.getLast().getFirst());    
    }
    
    
    public void testForComplexNameReference() {
      //if lhs is a package data, we want to keep building it:
      
      //if whole reference is just package reference, return package data
      ComplexNameReference ref1 = 
        new ComplexNameReference(SourceInfo.NONE, 
                                 new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "java")), 
                                 new Word(SourceInfo.NONE, "lang"));
      assertEquals("Should return correct package data", "java.lang", ref1.visit(_etc).getName());
      assertEquals("Should be no errors", 0, errors.size());
      
      // if reference builds to a class in the symbol table, return that class
      ComplexNameReference ref2 = 
        new ComplexNameReference(SourceInfo.NONE, ref1, new Word(SourceInfo.NONE, "String"));
      assertTrue("symbol table already contains String", symbolTable.containsKey("java.lang.String"));
      SymbolData string = symbolTable.get("java.lang.String");          
//      SymbolData string = new SymbolData("java.lang.String");
//      string.setPackage("java.lang");
//      string.setMav(_publicMav);
//      string.setIsContinuation(false);
//      symbolTable.put("java.lang.String", string);
      
      assertEquals("Should return string", string, ref2.visit(_etc));
      
      assertEquals("Should still be no errors", 0, errors.size());
      
      
      //if lhs is not a package data, it gets more complicated:
      
      // we're referencing a variable inside of symbol data lhs:
      VariableData myVar = new VariableData("myVar", _publicStaticMav, SymbolData.DOUBLE_TYPE, true, string);
      string.addVar(myVar);
      ComplexNameReference varRef1 = new ComplexNameReference(SourceInfo.NONE, ref2, new Word(SourceInfo.NONE, "myVar"));
      
      // static var from static context
      assertEquals("Should return Double_Type instance", SymbolData.DOUBLE_TYPE.getInstanceData(), varRef1.visit(_etc));
      assertEquals("There should still be no errors", 0, errors.size());
      
      // Static uninitialized var from static context
      myVar.lostValue();
      assertEquals("Should return Double_Type instance", SymbolData.DOUBLE_TYPE.getInstanceData(), varRef1.visit(_etc));
      assertEquals("There should still be one error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot use myVar here, because it may not have been given a value", 
                   errors.getLast().getFirst());
      
      // Non-static var--this is a static context
      myVar.gotValue();
      myVar.setMav(_publicMav);
      assertEquals("Should return double instance", SymbolData.DOUBLE_TYPE.getInstanceData(), varRef1.visit(_etc));
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "Non-static variable myVar cannot be accessed from the static context java.lang.String.  "
                     + "Perhaps you meant to instantiate an instance of java.lang.String", 
                   errors.getLast().getFirst());
      
      // Non-static context, okay to reference non-static var
      VariableData stringVar = new VariableData("s", _publicMav, string, true, _etc._data);  
      _etc._vars.add(stringVar);
      ComplexNameReference varRef2 = 
        new ComplexNameReference(SourceInfo.NONE, 
                                 new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "s")), 
                                 new Word(SourceInfo.NONE, "myVar"));
      assertEquals("Should return double instance", SymbolData.DOUBLE_TYPE.getInstanceData(), varRef2.visit(_etc));
      assertEquals("Should still just be 2 errors", 2, errors.size());
      
      // Non-static context, okay to reference private non-static var
      VariableData privateStringVar = new VariableData("ps", _privateMav, string, true, _etc._data);
      _etc._vars.add(privateStringVar);
      ComplexNameReference varRef25 = 
        new ComplexNameReference(SourceInfo.NONE, 
                                 new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "ps")), 
                                 new Word(SourceInfo.NONE, "myVar"));
      assertEquals("Should return double instance", SymbolData.DOUBLE_TYPE.getInstanceData(), varRef25.visit(_etc));
      assertEquals("Should still just be 2 errors", 2, errors.size());
      
      // if it is a variable of the super class, you should still be able to see it.  Check this case.
      string.setVars(new LinkedList<VariableData>());
      string.setSuperClass(_sd2);
      _sd2.addVar(myVar);
      assertEquals("Should return double instance", SymbolData.DOUBLE_TYPE.getInstanceData(), varRef2.visit(_etc));
      assertEquals("Should still be 2 errors", 2, errors.size());
      
      // a complex multiple variable reference case:
      VariableData vd1 = new VariableData("Mojo", _publicMav, SymbolData.INT_TYPE, true, _sd1);   // was _publicMav
      VariableData vd2 = new VariableData("Santa's Little Helper", _publicMav, _sd1, true, _sd2); // was _publicMav
      VariableData vd3 = new VariableData("Snowball1", _publicMav, _sd2, true, _sd3);             // was _publicMav
      _sd3.addVar(vd3);
      _sd2.addVar(vd2);
      _sd1.addVar(vd1);
      
      ComplexNameReference varRef3 = 
        new ComplexNameReference(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, 
                                                                                                    "Snowball1")),
                                 new Word(SourceInfo.NONE, "Santa's Little Helper"));
      ComplexNameReference varRef4 = 
        new ComplexNameReference(SourceInfo.NONE, varRef3, new Word(SourceInfo.NONE, "Mojo"));
      
      Data oldData = _etc._data;
      _etc._data = _sd3;
      _etc._vars.add(vd3);
      _sd3.setMav(_publicMav);
      _sd1.setMav(_publicMav);
      _sd2.setMav(_publicMav);
      
      TypeData result = varRef4.visit(_etc);
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), result);
      assertEquals("Should still be 2 errors", 2, errors.size());
      
      _etc._data = oldData;

      // What if what we have is an inner class?
      SymbolData inner = new SymbolData("java.lang.String$Inner");
      inner.setPackage("java.lang");
      inner.setIsContinuation(false);
      inner.setOuterData(string);
      string.addInnerClass(inner);
      
      //if inner is not visible, throw error
      ComplexNameReference innerRef0 = 
        new ComplexNameReference(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, 
                                                                          new Word(SourceInfo.NONE, "s")),
                                 new Word(SourceInfo.NONE, "Inner"));
      assertEquals("Should return null", null, innerRef0.visit(_etc));
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", 
                   "The class or interface java.lang.String.Inner is package protected because there is no access "
                     + "specifier and cannot be accessed from i.like.monkey", 
                   errors.getLast().getFirst());
      
      inner.setMav(_publicMav);
      
      
      //if inner is not static, give error:
      ComplexNameReference innerRef1 = 
        new ComplexNameReference(SourceInfo.NONE, ref2, new Word(SourceInfo.NONE, "Inner"));
      assertEquals("Should return inner", inner, innerRef1.visit(_etc));
      assertEquals("Should be 4 errors", 4, errors.size());
      assertEquals("Error message should be correct", 
                   "Non-static inner class java.lang.String.Inner cannot be accessed from this context.  "
                     + "Perhaps you meant to instantiate it", errors.getLast().getFirst());
      
      //if inner is not static and outer is not static, it's okay...
      ComplexNameReference innerRef2 = 
        new ComplexNameReference(SourceInfo.NONE, 
                                 new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "s")), 
                                 new Word(SourceInfo.NONE, "Inner"));
      assertEquals("Should return inner", inner, innerRef2.visit(_etc));
      assertEquals("Should still be 5 errors", 5, errors.size());
      assertEquals("Error message should be correct", 
                   "Non-static inner class java.lang.String.Inner cannot be accessed from this context.  "
                     + "Perhaps you meant to instantiate it", 
                   errors.getLast().getFirst());
      
      //if inner is static and outer is not static, throw error
      inner.setMav(_publicStaticMav);
      assertEquals("Should return inner", inner, innerRef2.visit(_etc));
      assertEquals("Should be 6 errors", 6, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot reference the static inner class java.lang.String.Inner from an instance of "
                     + "java.lang.String.  Perhaps you meant to say java.lang.String.Inner", 
                   errors.getLast().getFirst());
      
      
      //if the symbol could not be matched, give an error and return null
      ComplexNameReference noSense = 
        new ComplexNameReference(SourceInfo.NONE, ref2, new Word(SourceInfo.NONE, "nonsense"));
      assertEquals("Should return null", null, noSense.visit(_etc));
      assertEquals("Should be 7 errors", 7, errors.size());
      assertEquals("Error message should be correct", "Could not resolve nonsense from the context of java.lang.String", 
                   errors.getLast().getFirst());
      
      //if the reference is ambiguous (matches both an interface and a class) give an error
      ComplexNameReference ambigRef = 
        new ComplexNameReference(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, 
                                                                          new Word(SourceInfo.NONE, "cebu")), 
                                 new Word(SourceInfo.NONE, "ambigThing"));
      
      SymbolData interfaceD = new SymbolData("interface");
      interfaceD.setIsContinuation(false);
      interfaceD.setInterface(true);
      interfaceD.setMav(_publicMav);
      
      SymbolData classD = new SymbolData("superClass");
      classD.setIsContinuation(false);
      classD.setMav(_publicMav);
      
      SymbolData ambigThingI = new SymbolData("ambigThing");
      ambigThingI.setIsContinuation(false);
      ambigThingI.setInterface(true);
      interfaceD.addInnerInterface(ambigThingI);
      ambigThingI.setOuterData(interfaceD);
      ambigThingI.setMav(_publicStaticMav);
      
      SymbolData ambigThingC = new SymbolData("ambigThing");
      ambigThingC.setIsContinuation(false);
      classD.addInnerClass(ambigThingC);
      ambigThingC.setOuterData(classD);
      ambigThingC.setMav(_publicStaticMav);
      
      _sd6.addInterface(interfaceD);
      _sd6.setSuperClass(classD);
      
      symbolTable.put("cebu", _sd6);
      _sd6.setMav(_publicMav);
      _sd6.setIsContinuation(false);
      
      assertEquals("Should return null", null, ambigRef.visit(_etc));
      assertEquals("Should be 8 errors", 8, errors.size());
      assertEquals("Error message should be correct", 
                   "Ambiguous reference to class or interface ambigThing", 
                   errors.getLast().getFirst());    
      
      //if lhs is not visible or inner is not visible, should throw error
      inner.setMav(_publicStaticMav);
      string.setMav(_privateMav);
      
      assertEquals("Should return inner", inner, innerRef1.visit(_etc));
      assertEquals("Should be 9 errors", 9, errors.size());
      assertEquals("Error message should be correct", 
                   "The class or interface java.lang.String in java.lang.String is private and cannot be accessed from i.like.monkey", 
                   errors.getLast().getFirst());
    }
    
    
    public void testForSimpleThisReference() {
      SimpleThisReference str = new SimpleThisReference(SourceInfo.NONE);
      
      //as long as we are not in a static method, this should be fine.
      assertEquals("Should return i.like.monkey instance", _etc._data.getSymbolData().getInstanceData(), str.visit(_etc));
      assertEquals("Should be no errors", 0, errors.size());
      
      //if we are in a static method, give appropriate error
      MethodData sm = new MethodData("staticMethod", new VariableData[0]);
      sm.setMav(_publicStaticMav);
      sm.setOuterData(_etc._data);
      _etc._data = sm;
      
      assertEquals("Should return i.like.monkey instance", _etc._data.getSymbolData().getInstanceData(), 
                   str.visit(_etc));
      assertEquals("Should be one errors", 1, errors.size());
      assertEquals("Error message should be correct", "'this' cannot be referenced from within a static method", 
                   errors.getLast().getFirst());
    }
    
    
    public void testForComplexThisReferenceOnly() {
      ComplexThisReference ctr = 
        new ComplexThisReference(SourceInfo.NONE, 
                                 new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "context")));
      
      // if enclosing_result is null, return null
      assertEquals("Should return null", null, _etc.forComplexThisReferenceOnly(ctr, null));
      assertEquals("Should be no errors", 0, errors.size());
      
      // if enclosing result is a PackageData, give appropriate error and return null
      assertEquals("Should return null", null, _etc.forComplexThisReferenceOnly(ctr, new PackageData("context")));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct","Could not resolve symbol context" , errors.getLast().getFirst());
      
      // if enclosing_result is not an outer data of the current context, give an error
      SymbolData contextClass = new SymbolData("context");
      contextClass.setIsContinuation(false);
      contextClass.setMav(_publicMav);
      assertEquals("Should return instance of this", contextClass.getInstanceData(), 
                   _etc.forComplexThisReferenceOnly(ctr, contextClass));
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("The error message should be correct", "You cannot reference context.this from here, "
                     + "because context is not an outer class of i.like.monkey", 
                   errors.getLast().getFirst());
      
      // if enclosing_result is an outer data of current context, everything is peachy
      _etc._data.setOuterData(contextClass);
      contextClass.addInnerClass(_etc._data.getSymbolData());
      assertEquals("Should return instance of this", contextClass.getInstanceData(), 
                   _etc.forComplexThisReferenceOnly(ctr, contextClass));
      assertEquals("Should still be 2 errors", 2, errors.size());
      
      // if we are in a static method, throw appropriate error
      MethodData sm = new MethodData("staticMethod", new VariableData[0]);
      sm.setMav(_publicStaticMav);
      sm.setOuterData(_etc._data);
      _etc._data = sm;
      assertEquals("Should return instance of this", contextClass.getInstanceData(), 
                   _etc.forComplexThisReferenceOnly(ctr, contextClass));
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("The error message should be correct", "'this' cannot be referenced from within a static method", 
                   errors.getLast().getFirst());
      
      // if the enclosing result is an instance type, throw an error
      _etc._data = sm.getOuterData();
      assertEquals("Should return instance of this", contextClass.getInstanceData(), 
                   _etc.forComplexThisReferenceOnly(ctr, contextClass.getInstanceData()));
      assertEquals("Should be 4 errors", 4, errors.size());
      assertEquals("The error message should be correct", 
                   "'this' can only be referenced from a type name, but you have specified an instance of that type.", 
                   errors.getLast().getFirst());
      
      //if current context is static, give an error
      _etc._data.getSymbolData().addModifier("static");
      assertEquals("Should return instance of this", contextClass.getInstanceData(), 
                   _etc.forComplexThisReferenceOnly(ctr, contextClass));
      assertEquals("Should be 5 errors", 5, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot reference context.this from here, because i.like.monkey or one of its enclosing "
                     + "classes is static.  Thus, an enclosing instance of context does not exist", 
                   errors.getLast().getFirst());
    }
    
    public void testForSimpleSuperReference() {
      SimpleSuperReference ssr = new SimpleSuperReference(SourceInfo.NONE);
      _sd1.setSuperClass(_sd2);
      
      //normally, should work
      assertEquals("Should return _sd2", _sd2.getInstanceData(), ssr.visit(_etc));
      assertEquals("Should be no errors", 0, errors.size());
      
      //if within static method, add error
      MethodData sm = new MethodData("staticMethod", new VariableData[0]);
      sm.setMav(_publicStaticMav);
      sm.setOuterData(_etc._data);
      _etc._data = sm;
      
      assertEquals("Should return _sd2", _sd2.getInstanceData(), ssr.visit(_etc));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "'super' cannot be referenced from within a static method", 
                   errors.getLast().getFirst());
    }
    
    
    public void testForComplexSuperReference() {
      ComplexSuperReference csr = 
        new ComplexSuperReference(SourceInfo.NONE, 
                                  new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "context")));
      
      // if enclosing_result is null, return null
      assertEquals("Should return null", null, _etc.forComplexSuperReferenceOnly(csr, null));
      assertEquals("Should be no errors", 0, errors.size());

      // if enclosing result is a PackageData, give appropriate error and return null
      assertEquals("Should return null", null, _etc.forComplexSuperReferenceOnly(csr, new PackageData("context")));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct","Could not resolve symbol context" , errors.getLast().getFirst());
      
      // if enclosing_result is not an outer data of the current context, give an error
      SymbolData contextClass = new SymbolData("context");
      contextClass.setIsContinuation(false);
      contextClass.setMav(_publicMav);
      contextClass.setSuperClass(_sd2);
      assertEquals("Should return instance of super", _sd2.getInstanceData(), 
                   _etc.forComplexSuperReferenceOnly(csr, contextClass));
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("The error message should be correct", 
                   "You cannot reference context.super from here, because context is not an outer class of i.like.monkey", 
                   errors.getLast().getFirst());
      
      
      // if enclosing_result is an outer data of current context, everything is peachy
      _etc._data.setOuterData(contextClass);
      contextClass.addInnerClass(_etc._data.getSymbolData());
      assertEquals("Should return instance of super", _sd2.getInstanceData(), 
                   _etc.forComplexSuperReferenceOnly(csr, contextClass));
      assertEquals("Should still be 2 errors", 2, errors.size());
      
      // if we are in a static method, throw appropriate error
      MethodData sm = new MethodData("staticMethod", new VariableData[0]);
      sm.setMav(_publicStaticMav);
      sm.setOuterData(_etc._data);
      _etc._data = sm;
      assertEquals("Should return instance of super", _sd2.getInstanceData(), 
                   _etc.forComplexSuperReferenceOnly(csr, contextClass));
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("The error message should be correct", "'super' cannot be referenced from within a static method", 
                   errors.getLast().getFirst());
      
      // if the enclosing result is an instance type, throw an error
      _etc._data = sm.getOuterData();
      assertEquals("Should return instance of super", _sd2.getInstanceData(), 
                   _etc.forComplexSuperReferenceOnly(csr, contextClass.getInstanceData()));
      assertEquals("Should be 4 errors", 4, errors.size());
      assertEquals("The error message should be correct", "'super' can only be referenced from a type name, "
                     + "but you have specified an instance of that type.", 
                   errors.getLast().getFirst());
      
      // if current context is static, give an error
      _etc._data.getSymbolData().addModifier("static");
      assertEquals("Should return instance of super", _sd2.getInstanceData(), 
                   _etc.forComplexSuperReferenceOnly(csr, contextClass));
      assertEquals("Should be 5 errors", 5, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot reference context.super from here, because i.like.monkey or one of its enclosing "
                     + "classes is static.  Thus, an enclosing instance of context does not exist", 
                   errors.getLast().getFirst());
    }
    
    public void testForArrayAccessOnly() {
      ArrayAccess aa = 
        new ArrayAccess(SourceInfo.NONE, NULL_LITERAL, NULL_LITERAL);
      
      Hashtable<SymbolData, LanguageLevelVisitor> testNewSDs = LanguageLevelConverter._newSDs;
      LanguageLevelVisitor testLLVisitor = 
        new LanguageLevelVisitor(_etc._file, 
                                 _etc._package,
                                 null, // enclosingClassName for top level traversal
                                 _etc._importedFiles, 
                                 _etc._importedPackages, 
                                 new HashSet<String>(), 
                                 new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                                 new LinkedList<Command>());
      
      //if lhs is an array, and index is an int instance, no errors
      ArrayData ad = new ArrayData(SymbolData.INT_TYPE, testLLVisitor, SourceInfo.NONE);             
      
      assertEquals("should return int", SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forArrayAccessOnly(aa, ad.getInstanceData(), SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("should return int", SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forArrayAccessOnly(aa, ad.getInstanceData(), SymbolData.CHAR_TYPE.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      //if either input is null, return null
      assertEquals("Should return null", null, _etc.forArrayAccessOnly(aa, null, SymbolData.INT_TYPE));
      assertEquals("Should return null", null, _etc.forArrayAccessOnly(aa, SymbolData.INT_TYPE, null));
      assertEquals("Should be no errors", 0, errors.size());
      
      //if lhs is a PackageData, give error and return null
      PackageData pd = new PackageData("bad_reference");
      assertEquals("Should return null", null, _etc.forArrayAccessOnly(aa, pd, SymbolData.INT_TYPE));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "Could not resolve symbol bad_reference", 
                   errors.getLast().getFirst());
      
      
      //if rhs is a PackageData, give an error and return null
      assertEquals("Should return null", null, _etc.forArrayAccessOnly(aa, SymbolData.INT_TYPE, pd));
      assertEquals("Should still be 1 error", 1, errors.size());  // Generated a duplicate error
      assertEquals("Error message should be correct", 
                   "Could not resolve symbol bad_reference", 
                   errors.getLast().getFirst());
      
      //if array type is not an instance data, give appropriate error:
      assertEquals("Should return int", 
                   SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forArrayAccessOnly(aa, ad, SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("Should now be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot access an array element of a type name.  Perhaps you meant to create " +
                   "a new instance of int[]", 
                   errors.get(1).getFirst());
      
      // if type is not an array data, give appropriate error:
      assertEquals("Should return char", SymbolData.CHAR_TYPE.getInstanceData(), 
                   _etc.forArrayAccessOnly(aa, SymbolData.CHAR_TYPE.getInstanceData(), 
                                           SymbolData.INT_TYPE.getInstanceData()));
      assertEquals("Should now be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", 
                   "The variable referred to by this array access is a char, not an array", 
                   errors.get(2).getFirst());
      
      // If the array index is not an instance type, give error
      assertEquals("should return int", SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forArrayAccessOnly(aa, ad.getInstanceData(), SymbolData.INT_TYPE));
      assertEquals("Should now be 4 errors", 4, errors.size());
      assertEquals("Error message should be correct", 
                   "You have used a type name in place of an array index.  Perhaps you meant to create " +
                   "a new instance of int", 
                   errors.get(3).getFirst());
      
      //If the array index is not an int, give error
      assertEquals("should return int", SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forArrayAccessOnly(aa, ad.getInstanceData(), SymbolData.DOUBLE_TYPE.getInstanceData()));
      assertEquals("Should now be 5 errors", 5, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot reference an array element with an index of type double.  Instead, you must use an int",
                   errors.get(4).getFirst());   
    }
    
    
    public void testLiterals() {
      StringLiteral sl = new StringLiteral(SourceInfo.NONE, "string literal!");
      IntegerLiteral il = new IntegerLiteral(SourceInfo.NONE, 4);
      LongLiteral ll = new LongLiteral(SourceInfo.NONE, 5);
      FloatLiteral fl = new FloatLiteral(SourceInfo.NONE, 1.2f);
      DoubleLiteral dl = new DoubleLiteral(SourceInfo.NONE, 4.2);
      CharLiteral cl = new CharLiteral(SourceInfo.NONE, 'c');
      BooleanLiteral bl = new BooleanLiteral(SourceInfo.NONE, true);
      ClassLiteral csl = 
        new ClassLiteral(SourceInfo.NONE, new ClassOrInterfaceType(SourceInfo.NONE, "monkey", new Type[0]));
      
      SymbolData string = new SymbolData("java.lang.String");
      string.setIsContinuation(false);
      string.setPackage("java.lang");
      string.setMav(_publicMav);
      SymbolData classD = new SymbolData("java.lang.Class");
      classD.setIsContinuation(false);
      classD.setPackage("java.lang");
      classD.setMav(_publicMav);
      
      symbolTable.put("java.lang.String", string);
      symbolTable.put("java.lang.Class", classD);
      
      assertEquals("Should return string", string.getInstanceData(), sl.visit(_etc));
      assertEquals("Should return int", SymbolData.INT_TYPE.getInstanceData(), il.visit(_etc));
      assertEquals("Should return long", SymbolData.LONG_TYPE.getInstanceData(), ll.visit(_etc));
      assertEquals("Should return float", SymbolData.FLOAT_TYPE.getInstanceData(), fl.visit(_etc));
      assertEquals("Should return double", SymbolData.DOUBLE_TYPE.getInstanceData(), dl.visit(_etc));
      assertEquals("Should return char", SymbolData.CHAR_TYPE.getInstanceData(), cl.visit(_etc));
      assertEquals("Should return boolean", SymbolData.BOOLEAN_TYPE.getInstanceData(), bl.visit(_etc));
      assertEquals("Should return null type", SymbolData.NULL_TYPE.getInstanceData(), NULL_LITERAL.visit(_etc));
      assertEquals("Should return class", classD.getInstanceData(), _etc.forClassLiteralOnly(csl));
    }
    
    
    public void testForParenthesizedOnly() {
      Parenthesized p = new Parenthesized(SourceInfo.NONE, NULL_LITERAL);
      
      // if valueRes is an intance data, no problems
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.forParenthesizedOnly(p, SymbolData.BOOLEAN_TYPE.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      // if valueRes null, just return null
      assertEquals("Should return null", null, _etc.forParenthesizedOnly(p, null));
      assertEquals("Should be no errors", 0, errors.size());
      
      // if valueRes is package data, add error
      assertEquals("Should return null", null, _etc.forParenthesizedOnly(p, new PackageData("bob")));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct","Could not resolve symbol bob" , errors.getLast().getFirst());

      // if value result not instance type, give error
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), 
                   _etc.forParenthesizedOnly(p, SymbolData.INT_TYPE));
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct",
                   "This class or interface name cannot appear in parentheses.  Perhaps you meant to create a new "
                     + "instance of int" , 
                   errors.getLast().getFirst());
      
      
    }
    
    public void testMethodInvocationHelper() {
      ParenthesizedExpressionList exp1 = new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]);
      MethodInvocation noArgs = new SimpleMethodInvocation(SourceInfo.NONE, new Word(SourceInfo.NONE, "myName"), exp1);
      ParenthesizedExpressionList exp2 = 
        new ParenthesizedExpressionList(SourceInfo.NONE, 
                                        new Expression[]{new SimpleNameReference(SourceInfo.NONE, 
                                                                                 new Word(SourceInfo.NONE, "int"))});
      MethodInvocation typeArg = new SimpleMethodInvocation(SourceInfo.NONE, new Word(SourceInfo.NONE, "myName"), exp2);
      ParenthesizedExpressionList exp3 =
        new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[]{new IntegerLiteral(SourceInfo.NONE, 5)});
      MethodInvocation oneIntArg =
        new SimpleMethodInvocation(SourceInfo.NONE, new Word(SourceInfo.NONE, "myName"), exp3);
      ParenthesizedExpressionList exp4 =
        new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[]{new DoubleLiteral(SourceInfo.NONE, 4.2)});
      MethodInvocation oneDoubleArg = 
        new SimpleMethodInvocation(SourceInfo.NONE, new Word(SourceInfo.NONE, "myName"), exp4);
      
      //should be able to match no args correctly
      MethodData noArgsM = 
        new MethodData("myName", _publicMav, new TypeParameter[0], SymbolData.BOOLEAN_TYPE, new VariableData[0], 
                       new String[0], _sd2, NULL_LITERAL);
      _sd2.addMethod(noArgsM);
      assertEquals("Should return boolean instance", 
                   SymbolData.BOOLEAN_TYPE.getInstanceData(), 
                   _etc.methodInvocationHelper(noArgs, _sd2.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      //if no matching method, give error
      assertEquals("Should return null", null, _etc.methodInvocationHelper(oneIntArg, _sd2.getInstanceData()));
      assertEquals("Should be one error", 1, errors.size());
      assertEquals("Error message should be correct", "No method found in class " + _sd2.getName() 
                     + " with signature: myName(int).", 
                   errors.getLast().getFirst());
      
      // if matching method, but arg is not instance type, give error
      MethodData intArg = 
        new MethodData("myName", _publicMav, new TypeParameter[0], SymbolData.LONG_TYPE, 
                       new VariableData[] {new VariableData(SymbolData.INT_TYPE)}, new String[0], _sd2, NULL_LITERAL);
      _sd2.addMethod(intArg);
      assertEquals("Should return long instance", SymbolData.LONG_TYPE.getInstanceData(), _etc.methodInvocationHelper(typeArg, _sd2.getInstanceData()));
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", "Cannot pass a class or interface name as an argument to a method.  Perhaps you meant to create an instance or use int.class", errors.getLast().getFirst());
      
      // if matching method, no error
      assertEquals("Should return long instance", SymbolData.LONG_TYPE.getInstanceData(), 
                   _etc.methodInvocationHelper(oneIntArg, _sd2.getInstanceData()));
      assertEquals("Should still be 2 errors", 2, errors.size());
      
      // non-static method from static context gives error
      assertEquals("Should return long instance", SymbolData.LONG_TYPE.getInstanceData(), 
                   _etc.methodInvocationHelper(oneIntArg, _sd2));
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", 
                   "Cannot access the non-static method myName from a static context", 
                   errors.getLast().getFirst());
      
      
      //static method from static context is okay
      MethodData doubleArg = 
        new MethodData("myName", _publicStaticMav, new TypeParameter[0], SymbolData.CHAR_TYPE, 
                       new VariableData[] {new VariableData(SymbolData.DOUBLE_TYPE)}, new String[0], _sd2, NULL_LITERAL);
      _sd2.addMethod(doubleArg);
      assertEquals("Should return char instance", SymbolData.CHAR_TYPE.getInstanceData(), 
                   _etc.methodInvocationHelper(oneDoubleArg, _sd2));
      assertEquals("Should still be 3 errors", 3, errors.size());
    }
    
    public void testForSimpleMethodInvocation() {
      ParenthesizedExpressionList pel1 = new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]);
      MethodInvocation noArgs = new SimpleMethodInvocation(SourceInfo.NONE, new Word(SourceInfo.NONE, "myName"), pel1);
      ParenthesizedExpressionList pel2 = 
        new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[]{new IntegerLiteral(SourceInfo.NONE, 5)});
      MethodInvocation oneIntArg = 
        new SimpleMethodInvocation(SourceInfo.NONE, new Word(SourceInfo.NONE, "myName"), pel2);
      ParenthesizedExpressionList pel3 = 
        new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[]{new DoubleLiteral(SourceInfo.NONE, 4.2)});
      MethodInvocation oneDoubleArg = 
        new SimpleMethodInvocation(SourceInfo.NONE, new Word(SourceInfo.NONE, "myName"), pel3);
      
      // if method not in class, give error
      assertEquals("Should return null", null, noArgs.visit(_etc));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "No method found in class i.like.monkey with signature: myName().", 
                   errors.getLast().getFirst());
      
      //if method is in class, should work fine!
      MethodData noArgsM = 
        new MethodData("myName", _publicMav, new TypeParameter[0], SymbolData.BOOLEAN_TYPE, 
                       new VariableData[0], new String[0], _sd1, NULL_LITERAL);
      _sd1.addMethod(noArgsM);
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), noArgs.visit(_etc));
      assertEquals("Should still just be 1 error", 1, errors.size());
      
      //should be able to reference a static method from instance context
      MethodData doubleArg = 
        new MethodData("myName", _publicStaticMav, new TypeParameter[0], SymbolData.CHAR_TYPE, 
                       new VariableData[] {new VariableData(SymbolData.DOUBLE_TYPE)}, new String[0], _sd1, NULL_LITERAL);
      _sd1.addMethod(doubleArg);
      
      assertEquals("Should return char instance", SymbolData.CHAR_TYPE.getInstanceData(), oneDoubleArg.visit(_etc));
      assertEquals("Should still be just 1 error", 1, errors.size());
      
      //if in context of a static method, should be able to reference static method               
      _etc._data = doubleArg;
      assertEquals("Should return char instance", SymbolData.CHAR_TYPE.getInstanceData(), oneDoubleArg.visit(_etc));
      assertEquals("Should still be just 1 error", 1, errors.size());
      
      //if in context of static method, should not be able to reference non-static method
      MethodData intArg = 
        new MethodData("myName", _publicMav, new TypeParameter[0], SymbolData.LONG_TYPE, 
                       new VariableData[] {new VariableData(SymbolData.INT_TYPE)}, new String[0], _sd1, NULL_LITERAL);
      _sd1.addMethod(intArg);
      assertEquals("Should return long instance", SymbolData.LONG_TYPE.getInstanceData().getName(), 
                   oneIntArg.visit(_etc).getName());
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", "Cannot access the non-static method myName from a static context", 
                   errors.getLast().getFirst());
      
    }
    
    
    public void testForComplexMethodInvocation() {
      MethodInvocation staticNoArgs = 
        new ComplexMethodInvocation(SourceInfo.NONE, 
                                    new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "giraffe")),
                                    new Word(SourceInfo.NONE, "myName"), 
                                    new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]));
      MethodInvocation noArgs = 
        new ComplexMethodInvocation(SourceInfo.NONE, 
                                    new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "g")), 
                                    new Word(SourceInfo.NONE, "myName"), 
                                    new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]));
      MethodInvocation oneIntArg = 
        new ComplexMethodInvocation(SourceInfo.NONE, 
                                    new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "g")), 
                                    new Word(SourceInfo.NONE, "myName"), 
                                    new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[] { 
        new IntegerLiteral(SourceInfo.NONE, 5)}));
      MethodInvocation staticOneDoubleArg = 
        new ComplexMethodInvocation(SourceInfo.NONE, 
                                    new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "giraffe")),
                                    new Word(SourceInfo.NONE, "myName"), 
                                    new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[] {
        new DoubleLiteral(SourceInfo.NONE, 4.2)}));
      MethodInvocation oneDoubleArg = 
        new ComplexMethodInvocation(SourceInfo.NONE, 
                                    new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "g")), 
                                    new Word(SourceInfo.NONE, "myName"), 
                                    new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[] {
        new DoubleLiteral(SourceInfo.NONE, 4.2)}));
      
      SymbolData g = new SymbolData("giraffe");
      g.setIsContinuation(false);
      g.setMav(_publicMav);
      symbolTable.put("giraffe", g);
      
      VariableData var = new VariableData("g", _publicMav, g, true, _sd1);
      _etc._vars.addLast(var);
      
      //if method not in class, give error
      assertEquals("Should return null", null, noArgs.visit(_etc));
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "No method found in class giraffe with signature: myName().", 
                   errors.getLast().getFirst());
      
      // if method is in class, should work fine!
      MethodData noArgsM = new MethodData("myName", _publicMav, new TypeParameter[0], SymbolData.BOOLEAN_TYPE, 
                                          new VariableData[0], new String[0], g, NULL_LITERAL);
      g.addMethod(noArgsM);
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), noArgs.visit(_etc));
      assertEquals("Should still just be 1 error", 1, errors.size());
      
      // should be able to reference a static method from instance context
      MethodData doubleArg = 
        new MethodData("myName", _publicStaticMav, new TypeParameter[0], SymbolData.CHAR_TYPE, 
                       new VariableData[] { new VariableData(SymbolData.DOUBLE_TYPE) }, 
                       new String[0], g, NULL_LITERAL);
      g.addMethod(doubleArg);
      
      assertEquals("Should return char instance", SymbolData.CHAR_TYPE.getInstanceData(), oneDoubleArg.visit(_etc));
      assertEquals("Should still be just 1 error", 1, errors.size());
      
      // should be able to reference a static method from static context
      staticOneDoubleArg.visit(_etc);
      assertEquals("Should return char instance", SymbolData.CHAR_TYPE.getInstanceData(), 
                   staticOneDoubleArg.visit(_etc));
      assertEquals("Should still be just 1 error", 1, errors.size());
      
      // should not be able to reference a non-static method from a static context
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(),
                   staticNoArgs.visit(_etc));
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "Cannot access the non-static method myName from a static context", 
                   errors.getLast().getFirst());
      
      //if in context of a static method, should be able to reference static method               
      _etc._data = doubleArg;
      var.setMav(_publicStaticMav);
      assertEquals("Should return char instance", SymbolData.CHAR_TYPE.getInstanceData(), oneDoubleArg.visit(_etc));
      assertEquals("Should still be just 2 errors", 2, errors.size());
      
      // if in context of static method, should be able to reference non-static method given a receiver
      MethodData intArg = 
        new MethodData("myName", _publicMav, new TypeParameter[0], SymbolData.LONG_TYPE, 
                       new VariableData[] { new VariableData(SymbolData.INT_TYPE)}, 
                       new String[0], g, NULL_LITERAL);
      g.addMethod(intArg);
      assertEquals("Should return long instance", SymbolData.LONG_TYPE.getInstanceData().getName(), 
                   oneIntArg.visit(_etc).getName());
      assertEquals("Should be 2 errors", 2, errors.size());
//      assertEquals("Error message should be correct", "Cannot access the non-static method myName from a static context", 
//      errors.getLast().getFirst());
      
      // if enclosing class is private, should not work!
      _etc._data = _sd1;
      g.setMav(_privateMav);
      noArgsM.setMav(_publicStaticMav);
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), noArgs.visit(_etc));
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", 
                   "The class or interface giraffe in giraffe is private and cannot be accessed from i.like.monkey", 
                   errors.getLast().getFirst());
      
    }
    
    
    public void testCanBeAssigned() {
      VariableData finalWithValue = new VariableData("i", _finalMav, SymbolData.INT_TYPE, true, _sd1);
      VariableData finalWithOutValue = new VariableData("i", _finalMav, SymbolData.INT_TYPE, false, _sd1);
      VariableData notFinalWithValue = new VariableData("i", _publicMav, SymbolData.INT_TYPE, true, _sd1);
      VariableData notFinalWithOutValue = new VariableData("i", _publicMav, SymbolData.INT_TYPE, false, _sd1);
      
      assertFalse("Should not be assignable", _etc.canBeAssigned(finalWithValue));
      assertTrue("Should be assignable", _etc.canBeAssigned(finalWithOutValue));
      assertTrue("Should be assignable", _etc.canBeAssigned(notFinalWithValue));
      assertTrue("Should be assignable", _etc.canBeAssigned(notFinalWithOutValue));
      
      
    }
    
    
    public void testForSimpleAssignment() {
      VariableData vd4 = new VariableData("Flanders", _publicMav, SymbolData.INT_TYPE, true, _sd4);
      VariableData vd5 = new VariableData("Ned", _publicMav, _sd4, true, _sd5);
      _sd5.addVar(vd5);
      _sd4.addVar(vd4);
      _etc._vars.add(vd5);
      _etc._data = _sd5;
      
      ComplexNameReference nf =
        new ComplexNameReference(SourceInfo.NONE, 
                                 new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "Ned")), 
                                 new Word(SourceInfo.NONE, "Flanders"));
      SimpleAssignmentExpression sa = 
        new SimpleAssignmentExpression(SourceInfo.NONE, nf, new IntegerLiteral(SourceInfo.NONE, 5));
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), sa.visit(_etc));
      assertEquals("Should be 0 errors", 0, errors.size());
      
      //if variable is final, with a value cannot be reassigned
      vd4.gotValue();
      vd4.setMav(_finalPublicMav);
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), sa.visit(_etc));
      assertEquals("Should now be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot assign a value to Flanders because it is immutable and has already been given a value",
                   errors.getLast().getFirst());
      
      // Test that an initialized value can be assigned to itself
      vd4.setMav(_publicMav);
      SimpleAssignmentExpression sa2 = new SimpleAssignmentExpression(SourceInfo.NONE, nf, nf);
      _sd4.setMav(_publicMav);
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), sa2.visit(_etc));
      assertEquals("There should be 1 error", 1, errors.size());
      
      // Test that an uninitialized value cannot be assigned to itself as an initialization
      vd4.lostValue();
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), sa2.visit(_etc));
      assertEquals("There should be 2 errors", 2, errors.size());
      assertEquals("The error message should be correct", 
                   "You cannot use Flanders here, because it may not have been given a value", 
                   errors.getLast().getFirst());
      
      // Test that a value cannot be assigned to a type
      SimpleAssignmentExpression sa3 = 
        new SimpleAssignmentExpression(SourceInfo.NONE, 
                                       new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "int")), 
                                       new IntegerLiteral(SourceInfo.NONE, 5));
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), sa3.visit(_etc));
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot assign a value to the type int.  Perhaps you meant to create a new instance of int", 
                   errors.getLast().getFirst());
      
      //Test that a type cannot be used on the rhs of an assignment
      SimpleAssignmentExpression sa4 = 
        new SimpleAssignmentExpression(SourceInfo.NONE, nf, 
                                       new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "int")));
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), sa4.visit(_etc));
      assertEquals("Should be 4 errors", 4, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot use the type name int on the right hand side of an assignment.  Perhaps you meant to "
                     + "create a new instance of int", 
                   errors.getLast().getFirst());
      
      //test that we can assign to an array element
      LanguageLevelVisitor llv = 
        new LanguageLevelVisitor(_etc._file, 
                                 _etc._package, 
                                 null, // enclosingClassName for top level traversal
                                 _etc._importedFiles, 
                                 _etc._importedPackages, 
                                 new HashSet<String>(), 
                                 new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                                 new LinkedList<Command>());
//      LanguageLevelConverter.symbolTable = llv.symbolTable = _etc.symbolTable;
//      LanguageLevelConverter._newSDs = new Hashtable<SymbolData, LanguageLevelVisitor>();
      ArrayData boolArray = new ArrayData(SymbolData.BOOLEAN_TYPE, llv, SourceInfo.NONE);
      boolArray.setIsContinuation(false);
      symbolTable.remove("boolean[]");
      symbolTable.put("boolean[]", boolArray);
      VariableData myArrayVD = new VariableData("myArray", _publicMav, boolArray, true, _etc._data);
      _etc._vars.addLast(myArrayVD);
      
      SimpleNameReference snr = new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "myArray"));
      SimpleAssignmentExpression sa5 = 
        new SimpleAssignmentExpression(SourceInfo.NONE, 
                                       new ArrayAccess(SourceInfo.NONE, snr, new IntegerLiteral(SourceInfo.NONE, 5)), 
                                       new BooleanLiteral(SourceInfo.NONE, true));
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), sa5.visit(_etc));
      assertEquals("Should still be 4 errors", 4, errors.size());
      
      
      
    }
    
    public void testForPlusAssignmentExpression() {
      VariableData vd4 = new VariableData("Flanders", _publicMav, SymbolData.INT_TYPE, true, _sd4);
      VariableData vd5 = new VariableData("Ned", _publicMav, _sd4, true, _sd5);
      _sd5.addVar(vd5);
      _sd4.addVar(vd4);
      _etc._vars.add(vd5);
      _etc._data = _sd5;
      
      // Plus Assignment with numbers:
      // test that other assignment operators work correctly
      ComplexNameReference nf = 
        new ComplexNameReference(SourceInfo.NONE, 
                                 new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "Ned")), 
                                 new Word(SourceInfo.NONE, "Flanders"));
      PlusAssignmentExpression pa = 
        new PlusAssignmentExpression(SourceInfo.NONE, nf, new IntegerLiteral(SourceInfo.NONE, 5));
      
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), pa.visit(_etc));
      assertEquals("Should be 0 errors", 0, errors.size());
      
      // if variable does not have value, cannot be plus assigned
      vd4.lostValue();
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), pa.visit(_etc));
      assertEquals("Should now be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot use Flanders here, because it may not have been given a value",
                   errors.get(0).getFirst());
      
      // if variable is final, with a value cannot be reassigned
      vd4.gotValue();
      vd4.setMav(_finalPublicMav);
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), pa.visit(_etc));
      assertEquals("Should now be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot assign a new value to Flanders because it is immutable and has already been given a value",
                   errors.get(1).getFirst());
      
      // Test that an initialized value can be assigned to itself
      vd4.setMav(_publicMav);
      _sd4.setMav(_publicMav);
      PlusAssignmentExpression pa2 = new PlusAssignmentExpression(SourceInfo.NONE, nf, nf);
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), pa2.visit(_etc));
      assertEquals("There should still be 2 errors", 2, errors.size());
      
      // Test that an uninitialized value cannot be assigned to itself as an initialization
      vd4.lostValue();
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), pa2.visit(_etc));
      assertEquals("There should still be 2 errors", 2, errors.size());  // Generated two duplicate messages.
      assertEquals("The first error message should be correct", 
                   "You cannot use Flanders here, because it may not have been given a value", 
                   errors.get(0).getFirst());
      
      //test Plus Assignment for String concatanation
      SymbolData stringSD = new SymbolData("java.lang.String");
      stringSD.setIsContinuation(false);
      stringSD.setPackage("java.lang");
      symbolTable.remove("java.lang.String");
      symbolTable.put("java.lang.String", stringSD);
      VariableData s = new VariableData("s", _publicMav, stringSD, true, _etc._data);
      _etc._vars.add(s);
      
      SimpleNameReference sRef = new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "s"));
      //test string concatenation, where the string is first.
      PlusAssignmentExpression pa3 = 
        new PlusAssignmentExpression(SourceInfo.NONE, sRef, new BooleanLiteral(SourceInfo.NONE, true));
      TypeData result =  pa3.visit(_etc);
      assertEquals("string concatenation with string at the front.  Should return String type", 
                   stringSD.getInstanceData(), 
                   pa3.visit(_etc));
      assertEquals("Should still be 2 errors", 2, errors.size());
      
      // when both sides are strings
      PlusAssignmentExpression pa4 = 
        new PlusAssignmentExpression(SourceInfo.NONE, sRef, new StringLiteral(SourceInfo.NONE, "cat"));
      assertEquals("string concatenation with string on both sides.  Should return String type", 
                   stringSD.getInstanceData(), pa4.visit(_etc));
      assertEquals("Should still be 2 errors", 2, errors.size());
      
      // when string is second
      vd4.gotValue();
      PlusAssignmentExpression pa5 = 
        new PlusAssignmentExpression(SourceInfo.NONE, nf, new StringLiteral(SourceInfo.NONE, "house "));
      assertEquals("string + concatenation with string at back.  Should give error", 
                   stringSD.getInstanceData(), 
                   pa5.visit(_etc));
      assertEquals("Should now be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", 
                   "The arguments to the Plus Assignment Operator (+=) must either include an instance of a String " + 
                   "or both be numbers.  You have specified arguments of type int and java.lang.String", 
                   errors.get(2).getFirst());
    }
    
    public void testForNumericAssignmentExpression() {
      VariableData vd4 = new VariableData("Flanders", _publicMav, SymbolData.INT_TYPE, true, _sd4);
      VariableData vd5 = new VariableData("Ned", _publicMav, _sd4, true, _sd5);
      _sd5.addVar(vd5);
      _sd4.addVar(vd4);
      _etc._vars.add(vd5);
      _etc._data = _sd5;
      
      // test that numeric assignment with good values on left and right works correctly
      ComplexNameReference nf = 
        new ComplexNameReference(SourceInfo.NONE, 
                                 new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "Ned")), 
                                 new Word(SourceInfo.NONE, "Flanders"));
      NumericAssignmentExpression na = 
        new MinusAssignmentExpression(SourceInfo.NONE, nf, new IntegerLiteral(SourceInfo.NONE, 5));
      
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), na.visit(_etc));
      assertEquals("Should be 0 errors", 0, errors.size());
      
      //if variable does not have value, cannot be plus assigned
      vd4.lostValue();
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), na.visit(_etc));
      assertEquals("Should now be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot use Flanders here, because it may not have been given a value",
                   errors.get(0).getFirst());
      
      //if variable is final, cannot be reassigned
      vd4.gotValue();
      vd4.setMav(_finalPublicMav);
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), na.visit(_etc));
      assertEquals("Should now be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot assign a new value to Flanders because it is immutable and has already been given a value",
                   errors.get(1).getFirst());
      
      // Test that an initialized value can be assigned to itself
      vd4.setMav(_publicMav);
      _sd4.setMav(_publicMav);
      NumericAssignmentExpression na2 = new ModAssignmentExpression(SourceInfo.NONE, nf, nf);
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), na2.visit(_etc));
      assertEquals("There should be 2 errors", 2, errors.size());
      
      // Test that an uninitialized value cannot be assigned to itself as an initialization
      vd4.lostValue();
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), na2.visit(_etc));
      assertEquals("There should still be 2 errors", 2, errors.size());  // Generated duplicate error message
      assertEquals("The new error message should be correct", 
                   "You cannot use Flanders here, because it may not have been given a value", 
                   errors.get(0).getFirst()); 
    }
    
    public void testForIncrementExpression() {
      VariableData vd4 = new VariableData("Flanders", _publicMav, SymbolData.INT_TYPE, true, _sd4);
      VariableData vd5 = new VariableData("Ned", _publicMav, _sd4, true, _sd5);
      _sd5.addVar(vd5);
      _sd4.addVar(vd4);
      _etc._vars.add(vd5);
      _etc._data = _sd5;
      
      //test that words with a pre-increment operator before only work if they already have a value and aren't final.
      ComplexNameReference nf = 
        new ComplexNameReference(SourceInfo.NONE, 
                                 new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "Ned")), 
                                 new Word(SourceInfo.NONE, "Flanders"));
      PositivePrefixIncrementExpression ppi = new PositivePrefixIncrementExpression(SourceInfo.NONE, nf);
      
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), ppi.visit(_etc));
      assertEquals("Should still be 0 errors", 0, errors.size());
      
      // test that attempting to increment the value of a field that doesn't have a value will throw an error
      vd4.lostValue();
      assertEquals("Should return int instance.", SymbolData.INT_TYPE.getInstanceData(), ppi.visit(_etc));
      assertEquals("Should now be 1 errors", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot use Flanders here, because it may not have been given a value",
                   errors.get(0).getFirst());
      
      // test that attempting to increment the value of a final field will throw an error
      vd4.gotValue();
      vd4.setMav(_finalPublicMav);
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), ppi.visit(_etc));
      assertEquals("Should now be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot assign a new value to Flanders because it is immutable and has already been given a value",
                   errors.get(1).getFirst());
      
      // Check that ++int doesn't work
      PositivePrefixIncrementExpression ppi2 = 
        new PositivePrefixIncrementExpression(SourceInfo.NONE, 
                                              new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "int")));
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), ppi2.visit(_etc));
      assertEquals("There should now be 3 errors", 3, errors.size());
      assertEquals("The error message should be correct", 
                   "You cannot increment or decrement int, because it is a class name not an instance.  " +
                   "Perhaps you meant to create a new instance of int", 
                   errors.get(2).getFirst());
      
      
      // Check that ++(int) doesn't work
      Parenthesized p1 = 
        new Parenthesized(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "int")));
      PositivePrefixIncrementExpression ppi3 = new PositivePrefixIncrementExpression(SourceInfo.NONE, p1);
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), ppi3.visit(_etc));
      assertEquals("There should now be 4 errors", 4, errors.size());  // Generated error is not a duplicate
      assertEquals("The error message should be correct", 
                   "You cannot increment or decrement int, because it is a class name not an instance.  " +
                   "Perhaps you meant to create a new instance of int",
                   errors.get(3).getFirst());
      
      
      // Test that words with post-decrement operator only work if they already have a value and aren't final.
      vd4.setMav(_publicMav);
      NegativePostfixIncrementExpression npi = new NegativePostfixIncrementExpression(SourceInfo.NONE, nf);
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), npi.visit(_etc));
      assertEquals("Should still be 4 errors", 4, errors.size());
      
      // Test that attempting to decrement the value of a field that doesn't have a value will throw an error
      vd4.lostValue();
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), npi.visit(_etc));
      assertEquals("Should still be 4 errors", 4, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot use Flanders here, because it may not have been given a value",
                   errors.get(0).getFirst());      
      
      // test that attempting to increment the value of a final field will throw an error
      vd4.gotValue();
      vd4.setMav(_finalPublicMav);
      assertEquals("Should return int instance.", SymbolData.INT_TYPE.getInstanceData(), npi.visit(_etc));
      assertEquals("Should still be 4 errors", 4, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot assign a new value to Flanders because it is immutable and has already been given a value",
                   errors.get(1).getFirst());
      
      
      // Check that int-- doesn't work
      NegativePostfixIncrementExpression npi2 = 
        new NegativePostfixIncrementExpression(SourceInfo.NONE, 
                                               new SimpleNameReference(SourceInfo.NONE, 
                                                                       new Word(SourceInfo.NONE, "int")));
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), npi2.visit(_etc));
      assertEquals("There should be 5 errors", 5, errors.size());
      assertEquals("The error message should be correct", 
                   "You cannot increment or decrement int, because it is a class name not an instance.  Perhaps you " + 
                   "meant to create a new instance of int", 
                   errors.get(4).getFirst());
      
      // Check that (int)-- doesn't work
      Parenthesized p2 = 
        new Parenthesized(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "int")));
      NegativePostfixIncrementExpression npi3 = new NegativePostfixIncrementExpression(SourceInfo.NONE, p2);
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), npi3.visit(_etc));
      assertEquals("There should be 6 errors", 6, errors.size());  // Wny isn't this a duplicate of error #4?
      assertEquals("The error message should be correct", 
                   "You cannot increment or decrement int, because it is a class name not an instance.  " + 
                   "Perhaps you meant to create a new instance of int", 
                   errors.get(5).getFirst());
      
      
      //should break: double increment/decrement ++(--Ned.Flanders)
      vd4.setMav(_publicMav);
      Parenthesized p3 = new Parenthesized(SourceInfo.NONE, new NegativePrefixIncrementExpression(SourceInfo.NONE, nf));
      PositivePrefixIncrementExpression ppi4 = new PositivePrefixIncrementExpression(SourceInfo.NONE, p3);
      assertEquals("Should return null", null, ppi4.visit(_etc));
      assertEquals("Should have added 1 error", 7, errors.size());
      assertEquals("Should have correct error message",
                   "You cannot assign a value to an increment expression", 
                   errors.getLast().getFirst());
      
//      //should break: non number being incremented
      VariableData s = new VariableData("s", _publicMav, SymbolData.BOOLEAN_TYPE, true, _etc._data);
      _etc._vars.addLast(s);
      PositivePrefixIncrementExpression ppi5 = 
        new PositivePrefixIncrementExpression(SourceInfo.NONE, 
                                              new SimpleNameReference(SourceInfo.NONE, 
                                                                      new Word(SourceInfo.NONE, "s")));
      assertEquals("Should return boolean instance", SymbolData.BOOLEAN_TYPE.getInstanceData(), ppi5.visit(_etc));
      assertEquals("Should have added 1 error", 8, errors.size());
      assertEquals("Should have correct error message", 
                   "You cannot increment or decrement something that is not a number type.  You have specified " +
                   "something of type boolean", errors.get(7).getFirst());
      
      //nested parentheses...should work
      PositivePrefixIncrementExpression ppi6 = 
        new PositivePrefixIncrementExpression(SourceInfo.NONE, new Parenthesized(SourceInfo.NONE, 
                                                                                 new Parenthesized(SourceInfo.NONE, nf)));
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), ppi6.visit(_etc));
      assertEquals("Should still be 8 errors", 8, errors.size());
    }
    
    
    public void testForSimpleAnonymousClassInstantiation() {
      ClassOrInterfaceType objType = new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]);
      AnonymousClassInstantiation basic = 
        new SimpleAnonymousClassInstantiation(SourceInfo.NONE, 
                                              objType, 
                                              new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]),
                                              new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      SymbolData object = LanguageLevelConverter.symbolTable.get("java.lang.Object");
      
      _sd1.setAnonymousInnerClassNum(0);
      
      // Once our enclosing data does have an anonymous inner class, it's okay to look it up
      SymbolData anon1 = new SymbolData("i.like.monkey$1");
      anon1.setIsContinuation(false);
      anon1.setPackage("i.like");
      anon1.setMav(_publicMav);
      anon1.setOuterData(_sd1);
      assert object != null;
      anon1.setSuperClass(object);
      _sd1.addInnerClass(anon1);
//      System.err.println("****** anon1 is: " + anon1);
//      System.err.println("****** instance data = " + anon1.getInstanceData());
      assertEquals("Should return anon1 instance", anon1.getInstanceData(), basic.visit(_etc));
      
      assertEquals("Should be no errors", 0, errors.size());
      
      VariableDeclaration vdecl = new VariableDeclaration(SourceInfo.NONE,
                                                          _packageMav,
                                                          new VariableDeclarator[] {
        new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                            new PrimitiveType(SourceInfo.NONE, "double"), 
                                            new Word (SourceInfo.NONE, "field1")),
          new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                              new PrimitiveType(SourceInfo.NONE, "boolean"), 
                                              new Word (SourceInfo.NONE, "field2"))});      
      
      PrimitiveType intt = new PrimitiveType(SourceInfo.NONE, "int");
      UninitializedVariableDeclarator uvd = 
        new UninitializedVariableDeclarator(SourceInfo.NONE, intt, new Word(SourceInfo.NONE, "i"));
      FormalParameter param = 
        new FormalParameter(SourceInfo.NONE, 
                            new UninitializedVariableDeclarator(SourceInfo.NONE, intt, 
                                                                new Word(SourceInfo.NONE, "j")), false);
      BracedBody bb = 
        new BracedBody(SourceInfo.NONE, 
                       new BodyItemI[] {new VariableDeclaration(SourceInfo.NONE,  _packageMav, 
                                                                new UninitializedVariableDeclarator[]{uvd}), 
                         new ValueReturnStatement(SourceInfo.NONE, new IntegerLiteral(SourceInfo.NONE, 5))});
      
      ConcreteMethodDef cmd1 = 
        new ConcreteMethodDef(SourceInfo.NONE, _publicMav, new TypeParameter[0], 
                              intt, new Word(SourceInfo.NONE, "myMethod"), new FormalParameter[] {param}, 
                              new ReferenceType[0], bb);
      BracedBody classBb = new BracedBody(SourceInfo.NONE, new BodyItemI[] { vdecl, cmd1 });
      
      SimpleAnonymousClassInstantiation  complicated = 
        new SimpleAnonymousClassInstantiation(SourceInfo.NONE, 
                                              new ClassOrInterfaceType(SourceInfo.NONE, "name", new Type[0]), 
                                              new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]), 
                                              classBb);
      SymbolData sd = new SymbolData("name");
      sd.setIsContinuation(false);
      sd.setMav(_publicMav);
      symbolTable.put("name", sd);
      SymbolData anon2 = new SymbolData("i.like.monkey$2");
      anon2.setIsContinuation(false);
      anon2.setPackage("i.like");
      anon2.setSuperClass(sd);
      anon2.setOuterData(_sd1);
      _sd1.addInnerClass(anon2);
      
      VariableData vd1 = new VariableData("field1", _publicMav, SymbolData.DOUBLE_TYPE, true, sd);
      VariableData vd2 = new VariableData("field2", _publicMav, SymbolData.DOUBLE_TYPE, true, sd);
      sd.addVar(vd1);
      sd.addVar(vd2);
      
      MethodData md = 
        new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, new VariableData[] {
        new VariableData("j", _finalMav, SymbolData.INT_TYPE, true, null)}, new String[0], sd, cmd1);
      md.getParams()[0].setEnclosingData(md);
      MethodData cd = 
        new MethodData("name", _publicMav, new TypeParameter[0], sd, new VariableData[0], new String[0], sd, cmd1);
      anon2.addMethod(md);
      sd.addMethod(cd);
      // check that this complex expression returns correct type, overwriting fields and method in super class
      assertEquals("Should return anon2.  ", anon2.getInstanceData(), complicated.visit(_etc));
      assertEquals("There should be no errors", 0, errors.size());
      
      _etc._data.addVar(new VariableData("myAnon", _publicMav, sd, false, _etc._data));
      
      
      // Test that I can assign the anonymous inner class to a variable of the right type.
      _sd1.setAnonymousInnerClassNum(1);
      symbolTable.put("int", SymbolData.INT_TYPE);
      VariableDeclaration vd =
        new VariableDeclaration(SourceInfo.NONE, _publicMav, new VariableDeclarator[] { 
        new InitializedVariableDeclarator(SourceInfo.NONE, 
                                          new ClassOrInterfaceType(SourceInfo.NONE, "name", new Type[0]), 
                                          new Word(SourceInfo.NONE, "myAnon"), complicated)});
      vd.visit(_etc);
      assertEquals("There should still be no errors", 0, errors.size());
      
      //Test that a method invoked from an anonymous inner class does its thing correctly
      _sd1.setAnonymousInnerClassNum(1);
      MethodInvocation mie = 
        new ComplexMethodInvocation(SourceInfo.NONE, 
                                    complicated, 
                                    new Word(SourceInfo.NONE, "myMethod"),
                                    new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[] { 
        new IntegerLiteral(SourceInfo.NONE, 5)}));
      assertEquals("Should return int", SymbolData.INT_TYPE.getInstanceData(), mie.visit(_etc));
      assertEquals("There should still be no errors", 0, errors.size());
      
      // Test that we can get a field from an anonymous inner class
      _sd1.setAnonymousInnerClassNum(1);
      
      Expression nr = new ComplexNameReference(SourceInfo.NONE, complicated, new Word(SourceInfo.NONE, "field1"));
      assertEquals("Should return double", SymbolData.DOUBLE_TYPE.getInstanceData(), nr.visit(_etc));
      assertEquals("There should be no errors...still!", 0, errors.size());
      
      // Let sd be abstract with an abstract method that our instantiation doesn't override.  Should throw an error.
      _sd1.setAnonymousInnerClassNum(1);
      sd.setMav(_publicAbstractMav);
      sd.addMethod(new MethodData("yeah", _abstractMav, new TypeParameter[0], SymbolData.BOOLEAN_TYPE, 
                                  new VariableData[0], new String[0], sd, cmd1));
      
      assertEquals("Should return anon2 instance", anon2.getInstanceData(), complicated.visit(_etc));
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "This anonymous inner class must override the abstract method: yeah() in name", 
                   errors.get(0).getFirst());
      
      //cannot use syntax new A.B() if B is not static.  Make sure appropriate error is thrown.
      SimpleAnonymousClassInstantiation nestedNonStatic = 
        new SimpleAnonymousClassInstantiation(SourceInfo.NONE, 
                                              new ClassOrInterfaceType(SourceInfo.NONE, "A.B", new Type[0]), 
                                              new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]), 
                                              new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      SymbolData a = new SymbolData("A");
      a.setIsContinuation(false);
      SymbolData b = new SymbolData("A$B");
      b.setIsContinuation(false);
      b.setOuterData(a);
      a.addInnerClass(b);
      MethodData consb = new MethodData("B", _publicMav, new TypeParameter[0], b, 
                                        new VariableData[0], 
                                        new String[0], 
                                        b,
                                        null);
      b.addMethod(consb);
      symbolTable.put("A", a);
      a.setMav(new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"}));
      b.setMav(new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"}));
      
      SymbolData anon3 = new SymbolData("i.like.monkey$3");
      anon3.setIsContinuation(false);
      anon3.setMav(_publicMav);
      _sd1.addInnerClass(anon3);
      anon3.setOuterData(_sd1);
      
      //if inner part is not static, give error
      assertEquals("Should return anon3", anon3.getInstanceData(), nestedNonStatic.visit(_etc));
      assertEquals("Should be 2 errors", 2, errors.size());
      
      assertEquals("Error message should be correct", 
                   "A.B is not a static inner class, and thus cannot be instantiated from this context.  "
                     + "Perhaps you meant to use an instantiation of the form new A().new B()",
                   errors.getLast().getFirst());
      
      _sd1.setAnonymousInnerClassNum(2);
      //if inner part is static, no problem
      b.addModifier("static");
      assertEquals("Should return anon3", anon3.getInstanceData(), nestedNonStatic.visit(_etc));
      assertEquals("Should still be just 2 errors", 2, errors.size());
      
    }
    
    public void testForComplexAnonymousClassInstantiation() {
      ClassOrInterfaceType objType = new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]);
      
      AnonymousClassInstantiation basic = 
        new ComplexAnonymousClassInstantiation(SourceInfo.NONE, 
                                               new SimpleNameReference(SourceInfo.NONE, 
                                                                       new Word(SourceInfo.NONE, "bob")),
                                               objType, 
                                               new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]),
                                               new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      // Create a variable 'bob' of type _sd2 within _sd1
      VariableData bob = new VariableData("bob", _publicMav, _sd2, true, _sd1);
      _etc._vars.add(bob);  // _data for _etc is _sd1
      
      SymbolData object = LanguageLevelConverter.symbolTable.get("java.lang.Object");
      _sd1.setAnonymousInnerClassNum(0);
      SymbolData anon1 = new SymbolData("i.like.monkey$1");
      anon1.setIsContinuation(false);
      anon1.setPackage("i.like");
      anon1.setMav(_publicMav);
      anon1.setOuterData(_sd1);
      assert object != null;
      anon1.setSuperClass(object);
      _sd1.addInnerClass(anon1);
      assertEquals("Should return anon1 instance", anon1.getInstanceData(), basic.visit(_etc));
      
      assertEquals("Should be no errors", 0, errors.size());
      
      
      VariableDeclaration vdecl = new VariableDeclaration(SourceInfo.NONE,
                                                          _packageMav,
                                                          new VariableDeclarator[] {
        new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                            new PrimitiveType(SourceInfo.NONE, "double"), 
                                            new Word (SourceInfo.NONE, "field1")),
          new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                              new PrimitiveType(SourceInfo.NONE, "boolean"), 
                                              new Word (SourceInfo.NONE, "field2"))});      
      
      PrimitiveType intt = new PrimitiveType(SourceInfo.NONE, "int");
      UninitializedVariableDeclarator uvd = 
        new UninitializedVariableDeclarator(SourceInfo.NONE, intt, new Word(SourceInfo.NONE, "i"));
      FormalParameter param = 
        new FormalParameter(SourceInfo.NONE, 
                            new UninitializedVariableDeclarator(SourceInfo.NONE, intt, 
                                                                new Word(SourceInfo.NONE, "j")), false);
      BracedBody bb = 
        new BracedBody(SourceInfo.NONE, new BodyItemI[] {
        new VariableDeclaration(SourceInfo.NONE, _packageMav, new UninitializedVariableDeclarator[]{uvd}), 
          new ValueReturnStatement(SourceInfo.NONE, new IntegerLiteral(SourceInfo.NONE, 5))});
      
      ConcreteMethodDef cmd1 = 
        new ConcreteMethodDef(SourceInfo.NONE, _publicMav, new TypeParameter[0], 
                              intt, new Word(SourceInfo.NONE, "myMethod"), new FormalParameter[] {param}, 
                              new ReferenceType[0], bb);
      BracedBody classBb = new BracedBody(SourceInfo.NONE, new BodyItemI[] { vdecl, cmd1 });
      
      ComplexAnonymousClassInstantiation  complicated = 
        new ComplexAnonymousClassInstantiation(SourceInfo.NONE, 
                                               new SimpleNameReference(SourceInfo.NONE, 
                                                                       new Word(SourceInfo.NONE, "bob")),
                                               new ClassOrInterfaceType(SourceInfo.NONE, "name", new Type[0]), 
                                               new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]), 
                                               classBb);
      
      // This test is not well-documented.  In refactoring, I tried to preserve it as best as possible.
      SymbolData sd = new SymbolData("name");
      sd.setIsContinuation(false);
      sd.setMav(_publicMav);
      sd.setSuperClass(object);
      symbolTable.put("name", sd);
      SymbolData anon2 = new SymbolData("i.like.monkey$2");
      anon2.setIsContinuation(false);
      anon2.setPackage("i.like");
      anon2.setSuperClass(sd);
      anon2.setOuterData(_sd1);
      _sd1.addInnerClass(anon2);
      
      VariableData vd1 = new VariableData("field1", _publicMav, SymbolData.DOUBLE_TYPE, true, sd);
      VariableData vd2 = new VariableData("field2", _publicMav, SymbolData.DOUBLE_TYPE, true, sd);
      sd.addVar(vd1);
      sd.addVar(vd2);
      
      MethodData md = 
        new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, new VariableData[] {
        new VariableData("j", _finalMav, SymbolData.INT_TYPE, true, null)}, new String[0], sd, cmd1);
      md.getParams()[0].setEnclosingData(md);
      MethodData cd = 
        new MethodData("name", _publicMav, new TypeParameter[0], sd, new VariableData[0], new String[0], sd, cmd1);
      anon2.addMethod(md);
      sd.addMethod(cd);
      // check that this complex expression returns correct type, overwriting fields in super class and method in 
      // super class
      assertEquals("Should return anon2.  ", anon2.getInstanceData(), complicated.visit(_etc));
      assertEquals("There should be no errors", 0, errors.size());
      
      _etc._data.addVar(new VariableData("myAnon", _publicMav, sd, false, _etc._data));
      
      
      // Test that I can assign the anonymous inner class to a variable of the right type.
      _sd1.setAnonymousInnerClassNum(1);
      symbolTable.put("int", SymbolData.INT_TYPE);
      VariableDeclaration vd = 
        new VariableDeclaration(SourceInfo.NONE, _publicMav, new VariableDeclarator[] { 
        new InitializedVariableDeclarator(SourceInfo.NONE, 
                                          new ClassOrInterfaceType(SourceInfo.NONE, "name", new Type[0]),
                                          new Word(SourceInfo.NONE, "myAnon"), 
                                          complicated)});
      vd.visit(_etc);
      assertEquals("There should still be no errors", 0, errors.size());
      
      //Test that a method invoked from an anonymous inner class does its thing correctly
      _sd1.setAnonymousInnerClassNum(1);
      MethodInvocation mie = 
        new ComplexMethodInvocation(SourceInfo.NONE, complicated, 
                                    new Word(SourceInfo.NONE, "myMethod"),
                                    new ParenthesizedExpressionList(SourceInfo.NONE, 
                                                                    new Expression[] { 
        new IntegerLiteral(SourceInfo.NONE, 5)}));
      assertEquals("Should return int", SymbolData.INT_TYPE.getInstanceData(), mie.visit(_etc));
      assertEquals("There should still be no errors", 0, errors.size());
      
//      //Test that we can get a field from an anonymous inner class
      _sd1.setAnonymousInnerClassNum(1);
      
      Expression nr = new ComplexNameReference(SourceInfo.NONE, complicated, new Word(SourceInfo.NONE, "field1"));
      assertEquals("Should return double", SymbolData.DOUBLE_TYPE.getInstanceData(), nr.visit(_etc));
      assertEquals("There should be no errors...still!", 0, errors.size());
      
      // If the implemented sd is abstract and it isn't overriden, type-checking should throw an error.
      _sd1.setAnonymousInnerClassNum(1);
      sd.setMav(_publicAbstractMav);
      sd.addMethod(new MethodData("yeah", _abstractMav, new TypeParameter[0], SymbolData.BOOLEAN_TYPE, 
                                  new VariableData[0], new String[0], sd, cmd1));
      
      assertEquals("Should return anon2 instance", anon2.getInstanceData(), complicated.visit(_etc));
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "This anonymous inner class must override the abstract method: yeah() in name", 
                   errors.get(0).getFirst());
      
//      //cannot use syntax a.new B() if B is static.  Make sure appropriate error is thrown.
      ComplexAnonymousClassInstantiation nestedNonStatic = 
        new ComplexAnonymousClassInstantiation(SourceInfo.NONE, 
                                               new SimpleNameReference(SourceInfo.NONE, 
                                                                       new Word(SourceInfo.NONE, "a")),
                                               new ClassOrInterfaceType(SourceInfo.NONE, "B", new Type[0]), 
                                               new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]), 
                                               new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      SymbolData a = new SymbolData("A");
      a.setIsContinuation(false);
      SymbolData b = new SymbolData("A$B");
      b.setIsContinuation(false);
      b.setOuterData(a);
      a.addInnerClass(b);
      MethodData consb = 
        new MethodData("B", _publicMav, new TypeParameter[0], b, new VariableData[0], new String[0], b, null);
      b.addMethod(consb);
      symbolTable.put("A", a);
      a.setMav(new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"}));
      b.setMav(new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"}));
      
      SymbolData anon3 = new SymbolData("i.like.monkey$3");
      anon3.setIsContinuation(false);
      anon3.setMav(_publicMav);
      _sd1.addInnerClass(anon3);
      anon3.setOuterData(_sd1);
      VariableData aVar = new VariableData("a", _publicMav, a, true, _sd1);
      _etc._vars.add(aVar);
      
      //if inner part is not static, no problem
      assertEquals("Should return anon3", anon3.getInstanceData(), nestedNonStatic.visit(_etc));
      assertEquals("Should still be just 1 error", 1, errors.size());      
      
      //if outer part is private, should break
      _sd1.setAnonymousInnerClassNum(2);
      a.setMav(_privateMav);
      assertEquals("Should return anon3", anon3.getInstanceData(), nestedNonStatic.visit(_etc));
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "The class or interface A in A is private and cannot be accessed from i.like.monkey", 
                   errors.getLast().getFirst());
      a.setMav(_publicMav);
      
      //if inner part is static, give error
      _sd1.setAnonymousInnerClassNum(2);
      b.setMav(_publicStaticMav);
      assertEquals("Should return anon3", anon3.getInstanceData(), nestedNonStatic.visit(_etc));
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot instantiate a static inner class or interface with this syntax.  Instead, try new A.B()", 
                   errors.getLast().getFirst());
      
      // if inner part is not static, but outer part is type name, give error
      ComplexAnonymousClassInstantiation nested = 
        new ComplexAnonymousClassInstantiation(SourceInfo.NONE, 
                                               new SimpleNameReference(SourceInfo.NONE, 
                                                                       new Word(SourceInfo.NONE, "A")),
                                               new ClassOrInterfaceType(SourceInfo.NONE, "B", new Type[0]), 
                                               new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]), 
                                               new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      _sd1.setAnonymousInnerClassNum(2);
      b.setMav(_publicMav);
      assertEquals("Should return anon3", anon3.getInstanceData(), nested.visit(_etc));
      assertEquals("Should be 4 errors", 4, errors.size());
      assertEquals("Error message should be correct", 
                   "The constructor of a non-static inner class can only be called on an instance of its "
                     + "containing class (e.g. new A().new B())", 
                   errors.getLast().getFirst());
      
    }
  }
}
