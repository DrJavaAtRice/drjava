/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.io.*;

/** Class with getClassName method for finding the name of the first class or 
 *  interface defined in a file */

public class ClassAndInterfaceFinder {
  
  private StreamTokenizer tokenizer;
  
  /* constructor to support unit testing */
  public ClassAndInterfaceFinder(Reader r) {
    initialize(r);
  }
  
  /* normal constructor */
  public ClassAndInterfaceFinder(File f) {
    Reader r;
    try {
      r = new FileReader(f);
    }
    catch(FileNotFoundException e) { /* create a Reader for an "empty" file */
      r = new StringReader("");
    }
    initialize(r);
  }
  
  
  
  private void initialize(Reader r) {
    
    tokenizer = new StreamTokenizer(r);
    tokenizer.slashSlashComments(true);
    tokenizer.slashStarComments(true);
    tokenizer.lowerCaseMode(false);
    tokenizer.wordChars('_','_');
    tokenizer.wordChars('.','.');
  }
  
  /** Finds the the name of the first class or interface defined in this file.
   *  @return the String containing this name or "" if no such class or interface
   *          is found.
   */
  public String getClassOrInterfaceName() { return getName(true); }
  
  /** Finds the the name of the first class (excluding interfaces) defined in this file.
   *  @return the String containing this name or "" if no such class or interface
   *          is found.
   */
  public String getClassName() { return getName(false); }
  
  /** Finds the the name of the first class or interface in this file, respecting the
   *  value of the interfaceOK flag.
   *  I hate flags but did not see a simpler way to avoid duplicated code.
   *  This method has package (rather than private) visibility for testing purposes.
   */
  String getName(boolean interfaceOK) {
    try {
      String package_name = "";
      int tokenType;
      
      // find the "class"/"interface" or "package" keyword (may encounter EOF)
      do {
        tokenType = tokenizer.nextToken();
      } while(! isClassOrInterfaceWord(tokenType,interfaceOK) && ! isPackageWord(tokenType));

      if (isEOF(tokenType)) return "";
      
      /* Save opening keyword */
      String keyword = tokenizer.sval;
      
      // find the name of the class or package (may encounter EOF)
      do {
        tokenType = tokenizer.nextToken();
      } while (! isWord(tokenType));

      if (isEOF(tokenType)) return "";
      
      if(keyword.equals("class")) return tokenizer.sval;  // a class defined without a package
        
      if (interfaceOK && keyword.equals("interface")) return tokenizer.sval; // an interface without a package
  
      if (keyword.equals("package")) package_name = tokenizer.sval;
      
      // find the "class" keyword
      do { tokenType = tokenizer.nextToken(); } while (! isClassOrInterfaceWord(tokenType, interfaceOK));
      
      // find the name of the class or interface
      do { tokenType = tokenizer.nextToken(); } while (! isWord(tokenType));
      
      if (tokenType == StreamTokenizer.TT_EOF) return "";
      
      if (package_name.length() > 0) return package_name + "." + tokenizer.sval;
      
      return tokenizer.sval;
      
    } catch(IOException e) {
      return "";
    }
  }
  
  /**
   * returns true iff the token is a word (as defined by StreamTokenizer)
   */
  private static boolean isWord(int tt) { return tt == StreamTokenizer.TT_WORD || isEOF(tt); }
  
  private static boolean isEOF(int tt)  { return tt == StreamTokenizer.TT_EOF; }
  
  
  /**
   * returns true iff the token is "class" or we're at the end of the file
   */
  private boolean isClassOrInterfaceWord(int tt, boolean interfaceOK) {
    return  isEOF(tt) || 
      (tt == StreamTokenizer.TT_WORD && tokenizer.sval.equals("class")) ||
      (tt == StreamTokenizer.TT_WORD && interfaceOK && tokenizer.sval.equals("interface"));
  }

  /**
   * returns true iff the token is "package" or we're at the end of the file
   */
  private boolean isPackageWord(int tt) {
    return (tt == StreamTokenizer.TT_WORD && tokenizer.sval.equals("package") ||
            tt == StreamTokenizer.TT_EOF);
  }
}



