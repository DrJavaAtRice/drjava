// Language Level Converter line number map: dj*->java. Entries: 10
//     1->4         2->5         3->6         4->7         5->8         6->9         7->10        8->11   
//     9->12       10->13   
//tests that empty statements in for loops are okay
class ForLoopWithEmptyStatements {
  int myMethod(int j) {
    for (; j<24 ; ) {
      System.out.println("I am running infinitely!");
    }
  return 245;
  }
}
  
