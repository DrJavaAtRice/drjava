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

package edu.rice.cs.drjava.ui.predictive;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 * Model class for predictive string input.
 */
public class PredictiveInputModel<T extends Comparable<? super T>> {
  /**
   * Strategy used for matching and mask extension.
   */
  public static interface MatchingStrategy<X extends Comparable<? super X>> {
    /**
     * Returns true if the item is a match.
     * @param item item to check
     * @param pim predictive input model
     * @return true if the item is a match
     */
    public boolean isMatch(X item, PredictiveInputModel<X> pim);
    
    /**
     * Returns true if the two items are equivalent under this matching strategy.
     * @param item1 first item
     * @param item2 second item
     * @param pim predictive input model
     * @return true if equivalent
     */
    public boolean equivalent(X item1, X item2, PredictiveInputModel<X> pim);
    
    /**
     * Compare the two items and return -1, 0, or 1 if item1 is less than, equal to, or greater than item2.
     * @param item1 first item
     * @param item2 second item
     * @param pim predictive input model
     * @return -1, 0, or 1
     */
    public int compare(X item1, X item2, PredictiveInputModel<X> pim);

    /**
     * Returns the item from the list that is the longest match.
     * @oaram item target item
     * @param items list with items
     * @param pim predictive input model
     * @return longest match
     */
    public X getLongestMatch(X item, List<X> items, PredictiveInputModel<X> pim);
    
    /**
     * Returns the shared mask extension for the list of items.
     * @param items items for which the mask extension should be generated
     * @param pim predictive input model
     * @return the shared mask extension
     */
    public String getSharedMaskExtension(List<X> items, PredictiveInputModel<X> pim);
  }
  
  /**
   * Matching based on string prefix.
   */
  public static class PrefixStrategy<X extends Comparable<? super X>> implements MatchingStrategy<X> {
    public String toString() { return "Prefix"; }
    public boolean isMatch(X item, PredictiveInputModel<X> pim) {
      String a = (pim._ignoreCase)?(item.toString().toLowerCase()):(item.toString());
      String b = (pim._ignoreCase)?(pim._mask.toLowerCase()):(pim._mask);
      return a.startsWith(b);
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
      X longestMatch = null;
      int matchLength = -1;
      for(X i: items) {
        String s = (pim._ignoreCase)?(i.toString().toLowerCase()):(i.toString());
        String t = (pim._ignoreCase)?(item.toString().toLowerCase()):(item.toString());
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
      String res = "";
      String ext = "";
      if (items.size() == 0) {
        return ext;
      }
      boolean allMatching = true;
      int len = pim._mask.length();
      while((allMatching) && (pim._mask.length() + ext.length() < items.get(0).toString().length())) {
        char origCh = items.get(0).toString().charAt(pim._mask.length()+ext.length());
        char ch = (pim._ignoreCase)?(Character.toLowerCase(origCh)):(origCh);
        allMatching = true;
        for (X i: items) {
          String a = (pim._ignoreCase)?(i.toString().toLowerCase()):(i.toString());
          if (a.charAt(len)!=ch) {
            allMatching = false;
            break;
          }
        }
        if (allMatching) {
          ext = ext + ch;
          res = res + origCh;
          ++len;
        }
      }
      return res;
    }
  };
  
  /**
   * Matching based on string fragments.
   */
  public static class FragmentStrategy<X extends Comparable<? super X>> implements MatchingStrategy<X> {
    public String toString() { return "Fragments"; }
    public boolean isMatch(X item, PredictiveInputModel<X> pim) {
      String a = (pim._ignoreCase)?(item.toString().toLowerCase()):(item.toString());
      String b = (pim._ignoreCase)?(pim._mask.toLowerCase()):(pim._mask);

      java.util.StringTokenizer tok = new java.util.StringTokenizer(b);
      while(tok.hasMoreTokens()) {
        if (a.indexOf(tok.nextToken())<0) {
          return false;
        }
      }
      return true;
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
      if (items.size()>0) {
        return items.get(0);
      }
      else {
        return null;
      }
    }
    public String getSharedMaskExtension(List<X> items, PredictiveInputModel<X> pim) {
      return ""; // can't thing of a good way
    }
  };
  
  /**
   * Matching based on string regular expressions.
   */
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
      if (items.size()>0) {
        return items.get(0); // can't thing of a good way
      }
      else {
        return null;
      }
    }
    public String getSharedMaskExtension(List<X> items, PredictiveInputModel<X> pim) {
      return ""; // can't thing of a good way
    }
  };
  
  /**
   * Array of items.
   */
  ArrayList<T> _items = new ArrayList<T>();

  /**
   * Index of currently selected full string.
   */
  int _index = 0;

  /**
   * Array of matching items.
   */
  ArrayList<T> _matchingItems = new ArrayList<T>();

  /**
   * Currently entered mask.
   */
  String _mask = "";
  
  /**
   * True if case should be ignored.
   */
  boolean _ignoreCase = false;
  
  /**
   * Matching strategy.
   */
  MatchingStrategy<T> _strategy;

  /**
   * Create a new predictive input model.
   * @param ignoreCase true if case should be ignored
   * @param pim other predictive input model
   */
  public PredictiveInputModel(boolean ignoreCase, PredictiveInputModel<T> pim) {
    this(ignoreCase, pim._strategy, pim._items);
    setMask(pim.getMask());
  }

  /**
   * Create a new predictive input model.
   * @param ignoreCase true if case should be ignored
   * @param strategy matching strategy to use
   * @param items list of items
   */
  public PredictiveInputModel(boolean ignoreCase, MatchingStrategy<T> strategy, List<T> items) {
    _ignoreCase = ignoreCase;
    _strategy = strategy;
    setList(items);
  }

  /**
   * Create a new predictive input model.
   * @param ignoreCase true if case should be ignored
   * @param strategy matching strategy to use
   * @param items varargs/array of items
   */
  public PredictiveInputModel(boolean ignoreCase, MatchingStrategy<T> strategy, T... items) {
    _ignoreCase = ignoreCase;
    _strategy = strategy;
    setList(items);
  }

  /**
   * Sets the strategy
   * @param items list of items
   */
  public void setStrategy(MatchingStrategy<T> strategy) {
    _strategy = strategy;
    updateMatchingStrings(_items);
  }

  /**
   * Sets the list.
   * @param items list of items
   */
  public void setList(List<T> items) {
    _items = new ArrayList<T>(items);
    Collections.sort(_items);
    updateMatchingStrings(_items);
  }

  /**
   * Sets the list
   * @param items varargs/array of items
   */
  public void setList(T... items) {
    _items = new ArrayList<T>(items.length);
    for(T s: items) {
      _items.add(s);
    }
    Collections.sort(_items);
    updateMatchingStrings(_items);
  }

  /**
   * Sets the list.
   * @param pim other predictive input model
   */
  public void setList(PredictiveInputModel<T> pim) {
    setList(pim._items);
  }  

  /**
   * Return the current mask.
   * @return current mask
   */
  public String getMask() {
    return _mask;
  }

  /**
   * Set the current mask.
   * @param mask new mask
   */
  public void setMask(String mask) {
    _mask = mask;
    updateMatchingStrings(_items);
  }

  /**
   * Helper function that does indexOf with ignoreCase option.
   * @param l list
   * @param item item for which the index should be retrieved
   * @return index of item in list, or -1 if not found
   */
  private int indexOf(ArrayList<T> l, T item) {
    int index = 0;
    for (T i: l) {
      if (_strategy.equivalent(item, i, this)) {
        return index;
      }
      ++index;
    }
    return -1;
  }
  
  /**
   * Update the list of matching strings and current index.
   * @param items list of items to base the matching on
   */
  private void updateMatchingStrings(ArrayList<T> items) {
    items = new ArrayList<T>(items); // create a new copy, otherwise we might be clearing the list in the next line
    _matchingItems.clear();
    for(T s: items) {
      if (_strategy.isMatch(s, this)) {
        _matchingItems.add(s);
      }
    }
    if (_items.size() > 0) {
      setCurrentItem(_items.get(_index));
    }
    else {
      _index = 0;
    }
  }

  /**
   * Get currently selected item.
   * @return currently selected item
   */
  public T getCurrentItem() {
    if (_items.size() > 0) {
      return _items.get(_index);
    }
    else {
      return null;
    }
  }

  /**
   * Set currently selected item.
   * Will select the item, or if the item does not match the mask, an item as closely preceding as possible.
   * @param item currently selected item
   */
  public void setCurrentItem(T item) {
    if (_items.size() == 0) {
      _index = 0;
      return;
    }
    boolean found = false;
    int index = indexOf(_items, item);
    if (index<0) {
      // not in list of items, pick first item
      pickClosestMatch(item);
    }
    else {
      for (int i=index; i<_items.size(); ++i) {
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

  /**
   * Select as current item the item in the list of current matches that lexicographically precedes it most closely.
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

  /**
   * Get matching items.
   * @return list of matching items
   */
  public List<T> getMatchingItems() {
    return new ArrayList<T>(_matchingItems);
  }

  /**
   * Returns the shared mask extension.
   * The shared mask extension is the string that can be added to the mask such that the list of
   * matching strings does not change.
   * @return shared mask extension
   */
  public String getSharedMaskExtension() {
    return _strategy.getSharedMaskExtension(_matchingItems, this);
  }

  /**
   * Extends the mask. This operation can only narrow the list of matching strings and is thus faster than
   * setting the mask.
   * @param extension string to append to mask
   */
  public void extendMask(String extension) {
    _mask = _mask + extension;
    updateMatchingStrings(_matchingItems);
  }
}
