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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class abstracts out process creation, similar to ProcessCreator,
 * which is only available in Java 1.5. Additionally, it transparently
 * creates process sequences and process chains, including piping.
 */

public class GeneralProcessCreator extends ProcessCreator {
  protected List<List<List<String>>> _seqs;
    
  /** Constructor for a process creator with the given command line and the work directory.
    * @param cmdline command line
    * @param workdir working directory
    * @param pm PropertyMaps object used for substitution
    */
  public GeneralProcessCreator(String cmdline, String workdir, PropertyMaps pm) {
    _cmdline = cmdline;
    _workdir = workdir;
    _props = pm;
  }

  /** Constructor for a process creator with the given command line already split up, and
    * the work directory.
    * @param seqs a sequence of commands to pipe
    * @param workdir working directory
    * @param pm PropertyMaps object used for substitution
    */
  public GeneralProcessCreator(List<List<List<String>>> seqs, String workdir, PropertyMaps pm) {
    _seqs = seqs;
    _workdir = workdir;
    _props = pm;
  }
  
  /** Reconstructs the command line for a simple process. */
  protected static String getProcessCmdLine(List<String> cmds) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < cmds.size(); ++i) {
      sb.append(" ");
      sb.append(StringOps.unescapeFileName(cmds.get(i)));
    }
    String s = sb.toString();
    if (s.length() > 0) {
      s = s.substring(1);
    }
    return s;
  }
  
  /** Reconstructs the command line for a process chain. */
  protected static String getProcessChainCmdLine(List<List<String>> pipe) {
    StringBuilder sb = new StringBuilder();
    final String sep = " " + ProcessChain.PIPE_SEPARATOR + " ";
    for (int i = 0; i < pipe.size(); ++i) {
      sb.append(sep);
      sb.append(getProcessCmdLine(pipe.get(i)));
    }
    String s = sb.toString();
    if (s.length() > 0) {
      s = s.substring(sep.length());
    }
    return s;
  }
  
  /** Reconstructs the command line for a process sequence. */
  protected static String getProcessSequenceCmdLine(List<List<List<String>>> seqs) {
    StringBuilder sb = new StringBuilder();
    final String sep = " " + ProcessChain.PROCESS_SEPARATOR + " ";
    for (int i = 0; i < seqs.size(); ++i) {
      sb.append(sep);
      sb.append(getProcessChainCmdLine(seqs.get(i)));
    }
    String s = sb.toString();
    if (s.length() > 0) {
      s = s.substring(sep.length());
    }
    return s;
  }
  
  /** Get the command line.
    * @return command line
    */
  public String cmdline() {
    if (_cmdline == null) {
      if (_cachedCmdLine == null) {
        if (_seqs.size() == 1) {
          // only one piping chain, creating a process sequence is not necessary
          List<List<String>> pipe = _seqs.get(0);
          if (pipe.size() == 1) {
            // only one process, creating a piping chain is not necessary
            List<String> cmds = pipe.get(0);
            _cachedCmdLine = getProcessCmdLine(cmds);
          }
          else {
            // more than one process, create a process chain
            _cachedCmdLine = getProcessChainCmdLine(pipe);
          }
        }
        else  {
          // more than one piping chain, create a process sequence
          _cachedCmdLine = getProcessSequenceCmdLine(_seqs);
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

  public static final edu.rice.cs.util.Log LOG = new edu.rice.cs.util.Log("process.txt",false);
  
  /** Starts a new process using the attributes of this process creator. */
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

    // set up command line
    if (_cmdline != null) {
      _evaluatedCmdLine = StringOps.replaceVariables(_cmdline, _props, PropertyMaps.GET_CURRENT);
      _seqs = StringOps.commandLineToLists(_evaluatedCmdLine);
    }
    LOG.log("\t" + edu.rice.cs.plt.iter.IterUtil.toString(_seqs));
    if (_seqs.size()<1) { throw new IOException("No process to start."); }
    if (_seqs.size() == 1) {
      // only one piping chain, creating a process sequence is not necessary
      List<List<String>> pipe = _seqs.get(0);
      if (pipe.size()<1) { throw new IOException("No process to start."); }
      if (pipe.size() == 1) {
        // only one process, creating a piping chain is not necessary
        List<String> cmds = pipe.get(0);
        if (cmds.size()<1) { throw new IOException("No process to start."); }
        String[] cmdarray = new String[cmds.size()];
        for (int i = 0; i < cmds.size(); ++i) {
          cmdarray[i] = StringOps.unescapeFileName(cmds.get(i));
        }
        // creating a simple process
        return Runtime.getRuntime().exec(cmdarray,env,dir);
      }
      // more than one process, create a process chain
      ProcessCreator[] creators = new ProcessCreator[pipe.size()];
      for (int i = 0; i < pipe.size(); ++i) {
        List<String> cmds = pipe.get(i);
        if (cmds.size()<1) { throw new IOException("No process to start."); }
        String[] cmdarray = new String[cmds.size()];
        for (int j=0; j<cmds.size(); ++j) {
          cmdarray[j] = StringOps.unescapeFileName(cmds.get(j));
        }
        creators[i] = new ProcessCreator(cmdarray, _workdir);
      }
      return new ProcessChain(creators);
    }
    // more than one piping chain, create a process sequence
    ProcessCreator[] creators = new ProcessCreator[_seqs.size()];
    for (int i = 0; i < _seqs.size(); ++i) {
      List<List<List<String>>> l = new ArrayList<List<List<String>>>();
      l.add(_seqs.get(i));
      creators[i] = new GeneralProcessCreator(l, _workdir, _props);
    }
    return new ProcessSequence(creators);
  }
}
