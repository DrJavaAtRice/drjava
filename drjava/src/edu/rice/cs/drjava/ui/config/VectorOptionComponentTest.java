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

package edu.rice.cs.drjava.ui.config;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Vector;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.DrJava;

import junit.framework.*;
import junit.extensions.*;

/**
 * Tests functionality of this OptionComponent
 */
public final class VectorOptionComponentTest extends TestCase {
  
  private static VectorOptionComponent _option;
 
  public VectorOptionComponentTest(String name) {
    super(name);
  }
    
  protected void setUp() {
    _option = new VectorOptionComponent( OptionConstants.EXTRA_CLASSPATH, "Extra Classpath", new Frame());
    DrJava.getConfig().resetToDefaults();
    
  }
  
  public void testCancelDoesNotChangeConfig() {

    Vector<File> testVector = new Vector<File>();
    testVector.add(new File("test"));
    
    _option.setValue(testVector);
    _option.resetToCurrent(); // should reset to the original.
    _option.updateConfig(); // should update with original values therefore no change.
  
    assertTrue("Cancel (resetToCurrent) should not change the config",
               vectorEquals( OptionConstants.EXTRA_CLASSPATH.getDefault(),
                            DrJava.getConfig().getSetting(OptionConstants.EXTRA_CLASSPATH)));
    
  }
  
  public void testApplyDoesChangeConfig() {
    Vector<File> testVector = new Vector<File>();
    testVector.add(new File("blah"));
    
    _option.setValue(testVector);
    _option.updateConfig();
    
    assertTrue("Apply (updateConfig) should write change to file",
               vectorEquals( testVector,
                            DrJava.getConfig().getSetting(OptionConstants.EXTRA_CLASSPATH)));
  }
  
  public void testApplyThenResetDefault() {
    Vector<File> testVector = new Vector<File>();
    testVector.add(new File("blah"));
    
    _option.setValue(testVector);
    _option.updateConfig();
    _option.resetToDefault(); // resets to default
    _option.updateConfig();
    
    assertTrue("Apply (updateConfig) should write change to file",
                 vectorEquals( OptionConstants.EXTRA_CLASSPATH.getDefault(),
                              DrJava.getConfig().getSetting(OptionConstants.EXTRA_CLASSPATH)));
  }
    
    /**
   * The equals method for a parameterized Vector
   * @param o the compared Vector<T>
   * @return boolean
   */
  public boolean vectorEquals( Vector<File> v1, Vector<File> v2) {
    
    if (v1.size() == v2.size()) {
      
      for (int i=0; i<v1.size(); i++) {
        if (!v1.get(i).equals( v2.get(i))) return false;
      }
      return true;
    }
    else return false;
  }
  
}
