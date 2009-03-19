package edu.rice.cs.dynamicjava.interpreter;

import java.lang.reflect.Modifier;
import java.util.*;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.Runnable2;
import edu.rice.cs.plt.lambda.WrappedException;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.collect.CollectUtil;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ClassWriter;

import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.tiger.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.interpreter.NodeProperties;
  
import static org.objectweb.asm.Opcodes.*;
import static koala.dynamicjava.tree.ModifierSet.Modifier.*;

/**
 * Compiles an AST class declaration by producing a stub class, where each method consists of
 * forwarding calls to the interpreter.  To preserve enclosing context (since all DynamicJava class
 * declarations appear in a local context), all constructors and static methods are parameterized
 * by an additional {@code RuntimeBindings} argument (the exception is inner classes, where constructors
 * are instead parameterized, as are standard javac compiled inner classes, by an enclosing object).
 * A compiled class looks something like the following:
 * <pre>
 * class CompiledClass extends SuperClass {
 *   private static final TreeCompiler.EvaluationAdapter $adapter;
 *   private final TreeCompiler.BindingsFactory $bindingsFactory;
 * 
 *   static String STATIC_FIELD;
 *   Number instanceField;
 * 
 *   static {
 *     $adapter = ((TreeClassLoader) CompiledClass.class.getClassLoader()).getAdapter("CompiledClass");
 *     STATIC_FIELD = $adapter.evaluateExpression("STATIC_FIELD", RuntimeBindings.EMPTY);
 *   }
 * 
 *   public CompiledClass(RuntimeBindings $bindings, int x, int y) {
 *     $bindingsFactory = $adapter.makeBindingsFactory($bindings);
 *     Object[] $args = new Object[]{ x, y };
 *     super($adapter.evaluateConstructorCallArg("(II)V", 0, $bindings, $args),
 *           $adapter.evaluateConstructorCallArg("(II)V", 1, $bindings, $args));
 *     instanceField = $adapter.evaluateExpression("instanceField", $bindingsFactory.value(this));
 *     $adapter.evaluateConstructorBody("(II)V", $bindingsFactory.value(this), $args);
 *   }
 * 
 *   public Number getInstanceField() {
 *     Object[] $args = new Object[]{};
 *     return $adapter.evaluateMethodBody("getInstance()Ljava/lang/Number;",
 *                                        $bindingsFactory.value(this), $args);
 *   }
 * 
 *   public static String staticMethod(RuntimeBindings $bindings, String arg) {
 *     Object[] $args = new Object[]{ arg };
 *     return $adapter.evaluateMethod("staticMethod(Ljava/lang/String;)Ljava/lang/String;",
 *                                    $bindings, $args);
 *   }
 * 
 * }
 * </pre>
 */
public class TreeCompiler {
  
  private static final String ADAPTER_FIELD = "$adapter";
  private static final String BINDINGS_FACTORY_FIELD = "$bindingsFactory";
  
  private static final String RUNTIME_BINDINGS_NAME =
    org.objectweb.asm.Type.getInternalName(RuntimeBindings.class);
  private static final String EVALUATION_ADAPTER_NAME =
    org.objectweb.asm.Type.getInternalName(EvaluationAdapter.class);
  private static final String BINDINGS_FACTORY_NAME =
    org.objectweb.asm.Type.getInternalName(BindingsFactory.class);
  private static final String TREE_CLASS_LOADER_NAME =
    org.objectweb.asm.Type.getInternalName(TreeClassLoader.class);

  private static final String OBJECT_DESCRIPTOR =
    org.objectweb.asm.Type.getDescriptor(Object.class);
  private static final String CLASS_DESCRIPTOR =
    org.objectweb.asm.Type.getDescriptor(Class.class);
  private static final String CLASS_LOADER_DESCRIPTOR =
    org.objectweb.asm.Type.getDescriptor(ClassLoader.class);
  private static final String STRING_DESCRIPTOR =
    org.objectweb.asm.Type.getDescriptor(String.class);
  private static final String BOOLEAN_DESCRIPTOR =
    org.objectweb.asm.Type.getDescriptor(Boolean.class);
  private static final String CHARACTER_DESCRIPTOR =
    org.objectweb.asm.Type.getDescriptor(Character.class);
  private static final String BYTE_DESCRIPTOR =
    org.objectweb.asm.Type.getDescriptor(Byte.class);
  private static final String SHORT_DESCRIPTOR =
    org.objectweb.asm.Type.getDescriptor(Short.class);
  private static final String INTEGER_DESCRIPTOR =
    org.objectweb.asm.Type.getDescriptor(Integer.class);
  private static final String LONG_DESCRIPTOR =
    org.objectweb.asm.Type.getDescriptor(Long.class);
  private static final String FLOAT_DESCRIPTOR =
    org.objectweb.asm.Type.getDescriptor(Float.class);
  private static final String DOUBLE_DESCRIPTOR =
    org.objectweb.asm.Type.getDescriptor(Double.class);
  private static final String RUNTIME_BINDINGS_DESCRIPTOR =
    org.objectweb.asm.Type.getDescriptor(RuntimeBindings.class);
  private static final String EVALUATION_ADAPTER_DESCRIPTOR =
    org.objectweb.asm.Type.getDescriptor(EvaluationAdapter.class);
  private static final String BINDINGS_FACTORY_DESCRIPTOR =
    org.objectweb.asm.Type.getDescriptor(BindingsFactory.class);
  
    
  private final TreeClass _treeClass;
  private final Options _opt;
  private ClassWriter _classWriter;
  private final EvaluationAdapter _adapter;
  private final String _name;
  private final boolean _java5;
  private final Map<String, MethodDeclaration> _methods; // keys: name + descriptor
  private final Map<String, ConstructorDeclaration> _constructors; // keys: descriptor
  private final Map<String, Initializer> _initializers; // keys: "class init x" or "instance init x"
  private final Map<String, Expression> _expressions; // keys: field names or "anon super arg x"
  private final List<Runnable2<MethodVisitor, StackSizeTracker>> _staticInits;
  private final List<Runnable2<MethodVisitor, StackSizeTracker>> _instanceInits;
  
  public TreeCompiler(TreeClass treeClass, Options opt) {
    _treeClass = treeClass;
    _opt = opt;
    _classWriter = null;
    _adapter = new EvaluationAdapter();
    _name = className(_treeClass);
    _java5 = JavaVersion.CURRENT.supports(JavaVersion.JAVA_5);
    _methods = new HashMap<String, MethodDeclaration>();
    _constructors = new HashMap<String, ConstructorDeclaration>();
    _initializers = new HashMap<String, Initializer>();
    _expressions = new HashMap<String, Expression>();
    _staticInits = new LinkedList<Runnable2<MethodVisitor, StackSizeTracker>>();
    _instanceInits = new LinkedList<Runnable2<MethodVisitor, StackSizeTracker>>();
  }
  
  // Use this to perform verification with the CheckClassAdapter:
  //private ClassVisitor _classWriter;
  //private ClassWriter _realWriter;
  public byte[] bytecode() {
    if (_classWriter == null) {
      _classWriter = new ClassWriter(0);
      //_realWriter = new ClassWriter(0);
      //_classWriter = new org.objectweb.asm.util.CheckClassAdapter(_realWriter);
      compileClass(_treeClass.declaration());
      //dumpClassFile();
    }
    return _classWriter.toByteArray();
    //return _realWriter.toByteArray();
  }
  
  /** Dump the compiled bytes to a file for use in debugging. */
  @SuppressWarnings("unused") private void dumpClassFile() {
    try {
      String name = _treeClass.fullName();
      int dot = name.lastIndexOf('.');
      if (dot >= 0) { name = name.substring(dot); }
      java.io.OutputStream out = new java.io.FileOutputStream(name + ".class");
      out.write(_classWriter.toByteArray());
      //out.write(_realWriter.toByteArray());
      out.close();
    }
    catch (java.io.IOException e) { System.out.println("Can't write bytes"); }
  }
  
  
  public EvaluationAdapter evaluationAdapter() { return _adapter; }
  
  
  private void compileClass(Node ast) {
    Type extendsT = TypeSystem.OBJECT;
    List<? extends ReferenceTypeName> implementsTs = CollectUtil.emptyList();
    List<Node> members = CollectUtil.emptyList();
    int accessFlags = 0;
    final boolean isInterface;
    if (ast instanceof ClassDeclaration) {
      ClassDeclaration cd = (ClassDeclaration) ast;
      extendsT = NodeProperties.getType(cd.getSuperclass());
      if (cd.getInterfaces() != null) { implementsTs = cd.getInterfaces(); }
      members = cd.getMembers();
      accessFlags = cd.getModifiers().getBitVector();
      isInterface = false;
    }
    else if (ast instanceof InterfaceDeclaration) {
      InterfaceDeclaration id = (InterfaceDeclaration) ast;
      if (id.getInterfaces() != null) { implementsTs = id.getInterfaces(); }
      members = id.getMembers();
      accessFlags = id.getModifiers().getBitVector(INTERFACE);
      isInterface = true;
    }
    else if (ast instanceof AnonymousAllocation) {
      AnonymousAllocation aa = (AnonymousAllocation) ast;
      ReferenceTypeName parent = aa.getCreationType();
      Type parentT = NodeProperties.getType(parent);
      if (extractClass(parentT).isInterface()) { implementsTs = Collections.singletonList(parent); }
      else { extendsT = parentT; }
      members = aa.getMembers();
      isInterface = false;
    }
    else if (ast instanceof AnonymousInnerAllocation) {
      extendsT = NodeProperties.getSuperType(ast);
      members = ((AnonymousInnerAllocation) ast).getMembers();
      isInterface = false;
    }
    else { throw new RuntimeException("Unexpected class AST node type: " + ast); }
    
    // Promote default access to public -- a reference may logically appear in the same
    // package but, due to implementation constraints, be loaded by a different class loader.
    // In that situation, default access isn't permitted at run time.
    accessFlags = defaultToPublicAccess(accessFlags);
    
    String classSig = null;
    if (_java5) {
      TypeParameter[] paramAsts;
      if (ast instanceof GenericClassDeclaration) {
        paramAsts = ((GenericClassDeclaration) ast).getTypeParameters();
      }
      else if (ast instanceof GenericInterfaceDeclaration) {
        paramAsts = ((GenericClassDeclaration) ast).getTypeParameters();
      }
      else { paramAsts = new TypeParameter[0]; }
      
      StringBuilder sigBuilder = new StringBuilder();
      if (paramAsts.length > 0) { sigBuilder.append(typeParamListSignature(paramAsts)); }
      sigBuilder.append(typeSignature(extendsT));
      for (ReferenceTypeName implementsT : implementsTs) {
        sigBuilder.append(typeSignature(NodeProperties.getType(implementsT)));
      }
      classSig = sigBuilder.toString();
    }
    
    _classWriter.visit(_java5 ? V1_5 : V1_4, accessFlags, _name, classSig,
                       className(extractClass(extendsT)), extractClassNames(implementsTs));
    
    if (isInterface) {
      // interface fields must be public (adapter is necessary to interpret declared field initializers)
      _classWriter.visitField(ACC_PUBLIC | ACC_STATIC | ACC_FINAL | ACC_SYNTHETIC, ADAPTER_FIELD,
                              EVALUATION_ADAPTER_DESCRIPTOR, null, null).visitEnd();
    }
    else {
      _classWriter.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_SYNTHETIC, ADAPTER_FIELD,
                              EVALUATION_ADAPTER_DESCRIPTOR, null, null).visitEnd();
      _classWriter.visitField(ACC_PRIVATE | ACC_FINAL | ACC_SYNTHETIC, BINDINGS_FACTORY_FIELD,
                              BINDINGS_FACTORY_DESCRIPTOR, null, null).visitEnd();
    }
    
    final List<ConstructorDeclaration> constructors = new LinkedList<ConstructorDeclaration>();
    for (Node member : members) {
      member.acceptVisitor(new AbstractVisitor<Void>() {
        @Override public Void defaultCase(Node member) {
          // Ignore any declarations we can't handle (if this declaration is malformed,
          // that should have already been caught)
          return null;
        }
        @Override public Void visit(ConstructorDeclaration member) {
          // constructor compilation must be deferred until all initialization code has been found
          constructors.add(member); return null;
        }
        @Override public Void visit(MethodDeclaration member) {
          compileMethod(member, isInterface); return null;
        }
        @Override public Void visit(FieldDeclaration member) {
          compileField(member, isInterface); return null;
        }
        @Override public Void visit(ClassInitializer member) {
          compileInitializerBlock(member, true); return null;
        }
        @Override public Void visit(InstanceInitializer member) {
          compileInitializerBlock(member, false); return null;
        }
      });
    }
    
    compileClassInitializer();
    if (ast instanceof AnonymousAllocation) {
      compileAnonymousConstructor((AnonymousAllocation) ast);
    }
    else if (ast instanceof AnonymousInnerAllocation) {
      compileAnonymousInnerConstructor((AnonymousInnerAllocation) ast);
    }
    else if (ast instanceof ClassDeclaration) {
      if (constructors.isEmpty()) { compileDefaultConstructor(extendsT); }
      else {
        for (ConstructorDeclaration k : constructors) { compileConstructor(k, extendsT); }
      }
    }
    
    _classWriter.visitEnd();
  }
  
  
  private void compileDefaultConstructor(Type extendsT) {
    DJClass outerC = SymbolUtil.dynamicOuterClass(_treeClass);
    String methodDescriptor;
    String methodSig;
    if (outerC == null) {
      methodDescriptor = "(" + RUNTIME_BINDINGS_DESCRIPTOR + ")V";
      methodSig = null;
    }
    else {
      Type outerT = SymbolUtil.thisType(outerC);
      methodDescriptor = "(" + typeDescriptor(outerT) + ")V";
      methodSig = _java5 ? ("(" + typeSignature(outerT) + ")V") : null;
    }
    MethodVisitor mv = _classWriter.visitMethod(ACC_PUBLIC, "<init>", methodDescriptor, methodSig, null);
    StackSizeTracker stack = new StackSizeTracker(2);
    mv.visitCode();

    int bindingsVar = emitPartialBindingsVar(mv, outerC, stack);
    emitBindingsFactoryAssignment(mv, bindingsVar, stack);
    emitDefaultSuperCall(mv, extendsT, bindingsVar, stack);
    for (Runnable2<MethodVisitor, StackSizeTracker> initCode : _instanceInits) {
      initCode.run(mv, stack);
    }
    
    mv.visitInsn(RETURN);
    mv.visitMaxs(stack.maxStack(), stack.maxLocals());
    mv.visitEnd();
  }
  
  private void compileAnonymousConstructor(AnonymousAllocation ast) {
    MethodVisitor mv = _classWriter.visitMethod(ACC_PUBLIC, "<init>",
                                                "(" + RUNTIME_BINDINGS_DESCRIPTOR + ")V", null, null);
    StackSizeTracker stack = new StackSizeTracker(2);
    mv.visitCode();
    
    emitBindingsFactoryAssignment(mv, 1, stack);
    
    // super call: this.<init>(args) or this.<init>($bindings, args)
    Type superT = NodeProperties.getType(ast.getCreationType());
    DJClass superC = extractClass(superT);
    if (superC.isInterface()) { emitDefaultSuperCall(mv, TypeSystem.OBJECT, 1, stack); }
    else {
      stack.mark();
      mv.visitVarInsn(ALOAD, 0);
      stack.adjust(1);
      boolean includeBindings = superC.hasRuntimeBindingsParams();
      String extraArg = "";
      if (includeBindings) {
        extraArg = RUNTIME_BINDINGS_DESCRIPTOR;
        mv.visitVarInsn(ALOAD, 1);
        stack.adjust(1);
      }
      DJConstructor superTarget = NodeProperties.getConstructor(ast);
      List<Expression> superArgs = ast.getArguments();
      if (superArgs != null) { emitAnonSuperArgs(mv, superTarget, superArgs, stack); }
      mv.visitMethodInsn(INVOKESPECIAL, className(superC), "<init>",
                         paramListDescriptor(extraArg, superTarget.declaredParameters()) + "V");
      stack.reset();
    }
    
    for (Runnable2<MethodVisitor, StackSizeTracker> initCode : _instanceInits) {
      initCode.run(mv, stack);
    }
    mv.visitInsn(RETURN);
    mv.visitMaxs(stack.maxStack(), stack.maxLocals());
    mv.visitEnd();
  }
  
  private void compileAnonymousInnerConstructor(AnonymousInnerAllocation ast) {
    MethodVisitor mv = _classWriter.visitMethod(ACC_PUBLIC, "<init>",
                                                "(" + RUNTIME_BINDINGS_DESCRIPTOR + ")V", null, null);
    StackSizeTracker stack = new StackSizeTracker(2);
    mv.visitCode();
    
    emitBindingsFactoryAssignment(mv, 1, stack);
    
    // super call: this.<init>(outer, args)
    Type superT = NodeProperties.getSuperType(ast);
    DJClass superC = extractClass(superT);
    DJClass superOuterC = SymbolUtil.dynamicOuterClass(superC);

    stack.mark();
    mv.visitVarInsn(ALOAD, 0);
    stack.adjust(1);
    
    String outerExpKey = "anon super arg outer";
    _expressions.put(outerExpKey, ast.getExpression());
    mv.visitFieldInsn(GETSTATIC, _name, ADAPTER_FIELD, EVALUATION_ADAPTER_DESCRIPTOR);
    mv.visitLdcInsn(outerExpKey);
    mv.visitVarInsn(ALOAD, 1);
    stack.adjust(3);
    mv.visitMethodInsn(INVOKEVIRTUAL, EVALUATION_ADAPTER_NAME, "evaluateExpression",
                       EVALUATE_EXPRESSION_DESCRIPTOR);
    stack.adjust(-2);
    
    DJConstructor superTarget = NodeProperties.getConstructor(ast);
    List<Expression> superArgs = ast.getArguments();
    if (superArgs != null) { emitAnonSuperArgs(mv, superTarget, superArgs, stack); }
    String callDescriptor = paramListDescriptor(typeDescriptor(SymbolUtil.thisType(superOuterC)),
                                                superTarget.declaredParameters()) + "V";
    mv.visitMethodInsn(INVOKESPECIAL, className(superC), "<init>", callDescriptor);
    stack.reset();
    
    for (Runnable2<MethodVisitor, StackSizeTracker> initCode : _instanceInits) {
      initCode.run(mv, stack);
    }

    mv.visitInsn(RETURN);
    mv.visitMaxs(stack.maxStack(), stack.maxLocals());
    mv.visitEnd();
  }
  
  private void compileConstructor(ConstructorDeclaration ast, Type extendsT) {
    DJClass outerC = SymbolUtil.dynamicOuterClass(_treeClass);
    Type outerT = (outerC == null) ? null : SymbolUtil.thisType(outerC);
    List<FormalParameter> params = ast.getParameters();
    List<? extends ReferenceTypeName> exceptions = ast.getExceptions();
    String firstArgDescriptor = (outerT == null) ? RUNTIME_BINDINGS_DESCRIPTOR : typeDescriptor(outerT);
    String methodDescriptor = paramListDescriptor(firstArgDescriptor, extractVars(params)) + "V";

    String methodSig = null;
    if (_java5) {
      TypeParameter[] typeParamAsts;
      if (ast instanceof PolymorphicConstructorDeclaration) {
        typeParamAsts = ((PolymorphicConstructorDeclaration) ast).getTypeParameters();
      }
      else { typeParamAsts = new TypeParameter[0]; }

      StringBuilder sigBuilder = new StringBuilder();
      if (typeParamAsts.length > 0) { sigBuilder.append(typeParamListSignature(typeParamAsts)); }
      String firstArgSig = (outerT == null) ? RUNTIME_BINDINGS_DESCRIPTOR : typeSignature(outerT);
      sigBuilder.append(paramListSignature(firstArgSig, extractVars(params)));
      sigBuilder.append("V");
      for (ReferenceTypeName tn : exceptions) {
        sigBuilder.append('^').append(typeSignature(NodeProperties.getType(tn)));
      }
      methodSig = sigBuilder.toString();
    }
    
    // Promote default access to protected -- a subclass may logically appear in the same
    // package but, due to implementation constraints, be loaded by a different class loader.
    // In that situation, default access isn't permitted at run time.
    int access = defaultToProtectedAccess(ast.getModifiers().getBitVector());
    MethodVisitor mv = _classWriter.visitMethod(access, "<init>", methodDescriptor,
                                                methodSig, extractClassNames(exceptions));

    String key = methodDescriptor;
    _constructors.put(key, ast);
    
    int[] paramLocations = computeParamLocations(params, 2);
    StackSizeTracker stack = new StackSizeTracker(paramLocations[params.size()]);
    mv.visitCode();
    
    int bindingsVar = emitPartialBindingsVar(mv, outerC, stack);
    int boxedParamsVar = emitBoxParams(mv, params, paramLocations, stack);

    // super/this call: this.<init>(args) or this.<init>(outer, args) or this.<init>($bindings, args)
    boolean callsSuper;
    ConstructorCall call = ast.getConstructorCall();
    if (call == null) {
      callsSuper = true;
      emitBindingsFactoryAssignment(mv, bindingsVar, stack);
      emitDefaultSuperCall(mv, extendsT, bindingsVar, stack);
    }
    else {
      callsSuper = call.isSuper();
      if (callsSuper) { emitBindingsFactoryAssignment(mv, bindingsVar, stack); }
      DJConstructor callTarget = NodeProperties.getConstructor(call);
      DJClass extendsC = extractClass(extendsT);
      stack.mark();
      mv.visitVarInsn(ALOAD, 0);
      stack.adjust(1);
      
      // Additional super/this call arg: may be a RuntimeBindings, an enclosing object, or nothing
      String extraArg = "";
      if (call.getExpression() == null) {
        if (callsSuper) {
          if (extendsC.hasRuntimeBindingsParams()) {
            extraArg = RUNTIME_BINDINGS_DESCRIPTOR;
            mv.visitVarInsn(ALOAD, bindingsVar);
            stack.adjust(1);
          }
        }
        else { // this call
          if (outerC == null) {
            extraArg = RUNTIME_BINDINGS_DESCRIPTOR;
            mv.visitVarInsn(ALOAD, bindingsVar);
            stack.adjust(1);
          }
          else { // outerC is defined
            extraArg = typeDescriptor(outerT);
            mv.visitVarInsn(ALOAD, 1);
            stack.adjust(1);
          }
        }
      }
      else { // call.getExpression() is defined
        if (callsSuper) {
          extraArg = typeDescriptor(SymbolUtil.thisType(SymbolUtil.dynamicOuterClass(extendsC)));
        }
        else {
          extraArg = typeDescriptor(outerT);
        }
        mv.visitFieldInsn(GETSTATIC, _name, ADAPTER_FIELD, EVALUATION_ADAPTER_DESCRIPTOR);
        mv.visitLdcInsn(key);
        stack.adjust(2);
        emitIntConstant(mv, -1, stack);
        mv.visitVarInsn(ALOAD, bindingsVar); // can't use $bindingsFactory until after the call
        mv.visitVarInsn(ALOAD, boxedParamsVar);
        stack.adjust(2);
        mv.visitMethodInsn(INVOKEVIRTUAL, EVALUATION_ADAPTER_NAME, "evaluateConstructorCallArg",
                           EVALUATE_CONSTRUCTOR_CALL_ARG_DESCRIPTOR);
        stack.adjust(-4);
      }
      
      // evaluate super/this call args
      int i = 0;
      for (Pair<LocalVariable, Expression> arg :
           IterUtil.zip(callTarget.declaredParameters(), call.getArguments())) {
        Type paramT = arg.first().type();
        Object val = expressionConstantVal(arg.second());
        if (val == null) {
          mv.visitFieldInsn(GETSTATIC, _name, ADAPTER_FIELD, EVALUATION_ADAPTER_DESCRIPTOR);
          mv.visitLdcInsn(key);
          stack.adjust(2);
          emitIntConstant(mv, i, stack);
          mv.visitVarInsn(ALOAD, bindingsVar); // can't use $bindingsFactory until after the call
          mv.visitVarInsn(ALOAD, boxedParamsVar);
          stack.adjust(2);
          mv.visitMethodInsn(INVOKEVIRTUAL, EVALUATION_ADAPTER_NAME, "evaluateConstructorCallArg",
                             EVALUATE_CONSTRUCTOR_CALL_ARG_DESCRIPTOR);
          stack.adjust(-4);
          emitConvert(mv, paramT, stack);
        }
        else { emitConstant(mv, val, stack); }
        i++;
      }
      
      mv.visitMethodInsn(INVOKESPECIAL, callsSuper ? className(extendsC) : _name, "<init>",
                         paramListDescriptor(extraArg, callTarget.declaredParameters()) + "V");
      stack.reset();
    }
      
    if (callsSuper) {
      for (Runnable2<MethodVisitor, StackSizeTracker> initCode : _instanceInits) {
        initCode.run(mv, stack);
      }
    }
      
    mv.visitFieldInsn(GETSTATIC, _name, ADAPTER_FIELD, EVALUATION_ADAPTER_DESCRIPTOR);
    mv.visitLdcInsn(key);
    stack.adjust(2);
    emitLoadBindings(mv, 0, _name, stack);
    mv.visitVarInsn(ALOAD, boxedParamsVar);
    stack.adjust(1);
    mv.visitMethodInsn(INVOKEVIRTUAL, EVALUATION_ADAPTER_NAME, "evaluateConstructorBody",
                       EVALUATE_CONSTRUCTOR_BODY_DESCRIPTOR);
    stack.adjust(-4);
    
    mv.visitInsn(RETURN);
    mv.visitMaxs(stack.maxStack(), stack.maxLocals());
    mv.visitEnd();
  }
  
  private void compileClassInitializer() {
    MethodVisitor mv = _classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
    StackSizeTracker stack = new StackSizeTracker(0);
    mv.visitCode();
    
    // $adapter = ((TreeClassLoader) Class.forName("Name").getClassLoader()).getAdapter("Name");
    mv.visitLdcInsn(_treeClass.fullName()); // don't use _name -- it's an internal name
    stack.adjust(1);
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName",
                       "(" + STRING_DESCRIPTOR + ")" + CLASS_DESCRIPTOR);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader",
                       "()" + CLASS_LOADER_DESCRIPTOR);
    mv.visitTypeInsn(CHECKCAST, TREE_CLASS_LOADER_NAME);
    mv.visitLdcInsn(_treeClass.fullName()); // don't use _name -- it's an internal name
    stack.adjust(1);
    mv.visitMethodInsn(INVOKEVIRTUAL, TREE_CLASS_LOADER_NAME, "getAdapter",
                       "(" + STRING_DESCRIPTOR + ")" + EVALUATION_ADAPTER_DESCRIPTOR);
    stack.adjust(-1);
    mv.visitFieldInsn(PUTSTATIC, _name, ADAPTER_FIELD, EVALUATION_ADAPTER_DESCRIPTOR);
    stack.adjust(-1);

    for (Runnable2<MethodVisitor, StackSizeTracker> initCode : _staticInits) {
      initCode.run(mv, stack);
    }
    
    mv.visitInsn(RETURN);
    mv.visitMaxs(stack.maxStack(), stack.maxLocals());
    mv.visitEnd();
  }
  
  private void compileMethod(MethodDeclaration ast, boolean isInterface) {
    int access = isInterface ? ast.getModifiers().getBitVector(ABSTRACT) : ast.getModifiers().getBitVector();
    if (isInterface) { access = defaultToPublicAccess(access); }
    List<FormalParameter> params = ast.getParameters();
    Type returnT = NodeProperties.getType(ast.getReturnType());
    List<? extends ReferenceTypeName> exceptions = ast.getExceptions();
    boolean isStatic = Modifier.isStatic(access);
    String extraArg = isStatic ? RUNTIME_BINDINGS_DESCRIPTOR : "";
    String methodDescriptor = paramListDescriptor(extraArg, extractVars(params)) + typeDescriptor(returnT);

    String methodSig = null;
    if (_java5) {
      TypeParameter[] typeParamAsts;
      if (ast instanceof PolymorphicMethodDeclaration) {
        typeParamAsts = ((PolymorphicMethodDeclaration) ast).getTypeParameters();
      }
      else { typeParamAsts = new TypeParameter[0]; }

      StringBuilder sigBuilder = new StringBuilder();
      if (typeParamAsts.length > 0) { sigBuilder.append(typeParamListSignature(typeParamAsts)); }
      sigBuilder.append(paramListSignature(extraArg, extractVars(params)));
      sigBuilder.append(typeSignature(returnT));
      for (ReferenceTypeName tn : exceptions) {
        sigBuilder.append('^').append(typeSignature(NodeProperties.getType(tn)));
      }
      methodSig = sigBuilder.toString();
    }
    
    final MethodVisitor mv = _classWriter.visitMethod(access, ast.getName(), methodDescriptor,
                                                      methodSig, extractClassNames(exceptions));
    
    if (!Modifier.isAbstract(access)) {
      String key = ast.getName() + methodDescriptor;
      _methods.put(key, ast);
      
      int[] paramLocations = computeParamLocations(params, 1);
      StackSizeTracker stack = new StackSizeTracker(paramLocations[params.size()]);
      mv.visitCode();
      
      int boxedParamsVar = emitBoxParams(mv, params, paramLocations, stack);
      
      mv.visitFieldInsn(GETSTATIC, _name, ADAPTER_FIELD, EVALUATION_ADAPTER_DESCRIPTOR);
      mv.visitLdcInsn(key);
      stack.adjust(2);
      if (isStatic) { mv.visitVarInsn(ALOAD, 0); stack.adjust(1); }
      else { emitLoadBindings(mv, 0, _name, stack); }
      mv.visitVarInsn(ALOAD, boxedParamsVar);
      stack.adjust(1);
      mv.visitMethodInsn(INVOKEVIRTUAL, EVALUATION_ADAPTER_NAME, "evaluateMethod",
                         EVALUATE_METHOD_DESCRIPTOR);
      stack.adjust(-3);
      
      stack.mark();
      emitConvert(mv, returnT, stack);
      _opt.typeSystem().erase(returnT).apply(new TypeAbstractVisitor_void() {
        @Override public void forReferenceType(ReferenceType t) { mv.visitInsn(ARETURN); }
        @Override public void forPrimitiveType(PrimitiveType t) { mv.visitInsn(IRETURN); }
        @Override public void forLongType(LongType t) { mv.visitInsn(LRETURN); }
        @Override public void forFloatType(FloatType t) { mv.visitInsn(FRETURN); }
        @Override public void forDoubleType(DoubleType t) { mv.visitInsn(DRETURN); }
        @Override public void forVoidType(VoidType t) { mv.visitInsn(RETURN); }
      });
      stack.reset();
      
      mv.visitMaxs(stack.maxStack(), stack.maxLocals());
    }
    mv.visitEnd();
  }
  
  private void compileField(final FieldDeclaration ast, boolean isInterface) {
    int access = isInterface ? ast.getModifiers().getBitVector(STATIC, FINAL) : ast.getModifiers().getBitVector();
    if (isInterface) { access = defaultToPublicAccess(access); }
    final boolean isStatic = Modifier.isStatic(access);
    final Type t = NodeProperties.getType(ast.getType());
    Expression init = ast.getInitializer();
    Object val = null;
    if (isStatic && init != null) { val = expressionConstantVal(init); }
    _classWriter.visitField(access, ast.getName(), typeDescriptor(t), typeSignature(t),
                            val).visitEnd();

    if (init != null && val == null) {
      final String key = ast.getName();
      _expressions.put(key, init);
      (isStatic ? _staticInits : _instanceInits).add(new Runnable2<MethodVisitor, StackSizeTracker>() {
        public void run(MethodVisitor mv, StackSizeTracker stack) {
          stack.mark();
          if (!isStatic) { mv.visitVarInsn(ALOAD, 0); stack.adjust(1); }
          mv.visitFieldInsn(GETSTATIC, _name, ADAPTER_FIELD, EVALUATION_ADAPTER_DESCRIPTOR);
          mv.visitLdcInsn(key);
          stack.adjust(2);
          if (isStatic) {
            mv.visitFieldInsn(GETSTATIC, RUNTIME_BINDINGS_NAME, "EMPTY", RUNTIME_BINDINGS_DESCRIPTOR);
            stack.adjust(1);
          }
          else {
            emitLoadBindings(mv, 0, _name, stack);
          }
          mv.visitMethodInsn(INVOKEVIRTUAL, EVALUATION_ADAPTER_NAME, "evaluateExpression",
                             EVALUATE_EXPRESSION_DESCRIPTOR);
          stack.adjust(-2);
          
          emitConvert(mv, t, stack);
          if (isStatic) {
            mv.visitFieldInsn(PUTSTATIC, _name, ast.getName(), typeDescriptor(t));
          }
          else {
            mv.visitFieldInsn(PUTFIELD, _name, ast.getName(), typeDescriptor(t));
          }
          stack.reset();
        }
      });
    }
    
  }
  
  private void compileInitializerBlock(Initializer ast, final boolean isStatic) {
    final String key = (isStatic ? "class init " : "instance init ") + _initializers.size();
    _initializers.put(key, ast);
    (isStatic ? _staticInits : _instanceInits).add(new Runnable2<MethodVisitor, StackSizeTracker>() {
      public void run(MethodVisitor mv, StackSizeTracker stack) {
        mv.visitFieldInsn(GETSTATIC, _name, ADAPTER_FIELD, EVALUATION_ADAPTER_DESCRIPTOR);
        mv.visitLdcInsn(key);
        stack.adjust(2);
        if (isStatic) {
          mv.visitFieldInsn(GETSTATIC, RUNTIME_BINDINGS_NAME, "EMPTY", RUNTIME_BINDINGS_DESCRIPTOR);
          stack.adjust(1);
        }
        else {
          emitLoadBindings(mv, 0, _name, stack);
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, EVALUATION_ADAPTER_NAME, "evaluateInitializer",
                           EVALUATE_INITIALIZER_DESCRIPTOR);
        stack.adjust(-3);
      }
    });
  }
  
  
  /* Compilation helper methods */

  /** Push a constant of unknown size onto the stack.  Uses emitIntConstant for Integers. */
  private void emitConstant(MethodVisitor mv, Object val, StackSizeTracker stack) {
    if (val instanceof Integer) { emitIntConstant(mv, (Integer) val, stack); }
    else {
      mv.visitLdcInsn(val);
      if (val instanceof Long || val instanceof Double) { stack.adjust(2); }
      else { stack.adjust(1); }
    }
  }
  
  /**
   * Push an int onto the stack.  Optimized to use simpler instructions than LDC (such as ICONST_0)
   * where possible.
   */
  private void emitIntConstant(MethodVisitor mv, int val, StackSizeTracker stack) {
    switch (val) {
      case -1: mv.visitInsn(ICONST_M1); break;
      case 0: mv.visitInsn(ICONST_0); break;
      case 1: mv.visitInsn(ICONST_1); break;
      case 2: mv.visitInsn(ICONST_2); break;
      case 3: mv.visitInsn(ICONST_3); break;
      case 4: mv.visitInsn(ICONST_4); break;
      case 5: mv.visitInsn(ICONST_5); break;
      default:
        if (val >= Byte.MIN_VALUE && val <= Byte.MAX_VALUE) { mv.visitIntInsn(BIPUSH, val); }
        else if (val >= Short.MIN_VALUE && val <= Short.MAX_VALUE) { mv.visitIntInsn(SIPUSH, val); }
        else { mv.visitLdcInsn(val); }
        break;
    }
    stack.adjust(1);
  }
  
  /**
   * Determine the location of a constructor's RuntimeBindings before "this" has been bound.
   * If this is one of the parameters of the constructor, no code is generated and the value 1 is
   * returned.  Otherwise, the constructor must have an outer object parameter, and that object
   * is used to compute the RuntimeBindings, which is then stored as a new local variable.
   */
  private int emitPartialBindingsVar(MethodVisitor mv, DJClass outerC, StackSizeTracker stack) {
    if (outerC == null) { return 1; }
    else {
      int result = stack.newVariable();
      emitLoadBindings(mv, 1, className(outerC), stack);
      mv.visitVarInsn(ASTORE, result);
      stack.adjust(-1);
      return result;
    }
  }
    
  /**
   * Set the "$bindingsFactory" field in a constructor.  Callers must provide a variable index
   * at which the partial bindings are stored.
   */
  private void emitBindingsFactoryAssignment(MethodVisitor mv, int bindingsVar, StackSizeTracker stack) {
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETSTATIC, _name, ADAPTER_FIELD, EVALUATION_ADAPTER_DESCRIPTOR);
    mv.visitVarInsn(ALOAD, bindingsVar);
    stack.adjust(3);
    mv.visitMethodInsn(INVOKEVIRTUAL, EVALUATION_ADAPTER_NAME, "makeBindingsFactory",
                       MAKE_BINDINGS_FACTORY_DESCRIPTOR);
    stack.adjust(-1);
    mv.visitFieldInsn(PUTFIELD, _name, BINDINGS_FACTORY_FIELD, BINDINGS_FACTORY_DESCRIPTOR);
    stack.adjust(-2);
  }
  
  /** Super constructor call -- one of "super()" or "super($bindings)" */
  private void emitDefaultSuperCall(MethodVisitor mv, Type extendsT, int bindingsVar,
                                    StackSizeTracker stack) {
    DJClass extendsC = extractClass(extendsT);
    mv.visitVarInsn(ALOAD, 0);
    stack.adjust(1);
    if (extendsC.hasRuntimeBindingsParams()) {
      mv.visitVarInsn(ALOAD, bindingsVar);
      stack.adjust(1);
      mv.visitMethodInsn(INVOKESPECIAL, className(extendsC), "<init>",
                         "(" + RUNTIME_BINDINGS_DESCRIPTOR + ")V");
      stack.adjust(-2);
    }
    else {
      mv.visitMethodInsn(INVOKESPECIAL, className(extendsC), "<init>", "()V");
      stack.adjust(-1);
    }
  }
  
  /** Invoke "var.$bindingsFactory.value(var)", for some local variable with the given class name. */
  private void emitLoadBindings(MethodVisitor mv, int thisVar, String thisClassName,
                                StackSizeTracker stack) {
    mv.visitVarInsn(ALOAD, thisVar);
    stack.adjust(1);
    mv.visitFieldInsn(GETFIELD, thisClassName, BINDINGS_FACTORY_FIELD, BINDINGS_FACTORY_DESCRIPTOR);
    mv.visitVarInsn(ALOAD, thisVar);
    stack.adjust(1);
    mv.visitMethodInsn(INVOKEVIRTUAL, BINDINGS_FACTORY_NAME, "value", BINDINGS_FACTORY_VALUE_DESCRIPTOR);
    stack.adjust(-1);
  }
  
    
  /** Evaluate all arguments appearing in an anonymous class's super constructor invocation. */
  private void emitAnonSuperArgs(MethodVisitor mv, DJConstructor k, List<Expression> args,
                                 StackSizeTracker stack) {
    int i = 0;
    for (Pair<LocalVariable, Expression> arg : IterUtil.zip(k.declaredParameters(), args)) {
      String key = "anon super arg " + i;
      _expressions.put(key, arg.second());
      Type paramT = arg.first().type();
      Object val = expressionConstantVal(arg.second());
      if (val == null) {
        mv.visitFieldInsn(GETSTATIC, _name, ADAPTER_FIELD, EVALUATION_ADAPTER_DESCRIPTOR);
        mv.visitLdcInsn(key);
        mv.visitVarInsn(ALOAD, 1);
        stack.adjust(3);
        mv.visitMethodInsn(INVOKEVIRTUAL, EVALUATION_ADAPTER_NAME, "evaluateExpression",
                           EVALUATE_EXPRESSION_DESCRIPTOR);
        stack.adjust(-2);
        emitConvert(mv, paramT, stack);
      }
      else { emitConstant(mv, val, stack); }
      i++;
    }
  }
  
  /**
   * Copy the arguments to a constructor or method into an Object[]; box as necessary.
   * @return  The location of the variable storing the Object[]
   */
  private int emitBoxParams(MethodVisitor mv, List<FormalParameter> params, int[] paramLocations,
                            StackSizeTracker stack) {
    emitIntConstant(mv, params.size(), stack);
    mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    int i = 0;
    for (FormalParameter param : params) {
      Type t = NodeProperties.getVariable(param).type();
      mv.visitInsn(DUP);
      stack.adjust(1);
      emitIntConstant(mv, i, stack);
      emitLoadAndBox(mv, t, paramLocations[i], stack);
      mv.visitInsn(AASTORE);
      stack.adjust(-3);
      i++;
    }
    int result = stack.newVariable();
    mv.visitVarInsn(ASTORE, result);
    stack.adjust(-1);
    return result;
  }
    
  /** Load a single variable onto the stack and box it if it is a primitive. */
  private void emitLoadAndBox(final MethodVisitor mv, Type t, final int var,
                                     final StackSizeTracker stack) {
    t.apply(new TypeAbstractVisitor_void() {
      @Override public void defaultCase(Type t) {
        mv.visitVarInsn(ALOAD, var); stack.adjust(1);
      }
      @Override public void forBooleanType(BooleanType t) {
        mv.visitVarInsn(ILOAD, var);
        stack.adjust(1);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)" + BOOLEAN_DESCRIPTOR);
      }
      @Override public void forCharType(CharType t) {
        generateBoxingCode(ILOAD, 1, "java/lang/Character", CHARACTER_DESCRIPTOR, "C");
      }
      @Override public void forByteType(ByteType t) {
        generateBoxingCode(ILOAD, 1, "java/lang/Byte", BYTE_DESCRIPTOR, "B");
      }
      @Override public void forShortType(ShortType t) {
        generateBoxingCode(ILOAD, 1, "java/lang/Short", SHORT_DESCRIPTOR, "S");
      }
      @Override public void forIntType(IntType t) {
        generateBoxingCode(ILOAD, 1, "java/lang/Integer", INTEGER_DESCRIPTOR, "I");
      }
      @Override public void forLongType(LongType t) {
        generateBoxingCode(LLOAD, 2, "java/lang/Long", LONG_DESCRIPTOR, "J");
      }
      @Override public void forFloatType(FloatType t) {
        generateBoxingCode(FLOAD, 1, "java/lang/Float", FLOAT_DESCRIPTOR, "F");
      }
      @Override public void forDoubleType(DoubleType t) {
        generateBoxingCode(DLOAD, 2, "java/lang/Double", DOUBLE_DESCRIPTOR, "D");
      }
      
      private void generateBoxingCode(int loadOp, int size, String className, String boxedDescriptor,
                                      String primDescriptor) {
        if (_java5) {
          mv.visitVarInsn(loadOp, var);
          stack.adjust(size);
          mv.visitMethodInsn(INVOKESTATIC, className, "valueOf", "(" + primDescriptor + ")" +
                             boxedDescriptor);
          stack.adjust(-size+1);
        }
        else {
          mv.visitTypeInsn(NEW, className);
          mv.visitInsn(DUP);
          stack.adjust(2);
          mv.visitVarInsn(loadOp, var);
          stack.adjust(size);
          mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "(" + primDescriptor + ")V");
          stack.adjust(-size-1);
        }
      }
      
    });
  }
  
  /** Convert the value on the stack to the given type, casting or unboxing if necessary. */
  private void emitConvert(final MethodVisitor mv, Type expectedT, final StackSizeTracker stack) {
    _opt.typeSystem().erase(expectedT).apply(new TypeAbstractVisitor_void() {
      // do nothing for NullType -- it should be erased to Object, so no cast is needed
      @Override public void forArrayType(ArrayType t) {
        mv.visitTypeInsn(CHECKCAST, typeDescriptor(t));
      }
      @Override public void forClassType(ClassType t) {
        if (!t.equals(TypeSystem.OBJECT)) {
          mv.visitTypeInsn(CHECKCAST, className(t.ofClass()));
        }
      }
      @Override public void forBooleanType(BooleanType t) {
        generateUnboxingCode(1, "java/lang/Boolean", "booleanValue", "Z");
      }
      @Override public void forCharType(CharType t) {
        generateUnboxingCode(1, "java/lang/Character", "charValue", "C");
      }
      @Override public void forByteType(ByteType t) {
        generateUnboxingCode(1, "java/lang/Byte", "byteValue", "B");
      }
      @Override public void forShortType(ShortType t) {
        generateUnboxingCode(1, "java/lang/Short", "shortValue", "S");
      }
      @Override public void forIntType(IntType t) {
        generateUnboxingCode(1, "java/lang/Integer", "intValue", "I");
      }
      @Override public void forLongType(LongType t) {
        generateUnboxingCode(2, "java/lang/Long", "longValue", "J");
      }
      @Override public void forFloatType(FloatType t) {
        generateUnboxingCode(1, "java/lang/Float", "floatValue", "F");
      }
      @Override public void forDoubleType(DoubleType t) {
        generateUnboxingCode(2, "java/lang/Double", "doubleValue", "D");
      }
      private void generateUnboxingCode(int size, String className, String methodName,
                                        String primDescriptor) {
        // The bytecode verifier requires this cast
        mv.visitTypeInsn(CHECKCAST, className);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, methodName, "()" + primDescriptor);
        stack.adjust(-1+size);
      }
    });
  }
  
  /** Helper method for printing debug messages from generated code. */
  @SuppressWarnings("unused") private void emitDebug(MethodVisitor mv, String message, StackSizeTracker stack) {
    mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    mv.visitLdcInsn(message);
    stack.adjust(2);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    stack.adjust(-2);
  }

  /** Helper method for printing a toString of the current object.  The object is not consumed. */
  @SuppressWarnings("unused") private void emitPrintToString(MethodVisitor mv, StackSizeTracker stack) {
    mv.visitInsn(DUP);
    mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    stack.adjust(2);
    mv.visitInsn(SWAP);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
    stack.adjust(-2);
  }
  
  /* AUXILIARY STATIC METHODS */
  
  private static String typeSignature(Type t) { return encodeType(t, '.'); }
  
  /** Nonstatic because it depends on field _opt. */
  private String typeDescriptor(Type t) { return encodeType(_opt.typeSystem().erase(t), '$'); }
  
  private static String className(DJClass c) { return c.fullName().replace('.', '/'); }
  
  private static DJClass extractClass(Type t) {
    if (t instanceof ClassType) { return ((ClassType) t).ofClass(); }
    else {
      throw new IllegalArgumentException("Expected ClassType, but given " + t.getClass().getName());
    }
  }
      
  private static String[] extractClassNames(List<? extends ReferenceTypeName> types) {
    String[] result = new String[types.size()];
    int i = 0;
    for (ReferenceTypeName tn : types) {
      result[i++] = className(extractClass(NodeProperties.getType(tn)));
    }
    return result;
  }
  
  private static String typeParamListSignature(TypeParameter[] params) {
    StringBuilder result = new StringBuilder();
    result.append('<');
    for (TypeParameter p : params) {
      VariableType t = NodeProperties.getTypeVariable(p);
      result.append(t.symbol().name());
      Type upper = t.symbol().upperBound();
      boolean validIntersection = false;
      if (upper instanceof IntersectionType) {
        for (Type intersectElt : ((IntersectionType) upper).ofTypes()) {
          validIntersection = true;
          result.append(':').append(typeSignature(intersectElt));
        }
      }
      if (!validIntersection) { result.append(':').append(typeSignature(upper)); }
    }
    result.append('>');
    return result.toString();
  }
  
  private static Iterable<LocalVariable> extractVars(Iterable<? extends FormalParameter> params) {
    return IterUtil.map(params, NodeProperties.NODE_VARIABLE);
  }
  
  private static String paramListSignature(String prefix, Iterable<LocalVariable> params) {
    StringBuilder result = new StringBuilder();
    result.append('(');
    result.append(prefix);
    for (LocalVariable var : params) { result.append(typeSignature(var.type())); }
    result.append(')');
    return result.toString();
  }
      
  /** Nonstatic because it depends on field _opt. */
  private String paramListDescriptor(String prefix, Iterable<LocalVariable> params) {
    StringBuilder result = new StringBuilder();
    result.append('(');
    result.append(prefix);
    for (LocalVariable var : params) { result.append(typeDescriptor(var.type())); }
    result.append(')');
    return result.toString();
  }
  
  /** Abstraction of typeDescriptor and typeSignature.  */
  private static String encodeType(Type t, final char classDelim) {
    final StringBuilder result = new StringBuilder();
    t.apply(new TypeAbstractVisitor_void() {
      @Override public void forBooleanType(BooleanType t) { result.append('Z'); }
      @Override public void forCharType(CharType t) { result.append('C'); }
      @Override public void forByteType(ByteType t) { result.append('B'); }
      @Override public void forShortType(ShortType t) { result.append('S'); }
      @Override public void forIntType(IntType t) { result.append('I'); }
      @Override public void forLongType(LongType t) { result.append('J'); }
      @Override public void forFloatType(FloatType t) { result.append('F'); }
      @Override public void forDoubleType(DoubleType t) { result.append('D'); }
      @Override public void forNullType(NullType t) { result.append(OBJECT_DESCRIPTOR); }
      @Override public void forArrayType(ArrayType t) { result.append('['); t.ofType().apply(this); }

      @Override public void forClassType(ClassType t) {
        result.append('L');
        boolean first = true;
        for (DJClass c : SymbolUtil.outerClassChain(t.ofClass())) {
          if (first) { result.append(className(c)); first = false; }
          else { result.append(classDelim).append(c.declaredName()); }
        }
        result.append(';');
      }
      
      @Override public void forParameterizedClassType(ParameterizedClassType t) {
        result.append('L');
        boolean first = true;
        Iterator<? extends Type> args = t.typeArguments().iterator();
        DJClass tClass = t.ofClass();
        for (DJClass c : SymbolUtil.outerClassChain(tClass)) {
          if (first) { result.append(className(c)); first = false; }
          else { result.append(classDelim).append(c.declaredName()); }
          if (SymbolUtil.dynamicallyEncloses(c, tClass)) {
            Iterable<VariableType> params = c.declaredTypeParameters();
            if (!IterUtil.isEmpty(params)) {
              result.append('<');
              for (VariableType param : params) { args.next().apply(this); }
              result.append('>');
            }
          }
        }
        result.append(';');
      }
      
      @Override public void forVariableType(VariableType t) {
        result.append('T');
        result.append(t.symbol().name());
        result.append(';');
      }
      
      @Override public void forIntersectionType(IntersectionType t) {
        if (IterUtil.isEmpty(t.ofTypes())) { result.append(OBJECT_DESCRIPTOR); }
        else { IterUtil.first(t.ofTypes()).apply(this); }
      }
      
      @Override public void forUnionType(UnionType t) {
        result.append("Ljava/lang/Object;");
      }
      
      @Override public void forTopType(TopType t) { result.append(OBJECT_DESCRIPTOR); }
      @Override public void forBottomType(BottomType t) { result.append(OBJECT_DESCRIPTOR); }
      @Override public void forVoidType(VoidType v) { result.append("V"); }

      @Override public void forWildcard(Wildcard w) {
        if (w.symbol().upperBound().equals(TypeSystem.OBJECT)) {
          if (w.symbol().lowerBound().equals(TypeSystem.NULL)) { result.append('*'); }
          else { result.append('-'); w.symbol().lowerBound().apply(this); }
        }
        else { result.append('+'); w.symbol().upperBound().apply(this); }
      }
      
    });
    return result.toString();
  }
  
  /**
   * Create an array mapping param indices to their corresponding locations in the local variable
   * stack.  The first entry has index {@code reservedSize} (representing the space reserved for
   * implicit parameters like "this" and "bindings").  The last entry (at index {@code params.size() + 1})
   * is the size of the local variable stack occupied by parameters.  This mapping is nontrivial,
   * because while most values have a 1-unit size, {@code double}s and {@code long}s have size 2.
   */
  private static int[] computeParamLocations(List<FormalParameter> params, int reservedSize) {
    int[] result = new int[params.size() + 1];
    int index = reservedSize;
    int paramI = 0;
    for (FormalParameter p : params) {
      result[paramI] = index;
      Type t = NodeProperties.getVariable(p).type();
      if (t instanceof LongType || t instanceof DoubleType) { index += 2; }
      else { index += 1; }
      paramI++;
    }
    result[paramI] = index;
    return result;
  }
  
  /**
   * Get the constant value of the given expression, if it exists and is of a supported type.
   * Otherwise, return {@code null}.
   */
  private static Object expressionConstantVal(Expression exp) {
    Object result = null;
    if (NodeProperties.hasValue(exp)) {
      result = NodeProperties.getValue(exp);
      if (!(result instanceof Integer || result instanceof Float || result instanceof Long ||
            result instanceof Double || result instanceof String)) {
        result = null;
      }
    }
    return result;
  }
  
  /** If the given access flags has default access, set it to public. */
  private static int defaultToPublicAccess(int accessFlags) {
    if (!Modifier.isPublic(accessFlags) && !Modifier.isProtected(accessFlags) &&
        !Modifier.isPrivate(accessFlags)) {
      accessFlags |= PUBLIC.getBits();
    }
    return accessFlags;
  }
  
  /** If the given access flags has default access, set it to protected. */
  private static int defaultToProtectedAccess(int accessFlags) {
    if (!Modifier.isPublic(accessFlags) && !Modifier.isProtected(accessFlags) &&
        !Modifier.isPrivate(accessFlags)) {
      accessFlags |= PROTECTED.getBits();
    }
    return accessFlags;
  }
  
  
  /* Helper classes */
  
  /**
   * Simplifies calculating the stack size that must be allocated for a method.  Keeps track
   * of both local variable allocations and stack growth as code is generated.
   */
  private static class StackSizeTracker {
    private int _maxStack;
    private int _maxLocals;
    private int _currentStack;
    private int _markedStack;
    
    /**
     * @param reservedParamsSize  The space reserved for special parameters like {@code this}.
     */
    public StackSizeTracker(int reservedParamsSize) {
      _maxStack = 0;
      _maxLocals = reservedParamsSize;
      _currentStack = 0;
      _markedStack = 0;
    }
    
    /**
     * Adjust the current stack size.  Positive values cause it to grow; negative values cause it
     * to shrink.
     */
    public void adjust(int delta) {
      _currentStack += delta;
      if (_currentStack > _maxStack) { _maxStack = _currentStack; }
      if (_currentStack < 0) { throw new IllegalStateException("Stack has negative size"); }
    }
    
    /**
     * Mark the current stack size for later resetting.  Note that nested marks (two marks followed by
     * two resets) are not supported (both resets go to the most recent mark).
     */
    public void mark() { _markedStack = _currentStack; }
    
    /** Reset stack size to the previously-marked location. */
    public void reset() { _currentStack = _markedStack; }
    
    /**
     * Allocate space for a variable of any type besides {@code double} or {@code long}.
     * @return  The index at which the variable should be stored.
     */
    public int newVariable() {
      return _maxLocals++; // result is the pre-increment value
    }
    
    /**
     * Allocate a variable holding a {@code double} or {@code long}.
     * @return  The index at which the variable should be stored.
     */
    public int newBigVariable() {
      int result = _maxLocals;
      _maxLocals += 2;
      return result;
    }
    
    public int maxStack() { return _maxStack; }
    
    public int maxLocals() { return _maxLocals; }
    
  }
  
  
  /**
   * Provides an interface through which compiled classes can invoke the interpreter.  Each instance
   * corresponds to a single compiled class; within the class, each relevant piece of syntax has 
   * a key (a string) generated at compile-time, which the adapter maps to the AST.  Compiled 
   * classes must only provide that key and an environment in which to perform evaluation,
   * and the adapter calls the appropriate interpreter methods.
   */
  public class EvaluationAdapter {
    
    /**
     * Evaluate a method body in the given environment, extended with the method parameters bound
     * to the given arguments.  If the method is non-static, {@code this} should be defined in
     * {@code bindings}.
     * @return  The value returned by the method body (via a return statement); {@code null}
     *          if the return statement is void, or if no return statement is evaluated.
     * @throws Throwable  Any exceptions (or errors) that occur during evaluation, without any wrapping.
     */
    public Object evaluateMethod(String key, RuntimeBindings bindings, Object[] args) throws Throwable {
      MethodDeclaration decl = _methods.get(key);
      RuntimeBindings methodBindings = bindArgs(bindings, decl.getParameters(), args);
      return evaluateBlock(decl.getBody(), NodeProperties.getErasedType(decl).value(), methodBindings);
    }
    
    /**
     * Evaluate an expression in the given environment.
     * @return  The value of the interpreted expression.
     * @throws Throwable  Any exceptions (or errors) that occur during evaluation, without any wrapping.
     */
    public Object evaluateExpression(String key, RuntimeBindings bindings) throws Throwable {
      Expression exp = _expressions.get(key);
      return evaluateExpression(exp, bindings);
    }
    
    /**
     * Evaluate the argument to a {@code this} or {@code super} constructor call at the given index.
     * The given environment is extended with the constructor parameters bound to the given arguments;
     * {@code this} should be defined in {@code bindings}.
     * @param index  0-based index into the call's arguments; -1 for the call's outer (prefix) expression
     * @throws Throwable  Any exceptions (or errors) that occur during evaluation, without any wrapping.
     */
    public Object evaluateConstructorCallArg(String key, int index, RuntimeBindings bindings,
                                             Object[] args) throws Throwable {
      ConstructorDeclaration decl = _constructors.get(key);
      Expression exp;
      if (index == -1) { exp = decl.getConstructorCall().getExpression(); }
      else { exp = decl.getConstructorCall().getArguments().get(index); }
      RuntimeBindings constructorBindings = bindArgs(bindings, decl.getParameters(), args);
      return evaluateExpression(exp, constructorBindings);
    }
    
    /**
     * Evaluate a constructor body in the given environment, extended with the constructor parameters
     * bound to the given arguments.  {@code this} should be defined in {@code bindings}.
     * @throws Throwable  Any exceptions (or errors) that occur during evaluation, without any wrapping.
     */    
    public void evaluateConstructorBody(String key, RuntimeBindings bindings, Object[] args)
      throws Throwable {
      ConstructorDeclaration decl = _constructors.get(key);
      RuntimeBindings constructorBindings = bindArgs(bindings, decl.getParameters(), args);
      evaluateBlock(new BlockStatement(decl.getStatements()), void.class, constructorBindings);
    }
    
    /**
     * Evaluate an initializer in the given environment.  If the initializer is non-static,
     * {@code this} should be defined in {@code bindings}.
     * @throws Throwable  Any exceptions (or errors) that occur during evaluation, without any wrapping.
     */
    public void evaluateInitializer(String key, RuntimeBindings bindings) throws Throwable {
      Initializer decl = _initializers.get(key);
      evaluateBlock(decl.getBlock(), void.class, bindings);
    }
    
    /**
     * Make a factory object for binding a value to {@code this}.  This level of indirection is
     * necessary because constructors cannot pass "this" to a method until after the super constructor
     * has run; in the mean time, other methods of this class or constructors of its inner classes may
     * have been invoked, requiring some way to produce the complete bindings.
     * @param bindings  A set of bindings to extend.
     */
    public BindingsFactory makeBindingsFactory(RuntimeBindings bindings) {
      return new BindingsFactory(bindings, _treeClass);
    }
    
    private RuntimeBindings bindArgs(RuntimeBindings parent, List<FormalParameter> params,
                                     Object[] args) {
      return new RuntimeBindings(parent, extractVars(params), IterUtil.asIterable(args));
    }
    
    private Object evaluateExpression(Expression exp, RuntimeBindings bindings) throws Throwable {
      try { return new ExpressionEvaluator(bindings, _opt).value(exp); }
      catch (WrappedException e) {
        if (e.getCause() instanceof EvaluatorException) {
          throw ((EvaluatorException) e.getCause()).getCause();
        }
        else { throw e; }
      }
    }
    
    private Object evaluateBlock(BlockStatement block, Class<?> returnType,
                                 RuntimeBindings bindings) throws Throwable {
      try {
        block.acceptVisitor(new StatementEvaluator(bindings, _opt));
        // if we didn't return, produce null or a zero primitive
        return SymbolUtil.initialValue(returnType);
      }
      catch (StatementEvaluator.ReturnException e) {
        return e.value().unwrap(null);
      }
      catch (WrappedException e) {
        if (e.getCause() instanceof EvaluatorException) {
          throw ((EvaluatorException) e.getCause()).getCause();
        }
        else { throw e; }
      }
    }
    
  }
  
  /**
   * A simple factory mapping an object to a RuntimeBindings in which that object is defined as "this".
   * See {@link EvaluationAdapter#makeBindingsFactory}.  By informal contract, all invocations of
   * {@link #value} should use the same {@code thisVal} parameter.
   */
  public static class BindingsFactory implements Lambda<Object, RuntimeBindings> {
    private final RuntimeBindings _bindings;
    private final DJClass _thisClass;
    private RuntimeBindings _cachedResult;
    public BindingsFactory(RuntimeBindings bindings, DJClass thisClass) {
      _bindings = bindings;
      _thisClass = thisClass;
      _cachedResult = null;
    }
    public RuntimeBindings value(Object thisVal) {
      // Since a BindingsFactory is stored with every instance of every class compiled by TreeCompiler,
      // and run with every method invocation, the time/space tradeoff of caching here is worth examining
      // more closely.
      if (_cachedResult == null) { _cachedResult = new RuntimeBindings(_bindings, _thisClass, thisVal); }
      return _cachedResult;
    }
  }
    
  
  /* Descriptors of EvaluationAdapter methods.  Declared here to be easy to check. */
  
  private static final String EVALUATE_METHOD_DESCRIPTOR =
    "(" + STRING_DESCRIPTOR + RUNTIME_BINDINGS_DESCRIPTOR + "[" + OBJECT_DESCRIPTOR +
    ")" + OBJECT_DESCRIPTOR;
  
  private static final String EVALUATE_EXPRESSION_DESCRIPTOR =
    "(" + STRING_DESCRIPTOR + RUNTIME_BINDINGS_DESCRIPTOR + ")" + OBJECT_DESCRIPTOR;
  
  private static final String EVALUATE_CONSTRUCTOR_CALL_ARG_DESCRIPTOR =
    "(" + STRING_DESCRIPTOR + "I" + RUNTIME_BINDINGS_DESCRIPTOR + "[" + OBJECT_DESCRIPTOR +
    ")" + OBJECT_DESCRIPTOR;
  
  private static final String EVALUATE_CONSTRUCTOR_BODY_DESCRIPTOR =
    "(" + STRING_DESCRIPTOR + RUNTIME_BINDINGS_DESCRIPTOR + "[" + OBJECT_DESCRIPTOR + ")V";
  
  private static final String EVALUATE_INITIALIZER_DESCRIPTOR =
    "(" + STRING_DESCRIPTOR + RUNTIME_BINDINGS_DESCRIPTOR + ")V";
  
  private static final String MAKE_BINDINGS_FACTORY_DESCRIPTOR =
    "(" + RUNTIME_BINDINGS_DESCRIPTOR + ")" + BINDINGS_FACTORY_DESCRIPTOR;
  
  private static final String BINDINGS_FACTORY_VALUE_DESCRIPTOR =
    "(" + OBJECT_DESCRIPTOR + ")" + RUNTIME_BINDINGS_DESCRIPTOR;
  
}
