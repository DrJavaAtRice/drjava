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

package koala.dynamicjava.util;

import koala.dynamicjava.interpreter.throwable.WrongVersionException;

import junit.framework.TestCase;
import java.lang.reflect.*;

/**
 * Tests the utility methods in the TigerUtilities class to
 * make sure they are working correctly.
 */
public class TigerUtilitiesTest extends TestCase {
  
  
  public void setUp() {
    TigerUtilities.resetVersion();    
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
   * Tests the ability to enable and disable the functionality of 1.5
   */
  public void testSetAndResetTigerEnabled() {
    TigerUtilities.setTigerEnabled(true);
    TigerUtilities.assertTigerEnabled("Tiger should be enabled");
    assertEquals("Tiger should be enabled",TigerUtilities.isTigerEnabled(),true);
    TigerUtilities.setTigerEnabled(false);
    assertEquals("Tiger should be disabled",TigerUtilities.isTigerEnabled(),false);
    
  }
  
  /**
   * Tests the isVarArgs method
   */
  public void testIsVarArgs() {
    try {    
      Method m1 = java.io.PrintStream.class.getMethod("printf", new Class[]{String.class, Object[].class});
      Method m2 = java.io.PrintStream.class.getMethod("println",new Class[]{ });
      //Don't run test if the user is only using 1.4
      if(TigerUtilities.isTigerEnabled()) {
        assertEquals("The method should have variable arguments",TigerUtilities.isVarArgs(m1),true);
        assertEquals("The method should not have variable arguments",TigerUtilities.isVarArgs(m2),false);   
      }
      
      TigerUtilities.setTigerEnabled(false);
      assertEquals("Tiger features are disabled, isVarArgs should return false",TigerUtilities.isVarArgs(m1),false);
      assertEquals("Tiger features are disabled, isVarArgs should return false",TigerUtilities.isVarArgs(m2),false);
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
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(int.class,Long.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(int.class,Double.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(int.class,Float.class),true);
    
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(double.class,Double.class),true);
    
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(float.class,Float.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(float.class,Double.class),true);
    
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(long.class,Long.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(long.class,Double.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(long.class,Float.class),true);
    
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(byte.class,Byte.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(byte.class,Integer.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(byte.class,Short.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(byte.class,Long.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(byte.class,Float.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(byte.class,Double.class),true);
    
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(char.class,Character.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(char.class,Integer.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(char.class,Long.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(char.class,Double.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(char.class,Float.class),true);
    
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(short.class,Short.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(short.class,Integer.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(short.class,Long.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(short.class,Double.class),true);
    assertEquals("Should be able to box primitive to reference type",TigerUtilities.boxesTo(short.class,Float.class),true);
    
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(int.class,Byte.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(int.class,Character.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(int.class,Short.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(int.class,Boolean.class),false);
    
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(double.class,Float.class),false);
    assertEquals("Should not be able to box primitive to reference type",TigerUtilities.boxesTo(double.class,Integer.class),false);
  }
}