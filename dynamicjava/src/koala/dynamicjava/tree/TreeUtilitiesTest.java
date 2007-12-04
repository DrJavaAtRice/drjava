/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

package koala.dynamicjava.tree;

import junit.framework.TestCase;
//import koala.dynamicjava.tree.*;

/**
 * JUnit tests for the koala.dynamicjava.tree.TreeUtilities class
 */ 
public class TreeUtilitiesTest extends TestCase {
  

  /**
   * Simple tests for the classToTypeName method.
   * There is one test for each case in the method.
   * It is assumed that the output of this method should be the same as if
   * a koala.dynamicjava.tree.TypeName was manually created.
   */ 
  public void testClassToType() {
    assertEquals("",true,new IntTypeName().
                   equals(TreeUtilities.classToTypeName(int.class)));
    assertEquals("",true,new DoubleTypeName().
                   equals(TreeUtilities.classToTypeName(double.class)));
    assertEquals("",true,new LongTypeName().
                   equals(TreeUtilities.classToTypeName(long.class)));
    assertEquals("",true,new FloatTypeName().
                   equals(TreeUtilities.classToTypeName(float.class)));
    assertEquals("",true,new CharTypeName().
                   equals(TreeUtilities.classToTypeName(char.class)));
    assertEquals("",true,new ByteTypeName().
                   equals(TreeUtilities.classToTypeName(byte.class)));
    assertEquals("",true,new ShortTypeName().
                   equals(TreeUtilities.classToTypeName(short.class)));
    assertEquals("",true,new BooleanTypeName().
                   equals(TreeUtilities.classToTypeName(boolean.class)));
    assertEquals("",true,new VoidTypeName().
                   equals(TreeUtilities.classToTypeName(void.class)));
    assertEquals("",true,new ArrayTypeName(new IntTypeName(),1).
                   equals(TreeUtilities.classToTypeName(int[].class)));
    assertEquals("",true,new ReferenceTypeName(Integer.class.getName()).
                   equals(TreeUtilities.classToTypeName(Integer.class)));
  }
  
}
