package edu.rice.cs.drjava;

import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.Box;

import java.awt.Frame;
import java.awt.Label;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class FindReplaceDialog extends JDialog {

    private JOptionPane _optionPane;
		private String _fWord = null;
		private String _rWord = null;
		private DefinitionsView _view;
		private int _currentPosition;
		private JButton _findButton;
		private JButton _findNextButton;
		private JButton _replaceButton;
		private JButton _replaceAllButton;
		private JButton _closeButton;


		public String getFindWord () {
				return _fWord;
		}

		public String getReplaceWord () {
				return _rWord;
		}

    public FindReplaceDialog(Frame frame, DefinitionsView view) {
				
				super(frame);

				_view = view;
        setTitle("Find/Replace");
				
				final String msgString1 = "Find:";
				final String msgString2 = "Replace:";
        final JTextField findField = new JTextField(10);
        final JTextField  replaceField = new JTextField(10);

				Object[] array = {msgString1, findField, msgString2, replaceField};

			  _findButton = new JButton ("Find");
				_findNextButton = new JButton ("Find Next");
				_replaceButton = new JButton ("Replace and\nFind Next");
			  _replaceAllButton = new JButton ("Replace All");
				_closeButton = new JButton ("Close");

				// set up the layout
				Box outside = Box.createVerticalBox();
				Box buttons = Box.createHorizontalBox();
				
				buttons.add(_findButton);
				buttons.add(_findNextButton);
				buttons.add(_replaceButton);
				_replaceButton.setEnabled(false);
				buttons.add(_replaceAllButton);
				buttons.add(_closeButton);

				outside.add(new Label(msgString1));
				outside.add(findField);
				outside.add(new Label(msgString2));				
				outside.add(replaceField);
				outside.add(buttons);

        setContentPane(outside);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                /*
                 * Instead of directly closing the window,
                 * we're going to change the JOptionPane's
                 * value property.
                 */
										System.out.println("closing?");
            }
        });


				findField.getDocument().addDocumentListener(new DocumentListener() {
						public void changedUpdate(DocumentEvent e) {
								_replaceButton.setEnabled(false);
						}

						public void insertUpdate(DocumentEvent e) {
								_replaceButton.setEnabled(false);
						}

						public void removeUpdate(DocumentEvent e) {
								_replaceButton.setEnabled(false);
						}
				});

				// button action listeners
				_findButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
								_fWord = findField.getText();
								_rWord = replaceField.getText();
								// finds the first occurance
								int pos = _view.findText(_fWord);
								if (pos != -1) {
										_currentPosition = pos;
										_replaceButton.setEnabled(true);
								} else {
										_replaceButton.setEnabled(false);
								}
										
						}
				});


				/** only enable if you found one.. otherwise leave it the same as before
				 */
				_findNextButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
								_fWord = findField.getText();
								_rWord = replaceField.getText();
								// finds the first occurance after curpos + length
								int pos = _view.findNextText(_fWord,
																						 _currentPosition
																						 + _fWord.length());
								if (pos != -1) {
										_currentPosition = pos;
										_replaceButton.setEnabled(true);
								}
						}
				});

				_replaceButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
								_fWord = findField.getText();
								_rWord = replaceField.getText();
								// replaces the occurance at the current position
								_currentPosition = _view.replaceText(_fWord,
																										 _rWord,
																										 _currentPosition);
								// and finds the next word
								_currentPosition = _view.findNextText(_fWord, _currentPosition);
								if (_currentPosition == -1)
										_replaceButton.setEnabled(false);
						}
				});

				_replaceAllButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
								_fWord = findField.getText();
								_rWord = replaceField.getText();
								// replaces everything from the current position on
								_currentPosition = _view.replaceAllText(_fWord,
																												_rWord,
																												_currentPosition);
								_replaceButton.setEnabled(false);
								//show "replaced bing occurances" dialog?
						}
				});

				_closeButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
								setVisible(false);
						}
				});
				
				setBounds(100, 200, 500, 300);
				setSize(500, 300);
				setVisible(true);
		}
}
