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

	public void testChainEffect()
		{
			model0.insertQuote();
			model0.insertBackSlash();
			model0.insertQuote();
			model0.insertQuote();
			model0.insertQuote();
			model0.insertBackSlash();
			model0.insertQuote();
			model0.insertQuote();
			model0.insertQuote();
			model0.insertBackSlash();
			model0.insertQuote();
			model0.insertQuote();
			// "\"""\"""\""#
			assertEquals("#0.0", "\"", model0._cursor.prevItem().getType());
			assertTrue("#0.1", model0._cursor.prevItem().isClosed());

			model0.move(-3);
			// "\"""\"""#\""
			assertEquals("#1.0", "\\\"", model0._cursor.current().getType());
			assertEquals("#1.1", ReducedToken.INSIDE_QUOTE,
									 model0._cursor.current().getState());
			assertEquals("#1.2", "\"", model0._cursor.prevItem().getType());
			assertEquals("#1.3", ReducedToken.FREE,
									 model0._cursor.prevItem().getState());
			assertTrue("#1.4", model0._cursor.prevItem().isOpen());

			model0.insertBackSlash();
			// "\"""\"""\#\""
			assertEquals("#2.0", "\\\\", model0._cursor.current().getType());
			assertEquals("#2.1", ReducedToken.INSIDE_QUOTE,
									 model0._cursor.current().getState());
			assertEquals("#2.2", 1, model0._offset);
			assertEquals("#2.3", "\"", model0._cursor.prevItem().getType());
			assertEquals("#2.4", ReducedToken.FREE,
									 model0._cursor.prevItem().getState());
			assertTrue("#2.5", model0._cursor.prevItem().isOpen());
			assertEquals("#2.6", "\"", model0._cursor.nextItem().getType());
			assertEquals("#2.7", ReducedToken.FREE,
									 model0._cursor.nextItem().getState());
			assertTrue("#2.8", model0._cursor.nextItem().isClosed());

			model0.insertQuote();
			// "\"""\"""\"#\""
			assertEquals("#3.0", "\\\"", model0._cursor.current().getType());
			assertEquals("#3.1", ReducedToken.INSIDE_QUOTE,
									 model0._cursor.current().getState());
			assertEquals("#3.2", 0, model0._offset);
			assertEquals("#3.3", "\\\"", model0._cursor.prevItem().getType());
			assertEquals("#3.4", ReducedToken.INSIDE_QUOTE,
									 model0._cursor.prevItem().getState());
			assertEquals("#3.5", "\"", model0._cursor.nextItem().getType());
			assertEquals("#3.6", ReducedToken.FREE,
									 model0._cursor.nextItem().getState());
			assertTrue("#3.7", model0._cursor.nextItem().isClosed());

			model0.move(-10);
			// "#\"""\"""\"\""
			model0.delete(1);
			// "#"""\"""\"\""
			assertEquals("#4.0", "\"", model0._cursor.prevItem().getType());
			assertTrue("#4.1", model0._cursor.prevItem().isOpen());
			assertEquals("#4.2", ReducedToken.FREE,
									 model0._cursor.prevItem().getState());									 
			assertEquals("#4.3", "\"", model0._cursor.current().getType());
			assertTrue("#4.4", model0._cursor.current().isClosed());
			assertEquals("#4.5", ReducedToken.FREE,
									 model0._cursor.current().getState());

			model0.move(2);
			// """#"\"""\"\""
			assertEquals("#5.0", "\"", model0._cursor.prevItem().getType());
			assertTrue("#5.1", model0._cursor.prevItem().isOpen());
			assertEquals("#5.2", ReducedToken.FREE,
									 model0._cursor.prevItem().getState());									 
			assertEquals("#5.3", "\"", model0._cursor.current().getType());
			assertTrue("#5.4", model0._cursor.current().isClosed());
			assertEquals("#5.5", ReducedToken.FREE,
									 model0._cursor.current().getState());
			assertEquals("#5.6", "\\\"", model0._cursor.nextItem().getType());
			assertEquals("#5.7", ReducedToken.FREE,
									 model0._cursor.nextItem().getState());
			
			model0.move(4);
			// """"\""#"\"\""
			assertEquals("#6.0", "\"", model0._cursor.prevItem().getType());
			assertTrue("#6.1", model0._cursor.prevItem().isOpen());
			assertEquals("#6.2", ReducedToken.FREE,
									 model0._cursor.prevItem().getState());									 
			assertEquals("#6.3", "\"", model0._cursor.current().getType());
			assertTrue("#6.4", model0._cursor.current().isClosed());
			assertEquals("#6.5", ReducedToken.FREE,
									 model0._cursor.current().getState());
			assertEquals("#6.6", "\\\"", model0._cursor.nextItem().getType());
			assertEquals("#6.7", ReducedToken.FREE,
									 model0._cursor.nextItem().getState());

			model0.move(5);
			// """"\"""\"\"#"
			assertEquals("#6.0", "\\\"", model0._cursor.prevItem().getType());
			assertEquals("#6.1", ReducedToken.FREE,
									 model0._cursor.prevItem().getState());									 
			assertEquals("#6.2", "\"", model0._cursor.current().getType());
			assertTrue("#6.3", model0._cursor.current().isOpen());
			assertEquals("#6.4", ReducedToken.FREE,
									 model0._cursor.current().getState());
		}
}





