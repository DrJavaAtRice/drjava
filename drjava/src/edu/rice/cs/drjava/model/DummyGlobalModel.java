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

package edu.rice.cs.drjava.model;

import java.awt.print.PageFormat;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import edu.rice.cs.util.AbsRelFile;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.OperationCanceledException;

import edu.rice.cs.util.docnavigation.IDocumentNavigator;
import edu.rice.cs.util.swing.DocumentIterator;
import edu.rice.cs.util.text.AbstractDocumentInterface;
import edu.rice.cs.util.text.ConsoleDocument;

import edu.rice.cs.drjava.model.definitions.DefinitionsEditorKit;
import edu.rice.cs.drjava.model.debug.Breakpoint;
import edu.rice.cs.drjava.model.debug.Debugger;
import edu.rice.cs.drjava.model.javadoc.JavadocModel;
import edu.rice.cs.drjava.model.repl.DefaultInteractionsModel;
import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.repl.InteractionsDJDocument;
import edu.rice.cs.drjava.model.repl.InteractionsScriptModel;
import edu.rice.cs.drjava.model.compiler.CompilerModel;
import edu.rice.cs.drjava.model.junit.JUnitModel;
import edu.rice.cs.drjava.project.DocumentInfoGetter;
import edu.rice.cs.drjava.project.MalformedProjectFileException;
import edu.rice.cs.drjava.config.OptionParser;

/** Concrete implementation of GlobalModel that always throws UnsupportedOperationExceptions.
  * @version $Id: DummyGlobalModel.java 5727 2012-09-30 03:58:32Z rcartwright $
  */
public class DummyGlobalModel implements GlobalModel {
  /** Since this is not supposed to be used, we need to throw an exception OTHER than the ones it officially supports.
    * @throws UnsupportedOperationException
    */
  
  public void addListener(GlobalModelListener listener) {
    throw new UnsupportedOperationException("Tried to call addListener on a Dummy");
  }
  
  public void removeListener(GlobalModelListener listener) {
    throw new UnsupportedOperationException("Tried to call removeListener on a Dummy");
  }
  
  public GlobalEventNotifier getNotifier() {
    throw new UnsupportedOperationException("Tried to call getNotifier on a Dummy");
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
  
  public RegionManager<Breakpoint> getBreakpointManager() {
    throw new UnsupportedOperationException("Tried to call getBreakpointManager on a Dummy");
  }
  
  public RegionManager<MovingDocumentRegion> getBookmarkManager() {
    throw new UnsupportedOperationException("Tried to call getBookmarkManager on a Dummy");
  }
  
//  public List<RegionManager<MovingDocumentRegion>> getFindResultsManagers() {
//    throw new UnsupportedOperationException("Tried to call getFindResultsManagers on a Dummy");
//  }
  
  public RegionManager<MovingDocumentRegion> createFindResultsManager() {
    throw new UnsupportedOperationException("Tried to call createFindResultsManager on a Dummy");
  }
  
  public void removeFindResultsManager(RegionManager<MovingDocumentRegion> rm) {
    throw new UnsupportedOperationException("Tried to call disposeFindResultsManager on a Dummy");
  }
  
  public BrowserHistoryManager getBrowserHistoryManager() {
    throw new UnsupportedOperationException("Tried to call getBookmarkManager on a Dummy");
  }
  
  public void addToBrowserHistory() {
    throw new UnsupportedOperationException("Tried to call getBookmarkManager on a Dummy");
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

  public OpenDefinitionsDocument newFile(String text) {
    throw new UnsupportedOperationException("Tried to call newFile on a Dummy");
  }
  
  public OpenDefinitionsDocument newTestCase(String name, boolean makeSetUp, boolean makeTearDown) {
    throw new UnsupportedOperationException("Tried to call newTest on a Dummy");
  }
  
  public OpenDefinitionsDocument openFile(FileOpenSelector com) throws IOException, OperationCanceledException, 
    AlreadyOpenException {
    throw new UnsupportedOperationException("Tried to call openFile on a Dummy");
  }
  
  public OpenDefinitionsDocument[] openFiles(FileOpenSelector com) throws IOException, OperationCanceledException, 
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

  public boolean closeFiles(List<OpenDefinitionsDocument> docs) {
    throw new UnsupportedOperationException("Tried to call closeFiles on a Dummy");
  }
  
  public void openFolder(File dir, boolean rec, String ext)
    throws IOException, OperationCanceledException, AlreadyOpenException {
    throw new UnsupportedOperationException("Tried to call openFolder on a Dummy");
  }
  
  public void setAutoRefreshStatus(boolean status) { 
    throw new UnsupportedOperationException("Tried to call setAutoRefreshStatus on a Dummy"); 
  }
  
  public boolean getAutoRefreshStatus() {
    throw new UnsupportedOperationException("Tried to call getAutoRefreshStatus on a Dummy"); }
  
  public Map<OptionParser<?>,String> getPreferencesStoredInProject() {
    throw new UnsupportedOperationException("Tried to call getPreferencesStoredInProject on a Dummy"); }

  public void setPreferencesStoredInProject(Map<OptionParser<?>,String> sp) {
    throw new UnsupportedOperationException("Tried to call setPreferencesStoredInProject on a Dummy"); }
  
  public void saveAllFiles(FileSaveSelector com) throws IOException {
    throw new UnsupportedOperationException("Tried to call saveAllFiles on a Dummy");
  }
  
  public void createNewProject(File f) {
    throw new UnsupportedOperationException("Tried to call createNewProject on a Dummy");
  }
  
  public void configNewProject() throws IOException {
    throw new UnsupportedOperationException("Tried to call configNewProject on a Dummy");
  }
  
  public void saveProject(File f, HashMap<OpenDefinitionsDocument,DocumentInfoGetter> ht) throws IOException {
    throw new UnsupportedOperationException("Tried to call saveProject on a Dummy");
  }
  
  public void reloadProject(File f, HashMap<OpenDefinitionsDocument,DocumentInfoGetter> ht) throws IOException {
    throw new UnsupportedOperationException("Tried to call reloadProject on a Dummy");
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
  
  public void openProject(File file) throws IOException, MalformedProjectFileException {
    throw new UnsupportedOperationException("Tried to call openProject on a Dummy");
  }
  
  public void closeProject(boolean quitting) {
    throw new UnsupportedOperationException("Tried to call closeProject on a Dummy");
  }
  
  public File getSourceFile(String fileName) {
    throw new UnsupportedOperationException("Tried to call getSourceFile on a Dummy");
  }
  
  public File findFileInPaths(String fileName, Iterable<File> paths) {
    throw new UnsupportedOperationException("Tried to call getSourceFileFromPaths on a Dummy");
  }
  
  public Iterable<File> getSourceRootSet() {
    throw new UnsupportedOperationException("Tried to call getSourceRootSet on a Dummy");
  } 
  
  public String getCompletePath(int index) {
    throw new UnsupportedOperationException("Tried to call getDisplayFullPath on a Dummy");
  }
  
  public DefinitionsEditorKit getEditorKit() {
    throw new UnsupportedOperationException("Tried to call getEditorKit on a Dummy");
  }
  
  public DocumentIterator getDocumentIterator() {
    throw new UnsupportedOperationException("Tried to call getDocumentIterator on a Dummy");
  }
  
  public void refreshActiveDocument() {
    throw new UnsupportedOperationException("Tried to call refreshActiveDocument on a Dummy");
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
  
  public void systemInEcho(String s) { 
    throw new UnsupportedOperationException("Tried to call systemInEcho on a Dummy");
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
  
  public void resetInteractions(File wd, boolean forceReset) { 
    throw new UnsupportedOperationException("Tried to call resetInteractions on a Dummy");
  }
  
  public void interpretCurrentInteraction() {
    throw new UnsupportedOperationException("Tried to call interpretCurrentInteraction on a Dummy");
  }
  
  public Iterable<File> getInteractionsClassPath() {
    throw new UnsupportedOperationException("Tried to call getInteractionsClasspath on a Dummy");
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

  public void saveConsoleCopy(ConsoleDocument doc, FileSaveSelector selector) throws IOException {
    throw new UnsupportedOperationException("Tried to call saveConsoleCopy on a Dummy");
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
  
  public Iterable<File> getClassPath() {
    throw new UnsupportedOperationException("Tried to call getClassPath on a Dummy");
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
  
  public void forceQuit() {
    throw new UnsupportedOperationException("Tried to call forceQuit on a Dummy");
  }
  
  public int getDocumentCount() {
    throw new UnsupportedOperationException("Tried to call getDocumentCount on a Dummy");
  }
  
  public int getNumCompilerErrors() {
    throw new UnsupportedOperationException("Tried to call getNumCompilerErrors on a Dummy");
  }
  
  public void setNumCompilerErrors(int num) {
    throw new UnsupportedOperationException("Tried to call setNumCompilerErrors on a Dummy");
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
  
//  public void junitAll() {
//     throw new UnsupportedOperationException("Tried to call junitAll on a Dummy");
//  }
  
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
  
  public void setMainClass(String f) {
    throw new UnsupportedOperationException("Tried to call setMainClass on a Dummy");
  }
  
  public String getMainClass() {
    throw new UnsupportedOperationException("Tried to call getMainClass on a Dummy");
  }
  
  public File getMainClassContainingFile() {
    throw new UnsupportedOperationException("Tried to call getMainClass on a Dummy");
  }
  
  public Iterable<AbsRelFile> getExtraProjectClassPath() {
    throw new UnsupportedOperationException("Tried to call getExtraClasspath on a Dummy");
  }
  
  public void setExtraClassPath(Iterable<AbsRelFile> cp) {
    throw new UnsupportedOperationException("Tried to call setExtraClasspath on a Dummy");
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
  
  public boolean inProjectPath(OpenDefinitionsDocument doc) {
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
  
  public boolean hasOutOfSyncDocuments(List<OpenDefinitionsDocument> lod) {
    throw new UnsupportedOperationException("Tried to call hasOutOfSyncDocuments on a Dummy");
  }

  public List<OpenDefinitionsDocument> getOutOfSyncDocuments() {
    throw new UnsupportedOperationException("Tried to call getOutOfSyncDocuments on a Dummy");
  }
  
  public List<OpenDefinitionsDocument> getOutOfSyncDocuments(List<OpenDefinitionsDocument> lod) {
    throw new UnsupportedOperationException("Tried to call getOutOfSyncDocuments on a Dummy");
  }
  
  public void cleanBuildDirectory() {
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

//  public List<OpenDefinitionsDocument> getLLOpenDefinitionsDocuments() {
//    throw new UnsupportedOperationException("Tried to getOpenDefinitionsDocuments on a Dummy!");
//  }

  public List<OpenDefinitionsDocument> getAuxiliaryDocuments() {
    throw new UnsupportedOperationException("Tried to getAuxiliaryDocuments on a Dummy!");
  }
  
  public List<OpenDefinitionsDocument> getSortedOpenDefinitionsDocuments() {
    throw new UnsupportedOperationException("Tried to getSortedOpenDefinitionsDocuments on a Dummy!");
  }
  
  public boolean hasModifiedDocuments() {
    throw new UnsupportedOperationException("Tried to call hasModifiedDocuments on a Dummy!");
  }
  public boolean hasModifiedDocuments(List<OpenDefinitionsDocument> lod) {
    throw new UnsupportedOperationException("Tried to call hasModifiedDocuments on a Dummy!");
  }
  
  public boolean hasUntitledDocuments() {
    throw new UnsupportedOperationException("Tried to call hasUntitliedDocuments on a Dummy!");
  }
  
  public String getCustomManifest() {
    throw new UnsupportedOperationException("Tried to call getCustomManifest on a Dummy!");
  }
  
  public void setCustomManifest(String manifest) {
    throw new UnsupportedOperationException("Tried to call setCustomManifest on a Dummy!");
  }
  
// Any lightweight parsing has been disabled until we have something that is beneficial and works better in the background.
//  /** @return the parsing control */
//  public LightWeightParsingControl getParsingControl() {
//    throw new UnsupportedOperationException("Tried to call getParsingControl on a Dummy!");
//  }
}
