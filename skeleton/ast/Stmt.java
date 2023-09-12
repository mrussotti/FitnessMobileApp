package ast;

import java.io.PrintStream;

public class Stmt extends ASTNode {

    final Expr expr;

    public Stmt(Expr expr, Location loc){
        super(loc);
        this.expr = expr;
    }

    public Expr getExpr() {
        return expr;
    }

    public void println(PrintStream ps){
        ps.pringln(expr);
    }
}