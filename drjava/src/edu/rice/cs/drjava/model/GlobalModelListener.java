package edu.rice.cs.drjava;

public interface GlobalModelListener {

  public void newFileCreated();
  
  public void fileSaved(String fileName);
  
  public void fileOpened(String fileName);
  
  public void compileStarted();
  
  public void compileEnded();
  
  public void quit();
  
  public void interactionsReset();
  
  public void consoleReset();
  
  public boolean canAbandonFile();
}