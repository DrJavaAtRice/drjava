 /*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.util.docnavigation;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.drjava.DrJavaTestCase;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

public class JTreeSortNavigatorTest extends DrJavaTestCase {
  
  protected JTreeSortNavigator<DummyINavigatorItem> tree;
  protected DefaultMutableTreeNode root;
  protected DefaultMutableTreeNode source;
  protected DefaultMutableTreeNode folder1;
  protected DefaultMutableTreeNode folder2;
  protected String projName;
  protected DummyINavigatorItem i1, i2, i3, i4;
  
  protected DefaultMutableTreeNode aux;
  protected DefaultMutableTreeNode auxFolder1;
  protected DefaultMutableTreeNode auxFolder2;
  protected DummyINavigatorItem auxi1, auxi2, auxi3, auxi4, auxi5;
  
  protected final String SOURCE_BIN_NAME = "[ Source Files ]";
  protected final String EXTERNAL_BIN_NAME = "[ External Files ]";
  
  public void setUp() throws Exception {
    super.setUp();
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {

        try {
          File f = File.createTempFile("project-",".pjt").getCanonicalFile();
          tree = new JTreeSortNavigator<DummyINavigatorItem>(f.getCanonicalPath());
          
          tree.addTopLevelGroup(SOURCE_BIN_NAME, new INavigatorItemFilter<INavigatorItem>(){
            public boolean accept(INavigatorItem n) { return !n.getName().startsWith("aux"); }
          });
          tree.addTopLevelGroup(EXTERNAL_BIN_NAME, new INavigatorItemFilter<INavigatorItem>(){
            public boolean accept(INavigatorItem n) { return n.getName().startsWith("aux"); }
          });

          i1 = new DummyINavigatorItem("item11");
          i2 = new DummyINavigatorItem("item12");
          i3 = new DummyINavigatorItem("item21");
          i4 = new DummyINavigatorItem("item22");
          tree.addDocument(i1, "folder1");
          tree.addDocument(i2, "folder1");
          tree.addDocument(i3, "folder2");
          tree.addDocument(i4, "folder2");
          
          auxi1 = new DummyINavigatorItem("auxitem11");
          auxi2 = new DummyINavigatorItem("auxitem12");
          auxi3 = new DummyINavigatorItem("auxitem21");
          auxi4 = new DummyINavigatorItem("auxitem22");
          auxi5 = new DummyINavigatorItem("auxitem23");
          tree.addDocument(auxi1, "auxfolder1");
          tree.addDocument(auxi2, "auxfolder1");
          tree.addDocument(auxi3, "auxfolder2");
          tree.addDocument(auxi4, "auxfolder2");
          tree.addDocument(auxi5, "auxfolder2");
          
          root = (DefaultMutableTreeNode)tree.getModel().getRoot();
          source = (DefaultMutableTreeNode)root.getChildAt(0);
          folder1 = (DefaultMutableTreeNode)source.getChildAt(0);
          folder2 = (DefaultMutableTreeNode)source.getChildAt(1);
          aux = (DefaultMutableTreeNode)root.getChildAt(1);
          auxFolder1 = (DefaultMutableTreeNode)aux.getChildAt(0);
          auxFolder2 = (DefaultMutableTreeNode)aux.getChildAt(1);
          
          projName = root.toString();
        }
        catch(Exception e) { throw new UnexpectedException(e); }
      }
    });
  }
  
  public void testTraversalOps() {
    assertEquals("doc count test", 9, tree.getDocumentCount());
    assertSame("getFirst test", i1, tree.getFirst());
    assertSame("getLast test", auxi5, tree.getLast());
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        tree.setNextChangeModelInitiated(true);
        tree.selectDocument(i1);
      }
    });
    assertSame("getCurrent test", i1, tree.getCurrent());
    assertSame("getNext test 1", i2, tree.getNext(i1));
    assertSame("getNext test 2", i3, tree.getNext(i2));
    assertSame("getNext test 3", i4, tree.getNext(i3));

    assertSame("getPrevious test 1", i3, tree.getPrevious(i4));
    assertSame("getPrevious test 2", i2, tree.getPrevious(i3));
    assertSame("getPrevious test 3", i1, tree.getPrevious(i2));
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
               ((InnerNode<?, ?>)tp1.getLastPathComponent()).isCollapsed());
    assertTrue("Folder1 should be collapsed.", tree.isCollapsed(tp2));
//    System.out.println(((InnerNode)tp2.getLastPathComponent()).isCollapsed());
//    System.out.println(tree.isCollapsed(tp3));
    assertTrue("folder1 InnerNode should say it is collapsed.", 
               ((InnerNode<?, ?>)tp2.getLastPathComponent()).isCollapsed());
    assertTrue("Tree should say Folder2 is collapsed.", tree.isCollapsed(tp3));
    assertFalse("folder2 InnerNode should not say it is collapsed.",
                ((InnerNode<?, ?>)tp3.getLastPathComponent()).isCollapsed());
    
    HashSet<String> tmp = new HashSet<String>();
    for(String s : tree.getCollapsedPaths()) {
      tmp.add(s);
    }
    assertEquals("Collapsed paths given should be collapsed paths received.", set, tmp);
    
  }
  
//  private String _name;
  /** When the node is the only child of its parent and it is refreshed it should not
   * try to delete the parent.
   */
  public void testRenameDocument() {
    final String name = "MyTest.dj0";
    final String newName = "MyTest.dj0*";
    final DummyINavigatorItem item = new DummyINavigatorItem(name);
    final DummyINavigatorItem newItem = new DummyINavigatorItem(newName);
    Utilities.invokeAndWait(new Runnable() { public void run() { tree.addDocument(item, "folder3"); } });
    InnerNode<?,?> folder3 = (InnerNode<?,?>)source.getChildAt(2);
    assertEquals("folder3 should have 1 children", 1, folder3.getChildCount());
    Utilities.invokeAndWait(new Runnable() { public void run() { tree.refreshDocument(item, "folder3"); } });
    assertEquals("folder3 should have 1 children", 1, folder3.getChildCount());
    LeafNode<?> node = (LeafNode<?>)folder3.getChildAt(0);
    assertEquals("node should have correct name", name, node.toString());
    Utilities.invokeAndWait(new Runnable() {
      public void run() { 
        tree.removeDocument(item);
        tree.addDocument(newItem, "folder3");
      }
    });
    folder3 = (InnerNode<?,?>) source.getChildAt(2);
    LeafNode<?> newNode = (LeafNode<?>) folder3.getChildAt(0);
    
//      tree.refreshDocument(newItem, "folder3")
    assertEquals("should have been renamed", newName, newNode.toString());
    assertEquals("node should have same parent", folder3, newNode.getParent());
    Utilities.invokeAndWait(new Runnable() { public void run() { tree.removeDocument(newItem); } });
  }
  
  /** Test the enumeration of items based on top-level bins. */
  public void testGetDocumentsInBin() {
    Iterator<DummyINavigatorItem> items = tree.getDocumentsInBin(SOURCE_BIN_NAME).iterator();
    DummyINavigatorItem d;
    assertTrue(SOURCE_BIN_NAME + " bin should not have 0 items", items.hasNext());
    d = items.next();
    assertEquals("Wrong item 1", i1, d);
    assertTrue(SOURCE_BIN_NAME + " bin should not have 1 item", items.hasNext());
    d = items.next();
    assertEquals("Wrong item 2", i2, d);
    assertTrue(SOURCE_BIN_NAME + " bin should not have 2 items", items.hasNext());
    d = items.next();
    assertEquals("Wrong item 3", i3, d);
    assertTrue(SOURCE_BIN_NAME + " bin should not have 3 items", items.hasNext());
    d = items.next();
    assertEquals("Wrong item 4", i4, d);
    assertFalse(SOURCE_BIN_NAME + " bin should not have 4 items", items.hasNext());
    
    items = tree.getDocumentsInBin(EXTERNAL_BIN_NAME).iterator();
    assertTrue(EXTERNAL_BIN_NAME + " bin should not have 0 items", items.hasNext());
    d = items.next();
    assertEquals("Wrong item 1", auxi1, d);
    assertTrue(EXTERNAL_BIN_NAME + " bin should not have 1 item", items.hasNext());
    d = items.next();
    assertEquals("Wrong item 2", auxi2, d);
    assertTrue(EXTERNAL_BIN_NAME + " bin should not have 2 items", items.hasNext());
    d = items.next();
    assertEquals("Wrong item 3", auxi3, d);
    assertTrue(EXTERNAL_BIN_NAME + " bin should not have 3 items", items.hasNext());
    d = items.next();
    assertEquals("Wrong item 4", auxi4, d);
    assertTrue(EXTERNAL_BIN_NAME + " bin should not have 4 items", items.hasNext());
    d = items.next();
    assertEquals("Wrong item 5", auxi5, d);
    assertFalse(EXTERNAL_BIN_NAME + " bin should not have 5 items", items.hasNext());
  }
  
  /** Test of getting the list of selected items.
   */
  public void testGetSelectedDocuments() {
    tree.clearSelection();
    tree.addSelectionRows(new int[] { 3, 4 });
    java.util.List<DummyINavigatorItem> l = tree.getSelectedDocuments();
    assertEquals("Two items should be selected", 2, l.size());
    assertEquals("Wrong item 1", i1, l.get(0));
    assertEquals("Wrong item 2", i2, l.get(1));

    tree.clearSelection();
    tree.addSelectionRows(new int[] { 3, 4, 6, 7 });
    l = tree.getSelectedDocuments();
    assertEquals("Four items should be selected", 4, l.size());
    assertEquals("Wrong item 1", i1, l.get(0));
    assertEquals("Wrong item 2", i2, l.get(1));
    assertEquals("Wrong item 3", i3, l.get(2));
    assertEquals("Wrong item 4", i4, l.get(3));

    tree.clearSelection();
    tree.addSelectionRows(new int[] { 1, 2, 3, 4, 5, 6, 7 });
    l = tree.getSelectedDocuments();
    assertEquals("Four items should be selected", 4, l.size());
    assertEquals("Wrong item 1", i1, l.get(0));
    assertEquals("Wrong item 2", i2, l.get(1));
    assertEquals("Wrong item 3", i3, l.get(2));
    assertEquals("Wrong item 4", i4, l.get(3));

    tree.clearSelection();
    tree.addSelectionRows(new int[] { 1, 2, 3, 5, 6 });
    l = tree.getSelectedDocuments();
    assertEquals("Two items should be selected", 2, l.size());
    assertEquals("Wrong item 1", i1, l.get(0));
    assertEquals("Wrong item 2", i3, l.get(1));
    
    tree.clearSelection();
    tree.addSelectionRows(new int[] { 10, 11 });
    l = tree.getSelectedDocuments();
    assertEquals("Two items should be selected", 2, l.size());
    assertEquals("Wrong item 1", auxi1, l.get(0));
    assertEquals("Wrong item 2", auxi2, l.get(1));

    tree.clearSelection();
    tree.addSelectionRows(new int[] { 10, 11, 13, 14, 15 });
    l = tree.getSelectedDocuments();
    assertEquals("Five items should be selected", 5, l.size());
    assertEquals("Wrong item 1", auxi1, l.get(0));
    assertEquals("Wrong item 2", auxi2, l.get(1));
    assertEquals("Wrong item 3", auxi3, l.get(2));
    assertEquals("Wrong item 4", auxi4, l.get(3));
    assertEquals("Wrong item 5", auxi5, l.get(4));

    tree.clearSelection();
    tree.addSelectionRows(new int[] { 8, 9, 10, 11, 12, 13, 14, 15 });
    l = tree.getSelectedDocuments();
    assertEquals("Five items should be selected", 5, l.size());
    assertEquals("Wrong item 1", auxi1, l.get(0));
    assertEquals("Wrong item 2", auxi2, l.get(1));
    assertEquals("Wrong item 3", auxi3, l.get(2));
    assertEquals("Wrong item 4", auxi4, l.get(3));
    assertEquals("Wrong item 5", auxi5, l.get(4));

    tree.clearSelection();
    tree.addSelectionRows(new int[] { 8, 9, 10, 12, 13 });
    l = tree.getSelectedDocuments();
    assertEquals("Two items should be selected", 2, l.size());
    assertEquals("Wrong item 1", auxi1, l.get(0));
    assertEquals("Wrong item 2", auxi3, l.get(1));
  }
  
  /** Test of getting the list of selected items.
   */
  public void testGetNamesOfSelectedTopLevelGroup() {
    java.util.Set<String> s = null;
    
    tree.clearSelection();
    tree.addSelectionRows(new int[] { 3, 4 });
    try {
      s = tree.getNamesOfSelectedTopLevelGroup();
    }
    catch(GroupNotSelectedException e) {
      fail("getNamesOfSelectedTopLevelGroup threw " + e);
    }
    assertEquals("Only one top-level group should be selected", 1, s.size());
    assertTrue("Wrong top-level group", s.contains(SOURCE_BIN_NAME));

    tree.clearSelection();
    tree.addSelectionRows(new int[] { 3, 4, 6, 7 });
    try {
      s = tree.getNamesOfSelectedTopLevelGroup();
    }
    catch(GroupNotSelectedException e) {
      fail("getNamesOfSelectedTopLevelGroup threw " + e);
    }
    assertEquals("Only one top-level group should be selected", 1, s.size());
    assertTrue("Wrong top-level group", s.contains(SOURCE_BIN_NAME));

    tree.clearSelection();
    tree.addSelectionRows(new int[] { 1, 2, 3, 4, 5, 6, 7 });
    try {
      s = tree.getNamesOfSelectedTopLevelGroup();
    }
    catch(GroupNotSelectedException e) {
      fail("getNamesOfSelectedTopLevelGroup threw " + e);
    }
    assertEquals("Only one top-level group should be selected", 1, s.size());
    assertTrue("Wrong top-level group", s.contains(SOURCE_BIN_NAME));

    tree.clearSelection();
    tree.addSelectionRows(new int[] { 1, 2, 3, 5, 6 });
    try {
      s = tree.getNamesOfSelectedTopLevelGroup();
    }
    catch(GroupNotSelectedException e) {
      fail("getNamesOfSelectedTopLevelGroup threw " + e);
    }
    assertEquals("Only one top-level group should be selected", 1, s.size());
    assertTrue("Wrong top-level group", s.contains(SOURCE_BIN_NAME));
    
    tree.clearSelection();
    tree.addSelectionRows(new int[] { 10, 11 });
    try {
      s = tree.getNamesOfSelectedTopLevelGroup();
    }
    catch(GroupNotSelectedException e) {
      fail("getNamesOfSelectedTopLevelGroup threw " + e);
    }
    assertEquals("Only one top-level group should be selected", 1, s.size());
    assertEquals("Wrong top-level group 1", EXTERNAL_BIN_NAME, s.toArray()[0]);

    tree.clearSelection();
    tree.addSelectionRows(new int[] { 10, 11, 13, 14, 15 });
    try {
      s = tree.getNamesOfSelectedTopLevelGroup();
    }
    catch(GroupNotSelectedException e) {
      fail("getNamesOfSelectedTopLevelGroup threw " + e);
    }
    assertEquals("Only one top-level group should be selected", 1, s.size());
    assertEquals("Wrong top-level group 1", EXTERNAL_BIN_NAME, s.toArray()[0]);

    tree.clearSelection();
    tree.addSelectionRows(new int[] { 8, 9, 10, 11, 12, 13, 14, 15 });
    try {
      s = tree.getNamesOfSelectedTopLevelGroup();
    }
    catch(GroupNotSelectedException e) {
      fail("getNamesOfSelectedTopLevelGroup threw " + e);
    }
    assertEquals("Only one top-level group should be selected", 1, s.size());
    assertEquals("Wrong top-level group 1", EXTERNAL_BIN_NAME, s.toArray()[0]);

    tree.clearSelection();
    tree.addSelectionRows(new int[] { 8, 9, 10, 12, 13 });
    try {
      s = tree.getNamesOfSelectedTopLevelGroup();
    }
    catch(GroupNotSelectedException e) {
      fail("getNamesOfSelectedTopLevelGroup threw " + e);
    }
    assertEquals("Only one top-level group should be selected", 1, s.size());
    assertEquals("Wrong top-level group 1", EXTERNAL_BIN_NAME, s.toArray()[0]);
    
    tree.clearSelection();
    tree.addSelectionRows(new int[] { 3, 13 });
    try {
      s = tree.getNamesOfSelectedTopLevelGroup();
    }
    catch(GroupNotSelectedException e) {
      fail("getNamesOfSelectedTopLevelGroup threw " + e);
    }
    assertEquals("Two top-level groups should be selected", 2, s.size());
    assertTrue("Wrong top-level group", s.contains(SOURCE_BIN_NAME));
    assertTrue("Wrong top-level group", s.contains(EXTERNAL_BIN_NAME));
    
    tree.clearSelection();
    try {
      s = tree.getNamesOfSelectedTopLevelGroup();
      fail("Didn't through");
    }
    catch(Exception e) {
      assertEquals("Exception isn't a GroupNotSelectedException", GroupNotSelectedException.class, e.getClass());
    }
  }
}