/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2008 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.collect;

import java.util.Set;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Collection;
import edu.rice.cs.plt.iter.FilteredIterator;
import edu.rice.cs.plt.lambda.Predicate;
import edu.rice.cs.plt.lambda.LambdaUtil;

/**
 * The complement of a set {@code excluded} in a domain {@code domain} (alternatively,
 * {@code domain - excluded}), constructed lazily and updated dynamically.
 */
public class ComplementSet<E> extends AbstractSet<E> {
  
  private final Set<? extends E> _domain;
  private final Set<?> _excluded;
  
  public ComplementSet(Set<? extends E> domain, Set<?> excluded) {
    _domain = domain;
    _excluded = excluded;
  }
  
  /** Traversing is linear in the size of {@code domain}. */
  public Iterator<E> iterator() {
    Predicate<Object> filter = LambdaUtil.negate(CollectUtil.containsPredicate(_excluded));
    return new FilteredIterator<E>(_domain.iterator(), filter);
  }
  
  /** Linear in the size of {@code domain}. */
  public int size() {
    int result = 0;
    for (E elt : this) { result++; }
    return result;
  }
  
  /** Linear in the size of {@code domain}. */
  public boolean isEmpty() {
    if (_domain.isEmpty()) { return true; }
    else if (_excluded.isEmpty()) { return false; }
    else if (_domain == _excluded) { return true; }
    else { return _excluded.containsAll(_domain); }
  }
  
  public boolean contains(Object o) {
    return _domain.contains(o) && !(_excluded.contains(o));
  }
  
  public boolean containsAll(Collection<?> objs) {
    if (_domain.containsAll(objs)) {
      for (Object obj : objs) {
        if (_excluded.contains(obj)) { return false; }
      }
      return true;
    }
    else { return false; }
  }
  
}
