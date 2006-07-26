package edu.rice.cs.plt.iter;

/**
 * <p>Allows size calculations on {@code Iterable}s.  Implementing classes must
 * be able to calculate their size; ideally, this calculation should be done
 * in roughly constant time.  {@link IterUtil#sizeOf} uses this interface to
 * optimize size calculations.</p>
 * 
 * <p>In an ideal design, {@code java.util.Collection} would implement a {@code SizedIterable}
 * API class, and applications that required this behavior could be defined in terms of 
 * {@code SizedIterable}s instead of {@code Iterable}s.  However, since the Java APIs can't be 
 * modified and are too valuable to abandon, the {@code sizeOf} method provides a workaround that, 
 * through casting, calculates the size appropriately.  An alternative design would allow 
 * collections as components of {@code SizedIterable}s only by manually wrapping them in
 * a bridge class (see {@link IterUtil#asSizedIterable(java.util.Collection)}).</p>
 */
public interface SizedIterable<T> extends Iterable<T> {
  /**
   * @return  The number of elements in the iterable; {@code next()} may be safely invoked on
   *           the iterator exactly this number of times
   */
  public int size();
  
  /**
   * @return  {@code true} iff this iterable has a fixed size; this is the case if the iterable
   *          is immutable, or if changes can only replace values, not remove or add them
   */
  public boolean isFixed();
}
