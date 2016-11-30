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

package edu.rice.cs.javalanglevels;

import edu.rice.cs.javalanglevels.tree.*;
import edu.rice.cs.javalanglevels.parser.JExprParser;
import java.util.*;

import junit.framework.TestCase;

/**
 * Represents the data for a piece (or pieces) of a package name.  This is used as a temporary
 * building block as we are discovering what class is referenced.
 */
public class PackageData extends TypeData {
  
  /**Constructor for PackageData.  First piece of name--just takes in the String*/
  public PackageData(String s) {
    super(null);
    _name = s;
  }
  
  /**Constructor for PackageData.  Takes in the package data that is on the lhs, and the name on the rhs,
   * and combines the names to get the name of this package data.
   */
  public PackageData(PackageData pd, String s) {
    super(null);
    _name = pd.getName() + '.' + s;
  }
  
  /** for now, throw a big error, becuase you should have known better. */ 
  public boolean isInstanceType() {
    throw new UnsupportedOperationException("Internal Program Error: Attempt to call isInstanceType() on a PackageData.  Please report this bug.");
  }
 
 
 /**@return the class SymbolData corresponding to this TypeData.*/
  public SymbolData getSymbolData() {
    //for now, throw a big error, becuase you should have known better.
    throw new UnsupportedOperationException("Internal Program Error: Attempt to call getSymbolData() on a PackageData.  Please report this bug.");
  }
 
 /**@return the InstanceData corresponding to this TypeData.*/
  public InstanceData getInstanceData() {
    //for now, throw a big error, becuase you should have known better.
    throw new UnsupportedOperationException("Internal Program Error: Attempt to call getInstanceData() on a PackageData.  Please report this bug.");
  }
  
}