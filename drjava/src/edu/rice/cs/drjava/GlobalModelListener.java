package edu.rice.cs.drjava;

public interface GlobalModelListener {

  public void fireNewFileEvent();
  
  public void fireSaveFileEvent(String fileName);
  
  public void fireOpenFileEvent(String fileName);
  
  public void fireCompileBeginEvent();
  
  public void fireCompileEndEvent();
  
  public void fireQuitEvent();
  
  public void fireResetInteractionsEvent();
  
  public void fireResetConsoleEvent();
  
}