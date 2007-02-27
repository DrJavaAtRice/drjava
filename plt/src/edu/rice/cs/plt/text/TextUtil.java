package edu.rice.cs.plt.text;

import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.recur.RecurUtil;

public final class TextUtil {
  
  public static final String NEWLINE = System.getProperty("line.separator", "\n");
  
  /** Prevents instance creation */
  private TextUtil() {}
  
  /**
   * Convert the given object to a string.  This method invokes {@link RecurUtil#safeToString(Object)}
   * to provide simple, safe handling of {@code null} values, arrays, and self-referential data structures
   * (with cooperation from the {@code toString()} method of the relevant class).
   */
  public static String toString(Object o) {
    return RecurUtil.safeToString(o);
  }
  
  /**
   * Break a string into a list of lines.  {@code "\n"}, {@code "\r"}, and {@code "\r\n"}
   * are considered line delimiters.  The empty string is taken to contain 0 lines.  An optional final
   * trailing newline will be ignored.
   */
  public static SizedIterable<String> getLines(String s) {
    SizedIterable<String> result = IterUtil.<String>empty();
    BufferedReader r = new BufferedReader(new StringReader(s));
    try {
      String line = r.readLine();
      while (line != null) {
        result = IterUtil.compose(result, line);
        line = r.readLine();
      }
    }
    catch (IOException e) {
      // Should not happen with a StringReader, but if it does, just ignore it
    }
    finally { 
      try { r.close(); }
      catch (IOException e) { /* ignore */ }
    }
    return result;
  }
  
  /** Produce a string by concatenating {@code copies} instances of {@code s} */
  public static String repeat(String s, int copies) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < copies; i++) { result.append(s); }
    return result.toString();
  }
  
  /** Produce a string by concatenating {@code copies} instances of {@code c} */
  public static String repeat(char c, int copies) {
    char[] result = new char[copies];
    Arrays.fill(result, c);
    return String.valueOf(result);
  }
  
  /** Determine if the given character occurs in {@code s}.  Defined in terms of {@link String#indexOf(int)}. */
  public static boolean contains(String s, int character) { return s.indexOf(character) >= 0; }
  
  /**
   * Determine if the given string occurs in {@code s}.  Defined in terms of {@link String#indexOf(String)}.
   * This is also defined as {@link String#contains}, but is defined here for legacy support.
   */
  public static boolean contains(String s, String piece) { return s.indexOf(piece) >= 0; }
  
  /**
   * Determine if <em>any</em> of the given characters occurs in {@code s}.  Defined in terms of
   * {@link String#indexOf(int)}.
   */
  public static boolean containsAny(String s, int... characters) {
    for (int c: characters) { if (contains(s, c)) { return true; } }
    return false;
  }
  
  /**
   * Determine if <em>any</em> of the given strings occurs in {@code s}.  Defined in terms of
   * {@link String#indexOf(String)}.
   */
  public static boolean containsAny(String s, String... pieces) {
    for (String piece: pieces) { if (contains(s, piece)) { return true; } }
    return false;
  }
  
  /**
   * Determine if <em>all</em> of the given characters occur in {@code s}.  Defined in terms of
   * {@link String#indexOf(int)}.
   */
  public static boolean containsAll(String s, int... characters) {
    for (int c: characters) { if (!contains(s, c)) { return false; } }
    return true;
  }
  
  /**
   * Determine if <em>all</em> of the given strings occur in {@code s}.  Defined in terms of
   * {@link String#indexOf(String)}.
   */
  public static boolean containsAll(String s, String... pieces) {
    for (String piece: pieces) { if (!contains(s, piece)) { return false; } }
    return true;
  }
  
  /**
   * Determine if the given string occurs in {@code s}, ignoring differences in case.  Unlike 
   * {@link String#equalsIgnoreCase}, this test only compares the lower-case conversion of {@code s} to the lower-case
   * conversion of {@code piece}.
   */
  public static boolean containsIgnoreCase(String s, String piece) {
    return s.toLowerCase().indexOf(piece.toLowerCase()) >= 0;
  }
  
  /**
   * Determine if <em>any</em> of the given strings occurs in {@code s}, ignoring differences in case.  Defined in 
   * terms of {@link #containsIgnoreCase}.
   */
  public static boolean containsAnyIgnoreCase(String s, String... pieces) {
    for (String piece: pieces) { if (contains(s, piece)) { return true; } }
    return false;
  }
  
  /**
   * Determine if <em>all</em> of the given strings occur in {@code s}, ignoring differences in case.  Defined in 
   * terms of {@link #containsIgnoreCase}.
   */
  public static boolean containsAllIgnoreCase(String s, String... pieces) {
    for (String piece: pieces) { if (!contains(s, piece)) { return false; } }
    return true;
  }
  
  /**
   * Find the first occurance of any of the given characters in {@code s}.  If none are present, the result is 
   * {@code -1}.  Defined in terms of {@link String#indexOf(int)}.
   */
  public static int indexOfFirst(String s, int... characters) {
    int result = -1;
    for (int c : characters) {
      int index = s.indexOf(c);
      if (index >= 0 && (result < 0 || index < result)) { result = index; }
    }
    return result;
  }
  
  /**
    * Find the first occurance of any of the given strings in {@code s}.  If none are present, the result is 
   * {@code -1}.  Defined in terms of {@link String#indexOf(String)}.
   */
  public static int indexOfFirst(String s, String... pieces) {
    int result = -1;
    for (String piece : pieces) {
      int index = s.indexOf(piece);
      if (index >= 0 && (result < 0 || index < result)) { result = index; }
    }
    return result;
  }
  
}
