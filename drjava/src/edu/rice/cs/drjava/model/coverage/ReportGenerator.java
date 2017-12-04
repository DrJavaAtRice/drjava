/* ******************************************************************************
 * Copyright (c) 2009, 2014 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation    
 * ******************************************************************************/
package edu.rice.cs.drjava.model.coverage;

import java.io.File;
import java.io.IOException;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;

import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;

/** This example creates a HTML report for eclipse like projects based on a single execution data store called 
  * jacoco.exec. The report contains no grouping information. The class files under test must be compiled with debug 
  * information, otherwise source highlighting will not work.
  */
public class ReportGenerator {
  
  /* For building the coverage report */
  private  CoverageBuilder coverageBuilder;
  
  /* The output directory in which to place the report */
  private final String reportDirectoryPath;
  
  /** Simple constructor for a ReportGenerator; initializes fields based on input parameters. 
    * @param reportDirectoryPath output directory in which to place the report
    * @param coverageBuilder object which builds the coverage report
    */
  public ReportGenerator(String reportDirectoryPath, CoverageBuilder coverageBuilder) { 
    this.reportDirectoryPath = reportDirectoryPath;
    this.coverageBuilder = coverageBuilder;
  }
  
  /** Given the coverage/execution information for the project rooted in the  input sourceDirectory, generates the 
    * coverage report for that project.  Does not return anything; instead, creates a tree of HTML files rooted
    * in this.reportDirectoryPath containing the coverage results.
    * @param bundleCoverage coverage data
    * @param executionData execution data
    * @param sessionInfos session data
    * @param sourceDirectory the directory containing the project for which
    *                        the report is being gnenerated
    * @throws IOException if an IO operation fails
    */
  public void createReport(final IBundleCoverage bundleCoverage, ExecutionDataStore executionData, 
                           SessionInfoStore sessionInfos, File sourceDirectory) throws IOException {
    
    /* Create a concrete report visitor using the default configuration. */
    final HTMLFormatter htmlFormatter = new HTMLFormatter();
    final IReportVisitor visitor = htmlFormatter.
      createVisitor(new FileMultiReportOutput(new File(this.reportDirectoryPath)));
    
    /* Initialize the report with all of the execution and session information. At this point the report doesn't yet 
     * have any structure.
     */
    visitor.visitInfo(sessionInfos.getInfos(), executionData.getContents());
    
    /* Populate the report structure with the bundle coverage information. Call visitGroup if you need groups in your
     * report.
     */
    visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(sourceDirectory, "utf-8", 4));
    
    /* Signal end of structure information to allow report to write all information out. */
    visitor.visitEnd();
  }
  
  /** Converts the input coverage status into the output color.
    * @param status the status of the line (how well it was covered)
    * @return a color corresponding to the amount of coverage
    */
  private String getColor(final int status) {
    
    switch (status) {
      case ICounter.NOT_COVERED: return "red";
      case ICounter.PARTLY_COVERED: return "yellow";
      case ICounter.FULLY_COVERED: return "green";
      default: return ""; /* this line contains no code, so don't color it */
    }
  }
  
  /** Help function which takes coverage data for a particular class and returns a corresponding list of line colors.
    * @param cc the IClassCoverage object from which to get line colors
    * @return a list of colors, where the i-th element in the list corresponds to the i-th line of code
    */
  private ArrayList<String> getLineColorsForClassHelper(IClassCoverage cc) {
    
    ArrayList<String> lineColors = new ArrayList<String>();
    
    /* Begin by assigning no color to all of the lines before the first actual line of code.  */
    for (int i = 0; i < cc.getFirstLine(); i++) lineColors.add("");               
    
    /* Next, get the color of the lines after the first code line. */
    for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) lineColors.add(getColor(cc.getLine(i).getStatus()));
    
    return lineColors;
  }
  
  /** Given a class name, determines the color of each line in that class according to coverage results.
    * @param className the class (/file) for which to get line colors
    * @return a list of colors, where the i-th element in the list corresponds to the i-th line of code
    */
  public ArrayList<String> getLineColorsForClass(String className) {
    
    /* Initialize it to empty, in case the className is invalid */ 
    ArrayList<String> lineColors = new ArrayList<String>();
    
    /* Search for the requested class */
    for (final IClassCoverage cc : this.coverageBuilder.getClasses()) {
      
      /* Found the class! Get the color of each line. */
      if (cc.getName().equals(className)) {
        lineColors = getLineColorsForClassHelper(cc);
        break;
      }
    }
    
    return lineColors;
  }
  
  /** Given a list of classes, determines the color of each line in each class (based on coverage results).
    * @return a mapping of class names to the line colors for that class
    */
  public Map<String, List<String>> getAllLineColors() {
    
    Map<String, List<String>> allLineColors = new HashMap<String, List<String>>();
    
    for (final IClassCoverage cc : this.coverageBuilder.getClasses()) {
      allLineColors.put(cc.getName(), this.getLineColorsForClassHelper(cc));
    }
    
    return allLineColors;
  }
}
