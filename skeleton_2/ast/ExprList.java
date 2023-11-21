package ast;

import java.io.PrintStream;

public class ExprList extends ASTNode {

    final Expr expr;
    final ExprList exprList;

    public ExprList(Expr expr, ExprList exprList, Location loc) {
        super(loc);
        this.expr = expr;
        this.exprList = exprList;
    }

    public Expr getExpr() {
        return expr;
    }

    public ExprList getExprList() {
        return exprList;
    }

    public int length() {
        if (this.exprList != null) {
            return 1 + this.exprList.length();
        } else {
            return 1;
        }
    }
}
