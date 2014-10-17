package edu.rice.cs.drjava.ui;

import javax.swing.JMenu;
import edu.rice.cs.drjava.model.AbstractGlobalModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Tests for NewNewJavaClassDialog.
  * @version $Id: NewJavaClassTest.java 5242 2010-04-29 14:46:13Z mgricken $
  */
public class NewJavaClassTest extends TestCase {
  public void testcheckClassNameGoodNames(){
    assertTrue("wrong class name", NewJavaClassDialog.checkClassName("Abcd"));
    assertTrue("wrong class name", NewJavaClassDialog.checkClassName("abcd"));
    assertTrue("wrong class name", NewJavaClassDialog.checkClassName("_abcd"));
    //if the space is at the beginning or at the end, it will trim it, so, it is good name
    assertTrue("wrong class name", NewJavaClassDialog.checkClassName(" abcd"));  
    assertTrue("wrong class name", NewJavaClassDialog.checkClassName(" abcd "));  
  }
  
  public void testcheckClassNameBadNames(){
    assertFalse("wrong class name", NewJavaClassDialog.checkClassName("(Abcd"));
    assertFalse("wrong class name", NewJavaClassDialog.checkClassName("%Abcd"));
    assertFalse("wrong class name", NewJavaClassDialog.checkClassName("^Abcd"));
    assertFalse("wrong class name", NewJavaClassDialog.checkClassName("#Abcd"));
    assertFalse("wrong class name", NewJavaClassDialog.checkClassName("_Ab%cd"));
    assertFalse("wrong class name", NewJavaClassDialog.checkClassName("_Abc d"));
    assertFalse("wrong class name", NewJavaClassDialog.checkClassName("5Abcd"));
    assertFalse("wrong class name", NewJavaClassDialog.checkClassName("$Abc d"));
    assertFalse("wrong class name", NewJavaClassDialog.checkClassName("Abc.d"));
    assertFalse("wrong class name", NewJavaClassDialog.checkClassName("Abc.d.Ef"));
    assertFalse("wrong class name", NewJavaClassDialog.checkClassName(""));
    assertFalse("wrong class name", NewJavaClassDialog.checkClassName(null));
  }
  
  public void testcheckSuperClassNameGoodNames(){
    assertTrue("wrong class name", NewJavaClassDialog.checkSuperClassName("Abcd"));
    assertTrue("wrong class name", NewJavaClassDialog.checkSuperClassName("abcd"));
    assertTrue("wrong class name", NewJavaClassDialog.checkSuperClassName("_abcd"));
    //if the space is at the beginning or at the end, it will trim it, so, it is good name
    assertTrue("wrong class name", NewJavaClassDialog.checkSuperClassName(" abcd"));  
    assertTrue("wrong class name", NewJavaClassDialog.checkSuperClassName(" abcd "));
    assertTrue("wrong class name", NewJavaClassDialog.checkSuperClassName(" ab.cd "));
    assertTrue("wrong class name", NewJavaClassDialog.checkSuperClassName(" ab.cd.ef "));
  }
  
  public void testcheckSuperClassNameBadNames(){
    assertFalse("wrong class name", NewJavaClassDialog.checkSuperClassName("(Abcd"));
    assertFalse("wrong class name", NewJavaClassDialog.checkSuperClassName("%Abcd"));
    assertFalse("wrong class name", NewJavaClassDialog.checkSuperClassName("^Abcd"));
    assertFalse("wrong class name", NewJavaClassDialog.checkSuperClassName("#Abcd"));
    assertFalse("wrong class name", NewJavaClassDialog.checkSuperClassName("_Ab%cd"));
    assertFalse("wrong class name", NewJavaClassDialog.checkSuperClassName("_Abc d"));
    assertFalse("wrong class name", NewJavaClassDialog.checkSuperClassName("5Abcd"));
    assertFalse("wrong class name", NewJavaClassDialog.checkSuperClassName("$Abc d"));
    assertFalse("wrong class name", NewJavaClassDialog.checkSuperClassName(""));
    assertFalse("wrong class name", NewJavaClassDialog.checkSuperClassName(null));
  }
  
  public void testInterfacesNamingConventionsCorrectNames() {
    assertTrue("Correct Interfaces Name", 
               NewJavaClassDialog.checkInterfaceNames("interfaces"));
    assertTrue("Correct Interfaces Name", 
               NewJavaClassDialog.checkInterfaceNames("interfaces, interfaceme"));
    assertTrue("Correct Interfaces Name", 
               NewJavaClassDialog.checkInterfaceNames("interfaces1, interfaceme123"));
    assertTrue("Correct Interfaces Name", 
               NewJavaClassDialog.checkInterfaceNames("int12erfaces1 , interfaceme123 , goodName"));
    assertTrue("Correct Interfaces Name", 
               NewJavaClassDialog.checkInterfaceNames("int12erfaces1 ,interfaceme123,goodName"));
    assertTrue("Correct Interfaces Name", 
               NewJavaClassDialog.checkInterfaceNames("inter.faces"));
    assertTrue("Correct Interfaces Name", 
               NewJavaClassDialog.checkInterfaceNames("interfaces, interf.aceme"));
    assertTrue("Correct Interfaces Name", 
               NewJavaClassDialog.checkInterfaceNames("interfaces1, interf.ace.me123"));
    assertTrue("Correct Interfaces Name", 
               NewJavaClassDialog.checkInterfaceNames("int12erfaces1 , interfaceme123 , good.Name"));
    assertTrue("Correct Interfaces Name", 
               NewJavaClassDialog.checkInterfaceNames("int12.erfaces1 ,interf.aceme123,good.Name"));
  }
  
  public void testInterfacesNamingConventionsWrongNames() {
    assertFalse("Wrong Interfaces Name", 
                NewJavaClassDialog.checkInterfaceNames("interf aces")); 
    assertFalse("Wrong Interfaces Name", 
                NewJavaClassDialog.checkInterfaceNames("interfaces; inter"));
    assertFalse("Wrong Interfaces Name", 
                NewJavaClassDialog.checkInterfaceNames("interfaces, &inter"));
    assertFalse("Wrong Interfaces Name", 
                NewJavaClassDialog.checkInterfaceNames("interfaces1, interfaceme123 , "));
  }
  
  public void testgetCapitalizedClassNameUpperCase() {
    String name = NewJavaClassDialog.getCapitalizedClassName("Abcd");
    assertEquals(name, "Abcd");
  }
  
  public void testgetCapitalizedClassNameLowerCase() {
    String name = NewJavaClassDialog.getCapitalizedClassName("abcd");
    assertEquals(name, "Abcd");
  }
  
  public void testgetCapitalizedSuperClassNameUpperCase() {
    String name = NewJavaClassDialog.getCapitalizedSuperClassName("abcd.Abcd");
    assertEquals(name, "abcd.Abcd");
    name = NewJavaClassDialog.getCapitalizedSuperClassName("xyz.abcd.Abcd");
    assertEquals(name, "xyz.abcd.Abcd");
  }
  
  public void testgetCapitalizedSuperClassNameLowerCase() {
    String name = NewJavaClassDialog.getCapitalizedSuperClassName("abcd.abcd");
    assertEquals(name, "abcd.Abcd");
    name = NewJavaClassDialog.getCapitalizedSuperClassName("xyz.abcd.abcd");
    assertEquals(name, "xyz.abcd.Abcd");
  }
  
  public void testClassDeclarationPublic(){ //public class <name>
    String classLine = NewJavaClassDialog.getClassDeclaration("public", "", "abcd", "", "", false);
    assertEquals("public class Abcd", classLine);
  }
  public void testClassDeclarationAbstract(){ //abstract class <name>
    String classLine = NewJavaClassDialog.getClassDeclaration("abstract", "", "abcd", "", "", false);
    assertEquals("abstract class Abcd", classLine);
  }
  public void testClassDeclarationFinal(){ //final class <name>
    String classLine = NewJavaClassDialog.getClassDeclaration("final", "", "abcd", "", "", false);
    assertEquals("final class Abcd", classLine);
  }
  
  public void testDeclarationPublicFinal() {
    String classLine = NewJavaClassDialog.getClassDeclaration("public", "final", "abcd", "", "", false);
    assertEquals("public final class Abcd", classLine); 
  }
  
  public void testDeclarationPublicAbstract() {
    String classLine = NewJavaClassDialog.getClassDeclaration("public", "abstract", "abcd", "", "", false);
    assertEquals("public abstract class Abcd", classLine); 
  }
  
  public void testClassDeclarationDefaultFinal(){ //final class <name>
    String classLine = NewJavaClassDialog.getClassDeclaration("", "final", "abcd", "", "", false);
    assertEquals("final class Abcd", classLine);
  }
  
  public void testClassDeclarationDefault(){ //final class <name>
    String classLine = NewJavaClassDialog.getClassDeclaration("", "", "abcd", "", "", false);
    assertEquals("class Abcd", classLine);
  }
  
  public void testClassDeclarationInheritance(){ 
    String classLine = NewJavaClassDialog.getClassDeclaration("public", "", "abcd", "parentClass", "", false);
    assertEquals("public class Abcd extends ParentClass", classLine);
  }
  
  public void testClassDeclarationManyInterfaces(){ 
    String classLine = NewJavaClassDialog.getClassDeclaration("public", "", "abcd", "",
                                                              "interface1, interface2", false);
    assertEquals("public class Abcd implements Interface1, Interface2", classLine);
  }
  
  public void testClassDeclarationInterface(){ 
    String classLine = NewJavaClassDialog.getClassDeclaration("public", "", "abcd", "", "interface1", false);
    assertEquals("public class Abcd implements Interface1", classLine);
  }
  public void testClassDeclarationInheritanceAndInterface(){ 
    String classLine = NewJavaClassDialog.getClassDeclaration("public", "final", "abcd",
                                                              "superClass", "interface1", false);
    assertEquals("public final class Abcd extends SuperClass implements Interface1", classLine);
  }
  
  public void testClassDeclarationInheritanceAndInterfaceNotWellStructured(){ 
    String classLine = NewJavaClassDialog.getClassDeclaration("public", "abstract", "abcd", 
                                                              "superClass ", " interface1 ,inter , inm", false);  
    assertEquals("public abstract class Abcd extends SuperClass implements Interface1, Inter, Inm", classLine);
  }
  
  
  public void testClassDeclarationFinalNullMethod(){ //final class <name>
    String classLine = NewJavaClassDialog.getClassDeclaration(null, "", "abcd", "", "", false);
    assertEquals("class Abcd", classLine);
  }
  
  public void testClassDeclarationWrongMethodName(){ //final class <name>
    String classLine = NewJavaClassDialog.getClassDeclaration("sdkjfh", "final", "abcd", "", "", false);
    assertEquals("final class Abcd", classLine);
  }
  
  public void testClassDeclarationFinalNullName(){ //final class <name>
    String classLine = NewJavaClassDialog.getClassDeclaration("public","", null, "", "", false);
    assertEquals("public class", classLine);
  }
  
  
  public void testClassDeclarationMainMethodSelection(){
    String content = "";
    content += "/**\n";
    content += "* Auto Generated Java Class.\n";
    content += "*/\n";
    content += "public abstract class Abcd {\n";
    content += "\n";
    content += "\n public static void main(String[] args) { \n\n";
    content += "}\n\n";
    content += "/* ADD YOUR CODE HERE */\n";
    content += "\n";
    content += "}\n";
    assertEquals(content, NewJavaClassDialog.getClassContent("public", "abstract", "abcd", true, false,
                                                             "", "", false));
  }
  
  public void testClassDeclarationConstructorSelection(){ 
    String content = "";
    content += "/**\n";
    content += "* Auto Generated Java Class.\n";
    content += "*/\n";
    content += "final class Abcd {\n";
    content += "\n";     
    content += "public Abcd() { \n";
    content += "/* YOUR CONSTRUCTOR CODE HERE*/";
    content += "\n}\n";
    content += "/* ADD YOUR CODE HERE */\n";
    content += "\n";
    content += "}\n";
    assertEquals(content, NewJavaClassDialog.getClassContent("", "final", "abcd", false, true,
                                                             "", "", false));
  }
  
  public void testClassDeclarationOnly(){
    String content = "";
    content += "/**\n";
    content += "* Auto Generated Java Class.\n";
    content += "*/\n";
    content += "abstract class Abcd {\n";
    content += "\n";
    content += "/* ADD YOUR CODE HERE */\n";
    content += "\n";
    content += "}\n";
    assertEquals(content, NewJavaClassDialog.getClassContent("abstract", "", "Abcd", false, false,
                                                             "", "", false));
  }
  
  public void testJavaClassFull(){
    String content = "";
    content += "/**\n";
    content += "* Auto Generated Java Class.\n";
    content += "*/\n";
    content += "public class Abcd extends MySuperClass implements MyInterface {\n";
    content += "\n";
    content += "public Abcd() { \n";
    content += "/* YOUR CONSTRUCTOR CODE HERE*/";
    content += "\n}\n";
    content += "\n public static void main(String[] args) { \n\n";
    content += "}\n\n";
    content += "/* ADD YOUR CODE HERE */\n";
    content += "\n";
    content += "}\n";
    assertEquals(content, NewJavaClassDialog.getClassContent("public", "", "Abcd", true, true,
                                                             "mySuperClass","myInterface", false));
  }
}