package edu.rice.cs.drjava.model.compiler;

import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.drjava.ui.SmartSourceFilter;
import edu.rice.cs.plt.reflect.JavaVersion;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.tools.*;

public class JavaxToolsCompiler implements CompilerInterface {

    private final JavaCompiler compiler;

    public JavaxToolsCompiler() {
        this.compiler = ToolProvider.getSystemJavaCompiler();
    }

    public boolean isAvailable() {
        return this.compiler != null;
    }

    public List<? extends DJError> compile(List<? extends File> files, List<? extends File> classPath,
                                           List<? extends File> sourcePath, File destination,
                                           List<? extends File> bootClassPath, String sourceVersion, boolean showWarnings) {
        // Check if compiler is available
        if (compiler == null) {
            List<DJError> errors = new ArrayList<>();
            errors.add(new DJError("Compiler is not available", false));
            return errors;
        }

        // Set up the file manager
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        // Set the classpath, source path, and bootclasspath
        try {
            if (classPath != null) {
                fileManager.setLocation(StandardLocation.CLASS_PATH, classPath);
            }
            if (sourcePath != null) {
                fileManager.setLocation(StandardLocation.SOURCE_PATH, sourcePath);
            }
            if (bootClassPath != null) {
                fileManager.setLocation(StandardLocation.PLATFORM_CLASS_PATH, bootClassPath);
            }
        } catch (IOException e) {
            List<DJError> errors = new ArrayList<>();
            errors.add(new DJError("Error setting paths: " + e.getMessage(), false));
            return errors;
        }

        // Convert files to a format the compiler understands
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);

        // Prepare the compilation options
        List<String> optionList = new ArrayList<>();
        if (sourceVersion != null) {
            optionList.add("-source");
            optionList.add(sourceVersion);
        }
        if (destination != null) {
            optionList.add("-d");
            optionList.add(destination.getAbsolutePath());
        }
        if (showWarnings) {
            optionList.add("-Xlint");
        } else {
            optionList.add("-Xlint:none");
        }

        // Prepare a diagnostic collector to collect compile errors
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        // Create a compilation task
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, optionList, null, compilationUnits);

        // Perform the compile task
        boolean success = task.call();

        // Process diagnostics to create DJError list
        List<DJError> errors = new ArrayList<>();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            DJError error = new DJError(new File(diagnostic.getSource().toUri()),
                    (int) diagnostic.getLineNumber(),
                    (int) diagnostic.getColumnNumber(),
                    diagnostic.getMessage(null),
                    diagnostic.getKind() == Diagnostic.Kind.ERROR);
            errors.add(error);
        }

        // If compilation failed and no errors were reported, add a generic error message
        if (!success && errors.isEmpty()) {
            errors.add(new DJError("Compilation failed with unknown error", true));
        }

        return errors;
    }

    public JavaVersion version() {
        return JavaVersion.JAVA_8;
    }

    public String getName() {
        return "javax.tools";
    }

    public String getDescription() {
        return "Custom compiler implementation using javax.tools";
    }

    public String toString() {
        return getName();
    }

    public List<File> additionalBootClassPathForInteractions() {
        // TODO: figure out what this looks like for javax.tools compiler
        return new ArrayList<File>();
    }

    public String transformCommands(String interactionsString) {
        // TODO: Implement command transformation logic (this is for interpreter)
        return interactionsString;
    }

    public boolean isSourceFileForThisCompiler(File f) {
        if (f == null) return false;
        String fileName = f.getName();
        return fileName.endsWith(".java");
    }

    public Set<String> getSourceFileExtensions() {
        HashSet<String> extensions = new HashSet<String>();
        extensions.add("java");
        return extensions;
    }

    public String getSuggestedFileExtension() {
        return ".java";
    }

    public FileFilter getFileFilter() {
        // TODO: this might need to be different... (I think smartsourcefilter includes .dj files which idk ab)
        return new SmartSourceFilter();
    }

    public String getOpenAllFilesInFolderExtension() {
        // Should we use OptionConstants for this?
        return ".java";
    }

    /** Return the set of keywords that should be highlighted in the specified file.
     * @param f file for which to return the keywords
     * @return the set of keywords that should be highlighted in the specified file. */
    public Set<String> getKeywordsForFile(File f) { return new HashSet<>(JAVA_KEYWORDS); }

    /** Set of Java/GJ keywords for special coloring. */
    public static final HashSet<String> JAVA_KEYWORDS = new HashSet<>();
    static {
        final String[] words =  {
                "import", "native", "package", "goto", "const", "if", "else", "switch", "while", "for", "do", "true", "false",
                "null", "this", "super", "new", "instanceof", "return", "static", "synchronized", "transient", "volatile",
                "final", "strictfp", "throw", "try", "catch", "finally", "throws", "extends", "implements", "interface", "class",
                "break", "continue", "public", "protected", "private", "abstract", "case", "default", "assert", "enum"
        };
        Collections.addAll(JAVA_KEYWORDS, words);
    }

    public boolean supportsLanguageLevels() {
        // TODO: should we support LanguageLevels?
        return false;
    }
}