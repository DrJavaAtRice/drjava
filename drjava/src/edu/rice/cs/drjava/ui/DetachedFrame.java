/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.jar.JarBuilder;
import edu.rice.cs.util.jar.ManifestWriter;
import edu.rice.cs.util.swing.FileChooser;
import edu.rice.cs.util.swing.FileSelectorStringComponent;
import edu.rice.cs.util.swing.FileSelectorComponent;
import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.SwingWorker;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.newjvm.ExecJVM;
import edu.rice.cs.util.StreamRedirectThread;
import edu.rice.cs.util.Lambda;

import javax.swing.*;
//import javax.swing.border.EmptyBorder;
//import javax.swing.event.DocumentEvent;
//import javax.swing.event.DocumentListener;
//import javax.swing.filechooser.FileFilter;
import java.awt.*;
//import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
//import java.io.File;
//import java.io.IOException;
//import java.util.Iterator;
//import java.util.List;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

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
  Lambda<Void,DetachedFrame> _detach;
  /** Lambda to execute when component is being re-attached. The parameter is the instance of DetachedFrame that
    * may contain the component. */
  Lambda<Void,DetachedFrame> _reattach;
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
      MainFrame.setPopupLoc(this, _mainFrame);
      setSize(700,400);
    }
    validate();
  }
  
  /** Create a tabbed pane frame. Initially, the tabbed pane is displayed in the split pane
    * @param name frame name
    * @param mf the MainFrame
    * @param detach command to detach the component. The parameter is the instance of DetachedFrame that may contain the component.
    * @param reattach command to re-attach the component. The parameter is the instance of DetachedFrame that may contain the component.
    */
  public DetachedFrame(String name, MainFrame mf, Lambda<Void,DetachedFrame> detach, Lambda<Void,DetachedFrame> reattach) {
    super(name);
    _mainFrame = mf;
    _detach = detach;
    _reattach = reattach;
    initDone(); // call mandated by SwingFrame contract
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
      _detach.apply(this);
      setVisible(true);
      addWindowListener(_wa);
    }
    else {
      removeWindowListener(_wa);
      setVisible(false);
      getContentPane().removeAll();
      _reattach.apply(this);
    }
  }
}
