/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.platform;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.prefs.Preferences;

/** Based on http://code.google.com/p/javaregistrywrapper/ */
public class WindowsRegistry {
  /** Windows handles to HKEY_CLASSES_ROOT hive. */
  public static final int HKEY_CLASSES_ROOT = 0x80000000;

  /** Windows handles to HKEY_CURRENT_USER hive. */
  public static final int HKEY_CURRENT_USER = 0x80000001;
  
  /** Windows handles to HKEY_LOCAL_MACHINE hive. */
  public static final int HKEY_LOCAL_MACHINE = 0x80000002;
  
  // Windows error codes.
  /** Registry Operation Successful */
  public static final int ERROR_SUCCESS = 0;
  
  /** Error because the specified Registry Key was not found */
  public static final int ERROR_FILE_NOT_FOUND = 2;
  
  /** Error because acces to the specified key was denied */
  public static final int ERROR_ACCESS_DENIED = 5;
  
  /** Error because acces to the specified key was denied */
  public static final int ERROR_UNKNOWN = -1;
  
  /** The null native handle */
  public static final int NULL_NATIVE_HANDLE = 0;
  
  // Security Masks  
  /** Mask allowing permission to Delete */
  public static final int DELETE = 0x10000;
  
  /** Mask allowing permission to Query */
  public static final int KEY_QUERY_VALUE = 1;
  
  /** Mask allowing permission to Set Value */
  public static final int KEY_SET_VALUE = 2;
  
  /** Mask allowing permission to Create a Sub Key */
  public static final int KEY_CREATE_SUB_KEY = 4;
  
  /** Mask allowing permission to enumerate sub keys */
  public static final int KEY_ENUMERATE_SUB_KEYS = 8;
  
  /** Mask allowing permission to read a value */
  public static final int KEY_READ = 0x20019;
  
  /** Mask allowing permission to write/create a value */
  public static final int KEY_WRITE = 0x20006;
  
  /** Mask allowing all access permission */
  public static final int KEY_ALL_ACCESS = 0xf003f;
  
  /** Exception thrown by these methods. */
  public static class RegistryException extends Exception {
    private int errorCode = ERROR_UNKNOWN;
    public RegistryException(String s) { super(s); }
    public RegistryException(String s, Throwable cause) { super(s, cause); }
    public RegistryException(int error) { super("Error code "+error); errorCode = error; }
    public RegistryException(String s, int error) { super(s); errorCode = error; }
    public int getErrorCode() { return errorCode; }
  }
  
  public static class RegistryKeyNotFoundException extends RegistryException {
    private int hive;
    private String subKey;
    public RegistryKeyNotFoundException(int hive, String subKey) {
      super("Registry key "+hiveToString(hive)+"\\"+subKey+" not found", ERROR_FILE_NOT_FOUND);
      this.hive = hive;
      this.subKey = subKey;
    }
    public int getHive() { return hive; }
    public String getSubKey() { return subKey; }
  }
  
  public static class RegistryAccessDeniedException extends RegistryException {
    private int hive;
    private String subKey;
    public RegistryAccessDeniedException(int hive, String subKey) {
      super("Registry key "+hiveToString(hive)+"\\"+subKey+" could not be accessed", ERROR_ACCESS_DENIED);
      this.hive = hive;
      this.subKey = subKey;
    }
    public int getHive() { return hive; }
    public String getSubKey() { return subKey; }
  }
  
  private static RegistryException newRegistryException(int error, int hive, String subKey) {
    if (error==ERROR_FILE_NOT_FOUND) return new RegistryKeyNotFoundException(hive, subKey);
    if (error==ERROR_ACCESS_DENIED) return new RegistryKeyNotFoundException(hive, subKey);
    return new RegistryException(error);
  }
  
  // Returned by the createKey method.
  public static class CreateResult {
    public int handle;
    public boolean wasCreated;
    public CreateResult(int h, boolean c) { handle = h; boolean wasCreated = c; }
  }

  // Returned by the queryInfoKey method.
  public static class QueryInfoResult {
    public int subkeyCount;
    public int valueCount;
    public int maxSubkeyLength;
    public int maxValueLength;
    public QueryInfoResult(int sc, int vc, int msl, int mvl) { subkeyCount = sc; valueCount = vc; maxSubkeyLength = msl; maxValueLength = mvl; }
  }
  
  
  // Constants used to interpret returns of native functions
  /** The index of Native Registry Handle in the return from opening/creating a key */
  private static final int NATIVE_HANDLE = 0;
  
  /** The index of Error Code in the return value */
  private static final int ERROR_CODE = 1;
  
  /** Index pointing to the count of sub keys */
  private static final int SUBKEYS_NUMBER = 0;
  
  /** Index pointing to the count of sub values */
  private static final int VALUES_NUMBER = 2;
  
  /** Index pointing to the max length of sub key */
  private static final int MAX_KEY_LENGTH = 3;
  
  /** Index pointing to the max length of a value name */
  private static final int MAX_VALUE_NAME_LENGTH = 4;
  
  /** Index specifying whether new key was created or existing key was opened */
  private static final int DISPOSITION = 2;
  
  /** Value specifying that new key was created */
  private  static final int REG_CREATED_NEW_KEY = 1;
  
  /** Value specifying that existing key was opened */
  private static final int REG_OPENED_EXISTING_KEY = 2;

  /** true if the Methods for reflection below have been initialized. */
  private static volatile boolean initialized = false;
  
  private static final Preferences userRoot = Preferences.userRoot();
  private static final Preferences systemRoot = Preferences.systemRoot();
  private static Class userClass = null;
  private static Class systemClass = null;
  private static Method windowsRegOpenKey = null;
  private static Method windowsRegCloseKey = null;
  private static Method windowsRegCreateKeyEx = null;
  private static Method windowsRegDeleteKey = null;
  private static Method windowsRegFlushKey = null;
  private static Method windowsRegQueryValueEx = null;
  private static Method windowsRegSetValueEx = null;
  private static Method windowsRegDeleteValue = null;
  private static Method windowsRegQueryInfoKey = null;
  private static Method windowsRegEnumKeyEx = null;
  private static Method windowsRegEnumValue = null;
  
  /** Initialize Methods above for reflection. */
  private static synchronized void initialize() throws NoSuchMethodException {
    if (initialized) return;
    initialized = true;
    
    userClass = userRoot.getClass();
    systemClass = systemRoot.getClass();
    windowsRegOpenKey = userClass.getDeclaredMethod("WindowsRegOpenKey", new Class[] { int.class, byte[].class, int.class });
    windowsRegOpenKey.setAccessible(true);
    
    windowsRegCloseKey = userClass.getDeclaredMethod("WindowsRegCloseKey", new Class[] { int.class });
    windowsRegCloseKey.setAccessible(true);
    
    windowsRegCreateKeyEx = userClass.getDeclaredMethod("WindowsRegCreateKeyEx", new Class[] { int.class, byte[].class });
    windowsRegCreateKeyEx.setAccessible(true);
    
    windowsRegDeleteKey = userClass.getDeclaredMethod("WindowsRegDeleteKey", new Class[] { int.class, byte[].class });
    windowsRegDeleteKey.setAccessible(true);
    
    windowsRegFlushKey = userClass.getDeclaredMethod("WindowsRegFlushKey", new Class[] { int.class });
    windowsRegFlushKey.setAccessible(true);
    
    windowsRegQueryValueEx = userClass.getDeclaredMethod("WindowsRegQueryValueEx", new Class[] { int.class, byte[].class });
    windowsRegQueryValueEx.setAccessible(true);
    
    windowsRegSetValueEx = userClass.getDeclaredMethod("WindowsRegSetValueEx", new Class[] { int.class, byte[].class, byte[].class });
    windowsRegSetValueEx.setAccessible(true);
    
    windowsRegDeleteValue = userClass.getDeclaredMethod("WindowsRegDeleteValue", new Class[] { int.class, byte[].class });
    windowsRegDeleteValue.setAccessible(true);
    
    windowsRegQueryInfoKey = userClass.getDeclaredMethod("WindowsRegQueryInfoKey", new Class[] { int.class });
    windowsRegQueryInfoKey.setAccessible(true);
    
    windowsRegEnumKeyEx = userClass.getDeclaredMethod("WindowsRegEnumKeyEx", new Class[] { int.class, int.class, int.class });
    windowsRegEnumKeyEx.setAccessible(true);
    
    windowsRegEnumValue = userClass.getDeclaredMethod("WindowsRegEnumValue", new Class[] { int.class, int.class, int.class });
    windowsRegEnumValue.setAccessible(true); 
  }
  
  /** Java wrapper for Windows registry APIRegOpenKey()
    * @param hKey handle to the hive
    * @param subKey sub key
    * @param securityMask security mask
    * @return native handle of the registry entry */
  public static int openKey(int hKey, String subKey, int securityMask) throws RegistryException {
    int[] retval = null;
    try {
      initialize();
      byte[] barr = stringToNullTerminated(subKey);
      retval = (int[])windowsRegOpenKey.invoke(systemRoot, new Object[] { hKey, barr, securityMask });
      if (retval.length!=2) throw new AssertionError("Invalid array length.");
      if (retval[ERROR_CODE]!=ERROR_SUCCESS) throw newRegistryException(retval[ERROR_CODE], hKey, subKey);
    }
    catch(NoSuchMethodException nsme) {
      throw new RegistryException("Exception thrown in openKey", nsme);
    }
    catch(IllegalArgumentException iae) {
      throw new RegistryException("Exception thrown in openKey", iae);
    }
    catch(IllegalAccessException iae2) {
      throw new RegistryException("Exception thrown in openKey", iae2);
    }
    catch(InvocationTargetException ite) {
      throw new RegistryException("Exception thrown in openKey", ite);
    }
    return retval[NATIVE_HANDLE];
  }
  
  /** Java wrapper for Windows registry API RegCloseKey()
    * @param hKey native handle */
  public static void closeKey(int hKey) throws RegistryException {
    try {
      initialize();
      int retval = ((Integer)windowsRegCloseKey.invoke(systemRoot, new Object[] { hKey })).intValue();
      if (retval!=ERROR_SUCCESS) throw new RegistryException(retval);
    }
    catch(NoSuchMethodException nsme) {
      throw new RegistryException("Exception thrown in closeKey", nsme);
    }
    catch(IllegalArgumentException iae) {
      throw new RegistryException("Exception thrown in closeKey", iae);
    }
    catch(IllegalAccessException iae2) {
      throw new RegistryException("Exception thrown in closeKey", iae2);
    }
    catch(InvocationTargetException ite) {
      throw new RegistryException("Exception thrown in closeKey", ite);
    }
  }
  
  /** Java wrapper for Windows registry API RegCreateKeyEx()
    * @param hKey native handle to the key
    * @param subKey sub key
    * @return CreateResult with native handle and flag whether this key was created or already existed */
  public static CreateResult createKey(int hKey, String subKey) throws RegistryException {
    // retval[NATIVE_HANDLE] will be the Native Handle of the registry entry
    // retval[ERROR_CODE] will be the error code; ERROR_SUCCESS means success
    // retval[DISPOSITION] will indicate whether key was created or existing key was opened */
    int[] retval = null;
    try {
      initialize();
      byte[] barr = stringToNullTerminated(subKey);
      retval = (int[])windowsRegCreateKeyEx.invoke(systemRoot, new Object[] { hKey, barr });
      if (retval.length!=3) throw new AssertionError("Invalid array length.");
      if (retval[ERROR_CODE]!=ERROR_SUCCESS) throw newRegistryException(retval[ERROR_CODE], hKey, subKey);
    }
    catch(NoSuchMethodException nsme) {
      throw new RegistryException("Exception thrown in createKey", nsme);
    }
    catch(IllegalArgumentException iae) {
      throw new RegistryException("Exception thrown in createKey", iae);
    }
    catch(IllegalAccessException iae2) {
      throw new RegistryException("Exception thrown in createKey", iae2);
    }
    catch(InvocationTargetException ite) {
      throw new RegistryException("Exception thrown in createKey", ite);
    }
    return new CreateResult(retval[NATIVE_HANDLE], retval[DISPOSITION]==REG_CREATED_NEW_KEY);
  }
  
  /** Java wrapper for Windows registry API RegDeleteKey()
    * @param hKey native handle to a key
    * @param subKey sub key to be deleted */
  public static void deleteKey(int hKey, String subKey) throws RegistryException {
    try {
      initialize();
      byte[] barr = stringToNullTerminated(subKey);
      int retval = ((Integer)windowsRegDeleteKey.invoke(systemRoot, new Object[] { hKey, barr })).intValue();
      if (retval!=ERROR_SUCCESS) throw newRegistryException(retval, hKey, subKey);
    }
    catch(NoSuchMethodException nsme) {
      throw new RegistryException("Exception thrown in deleteKey", nsme);
    }
    catch(IllegalArgumentException iae) {
      throw new RegistryException("Exception thrown in deleteKey", iae);
    }
    catch(IllegalAccessException iae2) {
      throw new RegistryException("Exception thrown in deleteKey", iae2);
    }
    catch(InvocationTargetException ite) {
      throw new RegistryException("Exception thrown in deleteKey", ite);
    }
  }
  
  /** Java wrapper for Windows registry API RegFlushKey()
    * @param hKey native handle */
  public static void flushKey(int hKey) throws RegistryException {
    try {
      initialize();
      int retval = ((Integer)windowsRegFlushKey.invoke(systemRoot, new Object[] { hKey })).intValue();
      if (retval!=ERROR_SUCCESS) throw new RegistryException(retval);
    }
    catch(NoSuchMethodException nsme) {
      throw new RegistryException("Exception thrown in flushKey", nsme);
    }
    catch(IllegalArgumentException iae) {
      throw new RegistryException("Exception thrown in flushKey", iae);
    }
    catch(IllegalAccessException iae2) {
      throw new RegistryException("Exception thrown in flushKey", iae2);
    }
    catch(InvocationTargetException ite) {
      throw new RegistryException("Exception thrown in flushKey", ite);
    }
  }
  
  /** Java wrapper for Windows registry API RegQueryValueEx()
    * @param hKey native handle
    * @param valueName name of value to be queried
    * @return value queried */
  public static String queryValue(int hKey, String valueName) throws RegistryException {
    byte[] retval = null;
    try {
      initialize();
      byte[] barr = stringToNullTerminated(valueName);
      retval = (byte[])windowsRegQueryValueEx.invoke(systemRoot, new Object[] { hKey, barr });
    }
    catch(NoSuchMethodException nsme) {
      throw new RegistryException("Exception thrown in queryValue", nsme);
    }
    catch(IllegalArgumentException iae) {
      throw new RegistryException("Exception thrown in queryValue", iae);
    }
    catch(IllegalAccessException iae2) {
      throw new RegistryException("Exception thrown in queryValue", iae2);
    }
    catch(InvocationTargetException ite) {
      throw new RegistryException("Exception thrown in queryValue", ite);
    }
    return nullTerminatedToString(retval);
  }  
  
  /** Java wrapper for Windows registry API RegSetValueEx()
    * Creates a value if it didnt exist or will overwrite existing value
    * @param hKey native handle to the key
    * @param valueName name of the value
    * @param value new vaue to be set */
  public static void setValue(int hKey, String valueName, String value) throws RegistryException {
    try {
      initialize();
      byte[] barrName = stringToNullTerminated(valueName);
      byte[] barrValue = stringToNullTerminated(value);
      int retval = ((Integer)windowsRegSetValueEx.invoke(systemRoot, new Object[] { hKey, barrName, barrValue })).intValue();
      if (retval!=ERROR_SUCCESS) throw new RegistryException(retval);
    }
    catch(NoSuchMethodException nsme) {
      throw new RegistryException("Exception thrown in setValue", nsme);
    }
    catch(IllegalArgumentException iae) {
      throw new RegistryException("Exception thrown in setValue", iae);
    }
    catch(IllegalAccessException iae2) {
      throw new RegistryException("Exception thrown in setValue", iae2);
    }
    catch(InvocationTargetException ite) {
      throw new RegistryException("Exception thrown in setValue", ite);
    }
  }
  
  /** Java wrapper for Windows registry API RegDeleteValue()
    * @param hKey native handle
    * @param valueName sub key name */
  public static void deleteValue(int hKey, String valueName) throws RegistryException {
    try {
      initialize();
      byte[] barr = stringToNullTerminated(valueName);
      int retval = ((Integer)windowsRegDeleteValue.invoke(systemRoot, new Object[] { hKey, barr })).intValue();
      if (retval!=ERROR_SUCCESS) throw new RegistryException(retval);
    }
    catch(NoSuchMethodException nsme) {
      throw new RegistryException("Exception thrown in deleteValue", nsme);
    }
    catch(IllegalArgumentException iae) {
      throw new RegistryException("Exception thrown in deleteValue", iae);
    }
    catch(IllegalAccessException iae2) {
      throw new RegistryException("Exception thrown in deleteValue", iae2);
    }
    catch(InvocationTargetException ite) {
      throw new RegistryException("Exception thrown in deleteValue", ite);
    }
  }
  
  
  /** Java wrapper for Windows registry API RegQueryInfoKey()
    * @param hKey native handle
    * @return QueryInfoResult with number of sub keys, number of values, and their maximum lengths */
  public static QueryInfoResult queryInfoKey(int hKey) throws RegistryException {
    // retval[SUBKEYS_NUMBER] will give then number of sub keys under the queried key.
    // retval[ERROR_CODE] will give the error code.
    // retval[VALUES_NUMBER] will give the number of values under the given key.
    // retval[MAX_KEY_LENGTH] will give the length of the longest sub key.
    // retval[MAX_VALUE_NAME_LENGTH] will give length of the longest value name.
    int[] retval = null;
    try {
      initialize();
      retval = (int[])windowsRegQueryInfoKey.invoke(systemRoot, new Object[] { hKey });
    }
    catch(NoSuchMethodException nsme) {
      throw new RegistryException("Exception thrown in queryInfoKey", nsme);
    }
    catch(IllegalArgumentException iae) {
      throw new RegistryException("Exception thrown in queryInfoKey", iae);
    }
    catch(IllegalAccessException iae2) {
      throw new RegistryException("Exception thrown in queryInfoKey", iae2);
    }
    catch(InvocationTargetException ite) {
      throw new RegistryException("Exception thrown in queryInfoKey", ite);
    }
    return new QueryInfoResult(retval[SUBKEYS_NUMBER], retval[VALUES_NUMBER], retval[MAX_KEY_LENGTH], retval[MAX_VALUE_NAME_LENGTH]);
  }
  
  /** Java wrapper for Windows registry API RegEnumKeyEx()
    * @param hKey native handle
    * @param subKeyIndex index of sub key
    * @param maxKeyLength length of max sub key
    * @return name of the sub key
   */
  public static String enumKey(int hKey, int subKeyIndex, int maxKeyLength) throws RegistryException {
    byte[] retval = null;
    try {
      initialize();
      retval = (byte[])windowsRegEnumKeyEx.invoke(systemRoot, new Object[] { hKey, subKeyIndex, maxKeyLength });
    }
    catch(NoSuchMethodException nsme) {
      throw new RegistryException("Exception thrown in enumKey", nsme);
    }
    catch(IllegalArgumentException iae) {
      throw new RegistryException("Exception thrown in enumKey", iae);
    }
    catch(IllegalAccessException iae2) {
      throw new RegistryException("Exception thrown in enumKey", iae2);
    }
    catch(InvocationTargetException ite) {
      throw new RegistryException("Exception thrown in enumKey", ite);
    }
    return nullTerminatedToString(retval);
  }  
  
  /** Java wrapper for Windows registry API RegEnumValue()
    * @param hKey native handle
    * @param valueIndex index of the value in the key
    * @param maxValueNameLength max length of name
    * @return value
    */
  public static String enumValue(int hKey, int valueIndex, int maxValueNameLength) throws RegistryException {
    byte[] retval = null;
    try {
      initialize();
      retval = (byte[])windowsRegEnumValue.invoke(systemRoot, new Object[] { hKey, valueIndex, maxValueNameLength });
    }
    catch(NoSuchMethodException nsme) {
      throw new RegistryException("Exception thrown in enumValue", nsme);
    }
    catch(IllegalArgumentException iae) {
      throw new RegistryException("Exception thrown in enumValue", iae);
    }
    catch(IllegalAccessException iae2) {
      throw new RegistryException("Exception thrown in enumValue", iae2);
    }
    catch(InvocationTargetException ite) {
      throw new RegistryException("Exception thrown in enumValue", ite);
    }
    return nullTerminatedToString(retval);
  }
  
  /** Convenience method to set a key.
    * @param hKey native handle for the hive
    * @param subKey name of the subkey
    * @param name name of the value ("" for default value)
    * @param value value to set */
  public static void setKey(int hKey, String subKey, String name, String value) throws RegistryException {
    int handle = createKey(hKey, subKey).handle;
    handle = openKey(hKey, subKey, KEY_ALL_ACCESS);
    setValue(handle, name, value);
    flushKey(handle);
    closeKey(handle);
  }
  
  /** Convenience method to get a key's value.
    * @param hKey native handle for the hive
    * @param subKey name of the subkey
    * @param name name of the value ("" for default value) */
  public static String getKey(int hKey, String subKey, String name) throws RegistryException {
    int handle = openKey(hKey, subKey, KEY_QUERY_VALUE);
    String s = queryValue(handle, name);
    closeKey(handle);
    return s;
  }
  
  /** @return a string representation of a registry key, including all subkeys. */
  public static String toString(int hKey, String subKey) throws RegistryException {
    StringBuilder sb = new StringBuilder();
    toStringHelper(hKey, subKey, "", sb);
    return sb.toString();
  }

  private static void toStringHelper(int hKey, String subKey, String prefix, StringBuilder sb) throws RegistryException {
    int handle = openKey(hKey, subKey, KEY_ENUMERATE_SUB_KEYS|KEY_QUERY_VALUE);
    QueryInfoResult qi = queryInfoKey(handle);
    sb.append(prefix).append(subKey).append('\n');
    sb.append(prefix).append(qi.subkeyCount).append(" subkeys, ").append(qi.valueCount).append(" values\n");
    String s;
    for(int i=0; i<qi.valueCount; ++i) {
      s = enumValue(handle, i, qi.maxValueLength+1);
      sb.append(prefix).append(s).append(" = ");
      s = queryValue(handle, s);
      sb.append(s).append('\n');
    }
    for(int i=0; i<qi.subkeyCount; ++i) {
      s = enumKey(handle, i, qi.maxSubkeyLength+1);
      toStringHelper(handle, s, prefix+"   ", sb);
    }
    closeKey(handle);
  }
  
  /** Recursively delete a key.
    * @param hKey native handle
    * @param subKey subkey name */
  public static void delKey(int hKey, String subKey) throws RegistryException {
    int handle = openKey(hKey, subKey, KEY_ALL_ACCESS);
    QueryInfoResult qi = queryInfoKey(handle);
    String s;
    for(int i=0; i<qi.valueCount; ++i) {
      s = enumValue(handle, i, qi.maxValueLength+1);
      if (s!=null) deleteValue(handle, s);
    }
    for(int i=0; i<qi.subkeyCount; ++i) {
      s = enumKey(handle, i, qi.maxSubkeyLength+1);
      if (s!=null) delKey(handle, s);
    }
    flushKey(handle);
    closeKey(handle);
    deleteKey(hKey, subKey);
  }
  
  /** @return this Java string as a null-terminated byte array in the default Charset */
  public static byte[] stringToNullTerminated(String str) {
    return stringToNullTerminated(str, java.nio.charset.Charset.defaultCharset());
  }
  
  /** @return this Java string as a null-terminated byte array */
  public static byte[] stringToNullTerminated(String str, java.nio.charset.Charset charset) {
    return stringToNullTerminated(str, charset.toString());
  }
  
  /** @return this Java string as a null-terminated byte array */
  public static byte[] stringToNullTerminated(String str, String charset) {
    try {
      byte[] barr = str.getBytes(charset);
      byte[] result = new byte[barr.length + 1];
      System.arraycopy(barr, 0, result, 0, barr.length);
      result[result.length-1] = 0;
      return result;
    }
    catch(java.io.UnsupportedEncodingException uee) {
      return new byte[] { 0 };
    }
  }

  /** @return this null-terminated byte array as Java String using the default Charset */
  public static String nullTerminatedToString(byte[] barr) {
    return nullTerminatedToString(barr, java.nio.charset.Charset.defaultCharset());
  }
  
  /** @return this null-terminated byte array as Java String */
  public static String nullTerminatedToString(byte[] barr, java.nio.charset.Charset charset) {
    return nullTerminatedToString(barr, charset.toString());
  }
  
  /** @return this null-terminated byte array as Java String */
  public static String nullTerminatedToString(byte[] barr, String charset) {
    try {
      if (barr==null) return null;
      // bugfix for 2976104: according to the String Javadocs, "The length of the new String is a function of the
      // charset, and hence may not be equal to the length of the byte array."
      // We cannot use barr.length to index in the string, we need to index in the array.
      int len = barr.length;
      if (barr[len-1] == 0) { --len; } // do not copy null terminator
      return new String(barr, 0, len, charset);
    }
    catch(Exception e) { return ""; } // defensive programming for bug 2976104
  }
  
  /** @return a name for the hive (HKEY_???). */
  public static String hiveToString(int hive) {
    return (hive==HKEY_CLASSES_ROOT?"HKEY_CLASSES_ROOT":
              (hive==HKEY_CURRENT_USER?"HKEY_CURRENT_USER":
                 (hive==HKEY_LOCAL_MACHINE?"HKEY_LOCAL_MACHINE":"0x"+Integer.toHexString(hive))));
  }
//  
//  public static void main(String args[]) {
//    try {
//      System.out.println(Preferences.userRoot().getClass());
//      
//      System.out.println("Proxy Server = "+getKey(HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings","ProxyServer"));
//      System.out.println("Internet Explorer Version = "+getKey(HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Internet Explorer","Version"));
//      System.out.println(".java Perceived Type = "+getKey(HKEY_CLASSES_ROOT, ".java","PerceivedType"));
//      
//      try {
//        System.out.println(".drjava = "+getKey(HKEY_CLASSES_ROOT, ".drjava",""));
//      }
//      catch(RegistryException re) { System.err.println(re); }
//
//      setKey(HKEY_CLASSES_ROOT, ".drjava", "", "DrJavaProject");
//      setKey(HKEY_CLASSES_ROOT, "DrJavaProject", "", "DrJava project file");
//      setKey(HKEY_CLASSES_ROOT, "DrJavaProject\\shell\\open\\command", "",
//             "\"C:\\Program Files\\Java\\jre6\\bin\\javaw.exe\" -jar \"C:\\Documents and Settings\\Administrator\\Desktop\\drjava.jar\" \"%1\" %*");
//
//      System.out.println(".drjava = "+getKey(HKEY_CLASSES_ROOT, ".drjava",""));
//      System.out.println(toString(HKEY_CLASSES_ROOT, "Drive"));
//      
////      deleteKey(HKEY_CLASSES_ROOT, ".drjava");
////      deleteKey(HKEY_CLASSES_ROOT, "DrJavaProject\\shell\\open\\command");
////      deleteKey(HKEY_CLASSES_ROOT, "DrJavaProject\\shell\\open");
////      deleteKey(HKEY_CLASSES_ROOT, "DrJavaProject\\shell");
////      deleteKey(HKEY_CLASSES_ROOT, "DrJavaProject");
//      delKey(HKEY_CLASSES_ROOT, ".drjava");
//      delKey(HKEY_CLASSES_ROOT, "DrJavaProject");
//    }
//    catch(RegistryException re) {
//      re.printStackTrace();
//    }
//  }
}
