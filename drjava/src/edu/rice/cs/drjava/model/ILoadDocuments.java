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
 *  @version $Id$
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