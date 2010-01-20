package edu.rice.cs.drjava.ui;

import javax.swing.JMenu;
import edu.rice.cs.drjava.model.AbstractGlobalModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Tests for NewJavaClass.
  * @version $Id$
  */
public class NewJavaClassTest extends TestCase {
  NewJavaClass javaClass = new NewJavaClass();
  
  public void testClassNameMeetsNamingConventionGoodNames(){
    assertTrue("wrong class name", javaClass.classNameMeetsNamingConvention("Abcd"));
    assertTrue("wrong class name", javaClass.classNameMeetsNamingConvention("abcd"));
    assertTrue("wrong class name", javaClass.classNameMeetsNamingConvention("_abcd"));
    //if the space is at the beginning or at the end, it will trim it, so, it is good name
    assertTrue("wrong class name", javaClass.classNameMeetsNamingConvention(" abcd"));  
    assertTrue("wrong class name", javaClass.classNameMeetsNamingConvention(" abcd "));  
  }
  
  public void testClassNameMeetsNamingConventionBadNames(){
    assertFalse("wrong class name", javaClass.classNameMeetsNamingConvention("(Abcd"));
    assertFalse("wrong class name", javaClass.classNameMeetsNamingConvention("%Abcd"));
    assertFalse("wrong class name", javaClass.classNameMeetsNamingConvention("^Abcd"));
    assertFalse("wrong class name", javaClass.classNameMeetsNamingConvention("#Abcd"));
    assertFalse("wrong class name", javaClass.classNameMeetsNamingConvention("_Ab%cd"));
    assertFalse("wrong class name", javaClass.classNameMeetsNamingConvention("_Abc d"));
    assertFalse("wrong class name", javaClass.classNameMeetsNamingConvention("5Abcd"));
    assertFalse("wrong class name", javaClass.classNameMeetsNamingConvention("$Abc d"));
    assertFalse("wrong class name", javaClass.classNameMeetsNamingConvention(""));
    assertFalse("wrong class name", javaClass.classNameMeetsNamingConvention(null));
  }
  
  public void testInterfacesNamingConventionsCorrectNames() {
    assertTrue("Correct Interfaces Name", 
               javaClass.interfacesNameMeetsNamingConvention("interfaces"));
    assertTrue("Correct Interfaces Name", 
               javaClass.interfacesNameMeetsNamingConvention("interfaces, interfaceme"));
    assertTrue("Correct Interfaces Name", 
               javaClass.interfacesNameMeetsNamingConvention("interfaces1, interfaceme123"));
    assertTrue("Correct Interfaces Name", 
               javaClass.interfacesNameMeetsNamingConvention("int12erfaces1 , interfaceme123 , goodName"));
    assertTrue("Correct Interfaces Name", 
               javaClass.interfacesNameMeetsNamingConvention("int12erfaces1 ,interfaceme123,goodName"));
  }
  
  public void testInterfacesNamingConventionsWrongNames() {
    assertFalse("Wrong Interfaces Name", 
                javaClass.interfacesNameMeetsNamingConvention("interf aces")); 
    assertFalse("Wrong Interfaces Name", 
                javaClass.interfacesNameMeetsNamingConvention("interfaces; inter"));
    assertFalse("Wrong Interfaces Name", 
                javaClass.interfacesNameMeetsNamingConvention("interfaces, &inter"));
    assertFalse("Correct Interfaces Name", 
                javaClass.interfacesNameMeetsNamingConvention("interfaces1, interfaceme123 , "));
  }
  
  public void testCapitalizeClassNameUpperCase() {
    String name = javaClass.capitalizeClassName("Abcd");
    assertEquals(name, "Abcd");
  }
  
  public void testCapitalizeClassNameLowerCase() {
    String name = javaClass.capitalizeClassName("abcd");
    assertEquals(name, "Abcd");
  }
  
  public void testClassDeclerationPublic(){ //public class <name>
    String classLine = javaClass.createClassNameDecleration("public", "", "abcd", "", "");
    assertEquals("public class Abcd", classLine);
  }
  public void testClassDeclerationAbstract(){ //abstract class <name>
    String classLine = javaClass.createClassNameDecleration("abstract", "", "abcd", "", "");
    assertEquals("abstract class Abcd", classLine);
  }
  public void testClassDeclerationFinal(){ //final class <name>
    String classLine = javaClass.createClassNameDecleration("final", "", "abcd", "", "");
    assertEquals("final class Abcd", classLine);
  }
  
  public void testDeclarationPublicFinal() {
    String classLine = javaClass.createClassNameDecleration("public", "final", "abcd", "", "");
    assertEquals("public final class Abcd", classLine); 
  }
  
  public void testDeclarationPublicAbstract() {
    String classLine = javaClass.createClassNameDecleration("public", "abstract", "abcd", "", "");
    assertEquals("public abstract class Abcd", classLine); 
  }
  
  public void testClassDeclerationDefaultFinal(){ //final class <name>
    String classLine = javaClass.createClassNameDecleration("", "final", "abcd", "", "");
    assertEquals("final class Abcd", classLine);
  }
  
  public void testClassDeclerationDefault(){ //final class <name>
    String classLine = javaClass.createClassNameDecleration("", "", "abcd", "", "");
    assertEquals("class Abcd", classLine);
  }
  
  public void testClassDeclerationInheritance(){ 
    String classLine = javaClass.createClassNameDecleration("public", "", "abcd", "parentClass", "");
    assertEquals("public class Abcd extends ParentClass", classLine);
  }
  
  public void testClassDeclerationManyInterfaces(){ 
    String classLine = javaClass.createClassNameDecleration("public", "", "abcd", "", "interface1, interface2");  
    assertEquals("public class Abcd implements Interface1, Interface2", classLine);
  }
  
  public void testClassDeclerationInterface(){ 
    String classLine = javaClass.createClassNameDecleration("public", "", "abcd", "", "interface1");  
    assertEquals("public class Abcd implements Interface1", classLine);
  }
  public void testClassDeclerationInheritanceAndInterface(){ 
    String classLine = javaClass.createClassNameDecleration("public", "final", "abcd", "superClass", "interface1");  
    assertEquals("public final class Abcd extends SuperClass implements Interface1", classLine);
  }
  
  public void testClassDeclerationInheritanceAndInterfaceNotWellStructured(){ 
    String classLine = javaClass.createClassNameDecleration("public", "abstract", "abcd", 
                                                            "superClass ", " interface1 ,inter , inm");  
    assertEquals("public abstract class Abcd extends SuperClass implements Interface1, Inter, Inm", classLine);
  }
  
  
  public void testClassDeclerationFinalNullMethod(){ //final class <name>
    String classLine = javaClass.createClassNameDecleration(null, "", "abcd", "", "");
    assertEquals("class Abcd", classLine);
  }
  
  public void testClassDeclerationWrongMethodName(){ //final class <name>
    String classLine = javaClass.createClassNameDecleration("sdkjfh", "final", "abcd", "", "");
    assertEquals("final class Abcd", classLine);
  }
  
  public void testClassDeclerationFinalNullName(){ //final class <name>
    String classLine = javaClass.createClassNameDecleration("public","", null, "", "");
    assertEquals("public class", classLine);
  }
  
  
  public void testClassDeclerationMainMethodSelection(){
    String content = "";
    content += "/**\n";
    content += "* Auto Generated Java Class.\n";
    content += "*/\n";
    content += "public abstract class Abcd {\n";
    content += "\n";
    content += "\n public static void main(String [] args) { \n\n";
    content += "}\n\n";
    content += "/* ADD YOUR CODE HERE */\n";
    content += "\n";
    content += "}\n";
    assertEquals(content, javaClass.createClassContent("public", "abstract", "abcd", true, false, "", ""));
  }
  
  public void testClassDeclerationConstructorSelection(){ 
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
    assertEquals(content, javaClass.createClassContent("", "final", "abcd", false, true, "", ""));
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
    assertEquals(content, javaClass.createClassContent("abstract", "", "Abcd", false, false, "", ""));
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
    content += "\n public static void main(String [] args) { \n\n";
    content += "}\n\n";
    content += "/* ADD YOUR CODE HERE */\n";
    content += "\n";
    content += "}\n";
    assertEquals(content, javaClass.createClassContent("public", "", "Abcd", true, true, "mySuperClass","myInterface"));
  }
}