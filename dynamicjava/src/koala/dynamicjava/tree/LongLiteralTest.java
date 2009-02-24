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

package koala.dynamicjava.tree;

import junit.framework.TestCase;
//import koala.dynamicjava.tree.*;

/**
 * JUnit tests for the koala.dynamicjava.tree.LongLiteralTest class.
 * This test class depends on static test methods written inside the
 * LongLiteralTest class.
 */ 
public class LongLiteralTest extends TestCase {
  
  /**
   * Test to make sure the testParse method correctly interprets various inputs
   * and processes them appropriately.
   */ 
  public void testLongLiteral()
  {
    LongLiteral ll;
    
    //Test parse refactored into JUnit
    ll = new LongLiteral("0x138");
    assertTrue("Parse 0x138", new Long("312").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("0470");
    assertTrue("Parse 0470", new Long("312").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("312");
    assertTrue("Parse 312", new Long("312").compareTo((Long)ll.getValue()) == 0);

    //Test parse long refactored into JUnit
    ll = new LongLiteral("0x138l");
    assertTrue("Parse 0x138l", new Long("312").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("0470l");
    assertTrue("Parse 0470l", new Long("312").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("312l");
    assertTrue("Parse 312l", new Long("312").compareTo((Long)ll.getValue()) == 0);    
    ll = new LongLiteral("0x138L");
    assertTrue("Parse 0x138L", new Long("312").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("0470L");
    assertTrue("Parse 0470L", new Long("312").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("312L");
    assertTrue("Parse 312L", new Long("312").compareTo((Long)ll.getValue()) == 0);    
    ll = new LongLiteral("0");
    assertTrue("Parse 0", new Long("0").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("0L");
    assertTrue("Parse 0L", new Long("0").compareTo((Long)ll.getValue()) == 0);    
    ll = new LongLiteral("0l");
    assertTrue("Parse 0l", new Long("0").compareTo((Long)ll.getValue()) == 0);    
    
    //Test parse hexadecimal refactored into JUnit
    ll = new LongLiteral("0x0");
    assertTrue("Parse 0", new Long("0").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("0x7fffffff");
    assertTrue("Parse 7fffffff", new Long("2147483647").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("0x80000000");
    assertTrue("Parse 80000000 Hexadecimal", new Long("2147483648").compareTo((Long)ll.getValue()) == 0);

    //Test parse octal refactored into JUnit
    ll = new LongLiteral("0");
    assertTrue("Parse 0 Octal", new Long("0").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("017777777777");
    assertTrue("Parse 17777777777 Octal", new Long("2147483647").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("020000000000");
    assertTrue("Parse 20000000000 Octal", new Long("2147483648").compareTo((Long)ll.getValue()) == 0);
    
    //Testing of negative numbers added
    ll = new LongLiteral("0xffffffffffffffff");
    assertTrue("Parse -1 Hexadecimal", new Long("-1").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("01777777777777777777777");
    assertTrue("Parse -1 Hexadecimal", new Long("-1").compareTo((Long)ll.getValue()) == 0);
    ll = new LongLiteral("-1");
    assertTrue("Parse -1", new Long("-1").compareTo((Long)ll.getValue()) == 0);
    
    }
}