package ast;

public class Q {

    public INT value;
    public Ref heap;

    public Q(long value) {
        this.value = value;
        this.heap = null;
    }

    public Q(Ref heap){
        this.value = null;
        this.heap = heap;
    }

    public Q(){
        this.value = null;
        this.heap = null;
    }

    public INT getValue() {
        return value;
    }

    public Ref getRef(){
        return heap;
    }
}
