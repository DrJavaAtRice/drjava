/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 *
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.plugins.eclipse.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.*;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.events.*;
import org.eclipse.swt.SWT;

import edu.rice.cs.drjava.plugins.eclipse.EclipsePlugin;
import edu.rice.cs.drjava.plugins.eclipse.DrJavaConstants;
import edu.rice.cs.drjava.plugins.eclipse.repl.EclipseInteractionsModel;

import edu.rice.cs.drjava.model.repl.InteractionsListener;
import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.repl.InputListener;
import edu.rice.cs.drjava.model.repl.ConsoleDocument;
import edu.rice.cs.util.text.SWTDocumentAdapter;
import edu.rice.cs.util.text.SWTDocumentAdapter.SWTStyle;
import edu.rice.cs.util.text.DocumentAdapter;
import edu.rice.cs.util.StringOps;

import java.util.Vector;

/**
 * This class installs listeners and actions between an InteractionsDocument
 * in the model and an InteractionsPane in the view.
 *
 * We may want to refactor this class into a different package.
 *
 * @version $Id$
 */
public class InteractionsController {

  // TODO: What to do with unexpected exceptions?

  /** InteractionsModel to handle the interpreter */
  protected EclipseInteractionsModel _model;

  /** Adapter for an SWT document */
  protected SWTDocumentAdapter _adapter;

  /** Document from the model */
  protected InteractionsDocument _doc;

  /** Pane from the SWT view */
  protected InteractionsView _view;

  /** The last input entered to System.in. */
  private String _input;

  // Colors created by the controller
  protected Color _colorRed;
  protected Color _colorDarkRed;
  protected Color _colorDarkGreen;
  protected Color _colorDarkBlue;
  protected Color _colorYellow;
  protected Color _colorPurple;

  /** Whether the Interactions Pane is currently enabled. */
  protected boolean _enabled;

  protected static final String INPUT_ENTERED_NAME = "Input Entered";
  protected static final String INSERT_NEWLINE_NAME = "Insert Newline";

  /** Input validator that always accepts input. */
  private IInputValidator _inputValidator = new IInputValidator() {
    public String isValid(String newText) {
      return null;
    }
  };

  /**
   * Listens for input requests from System.in, displaying an input box as needed.
   */
  protected InputListener _inputListener = new InputListener() {
    public String getConsoleInput() {
//      String msg = "System.in is not yet supported!" + System.getProperty("line.separator");
//      _model.getDocument().insertBeforeLastPrompt(msg, InteractionsDocument.ERROR_STYLE);
//      return "\n";
      Display d = _view.getTextPane().getDisplay();
      d.syncExec(new Runnable() {
        public void run() {
          try {
            InputDialog input = new InputDialog(_view.getSite().getShell(), "System.in",
                                                "Please enter a line of input to System.in.",
                                                "", _inputValidator);
            input.open();
            _input = input.getValue();
          }
          catch (Throwable t) {
//            t.printStackTrace();
            _input = StringOps.getStackTrace(t);
          }
        }
      });
      _input += "\n";
      _doc.insertBeforeLastPrompt(_input, ConsoleDocument.SYSTEM_IN_STYLE);
      return _input;
    }
  };

  // ---- Preferences ----

  /** Listens to changes to preferences. */
  protected IPropertyChangeListener _preferenceListener;

  /** Whether to prompt before resetting Interactions. */
  protected boolean _promptToReset;

  /** Whether to prompt if Interactions are reset unexpectedly. */
  protected boolean _promptIfExited;

  /**
   * Glue together the given model and view.
   * @param model EclipseInteractionsModel to handle the interpreter
   * @param adapter DocumentAdapter that the document uses
   * @param view InteractionsView in the view
   */
  public InteractionsController(EclipseInteractionsModel model,
                                SWTDocumentAdapter adapter,
                                InteractionsView view) {
    _model = model;
    _adapter = adapter;
    _doc = model.getDocument();
    _view = view;
    _enabled = true;

    // Initialize preferences
    IPreferenceStore store = EclipsePlugin.getDefault().getPreferenceStore();
    _preferenceListener = new PrefChangeListener();
    store.addPropertyChangeListener(_preferenceListener);
    JFaceResources.getFontRegistry().addListener(_preferenceListener);
    _updatePreferences();

    // Put the caret at the end
    _view.getTextPane().setCaretOffset(_doc.getDocLength());

    _addDocumentStyles();
    _setupModel();
    _setupView();
  }

  /**
   * Cleans up any resources this controller created, as well as the model.
   */
  public void dispose() {
    _model.dispose();
    _colorRed.dispose();
    _colorDarkRed.dispose();
    _colorDarkGreen.dispose();
    _colorDarkBlue.dispose();
    _colorYellow.dispose();

    // Remove preference listener
    IPreferenceStore store = EclipsePlugin.getDefault().getPreferenceStore();
    store.removePropertyChangeListener(_preferenceListener);
    JFaceResources.getFontRegistry().removeListener(_preferenceListener);
  }

  /**
   * Reads user-defined preferences and sets up a PropertyChangeListener
   * to react to changes.
   */
  private void _updatePreferences() {
    IPreferenceStore store = EclipsePlugin.getDefault().getPreferenceStore();

    // Notifications
    _promptToReset = store.getBoolean(DrJavaConstants.INTERACTIONS_RESET_PROMPT);
    _promptIfExited = store.getBoolean(DrJavaConstants.INTERACTIONS_EXIT_PROMPT);

    // Set the interpreter's accessibility
    _model.setPrivateAccessible(store.getBoolean(DrJavaConstants.ALLOW_PRIVATE_ACCESS));

    // History size
    _doc.getHistory().setMaxSize(store.getInt(DrJavaConstants.HISTORY_MAX_SIZE));

    // Update the font
    _view.updateFont();

    // Set the new interpreter JVM arguments
    String jvmArgs = store.getString(DrJavaConstants.JVM_ARGS);
    if (jvmArgs.equals("") || !called) {
      _model.setOptionArgs(jvmArgs);
    }
    else {
      String confirmMessage =
        "Specifying the command-line arguments to the Interactions JVM is an\n" +
        "advanced option, and incorrect arguments may cause the Interactions\n" +
        "Pane to stop responding. Are you sure you want to set this option?\n" +
        "(You must reset the Interactions Pane before changes will take effect.)";
      if (_view.showConfirmDialog("Setting JVM Arguments", confirmMessage)) {
        _model.setOptionArgs(jvmArgs);
      }
      else {
        store.setValue(DrJavaConstants.JVM_ARGS, "");
      }
    }
    called = true;
  }
  boolean called = false;

  /**
   * Accessor method for the InteractionsModel.
   */
  public EclipseInteractionsModel getInteractionsModel() {
    return _model;
  }

  /**
   * Accessor method for the DocumentAdapter.
   */
  public DocumentAdapter getDocumentAdapter() {
    return _adapter;
  }

  /**
   * Accessor method for the InteractionsDocument.
   */
  public InteractionsDocument getDocument() {
    return _doc;
  }

  /**
   * Accessor method for the InteractionsPane.
   */
  public InteractionsView getView() {
    return _view;
  }

  /**
   * Adds AttributeSets as named styles to the document adapter.
   */
  protected void _addDocumentStyles() {
    Display display = _view.getTextPane().getDisplay();
    _colorRed = new Color(display, 255, 0, 0);
    _colorDarkRed = new Color(display, 178, 0, 0);
    _colorDarkGreen = new Color(display, 0, 124, 0);
    _colorDarkBlue = new Color(display, 0, 0, 178);
    _colorYellow = new Color(display, 255, 255, 0);
    _colorPurple = new Color(display, 124, 0, 124);

    // System.out
    SWTStyle out = new SWTStyle(_colorDarkGreen, 0);
    _adapter.addDocStyle(InteractionsDocument.SYSTEM_OUT_STYLE, out);

    // System.err
    SWTStyle err = new SWTStyle(_colorRed, 0);
    _adapter.addDocStyle(InteractionsDocument.SYSTEM_ERR_STYLE, err);

    // System.in
    SWTStyle in = new SWTStyle(_colorPurple, 0);
    _adapter.addDocStyle(InteractionsDocument.SYSTEM_IN_STYLE, in);

    // Error
    SWTStyle error = new SWTStyle(_colorDarkRed, SWT.BOLD);
    _adapter.addDocStyle(InteractionsDocument.ERROR_STYLE, error);

    // Debug
    SWTStyle debug = new SWTStyle(_colorDarkBlue, SWT.BOLD);
    _adapter.addDocStyle(InteractionsDocument.DEBUGGER_STYLE, debug);
  }

  /**
   * Adds listeners to the model.
   */
  protected void _setupModel() {
    _adapter.addModifyListener(new DocumentUpdateListener());
    _doc.setBeep(_view.getBeep());
    _model.addInteractionsListener(new EclipseInteractionsListener());
    _model.setInputListener(_inputListener);
  }

  /**
   * Listener to ensure that the caret always stays on or after the
   * prompt, so that output is always scrolled to the bottom.
   */
  class DocumentUpdateListener implements ModifyListener {
    public void modifyText(ModifyEvent e) {
      // Update the caret position
      StyledText pane = _view.getTextPane();
      int caretPos = pane.getCaretOffset();
      int promptPos = _doc.getPromptPos();
      int docLength = _doc.getDocLength();

      if (_doc.inProgress()) {
        // Scroll to the end of the document, since output has been
        // inserted after the prompt.
        moveToEnd();
      }
      else {
        // Only update caret if it has fallen behind the prompt.
        // (And be careful not to move it during a reset, when the
        //  prompt pos is temporarily far greater than the length.)
        if ((caretPos < promptPos) && (promptPos <= docLength)) {
          moveToPrompt();
        }
        else {
          pane.showSelection();
        }
      }
    }
  }

  /** Enables the Interactions Pane. */
  protected void _enableInteractionsPane() {
    _enabled = true;
    _view.setBusyCursorShown(false);
    _view.setEditable(true);
  }

  /** Disables the Interactions Pane. */
  protected void _disableInteractionsPane() {
    _enabled = false;
    _view.setBusyCursorShown(true);
    _view.setEditable(false);
  }

  /**
   * Listens and reacts to interactions-related events.
   */
  class EclipseInteractionsListener implements InteractionsListener {
    public void interactionStarted() {
      _disableInteractionsPane();
    }

    public void interactionEnded() {
      _enableInteractionsPane();
      moveToPrompt();
    }

    public void interactionErrorOccurred(final int offset, final int length) {
      _view.getTextPane().getDisplay().asyncExec(new Runnable() {
        public void run() {
          _adapter.highlightRange(offset, length, _colorYellow);
        }
      });
    }

    public void interpreterResetting() {
      _disableInteractionsPane();
    }

    public void interpreterReady() {
      _enableInteractionsPane();
      moveToPrompt();
    }

    public void interpreterExited(int status) {
      if (_promptIfExited) {
        String title = "Interactions terminated by System.exit(" + status + ")";
        String msg = "The interactions window was terminated by a call " +
          "to System.exit(" + status + ").\n" +
          "The interactions window will now be restarted.";
        _view.showInfoDialog(title, msg);
      }
    }

    public void interpreterChanged(boolean inProgress) {
      if (inProgress) {
        _disableInteractionsPane();
      }
      else {
        _enableInteractionsPane();
      }
    }

    public void interpreterResetFailed(Throwable t) {
      String title = "Interactions Could Not Reset";
      String msg = "The interactions window could not be reset:\n" +
        t.toString();
      _view.showInfoDialog(title, msg);
      interpreterReady();
    }

    public void interactionIncomplete() {
//      newLineAction();
      StyledText pane = _view.getTextPane();
      int offs = pane.getCaretOffset();
      pane.replaceTextRange(offs, 0, "\n");
      pane.setCaretOffset(offs + 1);
    }
  }

  /**
   * Assigns key bindings to the view.
   */
  protected void _setupView() {
/*    _view.getTextPane().getDisplay().syncExec(new Runnable() {
      public void run() {
        int b1 = ((int) '\t') | SWT.SHIFT;
        _view.showInfoDialog("binding for " + b1, String.valueOf(_view.getTextPane().getKeyBinding(b1)));
      }
    });*/
    _view.getTextPane().addVerifyKeyListener(new KeyUpdateListener());
//    _view.getTextPane().setKeyBinding(((int) '\t') | SWT.SHIFT, SWT.NULL);

    // Set up menu
    _setupMenu();
  }

  /**
   * Adds actions to the toolbar menu.
   */
  protected void _setupMenu() {
    Action resetInteractionsAction = new Action() {
      public void run() {
        String title = "Confirm Reset Interactions";
        String message = "Are you sure you want to reset the Interactions Pane?";
        if (!_promptToReset || _view.showConfirmDialog(title, message)) {
          _model.resetInterpreter();
        }
      }
    };
    resetInteractionsAction.setText("Reset Interactions");
    resetInteractionsAction.setToolTipText("Resets the Interactions Pane");
    _view.addMenuItem(resetInteractionsAction);

    Action showClasspathAction = new Action() {
      public void run() {
        String title = "Interpreter Classpath";
        StringBuffer cpBuf = new StringBuffer();
        Vector<String> classpathElements = _model.getClasspath();
        for(int i = 0; i < classpathElements.size(); i++) {
          cpBuf.append(classpathElements.get(i));
          if (i + 1 < classpathElements.size()) {
            cpBuf.append("\n");
          }
        }
        _view.showInfoDialog(title, cpBuf.toString());
      }
    };
    showClasspathAction.setText("Show Interpreter Classpath");
    showClasspathAction.setToolTipText("Shows the classpath used in the interactions pane.");
    _view.addMenuItem(showClasspathAction);
  }

  /**
   * Listener to perform the correct action when a key is pressed.
   */
  class KeyUpdateListener implements VerifyKeyListener {
    public void verifyKey(VerifyEvent event) {
      //StyledText pane = _view.getTextPane();
      //System.out.println("event consumer: keycode: " + event.keyCode + ", char: " + event.character);

      // -- Branch to an action on certain keystrokes --
      //  (needs to be refactored for better OO code)

      // Keys have no action if Interactions pane is not enabled.
      if (!_enabled) return;

      // enter
      if (event.keyCode == 13 && event.stateMask == 0) {
        event.doit = evalAction();
      }
      // shift + enter
      else if (event.keyCode == 13 && (event.stateMask & SWT.SHIFT) == 1) {
        event.doit = newLineAction();
      }
      // up
      else if (event.keyCode == SWT.ARROW_UP) {
        event.doit = historyPrevAction();
      }
      // down
      else if (event.keyCode == SWT.ARROW_DOWN) {
        event.doit = historyNextAction();
      }
      // left
      else if (event.keyCode == SWT.ARROW_LEFT) {
        event.doit = moveLeftAction();
      }
      // right
      else if (event.keyCode == SWT.ARROW_RIGHT) {
        event.doit = moveRightAction();
      }
      // shift+home
      else if (event.keyCode == SWT.HOME && (event.stateMask & SWT.SHIFT) == 1) {
        event.doit = selectToPromptPosAction();
      }
      // home
      else if (event.keyCode == SWT.HOME) {
        event.doit = gotoPromptPosAction();
      }
      // tab
      else if (event.keyCode == '\t' && event.stateMask == 0) {
        event.doit = historyReverseSearchAction();
      }
      // shift+tab
      else if (event.keyCode == '\t' && (event.stateMask & SWT.SHIFT) == 1) {
        event.doit = historyForwardSearchAction();
      }
      // shortcut for clear command?  (ctrl+B is build project)
    }
  }

  /**
   * Submits the text in the view to the model, and appends the
   * result to the view.
   */
  boolean evalAction() {
//    _disableInteractionsPane();
    _model.interpretCurrentInteraction();
    return false;
  }

  /** Inserts a new line at the caret position. */
  boolean newLineAction() {
    StyledText pane = _view.getTextPane();
    pane.replaceTextRange(pane.getCaretOffset(), 0, "\n");
    return false;
  }

  /** Recalls the previous command from the history. */
  boolean historyPrevAction() {
    _doc.recallPreviousInteractionInHistory();
    moveToEnd();
    return false;
  }

  /** Recalls the next command from the history. */
  boolean historyNextAction() {
    _doc.recallNextInteractionInHistory();
    moveToEnd();
    return false;
  }

  /** Searches backwards through the history. */
  boolean historyReverseSearchAction() {
    _doc.reverseSearchInteractionsInHistory();
    moveToEnd();
    return false;
  }

  /** Searches forwards through the history. */
  boolean historyForwardSearchAction() {
    _doc.forwardSearchInteractionsInHistory();
    moveToEnd();
    return false;
  }

  /** Removes all text after the prompt. */
  boolean clearCurrentAction() {
    _doc.clearCurrentInteraction();
    return false;
  }

  /** Moves the caret to the prompt. */
  boolean gotoPromptPosAction() {
    moveToPrompt();
    return false;
  }

  /** Selects all text between the caret and the prompt */
  boolean selectToPromptPosAction() {
    // Selects the text between the old pos and the prompt
    StyledText pane = _view.getTextPane();
    int start = _doc.getPromptPos();
    int end = pane.getCaretOffset();
    if (end < start) {
      int t = start;
      start = end;
      end = t;
    }

    pane.setSelection(start, end);
    return false;
  }

  /** Moves the caret left or wraps around. */
  boolean moveLeftAction() {
    int position = _view.getTextPane().getCaretOffset();
    if (position < _doc.getPromptPos()) {
      moveToPrompt();
      return false;
    }
    else if (position == _doc.getPromptPos()) {
      // Wrap around to the end
      moveToEnd();
      return false;
    }
    else { // position > _doc.getPromptPos()
      //_view.getTextPane().setCaretOffset(position - 1);
      return true;
    }
  }

  /** Moves the caret right or wraps around. */
  boolean moveRightAction() {
    int position = _view.getTextPane().getCaretOffset();
    if (position < _doc.getPromptPos()) {
      moveToEnd();
      return false;
    }
    else if (position >= _doc.getDocLength()) {
      // Wrap around to the start
      moveToPrompt();
      return false;
    }
    else { // position between prompt and end
      //_view.getTextPane().setCaretOffset(position + 1);
      return true;
    }
  }

  /** Moves the pane's caret to the end of the document. */
  void moveToEnd() {
    final StyledText pane = _view.getTextPane();
    pane.getDisplay().syncExec(new Runnable() {
      public void run() {
        pane.setCaretOffset(_doc.getDocLength());
        pane.showSelection();
      }
    });
  }

  /** Moves the pane's caret to the document's prompt. */
  void moveToPrompt() {
    final StyledText pane = _view.getTextPane();
    pane.getDisplay().syncExec(new Runnable() {
      public void run() {
        pane.setCaretOffset(_doc.getPromptPos());
        pane.showSelection();
      }
    });
  }

  /**
   * Class to listen to preference changes and update the
   * controller accordingly.
   */
  class PrefChangeListener implements IPropertyChangeListener {
    public void propertyChange(PropertyChangeEvent event) {
      _updatePreferences();
    }
  }
}
