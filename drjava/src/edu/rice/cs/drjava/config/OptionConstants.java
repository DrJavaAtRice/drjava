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

package edu.rice.cs.drjava.config;
import java.io.File;
import java.util.Vector;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import edu.rice.cs.drjava.platform.PlatformFactory;

/**
 * @version $Id$
 */
public interface OptionConstants {

  // STATIC VARIABLES

  /* ---------- Resource Location and Classpath Options ---------- */

  /**
   * A file path to a user's preferred browser.
   */
  public static final FileOption BROWSER_FILE =
    new FileOption("browser.file", FileOption.NULL_FILE);

  /**
   * A String used with the command to launch a user's preferred browser.
   * This will be tokenized and appended to the file path.
   */
  public static final StringOption BROWSER_STRING =
    new StringOption("browser.string", "");
  
  /** 
   * the extension for a DrJava project file 
   */
  public static final String PROJECT_FILE_EXTENSION = ".pjt";
  
  public static final FileOption JAVAC_LOCATION =
    new FileOption("javac.location", FileOption.NULL_FILE);

  public static final FileOption JSR14_LOCATION =
    new FileOption("jsr14.location", FileOption.NULL_FILE);

  public static final FileOption JSR14_COLLECTIONSPATH =
    new FileOption("jsr14.collectionspath", FileOption.NULL_FILE);

  public static final VectorOption<File> EXTRA_CLASSPATH =
    new ClasspathOption().evaluate("extra.classpath");

  public static final VectorOption<String> EXTRA_COMPILERS =
    new VectorOption<String>("extra.compilers", new StringOption("",""), new Vector<String>());

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
  public static final ColorOption SYSTEM_OUT_COLOR =
    new ColorOption("system.out.color", Color.green.darker().darker());
  public static final ColorOption SYSTEM_ERR_COLOR =
    new ColorOption("system.err.color", Color.red);
  public static final ColorOption SYSTEM_IN_COLOR =
    new ColorOption("system.in.color", Color.magenta.darker().darker());
  public static final ColorOption INTERACTIONS_ERROR_COLOR =
    new ColorOption("interactions.error.color", Color.red.darker());
  public static final ColorOption DEBUG_MESSAGE_COLOR =
    new ColorOption("debug.message.color", Color.blue.darker());


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
    new FontOption("font.main", DefaultFont.getDefaultMainFont());

  /**
   * Class that allows the main font to be initialized properly.
   * On Mac14, Monospaced-PLAIN-12 is too faint, so use Monaco instead.
   */
  static class DefaultFont {
    public static Font getDefaultMainFont() {
      if (PlatformFactory.ONLY.isMac14Platform()) {
        return Font.decode("Monaco-12");
      }
      else {
        return Font.decode("Monospaced-12");
      }
    }
    public static Font getDefaultLineNumberFont() {
      if (PlatformFactory.ONLY.isMac14Platform()) {
        return Font.decode("Monaco-12");
      }
      else {
        return Font.decode("Monospaced-12");
      }
    }
    public static Font getDefaultDocListFont() {
      if (PlatformFactory.ONLY.isMac14Platform()) {
        return Font.decode("Monaco-10");
      }
      else {
        return Font.decode("Monospaced-10");
      }
    }
  }

  /** Line numbers */
  public static final FontOption FONT_LINE_NUMBERS =
    new FontOption("font.line.numbers", DefaultFont.getDefaultLineNumberFont());

  /** List of open documents */
  public static final FontOption FONT_DOCLIST =
    new FontOption("font.doclist", DefaultFont.getDefaultDocListFont());

 /** Toolbar buttons */
  public static final FontOption FONT_TOOLBAR =
    new FontOption("font.toolbar", Font.decode("dialog-10"));

  /**
   * Whether to draw anti-aliased text.  (Slightly slower.)
   */
  public static final BooleanOption TEXT_ANTIALIAS =
    new BooleanOption("text.antialias", Boolean.FALSE);


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
   * Whether to save and restore window size and position at startup/shutdown.
   */
  public static final BooleanOption WINDOW_STORE_POSITION =
    new BooleanOption("window.store.position", Boolean.TRUE);


  /**
   * The current look and feel.
   */
  public static final ForcedChoiceOption LOOK_AND_FEEL =
    new ForcedChoiceOption("look.and.feel",
                           LookAndFeels.getDefaultLookAndFeel(),
                           LookAndFeels.getLookAndFeels());

  /**
   * Class that allows the look and feels to be initialized properly.
   */
  static class LookAndFeels {
    /**
     * Mac platform should default to aqua; use metal elsewhere.
     * @return the look-and-feel to use by default
     */
    public static String getDefaultLookAndFeel() {
      if (PlatformFactory.ONLY.isMacPlatform()) {
        return UIManager.getSystemLookAndFeelClassName();
      }
      else {
        return UIManager.getCrossPlatformLookAndFeelClassName();
      }
    }
    /**
     * Need to ensure that a look-and-feel can be instantiated and is valid.
     * TODO:  store the LookAndFeel object rather than its classname.
     *        This would be much nicer, as we could display a useful name,
     *        and wouldn't have to reinstantiate it when it's installed.
     * @return the list of availabe look-and-feel classnames
     */
    public static ArrayList<String> getLookAndFeels() {
      ArrayList<String> lookAndFeels = new ArrayList<String>();
      LookAndFeelInfo[] lafis = UIManager.getInstalledLookAndFeels();
      if (lafis != null) {
        for (int i = 0; i < lafis.length; i++) {
          try {
            String currName = lafis[i].getClassName();
            LookAndFeel currLAF = (LookAndFeel) Class.forName(currName).newInstance();
            if (currLAF.isSupportedLookAndFeel()) {
              lookAndFeels.add(currName);
            }
          }
          catch (Exception ex) {
            // failed to load/instantiate class, or it is not supported.
            // It is not a valid choice.
          }
        }
      }
      return lookAndFeels;
    }
  }

  /* ---------- Key Binding Options ----------- */
  static int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
  /**
   * The key binding for creating a new file
   */
  public static final KeyStrokeOption KEY_NEW_FILE =
    new KeyStrokeOption("key.new.file",
                        KeyStroke.getKeyStroke(KeyEvent.VK_N, mask));
  /**
   * The key binding for opening an entire project.  I is right next to O, so
   * it seemed logical that ctrl-I would open a project and ctrl-O open a file
   */
  public static final KeyStrokeOption KEY_OPEN_PROJECT =
    new KeyStrokeOption("key.open.project",
                        KeyStroke.getKeyStroke(KeyEvent.VK_I, mask));
  /**
   * The key binding for creating a new JUnit test case
   */
  public static final KeyStrokeOption KEY_NEW_TEST =
    new KeyStrokeOption("key.new.test",
                        KeyStrokeOption.NULL_KEYSTROKE);
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
   * The key binding for saving all files
   */
  public static final KeyStrokeOption KEY_SAVE_ALL_FILES =
    new KeyStrokeOption("key.save.all.files",
                        KeyStroke.getKeyStroke(KeyEvent.VK_S, mask |
                                               InputEvent.ALT_MASK));
  /**
   * The key binding for reverting a file
   */
  public static final KeyStrokeOption KEY_REVERT_FILE =
    new KeyStrokeOption("key.revert.file",
                        KeyStroke.getKeyStroke(KeyEvent.VK_R, mask));
  /**
   * The key binding for closing a file
   */
  public static final KeyStrokeOption KEY_CLOSE_FILE =
    new KeyStrokeOption("key.close.file",
                        KeyStroke.getKeyStroke(KeyEvent.VK_W, mask));
  /**
   * The key binding for closing all files
   */
  public static final KeyStrokeOption KEY_CLOSE_ALL_FILES =
    new KeyStrokeOption("key.close.all.files",
                        KeyStroke.getKeyStroke(KeyEvent.VK_W, mask |
                                               InputEvent.ALT_MASK));
  
  public static final KeyStrokeOption KEY_CLOSE_PROJECT =
    new KeyStrokeOption("key.close.project",
                        KeyStroke.getKeyStroke(KeyEvent.VK_W, mask |
                                               InputEvent.SHIFT_MASK));
  /**
   * The key binding for showing the print preview
   */
  public static final KeyStrokeOption KEY_PAGE_SETUP =
    new KeyStrokeOption("key.page.setup",
                        KeyStrokeOption.NULL_KEYSTROKE);
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
                        KeyStroke.getKeyStroke(KeyEvent.VK_Z, mask |
                                               InputEvent.SHIFT_MASK));
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
                                               (mask | InputEvent.SHIFT_MASK)));

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
   * The key binding for changing the focus to the previous pane
   */
  public static final KeyStrokeOption KEY_PREVIOUS_PANE =
    new KeyStrokeOption("key.previous.pane",
                        KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, mask));

  /**
   * The key binding for changing the focus to the previous pane
   */
  public static final KeyStrokeOption KEY_NEXT_PANE =
    new KeyStrokeOption("key.next.pane",
                        KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, mask));

  /**
   * The key binding for openning the preferences dialog
   */
  public static final KeyStrokeOption KEY_PREFERENCES =
    new KeyStrokeOption("key.preferences",
                        KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, mask));

  /**
   * The key binding for compiling current document
   */
  public static final KeyStrokeOption KEY_COMPILE =
    new KeyStrokeOption("key.compile", KeyStroke.getKeyStroke(KeyEvent.VK_F5, InputEvent.SHIFT_MASK));

  /**
   * The key binding for compiling all
   */
  public static final KeyStrokeOption KEY_COMPILE_ALL =
    new KeyStrokeOption("key.compile.all", KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));

  /**
   * The key binding for openning the preferences dialog
   */
  public static final KeyStrokeOption KEY_RUN =
    new KeyStrokeOption("key.run", KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));

  /**
   * The key binding for testing the current document
   */
  public static final KeyStrokeOption KEY_TEST =
    new KeyStrokeOption("key.test",
                        KeyStroke.getKeyStroke(KeyEvent.VK_T, mask | InputEvent.SHIFT_MASK));

  /**
   * The key binding for testing all open JUnit test cases.
   */
  public static final KeyStrokeOption KEY_TEST_ALL =
    new KeyStrokeOption("key.test.all", KeyStroke.getKeyStroke(KeyEvent.VK_T, mask));

  /**
   * The key binding for generating javadoc for all documents
   */
  public static final KeyStrokeOption KEY_JAVADOC_ALL =
    new KeyStrokeOption("key.javadoc.all", KeyStroke.getKeyStroke(KeyEvent.VK_J, mask));

  /**
   * The key binding for generating javadoc for the current document
   */
  public static final KeyStrokeOption KEY_JAVADOC_CURRENT =
    new KeyStrokeOption("key.javadoc.current",
                        KeyStroke.getKeyStroke(KeyEvent.VK_J, mask | InputEvent.SHIFT_MASK));

  /**
   * The key binding for executing an interactions history.
   */
  public static final KeyStrokeOption KEY_EXECUTE_HISTORY =
    new KeyStrokeOption("key.execute.history", KeyStrokeOption.NULL_KEYSTROKE);

  /**
   * The key binding for loading an interactions history as a script.
   */
  public static final KeyStrokeOption KEY_LOAD_HISTORY_SCRIPT =
    new KeyStrokeOption("key.load.history.script", KeyStrokeOption.NULL_KEYSTROKE);

  /**
   * The key binding for saving an interactions history.
   */
  public static final KeyStrokeOption KEY_SAVE_HISTORY =
    new KeyStrokeOption("key.save.history", KeyStrokeOption.NULL_KEYSTROKE);

  /**
   * The key binding for clearing the interactions history.
   */
  public static final KeyStrokeOption KEY_CLEAR_HISTORY =
    new KeyStrokeOption("key.clear.history", KeyStrokeOption.NULL_KEYSTROKE);

  /**
   * The key binding for resetting the interactions pane.
   */
  public static final KeyStrokeOption KEY_RESET_INTERACTIONS =
    new KeyStrokeOption("key.reset.interactions", KeyStrokeOption.NULL_KEYSTROKE);

  /**
   * The key binding for viewing the interactions classpath.
   */
  public static final KeyStrokeOption KEY_VIEW_INTERACTIONS_CLASSPATH =
    new KeyStrokeOption("key.view.interactions.classpath", KeyStrokeOption.NULL_KEYSTROKE);

  /**
   * The key binding for lifting the current interaction to definitions.
   */
  public static final KeyStrokeOption KEY_LIFT_CURRENT_INTERACTION =
    new KeyStrokeOption("key.lift.current.interaction", KeyStrokeOption.NULL_KEYSTROKE);

  /**
   * The key binding to enter or leave multiline input mode.
   *
  public static final KeyStrokeOption KEY_TOGGLE_MULTILINE_INTERACTION =
    new KeyStrokeOption("key.toggle.multiline.interaction",
                        KeyStroke.getKeyStroke(KeyEvent.VK_M, mask));
   */

  /**
   * The key binding for clearing the console.
   */
  public static final KeyStrokeOption KEY_CLEAR_CONSOLE =
    new KeyStrokeOption("key.clear.console", KeyStrokeOption.NULL_KEYSTROKE);

  /**
   * The key binding for moving the cursor backwards
   */
  public static final KeyStrokeOption KEY_BACKWARD =
    new KeyStrokeOption("key.backward", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));

  /**
   * The key binding for moving the cursor to the beginning of the document
   */
  public static final KeyStrokeOption KEY_BEGIN_DOCUMENT =
    new KeyStrokeOption("key.begin.document", KeyStroke.getKeyStroke(KeyEvent.VK_HOME, mask));

  /**
   * The key binding for moving the cursor to the beginning of the current line
   */
  public static final KeyStrokeOption KEY_BEGIN_LINE =
    new KeyStrokeOption("key.begin.line", KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0));

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
    new KeyStrokeOption("key.previous.word", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, mask));

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
    new KeyStrokeOption("key.delete.previous", KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));

  /**
   * The key binding for moving the cursor down
   */
  public static final KeyStrokeOption KEY_DOWN =
    new KeyStrokeOption("key.down", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));

  /**
   * The key binding for moving the cursor up
   */
  public static final KeyStrokeOption KEY_UP =
    new KeyStrokeOption("key.up", KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));

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

  /**
   * The key binding for clearing all breakpoints
   */
  public static final KeyStrokeOption KEY_DEBUG_CLEAR_ALL_BREAKPOINTS =
    new KeyStrokeOption("key.debug.clear.all.breakpoints",
                        KeyStrokeOption.NULL_KEYSTROKE);

  /**
   * The key binding for help
   */
  public static final KeyStrokeOption KEY_HELP =
    new KeyStrokeOption("key.help",
                        KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

  /**
   * The key binding for the about dialog
   */
  public static final KeyStrokeOption KEY_ABOUT =
    new KeyStrokeOption("key.about",
                        KeyStrokeOption.NULL_KEYSTROKE);


  /* ---------- Debugger Options ---------- */

  /**
   * A classpath-structured vector of all paths to look for source files on
   * while stepping in the debugger.
   */
  public static final VectorOption<File> DEBUG_SOURCEPATH =
    new ClasspathOption().evaluate("debug.sourcepath");

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
  static final ArrayList<String> accessLevelChoices =
    AccessLevelChoices.evaluate();
  static class AccessLevelChoices {
    public static ArrayList<String> evaluate() {
      ArrayList<String> aList = new ArrayList<String>(4);
      aList.add("public");
      aList.add("protected");
      aList.add("package");
      aList.add("private");
      return aList;
    }
  }

  /**
   * The lowest access level of classes and members to include in the javadoc.
   */
  public static final ForcedChoiceOption JAVADOC_ACCESS_LEVEL =
    new ForcedChoiceOption("javadoc.access.level", "package", accessLevelChoices);

  /**
   * Possible options for Javadoc system class documentation links.
   */
  static final String JAVADOC_NONE_TEXT = "none";
  static final String JAVADOC_1_3_TEXT = "1.3";
  static final String JAVADOC_1_4_TEXT = "1.4";

  static final ArrayList<String> linkVersionChoices =
    LinkVersionChoices.evaluate();
  static class LinkVersionChoices {
    public static ArrayList<String> evaluate() {
      ArrayList<String> aList = new ArrayList<String>(4);
      aList.add(JAVADOC_NONE_TEXT);
      aList.add(JAVADOC_1_3_TEXT);
      aList.add(JAVADOC_1_4_TEXT);
      return aList;
    }
  }

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
    new StringOption("javadoc.custom.params", "-author -version");

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

  /* ---------- Notifications Options ---------- */

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
   * Whether to prompt before resetting the interactions pane.
   */
  public static final BooleanOption INTERACTIONS_RESET_PROMPT =
    new BooleanOption("interactions.reset.prompt", Boolean.TRUE);

  /**
   * Whether to prompt to save before compiling.
   */
  public static final BooleanOption ALWAYS_SAVE_BEFORE_COMPILE =
    new BooleanOption("save.before.compile", Boolean.FALSE);

  /**
   * Whether to prompt to save before running.
   */
  public static final BooleanOption ALWAYS_SAVE_BEFORE_RUN =
    new BooleanOption("save.before.run", Boolean.FALSE);

  /**
   * Whether to prompt to save before testing.
   */
  public static final BooleanOption ALWAYS_SAVE_BEFORE_JUNIT =
    new BooleanOption("save.before.junit", Boolean.FALSE);

  /**
   * Whether to prompt to save before compiling.
   */
  public static final BooleanOption ALWAYS_SAVE_BEFORE_JAVADOC =
    new BooleanOption("save.before.javadoc", Boolean.FALSE);

  /**
   * Whether to prompt to save before compiling.
   */
  public static final BooleanOption ALWAYS_SAVE_BEFORE_DEBUG =
    new BooleanOption("save.before.debug", Boolean.FALSE);

  /**
   * Whether to warn if a document has been modified before allowing the
   * user to set a breakpoint in it.
   */
  public static final BooleanOption WARN_BREAKPOINT_OUT_OF_SYNC =
    new BooleanOption("warn.breakpoint.out.of.sync", Boolean.TRUE);

  /**
   * Whether to warn that the user is debugging a file that is out of sync
   * with its class file.
   */
  public static final BooleanOption WARN_DEBUG_MODIFIED_FILE =
    new BooleanOption("warn.debug.modified.file", Boolean.TRUE);

  /**
   * Whether to warn that a restart is necessary before the look and feel will change.
   */
  public static final BooleanOption WARN_CHANGE_LAF =
    new BooleanOption("warn.change.laf", Boolean.TRUE);

  /**
   * Whether to warn that a file's path contains a "#' symbol.
   */
  public static final BooleanOption WARN_PATH_CONTAINS_POUND =
    new BooleanOption("warn.path.contains.pound", Boolean.TRUE);

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
   * Whether to automatically close comments.
   */
  public static final BooleanOption AUTO_CLOSE_COMMENTS =
    new BooleanOption("auto.close.comments", Boolean.FALSE);

  /**
   * Whether to clear the console when manually resetting the interactions pane.
   */
  public static final BooleanOption RESET_CLEAR_CONSOLE =
    new BooleanOption("reset.clear.console", Boolean.TRUE);

  /**
   * Whether to allow the assert keyword in Java 1.4+.
   */
  public static final BooleanOption JAVAC_ALLOW_ASSERT =
    new BooleanOption("javac.allow.assert", Boolean.FALSE);

  /**
   * Whether to make emacs-style backup files.
   */
  public static final BooleanOption BACKUP_FILES =
    new BooleanOption("files.backup", Boolean.TRUE);

  /**
   * Whether to allow users to access to all members in the Interactions Pane.
   */
  public static final BooleanOption ALLOW_PRIVATE_ACCESS =
    new BooleanOption("allow.private.access", Boolean.FALSE);

  /* ---------- Undisplayed Options ---------- */

  /**
   * A vector containing the most recently used files.
   */
  public static final VectorOption<File> RECENT_FILES =
    new VectorOption<File>("recent.files",new FileOption("",null),new Vector<File>());

  /**
   * Whether to enabled the Show Debug Console menu item in the Tools menu.
   */
  public static final BooleanOption SHOW_DEBUG_CONSOLE =
    new BooleanOption("show.debug.console", Boolean.FALSE);

  /**
   * Height of MainFrame at startup.  Can be overridden if out of bounds.
   */
  public static final NonNegativeIntegerOption WINDOW_HEIGHT =
    new NonNegativeIntegerOption("window.height",new Integer(700));

  /**
   * Width of MainFrame at startup.  Can be overridden if out of bounds.
   */
  public static final NonNegativeIntegerOption WINDOW_WIDTH =
    new NonNegativeIntegerOption("window.width",new Integer(800));

  /**
   * X position of MainFrame at startup.  Can be overridden if out of bounds.
   * This value can legally be negative in a multi-screen setup.
   */
  public static final IntegerOption WINDOW_X =
    new IntegerOption("window.x", new Integer(Integer.MAX_VALUE));

  /**
   * Y position of MainFrame at startup.  Can be overridden if out of bounds.
   * This value can legally be negative in a multi-screen setup.
   */
  public static final IntegerOption WINDOW_Y =
    new IntegerOption("window.y", new Integer(Integer.MAX_VALUE));

  /**
   * Width of DocList at startup.  Must be less than WINDOW_WIDTH.
   * Can be overridden if out of bounds.
   */
  public static final NonNegativeIntegerOption DOC_LIST_WIDTH =
    new NonNegativeIntegerOption("doc.list.width",new Integer(150));

  /**
   * Height of tabbed panel at startup.  Must be less than WINDOW_HEIGHT +
   * DEBUG_PANEL_HEIGHT.  Can be overridden if out of bounds.
   */
  public static final NonNegativeIntegerOption TABS_HEIGHT =
    new NonNegativeIntegerOption("tabs.height",new Integer(120));

  /**
   * Height of debugger panel at startup.  Must be less than WINDOW_HEIGHT +
   * TABS_HEIGHT.  Can be overridden if out of bounds.
   */
  public static final NonNegativeIntegerOption DEBUG_PANEL_HEIGHT =
    new NonNegativeIntegerOption("debug.panel.height",new Integer(0));

  /**
   * The directory in use by the user upon the previous quit.
   */
  public static final FileOption LAST_DIRECTORY =
    new FileOption("last.dir", FileOption.NULL_FILE);

  /**
   * The command-line arguments to be passed to the interpreter jvm.
   */
  public static final StringOption JVM_ARGS = new StringOption("jvm.args", "");
}
