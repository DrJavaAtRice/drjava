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
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.ant;

import java.io.*;
import java.util.*;
import java.text.*;
import org.jdom.*;
import org.jdom.input.*;

public class JUnitReportGenerator {
  
  private static TreeMap/*<String,HashMap<String,Double>>*/ times = new TreeMap/*<String,HashMap<String,Double>>*/();
  private static ArrayList/*<String>*/ revs = new ArrayList/*<String>*/();
  
  public static void main(String[] args) throws Exception {
    
    File[] revDirs = new File( "benchmarkResults" ).listFiles( new FileFilter() {
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    });
    Arrays.sort(revDirs);
    
    for (int i=0; i<revDirs.length;i++)
      parseRevisionResults( revDirs[i].getName(), revDirs[i].getPath() );
    
    createHTMLFile("report.html");
    
  }
  
  public static void parseRevisionResults(String name,final String loc) throws Exception {
    revs.add(name);
    File[] files = new File( loc ).listFiles( new FilenameFilter() {
      public boolean accept(File dir,String filename) {
        return filename.matches("^TEST-.*\\.xml$");
      }
    });
    
    for (int i=0; i<files.length;i++) {
      parseTestCaseResult( name, files[i] );
    }
  }
  
  public static void parseTestCaseResult(String revName,File file) throws Exception {
    Document doc = new SAXBuilder().build( file );
    Element testSuiteElt = doc.getRootElement();
    String testName = testSuiteElt.getAttribute("name").getValue();
    testName = testName.substring(testName.lastIndexOf(".")+1);
    if (!times.containsKey(testName)) times.put(testName,new HashMap/*<String,Double>*/());
    ((HashMap)times.get(testName)).put(revName, testSuiteElt.getAttribute("time").getValue());
  }
  
  public static void createHTMLFile(String filename) throws Exception {
    DecimalFormat output = new DecimalFormat("0.000");
    PrintWriter fout = new PrintWriter( new FileWriter(filename) );
    fout.println("<font size=5><b>Results of Benchmark</b></font><br><br>");
    fout.println("<table border=0 cellpadding=10><tr><td valign=top>");
    fout.println("<table border=1 cellpadding=3 cellspacing=0><tr><td><b>Test Case</b></td>");
    Iterator itr = revs.iterator();
    while (itr.hasNext()) {
      String rev = (String)itr.next();
      fout.print("<td><b>"+rev+"</b></td>");
    }
    fout.println("</tr>");
    itr = times.keySet().iterator();
    while (itr.hasNext()) {
      String test = (String)itr.next();
      fout.println("<tr><td>"+test+"</td>");
      HashMap/*<String,Double>*/ results = (HashMap)times.get(test);
      double last = -1;
      Iterator itr2 = revs.iterator();
      while (itr2.hasNext()) {
        String rev = (String)itr2.next();
        String resD = (String)results.get(rev);
        if (resD!=null) {
          double res = Double.parseDouble(resD);
          if (last!=-1)
            fout.print("<td align=right bgcolor="+pctToColor(res/last)+"><font face=monospace>"+output.format(res)+"</td>");
          else
            fout.print("<td align=right><font face=monospace>"+output.format(res)+"</td>");
          last = res;
        } 
        else
          fout.print("<td bgcolor=black align=center><font face=monospace color=white>No Result</font></td>");
      }
      fout.println("</tr>");
    }
    // Output legend
    fout.println("</table></td><td valign=top><table border=1 cellpadding=3 cellspacing=0><tr><td align=center width=200><b>Legend</b></td></tr>");
    fout.println("<tr><td align=center bgcolor="+pctToColor(1.3)+">>30% increase</td></tr>");
    fout.println("<tr><td align=center bgcolor="+pctToColor(1.2)+">20-30% increase</td></tr>");
    fout.println("<tr><td align=center bgcolor="+pctToColor(1.1)+">10-20% increase</td></tr>");
    fout.println("<tr><td align=center bgcolor="+pctToColor(1.05)+">5-10% increase</td></tr>");
    fout.println("<tr><td align=center bgcolor="+pctToColor(.95)+">Within 5%</td></tr>");
    fout.println("<tr><td align=center bgcolor="+pctToColor(.9)+">5-10% decrease</td></tr>");
    fout.println("<tr><td align=center bgcolor="+pctToColor(.8)+">10-20% decrease</td></tr>");
    fout.println("<tr><td align=center bgcolor="+pctToColor(.7)+">20-30% decrease</td></tr>");
    fout.println("<tr><td align=center bgcolor="+pctToColor(0)+">>30% decrease</td></tr>");
    fout.println("</table></td></tr></table>");
    fout.close();
  }
  
  public static String pctToColor(double pct) {
    if (pct>=1.3) return "#FF6F6F";  // red
    if (pct>=1.2) return "#FF9F7F";  // orange
    if (pct>=1.1) return "#FFD07F";  // yellow-orange
    if (pct>=1.05) return "#FFFF7F"; // yellow
    if (pct>=.95) return "#FFFFFF";  // white
    if (pct>=.9) return "#99FF6F";   // light green
    if (pct>=.8) return "#6FD399";   // darker green
    if (pct>=.7) return "#88AAFF";   // blue
    return "#BB88FF";                // purple
  }
  
}