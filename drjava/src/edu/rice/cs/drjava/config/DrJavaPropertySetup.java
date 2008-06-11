/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.config;

import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.XMLConfig;
import edu.rice.cs.drjava.DrJava;
import java.io.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;

import static edu.rice.cs.util.XMLConfig.XMLConfigException;

/** Class setting up the variables for external processes.
  *
  *  @version $Id$
  */
public class DrJavaPropertySetup implements OptionConstants {
  public static void setup() {
    final String DEF_DIR = "${drjava.working.dir}";
    
    // fake "Config" properties
    PropertyMaps.ONLY.setProperty("Config", new EagerProperty("config.master.jvm.args.combined",
                                                              "This property contains all the JVM arguments passed "+
                                                              "to DrJava's master JVM, i.e. the JVM the user is editing "+
                                                              "programs in. The arguments from the \"JVM Args for "+
                                                              "Main JVM\" and the special -X arguments from \"Maximum "+
                                                              "Heap Size for Main JVM\" are combined.") {
      public void update() {
        StringBuilder sb = new StringBuilder(DrJava.getConfig().getSetting(MASTER_JVM_XMX));
        if (sb.length()>0) { sb.append(" "); }
        sb.append(DrJava.getConfig().getSetting(MASTER_JVM_ARGS));
        _value = "-Xmx"+sb.toString().trim();
      }
      public void resetAttributes() {
        _attributes.clear();
      }
    }).listenToInvalidatesOf(PropertyMaps.ONLY.getProperty("Config", "config.master.jvm.args"))
      .listenToInvalidatesOf(PropertyMaps.ONLY.getProperty("Config", "config.master.jvm.xmx"));
    
    PropertyMaps.ONLY.setProperty("Config", new EagerProperty("config.slave.jvm.args.combined",
                                                              "This property contains all the JVM arguments passed "+
                                                              "to DrJava's master JVM, i.e. the JVM the user is editing "+
                                                              "programs in. The arguments from the \"JVM Args for "+
                                                              "Slave JVM\" and the special -X arguments from \"Maximum "+
                                                              "Heap Size for Main JVM\" are combined.") {
      public void update() {
        StringBuilder sb = new StringBuilder(DrJava.getConfig().getSetting(SLAVE_JVM_XMX));
        if (sb.length()>0) { sb.append(" "); }
        sb.append(DrJava.getConfig().getSetting(SLAVE_JVM_ARGS));
        _value = "-Xmx"+sb.toString().trim();
      }
      public void resetAttributes() {
        _attributes.clear();
      }
    }).listenToInvalidatesOf(PropertyMaps.ONLY.getProperty("Config", "config.slave.jvm.args"))
      .listenToInvalidatesOf(PropertyMaps.ONLY.getProperty("Config", "config.slave.jvm.xmx"));
    
    // Misc
    PropertyMaps.ONLY.setProperty("Misc", new DrJavaProperty("tmpfile",
                                                             "Creates a temporary file and returns the name of it.\n"+
                                                             "Optional attributes:\n"+
                                                             "\tname=\"<name for temp file>\"\n"+
                                                             "\tdir=\"<dir for temp file>\"\n"+
                                                             "\tkeep=\"<true if the file should not be erased>\"\n"+
                                                             "\tcontent=\"<text to go into the file>\"") {
      java.util.Random _r = new java.util.Random();
      public String toString() {
        invalidate();
        update();
        return _value;
      }
      public void update() {
        try {
          String name = _attributes.get("name");
          String dir = _attributes.get("dir");
          if (name.equals("")) name = "DrJava-Execute-" + System.currentTimeMillis() + "-" + (_r.nextInt() & 0xffff) + ".tmp";
          if (dir.equals("")) dir = System.getProperty("java.io.tmpdir");
          else {
            dir = StringOps.unescapeSpacesWith1bHex(dir);
          }
   
          File f = new File(dir, name);
          try { f = f.getCanonicalFile(); }
          catch(IOException ioe) { }
          f.deleteOnExit();
          _value = StringOps.escapeSpacesWith1bHex(f.toString());
        }
        catch(SecurityException e) { _value = "Error."; }
      }
      
      public String getCurrent() {
        invalidate();
        final String s = super.getCurrent();
        File f = new File(StringOps.unescapeSpacesWith1bHex(_value));
        final String keep = _attributes.get("keep");
        if (!(keep.equals("true") || keep.equals("yes") || keep.equals("1"))) {
          f.deleteOnExit();
        }
        String text = StringOps.unescapeSpacesWith1bHex(_attributes.get("content"));
        try {
          FileWriter fw = new FileWriter(f);
          fw.write(text, 0, text.length());
          fw.close();
        }
        catch(IOException ioe) { /*ignore*/ }
        return s;
      }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("dir", "");
        _attributes.put("name", "");
        _attributes.put("keep", "");
        _attributes.put("content", "");
      }
    });
    
    PropertyMaps.ONLY.setProperty("File", new RecursiveFileListProperty("file.find", File.pathSeparator, DEF_DIR, DEF_DIR,
                                                                        "Return a list of files found in the starting dir.\n"+
                                                                        "Optional attributes:\n"+
                                                                        "\tsep=\"<separator between files>\"\n"+
                                                                        "\tdir=\"<dir where to start>\"\n"+
                                                                        "\trel=\"<dir to which the files are relative>\"\n"+
                                                                        "\tfilter=\"<filter, like *.txt, for files to list>\"\n"+
                                                                        "\tdirfilter=\"<filter for which dirs to recurse>\""));

    PropertyMaps.ONLY.setProperty("File", new DrJavaProperty("file.isdir",
                                                             "Return true if the specified file is a directory, false "+
                                                             "otherwise.\n"+
                                                             "Required attributes:\n"+
                                                             "\tfile=\"<file to test>\"\n"+
                                                             "Multiple files can be specified, separated by ${path.separator}, "+
                                                             "which is "+File.pathSeparator+" on this machine. If multiple "+
                                                             "files are specified, then a list of values, each "+
                                                             "separated by ${path.separator}, is returned.") {
      public void update() {
        String s = _attributes.get("file");
        if (s==null) {
          _value = "(file.isdir Error...)";
          return;
        }
        StringBuilder sb = new StringBuilder();
        for(String fs: s.split(edu.rice.cs.plt.text.TextUtil.regexEscape(File.pathSeparator))) {
          File f = new File(StringOps.unescapeSpacesWith1bHex(fs));
          sb.append(File.pathSeparator);
          sb.append((f.exists() && f.isDirectory())?"true":"false");
        }
        s = sb.toString();
        if (s.startsWith(File.pathSeparator)) {
          _value = s.substring(File.pathSeparator.length());
        }
        else {
          _value = s;
        }
      }
      public boolean isCurrent() { return false; }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("file", null);
      }
    });
    
    PropertyMaps.ONLY.setProperty("File", new DrJavaProperty("file.isfile",
                                                             "Return true if the specified file is a file, not a "+
                                                             "directory.\n"+
                                                             "Required attributes:\n"+
                                                             "\tfile=\"<file to test>\"\n"+
                                                             "Multiple files can be specified, separated by ${path.separator}, "+
                                                             "which is "+File.pathSeparator+" on this machine. If multiple "+
                                                             "files are specified, then a list of values, each "+
                                                             "separated by ${path.separator}, is returned.") {
      public void update() {
        String s = _attributes.get("file");
        if (s==null) {
          _value = "(file.isfile Error...)";
          return;
        }
        StringBuilder sb = new StringBuilder();
        for(String fs: s.split(edu.rice.cs.plt.text.TextUtil.regexEscape(File.pathSeparator))) {
          fs = StringOps.unescapeSpacesWith1bHex(fs);
          File f = new File(fs);
          sb.append(File.pathSeparator);
          sb.append((f.exists() && f.isFile())?"true":"false");
        }
        s = sb.toString();
        if (s.startsWith(File.pathSeparator)) {
          _value = s.substring(File.pathSeparator.length());
        }
        else {
          _value = s;
        }
      }
      public boolean isCurrent() { return false; }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("file", null);
      }
    });

    PropertyMaps.ONLY.setProperty("File", new DrJavaProperty("file.exists",
                                                             "Return true if the specified file exists.\n"+
                                                             "Required attributes:\n"+
                                                             "\tfile=\"<file to test>\"\n"+
                                                             "Multiple files can be specified, separated by ${path.separator}, "+
                                                             "which is "+File.pathSeparator+" on this machine. If multiple "+
                                                             "files are specified, then a list of values, each "+
                                                             "separated by ${path.separator}, is returned.") {
      public void update() {
        String s = _attributes.get("file");
        if (s==null) {
          _value = "(file.exists Error... )";
          return;
        }
        StringBuilder sb = new StringBuilder();
        for(String fs: s.split(edu.rice.cs.plt.text.TextUtil.regexEscape(File.pathSeparator))) {
          fs = StringOps.unescapeSpacesWith1bHex(fs);
          File f = new File(fs);
          sb.append(File.pathSeparator);
          sb.append(f.exists()?"true":"false");
        }
        s = sb.toString();
        if (s.startsWith(File.pathSeparator)) {
          _value = s.substring(File.pathSeparator.length());
        }
        else {
          _value = s;
        }
      }
      public boolean isCurrent() { return false; }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("file", null);
      }
    });

    PropertyMaps.ONLY.setProperty("File", new DrJavaProperty("file.parent",
                                                             "Return the path of the parent, or and empty string \"\" if "+
                                                             "no parent exists.\n"+
                                                             "Required attributes:\n"+
                                                             "\tfile=\"<file for which to return the parent>\"\n"+
                                                             "Multiple files can be specified, separated by ${path.separator}, "+
                                                             "which is "+File.pathSeparator+" on this machine. If multiple "+
                                                             "files are specified, then a list of values, each "+
                                                             "separated by ${path.separator}, is returned.") {
      public void update() {
        String s = _attributes.get("file");
        if (s==null) {
          _value = "(file.parent Error...)";
          return;
        }
        StringBuilder sb = new StringBuilder();
        for(String fs: s.split(edu.rice.cs.plt.text.TextUtil.regexEscape(File.pathSeparator))) {
          fs = StringOps.unescapeSpacesWith1bHex(fs);
          File f = new File(fs);
          String p = StringOps.escapeSpacesWith1bHex(f.getParent());
          sb.append(File.pathSeparator);
          sb.append((p!=null)?p:"");
        }
        s = sb.toString();
        if (s.startsWith(File.pathSeparator)) {
          _value = s.substring(File.pathSeparator.length());
        }
        else {
          _value = s;
        }
      }
      public boolean isCurrent() { return false; }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("file", null);
      }
    });

    PropertyMaps.ONLY.setProperty("File", new DrJavaProperty("file.abs",
                                                             "Return the absolute path of the file which has been "+
                                                             "relative to another file.\n"+
                                                             "Required attributes:\n"+
                                                             "\tfile=\"<file for which to return the absolute path>\"\n"+
                                                             "\tbase=\"<other file which serves as base path>\"\n"+
                                                             "Multiple files can be specified for the file attribute, each "+
                                                             "separated by ${path.separator}, which is "+File.pathSeparator+
                                                             " on this machine. If multiple files are specified, then a list "+
                                                             "of values, each separated by ${path.separator}, is returned.") {
      public void update() {
        String s = _attributes.get("base");
        if (s==null) {
          _value = "(file.rel Error: base missing...)";
          return;
        }
        s = StringOps.unescapeSpacesWith1bHex(s);
        File b = new File(s);
        s = _attributes.get("file");
        if (s==null) {
          _value = "(file.rel Error: file missing...)";
          return;
        }
        StringBuilder sb = new StringBuilder();
        for(String fs: s.split(edu.rice.cs.plt.text.TextUtil.regexEscape(File.pathSeparator))) {
          fs = StringOps.unescapeSpacesWith1bHex(fs);
          File f = new File(fs);
          sb.append(File.pathSeparator);
          sb.append(StringOps.escapeSpacesWith1bHex(f.getAbsolutePath()));
        }
        s = sb.toString();
        if (s.startsWith(File.pathSeparator)) {
          _value = s.substring(File.pathSeparator.length());
        }
        else {
          _value = s;
        }
      }
      public boolean isCurrent() { return false; }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("file", null);
        _attributes.put("base", null);
      }
    });

    PropertyMaps.ONLY.setProperty("File", new DrJavaProperty("file.rel",
                                                             "Return the path of the file, relative to another file.\n"+
                                                             "Required attributes:\n"+
                                                             "\tfile=\"<file for which to return the relative path>\"\n"+
                                                             "\tbase=\"<other file which serves as base path>\"\n"+
                                                             "Multiple files can be specified for the file attribute, each "+
                                                             "separated by ${path.separator}, which is "+File.pathSeparator+
                                                             " on this machine. If multiple files are specified, then a list "+
                                                             "of values, each separated by ${path.separator}, is returned.") {
      public void update() {
        String s = _attributes.get("base");
        if (s==null) {
          _value = "(file.rel Error: base missing...)";
          return;
        }
        s = StringOps.unescapeSpacesWith1bHex(s);
        File b = new File(s);
        s = _attributes.get("file");
        if (s==null) {
          _value = "(file.rel Error: file missing...)";
          return;
        }
        StringBuilder sb = new StringBuilder();
        for(String fs: s.split(edu.rice.cs.plt.text.TextUtil.regexEscape(File.pathSeparator))) {
          fs = StringOps.unescapeSpacesWith1bHex(fs);
          File f = new File(fs);
          try {
            s = edu.rice.cs.util.FileOps.stringMakeRelativeTo(f,b);
            if (s.endsWith(File.separator)) {
              // path ends with "/", check if we can remove the "/"
              File[] roots = File.listRoots();
              if (roots!=null) {
                boolean equalsRoot = false;
                for(File r: roots) {
                  if (s.equals(r.toString())) {
                    equalsRoot = true;
                    break;
                  }
                }
                if (!equalsRoot) {
                  // path ends with "/" and does not equal a file system root
                  // we can remove the trailing "/"
                  s = s.substring(0, s.length()-1);
                }
              }
            }
            sb.append(File.pathSeparator);
            sb.append(StringOps.escapeSpacesWith1bHex(s));
          }
          catch(Exception e) {
            _value = "(file.rel I/O Error...)";
            return;
          }
        }
        s = sb.toString();
        if (s.startsWith(File.pathSeparator)) {
          _value = s.substring(File.pathSeparator.length());
        }
        else {
          _value = s;
        }
      }
      public boolean isCurrent() { return false; }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("file", null);
        _attributes.put("base", null);
      }
    });

    PropertyMaps.ONLY.setProperty("File", new DrJavaProperty("file.mkdir",
                                                             "Make the directory with the provided file name. "+
                                                             "Evaluates to the empty string \"\" if successful.\n"+
                                                             "Required attributes:\n"+
                                                             "\tfile=\"<directory to create>\"\n"+
                                                             "Multiple files can be specified for the file attribute, each "+
                                                             "separated by ${path.separator}, which is "+File.pathSeparator+
                                                             " on this machine. If multiple files are specified, then DrJava "+
                                                             "will attempt to make all those directories.") {
      public void update() {
        String s = _attributes.get("file");
        if (s==null) {
          _value = "(file.mkdir Error: file missing...)";
          return;
        }
        for(String fs: s.split(edu.rice.cs.plt.text.TextUtil.regexEscape(File.pathSeparator))) {
          fs = StringOps.unescapeSpacesWith1bHex(fs);
          File n = new File(fs);
          if (!n.mkdir()) {
            _value = "(file.mkdir I/O Error...)";
            return;
          }
        }
        _value = "";
      }
      public boolean isCurrent() { return false; }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("file", null);
      }
    });

    PropertyMaps.ONLY.setProperty("File", new DrJavaProperty("file.rm",
                                                             "Remove the specified file or directory, recursively if desired. "+
                                                             "Evaluates to the empty string \"\" if successful.\n"+
                                                             "Required attributes:\n"+
                                                             "\tfile=\"<file or directory to remove>\""+
                                                             "Optional attributes:\n"+
                                                             "\trec=\"<true for recursive removal>\"\n"+
                                                             "(if not specified, false is used and removal is not recursive)\n"+
                                                             "Multiple files can be specified for the file attribute, each "+
                                                             "separated by ${path.separator}, which is "+File.pathSeparator+
                                                             " on this machine. If multiple files are specified, then DrJava "+
                                                             "will attempt to remove all those files.") {
      public void update() {
        String s = _attributes.get("rec");
        boolean rec = new Boolean(s).booleanValue();
        s = _attributes.get("file");
        if (s==null) {
          _value = "(file.rm Error...)";
          return;
        }
        s = StringOps.unescapeSpacesWith1bHex(s);
        boolean res = false;
        for(String fs: s.split(edu.rice.cs.plt.text.TextUtil.regexEscape(File.pathSeparator))) {
          fs = StringOps.unescapeSpacesWith1bHex(fs);
          File f = new File(fs);
          if (rec) {
            // recursive removal
            try {
              res = edu.rice.cs.plt.io.IOUtil.deleteRecursively(f);
            }
            catch(Exception e) { res = false; }
          }
          else {
            // non-recursive removal
            try {
              res = f.delete();
            }
            catch(SecurityException e) { res = false; }
          }
        }
        _value = res?"":"(file.rm I/O Error...)";
      }
      public boolean isCurrent() { return false; }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("file", null);
        _attributes.put("rec", "false");
      }
    });
    
    PropertyMaps.ONLY.setProperty("File", new DrJavaProperty("file.mv",
                                                             "Move the specified file or directory to a new location. "+
                                                             "Evaluates to the empty string \"\" if successful.\n"+
                                                             "Required attributes:\n"+
                                                             "\tfile=\"<file or directory to move>\""+
                                                             "\tnew=\"<new location>\"") {
      public void update() {
        String s = _attributes.get("file");
        if (s==null) {
          _value = "(file.mv Error: file missing...)";
          return;
        }
        File f = new File(s);
        s = _attributes.get("new");
        if (s==null) {
          _value = "(file.mv Error: new missing...)";
          return;
        }
        File n = new File(s);
        boolean res = false;
        if ((f.getParent()!=null) && (f.getParent().equals(n.getParent()))) {
          // just renaming a file or directory
          res = edu.rice.cs.plt.io.IOUtil.attemptRenameTo(f,n);
        }
        else {
          if (f.isFile()) {
            // just moving a file
            try {
              res = edu.rice.cs.plt.io.IOUtil.attemptMove(f,n);
            }
            catch(Exception e) { res = false; }
          }
          else {
            // moving a whole directory
            try {
              res = edu.rice.cs.util.FileOps.moveRecursively(f,n);
            }
            catch(Exception e) { res = false; }
          }
        }
        _value = res?"":"(file.mv I/O Error...)";
      }
      public boolean isCurrent() { return false; }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("file", null);
        _attributes.put("new", null);
      }
    });
    
    PropertyMaps.ONLY.setProperty("DrJava", new EagerProperty("drjava.current.time.millis",
                                                              "Returns the current time in milliseconds since 01/01/1970, "+
                                                              "unless other format is specified by the fmt attribute.\n"+
                                                              "Optional attributes:\n"+
                                                              "\tfmt=\"full\" or \"long\" or \"medium\" or \"short\"") {
      public void update() {
        long millis = System.currentTimeMillis();
        String f = _attributes.get("fmt").toLowerCase();
        if (f.equals("full")) {
          _value = java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL).format(new java.util.Date(millis));
        }
        else if (f.equals("long")) {
          _value = java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG).format(new java.util.Date(millis));
        }
        else if (f.equals("medium")) {
          _value = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM).format(new java.util.Date(millis));
        }
        else if (f.equals("short")) {
          _value = java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT).format(new java.util.Date(millis));
        }
        else {
          _value = String.valueOf(millis);
        }
      }
      
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("fmt", "millis");
      }
    });

    PropertyMaps.ONLY.setProperty("Misc", new EagerProperty("ignore",
                                                            "Evaluates the string specified in the command attribute, "+
                                                            "but ignore the result of the evaluation. Only side effects "+
                                                            "of the evaluation are apparent. This property always "+
                                                            "evaluates to the empty string \"\" (unless the command "+
                                                            "attribute is missing).\n"+
                                                            "Required attributes:\n"+
                                                            "\tcmd=\"<command to evaluate>\"") {
      public void update() {
        String s = _attributes.get("cmd");
        if (s==null) {
          _value = "(ignore Error...)";
          return;
        }
        _value = "";
      }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("cmd", null);
      }
    });

    PropertyMaps.ONLY.setProperty("Misc", new DrJavaProperty("if",
                                                             "If the cond attribute evaluates to true, returns "+
                                                             "the evaluation of the then attribute, otherwise "+
                                                             "the evaluation of the else attribute.\n"+
                                                             "Required attribute:\n"+
                                                             "\tcond=\"<string evaluating to true of false>\"\n"+
                                                             "Optional attributes:\n"+
                                                             "\tthen=\"<evaluated if true>\"\n"+
                                                             "\telse=\"<evaluated if false>\"") {
      public String toString() {
        invalidate();
        update();
        return _value;
      }
      public void update() {
        if (_attributes.get("cond")==null) {
          _value = "(if Error...)";
          return;
        }
        if (_attributes.get("cond").toLowerCase().equals("true")) {
          _value = _attributes.get("then");
        }
        else if (_attributes.get("cond").toLowerCase().equals("false")) {
          _value = _attributes.get("else");
        }
        else {
          _value = "(if Error...)";
          return;
        }
      }
      public String getCurrent() {
        invalidate();
        return super.getCurrent();
      }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("cond", null);
        _attributes.put("then", "");
        _attributes.put("else", "");
      }
    });
    
    PropertyMaps.ONLY.setProperty("Misc", new DrJavaProperty("not",
                                                             "If the value is true, returns false; if the "+
                                                             "value is false, returns true.\n"+
                                                             "Required attribute:\n"+
                                                             "\tvalue=\"<string evaluating to true of false>\"") {
      public String toString() {
        invalidate();
        update();
        return _value;
      }
      public void update() {
        if (_attributes.get("value")==null) {
          _value = "(not Error...)";
          return;
        }
        if (_attributes.get("value").toLowerCase().equals("true")) {
          _value = "false";
        }
        else if (_attributes.get("value").toLowerCase().equals("false")) {
          _value = "true";
        }
        else {
          _value = "(not Error...)";
          return;
        }
      }
      public String getCurrent() {
        invalidate();
        return super.getCurrent();
      }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("value", null);
      }
    });
    
    PropertyMaps.ONLY.setProperty("Misc", new BinaryOpProperty
                                    <Double,Double,Boolean>("gt",
                                                            "If the op1 is greater than op2, returns true,"+
                                                            "false otherwise.\n"+
                                                            "Required attributes:\n"+
                                                            "\top1=\"<string evaluating to a number>\"\n"+
                                                            "\top2=\"<string evaluating to a number>\"",
                                                            new edu.rice.cs.plt.lambda.Lambda2<Double,Double,Boolean>() {
      public Boolean value(Double op1, Double op2) { return (op1>op2); }
    },
                                                            UnaryOpProperty.PARSE_DOUBLE,
                                                            UnaryOpProperty.PARSE_DOUBLE,
                                                            UnaryOpProperty.FORMAT_BOOL));
    PropertyMaps.ONLY.setProperty("Misc", new BinaryOpProperty
                                    <Double,Double,Boolean>("lt",
                                                            "If the op1 is less than op2, returns true,"+
                                                            "false otherwise.\n"+
                                                            "Required attributes:\n"+
                                                            "\top1=\"<string evaluating to a number>\"\n"+
                                                            "\top2=\"<string evaluating to a number>\"",
                                                            new edu.rice.cs.plt.lambda.Lambda2<Double,Double,Boolean>() {
      public Boolean value(Double op1, Double op2) { return (op1<op2); }
    },
                                                            UnaryOpProperty.PARSE_DOUBLE,
                                                            UnaryOpProperty.PARSE_DOUBLE,
                                                            UnaryOpProperty.FORMAT_BOOL));
    PropertyMaps.ONLY.setProperty("Misc", new BinaryOpProperty
                                    <Double,Double,Boolean>("gte",
                                                            "If the op1 is greater than or equal to op2, returns true,"+
                                                            "false otherwise.\n"+
                                                            "Required attributes:\n"+
                                                            "\top1=\"<string evaluating to a number>\"\n"+
                                                            "\top2=\"<string evaluating to a number>\"",
                                                            new edu.rice.cs.plt.lambda.Lambda2<Double,Double,Boolean>() {
      public Boolean value(Double op1, Double op2) { return (op1>=op2); }
    },
                                                            UnaryOpProperty.PARSE_DOUBLE,
                                                            UnaryOpProperty.PARSE_DOUBLE,
                                                            UnaryOpProperty.FORMAT_BOOL));
    PropertyMaps.ONLY.setProperty("Misc", new BinaryOpProperty
                                    <Double,Double,Boolean>("lte",
                                                            "If the op1 is less than or equal to op2, returns true,"+
                                                            "false otherwise.\n"+
                                                            "Required attributes:\n"+
                                                            "\top1=\"<string evaluating to a number>\"\n"+
                                                            "\top2=\"<string evaluating to a number>\"",
                                                            new edu.rice.cs.plt.lambda.Lambda2<Double,Double,Boolean>() {
      public Boolean value(Double op1, Double op2) { return (op1<=op2); }
    },
                                                            UnaryOpProperty.PARSE_DOUBLE,
                                                            UnaryOpProperty.PARSE_DOUBLE,
                                                            UnaryOpProperty.FORMAT_BOOL));
    PropertyMaps.ONLY.setProperty("Misc", new BinaryOpProperty
                                    <String,String,Boolean>("eq",
                                                            "If the op1 is equal to op2, returns true,"+
                                                            "false otherwise.\n"+
                                                            "Required attributes:\n"+
                                                            "\top1=\"<string>\"\n"+
                                                            "\top2=\"<string>\"",
                                                            new edu.rice.cs.plt.lambda.Lambda2<String,String,Boolean>() {
      public Boolean value(String op1, String op2) { return op1.equals(op2); }
    },
                                                            UnaryOpProperty.PARSE_STRING,
                                                            UnaryOpProperty.PARSE_STRING,
                                                            UnaryOpProperty.FORMAT_BOOL));
    PropertyMaps.ONLY.setProperty("Misc", new BinaryOpProperty
                                    <Double,Double,Double>("add",
                                                           "Returns the sum of the two operands (op1+op2).\n"+
                                                           "Required attributes:\n"+
                                                           "\top1=\"<string evaluating to a number>\"\n"+
                                                           "\top2=\"<string evaluating to a number>\"",
                                                           new edu.rice.cs.plt.lambda.Lambda2<Double,Double,Double>() {
      public Double value(Double op1, Double op2) { return op1 + op2; }
    },
                                                           UnaryOpProperty.PARSE_DOUBLE,
                                                           UnaryOpProperty.PARSE_DOUBLE,
                                                           UnaryOpProperty.FORMAT_DOUBLE));
    PropertyMaps.ONLY.setProperty("Misc", new BinaryOpProperty
                                    <Double,Double,Double>("sub",
                                                           "Returns the difference between the two operands (op1-op2).\n"+
                                                           "Required attributes:\n"+
                                                           "\top1=\"<string evaluating to a number>\"\n"+
                                                           "\top2=\"<string evaluating to a number>\"",
                                                           new edu.rice.cs.plt.lambda.Lambda2<Double,Double,Double>() {
      public Double value(Double op1, Double op2) { return op1 - op2; }
    },
                                                           UnaryOpProperty.PARSE_DOUBLE,
                                                           UnaryOpProperty.PARSE_DOUBLE,
                                                           UnaryOpProperty.FORMAT_DOUBLE));
    PropertyMaps.ONLY.setProperty("Misc", new BinaryOpProperty
                                    <Double,Double,Double>("mul",
                                                           "Returns the product of the two operands (op1*op2).\n"+
                                                           "Required attributes:\n"+
                                                           "\top1=\"<string evaluating to a number>\"\n"+
                                                           "\top2=\"<string evaluating to a number>\"",
                                                           new edu.rice.cs.plt.lambda.Lambda2<Double,Double,Double>() {
      public Double value(Double op1, Double op2) { return op1 * op2; }
    },
                                                           UnaryOpProperty.PARSE_DOUBLE,
                                                           UnaryOpProperty.PARSE_DOUBLE,
                                                           UnaryOpProperty.FORMAT_DOUBLE));
    PropertyMaps.ONLY.setProperty("Misc", new BinaryOpProperty
                                    <Double,Double,Double>("div",
                                                           "Returns the quotient of the two operands (op1/op2).\n"+
                                                           "Required attributes:\n"+
                                                           "\top1=\"<string evaluating to a number>\"\n"+
                                                           "\top2=\"<string evaluating to a number>\"",
                                                           new edu.rice.cs.plt.lambda.Lambda2<Double,Double,Double>() {
      public Double value(Double op1, Double op2) { return op1 / op2; }
    },
                                                           UnaryOpProperty.PARSE_DOUBLE,
                                                           UnaryOpProperty.PARSE_DOUBLE,
                                                           UnaryOpProperty.FORMAT_DOUBLE));
    PropertyMaps.ONLY.setProperty("Misc", new UnaryOpProperty
                                    <String,Double>("strlen",
                                                    "Returns the length of the operand in characters.\n"+
                                                    "Required attributes:\n"+
                                                    "\tlist=\"<string>\"",
                                                    new edu.rice.cs.util.Lambda<Double,String>() {
      public Double apply(String s) { return ((double)s.length()); }
    },
                                                    "list",
                                                    null,
                                                    UnaryOpProperty.PARSE_STRING,
                                                    UnaryOpProperty.FORMAT_DOUBLE));
    PropertyMaps.ONLY.setProperty("Misc", new BinaryOpProperty
                                    <String,String,Double>("count",
                                                           "Counts the number of elements in the list.\n"+
                                                           "Required attributes:\n"+
                                                           "\tlist=\"<list string>\"\n"+
                                                           "Optional attributes:\n"+
                                                           "\tsep=\"<separator string>\"\n"+
                                                           "(if none specified, ${path.separator} will be used)",
                                                           new edu.rice.cs.plt.lambda.Lambda2<String,String,Double>() {
      public Double value(String s, String sep) {
        if (s.length()==0) return 0.0;
        return ((double)s.split(edu.rice.cs.plt.text.TextUtil.regexEscape(sep)).length);
      }
    },
                                                           "list",
                                                           null,
                                                           UnaryOpProperty.PARSE_STRING,
                                                           "sep",
                                                           System.getProperty("path.separator"),
                                                           UnaryOpProperty.PARSE_STRING,
                                                           UnaryOpProperty.FORMAT_DOUBLE));
    PropertyMaps.ONLY.setProperty("Misc", new QuaternaryOpProperty
                                    <String,Double,Double,String,String>("sublist",
                                                           "Extracts a sublist of elements from a list, beginning at "+
                                                           "a specified index, and including a specified number of elements."+
                                                           "Required attributes:\n"+
                                                           "\tlist=\"<list string>\"\n"+
                                                           "\tindex=\"<index in list, starting with 0>\"\n"+
                                                           "Optional attributes:\n"+
                                                           "\tcount=\"<number of items>\"\n"+
                                                           "(if not specified, 1 will be used)\n"+
                                                           "\tsep=\"<separator string>\"\n"+
                                                           "(if none specified, ${path.separator} will be used)",
                                                           new edu.rice.cs.plt.lambda.Lambda4<String,Double,Double,String,String>() {
      public String value(String s, Double index, Double count, String sep) {
        if (s.length()==0) return "";
        int i = index.intValue();
        int c = count.intValue();
        StringBuilder sb = new StringBuilder();
        String[] els = s.split(edu.rice.cs.plt.text.TextUtil.regexEscape(sep));
        for(int j=0; j<c; ++j) {
          if (i+j>=els.length) { break; }
          sb.append(sep);
          sb.append(els[i+j]);
        }
        s = sb.toString();
        if (s.length()>=sep.length()) {
          return s.substring(sep.length());
        }
        else {
          return "";
        }
      }
    },
                                                           "list",
                                                           null,
                                                           UnaryOpProperty.PARSE_STRING,
                                                           "index",
                                                           null,
                                                           UnaryOpProperty.PARSE_DOUBLE,
                                                           "count",
                                                           "1",
                                                           UnaryOpProperty.PARSE_DOUBLE,
                                                           "sep",
                                                           System.getProperty("path.separator"),
                                                           UnaryOpProperty.PARSE_STRING,
                                                           UnaryOpProperty.FORMAT_STRING));
    PropertyMaps.ONLY.setProperty("Misc", new TernaryOpProperty
                                    <String,String,String,String>("change.sep",
                                                           "Changes the separator used in a list of values."+
                                                           "Required attributes:\n"+
                                                           "\tlist=\"<list string>\"\n"+
                                                           "\told=\"<old separator>\"\n"+
                                                           "\tnew=\"<new separator>\"",
                                                           new edu.rice.cs.plt.lambda.Lambda3<String,String,String,String>() {
      public String value(String s, String oldSep, String newSep) {
        if (s.length()==0) return "";
        StringBuilder sb = new StringBuilder();
        for(String el: s.split(edu.rice.cs.plt.text.TextUtil.regexEscape(oldSep))) {
          sb.append(newSep);
          sb.append(el);
        }
        s = sb.toString();
        if (s.startsWith(newSep)) {
          s = s.substring(newSep.length());
        }
        return s;
      }
    },
                                                           "list",
                                                           null,
                                                           UnaryOpProperty.PARSE_STRING,
                                                           "old",
                                                           null,
                                                           UnaryOpProperty.PARSE_STRING,
                                                           "new",
                                                           null,
                                                           UnaryOpProperty.PARSE_STRING,
                                                           UnaryOpProperty.FORMAT_STRING));
    
    // XML properties, correspond to XMLConfig
    PropertyMaps.ONLY.setProperty("Misc", new DrJavaProperty("xml.in", "(XML Input...)",
                                                             "Read data from an XML file.\n"+
                                                             "Required attributes:\n"+
                                                             "\tfile=\"<file with the XML>\"\n"+
                                                             "\tpath=\"<path into the XML tree>\"\n"+
                                                             "\tdefault=\"<default value if not found>\"\n"+
                                                             "\tmulti=\"<true if multiple values are allowed>\"\n"+
                                                             "\tsep=\"<separator between results>\"") {
      public String toString() {
        return "(XML Input...)";
      }
      public void update() {
        String xmlfile = _attributes.get("file");
        String xmlpath = _attributes.get("path");
        String defval = _attributes.get("default");
        String multi = _attributes.get("multi");
        String sep = _attributes.get("sep");
        if ((xmlfile==null) ||
            (xmlpath==null) ||
            (defval==null)) {
          _value = "(XML Input Error...)";
          return;
        }
        try {
          File f = new File(xmlfile);
          if (!f.exists()) {
            _value = defval;
            return;
          }
          XMLConfig xc = new XMLConfig(f);
          List<String> values = xc.getMultiple(xmlpath);
          if (!"true".equals(multi.toLowerCase())) {
            if (values.size()!=1) {
              _value = defval;
              return;
            }
            _value = values.get(0);
            return;
          }
          StringBuilder sb = new StringBuilder();
          for (String v: values) {
            sb.append(sep);
            sb.append(v);
          }
          _value = sb.toString().substring(1);
        }
        catch(XMLConfigException xce) {
          _value = defval;
        }
      }
      public String getCurrent() {
        invalidate();
        return super.getCurrent();
      }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("file", null);
        _attributes.put("path", null);
        _attributes.put("default", null);
        _attributes.put("multi", "true");
        _attributes.put("sep", File.pathSeparator);
      }
    });
    
    PropertyMaps.ONLY.setProperty("Misc", new DrJavaProperty("xml.out.action", "(XML Output...)",
                                                             "Write data to an XML file. Since this is an "+
                                                             "action, it will not produce any output, but it will "+
                                                             "write to the XML file.\n"+
                                                             "Required attributes:\n"+
                                                             "\tfile=\"<file with the XML>\"\n"+
                                                             "\tpath=\"<path into the XML tree>\"\n"+
                                                             "\tcontent=\"<value to write into the XML>\"\n"+
                                                             "\tappend=\"<true to append, false to overwrite existing>\"") {
      public String toString() {
        return "(XML Output...)";
      }
      public void update() {
        String xmlfile = _attributes.get("file");
        String xmlpath = _attributes.get("path");
        String content = _attributes.get("content");
        String append = _attributes.get("append");
        if ((xmlfile==null) ||
            (xmlpath==null)) {
          _value = "(XML Output Error...)";
        }
        try {
          File f = new File(xmlfile);
          XMLConfig xc;
          if (f.exists()) { xc = new XMLConfig(f); }
          else { xc = new XMLConfig(); }
          xc.set(xmlpath, content, append.toLowerCase().equals("false"));
          xc.save(xmlfile);
          _value = "";
        }
        catch(XMLConfigException xce) {
          _value = "(XML Output Error...)";
        }
      }
      public String getCurrent() {
        invalidate();
        return super.getCurrent();
      }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("file", null);
        _attributes.put("path", null);
        _attributes.put("content", "");
        _attributes.put("append", "false");
      }
    });
    
    // Variables
    PropertyMaps.ONLY.setProperty("Misc", new DrJavaProperty("var",
                                                             "Create a new scope and define a variable with the "+
                                                             "specified name and value; then evaluate the command "+
                                                             "with the new variable in the environment.\n"+
                                                             "Required attributes:\n"+
                                                             "\tname=\"<name of the variable>\"\n"+
                                                             "\tval=\"<value of the variable>\"\n"+
                                                             "\tcmd=\"<command to evaluate>\"") {
      public void update() {
        String name = _attributes.get("name");
        String val = _attributes.get("val");
        String cmd = _attributes.get("cmd");
        if (name==null) {
          _value = "(var Error: name missing...)";
          return;
        }
        if (val==null) {
          _value = "(var Error: val missing...)";
          return;
        }
        if (cmd==null) {
          _value = "(var Error: cmd missing...)";
          return;
        }
        try {
          PropertyMaps.ONLY.addVariable(name,val);
          _value = StringOps.replaceVariables(cmd, PropertyMaps.ONLY, PropertyMaps.GET_CURRENT);
          PropertyMaps.ONLY.removeVariable(name);
        }
        catch(IllegalArgumentException e) {
          _value = "(var Error: "+e.getMessage()+"...)";
        }
      }
      public boolean isCurrent() { return false; }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("name", null);
        _attributes.put("val", null);
        _attributes.put("cmd", null);
      }
      public String toString() { return "--uninitialized--"; }
      public void setAttributes(HashMap<String,String> attrs, edu.rice.cs.util.Lambda<String,String> replaceLambda) {
        String value;
        for(String key: attrs.keySet()) {
          value = attrs.get(key);
          if (key.equals("cmd")) {
            // do not evaluate the cmd attribute yet
            setAttribute(key, value);
          }
          else {
            setAttribute(key, replaceLambda.apply(value));
          }
        }
      }
    });

    PropertyMaps.ONLY.setProperty("Misc", new DrJavaProperty("var.set",
                                                             "Mutate the value of the variable with the "+
                                                             "specified name and value.\n"+
                                                             "Required attributes:\n"+
                                                             "\tname=\"<name of the variable>\"\n"+
                                                             "\tval=\"<value of the variable>\"") {
      public void update() {
        String name = _attributes.get("name");
        String val = _attributes.get("val");
        if (name==null) {
          _value = "(var.set Error: name missing...)";
          return;
        }
        if (val==null) {
          _value = "(var.set Error: val missing...)";
          return;
        }
        try {
          PropertyMaps.ONLY.setVariable(name,val);
        }
        catch(IllegalArgumentException e) {
          _value = "(var.set Error: "+e.getMessage()+"...)";
          return;
        }
        _value = "";
      }
      public boolean isCurrent() { return false; }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("name", null);
        _attributes.put("val", null);
      }
      public String toString() { return ""; }
    });

    PropertyMaps.ONLY.setProperty("Misc", new DrJavaProperty("for",
                                                             "Create a new scope and define a variable with the "+
                                                             "specified name. Then process the given list in smaller "+
                                                             "pieces, assigning them to the variable.\n"+
                                                             "Required attributes:\n"+
                                                             "\tlist=\"<list string>\"\n"+
                                                             "\tvar=\"<name of the variable>\"\n"+
                                                             "\tcmd=\"<command to evaluate for each piece>\"\n"+
                                                             "Optional attributes:\n"+
                                                             "\tsep=\"<separator between elements>\"\n"+
                                                             "(if not defined, ${path.separator}, which is "+File.pathSeparator+
                                                             " on this machine)\n"+
                                                             "\toutsep=\"<separator between elements in the output>\"\n"+
                                                             "(if not defined, ${process.separator}, which is "+
                                                             edu.rice.cs.util.ProcessChain.PROCESS_SEPARATOR+
                                                             " on this machine, will be used)\n"+
                                                             "\teach=\"<number of elements to process as one piece>\"\n"+
                                                             "(if not defined, 1 is used)") {
      public void update() {
        String list = _attributes.get("list");
        String var = _attributes.get("var");
        String cmd = _attributes.get("cmd");
        if (list==null) {
          _value = "(for Error: list missing...)";
          return;
        }
        if (var==null) {
          _value = "(for Error: var missing...)";
          return;
        }
        if (cmd==null) {
          _value = "(for Error: cmd missing...)";
          return;
        }
        String sep = _attributes.get("sep");
        String outSep = _attributes.get("outsep");
        int each = 1;
        try {
          each = new Integer(_attributes.get("each"));
          if (each<1) { throw new NumberFormatException(); }
        }
        catch(NumberFormatException e) {
          _value = "(for Error: each not a positive number...)";
          return;
        }
        StringBuilder sb = new StringBuilder();
        StringBuilder sbVar = new StringBuilder();
        PropertyMaps.ONLY.addVariable(var,"");
        String val;
        try {
          String[] els = list.split(edu.rice.cs.plt.text.TextUtil.regexEscape(sep));
          int start = 0;
          while(start<els.length) {
            sbVar.setLength(0);
            for(int i=start; i<start+each; ++i) {
              if (i>=els.length) break;
              sbVar.append(sep);
              sbVar.append(els[i]);
            }
            val = sbVar.toString();
            if (val.startsWith(sep)) {
              val = val.substring(sep.length());
            }
            PropertyMaps.ONLY.setVariable(var,val);
            sb.append(outSep);
            sb.append(StringOps.replaceVariables(cmd, PropertyMaps.ONLY, PropertyMaps.GET_CURRENT));
            start += each;
          }
        }
        catch(IllegalArgumentException e) {
          _value = "(for Error: "+e.getMessage()+"...)";
        }
        PropertyMaps.ONLY.removeVariable(var);
        val = sb.toString();
        if (val.startsWith(outSep)) {
          val = val.substring(outSep.length());
        }
        _value = val;
      }
      public boolean isCurrent() { return false; }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("list", null);
        _attributes.put("var", null);
        _attributes.put("cmd", null);
        _attributes.put("sep", File.pathSeparator);
        _attributes.put("outsep", edu.rice.cs.util.ProcessChain.PROCESS_SEPARATOR);
        _attributes.put("each", "1");
      }
      public String toString() { return "--uninitialized--"; }
      public void setAttributes(HashMap<String,String> attrs, edu.rice.cs.util.Lambda<String,String> replaceLambda) {
        String value;
        for(String key: attrs.keySet()) {
          value = attrs.get(key);
          if (key.equals("cmd")) {
            // do not evaluate the cmd attribute yet
            setAttribute(key, value);
          }
          else {
            setAttribute(key, replaceLambda.apply(value));
          }
        }
      }
    });
    
    PropertyMaps.ONLY.setProperty("Config", new ConstantProperty("process.separator",
                                                                 edu.rice.cs.util.ProcessChain.PROCESS_SEPARATOR,
                                                                 "This property contains the separator used between "+
                                                                 "processes."));
  }
}
