package ast;

public class Q extends Type {

    final int value;
    final Ref heap;

    public Q(int value) {
        super(loc);
        this.value = value;
        this.heap = null;
    }

    public String getIdent() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
