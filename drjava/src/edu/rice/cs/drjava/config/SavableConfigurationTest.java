/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.config;

import edu.rice.cs.drjava.DrJavaTestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

/** JUnit test class for testing SavableConfiguration.
  *
  *  @author <a href="mailto:chrisw@rice.edu">Chris Warrington</a>
  *  @version $Id: $
  */
public class SavableConfigurationTest extends DrJavaTestCase {
  /**
   * This is the date format the Date.toString() uses.
   */
  SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
  
  ByteArrayOutputStream outputBytes = null;
  
  public void setUp() throws Exception {
    super.setUp();
    outputBytes = new ByteArrayOutputStream();
  }
  
  /**
   * Tests the saveConfiguration method with no configuration data
   */
  public void testEmptyConfiguration() throws IOException {  
    SavableConfiguration emptyConfig = new SavableConfiguration(new DefaultOptionMap());
    
    emptyConfig.saveConfiguration(outputBytes, "header");
    
    String outputString = outputBytes.toString();
    String[] lines = outputString.split("\n");
    
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
  
  /**
   * Tests the saveConfiguration method with some configuration data.
   */
  public void testNonEmptyConfiguration() throws IOException {
    DefaultOptionMap optionsMap = new DefaultOptionMap();
    optionsMap.setOption(new BooleanOption("tests_are_good", false), true);
    optionsMap.setOption(new IntegerOption("meaning_of_life", 0), 42);
    optionsMap.setOption(new StringOption("yay_strings", "hello?"), "goodbye");
    
    SavableConfiguration nonEmptyConfig = new SavableConfiguration(optionsMap);
    
    nonEmptyConfig.saveConfiguration(outputBytes, "header");                 
    
    String outputString = outputBytes.toString();
    String[] lines = outputString.split("\n");

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