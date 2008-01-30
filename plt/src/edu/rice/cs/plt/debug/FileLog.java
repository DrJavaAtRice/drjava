/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2008 JavaPLT group at Rice University
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
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
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

package edu.rice.cs.plt.debug;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;

import edu.rice.cs.plt.text.TextUtil;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.LazyThunk;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.Predicate2;
import edu.rice.cs.plt.lambda.WrappedException;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.collect.TotalMap;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.concurrent.ReaderWriterLock;

/**
 * A log that writes tagged, indented text to a collection of files.  Messages may all be directed to a single file
 * or, based on the calling class and method names, directed to a requested file.  Files are created and opened
 * on-demand: when the first log message to the file occurs, it is opened.  A banner recording a time stamp is 
 * appended to the file, followed by all applicable logging messages.  Finally, files are closed 
 * on system exit.  Log requests from certain threads or code locations may be ignored completely by providing a 
 * filter predicate.
 */
public class FileLog extends TextLog {
  
  private final Set<Pair<String, File>> _files;
  private final ReaderWriterLock _filesLock;
  private final TotalMap<File, BufferedWriter> _writers;
  
  private static final Lambda<File, BufferedWriter> OPEN_WRITER = new Lambda<File, BufferedWriter>() {
    public BufferedWriter value(File f) {
      try {
        BufferedWriter result = new BufferedWriter(new FileWriter(f, true));
        IOUtil.closeOnExit(result);
        String stars = TextUtil.repeat('*', 40);
        result.write(stars);
        result.newLine();
        result.write("Opened log file " + new Date());
        result.newLine();
        result.write(stars);
        result.newLine();
        result.newLine();
        result.flush();
        return result;
      }
      catch (IOException e) { throw new WrappedException(e); }
    }
  };

  /** 
   * Create an empty file log with no filter.  Until {@link #addFile} is invoked, log messages will not be written 
   * anywhere.
   */
  public FileLog() {
    super();
    _files = new HashSet<Pair<String, File>>();
    _filesLock = new ReaderWriterLock();
    _writers = new TotalMap<File, BufferedWriter>(OPEN_WRITER, true);
  }
  
  /** 
   * Create a file log with no filter.  All logging messages will be recorded in the given file.
   * {@link #addFile} may be used to create additional log files.
   */
  public FileLog(File topLevelFile) {
    this();
    addFile(topLevelFile, "");
  }
  
  /** 
   * Create an empty, filtered file log.  Until {@link #addFile} is invoked, log messages will not be written 
   * anywhere.  Afterwards, messages that are permitted by the filter and mapped to some file will be recorded there.
   */
  public FileLog(Predicate2<? super Thread, ? super StackTraceElement> filter) {
    super(filter);
    _files = new HashSet<Pair<String, File>>();
    _filesLock = new ReaderWriterLock();
    _writers = new TotalMap<File, BufferedWriter>(OPEN_WRITER, true);
  }

  /**
   * Create a filtered file log.  All logging messages permitted by the filter will be recorded in the given file.
   * {@link #addFile} may be used to create additional log files.
   */
  public FileLog(File topLevelFile, Predicate2<? super Thread, ? super StackTraceElement> filter) {
    this(filter);
    addFile(topLevelFile, "");
  }
  
  /**
   * Define an additional mapping from a prefix (or list of prefixes) to a log file.  All unfiltered log requests 
   * that come from a class and method name matching the given prefix (that is,
   * {@code (className + "." + methodName).startsWith(prefix)}) will be recorded in the given file.  There is a 
   * many-to-many relationship between files and prefixes, and this method simply adds file-prefix pairs
   * to that relationship.  If a log message matches multiple prefixes, it will be recorded once for each matching
   * prefix (which may direct it to multiple files, or to the same file multiple times).  If a prefix is the empty 
   * string, it matches all messages.  If {@code prefixes} is empty, this method has no effect.
   */
  public void addFile(File f, String... prefixes) {
    _filesLock.startWrite();
    try {
      File canonical = IOUtil.attemptCanonicalFile(f);
      for (String p : prefixes) { _files.add(Pair.make(p, canonical)); }
    }
    finally { _filesLock.endWrite(); }
  }
  
  protected void write(Date time, Thread thread, StackTraceElement location, SizedIterable<? extends String> messages) {
    /* Synchronization strategy: multiple reads on _files are okay, but a concurrent write would cause an exception.
     * So we protect _files with a ReaderWriterLock.  Reading from _writers, on the other hand, may cause modification
     * of internal data structures, and concurrent access could cause multiple evaluations of OPEN_WRITER for the
     * same file.  So we synchronize on _writers to lookup the writer.  Writes to the file then are made atomic by
     * synchronizing on the writer object.
     */
    String caller = location.getClassName() + "." + location.getMethodName();
    _filesLock.startRead();
    try {
      for (Pair<String, File> p : _files) {
        if (caller.startsWith(p.first())) {
          BufferedWriter w;
          synchronized(_writers) { w = _writers.get(p.second()); }
          synchronized(w) { writeText(w, time, thread, location, messages); }
        }
      }
    }
    finally { _filesLock.endRead(); }
  }
    
}
