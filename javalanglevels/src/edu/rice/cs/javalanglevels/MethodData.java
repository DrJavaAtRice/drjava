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
import edu.rice.cs.javalanglevels.util.Log;
import java.util.*;
import junit.framework.TestCase;
import edu.rice.cs.javalanglevels.parser.JExprParser;

/** Represents the data for a given method. */
public class MethodData extends BodyData {
  
//  protected static final Log _log = new Log("MethodData.txt", false);

  /**Generic Type Parameters.  Not used at any language level.*/
  private TypeParameter[] _typeParameters;
  
  /**The return type of the method*/
  private SymbolData _returnType;
  
  /**Array of VariableDatas, corresponding to the parameters to the method.*/
  private VariableData[] _params;
  
  /**Array of Strings corresponding to exceptions this method is declared to throw*/
  private String[] _thrown;
  
  /**JExpression corresponding to this method */
  private JExpression _jexpr;
  
  /**True if this method was auto-generated during our language level conversion.  False otherwise*/
  private boolean _generated;

  /** Constructor for MethodData.
   * @param name  The String name of the method
   * @param modifiersAndVisibility  The modifiers of the method
   * @param typeParameters  The generic type parameters of the method.  Not used.
   * @param returnType  The SymbolData corresponding to the return type of the method
   * @param params  The VariableData[] corresponding to the method parameters
   * @param thrown  The Strings corresponding to the exceptions the method is declared to throw
   * @param enclosingClass  The SymbolData that contains this method
   * @param jexpr  The JExpression corresponding to this method.
   */
  public MethodData(String name, ModifiersAndVisibility modifiersAndVisibility, TypeParameter[] typeParameters, 
                    SymbolData returnType, VariableData[] params, String[] thrown, SymbolData enclosingClass, 
                    JExpression jexpr) { 
    super(enclosingClass);
    _name = name;
    _modifiersAndVisibility = modifiersAndVisibility;
    _typeParameters = typeParameters;
    _returnType = returnType;
    _params = params;
    _thrown = thrown;
    _jexpr = jexpr;
    _generated = false;
  }
  
  /** Constructor used by the LanguageLevelConverter, where only the name and params matter*/
  public MethodData(String name, VariableData[] params) { 
    this(name, new ModifiersAndVisibility(SourceInfo.NONE, new String[0]), new TypeParameter[0], null, 
         params, new String[0], null, new NullLiteral(SourceInfo.NONE));
  }
  
  /** Factory methods used for debugging purposes among other things. */
  public static MethodData make(String name, ModifiersAndVisibility modifiersAndVisibility, TypeParameter[] typeParameters, 
                    SymbolData returnType, VariableData[] params, String[] thrown, SymbolData enclosingClass, 
                     JExpression jexpr) {

    MethodData md = 
      new MethodData(name, modifiersAndVisibility, typeParameters, returnType, params, thrown, enclosingClass, jexpr);
//    _log.log("Allocated: " + md + '\n' + "With modifiers: " + md.getMav());
    return md;
  }
  
  public static MethodData make(String name, VariableData[] params) { return new MethodData(name, params); }
  
  /**@return true if this method was generated during the LanguageLevel conversion process*/
  public boolean isGenerated() { return _generated; }
  
  /** Returns true if this MethodData is static. */
  public boolean isStatic() { return hasModifier("static"); }
  
  /**@param generated  true or false--whether to set this method to generated or not*/
  public void setGenerated(boolean generated) { _generated = generated; }
  
  /** Two MethodDatas are equal if ... */ 
  public boolean equals(Object obj) { 
    if (obj == this) return true;
    if (obj == null) return false;
    if ((obj.getClass() != this.getClass())) return false;
    MethodData md = (MethodData) obj;

    return _name.equals(md.getName()) &&
      _modifiersAndVisibility.equals(md.getMav()) &&
      // Type parameters are not used in functional code, but names are irrelevant anyway
//      LanguageLevelVisitor.arrayEquals(_typeParameters, md.getTypeParameters()) &&
      LanguageLevelVisitor.arrayEquals(_params, md.getParams()) &&  // TODO: this comparison should be less stringnet; names don't matter
      LanguageLevelVisitor.arrayEquals(_thrown, md.getThrown()) &&
      _enclosingData.get(0) == md.getEnclosingData().get(0) &&
      _vars.equals(md.getVars());
  }
  
  /** Define a hashCode() method that hashes on the method name and its enclosing data. */
  public int hashCode() { return getName().hashCode() ^ getEnclosingData().hashCode(); }
  
  public boolean isMethodData() { return true; }  
  
  public MethodData getMethodData() { return this; }
  
  /**@return the type parameters*/
  public TypeParameter[] getTypeParameters() { return _typeParameters; }
  
  /**@return the return type*/
  public SymbolData getReturnType() { return _returnType; }
  
  /**set the return type to be rt*/
  public void setReturnType(SymbolData rt) { _returnType = rt; }
  
  /**@return the method params*/
  public VariableData[] getParams() { return _params; }
  
  /**Set the method params to be p*/
  public void setParams(VariableData[] p) { _params = p; }
  
  /**@return the thrown array for this method*/
  public String[] getThrown() { return _thrown; }
  
  /** Sets thrown to be thrown*/
  public void setThrown(String[] thrown) { _thrown = thrown; }
  
  /** @return the modifiers and visibility*/
  public ModifiersAndVisibility getMav() { return _modifiersAndVisibility; }
    
  /** Makes this method public.  Only used in ClassBodyElementaryVisitor. */
  public void addPublicMav() { 
    String[] oldMav = _modifiersAndVisibility.getModifiers();
    String[] modifiers = new String[oldMav.length + 1];
    modifiers[0] = "public";
    for (int i = 0; i < oldMav.length; i++) {   modifiers[i+1] = oldMav[i];
    }
    _modifiersAndVisibility = new ModifiersAndVisibility(_modifiersAndVisibility.getSourceInfo(), modifiers);
  }
  

  /** @return the JExpression corresponding to this method*/
  public JExpression getJExpression() { return _jexpr; }
  
  public String toString() { return "method: " + _name; }
  
  public String toBigString() { 
    return "method " + _name + "<" + _modifiersAndVisibility + ", " + 
    Arrays.toString(_typeParameters) + ", " + _returnType + ", " + Arrays.toString(_params) + ", " + 
    Arrays.toString(_thrown) + ", " + _jexpr + ", " + _generated +  ">" ; }
  
  /** Tests the methods declared above*/
  public static class MethodDataTest extends TestCase {
    
    private MethodData _md;
    private MethodData _md2;
    
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"});
    private ModifiersAndVisibility _publicMav2 = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"protected"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"final"});
    
    public MethodDataTest() { this(""); }
    
    public MethodDataTest(String name) { super(name); }
    
    public void testEquals() {
      VariableData vd = new VariableData("v", _publicMav, SymbolData.INT_TYPE, true, _md);
      VariableData vd2 = new VariableData("v2", _protectedMav, SymbolData.BOOLEAN_TYPE, true, _md);
      TypeParameter[] tp = new TypeParameter[0];
      
      Type t = new PrimitiveType(SourceInfo.NONE, "int");
      Word name = new Word(SourceInfo.NONE, "m");
      Word paramName = new Word(SourceInfo.NONE, "i");
      FormalParameter fp = 
        new FormalParameter(SourceInfo.NONE, new UninitializedVariableDeclarator(SourceInfo.NONE, t, paramName), 
                            false);
      MethodDef mdef = 
        new AbstractMethodDef(SourceInfo.NONE, _publicMav, tp, t, name, new FormalParameter[] {fp}, 
                              new ReferenceType[0]);
      _md = new MethodData("m", _publicMav, tp, SymbolData.INT_TYPE, new VariableData[] {vd},
                             new String[] {"I throw this"}, SymbolData.BOOLEAN_TYPE, mdef);

     //Two method datas that should be equal
     _md2 = new MethodData("m", _publicMav2, tp, SymbolData.INT_TYPE, new VariableData[]{vd},
                           new String[] {"I throw this"}, SymbolData.BOOLEAN_TYPE, mdef);
     assertTrue("Two MethodDatas with same fields should be equal", _md.equals(_md2));

    //different return types okay
     _md2 = new MethodData("m", _publicMav2, tp, SymbolData.DOUBLE_TYPE, new VariableData[]{vd},
                           new String[] {"I throw this"}, SymbolData.BOOLEAN_TYPE, mdef);                        
     assertTrue("Two MethodDatas with same fields but different return types should be equal", _md.equals(_md2));
     
    //different method defs okay
      _md2 = new MethodData("m", _publicMav2, tp, SymbolData.INT_TYPE, new VariableData[]{vd},
                           new String[] {"I throw this"}, SymbolData.BOOLEAN_TYPE, null);    
     assertTrue("Two MethodDatas with same fields but different method defs should be equal", _md.equals(_md2));
    
     //compared to null
     _md2 = null;
     assertFalse("A MethodData is never equal to null", _md.equals(_md2));

    //compared to different class
     assertFalse("A MethodData is never equal to another class", _md.equals(new Integer(5)));

    //different names
     _md2 = new MethodData("q", _publicMav2, tp, SymbolData.INT_TYPE, new VariableData[]{vd},
                           new String[] {"I throw this"}, SymbolData.BOOLEAN_TYPE, mdef);
     assertFalse("Two MethodDatas with different names are not equal", _md.equals(_md2));
     
    //different modifiers and visibility
     _md2 = new MethodData("m", _finalMav, tp, SymbolData.INT_TYPE, new VariableData[]{vd},
                           new String[] {"I throw this"}, SymbolData.BOOLEAN_TYPE, mdef);
     assertFalse("Two MethodDatas with different MAVs are not equal", _md.equals(_md2));
     
     /* The different type parameters test is quoted out because the particular names do not matter. */
     // Different type parameters
//     TypeParameter[] tp2 = 
//       new TypeParameter[] { new TypeParameter(SourceInfo.NONE, new TypeVariable(SourceInfo.NONE,"tv"), 
//                                               new TypeVariable(SourceInfo.NONE,"i"))};
//   
//     _md2 = new MethodData("m", _publicMav2, tp2, SymbolData.INT_TYPE, new VariableData[]{vd},
//                           new String[] {"I throw this"}, SymbolData.BOOLEAN_TYPE, mdef);
//     assertFalse("Two MethodDatas with different type parameters are not equal", _md.equals(_md2));
     
    //different thrown
    _md2 = new MethodData("m", _publicMav2, tp, SymbolData.INT_TYPE, new VariableData[]{vd},
                           new String[] {"I throw this", "maybe this too"}, SymbolData.BOOLEAN_TYPE, mdef);
     assertFalse("Two MethodDatas with different thrown arrays are not equal", _md.equals(_md2));

     //different enclosing datas
     _md2 = new MethodData("m", _publicMav2, tp, SymbolData.INT_TYPE, new VariableData[]{vd},
                           new String[] {"I throw this"}, SymbolData.NULL_TYPE, mdef);
     assertFalse("Two MethodDatas with different enclosing datas are not equal", _md.equals(_md2));
    
     //different parameters
     _md2 = new MethodData("m", _publicMav2, tp, SymbolData.INT_TYPE, new VariableData[]{vd, vd2},
                           new String[] {"I throw this"}, SymbolData.NULL_TYPE, mdef);
     assertFalse("Two MethodDatas with different variables are not equal", _md.equals(_md2));
     
     //same parameters, but in different order
     _md = new MethodData("m", _publicMav2, tp, SymbolData.INT_TYPE, new VariableData[]{vd2, vd},
                           new String[] {"I throw this"}, SymbolData.NULL_TYPE, mdef);
     assertFalse("Two MethodDatas with same parameters in different order are not equal", _md.equals(_md2));
     
    }
  }
}