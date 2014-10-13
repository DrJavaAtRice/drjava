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

import edu.rice.cs.drjava.DrJavaTestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

/** JUnit test class for testing SavableConfiguration.
  * @author <a href="mailto:chrisw@rice.edu">Chris Warrington</a>
  * @version $Id: SavableConfigurationTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class SavableConfigurationTest extends DrJavaTestCase {
  /** This is the date format the Date.toString() uses.
   */
  SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
  
  ByteArrayOutputStream outputBytes = null;
  
  public void setUp() throws Exception {
    super.setUp();
    outputBytes = new ByteArrayOutputStream();
  }
  
  /** Tests the saveConfiguration method with no configuration data
   */
  public void testEmptyConfiguration() throws IOException {  
    SavableConfiguration emptyConfig = new SavableConfiguration(new DefaultOptionMap());
    
    emptyConfig.saveConfiguration(outputBytes, "header");
    
    String outputString = outputBytes.toString();
    String[] lines = outputString.split(System.getProperty("line.separator"));
    
    assertTrue("Data exists", outputString.length() > 0);
    assertEquals("Number of lines", 2, lines.length);
    assertEquals("Starts with \"#header\"", "#header", lines[0]);
    try {
      //get the date without the #
      String bareDate = lines[1].substring(1, lines[1].length());
      Date readInDate = dateFormat.parse(bareDate);
      assertTrue("Embedded date less than now",
                 readInDate.compareTo(new Date()) <= 0);
    }
    catch (ParseException pe) {
      fail("Could not parse second line into a date."); 
    }
  }
  
  /** Tests the saveConfiguration method with some configuration data.
   */
  public void testNonEmptyConfiguration() throws IOException {
    DefaultOptionMap optionsMap = new DefaultOptionMap();
    optionsMap.setOption(new BooleanOption("tests_are_good", false), true);
    optionsMap.setOption(new IntegerOption("meaning_of_life", 0), 42);
    optionsMap.setOption(new StringOption("yay_strings", "hello?"), "goodbye");
    
    SavableConfiguration nonEmptyConfig = new SavableConfiguration(optionsMap);
    
    nonEmptyConfig.saveConfiguration(outputBytes, "header");                 
    
    String outputString = outputBytes.toString();
    String[] lines = outputString.split(System.getProperty("line.separator"));

    assertTrue("Data exists", outputString.length() > 0);
    assertEquals("Number of lines", 5, lines.length);
    assertEquals("Starts with \"#header\"", "#header", lines[0]);
    try {
      //get the date without the #
      String bareDate = lines[1].substring(1, lines[1].length());
      Date readInDate = dateFormat.parse(bareDate);
      assertTrue("Embedded date less than now",
                 readInDate.compareTo(new Date()) <= 0);
    }
    catch (ParseException pe) {
      fail("Could not parse second line into a date."); 
    }
    assertEquals("BooleanOption", "tests_are_good = true", lines[2]);
    assertEquals("IntegerOption", "meaning_of_life = 42", lines[3]);
    assertEquals("StringOption", "yay_strings = goodbye", lines[4]);
  }
}