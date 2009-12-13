// Language Level Converter line number map: dj*->java. Entries: 9
//     1->4         2->5         3->6         4->7         5->8         6->9         7->10        8->11   
//     9->12   
class FunWithForLoop {
  int myFunForLoopMethod() { 
    String myString = "";
    for (int i = 0; i<250; i++) {
      myString += i + "abcdefg";
    }
    return myString.length();
  }
}
