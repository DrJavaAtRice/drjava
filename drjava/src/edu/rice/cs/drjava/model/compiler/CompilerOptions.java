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

package edu.rice.cs.drjava.model.compiler;

import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.DrJava;

import java.util.HashMap;

/** Represents the compiler warnings */

public class CompilerOptions implements OptionConstants {
  
  private static boolean SHOW_UNCHECKED = DrJava.getConfig().getSetting(SHOW_UNCHECKED_WARNINGS);
  private static boolean SHOW_DEPRECATION = DrJava.getConfig().getSetting(SHOW_DEPRECATION_WARNINGS);
  private static boolean SHOW_PATH = DrJava.getConfig().getSetting(SHOW_PATH_WARNINGS);
  private static boolean SHOW_SERIAL = DrJava.getConfig().getSetting(SHOW_SERIAL_WARNINGS);
  private static boolean SHOW_FINALLY = DrJava.getConfig().getSetting(SHOW_FINALLY_WARNINGS);
  private static boolean SHOW_FALLTHROUGH = DrJava.getConfig().getSetting(SHOW_FALLTHROUGH_WARNINGS);
   
  private static WarningOptionListener wol = new WarningOptionListener();
  
  /**
   * The OptionListener for the Warning Options
   */
  private static class WarningOptionListener implements OptionListener<Boolean> {
    public void optionChanged(OptionEvent<Boolean> oce) {
      updateWarnings();
    }
  }
  
  public static void updateWarnings() {
    SHOW_UNCHECKED = DrJava.getConfig().getSetting(SHOW_UNCHECKED_WARNINGS);
    SHOW_DEPRECATION = DrJava.getConfig().getSetting(SHOW_DEPRECATION_WARNINGS);
    SHOW_PATH = DrJava.getConfig().getSetting(SHOW_PATH_WARNINGS);
    SHOW_SERIAL = DrJava.getConfig().getSetting(SHOW_SERIAL_WARNINGS);
    SHOW_FINALLY = DrJava.getConfig().getSetting(SHOW_FINALLY_WARNINGS);
    SHOW_FALLTHROUGH = DrJava.getConfig().getSetting(SHOW_FALLTHROUGH_WARNINGS);
  }
  
  
  static {
    DrJava.getConfig().addOptionListener( OptionConstants.SHOW_UNCHECKED_WARNINGS, wol);
    DrJava.getConfig().addOptionListener( OptionConstants.SHOW_DEPRECATION_WARNINGS, wol);
    DrJava.getConfig().addOptionListener( OptionConstants.SHOW_PATH_WARNINGS, wol);
    DrJava.getConfig().addOptionListener( OptionConstants.SHOW_SERIAL_WARNINGS, wol);
    DrJava.getConfig().addOptionListener( OptionConstants.SHOW_FINALLY_WARNINGS, wol);
    DrJava.getConfig().addOptionListener( OptionConstants.SHOW_FALLTHROUGH_WARNINGS, wol);    
  }
  
  public static HashMap<String,String> getOptions(boolean warningsEnabled) {    
    HashMap<String,String> options = new HashMap<String,String>();
    if(warningsEnabled) {
      if(SHOW_UNCHECKED) {
        options.put("-Xlint:unchecked","");
      }
      
      if(SHOW_DEPRECATION) {
        options.put("-Xlint:deprecation","");
      }
      
      if(SHOW_PATH) {
        options.put("-Xlint:path","");
      }
      
      if(SHOW_SERIAL) {
        options.put("-Xlint:serial","");
      }
      
      if(SHOW_FINALLY) {
        options.put("-Xlint:finally","");
      }
      
      if(SHOW_FALLTHROUGH) {
        options.put("-Xlint:fallthrough","");
        options.put("-Xlint:switchcheck",""); //Some compilers appear to use this option instead. Anyone know anything about this?
      }
    }
    
    //Add any other options we want to add to the compiler in the future
    return options;
  }
}