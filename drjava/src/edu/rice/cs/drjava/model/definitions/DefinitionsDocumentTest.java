/* $Id$ */

package edu.rice.cs.drjava;

import junit.framework.*;
import java.util.Vector;
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

				ReducedModel rm = defModel._reduced;
				assertEquals("2.1",true,rm._cursor.current().isGap());
				assertEquals("2.2",6,rm._offset);
				rm._cursor.next();
				assertEquals("2.3","/*",rm._cursor.current().getType());
				rm._cursor.next();
				assertEquals("2.4",true,rm._cursor.current().isGap());
				assertEquals("2.5",ReducedToken.INSIDE_BLOCK_COMMENT,
										 rm._cursor.current().getState());
				rm._cursor.next();
				assertEquals("2.6","*/",rm._cursor.current().getType());
				rm._cursor.next();
				assertEquals("2.7","\"",rm._cursor.current().getType());
				rm._cursor.next();
				assertEquals("2.8","\\",rm._cursor.current().getType());
				rm._cursor.next();
				assertEquals("2.9","{",rm._cursor.current().getType());
				rm._cursor.next();
				assertEquals("2.91","}",rm._cursor.current().getType());
				rm._cursor.next();
				assertEquals("2.92","(",rm._cursor.current().getType());
				rm._cursor.next();
				assertEquals("2.93",")",rm._cursor.current().getType());
			}
			catch( javax.swing.text.BadLocationException e)
				{
					System.out.println("EXCEPTION");
				}
		}

	public void testDeleteDoc()
		{
			try
				{
					defModel.insertString(0,"a/*bc */",null);
					defModel.remove(3,3);
					assertEquals("#0.0","a/**/",defModel.getText(0,5));
					assertEquals("#0.1",3,defModel._currentLocation);
					
					ReducedModel rm = defModel._reduced;
					assertEquals("1.0","*/",rm._cursor.current().getType());
					assertEquals("1.1",0,rm._offset);
					assertEquals("1.2","/*",rm._cursor.prevItem().getType());
					assertEquals("1.3",ReducedToken.INSIDE_BLOCK_COMMENT,
											 rm.getStateAtCurrent());
				}
			catch( javax.swing.text.BadLocationException e)
				{
					System.out.println("EXCEPTION");
				}
		}
}





