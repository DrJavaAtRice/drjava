package edu.rice.cs.drjava.ui;

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

/** Generates Java source from information entered in the "New Class" dialog.
  * @version $Id$
  */
public class NewJavaClass {
  
  //newclass addition
  public String capitalizeClassName(String name){
    return name.trim().substring(0,1).toUpperCase() + name.trim().substring(1);      
  }
  
  //newclass addition
  public boolean classNameMeetsNamingConvention(String name){
    if(name != null && name.length() != 0 && name.trim().matches("([A-Za-z_][A-Za-z0-9_]*)"))
      return true;
    else
      return false;
  }
  
  public boolean iterateListOfClassNames(String name) {
    String[] separatedNames = name.split(",");
    Boolean correct = true;
    for(int i = 0; i < separatedNames.length; i++) 
      if(!classNameMeetsNamingConvention(separatedNames[i].trim())) {
      correct = false;
      break;
    }
    return correct;
  }
  
  public boolean interfacesNameMeetsNamingConvention(String name){
    if(name != null && name.length() != 0 && name.matches("([A-Za-z_][A-Za-z0-9_, ]*)")) {
      return iterateListOfClassNames(name);
    } else
      return false;
  }
  
  public String capitalizeInterfacesNames(String name) {
    String[] separatedNames = name.split(",");
    String correctNames = "";
    for(int i = 0; i < separatedNames.length;) {
      correctNames += capitalizeClassName(separatedNames[i].trim());
      if(++i != separatedNames.length)
        correctNames += ", ";
    }
    return correctNames ;
  }
  
  //newclass addition   
  public String getMethodName(String methodName){
    if(methodName == null)
      return ""; //return blank method
    else if(methodName.equals("public") || methodName.equals("final") || methodName.equals("abstract"))
      return methodName + " ";
    else
      return ""; //return blank method
  }
  
  //newclass addition
  public String createClassNameDecleration(String methodName, String modifier, String name, 
                                           String superclass, String interfaces) {
    String declaration = "";
    if(name == null)
      declaration += getMethodName(methodName) + getMethodName(modifier) + "class"; //no class name returned because it is null
    else
      declaration += getMethodName(methodName) + getMethodName(modifier) + "class " + capitalizeClassName(name); 
    if(superclass.length() != 0) {
      declaration += " extends " + capitalizeClassName(superclass);
    }
    if(interfaces.length() != 0) {
      declaration += " implements " + capitalizeInterfacesNames(interfaces);
    }
    return declaration;
  }
  
  public String createClassContent(String methodName, String modifier, String className, 
                                   boolean mainMethod, boolean classConstructor, String inheritance, 
                                   String interfaces){
    String classContent = "";
    
    classContent += "/**\n";
    classContent += "* Auto Generated Java Class.\n";
    classContent += "*/\n";
    classContent += createClassNameDecleration(methodName, modifier, className, inheritance, interfaces);
    classContent += " {\n";
    classContent += "\n";
    
    if(classConstructor) {
      classContent += "public " + capitalizeClassName(className) + "() { \n";
//    classContent += getMethodName(methodName) + " " + capitalizeClassName(className) + "() { \n";
      classContent += "/* YOUR CONSTRUCTOR CODE HERE*/";
      classContent += "\n}\n";
    }
    
    if(mainMethod) {
      classContent += "\n public static void main(String [] args) { \n\n";
      classContent += "}\n\n";
    }
    
    classContent += "/* ADD YOUR CODE HERE */\n";
    classContent += "\n";
    classContent += "}\n";
    return classContent;
  }
}