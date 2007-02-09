package edu.rice.cs.plt.text;

import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.recur.RecurUtil;

public final class TextUtil {
  
  public static final String NEWLINE;
  
  static {
    String newline = System.getProperty("line.separator");
    if (newline == null) { newline = "\n"; }
    NEWLINE = newline;
  }
  
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
   * are considered line delimiters.  The empty string is taken to contain 0 lines.
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
  
}
