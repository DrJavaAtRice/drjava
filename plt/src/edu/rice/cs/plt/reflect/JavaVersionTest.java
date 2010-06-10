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
    assertEquals("5.1-beta", v4.versionString());
    
    FullVersion v5 = parseFullVersion("1.5.1","","");
    assertEquals(JAVA_5, v5.majorVersion());
    assertEquals("5.1", v5.versionString());
    
    FullVersion v6 = parseFullVersion("1.6.0_2","","");
    assertEquals(JAVA_6, v6.majorVersion());
    assertEquals("6.0_2", v6.versionString());
    
    FullVersion v7 = parseFullVersion("1.6.0_11","","");
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
  
  public void testParseFullVersionApple() {
    FullVersion v1 = parseFullVersion("1.4.2_10","Java(TM) 2 Runtime Environment, Standard Edition","\"Apple Computer, Inc.\"");
    assertEquals(JAVA_1_4, v1.majorVersion());
    assertEquals("1.4.2_10", v1.versionString());
    
    FullVersion v2 = parseFullVersion("1.4.3","Java(TM) 2 Runtime Environment, Standard Edition","\"Apple Computer, Inc.\"");
    assertEquals(JAVA_1_4, v2.majorVersion());
    assertEquals("1.4.3", v2.versionString());
    
    FullVersion v3 = parseFullVersion("1.5.0.1","Java(TM) 2 Runtime Environment, Standard Edition","\"Apple Computer, Inc.\"");
    assertEquals(JAVA_5, v3.majorVersion());
    assertEquals("5.0_1", v3.versionString());
    
    FullVersion v4 = parseFullVersion("1.5.1-beta","Java(TM) 2 Runtime Environment, Standard Edition","\"Apple Computer, Inc.\"");
    assertEquals(JAVA_5, v4.majorVersion());
    assertEquals("5.1-beta", v4.versionString());
    
    FullVersion v5 = parseFullVersion("1.5.1","Java(TM) 2 Runtime Environment, Standard Edition","\"Apple Computer, Inc.\"");
    assertEquals(JAVA_5, v5.majorVersion());
    assertEquals("5.1", v5.versionString());
    
    FullVersion v6 = parseFullVersion("1.6.0_2","Java(TM) 2 Runtime Environment, Standard Edition","\"Apple Computer, Inc.\"");
    assertEquals(JAVA_6, v6.majorVersion());
    assertEquals("6.0_2", v6.versionString());
    
    FullVersion v7 = parseFullVersion("1.6.0_11","Java(TM) 2 Runtime Environment, Standard Edition","\"Apple Computer, Inc.\"");
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
  
  public void testParseFullVersionSun() {
    FullVersion v1 = parseFullVersion("1.4.2_10","Java(TM) 2 Runtime Environment, Standard Edition","Sun Microsystems Inc.");
    assertEquals(JAVA_1_4, v1.majorVersion());
    assertEquals("1.4.2_10", v1.versionString());
    
    FullVersion v2 = parseFullVersion("1.4.3","Java(TM) 2 Runtime Environment, Standard Edition","Sun Microsystems Inc.");
    assertEquals(JAVA_1_4, v2.majorVersion());
    assertEquals("1.4.3", v2.versionString());
    
    FullVersion v3 = parseFullVersion("1.5.0.1","Java(TM) 2 Runtime Environment, Standard Edition","Sun Microsystems Inc.");
    assertEquals(JAVA_5, v3.majorVersion());
    assertEquals("5.0_1", v3.versionString());
    
    FullVersion v4 = parseFullVersion("1.5.1-beta","Java(TM) 2 Runtime Environment, Standard Edition","Sun Microsystems Inc.");
    assertEquals(JAVA_5, v4.majorVersion());
    assertEquals("5.1-beta", v4.versionString());
    
    FullVersion v5 = parseFullVersion("1.5.1","Java(TM) 2 Runtime Environment, Standard Edition","Sun Microsystems Inc.");
    assertEquals(JAVA_5, v5.majorVersion());
    assertEquals("5.1", v5.versionString());
    
    FullVersion v6 = parseFullVersion("1.6.0_2","Java(TM) 2 Runtime Environment, Standard Edition","Sun Microsystems Inc.");
    assertEquals(JAVA_6, v6.majorVersion());
    assertEquals("6.0_2", v6.versionString());
    
    FullVersion v7 = parseFullVersion("1.6.0_11","Java(TM) 2 Runtime Environment, Standard Edition","Sun Microsystems Inc.");
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
    assertEquals("5.1-beta-OpenJDK", v4.versionString());
    
    FullVersion v5 = parseFullVersion("1.5.1","OpenJDK Runtime Environment","Sun Microsystems Inc.");
    assertEquals(JAVA_5, v5.majorVersion());
    assertEquals("5.1-OpenJDK", v5.versionString());
    
    FullVersion v6 = parseFullVersion("1.6.0_2","OpenJDK Runtime Environment","Sun Microsystems Inc.");
    assertEquals(JAVA_6, v6.majorVersion());
    assertEquals("6.0_2-OpenJDK", v6.versionString());
    
    FullVersion v7 = parseFullVersion("1.6.0_11","OpenJDK Runtime Environment","Sun Microsystems Inc.");
    assertEquals("6.0_11-OpenJDK", v7.versionString());
    
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
  
  public void testParseFullVersionMint() {
    FullVersion v1 = parseFullVersion("1.4.2_10","mint","mint");
    assertEquals(JAVA_1_4, v1.majorVersion());
    assertEquals("1.4.2_10-Mint", v1.versionString());
    
    FullVersion v2 = parseFullVersion("1.4.3","mint","mint");
    assertEquals(JAVA_1_4, v2.majorVersion());
    assertEquals("1.4.3-Mint", v2.versionString());
    
    FullVersion v3 = parseFullVersion("1.5.0.1","mint","mint");
    assertEquals(JAVA_5, v3.majorVersion());
    assertEquals("5.0_1-Mint", v3.versionString());
    
    FullVersion v4 = parseFullVersion("1.5.1-beta","mint","mint");
    assertEquals(JAVA_5, v4.majorVersion());
    assertEquals("5.1-beta-Mint", v4.versionString());
    
    FullVersion v5 = parseFullVersion("1.5.1","mint","mint");
    assertEquals(JAVA_5, v5.majorVersion());
    assertEquals("5.1-Mint", v5.versionString());
    
    FullVersion v6 = parseFullVersion("1.6.0_2","mint","mint");
    assertEquals(JAVA_6, v6.majorVersion());
    assertEquals("6.0_2-Mint", v6.versionString());
    
    FullVersion v7 = parseFullVersion("1.6.0_11","mint","mint");
    assertEquals("6.0_11-Mint", v7.versionString());
    
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

  public void testParseFullVersionDetectors() {
    FullVersion v1 = parseFullVersion("1.4.2_10","habanero","habanero",DETECTORS);
    assertEquals(JAVA_1_4, v1.majorVersion());
    assertEquals("1.4.2_10-Habanero", v1.versionString());
    
    FullVersion v2 = parseFullVersion("1.4.3","mint","mint",DETECTORS);
    assertEquals(JAVA_1_4, v2.majorVersion());
    assertEquals("1.4.3-Mint", v2.versionString());
    
    FullVersion v3 = parseFullVersion("1.5.0.1","habanero","habanero",DETECTORS);
    assertEquals(JAVA_5, v3.majorVersion());
    assertEquals("5.0_1-Habanero", v3.versionString());
    
    FullVersion v4 = parseFullVersion("1.5.1-beta","mint","mint",DETECTORS);
    assertEquals(JAVA_5, v4.majorVersion());
    assertEquals("5.1-beta-Mint", v4.versionString());
    
    FullVersion v5 = parseFullVersion("1.5.1","habanero","habanero",DETECTORS);
    assertEquals(JAVA_5, v5.majorVersion());
    assertEquals("5.1-Habanero", v5.versionString());
    
    FullVersion v6 = parseFullVersion("1.6.0_2","mint","mint",DETECTORS);
    assertEquals(JAVA_6, v6.majorVersion());
    assertEquals("6.0_2-Mint", v6.versionString());
    
    FullVersion v7 = parseFullVersion("1.6.0_11","habanero","habanero",DETECTORS);
    assertEquals("6.0_11-Habanero", v7.versionString());
    
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
  
  public void testParseFullVersionSort() {
    FullVersion vUnrecognized = parseFullVersion("1.6.0_11","","");
    assertEquals("6.0_11", vUnrecognized.versionString());
    FullVersion vMint = parseFullVersion("1.6.0_11","mint","mint");
    assertEquals("6.0_11-Mint", vMint.versionString());
    FullVersion vOpenJDK = parseFullVersion("1.6.0_11","OpenJDK Runtime Environment","Sun Microsystems Inc.");
    assertEquals("6.0_11-OpenJDK", vOpenJDK.versionString());
    FullVersion vApple = parseFullVersion("1.6.0_11","Java(TM) 2 Runtime Environment, Standard Edition","\"Apple Computer, Inc.\"");
    assertEquals("6.0_11", vApple.versionString());
    FullVersion vSun = parseFullVersion("1.6.0_11","Java(TM) 2 Runtime Environment, Standard Edition","Sun Microsystems Inc.");
    assertEquals("6.0_11", vSun.versionString());
    
    Set<FullVersion> sorter = new TreeSet<FullVersion>();
    sorter.add(vSun);
    sorter.add(vApple);
    sorter.add(vOpenJDK);
    sorter.add(vUnrecognized);
    sorter.add(vMint);
    
    assertTrue(IterUtil.isEqual(sorter, IterUtil.make(vMint, vUnrecognized, vOpenJDK, vApple, vSun)));
  }
  
  public void testParseFullVersionDifferentSort() {
    FullVersion vUnrecognized = parseFullVersion("1.6.0_11","","");
    assertEquals("6.0_11", vUnrecognized.versionString());
    FullVersion vCompound = parseFullVersion("1.7.0_11","mint","mint");
    assertEquals("7.0_11-Mint", vCompound.versionString());
    FullVersion vOpenJDK = parseFullVersion("1.6.0_11","OpenJDK Runtime Environment","Sun Microsystems Inc.");
    assertEquals("6.0_11-OpenJDK", vOpenJDK.versionString());
    FullVersion vApple = parseFullVersion("1.6.0_11","Java(TM) 2 Runtime Environment, Standard Edition","\"Apple Computer, Inc.\"");
    assertEquals("6.0_11", vApple.versionString());
    FullVersion vSun = parseFullVersion("1.6.0_11","Java(TM) 2 Runtime Environment, Standard Edition","Sun Microsystems Inc.");
    assertEquals("6.0_11", vSun.versionString());
    
    Set<FullVersion> sorter = new TreeSet<FullVersion>();
    sorter.add(vSun);
    sorter.add(vApple);
    sorter.add(vOpenJDK);
    sorter.add(vUnrecognized);
    sorter.add(vCompound);
    
    assertTrue(IterUtil.isEqual(sorter, IterUtil.make(vCompound, vUnrecognized, vOpenJDK, vApple, vSun)));
  }
  
  public void testParseFullVersionDifferentSortDetectors() {
    FullVersion vUnrecognized = parseFullVersion("1.6.0_11","","",DETECTORS);
    assertEquals("6.0_11", vUnrecognized.versionString());
    FullVersion vCompound = parseFullVersion("1.7.0_11","mint","mint",DETECTORS);
    assertEquals("7.0_11-Mint", vCompound.versionString());
    FullVersion vCompound2 = parseFullVersion("1.7.0_11","habanero","habanero",DETECTORS);
    assertEquals("7.0_11-Habanero", vCompound2.versionString());
    FullVersion vOpenJDK = parseFullVersion("1.6.0_11","OpenJDK Runtime Environment","Sun Microsystems Inc.",DETECTORS);
    assertEquals("6.0_11-OpenJDK", vOpenJDK.versionString());
    FullVersion vApple = parseFullVersion("1.6.0_11","Java(TM) 2 Runtime Environment, Standard Edition",
                                          "\"Apple Computer, Inc.\"",DETECTORS);
    assertEquals("6.0_11", vApple.versionString());
    FullVersion vSun = parseFullVersion("1.6.0_11","Java(TM) 2 Runtime Environment, Standard Edition",
                                        "Sun Microsystems Inc.",DETECTORS);
    assertEquals("6.0_11", vSun.versionString());
    
    Set<FullVersion> sorter = new TreeSet<FullVersion>();
    sorter.add(vSun);
    sorter.add(vApple);
    sorter.add(vOpenJDK);
    sorter.add(vUnrecognized);
    sorter.add(vCompound);
    sorter.add(vCompound2);
    
    assertTrue(IterUtil.isEqual(sorter, IterUtil.make(vCompound, vUnrecognized, vOpenJDK, vApple, vSun)));
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
  
  public static final ArrayList<Lambda3<String,String,String,String>> DETECTORS =
    new ArrayList<Lambda3<String,String,String,String>>();
  static {
    DETECTORS.add(new Lambda3<String,String,String,String>() {
      public String value(String java_version, String java_runtime_name, String java_vm_vendor) {
        if (java_runtime_name.toLowerCase().contains("mint")) return "Mint";
        return null;
      }
    });
    DETECTORS.add(new Lambda3<String,String,String,String>() {
      public String value(String java_version, String java_runtime_name, String java_vm_vendor) {
        if (java_runtime_name.toLowerCase().contains("habanero")) return "Habanero";
        return null;
      }
    });
  }
}
