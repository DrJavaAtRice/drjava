/* $Id$ */

package edu.rice.cs.drjava;

import junit.framework.*;
import gj.util.Vector;
import junit.extensions.*;

public class StateBlockTest extends TestCase {
	protected ReducedModel model;
	protected Vector<StateBlock> blocks;
	
	public StateBlockTest(String name)
		{
			super(name);
		}
	
	protected void setUp()
		{
			model = new ReducedModel();
			blocks = new Vector<StateBlock>();
		}
	
	public static Test suite()
		{
			return new TestSuite(StateBlockTest.class);
		}
	protected Vector<StateBlock> setUpExample()
		{
			ReducedModel model = new ReducedModel();
			model.insertOpenSquiggly();
			model.insertNewline();
			model.insertGap(3);
			model.insertNewline();
			model.insertOpenParen();
			model.insertGap(2);
			model.insertClosedParen();
			model.insertNewline();
			model.insertGap(3);
			model.insertSlash();
			model.insertSlash();
			model.insertGap(3);
			model.insertNewline();
			model.insertQuote();
			model.insertGap(1);
			model.insertOpenSquiggly();
			model.insertGap(1);
			model.insertQuote();
			model.insertSlash();
			model.insertStar();
			model.insertGap(1);
			model.insertOpenParen();
			model.insertGap(1);
			model.insertClosedParen();
			model.insertGap(1);
			model.insertStar();
			model.insertSlash();
			model.insertNewline();
			model.insertClosedSquiggly();
			// {
			// ___
			// (__)
			// ___//___
			// "_{_"/*_(_)_*/
			// }#
			model.move(-36);
			return SBVectorFactory.generate(model._cursor, 0);
		}

	public void testExample()
		{
			Vector<StateBlock> actual = setUpExample();
			Vector<StateBlock> expected = new Vector<StateBlock>();

			expected.addElement(new StateBlock(14, 5,
																				 ReducedToken.INSIDE_LINE_COMMENT));
			expected.addElement(new StateBlock(20, 5,
																				 ReducedToken.INSIDE_QUOTE));
			expected.addElement(new StateBlock(25, 9,
																				 ReducedToken.INSIDE_BLOCK_COMMENT));

			for(int i = 0; i < expected.size(); i++)
				{
					assertTrue("#0." + i,
										 expected.elementAt(i).equals(actual.elementAt(i)));
				}
		}

	public void testDelete()
		{
			model.insertSlash();
			blocks = model.insertSlash();
			assertTrue("#0.0",
								 blocks.elementAt(0).equals(
									 new StateBlock(-2,2,
																	ReducedToken.INSIDE_LINE_COMMENT)));
			
			model.move(-1);
			blocks = model.insertStar();
			assertTrue("#1.0",
								 blocks.elementAt(0).equals(
									 new StateBlock(-2,3,
																	ReducedToken.INSIDE_BLOCK_COMMENT)));

			blocks = model.delete(-1);
			assertTrue("#2.0",
								 blocks.elementAt(0).equals(
									 new StateBlock(-1,2,
																	ReducedToken.INSIDE_LINE_COMMENT)));

			blocks = model.delete(1);
			assertTrue("#3.0", blocks.isEmpty());

			blocks = model.delete(-1);
			assertTrue("#4.0", blocks.isEmpty());			
		}

	public void testQuote()
		{
			blocks = model.insertQuote();
			assertEquals("#0.0",
									 blocks.elementAt(0),
									 new StateBlock(-1,1,
																	ReducedToken.INSIDE_QUOTE));
			
			blocks = model.insertQuote();
			assertEquals("#1.0",
									 blocks.elementAt(0),
									 new StateBlock(-1,1,ReducedToken.INSIDE_QUOTE));

			blocks = model.insertOpenSquiggly();
			assertTrue("#2.1", blocks.isEmpty());

			model.move(-2);
			blocks = model.insertQuote();
			// ""#"{
			assertEquals("#3.0",
									 blocks.elementAt(0),
									 new StateBlock(-1,3,ReducedToken.INSIDE_QUOTE));
			blocks = model.insertBackSlash();
			// ""\#"{
			assertEquals("#4.0",
									 blocks.elementAt(0),
									 new StateBlock(-2,1,ReducedToken.INSIDE_QUOTE));

			blocks = model.insertQuote();
			// ""\"#"{
			assertEquals("#5.0",
									 blocks.elementAt(0),
									 new StateBlock(0,2,ReducedToken.INSIDE_QUOTE));
			model.move(-4);
			blocks = model.delete(1);
			// #"\""{
			assertEquals("#6.0",
									 blocks.elementAt(0),
									 new StateBlock(0,4,ReducedToken.INSIDE_QUOTE));
		}
}

