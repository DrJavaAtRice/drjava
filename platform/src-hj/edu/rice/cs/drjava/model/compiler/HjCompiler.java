
/*BEGIN_COPYRIGHT_BLOCK
*
* This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
* or http://sourceforge.net/projects/drjava/
*
* DrJava Open Source License
* 
* Copyright (C) 2001-2010 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
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

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.io.FilenameFilter;
import java.util.Scanner;




import hj.lang.Runtime;
import soot.Main;
import edu.rice.cs.plt.reflect.PathClassLoader;
import edu.rice.cs.plt.reflect.ShadowingClassLoader;
import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.drjava.model.JarJDKToolsLibrary;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.util.ArgumentTokenizer;

import static edu.rice.cs.drjava.model.compiler.descriptors.JDKDescriptor.Util;

/** An implementation of JavacCompiler that supports compiling with the Java 1.6.0/Mint compiler.
  * Must be compiled using javac 1.6.0 and with Mint on the boot classpath.
  *
  *  @version $Id: HjCompiler.java 5206 2010-04-06 06:52:54Z mgricken $
  */
public class HjCompiler extends JavacCompiler {

  private final boolean _filterExe;
  private File _tempJUnit = null;
  private final String PREFIX = "drjava-junit";
  private final String SUFFIX = ".jar";  
  private static String Dir ="";

  public HjCompiler(JavaVersion.FullVersion version, String location, java.util.List<? extends File> defaultBootClassPath) {
    super(version, location, defaultBootClassPath);
    _filterExe = version.compareTo(JavaVersion.parseFullVersion("1.6.0_04")) >= 0;
    if (_filterExe) {
      // if we need to filter out exe files from the classpath, we also need to
      // extract junit.jar and create a temporary file
      try {
        // edu.rice.cs.util.Log LOG = new edu.rice.cs.util.Log("jdk160.txt",true);
        // LOG.log("Filtering exe files from classpath.");
        InputStream is = HjCompiler.class.getResourceAsStream("/junit.jar");
        if (is!=null) {
          // LOG.log("\tjunit.jar found");
          _tempJUnit = edu.rice.cs.plt.io.IOUtil.createAndMarkTempFile(PREFIX,SUFFIX);
          FileOutputStream fos = new FileOutputStream(_tempJUnit);
          int size = edu.rice.cs.plt.io.IOUtil.copyInputStream(is,fos);
          // LOG.log("\t"+size+" bytes written to "+_tempJUnit.getAbsolutePath());
        }
        else {
          // LOG.log("\tjunit.jar not found");
          if (_tempJUnit!=null) {
            _tempJUnit.delete();
            _tempJUnit = null;
          }
        }
      }
      catch(IOException ioe) {
        if (_tempJUnit!=null) {
          _tempJUnit.delete();
          _tempJUnit = null;
        }
      }
      // sometimes this file may be left behind, so create a shutdown hook
      // that deletes temporary files matching our pattern
      java.lang.Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        public void run() {
          try {
            File temp = File.createTempFile(PREFIX, SUFFIX);
            IOUtil.attemptDelete(temp);
            File[] toDelete = temp.getParentFile().listFiles(new FilenameFilter() {
              public boolean accept(File dir, String name) {
                if ((!name.startsWith(PREFIX)) || (!name.endsWith(SUFFIX))) return false;
                String rest = name.substring(PREFIX.length(), name.length()-SUFFIX.length());
                try {
                  Integer i = new Integer(rest);
                  // we could create an integer from the rest, this is one of our temporary files
                  return true;
                }
                catch(NumberFormatException e) { /* couldn't convert, ignore this file */ }
                return false;
              }
            });
            for(File f: toDelete) {
              f.delete();
            }
          }
          catch(IOException ioe) { /* could not delete temporary files, ignore */ }
        }
      })); 
    }
  }

  public String getName() { return "HJ " + _version.versionString(); }
  
  /** A compiler can instruct DrJava to include additional elements for the boot
    * class path of the Interactions JVM. This is necessary for the Mint compiler,
    * since the Mint compiler needs to be invoked at runtime. */
  public java.util.List<File> additionalBootClassPathForInteractions() {
//   String hj_home = System.getenv("HJ_HOME") + "/lib";
//   File dir = new File(hj_home); 
//   File[] jarfiles = dir.listFiles(filter);
//   
//   File f= new File("/Users/triplezero163/Work/workspace/Test/src");
//   File [] dirs = {f};
//   
//    //return Arrays.asList(f);
//   return Collections.emptyList();
    File parentDir = new File(_location).getParentFile();
    try {
      File[] jarFiles = new File[] {
        Util.oneOf(parentDir, "sootclasses-2.3.0.jar"),
          Util.oneOf(parentDir, "polyglot.jar"),
          Util.oneOf(parentDir, "lpg.jar"),
          Util.oneOf(parentDir, "jasminclasses-2.3.0.jar"),
          Util.oneOf(parentDir, "java_cup.jar"),
          Util.oneOf(parentDir, "hj.jar")
      };
      return Arrays.asList(jarFiles);    
    }
    catch(FileNotFoundException fnfe) { return Collections.emptyList(); }
  }
  

  FilenameFilter filter = new FilenameFilter() {
   public boolean accept(File dir, String name) 
   { 
    return name.endsWith(".jar"); 
   } 
  };

  /** Transform the command line to be interpreted into something the Interactions JVM can use.
    * This replaces "java MyClass a b c" with Java code to call MyClass.main(new String[]{"a","b","c"}).
    * "import MyClass" is not handled here.
    * @param interactionsString unprocessed command line
    * @return command line with commands transformed */
  public String transformCommands(String interactionsString) {
   System.out.println(interactionsString);
   if (interactionsString.startsWith("hj ")){
    interactionsString = interactionsString.replace("hj ", "hj hj.lang.Runtime ");
    interactionsString = transformHJCommand(interactionsString);
    System.out.println(interactionsString);
   }
   if (interactionsString.startsWith("java "))  {
    interactionsString = interactionsString.replace("java ", "java hj.lang.Runtime ");
    interactionsString = transformHJCommand(interactionsString);
   }
   
   if (interactionsString.startsWith("run "))  {
    interactionsString = interactionsString.replace("run ", "java hj.lang.Runtime ");
    interactionsString = transformHJCommand(interactionsString);
    System.out.println(interactionsString);
   }
   
   
      return interactionsString;
   
  }
  
  public static String transformHJCommand(String s) {
     // check the return type and public access before executing, per bug #1585210
   //String [] hjcommand = new String[s.length()];
 //  hj.lang.runtime.Runtime.mainEntry

   
   final String HJcommand = "hj.lang.Runtime.mainEntry(new String[]'{'\"-rt=wsh\",{1}'}');";
   //final String HJcommand = "hj.lang.Runtime.mainEntry(new String[]'{'\"-ea\",\"-classpath\",\"" + Dir  + "\",\"-rt=wsh\",{1}'}');";
   
     if (s.endsWith(";"))  s = _deleteSemiColon(s);
     java.util.List<String> args = ArgumentTokenizer.tokenize(s, true);
     final String classNameWithQuotes = args.get(1); // this is "MyClass"
     final String className = classNameWithQuotes.substring(1, classNameWithQuotes.length() - 1); // removes quotes, becomes MyClass
     final StringBuilder argsString = new StringBuilder();
     for (int i = 2; i < args.size(); i++) {
       //argsString.append(",");
       argsString.append(args.get(i));
     }
    String name = args.get(2);
  /*  if(!Dir.equals("")) {
     File path = new File(Dir);
     try {
     new PathClassLoader(JarJDKToolsLibrary.class.getClassLoader(), path).loadClass(name); 
     }
     catch (Exception e){
      e.printStackTrace();
     }
     } */
     
     return java.text.MessageFormat.format(HJcommand, className, argsString.toString());
   
   }
  

  public boolean isAvailable() {
   return true;
  }
  

  /** Compile the given files.
    *  @param files  Source files to compile.
    *  @param classPath  Support jars or directories that should be on the classpath.  If @code{null}, the default is used.
    *  @param sourcePath  Location of additional sources to be compiled on-demand.  If @code{null}, the default is used.
    *  @param destination  Location (directory) for compiled classes.  If @code{null}, the default in-place location is used.
    *  @param bootClassPath  The bootclasspath (contains Java API jars or directories); should be consistent with @code{sourceVersion} 
    *                        If @code{null}, the default is used.
    *  @param sourceVersion  The language version of the sources.  Should be consistent with @code{bootClassPath}.  If @code{null},
    *                        the default is used.
    *  @param showWarnings  Whether compiler warnings should be shown or ignored.
    *  @return Errors that occurred. If no errors, should be zero length (not null).
    */
  public java.util.List<? extends DJError> compile(java.util.List<? extends File> files,
                                                   java.util.List<? extends File> classPath, 
                                                   java.util.List<? extends File> sourcePath,
                                                   File destination, 
                                                   java.util.List<? extends File> bootClassPath,
                                                   String sourceVersion,
                                                   boolean showWarnings) {
   
    java.util.List<File> filteredClassPath = null;
    if (classPath!=null) {
      filteredClassPath = new LinkedList<File>(classPath);
      
      if (_filterExe) {
        FileFilter filter = IOUtil.extensionFilePredicate("exe");
        Iterator<? extends File> i = filteredClassPath.iterator();
        while (i.hasNext()) {
          if (filter.accept(i.next())) { i.remove(); }
        }
        if (_tempJUnit!=null) { filteredClassPath.add(_tempJUnit); }
      }
    }
    String s ="";
    Iterator<? extends File> ti = _defaultBootClassPath.listIterator();
    while(ti.hasNext()){
     s += ":" + ti.next().getPath();
    }
    String [] testCommand = new String[11];
        
    testCommand[0] = "-hj";
    testCommand[1] = "-info";
    testCommand[2] = "-sp";
    testCommand[4] = "-cp";
    testCommand[5] = s;
    testCommand[6] = "-d";
    testCommand[8] = "-w";
    testCommand[9] = "-pp";
    
    
    Iterator<? extends File> it = files.listIterator();
    while(it.hasNext()){
     File next = it.next();
      testCommand[3] = next.getParent();
      Dir = testCommand[3];
      testCommand[7] = next.getParent();
      testCommand[10] = next.getName();
      File path = new File(next.getParent());
     // File path = new File("/Users/triplezero163/Work/workspace/hj.trunk/Eclipse-license-code/hj/hj.release/lib/hjc.jar");
      String name = next.getName().replace(".hj", "");
      try {
      soot.Main.mainEntry(testCommand); 
     // new PathClassLoader(JarJDKToolsLibrary.class.getClassLoader(), path).loadClass(name);
      }
      catch(Exception e) {
       e.printStackTrace();
      }
    }
    

   

       return Collections.emptyList();
  }
  
      
  //////////////////////////////////////////////////////////////
}
