/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.text.BadLocationException;
import junit.framework.*;
import gj.util.Vector;
import junit.extensions.*;

public class DefinitionsDocumentTest extends TestCase {
  protected DefinitionsDocument defModel;

  public DefinitionsDocumentTest(String name)
  {
    super(name);
  }

  protected void setUp()
  {
    defModel = new DefinitionsDocument();
  }

  public static Test suite()
  {
    return new TestSuite(DefinitionsDocumentTest.class);
  }

  public void testInsertToDoc()
  {
    try {
      defModel.insertString(0,"a/*bc */\"\\{}()",null);
      assertEquals("#0.0",defModel.getText(0,8),"a/*bc */");
      assertEquals("#0.1",14,defModel._currentLocation);
      defModel.insertString(0,"Start:",null);
      assertEquals("#1.0",defModel.getText(0,14),"Start:a/*bc */");
      assertEquals("#1.1",6,defModel._currentLocation);

      // document is:
      // Start:=>a/*bc */"\\{}()

      BraceReduction rm = defModel._reduced;
      assertEquals("2.1",0,rm.getStateAtCurrent());
      rm.move(2);
      // document is:
      // Start:a/=>*bc */"\\{}()
      assertEquals("2.3","/*",rm.currentToken().getType());
      rm.move(2);
      // document is:
      // Start:a/*b=>c */"\\{}()
      assertEquals("2.4",true,rm.currentToken().isGap());
      assertEquals("2.5",
                   ReducedToken.INSIDE_BLOCK_COMMENT,
                   rm.currentToken().getState());
      rm.move(2);
      // document is:
      // Start:a/*bc =>*/"\{}()
      assertEquals("2.6","*/",rm.currentToken().getType());
      rm.move(2);
      // document is:
      // Start:a/*bc */=>"\{}()
      assertEquals("2.7","\"",rm.currentToken().getType());
      rm.move(1);
      // document is:
      // Start:a/*bc */"=>\{}()
      assertEquals("2.8","\\",rm.currentToken().getType());
      rm.move(1);
      // document is:
      // Start:a/*bc */"\=>{}()
      assertEquals("2.9","{",rm.currentToken().getType());
      rm.move(1);
      // document is:
      // Start:a/*bc */"\{=>}()
      assertEquals("2.91","}",rm.currentToken().getType());
      rm.move(1);
      // document is:
      // Start:a/*bc */"\{}=>()
      assertEquals("2.92","(",rm.currentToken().getType());
      rm.move(1);
      // document is:
      // Start:a/*bc */"\\{}(=>)
      assertEquals("2.93",")",rm.currentToken().getType());
    }
    catch( javax.swing.text.BadLocationException e) {
      System.out.println("EXCEPTION");
    }
  }

  public void testDeleteDoc()
  {
    try {
      defModel.insertString(0,"a/*bc */",null);
      defModel.remove(3,3);
      assertEquals("#0.0","a/**/",defModel.getText(0,5));
      assertEquals("#0.1",3,defModel._currentLocation);

      BraceReduction rm = defModel._reduced;
      assertEquals("1.0","*/",rm.currentToken().getType());
      // no longer support getBlockOffset
      //        assertEquals("1.1",0,rm.getBlockOffset());
      rm.move(-2);
      assertEquals("1.2","/*",rm.currentToken().getType());
      rm.move(2);
      assertEquals("1.3",ReducedToken.INSIDE_BLOCK_COMMENT,
          rm.getStateAtCurrent());
    }
    catch( javax.swing.text.BadLocationException e)
    {
      System.out.println(e.toString());
    }
  }

  // make sure the vector is consistent: all elements immediately adjoin
  // one another (no overlap), and make sure all indices between start and end
  // are in the vector.
  // vector guaranteed to not have size zero
  private void _checkHighlightStatusConsistent(Vector<HighlightStatus> v,
                                               int start,
                                               int end)
  {
    // location we're at so far
    int walk = start;

    for (int i = 0; i < v.size(); i++) {
      assertEquals("Item #" + i + "in highlight vector starts at right place",
                   walk,
                   v.elementAt(i).getLocation());
      walk += v.elementAt(i).getLength();
    }

    assertEquals("Highlight vector ends at right place",
                 walk,
                 end);
  }

  public void testHighlightKeywords() throws BadLocationException {
    Vector<HighlightStatus> v;

    final String s = "public class Foo {\n" +
                     "  private int _x = 0;\n" +
                     "}";

    defModel.insertString(defModel.getLength(), s, null);
    v = defModel.getHighlightStatus(0, defModel.getLength());
    _checkHighlightStatusConsistent(v, 0, defModel.getLength());

    // Make sure the keywords are highlighted
    assertEquals("vector length", 8, v.size());
    assertEquals(HighlightStatus.KEYWORD, v.elementAt(0).getState());
    assertEquals(HighlightStatus.NORMAL, v.elementAt(1).getState());
    assertEquals(HighlightStatus.KEYWORD, v.elementAt(2).getState());
    assertEquals(HighlightStatus.NORMAL, v.elementAt(3).getState());
    assertEquals(HighlightStatus.KEYWORD, v.elementAt(4).getState());
    assertEquals(HighlightStatus.NORMAL, v.elementAt(5).getState());
    assertEquals(HighlightStatus.KEYWORD, v.elementAt(6).getState());
    assertEquals(HighlightStatus.NORMAL, v.elementAt(7).getState());
  }
}
