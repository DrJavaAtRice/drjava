package edu.rice.cs.drjava.ui;

import javax.swing.JTextPane;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.repl.*;

/**
 * The view component for repl interaction.
 *
 * @version $Id$
 */
public class InteractionsPane extends JTextPane {
  private GlobalModel _model;
  private AbstractAction _evalAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      _model.interpretCurrentInteraction();
    }
  };
  
  private Runnable BEEP = new Runnable() {
    public void run() {
        Toolkit.getDefaultToolkit().beep();      
    }
  };
  
  private AbstractAction _historyPrevAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      _model.recallPreviousInteractionInHistory(BEEP);
    }
  };

  private AbstractAction _historyNextAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      _model.recallNextInteractionInHistory(BEEP);
    }
  };

  public InteractionsPane(GlobalModel model) {
    super(model.getInteractionsDocument());
    _model = model;
    
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

}



