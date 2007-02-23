package edu.rice.cs.plt.text;

import junit.framework.TestCase;
import edu.rice.cs.plt.iter.IterUtil;

import static edu.rice.cs.plt.text.TextUtil.*;

public class TextUtilTest extends TestCase {
  
  public void testToString() {
    assertEquals("null", TextUtil.toString(null));
    assertEquals("fish", TextUtil.toString("fish"));
    assertEquals("12", TextUtil.toString(12));
    assertEquals("{ 1, 2, 3 }", TextUtil.toString(new int[]{ 1, 2, 3 }));
    assertEquals("{ null, null, null, null }", TextUtil.toString(new Object[4]));
    Object[] array = new Object[4];
    array[2] = array;
    assertEquals("{ null, null, { ... }, null }", TextUtil.toString(array));
  }
  
  public void testGetLines() {
    assertTrue(IterUtil.isEqual(IterUtil.empty(), getLines("")));
    assertTrue(IterUtil.isEqual(IterUtil.make("a"), getLines("a")));
    assertTrue(IterUtil.isEqual(IterUtil.make("a"), getLines("a\n")));
    assertTrue(IterUtil.isEqual(IterUtil.make("a", "", "", ""), getLines("a\n\n\n\n")));
    assertTrue(IterUtil.isEqual(IterUtil.make("", "a"), getLines("\na")));
    assertTrue(IterUtil.isEqual(IterUtil.make("a", "b", "c", "d", "", "e"), getLines("a\nb\r\nc\rd\n\re")));
  }
  
  public void testRepeat() {
    assertEquals("ababab", repeat("ab", 3));
    assertEquals("", repeat("fish are fun", 0));
    assertEquals("bbbbbb", repeat('b', 6));
    assertEquals("", repeat('x', 0));
  }
  
  public void testContains() {
    assertTrue(contains("foo", 'f'));
    assertTrue(contains("foo", 'o'));
    assertFalse(contains("foo", 'p'));
    assertTrue(contains("foo", "f"));
    assertTrue(contains("foo", ""));
    assertTrue(contains("foo", "oo"));
    assertTrue(contains("foo", "foo"));
    assertFalse(contains("foo", "food"));
    assertFalse(contains("foo", "F"));
    assertFalse(contains("Foo", "f"));
  }
  
  public void testContainsIgnoreCase() {
    assertTrue(containsIgnoreCase("foo", "f"));
    assertTrue(containsIgnoreCase("foo", "F"));
    assertTrue(containsIgnoreCase("Foo", "F"));
    assertTrue(containsIgnoreCase("fOo", "oO"));
    assertTrue(containsIgnoreCase("Foo", ""));
    assertTrue(containsIgnoreCase("Foo", "foo"));
    assertFalse(containsIgnoreCase("Foo", "Food"));
  }
  
}
