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

package edu.rice.cs.drjava.plugins.jedit;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.msg.*;

import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.plugins.jedit.repl.*;
import edu.rice.cs.drjava.ui.*;
import edu.rice.cs.util.text.*;

public class JEditPlugin extends EBPlugin {
  /**
   * The interactions controller.
   * Glues the InteractionsModel to a swing InteractionsPane.
   */
  private static InteractionsController _controller;

  /**
   * The pane that the user interacts in.
   */
  private static InteractionsPane _pane;

  /**
   * The interactions model.
   */
  private static JEditInteractionsModel _model;

  /**
   * The document adapter.
   */
  private static SwingDocumentAdapter _doc;

  /**
   * Popup menu to show in the interactions pane.
   */
  private static JPopupMenu _popup;

  /**
   * The jEdit View.
   */
  private static View _view;

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
    _model = null;
    _popup = new JPopupMenu();
    _popup.add(new AbstractAction("Reset Interactions Pane") {
      public void actionPerformed(ActionEvent e) {
        resetInteractions();
      }
    });
    _popup.add(new AbstractAction("Show Interpreter Classpath") {
      public void actionPerformed(ActionEvent e) {
        showClasspath();
      }
    });
  }

  /**
   * Cleans up this plugin when it's finished.
   */
  public void stop() {
    if (_model != null) {
      _model.dispose();
    }
  }

  public static String getConsoleInput() {
    return _controller.getInputListener().getConsoleInput();
  }

  /**
   * @return a new InteractionsPane for the plugin
   */
  public static JComponent newPane(View view) {
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
  public static void resetInteractions() {
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
  public static void showClasspath() {
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
  protected static void _disableInteractionsPane() {
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
  protected static void _enableInteractionsPane() {
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
  static class JEditInteractionsListener implements InteractionsListener {
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
      JOptionPane.showMessageDialog(_view, title, msg, JOptionPane.INFORMATION_MESSAGE);
      interpreterReady();
    }
  }
}