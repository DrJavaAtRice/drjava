package edu.rice.cs.util;

public class Pair<T,U> {
  T _first;
  U _second;

  public Pair(T first, U second) {
    _first = first;
    _second = second;
  }

  public T getFirst() {
    return _first;
  }

  public U getSecond() {
    return _second;
  }
}
