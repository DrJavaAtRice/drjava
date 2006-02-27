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

import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import javax.swing.text.BadLocationException;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;

/**
 * Test implementation of IGetDocuments interface.
 */
public class TestDocGetter extends DummyGlobalModel {
  /**
   * Storage for documents and File keys.
   */
  HashMap<File, OpenDefinitionsDocument> docs;

  /**
   * Convenience constructor for no-documents case.
   */
  public TestDocGetter() {
    this(new File[0], new String[0]);
  }

  /**
   * Primary constructor, builds OpenDefDocs from Strings.
   * @param files the keys to use when getting OpenDefDocs
   * @param texts the text to put in the OpenDefDocs
   */
  public TestDocGetter(File[] files, String[] texts) {
    if (files.length != texts.length) {
      throw new IllegalArgumentException("Argument arrays must match in size.");
    }

    docs = new HashMap<File, OpenDefinitionsDocument>(texts.length * 2);

    GlobalEventNotifier en = new GlobalEventNotifier();
    for (int i = 0; i < texts.length; i++) {
      DefinitionsDocument doc = new DefinitionsDocument(en);
      OpenDefinitionsDocument odoc = new TestOpenDoc(doc);
      odoc.setFile(files[i]);
      try {
        doc.insertString(0, texts[i], null);
      }
      catch (BadLocationException e) {
        throw new UnexpectedException(e);
      }
      docs.put(files[i], odoc);
    }
  }

  public OpenDefinitionsDocument getDocumentForFile(File file)
    throws IOException {
    // Try to find the key in docs.
    if (docs.containsKey(file)) return docs.get(file);
    else throw new IllegalStateException("TestDocGetter can't open new files!");
  }

  /** Test implementation of OpenDefinitionsDocument interface. */
  private static class TestOpenDoc extends DummyOpenDefDoc {
    DefinitionsDocument _doc;
    File _file;
    TestOpenDoc(DefinitionsDocument d) {
      _doc = d;
      _defDoc = d;
      _file = null;
    }

    /** This is the only method that we care about. */
    protected DefinitionsDocument getDocument() { return _doc; }

    /** Okay, I lied.  We need this one, too. */
    public File getFile() throws FileMovedException  { return _file; }
    
    public void setFile(File f) { _file = f; }
  }
}
