// Language Level Converter line number map: dj*->java. Entries: 10
//     1->4         2->5         3->6         4->7         5->8         6->9         7->10        8->11   
//     9->12       10->13   
/**Test that a while loop can be correctly handled */
class FunWithWhileLoops {
  int myWhilingMethod(int i, int j, char c, double d) {
    int k = i;
    while (k<j && (((int) j+d) == c)) {
      k = i*4;
    }
    return 365;
  }
}
