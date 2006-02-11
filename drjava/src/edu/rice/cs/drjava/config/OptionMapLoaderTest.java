/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.config;
import junit.framework.TestCase;
import java.io.*;

import edu.rice.cs.drjava.DrJavaTestCase;

/**
 * Tests the OptionMapLoader.
 * @version $Id$
 */
public final class OptionMapLoaderTest extends DrJavaTestCase
  implements OptionConstants
{
  
  public OptionMapLoaderTest(String s) {
    super(s);
  }
  
  public static class StringInputStream extends ByteArrayInputStream {
    public StringInputStream(String s) {
      super(s.getBytes());
    }
  }
  
  /** an artificially created properties "file" **/
  public static final String OPTION_DOC = 
    "# this is a fake header\n"+
    "this.is.a.real.key = value\n"+
    "indent.level = 1\n"+
    "javac.location = foo\n"+
    "extra.classpath = bam\n\n";
  
  public void testProperConfigSet() throws IOException {
    checkSet(OPTION_DOC,new Integer(1), new File("foo"), 1);
  }
  
  private void checkSet(String set, Integer indent, File javac, int size) throws IOException {
    StringInputStream is = new StringInputStream(set);
    OptionMapLoader loader = new OptionMapLoader(is);
    DefaultOptionMap map = new DefaultOptionMap();
    loader.loadInto(map);
    assertEquals("indent (integer) option",  map.getOption(INDENT_LEVEL),indent);
    assertEquals("JAVAC", map.getOption(JAVAC_LOCATION),javac.getAbsoluteFile());
    assertEquals("size of extra-classpath vector", new Integer(size), 
                 new Integer(map.getOption(EXTRA_CLASSPATH).size()));
  }
  
  public void testEmptyConfigSet() throws IOException {
    checkSet("",INDENT_LEVEL.getDefault(), JAVAC_LOCATION.getDefault(),  EXTRA_CLASSPATH.getDefault().size());
  }
}
