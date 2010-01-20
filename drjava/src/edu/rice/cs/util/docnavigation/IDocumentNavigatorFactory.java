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

package edu.rice.cs.util.docnavigation;
import edu.rice.cs.plt.tuple.Pair;
import java.util.List;

/** Factory that produces bbjects that implement IDocumentNavigator. */

public interface IDocumentNavigatorFactory<ItemT extends INavigatorItem> {
  
  /** Creates a new List Navigator
    * @return a list navigator
    */
  public IDocumentNavigator<ItemT> makeListNavigator();
  
  /** Returns a new tree Navigator with the specified root
    * @param name the name of the root node
    * @return a tree navigator
    */
  public IDocumentNavigator<ItemT> makeTreeNavigator(String name);
  
  /** Creates a list navigator and migrates the navigator items from parent to the new navigator
    * @param parent the navigator to migrate from
    * @return the new list navigator
    */
  // Note: parent's type cannot be relaxed with a wildcard.  The type has an upper bound of ItemT,
  // because the new navigator must be able to contain all of its items; the type has a lower bound
  // of ItemT, because the parent's listeners must be able to handle all items in the new navigator.
  public IDocumentNavigator<ItemT> makeListNavigator(IDocumentNavigator<ItemT> parent);
  
  /** Creates a tree navigator and migrates the navigator items from the parent to the new navigator
    * @param name the name of the root node
    * @param parent the navigator to migrate from
    * @return the new tree navigator
    */
  public IDocumentNavigator<ItemT> makeTreeNavigator(String name, IDocumentNavigator<ItemT> parent, 
                                                     List<Pair<String, INavigatorItemFilter<ItemT>>> l);
}
