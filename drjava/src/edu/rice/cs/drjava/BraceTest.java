/* $Id$ */

package edu.rice.cs.drjava;

import junit.framework.*;
import java.util.Vector;
import junit.extensions.*;

public class BraceTest extends TestCase
{

	protected Brace rparen;
	protected Brace lparen;
	
	public BraceTest(String name)
		{
			super(name);
		}
	
	public void setUp()
		{
			lparen = Brace.MakeBrace("(", ReducedToken.FREE);
			rparen = Brace.MakeBrace(")", ReducedToken.FREE);
		}

	public static Test suite()
		{
			return new TestSuite(BraceTest.class);
		}


	public void testMakeBraceSuccess()
		{
			Brace brace = Brace.MakeBrace("{", ReducedToken.FREE);
			assertEquals("{", brace.getType());
			assertEquals(1, brace.getSize());
		}
	
	public void testMakeBraceFailure()
		{
			Brace brace;
			try {
				brace = Brace.MakeBrace("k", ReducedToken.FREE);
			}
			catch (BraceException e)
				{
					assertEquals("Invalid brace type \"k\"", e.getMessage());
				}
		}

	public void testGetType()
		{
			assertEquals("(", lparen.getType());
			assertEquals(")", rparen.getType());
		}

	public void testIsShadowed()
		{
			assertTrue("#0.0", !lparen.isShadowed());
			lparen.setState(ReducedToken.INSIDE_QUOTE);
			assertEquals("#0.0.1", ReducedToken.INSIDE_QUOTE, lparen.getState());
			assertTrue("#0.1", lparen.isShadowed());
			rparen.setState(ReducedToken.INSIDE_BLOCK_COMMENT);
			assertTrue("#0.2", rparen.isShadowed());
			rparen.setState(ReducedToken.FREE);
			assertTrue("#0.3", !rparen.isShadowed());
		}

	public void testIsQuoted()
		{
			assertTrue("#0.0", !lparen.isQuoted());
			lparen.setState(ReducedToken.INSIDE_QUOTE);
			assertTrue("#0.1", lparen.isQuoted());
			lparen.setState(ReducedToken.INSIDE_BLOCK_COMMENT);
			assertTrue("#0.2", !lparen.isQuoted());
		}

	public void testIsCommented()
		{
			assertTrue("#0.0", !lparen.isCommented());
			lparen.setState(ReducedToken.INSIDE_BLOCK_COMMENT);
			assertTrue("#0.1", lparen.isCommented());
			lparen.setState(ReducedToken.INSIDE_QUOTE);
			assertTrue("#0.2", !lparen.isCommented());
		}

	public void testToString()
		{
			assertEquals(" (", lparen.toString());
			assertEquals(" )", rparen.toString());
		}

	public void testFlip()
		{
			lparen.flip();
			rparen.flip();
			assertEquals("(", rparen.getType());
			assertEquals(")", lparen.getType());
		}

	public void testOpenClosed()
		{
			assertTrue(lparen.isOpen());
			assertTrue(rparen.isClosed());
		}

	public void testIsMatch()
		{
			Brace bracket = Brace.MakeBrace("]", ReducedToken.FREE);
			Brace dummy = Brace.MakeBrace("", ReducedToken.FREE);			
			assertTrue(lparen.isMatch(rparen));
			assertTrue(!lparen.isMatch(bracket));
			assertTrue(!lparen.isMatch(dummy));
			assertTrue(!dummy.isMatch(lparen));
		}
}
