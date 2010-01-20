/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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
  
  /** The OptionListener for the Warning Options
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
    if (warningsEnabled) {
      if (SHOW_UNCHECKED) {
        options.put("-Xlint:unchecked","");
      }
      
      if (SHOW_DEPRECATION) {
        options.put("-Xlint:deprecation","");
      }
      
      if (SHOW_PATH) {
        options.put("-Xlint:path","");
      }
      
      if (SHOW_SERIAL) {
        options.put("-Xlint:serial","");
      }
      
      if (SHOW_FINALLY) {
        options.put("-Xlint:finally","");
      }
      
      if (SHOW_FALLTHROUGH) {
        options.put("-Xlint:fallthrough","");
        options.put("-Xlint:switchcheck",""); //Some compilers appear to use this option instead. Anyone know anything about this?
      }
    }
    
    //Add any other options we want to add to the compiler in the future
    return options;
  }
}