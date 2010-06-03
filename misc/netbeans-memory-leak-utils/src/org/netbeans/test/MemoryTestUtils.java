/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.test;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.netbeans.insane.live.LiveReferences;
import org.netbeans.insane.live.Path;
import org.netbeans.insane.scanner.CountingVisitor;
import org.netbeans.insane.scanner.ScannerUtils;

/**
 * This code is copied from NetBeans -- in particular NbTestCase!!!
 * http://hg.netbeans.org/main-silver/annotate/63b0eb0ebe1a/nbjunit/src/org/netbeans/junit/NbTestCase.java
 */
public class MemoryTestUtils {
    /** Asserts that the object can be garbage collected. Tries to GC ref's referent.
     * @param text the text to show when test fails.
     * @param ref the referent to object that
     * should be GCed
     */
    public static void assertGC(String text, Reference<?> ref) {
        assertGC(text, ref, Collections.emptySet());
    }

    /** Asserts that the object can be garbage collected. Tries to GC ref's referent.
     * @param text the text to show when test fails.
     * @param ref the referent to object that should be GCed
     * @param rootsHint a set of objects that should be considered part of the
     * root-set for this scan. This is useful if you want to verify that one structure
     * (usually long living in real application) is not holding another structure
     * in memory, without setting a static reference to the former structure.
     * <h3>Example:</h3>
     * <pre>
     *  // test body
     *  WeakHashMap map = new WeakHashMap();
     *  Object target = new Object();
     *  map.put(target, "Val");
     *
     *  // verification step
     *  Reference ref = new WeakReference(target);
     *  target = null;
     *  assertGC("WeakMap does not hold the key", ref, Collections.singleton(map));
     * </pre>
     */
    public static void assertGC(String text, Reference<?> ref, Set<?> rootsHint) {
        List<byte[]> alloc = new ArrayList<byte[]>();
        int size = 100000;
        for (int i = 0; i < 50; i++) {
            if (ref.get() == null) {
                return;
            }
            try {
                System.gc();
            } catch (OutOfMemoryError error) {
                // OK
            }
            try {
                System.runFinalization();
            } catch (OutOfMemoryError error) {
                // OK
            }
            try {
                alloc.add(new byte[size]);
                size = (int) (((double) size) * 1.3);
            } catch (OutOfMemoryError error) {
                size = size / 2;
            }
            try {
                if (i % 3 == 0) {
                    Thread.sleep(321);
                }
            } catch (InterruptedException t) {
                // ignore
            }
        }
        alloc = null;
        String str = null;
        try {
            str = findRefsFromRoot(ref.get(), rootsHint);
        } catch (Exception e) {
            throw new AssertionFailedErrorException(e);
        } catch (OutOfMemoryError err) {
            // OK
        }
        TestCase.fail(text + ":\n" + str);
    }

    /** Assert size of some structure. Traverses the whole reference
     * graph of objects accessible from given root object and check its size
     * against the limit.
     * @param message the text to show when test fails.
     * @param limit maximal allowed heap size of the structure
     * @param root the root object from which to traverse
     */
    public static void assertSize(String message, int limit, Object root) {
        assertSize(message, Arrays.asList(new Object[] {root}), limit);
    }

    /** Assert size of some structure. Traverses the whole reference
     * graph of objects accessible from given roots and check its size
     * against the limit.
     * @param message the text to show when test fails.
     * @param roots the collection of root objects from which to traverse
     * @param limit maximal allowed heap size of the structure
     */
    public static void assertSize(String message, Collection<?> roots, int limit) {
        assertSize(message, roots, limit, new Object[0]);
    }

    /** Assert size of some structure. Traverses the whole reference
     * graph of objects accessible from given roots and check its size
     * against the limit.
     * @param message the text to show when test fails.
     * @param roots the collection of root objects from which to traverse
     * @param limit maximal allowed heap size of the structure
     * @param skip Array of objects used as a boundary during heap scanning,
     *        neither these objects nor references from these objects
     *        are counted.
     */
    public static void assertSize(String message, Collection<?> roots, int limit, Object[] skip) {
        org.netbeans.insane.scanner.Filter f = ScannerUtils.skipObjectsFilter(Arrays.asList(skip), false);
        assertSize(message, roots, limit, f);
    }

    /** Assert size of some structure. Traverses the whole reference
     * graph of objects accessible from given roots and check its size
     * against the limit.
     * @param message the text to show when test fails.
     * @param roots the collection of root objects from which to traverse
     * @param limit maximal allowed heap size of the structure
     * @param skip custom filter for counted objects
     * @return actual size or <code>-1</code> on internal error.
     */
    public static int assertSize(String message, Collection<?> roots, int limit, final MemoryFilter skip) {
        org.netbeans.insane.scanner.Filter f = new org.netbeans.insane.scanner.Filter() {
            public boolean accept(Object o, Object refFrom, Field ref) {
                return !skip.reject(o);
            }
        };
        return assertSize(message, roots, limit, f);
    }

    private static int assertSize(String message, Collection<?> roots, int limit,
            org.netbeans.insane.scanner.Filter f) {
        try {
            CountingVisitor counter = new CountingVisitor();
            ScannerUtils.scan(f, counter, roots, false);
            int sum = counter.getTotalSize();
            if (sum > limit) {
                StringBuilder sb = new StringBuilder(4096);
                sb.append(message);
                sb.append(": leak ").append(sum - limit).append(" bytes ");
                sb.append(" over limit of ");
                sb.append(limit + " bytes");
                sb.append('\n');
                for (Iterator it = counter.getClasses().iterator(); it.hasNext();) {
                    sb.append("  ");
                    Class cls = (Class) it.next();
                    if (counter.getCountForClass(cls) == 0) {
                        continue;
                    }
                    sb.append(cls.getName()).append(": ").
                            append(counter.getCountForClass(cls)).append(", ").
                            append(counter.getSizeForClass(cls)).append("B\n");
                }
                TestCase.fail(sb.toString());
            }
            return sum;
        } catch (Exception e) {
            throw new AssertionFailedErrorException("Could not traverse reference graph", e);
        }
    }

    private static String findRefsFromRoot(final Object target, final Set<?> rootsHint) throws Exception {
        int count = Integer.getInteger("assertgc.paths", 1);
        StringBuilder sb = new StringBuilder();
        final Map<Object, Boolean> skip = new IdentityHashMap<Object, Boolean>();

        org.netbeans.insane.scanner.Filter knownPath = new org.netbeans.insane.scanner.Filter() {
            public boolean accept(Object obj, Object referredFrom, Field reference) {
                return !skip.containsKey(obj);
            }
        };

        while (count-- > 0) {
            @SuppressWarnings("unchecked")
            Map m = LiveReferences.fromRoots(Collections.singleton(target), (Set<Object>) rootsHint, null, knownPath);
            Path p = (Path) m.get(target);
            if (p == null) {
                break;
            }
            if (sb.length() > 0) {
                sb.append("\n\n");
            }

            sb.append(p.toString());
            for (; p != null; p = p.nextNode()) {
                Object o = p.getObject();
                if (o != target) {
                    skip.put(o, Boolean.TRUE);
                }
            }
        }
        return sb.length() > 0 ? sb.toString() : "Not found!!!";
    }

    /** Error containing nested Exception.
     * It describes the failure and holds and print also the original Exception.
     * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
     * @version 1.0
     */
    public static class AssertionFailedErrorException extends AssertionFailedError {
        /** contains Exception that caused AssertionFailedError
         */
        protected Throwable nestedException;

        /** Creates new AssertionFailedErrorException
         * @param nestedException contains Exception that caused AssertionFailedError
         */
        public AssertionFailedErrorException(Throwable nestedException) {
            this(null, nestedException);
        }

        /** Creates new AssertionFailedErrorException
         *  @param message The error description message.
         *  @param nestedException contains Exception that caused AssertionFailedError
         */
        public AssertionFailedErrorException(String message, Throwable nestedException) {
            super(message);
            this.nestedException = nestedException;
        }

        /** prints stack trace of assertion error and nested exception into System.err
         */
        public void printStackTrace() {
            printStackTrace(System.err);
        }

        /** prints stack trace of assertion error and nested exception
         * @param err PrintWriter where to print stack trace
         */
        public void printStackTrace(PrintWriter err) {
            synchronized (err) {
                super.printStackTrace(err);
                err.println("\nNested Exception is:");
                nestedException.printStackTrace(err);
            }
        }

        /** prints stack trace of assertion error and nested exception
         * @param err PrintStream where to print stack trace
         */
        public void printStackTrace(PrintStream err) {
            synchronized (err) {
                super.printStackTrace(err);
                err.println("\nNested Exception is:");
                nestedException.printStackTrace(err);
            }
        }
    }

    /**
     * Instance filter contract.
     *
     * @author Petr Kuzel
     */
    public interface MemoryFilter {
        /**
         * Decides non-destructively whether given instance pass
         * custom criteria. Implementation must not alter
         * JVM heap and it must return the same result if
         * it gets some instance multiple times. And
         * it must be very fast.
         *
         * @return <code>true</code> if passed instance is not accepted.
         *
         * <p>E.g.:
         * <code>return obj instanceof java.lang.ref.Reference</code>
         */
        boolean reject(Object obj);
    }

    private MemoryTestUtils() {
    }
}
