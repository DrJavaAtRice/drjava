/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import koala.dynamicjava.gui.resource.*;
import koala.dynamicjava.gui.resource.ActionMap;

/**
 * A component used to enter an URL or to choose a local file
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/11/14
 */

public class URLChooser extends JDialog implements ActionMap {
    /**
     * The resource file name
     */
    protected final static String RESOURCE =
	"koala.dynamicjava.gui.resources.urlchooser";

    /**
     * The resource bundle
     */
    protected static ResourceBundle bundle;

    /**
     * The resource manager
     */
    protected static ResourceManager resources;

    /**
     * The button factory
     */
    protected ButtonFactory buttonFactory;

    /**
     * The text field
     */
    protected JTextField textField;

    /**
     * The OK button
     */
    protected JButton okButton;
    
    /**
     * The Clear button
     */
    protected JButton clearButton;

    /**
     * The external action associated with the ok button
     */
    protected Action okAction;

    static {
	bundle = ResourceBundle.getBundle(RESOURCE, Locale.getDefault());
	resources = new ResourceManager(bundle);
    }

    /**
     * Creates a new URLChooser
     * @param d the parent dialog
     * @param okAction the action to associate to the ok button
     */
    public URLChooser(JDialog d, Action okAction) {
	super(d);
	initialize(okAction);
    }

    /**
     * Creates a new URLChooser
     * @param f the parent frame
     * @param okAction the action to associate to the ok button
     */
    public URLChooser(JFrame f, Action okAction) {
	super(f);
	initialize(okAction);
    }

    /**
     * Returns the text contained in the text field
     */
    public String getText() {
	return textField.getText();
    }

    /**
     * Initializes the dialog
     */
    protected void initialize(Action okAction) {
	this.okAction = okAction;
	setModal(true);

	listeners.put("BrowseButtonAction", new BrowseButtonAction());
	listeners.put("OKButtonAction",     new OKButtonAction());
	listeners.put("CancelButtonAction", new CancelButtonAction());
	listeners.put("ClearButtonAction",  new ClearButtonAction());

	setTitle(resources.getString("URLChooser.title"));
	buttonFactory = new ButtonFactory(bundle, this);
	
	getContentPane().add("North", createURLSelectionPanel());
	getContentPane().add("South", createButtonsPanel());
    }

    /**
     * Creates the URL selection panel
     */
    protected JPanel createURLSelectionPanel() {
	JPanel p = new JPanel(new GridBagLayout());
	p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    
	GridBagConstraints constraints = new GridBagConstraints();

	constraints.insets = new Insets(5, 5, 5, 5);
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	setConstraintsCoords(constraints, 0, 0, 2, 1);
	p.add(new JLabel(resources.getString("URLChooser.label")), constraints);

	textField = new JTextField(30);
	textField.getDocument().addDocumentListener(new DocumentAdapter());
	constraints.weightx = 1.0;
	constraints.weighty = 0;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	setConstraintsCoords(constraints, 0, 1, 1, 1);
	p.add(textField, constraints);
        
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.fill = GridBagConstraints.NONE;
	setConstraintsCoords(constraints, 1, 1, 1, 1);
	p.add(buttonFactory.createJButton("BrowseButton"), constraints);
	
	return p;
    }

    /**
     * Creates the buttons panel
     */
    protected JPanel createButtonsPanel() {
	JPanel  p = new JPanel(new FlowLayout());

	p.add(okButton = buttonFactory.createJButton("OKButton"));
	p.add(buttonFactory.createJButton("CancelButton"));
	p.add(clearButton = buttonFactory.createJButton("ClearButton"));
	    
	okButton.setEnabled(false);
	clearButton.setEnabled(false);
	
	return p;
    }

    /**
     * An utility funtion
     */
    protected static void setConstraintsCoords(GridBagConstraints constraints,
					       int x, int y, 
					       int width, int height) {
	constraints.gridx = x;
	constraints.gridy = y;
	constraints.gridwidth = width;
	constraints.gridheight = height;
    }

    /**
     * To update the state of the OK button
     */
    protected void updateOKButtonAction() {
	okButton.setEnabled(!textField.getText().equals(""));
    }

    /**
     * To update the state of the Clear button
     */
    protected void updateClearButtonAction() {
	clearButton.setEnabled(!textField.getText().equals(""));
    }

    /**
     * To listen to the document changes
     */
    protected class DocumentAdapter implements DocumentListener {
	public void changedUpdate(DocumentEvent e) {
	    updateOKButtonAction();
	    updateClearButtonAction();
	}
	    
	public void insertUpdate(DocumentEvent e) {
	    updateOKButtonAction();
	    updateClearButtonAction();
	}

	public void removeUpdate(DocumentEvent e) {
	    updateOKButtonAction();
	    updateClearButtonAction();
	}       
    }

    /**
     * The action associated with the 'browse' button
     */
    protected class BrowseButtonAction extends AbstractAction {
	public void actionPerformed(ActionEvent e) {
	    JFileChooser fileChooser = new JFileChooser();
	    fileChooser.setFileHidingEnabled(false);
	    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

	    int choice = fileChooser.showOpenDialog(URLChooser.this);
	    if (choice == JFileChooser.APPROVE_OPTION) {
		File f = fileChooser.getSelectedFile();
		try {
		    textField.setText(f.getCanonicalPath());
		} catch (IOException ex) {
		}
	    }
	}
    }

    /**
     * The action associated with the 'OK' button of the URL chooser
     */
    protected class OKButtonAction extends AbstractAction {
	public void actionPerformed(ActionEvent e) {
	    okAction.actionPerformed(e);
	    dispose();
	    textField.setText("");
	}
    }

    /**
     * The action associated with the 'Cancel' button of the URL chooser
     */
    protected class CancelButtonAction extends AbstractAction {
	public void actionPerformed(ActionEvent e) {
	    dispose();
	    textField.setText("");
	}
    }

    /**
     * The action associated with the 'Clear' button of the URL chooser
     */
    protected class ClearButtonAction extends AbstractAction {
	public void actionPerformed(ActionEvent e) {
	    textField.setText("");
	}
    }

    // ActionMap implementation

    /**
     * The map that contains the listeners
     */
    protected Map listeners = new HashMap();

    /**
     * Returns the action associated with the given string
     * or null on error
     * @param key the key mapped with the action to get
     * @throws MissingListenerException if the action is not found
     */
    public Action getAction(String key) throws MissingListenerException {
	return (Action)listeners.get(key);
    }
}
