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

package edu.rice.cs.util;

import edu.rice.cs.drjava.config.PropertyMaps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This class abstracts out process creation, similar to ProcessCreator,
 * which is only available in Java 1.5.
 */

public class ProcessCreator {
  protected String _cmdline;
  protected String _workdir;
  protected Map<String,String> _env;
    
  /** Constructor for a process creator with the given command line and map of properties.
   * @param cmdline command line
   * @param workdir working directory
   */
  public ProcessCreator(String cmdline, String workdir) {
    _cmdline = cmdline;
    _workdir = workdir;
  }
  
  /** Get the command line.
   * @return command line
   */
  public String cmdline() {
    return _cmdline;
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

  /** Starts a new process using the attributes of this process creator.
   */
  public Process start() throws IOException {
    List<String> cmds = StringOps.commandLineToList(StringOps.replaceVariables(_cmdline, PropertyMaps.ONLY, PropertyMaps.GET_CURRENT));
    String[] cmdarray = new String[cmds.size()];
    for (int i=0; i<cmds.size(); ++i) {
      cmdarray[i] = StringOps.unescapeSpacesWith1bHex(cmds.get(i));
    }
    String workdir = StringOps.replaceVariables(_workdir, PropertyMaps.ONLY, PropertyMaps.GET_CURRENT);
    workdir = StringOps.unescapeSpacesWith1bHex(workdir);
    File dir = null;
    if (!workdir.trim().equals("")) { dir = new File(workdir); }
    String[] env = null;
    if ((_env!=null) && (_env.size()>0)) {
      env = new String[_env.size()];
      int i = 0;
      for(String key: _env.keySet()) {
        String value = _env.get(key);
        env[i] = key+"="+value;
      }
    }
    
    // edu.rice.cs.util.Log log = new edu.rice.cs.util.Log("process.txt", true);
    // for(String c: cmdarray) { log.log(c); }
    // log.log("workdir: "+dir);
    
    return Runtime.getRuntime().exec(cmdarray,env,dir);
  }
}
