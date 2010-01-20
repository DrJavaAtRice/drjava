/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (javaplt@rice.edu)
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

package edu.rice.cs.drjava.config;

import edu.rice.cs.drjava.model.MultiThreadedTestCase;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.util.*;

import java.io.*;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/**
 * Tests for the variables and language constructs that can be used in external processes.
 * @author Mathias Ricken
 */
public class DrJavaPropertySetupTest extends MultiThreadedTestCase {
  public final String PS = File.pathSeparator; // path separator
  public final String FS = File.separator; // path separator
  public final String TMPDIR = System.getProperty("java.io.tmpdir")+
      ((System.getProperty("java.io.tmpdir").endsWith(File.separator))?"":File.separator);
  public static final java.util.Random _r = new java.util.Random();
  public void setUp() throws Exception {
    super.setUp();
    DrJavaPropertySetup.setup();
  }
  public void tearDown() throws Exception {
    super.tearDown();
  }
  public void testArithmetic() throws CloneNotSupportedException {
    PropertyMaps pm = PropertyMaps.TEMPLATE.clone();
    DrJavaProperty p;
    
    // add
    p = pm.getProperty("Misc","add");
    assertTrue(p.getCurrent(pm).startsWith("(add Error"));
    p.setAttribute("op1","1");
    assertTrue(p.getCurrent(pm).startsWith("(add Error"));
    p.resetAttributes();
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(add Error"));
    p.setAttribute("op1","1");
    p.setAttribute("op2","2");
    assertEquals("3",p.getCurrent(pm));
    p.setAttribute("op1","30");
    p.setAttribute("op2","2");
    assertEquals("32",p.getCurrent(pm));
    p.setAttribute("op1","30");
    p.setAttribute("op2","1.23");
    assertEquals("31.23",p.getCurrent(pm));
    p.setAttribute("op1","x");
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(add Error"));
    p.setAttribute("op1","2");
    p.setAttribute("op2","x");
    assertTrue(p.getCurrent(pm).startsWith("(add Error"));
    
    // sub
    p = pm.getProperty("Misc","sub");
    assertTrue(p.getCurrent(pm).startsWith("(sub Error"));
    p.setAttribute("op1","1"); 
    assertTrue(p.getCurrent(pm).startsWith("(sub Error"));
    p.resetAttributes();
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(sub Error"));
    p.setAttribute("op1","1");
    p.setAttribute("op2","2");
    assertEquals("-1",p.getCurrent(pm));
    p.setAttribute("op1","30");
    p.setAttribute("op2","2");
    assertEquals("28",p.getCurrent(pm));
    p.setAttribute("op1","30.123");
    p.setAttribute("op2","2.1");
    assertEquals("28.023",p.getCurrent(pm));
    p.setAttribute("op1","x");
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(sub Error"));
    p.setAttribute("op1","2");
    p.setAttribute("op2","x");
    assertTrue(p.getCurrent(pm).startsWith("(sub Error"));
    
    // mul
    p = pm.getProperty("Misc","mul");
    assertTrue(p.getCurrent(pm).startsWith("(mul Error"));
    p.setAttribute("op1","1");
    assertTrue(p.getCurrent(pm).startsWith("(mul Error"));
    p.resetAttributes();
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(mul Error"));
    p.setAttribute("op1","3");
    p.setAttribute("op2","4");
    assertEquals("12",p.getCurrent(pm));
    p.setAttribute("op1","30");
    p.setAttribute("op2","-2");
    assertEquals("-60",p.getCurrent(pm));
    p.setAttribute("op1","30.2");
    p.setAttribute("op2","3");
    assertEquals("90.6",p.getCurrent(pm));
    p.setAttribute("op1","x");
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(mul Error"));
    p.setAttribute("op1","2");
    p.setAttribute("op2","x");
    assertTrue(p.getCurrent(pm).startsWith("(mul Error"));
    
    // div
    p = pm.getProperty("Misc","div");
    assertTrue(p.getCurrent(pm).startsWith("(div Error"));
    p.setAttribute("op1","1");
    assertTrue(p.getCurrent(pm).startsWith("(div Error"));
    p.resetAttributes();
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(div Error"));
    p.setAttribute("op1","12");
    p.setAttribute("op2","6");
    assertEquals("2",p.getCurrent(pm));
    p.setAttribute("op1","30");
    p.setAttribute("op2","-2");
    assertEquals("-15",p.getCurrent(pm));
    p.setAttribute("op1","-90.6");
    p.setAttribute("op2","-3");
    assertEquals("30.2",p.getCurrent(pm));
    p.setAttribute("op1","x");
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(div Error"));
    p.setAttribute("op1","2");
    p.setAttribute("op2","x");
    assertTrue(p.getCurrent(pm).startsWith("(div Error"));
    
    // not
    p = pm.getProperty("Misc","not");
    assertTrue(p.getCurrent(pm).startsWith("(not Error"));
    p.setAttribute("op","true");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op","false");
    assertEquals("true",p.getCurrent(pm));
    p.setAttribute("op","x"); // anything but "true" counts as false as per new Boolean("x")
    assertEquals("true",p.getCurrent(pm));
    p.setAttribute("op","2");
    assertEquals("true",p.getCurrent(pm));

    // gt
    p = pm.getProperty("Misc","gt");
    assertTrue(p.getCurrent(pm).startsWith("(gt Error"));
    p.setAttribute("op1","1");
    assertTrue(p.getCurrent(pm).startsWith("(gt Error"));
    p.resetAttributes();
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(gt Error"));
    p.setAttribute("op1","-1.123");
    p.setAttribute("op2","2");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","2");
    p.setAttribute("op2","-1.123");
    assertEquals("true",p.getCurrent(pm));
    p.setAttribute("op1","30");
    p.setAttribute("op2","30");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","x");
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(gt Error"));
    p.setAttribute("op1","2");
    p.setAttribute("op2","x");
    assertTrue(p.getCurrent(pm).startsWith("(gt Error"));

    // gte
    p = pm.getProperty("Misc","gte");
    assertTrue(p.getCurrent(pm).startsWith("(gte Error"));
    p.setAttribute("op1","1");
    assertTrue(p.getCurrent(pm).startsWith("(gte Error"));
    p.resetAttributes();
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(gte Error"));
    p.setAttribute("op1","-1.123");
    p.setAttribute("op2","2");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","2");
    p.setAttribute("op2","-1.123");
    assertEquals("true",p.getCurrent(pm));
    p.setAttribute("op1","30");
    p.setAttribute("op2","30");
    assertEquals("true",p.getCurrent(pm));
    p.setAttribute("op1","x");
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(gte Error"));
    p.setAttribute("op1","2");
    p.setAttribute("op2","x");
    assertTrue(p.getCurrent(pm).startsWith("(gte Error"));

    // lt
    p = pm.getProperty("Misc","lt");
    assertTrue(p.getCurrent(pm).startsWith("(lt Error"));
    p.setAttribute("op1","1");
    assertTrue(p.getCurrent(pm).startsWith("(lt Error"));
    p.resetAttributes();
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(lt Error"));
    p.setAttribute("op1","-1.123");
    p.setAttribute("op2","2");
    assertEquals("true",p.getCurrent(pm));
    p.setAttribute("op1","2");
    p.setAttribute("op2","-1.123");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","30");
    p.setAttribute("op2","30");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","x");
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(lt Error"));
    p.setAttribute("op1","2");
    p.setAttribute("op2","x");
    assertTrue(p.getCurrent(pm).startsWith("(lt Error"));

    // lte
    p = pm.getProperty("Misc","lte");
    assertTrue(p.getCurrent(pm).startsWith("(lte Error"));
    p.setAttribute("op1","1");
    assertTrue(p.getCurrent(pm).startsWith("(lte Error"));
    p.resetAttributes();
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(lte Error"));
    p.setAttribute("op1","-1.123");
    p.setAttribute("op2","2");
    assertEquals("true",p.getCurrent(pm));
    p.setAttribute("op1","2");
    p.setAttribute("op2","-1.123");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","30");
    p.setAttribute("op2","30");
    assertEquals("true",p.getCurrent(pm));
    p.setAttribute("op1","x");
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(lte Error"));
    p.setAttribute("op1","2");
    p.setAttribute("op2","x");
    assertTrue(p.getCurrent(pm).startsWith("(lte Error"));

    // eq
    p = pm.getProperty("Misc","eq");
    assertTrue(p.getCurrent(pm).startsWith("(eq Error"));
    p.setAttribute("op1","1");
    assertTrue(p.getCurrent(pm).startsWith("(eq Error"));
    p.resetAttributes();
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(eq Error"));
    p.setAttribute("op1","-1.123");
    p.setAttribute("op2","2");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","2");
    p.setAttribute("op2","2");
    assertEquals("true",p.getCurrent(pm));
    p.setAttribute("op1","xyz");
    p.setAttribute("op2","xyz");
    assertEquals("true",p.getCurrent(pm));
    p.setAttribute("op1","x");
    p.setAttribute("op2","2");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","2");
    p.setAttribute("op2","x");
    assertEquals("false",p.getCurrent(pm));

    // neq
    p = pm.getProperty("Misc","neq");
    assertTrue(p.getCurrent(pm).startsWith("(neq Error"));
    p.setAttribute("op1","1");
    assertTrue(p.getCurrent(pm).startsWith("(neq Error"));
    p.resetAttributes();
    p.setAttribute("op2","2");
    assertTrue(p.getCurrent(pm).startsWith("(neq Error"));
    p.setAttribute("op1","-1.123");
    p.setAttribute("op2","2");
    assertEquals("true",p.getCurrent(pm));
    p.setAttribute("op1","2");
    p.setAttribute("op2","2");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","xyz");
    p.setAttribute("op2","xyz");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","x");
    p.setAttribute("op2","2");
    assertEquals("true",p.getCurrent(pm));
    p.setAttribute("op1","2");
    p.setAttribute("op2","x");
    assertEquals("true",p.getCurrent(pm));

    // and
    p = pm.getProperty("Misc","and");
    assertTrue(p.getCurrent(pm).startsWith("(and Error"));
    p.setAttribute("op1","true");
    assertTrue(p.getCurrent(pm).startsWith("(and Error"));
    p.resetAttributes();
    p.setAttribute("op2","true");
    assertTrue(p.getCurrent(pm).startsWith("(and Error"));
    p.setAttribute("op1","true");
    p.setAttribute("op2","true");
    assertEquals("true",p.getCurrent(pm));
    p.setAttribute("op1","true");
    p.setAttribute("op2","false");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","false");
    p.setAttribute("op2","true");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","false");
    p.setAttribute("op2","false");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","x"); // anything but "true" counts as false as per new Boolean("x")
    p.setAttribute("op2","2");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","2");
    p.setAttribute("op2","x");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","true");
    p.setAttribute("op2","x");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","2");
    p.setAttribute("op2","true");
    assertEquals("false",p.getCurrent(pm));

    // or
    p = pm.getProperty("Misc","or");
    assertTrue(p.getCurrent(pm).startsWith("(or Error"));
    p.setAttribute("op1","true");
    assertTrue(p.getCurrent(pm).startsWith("(or Error"));
    p.resetAttributes();
    p.setAttribute("op2","true");
    assertTrue(p.getCurrent(pm).startsWith("(or Error"));
    p.setAttribute("op1","true");
    p.setAttribute("op2","true");
    assertEquals("true",p.getCurrent(pm));
    p.setAttribute("op1","true");
    p.setAttribute("op2","false");
    assertEquals("true",p.getCurrent(pm));
    p.setAttribute("op1","false");
    p.setAttribute("op2","true");
    assertEquals("true",p.getCurrent(pm));
    p.setAttribute("op1","false");
    p.setAttribute("op2","false");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","x"); // anything but "true" counts as false as per new Boolean("x")
    p.setAttribute("op2","2");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","2");
    p.setAttribute("op2","x");
    assertEquals("false",p.getCurrent(pm));
    p.setAttribute("op1","true");
    p.setAttribute("op2","x");
    assertEquals("true",p.getCurrent(pm));
    p.setAttribute("op1","2");
    p.setAttribute("op2","true");
    assertEquals("true",p.getCurrent(pm));
  }
  public void testString() throws CloneNotSupportedException {
    PropertyMaps pm = PropertyMaps.TEMPLATE.clone();
    DrJavaProperty p;
    
    // strlen
    p = pm.getProperty("Misc","strlen");
    assertTrue(p.getCurrent(pm).startsWith("(strlen Error"));
    p.setAttribute("op","abc");
    assertEquals("3",p.getCurrent(pm));
    p.setAttribute("op","");
    assertEquals("0",p.getCurrent(pm));
    p.setAttribute("op","1234567890");
    assertEquals("10",p.getCurrent(pm));
    
    // replace.string
    p = pm.getProperty("Misc","replace.string");
    assertTrue(p.getCurrent(pm).startsWith("(replace.string Error"));
    p.setAttribute("text","");
    assertTrue(p.getCurrent(pm).startsWith("(replace.string Error"));
    p.resetAttributes();
    p.setAttribute("old","abc");
    assertTrue(p.getCurrent(pm).startsWith("(replace.string Error"));
    p.resetAttributes();
    p.setAttribute("new","123");
    assertTrue(p.getCurrent(pm).startsWith("(replace.string Error"));
    p.resetAttributes();
    p.setAttribute("text","");
    p.setAttribute("old","abc");
    assertTrue(p.getCurrent(pm).startsWith("(replace.string Error"));
    p.resetAttributes();
    p.setAttribute("text","");
    p.setAttribute("new","123");
    assertTrue(p.getCurrent(pm).startsWith("(replace.string Error"));
    p.resetAttributes();
    p.setAttribute("old","abc");
    p.setAttribute("new","123");
    assertTrue(p.getCurrent(pm).startsWith("(replace.string Error"));
    p.setAttribute("text","");
    assertEquals("",p.getCurrent(pm));
    p.setAttribute("text","abc");
    assertEquals("123",p.getCurrent(pm));
    p.setAttribute("text","xyzabcdef");
    assertEquals("xyz123def",p.getCurrent(pm));
    p.setAttribute("text","abc111abc222");
    assertEquals("123111123222",p.getCurrent(pm));
    p.setAttribute("text","111.*222");
    p.setAttribute("old",".*"); // regular expressions are not enabled; they are escaped
    assertEquals("111123222",p.getCurrent(pm));
  }
  
  public void testList() throws CloneNotSupportedException {
    PropertyMaps pm = PropertyMaps.TEMPLATE.clone();
    DrJavaProperty p;
    
    // count
    p = pm.getProperty("Misc","count");
    assertTrue(p.getCurrent(pm).startsWith("(count Error"));
    p.setAttribute("list","abc");
    assertEquals("1",p.getCurrent(pm));
    p.setAttribute("list","");
    assertEquals("0",p.getCurrent(pm));
    p.setAttribute("list","abc" + PS + "def");
    assertEquals("2",p.getCurrent(pm));
    p.setAttribute("list",PS + "abc" + PS + "def");
    assertEquals("3",p.getCurrent(pm));
    p.setAttribute("list",PS + "abc" + PS + "def" + PS);
    assertEquals("4",p.getCurrent(pm));
    p.setAttribute("list",PS + "abc" + PS + "def" + PS+PS);
    assertEquals("5",p.getCurrent(pm));
    p.setAttribute("list",PS+PS + "abc" + PS + "def" + PS+PS);
    assertEquals("6",p.getCurrent(pm));
    p.setAttribute("list",PS+PS + "abc" + PS+PS + "def" + PS+PS);
    assertEquals("7",p.getCurrent(pm));
    
    p.setAttribute("list","abc 123");
    assertEquals("1",p.getCurrent(pm));
    p.setAttribute("list","");
    assertEquals("0",p.getCurrent(pm));
    p.setAttribute("list","abc 123" + PS + "def 456");
    assertEquals("2",p.getCurrent(pm));
    p.setAttribute("list",PS + "abc 123" + PS + "def 456");
    assertEquals("3",p.getCurrent(pm));
    p.setAttribute("list",PS + "abc 123" + PS + "def 456" + PS);
    assertEquals("4",p.getCurrent(pm));
    p.setAttribute("list",PS + "abc 123" + PS + "def 456" + PS+PS);
    assertEquals("5",p.getCurrent(pm));
    p.setAttribute("list",PS+PS + "abc 123" + PS + "def 456" + PS+PS);
    assertEquals("6",p.getCurrent(pm));
    p.setAttribute("list",PS+PS + "abc 123" + PS+PS + "def 456" + PS+PS);
    assertEquals("7",p.getCurrent(pm));
    
    p.setAttribute("sep"," ");
    p.setAttribute("list","abc" + PS + "def");
    assertEquals("1",p.getCurrent(pm));
    p.setAttribute("list","");
    assertEquals("0",p.getCurrent(pm));
    p.setAttribute("list","abc def");
    assertEquals("2",p.getCurrent(pm));
    p.setAttribute("list"," abc def");
    assertEquals("3",p.getCurrent(pm));
    p.setAttribute("list"," abc def ");
    assertEquals("4",p.getCurrent(pm));
    p.setAttribute("list"," abc def  ");
    assertEquals("5",p.getCurrent(pm));
    p.setAttribute("list","  abc def  ");
    assertEquals("6",p.getCurrent(pm));
    p.setAttribute("list","  abc  def  ");
    assertEquals("7",p.getCurrent(pm));
    
    // sublist
    p = pm.getProperty("Misc","sublist");
    assertTrue(p.getCurrent(pm).startsWith("(sublist Error"));
    p.resetAttributes();
    p.setAttribute("list","");
    assertTrue(p.getCurrent(pm).startsWith("(sublist Error"));
    p.resetAttributes();
    p.setAttribute("index","0");
    assertTrue(p.getCurrent(pm).startsWith("(sublist Error"));
    p.resetAttributes();
    p.setAttribute("list","");
    p.setAttribute("index","0");
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc");
    assertEquals("abc", p.getCurrent(pm));
    p.setAttribute("list","abc" + PS);
    assertEquals("abc", p.getCurrent(pm));
    p.setAttribute("list","abc" + PS + "def");
    assertEquals("abc", p.getCurrent(pm));
    p.setAttribute("list",PS + "def");
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc" + PS + "def" + PS + "ghi");
    assertEquals("abc", p.getCurrent(pm));
    
    p.setAttribute("list","");
    p.setAttribute("index","1");
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc");
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc" + PS);
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc" + PS + "def");
    assertEquals("def", p.getCurrent(pm));
    p.setAttribute("list",PS + "def");
    assertEquals("def", p.getCurrent(pm));
    p.setAttribute("list","abc" + PS + "def" + PS + "ghi");
    assertEquals("def", p.getCurrent(pm));
    
    p.setAttribute("list","");
    p.setAttribute("index","0");
    p.setAttribute("count","2");
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc");
    assertEquals("abc", p.getCurrent(pm));
    p.setAttribute("list","abc" + PS);
    assertEquals("abc" + PS, p.getCurrent(pm));
    p.setAttribute("list","abc" + PS + "def");
    assertEquals("abc" + PS + "def", p.getCurrent(pm));
    p.setAttribute("list",PS + "def");
    assertEquals(PS + "def", p.getCurrent(pm));
    p.setAttribute("list","abc" + PS + "def" + PS + "ghi");
    assertEquals("abc" + PS + "def", p.getCurrent(pm));
    
    p.setAttribute("list","");
    p.setAttribute("index","1");
    p.setAttribute("count","2");
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc");
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc" + PS);
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc" + PS + "def");
    assertEquals("def", p.getCurrent(pm));
    p.setAttribute("list",PS + "def");
    assertEquals("def", p.getCurrent(pm));
    p.setAttribute("list","abc" + PS + "def" + PS + "ghi");
    assertEquals("def" + PS + "ghi", p.getCurrent(pm));
    
    p.resetAttributes();
    p.setAttribute("sep"," ");
    p.setAttribute("list","");
    p.setAttribute("index","0");
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc");
    assertEquals("abc", p.getCurrent(pm));
    p.setAttribute("list","abc ");
    assertEquals("abc", p.getCurrent(pm));
    p.setAttribute("list","abc def");
    assertEquals("abc", p.getCurrent(pm));
    p.setAttribute("list"," def");
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc def ghi");
    assertEquals("abc", p.getCurrent(pm));
    
    p.setAttribute("list","");
    p.setAttribute("index","1");
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc");
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc ");
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc def");
    assertEquals("def", p.getCurrent(pm));
    p.setAttribute("list"," def");
    assertEquals("def", p.getCurrent(pm));
    p.setAttribute("list","abc def ghi");
    assertEquals("def", p.getCurrent(pm));
    
    p.setAttribute("list","");
    p.setAttribute("index","0");
    p.setAttribute("count","2");
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc");
    assertEquals("abc", p.getCurrent(pm));
    p.setAttribute("list","abc ");
    assertEquals("abc ", p.getCurrent(pm));
    p.setAttribute("list","abc def");
    assertEquals("abc def", p.getCurrent(pm));
    p.setAttribute("list"," def");
    assertEquals(" def", p.getCurrent(pm));
    p.setAttribute("list","abc def ghi");
    assertEquals("abc def", p.getCurrent(pm));
    
    p.setAttribute("list","");
    p.setAttribute("index","1");
    p.setAttribute("count","2");
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc");
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc ");
    assertEquals("", p.getCurrent(pm));
    p.setAttribute("list","abc def");
    assertEquals("def", p.getCurrent(pm));
    p.setAttribute("list"," def");
    assertEquals("def", p.getCurrent(pm));
    p.setAttribute("list","abc def ghi");
    assertEquals("def ghi", p.getCurrent(pm));

    // change.sep
    p = pm.getProperty("Misc","change.sep");
    assertTrue(p.getCurrent(pm).startsWith("(change.sep Error"));
    p.setAttribute("list","");
    assertTrue(p.getCurrent(pm).startsWith("(change.sep Error"));
    p.resetAttributes();
    p.setAttribute("old","abc");
    assertTrue(p.getCurrent(pm).startsWith("(change.sep Error"));
    p.resetAttributes();
    p.setAttribute("new","123");
    assertTrue(p.getCurrent(pm).startsWith("(change.sep Error"));
    p.resetAttributes();
    p.setAttribute("list","");
    p.setAttribute("old","abc");
    assertTrue(p.getCurrent(pm).startsWith("(change.sep Error"));
    p.resetAttributes();
    p.setAttribute("list","");
    p.setAttribute("new","123");
    assertTrue(p.getCurrent(pm).startsWith("(change.sep Error"));
    p.resetAttributes();
    p.setAttribute("old",PS);
    p.setAttribute("new"," ");
    assertTrue(p.getCurrent(pm).startsWith("(change.sep Error"));
    p.setAttribute("list","abc");
    assertEquals("abc",p.getCurrent(pm));
    p.setAttribute("list","");
    assertEquals("",p.getCurrent(pm));
    p.setAttribute("list","abc" + PS + "def");
    assertEquals("abc def",p.getCurrent(pm));
    p.setAttribute("list",PS + "abc" + PS + "def");
    assertEquals(" abc def",p.getCurrent(pm));
    p.setAttribute("list",PS + "abc" + PS + "def" + PS);
    assertEquals(" abc def ",p.getCurrent(pm));
    p.setAttribute("list",PS + "abc" + PS + "def" + PS+PS);
    assertEquals(" abc def  ",p.getCurrent(pm));
    p.setAttribute("list",PS+PS + "abc" + PS + "def" + PS+PS);
    assertEquals("  abc def  ",p.getCurrent(pm));
    p.setAttribute("list",PS+PS + "abc" + PS+PS + "def" + PS+PS);
    assertEquals("  abc  def  ",p.getCurrent(pm));
    
    p.setAttribute("list","abc 123");
    assertEquals("abc 123",p.getCurrent(pm));
    p.setAttribute("list","");
    assertEquals("",p.getCurrent(pm));
    p.setAttribute("list","abc 123" + PS + "def 456");
    assertEquals("abc 123 def 456",p.getCurrent(pm));
    p.setAttribute("list",PS + "abc 123" + PS + "def 456");
    assertEquals(" abc 123 def 456",p.getCurrent(pm));
    p.setAttribute("list",PS + "abc 123" + PS + "def 456" + PS);
    assertEquals(" abc 123 def 456 ",p.getCurrent(pm));
    p.setAttribute("list",PS + "abc 123" + PS + "def 456" + PS+PS);
    assertEquals(" abc 123 def 456  ",p.getCurrent(pm));
    p.setAttribute("list",PS+PS + "abc 123" + PS + "def 456" + PS+PS);
    assertEquals("  abc 123 def 456  ",p.getCurrent(pm));
    p.setAttribute("list",PS+PS + "abc 123" + PS+PS + "def 456" + PS+PS);
    assertEquals("  abc 123  def 456  ",p.getCurrent(pm));
    
    p.setAttribute("old"," ");
    p.setAttribute("new",PS);
    p.setAttribute("list","abc" + PS + "def");
    assertEquals("abc" + PS + "def",p.getCurrent(pm));
    p.setAttribute("list","");
    assertEquals("",p.getCurrent(pm));
    p.setAttribute("list","abc def");
    assertEquals("abc" + PS + "def",p.getCurrent(pm));
    p.setAttribute("list"," abc def");
    assertEquals(PS + "abc" + PS + "def",p.getCurrent(pm));
    p.setAttribute("list"," abc def ");
    assertEquals(PS + "abc" + PS + "def" + PS,p.getCurrent(pm));
    p.setAttribute("list"," abc def  ");
    assertEquals(PS + "abc" + PS + "def" + PS+PS,p.getCurrent(pm));
    p.setAttribute("list","  abc def  ");
    assertEquals(PS+PS + "abc" + PS + "def" + PS+PS,p.getCurrent(pm));
    p.setAttribute("list","  abc  def  ");
    assertEquals(PS+PS + "abc" + PS+PS + "def" + PS+PS,p.getCurrent(pm));
  }
  
  public void testFakeConfigProperties() throws CloneNotSupportedException {
    PropertyMaps pm = PropertyMaps.TEMPLATE.clone();
    DrJavaProperty p;
    
    // config.master.jvm.args.combined
    p = pm.getProperty("Config","config.master.jvm.args.combined");
    String oldXMX = DrJava.getConfig().getSetting(OptionConstants.MASTER_JVM_XMX);
    String oldArgs = DrJava.getConfig().getSetting(OptionConstants.MASTER_JVM_ARGS);
    try {
      DrJava.getConfig().setSetting(OptionConstants.MASTER_JVM_XMX,"");
      DrJava.getConfig().setSetting(OptionConstants.MASTER_JVM_ARGS,"");
      assertEquals("", p.getCurrent(pm));
      DrJava.getConfig().setSetting(OptionConstants.MASTER_JVM_XMX,"default");
      assertEquals("", p.getCurrent(pm));
      DrJava.getConfig().setSetting(OptionConstants.MASTER_JVM_XMX,"512");
      assertEquals("-Xmx512M", p.getCurrent(pm));
      DrJava.getConfig().setSetting(OptionConstants.MASTER_JVM_XMX,"1024");
      assertEquals("-Xmx1024M", p.getCurrent(pm));
      DrJava.getConfig().setSetting(OptionConstants.MASTER_JVM_ARGS,"-server");
      assertEquals("-Xmx1024M -server", p.getCurrent(pm));
      DrJava.getConfig().setSetting(OptionConstants.MASTER_JVM_XMX,"");
      assertEquals("-server", p.getCurrent(pm));
    }
    finally {
      DrJava.getConfig().setSetting(OptionConstants.MASTER_JVM_XMX,oldXMX);
      DrJava.getConfig().setSetting(OptionConstants.MASTER_JVM_ARGS,oldArgs);
    }
    
    // config.slave.jvm.args.combined
    p = pm.getProperty("Config","config.slave.jvm.args.combined");
    oldXMX = DrJava.getConfig().getSetting(OptionConstants.SLAVE_JVM_XMX);
    oldArgs = DrJava.getConfig().getSetting(OptionConstants.SLAVE_JVM_ARGS);
    try {
      DrJava.getConfig().setSetting(OptionConstants.SLAVE_JVM_XMX,"");
      DrJava.getConfig().setSetting(OptionConstants.SLAVE_JVM_ARGS,"");
      assertEquals("", p.getCurrent(pm));
      DrJava.getConfig().setSetting(OptionConstants.SLAVE_JVM_XMX,"default");
      assertEquals("", p.getCurrent(pm));
      DrJava.getConfig().setSetting(OptionConstants.SLAVE_JVM_XMX,"512");
      assertEquals("-Xmx512M", p.getCurrent(pm));
      DrJava.getConfig().setSetting(OptionConstants.SLAVE_JVM_XMX,"1024");
      assertEquals("-Xmx1024M", p.getCurrent(pm));
      DrJava.getConfig().setSetting(OptionConstants.SLAVE_JVM_ARGS,"-server");
      assertEquals("-Xmx1024M -server", p.getCurrent(pm));
      DrJava.getConfig().setSetting(OptionConstants.SLAVE_JVM_XMX,"");
      assertEquals("-server", p.getCurrent(pm));
    }
    finally {
      DrJava.getConfig().setSetting(OptionConstants.SLAVE_JVM_XMX,oldXMX);
      DrJava.getConfig().setSetting(OptionConstants.SLAVE_JVM_ARGS,oldArgs);
    }
  }
  
  public void testFile() throws CloneNotSupportedException, IOException {
    PropertyMaps pm = PropertyMaps.TEMPLATE.clone();
    DrJavaProperty p;
    
    // tmpfile
    p = pm.getProperty("Misc","tmpfile");
    String s = StringOps.unescapeFileName(p.getCurrent(pm));
    assertTrue(s.startsWith(TMPDIR + "DrJava-Execute-"));
    String s2 = StringOps.unescapeFileName(p.getCurrent(pm));
    assertTrue(s2.startsWith(TMPDIR + "DrJava-Execute-"));
    assertFalse(s.equals(s2));
    p.setAttribute("name","foo");
    s = StringOps.unescapeFileName(p.getCurrent(pm));
    assertEquals(TMPDIR + "foo", s);
    
    File dir = FileOps.createTempDirectory("DrJavaPropertySetupTest");
    p.setAttribute("dir",StringOps.escapeFileName(dir.getAbsolutePath()));
    s = StringOps.unescapeFileName(p.getCurrent(pm));
    assertEquals(dir.getAbsolutePath()+File.separator + "foo", s);
    
    p.resetAttributes();
    p.setAttribute("dir",StringOps.escapeFileName(dir.getAbsolutePath()));
    s = StringOps.unescapeFileName(p.getCurrent(pm));
    assertTrue(s.startsWith(dir.getAbsolutePath()+File.separator + "DrJava-Execute-"));
    
    final String TEST_STRING = "This is a test file from DrJavaPropertySetupTest.";
    p.setAttribute("content",TEST_STRING);
    s = StringOps.unescapeFileName(p.getCurrent(pm));
    assertTrue(s.startsWith(dir.getAbsolutePath()+File.separator + "DrJava-Execute-"));
    String text = edu.rice.cs.plt.io.IOUtil.toString(new File(s));
    assertEquals(TEST_STRING, text);
    
    // file.find
    
    // file.isdir
    p = pm.getProperty("File","file.isdir");    
    assertTrue(p.getCurrent(pm).startsWith("(file.isdir Error"));    
    dir = FileOps.createTempDirectory("DrJavaPropertySetupTest");
    p.setAttribute("file",StringOps.escapeFileName(dir.getAbsolutePath()));
    assertEquals("true", p.getCurrent(pm));

    File fil = edu.rice.cs.plt.io.IOUtil.createAndMarkTempFile("DrJavaPropertySetupTest", ".txt");
    p.setAttribute("file",StringOps.escapeFileName(fil.getAbsolutePath()));
    assertEquals("false", p.getCurrent(pm));
    
    File notFound = new File(System.getProperty("java.io.tmpdir"),"DrJavaPropertySetupTest." +
                             System.currentTimeMillis() + "-" + (_r.nextInt() & 0xffff) + ".tmp");
    p.setAttribute("file",StringOps.escapeFileName(notFound.getAbsolutePath()));
    assertEquals("false", p.getCurrent(pm));

    p.setAttribute("file",StringOps.escapeFileName(dir.getAbsolutePath())+File.pathSeparator+
                          StringOps.escapeFileName(fil.getAbsolutePath())+File.pathSeparator+
                          StringOps.escapeFileName(notFound.getAbsolutePath()));
    assertEquals("true" + File.pathSeparator + "false" + File.pathSeparator + "false", p.getCurrent(pm));
    
    // file.isfile
    p = pm.getProperty("File","file.isfile");    
    assertTrue(p.getCurrent(pm).startsWith("(file.isfile Error"));    
    dir = FileOps.createTempDirectory("DrJavaPropertySetupTest");
    p.setAttribute("file",StringOps.escapeFileName(dir.getAbsolutePath()));
    assertEquals("false", p.getCurrent(pm));

    fil = edu.rice.cs.plt.io.IOUtil.createAndMarkTempFile("DrJavaPropertySetupTest", ".txt");
    p.setAttribute("file",StringOps.escapeFileName(fil.getAbsolutePath()));
    assertEquals("true", p.getCurrent(pm));
    
    notFound = new File(System.getProperty("java.io.tmpdir"),"DrJavaPropertySetupTest." + System.currentTimeMillis() +
                        "-" + (_r.nextInt() & 0xffff) + ".tmp");
    p.setAttribute("file",StringOps.escapeFileName(notFound.getAbsolutePath()));
    assertEquals("false", p.getCurrent(pm));

    p.setAttribute("file",StringOps.escapeFileName(dir.getAbsolutePath())+File.pathSeparator+
                          StringOps.escapeFileName(fil.getAbsolutePath())+File.pathSeparator+
                          StringOps.escapeFileName(notFound.getAbsolutePath()));
    assertEquals("false" + File.pathSeparator + "true" + File.pathSeparator + "false", p.getCurrent(pm));
    
    // file.isfile
    p = pm.getProperty("File","file.exists");    
    assertTrue(p.getCurrent(pm).startsWith("(file.exists Error"));    
    dir = FileOps.createTempDirectory("DrJavaPropertySetupTest");
    p.setAttribute("file",StringOps.escapeFileName(dir.getAbsolutePath()));
    assertEquals("true", p.getCurrent(pm));

    fil = edu.rice.cs.plt.io.IOUtil.createAndMarkTempFile("DrJavaPropertySetupTest", ".txt");
    p.setAttribute("file",StringOps.escapeFileName(fil.getAbsolutePath()));
    assertEquals("true", p.getCurrent(pm));
    
    notFound = new File(System.getProperty("java.io.tmpdir"),"DrJavaPropertySetupTest." + System.currentTimeMillis() +
                        "-" + (_r.nextInt() & 0xffff) + ".tmp");
    p.setAttribute("file",StringOps.escapeFileName(notFound.getAbsolutePath()));
    assertEquals("false", p.getCurrent(pm));

    p.setAttribute("file",StringOps.escapeFileName(dir.getAbsolutePath())+File.pathSeparator+
                          StringOps.escapeFileName(fil.getAbsolutePath())+File.pathSeparator+
                          StringOps.escapeFileName(notFound.getAbsolutePath()));
    assertEquals("true" + File.pathSeparator + "true" + File.pathSeparator + "false", p.getCurrent(pm));
    
    // file.parent
    p = pm.getProperty("File","file.parent");
    assertTrue(p.getCurrent(pm).startsWith("(file.parent Error"));    
    dir = FileOps.createTempDirectory("DrJavaPropertySetupTest");
    p.setAttribute("file",StringOps.escapeFileName(dir .getAbsolutePath()));
    assertEquals(dir.getParentFile().getAbsolutePath(), StringOps.unescapeFileName(p.getCurrent(pm)));

    fil = edu.rice.cs.plt.io.IOUtil.createAndMarkTempFile("DrJavaPropertySetupTest", ".txt");
    p.setAttribute("file",StringOps.escapeFileName(fil.getAbsolutePath()));
    assertEquals(fil.getParentFile().getAbsolutePath(), StringOps.unescapeFileName(p.getCurrent(pm)));
    
    notFound = new File(System.getProperty("java.io.tmpdir"),"DrJavaPropertySetupTest." + System.currentTimeMillis() +
                        "-" + (_r.nextInt() & 0xffff) + ".tmp");
    p.setAttribute("file",StringOps.escapeFileName(notFound.getAbsolutePath()));
    assertEquals(new File(System.getProperty("java.io.tmpdir")).getAbsolutePath(),
                 StringOps.unescapeFileName(p.getCurrent(pm)));

    p.setAttribute("file",StringOps.escapeFileName(dir.getAbsolutePath())+File.pathSeparator+
                          StringOps.escapeFileName(fil.getAbsolutePath())+File.pathSeparator+
                          StringOps.escapeFileName(notFound.getAbsolutePath()));
    assertEquals(dir.getParentFile().getAbsolutePath()+File.pathSeparator+
                 fil.getParentFile().getAbsolutePath()+File.pathSeparator+
                 new File(System.getProperty("java.io.tmpdir")).getAbsolutePath(),
                 StringOps.unescapeFileName(p.getCurrent(pm)));
    
    // file.abs
    p = pm.getProperty("File","file.abs");
    assertTrue(p.getCurrent(pm).startsWith("(file.abs Error"));    
    dir = FileOps.createTempDirectory("DrJavaPropertySetupTest");
    p.setAttribute("file",StringOps.escapeFileName(dir.getName()));
    assertTrue(p.getCurrent(pm).startsWith("(file.abs Error"));
    p.resetAttributes();
    p.setAttribute("base",StringOps.escapeFileName(dir.getParentFile().getAbsolutePath()));
    assertTrue(p.getCurrent(pm).startsWith("(file.abs Error"));
    p.setAttribute("file",StringOps.escapeFileName(dir.getName()));
    assertEquals(dir.getAbsolutePath(), StringOps.unescapeFileName(p.getCurrent(pm)));

    fil = edu.rice.cs.plt.io.IOUtil.createAndMarkTempFile("DrJavaPropertySetupTest", ".txt");
    p.setAttribute("file",StringOps.escapeFileName(fil.getName()));
    p.setAttribute("base",StringOps.escapeFileName(fil.getParentFile().getAbsolutePath()));
    assertEquals(fil.getAbsolutePath(), StringOps.unescapeFileName(p.getCurrent(pm)));
    
    notFound = new File(System.getProperty("java.io.tmpdir"),"DrJavaPropertySetupTest." + System.currentTimeMillis() +
                        "-" + (_r.nextInt() & 0xffff) + ".tmp");
    p.setAttribute("file",StringOps.escapeFileName(notFound.getName()));
    p.setAttribute("base",StringOps.escapeFileName(notFound.getParentFile().getAbsolutePath()));
    assertEquals(notFound.getAbsolutePath(), StringOps.unescapeFileName(p.getCurrent(pm)));

    p.setAttribute("file",StringOps.escapeFileName(dir.getName())+File.pathSeparator+
                          StringOps.escapeFileName(fil.getName())+File.pathSeparator+
                          StringOps.escapeFileName(notFound.getName()));
    assertEquals(dir.getAbsolutePath()+File.pathSeparator+
                 fil.getAbsolutePath()+File.pathSeparator+
                 notFound.getAbsolutePath(), StringOps.unescapeFileName(p.getCurrent(pm)));
    
    // file.rel
    p = pm.getProperty("File","file.rel");
    assertTrue(p.getCurrent(pm).startsWith("(file.rel Error"));
    dir = FileOps.createTempDirectory("DrJavaPropertySetupTest");
    p.setAttribute("file",StringOps.escapeFileName(dir.getAbsolutePath()));
    assertTrue(p.getCurrent(pm).startsWith("(file.rel Error"));
    p.resetAttributes();
    p.setAttribute("base",StringOps.escapeFileName(dir.getAbsolutePath()));
    assertTrue(p.getCurrent(pm).startsWith("(file.rel Error"));
    p.setAttribute("file",StringOps.escapeFileName(dir.getAbsolutePath()));
    p.setAttribute("base",StringOps.escapeFileName(dir.getAbsolutePath()));
    assertEquals(".", StringOps.unescapeFileName(p.getCurrent(pm)));
    p.setAttribute("file",StringOps.escapeFileName(dir.getAbsolutePath()));
    p.setAttribute("base",StringOps.escapeFileName(dir.getParentFile().getAbsolutePath()));
    assertEquals(dir.getName(), StringOps.unescapeFileName(p.getCurrent(pm)));
    dir = edu.rice.cs.plt.io.IOUtil.createAndMarkTempDirectory("DrJavaPropertySetupTest","");
    File dir1 = edu.rice.cs.plt.io.IOUtil.createAndMarkTempDirectory("DrJavaPropertySetupTest","",dir);
    File dir2 = edu.rice.cs.plt.io.IOUtil.createAndMarkTempDirectory("DrJavaPropertySetupTest","",dir);
    fil = edu.rice.cs.plt.io.IOUtil.createAndMarkTempFile("DrJavaPropertySetupTest",".txt",dir1);
    p.setAttribute("file",StringOps.escapeFileName(fil.getAbsolutePath()));
    p.setAttribute("base",StringOps.escapeFileName(dir2.getAbsolutePath()));
    assertEquals(".." + FS+dir1.getName()+FS+fil.getName(), StringOps.unescapeFileName(p.getCurrent(pm)));
        
    // file.mkdir
    p = pm.getProperty("File","file.mkdir");
    assertTrue(p.getCurrent(pm).startsWith("(file.mkdir Error"));    
    
    // file.rm
    p = pm.getProperty("File","file.rm");
    assertTrue(p.getCurrent(pm).startsWith("(file.rm Error"));    
    
    // file.mv
    p = pm.getProperty("File","file.mv");
    assertTrue(p.getCurrent(pm).startsWith("(file.mv Error"));    
  }
  
  public void testMisc() throws CloneNotSupportedException {
    PropertyMaps pm = PropertyMaps.TEMPLATE.clone();
    @SuppressWarnings("unused") DrJavaProperty p;

    // drjava.current.time.millis
    p = pm.getProperty("DrJava","drjava.current.time.millis");
    
    // ignore
    p = pm.getProperty("Misc","ignore");
    
    // process.separator
    p = pm.getProperty("Config","process.separator");
    
    // enclosing.djapp.file
    p = pm.getProperty("Misc","enclosing.djapp.file");
    
    // drjava.file
    // during testing, this is the classes/base directory
    p = pm.getProperty("Misc","drjava.file");
    
    // echo
    p = pm.getProperty("Misc","echo");
  }
  
  public void testControlFlow() throws CloneNotSupportedException {
    PropertyMaps pm = PropertyMaps.TEMPLATE.clone();
    DrJavaProperty p;

    // if
    p = pm.getProperty("Misc","if");
    
    // for
    p = pm.getProperty("Misc","for");
    
    // var
    p = pm.getProperty("Misc","var");
    
    // var.set
    p = pm.getProperty("Misc","var.set");
  }
  
  public void testXML() throws CloneNotSupportedException {
    PropertyMaps pm = PropertyMaps.TEMPLATE.clone();
    DrJavaProperty p;

    // xml.in
    p = pm.getProperty("Misc","xml.in");
    
    // xml.out.action
    p = pm.getProperty("Misc","xml.out.action");
  }
}
