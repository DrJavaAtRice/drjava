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

package edu.rice.cs.drjava.model.compiler;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;


/* Class used to get TreeMaps with dj* to java line number (and vise versa) conversions */

public class LanguageLevelStackTraceMapper {
  
  /** logging information */
  public static final edu.rice.cs.util.Log LOG = new edu.rice.cs.util.Log("llstm.txt",false);
  
  /** cache to store the tree maps */
  private HashMap<String,TreeMap<Integer,Integer>> cache;
  
  /** model used to get the OpenDefinitionsDocuments from files */
  private GlobalModel AGmodel;
  
  /* constructor */
  public LanguageLevelStackTraceMapper(GlobalModel AGM){
    AGmodel = AGM;
    cache = new HashMap<String,TreeMap<Integer,Integer>>();
  }
  
  
  public StackTraceElement replaceStackTraceElement(StackTraceElement s,
                                                    File d, TreeMap<Integer,Integer> m) {
    if(!matches(d,s)) return s;
    
    StackTraceElement NewS = new StackTraceElement(s.getClassName(),s.getMethodName(),d.getName(),m.get(s.getLineNumber()));
    
    return NewS;
    
    
  }
  
  
  /** 
   * method to replace the dj* file name and line number in a given stacktrace element.
   * @param s the StackTraceElement to do the replacing in
   * @param d the dj* file whose name and line numbers need replacing in the StackTraceElement
   */
  public StackTraceElement replaceStackTraceElement(StackTraceElement s,
                                                    File d) {
    if(!matches(d,s)) return s;
    String FileName = d.getAbsolutePath();
    if(cache.containsKey(FileName)) return replaceStackTraceElement(s,d,cache.get(FileName));
    
    String dn = d.getName();
    dn = dn.substring(0, dn.lastIndexOf('.'))+".java";
    File javaFile = new File(d.getParentFile(), dn);
    
    cache.put(FileName,ReadLanguageLevelLineBlock(javaFile));  
    
    return replaceStackTraceElement(s,d,cache.get(FileName));
    
    
  }
// If the file name in s matches f, check if n already exists in the cache.
// If it does, call the above replaceStackTraceElement with the cached TreeMap
// Otherwise load the TreeMap from the *.java file, store it in the cache, and then
// call the above replaceStackTraceElement.
// If the file name does not match, just return s
  
  
  /**
   * method to replace the dj* file name and line numbers in a given stacktrace element.
   * @param s the StackTraceElement to do the replacing in
   * @param ds a list of the dj* file whose names and line numbers need replacing in the StackTraceElement
   */
  public StackTraceElement replaceStackTraceElement(StackTraceElement s,
                                                    List</**OpenDefinitionsDocument*/ File> ds) {
    for(int i=0;i<ds.size();i++) {
      s = replaceStackTraceElement(s,ds.get(i)); 
    }
    return s;
  }
// Call replaceStackTraceElement(s, f.get(i), cache) for all i to replace the
// file name and map the numbers for all files
  
  
  
  /** 
   * method to replace the dj* file names and line numbers in the given stacktrace elements.
   * @param ss an array of StackTraceElement to do the replacing in
   * @param ds a list of the dj* file whose names and line numbers need replacing in the StackTraceElement
   */
  public StackTraceElement[] replaceStackTrace(StackTraceElement[] ss,
                                               List</**OpenDefinitionsDocument*/ File> ds){
    for(int i=0;i<ss.length;i++){
      ss[i]=replaceStackTraceElement(ss[i],ds);
    }
    return ss;
  }
// Call replaceStackTraceElement(ss.get(i), f, cache) for all i to process all stack trace
// elements in the array.
  
  
  /**
   * clears the TreeMap cache
   */
  public void clearCache(){
    cache = new HashMap<String,TreeMap<Integer,Integer>>();
  }
  
  
  /**
   * method to make sure the given file and StackTraceElement match
   * @param f the file
   * @param s the StackTraceElement
   */
  private boolean matches(File f, StackTraceElement s) {
    LOG.log("matches("+f+", "+s+")");
    if (s.getFileName()==null) return false;
    OpenDefinitionsDocument d;      
    try{
      d = AGmodel.getDocumentForFile(f);}
    
    catch(java.io.IOException e){return false;}
    
    String dn = d.getRawFile().getName();
    
// make sure that the document is a LL document
    if (!isLLFileName(dn)) return false;
    
// replace suffix with ".java"
    dn = dn.substring(0, dn.lastIndexOf('.'))+".java";
    
// make sure packages match
    String dp = d.getPackageName();
    int dotPos = s.getClassName().lastIndexOf('.');
    if ((dp.length()==0) && (dotPos>=0)) return false; // d in default package, s not
    if ((dp.length()>0) && (dotPos<0)) return false; // s in default package, d not
    String sp = "";
    if (dotPos>=0) {
      sp = s.getClassName().substring(0, dotPos);
    }
    if (!dp.equals(sp)) return false; // packages do not match
    
// make sure the file names match
    return s.getFileName().equals(dn);
  }
  
  
  
  /**
   * Reads the LanguageLevel header from a LL file and pulls the line number conversion map out.
   * @return <java line, dj* line>
   */
  public TreeMap<Integer, Integer> ReadLanguageLevelLineBlock(File LLFile){
    
    BufferedReader BReader = null;
    String ReadLine = "";
    
    try{  BReader = new BufferedReader(new FileReader(LLFile));  } catch(java.io.FileNotFoundException e){}
    
    try{  ReadLine = BReader.readLine();  }  catch(java.io.IOException e){}
    
    LOG.log("ReadLine = '"+ReadLine+"'");
    LOG.log("\tlastIndex = "+ReadLine.lastIndexOf(" "));
    Integer MapSize = new Integer (ReadLine.substring(ReadLine.lastIndexOf(" ")+1));
    
    try{  ReadLine = BReader.readLine();  }  catch(java.io.IOException e){}
    
    if(ReadLine.indexOf("//")!=0) MapSize=0;  //Kills the for loop if read line is not of correct format
    
    
    String temp = "";
    String numRnum = "";
    TreeMap<Integer,Integer> JavaDjMap = new TreeMap<Integer,Integer>();
    
    temp = ReadLine.substring(2);
    temp = temp.trim() + " ";
    
    Integer djNum;
    Integer javaNum;
    
    for(int i=0; i<MapSize; i++){
      if(temp.length()<2)  temp = ReadLanguageLevelLineBlockHelper(BReader);
      if(temp==null) break;
      
      numRnum = temp.substring(0,temp.indexOf(" "));
      
      djNum = new Integer(numRnum.substring(0,numRnum.indexOf("->")));
      javaNum = new Integer(numRnum.substring(numRnum.indexOf("->")+2));
      
      JavaDjMap.put(javaNum,djNum);
      temp = temp.substring(temp.indexOf(" ")).trim() + " ";
    }
    return JavaDjMap;
  }
  
  
  
  /**
   * Reads the LanguageLevel header from a LL file and pulls the line number conversion map out.
   * @return <dj* line, java line>
   */
  public TreeMap<Integer, Integer> ReadLanguageLevelLineBlockRev(File LLFile){
    
    BufferedReader BReader = null;
    String ReadLine = "";
    
    try{  BReader = new BufferedReader(new FileReader(LLFile));  } catch(java.io.FileNotFoundException e){}
    
    try{  ReadLine = BReader.readLine();  }  catch(java.io.IOException e){}
    
    LOG.log("ReadLine = '"+ReadLine+"'");
    LOG.log("\tlastIndex = "+ReadLine.lastIndexOf(" "));
    Integer MapSize = new Integer (ReadLine.substring(ReadLine.lastIndexOf(" ")+1));
    
    try{  ReadLine = BReader.readLine();  }  catch(java.io.IOException e){}
    
    if(ReadLine.indexOf("//")!=0) MapSize=0;  //Kills the for loop if read line is not of correct format
    
    
    String temp = "";
    String numRnum = "";
    TreeMap<Integer,Integer> DjJavaMap = new TreeMap<Integer,Integer>();
    
    temp = ReadLine.substring(2);
    temp = temp.trim() + " ";
    
    Integer djNum;
    Integer javaNum;
    
    for(int i=0; i<MapSize; i++){
      if(temp.length()<2)  temp = ReadLanguageLevelLineBlockHelper(BReader);
      if(temp==null) break;
      
      numRnum = temp.substring(0,temp.indexOf(" "));
      
      djNum = new Integer(numRnum.substring(0,numRnum.indexOf("->")));
      javaNum = new Integer(numRnum.substring(numRnum.indexOf("->")+2));
      
      DjJavaMap.put(djNum,javaNum);
      temp = temp.substring(temp.indexOf(" ")).trim() + " ";
    }
    return DjJavaMap;
  }
  
  
  
  /**
   * Helper method to read the next line in a file
   */
  private String ReadLanguageLevelLineBlockHelper(BufferedReader BR) {
    String line = "";
    try{  line = BR.readLine(); } catch(java.io.IOException e){}
    
    if(line.indexOf("//")!=0) return null;
    line = line.substring(2).trim();
    return line;
  }
  
  /** Return true if the file name ends with .dj0, .dj1 or .dj2. */
  public static boolean isLLFileName(String s) {
    return (s.endsWith(".dj0") ||
            s.endsWith(".dj1") ||
            s.endsWith(".dj2"));
  }
  
  /** Return true if the file name ends with .dj0, .dj1 or .dj2. */
  public static boolean isLLFile(File f) {
    return isLLFileName(f.getName());
  }
  
  /** Change a language level extension into a .java extension. */
  public static File getJavaFileForLLFile(File llFile) {
    if (!isLLFile(llFile)) throw new AssertionError("File is not a language level file: "+llFile);
    String dn = llFile.getPath();
    dn = dn.substring(0, dn.lastIndexOf('.'))+".java";
    return new File(dn);
  }
}