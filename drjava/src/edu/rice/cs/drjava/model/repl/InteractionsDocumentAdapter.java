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

package edu.rice.cs.drjava.model.repl;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import edu.rice.cs.util.Pair;

import java.awt.*;
import java.util.List;
import java.util.LinkedList;
import javax.swing.text.AbstractDocument;

/**
 * Represents the Interactions Document Adapter. Extends from the Abstract DrJava Document, 
 * which contains shared code between the interactions and definitions documents.
 */
public class InteractionsDocumentAdapter extends AbstractDJDocument {
  
  private static Color INTERACTIONS_SYSTEM_ERR_COLOR = DrJava.getConfig().getSetting(SYSTEM_ERR_COLOR);
  private static Color INTERACTIONS_SYSTEM_IN_COLOR = DrJava.getConfig().getSetting(SYSTEM_IN_COLOR);
  private static Color INTERACTIONS_SYSTEM_OUT_COLOR = DrJava.getConfig().getSetting(SYSTEM_OUT_COLOR);
  //Renamed as to avoid confusion with the one in option constants
  private static Color INTERACTIONS_STANDARD_ERROR_COLOR = DrJava.getConfig().getSetting(INTERACTIONS_ERROR_COLOR);
  private static Color INTERACTIONS_DEBUGGER_COLOR = DrJava.getConfig().getSetting(DEBUG_MESSAGE_COLOR);
  
  private static Color INTERACTIONS_OBJECT_RETURN_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_NORMAL_COLOR);
  private static Color INTERACTIONS_STRING_RETURN_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_DOUBLE_QUOTED_COLOR);
  private static Color INTERACTIONS_CHARACTER_RETURN_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_SINGLE_QUOTED_COLOR);
  private static Color INTERACTIONS_NUMBER_RETURN_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_NUMBER_COLOR);
  
  private static Font INTERACTIONS_MAIN_FONT = DrJava.getConfig().getSetting(FONT_MAIN);
  
  private boolean _toClear = false;
  
  final ColorOptionListener col = new ColorOptionListener();
  final FontOptionListener fol = new FontOptionListener();
  
  private final Object _lock = new Object();
  
  protected void throwErrorHuh() {
    //Do nothing
  }
  
  protected int startCompoundEdit() {
    //Do nothing
    return 0;
  }
  
  protected void endCompoundEdit(int key) {
    //Do nothing
  }
  
  protected void addUndoRedo(AbstractDocument.DefaultDocumentEvent chng, Runnable undoCommand, Runnable doCommand) {
    //Do nothing
  }
  protected void _styleChanged() {
    //Do nothing 
  }
  
  /**
   * Returns a new indenter.
   * Eventually to be used to return an interactions indenter
   */
  protected Indenter makeNewIndenter(int indentLevel) {
    return new Indenter(indentLevel);
  }
  
  //A list of styles and their locations
  private List<Pair<Pair<Integer,Integer>,String>> _stylesList = new LinkedList<Pair<Pair<Integer,Integer>,String>>();
  
  //Adds the given coloring style to the list
  public void addColoring(int start, int end, String style) {
    synchronized(_lock) {
      
      if(_toClear) {
        _stylesList.clear();    
        _toClear = false;
      }
      _stylesList.add(new Pair<Pair<Integer,Integer>,String>
                      (new Pair<Integer,Integer>(start,end), style));
    }
  }
  /**
   * package protected accessor method used for test cases
   */
  List<Pair<Pair<Integer, Integer>, String>> getStylesList() {
    return _stylesList;
  }
  
  /**
   * Attempts to set the coloring on the graphics based upon the content of the styles list
   * returns false if the point is not in the list.
   */
  public boolean setColoring(int point, Graphics g) {
    synchronized(_lock) {
      for(Pair<Pair<Integer,Integer>,String> p :  _stylesList) {
        Pair<Integer,Integer> loc = p.getFirst();
        if(loc.getFirst() <= point && loc.getSecond() >= point) {
          if(p.getSecond().equals(InteractionsDocument.ERROR_STYLE)) {
            //DrJava.consoleErr().println("Error Style");
            g.setColor(INTERACTIONS_STANDARD_ERROR_COLOR);   
            g.setFont(g.getFont().deriveFont(Font.BOLD));
          }
          else if(p.getSecond().equals(InteractionsDocument.DEBUGGER_STYLE)) {
            //DrJava.consoleErr().println("Debugger Style");
            g.setColor(INTERACTIONS_DEBUGGER_COLOR);
            g.setFont(g.getFont().deriveFont(Font.BOLD));
          }
          else if(p.getSecond().equals(ConsoleDocument.SYSTEM_OUT_STYLE)) {
            //DrJava.consoleErr().println("System.out Style");
            g.setColor(INTERACTIONS_SYSTEM_OUT_COLOR);
            g.setFont(INTERACTIONS_MAIN_FONT);
          }
          else if(p.getSecond().equals(ConsoleDocument.SYSTEM_IN_STYLE)) {
            //DrJava.consoleErr().println("System.in Style");
            g.setColor(INTERACTIONS_SYSTEM_IN_COLOR);
            g.setFont(INTERACTIONS_MAIN_FONT);
          }
          else if(p.getSecond().equals(ConsoleDocument.SYSTEM_ERR_STYLE)) {
            //DrJava.consoleErr().println("System.err Style");
            g.setColor(INTERACTIONS_SYSTEM_ERR_COLOR);
            g.setFont(INTERACTIONS_MAIN_FONT);
          }
          else if(p.getSecond().equals(InteractionsDocument.OBJECT_RETURN_STYLE)) {
            g.setColor(INTERACTIONS_OBJECT_RETURN_COLOR);
            g.setFont(INTERACTIONS_MAIN_FONT);
          }
          else if(p.getSecond().equals(InteractionsDocument.STRING_RETURN_STYLE)) {
            g.setColor(INTERACTIONS_STRING_RETURN_COLOR);
            g.setFont(INTERACTIONS_MAIN_FONT);
          }
          else if(p.getSecond().equals(InteractionsDocument.NUMBER_RETURN_STYLE)) {
            g.setColor(INTERACTIONS_NUMBER_RETURN_COLOR);
            g.setFont(INTERACTIONS_MAIN_FONT);
          }
          else if(p.getSecond().equals(InteractionsDocument.CHARACTER_RETURN_STYLE)) {
            g.setColor(INTERACTIONS_CHARACTER_RETURN_COLOR);
            g.setFont(INTERACTIONS_MAIN_FONT);
          }
          else { //Normal text color
            return false; 
          }
          return true;
        }
      }
      return false;
    }
  }
  
  public void setBoldFonts(int point, Graphics g) {
    synchronized(_lock) {
      for(Pair<Pair<Integer,Integer>,String> p :  _stylesList) {
        Pair<Integer,Integer> loc = p.getFirst();
        if(loc.getFirst() <= point && loc.getSecond() >= point) {
          if(p.getSecond().equals(InteractionsDocument.ERROR_STYLE)) {
            g.setFont(g.getFont().deriveFont(Font.BOLD));
          }
          else if(p.getSecond().equals(InteractionsDocument.DEBUGGER_STYLE)) {
            g.setFont(g.getFont().deriveFont(Font.BOLD));
          }
          else {
            g.setFont(INTERACTIONS_MAIN_FONT);
          }
          return;
        }
      }
    }
  }
  
  //Called when the Interactions pane is reset
  public void clearColoring() {
    //Don't clear immediately or else the colors will disappear while the interactions pane resets
    //_stylesList.clear();
    _toClear = true;
  }
  
  
  /**
   * Returns true iff the end of the current interaction is an open comment block
   * @return true iff the end of the current interaction is an open comment block
   */
  public boolean isInCommentBlock() {
    resetReducedModelLocation();
    ReducedModelState state = stateAtRelLocation(getLength()-_currentLocation);
    boolean toReturn = (state.equals(ReducedModelStates.INSIDE_BLOCK_COMMENT));
    return toReturn;
  }
  
  
  public InteractionsDocumentAdapter() {
    synchronized(_lock) {
      DrJava.getConfig().addOptionListener( OptionConstants.SYSTEM_IN_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.SYSTEM_OUT_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.SYSTEM_ERR_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.INTERACTIONS_ERROR_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.DEBUG_MESSAGE_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_DOUBLE_QUOTED_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_SINGLE_QUOTED_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_NUMBER_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_NORMAL_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.FONT_MAIN, fol); 
    }
    
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  private void updateColors() {
    INTERACTIONS_SYSTEM_ERR_COLOR = DrJava.getConfig().getSetting(SYSTEM_ERR_COLOR);
    INTERACTIONS_SYSTEM_IN_COLOR = DrJava.getConfig().getSetting(SYSTEM_IN_COLOR);
    INTERACTIONS_SYSTEM_OUT_COLOR = DrJava.getConfig().getSetting(SYSTEM_OUT_COLOR);
    INTERACTIONS_STANDARD_ERROR_COLOR = DrJava.getConfig().getSetting(INTERACTIONS_ERROR_COLOR);
    INTERACTIONS_DEBUGGER_COLOR = DrJava.getConfig().getSetting(DEBUG_MESSAGE_COLOR);
    
    INTERACTIONS_OBJECT_RETURN_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_NORMAL_COLOR);
    INTERACTIONS_STRING_RETURN_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_DOUBLE_QUOTED_COLOR);
    INTERACTIONS_CHARACTER_RETURN_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_SINGLE_QUOTED_COLOR);
    INTERACTIONS_NUMBER_RETURN_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_NUMBER_COLOR);
    
  }  
  
  /**
   * The OptionListeners for DEFINITIONS COLORs
   */
  private class ColorOptionListener implements OptionListener<Color> {
    
    public void optionChanged(OptionEvent<Color> oce) {
      updateColors();
    }
  }
  
  private class FontOptionListener implements OptionListener<Font> {
    
    public void optionChanged(OptionEvent<Font> oce) {
      INTERACTIONS_MAIN_FONT = DrJava.getConfig().getSetting(FONT_MAIN);
    }
  }
  
  
}