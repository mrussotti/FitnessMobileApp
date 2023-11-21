package ast;

public class Q extends Type {

    final int value;
    final Ref heap;

    public Q(int value) {
        this.value = value;
        this.heap = null;
    }

    public Q(Ref heap){
        this.value = null;
        this.heap = heap;
    }

    public String getInt() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
