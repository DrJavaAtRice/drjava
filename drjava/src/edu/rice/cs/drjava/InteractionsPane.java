/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.JTextArea;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import javax.swing.text.Keymap;

import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

public class InteractionsView extends JTextArea
{
	private class EvalAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			setCaretPosition(getInteractionsDocument().getLength());
			getInteractionsDocument().eval();
		}
	}
	
	private EvalAction _evalAction = new EvalAction();
	
	public InteractionsView() {
    super(new InteractionsDocument());
    reset();

		//add actions for indent keay
		Keymap ourMap = addKeymap("INDENT_KEYMAP", getKeymap());
		
		ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
																 (Action) _evalAction);

		setKeymap(ourMap);

  }

  private InteractionsDocument getInteractionsDocument() {
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
