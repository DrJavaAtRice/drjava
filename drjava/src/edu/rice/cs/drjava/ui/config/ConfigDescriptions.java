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

package edu.rice.cs.drjava.ui.config;

import java.util.HashMap;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.drjava.config.*;

/** Descriptions for configuration items.
  *  @version $Id$
  */
public class ConfigDescriptions {
  public static final HashMap<OptionParser<?>,String> CONFIG_DESCRIPTIONS = new HashMap<OptionParser<?>,String>();
  public static final HashMap<OptionParser<?>,String> CONFIG_LONG_DESCRIPTIONS = new HashMap<OptionParser<?>,String>();
  protected static final String SEPS = " \t\n-,;.(";
  
  public static void add(Option<?> o, String s, String l) {
    CONFIG_DESCRIPTIONS.put(o, s);
    CONFIG_LONG_DESCRIPTIONS.put(o, l);
  }
  
  static {
    add(OptionConstants.BROWSER_FILE, "Web Browser",
        "<html>Location of a web browser to use for Javadoc and Help links.<br>" +
        "If left blank, only the Web Browser Command will be used.<br>" +
        "This is not necessary if a default browser is available on your system.");
    add(OptionConstants.BROWSER_STRING, "Web Browser Command",
        "<html>Command to send to the web browser to view a web location.<br>" +
        "The string <code>&lt;URL&gt;</code> will be replaced with the URL address.<br>" +
        "This is not necessary if a default browser is available on your system.");
    add(OptionConstants.JAVAC_LOCATION, "Tools.jar Location",
        "Optional location of the JDK's tools.jar, which contains the compiler and debugger.");
    add(OptionConstants.DISPLAY_ALL_COMPILER_VERSIONS, "Display All Compiler Versions",
        "Display all compiler versions, even if they have the same major version.");
    add(OptionConstants.EXTRA_CLASSPATH, "Extra Classpath",
        "<html>Any directories or jar files to add to the classpath<br>"+
        "of the Compiler and Interactions Pane.</html>");
    add(OptionConstants.LOOK_AND_FEEL, "Look and Feel", "Changes the general appearance of DrJava.");
    add(OptionConstants.PLASTIC_THEMES, "Plastic Theme", 
        "Pick the theme to be used by the Plastic family of Look and Feels");
    
    //ToolBarOptionComponent is a degenerate option component
    // addOptionComponent(panel, new ToolBarOptionComponent("ToolBar Buttons"
    //                                               "How to display the toolbar buttons."));
    add(OptionConstants.LINEENUM_ENABLED,
        "Show All Line Numbers",
        "Whether to show line numbers on the left side of the Definitions Pane.");
    
    add(OptionConstants.SHOW_SOURCE_WHEN_SWITCHING, 
        "Show sample of source code when fast switching",
        "Whether to show a sample of the source code under the document's filename when fast switching documents.");
    add(OptionConstants.SHOW_CODE_PREVIEW_POPUPS, 
        "Show Code Preview Popups",
        "<html>Whether to show a popup window with a code preview when the mouse is hovering<br>"+
        "over an item in the Breakpoints, Bookmarks and Find All panes.</html>");
    
    add(OptionConstants.CLIPBOARD_HISTORY_SIZE,
        "Size of Clipboard History",
        "Determines how many entries are kept in the clipboard history.");
    
    add(OptionConstants.DIALOG_GOTOFILE_FULLY_QUALIFIED,
        "<html><p align=\"right\">" + 
        StringOps.
          splitStringAtWordBoundaries("Display Fully-Qualified Class Names in \"Go to File\" Dialog",
                                      40, "<br>", SEPS)+"</p></html>",
        "<html>Whether to also display fully-qualified class names in the \"Go to File\" dialog.<br>"+
        "Enabling this option on network drives might cause the dialog to display after a slight delay.</html>");
    
    add(OptionConstants.DIALOG_COMPLETE_SCAN_CLASS_FILES,
        "<html><p align=\"right\">" + 
        StringOps.
          splitStringAtWordBoundaries("Scan Class Files After Each Compile for Auto-Completion and Auto-Import",
                                      40, "<br>", SEPS)+"</p></html>",
        "<html>Whether to scan the class files after a compile to generate class names<br>"+
        "used for auto-completion and auto-import.<br>"+
        "Enabling this option will slow compiles down.</html>");
    
    add(OptionConstants.DIALOG_COMPLETE_JAVAAPI,
        "<html><p align=\"right\">" + 
        StringOps.
          splitStringAtWordBoundaries("Consider Java API Classes for Auto-Completion",
                                      40, "<br>", SEPS)+"</p></html>",
        "Whether to use the names of the Java API classes for auto-completion as well.");
    
    add(OptionConstants.DISPLAY_RIGHT_MARGIN, "Display right margin",
        "Whether to display a line at the right margin.");
    
    add(OptionConstants.RIGHT_MARGIN_COLUMNS,
        "Right Margin Position",
        "The number of columns after which the right margin is displayed.");
    
    add(OptionConstants.FONT_MAIN, "Main Font", 
        "The font used for most text in DrJava.");
    add(OptionConstants.FONT_LINE_NUMBERS, "Line Numbers Font",
        "<html>The font for displaying line numbers on the left side of<br>" +
        "the Definitions Pane if Show All Line Numbers is enabled.<br>" +
        "Cannot be displayed larger than the Main Font.</html>");
    add(OptionConstants.FONT_DOCLIST, "Document List Font",
        "The font used in the list of open documents.");
    add(OptionConstants.FONT_TOOLBAR, "ToolBar Font",
        "The font used in the toolbar buttons.");
    add(OptionConstants.TEXT_ANTIALIAS, "Use anti-aliased text",
        "Whether to graphically smooth the text.");
    
    add(OptionConstants.DEFINITIONS_NORMAL_COLOR, "Normal Color",
        "The default color for text in the Definitions Pane.");
    add(OptionConstants.DEFINITIONS_KEYWORD_COLOR, "Keyword Color",
        "The color for Java keywords in the Definitions Pane.");
    add(OptionConstants.DEFINITIONS_TYPE_COLOR, "Type Color",
        "The color for classes and types in the Definitions Pane.");
    add(OptionConstants.DEFINITIONS_COMMENT_COLOR, "Comment Color",
        "The color for comments in the Definitions Pane.");
    add(OptionConstants.DEFINITIONS_DOUBLE_QUOTED_COLOR, "Double-quoted Color",
        "The color for quoted strings (eg. \"...\") in the Definitions Pane.");
    add(OptionConstants.DEFINITIONS_SINGLE_QUOTED_COLOR, "Single-quoted Color",
        "The color for quoted characters (eg. 'a') in the Definitions Pane.");
    add(OptionConstants.DEFINITIONS_NUMBER_COLOR, "Number Color",
        "The color for numbers in the Definitions Pane.");
    add(OptionConstants.DEFINITIONS_BACKGROUND_COLOR, "Background Color",
        "The background color of the Definitions Pane.");
    add(OptionConstants.DEFINITIONS_LINE_NUMBER_COLOR, "Line Number Color",
        "The color for line numbers in the Definitions Pane.");
    add(OptionConstants.DEFINITIONS_LINE_NUMBER_BACKGROUND_COLOR, "Line Number Background Color",
        "The background color for line numbers in the Definitions Pane.");
    add(OptionConstants.DEFINITIONS_MATCH_COLOR, "Brace-matching Color",
        "The color for matching brace highlights in the Definitions Pane.");
    add(OptionConstants.COMPILER_ERROR_COLOR, "Compiler Error Color",
        "The color for compiler error highlights in the Definitions Pane.");
    add(OptionConstants.BOOKMARK_COLOR, "Bookmark Color",
        "The color for bookmarks in the Definitions Pane.");
    for (int i = 0; i < OptionConstants.FIND_RESULTS_COLORS.length; ++i) {
      add(OptionConstants.FIND_RESULTS_COLORS[i], "Find Results Color "+(i+1),
          "A color for highlighting find results in the Definitions Pane.");
    }
    add(OptionConstants.DEBUG_BREAKPOINT_COLOR, "Debugger Breakpoint Color",
        "The color for breakpoints in the Definitions Pane.");
    add(OptionConstants.DEBUG_BREAKPOINT_DISABLED_COLOR, "Disabled Debugger Breakpoint Color",
        "The color for disabled breakpoints in the Definitions Pane.");
    add(OptionConstants.DEBUG_THREAD_COLOR, "Debugger Location Color",
        "The color for the location of the current suspended thread in the Definitions Pane.");
    add(OptionConstants.SYSTEM_OUT_COLOR, "System.out Color",
        "The color for System.out in the Interactions and Console Panes.");
    add(OptionConstants.SYSTEM_ERR_COLOR, "System.err Color",
        "The color for System.err in the Interactions and Console Panes.");
    add(OptionConstants.SYSTEM_IN_COLOR, "System.in Color",
        "The color for System.in in the Interactions Pane.");
    add(OptionConstants.INTERACTIONS_ERROR_COLOR, "Interactions Error Color",
        "The color for interactions errors in the Interactions Pane.");
    add(OptionConstants.DEBUG_MESSAGE_COLOR, "Debug Message Color",
        "The color for debugger messages in the Interactions Pane.");
    add(OptionConstants.DRJAVA_ERRORS_BUTTON_COLOR, "DrJava Errors Button Background Color",
        "The background color of the \"Errors\" button used to show internal DrJava errors.");
    add(OptionConstants.RIGHT_MARGIN_COLOR, "Right Margin Color",
        "The color of the right margin line, if displayed.");
    
    add(OptionConstants.WINDOW_STORE_POSITION,
        "Save Main Window Position",
        "Whether to save and restore the size and position of the main window.");
    
    add(OptionConstants.DIALOG_CLIPBOARD_HISTORY_STORE_POSITION,
        "Save \"Clipboard History\" Dialog Position",
        "Whether to save and restore the size and position of the \"Clipboard History\" dialog.");
    add(OptionConstants.DIALOG_GOTOFILE_STORE_POSITION,
        "Save \"Go to File\" Dialog Position",
        "Whether to save and restore the size and position of the \"Go to File\" dialog.");    
    add(OptionConstants.DIALOG_COMPLETE_WORD_STORE_POSITION,
        "Save \"Auto-Complete Word\" Dialog Position",
        "Whether to save and restore the size and position of the \"Auto-Complete Word\" dialog.");
    
    add(OptionConstants.DIALOG_JAROPTIONS_STORE_POSITION,
        "Save \"Create Jar File from Project\" Dialog Position",
        "Whether to save and restore the position of the \"Create Jar File from Project\" dialog.");
    add(OptionConstants.DIALOG_OPENJAVADOC_STORE_POSITION,
        "Save \"Open Javadoc\" Dialog Position",
        "Whether to save and restore the size and position of the \"Open Javadoc\" dialog.");
    add(OptionConstants.DIALOG_AUTOIMPORT_STORE_POSITION,
        "Save \"Auto Import\" Dialog Position",
        "Whether to save and restore the size and position of the \"Auto Import\" dialog.");
    
    add(OptionConstants.DIALOG_EXTERNALPROCESS_STORE_POSITION,
        "Save \"Execute External Process\" Dialog Position",
        "Whether to save and restore the position of the \"Execute External Process\" dialog.");
    
    add(OptionConstants.DIALOG_EDITEXTERNALPROCESS_STORE_POSITION,
        "Save \"Edit External Process\" Dialog Position",
        "Whether to save and restore the position of the \"Edit External Process\" dialog.");
    
    add(OptionConstants.DIALOG_OPENJAVADOC_STORE_POSITION,
        "Save \"Open Javadoc\" Dialog Position",
        "Whether to save and restore the position of the \"Open Javadoc\" dialog.");
    add(OptionConstants.DIALOG_TABBEDPANES_STORE_POSITION,
        "Save \"Tabbed Panes\" Window Position",
        "Whether to save and restore the position of the \"Tabbed Panes\" window.");
    
    add(OptionConstants.DETACH_TABBEDPANES,
        "Detach Tabbed Panes",
        "Whether to detach the tabbed panes and display them in a separate window.");                         
    
    add(OptionConstants.DIALOG_DEBUGFRAME_STORE_POSITION,
        "Save \"Debugger\" Window Position",
        "Whether to save and restore the position of the \"Debugger\" window.");
    
    add(OptionConstants.DETACH_DEBUGGER,
        "Detach Debugger",
        "Whether to detach the debugger and display it in a separate window.");
    
    /** Adds all of the components for the Key Bindings panel of the preferences window.
      */
    
    add(OptionConstants.DEBUG_SOURCEPATH, "Sourcepath",
        "<html>Any directories in which to search for source<br>" +
        "files when stepping in the Debugger.</html>");
    add(OptionConstants.DEBUG_STEP_JAVA,
        "Step Into Java Classes", 
        "<html>Whether the Debugger should step into Java library classes,<br>" +
        "including java.*, javax.*, sun.*, com.sun.*, com.apple.eawt.*, and com.apple.eio.*</html>");
    add(OptionConstants.DEBUG_STEP_INTERPRETER,
        "Step Into Interpreter Classes",
        "<html>Whether the Debugger should step into the classes<br>" +
        "used by the Interactions Pane (DynamicJava).</html>");
    add(OptionConstants.DEBUG_STEP_DRJAVA,
        "Step Into DrJava Classes",
        "Whether the Debugger should step into DrJava's own class files.");
    add(OptionConstants.DEBUG_STEP_EXCLUDE,
        "Classes/Packages To Exclude",
        "<html>Any classes that the debuggger should not step into.<br>" +
        "Should be a list of fully-qualified class names.<br>" +
        "To exclude a package, add <code>packagename.*</code> to the list.</html>");
    add(OptionConstants.DEBUG_AUTO_IMPORT,
        "Auto-Import after Breakpoint/Step",
        "<html>Whether the Debugger should automatically import packages<br>"+
        "and classes again after a breakpoint or step.</html>");
    
    add(OptionConstants.AUTO_STEP_RATE,
        "Auto-Step Rate in ms",
        "<html>A defined rate in ms at which the debugger automatically steps into/over each line of code.<br>" +
        "Value entered must be an integer value. </html>");                                                            
    add(OptionConstants.DEBUG_EXPRESSIONS_AND_METHODS_IN_WATCHES,
        "Allow Expressions and Method Calls in Watches",
        "<html>Whether the Debugger should allow expressions and method<br>"+
        "calls in watches. These may have side effects and can cause<br>"+
        "delays during the debug process.</html>");
    
    add(OptionConstants.JAVADOC_API_REF_VERSION,
        "Java Version for \"Open Java API Javadoc\"",
        "Version of the Java API documentation to be used.");
    add(OptionConstants.JAVADOC_ACCESS_LEVEL,
        "Access Level", 
        "<html>Fields and methods with access modifiers at this level<br>" +
        "or higher will be included in the generated Javadoc.</html>");
    add(OptionConstants.JAVADOC_LINK_VERSION,
        "Java Version for Javadoc Links", 
        "Version of Java for generating links to online Javadoc documentation.");
    add(OptionConstants.JAVADOC_1_6_LINK,
        "Javadoc 1.6 URL",
        "URL for the Java 1.6 API, for generating links to library classes.");
    add(OptionConstants.JAVADOC_1_7_LINK,
        "Javadoc 1.7 URL",
        "URL for the Java 1.7 API, for generating links to library classes.");
    add(OptionConstants.JAVADOC_1_8_LINK,
        "Javadoc 1.8 URL",
        "URL for the Java 1.8 API, for generating links to library classes.");
    add(OptionConstants.JUNIT_LINK,
        "JUnit URL",
        "URL for the JUnit API, for \"Open Java API Javadoc\".");
    
    add(OptionConstants.JAVADOC_ADDITIONAL_LINKS, "Additional Javadoc URLs",
        "<html>Additional URLs with Javadoc, for \"Open Java API Javadoc\"<br>" +
        "and auto-completion.</html>");
    add(OptionConstants.JAVADOC_DESTINATION,
        "Default Destination Directory",
        "Optional default directory for saving Javadoc documentation.");
    
    add(OptionConstants.JAVADOC_CUSTOM_PARAMS,
        "Custom Javadoc Parameters",
        "Any extra flags or parameters to pass to Javadoc.");
    
    // Note: JAVADOC_FROM_ROOTS is intended to set the -subpackages flag, but I don't think that's something
    // we should support -- in general, we only support performing operations on the files that are open.
    // (dlsmith r4189)
//    add(OptionConstants.JAVADOC_FROM_ROOTS,
//        "Generate Javadoc From Source Roots"
//          "<html>Whether \"Javadoc All\" should generate Javadoc for all packages<br>" +
//        "in an open document's source tree, rather than just the document's<br>" +
//        "own package and sub-packages.</html>");
    
    add(OptionConstants.QUIT_PROMPT, "Prompt Before Quit",
        "Whether DrJava should prompt the user before quitting.");
    
    
    // Interactions
    add(OptionConstants.INTERACTIONS_RESET_PROMPT,
        "Prompt Before Resetting Interactions Pane",
        "<html>Whether DrJava should prompt the user before<br>" +
        "manually resetting the interactions pane.</html>");
    
    
    add(OptionConstants.INTERACTIONS_EXIT_PROMPT,
        "Prompt if Interactions Pane Exits Unexpectedly",
        "<html>Whether DrJava should show a dialog box if a program<br>" +
        "in the Interactions Pane exits without the user clicking Reset.</html>");
    
    
    // Javadoc
    add(OptionConstants.JAVADOC_PROMPT_FOR_DESTINATION,
        "Prompt for Javadoc Destination", 
        "<html>Whether Javadoc should always prompt the user<br>" +
        "to select a destination directory.</html>");
    
    
    
    // Clean
    add(OptionConstants.PROMPT_BEFORE_CLEAN,
        "Prompt before Cleaning Build Directory",
        "<html>Whether DrJava should prompt before cleaning the<br>" +
        "build directory of a project</html>");
    
    
    // Prompt to change the language level extensions (.dj0/.dj1->.dj, .dj2->.java)
    add(OptionConstants.PROMPT_RENAME_LL_FILES, "Prompt to Rename Old Language Level Files When Saving",
        "<html>Whether DrJava should prompt the user to rename old language level files.<br>"+
        "DrJava suggests to rename .dj0 and .dj1 files to .dj, and .dj2 files to .java.</html>");
    
    
    
    // Save before X
    add(OptionConstants.ALWAYS_SAVE_BEFORE_COMPILE,
        "Automatically Save Before Compiling",
        "<html>Whether DrJava should automatically save before<br>" +
        "recompiling or ask the user each time.</html>");
    
    
    add(OptionConstants.ALWAYS_COMPILE_BEFORE_JUNIT, 
        "Automatically Compile Before Testing",
        "<html>Whether DrJava should automatically compile before<br>" +
        "testing with JUnit or ask the user each time.</html>");
    
    
    add(OptionConstants.ALWAYS_SAVE_BEFORE_JAVADOC,
        "Automatically Save Before Generating Javadoc",
        "<html>Whether DrJava should automatically save before<br>" +
        "generating Javadoc or ask the user each time.</html>");
    
    
    add(OptionConstants.ALWAYS_COMPILE_BEFORE_JAVADOC,
        "Automatically Compile Before Generating Javadoc",
        "<html>Whether DrJava should automatically compile before<br>" +
        "generating Javadoc or ask the user each time.</html>");
    
    
    
    // These are very problematic features, and so are disabled for the forseeable future.
//    addOptionComponent(panel, 
//                       new BooleanOptionComponent(OptionConstants.ALWAYS_SAVE_BEFORE_RUN, 
//                                                  "Automatically Save and Compile Before Running Main Method", 
//                                                  this,
//                                                  "<html>Whether DrJava automatically saves and compiles before running<br>" +
//                                                  "a document's main method or explicitly asks the user each time.</html>");
//    addOptionComponent(panel, 
//                       new BooleanOptionComponent(OptionConstants.ALWAYS_SAVE_BEFORE_DEBUG, 
//                                                  "Automatically Save and Compile Before Debugging", 
//                                                  this,
//                                                  "<html>Whether DrJava automatically saves and compiles before<br>" +
//                                                  "debugging or explicitly asks the user each time.</html>");
    
    // Warnings
    add(OptionConstants.WARN_BREAKPOINT_OUT_OF_SYNC,
        "Warn on Breakpoint if Out of Sync", 
        "<html>Whether DrJava should warn the user if the class file<br>" +
        "is out of sync before setting a breakpoint in that file.</html>");
    
    add(OptionConstants.WARN_DEBUG_MODIFIED_FILE,
        "Warn if Debugging Modified File", 
        "<html>Whether DrJava should warn the user if the file being<br>" +
        "debugged has been modified since its last save.</html>");
    
    add(OptionConstants.WARN_CHANGE_LAF,
        "Warn to Restart to Change Look and Feel",
        "<html>Whether DrJava should warn the user that look and feel<br>" +
        "changes will not be applied until DrJava is restarted.</html>.");
    
    add(OptionConstants.WARN_CHANGE_THEME,
        "Warn to Restart to Change Theme",
        "<html>Whether DrJava should warn the user that theme<br>" +
        "changes will not be applied until DrJava is restarted.</html>.");
    
    add(OptionConstants.WARN_CHANGE_DCP,
        "Warn to Restart to Change Default Compiler Preference",
        "<html>Whether DrJava should warn the user that default compiler preference<br>" +
        "changes will not be applied until DrJava is restarted.</html>.");
    
    add(OptionConstants.WARN_CHANGE_MISC,
        "Warn to Restart to Change Preferences (other)",
        "<html>Whether DrJava should warn the user that preference<br>" +
        "changes will not be applied until DrJava is restarted.</html>.");
    
    add(OptionConstants.WARN_CHANGE_INTERACTIONS,
        "Warn to Reset to Change Interactions",
        "<html>Whether DrJava should warn the user that preference<br>" +
        "changes will not be applied until the Interactions Pane<br>" +
        "is reset.</html>.");
    
    add(OptionConstants.WARN_PATH_CONTAINS_POUND,
        "Warn if File's Path Contains a '#' Symbol",
        "<html>Whether DrJava should warn the user if the file being<br>" +
        "saved has a path that contains a '#' symbol.<br>" +
        "Users cannot use such files in the Interactions Pane<br>" +
        "because of a bug in Java.</html>");
    
    
    add(OptionConstants.DIALOG_DRJAVA_ERROR_POPUP_ENABLED, 
        "Show a notification window when the first DrJava error occurs",
        "<html>Whether to show a notification window when the first DrJava error occurs.<br>" +
        "If this is disabled, only the \"DrJava Error\" button will appear.</html>");
    
    add(OptionConstants.WARN_IF_COMPIZ, 
        "Warn If Compiz Detected",
        "<html>Whether DrJava should warn the user if Compiz is running.<br>"+
        "Compiz and Java Swing are incompatible and can lead to crashes.</html>");
    
    add(OptionConstants.DELETE_LL_CLASS_FILES,
        "Delete language level class files?",
        "Whether DrJava should delete class files in directories with language level files.");
    
    add(OptionConstants.NEW_VERSION_NOTIFICATION,
        "Check for new versions?",
        "Whether DrJava should check for new versions on drjava.org.");
    add(OptionConstants.NEW_VERSION_NOTIFICATION_DAYS,
        "Days between new version check",
        "The number of days between automatic new version checks.");
    
    /* Dialog box options */
    add(OptionConstants.INDENT_INC,
        "Indent Level",
        "The number of spaces to use for each level of indentation.");
    add(OptionConstants.RECENT_FILES_MAX_SIZE, "Recent Files List Size",
        "<html>The number of files to remember in<br>" +
        "the recently used files list in the File menu.</html>");
    add(OptionConstants.BROWSER_HISTORY_MAX_SIZE,
        "Maximum Size of Browser History", 
        "Determines how many entries are kept in the browser history.");
    
    /* Check box options */
    add(OptionConstants.AUTO_CLOSE_COMMENTS, "Automatically Close Block Comments", 
        "<html>Whether to automatically insert a closing comment tag (\"*/\")<br>" +
        "when the enter key is pressed after typing a new block comment<br>" +
        "tag (\"/*\" or \"/**\").</html>");
    String runWithAssertMsg = 
      "<html>Whether to execute <code>assert</code> statements in classes running in the interactions pane.</html>";
    add(OptionConstants.RUN_WITH_ASSERT, "Enable Assert Statement Execution", 
        runWithAssertMsg);
    
    add(OptionConstants.BACKUP_FILES, "Keep Emacs-style Backup Files", 
        "<html>Whether DrJava should keep a backup copy of each file that<br>" +
        "the user modifies, saved with a '~' at the end of the filename.</html>");
    add(OptionConstants.RESET_CLEAR_CONSOLE, "Clear Console After Interactions Reset", 
        "Whether to clear the Console output after resetting the Interactions Pane.");
    
    add(OptionConstants.FIND_REPLACE_FOCUS_IN_DEFPANE, 
        "Focus on the definitions pane after find/replace", 
        "<html>Whether to focus on the definitions pane after executing a find/replace operation.<br>" +
        "If this is not selected, the focus will be in the Find/Replace pane.</html>");
    add(OptionConstants.DRJAVA_USE_FORCE_QUIT, 
        "Forcefully Quit DrJava",
        "<html>On some platforms, DrJava does not shut down properly when files are open<br>"+
        "(namely tablet PCs). Check this option to force DrJava to close.</html>");
    add(OptionConstants.REMOTE_CONTROL_ENABLED, 
        "Enable Remote Control",
        "<html>Whether DrJava should listen to a socket (see below) so it<br>"+
        "can be remote controlled and told to open files.<br>"+
        "(Changes will not be applied until DrJava is restarted.)</html>");
    add(OptionConstants.REMOTE_CONTROL_PORT, 
        "Remote Control Port",
        "<html>A running instance of DrJava can be remote controlled and<br>"+
        "told to open files. This specifies the port used for remote control.<br>" + 
        "(Changes will not be applied until DrJava is restarted.)</html>");
    add(OptionConstants.FOLLOW_FILE_DELAY, 
        "Follow File Delay",
        "<html>The delay in milliseconds that has to elapse before DrJava will check<br>"+
        "if a file that is being followed or the output of an external process has changed.</html>");
    add(OptionConstants.FOLLOW_FILE_LINES, 
        "Maximum Lines in \"Follow File\" Window",
        "<html>The maximum number of lines to keep in a \"Follow File\"<br>"+
        "or \"External Process\" pane. Enter 0 for unlimited.</html>");
    
// Any lightweight parsing has been disabled until we have something that is beneficial and works better in the background.
//    add(OptionConstants.LIGHTWEIGHT_PARSING_ENABLED, 
//                                                  "Perform lightweight parsing"
//                                                  "<html>Whether to continuously parse the source file for useful information.<br>" +
//                                                  "Enabling this option might introduce delays when editing files.<html>"));
//    add(OptionConstants.DIALOG_LIGHTWEIGHT_PARSING_DELAY, "Light-weight parsing delay in milliseconds"
//                                                  "The amount of time DrJava will wait after the last keypress before beginning to parse."));
    
    add(OptionConstants.MASTER_JVM_XMX, 
        "Maximum Heap Size for Main JVM in MB", 
        "The maximum heap the Main JVM can use. Select blank for default.");
    add(OptionConstants.MASTER_JVM_ARGS, "JVM Args for Main JVM", 
        "The command-line arguments to pass to the Main JVM.");
    add(OptionConstants.SLAVE_JVM_XMX, 
        "Maximum Heap Size for Interactions JVM in MB", 
        "The maximum heap the Interactions JVM can use. Select blank for default");
    add(OptionConstants.SLAVE_JVM_ARGS, "JVM Args for Interactions JVM",
        "The command-line arguments to pass to the Interactions JVM.");    
    
    /** Adds all of the components for the Compiler Options Panel of the preferences window
      */
    add(OptionConstants.SHOW_UNCHECKED_WARNINGS, "Show Unchecked Warnings", 
        "<html>Warn about unchecked conversions involving parameterized types.</html>");
    
    
    add(OptionConstants.SHOW_DEPRECATION_WARNINGS, "Show Deprecation Warnings", 
        "<html>Warn about each use or override of a deprecated method, field, or class.</html>");
    
    
    add(OptionConstants.SHOW_PATH_WARNINGS, "Show Path Warnings",
        "<html>Warn about nonexistent members of the classpath and sourcepath.</html>");
    
    
    add(OptionConstants.SHOW_SERIAL_WARNINGS, "Show Serial Warnings", 
        "<html>Warn about missing <code>serialVersionUID</code> definitions on serializable classes.</html>");
    
    
    add(OptionConstants.SHOW_FINALLY_WARNINGS, "Show Finally Warnings",
        "<html>Warn about <code>finally</code> clauses that cannot complete normally.</html>");
    
    
    add(OptionConstants.SHOW_FALLTHROUGH_WARNINGS, "Show Fall-Through Warnings",
        "<html>Warn about <code>switch</code> block cases that fall through to the next case.</html>");
    
    /*
     * The drop down box containing the compiler names
     */
    add(OptionConstants.COMPILER_PREFERENCE_CONTROL.evaluate(), "Compiler Preference", 
        "Which compiler is prefered?");
    
    
    /** Add all of the components for the Interactions panel of the preferences window. */
    add(OptionConstants.FIXED_INTERACTIONS_DIRECTORY,
        "Interactions Working Directory",
        "<html>Working directory for the Interactions Pane (unless<br>"+
        "a project working directory has been set).</html>");
    add(OptionConstants.STICKY_INTERACTIONS_DIRECTORY,
        "<html><p align=\"right\">" + 
        StringOps.
          splitStringAtWordBoundaries("Restore last working directory of the Interactions pane on start up",
                                      33, "<br>", SEPS),
        "<html>Whether to restore the last working directory of the Interaction pane on start up,<br>" +
        "or to always use the value of the \"user.home\" Java property<br>"+
        "(currently "+System.getProperty("user.home")+").");
    
    add(OptionConstants.SMART_RUN_FOR_APPLETS_AND_PROGRAMS, 
        "Smart Run Command",
        "<html>Whether the Run button and meni item should automatically detect<br>"+
        "applets and ACM Java Task Force programs (subclasses of acm.program.Program).</html>");
    
    add(OptionConstants.HISTORY_MAX_SIZE, "Size of Interactions History",
        "The number of interactions to remember in the history.");
    add(OptionConstants.DIALOG_AUTOIMPORT_ENABLED, 
        "Enable the \"Auto Import\" Dialog",
        "<html>Whether DrJava should open the \"Auto Import\" dialog when<br>"+
        "an undefined class is encountered in the Interactions Pane.</html>");
    add(OptionConstants.INTERACTIONS_AUTO_IMPORT_CLASSES, "Classes to Auto-Import",
        "<html>List of classes to auto-import every time the<br>"+
        "Interaction Pane is reset or started. Examples:<br><br>"+
        "java.io.File<br>"+
        "java.util.*</html>");
    
    add(OptionConstants.DYNAMICJAVA_ACCESS_CONTROL,
        "Enforce access control", 
        "What kind of access control should DrJava enforce in the Interactions Pane?");
    add(OptionConstants.DYNAMICJAVA_REQUIRE_SEMICOLON, 
        "Require Semicolon",
        "<html>Whether DrJava should require a semicolon at the<br>"+
        "end of a statement in the Interactions Pane.</html>");
    add(OptionConstants.DYNAMICJAVA_REQUIRE_VARIABLE_TYPE, 
        "Require Variable Type",
        "<html>Whether DrJava should require a variable type for<br>"+
        "variable declarations in the Interactions Pane.</html>");
    
    /** Add all of the components for the JUnit panel of the preferences window. */
    add(OptionConstants.JUNIT_LOCATION_ENABLED, "Use external JUnit",
        "<html>If this is enabled, DrJava will use the JUnit configured<br>"+
        "below under 'JUnit/ConcJUnit Location'. If it is disabled,<br>"+
        "DrJava will use the JUnit that is built-in.</html>");
    add(OptionConstants.JUNIT_LOCATION, "JUnit/ConcJUnit Location",
        "<html>Optional location of the JUnit or ConcJUnit jar file.<br>"+
        "(Changes will not be applied until the Interactions Pane<br>"+
        "is reset.)</html>");
    add(OptionConstants.CONCJUNIT_CHECKS_ENABLED, "Enabled ConcJUnit Checks",
        "<html>The concurrent unit testing checks that should be performed.<br>"+
        "'none' uses plain JUnit. ConcJUnit can also detect failures in<br>"+
        "all threads ('all-threads'), detect threads that did not end in<br>"+
        "time ('all-threads, join'), and threads that ended in time only<br>"+
        "because they were lucky ('all-threads, nojoin, lucky).<br>"+
        "The last setting requires a 'ConcJUnit Runtime Location' to be set.</html>");
    
    add(OptionConstants.RT_CONCJUNIT_LOCATION, "ConcJUnit Runtime Location",
        "<html>Optional location of the Java Runtime Library processed<br>"+
        "to generate &quot;lucky&quot; warnings. If left blank, &quot;lucky&quot; warnings<br>"+
        "will not be generated. This setting is deactivated if the path to<br>"+
        "ConcJUnit has not been specified above.<br>" + 
        "(Changes will not be applied until the Interactions Pane is reset.)</html>");
    
    
    add(OptionConstants.FORCE_TEST_SUFFIX,
        "Require test classes in projects to end in \"Test\"",
        "Whether to force test classes in projects to end in \"Test\".");

    add(OptionConstants.LANGUAGE_LEVEL,
        "Language Level",
        "The language level DrJava currently uses.");
  } 
}
