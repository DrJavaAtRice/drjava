/* $Id$ */

package edu.rice.cs.drjava;

public interface JavaInterpreter
{
	public static final Object NO_RESULT = new Object();
	
	public Object interpret(String s);
  public void addClassPath(String path);
}
