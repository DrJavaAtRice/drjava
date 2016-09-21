/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.util.swing;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import java.io.File;

/** This FileDisplayManager uses the same icons and naming schemes as the JFileChooser offered by swing. */
public class DefaultFileDisplayManager implements FileDisplayManager {
  
  private JFileChooser _jfc;
  
  public DefaultFileDisplayManager() { _jfc = new JFileChooser(); }
  
  /** Given a file, decide which icon to use
    * @param f The file to base the icon decision on
    * @return The icon to display for the given file
    */
  public Icon getIcon(File f) {
    // avoid problem with windows filesystem drivers that would cause a filenotfound exception
    if (f != null && ! f.exists()) f = null; 
    
    return _jfc.getIcon(f);
  }
  
  /** Given a file, decide on which name to display for it
    * @param f The file to base the naming decison on
    * @return The name to display for the file
    */
  public String getName(File f) { return _jfc.getName(f); }
  
  /** Creates a file display that displays a file as this manager specifies
    * @param f the file to display using the display manager
    * @return the file display object used to display a file's name
    */
  public FileDisplay makeFileDisplay(File f) { return new FileDisplay(f, this); }
  
  /** Creates a file display that displays a file as this manager specifies
    * @param parent the parent of the file to display using the display manager
    * @param child the name of the child such that <code>new File(parent, child)</code> is the file to be displayed.
    * @return the file display object used to display a file's name
    */
  public FileDisplay makeFileDisplay(File parent, String child) { return new FileDisplay(parent, child, this); }
  
  /** Creates a FileDisplay representing a new untitled folder that is yet to be created.
    * @param parent the parent folder of the new folder
    * @return the new file display
    */
  public FileDisplay makeNewFolderDisplay(File parent) { return FileDisplay.newFile(parent, this); }
  
  /** Updates the UI to reflect any changes in the fs. */
  public void update() { _jfc.updateUI(); }
}