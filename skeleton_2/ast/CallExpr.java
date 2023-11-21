package ast;

public class CallExpr extends Expr {

    final String indetifier;
    final ExprList exprList;

    public CallExpr(Object i, ExprList el, Location loc) {
        super(loc);
        this.indetifier = i.toString();
        this.exprList = el;
    }

    public String getIndetifier() {
        return indetifier;
    }

    public ExprList getExprList() {
        return exprList;
    }
}
