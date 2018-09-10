/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu).  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
 * following conditions are met:
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *      disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *      following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the names of its contributors may 
 *      be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui.config;

import edu.rice.cs.drjava.DrScala;
import edu.rice.cs.drjava.DrScalaTestCase;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.util.swing.DefaultSwingFrame;
import edu.rice.cs.util.swing.Utilities;

import java.io.File;
import java.util.ArrayList;

/** Tests functionality of this OptionComponent */
public final class ArrayListFileOptionComponentTest extends DrScalaTestCase {
  private static ArrayListFileOptionComponent _option;

  protected void setUp() throws Exception {
    super.setUp();
    _option = new ArrayListFileOptionComponent(OptionConstants.EXTRA_CLASSPATH, "Extra Classpath", 
                                               new DefaultSwingFrame());
    DrScala.getConfig().resetToDefaults();
    Utilities.clearEventQueue();
  }

  public void testCancelDoesNotChangeConfig() {
    ArrayList<File> testArrayList = new ArrayList<File>();
    testArrayList.add(new File("test"));

    _option.setValue(testArrayList);
    Utilities.clearEventQueue();
    _option.resetToCurrent(); // should reset to the original.
    Utilities.clearEventQueue();
    _option.updateConfig(); // should update with original values therefore no change.
    Utilities.clearEventQueue();
    
    assertTrue("Cancel (resetToCurrent) should not change the config",
               OptionConstants.EXTRA_CLASSPATH.getDefault().equals(DrScala.getConfig().getSetting(OptionConstants.EXTRA_CLASSPATH)));
  }

  public void testApplyDoesChangeConfig() {
    ArrayList<File> testArrayList = new ArrayList<File>();
    testArrayList.add(new File("blah"));

    _option.setValue(testArrayList);
    Utilities.clearEventQueue();
    _option.updateConfig();
    Utilities.clearEventQueue();

    assertTrue("Apply (updateConfig) should write change to file",
               testArrayList.equals(DrScala.getConfig().getSetting(OptionConstants.EXTRA_CLASSPATH)));
  }

  public void testApplyThenResetDefault() {
    ArrayList<File> testArrayList = new ArrayList<File>();
    testArrayList.add(new File("blah"));

    _option.setValue(testArrayList);
    Utilities.clearEventQueue();
    _option.updateConfig();
    Utilities.clearEventQueue();

    
    _option.resetToDefault();     // resets to default
    Utilities.clearEventQueue();  // preceding line generates event queue tasks
    _option.updateConfig();
    Utilities.clearEventQueue();
    
    assertTrue("Apply (updateConfig) should write change to file",
               OptionConstants.EXTRA_CLASSPATH.getDefault().equals(DrScala.getConfig().getSetting(OptionConstants.EXTRA_CLASSPATH)));
  }

//  /** The equals method for a parameterized Vector.
//    * @param v1 the first Vector<File>
//    * @param v2 the Vector<File> to compare with
//    * @return <code>true</code> iff the two vectors are equal
//    */
//  public boolean vectorEquals(Vector<File> v1, Vector<File> v2) {
//    if (v1.size() == v2.size()) {
//      for (int i = 0; i < v1.size(); i++) {
//        if (!v1.elementAt(i).equals(v2.elementAt(i)))  return false;
//      }
//      return true;
//    }
//    else return false; /* different sizes */
//  }
}
