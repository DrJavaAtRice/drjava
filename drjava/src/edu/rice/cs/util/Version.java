package edu.rice.cs.util;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * This interface hold the information about this build of util.
 * This file is copied to Version.java by the build process, which also
 * fills in the right values of the date and time.
 *
 * This javadoc corresponds to build util-20020207-1526;
 *
 * @version $Id$
 */
public abstract class Version {
  /**
   * This string will be automatically expanded upon "ant commit".
   * Do not edit it by hand!
   */
  public static final String BUILD_TIME_STRING = "20020207-1526";

  /** A {@link Date} version of the build time. */
  public static final Date BUILD_TIME = _getBuildDate();

  private static Date _getBuildDate() {
    try {
      return new SimpleDateFormat("yyyyMMdd-HHmm").parse(BUILD_TIME_STRING);
    }
    catch (Exception e) { // parse format or whatever problem
      return null;
    }
  }

  public static void main(String[] args) {
    System.out.println("Version for edu.rice.cs.util: " + BUILD_TIME_STRING);
  }
} 
