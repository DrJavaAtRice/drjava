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

package edu.rice.cs.drjava.model.cache;

import javax.swing.text.BadLocationException;
import java.util.*;
import java.io.IOException;

import edu.rice.cs.util.Pair;

import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.NoSuchDocumentException;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.FileMovedException;
import edu.rice.cs.util.UnexpectedException;


public class DocumentCache{
  
  Hashtable<OpenDefinitionsDocument,Pair<DefinitionsDocument,DDReconstructor>> table;
  
  /**
   * most recent are first
   * least recent are last
   */
  LinkedList<OpenDefinitionsDocument> lru;
  
  private int CACHE_SIZE = 24;
  
  public DocumentCache(){
    lru = new LinkedList<OpenDefinitionsDocument>();
    table = new Hashtable<OpenDefinitionsDocument,Pair<DefinitionsDocument,DDReconstructor>>();
  }

  /**
   * adds the ODD to the cache with the given document and reconstructor as its value (a pair Open)
   * @param odd the ODD to use as the key to the cache
   * @param reconstructor a Open which can make another DefinitionsDocument for this odd if needed
   */
  public void put(OpenDefinitionsDocument odd, DDReconstructor reconstructor){
    //System.out.println("put: " + odd);
    Pair<DefinitionsDocument,DDReconstructor> pair = new Pair<DefinitionsDocument,DDReconstructor>(null, reconstructor);
    table.remove(odd);
    table.put(odd, pair);
    lru.remove(odd);
    
//    System.out.println(this);
  }

  /**
   * retrieves the definitions document for the ODD.  If the dd is not available, it is reconstructed
   * then returned.  When the dd is retrieved, it is placed at the top of the most recently used list
   * so that it will stay in the cache longer
   * @param odd the ODD to use as the key to the cache
   * @return a DefinitionsDocument for this odd 
   */
  public DefinitionsDocument get(OpenDefinitionsDocument odd) throws IOException, FileMovedException{
    DefinitionsDocument retdoc;
    Pair<DefinitionsDocument,DDReconstructor> pair = table.get(odd);
    if(pair == null){
      throw new NoSuchDocumentException("Cannot obtain the definitions document for: " + odd);
    }
    retdoc = pair.getFirst();
    updatelru(odd, pair);
//    System.out.println(this);
    if(retdoc == null){
      try{
//        System.out.println("DocumentCache.java: 114: creating document from reconstructor for " + odd);
        retdoc = pair.getSecond().make();
        pair = new Pair<DefinitionsDocument,DDReconstructor>(retdoc, pair.getSecond());
        table.remove(odd);
        table.put(odd, pair);
      }catch(BadLocationException e){
        throw new UnexpectedException(e);
      }
    }
    return retdoc;
  }
  
  /**
   * @param odd the open definitions document who registered the reconstructor
   * @return the reconstructor associated with the given odd
   */
  public DDReconstructor getReconstructor(OpenDefinitionsDocument odd) {
    Pair<DefinitionsDocument,DDReconstructor> pair = table.get(odd);
    if(pair == null){
      throw new NoSuchDocumentException("Cannot obtain the reconstructor for: " + odd);
    }
    return pair.getSecond();
  }
  
  /**
   * updates the ODD to the cache with the given document and reconstructor as its value (a pair Open)
   * @param odd the ODD to use as the key to the cache
   * @param reconstructor a new reconstructor for this odd
   */
  public void update(OpenDefinitionsDocument odd, DDReconstructor reconstructor){
    //System.out.println("update " + odd);
    Pair<DefinitionsDocument,DDReconstructor> oldpair = table.get(odd);
    Pair<DefinitionsDocument,DDReconstructor> newpair = new Pair<DefinitionsDocument,DDReconstructor>(null, reconstructor);
    if(isDDocInCache(odd)){
      reconstructor.saveDocInfo(oldpair.getFirst());
      oldpair.getFirst().close();
    }
    oldpair = table.remove(odd);
    table.put(odd, newpair);
  }
  
  
  /**
   * updates the lru cache to have the input document as most recently used
   * @param odd the document that has been used most recently
   */
  private void updatelru(OpenDefinitionsDocument odd, Pair<DefinitionsDocument,DDReconstructor> pair){
    if(!lru.isEmpty() && lru.getFirst() == odd){
      //System.out.println("updatelru: " + odd + " is first in list");
      return;
    }
    lru.remove(odd);
    
    if(!(isDDocInCache(odd) && pair.getFirst().isModifiedSinceSave())){
//      System.out.println("adding " + odd + " to lru");
      lru.addFirst(odd);
    }
    
    //System.out.println("Cache size is : " + lru.size());
    if(lru.size() > CACHE_SIZE){
      odd = lru.removeLast();
      Pair<DefinitionsDocument,DDReconstructor> removedPair = table.get(odd);
//      System.out.println("should i dispose of " + odd + "?");
     
      if(isDDocInCache(odd) && removedPair.getFirst().isModifiedSinceSave()){
//        System.out.println("no");
      }else{
//        System.out.println("disposing of " + odd);
        update(odd, removedPair.getSecond());
      }
    }
  }
  
  
  
  /**
   * @return true if the DefinitionsDocument for this OpenDefinitionsDocument is in the cache
   * @param oddoc the key to this hash
   */
  public boolean isDDocInCache(OpenDefinitionsDocument oddoc){
//    System.out.print("checking doc in cache: " + oddoc);
    Pair<DefinitionsDocument,DDReconstructor> pair = table.get(oddoc);
    if(pair == null){
      throw new NoSuchDocumentException("Cannot obtain the needed definitions document for " + oddoc);
    }
    DefinitionsDocument retdoc = pair.getFirst();
    return retdoc != null;
  }
  
  
  public void removeDoc(OpenDefinitionsDocument odd){
    Pair<DefinitionsDocument,DDReconstructor> pair = table.remove(odd);
    if(pair.getFirst() != null){
//      pair.getSecond().saveDocInfo(pair.getFirst());
      pair.getFirst().close();
    }
    lru.remove(odd);
  }
  
  public void setCacheSize(int size) {
    if (size < 0) {
      throw new IllegalArgumentException("Cannot set the cache size less than 0");
    }
    CACHE_SIZE = size;
  }
  
  public int getCacheSize() {
    return CACHE_SIZE;
  }
  
  public int getNumInCache(){
    return lru.size();
  }
  
  public String toString() {
    return "Document Cache: LRU: " + lru;
  }
}
