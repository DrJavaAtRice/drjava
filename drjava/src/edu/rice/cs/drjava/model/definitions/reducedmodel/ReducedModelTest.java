/* $Id$ */

package edu.rice.cs.drjava;

import junit.framework.*;
import java.util.Vector;
import junit.extensions.*;

public class ReducedModelTest extends TestCase {
	protected ReducedModel model1;
	protected ReducedModel model2;
	
	public ReducedModelTest(String name)
		{
			super(name);
		}
	
	protected void setUp()
		{
			model1 = new ReducedModel();
			model2 = new ReducedModel();
		}

	public static Test suite()
		{
			return new TestSuite(ReducedModelTest.class);
		}

	public void testInsertGap()
		{
			model1.insertGap(4);
			assertTrue("#0.0", model1._cursor.prevItem().isGap());
			assertTrue("#0.1", model1._cursor.atEnd());
			assertEquals("#0.2", 4, model1._cursor.prevItem().getSize());
			model2._cursor.next();
			model2.insertGap(5);
			assertTrue("#1.0", model2._cursor.prevItem().isGap());
			assertTrue("#1.1", model2._cursor.atEnd());
			assertEquals("#1.2", 5, model2._cursor.prevItem().getSize());
		}

	public void testInsertGapBeforeGap()
		{
			model1.insertGap(3);
			assertTrue("#0.0.0", model1._cursor.atEnd());
			model1._cursor.prev();
			model1.insertGap(3);
			assertTrue("#0.0", model1._cursor.current().isGap());
			assertEquals("#0.1", 3, model1._offset);
			assertEquals("#0.2", 6, model1._cursor.current().getSize());
			assertTrue("#0.3", model1._cursor.atFirstItem());
			assertTrue("#0.4", model1._cursor.atLastItem());
			model1._cursor.prev();
			model1._offset = 0; // now pointing to head.  move(int) will take
                     			// care of this in the future
			model1.insertGap(2);
			assertTrue("#1.0", model1._cursor.current().isGap());
			assertEquals("#1.1", 2, model1._offset);
			assertEquals("#1.2", 8, model1._cursor.current().getSize());
			assertTrue("#1.3", model1._cursor.atFirstItem());
			assertTrue("#1.4", model1._cursor.atLastItem());			
		}

	public void testInsertGapAfterGap()
		{
			model1.insertGap(3);
			assertTrue("#0.0", model1._cursor.atEnd());
			assertTrue("#0.1", model1._cursor.prevItem().isGap());
			assertEquals("#0.2", 3, model1._cursor.prevItem().getSize());	
			model1.insertGap(4);
			assertTrue("#1.0", model1._cursor.atEnd());
			assertTrue("#1.1", model1._cursor.prevItem().isGap());
			assertEquals("#1.2", 7, model1._cursor.prevItem().getSize());
		}

	public void testInsertGapInsideGap()
		{
			model1.insertGap(3);
			assertTrue("#0.0", model1._cursor.atEnd());
			assertTrue("#0.1", model1._cursor.prevItem().isGap());
			assertEquals("#0.2", 3, model1._cursor.prevItem().getSize());			
			model1._cursor.prev();
			model1.insertGap(3);
			assertTrue("#1.0", model1._cursor.atLastItem());
			assertTrue("#1.1", model1._cursor.current().isGap());
			assertEquals("#1.2", 6, model1._cursor.current().getSize());
			assertEquals("#1.3", 3, model1._offset);
			model1.insertGap(3);
			assertTrue("#1.0", model1._cursor.atLastItem());
			assertTrue("#1.1", model1._cursor.current().isGap());
			assertEquals("#1.2", 9, model1._cursor.current().getSize());
			assertEquals("#1.3", 6, model1._offset);			
		}

	public void testInsertBraceAtStartAndEnd()
		{
			model1.insertOpenParen();
			assertTrue("#0.0", model1._cursor.atEnd());
			assertEquals("#0.1","(", model1._cursor.prevItem().getType());
			assertEquals("#0.2", 1, model1._cursor.prevItem().getSize());

			model2._cursor.next();
			model2.insertClosedParen();
			assertTrue("#1.0", model2._cursor.atEnd());
			assertEquals("#1.1",")", model2._cursor.prevItem().getType());
			assertEquals("#1.2", 1, model2._cursor.prevItem().getSize());
		}

	public void testInsertBraceInsideGap()
		{
			model1.insertGap(4);
			model1._cursor.prev();
			model1.insertGap(3);
			assertEquals("#0.0", 3, model1._offset);
			assertEquals("#0.1", 7, model1._cursor.current().getSize());
			model1.insertOpenSquiggly();
			assertEquals("#1.0", 0, model1._offset);
			assertEquals("#1.1", 4, model1._cursor.current().getSize());
			assertTrue("#1.2", model1._cursor.current().isGap());
			model1._cursor.prev();
			assertEquals("#2.0", 1, model1._cursor.current().getSize());
			assertEquals("#2.1", "{", model1._cursor.current().getType());
			model1._cursor.prev();
			assertEquals("#3.0", 0, model1._offset);
			assertEquals("#3.1", 3, model1._cursor.current().getSize());
			assertTrue("#3.2", model1._cursor.current().isGap());
		}

	public void testInsertBrace()
		{
			model1.insertOpenSquiggly();
			assertTrue("#0.0", model1._cursor.atEnd());
			model1._cursor.prev();
			assertEquals("#1.0", 1, model1._cursor.current().getSize());
			assertEquals("#1.1", "{", model1._cursor.current().getType());
			model1.insertOpenParen();
			model1.insertOpenBracket();
			assertEquals("#2.0", 1, model1._cursor.current().getSize());
			assertEquals("#2.1", "{", model1._cursor.current().getType());
			model1._cursor.prev();
			assertEquals("#3.0", 1, model1._cursor.current().getSize());
			assertEquals("#3.1", "[", model1._cursor.current().getType());
			model1._cursor.prev();
			assertEquals("#3.0", 1, model1._cursor.current().getSize());
			assertEquals("#3.1", "(", model1._cursor.current().getType());
			
		}

	public void testInsertBraceAndBreakLineComment()
		{
			model1.insertLineComment();
			assertEquals("#0.0", 2, model1._cursor.prevItem().getSize());			
			model1._cursor.prev();
			model1._offset = 1;
			model1.insertOpenSquiggly();
			model1._offset = 0;
			assertEquals("#1.0", "/", model1._cursor.current().getType());
			assertEquals("#1.1", 1, model1._cursor.current().getSize());
			model1._cursor.prev();
			assertEquals("#2.0", "{", model1._cursor.current().getType());
			assertEquals("#2.1", 1, model1._cursor.current().getSize());
			model1._cursor.prev();
			assertEquals("#3.0", "/", model1._cursor.current().getType());
			assertEquals("#3.1", 1, model1._cursor.current().getSize());						
		}

		public void testInsertBraceAndBreakBlockCommentStart()
		{
			model1.insertBlockCommentStart();
			assertEquals("#0.0", 2, model1._cursor.prevItem().getSize());			
			model1._cursor.prev();
			model1._offset = 1;
			model1.insertOpenSquiggly();
			assertEquals("#1.0", "*", model1._cursor.current().getType());
			assertEquals("#1.1", 1, model1._cursor.current().getSize());
			model1._cursor.prev();
			assertEquals("#2.0", "{", model1._cursor.current().getType());
			assertEquals("#2.1", 1, model1._cursor.current().getSize());
			model1._cursor.prev();
			assertEquals("#3.0", "/", model1._cursor.current().getType());
			assertEquals("#3.1", 1, model1._cursor.current().getSize());						
		}
	
	public void testInsertMultipleBraces()
		{
			model1.insertBlockCommentStart();
			model1.insertOpenSquiggly();
			model1._cursor.prev();
			// /*#{
			assertEquals("#0.0",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#0.1",ReducedToken.FREE,
									 model1._cursor.prevItem().getState());

			model1._cursor.next();
			model1.insertBlockCommentEnd();
			// /*{*/#
			assertEquals("#1.0",ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			
			model1._cursor.prev();
			model1._offset = 1;
			model1.insertOpenSquiggly();
			model1._offset = 0;
      // /*{*{#/
			
		 assertEquals("#2.0",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1._cursor.prevItem().getState());
		 assertEquals("#2.1",ReducedToken.INSIDE_BLOCK_COMMENT,
									model1._cursor.prevItem().getState());
		 assertEquals("#2.2","/",model1._cursor.current().getType());
		}

	public void testCrazyCase1()
		{
			model1.insertSlash();
			model1.insertGap(4);
			model1.insertStar();
			model1.insertSlash();
			//should not form an end block comment
			model1._cursor.prev();
			assertEquals("#0.0","/",model1._cursor.current().getType());
			model1._cursor.prev();
			assertEquals("#0.1","*",model1._cursor.current().getType());
			
			// /____#*/
			model1._cursor.next();
			model1.insertSlash();
			model1._cursor.prev();
			assertEquals("#1.0","//",model1._cursor.current().getType());
			// /____*#//
			model1._cursor.prev();
			model1._cursor.prev();
			model1.insertStar();
			// /*#____*//
			assertEquals("#2.0","/*",model1._cursor.prevItem().getType());
			assertEquals("#2.1",ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			model1._cursor.next();
			assertEquals("#2.2","*/",model1._cursor.current().getType());
			assertEquals("#2.3",ReducedToken.FREE,
									 model1._cursor.current().getState());
			assertEquals("#2.4",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());
			// /*____#*//
		}
	
	public void testCrazyCase2()
		{
			model1.insertSlash();
			model1.insertGap(4);
			model1._cursor.prev();
			model1._offset =2;
			model1.insertSlash();
			model1._offset = 0;
			model1._cursor.prev();
			model1._cursor.prev();
			//System.out.println("SHOULD BE:  /#__/__  \n"+model1.simpleString());
			assertEquals("#0.0", 2, model1._cursor.current().getSize());			
			assertEquals("#0.1", "/", model1._cursor.nextItem().getType());
			assertEquals("#0.2", 1, model1._cursor.nextItem().getSize());
			assertEquals("#0.3",ReducedToken.FREE, model1.getStateAtCurrent());
			model1.insertSlash();
			//System.out.println(model1.simpleString());
			assertEquals("#1.1", "//", model1._cursor.prevItem().getType());
			assertEquals("#1.2",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#1.3",ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("1.4",ReducedToken.INSIDE_LINE_COMMENT,
									 model1._cursor.current().getState());
			assertEquals("1.5",ReducedToken.INSIDE_LINE_COMMENT,
									 model1._cursor.nextItem().getState());
			// //#__/__
			//break line comment simultaneously forming a new line comment
			model1._cursor.prev();
			model1._offset = 1;
			model1.insertSlash();
			model1._offset = 0;
			// //#/__/__
			//System.out.println(model1.simpleString());
			assertEquals("#2.0", "//", model1._cursor.prevItem().getType());
			assertEquals("#2.1", "/", model1._cursor.current().getType());
			assertEquals("#2.2",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#2.3",ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("2.4",ReducedToken.INSIDE_LINE_COMMENT,
									 model1._cursor.current().getState());
			assertEquals("2.5",ReducedToken.INSIDE_LINE_COMMENT,
									 model1._cursor.nextItem().getState());
			//break line comment forming a block comment
			model1._cursor.prev();
			model1._offset = 1;
			model1.insertStar();
			model1._offset = 0;
      // /*#//__/__
						
			assertEquals("#3.0", "/*", model1._cursor.prevItem().getType());
			assertEquals("#3.1", "/", model1._cursor.current().getType());
			assertEquals("#3.2", "/", model1._cursor.nextItem().getType());
			assertEquals("#3.3",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#3.3",ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("3.4",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1._cursor.current().getState());
			assertEquals("3.5",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1._cursor.nextItem().getState());
			//break block comment start with a star.
			model1._cursor.prev();
			model1._offset = 1;
			model1.insertStar();
			model1._offset = 0;
			
			 // /*#*//__/__			
			assertEquals("#4.0", "/*", model1._cursor.prevItem().getType());
			assertEquals("#4.1", "*/", model1._cursor.current().getType());
			assertEquals("#4.2", "/", model1._cursor.nextItem().getType());
			assertEquals("#4.3",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#4.3",ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("4.4",ReducedToken.FREE,
									 model1._cursor.current().getState());
			assertEquals("4.5",ReducedToken.FREE,
									 model1._cursor.nextItem().getState());

			model1._offset = 1;
			model1.insertGap(3);
			 // /**___#//__/__	
			assertEquals("#5.0", true, model1._cursor.prevItem().isGap());
			assertEquals("#5.1",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#5.2", "/", model1._cursor.current().getType());
			assertEquals("#5.3", "/", model1._cursor.nextItem().getType());
			assertEquals("#5.4",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1._cursor.prevItem().getState());
			assertEquals("5.5",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1._cursor.current().getState());
			assertEquals("5.6",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1._cursor.nextItem().getState());
		}

	public void testBasicBlockComment()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertStar();
			model1.insertSlash();
			model1._cursor.prev();
			assertEquals("0.0",ReducedToken.FREE,
									 model1._cursor.current().getState());
			assertEquals("0.1",ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("0.0","*/",
									 model1._cursor.current().getType());
			assertEquals("0.1","/*",
									 model1._cursor.prevItem().getType());
			model1.insertSlash();
			assertEquals("1.0",ReducedToken.FREE,
									 model1._cursor.current().getState());
			assertEquals("1.1",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1._cursor.prevItem().getState());
			assertEquals("1.2","*/",
									 model1._cursor.current().getType());
			assertEquals("1.3","/",
									 model1._cursor.prevItem().getType());
			model1.insertStar();
			assertEquals("1.0",ReducedToken.FREE,
									 model1._cursor.current().getState());
			assertEquals("1.1",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1._cursor.prevItem().getState());
			assertEquals("1.2","*/",
									 model1._cursor.current().getType());
			assertEquals("1.3","*",
									 model1._cursor.prevItem().getType());
		}
	
	public void testInsertBraceAndBreakBlockCommentEnd()
		{
			model1.insertBlockCommentEnd();
			assertEquals("#0.0", 1, model1._cursor.prevItem().getSize());			
			model1._cursor.prev();
			model1.insertOpenSquiggly();

			assertEquals("#1.0", "/", model1._cursor.current().getType());
			assertEquals("#1.1", 1, model1._cursor.current().getSize());
			model1._cursor.prev();
			assertEquals("#2.0", "{", model1._cursor.current().getType());
			assertEquals("#2.1", 1, model1._cursor.current().getSize());
			model1._cursor.prev();
			assertEquals("#3.0", "*", model1._cursor.current().getType());
			assertEquals("#3.1", 1, model1._cursor.current().getSize());						
		}

	public void testGetStateAtCurrent()
		{
			assertEquals("#0.0", ReducedToken.FREE, model1.getStateAtCurrent());
			model1._cursor.next();
			assertEquals("#0.1", ReducedToken.FREE, model1.getStateAtCurrent());

			model1.insertOpenParen();
			assertEquals("#1.0", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());

			model1.insertLineComment();
			assertEquals("#2.0", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("#2.1", ReducedToken.INSIDE_LINE_COMMENT,
						 model1.getStateAtCurrent());
			// {//#
			model1._cursor.prev();
			model1._cursor.prev();
			model1.insertLineComment();
			// //#{//
			assertEquals("#3.0", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("#3.1", ReducedToken.INSIDE_LINE_COMMENT,
						 model1.getStateAtCurrent());			
			assertEquals("#3.2", ReducedToken.INSIDE_LINE_COMMENT,
									 model1._cursor.current().getState());
			assertEquals("#3.3", ReducedToken.INSIDE_LINE_COMMENT,
									 model1._cursor.nextItem().getState());
			assertEquals("#3.4", "/", model1._cursor.nextItem().getType());

			model1._cursor.next();
			assertEquals("#4.1", ReducedToken.INSIDE_LINE_COMMENT,
									 model1._cursor.nextItem().getState());
			assertEquals("#4.2", "/", model1._cursor.nextItem().getType());
		}

	public void testQuotesSimple()
		{
			model1.insertQuote();
			model1.insertQuote();
			model1._cursor.prev();
			assertEquals("#0.0", "\"", model1._cursor.current().getType());
			assertEquals("#0.1", "\"", model1._cursor.prevItem().getType());
			assertEquals("#0.2", ReducedToken.FREE,
									 model1._cursor.current().getState());
			assertEquals("#0.3", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("#0.4", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());
		}

	public void testQuotesWithGap()
		{
			model1.insertQuote();
			model1.insertQuote();
			model1._cursor.prev();
			assertEquals("#0.0", "\"", model1._cursor.current().getType());
			assertEquals("#0.1", "\"", model1._cursor.prevItem().getType());
			assertEquals("#0.2", ReducedToken.FREE,
									 model1._cursor.current().getState());
			assertEquals("#0.3", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("#0.4", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());
			model1.insertGap(4);
			// "____#"
			assertEquals("#1.0", "\"", model1._cursor.current().getType());
			assertEquals("#1.1", true, model1._cursor.prevItem().isGap());
			assertEquals("#1.2", ReducedToken.FREE,
									 model1._cursor.current().getState());
			assertEquals("#1.3", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.prevItem().getState());
			assertEquals("#1.4", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());
			model1._cursor.prev();
			model1._offset = 2;
			model1.insertSlash();
			model1._offset = 0;
			assertEquals("#2.0", true, model1._cursor.current().isGap());
			assertEquals("#2.1", "/", model1._cursor.prevItem().getType());
			assertEquals("#2.2", "\"", model1._cursor.nextItem().getType());
			assertEquals("#2.3", ReducedToken.FREE,
									 model1._cursor.nextItem().getState());
			assertEquals("#2.4", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.current().getState());
			assertEquals("#2.5", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.prevItem().getState());
			assertEquals("#2.6", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());
			// "__/#__"

			model1.insertQuote();
			// "__/"#__"
			assertEquals("#3.0", true, model1._cursor.current().isGap());
			assertEquals("#3.1", "\"", model1._cursor.prevItem().getType());
			assertEquals("#3.2", "\"", model1._cursor.nextItem().getType());
			assertEquals("#3.3", ReducedToken.FREE,
									 model1._cursor.nextItem().getState());
			assertEquals("#3.4", ReducedToken.FREE,
									 model1._cursor.current().getState());
			assertEquals("#3.5", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("#3.6", ReducedToken.FREE,
									 model1.getStateAtCurrent());
			model1._cursor.prev();
			model1._cursor.prev();
			// "__#/"__"
			
			assertEquals("#4.0", "/", model1._cursor.current().getType());
			assertEquals("#4.1", true, model1._cursor.prevItem().isGap());
			assertEquals("#4.2", "\"", model1._cursor.nextItem().getType());
			assertEquals("#4.3", ReducedToken.FREE,
									 model1._cursor.nextItem().getState());
			assertEquals("#4.4", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.current().getState());
			assertEquals("#4.5", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.prevItem().getState());
			assertEquals("#4.6", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());
			model1.insertNewline();
			// "__\n#/"__"
			assertEquals("#4.5", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("#4.6", ReducedToken.FREE,
									 model1.getStateAtCurrent());
			assertEquals("#4.3", ReducedToken.FREE,
									 model1._cursor.nextItem().getState());
			assertEquals("#4.4", ReducedToken.FREE,
									 model1._cursor.current().getState());
			model1._cursor.prev();
			model1._cursor.prev();
			assertEquals("#4.5", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.current().getState());
			assertEquals("#4.6",true,model1._cursor.current().isGap());
		}

	public void testQuoteBreaksComment()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertStar();
			model1.insertSlash();
			model1._cursor.prev();
			assertEquals("#0.0", "/*", model1._cursor.prevItem().getType());
			assertEquals("#0.1", "*/", model1._cursor.current().getType());

			model1.insertQuote();			
			model1._cursor.prev();
			// /*#"*/
			assertEquals("#1.1", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());			
			assertEquals("#1.2", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1._cursor.current().getState());
			assertEquals("#1.1", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());			
			assertEquals("#1.2", ReducedToken.FREE,
									 model1._cursor.nextItem().getState());
			model1._cursor.prev();
			// #/*"*/
			model1.insertQuote();
			assertEquals("#2.0", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());
			assertEquals("#2.1", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.current().getState());
			assertEquals("#2.2", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("#2.3", "/", model1._cursor.current().getType());
			assertEquals("#2.4", "*", model1._cursor.nextItem().getType());
			model1._cursor.next();
			// "/#*"*/
			assertEquals("#3.0", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());
			assertEquals("#3.1", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.current().getState());
			assertEquals("#3.2", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.prevItem().getState());
			assertEquals("#3.3", "*", model1._cursor.current().getType());
			assertEquals("#3.4", "\"", model1._cursor.nextItem().getType());

			model1._cursor.next();
			// "/*#"*/
			assertEquals("#4.0", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());
			assertEquals("#4.1", ReducedToken.FREE,
									 model1._cursor.current().getState());
			assertEquals("#4.2", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.prevItem().getState());
			assertEquals("#4.3", "\"", model1._cursor.current().getType());
			assertEquals("#4.4", "*", model1._cursor.nextItem().getType());
			assertEquals("#4.5", ReducedToken.FREE,
									 model1._cursor.nextItem().getState());
			assertTrue("#4.6", model1._cursor.current().isClosed());
			model1._cursor.next();
			// "/*"#*/
			assertEquals("#5.0", ReducedToken.FREE,
									 model1.getStateAtCurrent());
			assertEquals("#5.1", ReducedToken.FREE,
									 model1._cursor.current().getState());
			assertEquals("#5.2", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("#5.3", "*", model1._cursor.current().getType());
			assertEquals("#5.4", "/", model1._cursor.nextItem().getType());
			assertEquals("#5.5", ReducedToken.FREE,
									 model1._cursor.nextItem().getState());

		}

	public void testQuoteBreakComment2()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertStar();
			model1.insertSlash();
			model1._cursor.prev();
			assertEquals("#0.0", "/*", model1._cursor.prevItem().getType());
			assertEquals("#0.1", "*/", model1._cursor.current().getType());
			model1._cursor.prev();
			// "#/**/
			model1.insertQuote();
			assertEquals("#1.0", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("#1.1", ReducedToken.INSIDE_QUOTE,
						 model1.getStateAtCurrent());			
			assertEquals("#1.2", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.current().getState());
			assertEquals("#1.3", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.nextItem().getState());
			assertEquals("#1.4", "*", model1._cursor.nextItem().getType());
			assertEquals("#1.4", "\"", model1._cursor.prevItem().getType());
			assertEquals("#1.4", "/", model1._cursor.current().getType());			
		}

	public void testInsertNewlineEndLineComment()
		{
			model1.insertSlash();
			model1.insertSlash();
			model1.insertGap(5);
			model1._cursor.prev();
			model1._offset = 3;
			model1.insertNewline();
			// //___\n#__
			assertEquals("#0.0", ReducedToken.FREE, model1.getStateAtCurrent());
			assertTrue("#0.1", model1._cursor.current().isGap());
			assertEquals("#0.2", "\n", model1._cursor.prevItem().getType());
			assertEquals("#0.3", 2, model1._cursor.current().getSize());
			assertEquals("#0.4", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("#0.5", ReducedToken.FREE,
									 model1._cursor.current().getState());
			
			model1._cursor.prev();
			// //___#\n__
			assertEquals("#1.0", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getStateAtCurrent());
			assertTrue("#1.1", model1._cursor.prevItem().isGap());
			assertEquals("#1.2", "\n", model1._cursor.current().getType());
			assertEquals("#1.3", 3, model1._cursor.prevItem().getSize());
			assertEquals("#1.4", ReducedToken.INSIDE_LINE_COMMENT,
									 model1._cursor.prevItem().getState());
			assertEquals("#1.5", ReducedToken.FREE,
									 model1._cursor.current().getState());
			
		}

	public void testInsertNewlineEndQuote()
		{
			model1.insertQuote();
			model1.insertGap(5);
			model1._cursor.prev();
			model1._offset = 3;
			model1.insertNewline();
			// "___\n#__
			assertEquals("#0.0", ReducedToken.FREE, model1.getStateAtCurrent());
			assertTrue("#0.1", model1._cursor.current().isGap());
			assertEquals("#0.2", "\n", model1._cursor.prevItem().getType());
			assertEquals("#0.3", 2, model1._cursor.current().getSize());
			assertEquals("#0.4", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("#0.5", ReducedToken.FREE,
									 model1._cursor.current().getState());
			
			model1._cursor.prev();
			// "___#\n__
			assertEquals("#1.0", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());
			assertTrue("#1.1", model1._cursor.prevItem().isGap());
			assertEquals("#1.2", "\n", model1._cursor.current().getType());
			assertEquals("#1.3", 3, model1._cursor.prevItem().getSize());
			assertEquals("#1.4", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.prevItem().getState());
			assertEquals("#1.5", ReducedToken.FREE,
									 model1._cursor.current().getState());
		}

	public void testInsertNewlineChainReaction()
		{
			model1.insertSlash();
			model1.insertSlash();
			model1.insertSlash();
			model1.insertStar();
			// ///*#
			model1._cursor.prev();
			// ///#*
			assertEquals("#0.0", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#0.1", "*", model1._cursor.current().getType());
			assertEquals("#0.2", "/", model1._cursor.prevItem().getType());
			assertEquals("#0.3", ReducedToken.INSIDE_LINE_COMMENT,
									 model1._cursor.prevItem().getState());
			assertEquals("#0.4", ReducedToken.INSIDE_LINE_COMMENT,
									 model1._cursor.current().getState());
			
			model1._cursor.next();
			model1.insertNewline();
			model1.insertQuote();
			model1.insertStar();
			model1.insertSlash();
			model1._cursor.prev();
			// ///*
			// "*#/

			
			assertEquals("#1.0", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());
			assertEquals("#1.1", "/", model1._cursor.current().getType());
			assertEquals("#1.2", "*", model1._cursor.prevItem().getType());
			assertEquals("#1.3", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.prevItem().getState());
			assertEquals("#1.4", ReducedToken.INSIDE_QUOTE,
									 model1._cursor.current().getState());

			for(int i = 0; i < 5; i++)
				model1._cursor.prev();

			assertEquals("#2.1", "/", model1._cursor.current().getType());
			assertEquals("#2.2", "//", model1._cursor.prevItem().getType());

			model1.insertNewline();
			// //
			// #/*
			// "*/
			assertEquals("#3.0", ReducedToken.FREE,
									 model1.getStateAtCurrent());
			assertEquals("#3.1", "/*", model1._cursor.current().getType());
			assertEquals("#3.2", "\n", model1._cursor.prevItem().getType());
			assertEquals("#3.3", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("#3.4", ReducedToken.FREE,
									 model1._cursor.current().getState());

			model1._cursor.next();
			model1._cursor.next();

			// //
			// /*
			// #"*/
			assertEquals("#4.0", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#4.1", "\"", model1._cursor.current().getType());
			assertEquals("#4.2", "\n", model1._cursor.prevItem().getType());
			assertEquals("#4.3", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1._cursor.prevItem().getState());
			assertEquals("#4.4", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1._cursor.current().getState());

			assertEquals("#4.5", ReducedToken.FREE,
									 model1._cursor.nextItem().getState());
			assertEquals("#4.6", "*/",
									 model1._cursor.nextItem().getType());
		}

	public void testMoveWithinToken()
		{
			model1.insertGap(10);
			assertTrue("#0.0", model1._cursor.atEnd());
			assertEquals("#0.1", 0, model1._offset);


			model1.move(-5);
			
			assertTrue("#1.0", model1._cursor.current().isGap());
			assertEquals("#1.1", 5, model1._offset);

			model1.move(2);
			assertTrue("#2.0", model1._cursor.current().isGap());
			assertEquals("#2.1", 7, model1._offset);

			model1.move(-4);
			assertTrue("#3.0", model1._cursor.current().isGap());
			assertEquals("#3.1", 3, model1._offset);

			model1.move(-3);
			assertTrue("#4.0", model1._cursor.current().isGap());
			assertEquals("#4.1", 0, model1._offset);

			model1.move(10);
			assertTrue("#5.0", model1._cursor.atEnd());
			assertEquals("#5.1", 0, model1._offset);
		}

	public void testMoveOnEmpty()
		{
			model1.move(0);
			assertTrue("#0.0", model1._cursor.atStart());
			try {
				model1.move(-1);
				assertTrue("#0.1", false);
			}
			catch (Exception e) {
			}

			try {
				model1.move(1);
				assertTrue("#0.2", false);
			}
			catch (Exception e) {
			}			
		}

	public void testMoveOverSeveralTokens()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertGap(4);
			model1.insertOpenSquiggly();
			model1.insertSlash();
			model1.insertQuote();
			model1.insertGap(3);
			model1.insertNewline();
			// /*____{"___#\n			
			assertTrue("#0.0", model1._cursor.atEnd());
			assertEquals("#0.1", 0, model1._offset);

			model1.move(-12);
			// /#*____{"___\n
			assertEquals("#1.0", "/*", model1._cursor.current().getType());
			assertEquals("#1.1", 1, model1._offset);

			model1.move(11);
			// /*____{"___#\n			
			assertEquals("#2.0", "\n", model1._cursor.current().getType());
			assertEquals("#2.1", 0, model1._offset);

			model1.move(-1);
			// /*____{"__#_\n
			assertTrue("#3.0", model1._cursor.current().isGap());
			assertEquals("#3.1", 2, model1._offset);

			model1.move(-6);
			// /*___#_{"___\n			
			assertTrue("#4.0", model1._cursor.current().isGap());
			assertEquals("#4.1", 3, model1._offset);

			try {
				model1.move(-8);
				assertTrue("#5.0", false);
			}
			catch (Exception e) {
			}
			assertTrue("#5.1", model1._cursor.current().isGap());
			assertEquals("#5.2", 3, model1._offset);

			try {
				model1.move(20);
				assertTrue("#6.0", false);
			}
			catch (Exception e) {
			}
			assertTrue("#6.1", model1._cursor.current().isGap());
			assertEquals("#6.2", 3, model1._offset);
			
		}
}



