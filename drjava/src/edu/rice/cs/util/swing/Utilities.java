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

package edu.rice.cs.util.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.datatransfer.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.StringOps;

public class Utilities {
  
  /** True if the program is run in non-interactive test mode. */
  public static volatile boolean TEST_MODE = false;
  
  public static final String JGOODIES_PACKAGE = "com.jgoodies.looks";
  
  /** Runs the task synchronously if the current thread is the event thread; otherwise passes it to the
    * event thread to be run asynchronously after all events already on the queue have been processed.
    */
  public static void invokeLater(Runnable task) {
    if (EventQueue.isDispatchThread()) {
      task.run(); 
      return;
    }
    EventQueue.invokeLater(task);
  }
  
  public static void invokeAndWait(Runnable task) {
    if (EventQueue.isDispatchThread()) {
      task.run(); 
      return;
    }
    try { EventQueue.invokeAndWait(task); }
    catch(Exception e) { throw new UnexpectedException(e); }
  }
  
  public static void main(String[] args) { clearEventQueue(); }

  /** Clears the event queue by waiting until all events currently in the queue
    * have been processed. Calls clearEventQueue(true);
    */
  public static void clearEventQueue() { clearEventQueue(true); }
  
  /** Clears the event queue by waiting until all events currently in the queue
    * have been processed. If newEvents is set to true, the method will also wait for
    * all events that have been put in the queue by the events that were just
    * processed, and so on.
    * @param newEvents true if the method should also clear new events that were added by the events just cleared
    */
  public static void clearEventQueue(boolean newEvents) {
    assert ! EventQueue.isDispatchThread();
    final EventQueue q = Toolkit.getDefaultToolkit().getSystemEventQueue();
    do {
      // it is an error to be in the event queue, so Utilties.invokeAndWait shouldn't be used
      try { EventQueue.invokeAndWait(LambdaUtil.NO_OP); }
      catch (Exception e) { throw new UnexpectedException(e); }
    } while (newEvents && (null != q.peekEvent()));
  }
  
  /** Show a modal debug message box with an OK button regardless of TEST_MODE.
    * @param msg string to display
    */
  public static void show(final String msg) { 
    Utilities.invokeAndWait(new Runnable() { public void run() {
      new edu.rice.cs.drjava.ui.DrJavaScrollableDialog(null,
                                                       "Debug Message",
                                                       "Debug Message from Utilities.show():",
                                                       msg,
                                                       false).show(); } } );
  }
  
  /** Shows a modal debug message box with an OK button when not in TEST_MODE.
    * @param msg string to display
    */
  public static void showDebug(String msg) { showMessageBox(msg, "Debug Message"); }
  
  /** Shows a modal message box with an OK button.
    * @param msg string to display
    */
  public static void showMessageBox(final String msg, final String title) {
    if (TEST_MODE) System.out.println(title + ": " + msg); else {
      //Utilities.invokeAndWait(new Runnable() { public void run() { JOptionPane.showMessageDialog(null, msg); } } );
      Utilities.invokeAndWait(new Runnable() { public void run() {
        new edu.rice.cs.drjava.ui.DrJavaScrollableDialog(null,
                                                         title,
                                                         "Message:",
                                                         msg,
                                                         false).show();
      } } );
    }
  }
  
  public static void showStackTrace(final Throwable t) {
    Utilities.invokeAndWait(new Runnable() { public void run() { 
      new edu.rice.cs.drjava.ui.DrJavaScrollableDialog(null,
                                                       "Stack Trace",
                                                       "Stack Trace:",
                                                       StringOps.getStackTrace(t),
                                                       false).show();
    } } );
  }
  
  /** @return a string with the current clipboard selection, or null if not available. */
  public static String getClipboardSelection(Component c) {
    Clipboard cb = c.getToolkit().getSystemClipboard();
    if (cb == null) return null;
    Transferable t = cb.getContents(null);
    if (t == null) return null;
    String s = null;
    try {
      java.io.Reader r = DataFlavor.stringFlavor.getReaderForText(t);
      int ch;
      final StringBuilder sb = new StringBuilder();
      while ((ch=r.read()) !=-1 ) { sb.append((char)ch); }
      s = sb.toString();
    }
    catch(UnsupportedFlavorException ufe) { /* ignore, return null */ }
    catch(java.io.IOException ioe) { /* ignore, return null */ }
    return s;
  }
  
  /** @return an action with a new name that delegates to another action. */
  public static AbstractAction createDelegateAction(String newName, final Action delegate) {
    return new AbstractAction(newName) {
      public void actionPerformed(ActionEvent ae) { delegate.actionPerformed(ae); }
    };
  }
  
  /** @return whether the current LookAndFeel is a Plastic (i.e. JGoodies) LookAndFeel */
  public static boolean isPlasticLaf() {
    LookAndFeel laf = UIManager.getLookAndFeel();
    return laf != null && laf.getClass().getName().startsWith(JGOODIES_PACKAGE);
  }
  
  /** @return whether a given LookAndFeel name is a Plastic (i.e. JGoodies) LookAndFeel
    * @param name the fully-qualified classname of the LookAndFeel */
  public static boolean isPlasticLaf(String name) {
    return name != null && name.startsWith(JGOODIES_PACKAGE);
  }
  
  /** Determines the location of the popup using a simple, uniform protocol.  If the popup has an owner, the popup is 
    * centered over the owner.  If the popup has no owner(owner == null), the popup is centered over the first monitor.
    * In either case, the popup is moved and scaled if any part of it is not on the screen.  This method should be 
    * called for all popups to maintain uniformity in the DrJava UI.
    * @param popup the popup window
    * @param owner the parent component for the popup
    */
  public static void setPopupLoc(Window popup, Component owner) {
    Rectangle frameRect = popup.getBounds();
    
    Point ownerLoc = null;
    Dimension ownerSize = null;
    if (owner != null && owner.isVisible()) {
      ownerLoc = owner.getLocation();
      ownerSize = owner.getSize();
    }
    else {
      //for multi-monitor support
      //Question: do we want it to popup on the first monitor always?
      GraphicsDevice[] dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
      Rectangle rec = dev[0].getDefaultConfiguration().getBounds();
      ownerLoc = rec.getLocation();
      ownerSize = rec.getSize();
    }
    
    // center it on owner
    Point loc = new Point(ownerLoc.x + (ownerSize.width - frameRect.width) / 2,
                          ownerLoc.y + (ownerSize.height - frameRect.height) / 2);
    frameRect.setLocation(loc);
    
    // now find the GraphicsConfiguration the popup is on
    GraphicsConfiguration gcBest = null;
    int gcBestArea = -1;
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gs = ge.getScreenDevices();
    for (GraphicsDevice gd: gs) {
      GraphicsConfiguration gc = gd.getDefaultConfiguration();
      Rectangle isect = frameRect.intersection(gc.getBounds());
      int gcArea = isect.width*isect.height;
      if (gcArea > gcBestArea) {
        gcBest = gc;
        gcBestArea = gcArea;
      }
    }
    
    // make it fit on the screen
    Rectangle screenRect = gcBest.getBounds();
    Dimension screenSize = screenRect.getSize();
    Dimension frameSize = popup.getSize();
    
    if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
    if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;
    
    frameRect.setSize(frameSize);
    
    // center it on owner again
    loc = new Point(ownerLoc.x + (ownerSize.width - frameRect.width) / 2,
                    ownerLoc.y + (ownerSize.height - frameRect.height) / 2);
    frameRect.setLocation(loc);
    
    // now fit it on the screen
    if (frameRect.x < screenRect.x) frameRect.x = screenRect.x;
    if (frameRect.x + frameRect.width > screenRect.x + screenRect.width)
      frameRect.x = screenRect.x + screenRect.width - frameRect.width;
    
    if (frameRect.y < screenRect.y) frameRect.y = screenRect.y;
    if (frameRect.y + frameRect.height > screenRect.y + screenRect.height)
      frameRect.y = screenRect.y + screenRect.height - frameRect.height;
    
    popup.setSize(frameRect.getSize());
    popup.setLocation(frameRect.getLocation());
  }
  
  /** Enables/disables the second action whenever the first action is enabled/disabled.
    * @param observable the action that is observed (leads)
    * @param observer the action that follows
    * @return the PropertyChangeListener used to do the observation */
  public static PropertyChangeListener enableDisableWith(Action observable, final Action observer) {
    PropertyChangeListener pcl = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("enabled")) { observer.setEnabled((Boolean)e.getNewValue()); }
      }
    };
    observable.addPropertyChangeListener(pcl);
    return pcl;
  }
  
  /** Return the index of the component in the parent container, or -1 if no parent or not found.
    * @param component
    * @return index of the component in the parent container, or -1 if not found */
  public static int getComponentIndex(Component component) {
    if (component != null && component.getParent() != null) {
      Container c = component.getParent();
      for (int i = 0; i < c.getComponentCount(); i++) {
        if (c.getComponent(i) == component)
          return i;
      }
    }
    
    return -1;
  }
}
