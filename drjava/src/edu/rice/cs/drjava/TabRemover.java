package edu.rice.cs.drjava;

import java.util.ArrayList;
import java.util.ListIterator;
import java.io.*;

public class TabRemover {
  public static void main(String[] args) {
    
    
    if (args.length == 2) {
      try {
        FileReader source = new FileReader(args[0]);
        BufferedReader bufferedSource = new BufferedReader(source);
      
        // Source data must be held in a temp in case the target file is the source file.
        ArrayList temp = new ArrayList();
        
        for (int next = bufferedSource.read(); next != -1; next = bufferedSource.read()) {
          if ((char)next != '\t') {
            System.err.println(next);
            temp.add(new Character((char)next));
          }
        }
        bufferedSource.close();
        
        FileWriter target = new FileWriter(args[1]);
        BufferedWriter bufferedTarget = new BufferedWriter(target);
        ListIterator i = temp.listIterator();
        
        while(i.hasNext()) {
          bufferedTarget.write(((Character)i.next()).charValue());
        }
        bufferedTarget.flush();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
    else {
      System.err.println("TabRemover usage: java TabRemover <source file> <target file>");
    }
  }
}
      