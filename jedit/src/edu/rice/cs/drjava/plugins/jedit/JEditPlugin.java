/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.plugins.jedit;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.msg.*;

import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.plugins.jedit.repl.*;
import edu.rice.cs.drjava.ui.*;
import edu.rice.cs.util.text.*;
import edu.rice.cs.util.swing.BorderlessScrollPane;
import edu.rice.cs.util.swing.ScrollableDialog;

public class JEditPlugin extends EBPlugin {
  /**
   * The interactions controller.
   * Glues the InteractionsModel to a swing InteractionsPane.
   */
  private InteractionsController _controller;

  /**
   * The pane that the user interacts in.
   */
  private InteractionsPane _pane;

  /**
   * The interactions model.
   */
  private JEditInteractionsModel _model;

  /**
   * The document adapter.
   */
  private SwingDocumentAdapter _doc;

  /**
   * Popup menu to show in the interactions pane.
   */
  private JPopupMenu _popup;

  /**
   * The jEdit View.
   */
  private View _view;

  /**
   * Action that resets the interactions pane.
   */
  private Action _resetInteractionsAction = new AbstractAction("Reset Interactions Pane") {
    public void actionPerformed(ActionEvent e) {
      resetInteractions();
    }
  };

  /**
   * Action that shows the current interpreter classpath.
   */
  private Action _showClasspathAction = new AbstractAction("Show Interpreter Classpath") {
    public void actionPerformed(ActionEvent e) {
      showClasspath();
    }
  };

  /**
   * Unfortunate hack to allow instance method calls from static context.
   */
  private static JEditPlugin _default;

  /**
   * Sets the "default" instance of this class.
   * Hopefully this only gets called once...
   */
  public JEditPlugin() {
    _default = this;
  }

  /**
   * Gets the "default" instance of this plugin.
   */
  public static JEditPlugin getDefault() {
    return _default;
  }

  /**
   * Sets up this plugin for use.
   */
  public void start() {
    _popup = new JPopupMenu();
    _popup.add(_resetInteractionsAction);
    _popup.add(_showClasspathAction);
  }

  /**
   * Cleans up this plugin when it's finished.
   */
  public void stop() {
    if (_model != null) {
      _model.dispose();
    }
  }

  /**
   * Gets console input for System.in.
   */
  public String getConsoleInput() {
    return _controller.getInputListener().getConsoleInput();
  }

  /**
   * @return a new InteractionsPane for the plugin
   */
  public JComponent newPane(View view) {
    _doc = new SwingDocumentAdapter();
    if (_model != null) {
      _model.dispose();
    }
    _model = new JEditInteractionsModel(_doc);
    _controller = new InteractionsController(_model, _doc);
    _pane = _controller.getPane();
    _model.addInteractionsListener(new JEditInteractionsListener());
    _view = view;
    _pane.addMouseListener(new RightClickMouseAdapter() {
      protected void _popupAction(MouseEvent e) {
        _popup.show(e.getComponent(), e.getX(), e.getY());
      }
    });
    return new BorderlessScrollPane(_pane);
  }

  /**
   * Resets the interactions pane.
   */
  public void resetInteractions() {
    if (!_model.getDocument().inProgress()) {
      String msg = "Are you sure you want to reset the Interactions Pane?";
      String title = "Confirm Reset Interactions";
      int result = JOptionPane.showConfirmDialog(_view, msg, title, JOptionPane.YES_NO_OPTION);
      if (result == JOptionPane.YES_OPTION) {
        _model.resetInterpreter();
      }
    }
  }

  /**
   * Shows the classpath of the interpreter.
   */
  public void showClasspath() {
    String classpath = "";
    Vector<String> classpathElements = _model.getClasspath();
    for(int i = 0; i < classpathElements.size(); i++) {
      classpath += classpathElements.elementAt(i);
      if (i + 1 < classpathElements.size()) {
        classpath += "\n";
      }
    }

    new ScrollableDialog(_view, "Interactions Classpath",
                         "Current Interpreter Classpath", classpath).show();
  }

  /**
   * Handles a message from the EditBus.
   * @param msg the message
   */
  public void handleMessage(EBMessage msg) {
    if (msg instanceof BufferUpdate) {
      BufferUpdate bu = (BufferUpdate)msg;
      Object w = bu.getWhat();
      if (w == BufferUpdate.CREATED || w == BufferUpdate.LOADED || w == BufferUpdate.SAVED) {
        _model.addToClassPath(((BufferUpdate)msg).getBuffer());
      }
    }
  }

  /**
   * Ensures that the interactions pane is not editable during an interaction.
   */
  protected void _disableInteractionsPane() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        _pane.setEditable(false);
        _pane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      }
    });
  }

  /**
   * Ensures that the interactions pane is editable after an interaction completes.
   */
  protected void _enableInteractionsPane() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        _pane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        _pane.setEditable(true);
        _pane.setCaretPosition(_doc.getLength());
        if (_pane.hasFocus()) {
          _pane.getCaret().setVisible(true);
        }
      }
    });
  }

  /**
   * Listens and reacts to interactions-related events.
   */
  class JEditInteractionsListener implements InteractionsListener {
    public void interactionStarted() {
      _disableInteractionsPane();
    }

    public void interactionEnded() {
      _enableInteractionsPane();
    }

    public void interactionErrorOccurred(final int offset, final int length) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          _pane.highlightError(offset, length);
        }
      });
    }

    public void interpreterResetting() {
      _disableInteractionsPane();
    }

    public void interpreterReady() {
      _enableInteractionsPane();
    }

    public void interpreterExited(final int status) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          String msg = "The interactions window was terminated by a call " +
            "to System.exit(" + status + ").\n" +
            "The interactions window will now be restarted.";
          String title = "Interactions terminated by System.exit(" + status + ")";
          JOptionPane.showMessageDialog(_view, msg, title, JOptionPane.INFORMATION_MESSAGE);
        }
      });
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
      String msg = "The interactions window could not be reset:\n" + t;
      JOptionPane.showMessageDialog(_view, msg, title, JOptionPane.INFORMATION_MESSAGE);
      interpreterReady();
    }

    public void interactionIncomplete() {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          int caretPos = _pane.getCaretPosition();
          _controller.getConsoleDoc().insertNewLine(caretPos);
        }
      });
    }
  }
}