/*BEGIN_COPYRIGHT_BLOCK*

DrJava Eclipse Plug-in BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of DrJava, the JavaPLT group, Rice University, nor the names of software 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.plugins.eclipse;

/**
 * A collection of preference names relevant to the DrJava Plug-in for Eclipse.
 * This file is a blatant duplication of portions of OptionConstants, which
 * cannot be used in Eclipse because of generics and our custom configuration
 * framework.
 * 
 * @version $Id$
 */
public interface DrJavaConstants {
  
  /**
   * The font used in the interactions pane.
   * (This is managed by the FontRegistry and an extension point in plugin.xml,
   * unlike the other preferences.)
   */
  public static final String FONT_MAIN  = "edu.rice.cs.drjava.InteractionsFont";
  
  /**
   * Whether to prompt before resetting the interactions pane.
   */
  public static final String INTERACTIONS_RESET_PROMPT =
    "interactions.reset.prompt";
  
  /**
   * Whether to allow users to access to all members in the Interactions Pane.
   */
  public static final String ALLOW_PRIVATE_ACCESS = "allow.private.access";

  /**
   * Whether to prompt when the interactions pane is unexpectedly reset.
   */
  public static final String INTERACTIONS_EXIT_PROMPT =
    "interactions.exit.prompt";
  
  /**
   * Number of lines to remember in the Interactions History.
   */
  public static final String HISTORY_MAX_SIZE = "history.max.size";

  /**
   * Optional arguments to the interpreter JVM.
   */
  public static final String JVM_ARGS = "jvm.args";
}
