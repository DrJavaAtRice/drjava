package edu.rice.cs.plt.debug;

import edu.rice.cs.plt.iter.IterUtil;

/** 
 * A log that sends messages to all the logs it contains.  This allows, for example, logging to be
 * viewed at runtime while, at the same time, being recorded to a file.
 */
public class LogSplitter implements Log {
  
  private final Iterable<Log> _logs;
  
  /** Create a log that will send its messages to each of {@code logs} */
  public LogSplitter(Log... logs) {
    _logs = IterUtil.asIterable(logs);
  }
  
  public LogSplitter(Iterable<? extends Log> logs) {
    _logs = IterUtil.snapshot(logs);
  }
  
  public void log() {
    for (Log l : _logs) { l.log(); }
  }
  
  public void log(String message) {
    for (Log l : _logs) { l.log(message); }
  }
    
  public void log(Throwable t) {
    for (Log l : _logs) { l.log(t); }
  }
  
  public void log(String message, Throwable t) {
    for (Log l : _logs) { l.log(message, t); }
  }
  
  public void logStart() {
    for (Log l : _logs) { l.logStart(); }
  }
  
  public void logStart(String message) {
    for (Log l : _logs) { l.logStart(message); }
  }
  
  public void logEnd() {
    for (Log l : _logs) { l.logEnd(); }
  }
  
  public void logEnd(String message) {
    for (Log l : _logs) { l.logEnd(message); }
  }
  
  public void logStack() {
    for (Log l : _logs) { l.logStack(); }
  }
  
  public void logStack(String message) {
    for (Log l : _logs) { l.logStack(message); }
  }
  
  public void logValue(String name, Object value) {
    for (Log l : _logs) { l.logValue(name, value); }
  }
  
  public void logValue(String message, String name, Object value) {
    for (Log l : _logs) { l.logValue(message, name, value); }
  }
  
  public void logValues(String[] names, Object... values) {
    for (Log l : _logs) { l.logValues(names, values); }
  }
  
  public void logValues(String message, String[] names, Object... values) {
    for (Log l : _logs) { l.logValues(message, names, values); }
  }
  
}
