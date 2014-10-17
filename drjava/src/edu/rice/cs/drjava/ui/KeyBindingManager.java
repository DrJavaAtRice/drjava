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

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.*;
import edu.rice.cs.drjava.config.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;

/** Contains Hashtables that are used in the key-binding process along with methods to build them and access their 
  * contents. Performs the assigning of keys to actions, checking for and resolving conflicts, and setting appropriate 
  * menu accelerators.
  * @version $Id: KeyBindingManager.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class KeyBindingManager {
  
  public static final KeyBindingManager ONLY = new KeyBindingManager();

  private KeyBindingManager() {   }
  
  // Key-binding configuration tables
  private HashMap<KeyStroke, KeyStrokeData> _keyToDataMap = new HashMap<KeyStroke, KeyStrokeData>();
  private HashMap<Action, KeyStrokeData> _actionToDataMap = new HashMap<Action, KeyStrokeData>();

  private MainFrame _mainFrame = null;

  /** Should only check conflicts when the keyboard configuration options are first entered into the maps. Afterwards, 
    * the GUI configuration will warn the user about actions whose key-bindings will be overwritten in the GetKeyDialog,
    * and the preferences panel will reflect the changes. When the user hit apply, no conflicts should exist in the 
    * preferences panel, and there should be no need to check for conflicts in the configuration.
    */
  private boolean _shouldCheckConflict = true;

  public void setMainFrame (MainFrame mainFrame) { _mainFrame = mainFrame; }

  public void setShouldCheckConflict (boolean bool) { _shouldCheckConflict = bool;  }
  
  public Collection<KeyStrokeData> getKeyStrokeData() { return _actionToDataMap.values(); }

  public void put(VectorOption<KeyStroke> vkso, Action a, JMenuItem jmi, String name)  {
    Vector<KeyStroke> keys = DrJava.getConfig().getSetting(vkso);
    Vector<KeyStroke> retained = new Vector<KeyStroke>();
    KeyStrokeData ksd = new KeyStrokeData(keys, a, jmi, name, vkso);
    _actionToDataMap.put(a, ksd);
    for(KeyStroke ks: keys) {
      if (shouldUpdate(ks, a)) {
        retained.add(ks);
        _keyToDataMap.put(ks, ksd);
      }
    }
    DrJava.getConfig().addOptionListener(vkso, new VectorKeyStrokeOptionListener(jmi, a, retained));
    if (retained.size() != keys.size()) {
      // not all keys were added
      DrJava.getConfig().setSetting(vkso,retained);
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
  
  /** Inserts a KeyStroke/Action pair into the _keyToActionMap. Checks for conflicts and displays an option pane if 
    * they are any.
    * @param ks the KeyStroke
    * @param a the Action
    * @return whether a map insertion was done
    */
  private boolean shouldUpdate(KeyStroke ks, Action a) {
    if (ks == KeyStrokeOption.NULL_KEYSTROKE) {
      // filter out NULL_KEYSTROKE
      return false;
    }

    if (!_keyToDataMap.containsKey(ks) ) {
      // the key is not assigned to any action yet, put it in 
      return true;
    }
    else if (_keyToDataMap.get(ks).getAction().equals(a)) {
      // this key is already assigned to the same action; no action necessary, but updating doesn't hurt either
      // and a gratuitous update simplifies dealing with multiple menu bars
      return true;
    }
    else { // key-binding conflict
      if (_shouldCheckConflict) {
        KeyStrokeOption opt = new KeyStrokeOption(null,null);
        KeyStrokeData conflictKSD = _keyToDataMap.get(ks);
        String key = opt.format(ks);
        KeyStrokeData newKSD = _actionToDataMap.get(a);
        String text = "\"" +  key  + "\"" + " is already assigned to \"" + conflictKSD.getName() +
          "\".\nWould you like to assign \"" + key + "\" to \"" + newKSD.getName() + "\"?";
        int rc = JOptionPane.showConfirmDialog(_mainFrame, text, "DrScala", JOptionPane.YES_NO_OPTION);

        switch (rc) {
          case JOptionPane.YES_OPTION:
            removeExistingKeyStroke(ks);
            return true;
          case JOptionPane.NO_OPTION:
          case JOptionPane.CLOSED_OPTION:
          case JOptionPane.CANCEL_OPTION:
            return false;
          default:
            throw new RuntimeException("Invalid rc: " + rc);
        }
      }
      else return true;
    }
  }
  
  private void removeExistingKeyStroke(KeyStroke ks) {
    // check for conflicting key binding
    if (_keyToDataMap.containsKey(ks) && _shouldCheckConflict) {
      // if new key in map, and shouldUpdate returns true, we are overwriting it
      KeyStrokeData conflictKSD = _keyToDataMap.get(ks);
      // remove ks from the conflicting keystroke data
      Set<KeyStroke> conflictKeys = new LinkedHashSet<KeyStroke>(conflictKSD.getKeyStrokes());
      conflictKeys.remove(ks);
      conflictKSD.setKeyStrokes(new Vector<KeyStroke>(conflictKeys));
      updateMenuItem(conflictKSD);
      _keyToDataMap.remove(ks);
      DrJava.getConfig().setSetting(conflictKSD.getOption(), conflictKSD.getKeyStrokes());
    }
  }
  
  private void updateMenuItem(KeyStrokeData data) {
    JMenuItem jmi = data.getJMenuItem();
    
    // Check associated Menu Item. If jmi is null, this keystroke maps to an action that isn't in the menu
    if (jmi != null) {
      Vector<KeyStroke> keys = data.getKeyStrokes();
      if (keys.size() > 0) {
        // Since we can have multiple keys mapped to the same action, we use the first key as menu item accelerator
        jmi.setAccelerator(keys.get(0));
      }
      else {
        // Clear the menu item's accelerator
        jmi.setAccelerator(null);
      }
    }
  }
  
  /** A listener that can be attached to VectorKeyStrokeOptions that automatically updates the Hashtables in 
    * KeyBindingManager, the corresponding selection Action bindings, and the menu accelerators.
    */
  public class VectorKeyStrokeOptionListener implements OptionListener<Vector<KeyStroke>> {
    protected JMenuItem _jmi; // the JMenuItem associated with this option
    protected Action _a; // the Action associated with this option
    protected Set<KeyStroke> _oldKeys; // the old KeyStroke value

    public VectorKeyStrokeOptionListener(JMenuItem jmi, Action a, Vector<KeyStroke> keys) {
      _jmi = jmi;
      _a = a;
      _oldKeys = new LinkedHashSet<KeyStroke>(keys);
    }

    public VectorKeyStrokeOptionListener(Action a, Vector<KeyStroke> keys) {
      this(null, a, keys);
    }

    public void optionChanged(OptionEvent<Vector<KeyStroke>> oce) {
      Set<KeyStroke> newKeys = new LinkedHashSet<KeyStroke>(oce.value);
      Set<KeyStroke> removed = new LinkedHashSet<KeyStroke>(_oldKeys);
      removed.removeAll(newKeys); // the keys that were removed
      Set<KeyStroke> added = new LinkedHashSet<KeyStroke>(newKeys);
      added.removeAll(_oldKeys); // the keys that were added
      Set<KeyStroke> retained = new LinkedHashSet<KeyStroke>(_oldKeys);
      retained.retainAll(newKeys); // the keys that were kept the same
      boolean update = false;
      KeyStrokeData data = _actionToDataMap.get(_a);
      if (data == null) {
        // Nothing to change
        return;
      }
      
      // check for removed keys
      for(KeyStroke ks: removed) {
        // Only remove the old keystroke from the map if it is currently mapped to our data.  If not, our old
        // keystroke has already been redefined and should not be removed!
        if (data.equals(_keyToDataMap.get(ks))) {
          _keyToDataMap.remove(ks);
          update = true;
        }
      }
      
      // check added keys for conflicts
      for(KeyStroke ks: added) {
        if (shouldUpdate(ks, _a)) {          
          _keyToDataMap.put(ks,data);
          retained.add(ks);
          update = true;
        }
      }
      
      if (update) {        
        Vector<KeyStroke> v = new Vector<KeyStroke>(retained);
        data.setKeyStrokes(v);
        updateMenuItem(data);
        _oldKeys = retained;
      }
    }
  }

  public static class KeyStrokeData {
    private Vector<KeyStroke> _ks;
    private Action _a;
    private JMenuItem _jmi;
    private String _name;
    private VectorOption<KeyStroke> _vkso;

    public KeyStrokeData(Vector<KeyStroke> ks, Action a, JMenuItem jmi, String name, VectorOption<KeyStroke> vkso) {
      _ks = new Vector<KeyStroke>(ks);
      _a = a;
      _jmi = jmi;
      _name = name;
      _vkso = vkso;
    }

    public Vector<KeyStroke> getKeyStrokes() { return _ks; }
    public Action getAction() { return _a; }
    public JMenuItem getJMenuItem() { return _jmi; }
    public String getName() { return _name; }
    public VectorOption<KeyStroke> getOption() { return _vkso; }
    
    public void setKeyStrokes(Vector<KeyStroke> ks) { _ks = new Vector<KeyStroke>(ks); }
    public void setAction(Action a) { _a = a; }
    public void setJMenuItem(JMenuItem jmi) { _jmi = jmi; }
    public void setName(String name) { _name = name; }
    public void setOption(VectorOption<KeyStroke> vkso) { _vkso = vkso; }
  }
}
