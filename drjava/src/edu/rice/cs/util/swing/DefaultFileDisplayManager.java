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

package edu.rice.cs.util.swing;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import java.io.File;

/**
 * This FileDisplayManager uses the same icons and naming 
 * schemes as the JFileChooser offered by swing.
 */
public class DefaultFileDisplayManager implements FileDisplayManager {
  
  private JFileChooser _jfc;
  
  public DefaultFileDisplayManager() {
    _jfc = new JFileChooser();
  }
  
  /**
   * Given a file, decide which icon to use
   * @param f The file to base the icon decision on
   * @return The icon to display for the given file
   */
  public Icon getIcon(File f) {
    // avoid problem with windows filesystem drivers
    // that would cause a filenotfound exception
    if (f != null && !f.exists()) f = null; 
    
    return _jfc.getIcon(f);
  }
  
  /**
   * Given a file, decide on which name to display for it
   * @param f The file to base the naming decison on
   * @return The name to display for the file
   */
  public String getName(File f) {
    return _jfc.getName(f);
  }
  
  /**
   * Creates a file display that displays a file as this manager specifies
   * @param f the file to display using the display manager
   * @return the file display object used to display a file's name
   */
  public FileDisplay makeFileDisplay(File f) {
    return new FileDisplay(f, this);
  }
  
  /**
   * Creates a FileDisplay representing a new untitled folder that is 
   * yet to be created.
   * @param f the parent location to place this new folder
   * @return the new file display
   */
  public FileDisplay makeNewFolderDisplay(File parent) {
    return FileDisplay.newFile(parent, this);
  }
  
  /**
   * Updates the UI to reflect any changes in the fs
   */
  public void update() {
    _jfc.updateUI();
  }
}