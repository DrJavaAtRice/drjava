// Language Level Converter line number map: dj*->java. Entries: 19
//     1->5         2->6         3->7         4->8         5->9         6->10        7->11        8->12   
//     9->13       10->14       11->15       12->16       13->17       14->18       15->19       16->20   
//    17->21       18->22       19->23   
//This makes sure that you can use an unqualified name to reference an InnerClass from within a method.

class RefInnerClassWithinMethod {
  
  void myMethod() {
    MyInnerClass c;
  }
    
  class MyInnerClass {
    
  }

}

    

  

  
