package  edu.rice.cs.drjava;

import  java.io.File;
import  javax.swing.filechooser.FileFilter;


/**
 * @version $Id$
 */
public class JavaSourceFilter extends FileFilter {

  /**
   * put your documentation comment here
   * @param f
   * @return 
   */
  public boolean accept(File f) {
    if (f.isDirectory()) {
      return  true;
    }
    String extension = getExtension(f);
    if (extension != null) {
      if (extension.equals("java") || extension.equals("j")) {
        return  true;
      } 
      else {
        return  false;
      }
    }
    return  false;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public String getDescription() {
    return  "Java and GJ source files";
  }

  /*
   * Get the extension of a file.
   */
  public static String getExtension(File f) {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');
    if (i > 0 && i < s.length() - 1) {
      ext = s.substring(i + 1).toLowerCase();
    }
    return  ext;
  }
}



