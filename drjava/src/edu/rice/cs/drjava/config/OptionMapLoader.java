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

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import gj.util.Vector;
import gj.util.Enumeration;
public class OptionMapLoader implements OptionConstants {

    /** bag of default options (programmatically defined, instead of in an options file) */
    private static DefaultOptionMap DEFAULTS = new DefaultOptionMap();
    private static Properties DEFAULT_STRINGS = new Properties();

    static {
	// initialize DEFAULTS object.
	initOption(INDENT_LEVEL,new Integer(2));
	initOption(JAVAC_LOCATION,"");
	initOption(JSR14_LOCATION,"");
	initOption(JSR14_COLLECTIONSPATH,"");
	initOption(EXTRA_CLASSPATH,new Vector<String>());
    }

    /** might as well have been a macro if Java had them */
    private static <T> void initOption(Option<T> option, T value) {
	String sval = option.format(value);
	DEFAULT_STRINGS.setProperty(option.name,sval);
	DEFAULTS.setOption(option,value);
    }

    public static OptionMapLoader DEFAULT = new OptionMapLoader(DEFAULT_STRINGS);
    
    /**
     * creates an OptionMapLoader from a given input stream.
     * does not maintain a reference to this input stream after 
     * Constructor creates
     * @param is the input stream to read. 
     */
    public OptionMapLoader(InputStream is) throws IOException {
	this(new Properties(DEFAULT_STRINGS));
	prop.load(is);
    }
    
    private final Properties prop;
    
    private OptionMapLoader(Properties prop) {
	this.prop = prop;
    }
    
    /**
     * creates an OptionMap from an InputStream.
     * @param is the inputstream to read from to load these options.
     */
    public void loadInto(OptionMap map) {
	Enumeration<OptionParser> options = DEFAULTS.keys();
	while(options.hasMoreElements()) {
	    OptionParser option = options.nextElement();
	    String val = prop.getProperty(option.name);
	    map.setString(option,val);
	}
    }
}
