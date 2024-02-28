package edu.rice.cs.drjava.model.compiler;

import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.drjava.ui.SmartSourceFilter;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.util.ArgumentTokenizer;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import javax.tools.*;



public class JavaxToolsCompiler implements CompilerInterface {
    /** The set of class names that are run as ACM Java Task Force library programs. */
    protected static final Set<String> ACM_PROGRAM_CLASSES = new HashSet<String>();
    static {
        Collections.addAll(ACM_PROGRAM_CLASSES, new String[] {
                "acm.program.Program",
                "acm.graphics.GTurtle"
        });
    }
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
        // TODO: enforce using java8
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
            try {
                fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(destination));
            } catch (IOException e) {
                List<DJError> errors = new ArrayList<>();
                errors.add(new DJError("Error setting build directory: " + e.getMessage(), false));
                return errors;
            }
            // This doesn't work for javax.tools compiler
            // optionList.add("-d");
            // optionList.add(destination.getAbsolutePath());
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
                    (int) diagnostic.getLineNumber() - 1, // DJError adds 1 to this number.
                    (int) diagnostic.getColumnNumber() - 1, // Fixes the cursor position offset.
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
        return Collections.emptyList();
    }

    /** Transform the command line to be interpreted into something the Interactions JVM can use.
     * This replaces "java MyClass a b c" with Java code to call MyClass.main(new String[]{"a","b","c"}).
     * "import MyClass" is not handled here.
     * transformCommands should support at least "run", "java" and "applet".
     * @param interactionsString unprocessed command line
     * @return command line with commands transformed */
    public String transformCommands(String interactionsString) {
        if (interactionsString.startsWith("java ")) {
            interactionsString = transformJavaCommand(interactionsString);
        }
        else if (interactionsString.startsWith("applet ")) {
            interactionsString = transformAppletCommand(interactionsString);
        }
        else if (interactionsString.startsWith("run ")) {
            interactionsString = transformRunCommand(interactionsString);
        }
        return interactionsString;
    }

    public static String transformJavaCommand(String s) {
        // check the return type and public access before executing, per bug #1585210
        String command =
                "try '{'\n" +
                        "  java.lang.reflect.Method m = {0}.class.getMethod(\"main\", java.lang.String[].class);\n" +
                        "  if (!m.getReturnType().equals(void.class)) throw new java.lang.NoSuchMethodException();\n" +
                        "'}'\n" +
                        "catch (java.lang.NoSuchMethodException e) '{'\n" +
                        "  throw new java.lang.NoSuchMethodError(\"main\");\n" +
                        "'}'\n" +
                        "{0}.main(new String[]'{'{1}'}');";
        return _transformCommand(s, command);
    }

    public static String transformAppletCommand(String s) {
        return _transformCommand(s,"edu.rice.cs.plt.swing.SwingUtil.showApplet(new {0}({1}), 400, 300);");
    }

    /** This method performs the "smart run". Unfortunately, we don't get the right static error messages.
     * @param s full command line, i.e. "run MyClass 1 2 3"
     * @param c class to be run, i.e. MyClass.class
     * @throws Throwable if something goes wrong
     */
    public static void runCommand(String s, Class<?> c) throws Throwable {
        if (s.endsWith(";"))  s = _deleteSemiColon(s);
        List<String> tokens = ArgumentTokenizer.tokenize(s, true);
        String[] args = new String[tokens.size() - 2];
        for (int i = 2; i < tokens.size(); i++) {
            String t = tokens.get(i);
            args[i - 2] = t.substring(1, t.length() - 1);
        }

        boolean isProgram = false;
        boolean isApplet = false;
        Class<?> oldC = c;
        while(c != null) {
            if (ACM_PROGRAM_CLASSES.contains(c.getName())) { isProgram = true; break; }
            c = c.getSuperclass();
        }
        c = oldC;
        if (!isProgram) {
            try {
                // if this doesn't throw, c is a subclass of Applet
                c.asSubclass(java.applet.Applet.class);
                isApplet = true;
            } catch(ClassCastException cce) { }
        }

        java.lang.reflect.Method m = null;
        if (isApplet) {
            try {
                m = c.getMethod("main", java.lang.String[].class);
                if (!m.getReturnType().equals(void.class)) { m = null; }
            }
            catch (java.lang.NoSuchMethodException e) { m = null; }
            if (m == null) {
                java.applet.Applet instance = null;
                if (args.length == 0) {
                    try {
                        // try default (nullary) constructor first
                        Constructor<?> ctor = c.getConstructor();
                        instance = java.applet.Applet.class.cast(ctor.newInstance());
                    }
                    catch(NoSuchMethodException | InstantiationException | IllegalAccessException nsme) {
                        instance = null;
                    }
                    catch(java.lang.reflect.InvocationTargetException ite) {
                        if (ite.getCause()!=null) {
                            throw ite.getCause();
                        }
                        else {
                            System.err.println("Error: Please turn off 'Smart Run' or use 'java' command instead of 'run'.");
                        }
                    }
                    if (instance == null) {
                        try {
                            // try String[] constructor next
                            Constructor<?> ctor = c.getConstructor(String[].class);
                            instance = java.applet.Applet.class.cast(ctor.newInstance(new Object[] { new String[0] }));
                        }
                        catch(NoSuchMethodException | InstantiationException | IllegalAccessException nsme) {
                            instance = null;
                        }
                        catch(java.lang.reflect.InvocationTargetException ite) {
                            if (ite.getCause()!=null) {
                                throw ite.getCause();
                            }
                            else {
                                System.err.println("Error: Please turn off 'Smart Run' or use 'java' command instead of 'run'.");
                                return;
                            }
                        }
                    }
                    if (instance == null) {
                        System.err.println("Static Error: This applet does not have a default constructor or a constructor "+
                                "accepting String[].");
                        return;
                    }
                }
                else {
                    try {
                        // try String[] constructor
                        Constructor<?> ctor = c.getConstructor(String[].class);
                        instance = java.applet.Applet.class.cast(ctor.newInstance(new Object[] { args }));
                    }
                    catch(NoSuchMethodException | InstantiationException | IllegalAccessException nsme) {
                        instance = null;
                    }
                    catch(java.lang.reflect.InvocationTargetException ite) {
                        if (ite.getCause()!=null) {
                            throw ite.getCause();
                        }
                        else {
                            System.err.println("Error: Please turn off 'Smart Run' or use 'java' command instead of 'run'.");
                            return;
                        }
                    }
                    if (instance == null) {
                        System.err.println("Static Error: This applet does not have a constructor accepting String[].");
                        return;
                    }
                }
                edu.rice.cs.plt.swing.SwingUtil.showApplet(instance, 400, 300);
            }
        }
        else {
            try {
                m = c.getMethod("main", java.lang.String[].class);
                if (!m.getReturnType().equals(void.class)) {
                    System.err.println("Static Error: This class does not have a static void main method accepting String[].");
                    m = null;
                }
            }
            catch (java.lang.NoSuchMethodException e) {
                System.err.println("Static Error: This class does not have a static void main method accepting String[].");
                m = null;
            }
        }
        if (m != null) {
            if (isProgram) {
                String[] newArgs = new String[args.length+1];
                newArgs[0] = "code="+c.getName();
                System.arraycopy(args, 0, newArgs, 1, args.length);
                args = newArgs;
            }
            try {
                m.setAccessible(true);
                m.invoke(null, new Object[] { args });
            }
            catch(SecurityException | IllegalAccessException se) {
                System.err.println("Error: Please turn off 'Smart Run' or use 'java' command instead of 'run'.");
            }
            catch(java.lang.reflect.InvocationTargetException ite) {
                if (ite.getCause()!=null) {
                    throw ite.getCause();
                }
                else {
                    System.err.println("Error: Please turn off 'Smart Run' or use 'java' command instead of 'run'.");
                }
            }
        }
    }

    /** This is a method that automatically detects if
     * a) the class is an ACM Java Task Force program (subclass of acm.program.Program)
     * b) an applet
     * c) a class with a static main method
     *
     * If a), then DrJava inserts "code=MyClass" as argument 0.
     * If b), then DrJava performs the same as "applet MyClass" (see above).
     * If c), then DrJava executes MyClass.main (traditional java behavior).
     *
     * @param s the command to be transformed
     * @return the transformed command
     */
    public String transformRunCommand(String s) {
        if (s.endsWith(";"))  s = _deleteSemiColon(s);
        List<String> args = ArgumentTokenizer.tokenize(s, true);
        final String classNameWithQuotes = args.get(1); // this is "MyClass"
        final String className =
                classNameWithQuotes.substring(1, classNameWithQuotes.length() - 1); // removes quotes, becomes MyClass

        // we pass MyClass.class just to get a "Static Error: Undefined class 'MyClass'"
        String ret = JavacCompiler.class.getName()+".runCommand(\""+s.toString()+"\", "+className+".class)";
        // System.out.println(ret);
        return ret;
    }

    /** Assumes a trimmed String. Returns a string of the call that the interpreter can use.
     * The arguments get formatted as comma-separated list of strings enclosed in quotes.
     * Example: _transformCommand("java MyClass arg1 arg2 arg3", "{0}.main(new String[]'{'{1}'}');")
     * returns "MyClass.main(new String[]{\"arg1\",\"arg2\",\"arg3\"});"
     * NOTE: the command to run is constructed using {@link java.text.MessageFormat}. That means that certain characters,
     * single quotes and curly braces, for example, are special. To write single quotes, you need to double them.
     * To write curly braces, you need to enclose them in single quotes. Example:
     * MessageFormat.format("Abc {0} ''foo'' '{'something'}'", "def") returns "Abc def 'foo' {something}".
     * @param s the command line, either "java MyApp arg1 arg2 arg3" or "applet MyApplet arg1 arg2 arg3"
     * @param command the command to execute, with {0} marking the place for the class name and {1} the place for the arguments
     * @return the transformed command
     */
    protected static String _transformCommand(String s, String command) {
        if (s.endsWith(";"))  s = _deleteSemiColon(s);
        List<String> args = ArgumentTokenizer.tokenize(s, true);
        final String classNameWithQuotes = args.get(1); // this is "MyClass"
        final String className = classNameWithQuotes.substring(1, classNameWithQuotes.length() - 1); // removes quotes, becomes MyClass
        final StringBuilder argsString = new StringBuilder();
        boolean seenArg = false;
        for (int i = 2; i < args.size(); i++) {
            if (seenArg) argsString.append(",");
            else seenArg = true;
            argsString.append(args.get(i));
        }
        return java.text.MessageFormat.format(command, className, argsString.toString());
    }

    /** Deletes the last character of a string.  Assumes semicolon at the end, but does not check.  Helper
     * for _transformCommand(String,String).
     * @param s the String containing the semicolon
     * @return a substring of s with one less character
     */
    protected static String _deleteSemiColon(String s) { return  s.substring(0, s.length() - 1); }



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