/* $Id$ */

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
