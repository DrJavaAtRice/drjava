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

package edu.rice.cs.drjava.config;
import java.io.File;
import java.util.Vector;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Font;
import edu.rice.cs.drjava.DrJava;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.Toolkit;
import edu.rice.cs.drjava.CodeStatus;
import java.util.ArrayList;

/**
 * @version $Id$
 */
public interface OptionConstants {
  
  // STATIC VARIABLES
  
  /* ---------- Resource Location and Classpath Options ---------- */
  
  public static final FileOption JAVAC_LOCATION =
    new FileOption("javac.location", FileOption.NULL_FILE);
  
  public static final FileOption JSR14_LOCATION =
    new FileOption("jsr14.location", FileOption.NULL_FILE);
  
  public static final FileOption JSR14_COLLECTIONSPATH =
    new FileOption("jsr14.collectionspath", FileOption.NULL_FILE);
  
  public static final VectorOption<File> EXTRA_CLASSPATH =
    new ClasspathOption().evaluate("extra.classpath");
  
  public static final BooleanOption JAVAC_ALLOW_ASSERT =
    new BooleanOption("javac.allow.assert", Boolean.FALSE);
  
  
  /* ---------- Color Options ---------- */
  
  public static final ColorOption DEFINITIONS_NORMAL_COLOR =
    new ColorOption("definitions.normal.color", Color.black);
  public static final ColorOption DEFINITIONS_KEYWORD_COLOR =
    new ColorOption("definitions.keyword.color", Color.blue);
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
  
  /**
   * Color for background of definitions pane.
   */
  public static final ColorOption DEFINITIONS_BACKGROUND_COLOR =
    new ColorOption("definitions.background.color", Color.white);
  
  /**
   * Color for highlighting brace-matching.
   */
  public static final ColorOption DEFINITIONS_MATCH_COLOR =
    new ColorOption("definitions.match.color", new Color(190, 255, 230));
  
  /**
   * Color for highlighting errors and test failures.
   */
  public static final ColorOption COMPILER_ERROR_COLOR =
    new ColorOption("compiler.error.color", Color.yellow);
  
  /**
   * Color for highlighting breakpoints.
   */
  public static final ColorOption DEBUG_BREAKPOINT_COLOR =
    new ColorOption("debug.breakpoint.color", Color.red);
  
  /**
   * Color for highlighting thread locations.
   */
  public static final ColorOption DEBUG_THREAD_COLOR =
    new ColorOption("debug.thread.color", new Color(100,255,255));
  
  
  /* ---------- Font Options ---------- */
  
  /** Main (definitions document, tab contents) */
  
  public static final FontOption FONT_MAIN =
    new FontOption("font.main", Font.decode("Monospaced-PLAIN-12"));
        
  /** List of open documents */
  public static final FontOption FONT_DOCLIST =
    new FontOption("font.doclist", Font.decode("Monospaced-PLAIN-10"));
        
 /** Toolbar buttons */
  public static final FontOption FONT_TOOLBAR =
    new FontOption("font.toolbar", Font.decode("dialog-PLAIN-10"));
       
  
  /* ---------- Other Display Options ---------- */
  
  /**
   * Whether icons should be displayed on the toolbar buttons.
   */
  public static final BooleanOption TOOLBAR_ICONS_ENABLED =
    new BooleanOption("toolbar.icons.enabled", Boolean.TRUE);
  
  /**
   * Whether text should be displayed on the toolbar buttons.
   * Note: this is only relevant if toolbar icons are enabled
   */
  public static final BooleanOption TOOLBAR_TEXT_ENABLED =
    new BooleanOption("toolbar.text.enabled", Boolean.TRUE);
  
  /**
   * Whether the line-numbers should be displayed in a row header.
   */
  public static final BooleanOption LINEENUM_ENABLED =
    new BooleanOption("lineenum.enabled", Boolean.FALSE);
    
  /**
   * Whether to draw anti-aliased text.  (Slightly slower.)
   */
  public static final BooleanOption TEXT_ANTIALIAS =
    new BooleanOption("text.antialias", Boolean.FALSE);
  
  /* ---------- Key Binding Options ----------- */
  static int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
  /**
   * The key binding for creating a new file
   */
  public static final KeyStrokeOption KEY_NEW_FILE =
    new KeyStrokeOption("key.new.file",
                        KeyStroke.getKeyStroke(KeyEvent.VK_N, mask));
  /**
   * The key binding for opening a file
   */
  public static final KeyStrokeOption KEY_OPEN_FILE =
    new KeyStrokeOption("key.open.file",
                        KeyStroke.getKeyStroke(KeyEvent.VK_O, mask));
  /**
   * The key binding for saving a file
   */
  public static final KeyStrokeOption KEY_SAVE_FILE =
    new KeyStrokeOption("key.save.file",
                        KeyStroke.getKeyStroke(KeyEvent.VK_S, mask));
  /**
   * The key binding for saving a file as
   */
  public static final KeyStrokeOption KEY_SAVE_FILE_AS =
    new KeyStrokeOption("key.save.file.as",
                        KeyStroke.getKeyStroke(KeyEvent.VK_S, mask |
                                               InputEvent.SHIFT_MASK));
  /**
   * The key binding for closing a file
   */
  public static final KeyStrokeOption KEY_CLOSE_FILE =
    new KeyStrokeOption("key.close.file",
                        KeyStroke.getKeyStroke(KeyEvent.VK_W, mask));
  /**
   * The key binding for showing the print preview
   */
  public static final KeyStrokeOption KEY_PRINT_PREVIEW =
    new KeyStrokeOption("key.print.preview",
                        KeyStroke.getKeyStroke(KeyEvent.VK_P, mask |
                                               InputEvent.SHIFT_MASK));
  /**
   * The key binding for printing a file
   */
  public static final KeyStrokeOption KEY_PRINT =
    new KeyStrokeOption("key.print",
                        KeyStroke.getKeyStroke(KeyEvent.VK_P, mask));
  /**
   * The key binding for quitting
   */
  public static final KeyStrokeOption KEY_QUIT =
    new KeyStrokeOption("key.quit",
                        KeyStroke.getKeyStroke(KeyEvent.VK_Q, mask));
  /**
   * The key binding for undo-ing
   */
  public static final KeyStrokeOption KEY_UNDO =
    new KeyStrokeOption("key.undo",
                        KeyStroke.getKeyStroke(KeyEvent.VK_Z, mask));
  /**
   * The key binding for redo-ing
   */
  public static final KeyStrokeOption KEY_REDO =
    new KeyStrokeOption("key.redo",
                        KeyStroke.getKeyStroke(KeyEvent.VK_R, mask));
  /**
   * The key binding for cutting
   */
  public static final KeyStrokeOption KEY_CUT =
    new KeyStrokeOption("key.cut",
                        KeyStroke.getKeyStroke(KeyEvent.VK_X, mask));
  /**
   * The key binding for copying
   */
  public static final KeyStrokeOption KEY_COPY =
    new KeyStrokeOption("key.copy",
                        KeyStroke.getKeyStroke(KeyEvent.VK_C, mask));
  /**
   * The key binding for pasting
   */
  public static final KeyStrokeOption KEY_PASTE =
    new KeyStrokeOption("key.paste",
                        KeyStroke.getKeyStroke(KeyEvent.VK_V, mask));
  /**
   * The key binding for selecting all text
   */
  public static final KeyStrokeOption KEY_SELECT_ALL =
    new KeyStrokeOption("key.select.all",
                        KeyStroke.getKeyStroke(KeyEvent.VK_A, mask));
  /**
   * The key binding for find and replace
   */
  public static final KeyStrokeOption KEY_FIND_NEXT =
    new KeyStrokeOption("key.find.next",
                        KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
  /**
   * The key binding for find and replace
   */
  public static final KeyStrokeOption KEY_FIND_REPLACE =
    new KeyStrokeOption("key.find.replace",
                        KeyStroke.getKeyStroke(KeyEvent.VK_F, mask));
  /**
   * The key binding for goto line
   */
  public static final KeyStrokeOption KEY_GOTO_LINE =
    new KeyStrokeOption("key.goto.line",
                        KeyStroke.getKeyStroke(KeyEvent.VK_G, mask));
  
  /**
   * The key binding for indenting
   *
  public static final KeyStrokeOption KEY_INDENT =
    new KeyStrokeOption("key.indent",
                        KeyStroke.getKeyStroke(KeyEvent.VK_TAB, mask)); */
  
  /**
   * The key binding for commenting out lines
   */
  public static final KeyStrokeOption KEY_COMMENT_LINES =
    new KeyStrokeOption("key.comment.lines",
                        KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, mask));
  
  /**
   * The key binding for un-commenting lines
   */
  public static final KeyStrokeOption KEY_UNCOMMENT_LINES =
    new KeyStrokeOption("key.uncomment.lines",
                        KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
                                               (mask | InputEvent.ALT_MASK)));
    
  /**
   * The key binding for selecting previous document
   */
  public static final KeyStrokeOption KEY_PREVIOUS_DOCUMENT =
    new KeyStrokeOption("key.previous.document",
                        KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, mask));
  /**
   * The key binding for selecting next document
   */
  public static final KeyStrokeOption KEY_NEXT_DOCUMENT =
    new KeyStrokeOption("key.next.document",
                        KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, mask));
  /**
   * The key binding for compiling current document
   */
  public static final KeyStrokeOption KEY_COMPILE =
    new KeyStrokeOption("key.compile",
                        KeyStroke.getKeyStroke(KeyEvent.VK_F5,
                                               InputEvent.SHIFT_MASK));
  
  /**
   * The key binding for compiling all
   */
  public static final KeyStrokeOption KEY_COMPILE_ALL =
    new KeyStrokeOption("key.compile.all",
                        KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
  
  /**
   * The key binding for aborting the current interaction.
   * (replaced by Reset Interactions, with no shortcut.)
   *
  public static final KeyStrokeOption KEY_ABORT_INTERACTION =
    new KeyStrokeOption("key.abort.interaction",
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
                        */
  /**
   * The key binding for moving the cursor backwards
   */
  public static final KeyStrokeOption KEY_BACKWARD =
    new KeyStrokeOption("key.backward",
                        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
  /**
   * The key binding for moving the cursor to the beginning of the document
   */
  public static final KeyStrokeOption KEY_BEGIN_DOCUMENT =
    new KeyStrokeOption("key.begin.document",
                        KeyStroke.getKeyStroke(KeyEvent.VK_HOME, mask));
  /**
   * The key binding for moving the cursor to the beginning of the current line
   */
  public static final KeyStrokeOption KEY_BEGIN_LINE =
    new KeyStrokeOption("key.begin.line",
                        KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0));
  /**
   * The key binding for moving the cursor to the beginning of the current paragraph.
   * (Doesn't seem to do anything useful...)
   *
  public static final KeyStrokeOption KEY_BEGIN_PARAGRAPH =
    new KeyStrokeOption("key.begin.paragraph",
                        KeyStroke.getKeyStroke(KeyEvent.VK_UP, mask));
   */
  
  /**
   * The key binding for moving the cursor to the beginning of the previous word
   */
  public static final KeyStrokeOption KEY_PREVIOUS_WORD =
    new KeyStrokeOption("key.previous.word",
                        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, mask));
  /**
   * The key binding for deleting the next character
   */
  public static final KeyStrokeOption KEY_DELETE_NEXT =
    new KeyStrokeOption("key.delete.next",
                        KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
  /**
   * The key binding for deleting the previous character
   */
  public static final KeyStrokeOption KEY_DELETE_PREVIOUS =
    new KeyStrokeOption("key.delete.previous",
                        KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
  /**
   * The key binding for moving the cursor down
   */
  public static final KeyStrokeOption KEY_DOWN =
    new KeyStrokeOption("key.down",
                        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
  /**
   * The key binding for moving the cursor up
   */
  public static final KeyStrokeOption KEY_UP =
    new KeyStrokeOption("key.up",
                        KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
  /**
   * The key binding for moving the cursor to the end of the document
   */
  public static final KeyStrokeOption KEY_END_DOCUMENT =
    new KeyStrokeOption("key.end.document",
                        KeyStroke.getKeyStroke(KeyEvent.VK_END, mask));
  /**
   * The key binding for moving the cursor to the end of the current line
   */
  public static final KeyStrokeOption KEY_END_LINE =
    new KeyStrokeOption("key.end.line",
                        KeyStroke.getKeyStroke(KeyEvent.VK_END, 0));
  /**
   * The key binding for moving the cursor to the end of the current paragraph.
   * (Doesn't seem to do anything useful...)
   *
  public static final KeyStrokeOption KEY_END_PARAGRAPH =
    new KeyStrokeOption("key.end.paragraph",
                        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, mask));
   */
  
  /**
   * The key binding for moving the cursor to the beginning of the next word
   */
  public static final KeyStrokeOption KEY_NEXT_WORD =
    new KeyStrokeOption("key.next.word",
                        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, mask));
  /**
   * The key binding for moving the cursor forwards
   */
  public static final KeyStrokeOption KEY_FORWARD =
    new KeyStrokeOption("key.forward",
                        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
  /**
   * The key binding for page down
   */
  public static final KeyStrokeOption KEY_PAGE_DOWN =
    new KeyStrokeOption("key.page.down",
                        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
  /**
   * The key binding for page up
   */
  public static final KeyStrokeOption KEY_PAGE_UP =
    new KeyStrokeOption("key.page.up",
                        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
  
  /**
   * The key binding for cutting a line
   */
  public static final KeyStrokeOption KEY_CUT_LINE =
    new KeyStrokeOption("key.cut.line",
                        KeyStroke.getKeyStroke(KeyEvent.VK_K,
                                               (mask | InputEvent.ALT_MASK)));
  
  /**
   * The key binding for clearing a line, emacs-style
   */
  public static final KeyStrokeOption KEY_CLEAR_LINE =
    new KeyStrokeOption("key.clear.line",
                        KeyStroke.getKeyStroke(KeyEvent.VK_K, mask));
  
  /**
   * The key binding for toggling debug mode
   */
  public static final KeyStrokeOption KEY_DEBUG_MODE_TOGGLE =
    new KeyStrokeOption("key.debug.mode.toggle",
                        KeyStroke.getKeyStroke(KeyEvent.VK_D, mask));
  
  /**
   * The key binding for suspending the debugger
   *
  public static final KeyStrokeOption KEY_DEBUG_SUSPEND =
    new KeyStrokeOption("key.debug.suspend",
                        KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
                        */
  
  /**
   * The key binding for resuming the debugger
   */
  public static final KeyStrokeOption KEY_DEBUG_RESUME =
    new KeyStrokeOption("key.debug.resume",
                        KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
  /**
   * The key binding for stepping into in the debugger
   */
  public static final KeyStrokeOption KEY_DEBUG_STEP_INTO =
    new KeyStrokeOption("key.debug.step.into",
                        KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
  /**
   * The key binding for stepping over in the debugger
   */
  public static final KeyStrokeOption KEY_DEBUG_STEP_OVER =
    new KeyStrokeOption("key.debug.step.over",
                        KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
  /**
   * The key binding for stepping out in the debugger
   */
  public static final KeyStrokeOption KEY_DEBUG_STEP_OUT =
    new KeyStrokeOption("key.debug.step.out",
                        KeyStroke.getKeyStroke(KeyEvent.VK_F12,
                                               InputEvent.SHIFT_MASK));
  /**
   * The key binding for toggling a breakpoint
   */
  public static final KeyStrokeOption KEY_DEBUG_BREAKPOINT_TOGGLE =
    new KeyStrokeOption("key.debug.breakpoint.toggle",
                        KeyStroke.getKeyStroke(KeyEvent.VK_B, mask));
    
    
  /* ---------- Debugger Options ---------- */
    
  /**
   * A classpath-structured vector of all paths to look for source files on
   * while stepping in the debugger.
   */
  public static final VectorOption<File> DEBUG_SOURCEPATH =
    new ClasspathOption().evaluate("debug.sourcepath");
  
  /**
   * Whether all current threads should be displayed when a thread suspends
   */
  public static final BooleanOption DEBUG_SHOW_THREADS =
    new BooleanOption("debug.show.threads", Boolean.FALSE);
  
  /**
   * Whether stepping should step through Java's source files
   */
  public static final BooleanOption DEBUG_STEP_JAVA =
    new BooleanOption("debug.step.java", Boolean.FALSE);
  
  /**
   * Whether stepping should step through Dynamic Java's source files
   */
  public static final BooleanOption DEBUG_STEP_INTERPRETER =
    new BooleanOption("debug.step.interpreter", Boolean.FALSE);
  
  /**
   * Whether stepping should step through DrJava's source files
   */
  public static final BooleanOption DEBUG_STEP_DRJAVA =
    new BooleanOption("debug.step.drjava", Boolean.FALSE);
  
  /**
   * Which packages to exclude when stepping.
   */
  public static final StringOption DEBUG_STEP_EXCLUDE =
    new StringOption("debug.step.exclude", "");
  
  
  
  /* ---------- Javadoc Options ---------- */
  
  /**
   * Possible options for Javadoc access levels.
   */
  static final ArrayList accessLevelChoices =
    (new Begin<ArrayList<String>>() {
       public ArrayList<String> evaluate() {
         ArrayList<String> aList = new ArrayList<String>(4);
         aList.add("public");
         aList.add("protected");
         aList.add("package");
         aList.add("private");
         return aList;
       }
     }).evaluate();
  
  /**
   * The lowest access level of classes and members to include in the javadoc.
   */
  public static final ForcedChoiceOption JAVADOC_ACCESS_LEVEL =
    new ForcedChoiceOption("javadoc.access.level", "protected", accessLevelChoices);
  
  /**
   * Possible options for Javadoc system class documentation links.
   */
  static final String JAVADOC_NONE_TEXT = "none";
  static final String JAVADOC_1_3_TEXT = "1.3";
  static final String JAVADOC_1_4_TEXT = "1.4";
  
  static final ArrayList linkVersionChoices =
    (new Begin<ArrayList<String>>() {
       public ArrayList<String> evaluate() {
         ArrayList<String> aList = new ArrayList<String>(4);
         aList.add(JAVADOC_NONE_TEXT);
         aList.add(JAVADOC_1_3_TEXT);
         aList.add(JAVADOC_1_4_TEXT);
         return aList;
       }
     }).evaluate();
     
  /**
   * Constants for the URLs of Sun's system class documentation for different
   * versions of Java.
   */
  public static final StringOption JAVADOC_1_3_LINK =
    new StringOption("javadoc.1.3.link", "http://java.sun.com/j2se/1.3/docs/api");
  public static final StringOption JAVADOC_1_4_LINK =
    new StringOption("javadoc.1.4.link", "http://java.sun.com/j2se/1.4/docs/api");
     
  /**
   * The version of Java to use for links to Javadoc for system classes.
   */
  public static final ForcedChoiceOption JAVADOC_LINK_VERSION =
    new ForcedChoiceOption("javadoc.link.version",
                           (System.getProperty("java.specification.version").equals("1.3") ?
                              JAVADOC_1_3_TEXT :
                              JAVADOC_1_4_TEXT),
                           linkVersionChoices);
  
  /**
   * Whether to include the entire package heirarchy from the source roots when
   * generating JavaDoc output.
   */
  public static final BooleanOption JAVADOC_FROM_ROOTS =
    new BooleanOption("javadoc.from.roots", Boolean.FALSE);
  
  /**
   * A string containing custom options to be passed to Javadoc.
   * This needs to be tokenized before passing it to Javadoc.
   */
  public static final StringOption JAVADOC_CUSTOM_PARAMS =
    new StringOption("javadoc.custom.params", "");
  
  /**
   * The default destination directory for Javadoc output.
   */
  public static final FileOption JAVADOC_DESTINATION =
    new FileOption("javadoc.destination", FileOption.NULL_FILE);
  
  /**
   * Whether to always prompt for a destination directory, whether or not a
   * default has been set.
   */
  public static final BooleanOption JAVADOC_PROMPT_FOR_DESTINATION =
    new BooleanOption("javadoc.prompt.for.destination", Boolean.TRUE);
  
  
  
  /* ---------- Misc Options ---------- */
  
  /**
   * Directory to start looking for files in when DrJava starts up.
   */
  public static final FileOption WORKING_DIRECTORY =
    new FileOption("working.directory", FileOption.NULL_FILE);
  
  /**
   * How many spaces to use for indenting.
   */
  public static final NonNegativeIntegerOption INDENT_LEVEL =
    new NonNegativeIntegerOption("indent.level",new Integer(2));
  
  /**
   * Number of lines to remember in the Interactions History
   */
  public static final NonNegativeIntegerOption HISTORY_MAX_SIZE =
    new NonNegativeIntegerOption("history.max.size", new Integer(500));
  
  /**
   * Number of files to list in the recent file list
   */
  public static final NonNegativeIntegerOption RECENT_FILES_MAX_SIZE =
    new NonNegativeIntegerOption("recent.files.max.size", new Integer(5));
  
  /**
   * Whether to prompt when the interactions pane is unexpectedly reset.
   */
  public static final BooleanOption INTERACTIONS_EXIT_PROMPT =
    new BooleanOption("interactions.exit.prompt", Boolean.TRUE);
  
  /**
   * Whether to prompt before quitting DrJava.
   */
  public static final BooleanOption QUIT_PROMPT =
    new BooleanOption("quit.prompt", Boolean.TRUE);

  /**
   * Whether to make file backups
   */
  public static final BooleanOption BACKUP_FILES =
    new BooleanOption("files.backup", Boolean.TRUE);
  
  /**
   * A vector containing the most recently used files
   */
  public static final VectorOption<File> RECENT_FILES =
    new VectorOption<File>("recent.files",new FileOption("",null),new Vector<File>());
}


