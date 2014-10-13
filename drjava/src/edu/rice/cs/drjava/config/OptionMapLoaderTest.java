/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.config;
import java.io.*;

import edu.rice.cs.drjava.DrJavaTestCase;

/**Tests the OptionMapLoader.
 * @version $Id: OptionMapLoaderTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class OptionMapLoaderTest extends DrJavaTestCase implements OptionConstants {
  
  public OptionMapLoaderTest(String s) { super(s); }
  
  public static class StringInputStream extends ByteArrayInputStream {
    public StringInputStream(String s) { super(s.getBytes()); }
  }
  
  /** an artificially created properties "file" **/
  public static final String OPTION_DOC = 
    "# this is a fake header\n" + 
    "this.is.a.real.key = value\n" + 
    "indent.level = 1\n" + 
    "javac.location = foo\n" + 
    "extra.classpath = bam\n\n";
  
  public void testProperConfigSet() throws IOException {
    checkSet(OPTION_DOC, Integer.valueOf(1), new File("foo"), 1);
  }
  
  private void checkSet(String set, Integer indent, File javac, int size) throws IOException {
    StringInputStream is = new StringInputStream(set);
    OptionMapLoader loader = new OptionMapLoader(is);
    DefaultOptionMap map = new DefaultOptionMap();
    loader.loadInto(map);
    assertEquals("indent (integer) option",  map.getOption(INDENT_LEVEL),indent);
    assertEquals("JAVAC", map.getOption(JAVAC_LOCATION),javac.getAbsoluteFile());
    assertEquals("size of extra-classpath vector", Integer.valueOf(size), 
                 Integer.valueOf(map.getOption(EXTRA_CLASSPATH).size()));
  }
  
  public void testEmptyConfigSet() throws IOException {
    checkSet("",INDENT_LEVEL.getDefault(), JAVAC_LOCATION.getDefault(),  EXTRA_CLASSPATH.getDefault().size());
  }
}
