package edu.rice.cs.drjava.project;
import java.io.*;
import java.util.*;

public class ProjectFileBuilder
{
  
  private Vector<File> _sourceFiles = new Vector<File>();
  
  private Vector<File> _resourceFiles = new Vector<File>();
  
  private Vector<File> _buildDirFiles = new Vector<File>();
  
  private Vector<File> _classpathFiles = new Vector<File>();
  
  private Vector<File> _jarFiles = new Vector<File>();
 
  private String _projectPath;
  
  public ProjectFileBuilder(String projectPath)
  {
    _projectPath = projectPath.replace('\\', '/');
  }
  
  public void addSourceFile(File sf)
  {
    _sourceFiles.add(sf);
  }
  
  public void addResourceFile(File sf)
  {
    _resourceFiles.add(sf);
  }
  public void setBuildDir(File sf)
  {
    _buildDirFiles.clear();
    _buildDirFiles.add(sf);
  }
  public void addClasspathFile(File sf)
  {
    _classpathFiles.add(sf);
  }
  
  public void addJarFile(File sf)
  {
    _jarFiles.add(sf);
  }
  
  public String makeProjectFile()
  {
    StringBuffer tbr = new StringBuffer();
    makeEntries(tbr, _sourceFiles, "Source", true);
    makeEntries(tbr, _resourceFiles, "Resources", true);
    makeEntries(tbr, _buildDirFiles, "BuildDir", false);
    //makeEntries(tbr, _classpathFiles, "Classpath", false);
    tbr.append("(Classpath\n)");
    makeEntries(tbr, _jarFiles, "Jar", true);
    return tbr.toString();
  }
  
  private void makeEntries(StringBuffer buffer, Vector<File> files, String roottag, boolean checkrelpath)
  {
    buffer.append("(" + roottag + "\n");
    
    for(int i = 0; i<files.size(); i++)
    {
      //System.out.println(files.get(i).getName());
      try
      {
        String path = files.elementAt(i).getCanonicalPath();
        path = path.replace('\\', '/');
        if(path.startsWith(_projectPath) || !checkrelpath)
        {
          if(checkrelpath)
          {
            path = path.substring(_projectPath.length());
          }
          buffer.append("  (" + path + ")\n");
        }
      }
      catch(IOException e)
      {
      }
      
    }
    buffer.append(")\n");
  }
  
  public Vector<File> getSourceFiles() {
    return _sourceFiles;
  }
  
  public Vector<File> getResourceFiles() {
    return _resourceFiles;
  }
  
  public Vector<File> getBuildDirFiles() {
    return _buildDirFiles;
  }
  
  public Vector<File> getClasspathFiles() {
    return _classpathFiles;
  }
  
  public Vector<File> getJarFiles() {
    return _jarFiles;
  }
  
  public String getProjectPath() {
    return _projectPath;
  }
}