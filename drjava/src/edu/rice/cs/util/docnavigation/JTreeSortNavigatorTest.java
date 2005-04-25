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

package edu.rice.cs.util.docnavigation;

import junit.framework.TestCase;

import java.io.*;
import java.util.*;

import javax.swing.tree.*;

public class JTreeSortNavigatorTest extends TestCase {
  
  protected JTreeSortNavigator tree;
  protected DefaultMutableTreeNode root;
  protected DefaultMutableTreeNode source;
  protected DefaultMutableTreeNode folder1;
  protected DefaultMutableTreeNode folder2;
  protected String projName;
  
  public void setUp() throws IOException {
    File f = File.createTempFile("project-",".pjt");
    tree = new JTreeSortNavigator(f.getCanonicalPath());
    
    tree.addTopLevelGroup("[ Source Files ]", new INavigatorItemFilter(){
      public boolean accept(INavigatorItem n){
        return true;
      }
    });
    tree.addDocument(new DummyINavigatorItem("item1"), "folder1");
    tree.addDocument(new DummyINavigatorItem("item2"), "folder1");
    tree.addDocument(new DummyINavigatorItem("item1"), "folder2");
    tree.addDocument(new DummyINavigatorItem("item2"), "folder2");
    
    root = (DefaultMutableTreeNode)tree.getModel().getRoot();
    source = (DefaultMutableTreeNode)root.getChildAt(0);
    folder1 = (DefaultMutableTreeNode)source.getChildAt(0);
    folder2 = (DefaultMutableTreeNode)source.getChildAt(1);
    
    projName = root.toString();
  }
  
  public void testGeneratePathString() {
    TreePath tp = new TreePath(root.getPath());
    assertEquals("Path String for Root", "./", tree.generatePathString(tp));
    
    tp = new TreePath(source.getPath());
    assertEquals("Path String for source", "./[ Source Files ]/", tree.generatePathString(tp));
    
    tp = new TreePath(folder1.getPath());
//    System.out.println(tree.generatePathString(tp));
    assertEquals("Path String for folder1", "./[ Source Files ]/folder1/", tree.generatePathString(tp));
    
    tp = new TreePath(folder2.getPath());
    assertEquals("Path String for folder2", "./[ Source Files ]/folder2/", tree.generatePathString(tp));
  }
  
  public void testCollapsedPaths() {
    HashSet<String> set = new HashSet<String>();
    set.add("./[ Source Files ]/folder1/");
    set.add("./[ Source Files ]/");
    // folder2 & root should not be collapsed
    
    TreePath tp1 = new TreePath(source.getPath());
    TreePath tp2 = new TreePath(folder1.getPath());
    TreePath tp3 = new TreePath(folder2.getPath());
    
    tree.collapsePaths(set);
    assertTrue("Source should be collapsed.", tree.isCollapsed(tp1));
    assertTrue("Source InnerNode should say it is collapsed.", 
               ((InnerNode)tp1.getLastPathComponent()).isCollapsed());
    assertTrue("Folder1 should be collapsed.", tree.isCollapsed(tp2));
//    System.out.println(((InnerNode)tp2.getLastPathComponent()).isCollapsed());
//    System.out.println(tree.isCollapsed(tp3));
    assertTrue("folder1 InnerNode should say it is collapsed.", 
               ((InnerNode)tp2.getLastPathComponent()).isCollapsed());
    assertTrue("Tree should say Folder2 is collapsed.", tree.isCollapsed(tp3));
    assertFalse("folder2 InnerNode should not say it is collapsed.",
                ((InnerNode)tp3.getLastPathComponent()).isCollapsed());
    
    HashSet<String> tmp = new HashSet<String>();
    for(String s : tree.getCollapsedPaths()) {
      tmp.add(s);
    }
    assertEquals("Collapsed paths given should be collapsed paths received.", set, tmp);
    
  }
  
  private String _name;
  /**
   * When the node is the only child of its parent and it is refreshed it should not
   * try to delete the parent.
   */
  public void testRenameDocument() {
    String name = "MyTest.dj0";
    String newName = "MyTest.dj0*";
    DummyINavigatorItem item = new DummyINavigatorItem(name);
    DummyINavigatorItem newItem = new DummyINavigatorItem(newName);
//    Object _lock = new Object();
//    synchronized(_lock) {
      tree.addDocument(item, "folder3");
//    }
    InnerNode folder3 = (InnerNode)source.getChildAt(2);
    assertEquals("folder3 should have 1 children", 1, folder3.getChildCount());
//    synchronized(_lock) {
      tree.refreshDocument(item, "folder3");
//    }
//    synchronized(_lock) {
      assertEquals("folder3 should have 1 children", 1, folder3.getChildCount());
      LeafNode node = (LeafNode)folder3.getChildAt(0);
      assertEquals("node should have correct name", name, node.toString());
      tree.removeDocument(item);
      tree.addDocument(newItem, "folder3");
      folder3 = (InnerNode)source.getChildAt(2);
      LeafNode newNode = (LeafNode)folder3.getChildAt(0);
      
//      tree.refreshDocument(newItem, "folder3")
      assertEquals("should have been renamed", newName, newNode.toString());
      assertEquals("node should have same parent", folder3, newNode.getParent());
      tree.removeDocument(newItem);
//    }
  }
  
}