package edu.rice.cs.drjava.model.compiler;

import junit.framework.*;
import edu.rice.cs.util.classloader.LimitingClassLoader;

/**
 * Test cases for {@link CompilerRegistry}.
 * Here we test that the compiler registry correctly finds
 * available compilers.
 *
 * @version $Id$
 */
public class CompilerRegistryTest extends TestCase {
  private static final CompilerRegistry _registry = CompilerRegistry.ONLY;
  private static final String[] _defaultCompilers
    = CompilerRegistry.DEFAULT_COMPILERS;

  /**
   * Stores the old state of {@link CompilerRegistry#getBaseClassLoader},
   * so it can be reset later.
   */
  private ClassLoader _oldBaseLoader;

  /**
   * Constructor.
   * @param  String name
   */
  public CompilerRegistryTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return new TestSuite(CompilerRegistryTest.class);
  }

  /** Test setup method, which saves the old base class loader. */
  public void setUp() {
    _oldBaseLoader = _registry.getBaseClassLoader();
    _registry.setActiveCompiler(NoCompilerAvailable.ONLY);
  }

  /** Test teardown method, which restores the old base class loader. */
  public void tearDown() {
    _registry.setBaseClassLoader(_oldBaseLoader);
  }

  /**
   * Test that the default compilers are available and what we expect.
   * This requires the environment (CLASSPATH) to have these compilers
   * available. This is OK though, since the build environment needs them!
   */
  public void testExpectedDefaultCompilers() {
    CompilerInterface[] compilers = _registry.getAvailableCompilers();
    
    assertEquals("Number of available compilers vs. number of default compilers",
                 _defaultCompilers.length,
                 compilers.length);
    
    for (int i = 0; i < compilers.length; i++) {
      assertEquals("Name of available compiler #" + i + " is the same as the " +
                     "name of the corresponding default compiler",
                   _defaultCompilers[i],
                   compilers[i].getClass().getName());
    }
  }

  /**
   * Tests that list of available compilers effectively is restricted
   * when the class is not available.
   * Here this is done by limiting the available compilers one at a time.
   */
  public void testLimitOneByOne() {
    for (int i = 0; i < _defaultCompilers.length; i++) {
      CompilerInterface[] compilers = _getCompilersAfterDisablingOne(i);
      // That method includes all the tests we need!
    }
  }

  /**
   * Tests that list of available compilers effectively is restricted
   * when all default compilers are not available.
   */
  public void testLimitAllAtOnce() {
    LimitingClassLoader loader = new LimitingClassLoader(_oldBaseLoader);
    _registry.setBaseClassLoader(loader);

    for (int i = 0; i < _defaultCompilers.length; i++) {
      loader.addToRestrictedList(_defaultCompilers[i]);
    }

    CompilerInterface[] compilers = _registry.getAvailableCompilers();
    assertEquals("Number of available compilers should be 1 " +
                   "because all real compilers are restricted.",
                 1,
                 compilers.length);
    
    assertEquals("Only available compiler should be NoCompilerAvailable.ONLY",
                 NoCompilerAvailable.ONLY,
                 compilers[0]);

    assertEquals("Active compiler",
                 NoCompilerAvailable.ONLY,
                 _registry.getActiveCompiler());
  }

  /**
   * Tests that {@link CompilerRegistry#setActiveCompiler} and
   * {@link CompilerRegistry#getActiveCompiler} work.
   */
  public void testActiveCompilerAllAvailable() {
    CompilerInterface[] compilers = _registry.getAvailableCompilers();

    assertEquals("active compiler before any setActive",
                 compilers[0],
                 _registry.getActiveCompiler());
    
    for (int i = 0; i < compilers.length; i++) {
      // TODO: deal with the problem that sometimes not all compilers avail!
      //if (compilers[i].isAvailable()) {
        _registry.setActiveCompiler(compilers[i]);
        assertEquals("active compiler after setActive",
                     compilers[i],
                     _registry.getActiveCompiler());
      //}
    }
  }

  /**
   * Returns the list of available compilers after disabling one of them.
   * This method includes checks for the correctness of the list
   * after disabling one.
   *
   * @param i Index of default compiler to disable.
   */
  private CompilerInterface[] _getCompilersAfterDisablingOne(int i) {
    return _getCompilersAfterDisablingSome(new int[] { i });
  }

  /**
   * Returns the list of available compilers after disabling some of them.
   * This method includes checks for the correctness of the list
   * after disabling them.
   * 
   * @param indices Array of ints signifying which of the default compilers
   *                to disable.
   */
  private CompilerInterface[] _getCompilersAfterDisablingSome(int[] indices) {
    LimitingClassLoader loader = new LimitingClassLoader(_oldBaseLoader);
    _registry.setBaseClassLoader(loader);

    for (int i = 0; i < indices.length; i++) {
      loader.addToRestrictedList(_defaultCompilers[indices[i]]);
    }

    CompilerInterface[] compilers = _registry.getAvailableCompilers();
    assertEquals("Number of available compilers",
                 _defaultCompilers.length - indices.length,
                 compilers.length);

    int indicesIndex = 0;

    for (int j = 0; j < _defaultCompilers.length; j++) {
      if ((indicesIndex < indices.length) && (j == indices[indicesIndex])) {
        // this is an index to skip.
        indicesIndex++;
        continue;
      }

      // Now indicesIndex is at the number of indices to skip!
      int indexInAvailable = j - indicesIndex;

      assertEquals("Name of available compiler #" + indexInAvailable,
                   _defaultCompilers[j],
                   compilers[indexInAvailable].getClass().getName());
    }

    return compilers;
  }
}
