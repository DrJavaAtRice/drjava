/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK */

package edu.rice.cs.drjava.model.definitions;

import java.io.IOException;
import java.io.StringReader;

/** The simple lexer class for extracting package names from Java and Scala files.  Only the first package name is
  * returned.  This name may be divided across CONSECUTIVE, cumulative package statements as is legal in Scala. */
public class PackageLexer extends java.io.StreamTokenizer {
  
  /* inherited fields:
   *   String sval    // WARNING: sval is NOT interned so == comparison on sval DOES NOT WORK
   *   ...
   */
  
  public PackageLexer(String text) {
    super(new StringReader(text));  // pass a Reader wrapping the text to the StreamTokenizer constructor
    
    // Cancel the default single character comment status of '/'
    ordinaryChar('/');
    
    // accept Java/Scala style comments
    slashSlashComments(true);
    slashStarComments(true);
    
    // add '_', '.', and '$' to set of wordChars (package name is read as single word)
    wordChars('_', '_');
    wordChars('.', '.');
    wordChars('$', '$');
    
    // treat ';' as whitespace
    whitespaceChars(';', ';');
  }
  
  public String getPackageName() {
    
    try {
      
      StringBuilder name = new StringBuilder();
      
      // read longest token stream of the form { package <name> }*, concatenating the names to form
      
      String prefix = "";
      int token = nextToken();

      while (token == TT_WORD && sval.equals("package")) {
//        System.err.println("Performing loop iteration");
        token = nextToken();
        if (token != TT_WORD || sval.startsWith(".") || sval.endsWith(".") || sval.contains("..")) break;
        name.append(prefix).append(sval);
        prefix = ".";  // This is horrible imperative hack; in tail recursive form it is an extra argument
        token = nextToken();
      }
      return name.toString();
    }
    catch(IOException e) { return ""; }
  }
}