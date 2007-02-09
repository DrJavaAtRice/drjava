package edu.rice.cs.plt.debug;

/** A log that ignores all logging requests.  All method invocations return immediately. */
public final class VoidLog implements Log {
  
  public static final VoidLog INSTANCE = new VoidLog();
  
  private VoidLog() {}
  
  public void log() {}
  public void log(String message) {}
  public void log(Throwable t) {}
  public void log(String message, Throwable t) {}
  
  public void logStart() {}
  public void logStart(String message) {}
  
  public void logEnd() {}
  public void logEnd(String message) {}
  
  public void logStack() {}
  public void logStack(String message) {}
  
  public void logValue(String name, Object value) {}
  public void logValue(String message, String name, Object value) {}
  public void logValues(String[] names, Object... values) {}
  public void logValues(String message, String[] names, Object... values) {}
}
