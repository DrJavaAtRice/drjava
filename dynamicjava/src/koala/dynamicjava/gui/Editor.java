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
import javax.swing.undo.*;

import koala.dynamicjava.gui.resource.*;
import koala.dynamicjava.gui.resource.ActionMap;

/**
 * The editor component of the GUI
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/10/09
 * modified by James Hsia and Eliot Flannery
 */

public class Editor extends JTextArea implements ActionMap {
  /**
   * The currently edited file
   */
  protected File currentFile;
  
  /**
   * The current document
   */
  protected Document document;
  
  /**
   * The input buffer
   */
  protected static char [] buffer = new char[4096];
  
  /**
   * Listener for the edits on the current document
   */
  protected UndoableEditListener undoHandler;
  
  /**
   * UndoManager that we add edits to.
   */
  protected UndoManager undo;
  
  /**
   * The undo action
   */
  protected UndoAction undoAction;
  
  /**
   * The redo action
   */
  protected RedoAction redoAction;
  
  /**
   * Has the document been modified?
   */
  protected boolean documentModified;
  
  /**
   * The message handler
   */
  protected MessageHandler messageHandler;
  
  /**
   * Creates a new editor
   * @param mh the object that displays the messages
   */
  public Editor(MessageHandler mh) {
    setFont(new Font("monospaced", Font.PLAIN, 12));
    
    undoHandler      = new UndoHandler();
    undo             = new UndoManager();
    
    actions.put("OpenAction",   new OpenAction());
    actions.put("SaveAction",   new SaveAction());
    actions.put("SaveAsAction", new SaveAsAction());
    actions.put("UndoAction",   undoAction = new UndoAction());
    actions.put("RedoAction",   redoAction = new RedoAction());
    
    document = getDocument();
    document.addDocumentListener(new DocumentAdapter());
    document.addUndoableEditListener(undoHandler);
    
    messageHandler = mh;
    messageHandler.setMainMessage("Status.init");
  }
  
  /**
   * Opens a file
   * @param name the name of the file
   */
  public void openFile(String name) {
    currentFile = new File(name);
    document = new PlainDocument();
    
    if (currentFile.exists()) {
      try {
        Reader in = new FileReader(currentFile);
        int nch;
        
        while ((nch = in.read(buffer, 0, buffer.length)) != -1) {
          document.insertString(document.getLength(),
                                new String(buffer, 0, nch), null);
        }
      } catch (Exception ex) {
        // TODO : dialog
        System.err.println(ex.toString());
      }
    }
    document.addDocumentListener(new DocumentAdapter());
    document.addUndoableEditListener(undoHandler);
    undo = new UndoManager();
    undoAction.update();
    redoAction.update();
    setDocument(document);
  }
  
  /**
   * Saves the document
   */
  protected void saveDocument() {
    if (currentFile == null) {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileHidingEnabled(false);
      
      int choice = fileChooser.showSaveDialog(this);
      if (choice == JFileChooser.APPROVE_OPTION) {
        currentFile = fileChooser.getSelectedFile();
      }
    }
    
    if (currentFile != null) {
      try {
        Writer out = new FileWriter(currentFile);
        out.write(document.getText(0, document.getLength()));
        out.flush();
      } catch (Exception ex) {
        // TODO : dialog
        System.err.println(ex.toString());
      }
    }
  }
  
  /**
   * Manages the closing of the buffer
   */
  public void closeProcedure() {
    if (documentModified) {
      if (JOptionPane.showConfirmDialog(this,
                                        "Save the current buffer?",
                                        "Unsaved Buffer",
                                        JOptionPane.YES_NO_OPTION) ==
          JOptionPane.YES_OPTION) {
        saveDocument();
      }
    }       
  }
  
  /**
   * To listen to the document undoable edit
   */
  class UndoHandler implements UndoableEditListener {
    
    /**
     * Messaged when the Document has created an edit, the edit is
     * added to <code>undo</code>, an instance of UndoManager.
     */
    public void undoableEditHappened(UndoableEditEvent e) {
      undo.addEdit(e.getEdit());
      undoAction.update();
      redoAction.update();
    }
  }
  
  /**
   * To undo the last edit
   */
  class UndoAction extends AbstractAction implements JComponentModifier {
    java.util.List<JComponent> components = new LinkedList<JComponent>();
    
    public void actionPerformed(ActionEvent e) {
      try {
        undo.undo();
      } catch (CannotUndoException ex) {
        // TODO : dialog
        System.out.println(ex);
      }
      update();
      redoAction.update();
    }
    
    public void addJComponent(JComponent c) {
      components.add(c);
      c.setEnabled(false);
    }
    
    protected void update() {
      documentModified = undo.canUndo();
      Iterator<JComponent> it = components.iterator();
      while (it.hasNext()) {
        it.next().setEnabled(documentModified);
      }
    }
  }
  
  /**
   * To redo the last undone edit
   */
  class RedoAction extends AbstractAction implements JComponentModifier {
    java.util.List<JComponent> components = new LinkedList<JComponent>();
    
    public void actionPerformed(ActionEvent e) {
      try {
        undo.redo();
      } catch (CannotRedoException ex) {
        // TODO : dialog
        System.out.println(ex);
      }
      update();
      undoAction.update();
    }
    
    public void addJComponent(JComponent c) {
      components.add(c);
      c.setEnabled(false);
    }
    
    protected void update() {
      Iterator<JComponent> it = components.iterator();
      while (it.hasNext()) {
        it.next().setEnabled(undo.canRedo());
      }
    }
  }
  
  /**
   * To listen to the document changes
   */
  class DocumentAdapter implements DocumentListener {
    
    public void changedUpdate(DocumentEvent e) {
      documentModified = true;
    }
    
    public void insertUpdate(DocumentEvent e) {
      documentModified = true;
    }
    
    public void removeUpdate(DocumentEvent e) {
      documentModified = true;
    }       
  }
  
  /**
   * To open a file
   */
  class OpenAction extends AbstractAction {
    
    public void actionPerformed(ActionEvent e) {
      if (documentModified) {
        document.removeUndoableEditListener(undoHandler);
        closeProcedure();
        documentModified = false;
      }
      
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileHidingEnabled(false);
      
      int choice = fileChooser.showOpenDialog(Editor.this);
      if (choice == JFileChooser.APPROVE_OPTION) {
        currentFile  = fileChooser.getSelectedFile();
        document = new PlainDocument();
        
        if (currentFile.exists()) {
          try {
            Reader in = new FileReader(currentFile);
            int nch;
            
            while ((nch = in.read(buffer, 0, buffer.length)) != -1) {
              document.insertString(document.getLength(),
                                    new String(buffer, 0, nch), null);
            }
            
            messageHandler.setMainMessage("Status.current",
                                          currentFile.getCanonicalPath());
          } catch (Exception ex) {
            // TODO : dialog
            System.err.println(ex.toString());
          }
        }
        document.addDocumentListener(new DocumentAdapter());
        document.addUndoableEditListener(undoHandler);
        undo = new UndoManager();
        undoAction.update();
        redoAction.update();
        setDocument(document);
      }
    }
  }
  
  /**
   * To save the buffer
   */
  class SaveAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      if (documentModified) {
        saveDocument();
        documentModified = false;
        try {
          messageHandler.setMessage("Status.wrote",
                                    currentFile.getCanonicalPath());
        } catch (Exception ex) {
        }
      }
    }
  }
  
  /**
   * To save the buffer as a file
   */
  class SaveAsAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileHidingEnabled(false);
      
      int choice = fileChooser.showSaveDialog(Editor.this);
      if (choice == JFileChooser.APPROVE_OPTION) {
        currentFile = fileChooser.getSelectedFile();
        
        try {
          Writer out = new FileWriter(currentFile);
          out.write(document.getText(0, document.getLength()));
          out.flush();
          messageHandler.setMainMessage("Status.current",
                                        currentFile.getCanonicalPath());
        } catch (Exception ex) {
          // TODO : dialog
          System.err.println(ex.toString());
        }
      }
    }
  }
  
  // ActionMap implementation
  
  /**
   * The action map
   */
  protected Map<String,Action> actions = new HashMap<String,Action>();
  
  /**
   * Returns the action associated with the given string
   * or null on error
   * @param key the key mapped with the action to get
   * @throws MissingListenerException if the action is not found
   */
  public Action getAction(String key) throws MissingListenerException {
    Action[] editorActions = getActions();
    
    for (int i = 0; i < editorActions.length; i++) {
      if (editorActions[i].getValue(Action.NAME).equals(key)) {
        return editorActions[i];
      }
    }
    return actions.get(key);
  }
}
