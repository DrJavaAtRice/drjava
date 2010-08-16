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

package edu.rice.cs.drjava.ui;

import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.drjava.platform.PlatformFactory;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.ui.KeyBindingManager;
import edu.rice.cs.drjava.ui.KeyBindingManager.KeyStrokeData;
import edu.rice.cs.drjava.DrJava;

public class DetachedFrame extends SwingFrame {
  /** Class to save the frame state, i.e. location. */
  public static class FrameState {
    private Point _loc;
    private Dimension _dim;
    public FrameState(Point l, Dimension d) {
      _loc = l;
      _dim = d;
    }
    public FrameState(String s) {
      StringTokenizer tok = new StringTokenizer(s);
      try {
        int x = Integer.valueOf(tok.nextToken());
        int y = Integer.valueOf(tok.nextToken());
        int w = Integer.valueOf(tok.nextToken());
        int h = Integer.valueOf(tok.nextToken());
        _loc = new Point(x, y);
        _dim = new Dimension(w, h);
      }
      catch(NoSuchElementException nsee) {
        throw new IllegalArgumentException("Wrong FrameState string: " + nsee);
      }
      catch(NumberFormatException nfe) {
        throw new IllegalArgumentException("Wrong FrameState string: " + nfe);
      }
    }
    public FrameState(DetachedFrame comp) {
      _loc = comp.getLocation();
      _dim = comp.getSize();
    }
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append(_loc.x);
      sb.append(' ');
      sb.append(_loc.y);
      sb.append(' ');
      sb.append(_dim.width);
      sb.append(' ');
      sb.append(_dim.height);
      return sb.toString();
    }
    public Point getLocation() { return _loc; }
    public Dimension getDimension() { return _dim; }
  }
  
  /** Lambda to execute when component is being detached. The parameter is the instance of DetachedFrame that
    * may contain the component. */
  Runnable1<DetachedFrame> _detach;
  /** Lambda to execute when component is being re-attached. The parameter is the instance of DetachedFrame that
    * may contain the component. */
  Runnable1<DetachedFrame> _reattach;
  /** Last frame state. It can be stored and restored. */
  private FrameState _lastState = null;
  /** Main frame. */
  private MainFrame _mainFrame;  
  /** Window adapter to re-attach the tabbed pane when the window is closed. */
  private WindowAdapter _wa = new WindowAdapter() {
    public void windowClosing(WindowEvent we) {
      setDisplayInFrame(false);
    }
  };
  /** Old InputMap without accelerators added. */
  private final InputMap _oldInputMap = new InputMap();
  /** Listeners that need to be removed when this frame is disposed. Key=Listener, Value=JMenuItem listened to. */
  private final HashMap<PropertyChangeListener,JMenuItem> _listenersToRemoveWhenDisposed =
    new HashMap<PropertyChangeListener,JMenuItem>();

  /** Returns the last state of the frame, i.e. the location and dimension.
    * @return frame state
    */
  public FrameState getFrameState() {
    if (isVisible()) {
      return new FrameState(this);
    }
    else {
      return _lastState;
    }
  }
  
  /** Sets state of the frame, i.e. the location and dimension of the frame for the next use.
    * @param ds State to update to, or {@code null} to reset
    */
  public void setFrameState(FrameState ds) {
    _lastState = ds;
    if (_lastState != null) {
      setLocation(_lastState.getLocation());
      setSize(_lastState.getDimension());
      validate();
    }
  }  
  
  /** Sets state of the frame, i.e. the location and dimension of the frame for the next use.
    * @param s  State to update to, or {@code null} to reset
    */
  public void setFrameState(String s) {
    try { _lastState = new FrameState(s); }
    catch(IllegalArgumentException e) { _lastState = null; }
    if (_lastState != null) {
      setLocation(_lastState.getLocation());
      setSize(_lastState.getDimension());
    }
    else {
      Utilities.setPopupLoc(this, _mainFrame);
      setSize(700,400);
    }
    validate();
  }
  
  /** Recursively process the MenuElement and add entries to the InputMap and ActionMap so that the
    * menu element's accelerator will invoke the menu element's action even if the MenuElement is
    * not present in another frame.
    * Note that this will only use the first key stroke configured for an action, because a menu item
    * can only have one accelerator key stroke.
    */
  protected static void processMenuElement(MenuElement elt, InputMap im, ActionMap am) {
    if ((elt instanceof JMenuItem) && !(elt instanceof JMenu)) {
      // this is a leaf
      JMenuItem menuItem = (JMenuItem)elt;
      KeyStroke ks = menuItem.getAccelerator();
      if (ks != null) {
        // it has an accelerator
        Action a = menuItem.getAction();
        final String ACTION_NAME =
          ks.toString()+"-"+System.identityHashCode(ks)+"-"+
          a.toString()+"-"+System.identityHashCode(a);
        im.put(ks, ACTION_NAME);
        am.put(ACTION_NAME, a);
      }
    }
    else {
      // interior node or root, process recursively
      for(MenuElement subElt: elt.getSubElements()) { processMenuElement(subElt, im, am); }
    }
  }
  
  /** Recursively copy the first menu bar's accelerators into the second menu bar.
    * Installs listeners to keep the accelerators updated. */
  protected void copyAccelerators(JMenuBar source, JMenuBar dest) {
    int sourceIndex = 0;
    int destIndex = 0;
    while(sourceIndex<source.getMenuCount() && destIndex<dest.getMenuCount()) {
      JMenu sourceMenu = source.getMenu(sourceIndex++);
      while((destIndex<dest.getMenuCount()) &&
            (dest.getMenu(destIndex).getText() == null ||
             !dest.getMenu(destIndex).getText().equals(sourceMenu.getText()))) {
        ++destIndex;
      }
      if (destIndex<dest.getMenuCount()) {
        JMenu destMenu = dest.getMenu(destIndex++);
        copyAccelerators(sourceMenu, destMenu);
      }
    }
  }
  
  /** Recursively copy the first menu's accelerators into the second menu.
    * Installs listeners to keep the accelerators updated. */
  protected void copyAccelerators(MenuElement source, MenuElement dest) {
    if (!(source instanceof JMenu) && !(source instanceof JPopupMenu) &&
        !(dest instanceof JMenu) && !(dest instanceof JPopupMenu) &&
        (source instanceof JMenuItem) && (dest instanceof JMenuItem)) {
      // this is a leaf
      final JMenuItem sourceItem = (JMenuItem)source;
      final JMenuItem destItem = (JMenuItem)dest;
      if ((sourceItem.getText() != null) &&
          sourceItem.getText().equals(destItem.getText())) {
        destItem.setAccelerator(sourceItem.getAccelerator());
        PropertyChangeListener listener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) {
            if ("accelerator".equals(evt.getPropertyName())) {
              destItem.setAccelerator(sourceItem.getAccelerator());
            }
          }
        };
        sourceItem.addPropertyChangeListener(listener);
        _listenersToRemoveWhenDisposed.put(listener, sourceItem);
      }
    }
    else {
      MenuElement[] sourceElts = source.getSubElements();
      MenuElement[] destElts = dest.getSubElements();
      int sourceIndex = 0;
      int destIndex = 0;
      while(sourceIndex<sourceElts.length && destIndex<destElts.length) {
        MenuElement sourceElement = sourceElts[sourceIndex++];
        boolean matches = false;
        do {
          while((destIndex<destElts.length) &&
                !(destElts[destIndex].getClass().equals(sourceElement.getClass()))) {
            ++destIndex;
          }
          if (destIndex>=destElts.length) { break; }
          if ((sourceElement instanceof JMenuItem) &&
              (destElts[destIndex]instanceof JMenuItem)) {
            JMenuItem sourceItem = (JMenuItem)sourceElement;
            JMenuItem destItem = (JMenuItem)destElts[destIndex];
            if (sourceItem.getText() == null) {
              matches = (destItem.getText() == null);
            }
            else {
              matches = sourceItem.getText().equals(destItem.getText());
            }
          }
          else {
            matches = true;
          }
        } while(!matches);
        if (destIndex<destElts.length) {
          MenuElement destElement = destElts[destIndex++];
          copyAccelerators(sourceElement, destElement);
        }
      }
    }
  }
  
  /** Create a tabbed pane frame. Initially, the tabbed pane is displayed in the split pane
    * @param name frame name
    * @param mf the MainFrame
    * @param detach command to detach the component. The parameter is the instance of DetachedFrame that may contain the component.
    * @param reattach command to re-attach the component. The parameter is the instance of DetachedFrame that may contain the component.
    */
  public DetachedFrame(String name, MainFrame mf, Runnable1<DetachedFrame> detach, Runnable1<DetachedFrame> reattach) {
    super(name);
    _mainFrame = mf;
    _detach = detach;
    _reattach = reattach;
    
    // not strictly necessary on Mac, because Mac DetachedFrames have a menu bar
    final InputMap im = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    // First clone the InputMap so we can change the keystroke mappings
    if (im.keys()!=null) { // keys() may return null!
      for(KeyStroke ks: im.keys()) { _oldInputMap.put(ks, im.get(ks)); }
    }
    // Add listeners to all key bindings
    for (KeyStrokeData ksd: KeyBindingManager.ONLY.getKeyStrokeData()) {
      if (ksd.getOption() != null) {
        DrJava.getConfig().addOptionListener(ksd.getOption(), _keyBindingOptionListener);
      }
    }
    // Then update the key bindings
    updateKeyBindings();

    initDone(); // call mandated by SwingFrame contract
  }  
    
  /** OptionListener responding to changes for the undo/redo key bindings. */
  private final OptionListener<Vector<KeyStroke>> _keyBindingOptionListener = new OptionListener<Vector<KeyStroke>>() {
    public void optionChanged(OptionEvent<Vector<KeyStroke>> oce) {
      updateKeyBindings();
    }
  };
  
  /** Update the key bindings from the MainFrame menu bar. */
  public void updateKeyBindings() {
    // first restore old InputMap.
    final InputMap im = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    final ActionMap am = getRootPane().getActionMap();
    
    if (im.keys()!=null) { // keys() may return null!
      for(KeyStroke ks: im.keys()) { im.remove(ks); }
    }
    if (_oldInputMap.keys()!=null) { // keys() may return null!
      for(KeyStroke ks: _oldInputMap.keys()) { im.put(ks, _oldInputMap.get(ks)); }
    }
    
    processMenuElement(_mainFrame.getJMenuBar(), im, am);
  }
  
  public void dispose() {
    // Mac only
    if (PlatformFactory.ONLY.isMacPlatform()) {
      for(Map.Entry<PropertyChangeListener,JMenuItem> e: _listenersToRemoveWhenDisposed.entrySet()) {
        e.getValue().removePropertyChangeListener(e.getKey());
      }

      _mainFrame.removeMenuBarInOtherFrame(getJMenuBar());
    }
    super.dispose();
  }
  
  public void setUpMenuBar() {
    // Mac only
    if (PlatformFactory.ONLY.isMacPlatform() && (getJMenuBar()==null)) {
      JMenuBar menuBar = new MainFrame.MenuBar(_mainFrame);
      _mainFrame._setUpMenuBar(menuBar);
      _mainFrame.addMenuBarInOtherFrame(menuBar);
      copyAccelerators(_mainFrame.getJMenuBar(), menuBar);
      setJMenuBar(menuBar); // it's not that easy to reproduce the MainFrame's menu bar
    }
  }
  
  /** Toggle visibility of this frame. Warning, it behaves like a modal dialog. */
  public void setVisible(boolean vis) {
    super.setVisible(vis);
    _lastState = new FrameState(this);
  }

  /** Set whether the the tabbed pane is displayed in this frame.
    * @param b true to display the tabbed pane in this window, false to display it in the MainFrame split pane */
  public void setDisplayInFrame(boolean b) {
    if (b) {
      _detach.run(this);
      setVisible(true);
      addWindowListener(_wa);
    }
    else {
      removeWindowListener(_wa);
      setVisible(false);
      getContentPane().removeAll();
      _reattach.run(this);
    }
  }
}
