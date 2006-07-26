package edu.rice.cs.plt.iter;

/**
 * An enumeration of all permutations of the given list.  The size of the enumeration, where 
 * the original list has size n, is n! (and is thus guaranteed to be >= 1).  The order of the 
 * results is "lexicographical" where each element of the original list is taken to 
 * lexicographically precede all of its successors.  (Thus, the original list is
 * the first to be returned, and a reversed list is the last.)  Of course, due to the factorial
 * complexity of enumerating all permutations, this class is probably not suitable
 * for applications in which n is unbounded (or just intractably large).
 * 
 * @param T  The element type of the permuted lists; note that the iterator returns
 *           {@code Iterable<T>}s, not {@code T}s.
 */
public class PermutationIterable<T> extends AbstractIterable<Iterable<T>> 
                                    implements SizedIterable<Iterable<T>> {
  
  private final Iterable<? extends T> _original;
  
  public PermutationIterable(Iterable<? extends T> original) { _original = original; }
  public PermutationIterator<T> iterator() { return new PermutationIterator<T>(_original); }

  public int size() {
    int n = IterUtil.sizeOf(_original);
    int result = 1;
    for (int i = 2; i < n; i++) { result *= i; }
    return result;
  }
  
  public boolean isFixed() { return IterUtil.isFixed(_original); }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> PermutationIterable<T> make(Iterable<? extends T> original) {
    return new PermutationIterable<T>(original);
  }
  
  /**
   * Create a {@code PermutationIterable} and wrap it in a {@code SnapshotIterable}, forcing
   * immediate evaluation of the permutations.
   */
  public static <T> SnapshotIterable<Iterable<T>> makeSnapshot(Iterable<? extends T> original) {
    return new SnapshotIterable<Iterable<T>>(new PermutationIterable<T>(original));
  }
  
}
