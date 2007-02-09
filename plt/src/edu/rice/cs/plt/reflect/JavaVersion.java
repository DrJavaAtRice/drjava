package edu.rice.cs.plt.reflect;

/** A representation of a major Java version, with methods for parsing version number strings. */
public enum JavaVersion {
  UNRECOGNIZED { public String versionString() { return "?"; } },
  JAVA_1_1 { public String versionString() { return "1.1"; } },
  JAVA_1_2 { public String versionString() { return "1.2"; } },
  JAVA_1_3 { public String versionString() { return "1.3"; } },
  JAVA_1_4 { public String versionString() { return "1.4"; } },
  JAVA_5 { public String versionString() { return "5"; } },
  JAVA_6 { public String versionString() { return "6"; } };
  
  /**
   * The currently-available Java version, based on the {@code "java.class.version"} property.  Ideally, a {@code true} result 
   * for {@code JavaVersion.CURRENT.supports(v)} implies that all APIs associated with that version are available 
   * at runtime.  However, we do not attempt to (and cannot, in general) guarantee that the boot class path or  
   * Java installation have not been modified to only support certain API classes.
   */
  public static final JavaVersion CURRENT = parseClassVersion(System.getProperty("java.class.version"));
  
  /** The currently-available Java version, based on the {@code "java.version"} property. */
  public static final JavaVersion.FullVersion CURRENT_FULL = parseFullVersion(System.getProperty("java.version"));
  
  /**
   * {@code true} iff this version is at least as recent as {@code v}, and thus can be expected to 
   * support {@code v}'s APIs (as long as the required features are backwards-compatible)
   */
  public boolean supports(JavaVersion v) { return compareTo(v) >= 0; }

  /** Produce the version number as a string */
  public abstract String versionString();
  
  /** Prepend {@code "Java "} to the version number string */
  public String toString() { return "Java " + versionString(); }
  
  /**
   * Produce the {@code JavaVersion} corresponding to the given class version string.  For example,
   * {@code "49.0"} maps to {@code JAVA_5}.  If the text cannot be parsed, {@code UNRECOGNIZED} will be
   * returned.
   */
  public static JavaVersion parseClassVersion(String text) {
    int dot = text.indexOf('.');
    if (dot == -1) { return UNRECOGNIZED; }
    try {
      int major = Integer.parseInt(text.substring(0, dot));
      int minor = Integer.parseInt(text.substring(dot+1));
      
      switch (major) {
        case 45:
          if (minor >= 3) { return JAVA_1_1; }
          else { return UNRECOGNIZED; }
        case 46: return JAVA_1_2;
        case 47: return JAVA_1_3;
        case 48: return JAVA_1_4;
        case 49: return JAVA_5;
        case 50: return JAVA_6;
        default: return UNRECOGNIZED;
      }
    }
    catch (NumberFormatException e) { return UNRECOGNIZED; }
  }
  
  /**
   * Produce the {@code JavaVersion.FullVersion} corresponding to the given version string.  Accepts
   * input of the form "1.6.0", "1.4.2_10", or "1.5.0_05-ea".  If the text cannot be parsed, a trivial
   * version with major version UNRECOGNIZED is returned.
   * 
   * @see <a href="http://java.sun.com/j2se/versioning_naming.html">Sun's version specification</a>
   */
  public static FullVersion parseFullVersion(String text) {
    String number;
    String typeString;
    int dash = text.indexOf('-');
    if (dash == -1) { number = text; typeString = null; }
    else { number = text.substring(0, dash); typeString = text.substring(dash+1); }
    
    int dot1 = number.indexOf('.');
    if (dot1 == -1) { return UNRECOGNIZED.new FullVersion(0, 0, ReleaseType.STABLE, null); }
    int dot2 = number.indexOf('.', dot1+1);
    if (dot2 == -1) { return UNRECOGNIZED.new FullVersion(0, 0, ReleaseType.STABLE, null); }
    int underscore = number.indexOf('_', dot2+1);
    if (underscore == -1) { underscore = number.length(); }
    try {
      int major = Integer.parseInt(number.substring(0, dot1));
      int feature = Integer.parseInt(number.substring(dot1+1, dot2));
      int maintenance = Integer.parseInt(number.substring(dot2+1, underscore));
      int update = (underscore >= number.length()) ? 0 : Integer.parseInt(number.substring(underscore+1));
      
      ReleaseType type;
      if (typeString == null) { type = ReleaseType.STABLE; }
      else if (typeString.startsWith("ea")) { type = ReleaseType.EARLY_ACCESS; }
      else if (typeString.startsWith("beta")) { type = ReleaseType.BETA; }
      else if (typeString.startsWith("rc")) { type = ReleaseType.RELEASE_CANDIDATE; }
      else { type = ReleaseType.UNRECOGNIZED; }
      
      JavaVersion version = UNRECOGNIZED;
      if (major == 1) {
        switch (feature) {
          case 1: version = JAVA_1_1; break;
          case 2: version = JAVA_1_2; break;
          case 3: version = JAVA_1_3; break;
          case 4: version = JAVA_1_4; break;
          case 5: version = JAVA_5; break;
          case 6: version = JAVA_6; break;
        }
      }
      
      return version.new FullVersion(maintenance, update, type, typeString);
    }
    catch (NumberFormatException e) { return UNRECOGNIZED.new FullVersion(0, 0, ReleaseType.STABLE, null); }
  }
  
  /**
   * A full Java version, implemented for the sake of comparison between version numbers.
   * 
   * @see <a href="http://java.sun.com/j2se/versioning_naming.html">Sun's version specification</a>
   */
  public class FullVersion implements Comparable<FullVersion> {
    private int _maintenance;
    private int _update;
    private ReleaseType _type;
    private String _typeString;
    
    /** Assumes {@code typeString} is {@code null} iff {@code type} is {@code STABLE} */
    private FullVersion(int maintenance, int update, ReleaseType type, String typeString) {
      _maintenance = maintenance;
      _update = update;
      _type = type;
      _typeString = typeString;
    }
    
    /** Get the major version associated with this full version */
    public JavaVersion majorVersion() { return JavaVersion.this; }
    
    /**
     * Compare two versions.  Major, maintenance, and update numbers are ordered sequentially.  When comparing
     * two versions that are otherwise equivalent, early access releases precede betas, followed by
     * release candidates and stable releases.
     */
    public int compareTo(FullVersion v) {
      int result = majorVersion().compareTo(v.majorVersion());
      if (result == 0) {
        result = _maintenance - v._maintenance;
        if (result == 0) {
          result = _update - v._update;
          if (result == 0) {
            result = _type.compareTo(v._type);
            if (result == 0 && !_type.equals(ReleaseType.STABLE)) {
              result = _typeString.compareTo(v._typeString);
            }
          }
        }
      }
      return result;
    }
    
    public boolean equals(Object o) {
      if (this == o) { return true; }
      else if (!(o instanceof FullVersion)) { return false; }
      else {
        FullVersion v = (FullVersion) o;
        return majorVersion().equals(v.majorVersion()) &&
          _maintenance == v._maintenance &&
          _update == v._update &&
          _type.equals(v._type) &&
          (_type.equals(ReleaseType.STABLE) || _typeString.equals(v._typeString));
      }
    }
    
    public int hashCode() {
      int stringHash = _typeString == null ? 0 : _typeString.hashCode();
      return majorVersion().hashCode() ^ (_maintenance << 1) ^ (_update << 2) ^ (_type.hashCode() << 3) ^ stringHash;
    }
    
    private String stringSuffix() {
      StringBuilder result = new StringBuilder();
      result.append("." + _maintenance);
      if (_update != 0) { result.append("_" + _update); }
      if (!_type.equals(ReleaseType.STABLE)) { result.append("-" + _typeString); }
      return result.toString();
    }
    
    /** Produce a string representing version number */
    public String versionString() { return majorVersion().versionString() + stringSuffix(); }

    public String toString() { return majorVersion() + stringSuffix(); }
    
  }
    
  private static enum ReleaseType { UNRECOGNIZED, EARLY_ACCESS, BETA, RELEASE_CANDIDATE, STABLE; }
}
