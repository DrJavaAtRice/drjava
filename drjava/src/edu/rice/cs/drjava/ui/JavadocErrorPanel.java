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

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.compiler.CompilerError;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;
import edu.rice.cs.util.UnexpectedException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * The panel which displays all the Javadoc parsing errors.
 *
 * @version $Id$
 */
public class JavadocErrorPanel extends ErrorPanel{

  private static final SimpleAttributeSet OUT_OF_SYNC_ATTRIBUTES = _getOutOfSyncAttributes();
  
  private static final SimpleAttributeSet _getOutOfSyncAttributes() {
    SimpleAttributeSet s = new SimpleAttributeSet();
    s.addAttribute(StyleConstants.Foreground, Color.red.darker());
    s.addAttribute(StyleConstants.Bold, Boolean.TRUE);
    return s;
  }

//   private static final String TEST_OUT_OF_SYNC = "The document being tested has been modified " +
//     "and should be recompiled!\n";

  protected JavadocErrorListPane _errorListPane;
//   private int _testCount;
  private boolean _successful;

  /**
   * Constructor.
   * @param model SingleDisplayModel in which we are running
   * @param frame MainFrame in which we are displayed
   */
  public JavadocErrorPanel(SingleDisplayModel model, MainFrame frame) {
    super(model, frame, "Javadoc Output");
//     _testCount = 0;
    _successful = true;
    _errorListPane = new JavadocErrorListPane();

    _mainPanel.setLayout(new BorderLayout());

    // We make the vertical scrollbar always there.
    // If we don't, when it pops up it cuts away the right edge of the
    // text. Very bad.
    JScrollPane scroller =
      new BorderlessScrollPane(_errorListPane,
                      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


    _mainPanel.add(scroller, BorderLayout.CENTER);
    
    JPanel sidePanel = new JPanel(new BorderLayout());
    sidePanel.setBorder(new EmptyBorder(0,5,0,5)); // 5 pix padding on sides
    JPanel innerPanel = new JPanel(new BorderLayout());  // bar and checkbox
    innerPanel.setBorder(new EmptyBorder(5,0,0,0)); // 5 pix padding on top
//     sidePanel.add(new JLabel("Test Progress", SwingConstants.LEFT),
//                       BorderLayout.NORTH);
       
    sidePanel.add(innerPanel,BorderLayout.CENTER);
    
    innerPanel.add(new JPanel(),BorderLayout.CENTER);
        
    _showHighlightsCheckBox = new JCheckBox( "Highlight source", true);
    _showHighlightsCheckBox.addChangeListener( new ChangeListener() {
      public void stateChanged (ChangeEvent ce) {
        DefinitionsPane lastDefPane = getErrorListPane().getLastDefPane();
        
        if (_showHighlightsCheckBox.isSelected()) {
          //lastDefPane.setCaretPosition( lastDefPane.getCaretPosition());
          _errorListPane.switchToError(getErrorListPane().getSelectedIndex());
          lastDefPane.requestFocus();
          lastDefPane.getCaret().setVisible(true);
        }
        else {
          lastDefPane.removeTestErrorHighlight();
        }
      }
    });
    
    innerPanel.add(_showHighlightsCheckBox, BorderLayout.SOUTH);
    _mainPanel.add(sidePanel, BorderLayout.EAST);

  }
  
  /**
   * Returns the JavadocErrorListPane that this panel manages.
   */
  public JavadocErrorListPane getErrorListPane() {
    return _errorListPane;
  }

  protected CompilerErrorModel getErrorModel(){
    return getModel().getJavadocErrorModel();
  }

  /** Called when work begins. */
  public void setJavadocInProgress() {
    _errorListPane.setJavadocInProgress();
  }

  /**
   * Clean up when the tab is closed.
   */
  protected void _close() {
    super._close();
    reset();
  }

  /**
   * Reset the errors to the current error information.
   */
  public void reset() {
    CompilerErrorModel em = _model.getJavadocErrorModel();
    if (em != null) {
      _numErrors = em.getNumErrors();
    } else {
      _numErrors = 0;
    }
    _errorListPane.updateListPane(true);
  }

  /**
   * A pane to show Javadoc errors. It acts a bit like a listbox (clicking
   * selects an item) but items can each wrap, etc.
   */
  public class JavadocErrorListPane extends ErrorPanel.ErrorListPane {
    private final JLabel _errorLabel = new JLabel(),
    /*_testLabel = new JLabel(),*/ _fileLabel = new JLabel();

    /** Puts the error pane into "compilation in progress" state. */
    public void setJavadocInProgress() {
      _errorListPositions = new Position[0];

      DefaultStyledDocument doc = new DefaultStyledDocument();

      try {
        doc.insertString(doc.getLength(),
                         "Generating Javadoc.  Please wait...\n",
                         NORMAL_ATTRIBUTES);
      }
      catch (BadLocationException ble) {
        throw new UnexpectedException(ble);
      }
      setDocument(doc);

      selectNothing();
    }

    /**
     * Used to show that the last javadoc command was unsuccessful.
     */
    protected void _updateWithErrors() throws BadLocationException {
      DefaultStyledDocument doc = new DefaultStyledDocument();
      _updateWithErrors("error", "found", doc);
    }

    /**
     * Used to show that the last compile was successful.
     */
    protected void _updateNoErrors(boolean done) throws BadLocationException {
      DefaultStyledDocument doc = new DefaultStyledDocument();
      String msg = (done) ? "Javadoc generated successfully." : "";

      doc.insertString(doc.getLength(),
                       msg,
                       NORMAL_ATTRIBUTES);
      setDocument(doc);

      selectNothing();
    }

//     public JavadocError getError() {
//       return _error;
//     }

  }
  
}
