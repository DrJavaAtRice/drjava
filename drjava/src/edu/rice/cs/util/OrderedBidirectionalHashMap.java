package edu.rice.cs.util;
import java.util.*;

public class OrderedBidirectionalHashMap<Type1, Type2> extends BidirectionalHashMap<Type1, Type2>
{
  private Vector order = new Vector();
  
  public OrderedBidirectionalHashMap()
  {
    super();
  }
  
  public void put(Type1 key, Type2 value)
  {
    super.put(key, value);
    order.add(value);
  }
   
  public Type2 removeValue(Type1 key)
  {
    Type2 value = super.removeValue(key);
    order.remove(value);
    return value;
  }
  
  public  Type1 removeKey(Type2 value)
  {
    Type1 key = super.removeKey(value);
    order.remove(value);
    return key;
  }
  
  public  Iterator<Type2> valuesIterator()
  {
    return order.iterator();
  }
  
  public  void clear()
  {
    super.clear();
    order.clear();
  }
  
}
