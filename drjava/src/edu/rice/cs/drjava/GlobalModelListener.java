package edu.rice.cs.drjava;

import java.io.File;

public interface GlobalModelListener {
  public class SaveReason {
    private SaveReason() {}
  }
  
  public static final SaveReason COMPILE_REASON = new SaveReason();
  
  public void newFileCreated();
  
  public void fileSaved(File file);
  
  public void fileOpened(File file);
    
  public void compileStarted();
  
  public void compileEnded();
      
  public void interactionsReset();
  
  public void consoleReset();
  
  public void saveBeforeProceeding(SaveReason reason);
  
  public boolean canAbandonFile(File file);
}
