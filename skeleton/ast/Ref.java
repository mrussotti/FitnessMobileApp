package ast;

public class Ref extends Q {
    final Q left;
    final Q right;

    public Ref(Q left, Q right) {
        this.left = left;
        this.right = right;
    }

    public Q getLeft() {
        return left;
    }
    
    public Q getRight(){
        return right;
    }

    @Override
    public String toString() {
        return "( " + left + " . " + right + " )";
    }
}
