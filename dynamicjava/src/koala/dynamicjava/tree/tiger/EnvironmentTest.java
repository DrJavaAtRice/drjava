/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

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