/* $Id$ */

package edu.rice.cs.drjava;

import junit.framework.*;
import java.util.Vector;
import junit.extensions.*;

public class ReducedModelTest extends TestCase {
	protected ReducedModelControl model0;
	protected ReducedModelControl model1;
	protected ReducedModelControl model2;
	
	public ReducedModelTest(String name)
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
			return new TestSuite(ReducedModelTest.class);
		}

	public void testInsertGap()
		{
			//inserts a gap. 
			model1.insertGap(4);
			model1.move(-4);
			//checks to make sure it is a gap
			assertTrue("#0.0", model1.currentToken().isGap());			
			assertEquals("#0.2", 4, model1.currentToken().getSize());
			model1.move(4);
			//inserts another gap after the afor mentioned gap
			model2.insertGap(5);
			model2.move(-5);
			//makes sure they united to form an Uber gap.
			assertTrue("#1.0", model2.currentToken().isGap());
			assertEquals("#1.2", 5, model2.currentToken().getSize());
		}

	/**
	 *Test that a gap inserted previous to a gap, unites with that gap.
	 */
	public void testInsertGapBeforeGap()
		{
			model1.insertGap(3);
			assertTrue("#0.0.0", model1.atEnd());
			model1.move(-3);
			model1.insertGap(3);
			//insert two consecutive gaps and make sure they combine.
			assertTrue("#0.0", model1.currentToken().isGap());
			assertEquals("#0.1", 3, model1.absOffset());
			assertEquals("#0.2", 6, model1.currentToken().getSize());
			model1.move(-3);
			model1.insertGap(2);
			assertTrue("#1.0", model1.currentToken().isGap());
			assertEquals("#1.1", 2, model1.absOffset());
			assertEquals("#1.2", 8, model1.currentToken().getSize());
		}

	public void testInsertGapAfterGap()
		{
			model1.insertGap(3);
			assertTrue("#0.0", model1.atEnd());
			model1.move(-3);
			assertTrue("#0.1", model1.currentToken().isGap());
			assertEquals("#0.2", 3, model1.currentToken().getSize());	
			model1.insertGap(4);
			assertTrue("#1.1", model1.currentToken().isGap());
			assertEquals("#1.2", 7, model1.currentToken().getSize());
		}

	/**Inserts one gap inside of the other*/
	public void testInsertGapInsideGap()
		{
			model1.insertGap(3);
			assertTrue("#0.0", model1.atEnd());
			model1.move(-3);
			assertTrue("#0.1", model1.currentToken().isGap());
			assertEquals("#0.2", 3, model1.currentToken().getSize());			
			model1.insertGap(3);
			assertTrue("#1.1", model1.currentToken().isGap());
			assertEquals("#1.2", 6, model1.currentToken().getSize());
			assertEquals("#1.3", 3, model1.absOffset());
			model1.insertGap(4);
			assertTrue("#1.1", model1.currentToken().isGap());
			assertEquals("#1.2", 10, model1.currentToken().getSize());
			assertEquals("#1.3", 7, model1._offset);			
		}

	public void testInsertBraceAtStartAndEnd()
		{
			model1.insertOpenParen();
			assertTrue("#0.0", model1.atEnd());
			model1.move(-1);
			assertEquals("#0.1","(", model1.currentToken().getType());
			assertEquals("#0.2", 1, model1.currentToken().getSize());
			
			model2.insertClosedParen();
			assertTrue("#1.0", model2.atEnd());
			model2.move(-1);
			assertEquals("#1.1",")", model2.currentToken().getType());
			assertEquals("#1.2", 1, model2.currentToken().getSize());
		}






	//**************
	public void testInsertBraceInsideGap()
		{
			model1.insertGap(4);
			model1.move(-4);
			model1.insertGap(3);
			assertEquals("#0.0", 3, model1.absOffset());
			assertEquals("#0.1", 7, model1.currentToken().getSize());
			model1.insertOpenSquiggly();
			assertEquals("#1.0", 4, model1.absOffset());
			assertEquals("#1.1", 4, model1.currentToken().getSize());
			assertTrue("#1.2", model1.currentToken().isGap());
			model1.move(-1);
			assertEquals("#2.0", 1, model1.currentToken().getSize());
			assertEquals("#2.1", "{", model1.currentToken().getType());
			model1.move(-3);
			assertEquals("#3.0", 0, model1.absOffset());
			assertEquals("#3.1", 3, model1.currentToken().getSize());
			assertTrue("#3.2", model1.currentToken().isGap());
		}

	public void testInsertBrace()
		{
			model1.insertOpenSquiggly();
			assertTrue("#0.0", model1.atEnd());
			model1.move(-1);
			assertEquals("#1.0", 1, model1.currentToken().getSize());
			assertEquals("#1.1", "{", model1.currentToken().getType());
			model1.insertOpenParen();
			model1.insertOpenBracket();
			assertEquals("#2.0", 1, model1.currentToken().getSize());
			assertEquals("#2.1", "{", model1.currentToken().getType());
			model1.move(-1);
 			assertEquals("#3.0", 1, model1.currentToken().getSize());
			assertEquals("#3.1", "[", model1.currentToken().getType());
			model1.move(-1);
			assertEquals("#3.0", 1, model1.currentToken().getSize());
			assertEquals("#3.1", "(", model1.currentToken().getType());
			
		}

	public void testInsertBraceAndBreakLineComment()
		{
			model1.insertSlash();
			model1.insertSlash();
			model1.move(-1);
			assertEquals("#0.0", 2, model1.currentToken().getSize());			
			//move to the middle of the // and break it with a {
			
			model1.insertOpenSquiggly();
			assertEquals("#1.0", "/", model1.currentToken().getType());
			assertEquals("#1.1", 1, model1.currentToken().getSize());
			model1.move(-1);
			assertEquals("#2.0", "{", model1.currentToken().getType());
			assertEquals("#2.1", 1, model1.currentToken().getSize());
			model1.move(-1);
			assertEquals("#3.0", "/", model1.currentToken().getType());
			assertEquals("#3.1", 1, model1.currentToken().getSize());						
		}

		public void testInsertBraceAndBreakBlockCommentStart()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.move(-2);
			assertEquals("#0.0", 2, model1.currentToken().getSize());			
			model1.move(1);
			model1.insertOpenSquiggly();
			assertEquals("#1.0", "*", model1.currentToken().getType());
			assertEquals("#1.1", 1, model1.currentToken().getSize());
			model1.move(-1);
			assertEquals("#2.0", "{", model1.currentToken().getType());
			assertEquals("#2.1", 1, model1.currentToken().getSize());
			model1.move(-1);
			assertEquals("#3.0", "/", model1.currentToken().getType());
			assertEquals("#3.1", 1, model1.currentToken().getSize());						
		}
	
//**************************

	public void testInsertMultipleBraces()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertOpenSquiggly();
			model1.move(-1);
			// /*#{
			assertEquals("#0.0",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());
			model1.move(-2);
			assertEquals("#0.1",ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(3);
			model1.insertStar();
			model1.insertSlash();
			// /*{*/#
			model1.move(-2);
			assertEquals("#1.0",ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(1);
			model1.insertOpenSquiggly();
			model1.move(0);
      // /*{*{#/
			model1.move(-1);
			assertEquals("#2.0",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.currentToken().getState());
			assertEquals("#2.1",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.currentToken().getState());
			model1.move(1);
			assertEquals("#2.2","/",model1.currentToken().getType());
		}

	public void testCrazyCase1()
		{
			model1.insertSlash();
			model1.insertGap(4);
			model1.insertStar();
			model1.insertSlash();
			//should not form an end block comment
			model1.move(-1);
			assertEquals("#0.0","/",model1.currentToken().getType());
			model1.move(-1);
			assertEquals("#0.1","*",model1.currentToken().getType());
			// /____#*/
			
			model1.move(1);
			model1.insertSlash();
			// /____*/#/
			assertEquals("#1.0",2,model1.currentToken().getSize());
			model1.move(-2);
			// /____#*//
			assertEquals("#1.0","*",model1.currentToken().getType());

			model1.move(-4);
			model1.insertStar();
			// /*#____*//
			model1.move(-2);
			assertEquals("#2.0","/*",model1.currentToken().getType());
			assertEquals("#2.1",ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(6);
			// /*____#*//
			assertEquals("#2.2","*/",model1.currentToken().getType());
			assertEquals("#2.3",ReducedToken.FREE,
									 model1.currentToken().getState());
			assertEquals("#2.4",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());
			// /*____#*//
		}

	/**Test sequences of inserts*/
	public void testCrazyCase2()
		{
			model1.insertSlash();
			model1.insertGap(4);
			model1.move(-2);
			model1.insertSlash();
			model1.move(0);
			model1.move(-3);
			//check that double slash works.
			assertEquals("#0.0", 2, model1.currentToken().getSize());			
			assertEquals("#0.3",ReducedToken.FREE, model1.getStateAtCurrent());
			model1.move(2);
			assertEquals("#0.2", 1, model1.currentToken().getSize());
			assertEquals("#0.1", "/", model1.currentToken().getType());
			model1.move(-2);
			model1.insertSlash();
			model1.move(-2);
			assertEquals("#1.1", "//", model1.currentToken().getType());
			assertEquals("#1.3",ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(2);
			// //#__/__
			assertEquals("#1.2",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("1.4",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.currentToken().getState());
			model1.move(2);
			assertEquals("1.5",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.currentToken().getState());
			model1.move(-2);

		}
	
	public void testLineCommentBreakCrazy()
		{
			model1.insertSlash();
			model1.insertSlash();
			model1.insertGap(4);
			model1.move(-2);
			model1.insertSlash();
			// //#__/__
//break line comment simultaneously forming a new line comment
			model1.move(-4);
			model1.insertSlash();
			model1.move(0);
			 // //#/__/__
			
			model1.move(-2);
			assertEquals("#2.0", "//", model1.currentToken().getType());
			assertEquals("#2.3",ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(2);
			assertEquals("#2.1", "/", model1.currentToken().getType());
			assertEquals("#2.2",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getStateAtCurrent());

			assertEquals("2.4",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.currentToken().getState());
			model1.move(1);
			assertEquals("2.5",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.currentToken().getState());
			//break line comment forming a block comment
			model1.move(-2);
			model1.insertStar();//  ///__/__ 
			model1.move(0);
      // /*#//__/__
			model1.move(-2);
			assertEquals("#3.0", "/*", model1.currentToken().getType());
			assertEquals("#3.3",ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(2);
			assertEquals("#3.1", "/", model1.currentToken().getType());
			assertEquals("#3.3",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("3.4",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.currentToken().getState());
			model1.move(1);
			assertEquals("#3.2", "/", model1.currentToken().getType());
			assertEquals("3.5",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.currentToken().getState());

		}

	public void testBreakBlockCommentWithStar()
		{
			// /*#//__/__
			model1.insertSlash();
			model1.insertStar();
			model1.insertSlash();
			model1.insertSlash();
			model1.insertGap(2);
			model1.insertSlash();
			model1.insertGap(2);
			
			//break block comment start with a star.
			model1.move(-8);
			model1.insertStar();
			
			 // /*#*//__/__			
			model1.move(-2);
			assertEquals("#4.0", "/*", model1.currentToken().getType());
			assertEquals("#4.3",ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(2);
			assertEquals("#4.1", "*/", model1.currentToken().getType());
			assertEquals("#4.3",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("4.4",ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(2);
			assertEquals("#4.2", "/", model1.currentToken().getType());						
			assertEquals("4.5",ReducedToken.FREE,
									 model1.currentToken().getState());

		}

	public void testBreakCloseBlockCommentWithStar()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertStar();
			model1.insertSlash();
			model1.insertSlash();
			model1.insertGap(2);
			model1.insertSlash();
			model1.insertGap(2);

			model1.move(-7);
			model1.insertGap(3);
			 // /**___#//__/__
			model1.move(-3);
			assertEquals("#5.0", true, model1.currentToken().isGap());
			assertEquals("#5.4",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.currentToken().getState());
			model1.move(3);
			assertEquals("#5.1",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#5.2", "/", model1.currentToken().getType());
			assertEquals("5.5",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.currentToken().getState());
			model1.move(1);
			assertEquals("#5.3", "/", model1.currentToken().getType());

			assertEquals("5.6",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.currentToken().getState());
		}

	public void testBasicBlockComment()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertStar();
			model1.insertSlash();
			model1.move(-4);
			assertEquals("0.1",ReducedToken.FREE,
									 model1.currentToken().getState());
			assertEquals("0.2","/*",
									 model1.currentToken().getType());
			model1.move(2);
			assertEquals("0.3",ReducedToken.FREE,
									 model1.currentToken().getState());
			assertEquals("0.4","*/",
									 model1.currentToken().getType());

			model1.insertSlash();
			model1.move(-1);
			assertEquals("1.1",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.currentToken().getState());
			assertEquals("1.3","/",
									 model1.currentToken().getType());
			model1.move(1);
			assertEquals("1.0",ReducedToken.FREE,
									 model1.currentToken().getState());

			assertEquals("1.2","*/",
									 model1.currentToken().getType());
		}
	
	public void testInsertBlockInsideBlockComment()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertSlash();
			model1.insertStar();
			model1.insertSlash();
			///*/*/#
			model1.move(-2);
			model1.insertStar();
			///*/*#*/
			model1.move(-1);
			assertEquals("1.1",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.currentToken().getState());
			assertEquals("1.3","*",
									 model1.currentToken().getType());
			model1.move(1);
			assertEquals("1.0",ReducedToken.FREE,
									 model1.currentToken().getState());

			assertEquals("1.2","*/",
									 model1.currentToken().getType());

		}
	
	public void testInsertBlockCommentEnd()
		{//should not form an end without a start.
			model1.insertStar();
			model1.insertSlash();
			model1.move(-1);
			assertEquals("#3.0", "/", model1.currentToken().getType());
			assertEquals("#3.1", 1, model1.currentToken().getSize());						
		}

	public void testGetStateAtCurrent()
		{
			assertEquals("#0.0", ReducedToken.FREE, model1.getStateAtCurrent());
			assertEquals("#0.1", ReducedToken.FREE, model1.getStateAtCurrent());

			model1.insertOpenParen();
			model1.move(-1);
			assertEquals("#1.0", ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(1);
			model1.insertSlash();
			model1.insertSlash();
			model1.move(-2);
			assertEquals("#2.0", ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(2);
			assertEquals("#2.1", ReducedToken.INSIDE_LINE_COMMENT,
						 model1.getStateAtCurrent());
			// {//#
			model1.move(-3);
			model1.insertSlash();
			model1.insertSlash();
			// //#{//
			model1.move(-2);
			assertEquals("#3.0", ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(2);
			assertEquals("#3.1", ReducedToken.INSIDE_LINE_COMMENT,
						 model1.getStateAtCurrent());			
			assertEquals("#3.2", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.currentToken().getState());
			model1.move(1);
			assertEquals("#3.3", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.currentToken().getState());
			assertEquals("#3.4", "/", model1.currentToken().getType());

			model1.move(1);
			assertEquals("#4.1", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.currentToken().getState());
			assertEquals("#4.2", "/", model1.currentToken().getType());
		}

	public void testQuotesSimple()
		{
			model1.insertQuote();
			model1.insertQuote();
			model1.move(-2);
			assertEquals("#0.0", "\"", model1.currentToken().getType());
			assertEquals("#0.3", ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(1);
			assertEquals("#0.1", "\"", model1.currentToken().getType());
			assertEquals("#0.2", ReducedToken.FREE,
									 model1.currentToken().getState());
			assertEquals("#0.4", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());
		}

	public void testQuotesWithGap()
		{
			model1.insertQuote();
			model1.insertQuote();
			model1.move(-2);
			assertEquals("#0.1", "\"", model1.currentToken().getType());
			assertEquals("#0.3", ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(1);			
			assertEquals("#0.0", "\"", model1.currentToken().getType());
			assertEquals("#0.2", ReducedToken.FREE,
									 model1.currentToken().getState());
			assertEquals("#0.4", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());
			
			model1.insertGap(4);
			// "____#"
			model1.move(-4);
			assertEquals("#1.1", true, model1.currentToken().isGap());
			assertEquals("#1.3", ReducedToken.INSIDE_QUOTE,
									 model1.currentToken().getState());
			model1.move(4);
			assertEquals("#1.0", "\"", model1.currentToken().getType());
			
			assertEquals("#1.2", ReducedToken.FREE,
									 model1.currentToken().getState());
			assertEquals("#1.4", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());
			model1.move(-2);
			model1.insertSlash();
			// "__/__"
			model1.move(-1);
			assertEquals("#2.1", "/", model1.currentToken().getType());
			model1.move(1);
			assertEquals("#2.0", true, model1.currentToken().isGap());
			assertEquals("#2.4", ReducedToken.INSIDE_QUOTE,
									 model1.currentToken().getState());
			assertEquals("#2.6", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());
			model1.move(2);
			assertEquals("#2.2", "\"", model1.currentToken().getType());
			assertEquals("#2.3", ReducedToken.FREE,
									 model1.currentToken().getState());					
		}

	public void testInsertQuoteToQuoteBlock()
		{
			model1.insertQuote();
			model1.insertGap(2);
			model1.insertSlash();
			model1.insertGap(2);
			model1.insertQuote();
			model1.move(-3);
			model1.insertQuote();
			// "__/"#__"
			model1.move(-1);
			assertEquals("#3.1", "\"", model1.currentToken().getType());
			assertEquals("#3.5", ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(1);
			assertEquals("#3.0", true, model1.currentToken().isGap());
			assertEquals("#3.4", ReducedToken.FREE,
									 model1.currentToken().getState());
			assertEquals("#3.6", ReducedToken.FREE,
									 model1.getStateAtCurrent());
			model1.move(2);
			assertEquals("#3.2", "\"", model1.currentToken().getType());
			assertEquals("#3.3", ReducedToken.FREE,
									 model1.currentToken().getState());
// "__/"__"

			
			model1.move(-6);
			assertEquals("#4.1", true, model1.currentToken().isGap());
			assertEquals("#4.5", ReducedToken.INSIDE_QUOTE,
									 model1.currentToken().getState());
			model1.move(2);
			assertEquals("#4.0", "/", model1.currentToken().getType());
			assertEquals("#4.4", ReducedToken.INSIDE_QUOTE,
									 model1.currentToken().getState());
			assertEquals("#4.6", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());
			model1.move(1);
			
			assertEquals("#4.2", "\"", model1.currentToken().getType());
			assertEquals("#4.3", ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(-1);
// "__/#"__"

			//break quote with newline
			model1.insertNewline();
			// "__\n#/"__"
			model1.move(-1);
			assertEquals("#5.5", ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(1);
			assertEquals("#5.4", ReducedToken.FREE,
									 model1.currentToken().getState());
			assertEquals("#5.6", ReducedToken.FREE,
									 model1.getStateAtCurrent());
			model1.move(1);
			assertEquals("#5.3", ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(1);
			assertEquals("#5.7", ReducedToken.INSIDE_QUOTE,
									 model1.currentToken().getState());
			assertEquals("#5.8",true,model1.currentToken().isGap());
		}

	public void testQuoteBreaksComment()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertStar();
			model1.insertSlash();
			model1.move(-2);
			model1.insertQuote();			
			model1.move(-1);
			// /*#"*/
			model1.move(-2);
			assertEquals("#1.1", ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(2);
			assertEquals("#1.1", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());			
			assertEquals("#1.2", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.currentToken().getState());
			model1.move(1);
			assertEquals("#1.2", ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(-3);
			// #/*"*/
			model1.insertQuote();

			model1.move(-1);
			assertEquals("#2.2", ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(1);
			assertEquals("#2.0", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());
			assertEquals("#2.1", ReducedToken.INSIDE_QUOTE,
									 model1.currentToken().getState());
			assertEquals("#2.3", "/", model1.currentToken().getType());
			model1.move(1);
			assertEquals("#2.4", "*", model1.currentToken().getType());
			// "/#*"*/

			model1.move(2);
			// "/*"#*/
			assertEquals("#5.0", ReducedToken.FREE,
									 model1.getStateAtCurrent());
			assertEquals("#5.1", ReducedToken.FREE,
									 model1.currentToken().getState());
			assertEquals("#5.3", "*", model1.currentToken().getType());
			model1.move(1);
			assertEquals("#5.4", "/", model1.currentToken().getType());
			assertEquals("#5.5", ReducedToken.FREE,
									 model1.currentToken().getState());
		}

	public void testQuoteBreakComment2()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertStar();
			model1.insertSlash();
			model1.move(-4);
			assertEquals("#0.0", "/*", model1.currentToken().getType());
			model1.move(2);
			assertEquals("#0.1", "*/", model1.currentToken().getType());
			model1.move(-2);
			// "#/**/
			model1.insertQuote();
			model1.move(-1);
			assertEquals("#1.0", ReducedToken.FREE,
									 model1.currentToken().getState());
			assertEquals("#1.4", "\"", model1.currentToken().getType());
			model1.move(1);
			assertEquals("#1.1", ReducedToken.INSIDE_QUOTE,
						 model1.getStateAtCurrent());			
			assertEquals("#1.4", "/", model1.currentToken().getType());			
			assertEquals("#1.2", ReducedToken.INSIDE_QUOTE,
									 model1.currentToken().getState());
			model1.move(1);
			assertEquals("#1.3", ReducedToken.INSIDE_QUOTE,
									 model1.currentToken().getState());
			assertEquals("#1.4", "*", model1.currentToken().getType());			
		}

	public void testInsertNewlineEndLineComment()
		{
			model1.insertSlash();
			model1.insertSlash();
			model1.insertGap(5);
			model1.move(-2);
			model1.insertNewline();
			// //___\n#__
			model1.move(-1);
			assertEquals("#0.2", "\n", model1.currentToken().getType());
			assertEquals("#0.4", ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(1);
			assertEquals("#0.0", ReducedToken.FREE, model1.getStateAtCurrent());
			assertTrue("#0.1", model1.currentToken().isGap());
			
			assertEquals("#0.3", 2, model1.currentToken().getSize());
			
			assertEquals("#0.5", ReducedToken.FREE,
									 model1.currentToken().getState());
		}

	public void testInsertNewlineEndQuote()
		{
			model1.insertQuote();
			model1.insertGap(5);
			model1.move(-2);
			model1.insertNewline();
			// "___\n#__
			model1.move(-1);
			assertEquals("#0.4", ReducedToken.FREE,
									 model1.currentToken().getState());
			assertEquals("#0.2", "\n", model1.currentToken().getType());
			model1.move(1);
			assertEquals("#0.0", ReducedToken.FREE, model1.getStateAtCurrent());
			assertTrue("#0.1", model1.currentToken().isGap());
			assertEquals("#0.3", 2, model1.currentToken().getSize());
			assertEquals("#0.5", ReducedToken.FREE,
									 model1.currentToken().getState());
			
		}

	public void testInsertNewlineChainReaction()
		{
			model1.insertSlash();
			model1.insertSlash();
			model1.insertSlash();
			model1.insertStar();
			// ///*#
			model1.move(-1);
			// ///#*
			model1.move(-1);
			assertEquals("#0.2", "/", model1.currentToken().getType());
			assertEquals("#0.3", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.currentToken().getState());
			model1.move(1);
			assertEquals("#0.0", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#0.1", "*", model1.currentToken().getType());			
			assertEquals("#0.4", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.currentToken().getState());
			
			model1.move(1);
			model1.insertNewline();
			model1.insertQuote();
			model1.insertStar();
			model1.insertSlash();
			model1.move(-1);
			// ///*
			// "*#/

			
			assertEquals("#1.0", ReducedToken.INSIDE_QUOTE,
									 model1.getStateAtCurrent());
			assertEquals("#1.1", "/", model1.currentToken().getType());
			assertEquals("#1.4", ReducedToken.INSIDE_QUOTE,
									 model1.currentToken().getState());

			model1.move(-5);

			assertEquals("#2.1", "/", model1.currentToken().getType());

			model1.insertNewline();
			// //
			// #/*
			// "*/
			assertEquals("#3.0", ReducedToken.FREE,
									 model1.getStateAtCurrent());
			assertEquals("#3.4", ReducedToken.FREE,
									 model1.currentToken().getState());
			model1.move(1);
			assertEquals("#3.1", "/*", model1.currentToken().getType());

			// //
			// /*
			// #"*/
			model1.move(2);
			assertEquals("#4.0", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#4.1", "\"", model1.currentToken().getType());
			assertEquals("#4.4", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.currentToken().getState());
			model1.move(1);
			assertEquals("#4.6", "*/",
									 model1.currentToken().getType());
		}

	public void testMoveWithinToken()
		{
			model1.insertGap(10);
			assertTrue("#0.0", model1.atEnd());
			assertEquals("#0.1", 10, model1.absOffset());


			model1.move(-5);
			
			assertTrue("#1.0", model1.currentToken().isGap());
			assertEquals("#1.1", 5, model1.absOffset());

			model1.move(2);
			assertTrue("#2.0", model1.currentToken().isGap());
			assertEquals("#2.1", 7, model1.absOffset());

			model1.move(-4);
			assertTrue("#3.0", model1.currentToken().isGap());
			assertEquals("#3.1", 3, model1.absOffset());

			model1.move(-3);
			assertTrue("#4.0", model1.currentToken().isGap());
			assertEquals("#4.1", 0, model1.absOffset());

			model1.move(10);
			assertTrue("#5.0", model1.atEnd());
			assertEquals("#5.1", 10, model1.absOffset());
		}

	public void testMoveOnEmpty()
		{
			model1.move(0);
			assertTrue("#0.0", model1.atStart());
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
	
	protected ReducedModelControl setUpExample()
		{
			ReducedModelControl model = new ReducedModelControl();
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
		{assertEquals("#0.0", -1, model0.balanceBackward());
			model0 = setUpExample();
			// {
			// ___
			// (__)
			// ___//___
			// "_{_"/*_(_)_*/
			// }#
			assertEquals("#1.0", 36, model0.balanceBackward());
			model0.move(-2);
			assertEquals("#2.0", -1, model0.balanceBackward());
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
}





	






