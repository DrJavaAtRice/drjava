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

import org.eclipse.ui.part.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.SWT;

import edu.rice.cs.drjava.model.repl.SimpleInteractionsDocument;
import edu.rice.cs.util.text.SWTDocumentAdapter;

/**
 * This class is the main view component for the Interactions Pane
 * Eclipse plugin.
 * @version $Id$
 */
public class InteractionsView extends ViewPart {
  
  // TO DO:
  //  - How to generate a beep?
  
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
        _styledText.getDisplay().beep();
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