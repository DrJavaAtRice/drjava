/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

import edu.rice.cs.util.newjvm.ExecJVM;
import edu.rice.cs.drjava.config.PropertyMaps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This class abstracts out creation of another JVM.
 */

public class JVMProcessCreator extends ProcessCreator { 
  protected String _jvmArgs;

  /** Creates a new process creator.
   * @param jvmArgs arguments for the JVM
   * @param cmdline command line
   * @param workdir working directory
   */
  public JVMProcessCreator(String jvmArgs, String cmdline, String workdir) {
    super(cmdline, workdir);
    _jvmArgs = jvmArgs;
  }
  

  /** Get the command line.
   * @return command line
   */
  public String cmdline() {
    return ExecJVM.getExecutable() + " " + _jvmArgs + _cmdline;
  }
  
  /** Starts a new JCM process using the attributes of this process creator.
   */
  public Process start() throws IOException {
    List<String> jvmArgs = StringOps.commandLineToList(StringOps.replaceVariables(_jvmArgs, PropertyMaps.ONLY, PropertyMaps.GET_CURRENT));
    List<String> cmds = StringOps.commandLineToList(StringOps.replaceVariables(_cmdline, PropertyMaps.ONLY, PropertyMaps.GET_CURRENT));
    LinkedList<String> args = new LinkedList<String>();
    args.add(ExecJVM.getExecutable());
    args.addAll(jvmArgs);
    args.addAll(cmds);
    String[] cmdarray = new String[args.size()];
    for (int i=0; i<args.size(); ++i) {
      cmdarray[i] = StringOps.unescapeSpacesWith1bHex(args.get(i));
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
    return Runtime.getRuntime().exec(cmdarray,env,dir);
  }
}
