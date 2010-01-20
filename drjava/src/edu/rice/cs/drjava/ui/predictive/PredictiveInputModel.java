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

package edu.rice.cs.drjava.ui.predictive;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/** Model class for predictive string input. */
public class PredictiveInputModel<T extends Comparable<? super T>> {
  
  /** Strategy used for matching and mask extension. */
  public static interface MatchingStrategy<X extends Comparable<? super X>> {
    
    /** Returns true if the item is a match.
     *  @param item item to check
     *  @param pim predictive input model
     *  @return true if the item is a match
     */
    public boolean isMatch(X item, PredictiveInputModel<X> pim);
    
    /** Returns true if the item is perfect a match.
     *  @param item item to check
     *  @param pim predictive input model
     *  @return true if the item is a match
     */
    public boolean isPerfectMatch(X item, PredictiveInputModel<X> pim);
    
    /** Returns true if the two items are equivalent under this matching strategy.
     *  @param item1 first item
     *  @param item2 second item
     *  @param pim predictive input model
     *  @return true if equivalent
     */
    public boolean equivalent(X item1, X item2, PredictiveInputModel<X> pim);
    
    /** Compare the two items and return -1, 0, or 1 if item1 is less than, equal to, or greater than item2.
      * @param item1 first item
      * @param item2 second item
      * @param pim predictive input model
      * @return -1, 0, or 1
      */
    public int compare(X item1, X item2, PredictiveInputModel<X> pim);

    /** Returns the item from the list that is the longest match.
      * @param item target item
      * @param items list with items
      * @param pim predictive input model
      * @return longest match
      */
    public X getLongestMatch(X item, List<X> items, PredictiveInputModel<X> pim);
    
    /** Returns the shared mask extension for the list of items.
      * @param items items for which the mask extension should be generated
      * @param pim predictive input model
      * @return the shared mask extension
      */
    public String getSharedMaskExtension(List<X> items, PredictiveInputModel<X> pim);
       
    /** Returns the mask extended by the shared extension.
      * @param items items for which the mask extension should be generated
      * @param pim predictive input model
      * @return the extended shared mask
      */
    public String getExtendedSharedMask(List<X> items, PredictiveInputModel<X> pim);
  
    /** Force the mask to fit this entry. The matching strategies that accept line numbers
      * can combine the current item with the line number. Other strategies just return the
      * current item.
      * @return forced string
      */
    public String force(X item, String mask);
  }
  
  /** Matching based on string prefix. */
  public static class PrefixStrategy<X extends Comparable<? super X>> implements MatchingStrategy<X> {
    public String toString() { return "Prefix"; }
    public boolean isMatch(X item, PredictiveInputModel<X> pim) {
      String a = (pim._ignoreCase) ? (item.toString().toLowerCase()) : (item.toString());
      String b = (pim._ignoreCase) ? (pim._mask.toLowerCase()) : (pim._mask);
      return a.startsWith(b);
    }
    public boolean isPerfectMatch(X item, PredictiveInputModel<X> pim) {
      String a = (pim._ignoreCase) ? (item.toString().toLowerCase()) : (item.toString());
      String b = (pim._ignoreCase) ? (pim._mask.toLowerCase()) : (pim._mask);
      return a.equals(b);
    }
    public boolean equivalent(X item1, X item2, PredictiveInputModel<X> pim) {
      String a = (pim._ignoreCase) ? (item1.toString().toLowerCase()) : (item1.toString());
      String b = (pim._ignoreCase) ? (item2.toString().toLowerCase()) : (item2.toString());
      return a.equals(b);
    }
    public int compare(X item1, X item2, PredictiveInputModel<X> pim) {
      String a = (pim._ignoreCase) ? (item1.toString().toLowerCase()) : (item1.toString());
      String b = (pim._ignoreCase) ? (item2.toString().toLowerCase()) : (item2.toString());
      return a.compareTo(b);
    }
    public X getLongestMatch(X item, List<X> items, PredictiveInputModel<X> pim) {
      X longestMatch = null;
      int matchLength = -1;
      for(X i: items) {
        String s = (pim._ignoreCase) ? (i.toString().toLowerCase()) : (i.toString());
        String t = (pim._ignoreCase) ? (item.toString().toLowerCase()) : (item.toString());
        int ml = 0;
        while((s.length() > ml) && (t.length() > ml) && (s.charAt(ml) == t.charAt(ml))) {
          ++ml;
        }
        if (ml>matchLength) {
          matchLength = ml;
          longestMatch = i;
        }
      }
      return longestMatch;
    }
    public String getSharedMaskExtension(List<X> items, PredictiveInputModel<X> pim) {
      StringBuilder res = new StringBuilder();
      String ext = "";
      if (items.size() == 0) {
        return ext;
      }
      boolean allMatching = true;
      int len = pim._mask.length();
      while((allMatching) && (pim._mask.length() + ext.length() < items.get(0).toString().length())) {
        char origCh = items.get(0).toString().charAt(pim._mask.length() + ext.length());
        char ch = (pim._ignoreCase) ? (Character.toLowerCase(origCh)) : (origCh);
        allMatching = true;
        for (X i: items) {
          String a = (pim._ignoreCase) ? (i.toString().toLowerCase()) : (i.toString());
          if (a.charAt(len) != ch) {
            allMatching = false;
            break;
          }
        }
        if (allMatching) {
          ext = ext + ch;
          res.append(origCh);
          ++len;
        }
      }
      return res.toString();
    }
    public String getExtendedSharedMask(List<X> items, PredictiveInputModel<X> pim) {
      return pim._mask + getSharedMaskExtension(items, pim);
    }
    public String force(X item, String mask) { return item.toString(); }
  };
  
  /** Matching based on string fragments. */
  public static class FragmentStrategy<X extends Comparable<? super X>> implements MatchingStrategy<X> {
    public String toString() { return "Fragments"; }
    public boolean isMatch(X item, PredictiveInputModel<X> pim) {
      String a = (pim._ignoreCase) ? (item.toString().toLowerCase()) : (item.toString());
      String b = (pim._ignoreCase) ? (pim._mask.toLowerCase()) : (pim._mask);

      java.util.StringTokenizer tok = new java.util.StringTokenizer(b);
      while(tok.hasMoreTokens()) {
        if (a.indexOf(tok.nextToken()) < 0) return false;
      }
      return true;
    }
    public boolean isPerfectMatch(X item, PredictiveInputModel<X> pim) {
      String a = (pim._ignoreCase) ? (item.toString().toLowerCase()) : (item.toString());
      String b = (pim._ignoreCase) ? (pim._mask.toLowerCase()) : (pim._mask);
      return a.equals(b);
    }
    public boolean equivalent(X item1, X item2, PredictiveInputModel<X> pim) {
      String a = (pim._ignoreCase)?(item1.toString().toLowerCase()):(item1.toString());
      String b = (pim._ignoreCase)?(item2.toString().toLowerCase()):(item2.toString());
      return a.equals(b);
    }
    public int compare(X item1, X item2, PredictiveInputModel<X> pim) {
      String a = (pim._ignoreCase)?(item1.toString().toLowerCase()):(item1.toString());
      String b = (pim._ignoreCase)?(item2.toString().toLowerCase()):(item2.toString());
      return a.compareTo(b);
    }
    public X getLongestMatch(X item, List<X> items, PredictiveInputModel<X> pim) {
      if (items.size() > 0)  return items.get(0);
      else return null;
    }
    public String getSharedMaskExtension(List<X> items, PredictiveInputModel<X> pim) {
      return ""; // can't thing of a good way
    }
    public String getExtendedSharedMask(List<X> items, PredictiveInputModel<X> pim) {
      return pim._mask;
    }
    public String force(X item, String mask) { return item.toString(); }
  };
  
  /** Matching based on string regular expressions. */
  public static class RegExStrategy<X extends Comparable<? super X>> implements MatchingStrategy<X> {
    public String toString() { return "RegEx"; }
    public boolean isMatch(X item, PredictiveInputModel<X> pim) {
      String a = item.toString();

      try {
        Pattern p = Pattern.compile(pim._mask,
                                    (pim._ignoreCase)?(Pattern.CASE_INSENSITIVE):(0));
        Matcher m = p.matcher(a);
        return m.matches();
      }
      catch (PatternSyntaxException e) {
        return false;
      }
    }
    public boolean isPerfectMatch(X item, PredictiveInputModel<X> pim) {
      String a = (pim._ignoreCase)?(item.toString().toLowerCase()):(item.toString());
      String b = (pim._ignoreCase)?(pim._mask.toLowerCase()):(pim._mask);
      return a.equals(b);
    }
    public boolean equivalent(X item1, X item2, PredictiveInputModel<X> pim) {
      String a = (pim._ignoreCase)?(item1.toString().toLowerCase()):(item1.toString());
      String b = (pim._ignoreCase)?(item2.toString().toLowerCase()):(item2.toString());
      return a.equals(b);
    }
    public int compare(X item1, X item2, PredictiveInputModel<X> pim) {
      String a = (pim._ignoreCase)?(item1.toString().toLowerCase()):(item1.toString());
      String b = (pim._ignoreCase)?(item2.toString().toLowerCase()):(item2.toString());
      return a.compareTo(b);
    }
    public X getLongestMatch(X item, List<X> items, PredictiveInputModel<X> pim) {
      if (items.size() > 0)  return items.get(0); // can't thing of a good way
      else return null;
    }
    public String getSharedMaskExtension(List<X> items, PredictiveInputModel<X> pim) {
      return ""; // can't thing of a good way
    }
    public String getExtendedSharedMask(List<X> items, PredictiveInputModel<X> pim) {
      return pim._mask;
    }
    public String force(X item, String mask) { return item.toString(); }
  };
  
  /** Matching based on string prefix, supporting line numbers separated by :. */
  public static class PrefixLineNumStrategy<X extends Comparable<? super X>> implements MatchingStrategy<X> {
    public String toString() { return "Prefix"; }
    public boolean isMatch(X item, PredictiveInputModel<X> pim) {
      int posB = pim._mask.lastIndexOf(':');
      if (posB < 0) { posB = pim._mask.length(); }
      String mask = pim._mask.substring(0,posB);
      
      String a = (pim._ignoreCase)?(item.toString().toLowerCase()):(item.toString());
      String b = (pim._ignoreCase)?(mask.toLowerCase()):(mask);
      return a.startsWith(b);
    }
    public boolean isPerfectMatch(X item, PredictiveInputModel<X> pim) {
      int posB = pim._mask.lastIndexOf(':');
      if (posB < 0) { posB = pim._mask.length(); }
      String mask = pim._mask.substring(0,posB);
      
      String a = (pim._ignoreCase)?(item.toString().toLowerCase()):(item.toString());
      String b = (pim._ignoreCase)?(mask.toLowerCase()):(mask);
      return a.equals(b);
    }
    public boolean equivalent(X item1, X item2, PredictiveInputModel<X> pim) {
      int posA = item1.toString().lastIndexOf(':');
      if (posA < 0) { posA = item1.toString().length(); }
      String i1 = item1.toString().substring(0,posA);

      int posB = item2.toString().lastIndexOf(':');
      if (posB < 0) { posB = item2.toString().length(); }
      String i2 = item2.toString().substring(0,posB);
      
      String a = (pim._ignoreCase)?(i1.toLowerCase()):(i1);
      String b = (pim._ignoreCase)?(i2.toLowerCase()):(i2);
      return a.equals(b);
    }
    public int compare(X item1, X item2, PredictiveInputModel<X> pim) {
      int posA = item1.toString().lastIndexOf(':');
      if (posA < 0) { posA = item1.toString().length(); }
      String i1 = item1.toString().substring(0,posA);

      int posB = item2.toString().lastIndexOf(':');
      if (posB < 0) { posB = item2.toString().length(); }
      String i2 = item2.toString().substring(0,posB);
      
      String a = (pim._ignoreCase)?(i1.toLowerCase()):(i1);
      String b = (pim._ignoreCase)?(i2.toLowerCase()):(i2);
      return a.compareTo(b);
    }
    public X getLongestMatch(X item, List<X> items, PredictiveInputModel<X> pim) {
      X longestMatch = null;
      int matchLength = -1;
      for(X i: items) {
        int posA = i.toString().lastIndexOf(':');
        if (posA < 0) { posA = i.toString().length(); }
        String i1 = i.toString().substring(0,posA);
        
        int posB = item.toString().lastIndexOf(':');
        if (posB < 0) { posB = item.toString().length(); }
        String i2 = item.toString().substring(0,posB);
        
        String s = (pim._ignoreCase)?(i1.toLowerCase()):(i1);
        String t = (pim._ignoreCase)?(i2.toLowerCase()):(i2);
        int ml = 0;
        while((s.length() > ml) && (t.length() > ml) && (s.charAt(ml) == t.charAt(ml))) {
          ++ml;
        }
        if (ml>matchLength) {
          matchLength = ml;
          longestMatch = i;
        }
      }
      return longestMatch;
    }
    public String getSharedMaskExtension(List<X> items, PredictiveInputModel<X> pim) {
      StringBuilder res = new StringBuilder();
      String ext = "";
      if (items.size() == 0) {
        return ext;
      }
      
      int posB = pim._mask.lastIndexOf(':');
      if (posB < 0) { posB = pim._mask.length(); }
      String mask = pim._mask.substring(0,posB);
      
      boolean allMatching = true;
      int len = mask.length();
      while((allMatching) && (mask.length() + ext.length() < items.get(0).toString().length())) {
        char origCh = items.get(0).toString().charAt(mask.length()+ext.length());
        char ch = (pim._ignoreCase)?(Character.toLowerCase(origCh)):(origCh);
        allMatching = true;
        for (X i: items) {
          String a = (pim._ignoreCase)?(i.toString().toLowerCase()):(i.toString());
          if (a.charAt(len) != ch) {
            allMatching = false;
            break;
          }
        }
        if (allMatching) {
          ext = ext + ch;
          res.append(origCh);
          ++len;
        }
      }
      return res.toString();
    }
    public String getExtendedSharedMask(List<X> items, PredictiveInputModel<X> pim) {
      int pos = pim._mask.lastIndexOf(':');
      if (pos < 0) { 
        return pim._mask + getSharedMaskExtension(items, pim);
      }
      else {
        return pim._mask.substring(0,pos) + getSharedMaskExtension(items, pim) + pim._mask.substring(pos);
      }
    }
    public String force(X item, String mask) {
      int pos = mask.lastIndexOf(':');
      if (pos < 0) { 
        return item.toString();
      }
      else {
        return item.toString()+mask.substring(pos);
      }
    }
  };
  
  /** Matching based on string fragments, supporting line numbers. */
  public static class FragmentLineNumStrategy<X extends Comparable<? super X>> implements MatchingStrategy<X> {
    public String toString() { return "Fragments"; }
    public boolean isMatch(X item, PredictiveInputModel<X> pim) {
      int posB = pim._mask.lastIndexOf(':');
      if (posB < 0) { posB = pim._mask.length(); }
      String mask = pim._mask.substring(0,posB);
      
      String a = (pim._ignoreCase)?(item.toString().toLowerCase()):(item.toString());
      String b = (pim._ignoreCase)?(mask.toLowerCase()):(mask);

      java.util.StringTokenizer tok = new java.util.StringTokenizer(b);
      while(tok.hasMoreTokens()) {
        if (a.indexOf(tok.nextToken()) < 0) return false;
      }
      return true;
    }
    public boolean isPerfectMatch(X item, PredictiveInputModel<X> pim) {
      int posB = pim._mask.lastIndexOf(':');
      if (posB < 0) { posB = pim._mask.length(); }
      String mask = pim._mask.substring(0,posB);
      
      String a = (pim._ignoreCase)?(item.toString().toLowerCase()):(item.toString());
      String b = (pim._ignoreCase)?(mask.toLowerCase()):(mask);
      return a.equals(b);
    }
    public boolean equivalent(X item1, X item2, PredictiveInputModel<X> pim) {
      int posA = item1.toString().lastIndexOf(':');
      if (posA < 0) { posA = item1.toString().length(); }
      String i1 = item1.toString().substring(0,posA);

      int posB = item2.toString().lastIndexOf(':');
      if (posB < 0) { posB = item2.toString().length(); }
      String i2 = item2.toString().substring(0,posB);
      
      String a = (pim._ignoreCase)?(i1.toLowerCase()):(i1);
      String b = (pim._ignoreCase)?(i2.toLowerCase()):(i2);
      return a.equals(b);
    }
    public int compare(X item1, X item2, PredictiveInputModel<X> pim) {
      int posA = item1.toString().lastIndexOf(':');
      if (posA < 0) { posA = item1.toString().length(); }
      String i1 = item1.toString().substring(0,posA);

      int posB = item2.toString().lastIndexOf(':');
      if (posB < 0) { posB = item2.toString().length(); }
      String i2 = item2.toString().substring(0,posB);
      
      String a = (pim._ignoreCase)?(i1.toLowerCase()):(i1);
      String b = (pim._ignoreCase)?(i2.toLowerCase()):(i2);
      return a.compareTo(b);
    }
    public X getLongestMatch(X item, List<X> items, PredictiveInputModel<X> pim) {
      if (items.size() > 0)  return items.get(0);
      else return null;
    }
    public String getSharedMaskExtension(List<X> items, PredictiveInputModel<X> pim) {
      return ""; // can't thing of a good way
    }
    public String getExtendedSharedMask(List<X> items, PredictiveInputModel<X> pim) {
      return pim._mask;
    }
    public String force(X item, String mask) {
      int pos = mask.lastIndexOf(':');
      if (pos < 0) { 
        return item.toString();
      }
      else {
        return item.toString()+mask.substring(pos);
      }
    }
  };
  
  /** Matching based on string regular expressions, supporting line numbers. */
  public static class RegExLineNumStrategy<X extends Comparable<? super X>> implements MatchingStrategy<X> {
    public String toString() { return "RegEx"; }
    public boolean isMatch(X item, PredictiveInputModel<X> pim) {
      int posB = pim._mask.lastIndexOf(':');
      if (posB < 0) { posB = pim._mask.length(); }
      String mask = pim._mask.substring(0,posB);
      
      String a = item.toString();

      try {
        Pattern p = Pattern.compile(mask,
                                    (pim._ignoreCase)?(Pattern.CASE_INSENSITIVE):(0));
        Matcher m = p.matcher(a);
        return m.matches();
      }
      catch (PatternSyntaxException e) {
        return false;
      }
    }
    public boolean isPerfectMatch(X item, PredictiveInputModel<X> pim) {
      int posB = pim._mask.lastIndexOf(':');
      if (posB < 0) { posB = pim._mask.length(); }
      String mask = pim._mask.substring(0,posB);
      
      String a = (pim._ignoreCase)?item.toString().toLowerCase():item.toString();
      return a.equals((pim._ignoreCase)?mask.toLowerCase():mask);
    }
    public boolean equivalent(X item1, X item2, PredictiveInputModel<X> pim) {
      int posA = item1.toString().lastIndexOf(':');
      if (posA < 0) { posA = item1.toString().length(); }
      String i1 = item1.toString().substring(0,posA);

      int posB = item2.toString().lastIndexOf(':');
      if (posB < 0) { posB = item2.toString().length(); }
      String i2 = item2.toString().substring(0,posB);
      
      String a = (pim._ignoreCase)?(i1.toLowerCase()):(i1);
      String b = (pim._ignoreCase)?(i2.toLowerCase()):(i2);
      return a.equals(b);
    }
    public int compare(X item1, X item2, PredictiveInputModel<X> pim) {
      int posA = item1.toString().lastIndexOf(':');
      if (posA < 0) { posA = item1.toString().length(); }
      String i1 = item1.toString().substring(0,posA);

      int posB = item2.toString().lastIndexOf(':');
      if (posB < 0) { posB = item2.toString().length(); }
      String i2 = item2.toString().substring(0,posB);
      
      String a = (pim._ignoreCase)?(i1.toLowerCase()):(i1);
      String b = (pim._ignoreCase)?(i2.toLowerCase()):(i2);
      return a.compareTo(b);
    }
    public X getLongestMatch(X item, List<X> items, PredictiveInputModel<X> pim) {
      if (items.size() > 0)  return items.get(0); // can't thing of a good way
      else return null;
    }
    public String getSharedMaskExtension(List<X> items, PredictiveInputModel<X> pim) {
      return ""; // can't thing of a good way
    }
    public String getExtendedSharedMask(List<X> items, PredictiveInputModel<X> pim) {
      return pim._mask;
    }
    public String force(X item, String mask) {
      int pos = mask.lastIndexOf(':');
      if (pos < 0) { 
        return item.toString();
      }
      else {
        return item.toString()+mask.substring(pos);
      }
    }
  };
  
  /** Array of items. */
  private volatile ArrayList<T> _items = new ArrayList<T>();

  /** Index of currently selected full string. */
  private volatile int _index = 0;

  /** Array of matching items. */
  private final ArrayList<T> _matchingItems = new ArrayList<T>();

  /** Currently entered mask. */
  private volatile String _mask = "";
  
  /** True if case should be ignored. */
  private volatile boolean _ignoreCase = false;
  
  /** Matching strategy. */
  private volatile MatchingStrategy<T> _strategy;

  /** Create a new predictive input model.
    * @param ignoreCase true if case should be ignored
    * @param pim other predictive input model
    */
  public PredictiveInputModel(boolean ignoreCase, PredictiveInputModel<T> pim) {
    this(ignoreCase, pim._strategy, pim._items);
    setMask(pim.getMask());
  }

  /** Create a new predictive input model.
    * @param ignoreCase true if case should be ignored
    * @param strategy matching strategy to use
    * @param items list of items
    */
  public PredictiveInputModel(boolean ignoreCase, MatchingStrategy<T> strategy, Collection<T> items) {
    _ignoreCase = ignoreCase;
    _strategy = strategy;
    setItems(items);
  }

  /** Create a new predictive input model.
   * @param ignoreCase true if case should be ignored
   * @param strategy matching strategy to use
   * @param items varargs/array of items
   */
  public PredictiveInputModel(boolean ignoreCase, MatchingStrategy<T> strategy, T... items) {
    _ignoreCase = ignoreCase;
    _strategy = strategy;
    setItems(items);
  }

  /** Sets the strategy
   */
  public void setStrategy(MatchingStrategy<T> strategy) {
    _strategy = strategy;
    updateMatchingStrings(_items);
  }

  /** Returns a copy of the list of items.
   * @return list of items
   */
  public List<T> getItems() {
    return new ArrayList<T>(_items);
  }
  
  /** Sets the list.
   * @param items list of items
   */
  public void setItems(Collection<T> items) {
    _items = new ArrayList<T>(items);
    Collections.sort(_items);
    updateMatchingStrings(_items);
  }

  /** Sets the list
    * @param items varargs/array of items
    */
  public void setItems(T... items) {
    _items = new ArrayList<T>(items.length);
    for(T s: items) _items.add(s);
    Collections.sort(_items);
    updateMatchingStrings(_items);
  }

  /** Sets the list.
    * @param pim other predictive input model
    */
  public void setItems(PredictiveInputModel<T> pim) { setItems(pim._items); }  

  /** Return the current mask.
    * @return current mask
    */
  public String getMask() { return _mask; }

  /** Set the current mask.
    * @param mask new mask
    */
  public void setMask(String mask) {
    _mask = mask;
    updateMatchingStrings(_items);
  }

  /** Helper function that does indexOf with ignoreCase option.
    * @param l list
    * @param item item for which the index should be retrieved
    * @return index of item in list, or -1 if not found
    */
  private int indexOf(ArrayList<T> l, T item) {
    int index = 0;
    for (T i: l) {
      if (_strategy.equivalent(item, i, this)) return index;
      ++index;
    }
    return -1;
  }
  
  /** Update the list of matching strings and current index.
    * @param items list of items to base the matching on
    */
  private void updateMatchingStrings(ArrayList<T> items) {
    items = new ArrayList<T>(items); // create a new copy, otherwise we might be clearing the list in the next line
    _matchingItems.clear();
    for(T s: items) {
      if (_strategy.isMatch(s, this)) _matchingItems.add(s);
    }
    if (_items.size() > 0) {
      for(int i = 0; i < _items.size(); ++i) {
        if (_strategy.isPerfectMatch(_items.get(i), this)) {
          _index = i;
          break;
        }
      }
      setCurrentItem(_items.get(_index));
    }
    else _index = 0;
  }

  /** Get currently selected item.
    * @return currently selected item
    */
  public T getCurrentItem() {
    if (_items.size() > 0) return _items.get(_index);
    else return null;
  }

  /** Set currently selected item. Will select the item, or if the item does not match the mask, an item as closely 
    * preceding as possible.
    * @param item currently selected item
    */
  public void setCurrentItem(T item) {
    if (_items.size() == 0) {
      _index = 0;
      return;
    }
    boolean found = false;
    int index = indexOf(_items, item);
    if (index < 0) {
      // not in list of items, pick first item
      pickClosestMatch(item);
    }
    else {
      for (int i=index; i < _items.size(); ++i) {
        if (0 <= indexOf(_matchingItems, _items.get(i))) {
          _index = i;
          found = true;
          break;
        }
      }
      if (!found) {
        pickClosestMatch(item);
      }
    }
  }

  /** Select as current item the item in the list of current matches that lexicographically precedes it most closely.
    * @param item item for witch to find the closest match
    */
  private void pickClosestMatch(T item) {
    if (_matchingItems.size() > 0) {
      // pick item that lexicographically follows
      T follows = _matchingItems.get(0);
      for (T i: _matchingItems) {
        if (_strategy.compare(item, i, this) < 0) {
          break;
        }
        follows = i;
      }
      _index = indexOf(_items, follows);
    }
    else {
      _index = indexOf(_items, _strategy.getLongestMatch(item, _items, this));
    }
  }

  /** Get matching items.
   * @return list of matching items
   */
  public List<T> getMatchingItems() {
    return new ArrayList<T>(_matchingItems);
  }

  /** Returns the shared mask extension.
   * The shared mask extension is the string that can be added to the mask such that the list of
   * matching strings does not change.
   * @return shared mask extension
   */
  public String getSharedMaskExtension() {
    return _strategy.getSharedMaskExtension(_matchingItems, this);
  }

  /** Extends the mask. This operation can only narrow the list of matching strings and is thus faster than
   * setting the mask.
   * @param extension string to append to mask
   */
  public void extendMask(String extension) {
    _mask = _mask + extension;
    updateMatchingStrings(_matchingItems);
  }
  

  /** Extends the mask by the shared string.
   */
  public void extendSharedMask() {
    _mask = _strategy.getExtendedSharedMask(_matchingItems, this);
    updateMatchingStrings(_matchingItems);
  }
}
