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

package koala.dynamicjava.util;

import koala.dynamicjava.interpreter.error.WrongVersionException;

import java.lang.reflect.*;

/**
 * Tests the utility methods in the TigerUtilities class to
 * make sure they are working correctly.
 */
public class TigerUtilitiesTest extends DynamicJavaTestCase {
  
  
  public void setUp() {
    setTigerEnabled(true);    
  }
  
  public void tearDown() {
    TigerUtilities.resetVersion();    
  }
  
  /**
   * Tests the resetVersion method 
   */
  public void testResetVersion() {
    TigerUtilities.resetVersion();
    assertEquals("Did not reset runtime version correctly",TigerUtilities.VERSION>=1.5,TigerUtilities.isTigerEnabled());
  }
   
  /**
   * Tests the isVarArgs method
   */
  public void testIsVarArgs() {
    try {    
      Method m1 = java.io.PrintStream.class.getMethod("printf", new Class<?>[]{String.class, Object[].class});
      Method m2 = java.io.PrintStream.class.getMethod("println",new Class<?>[]{ });
      assertEquals("The method should have variable arguments",TigerUtilities.isVarArgs(m1),true);
      assertEquals("The method should not have variable arguments",TigerUtilities.isVarArgs(m2),false);   
      
      
      TigerUtilities.setTigerEnabled(false);
      assertEquals("Tiger features are disabled, isVarArgs should return false",TigerUtilities.isVarArgs(m1),false);
      assertEquals("Tiger features are disabled, isVarArgs should return false",TigerUtilities.isVarArgs(m2),false);
      TigerUtilities.setTigerEnabled(true);
    }
    catch(NoSuchMethodException e) {
      throw new RuntimeException(e.toString());
    }
    catch(WrongVersionException e) {
      throw new RuntimeException("Should not have thrown a Wrong Version Exception");
    }
  }
  
  /**
   * Tests the correspondingBoxingType method
   */
  public void testCorrespondingBoxingType() {
    assertEquals("Should have returned boxed Boolean class",TigerUtilities.correspondingBoxingType(boolean.class),Boolean.class); 
    assertEquals("Should have returned boxed Byte class",TigerUtilities.correspondingBoxingType(byte.class),Byte.class);
    assertEquals("Should have returned boxed Character class",TigerUtilities.correspondingBoxingType(char.class),Character.class);
    assertEquals("Should have returned boxed Short class",TigerUtilities.correspondingBoxingType(short.class),Short.class);
    assertEquals("Should have returned boxed Long class",TigerUtilities.correspondingBoxingType(long.class),Long.class);
    assertEquals("Should have returned boxed Integer class",TigerUtilities.correspondingBoxingType(int.class),Integer.class);
    assertEquals("Should have returned boxed Float class",TigerUtilities.correspondingBoxingType(float.class),Float.class);
    assertEquals("Should have returned boxed Double class",TigerUtilities.correspondingBoxingType(double.class),Double.class);
    
    ///**///Eventually add a test that it throws an exception if given a class that is not a primitive type or a boxing type
    
  }
  
  /**
   * Tests the correspondingPrimType method
   */
  public void testCorrespondingPrimType() {
    assertEquals("Should have returned primitive boolean class",TigerUtilities.correspondingPrimType(Boolean.class),boolean.class);
    assertEquals("Should have returned primitive byte class",TigerUtilities.correspondingPrimType(Byte.class),byte.class);
    assertEquals("Should have returned primitive char class",TigerUtilities.correspondingPrimType(Character.class),char.class);
    assertEquals("Should have returned primitive short class",TigerUtilities.correspondingPrimType(Short.class),short.class);
    assertEquals("Should have returned primitive long class",TigerUtilities.correspondingPrimType(Long.class),long.class);
    assertEquals("Should have returned primitive int class",TigerUtilities.correspondingPrimType(Integer.class),int.class);
    assertEquals("Should have returned primitive float class",TigerUtilities.correspondingPrimType(Float.class),float.class);
    assertEquals("Should have returned primitive double class",TigerUtilities.correspondingPrimType(Double.class),double.class);
  }
  
  /**
   * Tests the isBoxingType method
   */
  public void testIsBoxingType() {
    assertEquals("Should be a boxing type",TigerUtilities.isBoxingType(Boolean.class),true);
    assertEquals("Should be a boxing type",TigerUtilities.isBoxingType(Byte.class),true);
    assertEquals("Should be a boxing type",TigerUtilities.isBoxingType(Character.class),true);
    assertEquals("Should be a boxing type",TigerUtilities.isBoxingType(Short.class),true);
    assertEquals("Should be a boxing type",TigerUtilities.isBoxingType(Long.class),true);
    assertEquals("Should be a boxing type",TigerUtilities.isBoxingType(Integer.class),true);
    assertEquals("Should be a boxing type",TigerUtilities.isBoxingType(Float.class),true);
    assertEquals("Should be a boxing type",TigerUtilities.isBoxingType(Double.class),true);
    assertEquals("Should not be a boxing type",TigerUtilities.isBoxingType(String.class),false);
  }
   
  /**
   * Tests the isIntegralType method
   */
  public void testIsIntegralType() {
   assertEquals("Should be an integral type",TigerUtilities.isIntegralType(int.class),true); 
   assertEquals("Should be an integral type",TigerUtilities.isIntegralType(Integer.class),true);
   assertEquals("Should be an integral type",TigerUtilities.isIntegralType(short.class),true);
   assertEquals("Should be an integral type",TigerUtilities.isIntegralType(Short.class),true);
   assertEquals("Should be an integral type",TigerUtilities.isIntegralType(long.class),true);
   assertEquals("Should be an integral type",TigerUtilities.isIntegralType(Long.class),true);
   assertEquals("Should be an integral type",TigerUtilities.isIntegralType(byte.class),true);
   assertEquals("Should be an integral type",TigerUtilities.isIntegralType(Byte.class),true);
   assertEquals("Should be an integral type",TigerUtilities.isIntegralType(char.class),true);
   assertEquals("Should be an integral type",TigerUtilities.isIntegralType(Character.class),true);
   assertEquals("Should not be an integral type",TigerUtilities.isIntegralType(double.class),false);
   assertEquals("Should not be an integral type",TigerUtilities.isIntegralType(Double.class),false);
   assertEquals("Should not be an integral type",TigerUtilities.isIntegralType(float.class),false);
   assertEquals("Should not be an integral type",TigerUtilities.isIntegralType(Float.class),false);
   assertEquals("Should not be an integral type",TigerUtilities.isIntegralType(boolean.class),false);
   assertEquals("Should not be an integral type",TigerUtilities.isIntegralType(Boolean.class),false);
   assertEquals("Should not be an integral type",TigerUtilities.isIntegralType(String.class),false);
  }
  
  /**
   * Tests the boxesTo method
   */
  public void testBoxesTo() {
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(boolean.class,Boolean.class),true);
    
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(int.class,Integer.class),true);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(int.class,Long.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(int.class,Double.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(int.class,Float.class),false);
    
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(double.class,Double.class),true);
    
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(float.class,Float.class),true);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(float.class,Double.class),false);
    
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(long.class,Long.class),true);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(long.class,Double.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(long.class,Float.class),false);
    
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(byte.class,Byte.class),true);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(byte.class,Integer.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(byte.class,Short.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(byte.class,Long.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(byte.class,Float.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(byte.class,Double.class),false);
    
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(char.class,Character.class),true);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(char.class,Integer.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(char.class,Long.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(char.class,Double.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(char.class,Float.class),false);
    
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(short.class,Short.class),true);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(short.class,Integer.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(short.class,Long.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(short.class,Double.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(short.class,Float.class),false);
    
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(int.class,Byte.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(int.class,Character.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(int.class,Short.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(int.class,Boolean.class),false);
    
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(double.class,Float.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(double.class,Integer.class),false);
  }
}