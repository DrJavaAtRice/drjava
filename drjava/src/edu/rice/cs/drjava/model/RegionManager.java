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

import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

/** Interface for a region manager.  Region ordering (as in StaticDocumentRegion) is not required, but it facilitates 
  * efficient implementation.
  * @version $Id$
  */
public interface RegionManager<R extends IDocumentRegion> {
  
  /** Returns the unique region [start,end), if any, containing the specified offset.  Assumes that document regions
    * are disjoint.
    * @param odd the document
    * @param offset the offset in the document
    * @return the unique StaticDocumentRegion containing the given offset, or null if it does not exist.
    */
  public R getRegionAt(OpenDefinitionsDocument odd, int offset);
  
  /** Returns the first and last region r where {@code r.getLineStart() <= offset < r.getLineEnd()}.  Assumes that 
    * document regions are disjoint and that lineStart precedes the start offset by at most 119 characters.
    * @param odd the document
    * @param offset the offset in the document
    * @return the unique StaticDocumentRegion containing the given offset, or null if it does not exist.
    */
  public Pair<R, R> getRegionInterval(OpenDefinitionsDocument odd, int offset);
  
  /** Returns the rightmost region starting on the same line containing the specified selection
   * @param odd the document
   * @param startOffset the start offset
   * @param endOffset the end offset
   * @return the rightmost StaticDocumentRegion containing the given selection, or 
   *         null if it does not exist.
   */
  public Collection<R> getRegionsOverlapping(OpenDefinitionsDocument odd, int startOffset, int endOffset);

  /** Tests if specified region r is contained in this manager.
    * @param r  The region
    * @return  whether the manager contains region r
    */
  public boolean contains(R r);
  
  /** Add the supplied StaticDocumentRegion to the manager.
   *  @param region the StaticDocumentRegion to be inserted into the manager
   */
  public void addRegion(R region);

  /** Remove the given StaticDocumentRegion from the manager.
   *  @param region the StaticDocumentRegion to be removed.
   */
  public void removeRegion(R region);
  
  /** Remove the given DocumentRegions from the manager.
   *  @param regions the DocumentRegions to be removed.
   */
  public void removeRegions(Iterable<? extends R> regions);
  
  /** Remove the given OpenDefinitionsDocument and all of its regions from the manager. 
   * @param odd the document for which to remove regions
   */
  public void removeRegions(OpenDefinitionsDocument odd);

  /** Apply the given command to the specified region to change it.
   *  @param region the region to find and change
   *  @param cmd command that mutates the region. */
  public void changeRegion(R region, Lambda<R,Object> cmd);
  
  /** @param odd the document for which to get regions
   * @return a {@code Vector<R>} containing the StaticDocumentRegion objects for 
   *         document odd in this manager. 
   */
  public RegionSet<R> getRegions(OpenDefinitionsDocument odd);
  
  /** @return a {@code Vector<R>} containing all the StaticDocumentRegion objects in this mangager. */
  public ArrayList<R> getRegions();

  /** @return the number if regions contained in this manager. */
  public int getRegionCount();
  
  /** @param r the upper bound on regions
   * @return the sorted set of regions less than r. 
   */
  public SortedSet<R> getHeadSet(R r);
  
  /** @param r the lower bound on regions
   * @return the sorted set of regions greater than or equal to r. 
   */
  public SortedSet<R> getTailSet(R r);

  /** Tells the manager to remove all regions. */
  public void clearRegions();

  /** @return the set of documents containing regions. */
  public Set<OpenDefinitionsDocument> getDocuments();
  
  /** Updates _lineStartPos, _lineEndPos of regions in the interval 
   * [firstRegion, lastRegion] using total ordering on regions.  
   * Removes empty regions.  firstRegion and lastRegion are not necessarily 
   * regions in this manager.  
   * @param firstRegion lower bound on the interval
   * @param lastRegion upper bound on the interval
   */
  public void updateLines(R firstRegion, R lastRegion);
  
  /** Adds a listener to the notifier.
   *  @param listener a listener that reacts on events
   */
  public void addListener(RegionManagerListener<R> listener);
  
  /** Removes a listener from the notifier.
   *  @param listener a listener that reacts on events
   */
  public void removeListener(RegionManagerListener<R> listener);

  /** Removes all listeners from this notifier.  */
  public void removeAllListeners();
  
//  /** @return the corresponding MutableTreeNode. */
//  public DefaultMutableTreeNode getTreeNode(OpenDefinitionsDocument doc);
//  
//  /** Sets the MutableTreeNode corresponding to this region. */
//  public void setTreeNode(OpenDefinitionsDocument doc, DefaultMutableTreeNode n);
}
