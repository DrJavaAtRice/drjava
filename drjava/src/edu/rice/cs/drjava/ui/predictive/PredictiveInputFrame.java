/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui.predictive;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.drjava.DrJavaRoot;

/** Frame with predictive string input based on a list of strings. */
public class PredictiveInputFrame<T extends Comparable<? super T>> extends SwingFrame {
  
  /** Interface that is used to generate additional information about an item. */
  public static interface InfoSupplier<X> extends Lambda<X,String> { }

//  /** General information supplier that just uses toString(). */
//  public static final InfoSupplier<Object> GET_LAZY_SUPPLIER = new InfoSupplier<Object>() {
//    public String apply(Object param) { return param.toString(); }
//  };
  
  /** Interface for an action to be performed when the user closes the frame,
   *  either by using "OK" or "Cancel".
   */
  public static interface CloseAction<X extends Comparable<? super X>> extends Lambda<PredictiveInputFrame<X>,Object> {
    public Object value(PredictiveInputFrame<X> param);
    public String getName();
    public KeyStroke getKeyStroke(); // or null if none desired
    public String getToolTipText(); // or null if none desired
  }
  
  /** Class to save the frame state, i.e. location and dimensions.*/
  public static class FrameState {
    private volatile Dimension _dim;
    private volatile Point _loc;
    private volatile int _currentStrategyIndex;
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
      catch(NullPointerException npe) {
        throw new IllegalArgumentException("Wrong FrameState string: " + npe);
      }
      catch(NoSuchElementException nsee) {
        throw new IllegalArgumentException("Wrong FrameState string: " + nsee);
      }
      catch(NumberFormatException nfe) {
        throw new IllegalArgumentException("Wrong FrameState string: " + nfe);
      }
    }
    public FrameState(PredictiveInputFrame<?> comp) {
      _dim = comp.getSize();
      _loc = comp.getLocation();
      _currentStrategyIndex = comp._strategies.indexOf(comp._currentStrategy);
    }
    public String toString() {
      final StringBuilder sb = new StringBuilder();
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

  /** Predictive input model */
  private volatile PredictiveInputModel<T> _pim;

  /** Code for the last button that was pressed.*/
  private volatile String _buttonPressed;

  /** Action buttons.*/
  private final JButton[] _buttons;
  
  /** Text field for string input. */
  private final JTextField _textField = new JTextField();
  
  /** Panel for additional options. */
  protected JPanel _optionsPanel;
  
  /** Optional components for the _optionsPanel. */
  protected JComponent[] _optionalComponents;
  
  /** Label with "Tab completes:" string. */
  private final JLabel _tabCompletesLabel = new JLabel("Tab completes: ");

  /** List with matches. */
  private final JList _matchList;

  /** True if the user is forced to select one of the items. */
  private final boolean _force;
  
  /** Label with shared extension.*/
  private final JLabel _sharedExtLabel = new JLabel("");

  /** Listener for several events. */
  private final PredictiveInputListener _listener = new PredictiveInputListener();

  /** Info supplier. */
  private final InfoSupplier<? super T> _info;

  /** Text area for additional information. */
  private final JLabel _infoLabel = new JLabel("");
  
  /** Owner frame. */
  private final SwingFrame _owner;
  
  /** Action to be performed when the user closes the frame using "OK". */
  private final ArrayList<CloseAction<T>> _actions;
  
  /** The index in the _actions list that cancels the dialog. */
  private final int _cancelIndex;
  
  /** Array of strategies. */
  private final java.util.List<PredictiveInputModel.MatchingStrategy<T>> _strategies;
  
  /** Combo box. */
  private final JComboBox _strategyBox;
  
  /** Last frame state. It can be stored and restored. */
  private volatile FrameState _lastState;
  
  /** Currently used strategy. */
  private volatile PredictiveInputModel.MatchingStrategy<T> _currentStrategy;

  /** Create a new predictive string input frame.
   *  @param owner owner frame
   *  @param force true if the user is forced to select one of the items
   *  @param ignoreCase true if case should be ignored
   *  @param info information supplier to use for additional information display
   *  @param strategies array of matching strategies
   *  @param actions actions to be performed when the user closes the frame, e.g. "OK" and "Cancel"; "Cancel" has to be last
   *  @param items list of items
   */

  public PredictiveInputFrame(SwingFrame owner, String title, boolean force, boolean ignoreCase, InfoSupplier<? super T> info, 
                              java.util.List<PredictiveInputModel.MatchingStrategy<T>> strategies,
                              java.util.List<CloseAction<T>> actions, int cancelIndex, Collection<T> items) {
    super(title);
    _strategies = strategies;
    _strategyBox = new JComboBox(_strategies.toArray());
    _currentStrategy = _strategies.get(0);
    _pim = new PredictiveInputModel<T>(ignoreCase, _currentStrategy, items);
    _matchList = new JList(_pim.getMatchingItems().toArray());
    _force = force;
    _info = info;
    _lastState = null;
    _owner = owner;
    _actions = new ArrayList<CloseAction<T>>(actions);
    _buttons = new JButton[actions.size()];
    _cancelIndex = cancelIndex;
    init(_info != null);
    initDone(); // call mandated by SwingFrame contract
  }
  
  /** Create a new predictive string input frame.
   *  @param owner owner frame
   *  @param force true if the user is forced to select one of the items
   *  @param info information supplier to use for additional information display
   *  @param strategies array of matching strategies
   *  @param actions actions to be performed when the user closes the frame, e.g. "OK" and "Cancel"; "Cancel" has to be last
   *  @param items varargs/array of items
   */
  public PredictiveInputFrame(SwingFrame owner, String title, boolean force, boolean ignoreCase, InfoSupplier<? super T> info, 
                              List<PredictiveInputModel.MatchingStrategy<T>> strategies,
                              java.util.List<CloseAction<T>> actions, int cancelIndex, T... items) {
    this(owner, title, force, ignoreCase, info, strategies, actions, cancelIndex, Arrays.asList(items));
  }
  
  /** Returns the last state of the frame, i.e. the location and dimension.
   *  @return frame state
   */
  public FrameState getFrameState() { return _lastState; }
  
  /** Sets state of the frame, i.e. the location and dimension of the frame for the next use.
   *  @param ds  State to update to, or {@code null} to reset
   */
  public void setFrameState(FrameState ds) {
    _lastState = ds;
    if (_lastState != null) {
      setSize(_lastState.getDimension());
      setLocation(_lastState.getLocation());
      int index = _lastState.getCurrentStrategyIndex();
      if ((index >= 0) && (index < _strategies.size())) {
        _currentStrategy = _strategies.get(index);
        _strategyBox.setSelectedIndex(index);
      }
      selectStrategy();
      validate();
    }
  }  
  
  /** Sets state of the frame, i.e. the location and dimension of the frame for the next use.
   *  @param s  State to update to, or {@code null} to reset
   */
  public void setFrameState(String s) {
    try { _lastState = new FrameState(s); }
    catch(IllegalArgumentException e) { _lastState = null; }
    if (_lastState != null) {
      setSize(_lastState.getDimension());
      setLocation(_lastState.getLocation());
      int index = _lastState.getCurrentStrategyIndex();
      if ((index >= 0) && (index < _strategies.size())) {
        _currentStrategy = _strategies.get(index);
        _strategyBox.setSelectedIndex(index);
      }
      selectStrategy();
      validate();
    }
    else {
      Dimension parentDim = (_owner != null) ? _owner.getSize() : getToolkit().getScreenSize();
      //int xs = (int)parentDim.getWidth()/3;
      int ys = (int) parentDim.getHeight()/4;
      // in line below, parentDim was _owner.getSize(); changed because former could generate NullPointerException
      setSize(new Dimension((int)getSize().getWidth(), (int) Math.min(parentDim.getHeight(), Math.max(ys, 300))));
      if (_owner!=null) { setLocationRelativeTo(_owner); }
      _currentStrategy = _strategies.get(0);
      _strategyBox.setSelectedIndex(0);
      selectStrategy();
    }
  }

  /** Return a copy of the list of items in the model.
    * @return list of items */
  public List<T> getItems() { return _pim.getItems(); }

  /** Set the predictive input model.
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

  /** Set the items.
    * @param ignoreCase true if case should be ignored
    * @param items list of items
    */
  public void setItems(boolean ignoreCase, Collection<T> items) {
    _pim = new PredictiveInputModel<T>(ignoreCase, _currentStrategy, items);
    removeListener();
    updateTextField();
    updateExtensionLabel();
    updateList();
    addListener();
  }
  
  /** Set the currently selected item.
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

  /** Set the items.
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

  /** Return the name for the last button that was pressed.
    * @return button name
    */
  public String getButtonPressed() {
    return _buttonPressed;
  }
  
  /** Return the raw, unforced text in the text field.
    * @return text in text field */
  public String getMask() {
    return _textField.getText();
  }

  /** Set the mask in the text field.
    * @param mask for text field*/
  public void setMask(String mask) {
    _pim.setMask(mask);
    removeListener();
    updateTextField();
    updateExtensionLabel();
    updateList();
    addListener();
  }

  /** Return the string that was entered in the text field.
    * If the user is forced to select an item, then the text of the item will be returned.
    * @return text in text field
    */
  public String getText() {
    if (_force) {
      @SuppressWarnings("unchecked") 
      T item = (T)_matchList.getSelectedValue();
      return (item == null) ? "" : _currentStrategy.force(item,_textField.getText());
    }
    return _textField.getText();
  }

  /** Return the item that was selected or null the user entered a mask not in the list and force == false.
    * @return item that was selected or null
    */
  public T getItem() {
    if (!_force && _pim.getMatchingItems().size() == 0) return null;
    @SuppressWarnings("unchecked") 
    T item = (T)_matchList.getSelectedValue();
    return item;
  }

  /** Initialize the frame.
   *  @param info true if additional information is desired
   */
  private void init(boolean info) {
    _buttonPressed = null;
    addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        validate();
        _matchList.ensureIndexIsVisible(_matchList.getSelectedIndex());
      }
    });

    // buttons
    int i = 0;
    for (final CloseAction<T> a: _actions) {
      _buttons[i] = new JButton(a.getName());
      final String tooltip = a.getToolTipText();
      if (tooltip != null) { _buttons[i].setToolTipText(tooltip); }
      _buttons[i].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) { buttonPressed(a); }
      });
      ++i;
    }

    getRootPane().setDefaultButton(_buttons[0]);

    _strategyBox.setEditable(false);
    _strategyBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // System.out.println("set strategy!");
        selectStrategy();
      }
    });
    _strategyBox.addFocusListener(new FocusAdapter() {

      public void focusLost(FocusEvent e) {
        boolean bf = false;
        for (JButton b: _buttons) { if (e.getOppositeComponent() == b) { bf = true; break; } }
        if ((e.getOppositeComponent() != _textField) && (!bf)) {
          for(JComponent c: _optionalComponents) {
            if (e.getOppositeComponent() == c) { return; }
          }
          _textField.requestFocus();
        }
      }
    });

    // text field
    _textField.setDragEnabled(false);
    _textField.setFocusTraversalKeysEnabled(false);

    addListener();

    Keymap ourMap = JTextComponent.addKeymap("PredictiveInputFrame._textField", _textField.getKeymap());
    for (final CloseAction<T> a: _actions) {
      KeyStroke ks = a.getKeyStroke();
      if (ks != null) {
        ourMap.addActionForKeyStroke(ks, new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            buttonPressed(a);
          }
        });
      }
    }
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
//        System.out.println("tab!");
        removeListener();
        _pim.extendSharedMask();
        updateTextField();
        updateExtensionLabel();
        updateList();
        addListener();
      }
    });
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
//        System.out.println("up!");
        if (_matchList.getModel().getSize() > 0) {
          removeListener();
          int i = _matchList.getSelectedIndex();
          if (i > 0) {
            _matchList.setSelectedIndex(i - 1);
            _matchList.ensureIndexIsVisible(i - 1);
            _pim.setCurrentItem(_pim.getMatchingItems().get(i - 1));
            updateInfo();
          }
          addListener();
        }
      }
    });
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
//        System.out.println("down!");
        if (_matchList.getModel().getSize() > 0) {
          removeListener();
          int i = _matchList.getSelectedIndex();
          if (i < _matchList.getModel().getSize() - 1) {
            _matchList.setSelectedIndex(i + 1);
            _matchList.ensureIndexIsVisible(i + 1);
            _pim.setCurrentItem(_pim.getMatchingItems().get(i + 1));
            updateInfo();
          }
          addListener();
        }
      }
    });
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
//        System.out.println("page up!");
        if (_matchList.getModel().getSize() > 0) {
          removeListener();
          int page = _matchList.getLastVisibleIndex() - _matchList.getFirstVisibleIndex() + 1;
          int i = _matchList.getSelectedIndex() - page;
          if (i < 0)  i = 0;
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
        if (_matchList.getModel().getSize() > 0) {
          removeListener();
          int page = _matchList.getLastVisibleIndex() - _matchList.getFirstVisibleIndex() + 1;
          int i = _matchList.getSelectedIndex() + page;
          if (i >= _matchList.getModel().getSize()) {
            i = _matchList.getModel().getSize() - 1;
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

    _textField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        boolean bf = false;
        for (JButton b: _buttons) { if (e.getOppositeComponent() == b) { bf = true; break; } }
        if ((e.getOppositeComponent() != _strategyBox) && (!bf)) {
          for(JComponent c: _optionalComponents) {
            if (e.getOppositeComponent() == c) { return; }
          }
          _textField.requestFocus();
        }
      }
    });

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
    c.insets.top = 2;
    c.insets.left = 2;
    c.insets.bottom = 2;
    c.insets.right = 2;
    
    if (info) {
      c.fill = GridBagConstraints.NONE;
      contentPane.add(_infoLabel, c);
    }

    c.fill = GridBagConstraints.BOTH;
    c.weighty = 1.0;
    contentPane.add(new JScrollPane(_matchList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), 
                    c);
    
    c.anchor = GridBagConstraints.SOUTHWEST;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    contentPane.add(_tabCompletesLabel, c);
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    contentPane.add(_sharedExtLabel, c);
    
    contentPane.add(_textField, c);
    
    _optionalComponents = makeOptions();
    if (_optionalComponents.length > 0) {
      _optionsPanel = new JPanel(new BorderLayout());
      _setupOptionsPanel(_optionalComponents);
      contentPane.add(_optionsPanel, c);
    }
    
    c.anchor = GridBagConstraints.SOUTHWEST;
    c.weightx = 1.0;
    c.weighty = 0.0;
    c.gridwidth = GridBagConstraints.REMAINDER; // end row
    c.insets.top = 2;
    c.insets.left = 2;
    c.insets.bottom = 2;
    c.insets.right = 2;
    
    JPanel buttonPanel = new JPanel(new GridBagLayout());
    GridBagConstraints bc = new GridBagConstraints();
    bc.insets.left = 2;
    bc.insets.right = 2;
    buttonPanel.add(new JLabel("Matching strategy:"), bc);
    buttonPanel.add(_strategyBox, bc);
    for(JButton b: _buttons) { buttonPanel.add(b, bc); }
    
    contentPane.add(buttonPanel, c);

    pack();
//    Dimension parentDim = (_owner != null) ? _owner.getSize() : getToolkit().getScreenSize();
//    //int xs = (int) parentDim.getWidth()/3;
//    int ys = (int) parentDim.getHeight()/4;
//    // in line below, parentDim was _owner.getSize(); changed because former could generate NullPointerException
//    setSize(new Dimension((int) getSize().getWidth(), (int)Math.min(parentDim.getHeight(), Math.max(ys, 300)))); 
    if (_owner!=null) { setLocationRelativeTo(_owner); }

    removeListener();
    updateTextField();
    addListener();
    updateList();
  }
  
  /** Creates the optional components. Should be overridden. */
  protected JComponent[] makeOptions() {        
    return new JComponent[0];    
  }
  
  /** Creates the panel with the optional components. */
  private void _setupOptionsPanel(JComponent[] components) {
    JPanel mainButtons = new JPanel();
    JPanel emptyPanel = new JPanel();
    GridBagLayout gbLayout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    mainButtons.setLayout(gbLayout);
    
    for (JComponent b: components) { mainButtons.add(b); }
    mainButtons.add(emptyPanel);
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.NORTH;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.weightx = 1.0;

    for (JComponent b: components) { gbLayout.setConstraints(b, c); }
    
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.SOUTH;
    c.gridheight = GridBagConstraints.REMAINDER;
    c.weighty = 1.0;
    
    gbLayout.setConstraints(emptyPanel, c);
    
    _optionsPanel.add(mainButtons, BorderLayout.CENTER);
  }
  
  /** Enable or disable owner. Can be overridden to toggle the hourglass, etc.
   * @param b whether the owner should be enabled (true) or disabled
   */
  public void setOwnerEnabled(boolean b) {
    // do nothing by default
  }
  
  /** Toggle visibility of this frame. Warning, it behaves like a modal dialog. */
  public void setVisible(boolean vis) {
    assert EventQueue.isDispatchThread();
    validate();
    if (vis) {
      DrJavaRoot.installModalWindowAdapter(this, LambdaUtil.NO_OP, CANCEL);
      setOwnerEnabled(false);
      selectStrategy();
      _textField.requestFocus();
      toFront();
    }
    else {
      DrJavaRoot.removeModalWindowAdapter(this);
      setOwnerEnabled(true);
      if (_owner!=null) { _owner.toFront(); }
    }
    super.setVisible(vis);
  }
  
  /** Runnable that calls _cancel. */
  protected final Runnable1<WindowEvent> CANCEL = new Runnable1<WindowEvent>() {
    public void run(WindowEvent e) { cancel(); }
  };

  /** Add the listener. */
  private void addListener() {
    _textField.getDocument().addDocumentListener(_listener);
    _textField.addCaretListener(_listener);
  }

  /** Remove the listener. */
  private void removeListener() {
    _textField.getDocument().removeDocumentListener(_listener);
    _textField.removeCaretListener(_listener);
  }

  /** Update the text field based on the model. */
  private void updateTextField() {
    _textField.setText(_pim.getMask());
    _textField.setCaretPosition(_pim.getMask().length());
  }
  
  /** Focus back in the text field. */
  public void resetFocus() {
    _textField.requestFocus();
  }

  /** Update the extension label based on the model. */
  private void updateExtensionLabel() {
    _sharedExtLabel.setText(_pim.getSharedMaskExtension() + " ");
    _tabCompletesLabel.setVisible(_pim.getSharedMaskExtension().length() > 0);
  }

  /** Update the match list based on the model. */
  private void updateList() {
    _matchList.setListData(_pim.getMatchingItems().toArray());
    _matchList.setSelectedValue(_pim.getCurrentItem(), true);
    updateExtensionLabel();
    updateInfo();
    if (_force) {
      for(int i = 0; i < _buttons.length-1; ++i) {
        _buttons[i].setEnabled(_matchList.getModel().getSize() > 0);
      }
    }
  }

  /** Update the information. */
  private void updateInfo() {
    if (_info == null) return;
    if (_matchList.getModel().getSize() > 0) {
      @SuppressWarnings("unchecked") 
      T item = (T)_matchList.getSelectedValue();
      _infoLabel.setText("Path:   " + _info.value(item));
    }
    else _infoLabel.setText("No file selected");
  }
  
  /** Cancel the dialog. */
  private void cancel() {
    buttonPressed(_actions.get(_cancelIndex));
  }
  
  /** Handle button pressed. */
  private void buttonPressed(CloseAction<T> a) {
    _buttonPressed = a.getName();
    _lastState = new FrameState(PredictiveInputFrame.this);
    setVisible(false);
    a.value(this);
  }
  
  /** Select the strategy for matching. */
  public void selectStrategy() {
    _currentStrategy = _strategies.get(_strategyBox.getSelectedIndex());
    removeListener();
    _pim.setStrategy(_currentStrategy);
    updateTextField();
    updateExtensionLabel();
    updateList();
    addListener();
    _textField.requestFocus();
  }

  /** Listener for several events. */
  private class PredictiveInputListener implements CaretListener, DocumentListener {
    public void insertUpdate(DocumentEvent e) {
      assert EventQueue.isDispatchThread();
//      System.out.println("insertUpdate fired!");
//      Utilities.invokeLater(new Runnable() {
//        public void run() { 
          removeListener();
          _pim.setMask(_textField.getText());
          updateExtensionLabel();
          updateList();
          addListener();
//        }
//      });
    }

    public void removeUpdate(DocumentEvent e) {
      assert EventQueue.isDispatchThread();
//      System.err.println("removeUpdate fired!");
//      Utilities.invokeLater(new Runnable() {
//        public void run() { 
          removeListener();
          _pim.setMask(_textField.getText());
          updateExtensionLabel();
          updateList();
          addListener();
//        }
//      });
    }

    public void changedUpdate(DocumentEvent e) {
      assert EventQueue.isDispatchThread();
//      System.err.println("changedUpdate fired!");
//      Utilities.invokeLater(new Runnable() {
//        public void run() {
          removeListener();
          _pim.setMask(_textField.getText());
          updateExtensionLabel();
          updateList();
          addListener();
//        }
//      });
    }

    public void caretUpdate(CaretEvent e) { }
  }
}
