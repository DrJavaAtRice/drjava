/* $Id$ */

package edu.rice.cs.drjava;

import junit.framework.*;
import java.util.Vector;
import junit.extensions.*;

public class ReducedModelDeleteTest extends TestCase {
	protected ReducedModel model0;
	protected ReducedModel model1;
	protected ReducedModel model2;
	
	public ReducedModelDeleteTest(String name)
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
			return new TestSuite(ReducedModelDeleteTest.class);
		}
	//*********************THE DELETE TESTS************************************/

	public void testDeleteSimple()
		{ 
			model1.insertSlash();
			model1.insertSlash();
			model1.move(-2);
			
			assertEquals("#0.0","//",model1.getCursor().current().getType());
			model1.delete(1);
			
			assertEquals("#1.0","/",model1.getCursor().current().getType());
			assertEquals("#1.1",0,model1.getBlockOffset());
			//System.out.println(model1.simpleString());
	 		model1.insertSlash();
			//System.out.println(model1.simpleString());
			model1.delete(1);   //This time delete the second slash
			//System.out.println(model1.simpleString());
			assertEquals("#2.0","/",model1.getCursor().prevItem().getType());
			assertTrue("#2.1",model1.getCursor().atEnd());
			assertEquals("#2.2",0,model1.getBlockOffset());

			// /#
			model1.move(-1);
			model1.delete(1);
			assertTrue("#3.0",model1.getBraces().isEmpty());

			model1.insertGap(8);
			model1.move(-6);
			model1.delete(3);
		 	assertEquals("#4.0",2,model1.getBlockOffset());
			assertEquals("#4.1",5,model1.getCursor().current().getSize());

			model1.move(3);
			model1.insertSlash();
			model1.insertStar();
			model1.insertGap(6);
			model1.move(-9);
			assertEquals("#5.0",5,model1.getCursor().current().getSize());
			assertEquals("#5.1",true,model1.getCursor().current().isGap());
			assertEquals("#5.2","/*",model1.getCursor().nextItem().getType());
			assertEquals("#5.3",4,model1.getBlockOffset());

			//_____#_/*______
			//System.out.println(model1.simpleString());
			model1.delete(5);
			assertEquals("#6.0",8,model1.getCursor().current().getSize());
			assertEquals("#6.1",true,model1.getCursor().current().isGap());
			assertEquals("#6.2",4,model1.getBlockOffset());
			assertTrue("#6.3",model1.getCursor().atLastItem());
		}

		
	public void testStartDeleteInDoubleBrace2()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertGap(2);
			model1.insertStar();
			model1.insertSlash();
			model1.insertGap(1);
			//  /*__*/_#
			model1.move(-6);
			model1.delete(4);
			//  /#/_

			assertEquals("#0.0","//",model1.getCursor().current().getType());
			assertEquals("#0.2",1,model1.getBlockOffset());
			assertEquals("#0.3",ReducedToken.FREE,
									 model1.getStateAtCurrent());
			model1.getCursor().next(); //move to tail, leave offset == 1
			assertEquals("#0.4",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().current().getState());
		}

	public void testStartDeleteInDoubleBrace3()
		{
			model1.insertSlash();
			model1.insertGap(2);
			model1.insertSlash();
			model1.insertStar();
			//  /__/*#
			model1.move(-4);
			model1.delete(2);
			// /#/*
			assertEquals("#0.0","//",model1.getCursor().current().getType());
			assertEquals("#0.1","*",model1.getCursor().nextItem().getType());
			assertEquals("#0.2",1,model1.getBlockOffset());
			assertEquals("#0.3",ReducedToken.FREE,
									 model1.getStateAtCurrent());
			assertEquals("#0.4",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().nextItem().getState());
			assertEquals("#0.4",ReducedToken.FREE,
									 model1.getCursor().current().getState());
		}
	public void testStartDeleteInDoubleBrace4()
		{
			model1.insertSlash();
			model1.insertGap(2);
			model1.insertSlash();
			model1.insertSlash();
			//  /__/*#
			model1.move(-4);
			model1.delete(2);
			// /#/*
			assertEquals("#0.0","//",model1.getCursor().current().getType());
			assertEquals("#0.1","/",model1.getCursor().nextItem().getType());
			assertEquals("#0.2",1,model1.getBlockOffset());
			assertEquals("#0.3",ReducedToken.FREE,
									 model1.getStateAtCurrent());
			assertEquals("#0.4",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().nextItem().getState());
			assertEquals("#0.5",ReducedToken.FREE,
									 model1.getCursor().current().getState());

		}

	public void testDeleteInsideGap()
		{
			model1.insertGap(15);
			model1.move(-6);
			model1.delete(4);
		 	assertTrue("#0.0",model1.getCursor().current().isGap());
			assertEquals("#0.1",11, model1.getCursor().current().getSize());
			assertEquals("#0.2",9,model1.getBlockOffset());
		}

public void testDeleteThroughToStar()
		{
			model1.insertSlash();
			model1.insertSlash();
			model1.insertStar();
			model1.insertNewline();
			model1.insertSlash();
			model1.insertStar();
			
			assertEquals("#0.4",ReducedToken.FREE,
									 model1.getCursor().prevItem().getState());
			// //*
			// /*#
			model1.move(-3);
			model1.delete(1);
			
			assertEquals("#0.0","/",model1.getCursor().current().getType());
			assertEquals("#0.1","*",model1.getCursor().prevItem().getType());
			assertEquals("#0.2","*",model1.getCursor().nextItem().getType());
			assertEquals("#0.3",0,model1.getBlockOffset());
			assertEquals("#0.4",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#0.5",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().prevItem().getState());
		}

	public void testStartDeleteInDoubleBrace()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertGap(2);
			model1.insertStar();
			model1.insertSlash();
			//  /*__*/#
			
			model1.move(-5);
			model1.delete(3);
			//  /#*/
			assertEquals("#0.0","/*",model1.getCursor().current().getType());
			assertEquals("#0.1","/",model1.getCursor().nextItem().getType());
			assertEquals("#0.2",1,model1.getBlockOffset());
			assertEquals("#0.3",ReducedToken.FREE,
									 model1.getStateAtCurrent());
			assertEquals("#0.4",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getCursor().nextItem().getState());
			assertEquals("#0.4",ReducedToken.FREE,
									 model1.getCursor().current().getState());

			model1.delete(2);
			//  /#
			assertEquals("#1.0", "/", model1.getCursor().prevItem().getType());
			assertEquals("#1.1", 0, model1.getBlockOffset());
			assertEquals("#1.2", ReducedToken.FREE,
									 model1.getStateAtCurrent());

			model1.insertGap(4);
			// /____#

			model1.insertSlash();
			model1.insertSlash();
			// /____//#

			model1.move(-6);
			model1.delete(4);
			// /#//
			assertEquals("#2.0", "//", model1.getCursor().current().getType());
			assertEquals("#2.1", "/", model1.getCursor().nextItem().getType());
			assertEquals("#2.2", 1, model1.getBlockOffset());
			assertEquals("#2.3", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().nextItem().getState());

			model1.move(-1);
			model1.delete(3);
			// <empty>
			assertTrue("#3.0", model1.getBraces().isEmpty());
			assertEquals("#3.1", 0, model1.getBlockOffset());

			model1.insertSlash();
			model1.insertGap(3);
			model1.insertSlash();
			model1.insertStar();
			model1.insertNewline();
			model1.insertGap(2);
			model1.insertOpenParen();
			model1.insertStar();
			model1.insertSlash();
			//  /___/*
			//  __(*/#
			model1.move(-3);
			//  /___/*
			//  __#(*/
			assertEquals("#4.0", "(", model1.getCursor().current().getType());
			assertEquals("#4.1", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getCursor().current().getState());
			assertTrue("#4.2", model1.getCursor().prevItem().isGap());
			assertEquals("#4.3", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getCursor().prevItem().getState());
			assertEquals("#4.4", "*/", model1.getCursor().nextItem().getType());
			assertEquals("#4.5", ReducedToken.FREE,
									 model1.getCursor().nextItem().getState());
			assertEquals("#4.3", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());

			model1.move(-8);
			model1.delete(3);

			//  /#/*
			//  __(*/
			assertEquals("#5.0", "//", model1.getCursor().current().getType());
			assertEquals("#5.1", 1, model1.getBlockOffset());
			assertEquals("#5.2", "*", model1.getCursor().nextItem().getType());
			assertEquals("#5.3", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().nextItem().getState());

			model1.move(6);
			//  //*
			//  __(#*/
			assertEquals("#6.0", "*", model1.getCursor().current().getType());
			assertEquals("#6.1", 0, model1.getBlockOffset());
			assertEquals("#6.2", "(", model1.getCursor().prevItem().getType());
			assertEquals("#6.3", ReducedToken.FREE,
									 model1.getCursor().prevItem().getState());			
			assertEquals("#6.4", ReducedToken.FREE,
									 model1.getCursor().current().getState());

			model1.move(-6);
			model1.delete(1);
			//  /#*
			//  __(*/
			assertEquals("#7.0", "/*", model1.getCursor().current().getType());
			assertEquals("#7.1", 1, model1.getBlockOffset());
			assertEquals("#7.2", "\n", model1.getCursor().nextItem().getType());
			assertEquals("#7.3", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getCursor().nextItem().getState());

			model1.move(5);
			assertEquals("#8.1", "(", model1.getCursor().prevItem().getType());
			assertEquals("#8.2", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getCursor().prevItem().getState());
			assertEquals("#8.3", "*/", model1.getCursor().current().getType());
			assertEquals("#8.4", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());
		}

	public void testStartDeleteGap()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertGap(2);
			model1.insertStar();
			model1.insertSlash();
			//  /*__*/#
			model1.move(-4);
			model1.delete(2);
			assertEquals("#0.0", "/*", model1.getCursor().prevItem().getType());
			assertEquals("#0.1", ReducedToken.FREE,
									 model1.getCursor().prevItem().getState());
			assertEquals("#0.2", "*/", model1.getCursor().current().getType());
			assertEquals("#0.3", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#0.4",0,model1.getBlockOffset());
		}

	public void testDeleteToCloseBlock()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertGap(2);
			model1.insertStar();
			model1.insertSlash();
			//  /*__*/#
			model1.move(-6);
			model1.delete(4);
		
			assertEquals("#0.0", "*", model1.getCursor().current().getType());
			assertEquals("#0.1", ReducedToken.FREE,
									 model1.getCursor().current().getState());
			assertEquals("#0.2", "/", model1.getCursor().nextItem().getType());
			assertEquals("#0.3", ReducedToken.FREE,
									 model1.getStateAtCurrent());
			assertEquals("#0.4", ReducedToken.FREE,
									 model1.getCursor().current().getState());
			assertEquals("#0.5", ReducedToken.FREE,
									 model1.getCursor().nextItem().getState());
			assertEquals("#0.6",0,model1.getBlockOffset());
		}

	public void testDeleteToLine()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertGap(2);
			model1.insertSlash();
			model1.insertSlash();
			//  /*__*/#
			model1.move(-6);
			model1.delete(4);

			assertEquals("#0.0", "//", model1.getCursor().current().getType());
			assertEquals("#0.1", ReducedToken.FREE,
									 model1.getCursor().current().getState());
			assertEquals("#0.3", ReducedToken.FREE,
									 model1.getStateAtCurrent());
			assertEquals("#0.4", ReducedToken.FREE,
									 model1.getCursor().current().getState());
			assertEquals("#0.5",0,model1.getBlockOffset());
		}
	
	public void testCrazyDelete()
		{
			model1.insertSlash();
			model1.insertSlash();
			model1.insertStar();
			model1.insertGap(2);
			model1.insertNewline();
			model1.insertSlash();
			model1.insertSlash();

			model1.move(-6);
			model1.delete(4);
			
			assertEquals("#0.0", "/", model1.getCursor().current().getType());
			assertEquals("#0.1", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().current().getState());
			assertEquals("#0.2", "/", model1.getCursor().nextItem().getType());
			assertEquals("#0.3", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#0.4", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().current().getState());
			assertEquals("#0.5", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().nextItem().getState());
			assertEquals("#0.6",0,model1.getBlockOffset());
		}


	public void testDeleteThroughToStar2()
		{
			model1.insertSlash();
			model1.insertSlash();
			model1.insertStar();
			model1.insertNewline();
			model1.insertSlash();
			model1.insertStar();
			
			assertEquals("#0.4",ReducedToken.FREE,
									 model1.getCursor().prevItem().getState());
			// //*
			// /*#
			model1.move(-2);
			model1.delete(-1);
			
			assertEquals("#0.0","/",model1.getCursor().current().getType());
			assertEquals("#0.1","*",model1.getCursor().prevItem().getType());
			assertEquals("#0.2","*",model1.getCursor().nextItem().getType());
			assertEquals("#0.3",0,model1.getBlockOffset());
			assertEquals("#0.4",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#0.5",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().prevItem().getState());
		}

	public void testStartDeleteInDoubleBrace5()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertGap(2);
			model1.insertStar();
			model1.insertSlash();
			//  /*__*/#
			
			model1.move(-2);
			model1.delete(-3);
			//  /#*/
			assertEquals("#0.0","/*",model1.getCursor().current().getType());
			assertEquals("#0.1","/",model1.getCursor().nextItem().getType());
			assertEquals("#0.2",1,model1.getBlockOffset());
			assertEquals("#0.3",ReducedToken.FREE,
									 model1.getStateAtCurrent());
			assertEquals("#0.4",ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getCursor().nextItem().getState());
			assertEquals("#0.4",ReducedToken.FREE,
									 model1.getCursor().current().getState());
			model1.move(2);
			model1.delete(-2);
			//  /#
			assertEquals("#1.0", "/", model1.getCursor().prevItem().getType());
			assertEquals("#1.1", 0, model1.getBlockOffset());
			assertEquals("#1.2", ReducedToken.FREE,
									 model1.getStateAtCurrent());

			model1.insertGap(4);
			// /____#

			model1.insertSlash();
			model1.insertSlash();
			// /____//#

			model1.move(-2);
			model1.delete(-4);
			// /#//
			assertEquals("#2.0", "//", model1.getCursor().current().getType());
			assertEquals("#2.1", "/", model1.getCursor().nextItem().getType());
			assertEquals("#2.2", 1, model1.getBlockOffset());
			assertEquals("#2.3", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().nextItem().getState());

			model1.move(2);
			model1.delete(-3);
			// <empty>
			assertTrue("#3.0", model1.getBraces().isEmpty());
			assertEquals("#3.1", 0, model1.getBlockOffset());

			model1.insertSlash();
			model1.insertGap(3);
			model1.insertSlash();
			model1.insertStar();
			model1.insertNewline();
			model1.insertGap(2);
			model1.insertOpenParen();
			model1.insertStar();
			model1.insertSlash();
			//  /___/*
			//  __(*/#
			model1.move(-3);
			//  /___/*
			//  __#(*/
			assertEquals("#4.0", "(", model1.getCursor().current().getType());
			assertEquals("#4.1", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getCursor().current().getState());
			assertTrue("#4.2", model1.getCursor().prevItem().isGap());
			assertEquals("#4.3", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getCursor().prevItem().getState());
			assertEquals("#4.4", "*/", model1.getCursor().nextItem().getType());
			assertEquals("#4.5", ReducedToken.FREE,
									 model1.getCursor().nextItem().getState());
			assertEquals("#4.3", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());

			model1.move(-5);
			model1.delete(-3);

			//  /#/*
			//  __(*/
			assertEquals("#5.0", "//", model1.getCursor().current().getType());
			assertEquals("#5.1", 1, model1.getBlockOffset());
			assertEquals("#5.2", "*", model1.getCursor().nextItem().getType());
			assertEquals("#5.3", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().nextItem().getState());

			model1.move(6);
			//  //*
			//  __(#*/
			assertEquals("#6.0", "*", model1.getCursor().current().getType());
			assertEquals("#6.1", 0, model1.getBlockOffset());
			assertEquals("#6.2", "(", model1.getCursor().prevItem().getType());
			assertEquals("#6.3", ReducedToken.FREE,
									 model1.getCursor().prevItem().getState());			
			assertEquals("#6.4", ReducedToken.FREE,
									 model1.getCursor().current().getState());

			model1.move(-5);
			model1.delete(-1);
			//  /#*
			//  __(*/
			assertEquals("#7.0", "/*", model1.getCursor().current().getType());
			assertEquals("#7.1", 1, model1.getBlockOffset());
			assertEquals("#7.2", "\n", model1.getCursor().nextItem().getType());
			assertEquals("#7.3", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getCursor().nextItem().getState());

			model1.move(5);
			assertEquals("#8.1", "(", model1.getCursor().prevItem().getType());
			assertEquals("#8.2", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getCursor().prevItem().getState());
			assertEquals("#8.3", "*/", model1.getCursor().current().getType());
			assertEquals("#8.4", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());
		}

	public void testStartDeleteGap2()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertGap(2);
			model1.insertStar();
			model1.insertSlash();
			//  /*__*/#
			model1.move(-2);
			model1.delete(-2);
			assertEquals("#0.0", "/*", model1.getCursor().prevItem().getType());
			assertEquals("#0.1", ReducedToken.FREE,
									 model1.getCursor().prevItem().getState());
			assertEquals("#0.2", "*/", model1.getCursor().current().getType());
			assertEquals("#0.3", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#0.4",0,model1.getBlockOffset());
		}

	public void testDeleteToCloseBlock2()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertGap(2);
			model1.insertStar();
			model1.insertSlash();
			//  /*__*/#
			model1.move(-2);
			model1.delete(-4);
		
			assertEquals("#0.0", "*", model1.getCursor().current().getType());
			assertEquals("#0.1", ReducedToken.FREE,
									 model1.getCursor().current().getState());
			assertEquals("#0.2", "/", model1.getCursor().nextItem().getType());
			assertEquals("#0.3", ReducedToken.FREE,
									 model1.getStateAtCurrent());
			assertEquals("#0.4", ReducedToken.FREE,
									 model1.getCursor().current().getState());
			assertEquals("#0.5", ReducedToken.FREE,
									 model1.getCursor().nextItem().getState());
			assertEquals("#0.6",0,model1.getBlockOffset());
		}
public void testDeleteToLine2()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertGap(2);
			model1.insertSlash();
			model1.insertSlash();
			//  /*__*/#
			model1.move(-2);
			model1.delete(-4);

			assertEquals("#0.0", "//", model1.getCursor().current().getType());
			assertEquals("#0.1", ReducedToken.FREE,
									 model1.getCursor().current().getState());
			assertEquals("#0.3", ReducedToken.FREE,
									 model1.getStateAtCurrent());
			assertEquals("#0.4", ReducedToken.FREE,
									 model1.getCursor().current().getState());
			assertEquals("#0.5",0,model1.getBlockOffset());
		}
	
	public void testCrazyDelete2()
		{
			model1.insertSlash();
			model1.insertSlash();
			model1.insertStar();
			model1.insertGap(2);
			model1.insertNewline();
			model1.insertSlash();
			model1.insertSlash();

			model1.move(-2);
			model1.delete(-4);
			
			assertEquals("#0.0", "/", model1.getCursor().current().getType());
			assertEquals("#0.1", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().current().getState());
			assertEquals("#0.2", "/", model1.getCursor().nextItem().getType());
			assertEquals("#0.3", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getStateAtCurrent());
			assertEquals("#0.4", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().current().getState());
			assertEquals("#0.5", ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().nextItem().getState());
			assertEquals("#0.6",0,model1.getBlockOffset());
		}


	public void testDeleteSimple2()
		{ 
			model1.insertSlash();
			model1.insertSlash();
			model1.move(-1);
			
			assertEquals("#0.0","//",model1.getCursor().current().getType());
			model1.delete(-1);
			
			assertEquals("#1.0","/",model1.getCursor().current().getType());
			assertEquals("#1.1",0,model1.getBlockOffset());
	 		model1.insertSlash();

			model1.move(1);
			model1.delete(-1);   //This time delete the second slash

			assertEquals("#2.0","/",model1.getCursor().prevItem().getType());
			assertTrue("#2.1",model1.getCursor().atEnd());
			assertEquals("#2.2",0,model1.getBlockOffset());

			// /#
			model1.delete(-1);
			assertTrue("#3.0",model1.getBraces().isEmpty());

			model1.insertGap(8);
			model1.move(-3);
			model1.delete(-3);
		 	assertEquals("#4.0",2,model1.getBlockOffset());
			assertEquals("#4.1",5,model1.getCursor().current().getSize());

			model1.move(3);
			model1.insertSlash();
			model1.insertStar();
			model1.insertGap(6);
			model1.move(-9);
			assertEquals("#5.0",5,model1.getCursor().current().getSize());
			assertEquals("#5.1",true,model1.getCursor().current().isGap());
			assertEquals("#5.2","/*",model1.getCursor().nextItem().getType());
			assertEquals("#5.3",4,model1.getBlockOffset());

			//_____#_/*______
			//System.out.println(model1.simpleString());
			model1.move(5);
			model1.delete(-5);
			assertEquals("#6.0",8,model1.getCursor().current().getSize());
			assertEquals("#6.1",true,model1.getCursor().current().isGap());
			assertEquals("#6.2",4,model1.getBlockOffset());
			assertTrue("#6.3",model1.getCursor().atLastItem());
		}

		
	public void testStartDeleteInDoubleBrace6()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertGap(2);
			model1.insertStar();
			model1.insertSlash();
			model1.insertGap(1);
			//  /*__*/_#
			model1.move(-2);
			model1.delete(-4);
			//  /#/_

			assertEquals("#0.0","//",model1.getCursor().current().getType());
			assertEquals("#0.2",1,model1.getBlockOffset());
			assertEquals("#0.3",ReducedToken.FREE,
									 model1.getStateAtCurrent());
			model1.getCursor().next(); //move to tail, leave offset == 1
			assertEquals("#0.4",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().current().getState());
		}

	public void testStartDeleteInDoubleBrace7()
		{
			model1.insertSlash();
			model1.insertGap(2);
			model1.insertSlash();
			model1.insertStar();
			//  /__/*#
			model1.move(-2);
			model1.delete(-2);
			// /#/*
			assertEquals("#0.0","//",model1.getCursor().current().getType());
			assertEquals("#0.1","*",model1.getCursor().nextItem().getType());
			assertEquals("#0.2",1,model1.getBlockOffset());
			assertEquals("#0.3",ReducedToken.FREE,
									 model1.getStateAtCurrent());
			assertEquals("#0.4",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().nextItem().getState());
			assertEquals("#0.4",ReducedToken.FREE,
									 model1.getCursor().current().getState());
		}
	public void testStartDeleteInDoubleBrace8()
		{
			model1.insertSlash();
			model1.insertGap(2);
			model1.insertSlash();
			model1.insertSlash();
			//  /__/*#
			model1.move(-2);
			model1.delete(-2);
			// /#/*
			assertEquals("#0.0","//",model1.getCursor().current().getType());
			assertEquals("#0.1","/",model1.getCursor().nextItem().getType());
			assertEquals("#0.2",1,model1.getBlockOffset());
			assertEquals("#0.3",ReducedToken.FREE,
									 model1.getStateAtCurrent());
			assertEquals("#0.4",ReducedToken.INSIDE_LINE_COMMENT,
									 model1.getCursor().nextItem().getState());
			assertEquals("#0.5",ReducedToken.FREE,
									 model1.getCursor().current().getState());

		}

	public void testDeleteInsideGap2()
		{
			model1.insertGap(15);
			model1.move(-2);
			model1.delete(-4);
		 	assertTrue("#0.0",model1.getCursor().current().isGap());
			assertEquals("#0.1",11, model1.getCursor().current().getSize());
			assertEquals("#0.2",9,model1.getBlockOffset());
		}

	public void testDeleteMakeBlockCommentStart()
		{
			model1.insertSlash();
			model1.insertGap(3);
			model1.insertStar();
			model1.move(-1);
			model1.delete(-3);

			assertEquals("#0.0", "/*", model1.getCursor().current().getType());
			assertEquals("#0.1", 1, model1.getBlockOffset());
		}

	public void testDeleteCloseOpenBlockComment()
		{
			model1.insertSlash();
			model1.insertStar();
			model1.insertGap(1);
			model1.insertStar();
			model1.insertGap(2);
			model1.insertSlash();
			model1.move(-1);
			model1.delete(-2);

			assertEquals("#0.0", "*/", model1.getCursor().current().getType());
			assertEquals("#0.1", 1, model1.getBlockOffset());
			model1.move(1);
			assertEquals("#0.2", ReducedToken.FREE,
									 model1.getStateAtCurrent());
		}

	public void testDeleteMakeLineCommentStart()
		{
			model1.insertSlash();
			model1.insertGap(3);
			model1.insertSlash();
			model1.move(-1);
			model1.delete(-3);

			assertEquals("#0.0", "//", model1.getCursor().current().getType());
			assertEquals("#0.1", 1, model1.getBlockOffset());
		}
	/** tests delete function in places where it formerly wasn't allowed. */
	
	public void testDeleteInsideBrace()
		{
			model0.insertSlash();
			model0.insertStar();
			assertEquals("#0.0", ReducedToken.INSIDE_BLOCK_COMMENT,
									 model0.getStateAtCurrent());
			model0.move(-1);
      model0.delete(1);
			assertEquals("#0.1", ReducedToken.FREE,
									 model0.getStateAtCurrent());

			model1.insertGap(5);
			model1.insertSlash();
			model1.insertStar();
			model1.move(-7);
      model1.delete(6);
		}


	/** sets up a reduction for the delete tests */
	
	protected ReducedModel setUpForDelete()
		{
			ReducedModel model = new ReducedModel();
			model.insertGap(5);
			model.insertNewline();
			model.insertOpenSquiggly();
			model.insertGap(4);
			model.insertNewline();
			model.insertGap(4);
			model.insertNewline();
			model.insertClosedSquiggly();
			// _____
			// {____
			// ____
			// }
			return model;
		}

	/** tests simple single-brace (or simple gap) deletes */
	
	public void testDeleteSimple10()
		{
			model0 = setUpForDelete();
			// _____
			// {____
			// ____
			// }#
			assertEquals("#0.0", 18, model0.absOffset());

			model0.move(-6);
			model0.delete(5);
      // _____
			// {____#
			assertEquals("#1.0", 12,model0.absOffset());			

			model0.move(-8);
      // ____#_
			// {____
			assertEquals("#2.0", 4, model0.absOffset());

			model0.delete(-4);	
      // #_
			// {____
			assertEquals("#3.0", 0, model0.absOffset());
		}

	/** tests multiple-brace deletes */

	public void testDeleteComplex()
		{
			model0 = setUpForDelete();
			// _____
			// {____
			// ____
			// }#
			assertEquals("#0.0", 18,model0.absOffset());
			model0.move(-14);
      // ____#_
			// {____
			// ____
			// }
			assertEquals("#1.0", 4, model0.absOffset());

			model0.delete(13);
      // ____#}
			assertEquals("#2.0", 4, model0.absOffset());

			model1 = setUpForDelete();
			// _____
			// {____
			// ____
			// }#
			assertEquals("#3.0", 18,model1.absOffset());

			// _____#
			model1.delete(-13);
			assertEquals("#4.0", 5, model1.absOffset());
		}


}
