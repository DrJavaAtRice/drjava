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
