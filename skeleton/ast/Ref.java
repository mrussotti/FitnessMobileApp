package ast;

public class Ref extends Type {
    final Ref left;
    final Ref right;

    public Ref( long address) {
        this.left = null;
        this.right = null;
    }

    public String getLeft() {
        return left;
    }
    
    public String getRight(){
        return right;
    }

    public void setLeft(Q left){
        this.left = left;
    }

    public void setRight(Q right){
        this.right = right;
    }

    @Override
    public String toString() {
        return "( " + left + " . " + right + " )";
    }
}
