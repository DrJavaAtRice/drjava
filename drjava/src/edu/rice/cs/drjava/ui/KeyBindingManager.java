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

import edu.rice.cs.drjava.*;
import edu.rice.cs.drjava.config.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;

/**
 * Contains Hashtables that are used in the key-binding process along with 
 * methods to build them and access their contents. Performs the 
 * assigning of keys to actions, checking for and resolving conflicts, and
 * setting appropriate menu accelerators
 * @version $Id$
 */
public class KeyBindingManager {
  
  // Key-binding configuration tables
  
  // Maps KeyStrokes to their Actions
  private Hashtable _keyToActionMap = new Hashtable(); 
  // Maps KeyStrokes to their JMenuItems
  private Hashtable _keyToMenuItemMap = new Hashtable();
  // Maps Actions to their Names (Strings)
  private Hashtable _actionToNameMap = new Hashtable();
  // Maps Actions to their Selection Actions
  private Hashtable _actionToShiftActionMap = new Hashtable();
  
  private MainFrame _mainFrame;
  
  // Needed to get the DefaultEditorKit actions from their names
  private ActionMap _actionMap;
  
  public KeyBindingManager(MainFrame mainFrame) {
    _mainFrame = mainFrame;
  }
    
  /**
   * Sets the ActionMap
   * @param am the ActionMap to set to
   */
  public void setActionMap (ActionMap am) {      
    _actionMap = _mainFrame.getCurrentDefPane().getActionMap();
  }
  
  /**
   * Takes a KeyStroke and gets its Action from the keyToActionMap
   * @param ks KeyStroke to look up
   * @return the corresponding Action or null if there is no Action associated 
   * with the KeyStroke
   */
  public Action get(KeyStroke ks) {
    return (Action)_keyToActionMap.get(ks);
  }
  
  /**
   * Puts an Action/String pair into the actionToNameMap
   * @param a an Action
   * @param name its name
   */
  public void putActionToNameMap (Action a, String name) {
    _actionToNameMap.put(a, name);
  } 
  
  /**
   * Puts an KeyStroke/JMenuItem pair into the actionToNameMap
   * @param ks a KeyStroke
   * @param tmpItem its JMenuitem
   */
  public void putKeyToMenuItemMap (KeyStroke ks, JMenuItem tmpItem) {
    _keyToMenuItemMap.put(ks, tmpItem);
  } 
  
  public void addListener(Option<KeyStroke> opt, JMenuItem jmi) {
    KeyStroke ks = DrJava.CONFIG.getSetting(opt);
    Action a = (Action)_keyToActionMap.get(ks);
    DrJava.CONFIG.addOptionListener(opt, new KeyStrokeOptionListener(jmi, a, ks));                                    
  }
  
  /**
   * Takes an option, its name, and the name of the corresponding
   * selection action and returns the selection action after putting
   * the appropriate data pairs into their respective Hashtables
   * Also adds new KeyStrokOptionListeners to the non-shifted Actions
   * @param opt the KeyStroke Option of the Action
   * @param s the name of the Action
   * @param shiftS the name of the Selection Action
   */
  public void addShiftAction(Option<KeyStroke> opt, String s, String shiftS) {
    KeyStroke ks = DrJava.CONFIG.getSetting(opt);
    Action a = _actionMap.get(s);
    Action shiftA = _actionMap.get(shiftS);
    
    mapInsert(ks, a);
    mapInsert(addShiftModifier(ks), shiftA);
    
    addListener(opt, null);
    _actionToShiftActionMap.put(a, shiftA);
  }
  
  /**
   * Takes a KeyStroke and returns a KeyStroke that is the same that
   * has the shift modifier
   * @param k a KeyStroke
   * @returns the same KeyStorke with the shift modifier
   */
  public KeyStroke addShiftModifier(KeyStroke k) {
    return KeyStroke.getKeyStroke(k.getKeyCode(),
                                  k.getModifiers() | InputEvent.SHIFT_MASK,
                                  k.isOnKeyRelease() );
  }
  
  /**
   * Inserts a KeyStroke/Action pair into the _keyToActionMap. Checks for 
   * conflicts and displays an option pane if they are any.
   * @param ks the KeyStroke
   * @param a the Action
   * @return whether a map insertion was done
   */
  public boolean mapInsert(KeyStroke ks, Action a) {
    if (CodeStatus.DEVELOPMENT) {
      if (ks == KeyStrokeOption.NULL_KEYSTROKE) 
        // then there should be no keystroke for this action
        return false;
      if (!_keyToActionMap.containsKey(ks) ) { 
        // the key is not in the Hashtable, put it in
        _keyToActionMap.put(ks, a);
        return true;
      } 
      else if (_keyToActionMap.get(ks).equals(a)) { 
        // this KeyStroke/Action pair is already in the Hashtable
        return false;
      }
      else { // key-binding conflict
        KeyStrokeOption opt = new KeyStrokeOption(null,null);
        String key = opt.format(ks);
        Action oldA = (Action) _keyToActionMap.get(ks);
        String text = key + " is already assigned to " + _actionToNameMap.get(oldA) + 
          ". Would you like to assign " + key + " to " + _actionToNameMap.get(a) + "?";
        int rc = JOptionPane.showConfirmDialog(_mainFrame,
                                               text,
                                               "DrJava",
                                               JOptionPane.YES_NO_CANCEL_OPTION);
        
        switch (rc) {
          case JOptionPane.YES_OPTION:
            _keyToActionMap.remove(ks);
            _keyToActionMap.put(ks,a);
            return true;
          case JOptionPane.NO_OPTION:
            return false;
          case JOptionPane.CLOSED_OPTION:
            return false;
          case JOptionPane.CANCEL_OPTION:
            return false;
          default:
            throw new RuntimeException("Invalid rc: " + rc);
        }
      }
    }
    else
      return false;
  }
  
  /**
   * A listener that can be attached to KeyStrokeOptions that automatically
   * updates the Hashtables in KeyBindingManager,the corresponding selection
   * Action bindings, and the menu accelerators
   */
  public class KeyStrokeOptionListener implements OptionListener<KeyStroke> {
    protected JMenuItem _jmi; // the JMenuItem associated with this option
    protected Action _a; // the Action associated with this option
    protected KeyStroke _ks; // the old KeyStroke value
    
    public KeyStrokeOptionListener(JMenuItem jmi, Action a, KeyStroke ks) {
      _jmi = jmi;
      _a = a;
      _ks = ks;
    }
    
    public KeyStrokeOptionListener(Action a, KeyStroke ks) {
      _jmi = null;
      _a = a;
      _ks = ks;
    }
    
    public void optionChanged(OptionEvent<KeyStroke> oce) {
      if (CodeStatus.DEVELOPMENT) {
        if(mapInsert(oce.value, _a)) // if overwrite, remove accelerator of overwritten menuitem
        {
          if (_jmi != null) { // otherwise this keystroke should map to an action that isn't in the menu
            JMenuItem overwrittenMenuItem = (JMenuItem) _keyToMenuItemMap.get(oce.value);
            if (overwrittenMenuItem != null) {
              overwrittenMenuItem.setAccelerator(null);
            }
            _jmi.setAccelerator(oce.value);
          }
          // change shift-version's binding
          Action shiftAction = (Action) _actionToShiftActionMap.get(_a);
          if (shiftAction != null) {
            _keyToActionMap.remove(addShiftModifier(_ks));
            mapInsert(addShiftModifier(oce.value), shiftAction);
          }
          
          _keyToActionMap.remove(_ks);
          _ks = oce.value;
        }
      }
    }
  }
}