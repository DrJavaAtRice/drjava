package edu.rice.cs.drjava.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.text.*;

import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.util.swing.FindReplaceMachine;
import edu.rice.cs.drjava.util.swing.ContinueCommand;

/**
 * The dialog box that handles requests for finding and replacing text.
 * @version $Id$
 */
class FindReplaceDialog extends JDialog {
  private JOptionPane _optionPane;
  private JButton _findNextButton;
  private JButton _replaceButton;
  private JButton _replaceFindButton;
  private JButton _replaceAllButton;
  private JButton _closeButton;
  private JTextField _findField = new JTextField(10);
  private JTextField _replaceField = new JTextField(10);
  private Label _message;
  private FindReplaceMachine _machine;
  private DefinitionsPane _defPane;
  private Object _previousHighlightTag;

  private static final DefaultHighlighter.DefaultHighlightPainter
    _documentHighlightPainter
      = new DefaultHighlighter.DefaultHighlightPainter(Color.lightGray);
  
  
  /** How the dialog responds to window events. */
  private WindowListener _dialogListener = new WindowListener() {
    public void windowActivated(WindowEvent ev) {}
    public void windowClosed(WindowEvent ev) {}
    public void windowClosing(WindowEvent ev) {
      _close();
    }
    public void windowDeactivated(WindowEvent ev) {}
    public void windowDeiconified(WindowEvent ev) {}
    public void windowIconified(WindowEvent ev) {}
    public void windowOpened(WindowEvent ev) {}
  };

  private Action _findNextAction = new AbstractAction("Find Next") {
    public void actionPerformed(ActionEvent e) {
      _machine.setFindWord(_findField.getText());
      _machine.setReplaceWord(_replaceField.getText());
      _message.setText("");
      
      
      int pos = _machine.findNext(CONFIRM_CONTINUE);
      if (pos >= 0) {
        _highlightFoundItem(pos - _machine.getFindWord().length(), pos);
        _replaceButton.setEnabled(true);
        _replaceFindButton.setEnabled(true);
      } 
      else {
        Toolkit.getDefaultToolkit().beep();
        _message.setText("Search text \"" + _machine.getFindWord() +
                         "\" not found.");
      }
    }
  };

  private Action _replaceAction = new AbstractAction("Replace") {  
    public void actionPerformed(ActionEvent e) {
      _machine.setFindWord(_findField.getText());
      _machine.setReplaceWord(_replaceField.getText());
      _message.setText("");
      
      // replaces the occurance at the current position
      _machine.replaceCurrent();
      _replaceButton.setEnabled(false);
      _replaceFindButton.setEnabled(false);
    }
  };



  private Action _replaceFindAction = new AbstractAction("Replace/Find Next") {
    public void actionPerformed(ActionEvent e) {
      _machine.setFindWord(_findField.getText());
      _machine.setReplaceWord(_replaceField.getText());
      _message.setText("");
      // replaces the occurance at the current position
      boolean replaced = _machine.replaceCurrent();
      int pos;
      // and finds the next word
      if (replaced) {
        pos = _machine.findNext(CONFIRM_CONTINUE);
        
        if (pos >= 0) {
          _highlightFoundItem(pos - _machine.getFindWord().length(), pos);
          _replaceButton.setEnabled(true);
          _replaceFindButton.setEnabled(true);
        } 
        else {
          _replaceButton.setEnabled(false);
          _replaceFindButton.setEnabled(false);
          Toolkit.getDefaultToolkit().beep();
          _message.setText("Search text \"" + _machine.getFindWord() + 
                           "\" not found.");
        }
      }
      else {
        _replaceButton.setEnabled(false);
        _replaceFindButton.setEnabled(false);
        Toolkit.getDefaultToolkit().beep();
        _message.setText("Replace failed.");
      }
    }
  };


  private Action _replaceAllAction = new AbstractAction("Replace All") {
    public void actionPerformed(ActionEvent e) {
      _machine.setFindWord(_findField.getText());
      _machine.setReplaceWord(_replaceField.getText());
      _message.setText("");
      int count = _machine.replaceAll(CONFIRM_CONTINUE);
      _message.setText("Replaced " + count + " occurrence" + ((count == 1) ? "" :
                                                              "s") + ".");
      _replaceButton.setEnabled(false);
      _replaceFindButton.setEnabled(false);
    }
  };


  private Action _closeAction = new AbstractAction("Close") {
    public void actionPerformed(ActionEvent e) {
      _close();
    }
  };
  
  /**
   * Constructor.
   * @param   Frame frame the overall enclosing window
   * @param   DefinitionsPane defPane the definitions pane which contains the
   * document text being searched over
   */
  public FindReplaceDialog(Frame frame, DefinitionsPane defPane) {
    super(frame);
    _defPane = defPane;
    
    addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          _close();
        }
      }
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          _close();
        }
      }
      public void keyTyped(KeyEvent e) {
      }      
    });
    
    addWindowListener(_dialogListener);
    
    setTitle("Find/Replace");
    final String msgString1 = "Find:";
    final String msgString2 = "Replace:";
    Object[] array =  {
      msgString1, _findField, msgString2, _replaceField
    };
    _findNextButton = new JButton(_findNextAction);
    _replaceButton = new JButton(_replaceAction);
    _replaceFindButton = new JButton(_replaceFindAction);
    _replaceAllButton = new JButton(_replaceAllAction);
    _closeButton = new JButton(_closeAction);
    _message = new Label();
    // set up the layout
    Box outside = Box.createVerticalBox();
    Box buttons = Box.createHorizontalBox();
    buttons.add(_findNextButton);
    buttons.add(_replaceButton);
    buttons.add(_replaceFindButton);
    _replaceButton.setEnabled(false);
    _replaceFindButton.setEnabled(false);
    buttons.add(_replaceAllButton);
    buttons.add(_closeButton);
    outside.add(new Label(msgString1));
    outside.add(_findField);
    outside.add(new Label(msgString2));
    outside.add(_replaceField);
    outside.add(buttons);
    outside.add(_message);
    setContentPane(outside);
    setDefaultCloseOperation(HIDE_ON_CLOSE);
    setModal(true);
    
    _findField.addActionListener((AbstractAction)_findNextAction);
    
    // DocumentListener that keeps track of changes in the find field.
    _findField.getDocument().addDocumentListener(new DocumentListener() {

      /**
       * If attributes in the find field have changed, gray out
       * "Replace" and "Replace and Find Next" buttons.
       * @param e the event caught by this listener
       */
      public void changedUpdate(DocumentEvent e) {
        _machine.makeCurrentOffsetStart();
        _replaceButton.setEnabled(false);
        _replaceFindButton.setEnabled(false);
      }

      /**
       * If text has been inserted into the find field, gray out
       * "Replace" and "Replace and Find Next" buttons.
       * @param e the event caught by this listener
       */
      public void insertUpdate(DocumentEvent e) {
        _machine.makeCurrentOffsetStart();
        _replaceButton.setEnabled(false);
        _replaceFindButton.setEnabled(false);
      }

      /**
       * If text has been deleted from the find field, gray out
       * "Replace" and "Replace and Find Next" buttons.
       * @param e the event caught by this listener
       */
      public void removeUpdate(DocumentEvent e) {
        _machine.makeCurrentOffsetStart();
        _replaceButton.setEnabled(false);
        _replaceFindButton.setEnabled(false);
      }
    });
        
    // Set the size and position of the dialog.
    // There are no guarantees to how accurate the size is,
    // given Swing's tendency to muck things up.
    setBounds(100, 200, 520, 300);
    setSize(520, 200);
  }
  
  public void setMachine(DefinitionsPane defPane) {
    _defPane = defPane;
    OpenDefinitionsDocument doc = defPane.getOpenDocument();
    _machine = doc.createFindReplaceMachine();
  }

  
  private void _highlightFoundItem(int from, int to) {
    try {
      _removePreviousHighlight();
      _addHighlight(from, to);
      
      // Scroll to make sure this item is visible
      Rectangle startRect = _defPane.modelToView(from);
      Rectangle endRect = _defPane.modelToView(to - 1);
      
      // Add the end rect onto the start rect to make a rectangle
      // that encompasses the entire error
      startRect.add(endRect);
      
      //System.err.println("scrll vis: " + startRect);
      
      _defPane.scrollRectToVisible(startRect);
      
    } 
    catch (BadLocationException badBadLocation) {}
  }

  /**
   * Adds an error highlight to the document.
   * @exception BadLocationException
   */
  private void _addHighlight(int from, int to) throws BadLocationException {
    _previousHighlightTag =
      _defPane.getHighlighter().addHighlight(from,
                                             to,
                                             _documentHighlightPainter);
  }

  /**
   * Removes the previous error highlight from the document after the cursor
   * has moved.
   */
  private void _removePreviousHighlight() {
    if (_previousHighlightTag != null) {
      _defPane.getHighlighter().removeHighlight(_previousHighlightTag);
      _previousHighlightTag = null;
    }
  }

 
  private void _close() {
    if (_machine.isOnMatch()) {
      _defPane.select(_machine.getCurrentOffset() - 
                      _machine.getFindWord().length(),
                      _machine.getCurrentOffset());
    }
    else {
      _defPane.setCaretPosition(_machine.getCurrentOffset());
    }
    _defPane.grabFocus();
    _defPane.getHighlighter().removeAllHighlights();
    setVisible(false);
  }  
  
  private ContinueCommand CONFIRM_CONTINUE = new ContinueCommand() {
    public boolean shouldContinue() {
      String text = "The search has reached the end of the document.\n" +
        "Continue searching from the start?";
      int rc = JOptionPane.showConfirmDialog(FindReplaceDialog.this,
          text,
          "Continue search?",
          JOptionPane.YES_NO_OPTION);

      switch (rc) {
        case JOptionPane.YES_OPTION:
          return true;
        case JOptionPane.NO_OPTION:
          return false;
        default:
          throw new RuntimeException("Invalid rc: " + rc);
      }

    }
  };
  
}

