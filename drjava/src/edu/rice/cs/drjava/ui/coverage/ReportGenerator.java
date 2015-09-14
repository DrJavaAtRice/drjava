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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.*;

import edu.rice.cs.plt.collect.CollectUtil;

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

    private final File sourceDirectory;
    private final File reportDirectory;
    private final List<File> targets;
    private final ArrayList<String> targetNames;
    private final String mainClassFileName;
 
    private  CoverageBuilder cb;
 
    /**
     * A class loader that loads classes from in-memory data.
     */
    public static class MemoryClassLoader extends ClassLoader {

        private final Map<String, byte[]> definitions = 
            new HashMap<String, byte[]>();

        /**
         * Add a in-memory representation of a class.
         * 
         * @param name  name of the class
         * @param bytes class definition
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

    /** Public getters and setters */
    public InputStream getTargetClass(final String name) {
        final String resource = '/' + name.replace('.', '/') + ".class";
        return getClass().getResourceAsStream(resource);
    }
 
    public InputStream getTargetClass(final File file) throws Exception {
        return new FileInputStream(file.getCanonicalPath().replace(".java",
            ".class"));
    }

    private String getTargetClassName(final OpenDefinitionsDocument doc) 
        throws Exception {

        File file = doc.getFile();
        String name = file.getName().replace(".java", "");

        if (doc.getPackageName() != "") {
            return doc.getPackageName() + "." + name;
        }

        return name;
    }

    private String getTargetClassName(final File file) throws Exception {
        String ClassName = file.getPath().replace(sourceDirectory.getPath() + 
            "/", "").replace(".java", "").replace(".class", "").
            replace("/", ".");

        return ClassName;
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
     * Create a new generator based for the given file.
     * 
     */
    public ReportGenerator(final GlobalModel _model, 
        final List<OpenDefinitionsDocument> docs, 
        final File destDirectory) throws Exception  {

        File src = docs.get(0).getFile().getParentFile(); 
        String packageName = docs.get(0).getPackageName();
        if (!packageName.equals("")) {
            // If there's package, go up until the root dir  
            int numUp = packageName.split("\\.").length;
            for (int i = 0; i< numUp ; i++) {
                src = src.getParentFile();
            }
        }
        this.sourceDirectory = src;
        this.title = src.getName();

        List<File> targets = new ArrayList<File>();
        for (File dir:CollectUtil.makeList(_model.getClassPath())) {
            if (!dir.isDirectory() || dir.getName().equals("lib") || 
                dir.getName().equals("base") || dir.getName().equals("test")) {
                continue;
            }
            targets.addAll(rec_init_target(dir));
        }
        this.targets = targets;

        ArrayList<String> targetNames = new ArrayList<String>();
        for (File f: targets) {
            targetNames.add(getTargetClassName(f));
        }

        this.targetNames = targetNames;
        this.reportDirectory = destDirectory;
        this.mainClassFileName = getTargetClassName(docs.get(0).getFile());
    }

    /**
     * Create a new generator based for the given project.
     */
    public ReportGenerator(final GlobalModel _model, 
        File sourceDirectory, String mainClassPath, File destDirectory) 
        throws Exception  {

        this.sourceDirectory = sourceDirectory;
        this.reportDirectory = destDirectory; 
        this.title = sourceDirectory.getName();  
        this.mainClassFileName = mainClassPath;

        this.targets = rec_init_src(sourceDirectory);
        ArrayList<String> targetNames = new ArrayList<String>();
        for (File f: targets) {
            targetNames.add(getTargetClassName(f));
        }

        this.targetNames = targetNames;
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

    /**
     * Create the report.
     * @throws Exception 
     */
    public void create(/* List<String> classNames, List<File> files */) throws Exception {
 
        // For instrumentation and runtime we need a IRuntime instance
        // to collect execution data:
        final IRuntime runtime = new LoggerRuntime();

        // The Instrumenter creates a modified version of our test target class
        // that contains additional probes for execution data recording:
        ArrayList<byte[]> instrumenteds = new ArrayList<byte[]>();
        for (int i = 0 ; i< targets.size() ; i++) {
            final Instrumenter instr = new Instrumenter(runtime);
            final byte[] instrumented = instr.instrument(getTargetClass(
                targets.get(i)), targetNames.get(i));
            instrumenteds.add(instrumented);
        }

        // Now we're ready to run our instrumented class and need to startup the
        // runtime first:
        final RuntimeData data = new RuntimeData();
        runtime.startup(data);

        // In this tutorial we use a special class loader to directly load the
        // instrumented class definition from a byte[] instances.
        final MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
        for (int i = 0; i< targetNames.size(); i++) {
            memoryClassLoader.addDefinition(targetNames.get(i), 
                instrumenteds.get(i));
        }

        final Class<?> mainClass = memoryClassLoader.loadClass(mainClassFileName);

        // Execute the test target class 
        Method meth = mainClass.getMethod("main", String[].class);
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

        for (int i = 0; i< targetNames.size(); i++) {
            analyzer.analyzeClass(getTargetClass(targets.get(i)), 
                targetNames.get(i));
        }
  
        this.cb = coverageBuilder;
        printCoverage(coverageBuilder);

        // Run the structure analyzer on a single class folder to build up
        // the coverage model. The process would be similar if your classes
        // were in a jar file. Typically you would create a bundle for each
        // class folder and each jar you want in your report. If you have
        // more than one bundle you will need to add a grouping node to your
        // report
        final IBundleCoverage bundleCoverage = coverageBuilder.getBundle(title);
        createReport(bundleCoverage, executionData, sessionInfos);
    }

    public void createReport(final IBundleCoverage bundleCoverage, 
        ExecutionDataStore executionData, 
        SessionInfoStore sessionInfos) throws IOException {

        // Create a concrete report visitor based on some supplied
        // configuration. In this case we use the defaults
        final HTMLFormatter htmlFormatter = new HTMLFormatter();
        final IReportVisitor visitor = htmlFormatter.
            createVisitor(new FileMultiReportOutput(reportDirectory));

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
 
    /** Public getters and setters */
    public File getSourceDirectory() {
        return this.sourceDirectory;
    }

    public ArrayList<String> getLineColorsForClass(String className) {
         
        ArrayList<String> lineColors = new ArrayList<String>();
         
        for (final IClassCoverage cc : cb.getClasses()) {
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
