package ast;

public class NilExpr extends Expr {

    public NilExpr(Location loc) {
        super(loc);
    }

   
    public String toString() {
        return "nil";
    }
}
