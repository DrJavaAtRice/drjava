/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.BoxLayout;
import javax.swing.JTextField;

import javax.swing.text.DefaultEditorKit;

import java.awt.event.ActionEvent;

import java.awt.BorderLayout;

import java.io.File;

/** Main DrJava window.
 *  It has a menu and then a scroll pane with three components:
 *    Definitions, Output and Interactions. */
public class MainFrame extends JFrame
{
  private CompilerErrorPanel _errorPanel;
  private DefinitionsView _definitionsView;
  private OutputView _outputView;
  private InteractionsView _interactionsView;
  private JTextField _fileNameField;

  private JMenuBar _menuBar;
  private JMenu _fileMenu;
  private JMenu _editMenu;
  
  // Make some actions for menus
  private Action _quitAction = new AbstractAction("Quit")
  {
    public void actionPerformed(ActionEvent ae)
    {
      System.exit(0);
    }
  };

  private Action _openAction = new AbstractAction("Open")
  {
    public void actionPerformed(ActionEvent ae)
    {
      boolean opened = _definitionsView.open();
      if (opened) {
        _resetInteractions();
      }
    }
  };

  private Action _newAction = new AbstractAction("New")
  {
    public void actionPerformed(ActionEvent ae)
    {
      boolean createdNew = _definitionsView.newFile();
      if (createdNew) {
        _resetInteractions();
      }
    }
  };

  private Action _saveAction = new AbstractAction("Save")
  {
    // This doesn't seem to ever re-enable once disabled!
    /*
    public boolean isEnabled() {
      return ! _definitionsView.modifiedSinceSave();
    }
    */

    public void actionPerformed(ActionEvent ae)
    {
      _definitionsView.save();
    }
  };

  private Action _saveAsAction = new AbstractAction("Save as")
  {
    public void actionPerformed(ActionEvent ae)
    {
      _definitionsView.saveAs();
    }
  };

  private Action _compileAction = new AbstractAction("Compile")
  {
    // This doesn't seem to ever re-enable once disabled!
    /*
    public boolean isEnabled() {
      return _definitionsView.getDocument().getLength() > 0;
    }
    */

    public void actionPerformed(ActionEvent ae)
    {
      boolean modified = _definitionsView.modifiedSinceSave();

      if (modified) {
        // file was not saved -- tell user they must save before compiling
        String msg = "The definitions must be saved before compiling. " + 
                     "Would you like to save and compile now?";
        int rc = JOptionPane.showConfirmDialog(MainFrame.this,
                                               msg,
                                               "File not saved",
                                               JOptionPane.YES_NO_OPTION);
        if (rc == JOptionPane.YES_OPTION) {
          _definitionsView.save();
          // Check if they cancelled the save. If they did, exit!
          if (_definitionsView.modifiedSinceSave()) {
            return;
          }
        }
        else {
          return; // user wants to do nothing
        }
      }

      String filename = _definitionsView.getCurrentFileName();

      if (filename.length() == 0) {
        // the file has never been saved. we can only get here
        // if the file was never changed and never saved.
        return;
      }

      // Clear the output window before compilation
      _outputView.clear();
      
      File file = new File(filename);
      CompilerError[] errors = DrJava.compiler.compile(new File[] { file });
      _errorPanel.resetErrors(errors);

      if (errors.length == 0) {
        // Success doesn't print anything, so we should print something
        // to let them know it worked.
        System.out.println(file.getName() + " compiled successfully.");
        _resetInteractions();
      }
    }
  };

  private Action _findReplaceAction = new AbstractAction("Find/Replace")
  {
    public void actionPerformed(ActionEvent ae)
    {
      _definitionsView.findReplace();
    }
  };

  private void _resetInteractions() {
    // Reset the interactions window, and add the source directory
    // of the file we just compiled to the class path.
    _interactionsView.reset();

    String filename = _definitionsView.getCurrentFileName();

    if (filename == "") {
      return; // no file, so no source path to add to classpath.
    }

    File file = new File(filename);
    String sourceDir = file.getAbsoluteFile().getParent();
    _interactionsView.addClassPath(sourceDir);
  }

  /** Creates the main window, and shows it. */
  public MainFrame()
  {
    _fileNameField = new JTextField();
    _fileNameField.setEditable(false);

    _definitionsView = new DefinitionsView(this);
    _outputView = new OutputView();
    _errorPanel = new CompilerErrorPanel(_definitionsView);

    // Make the menu bar, and stub file and edit menus
    _menuBar = new JMenuBar();
    _fileMenu = new JMenu("File");
    _editMenu = new JMenu("Edit");

    // Add items to menus
    _fileMenu.add(_newAction);
    _fileMenu.add(_openAction);
    _fileMenu.add(_saveAction);
    _fileMenu.add(_saveAsAction);
    _fileMenu.addSeparator();
    _fileMenu.add(_compileAction);
    _fileMenu.addSeparator();
    _fileMenu.add(_quitAction);

    Action cutAction = new DefaultEditorKit.CutAction();
    cutAction.putValue(Action.NAME, "Cut");
    Action copyAction = new DefaultEditorKit.CopyAction();
    copyAction.putValue(Action.NAME, "Copy");
    Action pasteAction = new DefaultEditorKit.PasteAction();
    pasteAction.putValue(Action.NAME, "Paste");

    _editMenu.add(_definitionsView.getUndoAction());
    _editMenu.add(_definitionsView.getRedoAction());
    _editMenu.addSeparator();
    _editMenu.add(cutAction);
    _editMenu.add(copyAction);
    _editMenu.add(pasteAction);
    _editMenu.addSeparator();
    _editMenu.add(_findReplaceAction);


    // Add the menus to the menu bar
    _menuBar.add(_fileMenu);
    _menuBar.add(_editMenu);
    
    // Menu bars can actually hold anything!
    _menuBar.add(_fileNameField);

    setJMenuBar(_menuBar);
   
    // Make the output view the active one
    _outputView.makeActive();
    
    _interactionsView = new InteractionsView();

    // Create split pane with defs and output
    JSplitPane split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                       true,
                                       new JScrollPane(_definitionsView),
                                       new JScrollPane(_outputView));

    // Split2 has split1 and the interactions view
    JSplitPane split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                       true,
                                       split1,
                                       new JScrollPane(_interactionsView));

    setBounds(25, 25, 300, 500);
    setSize(300, 500);

    getContentPane().add(_errorPanel, BorderLayout.SOUTH);
    getContentPane().add(split2, BorderLayout.CENTER);


    //getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
//    show();
    setSize(300, 500);

    // This is annoyingly order-dependent. Since split2 contains split1,
    // we need to get split2's divider set up first to give split1 an overall
    // size. Then we can set split1's divider. Ahh, Swing.
    // Also, according to the Swing docs, we need to set these dividers AFTER
    // we have shown the window. How annoying.
    split2.setDividerLocation(.8);
    split1.setDividerLocation(.8);

    updateFileTitle("Untitled");
  }

  public void updateFileTitle(String filename)
  {
    setTitle(filename + " - DrJava");
    _fileNameField.setText(filename);
  }
		
		DefinitionsView getDefView()
		{
				return _definitionsView;
		}
}

