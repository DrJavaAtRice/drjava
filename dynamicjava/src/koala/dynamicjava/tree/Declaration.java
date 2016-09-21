package koala.dynamicjava.tree;

public abstract class Declaration extends Node {
  
  private ModifierSet modifiers;

  protected Declaration(ModifierSet mods, SourceInfo si) {
    super(si);
   if (mods == null) { throw new IllegalArgumentException(); }
    modifiers = mods;
  }
  
  public ModifierSet getModifiers() { return modifiers; }
  
  public void setModifiers(ModifierSet mods) {
    if (mods == null) { throw new IllegalArgumentException(); }
    modifiers = mods;
  }
  
}
