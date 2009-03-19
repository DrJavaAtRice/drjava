package koala.dynamicjava.tree;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import koala.dynamicjava.tree.visitor.Visitor;

public class ModifierSet extends Node {
  
  private final Set<Modifier> flags;
  private final List<Annotation> annotations;
  
  public ModifierSet(Set<Modifier> flgs, List<Annotation> annots) {
    this(flgs, annots, SourceInfo.NONE);
  }
  
  public ModifierSet(Set<Modifier> flgs, List<Annotation> annots, SourceInfo si) {
    super(si);
    flags = flgs;
    annotations = annots;
  }

  @Override public <T> T acceptVisitor(Visitor<T> visitor) {
    return visitor.visit(this);
  }
  
  public Set<Modifier> getFlags() { return flags; }
  
  public List<Annotation> getAnnotations() { return annotations; }
  
  /**
   * Get the standard Java bit vector corresponding to {@link #getFlags}.  Optionally, some flags
   * can be "forced" on by providing them as arguments.
   */
  public int getBitVector(Modifier... forced) {
    int result = 0;
    for (Modifier m : flags) {
      result |= m.getBits();
    }
    for (Modifier m : forced) {
      result |= m.getBits();
    }
    return result;
  }
  
  public boolean isEmpty() { return flags.isEmpty() && annotations.isEmpty(); }
  
  public boolean isPublic() { return flags.contains(Modifier.PUBLIC); }
  public boolean isPrivate() { return flags.contains(Modifier.PRIVATE); }
  public boolean isProtected() { return flags.contains(Modifier.PROTECTED); }
  
  public boolean isStatic() { return flags.contains(Modifier.STATIC); }
  public boolean isFinal() { return flags.contains(Modifier.FINAL); }
  public boolean isAbstract() { return flags.contains(Modifier.ABSTRACT); }
    
  public boolean isVolatile() { return flags.contains(Modifier.VOLATILE); }
  public boolean isTransient() { return flags.contains(Modifier.TRANSIENT); }

  public boolean isSynchronized() { return flags.contains(Modifier.SYNCHRONIZED); }
  public boolean isNative() { return flags.contains(Modifier.NATIVE); }
  public boolean isStrict() { return flags.contains(Modifier.STRICT); }
  
  public boolean isInterface() { return flags.contains(Modifier.INTERFACE); }
  public boolean isAnnotation() { return flags.contains(Modifier.ANNOTATION); }
  public boolean isEnum() { return flags.contains(Modifier.ENUM); }
  public boolean isBridge() { return flags.contains(Modifier.BRIDGE); }
  public boolean isVarargs() { return flags.contains(Modifier.VARARGS); }
  public boolean isSynthetic() { return flags.contains(Modifier.SYNTHETIC); }

  public String toString() {
    return "(" + getClass().getName() + ": " + flags + ", " + annotations + ")"; 
  }
  
  
  
  public static ModifierSet make() {
    return new ModifierSet(EnumSet.noneOf(Modifier.class), new LinkedList<Annotation>());
  }
  
  public static ModifierSet make(Modifier mod, Modifier... mods) {
    return new ModifierSet(EnumSet.of(mod, mods), new LinkedList<Annotation>());
  }
  
  public enum Modifier {
    PUBLIC {
      public int getBits() { return 0x0001; }
      public String getName() { return "public"; }
    },
    PRIVATE {
      public int getBits() { return 0x0002; }
      public String getName() { return "private"; }
    },
    PROTECTED {
      public int getBits() { return 0x0004; }
      public String getName() { return "protected"; }
    },
    STATIC {
      public int getBits() { return 0x0008; }
      public String getName() { return "static"; }
    },
    FINAL {
      public int getBits() { return 0x0010; }
      public String getName() { return "final"; }
    },
    ABSTRACT {
      public int getBits() { return 0x0400; }
      public String getName() { return "abstract"; }
    },
    VOLATILE {
      public int getBits() { return 0x0040; }
      public String getName() { return "volatile"; }
    },
    TRANSIENT {
      public int getBits() { return 0x0080; }
      public String getName() { return "transient"; }
    },
    SYNCHRONIZED {
      public int getBits() { return 0x0020; }
      public String getName() { return "synchronized"; }
    },
    NATIVE {
      public int getBits() { return 0x0100; }
      public String getName() { return "native"; }
    },
    STRICT {
      public int getBits() { return 0x0800; }
      public String getName() { return "strictfp"; }
    },
    INTERFACE {
      public int getBits() { return 0x0200; }
      public String getName() { return "[interface]"; }
    },
    ANNOTATION {
      public int getBits() { return 0x2000; }
      public String getName() { return "[annotation]"; }
    },
    ENUM {
      public int getBits() { return 0x4000; }
      public String getName() { return "[enum]"; }
    },
    BRIDGE {
      public int getBits() { return 0x0040; }
      public String getName() { return "[bridge]"; }
    },
    VARARGS {
      public int getBits() { return 0x0080; }
      public String getName() { return "[varargs]"; }
    },
    SYNTHETIC {
      public int getBits() { return 0x1000; }
      public String getName() { return "[synthetic]"; }
    };
    
    /** Get the bit mask used by the JVM to represent this modifier. */
    public abstract int getBits();
    /** Get the string used to represent this modifier in source code.  Implicit modifiers are bracketed. */
    public abstract String getName();
  }

}
