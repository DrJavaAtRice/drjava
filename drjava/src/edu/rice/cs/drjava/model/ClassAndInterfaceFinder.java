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

public class ClassAndInterfaceFinder{
  private File _file;
  
  public ClassAndInterfaceFinder(File f){
    _file = f;
  }
  
  public String getClassName(){
    try{
      String package_name = "";
      
      FileReader fr = new FileReader(_file);
      StreamTokenizer tokenizer = new StreamTokenizer(fr);
      tokenizer.slashSlashComments(true);
      tokenizer.slashStarComments(true);
      tokenizer.lowerCaseMode(false);
      tokenizer.wordChars('_','_');
      tokenizer.wordChars('.','.');
      
      int tokenType;
      
      // find the "class" or "package" keyword
      do{
        tokenType = tokenizer.nextToken();
      }while(!foundClassOrInterfaceKeyword(tokenType, tokenizer) && 
             !foundPackageKeyword(tokenType, tokenizer));
      
      String keyword = tokenizer.sval;
      
      // find the name of the class or package
      do{
        tokenType = tokenizer.nextToken();
      }while(!(tokenType == StreamTokenizer.TT_WORD || tokenType == StreamTokenizer.TT_EOF));
      
      
      if(keyword.equals("class")){
        // we have just a class defined without a package, so return;
        return tokenizer.sval;
      }else
      if(keyword.equals("interface")){
        // we have just an interface without a package, so return;
        return tokenizer.sval;
      }else
      if(keyword.equals("package")){
        package_name = tokenizer.sval;
      }
      
      // find the "class" keyword
      do{
        tokenType = tokenizer.nextToken();
      }while(!foundClassOrInterfaceKeyword(tokenType, tokenizer));
      
      // find the name of the class or interface
      do{
        tokenType = tokenizer.nextToken();
      }while(!(tokenType == StreamTokenizer.TT_WORD || tokenType == StreamTokenizer.TT_EOF));
      
      
      if(tokenType != StreamTokenizer.TT_EOF){
        if(package_name.length() > 0){
          return package_name + "." + tokenizer.sval;
        }else{
          return tokenizer.sval;
        }
      }else{
        return "";      
      }
    }catch(FileNotFoundException e){
      return "";
    }catch(IOException e){
      return "";
    }
  }
  
  /**
   * returns true if the token is "class" or we're at the end of the file
   */
  private boolean foundClassOrInterfaceKeyword(int type, StreamTokenizer tokenizer){
    return (type == StreamTokenizer.TT_WORD && tokenizer.sval.equals("class") ||
            type == StreamTokenizer.TT_WORD && tokenizer.sval.equals("interface") ||
            type == StreamTokenizer.TT_EOF);
  }

  /**
   * returns true if the token is "package" or we're at the end of the file
   */
  private boolean foundPackageKeyword(int type, StreamTokenizer tokenizer){
    return (type == StreamTokenizer.TT_WORD && tokenizer.sval.equals("package") ||
            type == StreamTokenizer.TT_EOF);
  }
}



