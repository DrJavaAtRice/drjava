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
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.io.*;

/** Class with getClassName method for finding the name of the first class, object, or trait defined in a file */

public class ClassAndInterfaceFinder {
  
  private StreamTokenizer tokenizer;
  
  /* constructor to support unit testing */
  public ClassAndInterfaceFinder(Reader r) {
    initialize(r);
  }
  
  /* normal constructor */
  public ClassAndInterfaceFinder(File f) {
    Reader r = null;
    try {
      try { r = new FileReader(f); }
      catch(FileNotFoundException e) { /* create a Reader for an "empty" file */
        r = new StringReader("");
      }
      initialize(r);
    }
    finally {
      try { if (r != null) r.close(); }
      catch(IOException ioe) { /* ignore exception on close */ }
    }
  }
  
  private void initialize(Reader r) {
    
    tokenizer = new StreamTokenizer(r);
    tokenizer.slashSlashComments(true);
    tokenizer.slashStarComments(true);
    tokenizer.lowerCaseMode(false);
    
    tokenizer.wordChars('_','_');
    tokenizer.wordChars('.','.');
    tokenizer.wordChars('$','$');
    
    // In Scala, treat ';' as whitespace
    tokenizer.whitespaceChars(';', ';');
  }
  
  /** Finds the the name of the first class, object, or trait defined in this file.
    * @return the String containing this name or "" if no such class, object, or interface is found.
    */
  public String getClassObjectOrTraitName() { return getName(true); }
  
  /** Finds the the name of the first class or object (excluding traits) defined in this file.
    * @return the String containing this name or "" if no such class or object is found.
    */
  public String getClassName() { return getName(false); }
  
  /** Finds the the name of the first class, object, or trait in this file, respecting the
    * value of the traitOK flag.
    * I hate flags but did not see a simpler way to avoid duplicated code.
    * This method has package (rather than private) visibility for testing purposes.
    */
  String getName(boolean traitOK) {
    try {
      String package_name = "";
      int tokenType;
      
      // find the "class"/"object"/"trait" or "package" keyword (may encounter EOF)
      do {
        tokenType = tokenizer.nextToken();
      } while(! isClassObjectOrTraitWord(tokenType, traitOK) && ! isPackageWord(tokenType));
      
      if (isEOF(tokenType)) return "";
      
      /* Save opening keyword */
      String keyword = tokenizer.sval;
      
      // find the name of the class or package (may encounter EOF)
      do {
        tokenType = tokenizer.nextToken();
      } while (! isWord(tokenType));
      
      if (isEOF(tokenType)) return "";
      
      if (keyword.equals("class") || keyword.equals("object")) return tokenizer.sval;  // a class or object defined without a package
      
      if (traitOK && keyword.equals("trait")) return tokenizer.sval; // a trait without a package
      
      if (keyword.equals("package")) package_name = tokenizer.sval;
      
      // find the "class", "object", or "trait" keyword
      do { tokenType = tokenizer.nextToken(); } while (! isClassObjectOrTraitWord(tokenType, traitOK));
      
      // find the name of the class or object
      do { tokenType = tokenizer.nextToken(); } while (! isWord(tokenType));
      
      if (tokenType == StreamTokenizer.TT_EOF) return "";
      
      if (package_name.length() > 0) return package_name + "." + tokenizer.sval;
      
      return tokenizer.sval;
      
    } catch(IOException e) { return ""; }
  }
  
  /** returns true iff the token is a word (as defined by StreamTokenizer)
   */
  private static boolean isWord(int tt) { return tt == StreamTokenizer.TT_WORD || isEOF(tt); }
  
  private static boolean isEOF(int tt)  { return tt == StreamTokenizer.TT_EOF; }
  
  
  /** returns true iff the token is "class" or we're at the end of the file
   */
  private boolean isClassObjectOrTraitWord(int tt, boolean traitOK) {
    return  isEOF(tt) || 
      (tt == StreamTokenizer.TT_WORD && (tokenizer.sval.equals("class") || tokenizer.sval.equals("object") || 
                                         (traitOK && tokenizer.sval.equals("trait"))));
  }
  
  /** returns true iff the token is "package" or we're at the end of the file
   */
  private boolean isPackageWord(int tt) {
    return (tt == StreamTokenizer.TT_WORD && tokenizer.sval.equals("package") ||
            tt == StreamTokenizer.TT_EOF);
  }
}



