/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

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

package koala.dynamicjava.util;

import junit.framework.*;
import java.util.List;
import java.lang.reflect.*;

/**
 * Test cases for {@link ImportationManager}.
 *
 * @version $Id$
 */
public class ImportationManagerTest extends TestCase {
  private ImportationManager im;
  
  protected void setUp(){
     im = new ImportationManager(ClassLoader.getSystemClassLoader());
  }
  
  public void testHasSuffix(){
    assertFalse("'java.lang.System' should not have the \"suffix\" 'lang.System'", 
                im.hasSuffix("java.lang.System", "lang.System"));
    assertFalse("'java.lang.System' should not have the suffix 'java.lang.System'", 
                im.hasSuffix("java.lang.System", "java.lang.System"));
    assertFalse("'lang.System' should not have the suffix 'lang.System'", 
                im.hasSuffix("lang.System", "lang.System"));
    assertFalse("'lang.System' should not have the suffix 'java.lang.System'", 
                im.hasSuffix("lang.System", "java.lang.System"));
    assertFalse("'java.lang.System' should not have the suffix 'lang'", 
                im.hasSuffix("java.lang.System", "lang"));
    assertTrue("'java.lang.System' should have the suffix 'System'", 
               im.hasSuffix("java.lang.System", "System"));
    assertTrue("'System' should have the suffix 'System'", 
               im.hasSuffix("System", "System"));
  }
  
  public void testFindInnerClass() throws ClassNotFoundException {
    Class result;
    //try {
    result = im.findInnerClass("java.util.Map.Entry");
    assertEquals("findInnerClass() for java.util.Map.Entry should return the java.util.Map$Entry Class", 
                 "java.util.Map$Entry", 
                 result.getName()); 
    //}
    //catch(ClassNotFoundException e){
    //  fail("Cannot find java.util.Map.Entry");
    //}
    
    try {
      result = im.findInnerClass("java.util.Max.Entry");
      fail("findInnerClass() for java.util.Max.Entry should not find the Class " + result.getName() + 
           " and should instead raise a ClassNotFoundException."); 
    }
    catch(ClassNotFoundException e){} // do nothing. Exception expected.
    
    try {
      result = im.findInnerClass("java.util.Map");
      fail("findInnerClass() for java.util.Map should not find the Class " + result.getName() + 
           " and should instead raise a ClassNotFoundException."); 
    }
    catch(ClassNotFoundException e){} // do nothing. Exception expected
    
    // Having java.lang automatically imported and having the class to be in java.lang is
    //  of no help. The class name has to be fully qualified.
    try {
      result = im.findInnerClass("Character.Subset"); 
      fail("findInnerClass() for Character.Subset should not find the Class " + result.getName() + 
           " and should instead raise a ClassNotFoundException.");
    }
    catch(ClassNotFoundException e){} // do nothing. Exception expected
    
    try {
      result = im.findInnerClass("Character");
      fail("findInnerClass() for Character should not find the Class " + result.getName() + 
           " and should instead of raise a ClassNotFoundException."); 
    }
    catch(ClassNotFoundException e){} // do nothing. Exception expected
    
  }
  
  public void testGetOuterNames(){
    List<String> l;
    
    l = im.getOuterNames("a.b.c$d.e.f$h");
    assertEquals("List of outer names for a.b.c$d.e.f$h should be [d.e.f, c]", 
                 "[d.e.f, c]", 
                 l.toString());
    
    l = im.getOuterNames("a.b.c.d.e.f$h");
    assertEquals("List of outer names for a.b.c.d.e.f$h should be [f]", 
                 "[f]", 
                 l.toString());

    l = im.getOuterNames("java.lang.Character$Subset");
    assertEquals("List of outer names for java.lang.Character$Subset should be [Character]", 
                 "[Character]", 
                 l.toString());
    
    l = im.getOuterNames("java.lang.Character.Subset");
    assertEquals("List of outer names for java.lang.Character.Subset should be []", 
                 "[]", 
                 l.toString());

    l = im.getOuterNames("a.b.c.d.e.f$");
    assertEquals("List of outer names for a.b.c.d.e.f$ should be [f]", 
                 "[f]", 
                 l.toString());

    l = im.getOuterNames("$a.b.c.d.e.f");
    assertEquals("List of outer names for $a.b.c.d.e.f should be []", 
                 "[]", 
                 l.toString());

    l = im.getOuterNames("$a.b.c.d.e.f$");
    assertFalse("List of outer names for $a.b.c.d.e.f$ should not be not [f]", 
                "[f]".equals(l.toString()));
    assertEquals("List of outer names for $a.b.c.d.e.f$ should be [a.b.c.d.e.f, ]", 
                 "[a.b.c.d.e.f, ]", 
                 l.toString());
  }
  
  public void testDeclarePackageImport(){
    List<String> l = im.getImportOnDemandClauses();

    // NOTE: l already has "java.lang" added by the constructor
    assertEquals("'java.lang' should be the first element in the importOnDemandClauses list", 
                 "java.lang", 
                 l.get(0));
    assertEquals("The size of importOnDemandClauses list to be 1", 
                 1, 
                 l.size());
    
    im.declarePackageImport("java.util");
    assertEquals("Calling declarePackageImport(\"java.util\") should get 'java.util' to be the first" +
                 " element in the importOnDemandClauses list", 
                 "java.util", 
                 l.get(0));
    assertEquals("Calling declarePackageImport(\"java.util\") should get 'java.lang' to be the second" +
                 " element in the importOnDemandClauses list", 
                 "java.lang", 
                 l.get(1));
    assertEquals("Calling declarePackageImport(\"java.util\") should get the size of importOnDemandClauses" +
                 " list to be 2", 
                 2, 
                 l.size());

    im.declarePackageImport("foo");
    assertEquals("Calling declarePackageImport(\"foo\") should get 'foo' to be the first element in" +
                 " the importOnDemandClauses list", 
                 "foo", 
                 l.get(0));
    assertFalse("Calling declarePackageImport(\"foo\") should not get 'java.lang' to be the second" + 
                " element in the importOnDemandClauses list", 
                "java.lang".equals(l.get(1)));
    assertEquals("Calling declarePackageImport(\"java.util\") again should keep the size of" +
                 " importOnDemandClauses list to be 3", 
                 3, 
                 l.size());

    im.declarePackageImport("foo"); // again
    assertEquals("Calling declarePackageImport(\"foo\") again should leav 'foo' as the first element" +
                 " in the importOnDemandClauses list", 
                 "foo", 
                 l.get(0));
    assertEquals("Calling declarePackageImport(\"foo\") again should keep the size of importOnDemandClauses" +
                 " list to be 3", 
                 3, 
                 l.size());
    assertEquals("Calling declarePackageImport(\"foo\") again should get 'java.util' to be the second" +
                 " element in the importOnDemandClauses list", 
                 "java.util", 
                 l.get(1));
    
    im.declarePackageImport("java.util"); // again
    assertEquals("Calling declarePackageImport(\"java.util\") again should get 'java.util' to be the" +
                 " first element in the importOnDemandClauses list", 
                 "java.util", 
                 l.get(0));
    assertEquals("Calling declarePackageImport(\"java.util\") again should keep the size of" +
                 " importOnDemandClauses list to be 3", 
                 3, 
                 l.size());
    assertEquals("Calling declarePackageImport(\"java.util\") again should get 'foo' to be the second" +
                 " element in the importOnDemandClauses list", 
                 "foo", 
                 l.get(1));
  }
  
  public void testDeclareClassImport() throws ClassNotFoundException {
    List<String> l = im.getSingleTypeImportClauses();
    
    try {
      im.declareClassImport("java.lang.Foo");
      fail("Calling declareClassImport(\"java.lang.Foo\") should have not load the class. There is no such class");
    }
    catch(ClassNotFoundException e){} // do nothing. Exception expected
    // ... it should, however, add it to the singleTypeImportClauses list (because of
    // the 'finally' stmt in the method (Read JLSv.2, Ch.14, for how an exception in a catch is handled!)
    assertEquals("The first element of the singleTypeImportClauses list should be 'java.lang.Foo'", 
                 "java.lang.Foo", 
                 l.get(0));
    
    im.declareClassImport("java.lang.System");
    assertEquals("Calling declareClassImport(\"java.lang.System\") should get the size of singleTypeImportClauses" +
                 " list to be 2",
                  2,
                 l.size());
  }
  
  public void testLookupClass(){
    // many test conditions have to be tested. Not less than 15 different cases.
    
  }
  
  public void testDeclareClassImportNullBug(){
  }

  public void testClone() {
    ImportationManager im2 = (ImportationManager)im.clone();
    assertNotSame("im and im2 should not be the same", im, im2);
    
    im = im2; // let im reference the clone. The testSuite uses im
    assertSame("im and im2 should be the same", im, im2);
    
    // so far has problems in calling run() or runBare() ... what's the difference? and what's the reason?
    //run(); // given that im == im2 it should succeed (if it succeeds initially for im)

    // im.declarePackageImport("...") to let run() fail, and learn how to catch its 'failure'
    //run(); // given that im == im2
  }
  
  
  
  public void testDeclareClassStaticImport() {
    List<String> l = im.getImportOnDemandStaticClauses();
    assertEquals("Initial list of staticly imported classes should be empty",0,l.size());
    
    try {
      im.declareClassStaticImport("java.lang.Math");
      assertEquals("List should contain java.lang.Math",1,l.size());
      assertEquals("List should contain java.lang.Math","java.lang.Math",l.get(0));
      
      im.declareClassStaticImport("java.lang.Integer");
      assertEquals("List should contain java.lang.Integer",2,l.size());
      assertEquals("First element of list should be java.lang.Integer","java.lang.Integer",l.get(0));
    } 
    catch(ClassNotFoundException e) {
      fail("Should have found classes java.lang.Math and java.lang.Integer");
    }
    
    try {
      im.declareClassStaticImport("java.lang.Foo");
      fail("Calling declareClassStaticImport(\"java.lang.Foo\") should have failed. There is no such class");
    }
    catch(ClassNotFoundException e){} // do nothing. Exception expected
    
    
    //Make sure that package list hast not changed
    assertEquals("Package list should not have changed while importing classes with static import",1,im.getImportOnDemandClauses().size());
  } 
  
  
  
  public void testDeclareMemberStaticImport() {
    List<Field> fields = im.getSingleTypeImportStaticFieldClauses();
    List<Method> methods = im.getSingleTypeImportStaticMethodClauses();
    List<String> classes = im.getSingleTypeImportClauses();
    
    assertEquals("Initial list of staticly imported fields should be empty",0,fields.size());
    assertEquals("Initial list of staticly imported methods should be empty",0,methods.size());
    assertEquals("Initial list of staticly imported inner classes should be empty",0,classes.size());
    
    
    try {
      im.declareMemberStaticImport("java.lang.Integer.MAX_VALUE");
    }
    catch(ClassNotFoundException e) {
      fail("Class java.lang.Integer should be found");
    }
    assertEquals("List of staticly imported methods should not have changed",0,methods.size());
    assertEquals("List of staticly imported inner classes should not have changed",0,classes.size());
    assertEquals("List of staticly imported fields should contain one imported field",1,fields.size());
    assertEquals("List of staticly imported fields should contain java.lang.Integer.MAX_VALUE","MAX_VALUE",fields.get(0).getName());
    assertEquals("List of staticly imported fields should contain java.lang.Integer.MAX_VALUE","public static final int java.lang.Integer.MAX_VALUE",fields.get(0).toString());
    
    
    try {
      im.declareMemberStaticImport("java.lang.Integer.parseInt");
    }
    catch(ClassNotFoundException e) {
      fail("Class java.lang.Integer should be found");
    }
    assertEquals("List of staticly imported inner classes should not have changed",0,classes.size());
    assertEquals("List of staticly imported fields should not have changed",1,fields.size());
    assertEquals("List of staticly imported methods should contain both parseint methods",2,methods.size());
    assertEquals("List of staticly imported fields should contain java.lang.Integer.MAX_VALUE","public static final int java.lang.Integer.MAX_VALUE",fields.get(0).toString());
    if(TigerUtilities.isTigerEnabled()) {      
      assertEquals("List of staticly imported fields should contain both parseInt methods",
                   "public static int java.lang.Integer.parseInt(java.lang.String) throws java.lang.NumberFormatException",methods.get(0).toString());
      assertEquals("List of staticly imported fields should contain both parseInt methods",
                   "public static int java.lang.Integer.parseInt(java.lang.String,int) throws java.lang.NumberFormatException",methods.get(1).toString());
    }
    else {
      //When running 1.4, the methods returned by java.lang.reflect.Class.getMethods() are in the array in reverse order from the way they are returned in 1.5.
      //This will not affect the program but does affect these assertions
      assertEquals("List of staticly imported fields should contain both parseInt methods",
                   "public static int java.lang.Integer.parseInt(java.lang.String,int) throws java.lang.NumberFormatException",methods.get(0).toString());
      assertEquals("List of staticly imported fields should contain both parseInt methods",
                   "public static int java.lang.Integer.parseInt(java.lang.String) throws java.lang.NumberFormatException",methods.get(1).toString());
    }
      
    
    
    //**//Note, all static inner classes imported with "import static" have to be added to the list twice, once with a '.' and once with a $. 
    //The first class in the list which successfully works is the one used when the user actually instantiates the class, and both are needed because 
    //Different methods require different formats, and having both can't hurt anything. Any of the methods that use the list of classes try and catch through the
    //list until they come across a class that fits
    try {
      im.declareMemberStaticImport("javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag");
    }
    catch(ClassNotFoundException e) {
      fail("Class javax.security.auth.login.AppConfigurationEntry should be found");
    }
    assertEquals("List of staticly imported fields should not have changed",1,fields.size());
    assertEquals("List of staticly imported methods should not have changed",2,methods.size());
    assertEquals("List of staticly imported inner classes should contain the AppConfigurationEntry$LoginModuleControlFlag class",2,classes.size());
    assertEquals("List of staticly imported inner classes should contain the AppConfigurationEntry.LoginModuleControlFlag class",
                 "javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag",classes.get(0));
    assertEquals("List of staticly imported inner classes should contain the AppConfigurationEntry$LoginModuleControlFlag class",
                 "javax.security.auth.login.AppConfigurationEntry$LoginModuleControlFlag",classes.get(1));
    
    
    try {
      im.declareMemberStaticImport("javax.swing.plaf.basic.BasicOptionPaneUI.ButtonAreaLayout");
    }
    catch(ClassNotFoundException e) {
      fail("Class javax.swing.plaf.basic.BasicOptionPaneUI should be found");
    }
    assertEquals("List of staticly imported fields should not have changed",1,fields.size());
    assertEquals("List of staticly imported methods should not have changed",2,methods.size());
    assertEquals("List of staticly imported inner classes should contain the BasicOptionPaneUI$ButtonAreaLayout class",4,classes.size());
    assertEquals("List of staticly imported inner classes should contain the BasicOptionPaneUI.ButtonAreaLayout class as its first entry",
                 "javax.swing.plaf.basic.BasicOptionPaneUI.ButtonAreaLayout",classes.get(0));
    assertEquals("List of staticly imported inner classes should contain the BasicOptionPaneUI$ButtonAreaLayout class as its first entry",
                 "javax.swing.plaf.basic.BasicOptionPaneUI$ButtonAreaLayout",classes.get(1));
    
    
    try {
      im.declareMemberStaticImport("java.lang.Integer.toString");
    }
    catch(ClassNotFoundException e) {
      fail("Class java.lang.Integer should be found");
    }
    assertEquals("List of staticly imported fields should not have changed",1,fields.size());
    assertEquals("List of staticly imported inner classes should not have changed",4,classes.size());
    assertEquals("List of staticly imported methods should contain the two static toString methods in the Integer class",4,methods.size());
    if(TigerUtilities.isTigerEnabled()) {
      assertEquals("List of staticly imported methods should contain the two static toString methods in the Integer class as its first two entries",
                   "public static java.lang.String java.lang.Integer.toString(int)",methods.get(0).toString());
      assertEquals("List of staticly imported methods should contain the two static toString methods in the Integer class as its first two entries",
                   "public static java.lang.String java.lang.Integer.toString(int,int)",methods.get(1).toString());
    }
    else {
      //When running 1.4, the methods returned by java.lang.reflect.Class.getMethods() are in the array in reverse order from the way they are returned in 1.5.
      //This will not affect the program but does affect these assertions
      assertEquals("List of staticly imported methods should contain the two static toString methods in the Integer class as its first two entries",
                   "public static java.lang.String java.lang.Integer.toString(int,int)",methods.get(0).toString());
      assertEquals("List of staticly imported methods should contain the two static toString methods in the Integer class as its first two entries",
                   "public static java.lang.String java.lang.Integer.toString(int)",methods.get(1).toString());
    }
  }
  
}