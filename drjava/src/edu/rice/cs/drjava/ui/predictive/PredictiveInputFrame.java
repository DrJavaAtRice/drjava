/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui.predictive;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import edu.rice.cs.util.Lambda;

/**
 * Frame with predictive string input based on a list of strings.
 */
public class PredictiveInputFrame<T extends Comparable<? super T>> extends JFrame {
  /**
   * Interface that is used to generate additional information about an item.
   */
  public static interface InfoSupplier<X> extends Lambda<String, X> {
    public String apply(X param);
  }

  /**
   * General information supplier that just uses toString().
   */
  public static final InfoSupplier<Object> TO_STRING_SUPPLIER = new InfoSupplier<Object>() {
    public String apply(Object param) {
      return param.toString();
    }
  };
  
  /**
   * Interface for an action to be performed when the user closes the frame,
   * either by using "OK" or "Cancel".
   */
  public static interface CloseAction<X extends Comparable<? super X>> extends Lambda<Object, PredictiveInputFrame<X>> {
    public Object apply(PredictiveInputFrame<X> param);
  }
  
  /**
   * Class to save the frame state, i.e. location and dimensions.
   */
  public static class FrameState {
    private Dimension _dim;
    private Point _loc;
    private int _currentStrategyIndex;
    public FrameState(Dimension d, Point l, int currentStrategyIndex) {
      _dim = d;
      _loc = l;
      _currentStrategyIndex = currentStrategyIndex;
    }
    public FrameState(String s) {
      StringTokenizer tok = new StringTokenizer(s);
      try {
        int x = Integer.valueOf(tok.nextToken());
        int y = Integer.valueOf(tok.nextToken());
        _dim = new Dimension(x, y);
        x = Integer.valueOf(tok.nextToken());
        y = Integer.valueOf(tok.nextToken());
        _loc = new Point(x, y);
        _currentStrategyIndex = Integer.valueOf(tok.nextToken());
      }
      catch(NoSuchElementException nsee) {
        throw new IllegalArgumentException("Wrong FrameState string: " + nsee);
      }
      catch(NumberFormatException nfe) {
        throw new IllegalArgumentException("Wrong FrameState string: " + nfe);
      }
    }
    public FrameState(PredictiveInputFrame comp) {
      _dim = comp.getSize();
      _loc = comp.getLocation();
      _currentStrategyIndex = comp._strategies.indexOf(comp._currentStrategy);
    }
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append((int)_dim.getWidth());
      sb.append(' ');
      sb.append((int)_dim.getHeight());
      sb.append(' ');
      sb.append(_loc.x);
      sb.append(' ');
      sb.append(_loc.y);
      sb.append(' ');
      sb.append(_currentStrategyIndex);
      return sb.toString();
    }
    public Dimension getDimension() { return _dim; }
    public Point getLocation() { return _loc; }
    public int getCurrentStrategyIndex() { return _currentStrategyIndex; }
  }

  /**
   * Predictive input model
   */
  private PredictiveInputModel<T> _pim;

  /**
   * Code for the last button that was pressed.
   */
  private int _buttonPressed;

  /**
   * Ok button.
   */
  private JButton _okButton;
  
  /**
   * Text field for string input.
   */
  private JTextField _textField;

  /**
   * List with matches.
   */
  private JList _matchList;

  /**
   * True if the user is forced to select one of the items.
   */
  private boolean _force;

  /**
   * Label with "Tab completes:" string.
   */
  private JLabel _tabCompletesLabel;
  
  /**
   * Label with shared extension.
   */
  private JLabel _sharedExtLabel;

  /**
   * Listener for several events.
   */
  private PredictiveInputListener _listener;

  /**
   * Info supplier.
   */
  InfoSupplier<? super T> _info = TO_STRING_SUPPLIER;

  /**
   * Text area for additional information.
   */
  private JLabel _infoLabel;
  
  /**
   * Last frame state. It can be stored and restored.
   */
  private FrameState _lastState = null;
  
  /**
   * Owner frame.
   */
  private Frame _owner;
  
  /**
   * Action to be performed when the user closes the frame using "OK".
   */
  private CloseAction<T> _okAction;
  
  /**
   * Action to be performed when the user closes the frame using "Cancel".
   */
  private CloseAction<T> _cancelAction;
  
  /**
   * Array of strategies.
   */
  private java.util.List<PredictiveInputModel.MatchingStrategy<T>> _strategies;
  
  /**
   * Currently used strategy.
   */
  private PredictiveInputModel.MatchingStrategy<T> _currentStrategy;
  
  /**
   * Combo box.
   */
  private JComboBox _strategyBox;

  /**
   * Create a new predictive string input frame.
   * @param owner owner frame
   * @param force true if the user is forced to select one of the items
   * @param ignoreCase true if case should be ignored
   * @param info information supplier to use for additional information display
   * @param strategies array of matching strategies
   * @param okAction action to be performed when the user closes the frame using "OK"
   * @param cancelAction action to be performed when the user closes the frame using "Cancel"
   * @param items list of items
   */
  public PredictiveInputFrame(Frame owner, String title, boolean force, boolean ignoreCase, InfoSupplier<? super T> info, 
                              java.util.List<PredictiveInputModel.MatchingStrategy<T>> strategies,
                              CloseAction<T> okAction, CloseAction<T> cancelAction, java.util.List<T> items) {
    super(title);
    if (info==null) {
      throw new IllegalArgumentException("info is null");
    }
    _strategies = strategies;
    _currentStrategy = _strategies.get(0);
    _pim = new PredictiveInputModel<T>(ignoreCase,
                                       _currentStrategy,
                                       items);
    _force = force;
    _info = info;
    _owner = owner;
    _okAction = okAction;
    _cancelAction = cancelAction;
    init(true);
  }

  /**
   * Create a new predictive string input frame.
   * @param owner owner frame
   * @param force true if the user is forced to select one of the items
   * @param info information supplier to use for additional information display
   * @param strategies array of matching strategies
   * @param okAction action to be performed when the user closes the frame using "OK"
   * @param cancelAction action to be performed when the user closes the frame using "Cancel"
   * @param items varargs/array of items
   */
  public PredictiveInputFrame(Frame owner, String title, boolean force, boolean ignoreCase, InfoSupplier<? super T> info, 
                              java.util.List<PredictiveInputModel.MatchingStrategy<T>> strategies,
                              CloseAction<T> okAction, CloseAction<T> cancelAction, T... items) {
    super(title);
    if (info==null) {
      throw new IllegalArgumentException("info is null");
    }
    _strategies = strategies;
    _currentStrategy = _strategies.get(0);
    _pim = new PredictiveInputModel<T>(ignoreCase,
                                       _currentStrategy,
                                       items);
    _force = force;
    _info = info;
    _owner = owner;
    _okAction = okAction;
    _cancelAction = cancelAction;
    init(true);
  }
  
  /**
   * Returns the last state of the frame, i.e. the location and dimension.
   * @return frame state
   */
  public FrameState getFrameState() {
    return _lastState;
  }
  
  /**
   * Sets state of the frame, i.e. the location and dimension of the frame for the next use.
   * @param state, or null to reset
   */
  public void setFrameState(FrameState ds) {
    _lastState = ds;
    if (_lastState!=null) {
      setSize(_lastState.getDimension());
      setLocation(_lastState.getLocation());
      int index = _lastState.getCurrentStrategyIndex();
      if ((index>=0) && (index<_strategies.size())) {
        _currentStrategy = _strategies.get(index);
        _strategyBox.setSelectedIndex(index);
      }
      selectStrategy();
      validate();
    }
  }  
  
  /**
   * Sets state of the frame, i.e. the location and dimension of the frame for the next use.
   * @param state, or null to reset
   */
  public void setFrameState(String s) {
    try {
      _lastState = new FrameState(s);
    }
    catch(IllegalArgumentException e) {
      _lastState = null;
    }
    if (_lastState!=null) {
      setSize(_lastState.getDimension());
      setLocation(_lastState.getLocation());
      int index = _lastState.getCurrentStrategyIndex();
      if ((index>=0) && (index<_strategies.size())) {
        _currentStrategy = _strategies.get(index);
        _strategyBox.setSelectedIndex(index);
      }
      selectStrategy();
      validate();
    }
    else {
      Dimension parentDim = (_owner!=null)?(_owner.getSize()):getToolkit().getScreenSize();
      int xs = (int)parentDim.getWidth()/3;
      int ys = (int)parentDim.getHeight()/4;
      setSize(Math.max(xs,400), Math.max(ys, 300));
      setLocationRelativeTo(_owner);
      _currentStrategy = _strategies.get(0);
      _strategyBox.setSelectedIndex(0);
      selectStrategy();
    }
  }

  /**
   * Set the predictive input model.
   * @param ignoreCase true if case should be ignored
   * @param pim predictive input model
   */
  public void setModel(boolean ignoreCase, PredictiveInputModel<T> pim) {
    _pim = new PredictiveInputModel<T>(ignoreCase, pim);
    removeListener();
    updateTextField();
    updateExtensionLabel();
    updateList();
    addListener();
  }

  /**
   * Set the items.
   * @param ignoreCase true if case should be ignored
   * @param items list of items
   */
  public void setItems(boolean ignoreCase, java.util.List<T> items) {
    _pim = new PredictiveInputModel<T>(ignoreCase, _currentStrategy, items);
    removeListener();
    updateTextField();
    updateExtensionLabel();
    updateList();
    addListener();
  }
  
  /**
   * Set the currently selected item.
   * @param item item to select
   */
  public void setCurrentItem(T item) {
    _pim.setCurrentItem(item);
    removeListener();
    updateTextField();
    updateExtensionLabel();
    updateList();
    addListener();
  }

  /**
   * Set the items.
   * @param ignoreCase true if case should be ignored
   * @param items varargs/array of items
   */
  public void setItems(boolean ignoreCase, T... items) {
    _pim = new PredictiveInputModel<T>(ignoreCase, _currentStrategy, items);
    removeListener();
    updateTextField();
    updateExtensionLabel();
    updateList();
    addListener();
  }

  /**
   * Return the code for the last button that was pressed.
   * This will be either JOptionPane.OK_OPTION or JOptionPane.CANCEL_OPTION.
   * @return button code
   */
  public int getButtonPressed() {
    return _buttonPressed;
  }

  /**
   * Return the string that was entered in the text field.
   * If the user is forced to select an item, then the text of the item will be returned.
   * @return text in text field
   */
  public String getText() {
    if (_force) {
      return _pim.getCurrentItem().toString();
    }
    return _textField.getText();
  }

  /**
   * Return the item that was selected or null the user entered a mask not in the list and force == false.
   * @return item that was selected or null
   */
  public T getItem() {
    if (!_force) {
      if (_pim.getMatchingItems().size()==0) {
        return null;
      }
    }
    return _pim.getCurrentItem();
  }

  /**
   * Initialize the frame.
   * @param info true if additional information is desired
   */
  private void init(boolean info) {
    _buttonPressed = JOptionPane.CANCEL_OPTION;
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(WindowEvent winEvt) {
        cancelButtonPressed();
      }
    });
    addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        validate();
        _matchList.ensureIndexIsVisible(_matchList.getSelectedIndex());
      }
    });

    // buttons
    _okButton = new JButton("OK");
    _okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okButtonPressed();
      }
    });
    getRootPane().setDefaultButton(_okButton);

    final JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButtonPressed();
      }
    });
    
    _strategyBox = new JComboBox(_strategies.toArray());
    _strategyBox.setEditable(false);
    _strategyBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // System.out.println("set strategy!");
        selectStrategy();
      }
    });
    _strategyBox.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
      }

      public void focusLost(FocusEvent e) {
        if ((e.getOppositeComponent()!=_textField) && 
            (e.getOppositeComponent()!=_okButton) && 
            (e.getOppositeComponent()!=cancelButton)) {
          _textField.requestFocus();
        }
      }
    });

    // text field
    _textField = new JTextField();
    _textField.setDragEnabled(false);
    _textField.setFocusTraversalKeysEnabled(false);

    _listener = new PredictiveInputListener();
    addListener();

    Keymap ourMap = JTextComponent.addKeymap("PredictiveInputFrame._textField", _textField.getKeymap());
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
//        System.out.println("esc!");
        cancelButtonPressed();
      }
    });
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
//        System.out.println("enter!");
        okButtonPressed();
      }
    });
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
//        System.out.println("tab!");
        removeListener();
        _pim.extendMask(_pim.getSharedMaskExtension());
        updateTextField();
        updateExtensionLabel();
        updateList();
        addListener();
      }
    });
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
//        System.out.println("up!");
        if (_matchList.getModel().getSize()>0) {
          removeListener();
          int i = _matchList.getSelectedIndex();
          if (i>0) {
            _matchList.setSelectedIndex(i-1);
            _matchList.ensureIndexIsVisible(i-1);
            _pim.setCurrentItem(_pim.getMatchingItems().get(i-1));
            updateInfo();
          }
          addListener();
        }
      }
    });
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
//        System.out.println("down!");
        if (_matchList.getModel().getSize()>0) {
          removeListener();
          int i = _matchList.getSelectedIndex();
          if (i<_matchList.getModel().getSize()-1) {
            _matchList.setSelectedIndex(i+1);
            _matchList.ensureIndexIsVisible(i+1);
            _pim.setCurrentItem(_pim.getMatchingItems().get(i+1));
            updateInfo();
          }
          addListener();
        }
      }
    });
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
//        System.out.println("page up!");
        if (_matchList.getModel().getSize()>0) {
          removeListener();
          int page = _matchList.getLastVisibleIndex() - _matchList.getFirstVisibleIndex() + 1;
          int i = _matchList.getSelectedIndex() - page;
          if (i<0) {
            i = 0;
          }
          _matchList.setSelectedIndex(i);
          _matchList.ensureIndexIsVisible(i);
          _pim.setCurrentItem(_pim.getMatchingItems().get(i));
          updateInfo();
          addListener();
        }
      }
    });
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
//        System.out.println("page down!");
        if (_matchList.getModel().getSize()>0) {
          removeListener();
          int page = _matchList.getLastVisibleIndex() - _matchList.getFirstVisibleIndex() + 1;
          int i = _matchList.getSelectedIndex() + page;
          if (i>=_matchList.getModel().getSize()) {
            i = _matchList.getModel().getSize()-1;
          }
          _matchList.setSelectedIndex(i);
          _matchList.ensureIndexIsVisible(i);
          _pim.setCurrentItem(_pim.getMatchingItems().get(i));
          updateInfo();
          addListener();
        }
      }
    });
    _textField.setKeymap(ourMap);
    
//    _textField.addKeyListener(new KeyAdapter() {
//      public void keyTyped(KeyEvent e) {
//        char c = e.getKeyChar();
//        if ((c != KeyEvent.VK_DELETE) && (c != KeyEvent.VK_BACK_SPACE) && (c >= 32)) {
//          String oldMask = _pim.getMask();
//          String newMask = oldMask.substring(0, _textField.getCaretPosition()) + c + oldMask.substring(_textField.getCaretPosition());
//          _pim.setMask(newMask);
//          if (_force && (_pim.getMatchingItems().size()==0)) {
//            Toolkit.getDefaultToolkit().beep();
//            e.consume();
//          }
//          _pim.setMask(oldMask);
//        }
//      }
//    });

    _textField.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
      }

      public void focusLost(FocusEvent e) {
        if ((e.getOppositeComponent()!=_strategyBox) && 
            (e.getOppositeComponent()!=_okButton) && 
            (e.getOppositeComponent()!=cancelButton)) {
          _textField.requestFocus();
        }
      }
    });

    _matchList = new JList(_pim.getMatchingItems().toArray());
    _matchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _matchList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
//        System.out.println("click!");
        removeListener();
        int i = _matchList.getSelectedIndex();
        if (i >= 0) {
          _pim.setCurrentItem(_pim.getMatchingItems().get(i));
          _matchList.ensureIndexIsVisible(i);
          updateInfo();
        }
        addListener();
      }
    });
    
    // put everything together
    Container contentPane = getContentPane();
    
    GridBagLayout layout = new GridBagLayout();
    contentPane.setLayout(layout);
    
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.weightx = 1.0;
    c.weighty = 0.0;
    c.gridwidth = GridBagConstraints.REMAINDER; // end row
    c.insets.set(2,2,2,2);
    
    _infoLabel = new JLabel("");
    if (info) {
      c.fill = GridBagConstraints.NONE;
      contentPane.add(_infoLabel, c);
    }

    c.fill = GridBagConstraints.BOTH;
    c.weighty = 1.0;
    contentPane.add(new JScrollPane(_matchList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), c);
    
    c.anchor = GridBagConstraints.SOUTHWEST;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    _tabCompletesLabel = new JLabel("Tab completes: ");
    contentPane.add(_tabCompletesLabel, c);
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    _sharedExtLabel = new JLabel("");
    contentPane.add(_sharedExtLabel, c);
    
    contentPane.add(_textField, c);
    
    c.anchor = GridBagConstraints.SOUTH;
    
    JPanel buttonPanel = new JPanel(new GridBagLayout());
    GridBagConstraints bc = new GridBagConstraints();
    bc.insets.set(0,2,0,2);
    buttonPanel.add(new JLabel("Matching strategy:"), bc);
    buttonPanel.add(_strategyBox, bc);
    buttonPanel.add(_okButton, bc);
    buttonPanel.add(cancelButton, bc);
    
    contentPane.add(buttonPanel, c);

    Dimension parentDim = (_owner!=null)?(_owner.getSize()):getToolkit().getScreenSize();
    int xs = (int)parentDim.getWidth()/3;
    int ys = (int)parentDim.getHeight()/4;
    setSize(Math.max(xs,400), Math.max(ys, 300));
    setLocationRelativeTo(_owner);

    removeListener();
    updateTextField();
    addListener();
    updateList();
  }
  
  /**
   * Validates before changing visibility,
   * @param b true if frame should be shown, false if it should be hidden.
   */
  public void setVisible(boolean b) {
    validate();
    _owner.setEnabled(!b);
    super.setVisible(b);
    if (b) {
      _textField.requestFocus();
    }
    else {
      _owner.toFront();
    }
  }

  /**
   * Add the listener.
   */
  private void addListener() {
    _textField.getDocument().addDocumentListener(_listener);
    _textField.addCaretListener(_listener);
  }

  /**
   * Remove the listener.
   */
  private void removeListener() {
    _textField.getDocument().removeDocumentListener(_listener);
    _textField.removeCaretListener(_listener);
  }

  /**
   * Update the text field based on the model.
   */
  private void updateTextField() {
    _textField.setText(_pim.getMask());
    _textField.setCaretPosition(_pim.getMask().length());
  }

  /**
   * Update the extension label based on the model.
   */
  private void updateExtensionLabel() {
    _sharedExtLabel.setText(_pim.getSharedMaskExtension()+" ");
    _tabCompletesLabel.setVisible(_pim.getSharedMaskExtension().length()>0);
  }

  /**
   * Update the match list based on the model.
   */
  private void updateList() {
    _matchList.setListData(_pim.getMatchingItems().toArray());
    _matchList.setSelectedValue(_pim.getCurrentItem(), true);
    updateExtensionLabel();
    updateInfo();
    _okButton.setEnabled(_matchList.getModel().getSize()>0);
  }

  /**
   * Update the information.
   */
  private void updateInfo() {
    if (_matchList.getModel().getSize()>0) {
      _infoLabel.setText("Path:   " + _info.apply(_pim.getCurrentItem()));
    }
    else {
      _infoLabel.setText("No file selected");
    }
  }
  
  /**
   * Handle ok button.
   */
  private void okButtonPressed() {
    if (_matchList.getModel().getSize()>0) {
      _buttonPressed = JOptionPane.OK_OPTION;
      _lastState = new FrameState(PredictiveInputFrame.this);
      setVisible(false);
      _okAction.apply(this);
    }
    else {
      Toolkit.getDefaultToolkit().beep();
    }
  }
  
  /**
   * Handle cancel button.
   */
  private void cancelButtonPressed() {
    _buttonPressed = JOptionPane.CANCEL_OPTION;
    _lastState = new FrameState(PredictiveInputFrame.this);
    setVisible(false);
    _cancelAction.apply(this);
  }
  
  /**
   * Select the strategy for matching.
   */
  private void selectStrategy() {
    _currentStrategy = _strategies.get(_strategyBox.getSelectedIndex());
    removeListener();
    _pim.setStrategy(_currentStrategy);
    updateTextField();
    updateExtensionLabel();
    updateList();
    addListener();
    _textField.requestFocus();
  }

  /**
   * Listener for several events.
   */
  private class PredictiveInputListener implements CaretListener, DocumentListener {
    public void insertUpdate(DocumentEvent e) {
//      System.out.println("insertUpdate fired!");
      removeListener();
      _pim.setMask(_textField.getText());
      updateExtensionLabel();
      updateList();
      addListener();
    }

    public void removeUpdate(DocumentEvent e) {
//      System.out.println("removeUpdate fired!");
      removeListener();
      _pim.setMask(_textField.getText());
      updateExtensionLabel();
      updateList();
      addListener();
    }

    public void changedUpdate(DocumentEvent e) {
//      System.out.println("changedUpdate fired!");
      removeListener();
      _pim.setMask(_textField.getText());
      updateExtensionLabel();
      updateList();
      addListener();
    }

    public void caretUpdate(CaretEvent e) {
    }
  }

  public static void main(String[] args) {
    Frame frame = JOptionPane.getFrameForComponent(null);
    InfoSupplier<String> info = new InfoSupplier<String>() {
      public String apply(String t) {
        StringBuilder sb = new StringBuilder();
        sb.append(t);
        sb.append("\nLength = ");
        sb.append(t.length());
        sb.append("\nHashcode = ");
        sb.append(t.hashCode());
        for(int i=0;i<5;++i) {
          sb.append("\n"+t);
        }
        return sb.toString();
      }
    };
    CloseAction<String> okAction = new CloseAction<String>() {
      public Object apply(PredictiveInputFrame<String> p) {
        System.out.println("User pressed Ok");
        System.out.println("Text = "+p.getText());
        System.out.println("Item = "+p.getItem());
        return null;
      }
    };
    CloseAction<String> cancelAction = new CloseAction<String>() {
      public Object apply(PredictiveInputFrame<String> p) {
        System.out.println("User pressed Cancel");
        return null;
      }
    };
    java.util.ArrayList<PredictiveInputModel.MatchingStrategy<String>> strategies =
      new java.util.ArrayList<PredictiveInputModel.MatchingStrategy<String>>();
    strategies.add(new PredictiveInputModel.PrefixStrategy<String>());
    strategies.add(new PredictiveInputModel.FragmentStrategy<String>());
    strategies.add(new PredictiveInputModel.RegExStrategy<String>());
    PredictiveInputFrame<String> pif = new PredictiveInputFrame<String>(frame,
                                                                        "Go to file",
                                                                        true,
                                                                        true,
                                                                        info,
                                                                        strategies,
                                                                        okAction,
                                                                        cancelAction,
                                                                        "AboutDialog.java",
                                                                        "FileOps.java",
                                                                        "FileOpsTest.java",
                                                                        "Utilities.java");
    pif.setVisible(true);
  }
}
