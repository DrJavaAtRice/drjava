/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.project;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.w3c.dom.Node;

import edu.rice.cs.util.AbsRelFile;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.drjava.model.DummyDocumentRegion;
import edu.rice.cs.drjava.model.FileRegion;
import edu.rice.cs.drjava.model.debug.DebugWatchData;
import edu.rice.cs.drjava.model.debug.DebugBreakpointData;
import edu.rice.cs.util.XMLConfig;
import edu.rice.cs.drjava.project.MalformedProjectFileException;
import edu.rice.cs.util.StringOps;

import static edu.rice.cs.util.XMLConfig.XMLConfigException;

import edu.rice.cs.plt.text.TextUtil;

/** This parser loads XML configuration files using the XMLConfig class in the util package.
 * 
 *  <p> If at some point new information is to be stored in the project file, the following places in the code that need to
 *  changed: <menu> <li> If the new information pertains to a document, the DocFile class should be augmented to
 *  store the new info.  <li> The interface for the DocumentInfoGetter should be expanded to allow for the new
 *  data to be retrieved.  <li> Add a new clause to the else-if ladder in the FilePropertyVisitor.  <li> 
 *  Add the new information to the DocFile form the DocumentInfoGetter in the ProjectFileBuilder's 
 *  addSourceDocument method.</p>
 * 
 *  <p> If the change is at the top level, you must modify the evaluateExpression method in this parser and add the 
 *  corresponding methods to the ProjectFileIR, ProjectFileIRImpl, and ProjectFileBuilder</p>
 */
public class XMLProjectFileParser extends ProjectFileParserFacade {
  /** Singleton instance of XMLProjectFileParser */
  public static final XMLProjectFileParser ONLY = new XMLProjectFileParser();
  private XMLProjectFileParser() { _xmlProjectFile = true; }
  
  protected String _parent;
  protected String _srcFileBase;
  protected XMLConfig _xc;
  
  static edu.rice.cs.util.Log LOG = new edu.rice.cs.util.Log("xmlparser.log", false);
    
  /** @param projFile the file to parse
   *  @return the project file IR
   */
  public ProjectFileIR parse(File projFile) throws IOException, FileNotFoundException, MalformedProjectFileException {  
    _projectFile = projFile;
    _parent = projFile.getParent();
    _srcFileBase = _parent;
    ProjectFileIR pfir = new ProjectProfile(projFile);
    
    try {
      XMLConfig xcParent = new XMLConfig(projFile);

      // read version... this string isn't actually used
      String version = xcParent.get("drjava.version", "unknown");
      LOG.log("version = '" + version + "'");
      
      pfir.setDrJavaVersion(version);
      
      // create a sub-configuration so we don't have to prefix everything with "drjava/project/"
      _xc = new XMLConfig(xcParent, xcParent.getNodes("drjava/project").get(0));
      LOG.log(_xc.toString());
      String s;
      
      // read project root; must be present
      try {
        s = _xc.get(".root");
        LOG.log("root = '" + s + "'");
        File root = new File(_parent, s);
        LOG.log("_parent = " + _parent);
        pfir.setProjectRoot(root);
        _srcFileBase = root.getCanonicalPath();
        LOG.log("_srcFileBase from reading the prject root = " + _srcFileBase);
      }
      catch(XMLConfigException e) { throw new MalformedProjectFileException("XML Parse Error: " + e.getMessage() + "\n" + StringOps.getStackTrace(e)); }
      
      // read create jar options
      try {
        s = _xc.get("createjar.file");
        LOG.log("createjar.file = '" + s + "'");
        File jarFile = new File(_parent, s);
        pfir.setCreateJarFile(jarFile);
      }
      catch(XMLConfigException e) { /* not present is ok too */ }
      try {
        s = _xc.get("createjar.flags");
        LOG.log("createjar.flags = '" + s + "'");
        int flags = Integer.valueOf(s);
        pfir.setCreateJarFlags(flags);
      }
      catch(XMLConfigException e) { /* not present is ok too */ }
      
      try{
        s = _xc.get(".manifest");
        LOG.log("manifest = '" + s + "'");
        pfir.setCustomManifest(TextUtil.xmlUnescape(s));
      }catch(XMLConfigException e) { /* not present is fine */ }
      
      // read build dir
      try {
        s = _xc.get(".build");
        LOG.log("build = '" + s + "'");
        File buildDir = (!new File(s).isAbsolute())?new File(_parent, s):new File(s);
        pfir.setBuildDirectory(buildDir);
      }
      catch(XMLConfigException e) { /* not present is ok too */ }

      // read working dir; must be present
      try {
        s = _xc.get(".work");
        LOG.log("work = '" + s + "'");
        File workDir = (!new File(s).isAbsolute())?new File(_parent, s):new File(s);
        pfir.setWorkingDirectory(workDir);
      }
      catch(XMLConfigException e) { throw new MalformedProjectFileException("XML Parse Error: " + e.getMessage() + "\n" + StringOps.getStackTrace(e)); }

      // read main class
      try {
        s = _xc.get(".main");
        LOG.log("main = '" + s + "'");
        /*File mainClass = new File(_parent, s);
        pfir.setMainClass(mainClass);*/
        pfir.setMainClass(s);
      }
      catch(XMLConfigException e) { /* not present is ok too */ }
      
      try {
        s = _xc.get(".autorefresh");
        boolean b = Boolean.valueOf(s);
        pfir.setAutoRefreshStatus(b);
      } 
      catch(XMLConfigException e) { /* not important */}
      
      try { // must all be present
        // read source files and included files
        
        pfir.setSourceFiles(readSourceFiles("source", _srcFileBase));
        pfir.setAuxiliaryFiles(readSourceFiles("included", ""));      
        
        // read excluded files
        pfir.setExcludedFiles(readSourceFiles("excluded", ""));
        
      
        // read collapsed paths
        pfir.setCollapsedPaths(readCollapsed());
      
        // read class paths
        pfir.setClassPaths(readFiles("classpath", _srcFileBase));
      
        // read breakpoints
        pfir.setBreakpoints(readBreakpoints());
      
        // read watches
        pfir.setWatches(readWatches());

        // read bookmarks
        pfir.setBookmarks(readBookmarks());
      }
      catch(XMLConfigException e) { throw new MalformedProjectFileException("XML Parse Error: " + e.getMessage() + "\n" + StringOps.getStackTrace(e)); }
    }
    catch(XMLConfigException e) {
      throw new MalformedProjectFileException("Malformed XML project file." + e.getMessage() + "\n" + StringOps.getStackTrace(e));
    }
    catch(NumberFormatException e) {
      throw new MalformedProjectFileException("Malformed XML project file; a value that should have been an integer was not.\n" + StringOps.getStackTrace(e));
    }
    catch(IllegalArgumentException e) {
      throw new MalformedProjectFileException("Malformed XML project file; a value had the wrong type.\n" + StringOps.getStackTrace(e));
    }
    catch(IndexOutOfBoundsException e) {
      throw new MalformedProjectFileException("Malformed XML project file; a required value was missing.\n" + StringOps.getStackTrace(e));
    }    
    LOG.log(pfir.toString());
    return pfir;
  }
  
  protected List<DocFile> readSourceFiles(String path, String rootPath) throws MalformedProjectFileException {
    LOG.log("readSourceFiles(path='" + path + "', rootPath='" + rootPath + "')");
    List<DocFile> docFList = new ArrayList<DocFile>();
    List<Node> defs = _xc.getNodes(path + "/file");
    LOG.log("\tdefs.size() = " + defs.size());
    for(Node n: defs) { LOG.log("\t" + n.getNodeValue()); }

    for(Node n: defs) {
      LOG.log("\t" + n.toString());
      
      // now all path names are relative to node n...
      String name = _xc.get(".name",n);
      LOG.log("\t\tname = '" + name + "'");
      
      int selectFrom = _xc.getInt("select.from",n);
      int selectTo = _xc.getInt("select.to",n);
      LOG.log("\t\tselect = '" + selectFrom + " to " + selectTo + "'");
      
      int scrollCol = _xc.getInt("scroll.column",n);
      int scrollRow = _xc.getInt("scroll.row",n);
      LOG.log("\t\tscroll = '" + scrollCol + " , " + scrollRow + "'");
      
      String timestamp = _xc.get(".timestamp",n);
      LOG.log("\t\ttimestamp = '" + timestamp + "'");
      Date modDate;
      try {
        // attemp parsing in default locale
        modDate = ProjectProfile.MOD_DATE_FORMAT.parse(timestamp); }
      catch (java.text.ParseException e1) {
        // parsing in default locale failed
        try {
          // attempt parsing in current locale
          modDate = new SimpleDateFormat(ProjectProfile.MOD_DATE_FORMAT_STRING).parse(timestamp);
        }
        catch (java.text.ParseException e2) {
          // both parsings failed
          throw new MalformedProjectFileException("Source file node contains badly formatted timestamp.");
        }
      }
      
      String pkg = _xc.get(".package",n);
      LOG.log("\t\tpackage = '" + pkg + "'");
      
      boolean active;
      try {
        active = _xc.getBool(".active",n);
        LOG.log("\t\tactive = '" + active + "'");
      }
      catch(XMLConfigException e) { active = false; /* it's ok if it doesn't exist */ }
      
      /* added to check if file path name refers to absolute. Intended to eliminate project errors over network paths */
      Boolean absName = (new File(name)).isAbsolute();   
      
      DocFile docF = new DocFile(((rootPath.length() > 0 && !absName)?new File(rootPath,name):new File(name)).getAbsoluteFile(),
                                 new Pair<Integer,Integer>(selectFrom,selectTo),
                                 new Pair<Integer,Integer>(scrollCol,scrollCol),
                                 active,
                                 pkg);
      docF.setSavedModDate(modDate.getTime());
      docFList.add(docF);
    }
    return docFList;
  }

  protected List<AbsRelFile> readFiles(String path) {
    return readFiles(path, "");
  }
  
  protected List<AbsRelFile> readFiles(String path, String rootPath) {
    List<AbsRelFile> fList = new ArrayList<AbsRelFile>();
    List<Node> defs = _xc.getNodes(path + "/file");
    for(Node n: defs) {
      // now all path names are relative to node n...
      String name = _xc.get(".name",n);
      boolean abs = _xc.getBool(".absolute",n,true); // default to true for backward compatibility

      /* added to check if file path name refers to absolute. Intended to eliminate project errors over network paths */
      abs |= (new File(name)).isAbsolute();   
      
      AbsRelFile f = new AbsRelFile(((rootPath.length() > 0 && !abs)?
                                       new File(rootPath,name):
                                       new File(name)).getAbsoluteFile(),abs);
      fList.add(f);
    }
    return fList;
  }
  
  protected List<String> readCollapsed() {
    List<String> pList = new ArrayList<String>();
    List<Node> defs = _xc.getNodes("collapsed/path");
    for(Node n: defs) {
      // now all path names are relative to node n...
      pList.add(_xc.get(".name", n));
    }
    return pList;
  }
  
  protected List<DebugBreakpointData> readBreakpoints() {
    List<DebugBreakpointData> bpList = new ArrayList<DebugBreakpointData>();
    List<Node> defs = _xc.getNodes("breakpoints/breakpoint");
    for(Node n: defs) {
      // now all path names are relative to node n...
      String name = _xc.get(".file", n);
      final int lnr = _xc.getInt(".line", n);
      final boolean enabled = _xc.getBool(".enabled", n);
      DebugBreakpointData dbd;
      if ((_srcFileBase == null) || (new File(name).isAbsolute())) {
        final File f = new File(name);
        dbd = new DebugBreakpointData() {
          public File getFile() { return f; }
          public int getLineNumber() { return lnr; }
          public boolean isEnabled() { return enabled; }
        };
      }
      else {
        final File f = new File(_srcFileBase, name);
        dbd = new DebugBreakpointData() {
          public File getFile() { return f; }
          public int getLineNumber() { return lnr; }
          public boolean isEnabled() { return enabled; }
        };
      }
      bpList.add(dbd);
    }
    return bpList;
  }

  protected List<DebugWatchData> readWatches() {
    List<DebugWatchData> wList = new ArrayList<DebugWatchData>();
    List<Node> defs = _xc.getNodes("watches/watch");
    for(Node n: defs) {
      // now all path names are relative to node n...
      wList.add(new DebugWatchData(_xc.get(".name", n)));
    }
    return wList;
  }
    
  protected List<FileRegion> readBookmarks() {
    List<FileRegion> rList = new ArrayList<FileRegion>();
    List<Node> defs = _xc.getNodes("bookmarks/bookmark");
    for(Node n: defs) {
      // now all path names are relative to node n...
      String name = _xc.get(".file", n);
      final int from = _xc.getInt(".from", n);
      final int to = _xc.getInt(".to", n);
      File f;
      if ((_srcFileBase == null) || (new File(name).isAbsolute())) { f = new File(name); }
      else { f = new File(_srcFileBase, name); }
      rList.add(new DummyDocumentRegion(f, from, to));
    }
    return rList;
  }
}
