/* $Id$ */

package edu.rice.cs.drjava;

import junit.framework.*;
import java.util.Vector;
import junit.extensions.*;

public class IndentTest extends TestCase
{
	protected DefinitionsDocument doc;
	static String noBrace = IndentInfo.noBrace;
	static String openSquiggly = IndentInfo.openSquiggly;
	static String openParen = IndentInfo.openParen;
	static String openBracket = IndentInfo.openBracket;
	
	public IndentTest(String name)
		{
			super(name);
		}
	
	public void setUp()
		{
			doc = new DefinitionsDocument();
		}

	public static Test suite()
		{
			return new TestSuite(IndentTest.class);
		}

	public void testIndentInfoSquiggly () {
		try {
			//empty document
			BraceReduction rm = doc._reduced;
			IndentInfo ii = rm.getIndentInformation();
			assertEquals("1.0", noBrace, ii.braceType);
			assertEquals("1.1", -1, ii.distToNewline);
			assertEquals("1.2", -1, ii.distToBrace);

			//single newline
			doc.insertString(0, "\n", null);
			assertEquals("0.1", "\n", doc.getText(0, doc.getLength()));
			ii = rm.getIndentInformation();
			assertEquals("2.0", noBrace, ii.braceType);
			assertEquals("2.1", -1, ii.distToNewline);
			assertEquals("2.2", -1, ii.distToBrace);

			//single layer brace
			doc.insertString(0, "{\n\n", null);
			// {\n\n#\n
			assertEquals("0.2", "{\n\n\n", doc.getText(0, doc.getLength()));
			ii = rm.getIndentInformation();
			assertEquals("3.0", openSquiggly, ii.braceType);
			assertEquals("3.2", 3, ii.distToBrace);
			assertEquals("3.1", 3, ii.distToNewline);
			

			//another squiggly
			doc.insertString(3, "{\n\n", null);
			// {\n\n{\n\n#\n
			assertEquals("0.3", "{\n\n{\n\n\n", doc.getText(0, doc.getLength()));
			ii = rm.getIndentInformation();
			assertEquals("4.0", openSquiggly, ii.braceType);
			assertEquals("4.1", 3, ii.distToNewline);
			assertEquals("4.2", 3, ii.distToBrace);

			//brace with whitespace
			doc.insertString(6, "  {\n\n", null);
			// {\n\n{\n\n  {\n\n#\n
			assertEquals("0.4", "{\n\n{\n\n  {\n\n\n", doc.getText(0, doc.getLength()));
			ii = rm.getIndentInformation();
			assertEquals("5.0", openSquiggly, ii.braceType);
			assertEquals("5.1", 5, ii.distToNewline);
			assertEquals("5.2", 3, ii.distToBrace);
		}
		catch( javax.swing.text.BadLocationException e)
			{
				System.out.println(e.toString());
			}
	}

	public void testIndentInfoParen () {
		try {
			// just paren
			BraceReduction rm = doc._reduced;
			doc.insertString(0, "\n(\n", null);
			IndentInfo ii = rm.getIndentInformation();
			assertEquals("1.0", openParen, ii.braceType);
			assertEquals("1.2", 2, ii.distToBrace);
			assertEquals("1.1", 2, ii.distToNewline);
			

			// paren with stuff in front
			doc.insertString(1, "  helo ", null);
			doc.move(2);
			// \n  helo (\n#
			assertEquals("0.1", "\n  helo (\n", doc.getText(0, doc.getLength()));
			ii = rm.getIndentInformation();
			assertEquals("2.0", openParen, ii.braceType);
			assertEquals("2.1", 9, ii.distToNewline);
			assertEquals("2.2", 2, ii.distToBrace);

			//single layer brace
			doc.move(-1);
			doc.insertString(9, " (", null);
			doc.move(1);
			// \n  helo ( (\n#
			assertEquals("0.2", "\n  helo ( (\n", doc.getText(0, doc.getLength()));
			ii = rm.getIndentInformation();
			assertEquals("3.0", openParen, ii.braceType);
			assertEquals("3.1", 11, ii.distToNewline);
			assertEquals("3.2", 2, ii.distToBrace);

		} 
		catch( javax.swing.text.BadLocationException e)
			{ 
			 	System.out.println(e.toString());
			} 
	}

	public void testIndentInfoBracket () {
		try {
			// just bracket
			BraceReduction rm = doc._reduced;
			doc.insertString(0, "\n[\n", null);
			IndentInfo ii = rm.getIndentInformation();
			assertEquals("1.0", openBracket, ii.braceType);
			assertEquals("1.1", 2, ii.distToNewline);
			assertEquals("1.2", 2, ii.distToBrace);

			// bracket with stuff in front
			doc.insertString(1, "  helo ", null);
			doc.move(2);
			// \n  helo (\n#
			assertEquals("0.1", "\n  helo [\n", doc.getText(0, doc.getLength()));
			ii = rm.getIndentInformation();
			assertEquals("2.0", openBracket, ii.braceType);
			assertEquals("2.1", 9, ii.distToNewline);
			assertEquals("2.2", 2, ii.distToBrace);

			//single layer brace
			doc.move(-1);
			doc.insertString(9, " [", null);
			doc.move(1);
			// \n  helo ( (\n#
			assertEquals("0.2", "\n  helo [ [\n", doc.getText(0, doc.getLength()));
			ii = rm.getIndentInformation();
			assertEquals("3.0", openBracket, ii.braceType);
			assertEquals("3.1", 11, ii.distToNewline);
			assertEquals("3.2", 2, ii.distToBrace);
		} 
		catch( javax.swing.text.BadLocationException e)
			{
				System.out.println(e.toString());
			}
	} 
	
	public void testSkippingBraces () {
		try {
			// just paren
			BraceReduction rm = doc._reduced;
			doc.insertString(0, "\n{\n   { ()}\n}", null);
			IndentInfo ii = rm.getIndentInformation();
			assertEquals("1.0", openSquiggly, ii.braceType);
			assertEquals("1.2", 12, ii.distToBrace);
			assertEquals("1.1", 12, ii.distToNewline);
		}
		catch( javax.swing.text.BadLocationException e)
			{ 
			 	System.out.println(e.toString());
			} 
	}

	public void testSkippingBraces2 () {
		try {
			// just paren
			BraceReduction rm = doc._reduced;
			doc.insertString(0, "\n{\n   { ()\n}", null);
			IndentInfo ii = rm.getIndentInformation();
			assertEquals("1.0", openSquiggly, ii.braceType);
			assertEquals("1.2", 6, ii.distToBrace);
			assertEquals("1.1", 9, ii.distToNewline);
		}
		catch( javax.swing.text.BadLocationException e)
			{ 
			 	System.out.println(e.toString());
			} 
	}

		public void testSkippingComments () {
		try {
			// just paren
			BraceReduction rm = doc._reduced;
			doc.insertString(0, "\n{\n   //{ ()\n}", null);
			IndentInfo ii = rm.getIndentInformation();
			assertEquals("1.0", openSquiggly, ii.braceType);
			assertEquals("1.2", 13, ii.distToBrace);
			assertEquals("1.1", 13, ii.distToNewline);
		}
		catch( javax.swing.text.BadLocationException e)
			{ 
			 	System.out.println(e.toString());
			} 
	}

	public void testSkippingCommentsBraceAtBeginning () {
		try {
			// just paren
			BraceReduction rm = doc._reduced;
			doc.insertString(0, "{\n   //{ ()}{", null);
			IndentInfo ii = rm.getIndentInformation();
			assertEquals("1.0", openSquiggly, ii.braceType);
			assertEquals("1.2", 13, ii.distToBrace);
			assertEquals("1.1", 13, ii.distToNewline);
		}
		catch( javax.swing.text.BadLocationException e)
			{ 
			 	System.out.println(e.toString());
			} 
	}

	public void testNothingToIndentOn () {
		try {
			// just paren
			BraceReduction rm = doc._reduced;
			doc.insertString(0, "   //{ ()}{", null);
			IndentInfo ii = rm.getIndentInformation();
			assertEquals("1.0", noBrace, ii.braceType);
			assertEquals("1.2", -1, ii.distToBrace);
			assertEquals("1.1", -1, ii.distToNewline);
		}
		catch( javax.swing.text.BadLocationException e)
			{ 
			 	System.out.println(e.toString());
			} 
	}
	
	public void testStartSimple () {
		try {
			// just paren
			BraceReduction rm = doc._reduced;
			doc.insertString(0, "abcde", null);
			doc.indentLine();
			assertEquals("0.1", "abcde", doc.getText(0, doc.getLength()));
		}
		catch( javax.swing.text.BadLocationException e)
			{ 
			 	System.out.println(e.toString());
			} 
	}
	
	
	public void testStartSpaceIndent () {
		try {
			// just paren
			BraceReduction rm = doc._reduced;
			doc.insertString(0, "  abcde", null);
			doc.indentLine(); 
			assertEquals("0.1", "abcde", doc.getText(0, doc.getLength()));
		} 
		catch( javax.swing.text.BadLocationException e)
			{ 
			 	System.out.println(e.toString());
			} 
	} 
	

	public void testStartBrace() 
		{
		try {
			// just paren
			BraceReduction rm = doc._reduced;
			doc.insertString(0, "public class temp \n {", null);
			doc.indentLine();
			assertEquals("0.1", "public class temp \n{",
									 doc.getText(0, doc.getLength()));
		}
		catch( javax.swing.text.BadLocationException e)
			{ 
			 	System.out.println(e.toString());
			} 
		}

		public void testEndBrace () {
		try {
			// just paren
			BraceReduction rm = doc._reduced;
			doc.insertString(0, "public class temp \n{ \n  }", null);
			doc.indentLine();
			assertEquals("0.1", "public class temp \n{ \n}",
									 doc.getText(0, doc.getLength()));
		}
		catch( javax.swing.text.BadLocationException e)
			{ 
			 	System.out.println(e.toString());
			} 
	}

	public void testInsideClass () {
		try {
			// just paren
			BraceReduction rm = doc._reduced;
			doc.insertString(0, "public class temp \n{ \ntext here", null);
			doc.indentLine();
			assertEquals("0.1", "public class temp \n{ \n  text here",
									 doc.getText(0, doc.getLength()));
		}
		catch( javax.swing.text.BadLocationException e)
			{ 
			 	System.out.println(e.toString());
			} 
	}

	public void testInsideClassWithBraceSets () {
		try {
			// just paren
			BraceReduction rm = doc._reduced;
			doc.insertString(0, "public class temp \n{  ()\ntext here", null);
			doc.indentLine();
			assertEquals("0.1", "public class temp \n{  ()\n  text here",
									 doc.getText(0, doc.getLength()));
		}
		catch( javax.swing.text.BadLocationException e)
			{ 
			 	System.out.println(e.toString());
			} 
	}

	public void testIgnoreBraceOnSameLine ()
		{
			try {
				// just paren
				BraceReduction rm = doc._reduced;
				doc.insertString(0, "public class temp \n{  ()\n{text here", null);
				doc.indentLine();
				assertEquals("0.1", "public class temp \n{  ()\n  {text here",
										 doc.getText(0, doc.getLength()));			
			}
			catch( javax.swing.text.BadLocationException e)
				{ 
					System.out.println(e.toString());
				} 
		}

	public void testLargerIndent()
		{
			try {
				// just paren
				BraceReduction rm = doc._reduced;
				doc.insertString(0, "public class temp \n  {  ()\n { text here", null);
				doc.indentLine();
				assertEquals("0.1", "public class temp \n  {  ()\n    { text here",
								 		 doc.getText(0, doc.getLength()));			
			} 
			catch( javax.swing.text.BadLocationException e)
				{ 
				 	System.out.println(e.toString());
				}  
		}

	public void testWierdFuckup()
		{
			try {
				// just paren
				BraceReduction rm = doc._reduced;
				doc.insertString(0, "hello\n", null);
				doc.indentLine();
				assertEquals("0.1", "hello\n", doc.getText(0, doc.getLength()));			
			} 
			catch( javax.swing.text.BadLocationException e)
				{ 
				 	System.out.println(e.toString());
				}  
		}

	public void testWierdFuckup2()
		{
			try {
				// just paren
				BraceReduction rm = doc._reduced;
				doc.insertString(0, "hello", null);
				doc.indentLine();
				assertEquals("0.1", "hello", doc.getText(0, doc.getLength()));			
			} 
			catch( javax.swing.text.BadLocationException e)
				{ 
				 	System.out.println(e.toString());
				}  
		} 

	public void testMotion()
		{
			try {
				// just paren
				BraceReduction rm = doc._reduced;
				doc.insertString(0, "hes{\n{abcde", null);
				doc.insertString(11,"\n{",null);
				// hes{\n{abcde\n{#
				doc.move(-8);
				// hes{\n#{abcde\n{
				doc.indentLine();
				// hes{\n  #{abcde\n{
				assertEquals("0.1", "hes{\n  {abcde\n{", doc.getText(0, doc.getLength()));			
			} 
			catch( javax.swing.text.BadLocationException e)
				{ 
				 	System.out.println(e.toString());
				}  
		} 

		public void testNextCharIsNewline()
		{
			try {
				// just paren
				BraceReduction rm = doc._reduced;
				doc.insertString(0, "hes{\n{abcde", null);
				doc.insertString(11,"\n{",null);
				// hes{\n{abcde\n{#
				doc.move(-2);
				// hes{\n{abcde#\n{
				doc.indentLine();
				// hes{\n  {abcde#\n{
				assertEquals("0.1", "hes{\n  {abcde\n{", doc.getText(0, doc.getLength()));
			} 
			catch( javax.swing.text.BadLocationException e)
				{ 
				 	System.out.println(e.toString());
				}  
		}
}






