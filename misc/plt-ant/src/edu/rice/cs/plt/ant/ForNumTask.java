/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * This file is based on ForTask.java from the Ant-Contrib project.
 *
 * Copyright (c) 2003-2005 Ant-Contrib project.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.ant;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.MacroDef;
import org.apache.tools.ant.taskdefs.MacroInstance;
import org.apache.tools.ant.taskdefs.Parallel;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

/***
 * Task definition for the for task.  This is based on
 * the foreach task but takes a sequential element
 * instead of a target and only works for ant >= 1.6Beta3
 * @author Peter Reilly
 * @author Mathias Ricken
 */
public class ForNumTask extends Task {

    private String     list;
    private Integer    count;
    private String     param;
    private String     delimiter = ",";
    private Path       currPath;
    private boolean    trim;
    private boolean    keepgoing = false;
    private MacroDef   macroDef;
    private List       hasIterators = new ArrayList();
    private boolean    parallel = false;
    private Integer    threadCount;
    private Parallel   parallelTasks;

    /**
     * Creates a new <code>ForNum</code> instance.
     * This checks if the ant version is correct to run this task.
     */
    public ForNumTask() {
    }

    /**
     * Attribute whether to execute the loop in parallel or in sequence.
     * @param parallel if true execute the tasks in parallel. Default is false.
     */
    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    /***
     * Set the maximum amount of threads we're going to allow
     * to execute in parallel
     * @param threadCount the number of threads to use
     */
    public void setThreadCount(int threadCount) {
        if (threadCount < 1) {
            throw new BuildException("Illegal value for threadCount " + threadCount
                                     + " it should be > 0");
        }
        this.threadCount = new Integer(threadCount);
    }

    /**
     * Set the trim attribute.
     *
     * @param trim if true, trim the value for each iterator.
     */
    public void setTrim(boolean trim) {
        this.trim = trim;
    }

    /**
     * Set the keepgoing attribute, indicating whether we
     * should stop on errors or continue heedlessly onward.
     *
     * @param keepgoing a boolean, if <code>true</code> then we act in
     * the keepgoing manner described.
     */
    public void setKeepgoing(boolean keepgoing) {
        this.keepgoing = keepgoing;
    }

    /**
     * Set the list attribute.
     *
     * @param list a list of delimiter separated tokens.
     */
    public void setList(String list) {
        this.list = list;
    }

    /**
     * Set the count attribute.
     *
     * @param count an integer number.
     */
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * Set the delimiter attribute.
     *
     * @param delimiter the delimiter used to separate the tokens in
     *        the list attribute. The default is ",".
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Set the param attribute.
     * This is the name of the macrodef attribute that
     * gets set for each iterator of the sequential element.
     *
     * @param param the name of the macrodef attribute.
     */
    public void setParam(String param) {
        this.param = param;
    }

    private Path getOrCreatePath() {
        if (currPath == null) {
            currPath = new Path(getProject());
        }
        return currPath;
    }

    /**
     * This is a path that can be used instread of the list
     * attribute to interate over. If this is set, each
     * path element in the path is used for an interator of the
     * sequential element.
     *
     * @param path the path to be set by the ant script.
     */
    public void addConfigured(Path path) {
        getOrCreatePath().append(path);
    }

    /**
     * This is a path that can be used instread of the list
     * attribute to interate over. If this is set, each
     * path element in the path is used for an interator of the
     * sequential element.
     *
     * @param path the path to be set by the ant script.
     */
    public void addConfiguredPath(Path path) {
        addConfigured(path);
    }

    /**
     * @return a MacroDef#NestedSequential object to be configured
     */
    public Object createSequential() {
        macroDef = new MacroDef();
        macroDef.setProject(getProject());
        return macroDef.createSequential();
    }

    /**
     * Run the fornum task.
     * This checks the attributes and nested elements, and
     * if there are ok, it calls doTheTasks()
     * which constructes a macrodef task and a
     * for each interation a macrodef instance.
     */
    public void execute() {
        if (parallel) {
            parallelTasks = (Parallel) getProject().createTask("parallel");
            if (threadCount != null) {
                parallelTasks.setThreadCount(threadCount.intValue());
            }
        }
        if (list == null && count == null && currPath == null && hasIterators.size() == 0) {
            throw new BuildException(
                "You must have a list, count or path to iterate through");
        }
        if (param == null) {
            throw new BuildException(
                "You must supply a property name to set on"
                + " each iteration in param");
        }
        if (macroDef == null) {
            throw new BuildException(
                "You must supply an embedded sequential "
                + "to perform");
        }
        doTheTasks();
        if (parallel) {
            parallelTasks.perform();
        }
    }


    private void doSequentialIteration(String val) {
        MacroInstance instance = new MacroInstance();
        instance.setProject(getProject());
        instance.setOwningTarget(getOwningTarget());
        instance.setMacroDef(macroDef);
        instance.setDynamicAttribute(param.toLowerCase(),
                                     val);
        if (!parallel) {
            instance.execute();
        } else {
            parallelTasks.addTask(instance);
        }
    }

    private void doTheTasks() {
        int errorCount = 0;
        int taskCount = 0;

        // Create a macro attribute
        if (macroDef.getAttributes().isEmpty()) {
         MacroDef.Attribute attribute = new MacroDef.Attribute();
         attribute.setName(param);
         macroDef.addConfiguredAttribute(attribute);
        }
        
        // Take Care of the list attribute
        if (list != null) {
            StringTokenizer st = new StringTokenizer(list, delimiter);

            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                if (trim) {
                    tok = tok.trim();
                }
                try {
                    taskCount++;
                    doSequentialIteration(tok);
                } catch (BuildException bx) {
                    if (keepgoing) {
                        log(tok + ": " + bx.getMessage(), Project.MSG_ERR);
                        errorCount++;
                    } else {
                        throw bx;
                    }
                }
            }
        }
        if (keepgoing && (errorCount != 0)) {
            throw new BuildException(
                "Keepgoing execution: " + errorCount
                + " of " + taskCount + " iterations failed.");
        }
        
        // Take Care of the count attribute
        if (count != null) {
   for (int i=0; i<count.intValue(); ++i) {
            try {
              taskCount++;
              doSequentialIteration(String.valueOf(i));
            } catch (BuildException bx) {
              if (keepgoing) {
                log(i + ": " + bx.getMessage(), Project.MSG_ERR);
                errorCount++;
              } else {
                throw bx;
              }
            }
          }
        }
        if (keepgoing && (errorCount != 0)) {
          throw new BuildException(
                                   "Keepgoing execution: " + errorCount
                                     + " of " + taskCount + " iterations failed.");
        }
        
        // Take Care of the path element
        String[] pathElements = new String[0];
        if (currPath != null) {
            pathElements = currPath.list();
        }
        for (int i = 0; i < pathElements.length; i++) {
            File nextFile = new File(pathElements[i]);
            try {
                taskCount++;
                doSequentialIteration(nextFile.getAbsolutePath());
            } catch (BuildException bx) {
                if (keepgoing) {
                 log(nextFile + ": " + bx.getMessage(), Project.MSG_ERR);
                    errorCount++;
                } else {
                    throw bx;
                }
            }
        }
        if (keepgoing && (errorCount != 0)) {
            throw new BuildException(
                "Keepgoing execution: " + errorCount
                + " of " + taskCount + " iterations failed.");
        }

        // Take care of iterators
        for (Iterator i = hasIterators.iterator(); i.hasNext();) {
            Iterator it = ((HasIterator) i.next()).iterator();
            while (it.hasNext()) {
             String s = it.next().toString();
                try {
                    taskCount++;
                    doSequentialIteration(s);
                } catch (BuildException bx) {
                    if (keepgoing) {
                     log(s + ": " + bx.getMessage(), Project.MSG_ERR);
                        errorCount++;
                    } else {
                        throw bx;
                    }
                }
            }
        }
        if (keepgoing && (errorCount != 0)) {
            throw new BuildException(
                "Keepgoing execution: " + errorCount
                + " of " + taskCount + " iterations failed.");
        }
    }

    /**
     * Add a Map, iterate over the values
     *
     * @param map a Map object - iterate over the values.
     */
    public void add(Map map) {
        hasIterators.add(new MapIterator(map));
    }

    /**
     * Add a fileset to be iterated over.
     *
     * @param fileset a <code>FileSet</code> value
     */
    public void add(FileSet fileset) {
        getOrCreatePath().addFileset(fileset);
    }

    /**
     * Add a fileset to be iterated over.
     *
     * @param fileset a <code>FileSet</code> value
     */
    public void addFileSet(FileSet fileset) {
        add(fileset);
    }

    /**
     * Add a dirset to be iterated over.
     *
     * @param dirset a <code>DirSet</code> value
     */
    public void add(DirSet dirset) {
        getOrCreatePath().addDirset(dirset);
    }

    /**
     * Add a dirset to be iterated over.
     *
     * @param dirset a <code>DirSet</code> value
     */
    public void addDirSet(DirSet dirset) {
        add(dirset);
    }

    /**
     * Add a collection that can be iterated over.
     *
     * @param collection a <code>Collection</code> value.
     */
    public void add(Collection collection) {
        hasIterators.add(new ReflectIterator(collection));
    }

    /**
     * Add an iterator to be iterated over.
     *
     * @param iterator an <code>Iterator</code> value
     */
    public void add(Iterator iterator) {
        hasIterators.add(new IteratorIterator(iterator));
    }

    /**
     * Add an object that has an Iterator iterator() method
     * that can be iterated over.
     *
     * @param obj An object that can be iterated over.
     */
    public void add(Object obj) {
        hasIterators.add(new ReflectIterator(obj));
    }

    /**
     * Interface for the objects in the iterator collection.
     */
    private interface HasIterator {
        Iterator iterator();
    }

    private static class IteratorIterator implements HasIterator {
        private Iterator iterator;
        public IteratorIterator(Iterator iterator) {
            this.iterator = iterator;
        }
        public Iterator iterator() {
            return this.iterator;
        }
    }

    private static class MapIterator implements HasIterator {
        private Map map;
        public MapIterator(Map map) {
            this.map = map;
        }
        public Iterator iterator() {
            return map.values().iterator();
        }
    }

    private static class ReflectIterator implements HasIterator {
        private Object  obj;
        private Method  method;
        public ReflectIterator(Object obj) {
            this.obj = obj;
            try {
                method = obj.getClass().getMethod(
                    "iterator", new Class[] {});
            } catch (Throwable t) {
                throw new BuildException(
                    "Invalid type " + obj.getClass() + " used in ForNum task, it does"
                    + " not have a public iterator method");
            }
        }

        public Iterator iterator() {
            try {
                return (Iterator) method.invoke(obj, new Object[] {});
            } catch (Throwable t) {
                throw new BuildException(t);
            }
        }
    }
}
