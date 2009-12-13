// Language Level Converter line number map: dj*->java. Entries: 25
//     1->6         2->7         3->8         4->9         5->10        6->11        7->12        8->13   
//     9->14       10->15       11->16       12->17       13->18       14->19       15->20       16->21   
//    17->22       18->23       19->24       20->25       21->26       22->27       23->28       24->29   
//    25->30   
class ArrayDeclarations {
  Integer[] iArray = new Integer[5];
  
  char c[];
  
  char initializedC[] = new char[] {'e', 'j', 's'};
  
  double[][] d = new double[5][5];
  
  Object[][][] o = new Object[2][][];
  
  int i, crazyArray[];
  
  double[] noNew = {1.2, 3.4, 5.2};
  
//  String[][][] definitelyIllegal = new String[4][][3];
  
  int nestedInitializer[][] = new int[][] { {1,2}, null};
  
  int myMethod() {
    return iArray[0];
    
//    o.field().field = value;
  } 
}
