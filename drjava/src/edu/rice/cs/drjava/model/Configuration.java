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

import java.io.*;
import java.util.*;
import edu.rice.cs.drjava.config.*;
import gj.util.Vector;
/**
 * A very very temporary class to store config data.
 *
 * This will DISAPPEAR in the NEAR NEAR NEAR FUTURE.
 *
 * @version $Id$
 */
public final class Configuration implements OptionConstants {
    public static final File PROPERTIES_FILE
	= new File(System.getProperty("user.home"), ".drjava");
    
    public static final Configuration ONLY = new Configuration();
    
    private final FileConfiguration _config;
    
    private Configuration() {
        try {
            PROPERTIES_FILE.createNewFile(); // be nice and ensure a config file
	} catch(IOException e) { // IOException occurred
	}
        _config = new FileConfiguration(PROPERTIES_FILE);
        try {
            _config.loadConfiguration();
        } catch(IOException e) {
        }
    }
    
    /**
     * Saves the properties back to a file.
     * This will do nothing if it can't save to the properties file
     * for some reason.
     * This is a temporary hack to enable feature req #523222.
     * @deprecated this is a dumb method.  this is a dumb class.
     */
    public void saveProperties() {
        try {
            _config.saveConfiguration();
        } catch(IOException e) {
            // for now, do nothing
        }
    }

    
    /**
     * Changes the setting for the javac classpath.
     * This is a temporary hack to enable feature req #523222.
     */
    public void setJavacLocation(String s) {
	_config.setSetting(JAVAC_LOCATION,s);
    }
    
    /**
     * Returns the setting for the javac classpath, or null if none was
     * specified.
     */
    public String getJavacLocation() {
	return _config.getSetting(JAVAC_LOCATION);
    }
    
    /**
     * Returns the setting for the jsr14 classpath, or null if none was
     * specified.
     */
    public String getJSR14Location() {
	return _config.getSetting(JSR14_LOCATION);
    }
    
    /**
     * Returns the setting for the jsr14 collections classses classpath,
     * or null if none was specified.
     */
    public String getJSR14CollectionsPath() {
	return _config.getSetting(JSR14_COLLECTIONSPATH);
    }
    
    /**
     * Gets additional items to add to the classpath for both
     * compilation and interpretation.
     *
     * The classpath property must use the platform's path separator.
     * @deprecated this method is dumb.
     * @return An array of items to add to the classpaths.
     */
    public String[] getExtraClasspath() {
	Vector<String> v = _config.getSetting(EXTRA_CLASSPATH);
	if(v==null) return new String[0];
	String[] ret = new String[v.size()];
	v.copyInto(ret);
	return ret;
    }
    
}
