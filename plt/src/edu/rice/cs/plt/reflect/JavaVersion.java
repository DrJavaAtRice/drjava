/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.reflect;

import java.io.Serializable;
import java.io.File;

/** A representation of a major Java version, with methods for parsing version number strings. */
public enum JavaVersion {
  UNRECOGNIZED { public String versionString() { return "?"; } },
  JAVA_1_1 { public String versionString() { return "1.1"; } },
  JAVA_1_2 { public String versionString() { return "1.2"; } },
  JAVA_1_3 { public String versionString() { return "1.3"; } },
  JAVA_1_4 { public String versionString() { return "1.4"; } },
  JAVA_5 { public String versionString() { return "5"; } },
  JAVA_6 { public String versionString() { return "6"; } },
  JAVA_7 { public String versionString() { return "7"; } },
  JAVA_8 { public String versionString() { return "8"; } },
  FUTURE { public String versionString() { return ">8"; } };
  
  /** The currently-available Java version, based on the {@code "java.class.version"} property.  Ideally, a {@code true}
    * result  for {@code JavaVersion.CURRENT.supports(v)} implies that all APIs associated with that version are
    * available  at runtime.  However, we do not attempt to (and cannot, in general) guarantee that the boot class path
    * or Java installation have not been modified to only support certain API classes.
    */
  public static final JavaVersion CURRENT = parseClassVersion(System.getProperty("java.class.version", ""));
  
  /** The currently-available Java version, based on the {@code "java.version"} property. */
  public static final JavaVersion.FullVersion CURRENT_FULL = 
    parseFullVersion(System.getProperty("java.version", ""),
                     System.getProperty("java.runtime.name", ""),
                     System.getProperty("java.vm.vendor", ""),
                     null);
  
  /** {@code true} iff this version is at least as recent as {@code v}, and thus can be expected to 
    * support {@code v}'s APIs (as long as the required features are backwards-compatible)
    */
  public boolean supports(JavaVersion v) { return compareTo(v) >= 0; }

  /** Produce the version number as a string */
  public abstract String versionString();
  
  /** Prepend {@code "Java "} to the version number string */
  public String toString() { return "Java " + versionString(); }
  
  /** Returns a FullVersion that corresponds to this JavaVersion, e.g. JAVA_6 will return a FullVersion 1.6.0_0. */
  public FullVersion fullVersion() {
    return new FullVersion(this, 0, 0, ReleaseType.STABLE, null, VendorType.UNKNOWN, "", null);
  }
  
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
      int minor = Integer.parseInt(text.substring(dot + 1));
      
      return parseClassVersion(major, minor);
    }
    catch (NumberFormatException e) { return UNRECOGNIZED; }
  }
  
  /**
   * Produce the {@code JavaVersion} corresponding to the given class version pair.  For example,
   * {@code 49,0} maps to {@code JAVA_5}.  If the pair cannot be matched, {@code UNRECOGNIZED} will be
   * returned.
   */
  public static JavaVersion parseClassVersion(int major, int minor) {
    switch (major) {
      case 45:
        if (minor >= 3) { return JAVA_1_1; }
        else { return UNRECOGNIZED; }
      case 46: return JAVA_1_2;
      case 47: return JAVA_1_3;
      case 48: return JAVA_1_4;
      case 49: return JAVA_5;
      case 50: return JAVA_6;
      case 51: return JAVA_7;
      case 52: return JAVA_8;
    }
    return (major > 51) ? FUTURE : UNRECOGNIZED;
  }
  
  /**
   * Produce the {@code JavaVersion} corresponding to the given class file.
   */
  public static JavaVersion parseClassVersion(java.io.File classFile) {
    java.io.FileInputStream fis = null;
    try {
      fis = new java.io.FileInputStream(classFile);
      return parseClassVersion(fis);
    }
    catch(java.io.IOException ioe) { return UNRECOGNIZED; }
    finally {
      if (fis!=null) {
        try { fis.close(); }
        catch(java.io.IOException ioe) { /* ignore */ }
      }
    }
  }
  
  /**
   * Produce the {@code JavaVersion} corresponding to the given class file.
   */
  public static JavaVersion parseClassVersion(java.io.InputStream is) {
    java.io.DataInputStream dis = null;
    try {
      dis = new java.io.DataInputStream(is);
      int magic = dis.readInt();
      if (magic != 0xCAFEBABE) { return UNRECOGNIZED; }
      int minor = dis.readUnsignedShort();
      int major = dis.readUnsignedShort();
      return parseClassVersion(major, minor);
    }
    catch(java.io.IOException ioe) { return UNRECOGNIZED; }
    finally {
      if (dis!=null) {
        try { dis.close(); }
        catch(java.io.IOException ioe) { /* ignore */ }
      }
    }
  }
  
  /** Produce the {@code JavaVersion.FullVersion} corresponding to the given version string.  Accepts
    * input of the form "1.6.0", "1.4.2_10", or "1.5.0_05-ea".  The underscore may be replaced by a dot.
    * If the text cannot be parsed, a trivial version with major version UNRECOGNIZED is returned.
    * The location of the JDK, which may be null, will be stored in the version.
    * 
    * @see <a href="http://java.sun.com/j2se/versioning_naming.html#">The Sun version specification</a>
    */
  // Why are underscores used as word separators in identifiers below?
  public static FullVersion parseFullVersion(String java_version,
                                             String java_runtime_name,
                                             String java_vm_vendor,
                                             File location) {
    VendorType vendor = VendorType.UNKNOWN;
    String vendorString = null;
    
    /** Strip trailing ".jdk" off of java_version strings from post Java 6 Oracle JVMs for Mac OS X. */
    if (java_version.endsWith(".jdk")) java_version = java_version.substring(0, java_version.length() - 4);
    
    if (vendor == VendorType.UNKNOWN) {
      if (java_runtime_name.toLowerCase().contains("openjdk")) {
        vendor = VendorType.OPENJDK;
        vendorString = "OpenJDK";
      }
      else if (java_vm_vendor.toLowerCase().contains("apple")) {
        vendor = VendorType.APPLE;
        vendorString = "Apple";
      }
      else if (java_vm_vendor.toLowerCase().contains("sun") ||
               java_vm_vendor.toLowerCase().contains("oracle")) {
        vendor = VendorType.ORACLE;
        vendorString = "Sun";
      }
    }
    
    String number;
    String typeString;
    // if version doesn't start with "1." and has only one dot, prefix with "1."
    // example: 6.0 --> 1.6.0
    if ((!java_version.startsWith("1.")) && (java_version.replaceAll("[^\\.]","").length()==1)) {
      java_version = "1." + java_version;
    }
    int dash = java_version.indexOf('-');
    if (dash == -1) { number = java_version; typeString = null; }
    else { number = java_version.substring(0, dash); typeString = java_version.substring(dash+1); }
    
    int dot1 = number.indexOf('.');
    if (dot1 == -1) {
      try {
        int feature = Integer.parseInt(number);
        
        ReleaseType type;
        if (typeString == null) { type = ReleaseType.STABLE; }
        else if (typeString.startsWith("ea")) { type = ReleaseType.EARLY_ACCESS; }
        else if (typeString.startsWith("beta")) { type = ReleaseType.BETA; }
        else if (typeString.startsWith("rc")) { type = ReleaseType.RELEASE_CANDIDATE; }
        else { type = ReleaseType.UNRECOGNIZED; }
        
        JavaVersion version = UNRECOGNIZED;
        switch (feature) {
          case 1: version = JAVA_1_1; break;
          case 2: version = JAVA_1_2; break;
          case 3: version = JAVA_1_3; break;
          case 4: version = JAVA_1_4; break;
          case 5: version = JAVA_5; break;
          case 6: version = JAVA_6; break;
          case 7: version = JAVA_7; break;
          case 8: version = JAVA_8; break;
          default: if (feature > 8) { version = FUTURE; } break;
        }
        return new FullVersion(version, 0, 0, type, typeString, vendor, vendorString, location);
      }
      catch(NumberFormatException nfe) {
        return new FullVersion(UNRECOGNIZED, 0, 0, ReleaseType.STABLE, null, vendor, vendorString, location);
      }
    }
    
    int dot2 = number.indexOf('.', dot1+1);
    if (dot2 == -1) { return new FullVersion(UNRECOGNIZED, 0, 0,
                                             ReleaseType.STABLE, null,
                                             vendor, vendorString, location); }
    int underscore = number.indexOf('_', dot2+1);
    if (underscore == -1) { underscore = number.indexOf('.', dot2+1); }
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
          case 7: version = JAVA_7; break;
          case 8: version = JAVA_8; break;
          default: if (feature > 8) { version = FUTURE; } break;
        }
      }
      
      return new FullVersion(version, maintenance, update, type, typeString, vendor, vendorString, location);
    }
    catch (NumberFormatException e) {
      return new FullVersion(UNRECOGNIZED, 0, 0, ReleaseType.STABLE, null, vendor, vendorString, location);
    }
  }
  
  /**
   * Produce the {@code JavaVersion.FullVersion} corresponding to the given version string.  Accepts
   * input of the form "1.6.0", "1.4.2_10", or "1.5.0_05-ea".  The underscore may be replaced by a dot.
   * If the text cannot be parsed, a trivial version with major version UNRECOGNIZED is returned.
   * The location of the JDK is null and will be stored in the version.
   * 
   * @see <a href="http://java.sun.com/j2se/versioning_naming.html#">The Sun version specification</a>
   */
  public static FullVersion parseFullVersion(String java_version,
                                             String java_runtime_name,
                                             String java_vm_vendor) {
    return parseFullVersion(java_version, java_runtime_name, java_vm_vendor, null);
  }
  
  /**
   * Produce the {@code JavaVersion.FullVersion} corresponding to the given version string.  Accepts
   * input of the form "1.6.0", "1.4.2_10", or "1.5.0_05-ea".  The underscore may be replaced by a dot.
   * If the text cannot be parsed, a trivial version with major version UNRECOGNIZED is returned.
   * 
   * @see <a href="http://java.sun.com/j2se/versioning_naming.html#">The Sun version specification</a>
   */
  public static FullVersion parseFullVersion(String text) {
    return parseFullVersion(text, "", "", null); // vendor = "unrecognized"
  }
  
  /**
   * A full Java version, implemented for the sake of comparison between version numbers.
   * 
   * @see <a href="http://java.sun.com/j2se/versioning_naming.html">The Sun version specification</a>
   */
  public static class FullVersion implements Comparable<FullVersion>, Serializable {
    private JavaVersion _majorVersion;
    private int _maintenance;
    private int _update;
    private ReleaseType _type;
    private String _typeString;
    private VendorType _vendor;
    private String _vendorString;
    private File _location; // may be null
    
    /** Assumes {@code typeString} is {@code null} iff {@code type} is {@code STABLE} */
    private FullVersion(JavaVersion majorVersion, int maintenance, int update, ReleaseType type,
                        String typeString, VendorType vendor, String vendorString, File location) {
      _majorVersion = majorVersion;
      _maintenance = maintenance;
      _update = update;
      _type = type;
      _typeString = typeString;
      _vendor = vendor;
      _vendorString = vendorString;
      _location = location;
    }
    
    /** Get the major version associated with this full version */
    public JavaVersion majorVersion() { return _majorVersion; }
    
    /** Get a full version with the maintenance, update and release type zeroed out. */
    public FullVersion onlyMajorVersionAndVendor() {
      return new FullVersion(_majorVersion, 0, 0, ReleaseType.STABLE, null, _vendor, _vendorString, _location);
    }
    
    /** Get the maintenance associated with this full version */
    public int maintenance() { return _maintenance; }
    
    /** Get the update associated with this full version */
    public int update() { return _update; }    
    
    /** Get the update associated with this full version */
    public ReleaseType release() { return _type; }    
    
    /** Get the vendor associated with this full version */
    public VendorType vendor() { return _vendor; }
    
    public File location() { return _location; }
    
    /** Convenience method calling {@code majorVersion().supports(v)} */
    public boolean supports(JavaVersion v) { return _majorVersion.supports(v); }
    
    /**
     * Compare two versions.  Major, maintenance, and update numbers are ordered sequentially.  When comparing
     * two versions that are otherwise equivalent, early access releases precede betas, followed by
     * release candidates and stable releases. Within the release types, Unrecognized < OpenJDK < Apple < Oracle.
     * Versions with unknown vendor are only equal if their locations are also equal.
     */
    public int compareTo(FullVersion v) {
      int result = _majorVersion.compareTo(v._majorVersion);
      if (result == 0) {
        result = _maintenance - v._maintenance;
        if (result == 0) {
          result = _update - v._update;
          if (result == 0) {
            result = _type.compareTo(v._type);
            if (result == 0) {
              result = _vendor.compareTo(v._vendor);
              if (result == 0 && !_type.equals(ReleaseType.STABLE)) {
                result = _typeString.compareTo(v._typeString);
                if (result == 0 && _vendor.equals(VendorType.UNKNOWN)) {
                  if (_location == null) {
                    if (v._location == null) return 0;
                    else return -1;
                  }
                  else {
                    if (v._location == null) return 1;
                    else return _location.compareTo(v._location);
                  }
                }
              }
              else if (result == 0 && _vendor.equals(VendorType.UNKNOWN)) {
                if (_location == null) {
                  if (v._location == null) return 0;
                  else return -1;
                }
                else {
                  if (v._location == null) return 1;
                  else return _location.compareTo(v._location);
                }
              }
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
        return _majorVersion.equals(v._majorVersion) &&
          _maintenance == v._maintenance &&
          _update == v._update &&
          _type.equals(v._type) &&
          _vendor.equals(v._vendor) &&
          (_type.equals(ReleaseType.STABLE) || _typeString.equals(v._typeString)) &&
          (!_vendor.equals(VendorType.UNKNOWN) ||
           ((_location==null) && (v._location==null)) || (_location.equals(v._location)));
      }
    }
    
    public int hashCode() {
      int stringHash = _typeString == null ? 0 : _typeString.hashCode();
      int fileHash = _location == null ? 0 : _location.hashCode();
      return _majorVersion.hashCode() ^ (_maintenance << 1) ^ (_update << 2) ^ (_type.hashCode() << 3) 
        ^ (_vendor.hashCode() << 4) ^ (stringHash << 5) ^ fileHash;
    }
    
    private String stringSuffix() {
      StringBuilder result = new StringBuilder();
      result.append("." + _maintenance);
      if (_update != 0) { result.append("_" + _update); }
      if (!_type.equals(ReleaseType.STABLE)) { result.append('-').append(_typeString); }
      return result.toString();
    }
    
    private String stringSuffixWithVendor() {
      StringBuilder result = new StringBuilder(stringSuffix());
      if ((!_vendor.equals(VendorType.ORACLE)) && 
          (!_vendor.equals(VendorType.APPLE)) &&
          (!_vendor.equals(VendorType.UNKNOWN))) {
        result.append('-').append(_vendorString);
      }
      return result.toString();
    }
    
    /** Produce a string representing version number */
    public String versionString() { return _majorVersion.versionString() + stringSuffixWithVendor(); }
    
    public String toString() { return _majorVersion + stringSuffix(); }
    
  }
  
  private static enum ReleaseType { UNRECOGNIZED, EARLY_ACCESS, BETA, RELEASE_CANDIDATE, STABLE; }
  
  /** The vendor of this version. */
  public static enum VendorType { UNKNOWN, OPENJDK, APPLE, ORACLE; }
}
