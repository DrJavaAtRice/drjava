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

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import koala.dynamicjava.tree.*;

/**
 * The instances of this class manages the importation clauses.
 *
 * The <code>declarePackageImport</code> method imports a new package.
 * This one has the highest priority over the imported packages when a
 * lookup is made to find a class.
 *
 * The <code>declareClassImport</code> method imports a new class. This
 * one has the highest priority over the same suffix imported class.
 *
 * @author  Stephane Hillion
 * @version 1.1 - 1999/11/28
 */

public class ImportationManager implements Cloneable {
  /**
   * This list contains the import-on-demand clauses.
   */
  protected List<String> importOnDemandClauses = new LinkedList<String>();
  
  /**
   * This set contains the single-type-import clauses.
   */
  protected List<String> singleTypeImportClauses = new LinkedList<String>();
    
  
  /**
   * This list contains the import-on-demand clauses for staticly imported classes
   */
  protected List<String> importOnDemandStaticClauses = new LinkedList<String>();
  
  
  /**
   * This list contains the single-type-import static clauses for imported static fields
   */
  protected List<Field> singleTypeImportStaticFieldClauses = new LinkedList<Field>();
  
  /**
   * This hashmap contains the single-type-import static clauses for imported static methods
   */
   protected List<Method> singleTypeImportStaticMethodClauses = new LinkedList<Method>();
  
  /**
   * This string contains the name of the current package
   */
  protected String currentPackage;
  
  /**
   * The class loader that must be used
   */
  protected ClassLoader classLoader;
  
  /**
   * Creates a new importation manager.
   * The manager is initialized with two predefined import-on-demand
   * clauses: "java.lang" (the java language package) and 
   * "" (the anonymous package)
   * @param cl the class loader to use
   */
  public ImportationManager(ClassLoader cl) {
    classLoader = cl;
    declarePackageImport("java.lang");
    setCurrentPackage("");
  }
  
  /**
   * Returns a copy of this object
   */
  public Object clone() {
    return new ImportationManager(this);
  }
  
  /**
   * Sets the class loader
   */
  public void setClassLoader(ClassLoader cl) {
    classLoader = cl;
  }
  
  /**
   * Sets the current package. This has no influence on the
   * behaviour of the <code>lookupClass</code> method.
   * @param pkg the package name
   */
  public void setCurrentPackage(String pkg) {
    currentPackage = pkg;
  }
  
  /**
   * Returns the current package
   */
  public String getCurrentPackage() {
    return currentPackage;
  }
  
  /**
   * Returns the import-on-demand clauses
   */
  public List<String> getImportOnDemandClauses() {
    return importOnDemandClauses;
  }
  
  /**
   * Returns the single-type-import clauses
   */
  public List<String> getSingleTypeImportClauses() {
    return singleTypeImportClauses;
  }
  
  /**
   * Returns the import-on-demand static clauses for staticly imported classes
   */
  public List<String> getImportOnDemandStaticClauses() {
    return importOnDemandStaticClauses;
  }
  
  /**
   * Returns the single-type-import clauses for staticly imported fields
   */
  public List<Field> getSingleTypeImportStaticFieldClauses() {
    return singleTypeImportStaticFieldClauses;
  }
  
  /**
   * Returns the single-type-import clauses for staticly imported methods
   */
  public List<Method> getSingleTypeImportStaticMethodClauses() {
    return singleTypeImportStaticMethodClauses;
  }
  
  
  /**
   * Declares a new import-on-demand clause
   * @param pkg the package name
   */
  public void declarePackageImport(String pkg) {
    if (importOnDemandClauses.size() == 0 ||
        !importOnDemandClauses.get(0).equals(pkg)) {
      importOnDemandClauses.remove(pkg);
      importOnDemandClauses.add(0, pkg);
    }
  }
  
  /**
   * Declares a new single-type-import clause
   * @param cname the fully qualified class name
   * @exception ClassNotFoundException if the class cannot be found
   */
  public void declareClassImport(String cname) throws ClassNotFoundException {  
    try {
      // A previous importation of this class is removed to avoid a new
      // existance verification and to speed up further loadings.
      if (!singleTypeImportClauses.remove(cname)) {
        Class.forName(cname, true, classLoader);
      }
    } catch (ClassNotFoundException e) {
      // try to find an inner class with this name
      Class c = findInnerClass(cname);
      singleTypeImportClauses.remove((c == null) ? cname : c.getName());
      singleTypeImportClauses.add(0, (c == null) ? cname : c.getName());
    } finally {
        singleTypeImportClauses.add(0, cname);
    }
  }
  
  /**
   * Declares a new import-on-demand clause for static importation.
   * @param cname the fully qualified class name of the class whose members are being imported
   */
  public void declareClassStaticImport(String cname) throws ClassNotFoundException{
    try {
      // A previous importation of this class is removed to avoid a new
      // existance verification and to speed up further loadings.
      if (!importOnDemandStaticClauses.remove(cname)) {
        Class.forName(cname, true, classLoader);
      }
    } catch (ClassNotFoundException e) {
      // try to find an inner class with this name
      Class c = findInnerClass(cname);
      importOnDemandStaticClauses.remove((c == null) ? cname : c.getName());
      importOnDemandStaticClauses.add(0, (c == null) ? cname : c.getName());
    } finally {
      importOnDemandStaticClauses.add(0, cname);
    }
  }
  
  /**
   * Declares a new single-type-import clause for the static import of a field, method, or inner class, or any
   * combination in the case of members of the same name.
   * @param member - the name of the member being staticly imported
   */  
  public void declareMemberStaticImport(String member) {
    int i = member.lastIndexOf('.');
    String surroundingClassName = member;
    if (i > 0) {
      surroundingClassName = member.substring(0,i);
    }
    
    Class surroundingClass;
    
    try {    
      try {
        surroundingClass = Class.forName(surroundingClassName, true, classLoader);
        
      } catch (ClassNotFoundException e) {
        // try to find an inner class with this name
        surroundingClass = findInnerClass(surroundingClassName);
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Class " + surroundingClassName + " not found");      
    }
    
       
    boolean foundSomethingToImport = false;
    boolean foundSecurityException = false;
    String name = member.substring(i+1,member.length());
    
    
    //First, check for a static inner class by the given name
    //**//Note, all static inner classes imported with "import static" have to be added to the list twice, once with a '.' and once with a '$'. 
    //The first class in the list which successfully works is the one used when the user actually instantiates the class, and both are needed because 
    //Different methods require different formats, and having both can't hurt anything. Any of the methods that use the list of classes try and catch through the
    //list until they come across a class that fits
    try {
      Class c;
      try {
        c = Class.forName(member, true, classLoader);
      } catch (ClassNotFoundException cnfe) {
        c = findInnerClass(member);
      }
      if(isPublicAndStatic(c.getModifiers())) {
        declareClassImport(member);
        foundSomethingToImport = true;
      }
    }
    catch(ClassNotFoundException e) {
    }
    catch(SecurityException e) {
      foundSecurityException = true;
    }
    
    
    
    
    //Next, check for all static fields
    try {
      Field f = surroundingClass.getField(name);
      if(isPublicAndStatic(f.getModifiers())) {
        singleTypeImportStaticFieldClauses.remove(f);
        singleTypeImportStaticFieldClauses.add(0,f);
        foundSomethingToImport = true;
      }
    }
    catch(NoSuchFieldException e) {
    }
    catch(SecurityException e) {
      foundSecurityException = true;
    }
    
    //Next, check for all static methods
    try {
      Method[] methodArray = surroundingClass.getMethods();
      for(int j = 0; j<methodArray.length; j++) {
        if(isPublicAndStatic(methodArray[j].getModifiers()) && methodArray[j].getName().equals(name)) {
          Method m = methodArray[j];
          if(isPublicAndStatic(m.getModifiers())) {
            singleTypeImportStaticMethodClauses.remove(m);
            singleTypeImportStaticMethodClauses.add(0,m);
            foundSomethingToImport = true;
          }
        }          
      }
    }
    catch(SecurityException e) {
      foundSecurityException = true;
    }
    
    if(foundSomethingToImport)
      return;
    
    throw new RuntimeException("No public members of the name " + member);
  }
  
  
  /**
   * Loads the class that match to the given name in the source file
   * @param  cname  the name of the class to find
   * @param  ccname the name of the current class or null
   * @return the class found
   * @exception ClassNotFoundException if the class cannot be loaded
   */
  public Class lookupClass(String cname, String ccname)
    throws ClassNotFoundException {
        
    String str = cname.replace('.', '$');
    // Search for the full name ...
    
    // ... in the current package ...
    String t = (currentPackage.equals(""))
      ? cname
      : currentPackage + "." + cname; 
    try {
      return Class.forName(t, false, classLoader);
    } catch (ClassNotFoundException e) {
    }
    
    if (cname.indexOf('.') != -1) {
      try {
        return Class.forName(cname, false, classLoader);
      } catch (ClassNotFoundException e) {
      }
    }
    
    // try to find an inner class with this name
    try {
      return findInnerClass(t);
    } catch (ClassNotFoundException e) {
    }
    
    // ... in the single-type-import clauses ...
    Iterator<String> it = singleTypeImportClauses.iterator();
    while (it.hasNext()) {
      String s = it.next();
      if (hasSuffix(s, cname) || hasSuffix(s, str)) {
        try
        {
          return Class.forName(s, false, classLoader);
        } catch (ClassNotFoundException e) {
          return findInnerClass(s);         
        }        
      }
      // It is perhaps an innerclass of an imported class
      // ie. a.b.C and C$D
      int i = str.indexOf('$');
      if (i != -1) {
        try {
          if (hasSuffix(s, str.substring(0, i))) {
            return Class.forName(s + str.substring(i, str.length()),
                                 false, classLoader);
          }
        } catch (ClassNotFoundException e) {
        }
      }
    }
    
    if (ccname != null) {
      // ... in the current class ...
      try {
        return Class.forName(ccname + "$" + str, false, classLoader);
      } catch (ClassNotFoundException e) {
      }
      
      // ... it is perhaps an outer class
      it = getOuterNames(ccname).iterator();
      String tmp = ccname;
      while (it.hasNext()) {
        String s = it.next();
        int i = tmp.lastIndexOf(s) + s.length();
        tmp = tmp.substring(0, i);
        if (s.equals(cname)) {
          return Class.forName(tmp, false, classLoader);
        }
      }
      
      // ... or the class itself
      if (ccname.endsWith(cname)) {
        int i = ccname.lastIndexOf(cname);
        if (i > 0 && ccname.charAt(i - 1) == '$') {
          return Class.forName(ccname, false, classLoader);
        }
      }
    }
    
     
    // ... with the import-on-demand clauses as prefix
    it = importOnDemandClauses.iterator();
    while (it.hasNext()) {
      String s = it.next();
      try {
        return Class.forName(s+"."+str, false, classLoader);
      } catch (ClassNotFoundException e) {
      }
    }
    //Now look through classes staticly imported with .*; for a static inner class 
    it = importOnDemandStaticClauses.iterator();
    while (it.hasNext()) {
      String s = it.next();
      try {
        return Class.forName(s+"$"+str, false, classLoader);
      } catch (ClassNotFoundException e) {
      }
    }
    
    throw new ClassNotFoundException(cname);
  }
  
  /**
   * Returns a list of the outer classes names
   */
  protected List<String> getOuterNames(String cname) {
    List<String> l = new LinkedList<String>();
    int i;
    while ((i = cname.lastIndexOf('$')) != -1) {
      cname = cname.substring(0, i);
      if ((i = cname.lastIndexOf('$')) != -1) {
        l.add(cname.substring(i + 1, cname.length()));
      } else if ((i = cname.lastIndexOf('.')) != -1) {
        l.add(cname.substring(i + 1, cname.length()));
      } else {
        l.add(cname);
      }
    }
    return l;
  }
  
  /**
   * Searches for an inner class from its name in the dotted notation
   */
  protected Class findInnerClass(String s) throws ClassNotFoundException {
    int   n;
    while ((n = s.lastIndexOf('.')) != -1) {
      s = s.substring(0, n) + '$' + s.substring(n + 1, s.length());
      try {
        return Class.forName(s, false, classLoader);
      } catch (ClassNotFoundException e) {
      }
    }
    throw new ClassNotFoundException(s);
  }
  
  /**
   * Copy constructor
   */
  protected ImportationManager(ImportationManager im) {
    //importOnDemandClauses     =
    //  (List<String>)((LinkedList<String>)im.importOnDemandClauses).clone();
    //singleTypeImportClauses   =
    //  (List<String>)((LinkedList<String>)im.singleTypeImportClauses).clone();
    
    importOnDemandClauses     = ListUtilities.listCopy(im.importOnDemandClauses);
    singleTypeImportClauses   = ListUtilities.listCopy(im.singleTypeImportClauses);
    importOnDemandStaticClauses = ListUtilities.listCopy(im.importOnDemandStaticClauses);
    singleTypeImportStaticFieldClauses = ListUtilities.listCopy(im.singleTypeImportStaticFieldClauses);
    
    singleTypeImportStaticMethodClauses = ListUtilities.listCopy(im.singleTypeImportStaticMethodClauses);
    
    currentPackage            = im.currentPackage;
    classLoader               = im.classLoader;
  }


  /** 
   * Looks through the list of staticly imported methods and through all statically imported classes
   * for the method with the given names and parameters
   * @param name - the name of the method
   * @param params - the parameters of the method
   */  
  protected Method lookupMethod(String name, Class[] params) throws NoSuchMethodException {
    Method m, toReturn;
    Iterator<Method> i = singleTypeImportStaticMethodClauses.iterator();
    while(i.hasNext()) {
      m = i.next();
      if(m.getName().equals(name) && ReflectionUtilities.hasCompatibleSignatures(m.getParameterTypes(),params))
        return m;
    }
    //If not found in the singleTypeImport list, check the import on demand list
    String className;
    Iterator<String> it = importOnDemandStaticClauses.iterator();
    while(it.hasNext()) {
      className = it.next();
      try {
        toReturn = ReflectionUtilities.lookupMethod(lookupClass(className,null),name,params);
          //Class.forName(className, true, classLoader).getMethod(name,params);    
        return toReturn;
      }
      catch(NoSuchMethodException nsme) {
        //Will throw lots of these
      }
      catch(ClassNotFoundException cnfe) {
        throw new RuntimeException("Class existed on static import and does not exist on method lookup");
      }
    }
    
    //If it hasn't returned by now, there hasn't been a method of this name with this parameter list that has been staticly imported
    throw new NoSuchMethodException(name);
  }
  
  
  /**
     * Returns the fully qualified class name that wraps the given staticly imported method
     * @param methodName the method name
     * @param args the argument list for the method
     */
  public List<IdentifierToken> getQualifiedName(String methodName, Class[] args) 
    throws NoSuchMethodException {
    List<IdentifierToken> toReturn = new LinkedList<IdentifierToken>(); 
    Method m = lookupMethod(methodName, args);
    String toParse = m.getDeclaringClass().getName();
    if(toParse.startsWith("class "))
      toParse = toParse.substring(6,toParse.length());
    int i;
    while((i=toParse.lastIndexOf(".")) != -1) {
      toReturn.add(0, new Identifier(toParse.substring(i+1,toParse.length())));
      toParse = toParse.substring(0,i);
    }
    toReturn.add(0, new Identifier(toParse));
    return toReturn;
  }
    
  /**
   * Tests whether the fully qualified class name c1 ends with c2
   */
  protected boolean hasSuffix(String c1, String c2) {
    return suffix(c1).equals(c2);
  }
  
  /**
   * Return the string following the last '.'
   */
  protected String suffix(String s) {
    int i = s.lastIndexOf('.');
    String s2 = s;
    if(i != -1) {
      s2 = s.substring(i+1, s.length());
    }
    return s2;
  }
  
  
  /**
   * Return true iff the integer argument includes the static modifer and the public modifier, false otherwise.
   */
  protected boolean isPublicAndStatic(int modifiers) {
    return Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers);
  }
  
  /**
   * Return true iff the name is a field that has been staticly imported
   */
  public boolean fieldExists(String name) {
    
    try {
      lookupField(name);
      return true;
    }
    catch (NoSuchFieldException e) {
     return false; 
    }
  }
  
  /**
   * Returns the Field with the given name if it has been staticly imported
   * throws a NoSuchFieldException if it cannot be found.
   */
  public Field lookupField(String name) throws NoSuchFieldException {
    Field f;
    Iterator<Field> i = singleTypeImportStaticFieldClauses.iterator();
    while(i.hasNext()) {
      f = i.next();
      if(f.getName().equals(name))
        return f;
    }
    String className;
    Iterator<String> it = importOnDemandStaticClauses.iterator();
    while(it.hasNext()) {
      className = it.next();
      try {
        f = ReflectionUtilities.getField(lookupClass(className,null),name);  
        return f;
      }
      catch(NoSuchFieldException nsme) {
        //Will throw lots of these
      }
      catch(AmbiguousFieldException afe) {
        throw new RuntimeException("Ambiguous Field exception: Field " + name + " is ambiguous");
      }
      catch(ClassNotFoundException cnfe) {
        throw new RuntimeException("Class existed on static import and does not exist on field lookup");
      }
      
    }
    //If it hasn't returned by now, there hasn't been a method of this name with this parameter list that has been staticly imported
    throw new NoSuchFieldException(name);
  }
  
  /**
   * Returns the fully qualified class name that wraps the given staticly imported field
   * @param fieldName the field name
   */
  public List<IdentifierToken> getQualifiedName(String fieldName) throws NoSuchFieldException {
    List<IdentifierToken> toReturn = new LinkedList<IdentifierToken>(); 
    Field f = lookupField(fieldName);
    String toParse = f.getDeclaringClass().getName() + "." + fieldName;
    if(toParse.startsWith("class "))
      toParse = toParse.substring(6,toParse.length());
    int i;    
    while((i=toParse.lastIndexOf(".")) != -1) {
      toReturn.add(0, new Identifier(toParse.substring(i+1,toParse.length())));
      toParse = toParse.substring(0,i);
    }
    toReturn.add(0, new Identifier(toParse));
    return toReturn;
  }
  
}