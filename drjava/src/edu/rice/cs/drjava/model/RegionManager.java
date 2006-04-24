/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;


import java.util.Vector;

/**
 * Interface for a region manager.
 *
 * @version $Id$
 */
public interface RegionManager<R extends DocumentRegion> {    
  /** Returns the region in this manager at the given offset, or null if one does not exist.
   *  @param odd the document
   *  @param offset the offset in the document
   *  @return the DocumentRegion at the given line number, or null if it does not exist.
   */
  public R getRegionAt(OpenDefinitionsDocument odd, int offset);

  /** Get the DocumentRegion that is stored in this RegionsTreePanel overlapping the area for the given document,
   *  or null if it doesn't exist.
   *  @param odd the document
   *  @param startOffset the start offset
   *  @param endOffset the end offset
   *  @return the DocumentRegion or null
   */
  public R getRegionOverlapping(OpenDefinitionsDocument odd, int startOffset, int endOffset);
  
  /** Add the supplied DocumentRegion to the manager.
   *  @param region the DocumentRegion to be inserted into the manager
   */
  public void addRegion(R region);

  /** Remove the given DocumentRegion from the manager.
   *  @param region the DocumentRegion to be removed.
   */
  public void removeRegion(R region);

  /** Helper method to actually copy data from newRegion into oldRegion, and returns true if a change was made.
   *  However, since the basic DocumentRegion does not contain any additional data, this is a no-op, but
   *  this method should be overridden, e.g. for use with Breakpoint.
   *  @param oldRegion the region to find and change
   *  @param newRegion a region with data to copy into the old region
   *  @return true if a change was made (here always false) */
  public boolean changeRegionHelper(R oldRegion, R newRegion);

  /** Uses changeRegionHelper to copy data from newRegion into oldRegion, if oldRegion is found, and returns true
   *  if a change was actually made.
   *  @param oldRegion the region to find and change
   *  @param newRegion a region with data to copy into the old region
   *  @return true if a change was made */
  public boolean changeRegion(R oldRegion, R newRegion);

  /** @return a Vector<R> containing the DocumentRegion objects in this mangager. */
  public Vector<R> getRegions();

  /** Tells the manager to remove all regions. */
  public void clearRegions();

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
}
