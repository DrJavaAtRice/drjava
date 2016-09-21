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

package edu.rice.cs.drjava.project;

import java.io.File;

import edu.rice.cs.plt.tuple.Pair;

/** Classes that implement this interface are expected to give information specific to a single document that is
 *  to be saved by the project file builder.  These objects are passed to the builder upon a save.  These objects
 *  should not be cached anywhere so that they may be gc'd.  They are meant for temporary transmition of data.
 */
public interface DocumentInfoGetter {
  
  /** @return the selection with the start and ending locations paired together. The cursor location is at the second
   *  location in the pair while the selection is defined between the two.
   */
  public Pair<Integer,Integer> getSelection();
  
  /** @return the scroll offset of the scroll pane with the first element being the vertical scroll and the second 
   *  being the horizontal scroll
   */
  public Pair<Integer,Integer> getScroll();
  
  /** @return the filename of the document being described by this getter. */
  public File getFile();
  
  /** Returns the package declared in the java code within the document being described
   *  @return the package of this document.  
   */
  public String getPackage();
  
  /** @return true iff the described document is the current active document. */
  public boolean isActive();
  
  /** @return whether the document has a file currently associated with it. */
  public boolean isUntitled();
}