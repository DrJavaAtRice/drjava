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

package edu.rice.cs.drjava.config;
import junit.framework.TestCase;
import java.io.*;
public class OptionMapLoaderTest extends TestCase implements OptionConstants {

    public OptionMapLoaderTest(String s) {
	super(s);
    }

    public static class StringInputStream extends ByteArrayInputStream {
	public StringInputStream(String s) {
	    super(s.getBytes());
	}
    }
    
    /** an artificially created properties "file" **/
    public static final String OPTION_DOC = 
	"# this is a fake header\n"+
	"this.is.a.real.key = value\n"+
	"indent.level = -1\n"+
	"javac.location = foo\n"+
	"jsr14.location = bar\n"+
	"jsr14.collectionspath = baz\n"+
	"extra.classpath = bam\n\n";
    
    public void testProperConfigSet() throws IOException {
	checkSet(OPTION_DOC,new Integer(-1),"foo","bar","baz",1);
    }

    private void checkSet(String set, Integer indent, String javac, String jsr, String col, int size) throws IOException {
        StringInputStream is = new StringInputStream(set);
	OptionMapLoader loader = new OptionMapLoader(is);
	DefaultOptionMap map = new DefaultOptionMap();
	loader.loadInto(map);
	assertEquals("indent (integer) option",
		     map.getOption(INDENT_LEVEL),indent);
	assertEquals(map.getOption(JAVAC_LOCATION),javac);
	assertEquals(map.getOption(JSR14_LOCATION),jsr);
	assertEquals(map.getOption(JSR14_COLLECTIONSPATH),col);
	assertEquals("size of extra-classpath vector",
                     new Integer(size),new Integer(map.getOption(EXTRA_CLASSPATH).size()));
    }

    public void testEmptyConfigSet() throws IOException {
        checkSet("",INDENT_LEVEL.getDefault(),JAVAC_LOCATION.getDefault(),
                 JSR14_LOCATION.getDefault(),JSR14_COLLECTIONSPATH.getDefault(),
                 EXTRA_CLASSPATH.getDefault().size());
        
    }
}
