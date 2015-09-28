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
package edu.rice.cs.drjava.ui.coverage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;
import java.util.*;

import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.util.swing.Utilities;

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

    private final String reportDirectoryPath;
 
    private  CoverageBuilder coverageBuilder;
 
    private String getTargetClassName(final OpenDefinitionsDocument doc) 
        throws Exception {

        File file = doc.getFile();
        String name = file.getName().replace(".java", "");

        if (doc.getPackageName() != "") {
            return doc.getPackageName() + "." + name;
        }

        return name;
    }

    //private String getTargetClassName(final File file) throws Exception {
    //    String ClassName = file.getPath().replace(sourceDirectory.getPath() + 
    //        "/", "").replace(".java", "").replace(".class", "").
    //        replace("/", ".");

    //    return ClassName;
    //}

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

    public ReportGenerator(String reportDirectoryPath, CoverageBuilder coverageBuilder) { 
        this.reportDirectoryPath = reportDirectoryPath;
        this.coverageBuilder = coverageBuilder;
    }

    public List<File> rec_init_src(File sourceDirectory) {

        ArrayList<File> files = new ArrayList<File>();
        for (File f: sourceDirectory.listFiles()) {
            if (f.isFile()) {
                int i = f.getPath().lastIndexOf('.');
                if (i > 0) {
                    String extension = f.getPath().substring(i+1);
                    if (extension.equals("java")) {
                        files.add(f);
                    }
                }
            } else if (f.isDirectory()) {
                files.addAll(rec_init_src(f));
            }
        }

        return files;
    }

    public List<File> rec_init_target(File sourceDirectory){
       ArrayList<File> files = new ArrayList<File>();
       for (File f: sourceDirectory.listFiles()) {                
           if (f.isFile()) {
               int i = f.getPath().lastIndexOf('.');
               if (i > 0) {
                   String extension = f.getPath().substring(i+1);
                   if (extension.equals("class")) {
                       files.add(f);
                   }
               }
           } else if (f.isDirectory()) {
               files.addAll(rec_init_target(f));
           }
       }

        return files;
    }

    public void createReport(final IBundleCoverage bundleCoverage, 
        ExecutionDataStore executionData, 
        SessionInfoStore sessionInfos, File sourceDirectory) throws IOException {

        // Create a concrete report visitor based on some supplied
        // configuration. In this case we use the defaults
        final HTMLFormatter htmlFormatter = new HTMLFormatter();
        final IReportVisitor visitor = htmlFormatter.
            createVisitor(new FileMultiReportOutput(new File(this.reportDirectoryPath)));

        // Initialize the report with all of the execution and session
        // information. At this point the report doesn't know about the
        // structure of the report being created
        visitor.visitInfo(sessionInfos.getInfos(), executionData.getContents());

        // Populate the report structure with the bundle coverage information.
        // Call visitGroup if you need groups in your report.
        visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(
            sourceDirectory, "utf-8", 4));
  
        // Signal end of structure information to allow report to write all
        // information out
        visitor.visitEnd();
    }
 
    public Map<String, List<String>> getLineColors(List<String> classNames) {

        Map<String, List<String>> lineColors = new HashMap<String, List<String>>();
        for (String className : classNames) {
            lineColors.put(className, this.getLineColorsForClass(className));
        }
        return lineColors;

    }

        
    public ArrayList<String> getLineColorsForClass(String className) {
         
        ArrayList<String> lineColors = new ArrayList<String>();
         
        for (final IClassCoverage cc : coverageBuilder.getClasses()) {
            if (!cc.getName().equals(className)) {
                continue;
            }
                   
            for (int i = 0; i < cc.getFirstLine(); i++) {
                lineColors.add("");               
            }

            for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
                lineColors.add(getColor(cc.getLine(i).getStatus()));
            }
        }
         
        return lineColors;
    }
 
 
    private void printCoverage(CoverageBuilder coverageBuilder) {
        for (final IClassCoverage cc : coverageBuilder.getClasses()) {
            System.out.printf("Coverage of class %s%n", cc.getName());

            printCounter("instructions", cc.getInstructionCounter());
            printCounter("branches", cc.getBranchCounter());
            printCounter("lines", cc.getLineCounter());
            printCounter("methods", cc.getMethodCounter());
            printCounter("complexity", cc.getComplexityCounter());

            for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
                System.out.printf("Line %s: %s%n", Integer.valueOf(i), 
                    getColor(cc.getLine(i).getStatus()));
            }
        }
    }
}
