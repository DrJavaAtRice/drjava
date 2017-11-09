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

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.lambda.Predicate2;

/**
 * The complement of a relation {@code excluded} in a domain {@code domain} (alternatively,
 * {@code domain - excluded}), constructed lazily and updated dynamically.
 */
public class ComplementRelation<T1, T2> extends FilteredRelation<T1, T2> {
  
  private final Relation<? super T1, ? super T2> _excluded;
  
  public ComplementRelation(Relation<T1, T2> domain, Relation<? super T1, ? super T2> excluded) {
    super(domain, LambdaUtil.negate((Predicate2<? super T1, ? super T2>)excluded));
    _excluded = excluded;
  }
  
  @Override public PredicateSet<T2> matchFirst(T1 first) {
    return new ComplementSet<T2>(_rel.matchFirst(first), _excluded.matchFirst(first));
  }
  
  @Override public PredicateSet<T1> matchSecond(T2 second) {
    return new ComplementSet<T1>(_rel.matchSecond(second), _excluded.matchSecond(second));
  }
  
  public boolean isInfinite() {
    return IterUtil.isInfinite(_rel) && !_excluded.isInfinite();
  }
  
  public boolean hasFixedSize() {
    return IterUtil.hasFixedSize(_rel) && _excluded.hasFixedSize();
  }
  
  public boolean isStatic() {
    return IterUtil.isStatic(_rel) && _excluded.isStatic();
  }
  
  @Override public boolean isEmpty() {
    if (_rel.isEmpty()) { return true; }
    else if (_excluded.isEmpty()) { return false; }
    else if (_rel == _excluded) { return true; }
    else { return _excluded.containsAll(_rel); }
  }
  
}
