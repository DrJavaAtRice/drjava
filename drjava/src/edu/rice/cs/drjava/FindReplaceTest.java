/* $Id$ */

package edu.rice.cs.drjava;

import junit.framework.*;
import java.util.Vector;
import junit.extensions.*;
import java.io.PrintStream;

import javax.swing.text.BadLocationException;

public class FindReplaceTest extends TestCase
{
	DefinitionsView _view;
	
	String testDoc = "This is a test document. It is only a test.\n" + // 44
	"If you wish to use DrJava in a non-test manner, you shouldn't\n" + //62
	"be invoking this file.  I hope that you take this under\n" + // 56
	"advisement.  No seriously.  You're testing my patience.  Get lost.\n"; //70
	
	public FindReplaceTest(String name)
		{
			super(name);
		}
	
	public static Test suite()
		{
			return new TestSuite(FindReplaceTest.class);
		}
	
	public void setUp()
		{
			MainFrame frame = new MainFrame();
			_view = frame.getDefView();
			frame.getOutView().deactivate();
		}
	
	public void testFindNext()
		{
			try {
				_view.getDocument().insertString(0,testDoc,null);
			}
			catch (BadLocationException e) {}

			// confirm = yes
			assertEquals("#0.0", testDoc.length(), _view.getDocument().getLength());
			assertEquals("#0.1", 0, _view.getCaretPosition());
			assertTrue("#0.2", _view.findNextTextHelper("test", false, true));
			assertEquals("#0.3", 14, _view.getCaretPosition());
			assertTrue("#0.4", _view.findNextTextHelper("test", false, true));
			assertEquals("#0.5", 42, _view.getCaretPosition());
			_view.setCaretPosition(162);
			assertTrue("#0.6", _view.findNextTextHelper("test", false, true));			
			assertEquals("#0.7", 201, _view.getCaretPosition());
			assertTrue("#0.8", !_view.findNextTextHelper("monkey", false, true));
			assertTrue("#0.9", _view.findNextTextHelper("test", false, true));
			assertEquals("#0.10", 14, _view.getCaretPosition());
			_view.setCaretPosition(201);
			// confirm = no
			assertTrue("#1.0", !_view.findNextTextHelper("test", false, false));
			assertTrue("#1.1", !_view.findNextTextHelper("monkey", false, false));
			
		}

	public void testReplace()
		{
			try {				
				_view.getDocument().insertString(
					0,
					"Tell all your friends.  I am Batman.",
					null);
			}
			catch (BadLocationException e) {}
			
			assertTrue("#0.0", _view.findNextTextHelper("Batman", false, true));
			assertEquals("#0.1", "Batman", _view.getSelectedText());
			assertEquals("#0.2", 29, _view.getSelectionStart());
			assertEquals("#0.3", 35, _view.getSelectionEnd());

			assertTrue("#1.0", _view.replaceText("Batman","the Walrus"));
			assertEquals("#1.1", "the Walrus", _view.getSelectedText());
			assertEquals("#1.2", 29, _view.getSelectionStart());
			assertEquals("#1.3", 39, _view.getSelectionEnd());

			_view.setCaretPosition(14);
			assertTrue("#2.0", !_view.replaceText("friends", "monkeys"));

			_view.moveCaretPosition(7);
			assertTrue("#3.0", !_view.replaceText("freinds", "goats"));
		}

	public void testReplaceFindNext()
		{
			try {				
				_view.getDocument().insertString(
					0,
					"To be or not to be, that is the question.",
					null);
			}
			catch (BadLocationException e) {}

			assertTrue("#0.0", _view.findNextTextHelper("be", false, true));
			assertEquals("#0.1", "be", _view.getSelectedText());

			assertTrue("#1.0", _view.replaceFindTextHelper("be", "rock",
																										 false, true));
			assertEquals("#1.1", 18, _view.getSelectionStart());
			assertEquals("#1.2", 20, _view.getSelectionEnd());

			assertTrue("#2.0", !_view.replaceFindTextHelper("be", "rock",
																											false, true));
			int length = _view.getDocument().getLength();
			try {
			assertEquals("#2.1",
									 "To rock or not to rock, that is the question.",
									 _view.getDocument().getText(0, length));
			}
			catch (BadLocationException f) {}

			assertEquals("#2.2", 22, _view.getCaretPosition());

			// confirm = yes
			_view.setCaretPosition(15);
			assertTrue("#4.0", _view.findNextTextHelper("rock", false, true));
			assertTrue("#4.1",
			_view.replaceFindTextHelper("rock", "be", false, true));
			assertEquals("#4.2", 7, _view.getCaretPosition());
			assertTrue("#4.3",
									 !_view.replaceFindTextHelper("rock", "be", false, true));
			
			length = _view.getDocument().getLength();
			try {
			assertEquals("#4.4",
									 "To be or not to be, that is the question.",
									 _view.getDocument().getText(0, length));
			}
			catch (BadLocationException f) {}

      // confirm = no
			_view.setCaretPosition(15);
			assertTrue("#5.0", _view.findNextTextHelper("be", false, false));
			assertTrue("#5.1",
								 !_view.replaceFindTextHelper("be", "rock", false, false));

			length = _view.getDocument().getLength();
			try {
			assertEquals("#5.2",
									 "To be or not to rock, that is the question.",
									 _view.getDocument().getText(0, length));
			}
			catch (BadLocationException f) {}

		}

	public void testReplaceAll()
		{
			try {				
				_view.getDocument().insertString(
					0,
					"First shalt thou take out the Holy Pin. Then shalt thou count " +
					"to three, no more, no less. Three shall be the number thou " +
					"shalt count, and the number of the counting shall be three. " +
					"Four shalt thou not count, neither count thou two, excepting " +
					"that thou then proceed to three. Five is right out. Once the " +
					"number three, being the third number, be reached, then lobbest " +
					"thou thy Holy Hand Grenade of Antioch towards thy foe, who, " +
					"being naughty in my sight, shall snuff it. ",
					null);
			}
			catch (BadLocationException e) {}

			int count = _view.replaceAllTextHelper("thou", "you", false, true);
			assertEquals("#0.0", 7, count);

			_view.setCaretPosition(39);
			count = _view.replaceAllTextHelper("you", "thou", false, true);
			assertEquals("#1.0", 7, count);

			// confirm = no
			_view.setCaretPosition(60);
			count = _view.replaceAllTextHelper("thou", "you", false, false);
			assertEquals("#2.0", 5, count);
		}
}

