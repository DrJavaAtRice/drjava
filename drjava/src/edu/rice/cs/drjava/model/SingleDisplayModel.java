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
import java.util.List;

/**
 * A GlobalModel that enforces invariants associated with having
 * one active document at a time.
 *
 * Invariants:
 * <OL>
 * <LI>{@link #getOpenDefinitionsDocuments} will always return an array of
 *     at least size 1.
 * </LI>
 * <LI>(follows from previous) If there is ever no document in the model,
 *     a new one will be created.
 * </LI>
 * <LI>There is always exactly one active document, which can be get/set
 *     via {@link #getActiveDocument} and {@link #setActiveDocument}.
 * </LI>
 * </OL>
 *
 * Other functions added by this class:
 * <OL>
 * <LI>When calling {@link #openFile}, if there is currently only one open
 *     document, and it is untitled and unchanged, it will be closed after the
 *     new document is opened. This means that, in one atomic transaction, the
 *     model goes from having one totally empty document open to having one
 *     document (the requested one) open.
 * </LI>
 * </OL>
 *
 * @version $Id: SingleDisplayModel.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public interface SingleDisplayModel extends GlobalModel {
  /** @return the currently active document. */
  public OpenDefinitionsDocument getActiveDocument();

  /** Sets the currently active document by updating the selection model.
   *  @param doc Document to set as active
   */
  public void setActiveDocument(OpenDefinitionsDocument doc);
  
  /** Invokes the activeDocumentChanged method in the global listener on the argument _activeDocument. */
  public void refreshActiveDocument();

  /** @return the IDocumentNavigator container expressed as an AWT component. */
  public java.awt.Container getDocCollectionWidget();

  /** Sets the active document to be the next one in the list. */
  public void setActiveNextDocument();

  /** Sets the active document to be the previous one in the list. */
  public void setActivePreviousDocument();

  /** Shared code between close project and close All files which only sets the new active document after all documents
   *  to be closed have been closed.
   *  @param docList the list of files to close
   *  @return whether all files were closed
   */
  public boolean closeFiles(List<OpenDefinitionsDocument> docList);  
  
  public void setActiveFirstDocument();
  
  public void dispose();
  
  /** Disposes of external resources, e.g. other VMs. */
  public void disposeExternalResources();

  public boolean closeAllFilesOnQuit();
  
  /** Return an array of the files excluded from the current project */
  public File[] getExclFiles();
  
  /** Sets the array of files excluded from the current project */
  public void setExcludedFiles(File[] fs);

// Any lightweight parsing has been disabled until we have something that is beneficial and works better in the background.
//  /** @return the parsing control */
//  public LightWeightParsingControl getParsingControl();
  
  /** Ensures that the _jvmStarter thread has executed. Never called in practice outside of GlobalModelTestCase.setUp(). */
  public void ensureJVMStarterFinished();
}
