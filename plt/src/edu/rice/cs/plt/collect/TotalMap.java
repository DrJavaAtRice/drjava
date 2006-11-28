package edu.rice.cs.plt.collect;

import java.util.Map;
import java.util.HashMap;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.LambdaUtil;

/**
 * <p>A map that is defined for all values in the domain {@code K}.  This class is similar
 * to the {@link java.util.Map} interface, but there are fundamental differences that make
 * it difficult to implement the interface.  For example, it's impossible to produce a
 * {@code valueSet} for an arbitrary {@code TotalMap}.  (It's even impossible to safely define
 * the {@code Map.get} method, since it is defined for all {@code Object}s, and the
 * lambda implementing the total map is only defined in terms of {@code K}.)<p>
 * 
 * <p>The total coverage of the domain is achieved by defining the map in terms of a
 * {@link Lambda}.  Additionally, specific values can be overridden with specific results
 * via the {@link #override} method; these mappings can be undone via {@link #revert}.
 * Optionally, the results of {@link #get} queries that call the lambda can be automatically 
 * cached with the other overriding mappings.</p>
 */
public class TotalMap<K, V> {
  
  private final Lambda<? super K, ? extends V> _lambda;
  private final Map<? super K, V> _overrides;
  private final boolean _cache;
  
  /** Create a {@code TotalMap} whose value is {@code null} for all inputs.  Caching is disabled. */
  public TotalMap() {
    this(LambdaUtil.<V>nullLambda(), new HashMap<K, V>(), false);
  }
  
  /** Create a {@code TotalMap} defined in terms of {@code lambda}.  Caching is disabled. */
  public TotalMap(Lambda<? super K, ? extends V> lambda) { 
    this(lambda, new HashMap<K, V>(), false);
  }
  
  /** 
   * Create a {@code TotalMap} defined in terms of {@code lambda}.  Caching may be enabled
   * or disabled.
   */
  public TotalMap(Lambda<? super K, ? extends V> lambda, boolean cache) {
    this (lambda, new HashMap<K, V>(), cache);
  }
  
  /** 
   * Create a {@code TotalMap} whose value is {@code null} for all inputs, with the exception
   * of the given overriding assignments.  The given map is stored (not copied), so subsequent
   * external changes will be reflected here (and vice versa).  Cahcing is disabled.
   */
  public TotalMap(Map<? super K, V> overrides) {
    this(LambdaUtil.<V>nullLambda(), overrides, false);
  }
  
  /**
   * Create a {@code TotalMap} defined in terms of {@code lambda}, with the exception of
   * the given overriding assignments.  The given map is stored (not copied), so subsequent
   * external changes will be reflected here (and vice versa).  Caching is disabled.
   */
  public TotalMap(Lambda<? super K, ? extends V> lambda, Map<? super K, V> overrides) {
    this (lambda, overrides, false);
  }
  
  /**
   * Create a {@code TotalMap} defined in terms of {@code lambda}, with the exception of
   * the given overriding assignments.  The given map is stored (not copied), so subsequent
   * external changes will be reflected here (and vice versa).  Caching is enabled or disabled
   * as specified.
   */
  public TotalMap(Lambda<? super K, ? extends V> lambda, Map<? super K, V> overrides,
                  boolean cache) {
    _lambda = lambda;
    _overrides = overrides;
    _cache = cache;
  }

  /** @return  The value corresponding to {@code key} */
  public V get(K key) {
    if (_overrides.containsKey(key)) { return _overrides.get(key); }
    else { 
      V result = _lambda.value(key);
      if (_cache) { _overrides.put(key, result); }
      return result;
    }
  }
  
  /**
   * Add the given mapping to the override map.  Subsequent invocations of {@code get(key)}
   * will immediately return {@code value}.
   */
  public void override(K key, V value) { _overrides.put(key, value); }
  
  /** @return  {@code true} iff the override map contains the given key */
  public boolean containsOverride(Object key) { return _overrides.containsKey(key); }
  
  /**
   * Remove the given key from the override map.  Subsequent invocations of {@code get(key)}
   * will invoke the lambda.
   * @return  The value overridden by the given key, or {@code null} if the mapping
   *          did not exist.
   */
  public V revert(K key) { return _overrides.remove(key); }
  
  /** Add all the given mappings to the override map */
  public void overrideAll(Map<? extends K, ? extends V> map) {
    _overrides.putAll(map);
  }
  
  /** 
   * Remove all mappings from the override map.  Subsequent invocations of {@code get} will
   * invoke the lambda.
   */
  public void revertAll() { _overrides.clear(); }
  
  /** Get the size of the cache -- the number of overridden values */
  public int cacheSize() { return _overrides.size(); }
    
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <K, V> TotalMap<K, V> make() { return new TotalMap<K, V>(); }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <K, V> TotalMap<K, V> make(Lambda<? super K, ? extends V> lambda) {
    return new TotalMap<K, V>(lambda);
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <K, V> TotalMap<K, V> make(Lambda<? super K, ? extends V> lambda, boolean cache) {
    return new TotalMap<K, V>(lambda, cache);
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <K, V> TotalMap<K, V> make(Map<? super K, V> overrides) {
    return new TotalMap<K, V>(overrides);
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <K, V> TotalMap<K, V> make(Lambda<? super K, ? extends V> lambda, 
                                    Map<? super K, V> overrides) {
    return new TotalMap<K, V>(lambda, overrides);
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <K, V> TotalMap<K, V> make(Lambda<? super K, ? extends V> lambda, 
                                    Map<? super K, V> overrides, boolean cache) {
    return new TotalMap<K, V>(lambda, overrides, cache);
  }
  
}
