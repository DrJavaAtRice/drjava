/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
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
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.LambdaUtil;

/**
 * The complement of a set {@code excluded} in a domain {@code domain} (alternatively,
 * {@code domain - excluded}), constructed lazily and updated dynamically.
 */
public class ComplementSet<E> extends FilteredSet<E> {
  
  private final PredicateSet<?> _excluded;
  
  public ComplementSet(Set<? extends E> domain, Set<?> excluded) {
    this(domain, CollectUtil.asPredicateSet(excluded));
  }
  
  private ComplementSet(Set<? extends E> domain, PredicateSet<?> excluded) {
    super(domain, LambdaUtil.negate(excluded));
    _excluded = excluded;
  }
  
  public boolean isInfinite() {
    return IterUtil.isInfinite(_set) && !_excluded.isInfinite();
  }
  
  public boolean hasFixedSize() {
    return IterUtil.hasFixedSize(_set) && _excluded.hasFixedSize();
  }
  
  public boolean isStatic() {
    return IterUtil.isStatic(_set) && _excluded.isStatic();
  }
  
  @Override public boolean isEmpty() {
    if (_set.isEmpty()) { return true; }
    else if (_excluded.isEmpty()) { return false; }
    else if (_set == _excluded) { return true; }
    else { return _excluded.containsAll(_set); }
  }
  
}
