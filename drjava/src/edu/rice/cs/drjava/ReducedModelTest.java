/* $Id$ */

package edu.rice.cs.drjava;

import junit.framework.*;
import java.util.Vector;
import junit.extensions.*;

public class ReducedModelTest extends TestCase {
	protected ReducedModel model0;
	protected ReducedModel model1;
	protected ReducedModel model2;
	
	public ReducedModelTest(String name)
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
			model1.insertSlash();
			model1.insertSlash();
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
			model1.insertSlash();
			model1.insertStar();
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
			model1.insertSlash();
			model1.insertStar();
			model1.insertOpenSquiggly();
			model1._cursor.prev();
			// /*#{
			assertEquals("#0.0",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#0.1",ReducedToken.FREE,
									 model1._cursor.prevItem().getState());

			model1._cursor.next();
			model1.insertStar();
			model1.insertSlash();
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
			assertEquals("#1.0",1,model1._offset);
			model1.move(-2);
			// /____#*//
			assertEquals("#1.0","*",model1._cursor.current().getType());

			model1.move(-4);
			model1.insertStar();
			// /*#____*//
			assertEquals("#2.0","/*",model1._cursor.prevItem().getType());
			assertEquals("#2.1",ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("#2.2",0,model1._offset);
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
			model1.insertStar();
			model1.insertSlash();
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

			model1.insertSlash();
			model1.insertSlash();
			assertEquals("#2.0", ReducedToken.FREE,
									 model1._cursor.prevItem().getState());
			assertEquals("#2.1", ReducedToken.INSIDE_LINE_COMMENT,
						 model1.getStateAtCurrent());
			// {//#
			model1._cursor.prev();
			model1._cursor.prev();
			model1.insertSlash();
			model1.insertSlash();
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


  public void testMove0StaysPut()
  {
    model0.insertSlash();
    assertEquals("#1", 1, model0.absOffset());
    model0.move(0);
    assertEquals("#2", 1, model0.absOffset());
    model0.insertSlash();
    assertEquals("#3", 2, model0.absOffset());
    model0.move(0);
    assertEquals("#4", 2, model0.absOffset());
    model0.move(-1);
    assertEquals("#5", 1, model0.absOffset());
    model0.move(0);
    assertEquals("#6", 1, model0.absOffset());
  }
    
	
	/** tests the function to test if something is inside comments */
	public void testInsideComment()
		{
			assertEquals("#0.0", ReducedToken.FREE, model0.getStateAtCurrent());

			model0.insertSlash();
			model0.insertStar();
			assertEquals("#0.1", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model0.getStateAtCurrent());

			model1.insertSlash();
			model1.insertSlash();
			assertEquals("#0.2", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getStateAtCurrent());

			model1.insertOpenParen();
			assertEquals("#0.3", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getStateAtCurrent());

			model1.insertNewline();
			assertEquals("#0.4", ReducedToken.FREE,
									 model1.getStateAtCurrent());

			model0.insertStar();
			model0.insertSlash();
			assertEquals("#0.4", ReducedToken.FREE,
									 model0.getStateAtCurrent());

		}

	/** tests the function to test if something is inside quotes */
	
	public void testInsideString()
		{
			assertEquals("#0.0", ReducedToken.FREE,
									 model0.getStateAtCurrent());
			model0.insertQuote();
			assertEquals("#0.1", ReducedToken.INSIDE_QUOTE,
									 model0.getStateAtCurrent());
			model1.insertQuote();

			assertEquals("#0.2", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());

			model1.insertOpenParen();
			assertEquals("#0.3", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());

			model1.insertQuote();
			assertEquals("#0.4", ReducedToken.FREE,
									 model1.getStateAtCurrent());
		}

	/** tests inserting braces */
	public void testInsertBraces()
		{
			assertEquals("#0.0", 0, model0.absOffset());
			model0.insertSlash();
      // /#
      assertEquals("#1.0", ReducedToken.FREE,
									 model0.getStateAtCurrent());
      
			model0.insertStar();
      // /*#
      assertEquals("#2.0", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model0.getStateAtCurrent());

			assertEquals("#2.1", 2, model0.absOffset());
			model0.move(-1);
      // /#*
			assertEquals("#3.0", 1, model0.absOffset());

      model0.insertOpenParen();
      // /(#*
      assertEquals("#4.0", ReducedToken.FREE,
									 model0.getStateAtCurrent());      

      model0.move(-1);
      // /#(*
      model0.delete(1);
      // /#*
			model0.move(1);
      // /*#
			assertEquals("#5.0", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model0.getStateAtCurrent());
			assertEquals("#5.1" + model0.simpleString(), 2, model0.absOffset());

			model0.insertStar();
      // /**#
			assertEquals("#6.0",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model0.getStateAtCurrent());
			
			model0.insertSlash();
      // /**/#
			assertEquals("#7.0", 4, model0.absOffset());
			assertEquals("#7.1", ReducedToken.FREE,
									 model0.getStateAtCurrent());

			model0.move(-2);
      // /*#*/
			assertEquals("#8.0", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model0.getStateAtCurrent());
			assertEquals("#8.1", 2, model0.absOffset());

			model0.insertOpenParen();
			assertEquals("#9.0", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model0.getStateAtCurrent());
      // /*(#*/
      model0.move(1);
      // /*(*#/
			assertEquals("#10.0", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model0.getStateAtCurrent());
									 
      model0.move(-2);
      // /*#(*/
			assertEquals("#11.0", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model0.getStateAtCurrent());

			model0.move(1);
      // /*(#*/

      // /*(#*/
			assertEquals("#12.0", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model0.getStateAtCurrent());      
			assertEquals("#12.1", 3, model0.absOffset());

			model0.insertGap(4);
      // /*(____#*/
			model0.move(-2);
      // /*(__#__*/
			assertEquals("#13.0", 5, model0.absOffset());

			model0.insertClosedParen();
      // /*(__)#__*/
			assertEquals("#14.0", 6, model0.absOffset());

      // move to the closed paren
      model0.move(-1);
      // /*(__#)__*/
			assertEquals("#12.0", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model0.getStateAtCurrent());      
		}


  /** tests inserting gaps */
	
	public void testInsertGap2()
		{
			model0.insertSlash();
			model0.insertStar();
			model0.insertGap(5);
			assertEquals("#0.0",7, model0.absOffset());
			model0.insertOpenParen();
			model0.move(-1);
			model0.insertGap(3);
			assertEquals("#1.0", 10, model0.absOffset());
		}

	/** tests the cursor movement function */
	
	public void testMove()
		{
			model0.insertOpenParen();
			model0.insertGap(5);
			model0.insertClosedParen();
			model0.insertNewline();
			model0.insertGap(2);
			model0.insertOpenSquiggly();
			model0.insertClosedSquiggly();
			try {
				model0.move(-30);
				assertTrue("#0.0", false);
			}
			catch (Exception e) {
			}
			try {
				model0.move(1);
				assertTrue("#0.1", false);
			}
			catch (Exception e) {
			}

			assertEquals("#0.2", 12, model0.absOffset());	
			model0.move(-2);
			assertEquals("#0.3", 10, model0.absOffset());
			model0.move(-8);
			assertEquals("#0.4", 2, model0.absOffset());
			model0.move(3);
			assertEquals("#0.5", 5, model0.absOffset());
			model0.move(4);
			assertEquals("#0.6", 9, model0.absOffset());
		}


	/** sets up example reduction for the following tests */
	
	protected ReducedModel setUpExample()
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
			return model;
		}

	/** tests previousBrace() */
	
	public void testPreviousBrace()
		{
			// #
			assertEquals("#1.0", -1, model0.previousBrace());
			model0 = setUpExample();
			// {
			// ___
			// (__)
			// ___//___
			// "_{_"/*_(_)_*/
			// }#
			assertEquals("#2.0", 1, model0.previousBrace());
			model0.move(-5);
			assertEquals("#3.0", 6, model0.previousBrace());
			model0.move(-7);
			assertEquals("#4.0", 4, model0.previousBrace());
			model0.move(-14);
			assertEquals("#5.0", 1, model0.previousBrace());
			model0.move(-1);
			assertEquals("#6.0", 3, model0.previousBrace());
			model0.move(6);
			assertEquals("#7.0", 6, model0.previousBrace());
		}

	/** tests nextBrace() */
	
	public void testNextBrace()
		{
			assertEquals("#0.0", -1, model0.nextBrace());
			model0 = setUpExample();
			assertEquals("#1.0", -1, model0.nextBrace());
			model0.move(-9);
			assertEquals("#2.0",5, model0.nextBrace());
			model0.move(-6);
			assertEquals("#3.0", 3, model0.nextBrace());
			model0.move(-6);
			assertEquals("#4.0", 5, model0.nextBrace());
			model0.move(-1);
			assertEquals("#5.0", 6, model0.nextBrace());			
		}

	/** tests forward balancer, e.g., '(' balances with ')' */
	
	public void testBalanceForward()
		{
			assertEquals("#0.0", -1, model0.balanceForward());
			model0 = setUpExample();
			assertEquals("#1.0",-1, model0.balanceForward());
			model0.move(-1);
			assertEquals("#2.0",-1,model0.balanceForward());
			model0.move(-35);
			assertEquals("#3.0",36, model0.balanceForward());
			model0.move(1);
			assertEquals("#4.0",-1, model0.balanceForward());
			model0.move(5);
			assertEquals("#5.0",4, model0.balanceForward());
			model0.move(27);
			assertEquals("#6.0",-1, model0.balanceForward());
			model0.move(-20);
			assertEquals(-1, model0.balanceForward());

			model1.insertOpenParen();
			model1.move(-1);
			assertEquals("#7.0", -1, model1.balanceForward());
			model1.move(1);
			model1.insertClosedSquiggly();
			model1.move(-1);
			assertEquals("#8.0", -1, model1.balanceForward());			
		}

	/** tests backwards balancer, e.g., ')' balances with '(' */
	
	public void testBalanceBackward()
		{
			assertEquals("#0.0", -1, model0.balanceBackward());
			model0 = setUpExample();
			assertEquals("#1.0", 36, model0.balanceBackward());
			model0.move(-2);
			assertEquals("#2.0", 9, model0.balanceBackward());
			model0.move(-14);
			assertEquals("#3.0", -1, model0.balanceBackward());
			model0.move(-10);
			assertEquals("#4.0", 4, model0.balanceBackward());
			model0.move(-10);
			assertEquals("#5.0", -1, model0.balanceBackward());			

			model1.insertClosedParen();
			assertEquals("#6.0", -1, model1.balanceBackward());
			model1.move(-1);
			model1.insertOpenSquiggly();
			model1.move(1);
			assertEquals("#7.0", -1, model1.balanceBackward());
		}

	public void testBlockCommentChain()
		{
			model0.insertSlash();
			model0.insertStar();
			model0.insertGap(2);

			model0.insertStar();
			model0.insertSlash();
			model0.insertStar();
			model0.insertGap(2);

			model0.insertStar();
			model0.insertSlash();
			model0.insertStar();
			model0.insertGap(2);

			model0.insertStar();
			model0.insertSlash();
			model0.insertStar();
			model0.insertQuote();
			// /*__*/*__*/*__*/*"#

			assertEquals("#0.0", ReducedToken.INSIDE_QUOTE,
									 model0.getStateAtCurrent());
			assertEquals("#0.1", "\"",
									 model0._cursor.prevItem().getType());
			assertEquals("#0.2", ReducedToken.FREE,
									 model0._cursor.prevItem().getState());

			model0.move(-18);
			model0.delete(1);
			// #*__*/*__*/*__*/*"

			assertEquals("#1.0", ReducedToken.FREE,
									 model0.getStateAtCurrent());
			assertEquals("#1.1", "*",
									 model0._cursor.current().getType());
			assertEquals("#1.2", ReducedToken.FREE,
									 model0._cursor.current().getState());

			model0.move(4);
			// *__*#/*__*/*__*/*"

			assertEquals("#2.0", ReducedToken.FREE,
									 model0.getStateAtCurrent());
			assertEquals("#2.1", "/*",
									 model0._cursor.current().getType());
			assertEquals("#2.2", ReducedToken.FREE,
									 model0._cursor.current().getState());
			assertEquals("#2.3", "*",
									 model0._cursor.prevItem().getType());
			assertEquals("#2.4", ReducedToken.FREE,
									 model0._cursor.prevItem().getState());
			assertTrue("#2.5", model0._cursor.nextItem().isGap());
			assertEquals("#2.6", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model0._cursor.nextItem().getState());
			
			model0.move(6);
			// *__*/*__*/#*__*/*"

			assertEquals("#3.0", ReducedToken.FREE,
									 model0.getStateAtCurrent());
			assertEquals("#3.1", "*",
									 model0._cursor.current().getType());
			assertEquals("#3.2", ReducedToken.FREE,
									 model0._cursor.current().getState());
			assertEquals("#3.3", "*/",
									 model0._cursor.prevItem().getType());
			assertEquals("#3.4", ReducedToken.FREE,
									 model0._cursor.prevItem().getState());
			assertTrue("#3.5", model0._cursor.nextItem().isGap());
			assertEquals("#3.6", ReducedToken.FREE,
									 model0._cursor.nextItem().getState());

			model0.move(6);
			// *__*/*__*/*__*/*#"

			assertEquals("#4.0", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model0.getStateAtCurrent());
			assertEquals("#4.1", "\"",
									 model0._cursor.current().getType());
			assertEquals("#4.2", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model0._cursor.current().getState());
			assertEquals("#4.3", "/*",
									 model0._cursor.prevItem().getType());
			assertEquals("#4.4", ReducedToken.FREE,
									 model0._cursor.prevItem().getState());
			assertTrue("#4.5", model0._cursor.atLastItem());

			model0.move(-16);
			model0.insertQuote();
			// "#*__*/*__*/*__*/*"
			
			assertEquals("#4.0", ReducedToken.INSIDE_QUOTE,
									 model0.getStateAtCurrent());
			assertEquals("#4.1", "*",
									 model0._cursor.current().getType());
			assertEquals("#4.2", ReducedToken.INSIDE_QUOTE,
									 model0._cursor.current().getState());
			assertEquals("#4.3", "\"",
									 model0._cursor.prevItem().getType());
			assertEquals("#4.4", ReducedToken.FREE,
									 model0._cursor.prevItem().getState());

			model0.move(-1);

			// go to each of the */* and note how they are now broken
			for(int i = 5; i <= 7; i++) {
				model0.move(5);
				assertEquals("#" + i + ".0", ReducedToken.INSIDE_QUOTE,
										 model0.getStateAtCurrent());
				assertEquals("#" + i + ".1", "/",
										 model0._cursor.current().getType());
				assertEquals("#" + i + ".2", ReducedToken.INSIDE_QUOTE,
										 model0._cursor.current().getState());
				assertEquals("#" + i + ".3", "*",
										 model0._cursor.prevItem().getType());
				assertEquals("#" + i + ".4", ReducedToken.INSIDE_QUOTE,
										 model0._cursor.prevItem().getState());
				assertEquals("#" + i + ".6", "*",
										 model0._cursor.nextItem().getType());
				assertEquals("#" + i + ".6", ReducedToken.INSIDE_QUOTE,
									 model0._cursor.nextItem().getState());
			}

      // "*__*/*__*/*__*#/*"
			model0.move(-14);
			model0.delete(-1);
			model0.insertSlash();
			// /#*__*/*__*/*__*/*"

			assertEquals("#8.0", ReducedToken.FREE,
									 model0.getStateAtCurrent());
			assertEquals("#8.1", "/*",
									 model0._cursor.current().getType());
			assertEquals("#8.2", ReducedToken.FREE,
									 model0._cursor.current().getState());
			assertEquals("#8.3", 1, model0._offset);
			assertTrue("#8.4", model0._cursor.nextItem().isGap());
			assertEquals("#8.5", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model0._cursor.nextItem().getState());

			model0.move(5);
			// /*__*/#*__*/*__*/*"
			assertEquals("#9.0", ReducedToken.FREE,
									 model0.getStateAtCurrent());
			assertEquals("#9.1", "*",
									 model0._cursor.current().getType());
			assertEquals("#9.2", ReducedToken.FREE,
									 model0._cursor.current().getState());			
			assertEquals("#9.3", "*/",
									 model0._cursor.prevItem().getType());
			assertEquals("#9.4", ReducedToken.FREE,
									 model0._cursor.prevItem().getState());

			model0.move(5);
			// /*__*/*__*/#*__*/*"
			assertEquals("#10.0", ReducedToken.FREE,
									 model0.getStateAtCurrent());
			assertEquals("#10.1", "/*",
									 model0._cursor.current().getType());
			assertEquals("#10.2", 1, model0._offset);
			assertEquals("#10.3", ReducedToken.FREE,
									 model0._cursor.current().getState());			
			assertEquals("#10.4", "*",
									 model0._cursor.prevItem().getType());
			assertEquals("#10.5", ReducedToken.FREE,
									 model0._cursor.prevItem().getState());
			assertTrue("#10.6", model0._cursor.nextItem().isGap());
			assertEquals("#10.7", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model0._cursor.nextItem().getState());

			model0.move(5);
			// /*__*/*__*/*__*/#*"
			assertEquals("#11.0", ReducedToken.FREE,
									 model0.getStateAtCurrent());
			assertEquals("#11.1", "*",
									 model0._cursor.current().getType());
			assertEquals("#11.2", ReducedToken.FREE,
									 model0._cursor.current().getState());			
			assertEquals("#11.3", "*/",
									 model0._cursor.prevItem().getType());
			assertEquals("#11.4", ReducedToken.FREE,
									 model0._cursor.prevItem().getState());
			assertEquals("#11.5", "\"",
									 model0._cursor.nextItem().getType());
			assertEquals("#11.6", ReducedToken.FREE,
									 model0._cursor.nextItem().getState());

			model0.move(2);
			// /*__*/*__*/*__*/*"#			
			assertEquals("#12.0", ReducedToken.INSIDE_QUOTE,
									 model0.getStateAtCurrent());
			assertEquals("#12.1", "\"",
									 model0._cursor.prevItem().getType());
			assertEquals("#12.2", ReducedToken.FREE,
									 model0._cursor.prevItem().getState());


		}

}

