/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JEditorPane;

import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.EditorKit;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;

import java.awt.Color;
import java.awt.Font;

public class DefinitionsView extends JEditorPane
{
  /** Keep track of the name of the file currently associated
   *  with the document we're editing. If we've never saved this file
   *  then this String is "". */
  private String _currentFileName = "";
  private MainFrame _mainFrame;

  public DefinitionsView(MainFrame mf)
  {
    _mainFrame = mf;
    _resetDocument("");
  }

  /** Overriding this method ensures that all new documents created in this
   *  editor pane use our editor kit (and thus our model). */
  protected EditorKit createDefaultEditorKit()
  {
    return new DefinitionsEditorKit();
  }

  /** Save the current document over the old version of the document.
   *  If the current document is unsaved, call save as. */
  public void save()
  {
    if (_currentFileName == "")
      saveAs();
    else
      _saveToFile(_currentFileName);
  }

  /** Prompt the user to select a place to save the file, then save it. */
  public void saveAs()
  {
    JFileChooser fc = new JFileChooser();
    int rc = fc.showSaveDialog(this);

    switch(rc)
    {
      case JFileChooser.CANCEL_OPTION:
      case JFileChooser.ERROR_OPTION:
        break;
      case JFileChooser.APPROVE_OPTION:
        File chosen = fc.getSelectedFile();
        _saveToFile(chosen.getAbsolutePath());
        break;
    }
  }

  private void _resetDocument(String path)
  {
    String titlebarName;

    if (path == "")
    {
      titlebarName = "Untitled";
    }
    else
    {
      File f = new File(path);
      titlebarName = f.getName();
    }

    _currentFileName = path;
    _doc().resetModification();
    _mainFrame.updateFileTitle(titlebarName);
  }

  /** Save the current document to the given path.
   *  Inform the user if there was a problem.
   */
  private void _saveToFile(String path)
  {
    try
    {
      FileWriter writer = new FileWriter(path);
      write(writer);
      writer.close(); // This flushes the buffer!
      // Update file name if the read succeeds.
      _resetDocument(path);
    }
    catch (IOException ioe)
    {
      String msg = "There was an error saving to the file " + path + "\n\n" +
                   ioe.getMessage();
         
      // Tell the user it failed and move on.
      JOptionPane.showMessageDialog(this,
                                    "Error saving file",
                                    msg,
                                    JOptionPane.ERROR_MESSAGE);
    }
  }

  /** Create a new, empty file in this view. */
  public void newFile()
  {
    boolean isOK = checkAbandoningChanges();
    if (!isOK) return;

    setDocument(getEditorKit().createDefaultDocument());
    _resetDocument("");
  }

  /** Prompt the user to select a place to open a file from, then load it.
   *  Ask the user if they'd like to save previous changes (if the current
   *  document has been modified) before opening.
   */
  public void open()
  {
    boolean isOK = checkAbandoningChanges();
    if (!isOK) return;

    JFileChooser fc = new JFileChooser();
    int rc = fc.showOpenDialog(this);

    switch(rc)
    {
      case JFileChooser.CANCEL_OPTION:
      case JFileChooser.ERROR_OPTION:
        break;
      case JFileChooser.APPROVE_OPTION:
        File chosen = fc.getSelectedFile();

        try
        {
          FileReader reader = new FileReader(chosen);
          read(reader, null);
          // Update file name if the read succeeds.
          _resetDocument(chosen.getAbsolutePath());
        }
        catch (IOException ioe)
        {
          String msg = "There was an error opening the file.\n\n" +
                       ioe.getMessage();
             
          // Tell the user it failed and move on.
          JOptionPane.showMessageDialog(this,
                                        "Error opening file",
                                        msg,
                                        JOptionPane.ERROR_MESSAGE);
        }

        break;
    }
  }

  /**
   * Check if the current document has been modified. If it has, ask the user
   * if he would like to save or not, and save the document if yes. Also
   * give the user a "cancel" option to cancel doing the operation that got
   * us here in the first place.
   *
   * @return A boolean, if true means the user is OK with the file being saved
   *         or not as they chose. If false, the user wishes to cancel.
   */
  public boolean checkAbandoningChanges()
  {
    boolean retVal = true;

    if (_doc().modifiedSinceSave())
    {
      String fname = _currentFileName;
      if (fname == "")
        fname = "untitled file";

      String text = fname + " has been modified. Would you like to " +
                    "save?";

      int rc = JOptionPane.showConfirmDialog(
                this, 
                "Would you like to save " + fname + "?",
                text,
                JOptionPane.YES_NO_CANCEL_OPTION);

      switch (rc)
      {
        case JOptionPane.YES_OPTION:
          save();
          retVal = true;
          break;
        case JOptionPane.NO_OPTION:
          retVal = true;
          break;
        case JOptionPane.CANCEL_OPTION:
          retVal = false;
          break;
      }

    }

    return retVal;
  }

  private DefinitionsDocument _doc()
  {
    return (DefinitionsDocument) getDocument();
  }
}
