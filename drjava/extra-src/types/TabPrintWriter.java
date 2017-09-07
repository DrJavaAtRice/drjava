package edu.rice.cs.drjava.model.repl.types;


/**
 * An extension of PrintWriter to support indenting levels.
 */
public class TabPrintWriter extends java.io.PrintWriter {
  private final int _tabSize;
  private int _numSpaces;
  
  public TabPrintWriter(java.io.Writer writer, int tabSize) {
    super(writer);
    _tabSize = tabSize;
    _numSpaces = 0;
  }
  
  /** ups indent for any future new lines. */
  public void indent() { _numSpaces += _tabSize; }
  public void unindent() { _numSpaces -= _tabSize; }

  public void startLine(java.lang.Object s) {
    startLine();
    print(s);
  }
  
  public void startLine() {
    println();
    for (int i = 0; i < _numSpaces; i++) { print(' '); }
  }
  
  public void printEscaped(java.lang.Object o) { printEscaped(o.toString()); }
  
  /** Print a string in Java source-compatible escaped form.  All control characters
    * (including line breaks) and quoting punctuation are escaped with a backslash.
    */
  public void printEscaped(java.lang.String s) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\b': print("\\b"); break;
        case '\t': print("\\t"); break;
        case '\n': print("\\n"); break;
        case '\f': print("\\f"); break;
        case '\r': print("\\r"); break;
        case '\"': print("\\\""); break;
        case '\'': print("\\\'"); break;
        case '\\': print("\\\\"); break;
        default:
          if (c < ' ' || c == '\u007f') {
            print('\\');
            // must use 3 digits so that unescaping doesn't consume too many chars ("\12" vs. "\0012")
            java.lang.String num = java.lang.Integer.toOctalString(c);
            while (num.length() < 3) num = "0" + num;
            print(num);
          }
          else { print(c); }
          break;
      }
    }
  }

  /** Conditionally print the serialzed form of the given object. */
  public void printPossiblyEscaped(java.lang.String s, boolean lossless) {
    if (lossless) {
      print("\"");
      printEscaped(s);
      print("\"");
    } else {
      print(s);
    }
  }
  
  /** Print the serialized form of the given object as a hexadecimal number.
    * @throws RuntimeException  If the object is not serializable.
    */
  public void printSerialized(java.lang.Object o) {
    java.io.ByteArrayOutputStream bs = new java.io.ByteArrayOutputStream();
    try {
      java.io.ObjectOutputStream objOut = new java.io.ObjectOutputStream(bs);
      try { objOut.writeObject(o); }
      finally { objOut.close(); }
    }
    catch (java.io.IOException e) { throw new java.lang.RuntimeException(e); }
    printBytes(bs.toByteArray());
  }

  /** Conditionally print the serialzed form of the given object. */
  public void printPossiblySerialized(java.lang.Object o, boolean lossless) {
    if (lossless) {
      printSerialized(o);
      print(" ");
      printEscaped(o);
    } else {
      print(o);
    }
  }
  
  private void printBytes(byte[] bs) {
    for (byte b : bs) {
      int unsigned = ((int)b) & 0xff;
      java.lang.String num = java.lang.Integer.toHexString(unsigned);
      if (num.length() == 1) print("0");
      print(num);
    }
  }
  
  public void startObject(String name) {
    print(name);
    indent();
  }
}