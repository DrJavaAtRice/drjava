package  edu.rice.cs.drjava;

import  javax.swing.JOptionPane;
import  javax.swing.JDialog;
import  javax.swing.JTextField;
import  javax.swing.JButton;
import  javax.swing.Box;
import  java.awt.Frame;
import  java.awt.Label;
import  java.awt.Toolkit;
import  java.awt.event.WindowAdapter;
import  java.awt.event.WindowEvent;
import  java.awt.event.ActionListener;
import  java.awt.event.ActionEvent;
import  javax.swing.event.DocumentListener;
import  javax.swing.event.DocumentEvent;
import  javax.swing.event.CaretListener;
import  javax.swing.event.CaretEvent;
import  java.beans.PropertyChangeListener;
import  java.beans.PropertyChangeEvent;


/**
 * The dialog box that handles requests for finding and replacing text.
 * @version $Id$
 */
public class FindReplaceDialog extends JDialog {
  private JOptionPane _optionPane;
  private String _fWord = null;
  private String _rWord = null;
  private DefinitionsPane _defPane;
  private int _currentPosition;
  private JButton _findNextButton;
  private JButton _replaceButton;
  private JButton _replaceFindButton;
  private JButton _replaceAllButton;
  private JButton _closeButton;
  private Label _message;

  /**
   * Get the word being searched for.
   * @return the value of the "find" field
   */
  public String getFindWord() {
    return  _fWord;
  }

  /**
   * Get the word replacing the word being searched for.
   * @return the value of the "replace" field
   */
  public String getReplaceWord() {
    return  _rWord;
  }

  /**
   * Constructor.
   * @param   Frame frame the overall enclosing window
   * @param   DefinitionsPane defPane the definitions pane which contains the
   * document text being searched over
   */
  public FindReplaceDialog(Frame frame, DefinitionsPane defPane) {
    super(frame);
    _defPane = defPane;
    _currentPosition = _defPane.getCaretPosition();
    setTitle("Find/Replace");
    final String msgString1 = "Find:";
    final String msgString2 = "Replace:";
    final JTextField findField = new JTextField(10);
    final JTextField replaceField = new JTextField(10);
    Object[] array =  {
      msgString1, findField, msgString2, replaceField
    };
    _findNextButton = new JButton("Find Next");
    _replaceButton = new JButton("Replace");
    _replaceFindButton = new JButton("Replace and Find Next");
    _replaceAllButton = new JButton("Replace All");
    _closeButton = new JButton("Close");
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
    outside.add(findField);
    outside.add(new Label(msgString2));
    outside.add(replaceField);
    outside.add(buttons);
    outside.add(_message);
    setContentPane(outside);
    setDefaultCloseOperation(HIDE_ON_CLOSE);
    setModal(false);
    // DocumentListener that keeps track of changes in the find field.
    findField.getDocument().addDocumentListener(new DocumentListener() {

      /**
       * If attributes in the find field have changed, gray out
       * "Replace" and "Replace and Find Next" buttons.
       * @param e the event caught by this listener
       */
      public void changedUpdate(DocumentEvent e) {
        _replaceButton.setEnabled(false);
        _replaceFindButton.setEnabled(false);
      }

      /**
       * If text has been inserted into the find field, gray out
       * "Replace" and "Replace and Find Next" buttons.
       * @param e the event caught by this listener
       */
      public void insertUpdate(DocumentEvent e) {
        _replaceButton.setEnabled(false);
        _replaceFindButton.setEnabled(false);
      }

      /**
       * If text has been deleted from the find field, gray out
       * "Replace" and "Replace and Find Next" buttons.
       * @param e the event caught by this listener
       */
      public void removeUpdate(DocumentEvent e) {
        _replaceButton.setEnabled(false);
        _replaceFindButton.setEnabled(false);
      }
    });
    
    // DocumentListener for the actual document
    _defPane.getDocument().addDocumentListener(new DocumentListener() {

      /**
       * If attributes have changed in the document, gray out
       * "Replace" and "Replace and Find Next" buttons.
       * @param e the event caught by this listener
       */
      public void changedUpdate(DocumentEvent e) {
        _replaceButton.setEnabled(false);
        _replaceFindButton.setEnabled(false);
      }

      /**
       * If text has been deleted from the document, gray out
       * "Replace" and "Replace and Find Next" buttons.
       * @param e the event caught by this listener
       */
      public void insertUpdate(DocumentEvent e) {
        _replaceButton.setEnabled(false);
        _replaceFindButton.setEnabled(false);
      }

      /**
       * If text has been inserted into the document, gray out
       * "Replace" and "Replace and Find Next" buttons.
       * @param e the event caught by this listener
       */
       public void removeUpdate(DocumentEvent e) {
        _replaceButton.setEnabled(false);
        _replaceFindButton.setEnabled(false);
      }
    });

    // Add a listener to see if there's a change in position in the document.
    _defPane.addCaretListener(new CaretListener() {

      /**
       * If the caret has moved in the document, gray out
       * "Replace" and "Replace and Find Next" buttons.
       * @param e the event caught by this listener
       */
       public void caretUpdate(CaretEvent e) {
        _replaceButton.setEnabled(false);
        _replaceFindButton.setEnabled(false);
      }
    });
    
    /** 
     * We want to enable the "Replace" and "Replace and Find Next"
     * fields iff we've found an instance of the find field in
     * the document.
     */
    _findNextButton.addActionListener(new ActionListener() {

      /**
       * What happens when the user presses the "Find Next" button.
       * @param e
       */
      public void actionPerformed(ActionEvent e) {
        _fWord = findField.getText();
        _rWord = replaceField.getText();
        _message.setText("");
        //System.err.println(e.getActionCommand());
        boolean found = _defPane.findNextText(_fWord);
        if (found) {
          _replaceButton.setEnabled(true);
          _replaceFindButton.setEnabled(true);
        } 
        else {
          Toolkit.getDefaultToolkit().beep();
          _message.setText("Search text \"" + _fWord + "\" not found.");
        }
      }
    });
    
    _replaceButton.addActionListener(new ActionListener() {

      /**
       * What happens when the user presses the "Replace" button.
       * @param e
       */
      public void actionPerformed(ActionEvent e) {
        _fWord = findField.getText();
        _rWord = replaceField.getText();
        _message.setText("");
        // replaces the occurance at the current position
        _defPane.replaceText(_fWord, _rWord);
        _replaceButton.setEnabled(false);
        _replaceFindButton.setEnabled(false);
      }
    });
    _replaceFindButton.addActionListener(new ActionListener() {

      /**
       * What happens when the user presses the "Replace and Find Next" button.
       * @param e
       */
      public void actionPerformed(ActionEvent e) {
        _fWord = findField.getText();
        _rWord = replaceField.getText();
        _message.setText("");
        // replaces the occurance at the current position
        boolean found = _defPane.replaceText(_fWord, _rWord);
        // and finds the next word
        if (found)
          found = _defPane.findNextText(_fWord);
        if (found) {
          _replaceButton.setEnabled(true);
          _replaceFindButton.setEnabled(true);
        } 
        else {
          _replaceButton.setEnabled(false);
          _replaceFindButton.setEnabled(false);
          Toolkit.getDefaultToolkit().beep();
          _message.setText("Search text \"" + _fWord + "\" not found.");
        }
      }
    });
    _replaceAllButton.addActionListener(new ActionListener() {

      /**
       * What happens when the user presses the "Replace All" button.
       * @param e
       */
      public void actionPerformed(ActionEvent e) {
        _fWord = findField.getText();
        _rWord = replaceField.getText();
        _message.setText("");
        int count = _defPane.replaceAllText(_fWord, _rWord);
        _message.setText("Replaced " + count + " occurrence" + ((count == 1) ? "" :
            "s") + ".");
        _replaceButton.setEnabled(false);
        _replaceFindButton.setEnabled(false);
      }
    });
    
    _closeButton.addActionListener(new ActionListener() {

      /**
       * Closes the Find/Replace dialog.
       * @param e
       */
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    
    // Set the size and position of the dialog.
    // There are no guarantees to how accurate the size is,
    // given Swing's tendency to muck things up.
    setBounds(100, 200, 520, 300);
    setSize(520, 300);
  }
}



