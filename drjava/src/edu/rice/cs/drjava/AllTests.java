/* $Id$
 *
 * File history:
 *
 * $Log$
 * Revision 1.2  2001/06/19 16:26:54  javaplt
 * Added CVS tags to comments (Id and Log).
 * Changed all uses of assert() in JUnit tests to assertTrue(). assert() has been
 * deprecated because it is a builtin keyword in Java 1.4.
 * Fixed build.xml to test correctly after compile and to add CVS targets.
 *
 */

package edu.rice.cs.drjava;

import junit.framework.*;
import java.util.Vector;
import junit.extensions.*;

public class AllTests
{
  public static Test suite()
  {
    TestSuite allSuite = new TestSuite("All DrJava tests");
//    allSuite.addTest(DefinitionsTest.suite());
    allSuite.addTest(BraceTest.suite());
    allSuite.addTest(GapTest.suite());
		allSuite.addTest(ModelListTest.suite());
    allSuite.addTest(ReducedModelTest.suite());
		

    return allSuite;
  }
}
