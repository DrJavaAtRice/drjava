/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.util;

import java.io.*;
import java.util.*;

/**
 * The instances of this class manages the importation clauses.
 *
 * The <code>declarePackageImport</code> method imports a new package.
 * This one has the highest priority over the imported packages when a
 * lookup is made to find a class.
 *
 * The <code>declareClassImport</code> method imports a new class. This
 * one has the highest priority over the same suffix imported class.
 *
 * @author  Stephane Hillion
 * @version 1.1 - 1999/11/28
 */

public class ImportationManager implements Cloneable {
    /**
     * This list contains the import-on-demand clauses.
     */
    protected List importOnDemandClauses = new LinkedList();

    /**
     * This set contains the single-type-import clauses.
     */
    protected List singleTypeImportClauses = new LinkedList();

    /**
     * This string contains the name of the current package
     */
    protected String currentPackage;

    /**
     * The class loader that must be used
     */
    protected ClassLoader classLoader;

    /**
     * Creates a new importation manager.
     * The manager is initialized with two predefined import-on-demand
     * clauses: "java.lang" (the java language package) and 
     * "" (the anonymous package)
     * @param cl the class loader to use
     */
    public ImportationManager(ClassLoader cl) {
	classLoader               = cl;
	declarePackageImport("java.lang");
	setCurrentPackage("");
    }

    /**
     * Returns a copy of this object
     */
    public Object clone() {
        return new ImportationManager(this);
    }

    /**
     * Sets the class loader
     */
    public void setClassLoader(ClassLoader cl) {
	classLoader = cl;
    }

    /**
     * Sets the current package. This has no influence on the
     * behaviour of the <code>lookupClass</code> method.
     * @param pkg the package name
     */
    public void setCurrentPackage(String pkg) {
	currentPackage = pkg;
    }

    /**
     * Returns the current package
     */
    public String getCurrentPackage() {
	return currentPackage;
    }

    /**
     * Returns the import-on-demand clauses
     */
    public List getImportOnDemandClauses() {
	return importOnDemandClauses;
    }

    /**
     * Returns the single-type-import clauses
     */
    public List getSingleTypeImportClauses() {
	return singleTypeImportClauses;
    }

    /**
     * Declares a new import-on-demand clause
     * @param pkg the package name
     */
    public void declarePackageImport(String pkg) {
	if (importOnDemandClauses.size() == 0 ||
	    !importOnDemandClauses.get(0).equals(pkg)) {
	    importOnDemandClauses.remove(pkg);
	    importOnDemandClauses.add(0, pkg);
	}
    }

    /**
     * Declares a new single-type-import clause
     * @param cname the fully qualified class name
     * @exception ClassNotFoundException if the class cannot be found
     */
    public void declareClassImport(String cname) throws ClassNotFoundException {
	try {
	    // A previous importation of this class is removed to avoid a new
	    // existance verification and to speed up further loadings.
	    if (!singleTypeImportClauses.remove(cname)) {
		Class.forName(cname, true, classLoader);
	    }
	} catch (ClassNotFoundException e) {
	    // try to find an inner class with this name
	    Class c = findInnerClass(cname);
	    singleTypeImportClauses.remove((c == null) ? c.getName() : cname);
	    singleTypeImportClauses.add(0, (c == null) ? c.getName() : cname);
	} finally {
	    singleTypeImportClauses.add(0, cname);
	}
    }

    /**
     * Loads the class that match to the given name in the source file
     * @param  cname  the name of the class to find
     * @param  ccname the name of the current class or null
     * @return the class found
     * @exception ClassNotFoundException if the class cannot be loaded
     */
    public Class lookupClass(String cname, String ccname)
	throws ClassNotFoundException {
	String str = cname.replace('.', '$');

	// Search for the full name ...
	
	// ... in the current package ...
	String t = (currentPackage.equals(""))
	    ? cname
	    : currentPackage + "." + cname;	
	try {
	    return Class.forName(t, false, classLoader);
	} catch (ClassNotFoundException e) {
	}

	if (cname.indexOf('.') != -1) {
	    try {
		return Class.forName(cname, false, classLoader);
	    } catch (ClassNotFoundException e) {
	    }
	}

	// try to find an inner class with this name
	try {
	    return findInnerClass(t);
	} catch (ClassNotFoundException e) {
	}

	// ... in the single-type-import clauses ...
	Iterator it = singleTypeImportClauses.iterator();
	while (it.hasNext()) {
	    String s = (String)it.next();
	    if (hasSuffix(s, cname) || hasSuffix(s, str)) {
		return Class.forName(s, false, classLoader);
	    }
	    // It is perhaps an innerclass of an imported class
	    // ie. a.b.C and C$D
	    int i = str.indexOf('$');
	    if (i != -1) {
		try {
		    if (hasSuffix(s, str.substring(0, i))) {
			return Class.forName(s + str.substring(i, str.length()),
					     false, classLoader);
		    }
		} catch (ClassNotFoundException e) {
		}
	    }
	}

	if (ccname != null) {
	    // ... in the current class ...
	    try {
		return Class.forName(ccname + "$" + str, false, classLoader);
	    } catch (ClassNotFoundException e) {
	    }

	    // ... it is perhaps an outer class
	    it = getOuterNames(ccname).iterator();
	    String tmp = ccname;
	    while (it.hasNext()) {
		String s = (String)it.next();
		int i = tmp.lastIndexOf(s) + s.length();
		tmp = tmp.substring(0, i);
		if (s.equals(cname)) {
		    return Class.forName(tmp, false, classLoader);
		}
	    }

	    // ... or the class itself
	    if (ccname.endsWith(cname)) {
		int i = ccname.lastIndexOf(cname);
		if (i > 0 && ccname.charAt(i - 1) == '$') {
		    return Class.forName(ccname, false, classLoader);
		}
	    }
	}
	
	// ... with the import-on-demand clauses as prefix
	it = importOnDemandClauses.iterator();
	while (it.hasNext()) {
	    String s = (String)it.next();
	    try {
	        return Class.forName(s+"."+str, false, classLoader);
	    } catch (ClassNotFoundException e) {
	    }
	}

	throw new ClassNotFoundException(cname);
    }

    /**
     * Returns a list of the outer classes names
     */
    protected List getOuterNames(String cname) {
	List l = new LinkedList();
	int i;
	while ((i = cname.lastIndexOf('$')) != -1) {
	    cname = cname.substring(0, i);
	    if ((i = cname.lastIndexOf('$')) != -1) {
		l.add(cname.substring(i + 1, cname.length()));
	    } else if ((i = cname.lastIndexOf('.')) != -1) {
		l.add(cname.substring(i + 1, cname.length()));
	    } else {
		l.add(cname);
	    }
	}
	return l;
    }

    /**
     * Searches for an inner class from its name in the dotted notation
     */
    protected Class findInnerClass(String s) throws ClassNotFoundException {
	int   n;
	while ((n = s.lastIndexOf('.')) != -1) {
	    s = s.substring(0, n) + '$' + s.substring(n + 1, s.length());
	    try {
		return Class.forName(s, false, classLoader);
	    } catch (ClassNotFoundException e) {
	    }
	}
	throw new ClassNotFoundException(s);
    }

    /**
     * Copy constructor
     */
    protected ImportationManager(ImportationManager im) {
	importOnDemandClauses     =
	    (List)((LinkedList)im.importOnDemandClauses).clone();
	singleTypeImportClauses   =
	    (List)((LinkedList)im.singleTypeImportClauses).clone();
	currentPackage            = im.currentPackage;
	classLoader               = im.classLoader;
    }

    /**
     * Tests whether the fully qualified class name c1 ends with c2
     */
    protected boolean hasSuffix(String c1, String c2) {
	int i = c1.lastIndexOf('.');
	String s = c1;
	if (i != -1) {
	    s = c1.substring(i + 1, c1.length());
	}
	return s.equals(c2);
    }
}
