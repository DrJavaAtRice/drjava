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

// A class created to help test the abstract class called MemberIdentifier
class AbstractMethodIDHelper extends AbstractMethodIdentifier{
  
  AbstractMethodIDHelper(String dc, String n, String t, String[] p){
    super(dc, n, t,p);
  }
  
}

public class AbstractMethodIdentifierTest extends TestCase {
  
  private String _declaringClassStr;
  private String _nameOfClassStr;
  private String _returnTypeStr;
  private String[] _parametersTypesStrs = {"type1","type2","type3"};
  private AbstractMethodIDHelper _absMethodIDHelper;
  private AbstractMethodIDHelper _absMethodIDHelper2;
  private AbstractMethodIDHelper _absMethodIDHelper3;
  /**
   * Create a new instance of this TestCase.
   * @param     String name
   */
  public AbstractMethodIdentifierTest(String name) {
    super(name);
  }
  
  /**
   * Initialize fields for each test.
   */
  protected void setUp() {
    _declaringClassStr = new String("declaringClassString");
    _nameOfClassStr = new String("nameOfClassString");
    _returnTypeStr = new String("returnTypeString");
    _absMethodIDHelper =  new AbstractMethodIDHelper(_declaringClassStr,_nameOfClassStr,_returnTypeStr,_parametersTypesStrs);
    _absMethodIDHelper2 =  new AbstractMethodIDHelper(_declaringClassStr,"someotherString",_returnTypeStr,_parametersTypesStrs);
    _absMethodIDHelper3 =  new AbstractMethodIDHelper(_declaringClassStr,_nameOfClassStr,_returnTypeStr,_parametersTypesStrs);
  }
  
  public void testGetParameters(){
    String[] p = _absMethodIDHelper.getParameters();
    assertTrue("The parameters should be of the same length",p.length==_parametersTypesStrs.length);
    for(int i=0;i<p.length;i++){
      assertTrue("Parameter: "+p[i]+" should have been equal to parameter: "+_parametersTypesStrs[i], p[i].equals(_parametersTypesStrs[i]));
    }
  }      
  
  
  //Testing with a null object
  public void testEquals(){
    assertFalse("Giving a null object should return false",_absMethodIDHelper.equals(null));
  }
  
  //Testing with an object other than an instance of AbstractMethodIdentifier
  public void testEquals2(){
    assertFalse("Giving an object that is not an instance of AbstractMethodIdentifier should return false",_absMethodIDHelper.equals(new String("SomeTestString")));
  }
  
  //Testing with an object which is an instance of AbstractMethodIdentifier but with different attributes for name, type and declaringClass
  public void testEquals3(){
    assertFalse("Giving an object that is an instance of AbstractMethodIdentifier but with not all equal attributes should return false",_absMethodIDHelper.equals(_absMethodIDHelper2));
  }
  //Testing with a an object that is an instance of AbstractMethodIdentifier with same value
  public void testEquals4(){
    assertTrue("Giving an object that is an instance of AbstractMethodIdentifier with same attributes should return true",_absMethodIDHelper.equals(_absMethodIDHelper3));
  }
}

