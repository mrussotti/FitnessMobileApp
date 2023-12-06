package ast;

public class Q {

    public INT inte;
    public Ref heap;
    public boolean mutable;

    public Q(long value) {
        this.inte = new INT(value);
        this.heap = null;
    }

    public Q(Ref heap){
        this.inte = null;
        this.heap = heap;
    }

    public Q(){
        this.inte = null;
        this.heap = null;
    }

    public INT getINT() {
        return inte;
    }

    public Ref getRef(){
        return heap;
    }

    @Override
    public String toString(){

        if(inte != null){
            return inte.toString();
        }
        if(heap != null){
            return heap.toString();
        }
        return "nil";
    }
}
