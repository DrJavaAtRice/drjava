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


/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */


package koala.dynamicjava.classfile;

import junit.framework.*;

public class ClassIdentifierTest extends TestCase {
  private ClassIdentifier _classIDf;
  private ClassIdentifier _classIDf2;
  private ClassIdentifier _classIDf3;
  private String _myTestString;
  /**
   * Create a new instance of this TestCase.
   * @param     String name
   */
  public ClassIdentifierTest(String name) {
    super(name);
  }
  
  /**
   * Initialize fields for each test.
   */
  protected void setUp() {
    _myTestString = new String("tstring");
    _classIDf = new ClassIdentifier(_myTestString);
    _classIDf2 = new ClassIdentifier(_myTestString);
    _classIDf3 = new ClassIdentifier("some other String");
  }
  
  public void testGetValue(){
    assertEquals(_myTestString,_classIDf.getValue());
  }

  //Testing with a null object
  public void testEquals(){
    assertFalse("Giving a null object should return false",_classIDf.equals(null));
  }
  
  //Testing with a an object other than an instance of ClassIdentifier
  public void testEquals2(){
    assertFalse("Giving an object that is not an instance of ClassIdentifier should return false",_classIDf.equals(_myTestString));
  }
  
  //Testing with a an object that is an instance of ClassIdentifier but different value
  public void testEquals3(){
    assertFalse("Giving an object that is an instance of ClassIdentifier but with different value should return false",_classIDf.equals(_classIDf3));
  }
  
    //Testing with a an object that is an instance of ClassIdentifier with same value
  public void testEquals4(){
    assertTrue("Giving an object that is an instance of ClassIdentifier with same value should return true",_classIDf.equals(_classIDf2));
  }
    
  //Hashcode of the name of the class and of the string in the value variable.
  //1588571558 comes from the String ClassIdentifier and _myTestString
   public void testHashCode(){
    assertEquals(1588571558,"ClassIdentifier".hashCode()+_classIDf.getValue().hashCode());
  }
}
  