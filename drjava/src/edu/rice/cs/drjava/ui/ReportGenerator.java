/*******************************************************************************
 * Copyright (c) 2009, 2014 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 *******************************************************************************/
package edu.rice.cs.drjava.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.*;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.LinkedList;
//import java.util.Properties;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;

import edu.rice.cs.drjava.model.*;

/**
 * This example creates a HTML report for eclipse like projects based on a
 * single execution data store called jacoco.exec. The report contains no
 * grouping information.
 * 
 * The class files under test must be compiled with debug information, otherwise
 * source highlighting will not work.
 */
public class ReportGenerator {

 private final String title;

 //private final File executionDataFile;
 //private final File classesDirectory;
 private final File sourceDirectory;
 private final File reportDirectory;
 private final List<OpenDefinitionsDocument> docs;

 //private ExecFileLoader execFileLoader;
 
 public static class TestTarget implements Runnable {

  public void run() {
   isPrime(7);
  }

  private boolean isPrime(final int n) {
   for (int i = 2; i * i <= n; i++) {
    if ((n ^ i) == 0) {
     return false;
    }
   }
   return true;
  }

 }
 
 /**
  * A class loader that loads classes from in-memory data.
  */
 public static class MemoryClassLoader extends ClassLoader {

  private final Map<String, byte[]> definitions = new HashMap<String, byte[]>();

  /**
   * Add a in-memory representation of a class.
   * 
   * @param name
   *            name of the class
   * @param bytes
   *            class definition
   */
  public void addDefinition(final String name, final byte[] bytes) {
   definitions.put(name, bytes);
  }

  @Override
  protected Class<?> loadClass(final String name, final boolean resolve)
    throws ClassNotFoundException {
   final byte[] bytes = definitions.get(name);
   if (bytes != null) {
    return defineClass(name, bytes, 0, bytes.length);
   }
   return super.loadClass(name, resolve);
  }

 }

 private InputStream getTargetClass(final String name) {
  final String resource = '/' + name.replace('.', '/') + ".class";
  return getClass().getResourceAsStream(resource);
 }
 
 private InputStream getTargetClass(final File file) throws Exception {
  return new FileInputStream(file.getCanonicalPath().replace(".java",".class"));
 }

 private String getTargetClassName(final OpenDefinitionsDocument doc) throws Exception {
  File file = doc.getFile();
  String path = file.getPath();
  String name = file.getName().replace(".java", "");
/*
  Properties properties = new Properties();
  try {
   properties.load(new FileInputStream(path));
  } catch (IOException e) {
    
  }
  return properties.getProperty("package").replace(";", "") + "." + name;
*/
  if(doc.getPackageName()!="")
   return doc.getPackageName() + "." + name;

  return name;
 }

 private void printCounter(final String unit, final ICounter counter) {
  final Integer missed = Integer.valueOf(counter.getMissedCount());
  final Integer total = Integer.valueOf(counter.getTotalCount());
  System.out.printf("%s of %s %s missed%n", missed, total, unit);
 }

 private String getColor(final int status) {
  switch (status) {
  case ICounter.NOT_COVERED:
   return "red";
  case ICounter.PARTLY_COVERED:
   return "yellow";
  case ICounter.FULLY_COVERED:
   return "green";
  }
  return "";
 }

 /**
  * Create a new generator based for the given project.
  * 
  * @param projectDirectory
  */
 public ReportGenerator(final List<OpenDefinitionsDocument> docs, File destDirectory) throws Exception  {
  //this.title = projectDirectory.getName();
  //this.executionDataFile = new File(projectDirectory, "jacoco.exec");
  //this.classesDirectory = new File(projectDirectory, "bin");
  //this.sourceDirectory =new File(projectDirectory, "src");
  this.docs = docs;
  File projectDirectory = docs.get(0).getFile().getParentFile(); 
  this.title = projectDirectory.getName(); 
  this.sourceDirectory = projectDirectory;
  this.reportDirectory = new File(destDirectory, "coveragereport");
 }

 /**
  * Create the report.
  * @throws Exception 
  */
 public void create() throws Exception {

  // Read the jacoco.exec file. Multiple data files could be merged
  // at this point
  //loadExecutionData();
  //final String targetName = TestTarget.class.getName();

  File target = docs.get(0).getFile();
  //File target = sourceDirectory.listFiles()[0].listFiles()[1];
  //File target2 = sourceDirectory.listFiles()[0].listFiles()[2];
  String targetName = getTargetClassName(docs.get(0));
  //String targetName2 = getTargetClassName(target2);
  
  // For instrumentation and runtime we need a IRuntime instance
  // to collect execution data:
  final IRuntime runtime = new LoggerRuntime();

  // The Instrumenter creates a modified version of our test target class
  // that contains additional probes for execution data recording:
  final Instrumenter instr = new Instrumenter(runtime);
  final byte[] instrumented = instr.instrument(getTargetClass(target), targetName);
  //final Instrumenter instr2 = new Instrumenter(runtime);
  //final byte[] instrumented2 = instr2.instrument(getTargetClass(targetName2), targetName2);
  
  // Now we're ready to run our instrumented class and need to startup the
  // runtime first:
  final RuntimeData data = new RuntimeData();
  runtime.startup(data);

  // In this tutorial we use a special class loader to directly load the
  // instrumented class definition from a byte[] instances.
  final MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
  memoryClassLoader.addDefinition(targetName, instrumented);
  //memoryClassLoader.addDefinition(targetName2, instrumented2);
  
  final Class<?> targetClass = memoryClassLoader.loadClass(targetName);

  // Here we execute our test target class through its Runnable interface:
  //final Runnable targetInstance = (Runnable) targetClass.newInstance();
  //targetInstance.run();
  
  //Execute the test target class 
     Method meth = targetClass.getMethod("main", String[].class);
     String[] params = new String[]{}; // init params accordingly
     meth.invoke(null, (Object) params); // static method doesn't have an instance

  // At the end of test execution we collect execution data and shutdown
  // the runtime:
  final ExecutionDataStore executionData = new ExecutionDataStore();
  final SessionInfoStore sessionInfos = new SessionInfoStore();
  data.collect(executionData, sessionInfos, false);
  runtime.shutdown();
  
  // Together with the original class definition we can calculate coverage
  // information:
  final CoverageBuilder coverageBuilder = new CoverageBuilder();
  final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
  analyzer.analyzeClass(getTargetClass(target), targetName);
  //analyzer.analyzeClass(getTargetClass(targetName2), targetName2);
  //analyzer.analyzeAll(classesDirectory);
  
  printCoverage(coverageBuilder);
  
  // Run the structure analyzer on a single class folder to build up
  // the coverage model. The process would be similar if your classes
  // were in a jar file. Typically you would create a bundle for each
  // class folder and each jar you want in your report. If you have
  // more than one bundle you will need to add a grouping node to your
  // report
  //final IBundleCoverage bundleCoverage = analyzeStructure(executionData);
  final IBundleCoverage bundleCoverage = coverageBuilder.getBundle(title);
  createReport(bundleCoverage, executionData, sessionInfos);
 }

 private void createReport(final IBundleCoverage bundleCoverage, ExecutionDataStore executionData, 
   SessionInfoStore sessionInfos) throws IOException {

  // Create a concrete report visitor based on some supplied
  // configuration. In this case we use the defaults
  final HTMLFormatter htmlFormatter = new HTMLFormatter();
  final IReportVisitor visitor = htmlFormatter
    .createVisitor(new FileMultiReportOutput(reportDirectory));

  // Initialize the report with all of the execution and session
  // information. At this point the report doesn't know about the
  // structure of the report being created
  visitor.visitInfo(sessionInfos.getInfos(), executionData.getContents());
  //visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
  //  execFileLoader.getExecutionDataStore().getContents());

  // Populate the report structure with the bundle coverage information.
  // Call visitGroup if you need groups in your report.
  visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(sourceDirectory, "utf-8", 4));
  
  // Signal end of structure information to allow report to write all
  // information out
  visitor.visitEnd();

 }
 
 private void printCoverage(CoverageBuilder coverageBuilder){
  for (final IClassCoverage cc : coverageBuilder.getClasses()) {
   System.out.printf("Coverage of class %s%n", cc.getName());

   printCounter("instructions", cc.getInstructionCounter());
   printCounter("branches", cc.getBranchCounter());
   printCounter("lines", cc.getLineCounter());
   printCounter("methods", cc.getMethodCounter());
   printCounter("complexity", cc.getComplexityCounter());

   for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
    System.out.printf("Line %s: %s%n", Integer.valueOf(i), getColor(cc
      .getLine(i).getStatus()));
   }
  }
 }
/*
 private void loadExecutionData() throws IOException {
  execFileLoader = new ExecFileLoader();
  execFileLoader.load(executionDataFile);
 }

 private IBundleCoverage analyzeStructure(ExecutionDataStore executionData) throws IOException {
  final CoverageBuilder coverageBuilder = new CoverageBuilder();
  final Analyzer analyzer = new Analyzer(
    executionData, coverageBuilder);
    //execFileLoader.getExecutionDataStore(), coverageBuilder);

  //analyzer.analyzeAll(classesDirectory);

  return coverageBuilder.getBundle(title);
 }
*/
 /**
  * Starts the report generation process
  * 
  * @param args
  *            Arguments to the application. This will be the location of the
  *            eclipse projects that will be used to generate reports for
  * @throws Exception 
  */
 public static void main(final String[] args) throws Exception {
  /*
  // Run ant build 
  File buildFile = new File("build.xml");
  Project p = new Project();
  p.setUserProperty("ant.file", buildFile.getAbsolutePath());
  p.init();
  ProjectHelper helper = ProjectHelper.getProjectHelper();
  p.addReference("ant.projectHelper", helper);
  helper.parse(p, buildFile);
  p.executeTarget(p.getDefaultTarget());
  */
  
  //final ReportGenerator generator = new ReportGenerator(new File("C:\\Users\\YTC\\workspace\\comp440\\JacocoTest2"));
  //generator.create();
 }

}
