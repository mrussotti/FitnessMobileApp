package ast;

public class ConcurrentExpression extends Expr {

    final BinaryExpr value;

    public ConcurrentExpression(BinaryExpr value, Location loc) {
        super(loc);
        this.value = value;
    }

    public BinaryExpr getBinaryExpr() {
        return value;
    }

    //@Override
    public String toString() {
        return value.toString();
    }
}
