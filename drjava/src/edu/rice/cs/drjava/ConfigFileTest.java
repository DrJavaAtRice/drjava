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

package edu.rice.cs.drjava;

import junit.framework.*;

import java.io.File;
import java.io.IOException;
import java.awt.Color;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;

/**
 * Tests that a custom config file can be specified.
 * @version $Id$
 */
public final class ConfigFileTest extends TestCase {
  private static final String CUSTOM_PROPS =
    "indent.level = 5\n" +
    "history.max.size = 1\n" +
    "definitions.keyword.color = #0000ff\n";
  
  
  /** Constructor. */
  public ConfigFileTest(String name) { super(name); }
  
  /** Creates a custom properties file, tells DrJava to use it, and checks that it is being used. */
  public void testCustomConfigFile() throws IOException {
    File propsFile = FileOps.writeStringToNewTempFile("DrJavaProps", ".txt", CUSTOM_PROPS);
    propsFile.deleteOnExit();
    DrJava.setPropertiesFile(propsFile.getAbsolutePath());
    DrJava._initConfig();
    FileConfiguration config = DrJava.getConfig();
    
    assertEquals("custom indent level", 5, config.getSetting(OptionConstants.INDENT_LEVEL).intValue());
    assertEquals("custom history size", 1, config.getSetting(OptionConstants.HISTORY_MAX_SIZE).intValue());
    //Tests if a user can put a default value in the .drjava file
    assertEquals("definitions.keyword.color", Color.blue, config.getSetting(OptionConstants.DEFINITIONS_KEYWORD_COLOR));
    assertEquals("default javac location", OptionConstants.JAVAC_LOCATION.getDefault(),
                 config.getSetting(OptionConstants.JAVAC_LOCATION));
  }
}