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

public class GapTest extends TestCase
{
	
	public GapTest(String name)
		{
			super(name);
		}
	
	public void setUp()
		{
		}

	public static Test suite()
		{
			return new TestSuite(GapTest.class);
		}

	public void testGrow()
		{
			Gap gap0 = new Gap(0, ReducedToken.FREE);
			Gap gap1 = new Gap(1, ReducedToken.FREE);
			gap0.grow(5);
			assertEquals(5, gap0.getSize());

			gap0.grow(0);
			assertEquals(5, gap0.getSize());
			
			gap1.grow(-6);
			assertEquals(1, gap1.getSize());
		}

	public void testShrink()
		{
			Gap gap0 = new Gap(5, ReducedToken.FREE);
			Gap gap1 = new Gap(1, ReducedToken.FREE);
			gap0.shrink(3);
			assertEquals(2, gap0.getSize());

			gap0.shrink(0);
			assertEquals(2, gap0.getSize());

		  gap1.shrink(3);
			assertEquals(1, gap1.getSize());

			gap1.shrink(-1);
			assertEquals(1, gap1.getSize());
		}

}
