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

package edu.rice.cs.drjava;

import edu.rice.cs.drjava.config.FileConfiguration;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.plt.io.IOUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Tests that a custom config file can be specified.
 * @version $Id: ConfigFileTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class ConfigFileTest extends DrJavaTestCase {
  private static final String CUSTOM_PROPS =
    "indent.level = 5\n" +
    "history.max.size = 1\n" +
    "definitions.keyword.color = #0000ff\n";
  
  
  /** Constructor. */
  public ConfigFileTest(String name) { super(name); }
  
  /** Creates a custom properties file, tells DrJava to use it, and checks that it is being used. */
  public void testCustomConfigFile() throws IOException {
    final File propsFile = IOUtil.createAndMarkTempFile("DrJavaProps", ".txt");
    IOUtil.writeStringToFile(propsFile, CUSTOM_PROPS);
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        DrJava.setPropertiesFile(propsFile.getAbsolutePath());
        DrJava._initConfig(); 
      } 
    });
//    Utilities.clearEventQueue();
//    Utilities.clearEventQueue();
    
    FileConfiguration config = DrJava.getConfig();
    assertEquals("custom indent level", 5, config.getSetting(OptionConstants.INDENT_LEVEL).intValue());
    assertEquals("custom history size", 1, config.getSetting(OptionConstants.HISTORY_MAX_SIZE).intValue());
    //Tests if a user can put a default value in the .drjava file
    assertEquals("definitions.keyword.color", Color.blue, config.getSetting(OptionConstants.DEFINITIONS_KEYWORD_COLOR));
    assertEquals("default javac location", OptionConstants.JAVAC_LOCATION.getDefault(),
                 config.getSetting(OptionConstants.JAVAC_LOCATION));
  }
}