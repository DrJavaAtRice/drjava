/* $Id$ */

package edu.rice.cs.drjava;

import junit.framework.*;
import java.util.Vector;
import junit.extensions.*;

public class BackSlashTest extends TestCase {
	protected ReducedModelControl model0;
	protected ReducedModelControl model1;
	protected ReducedModelControl model2;
	
	public BackSlashTest(String name)
		{
			super(name);
		}
	
	protected void setUp()
		{
			model0 = new ReducedModelControl();
			model1 = new ReducedModelControl();
			model2 = new ReducedModelControl();
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
			model1.move(-2);
			assertEquals("#0.0", "\\\"", model1.currentToken().getType());
			assertEquals("#0.1", ReducedToken.INSIDE_QUOTE,
									 model1.currentToken().getState());			
			model1.move(2);
			model1.insertQuote();
			model1.move(-1);
			assertEquals("#1.0", "\"", model1.currentToken().getType());
			assertEquals("#1.1", ReducedToken.FREE,
									 model1.currentToken().getState());
			assertTrue("#1.2", model1.currentToken().isClosed());
			model1.move(1);
			model1.insertQuote();			
			model1.insertBackSlash();
			model1.insertBackSlash();
			model1.move(-2);
			assertEquals("#2.0", "\\\\", model1.currentToken().getType());
			assertEquals("#2.1", ReducedToken.INSIDE_QUOTE,
									 model1.currentToken().getState());			
			model1.move(2);
			model1.insertBackSlash();
			model1.move(-1);
			assertEquals("#3.0", "\\", model1.currentToken().getType());
			assertEquals("#3.1", ReducedToken.INSIDE_QUOTE,
									 model1.currentToken().getState());			
			model1.move(1);
			model1.insertQuote();
			model1.move(-1);
			assertEquals("#4.0", "\\\"", model1.currentToken().getType());
			assertEquals("#4.1", ReducedToken.INSIDE_QUOTE,
									 model1.currentToken().getState());
		}

	public void testInsideQuoteNext()
		{
			model1.insertQuote();
			model1.insertQuote();
			model1.move(-1);
			model1.insertBackSlash();

			assertEquals("#0.0", "\\\"", model1.currentToken().getType());
			assertEquals("#0.1", ReducedToken.INSIDE_QUOTE,
									 model1.currentToken().getState());
			assertEquals("#0.2", 1, model1.getBlockOffset());

			model1.move(1);			
			model1.insertQuote();
			model1.move(-1);
			assertEquals("#1.0", "\"", model1.currentToken().getType());
			assertEquals("#1.1", ReducedToken.FREE,
									 model1.currentToken().getState());
			assertTrue("#1.2", model1.currentToken().isClosed());
			model1.move(1);
			model1.insertQuote();			
			model1.insertBackSlash();
			model1.move(-1);
			model1.insertBackSlash();
			assertEquals("#2.0", "\\\\", model1.currentToken().getType());
			assertEquals("#2.1", ReducedToken.INSIDE_QUOTE,
									 model1.currentToken().getState());			
			assertEquals("#2.2", 6, model1.absOffset());

			model1.move(-2);
			model1.insertOpenSquiggly();
			model1.move(-1);
			assertEquals("#3.0", "{", model1.currentToken().getType());
			assertEquals("#3.1", ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(1);
			model1.move(3);
			model1.insertQuote();
			model1.move(-1);
			assertEquals("#4.0", "\"", model1.currentToken().getType());
			assertEquals("#4.1", ReducedToken.FREE,
									 model1.currentToken().getState());
			assertTrue("#4.2", model1.currentToken().isClosed());

			model1.insertBackSlash();
			assertEquals("#5.0", "\\\"", model1.currentToken().getType());
			assertEquals("#5.1", ReducedToken.INSIDE_QUOTE,
									 model1.currentToken().getState());			
			assertEquals("#5.2", 1, model1.getBlockOffset());			
		}

	public void testBackSlashBeforeDoubleEscape()
		{
			model1.insertBackSlash();
			model1.insertBackSlash();
			model1.move(-2);
			model1.insertBackSlash();
			assertEquals("#0.0", "\\\\", model1.currentToken().getType());
			assertEquals("#0.1", 2, model1.currentToken().getSize());
			model1.move(1);
			assertEquals("#0.2", "\\", model1.currentToken().getType());

			model2.insertBackSlash();
			model2.insertQuote();
			model2.move(-2);
			model2.insertBackSlash();
			assertEquals("#1.0", "\\\\", model2.currentToken().getType());
			assertEquals("#1.1", 1, model2.absOffset());
			model2.move(1);
			assertEquals("#1.2", "\"", model2.currentToken().getType());
			
		}

	public void testInsertBetweenDoubleEscape()
		{
			model1.insertBackSlash();
			model1.insertBackSlash();
			model1.move(-1);
			model1.insertBackSlash();
			model1.move(-2);
			assertEquals("#0.0", "\\\\", model1.currentToken().getType());
			model1.move(2);
			assertEquals("#0.1", "\\", model1.currentToken().getType());

			model2.insertBackSlash();
			model2.insertQuote();
			model2.move(-1);
			model2.insertBackSlash();
			model2.move(-2);
			assertEquals("#1.0", "\\\\", model2.currentToken().getType());
			model2.move(2);
			assertEquals("#1.1", "\"", model2.currentToken().getType());

			model0.insertBackSlash();
			model0.insertBackSlash();
			model0.move(-1);
			model0.insertClosedParen();
			model0.move(-2);

			assertEquals("#2.0", "\\", model0.currentToken().getType());
			model0.move(1);
			assertEquals("#2.1", ")", model0.currentToken().getType());
			model0.move(1);
			assertEquals("#2.2", "\\", model0.currentToken().getType());

			model0.move(1);
			model0.delete(-3);
			model0.insertBackSlash();
			model0.insertQuote();
			model0.move(-1);
			model0.insertClosedParen();
			model0.move(-2);

			assertEquals("#3.0", "\\", model0.currentToken().getType());
			model0.move(1);
			assertEquals("#3.1", ")", model0.currentToken().getType());
			model0.move(1);
			assertEquals("#3.2", "\"", model0.currentToken().getType());
		}

	public void testDeleteAndCombine()
		{
			model0.insertBackSlash();
			model0.insertGap(2);
			model0.insertQuote();
			model0.move(-1);
			assertEquals("#0.0", "\"", model0.currentToken().getType());
			model0.delete(-2);
			assertEquals("#1.0", "\\\"", model0.currentToken().getType());
			assertEquals("#1.1", 1, model0.absOffset());
			
			model0.delete(1);
			model0.insertGap(2);
			model0.insertBackSlash();
			model0.move(-1);
			assertEquals("#2.0", "\\", model0.currentToken().getType());
			model0.delete(-2);
			assertEquals("#3.0", "\\\\", model0.currentToken().getType());
			assertEquals("#3.1", 2, model0.currentToken().getSize());
		}

	public void testDeleteAndCombine2()
		{
			model0.insertBackSlash();
			model0.insertQuote();
			model0.move(-1);
			model0.delete(-1);

			assertEquals("#0.0", "\"", model0.currentToken().getType());
			assertEquals("#0.1", ReducedToken.FREE,
									 model0.getStateAtCurrent());
			
			model1.insertBackSlash();
			model1.insertBackSlash();
			model1.delete(-1);
			model1.move(-1);
			assertEquals("#1.0", "\\", model1.currentToken().getType());
			model1.move(1);
			model1.insertBackSlash();
			model1.move(-1);
			model1.delete(-1);
			assertEquals("#2.0", "\\", model1.currentToken().getType());			
		}

	public void testDeleteAndCombine3()
		{
			model0.insertBackSlash();
			model0.insertBackSlash();
			model0.insertGap(3);
			model0.insertBackSlash();
			model0.move(-1);
			model0.delete(-4);
			assertEquals("#0.0", "\\\\", model0.currentToken().getType());
			assertEquals("#0.1", 1, model0.absOffset());

			model1.insertBackSlash();
			model1.insertGap(3);
			model1.insertBackSlash();
			model1.insertQuote();
			model1.move(-1);
			model1.delete(-4);
			assertEquals("#1.0", "\\\"", model1.currentToken().getType());
			assertEquals("#1.1", 1, model1.absOffset());
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
			model0.move(-1);
			assertEquals("#0.0", "\"", model0.currentToken().getType());
			assertTrue("#0.1", model0.currentToken().isClosed());

			model0.move(-2);
			// "\"""\"""#\""
			assertEquals("#1.0", "\\\"", model0.currentToken().getType());
			assertEquals("#1.1", ReducedToken.INSIDE_QUOTE,
									 model0.currentToken().getState());
			model0.move(-1);
			assertEquals("#1.2", "\"", model0.currentToken().getType());
			assertEquals("#1.3", ReducedToken.FREE,
									 model0.currentToken().getState());
			assertTrue("#1.4", model0.currentToken().isOpen());
			
			model0.move(1);
			model0.insertBackSlash();
			// "\"""\"""\#\""
			assertEquals("#2.0", "\\\\", model0.currentToken().getType());
			assertEquals("#2.1", ReducedToken.INSIDE_QUOTE,
									 model0.currentToken().getState());
			assertEquals("#2.2", 10, model0.absOffset());
			model0.move(-2);
			assertEquals("#2.3", "\"", model0.currentToken().getType());
			assertEquals("#2.4", ReducedToken.FREE,
									 model0.currentToken().getState());
			assertTrue("#2.5", model0.currentToken().isOpen());

			model0.move(3);
			assertEquals("#2.6", "\"", model0.currentToken().getType());
			assertEquals("#2.7", ReducedToken.FREE,
									 model0.currentToken().getState());
			assertTrue("#2.8", model0.currentToken().isClosed());

			model0.move(-1);
			model0.insertQuote();
			// "\"""\"""\"#\""
			assertEquals("#3.0", "\\\"", model0.currentToken().getType());
			assertEquals("#3.1", ReducedToken.INSIDE_QUOTE,
									 model0.currentToken().getState());
			assertEquals("#3.2", 11, model0.absOffset());
			model0.move(-2);
			assertEquals("#3.3", "\\\"", model0.currentToken().getType());
			assertEquals("#3.4", ReducedToken.INSIDE_QUOTE,
									 model0.currentToken().getState());
			model0.move(4);
			assertEquals("#3.5", "\"", model0.currentToken().getType());
			assertEquals("#3.6", ReducedToken.FREE,
									 model0.currentToken().getState());
			assertTrue("#3.7", model0.currentToken().isClosed());

			model0.move(-12);
			// "#\"""\"""\"\""
			model0.delete(1);
			// "#"""\"""\"\""
			model0.move(-1);
			// #""""\"""\"\""
			assertEquals("#4.0", "\"", model0.currentToken().getType());
			assertTrue("#4.1", model0.currentToken().isOpen());
			assertEquals("#4.2", ReducedToken.FREE,
									 model0.currentToken().getState());
			model0.move(1);
			// "#"""\"""\"\""
			assertEquals("#4.3", "\"", model0.currentToken().getType());
			assertTrue("#4.4", model0.currentToken().isClosed());
			assertEquals("#4.5", ReducedToken.FREE,
									 model0.currentToken().getState());

			model0.move(1);
			// ""#""\"""\"\""
			assertEquals("#5.0", "\"", model0.currentToken().getType());
			assertTrue("#5.1", model0.currentToken().isOpen());
			assertEquals("#5.2", ReducedToken.FREE,
									 model0.currentToken().getState());
			model0.move(1);
			// """#"\"""\"\""
			assertEquals("#5.3", "\"", model0.currentToken().getType());
			assertTrue("#5.4", model0.currentToken().isClosed());
			assertEquals("#5.5", ReducedToken.FREE,
									 model0.currentToken().getState());
			model0.move(1);
// """"#\"""\"\""
			assertEquals("#5.6", "\\\"", model0.currentToken().getType());
			assertEquals("#5.7", ReducedToken.FREE,
									 model0.currentToken().getState());
			
			model0.move(2);
			// """"\"#""\"\""
			assertEquals("#6.0", "\"", model0.currentToken().getType());
			assertTrue("#6.1", model0.currentToken().isOpen());
			assertEquals("#6.2", ReducedToken.FREE,
									 model0.currentToken().getState());
			model0.move(1);
			// """"\""#"\"\""
			assertEquals("#6.3", "\"", model0.currentToken().getType());
			assertTrue("#6.4", model0.currentToken().isClosed());
			assertEquals("#6.5", ReducedToken.FREE,
									 model0.currentToken().getState());
			model0.move(1);
			// """"\"""#\"\""
			assertEquals("#6.6", "\\\"", model0.currentToken().getType());
			assertEquals("#6.7", ReducedToken.FREE,
									 model0.currentToken().getState());

			model0.move(2);
			// """"\"""\"#\""
			assertEquals("#6.0", "\\\"", model0.currentToken().getType());
			assertEquals("#6.1", ReducedToken.FREE,
									 model0.currentToken().getState());
			model0.move(2);
			// """"\"""\"\"#"
			assertEquals("#6.2", "\"", model0.currentToken().getType());
			assertTrue("#6.3", model0.currentToken().isOpen());
			assertEquals("#6.4", ReducedToken.FREE,
									 model0.currentToken().getState());
		}
}





