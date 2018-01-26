/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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
package edu.rice.cs.drjava.model;
import edu.rice.cs.drjava.DrJavaTestCase;

/** DummyOpenDefDocTest for unit testing DummyOpenDefDoc.  Uses JUnit for testing.
  * @author <a href="mailto:jasonbs@rice.edu">Jason Schiller</a>
  * @version $Id$
  */

public class DummyOpenDefDocTest extends DrJavaTestCase {
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements isModifiedOnDisk(). */
  public void testModifiedOnDisk() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.modifiedOnDisk();
      fail("DummyOpenDefDoc.modifiedOnDisk did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.modifiedOnDisk did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements addBrowserRegion(...). */
  public void testAddBrowserRegion() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.addBrowserRegion(null);
      fail("DummyOpenDefDoc.addBrowserRegion did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.addBrowserRegion did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements removeBrowserRegion(...). */
  public void testRemoveBrowserRegion() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.removeBrowserRegion(null);
      fail("DummyOpenDefDoc.removeBrowserRegion did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.removeBrowserRegion did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements saveFile(...). */
  public void testSaveFile() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.saveFile(null);
      fail("DummyOpenDefDoc.saveFile did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.saveFile did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements canAbandonFile(...). */
  public void testCanAbandonFile() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.canAbandonFile();
      fail("DummyOpenDefDoc.canAbandonFile did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.canAbandonFile did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements quitFile(...). */
  public void testQuitFile() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.quitFile();
      fail("DummyOpenDefDoc.quitFile did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.quitFile did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements setCurrentLocation(...). */
  public void testSetCurrentLocation() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.setCurrentLocation(0);
      fail("DummyOpenDefDoc.setCurrentLocation did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.setCurrentLocation did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements getDocument(...). */
  public void testGetDocument() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.getDocument();
      fail("DummyOpenDefDoc.getDocument did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.getDocument did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements isModifiedSinceSave(...). */
  public void testIsModifiedSinceSave() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.isModifiedSinceSave();
      fail("DummyOpenDefDoc.isModifiedSinceSave did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.isModifiedSinceSave did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements balanceForward(...). */
  public void testBalanceForward() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.balanceForward();
      fail("DummyOpenDefDoc.balanceForward did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.balanceForward did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements balanceBackward(...). */
  public void testBalanceBackward() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.balanceBackward();
      fail("DummyOpenDefDoc.balanceBackward did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.balanceBackward did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements getFile(...). */
  public void testGetFile() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.getFile();
      fail("DummyOpenDefDoc.getFile did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.getFile did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that getRawFile(...) returns null. */
  public void testGetRawFile() {
    DummyOpenDefDoc dummy = new DummyOpenDefDoc();
    assertEquals("getRawFile() does not return null", null, dummy.getRawFile());
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements getParentDirectory(...). */
  public void testParentDirectory() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.getParentDirectory();
      fail("DummyOpenDefDoc.getParentDirectory did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.getParentDirectory did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements fileExists(...). */
  public void testFileExists() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.fileExists();
      fail("DummyOpenDefDoc.fileExists did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.fileExists did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements verifyExists(...). */
  public void testVerifyExists() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.verifyExists();
      fail("DummyOpenDefDoc.verifyExists did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.verifyExists did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements cleanUpPrintJob(...). */
  public void testCleanUpPrintJob() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.cleanUpPrintJob();
      fail("DummyOpenDefDoc.cleanUpPrintJob did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.cleanUpPrintJob did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements getFirstTopLevelClassName(...). */
  public void testGetFirstTopLevelClassName() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.getFirstTopLevelClassName();
      fail("DummyOpenDefDoc.getFirstTopLevelClassName did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.getFirstTopLevelClassName did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements startCompile(...). */
  public void testStartCompile() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.startCompile();
      fail("DummyOpenDefDoc.startCompile did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.startCompile did not throw UnsupportedOperationException, but " + e);
    }
  }
  
  /** Creates a DummyOpenDefDoc and ensures that it properly implements runMain(...). */
  public void testRunMain() {
    try {
      DummyOpenDefDoc dummy = new DummyOpenDefDoc();
      dummy.runMain("");
      fail("DummyOpenDefDoc.runMain did not throw UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      //Do nothing, this is expected.
    }
    catch(Exception e) {
      fail("DummyOpenDefDoc.runMain did not throw UnsupportedOperationException, but " + e);
    }
  }
}
