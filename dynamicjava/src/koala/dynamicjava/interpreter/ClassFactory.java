/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.interpreter;

import java.io.*;
import java.lang.reflect.*;

import koala.dynamicjava.classfile.*;

/**
 * The instances of this class dynamically create java classes
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/05/08
 */

public class ClassFactory extends ClassFile {
  
  private final static String VOID_NAME =
    JVMUtilities.getName("void");
  private final static String BOOLEAN_NAME =
    JVMUtilities.getName("boolean");
  private final static String CHAR_NAME =
    JVMUtilities.getName("char");
  private final static String INT_NAME =
    JVMUtilities.getName("int");
  private final static String LONG_NAME =
    JVMUtilities.getName("long");
  private final static String FLOAT_NAME =
    JVMUtilities.getName("float");
  private final static String DOUBLE_NAME =
    JVMUtilities.getName("double");
  private final static String BYTE_NAME =
    JVMUtilities.getName("byte");
  private final static String SHORT_NAME =
    JVMUtilities.getName("short");
  private final static String OBJECT_NAME =
    JVMUtilities.getName("java.lang.Object");
  
  private final static String BOOLEAN_WRAPPER_NAME =
    JVMUtilities.getName("java.lang.Boolean");
  private final static String CHAR_WRAPPER_NAME =
    JVMUtilities.getName("java.lang.Character");
  private final static String INT_WRAPPER_NAME =
    JVMUtilities.getName("java.lang.Integer");
  private final static String LONG_WRAPPER_NAME =
    JVMUtilities.getName("java.lang.Long");
  private final static String FLOAT_WRAPPER_NAME =
    JVMUtilities.getName("java.lang.Float");
  private final static String DOUBLE_WRAPPER_NAME =
    JVMUtilities.getName("java.lang.Double");
  private final static String BYTE_WRAPPER_NAME =
    JVMUtilities.getName("java.lang.Byte");
  private final static String SHORT_WRAPPER_NAME =
    JVMUtilities.getName("java.lang.Short");
  private final static String NUMBER_NAME =
    JVMUtilities.getName("java.lang.Number");
  
  /**
   * The class "java.lang.Boolean" identifier
   */
  private final static ClassIdentifier BOOLEAN_IDENTIFIER = 
    new ClassIdentifier(BOOLEAN_WRAPPER_NAME);
  
  /**
   * The Boolean(boolean) constructor identifier
   */
  private final static MethodIdentifier BOOLEAN_CONSTRUCTOR = 
    new MethodIdentifier
    (BOOLEAN_WRAPPER_NAME, "<init>", VOID_NAME, new String[] { BOOLEAN_NAME });
  
  /**
   * The class "java.lang.Character" identifier
   */
  private final static ClassIdentifier CHARACTER_IDENTIFIER = 
    new ClassIdentifier(CHAR_WRAPPER_NAME);
  
  /**
   * The Character(char) constructor identifier
   */
  private final static MethodIdentifier CHARACTER_CONSTRUCTOR = 
    new MethodIdentifier
    (CHAR_WRAPPER_NAME, "<init>", VOID_NAME, new String[] { CHAR_NAME });
  
  /**
   * The class "java.lang.Integer" identifier
   */
  private final static ClassIdentifier INTEGER_IDENTIFIER = 
    new ClassIdentifier(INT_WRAPPER_NAME);
  
  /**
   * The Integer(int) constructor identifier
   */
  private final static MethodIdentifier INTEGER_CONSTRUCTOR = 
    new MethodIdentifier
    (INT_WRAPPER_NAME, "<init>", VOID_NAME, new String[] { INT_NAME });
  
  /**
   * The class "java.lang.Long" identifier
   */
  private final static ClassIdentifier LONG_IDENTIFIER = 
    new ClassIdentifier(LONG_WRAPPER_NAME);
  
  /**
   * The Long(long) constructor identifier
   */
  private final static MethodIdentifier LONG_CONSTRUCTOR = 
    new MethodIdentifier
    (LONG_WRAPPER_NAME, "<init>", VOID_NAME, new String[] { LONG_NAME });
  
  /**
   * The class "java.lang.Float" identifier
   */
  private final static ClassIdentifier FLOAT_IDENTIFIER = 
    new ClassIdentifier(FLOAT_WRAPPER_NAME);
  
  /**
   * The Float(float) constructor identifier
   */
  private final static MethodIdentifier FLOAT_CONSTRUCTOR = 
    new MethodIdentifier
    (FLOAT_WRAPPER_NAME, "<init>", VOID_NAME, new String[] { FLOAT_NAME });
  
  /**
   * The class "java.lang.Double" identifier
   */
  private final static ClassIdentifier DOUBLE_IDENTIFIER = 
    new ClassIdentifier(DOUBLE_WRAPPER_NAME);
  
  /**
   * The Double(double) constructor identifier
   */
  private final static MethodIdentifier DOUBLE_CONSTRUCTOR = 
    new MethodIdentifier
    (DOUBLE_WRAPPER_NAME, "<init>", VOID_NAME, new String[] { DOUBLE_NAME });
  
  /**
   * The class "java.lang.Byte" identifier
   */
  private final static ClassIdentifier BYTE_IDENTIFIER = 
    new ClassIdentifier(BYTE_WRAPPER_NAME);
  
  /**
   * The Byte(byte) constructor identifier
   */
  private final static MethodIdentifier BYTE_CONSTRUCTOR = 
    new MethodIdentifier
    (BYTE_WRAPPER_NAME, "<init>", VOID_NAME, new String[] { BYTE_NAME });
  
  /**
   * The class "java.lang.Short" identifier
   */
  private final static ClassIdentifier SHORT_IDENTIFIER = 
    new ClassIdentifier(SHORT_WRAPPER_NAME);
  
  /**
   * The Short(short) constructor identifier
   */
  private final static MethodIdentifier SHORT_CONSTRUCTOR = 
    new MethodIdentifier
    (SHORT_WRAPPER_NAME, "<init>", VOID_NAME, new String[] { SHORT_NAME });
  
  /**
   * The class "java.lang.Object" identifier
   */
  private final static ClassIdentifier OBJECT_IDENTIFIER = 
    new ClassIdentifier(OBJECT_NAME);
  
  /**
   * The class "java.lang.Number" identifier
   */
  private final static ClassIdentifier NUMBER_IDENTIFIER = 
    new ClassIdentifier(NUMBER_NAME);
  
  /**
   * The identifier for Number.booleanValue()
   */
  private final static MethodIdentifier BOOLEAN_BOOLEAN_VALUE_METHOD =
    new MethodIdentifier(BOOLEAN_WRAPPER_NAME, "booleanValue",
                         BOOLEAN_NAME, new String[0]);
  
  /**
   * The identifier for Number.charValue()
   */
  private final static MethodIdentifier CHARACTER_CHAR_VALUE_METHOD =
    new MethodIdentifier(CHAR_WRAPPER_NAME, "charValue", CHAR_NAME, new String[0]);
  
  /**
   * The identifier for Number.intValue()
   */
  private final static MethodIdentifier NUMBER_INT_VALUE_METHOD =
    new MethodIdentifier(NUMBER_NAME, "intValue", INT_NAME, new String[0]);
  
  /**
   * The identifier for Number.longValue()
   */
  private final static MethodIdentifier NUMBER_LONG_VALUE_METHOD =
    new MethodIdentifier(NUMBER_NAME, "longValue", LONG_NAME, new String[0]);
  
  /**
   * The identifier for Number.floatValue()
   */
  private final static MethodIdentifier NUMBER_FLOAT_VALUE_METHOD =
    new MethodIdentifier(NUMBER_NAME, "floatValue", FLOAT_NAME, new String[0]);
  
  /**
   * The identifier for Number.doubleValue()
   */
  private final static MethodIdentifier NUMBER_DOUBLE_VALUE_METHOD =
    new MethodIdentifier(NUMBER_NAME, "doubleValue", DOUBLE_NAME, new String[0]);
  
  /**
   * The identifier for Number.byteValue()
   */
  private final static MethodIdentifier NUMBER_BYTE_VALUE_METHOD =
    new MethodIdentifier(NUMBER_NAME, "byteValue", BYTE_NAME, new String[0]);
  
  /**
   * The identifier for Number.shortValue()
   */
  private final static MethodIdentifier NUMBER_SHORT_VALUE_METHOD =
    new MethodIdentifier(NUMBER_NAME, "shortValue", SHORT_NAME, new String[0]);
  
  /**
   * The name of the class to build
   */
  private String name;
  
  /**
   * The name of the superclass of the class to build
   */
  private String superName;
  
  /**
   * Whether the class to build is static
   */
  private boolean isStaticClass;
  
  /**
   * The identifier of the method called from the generated methods
   */
  private MethodIdentifier interpreterMethod;
  
  /**
   * The identifier of the method called from the generated constructors
   */
  private MethodIdentifier interpretArgumentsMethod;
  
  /**
   * The identifier for ThrownException.getException()
   */
  private MethodIdentifier thrownExceptionMethod;
  
  /**
   * The name of the thrown exception
   */
  private String thrownExceptionName;
  
  /**
   * The InnerClassAttribute
   */
  private InnerClassesAttribute innerClassesAttribute;
  
  /**
   * The classloader identifier
   */
  private String classLoaderId;
  
  /**
   * Creates a new class factory for an innerclass
   * @param af     the access flags
   * @param name   the name of the class to create
   * @param sname  the name of the superclass
   * @param interp the class of the interpreter used to evaluate the body of
   *               the methods.
   *               It must implement methods with the following signatures:<br>
   *               <code>public static Object[] interpretArguments
   *                     (String key, Object[] args)</code><br>
   *               <code>public static Object invokeMethod
   *                     (String key, Object obj, Object[] args)</code>
   * @param except the class of the exception used to manage exception throwing.
   *               It must implement a method with the following signature:<br>
   *               <code>public Throwable getException()</code>.
   * @param clid   the class loader identifier
   */
  public ClassFactory(int     af,
                      String  name,
                      String  sname,
                      Class   interp,
                      Class   except,
                      String  clid) {
    super(name, sname);
    this.name = name;
    superName = sname;
    classLoaderId = clid;
    
    isStaticClass = (af & Modifier.STATIC) != 0;
    int taf = af & ~Modifier.PRIVATE
      & ~Modifier.PROTECTED
      & ~Modifier.STATIC;
    setAccessFlags(taf | 0x20 /* SUPER */ | (isStaticClass ? Modifier.PUBLIC : 0));
    
    String interpClassName = interp.getName();
    
    interpreterMethod =
      new MethodIdentifier
      (JVMUtilities.getName(interpClassName),
       "invokeMethod",
       JVMUtilities.getReturnTypeName("java.lang.Object"),
       new String[] {
      JVMUtilities.getParameterTypeName("java.lang.String"),
        JVMUtilities.getParameterTypeName("java.lang.Object"),
        JVMUtilities.getParameterTypeName("java.lang.Object[]") });
      
      interpretArgumentsMethod =
        new MethodIdentifier
        (JVMUtilities.getName(interpClassName),
         "interpretArguments",
         JVMUtilities.getReturnTypeName("java.lang.Object[]"),
         new String[] {
        JVMUtilities.getParameterTypeName("java.lang.String"),
          JVMUtilities.getParameterTypeName("java.lang.Object[]") });
        
        thrownExceptionName = except.getName();
        thrownExceptionMethod =
          new MethodIdentifier
          (JVMUtilities.getName(thrownExceptionName),
           "getException",
           JVMUtilities.getReturnTypeName("java.lang.Exception"),
           new String[0]);
  }
  
  /**
   * Computes a method identifier. It starts with the name of
   * the class followed by a '#'.
   * @param cname  the name of the class
   * @param mname  the name of the method
   * @param pnames the names of the parameter classes
   * @param clid   the classloader identifier
   */
  public static String getMethodIdentifier(String       cname,
                                           String       mname,
                                           String[]     pnames,
                                           String       clid) {
    String result = cname + "#" + mname + "(";
    if (pnames.length > 0) {
      result += pnames[0];
    }
    for (int i = 1; i < pnames.length; i++) {
      result += "," + pnames[i];
    }
    return result + ")" + clid;
  }
  
  /**
   * Creates the class initializer.
   * @return the method identifier
   */
  public String createClassInitializer() {
    try {
      MethodInfo mi = createMethod("void", "<clinit>", new String[0]);
      String result = getMethodIdentifier(name,
                                          "<clinit>",
                                          new String[0],
                                          classLoaderId);
      
      mi.setAccessFlags(Modifier.STATIC);
      
      ConstantPool          cp        = getConstantPool();
      ByteArrayOutputStream bytes     = new ByteArrayOutputStream();
      DataOutputStream      data      = new DataOutputStream(bytes);
      MethodIdentifier      mid;
      
      // Params array creation /////////////////////////////////////////////
      
      /* Object[] t = new Object[<pt.length>]; */
      
      //   Put 0 on the stack
      bipush(0, data);
      
      // Create the array (anewarray <java/lang/Object>)
      data.writeByte(0xBD);
      short s = cp.put(OBJECT_IDENTIFIER);
      data.writeShort(s);
      
      // Store the result in the local variable 0
      astore(0, data);
      
      // Method invocation ////////////////////////////////////////////////
      
      // Interpreter.invokeMethod(class+name+signature, null, t);
      
      // ldc <class+name+sig>
      s = cp.put(new ConstantString(result));
      if (s > 256) {
        data.writeByte(0x13);
        data.writeShort(s);
      } else {
        data.writeByte(0x12);
        data.writeByte(s);
      }
      
      // aconst_null
      data.writeByte(0x01);
      
      // loads the table
      aload(0, data);
      
      // invokestatic Interpreter.invokeMethod
      data.writeByte(0xB8);
      data.writeShort(cp.put(interpreterMethod));
      
      // return
      data.writeByte(0xB1);
      
      CodeAttribute ca = mi.createCodeAttribute();
      ca.setCode(bytes.toByteArray(), (short)1, (short)6);
      
      return result;
    } catch (IOException e) {
      // Should never append
      e.printStackTrace();     
    }
    return null;
  }
  
  /**
   * Adds a constructor to the class. The body is automatically
   * generated.
   * @param af  the access flags
   * @param pt  the parameters types
   * @param ex  the exceptions thrown
   * @param sup the name of the super constructor
   * @param st  the initialization method parameter types
   * @return the constructor identifier
   */
  public String addConstructor(int af, String[] pt, String[] ex,
                               String sup, String[] st) {
    try {
      String result = getMethodIdentifier(name, "<init>", pt, classLoaderId);
      
      MethodInfo mi = createMethod("void", "<init>", pt);
      mi.setAccessFlags(af);
      
      ConstantPool          cp        = getConstantPool();
      ByteArrayOutputStream bytes     = new ByteArrayOutputStream();
      DataOutputStream      data      = new DataOutputStream(bytes);
      short                 maxStack  = (short)6;
      short                 maxLocals = (short)3;
      
      // Compute the max locals
      for (int i = 0; i < pt.length; i++) {
        String s = pt[i];
        if (s.equals("long") || s.equals("double")) {
          maxLocals += 2;
        } else {
          maxLocals += 1;
        }
      }
      short variablesStart = maxLocals;
      for (int i = 0; i < st.length; i++) {
        String s = st[i];
        if (s.equals("long") || s.equals("double")) {
          maxLocals += 2;
          maxStack  += 2;
        } else {
          maxLocals += 1;
          maxStack  += 1;
        }
      }     
      
      // Params array creation /////////////////////////////////////////////
      
      /* Object[] t = new Object[<pt.length>]; */
      
      //   Put <pt.length> on the stack
      bipush(pt.length, data);
      
      // Create the array (anewarray <java/lang/Object>)
      data.writeByte(0xBD);
      short s = cp.put(OBJECT_IDENTIFIER);
      data.writeShort(s);
      
      // Store the result in the local variable <variablesStart - 1>
      if (variablesStart < 5) {
        // astore_<variablesStart - 1>
        data.writeByte(0x4B + variablesStart-1);
      } else {
        // astore <variablesStart - 1>
        data.writeByte(0x3A);
        data.writeByte(variablesStart-1);
      }
      
      // Fill params array //////////////////////////////////////////////////
      
      /* t[0] = param1;
       t[1] = param2;
       ...             */
      
      int currentLocal = 1;
      for (int i = 0; i < pt.length; i++) {
        String a = pt[i];
        
        // Load the array reference
        aload(variablesStart - 1, data);
        
        // Load the array index
        bipush(i, data);
        
        // Load the argument
        if (a.equals("int")) {
          loadInt(currentLocal, data, cp);
          
        } else if (a.equals("long")) {
          loadLong(currentLocal, data, cp);
          currentLocal++;
          
        } else if (a.equals("float")) {
          loadFloat(currentLocal, data, cp);
          
        } else if (a.equals("double")) {
          loadDouble(currentLocal, data, cp);
          currentLocal++;
          
        } else if (a.equals("byte")) {
          loadByte(currentLocal, data, cp);
          
        } else if (a.equals("short")) {
          loadShort(currentLocal, data, cp);
          
        } else if (a.equals("boolean")) {
          loadBoolean(currentLocal, data, cp);
          
        } else if (a.equals("char")) {
          loadChar(currentLocal, data, cp);
          
        } else {
          aload(currentLocal, data);
        }
        
        // Put the argument in the array (aastore)
        data.writeByte(0x53);
        currentLocal++;
      }
      
      short tryStart = (short)data.size();
      
      // Method invocation ////////////////////////////////////////////////
      
      // Interpreter.interpretArguments(class+name+signature, t);
      
      // ldc <class+name+sig>
      s = cp.put(new ConstantString(result));
      if (s > 256) {
        data.writeByte(0x13);
        data.writeShort(s);
      } else {
        data.writeByte(0x12);
        data.writeByte(s);
      }
      
      // loads the table
      aload(variablesStart - 1, data);
      
      // invokestatic Interpreter.interpretArguments
      data.writeByte(0xB8);
      data.writeShort(cp.put(interpretArgumentsMethod));
      
      // Store the result in the local variable <variablesStart-2>
      astore(variablesStart - 2, data);
      
      // Result management ///////////////////////////////////////////////
      
      currentLocal = variablesStart;
      
      // Unwrap each element of the array and store it in a local variable
      for (int i = 0; i < st.length; i++) {
        String a = st[i];
        
        // Load the array reference
        aload(variablesStart - 2, data);
        
        // Load the array index
        bipush(i, data);
        
        // aaload
        data.writeByte(0x32);
        
        if (a.equals("int")) {
          // checkcast java.lang.Number
          data.writeByte(0xC0);
          data.writeShort(cp.put(NUMBER_IDENTIFIER));
          
          // invokevirtual java/lang/Number.intValue:()I
          data.writeByte(0xB6);
          data.writeShort(cp.put(NUMBER_INT_VALUE_METHOD));
          
          if (currentLocal < 4) {
            // istore_<currentLocal>
            data.writeByte(0x3B + currentLocal);
          } else {
            // istore <currentLocal>
            data.writeByte(0x36);
            data.writeByte(currentLocal);
          }
          
        } else if (a.equals("long")) {
          // checkcast java.lang.Number
          data.writeByte(0xC0);
          data.writeShort(cp.put(NUMBER_IDENTIFIER));
          
          // invokevirtual java/lang/Number.longValue:()J
          data.writeByte(0xB6);
          data.writeShort(cp.put(NUMBER_LONG_VALUE_METHOD));
          
          if (currentLocal < 4) {
            // lstore_<currentLocal>
            data.writeByte(0x3F + currentLocal);
          } else {
            // lstore <currentLocal>
            data.writeByte(0x37);
            data.writeByte(currentLocal);
          }
          currentLocal++;
          
        } else if (a.equals("float")) {
          // checkcast java.lang.Number
          data.writeByte(0xC0);
          data.writeShort(cp.put(NUMBER_IDENTIFIER));
          
          // invokevirtual java/lang/Number.floatValue:()F
          data.writeByte(0xB6);
          data.writeShort(cp.put(NUMBER_FLOAT_VALUE_METHOD));
          
          if (currentLocal < 4) {
            // fstore_<currentLocal>
            data.writeByte(0x43 + currentLocal);
          } else {
            // fstore <currentLocal>
            data.writeByte(0x38);
            data.writeByte(currentLocal);
          }
          
        } else if (a.equals("double")) {
          // checkcast java.lang.Number
          data.writeByte(0xC0);
          data.writeShort(cp.put(NUMBER_IDENTIFIER));
          
          // invokevirtual java/lang/Number.doubleValue:()D
          data.writeByte(0xB6);
          data.writeShort(cp.put(NUMBER_DOUBLE_VALUE_METHOD));
          
          if (currentLocal < 4) {
            // dstore_<currentLocal>
            data.writeByte(0x47 + currentLocal);
          } else {
            // dstore <currentLocal>
            data.writeByte(0x39);
            data.writeByte(currentLocal);
          }
          currentLocal++;
          
        } else if (a.equals("byte")) {
          // checkcast java.lang.Number
          data.writeByte(0xC0);
          data.writeShort(cp.put(NUMBER_IDENTIFIER));
          
          // invokevirtual java/lang/Number.byteValue:()B
          data.writeByte(0xB6);
          data.writeShort(cp.put(NUMBER_BYTE_VALUE_METHOD));
          
          if (currentLocal < 4) {
            // istore_<currentLocal>
            data.writeByte(0x3B + currentLocal);
          } else {
            // istore <currentLocal>
            data.writeByte(0x36);
            data.writeByte(currentLocal);
          }
          
        } else if (a.equals("short")) {
          // checkcast java.lang.Number
          data.writeByte(0xC0);
          data.writeShort(cp.put(NUMBER_IDENTIFIER));
          
          // invokevirtual java/lang/Number.shortValue:()S
          data.writeByte(0xB6);
          data.writeShort(cp.put(NUMBER_SHORT_VALUE_METHOD));
          
          if (currentLocal < 4) {
            // istore_<currentLocal>
            data.writeByte(0x3B + currentLocal);
          } else {
            // istore <currentLocal>
            data.writeByte(0x36);
            data.writeByte(currentLocal);
          }
          
        } else if (a.equals("boolean")) {
          // checkcast java.lang.Boolean
          data.writeByte(0xC0);
          data.writeShort(cp.put(BOOLEAN_IDENTIFIER));
          
          // invokevirtual java/lang/Boolean.booleanValue:()Z
          data.writeByte(0xB6);
          data.writeShort(cp.put(BOOLEAN_BOOLEAN_VALUE_METHOD));
          
          if (currentLocal < 4) {
            // istore_<currentLocal>
            data.writeByte(0x3B + currentLocal);
          } else {
            // istore <currentLocal>
            data.writeByte(0x36);
            data.writeByte(currentLocal);
          }
          
        } else if (a.equals("char")) {
          // checkcast java.lang.Character
          data.writeByte(0xC0);
          data.writeShort(cp.put(CHARACTER_IDENTIFIER));
          
          // invokevirtual java/lang/Character.charValue:()Z
          data.writeByte(0xB6);
          data.writeShort(cp.put(CHARACTER_CHAR_VALUE_METHOD));
          
          if (currentLocal < 4) {
            // istore_<currentLocal>
            data.writeByte(0x3B + currentLocal);
          } else {
            // istore <currentLocal>
            data.writeByte(0x36);
            data.writeByte(currentLocal);
          }
          
        } else {
          astore(currentLocal, data);
        }
        
        currentLocal++;
      }
      
      // Constructor invocation //////////////////////////////////////////////
      
      // aload_0
      data.writeByte(0x2A);
      
      currentLocal = variablesStart;
      
      // Push each stored variable on the stack
      for (int i = 0; i < st.length; i++) {
        String a = st[i];
        
        if (a.equals("int")) {
          iload(currentLocal, data);
        } else if (a.equals("long")) {
          if (currentLocal < 4) {
            // lload_<currentLocal>
            data.writeByte(0x1E + currentLocal);
          } else {
            // lload <currentLocal>
            data.writeByte(0x16);
            data.writeByte(currentLocal);
          }      
          currentLocal++;
          
        } else if (a.equals("float")) {
          if (currentLocal < 4) {
            // fload_<currentLocal>
            data.writeByte(0x22 + currentLocal);
          } else {
            // fload <currentLocal>
            data.writeByte(0x17);
            data.writeByte(currentLocal);
          }      
          
        } else if (a.equals("double")) {
          if (currentLocal < 4) {
            // dload_<currentLocal>
            data.writeByte(0x26 + currentLocal);
          } else {
            // dload <currentLocal>
            data.writeByte(0x18);
            data.writeByte(currentLocal);
          }
          currentLocal++;
          
        } else if (a.equals("byte")) {
          iload(currentLocal, data);
          
        } else if (a.equals("short")) {
          iload(currentLocal, data);
          
        } else if (a.equals("boolean")) {
          iload(currentLocal, data);
          
        } else if (a.equals("char")) {
          iload(currentLocal, data);
          
        } else {
          aload(currentLocal, data);
          
          // checkcast <rt>
          data.writeByte(0xC0);
          data.writeShort
            (cp.put(new ClassIdentifier(JVMUtilities.getName(st[i]))));
        }
        
        currentLocal++;
      }
      
      data.writeByte(0xB7);
      
      for (int i = 0; i < st.length; i++) {
        st[i] = JVMUtilities.getParameterTypeName(st[i]);
      }
      
      // invokespecial sup
      MethodIdentifier mid = new MethodIdentifier
        (JVMUtilities.getName(sup),
         "<init>",
         JVMUtilities.getName("void"),
         st);
      
      data.writeShort(cp.put(mid));
      
      // Interpreter.invokeMethod(class+name+signature, this, t); ////////////
      
      // ldc <class+name+sig>
      s = cp.put(new ConstantString(result));
      if (s > 256) {
        data.writeByte(0x13);
        data.writeShort(s);
      } else {
        data.writeByte(0x12);
        data.writeByte(s);
      }
      
      // aload_0
      data.writeByte(0x2A);
      
      // loads the table
      aload(variablesStart - 1, data);
      
      // invokestatic Interpreter.invokeMethod
      data.writeByte(0xB8);
      data.writeShort(cp.put(interpreterMethod));
      
      // return
      data.writeByte(0xB1);
      
      // Exceptions ////////////////////////////////////////////////////////
      
      short tryEnd = (short)data.size();
      
      // Create the handler bytecode
      if (ex.length > 0) {
        maxLocals += 2;
        
        // catch (ThrownException e)
        astore(maxLocals - 2, data);
        aload(maxLocals - 2, data);
        
        // Exception ex = e.getException();
        
        // invokevirtual e.getException()
        data.writeByte(0xB6);
        data.writeShort(cp.put(thrownExceptionMethod));
        
        astore(maxLocals - 1, data);
        
        // if (ex instanceof <ex[i]>) throw (<ex[i]>)ex;
        for (int i = 0; i < ex.length; i++) {
          // instanceof <ex[i]>
          aload(maxLocals - 1, data);
          data.writeByte(0xC1);
          s = cp.put(new ClassIdentifier(JVMUtilities.getName(ex[i])));
          data.writeShort(s);
          
          // ifeq ...
          data.writeByte(0x99);
          if (maxLocals < 5) {
            // ... 8
            data.writeShort(0x08);
          } else {
            // ... 9
            data.writeShort(0x09);
          }
          
          aload(maxLocals - 1, data);
          
          // checkcast <ex[i]>
          data.writeByte(0xC0);
          data.writeShort(s);
          
          // athrow
          data.writeByte(0xBF);
        }
        
        aload(maxLocals - 2, data);
        
        // athrow
        data.writeByte(0xBF);
      }
      
      CodeAttribute ca = mi.createCodeAttribute();
      ca.setCode(bytes.toByteArray(), maxLocals, maxStack);
      
      // Fill the exception table
      if (ex.length > 0) {
        ca.addExceptionTableEntry
          (tryStart, tryEnd, tryEnd, thrownExceptionName);
      }
      
      // Adds the exceptions to the exceptions attribute
      if (ex.length > 0) {
        ExceptionsAttribute ea = mi.createExceptionsAttribute();
        for (int i = 0; i < ex.length; i++) {
          ea.addException(ex[i]);
        }
      }
      
      return result;
    } catch (IOException e) {
      // Should never append
      e.printStackTrace();     
    }
    return null;
  }
  
  /**
   * Adds a field to the class.
   * @param af the access flags
   * @param ft the field type
   * @param nm the field name
   */
  public void addField(int af, String ft, String nm) {
    FieldInfo fi = createField(ft, nm);
    fi.setAccessFlags(af);
  }
  
  /**
   * Adds a constant int field to the class.
   * @param af the access flags
   * @param ft the field type
   * @param nm the field name
   */
  public void addConstantIntField(int af, String ft, String nm, Integer v) {
    FieldInfo fi = createField(ft, nm);
    fi.setAccessFlags(af);
    fi.setConstantValueAttribute(v);
  }
  
  /**
   * Adds a constant long field to the class.
   * @param af the access flags
   * @param ft the field type
   * @param nm the field name
   */
  public void addConstantLongField(int af, String ft, String nm, Long v) {
    FieldInfo fi = createField(ft, nm);
    fi.setAccessFlags(af);
    fi.setConstantValueAttribute(v);
  }
  
  /**
   * Adds a constant float field to the class.
   * @param af the access flags
   * @param ft the field type
   * @param nm the field name
   */
  public void addConstantFloatField(int af, String ft, String nm, Float v) {
    FieldInfo fi = createField(ft, nm);
    fi.setAccessFlags(af);
    fi.setConstantValueAttribute(v);
  }
  
  /**
   * Adds a constant double field to the class.
   * @param af the access flags
   * @param ft the field type
   * @param nm the field name
   */
  public void addConstantDoubleField(int af, String ft, String nm, Double v) {
    FieldInfo fi = createField(ft, nm);
    fi.setAccessFlags(af);
    fi.setConstantValueAttribute(v);
  }
  
  /**
   * Adds a constant boolean field to the class.
   * @param af the access flags
   * @param ft the field type
   * @param nm the field name
   */
  public void addConstantBooleanField(int af, String ft, String nm, Boolean v) {
    FieldInfo fi = createField(ft, nm);
    fi.setAccessFlags(af);
    fi.setConstantValueAttribute(new Integer(v.booleanValue() ? 1 : 0));
  }
  
  /**
   * Adds a constant string field to the class.
   * @param af the access flags
   * @param ft the field type
   * @param nm the field name
   */
  public void addConstantStringField(int af, String ft, String nm, String v) {
    FieldInfo fi = createField(ft, nm);
    fi.setAccessFlags(af);
    fi.setConstantValueAttribute(v);
  }
  
  /**
   * Adds an InnerClasses entry
   */
  public InnerClassesEntry addInnerClassesEntry() {
    if (innerClassesAttribute == null) {
      innerClassesAttribute = new InnerClassesAttribute(getConstantPool());
      setInnerClassesAttribute(innerClassesAttribute);
    }
    return innerClassesAttribute.addInnerClassesEntry();
  }
  
  /**
   * Adds a method that calls the supermethod of the given method
   * @param af the access flags
   * @param rt the return type
   * @param nm the name of the method
   * @param pt the parameters types
   * @param ex the exceptions thrown
   */
  public void addSuperMethodAccessor(int af,
                                     String rt,
                                     String nm,
                                     String[] pt,
                                     String[] ex) {
    try {
      MethodInfo mi = createMethod(rt, "super$" + nm, pt);
      mi.setAccessFlags(Modifier.PUBLIC);
      
      ConstantPool          cp        = getConstantPool();
      ByteArrayOutputStream bytes     = new ByteArrayOutputStream();
      DataOutputStream      data      = new DataOutputStream(bytes);
      short                 maxStack;
      boolean               isStatic = Modifier.isStatic(af);
      short                 maxLocals = (short)(isStatic ? 1 : 2);
      MethodIdentifier      mid;
      
      // Compute the max locals
      for (int i = 0; i < pt.length; i++) {
        String s = pt[i];
        if (s.equals("long") || s.equals("double")) {
          maxLocals += 2;
        } else {
          maxLocals += 1;
        }
      }
      maxStack = maxLocals;
      
      int currentLocal = 0;
      if (!isStatic) {
        // aload_0
        data.writeByte(0x2A);
        currentLocal = 1;
      }
      
      // Push each local variable on the stack
      for (int i = 0; i < pt.length; i++) {
        String a = pt[i];
        
        if (a.equals("int")) {
          iload(currentLocal, data);
        } else if (a.equals("long")) {
          if (currentLocal < 4) {
            // lload_<currentLocal>
            data.writeByte(0x1E + currentLocal);
          } else {
            // lload <currentLocal>
            data.writeByte(0x16);
            data.writeByte(currentLocal);
          }      
          currentLocal++;
          
        } else if (a.equals("float")) {
          if (currentLocal < 4) {
            // fload_<currentLocal>
            data.writeByte(0x22 + currentLocal);
          } else {
            // fload <currentLocal>
            data.writeByte(0x17);
            data.writeByte(currentLocal);
          }      
          
        } else if (a.equals("double")) {
          if (currentLocal < 4) {
            // dload_<currentLocal>
            data.writeByte(0x26 + currentLocal);
          } else {
            // dload <currentLocal>
            data.writeByte(0x18);
            data.writeByte(currentLocal);
          }
          currentLocal++;
          
        } else if (a.equals("byte")) {
          iload(currentLocal, data);
          
        } else if (a.equals("short")) {
          iload(currentLocal, data);
          
        } else if (a.equals("boolean")) {
          iload(currentLocal, data);
          
        } else if (a.equals("char")) {
          iload(currentLocal, data);
          
        } else {
          aload(currentLocal, data);
          
          // checkcast <pt[i]>
          data.writeByte(0xC0);
          data.writeShort
            (cp.put(new ClassIdentifier(JVMUtilities.getName(pt[i]))));
        }
        
        currentLocal++;
      }
      
      String[] st = new String[pt.length];
      for (int i = 0; i < pt.length; i++) {
        st[i] = JVMUtilities.getParameterTypeName(pt[i]);
      }
      
      mid = new MethodIdentifier
        (JVMUtilities.getName(superName),
         nm,
         JVMUtilities.getReturnTypeName(rt),
         st);
      
      if (isStatic) {
        // invokestatic method
        data.writeByte(0xB8);
      } else {
        // invokespecial method
        data.writeByte(0xB7);
      }
      data.writeShort(cp.put(mid));
      
      // Return ////////////////////////////////////////////////////////////
      
      if (rt.equals("void")) {
        // return
        data.writeByte(0xB1);
      } else if (rt.equals("int")) {
        // ireturn
        data.writeByte(0xAC);
        
      } else if (rt.equals("long")) {
        // lreturn
        data.writeByte(0xAD);
        
      } else if (rt.equals("float")) {
        // freturn
        data.writeByte(0xAE);
        
      } else if (rt.equals("double")) {
        // dreturn
        data.writeByte(0xAF);
        
      } else if (rt.equals("byte")) {
        // ireturn
        data.writeByte(0xAC);
        
      } else if (rt.equals("short")) {
        // ireturn
        data.writeByte(0xAC);
        
      } else if (rt.equals("boolean")) {
        // ireturn
        data.writeByte(0xAC);
        
      } else if (rt.equals("char")) {
        // ireturn
        data.writeByte(0xAC);
        
      } else {
        // areturn
        data.writeByte(0xB0);
      }
      
      CodeAttribute ca = mi.createCodeAttribute();
      ca.setCode(bytes.toByteArray(), maxLocals, maxStack);
      
      // Adds the exceptions to the exceptions attribute
      if (ex.length > 0) {
        ExceptionsAttribute ea = mi.createExceptionsAttribute();
        for (int i = 0; i < ex.length; i++) {
          ea.addException(ex[i]);
        }
      }
      
    } catch (IOException e) {
      // Should never append
      e.printStackTrace();      
    }
  }     
  
  /**
   * Adds a method to the class. The body is automatically
   * generated.
   * @param af the access flags
   * @param rt the return type
   * @param nm the name of the method
   * @param pt the parameters types
   * @param ex the exceptions thrown
   * @return the method identifier
   */
  public String addMethod(int af, String rt, String nm, String[] pt, String[] ex) {
    try {
      MethodInfo mi = createMethod(rt, nm, pt);
      String result = getMethodIdentifier(name, nm, pt, classLoaderId);
      
      mi.setAccessFlags(af);
      
      if (!mi.isAbstract()) {
        ConstantPool          cp        = getConstantPool();
        ByteArrayOutputStream bytes     = new ByteArrayOutputStream();
        DataOutputStream      data      = new DataOutputStream(bytes);
        short                 maxStack  = (short)6;
        short                 maxLocals = (short)((mi.isStatic()) ? 1 : 2);
        MethodIdentifier      mid;
        
        // Compute the max locals
        for (int i = 0; i < pt.length; i++) {
          String s = pt[i];
          if (s.equals("long") || s.equals("double")) {
            maxLocals += 2;
          } else {
            maxLocals += 1;
          }
        }
        
        // Params array creation /////////////////////////////////////////////
        
        /* Object[] t = new Object[<pt.length>]; */
        
        //   Put <pt.length> on the stack
        bipush(pt.length, data);
        
        // Create the array (anewarray <java/lang/Object>)
        data.writeByte(0xBD);
        short s = cp.put(OBJECT_IDENTIFIER);
        data.writeShort(s);
        
        // Store the result in the local variable #maxLocals - 1
        astore(maxLocals - 1, data);
        
        // Fill params array //////////////////////////////////////////////////
        
        /* t[0] = param1;
         t[1] = param2;
         ...             */
        
        int currentLocal = (mi.isStatic()) ? 0 : 1;
        for (int i = 0; i < pt.length; i++) {
          String a = pt[i];
          
          // Load the array reference
          aload(maxLocals - 1, data);
          
          // Push the array index
          bipush(i, data);
          
          // Load the argument
          if (a.equals("int")) {
            loadInt(currentLocal, data, cp);
            
          } else if (a.equals("long")) {
            loadLong(currentLocal, data, cp);
            currentLocal++;
            
          } else if (a.equals("float")) {
            loadFloat(currentLocal, data, cp);
            
          } else if (a.equals("double")) {
            loadDouble(currentLocal, data, cp);
            currentLocal++;
            
          } else if (a.equals("byte")) {
            loadByte(currentLocal, data, cp);
            
          } else if (a.equals("short")) {
            loadShort(currentLocal, data, cp);
            
          } else if (a.equals("boolean")) {
            loadBoolean(currentLocal, data, cp);
            
          } else if (a.equals("char")) {
            loadChar(currentLocal, data, cp);
            
          } else {
            aload(currentLocal, data);
          }
          
          // Put the argument in the array (aastore)
          data.writeByte(0x53);
          currentLocal++;
        }
        
        short tryStart = (short)data.size();
        
        // Method invocation ////////////////////////////////////////////////
        
        // Interpreter.invokeMethod(class+name+signature, this, t);
        
        // ldc <class+name+sig>
        s = cp.put(new ConstantString(result));
        if (s > 256) {
          data.writeByte(0x13);
          data.writeShort(s);
        } else {
          data.writeByte(0x12);
          data.writeByte(s);
        }
        
        if (mi.isStatic()) {
          // aconst_null
          data.writeByte(0x01);
        } else {
          // aload_0
          data.writeByte(0x2A);
        }
        
        // loads the table
        aload(maxLocals - 1, data);
        
        // invokestatic Interpreter.invokeMethod
        data.writeByte(0xB8);
        data.writeShort(cp.put(interpreterMethod));
        
        // Return ////////////////////////////////////////////////////////////
        
        if (rt.equals("void")) {
          // return
          data.writeByte(0xB1);
        } else if (rt.equals("int")) {
          // checkcast java.lang.Number
          data.writeByte(0xC0);
          data.writeShort(cp.put(NUMBER_IDENTIFIER));
          
          // invokevirtual java/lang/Number.intValue:()I
          data.writeByte(0xB6);
          data.writeShort(cp.put(NUMBER_INT_VALUE_METHOD));
          
          // ireturn
          data.writeByte(0xAC);
          
        } else if (rt.equals("long")) {
          // checkcast java.lang.Number
          data.writeByte(0xC0);
          data.writeShort(cp.put(NUMBER_IDENTIFIER));
          
          // invokevirtual java/lang/Number.longValue:()J
          data.writeByte(0xB6);
          data.writeShort(cp.put(NUMBER_LONG_VALUE_METHOD));
          
          // lreturn
          data.writeByte(0xAD);
          
        } else if (rt.equals("float")) {
          // checkcast java.lang.Number
          data.writeByte(0xC0);
          data.writeShort(cp.put(NUMBER_IDENTIFIER));
          
          // invokevirtual java/lang/Number.floatValue:()F
          data.writeByte(0xB6);
          data.writeShort(cp.put(NUMBER_FLOAT_VALUE_METHOD));
          
          // freturn
          data.writeByte(0xAE);
          
        } else if (rt.equals("double")) {
          // checkcast java.lang.Number
          data.writeByte(0xC0);
          data.writeShort(cp.put(NUMBER_IDENTIFIER));
          
          // invokevirtual java/lang/Number.doubleValue:()D
          data.writeByte(0xB6);
          data.writeShort(cp.put(NUMBER_DOUBLE_VALUE_METHOD));
          
          // dreturn
          data.writeByte(0xAF);
          
        } else if (rt.equals("byte")) {
          // checkcast java.lang.Number
          data.writeByte(0xC0);
          data.writeShort(cp.put(NUMBER_IDENTIFIER));
          
          // invokevirtual java/lang/Number.byteValue:()B
          data.writeByte(0xB6);
          data.writeShort(cp.put(NUMBER_BYTE_VALUE_METHOD));
          
          // ireturn
          data.writeByte(0xAC);
          
        } else if (rt.equals("short")) {
          // checkcast java.lang.Number
          data.writeByte(0xC0);
          data.writeShort(cp.put(NUMBER_IDENTIFIER));
          
          // invokevirtual java/lang/Number.shortValue:()S
          data.writeByte(0xB6);
          data.writeShort(cp.put(NUMBER_SHORT_VALUE_METHOD));
          
          // ireturn
          data.writeByte(0xAC);
          
        } else if (rt.equals("boolean")) {
          // checkcast java.lang.Boolean
          data.writeByte(0xC0);
          data.writeShort(cp.put(BOOLEAN_IDENTIFIER));
          
          // invokevirtual java/lang/Boolean.booleanValue:()Z
          data.writeByte(0xB6);
          data.writeShort(cp.put(BOOLEAN_BOOLEAN_VALUE_METHOD));
          
          // ireturn
          data.writeByte(0xAC);
          
        } else if (rt.equals("char")) {
          // checkcast java.lang.Character
          data.writeByte(0xC0);
          data.writeShort(cp.put(CHARACTER_IDENTIFIER));
          
          // invokevirtual java/lang/Character.charValue:()C
          data.writeByte(0xB6);
          data.writeShort(cp.put(CHARACTER_CHAR_VALUE_METHOD));
          
          // ireturn
          data.writeByte(0xAC);
          
        } else {
          // checkcast <rt>
          data.writeByte(0xC0);
          data.writeShort
            (cp.put(new ClassIdentifier(JVMUtilities.getName(rt))));
          
          // areturn
          data.writeByte(0xB0);
        }
        
        // Exceptions ////////////////////////////////////////////////////////
        
        short tryEnd = (short)data.size();
        
        // Create the handler bytecode
        if (ex.length > 0) {
          maxLocals += 2;
          
          // catch (ThrownException e)
          astore(maxLocals - 2, data);
          aload(maxLocals - 2, data);
          
          // Exception ex = e.getException();
          
          // invokevirtual e.getException()
          data.writeByte(0xB6);
          data.writeShort(cp.put(thrownExceptionMethod));
          
          astore(maxLocals - 1, data);
          
          // if (ex instanceof <ex[i]>) throw (<ex[i]>)ex;
          for (int i = 0; i < ex.length; i++) {
            // instanceof <ex[i]>
            aload(maxLocals - 1, data);
            data.writeByte(0xC1);
            s = cp.put(new ClassIdentifier(JVMUtilities.getName(ex[i])));
            data.writeShort(s);
            
            // ifeq ...
            data.writeByte(0x99);
            if (maxLocals < 5) {
              // ... 8
              data.writeShort(0x08);
            } else {
              // ... 9
              data.writeShort(0x09);
            }
            
            aload(maxLocals - 1, data);
            
            // checkcast <ex[i]>
            data.writeByte(0xC0);
            data.writeShort(s);
            
            // athrow
            data.writeByte(0xBF);
          }
          
          aload(maxLocals - 2, data);
          
          // athrow
          data.writeByte(0xBF);
        }
        
        CodeAttribute ca = mi.createCodeAttribute();
        ca.setCode(bytes.toByteArray(), maxLocals, maxStack);
        
        // Fill the exception table
        if (ex.length > 0) {
          ca.addExceptionTableEntry
            (tryStart, tryEnd, tryEnd, thrownExceptionName);
        }
      } // end if (!mi.isAbstract())
      
      // Adds the exceptions to the exceptions attribute
      if (ex.length > 0) {
        ExceptionsAttribute ea = mi.createExceptionsAttribute();
        for (int i = 0; i < ex.length; i++) {
          ea.addException(ex[i]);
        }
      }
      
      return result;
    } catch (IOException e) {
      // Should never append
      e.printStackTrace();      
    }
    return null;
  }
  
  /**
   * Returns the generated class
   */
  public byte[] getByteCode() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      write(out);
      
      /*
       File f = new File(name+"xxx.class");
       FileOutputStream fos = new FileOutputStream(f);
       fos.write(out.toByteArray());
       fos.close();
       */
      
    } catch (IOException e) {
      // Should never append
      e.printStackTrace();
    }
    return out.toByteArray();
  }
  
  /**
   * This method generates the code that loads a local variable on the stack
   * @param local  the local variable to load
   * @param data   the stream where the code is written     
   */
  private void iload(int local, DataOutputStream data) throws IOException {
    if (local < 4) {
      // iload_<local>
      data.writeByte(0x1A + local);
    } else {
      // iload <local>
      data.writeByte(0x15);
      data.writeByte(local);
    }
  }
  
  /**
   * This method generates the code that loads a local variable on the stack
   * @param local  the local variable to load
   * @param data   the stream where the code is written     
   */
  private void aload(int local, DataOutputStream data) throws IOException {
    if (local < 4) {
      // aload_<local>
      data.writeByte(0x2A + local);
    } else {
      // aload <local>
      data.writeByte(0x19);
      data.writeByte(local);
    }
  }
  
  /**
   * This method generates the code that stores a local variable from the stack
   * @param local  the local variable to store
   * @param data   the stream where the code is written     
   */
  private void astore(int local, DataOutputStream data) throws IOException {
    if (local < 4) {
      // aload_<local>
      data.writeByte(0x4B + local);
    } else {
      // aload <local>
      data.writeByte(0x3A);
      data.writeByte(local);
    }
  }
  
  /**
   * This method generates the code that push a byte constant on the stack
   * @param cst  the local variable to store
   * @param data the stream where the code is written     
   */
  private void bipush(int cst, DataOutputStream data) throws IOException {
    if (cst < 6) {
      // iconst_<i>
      data.writeByte(0x03 + cst);
    } else {
      // bipush <i>
      data.writeByte(0x10);
      data.writeByte(cst);
    }
  }
  
  /**
   * This method generates the code that puts an int argument in the
   * table passed to 'Interpreter.invokeMethod'
   * @param currentLocal the current parameter
   * @param data         the stream where the code is written
   * @param cp           the constant pool
   */
  private void loadInt(int currentLocal, DataOutputStream data, ConstantPool cp)
    throws IOException {
    // Create a new Integer (new <java/lang/Integer>)
    data.writeByte(0xBB);
    data.writeShort(cp.put(INTEGER_IDENTIFIER));
    
    // duplicate the top operand (dup)
    data.writeByte(0x59);
    
    // Load the argument
    if (currentLocal < 4) {
      // iload_<i>
      data.writeByte(0x1A + currentLocal);
    } else {
      // iload <i>
      data.writeByte(0x15);
      data.writeByte(currentLocal);
    }
    
    // Calls <init> (invokespecial <java/lang/Integer.<init>:(I)V>)
    data.writeByte(0xB7);
    data.writeShort(cp.put(INTEGER_CONSTRUCTOR));
  }
  
  /**
   * This method generates the code that puts a long argument in the
   * table passed to 'Interpreter.invokeMethod'
   * @param currentLocal the current parameter
   * @param data         the stream where the code is written
   * @param cp           the constant pool
   */
  private void loadLong(int currentLocal, DataOutputStream data, ConstantPool cp)
    throws IOException {
    // Create a new Long (new <java/lang/Long>)
    data.writeByte(0xBB);
    data.writeShort(cp.put(LONG_IDENTIFIER));
    
    // duplicate the top operand (dup)
    data.writeByte(0x59);      
    
    // Load the argument
    if (currentLocal < 4) {
      // lload_<i>
      data.writeByte(0x1E + currentLocal);
    } else {
      // lload <i>
      data.writeByte(0x16);
      data.writeByte(currentLocal);
    }
    
    // Calls <init> (invokespecial <java/lang/Long.<init>:(J)V>)
    data.writeByte(0xB7);
    data.writeShort(cp.put(LONG_CONSTRUCTOR));
  }
  
  /**
   * This method generates the code that puts a float argument in the
   * table passed to 'Interpreter.invokeMethod'
   * @param currentLocal the current parameter
   * @param data         the stream where the code is written
   * @param cp           the constant pool
   */
  private void loadFloat(int currentLocal, DataOutputStream data, ConstantPool cp)
    throws IOException {
    // Create a new Float (new <java/lang/Float>)
    data.writeByte(0xBB);
    data.writeShort(cp.put(FLOAT_IDENTIFIER));
    
    // duplicate the top operand (dup)
    data.writeByte(0x59);      
    
    // Load the argument
    if (currentLocal < 4) {
      // fload_<i>
      data.writeByte(0x22 + currentLocal);
    } else {
      // fload <i>
      data.writeByte(0x17);
      data.writeByte(currentLocal);
    }
    
    // Calls <init> (invokespecial <java/lang/Float.<init>:(F)V>)
    data.writeByte(0xB7);
    data.writeShort(cp.put(FLOAT_CONSTRUCTOR));
  }
  
  /**
   * This method generates the code that puts a double argument in the
   * table passed to 'Interpreter.invokeMethod'
   * @param currentLocal the current parameter
   * @param data         the stream where the code is written
   * @param cp           the constant pool
   */
  private void loadDouble(int currentLocal, DataOutputStream data, ConstantPool cp)
    throws IOException {
    // Create a new Double (new <java/lang/Double>)
    data.writeByte(0xBB);
    data.writeShort(cp.put(DOUBLE_IDENTIFIER));
    
    // duplicate the top operand (dup)
    data.writeByte(0x59);      
    
    // Load the argument
    if (currentLocal < 4) {
      // fload_<i>
      data.writeByte(0x26 + currentLocal);
    } else {
      // fload <i>
      data.writeByte(0x18);
      data.writeByte(currentLocal);
    }
    
    // Calls <init> (invokespecial <java/lang/Double.<init>:(D)V>)
    data.writeByte(0xB7);
    data.writeShort(cp.put(DOUBLE_CONSTRUCTOR));
  }
  
  /**
   * This method generates the code that puts a byte argument in the
   * table passed to 'Interpreter.invokeMethod'
   * @param currentLocal the current parameter
   * @param data         the stream where the code is written
   * @param cp           the constant pool
   */
  private void loadByte(int currentLocal, DataOutputStream data, ConstantPool cp)
    throws IOException {
    // Create a new Byte (new <java/lang/Byte>)
    data.writeByte(0xBB);
    data.writeShort(cp.put(BYTE_IDENTIFIER));
    
    // duplicate the top operand (dup)
    data.writeByte(0x59);      
    
    // Load the argument
    if (currentLocal < 4) {
      // iload_<i>
      data.writeByte(0x1A + currentLocal);
    } else {
      // iload <i>
      data.writeByte(0x15);
      data.writeByte(currentLocal);
    }
    
    // Calls <init> (invokespecial <java/lang/Byte.<init>:(B)V>)
    data.writeByte(0xB7);
    data.writeShort(cp.put(BYTE_CONSTRUCTOR));
  }
  
  /**
   * This method generates the code that puts a short argument in the
   * table passed to 'Interpreter.invokeMethod'
   * @param currentLocal the current parameter
   * @param data         the stream where the code is written
   * @param cp           the constant pool
   */
  private void loadShort(int currentLocal, DataOutputStream data, ConstantPool cp)
    throws IOException {
    // Create a new Short (new <java/lang/Short>)
    data.writeByte(0xBB);
    data.writeShort(cp.put(SHORT_IDENTIFIER));
    
    // duplicate the top operand (dup)
    data.writeByte(0x59);      
    
    // Load the argument
    if (currentLocal < 4) {
      // iload_<i>
      data.writeByte(0x1A + currentLocal);
    } else {
      // iload <i>
      data.writeByte(0x15);
      data.writeByte(currentLocal);
    }
    
    // Calls <init> (invokespecial <java/lang/Short.<init>:(S)V>)
    data.writeByte(0xB7);
    data.writeShort(cp.put(SHORT_CONSTRUCTOR));
  }
  
  /**
   * This method generates the code that puts a boolean argument in the
   * table passed to 'Interpreter.invokeMethod'
   * @param currentLocal the current parameter
   * @param data         the stream where the code is written
   * @param cp           the constant pool
   */
  private void loadBoolean(int currentLocal, DataOutputStream data, ConstantPool cp)
    throws IOException {
    // Create a new Boolean (new <java/lang/Boolean>)
    data.writeByte(0xBB);
    data.writeShort(cp.put(BOOLEAN_IDENTIFIER));
    
    // duplicate the top operand (dup)
    data.writeByte(0x59);    
    
    // Load the argument
    if (currentLocal < 4) {
      // iload_<i>
      data.writeByte(0x1A + currentLocal);
    } else {
      // iload <i>
      data.writeByte(0x15);
      data.writeByte(currentLocal);
    }
    
    // Calls <init> (invokespecial <java/lang/Boolean.<init>:(B)V>)
    data.writeByte(0xB7);
    data.writeShort(cp.put(BOOLEAN_CONSTRUCTOR));
  }
  
  /**
   * This method generates the code that puts a char argument in the
   * table passed to 'Interpreter.invokeMethod'
   * @param currentLocal the current parameter
   * @param data         the stream where the code is written
   * @param cp           the constant pool
   */
  private void loadChar(int currentLocal, DataOutputStream data, ConstantPool cp)
    throws IOException {
    // Create a new Character (new <java/lang/Character>)
    data.writeByte(0xBB);
    data.writeShort(cp.put(CHARACTER_IDENTIFIER));
    
    // duplicate the top operand (dup)
    data.writeByte(0x59);      
    
    // Load the argument
    if (currentLocal < 4) {
      // iload_<i>
      data.writeByte(0x1A + currentLocal);
    } else {
      // iload <i>
      data.writeByte(0x15);
      data.writeByte(currentLocal);
    }
    
    // Calls <init> (invokespecial <java/lang/Character.<init>:(C)V>)
    data.writeByte(0xB7);
    data.writeShort(cp.put(CHARACTER_CONSTRUCTOR));
  }
}
