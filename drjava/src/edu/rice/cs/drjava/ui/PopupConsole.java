/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.util.Lambda;

public class PopupConsole {
  
    protected static final String INPUT_ENTERED_NAME = "Input Entered";
    protected static final String INSERT_NEWLINE_NAME = "Insert Newline";
    
    protected Frame _parentFrame;
    protected Runnable _interruptCommand;
    protected Lambda<Object,String> _insertTextCommand;

    // allows threads to wait for the console to be ready to input text externally
    private final Object CONSOLE_READY = new Object(); 
    private final Object commandLock = new Object();

  public PopupConsole(Component owner) {
    _parentFrame = JOptionPane.getFrameForComponent(owner);
  }
  
  public synchronized String getConsoleInput() {     
      if (_parentFrame.isVisible()) 
	  return showDialog() + "\n";
      else
	  return silentInput() + "\n";
  }
  
  public void interruptConsole() {
      synchronized(commandLock) {
	  if (_interruptCommand != null) _interruptCommand.run();
      }
  }

  public void insertConsoleText(String txt) { 
      if (_insertTextCommand != null) 
	  synchronized(commandLock) {
	      _insertTextCommand.apply(txt); 
	  }
      else
	  throw new IllegalStateException("Console not ready for text insertion");
  }
    
    /**
     * Causes the current thread to wait until the console is ready for input 
     * via the insertConsoleText method.
     */
    public void waitForConsoleReady() throws InterruptedException {
	synchronized(CONSOLE_READY) {
	    CONSOLE_READY.wait();
	}
    }
    
    protected String showDialog() {
	final InputBox inputBox = new InputBox();
	final JDialog dialog = createDialog(inputBox);
	synchronized(commandLock) {
	    _interruptCommand = new Runnable() {
		    public synchronized void run() { 
			dialog.setVisible(false);
		    }
		};
	    _insertTextCommand = new Lambda<Object,String>() {
		public synchronized Object apply(String input) {
		    inputBox.insert(input, inputBox.getCaretPosition());
		    return null;
		}
	    };
	}
	
	synchronized(CONSOLE_READY) {
	    CONSOLE_READY.notifyAll();
	}
      
	dialog.setVisible(true);
	dialog.dispose();
	synchronized(commandLock) {
	    _interruptCommand = null;
	    _insertTextCommand = null;
	}
	
	return inputBox.getText();
    }
  
    private JDialog createDialog(InputBox inputBox) {
	
	final JDialog dialog = new JDialog(_parentFrame, "Standard Input (System.in)", true);
    
	Container cp = dialog.getContentPane();
       	cp.add(new JScrollPane(inputBox), BorderLayout.CENTER);
	
	JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	JLabel label = new JLabel("Hit SHIFT+<Enter> For New Line  ");
	buttonPanel.add(label);
	
	Action inputEnteredAction = new AbstractAction("Done") {
		public void actionPerformed(ActionEvent e) {
		    dialog.setVisible(false);
		}
	    };    
	JButton doneButton = new JButton(inputEnteredAction);
	doneButton.setMargin(new Insets(1,5,1,5));
	buttonPanel.add(doneButton);
	dialog.getRootPane().setDefaultButton(doneButton);
	cp.add(buttonPanel, BorderLayout.SOUTH);
	
	inputBox.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), 
				   INPUT_ENTERED_NAME);
	inputBox.getActionMap().put(INPUT_ENTERED_NAME, 
				    inputEnteredAction);
	
	dialog.setSize(400,115);
	dialog.setLocationRelativeTo(_parentFrame);
	return dialog;
    }
    
    private String silentInput() {
	final Object monitor = new Object();
	final StringBuffer input = new StringBuffer();
	synchronized(commandLock) {
	    _insertTextCommand = new Lambda<Object,String>() {
		public synchronized Object apply(String s) {
		    input.append(s);
		    return null;
		}
	    };
	    _interruptCommand = new Runnable() {
		    public void run() {
			_insertTextCommand = null;
			_interruptCommand = null;
			synchronized(monitor) {
			    monitor.notify();
			}
		    }
		};
	}
	synchronized (CONSOLE_READY) {
	    CONSOLE_READY.notifyAll();
	}
	synchronized (monitor) {
	    try {
		monitor.wait();
	    } catch (InterruptedException e) { }
	}
	return input.toString();
    }

    /**
   * A box that can be inserted into the interactions pane for separate input.
   */
  class InputBox extends JTextArea {
    private static final int BORDER_WIDTH = 1;
    private static final int INNER_BUFFER_WIDTH = 3;
    private static final int OUTER_BUFFER_WIDTH = 2;
    private Color _bgColor = DrJava.getConfig().getSetting(OptionConstants.DEFINITIONS_BACKGROUND_COLOR);
    private Color _fgColor = DrJava.getConfig().getSetting(OptionConstants.DEFINITIONS_NORMAL_COLOR);
    private Color _sysInColor = DrJava.getConfig().getSetting(OptionConstants.SYSTEM_IN_COLOR);
    private boolean _antiAliasText = DrJava.getConfig().getSetting(OptionConstants.TEXT_ANTIALIAS);
    
    public InputBox() {
      setForeground(_sysInColor);
      setBackground(_bgColor);
      setCaretColor(_fgColor);
      setBorder(_createBorder());
      setLineWrap(true);

      InputMap im = getInputMap(WHEN_FOCUSED);
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,Event.SHIFT_MASK), INSERT_NEWLINE_NAME);
      
      ActionMap am = getActionMap();
      am.put(INSERT_NEWLINE_NAME, _insertNewlineAction);

      DrJava.getConfig().addOptionListener(OptionConstants.DEFINITIONS_NORMAL_COLOR,
                                           new OptionListener<Color>() {
        public void optionChanged(OptionEvent<Color> oe) {
          _fgColor = oe.value;
          setBorder(_createBorder());
          setCaretColor(oe.value);
        }
      });
      DrJava.getConfig().addOptionListener(OptionConstants.DEFINITIONS_BACKGROUND_COLOR,
                                           new OptionListener<Color>() {
        public void optionChanged(OptionEvent<Color> oe) {
          _bgColor = oe.value;
          setBorder(_createBorder());
          setBackground(oe.value);
        }
      });
      DrJava.getConfig().addOptionListener(OptionConstants.SYSTEM_IN_COLOR,
                                           new OptionListener<Color>() {
        public void optionChanged(OptionEvent<Color> oe) {
          _sysInColor = oe.value;
          setForeground(oe.value);
        }
      });
      DrJava.getConfig().addOptionListener(OptionConstants.TEXT_ANTIALIAS,
                                           new OptionListener<Boolean>() {
        public void optionChanged(OptionEvent<Boolean> oce) {
          _antiAliasText = oce.value.booleanValue();
          InputBox.this.repaint();
        }
      });
      System.out.println("document class: " + getDocument().getClass());
    }
    private Border _createBorder() {
      Border outerouter = BorderFactory.createLineBorder(_bgColor, OUTER_BUFFER_WIDTH);
      Border outer = BorderFactory.createLineBorder(_fgColor, BORDER_WIDTH);
      Border inner = BorderFactory.createLineBorder(_bgColor, INNER_BUFFER_WIDTH);
      Border temp = BorderFactory.createCompoundBorder(outer, inner);
      return BorderFactory.createCompoundBorder(outerouter, temp);
    }
    /**
     * Enable anti-aliased text by overriding paintComponent.
     */
    protected void paintComponent(Graphics g) {
      if (_antiAliasText && g instanceof Graphics2D) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      }
      super.paintComponent(g);
    }
    /**
     * Shift-Enter action in a System.in box.  Inserts a newline.
     */
    private Action _insertNewlineAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        insert("\n", getCaretPosition());
      }
    };
  }
}
