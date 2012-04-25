/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

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

package edu.rice.cs.plt.io;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

import edu.rice.cs.plt.debug.ThreadSnapshot;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.ReadOnlyIterator;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.lambda.*;
import edu.rice.cs.plt.tuple.*;
import edu.rice.cs.plt.recur.RecursionStack;
import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.plt.text.TextUtil;

import static edu.rice.cs.plt.debug.DebugUtil.error;

/**
 * Provides additional operations on {@link File}s, {@link InputStream}s, {@link OutputStream}s,
 * {@link Reader}s, and {@link Writer}s not defined in the {@code java.io} package.
 */
public final class IOUtil {
  
  /** Prevents instance creation */
  private IOUtil() {}
  
  /**
   * The current working directory, used as the base for relative paths.  Based on System property {@code user.dir},
   * if defined, converted to an absolute path.
   */
  public static final File WORKING_DIRECTORY = IOUtil.attemptAbsoluteFile(new File(System.getProperty("user.dir", "")));
  
  /** A factory for Files based on a String filename. */
  public static final Lambda<String, File> FILE_FACTORY = new FileFactory();
  
  private static class FileFactory implements Lambda<String, File>, Serializable {
    private FileFactory() {}
    public File value(String name) { return new File(name); }
  };
  
  /**
   * Make a best attempt at evaluating {@link File#getAbsoluteFile()}.  In the event of a
   * {@link SecurityException}, the result is just {@code f}.  (Clients <em>cannot</em>
   * assume, then, that the result is absolute.)
   */
  public static File attemptAbsoluteFile(File f) {
    try { return f.getAbsoluteFile(); }
    catch (SecurityException e) { return f; }
  }
  
  /**
   * Apply {@link File#getAbsoluteFile} to all files in a list
   * @throws SecurityException  If any of the {@code getAbsoluteFile()} invocations triggers
   *                            a {@code SecurityException}
   */
  public static SizedIterable<File> getAbsoluteFiles(Iterable<? extends File> files) {
    return IterUtil.mapSnapshot(files, GET_ABSOLUTE_FILE);
  }
  
  private static final Lambda<File, File> GET_ABSOLUTE_FILE = new Lambda<File, File>() {
    public File value(File arg) { return arg.getAbsoluteFile(); }
  };
  
  /** Apply {@link #attemptAbsoluteFile} to all files in a list */
  public static SizedIterable<File> attemptAbsoluteFiles(Iterable<? extends File> files) {
    return IterUtil.mapSnapshot(files, ATTEMPT_ABSOLUTE_FILE);
  }
  
  private static final Lambda<File, File> ATTEMPT_ABSOLUTE_FILE = new Lambda<File, File>() {
    public File value(File arg) { return attemptAbsoluteFile(arg); }
  };
  
  /**
   * Make a best attempt at evaluating {@link File#getCanonicalFile()}.  In the event of an
   * {@link IOException} or a {@link SecurityException}, the result is instead the result of
   * {@link #attemptAbsoluteFile}.  (Clients <em>cannot</em> assume, then, that the result 
   * is canonical.)
   */
  public static File attemptCanonicalFile(File f) {
    try { return f.getCanonicalFile(); }
    catch (IOException e) { return attemptAbsoluteFile(f); }
    catch (SecurityException e) { return attemptAbsoluteFile(f); }
  }
  
  /**
   * Apply {@link File#getCanonicalFile} to all files in a list
   * @throws IOException  If any of the {@code getCanonicalFile()} invocations triggers
   *                      an {@code IOException}
   * @throws SecurityException  If any of the {@code getCanonicalFile()} invocations triggers
   *                            a {@code SecurityException}
   */
  public static SizedIterable<File> getCanonicalFiles(Iterable<? extends File> files) throws IOException {
    try { return IterUtil.mapSnapshot(files, GET_CANONICAL_FILE); }
    catch (WrappedException e) { 
      if (e.getCause() instanceof IOException) { throw (IOException) e.getCause(); }
      else { throw e; }
    }
  }
  
  private static final Lambda<File, File> GET_CANONICAL_FILE = new Lambda<File, File>() {
    public File value(File arg) {
      try { return arg.getCanonicalFile(); }
      catch (IOException e) { throw new WrappedException(e); }
    }
  };
  
  /** Apply {@link #attemptCanonicalFile} to all files in a list */
  public static SizedIterable<File> attemptCanonicalFiles(Iterable<? extends File> files) {
    return IterUtil.mapSnapshot(files, ATTEMPT_CANONICAL_FILE);
  }
  
  private static final Lambda<File, File> ATTEMPT_CANONICAL_FILE = new Lambda<File, File>() {
    public File value(File arg) { return attemptAbsoluteFile(arg); }
  };
    
  /**
   * Make a best attempt at evaluating {@link File#canRead()}.  In the event of a
   * {@link SecurityException}, the result is {@code false}.
   */
  public static boolean attemptCanRead(File f) {
    try { return f.canRead(); }
    catch (SecurityException e) { return false; }
  }
  
  /**
   * Make a best attempt at evaluating {@link File#canWrite()}.  In the event of a
   * {@link SecurityException}, the result is {@code false}.
   */
  public static boolean attemptCanWrite(File f) {
    try { return f.canWrite(); }
    catch (SecurityException e) { return false; }
  }
  
  /**
   * Make a best attempt at evaluating {@link File#exists()}.  In the event of a
   * {@link SecurityException}, the result is {@code false}.
   */
  public static boolean attemptExists(File f) {
    try { return f.exists(); }
    catch (SecurityException e) { return false; }
  }
  
  /**
   * Make a best attempt at evaluating {@link File#isDirectory()}.  In the event of a
   * {@link SecurityException}, the result is {@code false}.
   */
  public static boolean attemptIsDirectory(File f) {
    try { return f.isDirectory(); }
    catch (SecurityException e) { return false; }
  }
  
  /**
   * Make a best attempt at evaluating {@link File#isFile()}.  In the event of a
   * {@link SecurityException}, the result is {@code false}.
   */
  public static boolean attemptIsFile(File f) {
    try { return f.isFile(); }
    catch (SecurityException e) { return false; }
  }
  
  /**
   * Make a best attempt at evaluating {@link File#isHidden()}.  In the event of a
   * {@link SecurityException}, the result is {@code false}.
   */
  public static boolean attemptIsHidden(File f) {
    try { return f.isHidden(); }
    catch (SecurityException e) { return false; }
  }
  
  /**
   * Make a best attempt at evaluating {@link File#lastModified()}.  In the event of a
   * {@link SecurityException}, the result is {@code 0l}.
   */
  public static long attemptLastModified(File f) {
    try { return f.lastModified(); }
    catch (SecurityException e) { return 0l; }
  }
  
  /**
   * Make a best attempt at evaluating {@link File#length()}.  In the event of a
   * {@link SecurityException}, the result is {@code 0l}.
   */
  public static long attemptLength(File f) {
    try { return f.length(); }
    catch (SecurityException e) { return 0l; }
  }
  
  /**
   * Make a best attempt at invoking {@link File#createNewFile()}.  In the event of an
   * {@link IOException} or a {@link SecurityException}, the result is {@code false}.
   */
  public static boolean attemptCreateNewFile(File f) {
    try { return f.createNewFile(); }
    catch (IOException e) { return false; }
    catch (SecurityException e) { return false; }
  }
  
  /**
   * Make a best attempt at invoking {@link File#delete()}.  In the event of a 
   * {@link SecurityException}, the result is {@code false}.
   */
  public static boolean attemptDelete(File f) {
    try { return f.delete(); }
    catch (SecurityException e) { return false; }
  }
  
  /**
   * Make a best attempt at invoking {@link File#deleteOnExit()}.  Any resulting 
   * {@link SecurityException} will be ignored.
   */
  public static void attemptDeleteOnExit(File f) {
    try { f.deleteOnExit(); }
    catch (SecurityException e) { /* ignore */ }
  }
  
  /**
   * Make a best attempt at invoking {@link File#listFiles()}.  In the event of a 
   * {@link SecurityException}, the result is {@code null}.
   */
  public static File[] attemptListFiles(File f) {
    try { return f.listFiles(); }
    catch (SecurityException e) { return null; }
  }
  
  /**
   * Make a best attempt at invoking {@link File#listFiles(FileFilter)}.  In the event of a 
   * {@link SecurityException}, the result is {@code null}.
   */
  public static File[] attemptListFiles(File f, FileFilter filter) {
    try { return f.listFiles(filter); }
    catch (SecurityException e) { return null; }
  }
  
  /**
   * Make a best attempt at invoking {@link File#listFiles(FileFilter)}.  In the event of a 
   * {@link SecurityException}, the result is {@code null}.  The given predicate is converted
   * to a {@code FileFilter}.
   */
  public static File[] attemptListFiles(File f, Predicate<? super File> filter) {
    return attemptListFiles(f, (FileFilter) asFilePredicate(filter));
  }
  
  /**
   * Make a best attempt at invoking {@link File#listFiles(FileFilter)}.  In the event of a 
   * {@link SecurityException}, the result is {@code null}.  (Defined to resolve method
   * ambiguity when a FilePredicate is used.)
   */
  public static File[] attemptListFiles(File f, FilePredicate filter) {
    return attemptListFiles(f, (FileFilter) filter);
  }
  
  /**
   * Similar to {@link #attemptListFiles(File)}, but returns a non-null {@code Iterable}
   * rather than an array.  Where {@code attemptListFiles(f)} returns {@code null},
   * the result here is an empty iterable.
   */
  public static SizedIterable<File> attemptListFilesAsIterable(File f) {
    File[] result = attemptListFiles(f);
    if (result == null) { return IterUtil.empty(); }
    else { return IterUtil.asIterable(result); }
  }
  
  /**
   * Similar to {@link #attemptListFiles(File, FileFilter)}, but returns a non-null {@code Iterable}
   * rather than an array.  Where {@code attemptListFiles(f)} returns {@code null},
   * the result here is an empty iterable.
   */
  public static SizedIterable<File> attemptListFilesAsIterable(File f, FileFilter filter) {
    File[] result = attemptListFiles(f, filter);
    if (result == null) { return IterUtil.empty(); }
    else { return IterUtil.asIterable(result); }
  }
  
  /**
   * Similar to {@link #attemptListFiles(File, FileFilter)}, but returns a non-null {@code Iterable}
   * rather than an array.  Where {@code attemptListFiles(f)} returns {@code null},
   * the result here is an empty iterable.  The given predicate is converted to a {@code FileFilter}.
   */
  public static SizedIterable<File> attemptListFilesAsIterable(File f, Predicate<? super File> filter) {
    return attemptListFilesAsIterable(f, (FileFilter) asFilePredicate(filter));
  }
  
  /**
   * Similar to {@link #attemptListFiles(File, FileFilter)}, but returns a non-null {@code Iterable}
   * rather than an array.  Where {@code attemptListFiles(f)} returns {@code null},
   * the result here is an empty iterable.  (Defined to resolve method ambiguity when called with
   * a FilePredicate.)
   */
  public static SizedIterable<File> attemptListFilesAsIterable(File f, FilePredicate filter) {
    return attemptListFilesAsIterable(f, (FileFilter) filter);
  }
  
  /**
   * Make a best attempt at invoking {@link File#mkdir()}.  In the event of a 
   * {@link SecurityException}, the result is {@code false}.
   */
  public static boolean attemptMkdir(File f) {
    try { return f.mkdir(); }
    catch (SecurityException e) { return false; }
  }
  
  /**
   * Make a best attempt at invoking {@link File#mkdirs()}.  In the event of a 
   * {@link SecurityException}, the result is {@code false}.
   */
  public static boolean attemptMkdirs(File f) {
    try { return f.mkdirs(); }
    catch (SecurityException e) { return false; }
  }
  
  /**
   * Make a best attempt at invoking {@link File#renameTo}.  In the event of a 
   * {@link SecurityException}, the result is {@code false}.
   */
  public static boolean attemptRenameTo(File f, File dest) {
    try { return f.renameTo(dest); }
    catch (SecurityException e) { return false; }
  }
  
  /**
   * Like {@link #attemptRenameTo}, makes a best attempt at renaming {@code f}.
   * Before doing so, however, {@code dest}, if it exists, is deleted.  (On some systems,
   * such as Windows, the rename will otherwise fail.)
   */
  public static boolean attemptMove(File f, File dest) {
    attemptDelete(dest);
    return attemptRenameTo(f, dest);
  }
  
  /**
   * Make a best attempt at invoking {@link File#setLastModified}.  In the event of a 
   * {@link SecurityException}, the result is {@code false}.
   */
  public static boolean attemptSetLastModified(File f, long time) {
    try { return f.setLastModified(time); }
    catch (SecurityException e) { return false; }
  }
  
  /**
   * Make a best attempt at invoking {@link File#setReadOnly}.  In the event of a 
   * {@link SecurityException}, the result is {@code false}.
   */
  public static boolean attemptSetReadOnly(File f) {
    try { return f.setReadOnly(); }
    catch (SecurityException e) { return false; }
  }
  
  /**
   * Converts a {@code File} to all lowercase in case-insensitive systems; otherwise, returns the file
   * untouched.  This provides a basis for comparing files in case-insensitive file systems.
   * Note that a canonical file (see {@link File#getCanonicalFile} and {@link #attemptCanonicalFile})
   * is not necessarily in canonical case, and applying this function to a canonical file may
   * create a file that is no longer canonical.  Where the file doesn't exist, it's even possible
   * that two <em>different</em> canonical files have the same canonical case representation.
   */
  public static File canonicalCase(File f) {
    File lowered = new File(f.getPath().toLowerCase());
    if (f.equals(lowered)) { return lowered; }
    else { return f; }
  }
  
  /** Apply {@link #canonicalCase} to all files in a list */
  public static SizedIterable<File> canonicalCases(Iterable<? extends File> files) {
    return IterUtil.mapSnapshot(files, CANONICAL_CASE);
  }
  
  private static final Lambda<File, File> CANONICAL_CASE = new Lambda<File, File>() {
    public File value(File arg) { return canonicalCase(arg); }
  };
  
  /**
   * Determine if the given file is a member (as determined by {@link File#getParentFile})
   * of another file.  If {@code f.equals(ancestor)}, the result is {@code true}.
   */
  public static boolean isMember(File f, File ancestor) {
    File parent = f;
    while (parent != null) {
      if (parent.equals(ancestor)) { return true; }
      parent = parent.getParentFile();
    }
    return false;
  }
  
  /**
   * Create a list of files representing the full path of {@code f}.  This list
   * has at least one element -- {@code f} -- and contains all files that are
   * produced by repeated invocations of {@link File#getParentFile()}.  The 
   * outermost file appears first in the list, with {@code f} appearing last.
   */
  public static SizedIterable<File> fullPath(File f) {
    SizedIterable<File> result = IterUtil.singleton(f);
    File parent = f.getParentFile();
    while (parent != null) {
      result = IterUtil.compose(parent, result);
      parent = parent.getParentFile();
    }
    return result;
  }
  
  /**
   * Attempt to delete the given {@code File}.  If {@code f} is a directory, this method will first
   * recur on each of {@code f}'s members.  In all cases, {@link #attemptDelete} will be invoked on
   * {@code f}, and the result of this method will be identical to that result.
   */
  public static boolean deleteRecursively(File f) {
    return deleteRecursively(f, new RecursionStack<File>(Wrapper.<File>factory()));
  }
  
  /** Helper method for {@link #deleteRecursively(File)} */
  private static boolean deleteRecursively(File f, final RecursionStack<File> stack) {
    if (f.isDirectory()) {
      try {
        final File canonicalF = f.getCanonicalFile();
        Runnable deleteMembers = new Runnable() {
          public void run() {
            for (File child : attemptListFilesAsIterable(canonicalF)) { deleteRecursively(child, stack); }
          }
        };
        stack.run(deleteMembers, canonicalF);
      }
      catch (IOException e) { /* ignore -- don't delete files */ }
      catch (SecurityException e) { /* ignore -- don't delete files */ }
    }
    return attemptDelete(f);
  }
  
  /**
   * Attempt to mark the given {@code File} for deletion.  If {@code f} is a directory, this method 
   * will also recur on each of {@code f}'s members.  In all cases, {@link #attemptDeleteOnExit} will 
   * be invoked on {@code f}.  (Note that {@code f} may not actually be deleted on exit if some of its 
   * contents change after invoking this method, or if an error occurs in listing and marking its members 
   * for deletion.)
   */
  public static void deleteOnExitRecursively(File f) {
    deleteOnExitRecursively(f, new RecursionStack<File>(Wrapper.<File>factory()));
  }
  
  /** Helper method for {@link #deleteRecursively(File)} */
  private static void deleteOnExitRecursively(File f, final RecursionStack<File> stack) {
    attemptDeleteOnExit(f);
    if (f.isDirectory()) {
      /* This recursive walk has to be done AFTER calling deleteOnExit on the directory itself because 
         Java closes the list of files to deleted on exit in reverse order. */
      try {
        final File canonicalF = f.getCanonicalFile();
        Runnable markMembers = new Runnable() {
          public void run() {
            for (File child : attemptListFilesAsIterable(canonicalF)) { deleteOnExitRecursively(child, stack); }
          }
        };
        stack.run(markMembers, canonicalF);
      }
      catch (IOException e) { /* ignore -- don't mark files */ }
      catch (SecurityException e) { /* ignore -- don't mark files */ }
    }
  }
  
  /** 
   * Produce a list of the recursive contents of a file.  The result is a list beginning with
   * {@code f}, followed (if {@code f} is a directory with a canonical path) by a recursive 
   * listing of each of the files belonging to {@code f}.  The recursion will halt cleanly in 
   * the presence of loops in the system.  If an error occurs in listing a directory, that
   * directory will be skipped.
   * 
   * @param f  A file (generally a directory) to be listed recursively
   */
  public static SizedIterable<File> listFilesRecursively(File f) {
    return listFilesRecursively(f, ALWAYS_ACCEPT, ALWAYS_ACCEPT);
  }
  
  /** 
   * Produce a list of the recursive contents of a file.  The result is a list beginning with
   * {@code f}, followed (if {@code f} is a directory with a canonical path}) by a recursive 
   * listing of each of the files belonging to {@code f}.  The recursion will halt cleanly in 
   * the presence of loops in the system.  If an error occurs in listing a directory, that
   * directory will be skipped.
   * 
   * @param f  A file (generally a directory) to be listed recursively
   * @param filter  A filter for the list -- files that do not match will not be included
   *                (but directories that do not match will still be traversed)
   */
  public static SizedIterable<File> listFilesRecursively(File f, FileFilter filter) {
    return listFilesRecursively(f, filter, ALWAYS_ACCEPT);
  }
  
  /** 
   * Produce a list of the recursive contents of a file.  The result is a list beginning with
   * {@code f}, followed (if {@code f} is a directory with a canonical path}) by a recursive 
   * listing of each of the files belonging to {@code f}.  The recursion will halt cleanly in 
   * the presence of loops in the system.  If an error occurs in listing a directory, that
   * directory will be skipped.
   * 
   * @param f  A file (generally a directory) to be listed recursively
   * @param filter  A filter for the list -- files that do not match will not be included
   *                (but directories that do not match will still be traversed)
   */
  public static SizedIterable<File> listFilesRecursively(File f, Predicate<? super File> filter) {
    return listFilesRecursively(f, asFilePredicate(filter), ALWAYS_ACCEPT);
  }
  
  /** 
   * Produce a list of the recursive contents of a file.  The result is a list beginning with
   * {@code f}, followed (if {@code f} is a directory with a canonical path}) by a recursive 
   * listing of each of the files belonging to {@code f}.  The recursion will halt cleanly in 
   * the presence of loops in the system.  If an error occurs in listing a directory, that
   * directory will be skipped.  (Defined to resolve method ambiguity when a FilePredicate is
   * used.)
   * 
   * @param f  A file (generally a directory) to be listed recursively
   * @param filter  A filter for the list -- files that do not match will not be included
   *                (but directories that do not match will still be traversed)
   */
  public static SizedIterable<File> listFilesRecursively(File f, FilePredicate filter) {
    return listFilesRecursively(f, filter, ALWAYS_ACCEPT);
  }
  
  /** 
   * Produce a list of the recursive contents of a file.  The result is a list beginning with
   * {@code f}, followed (if {@code f} is a directory with a canonical path}) by a recursive 
   * listing of each of the files belonging to {@code f}.  The recursion will halt cleanly in 
   * the presence of loops in the system.
   * 
   * @param f  A file (generally a directory) to be listed recursively
   * @param filter  A filter for the list -- files that do not match will not be included
   *                (but directories that do not match will still be traversed)
   * @param recursionFilter  A filter controlling recursion -- directories that are rejected will
   *                         not be traversed.
   */
  public static SizedIterable<File> listFilesRecursively(File f, FileFilter filter, FileFilter recursionFilter) {
    return listFilesRecursively(f, filter, recursionFilter, new RecursionStack<File>(Wrapper.<File>factory()));
  }
  
  /** 
   * Produce a list of the recursive contents of a file.  The result is a list beginning with
   * {@code f}, followed (if {@code f} is a directory with a canonical path}) by a recursive 
   * listing of each of the files belonging to {@code f}.  The recursion will halt cleanly in 
   * the presence of loops in the system.
   * 
   * @param f  A file (generally a directory) to be listed recursively
   * @param filter  A filter for the list -- files that do not match will not be included
   *                (but directories that do not match will still be traversed)
   * @param recursionFilter  A filter controlling recursion -- directories that are rejected will
   *                         not be traversed.
   */
  public static SizedIterable<File> listFilesRecursively(File f, Predicate<? super File> filter, 
                                                         Predicate<? super File> recursionFilter) {
    return listFilesRecursively(f, asFilePredicate(filter), asFilePredicate(recursionFilter),
                                new RecursionStack<File>(Wrapper.<File>factory()));
  }
  
  /** 
   * Produce a list of the recursive contents of a file.  The result is a list beginning with
   * {@code f}, followed (if {@code f} is a directory with a canonical path}) by a recursive 
   * listing of each of the files belonging to {@code f}.  The recursion will halt cleanly in 
   * the presence of loops in the system.  (Defined to resolve method ambiguity where two
   * FilePredicates are used.)
   * 
   * @param f  A file (generally a directory) to be listed recursively
   * @param filter  A filter for the list -- files that do not match will not be included
   *                (but directories that do not match will still be traversed)
   * @param recursionFilter  A filter controlling recursion -- directories that are rejected will
   *                         not be traversed.
   */
  public static SizedIterable<File> listFilesRecursively(File f, FilePredicate filter, FilePredicate recursionFilter) {
    return listFilesRecursively(f, filter, recursionFilter, new RecursionStack<File>(Wrapper.<File>factory()));
  }
  
  /** Helper method for {@code listFilesRecursively} */
  private static SizedIterable<File> listFilesRecursively(final File f, final FileFilter filter, 
                                                          final FileFilter recursionFilter, 
                                                          final RecursionStack<File> stack) {
    SizedIterable<File> result = (filter.accept(f)) ? IterUtil.singleton(f) : IterUtil.<File>empty();
    if (f.isDirectory() && recursionFilter.accept(f)) {
      Thunk<Iterable<File>> getMembers = new Thunk<Iterable<File>>() {
        public Iterable<File> value() {
          Iterable<File> dirFiles = IterUtil.empty();
          for (File child : attemptListFilesAsIterable(f)) {
            dirFiles = IterUtil.compose(dirFiles, listFilesRecursively(child, filter, recursionFilter, stack));
          }
          return dirFiles;
        }
      };
      try {
        result = IterUtil.compose(result, stack.apply(getMembers, IterUtil.<File>empty(), f.getCanonicalFile()));
      }
      catch (IOException e) { /* ignore -- don't include directory's files */ }
      catch (SecurityException e) { /* ignore -- don't include directory's files */ }
    }
    return result;
  }
  
  /**
   * Reads the entire contents of a file and return it as a byte array.
   * @throws  IOException  If the file does not exist or cannot be opened, or if an error occurs during reading
   * @throws  SecurityException  If read access to the file is denied
   */
  public static byte[] toByteArray(File file) throws IOException {
    FileInputStream input = new FileInputStream(file);
    try { return toByteArray(input); }
    finally { input.close(); }
  }
  
  /**
   * Reads the entire contents of a file and return it as a StringBuffer.  (We use a StringBuffer rather than a
   * StringBuilder because that is what {@link StringWriter} supports.)
   * 
   * @throws  IOException  If the file does not exist or cannot be opened, or if an error occurs during reading
   * @throws  SecurityException  If read access to the file is denied
   */
  public static StringBuffer toStringBuffer(File file) throws IOException {
    FileReader reader = new FileReader(file);
    try { return toStringBuffer(reader); }
    finally { reader.close(); }
  }
  
  /**
   * Reads the entire contents of a file and return it as a String.
   * @throws  IOException  If the file does not exist or cannot be opened, or if an error occurs during reading
   * @throws  SecurityException  If read access to the file is denied
   */
  public static String toString(File file) throws IOException {
    FileReader reader = new FileReader(file);
    try { return toString(reader); }
    finally { reader.close(); }
  }

  /**
   * Get the contents of a file as a sequence of lines, accessed via an iterator.  Lines are
   * broken as determined by {@link BufferedReader#readLine}.  The result iterator's {@code next()}
   * method throws a {@link WrappedException} wrapping an {@link IOException} if an error occurs while
   * reading.
   * @throws IOException  If the file can't be found or an error occurs when reading the first line (for lookahead)
   */  
  public static Iterator<String> readLines(File file) throws IOException {
    FileReader reader = new FileReader(file);
    return readLines(reader);
  }
  
  /**
   * Produce an Adler-32 hash for the given file.  The result is an int (32 bits).
   * @throws  IOException  If the file does not exist or cannot be opened, or if an error occurs during reading
   * @throws  SecurityException  If read access to the file is denied
   */
  public static int adler32Hash(File file) throws IOException {
    InputStream input = new FileInputStream(file);
    try { return adler32Hash(input); }
    finally { input.close(); }
  }
  
  /**
   * Produce a CRC-32 hash for the given file.  The result is an int (32 bits).
   * @throws  IOException  If the file does not exist or cannot be opened, or if an error occurs during reading
   * @throws  SecurityException  If read access to the file is denied
   */
  public static int crc32Hash(File file) throws IOException {
    InputStream input = new FileInputStream(file);
    try { return crc32Hash(input); }
    finally { input.close(); }
  }
  
  /**
   * Produce an MD5 hash for the given file.  The result is 16 bytes (128 bits) long.
   * @throws  IOException  If the file does not exist or cannot be opened, or if an error occurs during reading
   * @throws  SecurityException  If read access to the file is denied
   */
  public static byte[] md5Hash(File file) throws IOException {
    InputStream input = new FileInputStream(file);
    try { return md5Hash(input); }
    finally { input.close(); }
  }
  
  /**
   * Produce an SHA-1 hash for the given file.  The result is 20 bytes (160 bits) long.
   * @throws  IOException  If the file does not exist or cannot be opened, or if an error occurs during reading
   * @throws  SecurityException  If read access to the file is denied
   */
  public static byte[] sha1Hash(File file) throws IOException {
    InputStream input = new FileInputStream(file);
    try { return sha1Hash(input); }
    finally { input.close(); }
  }
  
  /**
   * Produce an SHA-256 hash for the given file.  The result is 32 bytes (256 bits) long.
   * @throws  IOException  If the file does not exist or cannot be opened, or if an error occurs during reading
   * @throws  SecurityException  If read access to the file is denied
   */
  public static byte[] sha256Hash(File file) throws IOException {
    InputStream input = new FileInputStream(file);
    try { return sha256Hash(input); }
    finally { input.close(); }
  }
  
  /**
   * Copies the contents of one file into another.
   * @param source the file to be copied
   * @param dest the file to be copied to
   * @throws  IOException  If one of the files does not exist or cannot be opened, or if an error 
   *                       occurs during reading or writing
   * @throws  SecurityException  If read or write access, respectively, to the file is denied
   */
  public static void copyFile(File source, File dest) throws IOException {
    FileInputStream in = new FileInputStream(source);
    try {
      FileOutputStream out = new FileOutputStream(dest);
      try { copyInputStream(in, out); }
      finally { out.close(); }
    }
    finally { in.close(); }
  }

  /**
   * Copies the contents of one file into another, using the given array as an intermediate buffer.
   * @param source  The file to be copied
   * @param dest  The file to be copied to
   * @param buffer  A buffer to use in copying
   * @throws  IOException  If one of the files does not exist or cannot be opened, or if an error 
   *                       occurs during reading or writing
   * @throws  SecurityException  If read or write access, respectively, to the file is denied
   */
  public static void copyFile(File source, File dest, byte[] buffer) throws IOException {
    FileInputStream in = new FileInputStream(source);
    try {
      FileOutputStream out = new FileOutputStream(dest);
      try { copyInputStream(in, out, buffer); }
      finally { out.close(); }
    }
    finally { in.close(); }
  }

  /**
   * Writes text to the file, overwriting whatever was there.
   * @param file  File to write to
   * @param text  Text to write
   * @throws  IOException  If the file does not exist or cannot be opened, or if an error occurs during writing
   * @throws  SecurityException  If write access to the file is denied
   */
  public static void writeStringToFile(File file, String text) throws IOException {
    writeStringToFile(file, text, false);
  }
  
  /**
   * Writes text to the file, overwriting whatever was there.  Ignores any exceptions that occur.
   * @param file  File to write to
   * @param text  Text to write
   * @return  {@code true} iff the operation succeeded without an exception.
   */
  public static boolean attemptWriteStringToFile(File file, String text) {
    try { writeStringToFile(file, text); return true; }
    catch (IOException e) { return false; }
    catch (SecurityException e) { return false; }
  }
  
  /**
   * Writes text to the file.
   * @param file  File to write to
   * @param text  Text to write
   * @param append  {@code true} iff the file should be opened in "append" mode (rather than
   *                "overwrite" mode)
   * @throws  IOException  If the file does not exist or cannot be opened, or if an error occurs during writing
   * @throws  SecurityException  If write access to the file is denied
   */
  public static void writeStringToFile(File file, String text, boolean append) throws IOException {
    FileWriter writer = new FileWriter(file, append);
    try { writer.write(text); }
    finally { writer.close(); }
  }
  
  /**
   * Writes text to the file, ignoring any exceptions that occur.
   * @param file  File to write to
   * @param text  Text to write
   * @param append  {@code true} iff the file should be opened in "append" mode (rather than
   *                "overwrite" mode)
   * @return  {@code true} iff the operation succeeded without an exception.
   */
  public static boolean attemptWriteStringToFile(File file, String text, boolean append) {
    try { writeStringToFile(file, text, append); return true; }
    catch (IOException e) { return false; }
    catch (SecurityException e) { return false; }
  }
  
  /**
   * Create a temporary file in the system temp directory (via {@link File#createTempFile(String, String)}) and 
   * immediately mark it for deletion
   * @throws IOException  If an exception occurs in {@link File#createTempFile(String, String)}
   * @throws SecurityException  If write or delete access to the system temp directory is denied
   */
  public static File createAndMarkTempFile(String prefix, String suffix) throws IOException {
    return createAndMarkTempFile(prefix, suffix, null);
  }
  
  /**
   * Create a temporary file in the specified directory (via {@link File#createTempFile(String, String, File)}) and 
   * immediately mark it for deletion
   * @throws IOException  If an exception occurs in {@link File#createTempFile(String, String, File)}
   * @throws SecurityException  If write or delete access to {@code location} is denied
   */
  public static File createAndMarkTempFile(String prefix, String suffix, File location) throws IOException {
    File result = File.createTempFile(prefix, suffix, location);
    attemptDeleteOnExit(result);
    return result;
  }
  
  /**
   * Create a temporary directory (named by {@link File#createTempFile}) and immediately mark it for 
   * deletion.  (Deletion will only actually occur if the directory is empty on exit, or if all files 
   * created in the directory are also marked for deletion.)
   * @throws IOException  If an exception occurs in {@link File#createTempFile}, or if the attempt to
   *                      create the directory is unsuccessful
   * @throws SecurityException  If write access to the system temp directory is denied
   */
  public static File createAndMarkTempDirectory(String prefix, String suffix) throws IOException {
    return createAndMarkTempDirectory(prefix, suffix, null);
  }
  
  /**
   * Create a temporary directory (named by {@link File#createTempFile}) and immediately mark it for 
   * deletion.  (Deletion will only actually occur if the directory is empty on exit, or if all files 
   * created in the directory are also marked for deletion.)
   * @throws IOException  If an exception occurs in {@link File#createTempFile}, or if the attempt to
   *                      create the directory is unsuccessful
   * @throws SecurityException  If write access to {@code location} is denied
   */
  public static File createAndMarkTempDirectory(String prefix, String suffix, File location) throws IOException {
    File result = File.createTempFile(prefix, suffix, location);
    boolean success = result.delete();
    success = success && result.mkdir();
    if (!success) { throw new IOException("Attempt to create directory failed"); }
    attemptDeleteOnExit(result);
    return result;
  }
  
  /** Parse a path string -- a list of file names separated by the system-dependent
    * path separator character (':' in Unix, ';' in Windows).  Filename strings in the path
    * are interpreted according to the {@code File} constructor.
    */
  public static SizedIterable<File> parsePath(String path) {
    String[] filenames = path.split(TextUtil.regexEscape(File.pathSeparator));
    return IterUtil.mapSnapshot(IterUtil.asIterable(filenames), FILE_FACTORY);
  }
      
  /** Produce a path string from a list of files.  Filenames in the result are delimited
    * by the system-dependent path separator character (':' in Unix, ';' in Windows).
    */
  public static String pathToString(Iterable<? extends File> path) {
    return IterUtil.toString(path, "", File.pathSeparator, "");
  }

  
  
  /**
   * Write the contents of {@code in} to {@code out}.  Processing will continue (or block) until 
   * the end of stream is reached.
   * @return  The number of chars written, or {@code -1} if the end of stream has already been reached,
   *          or {@code Integer.MAX_VALUE} if the number cannot be represented as an {@code int}.
   * @throws IOException  If an IOException occurs during reading or writing
   */
  public static int copyReader(Reader source, Writer dest) throws IOException {
    return WrappedDirectReader.makeDirect(source).readAll(dest);
  }
  
  /**
   * Write the contents of {@code in} to {@code out}, using the given buffer.  Processing will 
   * continue (or block) until the end of stream is reached.
   * @return  The number of chars written, or {@code -1} if the end of stream has already been reached,
   *          or {@code Integer.MAX_VALUE} if the number cannot be represented as an {@code int}.
   * @throws IOException  If an IOException occurs during reading or writing
   */
  public static int copyReader(Reader source, Writer dest, char[] buffer) throws IOException {
    return WrappedDirectReader.makeDirect(source).readAll(dest, buffer);
  }
  
  /**
   * Copy the given number of chars from {@code in} to {@code out}.
   * @return  The number of chars written, or {@code -1} if the end of stream has already been reached.
   *          May be less than {@code chars} if {@code source} does not provide all the requested data.
   * @throws IOException  If an IOException occurs during reading or writing
   */
  public static int writeFromReader(Reader source, Writer dest, int chars) throws IOException {
    return WrappedDirectReader.makeDirect(source).read(dest, chars);
  }
  
  /**
   * Copy the given number of chars from {@code in} to {@code out}, using the given buffer.
   * @return  The number of chars written, or {@code -1} if the end of stream has already been reached.
   *          May be less than {@code chars} if {@code source} does not provide all the requested data.
   * @throws IOException  If an IOException occurs during reading or writing
   */
  public static int writeFromReader(Reader source, Writer dest, int chars, char[] buffer) throws IOException {
    return WrappedDirectReader.makeDirect(source).read(dest, chars, buffer);
  }
  
  /**
   * Write the contents of {@code in} to {@code out}.  Processing will continue (or block) until 
   * the end of stream is reached.
   * @return  The number of bytes written, or {@code -1} if the end of stream has already been reached,
   *          or {@code Integer.MAX_VALUE} if the number cannot be represented as an {@code int}.
   * @throws IOException  If an IOException occurs during reading or writing
   */
  public static int copyInputStream(InputStream source, OutputStream dest) throws IOException {
    return WrappedDirectInputStream.makeDirect(source).readAll(dest);
  }
  
  /**
   * Write the contents of {@code in} to {@code out}, using the given buffer.  Processing will 
   * continue (or block) until the end of stream is reached.
   * @return  The number of bytes written, or {@code -1} if the end of stream has already been reached,
   *          or {@code Integer.MAX_VALUE} if the number cannot be represented as an {@code int}.
   * @throws IOException  If an IOException occurs during reading or writing
   */
  public static int copyInputStream(InputStream source, OutputStream dest, byte[] buffer) throws IOException {
    return WrappedDirectInputStream.makeDirect(source).readAll(dest, buffer);
  }
  
  /**
   * Copy the given number of bytes from {@code in} to {@code out}.
   * @return  The number of bytes written, or {@code -1} if the end of stream has already been reached.
   *          May be less than {@code bytes} if {@code source} does not provide all the requested data.
   * @throws IOException  If an IOException occurs during reading or writing
   */
  public static int writeFromInputStream(InputStream source, OutputStream dest, int bytes) throws IOException {
    return WrappedDirectInputStream.makeDirect(source).read(dest, bytes);
  }
  
  /**
   * Copy the given number of bytes from {@code in} to {@code out}, using the given buffer.
   * @return  The number of bytes written, or {@code -1} if the end of stream has already been reached.
   *          May be less than {@code bytes} if {@code source} does not provide all the requested data.
   * @throws IOException  If an IOException occurs during reading or writing
   */
  public static int writeFromInputStream(InputStream source, OutputStream dest, int bytes,
                                         byte[] buffer) throws IOException {
    return WrappedDirectInputStream.makeDirect(source).read(dest, bytes, buffer);
  }
  
  /**
   * Implementation of reader-to-writer copying for use by {@link DirectReader} and {@link DirectWriter}
   * (placed here to avoid code duplication).  The method will block until an end-of-file is reached.
   * 
   * @return  {@code -1} if the reader is at the end of file; otherwise, the number of characters read 
   *          (or, if the number is too large, {@code Integer.MAX_VALUE})
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code buffer} has size {@code 0}
   */
  protected static int doCopyReader(Reader r, Writer w, char[] buffer) throws IOException {
    if (buffer.length == 0) { throw new IllegalArgumentException(); }
    int charsRead = r.read(buffer);
    if (charsRead == -1) { return -1; }
    else {
      int totalCharsRead = 0;
      do {
        totalCharsRead += charsRead;
        if (totalCharsRead < 0) { totalCharsRead = Integer.MAX_VALUE; }
        w.write(buffer, 0, charsRead);
        charsRead = r.read(buffer);
      } while (charsRead != -1);
      return totalCharsRead;
    }
  }
  
  /**
   * Implementation of stream copying for use by {@link DirectInputStream} and {@link DirectOutputStream}
   * (placed here to avoid code duplication).  The method will block until an end-of-file is reached.
   * 
   * @return  {@code -1} if the reader is at the end of file; otherwise, the number of characters read 
   *          (or, if the number is too large, {@code Integer.MAX_VALUE})
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code buffer} has size {@code 0}
   */
  protected static int doCopyInputStream(InputStream in, OutputStream out, byte[] buffer) throws IOException {
    if (buffer.length == 0) { throw new IllegalArgumentException(); }
    int charsRead = in.read(buffer);
    if (charsRead == -1) { return -1; }
    else {
      int totalCharsRead = 0;
      do {
        totalCharsRead += charsRead;
        if (totalCharsRead < 0) { totalCharsRead = Integer.MAX_VALUE; }
        out.write(buffer, 0, charsRead);
        charsRead = in.read(buffer);
      } while (charsRead != -1);
      return totalCharsRead;
    }
  }
  
  /**
   * Implementation of reader-to-writer writing for use by {@link DirectReader} and {@link DirectWriter}
   * (placed here to avoid code duplication).
   *
   * @return  {@code -1} if this reader is at the end of file; otherwise, the number of characters read
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code buffer} has size {@code 0}
   */
  protected static int doWriteFromReader(Reader r, Writer w, int chars, char[] buffer) throws IOException {
    if (buffer.length == 0 && chars > 0) { throw new IllegalArgumentException(); }
    int charsRead = r.read(buffer, 0, (chars < buffer.length) ? chars : buffer.length);
    if (charsRead == -1) { return -1; }
    else {
      int totalCharsRead = 0;
      while (chars > 0 && charsRead > 0) {
        totalCharsRead += charsRead;
        chars -= charsRead;
        w.write(buffer, 0, charsRead);
        if (chars > 0) { charsRead = r.read(buffer, 0, (chars < buffer.length) ? chars : buffer.length); }
      }
      return totalCharsRead;
    }
  }
  
  /**
   * Implementation of stream-to-stream writing for use by {@link DirectInputStream} and 
   * {@link DirectOutputStream} (placed here to avoid code duplication).
   *
   * @return  {@code -1} if this reader is at the end of file; otherwise, the number of characters read
   * @throws IOException  If an error occurs during reading or writing
   * @throws IllegalArgumentException  If {@code buffer} has size {@code 0}
   */
  protected static int doWriteFromInputStream(InputStream in, OutputStream out, int bytes, 
                                              byte[] buffer) throws IOException {
    if (buffer.length == 0 && bytes > 0) { throw new IllegalArgumentException(); }
    int bytesRead = in.read(buffer, 0, (bytes < buffer.length) ? bytes : buffer.length);
    if (bytesRead == -1) { return -1; }
    else {
      int totalBytesRead = 0;
      while (bytes > 0 && bytesRead > 0) {
        totalBytesRead += bytesRead;
        bytes -= bytesRead;
        out.write(buffer, 0, bytesRead);
        if (bytes > 0) { bytesRead = in.read(buffer, 0, (bytes < buffer.length) ? bytes : buffer.length); }
      }
      return totalBytesRead;
    }
  }
  
  /**
   * Create a byte array with the contents of the given stream.  The method will not return
   * until an end of stream has been reached.
   */
  public static byte[] toByteArray(InputStream stream) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      copyInputStream(stream, out);
      return out.toByteArray();
    }
    finally { out.close(); }
  }

  /**
   * Create a StringBuffer with the contents of the given {@code Reader}.  The method will not return
   * until an end of stream has been reached.  (We use a StringBuffer rather than a
   * StringBuilder because that is what {@link StringWriter} supports.)
   */
  public static StringBuffer toStringBuffer(Reader r) throws IOException {
    StringWriter out = new StringWriter();
    try {
      copyReader(r, out);
      return out.getBuffer();
    }
    finally { out.close(); }
  }

  /**
   * Create a String with the contents of the given {@code Reader}.  The method will not return
   * until an end of stream has been reached.
   */
  public static String toString(Reader r) throws IOException {
    return toStringBuffer(r).toString();
  }
  
  /**
   * Get the contents of a reader as a sequence of lines, accessed via an iterator.  Lines are
   * broken as determined by {@link BufferedReader#readLine}.  The Reader is closed once iteration
   * has been completed. The result iterator's {@code next()} method throws a {@link WrappedException}
   * wrapping an {@link IOException} if an error occurs while reading.
   * @throws IOException  If an error occurs when reading the first line (for lookahead).  
   */
  public static Iterator<String> readLines(Reader r) throws IOException {
    final BufferedReader br = asBuffered(r);
    final String firstLine = br.readLine();
    if (firstLine == null) { br.close(); }
    return new ReadOnlyIterator<String>() {
      String lookahead = firstLine;
      public boolean hasNext() { return lookahead != null; }
      public String next() {
        if (lookahead == null) { throw new NoSuchElementException(); }
        try {
          String result = lookahead;
          lookahead = br.readLine();
          if (lookahead == null) { br.close(); }
          return result;
        }
        catch (IOException e) { throw new WrappedException(e); }
      }
    };
  }
  
  /**
   * Produce an Adler-32 hash for the contents of the given stream.  The result is an int (32 bits).
   * The method will not return until an end of stream has been reached.
   */
  public static int adler32Hash(InputStream stream) throws IOException {
    ChecksumOutputStream out = ChecksumOutputStream.makeAdler32();
    try {
      copyInputStream(stream, out);
      return (int) out.getValue();
    }
    finally { out.close(); }
  }
  
  /**
   * Produce a CRC-32 hash for the contents of the given stream.  The result is an int (32 bits).
   * The method will not return until an end of stream has been reached.
   */
  public static int crc32Hash(InputStream stream) throws IOException {
    ChecksumOutputStream out = ChecksumOutputStream.makeCRC32();
    try {
      copyInputStream(stream, out);
      return (int) out.getValue();
    }
    finally { out.close(); }
  }
  
  /**
   * Produce an MD5 hash for the contents of the given stream.  The result is 16 bytes (128 bits) long.
   * The method will not return until an end of stream has been reached.
   */
  public static byte[] md5Hash(InputStream stream) throws IOException {
    MessageDigestOutputStream out = MessageDigestOutputStream.makeMD5();
    try {
      copyInputStream(stream, out);
      return out.digest();
    }
    finally { out.close(); }
  }
  
  /**
   * Produce an SHA-1 hash for the contents of the given stream.  The result is 20 bytes (160 bits) long.
   * The method will not return until an end of stream has been reached.
   */
  public static byte[] sha1Hash(InputStream stream) throws IOException {
    MessageDigestOutputStream out = MessageDigestOutputStream.makeSHA1();
    try {
      copyInputStream(stream, out);
      return out.digest();
    }
    finally { out.close(); }
  }
  
  /**
   * Produce an SHA-256 hash for the contents of the given stream.  The result is 32 bytes (256 bits) long.
   * The method will not return until an end of stream has been reached.
   */
  public static byte[] sha256Hash(InputStream stream) throws IOException {
    MessageDigestOutputStream out = MessageDigestOutputStream.makeSHA256();
    try {
      copyInputStream(stream, out);
      return out.digest();
    }
    finally { out.close(); }
  }
  
  /** If {@code r} is a {@code BufferedReader}, cast it as such; otherwise, wrap it in a {@code BufferedReader}. */
  public static BufferedReader asBuffered(Reader r) {
    if (r instanceof BufferedReader) { return (BufferedReader) r; }
    else { return new BufferedReader(r); }
  }
  
  /** If {@code w} is a {@code BufferedWriter}, cast it as such; otherwise, wrap it in a {@code BufferedWriter}. */
  public static BufferedWriter asBuffered(Writer w) {
    if (w instanceof BufferedWriter) { return (BufferedWriter) w; }
    else { return new BufferedWriter(w); }
  }
  
  /**
   * If {@code in} is a {@code BufferedInputStream}, cast it as such; otherwise, wrap it in a 
   * {@code BufferedInputStream}.
   */
  public static BufferedInputStream asBuffered(InputStream in) {
    if (in instanceof BufferedInputStream) { return (BufferedInputStream) in; }
    else { return new BufferedInputStream(in); }
  }
  
  /**
   * If {@code out} is a {@code BufferedOutputStream}, cast it as such; otherwise, wrap it in a 
   * {@code BufferedOutputStream}.
   */
  public static BufferedOutputStream asBuffered(OutputStream out) {
    if (out instanceof BufferedOutputStream) { return (BufferedOutputStream) out; }
    else { return new BufferedOutputStream(out); }
  }
  
  
  private static final Thunk<List<Closeable>> TO_CLOSE = LazyThunk.make(new Thunk<List<Closeable>>() {
    public List<Closeable> value() {
      // On the first request, register a shutdown hook to clean up the list
      Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() {
          for (Closeable c : TO_CLOSE.value()) { attemptClose(c); }
        }
      });
      return new LinkedList<Closeable>();
    }
  });
  
  /**
   * Register the given resource to be closed on exit.  {@link Closeable#close} will be invoked from a
   * shutdown hook.
   */
  public static void closeOnExit(Closeable c) {
    TO_CLOSE.value().add(c);
  }
  
  /** Attempt to close the given resource, failing silently if an exception occurs. */
  public static void attemptClose(Closeable c) {
    try { c.close(); }
    catch (IOException e) { /* intentionally ignore */ }
  }
  
  /** Define a {@code FileFilter} in terms of a {@code Predicate}. */
  public static FilePredicate asFilePredicate(Predicate<? super File> p) {
    return new PredicateFilePredicate(p);
  }
  
  private static final class PredicateFilePredicate implements FilePredicate, Serializable {
    private final Predicate<? super File> _p;
    public PredicateFilePredicate(Predicate<? super File> p) { _p = p; }
    public boolean accept(File f) { return _p.contains(f); }
    public boolean contains(File f) { return _p.contains(f); }
  }
  
  /**
   * Define a {@code FilePredicate} in terms of a {@code FileFilter} (this provides access
   * to predicate operations like {@code and} and {@code or}).
   */
  public static FilePredicate asFilePredicate(FileFilter filter) {
    return new FileFilterFilePredicate(filter);
  }
  
  private static final class FileFilterFilePredicate implements FilePredicate, Serializable {
    private final FileFilter _filter;
    public FileFilterFilePredicate(FileFilter filter) { _filter = filter; }
    public boolean accept(File f) { return _filter.accept(f); }
    public boolean contains(File f) { return _filter.accept(f); }
  }
  
  /**
   * Define a {@code FilePredicate} that accepts files whose (simple) names match
   * a regular expression.
   */
  public static FilePredicate regexFilePredicate(String regex) {
    return new RegexFilePredicate(regex);
  }
  
  /**
   * Define a {@code FilePredicate} that accepts files whose (simple) names match
   * a regular expression.
   */
  public static FilePredicate regexFilePredicate(Pattern regex) {
    return new RegexFilePredicate(regex);
  }
  
  private static final class RegexFilePredicate implements FilePredicate, Serializable {
    private final Pattern _regex;
    public RegexFilePredicate(String regex) { _regex = Pattern.compile(regex); }
    public RegexFilePredicate(Pattern regex) { _regex = regex; }
    public boolean accept(File f) { return _regex.matcher(f.getName()).matches(); }
    public boolean contains(File f) { return _regex.matcher(f.getName()).matches(); }
  }
  
  /** Define a {@code FilePredicate} that accepts files whose (simple) names in the
    * canonical case (see {@link #canonicalCase}) match a regular expression.
    */
  public static FilePredicate regexCanonicalCaseFilePredicate(String regex) {
    return new RegexCanonicalCaseFilePredicate(regex);
  }
  
  private static final class RegexCanonicalCaseFilePredicate implements FilePredicate, Serializable {
    private final Pattern _regex;
    public RegexCanonicalCaseFilePredicate(String regex) { _regex = Pattern.compile(regex); }
    public RegexCanonicalCaseFilePredicate(Pattern regex) { _regex = regex; }
    public boolean accept(File f) { return _regex.matcher(canonicalCase(f).getName()).matches(); }
    public boolean contains(File f) { return _regex.matcher(canonicalCase(f).getName()).matches(); }
  }
  
  /** Define a {@code FilePredicate} that accepts file objects with the given extension (that is,
    * for extension {@code txt}, file objects whose canonical-case names (see {@link #canonicalCase}) 
    * end in {@code .txt}).
    */
  public static FilePredicate extensionFilePredicate(String extension) {
    return new RegexCanonicalCaseFilePredicate(".*\\." + canonicalCase(new File(extension)).getName());
  }
  
  /** Define a {@code FilePredicate} that only accepts files with the given name (where both names
    * are converted to the canonical case; see {@link #canonicalCase}).
    */
  public static FilePredicate sameNameFilePredicate(String name) {
    return new SamePathFilePredicate(new File(name));
  }
  
  /** Define a {@code FilePredicate} that only accepts files with the given path and name (where
    * both paths are converted to the canonical case; see {@link #canonicalCase}).  If {@code path} is
    * relative, any file with an absolute path that ends with {@code path} will be accepted.
    * Otherwise, only a file with the exact same path is accepted.
    */
  public static FilePredicate samePathFilePredicate(File path) {
    return new SamePathFilePredicate(path);
  }
  
  private static final class SamePathFilePredicate implements FilePredicate, Serializable {
    private final File _f;
    public SamePathFilePredicate(File f) { _f = canonicalCase(f); }
    public boolean accept(File f) {
      File candidate = canonicalCase(attemptAbsoluteFile(f));
      for (File compareTo = _f; compareTo != null; compareTo = compareTo.getParentFile()) {
        if (candidate == null || !compareTo.getName().equals(candidate.getName())) {
          return false;
        }
        candidate = candidate.getParentFile();
      }
      return true;
    }
    public boolean contains(File f) { return accept(f); }
  }
  
  /** Define a {@code FilePredicate} that only accepts a file with the same attributes as 
    * {@code f} (at creation time).  This is useful in detecting changes being made to a file.
    * The files' modification dates, lengths, and read/write permissions are compared.
    * @throws FileNotFoundException  If {@code f} is not a normal file, or if access to its attributes
    *                                is not available.
    */
  public static FilePredicate sameAttributesFilePredicate(File f) throws FileNotFoundException {
    return new SameAttributesFilePredicate(f);
  }
  
  private static final class SameAttributesFilePredicate implements FilePredicate, Serializable {
    private final long _lastModified;
    private final long _length;
    private final boolean _canRead;
    private final boolean _canWrite;
    
    public SameAttributesFilePredicate(File f) throws FileNotFoundException {
      try {
        if (!f.isFile()) { throw new FileNotFoundException(f + " is not a valid file"); }
        _lastModified = f.lastModified();
        if (_lastModified == 0l) {
          throw new FileNotFoundException("Can't get valid modification date for " + f);
        }
        _length = f.length();
        _canRead = f.canRead();
        _canWrite = f.canWrite();
      }
      catch (SecurityException e) { throw new FileNotFoundException(e.getMessage()); }
    }
    
    public boolean accept(File f) {
      try {
        return f.isFile() && f.lastModified() == _lastModified && f.length() == _length &&
               f.canRead() == _canRead && f.canWrite() == _canWrite;
      }
      catch (SecurityException e) { return false; }
    }
    
    public boolean contains(File f) { return accept(f); }
  }
  
  /** Define a {@code FilePredicate} that only accepts files with contents matching a CRC-32
    * hash of {@code f} (at creation time).  This is useful in detecting changes being made to
    * a file.
    * @throws IOException  If {@code f} cannot be read.
    */
  public static FilePredicate sameContentsFilePredicate(File f) throws IOException {
    return new SameContentsFilePredicate(f);
  }
  
  private static final class SameContentsFilePredicate implements FilePredicate, Serializable {
    private final long _length;
    private final int _hash;
    public SameContentsFilePredicate(File f) throws IOException {
      // as an optimization, try to check the length before using a hash, but
      // don't let failures result in an exception, since it's only an optimization
      _length = attemptLength(f);
      _hash = crc32Hash(f);
    }
    public boolean accept(File f) {
      long fLength = attemptLength(f);
      if (fLength > 0l && _length > 0l && fLength != _length) { return false; }
      try { return _hash == crc32Hash(f); }
      catch (IOException e) { return false; }
    }
    public boolean contains(File f) { return accept(f); }
  }
  
  /** A predicate that tests whether {@link #attemptIsFile} holds for a file. */
  public static final FilePredicate IS_FILE = new IsFileFilePredicate();
  
  private static final class IsFileFilePredicate implements FilePredicate, Serializable {
    public boolean accept(File f) { return attemptIsFile(f); }
    public boolean contains(File f) { return attemptIsFile(f); }
  }
  
  /** A predicate that tests whether {@link #attemptIsDirectory} holds for a file. */
  public static final FilePredicate IS_DIRECTORY = new IsDirectoryFilePredicate();
  
  private static final class IsDirectoryFilePredicate implements FilePredicate, Serializable {
    public boolean accept(File f) { return attemptIsDirectory(f); }
    public boolean contains(File f) { return attemptIsDirectory(f); }
  }
  
  /** A {@code FilePredicate} that always accepts. */
  public static final FilePredicate ALWAYS_ACCEPT = asFilePredicate(LambdaUtil.TRUE);

  /** A {@code FilePredicate} that always rejects. */
  public static final FilePredicate ALWAYS_REJECT = asFilePredicate(LambdaUtil.FALSE);

  /** Produce a conjunction of the given FileFilters. */
  public static FilePredicate and(FileFilter... filters) {
    return new AndFilePredicate(IterUtil.asIterable(filters));
  }
  
  /** Produce a conjunction of the given FileFilters. */
  public static FilePredicate and(Iterable<? extends FileFilter> filters) {
    return new AndFilePredicate(filters);
  }
  
  private static final class AndFilePredicate implements FilePredicate, Serializable {
    private final Iterable<? extends FileFilter> _filters;
    public AndFilePredicate(Iterable<? extends FileFilter> filters) { _filters = filters; }
    public boolean accept(File f) {
      for (FileFilter filter : _filters) {
        if (!filter.accept(f)) { return false; }
      }
      return true;
    }
    public boolean contains(File f) { return accept(f); }
  }
  
  /** Produce a disjunction of the given FileFilters. */
  public static FilePredicate or(FileFilter... filters) {
    return new OrFilePredicate(IterUtil.asIterable(filters));
  }
  
  /** Produce a disjunction of the given FileFilters. */
  public static FilePredicate or(Iterable<? extends FileFilter> filters) {
    return new OrFilePredicate(filters);
  }
  
  private static final class OrFilePredicate implements FilePredicate, Serializable {
    private final Iterable<? extends FileFilter> _filters;
    public OrFilePredicate(Iterable<? extends FileFilter> filters) { _filters = filters; }
    public boolean accept(File f) {
      for (FileFilter filter : _filters) {
        if (filter.accept(f)) { return true; }
      }
      return false;
    }
    public boolean contains(File f) { return accept(f); }
  }
  
  /** Produce the complement of the given FileFilter. */
  public static FilePredicate negate(FileFilter filter) {
    return new NegationFilePredicate(filter);
  }
  
  private static final class NegationFilePredicate implements FilePredicate, Serializable {
    private final FileFilter _filter;
    public NegationFilePredicate(FileFilter filter) { _filter = filter; }
    public boolean accept(File f) { return !_filter.accept(f); }
    public boolean contains(File f) { return !_filter.accept(f); }
  }
  
  /**
   * Produce a {@code FilePredicate} that acts as a "key" representing the current state of
   * the given file.  A file will match only if it has the same name, absolute path, attributes,
   * and contents as {@code f}.  This is useful for detecting background changes to a file.
   * @throws IOException  If the given file does not exist, is not a normal file, or does
   *                      not allow its attributes or contents to be accessed.
   */
  public static FilePredicate fileKey(File f) throws IOException {
    return and(samePathFilePredicate(attemptAbsoluteFile(f)),
               sameAttributesFilePredicate(f),
               sameContentsFilePredicate(f));
  }

  
  private static final LinkedList<PrintStream> SYSTEM_OUT_STACK = new LinkedList<PrintStream>();
  private static final LinkedList<PrintStream> SYSTEM_ERR_STACK = new LinkedList<PrintStream>();
  private static final LinkedList<InputStream> SYSTEM_IN_STACK = new LinkedList<InputStream>();
  
  /**
   * Replace {@code System.out} with the given stream and remember the current stream.  This call
   * should always be matched by a subsequent call to {@link #revertSystemOut}.
   */
  public static void replaceSystemOut(OutputStream substitute) {
    SYSTEM_OUT_STACK.addLast(System.out);
    if (substitute instanceof PrintStream) { System.setOut((PrintStream) substitute); }
    else { System.setOut(new PrintStream(substitute)); }
  }
  
  /**
   * Ignore subsequent writes to {@code System.out} until {@link #revertSystemOut} is called.
   * A matching revert call should be made.
   */
  public static void ignoreSystemOut() {
    replaceSystemOut(VoidOutputStream.INSTANCE);
  }
  
  /**
   * Set {@code System.out} to its value before the last call to {@link #replaceSystemOut}.  This call
   * should always follow a call to {@code replaceSystemOut()}.  Assuming all calls are properly
   * paired, and that multiple threads do not concurrently invoke these methods, the stream after this
   * call will be the stream that was replaced by {@code replaceSystemOut()}.
   */
  public static void revertSystemOut() {
    if (SYSTEM_OUT_STACK.isEmpty()) { error.logStack("Unbalanced call to revertSystemOut"); }
    else { System.setOut(SYSTEM_OUT_STACK.removeLast()); }
  }
    
  /**
   * Replace {@code System.err} with the given stream and remember the current stream.  This call
   * should always be matched by a subsequent call to {@link #revertSystemErr}.
   */
  public static void replaceSystemErr(OutputStream substitute) {
    SYSTEM_ERR_STACK.addLast(System.err);
    if (substitute instanceof PrintStream) { System.setErr((PrintStream) substitute); }
    else { System.setErr(new PrintStream(substitute)); }
  }
    
  /**
   * Ignore subsequent writes to {@code System.err} until {@link #revertSystemErr} is called.  A
   * matching revert call should be made.
   */
  public static void ignoreSystemErr() {
    replaceSystemErr(VoidOutputStream.INSTANCE);
  }
  
  /**
   * Set {@code System.err} to its value before the last call to {@link #replaceSystemErr}.  This call
   * should always follow a call to {@code replaceSystemErr()}.  Assuming all calls are properly
   * paired, and that multiple threads do not concurrently invoke these methods, the stream after this
   * call will be the stream that was replaced by {@code replaceSystemErr()}.
   */
  public static void revertSystemErr() {
    if (SYSTEM_ERR_STACK.isEmpty()) { error.logStack("Unbalanced call to revertSystemErr"); }
    else { System.setErr(SYSTEM_ERR_STACK.removeLast()); }
  }
  
  /**
   * Replace {@code System.in} with the given stream and remember the current stream.  This call
   * should always be matched by a subsequent call to {@link #revertSystemIn}.
   */
  public static void replaceSystemIn(InputStream substitute) {
    SYSTEM_IN_STACK.addLast(System.in);
    System.setIn(substitute);
  }
  
  /**
   * Set {@code System.in} to its value before the last call to {@link #replaceSystemIn}.  This call
   * should always follow a call to {@code replaceSystemIn()}.  Assuming all calls are properly
   * paired, and that multiple threads do not concurrently invoke these methods, the stream after this
   * call will be the stream that was replaced by {@code replaceSystemIn()}.
   */
  public static void revertSystemIn() {
    if (SYSTEM_IN_STACK.isEmpty()) { error.logStack("Unbalanced call to revertSystemIn"); }
    else { System.setIn(SYSTEM_IN_STACK.removeLast()); }
  }
  
  
  /**
   * Classes that are known to always be serializable.  (No guarantees can be made about subclasses of these,
   * as subclasses can introduce arbitrary fields.)
   */
  private static final Set<Class<?>> SERIALIZABLE_CLASSES = new HashSet<Class<?>>();
  static {
    // Java API classes
    SERIALIZABLE_CLASSES.add(String.class);
    SERIALIZABLE_CLASSES.add(Boolean.class);
    SERIALIZABLE_CLASSES.add(Character.class);
    SERIALIZABLE_CLASSES.add(Byte.class);
    SERIALIZABLE_CLASSES.add(Short.class);
    SERIALIZABLE_CLASSES.add(Integer.class);
    SERIALIZABLE_CLASSES.add(Long.class);
    SERIALIZABLE_CLASSES.add(Float.class);
    SERIALIZABLE_CLASSES.add(Double.class);
    SERIALIZABLE_CLASSES.add(Date.class);
    SERIALIZABLE_CLASSES.add(File.class);
    SERIALIZABLE_CLASSES.add(StackTraceElement.class);
    
    // PLT classes
    SERIALIZABLE_CLASSES.add(ThreadSnapshot.class);
    SERIALIZABLE_CLASSES.add(Null.class);
    
    // primitive arrays
    SERIALIZABLE_CLASSES.add(boolean[].class);
    SERIALIZABLE_CLASSES.add(char[].class);
    SERIALIZABLE_CLASSES.add(byte[].class);
    SERIALIZABLE_CLASSES.add(short[].class);
    SERIALIZABLE_CLASSES.add(int[].class);
    SERIALIZABLE_CLASSES.add(long[].class);
    SERIALIZABLE_CLASSES.add(float[].class);
    SERIALIZABLE_CLASSES.add(double[].class);
  }
  
  
  /**
   * Converts the given object to a form that will successfully serialize.  Typical serializable primitives like
   * null, Strings and Files are left untouched; tuples, Iterables, Throwables, and arrays are processed recursively;
   * and other types are handled by invoking {@code obj.toString()}.  Note that subsequent (or concurrent) mutation
   * of the object may prevent successful serialization.
   */
  public static Object ensureSerializable(Object obj) {
    if (obj == null) { return null; }
    else if (SERIALIZABLE_CLASSES.contains(obj.getClass())) { return obj; }
    else if (obj instanceof Object[]) { return ensureSerializable((Object[]) obj); }
    else if (obj instanceof Iterable<?>) { return ensureSerializable((Iterable<?>) obj); }
    else if (obj instanceof Throwable) { return ensureSerializable((Throwable) obj); }
    else if (obj instanceof Tuple) { return ensureSerializable((Tuple) obj); }
    else { return obj.toString(); }
  }
  
  /**
   * Convert the given (non-null) array to an array of objects that will successfully serialize.  If the type of
   * the array guarantees this property, or if none of the current elements requires conversion, returns {@code arr}
   * unchanged.  Otherwise, makes a converted copy.  Note that subsequent (or concurrent) mutation
   * of the array may prevent successful serialization.
   */
  public static Object[] ensureSerializable(Object[] arr) {
    Class<?> base = ReflectUtil.arrayBaseClass(arr.getClass());
    if (SERIALIZABLE_CLASSES.contains(base) && Modifier.isFinal(base.getModifiers())) {
      // if the base type is final and known to be safe, the array's type guarantees that it will
      // never contain non-serializable elements
      return arr;
    }
    else {
      boolean keep = true;
      Object[] result = new Object[arr.length];
      for (int i = 0; i < arr.length; i++) {
        result[i] = ensureSerializable(arr[i]);
        keep &= (result[i] == arr[i]);
      }
      return keep ? arr : result;
    }
  }
  
  /**
   * Convert the given (non-null) Iterable to a list of objects that will successfully serialize.  Discards any
   * problematic fields by copying the Iterable into a List (recursively converting the elements).  Infinite Iterables
   * are handled by truncating the list with a {@code "..."} string.  Note that subsequent (or concurrent) mutation
   * of the elements may prevent successful serialization.
   */
  public static Iterable<?> ensureSerializable(Iterable<?> iter) {
    if (IterUtil.isInfinite(iter)) { iter = IterUtil.compose(IterUtil.truncate(iter, 8), "..."); }
    // can't make an exhaustive list of types that are okay, but at least we shouldn't make a new copy when
    // the method is invoked on its own result
    boolean keep = iter.getClass().equals(ArrayList.class);
    List<Object> result = new ArrayList<Object>();
    for (Object elt : iter) {
      Object safe = ensureSerializable(elt);
      keep &= (elt == safe);
      result.add(safe);
    }
    return keep ? iter : result;
  }
  
  /**
   * Convert the given Throwable to a form that will successfully serialize.  If necessary, copies the throwable
   * into a {@link SerializableException}.  Note that subsequent (or concurrent) mutation of the cause may prevent
   * successful serialization.
   */
  public static Throwable ensureSerializable(Throwable t) {
    Throwable safeCause = (t.getCause() == null) ? null : ensureSerializable(t.getCause());
    if (t.getCause() == safeCause && isSafeThrowableClass(t.getClass())) { return t; }
    else { return new SerializableException(t, safeCause); }
  }
    
  /**
   * Convert the given Exception to a form that will successfully serialize.  If necessary, copies the exception
   * into a {@link SerializableException}.  Note that subsequent (or concurrent) mutation of the cause may prevent
   * successful serialization.
   */
  public static Exception ensureSerializable(Exception e) {
    Throwable safeCause = (e.getCause() == null) ? null : ensureSerializable(e.getCause());
    if (e.getCause() == safeCause && isSafeThrowableClass(e.getClass())) { return e; }
    else { return new SerializableException(e, safeCause); }
  }
    
  /**
   * Convert the given RuntimeException to a form that will successfully serialize.  If necessary, copies the 
   * exception into a {@link SerializableException}.  Note that subsequent (or concurrent) mutation of the cause
   * may prevent successful serialization.
   */
  public static RuntimeException ensureSerializable(RuntimeException e) {
    Throwable safeCause = (e.getCause() == null) ? null : ensureSerializable(e.getCause());
    if (e.getCause() == safeCause && isSafeThrowableClass(e.getClass())) { return e; }
    else { return new SerializableException(e, safeCause); }
  }
  
  /** Tests whether all fields in subclasses of Throwable have guaranteed serializable types. */
  private static boolean isSafeThrowableClass(Class<?> c) {
    try {
      if (!c.getMethod("getCause").getDeclaringClass().equals(Throwable.class)) {
        // getCause returns an arbitrary object; the actual value of the cause field may be hidden and unsafe.
        // A lot of API classes (InvocationTargetException, RemoteException, others listed in the Throwable
        // javadoc as having "non-standard exception chaining mechanisms") follow a convention of setting
        // Throwable.cause to null, keeping their own cause field, and overriding getCause().  This is
        // okay, but there's not a nice way to detect that convention, so such classes will be treated as
        // unsafe.  We could make a list of special cases, but that list could grow arbitrarily long...
        return false;
      }
    }
    catch (NoSuchMethodException e) { return false; }
    catch (SecurityException e) { return false; }
    
    Class<?> parent = c;
    while (!parent.equals(Throwable.class) && parent != null) {
      for (Field f : parent.getDeclaredFields()) {
        Class<?> fType = f.getType();
        if (!fType.isPrimitive() && !SERIALIZABLE_CLASSES.contains(f.getType())) { return false; }
      }
      parent = parent.getSuperclass();
    }
    return true;
  }
  
  /**
   * Apply {@code ensureSerializable()} to each of the elements of the given tuple.  If the elements
   * are unchanged and the tuple's class is known to serialize safely, returns the tuple unchanged.
   */
  public static Tuple ensureSerializable(Tuple t) {
    if (t instanceof Null) { return t; } // valid because Null is final
    else if (t instanceof Wrapper<?>) { return ensureSerializable((Wrapper<?>) t); }
    else if (t instanceof Pair<?,?>) { return ensureSerializable((Pair<?,?>) t); }
    else if (t instanceof Triple<?,?,?>) { return ensureSerializable((Triple<?,?,?>) t); }
    else if (t instanceof Quad<?,?,?,?>) { return ensureSerializable((Quad<?,?,?,?>) t); }
    else if (t instanceof Quint<?,?,?,?,?>) { return ensureSerializable((Quint<?,?,?,?,?>) t); }
    else if (t instanceof Sextet<?,?,?,?,?,?>) { return ensureSerializable((Sextet<?,?,?,?,?,?>) t); }
    else if (t instanceof Septet<?,?,?,?,?,?,?>) { return ensureSerializable((Septet<?,?,?,?,?,?,?>) t); }
    else if (t instanceof Octet<?,?,?,?,?,?,?,?>) { return ensureSerializable((Octet<?,?,?,?,?,?,?,?>) t); }
    else { throw new IllegalArgumentException("Unrecognized tuple type: " + t.getClass().getName()); }
  }
  
  /**
   * Apply {@code ensureSerializable()} to the given option value.  If the value is
   * unchanged and the option's class is known to serialize safely, returns the option unchanged.
   */
  public static Option<?> ensureSerializable(Option<?> opt) {
    if (opt instanceof Null) { return opt; } // valid because Null is final
    else if (opt instanceof Wrapper<?>) { return ensureSerializable((Wrapper<?>) opt); }
    else { throw new IllegalArgumentException("Unrecognized option type: " + opt.getClass().getName()); }
  }
  
  /**
   * Apply {@code ensureSerializable()} to the given wrapped value.  If the value is
   * unchanged and the wrapper's class is known to serialize safely, returns the wrapper unchanged.
   */
  public static Wrapper<?> ensureSerializable(Wrapper<?> w) {
    Object safeVal = ensureSerializable(w.value());
    if (w.getClass().equals(Wrapper.class) && w.value() == safeVal) { return w; }
    else { return Wrapper.make(safeVal); }
  }
  
  /**
   * Apply {@code ensureSerializable()} to each of the elements of the given tuple.  If the elements
   * are unchanged and the tuple's class is known to serialize safely, returns the tuple unchanged.
   */
  public static Pair<?,?> ensureSerializable(Pair<?,?> p) {
    Object safeFirst = ensureSerializable(p.first());
    Object safeSecond = ensureSerializable(p.second());
    if (p.getClass().equals(Pair.class) && p.first() == safeFirst && p.second() == safeSecond) { return p; }
    else { return Pair.make(safeFirst, safeSecond); }
  }
  
  /**
   * Apply {@code ensureSerializable()} to each of the elements of the given tuple.  If the elements
   * are unchanged and the tuple's class is known to serialize safely, returns the tuple unchanged.
   */
  public static Triple<?,?,?> ensureSerializable(Triple<?,?,?> t) {
    Object safeFirst = ensureSerializable(t.first());
    Object safeSecond = ensureSerializable(t.second());
    Object safeThird = ensureSerializable(t.third());
    if (t.getClass().equals(Triple.class) &&
        t.first() == safeFirst &&
        t.second() == safeSecond &&
        t.third() == safeThird) { 
      return t;
    }
    else { return Triple.make(safeFirst, safeSecond, safeThird); }
  }
  
  /**
   * Apply {@code ensureSerializable()} to each of the elements of the given tuple.  If the elements
   * are unchanged and the tuple's class is known to serialize safely, returns the tuple unchanged.
   */
  public static Quad<?,?,?,?> ensureSerializable(Quad<?,?,?,?> q) {
    Object safeFirst = ensureSerializable(q.first());
    Object safeSecond = ensureSerializable(q.second());
    Object safeThird = ensureSerializable(q.third());
    Object safeFourth = ensureSerializable(q.fourth());
    if (q.getClass().equals(Quad.class) &&
        q.first() == safeFirst &&
        q.second() == safeSecond &&
        q.third() == safeThird && 
        q.fourth() == safeFourth) { 
      return q;
    }
    else { return Quad.make(safeFirst, safeSecond, safeThird, safeFourth); }
  }
  
  /**
   * Apply {@code ensureSerializable()} to each of the elements of the given tuple.  If the elements
   * are unchanged and the tuple's class is known to serialize safely, returns the tuple unchanged.
   */
  public static Quint<?,?,?,?,?> ensureSerializable(Quint<?,?,?,?,?> q) {
    Object safeFirst = ensureSerializable(q.first());
    Object safeSecond = ensureSerializable(q.second());
    Object safeThird = ensureSerializable(q.third());
    Object safeFourth = ensureSerializable(q.fourth());
    Object safeFifth = ensureSerializable(q.fifth());
    if (q.getClass().equals(Quint.class) &&
        q.first() == safeFirst &&
        q.second() == safeSecond &&
        q.third() == safeThird && 
        q.fourth() == safeFourth && 
        q.fifth() == safeFifth) { 
      return q;
    }
    else { return Quint.make(safeFirst, safeSecond, safeThird, safeFourth, safeFifth); }
  }
  
  /**
   * Apply {@code ensureSerializable()} to each of the elements of the given tuple.  If the elements
   * are unchanged and the tuple's class is known to serialize safely, returns the tuple unchanged.
   */
  public static Sextet<?,?,?,?,?,?> ensureSerializable(Sextet<?,?,?,?,?,?> s) {
    Object safeFirst = ensureSerializable(s.first());
    Object safeSecond = ensureSerializable(s.second());
    Object safeThird = ensureSerializable(s.third());
    Object safeFourth = ensureSerializable(s.fourth());
    Object safeFifth = ensureSerializable(s.fifth());
    Object safeSixth = ensureSerializable(s.sixth());
    if (s.getClass().equals(Quint.class) &&
        s.first() == safeFirst &&
        s.second() == safeSecond &&
        s.third() == safeThird && 
        s.fourth() == safeFourth && 
        s.fifth() == safeFifth &&
        s.sixth() == safeSixth) { 
      return s;
    }
    else { return Sextet.make(safeFirst, safeSecond, safeThird, safeFourth, safeFifth, safeSixth); }
  }
  
  /**
   * Apply {@code ensureSerializable()} to each of the elements of the given tuple.  If the elements
   * are unchanged and the tuple's class is known to serialize safely, returns the tuple unchanged.
   */
  public static Septet<?,?,?,?,?,?,?> ensureSerializable(Septet<?,?,?,?,?,?,?> s) {
    Object safeFirst = ensureSerializable(s.first());
    Object safeSecond = ensureSerializable(s.second());
    Object safeThird = ensureSerializable(s.third());
    Object safeFourth = ensureSerializable(s.fourth());
    Object safeFifth = ensureSerializable(s.fifth());
    Object safeSixth = ensureSerializable(s.sixth());
    Object safeSeventh = ensureSerializable(s.seventh());
    if (s.getClass().equals(Quint.class) &&
        s.first() == safeFirst &&
        s.second() == safeSecond &&
        s.third() == safeThird && 
        s.fourth() == safeFourth && 
        s.fifth() == safeFifth &&
        s.sixth() == safeSixth &&
        s.seventh() == safeSeventh) { 
      return s;
    }
    else { return Septet.make(safeFirst, safeSecond, safeThird, safeFourth, safeFifth, safeSixth, safeSeventh); }
  }
  
  /**
   * Apply {@code ensureSerializable()} to each of the elements of the given tuple.  If the elements
   * are unchanged and the tuple's class is known to serialize safely, returns the tuple unchanged.
   */
  public static Octet<?,?,?,?,?,?,?,?> ensureSerializable(Octet<?,?,?,?,?,?,?,?> o) {
    Object safeFirst = ensureSerializable(o.first());
    Object safeSecond = ensureSerializable(o.second());
    Object safeThird = ensureSerializable(o.third());
    Object safeFourth = ensureSerializable(o.fourth());
    Object safeFifth = ensureSerializable(o.fifth());
    Object safeSixth = ensureSerializable(o.sixth());
    Object safeSeventh = ensureSerializable(o.seventh());
    Object safeEighth = ensureSerializable(o.eighth());
    if (o.getClass().equals(Quint.class) &&
        o.first() == safeFirst &&
        o.second() == safeSecond &&
        o.third() == safeThird && 
        o.fourth() == safeFourth && 
        o.fifth() == safeFifth &&
        o.sixth() == safeSixth &&
        o.seventh() == safeSeventh &&
        o.eighth() == safeEighth) { 
      return o;
    }
    else {
      return Octet.make(safeFirst, safeSecond, safeThird, safeFourth, safeFifth, safeSixth, safeSeventh, safeEighth);
    }
  }
  
}
