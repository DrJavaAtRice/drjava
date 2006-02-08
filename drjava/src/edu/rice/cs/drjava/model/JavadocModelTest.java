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

package edu.rice.cs.drjava.model;

import edu.rice.cs.drjava.model.definitions.*;

import java.io.*;

import junit.framework.TestCase;

/**
 * Tests the functionality provided by an implementation of JavadocModel.
 * For now, this class is hard-coded to test DefaultJavadocModel, but it can be
 * extended to test any implementation of the interface.
 * @version $Id$
 */
public class JavadocModelTest extends TestCase {
  
  /**
   * Field needed by testUnsavedSuggestedDirectory
   */
  private File _storedFile;

  /**
   * Tests that a simple suggestion can be made for the destination directory.
   */
  public void testSimpleSuggestedDirectory() {
    GlobalModel getDocs = new DummyGlobalModel() {
      public boolean hasModifiedDocuments() {
        return false;  // pretend all docs are saved
      }
      public boolean hasUntitledDocuments() {
        return false; // pretend no docs are untitled
      }
    };
    JavadocModel jModel = new DefaultJavadocModel(getDocs);
    final File file = new File(System.getProperty("user.dir"));
    OpenDefinitionsDocument doc = new DummyOpenDefDoc() {
      public File getSourceRoot() throws InvalidPackageException {
        return file;
      }
    };

    File suggestion = jModel.suggestJavadocDestination(doc);
    File expected = new File(file, JavadocModel.SUGGESTED_DIR_NAME);
    assertEquals("simple suggested destination", expected, suggestion);
  }
  
  /**
   * Tests that a suggestion can be made for an unsaved file,
   * if the user chooses to save it.
   */
  public void testUnsavedSuggestedDirectory() {
    _storedFile = null;
    
    GlobalModel getDocs = new DummyGlobalModel() {
      public boolean hasModifiedDocuments() {
        return true;  // pretend doc is unsaved
      }
    };
    JavadocModel jModel = new DefaultJavadocModel(getDocs);
    final File file = new File(System.getProperty("user.dir"));

    // Make sure it doesn't return a file until it's saved.
    JavadocListener listener = new JavadocListener() {
      public void saveBeforeJavadoc() {
        _storedFile = file;
      }
      public void javadocStarted() { }
      public void javadocEnded(boolean success, File destDir, boolean allDocs) { }
    };
    jModel.addListener(listener);
    
    OpenDefinitionsDocument doc = new DummyOpenDefDoc() {
      public File getSourceRoot() throws InvalidPackageException {
        return _storedFile;
      }
    };

    File suggestion = jModel.suggestJavadocDestination(doc);
    File expected = new File(file, JavadocModel.SUGGESTED_DIR_NAME);
    assertEquals("simple suggested destination", expected, suggestion);
  }

  /**
   * Tests that a no suggestion can be made for the destination directory
   * if there is no valid source root.
   */
  public void testNoSuggestedDirectory() {
    GlobalModel getDocs = new DummyGlobalModel() {
      public boolean hasModifiedDocuments() {
        return false;  // pretend all docs are saved
      }
      public boolean hasUntitledDocuments() {
        return false;  // pretent no docs are untitled
      }
    };
    JavadocModel jModel = new DefaultJavadocModel(getDocs);
//    final File file = new File(System.getProperty("user.dir"));
    OpenDefinitionsDocument doc = new DummyOpenDefDoc() {
      public File getSourceRoot() throws InvalidPackageException {
        throw new InvalidPackageException(-1, "invalid package");
      }
    };

    File suggestion = jModel.suggestJavadocDestination(doc);
    assertNull("suggestion should be null", suggestion);
  }

  public void testFileDefaultPackage() {

  }

  public void testFileOnePackage() {

  }

  public void testFilesOnePackage() {

  }

  public void testFilesMultiplePackages() {

  }

  public void testWarnings() {

  }

  public void testErrors() {

  }

  public void testSaveFirst() {

  }

  public void testPromptForDestination() {

  }

  public void testExtractErrors() {

  }

  public void testParseLine() {

  }

  /**
   * Be sure to test: -tag require:a:"Require:"
   */
  public void testCustomArguments() {

  }
}
