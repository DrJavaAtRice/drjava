package edu.rice.cs.drjava.project;
import java.io.*;
import java.util.*;

public class ProjectFileBuilder
{
  
  private Vector<File> _sourcefiles = new Vector<File>();
  
  private Vector<File> _resourcefiles = new Vector<File>();
  
  private Vector<File> _builddirfiles = new Vector<File>();
  
  private Vector<File> _classpathfiles = new Vector<File>();
  
  private Vector<File> _jarfiles = new Vector<File>();
 
  private String _projectpath;
  
  public ProjectFileBuilder(String projectpath)
  {
    _projectpath = projectpath.replace('\\', '/');
  }
  
  public void addSourceFile(File sf)
  {
    _sourcefiles.add(sf);
  }
  
  public void addResourceFile(File sf)
  {
    _resourcefiles.add(sf);
  }
  public void setBuildDir(File sf)
  {
    _builddirfiles.clear();
    _builddirfiles.add(sf);
  }
  public void addClasspathFile(File sf)
  {
    _classpathfiles.add(sf);
  }
  
  public void addJarFile(File sf)
  {
    _jarfiles.add(sf);
  }
  
  public String makeProjectFile()
  {
    StringBuffer tbr = new StringBuffer();
    makeEntries(tbr, _sourcefiles, "Source", true);
    makeEntries(tbr, _resourcefiles, "Resources", true);
    makeEntries(tbr, _builddirfiles, "BuildDir", true);
    //makeEntries(tbr, _classpathfiles, "Classpath", false);
    tbr.append("(Classpath\n)");
    makeEntries(tbr, _jarfiles, "Jar", true);
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
        if(path.startsWith(_projectpath) || !checkrelpath)
        {
          if(checkrelpath)
          {
            path = path.substring(_projectpath.length());
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
  
}