package ast;

public class Q {

    final int value;
    final Ref heap;

    public Q(int value) {
        this.value = value;
        this.heap = null;
    }

    public Q(Ref heap){
        this.value = -1;
        this.heap = heap;
    }

    public Q(){
        this.value = -1;
        this.heap = null;
    }

    public int getInt() {
        return value;
    }

    public Ref getRef(){
        return heap;
    }
}
