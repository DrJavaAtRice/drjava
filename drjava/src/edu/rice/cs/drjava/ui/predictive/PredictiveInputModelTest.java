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

package edu.rice.cs.drjava.ui.predictive;

import edu.rice.cs.drjava.DrJavaTestCase;

import java.util.List;
import java.util.Set;

/**
 * Unit tests for PredictiveInputModel class.
 */
public class PredictiveInputModelTest extends DrJavaTestCase {
  public void testInitial() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(false,
                                                                        new PredictiveInputModel.PrefixStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    assertEquals("AboutDialog.java", pim.getCurrentItem());
    assertEquals("", pim.getMask());
    assertEquals(4, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());
  }

  public void testEmpty() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(false,
                                                                        new PredictiveInputModel.PrefixStrategy<String>());
    assertEquals(null, pim.getCurrentItem());
    assertEquals("", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());
    
    pim.setMask("F");
    assertEquals(null, pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.extendMask(pim.getSharedMaskExtension());
    assertEquals(null, pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.extendMask(".");
    assertEquals(null, pim.getCurrentItem());
    assertEquals("F.", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.extendMask("x");
    assertEquals(null, pim.getCurrentItem());
    assertEquals("F.x", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.extendMask("y");
    assertEquals(null, pim.getCurrentItem());
    assertEquals("F.xy", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());
    
    pim.setItems("AboutDialog.java",
                 "FileOps.java",
                 "FileOpsTest.java",
                 "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.extendMask(pim.getSharedMaskExtension());
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.extendMask(".");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps.", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());
    assertEquals("java", pim.getSharedMaskExtension());

    pim.extendMask("x");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps.x", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.extendMask("y");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps.xy", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());
  }
  
  public void testNarrowingWithExtend() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(false,
                                                                        new PredictiveInputModel.PrefixStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.extendMask(pim.getSharedMaskExtension());
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.extendMask(".");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps.", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());
    assertEquals("java", pim.getSharedMaskExtension());

    pim.extendMask("x");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps.x", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.extendMask("y");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps.xy", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());
  }

  public void testNarrowingWithSet() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(false,
                                                                        new PredictiveInputModel.PrefixStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setMask(pim.getMask()+pim.getSharedMaskExtension());
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.setMask(pim.getMask() + ".");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps.", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());
    assertEquals("java", pim.getSharedMaskExtension());

    pim.setMask(pim.getMask() + "x");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps.x", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.setMask(pim.getMask() + "y");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps.xy", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());
  }

  public void testWidening() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(false,
                                                                        new PredictiveInputModel.PrefixStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("FileOps.");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps.", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());
    assertEquals("java", pim.getSharedMaskExtension());

    pim.setMask("FileOps");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setMask("");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("", pim.getMask());
    assertEquals(4, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());
  }

  public void testSetMatching() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(false,
                                                                        new PredictiveInputModel.PrefixStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setCurrentItem(pim.getCurrentItem());
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    List<String> m = pim.getMatchingItems();
    int i = m.indexOf(pim.getCurrentItem());
    pim.setCurrentItem(m.get(i+1));
    assertEquals("FileOpsTest.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());
  }

  public void testSetNotMatching() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(false,
                                                                        new PredictiveInputModel.PrefixStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setCurrentItem("AboutDialog.java");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setCurrentItem("Utilities.java");
    assertEquals("FileOpsTest.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());
  }

  public void testSetNotInList() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(false,
                                                                        new PredictiveInputModel.PrefixStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setCurrentItem("BBBB");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setCurrentItem("FileOps.XXX");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setCurrentItem("XXX");
    assertEquals("FileOpsTest.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());
  }
  
  public void testIgnoreCaseNarrowingWithExtend() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(true,
                                                                        new PredictiveInputModel.PrefixStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("f");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.extendMask(pim.getSharedMaskExtension().toUpperCase());
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("fILEOPS", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.extendMask(".");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("fILEOPS.", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());
    assertEquals("java", pim.getSharedMaskExtension());

    pim.extendMask("x");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("fILEOPS.x", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.extendMask("y");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("fILEOPS.xy", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());
  }

  public void testIgnoreCaseNarrowingWithSet() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(true,
                                                                        new PredictiveInputModel.PrefixStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("f");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setMask(pim.getMask()+pim.getSharedMaskExtension().toUpperCase());
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("fILEOPS", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.setMask(pim.getMask() + ".");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("fILEOPS.", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());
    assertEquals("java", pim.getSharedMaskExtension());

    pim.setMask(pim.getMask() + "x");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("fILEOPS.x", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.setMask(pim.getMask() + "y");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("fILEOPS.xy", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());
  }

  public void testIgnoreCaseWidening() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(true,
                                                                        new PredictiveInputModel.PrefixStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("fILEOPS.");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("fILEOPS.", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());
    assertEquals("java", pim.getSharedMaskExtension());

    pim.setMask("fILEOPS");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("fILEOPS", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.setMask("f");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setMask("");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("", pim.getMask());
    assertEquals(4, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());
  }

  public void testIgnoreCaseSetMatching() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(true,
                                                                        new PredictiveInputModel.PrefixStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("f");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setCurrentItem(pim.getCurrentItem());
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setCurrentItem(pim.getCurrentItem().toUpperCase());
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    List<String> m = pim.getMatchingItems();
    int i = m.indexOf(pim.getCurrentItem());
    pim.setCurrentItem(m.get(i+1).toUpperCase());
    assertEquals("FileOpsTest.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());
  }

  public void testIgnoreCaseSetNotMatching() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(true,
                                                                        new PredictiveInputModel.PrefixStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("f");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setCurrentItem("AboutDialog.java");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setCurrentItem("aboutDIALOG.JAVa");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setCurrentItem("Utilities.java");
    assertEquals("FileOpsTest.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setCurrentItem("uTILIties.JaVa");
    assertEquals("FileOpsTest.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());
  }

  public void testIgnoreCaseSetNotInList() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(true,
                                                                        new PredictiveInputModel.PrefixStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("f");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setCurrentItem("BBBB");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setCurrentItem("bbbb");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setCurrentItem("FileOps.XXX");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setCurrentItem("fILEoPS.xXx");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());

    pim.setCurrentItem("xxx");
    assertEquals("FileOpsTest.java", pim.getCurrentItem());
    assertEquals("f", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
    assertEquals("ileOps", pim.getSharedMaskExtension());
  }
  
  public void testFragmentInitial() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(false,
                                                                        new PredictiveInputModel.FragmentStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    assertEquals("AboutDialog.java", pim.getCurrentItem());
    assertEquals("", pim.getMask());
    assertEquals(4, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());
  }

  public void testFragmentEmpty() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(false,
                                                                        new PredictiveInputModel.FragmentStrategy<String>());
    assertEquals(null, pim.getCurrentItem());
    assertEquals("", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    
    pim.setMask("F");
    assertEquals(null, pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());

    pim.extendMask(pim.getSharedMaskExtension());
    assertEquals(null, pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());

    pim.extendMask(".");
    assertEquals(null, pim.getCurrentItem());
    assertEquals("F.", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());

    pim.extendMask("x");
    assertEquals(null, pim.getCurrentItem());
    assertEquals("F.x", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());

    pim.extendMask("y");
    assertEquals(null, pim.getCurrentItem());
    assertEquals("F.xy", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
        
    pim.setMask("i .java");
    assertEquals(null, pim.getCurrentItem());
    assertEquals("i .java", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());

    pim.setItems("AboutDialog.java",
                 "FileOps.java",
                 "FileOpsTest.java",
                 "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.extendMask("ileOps");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.extendMask(".");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps.", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());

    pim.extendMask("x");
    assertEquals("AboutDialog.java", pim.getCurrentItem());
    assertEquals("FileOps.x", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());

    pim.extendMask("y");
    assertEquals("AboutDialog.java", pim.getCurrentItem());
    assertEquals("FileOps.xy", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    
    pim.setMask("File java");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("File java", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
  }
  
  public void testFragmentNarrowingWithExtend() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(false,
                                                                        new PredictiveInputModel.FragmentStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.extendMask(" ileOps");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F ileOps", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.extendMask(".");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F ileOps.", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());

    pim.extendMask("x");
    assertEquals("AboutDialog.java", pim.getCurrentItem());
    assertEquals("F ileOps.x", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());

    pim.extendMask("y");
    assertEquals("AboutDialog.java", pim.getCurrentItem());
    assertEquals("F ileOps.xy", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
  }

  public void testFragmentNarrowingWithSet() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(false,
                                                                        new PredictiveInputModel.FragmentStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setMask("F ileOps");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F ileOps", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setMask(pim.getMask() + ".");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F ileOps.", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());

    pim.setMask(pim.getMask() + "x");
    assertEquals("AboutDialog.java", pim.getCurrentItem());
    assertEquals("F ileOps.x", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());

    pim.setMask(pim.getMask() + "y");
    assertEquals("AboutDialog.java", pim.getCurrentItem());
    assertEquals("F ileOps.xy", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
  }

  public void testFragmentWidening() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(false,
                                                                        new PredictiveInputModel.FragmentStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("File Ops.");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("File Ops.", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());

    pim.setMask("File Ops");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("File Ops", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setMask("F i");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F i", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setMask("");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("", pim.getMask());
    assertEquals(4, pim.getMatchingItems().size());
  }

  public void testFragmentSetMatching() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(false,
                                                                        new PredictiveInputModel.FragmentStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setCurrentItem(pim.getCurrentItem());
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    List<String> m = pim.getMatchingItems();
    int i = m.indexOf(pim.getCurrentItem());
    pim.setCurrentItem(m.get(i+1));
    assertEquals("FileOpsTest.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
  }

  public void testFragmentSetNotMatching() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(false,
                                                                        new PredictiveInputModel.FragmentStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setCurrentItem("AboutDialog.java");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setCurrentItem("Utilities.java");
    assertEquals("FileOpsTest.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
  }

  public void testFragmentSetNotInList() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(false,
                                                                        new PredictiveInputModel.FragmentStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setCurrentItem("BBBB");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setCurrentItem("FileOps.XXX");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setCurrentItem("XXX");
    assertEquals("FileOpsTest.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
  }

  public void testFragmentIgnoreCaseEmpty() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(true,
                                                                        new PredictiveInputModel.FragmentStrategy<String>());
    assertEquals(null, pim.getCurrentItem());
    assertEquals("", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    
    pim.setMask("F");
    assertEquals(null, pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());

    pim.extendMask(".");
    assertEquals(null, pim.getCurrentItem());
    assertEquals("F.", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());

    pim.extendMask("x");
    assertEquals(null, pim.getCurrentItem());
    assertEquals("F.x", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());

    pim.extendMask("y");
    assertEquals(null, pim.getCurrentItem());
    assertEquals("F.xy", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
        
    pim.setMask("I .JAVa");
    assertEquals(null, pim.getCurrentItem());
    assertEquals("I .JAVa", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());

    pim.setItems("AboutDialog.java",
                 "FileOps.java",
                 "FileOpsTest.java",
                 "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.extendMask("ILEoPS");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FILEoPS", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.extendMask(".");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FILEoPS.", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());

    pim.extendMask("x");
    assertEquals("AboutDialog.java", pim.getCurrentItem());
    assertEquals("FILEoPS.x", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());

    pim.extendMask("y");
    assertEquals("AboutDialog.java", pim.getCurrentItem());
    assertEquals("FILEoPS.xy", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    
    pim.setMask("FiLe JAVa");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FiLe JAVa", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
  }
  
  public void testFragmentIgnoreCaseNarrowingWithExtend() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(true,
                                                                        new PredictiveInputModel.FragmentStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.extendMask(" IlEOpS");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F IlEOpS", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.extendMask(".");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F IlEOpS.", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());

    pim.extendMask("x");
    assertEquals("AboutDialog.java", pim.getCurrentItem());
    assertEquals("F IlEOpS.x", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());

    pim.extendMask("y");
    assertEquals("AboutDialog.java", pim.getCurrentItem());
    assertEquals("F IlEOpS.xy", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
  }

  public void testFragmentIgnoreCaseNarrowingWithSet() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(true,
                                                                        new PredictiveInputModel.FragmentStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setMask("F IlEOpS");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F IlEOpS", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setMask(pim.getMask() + ".");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F IlEOpS.", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());

    pim.setMask(pim.getMask() + "x");
    assertEquals("AboutDialog.java", pim.getCurrentItem());
    assertEquals("F IlEOpS.x", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());

    pim.setMask(pim.getMask() + "y");
    assertEquals("AboutDialog.java", pim.getCurrentItem());
    assertEquals("F IlEOpS.xy", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
  }

  public void testFragmentIgnoreCaseWidening() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(true,
                                                                        new PredictiveInputModel.FragmentStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("FiLE oPS.");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FiLE oPS.", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());

    pim.setMask("FiLE oPS");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FiLE oPS", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setMask("f I");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("f I", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setMask("");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("", pim.getMask());
    assertEquals(4, pim.getMatchingItems().size());
  }

  public void testFragmentIgnoreCaseSetMatching() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(true,
                                                                        new PredictiveInputModel.FragmentStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setCurrentItem(pim.getCurrentItem().toUpperCase());
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    List<String> m = pim.getMatchingItems();
    int i = m.indexOf(pim.getCurrentItem());
    pim.setCurrentItem(m.get(i+1).toUpperCase());
    assertEquals("FileOpsTest.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
  }

  public void testFragmentIgnoreCaseSetNotMatching() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(true,
                                                                        new PredictiveInputModel.FragmentStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setCurrentItem("AboutDialog.java");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setCurrentItem("Utilities.java");
    assertEquals("FileOpsTest.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
  }

  public void testFragmentIgnoreCaseSetNotInList() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(true,
                                                                        new PredictiveInputModel.FragmentStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pim.setMask("F");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setCurrentItem("BBBB");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setCurrentItem("FileOps.XXX");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setCurrentItem("XXX");
    assertEquals("FileOpsTest.java", pim.getCurrentItem());
    assertEquals("F", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());
  }
  
  public void testRegExStrategy() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(false,
                                                                        new PredictiveInputModel.RegExStrategy<String>(),
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java",
                                                                        "NewFileOps.java");
    pim.setMask("^F.*");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("^F.*", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setCurrentItem(pim.getCurrentItem());
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("^F.*", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    List<String> m = pim.getMatchingItems();
    int i = m.indexOf(pim.getCurrentItem());
    pim.setCurrentItem(m.get(i+1));
    assertEquals("FileOpsTest.java", pim.getCurrentItem());
    assertEquals("^F.*", pim.getMask());
    assertEquals(2, pim.getMatchingItems().size());

    pim.setMask("[^F].*F.*");
    assertEquals("NewFileOps.java", pim.getCurrentItem());
    assertEquals("[^F].*F.*", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());
  }

  public void testFragmentLineNumStrategy() {
    PredictiveInputModel<String> pim = new PredictiveInputModel<String>(true,
                                                                        new PredictiveInputModel.FragmentLineNumStrategy<String>(),
                                                                        "Frame",
                                                                        "JFrame",
                                                                        "Window",
                                                                        "JWindow",
                                                                        "Test");
    pim.setMask("");
    assertEquals(5, pim.getMatchingItems().size());
    assertTrue(pim.getMatchingItems().contains("Frame"));
    assertTrue(pim.getMatchingItems().contains("JFrame"));
    assertTrue(pim.getMatchingItems().contains("Window"));
    assertTrue(pim.getMatchingItems().contains("JWindow"));
    assertTrue(pim.getMatchingItems().contains("Test"));

    pim.setMask("f");
    assertEquals(2, pim.getMatchingItems().size());
    assertTrue(pim.getMatchingItems().contains("Frame"));
    assertTrue(pim.getMatchingItems().contains("JFrame"));
  }

  public void testJavaAPIFragmentLineNumStrategy() {
    final String base = edu.rice.cs.drjava.DrJava.
      getConfig().getSetting(edu.rice.cs.drjava.config.OptionConstants.JAVADOC_1_5_LINK) + "/";
    final String stripPrefix = ""; // nothing needs to be stripped, links in 1.4 Javadoc are relative
    final String suffix = "/allclasses-1.5.html";
    Set<edu.rice.cs.drjava.ui.MainFrameStatics.JavaAPIListEntry> l = 
      edu.rice.cs.drjava.ui.MainFrame._generateJavaAPISet(base,
                                                          stripPrefix,
                                                          suffix);
    assertTrue(l.size() > 0);
  }
}
