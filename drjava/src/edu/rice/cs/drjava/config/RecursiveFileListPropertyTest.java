package edu.rice.cs.drjava.config;

import junit.framework.TestCase;
import junit.framework.Assert;

import java.io.File;
import java.io.FileFilter;

/**
 * A JUnit test case class.
 * Every method starting with the word "test" will be called when running
 * the test with JUnit.
 */
public class RecursiveFileListPropertyTest extends TestCase {
  
  /**
   * Test the RegexFilter inner class.
   */
  public void testRegexFilter() {
    FileFilter filter = new RecursiveFileListProperty.RegexFilter(".");
    Assert.assertTrue("Does not accept single char strings", filter.accept(new File("a")));
    Assert.assertFalse("Does not reject non-length 1 strings", filter.accept(new File("abc")));
  }
  
  /**
   * Test the FileMaskFilter inner class.
   */
  public void testFileMaskFilter(){
     RecursiveFileListProperty.FileMaskFilter filter = new RecursiveFileListProperty.FileMaskFilter("?");
     
     filter.addIncludedFile(new File("abc"));
     Assert.assertTrue("Doesn't accept included file", filter.accept(new File("abc")));
     Assert.assertTrue("Doesn't accept regex filter", filter.accept(new File("a")));
     
     filter.removeIncludedFile(new File("abc"));
     Assert.assertFalse("Doesn't reject removed file", filter.accept(new File("abc")));
     
     filter.addExcludedFile(new File("b"));
     Assert.assertFalse("Accepts excluded file", filter.accept(new File("b")));
     
     filter.removeExcludedFile(new File("b"));
     Assert.assertTrue("Rejects no-longer excluded file", filter.accept(new File("b")));
     
     filter.clearIncludedFile();
     filter.clearExcludedFile();
  }
  
}
