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

package edu.rice.cs.util.jar;

import edu.rice.cs.drjava.DrJavaTestCase;

import java.io.*;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class JarCreationTest extends DrJavaTestCase {
  /** Tests the creation of manifest files through the ManifestWriter class
   */
  public void testCreateManifest() {
    ManifestWriter mw = new ManifestWriter();
    Manifest manifest = mw.getManifest();
    assertTrue("should have version attribute", manifest.getMainAttributes().containsKey(Attributes.Name.MANIFEST_VERSION));
    assertEquals("should have version attribute", "1.0", manifest.getMainAttributes().get(Attributes.Name.MANIFEST_VERSION));
    assertEquals("should only have manifest attribute", 1, manifest.getMainAttributes().size());

    mw.setMainClass("edu.rice.cs.drjava.DrJava");
    manifest = mw.getManifest();
    assertTrue("should have version attribute", manifest.getMainAttributes().containsKey(Attributes.Name.MANIFEST_VERSION));
    assertEquals("should have version attribute", "1.0", manifest.getMainAttributes().get(Attributes.Name.MANIFEST_VERSION));
    assertTrue("should have main class attribute", manifest.getMainAttributes().containsKey(Attributes.Name.MAIN_CLASS));
    assertEquals("should have main class attribute", "edu.rice.cs.drjava.DrJava", manifest.getMainAttributes().get(Attributes.Name.MAIN_CLASS));
    assertEquals("should only have manifest attribute", 2, manifest.getMainAttributes().size());


    mw = new ManifestWriter();
    mw.addClassPath("koala.dynamicjava");
    manifest = mw.getManifest();
    assertTrue("should have version attribute", manifest.getMainAttributes().containsKey(Attributes.Name.MANIFEST_VERSION));
    assertEquals("should have version attribute", "1.0", manifest.getMainAttributes().get(Attributes.Name.MANIFEST_VERSION));
    assertTrue("should have classpath attribute", manifest.getMainAttributes().containsKey(Attributes.Name.CLASS_PATH));
    assertEquals("should have correct classpath", "koala.dynamicjava", manifest.getMainAttributes().get(Attributes.Name.CLASS_PATH));
    assertEquals("have version and classpath", 2, manifest.getMainAttributes().size());

    mw.addClassPath("edu.rice.cs.util");
    manifest = mw.getManifest();
    assertTrue("should have version attribute", manifest.getMainAttributes().containsKey(Attributes.Name.MANIFEST_VERSION));
    assertEquals("should have version attribute", "1.0", manifest.getMainAttributes().get(Attributes.Name.MANIFEST_VERSION));
    assertTrue("should have classpath attribute", manifest.getMainAttributes().containsKey(Attributes.Name.CLASS_PATH));
    assertEquals("should have correct classpath", "koala.dynamicjava edu.rice.cs.util", manifest.getMainAttributes().get(Attributes.Name.CLASS_PATH));
    assertEquals("have version and classpath", 2, manifest.getMainAttributes().size());

    mw.setMainClass("edu.rice.cs.drjava.DrJava");
    manifest = mw.getManifest();
    assertTrue("should have version attribute", manifest.getMainAttributes().containsKey(Attributes.Name.MANIFEST_VERSION));
    assertEquals("should have version attribute", "1.0", manifest.getMainAttributes().get(Attributes.Name.MANIFEST_VERSION));
    assertTrue("should have classpath attribute", manifest.getMainAttributes().containsKey(Attributes.Name.CLASS_PATH));
    assertEquals("should have correct classpath", "koala.dynamicjava edu.rice.cs.util", manifest.getMainAttributes().get(Attributes.Name.CLASS_PATH));
    assertTrue("should have main class attribute", manifest.getMainAttributes().containsKey(Attributes.Name.MAIN_CLASS));
    assertEquals("should have main class attribute", "edu.rice.cs.drjava.DrJava", manifest.getMainAttributes().get(Attributes.Name.MAIN_CLASS));
    assertEquals("have version and classpath", 3, manifest.getMainAttributes().size());
  }

  /** Test create addDirectoryRecursive
   */
  public void testCreateJarFromDirectoryRecursive() {
    File dir = new File("temp_dir");
    dir.mkdir();
    File dir1 = new File(dir, "dir");
    dir1.mkdir();
    File[] files = new File[]{new File(dir, "test.java"),
                              new File(dir, "test.class"),
                              new File(dir, "p1.tmp"),
                              new File(dir1, "test1.java"),
                              new File(dir1, "out.class"),
                              new File(dir1, "out.out.out.class"),
                              new File(dir1, "that.java")};
    dir.deleteOnExit();
    dir1.deleteOnExit();
    for(int i = 0; i < files.length; i++)
      files[i].deleteOnExit();
    try {

      PrintWriter pw = null;
      for (int i = 0; i < files.length; i++) {
        pw = new PrintWriter(new FileOutputStream(files[i]));
        pw.write(files[i].getName());
        pw.close();
      }

      File f = new File("test~.jar");
      f.deleteOnExit();
      JarBuilder jb = new JarBuilder(f);
      jb.addDirectoryRecursive(dir, "");
      jb.close();

      testArchive(f,
              new TreeSet<String>(Arrays.asList(new String[]{files[0].getName(),
                                                             files[1].getName(),
                                                             files[2].getName(),
                                                             "dir/" + files[3].getName(),
                                                             "dir/" + files[4].getName(),
                                                             "dir/" + files[5].getName(),
                                                             "dir/" + files[6].getName()})));

      jb = new JarBuilder(f);
      jb.addDirectoryRecursive(dir, "", new FileFilter() {
        public boolean accept(File pathname) {
          return pathname.getName().endsWith(".class") || pathname.isDirectory();
        }
      });
      jb.close();

      testArchive(f,
              new TreeSet<String>(Arrays.asList(new String[]{files[1].getName(),
                                                             "dir/" + files[4].getName(),
                                                             "dir/" + files[5].getName()})));

      jb = new JarBuilder(f);
      jb.addDirectoryRecursive(dir, "", new FileFilter() {
        public boolean accept(File pathname) {
          return pathname.getName().endsWith(".java") || pathname.isDirectory();
        }
      });
      jb.close();

      testArchive(f,
              new TreeSet<String>(Arrays.asList(new String[]{files[0].getName(),
                                                             "dir/" + files[3].getName(),
                                                             "dir/" + files[6].getName()})));

    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

  }

  /** Test the manual creation of jar files
   */
  public void testCreateJar() throws IOException {
    File f = edu.rice.cs.plt.io.IOUtil.createAndMarkTempFile("test", ".jar");
    File add = null;
    try {
      String fileContents = "public class JarTest {" +
              "\tpublic String getClassName() {" +
              "\t\treturn \"JarTest\";" +
              "\t}" +
              "}";

      add = File.createTempFile("JarTest",".java").getCanonicalFile();
      add.deleteOnExit();

      PrintWriter pw = new PrintWriter(new FileOutputStream(add));
      pw.write(fileContents);
      pw.close();

      JarBuilder jb = new JarBuilder(f);
      jb.addFile(add, "", "JarTest.java");
      jb.addFile(add, "dir", "JarTest.java");
      jb.close();


      testArchive(f,
              new TreeSet<String>(Arrays.asList(new String[]{"JarTest.java",
                                                             "dir/JarTest.java"})));

      JarInputStream jarStream = new JarInputStream(new FileInputStream(f), true);

      JarEntry ent = jarStream.getNextJarEntry();
      assertTrue("should have JarTest", ent != null);
      assertEquals("names should match", "JarTest.java", ent.getName());

      ent = jarStream.getNextJarEntry();
      assertTrue("should have JarTest", ent != null);
      assertEquals("names should match", "dir/JarTest.java", ent.getName());
    }
    catch (IOException e) {
      e.printStackTrace();
      fail("failed test");
    }
  }

  /** Check that all files in an a Set are in the jar file
   * @param jar the jar file to check
   * @param fileNames the set of the names of files
   */
  private void testArchive(File jar, Set<String> fileNames) {
    JarInputStream jarStream = null;
    try {
      jarStream = new JarInputStream(new FileInputStream(jar), true);

      JarEntry ent = null;
      while( (ent = jarStream.getNextJarEntry()) != null ) {
        assertTrue("found " + ent.getName() + " should be in list", fileNames.contains(ent.getName()));
        fileNames.remove(ent.getName());
      }
    }
    catch (IOException e) {
      fail("couldn't open file");
    } finally {
      if( jarStream != null)
        try {
          jarStream.close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
    }
    assertEquals("all listed files should have been in archive", 0, fileNames.size());
  }
}
