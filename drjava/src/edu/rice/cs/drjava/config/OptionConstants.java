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

package edu.rice.cs.drjava.config;

import java.io.File;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.util.FileOps;

import static java.awt.Event.*;


/** Defines the commonly used Option constants in DrJava config and project profiles.
  * @version $Id: OptionConstants.java 5707 2012-08-26 05:57:03Z rcartwright $
  */
public interface OptionConstants {
  
  // STATIC VARIABLES
  
  /* ---------- Resource Location and Classpath Options ---------- */
  
  /** A file path to a user's preferred browser. */
  public static final FileOption BROWSER_FILE = new FileOption("browser.file", FileOps.NULL_FILE);
  
  /** A String used to launch a user's preferred browser. It is tokenized and appended to the file path. */
  public static final StringOption BROWSER_STRING = new StringOption("browser.string", "");
  
  /** A Vector<String> storing the classes to automatically import. */
  public static final VectorOption<String> INTERACTIONS_AUTO_IMPORT_CLASSES =
    new VectorOption<String>("interactions.auto.import.classes", new StringOption("",""), new Vector<String>());
  
  /* The default rate at which the debugger steps into or over every line of code*/
  public static final NonNegativeIntegerOption AUTO_STEP_RATE = new NonNegativeIntegerOption("auto.step.rate", 1000);
  
  /** The extension for an old DrJava project file */
  public static final String OLD_PROJECT_FILE_EXTENSION = ".pjt";
  
  /** The extension for a DrJava project file */
  public static final String PROJECT_FILE_EXTENSION = ".drjava";
  
  /** The alternative extension for a DrJava project file */
  public static final String PROJECT_FILE_EXTENSION2 = ".xml";
  
  /** The extension for stand-alone DrJava external process file. */
  public static final String EXTPROCESS_FILE_EXTENSION = ".djapp";

  /** The extension for a Java source file */
  public static final String JAVA_FILE_EXTENSION = ".java";
  
  /** The extension for a Java source file */
  public static final String SCALA_FILE_EXTENSION = ".scala";
  
  // Java Language Levels is disabled

//  /** The extension for a language level source file */
//  public static final String DJ_FILE_EXTENSION = ".dj";
//
//  /** The old extension for an elementary language level source file */
//  public static final String OLD_DJ0_FILE_EXTENSION = ".dj0";
//
//  /** The old extension for an intermediate language level source file */
//  public static final String OLD_DJ1_FILE_EXTENSION = ".dj1";
//
//  /** The old extension for an advanced language level source file */
//  public static final String OLD_DJ2_FILE_EXTENSION = ".dj2";
    
  /* Constants for language levels */
  public static final int FULL_JAVA = 0;
  public static final int ELEMENTARY_LEVEL = 1;
  public static final int INTERMEDIATE_LEVEL = 2;
  public static final int ADVANCED_LEVEL = 3;
  public static final int FUNCTIONAL_JAVA_LEVEL = 4;
  public static final String[] LANGUAGE_LEVEL_EXTENSIONS = new String[] {
    SCALA_FILE_EXTENSION,
    JAVA_FILE_EXTENSION 

//      OLD_DJ0_FILE_EXTENSION, // = .dj0
//      OLD_DJ1_FILE_EXTENSION, // = .dj1
//      OLD_DJ2_FILE_EXTENSION, // = .dj2
//      DJ_FILE_EXTENSION  // = .dj
  };
  /** The configuration XML file that DrJava looks for inside a .djapp file */
  public static final String EXTPROCESS_FILE_NAME_INSIDE_JAR = "process" + EXTPROCESS_FILE_EXTENSION;

  /** The extension for a text file */
  public static final String TEXT_FILE_EXTENSION = ".txt";
  
  /** tools.jar location, or NULL_FILE if not specified. */
  public static final FileOption JAVAC_LOCATION = new FileOption("javac.location", FileOps.NULL_FILE);
  
  /** Extra class path. */
  public static final VectorOption<File> EXTRA_CLASSPATH = new ClassPathOption().evaluate("extra.classpath");
  
  public static final VectorOption<String> EXTRA_COMPILERS =
    new VectorOption<String>("extra.compilers", new StringOption("",""), new Vector<String>());
  
  /** Whether to display all versions of the compilers (even if they have the same major version). */
  public static final BooleanOption DISPLAY_ALL_COMPILER_VERSIONS = 
    new BooleanOption("all.compiler.versions", Boolean.FALSE);
  
  
  /* ---------- Color Options ---------- */
  
  public static final ColorOption DEFINITIONS_NORMAL_COLOR = new ColorOption("definitions.normal.color", Color.black);
  public static final ColorOption DEFINITIONS_KEYWORD_COLOR = new ColorOption("definitions.keyword.color", Color.blue);
  public static final ColorOption DEFINITIONS_TYPE_COLOR =
    new ColorOption("definitions.type.color", Color.blue.darker().darker());
  public static final ColorOption DEFINITIONS_COMMENT_COLOR =
    new ColorOption("definitions.comment.color", Color.green.darker().darker());
  public static final ColorOption DEFINITIONS_DOUBLE_QUOTED_COLOR =
    new ColorOption("definitions.double.quoted.color", Color.red.darker());
  public static final ColorOption DEFINITIONS_SINGLE_QUOTED_COLOR =
    new ColorOption("definitions.single.quoted.color", Color.magenta);
  public static final ColorOption DEFINITIONS_NUMBER_COLOR =
    new ColorOption("definitions.number.color", Color.cyan.darker());
  public static final ColorOption SYSTEM_OUT_COLOR = new ColorOption("system.out.color", Color.green.darker().darker());
  public static final ColorOption SYSTEM_ERR_COLOR = new ColorOption("system.err.color", Color.red);
  public static final ColorOption SYSTEM_IN_COLOR = new ColorOption("system.in.color", Color.magenta.darker().darker());
  public static final ColorOption INTERACTIONS_ERROR_COLOR =
    new ColorOption("interactions.error.color", Color.red.darker());
  public static final ColorOption DEBUG_MESSAGE_COLOR = new ColorOption("debug.message.color", Color.blue.darker());
  
  /** Color for background of definitions pane. */
  public static final ColorOption DEFINITIONS_BACKGROUND_COLOR =
    new ColorOption("definitions.background.color", Color.white);
  
  /** Color for background of line numbers in definitions pane. */
  public static final ColorOption DEFINITIONS_LINE_NUMBER_BACKGROUND_COLOR =
    new ColorOption("definitions.line.number.background.color",new Color(250, 250, 250));
  
  /** Color for background of line numbers in definitions pane. */
  public static final ColorOption DEFINITIONS_LINE_NUMBER_COLOR =
    new ColorOption("definitions.line.number.color", Color.black);
  
  /** Color for highlighting brace-matching. */
  public static final ColorOption DEFINITIONS_MATCH_COLOR =
    new ColorOption("definitions.match.color", new Color(190, 255, 230));
  
  /** Color for highlighting errors and test failures. */
  public static final ColorOption COMPILER_ERROR_COLOR = new ColorOption("compiler.error.color", Color.yellow);
  
  /** Color for highlighting bookmarks. */
  public static final ColorOption BOOKMARK_COLOR = new ColorOption("bookmark.color", Color.green);
  
  /** Color for highlighting find results. */
  public static final ColorOption FIND_RESULTS_COLOR1 = 
    new ColorOption("find.results.color1", new Color(0xFF, 0x99, 0x33));
  public static final ColorOption FIND_RESULTS_COLOR2 = 
    new ColorOption("find.results.color2", new Color(0x30, 0xC9, 0x96));
  public static final ColorOption FIND_RESULTS_COLOR3 = 
    new ColorOption("find.results.color3", Color.ORANGE);
  public static final ColorOption FIND_RESULTS_COLOR4 = 
    new ColorOption("find.results.color4", Color.MAGENTA);
  public static final ColorOption FIND_RESULTS_COLOR5 = 
    new ColorOption("find.results.color5", new Color(0xCD, 0x5C, 0x5C));
  public static final ColorOption FIND_RESULTS_COLOR6 = 
    new ColorOption("find.results.color6", Color.DARK_GRAY);
  public static final ColorOption FIND_RESULTS_COLOR7 = 
    new ColorOption("find.results.color7", Color.GREEN);
  public static final ColorOption FIND_RESULTS_COLOR8 = 
    new ColorOption("find.results.color8", Color.BLUE);
  
  public static final ColorOption[] FIND_RESULTS_COLORS = new ColorOption[] {
    FIND_RESULTS_COLOR1,
      FIND_RESULTS_COLOR2,
      FIND_RESULTS_COLOR3,
      FIND_RESULTS_COLOR4,
      FIND_RESULTS_COLOR5,
      FIND_RESULTS_COLOR6,
      FIND_RESULTS_COLOR7,
      FIND_RESULTS_COLOR8
  };
  
  /** Color for highlighting breakpoints. */
  public static final ColorOption DEBUG_BREAKPOINT_COLOR = new ColorOption("debug.breakpoint.color", Color.red);
  
  /** Color for highlighting disabled breakpoints. */
  public static final ColorOption DEBUG_BREAKPOINT_DISABLED_COLOR = 
    new ColorOption("debug.breakpoint.disabled.color", new Color(128,0,0));
  
  /** Color for highlighting thread locations. */
  public static final ColorOption DEBUG_THREAD_COLOR = new ColorOption("debug.thread.color", new Color(100,255,255));
  
  /** Color for the background of the "DrScala Errors" button. */
  public static final ColorOption DRJAVA_ERRORS_BUTTON_COLOR = new ColorOption("drjava.errors.button.color", Color.red);

  /** Color for the line at the right margin. */
  public static final ColorOption RIGHT_MARGIN_COLOR = new ColorOption("right.margin.color", new Color(204,204,204));
  
  /* ---------- Font Options ---------- */
  
  /** Main (definitions document, tab contents) */
  public static final FontOption FONT_MAIN = new FontOption("font.main", DefaultFont.getDefaultMainFont());
  
  /** Class that allows the main font to be initialized properly. On Mac OS X, Monaco is the best monospaced font. */
  static class DefaultFont {
    public static Font getDefaultMainFont() {
      if (PlatformFactory.ONLY.isMacPlatform())  return Font.decode("Monaco-12");
      else return Font.decode("Monospaced-12");
    }
    public static Font getDefaultLineNumberFont() {
      if (PlatformFactory.ONLY.isMacPlatform()) return Font.decode("Monaco-12");
      else return Font.decode("Monospaced-12");
    }
    public static Font getDefaultDocListFont() {
      if (PlatformFactory.ONLY.isMacPlatform()) return Font.decode("Monaco-10");
      else return Font.decode("Monospaced-10");
    }
  }
  
  /** Line numbers */
  public static final FontOption FONT_LINE_NUMBERS =
    new FontOption("font.line.numbers", DefaultFont.getDefaultLineNumberFont());
  
  /** List of open documents */
  public static final FontOption FONT_DOCLIST = new FontOption("font.doclist", DefaultFont.getDefaultDocListFont());
  
  /** Toolbar buttons */
  public static final FontOption FONT_TOOLBAR = new FontOption("font.toolbar", Font.decode("dialog-10"));
  
  /** Whether to draw anti-aliased text.  (Slightly slower.) */
  public static final BooleanOption TEXT_ANTIALIAS = new BooleanOption("text.antialias", Boolean.TRUE);

  /** Whether to draw the right margin.  (Slightly slower.) */
  public static final BooleanOption DISPLAY_RIGHT_MARGIN = new BooleanOption("display.right.margin", Boolean.TRUE);

  /** After how many columns to draw the right margin. */
  public static final NonNegativeIntegerOption RIGHT_MARGIN_COLUMNS =
    new NonNegativeIntegerOption("right.margin.columns", 120);
  
  
  /* ---------- Other Display Options ---------- */
  
  /** Whether icons should be displayed on the toolbar buttons. */
  public static final BooleanOption TOOLBAR_ICONS_ENABLED =
    new BooleanOption("toolbar.icons.enabled", Boolean.TRUE);
  
  /** Whether text should be displayed on toolbar buttons. Note: only relevant if toolbar icons are enabled. */
  public static final BooleanOption TOOLBAR_TEXT_ENABLED = new BooleanOption("toolbar.text.enabled", Boolean.TRUE);
  
  /** Whether or not the toolbar should be displayed. */
  public static final BooleanOption TOOLBAR_ENABLED = new BooleanOption("toolbar.enabled", Boolean.TRUE);
  
  /** Whether the line-numbers should be displayed in a row header. */
  public static final BooleanOption LINEENUM_ENABLED = new BooleanOption("lineenum.enabled", Boolean.FALSE);
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption WINDOW_STORE_POSITION = new BooleanOption("window.store.position", Boolean.TRUE);
  
  /** Whether a sample of the source code will be show when fast switching documents. */
  public static final BooleanOption SHOW_SOURCE_WHEN_SWITCHING = 
    new BooleanOption("show.source.for.fast.switch", Boolean.TRUE);
  
  /** The current look and feel. */
  public static final ForcedChoiceOption LOOK_AND_FEEL =
    new ForcedChoiceOption("look.and.feel", LookAndFeels.getDefaultLookAndFeel(), LookAndFeels.getLookAndFeels());
  
  /** Class that allows the look and feels to be initialized properly. */
  static class LookAndFeels {
    private static String[][] _registerLAFs = {
      {"Sea Glass", "com.seaglasslookandfeel.SeaGlassLookAndFeel"},
      {"Plastic 3D", "com.jgoodies.looks.plastic.Plastic3DLookAndFeel"},
      {"Plastic XP", "com.jgoodies.looks.plastic.PlasticXPLookAndFeel"},
      {"Plastic Windows", "com.jgoodies.looks.windows.Plastic3DLookAndFeel"},
      {"Plastic", "com.jgoodies.looks.plastic.PlasticLookAndFeel"}
    };
    
    private static boolean _registered = false;
    
    /** Return the look-and-feel class name to use by default */
    public static String getDefaultLookAndFeel() {
      if (PlatformFactory.ONLY.isMacPlatform())
        return UIManager.getSystemLookAndFeelClassName(); // Mac: Let the system decide.
      else // Set CrossPlatform "Nimbus" LookAndFeel
        try {
          for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
            if ("Nimbus".equals(info.getName())) return info.getClassName();
      } catch (Exception e) {
        // If Nimbus is not available, fall through and use CrossPlatformLookAndFeel
      }
      return UIManager.getCrossPlatformLookAndFeelClassName();
    }
    
    /** Need to ensure that a look-and-feel can be instantiated and is valid.
      * TODO:  store the LookAndFeel object rather than its classname.  This would be much nicer, as we could display a 
      * useful name, and wouldn't have to reinstantiate it when it's installed.
      * @return the list of available look-and-feel classnames
      */
    public static ArrayList<String> getLookAndFeels() {
      if(!_registered && !PlatformFactory.ONLY.isMacPlatform()) {
        for(String[] newLaf : _registerLAFs) {
          try {
            Class.forName(newLaf[1]);
          } catch(ClassNotFoundException ex) {
            continue;
          }
          UIManager.installLookAndFeel(newLaf[0], newLaf[1]);
        }
      }
      ArrayList<String> lookAndFeels = new ArrayList<String>();
      LookAndFeelInfo[] lafis = UIManager.getInstalledLookAndFeels();
      if (lafis != null) {
        for (int i = 0; i < lafis.length; i++) {
          try {
            String currName = lafis[i].getClassName();
            LookAndFeel currLAF = (LookAndFeel) Class.forName(currName).newInstance();
            // Filter out "gtk" LookAndFeel; it is broken on Linux in several ways
            if (! currName.contains("gtk") && currLAF.isSupportedLookAndFeel()) lookAndFeels.add(currName);
//  This patch works around a bug in the Oracle Java 7/8 JVMs            
//            if (currLAF.isSupportedLookAndFeel() && ! currName.contains("gtk")) lookAndFeels.add(currName);
          }
          // failed to load/instantiate class, or it is not supported; it is not a valid choice.
          catch (ClassNotFoundException e) { /* do nothing */ }
          catch (InstantiationException e) { /* do nothing */ }
          catch (IllegalAccessException e) { /* do nothing */ }
        }
      }
      return lookAndFeels;
    }
  }
  
  public static final ForcedChoiceOption PLASTIC_THEMES =
    new ForcedChoiceOption("plastic.theme", PlasticThemes.getDefaultTheme(), PlasticThemes.getThemes());
  
  /* TODO: This theme list is current as of JGoodies Looks 2.1. 
   *       We could automatically update this list by enumerating types in the
   *       com.jgoodies.looks.themes package, and excluding abstract classes.
   */
  static class PlasticThemes {
    public static ArrayList<String> getThemes() {
      ArrayList<String> al = new ArrayList<String>();
      String[] themes = new String[] {
        "BrownSugar", "DarkStar",
          "SkyBlue", "SkyGreen", "SkyKrupp", "SkyPink", "SkyRed", "SkyYellow",
          "DesertBluer", "DesertBlue", "DesertGreen", "DesertRed", "DesertYellow",
          "ExperienceBlue", "ExperienceGreen", "LightGray", "Silver",
          "ExperienceRoyale"
      };
      for(String theme : themes) {
        al.add(theme);
      }
      return al;
    }
    
    public static String getDefaultTheme() {
      return "DesertBlue";
    }
  }
  
  /* ---------- Key Binding Options ----------- */
  public static int MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
  
  static class to {
    public static Vector<KeyStroke> vector(KeyStroke... ks) {
      Vector<KeyStroke> v = new Vector<KeyStroke>();
      for(KeyStroke k: ks) { v.add(k); }
      return v;
    }
  }
  
  /** The key binding for creating a new file */
  public static final VectorOption<KeyStroke> KEY_NEW_FILE =
    new VectorOption<KeyStroke>("key.new.file", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_N, MASK)));
  
  /** The key binding for creating a new java class file */
  public static final VectorOption<KeyStroke> KEY_NEW_CLASS_FILE =
    new VectorOption<KeyStroke>("key.new.javafile", new KeyStrokeOption("",null),
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_N, MASK|SHIFT_MASK))); 
  
  /** The key binding for opening an entire project.  I is right next to O, so
    * it seemed logical that ctrl-I would open a project and ctrl-O open a file */
  public static final VectorOption<KeyStroke> KEY_OPEN_PROJECT =
    new VectorOption<KeyStroke>("key.open.project", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_I, MASK)));

  /** The key binding for creating a new project. */
  public static final VectorOption<KeyStroke> KEY_NEW_PROJECT = 
    new VectorOption<KeyStroke>("key.new.project", new KeyStrokeOption("",null), to.vector());

  /** The key binding for saving a project. */
  public static final VectorOption<KeyStroke> KEY_SAVE_PROJECT = 
    new VectorOption<KeyStroke>("key.save.project", new KeyStrokeOption("",null), to.vector());

  /** The key binding for saving a project as a different file. */
  public static final VectorOption<KeyStroke> KEY_SAVE_AS_PROJECT = 
    new VectorOption<KeyStroke>("key.save.as.project", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for compiling a project. */
  public static final VectorOption<KeyStroke> KEY_COMPILE_PROJECT = 
    new VectorOption<KeyStroke>("key.compile.project", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for testing a project. */
  public static final VectorOption<KeyStroke> KEY_JUNIT_PROJECT = 
    new VectorOption<KeyStroke>("key.junit.project", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for running a project. */
  public static final VectorOption<KeyStroke> KEY_RUN_PROJECT = 
    new VectorOption<KeyStroke>("key.run.project", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for cleaning a project. */
  public static final VectorOption<KeyStroke> KEY_CLEAN_PROJECT = 
    new VectorOption<KeyStroke>("key.clean.project", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for refreshing a project. */
  public static final VectorOption<KeyStroke> KEY_AUTO_REFRESH_PROJECT = 
    new VectorOption<KeyStroke>("key.auto.refresh.project", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for creating a jar of a project. */
  public static final VectorOption<KeyStroke> KEY_JAR_PROJECT = 
    new VectorOption<KeyStroke>("key.jar.project", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for editing the project properties. */
  public static final VectorOption<KeyStroke> KEY_PROJECT_PROPERTIES = 
    new VectorOption<KeyStroke>("key.project.properties", new KeyStrokeOption("",null),
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_I, MASK|SHIFT_MASK)));
  
  /** The key binding for creating a new JUnit test case */
  public static final VectorOption<KeyStroke> KEY_NEW_TEST = 
    new VectorOption<KeyStroke>("key.new.test", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for opening a folder */
  public static final VectorOption<KeyStroke> KEY_OPEN_FOLDER =
    new VectorOption<KeyStroke>("key.open.folder", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_O, MASK|SHIFT_MASK)));
  
  /** The key binding for opening a file. */
  public static final VectorOption<KeyStroke> KEY_OPEN_FILE =
    new VectorOption<KeyStroke>("key.open.file", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_O, MASK)));
  
  /** The key binding for saving a file. */
  public static final VectorOption<KeyStroke> KEY_SAVE_FILE =
    new VectorOption<KeyStroke>("key.save.file", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_S, MASK)));
  
  /** The key binding for saving a file as. */
  public static final VectorOption<KeyStroke> KEY_SAVE_FILE_AS =
    new VectorOption<KeyStroke>("key.save.file.as", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_S, MASK | SHIFT_MASK)));
  
  /** The key binding for saving a file copy */
  public static final VectorOption<KeyStroke> KEY_SAVE_FILE_COPY =
    new VectorOption<KeyStroke>("key.save.file.copy", 
                                new KeyStrokeOption("",null), 
                                to.vector());
  
  /** The key binding for saving all files. */
  public static final VectorOption<KeyStroke> KEY_SAVE_ALL_FILES =
    new VectorOption<KeyStroke>("key.save.all.files", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_S, MASK | ALT_MASK)));
  
  /** The key binding for exporting in the old project file format */
  public static final VectorOption<KeyStroke> KEY_EXPORT_OLD = 
    new VectorOption<KeyStroke>("key.export.old", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for renaming a file. */
  public static final VectorOption<KeyStroke> KEY_RENAME_FILE = 
    new VectorOption<KeyStroke>("key.rename.file", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_R, MASK)));
  
  /** The key binding for reverting a file. */
  public static final VectorOption<KeyStroke> KEY_REVERT_FILE =
    new VectorOption<KeyStroke>("key.revert.file",
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_R, MASK|SHIFT_MASK)));
  
  /** The key binding for closing a file */
  public static final VectorOption<KeyStroke> KEY_CLOSE_FILE =
    new VectorOption<KeyStroke>("key.close.file", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_W, MASK)));
  
  /** The key binding for closing all files */
  public static final VectorOption<KeyStroke> KEY_CLOSE_ALL_FILES =
    new VectorOption<KeyStroke>("key.close.all.files", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_W, MASK|ALT_MASK)));
  
  public static final VectorOption<KeyStroke> KEY_CLOSE_PROJECT =
    new VectorOption<KeyStroke>("key.close.project", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_W, MASK|SHIFT_MASK)));
  
  /** The key binding for showing the print preview */
  public static final VectorOption<KeyStroke> KEY_PAGE_SETUP =
    new VectorOption<KeyStroke>("key.page.setup", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for showing the print preview. */
  public static final VectorOption<KeyStroke> KEY_PRINT_PREVIEW =
    new VectorOption<KeyStroke>("key.print.preview", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_P, MASK | SHIFT_MASK)));
  
  /** The key binding for printing a file */
  public static final VectorOption<KeyStroke> KEY_PRINT =
    new VectorOption<KeyStroke>("key.print", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_P, MASK)));
  
  /** The key binding for quitting */
  public static final VectorOption<KeyStroke> KEY_QUIT =
    new VectorOption<KeyStroke>("key.quit", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_Q, MASK)));
  
  /** The key binding for forced quitting */
  public static final VectorOption<KeyStroke> KEY_FORCE_QUIT =
    new VectorOption<KeyStroke>("key.force.quit", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for undo-ing */
  public static final VectorOption<KeyStroke> KEY_UNDO =
    new VectorOption<KeyStroke>("key.undo", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_Z, MASK)));
  
  /** The key binding for redo-ing */
  public static final VectorOption<KeyStroke> KEY_REDO =
    new VectorOption<KeyStroke>("key.redo", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_Z, MASK|SHIFT_MASK)));
  
  /** The key binding for cutting */
  public static final VectorOption<KeyStroke> KEY_CUT =
    new VectorOption<KeyStroke>("key.cut", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_X, MASK)));
  
  /** The key binding for copying */
  public static final VectorOption<KeyStroke> KEY_COPY =
    new VectorOption<KeyStroke>("key.copy", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_C, MASK)));
  
  /** The key binding for pasting */
  public static final VectorOption<KeyStroke> KEY_PASTE =
    new VectorOption<KeyStroke>("key.paste", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_V, MASK)));
  
  /** The key binding for pasting from history */
  public static final VectorOption<KeyStroke> KEY_PASTE_FROM_HISTORY =
    new VectorOption<KeyStroke>("key.paste.from.history", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_V , MASK|SHIFT_MASK)));
  
  /** The key binding for selecting all text */
  public static final VectorOption<KeyStroke> KEY_SELECT_ALL =
    new VectorOption<KeyStroke>("key.select.all", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_A, MASK)));
  
  /** The key binding for find and replace */
  public static final VectorOption<KeyStroke> KEY_FIND_NEXT =
    new VectorOption<KeyStroke>("key.find.next", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0)));
  
  /** The key binding for find previous (opposite direction) */
  public static final VectorOption<KeyStroke> KEY_FIND_PREV =
    new VectorOption<KeyStroke>("key.find.prev", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F3, SHIFT_MASK)));
  
  /** The key binding for find and replace */
  public static final VectorOption<KeyStroke> KEY_FIND_REPLACE =
    new VectorOption<KeyStroke>("key.find.replace", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F, MASK)));
  
  /** The key binding for goto line */
  public static final VectorOption<KeyStroke> KEY_GOTO_LINE =
    new VectorOption<KeyStroke>("key.goto.line", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_G, MASK)));
  
  /** The key binding for goto file. */
  public static final VectorOption<KeyStroke> KEY_GOTO_FILE =
    new VectorOption<KeyStroke>("key.goto.file", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_G, MASK|KeyEvent.SHIFT_MASK)));
  
  /** The key binding for goto this file. */
  public static final VectorOption<KeyStroke> KEY_GOTO_FILE_UNDER_CURSOR =
    new VectorOption<KeyStroke>("key.goto.file.under.cursor", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0)));
  
  /** The key binding for open Javadoc. */
  public static final VectorOption<KeyStroke> KEY_OPEN_JAVADOC =
    new VectorOption<KeyStroke>("key.open.javadoc", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.SHIFT_MASK)));
  
  /** The key binding for open Javadoc under cursor. */
  public static final VectorOption<KeyStroke> KEY_OPEN_JAVADOC_UNDER_CURSOR =
    new VectorOption<KeyStroke>("key.open.javadoc.under.cursor", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F6, MASK)));
  
  /** The key binding for complete file. */
  public static final VectorOption<KeyStroke> KEY_COMPLETE_FILE =
    new VectorOption<KeyStroke>("key.complete.file", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, MASK|KeyEvent.SHIFT_MASK)));
  
//  /** The key binding for indenting */
//  public static final VectorOption<KeyStroke> KEY_INDENT =
//    new VectorOption<KeyStroke>("key.indent",
//                                new KeyStrokeOption("",null), 
//                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, MASK)));
  
  /** The key binding for commenting out lines */
  public static final VectorOption<KeyStroke> KEY_COMMENT_LINES =
    new VectorOption<KeyStroke>("key.comment.lines", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, MASK)));
  
  /** The key binding for un-commenting lines */
  public static final VectorOption<KeyStroke> KEY_UNCOMMENT_LINES =
    new VectorOption<KeyStroke>("key.uncomment.lines", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, MASK|SHIFT_MASK)));
  
  /** The key binding for selecting previous document */
  public static final VectorOption<KeyStroke> KEY_PREVIOUS_DOCUMENT =
    new VectorOption<KeyStroke>("key.previous.document", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, MASK)));
  
  /** The key binding for selecting next document */
  public static final VectorOption<KeyStroke> KEY_NEXT_DOCUMENT =
    new VectorOption<KeyStroke>("key.next.document",
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, MASK)));
  
  /** The key binding for changing the focus to the previous pane */
  public static final VectorOption<KeyStroke> KEY_PREVIOUS_PANE =
    new VectorOption<KeyStroke>("key.previous.pane", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, MASK)));
  
  /** The key binding for changing the focus to the next pane */
  public static final VectorOption<KeyStroke> KEY_NEXT_PANE =
    new VectorOption<KeyStroke>("key.next.pane", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, MASK)));
  
  /** The key binding for going to the opening brace. */
  public static final VectorOption<KeyStroke> KEY_OPENING_BRACE =
    new VectorOption<KeyStroke>("key.goto.opening.brace", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, MASK|SHIFT_MASK)));
  
  /** The key binding for going to the closing brace. */
  public static final VectorOption<KeyStroke> KEY_CLOSING_BRACE =
    new VectorOption<KeyStroke>("key.goto.closing.brace", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, MASK|SHIFT_MASK)));
  
  /** The key binding for jumping to the next location in the browser history */
  public static final VectorOption<KeyStroke> KEY_BROWSE_FORWARD =
    new VectorOption<KeyStroke>("key.browse.forward", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ALT_MASK|SHIFT_MASK)));
  
  /** The key binding for jumping to the previous location in the browser history */
  public static final VectorOption<KeyStroke> KEY_BROWSE_BACK =
    new VectorOption<KeyStroke>("key.browse.back", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ALT_MASK|SHIFT_MASK)));
  
  /** The key binding for going to the next region in the tabbed pane */
  public static final VectorOption<KeyStroke> KEY_TABBED_NEXT_REGION =
    new VectorOption<KeyStroke>("key.tabbed.next.region", 
                                new KeyStrokeOption("",null),
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ALT_MASK|SHIFT_MASK)));
  
  /** The key binding for going to the previous region in the tabbed pane */
  public static final VectorOption<KeyStroke> KEY_TABBED_PREV_REGION =
    new VectorOption<KeyStroke>("key.tabbed.prev.region", 
                                new KeyStrokeOption("",null),
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_UP, ALT_MASK|SHIFT_MASK)));
  
  /** The key binding for openning the preferences dialog */
  public static final VectorOption<KeyStroke> KEY_PREFERENCES =
    new VectorOption<KeyStroke>("key.preferences", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, MASK)));
  
  /** The key binding for compiling current document */
  public static final VectorOption<KeyStroke> KEY_COMPILE =
    new VectorOption<KeyStroke>("key.compile", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F5, SHIFT_MASK)));
  
  /** The key binding for compiling all */
  public static final VectorOption<KeyStroke> KEY_COMPILE_ALL =
    new VectorOption<KeyStroke>("key.compile.all", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)));
  
  /** The key binding for running the main method of the current document */
  public static final VectorOption<KeyStroke> KEY_RUN =
    new VectorOption<KeyStroke>("key.run", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0)));
  
  /** The key binding for running the current document as applet. */
  public static final VectorOption<KeyStroke> KEY_RUN_APPLET =
    new VectorOption<KeyStroke>("key.run.applet", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F2, SHIFT_MASK)));
  
  /** The key binding for testing the current document */
  public static final VectorOption<KeyStroke> KEY_TEST =
    new VectorOption<KeyStroke>("key.test", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_T, MASK|SHIFT_MASK)));
  
  /** The key binding for testing all open JUnit test cases. */
  public static final VectorOption<KeyStroke> KEY_TEST_ALL =
    new VectorOption<KeyStroke>("key.test.all", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_T, MASK)));
  
  /** The key binding for generating javadoc for all documents */
  public static final VectorOption<KeyStroke> KEY_JAVADOC_ALL =
    new VectorOption<KeyStroke>("key.javadoc.all", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_J, MASK)));
  
  /** The key binding for generating javadoc for the current document */
  public static final VectorOption<KeyStroke> KEY_JAVADOC_CURRENT =
    new VectorOption<KeyStroke>("key.javadoc.current", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_J, MASK | SHIFT_MASK)));
  
  /** The key binding for saving the interactions copy to a file. */
  public static final VectorOption<KeyStroke> KEY_SAVE_INTERACTIONS_COPY =
    new VectorOption<KeyStroke>("key.save.interactions.copy", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for executing an interactions history. */
  public static final VectorOption<KeyStroke> KEY_EXECUTE_HISTORY =
    new VectorOption<KeyStroke>("key.execute.history", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for loading an interactions history as a script. */
  public static final VectorOption<KeyStroke> KEY_LOAD_HISTORY_SCRIPT =
    new VectorOption<KeyStroke>("key.load.history.script", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for saving an interactions history. */
  public static final VectorOption<KeyStroke> KEY_SAVE_HISTORY =
    new VectorOption<KeyStroke>("key.save.history", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for clearing the interactions history. */
  public static final VectorOption<KeyStroke> KEY_CLEAR_HISTORY =
    new VectorOption<KeyStroke>("key.clear.history", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for resetting the interactions pane. */
  public static final VectorOption<KeyStroke> KEY_RESET_INTERACTIONS =
    new VectorOption<KeyStroke>("key.reset.interactions", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for viewing the interactions classpath. */
  public static final VectorOption<KeyStroke> KEY_VIEW_INTERACTIONS_CLASSPATH =
    new VectorOption<KeyStroke>("key.view.interactions.classpath", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for printing the interactions. */
  public static final VectorOption<KeyStroke> KEY_PRINT_INTERACTIONS =
    new VectorOption<KeyStroke>("key.view.print.interactions", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for lifting the current interaction to definitions. */
  public static final VectorOption<KeyStroke> KEY_LIFT_CURRENT_INTERACTION =
    new VectorOption<KeyStroke>("key.lift.current.interaction", new KeyStrokeOption("",null), to.vector());
  
//  /** The key binding to enter or leave multiline input mode. */
//  public static final VectorOption<KeyStroke> KEY_TOGGLE_MULTILINE_INTERACTION =
//   new VectorOption<KeyStroke>("key.toggle.multiline.interaction", 
//                               new KeyStrokeOption("",null), 
//                               to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_M, MASK)));
  
  /** The key binding for saving the console copy to a file. */
  public static final VectorOption<KeyStroke> KEY_SAVE_CONSOLE_COPY =
    new VectorOption<KeyStroke>("key.save.console.copy", new KeyStrokeOption("",null), to.vector());

  /** The key binding for clearing the console. */
  public static final VectorOption<KeyStroke> KEY_CLEAR_CONSOLE =
    new VectorOption<KeyStroke>("key.clear.console", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for printing the console. */
  public static final VectorOption<KeyStroke> KEY_PRINT_CONSOLE =
    new VectorOption<KeyStroke>("key.view.print.console", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for moving the cursor backwards */
  public static final VectorOption<KeyStroke> KEY_BACKWARD =
    new VectorOption<KeyStroke>("key.backward", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0)));
  
  /** The key binding for moving the cursor backwards with selection */
  public static final VectorOption<KeyStroke> KEY_BACKWARD_SELECT =
    new VectorOption<KeyStroke>("key.backward.select", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, SHIFT_MASK)));
  
  /** The key binding for moving the cursor to the beginning of the document
    */
  public static final VectorOption<KeyStroke> KEY_BEGIN_DOCUMENT =
    new VectorOption<KeyStroke>("key.begin.document",
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, MASK)));
  
  /** The key binding for moving the cursor to the beginning of the document */
  public static final VectorOption<KeyStroke> KEY_BEGIN_DOCUMENT_SELECT =
    new VectorOption<KeyStroke>("key.begin.document.select", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, SHIFT_MASK|MASK)));
  
  /** The key binding for moving the cursor to the beginning of the current line. */
  public static final VectorOption<KeyStroke> KEY_BEGIN_LINE =
    new VectorOption<KeyStroke>("key.begin.line",
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0)));
  
  /** The key binding for moving the cursor to the beginning of the current line. */
  public static final VectorOption<KeyStroke> KEY_BEGIN_LINE_SELECT =
    new VectorOption<KeyStroke>("key.begin.line.select", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, SHIFT_MASK)));
  
//  /** The key binding for moving the cursor to the beginning of the current paragraph.
//   * (Doesn't seem to do anything useful...)
//   */
//   public static final VectorOption<KeyStroke> KEY_BEGIN_PARAGRAPH =
//     new VectorOption<KeyStroke>("key.begin.paragraph", new KeyStrokeOption("",null), to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_UP, MASK)));
  
  /** The key binding for moving the cursor to the beginning of the previous word. */
  public static final VectorOption<KeyStroke> KEY_PREVIOUS_WORD =
    new VectorOption<KeyStroke>("key.previous.word", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, MASK)));
  
  /** The key binding for moving the cursor to the beginning of the previous word. */
  public static final VectorOption<KeyStroke> KEY_PREVIOUS_WORD_SELECT =
    new VectorOption<KeyStroke>("key.previous.word.select", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, SHIFT_MASK|MASK)));
  
  /** The key binding for deleting the next character. */
  public static final VectorOption<KeyStroke> KEY_DELETE_NEXT =
    new VectorOption<KeyStroke>("key.delete.next", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)));
  
  /** The key binding for deleting the previous character (with shift set). */
  public static final VectorOption<KeyStroke> KEY_DELETE_PREVIOUS =
    new VectorOption<KeyStroke>("key.delete.previous", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0)));
  
  /** The key binding for deleting the next character (with shift set). */
  public static final VectorOption<KeyStroke> KEY_SHIFT_DELETE_NEXT =
    new VectorOption<KeyStroke>("key.delete.next", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, SHIFT_MASK)));
  
  /** The key binding for deleting the previous character (with shift set). */
  public static final VectorOption<KeyStroke> KEY_SHIFT_DELETE_PREVIOUS =
    new VectorOption<KeyStroke>("key.delete.previous", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, SHIFT_MASK)));
  
  /** The key binding for moving the cursor down. */
  public static final VectorOption<KeyStroke> KEY_DOWN =
    new VectorOption<KeyStroke>("key.down", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)));
  
  /** The key binding for moving the cursor down. */
  public static final VectorOption<KeyStroke> KEY_DOWN_SELECT =
    new VectorOption<KeyStroke>("key.down.select", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, SHIFT_MASK)));
  
  /** The key binding for moving the cursor up. */
  public static final VectorOption<KeyStroke> KEY_UP =
    new VectorOption<KeyStroke>("key.up", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)));
  
  /** The key binding for moving the cursor up. */
  public static final VectorOption<KeyStroke> KEY_UP_SELECT =
    new VectorOption<KeyStroke>("key.up.select",
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_UP, SHIFT_MASK)));
  
  /** The key binding for moving the cursor to the end of the document. */
  public static final VectorOption<KeyStroke> KEY_END_DOCUMENT =
    new VectorOption<KeyStroke>("key.end.document", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_END, MASK)));
  
  /** The key binding for moving the cursor to the end of the document. */
  public static final VectorOption<KeyStroke> KEY_END_DOCUMENT_SELECT =
    new VectorOption<KeyStroke>("key.end.document.select", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_END, SHIFT_MASK|MASK)));
  
  /** The key binding for moving the cursor to the end of the current line. */
  public static final VectorOption<KeyStroke> KEY_END_LINE =
    new VectorOption<KeyStroke>("key.end.line", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0)));
  
  /** The key binding for moving the cursor to the end of the current line. */
  public static final VectorOption<KeyStroke> KEY_END_LINE_SELECT =
    new VectorOption<KeyStroke>("key.end.line.select", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_END, SHIFT_MASK)));
  
//  /** The key binding for moving the cursor to the end of the current paragraph. */
//  public static final VectorOption<KeyStroke> KEY_END_PARAGRAPH =
//    new VectorOption<KeyStroke>("key.end.paragraph", 
//                                new KeyStrokeOption("",null), 
//                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, MASK)));
  
  /** The key binding for moving the cursor to the beginning of the next word. */
  public static final VectorOption<KeyStroke> KEY_NEXT_WORD =
    new VectorOption<KeyStroke>("key.next.word", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, MASK)));
  
  /** The key binding for moving the cursor to the beginning of the next word. */
  public static final VectorOption<KeyStroke> KEY_NEXT_WORD_SELECT =
    new VectorOption<KeyStroke>("key.next.word.select",
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, SHIFT_MASK|MASK)));
  
  /** The key binding for moving the cursor forwards. */
  public static final VectorOption<KeyStroke> KEY_FORWARD =
    new VectorOption<KeyStroke>("key.forward", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0)));
  
  /** The key binding for moving the cursor forwards. */
  public static final VectorOption<KeyStroke> KEY_FORWARD_SELECT =
    new VectorOption<KeyStroke>("key.forward.select", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, SHIFT_MASK)));
  
  /** The key binding for page down. */
  public static final VectorOption<KeyStroke> KEY_PAGE_DOWN =
    new VectorOption<KeyStroke>("key.page.down",
                                new KeyStrokeOption("",null),
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0)));
  
  /** The key binding for page up. */
  public static final VectorOption<KeyStroke> KEY_PAGE_UP =
    new VectorOption<KeyStroke>("key.page.up", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0)));
  
//  public static final VectorOption<KeyStroke> KEY_NEXT_RECENT_DOCUMENT = // Key code for '`'
//    new VectorOption<KeyStroke>("key.next.recent.document", 
//                                new KeyStrokeOption("",null), 
//                                to.vector(KeyStroke.getKeyStroke(KeyEvent.BACK_QUOTE, CTRL_MASK)));  
  
//  public static final VectorOption<KeyStroke> KEY_PREV_RECENT_DOCUMENT =   // Key code for '~'
//    new VectorOption<KeyStroke>("key.prev.recent.document", 
//                                new KeyStrokeOption("",null), 
//                                to.vector(KeyStroke.getKeyStroke(KeyEvent.BACK_QUOTE, SHIFT_MASK | CTRL_MASK))); 
  
  /** The key binding for cutting a line. */
  public static final VectorOption<KeyStroke> KEY_CUT_LINE =
    new VectorOption<KeyStroke>("key.cut.line", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_K, MASK|ALT_MASK)));
  
  /** The key binding for clearing a line, emacs-style. */
  public static final VectorOption<KeyStroke> KEY_CLEAR_LINE =
    new VectorOption<KeyStroke>("key.clear.line", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_K, MASK)));
  
  /** The key binding for toggling debug mode. */
  public static final VectorOption<KeyStroke> KEY_DEBUG_MODE_TOGGLE =
    new VectorOption<KeyStroke>("key.debug.mode.toggle", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_D, MASK | SHIFT_MASK)));
  
//  /** The key binding for suspending the debugger. */
//  public static final VectorOption<KeyStroke> KEY_DEBUG_SUSPEND =
//    new VectorOption<KeyStroke>("key.debug.suspend", 
//                                new KeyStrokeOption("",null), 
//                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0)));
  
  /** The key binding for resuming the debugger. */
  public static final VectorOption<KeyStroke> KEY_DEBUG_RESUME =
    new VectorOption<KeyStroke>("key.debug.resume", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0)));
  
  /** The key binding for automatically tracing through each line of a program*/
  public static final VectorOption<KeyStroke> KEY_DEBUG_AUTOMATIC_TRACE = 
    new VectorOption<KeyStroke>("key.debug.automatic.trace", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0)));
  
  /** The key binding for stepping into in the debugger */
  public static final VectorOption<KeyStroke> KEY_DEBUG_STEP_INTO =
    new VectorOption<KeyStroke>("key.debug.step.into", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0)));
  
  /** The key binding for stepping over in the debugger. */
  public static final VectorOption<KeyStroke> KEY_DEBUG_STEP_OVER =
    new VectorOption<KeyStroke>("key.debug.step.over", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0)));
  
  /** The key binding for stepping out in the debugger. */
  public static final VectorOption<KeyStroke> KEY_DEBUG_STEP_OUT =
    new VectorOption<KeyStroke>("key.debug.step.out", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F12, SHIFT_MASK)));
  
  /** The key binding for toggling a breakpoint. */
  public static final VectorOption<KeyStroke> KEY_DEBUG_BREAKPOINT_TOGGLE =
    new VectorOption<KeyStroke>("key.debug.breakpoint.toggle",
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_B, MASK)));
  
  /** The key binding for displaying the breakpoints panel. */
  public static final VectorOption<KeyStroke> KEY_DEBUG_BREAKPOINT_PANEL =
    new VectorOption<KeyStroke>("key.debug.breakpoint.panel", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_B, MASK | SHIFT_MASK)));
  
  /** The key binding for clearing all breakpoints. */
  public static final VectorOption<KeyStroke> KEY_DEBUG_CLEAR_ALL_BREAKPOINTS =
    new VectorOption<KeyStroke>("key.debug.clear.all.breakpoints", 
                                new KeyStrokeOption("",null), 
                                to.vector());
  
  /** The key binding for toggling a bookmark. */
  public static final VectorOption<KeyStroke> KEY_BOOKMARKS_TOGGLE =
    new VectorOption<KeyStroke>("key.bookmarks.toggle", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_M, MASK)));
  
  /** The key binding for displaying the bookmarks panel. */
  public static final VectorOption<KeyStroke> KEY_BOOKMARKS_PANEL =
    new VectorOption<KeyStroke>("key.bookmarks.panel", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_M, MASK | SHIFT_MASK)));
  
  /** The key binding for help. */
  public static final VectorOption<KeyStroke> KEY_HELP =
    new VectorOption<KeyStroke>("key.help",
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0)));
  
  /** The key binding for quickstart. Currently set to the null keystroke. */
  public static final VectorOption<KeyStroke> KEY_QUICKSTART = 
    new VectorOption<KeyStroke>("key.quickstart", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for the about dialog */
  public static final VectorOption<KeyStroke> KEY_ABOUT = 
    new VectorOption<KeyStroke>("key.about", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for the check new version dialog */
  public static final VectorOption<KeyStroke> KEY_CHECK_NEW_VERSION = 
    new VectorOption<KeyStroke>("key.check.new.version", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for the DrJava survey dialog */
  public static final VectorOption<KeyStroke> KEY_DRJAVA_SURVEY = 
    new VectorOption<KeyStroke>("key.drjava.survey", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for the "DrScala Errors" dialog */
  public static final VectorOption<KeyStroke> KEY_DRJAVA_ERRORS = 
    new VectorOption<KeyStroke>("key.drjava.errors", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for following a file, like using "less" and F. */
  public static final VectorOption<KeyStroke> KEY_FOLLOW_FILE =
    new VectorOption<KeyStroke>("key.follow.file",
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_L, MASK | SHIFT_MASK)));
  
  /** The key binding for executing an external process. */
  public static final VectorOption<KeyStroke> KEY_EXEC_PROCESS =
    new VectorOption<KeyStroke>("key.exec.process", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_X, MASK | SHIFT_MASK)));
  
  /** The key binding to detach/re-attach the tabbed panes. */
  public static final VectorOption<KeyStroke> KEY_DETACH_TABBEDPANES = 
    new VectorOption<KeyStroke>("key.detach.tabbedpanes", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding to detach/re-attach the debugger panel. */
  public static final VectorOption<KeyStroke> KEY_DETACH_DEBUGGER = 
    new VectorOption<KeyStroke>("key.detach.debugger", new KeyStrokeOption("",null), to.vector());
  
  /** The key binging to close stream input in the ineractions panel. Ctrl-D on all systems.
    * In the console on DOS/Windows, this was typically Ctrl-Z, but Ctrl-Z is now the
    * default for Undo, even on Windows. */
  public static final VectorOption<KeyStroke> KEY_CLOSE_SYSTEM_IN = 
    new VectorOption<KeyStroke>("key.close.system.in", 
                                new KeyStrokeOption("",null), 
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_D, CTRL_MASK)));
  
  /** Keystroke option for KeyStrokeOptionComponentTest. */
  public static final KeyStrokeOption KEY_FOR_UNIT_TESTS_ONLY = 
    new KeyStrokeOption("key.for.unit.tests.only", KeyStroke.getKeyStroke(KeyEvent.VK_N, CTRL_MASK|SHIFT_MASK|MASK));

  /** The key binding for the GenerateCustomDrJavaJarFrame. */
  public static final VectorOption<KeyStroke> KEY_GENERATE_CUSTOM_DRJAVA = 
    new VectorOption<KeyStroke>("key.generate.custom.drjava", new KeyStrokeOption("",null), to.vector());
  
  /** The key binding for starting a new, blank DrJava instance. */
  public static final VectorOption<KeyStroke> KEY_NEW_DRJAVA_INSTANCE= 
    new VectorOption<KeyStroke>("key.new.drjava.instance", new KeyStrokeOption("",null),
                                to.vector(KeyStroke.getKeyStroke(KeyEvent.VK_F1, CTRL_MASK|SHIFT_MASK)));
  
  /* ---------- Find Replace Options ---------- */
  
  public static final BooleanOption FIND_MATCH_CASE = 
    new BooleanOption("find.replace.match.case", Boolean.TRUE);
  
  public static final BooleanOption FIND_SEARCH_BACKWARDS = 
    new BooleanOption("find.replace.search.backwards", Boolean.FALSE);
  
  public static final BooleanOption FIND_WHOLE_WORD = 
    new BooleanOption("find.replace.whole.word", Boolean.FALSE);
  
  public static final BooleanOption FIND_ALL_DOCUMENTS = 
    new BooleanOption("find.replace.all.documents", Boolean.FALSE);
  
  public static final BooleanOption FIND_ONLY_SELECTION = 
    new BooleanOption("find.replace.only.selection", Boolean.FALSE);
  
  public static final BooleanOption FIND_NO_COMMENTS_STRINGS =
    new BooleanOption("find.replace.no.comments.strings", Boolean.FALSE);
  
  public static final BooleanOption FIND_NO_TEST_CASES =
    new BooleanOption("find.replace.no.test.cases", Boolean.FALSE);
  
  /* ---------- Debugger Options ---------- */
  
  /** A classpath-structured vector of all paths to look for source files on while stepping in the debugger. */
  public static final VectorOption<File> DEBUG_SOURCEPATH =
    new ClassPathOption().evaluate("debug.sourcepath");
  
  /** Whether stepping should step through Java's source files. */
  public static final BooleanOption DEBUG_STEP_JAVA = new BooleanOption("debug.step.java", Boolean.FALSE);
  
  /** Whether stepping should step through Dynamic Java's source files. */
  public static final BooleanOption DEBUG_STEP_INTERPRETER =
    new BooleanOption("debug.step.interpreter", Boolean.FALSE);
  
  /** Whether stepping should step through DrJava's source files. */
  public static final BooleanOption DEBUG_STEP_DRJAVA =
    new BooleanOption("debug.step.drjava", Boolean.FALSE);
  
  /** Which packages to exclude when stepping. */
  public static final VectorOption<String> DEBUG_STEP_EXCLUDE =
    new VectorOption<String>("debug.step.exclude", new StringOption("",null), new Vector<String>());
  
  /** Whether we want to automatically import packages after breakpoints or steps. */
  public static final BooleanOption DEBUG_AUTO_IMPORT =
    new BooleanOption("debug.auto.import", Boolean.TRUE);
  
  /** Whether we want to allow expressions and method calls in watches. */
  public static final BooleanOption DEBUG_EXPRESSIONS_AND_METHODS_IN_WATCHES =
    new BooleanOption("debug.expressions.and.methods.in.watches", Boolean.FALSE);
  
  
  /* ---------- Javadoc Options ---------- */
  
  /** Possible options for Javadoc access levels. */
  static final ArrayList<String> accessLevelChoices =
    AccessLevelChoices.evaluate();
  public static class AccessLevelChoices {
    public static final String PUBLIC = "public";
    public static final String PROTECTED = "protected";
    public static final String PACKAGE = "package";
    public static final String PRIVATE = "private";
    public static ArrayList<String> evaluate() {
      ArrayList<String> aList = new ArrayList<String>(4);
      aList.add(PUBLIC);
      aList.add(PROTECTED);
      aList.add(PACKAGE);
      aList.add(PRIVATE);
      return aList;
    }
  }
  
  /** The lowest access level of classes and members to include in the javadoc. */
  public static final ForcedChoiceOption JAVADOC_ACCESS_LEVEL =
    new ForcedChoiceOption("javadoc.access.level", AccessLevelChoices.PACKAGE, accessLevelChoices);
  
  /** Possible options for Javadoc system class documentation links. */
  static final String JAVADOC_NONE_TEXT = "none";
  static final String JAVADOC_1_3_TEXT = "1.3";
  static final String JAVADOC_1_4_TEXT = "1.4";
  static final String JAVADOC_1_5_TEXT = "1.5";
  static final String JAVADOC_1_6_TEXT = "1.6";
  static final String JAVADOC_1_7_TEXT = "1.7";
  static final String JAVADOC_AUTO_TEXT = "use compiler version"; // for "Open Java API Javadoc"
  
  static final String[] linkChoices = new String[]{
    JAVADOC_NONE_TEXT, JAVADOC_1_5_TEXT, JAVADOC_1_6_TEXT, JAVADOC_1_7_TEXT };
  static final ArrayList<String> linkVersionChoices = new ArrayList<String>(Arrays.asList(linkChoices));

  static final String[] linkDeprecated = new String[]{
    JAVADOC_1_3_TEXT, JAVADOC_1_4_TEXT };
  static final ArrayList<String> linkVersionDeprecated = new ArrayList<String>(Arrays.asList(linkDeprecated));  
  
  /** Constants for the URLs of Sun's system class documentation for different versions of Java. */
  public static final StringOption JAVADOC_1_3_LINK =
    new StringOption("javadoc.1.3.link", "http://download.oracle.com/javase/1.3/docs/api");
  public static final StringOption JAVADOC_1_4_LINK =
    new StringOption("javadoc.1.4.link", "http://download.oracle.com/javase/1.4.2/docs/api");
  public static final StringOption JAVADOC_1_5_LINK =
    new StringOption("javadoc.1.5.link", "http://download.oracle.com/javase/1.5.0/docs/api");
  public static final StringOption JAVADOC_1_6_LINK =
    new StringOption("javadoc.1.6.link", "http://download.oracle.com/javase/6/docs/api");
  public static final StringOption JAVADOC_1_7_LINK =
    new StringOption("javadoc.1.7.link", "http://download.oracle.com/javase/7/docs/api/");
  
  /** The version of Java to use for links to Javadoc for system classes. */
  public static final ForcedChoiceOption JAVADOC_LINK_VERSION =
    new ForcedChoiceOption("javadoc.link.version",
                           (System.getProperty("java.specification.version").startsWith("1.5") ? JAVADOC_1_5_TEXT : 
                              (System.getProperty("java.specification.version").startsWith("1.6") ? JAVADOC_1_6_TEXT : 
                                 JAVADOC_1_7_TEXT)),
                           linkVersionChoices, linkVersionDeprecated);
  
  static final String[] apiJavadocChoices = new String[] {
    JAVADOC_1_5_TEXT, JAVADOC_1_6_TEXT, JAVADOC_1_7_TEXT, JAVADOC_AUTO_TEXT};
  static final ArrayList<String> apiJavadocVersionChoices = new ArrayList<String>(Arrays.asList(apiJavadocChoices));

  static final String[] apiJavadocDeprecated = new String[] {
    JAVADOC_1_3_TEXT, JAVADOC_1_4_TEXT}; // deprecated, will be changed to JAVADOC_AUTO_TEXT
  static final ArrayList<String> apiJavadocVersionDeprecated = new ArrayList<String>(Arrays.asList(apiJavadocDeprecated));  
  
  /** The version of Java to use for the "Open Java API Javadoc" feature. */
  public static final ForcedChoiceOption JAVADOC_API_REF_VERSION =
    new ForcedChoiceOption("javadoc.api.ref.version", JAVADOC_AUTO_TEXT,
                           apiJavadocVersionChoices, apiJavadocVersionDeprecated);
  
  /** URL for JUnit javadocs. */
  public static final StringOption JUNIT_LINK =
    new StringOption("junit.link", "http://www.cs.rice.edu/~javaplt/javadoc/concjunit4.7");
  
  /** Additional Javadoc URLs. */
  public static final VectorOption<String> JAVADOC_ADDITIONAL_LINKS =
    new VectorOption<String>("javadoc.additional.links", new StringOption("",null), new Vector<String>());
  
  /** Whether to include the entire package heirarchy from the source roots when generating JavaDoc output. */
  public static final BooleanOption JAVADOC_FROM_ROOTS = new BooleanOption("javadoc.from.roots", Boolean.FALSE);
  
  /** A string containing custom options to be passed to Javadoc. This string needs to be tokenized before passing it to 
    * Javadoc.
    */
  public static final StringOption JAVADOC_CUSTOM_PARAMS = 
    new StringOption("javadoc.custom.params", "-author -version");
  
  /** The default destination directory for Javadoc output. */
  public static final FileOption JAVADOC_DESTINATION = new FileOption("javadoc.destination", FileOps.NULL_FILE);
  
  /** Whether to always prompt for a destination directory, whether or not a default has been set. */
  public static final BooleanOption JAVADOC_PROMPT_FOR_DESTINATION =
    new BooleanOption("javadoc.prompt.for.destination", Boolean.TRUE);
  
  /* ---------- NOTIFICATION OPTIONS ---------- */
  
  /** Whether to prompt when the interactions pane is unexpectedly reset. */
  public static final BooleanOption INTERACTIONS_EXIT_PROMPT =
    new BooleanOption("interactions.exit.prompt", Boolean.TRUE);
  
  /** Whether to prompt before quitting DrJava. */
  public static final BooleanOption QUIT_PROMPT = new BooleanOption("quit.prompt", Boolean.TRUE);
  
  /** Whether to prompt before resetting the interactions pane. */
  public static final BooleanOption INTERACTIONS_RESET_PROMPT =
    new BooleanOption("interactions.reset.prompt", Boolean.TRUE);
  
  /** Whether to prompt to save before compiling. */
  public static final BooleanOption ALWAYS_SAVE_BEFORE_COMPILE =
    new BooleanOption("save.before.compile", Boolean.FALSE);
  
  /** Whether to prompt to save before running. */
  public static final BooleanOption ALWAYS_SAVE_BEFORE_RUN =
    new BooleanOption("save.before.run", Boolean.FALSE);
  
  /** Whether to prompt to save before testing. */
  public static final BooleanOption ALWAYS_COMPILE_BEFORE_JUNIT =
    new BooleanOption("compile.before.junit", Boolean.FALSE);
  
  /** Whether to prompt to save before compiling. */
  public static final BooleanOption ALWAYS_SAVE_BEFORE_JAVADOC =
    new BooleanOption("save.before.javadoc", Boolean.FALSE);

  /** Whether to prompt to compile before compiling. */
  public static final BooleanOption ALWAYS_COMPILE_BEFORE_JAVADOC =
    new BooleanOption("compile.before.javadoc", Boolean.FALSE);
  
  /** Whether to prompt to save before compiling. */
  public static final BooleanOption ALWAYS_SAVE_BEFORE_DEBUG =
    new BooleanOption("save.before.debug", Boolean.FALSE);
  
  /** Whether to warn if a document has been modified before allowing the user to set a breakpoint in it. */
  public static final BooleanOption WARN_BREAKPOINT_OUT_OF_SYNC =
    new BooleanOption("warn.breakpoint.out.of.sync", Boolean.TRUE);
  
  /** Whether to warn that the user is debugging a file that is out of sync with its class file. */
  public static final BooleanOption WARN_DEBUG_MODIFIED_FILE =
    new BooleanOption("warn.debug.modified.file", Boolean.TRUE);
  
  /** Whether to warn that a restart is necessary before the look and feel will change. */
  public static final BooleanOption WARN_CHANGE_LAF = new BooleanOption("warn.change.laf", Boolean.TRUE);
  
  /** Whether to warn that a restart is necessary before the theme will change. */
  public static final BooleanOption WARN_CHANGE_THEME = new BooleanOption("warn.change.theme", Boolean.TRUE);
  
  /** Whether to warn that a restart is necessary before these miscellaneous preferences will change. */
  public static final BooleanOption WARN_CHANGE_MISC = new BooleanOption("warn.change.misc", Boolean.TRUE);
  
  /** Whether to warn that a reset is necessary before these Interactions Pane preferences will change. */
  public static final BooleanOption WARN_CHANGE_INTERACTIONS = new BooleanOption("warn.change.interactions", Boolean.TRUE);
  
  /** Whether to warn that a file's path contains a "#' symbol. */
  public static final BooleanOption WARN_PATH_CONTAINS_POUND =
    new BooleanOption("warn.path.contains.pound", Boolean.TRUE);
  
  /** Whether to warn that a restart is necessary before the default compiler preference will change. */
  public static final BooleanOption WARN_CHANGE_DCP = new BooleanOption("warn.change.dcp", Boolean.TRUE);

  /** Whether to prompt to change the language level extensions (.dj0/.dj1->.dj, .dj2->.java). */
  public static final BooleanOption PROMPT_RENAME_LL_FILES = new BooleanOption("prompt.rename.ll.files", Boolean.TRUE);
  
  /* ---------- MISC OPTIONS ---------- */
  
  /** Whether to warn when cleaning the build directory */
  public static final BooleanOption PROMPT_BEFORE_CLEAN = new BooleanOption("prompt.before.clean", Boolean.TRUE);
  
  /** Open directory should default to recursive */
  public static final BooleanOption OPEN_FOLDER_RECURSIVE =  new BooleanOption("open.folder.recursive", Boolean.FALSE);
  
  /** How many spaces to use for indenting. */
  public static final NonNegativeIntegerOption INDENT_LEVEL = 
    new NonNegativeIntegerOption("indent.level", Integer.valueOf(2));
  
  /** Number of lines to remember in the Interactions History */
  public static final NonNegativeIntegerOption HISTORY_MAX_SIZE =
    new NonNegativeIntegerOption("history.max.size", Integer.valueOf(500));
  
  /** Number of files to list in the recent file list */
  public static final NonNegativeIntegerOption RECENT_FILES_MAX_SIZE =
    new NonNegativeIntegerOption("recent.files.max.size", Integer.valueOf(5));
  
  /** Whether to automatically close comments. */
  public static final BooleanOption AUTO_CLOSE_COMMENTS = new BooleanOption("auto.close.comments", Boolean.FALSE);
  
  /** Whether to clear the console when manually resetting the interactions pane. */
  public static final BooleanOption RESET_CLEAR_CONSOLE = new BooleanOption("reset.clear.console", Boolean.TRUE);
  
  /** Whether to run assert statements in the interactions pane. */
  public static final BooleanOption RUN_WITH_ASSERT = new BooleanOption("run.with.assert", Boolean.TRUE);

  /** Whether the java command should automatically detect applets and acm.program.Program subclasses. */
  public static final BooleanOption SMART_RUN_FOR_APPLETS_AND_PROGRAMS =
    new BooleanOption("smart.run.for.applets.and.programs", Boolean.TRUE);
  
  /** Whether to make emacs-style backup files. */
  public static final BooleanOption BACKUP_FILES = new BooleanOption("files.backup", Boolean.TRUE);
  
  /** Whether to allow users to access to all members in the Interactions Pane.
    * This should not be used anymore. Instead, use DYNAMICJAVA_ACCESS_CONTROL. */
  @Deprecated public static final BooleanOption ALLOW_PRIVATE_ACCESS = new BooleanOption("allow.private.access", Boolean.FALSE);
  
  /** Whether to force test classes in projects to end in "Test". */
  public static final BooleanOption FORCE_TEST_SUFFIX = new BooleanOption("force.test.suffix", Boolean.FALSE);
  
  /** Whether remote control using sockets is enabled. */
  public static final BooleanOption REMOTE_CONTROL_ENABLED = new BooleanOption("remote.control.enabled", Boolean.TRUE);
  
  /** The port where DrJava will listen for remote control requests. */
  public static final IntegerOption REMOTE_CONTROL_PORT = new IntegerOption("remote.control.port", Integer.valueOf(4444));
  
  /** Whether to warn if Compiz is being used */
  public static final BooleanOption WARN_IF_COMPIZ = new BooleanOption("warn.if.compiz", Boolean.TRUE);
  
  /* ---------- COMPILER OPTIONS ------------- */
  
  /** Whether to show unchecked warnings */
  public static final BooleanOption SHOW_UNCHECKED_WARNINGS = 
    new BooleanOption("show.unchecked.warnings", Boolean.TRUE);
  
  /** Whether to show deprecation warnings */
  public static final BooleanOption SHOW_DEPRECATION_WARNINGS = 
    new BooleanOption("show.deprecation.warnings", Boolean.TRUE);
  
  /** Whether to show finally warnings */
  public static final BooleanOption SHOW_FINALLY_WARNINGS = new BooleanOption("show.finally.warnings", Boolean.FALSE);
  
  /** Whether to show serial warnings */
  public static final BooleanOption SHOW_SERIAL_WARNINGS = 
    new BooleanOption("show.serial.warnings", Boolean.FALSE);
  
  /** Whether to show serial warnings */
  public static final BooleanOption SHOW_FALLTHROUGH_WARNINGS = 
    new BooleanOption("show.fallthrough.warnings", Boolean.FALSE);
  
  /** Whether to show serial warnings */
  public static final BooleanOption SHOW_PATH_WARNINGS = 
    new BooleanOption("show.path.warnings", Boolean.FALSE);
  
  /** Default compiler to use
    * Stores the name of the compiler to use, set by changing the selection in
    * the ForcedChoiceOption created by COMPILER_PREFERENCE_CONTROL.evaluate()
    */
  public static final StringOption DEFAULT_COMPILER_PREFERENCE = 
    new StringOption("default.compiler.preference", COMPILER_PREFERENCE_CONTROL.SCALA);  // NO_PREFERENCE in DrJava
  
  /** Class that is used to dynamically populate the ForcedChoiceOption.
    * setList method is used by DefaultCompilerModel to set the available compilers that it has
    * Must store the selected name into DEFAULT_COMPILER_PREFERENCE to save the setting
    */
  public static final class COMPILER_PREFERENCE_CONTROL {
    public static final String NO_PREFERENCE = "No Preference";
    public static final String SCALA = "Scala version 2.9";
    public static ArrayList<String> _list = new ArrayList<String>();
    
    public static void setList(ArrayList<String> list) { _list = list; }
    public static ForcedChoiceOption evaluate() {
      if (! _list.contains(NO_PREFERENCE)) _list.add(NO_PREFERENCE);
      
      ForcedChoiceOption fco;
      String defaultC = edu.rice.cs.drjava.DrJava.getConfig().getSetting(DEFAULT_COMPILER_PREFERENCE);
 
      if (_list.contains(defaultC))  {
        fco = new ForcedChoiceOption("compiler.preference.control", defaultC, _list);
      }
      else {
        fco = new ForcedChoiceOption("compiler.preference.control", NO_PREFERENCE, _list);
        edu.rice.cs.drjava.DrJava.getConfig().setSetting(DEFAULT_COMPILER_PREFERENCE,NO_PREFERENCE);
      }
      
      edu.rice.cs.drjava.DrJava.getConfig().setSetting(fco, edu.rice.cs.drjava.DrJava.getConfig().getSetting(DEFAULT_COMPILER_PREFERENCE));
      return fco;
    }
  }
  
  /* ---------- UNDISPLAYED OPTIONS ---------- */
  
  /** The language level to use when starting DrJava.  Stores the most recently used one.  Defaults to Scala. */
  public static final IntegerOption LANGUAGE_LEVEL = new IntegerOption("language.level", Integer.valueOf(0));
  
  /** A vector containing the most recently used files. */
  public static final VectorOption<File> RECENT_FILES =
    new VectorOption<File>("recent.files", new FileOption("", null), new Vector<File>());
  
  /** A vector containing the most recently used projects. */
  public static final VectorOption<File> RECENT_PROJECTS =
    new VectorOption<File>("recent.projects", new FileOption("", null), new Vector<File>());
  
  /** Whether to enabled the Show Debug Console menu item in the Tools menu. */
  public static final BooleanOption SHOW_DEBUG_CONSOLE = new BooleanOption("show.debug.console", Boolean.FALSE);
  
  /** Height of MainFrame at startUp.  Can be overridden if out of bounds. */
  public static final NonNegativeIntegerOption WINDOW_HEIGHT =
    new NonNegativeIntegerOption("window.height", Integer.valueOf(700));
  
  /** Width of MainFrame at startUp.  Can be overridden if out of bounds. */
  public static final NonNegativeIntegerOption WINDOW_WIDTH =
    new NonNegativeIntegerOption("window.width", Integer.valueOf(800));
  
  /** X position of MainFrame at startUp.  Can be overridden if out of bounds. This value can legally be negative in a
    * multi-screen setup.
    */
  public static final IntegerOption WINDOW_X = new IntegerOption("window.x",  Integer.valueOf(Integer.MAX_VALUE));
  
  /** Y position of MainFrame at startUp.  Can be overridden if out of bounds. This value can legally be negative in a
    * multi-screen setup.
    */
  public static final IntegerOption WINDOW_Y = new IntegerOption("window.y", Integer.valueOf(Integer.MAX_VALUE));
  
  /** The window state (maxamized or normal). The current window state
    * is saved on shutdown.
    */
  public static final IntegerOption WINDOW_STATE =
    new IntegerOption("window.state", Integer.valueOf(Frame.NORMAL));
  
  /** Width of DocList at startUp.  Must be less than WINDOW_WIDTH. Can be overridden if out of bounds. */
  public static final NonNegativeIntegerOption DOC_LIST_WIDTH =
    new NonNegativeIntegerOption("doc.list.width", Integer.valueOf(150));
  
  /** Height of tabbed panel at startUp.  Must be less than WINDOW_HEIGHT + DEBUG_PANEL_HEIGHT.  Can be overridden if 
    * out of bounds.
    */
  public static final NonNegativeIntegerOption TABS_HEIGHT =
    new NonNegativeIntegerOption("tabs.height", Integer.valueOf(120));
  
  /** Height of debugger panel at startUp.  Must be less than WINDOW_HEIGHT + TABS_HEIGHT.  Can be overridden if out of
    * bounds.
    */
  public static final NonNegativeIntegerOption DEBUG_PANEL_HEIGHT =
    new NonNegativeIntegerOption("debug.panel.height", Integer.valueOf(0));
  
  /** The directory in use by the file choosers upon the previous quit. */
  public static final FileOption LAST_DIRECTORY = new FileOption("last.dir", FileOps.NULL_FILE);
  
  /** The directory in use by the Interactions pane upon the previous quit. */
  public static final FileOption LAST_INTERACTIONS_DIRECTORY = new FileOption("last.interactions.dir", FileOps.NULL_FILE);
  
  /** The directory for the Interactions pane to use (as long as there is no project working directory). */
  public static final FileOption FIXED_INTERACTIONS_DIRECTORY = new FileOption("fixed.interactions.dir", FileOps.NULL_FILE);
  
  /** Whether to save and restore Interactions pane directory at startUp/shutdown (sticky=true), or to use
    * "user.home" (sticky=false). */
  public static final BooleanOption STICKY_INTERACTIONS_DIRECTORY =
    new BooleanOption("sticky.interactions.dir", Boolean.TRUE);
  
  /** Whether to require a semicolon at the end of statements in the Interactions Pane. */
  public static final BooleanOption DYNAMICJAVA_REQUIRE_SEMICOLON =
    new BooleanOption("dynamicjava.require.semicolon", Boolean.FALSE);
  
  /** Whether to require a variable type for variable declarations in the Interactions Pane. */
  public static final BooleanOption DYNAMICJAVA_REQUIRE_VARIABLE_TYPE =
    new BooleanOption("dynamicjava.require.variable.type", Boolean.TRUE);
  
  
  /** Dynamic Java access control. */
  public static final ArrayList<String> DYNAMICJAVA_ACCESS_CONTROL_CHOICES =
    DynamicJavaAccessControlChoices.evaluate();
  public static class DynamicJavaAccessControlChoices {
    public static final String DISABLED = "disabled";
    public static final String PRIVATE = "private only";
    public static final String PRIVATE_AND_PACKAGE = "private and package only";
    public static ArrayList<String> evaluate() {
      ArrayList<String> aList = new ArrayList<String>(4);
      aList.add(DISABLED);
      aList.add(PRIVATE);
      
      // NOTE: this sets the enforceAllAccess option in InteractionsPaneOptions, but since that is not fully
      // implemented, this description is better.
      aList.add(PRIVATE_AND_PACKAGE); 
      return aList;
    }
  }
  
  /** File extension registration. */
  public static final ForcedChoiceOption DYNAMICJAVA_ACCESS_CONTROL =
    new ForcedChoiceOption("dynamicjava.access.control", DynamicJavaAccessControlChoices.PRIVATE_AND_PACKAGE,
                           DYNAMICJAVA_ACCESS_CONTROL_CHOICES);
  
  /** The command-line arguments to be passed to the Master JVM. */
  public static final StringOption MASTER_JVM_ARGS = new StringOption("master.jvm.args", "");
  
  /** The command-line arguments to be passed to the Slave JVM. */
  public static final StringOption SLAVE_JVM_ARGS = new StringOption("slave.jvm.args", "");
  
  /* Possible maximum heap sizes. */
  public static final ArrayList<String> heapSizeChoices = HeapSizeChoices.evaluate();
  static class HeapSizeChoices {
    public static ArrayList<String> evaluate() {
      ArrayList<String> aList = new ArrayList<String>(4);
      aList.add("default");
      aList.add("64");
      aList.add("128");
      aList.add("256");
      aList.add("512");
      aList.add("768");
      aList.add("1024");
      aList.add("1536");
      aList.add("2048");
      aList.add("2560");
      aList.add("3072");
      aList.add("3584");
      aList.add("4096");
      return aList;
    }
  }
  
  /** The command-line arguments for the maximum heap size (-Xmx___) to be passed to the Master JVM. */
  public static final ForcedChoiceOption MASTER_JVM_XMX =
    new ForcedChoiceOption("master.jvm.xmx", "default", heapSizeChoices);
  
  /** The command-line arguments for the maximum heap size (-Xmx___) to be passed to the Slave JVM. */
  public static final ForcedChoiceOption SLAVE_JVM_XMX =
    new ForcedChoiceOption("slave.jvm.xmx", "default", heapSizeChoices);
  
  /** The last state of the "Clipboard History" dialog. */
  public static final StringOption DIALOG_CLIPBOARD_HISTORY_STATE = new StringOption("dialog.clipboard.history.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_CLIPBOARD_HISTORY_STORE_POSITION =
    new BooleanOption("dialog.clipboardhistory.store.position", Boolean.TRUE);
  
  /** How many entries are kept in the clipboard history. */
  public static final NonNegativeIntegerOption CLIPBOARD_HISTORY_SIZE =
    new NonNegativeIntegerOption("clipboardhistory.store.size", 10);
  
  /** The last state of the "Go to File" dialog. */
  public static final StringOption DIALOG_GOTOFILE_STATE = new StringOption("dialog.gotofile.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_GOTOFILE_STORE_POSITION =
    new BooleanOption("dialog.gotofile.store.position", Boolean.TRUE);
  
  /** The last state of the "Open Javadoc" dialog. */
  public static final StringOption DIALOG_OPENJAVADOC_STATE = new StringOption("dialog.openjavadoc.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_OPENJAVADOC_STORE_POSITION =
    new BooleanOption("dialog.openjavadoc.store.position", Boolean.TRUE);
  
  /** The last state of the "Auto Import" dialog. */
  public static final StringOption DIALOG_AUTOIMPORT_STATE = new StringOption("dialog.autoimport.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_AUTOIMPORT_STORE_POSITION =
    new BooleanOption("dialog.autoimport.store.position", Boolean.TRUE);
  
  /** Number of entries in the browser history (0 for unlimited). */
  public static final NonNegativeIntegerOption BROWSER_HISTORY_MAX_SIZE =
    new NonNegativeIntegerOption("browser.history.max.size", Integer.valueOf(50));
  
  /** Whether to also list files with fully qualified paths.
    */
  public static final BooleanOption DIALOG_GOTOFILE_FULLY_QUALIFIED =
    new BooleanOption("dialog.gotofile.fully.qualified", Boolean.FALSE);
  
  /** The last state of the "Complete File" dialog. */
  public static final StringOption DIALOG_COMPLETE_WORD_STATE = new StringOption("dialog.completeword.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_COMPLETE_WORD_STORE_POSITION =
    new BooleanOption("dialog.completeword.store.position", Boolean.TRUE);
  
  /** Whether to scan class files for auto-completion class names. */
  public static final BooleanOption DIALOG_COMPLETE_SCAN_CLASS_FILES =
    new BooleanOption("dialog.completeword.scan.class.files", Boolean.FALSE);
  
  /** Whether to include Java API classes in auto-completion. */
  public static final BooleanOption DIALOG_COMPLETE_JAVAAPI =
    new BooleanOption("dialog.completeword.javaapi", Boolean.FALSE);
  
// Any lightweight parsing has been disabled until we have something that is beneficial and works better in the background.
  /** Whether to perform light-weight parsing. */
  public static final BooleanOption LIGHTWEIGHT_PARSING_ENABLED =
    new BooleanOption("lightweight.parsing.enabled", Boolean.FALSE);
  
  /** Delay for light-weight parsing. */
  public static final NonNegativeIntegerOption DIALOG_LIGHTWEIGHT_PARSING_DELAY =
    new NonNegativeIntegerOption("lightweight.parsing.delay", Integer.valueOf(500));
  
  /** The last state of the "Tabbed Panes" frame. */
  public static final StringOption DIALOG_TABBEDPANES_STATE = new StringOption("tabbedpanes.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_TABBEDPANES_STORE_POSITION =
    new BooleanOption("tabbedpanes.store.position", Boolean.TRUE);
  
  /** Whether the tabbed pane is detached from the MainFrame. */
  public static final BooleanOption DETACH_TABBEDPANES =
    new BooleanOption("tabbedpanes.detach", Boolean.FALSE);
  
  /** The last state of the "Debugger" frame. */
  public static final StringOption DIALOG_DEBUGFRAME_STATE = new StringOption("debugger.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_DEBUGFRAME_STORE_POSITION =
    new BooleanOption("debugger.store.position", Boolean.TRUE);
  
  /** Whether the debugger is detached from the MainFrame. */
  public static final BooleanOption DETACH_DEBUGGER =
    new BooleanOption("debugger.detach", Boolean.FALSE);
  
  /** The last state of the "Create Jar from Project" dialog. */
  public static final StringOption DIALOG_JAROPTIONS_STATE = new StringOption("dialog.jaroptions.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_JAROPTIONS_STORE_POSITION =
    new BooleanOption("dialog.jaroptions.store.position", Boolean.TRUE);
  
  /** The last state of the "Execute External Process" dialog. */
  public static final StringOption DIALOG_EXTERNALPROCESS_STATE = new StringOption("dialog.externalprocess.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_EXTERNALPROCESS_STORE_POSITION =
    new BooleanOption("dialog.externalprocess.store.position", Boolean.TRUE);
  
  /** The last state of the "Edit External Process" dialog. */
  public static final StringOption DIALOG_EDITEXTERNALPROCESS_STATE = new StringOption("dialog.editexternalprocess.state", "default");
  
  /** Whether to save and restore window size and position at startUp/shutdown. */
  public static final BooleanOption DIALOG_EDITEXTERNALPROCESS_STORE_POSITION =
    new BooleanOption("dialog.editexternalprocess.store.position", Boolean.TRUE);
  
  /** Whether to put the focus in the definitions pane after find/replace. */
  public static final BooleanOption FIND_REPLACE_FOCUS_IN_DEFPANE =
    new BooleanOption("find.replace.focus.in.defpane", Boolean.FALSE);
  
  /** Whether to show a notification popup when the first DrJava error occurs. */
  public static final BooleanOption DIALOG_DRJAVA_ERROR_POPUP_ENABLED =
    new BooleanOption("dialog.drjava.error.popup.enabled", Boolean.TRUE);
  
  /** Whether to ask the user if DrJava may send system information to the DrJava developers. */
  public static final BooleanOption DIALOG_DRJAVA_SURVEY_ENABLED =
    new BooleanOption("dialog.drjava.survey.enabled", Boolean.TRUE);
  
  /** Whether to show the "code preview" popups in the RegionTreePanels (bookmarks, breakpoints, find all). */
  public static final BooleanOption SHOW_CODE_PREVIEW_POPUPS =
    new BooleanOption("show.code.preview.popups", Boolean.TRUE);
  
  /** Whether to use Runtime.halt to quit DrScala (see bugs 1550220 and 1478796). */
  public static final BooleanOption DRJAVA_USE_FORCE_QUIT =
    new BooleanOption("drjava.use.force.quit", Boolean.FALSE);
  
  /** Whether to display the "Auto Import" dialog when an undefined class
    * is encountered in the Interactions Pane. */
  public static final BooleanOption DIALOG_AUTOIMPORT_ENABLED =
    new BooleanOption("dialog.autoimport.enabled", Boolean.TRUE);
  
  /** Delay for following files. */
  public static final NonNegativeIntegerOption FOLLOW_FILE_DELAY =
    new NonNegativeIntegerOption("follow.file.delay", Integer.valueOf(300));
  
  /** Maximum lines to keep when following files, or 0 for unlimited. */
  public static final NonNegativeIntegerOption FOLLOW_FILE_LINES =
    new NonNegativeIntegerOption("follow.file.lines", Integer.valueOf(1000));
  
  /** Prefix for the "external saved" settings. */
  public static final String EXTERNAL_SAVED_PREFIX = "external.saved.";
  
  /** The number of saved external processes. */
  public static final NonNegativeIntegerOption EXTERNAL_SAVED_COUNT =
    new NonNegativeIntegerOption(EXTERNAL_SAVED_PREFIX + "count", Integer.valueOf(0));
  
  /** The names of saved external processes. */
  public static final VectorOption<String> EXTERNAL_SAVED_NAMES =
    new VectorOption<String>(EXTERNAL_SAVED_PREFIX + "names",
                             new StringOption("",""),
                             new Vector<String>());
  
  /** The command lines of saved external processes. */
  public static final VectorOption<String> EXTERNAL_SAVED_CMDLINES =
    new VectorOption<String>(EXTERNAL_SAVED_PREFIX + "cmdlines",
                             new StringOption("",""),
                             new Vector<String>());
  
  /** The work directories of saved external processes. */
  public static final VectorOption<String> EXTERNAL_SAVED_WORKDIRS =
    new VectorOption<String>(EXTERNAL_SAVED_PREFIX + "workdirs",
                             new StringOption("",""),
                             new Vector<String>());
  
  /** The script file (or "" if none) of saved external processes. */
  public static final VectorOption<String> EXTERNAL_SAVED_ENCLOSING_DJAPP_FILES =
    new VectorOption<String>(EXTERNAL_SAVED_PREFIX + "enclosingdjappfiles",
                             new StringOption("",""),
                             new Vector<String>());
  
  /** Notification of new versions. */
  public static final ArrayList<String> NEW_VERSION_NOTIFICATION_CHOICES =
    VersionNotificationChoices.evaluate();
  public static class VersionNotificationChoices {
    public static final String STABLE = "stable versions only";
    public static final String BETA = "stable and beta versions only";
    public static final String ALL_RELEASES = "all release versions";
    public static final String EXPERIMENTAL = "weekly experimental builds";
    public static final String DISABLED = "none (disabled)";
    public static ArrayList<String> evaluate() {
      ArrayList<String> aList = new ArrayList<String>(4);
      aList.add(STABLE);
      aList.add(BETA);
      aList.add(ALL_RELEASES);
      aList.add(EXPERIMENTAL);
      aList.add(DISABLED);
      return aList;
    }
  }
  
  /** The kind of version DrJava should be looking for. */
  public static final ForcedChoiceOption NEW_VERSION_NOTIFICATION =
    new ForcedChoiceOption("new.version.notification", VersionNotificationChoices.BETA, NEW_VERSION_NOTIFICATION_CHOICES);

  /** Whether the new version feature may be used at all. */
  public static final BooleanOption NEW_VERSION_ALLOWED = new BooleanOption("new.version.allowed", Boolean.TRUE);
  
  /** The last time we checked for a new version. */
  public static final LongOption LAST_NEW_VERSION_NOTIFICATION = new LongOption("new.version.notification.last", (long)0);  
  
  /** The number of days that have to pass before we automatically check again. */
  public static final NonNegativeIntegerOption NEW_VERSION_NOTIFICATION_DAYS =
    new NonNegativeIntegerOption("new.version.notification.days", 7);  
  
  /** The number of days that have to pass before we ask and allow the user to participate in the DrJava survey again. */
  public static final NonNegativeIntegerOption DRJAVA_SURVEY_DAYS =
    new NonNegativeIntegerOption("drjava.survey.days", 91); // every three month  
  
  /** The last time we asked the user to participate in the DrJava survey. */
  public static final LongOption LAST_DRJAVA_SURVEY = new LongOption("drjava.survey.notification.last", (long)0);  
  
  /** The request URL that the user generated the last time the DrJava survey was taken. */
  public static final StringOption LAST_DRJAVA_SURVEY_RESULT = new StringOption("drjava.survey.result.last", "");
  
  /** Delete class files for language-level classes. */
  public static final ArrayList<String> DELETE_LL_CLASS_FILES_CHOICES =
    DeleteLLClassFileChoices.evaluate();
  public static class DeleteLLClassFileChoices {
    public static final String NEVER = "never";
    public static final String ASK_ME = "ask me at startup";
    public static final String ALWAYS = "always";
    public static ArrayList<String> evaluate() {
      ArrayList<String> aList = new ArrayList<String>(3);
      aList.add(NEVER);
      aList.add(ASK_ME);
      aList.add(ALWAYS);
      return aList;
    }
  }
  
  /** Whether to delete language level class files. */
  public static final ForcedChoiceOption DELETE_LL_CLASS_FILES =
    new ForcedChoiceOption("delete.ll.class.files", DeleteLLClassFileChoices.ALWAYS, DELETE_LL_CLASS_FILES_CHOICES);
  
  /** File extension registration choices. */
  public static final ArrayList<String> FILE_EXT_REGISTRATION_CHOICES =
    FileExtRegistrationChoices.evaluate();
  public static class FileExtRegistrationChoices {
    public static final String NEVER = "never";
    public static final String ASK_ME = "ask me at startup";
    public static final String ALWAYS = "always";
    public static ArrayList<String> evaluate() {
      ArrayList<String> aList = new ArrayList<String>(4);
      aList.add(NEVER);
      aList.add(ASK_ME);
      aList.add(ALWAYS);
      return aList;
    }
  }
  
  /** File extension registration. */
  public static final ForcedChoiceOption FILE_EXT_REGISTRATION =
    new ForcedChoiceOption("file.ext.registration", FileExtRegistrationChoices.ASK_ME,
                           FILE_EXT_REGISTRATION_CHOICES);
  
  /** JUnit/ConcJUnit. */
  
  /** junitrt.jar/concutest-junit-3.8.2-withrt.jar location, or NULL_FILE if not specified. */
  public static final FileOption JUNIT_LOCATION = new FileOption("junit.location", FileOps.NULL_FILE);
  
  /** True if the JUnit jar in JUNIT_LOCATION should be used. */
  public static final BooleanOption JUNIT_LOCATION_ENABLED = new BooleanOption("junit.location.enabled", Boolean.FALSE);
  
  /** ConcJUnit processed Java Runtime (rt.concjunit.jar) location, or NULL_FILE if not specified. */
  public static final FileOption RT_CONCJUNIT_LOCATION = new FileOption("rt.concjunit.location", FileOps.NULL_FILE);
  
  /** Possible options for Javadoc access levels. */
  static final ArrayList<String> concJUnitCheckChoices =
    ConcJUnitCheckChoices.evaluate();
  public static class ConcJUnitCheckChoices {
    public static final String ALL = "all-threads, no-join, lucky";
    public static final String NO_LUCKY = "all-threads, no-join";
    public static final String ONLY_THREADS = "all-threads";
    public static final String NONE = "none (use JUnit)";
    public static ArrayList<String> evaluate() {
      ArrayList<String> aList = new ArrayList<String>(4);
      aList.add(ALL);
      aList.add(NO_LUCKY);
      aList.add(ONLY_THREADS);
      aList.add(NONE);
      return aList;
    }
  }
  
  /** The concurrent test checks that ConcJUnit should perform. */
  public static final ForcedChoiceOption CONCJUNIT_CHECKS_ENABLED =
    new ForcedChoiceOption("concjunit.checks.enabled", ConcJUnitCheckChoices.NONE, concJUnitCheckChoices);

  /** A version suffix that describes custom additions to DrJava. */
  public static final StringOption CUSTOM_DRJAVA_JAR_VERSION_SUFFIX = new StringOption("custom.drjava.jar.version.suffix", "");
}
