import java.io.*;
import java.util.Properties;
import java.util.Date;

public class DrJavaHJPropertyFile {
  public static void main(String[] args) throws IOException {
    System.out.println("Appends the DrJava version string.");
    System.out.println("Disables automatic updates in DrJava.");
    
    String hjVersion = polyglot.ext.hj.Version.getVersion();
    System.out.println("HJ version is "+hjVersion);
    
    Properties p = new Properties();
    String fileName = "edu/rice/cs/drjava/config/options.properties";
    String versionAddition = "www.habanero.rice.edu";
    if (args.length>0) {
      fileName = args[0];
    }
    if (args.length>1) {
      versionAddition = args[1];
    }
    
    p.load(new FileInputStream(fileName));
    
    String versionSuffix = p.getProperty("custom.drjava.jar.version.suffix");
    if (versionSuffix==null) {
      versionSuffix = versionAddition;
    }
    else {
      versionSuffix += ", "+versionAddition;
    }
    p.setProperty("custom.drjava.jar.version.suffix", versionSuffix);
    p.setProperty("new.version.allowed", "false");
    p.setProperty("new.version.notification", "none (disabled)");
    p.setProperty("default.compiler.preference.control", "HJ "+hjVersion);
    p.setProperty("default.compiler.preference", "HJ "+hjVersion);

    p.store(new FileOutputStream(fileName), "drjava.jar with Habanero Java file generated "+new Date());
  }
  
  public static class GetFileName {
      public static void main(String[] args) throws IOException {
          int drjavaRev = edu.rice.cs.drjava.Version.getRevisionNumber();
          String hjVersion = polyglot.ext.hj.Version.getVersion();
          System.out.println("drjava-r"+drjavaRev+"-hj-"+hjVersion+".jar");
      }
  }
}
