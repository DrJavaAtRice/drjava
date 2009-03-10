package edu.rice.cs.dynamicjava.sourcechecker;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import koala.dynamicjava.parser.wrapper.JavaCCParser;
import koala.dynamicjava.parser.wrapper.ParseError;
import koala.dynamicjava.tree.CompilationUnit;
import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.CheckerException;
import edu.rice.cs.dynamicjava.interpreter.CompositeException;
import edu.rice.cs.dynamicjava.interpreter.LibraryContext;
import edu.rice.cs.dynamicjava.interpreter.ParserException;
import edu.rice.cs.dynamicjava.interpreter.InterpreterException;
import edu.rice.cs.dynamicjava.interpreter.TopLevelContext;
import edu.rice.cs.dynamicjava.interpreter.TypeContext;
import edu.rice.cs.dynamicjava.symbol.Library;
import edu.rice.cs.dynamicjava.symbol.SymbolUtil;
import edu.rice.cs.dynamicjava.symbol.TreeLibrary;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.reflect.PathClassLoader;
import edu.rice.cs.plt.text.ArgumentParser;

public class SourceChecker {

  private final Options _opt;
  
  public SourceChecker(Options opt) {
    _opt = opt;
  }
  
  public void check(File... sources) throws InterpreterException {
    check(IterUtil.asIterable(sources), IterUtil.<File>empty());
  }
  
  public void check(Iterable<? extends File> sources) throws InterpreterException {
    check(sources, IterUtil.<File>empty());
  }
  
  public void check(Iterable<? extends File> sources, Iterable<? extends File> classPath) throws InterpreterException {
    Iterable<CompilationUnit> tree = parse(sources);
    typeCheck(tree, classPath);
  }
  
  private Iterable<CompilationUnit> parse(Iterable<? extends File> sources) throws InterpreterException {
    List<CompilationUnit> result = new ArrayList<CompilationUnit>();
    List<InterpreterException> errors = new ArrayList<InterpreterException>();
    FileFilter filter = IOUtil.extensionFilePredicate("java");
    for (File f : sources) {
      for (File source : IOUtil.listFilesRecursively(f, filter)) {
        try {
          result.add(new JavaCCParser(new FileReader(source), source).parseCompilationUnit());
        }
        catch (ParseError e) {
          errors.add(new ParserException(e));
        }
        catch (FileNotFoundException e) {
          errors.add(new SourceException(e));
        }
        debug.log("Parsed file " + source);
      }
    }
    if (errors.isEmpty()) { return result; }
    else { throw new CompositeException(errors); }
  }
  
  private void typeCheck(Iterable<CompilationUnit> sources, Iterable<? extends File> cp) throws InterpreterException {
    ClassLoader loader = new PathClassLoader(null, cp);
    Library classLib = SymbolUtil.classLibrary(loader);
    Library sourceLib = new TreeLibrary(sources, loader, _opt);
    TypeContext context = new TopLevelContext(new LibraryContext(new LibraryContext(classLib), sourceLib), loader);
    CompilationUnitChecker checker = new CompilationUnitChecker(context, _opt);
    List<InterpreterException> errors = new ArrayList<InterpreterException>();
    Iterable<CompilationUnitChecker.BodyChecker> bodyCheckers = IterUtil.empty();
    for (CompilationUnit u : sources) {
      debug.logValue("Checking source", "location", u.getSourceInfo());
      try { bodyCheckers = IterUtil.compose(bodyCheckers, checker.check(u)); }
      catch (CheckerException e) { errors.add(e); }
    }
    if (!errors.isEmpty()) {
      for (CompilationUnitChecker.BodyChecker c : bodyCheckers) {
        try { c.check(); }
        catch (CheckerException e) { errors.add(e); }
      }
    }
    if (!errors.isEmpty()) { throw new CompositeException(errors); }
  }
  
  
  public static void main(String... args) {
    ArgumentParser argParser = new ArgumentParser();
    argParser.supportOption("classpath", "");
    argParser.supportAlias("cp", "classpath");
    argParser.requireParams(1);
    ArgumentParser.Result parsedArgs = argParser.parse(args);
    
    Options opt = Options.DEFAULT;
    Iterable<File> cp = IOUtil.parsePath(parsedArgs.getUnaryOption("classpath"));
    Iterable<File> sources = IterUtil.map(parsedArgs.params(), IOUtil.FILE_FACTORY);
    
    try {
      new SourceChecker(opt).check(sources, cp);
      System.out.println("Completed checking successfully.");
    }
    catch (InterpreterException e) { e.printUserMessage(new PrintWriter(System.out, true)); }
  }
  
}
