/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.swing;

import java.io.File;
import java.io.Reader;
// import java.io.FileFilter;  not imported to avoid ambiguity
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
// import javax.swing.filechooser.FileFilter;  not imported to avoid ambiguity
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.Border;

import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.lambda.WrappedException;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.lambda.Predicate;
import edu.rice.cs.plt.reflect.ReflectException;
import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.io.FilePredicate;

import static edu.rice.cs.plt.debug.DebugUtil.error;

public class SwingUtil {
  
  /**
   * Create a shade of gray with the given degree of darkness.  {@code 0.0f} corresponds to white;
   * {@code 1.0f} corresponds to black.
   */
  public static Color gray(float degree) {
    float x = 1.0f - degree;
    return new Color(x, x, x);
  }
  
  /**
   * Boilerplate for creating a JFrame to be used as the main application window.  Sets the title and
   * preferred size to the given values.  Sets the default close operation to "exit on close."
   */
  public static JFrame makeMainApplicationFrame(String title, int width, int height) {
    JFrame result = new JFrame(title);
    result.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    result.getContentPane().setPreferredSize(new Dimension(width, height));
    return result;
  }
  
  /**
   * Boilerplate for creating a JFrame.  Sets the title and preferred size to the given values.  Sets the 
   * default close operation to "dispose on close."
   */
  public static JFrame makeDisposableFrame(String title, int width, int height) {
    JFrame result = new JFrame(title);
    result.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    // cast is necessary for 1.4 compatibility
    result.getContentPane().setPreferredSize(new Dimension(width, height));
    return result;
  }
  
  /**
   * Boilerplate for creating a JFrame.  Sets the title and preferred size to the given values.  Sets the 
   * default close operation to "hide on close."
   */
  public static JFrame makeReusableFrame(String title, int width, int height) {
    JFrame result = new JFrame(title);
    result.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    // cast is necessary for 1.4 compatibility
    result.getContentPane().setPreferredSize(new Dimension(width, height));
    return result;
  }
  
  public static void onWindowClosed(Window w, final Runnable r) {
    w.addWindowListener(new WindowAdapter() {
      public void windowClosed(WindowEvent e) { r.run(); }
    });
  }
  
  /** Shortcut for the boilerplate code to pack and display a window (such as a {@code JFrame}). */
  public static void displayWindow(Window w) {
    w.pack();
    w.setVisible(true);
  }
  
  /** Make a JPanel with a horizontal {@link BoxLayout}. */
  public static JPanel makeHorizontalBoxPanel() {
    JPanel result = new JPanel();
    result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));
    return result;
  }
  
  /** Make a JPanel with a horizontal {@link BoxLayout}; set the border to the given margin on all sides. */
  public static JPanel makeHorizontalBoxPanel(int margin) {
    return makeHorizontalBoxPanel(margin, margin, margin, margin);
  }
  
  /** Make a JPanel with a horizontal {@link BoxLayout}; set the border to the given vertical/horizontal margins. */
  public static JPanel makeHorizontalBoxPanel(int vMargin, int hMargin) {
    return makeHorizontalBoxPanel(vMargin, hMargin, vMargin, hMargin);
  }
  
  /** Make a JPanel with a horizontal {@link BoxLayout}; set the border to the given margins. */
  public static JPanel makeHorizontalBoxPanel(int topMargin, int leftMargin,
                                              int bottomMargin, int rightMargin) {
    JPanel result = makeHorizontalBoxPanel();
    result.setBorder(BorderFactory.createEmptyBorder(topMargin, leftMargin, bottomMargin, rightMargin));
    return result;
  }
  
  /** Make a JPanel with a vertical {@link BoxLayout}. */
  public static JPanel makeVerticalBoxPanel() {
    JPanel result = new JPanel();
    result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
    return result;
  }
  
  /** Make a JPanel with a vertical {@link BoxLayout}; set the border to the given margin on all sides. */
  public static JPanel makeVerticalBoxPanel(int margin) {
    return makeVerticalBoxPanel(margin, margin, margin, margin);
  }
  
  /** Make a JPanel with a vertical {@link BoxLayout}; set the border to the given vertical/horizontal margins. */
  public static JPanel makeVerticalBoxPanel(int vMargin, int hMargin) {
    return makeVerticalBoxPanel(vMargin, hMargin, vMargin, hMargin);
  }
  
  /** Make a JPanel with a vertical {@link BoxLayout}; set the border to the given margins. */
  public static JPanel makeVerticalBoxPanel(int topMargin, int leftMargin,
                                            int bottomMargin, int rightMargin) {
    JPanel result = makeVerticalBoxPanel();
    result.setBorder(BorderFactory.createEmptyBorder(topMargin, leftMargin, bottomMargin, rightMargin));
    return result;
  }
  
  /** Make a JPanel with a {@link BorderLayout}. */
  public static JPanel makeBorderPanel() {
    return new JPanel(new BorderLayout());
  }
  
  /** Make a JPanel with a {@link BorderLayout}; set the border to the given margin on all sides. */
  public static JPanel makeBorderPanel(int margin) {
    return makeBorderPanel(margin, margin, margin, margin);
  }
  
  /** Make a JPanel with a {@link BorderLayout}; set the border to the given vertical/horizontal margins. */
  public static JPanel makeBorderPanel(int vMargin, int hMargin) {
    return makeBorderPanel(vMargin, hMargin, vMargin, hMargin);
  }
  
  /** Make a JPanel with a {@link BorderLayout}; set the border to the given margins. */
  public static JPanel makeBorderPanel(int topMargin, int leftMargin,
                                       int bottomMargin, int rightMargin) {
    JPanel result = makeBorderPanel();
    result.setBorder(BorderFactory.createEmptyBorder(topMargin, leftMargin, bottomMargin, rightMargin));
    return result;
  }
  
  /** Make a JPanel with a {@link FlowLayout}. */
  public static JPanel makeFlowPanel() {
    // default is FlowLayout
    return new JPanel();
  }
  
  /** Make a JPanel with a {@link FlowLayout}; set the border to the given margin on all sides. */
  public static JPanel makeFlowPanel(int margin) {
    return makeFlowPanel(margin, margin, margin, margin);
  }
  
  /** Make a JPanel with a {@link FlowLayout}; set the border to the given vertical/horizontal margins. */
  public static JPanel makeFlowPanel(int vMargin, int hMargin) {
    return makeFlowPanel(vMargin, hMargin, vMargin, hMargin);
  }
  
  /** Make a JPanel with a {@link FlowLayout}; set the border to the given margins. */
  public static JPanel makeFlowPanel(int topMargin, int leftMargin,
                                     int bottomMargin, int rightMargin) {
    JPanel result = makeFlowPanel();
    result.setBorder(BorderFactory.createEmptyBorder(topMargin, leftMargin, bottomMargin, rightMargin));
    return result;
  }
  
  /** Add the given components to {@code parent} in sequence.  Intended to reduce clutter in GUI code. */
  public static void add(Container parent, Component... children) {
    for (Component child : children) { parent.add(child); }
  }
  
  /** Set the background color property of the given components.  Intended to reduce clutter in GUI code. */
  public static void setBackground(Color c, Component... components) {
    for (Component cm : components) { cm.setBackground(c); }
  }
    
  /** Set the foreground color property of the given components.  Intended to reduce clutter in GUI code. */
  public static void setForeground(Color c, Component... components) {
    for (Component cm : components) { cm.setForeground(c); }
  }
    
  /** Set the border of the given components.  Intended to reduce clutter in GUI code. */
  public static void setBorder(Border b, JComponent... components) {
    for (JComponent c : components) { c.setBorder(b); }
  }
    
  /** Set the border of the given components to an empty border with the given margin. */
  public static void setEmptyBorder(int margin, JComponent... components) {
    setBorder(BorderFactory.createEmptyBorder(margin, margin, margin, margin), components);
  }
    
  /** Set the border of the given components to an empty border with the given vertical/horizontal margins. */
  public static void setEmptyBorder(int vMargin, int hMargin, JComponent... components) {
    setBorder(BorderFactory.createEmptyBorder(vMargin, hMargin, vMargin, hMargin), components);
  }
    
  /** Set the border of the given components to an empty border with the given margins. */
  public static void setEmptyBorder(int top, int left, int bottom, int right, JComponent... components) {
    setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right), components);
  }
    
  /** Set the border of the given components to a black line border. */
  public static void setLineBorder(JComponent... components) {
    setBorder(BorderFactory.createLineBorder(Color.BLACK), components);
  }
    
  /** Set the font property of the given components.  Intended to reduce clutter in GUI code. */
  public static void setFont(Font f, Component... components) {
    for (Component c : components) { c.setFont(f); }
  }
    
  /** Set the font property of the given components.  Intended to reduce clutter in GUI code. */
  public static void setFont(String name, int size, Component... components) {
    setFont(new Font(name, Font.PLAIN, size), components);
  }
  
  /** Set the font of the given components to the logical {@code "Serif"} font of the given size. */
  public static void setSerifFont(int size, Component... components) {
    setFont(new Font("Serif", Font.PLAIN, size), components);
  }
  
  /** Set the font of the given components to the logical {@code "SansSerif"} font of the given size. */
  public static void setSansSerifFont(int size, Component... components) {
    setFont(new Font("SansSerif", Font.PLAIN, size), components);
  }
  
  /** Set the font of the given components to the logical {@code "Monospaced"} font of the given size. */
  public static void setMonospacedFont(int size, Component... components) {
    setFont(new Font("Monospaced", Font.PLAIN, size), components);
  }
  
  /** Set the font of the given components to the logical {@code "Dialog"} font of the given size. */
  public static void setDialogFont(int size, Component... components) {
    setFont(new Font("Dialog", Font.PLAIN, size), components);
  }
  
  /** Set the font of the given components to the logical {@code "DialogInput"} font of the given size. */
  public static void setDialogInputFont(int size, Component... components) {
    setFont(new Font("DialogInput", Font.PLAIN, size), components);
  }
  
  /** Set the enabled property of the given components.  Intended to reduce clutter in GUI code. */
  public static void setEnabled(boolean b, Component... components) {
    for (Component c : components) { c.setEnabled(b); }
  }
    
  /** Set the focusable property of the given components.  Intended to reduce clutter in GUI code. */
  public static void setFocusable(boolean b, Component... components) {
    for (Component c : components) { c.setFocusable(b); }
  }
    
  /** Set the visible property of the given components.  Intended to reduce clutter in GUI code. */
  public static void setVisible(boolean b, Component... components) {
    for (Component c : components) { c.setVisible(b); }
  }
    
  /** Set the opaque property of the given components.  Intended to reduce clutter in GUI code. */
  public static void setOpaque(boolean b, JComponent... components) {
    for (JComponent c : components) { c.setOpaque(b); }
  }
    
  /**
   * Set the preferred size of the given components.  Intended to reduce clutter in GUI code.
   * For Java 1.4 compatibility, the arguments must be {@code JComponent}s, not arbitrary
   * {@code Component}s.
   */
  public static void setPreferredSize(Dimension d, JComponent... components) {
    for (JComponent c : components) { c.setPreferredSize(d); }
  }
    
  /**
   * Set the preferred size of the given components.  Intended to reduce clutter in GUI code.
   * For Java 1.4 compatibility, the arguments must be {@code JComponent}s, not arbitrary
   * {@code Component}s.
   */
  public static void setPreferredSize(int width, int height, JComponent... components) {
    setPreferredSize(new Dimension(width, height), components);
  }
  
  /**
   * Set the maximum size of the given components.  Intended to reduce clutter in GUI code.
   * For Java 1.4 compatibility, the arguments must be {@code JComponent}s, not arbitrary
   * {@code Component}s.
   */
  public static void setMaximumSize(Dimension d, JComponent... components) {
    for (JComponent c : components) { c.setMaximumSize(d); }
  }
    
  /**
   * Set the maximum size of the given components.  Intended to reduce clutter in GUI code.
   * For Java 1.4 compatibility, the arguments must be {@code JComponent}s, not arbitrary
   * {@code Component}s.
   */
  public static void setMaximumSize(int width, int height, JComponent... components) {
    setMaximumSize(new Dimension(width, height), components);
  }
  
  /**
   * Set the minimum size of the given components.  Intended to reduce clutter in GUI code.
   * For Java 1.4 compatibility, the arguments must be {@code JComponent}s, not arbitrary
   * {@code Component}s.
   */
  public static void setMinimumSize(Dimension d, JComponent... components) {
    for (JComponent c : components) { c.setMinimumSize(d); }
  }
    
  /**
   * Set the minimum size of the given components.  Intended to reduce clutter in GUI code.
   * For Java 1.4 compatibility, the arguments must be {@code JComponent}s, not arbitrary
   * {@code Component}s.
   */
  public static void setMinimumSize(int width, int height, JComponent... components) {
    setMinimumSize(new Dimension(width, height), components);
  }
  
  /** Make the given components horizontally-left aligned. */
  public static void setLeftAlignment(JComponent... components) {
    setAlignmentX(Component.LEFT_ALIGNMENT, components);
  }
  
  /** Make the given components horizontally-right aligned. */
  public static void setRightAlignment(JComponent... components) {
    setAlignmentX(Component.RIGHT_ALIGNMENT, components);
  }
  
  /** Make the given components horizontally-center aligned. */
  public static void setHorizontalCenterAlignment(JComponent... components) {
    setAlignmentX(Component.CENTER_ALIGNMENT, components);
  }
  
  /** Make the given components vertically-top aligned. */
  public static void setTopAlignment(JComponent... components) {
    setAlignmentY(Component.TOP_ALIGNMENT, components);
  }
  
  /** Make the given components vertically-bottom aligned. */
  public static void setBottomAlignment(JComponent... components) {
    setAlignmentY(Component.BOTTOM_ALIGNMENT, components);
  }
  
  /** Make the given components vertically-center aligned. */
  public static void setVerticalCenterAlignment(JComponent... components) {
    setAlignmentY(Component.CENTER_ALIGNMENT, components);
  }
  
  /** Set the horizontal alignment of the given components.  Intended to reduce clutter in GUI code. */
  public static void setAlignmentX(float a, JComponent... components) {
    for (JComponent c : components) { c.setAlignmentX(a); }
  }
    
  /** Set the vertical alignment of the given components.  Intended to reduce clutter in GUI code. */
  public static void setAlignmentY(float a, JComponent... components) {
    for (JComponent c : components) { c.setAlignmentY(a); }
  }
    
  
  /** 
   * Runs the task synchronously if the current thread is the event thread; otherwise passes it to the
   * event thread to be run asynchronously after all events already on the queue have been processed.
   */
  public static void invokeLater(Runnable task) {
    if (EventQueue.isDispatchThread()) { task.run(); }
    else { EventQueue.invokeLater(task); }
  }
  
  /**
   * Runs the the task in the event thread, blocking until it is complete.
   * 
   * @throws WrappedException  Wrapping an {@link InterruptedException} if the current thread is interrupted
   * @throws RuntimeException  If an exception is thrown by {@code task}
   */
  public static void invokeAndWait(Runnable task) {
    if (EventQueue.isDispatchThread()) { task.run(); }
    else {
      try { EventQueue.invokeAndWait(task); }
      catch (InterruptedException e) { throw new WrappedException(e); }
      catch (InvocationTargetException e) {
        Throwable cause = e.getCause();
        // must be a RuntimeException or an Error, because Runnable's can't have checked exceptions
        if (cause instanceof RuntimeException) { throw (RuntimeException) cause; }
        else if (cause instanceof Error) { throw (Error) cause; }
        else { error.log("Unexpected InvocationTargetException caused by invokeAndWait", cause); }
      }
    }
  }
  
  /**
   * Wait for all items in the event queue to be handled.  This thread will block until all items
   * <em>currently</em> on the event queue have been handled.
   * @throws IllegalStateException  If this is the event dispatch thread (it is impossible to wait
   *                                for the event queue to clear if this code is running in the
   *                                event queue).
   * @throws InterruptedException  If this thread is interrupted while waiting.
   */
  public static void clearEventQueue() throws InterruptedException {
    if (SwingUtilities.isEventDispatchThread()) {
      throw new IllegalStateException("Can't clear the event queue from within the event dispatch thread");
    }
    try { SwingUtilities.invokeAndWait(LambdaUtil.NO_OP); }
    catch (InvocationTargetException e) {
      // Should never happen: Runnable is a no-op.
      error.log(e);
    }
  }
  
  /**
   * Call @link{#clearEventQueue}, but ignore any resulting InterruptedException.  This method does
   * not guarantee that the queue will actually be cleared.
   * @throws IllegalStateException  If this is the event dispatch thread (it is impossible to wait
   *                                for the event queue to clear if this code is running in the
   *                                event queue).
   */
  public static void attemptClearEventQueue() {
    try { clearEventQueue(); }
    catch (InterruptedException e) { /* ignore */ }
  }
  
  /** Convert a {@code Runnable} to an {@code ActionListener} */
  public static ActionListener asActionListener(final Runnable r) {
    return new ActionListener() {
      public void actionPerformed(ActionEvent e) { r.run(); }
    };
  }
  
  /** Convert a {@code Runnable1} to an {@code ActionListener} */
  public static ActionListener asActionListener(final Runnable1<? super ActionEvent> r) {
    return new ActionListener() {
      public void actionPerformed(ActionEvent e) { r.run(e); }
    };
  }
  
  /** Convert an {@code ActionListener} to a {@code Runnable1} */
  public static Runnable1<ActionEvent> asRunnable(final ActionListener l) {
    return new Runnable1<ActionEvent>() {
      public void run(ActionEvent e) { l.actionPerformed(e); }
    };
  }
  
  /**
   * Define a Swing file filter (for use with {@link javax.swing.JFileChooser}s) in terms if a 
   * {@code FileFilter}.
   */
  public static javax.swing.filechooser.FileFilter asSwingFileFilter(final java.io.FileFilter filter, 
                                                                     final String description) {
    return new javax.swing.filechooser.FileFilter() {
      public boolean accept(File f) { return filter.accept(f); }
      public String getDescription() { return description; }
    };
  }
  
  /**
   * Define a Swing file filter (for use with {@link javax.swing.JFileChooser}s) in terms if a 
   * {@code FileFilter}.
   */
  public static javax.swing.filechooser.FileFilter asSwingFileFilter(Predicate<? super File> p, String description) {
    return asSwingFileFilter((java.io.FileFilter) IOUtil.asFilePredicate(p), description);
  }
  
  /**
   * Define a Swing file filter (for use with {@link javax.swing.JFileChooser}s) in terms if a 
   * {@code FileFilter}.  (Defined to disambiguate, since both other declarations apply to FilePredicates.)
   */
  public static javax.swing.filechooser.FileFilter asSwingFileFilter(FilePredicate p, String description) {
    return asSwingFileFilter((java.io.FileFilter) p, description);
  }
  
  /** Create an action to invoke {@link Window#dispose} on the given window */
  public static ActionListener disposeAction(final Window w) {
    return new ActionListener() {
      public void actionPerformed(ActionEvent e) { w.dispose(); }
    };
  }
  
  /** Invoke {@link #showPopup} with title {@code "Debug Message"}.  This may be called by any thread. */
  public static void showDebug(String msg) { showPopup("Debug Message", msg); }
  
  /**
   * Show a modal message box with an OK button.  This may be called by any thread.
   * 
   * @param msg  String to display
   * @param title  Title of the box
   */
  public static void showPopup(final String title, final String msg) {
    invokeAndWait(new Runnable() { public void run() {
      TextAreaMessageDialog.showDialog(null, title, msg); 
    } } );
  }
  
  /**
   * Load the given applet class from the sources at URL {@code classPath} and display it in a window
   * with title {@code "Applet Viewer"}.
   */ 
  public static void showApplet(URL classPath, String className, int width, int height) throws ReflectException {
    showApplet("Applet Viewer", classPath, className, width, height, Collections.<String, String>emptyMap());
  }
  
  /**
   * Load the given applet class from the sources at URL {@code classPath} and display it in a window
   * with the given title.
   */ 
  public static void showApplet(String title, URL classPath, String className, int width, int height)
      throws ReflectException {
    showApplet(title, classPath, className, width, height, Collections.<String, String>emptyMap());
  }
  
  /**
   * Load the given applet class from the sources at URL {@code classPath} and display it in a window
   * with the given title.
   */ 
  public static void showApplet(String title, URL classPath, String className, int width, int height,
                                Map<String, String> params) throws ReflectException {
    Applet a = (Applet) ReflectUtil.loadObject(new URLClassLoader(new URL[]{ classPath }), className);
    showApplet(title, a, width, height, classPath, params);
  }
  
  /** Display the given applet in a window with title {@code "Applet Viewer"}. */
  public static void showApplet(Applet applet, int width, int height) {
    showApplet("Applet Viewer", applet, width, height, null, Collections.<String, String>emptyMap());
  }

  /** Display the given applet in a window with the given title. */
  public static void showApplet(String title, Applet applet, int width, int height) {
    showApplet(title, applet, width, height, null, Collections.<String, String>emptyMap());
  }
  
  /**
   * Display the given applet in a window with the given title; if it is not {@code null}, sources
   * are assumed to be rooted at the given URL.
   */
  public static void showApplet(String title, Applet applet, int width, int height, URL root) {
    showApplet(title, applet, width, height, root);
  }
  
  /**
   * Display the given applet in a window with the given title; if it is not {@code null}, sources
   * are assumed to be rooted at the given URL.
   */
  public static void showApplet(String title, Applet applet, int width, int height, URL root,
                                Map<String, String> params) {
    JFrame frame = makeDisposableFrame(title, width, height);
    frame.add(new AppletComponent(applet, width, height, root, params));
    displayWindow(frame);
  }
  
  /** @return a string with the current clipboard selection, or null if not available. */
  public static String getClipboardSelection(Component c) {
    Clipboard cb = c.getToolkit().getSystemClipboard();
    if (cb==null) return null;
    Transferable t = cb.getContents(null);
    if (t==null) return null;
    String s = null;
    try {
      Reader r = DataFlavor.stringFlavor.getReaderForText(t);
      s = IOUtil.toString(r);
    }
    catch(UnsupportedFlavorException ufe) { /* ignore, return null */ }
    catch(java.io.IOException ioe) { /* ignore, return null */ }
    return s;
  }
  
  
  /**
   * Search the given text component's list of actions for an action with the given name.
   * Returns {@code null} if no such action is found.  See {@link DefaultEditorKit} for a list of
   * action name constants.
   */
  public static Action getAction(JTextComponent component, String actionName) {
    for (Action a : component.getActions()) {
      if (actionName.equals(a.getValue(Action.NAME))) { return a; }
    }
    return null;
  }
  
  /**
   * Create a map view of {@code component.getActions()}, where the keys are the action
   * names.  See {@link DefaultEditorKit} for a list of action name constants.
   */
  public static Map<String, Action> getActions(JTextComponent component) {
    Map<String, Action> result = new HashMap<String, Action>();
    for (Action a : component.getActions()) {
      // Documentation for Action.NAME asserts that it will always be a String
      result.put((String) a.getValue(Action.NAME), a);
    }
    return result;
  }
  


  /** Convenience method for {@link #setPopupLoc(Window, Component)} that gets the owner from {@code popup.getOwner()} */
  public static void setPopupLoc(Window popup) { setPopupLoc(popup, popup.getOwner()); }
  
  /** 
   * <p>Sets the location of the popup in a consistent way.  If the popup has an owner, the popup is centered over the
   * owner.  If the popup has no owner (owner == null), the popup is centered over the first monitor.  In either case,
   * the popup is moved and scaled if any part of it is not on the screen.  This method should be called for all popups
   * to maintain consistancy.</p>
   * <p>This method should only be called from the event thread.</p>
   * 
   * @param popup the popup window
   * @param owner the parent component for the popup, or {@code null}
   */
  public static void setPopupLoc(Window popup, Component owner) {
    Rectangle frameRect = popup.getBounds();
    
    Point ownerLoc = null;
    Dimension ownerSize = null;
    if(owner!=null) {
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
      if (gcArea>gcBestArea) {
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
  
  
}
