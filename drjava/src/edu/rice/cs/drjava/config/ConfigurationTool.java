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
//import java.io.File;
//import java.io.IOException;
//import java.util.Vector;
//import java.awt.Color;
//import edu.rice.cs.drjava.DrJava;

/**
 * Interface which sets up the global configuration object at runtime.
 * 
 * NOTE: Not used any more.  DrJava.java now handles all setup of
 * the config object, because it must be delayed until the user can
 * specify a custom config file.
 * 
 * @version $Id$
 */
public interface ConfigurationTool {
  
  // STATIC VARIABLES
  
  // Not used any more:
  //  DrJava.java now creates the config object after determining if the
  //  user has specified a custom config file.
  
//  /**
//   * The ".drjava" file in the user's home directory.
//   */
//  public static final File PROPERTIES_FILE =  
//    new File(System.getProperty("user.home"), ".drjava");  
//  
//  /**
//   * The global Configuration object to use for all configurable options.
//   */
//  public static final FileConfiguration CONFIG = new DefaultFileConfig().evaluate();
}

/**
 * Generate the CONFIG object separately to appease the almighty Javadoc.
 * (It didn't like anonymous inner classes with generics in interfaces in Java 1.3.)
 */
//class DefaultFileConfig {
//  public FileConfiguration evaluate() {
//    try { 
//      ConfigurationTool.PROPERTIES_FILE.createNewFile(); 
//      // be nice and ensure a config file 
//    } 
//    catch(IOException e) { // IOException occurred 
//    } 
//    FileConfiguration config = 
//      new FileConfiguration(ConfigurationTool.PROPERTIES_FILE); 
//    try { 
//      config.loadConfiguration(); 
//    }
//    catch (Exception e) {
//      // problem parsing the config file.
//      // Use defaults and remember what happened (for the UI)
//      config.resetToDefaults();
//      config.storeStartupException(e);
//    }
//    return config;
//  }
//}


