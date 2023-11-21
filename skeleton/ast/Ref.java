package ast;

public class Ref extends Type {
    final Ref left;
    final Ref right;

    public Ref(Ref left, Ref right) {
        this.left = left;
        this.right = right;
    }

    public String getLeft() {
        return left;
    }
    
    public String getRight(){
        return right;
    }

    @Override
    public String toString() {
        return "( " + left + " . " + right + " )";
    }
}
