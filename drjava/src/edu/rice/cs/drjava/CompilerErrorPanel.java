/* $Id$ */

/* If click on combobox, move cursor to error location.
 * If move cursor onto line with error, select error in combobox but do
 * not move the cursor.
 */

package edu.rice.cs.drjava;

import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.ListSelectionModel;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;

import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.Color;
import java.awt.Component;
import java.awt.BorderLayout;
public class CompilerErrorPanel extends JPanel {
  private static final String NEWLINE = System.getProperty("line.separator");

  private static DefaultHighlighter.DefaultHighlightPainter _highlightPainter
  = new DefaultHighlighter.DefaultHighlightPainter(Color.red);

  // when we create a highlight we get back a tag we can use to remove it
  private Object _previousHighlightTag = null;

  private CompilerError[] _errors;
  private Position[] _errorPositions;

  private DefinitionsView _definitionsView;
  private int _selectedError;

  private CompilerErrorListModel _listModel;
  private JList _errorList;
  private JButton _showAllButton;
  private JButton _nextButton;
  private JButton _previousButton;

  /**
   * We want the selected error in the combo box to change when the caret
   * reaches a line with an error. But when the combo box is changed by a click,
   * we want to move the caret to the location of the error. To keep from moving
   * the caret when all we wanted to do was select the error in the combo box,
   * we set this flag.
   */
  private boolean _caretGuard = false;

  private ListSelectionListener _listListener = new ListSelectionListener() {
    public void valueChanged(ListSelectionEvent e) {
      if (! _caretGuard) {
        // subtract 1 to deal with the (none) line in the list
        _gotoError(_errorList.getSelectedIndex() - 1);
      }
    }
  };

  private CaretListener _listener = new CaretListener() {
    public void caretUpdate(CaretEvent evt) {
      if (_errorPositions.length == 0) {
        return;
      }

      // Now we can assume at least one error.

      int curPos = evt.getDot();
      // check if the dot is on a line with an error.
      // Find the first error that is on or after the dot. If this comes
      // before the newline after the dot, it's on the same line.
      int errorAfter; // index of the first error after the dot
      for (errorAfter = 0; errorAfter < _errorPositions.length; errorAfter++) {
        if (_errorPositions[errorAfter].getOffset() >= curPos) {
          break;
        }
      }

      // index of the first error before the dot
      int errorBefore = errorAfter - 1;

      // this will be set to what we want to select
      int shouldSelect = -1;

      if (errorBefore >= 0) { // there's an error before the dot
        int errPos = _errorPositions[errorBefore].getOffset();
        try {
          String betweenDotAndErr =
            _definitionsView.getDocument().getText(errPos, curPos - errPos);

          if (betweenDotAndErr.indexOf(NEWLINE) == -1) {
            shouldSelect = errorBefore;
          }
        } 
        catch (BadLocationException willNeverHappen) {}
      }

      if ((shouldSelect == -1) && (errorAfter != _errorPositions.length)) {
        // we found an error on/after the dot
        // if there's a newline between dot and error,
        // then it's not on this line
        int errPos = _errorPositions[errorAfter].getOffset();
        try {
          String betweenDotAndErr =
            _definitionsView.getDocument().getText(curPos, errPos - curPos);

          if (betweenDotAndErr.indexOf(NEWLINE) == -1) {
            shouldSelect = errorAfter;
          }
        } 
        catch (BadLocationException willNeverHappen) {}
      }

      // if no error is on this line, select the (none) item
      if (shouldSelect == -1) {
        _removePreviousHighlight();
        _caretGuard = true;
        _errorList.setSelectedIndex(0);
        _selectedError = -1;
        _caretGuard = false;
      }
      else if (shouldSelect != _selectedError) {
        _caretGuard = true;
        _selectError(shouldSelect);
        _caretGuard = false;
      }
    }
  };

  private void _addHighlight(int from, int to) throws BadLocationException {
    _previousHighlightTag =
      _definitionsView.getHighlighter().addHighlight(from,
                                                     to,
                                                     _highlightPainter);
  }

  private void _removePreviousHighlight() {
    if (_previousHighlightTag != null) {
      _definitionsView.getHighlighter().removeHighlight(_previousHighlightTag);
      _previousHighlightTag = null;
    }
  }

  public CompilerErrorPanel(DefinitionsView view) {
		setLayout(new BorderLayout());
				
		_listModel = new CompilerErrorListModel();
    _definitionsView = view;
    _definitionsView.addCaretListener(_listener);

    // Make errors initially zero-length array so it's never null
    _errors = new CompilerError[0];
    _errorPositions = new Position[0];
    _selectedError = 0;

    _errorList = new JList(_listModel);
		_errorList.setCellRenderer(new myCellRenderer());
		_errorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _errorList.addListSelectionListener(_listListener);
    
    _showAllButton = new JButton("Show all");
    _showAllButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          _showAllErrors();
        }
    });

    _nextButton = new JButton("Next");
    _nextButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          _gotoError(_selectedError + 1);
        }
    });

    _previousButton = new JButton("Previous");
    _previousButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          _gotoError(_selectedError - 1);
        }
    });
		
    _resetEnabledStatus();
		//size stuff
		_errorList.setVisibleRowCount(4);
		JScrollPane scrollPane = new JScrollPane(_errorList);
		//scrollPane.setPreferredSize(
		//	_errorList.getPreferredScrollableViewportSize());
    add(scrollPane, BorderLayout.CENTER);
    // Show all not yet implemented.
    // add(_showAllButton);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(_previousButton);
    buttonPanel.add(_nextButton);
		add(buttonPanel, BorderLayout.EAST);
  }

  private void _gotoError(int newIndex) {
    if (newIndex < 0) return;

    _selectError(newIndex);
    // move caret to that position
    final int idx = newIndex; // final so it's accessible inside inner class

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        int errPos = _errorPositions[idx].getOffset();
        _definitionsView.setCaretPosition(errPos);
        _definitionsView.grabFocus();
      }
    });
  }

  private void _selectError(int newIndex) {
    if (newIndex != _selectedError) {
      _selectedError = newIndex;
      // add one to account for the (none) item
      _errorList.setSelectedIndex(_selectedError + 1);
    }

    int errPos = _errorPositions[newIndex].getOffset();
    try {
      Document doc = _definitionsView.getDocument();
      String text = doc.getText(0, doc.getLength());

      int prevNewline = text.lastIndexOf(NEWLINE, errPos);
      if (prevNewline == -1) {
        prevNewline = 0;
      }
      
      int nextNewline = text.indexOf(NEWLINE, errPos);
      if (nextNewline == -1) {
        nextNewline = doc.getLength();
      }

      _removePreviousHighlight();
      _addHighlight(prevNewline, nextNewline);
    }
    catch (BadLocationException stupidMachineItWontHappen) {}

    // now display that line with highlight
    //System.err.println("Selected error: " + newIndex);
    _resetEnabledStatus();
  }

  private void _showAllErrors() {
  }

  private void _resetEnabledStatus() {
    _nextButton.setEnabled(_selectedError < (_errors.length - 1));
    _previousButton.setEnabled(_selectedError > 0);
    _showAllButton.setEnabled(_errors.length != 0);
  }

  public void resetErrors(CompilerError[] errors) {
    int oldSize = _errors.length;
    _errors = errors;

    // sort the errors by location
    Arrays.sort(_errors);

    _createPositionsArray();

    _listModel.notifyChanged(oldSize);

    if (_errors.length > 0) {
      // dummy value so it knows we did really change selection
      _selectedError = -1;
      _gotoError(0);
    }

    _resetEnabledStatus();
  }

  private void _createPositionsArray() {
    // Create array of positions where each error occurred
    _errorPositions = new Position[_errors.length];
    Document defsDoc = _definitionsView.getDocument();
    try {
      String defsText = defsDoc.getText(0, defsDoc.getLength());

      int curLine = 0;
      int offset = 0; // offset is number of chars from beginning of file
      int numProcessed = 0;

      // offset is always pointing to the first character in a line
      // at the top of the loop
      while ((numProcessed < _errors.length) &&
          (offset < defsText.length())) {
        // first figure out if we need to create any new positions on this line
        for (int i = numProcessed;
            (i < _errors.length) && (_errors[i].lineNumber() == curLine);
            i++) {
          _errorPositions[i] = defsDoc.createPosition(offset +
              _errors[i].startColumn());
          numProcessed++;
        }

        int nextNewline = defsText.indexOf(NEWLINE, offset);
        if (nextNewline == -1) {
          break;
        }
        else {
          curLine++;
          offset = nextNewline + 1;
        }
      }
    }
    catch (BadLocationException willNeverEverEverHappen) {}
  }

  class CompilerErrorListModel extends DefaultComboBoxModel {
    // we put in fake element #0
    // thus we need to subtract one to get the index into the real list of errs
    public Object getElementAt(int i) {
      if (i == 0) {
        return "(no error on this line)";
      } else {
        CompilerError curErr = _errors[i - 1];

        // We need to make each message unique due to a documented bug in
        // JComboBox. Otherwise navigation in the list gets messed.
        // So we put the error # in the message to make it unique.
        return i + ": " + curErr.message();
      }
    }

    public int getSize() {
      return _errors.length + 1;
    }

    void notifyChanged(int oldSize) {
      int sizeDelta = _errors.length - oldSize;

      if (sizeDelta > 0) {
        fireIntervalAdded(this, oldSize + 1, _errors.length - 1);
      }
      else if (sizeDelta < 0) {
        fireIntervalRemoved(this, _errors.length + 1, oldSize - 1);
      }

      // We use min of the two sizes since remove/add dealt with the ones
      // beyond the minimum of the sizes.
      fireContentsChanged(this, 0, Math.min(oldSize, _errors.length) + 1);
    }
  }

	private class myCellRenderer extends JTextArea implements ListCellRenderer {
		public myCellRenderer()
			{
				setLineWrap(true);
				setEditable(false);
			}
		
		public myCellRenderer (String text, boolean isSelected)
			{
				if(isSelected) {
					setBackground(Color.yellow);
					setForeground(Color.black);
				}
				setLineWrap(true);
				setEditable(false);
				append(text);
			}

		public Component getListCellRendererComponent (JList list,
																									 Object value,
																									 int index,
																									 boolean isSelected,
																									 boolean cellHasFocus)
			{
				return new myCellRenderer(value.toString(),isSelected);
			}
	}
}











