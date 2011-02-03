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

import junit.framework.TestCase;

import edu.rice.cs.plt.reflect.JavaVersion;

/** Represents the data for a given class.  There are two states of SymbolData.  One is a continuation which
  * is created when a type is referenced, but the corresponding class has not been read for its members.  The
  * other is a complete SymbolData containing all of the member data.
  */
public class SymbolData extends TypeData {
  
  /***************Singleton primitive SymbolDatas********************/
  
  /** This anonymous class represents the boolean primitive type */
  public static final SymbolData BOOLEAN_TYPE = new PrimitiveData("boolean") {
    
    /** You can only cast a boolean primitive to a boolean primitive or a Boolean object (in 1.5). */
    public boolean isCastableTo(SymbolData castTo, JavaVersion version) {
      return isAssignableTo(castTo, version);
    }
    
    /** Returns true if the specified SymbolData is a boolean type. */
    public boolean isAssignableTo(SymbolData toCheck, JavaVersion version) {
      if (toCheck == null) return false;
      if (LanguageLevelConverter.versionSupportsAutoboxing(version) && !toCheck.isPrimitiveType()) {
        SymbolData autoBoxMe = LanguageLevelConverter.symbolTable.get("java.lang.Boolean");
        return (autoBoxMe != null && autoBoxMe.isAssignableTo(toCheck, version));
      }
      return toCheck == BOOLEAN_TYPE;
    }
  };
  
  /** This anonymous class represents the char primitive type. */
  public static final SymbolData CHAR_TYPE = new PrimitiveData("char") {
    
    /** You can cast a char to a char, int, long, float, double or short or byte or Character object (in 1.5) */
    public boolean isCastableTo(SymbolData castTo, JavaVersion version) {
      return isAssignableTo(castTo, version) || castTo == SymbolData.SHORT_TYPE || castTo == SymbolData.BYTE_TYPE;
    }

    /** You can assign a char primitive to a char, int, long, float, double or Character object (in 1.5) */
    public boolean isAssignableTo(SymbolData assignTo, JavaVersion version) {
      if (assignTo == null) return false;
      if (LanguageLevelConverter.versionSupportsAutoboxing(version) && !assignTo.isPrimitiveType()) {
        SymbolData autoBoxMe = LanguageLevelConverter.symbolTable.get("java.lang.Character");
        return autoBoxMe != null && autoBoxMe.isAssignableTo(assignTo, version);
      }

      return assignTo == SymbolData.INT_TYPE || 
        assignTo == SymbolData.LONG_TYPE || 
        assignTo == SymbolData.FLOAT_TYPE || 
        assignTo == SymbolData.DOUBLE_TYPE || 
        assignTo == SymbolData.CHAR_TYPE;
    }
  };
  
  /** This anonymous class represents the byte primitive type */
  public static final SymbolData BYTE_TYPE = new PrimitiveData("byte") {
    
    /** You can cast a byte primitive to a char, int, long, float, double or short or byte or Byte object (in 1.5) */
    public boolean isCastableTo(SymbolData castTo, JavaVersion version) {
      return isAssignableTo(castTo, version) || castTo == SymbolData.CHAR_TYPE;
    }

    /** You can assign a byte primitive to a byte, short, int, long, float, double, or Byte object(in 1.5). */
    public boolean isAssignableTo(SymbolData assignTo, JavaVersion version) {
      if (assignTo == null) return false;

      if (LanguageLevelConverter.versionSupportsAutoboxing(version) && !assignTo.isPrimitiveType()) {
        SymbolData autoBoxMe = LanguageLevelConverter.symbolTable.get("java.lang.Byte");
        return autoBoxMe != null && autoBoxMe.isAssignableTo(assignTo, version);
      }

      return assignTo == SymbolData.BYTE_TYPE || 
        assignTo == SymbolData.SHORT_TYPE || 
        assignTo == SymbolData.INT_TYPE || 
        assignTo == SymbolData.LONG_TYPE || 
        assignTo == SymbolData.FLOAT_TYPE || 
        assignTo == SymbolData.DOUBLE_TYPE;
    }
  };

  /** This anonymous class represents the short primitive type. */
  public static final SymbolData SHORT_TYPE = new PrimitiveData("short") {
    
    /** You can cast a short primitive to a char, int, long, float, double or short or byte or Short object (in 1.5) */
    public boolean isCastableTo(SymbolData castTo, JavaVersion version) {
      return isAssignableTo(castTo, version) || castTo == SymbolData.CHAR_TYPE || castTo == SymbolData.BYTE_TYPE;
    }

    /** You can assign a short primitive to a short, int, long, float, double, or Short object(in 1.5) */
    public boolean isAssignableTo(SymbolData assignTo, JavaVersion version) {
      if (assignTo == null) return false;

      if (LanguageLevelConverter.versionSupportsAutoboxing(version) && !assignTo.isPrimitiveType()) {
        SymbolData autoBoxMe = LanguageLevelConverter.symbolTable.get("java.lang.Short");
        return autoBoxMe != null && autoBoxMe.isAssignableTo(assignTo, version);
      }
      
      return assignTo==SymbolData.SHORT_TYPE || 
        assignTo == SymbolData.INT_TYPE || 
        assignTo == SymbolData.LONG_TYPE || 
        assignTo == SymbolData.FLOAT_TYPE || 
        assignTo == SymbolData.DOUBLE_TYPE; 
    }
  };
  
  /** This anonymous class represents the int primitive type. */
  public static final SymbolData INT_TYPE = new PrimitiveData("int") {
    
    /** You can cast a int primitive to a char, int, long, float, double or short or byte or Short object (in 1.5) */
    public boolean isCastableTo(SymbolData castTo, JavaVersion version) {
      return isAssignableTo(castTo, version) || castTo == SymbolData.CHAR_TYPE || castTo == SymbolData.SHORT_TYPE 
        || castTo == SymbolData.BYTE_TYPE;
    }

    /** You can assign a int primitive to a int, long, float, double, or Integer/Long/Float/Double or
      * Number/Object type (in 1.5) */
    public boolean isAssignableTo(SymbolData assignTo, JavaVersion version) {
      if (assignTo == null) return false;

      if (LanguageLevelConverter.versionSupportsAutoboxing(version) && !assignTo.isPrimitiveType()) {
        SymbolData autoBoxMe = LanguageLevelConverter.symbolTable.get("java.lang.Integer");
        return autoBoxMe != null && autoBoxMe.isAssignableTo(assignTo, version);
      }

      return assignTo == SymbolData.INT_TYPE || assignTo == SymbolData.LONG_TYPE || 
        assignTo == SymbolData.FLOAT_TYPE ||  assignTo == SymbolData.DOUBLE_TYPE;
    }
  };
  
  /** This represents the long primitive */
  public static final SymbolData LONG_TYPE = new PrimitiveData("long"){
    
    /** You can cast a long primitive to a char, int, long, float, double or short or byte or Short object (in 1.5) */
    public boolean isCastableTo(SymbolData castTo, JavaVersion version) {
      return isAssignableTo(castTo, version) || castTo == SymbolData.CHAR_TYPE || castTo == SymbolData.INT_TYPE || 
        castTo == SymbolData.SHORT_TYPE || castTo == SymbolData.BYTE_TYPE;
    }

    /** You can assign a long primitive to a long, float, double, or Long object(in 1.5) */
    public boolean isAssignableTo(SymbolData assignTo, JavaVersion version) {
      if (assignTo == null) {return false;}

      if (LanguageLevelConverter.versionSupportsAutoboxing(version) && !assignTo.isPrimitiveType()) {
        SymbolData autoBoxMe = LanguageLevelConverter.symbolTable.get("java.lang.Long");
        return autoBoxMe != null && autoBoxMe.isAssignableTo(assignTo, version);
      }

      return assignTo == SymbolData.LONG_TYPE || 
        assignTo == SymbolData.FLOAT_TYPE || 
        assignTo == SymbolData.DOUBLE_TYPE;
        
    }
  };
  
  /** This represents the float primitive */
  public static final SymbolData FLOAT_TYPE = new PrimitiveData("float"){
    
    /** You can cast a float primitive to a char, int, long, float, double or short or byte or Short object (in 1.5) */
    public boolean isCastableTo(SymbolData castTo, JavaVersion version) {
      return isAssignableTo(castTo, version) || castTo == SymbolData.CHAR_TYPE || castTo == SymbolData.INT_TYPE 
        || castTo == SymbolData.LONG_TYPE || castTo == SymbolData.SHORT_TYPE || castTo == SymbolData.BYTE_TYPE;
    }

    /** You can assign a float primitive to a float, double, or Float object(in 1.5) */
    public boolean isAssignableTo(SymbolData assignTo, JavaVersion version) {
      if (assignTo == null) return false;

      if (LanguageLevelConverter.versionSupportsAutoboxing(version) && !assignTo.isPrimitiveType()) {
        SymbolData autoBoxMe = LanguageLevelConverter.symbolTable.get("java.lang.Float");
        return autoBoxMe != null && autoBoxMe.isAssignableTo(assignTo, version);
      }

      return assignTo == SymbolData.FLOAT_TYPE || assignTo == SymbolData.DOUBLE_TYPE;
    }
  };
  
  /**This represents the double primitive*/
  public static final SymbolData DOUBLE_TYPE = new PrimitiveData("double"){
    
    /** A double primitive can be cast to a char, int, long, float, double, short or byte primitive or a Float
      * or Double object,  */
    public boolean isCastableTo(SymbolData castTo, JavaVersion version) {
      return isAssignableTo(castTo, version) || castTo == SymbolData.CHAR_TYPE || castTo == SymbolData.INT_TYPE 
        || castTo == SymbolData.LONG_TYPE || castTo == SymbolData.DOUBLE_TYPE || castTo == SymbolData.SHORT_TYPE 
        || castTo == SymbolData.BYTE_TYPE;
    }

    /** You can assign a double primitive to a float, double, or Float object(in 1.5) */
    public boolean isAssignableTo(SymbolData assignTo, JavaVersion version) {
      if (assignTo == null) { return false; }

      if (LanguageLevelConverter.versionSupportsAutoboxing(version) && ! assignTo.isPrimitiveType()) {
        SymbolData autoBoxMe = LanguageLevelConverter.symbolTable.get("java.lang.Double");
        return autoBoxMe != null && autoBoxMe.isAssignableTo(assignTo, version);
      }
      return assignTo == SymbolData.DOUBLE_TYPE; 
    }
  };
  
  /** Used for the void type. */
  public static final SymbolData VOID_TYPE = new PrimitiveData("void") {
  
    /** A void value cannot be cast to anything */
    public boolean isCastableTo(SymbolData castTo, JavaVersion version) { return false; }
    
    /** A void value can be assigned to itself */
    public boolean isAssignableTo(SymbolData toCheck, JavaVersion version) { return this == toCheck; }
  };
  
  /** Used for an exception */
  public static final SymbolData EXCEPTION = new SymbolData("exception") {
    /** You cannot cast an exception to anything. */
    public boolean isCastableTo(SymbolData castTo, JavaVersion version) { return false; }
    
    /** Returns true, because an exception takes the place of a return.
      * This SymbolData is only used when the user has thrown an Exception.
      */
    public boolean isAssignableTo(SymbolData toCheck, JavaVersion version) { return true; }
    
  };

  /** Used to signal a symbol table search that failed. */
  public static final SymbolData NOT_FOUND = new SymbolData("not found") {
  
    /** A not-found value cannot be cast to anything */
    public boolean isCastableTo(SymbolData castTo, JavaVersion version) { return false; }
    
    /** A not-found value cannot be assigned to anything */
    public boolean isAssignableTo(SymbolData toCheck, JavaVersion version) { return false; }
  };
  
 /** Singleton class representing null*/
  public static final SymbolData NULL_TYPE = new SymbolData("null") {

    /** You can cast null to any reference type (i.e. something that is not a primitive). */
    public boolean isCastableTo(SymbolData castTo, JavaVersion version) {
      return isAssignableTo(castTo, version);
    }

    /** You can assign a null to any reference (non-primitive) type */
    public boolean isAssignableTo(SymbolData assignTo, JavaVersion version) {
      return (assignTo != null) && ! assignTo.isPrimitiveType();
    }
    
  };
  
  /** Used when 2 or more SymbolDatas could match*/
  public static final SymbolData AMBIGUOUS_REFERENCE = new SymbolData("ambiguous reference") {
    /** An ambiguous reference cannot be cast to anything */
    public boolean isCastableTo(SymbolData castTo, JavaVersion version) { return false; }
    
    /** An ambiguous reference cannot be assigned to anything */
    public boolean isAssignableTo(SymbolData toCheck, JavaVersion version) { return false; }
  };
  
  /** Used when a this constructor invocation is seen. */
  public static final SymbolData THIS_CONSTRUCTOR = new SymbolData("this constructor") {
    /** Cannot be cast to anything */
    public boolean isCastableTo(SymbolData castTo, JavaVersion version) { return false; }
    
    /** Cannot be assigned to anything */
    public boolean isAssignableTo(SymbolData toCheck, JavaVersion version) { return false; }
  };
  
  /** Used when a super constructor invocation is seen. */
  public static final SymbolData SUPER_CONSTRUCTOR = new SymbolData("super constructor") {
    /** Cannot be cast to anything */
    public boolean isCastableTo(SymbolData castTo, JavaVersion version) { return false; }
    
    /** Cannot be assigned to anything */
    public boolean isAssignableTo(SymbolData toCheck, JavaVersion version) { return false; }
  };

  /** Do some initialization*/
  static {
    ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"});
    VOID_TYPE.setIsContinuation(false);    
    VOID_TYPE.setMav(_publicMav);
    NOT_FOUND.setIsContinuation(false);
    NOT_FOUND.setMav(_publicMav);
    EXCEPTION.setMav(_publicMav);
    EXCEPTION.setIsContinuation(false);
  }
  
  /********Instance fields***********/
  
  /* The inherited _name field is fully qualified. */
  
  /**True iff this symbol data is a continuation (i.e. hasn't been resolved)*/
  private boolean _isContinuation;
  
  /* True iff this symbol corresponds to a file with an autogenerated "import junit.framework.*;" */
  private boolean _hasAutoGeneratedJunitImport;

  /**The generic type information for this class.  Not used. */
  private TypeParameter[] _typeParameters;
  
  /**List of methods defined in this class*/
  private LinkedList<MethodData> _methods;
  
  /**The SymbolData corresponding to this class's super class. */
  private SymbolData _superClass;
  
  /**List of SymbolDatas corresponding to super interfaces. */
  private ArrayList<SymbolData> _interfaces;
  
  /**List of SymbolDatas corresponding to inner interfaces. */
  private LinkedList<SymbolData> _innerInterfaces;
  
  /** Flag that is true if this SymbolData is an interface. */
  private boolean _isInterface;
  
  /** Stores the package information for this symbol data.  May not be set. */
  private String _package;
  
  /** Represents an instance of this class--i.e. an instantiation of it.*/
  private InstanceData _instanceData;
  
  /**The current constructor count.  Incremented during first pass, decremented during second pass. */
  private int _constructorNumber;
  
  /** The number of local classes, used in naming them. */
  private int _localClassNum;
  
  /* The number of anonymous inner classes, used in naming them. */
  private int _anonymousInnerClassNum;
  
  /** Constructors */
  
  /** Constructor for SymbolData
    * @param name  The name of this class or interface
    * @param modifiersAndVisibility  The modifiersAndVisibility of this class or interface
    * @param typeParameters  Generic type info, not used
    * @param superClass  The super class of this class
    * @param interfaces  The super interfaces
    * @param outerData  The enclosing data of this class, or null
    */
  public SymbolData(String name, ModifiersAndVisibility modifiersAndVisibility, TypeParameter[] typeParameters, 
                    SymbolData superClass, ArrayList<SymbolData> interfaces, Data outerData) {
    super(outerData);
    _name = name;
    _modifiersAndVisibility = modifiersAndVisibility;
    _typeParameters = typeParameters;
    _methods = new LinkedList<MethodData>();
    _superClass = superClass;
    _interfaces = interfaces;
    
    assert interfaces != null;
    
    for (SymbolData sd: interfaces) { addEnclosingData(sd); }
    
    /* Add first because we want to look at the super class first */
    _enclosingData.addFirst(_superClass);
    _innerClasses = new LinkedList<SymbolData>();
    _innerInterfaces = new LinkedList<SymbolData>();
    _isContinuation = false;
    _hasAutoGeneratedJunitImport = false;
    _isInterface = false;
    _localClassNum = 0;
    _anonymousInnerClassNum = 0;
    _package = "";
    _constructorNumber = 0;
    _instanceData = new InstanceData(this);
  }

  /** Constructor for SymbolData
    * @param name  The name of this class or interface
    * @param modifiersAndVisibility  The modifiersAndVisibility of this class or interface
    * @param typeParameters  Generic type info, not used
    * @param superClass  The super class of this class
    * @param interfaces  The super interfaces
    * @param outerData  The enclosing data of this class, or null
    */
  public SymbolData(String name, ModifiersAndVisibility modifiersAndVisibility, TypeParameter[] typeParameters,
                    SymbolData superClass, ArrayList<SymbolData> interfaces, Data outerData, String pkg) {
    this(name, modifiersAndVisibility, typeParameters, superClass, interfaces, outerData);
    _package = pkg;
  }
  
  /** This constructor is only called by Interfaces.  Thus, there is never a super class, and _isInterface
    * is set to true.
    * @param name  The name of this class or interface
    * @param modifiersAndVisibility  The modifiersAndVisibility of this class or interface
    * @param typeParameters  Generic type info, not used
    * @param interfaces  The super interfaces
    * @param outerData  The enclosing data of this class, or null
    */
  public SymbolData(String name, ModifiersAndVisibility modifiersAndVisibility, TypeParameter[] typeParameters,
                    ArrayList<SymbolData> interfaces, Data outerData) {
    this(name, modifiersAndVisibility, typeParameters, null, interfaces, outerData);
    _isInterface = true;
  }

  /** Creates a continuation symbol for the specified name; does not enter this name in any table. */
  public SymbolData(String name) { this(name, SourceInfo.NONE); }
  
  /** Creates a continuation symbol for the specified name and source info; does not enter this name in any table. */ 
  public SymbolData(String name, SourceInfo si) {
    super(null);
    _name = name;
    _modifiersAndVisibility = new ModifiersAndVisibility(si, new String[0]);
    _typeParameters = new TypeParameter[0];
    _methods = new LinkedList<MethodData>();
    _superClass = null;
    _interfaces = new ArrayList<SymbolData>();
    _innerClasses = new LinkedList<SymbolData>();
    _innerInterfaces = new LinkedList<SymbolData>();
    _isContinuation = true;
    _isInterface = false;
    _package = "";
    _instanceData = new InstanceData(this);
  }
  
  /** No reference type is a Primitive type. */
  public boolean isPrimitiveType() { return false; }
  
  public String toString() {
    if (_isContinuation) return "? " + _name;
    return "!" + _name;
  }

  /**
   * See if this can be assigned to assignTo.
   * An assignment is legal:
   *   1. if this is a class type
   *      - if assignTo is a class type, then the types must be the same, or this must be a subclass of assignTo
   *      - if assignTo is an interface, then we must implement it.
   *      - if assignTo is an array, then it is not assignable.
   *  2. if this is an interface
   *      - if assignTo is a class, assignTo must be java.lang.Object.
   *      - if assignTo is an interface, then the types must be the same, or this must be a subinterface of assignTo.
   *      - if assignTo is an array, then it is not assignable
   * All of these rules are followed in isSubclassOf(), which traverses the hierarchy.
   */
   public boolean isAssignableTo(SymbolData assignTo, JavaVersion version) {
     if (assignTo != null) {
       if (assignTo.isPrimitiveType() && LanguageLevelConverter.versionSupportsAutoboxing(version)) { 
         // You never box the left, so see if this can be unboxed to be a primitive.
         SymbolData unboxedType = this.unbox();
         if (unboxedType == null) return false;
         else return unboxedType.isAssignableTo(assignTo, version);
       }
       else return this.isSubClassOf(assignTo);
     }
     return false;
   }
  
   /** Resolves this symbol using the visitor llv. 
     * @return the new symbol definition. */
   public SymbolData resolve(SourceInfo si, LanguageLevelVisitor llv) { return llv.resolveSymbol(si, this); }
   
   /** If this SymbolData is a wrapper class for a primitive, return the primitive type.  Else return null.
     * @return the Primitive this SymbolData wraps, if there is one.  Otherwise return null.
     */
   private SymbolData unbox() {
     String name = getName();
     if (name.equals("java.lang.Integer")) {return SymbolData.INT_TYPE;}
     if (name.equals("java.lang.Character")) {return SymbolData.CHAR_TYPE;}
     if (name.equals("java.lang.Short")) {return SymbolData.SHORT_TYPE;}
     if (name.equals("java.lang.Byte")) {return SymbolData.BYTE_TYPE;}
     if (name.equals("java.lang.Float")) {return SymbolData.FLOAT_TYPE;}
     if (name.equals("java.lang.Double")) {return SymbolData.DOUBLE_TYPE;}
     if (name.equals("java.lang.Long")) {return SymbolData.LONG_TYPE;}
     if (name.equals("java.lang.Boolean")) {return SymbolData.BOOLEAN_TYPE;}
     return null;
   }

  /**
   * See if this can be cast as a castTo.
   * A cast is legal if:
   *   1. if both sides are the same type
   *   2. Both are primitives and
   *      - a widening primitive coversion is possible OR
   *      - a narrowing primitive conversion is possible
   *   3. this is a class type and
   *      - castTo is a class type and one is the super class of the other
   *      - castTo is an interface type and this is not final OR this implements castTo
   *      - castTo is an array type, and this is Object
   *   4. this is an interface type and
   *      - castTo is a class type and is not final OR this implements this
   *      - castTo is an interface type and this and castTo do not contain one or more methods with the same signature 
   *        but different return types
   *      - castTo is an array type and this is either Serializable or Cloneable.
   * @param castTo  The TypeData we are trying to cast to.  (castTo) this
   * @return true  If the cast is legal, false otherwise.
   */
   public boolean isCastableTo(SymbolData castTo, JavaVersion version) {
     if (castTo != null) {
       if (castTo.isPrimitiveType()) { 
         if (LanguageLevelConverter.versionSupportsAutoboxing(version)) { 
           //You never box the left, so see if this can be unboxed to be a primitive.
           SymbolData unboxedType = this.unbox();
           if (unboxedType == null) {return false;}
           else {return unboxedType.isCastableTo(castTo, version);}
         }
         else {return false;} //without autoboxing, cannot cast an object to a primitive
       }
       
       else if (!this.isInterface()) { //we're a class
         if (!castTo.isInterface()) {//castTo is a class or array
           return this.isSubClassOf(castTo) || castTo.isSubClassOf(this);
         }
         
         else { //castTo is an interface
           return (!castTo.hasModifier("final") || castTo.isSubClassOf(this));
         }
         
       }
       
       else { // this is an interface
         
         if (!castTo.isInterface()) {//castTo is a class or array
           return !castTo.hasModifier("final") || castTo.isSubClassOf(this);
         }
         
         else { // castTo is an interface
           // return false if this and castTo contain methods with the same signature but different return types.
           if (LanguageLevelConverter.versionSupportsAutoboxing(version)) return true; 
           for (MethodData md: this.getMethods()) {
             if (checkDifferentReturnTypes(md, castTo, false, version)) {
               /* TypeChecker._addError("Types " + this.getName() + " and " + castTo.getName() + " are incompatible.  
                  Both implement " + md.getName() + " but have different return types", md.getSourceInfo()); */
               return false;
             }
           }
           return true;
         }
       }
     }
     return false;
   }

   /** Depth-first traversal of the tree of enclosing data checking to see if sd is above this SymbolData 
     * in the class hierarchy.
     */
   public boolean isSubClassOf(SymbolData sd) {
    if (sd == null) return false;
    if (this.equals(sd)) return true;
    if (sd.isInterface()) {
      for (SymbolData i: _interfaces) {
        if (i == null) continue;
        if (i.equals(sd)) return true;
        if (i.isSubClassOf(sd)) return true;
      }
    }

    if (_superClass != null) return _superClass.isSubClassOf(sd);
    return false;
  }
  
   /** Checks to see if this SymbolData is an inner class of outerClass (that is, that outerClass appears
     * somewhere in its enclosing data chain.  If stopAtStatic flag is true, stop as soon as we see a static class
     * in the chain.  This is because we are trying to resolve something, such as a "this" call, that cannot be seen outside
     * of a static inner class.
     * @param outerClass  The SymbolData we believe might be our outer class.
     * @param stopAtStatic  boolean flag, true if we want to stop at a static outer class.
     * @return  true if we are its inner class, false otherwise.
     */
  public boolean isInnerClassOf(SymbolData outerClass, boolean stopAtStatic) {
    if (this == outerClass) return true;
    Data outerData = this.getOuterData();
    if (outerData == null) return false;
    if (stopAtStatic && this.hasModifier("static")) {return false;}
    return outerData.getSymbolData().isInnerClassOf(outerClass, stopAtStatic);
  }
  
  /**@return false, since this is a SymbolData and not an InstanceData*/
  public boolean isInstanceType() { return false; }

 /**@return this SymbolData.*/
  public SymbolData getSymbolData() { return this; }
  
  /** @return the InstanceData corresponding to this class. */
  public InstanceData getInstanceData() { return _instanceData; }

  /** Sets the InstanceData for this class to the specified value. */
  public void setInstanceData(InstanceData id) { _instanceData = id; }
  
  /** @return the package */
  public String getPackage() { return _package; }
  
  /** Sets the package to the specified value */
  public void setPackage(String pkg) { _package = pkg;  }
  
  /** @return the generic type parameters */
  public TypeParameter[] getTypeParameters() {
    return _typeParameters;
  }
  
  /**Set the generic type parameters to the specified value*/
  public void setTypeParameters(TypeParameter[] typeParameters) {
    _typeParameters = typeParameters;
  }
  
  /**@return true if this is an interface*/
  public boolean isInterface() {
    return _isInterface;
  }
  
  public void setInterface(boolean ii) {
    _isInterface = ii;
  }
  
  /**@return the list of inner interfaces*/
  public LinkedList<SymbolData> getInnerInterfaces() {
    return _innerInterfaces;
  }
  
  /** Takes in a name and tries to match it with one of this Data's inner classes or inner interfaces.  The input string
    * is a name relative to this SymbolData (such as B.C to request the class A.B.C from class A) and may be delimited 
    * by '.' or '$'.  
    * TODO; NO!!! This is broken.  '$' appears in anonymous class names and local class names, where it is NOT
    * used as a name segment separator
    * Checks the super class and interfaces of this SymbolData to see if the inner class or interface 
    * can be found there.  If no matching visibile inner classes or interfaces are found, but one or more that are not 
    * visible are found, one of the non-visibile ones will be returned.
    * @return  The SymbolData for the matching inner class or interface or null if there isn't one or SymbolData.
    *          AMBIGUOUS_REFERENCE if the reference is ambiguous.
    */
  protected SymbolData getInnerClassOrInterfaceHelper(String nameToMatch, int firstIndexOfDot) {
    Iterator<SymbolData> iter = innerClassesAndInterfacesIterator();
    while (iter.hasNext()) {
      SymbolData sd = iter.next();
      String sdName = sd.getName();

      sdName = LanguageLevelVisitor.getUnqualifiedClassName(sdName);
      if (firstIndexOfDot == -1) {
        if (sdName.equals(nameToMatch))
          return sd;
      }
      else {
        if (sdName.equals(nameToMatch.substring(0, firstIndexOfDot))) {
          return sd.getInnerClassOrInterface(nameToMatch.substring(firstIndexOfDot + 1));
        }
      }
    }
    
    SymbolData result = null;
    SymbolData newResult = null;
    SymbolData privateResult = null;
    
    // Next, look through the inner classes/interfaces of this class's super class
    // Check accessibility, because if you cannot see the super class's inner class, you can't use it.
    if (_superClass != null) {
      newResult = _superClass.getInnerClassOrInterfaceHelper(nameToMatch, firstIndexOfDot);
      if (newResult != null) {
        SymbolData outerPiece;
        
        if (firstIndexOfDot > 0)
          outerPiece = _superClass.getInnerClassOrInterfaceHelper(nameToMatch.substring(0, firstIndexOfDot), -1);
        else outerPiece = newResult;
        
        if (TypeChecker.checkAccess(outerPiece.getMav(), outerPiece, this)) {result = newResult;}
        else privateResult = newResult;
      }
    }
    
    // Next, look through the inner classes/interfaces of each of this class's interfaces
    // Check accessibility, because if you cannot see the super class's inner class, you can't use it.
    for (SymbolData id: _interfaces) {  // TODO: find out how null is being inserted in _interfaces
      if (id == null) {
//        System.err.println("In SymbolData " + getName() + ", _interfaces contains a null entry");
//        assert false;
        continue;
      }
       newResult = id.getInnerClassOrInterfaceHelper(nameToMatch, firstIndexOfDot);
      if (newResult != null) {
        SymbolData outerPiece;
        if (firstIndexOfDot > 0) {
          outerPiece = _superClass.getInnerClassOrInterfaceHelper(nameToMatch.substring(0, firstIndexOfDot), -1);
        }
        else { outerPiece = newResult; }        
        if (TypeChecker.checkAccess(outerPiece.getMav(), outerPiece, this)) {
          if (result == null) {result = newResult;}
          else {return SymbolData.AMBIGUOUS_REFERENCE;}
        }
        else {privateResult = newResult;}
      }
    }
    if (result != null) {return result;}
    return privateResult;  //this will be null if no matching inner classes/interfaces were found.  
  }
  

  /**Iterate over first the inner classes and then the inner interfaces.*/
  public Iterator<SymbolData> innerClassesAndInterfacesIterator() {
    return new Iterator<SymbolData>() {
      private Iterator<SymbolData> _first = _innerClasses.iterator();
      private Iterator<SymbolData> _second = _innerInterfaces.iterator();
      
      public boolean hasNext() { return _first.hasNext() || _second.hasNext(); }
      
      public SymbolData next() {
        if (_first.hasNext()) return _first.next();
        else return _second.next();
      }
      
      public void remove() { throw new UnsupportedOperationException(); }
    };
  }
  
  
  /**Add the specified innerInterface to the list of innerInterfaces*/
  public void addInnerInterface(SymbolData innerInterface) { _innerInterfaces.addLast(innerInterface); }
  
  /**Increment the local class num and return it*/
  public int preincrementLocalClassNum() { return ++_localClassNum; }
  
  /**Set the anonymous inner class num to the specified value*/
  public void setAnonymousInnerClassNum(int i) { _anonymousInnerClassNum=i; }
  
  /**Increment the anonymous inner class num, and return it*/
  public int preincrementAnonymousInnerClassNum() { return ++_anonymousInnerClassNum; }
  
  /**@return the anonymous inner class num*/
  public int getAnonymousInnerClassNum() { return _anonymousInnerClassNum; }
  
  /**Return the local class num, and then decrement it*/
  public int postdecrementLocalClassNum() { return _localClassNum--; }
  
  /**Return the anonymous inner class num, and then decrement it*/
  public int postdecrementAnonymousInnerClassNum() { return _anonymousInnerClassNum--; }  
  
  /** Adds a (perhaps mutable) variable or field to a SymbolData. */
  public boolean addVar(VariableData var) {
    // Next line commented out because local variables do not have a initial value
//    if (! var.isFinal()) var.setHasValue();
    return super.addVar(var); 
  }
  
  /** Adds fields or variables to a SymboLData.*/
  public boolean addVars(VariableData[] vars) {
   boolean success = true;
    for (int i = 0; i<vars.length; i++) {
      LinkedList<SymbolData> seen = new LinkedList<SymbolData>();
      if (! _repeatedName(vars[i], seen)) {
        // Next line commented out because local variables do not have a initial value
//        if (! vars[i].isFinal()) vars[i].setHasValue();
        _vars.addLast(vars[i]);
      }
      else success = false;
    }
    return success;
 }

  /** Add the array of variable datas to the list of variables defined in this scope, unless a name has already been 
    * used.  Return true if all variables were added successfully, false otherwise.  Set each of the variable datas in 
    * the array to be final before adding them. 
    * Since this method is used to add fields at the Functional level,
    * and at this level, we do not want the user to be able to shadow fields defined
    * in the superclass hierarchy, instead of using the normal repeated Name method,
    * check the repeated name throughout the hierarchy.
    * 
    * @param vars  The VariableData[] that we want to add.
    * @return  true if all VariableDatas were added successfully, false otherwise.
    */
  public boolean addFinalVars(VariableData[] vars) {
    boolean success = true;
    for (int i = 0; i<vars.length; i++) {
      LinkedList<SymbolData> seen = new LinkedList<SymbolData>();
      if (!_repeatedNameInHierarchy(vars[i], seen)) {
        if (! vars[i].isFinal()) vars[i].setFinal();
        _vars.addLast(vars[i]);
      }
      else {
        success = false;
      }
    }
    return success;
  }
  
  /** Checks to see if a variable with the same name as vr has already been defined in the scope of this data.  If so, 
    * return true.  Otherwise, return false.
    * @param vr  The VariableData whose name we are searching for.
    * @return  true if that name has already been used in this scope, false otherwise.
    */
  private boolean _repeatedName(VariableData vr, LinkedList<SymbolData> seen) {
    seen.addLast(this);
    Iterator<VariableData> iter = _vars.iterator();
    while (iter.hasNext()) {
      if (vr.getName().equals(iter.next().getName())) return true;
    }
    return false;
  }
  
  /** Checks to see if a variable with the same name as vr has already been defined in the scope of this data or its 
    * super class/interfaces.  If so, return true.  Otherwise, return false.
    * @param vr  The VariableData whose name we are searching for.
    * @return  true if that name has already been used in this scope, false otherwise.
    */
  private boolean _repeatedNameInHierarchy (VariableData vr, LinkedList<SymbolData> seen) {
    seen.addLast(this);
    Iterator<VariableData> iter = _vars.iterator();
    while (iter.hasNext()) {
      if (vr.getName().equals(iter.next().getName())) return true;
    }
    
    // Does this shadow something in the super class?
    if (_superClass != null && _superClass._repeatedNameInHierarchy(vr, seen)) return true;
    
    //Does this shadow something in the super interfaces?  TODO: postpone this test until all interfaces have been identified.
    for (SymbolData sd: _interfaces) {
      if (sd != null && sd._repeatedNameInHierarchy(vr, seen)) return true;
    }
    return false;
  }
  
  /** @return the list of methods defined in this symbol data*/
  public LinkedList<MethodData> getMethods() { return _methods; }
  
  /** Returns true if there is a method with the given name in this SymbolData.
    * @param name  The name of the method to return
    * @return  true if a MethodData is found or false otherwise.
    */
  public boolean hasMethod(String name) {
    for (int i = 0; i < _methods.size(); i++) {
      MethodData currMd = _methods.get(i);
      if (currMd.getName().equals(name)) return true;
    }
    return false;
  }
  
  /** Returns the method with the given name and param types.
    * @param name  The name of the method to return
    * @param paramTypes  Array of the TypeDatas correpsonding to the parameters to the method.
    * @return  The matched MethodData or null if it is not found
    */
  public MethodData getMethod(String name, TypeData[] paramTypes) {
    for (MethodData currMd: _methods) {
      if (currMd.getName().equals(name)) {
        if (paramTypes.length == currMd.getParams().length) {
          boolean match = true;
          for (int j = 0; j < paramTypes.length; j++) {  // TODO; clean up this coding!
            if (paramTypes[j] == null || paramTypes[j].getSymbolData() == null || currMd.getParams()[j].getType() == null)
              continue;  // prevents a null pointer exception
            if (! paramTypes[j].getSymbolData().equals(currMd.getParams()[j].getType().getSymbolData())) { 
              match = false; 
              break; 
            }
          }
          if (match) return currMd;
        }
      }
    }
    return null;
  }
  
  /**Sets the list of methods to the specified one*/
  public void setMethods(LinkedList<MethodData> methods) {
    _methods = methods;
  }
  
  /**Calls repeatedSignature with fromClassFile set to false by default.*/
  public static MethodData repeatedSignature(LinkedList<MethodData> listOfMethods, MethodData method) {
    return repeatedSignature(listOfMethods, method, false);
  }
  
  /** Checks if two methods in this SymbolData have the same name and parameters.
    * @param listOfMethods  The methods in this SymbolData
    * @param method  The MethodData for the method to be added
    * @param fromClassFile  Whether or not a class file is adding this method.
    *                       Important because bridge methods, which differ only in return type,
    *                       can be added from a class file and are legal but cannot exist in source code.
    * @return the MethodData that was already in listOfMethods that method duplicates if there is one or null otherwise.
    */
  public static MethodData repeatedSignature(LinkedList<MethodData> listOfMethods, MethodData method, 
                                             boolean fromClassFile) {
    Iterator<MethodData> iter = listOfMethods.iterator();
    VariableData[] methodParams = method.getParams();
    while (iter.hasNext()) {
      boolean match = true;
      MethodData currMd = iter.next();
      // Check if names are the same and if this is called from a class file check if return types are the same.
      if (currMd.getName().equals(method.getName()) && 
          (! fromClassFile || currMd.getReturnType() == method.getReturnType())) {
        VariableData[] currMdParams = currMd.getParams();
        if (currMdParams.length == methodParams.length) {
          for (int i = 0; i < currMdParams.length; i++) {
            if (currMdParams[i].getType() != methodParams[i].getType()) {
              match = false;
              break;
            }
          }
          if (match) return currMd;
        }
      }
    }
    return null;
  }
  
  

  /** @return true if this is a primitive boolean or a Boolean with autoboxing enabled*/
  boolean isBooleanType(JavaVersion version) {
    return this == BOOLEAN_TYPE || 
      (getName().equals("java.lang.Boolean") && LanguageLevelConverter.versionSupportsAutoboxing(version));
  }
  

  /** @return true if this is a primitive char or a Character with autoboxing enabled*/
  boolean isCharType(JavaVersion version) {
    return this == CHAR_TYPE || (getName().equals("java.lang.Character") && 
                                 LanguageLevelConverter.versionSupportsAutoboxing(version));
  }
    
  /** @return true if this is a primitive byte or a Byte with autoboxing enabled*/
  boolean isByteType(JavaVersion version) {
    return this == BYTE_TYPE || (getName().equals("java.lang.Byte") && 
                                 LanguageLevelConverter.versionSupportsAutoboxing(version));
  }
  
  /** @return true if this is a primitive short or a Short with autoboxing enabled*/
  boolean isShortType(JavaVersion version) {
    return this == SHORT_TYPE || (getName().equals("java.lang.Short") && 
                                  LanguageLevelConverter.versionSupportsAutoboxing(version));
  }
  
  /**@return true if this is a primitive int or an Integer with autoboxing enabled*/
  boolean isIntType(JavaVersion version) {
    return this==INT_TYPE || (getName().equals("java.lang.Integer") && 
                              LanguageLevelConverter.versionSupportsAutoboxing(version));
  }
  
  /**@return true if this is a primitive long or a Long with autoboxing enabled*/
  boolean isLongType(JavaVersion version) {
    return this == LONG_TYPE || (this.getName().equals("java.lang.Long") && 
                                 LanguageLevelConverter.versionSupportsAutoboxing(version));
  }
  
  /**@return true if this is a primitive char or a Character with autoboxing enabled*/
  boolean isFloatType(JavaVersion version) {
    return this == FLOAT_TYPE || (getName().equals("java.lang.Float") && 
                                  LanguageLevelConverter.versionSupportsAutoboxing(version));
  }
  
  /**@return true if this is a primitive double or a Double with autoboxing enabled*/
  boolean isDoubleType(JavaVersion version) {
    return this == DOUBLE_TYPE || 
      (getName().equals("java.lang.Double") && LanguageLevelConverter.versionSupportsAutoboxing(version));
  }
  
  /** Compares the ModifiersAndVisibility of the 2 method data to determine if overwriting can override the access 
    * priviledges of overwritten.  
    */
  private static boolean _isCompatible(MethodData overwritten, MethodData overwriting) {
    if (overwritten.hasModifier("public")) { // A public method can only be overwritten by a public method.
      return overwriting.hasModifier("public");
    }
    if (overwritten.hasModifier("protected")) { // A protected method can only be overwritten by protected/public method
      return overwriting.hasModifier("protected") || overwriting.hasModifier("public");
    } 
    if (! overwritten.hasModifier("private")) { // default methods can be overridden by a non-private method
      return ! overwriting.hasModifier("private");
    }
    return true;
  }
  
  /**Call checkDifferentReturnTypes with addError set to true by default*/
  protected static boolean checkDifferentReturnTypes(MethodData md, SymbolData sd, JavaVersion version) {
      return checkDifferentReturnTypes(md, sd, true, version);
    }
  
  /** Called to make sure that no method has the same name and parameters as a method it inherits while having
    * different return types.
    * @param md  The MethodData we're currently trying to add
    * @param sd  The SymbolData of the class whose enclosingDatas we're examining for 
    *            different return types.
    * @param addError  true if errors should be added to the type checker
    * @return  Whether there exists a conflict
    */
  protected static boolean checkDifferentReturnTypes(MethodData md, SymbolData sd, boolean addError, 
                                                     JavaVersion version) {
    // We only want to check the super class and interfaces, not outer classes.
    ArrayList<SymbolData> interfaces = sd.getInterfaces();    
    LinkedList<SymbolData> enclosingData = new LinkedList<SymbolData>();
    enclosingData.addAll(interfaces);
    SymbolData superClass = sd.getSuperClass();
    if (superClass != null) {
      enclosingData.add(superClass);
    }
    Iterator<SymbolData> iter = enclosingData.iterator();
    while (iter.hasNext()) {
      SymbolData currSd = iter.next();
      MethodData matchingMd = repeatedSignature(currSd.getMethods(), md);
      if (matchingMd != null) {
        if (matchingMd.hasModifier("private")) return false;
        boolean subclass = md.getReturnType().isSubClassOf(matchingMd.getReturnType());
        if (matchingMd.getReturnType() != md.getReturnType() && ! subclass && 
            LanguageLevelConverter.versionIs15(version)) {
          StringBuffer methodSignature = new StringBuffer(md.getName() + "(");
          VariableData[] params = md.getParams();
          for (int i = 0; i < params.length; i++) {
            if (i > 0) methodSignature.append(", ");
            methodSignature.append(params[i].getType().getName());
          }
          methodSignature.append(")");
          String methodSigString = methodSignature.toString();
          // This entire method is only called from the type checker, so add an error to its error list.
          if (addError) { 
            TypeChecker.errors.addLast(new Pair<String, JExpressionIF>(methodSigString + " in " + sd.getName() + 
                                                                       " cannot override " + methodSigString + " in " +
                                                                       currSd.getName() + 
                                                                       "; attempting to use different return types",
                                                                       md.getJExpression())); }
          return true;
        }
        
        if (! _isCompatible(matchingMd, md)) {  // check compatibility of visiblity modifiers
          String access = "package";
          if (matchingMd.hasModifier("private")) access = "private";
          if (matchingMd.hasModifier("public")) access = "public";
          if (matchingMd.hasModifier("protected")) access = "protected";
          if (addError) {
            TypeChecker.errors.
              addLast(new Pair<String, JExpressionIF>(md.getName() + " in " + md.getSymbolData().getName() 
                                                        + " cannot override " + matchingMd.getName() + " in " 
                                                        + matchingMd.getSymbolData().getName() + 
                                                      ".  You are attempting to assign weaker access priviledges. In " 
                                                        + matchingMd.getSymbolData().getName() + ", "
                                                        + matchingMd.getName() + " was " 
                                                        + access, md.getJExpression())); } 
          return true;
        }
      }
      else if (checkDifferentReturnTypes(md, currSd, version)) return true;
    }
    return false;
  }
  
  /** Checks to see if methodName is used in this SymbolData's scope.  If so, finds a new name for the method by 
    * appending a counter to its name until an unused method name results.  Returns the new name.
    * @param methodName  The initial String name of the variable we are creating.
    * @return  The new variable name which does not shadow anything in vars.
    */
  public String createUniqueMethodName(String methodName) {
    LinkedList<SymbolData> toCheck = new LinkedList<SymbolData>();
    toCheck.add(this);
    Set<String> names = new HashSet<String>();
    while(toCheck.size() > 0) {
      SymbolData sd = toCheck.removeFirst();
      LinkedList<MethodData> methods = sd.getMethods();
      for(MethodData md : methods) { names.add(md.getName()); }
      if (sd.getSuperClass() != null) { toCheck.add(sd.getSuperClass()); }
      toCheck.addAll(sd.getInterfaces());
      if (sd.getOuterData() != null) { toCheck.add(sd.getOuterData().getSymbolData()); }
    }
        
    String newName = methodName;
    int counter = 0;  // Note: loop tests for counter overflow, but memory would be exhausted much earlier
    while (names.contains(newName) && counter != -1) {
      newName = methodName + counter; 
      counter++;
    }
    
    if (counter == -1) throw 
      new RuntimeException("Internal Program Error: Unable to rename method " + methodName 
                             + ".  All possible names were taken.  Please report this bug.");

    return newName; 
  }
  
  
  /** Called to generate an error message when two methods are created with the same signature. */
  private String _createErrorMessage(MethodData md) {
    StringBuffer message = 
      new StringBuffer("In the class \"" + md.getSymbolData().getName() + 
                       "\", you cannot have two methods with the same name: \"" + md.getName() + "\"");
    VariableData[] params = md.getParams();
    if (params.length > 0) {
      message.append(" and parameter type");
      if (params.length > 1) {
        message.append("s");
      }
      message.append(":");
    }
    for (int i = 0; i < params.length; i++) {
      VariableData p = params[i];
      if (p != null && p.getType() != null) {
        if (i > 0) {
          message.append(",");
        }
        message.append(" " + p.getType().getName());
      }
    }
    return message.toString();
  }
  
  /** When adding a method, we must check that the method's signature (name and parameters)
    * does not match that of any other method in the same class.  We also must check that
    * a method does not have the same signature but a different return type from a method 
    * in its superclass or one of its interfaces.
`    */
  public void addMethod(MethodData method) {
    // Detect repeated methods
    if (repeatedSignature(_methods, method) != null) {
      LanguageLevelVisitor.errors.addLast(new Pair<String, JExpressionIF>(_createErrorMessage(method), 
                                                                          method.getJExpression()));
    }
    else {
      _methods.addLast(method);
//      System.err.println("*** Adding method " + method.getName() + " to " + this);
    }
  }
  
  /** This version of addMethod is called whenever the method corresponds to one that is auto-generated
    * (toString, equals, hashCode, etc.) and is necessary because we need to check if the user defined
    * a method with the same signature and, if so, highlight the user method instead of trying to highlight
    * the auto-generated method (which doesn't appear in the raw version of the source anyway).
    */
  public void addMethod(MethodData method, boolean isAugmentedCode) {
    // Detect if a method was user-defined that matches the signature of an auto-generated method.
    MethodData md = repeatedSignature(_methods, method);
    if (md != null) {
      LanguageLevelVisitor.errors.
        addLast(new Pair<String, JExpressionIF>("This method's signature conflicts with an automatically generated "
                                                  + "method's signature", 
                                                md.getJExpression()));
    }
    else {
        _methods.addLast(method);
    }
  }
  
  /**
   * Important to know if this is called from a class file since JSR14 allows methods to have the same
   * name and parameters with different return types for bridge methods.
   */
  public void addMethod(MethodData method, boolean isAugmentedCode, boolean fromClassFile) {
  // This checking cannot be performed until symbolTable is complete  
//    // Detect if a method was user-defined that matches the signature of an auto-generated method.
//    MethodData md = repeatedSignature(_methods, method, fromClassFile);
//    if (md != null) {
//      LanguageLevelVisitor.errors.
//        addLast(new Pair<String, JExpressionIF>(_createErrorMessage(method), md.getJExpression()));
//    }
//    else {
      _methods.addLast(method);
//    }
  }

  /**@return the superClass of this symbol data*/
  public SymbolData getSuperClass() {
    return _superClass;
  }
  
  public void clearSuperClass() { _superClass = null; }
  
  /** Set the super class to the specified value. */
  public void setSuperClass(SymbolData superClass) {
    assert superClass != null;
    _superClass = superClass;
    addEnclosingData(superClass);
  }
  
  /**@return the interfaces of this symbol data*/
  public ArrayList<SymbolData> getInterfaces() { return _interfaces; }
  
  /** Add an interface to the list of interfaces.  TODO: find out where null is being added as an interface! */
  public void addInterface(SymbolData interphace) {
    if (interphace != null) {
      _interfaces.add(interphace);
      addEnclosingData(interphace);
    }
  }
  
  /**Set the interfaces to be the specified list*/
  public void setInterfaces(ArrayList<SymbolData> interfaces) {
    assert interfaces != null;
    _interfaces = interfaces;
    for (SymbolData sd: interfaces) { if (sd != null) addEnclosingData(sd); }
  }
  
  /**Add one to the number of constructors for this symbol data*/
  public void incrementConstructorCount() {
    _constructorNumber ++;
  }
  
  /**Subtract one from the number of constructors for this symbol data*/
  public void decrementConstructorCount() {
    _constructorNumber --;
  }
  
  /**@return the number of constructors*/
  public int getConstructorCount() {
    return _constructorNumber;
  }
  
  /**@return true if this is a continuation*/
  public boolean isContinuation() {
    return _isContinuation;
  }
  
  /**Set the isContinuation flag to the specified value*/
  public void setIsContinuation(boolean isContinuation) {
    _isContinuation = isContinuation;
  }
  
  /**@return true if this is a continuation*/
  public boolean hasAutoGeneratedJunitImport() {
    return _hasAutoGeneratedJunitImport;
  }
  
  /**Set the isContinuation flag to the specified value*/
  public void setHasAutoGeneratedJunitImport(boolean hasAutoGeneratedJunitImport) {
    _hasAutoGeneratedJunitImport = hasAutoGeneratedJunitImport;
  }

  /**@return true if this Symbol Data is a primitive type that is a number type*/
  public boolean isNumberTypeWithoutAutoboxing() {
    return (this == SymbolData.INT_TYPE ||
        this == SymbolData.DOUBLE_TYPE ||
        this == SymbolData.LONG_TYPE ||
        this == SymbolData.CHAR_TYPE ||
        this == SymbolData.FLOAT_TYPE ||
        this == SymbolData.SHORT_TYPE ||
        this == SymbolData.BYTE_TYPE);
  }
  
  /**@return true if this Symbol Data is a primitive type that is a number type or is
   * a Object number type with autoboxing enabled.*/
  public boolean isNumberType(JavaVersion version) {
    if (!LanguageLevelConverter.versionSupportsAutoboxing(version)) {
      return isNumberTypeWithoutAutoboxing();
    }
    if (this.isDoubleType(version) ||
        this.isFloatType(version) ||
        this.isLongType(version) ||
        this.isIntType(version) ||        
        this.isCharType(version) ||
        this.isShortType(version) ||
        this.isByteType(version)) {
      return true;
    }
    return false;
  }


  /**@return true if this is a non float or boolean type, with autoboxing if it is allowed, or without autoboxing*/
  public boolean isNonFloatOrBooleanType(JavaVersion version) {
    if (!LanguageLevelConverter.versionSupportsAutoboxing(version)) {
      return isNonFloatOrBooleanTypeWithoutAutoboxing();
    }

    return (this.isIntType(version) ||
        this.isLongType(version) ||
        this.isCharType(version) ||
        this.isShortType(version) ||
        this.isByteType(version) ||
        this.isBooleanType(version));
  }

  /** @return true if this is a non float or boolean type, without autoboxing*/
  public boolean isNonFloatOrBooleanTypeWithoutAutoboxing() {
    return (this==SymbolData.INT_TYPE ||
        this==SymbolData.LONG_TYPE ||
        this==SymbolData.CHAR_TYPE ||
        this==SymbolData.SHORT_TYPE ||
        this==SymbolData.BYTE_TYPE ||
        this==SymbolData.BOOLEAN_TYPE);
    
  }
  
  
  /**
   * Returns true if the provided Object is equal to this symbol data.
   * SymbolDatas are equal if their fields are the same.
   * */
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if ((obj.getClass() != this.getClass())) { //|| (obj.hashCode() != this.hashCode())) {
      return false;
    }
    SymbolData sd = (SymbolData) obj;    

    /*Return true if the fields are all .equals.*/
      
    return this.isContinuation() == sd.isContinuation() && 
      this.getMav().equals(sd.getMav()) &&
      LanguageLevelVisitor.arrayEquals(this.getTypeParameters(), sd.getTypeParameters()) &&
      this.getMethods().equals(sd.getMethods()) &&
      this.getSuperClass() == sd.getSuperClass() && //could be null
      this.getInterfaces().equals(sd.getInterfaces()) &&
      this.getOuterData() == sd.getOuterData() && //could be null
      this.getInnerClasses().equals(sd.getInnerClasses()) &&
      this.getName().equals(sd.getName()) &&
      this.getInnerInterfaces().equals(sd.getInnerInterfaces()) &&
      this.getPackage().equals(sd.getPackage()) &&
      this.isInterface() == sd.isInterface();
 

  }
  
  /**
   * Since SymbolDatas have unique names, in any given symboltable, hash on the name
   */
  public int hashCode() {
    return getName().hashCode();
  }
  
  /** Check to see if this class is one of the 5 known classes that implement Runnable.  Checking each class recursively
    * to see if it extends Runnable is too time consuming, so we are hoping this short cut will work.
    * Clearly, this has some disadvantages.
    */
  public boolean implementsRunnable() {
   return this.getName().equals("java.lang.Thread") || this.getName().equals("java.util.TimerTask") 
     || this.getName().equals("javax.swing.text.AsyncBoxView$ChildState") 
     || this.getName().equals("java.awt.image.renderable.RenderableImageProducer")
     || this.getName().equals("java.util.concurrent.FutureTask");// || this.getName().equals("java.lang.Runnable");
  }
  
  /**Check to see if the interface i appears anywhere in the hierarchy for this class/interface*/
  public boolean hasInterface(SymbolData i) {
    if (i == null) return false;

    if (getInterfaces().contains(i)) { return true; }
    
    if ((this.getSuperClass() != null) && this.getSuperClass().hasInterface(i)) { return true; }
    
    for (int j = 0; j < getInterfaces().size(); j++) {
      if (getInterfaces().get(j).hasInterface(i)) return true;
    }
    return false;
  }
  
  /**
   * Get all of your vars and all vars that you inherit.
   */
  public LinkedList<VariableData> getAllSuperVars() {
    LinkedList<VariableData> myVars = new LinkedList<VariableData>();
    if (this.getSuperClass() != null) {
      myVars.addAll(this.getSuperClass().getVars());
      myVars.addAll(this.getSuperClass().getAllSuperVars());
    }
    for (int i = 0; i<getInterfaces().size(); i++) {
      myVars.addAll(this.getInterfaces().get(i).getVars());
      myVars.addAll(this.getInterfaces().get(i).getAllSuperVars());
    }
    return myVars;
  }
  
   /** Test the methods defined in the above class */
  public static class SymbolDataTest extends TestCase {
    
    private SymbolData _sd;
    
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[0]);
    private ModifiersAndVisibility _abstractMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"final"});
    private ModifiersAndVisibility _publicFinalMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[]{"public", "final"});
    
    public SymbolDataTest() {
      this("");
    }
    public SymbolDataTest(String name) {
      super(name);
    }
    
    public void setUp() {
      _sd = new SymbolData("i.like.monkey");
      LanguageLevelVisitor.errors = new LinkedList<Pair<String, JExpressionIF>>();
    }
    
    public void testRepeatedSignatures() {
      MethodData md = new MethodData("methodName", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, 
                                     new VariableData[] { new VariableData(SymbolData.CHAR_TYPE),
        new VariableData(SymbolData.BOOLEAN_TYPE)}, 
                                     new String[0],
                                     _sd,
                                     null);
      md.getParams()[0].setEnclosingData(md);
      md.getParams()[1].setEnclosingData(md);
      LinkedList<MethodData> mds = new LinkedList<MethodData>();
      mds.addFirst(md);
      // Test same name, return type, and params from a source file.
      MethodData md2 = new MethodData("methodName", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, 
                                      new VariableData[] { new VariableData(SymbolData.CHAR_TYPE),
        new VariableData(SymbolData.BOOLEAN_TYPE) }, 
                                      new String[0],
                                      _sd,
                                      null);
      md2.getParams()[0].setEnclosingData(md2);
      md2.getParams()[1].setEnclosingData(md2);
                                
      assertTrue("repeatedSignatures should return md exactly.", md == _sd.repeatedSignature(mds, md2));
      // Test same name, return type, and params from a class file.
      MethodData md3 = new MethodData("methodName", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, 
                                      new VariableData[] { new VariableData(SymbolData.CHAR_TYPE),
        new VariableData(SymbolData.BOOLEAN_TYPE) }, 
                                      new String[0],
                                      _sd,
                                      null);
      md3.getParams()[0].setEnclosingData(md3);
      md3.getParams()[1].setEnclosingData(md3);
                                
      assertTrue("repeatedSignatures should return md exactly.", md == _sd.repeatedSignature(mds, md3, true));
      // Test same name, different return type, and params from a class file.
      MethodData md4 = new MethodData("methodName", _publicMav, new TypeParameter[0], SymbolData.SHORT_TYPE, 
                                      new VariableData[] { new VariableData(SymbolData.CHAR_TYPE),
        new VariableData(SymbolData.BOOLEAN_TYPE) }, 
                                      new String[0],
                                      _sd,
                                      null);      
      md4.getParams()[0].setEnclosingData(md4);
      md4.getParams()[1].setEnclosingData(md4);

                                      
      assertEquals("repeatedSignatures should return null.", null, _sd.repeatedSignature(mds, md4, true));
      // Test same name, return type, and different params from a source file.
      MethodData md5 = new MethodData("methodName", _publicMav, new TypeParameter[0], SymbolData.SHORT_TYPE, 
                                      new VariableData[] { new VariableData(SymbolData.BOOLEAN_TYPE) }, 
                                      new String[0],
                                      _sd,
                                      null);
      md5.getParams()[0].setEnclosingData(md5);
                                    
      assertEquals("repeatedSignatures should return null.", null, _sd.repeatedSignature(mds, md5));
      // Test same name, return type, and different order params from a source file.
      MethodData md6= new MethodData("methodName", _publicMav, new TypeParameter[0], SymbolData.SHORT_TYPE, 
                                      new VariableData[] { new VariableData(SymbolData.BOOLEAN_TYPE),
        new VariableData(SymbolData.CHAR_TYPE) }, 
                                      new String[0],
                                      _sd,
                                      null);
      md6.getParams()[0].setEnclosingData(md6);
      md6.getParams()[1].setEnclosingData(md6);
                                      
      assertEquals("repeatedSignatures should return null.", null, _sd.repeatedSignature(mds, md6));
    }
    
    public void testCheckDifferentReturnTypes() {
      
      TypeChecker.errors = new LinkedList<Pair<String, JExpressionIF>>();
      
      // Create a super class and give it a method.
      SymbolData superSd = new SymbolData("superClass");
      MethodData md = new MethodData("methodName",
                                     _publicMav,
                                     new TypeParameter[0],
                                     SymbolData.INT_TYPE,
                                     new VariableData[0],
                                     new String[0],
                                     superSd,
                                     null);
      superSd.addMethod(md);
      _sd.setSuperClass(superSd);
      // Test with an exact copy of the method.
      MethodData md2 = new MethodData("methodName",
                                      _publicMav,
                                      new TypeParameter[0],
                                      SymbolData.INT_TYPE,
                                      new VariableData[0],
                                      new String[0],
                                      _sd,
                                      null);
      assertFalse("There should not be a conflict.", checkDifferentReturnTypes(md2, _sd, JavaVersion.JAVA_5));
      assertEquals("There should not be an error.", 0, TypeChecker.errors.size());
      // Test with an exact copy of the method except for differing return types.
      MethodData md3 = new MethodData("methodName",
                                      _publicMav,
                                      new TypeParameter[0],
                                      SymbolData.CHAR_TYPE,
                                      new VariableData[0],
                                      new String[0],
                                      _sd,
                                      null);
      assertTrue("There should be a conflict.", checkDifferentReturnTypes(md3, _sd, JavaVersion.JAVA_5));
      assertEquals("There should be one error.", 1, TypeChecker.errors.size());
      assertEquals("The error message should be correct.", 
                   "methodName() in i.like.monkey cannot override methodName() in superClass;" + 
                   " attempting to use different return types",
                   TypeChecker.errors.get(0).getFirst());
      // Create a super super class and give it a method.
      SymbolData superSuperSd = new SymbolData("superSuperClass");
      MethodData md4 = new MethodData("superSuperMethodName",
                                      _publicMav,
                                      new TypeParameter[0],
                                      SymbolData.INT_TYPE,
                                      new VariableData[] { new VariableData(SymbolData.CHAR_TYPE) },
                                      new String[0],
                                      superSuperSd,
                                      null);
      md4.getParams()[0].setEnclosingData(md4);
      superSuperSd.addMethod(md4);
      superSd.setSuperClass(superSuperSd);
      // Test with an exact copy except for differing parameters.
      MethodData md5 = new MethodData("superSuperMethodName",
                                      _publicMav,
                                      new TypeParameter[0],
                                      SymbolData.INT_TYPE,
                                      new VariableData[0],
                                      new String[0],
                                      null,
                                      null);
      assertFalse("There should not be a conflict.", checkDifferentReturnTypes(md5, _sd, JavaVersion.JAVA_5));
      assertEquals("There should still be one error.", 1, TypeChecker.errors.size());
      // Test with an exact copy except for diffeing return types.
      MethodData md6 = new MethodData("superSuperMethodName",
                                      _publicMav,
                                      new TypeParameter[0],
                                      SymbolData.BYTE_TYPE,
                                      new VariableData[] { new VariableData(SymbolData.CHAR_TYPE) },
                                      new String[0],
                                      null,
                                      null);
      md6.getParams()[0].setEnclosingData(md6);
      assertTrue("There should be a conflict.", checkDifferentReturnTypes(md6, _sd, JavaVersion.JAVA_5));
      assertEquals("There should be two errors.", 2, TypeChecker.errors.size());
      
      //Test a method that restricts the mav of the super class's method
      MethodData md7 = new MethodData("superSuperMethodName",
                                      _privateMav,
                                      new TypeParameter[0],
                                      SymbolData.INT_TYPE,
                                      new VariableData[] { new VariableData(SymbolData.CHAR_TYPE) },
                                      new String[0],
                                      new SymbolData("myData"),
                                      null);

      md7.getParams()[0].setEnclosingData(md7);
      assertTrue("There should be a conflict", checkDifferentReturnTypes(md7, _sd, JavaVersion.JAVA_5));
      assertEquals("There should be three errors", 3, TypeChecker.errors.size());
      assertEquals("The error message should be correct", 
                   "superSuperMethodName in myData cannot override superSuperMethodName in " + superSuperSd.getName() + 
                   ".  You are attempting to assign weaker access priviledges. In " + superSuperSd.getName() + 
                   ", superSuperMethodName was public", TypeChecker.errors.get(2).getFirst());
                                      
      
      //Test a method that narrows the return type of the super class's method
      SymbolData stringSd = new SymbolData("java.lang.String");
      stringSd.setIsContinuation(false);
      stringSd.setSuperClass(SymbolData.INT_TYPE);
      
      MethodData md8 = new MethodData("superSuperMethodName",
                                      _publicMav,
                                      new TypeParameter[0],
                                      stringSd,
                                      new VariableData[] {new VariableData(SymbolData.CHAR_TYPE)},
                                      new String[0],
                                      new SymbolData("myData"),
                                      null);
                                      
      assertFalse("There should be no conflict", checkDifferentReturnTypes(md8, _sd, JavaVersion.JAVA_5));
      assertEquals("There should still be 3 errors", 3, TypeChecker.errors.size());
      /* Java 1.4 is not supported. */
//      assertTrue("There should be a conflict in 1.4", checkDifferentReturnTypes(md8, _sd, JavaVersion.JAVA_1_4));
//      assertEquals("There should now be 4 errors", 4, TypeChecker.errors.size());
//      assertEquals("The error message should be correct", TypeChecker.errors.getLast().getFirst(), 
//                   "superSuperMethodName(char) in superClass cannot override superSuperMethodName(char) in " + 
//                   superSuperSd.getName() + "; attempting to use different return types");
    }
    
    public void test_createErrorMessage() {
      // Test with a method with no parameters.
      MethodData md = new MethodData("methodName",
                                     _publicMav,
                                     new TypeParameter[0],
                                     SymbolData.INT_TYPE,
                                     new VariableData[0],
                                     new String[0],
                                     _sd,
                                     null);
      assertEquals("The error message should be correct.", 
                   "In the class \"i.like.monkey\", you cannot have two methods with the same name: \"methodName\"", 
                   _sd._createErrorMessage(md));
      // Test with a method with one parameter.
      MethodData md2 = new MethodData("superSuperMethodName",
                                      _publicMav,
                                      new TypeParameter[0],
                                      SymbolData.INT_TYPE,
                                      new VariableData[] { new VariableData(SymbolData.CHAR_TYPE) },
                                      new String[0],
                                      _sd,
                                      null);
      assertEquals("The error message should be correct.", 
                   "In the class \"i.like.monkey\", you cannot have two methods with the same name: "
                     + "\"superSuperMethodName\" and parameter type: char", 
                   _sd._createErrorMessage(md2));
      md2.getParams()[0].setEnclosingData(md2);
      // Test with a method with two parameters.
      MethodData md3 = new MethodData("superSuperMethodName",
                                      _publicMav,
                                      new TypeParameter[0],
                                      SymbolData.INT_TYPE,
                                      new VariableData[] { new VariableData(SymbolData.CHAR_TYPE),
        new VariableData(SymbolData.CHAR_TYPE) },
                                      new String[0],
                                      _sd,
                                      null);
      md3.getParams()[0].setEnclosingData(md3);
      md3.getParams()[1].setEnclosingData(md3);
                                      
      assertEquals("The error message should be correct.", 
                   "In the class \"i.like.monkey\", you cannot have two methods with the same name: "
                     + "\"superSuperMethodName\" and parameter types: char, char", 
                   _sd._createErrorMessage(md3));
      
    }
    
    public void testAddMethod() {
      MethodData md = new MethodData("methodName", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, 
                                     new VariableData[] { new VariableData(SymbolData.CHAR_TYPE),
        new VariableData(SymbolData.BOOLEAN_TYPE)}, 
                                     new String[0],
                                     _sd,
                                     null);
                                     
      md.getParams()[0].setEnclosingData(md);
      md.getParams()[1].setEnclosingData(md);
                                     
      _sd.addMethod(md);
      assertEquals("There should be no errors.", 0, LanguageLevelVisitor.errors.size());
      // Test same name, return type, and params from a source file.
      MethodData md2 = new MethodData("methodName", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, 
                                      new VariableData[] { new VariableData(SymbolData.CHAR_TYPE),
        new VariableData(SymbolData.BOOLEAN_TYPE) }, 
                                      new String[0],
                                      _sd,
                                      null);
      md2.getParams()[0].setEnclosingData(md2);
      md2.getParams()[1].setEnclosingData(md2);
                                      
      _sd.addMethod(md2);
      assertEquals("There should be one error.", 1, LanguageLevelVisitor.errors.size());
      assertEquals("The error message should be correct.", "In the class \"" + _sd.getName() + 
                   "\", you cannot have two methods with the same name: \"" + md.getName() 
                     + "\" and parameter types: char, boolean",
                   LanguageLevelVisitor.errors.get(0).getFirst());
      MethodData md4 = new MethodData("methodName",
                                     _publicMav,
                                     new TypeParameter[0],
                                     SymbolData.INT_TYPE,
                                     new VariableData[0],
                                     new String[0],
                                     _sd,
                                     null);
      _sd.addMethod(md4);
      assertEquals("There should still be one error.", 1, LanguageLevelVisitor.errors.size());
      MethodData md5 = new MethodData("methodNamePlusStuff",
                                     _publicMav,
                                     new TypeParameter[0],
                                     SymbolData.INT_TYPE,
                                     new VariableData[0],
                                     new String[0],
                                     _sd,
                                     null);
      _sd.addMethod(md5);
      assertEquals("There should still be one error.", 1, LanguageLevelVisitor.errors.size());
      // Check that _sd only has two methods, the original md and md5.
      assertEquals("There are three methods in _sd.", 3, _sd.getMethods().size());
      assertEquals("The original method was added.", md, _sd.getMethods().get(0));
      assertEquals("md5 was added.", md5, _sd.getMethods().get(2));
      
      // Check that adding a method from augmented code produces the correct error message.
      _sd.addMethod(md2, true);
      assertEquals("There should be two errors.", 2, LanguageLevelVisitor.errors.size());
      assertEquals("The error message should be correct.",
                   "This method's signature conflicts with an automatically generated method's signature",
                   LanguageLevelVisitor.errors.get(1).getFirst());
    }
    
    public void testIsNumberType() {
      assertTrue(SymbolData.INT_TYPE.isNumberType(JavaVersion.JAVA_5));
      assertTrue(new SymbolData("java.lang.Integer").isNumberType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.DOUBLE_TYPE.isNumberType(JavaVersion.JAVA_5));
      assertTrue(new SymbolData("java.lang.Double").isNumberType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.LONG_TYPE.isNumberType(JavaVersion.JAVA_5));
      assertTrue(new SymbolData("java.lang.Long").isNumberType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.CHAR_TYPE.isNumberType(JavaVersion.JAVA_5));
      assertTrue(new SymbolData("java.lang.Character").isNumberType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.FLOAT_TYPE.isNumberType(JavaVersion.JAVA_5));
      assertTrue(new SymbolData("java.lang.Float").isNumberType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.SHORT_TYPE.isNumberType(JavaVersion.JAVA_5));
      assertTrue(new SymbolData("java.lang.Short").isNumberType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.BYTE_TYPE.isNumberType(JavaVersion.JAVA_5));
      assertTrue(new SymbolData("java.lang.Byte").isNumberType(JavaVersion.JAVA_5));     
      assertFalse(SymbolData.BOOLEAN_TYPE.isNumberType(JavaVersion.JAVA_5));
      assertFalse(new SymbolData("java.lang.Boolean").isNumberType(JavaVersion.JAVA_5));
    }

    public void testIsNumberTypeWithoutAutoboxing() {
      assertTrue(SymbolData.INT_TYPE.isNumberTypeWithoutAutoboxing());
      assertFalse((new SymbolData("java.lang.Integer")).isNumberTypeWithoutAutoboxing());
      assertTrue(SymbolData.DOUBLE_TYPE.isNumberTypeWithoutAutoboxing());
      assertFalse((new SymbolData("java.lang.Double")).isNumberTypeWithoutAutoboxing());
      assertTrue(SymbolData.LONG_TYPE.isNumberTypeWithoutAutoboxing());
      assertFalse((new SymbolData("java.lang.Long")).isNumberTypeWithoutAutoboxing());
      assertTrue(SymbolData.CHAR_TYPE.isNumberTypeWithoutAutoboxing());
      assertFalse((new SymbolData("java.lang.Character")).isNumberTypeWithoutAutoboxing());
      assertTrue(SymbolData.FLOAT_TYPE.isNumberTypeWithoutAutoboxing());
      assertFalse((new SymbolData("java.lang.Float")).isNumberTypeWithoutAutoboxing());
      assertTrue(SymbolData.SHORT_TYPE.isNumberTypeWithoutAutoboxing());
      assertFalse((new SymbolData("java.lang.Short")).isNumberTypeWithoutAutoboxing());
      assertTrue(SymbolData.BYTE_TYPE.isNumberTypeWithoutAutoboxing());
      assertFalse((new SymbolData("java.lang.Byte")).isNumberTypeWithoutAutoboxing());     
      assertFalse(SymbolData.BOOLEAN_TYPE.isNumberTypeWithoutAutoboxing());
      assertFalse((new SymbolData("java.lang.Boolean")).isNumberTypeWithoutAutoboxing());
    }

    
    public void testIsIntType() {
      assertTrue(SymbolData.INT_TYPE.isIntType(JavaVersion.JAVA_5));
      assertTrue((new SymbolData("java.lang.Integer")).isIntType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.DOUBLE_TYPE.isIntType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Double")).isIntType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.LONG_TYPE.isIntType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Long")).isIntType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.CHAR_TYPE.isIntType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Character")).isIntType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.FLOAT_TYPE.isIntType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Float")).isIntType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.SHORT_TYPE.isIntType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Short")).isIntType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.BYTE_TYPE.isIntType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Byte")).isIntType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.BOOLEAN_TYPE.isIntType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Boolean")).isIntType(JavaVersion.JAVA_5));
    }
    

    public void testIsDoubleType() {
      assertFalse(SymbolData.INT_TYPE.isDoubleType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Integer")).isDoubleType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.DOUBLE_TYPE.isDoubleType(JavaVersion.JAVA_5));
      assertTrue((new SymbolData("java.lang.Double")).isDoubleType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.CHAR_TYPE.isDoubleType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Character")).isDoubleType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.FLOAT_TYPE.isDoubleType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Float")).isDoubleType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.SHORT_TYPE.isDoubleType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Short")).isDoubleType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.BYTE_TYPE.isDoubleType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Byte")).isDoubleType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.BOOLEAN_TYPE.isDoubleType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Boolean")).isDoubleType(JavaVersion.JAVA_5));
    }
    
    public void testIsCharType() {
      assertFalse(SymbolData.INT_TYPE.isCharType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Integer")).isCharType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.DOUBLE_TYPE.isCharType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Double")).isCharType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.CHAR_TYPE.isCharType(JavaVersion.JAVA_5));
      assertTrue((new SymbolData("java.lang.Character")).isCharType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.FLOAT_TYPE.isCharType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Float")).isCharType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.SHORT_TYPE.isCharType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Short")).isCharType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.BYTE_TYPE.isCharType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Byte")).isCharType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.BOOLEAN_TYPE.isCharType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Boolean")).isCharType(JavaVersion.JAVA_5));
    }
    
    public void testIsFloatType() {
      assertFalse(SymbolData.INT_TYPE.isFloatType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Integer")).isFloatType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.DOUBLE_TYPE.isFloatType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Double")).isFloatType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.CHAR_TYPE.isFloatType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Character")).isFloatType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.FLOAT_TYPE.isFloatType(JavaVersion.JAVA_5));
      assertTrue((new SymbolData("java.lang.Float")).isFloatType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.SHORT_TYPE.isFloatType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Short")).isFloatType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.BYTE_TYPE.isFloatType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Byte")).isFloatType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.BOOLEAN_TYPE.isFloatType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Boolean")).isFloatType(JavaVersion.JAVA_5));
    }
    
    
    public void testIsByteType() {
      assertFalse(SymbolData.INT_TYPE.isByteType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Integer")).isByteType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.DOUBLE_TYPE.isByteType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Double")).isByteType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.CHAR_TYPE.isByteType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Character")).isByteType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.FLOAT_TYPE.isByteType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Float")).isByteType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.SHORT_TYPE.isByteType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Short")).isByteType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.BYTE_TYPE.isByteType(JavaVersion.JAVA_5));
      assertTrue((new SymbolData("java.lang.Byte")).isByteType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.BOOLEAN_TYPE.isByteType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Boolean")).isByteType(JavaVersion.JAVA_5));
    }
    
    public void testIsBooleanType() {
      assertFalse(SymbolData.INT_TYPE.isBooleanType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Integer")).isBooleanType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.DOUBLE_TYPE.isBooleanType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Double")).isBooleanType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.CHAR_TYPE.isBooleanType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Character")).isBooleanType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.FLOAT_TYPE.isBooleanType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Float")).isBooleanType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.SHORT_TYPE.isBooleanType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Short")).isBooleanType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.BYTE_TYPE.isBooleanType(JavaVersion.JAVA_5));
      assertFalse((new SymbolData("java.lang.Byte")).isBooleanType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.BOOLEAN_TYPE.isBooleanType(JavaVersion.JAVA_5));
      assertTrue((new SymbolData("java.lang.Boolean")).isBooleanType(JavaVersion.JAVA_5));
    }
    
    public void testIsNonFloatOrBooleanType() {
      assertTrue(SymbolData.INT_TYPE.isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertTrue(new SymbolData("java.lang.Integer").isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.DOUBLE_TYPE.isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertFalse(new SymbolData("java.lang.Double").isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.CHAR_TYPE.isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertTrue(new SymbolData("java.lang.Character").isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.FLOAT_TYPE.isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertFalse(new SymbolData("java.lang.Float").isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.SHORT_TYPE.isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertTrue(new SymbolData("java.lang.Short").isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.BYTE_TYPE.isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertTrue(new SymbolData("java.lang.Byte").isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.LONG_TYPE.isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertTrue(new SymbolData("java.lang.Long").isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.BOOLEAN_TYPE.isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertTrue(new SymbolData("java.lang.Boolean").isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      
    }
    
    public void testIsNonFloatOrBooleanTypeWithoutAutoboxing() {
      assertTrue(SymbolData.INT_TYPE.isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.DOUBLE_TYPE.isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.CHAR_TYPE.isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertFalse(SymbolData.FLOAT_TYPE.isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.SHORT_TYPE.isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.BYTE_TYPE.isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.LONG_TYPE.isNonFloatOrBooleanType(JavaVersion.JAVA_5));
      assertTrue(SymbolData.BOOLEAN_TYPE.isNonFloatOrBooleanType(JavaVersion.JAVA_5));
    }
    
    
    public void testEquals() {
      SymbolData superSd = new SymbolData("superClass");
      _sd = 
        new SymbolData("i.like.monkey", _publicMav, new TypeParameter[0], superSd, new ArrayList<SymbolData>(), null);
      
      //check variables that are equal;
      SymbolData _sd2 = 
        new SymbolData("i.like.monkey", _publicMav, new TypeParameter[0], superSd, new ArrayList<SymbolData>(), null);
      assertTrue("Equals should return true if two SymbolDatas are equal", _sd.equals(_sd2));
      assertTrue("Equals should return true in opposite direction as well", _sd2.equals(_sd));

      //comparison to null
      assertFalse("Equals should return false if SymbolData is compared to null",_sd.equals(null));   
    
      //different names
      _sd2 = new SymbolData("q", _publicMav, new TypeParameter[0], superSd, new ArrayList<SymbolData>(), null);
      assertFalse("Equals should return false if class names are different", _sd.equals(_sd2));
      
      //different MAV
      _sd2 = 
        new SymbolData("i.like.monkey", _protectedMav, new TypeParameter[0], superSd, new ArrayList<SymbolData>(), null);
      assertFalse("Equals should return false if class modifiers are different", _sd.equals(_sd2));
      
      //different type parameters
      _sd2 = 
        new SymbolData("i.like.monkey", 
                       _publicMav, 
                       new TypeParameter[] { new TypeParameter(SourceInfo.NONE, 
                                                               new TypeVariable(SourceInfo.NONE,"tv"), 
                                                               new TypeVariable(SourceInfo.NONE,"i")) }, 
                       superSd, 
                       new ArrayList<SymbolData>(), null);
      assertFalse("Equals should return false if class type parameters are different", _sd.equals(_sd2));
      
      //different super classes
      _sd2 = new SymbolData("i.like.monkey", _publicMav, new TypeParameter[0], null, new ArrayList<SymbolData>(), null);
      assertFalse("Equals should return false if super classes are different", _sd.equals(_sd2));
      
      //different interfaces
      ArrayList<SymbolData> interfaces = new ArrayList<SymbolData>();
      interfaces.add(SymbolData.INT_TYPE);
      _sd2 = new SymbolData("i.like.monkey", _publicMav, new TypeParameter[0], superSd, interfaces, null);
      assertFalse("Equals should return false if the interfaces are different", _sd.equals(_sd2));
    }
     
    public void testImplementsRunnable() {
      SymbolData sd1 = new SymbolData("java.lang.Thread");
      SymbolData sd2 = new SymbolData("java.util.TimerTask");
      SymbolData sd3 = new SymbolData("javax.swing.text.AsyncBoxView$ChildState");
      SymbolData sd4 = new SymbolData("java.awt.image.renderable.RenderableImageProducer");
      SymbolData sd5 = new SymbolData("java.util.concurrent.FutureTask");
      SymbolData sd6 = new SymbolData("java.util.Vector");
      assertTrue("Should implement Runnable", sd1.implementsRunnable());
      assertTrue("Should implement Runnable", sd2.implementsRunnable());
      assertTrue("Should implement Runnable", sd3.implementsRunnable());
      assertTrue("Should implement Runnable", sd4.implementsRunnable());
      assertTrue("Should implement Runnable", sd5.implementsRunnable());
      assertFalse("Should not implement Runnable", sd6.implementsRunnable());
    }
  
    public void testHasInterface() {
      /* Runnable has two subinterfacers, subRunnable and subRunnable2.  subRunnable has a subclass someSd and
         subRunnable2 has a subclass someOtherSd. */
      
      SymbolData runnable = new SymbolData("java.lang.Runnable");
      runnable.setInterface(true);
      SymbolData subRunnable = new SymbolData("edgar");
      subRunnable.setInterface(true);
      subRunnable.addInterface(runnable);
      assertTrue(subRunnable.hasInterface(runnable));
      SymbolData subRunnable2 = new SymbolData("jones");
      subRunnable2.setInterface(true);
      subRunnable2.addInterface(runnable);
      assertTrue(subRunnable2.hasInterface(runnable));
      SymbolData someSd = new SymbolData("someSd");
      someSd.addInterface(subRunnable);      
      assertTrue(someSd.hasInterface(runnable));
      SymbolData someOtherSd = new SymbolData("someOtherSd");
      someOtherSd.addInterface(subRunnable2);
      assertTrue(someOtherSd.hasInterface(runnable));
      SymbolData notRunnable = new SymbolData("aksdf");
      assertFalse(notRunnable.hasInterface(runnable));
      someOtherSd.addInterface(notRunnable);
      assertTrue(someOtherSd.hasInterface(runnable));
      
      // Try myClass without setting a super class, then try it once it's super class is someSd.
      SymbolData myClass = new SymbolData("myClass");
      assertFalse(myClass.hasInterface(runnable));
      myClass.setSuperClass(someSd);
      assertTrue(myClass.hasInterface(runnable));
      SymbolData myClass2 = new SymbolData("myClass2");
      assertFalse(myClass2.hasInterface(runnable));
      myClass2.addInterface(someOtherSd);
      assertTrue(myClass2.hasInterface(runnable));
    }
    
    public void test_isAssignable() {
      MethodData md = 
        new MethodData("Overwritten", _publicMav, new TypeParameter[0], _sd, new VariableData[0], new String[0], _sd, 
                       new NullLiteral(SourceInfo.NONE));
      MethodData md2 = 
        new MethodData("Overwriting", _publicMav, new TypeParameter[0], _sd, new VariableData[0], new String[0], _sd, 
                       new NullLiteral(SourceInfo.NONE));

      //tests a wide variety of possibilities, but not all possibilities.
      assertTrue("Should be assignable", _isCompatible(md, md2));
      md.setMav(_protectedMav);
      assertTrue("Should be assignable", _isCompatible(md, md2));
      md.setMav(_privateMav);
      assertTrue("Should be assignable", _isCompatible(md, md2));
      md.setMav(_packageMav);
      assertTrue("Should be assignable", _isCompatible(md, md2));
      md2.setMav(_protectedMav);
      assertTrue("Should be assignable", _isCompatible(md, md2));
      md2.setMav(_privateMav);
      assertFalse("Should not be assignable", _isCompatible(md, md2));
      md2.setMav(_packageMav);
      assertTrue("Should be assignable", _isCompatible(md, md2));
      
    }
    
    
    public void testRepeatedNameInHierarchy() {
      SymbolData _d = new SymbolData("myname");
      
      VariableData vd = new VariableData("v1", _publicMav, SymbolData.INT_TYPE, false, _d);

      //compare a vd to an symbol data with no vars
      assertFalse("No variables to repeat name", _d._repeatedNameInHierarchy(vd, new LinkedList<SymbolData>()));
      
      //compare a vd to a symbol data with 1 var with a different name
      _d.addVar(new VariableData("v2", _protectedMav, SymbolData.BOOLEAN_TYPE, true, _d));
      assertFalse("No repeated name", _d._repeatedNameInHierarchy(vd, new LinkedList<SymbolData>()));
      
      //compare a vd to a symbol data who has a var with the same name
      _d.addVar(vd);
      assertTrue("Should be repeated name", _d._repeatedNameInHierarchy(vd, new LinkedList<SymbolData>()));
      
      //what if super class has var
      _d.setVars(new LinkedList<VariableData>());
      SymbolData superC = new SymbolData("I am a super class");
      superC.addVar(new VariableData(vd.getName(), _protectedMav, SymbolData.DOUBLE_TYPE, false, superC));
      _d.setSuperClass(superC);
      assertTrue("Should be repeated name", _d._repeatedNameInHierarchy(vd, new LinkedList<SymbolData>()));
      
      //what if interface has var
      _d.clearSuperClass();
      _d.addInterface(superC);
      assertTrue("Should also be repeated name", _d._repeatedNameInHierarchy(vd, new LinkedList<SymbolData>()));
    }
    
    public void testAddFinalVars() {
      SymbolData _d = new SymbolData("genius");
      VariableData vd = new VariableData("v1", _publicMav, SymbolData.INT_TYPE, true, _d);
      VariableData vd2 = new VariableData("v2", _publicMav, SymbolData.CHAR_TYPE, true, _d);
      VariableData[] toAdd = new VariableData[] {vd, vd2};
      LinkedList<VariableData> myVds = new LinkedList<VariableData>();
      
      //first adding array
      myVds.addLast(vd);
      myVds.addLast(vd2);
      assertTrue("Should be able to add new vars array", _d.addFinalVars(toAdd));
      assertEquals("Variable list should have 2 variables", myVds, _d.getVars());
      
      //trying to read array whose variables are already there.
      assertFalse("Should not be able to add same variables again", _d.addFinalVars(toAdd));
      assertEquals("Variable list should not have changed", myVds, _d.getVars());
      assertTrue("vd should now be final", _d.getVars().get(0).hasModifier("final"));
      assertTrue("vd2 should now be final", _d.getVars().get(1).hasModifier("final"));

      
      
      //trying to add a different array
      VariableData vd3 = new VariableData("v3", _publicFinalMav, SymbolData.INT_TYPE, true, _d);
      VariableData[] toAdd2 = new VariableData[] {vd3};
      myVds.addLast(vd3);
      
      assertTrue("Should be able to add new variable array", _d.addFinalVars(toAdd2));
      assertEquals("Variable list should now have 3 variables", myVds, _d.getVars());
      assertTrue("vd3 should now be final", _d.getVars().get(2).hasModifier("final"));

      
      //try adding an empty array of variable datas
      assertTrue("Should be able to add an empty array", _d.addFinalVars(new VariableData[0]));
      assertEquals("Variable list should not have changed by adding empty array", myVds, _d.getVars());
    }
    
    public void testGetAllVars() {
      SymbolData sd1 = new SymbolData("SubSub");
      SymbolData sd2 = new SymbolData("Sub");
      SymbolData sd3 = new SymbolData("Super");
      SymbolData sd4 = new SymbolData("SuperInterface");
      sd4.setInterface(true);
      SymbolData sd5 = new SymbolData("NotRelated");
      
      sd1.setSuperClass(sd2);
      sd2.setSuperClass(sd3);
      sd2.addInterface(sd4);

      VariableData vd1 = new VariableData("vd1", _finalMav, SymbolData.INT_TYPE, false, sd1);
      VariableData vd2 = new VariableData("vd2", _finalMav, SymbolData.INT_TYPE, false, sd2);
      VariableData vd3 = new VariableData("vd3", _finalMav, SymbolData.INT_TYPE, false, sd3);
      VariableData vd4 = new VariableData("vd4", _finalMav, SymbolData.INT_TYPE, false, sd4);
      VariableData vd5 = new VariableData("vd5", _finalMav, SymbolData.INT_TYPE, false, sd5);

      sd1.addVar(vd1);
      sd2.addVar(vd2);
      sd3.addVar(vd3);
      sd4.addVar(vd4);
      sd5.addVar(vd5);
      
      LinkedList<VariableData> shouldContain = new LinkedList<VariableData>();
      shouldContain.addLast(vd2);
      shouldContain.addLast(vd3);
      shouldContain.addLast(vd4);

      LinkedList<VariableData> vds = sd1.getAllSuperVars();
      assertEquals("the list should have 3 elements", 3, vds.size());
      assertEquals("The list should be correct", shouldContain, vds);
    }

    public void testIsSubclassOf() {
      _sd = new SymbolData("subClass");
      SymbolData superC = new SymbolData("superC");
      _sd._superClass = superC;
      
      //not subclass
      assertFalse("subClass, int, and boolean are not related", _sd.isSubClassOf(SymbolData.BOOLEAN_TYPE));
      
      //same class
      assertTrue("subClass is a subclass of itself", _sd.isSubClassOf(_sd));
      
      //direct subclass
      assertTrue("subClass is a subclass of its super class", _sd.isSubClassOf(superC));
      
      //subclass of subclass of provided data
      superC._superClass = SymbolData.CHAR_TYPE;
      assertTrue("subClass is a subclass of its super class's super class", _sd.isSubClassOf(SymbolData.CHAR_TYPE));
      
      //Interface
      SymbolData myData = new SymbolData("yes");
      SymbolData yourData = new SymbolData("interface");
      yourData.setInterface(true);
      myData.setIsContinuation(false);
      myData.addInterface(yourData);
      yourData.setIsContinuation(false);
      assertTrue("Should be assignable", myData.isSubClassOf(yourData));
    }

    public void testIsInnerClassOf() {
      _sd = new SymbolData("innerClass");
      SymbolData outer1 = new SymbolData("outer1");
      outer1.addInnerClass(_sd);
      _sd.setOuterData(outer1);
      SymbolData outer2 = new SymbolData("outer2");
      outer2.addInnerClass(outer1);
      outer1.setOuterData(outer2);
      outer2.setMav(new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"static"}));
      SymbolData outer3 = new SymbolData("outer3");
      outer3.addInnerClass(outer2);
      outer2.setOuterData(outer3);
      
      
      SymbolData notInChain = new SymbolData("no");
      
      assertTrue("_sd is an inner class of itself", _sd.isInnerClassOf(_sd, true));
      assertTrue("outer1 is the outer class of _sd", _sd.isInnerClassOf(outer1, true));
      assertTrue("outer2 is an outer class of _sd", _sd.isInnerClassOf(outer2, true));
      assertTrue("outer2 is an outer class of itself", outer2.isInnerClassOf(outer2, true));
      assertFalse("_sd is not related to notInChain", _sd.isInnerClassOf(notInChain, true));
      assertFalse("_sd is not an outer class of outer1", outer1.isInnerClassOf(_sd, true));
      assertFalse("outer3 cannot be seen from _sd if the static flag is true, because outer2 is static", 
                  _sd.isInnerClassOf(outer3, true));
      assertTrue("But, outer3 is an outer class of _sd", _sd.isInnerClassOf(outer3, false));
    }
    
    public void testCreateUniqueMethodName() {
      SymbolData george = new SymbolData("George");
      VariableData[] noVars = new VariableData[0];
      george.addMethod(new MethodData("getFriends", noVars));
      george.addMethod(new MethodData("sayHello", noVars));
      
      SymbolData iAteGeorge = new SymbolData("IAteGeorge");
      iAteGeorge.addMethod(new MethodData("sayHello", noVars));
      iAteGeorge.addMethod(new MethodData("sayHello0", noVars));
      iAteGeorge.addInnerClass(george);
      george.setOuterData(iAteGeorge);
      
      SymbolData mrsGeorge = new SymbolData("MrsGeorge");
      mrsGeorge.addMethod(new MethodData("sayHello1", noVars));
      george.setSuperClass(mrsGeorge);
      
      SymbolData grandmaGeorge = new SymbolData("GrandmaGeorge");
      grandmaGeorge.addMethod(new MethodData("sayHello2", noVars));
      grandmaGeorge.addMethod(new MethodData("sayHello5", noVars));
      george.addInterface(grandmaGeorge);
      
      SymbolData papaOfIAteGeorge = new SymbolData("PapaOfIAteGeorge");
      papaOfIAteGeorge.addMethod(new MethodData("sayHello3", noVars));
      iAteGeorge.setSuperClass(papaOfIAteGeorge);
      
      assertEquals("The generated name is correct when SymbolData has that method name",
                   "getFriends0", george.createUniqueMethodName("getFriends"));
      assertEquals("The generated name is correct when SymbolData doesn't have a method of that name",
                   "eatDinner", george.createUniqueMethodName("eatDinner"));
      assertEquals("The generated name is correct when a super class uses the name",
                   "sayHello10", george.createUniqueMethodName("sayHello1"));
      assertEquals("The generated name is correct when an outer class uses the name",
                   "sayHello00", george.createUniqueMethodName("sayHello0"));
      assertEquals("The generated name is correct when an interface uses the name",
                   "sayHello20", george.createUniqueMethodName("sayHello2"));
      assertEquals("The generated name is correct when a super class of an outer class uses the name",
                   "sayHello30", george.createUniqueMethodName("sayHello3"));
      assertEquals("The generated name is correct when a super class of an outer class uses the name",
                   "sayHello30", george.createUniqueMethodName("sayHello3"));
      assertEquals("The generated name is correct when a lot of things use the name",
                   "sayHello4", george.createUniqueMethodName("sayHello"));
      
    }
  }
}
