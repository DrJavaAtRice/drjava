package edu.rice.cs.dynamicjava.sourcechecker;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import koala.dynamicjava.interpreter.error.ExecutionError;
import koala.dynamicjava.parser.wrapper.JavaCCParser;
import koala.dynamicjava.parser.wrapper.ParseError;
import koala.dynamicjava.tree.CompilationUnit;
import koala.dynamicjava.tree.SourceInfo;
import koala.dynamicjava.tree.TypeDeclaration;
import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.*;
import edu.rice.cs.dynamicjava.symbol.ExtendedTypeSystem;
import edu.rice.cs.dynamicjava.symbol.JLSTypeSystem;
import edu.rice.cs.dynamicjava.symbol.Library;
import edu.rice.cs.dynamicjava.symbol.SymbolUtil;
import edu.rice.cs.dynamicjava.symbol.TreeLibrary;
import edu.rice.cs.dynamicjava.symbol.TypeSystem;
import edu.rice.cs.plt.collect.UnindexedRelation;
import edu.rice.cs.plt.collect.Relation;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.reflect.PathClassLoader;
import edu.rice.cs.plt.text.ArgumentParser;
import edu.rice.cs.plt.text.TextUtil;
import edu.rice.cs.plt.tuple.Pair;

public class SourceChecker {

  private final Options _opt;
  private final boolean _quiet;
  private int _statusCount;
  
  public SourceChecker(Options opt, boolean quiet) {
    _opt = opt;
    _quiet = quiet;
    _statusCount = 0;
  }
  
  public void check(File... sources) throws InterpreterException {
    check(IterUtil.asIterable(sources), IterUtil.<File>empty());
  }
  
  public void check(Iterable<? extends File> sources) throws InterpreterException {
    check(sources, IterUtil.<File>empty());
  }
  
  public void check(Iterable<? extends File> sources, Iterable<? extends File> classPath)
                      throws InterpreterException {
    Iterable<CompilationUnit> tree = parse(sources);
    TypeContext context = makeContext(tree, classPath);
    Relation<TypeDeclaration, ClassChecker> decls = extractDeclarations(tree, context);
    initializeClassSignatures(decls);
    checkSignatures(decls);
    checkBodies(decls);
  }
  
  private Iterable<CompilationUnit> parse(Iterable<? extends File> sources) throws InterpreterException {
    final List<CompilationUnit> result = new ArrayList<CompilationUnit>();
    Iterable<File> files = IterUtil.collapse(IterUtil.map(sources, new Lambda<File, Iterable<File>>() {
      private final FileFilter _filter = IOUtil.extensionFilePredicate("java");
      public Iterable<File> value(File f) { return IOUtil.listFilesRecursively(f, _filter); }
    }));
    new Phase<File>("Parsing") {
      protected void step(File source) throws InterpreterException {
        try {
          JavaCCParser parser = new JavaCCParser(new FileReader(source), source, _opt);
          result.add(parser.parseCompilationUnit());
        }
        catch (ParseError e) { throw new ParserException(e); }
        catch (FileNotFoundException e) { throw new SourceException(e); }
      }
      protected SourceInfo location(File f) { return SourceInfo.point(f, 0, 0); }
    }.run(files);
    return result;
  }
  
  private TypeContext makeContext(Iterable<CompilationUnit> sources, Iterable<? extends File> cp) {
    Library classLib = SymbolUtil.classLibrary(new PathClassLoader(null, cp));
    debug.logStart("creating TreeLibrary");
    Library sourceLib = new TreeLibrary(sources, classLib.classLoader(), _opt);
    debug.logEnd("creating TreeLibrary");
    return new TopLevelContext(new LibraryContext(new LibraryContext(classLib), sourceLib), _opt);
  }
  
  private Relation<TypeDeclaration, ClassChecker> extractDeclarations(Iterable<CompilationUnit> sources,
                                                                       TypeContext context)
                                                                       throws InterpreterException {
    final CompilationUnitChecker unitChecker = new CompilationUnitChecker(context, _opt);
    final Relation<TypeDeclaration, ClassChecker> checkers = UnindexedRelation.makeLinkedHashBased();
    new Phase<CompilationUnit>("Resolving imports") {
      protected void step(CompilationUnit u) throws InterpreterException {
        checkers.addAll(unitChecker.extractDeclarations(u));
      }
      protected SourceInfo location(CompilationUnit arg) { return arg.getSourceInfo(); }
    }.run(sources);
    return checkers;
  }

  private void initializeClassSignatures(Relation<TypeDeclaration, ClassChecker> decls) throws InterpreterException {
    new ClassCheckerPhase("Checking class signatures") {
      protected void step(TypeDeclaration ast, ClassChecker checker) { checker.initializeClassSignatures(ast); } 
    }.run(decls);
  }
  
  private void checkSignatures(Relation<TypeDeclaration, ClassChecker> decls) throws InterpreterException {
    new ClassCheckerPhase("Checking class member signatures") {
      protected void step(TypeDeclaration ast, ClassChecker checker) { checker.checkSignatures(ast); } 
    }.run(decls);
  }
  
  private void checkBodies(Relation<TypeDeclaration, ClassChecker> decls) throws InterpreterException {
    new ClassCheckerPhase("Checking class member bodies") {
      protected void step(TypeDeclaration ast, ClassChecker checker) { checker.checkBodies(ast); } 
    }.run(decls);
  }
  
  
  private void startStatus(String description) {
    _statusCount = 0;
    if (!_quiet) {
      String fullDesc = TextUtil.padRight(description + "...", ' ', 36);
      System.out.print(fullDesc);
      System.out.flush();
    }
  }
  
  private void incrementStatus() {
    _statusCount++;
    // arbitrarily chose 10 as the interval for status printouts
    if (!_quiet && (_statusCount % 10 == 0)) { System.out.print('*'); System.out.flush(); }
  }
  
  private void endStatus() {
    if (!_quiet) { System.out.println(); }
  }
  
  
  private abstract class Phase<T> {
    private final String _description;
    protected Phase(String description) { _description = description; }
    
    protected abstract void step(T arg) throws InterpreterException;
    protected abstract SourceInfo location(T arg);
    
    public void run(Iterable<? extends T> args) throws InterpreterException {
      List<InterpreterException> errors = new ArrayList<InterpreterException>();
      debug.logStart(_description);
      startStatus(_description);
      for (T arg : args) {
        debug.logStart("location", location(arg));
        try { step(arg); }
        catch (InterpreterException e) { errors.add(e); }
        catch (RuntimeException e) { errors.add(new InternalException(e, location(arg))); }
        incrementStatus();
        debug.logEnd();
      }
      endStatus();
      debug.logEnd(_description);
      if (!errors.isEmpty()) { throw CompositeException.make(errors); }
    }
  }
  
  private abstract class ClassCheckerPhase extends Phase<Pair<TypeDeclaration, ClassChecker>> {
    protected ClassCheckerPhase(String description) { super(description); }
    protected final void step(Pair<TypeDeclaration, ClassChecker> arg) throws InterpreterException {
      try { step(arg.first(), arg.second()); }
      catch (ExecutionError e) { throw new CheckerException(e); }
    }
    protected final SourceInfo location(Pair<TypeDeclaration, ClassChecker> arg) {
      return arg.first().getSourceInfo();
    }
    protected abstract void step(TypeDeclaration ast, ClassChecker checker);
  }
    
  public static void main(String... args) {
    ArgumentParser argParser = new ArgumentParser();
    argParser.supportOption("classpath", "");
    argParser.supportAlias("cp", "classpath");
    argParser.supportOption("jls");
    argParser.requireParams(1);
    final ArgumentParser.Result parsedArgs = argParser.parse(args);
    
    Options opt = new Options() {
      @Override protected Thunk<? extends TypeSystem> typeSystemFactory() {
        TypeSystem result = parsedArgs.hasOption("jls") ? new JLSTypeSystem(this) : new ExtendedTypeSystem(this);
        return LambdaUtil.valueLambda(result);
      }
      @Override public boolean enforceAllAccess() { return true; }
      @Override public boolean prohibitUncheckedCasts() { return false; }
    };
    Iterable<File> cp = IOUtil.parsePath(parsedArgs.getUnaryOption("classpath"));
    Iterable<File> sources = IterUtil.map(parsedArgs.params(), IOUtil.FILE_FACTORY);
    
    try {
      new SourceChecker(opt, false).check(sources, cp);
      System.out.println("Completed checking successfully.");
    }
    catch (InterpreterException e) {
      debug.log(e);
      e.printUserMessage(new PrintWriter(System.out, true));
    }
  }
  
}
