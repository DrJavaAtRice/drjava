package edu.rice.cs.drjava.model.repl.newjvm

import java.io.{ BufferedReader, PrintWriter }
import scala.tools.nsc.interpreter.ILoop
import scala.tools.nsc.util.ClassPath
import scala.tools.nsc.io.File
import scala.tools.nsc.io.File.pathSeparator
import scala.collection.mutable.ListBuffer
// import scala.tools.nsc.interpreter.LoopCommands.LoopCommand

class DrScalaILoop(r: BufferedReader, w: PrintWriter) 
extends ILoop(r, w) with ScalaInterpreterAdapter { 
  lazy val addedClasspaths = (ListBuffer[String]() ++
    settings.classpath.value.split(pathSeparator).filterNot(_ == ""))
  override def addClasspath(arg: String): Unit = {
    val f = File(arg).normalize
    val fPath = f.path
    if (!addedClasspaths.contains(fPath)) {
      if (!f.exists)
        echo("The path '" + f + "' doesn't seem to exist.")
      else {
        addedClasspaths += fPath
        //replay()
        closeInterpreter()
        createInterpreter()
        //command("1 + 1")
      }
    }
    // else System.err.println("Ignoring duplicate path, " + fPath)
  }
  override def createInterpreter() {
    val pathsToAdd = (addedClasspaths.filterNot(
      settings.classpath.value.split(pathSeparator).toSet.contains(_)))
    settings.classpath append pathsToAdd.mkString(pathSeparator)
    // System.err.println("Creating new interpreter with classpath: " + settings.classpath.value)
    this.intp = new ILoopInterpreter
  }
  def reset() {
    closeInterpreter()
    createInterpreter()
  }
  override def commands: List[LoopCommand] = 
  (LoopCommand.nullary("reset","reset Interpreter",() => reset()) :: 
    this.standardCommands)
// ++ { if (isReplPower) powerCommands else Nil }
}
