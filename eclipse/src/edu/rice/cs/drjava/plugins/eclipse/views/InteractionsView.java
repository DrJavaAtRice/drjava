/*BEGIN_COPYRIGHT_BLOCK*

DrJava Eclipse Plug-in BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of DrJava, the JavaPLT group, Rice University, nor the names of software 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.plugins.eclipse.views;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import edu.rice.cs.drjava.plugins.eclipse.EclipsePlugin;
import edu.rice.cs.drjava.plugins.eclipse.DrJavaConstants;
import edu.rice.cs.drjava.plugins.eclipse.repl.EclipseInteractionsModel;
import edu.rice.cs.drjava.plugins.eclipse.util.text.SWTDocumentAdapter;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/**
 * This class is the main view component for the Interactions Pane
 * Eclipse plugin.
 * @version $Id$
 */
public class InteractionsView extends ViewPart {
  
  /**
   * Glue code between the model and view.
   * We have to have a reference to it so we can dispose it.
   */
  protected InteractionsController _controller;

  /**
   * The widget displaying the text.  Equivalent of a JTextPane.
   */
  protected StyledText _styledText;

  /**
   * An arrow cursor to display when the pane is not busy.
   */
  protected Cursor _arrowCursor;

  /**
   * An hourglass cursor to display when the pane is busy.
   */
  protected Cursor _waitCursor;

  /**
   * A runnable command to sound a beep as an alert.
   */
  protected Runnable _beep;
  
  /**
   * Toolbar for this view.
   */
  protected IToolBarManager _toolbar;

  /**
   * Drop-down toolbar menu for this view.
   */
  protected IMenuManager _toolbarMenu;

  /**
   * Context menu for this view.
   */
  protected MenuManager _contextMenu;

  protected IActionBars _bars;

  /**
   * Constructor.
   */
  public InteractionsView() {
    _beep = new Runnable() {
      public void run() {
        _styledText.getDisplay().beep();
      }
    };
  }

  /**
   * Cleans up any resources this view created.
   */
  public void dispose() {
    _arrowCursor.dispose();
    _waitCursor.dispose();
    _controller.dispose();
    _styledText.dispose();
    super.dispose();
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
    return _beep;
  }

  /**
   * Sets the command that creates a beep when run.
   * @param beep Command to create a beep
   */
  public void setBeep(Runnable beep) {
    _beep = beep;
  }

  /**
   * Sets the font of the Interactions Pane to be the value
   * stored in JFace's FontRegistry under the key corresponding
   * to DrJava's font preference.
   */
  public void updateFont() {
    Font font = JFaceResources.getFont(DrJavaConstants.FONT_MAIN);
    _styledText.setFont(font);
  }


  /**
   * Callback method that creates and initializes the view.
   */
  public void createPartControl(Composite parent) {
    // Workaroud for Eclipse bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=257749
    // Toolkit must be loaded by this thread -- otherwise other threads will block
    // when trying to load it.  (The DrJava dependency is OptionConstants.)
    try { Class.forName("java.awt.Toolkit"); } catch (ClassNotFoundException e) {}

    setTextPane(new StyledText(parent, SWT.WRAP | SWT.V_SCROLL));

    // Get menus

    _bars = getViewSite().getActionBars();
    /*
    bars.setGlobalActionHandler(
         IWorkbenchActionConstants.CUT, 
  new CutAction(_styledText, _clipboard));
    */

    _toolbar = _bars.getToolBarManager();
    _toolbarMenu = _bars.getMenuManager();
    _contextMenu = new MenuManager("#PopupMenu");
    Menu menu = _contextMenu.createContextMenu(_styledText);
    _styledText.setMenu(menu);

    SWTDocumentAdapter adapter = new SWTDocumentAdapter(_styledText);
    EclipseInteractionsModel model = new EclipseInteractionsModel(adapter);
    InteractionsController controller = new InteractionsController(model, adapter, this);
    setController(controller);
  }

  /**
   * Sets which text pane this view uses.  Package-private; for tests only.
   * NOTE: Should only be called once!
   * @param text A StyledText pane to use for the text
   */
  void setTextPane(StyledText text) {
    _styledText = text;
    _arrowCursor = new Cursor(_styledText.getDisplay(), SWT.CURSOR_ARROW);
    _waitCursor = new Cursor(_styledText.getDisplay(), SWT.CURSOR_WAIT);
  }

  /**
   * Sets which controller is managing this view, so we can dispose it.
   * Package-private; for tests only.
   * @param controller An InteractionsController managing the view
   */
  void setController(InteractionsController controller) {
    _controller = controller;
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
  public void setEditable(final boolean editable) {
    _styledText.getDisplay().syncExec(new Runnable() {
      public void run() {
        _styledText.setEditable(editable);
      }
    });
  }

  /**
   * Sets whether a busy (hourglass) cursor is currently shown.
   * @param busy Whether to show a busy cursor
   */
  public void setBusyCursorShown(final boolean busy) {
    _styledText.getDisplay().syncExec(new Runnable() {
      public void run() {
        if (busy) {
          _styledText.setCursor(_waitCursor);
        }
        else {
          _styledText.setCursor(_arrowCursor);
        }
      }
    });
  }

  /**
   * Shows a modal dialog without halting the thread of execution.
   * @param title Title of the dialog box
   * @param msg Message to display
   */
  public void showInfoDialog(final String title, final String msg) {
    _styledText.getDisplay().asyncExec(new Runnable() {
      public void run() {
        MessageDialog.openInformation(_styledText.getShell(),
                                      title, msg);
      }
    });
  }

  /**
   * Shows a modal dialog to confirm an operation.
   * @param title Title of the dialog box
   * @param msg Message to display
   * @return Whether the user clicked yes or not
   */
  public boolean showConfirmDialog(final String title, final String msg) {
    return MessageDialog.openQuestion(_styledText.getShell(),
                                      title, msg);
  }

  /**
   * Gives the focus to this component.
   */
  public void setFocus() {
    _styledText.setFocus();
  }

  /**
   * Add a menu item to both the toolbar menu and context menu.
   * @param action Menu item to add
   */
  public void addMenuItem(IAction action) {
    addToolbarMenuItem(action);
    addContextMenuItem(action);
  }
  
  public void addAction(String op, IAction action) {
   _bars.setGlobalActionHandler(op, action); 
   addToolbarMenuItem(action);
   addContextMenuItem(action);
  }
  
  /**
   * Add a menu item to the toolbar.
   * @param action Menu item to add
   */
  public void addToolbarMenuItem(IAction action) {
    _toolbarMenu.add(action);
  }

  public void addSelectionListener(SelectionListener listener) { 
    _styledText.addSelectionListener(listener);
  }

  /**
   * Add a menu item to the context menu.
   * @param action Menu item to add
   */
  public void addContextMenuItem(IAction action) {
    _contextMenu.add(action);
  }
  
  /** Add a top-level action to the toolbar. */
  public void addToolbarItem(IAction action) {
    _toolbar.add(action);
  }
         
}
