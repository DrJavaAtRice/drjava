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

package edu.rice.cs.drjava.model;

import java.io.File;

import edu.rice.cs.util.OperationCanceledException;

/** An interface to give GlobalModel a file to save a document to.
  * @version $Id: FileSaveSelector.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public interface FileSaveSelector {
  
  /** Returns the file to save.
    * @throws OperationCanceledException if the save request is cancelled
    */
  public File getFile() throws OperationCanceledException;
  
  /** Informs the user that the chosen file is already open and prompts them asking whether to continue with the save
    * @param f the file being saved
    * @return true iff the save is to occur
    */
  public boolean warnFileOpen(File f);
  
  /** Confirms whether the existing chosen file should be overwritten.
    * @param f the file being saved
    * @return true iff the user wants to overwrite
    */
  public boolean verifyOverwrite(File f);
  
  /** Confirms whether a new file should be selected since the previously chosen file has been deleted or moved.
    * @param oldFile The file that was moved or deleted.
    */
  public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile);
  
  /** Return true if saving should update the document's state (i.e. new file name and reset
    * modification status). Save/save as = true. Save copy = false.
    * @return true to update document state */
  public boolean shouldUpdateDocumentState();
}
