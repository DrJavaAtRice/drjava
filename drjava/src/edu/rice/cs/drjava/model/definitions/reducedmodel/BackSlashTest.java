/* $Id$ */

package edu.rice.cs.drjava;

import junit.framework.*;
import java.util.Vector;
import junit.extensions.*;

public class BackSlashTest extends TestCase {
	protected ReducedModel model0;
	protected ReducedModel model1;
	protected ReducedModel model2;
	
	public BackSlashTest(String name)
		{
			super(name);
		}
	
	protected void setUp()
		{
			model0 = new ReducedModel();
			model1 = new ReducedModel();
			model2 = new ReducedModel();
		}
	
	public static Test suite()
		{
			return new TestSuite(BackSlashTest.class);
		}

	public void testInsideQuotePrevious()
		{
			model1.insertQuote();
			model1.insertBackSlash();
			model1.insertQuote();
			assertEquals("#0.0", "\\\"", model1._cursor.prevItem().getType());
			assertEquals("#0.1", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.prevItem().getState());			

			model1.insertQuote();
			assertEquals("#1.0", "\"", model1._cursor.prevItem().getType());
			assertEquals("#1.1", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertTrue("#1.2", model1._cursor.prevItem().isClosed());

			model1.insertQuote();			
			model1.insertBackSlash();
			model1.insertBackSlash();
			assertEquals("#2.0", "\\\\", model1._cursor.prevItem().getType());
			assertEquals("#2.1", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.prevItem().getState());			

			model1.insertBackSlash();
			assertEquals("#3.0", "\\", model1._cursor.prevItem().getType());
			assertEquals("#3.1", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.prevItem().getState());			

			model1.insertQuote();
			assertEquals("#4.0", "\\\"", model1._cursor.prevItem().getType());
			assertEquals("#4.1", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.prevItem().getState());
		}

	public void testInsideQuoteNext()
		{
			model1.insertQuote();
			model1.insertQuote();
			model1.move(-1);
			model1.insertBackSlash();

			assertEquals("#0.0", "\\\"", model1._cursor.current().getType());
			assertEquals("#0.1", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.current().getState());
			assertEquals("#0.2", 1, model1._offset);

			model1.move(1);			
			model1.insertQuote();

			assertEquals("#1.0", "\"", model1._cursor.prevItem().getType());
			assertEquals("#1.1", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertTrue("#1.2", model1._cursor.prevItem().isClosed());

			model1.insertQuote();			
			model1.insertBackSlash();
			model1.move(-1);
			model1.insertBackSlash();
			assertEquals("#2.0", "\\\\", model1._cursor.current().getType());
			assertEquals("#2.1", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.current().getState());			
			assertEquals("#2.2", 1, model1._offset);

			model1.move(-2);
			model1.insertOpenSquiggly();
			assertEquals("#3.0", "{", model1._cursor.prevItem().getType());
			assertEquals("#3.1", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());

			model1.move(3);
			model1.insertQuote();
			assertEquals("#4.0", "\"", model1._cursor.prevItem().getType());
			assertEquals("#4.1", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertTrue("#4.2", model1._cursor.prevItem().isClosed());

			model1.move(-1);
			model1.insertBackSlash();
			assertEquals("#5.0", "\\\"", model1._cursor.current().getType());
			assertEquals("#5.1", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.current().getState());			
			assertEquals("#5.2", 1, model1._offset);			
		}

	public void testBackSlashBeforeDoubleEscape()
		{
			model1.insertBackSlash();
			model1.insertBackSlash();
			model1.move(-2);
			model1.insertBackSlash();
			assertEquals("#0.0", "\\\\", model1._cursor.current().getType());
			assertEquals("#0.1", 1, model1._offset);
			assertEquals("#0.2", "\\", model1._cursor.nextItem().getType());

			model2.insertBackSlash();
			model2.insertQuote();
			model2.move(-2);
			model2.insertBackSlash();
			assertEquals("#1.0", "\\\\", model2._cursor.current().getType());
			assertEquals("#1.1", 1, model2._offset);
			assertEquals("#1.2", "\"", model2._cursor.nextItem().getType());
			
		}

	public void testInsertBetweenDoubleEscape()
		{
			model1.insertBackSlash();
			model1.insertBackSlash();
			model1.move(-1);
			model1.insertBackSlash();
			assertEquals("#0.0", "\\\\", model1._cursor.prevItem().getType());
			assertEquals("#0.1", "\\", model1._cursor.current().getType());

			model2.insertBackSlash();
			model2.insertQuote();
			model2.move(-1);
			model2.insertBackSlash();
			assertEquals("#1.0", "\\\\", model2._cursor.prevItem().getType());
			assertEquals("#1.1", "\"", model2._cursor.current().getType());

			model0.insertBackSlash();
			model0.insertBackSlash();
			model0.move(-1);
			model0.insertClosedParen();
			model0.move(-1);
			assertEquals("#2.0", "\\", model0._cursor.prevItem().getType());
			assertEquals("#2.1", ")", model0._cursor.current().getType());
			assertEquals("#2.2", "\\", model0._cursor.nextItem().getType());

			model0.move(2);
			model0.delete(-3);
			model0.insertBackSlash();
			model0.insertQuote();
			model0.move(-1);
			model0.insertClosedParen();
			model0.move(-1);
			assertEquals("#3.0", "\\", model0._cursor.prevItem().getType());
			assertEquals("#3.1", ")", model0._cursor.current().getType());
			assertEquals("#3.2", "\"", model0._cursor.nextItem().getType());
		}

	public void testDeleteAndCombine()
		{
			model0.insertBackSlash();
			model0.insertGap(2);
			model0.insertQuote();
			model0.move(-1);
			assertEquals("#0.0", "\"", model0._cursor.current().getType());
			model0.delete(-2);
			System.out.println(model0.simpleString());
			assertEquals("#1.0", "\\\"", model0._cursor.current().getType());
			assertEquals("#1.1", 1, model0._offset);

			model0.delete(1);
			model0.insertGap(2);
			model0.insertBackSlash();
			model0.move(-1);
			assertEquals("#2.0", "\\", model0._cursor.current().getType());
			model0.delete(-2);
			assertEquals("#3.0", "\\\\", model0._cursor.current().getType());
			assertEquals("#3.1", 1, model0._offset);
		}

	public void testDeleteAndCombine2()
		{
			model0.insertBackSlash();
			model0.insertQuote();
			model0.move(-1);
			model0.delete(-1);
			model0.move(1);
			assertEquals("#0.0", "\"", model0._cursor.prevItem().getType());
			assertEquals("#0.1", ReducedToken.INSIDE_QUOTE,
									 model0.getStateAtCurrent());

			model1.insertBackSlash();
			model1.insertBackSlash();
			model1.delete(-1);
			assertEquals("#1.0", "\\", model1._cursor.prevItem().getType());
			model1.insertBackSlash();
			model1.move(-1);
			model1.delete(-1);
			assertEquals("#2.0", "\\", model1._cursor.current().getType());			
		}

	public void testDeleteAndCombine3()
		{
			model0.insertBackSlash();
			model0.insertBackSlash();
			model0.insertGap(3);
			model0.insertBackSlash();
			model0.move(-1);
			model0.delete(-4);
			assertEquals("#0.0", "\\\\", model0._cursor.current().getType());
			assertEquals("#0.1", 1, model0._offset);

			model1.insertBackSlash();
			model1.insertGap(3);
			model1.insertBackSlash();
			model1.insertQuote();
			model1.move(-1);
			model1.delete(-4);
			assertEquals("#1.0", "\\\"", model1._cursor.current().getType());
			assertEquals("#1.1", 1, model1._offset);
		}
}





