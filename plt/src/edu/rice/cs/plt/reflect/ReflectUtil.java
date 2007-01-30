package edu.rice.cs.plt.reflect;

import java.util.StringTokenizer;
import java.util.NoSuchElementException;

public final class ReflectUtil {
  
  /** Prevents instance creation */
  private ReflectUtil() {}
  
  /**
   * Produce the simple name of the given class, as specified by {@link Class#getSimpleName},
   * with an improved scheme for anonymous classes.  The simple name of a class is generally
   * the unqualified name used to declare it.  Arrays evaluate to the simple name of their
   * element type, followed by a pair of brackets.  Anonymous classes, rather than evaluating to an 
   * empty string, produce something like "anonymous Foo" (where Foo is the supertype).
   */
  public static String simpleNameOf(Class<?> c) {
    if (c.isArray()) { return simpleNameOf(c.getComponentType()) + "[]"; }
    else if (isAnonymousClass(c)) {
      if (c.getInterfaces().length > 0) { return "anonymous " + simpleNameOf(c.getInterfaces()[0]); }
      else { return "anonymous " + simpleNameOf(c.getSuperclass()); }
    }
    else {
      String fullName = c.getName();
      return fullName.substring(fullName.lastIndexOf('.') + 1);
    }
  }
  
  /** An implementation of {@link Class#isAnonymousClass}, which is unavailable prior to Java 5.0 */
  public static boolean isAnonymousClass(Class<?> c) {
    String name = c.getName();
    String nameEnd = name.substring(name.lastIndexOf('$') + 1); // index is -1 if there is none
    for (int i = 0; i < nameEnd.length(); i++) {
      if (Character.isJavaIdentifierStart(nameEnd.charAt(i))) { return false; }
    }
    return true;
  }
  

  private static final int JAVA_VERSION = parseJavaVersion(System.getProperty("java.class.version"));
  
  /**
   * Convert the given class version string to a number.  The major version is multiplied by 100 and
   * added to the minor version.  If there is an error in parsing, the result is 0.
   */
  private static int parseJavaVersion(String text) {
    StringTokenizer t = new StringTokenizer(text, ".");
    try {
      int result = Integer.parseInt(t.nextToken()) * 100;
      result += Integer.parseInt(t.nextToken());
      return result;
    }
    catch (NoSuchElementException e) { return 0; }
    catch (NumberFormatException e) { return 0; }
  }
  
  /**
   * Tests whether the given version of Java is supported in the currently-running JVM.  Each version 
   * supports all of its predecessors.  Ideally, a {@code true} result means that all APIs associated with  
   * that version are available at runtime.  However, this method does not attempt to (and cannot, in general) 
   * guarantee that the boot class path or Java installation have not been modified to only support certain 
   * API classes, or that certain classes and methods available in the given version have not been removed 
   * (after a few cycles of deprecation, for example) from a newer version's APIs.
   */
  public static boolean isSupported(Version v) {
    return v.supportedUnder(JAVA_VERSION);
  }
  
  /**
   * Tests whether the given version of Java is the version of the currently-running JVM.  Ideally, a 
   * {@code true} result means that all APIs associated with that version are available at runtime.  However, 
   * this method does not attempt to (and cannot, in general) guarantee that the boot class path or Java 
   * installation have not been modified to only support certain API classes.
   */
  public static boolean isCurrent(Version v) {
    return v.supportedUnder(JAVA_VERSION) && v.newUnder(JAVA_VERSION);
  }
  
  /** A representation of a major Java version.  Used with {@link #isSupported} and {@link #isCurrent}. */
  public enum Version {
    JAVA_11 {
      protected boolean supportedUnder(int num) { return num >= 4503; }
      protected boolean newUnder(int num) { return num < 4600; }
    },
    JAVA_12 {
      protected boolean supportedUnder(int num) { return num >= 4600; }
      protected boolean newUnder(int num) { return num < 4700; }
    },
    JAVA_13 {
      protected boolean supportedUnder(int num) { return num >= 4700; }
      protected boolean newUnder(int num) { return num < 4800; }
    },
    JAVA_14 {
      protected boolean supportedUnder(int num) { return num >= 4800; }
      protected boolean newUnder(int num) { return num < 4900; }
    },
    JAVA_5 {
      protected boolean supportedUnder(int num) { return num >= 4900; }
      protected boolean newUnder(int num) { return num < 5000; }
    },
    JAVA_6 {
      protected boolean supportedUnder(int num) { return num >= 5000; }
      protected boolean newUnder(int num) { return num < 5100; }
    };
    
    protected abstract boolean supportedUnder(int num);
    protected abstract boolean newUnder(int num);
  }
  
}
