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
  
  public static final KeyBindingManager Singleton = new KeyBindingManager();
  
  private KeyBindingManager() {}
  
  // Key-binding configuration tables
  /**
   * TODO: should these be synchronized?
   */
  private Hashtable<KeyStroke, KeyStrokeData> _keyToDataMap =
    new Hashtable<KeyStroke, KeyStrokeData>();
  private Hashtable<Action, KeyStrokeData> _actionToDataMap =
    new Hashtable<Action, KeyStrokeData>();
  
  private MainFrame _mainFrame = null;
  
  // Needed to get the DefaultEditorKit actions from their names
  private ActionMap _actionMap;
  
  /**
   * Should only check conflicts when the keyboard configuration options
   * are first entered into the maps. Afterwards, the GUI configuration
   * will warn the user about actions whose key-bindings will be overwritten
   * in the GetKeyDialog, and the preferences panel will reflect the changes.
   * When the user hit apply, no conflicts should exist in the preferences panel,
   * and there should be no need to check for conflicts in the configuration.
   */
  private boolean _shouldCheckConflict = true;
   
  public void setMainFrame (MainFrame mainFrame) {
    _mainFrame = mainFrame;
  }
  
  /**
   * Sets the ActionMap
   * @param am the ActionMap to set to
   */
  public void setActionMap (ActionMap actionMap) {
    _actionMap = actionMap;
  }
  
  public void setShouldCheckConflict (boolean bool) {
    _shouldCheckConflict = bool;
  }
  public Enumeration getKeyStrokeData() { return _actionToDataMap.elements(); }
  
  public void put(Option<KeyStroke> kso, Action a, JMenuItem jmi, String name)  {
    KeyStroke ks = DrJava.getConfig().getSetting(kso);
    KeyStrokeData ksd = new KeyStrokeData(ks, a, jmi, name, kso);
    _keyToDataMap.put(ks, ksd);
    _actionToDataMap.put(a, ksd);
    
    // check for shift-actions
    if (kso != null) {
      DrJava.getConfig().addOptionListener(kso, new KeyStrokeOptionListener(jmi, a, ks));
    }
  }
  
  /**
   * Takes a KeyStroke and gets its Action from the keyToActionMap
   * @param ks KeyStroke to look up
   * @return the corresponding Action or null if there is no Action associated
   * with the KeyStroke
   */
  public Action get(KeyStroke ks) {
    KeyStrokeData ksd = (KeyStrokeData) _keyToDataMap.get(ks);
    if (ksd == null) {
      return null;
    }
    return ksd.getAction();
  }
  
  public String getName(KeyStroke ks) {
    KeyStrokeData ksd = (KeyStrokeData)_keyToDataMap.get(ks);
    if (ksd == null)
      return null;
    return ksd.getName();
  }
  
  public String getName(Action a) {
    KeyStrokeData ksd = (KeyStrokeData)_actionToDataMap.get(a);
    if (ksd == null)
      return null;
    return ksd.getName();
  }
/*
 public void addListener(Option<KeyStroke> opt, JMenuItem jmi) {
    KeyStroke ks = DrJava.getConfig().getSetting(opt);
    Action a = (Action)_keyToActionMap.get(ks);
    DrJava.getConfig().addOptionListener(opt, new KeyStrokeOptionListener(jmi, a, ks));
  }
*/
  /**
   * Takes an option, its name, and the name of the corresponding
   * selection action and returns the selection action after putting
   * the appropriate data pairs into their respective Hashtables
   * Also adds new KeyStrokOptionListeners to the non-shifted Actions
   * @param opt the KeyStroke Option of the Action
   * @param s the name of the Action
   * @param shiftS the name of the Selection Action
   */
  public void addShiftAction(Option<KeyStroke> opt, String shiftS) {
    KeyStroke ks = DrJava.getConfig().getSetting(opt);

    KeyStrokeData normal = (KeyStrokeData) _keyToDataMap.get(ks);
    Action shiftA = _actionMap.get(shiftS);
    normal.setShiftAction(shiftA);

    KeyStrokeData ksd = new KeyStrokeData(addShiftModifier(ks), shiftA, null,
                                          "Selection " + normal.getName(), null);
    
    _keyToDataMap.put(addShiftModifier(ks), ksd);
    _actionToDataMap.put(shiftA, ksd);
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
  //precondition ks != KeyStrokeOption.NULL_KEYSTROKE
  private boolean shouldUpdate(KeyStroke ks, Action a) {
    if (ks == KeyStrokeOption.NULL_KEYSTROKE) {
      // then there should be no keystroke for this action
      return true;
    }
      
    if (!_keyToDataMap.containsKey(ks) ) {
      // the key is not in the Hashtable, put it in
      //_keyToActionMap.put(ks, a);
      //need to update map
      //KeyStrokeData data = (KeyStrokeData)_actionToDataMap.get(a);
      //data.setKeyStroke(ks);
      //_keyToDataMap.put(ks,data);
      
      return true;
    }
    else if (((KeyStrokeData)_keyToDataMap.get(ks)).getAction().equals(a)) {
      // this KeyStroke/Action pair is already in the Hashtable
      return false;
    }
    else { // key-binding conflict
      if (_shouldCheckConflict) {
        KeyStrokeOption opt = new KeyStrokeOption(null,null);
        KeyStrokeData conflictKSD = (KeyStrokeData)_keyToDataMap.get(ks);
        String key = opt.format(ks);
        KeyStrokeData newKSD = (KeyStrokeData)_actionToDataMap.get(a);
        String text = "\""+ key +"\"" + " is already assigned to \"" + conflictKSD.getName() +
          "\".\nWould you like to assign \"" + key + "\" to \"" + newKSD.getName() + "\"?";
        int rc = JOptionPane.showConfirmDialog(_mainFrame,
                                               text,
                                               "DrJava",
                                               JOptionPane.YES_NO_CANCEL_OPTION);
        
        switch (rc) {
          case JOptionPane.YES_OPTION:
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
      else {
        return true;
      }
    }
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
    
    private void _updateMenuItem (KeyStrokeData data) {
      JMenuItem jmi = data.getJMenuItem();
      
      //Check associated Menu Item
      if (jmi != null) { // otherwise this keystroke should map to an action that isn't in the menu
        KeyStroke ks = data.getKeyStroke();
        jmi.setAccelerator(ks);
      }
    }
    
    public void optionChanged(OptionEvent<KeyStroke> oce) {
      if(shouldUpdate(oce.value, _a))
      {
        KeyStrokeData data = (KeyStrokeData)_actionToDataMap.get(_a);
        _keyToDataMap.remove(_ks);
        
        //check for conflicting key binding
        if (_keyToDataMap.containsKey(oce.value) && _shouldCheckConflict) {
          //if new key in map, and shouldUpdate returns true, we are overwriting it
          KeyStrokeData conflictKSD = (KeyStrokeData)_keyToDataMap.get(oce.value);
          conflictKSD.setKeyStroke(KeyStrokeOption.NULL_KEYSTROKE);
          _updateMenuItem(conflictKSD);
          _keyToDataMap.remove(oce.value);
          DrJava.getConfig().setSetting(conflictKSD.getOption(), KeyStrokeOption.NULL_KEYSTROKE);
        }
        
        if (oce.value != KeyStrokeOption.NULL_KEYSTROKE) {
          _keyToDataMap.put(oce.value,data);
        }
        data.setKeyStroke(oce.value);
        _updateMenuItem(data);
        
        //Check associated shift-version's binding
        Action shiftAction = (Action) data.getShiftAction();
        if (shiftAction != null) {
          //_keyToActionMap.remove(addShiftModifier(_ks));
          KeyStrokeData shiftKSD = (KeyStrokeData) _actionToDataMap.get(shiftAction);
          _keyToDataMap.remove(shiftKSD.getKeyStroke());
          shiftKSD.setKeyStroke(addShiftModifier(oce.value));
          _keyToDataMap.put(shiftKSD.getKeyStroke(), shiftKSD);
          //mapInsert(addShiftModifier(oce.value), shiftAction);
        }
          
        _ks = oce.value;
      }
      else if (_ks != oce.value) {
        DrJava.getConfig().setSetting(oce.option, _ks);
      }
    }
  }
  
  public class KeyStrokeData {
    private KeyStroke _ks;
    private Action _a;
    private JMenuItem _jmi;
    private String _name;
    private Option<KeyStroke> _kso;
    private Action _shiftA;
    
    public KeyStrokeData(KeyStroke ks, Action a, JMenuItem jmi, String name,
                         Option<KeyStroke> kso) {
      _ks = ks;
      _a = a;
      _jmi = jmi;
      _name = name;
      _kso = kso;
      _shiftA = null;
    }
    
    public KeyStroke getKeyStroke() {
      return _ks;
    }
    
    public Action getAction() {
      return _a;
    }
    
    public JMenuItem getJMenuItem() {
      return _jmi;
    }
    
    public String getName() {
      return _name;
    }
    
    public Option<KeyStroke> getOption() {
      return _kso;
    }
    
    public Action getShiftAction() {
      return _shiftA;
    }
    
    public void setKeyStroke(KeyStroke ks) {
      _ks = ks;
    }
    
    public void setAction(Action a) {
      _a = a;
    }
    
    public void setJMenuItem(JMenuItem jmi) {
      _jmi = jmi;
    }
    
    public void setName(String name) {
      _name = name;
    }
    
    public void setOption(Option<KeyStroke> kso) {
      _kso = kso;
    }
    
    public void setShiftAction(Action shiftA) {
      _shiftA = shiftA;
    }
  }
}
