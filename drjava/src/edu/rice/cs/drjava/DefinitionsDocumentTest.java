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
					
				ReducedModel rm = defModel._reduced;
				assertEquals("1.0","*/",rm._cursor.current().getType());
				assertEquals("1.1",0,rm._offset);
				assertEquals("1.2","/*",rm._cursor.prevItem().getType());
				assertEquals("1.3",ReducedToken.INSIDE_BLOCK_COMMENT,
										 rm.getStateAtCurrent());
			}
			catch( javax.swing.text.BadLocationException e)
				{
					System.out.println(e.toString());
				}
		}

	public void testStateChangeDoc()
		{
			try {
				Vector<StateBlock> actual;
				Vector<StateBlock> expected = new Vector<StateBlock>();

				defModel.insertString(0,"a/*bc */\"\\{}()",null);

				assertEquals("#0.0",true, defModel.hasHighlightChanged());
				actual = defModel.getHighLightInformation();
				expected.addElement(new StateBlock(1, 7,
																					 ReducedToken.INSIDE_BLOCK_COMMENT));
				assertTrue("#0.1",actual.elementAt(0).equals(expected.elementAt(0)));
				assertEquals("#0.2",2,actual.size());
				defModel.insertString(0,"Start:",null);
//				assertEquals("#0.0",false, defModel.hasHighlightChanged());
			}
			catch (BadLocationException e) {
				throw new RuntimeException(e.toString());
			}
		}

	public void testStateChangeDoc2()
		{
			try {
				Vector<StateBlock> actual = new Vector<StateBlock>();
				Vector<StateBlock> expected = new Vector<StateBlock>();

				defModel.insertString(0,"/*bc */ //ad}()",null);

				assertEquals("#0.0",true, defModel.hasHighlightChanged());

				actual = defModel.getHighLightInformation();
				expected.addElement(new StateBlock(0, 7,
																					 ReducedToken.INSIDE_BLOCK_COMMENT));
				expected.addElement(new StateBlock(8, 7,
																					 ReducedToken.INSIDE_LINE_COMMENT));
			
				assertTrue("#0.1",actual.elementAt(0).equals(expected.elementAt(0)));
				assertTrue("#0.2",actual.elementAt(1).equals(expected.elementAt(1)));
				assertEquals("#0.3",actual.size(),2);
			
				expected = new Vector<StateBlock>();
			
				defModel.insertString(1,"\"hehe\"",null);
				// /"hehe"*bc */ //ad}()
							
				actual = defModel.getHighLightInformation();
				expected.addElement(new StateBlock(1, 6,
																					 ReducedToken.INSIDE_QUOTE));
				expected.addElement(new StateBlock(14, 7,
																					 ReducedToken.INSIDE_LINE_COMMENT));
				assertEquals("#0.4",true, defModel.hasHighlightChanged());
				assertEquals("#0.5",expected.elementAt(0),actual.elementAt(0));
				assertEquals("#0.6",expected.elementAt(1),actual.elementAt(1));
				assertEquals("#0.7",actual.size(),2);
			}
			catch (BadLocationException e) {
				throw new RuntimeException(e.toString());
			}

		}

	
	public void testStateChangeOnRemove()
		{
			try {
				Vector<StateBlock> actual;
				Vector<StateBlock> expected = new Vector<StateBlock>();
				defModel.insertString(0,"/*bc */ //ad}()",null);
				defModel.remove(13,2);
				//			assertEquals("#0.1",false, defModel.hasHighlightChanged());
				defModel.remove(4,1);
				// /*bc*/ //ad}
				actual = defModel.getHighLightInformation();
				expected.addElement(new StateBlock(0, 6,
																					 ReducedToken.INSIDE_BLOCK_COMMENT));
				expected.addElement(new StateBlock(7, 5,
																					 ReducedToken.INSIDE_LINE_COMMENT));
				
				assertEquals("#0.2",2,actual.size());
				assertEquals("#0.3",expected.elementAt(0),actual.elementAt(0));
				assertEquals("#0.4",expected.elementAt(1),actual.elementAt(1));
				
			}
			catch (BadLocationException e) {
				throw new RuntimeException(e.toString());
			}
		}

	
	public void testInsertBetween()
		{
			try {
				Vector<StateBlock> actual = new Vector<StateBlock>();
				Vector<StateBlock> expected = new Vector<StateBlock>();

				defModel.insertString(0,"{}\"\"/*bcnrqu */jl/}()",null);

				assertEquals("#0.0",true, defModel.hasHighlightChanged());
				actual = defModel.getHighLightInformation();
				expected.addElement(new StateBlock(4, 11,
																					 ReducedToken.INSIDE_BLOCK_COMMENT));
				defModel.insertString(6,"1",null);
				assertEquals("0.1",true,defModel.hasHighlightChanged());

				actual = defModel.getHighLightInformation();
				expected = new Vector<StateBlock>();
				expected.addElement(new StateBlock(4, 12,
																					 ReducedToken.INSIDE_BLOCK_COMMENT));

				defModel.insertString(9,"2",null);
				assertEquals("0.1",true,defModel.hasHighlightChanged());

				actual = defModel.getHighLightInformation();
				expected = new Vector<StateBlock>();
				expected.addElement(new StateBlock(4, 13,
																					 ReducedToken.INSIDE_BLOCK_COMMENT));
			}
			catch (BadLocationException e) {
				throw new RuntimeException(e.toString());
			}

		}


	public void testInsertBetweenFormNewBrace()
		{
			try {
				Vector<StateBlock> actual = new Vector<StateBlock>();
				Vector<StateBlock> expected = new Vector<StateBlock>();

				defModel.insertString(0,"abcd/ddd",null);

//				assertEquals("#0.0",false, defModel.hasHighlightChanged());

				defModel.insertString(5,"/",null);
				assertEquals("#0.1",true, defModel.hasHighlightChanged());

				actual = defModel.getHighLightInformation();
				expected = new Vector<StateBlock>();
				expected.addElement(new StateBlock(4, 5,
																					 ReducedToken.INSIDE_LINE_COMMENT));
			}
			catch (BadLocationException e) {
				throw new RuntimeException(e.toString());
			}
			
		}

	public void testInsertBetweenFormNewBrace2()
		{
			try {
				Vector<StateBlock> actual = new Vector<StateBlock>();
				Vector<StateBlock> expected = new Vector<StateBlock>();

				defModel.insertString(0,"\"",null);

				assertEquals("#0.0",true, defModel.hasHighlightChanged());

				actual = defModel.getHighLightInformation();
				expected = new Vector<StateBlock>();
				expected.addElement(new StateBlock(0, 1,
																					 ReducedToken.INSIDE_QUOTE));
			}
			catch (BadLocationException e) {
				throw new RuntimeException(e.toString());
			}			
		}
}







