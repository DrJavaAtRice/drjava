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

package edu.rice.cs.util;

import java.io.*;
import java.util.*;

import edu.rice.cs.drjava.config.FileOption;

import edu.rice.cs.util.Log;

/** A class to provide some convenient file operations as static methods.
 *  It's abstract to prevent (useless) instantiation, though it can be subclassed
 *  to provide convenient namespace importation of its methods.
 *
 *  @version $Id$
 */
public abstract class FileOps {
  
  private static Log _log = new Log("FileOpsTest.txt", true);
  
  /** Special File object corresponding to a dummy file. Simliar to FileOption.NULL_FILE but exists() returns false. */
  public static final File NONEXISTENT_FILE = new File("") {
    public String getAbsolutePath() { return ""; }
    public String getName() { return ""; }
    public String toString() { return ""; }
    public boolean exists() { return false; }
  };
  
  /** Determines whether the specified file in within the specified file tree. */
  public static boolean inFileTree(File f, File root) {
    if (root == null || f == null) return false;
    try {
      if (! f.isDirectory()) f = f.getParentFile();
      String filePath = f.getCanonicalPath() + File.separator;
      String projectPath = root.getCanonicalPath() + File.separator;
      return (filePath.startsWith(projectPath));
    }
    catch(IOException e) { return false; }
  }
  
  
  /** Makes a file equivalent to the given file f that is relative to base file b.  In other words,
   *  <code>new File(b,makeRelativeTo(base,abs)).getCanonicalPath()</code> equals
   *  <code>f.getCanonicalPath()</code><p> 
   *  In Linux/Unix, if the file f is <code>/home/username/folder/file.java</code> and the file b is 
   *  <code>/home/username/folder/sublevel/file2.java</code>, then the resulting File path from this method would be 
   *  <code>../file.java</code> while its canoncial path would be <code>/home/username/folder/file.java</code>.</p>
   *  @param f The path that is to be made relative to the base file
   *  @param b The file to make the next file relative to
   *  @return A new file whose path is relative to the base file while the value of <code>getCanonicalPath()</code> 
   *    for the returned file is the same as the result of <code>getCanonicalPath()</code> for the given file.
   */
  public static File makeRelativeTo(File f, File b) throws IOException, SecurityException {
    File base = b.getCanonicalFile();
    File abs  = f.getCanonicalFile();  // If  f is relative, uses current working directory ("user.dir")
    if (! base.isDirectory()) base = base.getParentFile();
    
    String last = "";
    if (! abs.isDirectory()) {
      String tmp = abs.getPath();
      last = tmp.substring(tmp.lastIndexOf(File.separator) + 1);
      abs = abs.getParentFile();
    }
    
//    System.err.println("makeRelativeTo called; f = " + f + " = " + abs + "; b = " + b + " = " + base);
    String[] basParts = splitFile(base);
    String[] absParts = splitFile(abs);
    
    StringBuffer result = new StringBuffer();
    // loop until elements differ, record what part of absParts to append
    // next find out how many .. to put in.
    int diffIndex = -1;
    boolean different = false;
    for (int i = 0; i < basParts.length; i++) {
      if (!different && ((i >= absParts.length) || !basParts[i].equals(absParts[i]))) {
        different = true;
        diffIndex = i;
      }
      if (different) result.append("..").append(File.separator);
    }
    if (diffIndex < 0) diffIndex = basParts.length;
    for (int i = diffIndex; i < absParts.length; i++) { 
      result.append(absParts[i]).append(File.separator);
    }
    result.append(last);
//    System.err.println("makeRelativeTo(" + f + ", " + b + ") = " + result);
    return new File(result.toString());
  }
  
  /** Splits a file into an array of strings representing each parent folder of the given file.  The file whose path
   *  is <code>/home/username/txt.txt</code> in linux would be split into the string array: 
   *  {&quot;&quot;,&quot;home&quot;,&quot;username&quot;,&quot;txt.txt&quot;}. Delimeters are excluded.
   *  @param fileToSplit the file to split into its directories.
   */
  public static String[] splitFile(File fileToSplit) {
    String path = fileToSplit.getPath();
    ArrayList<String> list = new ArrayList<String>();
    while (! path.equals("")) {
      int idx = path.indexOf(File.separator);
      if (idx < 0) {
        list.add(path);
        path = "";
      }
      else {
        list.add(path.substring(0,idx));
        path = path.substring(idx+1);
      }
    }
    return list.toArray(new String[list.size()]);
  }
  
  /** List all files (that is, {@code File}s for which {@code isFile()} is {@code true}) matching the provided filter in
   *  the given directory.
   *  @param d  The directory to search.
   *  @param recur  Whether subdirectories accepted by {@code f} should be recursively searched.  Note that 
   *         subdirectories that <em>aren't</em> accepted by {@code f} will be ignored.
   *  @param f  The filter to apply to contained {@code File}s.
   *  @return  An array of Files in the directory specified; if the directory does not exist, returns an empty list. */
  public static ArrayList<File> getFilesInDir(File d, boolean recur, FileFilter f){
    ArrayList<File> l = new ArrayList<File>();
    getFilesInDir(d, l, recur, f);
    return l;
  }
  
  /** Helper fuction for getFilesInDir(File d, boolean recur). {@code acc} is mutated to contain
   *  a list of <c>File</c>s in the directory specified, not including directories.
   */
  private static void getFilesInDir(File d, List<File> acc, boolean recur, FileFilter filter) {
    if (d.isDirectory()) {
      File[] files = d.listFiles(filter);
      if (files!=null) { // listFiles may return null if there's an IO error
        for (File f: files) {
          if (f.isDirectory() && recur) getFilesInDir(f, acc, recur, filter);
          else if (f.isFile()) acc.add(f);
        }
      }
    }      
  }
  
  /** @return the canonical file equivalent to f.  Identical to f.getCanonicalFile() except it does not throw an 
   *  exception when the file path syntax is incorrect (or an IOException or SecurityException occurs for any
   *  other reason).  It returns the absolute File intead. */
  public static File getCanonicalFile(File f) {
    if (f == null) return f;
    try { return f.getCanonicalFile(); }
    catch (IOException e) { /* fall through */ }
    catch (SecurityException e) { /* fall through */ }
    return f.getAbsoluteFile();
  }
  
  /** @return the canonical path for f.  Identical to f.getCanonicalPath() except it does not throw an 
   *  exception when the file path syntax is incorrect; it returns the absolute path instead. */
  public static String getCanonicalPath(File f) { return getCanonicalFile(f).getPath(); }
    
  /** @return the file f unchanged if f exists; otherwise returns NULL_FILE. */
  public static File validate(File f) {
    if (f.exists()) return f;
    return FileOption.NULL_FILE;  // This File object exists
  }
  
  /** This filter checks for files with names that end in ".java". */
  public static final FileFilter JAVA_FILE_FILTER = new FileFilter() {
    public boolean accept(File f){
      // Do this runaround for filesystems that are case preserving but case insensitive. Remove the last 5 
      // letters from the file name, append ".java" to the end, create a new file and see if its equivalent 
      // to the original
      StringBuffer name = new StringBuffer(f.getAbsolutePath());
      String shortName = f.getName();
      if (shortName.length() < 6) return false;
      name.delete(name.length() - 5, name.length());
      name.append(".java");
      File test = new File(new String(name));
      return (test.equals(f));
    }
    public String getDescription() { return "Java Source Files (*.java)"; }
  };
  
  /** Reads the stream until it reaches EOF, and then returns the read contents as a byte array. This call may 
   *  block, since it will not return until EOF has been reached.
   *  @param stream Input stream to read.
   *  @return Byte array consisting of all data read from stream.
   */
  public static byte[] readStreamAsBytes(final InputStream stream) throws IOException {
    BufferedInputStream buffered;

    if (stream instanceof BufferedInputStream) buffered = (BufferedInputStream) stream;
    else  buffered = new BufferedInputStream(stream);

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    int readVal = buffered.read();
    while (readVal != -1) {
      out.write(readVal);
      readVal = buffered.read();
    }

    stream.close();
    return out.toByteArray();
  }

  /** Reads the entire contents of a file and return them as a String. */
  public static String readFileAsString(final File file) throws IOException {
    FileReader reader = new FileReader(file);
    StringBuffer buf = new StringBuffer();

    while (reader.ready()) {
      char c = (char) reader.read();
      buf.append(c);
    }

    reader.close();
    return buf.toString();
  }
  
  /** Copies the text of one file into another.
   *  @param source the file to be copied
   *  @param dest the file to be copied to
   */
  public static void copyFile(File source, File dest) throws IOException {
    String text = readFileAsString(source);
    writeStringToFile(dest, text);
  }

  /** Creates a new temporary file and writes the given text to it. The file will be deleted on exit.
   *  @param prefix Beginning part of file name, before unique number
   *  @param suffix Ending part of file name, after unique number
   *  @param text Text to write to file
   *  @return name of the temporary file that was created
   */
  public static File writeStringToNewTempFile(final String prefix, final String suffix, final String text)
    throws IOException {
    
    File file = File.createTempFile(prefix, suffix);
    file.deleteOnExit();
    writeStringToFile(file, text);
    return file;
  }

  /** Writes text to the file overwriting whatever was there.
   *  @param file File to write to
   *  @param text Test to write
   */
  public static void writeStringToFile(File file, String text) throws IOException {
    writeStringToFile(file, text, false);
  }
  
  /** Writes text to the file.
   *  @param file File to write to
   *  @param text Text to write
   *  @param append whether to append. (false=overwrite)
   */
  public static void writeStringToFile(File file, String text, boolean append) throws IOException {
    FileWriter writer = new FileWriter(file, append);
    writer.write(text);
    writer.close();
  }
  
  /** Writes the text to the given file returning true if it happend and false if it could not.  This is a 
   *  simple wrapper for writeStringToFile that doesn't throw an IOException.
   *  @param file File to write to
   *  @param text Text to write
   *  @param append whether to append. (false=overwrite)
   */
  public static boolean writeIfPossible(File file, String text, boolean append) {
    try {
      writeStringToFile(file, text, append);
      return true;
    }
    catch(IOException e) { return false; }
  }
  
  /** Create a new temporary directory. The directory will be deleted on exit, if empty.
   *  (To delete it recursively on exit, use deleteDirectoryOnExit.)
   *  @param name Non-unique portion of the name of the directory to create.
   *  @return File representing the directory that was created.
   */
  public static File createTempDirectory(final String name) throws IOException {
    return createTempDirectory(name, null);
  }

  /** Create a new temporary directory. The directory will be deleted on exit, if it only contains temp files and temp
   *  directories created after it.  (To delete it on exit regardless of contents, call deleteDirectoryOnExit after
   *  constructing the file tree rooted at this directory.  Note that createTempDirectory(..) is not much more helpful 
   *  than mkdir() in this context (other than generating a new temp file name) because cleanup is a manual process.)
   *  @param name Non-unique portion of the name of the directory to create.
   *  @param parent Parent directory to contain the new directory
   *  @return File representing the directory that was created.
   */
  public static File createTempDirectory(final String name, final File parent) throws IOException {
    File file =  File.createTempFile(name, "", parent);
    file.delete();
    file.mkdir();
    file.deleteOnExit();

    return file;
  }

  /** Delete the given directory including any files and directories it contains.
   *  @param dir File object representing directory to delete. If, for some reason, this file object is not a 
   *             directory, it will still be deleted.
   *  @return true if there were no problems in deleting. If it returns false, something failed and the directory
   *          contents likely at least partially still exist.
   */
  public static boolean deleteDirectory(final File dir) {
//    System.err.println("Deleting file or directory " + dir);
    if (! dir.isDirectory()) { 
      boolean res;
      res = dir.delete();
//      System.err.println("Deletion of " + dir + " returned " + res);
      return res;
    }

    boolean ret = true;
    File[] childFiles = dir.listFiles();
    if (childFiles!=null) { // listFiles may return null if there's an IO error
      for (File f: childFiles) { ret = ret && deleteDirectory(f); }
    }
    
    // Now we should have an empty directory
    ret = ret && dir.delete();
//    System.err.println("Recursive deletion of " + dir + " returned " + ret);
    return ret;
  }
  
  /** Instructs Java to recursively delete the given directory and its contents when the JVM exits.
   *  @param dir File object representing directory to delete. If, for some reason, this file object is not a 
   *             directory, it will still be deleted.
   */
  public static void deleteDirectoryOnExit(final File dir) {

    // Delete this on exit, whether it's a directory or file
    _log.log("Deleting file/directory " + dir + " on exit");
    dir.deleteOnExit(); 
    
    // If it's a directory, visit its children.  This recursive walk has to be done AFTER calling deleteOnExit
    //  on the directory itself because Java closes the list of files to deleted on exit in reverse order.
    if (dir.isDirectory()) {
      File[] childFiles = dir.listFiles();
      if (childFiles != null) { // listFiles may return null if there's an IO error
        for (File f: childFiles) { deleteDirectoryOnExit(f); }
      }
    }

  
  }

  /** This function starts from the given directory and finds all  packages within that directory
   *  @param prefix the package name of files in the given root
   *  @param root the directory to start exploring from
   *  @return a list of valid packages, excluding the root ("") package
   */
  public static LinkedList<String> packageExplore(String prefix, File root) {
    /* Inner holder class. */
    class PrefixAndFile {
      public String prefix;
      public File root;
      public PrefixAndFile(String prefix, File root) {
        this.root = root;
        this.prefix = prefix;
      }
    }
    
    // This set makes sure we don't get caught in a loop if the filesystem has symbolic links
    // that form a circle by tracking the directories we have already explored
    final Set<File> exploredDirectories = new HashSet<File>();

    LinkedList<String> output = new LinkedList<String>();
    Stack<PrefixAndFile> working = new Stack<PrefixAndFile>();
    working.push(new PrefixAndFile(prefix, root));
    exploredDirectories.add(root);

    // This filter allows only directories, and accepts each directory only once
    FileFilter directoryFilter = new FileFilter(){
      public boolean accept(File f){
        boolean toReturn = f.isDirectory() && !exploredDirectories.contains(f);
        exploredDirectories.add(f);
        return toReturn;
      }
      public String getDescription() { return "All Folders"; }
    };

    // Explore each directory, adding (unique) subdirectories to the working list.  If a directory has .java 
    // files, add the associated package to the list of packages
    while (! working.empty()) {
      PrefixAndFile current = working.pop();
      File [] subDirectories = current.root.listFiles(directoryFilter);
      if (subDirectories!=null) { // listFiles may return null if there's an IO error
        for (File dir: subDirectories) {
          PrefixAndFile paf;
//         System.out.println("exploring " + dir);
          if (current.prefix.equals("")) paf = new PrefixAndFile(dir.getName(), dir);
          else  paf = new PrefixAndFile(current.prefix + "." + dir.getName(), dir);
          working.push(paf);
        }
      }
      File [] javaFiles = current.root.listFiles(JAVA_FILE_FILTER);

      if (javaFiles!=null) { // listFiles may return null if there's an IO error
        //Only add package names if they have java files and are not the root package
        if (javaFiles.length != 0 && !current.prefix.equals("")) {
          output.add(current.prefix);
//         System.out.println("adding " + current.prefix);
        }
      }
    }
    return output;
  }

  /** Renames the given file to the given destination.  Needed since Windows will not allow a rename to 
   *  overwrite an existing file.
   *  @param file the file to rename
   *  @param dest the destination file
   *  @return true iff the rename was successful
   */
  public static boolean renameFile(File file, File dest) {
    if (dest.exists()) dest.delete();
    return file.renameTo(dest);
  }

  /** This method writes files correctly; it takes care of catching errors and
   *  making backups and keeping an unsuccessful file save from destroying the old
   *  file (unless a backup is made).  It makes sure that the file to be saved is 
   *  not read-only, throwing an IOException if it is.  Note: if saving fails and a 
   *  backup was being created, any existing backup will be destroyed (this is 
   *  because the backup is written before saving begins, and then moved back over 
   *  the original file when saving fails).  As the old backup would have been destroyed 
   *  anyways if saving had succeeded, I do not think that this is incorrect or 
   *  unreasonable behavior.
   *  @param fileSaver keeps track of the name of the file to write, whether to back up the file, and has 
   *                   a method that actually performs the writing of the file
   *  @throws IOException if the saving or backing up of the file fails for any reason
   */
  public static void saveFile(FileSaver fileSaver) throws IOException {
    
//    ScrollableDialog sd1 = new ScrollableDialog(null, "saveFile (" + fileSaver + ") called in FileOps.java", "", "");
//    sd1.show();
    boolean makeBackup = fileSaver.shouldBackup();
    boolean success = false;
    File file = fileSaver.getTargetFile();
    File backup = null;
    boolean tempFileUsed = true;
    // file.canWrite() is false if file.exists() is false
    // but we want to be able to save a file that doesn't
    // yet exist.
    if (file.exists() && !file.canWrite()) throw new IOException("Permission denied");
    /* First back up the file, if necessary */
    if (makeBackup) {
      backup = fileSaver.getBackupFile();
      if (!renameFile(file, backup)){
        throw new IOException("Save failed. Could not create backup file "
                                + backup.getAbsolutePath() +
                              "\nIt may be possible to save by disabling file backups\n");
      }
      fileSaver.backupDone();
    }
    
//    ScrollableDialog sd2 = new ScrollableDialog(null, "backup done in FileOps.saveFile", "", "");
//    sd2.show();
    
    //Create a temp file in the same directory as the file to be saved.
    //From this point forward, enclose in try...finally so that we can clean
    //up the temp file and restore the file from its backup.
    File parent = file.getParentFile();
    File tempFile = File.createTempFile("drjava", ".temp", parent);
    
//    ScrollableDialog sd3 = new ScrollableDialog(null, "temp file " + tempFile + "created in FileOps.saveFile", "", "");
//    sd3.show();
    
    try {
      /* Now, write your output to the temp file, then rename it to the correct
       name.  This way, if writing fails in the middle, the old file is not
       lost. */
      FileOutputStream fos;
      try {
        /* The next line will fail if we can't create the temp file.  This may mean that
         * the user does not have write permission on the directory the file they
         * are editing is in.  We may want to go ahead and try writing directly
         * to the target file in this case
         */
        fos = new FileOutputStream(tempFile);
      } 
      catch (FileNotFoundException fnfe) {
        if (fileSaver.continueWhenTempFileCreationFails()) {
          fos = new FileOutputStream(file);
          tempFileUsed = false;
        } 
        else throw new IOException("Could not create temp file " + tempFile + " in attempt to save " + file);
      }
      BufferedOutputStream bos = new BufferedOutputStream(fos);
      fileSaver.saveTo(bos);
      bos.close();
      fos.close();

      if (tempFileUsed && !renameFile(tempFile, file))
        throw new IOException("Save failed. Another process may be using " + file + ".");

      success = true;
    } 
    finally {
//      ScrollableDialog sd4 = new ScrollableDialog(null, "finally clause reached in FileOps.saveFile", "", "");
//      sd4.show();
    
      if (tempFileUsed) tempFile.delete(); /* Delete the temp file */
        
      if (makeBackup) {
        /* On failure, attempt to move the backup back to its original location if we
         made one.  On success, register that a backup was successfully made */
        if (success) fileSaver.backupDone();
        else  renameFile(backup, file);
      }
    }
  }

  public interface FileSaver {
    
    /** This method tells what to name the backup of the file, if a backup is to be made.
     *  It may depend on getTargetFile(), so it can thrown an IOException
     */
    public abstract File getBackupFile() throws IOException;
    
    /** This method indicates whether or not a backup of the file should be made.  It
     *  may depend on getTargetFile(), so it can throw an IOException
     */
    public abstract boolean shouldBackup() throws IOException;

    /** This method specifies if the saving process should continue trying to save
     *  if it can not create the temp file that is written initially.  If you do
     *  continue saving in this case, the original file may be lost if saving fails.
     */
    public abstract boolean continueWhenTempFileCreationFails();
    
    /** This method is called to tell the file saver that a backup was successfully made. */
    public abstract void backupDone();

    /**
     * This method actually writes info to a file.  NOTE: It is important that this
     * method write to the stream it is passed, not the target file.  If you write
     * directly to the target file, the target file will be destroyed if saving fails.
     * Also, it is important that when saving fails this method throw an IOException
     * @throws IOException when saving fails for any reason
     */
    public abstract void saveTo(OutputStream os) throws IOException;

    /** This method tells what the file is that we want to save to.  It should
     *  use the canonical name of the file (this means resolving symlinks).  Otherwise,
     *  the saver would not deal correctly with symlinks.  Resolving symlinks may cause
     *  an IOException, so this method throws an IOException.
     */
    public abstract File getTargetFile() throws IOException;
  }

  /** This class is a default implementation of FileSaver that makes only 1 backup
   *  of each file per instantiation of the program (following Emacs' lead).  It 
   *  backs up to files named <file>~.  It does not implement the saveTo method.
   */
  public abstract static class DefaultFileSaver implements FileSaver {

    private File outputFile = null;
    private static Set<File> filesNotNeedingBackup = new HashSet<File>();
    private static boolean backupsEnabled = true;

    /** This field keeps track of whether or not outputFile has been resolved to its canonical name. */
    private boolean isCanonical = false;
    
    /** Globally enables backups for any DefaultFileSaver that does not override the shouldBackup method. */
    public static void setBackupsEnabled(boolean isEnabled) { backupsEnabled = isEnabled; }
    
    public DefaultFileSaver(File file){ outputFile = file.getAbsoluteFile(); }
    
    public boolean continueWhenTempFileCreationFails(){ return true; }
    
    public File getBackupFile() throws IOException{ return new File(getTargetFile().getPath() + "~"); }

    public boolean shouldBackup() throws IOException{
      if (!backupsEnabled) return false;
      if (!getTargetFile().exists()) return false;
      if (filesNotNeedingBackup.contains(getTargetFile())) return false;
      return true;
    }
    
    public void backupDone() {
      try { filesNotNeedingBackup.add(getTargetFile()); } 
      catch (IOException ioe) { throw new UnexpectedException(ioe, "getTargetFile should fail earlier"); }
    }

    public File getTargetFile() throws IOException{
      if (!isCanonical) {
        outputFile = outputFile.getCanonicalFile();
        isCanonical = true;
      }
      return outputFile;
    }
  }
  
  /** Convert all path entries in a path string to absolute paths. The delimiter in the path string is the 
   *  "path.separator" property.  Empty entries are equivalent to "." and will thus are converted to the 
   *  "user.dir" (working directory).
   *  Example:
   *    ".:drjava::/home/foo/junit.jar" with "user.dir" set to "/home/foo/bar" will be converted to 
   *    "/home/foo/bar:/home/foo/bar/drjava:/home/foo/bar:/home/foo/junit.jar".
   * 
   *  @param path path string with entries to convert
   *  @return path string with all entries as absolute paths
   */
  public static String convertToAbsolutePathEntries(String path) {
    String pathSep = System.getProperty("path.separator");
    
    // split leaves off trailing empty strings
    // (see API javadocs: "Trailing empty strings are therefore not included in the resulting array.")
    // we therefore append one element at the end and later remove it
    path += pathSep + "x";
    
    // now path ends with ":x", so we'll have an additional "x" element in the pathEntries array
    
    // split up the path into individual entries, turn all of the entries
    // into absolute paths, and put the path back together
    // EXCEPT for the last item in the array, because that's the "x" we added
    String[] pathEntries = path.split(pathSep);
    StringBuilder sb = new StringBuilder();
    for(int i=0; i<pathEntries.length-1; ++i) { // length-1 to ignore the last element
      File f = new File(pathEntries[i]);
      sb.append(f.getAbsolutePath());
      sb.append(pathSep);
    }
    String reconstructedPath = sb.toString();
    
    // if the reconstructed path is non-empty, then it will have an extra
    // path separator at the end; take it off
    if (reconstructedPath.length()!=0) {
      reconstructedPath = reconstructedPath.substring(0, reconstructedPath.length()-1);
    }
    
    return reconstructedPath;
  }
}
