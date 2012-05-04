/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.reflect;

import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Collections;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Lambda3;
import junit.framework.TestCase;
import java.io.File;
import java.io.IOException;

import static edu.rice.cs.plt.reflect.JavaVersion.*;
import static edu.rice.cs.plt.reflect.JavaVersion.VendorType.*;

public class JavaVersionTest extends TestCase {
  
  public void testParseClassVersion() {
    assertEquals(JAVA_7, parseClassVersion("51.0"));
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
    assertEquals("5.1", v4.versionString());
    
    FullVersion v5 = parseFullVersion("1.5.1");
    assertEquals(JAVA_5, v5.majorVersion());
    assertEquals("5.1", v5.versionString());
    
    FullVersion v6 = parseFullVersion("1.6.0_2");
    assertEquals(JAVA_6, v6.majorVersion());
    assertEquals("6.0_2", v6.versionString());
    
    FullVersion v7 = parseFullVersion("1.6.0_11");
    assertEquals("6.0_11", v7.versionString());
    
    FullVersion v8 = parseFullVersion("1.7.0");
    assertEquals("7.0", v8.versionString());
    
    FullVersion v9 = parseFullVersion("1.7.0_11");
    assertEquals("7.0_11", v9.versionString());
    
    Set<FullVersion> sorter = new TreeSet<FullVersion>();
    sorter.add(v9);
    sorter.add(v8);
    sorter.add(v7);
    sorter.add(v5);
    sorter.add(v3);
    sorter.add(v1);
    sorter.add(v2);
    sorter.add(v4);
    sorter.add(v6);
    
    assertTrue(IterUtil.isEqual(sorter, IterUtil.make(v1, v2, v3, v5, v6, v7, v8, v9)));
  }
  
  public void testParseFullVersionUnrecognized() {
    FullVersion v1 = parseFullVersion("1.4.2_10","","");
    assertEquals(JAVA_1_4, v1.majorVersion());
    assertEquals("1.4.2_10", v1.versionString());
    
    FullVersion v2 = parseFullVersion("1.4.3","","");
    assertEquals(JAVA_1_4, v2.majorVersion());
    assertEquals("1.4.3", v2.versionString());
    
    FullVersion v3 = parseFullVersion("1.5.0.1","","");
    assertEquals(JAVA_5, v3.majorVersion());
    assertEquals("5.0_1", v3.versionString());
    
    FullVersion v4 = parseFullVersion("1.5.1-beta","","");
    assertEquals(JAVA_5, v4.majorVersion());
    assertEquals("5.1", v4.versionString());
    
    FullVersion v5 = parseFullVersion("1.5.1","","");
    assertEquals(JAVA_5, v5.majorVersion());
    assertEquals("5.1", v5.versionString());
    
    FullVersion v6 = parseFullVersion("1.6.0_2","","");
    assertEquals(JAVA_6, v6.majorVersion());
    assertEquals("6.0_2", v6.versionString());
    
    FullVersion v7 = parseFullVersion("1.6.0_11","","");
    assertEquals("6.0_11", v7.versionString());
    
    FullVersion v8 = parseFullVersion("1.7.0","","");
    assertEquals("7.0", v8.versionString());
    
    FullVersion v9 = parseFullVersion("1.7.0_11","","");
    assertEquals("7.0_11", v9.versionString());
    
    Set<FullVersion> sorter = new TreeSet<FullVersion>();
    sorter.add(v9);
    sorter.add(v8);
    sorter.add(v7);
    sorter.add(v5);
    sorter.add(v3);
    sorter.add(v1);
    sorter.add(v2);
    sorter.add(v4);
    sorter.add(v6);
    
    assertTrue(IterUtil.isEqual(sorter, IterUtil.make(v1, v2, v3, v5, v6, v7, v8, v9)));
  }
  
  public void testParseFullVersionApple() {
    FullVersion v1 = parseFullVersion("1.4.2_10","Java(TM) 2 Runtime Environment, Standard Edition",
                                      "\"Apple Computer, Inc.\"");
    assertEquals(JAVA_1_4, v1.majorVersion());
    assertEquals("1.4.2_10", v1.versionString());
    
    FullVersion v2 = parseFullVersion("1.4.3","Java(TM) 2 Runtime Environment, Standard Edition",
                                      "\"Apple Computer, Inc.\"");
    assertEquals(JAVA_1_4, v2.majorVersion());
    assertEquals("1.4.3", v2.versionString());
    
    FullVersion v3 = parseFullVersion("1.5.0.1","Java(TM) 2 Runtime Environment, Standard Edition",
                                      "\"Apple Computer, Inc.\"");
    assertEquals(JAVA_5, v3.majorVersion());
    assertEquals("5.0_1", v3.versionString());
    
    FullVersion v4 = parseFullVersion("1.5.1-beta","Java(TM) 2 Runtime Environment, Standard Edition",
                                      "\"Apple Computer, Inc.\"");
    assertEquals(JAVA_5, v4.majorVersion());
    assertEquals("5.1", v4.versionString());
    
    FullVersion v5 = parseFullVersion("1.5.1","Java(TM) 2 Runtime Environment, Standard Edition",
                                      "\"Apple Computer, Inc.\"");
    assertEquals(JAVA_5, v5.majorVersion());
    assertEquals("5.1", v5.versionString());
    
    FullVersion v6 = parseFullVersion("1.6.0_2","Java(TM) 2 Runtime Environment, Standard Edition",
                                      "\"Apple Computer, Inc.\"");
    assertEquals(JAVA_6, v6.majorVersion());
    assertEquals("6.0_2", v6.versionString());
    
    FullVersion v7 = parseFullVersion("1.6.0_11","Java(TM) 2 Runtime Environment, Standard Edition",
                                      "\"Apple Computer, Inc.\"");
    assertEquals("6.0_11", v7.versionString());
    
    FullVersion v8 = parseFullVersion("1.7.0","Java(TM) SE Runtime Environment",
                                      "Oracle Corporation");
    assertEquals("7.0", v8.versionString());
    
    FullVersion v9 = parseFullVersion("1.7.0_11","Java(TM) SE Runtime Environment",
                                      "Oracle Corporation");
    assertEquals("7.0_11", v9.versionString());
    
    Set<FullVersion> sorter = new TreeSet<FullVersion>();
    sorter.add(v8);
    sorter.add(v7);
    sorter.add(v5);
    sorter.add(v3);
    sorter.add(v1);
    sorter.add(v2);
    sorter.add(v4);
    sorter.add(v9);
    sorter.add(v6);
    
    assertTrue(IterUtil.isEqual(sorter, IterUtil.make(v1, v2, v3, v5, v6, v7, v8, v9)));
  }
  
  public void testParseFullVersionSun() {
    FullVersion v1 = parseFullVersion("1.4.2_10","Java(TM) 2 Runtime Environment, Standard Edition",
                                      "Sun Microsystems Inc.");
    assertEquals(JAVA_1_4, v1.majorVersion());
    assertEquals("1.4.2_10", v1.versionString());
    
    FullVersion v2 = parseFullVersion("1.4.3","Java(TM) 2 Runtime Environment, Standard Edition",
                                      "Sun Microsystems Inc.");
    assertEquals(JAVA_1_4, v2.majorVersion());
    assertEquals("1.4.3", v2.versionString());
    
    FullVersion v3 = parseFullVersion("1.5.0.1","Java(TM) 2 Runtime Environment, Standard Edition",
                                      "Sun Microsystems Inc.");
    assertEquals(JAVA_5, v3.majorVersion());
    assertEquals("5.0_1", v3.versionString());
    
    FullVersion v4 = parseFullVersion("1.5.1-beta","Java(TM) 2 Runtime Environment, Standard Edition",
                                      "Sun Microsystems Inc.");
    assertEquals(JAVA_5, v4.majorVersion());
    assertEquals("5.1", v4.versionString());
    
    FullVersion v5 = parseFullVersion("1.5.1","Java(TM) 2 Runtime Environment, Standard Edition",
                                      "Sun Microsystems Inc.");
    assertEquals(JAVA_5, v5.majorVersion());
    assertEquals("5.1", v5.versionString());
    
    FullVersion v6 = parseFullVersion("1.6.0_2","Java(TM) 2 Runtime Environment, Standard Edition",
                                      "Sun Microsystems Inc.");
    assertEquals(JAVA_6, v6.majorVersion());
    assertEquals("6.0_2", v6.versionString());
    
    FullVersion v7 = parseFullVersion("1.6.0_11","Java(TM) 2 Runtime Environment, Standard Edition",
                                      "Sun Microsystems Inc.");
    assertEquals("6.0_11", v7.versionString());
    
    FullVersion v8 = parseFullVersion("1.7.0","Java(TM) SE Runtime Environment",
                                      "Oracle Corporation");
    assertEquals("7.0", v8.versionString());
    
    FullVersion v9 = parseFullVersion("1.7.0_11","Java(TM) SE Runtime Environment",
                                      "Oracle Corporation");
    assertEquals("7.0_11", v9.versionString());
    
    Set<FullVersion> sorter = new TreeSet<FullVersion>();
    sorter.add(v7);
    sorter.add(v5);
    sorter.add(v8);
    sorter.add(v9);
    sorter.add(v3);
    sorter.add(v1);
    sorter.add(v2);
    sorter.add(v4);
    sorter.add(v6);
    
    assertTrue(IterUtil.isEqual(sorter, IterUtil.make(v1, v2, v3, v5, v6, v7, v8, v9)));
  }
  
  public void testParseFullVersionOracle() {
    FullVersion v1 = parseFullVersion("1.4.2_10","Java(TM) 2 Runtime Environment, Standard Edition","Oracle");
    assertEquals(JAVA_1_4, v1.majorVersion());
    assertEquals("1.4.2_10", v1.versionString());
    
    FullVersion v2 = parseFullVersion("1.4.3","Java(TM) 2 Runtime Environment, Standard Edition","Oracle");
    assertEquals(JAVA_1_4, v2.majorVersion());
    assertEquals("1.4.3", v2.versionString());
    
    FullVersion v3 = parseFullVersion("1.5.0.1","Java(TM) 2 Runtime Environment, Standard Edition","Oracle");
    assertEquals(JAVA_5, v3.majorVersion());
    assertEquals("5.0_1", v3.versionString());
    
    FullVersion v4 = parseFullVersion("1.5.1-beta","Java(TM) 2 Runtime Environment, Standard Edition","Oracle");
    assertEquals(JAVA_5, v4.majorVersion());
    assertEquals("5.1", v4.versionString());
    
    FullVersion v5 = parseFullVersion("1.5.1","Java(TM) 2 Runtime Environment, Standard Edition","Oracle");
    assertEquals(JAVA_5, v5.majorVersion());
    assertEquals("5.1", v5.versionString());
    
    FullVersion v6 = parseFullVersion("1.6.0_2","Java(TM) 2 Runtime Environment, Standard Edition","Oracle");
    assertEquals(JAVA_6, v6.majorVersion());
    assertEquals("6.0_2", v6.versionString());
    
    FullVersion v7 = parseFullVersion("1.6.0_11","Java(TM) 2 Runtime Environment, Standard Edition","Oracle");
    assertEquals("6.0_11", v7.versionString());
    
    FullVersion v8 = parseFullVersion("1.7.0","Java(TM) SE Runtime Environment","Oracle");
    assertEquals("7.0", v8.versionString());
    
    FullVersion v9 = parseFullVersion("1.7.0_11","Java(TM) SE Runtime Environment","Oracle");
    assertEquals("7.0_11", v9.versionString());
    
    Set<FullVersion> sorter = new TreeSet<FullVersion>();
    sorter.add(v7);
    sorter.add(v5);
    sorter.add(v3);
    sorter.add(v1);
    sorter.add(v8);
    sorter.add(v9);
    sorter.add(v2);
    sorter.add(v4);
    sorter.add(v6);
    
    assertTrue(IterUtil.isEqual(sorter, IterUtil.make(v1, v2, v3, v5, v6, v7, v8, v9)));
  }
    
  public void testParseFullVersionOpenJDK() {
    FullVersion v1 = parseFullVersion("1.4.2_10","OpenJDK Runtime Environment","Sun Microsystems Inc.");
    assertEquals(JAVA_1_4, v1.majorVersion());
    assertEquals("1.4.2_10-OpenJDK", v1.versionString());
    
    FullVersion v2 = parseFullVersion("1.4.3","OpenJDK Runtime Environment","Sun Microsystems Inc.");
    assertEquals(JAVA_1_4, v2.majorVersion());
    assertEquals("1.4.3-OpenJDK", v2.versionString());
    
    FullVersion v3 = parseFullVersion("1.5.0.1","OpenJDK Runtime Environment","Sun Microsystems Inc.");
    assertEquals(JAVA_5, v3.majorVersion());
    assertEquals("5.0_1-OpenJDK", v3.versionString());
    
    FullVersion v4 = parseFullVersion("1.5.1-beta","OpenJDK Runtime Environment","Sun Microsystems Inc.");
    assertEquals(JAVA_5, v4.majorVersion());
    assertEquals("5.1-OpenJDK", v4.versionString());
  
    FullVersion v5 = parseFullVersion("1.5.1","OpenJDK Runtime Environment","Sun Microsystems Inc.");
    assertEquals(JAVA_5, v5.majorVersion());
    assertEquals("5.1-OpenJDK", v5.versionString());
    
    FullVersion v6 = parseFullVersion("1.6.0_2","OpenJDK Runtime Environment","Sun Microsystems Inc.");
    assertEquals(JAVA_6, v6.majorVersion());
    assertEquals("6.0_2-OpenJDK", v6.versionString());
    
    FullVersion v7 = parseFullVersion("1.6.0_11","OpenJDK Runtime Environment","Sun Microsystems Inc.");
    assertEquals("6.0_11-OpenJDK", v7.versionString());
    
    FullVersion v8 = parseFullVersion("1.7.0","OpenJDK Runtime Environment","Sun Microsystems Inc.");
    assertEquals("7.0-OpenJDK", v8.versionString());

    FullVersion v9 = parseFullVersion("1.7.0_11","OpenJDK Runtime Environment","Sun Microsystems Inc.");
    assertEquals("7.0_11-OpenJDK", v9.versionString());
    
    Set<FullVersion> sorter = new TreeSet<FullVersion>();
    sorter.add(v7);
    sorter.add(v5);
    sorter.add(v3);
    sorter.add(v1);
    sorter.add(v2);
    sorter.add(v4);
    sorter.add(v8);
    sorter.add(v9);
    sorter.add(v6);
    
    assertTrue(IterUtil.isEqual(sorter, IterUtil.make(v1, v2, v3, v5, v6, v7, v8, v9)));
  }
  
  public void testParseFullVersionMint() {
    FullVersion v1 = parseFullVersion("1.4.2_10","mint","mint");
    assertEquals(JAVA_1_4, v1.majorVersion());
    assertEquals(UNKNOWN, v1.vendor());
    assertEquals("1.4.2_10", v1.versionString());
    
    FullVersion v2 = parseFullVersion("1.4.3","mint","mint");
    assertEquals(JAVA_1_4, v2.majorVersion());
    assertEquals(UNKNOWN, v2.vendor());
    assertEquals("1.4.3", v2.versionString());
    
    FullVersion v3 = parseFullVersion("1.5.0.1","mint","mint");
    assertEquals(JAVA_5, v3.majorVersion());
    assertEquals(UNKNOWN, v3.vendor());
    assertEquals("5.0_1", v3.versionString());
    
    FullVersion v4 = parseFullVersion("1.5.1-beta","mint","mint");
    assertEquals(JAVA_5, v4.majorVersion());
    assertEquals(UNKNOWN, v4.vendor());
    assertEquals("5.1", v4.versionString());
    
    FullVersion v5 = parseFullVersion("1.5.1","mint","mint");
    assertEquals(JAVA_5, v5.majorVersion());
    assertEquals(UNKNOWN, v5.vendor());
    assertEquals("5.1", v5.versionString());
    
    FullVersion v6 = parseFullVersion("1.6.0_2","mint","mint");
    assertEquals(JAVA_6, v6.majorVersion());
    assertEquals(UNKNOWN, v6.vendor());
    assertEquals("6.0_2", v6.versionString());
    
    FullVersion v7 = parseFullVersion("1.6.0_11","mint","mint");
    assertEquals(UNKNOWN, v7.vendor());
    assertEquals("6.0_11", v7.versionString());
    
    FullVersion v8 = parseFullVersion("1.7.0","mint","mint");
    assertEquals(UNKNOWN, v8.vendor());
    assertEquals("7.0", v8.versionString());
    
    FullVersion v9 = parseFullVersion("1.7.0_11","mint","mint");
    assertEquals(UNKNOWN, v9.vendor());
    assertEquals("7.0_11", v9.versionString());
    
    Set<FullVersion> sorter = new TreeSet<FullVersion>();
    sorter.add(v7);
    sorter.add(v5);
    sorter.add(v3);
    sorter.add(v1);
    sorter.add(v2);
    sorter.add(v8);
    sorter.add(v9);
    sorter.add(v4);
    sorter.add(v6);
    
    assertTrue(IterUtil.isEqual(sorter, IterUtil.make(v1, v2, v3, v5, v6, v7, v8, v9)));
  }

  public void testParseFullVersionSort() throws IOException {
    FullVersion vUnrecognized = parseFullVersion("1.6.0_11","","");
    assertEquals("6.0_11", vUnrecognized.versionString());
    File mintJDK = File.createTempFile("jdk-mint-",".tmp");
    mintJDK.delete();
    FullVersion vMint = parseFullVersion("1.6.0_11","mint","mint", mintJDK);
    assertEquals(UNKNOWN, vMint.vendor());
    assertEquals("6.0_11", vMint.versionString());
    File hjJDK = File.createTempFile("jdk-hj-",".tmp");
    hjJDK.delete();
    FullVersion vHJ = parseFullVersion("1.6.0_11","hj","hj", hjJDK);
    assertEquals(UNKNOWN, vHJ.vendor());
    assertEquals("6.0_11", vHJ.versionString());
    File nextGenJDK = File.createTempFile("jdk-nextgen-",".tmp");
    nextGenJDK.delete();
    FullVersion vNextGen = parseFullVersion("1.6.0_11","nextgen","nextgen", nextGenJDK);
    assertEquals(UNKNOWN, vNextGen.vendor());
    assertEquals("6.0_11", vNextGen.versionString());
    FullVersion vOpenJDK = parseFullVersion("1.6.0_11","OpenJDK Runtime Environment",
                                            "Sun Microsystems Inc.");
    assertEquals("6.0_11-OpenJDK", vOpenJDK.versionString());
    FullVersion vApple = parseFullVersion("1.6.0_11","Java(TM) 2 Runtime Environment, Standard Edition",
                                          "\"Apple Computer, Inc.\"");
    assertEquals("6.0_11", vApple.versionString());
    FullVersion vSun = parseFullVersion("1.6.0_11","Java(TM) 2 Runtime Environment, Standard Edition",
                                        "Sun Microsystems Inc.");
    assertEquals("6.0_11", vSun.versionString());
    FullVersion vOracle = parseFullVersion("1.7.0","Java(TM) SE Runtime Environment",
                                        "Oracle Corporation");
    assertEquals("7.0", vOracle.versionString());
    
    Set<FullVersion> sorter = new TreeSet<FullVersion>();
    sorter.add(vSun);
    sorter.add(vOracle);
    sorter.add(vApple);
    sorter.add(vOpenJDK);
    sorter.add(vUnrecognized);
    sorter.add(vMint);
    sorter.add(vHJ);
    sorter.add(vNextGen);
    
    // alphabetically ordered by file: jdk-hj... < jdk-mint... < jdk-nextgen...
    Iterable<FullVersion> expected = IterUtil.make(vUnrecognized, vHJ, vMint, vNextGen, vOpenJDK, vApple, vSun, vOracle);
    
    assertTrue(IterUtil.isEqual(sorter, expected));
  }
  
  public void testParseFullVersionDifferentSort() {
    FullVersion vUnrecognized = parseFullVersion("1.6.0_11","","");
    assertEquals("6.0_11", vUnrecognized.versionString());
    FullVersion vMint = parseFullVersion("1.7.0_11","mint","mint");
    assertEquals(UNKNOWN, vMint.vendor());
    assertEquals("7.0_11", vMint.versionString());
    FullVersion vOpenJDK = parseFullVersion("1.6.0_11","OpenJDK Runtime Environment",
                                            "Sun Microsystems Inc.");
    assertEquals("6.0_11-OpenJDK", vOpenJDK.versionString());
    FullVersion vApple = parseFullVersion("1.6.0_11","Java(TM) 2 Runtime Environment, Standard Edition",
                                          "\"Apple Computer, Inc.\"");
    assertEquals("6.0_11", vApple.versionString());
    FullVersion vSun = parseFullVersion("1.6.0_11","Java(TM) 2 Runtime Environment, Standard Edition",
                                        "Sun Microsystems Inc.");
    assertEquals("6.0_11", vSun.versionString());
    FullVersion vOracle = parseFullVersion("1.7.0","Java(TM) SE Runtime Environment",
                                        "Oracle Corporation");
    assertEquals("7.0", vOracle.versionString());
    
    Set<FullVersion> sorter = new TreeSet<FullVersion>();
    sorter.add(vSun);
    sorter.add(vOracle);
    sorter.add(vApple);
    sorter.add(vOpenJDK);
    sorter.add(vUnrecognized);
    sorter.add(vMint);
    
    Iterable<FullVersion> expected = IterUtil.make(vUnrecognized, vOpenJDK, vApple, vSun, vOracle, vMint);   
    assertTrue(IterUtil.isEqual(sorter, expected));
  }
    
  public void testVersionToFullVersion() {
    assertEquals("Java 1.1.0", JAVA_1_1.fullVersion().toString());
    assertEquals("Java 1.2.0", JAVA_1_2.fullVersion().toString());
    assertEquals("Java 1.3.0", JAVA_1_3.fullVersion().toString());
    assertEquals("Java 1.4.0", JAVA_1_4.fullVersion().toString());
    assertEquals("Java 5.0", JAVA_5.fullVersion().toString());
    assertEquals("Java 6.0", JAVA_6.fullVersion().toString());
    assertEquals("Java 7.0", JAVA_7.fullVersion().toString());
    assertEquals("Java >7.0", FUTURE.fullVersion().toString());
  }  
}
