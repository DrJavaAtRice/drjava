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

package edu.rice.cs.util;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

/**
 * XML configuration management tests.
 * @author Mathias Ricken
 */
public class XMLConfigTest extends TestCase {
  /**
   * Newline string.
   */
  public static final String NL = System.getProperty("line.separator");
  public void testNodes() throws Exception {
    XMLConfig xc = new XMLConfig(new StringReader(
                                                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><foo a=\"foo.a\">\n"
                                                    + "  <bar>abc</bar>\n"
                                                    + "  <fum fee=\"xyz\">def</fum>\n"
                                                    + "</foo>"));
    assertEquals("abc", xc.get("foo/bar"));
    assertEquals("def", xc.get("foo/fum"));
  }
  public void testAttrs() throws Exception {
    XMLConfig xc = new XMLConfig(new StringReader(
                                                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><foo a=\"foo.a\">\n"
                                                    + "  <bar>abc</bar>\n"
                                                    + "  <fum fee=\"xyz\">def</fum>\n"
                                                    + "</foo>"));
    assertEquals("foo.a", xc.get("foo.a"));
    assertEquals("xyz", xc.get("foo/fum.fee"));
  }
  public void testExceptions() throws Exception {
    XMLConfig xc = new XMLConfig(new StringReader(
                                                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><foo a=\"foo.a\">\n"
                                                    + "  <bar>abc</bar>\n"
                                                    + "  <fum fee=\"xyz\">def</fum>\n"
                                                    + "</foo>"));
    try {
      xc.get("");
      fail("Should throw 'no node' exception");
    }
    catch (RuntimeException e) {
      // ignore
    }
    try {
      xc.get("xyz");
      fail("Should throw 'no node' exception");
    }
    catch (RuntimeException e) {
      // ignore
    }
    try {
      xc.get("foo.xyz");
      fail("Should throw 'no node' exception");
    }
    catch (RuntimeException e) {
      // ignore
    }
    try {
      xc.get("foo.b");
      fail("Should throw 'no attribute' exception");
    }
    catch (RuntimeException e) {
      // ignore
    }
  }
  public void testSave() throws Exception {
    XMLConfig xc = new XMLConfig(new StringReader(
                                                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><foo a=\"foo.a\">\n"
                                                    + "  <bar>abc</bar>\n"
                                                    + "  <fum fee=\"xyz\">def</fum>\n"
                                                    + "</foo>"));
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL
                   + "<foo a=\"foo.a\">"+NL
                   + "  <bar>abc</bar>"+NL
                   + "  <fum fee=\"xyz\">def</fum>"+NL
                   + "</foo>"+NL, xc.toString());
  }
  public void testSetNodeFromEmpty() throws Exception {
    XMLConfig xc = new XMLConfig();
    xc.set("foo/bar", "abc");
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo>"+NL+"  <bar>abc</bar>"+NL+"</foo>"+NL, xc.toString());
    assertEquals("abc", xc.get("foo/bar"));
    
    xc.set("foo/fum", "def");
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo>"+NL+"  <bar>abc</bar>"+NL+"  <fum>def</fum>"+NL+"</foo>"+NL, xc.toString());
    assertEquals("def", xc.get("foo/fum"));
  }
  public void testSetNodeOverwrite() throws Exception {
    XMLConfig xc = new XMLConfig();
    xc.set("foo/bar", "abc");
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo>"+NL+"  <bar>abc</bar>"+NL+"</foo>"+NL, xc.toString());
    assertEquals("abc", xc.get("foo/bar"));
    
    xc.set("foo/bar", "def");
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo>"+NL+"  <bar>def</bar>"+NL+"</foo>"+NL, xc.toString());
    assertEquals("def", xc.get("foo/bar"));
    
    xc.set("foo", "xyz");
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo>xyz</foo>"+NL, xc.toString());
    assertEquals("xyz", xc.get("foo"));
  }
  public void testSetAttrFromEmpty() throws Exception {
    XMLConfig xc = new XMLConfig();
    xc.set("foo.bar", "abc");
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo bar=\"abc\"/>"+NL, xc.toString());
    assertEquals("abc", xc.get("foo.bar"));
    
    xc.set("foo/fum.fee", "def");
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo bar=\"abc\">"+NL+"  <fum fee=\"def\"/>"+NL+"</foo>"+NL, xc.toString());
    assertEquals("def", xc.get("foo/fum.fee"));
  }
  public void testSetAttrOverwrite() throws Exception {
    XMLConfig xc = new XMLConfig();
    xc.set("foo.bar", "abc");
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo bar=\"abc\"/>"+NL, xc.toString());
    assertEquals("abc", xc.get("foo.bar"));
    
    xc.set("foo.bar", "def");
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo bar=\"def\"/>"+NL, xc.toString());
    assertEquals("def", xc.get("foo.bar"));
  }
  public void testSetNodeNoOverwrite() throws Exception {
    XMLConfig xc = new XMLConfig();
    xc.set("foo/bar", "abc", false);
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo>"+NL+"  <bar>abc</bar>"+NL+"</foo>"+NL, xc.toString());
    assertEquals("abc", xc.get("foo/bar"));
    
    xc.set("foo/bar", "def", false);
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo>"+NL+"  <bar>abc</bar>"+NL+"  <bar>def</bar>"+NL+"</foo>"+NL, xc.toString());
    List<String> r = xc.getMultiple("foo/bar");
    assertEquals(2, r.size());
    assertEquals("abc", r.get(0));
    assertEquals("def", r.get(1));
  }
  public void testSetAttrNoOverwrite() throws Exception {
    XMLConfig xc = new XMLConfig();
    xc.set("foo/bar.fee", "abc", false);
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo>"+NL+"  <bar fee=\"abc\"/>"+NL+"</foo>"+NL, xc.toString());
    assertEquals("abc", xc.get("foo/bar.fee"));
    
    xc.set("foo/bar.fee", "def", false);
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo>"+NL+"  <bar fee=\"abc\"/>"+NL+"  <bar fee=\"def\"/>"+NL+"</foo>"+NL, xc.toString());
    List<String> r = xc.getMultiple("foo/bar.fee");
    assertEquals(2, r.size());
    assertEquals("abc", r.get(0));
    assertEquals("def", r.get(1));
  }
  public void testSetFromNode() throws Exception {
    XMLConfig xc = new XMLConfig();
    Node n = xc.set("foo/bar", "abc", false);
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo>"+NL+"  <bar>abc</bar>"+NL+"</foo>"+NL, xc.toString());
    assertEquals("abc", xc.get("foo/bar"));
    
    xc.set(".fuz", "def", n, false);
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo>"+NL+"  <bar fuz=\"def\">abc</bar>"+NL+"</foo>"+NL, xc.toString());
    assertEquals("abc", xc.get("foo/bar"));
    
    n = xc.set("fum", "", n.getParentNode(), false);
    
    if (System.getProperty("java.version").startsWith("1.5")) {
      assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo>"+NL+"  <bar fuz=\"def\">abc</bar>"+NL+"  <fum></fum>"+NL+"</foo>"+NL,
                   xc.toString());
    }
    else {
      assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo>"+NL+"  <bar fuz=\"def\">abc</bar>"+NL+"  <fum/>"+NL+"</foo>"+NL,
                   xc.toString());
    }
    assertEquals("", xc.get("foo/fum"));
    
    xc.set("file", "test1.txt", n, false);
    xc.set("file", "test2.txt", n, false);
    
    if (System.getProperty("java.version").startsWith("1.5")) {
      assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo>"+NL+"  <bar fuz=\"def\">abc</bar>"+NL
                     + "  <fum><file>test1.txt</file>"+NL+"    <file>test2.txt</file>"+NL+"  </fum>"+NL+"</foo>"+NL,
                   xc.toString());
    }
    else {
      assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+NL+"<foo>"+NL+"  <bar fuz=\"def\">abc</bar>"+NL
                     + "  <fum>"+NL+"    <file>test1.txt</file>"+NL+"    <file>test2.txt</file>"+NL+"  </fum>"+NL+"</foo>"+NL,
                   xc.toString());
    }
    List<String> r = xc.getMultiple("foo/fum/file");
    assertEquals(2, r.size());
    assertEquals("test1.txt", r.get(0));
    assertEquals("test2.txt", r.get(1));
  }
  
  public void testMultipleNodes() throws Exception {
    XMLConfig xc = new XMLConfig(new StringReader(
                                                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><foo a=\"foo.a\">\n"
                                                    + "  <bar>abc</bar>\n"
                                                    + "  <bar>ghi</bar>\n"
                                                    + "  <fum fee=\"xyz\">def</fum>\n"
                                                    + "</foo>"));
    List<String> r = xc.getMultiple("foo/bar");
    assertEquals(2, r.size());
    assertEquals("abc", r.get(0));
    assertEquals("ghi", r.get(1));
  }
  public void testMultipleNodesAttr() throws Exception {
    XMLConfig xc = new XMLConfig(new StringReader(
                                                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><foo a=\"foo.a\">\n"
                                                    + "  <bar fuz=\"aaa\">abc</bar>\n"
                                                    + "  <bar fuz=\"bbb\">ghi</bar>\n"
                                                    + "  <fum fee=\"xyz\">def</fum>\n"
                                                    + "</foo>"));
    List<String> r = xc.getMultiple("foo/bar.fuz");
    assertEquals(2, r.size());
    assertEquals("aaa", r.get(0));
    assertEquals("bbb", r.get(1));
  }
  public void testNodesStarEnd() throws Exception {
    XMLConfig xc = new XMLConfig(new StringReader(
                                                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><foo a=\"foo.a\">\n"
                                                    + "  <bar>abc</bar>\n"
                                                    + "  <bar>ghi</bar>\n"
                                                    + "  <fum fee=\"xyz\">def</fum>\n"
                                                    + "</foo>"));
    List<String> r = xc.getMultiple("foo/*");
    assertEquals(3, r.size());
    assertEquals("abc", r.get(0));
    assertEquals("ghi", r.get(1));
    assertEquals("def", r.get(2));
  }
  public void testNodesStarMiddle() throws Exception {
    XMLConfig xc = new XMLConfig(new StringReader(
                                                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><foo a=\"foo.a\">\n"
                                                    + "  <bar fee=\"xxx\" fuz=\"aaa\">abc</bar>\n"
                                                    + "  <bar fee=\"yyy\" fuz=\"bbb\">ghi</bar>\n"
                                                    + "  <fum fee=\"zzz\" fuz=\"ccc\">def</fum>\n"
                                                    + "</foo>"));
    List<String> r = xc.getMultiple("foo/*.fee");
    assertEquals(3, r.size());
    assertEquals("xxx", r.get(0));
    assertEquals("yyy", r.get(1));
    assertEquals("zzz", r.get(2));
    
    r = xc.getMultiple("foo/*.*");
    assertEquals(6, r.size());
    assertEquals("xxx", r.get(0));
    assertEquals("aaa", r.get(1));
    assertEquals("yyy", r.get(2));
    assertEquals("bbb", r.get(3));
    assertEquals("zzz", r.get(4));
    assertEquals("ccc", r.get(5));
    
    xc = new XMLConfig(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?><flub>\n"
                                          + "  <foo/>\n"
                                          + "  <fee/>\n"
                                          + "  <fum foz=\"abc\"/>"
                                          + "</flub>"));
    r = xc.getMultiple("flub/*");
    assertEquals(3, r.size());
  }
  public void testAttrsStarEnd() throws Exception {
    XMLConfig xc = new XMLConfig(new StringReader(
                                                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><foo a=\"foo.a\">\n"
                                                    + "  <bar>abc</bar>\n"
                                                    + "  <fum fee=\"xyz\" fuz=\"zzz\" fiz=\"aaa\">def</fum>\n"
                                                    + "</foo>"));
    List<String> r = xc.getMultiple("foo.*");
    assertEquals(1, r.size());
    assertEquals("foo.a", r.get(0));
    
    r = xc.getMultiple("foo/fum.*");
    assertEquals(3, r.size());
    assertEquals("xyz", r.get(0));
    assertEquals("aaa", r.get(1));
    assertEquals("zzz", r.get(2));
  }
  public void testNodeStarAttrsStar() throws Exception {
    XMLConfig xc = new XMLConfig(new StringReader(
                                                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><foo a=\"foo.a\">\n"
                                                    + "  <bar flubb=\"mno\">abc</bar>\n"
                                                    + "  <fum fee=\"xyz\" fuz=\"zzz\" fiz=\"aaa\">def</fum>\n"
                                                    + "</foo>"));
    List<String> r = xc.getMultiple("*.*");
    assertEquals(1, r.size());
    assertEquals("foo.a", r.get(0));
    
    r = xc.getMultiple("foo/*.*");
    assertEquals(4, r.size());
    assertEquals("mno", r.get(0));
    assertEquals("xyz", r.get(1));
    assertEquals("aaa", r.get(2));
    assertEquals("zzz", r.get(3));
  }
  
  public void getNodePath1() throws Exception {
    XMLConfig xc = new XMLConfig(new StringReader(
                                                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><concutest>\n"
                                                    +"  <threadcheck:def>\n"
                                                    +"    <invariant>\n"
                                                    +"      <name type=\"only\" value=\"childclass1\"/>\n"
                                                    +"    </invariant>\n"
                                                    +"    <class name=\"sample.threadCheck.ThreadCheckSample4\"/>\n"
                                                    +"  </threadcheck:def>\n"
                                                    +"  <threadcheck:def>\n"
                                                    +"    <invariant>\n"
                                                    +"      <name type=\"only\" value=\"childclass-method1\"/>\n"
                                                    +"    </invariant>\n"
                                                    +"    <method name=\"sample.threadCheck.ThreadCheckSample4\" sig=\"run()V\"/>\n"
                                                    +"    <method name=\"sample.threadCheck.ThreadCheckSample4\" sig=\"run2()V\"/>\n"
                                                    +"  </threadcheck:def>\n"
                                                    +"</concutest>"));
    assertEquals("Path of null is wrong", "", XMLConfig.getNodePath(null));
    
    List<Node> roots = xc.getNodes("concutest");
    Assert.assertEquals(1, roots.size());
    assertEquals("Path of "+roots.get(0).getNodeName()+" is wrong", "concutest", XMLConfig.getNodePath(roots.get(0)));
    
    List<Node> defs = xc.getNodes("concutest/threadcheck:def");
    Assert.assertEquals(2, defs.size());
    
    for(Node def: defs) {
      assertEquals("Path of "+def.getNodeName()+" is wrong", "concutest/threadcheck:def", XMLConfig.getNodePath(def));
      List<Node> invs = xc.getNodes("invariant", def);
      Assert.assertEquals(1, invs.size());
      Node inv = invs.get(0);
      assertEquals("Path of "+inv.getNodeName()+" is wrong", "concutest/threadcheck:def/invariant", XMLConfig.getNodePath(inv));
      List<Node> annots = xc.getNodes("*", inv);
      Assert.assertEquals(1, annots.size());
      assertEquals("Path of "+annots.get(0).getNodeName()+" is wrong", "concutest/threadcheck:def/invariant/name", XMLConfig.getNodePath(annots.get(0)));
      List<Node> classes = xc.getNodes("class", def);
      List<Node> methods = xc.getNodes("method", def);
      Assert.assertTrue("There must be at least one class or method per definition", (classes.size()+methods.size()>0));
      List<Node> all = xc.getNodes("*", def);
      Assert.assertEquals(0, all.size()-invs.size()-classes.size()-methods.size());
      for(Node target: classes) {
        assertEquals("Path of "+target.getNodeName()+" is wrong", "concutest/threadcheck:def/class", XMLConfig.getNodePath(target));
      }
      for(Node target: methods) {
        assertEquals("Path of "+target.getNodeName()+" is wrong", "concutest/threadcheck:def/method", XMLConfig.getNodePath(target));
      }
    }
  }
}
