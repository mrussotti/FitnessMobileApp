package ast;

public class Ref extends Q {
    public Q left;
    public Q right;

    public static final Ref NIL = new Ref(null, null);

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

    public boolean isNil(){
        return this == NIL;
    }

    public void setLeft(Q newLeft){
        left = newLeft;
    }

    public void setRight(Q newRight){
        right = newRight;
    }

    @Override
    public String toString() {
        if (this == NIL){
            
            return "nil";
        }
        return "(" + left + " . " + right + ")";
    }
}
