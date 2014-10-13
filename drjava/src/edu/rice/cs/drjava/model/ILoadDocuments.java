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

import java.io.IOException;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.OperationCanceledException;

/** This interface encapsulates the behavior of a document loader. Components which provide a means to load 
 *  documents (e.g. from disk, from a stream, from the ether) should implement this interface, rather than the
 *  entire GlobalModel.  Components which simply need access to documents should use the more general IGetDocuments
 *  instead of this interface.
 *  TODO: Subdivide GlobalModelListener to target only events generated here.
 *  TODO: Simplify signatures to avoid command pattern overhead.
 *  @see GlobalModel
 *  @see DefaultGlobalModel
 *  @version $Id: ILoadDocuments.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public interface ILoadDocuments {
  /** Open a file and read it into the definitions.  The provided file selector chooses a file, and on a successful
   *  open, the fileOpened() event is fired.
   *  @param com a command pattern command that selects what file to open
   *  @return The open document, or null if unsuccessful
   *  @exception IOException
   *  @exception OperationCanceledException if the open was canceled
   *  @exception AlreadyOpenException if the file is already open
   */
  public OpenDefinitionsDocument openFile(FileOpenSelector com) throws IOException, OperationCanceledException, 
    AlreadyOpenException;

  /** Opens multiple files and reads them into the definitions.  The provided file selector chooses multiple files, 
   *  and for each successful open, the fileOpened() event is fired. Note that getFile called on the returned 
   *  OpenDefinitionsDocument is guaranteed to return an absolute path, as this method makes it absolute.
   * 
   *  @param com a command pattern command that selects which files to open
   *  @return The last opened document, or null if unsuccessful.
   *  @exception IOException if an underlying I/O operation fails
   *  @exception OperationCanceledException if the open was canceled
   *  @exception AlreadyOpenException if the file is already open
   */
  public OpenDefinitionsDocument[] openFiles(FileOpenSelector com) throws IOException, OperationCanceledException, 
    AlreadyOpenException;
}