/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui.predictive;

import edu.rice.cs.drjava.DrJavaTestCase;

import java.util.List;

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
    
    pim.setList("AboutDialog.java",
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

    pim.setMask(pim.getMask()+".");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps.", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());
    assertEquals("java", pim.getSharedMaskExtension());

    pim.setMask(pim.getMask()+"x");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("FileOps.x", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.setMask(pim.getMask()+"y");
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

    pim.setMask(pim.getMask()+".");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("fILEOPS.", pim.getMask());
    assertEquals(1, pim.getMatchingItems().size());
    assertEquals("java", pim.getSharedMaskExtension());

    pim.setMask(pim.getMask()+"x");
    assertEquals("FileOps.java", pim.getCurrentItem());
    assertEquals("fILEOPS.x", pim.getMask());
    assertEquals(0, pim.getMatchingItems().size());
    assertEquals("", pim.getSharedMaskExtension());

    pim.setMask(pim.getMask()+"y");
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
}
