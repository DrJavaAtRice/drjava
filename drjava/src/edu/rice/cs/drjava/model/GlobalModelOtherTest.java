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

import junit.framework.*;

import java.io.*;

import java.util.Vector;
import javax.swing.text.BadLocationException;
import junit.extensions.*;
import java.util.LinkedList;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.util.*;

/**
 * A test on the GlobalModel that does deals with everything outside of
 * simple file operations, e.g., compile, quit.
 *
 * @version $Id$
 */
public class GlobalModelOtherTest extends GlobalModelTestCase implements OptionConstants {
  /**
   * Constructor.
   * @param  String name
   */
  public GlobalModelOtherTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return new TestSuite(GlobalModelOtherTest.class);
  }

  /**
   * Checks that System.exit is handled appropriately from
   * interactions frame.
   */
  public void testInteractionPreventedFromExit()
    throws BadLocationException, InterruptedException
  {
    TestListener listener = new TestListener() {
      public void interactionStarted() {
        interactionStartCount++;
      }

      public void interactionsExited(int status) {
        assertInteractionStartCount(1);
        interactionsExitedCount++;
        lastExitStatus = status;
      }

      public void interactionsReset() {
        synchronized(this) {
          assertInteractionStartCount(1);
          assertInteractionsExitedCount(1);
          interactionsResetCount++;
          this.notify();
        }
      }
    };

    _model.addListener(listener);
    synchronized(listener) {
      interpretIgnoreResult("System.exit(23);");
      listener.wait();
    }
    _model.removeListener(listener);

    listener.assertInteractionStartCount(1);
    listener.assertInteractionsResetCount(1);
    listener.assertInteractionsExitedCount(1);
    assertEquals("exit status", 23, listener.lastExitStatus);
  }

  /**
   * Checks that the interpreter can be aborted and then work
   * correctly later.
   * Part of what we check here is that the interactions classpath
   * is correctly reset after aborting interactions. That is, we ensure
   * that the compiled class is still visible after aborting. This was
   * broken in drjava-20020108-0958 -- or so I thought. I can't consistently
   * reproduce the problem in the UI (seems to show up using IBM's JDK only),
   * and I can never reproduce it in the test case. Grr.
   *
   * OK, now I found the explanation: We were in some cases running two new JVMs
   * on an abort. I fixed the problem in {@link MainJVM#restartInterpreterJVM}.
   */
  public void testInteractionAbort()
    throws BadLocationException, InterruptedException, IOException
  {
    //System.err.println("Entering testInteractionAbort");
    _doCompile(setupDocument(FOO_TEXT), tempFile());
    final String beforeAbort = interpret("Foo.class.getName()");
    assertEquals("Foo", beforeAbort);
    
    TestListener listener = new TestListener() {
      public void interactionStarted() {
        //System.err.println("start notice");
        interactionStartCount++;
      }

      public void interactionEnded() {
        // this can only happen on the second interpretation!
        assertInteractionStartCount(2);
        interactionEndCount++;
      }

      public void interactionsExited(int status) {
        //System.err.println("exit notice");
        try {
          Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
          //System.err.println("Interrupted!");
        }
        assertInteractionStartCount(1);
        interactionsExitedCount++;
      }

      public void interactionsReset() {
        synchronized(this) {
          //System.err.println("reset notice");
          assertInteractionStartCount(1);
          assertInteractionsExitedCount(1);
          interactionsResetCount++;
          this.notify();
        }
      }
    };

    _model.addListener(listener);
    synchronized(listener) {
      interpretIgnoreResult("while (true) {}");
      listener.assertInteractionStartCount(1);
      //System.err.println("about to abort");
      _model.abortCurrentInteraction();
      //System.err.println("about to wait for abort");
      listener.wait();
    }

    //System.err.println("waiting done");
    listener.assertInteractionsResetCount(1);
    //System.err.println("after wait");
    listener.assertInteractionsExitedCount(1);
    //System.err.println("after wait");
    _model.removeListener(listener);

    // now make sure it still works!
    //System.err.println("after wait");
    assertEquals("5", interpret("5"));

    // make sure we can still see class foo
    //System.err.println("about to check Foo");
    final String afterAbort = interpret("Foo.class.getName()");
    assertEquals("Foo", afterAbort);
    //System.err.println("done check Foo: " + afterAbort);
  }

  /**
   * Checks that reset console works.
   */
  public void testResetConsole()
    throws BadLocationException, InterruptedException
  {
    //System.err.println("Entering testResetConsole");
    TestListener listener = new TestListener() {
      public void interactionStarted() {}
      public void interactionEnded() {}

      public void consoleReset() {
        consoleResetCount++;
      }
    };

    _model.addListener(listener);

    _model.resetConsole();
    assertEquals("Length of console text",
                 0,
                 _model.getConsoleDocument().getLength());

    listener.assertConsoleResetCount(1);

    interpretIgnoreResult("System.out.print(\"a\");");

    // alas, there's no very good way to know when it's done
    // so we just wait some time hoping the println will have happened
    int i;
    for (i = 0; i < 50; i++) {
      if (_model.getConsoleDocument().getLength() == 1) {
        break;
      }

      Thread.currentThread().sleep(100);
    }

    //System.err.println("wait i=" + i);

    assertEquals("Length of console text",
                 1,
                 _model.getConsoleDocument().getLength());


    _model.resetConsole();
    assertEquals("Length of console text",
                 0,
                 _model.getConsoleDocument().getLength());

    listener.assertConsoleResetCount(2);
  }

  /**
   * Saves to the given file, and then compiles the given document.
   * The compile is expected to succeed and it is checked to make sure it worked
   * reasonably.
   */
  private void _doCompile(OpenDefinitionsDocument doc, File file)
    throws IOException
  {
    doc.saveFile(new FileSelector(file));

    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    doc.startCompile();
    listener.checkCompileOccurred();
    assertCompileErrorsPresent(false);
    _model.removeListener(listener);
  }

  /**
   * Creates a new class, compiles it and then checks that the REPL
   * can see it.
   */
  public void testInteractionsCanSeeCompile()
    throws BadLocationException, IOException
  {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    _doCompile(doc, tempFile());

    String result = interpret("new Foo().getClass().getName()");

    assertEquals("interactions result",
                 "Foo",
                 result);
  }

  /**
   * Checks that updating a class and recompiling it is visible from
   * the REPL.
   */
  public void testInteractionsCanSeeChangedClass()
    throws BadLocationException, IOException
  {
    final String text_before = "class Foo { public int m() { return ";
    final String text_after = "; } }";
    final int num_iterations = 5;
    File file;
    OpenDefinitionsDocument doc;


    for (int i = 0; i < num_iterations; i++) {
      doc = setupDocument(text_before + i + text_after);
      file = tempFile(i);
      _doCompile(doc, file);

      assertEquals("interactions result, i=" + i,
          String.valueOf(i),
          interpret("new Foo().m()"));
    }
  }

  /**
   * Checks that an anonymous inner class can be defined in the repl!
   */
  public void testInteractionsDefineAnonymousInnerClass()
    throws BadLocationException, IOException
  {
    final String interface_text = "public interface I { int getValue(); }";
    final File file = createFile("I.java");

    OpenDefinitionsDocument doc;

    doc = setupDocument(interface_text);
    _doCompile(doc, file);

    for (int i = 0; i < 5; i++) {
      String s = "new I() { public int getValue() { return " + i + "; } }.getValue()";

      assertEquals("interactions result, i=" + i,
                   String.valueOf(i),
                   interpret(s));
    }
  }

  public void testGetSourceRootDefaultPackage()
    throws BadLocationException, IOException, InvalidPackageException
  {
    // Get source root (current directory only)
    File[] roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 1, roots.length);
    assertEquals("source root (current directory)", 
                 new File(DrJava.CONFIG.getSetting(WORKING_DIRECTORY).toString()),
                 roots[0]);

    // Create temp directory
    File baseTempDir = tempDirectory();

    // Now make subdirectory a/b/c
    File subdir = new File(baseTempDir, "a");
    subdir = new File(subdir, "b");
    subdir = new File(subdir, "c");
    subdir.mkdirs();

    // Save the footext to Foo.java in the subdirectory
    File fooFile = new File(subdir, "Foo.java");
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    doc.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    // Get source roots
    roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 2, roots.length);
    assertEquals("source root", subdir, roots[1]);
  }

  public void testGetSourceRootPackageThreeDeepValid()
    throws BadLocationException, IOException, InvalidPackageException
  {
    // Create temp directory
    File baseTempDir = tempDirectory();

    // Now make subdirectory a/b/c
    File subdir = new File(baseTempDir, "a");
    subdir = new File(subdir, "b");
    subdir = new File(subdir, "c");
    subdir.mkdirs();

    // Save the footext to Foo.java in the subdirectory
    File fooFile = new File(subdir, "Foo.java");
    OpenDefinitionsDocument doc =
      setupDocument("package a.b.c;\n" + FOO_TEXT);
    doc.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    // Since we had the package statement the source root should be base dir
    // (also contains currDir)
    File[] roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 2, roots.length);
    assertEquals("source root", baseTempDir, roots[1]);

  }

  public void testGetSourceRootPackageThreeDeepInvalid()
    throws BadLocationException, IOException
  {
    // Create temp directory
    File baseTempDir = tempDirectory();

    // Now make subdirectory a/b/d
    File subdir = new File(baseTempDir, "a");
    subdir = new File(subdir, "b");
    subdir = new File(subdir, "d");
    subdir.mkdirs();

    // Save the footext to Foo.java in the subdirectory
    File fooFile = new File(subdir, "Foo.java");
    OpenDefinitionsDocument doc =
      setupDocument("package a.b.c;\n" + FOO_TEXT);
    doc.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    // The package name is wrong so this should return only currDir
    File[] roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 1, roots.length);
    assertEquals("source root (current directory)", 
                 new File(DrJava.CONFIG.getSetting(WORKING_DIRECTORY).toString()),
                 roots[0]);
  }

  public void testGetSourceRootPackageOneDeepValid()
    throws BadLocationException, IOException
  {
    // Create temp directory
    File baseTempDir = tempDirectory();

    // Now make subdirectory a
    File subdir = new File(baseTempDir, "a");
    subdir.mkdir();

    // Save the footext to Foo.java in the subdirectory
    File fooFile = new File(subdir, "Foo.java");
    OpenDefinitionsDocument doc = setupDocument("package a;\n" + FOO_TEXT);
    doc.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    // Since we had the package statement the source root should be base dir
    //  (also currDir)
    File[] roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 2, roots.length);
    assertEquals("source root", baseTempDir, roots[1]);

  }


  public void testGetMultipleSourceRootsDefaultPackage()
    throws BadLocationException, IOException
  {
    // Create temp directory
    File baseTempDir = tempDirectory();

    // Now make subdirectories a, b
    File subdir1 = new File(baseTempDir, "a");
    subdir1.mkdir();
    File subdir2 = new File(baseTempDir, "b");
    subdir2.mkdir();

    // Save the footext to Foo.java in subdirectory 1
    File file1 = new File(subdir1, "Foo.java");
    OpenDefinitionsDocument doc1 = setupDocument(FOO_TEXT);
    doc1.saveFileAs(new FileSelector(file1));

    // Save the bartext to Bar.java in subdirectory 1
    File file2 = new File(subdir1, "Bar.java");
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    doc2.saveFileAs(new FileSelector(file2));

    // Save the bartext to Bar.java in subdirectory 2
    File file3 = new File(subdir2, "Bar.java");
    OpenDefinitionsDocument doc3 = setupDocument(BAR_TEXT);
    doc3.saveFileAs(new FileSelector(file3));

    // No events should fire
    _model.addListener(new TestListener());

    // Get source roots (should be 3: no duplicates, but also currDir)
    File[] roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 3, roots.length);
    File root1 = roots[1];
    File root2 = roots[2];

    // Make sure both source roots are in set
    // But we don't care about the order
    if (!( (root1.equals(subdir1) && root2.equals(subdir2)) ||
           (root1.equals(subdir2) && root2.equals(subdir1)) ))
    {
      fail("source roots did not match");
    }
  }

}
