/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */


/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
 END_COPYRIGHT_BLOCK*/

package koala.dynamicjava.interpreter;

import junit.framework.TestCase;

import koala.dynamicjava.parser.wrapper.JavaCCParserFactory;
import koala.dynamicjava.util.*;
import java.io.StringReader;


public class DynamicjavaTest extends DynamicJavaTestCase {
  
  Interpreter interpreter;
  String testString;
  
  protected void setUp(){
    setTigerEnabled(true);
    interpreter = new TreeInterpreter(new JavaCCParserFactory());
  }
  
  protected void tearDown(){
   TigerUtilities.resetVersion();    
  }
  
  Object interpret(String testString){
    return interpreter.interpret(new StringReader(testString),"UnitTest");
  }
  
  public void testBasics() {
    testString = "Integer x = new Integer(5);x;";
    assertEquals("Evaluation of Integer(5)", new Integer(5), interpret(testString));
    
    testString = "Boolean y = Boolean.FALSE; y;";
    assertEquals("Evaluation of Boolean.FALSE", Boolean.FALSE, interpret(testString));
    
    testString = "String z = \"FOO\" + \"BAR\"; z;";
    assertEquals("String concatenation", "FOOBAR", interpret(testString));
    
    testString = "int[] a = new int[]{1,2,3}; \"\"+a[0]+a[1]+a[2];";
    assertEquals("Anonymous array", "123", interpret(testString));
    
    testString = "int[][] b = new int[][]{{12, 0}, {1, -15}}; b[0][0] + b[1][1];";
    assertEquals("2D Anonymous array", new Integer(-3), interpret(testString));
    
    testString = "(Number) new Integer(12);";
    assertEquals("Successful cast test", new Integer(12), interpret(testString));
  }
  
  public void testAutoBoxing() {
    testString = "Object o = 5; o;";
    assertEquals("Evaluation of autoboxed int 5 in Object context", new Integer(5), interpret(testString));
    
    testString = "Number n = 5; n;";
    assertEquals("Evaluation of autoboxed int 5 in Number context", new Integer(5), interpret(testString));
  }
  
  public void testNonGenericList(){
    testString = 
      "import java.util.*;"+
      "List l = new LinkedList();"+
      "l.add(new Float(5.5));"+
      "l.add(new Integer(7));"+
      "String s = l.toString();"+
      "s;";
    
    assertEquals("s should be [5.5, 7]", "[5.5, 7]", interpret(testString));
    
    testString = "Float f = (Float)l.get(0); f;";
    assertEquals("f should be 5.5", new Float(5.5), interpret(testString));
    
    testString = "l.remove(0); Integer i = (Integer)l.get(0); i;";
    assertEquals("i should be 7", new Integer(7), interpret(testString));  
  }
  
  public void testGenericNonGenerifiedList(){
    testString = 
      "import java.util.*;"+
      "List l = new LinkedList();"+
      "l.add(new Float(5.5));"+
      "l.add(new Float(7.3));"+
      "String s = l.toString();"+
      "s;";
    
    assertEquals("s should be [5.5, 7.3]", "[5.5, 7.3]", interpret(testString));
    
    testString = "Float f = (Float)l.get(0); f;";
    assertEquals("f should be 5.5", new Float(5.5), interpret(testString));
    
    testString = "l.remove(0); Float f2 = (Float)l.get(0); f2;";
    assertEquals("f2 should be 7.3", new Float(7.3), interpret(testString));  
  }
  
  public void testFor(){
    TigerUtilities.assertTigerEnabled("Tiger should be enabled to use generics");
    testString = 
      "import java.util.*;"+
      "List<Double> l = new LinkedList<Double>();"+
      "l.add(new Double(5.5));"+
      "l.add(new Double(7.3));"+
      "List<Double> l2 = new LinkedList<Double>();" +
      "for(Iterator<Double> i = l.iterator();i.hasNext();){"+
      "Double d = i.next();" +
      "l2.add(d);" +
      "}" +
      "l2.get(0);";
    
    assertEquals("first of l2 should be 5.5", new Double(5.5), interpret(testString));
    
  }
  
  public void testForEachGeneric(){
    testString = 
      "import java.util.*;"+
      "List<Double> l = new LinkedList<Double>();"+
      "l.add(new Double(5.5));"+
      "l.add(new Double(7.3));"+
      "List<Double> l2 = new LinkedList<Double>();" +
      "for(Double d: l){"+
      "l2.add(d);" +
      "}" +
      "l2.get(0);";
    
    assertEquals("first of l2 should be 5.5", new Double(5.5), interpret(testString));
  }

  
  public void testForEachNonGeneric(){
    testString = 
      "import java.util.*;"+
      "List l = new LinkedList();"+
      "l.add(new Double(5.5));"+
      "l.add(new Double(7.3));"+
      "List l2 = new LinkedList();" +
      "for(final Object d: l){"+
      "l2.add(d);" +
      "}" +
      "l2.get(0);";
    
    assertEquals("first of l2 should be 5.5", new Double(5.5), interpret(testString));
  }

  public void testForEachLengthOfNewList(){
    testString = 
      "import java.util.*;"+
      "List l = new LinkedList();"+
      "l.add(new Double(5.5));"+
      "l.add(new Double(7.3));"+
      "List l2 = new LinkedList();" +
      "for(final Object d: l){"+
      "l2.add(d);" +
      "}" +
      "l2.size();";
    
    assertEquals("the size of the list should be 2", 2, interpret(testString));
  }

  public void testForEachWithArray(){
    testString = 
      "import java.util.*;"+
      "import java.lang.reflect.Array;" +
      "Double[] l = {new Double(5.5), new Double(7.3)};"+
      "Double l2 = new Double(0);" +
      "for(Double d: l){"+
      "l2 = new Double(l2.doubleValue() + d.doubleValue());" +
      "}" +
      "l2;";
    
    assertEquals("l2 should be 12.8", new Double(12.8), interpret(testString));
  }
  
  public void testForEachWithArrayNarrow(){
    testString = 
      "import java.util.*;"+
      "import java.lang.reflect.Array;" +
      "Double[] l = {new Double(5.5), new Double(7.3)};"+
      "Double l2 = new Double(0);" +
      "for(Object d: l){"+
      "l2 = new Double(l2.doubleValue() + ((Double)d).doubleValue());" +
      "}" +
      "l2;";
    
    assertEquals("l2 should be 12.8", new Double(12.8), interpret(testString));
  }
  
  // Test with Generics.
  //  public void testGenericList(){ /**/
  //    testString = 
  //      "import java.util.*;"+
  //      "List<Float> l = new LinkedList<Float>();"+
  //      "l.add(new Float(5.5));"+
  //      "l.add(new Float(7.3));"+
  //      "String s = l.toString();"+
  //      "s;";
  //    
  //    assertEquals("s should be [5.5, 7.3]", "[5.5, 7.3]", interpret(testString));
  //    
  //    testString = "Float f = l.get(0); f;";
  //    assertEquals("f should be 5.5", new Float(5.5), interpret(testString));
  //    
  //    testString = "l.remove(0); Float f2 = l.get(0); f2;";
  //    assertEquals("f2 should be 7.3", new Float(7.3), interpret(testString));  
  //  }
  
  // More complex test with visitors.
  // The types for the AbstractShapeVisitor interface methods
  // have been weakened to Object because DynamicJava does not
  // support forward class references
  
  public void testVisitorsNonGeneric() {
    testString = stringHelper()+
      "AbstractShape a;"+
      "a = new Box(2,2,2);"+
      "Integer result = (Integer)a.accept(new VolumeCalculator());"+
      "result;";
    
       assertEquals("Result should be 8", new Integer(8), interpret(testString));        
    
  }
  
  private String stringHelper(){
    return
      "interface AbstractShapeVisitor{"+
      "Object forBox(Object b);"+
      "Object forSphere(Object s);}"+
      
      "interface AbstractShape{"+
      "Object accept(AbstractShapeVisitor v);}"+
      
      "class Box implements AbstractShape{"+
      "private int _length;"+
      "private int _width;"+
      "private int _height;"+
      
      "Box(int l,int w,int h){"+
      "_length=l;"+
      "_width=w;"+
      "_height=h;}"+
      
      "int getLength(){"+
      "return _length;}"+
      
      "int getWidth(){"+
      "return _length;}"+
      
      "int getHeight(){"+
      "return _length;}"+
      
      "public Object accept(AbstractShapeVisitor v){return v.forBox(this);}}"+
      
      "class Sphere implements AbstractShape{"+
      "private int _radius;"+
      
      "Sphere(int r){ _radius=r; }"+
      
      "int getRadius(){ return _radius; }"+
      
      "public Object accept(AbstractShapeVisitor v){ return v.forSphere(this); }}"+
      
      "public class VolumeCalculator implements AbstractShapeVisitor{"+
      
      "public Object forBox(Object a){"+
      "Box b = (Box) a;" +
      "return new Integer(b.getLength()*b.getWidth()*b.getHeight());}"+
      
      "public Object forSphere(Object a){"+
      "Sphere s = (Sphere) a;" +
      "int rad = s.getRadius();"+
      "return new Integer(rad*rad*rad*3*1);}}";
  }
  
}
