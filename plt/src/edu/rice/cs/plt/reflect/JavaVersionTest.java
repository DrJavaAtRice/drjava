package edu.rice.cs.plt.reflect;

import java.util.Set;
import java.util.TreeSet;
import edu.rice.cs.plt.iter.IterUtil;
import junit.framework.TestCase;

import static edu.rice.cs.plt.reflect.JavaVersion.*;

public class JavaVersionTest extends TestCase {
  
  public void testParseClassVersion() {
    assertEquals(JAVA_6, parseClassVersion("50.0"));
    assertEquals(JAVA_5, parseClassVersion("49.0"));
    assertEquals(JAVA_1_4, parseClassVersion("48.0"));
  }
  
  public void testParseFullVersion() {
    FullVersion v1 = parseFullVersion("1.4.2_10");
    assertEquals(JAVA_1_4, v1.majorVersion());
    assertEquals("1.4.2_10", v1.versionString());
    
    FullVersion v2 = parseFullVersion("1.4.3");
    assertEquals(JAVA_1_4, v2.majorVersion());
    assertEquals("1.4.3", v2.versionString());
    
    FullVersion v3 = parseFullVersion("1.5.0.1");
    assertEquals(JAVA_5, v3.majorVersion());
    assertEquals("5.0_1", v3.versionString());
    
    FullVersion v4 = parseFullVersion("1.5.1-beta");
    assertEquals(JAVA_5, v4.majorVersion());
    assertEquals("5.1-beta", v4.versionString());
    
    FullVersion v5 = parseFullVersion("1.5.1");
    assertEquals(JAVA_5, v5.majorVersion());
    assertEquals("5.1", v5.versionString());
    
    FullVersion v6 = parseFullVersion("1.6.0_2");
    assertEquals(JAVA_6, v6.majorVersion());
    assertEquals("6.0_2", v6.versionString());
    
    FullVersion v7 = parseFullVersion("1.6.0_11");
    assertEquals("6.0_11", v7.versionString());
    
    Set<FullVersion> sorter = new TreeSet<FullVersion>();
    sorter.add(v7);
    sorter.add(v5);
    sorter.add(v3);
    sorter.add(v1);
    sorter.add(v2);
    sorter.add(v4);
    sorter.add(v6);
    
    assertTrue(IterUtil.isEqual(sorter, IterUtil.make(v1, v2, v3, v4, v5, v6, v7)));
  }
  
}
