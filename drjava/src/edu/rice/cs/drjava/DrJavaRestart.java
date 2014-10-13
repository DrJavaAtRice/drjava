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

package edu.rice.cs.drjava;

import edu.rice.cs.plt.concurrent.JVMBuilder;
import edu.rice.cs.util.FileOps;

import java.io.*;
import javax.swing.JOptionPane;

/** Helper class for DrJava that copies a file and then starts it. Part of the automatic update
  * in NewVersionPopup.
  * @version $Id: DrJavaRestart.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class DrJavaRestart {
  /**
   * Number of attempts to delete or rename files. This is necessary since an OS,
   * particularly Windows, may still have a write lock on a file.
   * Currently, we pause up to 5*1000 ms per operation. */
  public static final int ATTEMPTS = 5;
  public static final int TIME_BETWEEN_ATTEMPTS = 1000;
  
  public static edu.rice.cs.util.Log LOG = new edu.rice.cs.util.Log("version.txt",false);
  public static void message(String message) {
    LOG.log(message);
    JOptionPane.showMessageDialog(null, message,
                                  "Error Updating DrJava", JOptionPane.ERROR_MESSAGE);

  }
  public static boolean delete(File f) {
    for(int i = 0; i < ATTEMPTS; ++i) {
      if (f.delete()) return true;
      LOG.log("Failed to delete " + f + ", trying again");
      try {
        Thread.sleep(TIME_BETWEEN_ATTEMPTS);
      }
      catch(InterruptedException ie) { /* just wait */ }
    }
    return false;
  }
  public static boolean deleteRecursively(File f) {
    for(int i = 0; i < ATTEMPTS; ++i) {
      if (edu.rice.cs.plt.io.IOUtil.deleteRecursively(f)) return true;
      LOG.log("Failed to recursively delete " + f + ", trying again");
      try {
        Thread.sleep(TIME_BETWEEN_ATTEMPTS);
      }
      catch(InterruptedException ie) { /* just wait */ }
    }
    return false;
  }
  public static boolean rename(File from, File to) {
    for(int i = 0; i < ATTEMPTS; ++i) {
      if (from.renameTo(to)) return true;
      LOG.log("Failed to rename " + from + " to " + to + ", trying again");
      try {
        Thread.sleep(TIME_BETWEEN_ATTEMPTS);
      }
      catch(InterruptedException ie) { /* just wait */ }
    }
    return false;
  }
  public static void main(final String[] args) {
    // args[0] source file
    // args[1] destination file (will be started)
    // args[2] restart file (should be deleted after the restart)
    File source = new File(args[0]);
    File dest = new File(args[1]);
    File exec = dest;
    
    try {      
      LOG.log("source: " + source.getAbsolutePath());
      LOG.log("dest  : " + dest.getAbsolutePath());
      
      if (dest.exists()) {
        if (dest.isFile()) {
          // is a file, try to delete it
          if (delete(dest)) {
            // could delete it, rename source to dest
            if (!rename(source,dest)) {
              // could not rename, keep source as name
              exec = source;
              message("A new version of DrJava was downloaded. However, it could not be\n" + 
                      "installed in the same place as the old DrJava.\n\n" + 
                      "The new copy is now installed at:\n" + 
                      source.getAbsolutePath() + "\n\n" + 
                      "The old copy has been deleted.");
            }
          }
          else {
            // could not delete it, keep source as name
            exec = source;
            message("A new version of DrJava was downloaded. However, it could not be\n" + 
                    "installed in the same place as the old DrJava.\n\n" + 
                    "The new copy is now installed at:\n" + 
                    source.getAbsolutePath() + "\n\n" + 
                    "The old copy is still installed at:\n" + 
                    dest.getAbsolutePath());
          }
          LOG.log("Restarting...");
          Process p = JVMBuilder.DEFAULT.classPath(exec).start(DrJava.class.getName(), "-new", "-delete-after-restart", args[2]);
          LOG.log("Done with DrJavaRestart");
          System.exit(0);
        }
        else {
          // is a directory, try to rename
          // this should only happen with Mac DrJava.app
          File old = FileOps.generateNewFileName(dest);
          if (rename(dest,old)) {
            if (rename(source,dest)) {
              // could rename, now delete the directory containing the former source
              delete(source.getParentFile());
              if (!deleteRecursively(old)) {
                message("A new version of DrJava was downloaded. However, the old version" + 
                        "could not be deleted.\n\n" + 
                        "The new copy is now installed at:\n" + 
                        dest.getAbsolutePath() + "\n\n" + 
                        "The old copy is still installed at:\n" + 
                        old.getAbsolutePath());
              }
            }
            else {
              // could not rename, keep source as name
              exec = source;
              // try to rename dest back
              if (rename(old,dest)) {
                // was at least able to rename back
                message("A new version of DrJava was downloaded. However, it could not be\n" + 
                        "installed in the same place as the old DrJava.\n\n" + 
                        "The new copy is now installed at:\n" + 
                        source.getAbsolutePath() + "\n\n" + 
                        "The old copy is still installed at:\n" + 
                        dest.getAbsolutePath());
              }
              else {
                // couldn't even rename back
                message("A new version of DrJava was downloaded. However, it could not be\n" + 
                        "installed in the same place as the old DrJava.\n\n" + 
                        "The new copy is now installed at:\n" + 
                        source.getAbsolutePath() + "\n\n" + 
                        "The old copy is still installed at:\n" + 
                        old.getAbsolutePath());
              }
            }
          }
          else {
            // could not rename keep source as name
            exec = source;
            message("A new version of DrJava was downloaded. However, it could not be\n" + 
                    "installed in the same place as the old DrJava.\n\n" + 
                    "The new copy is now installed at:\n" + 
                    source.getAbsolutePath() + "\n\n" + 
                    "The old copy is still installed at:\n" + 
                    dest.getAbsolutePath());
          }
          
          // check if Mac's open command exists
          File macOpenFile = new File("/usr/bin/open");
          LOG.log("Searching for " + macOpenFile);
          if (!macOpenFile.exists()) {
            String path = System.getenv("PATH");
            for(String p: path.split(System.getProperty("path.separator"))) {
              macOpenFile = new File(p, "tar");
              LOG.log("Searching for " + macOpenFile);
              if (macOpenFile.exists()) break;
            }
          }
          if (macOpenFile.exists()) {
            LOG.log("Restarting using ProcessBuilder...");
            Process p = new ProcessBuilder()
              .command(macOpenFile.getAbsolutePath(), exec.getAbsolutePath())
              .redirectErrorStream(true)
              .start();
            System.exit(0);
          }
          else {
            LOG.log("Restarting using JVMBuilder...");
            exec = new File(exec,"Contents/Resources/Java/drjava.jar");
            Process p = JVMBuilder.DEFAULT.classPath(exec).start(DrJava.class.getName(), "-new", "-delete-after-restart", args[2]);
            LOG.log("Done with DrJavaRestart");
            System.exit(0);
          }
        }
      }
    }
    catch(Exception e) {
      message("A new version of DrJava was downloaded. However, there was an error" + 
              "during installation:\n" + e.getMessage());
    }
  }
}
