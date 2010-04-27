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

package edu.rice.cs.drjava.model;

import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import java.util.List;
import java.util.ArrayList;
  
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;

/** Test implementation of the document fetching methods in the GlobalModel interface. */
public class TestDocGetter extends DummyGlobalModel {
  
  /** Storage for documents and File keys. */
  HashMap<File, OpenDefinitionsDocument> docs;

  /** Convenience constructor for no-documents case. */
  public TestDocGetter() { this(new File[0], new String[0]); }

  /** Primary constructor, builds OpenDefDocs from Strings.
   *  @param files the keys to use when getting OpenDefDocs
   *  @param texts the text to put in the OpenDefDocs
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
      try { doc.insertString(0, texts[i], null); }
      catch (BadLocationException e) { throw new UnexpectedException(e); }
      docs.put(files[i], odoc);
    }
  }

  public OpenDefinitionsDocument getDocumentForFile(File file)
    throws IOException {
    // Try to find the key in docs.
    if (docs.containsKey(file)) return docs.get(file);
    else throw new IllegalStateException("TestDocGetter can't open new files!");
  }
  
  public List<OpenDefinitionsDocument> getOpenDefinitionsDocuments() {
    return new ArrayList<OpenDefinitionsDocument>(docs.values());
  }

  /** Test implementation of OpenDefinitionsDocument interface. */
  private static class TestOpenDoc extends DummyOpenDefDoc {
    DefinitionsDocument _doc;
    File _file;
    TestOpenDoc(DefinitionsDocument d) {
      _doc = d;
      _defDoc = d;
      _file = FileOps.NULL_FILE;
    }

    /** This is the only method that we care about. */
    public DefinitionsDocument getDocument() { return _doc; }

    /** Okay, I lied.  We need this one, too. */
    public File getFile() throws FileMovedException  { return _file; }
    
    public void setFile(File f) { _file = f; }
  }
}
