package ast;

public class Q {

    public INT value;
    public Ref heap;

    public Q(long value) {
        this.value = new INT(value);
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

    public INT getINT() {
        return value;
    }

    public Ref getRef(){
        return heap;
    }

    @Override
    public String toString(){
        if(value == null){
            return heap.toString();
        }else if(heap == null){
            return value.toString();
        }
        return "nil";
    }
}
