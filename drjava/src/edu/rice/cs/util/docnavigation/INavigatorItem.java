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

package edu.rice.cs.util.docnavigation;

/**
 * <code>INavigatorItem</code> models (very abstractly) some entity that is
 * eligible for insertion and removal inside an
 * <code>IDocumentNavigator</code>. Conceptually, if an
 * <code>IDocumentNavigator</code> represented a file cabinet then each file in
 * the cabinet would be an <code>INavigatorItem</code>. If an
 * <code>IDocumentNavigator</code> represented a wallet then each credit card
 * and driver's license would be an <code>INavigatorItem</code>. <p> The
 * only real responsibilities a class implementing <code>INavigatorItem</code> has is
 * to provide a simple <code>String</code> representation of itself via the
 * <code>getName</code> method and to devise some notion of equality.
 */
public interface INavigatorItem {
  
  public boolean checkIfClassFileInSync();
  public boolean fileExists();
  
  /** Returns a "simple" name representing this <code>INavigatorItem</code>.
    * Strings returned by this method may or may not be unique with respect to other <code>INavigatorItem</code>s within or without
    * a given <code>IDocumentNavigator</code>.
    * @return the simple name for this document.
    */
  public String getName();
  public boolean isAuxiliaryFile();
  public boolean inProject();
  public boolean isUntitled();
}
