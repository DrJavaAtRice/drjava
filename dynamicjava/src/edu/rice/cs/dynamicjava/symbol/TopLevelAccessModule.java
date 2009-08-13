package edu.rice.cs.dynamicjava.symbol;

/** An access module for symbols declared outside of a class declaration. */
public class TopLevelAccessModule implements Access.Module {
  private final String _packageName;
  public TopLevelAccessModule(String packageName) { _packageName = packageName; }
  public String packageName() { return _packageName; }
  public boolean equals(Object o) {
    return (o instanceof TopLevelAccessModule) &&
            ((TopLevelAccessModule) o)._packageName.equals(_packageName);
  }
}
