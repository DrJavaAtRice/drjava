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

package edu.rice.cs.drjava.model.repl.newjvm;

import junit.framework.TestCase;
import java.lang.reflect.*;
import edu.rice.cs.plt.tuple.Pair;

/** Tests for InterpreterJVM.
  * <p>
  * @version $Id$
  */
public class InterpreterJVMTest extends TestCase {
  public static void xtestIsValidFieldName() {
    // only allow these formats:
    assertFalse(InterpreterJVM.isValidFieldName(""));
    assertFalse(InterpreterJVM.isValidFieldName(" "));
    assertFalse(InterpreterJVM.isValidFieldName("1"));
    // f
    assertTrue(InterpreterJVM.isValidFieldName("f"));
    assertTrue(InterpreterJVM.isValidFieldName("f1"));
    assertTrue(InterpreterJVM.isValidFieldName("_"));
    
    assertFalse(InterpreterJVM.isValidFieldName("f()"));
    assertFalse(InterpreterJVM.isValidFieldName("f(1)"));
    assertFalse(InterpreterJVM.isValidFieldName("f["));
    assertFalse(InterpreterJVM.isValidFieldName("f1["));
    assertFalse(InterpreterJVM.isValidFieldName("_["));
    assertFalse(InterpreterJVM.isValidFieldName("f[]"));
    assertFalse(InterpreterJVM.isValidFieldName("f1[]"));
    assertFalse(InterpreterJVM.isValidFieldName("_[]"));
    // f[1]
    assertTrue(InterpreterJVM.isValidFieldName("f[0]"));
    assertTrue(InterpreterJVM.isValidFieldName("f1[0]"));
    assertTrue(InterpreterJVM.isValidFieldName("_[0]"));
    assertFalse(InterpreterJVM.isValidFieldName("f[x]"));
    assertFalse(InterpreterJVM.isValidFieldName("f1[x]"));
    assertFalse(InterpreterJVM.isValidFieldName("_[x]"));
    assertFalse(InterpreterJVM.isValidFieldName("f[0]["));
    assertFalse(InterpreterJVM.isValidFieldName("f1[0]["));
    assertFalse(InterpreterJVM.isValidFieldName("_[0]["));
    assertFalse(InterpreterJVM.isValidFieldName("f[0][]"));
    assertFalse(InterpreterJVM.isValidFieldName("f1[0][]"));
    assertFalse(InterpreterJVM.isValidFieldName("_[0][]"));
    // f[1][2]
    assertTrue(InterpreterJVM.isValidFieldName("f[0][1]"));
    assertTrue(InterpreterJVM.isValidFieldName("f1[0][1]"));
    assertTrue(InterpreterJVM.isValidFieldName("_[0][1]"));
    // f[1] [2]
    assertTrue(InterpreterJVM.isValidFieldName("f[0] [1]"));
    assertTrue(InterpreterJVM.isValidFieldName("f1[0] [1]"));
    assertTrue(InterpreterJVM.isValidFieldName("_[0] [1]"));
    assertTrue(InterpreterJVM.isValidFieldName("f[0]  [1]"));
    assertTrue(InterpreterJVM.isValidFieldName("f1[0]  [1]"));
    assertTrue(InterpreterJVM.isValidFieldName("_[0]  [1]"));
    assertTrue(InterpreterJVM.isValidFieldName("f[0] \t[1]"));
    assertTrue(InterpreterJVM.isValidFieldName("f1[0] \t[1]"));
    assertTrue(InterpreterJVM.isValidFieldName("_[0] \t[1]"));
    
    assertFalse(InterpreterJVM.isValidFieldName("f."));
    assertFalse(InterpreterJVM.isValidFieldName("f1."));
    assertFalse(InterpreterJVM.isValidFieldName("_."));
    // o.f
    assertTrue(InterpreterJVM.isValidFieldName("f.x"));
    assertTrue(InterpreterJVM.isValidFieldName("f1.x"));
    assertTrue(InterpreterJVM.isValidFieldName("_.x"));
    assertTrue(InterpreterJVM.isValidFieldName("f.x1"));
    assertTrue(InterpreterJVM.isValidFieldName("f1.x1"));
    assertTrue(InterpreterJVM.isValidFieldName("_.x1"));
    assertTrue(InterpreterJVM.isValidFieldName("f._"));
    assertTrue(InterpreterJVM.isValidFieldName("f1._"));
    assertTrue(InterpreterJVM.isValidFieldName("_._"));
    
    assertFalse(InterpreterJVM.isValidFieldName("f[0]."));
    assertFalse(InterpreterJVM.isValidFieldName("f1[0]."));
    assertFalse(InterpreterJVM.isValidFieldName("_[0]."));
    assertFalse(InterpreterJVM.isValidFieldName("f[0] ."));
    assertFalse(InterpreterJVM.isValidFieldName("f1[0] ."));
    assertFalse(InterpreterJVM.isValidFieldName("_[0] ."));
    assertFalse(InterpreterJVM.isValidFieldName("f[0] \t."));
    assertFalse(InterpreterJVM.isValidFieldName("f1[0] \t."));
    assertFalse(InterpreterJVM.isValidFieldName("_[0] \t."));
    assertFalse(InterpreterJVM.isValidFieldName("f[0] [1]."));
    assertFalse(InterpreterJVM.isValidFieldName("f1[0] [1]."));
    assertFalse(InterpreterJVM.isValidFieldName("_[0] [1]."));
    assertFalse(InterpreterJVM.isValidFieldName("f[0]  [1]."));
    assertFalse(InterpreterJVM.isValidFieldName("f1[0]  [1]."));
    assertFalse(InterpreterJVM.isValidFieldName("_[0]  [1]."));
    assertFalse(InterpreterJVM.isValidFieldName("f[0] \t[1]."));
    assertFalse(InterpreterJVM.isValidFieldName("f1[0] \t[1]."));
    assertFalse(InterpreterJVM.isValidFieldName("_[0] \t[1]."));
    // o[1].f
    assertTrue(InterpreterJVM.isValidFieldName("f[0].f"));
    assertTrue(InterpreterJVM.isValidFieldName("f1[0].f"));
    assertTrue(InterpreterJVM.isValidFieldName("_[0].f"));
    assertTrue(InterpreterJVM.isValidFieldName("f[0] .f"));
    assertTrue(InterpreterJVM.isValidFieldName("f1[0] .f"));
    assertTrue(InterpreterJVM.isValidFieldName("_[0] .f"));
    assertTrue(InterpreterJVM.isValidFieldName("f[0]\t.f"));
    assertTrue(InterpreterJVM.isValidFieldName("f1[0]\t.f"));
    assertTrue(InterpreterJVM.isValidFieldName("_[0]\t.f"));
    // o[1][2].f
    assertTrue(InterpreterJVM.isValidFieldName("f[0][1].f"));
    assertTrue(InterpreterJVM.isValidFieldName("f1[0][1].f"));
    assertTrue(InterpreterJVM.isValidFieldName("_[0][1].f"));
    assertTrue(InterpreterJVM.isValidFieldName("f[0][1]. f"));
    assertTrue(InterpreterJVM.isValidFieldName("f1[0][1]. f"));
    assertTrue(InterpreterJVM.isValidFieldName("_[0][1]. f"));
    assertTrue(InterpreterJVM.isValidFieldName("f[0][1] .f"));
    assertTrue(InterpreterJVM.isValidFieldName("f1[0][1] .f"));
    assertTrue(InterpreterJVM.isValidFieldName("_[0][1] .f"));
    // o[1] [2].f
    assertTrue(InterpreterJVM.isValidFieldName("f[0] [1].f"));
    assertTrue(InterpreterJVM.isValidFieldName("f1[0] [1].f"));
    assertTrue(InterpreterJVM.isValidFieldName("_[0] [1].f"));
    assertTrue(InterpreterJVM.isValidFieldName("f[0]  [1].f"));
    assertTrue(InterpreterJVM.isValidFieldName("f1[0]  [1].f"));
    assertTrue(InterpreterJVM.isValidFieldName("_[0]  [1].f"));
    assertTrue(InterpreterJVM.isValidFieldName("f[0] \t[1].f"));
    assertTrue(InterpreterJVM.isValidFieldName("f1[0] \t[1].f"));
    assertTrue(InterpreterJVM.isValidFieldName("_[0] \t[1].f"));    
  }
  
  public static void xtestGetValidFieldType() {
    Struct g = new Struct();
    Struct f = new Struct(101, 102,
                          new int[] { 103, 104 },
                          new Integer[] { 105, 106 },
                          new int[][] { new int[] { 107, 108 }, new int[] { 109, 110, 111 } },
                          new Integer[][] { new Integer[] { 112, 113 }, new Integer[] { 114, 115, 116 } },
                          new Struct[] { g, g },
                          new Struct[][] { new Struct[] { g, g }, new Struct[] { g, g } }, g);
    Struct thisO = new Struct(1, 2,
                              new int[] { 3, 4 },
                              new Integer[] { 5, 6 },
                              new int[][] { new int[] { 7, 8 }, new int[] { 9, 10, 11 } },
                              new Integer[][] { new Integer[] { 12, 13 }, new Integer[] { 14, 15, 16 } },
                              new Struct[] { f, f },
                              new Struct[][] { new Struct[] { f, f }, new Struct[] { f, f } }, f);

    final Pair<Boolean,Class<?>> fail = new Pair<Boolean,Class<?>>(false,null);
    final Pair<Boolean,Class<?>> trueStruct = new Pair<Boolean,Class<?>>(true,Struct.class);
    final Pair<Boolean,Class<?>> trueIntArrArr = new Pair<Boolean,Class<?>>(true,int[][].class);
    final Pair<Boolean,Class<?>> trueIntArr = new Pair<Boolean,Class<?>>(true,int[].class);
    final Pair<Boolean,Class<?>> trueInt = new Pair<Boolean,Class<?>>(true,int.class);
    final Pair<Boolean,Class<?>> trueIntegerArrArr = new Pair<Boolean,Class<?>>(true,Integer[][].class);
    final Pair<Boolean,Class<?>> trueIntegerArr = new Pair<Boolean,Class<?>>(true,Integer[].class);
    final Pair<Boolean,Class<?>> trueInteger = new Pair<Boolean,Class<?>>(true,Integer.class);
    final Pair<Boolean,Class<?>> trueStructArr = new Pair<Boolean,Class<?>>(true,Struct[].class);
    final Pair<Boolean,Class<?>> trueStructArrArr = new Pair<Boolean,Class<?>>(true,Struct[][].class);
    
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("f", thisO.getClass(), thisO));
    // can't distinguish runtime type int/Integer
    // assertEquals(trueInt,           InterpreterJVM.getValidFieldType("i", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("i", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("j", thisO.getClass(), thisO));
    assertEquals(trueIntArr,        InterpreterJVM.getValidFieldType("ia", thisO.getClass(), thisO));
    assertEquals(trueIntegerArr,    InterpreterJVM.getValidFieldType("ja", thisO.getClass(), thisO));
    // can't distinguish runtime type int/Integer
    // assertEquals(trueInt,           InterpreterJVM.getValidFieldType("ia[0]", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("ia[0]", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("ja[0]", thisO.getClass(), thisO));
    assertEquals(trueIntArrArr,     InterpreterJVM.getValidFieldType("iaa", thisO.getClass(), thisO));
    assertEquals(trueIntegerArrArr, InterpreterJVM.getValidFieldType("jaa", thisO.getClass(), thisO));
    assertEquals(trueIntArr,        InterpreterJVM.getValidFieldType("iaa[0]", thisO.getClass(), thisO));
    assertEquals(trueIntegerArr,    InterpreterJVM.getValidFieldType("jaa[0]", thisO.getClass(), thisO));
    // can't distinguish runtime type int/Integer
    // assertEquals(trueInt,           InterpreterJVM.getValidFieldType("iaa[0][0]", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("iaa[0][0]", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("jaa[0][0]", thisO.getClass(), thisO));
    assertEquals(trueStructArr,     InterpreterJVM.getValidFieldType("sa", thisO.getClass(), thisO));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("sa[0]", thisO.getClass(), thisO));
    assertEquals(trueStructArrArr,  InterpreterJVM.getValidFieldType("saa", thisO.getClass(), thisO));
    assertEquals(trueStructArr,     InterpreterJVM.getValidFieldType("saa[0]", thisO.getClass(), thisO));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("saa[0][0]", thisO.getClass(), thisO));

    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("f.f", thisO.getClass(), thisO));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("f.f.f", thisO.getClass(), thisO));
    
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("f.i", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("f.j", thisO.getClass(), thisO));
    assertEquals(trueIntArr,        InterpreterJVM.getValidFieldType("f.ia", thisO.getClass(), thisO));
    assertEquals(trueIntegerArr,    InterpreterJVM.getValidFieldType("f.ja", thisO.getClass(), thisO));
    // can't distinguish runtime type int/Integer
    // assertEquals(trueInt,           InterpreterJVM.getValidFieldType("ia[0]", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("f.ia[0]", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("f.ja[0]", thisO.getClass(), thisO));
    assertEquals(trueIntArrArr,     InterpreterJVM.getValidFieldType("f.iaa", thisO.getClass(), thisO));
    assertEquals(trueIntegerArrArr, InterpreterJVM.getValidFieldType("f.jaa", thisO.getClass(), thisO));
    assertEquals(trueIntArr,        InterpreterJVM.getValidFieldType("f.iaa[0]", thisO.getClass(), thisO));
    assertEquals(trueIntegerArr,    InterpreterJVM.getValidFieldType("f.jaa[0]", thisO.getClass(), thisO));
    // can't distinguish runtime type int/Integer
    // assertEquals(trueInt,           InterpreterJVM.getValidFieldType("iaa[0][0]", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("f.iaa[0][0]", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("f.jaa[0][0]", thisO.getClass(), thisO));
    assertEquals(trueStructArr,     InterpreterJVM.getValidFieldType("f.sa", thisO.getClass(), thisO));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("f.sa[0]", thisO.getClass(), thisO));
    assertEquals(trueStructArrArr,  InterpreterJVM.getValidFieldType("f.saa", thisO.getClass(), thisO));
    assertEquals(trueStructArr,     InterpreterJVM.getValidFieldType("f.saa[0]", thisO.getClass(), thisO));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("f.saa[0][0]", thisO.getClass(), thisO));
    
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("sa[0].f", thisO.getClass(), thisO));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("sa[0].f.f", thisO.getClass(), thisO));
    
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("sa[0].i", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("sa[0].j", thisO.getClass(), thisO));
    assertEquals(trueIntArr,        InterpreterJVM.getValidFieldType("sa[0].ia", thisO.getClass(), thisO));
    assertEquals(trueIntegerArr,    InterpreterJVM.getValidFieldType("sa[0].ja", thisO.getClass(), thisO));
    // can't distinguish runtime type int/Integer
    // assertEquals(trueInt,           InterpreterJVM.getValidFieldType("ia[0]", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("sa[0].ia[0]", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("sa[0].ja[0]", thisO.getClass(), thisO));
    assertEquals(trueIntArrArr,     InterpreterJVM.getValidFieldType("sa[0].iaa", thisO.getClass(), thisO));
    assertEquals(trueIntegerArrArr, InterpreterJVM.getValidFieldType("sa[0].jaa", thisO.getClass(), thisO));
    assertEquals(trueIntArr,        InterpreterJVM.getValidFieldType("sa[0].iaa[0]", thisO.getClass(), thisO));
    assertEquals(trueIntegerArr,    InterpreterJVM.getValidFieldType("sa[0].jaa[0]", thisO.getClass(), thisO));
    // can't distinguish runtime type int/Integer
    // assertEquals(trueInt,           InterpreterJVM.getValidFieldType("iaa[0][0]", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("sa[0].iaa[0][0]", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("sa[0].jaa[0][0]", thisO.getClass(), thisO));
    assertEquals(trueStructArr,     InterpreterJVM.getValidFieldType("sa[0].sa", thisO.getClass(), thisO));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("sa[0].sa[0]", thisO.getClass(), thisO));
    assertEquals(trueStructArrArr,  InterpreterJVM.getValidFieldType("sa[0].saa", thisO.getClass(), thisO));
    assertEquals(trueStructArr,     InterpreterJVM.getValidFieldType("sa[0].saa[0]", thisO.getClass(), thisO));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("sa[0].saa[0][0]", thisO.getClass(), thisO));
    
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("sa[0].sa[0].f", thisO.getClass(), thisO));
    // can't distinguish runtime type int/Integer
    // assertEquals(trueInt,           InterpreterJVM.getValidFieldType("sa[0].sa[0].i", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("sa[0].sa[0].i", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("sa[0].sa[0].j", thisO.getClass(), thisO));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("sa[0].sa[0].f.f", thisO.getClass(), thisO));
    // this type is determined statically, so we can distinguish int/Integer
    assertEquals(trueInt,           InterpreterJVM.getValidFieldType("sa[0].sa[0].f.i", thisO.getClass(), thisO));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("sa[0].sa[0].f.j", thisO.getClass(), thisO));

    // array indices out of bounds
    assertEquals(fail, InterpreterJVM.getValidFieldType("ia[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("ja[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("iaa[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("jaa[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("iaa[0][3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("jaa[0][3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("saa[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("saa[0][3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("f.ia[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("f.ja[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("f.iaa[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("f.jaa[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("f.iaa[0][3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("f.jaa[0][3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("f.sa[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("f.saa[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("f.saa[0][3]", thisO.getClass(), thisO));
    
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[3].f", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[3].f.f", thisO.getClass(), thisO));
    
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[3].i", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[3].j", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[3].ia", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[3].ja", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[0].ia[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[0].ja[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[0].iaa[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[0].jaa[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[0].iaa[0][3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[0].jaa[0][3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[0].sa[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[0].saa[3]", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[0].saa[0][3]", thisO.getClass(), thisO));
    
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[0].sa[3].f", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[0].sa[3].i", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[0].sa[3].j", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[0].sa[3].f.f", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[0].sa[3].f.i", thisO.getClass(), thisO));
    assertEquals(fail, InterpreterJVM.getValidFieldType("sa[0].sa[3].f.j", thisO.getClass(), thisO));
  }
  
  public static void testGetValidFieldTypeAllStatic() {
    final Pair<Boolean,Class<?>> fail = new Pair<Boolean,Class<?>>(false,null);
    final Pair<Boolean,Class<?>> trueStruct = new Pair<Boolean,Class<?>>(true,Struct.class);
    final Pair<Boolean,Class<?>> trueIntArrArr = new Pair<Boolean,Class<?>>(true,int[][].class);
    final Pair<Boolean,Class<?>> trueIntArr = new Pair<Boolean,Class<?>>(true,int[].class);
    final Pair<Boolean,Class<?>> trueInt = new Pair<Boolean,Class<?>>(true,int.class);
    final Pair<Boolean,Class<?>> trueIntegerArrArr = new Pair<Boolean,Class<?>>(true,Integer[][].class);
    final Pair<Boolean,Class<?>> trueIntegerArr = new Pair<Boolean,Class<?>>(true,Integer[].class);
    final Pair<Boolean,Class<?>> trueInteger = new Pair<Boolean,Class<?>>(true,Integer.class);
    final Pair<Boolean,Class<?>> trueStructArr = new Pair<Boolean,Class<?>>(true,Struct[].class);
    final Pair<Boolean,Class<?>> trueStructArrArr = new Pair<Boolean,Class<?>>(true,Struct[][].class);
    
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("f", Struct.class, null));
    assertEquals(trueInt,           InterpreterJVM.getValidFieldType("i", Struct.class, null));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("j", Struct.class, null));
    assertEquals(trueIntArr,        InterpreterJVM.getValidFieldType("ia", Struct.class, null));
    assertEquals(trueIntegerArr,    InterpreterJVM.getValidFieldType("ja", Struct.class, null));
    assertEquals(trueInt,           InterpreterJVM.getValidFieldType("ia[0]", Struct.class, null));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("ja[0]", Struct.class, null));
    assertEquals(trueIntArrArr,     InterpreterJVM.getValidFieldType("iaa", Struct.class, null));
    assertEquals(trueIntegerArrArr, InterpreterJVM.getValidFieldType("jaa", Struct.class, null));
    assertEquals(trueIntArr,        InterpreterJVM.getValidFieldType("iaa[0]", Struct.class, null));
    assertEquals(trueIntegerArr,    InterpreterJVM.getValidFieldType("jaa[0]", Struct.class, null));
    assertEquals(trueInt,           InterpreterJVM.getValidFieldType("iaa[0][0]", Struct.class, null));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("jaa[0][0]", Struct.class, null));
    assertEquals(trueStructArr,     InterpreterJVM.getValidFieldType("sa", Struct.class, null));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("sa[0]", Struct.class, null));
    assertEquals(trueStructArrArr,  InterpreterJVM.getValidFieldType("saa", Struct.class, null));
    assertEquals(trueStructArr,     InterpreterJVM.getValidFieldType("saa[0]", Struct.class, null));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("saa[0][0]", Struct.class, null));

    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("f.f", Struct.class, null));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("f.f.f", Struct.class, null));
    
    assertEquals(trueInt,           InterpreterJVM.getValidFieldType("f.i", Struct.class, null));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("f.j", Struct.class, null));
    assertEquals(trueIntArr,        InterpreterJVM.getValidFieldType("f.ia", Struct.class, null));
    assertEquals(trueIntegerArr,    InterpreterJVM.getValidFieldType("f.ja", Struct.class, null));
    assertEquals(trueInt,           InterpreterJVM.getValidFieldType("ia[0]", Struct.class, null));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("f.ja[0]", Struct.class, null));
    assertEquals(trueIntArrArr,     InterpreterJVM.getValidFieldType("f.iaa", Struct.class, null));
    assertEquals(trueIntegerArrArr, InterpreterJVM.getValidFieldType("f.jaa", Struct.class, null));
    assertEquals(trueIntArr,        InterpreterJVM.getValidFieldType("f.iaa[0]", Struct.class, null));
    assertEquals(trueIntegerArr,    InterpreterJVM.getValidFieldType("f.jaa[0]", Struct.class, null));
    assertEquals(trueInt,           InterpreterJVM.getValidFieldType("iaa[0][0]", Struct.class, null));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("f.jaa[0][0]", Struct.class, null));
    assertEquals(trueStructArr,     InterpreterJVM.getValidFieldType("f.sa", Struct.class, null));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("f.sa[0]", Struct.class, null));
    assertEquals(trueStructArrArr,  InterpreterJVM.getValidFieldType("f.saa", Struct.class, null));
    assertEquals(trueStructArr,     InterpreterJVM.getValidFieldType("f.saa[0]", Struct.class, null));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("f.saa[0][0]", Struct.class, null));
    
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("sa[0].f", Struct.class, null));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("sa[0].f.f", Struct.class, null));
    
    assertEquals(trueInt,           InterpreterJVM.getValidFieldType("sa[0].i", Struct.class, null));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("sa[0].j", Struct.class, null));
    assertEquals(trueIntArr,        InterpreterJVM.getValidFieldType("sa[0].ia", Struct.class, null));
    assertEquals(trueIntegerArr,    InterpreterJVM.getValidFieldType("sa[0].ja", Struct.class, null));
    assertEquals(trueInt,           InterpreterJVM.getValidFieldType("ia[0]", Struct.class, null));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("sa[0].ja[0]", Struct.class, null));
    assertEquals(trueIntArrArr,     InterpreterJVM.getValidFieldType("sa[0].iaa", Struct.class, null));
    assertEquals(trueIntegerArrArr, InterpreterJVM.getValidFieldType("sa[0].jaa", Struct.class, null));
    assertEquals(trueIntArr,        InterpreterJVM.getValidFieldType("sa[0].iaa[0]", Struct.class, null));
    assertEquals(trueIntegerArr,    InterpreterJVM.getValidFieldType("sa[0].jaa[0]", Struct.class, null));
    assertEquals(trueInt,           InterpreterJVM.getValidFieldType("iaa[0][0]", Struct.class, null));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("sa[0].jaa[0][0]", Struct.class, null));
    assertEquals(trueStructArr,     InterpreterJVM.getValidFieldType("sa[0].sa", Struct.class, null));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("sa[0].sa[0]", Struct.class, null));
    assertEquals(trueStructArrArr,  InterpreterJVM.getValidFieldType("sa[0].saa", Struct.class, null));
    assertEquals(trueStructArr,     InterpreterJVM.getValidFieldType("sa[0].saa[0]", Struct.class, null));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("sa[0].saa[0][0]", Struct.class, null));
    
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("sa[0].sa[0].f", Struct.class, null));
    assertEquals(trueInt,           InterpreterJVM.getValidFieldType("sa[0].sa[0].i", Struct.class, null));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("sa[0].sa[0].j", Struct.class, null));
    assertEquals(trueStruct,        InterpreterJVM.getValidFieldType("sa[0].sa[0].f.f", Struct.class, null));
    assertEquals(trueInt,           InterpreterJVM.getValidFieldType("sa[0].sa[0].f.i", Struct.class, null));
    assertEquals(trueInteger,       InterpreterJVM.getValidFieldType("sa[0].sa[0].f.j", Struct.class, null));
  }

  
  public static class Struct {
    private int i;
    private Integer j;
    private int[] ia;
    private Integer[] ja;
    private int[][] iaa;
    private Integer[][] jaa;
    private Struct[] sa;
    private Struct[][] saa;
    private Struct f;

    public Struct() {
      this(0, 0, new int[0], new Integer[0], new int[][] { }, new Integer[][] { },
           new Struct[0], new Struct[][] { }, null);
    }
    public Struct(int _i, Integer _j, int[] _ia, Integer[] _ja,
                  int[][] _iaa, Integer[][] _jaa, Struct[] _sa,
                  Struct[][] _saa, Struct _f) {
      i = _i;
      j = _j;
      ia = _ia;
      ja = _ja;
      iaa = _iaa;
      jaa = _jaa;
      sa = _sa;
      saa = _saa;
      f = _f;
    }
  }
}
