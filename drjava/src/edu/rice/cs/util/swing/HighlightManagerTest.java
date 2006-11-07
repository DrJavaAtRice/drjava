/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2006 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
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

package edu.rice.cs.util.swing;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.ui.ReverseHighlighter;

import javax.swing.*;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * A JUnit test case class for the class HighlightManager.
 * Every method starting with the word "test" will be called when running
 * the test with JUnit.
 */
public class HighlightManagerTest extends DrJavaTestCase {
  
  /**
   * A test method.
   * (Replace "X" with a name describing the test.  You may write as
   * many "testSomething" methods in this class as you wish, and each
   * one will be called when running JUnit over this class.)
   */
  
  JTextComponent jtc;
  Highlighter.HighlightPainter p, p1, p2;
  HighlightManager hm;
  
  public void setUp() throws Exception {
    super.setUp();

    jtc = new JTextField();
    jtc.setHighlighter(new ReverseHighlighter());
    
    p = new ReverseHighlighter.DefaultHighlightPainter(Color.BLACK);
    p1 = new ReverseHighlighter.DefaultFrameHighlightPainter(Color.RED, 2);
    p2 = new ReverseHighlighter.DefaultHighlightPainter(Color.BLACK);
    
    hm = new HighlightManager(jtc);
    
    hm.addHighlight(0,0,p);
    hm.addHighlight(0,1,p);
  }
    
  public void testAddRemove() {
    hm.addHighlight(0,0,p);
    hm.addHighlight(0,1,p);
    assertEquals("HighlightManager add Test", 2, hm.size());
    hm.removeHighlight(0,0,p);
    assertEquals("HighlightManager remove Test 1", 1, hm.size());
    hm.removeHighlight(0,1,p);
    assertEquals("HighlightManager remove Test 1", 0, hm.size());
  }
  
  public void testHighlightInfoEquals() {
    HighlightManager.HighlightInfo hi1, hi2, hi3, hi4, hi5;
    hi1 = hm.new HighlightInfo(0,0,p);
    hi2 = hm.new HighlightInfo(0,0,p);
    hi3 = hm.new HighlightInfo(0,1,p);
    hi4 = hm.new HighlightInfo(0,0,p1);
    hi5 = hm.new HighlightInfo(0,0,p2);
    assertEquals("HighlightInfo equals test 1", hi1, hi2);
    assertFalse("HighlightInfo equals test 2", hi1.equals(hi3));
    assertFalse("HighlightInfo equals test 3", hi1.equals(hi4));
  }
}
