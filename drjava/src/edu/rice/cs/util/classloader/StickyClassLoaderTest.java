package edu.rice.cs.util.classloader;

import junit.framework.*;
import java.io.*;

/**
 * Test cases for {@link StickyClassLoader}.
 *
 * @version $Id$
 */
public class StickyClassLoaderTest extends TestCase {
  private final String myName = getClass().getName();
  private final ClassLoader myLoader = getClass().getClassLoader();

  /**
   * Constructor.
   * @param String name
   */
  public StickyClassLoaderTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return new TestSuite(StickyClassLoaderTest.class);
  }

  /**
   * Make sure getClass().getClassLoader() sticks, regardless of where
   * the class data came from.
   */
  public void testLoaderSticks() throws Throwable {
    StickyClassLoader loader = new StickyClassLoader(myLoader, myLoader);

    Class c = loader.loadClass(myName);
    assertEquals("getClassLoader()", loader, c.getClassLoader());
    assertEquals("getName()", myName, c.getName());
  }

  /**
   * Make sure it works even for java.* classes.
   */
  public void testLoaderUsesSystemForJavaClasses() throws Throwable {
    StickyClassLoader loader = new StickyClassLoader(myLoader, myLoader);

    Class c = loader.loadClass("java.lang.Object");
    assertEquals("java.lang.Object", c.getName());
  }

  /**
   * Make sure getClass().getClassLoader() does not stick if the class
   * was on the useOldLoader list.
   */
  public void testLoaderRespectsOldList() throws Throwable {
    StickyClassLoader loader = new StickyClassLoader(myLoader,
                                                     myLoader,
                                                     new String[] { myName });

    Class c = loader.loadClass(myName);
    assertEquals("getClassLoader()", myLoader, c.getClassLoader());
    assertEquals("getName()", myName, c.getName());
  }

  /**
   * Make sure that if we load A through sticky loader, and A requires B 
   * to be loaded, B is also loaded through sticky loader.
   * We load the BMaker interface through the old loader so we can
   * cast to that interface.
   */
  public void testLoaderSticksTransitively() throws Throwable {
    String[] names = { myName + "$BMaker" };

    StickyClassLoader loader = new StickyClassLoader(myLoader, myLoader, names);
    Class c = loader.loadClass(myName + "$A");
    assertEquals("getClassLoader()", loader, c.getClassLoader());

    Object aObj = c.newInstance();
    BMaker aCasted = (BMaker) aObj;

    Object b = aCasted.makeB();

    assertEquals("getClass().getName()",
                 myName + "$A$B",
                 b.getClass().getName());

    assertEquals("getClass().getClassLoader()",
                 loader,
                 b.getClass().getClassLoader());
  }

  public interface BMaker {
    public Object makeB();
  }

  public static class A implements BMaker {
    private static class B {}

    public Object makeB() { return new B(); }
  }
}
