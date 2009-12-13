// Language Level Converter line number map: dj*->java. Entries: 16
//     1->4         2->5         3->6         4->7         5->8         6->9         7->10        8->11   
//     9->12       10->13       11->14       12->15       13->16       14->17       15->18       16->19   
package listFW;

public class NEList implements IList {
  Object _first;
  IList _rest;
  
  public NEList(Object first, IList rest) {
    _first = first;
    _rest = rest;
  }

  public int getLength() {
    return 1 + _rest.getLength();
  }
  
}
