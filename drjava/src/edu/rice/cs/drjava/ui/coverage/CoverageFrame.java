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

package edu.rice.cs.drjava.ui.coverage;

import java.awt.event.*;
import java.awt.*;
import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;

import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.compiler.CompilerListener;
import edu.rice.cs.drjava.model.compiler.DummyCompilerListener;
import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;

import edu.rice.cs.drjava.config.Option;
import edu.rice.cs.drjava.config.OptionParser;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.ui.config.*;
import edu.rice.cs.drjava.ui.*;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.AbsRelFile;
import edu.rice.cs.util.swing.FileSelectorComponent;
import edu.rice.cs.util.swing.DirectorySelectorComponent;
import edu.rice.cs.util.swing.DirectoryChooser;
import edu.rice.cs.util.swing.FileChooser;
import edu.rice.cs.util.swing.HighlightManager.HighlightInfo;
import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.Utilities;

import edu.rice.cs.drjava.model.junit.JUnitResultTuple;

import javax.swing.filechooser.FileFilter;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;;

/** 
 * A frame with options for generating a code coverage report.
 */
public class CoverageFrame extends SwingFrame {

    private static final int FRAME_WIDTH = 503;
    private static final int FRAME_HEIGHT = 270;

    private final MainFrame _mainFrame;      
    private final SingleDisplayModel _model; 

    private final JButton _okButton;
    private final JButton _cancelButton;
    private final JCheckBox _openHTMLBrowser;
    //private final JCheckBox _useCurrentFile;

    private final JPanel _mainPanel;

    private volatile DirectorySelectorComponent _srcRootSelector;
    private volatile DirectorySelectorComponent _outputDirSelector;
    private volatile JTextField _mainDocumentSelector;
    private volatile JButton selectFile;

    private final Map<OptionParser<?>,String> _storedPreferences = 
        new HashMap<OptionParser<?>,String>();
  
    /** 
     * Constructs project properties frame for a new project and displays it.  
     * Assumes that a project is active. 
     *
     * @param mf the main display frame
     */
    public CoverageFrame(MainFrame mf) {
        super("Code Coverage");

        _mainFrame = mf;
        _model = _mainFrame.getModel();
        _mainPanel= new JPanel();

        /* Add options */
        //_useCurrentFile = new JCheckBox(
        //    "Generete report for current selected file", true);
        //_useCurrentFile.addActionListener(new ActionListener(){
        //    @Override
        //    public void actionPerformed(ActionEvent e) {
        //        _srcRootSelector.setEnabled(!_useCurrentFile.isSelected());
        //        _mainDocumentSelector.setEnabled(!_useCurrentFile.isSelected());
        //        selectFile.setEnabled(!_useCurrentFile.isSelected()); 
        //    }
        //});

        _openHTMLBrowser = new JCheckBox(
            "Open web browser to display the report", true);
 
        /* Connect the main buttons to their actions */
        Action okAction = new AbstractAction("Ok") {
            public void actionPerformed(ActionEvent e) {
                startJUnit();
            }
        };
        _okButton = new JButton(okAction);

        Action cancelAction = new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) { 
                cancel(); 
            }
        };
        _cancelButton = new JButton(cancelAction);

        init();
        initDone(); /* call mandated by SwingFrame contract */
    }

    /**
     * Displays the report in the browser, if requested by the user. 
     * @param result data from which to generate report
     */
    public void displayReport(JUnitResultTuple result) {

        /* Only display the result if testing was successful */
        //if (result.getRetval()) {
            if (_openHTMLBrowser.isSelected()) {
                String indexURL = _outputDirSelector.getFileFromField().getPath()
                     + "/index.html";
                this.displayReportUsingDefaultBrowser(indexURL);
            }

            //this.highlight(_mainFrame.getLastJUnitResult().getLineColors(), false);
            this._model.getJUnitModel().setCoverage(false, "");
        //} 
        //else {
        //    /* If testing failed, display an error message. */
        //}
    }

    /** 
     * Caches the settings in the global model.
     */
    public void saveSettings() { 
        File pr = _srcRootSelector.getFileFromField();
        if (_srcRootSelector.getFileField().getText().equals("")) {
            pr = FileOps.NULL_FILE;
        }

        File wd = _outputDirSelector.getFileFromField();
        if (_outputDirSelector.getFileField().getText().equals("")) {
            wd = FileOps.NULL_FILE;
        }

        String mc = _mainDocumentSelector.getText();
        if (mc == null) {
            mc = "";
        }
    }

    /** 
      * Validates before changing visibility. Only runs in the event thread.
      *
      * @param vis true if frame should be shown, false if it should be hidden.
      */
    public void setVisible(boolean vis) {

        /* Make sure we're in the event thread. */
        assert EventQueue.isDispatchThread();
        validate();

        if (vis) {
            _mainFrame.hourglassOn();
            _mainFrame.installModalWindowAdapter(this, LambdaUtil.NO_OP, CANCEL);
            toFront();
        } else {
            _mainFrame.removeModalWindowAdapter(this);
            _mainFrame.hourglassOff();
            _mainFrame.toFront();
        }

        super.setVisible(vis);
    }

    /**
     * Sets the output directory in which to generate the HTML JaCoCo report.
     *
     * @param file the output directory
     */
    public void setOutputDir(File file) {
         _outputDirSelector.setFileField(file);
    }

    /** 
     * Initializes the components in this frame. 
     */
    private void init() {

        _setupPanel(_mainPanel);
        JScrollPane scrollPane = new JScrollPane(_mainPanel);
        Container cp = getContentPane();
    
        GridBagLayout cpLayout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        cp.setLayout(cpLayout);
    
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = GridBagConstraints.RELATIVE;
        c.weightx = 1.0;
        c.weighty = 1.0;
        cpLayout.setConstraints(scrollPane, c);
        cp.add(scrollPane);

        /* Add buttons */
        JPanel bottom = new JPanel();
        bottom.setBorder(new EmptyBorder(5,5,5,5));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.add(Box.createHorizontalGlue());
        bottom.add(_okButton);
        bottom.add(_cancelButton);
        bottom.add(Box.createHorizontalGlue());

        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.SOUTH;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.weighty = 0.0;
        cpLayout.setConstraints(bottom, c);
        cp.add(bottom);

        /* Set all dimensions */
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        if (dim.width>FRAME_WIDTH) { 
            dim.width = FRAME_WIDTH; 
        } else { 
            dim.width -= 80; 
        }

        if (dim.height>FRAME_HEIGHT) { 
             dim.height = FRAME_HEIGHT;
        } else { 
             dim.height -= 80; 
        }

        setSize(dim);
        Utilities.setPopupLoc(this, _mainFrame);
    }

    /** 
     * Resets the frame and hides it. 
     */
    private void cancel() {
        CoverageFrame.this.setVisible(false);
    }

    /**
     * Begins JUnit testing with code coverage. 
     */
    private void startJUnit(){
         _model.getJUnitModel().setCoverage(true, 
             this._outputDirSelector.getFileFromField().getPath());
         _mainFrame._junitAll(); 
         CoverageFrame.this.setVisible(false);
    }

    /**
     * Highlights each line of code in the open files (green, yellow, or red,
     * based on the results of running JaCoCo.
     * 
     * @param lineColors the colors to highlight
     * @param selOnly true if we only want to highlight the currently-selected
     *                document; false to highlight everything
     */
    private void highlight(Map<String, List<String>> lineColors, boolean selOnly) {
    
        /* Get an iterator over the documents to be highlighted */
        Iterator<OpenDefinitionsDocument> iter;
        if (!selOnly) {
            iter = _model.getDocumentNavigator().getDocuments().
                iterator();
        } else {
            iter = _model.getDocumentNavigator().getSelectedDocuments().
                iterator();
        }

        while (iter.hasNext()) {

            /* Get the file to highlight */
            OpenDefinitionsDocument o = iter.next(); 
            final DefinitionsPane pane = _mainFrame.getDefPaneGivenODD(o);

            try {
                List<String> colors = lineColors.get(o.getQualifiedClassName());

                /* Highlight each line */
                for (int i = 0; i < colors.size(); i++) {
                    String color = colors.get(i);
              
                    Color c = Color.black;
                    if (color.equals("")) {
                        continue;
                    } else if (color.equals("green")) {
                        c = Color.green;
                    } else if (color.equals("red")) {
                        c = Color.red;
                    } else if (color.equals("yellow")) {
                        c = Color.yellow;
                    }
      
                    final HighlightInfo info = pane.getHighlightManager().
                        addHighlight(o._getOffset(i), o._getOffset(i+1), 
                        new ReverseHighlighter.DrJavaHighlightPainter(c));

                    CompilerListener removeHighlight = new DummyCompilerListener() {

                        @Override public void compileAborted(Exception e) {
                            /**
                             * Gets called if there are modified files and the 
                             * user chooses NOT to save the files see bug 
                             * report 2582488: Hangs If Testing Modified File, 
                             * But Choose "No" for Saving
                             */
                            final CompilerListener listenerThis = this;
                            _model.getCompilerModel().removeListener(listenerThis); 
                        }

                        @Override public void compileEnded(File workDir, 
                            List<? extends File> excludedFiles) {

                            final CompilerListener listenerThis = this;

                            try {

                                if (_model.hasOutOfSyncDocuments() || _model.
                                    getNumCompilerErrors() > 0) {
                                    return;
                                }

                                EventQueue.invokeLater(new Runnable() {  
                                    /**
                                     * Defer running this code; would prefer
                                     * to waitForInterpreter.
                                     */
                                    public void run() {
                                        pane.getHighlightManager().
                                        removeHighlight(info);
                                    }
                                });
                            }

                            finally {

                                /* Remove listener after its first execution */
                                EventQueue.invokeLater(new Runnable() { 
                                    public void run() { 
                                        _model.getCompilerModel().
                                        removeListener(listenerThis); 
                                    }
                                });
                            }
                        }
                    }; /* end coverage listener */

                    _model.getCompilerModel().addListener(removeHighlight);
                }

            } catch (ClassNameNotFoundException e) {
                continue;
            }
        }
    }

    /** 
     * @return the default null dir. 
     */
    private File _getDefaultNullDir() {
        return FileOps.NULL_FILE;
    }

    /**
     * Initializes the main panel.
     *
     * @param panel the panel to set up
     */
    private void _setupPanel(JPanel panel) {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(gridbag);
        c.fill = GridBagConstraints.HORIZONTAL;
        Insets labelInsets = new Insets(5, 10, 0, 0);
        Insets compInsets  = new Insets(5, 5, 0, 10);

        // CheckBox for using current selected files
        //c.weightx = 0.0;
        //c.gridwidth = GridBagConstraints.REMAINDER;
        //c.insets = compInsets;
        //gridbag.setConstraints(_useCurrentFile, c);
        //panel.add(_useCurrentFile);

        // CheckBox for opening HTML report in web browser
        c.weightx = 0.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = compInsets;
        gridbag.setConstraints(_openHTMLBrowser, c);
        panel.add(_openHTMLBrowser);

        // Project Root
        /*c.weightx = 0.0;
        c.gridwidth = 1;
        c.insets = labelInsets;

        JLabel prLabel = new JLabel("Src Root");
        prLabel.setToolTipText(
            "<html>The root directory for the project source files.</html>");
        gridbag.setConstraints(prLabel, c);

        panel.add(prLabel);
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = compInsets;

        this._srcRootSelector = this._createDirectoryPanel("Select Src Root Panel");
        gridbag.setConstraints(this._srcRootSelector, c);
        panel.add(this._srcRootSelector);*/

        // Main Document file
        /*c.weightx = 0.0;
        c.gridwidth = 1;
        c.insets = labelInsets;

        JLabel classLabel = new JLabel("Main Class");
        classLabel.setToolTipText(
            "<html>The class containing the <code>main</code><br>" 
            + "method for the entire project</html>");
        gridbag.setConstraints(classLabel, c);
        panel.add(classLabel);

        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = compInsets;

        JPanel mainClassPanel = _mainDocumentSelector();
        gridbag.setConstraints(mainClassPanel, c);
        panel.add(mainClassPanel);

        c.weightx = 0.0;
        c.gridwidth = 1;
        c.insets = labelInsets;*/

        // Output Directory
        JLabel wdLabel = new JLabel("Output Directory");
        wdLabel.setToolTipText(
            "<html>The output directory for reports.</html>");
        gridbag.setConstraints(wdLabel, c);

        panel.add(wdLabel);
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = compInsets;

        this._outputDirSelector = this._createDirectoryPanel("Select Output Directory");
        gridbag.setConstraints(this._outputDirSelector, c);
        panel.add(this._outputDirSelector);
    }

    /**
     * Creates and returns a panel from which to select a file.
     * @param title the title of the panel
     * @return the panel
     */
    private DirectorySelectorComponent _createDirectoryPanel(String title) {

        DirectoryChooser dirChooser = new DirectoryChooser(this);
        dirChooser.setSelectedFile(_getDefaultNullDir());
        dirChooser.setDialogTitle(title);
        dirChooser.setApproveButtonText("Select");
        //dirChooser.setEditable(true);

        DirectorySelectorComponent panel = new DirectorySelectorComponent(
            this, dirChooser, 20, 12f) {
            protected void _chooseFile() {
                _mainFrame.removeModalWindowAdapter(CoverageFrame.this);
                super._chooseFile();
                _mainFrame.installModalWindowAdapter(CoverageFrame.this, 
                    LambdaUtil.NO_OP, CANCEL);
            }
        };

        return panel;
    }

    /**
     * Creates and returns a pane from which to select the main document
     * to generate the report from.
     *
     * @return the panel
     */
    private JPanel _mainDocumentSelector() {

        final File srcRoot = _getDefaultNullDir();
        final FileChooser chooser = new FileChooser(srcRoot);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
        chooser.setDialogTitle("Select Main Class");
        chooser.setCurrentDirectory(srcRoot);
        chooser.setApproveButtonText("Select");

        chooser.resetChoosableFileFilters();
        chooser.addChoosableFileFilter(new SmartSourceFilter());
        chooser.addChoosableFileFilter(new JavaSourceFilter());
        _mainDocumentSelector = new JTextField(20) {
            public Dimension getMaximumSize() {
                return new Dimension(Short.MAX_VALUE, 
                    super.getPreferredSize().height);
            }
        };

        _mainDocumentSelector.setFont(_mainDocumentSelector.getFont().deriveFont(12f));
        _mainDocumentSelector.setPreferredSize(new Dimension(22, 22));

        selectFile = new JButton("...");
        selectFile.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                int ret = chooser.showOpenDialog(CoverageFrame.this);
                if (ret != JFileChooser.APPROVE_OPTION) {
                    return;
                }
        
                /* Validate the selected file */
                File mainClass = chooser.getSelectedFile();
                File sourceRoot = new File(_srcRootSelector.getFileField().getText());
        
                if (sourceRoot == null || mainClass == null) {
                    return;
                }

                if (!mainClass.getAbsolutePath().startsWith(sourceRoot.
                    getAbsolutePath())) {
                    JOptionPane.showMessageDialog(CoverageFrame.this,
                        "Main Class must be in either Project Root or one of its sub-directories.", 
                        "Unable to set Main Class", JOptionPane.ERROR_MESSAGE);
          
                    _mainDocumentSelector.setText("");
                    return;
                }

                /* Strip off the source root path */
                String qualifiedName = mainClass.getAbsolutePath().substring(
                    sourceRoot.getAbsolutePath().length());
        
                /* Strip off any leading slashes */
                if (qualifiedName.startsWith("" + File.separatorChar)) {
                    qualifiedName = qualifiedName.substring(1);
                }
        
                /**
                 * Remove the .java extension if it exists
                 * TODO: What about language level file extensions? 
                 *       What about Habanero Java extension?
                 */
                if (qualifiedName.toLowerCase().endsWith(OptionConstants.
                    JAVA_FILE_EXTENSION)) {
                    qualifiedName = qualifiedName.substring(0, 
                        qualifiedName.length() - 5);
                }

                /** 
                 * Replace path seperators with java standard '.' package 
                 * seperators.
                 */
                _mainDocumentSelector.setText(qualifiedName.replace(File.
                    separatorChar, '.'));
                }
           });

        selectFile.setMaximumSize(new Dimension(22, 22));
        selectFile.setMargin(new Insets(0, 5 ,0, 5));
    
        JPanel toRet = new JPanel();
        javax.swing.BoxLayout layout = new javax.swing.BoxLayout(toRet, 
            javax.swing.BoxLayout.X_AXIS);
        toRet.setLayout(layout);
        toRet.add(_mainDocumentSelector);
        toRet.add(selectFile);
    
        return toRet;
    }

    /** Runnable that calls _cancel. */
    protected final Runnable1<WindowEvent> CANCEL = new Runnable1<WindowEvent>() {
        public void run(WindowEvent e) { cancel(); }
    };

    /**
     * Opens the default browser to display the report, stored at the given url.
     *
     * @param url location of the report
     */
    private void displayReportUsingDefaultBrowser(String url) {

        if (Desktop.isDesktopSupported()) {

            /* Build the URI corresponding to the url and open the browser */
            Desktop desktop = Desktop.getDesktop();
            try {
                URI uri = new File(url).toURI();
                desktop.browse(uri);
            } catch (IOException e0) { 
                displayErrorMessage(e0);
            }

        } else {

            /* xdg-open the file directly */
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + url);
            } catch (IOException e2) {
                displayErrorMessage(e2);
            }
        }
    }

    /**
     * Displays the stack trace of the given exception.
     *
     * @param e the exception to display.
     */
    private void displayErrorMessage(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String s = sw.toString(); // stack trace as a string
        JOptionPane.showMessageDialog(_mainFrame, s, "error: ", JOptionPane.ERROR_MESSAGE);
    }
}
