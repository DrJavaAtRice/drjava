/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.JTextArea;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import javax.swing.text.Keymap;

import java.awt.Toolkit;

import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

public class InteractionsPane extends JTextArea
{
	private AbstractAction _evalAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			setCaretPosition(getInteractionsDocument().getLength());
			getInteractionsDocument().eval();
		}
  };
	
	private AbstractAction _historyPrevAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
      InteractionsDocument doc = getInteractionsDocument();

      if (doc.hasHistoryPrevious()) {
        doc.moveHistoryPrevious();
      }
      else {
        Toolkit.getDefaultToolkit().beep();
      }
		}
  };
	
	private AbstractAction _historyNextAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
      InteractionsDocument doc = getInteractionsDocument();

      if (doc.hasHistoryNext()) {
        doc.moveHistoryNext();
      }
      else {
        Toolkit.getDefaultToolkit().beep();
      }
		}
  };
	
	public InteractionsPane() {
    super(new InteractionsDocument());
    setLineWrap(true);
    setWrapStyleWord(true);

    reset();

		//add actions for enter key, etc.
		Keymap ourMap = addKeymap("INTERACTIONS_KEYMAP", getKeymap());
		
		ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
																 _evalAction);

    // Up and down need to be bound both for keypad and not
		ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0),
																 _historyPrevAction);
		ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
																 _historyPrevAction);

		ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0),
																 _historyNextAction);
		ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
																 _historyNextAction);

		setKeymap(ourMap);

  }

  InteractionsDocument getInteractionsDocument() {
    return (InteractionsDocument) getDocument();
  }

  // The class path will be reset on reset().
  public void addClassPath(String path) {
    getInteractionsDocument().addClassPath(path);
  }

  public void reset() {
    getInteractionsDocument().reset();
    setCaretPosition(getInteractionsDocument().getLength());
  }
  
  public void prompt() {
    getInteractionsDocument().prompt();		
  }
  
  // public boolean atEnd() { return getCaretPosition() == doc.getLength(); }
}
