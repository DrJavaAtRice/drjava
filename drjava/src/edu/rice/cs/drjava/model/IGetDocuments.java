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

package edu.rice.cs.drjava.model;

import java.io.File;
import java.io.IOException;
import javax.swing.ListModel;
import java.util.List;

/**
 * This interface encapsulates the behavior of a document store.
 * Model components which simply need to work with document text
 * should use this interface rather than the entire GlobalModel.
 * Documents which need to be loaded will most likely be retrieved from an
 * ILoadDocuments by a concrete implemention.
 * @see ILoadDocuments, GlobalModel, DefaultGlobalModel
 * @version $Id$
 */
public interface IGetDocuments {
  /**
   * Returns the OpenDefinitionsDocument for the specified
   * File, opening a new copy if one is not already open.
   * @param file File contained by the document to be returned
   * @return OpenDefinitionsDocument containing file
   */
  public OpenDefinitionsDocument getDocumentForFile(File file)
    throws IOException, OperationCanceledException;
  
  /**
   * Determines whether the named file is already open, or if it must be loaded.
   * Clients which want to avoid visual side-effects should check this before
   * calling getDocumentForFile.
   * @param file the File in question
   * @return true if the document is already open in this IGetDocument
   */
  public boolean isAlreadyOpen(File file);
  
  /**
   * Returns a collection of all documents currently open for editing.
   * This is equivalent to the results of getDocumentForFile for the set
   * of all files for which isAlreadyOpen returns true.
   * @return a random-access List of the open definitions documents.
   */
  public List<OpenDefinitionsDocument> getDefinitionsDocuments();
  
  /**
   * Checks if any open definitions documents have been modified
   * since last being saved.
   * @return whether any documents have been modified
   */
  public boolean hasModifiedDocuments();
}
