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

package edu.rice.cs.drjava.model.javadoc;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.DummyGlobalModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.DummyOpenDefDoc;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.model.compiler.CompilerListener;

import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.util.FileOps;

import java.io.File;

/**
 * Tests the functionality provided by an implementation of JavadocModel.
 * For now, this class is hard-coded to test DefaultJavadocModel, but it can be
 * extended to test any implementation of the interface.
 * @version $Id: JavadocModelTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class JavadocModelTest extends DrJavaTestCase {
  
  /** Field needed by testUnsavedSuggestedDirectory */
  private File _storedFile;

  /** Tests that a simple suggestion can be made for the destination directory. */
  public void testSimpleSuggestedDirectory() {
    GlobalModel getDocs = new DummyGlobalModel() {
      public boolean hasModifiedDocuments() {
        return false;  // pretend all docs are saved
      }
      public boolean hasUntitledDocuments() {
        return false; // pretend no docs are untitled
      }
    };
    // TODO: define so that tools.jar doesn't need to be on the class path
    JavadocModel jModel = new DefaultJavadocModel(getDocs, null, ReflectUtil.SYSTEM_CLASS_PATH);
    final File file = new File(System.getProperty("user.dir"));
    OpenDefinitionsDocument doc = new DummyOpenDefDoc() {
      public File getSourceRoot() throws InvalidPackageException { return file; }
    };

    File suggestion = jModel.suggestJavadocDestination(doc);
    File expected = new File(file, JavadocModel.SUGGESTED_DIR_NAME);
    assertEquals("simple suggested destination", expected, suggestion);
  }
  
  /** Tests that a suggestion can be made for an unsaved file, if the user chooses to save it. */
  public void testUnsavedSuggestedDirectory() {
    _storedFile = FileOps.NULL_FILE;
    
    GlobalModel getDocs = new DummyGlobalModel() {
      public boolean hasModifiedDocuments() {
        return true;  // pretend doc is unsaved
      }
    };
    JavadocModel jModel = new DefaultJavadocModel(getDocs, null, ReflectUtil.SYSTEM_CLASS_PATH);
    final File file = new File(System.getProperty("user.dir"));

    // Make sure it doesn't return a file until it's saved.
    JavadocListener listener = new JavadocListener() {
      public void saveBeforeJavadoc() { _storedFile = file; }
      public void compileBeforeJavadoc(final CompilerListener afterCompile) { }
      public void javadocStarted() { }
      public void javadocEnded(boolean success, File destDir, boolean allDocs) { }
    };
    jModel.addListener(listener);
    
    OpenDefinitionsDocument doc = new DummyOpenDefDoc() {
      public File getSourceRoot() throws InvalidPackageException { return _storedFile; }
    };

    File suggestion = jModel.suggestJavadocDestination(doc);
    File expected = new File(file, JavadocModel.SUGGESTED_DIR_NAME);
    assertEquals("simple suggested destination", expected, suggestion);
  }

  /** Tests that a no suggestion can be made for the destination directory if there is no valid source root. */
  public void testNoSuggestedDirectory() {
    GlobalModel getDocs = new DummyGlobalModel() {
      public boolean hasModifiedDocuments() { return false;  /* pretend all docs are saved */ }
      public boolean hasUntitledDocuments() { return false;  /* pretend no docs are untitled */ }
    };
    JavadocModel jModel = new DefaultJavadocModel(getDocs, null, null);
//    final File file = new File(System.getProperty("user.dir"));
    OpenDefinitionsDocument doc = new DummyOpenDefDoc() {
      public File getSourceRoot() throws InvalidPackageException {
        throw new InvalidPackageException(-1, "invalid package");
      }
    };

    File suggestion = jModel.suggestJavadocDestination(doc);
    assertNull("suggestion should be null", suggestion);
  }

  public void testFileDefaultPackage() { }
  public void testFileOnePackage() { }
  public void testFilesOnePackage() { }
  public void testFilesMultiplePackages() { }
  public void testWarnings() { }
  public void testErrors() { }
  public void testSaveFirst() { }
  public void testPromptForDestination() { }
  public void testExtractErrors() { }
  public void testParseLine() { }
  /** Be sure to test: -tag require:a:"Require:" */
  public void testCustomArguments() { }
}
