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

import edu.rice.cs.drjava.*;
import edu.rice.cs.drjava.config.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;

/** Contains Hashtables that are used in the key-binding process along with methods to build them and access their 
  * contents. Performs the assigning of keys to actions, checking for and resolving conflicts, and setting appropriate 
  * menu accelerators.
  * TODO: fix the design of our key binding scheme. We should be able to bind multiple keys to the same action! 
  * @version $Id$
  */
public class KeyBindingManager {
  
  public static final KeyBindingManager ONLY = new KeyBindingManager();

  private KeyBindingManager() {   }
  
  // Key-binding configuration tables
  private Hashtable<KeyStroke, KeyStrokeData> _keyToDataMap = new Hashtable<KeyStroke, KeyStrokeData>();
  private Hashtable<Action, KeyStrokeData> _actionToDataMap = new Hashtable<Action, KeyStrokeData>();

  private MainFrame _mainFrame = null;

  // Needed to get the DefaultEditorKit actions from their names
  private ActionMap _actionMap;

  /** Should only check conflicts when the keyboard configuration options are first entered into the maps. Afterwards, 
    * the GUI configuration will warn the user about actions whose key-bindings will be overwritten in the GetKeyDialog,
    * and the preferences panel will reflect the changes. When the user hit apply, no conflicts should exist in the 
    * preferences panel, and there should be no need to check for conflicts in the configuration.
    */
  private boolean _shouldCheckConflict = true;

  public void setMainFrame (MainFrame mainFrame) { _mainFrame = mainFrame; }

  /** Sets the ActionMap
    * @param actionMap the ActionMap to set to
    */
  public void setActionMap (ActionMap actionMap) { _actionMap = actionMap; }

  public void setShouldCheckConflict (boolean bool) { _shouldCheckConflict = bool;  }
  
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

  /** Takes a KeyStroke and gets its Action from the keyToActionMap
    * @param ks KeyStroke to look up
    * @return the corresponding Action or null if there is no Action associated with the KeyStroke
    */
  public Action get(KeyStroke ks) {
    KeyStrokeData ksd = _keyToDataMap.get(ks);
    if (ksd == null) return null;
    return ksd.getAction();
  }

  public String getName(KeyStroke ks) {
    KeyStrokeData ksd = _keyToDataMap.get(ks);
    if (ksd == null) return null;
    return ksd.getName();
  }

  public String getName(Action a) {
    KeyStrokeData ksd = _actionToDataMap.get(a);
    if (ksd == null) return null;
    return ksd.getName();
  }

// public void addListener(Option<KeyStroke> opt, JMenuItem jmi) {
//    KeyStroke ks = DrJava.getConfig().getSetting(opt);
//    Action a = _keyToActionMap.get(ks);
//    DrJava.getConfig().addOptionListener(opt, new KeyStrokeOptionListener(jmi, a, ks));
//  }

  /** Assigns the selection action with the given name to the combination of the shift key and the given key stroke
    * option.  Also adds new KeyStrokOptionListeners to the non-shifted Actions
    * @param opt the KeyStroke Option of the Action
    * @param shiftS the name of the Selection Action
    */
  public void addShiftAction(Option<KeyStroke> opt, String shiftS) {
    Action shiftA = _actionMap.get(shiftS);
    addShiftAction(opt, shiftA);
  }

  /** Assigns the given selection action to the combination of the shift key and the given key stroke option.
    * Also adds new KeyStrokOptionListeners to the non-shifted Actions
    * @param opt the KeyStroke Option of the Action
    * @param shiftA the Selection Action
    */
  public void addShiftAction(Option<KeyStroke> opt, Action shiftA) {
    KeyStroke ks = DrJava.getConfig().getSetting(opt);

    KeyStrokeData normal = _keyToDataMap.get(ks);
    normal.setShiftAction(shiftA);

    KeyStrokeData ksd = new KeyStrokeData(addShiftModifier(ks), shiftA, null, "Selection " + normal.getName(), null);

    _keyToDataMap.put(addShiftModifier(ks), ksd);
    _actionToDataMap.put(shiftA, ksd);
  }

  /** Takes a KeyStroke and returns a KeyStroke that is the same that has the shift modifier
    * @param k a KeyStroke
    * @return the same KeyStorke with the shift modifier
    */
  public KeyStroke addShiftModifier(KeyStroke k) {
    return KeyStroke.getKeyStroke(k.getKeyCode(), k.getModifiers() | InputEvent.SHIFT_MASK, k.isOnKeyRelease() );
  }

  /** Inserts a KeyStroke/Action pair into the _keyToActionMap. Checks for conflicts and displays an option pane if 
    * they are any.
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
      //KeyStrokeData data = _actionToDataMap.get(a);
      //data.setKeyStroke(ks);
      //_keyToDataMap.put(ks,data);

      return true;
    }
    else if (_keyToDataMap.get(ks).getAction().equals(a)) {
      // this KeyStroke/Action pair is already in the Hashtable
      return false;
    }
    else { // key-binding conflict
      if (_shouldCheckConflict) {
        KeyStrokeOption opt = new KeyStrokeOption(null,null);
        KeyStrokeData conflictKSD = _keyToDataMap.get(ks);
        String key = opt.format(ks);
        KeyStrokeData newKSD = _actionToDataMap.get(a);
        String text = "\""+ key +"\"" + " is already assigned to \"" + conflictKSD.getName() +
          "\".\nWould you like to assign \"" + key + "\" to \"" + newKSD.getName() + "\"?";
        int rc = JOptionPane.showConfirmDialog(_mainFrame, text, "DrJava", JOptionPane.YES_NO_CANCEL_OPTION);

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
      else return true;
    }
  }

  /** A listener that can be attached to KeyStrokeOptions that automatically updates the Hashtables in 
    * KeyBindingManager, the corresponding selection Action bindings, and the menu accelerators
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
      // If jmi is null, this keystroke maps to an action that isn't in the menu
      if (jmi != null) {
        KeyStroke ks = data.getKeyStroke();
        if (ks != KeyStrokeOption.NULL_KEYSTROKE) {
          // If ks is NULL_KEYSTROKE, we don't want it "active", since some
          //  Windows keys generate NULL_KEYSTROKE
          jmi.setAccelerator(ks);
        }
        else {
          // Clear the menu item's accelerator
          jmi.setAccelerator(null);
        }
      }
    }

    public void optionChanged(OptionEvent<KeyStroke> oce) {
      if (shouldUpdate(oce.value, _a)) {
        KeyStrokeData data = _actionToDataMap.get(_a);
        if (data == null) {
          // Nothing to change
          return;
        }

        // Only remove the old keystroke from the map if it is currently mapped to our data.  If not, our old
        //  keystroke has already been redefined and should not be removed!
        if (data.equals(_keyToDataMap.get(_ks))) {
          _keyToDataMap.remove(_ks);
        }

        //check for conflicting key binding
        if (_keyToDataMap.containsKey(oce.value) && _shouldCheckConflict) {
          //if new key in map, and shouldUpdate returns true, we are overwriting it
          KeyStrokeData conflictKSD = _keyToDataMap.get(oce.value);
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
        Action shiftAction = data.getShiftAction();
        if (shiftAction != null) {
          //_keyToActionMap.remove(addShiftModifier(_ks));
          KeyStrokeData shiftKSD = _actionToDataMap.get(shiftAction);
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

  public static class KeyStrokeData {
    private KeyStroke _ks;
    private Action _a;
    private JMenuItem _jmi;
    private String _name;
    private Option<KeyStroke> _kso;
    private Action _shiftA;

    public KeyStrokeData(KeyStroke ks, Action a, JMenuItem jmi, String name, Option<KeyStroke> kso) {
      _ks = ks;
      _a = a;
      _jmi = jmi;
      _name = name;
      _kso = kso;
      _shiftA = null;
    }

    public KeyStroke getKeyStroke() { return _ks; }
    public Action getAction() { return _a; }
    public JMenuItem getJMenuItem() { return _jmi; }
    public String getName() { return _name; }
    public Option<KeyStroke> getOption() { return _kso; }
    public Action getShiftAction() { return _shiftA; }
    
    public void setKeyStroke(KeyStroke ks) { _ks = ks; }
    public void setAction(Action a) { _a = a; }
    public void setJMenuItem(JMenuItem jmi) { _jmi = jmi; }
    public void setName(String name) { _name = name; }
    public void setOption(Option<KeyStroke> kso) { _kso = kso; }
    public void setShiftAction(Action shiftA) { _shiftA = shiftA; }
  }
}
