public class D implements IntBox {
public int get() { return new A().get() + new B().get() + new C().get() - 2; }
}

