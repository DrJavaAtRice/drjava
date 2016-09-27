/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2015, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.swing;

import javax.swing.table.*;
import java.util.ArrayList;

/** Common TableModel for Uneditable tables
 *  @version $Id: UneditableTableModel.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class UneditableTableModel extends DefaultTableModel {
  public UneditableTableModel() { super(); }
  public UneditableTableModel(int rowCount, int columnCount) { super(rowCount,columnCount); }
  public UneditableTableModel(Object[][] data, Object[] columnNames) { super(data, columnNames); }
  public UneditableTableModel(ArrayList<String> columnNames, int rowCount) { 
    super(Utilities.toStringArray(columnNames), rowCount); 
  }
  
  /* Returns Object[][] corresponding to data (an ArrayList<ArrayList<Object>>) */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private static Object[][] toArrayOfArray(ArrayList<ArrayList<Object>> data) {
    int size = data.size();
    ArrayList<Object>[] buffer = (ArrayList<Object>[]) data.toArray(new ArrayList[size]);
    Object[][] result = new Object[size][];  // result is an array with size elements; each element has type Object[]
    for (int i = 0; i < size; i++) { result[i] = buffer[i].toArray(); }
    return result;
  }
      
  public UneditableTableModel(ArrayList<ArrayList<Object>> data, ArrayList<String> columnNames) { 
    super(toArrayOfArray(data),columnNames.toArray()); }
  public boolean isCellEditable(int row, int col) { return false; }
}
