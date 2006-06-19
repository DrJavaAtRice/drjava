class Tom extends Jerry {

  public Tom() {
    super();
  }

  public String toString() {
    return "Tom(" + ")";
  }

  public boolean equals(Object o) {
    if ((o == null) || getClass() != o.getClass()) return false;
    return true;
  }

  public int hashCode() {
    return getClass().hashCode();
  }
}
