/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import java.io.*;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.debug.*;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.config.OptionConstants;

import com.bluemarsh.jswat.*;
import com.bluemarsh.jswat.ui.*;
import com.bluemarsh.jswat.view.*;

/** 
 * Panel for displaying the debugger input and output in MainFrame.
 * @version $Id$
 */
public class DebugPanel extends JPanel implements OptionConstants {
  
  private static final String BANNER_TEXT = "JSwat Debugger Console\n\n";

  private final SingleDisplayModel _model;
  private final MainFrame _frame;
  private final DebugManager _debugger;
  
  private final Hashtable _jswatProperties;
  private final UIAdapter _uiAdapter;

  private final StyledDocument _outputDoc;
  private final JScrollPane _scrollPane;
  private final JTextPane _outputPane;
  private final JTextField _inputField; 
  
  private final DebugLogger _logger;

    //  private Object _curBreakpointTag=null; // currently highlighted line
    //  private int    _curBP;                 // and its associated tag.
    
  /**
   * Action performed when the Enter key is pressed.
   */
  private ActionListener _performAction = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      _performCommand();
    }
  };                                                             

  /**
   * Highlighter
   */
  /** Highlight painter for selected list items. */
    //  private static final DefaultHighlighter.DefaultHighlightPainter
    //    _breakpointHighlightPainter
    //      = new DefaultHighlighter.DefaultHighlightPainter(new Color(255,155,155));

  /**
   * Highlighter for active breakpoint.
   */
  /** Highlight painter for selected list items. */
    //  private static final DefaultHighlighter.DefaultHighlightPainter
    //    _activeBreakpointHighlightPainter
    //      = new DefaultHighlighter.DefaultHighlightPainter(new Color(255,155,155));
    
  /**
   * Constructor.
   * @param model SingleDisplayModel in which we are running
   * @param frame MainFrame in which we are displayed
   */
  public DebugPanel(SingleDisplayModel model, MainFrame frame) {
    _model = model;
    _frame = frame;
    _debugger = _model.getDebugManager();
    
    _jswatProperties = new Hashtable();
    _uiAdapter = new DebugPanelUIAdapter();
    
    // Set up layout of panel
    setLayout(new BorderLayout());

    _outputDoc = _model.getDebugDocument();  
    _outputPane = new JTextPane(_outputDoc);
    _outputPane.setEditable(false);
    _scrollPane = new BorderlessScrollPane(_outputPane,
                                           JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                           JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    _scrollPane.setBorder(null); // removes all default borders
    
    _inputField = new JTextField();
      
    // Add the fields to the panel
    add(_scrollPane, BorderLayout.CENTER);
    // Only add input field in advanced mode
    boolean advancedMode = 
      DrJava.CONFIG.getSetting(DEBUGGER_ADVANCED).booleanValue();
    if (advancedMode && CodeStatus.DEVELOPMENT) {
      add(_inputField, BorderLayout.SOUTH);
    
      // Listen for enter key
      _inputField.addActionListener(_performAction);
    }
    
    _logger = new DebugLogger(System.err);
    reset();
  }
  
  /**
   * Returns the UIAdapter used by JSwat.
   */
  public UIAdapter getUIAdapter() {
    return _uiAdapter;
  }
  
  /**
   * Sets the font for displaying text.
   * @param f Font to be used for input and output
   */
  public void setFonts(Font f) {
    _outputPane.setFont(f);
    _inputField.setFont(f);
  }
  
  /**
   * Sends the command currently in the inField to the JSwat debugger.
   * (Specific to JSwat, used in Advanced mode.)
   */
  private void _performCommand() {
    String cmd = _inputField.getText().trim();
    if (!cmd.equals("")) {
      _inputField.setText("");
      _appendString(cmd + "\n");

      // Execute command
      _model.getDebugManager().performCommand(cmd);
    }
  }
  
  /**
   * Appends the given string to the output document and scrolls
   * to the end.
   */
  private void _appendString(String s) {
    try {
      _outputDoc.insertString(_outputDoc.getLength(), s, null);

      // Scroll to end
      _outputPane.setCaretPosition(_outputPane.getText().length());
    }
    catch (BadLocationException e) {
      // Can't happen: getLength() and endPos are always legal
    }
    _outputPane.repaint();
  }
  
  /**
   * Resets the output document by removing all text and inserting the banner.
   */
  public void reset() {
    try {
      _outputDoc.remove(0, _outputDoc.getLength());
    }
    catch (BadLocationException e) {
      // Can't happen: 0 and getLength() are always legal
    }
    
    _appendString(BANNER_TEXT);
  }
  
 

  /**
   * A Writer which writes to the output pane.
   */
  private class DebugLogger extends BufferedWriter {
    public DebugLogger(OutputStream os) {
      super(new PrintWriter(os));
    }
    
    /**
     * Writes a line to the output document.
     */
    public void write(String s) {
      _appendString(s);
    }
  }
  
  /**
   * A UIAdapter for JSwat, to be used for displaying JSwat's status.
   */
  class DebugPanelUIAdapter implements UIAdapter {
    
    /**
     * Construct the appropriate user interface and connect all
     * the pieces together. The result should be a fully
     * functional interface that is ready to be used.
     */
    public void buildInterface() {
      //DrJava.consoleErr().println("DP: building interface...");
      _debugger.attachLogWriter(_logger);
    }
    
    /**
     * Indicate if this interface adapter has the ability to find
     * a string in the currently selected source view.
     *
     * @return  true if the ability exists, false otherwise.
     */
    public boolean canFindString() {
      // False for now, but we can use Find/Replace later
      return false;
    }
    
    /**
     * Indicate if this interface adapter has the ability to show
     * source files in a manner appropriate for the user to read.
     *
     * @return  true if the ability exists, false otherwise.
     */
    public boolean canShowFile() {
      //DrJava.consoleErr().println("DP: saying that I can show a file...");
      return true;
    }    
    
    /**
     * Indicate if this interface adapter has the ability to show
     * the status in a manner appropriate for the user to view.
     *
     * @return  true if the ability exists, false otherwise.
     */
    public boolean canShowStatus() {
      return false;
    }
    
    /**
     * Deconstruct the user interface such that all components
     * are made invisible and prepared for non-use.
     */
    public void destroyInterface() {
    }
    
    /**
     * This is called when there are no more open Sessions. The
     * adapter should take the appropriate action at this time.
     * In most cases that will be to exit the JVM.
     */
    public void exit() {
      _frame.hideDebugger();
    }
    
    /**
     * Search for the given string in the currently selected source view.
     * The search should continue from the last successful match, and
     * wrap around to the beginning when the end is reached.
     *
     * @param  query       string to look for.
     * @param  ignoreCase  true to ignore case.
     * @return  true if string was found.
     * @exception  NoOpenViewException
     *             Thrown if there is no view to be searched.
     */
    public boolean findString(String query, boolean ignoreCase)
      throws NoOpenViewException {
      
      return false;
    }
    
    /**
     * Searches for the property with the specified key in the property
     * list. The method returns null if the property is not found.
     *
     * @param  key  the property key.
     * @return  the value in the property list with the specified key value.
     */
    public Object getProperty(String key) {
      return _jswatProperties.get(key);
    }
    
    /**
     * Retrieves the currently active view in JSwat.
     *
     * @return  selected view, or null if none selected.
     */
    public JSwatView getSelectedView() {
      return null;
    }
    
    /**
     * Called when the Session initialization has completed.
     */
    public void initComplete() {
      //DrJava.consoleErr().println("DP: init complete...");
    }
    
    /**
     * Refresh the display to reflect changes in the program.
     * Generally this means refreshing the panels.
     */
    public void refreshDisplay() {
      //DrJava.consoleErr().println("DP: refreshing display...");
      _outputPane.repaint();
      _inputField.repaint();
    }
    
    /**
     * Save any settings to the appropriate places, the program
     * is about the terminate.
     */
    public void saveSettings() {
    }
    
    /**
     * Stores the given value in the properties list with the given
     * key as a reference. If the value is null, then the key and
     * value will be removed from the properties.
     *
     * @param  key    the key to be placed into this property list.
     * @param  value  the value corresponding to key, or null to remove
     *                the key and value from the properties.
     * @return  previous value stored using this key.
     */
    public Object setProperty(String key, Object value) {
      if (value == null) {
        return _jswatProperties.remove(value);
      }
      else {
        return _jswatProperties.put(key, value);
      }
    }
    
    /**
     * Show the given file in the appropriate view and make the
     * given line visible in that view.
     *
     * @param  src    source to be displayed.
     * @param  line   one-based line to be made visible, or zero for
     *                a reasonable default.
     * @param  count  number of lines to display, or zero for a
     *                reasonable default. Some adapters will ignore
     *                this value if, for instance, they utilize a
     *                scrollable view.
     * @return  true if successful, false if error.
     */

    public boolean showFile(SourceSource src, int line, int count) {
      //DrJava.consoleErr().println("DP: showFile()...");
      if (src instanceof FileSource) {
        try {
          File file = ((FileSource)src).getFile();
          if (file.exists()) {
       //            DrJava.consoleOut().println("DebugPanel: file: " + file.getName());
            OpenDefinitionsDocument doc = _model.getDocumentForFile(file);
            _model.setActiveDocument(doc);     
     //            DrJava.consoleErr().println("Showing line " + line);     
            if (line > 0) {
  /*
        if (_curBP>0 && line != _curBP) // remove existing bp
  _frame.getCurrentDefPane().getHighlighter().removeHighlight(_curBreakpointTag);

       _curBreakpointTag = highlightLine(line, _activeBreakpointHighlightPainter);
       _curBP = line;
  */    
       // _frame.getCurrentDefPane().setCaretPosition(doc.getDocument().getCurrentLocation());
       
            }
            _inputField.grabFocus();
            return true;
          }
          else {
            String name = file.getName();
            if ((name.length() > 5) && 
                (name.substring(name.length() - 5).equals(".java"))) {
              if (name.lastIndexOf(".",name.length() - 6) != -1)
                name = name.substring(name.lastIndexOf(".",name.length() - 6));
            }
            else {
              if (name.lastIndexOf(".") == -1)
                name = name + ".java";
              else
                name = name.substring(name.lastIndexOf(".")) + ".java";
            }
            file = new File(name);
            if (file.exists()) {
              //DrJava.consoleOut().println("DebugPanel: file: " + file.getName());
              //DrJava.consoleOut().println(" NEEDED HACK.");
              OpenDefinitionsDocument doc = _model.getDocumentForFile(file);
              _model.setActiveDocument(doc);
       //              DrJava.consoleErr().println("Showing line " + line);
              if (line > 0) {
    /* no highlighting
  if (_curBP>0 && line != _curBP) // remove existing bp
    _frame.getCurrentDefPane().getHighlighter().removeHighlight(_curBreakpointTag);

           _curBreakpointTag = highlightLine(line, _activeBreakpointHighlightPainter);
         _curBP = line;
    */
         // _frame.getCurrentDefPane().setCaretPosition(doc.getDocument().getCurrentLocation());

              }
              _inputField.grabFocus();
              return true;
            }
            else {
              //DrJava.consoleOut().println("DebugPanel: file: " + file.getName());
              //DrJava.consoleOut().println("  didn't exist.");
            }
          }
        }
        catch (Exception e) {
          // ACK: clean this up
          DrJava.consoleErr().println("DebugPanel: Error showing file: " + e);
          e.printStackTrace();
          return false;
        }
      }
      return false;
    }
    
    /**
     * Show a status message in a reasonable location.
     *
     * @param  status  message to be shown to the user.
     */
    public void showStatus(String status) {
    }
  }

  /**
    * Highlights the given line.
    * @param  line  the line to highlight
    * @param  brush the highlighter to use.
    *
    * @return the highlight tag for removal later (Object)
    */
    /*
  public Object highlightLine(int line, DefaultHighlighter.DefaultHighlightPainter brush) {
      
      OpenDefinitionsDocument doc = _model.getActiveDocument();
      doc.syncCurrentLocationWithDefinitions(doc.getDocument().getCurrentLocation());
      doc.gotoLine(line);
      
      int curPos = doc.getDocument().getCurrentLocation();
      int startPos = doc.getDocument().getLineStartPos(curPos);
      int endPos = doc.getDocument().getLineEndPos(curPos);
      Object o=null;      
      try {
     o = _frame.getCurrentDefPane().getHighlighter().addHighlight(startPos,
               endPos,
               brush);
      } catch (BadLocationException badBadLocation) { System.err.println("DebugPanel.highlightLine() Got a ble."); }
      return o;
  }

    */
  
   /**
    * Highlights the given region (not used right now)
    *
    * @param  start  the start pos of region to highlight
    * @param  start  the end pos of region to highlight    
    * @param  brush  the highlighter to use.
    *
    * @return the highlight tag for removal later (Object)
    */

    /*
  public Object highlightRegion(int start, int end, DefaultHighlighter.DefaultHighlightPainter brush) {
 Object o=null;      
 try {
     o = _frame.getCurrentDefPane().getHighlighter().addHighlight(start,
          end,
          brush);
      } catch (BadLocationException badBadLocation) { System.err.println("DebugPanel.highlightRegion() Got a ble."); }
 return o;
 } */
}
    
