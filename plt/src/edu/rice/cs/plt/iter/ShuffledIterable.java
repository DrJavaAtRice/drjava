package edu.rice.cs.plt.iter;

import java.util.Iterator;
import java.util.Random;
import edu.rice.cs.plt.collect.ExternallySortedSet;

/**
 * An iterable that randomly reorders the contents of some other iterable.  Like 
 * {@link SnapshotIterable}, the original list is read immediately, and subsequent 
 * changes will not be reflected here.
 */
public class ShuffledIterable<T> extends AbstractIterable<T> implements SizedIterable<T> {
  
  private ExternallySortedSet<T, Long> _shuffledVals;
  
  /** Create a ShuffledIterable with the default implementation of {@link Random} */
  public ShuffledIterable(Iterable<T> vals) {
    this(vals, new Random());
  }
  
  /** Create a ShuffledIterable with a custom {@link Random} object */
  public ShuffledIterable(Iterable<T> vals, Random randomizer) {
    _shuffledVals = new ExternallySortedSet<T, Long>();
    for (T val : vals) {
      // TODO: Handle collisions in a random way
      _shuffledVals.add(val, randomizer.nextLong());
    }
  }
  
  public Iterator<T> iterator() { return _shuffledVals.iterator(); }
  
  public int size() { return _shuffledVals.size(); }
  
  public boolean isFixed() { return false; }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ShuffledIterable<T> make(Iterable<T> vals) {
    return new ShuffledIterable<T>(vals);
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ShuffledIterable<T> make(Iterable<T> vals, Random randomizer) {
    return new ShuffledIterable<T>(vals, randomizer);
  }
  
}
