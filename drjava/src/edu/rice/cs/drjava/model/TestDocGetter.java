/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 *
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
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
public class TestDocGetter extends DummyGetDocuments {
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
      doc.setFile(files[i]);
      try {
        doc.insertString(0, texts[i], null);
      }
      catch (BadLocationException e) {
        throw new UnexpectedException(e);
      }
      docs.put(files[i], new TestOpenDoc(doc));
    }
  }

  public OpenDefinitionsDocument getDocumentForFile(File file)
    throws IOException, OperationCanceledException {
    // Try to find the key in docs.
    if (docs.containsKey(file)) {
      return docs.get(file);
    }
    else {
      throw new IllegalStateException("TestDocGetter can't open new files!");
    }
  }

  /**
   * Test implementation of OpenDefinitionsDocument interface.
   */
  private static class TestOpenDoc extends DummyOpenDefDoc {
    DefinitionsDocument _doc;
    TestOpenDoc(DefinitionsDocument doc) {
      _doc = doc;
    }

    /**
     * This is the only method that we care about.
     */
    public DefinitionsDocument getDocument() {
      return _doc;
    }

    /**
     * Okay, I lied.  We need this one, too.
     */
    public File getFile() throws IllegalStateException, FileMovedException  {
      return _doc.getFile();
    }
  }
}
