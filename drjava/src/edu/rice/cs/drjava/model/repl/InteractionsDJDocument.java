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
 END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

import edu.rice.cs.util.Pair;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.EditDocumentException;
import edu.rice.cs.util.text.ConsoleDocument;

import java.io.*;
import java.awt.*;
import java.util.List;
import java.util.LinkedList;
import javax.swing.text.AbstractDocument;

import static edu.rice.cs.drjava.model.definitions.ColoringView.*;

/** Represents the Interactions Document Adapter. Extends from the Abstract DrJava Document, 
 *  which contains shared code between the interactions and definitions documents.
 */
public class InteractionsDJDocument extends AbstractDJDocument {
  
  /** Holds a flag telling the adapter that the interpreter was recently reset, and to reset the styles list 
   *  the next  time a style is added. Cannot reset immediately because then the styles would be lost while 
   *  the interactions pane is resetting.
   */
  private boolean _toClear = false;
  
  /** Standard constructor. Currently does nothing */
  public InteractionsDJDocument() { super(); }  
  
  protected int startCompoundEdit() { return 0; /* Do nothing */ }
  
  protected void endCompoundEdit(int key) { /* Do nothing */ }

  protected void endLastCompoundEdit() { /* Do nothing */ }
  
  protected void addUndoRedo(AbstractDocument.DefaultDocumentEvent chng, Runnable undoCommand, Runnable doCommand) { }
  protected void _styleChanged() { /* Do nothing */ }
  
  /** Returns a new indenter. Eventually to be used to return an interactions indenter */
  protected Indenter makeNewIndenter(int indentLevel) { return new Indenter(indentLevel); }
  
  /** A list of styles and their locations. This list holds pairs of locations in the document and styles, which
   *  is basically a map of regions where the coloring view that is now attached to the Interactions Pane is not 
   *  allowed to use the reduced model to determine the color settings when rendering text. We keep a list of all 
   *  places where styles not considered by the reduced model are being used, such as System.out, System.err, 
   *  and the various return styles for Strings and other Objects.  Since the LinkedList class is not thread safe, 
   *  we have to synchronized all methods that access pointers in _stylesList and the associated boolean _toClear.
   */
  private List<Pair<Pair<Integer,Integer>,String>> _stylesList = new LinkedList<Pair<Pair<Integer,Integer>,String>>();
  
  /** Adds the given coloring style to the styles list. */
  public void addColoring(int start, int end, String style) {
    synchronized(_stylesList) {
      if (_toClear) {
        _stylesList.clear();    
        _toClear = false;
      }
      if (style != null)
        _stylesList.add(0, new Pair<Pair<Integer,Integer>,String>
                        (new Pair<Integer,Integer>(new Integer(start),new Integer(end)), style));
    }
  }
  
  /** Package protected accessor method used for test cases */
  List<Pair<Pair<Integer, Integer>, String>> getStylesList() { return _stylesList; }
  
  /** Attempts to set the coloring on the graphics based upon the content of the styles list
   *  returns false if the point is not in the list.
   */
  public boolean setColoring(int point, Graphics g) {
    synchronized(_stylesList) {
      for(Pair<Pair<Integer,Integer>,String> p :  _stylesList) {
        Pair<Integer,Integer> loc = p.getFirst();
        if (loc.getFirst() <= point && loc.getSecond() >= point) {
          if (p.getSecond().equals(InteractionsDocument.ERROR_STYLE)) {
            //DrJava.consoleErr().println("Error Style");
            g.setColor(ERROR_COLOR);   
            g.setFont(g.getFont().deriveFont(Font.BOLD));
          }
          else if (p.getSecond().equals(InteractionsDocument.DEBUGGER_STYLE)) {
            //DrJava.consoleErr().println("Debugger Style");
            g.setColor(DEBUGGER_COLOR);
            g.setFont(g.getFont().deriveFont(Font.BOLD));
          }
          else if (p.getSecond().equals(ConsoleDocument.SYSTEM_OUT_STYLE)) {
            //DrJava.consoleErr().println("System.out Style");
            g.setColor(INTERACTIONS_SYSTEM_OUT_COLOR);
            g.setFont(MAIN_FONT);
          }
          else if (p.getSecond().equals(ConsoleDocument.SYSTEM_IN_STYLE)) {
            //DrJava.consoleErr().println("System.in Style");
            g.setColor(INTERACTIONS_SYSTEM_IN_COLOR);
            g.setFont(MAIN_FONT);
          }
          else if (p.getSecond().equals(ConsoleDocument.SYSTEM_ERR_STYLE)) {
            //DrJava.consoleErr().println("System.err Style");
            g.setColor(INTERACTIONS_SYSTEM_ERR_COLOR);
            g.setFont(MAIN_FONT);
          }
          else if (p.getSecond().equals(InteractionsDocument.OBJECT_RETURN_STYLE)) {
            g.setColor(NORMAL_COLOR);
            g.setFont(MAIN_FONT);
          }
          else if (p.getSecond().equals(InteractionsDocument.STRING_RETURN_STYLE)) {
            g.setColor(DOUBLE_QUOTED_COLOR);
            g.setFont(MAIN_FONT);
          }
          else if (p.getSecond().equals(InteractionsDocument.NUMBER_RETURN_STYLE)) {
            g.setColor(NUMBER_COLOR);
            g.setFont(MAIN_FONT);
          }
          else if (p.getSecond().equals(InteractionsDocument.CHARACTER_RETURN_STYLE)) {
            g.setColor(SINGLE_QUOTED_COLOR);
            g.setFont(MAIN_FONT);
          }
          else return false; /* Normal text color */ 
          
          return true;
        }
      }
      return false;
    }
  }
  
  /** Attempts to set the font on the graphics context based upon the styles held in the styles list. */
  public void setBoldFonts(int point, Graphics g) {
    synchronized(_stylesList) {
      for(Pair<Pair<Integer,Integer>,String> p :  _stylesList) {
        Pair<Integer,Integer> loc = p.getFirst();
        if (loc.getFirst() <= point && loc.getSecond() >= point) {
          if (p.getSecond().equals(InteractionsDocument.ERROR_STYLE))
            g.setFont(g.getFont().deriveFont(Font.BOLD));
          else if (p.getSecond().equals(InteractionsDocument.DEBUGGER_STYLE))
            g.setFont(g.getFont().deriveFont(Font.BOLD));
          else  g.setFont(MAIN_FONT);
          return;
        }
      }
    }
  }
    
  /** Called when the Interactions pane is reset. */
  public void clearColoring() { synchronized(_stylesList) { _toClear = true; } }
  
  /** Returns true iff the end of the current interaction is an open comment block
   *  @return true iff the end of the current interaction is an open comment block
   */
  public boolean isInCommentBlock() {
    readLock();
    try {
      synchronized(_reduced) {
        resetReducedModelLocation();
        ReducedModelState state = stateAtRelLocation(getLength() - _currentLocation);
        boolean toReturn = (state.equals(ReducedModelStates.INSIDE_BLOCK_COMMENT));
        return toReturn;
      }
    }
    finally { readUnlock(); }
  }
  
  /** Inserts the given exception data into the document with the given style.
   *  @param exceptionClass Name of the exception that was thrown
   *  @param message Message contained in the exception
   *  @param stackTrace String representation of the stack trace
   *  @param styleName name of the style for formatting the exception
   */
  public void appendExceptionResult(String exceptionClass, String message, String stackTrace, String styleName) {
    
    String c = exceptionClass;
    if (c.indexOf('.') != -1) c = c.substring(c.lastIndexOf('.') + 1, c.length());
    
    modifyLock();
    try {
      insertText(getLength(), c + ": " + message + "\n", styleName);
      
      // An example stack trace:
      //
      // java.lang.IllegalMonitorStateException:
      // at java.lang.Object.wait(Native Method)
      // at java.lang.Object.wait(Object.java:425)
      if (! stackTrace.trim().equals("")) {
        BufferedReader reader = new BufferedReader(new StringReader(stackTrace));
        
        String line;
        // a line is parsable if it has ( then : then ), with some
        // text between each of those
        while ((line = reader.readLine()) != null) {
          String fileName;
          int lineNumber;
          
          // TODO:  Why is this stuff here??
          int openLoc = line.indexOf('(');
          if (openLoc != -1) {
            int closeLoc = line.indexOf(')', openLoc + 1);
            
            if (closeLoc != -1) {
              int colonLoc = line.indexOf(':', openLoc + 1);
              if ((colonLoc > openLoc) && (colonLoc < closeLoc)) {
                // ok this line is parsable!
                String lineNumStr = line.substring(colonLoc + 1, closeLoc);
                try {
                  lineNumber = Integer.parseInt(lineNumStr);
                  fileName = line.substring(openLoc + 1, colonLoc);
                }
                catch (NumberFormatException nfe) {
                  // do nothing; we failed at parsing
                }
              }
            }
          }
          
          insertText(getLength(), line, styleName);
          
          // OK, now if fileName != null we did parse out fileName
          // and lineNumber.
          // Here's where we'd add the button, etc.
          /*
           if (fileName != null) {
           JButton button = new JButton("go");
           button.addActionListener(new ExceptionButtonListener(fileName, lineNumber));
           SimpleAttributeSet buttonSet = new SimpleAttributeSet(set);
           StyleConstants.setComponent(buttonSet, button);
           insertString(getLength(), "  ", null);
           insertString(getLength() - 1, " ", buttonSet);
           JOptionPane.showMessageDialog(null, "button in");
           insertString(getLength(), " ", null);
           JOptionPane.showMessageDialog(null, "extra space");
           }*/
          
          //JOptionPane.showMessageDialog(null, "\\n");
          insertText(getLength(), "\n", styleName);
          
        } // end the while
      }
    }
    catch (IOException ioe) { throw new UnexpectedException(ioe); }
    catch (EditDocumentException ble) { throw new UnexpectedException(ble); }
    finally { modifyUnlock(); }
  }  
}
