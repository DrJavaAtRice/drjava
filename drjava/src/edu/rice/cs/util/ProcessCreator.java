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

package edu.rice.cs.util;

import edu.rice.cs.drjava.config.PropertyMaps;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This class abstracts out process creation, similar to ProcessCreator,
 * which is only available in Java 1.5.
 * This ProcessCreator cannot deal with process sequences and chains.
 * It can only create one processes.
 */

public class ProcessCreator {
  protected String _cmdline = null;
  protected String _evaluatedCmdLine = null;
  protected String _workdir;
  protected String _evaluatedWorkDir = null;
  protected String[] _cmdarray; // command line, already split
  protected Map<String,String> _env;
  protected PropertyMaps _props = PropertyMaps.TEMPLATE;
  
  /** Degenerate constructor, only for subclasses that completely override this class. */
  protected ProcessCreator() { }
  
  /** Constructor for a process creator with the given command line and map of properties.
    * @param cmdline command line
    * @param workdir working directory
    * @param pm PropertyMaps used for substitution when replacing variables
    */
  public ProcessCreator(String cmdline, String workdir, PropertyMaps pm) {
    _cmdline = cmdline;
    _workdir = workdir;
    _props = pm;
  }

  /** Constructor for a process creator with the given command line already split up,
    * and map of properties.
    * @param cmdarray array of command line arguments
    * @param workdir working directory
    */
  public ProcessCreator(String[] cmdarray, String workdir) {
    _cmdarray = cmdarray;
    _workdir = workdir;
  }
  
  /** Cached copy of the reconstructed command line. */
  protected String _cachedCmdLine = null;
  
  /** Get the command line.
    * @return command line
    */
  public String cmdline() {
    if (_cmdline == null) {
      if (_cachedCmdLine == null) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < _cmdarray.length; ++i) {
          sb.append(" ");
          sb.append(StringOps.unescapeFileName(_cmdarray[i]));
        }
        _cachedCmdLine = sb.toString();
        if (_cachedCmdLine.length() > 0) {
          _cachedCmdLine = _cachedCmdLine.substring(1);
        }
      }
      return _cachedCmdLine;
    }
    else {
      return _cmdline;
    }
  }
  
  /** Returns a map of this process creator's environment.
    * @return environment map
    */
  public Map<String,String> environment() {
    return _env;
  }
  
  /** Returns this process creator's working directory.
    * @return working directory
    */
  public String workDir() {
    return _workdir;
  }
  
  /** Return the command line after evaluation, or null if it hasn't been replaced yet. */
  public String evaluatedCommandLine() {
    return _evaluatedCmdLine;
  }
  
  /** Return the work directory after evaluation, or null if it hasn't been replaced yet. */
  public String evaluatedWorkDir() {
    return _evaluatedWorkDir;
  }
  
  /** Return the PropertyMaps object used for substitution. */
  public PropertyMaps getPropertyMaps() { return _props; }
  
  /** Starts a new process using the attributes of this process creator.
    */
  public Process start() throws IOException {
    // set up work directory
    _evaluatedWorkDir = StringOps.replaceVariables(_workdir, _props, PropertyMaps.GET_CURRENT);
    _evaluatedWorkDir = StringOps.unescapeFileName(_evaluatedWorkDir);
    File dir = null;
    if (!_evaluatedWorkDir.trim().equals("")) { dir = new File(_evaluatedWorkDir); }
    
    // set up environment
    String[] env = null;
    if ((_env != null) && (_env.size() > 0)) {
      env = new String[_env.size()];
      int i = 0;
      for(String key: _env.keySet()) {
        String value = _env.get(key);
        env[i] = key + "=" + value;
      }
    }
    
    // set up command line, if necessary
    if (_cmdline != null) {
      _evaluatedCmdLine = StringOps.replaceVariables(_cmdline, _props, PropertyMaps.GET_CURRENT);
      List<List<List<String>>> seqs = StringOps.commandLineToLists(_evaluatedCmdLine);
      if (seqs.size() != 1) { throw new IllegalArgumentException("ProcessCreator needs a command line with just one process."); }
      List<List<String>> pipe = seqs.get(0);
      if (pipe.size()<1) { throw new IllegalArgumentException("ProcessCreator needs a command line with just one process."); }
      List<String> cmds = pipe.get(0);
      _cmdarray = new String[cmds.size()];
      for (int i = 0; i < cmds.size(); ++i) {
        _cmdarray[i] = StringOps.unescapeFileName(cmds.get(i));
      }
    }
    
    return Runtime.getRuntime().exec(_cmdarray,env,dir);
  }
}
