package edu.rice.cs.drjava.plugins.eclipse.views;

// It would be good to eliminate these if possible...
import javax.swing.text.*;
import javax.swing.text.BadLocationException;

import org.eclipse.ui.part.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.SWT;

import edu.rice.cs.drjava.model.repl.SimpleInteractionsDocument;
import edu.rice.cs.util.text.SWTDocumentAdapter;

/**
 * This class is the main view component for the Interactions Pane
 * Eclipse plugin.
 * @version $Id$
 */
public class InteractionsView extends ViewPart {
  /**
   * Glue code between the model and view.
   */
  protected InteractionsController _controller;
  
  /**
   * The widget displaying the text.  Equivalent of a JTextPane.
   */
  protected StyledText _styledText;
  
  /**
   * Constructor.
   */
  public InteractionsView() {
  }
  
  /**
   * Accessor method for the text widget.
   */
  public StyledText getTextPane() {
    return _styledText;
  }
  
  /**
   * Returns a command that creates a beep when run.
   */
  public Runnable getBeep() {
    return new Runnable() {
      public void run() {
        // TO DO: how do you generate a beep in eclipse?
        System.out.println("BEEP!");
      }
    };
  }
  
  /**
   * Callback method that creates and initializes the view.
   */
  public void createPartControl(Composite parent) {
    // NOTE: Do not let anything instantiate the DrJava config framework here...
    _styledText = new StyledText(parent, SWT.WRAP | SWT.V_SCROLL);
    SWTDocumentAdapter adapter = new SWTDocumentAdapter(_styledText);
    SimpleInteractionsDocument doc = new SimpleInteractionsDocument(adapter);
    _controller = new InteractionsController(adapter, doc, this);
  }
  
  /**
   * Returns any text that is after the current prompt.
   */
  public String getCurrentInteraction(int promptPos) {
    int length = _styledText.getText().length();
    return _styledText.getText(promptPos, length - 1);
  }
  
  /**
   * Sets whether the StyledText widget is editable.
   * @param editable whether the widget should be editable
   */
  public void setEditable(boolean editable) {
    _styledText.setEditable(editable);
  }
  
  /**
   * Gives the focus to this component.
   */
  public void setFocus() {
    _styledText.setFocus();
  }
}