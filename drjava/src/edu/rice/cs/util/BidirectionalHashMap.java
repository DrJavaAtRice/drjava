package edu.rice.cs.util;
import java.util.*;

public class BidirectionalHashMap<Type1, Type2>
{
  HashMap<Type1, Type2> forward = new HashMap<Type1, Type2>();
  HashMap<Type2, Type1> backward = new HashMap<Type2, Type1>();
  
  public void put(Type1 key, Type2 value)
  {
    if(forward.containsKey(key))
    {
      throw new IllegalArgumentException("Key "  + key + " exists in hash already.");
    }
    if(forward.containsValue(value))
    {
      throw new IllegalArgumentException("Double hashes must be one to one. " + value + " exists already in hash.");
    }      
    forward.put(key, value);
    backward.put(value,key);
  }
  
  public Type2 getValue(Type1 key)
  {
    return forward.get(key);
  }

  public Type1 getKey(Type2 value)
  {
    return backward.get(value);
  }
  
  public  boolean containsKey(Object key)
  {
    return forward.containsKey(key);
  }
  
  public  boolean containsValue(Object value)
  {
    return backward.containsKey(value);
  }
  
  public  Type2 removeValue(Type1 key)
  {
    Type2 tmp = forward.remove(key);
    backward.remove(tmp);
    return tmp;
  }
  
  public  Type1 removeKey(Type2 value)
  {
    Type1 tmp = backward.remove(value);
    forward.remove(tmp);
    return tmp;
  }
  
  public  int size()
  {
    return forward.size();
  }
  
  public  Iterator<Type2> valuesIterator()
  {
    return forward.values().iterator();
  }
  
  public  void clear()
  {
    forward = new HashMap<Type1, Type2>();
    backward = new HashMap<Type2, Type1>();
  }
  
  public String toString()
  {
    String ret = new String();
    ret = "forward = \n" + forward.values() + "\n backward = \n" + backward.values();
    return ret;
  }
}