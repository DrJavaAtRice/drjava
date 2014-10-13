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

package edu.rice.cs.drjava.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.plt.io.IOUtil;

import static edu.rice.cs.drjava.config.OptionConstants.*;

/** Some common methods for determining Java source files, language level files, etc.
  * @version $Id: DrJavaFileUtils.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class DrJavaFileUtils  {
  /** Return the set of source file extensions that this compiler supports.
    * @return the set of source file extensions that this compiler supports. */
  public static Set<String> getSourceFileExtensions() {
    HashSet<String> extensions = new HashSet<String>();
    extensions.add(JAVA_FILE_EXTENSION);
    extensions.add(SCALA_FILE_EXTENSION);
    // Java language levels support is disabled
//    extensions.add(DJ_FILE_EXTENSION);
//    extensions.add(OLD_DJ0_FILE_EXTENSION);
//    extensions.add(OLD_DJ1_FILE_EXTENSION);
//    extensions.add(OLD_DJ2_FILE_EXTENSION);
    return extensions;
  }
  
  /** Return the suggested file extension that will be appended to a file without extension.
    * @return the suggested file extension */
  public static String getSuggestedFileExtension() {
    return SCALA_FILE_EXTENSION;
  }
  
  /** .java --> true
    * .scala --> true
    * otherwise false
    * @return true if the file is a Java or language level file. */
  public static boolean isSourceFile(String fileName) {
    return fileName.endsWith(JAVA_FILE_EXTENSION)
      || fileName.endsWith(SCALA_FILE_EXTENSION);
  }
  
  /** @return true if the file is a Java or language level file. */
  public static boolean isSourceFile(File f) {
    File canonicalFile = IOUtil.attemptCanonicalFile(f);
    String fileName = canonicalFile.getPath();
    return isSourceFile(fileName);
  }
  
  // Java language levels is disabled

//  /** .pjt  --> true
//    * otherwise false
//    * @return true if the file is an old project file. */
//  public static boolean isOldProjectFile(String fileName) {
//    return fileName.endsWith(OLD_PROJECT_FILE_EXTENSION);
//  }
//  
//  /** @return true if the file is an old project file. */
//  public static boolean isOldProjectFile(File f) {
//    File canonicalFile = IOUtil.attemptCanonicalFile(f);
//    String fileName = canonicalFile.getPath();
//    return isOldProjectFile(fileName);
//  }
  
  /** .pjt    --> true
    * .drjava --> true
    * .xml    --> true
    * otherwise false
    * @return true if the file is a project file. */
  public static boolean isProjectFile(String fileName) {
    return fileName.endsWith(PROJECT_FILE_EXTENSION)
      || fileName.endsWith(PROJECT_FILE_EXTENSION2)
      || fileName.endsWith(OLD_PROJECT_FILE_EXTENSION);
  }
  
  /** @return true if the file is a project file. */
  public static boolean isProjectFile(File f) {
    File canonicalFile = IOUtil.attemptCanonicalFile(f);
    String fileName = canonicalFile.getPath();
    return isProjectFile(fileName);
  }

  // Language levels processing disabled
//  /** getFileWithDifferentExt("A.java", ".java", ".dj") --> "A.dj"
//    * @return matching file with extension dest for a file with extension source. */
//  public static String getFileWithDifferentExt(String fileName, String source, String dest) {
//    if (fileName.endsWith(source)) {
//      return fileName.substring(0, fileName.lastIndexOf(source)) + dest;
//    }
//    else return fileName;
//  }
//
//  /** getFileWithDifferentExt(new File("A.java"), ".java", ".dj") ~= new File("A.dj")
//    * @return matching file with extension dest for a file with extension source. */
//  public static File getFileWithDifferentExt(File f, String source, String dest) {
//    File canonicalFile = IOUtil.attemptCanonicalFile(f);
//    String fileName = canonicalFile.getPath();
//    return new File(getFileWithDifferentExt(fileName, source, dest));
//  }
   
  /** Returns the relative directory (from the source root) that the source file with this qualifed name will be in, 
    * given its package. Returns the empty string for classes without packages.
    * @param className The fully qualified class name
    */
  public static String getPackageDir(String className) {
    // Only keep up to the last dot
    int lastDotIndex = className.lastIndexOf(".");
    if (lastDotIndex == -1) {
      // No dots, so no package
      return "";
    }
    else {
      String packageName = className.substring(0, lastDotIndex);
      packageName = packageName.replace('.', File.separatorChar);
      return packageName + File.separatorChar;
    }
  }
  
  /** @return the file without the extension; the dot is removed too. */
  public static String removeExtension(String fileName) {
    int lastDotIndex = fileName.lastIndexOf(".");
    if (lastDotIndex == -1) {
      // No dots, so no package
      return fileName;
    }
    return fileName.substring(0, lastDotIndex);
  }
  
  /** @return the extension, including the dot. */
  public static String getExtension(String fileName) {
    int lastDotIndex = fileName.lastIndexOf(".");
    if (lastDotIndex == -1) {
      // No dots, so no package
      return "";
    }
    return fileName.substring(lastDotIndex);
  }
 
  /** Adds element to list provided list does not already contain element. */
  public static <T> boolean addUnique(ArrayList<T> list, T element) {
    boolean result = ! list.contains(element);
    if (result) list.add(element);
    return result;
  }

}
