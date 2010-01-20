/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (javaplt@rice.edu)
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

package edu.rice.cs.util;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

/**
 * XML configuration management.
 * <p/>
 * This class uses DOM paths of a specific form to refer to nodes in the XML document.
 * Consider this XML structure:
 * <foo a="foo.a">
 *   <bar>abc</bar>
 *   <fum fee="xyz">def</fum>
 * </foo>
 * The path "foo/bar" refers to the value "abc".
 * The path "foo/fum" refers to the value "def".
 * If this form is used, there may be only #text or #comment nodes in the node. All #text nodes will be
 * concatenated and then stripped of whitespace at the beginning and the end.
 * The path "foo/fum.fee" refers to the value "xyz".
 * The path "foo.a" refers to the value "foo.a".
 *
 * When using getMultiple, any node or attribute name can be substituted with "*" to get all elements:
 * The path "foo/*" returns both the value "abc" and "def".
 * @author Mathias Ricken
 */
public class XMLConfig {
  /** Newline string.
   */
  public static final String NL = System.getProperty("line.separator");
  
  /** XML document.
   */
  private Document _document;
  
  /** XMLConfig to delegate to, or null.
   */
  private XMLConfig _parent = null;
  
  /** Node where this XMLConfig starts if delegation is used, or null.
   */
  private Node _startNode = null;
  
  /** Creates an empty configuration.
   */
  public XMLConfig() {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      _document = builder.newDocument();  // Create from whole cloth
      // NOTE: not 1.4 compatible -- _document.setXmlStandalone(true);
    }
    catch(ParserConfigurationException e) {
      e.printStackTrace();
    }
  }
  
  /** Creates a configuration from an input stream.
   * @param is input stream
   */
  public XMLConfig(InputStream is) {
    init(new InputSource(is));
  }
  
  /** Creates a configuration from a reader.
   * @param r reader
   */
  public XMLConfig(Reader r) {
    init(new InputSource(r));
  }
  
  /** Creates a configuration that is a part of another configuration, starting at the specified node.
   * @param parent the configuration that contains this part
   * @param node the node in the parent configuration where this part starts
   */
  public XMLConfig(XMLConfig parent, Node node) {
    if ((parent == null) || (node == null)) { throw new XMLConfigException("Error in ctor: parent or node is null"); }
    _parent = parent;
    _startNode = node;
    _document = null;
  }
  
  /** Initialize this XML configuration.
   * @param is the XML input source
   */
  private void init(InputSource is) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    try {
      builder = factory.newDocumentBuilder();
      _document = builder.parse(is);
      // NOTE: not 1.4 compatible -- _document.setXmlStandalone(true);
    }
    catch(Exception e) {
      throw new XMLConfigException("Error in ctor", e);
    }
    _document.normalize();
  }
  
  /** Creates a configuration from a file.
   * @param f file
   */
  public XMLConfig(File f) {
    try {
      init(new InputSource(new FileInputStream(f)));
    }
    catch(FileNotFoundException e) {
      throw new XMLConfigException("Error in ctor", e);
    }
  }
  
  /** Creates a configuration from a file name.
   * @param filename file name
   */
  public XMLConfig(String filename)  {
    try {
      init(new InputSource(new FileInputStream(filename)));
    }
    catch(FileNotFoundException e) {
      throw new XMLConfigException("Error in ctor", e);
    }
  }
  
  public boolean isDelegated() { return (_parent != null); }
  
  /** Saves configuration to an output stream
   * @param os output stream
   */
  public void save(OutputStream os) {
    if (isDelegated()) { _parent.save(os); return; }
    
    // Prepare the DOM document for writing
    Source source = new DOMSource(_document);
    /*
     // Prepare the output file
     Result result = new StreamResult(os);
     */
    // Write the DOM document to the file
    try {
      TransformerFactory tf = TransformerFactory.newInstance();
      tf.setAttribute("indent-number", Integer.valueOf(2));
      Transformer t = tf.newTransformer();
      t.setOutputProperty(OutputKeys.INDENT, "yes");
      t.transform(source, new StreamResult(new OutputStreamWriter(os, "utf-8")));
      /*            
       Transformer xformer = TransformerFactory.newInstance().newTransformer();
       xformer.setOutputProperty(OutputKeys.INDENT, "yes");
       xformer.transform(source, result);
       */
    }
    catch(TransformerException e) {
      throw new XMLConfigException("Error in save", e);
    }
    catch(UnsupportedEncodingException e) {
      throw new XMLConfigException("Error in save", e);
    }
  }
  
  /** Saves configuration to a file.
   * @param f file
   */
  public void save(File f) {
    if (isDelegated()) { _parent.save(f); return; }
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(f);
      save(fos);
    }
    catch(FileNotFoundException e) {
      throw new XMLConfigException("Error in save", e);
    }
    finally {
      try {
        if (fos != null) fos.close();
      }
      catch(IOException ioe) { /* ignore exception when closing */ }
    }
  }
  
  /** Saves configuration to a file specified by a file name.
   * @param filename file name
   */
  public void save(String filename) {
    save(new File(filename));
  }
  
  // ----- String ------
  
  /** Returns the value as specified by the DOM path.
   * @param path DOM path
   * @return value.
   */
  public String get(String path) {
    List<String> r = getMultiple(path);
    if (r.size() != 1) throw new XMLConfigException("Number of results != 1");
    return r.get(0);
  }

  /** Returns the value as specified by the DOM path.
   * @param path DOM path
   * @param root node where the search should start
   * @return value.
   */
  public String get(String path, Node root) {
    List<String> r = getMultiple(path, root);
    if (r.size() != 1) throw new XMLConfigException("Number of results != 1");
    return r.get(0);
  }
  
    /** Returns the value as specified by the DOM path, or the default value if the value could not be found.
   * @param path DOM path
   * @param defaultVal default value in case value is not in DOM
   * @return value.
   */
  public String get(String path, String defaultVal) {
    try {
      return get(path);
    }
    catch(XMLConfigException e) {
      return defaultVal;
    }
  }
  
  /** Returns the value as specified by the DOM path, or the default value if the value could not be found.
   * @param path DOM path
   * @param root node where the search should start
   * @param defaultVal default value in case value is not in DOM
   * @return value.
   */
  public String get(String path, Node root, String defaultVal) {
    try {
      return get(path, root);
    }
    catch(XMLConfigException e) {
      return defaultVal;
    }
  }
  
  // ----- Integer ------
  
  /** Returns the value as specified by the DOM path.
   * @param path DOM path
   * @return value.
   * @throws IllegalArgumentException
   */
  public int getInt(String path) {
    List<String> r = getMultiple(path);
    if (r.size() != 1) throw new XMLConfigException("Number of results != 1");
    try {
      return Integer.valueOf(r.get(0));
    }
    catch(NumberFormatException nfe) { throw new IllegalArgumentException("Not an integer value.", nfe); }
  }

  /** Returns the value as specified by the DOM path.
   * @param path DOM path
   * @param root node where the search should start
   * @return value.
   * @throws IllegalArgumentException
   */
  public int getInt(String path, Node root) {
    List<String> r = getMultiple(path, root);
    if (r.size() != 1) throw new XMLConfigException("Number of results != 1");
    try {
      return Integer.valueOf(r.get(0));
    }
    catch(NumberFormatException nfe) { throw new IllegalArgumentException("Not an integer value.", nfe); }
  }
  
  /** Returns the value as specified by the DOM path, or the default value if the value could not be found.
   * @param path DOM path
   * @param defaultVal default value in case value is not in DOM
   * @return value.
   * @throws IllegalArgumentException
   */
  public int getInt(String path, int defaultVal) {
    try {
      return getInt(path);
    }
    catch(XMLConfigException e) {
      return defaultVal;
    }
  }
  
  /** Returns the value as specified by the DOM path, or the default value if the value could not be found.
   * @param path DOM path
   * @param root node where the search should start
   * @param defaultVal default value in case value is not in DOM
   * @return value.
   * @throws IllegalArgumentException
   */
  public int getInt(String path, Node root, int defaultVal) {
    try {
      return getInt(path, root);
    }
    catch(XMLConfigException e) {
      return defaultVal;
    }
  }

  // ----- Boolean ------
  
  /** Returns the value as specified by the DOM path.
   * @param path DOM path
   * @return value.
   * @throws IllegalArgumentException
   */
  public boolean getBool(String path) {
    List<String> r = getMultiple(path);
    if (r.size() != 1) throw new XMLConfigException("Number of results != 1");
    String s = r.get(0).toLowerCase().trim();
    if ((s.equals("true")) ||
        (s.equals("yes")) ||
        (s.equals("on"))) return true;
    if ((s.equals("false")) ||
        (s.equals("no")) ||
        (s.equals("off"))) return false;
    throw new IllegalArgumentException("Not a Boolean vlaue.");
  }

  /** Returns the value as specified by the DOM path.
   * @param path DOM path
   * @param root node where the search should start
   * @return value.
   * @throws IllegalArgumentException
   */
  public boolean getBool(String path, Node root) {
    List<String> r = getMultiple(path, root);
    if (r.size() != 1) throw new XMLConfigException("Number of results != 1");
    String s = r.get(0).toLowerCase().trim();
    if ((s.equals("true")) ||
        (s.equals("yes")) ||
        (s.equals("on"))) return true;
    if ((s.equals("false")) ||
        (s.equals("no")) ||
        (s.equals("off"))) return false;
    throw new IllegalArgumentException("Not a Boolean vlaue.");

  }
  
  /** Returns the value as specified by the DOM path, or the default value if the value could not be found.
   * @param path DOM path
   * @param defaultVal default value in case value is not in DOM
   * @return value.
   * @throws IllegalArgumentException
   */
  public boolean getBool(String path, boolean defaultVal) {
    try {
      return getBool(path);
    }
    catch(XMLConfigException e) {
      return defaultVal;
    }
  }
  
  /** Returns the value as specified by the DOM path, or the default value if the value could not be found.
   * @param path DOM path
   * @param root node where the search should start
   * @param defaultVal default value in case value is not in DOM
   * @return value.
   * @throws IllegalArgumentException
   */
  public boolean getBool(String path, Node root, boolean defaultVal) {
    try {
      return getBool(path, root);
    }
    catch(XMLConfigException e) {
      return defaultVal;
    }
  }
  
  // ----- Other -----
  
  /** Returns the value as specified by the DOM path.
   * @param path DOM path
   * @return list of values.
   */
  public List<String> getMultiple(String path) {
    if (isDelegated()) { return getMultiple(path, _startNode); }

    return getMultiple(path, _document);
  }
  
  /** Returns the value as specified by the DOM path.
   * @param path DOM path
   * @param root node where the search should start
   * @return list of values.
   */
  public List<String> getMultiple(String path, Node root) {
    List<Node> accum = getNodes(path, root);
    List<String> strings = new LinkedList<String>();
    for(Node n: accum) {
      if (n instanceof Attr) {
        strings.add(n.getNodeValue());
      }
      else {
        Node child;
        String acc = "";
        child = n.getFirstChild();
        while(child != null) {
          if (child.getNodeName().equals("#text")) {
            acc += " " + child.getNodeValue();
          }
          else if (child.getNodeName().equals("#comment")) {
            // ignore
          }
          else {
            throw new XMLConfigException("Node " + n.getNodeName() + " contained node " + child.getNodeName() + ", but should only contain #text and #comment.");
          }
          child = child.getNextSibling();
        }
        strings.add(acc.trim());
      }
    }
    return strings;
  }
  
  /** Returns the nodes as specified by the DOM path.
   * @param path DOM path
   * @return list of nodes.
   */
  public List<Node> getNodes(String path) {
    if (isDelegated()) { return getNodes(path, _startNode); }

    return getNodes(path, _document);
  }
  
  /** Returns the nodes as specified by the DOM path.
   * @param path DOM path
   * @param root node where the search should start
   * @return list of nodes.
   */
  public List<Node> getNodes(String path, Node root) {
    List<Node> accum = new LinkedList<Node>();
    getMultipleHelper(path, root, accum, false);
    return accum;
  }
  
  /** Returns the value as specified by the DOM path.
   * @param path DOM path
   * @param n node where the search begins
   * @param accum accumulator
   * @param dotRead whether a dot has been read
   */
  private void getMultipleHelper(String path, Node n, List<Node> accum, boolean dotRead) {
    int dotPos = path.indexOf('.');
    boolean initialDot = (dotPos == 0);
    if ((path.length() > 0) && (dotPos == -1) && (!path.endsWith("/"))) {
      path = path + "/";
    }
    int slashPos = path.indexOf('/');
    
    if(dotPos != -1 && path.indexOf('.', dotPos+1) != -1)
      throw new XMLConfigException("An attribute cannot have subparts (foo.bar.fum and foo.bar/fum not allowed)");
    
    if(dotPos != -1 && path.indexOf('/', dotPos+1) != -1)
      throw new XMLConfigException("An attribute cannot have subparts (foo.bar.fum and foo.bar/fum not allowed)");
    
    if (((slashPos > -1) || (dotPos > -1)) && !dotRead || initialDot)  {
      String nodeName;
      if ((slashPos > -1) && ((dotPos == -1) || (slashPos < dotPos))) {
        nodeName = path.substring(0, slashPos);
        path = path.substring(slashPos+1);
      }
      else {
        if (slashPos > -1) {
          throw new XMLConfigException("An attribute cannot have subparts (foo.bar.fum and foo.bar/fum not allowed)");
        }
        if (!initialDot) {
          nodeName = path.substring(0, dotPos);
          path = path.substring(dotPos+1);
          dotRead = true;
        }
        else {
          path = path.substring(1);
          getMultipleAddAttributesHelper(path, n, accum);
          return;
        }
      }
      Node child = n.getFirstChild();
      if (nodeName.equals("*")) {
        while(child != null) {
          if (!child.getNodeName().equals("#text") && !child.getNodeName().equals("#comment")) {
            if (dotRead) {
              getMultipleAddAttributesHelper(path, child, accum);
            }
            else {
              getMultipleHelper(path, child, accum, false);
            }
          }
          child = child.getNextSibling();
        }
        return;
      }
      else {
        while(child != null) {
          if (child.getNodeName().equals(nodeName)) {
            // found
            if (dotRead) {
              getMultipleAddAttributesHelper(path, child, accum);
            }
            else {
              getMultipleHelper(path, child, accum, false);
            }
          }
          child = child.getNextSibling();
        }
        return;
      }
    }
    else {
      accum.add(n);
    }
  }
  
  private void getMultipleAddAttributesHelper(String path, Node n, List<Node> accum) {
    if ((path.indexOf('.') > -1) || (path.indexOf('/') > -1)) {
      throw new XMLConfigException("An attribute cannot have subparts (foo.bar.fum and foo.bar/fum not allowed)");
    }
    NamedNodeMap attrMap = n.getAttributes();
    if (path.equals("*")) {
      for(int i = 0; i < attrMap.getLength(); ++i) {
        Node attr = attrMap.item(i);
        accum.add(attr);
      }
    }
    else {
      Node attr = attrMap.getNamedItem(path);
      if (attr != null) {
        accum.add(attr);
      }
    }
  }
  
  /** Set the value of the node or attribute specified by the DOM path.
   * @param path DOM path
   * @param value node or attribute value
   * @return the node that was created, or the parent node of the attribute if it was an attribute
   */
  public Node set(String path, String value) {
    if (isDelegated()) { return set(path, value, _startNode, true); }

    return set(path, value, _document, true);
  }
  
  /** Set the value of the node or attribute specified by the DOM path.
   * @param path DOM path
   * @param value node or attribute value
   * @param overwrite whether to overwrite (true) or add (false)
   * @return the node that was created, or the parent node of the attribute if it was an attribute
   */
  public Node set(String path, String value, boolean overwrite) {
    if (isDelegated()) { return set(path, value, _startNode, overwrite); }

    return set(path, value, _document, overwrite);
  }
  
  
  /** Set the value of the node or attribute specified by the DOM path.
   * @param path DOM path
   * @param value node or attribute value
   * @param n node where the search should start
   * @param overwrite whether to overwrite (true) or add (false) -- only applies for last node!
   * @return the node that was created, or the parent node of the attribute if it was an attribute
   */
  public Node set(String path, String value, Node n, boolean overwrite) {
    if (isDelegated()) { return _parent.set(path, value, n, overwrite); }
    
    int dotPos = path.lastIndexOf('.');
    Node node;
    if (dotPos == 0) {
      node = n;
    }
    else {
      node = createNode(path, n, overwrite);
    }
    if (dotPos >= 0) {
      Element e = (Element)node;
      e.setAttribute(path.substring(dotPos+1),value);
    }
    else {
      node.appendChild(_document.createTextNode(value));
    }
    return node;
  }
  
  /** Create the node specified by the DOM path.
   * @param path DOM path
   * @return the node that was created, or the parent node of the attribute if it was an attribute
   */
  public Node createNode(String path) {
    if (isDelegated()) { return createNode(path, _startNode, true); }
    
    return createNode(path, _document, true);
  }
  
  /** Create the node specified by the DOM path.
   * @param path DOM path
   * @param n node where the search should start, or null for the root
   * @return the node that was created, or the parent node of the attribute if it was an attribute
   */
  public Node createNode(String path, Node n) {
    return createNode(path, n, true);
  }
  
  /** Create the node specified by the DOM path.
   * @param path DOM path
   * @param n node where the search should start, or null for the root
   * @param overwrite whether to overwrite (true) or add (false) -- only applies for last node!
   * @return the node that was created, or the parent node of the attribute if it was an attribute
   */
  public Node createNode(String path, Node n, boolean overwrite) {
    if (isDelegated()) { return _parent.createNode(path, n, overwrite); }

    if (n == null) { n = _document; }
    while(path.indexOf('/') > -1) {
      Node child = null;
      String nodeName = path.substring(0, path.indexOf('/'));
      path = path.substring(path.indexOf('/')+1);
      child = n.getFirstChild();
      while(child != null) {
        if (child.getNodeName().equals(nodeName)) {
          // found
          n = child;
          break;
        }
        child = child.getNextSibling();
      }
      if (child == null) {
        // not found
        child = _document.createElement(nodeName);
        n.appendChild(child);
        n = child;
      }
    }
    
    String nodeName;
    if (path.indexOf('.') > -1) {
      nodeName = path.substring(0, path.indexOf('.'));
    }
    else {
      if (path.length() == 0) {
        throw new XMLConfigException("Cannot set node with empty name");
      }
      nodeName = path;
    }
    Node child = null;
    if (nodeName.length() > 0) {
      if (overwrite) {
        child = n.getFirstChild();
        while(child != null) {
          if (child.getNodeName().equals(nodeName)) {
            // found
            n = child;
            break;
          }
          child = child.getNextSibling();
        }
        if (child == null) {
          child = _document.createElement(nodeName);
          n.appendChild(child);
          n = child;
        }
      }
      else {
        child = _document.createElement(nodeName);
        n.appendChild(child);
        n = child;
      }
    }
    
    if (path.indexOf('.') > -1) {
      if (!(n instanceof Element)) {
        throw new XMLConfigException("Node " + n.getNodeName() + " should be an element so it can contain attributes");
      }
      return n;
    }
    else {
      if (overwrite) {
        child = n.getFirstChild();
        // remove all children
        while(child != null) {
          Node temp = child.getNextSibling();
          n.removeChild(child);
          child = temp;
        }
        return n;
      }
      else {
        return child;
      }
    }
  }
  
  
  /** Returns a string representation of the object.
   * @return a string representation of the object.
   */
  public String toString() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    save(os);
    return os.toString();
  }
  
  /** Return the path of a node as it is used in XMLConfig.
   * @param n node
   * @return path
   */
  public static String getNodePath(Node n) {
    if (n == null) { return ""; }
    String path = "";
    while(n.getParentNode() != null) {
      path = n.getNodeName() + "/" + path;
      n = n.getParentNode();
    }
    
    return path.substring(0,path.length()-1);
  }
  
  /** Exception in XMLConfig methods.
   */
  public static class XMLConfigException extends RuntimeException {
    public XMLConfigException() {
      super();
    }
    
    public XMLConfigException(String message) {
      super(message);
    }
    
    public XMLConfigException(String message, Throwable cause) {
      super(message, cause);
    }
    
    public XMLConfigException(Throwable cause) {
      super(cause);
    }
  }
}
