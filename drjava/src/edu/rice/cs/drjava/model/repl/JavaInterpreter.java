/* $Id$ */

package edu.rice.cs.drjava;

public interface JavaInterpreter
{
  public Object interpret(String s);
  public void addClassPath(String path);
}
