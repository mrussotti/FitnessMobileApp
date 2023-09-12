package ast;

import java.io.PrintStream;

public class Program extends ASTNode {

    final Expr expr;
    final Stmt stmt;

    public Program(Expr expr, Location loc) {
        super(loc);
        this.expr = expr;
        this.stmt = null;
    }

    public Program(Stmt stmt, Location loc){
        super(loc);
        this.expr = null;
        this.stmt = stmt;
    }

    public Expr getExpr() {
        return expr;
    }

    public Stmt getStmt() {
        return stmt;
    }

    public void println(PrintStream ps) {
        ps.println(expr == null ? stmt : expr);
    }
}
