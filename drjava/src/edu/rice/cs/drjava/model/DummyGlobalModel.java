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

package edu.rice.cs.drjava.model;

import java.awt.Container;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import java.awt.print.Pageable;
import java.awt.Font;
import java.awt.Color;

import javax.swing.ProgressMonitor;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.Style;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import edu.rice.cs.util.ClassPathVector;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.OrderedHashSet;
import edu.rice.cs.util.Pair;
import edu.rice.cs.util.SRunnable;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;

import edu.rice.cs.util.docnavigation.INavigationListener;
import edu.rice.cs.util.docnavigation.NodeData;
import edu.rice.cs.util.docnavigation.NodeDataVisitor;
import edu.rice.cs.util.docnavigation.AWTContainerNavigatorFactory;
import edu.rice.cs.util.docnavigation.IDocumentNavigator;
import edu.rice.cs.util.docnavigation.INavigatorItem;
import edu.rice.cs.util.docnavigation.INavigatorItemFilter;
import edu.rice.cs.util.docnavigation.JTreeSortNavigator;
import edu.rice.cs.util.swing.DocumentIterator;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.AbstractDocumentInterface;
import edu.rice.cs.util.text.ConsoleDocument;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.FileOption;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.model.print.DrJavaBook;

import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.DefinitionsEditorKit;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.model.definitions.DocumentUIListener;
import edu.rice.cs.drjava.model.definitions.CompoundUndoManager;
import edu.rice.cs.drjava.model.definitions.reducedmodel.HighlightStatus;
import edu.rice.cs.drjava.model.definitions.reducedmodel.IndentInfo;
import edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelState;
import edu.rice.cs.drjava.model.debug.Breakpoint;
import edu.rice.cs.drjava.model.debug.Debugger;
import edu.rice.cs.drjava.model.repl.DefaultInteractionsModel;
import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.repl.InteractionsDJDocument;
import edu.rice.cs.drjava.model.repl.InteractionsScriptModel;
import edu.rice.cs.drjava.model.compiler.CompilerModel;
import edu.rice.cs.drjava.model.junit.JUnitModel;
import edu.rice.cs.drjava.project.DocFile;
import edu.rice.cs.drjava.project.DocumentInfoGetter;
import edu.rice.cs.drjava.project.MalformedProjectFileException;
import edu.rice.cs.drjava.project.ProjectProfile;
import edu.rice.cs.drjava.project.ProjectFileIR;
import edu.rice.cs.drjava.project.ProjectFileParser;
import edu.rice.cs.drjava.model.cache.DCacheAdapter;
import edu.rice.cs.drjava.model.cache.DDReconstructor;
import edu.rice.cs.drjava.model.cache.DocumentCache;

/**
 * Concrete implementation of GlobalModel that always throws UnsupportedOperation exceptions.
 * @version $Id$
 */
public class DummyGlobalModel implements GlobalModel {
  /** Since this is not supposed to be used, we need to throw an exception OTHER than the ones it officially supports.
   *  @throws UnsupportedOperationException
   */
  
  public void addListener(GlobalModelListener listener) {
    throw new UnsupportedOperationException("Tried to call addListener on a Dummy");
  }

  public void removeListener(GlobalModelListener listener) {
    throw new UnsupportedOperationException("Tried to call removeListener on a Dummy");
  }
  
  public DefaultInteractionsModel getInteractionsModel() {
    throw new UnsupportedOperationException("Tried to call getInteractionsModel on a Dummy");
  }

  public CompilerModel getCompilerModel() {
    throw new UnsupportedOperationException("Tried to call getCompilerModel on a Dummy");
  }

  public JUnitModel getJUnitModel() {
    throw new UnsupportedOperationException("Tried to call getJUnitModel on a Dummy");
  }

  public JavadocModel getJavadocModel() {
    throw new UnsupportedOperationException("Tried to call getJavadocModel on a Dummy");
  }

  public Debugger getDebugger() {
    throw new UnsupportedOperationException("Tried to call getDebugger on a Dummy");
  }
  
  public IDocumentNavigator<OpenDefinitionsDocument> getDocumentNavigator() {
    throw new UnsupportedOperationException("Tried to call getDocumentNavigator on a Dummy");
  }
   
  public void setDocumentNavigator(IDocumentNavigator<OpenDefinitionsDocument> newnav) {
    throw new UnsupportedOperationException("Tried to call setDocumentNavigator on a Dummy");
  }

  public OpenDefinitionsDocument newFile() {
    throw new UnsupportedOperationException("Tried to call newFile on a Dummy");
  }

  public OpenDefinitionsDocument newTestCase(String name, boolean makeSetUp, boolean makeTearDown) {
    throw new UnsupportedOperationException("Tried to call newTest on a Dummy");
  }
  
  public OpenDefinitionsDocument openFile(FileOpenSelector com) throws IOException, OperationCanceledException, 
    AlreadyOpenException {
    throw new UnsupportedOperationException("Tried to call openFile on a Dummy");
  }

  public OpenDefinitionsDocument openFiles(FileOpenSelector com) throws IOException, OperationCanceledException, 
    AlreadyOpenException {
    throw new UnsupportedOperationException("Tried to call openFiles on a Dummy");
  }
  
  public boolean closeFile(OpenDefinitionsDocument doc) {
    throw new UnsupportedOperationException("Tried to call closeFile on a Dummy");
  }
  
  public boolean closeFileWithoutPrompt(OpenDefinitionsDocument doc) {
    throw new UnsupportedOperationException("Tried to call closeFileWithoutPrompt on a Dummy");
  }

  public boolean closeAllFiles() {
    throw new UnsupportedOperationException("Tried to call closeAllFiles on a Dummy");
  }

  public void openFolder(File dir, boolean rec) throws IOException, OperationCanceledException, AlreadyOpenException {
    throw new UnsupportedOperationException("Tried to call openFolder on a Dummy");
  }

  public void saveAllFiles(FileSaveSelector com) throws IOException {
    throw new UnsupportedOperationException("Tried to call saveAllFiles on a Dummy");
  }
  
  public void createNewProject(File f) {
     throw new UnsupportedOperationException("Tried to call createNewProject on a Dummy");
  }
  
  public void configNewProject() throws IOException {
     throw new UnsupportedOperationException("Tried to call configNewProject on a Dummy");
  }
  
  public void saveProject(File f, Hashtable<OpenDefinitionsDocument,DocumentInfoGetter> ht) throws IOException {
     throw new UnsupportedOperationException("Tried to call saveProject on a Dummy");
  }
  
  public String fixPathForNavigator(String path) throws IOException {
     throw new UnsupportedOperationException("Tried to call fixPathForNavigator on a Dummy");
  }

  public String getSourceBinTitle() {
     throw new UnsupportedOperationException("Tried to call getSourceBinTitle on a Dummy");
  }
  
  public String getExternalBinTitle() {
     throw new UnsupportedOperationException("Tried to call getExternalBinTitle on a Dummy");
  }
  
  public String getAuxiliaryBinTitle() {
     throw new UnsupportedOperationException("Tried to call getAuxiliaryBinTitle on a Dummy");
  }
  
  public void addAuxiliaryFile(OpenDefinitionsDocument doc) {
     throw new UnsupportedOperationException("Tried to call addAuxiliaryFile on a Dummy");
  }
  
  public void removeAuxiliaryFile(OpenDefinitionsDocument doc) {
     throw new UnsupportedOperationException("Tried to call removeAuxiliaryFile on a Dummy");
  }
  
  public File[] openProject(File file) throws IOException, MalformedProjectFileException {
     throw new UnsupportedOperationException("Tried to call openProject on a Dummy");
  }

  public void closeProject(boolean quitting) {
     throw new UnsupportedOperationException("Tried to call closeProject on a Dummy");
  }
  
  public File getSourceFile(String fileName) {
     throw new UnsupportedOperationException("Tried to call getSourceFile on a Dummy");
  }

  public File getSourceFileFromPaths(String fileName, List<File> paths) {
     throw new UnsupportedOperationException("Tried to call getSourceFileFromPaths on a Dummy");
  }

  public File[] getSourceRootSet() {
     throw new UnsupportedOperationException("Tried to call getSourceRootSet on a Dummy");
  } 
  
  public String getDisplayFileName(OpenDefinitionsDocument doc) {
     throw new UnsupportedOperationException("Tried to call getDisplayFilename on a Dummy");
  }
  
  public String getDisplayFullPath(int index) {
     throw new UnsupportedOperationException("Tried to call getDisplayFullPath on a Dummy");
  }

  public DefinitionsEditorKit getEditorKit() {
     throw new UnsupportedOperationException("Tried to call getEditorKit on a Dummy");
  }

  public DocumentIterator getDocumentIterator() {
     throw new UnsupportedOperationException("Tried to call getDocumentIterator on a Dummy");
  }

  public ConsoleDocument getConsoleDocument() {
     throw new UnsupportedOperationException("Tried to call getConsoleDocument on a Dummy");
  }

  public InteractionsDJDocument getSwingConsoleDocument() {
     throw new UnsupportedOperationException("Tried to call getSwingConsoleDocument on a Dummy");
  }

  public void resetConsole() {
     throw new UnsupportedOperationException("Tried to call resetConsole on a Dummy");
  }

  public void systemOutPrint(String s) {
     throw new UnsupportedOperationException("Tried to call systemOutPrint on a Dummy");
  }

  public void systemErrPrint(String s) {
     throw new UnsupportedOperationException("Tried to call systemErrPrint on a Dummy");
  }

  public InteractionsDocument getInteractionsDocument() {
     throw new UnsupportedOperationException("Tried to call getInteractionsDocument on a Dummy");
  }

  public InteractionsDJDocument getSwingInteractionsDocument() {
     throw new UnsupportedOperationException("Tried to call getSwingInteractionsDocument on a Dummy");
  }

  public void resetInteractions(File wd) {
     throw new UnsupportedOperationException("Tried to call resetInteractions on a Dummy");
  }

  public void waitForInterpreter() {
     throw new UnsupportedOperationException("Tried to call waitForInterpreter on a Dummy");
  }

  public void interpretCurrentInteraction() {
     throw new UnsupportedOperationException("Tried to call interpretCurrentInteraction on a Dummy");
  }

  public ClassPathVector getClassPath() {
     throw new UnsupportedOperationException("Tried to call getClasspath on a Dummy");
  }

  public ClassPathVector getExtraClassPath() {
     throw new UnsupportedOperationException("Tried to call getExtraClasspath on a Dummy");
  }

  public void setExtraClassPath(ClassPathVector cp) {
     throw new UnsupportedOperationException("Tried to call setExtraClasspath on a Dummy");
  }
  
  public void loadHistory(FileOpenSelector selector) throws IOException {
     throw new UnsupportedOperationException("Tried to call loadHistory on a Dummy");
  }

  public InteractionsScriptModel loadHistoryAsScript(FileOpenSelector s) throws IOException, OperationCanceledException
  {
     throw new UnsupportedOperationException("Tried to call loadHistoryAsScript on a Dummy");
  }

  public void clearHistory() {
     throw new UnsupportedOperationException("Tried to call clearHistory on a Dummy");
  }

  public void saveHistory(FileSaveSelector selector) throws IOException {
     throw new UnsupportedOperationException("Tried to call saveHistory on a Dummy");
  }

  public void saveHistory(FileSaveSelector selector, String editedVersion) throws IOException {
     throw new UnsupportedOperationException("Tried to call saveHistory on a Dummy");
  }

  public String getHistoryAsStringWithSemicolons() {
     throw new UnsupportedOperationException("Tried to call getHistoryAsStringWithSemicolons on a Dummy");
  }

  public String getHistoryAsString() {
     throw new UnsupportedOperationException("Tried to call getHistory on a Dummy");
  }

  public void printDebugMessage(String s) {
     throw new UnsupportedOperationException("Tried to call printDebugMessage on a Dummy");
  }
  
  public int getDebugPort() throws IOException {
     throw new UnsupportedOperationException("Tried to call getDebugPort on a Dummy");
  }

  public PageFormat getPageFormat() {
     throw new UnsupportedOperationException("Tried to call getPageFormat on a Dummy");
  }
  
  public void setPageFormat(PageFormat format) {
     throw new UnsupportedOperationException("Tried to call setPageFormat on a Dummy");
  }

  public void quit() {
     throw new UnsupportedOperationException("Tried to call quit on a Dummy");
  }
  
  public int getDocumentCount() {
     throw new UnsupportedOperationException("Tried to call getDocumentCount on a Dummy");
  }
  
  public OpenDefinitionsDocument getODDForDocument(AbstractDocumentInterface doc) {
     throw new UnsupportedOperationException("Tried to call getODDForDocument on a Dummy");
  }
    
  public List<OpenDefinitionsDocument> getNonProjectDocuments() {
     throw new UnsupportedOperationException("Tried to call getNonProjectDocuments on a Dummy");
  }

  public List<OpenDefinitionsDocument> getProjectDocuments() {
     throw new UnsupportedOperationException("Tried to call getProjectDocuments on a Dummy");
  }

  public boolean isProjectActive() {
     throw new UnsupportedOperationException("Tried to call isProjectActive on a Dummy");
  }
  
  public void junitAll() {
     throw new UnsupportedOperationException("Tried to call junitAll on a Dummy");
  }
  
  public File getProjectFile() {
     throw new UnsupportedOperationException("Tried to call getProjectFile on a Dummy");
  }
  
  public File[] getProjectFiles() {
     throw new UnsupportedOperationException("Tried to call getProjectFiles on a Dummy");
  }
  
  public File getProjectRoot() {
     throw new UnsupportedOperationException("Tried to call getProjectRoot on a Dummy");
  }
  
  public void setProjectFile(File f) {
    throw new UnsupportedOperationException("Tried to call setProjectFile on a Dummy");
  }

  public void setProjectRoot(File f) {
     throw new UnsupportedOperationException("Tried to call setProjectRoot on a Dummy");
  }

  public File getBuildDirectory() {
     throw new UnsupportedOperationException("Tried to call getBuildDirectory on a Dummy");
  }
  
  public void setBuildDirectory(File f) {
     throw new UnsupportedOperationException("Tried to call setBuildDirectory on a Dummy");
  }
  
  public File getMasterWorkingDirectory() {
     throw new UnsupportedOperationException("Tried to call getMasterWorkingDirectory on a Dummy");
  }

  public File getWorkingDirectory() {
     throw new UnsupportedOperationException("Tried to call getWorkingDirectory on a Dummy");
  }
  
  public void setWorkingDirectory(File f) {
     throw new UnsupportedOperationException("Tried to call setWorkingDirectory on a Dummy");
  }

  public void setMainClass(File f) {
     throw new UnsupportedOperationException("Tried to call setMainClass on a Dummy");
  }
  
  public File getMainClass() {
     throw new UnsupportedOperationException("Tried to call getMainClass on a Dummy");
  }
  
  public void setCreateJarFile(File f) {
     throw new UnsupportedOperationException("Tried to call setCreateJarFile on a Dummy");
  }
  
  public File getCreateJarFile() {
     throw new UnsupportedOperationException("Tried to call getCreateJarFile on a Dummy");
  }
  public void setCreateJarFlags(int f) {
     throw new UnsupportedOperationException("Tried to call setCreateJarFlags on a Dummy");
  }
  
  public int getCreateJarFlags() {
     throw new UnsupportedOperationException("Tried to call getCreateJarFlags on a Dummy");
  }


  public boolean inProject(File f) {
     throw new UnsupportedOperationException("Tried to call inProject on a Dummy");
  }

  public boolean isInProjectPath(OpenDefinitionsDocument doc) {
     throw new UnsupportedOperationException("Tried to call inProject on a Dummy");
  }

  public void setProjectChanged(boolean changed) {
     throw new UnsupportedOperationException("Tried to call setProjectChanged on a Dummy");
  }

  public boolean isProjectChanged() {
     throw new UnsupportedOperationException("Tried to call isProjectChanged on a Dummy");
  }

  public boolean hasOutOfSyncDocuments() {
     throw new UnsupportedOperationException("Tried to call hasOutOfSyncDocuments on a Dummy");
  }
  
  public void cleanBuildDirectory() throws FileMovedException, IOException {
     throw new UnsupportedOperationException("Tried to call cleanBuildDirectory on a Dummy");
  }
  
  public List<File> getClassFiles()  {
     throw new UnsupportedOperationException("Tried to call getClassFiles on a Dummy");
  }
  
  public OpenDefinitionsDocument getDocumentForFile(File file) throws IOException {
    throw new UnsupportedOperationException("Tried to getDocumentForFile on a Dummy with file: " + file);
  }

  public boolean isAlreadyOpen(File file) {
    throw new UnsupportedOperationException("Tried to call isAlreadyOpen on a Dummy with file: " + file);
  }

  public List<OpenDefinitionsDocument> getOpenDefinitionsDocuments() {
    throw new UnsupportedOperationException("Tried to getOpenDefinitionsDocuments on a Dummy!");
  }

  public boolean hasModifiedDocuments() {
    throw new UnsupportedOperationException("Tried to call hasModifiedDocuments on a Dummy!");
  }
  
  public boolean hasUntitledDocuments() {
    throw new UnsupportedOperationException("Tried to call hasUntitliedDocuments on a Dummy!");
  }
}
