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

package edu.rice.cs.drjava.project;

import java.io.*;
import java.util.*;

/**
 * A factory which creates SourceTags, BuildDirTags, ResourcesTags, lasspathTags, and JarTags from the output of a Reader
 */
public class TagFactory {
  /* what a source tag looks like */
  public static final String SOURCE_TAG = "(Source";
  /* what a resource tag looks like */
  public static final String RESOURCE_TAG = "(Resources";
  /* what a misc tag looks like */
  public static final String BUILDDIR_TAG = "(BuildDir";
  /* what a classpath tag looks like */
  public static final String CLASSPATH_TAG = "(Classpath";
  /* what a jar tag looks like */
  public static final String JAR_TAG = "(Jar";
  
  /* what a source tag looks like */
  public static final char BEGIN_ENTRY = '(';
  /* what a source tag looks like */
  public static final char END_TAG = ')';
  /* what a source tag looks like */
  public static final char END_ENTRY = END_TAG;
  
  /* the char used as a path separator in the project file */
  public static final char SEPARATOR = '/';
  
  /**
   * @param r A reader from which a SourceTag is constructed.  It is assumed that the reader
   * is immedeately before a SourceTag in the ProjectFile.  Pathnames are relative to the location of the project file.
   * The unix separator chars are used.
   * @return An IR object representing the contents of a Source tag in the Project file the Reader was reading
   */
  public static SourceTag makeSourceTag(File projFile, Reader r) throws IOException  {
    return new SourceTagImpl(_readAllEntries(SOURCE_TAG, projFile, r));
  }
  
  /**
   * @param r A reader from which a ResourceTag is constructed.  It is assumed that the reader
   * is immedeately before a ResourceTag in the ProjectFile
   * @return An IR object representing the contents of a Resource tag in the Project file the Reader was reading
   */
  public static ResourceTag makeResourceTag(File projFile, Reader r) throws IOException {
    return new ResourceTagImpl(_readAllEntries(RESOURCE_TAG, projFile, r));
  }
  
  /**
   * @param r A reader from which a BuildDirTag is constructed.  It is assumed that the reader
   * is immedeately before a BuildDirTag in the ProjectFile
   * @return An IR object representing the contents of a BuildDir tag in the Project file the Reader was reading
   */
  public static BuildDirTag makeBuildDirTag(File projFile, Reader r) throws IOException {
    return new BuildDirTagImpl(_readAllEntries(BUILDDIR_TAG, projFile, r));
  } 
  
  /**
   * @param r A reader from which a ClasspathTag is constructed.  It is assumed that the reader
   * is immedeately before a ClasspathTag in the ProjectFile
   * @return An IR object representing the contents of a Classpath tag in the Project file the Reader was reading
   */
  public static ClasspathTag makeClasspathTag(File projFile, Reader r) throws IOException {
    return new ClasspathTagImpl(_readAllEntries(CLASSPATH_TAG, null, r));
  }
  
  /**
   * @param r A reader from which a JarTag is constructed.  It is assumed that the reader
   * is immedeately before a JarTag in the ProjectFile
   * @return An IR object representing the contents of a Jar tag in the Project file the Reader was reading
   */
  public static JarTag makeJarTag(File projFile, Reader r) throws IOException {
    return new JarTagImpl(_readAllEntries(JAR_TAG, null, r));
  }
  
  /**
   * @return A collection of File objects representing the entries inside the tag tag
   * @param tag The String which is the tag
   * @param projFile The File representing the project file's location
   * @param r The Reader from which input is read
   */
  private static ArrayList<File> _readAllEntries(String tag, File projFile, Reader r) throws IOException {
    ArrayList<File> files = new ArrayList<File>();
    File relPath = null;
    if( projFile != null ) {
      relPath = new File(projFile.getPath());
    }
    File f = null;
    
    /* read (Source */
    _readBeginTag(r, tag);
    
    /* read one (<file path>) entry, this returns null when there are no more entries to read */
    f = _readEntry(relPath, r);
    while(f != null) {
      files.add(f);
      f = _readEntry(relPath, r);
    }
    
    return files;
  }
  
  /**
   * Read chars from the Reader r until we have matched all the characters in tag, ignoring whitespace in the file.
   * @param r
   * @param tag
   */
  private static void _readBeginTag(Reader r, String tag) throws IOException {
    if( tag == null || tag.length() == 0 ) {
      throw new IllegalArgumentException("Tag string " + tag + " cannot be matched against while parsing project file.");
    }
    
    _munchWhiteSpaceUntil(r, tag.charAt(0));
    
    int ch =  r.read();
    /* _munchWhiteSpaceUntil already matched tag.charAt(0) so start at second char */
    for(int i = 1; i < tag.length(); i++) {     
      if( ch != tag.charAt(i) ) {
        throw new MalformedProjectFileException("Expected tag " + tag + " but did not find it");
      }
      ch =  r.read();
    }
  }
  
  /**
   * @return a File representing the entry read from the project file at this point, ignoring whitespace
   * @param r the Reader to read from
   */
  private static File _readEntry(File projFile, Reader r) throws IOException {
    String str = "";
    
    /* if we encouter the end tag then there weren't any more entries and we ought to return null to signify this */
    if( !_munchWhiteSpaceUntil(r,BEGIN_ENTRY) ) {
      return null;
    }
    
    int ch = r.read();
    while( ch != END_ENTRY ) {     
      if( ch == -1 ) {
        throw new MalformedProjectFileException("Unexpected end of file encountered while parsing project file");
      }
      str += (char)ch;
      ch = r.read();
    } 
    return new File(projFile, _replaceSeparatorChar(str));
  }
  
  /**
   * @return a copy of the String str with its separator char's replaced with the actual OS specific char
   */
  private static String _replaceSeparatorChar(String str) {
    String retStr = "";
    
    for(int i = 0; i < str.length(); i++) {
      if( str.charAt(i) == SEPARATOR ) {
        retStr += File.separatorChar;
      }
      else {
        retStr += str.charAt(i);
      }
    }
    
    return retStr;
  }
  
  /**
   * Read chars until we reach the end of a tag
   * @param r
   */
  private static void _readEndTag(Reader r) throws IOException {
    _munchWhiteSpaceUntil(r,END_TAG);
  }
  
  /**
   * eat whitespace chars until we reach the char c, throw an exception if
   * @param r The Reader to read from
   * @param c The character at which to cease reading
   * @exception MalformedProjectFileException if c is not found or non-whitespace is encountered
   */
  private static boolean _munchWhiteSpaceUntil(Reader r, char c) throws IOException {
    
    int ch = r.read();
    
    while( ch != c ) {     
      if( ch == -1 ) {
        throw new MalformedProjectFileException("End of file reached while searching for char " + c + " in the project file");
      }
      /* return false if we encounter the end tag, this signifies that there are no more entries */
      if( (char)ch == END_TAG ) {
        return false;
      }
      if( !_isWhiteSpace((char)ch) ) {
        throw new MalformedProjectFileException("Non-whitespace character encountered while searching for character " + c + " in the Project file");
      }
      ch = (char)r.read();
    }
    return true;
  }
  
  /**
   * @return true if c is whitespace, false if not
   * @param c
   */
  private static boolean _isWhiteSpace(char c) {
    if( c == ' ' || c == '\t' || c == '\n' || c == '\r' )
      return true;
    else
      return false;
  }
}
