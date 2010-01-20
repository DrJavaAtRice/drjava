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

package edu.rice.cs.util;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.w3c.dom.*;

import java.io.*;
import java.util.*;

/**
 * XML configuration management tests.
 * @author Mathias Ricken
 */
public class XMLConfigTest extends TestCase {
  /** Newline string.
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
  private String remove16XML(String s) {
    if ((System.getProperty("java.version").startsWith("1.5")) ||
        (System.getProperty("java.version").startsWith("1.4"))) {
      int pos1 = s.indexOf(" standalone=");
      int pos2 = s.indexOf("?>", pos1);
      return s.substring(0, pos1) + s.substring(pos2);
    }
    else {
      return s;
    }
  }
  public void testSave() throws Exception {
    XMLConfig xc = new XMLConfig(new StringReader(
                                                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><foo a=\"foo.a\">\n"
                                                    + "  <bar>abc</bar>\n"
                                                    + "  <fum fee=\"xyz\">def</fum>\n"
                                                    + "</foo>"));
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL +
                             "<foo a=\"foo.a\">" + NL +
                             "  <bar>abc</bar>" + NL +
                             "  <fum fee=\"xyz\">def</fum>" + NL +
                             "</foo>" + NL), xc.toString());
  }
  public void testSetNodeFromEmpty() throws Exception {
    XMLConfig xc = new XMLConfig();
    xc.set("foo/bar", "abc");
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar>abc</bar>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("abc", xc.get("foo/bar"));
    
    xc.set("foo/fum", "def");
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar>abc</bar>" + NL + "  <fum>def</fum>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("def", xc.get("foo/fum"));
  }
  public void testSetNodeOverwrite() throws Exception {
    XMLConfig xc = new XMLConfig();
    xc.set("foo/bar", "abc");
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar>abc</bar>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("abc", xc.get("foo/bar"));
    
    xc.set("foo/bar", "def");
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL+ "<foo>" + NL+
                             "  <bar>def</bar>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("def", xc.get("foo/bar"));
    
    xc.set("foo", "xyz");
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>xyz</foo>" + NL),
                 xc.toString());
    assertEquals("xyz", xc.get("foo"));
  }
  public void testSetAttrFromEmpty() throws Exception {
    XMLConfig xc = new XMLConfig();
    xc.set("foo.bar", "abc");
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL+
                             "<foo bar=\"abc\"/>" + NL), xc.toString());
    assertEquals("abc", xc.get("foo.bar"));
    
    xc.set("foo/fum.fee", "def");
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL+
                             "<foo bar=\"abc\">" + NL + "  <fum fee=\"def\"/>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("def", xc.get("foo/fum.fee"));
  }
  public void testSetAttrOverwrite() throws Exception {
    XMLConfig xc = new XMLConfig();
    xc.set("foo.bar", "abc");
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL+
                             "<foo bar=\"abc\"/>" + NL), xc.toString());
    assertEquals("abc", xc.get("foo.bar"));
    
    xc.set("foo.bar", "def");
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL+
                             "<foo bar=\"def\"/>" + NL), xc.toString());
    assertEquals("def", xc.get("foo.bar"));
  }
  public void testSetNodeNoOverwrite() throws Exception {
    XMLConfig xc = new XMLConfig();
    xc.set("foo/bar", "abc", false);
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar>abc</bar>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("abc", xc.get("foo/bar"));
    
    xc.set("foo/bar", "def", false);
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar>abc</bar>" + NL + "  <bar>def</bar>" + NL + "</foo>" + NL), xc.toString());
    List<String> r = xc.getMultiple("foo/bar");
    assertEquals(2, r.size());
    assertEquals("abc", r.get(0));
    assertEquals("def", r.get(1));
  }
  public void testSetAttrNoOverwrite() throws Exception {
    XMLConfig xc = new XMLConfig();
    xc.set("foo/bar.fee", "abc", false);
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar fee=\"abc\"/>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("abc", xc.get("foo/bar.fee"));
    
    xc.set("foo/bar.fee", "def", false);
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar fee=\"abc\"/>" + NL + "  <bar fee=\"def\"/>" + NL + "</foo>" + NL), xc.toString());
    List<String> r = xc.getMultiple("foo/bar.fee");
    assertEquals(2, r.size());
    assertEquals("abc", r.get(0));
    assertEquals("def", r.get(1));
  }
  public void testSetFromNode() throws Exception {
    XMLConfig xc = new XMLConfig();
    Node n = xc.set("foo/bar", "abc", false);
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar>abc</bar>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("abc", xc.get("foo/bar"));
    
    xc.set(".fuz", "def", n, false);
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar fuz=\"def\">abc</bar>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("abc", xc.get("foo/bar"));
    
    n = xc.set("fum", "", n.getParentNode(), false);
    
    if (System.getProperty("java.version").startsWith("1.5")) {
      assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                               "  <bar fuz=\"def\">abc</bar>" + NL + "  <fum></fum>" + NL + "</foo>" + NL),
                   xc.toString());
    }
    else {
      assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                               "  <bar fuz=\"def\">abc</bar>" + NL + "  <fum/>" + NL + "</foo>" + NL),
                   xc.toString());
    }
    assertEquals("", xc.get("foo/fum"));
    
    xc.set("file", "test1.txt", n, false);
    xc.set("file", "test2.txt", n, false);
    
    if (System.getProperty("java.version").startsWith("1.5")) {
      assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                               "  <bar fuz=\"def\">abc</bar>" + NL
                                 + "  <fum><file>test1.txt</file>" + NL + "    <file>test2.txt</file>" + NL+
                               "  </fum>" + NL + "</foo>" + NL),
                   xc.toString());
    }
    else {
      assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL +
                               "  <bar fuz=\"def\">abc</bar>" + NL
                                 + "  <fum>" + NL + "    <file>test1.txt</file>" + NL + "    <file>test2.txt</file>" +
                               NL + "  </fum>" + NL + "</foo>" + NL),
                   xc.toString());
    }
    List<String> r = xc.getMultiple("foo/fum/file");
    assertEquals(2, r.size());
    assertEquals("test1.txt", r.get(0));
    assertEquals("test2.txt", r.get(1));
  }
  
  public void testMultipleNodes() throws Exception {
    XMLConfig xc = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foo a=\"foo.a\">\n"
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
    XMLConfig xc = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foo a=\"foo.a\">\n"
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
    XMLConfig xc = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foo a=\"foo.a\">\n"
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
    XMLConfig xc = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foo a=\"foo.a\">\n"
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
    
    xc = new XMLConfig(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><flub>\n"
                                          + "  <foo/>\n"
                                          + "  <fee/>\n"
                                          + "  <fum foz=\"abc\"/>"
                                          + "</flub>"));
    r = xc.getMultiple("flub/*");
    assertEquals(3, r.size());
  }
  public void testAttrsStarEnd() throws Exception {
    XMLConfig xc = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foo a=\"foo.a\">\n"
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
    XMLConfig xc = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foo a=\"foo.a\">\n"
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
    XMLConfig xc = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><concutest>\n"
                                        + "  <threadcheck:def>\n"
                                        + "    <invariant>\n"
                                        + "      <name type=\"only\" value=\"childclass1\"/>\n"
                                        + "    </invariant>\n"
                                        + "    <class name=\"sample.threadCheck.ThreadCheckSample4\"/>\n"
                                        + "  </threadcheck:def>\n"
                                        + "  <threadcheck:def>\n"
                                        + "    <invariant>\n"
                                        + "      <name type=\"only\" value=\"childclass-method1\"/>\n"
                                        + "    </invariant>\n"
                                        + "    <method name=\"sample.threadCheck.ThreadCheckSample4\" sig=\"run()V\"/>\n"
                                        + "    <method name=\"sample.threadCheck.ThreadCheckSample4\" sig=\"run2()V\"/>\n"
                                        + "  </threadcheck:def>\n"
                                        + "</concutest>"));
    assertEquals("Path of null is wrong", "", XMLConfig.getNodePath(null));
    
    List<Node> roots = xc.getNodes("concutest");
    Assert.assertEquals(1, roots.size());
    assertEquals("Path of " + roots.get(0).getNodeName() + " is wrong", "concutest", XMLConfig.getNodePath(roots.get(0)));
    
    List<Node> defs = xc.getNodes("concutest/threadcheck:def");
    Assert.assertEquals(2, defs.size());
    
    for(Node def: defs) {
      assertEquals("Path of " + def.getNodeName() + " is wrong", "concutest/threadcheck:def", XMLConfig.getNodePath(def));
      List<Node> invs = xc.getNodes("invariant", def);
      Assert.assertEquals(1, invs.size());
      Node inv = invs.get(0);
      assertEquals("Path of " + inv.getNodeName() + " is wrong", "concutest/threadcheck:def/invariant", 
                   XMLConfig.getNodePath(inv));
      List<Node> annots = xc.getNodes("*", inv);
      Assert.assertEquals(1, annots.size());
      assertEquals("Path of " + annots.get(0).getNodeName() + " is wrong", "concutest/threadcheck:def/invariant/name", 
                   XMLConfig.getNodePath(annots.get(0)));
      List<Node> classes = xc.getNodes("class", def);
      List<Node> methods = xc.getNodes("method", def);
      Assert.assertTrue("There must be at least one class or method per definition", 
                        classes.size() + methods.size() > 0);
      List<Node> all = xc.getNodes("*", def);
      Assert.assertEquals(0, all.size()-invs.size()-classes.size()-methods.size());
      for(Node target: classes) {
        assertEquals("Path of " + target.getNodeName() + " is wrong", "concutest/threadcheck:def/class", 
                     XMLConfig.getNodePath(target));
      }
      for(Node target: methods) {
        assertEquals("Path of " + target.getNodeName() + " is wrong", "concutest/threadcheck:def/method", 
                     XMLConfig.getNodePath(target));
      }
    }
  }
  
  // ----- Delegation Tests -----
  public void testNodesDelegate() throws Exception {
    XMLConfig xcParent = new XMLConfig(new StringReader(
                                                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><foo a=\"foo.a\">\n"
                                                          + "  <bar>abc</bar>\n"
                                                          + "  <fum fee=\"xyz\">def</fum>\n"
                                                          + "</foo>"));
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));
    assertEquals("abc", xc.get("bar"));
    assertEquals("def", xc.get("fum"));
  }
  public void testAttrsDelegate() throws Exception {
    XMLConfig xcParent = new XMLConfig(new StringReader(
                                                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><foo a=\"foo.a\">\n"
                                                          + "  <bar>abc</bar>\n"
                                                          + "  <fum fee=\"xyz\">def</fum>\n"
                                                          + "</foo>"));
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));
    assertEquals("foo.a", xc.get(".a"));
    assertEquals("xyz", xc.get("fum.fee"));
  }
  public void testExceptionsDelegate() throws Exception {
    XMLConfig xcParent = new XMLConfig(new StringReader(
                                                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><foo a=\"foo.a\">\n"
                                                          + "  <bar>abc</bar>\n"
                                                          + "  <fum fee=\"xyz\">def</fum>\n"
                                                          + "</foo>"));
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));    
    
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
      xc.get(".xyz");
      fail("Should throw 'no node' exception");
    }
    catch (RuntimeException e) {
      // ignore
    }
    try {
      xc.get(".b");
      fail("Should throw 'no attribute' exception");
    }
    catch (RuntimeException e) {
      // ignore
    }
  }
  public void testSaveDelegate() throws Exception {
    XMLConfig xcParent = new XMLConfig(new StringReader(
                                                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><foo a=\"foo.a\">\n"
                                                          + "  <bar>abc</bar>\n"
                                                          + "  <fum fee=\"xyz\">def</fum>\n"
                                                          + "</foo>"));
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL +
                             "<foo a=\"foo.a\">" + NL +
                             "  <bar>abc</bar>" + NL +
                             "  <fum fee=\"xyz\">def</fum>" + NL +
                             "</foo>" + NL), xc.toString());
  }
  public void testSetNodeFromEmptyDelegate() throws Exception {
    XMLConfig xcParent = new XMLConfig();
    xcParent.set("foo/bar", "abc");
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar>abc</bar>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("abc", xc.get("bar"));
    
    xc.set("fum", "def");
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar>abc</bar>" + NL + "  <fum>def</fum>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("def", xc.get("fum"));
  }
  public void testSetNodeOverwriteDelegate() throws Exception {
    XMLConfig xcParent = new XMLConfig();
    xcParent.set("foo/bar", "abc");
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar>abc</bar>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("abc", xc.get("bar"));
    
    xc.set("bar", "def");
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar>def</bar>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("def", xc.get("bar"));
    
    xcParent.set("foo", "xyz");
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>xyz</foo>" + NL),
                 xc.toString());
    assertEquals("xyz", xcParent.get("foo"));
  }
  public void testSetAttrFromEmptyDelegate() throws Exception {
    XMLConfig xcParent = new XMLConfig();
    xcParent.set("foo.bar", "abc");
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));    
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo bar=\"abc\"/>" + NL),
                 xc.toString());
    assertEquals("abc", xc.get(".bar"));
    
    xc.set("fum.fee", "def");
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo bar=\"abc\">" + NL+
                             "  <fum fee=\"def\"/>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("def", xc.get("fum.fee"));
  }
  public void testSetAttrOverwriteDelegate() throws Exception {
    XMLConfig xcParent = new XMLConfig();
    xcParent.set("foo.bar", "abc");
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo bar=\"abc\"/>" + NL),
                 xc.toString());
    assertEquals("abc", xc.get(".bar"));
    
    xc.set(".bar", "def");
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo bar=\"def\"/>" + NL),
                 xc.toString());
    assertEquals("def", xc.get(".bar"));
  }
  public void testSetNodeNoOverwriteDelegate() throws Exception {
    XMLConfig xcParent = new XMLConfig();
    xcParent.set("foo/bar", "abc", false);
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar>abc</bar>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("abc", xc.get("bar"));
    
    xc.set("bar", "def", false);
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar>abc</bar>" + NL + "  <bar>def</bar>" + NL + "</foo>" + NL), xc.toString());
    List<String> r = xc.getMultiple("bar");
    assertEquals(2, r.size());
    assertEquals("abc", r.get(0));
    assertEquals("def", r.get(1));
  }
  public void testSetAttrNoOverwriteDelegate() throws Exception {
    XMLConfig xcParent = new XMLConfig();
    xcParent.set("foo/bar.fee", "abc", false);
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar fee=\"abc\"/>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("abc", xc.get("bar.fee"));
    
    xc.set("bar.fee", "def", false);
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar fee=\"abc\"/>" + NL + "  <bar fee=\"def\"/>" + NL + "</foo>" + NL), xc.toString());
    List<String> r = xc.getMultiple("bar.fee");
    assertEquals(2, r.size());
    assertEquals("abc", r.get(0));
    assertEquals("def", r.get(1));
  }
  public void testSetFromNodeDelegate() throws Exception {
    XMLConfig xcParent = new XMLConfig();
    Node n = xcParent.set("foo/bar", "abc", false);
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar>abc</bar>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("abc", xc.get("bar"));
    
    xc.set(".fuz", "def", n, false);
    
    assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                             "  <bar fuz=\"def\">abc</bar>" + NL + "</foo>" + NL), xc.toString());
    assertEquals("abc", xc.get("bar"));
    
    n = xc.set("fum", "", n.getParentNode(), false);
    
    if (System.getProperty("java.version").startsWith("1.5")) {
      assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                               "  <bar fuz=\"def\">abc</bar>" + NL + "  <fum></fum>" + NL + "</foo>" + NL),
                   xc.toString());
    }
    else {
      assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                               "  <bar fuz=\"def\">abc</bar>" + NL + "  <fum/>" + NL + "</foo>" + NL),
                   xc.toString());
    }
    assertEquals("", xc.get("fum"));
    
    xc.set("file", "test1.txt", n, false);
    xc.set("file", "test2.txt", n, false);
    
    if (System.getProperty("java.version").startsWith("1.5")) {
      assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                               "  <bar fuz=\"def\">abc</bar>" + NL
                                 + "  <fum><file>test1.txt</file>" + NL + "    <file>test2.txt</file>" + NL + "  </fum>" + 
                               NL + "</foo>" + NL),
                   xc.toString());
    }
    else {
      assertEquals(remove16XML("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NL + "<foo>" + NL+
                               "  <bar fuz=\"def\">abc</bar>" + NL
                                 + "  <fum>" + NL + "    <file>test1.txt</file>" + NL + "    <file>test2.txt</file>" + NL+
                               "  </fum>" + NL + "</foo>" + NL),
                   xc.toString());
    }
    List<String> r = xc.getMultiple("fum/file");
    assertEquals(2, r.size());
    assertEquals("test1.txt", r.get(0));
    assertEquals("test2.txt", r.get(1));
  }
  
  public void testMultipleNodesDelegate() throws Exception {
    XMLConfig xcParent = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foo a=\"foo.a\">\n"
                                       + "  <bar>abc</bar>\n"
                                       + "  <bar>ghi</bar>\n"
                                       + "  <fum fee=\"xyz\">def</fum>\n"
                                       + "</foo>"));
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));
    List<String> r = xc.getMultiple("bar");
    assertEquals(2, r.size());
    assertEquals("abc", r.get(0));
    assertEquals("ghi", r.get(1));
  }
  public void testMultipleNodesAttrDelegate() throws Exception {
    XMLConfig xcParent = 
      new XMLConfig(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foo a=\"foo.a\">\n"
                                       + "  <bar fuz=\"aaa\">abc</bar>\n"
                                       + "  <bar fuz=\"bbb\">ghi</bar>\n"
                                       + "  <fum fee=\"xyz\">def</fum>\n"
                                       + "</foo>"));
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));
    
    List<String> r = xc.getMultiple("bar.fuz");
    assertEquals(2, r.size());
    assertEquals("aaa", r.get(0));
    assertEquals("bbb", r.get(1));
  }
  public void testNodesStarEndDelegate() throws Exception {
    XMLConfig xcParent = 
      new XMLConfig(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foo a=\"foo.a\">\n"
                                       + "  <bar>abc</bar>\n"
                                       + "  <bar>ghi</bar>\n"
                                       + "  <fum fee=\"xyz\">def</fum>\n"
                                       + "</foo>"));
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));
    List<String> r = xc.getMultiple("*");
    assertEquals(3, r.size());
    assertEquals("abc", r.get(0));
    assertEquals("ghi", r.get(1));
    assertEquals("def", r.get(2));
  }
  public void testNodesStarMiddleDelegate() throws Exception {
    XMLConfig xcParent = 
      new XMLConfig(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foo a=\"foo.a\">" +
                                     "<fum>\n" 
                                       + "  <bar fee=\"xxx\" fuz=\"aaa\">abc</bar>\n"
                                       + "  <bar fee=\"yyy\" fuz=\"bbb\">ghi</bar>\n"
                                       + "  <fum fee=\"zzz\" fuz=\"ccc\">def</fum>\n"
                                       + "</fum></foo>"));
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));
    List<String> r = xc.getMultiple("fum/*.fee");
    assertEquals(3, r.size());
    assertEquals("xxx", r.get(0));
    assertEquals("yyy", r.get(1));
    assertEquals("zzz", r.get(2));
    
    r = xc.getMultiple("fum/*.*");
    assertEquals(6, r.size());
    assertEquals("xxx", r.get(0));
    assertEquals("aaa", r.get(1));
    assertEquals("yyy", r.get(2));
    assertEquals("bbb", r.get(3));
    assertEquals("zzz", r.get(4));
    assertEquals("ccc", r.get(5));
    
    xcParent = 
      new XMLConfig(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foo><flub>\n"
                                       + "  <foo/>\n"
                                       + "  <fee/>\n"
                                       + "  <fum foz=\"abc\"/>"
                                       + "</flub></foo>"));
    xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));
    r = xc.getMultiple("flub/*");
    assertEquals(3, r.size());
  }
  public void testAttrsStarEndDelegate() throws Exception {
    XMLConfig xcParent = 
      new XMLConfig(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foo a=\"foo.a\">\n"
                                       + "  <bar>abc</bar>\n"
                                       + "  <fum fee=\"xyz\" fuz=\"zzz\" fiz=\"aaa\">def</fum>\n"
                                       + "</foo>"));
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));
    List<String> r = xc.getMultiple(".*");
    assertEquals(1, r.size());
    assertEquals("foo.a", r.get(0));
    
    r = xc.getMultiple("fum.*");
    assertEquals(3, r.size());
    assertEquals("xyz", r.get(0));
    assertEquals("aaa", r.get(1));
    assertEquals("zzz", r.get(2));
  }
  public void testNodeStarAttrsStarDelegate() throws Exception {
    XMLConfig xcParent = 
      new XMLConfig(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><middle>" +
                                     "<foo a=\"foo.a\">\n"
                                       + "  <bar flubb=\"mno\">abc</bar>\n"
                                       + "  <fum fee=\"xyz\" fuz=\"zzz\" fiz=\"aaa\">def</fum>\n"
                                       + "</foo></middle>"));
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("middle").get(0));
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
  
  public void getNodePath1Delegate() throws Exception {
    XMLConfig xcParent = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foo><concutest>\n"
                                        + "  <threadcheck:def>\n"
                                        + "    <invariant>\n"
                                        + "      <name type=\"only\" value=\"childclass1\"/>\n"
                                        + "    </invariant>\n"
                                        + "    <class name=\"sample.threadCheck.ThreadCheckSample4\"/>\n"
                                        + "  </threadcheck:def>\n"
                                        + "  <threadcheck:def>\n"
                                        + "    <invariant>\n"
                                        + "      <name type=\"only\" value=\"childclass-method1\"/>\n"
                                        + "    </invariant>\n"
                                        + "    <method name=\"sample.threadCheck.ThreadCheckSample4\" sig=\"run()V\"/>\n"
                                        + "    <method name=\"sample.threadCheck.ThreadCheckSample4\" sig=\"run2()V\"/>\n"
                                        + "  </threadcheck:def>\n"
                                        + "</concutest></foo>"));
    XMLConfig xc = new XMLConfig(xcParent, xcParent.getNodes("foo").get(0));
    assertEquals("Path of null is wrong", "", XMLConfig.getNodePath(null));
    
    List<Node> roots = xc.getNodes("concutest");
    Assert.assertEquals(1, roots.size());
    assertEquals("Path of " + roots.get(0).getNodeName() + " is wrong", "concutest", XMLConfig.getNodePath(roots.get(0)));
    
    List<Node> defs = xc.getNodes("concutest/threadcheck:def");
    Assert.assertEquals(2, defs.size());
    
    for(Node def: defs) {
      assertEquals("Path of " + def.getNodeName() + " is wrong", "concutest/threadcheck:def", XMLConfig.getNodePath(def));
      List<Node> invs = xc.getNodes("invariant", def);
      Assert.assertEquals(1, invs.size());
      Node inv = invs.get(0);
      assertEquals("Path of " + inv.getNodeName() + " is wrong", "concutest/threadcheck:def/invariant", 
                   XMLConfig.getNodePath(inv));
      List<Node> annots = xc.getNodes("*", inv);
      Assert.assertEquals(1, annots.size());
      assertEquals("Path of " + annots.get(0).getNodeName() + " is wrong", "concutest/threadcheck:def/invariant/name", 
                   XMLConfig.getNodePath(annots.get(0)));
      List<Node> classes = xc.getNodes("class", def);
      List<Node> methods = xc.getNodes("method", def);
      Assert.assertTrue("There must be at least one class or method per definition", 
                        classes.size() + methods.size() > 0);
      List<Node> all = xc.getNodes("*", def);
      Assert.assertEquals(0, all.size()-invs.size()-classes.size()-methods.size());
      for(Node target: classes) {
        assertEquals("Path of " + target.getNodeName() + " is wrong", "concutest/threadcheck:def/class", 
                     XMLConfig.getNodePath(target));
      }
      for(Node target: methods) {
        assertEquals("Path of " + target.getNodeName() + " is wrong", "concutest/threadcheck:def/method", 
                     XMLConfig.getNodePath(target));
      }
    }
  }
  
  public void testGet() throws Exception {
    XMLConfig xc = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><concutest>\n"
                                        + "  <threadcheck:def>\n"
                                        + "    <invariant>\n"
                                        + "      <name type=\"only\" value=\"childclass1\"/>\n"
                                        + "    </invariant>\n"
                                        + "    <class name=\"sample.threadCheck.ThreadCheckSample4\"/>\n"
                                        + "  </threadcheck:def>\n"
                                        + "  <threadcheck:def>\n"
                                        + "    <invariant>\n"
                                        + "      <name type=\"only\" value=\"childclass-method1\"/>\n"
                                        + "    </invariant>\n"
                                        + "    <method name=\"sample.threadCheck.ThreadCheckSample4\" sig=\"run()V\"/>\n"
                                        + "    <method name=\"sample.threadCheck.ThreadCheckSample4\" sig=\"run2()V\"/>\n"
                                        + "  </threadcheck:def>\n"
                                        + "</concutest>"));    
    
    subTestGet("arbitraryPath", xc);
    subTestGet("path1/path2", xc);
    subTestGet("path1.prop1", xc);
    subTestGet("path1/path2.prop2", xc);
  }
  
  /**
   * Expectes "concutest" to be the root node of passed XMLConfig.
   */
  private void subTestGet(String pathToTest, XMLConfig xc) throws Exception{
    String ret = xc.get(pathToTest, xc.getNodes("concutest").get(0), "arbitraryDefaultValue");
    
    Assert.assertEquals("Default value not returned", "arbitraryDefaultValue", ret);
    
    ret = xc.get(pathToTest, "arbitraryDefaultValue");
    
    Assert.assertEquals("Default value not returned", "arbitraryDefaultValue", ret);
    
    xc.set("concutest/" + pathToTest, "actualValue");
    
    ret = xc.get("arbitraryPath", xc.getNodes("concutest").get(0), "arbitraryDefaultValue");
    
    Assert.assertEquals("Set value not returned", "actualValue", ret);
  }
  
  
  
  
  public void testGetInt() throws Exception {
    XMLConfig xc = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><concutest>\n"
                                        + " <name type=\"only\" value=\"5\"/>\n"
                                        + " <class name=\"sample.threadCheck.ThreadCheckSample4\"/>\n"
                                        + "</concutest>"));    
    
    //Test for getInt(String path)
    int n = xc.getInt("concutest/name.value");
    Assert.assertEquals("Found expected value", 5, n);
    try{
      xc.getInt("concutest/class.name");
      Assert.fail("Should have thrown exception");
    }
    catch(IllegalArgumentException e){ }
    
    try{
      xc.getInt("doesNotExitPath/subPath");
      Assert.fail("Should have thrown exception");
    }catch(XMLConfig.XMLConfigException e){ }
    
    //Test for getInt(String path, Node root)
    n = xc.getInt("name.value", xc.getNodes("concutest").get(0));
    Assert.assertEquals("Found expected value", 5, n);
    try{
      xc.getInt("class.name", xc.getNodes("concutest").get(0));
      Assert.fail("Should have thrown exception");
    }
    catch(IllegalArgumentException e){ }
    
    try{
      xc.getInt("doesNotExitPath/subPath", xc.getNodes("concutest").get(0));
      Assert.fail("Should have thrown exception");
    }catch(XMLConfig.XMLConfigException e){ }
    
    //Test for getInt(String path, int default)
    n = xc.getInt("concutest/name.value", 10);
    Assert.assertEquals("Found expected value", 5, n);
    n = xc.getInt("doesNotExitPath/subPath", 10);
    Assert.assertEquals("Found expected value", 10, n);
    
    //Test for getInt(String path, Node root, int defaultValue)
    n = xc.getInt("name.value", xc.getNodes("concutest").get(0), 10);
    Assert.assertEquals("Found expected value", 5, n);
    n = xc.getInt("doesNotExitPath/subPath", xc.getNodes("concutest").get(0), 10);
    Assert.assertEquals("Found expected value", 10, n);
  }
  
  public void testGetMultipleExcludesComments() throws Exception{
    XMLConfig xc = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><concutest>\n"
                                        + " Some text to actually fetch.\n"
                                        + " <!-- A comment to ignore -->\n"
                                        + "</concutest>"));
    
    List<String> ret = xc.getMultiple("concutest");
    
    Assert.assertEquals("Only fetched one item", 1, ret.size());
    Assert.assertEquals("Fetched correct text item", " Some text to actually fetch.\n".trim(), ret.get(0).trim());
  }
  
  /**
   * Tests for exceptions thrown when getNodes() is passed malformed paths
   */
  public void testGetNodesExceptions() throws Exception{
    XMLConfig xc = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><concutest>\n"
                                        + " <name type=\"only\" value=\"true\"/>\n"
                                        + " <thread value=\"false\" />\n"
                                        + " <node value=\"on\" /> \n"
                                        + " <dot value=\"off\" /> \n"
                                        + " <class name=\"sample.threadCheck.ThreadCheckSample4\"/>\n"
                                        + "</concutest>"));
    
    try{
      xc.getNodes("somePath.attr.subAttr");
      Assert.fail("Shouldn't be able to refer to sub attributes");
    }catch(XMLConfig.XMLConfigException e){
      //Needs to be kept in sync with exception message on ~496:XMLConfig
      Assert.assertEquals("Didn't get expected exception message",
                          "An attribute cannot have subparts (foo.bar.fum and foo.bar/fum not allowed)",
                          e.getMessage());
    }
    
    try{
      xc.getNodes("somePath.attr/subAttr");
      Assert.fail("Shouldn't be able to refer to a path relative to an attribute");
    }catch(XMLConfig.XMLConfigException e){
      //Needs to be kept in sync with exception message on ~496:XMLConfig
      Assert.assertEquals("Didn't get expected exception message",
                          "An attribute cannot have subparts (foo.bar.fum and foo.bar/fum not allowed)",
                          e.getMessage());
    }
    
  }
  
  public void testGetBool() throws Exception {
    XMLConfig xc = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><concutest>\n"
                                        + " <name type=\"only\" value=\"true\"/>\n"
                                        + " <thread value=\"false\" />\n"
                                        + " <node value=\"on\" /> \n"
                                        + " <dot value=\"off\" /> \n"
                                        + " <class name=\"sample.threadCheck.ThreadCheckSample4\"/>\n"
                                        + "</concutest>")); 
    Boolean b = xc.getBool("concutest/name.value") && !xc.getBool("concutest/thread.value") && xc.getBool("concutest/node.value") && !xc.getBool("concutest/dot.value");
    Assert.assertTrue("Should be true",b);
    
    try{
      xc.getBool("concutest/class.name");
      Assert.fail("Should have thrown exception");
    }
    catch(IllegalArgumentException e){ }
    
    
    //Test for getBool(String path, Node root)
    Node root = xc.getNodes("concutest").get(0);
    b = xc.getBool("name.value",root) && !xc.getBool("thread.value", root) && xc.getBool("node.value", root) && !xc.getBool("dot.value", root);
    Assert.assertTrue("Should be true",b);
    
    try{
      xc.getBool("class.name", root);
      Assert.fail("Should have thrown exception");
    }
    catch(IllegalArgumentException e){ }
    
    //getBool(String path, boolean defaultVal)
    b = xc.getBool("concutest/name.name",true);
    Assert.assertTrue("Want to get default value", b);
    
    b = xc.getBool("concutest/name.value",false);
    Assert.assertTrue("Do Not Want to get default value", b);
    
    //getBool(String path, Node root, boolean defaultVal)
    
    b = xc.getBool("name.name", root, true);
    Assert.assertTrue("Want to get default value", b);
    
    b = xc.getBool("name.value", root, false);
    Assert.assertTrue("Do Not Want to get default value", b);
  }
  
  /**
   * Test getNodePath(Node n)
   */
  public void testGetNodePath() throws Exception{
    XMLConfig xc = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><concutest>\n"
                                        + " <name type=\"only\" value=\"true\"/>\n"
                                        + " <thread value=\"false\" />\n"
                                        + " <node value=\"on\" /> \n"
                                        + " <dot value=\"off\" /> \n"
                                        + " <class name=\"sample.threadCheck.ThreadCheckSample4\"/>\n"
                                        + "</concutest>")); 
    
    Assert.assertEquals("Null does not return empty string", "", xc.getNodePath(null));
    
    Node concutest = xc.getNodes("concutest").get(0);
    
    Node name = concutest.getFirstChild();
    
    while(!name.getNodeName().equals("name"))
      name = name.getNextSibling();
    
    Assert.assertEquals("Path to name is not concutest/name", "concutest/name", xc.getNodePath(name));
  }
  
  /**
   * Test that construction of XMLConfigException's will succeed
   * Kind of pointless really.
   */
  public void testXMLConfigException() throws Exception{
    XMLConfig.XMLConfigException e1 = new XMLConfig.XMLConfigException();
    XMLConfig.XMLConfigException e2 = new XMLConfig.XMLConfigException("dummy message", null);
    XMLConfig.XMLConfigException e3 = new XMLConfig.XMLConfigException((Throwable)null);
  }
  
  /**
   * Test save(File f)
   */
  public void testSaveAndLoadConstructors() throws Exception{
    XMLConfig xc = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><concutest>\n"
                                        + " <name type=\"only\" value=\"true\"/>\n"
                                        + " <thread value=\"false\" />\n"
                                        + " <node value=\"on\" /> \n"
                                        + " <dot value=\"off\" /> \n"
                                        + " <class name=\"sample.threadCheck.ThreadCheckSample4\"/>\n"
                                        + "</concutest>")); 
    
    try{
      char c = File.separatorChar;
      xc.save(new File("." + c + "does" + c + "not" + c + "exist" + c + "file.xml"));
      Assert.fail("Should not have succeeded in saving to non-existant path");
    }catch(XMLConfig.XMLConfigException e){ }
    
    File saveTo = File.createTempFile("drjava_test", "xml");
    xc.save(saveTo);
    xc.save(saveTo.getAbsolutePath());
    
    XMLConfig xcCopy = new XMLConfig(saveTo.getAbsolutePath());
    
    try{
      char c = File.separatorChar;
      XMLConfig failCopy = new XMLConfig("." + c + "does" + c + "not" + c + "exist" + c + "file.xml");
      Assert.fail("Should not succeed in load from non-existant file");
    }catch(XMLConfig.XMLConfigException e){ }
    
    try{
      char c = File.separatorChar;
      XMLConfig failCopy = new XMLConfig(new File("." + c + "does" + c + "not" + c + "exist" + c + "file.xml"));
      Assert.fail("Should not succeed in load from non-existant file");
    }catch(XMLConfig.XMLConfigException e){ }
    
    saveTo.delete();
  }
  
  /**
   * Tests is XMLConfig constructor rejects null as parameters
   */
  public void testNullParamsinConstructor() throws Exception{
    XMLConfig xc = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><concutest>\n"
                                        + " <name type=\"only\" value=\"true\"/>\n"
                                        + " <thread value=\"false\" />\n"
                                        + " <node value=\"on\" /> \n"
                                        + " <dot value=\"off\" /> \n"
                                        + " <class name=\"sample.threadCheck.ThreadCheckSample4\"/>\n"
                                        + "</concutest>")); 
    
    Node nd = xc.getNodes("concutest").get(0);
    
    try{
      XMLConfig xc2 = new XMLConfig(null, nd);
      Assert.fail("Should not have been able to make new XMLConfig with null parent");
    }
    catch(XMLConfig.XMLConfigException e){ }
    
    try{
      XMLConfig xc2 = new XMLConfig(xc, null);
      Assert.fail("Should not have been able to make ne XMLConfig with null node");
    }
    catch(XMLConfig.XMLConfigException e){ }
  }
  
  
  /**
   * Tests XMLConfig(String filename)
   */
  public void testConstructorWithFileName() throws Exception{
    
    XMLConfig xc = 
      new XMLConfig(new StringReader(
                                     "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><concutest>\n"
                                        + " <name type=\"only\" value=\"true\"/>\n"
                                        + " <thread value=\"false\" />\n"
                                        + " <node value=\"on\" /> \n"
                                        + " <dot value=\"off\" /> \n"
                                        + " <class name=\"sample.threadCheck.ThreadCheckSample4\"/>\n"
                                        + "</concutest>")); 
    
    File saveTo = File.createTempFile("drjava_test", "xml");
    xc.save(saveTo);
    
    try{
      XMLConfig xc2 = new XMLConfig("badfileName");
    }
    catch(XMLConfig.XMLConfigException e){ }
    
    XMLConfig xc2 = new XMLConfig(saveTo.getAbsolutePath());
    
    //just makeing sure it worked right
    Boolean b = xc.getBool("concutest/name.name",true);
    Assert.assertTrue("Want to get default value", b);
    
  }
}