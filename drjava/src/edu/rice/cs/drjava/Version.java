/* $Id$ */

package edu.rice.cs.drjava;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * This interface hold the information about this build of DrJava.
 * This file is copied to Version.java by the build process, which also
 * fills in the right values of the date and time.
 */
public abstract class Version {
  /**
   * This string will be automatically expanded upon "ant commit".
   * Do not edit it by hand!
   */
  public static final Date BUILD_TIME = _getBuildDate();

  private static Date _getBuildDate() {
    try {
      return new SimpleDateFormat("yyyyMMdd-HHmm").parse("20010815-1038");
    }
    catch (Exception e) { // parse format or whatever problem
      return null;
    }
  }
} 
