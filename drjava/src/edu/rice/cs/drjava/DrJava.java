/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
 END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava;

import java.io.*;
import javax.swing.*;

import edu.rice.cs.drjava.ui.MainFrame;
import edu.rice.cs.util.PreventExitSecurityManager;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.drjava.config.*;
/** 
 * Main class for DrJava. 
 * @version $Id$
 */
public class DrJava implements ConfigurationTool, OptionConstants {
  private static final PrintStream _consoleOut = System.out;
  private static final PrintStream _consoleErr = System.err;
  private static PreventExitSecurityManager _manager;
  public static final File PROPERTIES_FILE = ConfigurationTool.PROPERTIES_FILE;
  public static final FileConfiguration CONFIG = ConfigurationTool.CONFIG;

    
  public static void main(final String[] args) {
    
    /*
    Thread dbg = new Thread() {
      public void run() {
        while (true) {
          System.gc();
          int free = (int) (Runtime.getRuntime().freeMemory() / 1000);
          int total = (int) (Runtime.getRuntime().totalMemory() / 1000);
          int used = total - free;
          System.err.println(used + "k / "  + total + "k");
          
          try {
            Thread.currentThread().sleep(10000);
          } 
          catch (InterruptedException ie) {
            break;
          }
        }
        an      }
    };
    dbg.setDaemon(true);
    dbg.start();
    */
    
    try {
      System.setProperty("com.apple.macos.useScreenMenuBar","true");
      
      _setupCompilerIfNeeded();
      
      // The MainFrame *must* be constructed after the compiler setup process has
      // occurred; otherwise, the list of compilers in the UI will be wrong.
      // At some point this should be fixed, which would involve making the
      // CompilerRegistry notify listeners when there is a change in the list of
      // available compilers.
      MainFrame mf = new MainFrame();
      System.setProperty("sun.awt.exception.handler", 
                         "edu.rice.cs.drjava.AWTExceptionHandler");
      
      // This enabling of the security manager must happen *after* the mainframe
      // is constructed. See bug #518509.
      enableSecurityManager();
      openCommandLineFiles(mf, args);
      mf.show();
    } catch (Exception ex) {
      _consoleErr.println(ex.getClass().getName() + ": " + ex.getMessage());
      ex.printStackTrace(_consoleErr);
    }
  }
  
  /**
   * Handle the list of files specified on the command line.  Feature request #509701.
   * If file exists, open it in DrJava.  Otherwise, ignore it.
   * Is there a better way to handle nonexistent files?  Dialog box, maybe?
   */
  public static void openCommandLineFiles(MainFrame mf, String[] args) {
    int i;
    for(i=0; i<args.length; i++) {
      final File file = new File(args[i]);
      FileOpenSelector command = new FileOpenSelector() {
        public File getFile() {
          return file;
        }
        public File[] getFiles() {
          return new File[] {file};
        }
      };
      try {
        OpenDefinitionsDocument doc = mf.getModel().openFile(command);
      } catch (FileNotFoundException ex) {
        //dialog: file not found
      } catch (AlreadyOpenException aoe) {
        // This explicitly does nothing to ignore duplicate files.
      } catch (Exception ex) {
        throw new UnexpectedException(ex);
      }
    }
  }
  
  /**
   * Implements feature req #523222: Prompt user for compiler if none found.
   */
  private static void _setupCompilerIfNeeded() {
    if (CompilerRegistry.ONLY.isNoCompilerAvailable()) {
      // no compiler available; let's try to let the user pick one.
      final String[] text = {
        "DrJava can not find any Java compiler. Would you ",
        "like to configure the location of the compiler? ",
        "The compiler is generally located in 'tools.jar', ",
        "in the 'lib' subdirectory under your JDK ",
        "installation directory. (If you say 'No', DrJava ",
        "will be unable to compile programs.)"
      };
      
      int result = JOptionPane.showConfirmDialog(null,
                                                 text,
                                                 "Compiler not found",
                                                 JOptionPane.YES_NO_OPTION);
      
      if (result == JOptionPane.YES_OPTION) {
        JFileChooser chooser = new JFileChooser();
        
        do {
          if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File jar = chooser.getSelectedFile();
            
            // set the javac property
            CONFIG.setSetting(JAVAC_LOCATION,
                              jar.getAbsolutePath());
            
            // need to re-call getAvailable for it to re-check availability
            CompilerInterface[] compilers
              = CompilerRegistry.ONLY.getAvailableCompilers();
            
            if (compilers[0] != NoCompilerAvailable.ONLY) {
              CompilerRegistry.ONLY.setActiveCompiler(compilers[0]);
              try {
                CONFIG.saveConfiguration();
              } catch(IOException e) {
                // for now, do nothing
              }
            }
          }
        }
        while (CompilerRegistry.ONLY.isNoCompilerAvailable() &&
               _userWantsToPickAgain());
      }
    }
  }
  
  private static boolean _userWantsToPickAgain() {
    final String[] text = {
      "The file you chose did not appear to contain the compiler. ",
      "Would you like to pick again? The compiler is generally ",
      "located in 'tools.jar', in the 'lib' subdirectory under ",
      "your JDK installation directory.",
      "(If you say 'No', DrJava will be unable to compile programs.)"
    };
      
    /*
     final String text = 
     "The file you chose did not appear to contain the compiler.\n" + 
     "Would you like to pick again? The compiler is generally\n" + 
     "located in 'tools.jar', in the 'lib' subdirectory under\n" + 
     "your JDK installation directory.\n" + 
     "(If you say 'No', DrJava will be unable to compile programs.)";
     
     final JTextArea area = new JTextArea(text);
     area.setEditable(false);
     */
    
    
    int result = JOptionPane.showConfirmDialog(null,
                                               text,
                                               "Compiler not found",
                                               JOptionPane.YES_NO_OPTION);
    
    return result == JOptionPane.YES_OPTION;
  }
  
  public static PreventExitSecurityManager getSecurityManager() {
    return _manager;
  }
  
  public static void enableSecurityManager() {
    if (_manager == null) {
      _manager = PreventExitSecurityManager.activate();
    }
    
    if (System.getSecurityManager() != _manager) {
      System.setSecurityManager(_manager);
    }
  }
  
  public static void disableSecurityManager() {
    _manager.deactivate();
  }
  
  /**
   * Get the actual System.err stream.
   * @return System.err
   */
  public static PrintStream consoleErr() {
    return  _consoleErr;
  }
  
  /**
   * Get the actual System.out stream.
   * @return System.out
   */
  public static PrintStream consoleOut() {
    return  _consoleOut;
  }
}
