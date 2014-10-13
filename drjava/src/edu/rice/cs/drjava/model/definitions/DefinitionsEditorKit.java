/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions;

import edu.rice.cs.drjava.ui.AbstractDJPane;
import edu.rice.cs.util.UnexpectedException;

import javax.swing.text.*;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import edu.rice.cs.drjava.model.GlobalEventNotifier;

/** The editor kit class for editing Java source files. It functions as the controller in an MVC hierarchy.  It also
  * implements a factory for new documents and a factory for Views (the things that render the document).  May only be
  * used as the EditorKit for panes extending AbstractDJPane.  In fact, only used as the EditorKit for DefintionsPanes.
  * Stored as a field of DefinitionsPane.
  * @version $Id: DefinitionsEditorKit.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class DefinitionsEditorKit extends StyledEditorKit {
  
  public static final String DELIMITERS = "!@%^&*()-=+[]{};:'\",.<>/?";
  
  private GlobalEventNotifier _notifier;
  private Action[] _actions;
  
  /** Creates a new editor kit with the given listeners.
    * @param notifier Keeps track of the listeners to the model
    */
  public DefinitionsEditorKit(GlobalEventNotifier notifier) {
    _notifier = notifier;
    Action[] supActions = super.getActions();
    _actions = new Action[supActions.length];
    for(int i = 0; i < _actions.length; ++i) {
      Action a = supActions[i];
      Object name = a.getValue("Name");
      if (name.equals(beginWordAction))
        _actions[i] = new BeginWordAction(beginWordAction, false);
      else if (name.equals(endWordAction))
        _actions[i] = new EndWordAction(endWordAction, false);
      else if (name.equals(nextWordAction))
        _actions[i] = new NextWordAction(nextWordAction, false);
      else if (name.equals(previousWordAction))
        _actions[i] = new PreviousWordAction(previousWordAction, false);
      else if (name.equals(selectionNextWordAction))
        _actions[i] = new NextWordAction(selectionNextWordAction, true);
      else if (name.equals(selectionPreviousWordAction))
        _actions[i] = new PreviousWordAction(selectionPreviousWordAction, true);
      else if (name.equals(selectWordAction))
        _actions[i] = new SelectWordAction();
      else _actions[i] = a;
    }
  }
  
  public Action[] getActions() { return _actions; }
  
  private static ViewFactory _factory = new ViewFactory() {
    public View create(Element elem) {
      // The following line is for performance analysis only!
      // return new WrappedPlainView(elem, true);
      return new ColoringView(elem);
    }
  };
  
  /** Creates a new DefinitionsDocument.  Formerly named createDefaultDocument() because the view (DefinitionsPane)
    * would create a DefinitionsDocument by default when it was constructed.  However, this default document was  
    * immediately discarded because a DefinitionsDocument for the constructed DefinitionsPane already existed. 
    * Unfortunately, JEditorPane does not have a constructor that takes a Document as input.  We conceivably could
    * design this EditorKit to return the pre-existing document when the JEditorPane requests a new one, but the 
    * EditorKit is specified by a static field of DefinitionsPane so there is no clean way to install the proper
    * EditorKit before the JEditorPane constructor asks for the Document.
    *
    * As an easier alternative, we just let the DefaultEditorKit return a PlainDocument (much lighter weight),
    * which is thrown away when the true DefinitionsDocument is assigned
    *
    * Improvements to this approach are welcome...  :)
    */
  public DefinitionsDocument createNewDocument() { return  _createDefaultTypedDocument(); }
  
  /** Creates a new DefinitionsDocument.
    * @return a new DefinitionsDocument.
    */
  private DefinitionsDocument _createDefaultTypedDocument() { return new DefinitionsDocument(_notifier); }
  
  /** Get the MIME content type of the document
    * @return "text/java"
    */
  public String getContentType() { return "text/java"; }
  
  /** We want to use our ColoringView to render text, so here we return a factory that creates ColoringViews. */
  public final ViewFactory getViewFactory() { return _factory; }
  
  /** Brings the cursor to the beginning of the current word separated by whitespace or a delimiting character. */
  static class BeginWordAction extends TextAction {
    private boolean _select;
        
    BeginWordAction(String nm, boolean b) {
      super(nm);
      _select = b;
    }
    
    public void actionPerformed(ActionEvent e) {
      AbstractDJPane target = (AbstractDJPane) getTextComponent(e);
      if (target != null) {
        try {
          int offs = target.getCaretPosition();
          final String text = target.getDocument().getText(0, offs);
          while(offs > 0) {
            char chPrev = text.charAt(offs - 1);
            if ((DELIMITERS.indexOf(chPrev) >= 0) || (Character.isWhitespace(chPrev))) {
              break;
            }
            --offs;
            if (offs == 0) break; // otherwise offs-1 below generates an index out of bounds
            char ch = text.charAt(offs);
            chPrev = text.charAt(offs - 1);
            if ((DELIMITERS.indexOf(ch) >= 0) || (DELIMITERS.indexOf(chPrev) >= 0) 
                  || Character.isWhitespace(ch) || Character.isWhitespace(chPrev)) {
              break;
            }
          }
          if (_select) {
            target.moveCaretPosition(offs);
          } else {
            target.setCaretPosition(offs);
          }
        }
        catch(BadLocationException ble) { throw new UnexpectedException(ble); }
      }
    }
  }
  
  /** Sets the cursor at the end of the current word separated by whitespace or a delimiting character. */
  static class EndWordAction extends TextAction {
    private boolean _select;
    
    EndWordAction(String nm, boolean b) {
      super(nm);
      _select = b;
    }
    
    public void actionPerformed(ActionEvent e) {
      AbstractDJPane target = (AbstractDJPane) getTextComponent(e);
      if (target != null) {
        try {
          int offs = target.getCaretPosition();
          final int iOffs = offs;
          final String text = target.getDocument().getText(iOffs,target.getDocument().getLength()-iOffs);
          while((offs-iOffs)<text.length()-1) {
            ++offs;
            char ch = text.charAt(offs-iOffs);
            if ((DELIMITERS.indexOf(ch) >= 0) || Character.isWhitespace(ch)) {
              break;
            }
          }
          if (_select) {
            target.moveCaretPosition(offs);
          } else {
            target.setCaretPosition(offs);
          }
        }
        catch(BadLocationException ble) { throw new UnexpectedException(ble); }
      }
    }
  }
  
  /** Moves the cursor to the beginning of the previous word. If the cursor is currently inside of a word, moves it to
    * the beginning of that word.  Otherwise, moves the cursor to the beginning of the previous word.
    * Also stops at delimiting characters and at the end of a line
    */
  static class PreviousWordAction extends TextAction {
    private boolean _select;
    
    PreviousWordAction(String nm, boolean b) {
      super(nm);
      _select = b;
    }
   
    public void actionPerformed(ActionEvent e) {
      AbstractDJPane target = (AbstractDJPane) getTextComponent(e);
      if (target != null) {
        try {
          int offs = target.getCaretPosition();
          final String text = target.getDocument().getText(0,offs);
          while(offs > 0) {
            --offs;
            if (offs == 0) break;
            char ch = text.charAt(offs);
            char chPrev = text.charAt(offs - 1);
            if (Character.isWhitespace(ch) && Character.isWhitespace(chPrev) && ch!='\n') continue;
            else if (DELIMITERS.indexOf(ch) >= 0 || DELIMITERS.indexOf(chPrev) >= 0 || 
                     (offs >= 2 && Character.isWhitespace(chPrev) && !Character.isWhitespace(text.charAt(offs-2)) && ch!='\n'))
              break;
            else if (Character.isWhitespace(chPrev) && !Character.isWhitespace(ch)) break;
            else if (!Character.isWhitespace(chPrev) && ch == '\n') break;
            else if (Character.isWhitespace(chPrev) && ch=='\n') {  // compensate for space at the end of a line
              while(Character.isWhitespace(chPrev) && (offs > 0)) {
                --offs;
                ch = text.charAt(offs);
                chPrev = text.charAt(offs - 1);
              }            
              break;
            }
          }
          if (_select) target.moveCaretPosition(offs);
          else target.setCaretPosition(offs);
        }
        catch(BadLocationException ble) { throw new UnexpectedException(ble); }
      }
    }
  }
  
  /** Moves the cursor from the current word to the beginning of the next word, stopping at delimiting characters and
    * at the end of a line
    */ 
  static class NextWordAction extends TextAction {
    private boolean _select;
    
    NextWordAction(String nm, boolean b) {
      super(nm);
      _select = b;
    }
    
    public void actionPerformed(ActionEvent e) {
      AbstractDJPane target = (AbstractDJPane) getTextComponent(e);
      if (target != null) {
        try {
          int offs = target.getCaretPosition();
          final int iOffs = offs;
          final String text = target.getDocument().getText(iOffs,target.getDocument().getLength() - iOffs);
          final int len = text.length();
          while((offs-iOffs) < len) {
            ++offs;
            if (offs-iOffs == len)
              break;
            char ch = text.charAt(offs-iOffs);
            char chPrev = text.charAt(offs-iOffs - 1);
            if ((DELIMITERS.indexOf(ch) >= 0) ||
                (DELIMITERS.indexOf(chPrev) >= 0) ||
                Character.isWhitespace(chPrev) ||
                ch == '\n') {
              while((offs-iOffs<len) && Character.isWhitespace(ch) && ch != '\n') {
                if (DELIMITERS.indexOf(chPrev) >= 0)
                  break;
                ++offs;
                ch = text.charAt(offs-iOffs);
              }
              if (ch == '\n' && Character.isWhitespace(text.charAt(offs - iOffs - 1))) continue;
              else break;
            }
            //used to fix incorrect behavior when a space is at the end of a line
            if (!Character.isWhitespace(chPrev) && Character.isWhitespace(ch)) {
              int offs0 = offs;
              while((offs-iOffs)<(len-1) && ch!='\n' && Character.isWhitespace(ch)) {
                ++offs; 
                ch = text.charAt(offs-iOffs);
                chPrev = text.charAt(offs-iOffs - 1);               
              }
              offs = offs0;
              if (ch=='\n') break;
              else continue;
            }   
          }
          if (_select) target.moveCaretPosition(offs);
          else target.setCaretPosition(offs);
        }
        catch(BadLocationException ble) { throw new UnexpectedException(ble); }
      }
    }
  }
  
  /** Defines the action for word selection as in when double-clicking a word. */
  static class SelectWordAction extends TextAction {
    private Action start;
    private Action end;
    
    public SelectWordAction() {
      super(selectWordAction);
      start = new BeginWordAction("pigdog", false);
      end = new EndWordAction("pigdog", true);
    }
    public void actionPerformed(ActionEvent e) {
      start.actionPerformed(e);
      end.actionPerformed(e);
    }
  }
}
