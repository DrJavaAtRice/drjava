/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu)
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

package edu.rice.cs.javalanglevels;

import edu.rice.cs.javalanglevels.tree.*;
import java.util.*;

import junit.framework.TestCase;

/**
 * Represents the data for a given method or block.
 */
public abstract class BodyData extends Data{
  
  public BodyData(Data outerData) {
    super(outerData);
  }
  
  /**
   * Return the enclosing SymbolData, corresponding to the enclosing class.
   * Note-this may be several layers up the tree.
   */
  public SymbolData getSymbolData() {
    return _outerData.getSymbolData();//_enclosingData.get(0).getSymbolData();
  }
  
  /**
   * Will return this, if it is a method data, or the enclosing method
   * data if this is a block data.
   */
  public abstract MethodData getMethodData();
  
  /**
   * True if this is a method data.
   */
  public abstract boolean isMethodData();
  
  
  
   /**
   * A JUnit test case class for BodyData.  Verifies that methods work as expected.
   */
  public static class BodyDataTest extends TestCase {
    
    private BodyData _bd1;
    private BodyData _bd2;
    
    private SymbolData _sd1;
    private SymbolData _sd2;
        
    public BodyDataTest() {
      this("");
    }
    public BodyDataTest(String name) {
      super(name);
    }
    
    public void testGetThis() {
      //getThis should be able to go up the tree correctly.
      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _bd1 = new BlockData(_sd2);
      _bd2 = new BlockData(_bd1);
      _sd2.setSuperClass(_sd1);
      assertEquals("Should return _sd2", _sd2, _bd2.getSymbolData());
    }
  }
}