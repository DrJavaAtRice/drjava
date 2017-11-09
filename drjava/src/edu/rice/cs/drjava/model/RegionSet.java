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

package edu.rice.cs.drjava.model;

import java.util.Collection;
import java.util.TreeSet;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import edu.rice.cs.util.swing.Utilities;

/** A set designed to store IDocumentRegions; extends TreeSet by automatically re-balancing when the underlying
  * document changes in such a way that the relative order of a pair of regions may flip.
  * 
  * Also optionally contains a reference to the ConcreteRegionManager that uses this region set, and notifies 
  * the manager on changes. Note that RegionSets can be used broadly by all types of RegionManagers; however, 
  * notification will only ever be requested by ConcreteRegionManagers.
  */
public class RegionSet<R extends IDocumentRegion> extends TreeSet<R> {

    /* Assumes that this RegionSet will only have regions from one document */
    private DocumentListener _docListener = null;

    /* Assumes that this RegionSet will belong to only one RegionManager.
     * Also assumes the manager is Concrete; in reality, it need not be, but 
     * we will only ever set the _manager field if it is (since this is only
     * used for find/replace). 
     */
    private ConcreteRegionManager<OrderedDocumentRegion> _manager = null;

    public void setManager(ConcreteRegionManager<OrderedDocumentRegion> manager) { 
      this._manager = manager; 
    }

    /** Set _docListener to listen on region.getDocument(), if not already set. 
      * @param region the region whose document should be listened on
      */
    private void _setDocListener(R region) {

      if (this._docListener != null) return;

      OpenDefinitionsDocument odd = region.getDocument();

      /* Listen on changes to the document, as these may affect the region. */
      final RegionSet<R> thisRef = this;
      _docListener = new DocumentListener() {

        public void insertUpdate(DocumentEvent e) {
          /* Insertion can't cause positions to flip */
          /* But we should still notify the RegionManager, if requested. */
          if (thisRef._manager != null) {
            for (R region : thisRef) {
              thisRef._manager.notifyChangedRegion((OrderedDocumentRegion)region);
            }
          }
        }

        public void removeUpdate(DocumentEvent e) {

          final DocumentEvent finalE = e;
          Utilities.invokeLater(new Runnable() { 

            public void run() {

              /* Removal can cause positions to flip, but only need to worry if the removed portion is within the bounds
               * of one or more regions. 
               */
              boolean requireRebalance = false;
              for (R region : thisRef) {
                if (region.getStartOffset() <= finalE.getOffset() &&
                  finalE.getOffset() <= region.getEndOffset()) {
                     requireRebalance = true;
                     break;
                }
              }

              /* Brute-force re-balance; can be relatively expensive if there are many regions, but in practice runs
               * infrequently enough so as to be unnoticeable. Note: this code can probably be made ore efficient 
               * because the region set to be rebalanced is almost in balance already.  Not worth the complication.
               */
              if (requireRebalance) {
                @SuppressWarnings("unchecked")
                RegionSet<R> thisCopy = (RegionSet<R>)thisRef.clone();
                thisRef.clear();
                thisRef.addAll(thisCopy);
              }

              /* Notify the RegionManager, if requested. */
              if (thisRef._manager != null) {
                for (R region : thisRef) {
                  thisRef._manager.notifyChangedRegion((OrderedDocumentRegion)region);
                }
              }
            }
           });
        }

        public void changedUpdate(DocumentEvent e) {
          /* Apparently not used for documents. */
          return;
        }
      };
      odd.addDocumentListener(_docListener);
    }

    /** Adds an input region to the set. Also sets up a listener on the document to which the region belongs, if this
      * is the first time an add method is being called.
      * @param region the region to add
      * @return indication of success
      */
    public boolean add(R region) {
      this._setDocListener(region);
      return super.add(region);
    }

    /** Adds all input regions to the set. Also sets up a listener on the document to which the regions belong, if this
      * is the first time an add
      * method is being called.
      * @param regions the regions to add
      * @return indication of success
      */
    public boolean addAll(Collection<? extends R> regions) {
        for (R region : regions) {
            this._setDocListener(region);
        }
        return super.addAll(regions);
    }
}

