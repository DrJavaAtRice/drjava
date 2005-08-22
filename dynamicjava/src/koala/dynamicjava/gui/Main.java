/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import koala.dynamicjava.gui.resource.*;
import koala.dynamicjava.gui.resource.ActionMap;
import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.parser.wrapper.*;

/**
 * A Graphical User Interface for DynamicJava
 *
 * @author Stephane Hillion
 * @version 1.4 - 1999/11/28
 */

public class Main extends JFrame implements ActionMap {
  /**
   * The entry point of the program
   */
  public static void main(String[] args) {
    new Main().setVisible(true);
  }
  
  // The action names
  public final static String OPEN_ACTION    = "OpenAction";
  public final static String SAVE_ACTION    = "SaveAction";
  public final static String SAVE_AS_ACTION = "SaveAsAction";
  public final static String EXIT_ACTION    = "ExitAction";
  public final static String UNDO_ACTION    = "UndoAction";
  public final static String REDO_ACTION    = "RedoAction";
  public final static String CUT_ACTION     = "CutAction";
  public final static String COPY_ACTION    = "CopyAction";
  public final static String PASTE_ACTION   = "PasteAction";
  public final static String CLEAR_ACTION   = "ClearAction";
  public final static String OPTIONS_ACTION = "OptionsAction";
  public final static String EVAL_ACTION    = "EvalAction";
  public final static String EVAL_S_ACTION  = "EvalSAction";
//  public final static String STOP_ACTION    = "StopAction";
  public final static String REINIT_ACTION  = "ReinitAction";
  public final static String ABOUT_ACTION   = "AboutAction";
  
  /**
   * The number of instances of this class
   */
  protected static int instances;
  
  /**
   * The resource file name
   */
  protected final static String RESOURCE = "koala.dynamicjava.gui.resources.main";
  
  /**
   * The resource bundle
   */
  protected static ResourceBundle bundle;
  
  /**
   * The resource manager
   */
  protected static ResourceManager rManager;
  
  /**
   * The editor
   */
  protected Editor editor;
  
  /**
   * The text area used to display the output
   */
  protected JTextArea output;
  
  /**
   * The output area vertical scroll bar model
   */
  protected BoundedRangeModel scrollBarModel;
  
  /**
   * The status bar
   */
  protected StatusBar status;
  
  /**
   * The options dialog
   */
  protected OptionsDialog options;
  
  /**
   * The DynamicJava current interpreter
   */
  protected Interpreter interpreter;
  
  /**
   * The current selection start
   */
  protected int selectionStart = -1;
  
  /**
   * The current selection end
   */
  protected int selectionEnd = -1;
  
  /**
   * The evaluator
   */
  protected EvalAction evalAction = new EvalAction();
  
  /**
   * The selection evaluator
   */
  protected EvalSelectionAction evalSelection = new EvalSelectionAction();
  
//  /**
//   * The stop action
//   */
//  protected StopAction stopAction = new StopAction();
  
  /**
   * The current interpreter thread
   */
  protected Thread thread;
  
  /**
   * Is the interpreter running?
   */
  protected boolean isRunning;
  
  /**
   * The object used to store the options
   */
  protected OptionsDialog.OptionSet optionSet;
  
  /**
   * The text component stream
   */
  protected PrintStream textComponentStream;
  
  /**
   * The current output stream
   */
  protected PrintStream out = System.out;
  
  /**
   * The current error stream
   */
  protected PrintStream err = System.err;
  
  static {
    bundle = ResourceBundle.getBundle(RESOURCE, Locale.getDefault());
    rManager = new ResourceManager(bundle);
  }
  
  /**
   * Creates the interface
   */
  public Main() {
    instances++;
    
    setTitle(rManager.getString("Frame.title"));
    setSize(rManager.getInteger("Frame.width"), rManager.getInteger("Frame.height"));
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        exit();
      }
    });
    
    getContentPane().add("South", status = new StatusBar(rManager));
    
    // Create the input and output areas
    JScrollPane scroll1 = new JScrollPane();
    scroll1.getViewport().add(editor = new Editor(status));
    scroll1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    
    editor.addCaretListener(new EditorCaretListener());
    
    JScrollPane scroll2 = new JScrollPane();
    scroll2.getViewport().add(output = new JTextArea());
    scroll2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    
    scrollBarModel = scroll2.getVerticalScrollBar().getModel();
    scrollBarModel.addChangeListener(new ScrollBarModelChangeListener());
    
    output.setEditable(false);
    output.setLineWrap(true);
    output.setBackground(Color.lightGray);
    
    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                      true,
                                      scroll1,
                                      scroll2);
    split.setDividerLocation(rManager.getInteger("Frame.divider"));
    
    getContentPane().add(split);
    
    // Put the menu actions in the table
    listeners.put(OPEN_ACTION,     editor.getAction("OpenAction"));
    listeners.put(SAVE_ACTION,     editor.getAction("SaveAction"));
    listeners.put(SAVE_AS_ACTION,  editor.getAction("SaveAsAction"));
    listeners.put(EXIT_ACTION,     new ExitAction());
    listeners.put(UNDO_ACTION,     editor.getAction("UndoAction"));
    listeners.put(REDO_ACTION,     editor.getAction("RedoAction"));
    listeners.put(CUT_ACTION,      editor.getAction("cut-to-clipboard"));
    listeners.put(COPY_ACTION,     editor.getAction("copy-to-clipboard"));
    listeners.put(PASTE_ACTION,    editor.getAction("paste-from-clipboard"));
    listeners.put(CLEAR_ACTION,    new ClearAction());
    listeners.put(OPTIONS_ACTION,  new OptionsAction());
    listeners.put(EVAL_ACTION,     evalAction);
    listeners.put(EVAL_S_ACTION,   evalSelection);
//    listeners.put(STOP_ACTION,     stopAction);
    listeners.put(REINIT_ACTION,   new ReinitAction());
    listeners.put(ABOUT_ACTION,    new AboutAction());
    
    // Create the menu
    MenuFactory mf = new MenuFactory(bundle, this);
    try {
      setJMenuBar(mf.createJMenuBar("MenuBar"));
    } catch (MissingResourceException e) {
      System.out.println(e.getMessage());
      System.exit(0);
    }
    
    // Create the toolbar
    ToolBarFactory tbf = new ToolBarFactory(bundle, this);
    try {
      JToolBar tb = tbf.createJToolBar("ToolBar");
      tb.setFloatable(false);
      getContentPane().add("North", tb);
      
    } catch (MissingResourceException e) {
      System.out.println(e.getMessage());
      System.exit(0);
    }
    
    textComponentStream = new PrintStream(new JTextComponentOutputStream(output));
    
    options = new OptionsDialog(this);
    loadOptions();
    
    // Run the initialization script if requested
    if (options.isStartupInitializationSelected()) {
      interpreter = createInterpreter();
      applyOptions();
      
      String s = options.getStartupInitializationFilename();
      Reader r = null;
      PrintStream oldout = System.out;
      PrintStream olderr = System.err;
      System.setOut(out);
      System.setErr(err);
      try {
        try {
          r = new InputStreamReader(new URL(s).openStream());
        } catch (Exception e) {
          r = new FileReader(s);
        }
        interpreter.interpret(r, s);
      } catch (Throwable e) {
        JOptionPane.showMessageDialog
          (this,
           rManager.getString("InterpreterInitializationError.text") +
           "\n" + e.getMessage(),
           rManager.getString("InterpreterInitializationError.title"),
           JOptionPane.ERROR_MESSAGE);
      } finally {
        System.setOut(oldout);
        System.setErr(olderr);
      }
    }
    
    interpreter = createInterpreter();
    applyOptions();
  }
  
  /**
   * Sets the options
   */
  public void setOptions(OptionsDialog.OptionSet opt) {
    options.setOptions(opt);
  }
  
  /**
   * Gets the options
   */
  public OptionsDialog.OptionSet getOptions() {
    return options.getOptions();
  }
  
  /**
   * Saves the options to System.getProperty("user.home") + "/.djava/options"
   */
  public void saveOptions() throws IOException {
    OptionsDialog.OptionSet opt = options.getOptions();
    
    // Create the .djava directory if it does not exist
    String dirName = System.getProperty("user.home") + "/.djava";
    File f = new File(dirName);
    if (!f.exists()) {
      f.mkdir();
    }
    
    // Create the options script
    String ls = System.getProperty("line.separator");
    Writer w = new FileWriter(dirName + "/options");
    w.write("// Generated by DynamicJava" + ls);
    w.write("import koala.dynamicjava.gui.*;" + ls + ls);
    w.write("OptionsDialog.OptionSet optionSet = new OptionsDialog.OptionSet();" +
            ls + ls);
    
    w.write("optionSet.classPath = new String[] { " + ls);
    if (opt.classPath.length > 0) {
      w.write("    " + stringToJavaString(opt.classPath[0]));
    }
    for (int i = 1; i < opt.classPath.length; i++) {
      w.write("," + ls + "    " + stringToJavaString(opt.classPath[i]));
    }
    w.write(" };" + ls + ls);
    
    w.write("optionSet.libraryPath = new String[] { " + ls);
    if (opt.libraryPath.length > 0) {
      w.write("    " + stringToJavaString(opt.libraryPath[0]));
    }
    for (int i = 1; i < opt.libraryPath.length; i++) {
      w.write("," + ls + "    " + stringToJavaString(opt.libraryPath[i]));
    }
    w.write(" };" + ls + ls);
    
    w.write("optionSet.isInterpreterSelected = " + opt.isInterpreterSelected + ";" + ls);
    w.write("optionSet.interpreterName = \"" + opt.interpreterName + "\";" + ls + ls);
    
    w.write("optionSet.interpreterFileSelected = " + opt.interpreterFileSelected +
            ";" + ls);
    w.write("optionSet.interpreterFilename = " +
            stringToJavaString(opt.interpreterFilename) + ";" + ls + ls);
    
    w.write("optionSet.isGUISelected = " + opt.isGUISelected + ";" + ls);
    w.write("optionSet.guiName = \"" + opt.guiName + "\";" + ls + ls);
    
    w.write("optionSet.isOutputSelected = " + opt.isOutputSelected + ";" + ls + ls);
    w.write("optionSet.isErrorSelected = " + opt.isErrorSelected + ";" + ls + ls);
    
    w.write("optionSet.guiFileSelected = " + opt.guiFileSelected + ";" + ls);
    w.write("optionSet.guiFilename = " +
            stringToJavaString(opt.guiFilename) + ";" + ls + ls);
    
    w.write("gui.setOptions(optionSet);" + ls);
    
    w.flush();
  }
  
  /**
   * translates a string to a java source string
   */
  protected String stringToJavaString(String s) {
    String result = "\"";
    for (int i = 0; i < s.length(); i++) {
      switch (s.charAt(i)) {
        case '\\':
        case '"':
          result += "\\" + s.charAt(i); break;
        default:
          result += s.charAt(i);
      }
    }
    return result + "\"";
  }
  
  /**
   * Loads the options
   */
  public void loadOptions() {
    PrintStream oldout = System.out;
    PrintStream olderr = System.err;
    System.setOut(out);
    System.setErr(err);
    Interpreter interpreter = createInterpreter();
    try {
      File f = new File(System.getProperty("user.home") + "/.djava/options");
      if (f.exists()) {
        interpreter.defineVariable("gui", this);
        interpreter.interpret(new FileReader(f), "options");
      }
    } catch (Throwable e) {
      System.out.println(e);
    } finally {
      System.setOut(oldout);
      System.setErr(olderr);
    }
  }
  
  /**
   * Returns the options dialog
   */
  public OptionsDialog getOptionsDialog() {
    return options;
  }
  
  /**
   * Returns the editor
   */
  public Editor getEditor() {
    return editor;
  }
  
  /**
   * Returns the output area
   */
  public JTextArea getOutputArea() {
    return output;
  }
  
  /**
   * Called when the interface exits
   */
  protected void exit() {
    editor.closeProcedure();
    if (--instances == 0) {
      System.exit(0);
    }
  }
  
  /**
   * Reinitializes the interpreter
   */
  protected void reinitializeInterpreter() {
    interpreter = createInterpreter();
  }
  
  /**
   * Applies the options
   */
  protected void applyOptions() {
    // Update the classpath
    String[] classpath = options.getClassPath();
    for (int i = 0; i < classpath.length; i++) {
      String s = classpath[i];
      try {
        interpreter.addClassURL(new URL(s));
      } catch (MalformedURLException e) {
        interpreter.addClassPath(s);
      }
    }
    
    // Update the library path
    String[] libpath = options.getLibraryPath();
    for (int i = 0; i < libpath.length; i++) {
      interpreter.addLibraryPath(libpath[i]);
    }
    
    // Define the interpreter if requested
    if (options.isInterpreterDefined()) {
      interpreter.defineVariable(options.getInterpreterName(), interpreter);
    }
    
    // Define the GUI if requested
    if (options.isGUIDefined()) {
      interpreter.defineVariable(options.getGUIName(), this);
    }
    
    // Redirect the output if requested
    out = (options.isOutputSelected()) ? textComponentStream : System.out;
    
    // Redirect the standard error if requested
    err = (options.isErrorSelected()) ? textComponentStream : System.err;
    
    // Run the initialization script if requested
    if (options.isInitializationSelected()) {
      String s = options.getInitializationFilename();
      Reader r = null;
      PrintStream oldout = System.out;
      PrintStream olderr = System.err;
      System.setOut(out);
      System.setErr(err);
      try {
        try {
          r = new InputStreamReader(new URL(s).openStream());
        } catch (Exception e) {
          r = new FileReader(s);
        }
        interpreter.interpret(r, s);
      } catch (Throwable e) {
        JOptionPane.showMessageDialog
          (this,
           rManager.getString("InterpreterInitializationError.text") +
           "\n" + e.getMessage(),
           rManager.getString("InterpreterInitializationError.title"),
           JOptionPane.ERROR_MESSAGE);
      } finally {
        System.setOut(oldout);
        System.setErr(olderr);
      }
    }
  }
  
  /**
   * Returns the line number that match the given position
   * @param p a position
   */
  protected int getCurrentLine(int p) {
    String t      = editor.getText();
    int    result = 1;
    for (int i = 0; i < p; i++) {
      if (t.charAt(i) == '\n') {
        result++;
      }
    }
    return result;
  }
  
  /**
   * Restores the options
   */
  protected void restoreOptions() {
    options.setOptions(optionSet);
  }
  
  /**
   * Creates a new interpreter
   */
  protected Interpreter createInterpreter() {
    Interpreter result = new TreeInterpreter(new JavaCCParserFactory());
    result.addLibrarySuffix(".java");
    return result;
  }
  
  // ActionMap implementation
  
  /**
   * The map that contains the listeners
   */
  protected Map<String,Action> listeners = new HashMap<String,Action>();
  
  /**
   * Returns the action associated with the given string
   * or null on error
   * @param key the key mapped with the action to get
   * @throws MissingListenerException if the action is not found
   */
  public Action getAction(String key) throws MissingListenerException {
    return listeners.get(key);
  }
  
  // Actions
  
  /**
   * To exit the application
   */
  protected class ExitAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      exit();
    }
  }
  
  /**
   * To clear the output
   */
  protected class ClearAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      output.setText("");
    }
  }
  
  /**
   * To pop the Options dialog
   */
  protected class OptionsAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      optionSet = options.getOptions();
      Rectangle fr = getBounds();
      Dimension od = options.getSize();
      options.setLocation(fr.x + (fr.width  - od.width) / 2,
                          fr.y + (fr.height - od.height) / 2);
      options.setVisible(true);
    }
  }
  
  /**
   * To evaluate the content of the buffer
   */
  protected class EvalAction extends AbstractAction
    implements JComponentModifier {
    java.util.List<JComponent> components = new LinkedList<JComponent>();
    
    public void actionPerformed(ActionEvent ev) {
      StringReader sr = new StringReader(editor.getText());
      thread = new InterpreterThread(sr);
      thread.start();
    }
    
    public void addJComponent(JComponent c) {
      components.add(c);
      c.setEnabled(true);
    }
    
    protected void update() {
      Iterator<JComponent> it = components.iterator();
      while (it.hasNext()) {
        it.next().setEnabled(!isRunning);
      }
    }
  }
  
  /**
   * To run the interpreter
   */
  protected class InterpreterThread extends Thread {
    Reader reader;
    
    InterpreterThread(Reader r) {
      reader = r;
      setPriority(Thread.MIN_PRIORITY);
    }
    
    public void run() {
      ThreadDeath td = null;
      PrintStream oldout = System.out;
      PrintStream olderr = System.err;
      System.setOut(out);
      System.setErr(err);
      try {
        isRunning = true;
//        stopAction.update();
        evalAction.update();
        evalSelection.update();
        output.append("==> " + interpreter.interpret(reader, "buffer") + "\n");
      } catch (InterpreterException e) {
        output.append(" *** " + e.getMessage() + "\n");
      } catch (ThreadDeath e) {
        td = e;
      } catch (Throwable e) {
        output.append(e + "\n");
      } finally {
        System.setOut(oldout);
        System.setErr(olderr);
      }
      isRunning = false;
//      stopAction.update();
      evalAction.update();
      evalSelection.update();
      
      if (td != null) {
        throw td;
      }
    }
  }
  
  /**
   * To evaluate the content of the selection
   */
  protected class EvalSelectionAction extends    AbstractAction
    implements JComponentModifier {
    java.util.List<JComponent> components = new LinkedList<JComponent>();
    
    public void actionPerformed(ActionEvent ev) {
      String s = editor.getSelectedText();
      if (s != null) {
        StringReader sr = new StringReader(s);
        thread = new InterpreterThread(sr);
        thread.start();
      }
    }
    
    public void addJComponent(JComponent c) {
      components.add(c);
      c.setEnabled(false);
    }
    
    protected void update() {
      Iterator<JComponent> it = components.iterator();
      while (it.hasNext()) {
        it.next().setEnabled(selectionStart != -1 && !isRunning);
      }
    }
  }
  
//  /**
//   * To stop the interpreter thread
//   */
//  protected class StopAction extends AbstractAction
//    implements JComponentModifier {
//    java.util.List<JComponent> components = new LinkedList<JComponent>();
//    
//    public void actionPerformed(ActionEvent ev) {
//      thread.stop();
//      isRunning = false;
//      update();
//      evalAction.update();
//      evalSelection.update();
//      status.setMessage("Status.evaluation.stopped");
//    }
//    
//    public void addJComponent(JComponent c) {
//      components.add(c);
//      c.setEnabled(false);
//    }
//    
//    protected void update() {
//      Iterator<JComponent> it = components.iterator();
//      while (it.hasNext()) {
//        it.next().setEnabled(isRunning);
//      }
//    }
//  }
//  
  /**
   * Reinitializes the interpreter
   */
  protected class ReinitAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      reinitializeInterpreter();
      applyOptions();
      status.setMessage("Status.interpreter.reinitialized");
    }
  }
  
  /**
   * Pop the About dialog
   */
  protected class AboutAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      JOptionPane.showMessageDialog(Main.this,
                                    bundle.getString("AboutMessage"));
    }
  }
  
  /**
   * To listen to the editor caret
   */
  protected class EditorCaretListener implements CaretListener {
    public void caretUpdate(CaretEvent e) {
      int p1 = e.getDot();
      int p2 = e.getMark();
      
      status.setLine(getCurrentLine(p1));
      
      if (p1 != p2) {
        if (p1 > p2) {
          int t = p2;
          p2 = p1;
          p1 = t;
        }
        selectionStart = p1;
        selectionEnd   = p2;
      } else {
        selectionStart = -1;
        selectionEnd   = -1;
      }
      evalSelection.update();
    }
  }
  
  /**
   * To listen to the changes in the output area vertical scroll bar model
   */
  protected class ScrollBarModelChangeListener implements ChangeListener {
    int oldMax;
    public void stateChanged(ChangeEvent e) {
      if (oldMax != scrollBarModel.getMaximum()) {
        oldMax = scrollBarModel.getMaximum();
        scrollBarModel.setValue(oldMax);
      }
    }
  }
}
