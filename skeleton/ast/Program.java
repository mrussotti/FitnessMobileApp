package ast;

import java.io.PrintStream;

public class Program extends ASTNode {

    //a program will have expressions and statements
    final Expr expr;
    final Stmt stmt;

    //construct a program with an expression
    public Program(Expr expr, Location loc) {
        super(loc);
        this.expr = expr;
        this.stmt = null;
    }

    //construct a program with a statement
    public Program(Stmt stmt, Location loc){
        super(loc);
        this.expr = null;
        this.stmt = stmt;
    }

    //get for a program's expression
    public Expr getExpr() {
        return expr;
    }

    //get for a program's statement
    public Stmt getStmt() {
        return stmt;
    }

    //print for the expr/stmt of a program
    public void println(PrintStream ps) {
        ps.println(expr == null ? stmt : expr);
    }
}
