package koala.dynamicjava.tree.tiger;

import java.util.*;
import junit.framework.*;

public class EnvironmentTest extends TestCase {
  public EnvironmentTest(String name) {
    super(name);
  }

  public void testEmptyEnvLookup() {
    try {
      new EmptyEnv<String, Object>().lookup("test");
      fail("lookup should fail on EmptyEnvs");
    }
    catch (IllegalArgumentException ex) {
    }
  }

  public void testExtendAndLookup() {
    HashMap<String, String> extension = new HashMap<String, String>();
    String key = "test";
    String value = "result";
    extension.put(key, value);
    Environment<String, String> e = new EmptyEnv<String, String>().extend(extension);
    assertEquals("Environment not extended properly.",
                 value,
                 e.lookup(key));
  }

  public void testDoubleExtension() {
    HashMap<String, String> extension1 = new HashMap<String, String>();
    HashMap<String, String> extension2 = new HashMap<String, String>();
    extension1.put("test1", "result1");
    extension2.put("test2", "result2");
    Environment<String, String> e = new EmptyEnv<String, String>().extend(extension1);
    e = e.extend(extension2);
    assertEquals("lookup() failed when accessing nested environment.",
                 "result1",
                 e.lookup("test1"));
    assertEquals("lookup() failed when accessing current environment.",
                 "result2",
                 e.lookup("test2"));
  }

  public void testGetRest() {
    HashMap<String, String> extension = new HashMap<String, String>();
    String key = "test";
    String value = "result";
    extension.put(key, value);
    Environment<String, String> e = new EmptyEnv<String, String>().extend(extension);
    assertEquals("Environment not extended properly.",
                 value, e.lookup(key));
    e = e.getRest(); // mutate e back. Equivalent to a stack 'pop'
    try{
      e.lookup(key);
      fail("lookup should fail on the empty rest of e");
    } catch(IllegalArgumentException x){
    }
  }
}