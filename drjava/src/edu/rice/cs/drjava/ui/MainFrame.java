/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.BoxLayout;
import javax.swing.JTextField;

import java.awt.event.ActionEvent;

/** Main DrJava window.
 *  It has a menu and then a scroll pane with three components:
 *    Definitions, Output and Interactions. */
public class MainFrame extends JFrame
{
  // Generated automatically when you check out with tag name!
  public static final String DRJAVA_BUILD = "$Name$";

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
      _definitionsView.open();
    }
  };

  private Action _newAction = new AbstractAction("New")
  {
    public void actionPerformed(ActionEvent ae)
    {
      _definitionsView.newFile();
    }
  };

  private Action _saveAction = new AbstractAction("Save")
  {
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


  /** Creates the main window, and shows it. */
  public MainFrame()
  {
    _fileNameField = new JTextField();
    _fileNameField.setEditable(false);

    // Make the menu bar, and stub file and edit menus
    _menuBar = new JMenuBar();
    _fileMenu = new JMenu("File");
    _editMenu = new JMenu("Edit");

    // Add items to menus
    _fileMenu.add(_newAction);
    _fileMenu.add(_openAction);
    _fileMenu.add(_saveAction);
    _fileMenu.add(_saveAsAction);
    _fileMenu.add(_quitAction);

    // Add the menus to the menu bar
    _menuBar.add(_fileMenu);
    _menuBar.add(_editMenu);
    _menuBar.add(_fileNameField);

    setJMenuBar(_menuBar);


    _definitionsView = new DefinitionsView(this);
    _outputView = new OutputView();
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

    getContentPane().add(split2);


    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
    show();
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

  public static void main(String[] args)
  {
    MainFrame mf = new MainFrame();
  }
}

