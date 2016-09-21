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

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.List;
import java.util.HashMap;
import java.util.TreeMap;

import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.DrJavaFileUtils;

/** Class used to get TreeMaps with dj* to java line number (and vise versa) conversions */
public class LanguageLevelStackTraceMapper {
  
  /** logging information */
  public static final edu.rice.cs.util.Log LOG = new edu.rice.cs.util.Log("llstm.txt",false);
  
  /** cache to store the tree maps */
  private volatile HashMap<String,TreeMap<Integer,Integer>> cache;
  
  /** model used to get the OpenDefinitionsDocuments from files */
  private volatile GlobalModel aGModel;
  
  /* constructor */
  public LanguageLevelStackTraceMapper(GlobalModel aGM){
    aGModel = aGM;
    cache = new HashMap<String,TreeMap<Integer,Integer>>();
  }
  
  public StackTraceElement replaceStackTraceElement(StackTraceElement s, File d, TreeMap<Integer,Integer> m) {
    
    int jl = s.getLineNumber();
    int lll = m.containsKey(jl) ? m.get(jl) : -1;
      
    return new StackTraceElement(s.getClassName(), s.getMethodName(), d.getName(), lll);
  }
  
  /** 
   * Converts java file and line number to corresponding dj* file name and 
   * line number in a given stacktrace element.
   * @param s the StackTraceElement to do be converted
   * @param d the dj* file whose name and line numbers are required in the StackTraceElement
   * @return the newly-converted stack trace element
   */
  public StackTraceElement replaceStackTraceElement(StackTraceElement s, File d) {
      
// If the file name in s matches d, check if n already exists in the cache.
// If it does, call the above replaceStackTraceElement with the cached TreeMap
// Otherwise load the TreeMap from the *.java file, store it in the cache, and then
// call the above replaceStackTraceElement.
// If the file name does not match, just return s
    
    if (! matches(d, s)) return s;
    String fileName = d.getAbsolutePath();
    if (cache.containsKey(fileName)) return replaceStackTraceElement(s, d, cache.get(fileName));
    
    String dn = d.getName();
    dn = dn.substring(0, dn.lastIndexOf('.')) + edu.rice.cs.drjava.config.OptionConstants.JAVA_FILE_EXTENSION;
    File javaFile = new File(d.getParentFile(), dn);
    
    TreeMap<Integer, Integer> djToJavaMap = readLLLineBlock(javaFile);
    
    cache.put(fileName, djToJavaMap);  
    
    return replaceStackTraceElement(s, d, djToJavaMap);
  }

  /** 
   * Replaces the dj* file name and line numbers in a given stacktrace element.
   * @param s the StackTraceElement to do the replacing in
   * @param ds a list of the dj* file whose names and line numbers need replacing in the StackTraceElement
   * @return the newly-converted stack trace element
   */
  public StackTraceElement replaceStackTraceElement(StackTraceElement s, List<File> ds) {
    for (int i = 0; i < ds.size(); i++) {
      s = replaceStackTraceElement(s, ds.get(i)); 
    }
    return s;
  }
// Call replaceStackTraceElement(s, f.get(i), cache) for all i to replace the
// file name and map the numbers for all files
  
  /** 
   * Replaces the dj* file names and line numbers in the given stacktrace elements.
   * @param ss an array of StackTraceElement to do the replacing in
   * @param ds a list of the dj* file whose names and line numbers need replacing in the StackTraceElement
   * @return the newly-converted stack trace element
   */
  public StackTraceElement[] replaceStackTrace(StackTraceElement[] ss, List<File> ds) {
    for(int i = 0; i < ss.length; i++) {
      ss[i] = replaceStackTraceElement(ss[i], ds);
    }
    return ss;
  }
// Call replaceStackTraceElement(ss.get(i), f, cache) for all i to process all stack trace
// elements in the array.
  
  
  /** Clears the TreeMap cache */
  public void clearCache() {
    cache = new HashMap<String,TreeMap<Integer,Integer>>();
  }
  
  /** 
   * Ensures the given file and StackTraceElement match.  The extension on 
   * the file f may be a LL extension while the extension in the 
   * StackTraceElement is the corresponding .java file. 
   * @param f the file
   * @param s the StackTraceElement
   * @return true if there's a match; false otherwise
   */
  private boolean matches(File f, StackTraceElement s) {
    LOG.log("matches(" + f + ", " + s + ")");
    if (s.getFileName() == null) return false;
//    OpenDefinitionsDocument d;      
//    try { d = aGModel.getDocumentForFile(f); }
//    catch(java.io.IOException e) { return false; }
//    
//    String dn = d.getRawFile().getName();
    
    String fn = f.getPath();  // a relative path because traces use relative paths for files.
    
// make sure that the document is a LL document
    if (! DrJavaFileUtils.isLLFile(fn)) return false;
    
// replace suffix with ".java"
    String javaFn = DrJavaFileUtils.getJavaForLLFile(fn);
    
//    Utilities.show("Document file name is: " + fn + "\nJava file name is: " + javaFn + "\nStack file name is: " + 
//                   s.getFileName() + "\nResult is: " + javaFn.endsWith(s.getFileName()));
    
//// make sure packages match
//    String dp = d.getPackageName();
//    int dotPos = s.getClassName().lastIndexOf('.');
//    if ((dp.length() == 0) && (dotPos >= 0)) return false; // d in default package, s not
//    if ((dp.length() > 0) && (dotPos < 0)) return false; // s in default package, d not
//    String sp = "";
//    if (dotPos >= 0) sp = s.getClassName().substring(0, dotPos);
//    if (! dp.equals(sp)) return false; // packages do not match
    
// make sure the file names match
    return javaFn.endsWith(s.getFileName());  // names in trace elements are relative
  }
  
  private TreeMap<Integer, Integer> createOneToOneMap(BufferedReader bufReader) {
    // legacy support for old .dj2 language level files:
    // see DrJava feature request 2990660
    // As of revisions 5225-5227, .dj2 files aren't converted by the LanguageLevelConverter anymore,
    // they are just copied. That means the debugger and JUnit test errors cannot translate their line
    // numbers in .java files back to the .dj2 line numbers. Since the .dj2 file and the .java file
    // are identical, we just create a 1-to-1 map that maps a line number to itself.
    TreeMap<Integer, Integer> oneToOne = new TreeMap<Integer, Integer>();
    int lineNo = 1;
    oneToOne.put(lineNo,lineNo);
    try {
      while(bufReader.readLine() != null) {
        ++lineNo;
        oneToOne.put(lineNo,lineNo);
      }
    }
    catch(java.io.IOException e) { /* ignore, just return incomplete map */ }
    return oneToOne;
  }
    
  /** 
   * Reads the LanguageLevel header from a LL file and pulls the line number conversion map out.
   * @param LLFile the language-level file to be read
   * @return {@literal <java line, dj* line>}
   */
  public TreeMap<Integer, Integer> readLLLineBlock(File LLFile){
    
    BufferedReader bufReader = null;
    String rdLine = "";
    
    try { bufReader = new BufferedReader(new FileReader(LLFile));  } catch(java.io.FileNotFoundException e){ }
    
    try { rdLine = bufReader.readLine();  }  catch(java.io.IOException e){ }
    
    if (! rdLine.startsWith("// Language Level Converter line number map: dj*->java. Entries:")) {
      // no language level header, create a 1-to-1 mapping
      return createOneToOneMap(bufReader);
    }
    
    // Process header line of block
    LOG.log("rdLine = '" + rdLine + "'");
    LOG.log("\tlastIndex = " + rdLine.lastIndexOf(" "));
    Integer mapSize = new Integer (rdLine.substring(rdLine.lastIndexOf(" ") + 1));
    
    try { rdLine = bufReader.readLine();  }  catch(java.io.IOException e){ }
    
    if (rdLine.indexOf("//") != 0) mapSize = 0;  // Kills the for loop if read line is not of correct format
   
    // Create the LL map
    TreeMap<Integer,Integer> javaDJMap = new TreeMap<Integer,Integer>();
    
    // Process block lines containing map entries; rdLine is first such line
    // Invariant for text: text has no leading whitespace and trailing whitespace consisting of a single blank
    String text = rdLine.substring(2).trim() + " ";
    
    Integer djNum;
    Integer javaNum;
    
//    Utilities.show("read " + mapSize + " entries from bufReader");
    for (int i = 0; i < mapSize; i++) {
      if (text.length() < 2)  text = readNextLLBlockLine(bufReader);
      if (text == null) break;  // no more entries in block; mapSize is wrong  // ERROR should be raised !!!

      int firstBlankPos = text.indexOf(" ");
      String numRnum = text.substring(0, firstBlankPos);
      text = text.substring(firstBlankPos).trim() + " ";  // Only need to trim leading blanks here; String API is clumsy
      
      djNum = new Integer(numRnum.substring(0, numRnum.indexOf("->")));
      javaNum = new Integer(numRnum.substring(numRnum.indexOf("->") + 2));
      
      javaDJMap.put(javaNum,djNum);
    }
    return javaDJMap;
  }
  
  
  
  /** 
   * Reads the LanguageLevel header from a LL file and pulls the line number conversion map out.
   * @param LLFile the language-level file to be read
   * @return {@literal <dj* line, java line>}
   */
  public TreeMap<Integer, Integer> readLLBlock(File LLFile) {
    
    BufferedReader bufReader = null;
    String rdLine = "";
    
    try { bufReader = new BufferedReader(new FileReader(LLFile)); } catch(java.io.FileNotFoundException e){ }
    
    try { rdLine = bufReader.readLine(); } catch(java.io.IOException e){ }
    
    if (! rdLine.startsWith("// Language Level Converter line number map: dj*->java. Entries:")) {
      // no language level header, create a 1-to-1 mapping
      return createOneToOneMap(bufReader);
    }
    
    LOG.log("rdLine = '" + rdLine + "'");
    LOG.log("\tlastIndex = " + rdLine.lastIndexOf(" "));
    Integer mapSize = new Integer (rdLine.substring(rdLine.lastIndexOf(" ") + 1));
    
    try { rdLine = bufReader.readLine(); } catch(java.io.IOException e){ }
    
    if(rdLine.indexOf("//") != 0) mapSize = 0;  // Kills the for loop if read line is not of correct format

    TreeMap<Integer,Integer> map = new TreeMap<Integer,Integer>();
    
    String text = rdLine.substring(2).trim() + " ";  // invariant: temp has no leading spaces and a single trailing space
    String numRnum = "";
    
    int djNum;
    int javaNum;

    for(int i = 0; i < mapSize; i++) {
      if (text.length() < 2)  text = readNextLLBlockLine(bufReader);
      if (text == null) break;
      
      numRnum = text.substring(0, text.indexOf(" "));
      
      djNum = Integer.parseInt(numRnum.substring(0, numRnum.indexOf("->")), 10);
      javaNum = Integer.parseInt(numRnum.substring(numRnum.indexOf("->") + 2), 10);
      
      map.put(djNum, javaNum);
      text = text.substring(text.indexOf(" ")).trim() + " ";  // slices off first non-blank section
      // NOTE: it would more efficient to simply remove all leading whitespace instead of trimming and adding a space.
    }
    return map;
  }
  
  /** 
   * Helper method to read the next comment line in a file.  Returns null if 
   * no new comment line exists. 
   * Line is trimmed and padded by a single blank on the end. 
   * @param br buffer from which to read the next comment line
   * @return the next comment line
   */
  private String readNextLLBlockLine(BufferedReader br) {
    String line = "";
    try { line = br.readLine(); } catch(java.io.IOException e){ }
    
    if (line.indexOf("//") != 0) return null;
    line = line.substring(2).trim() + " ";
    return line;
  }
}
