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
}
