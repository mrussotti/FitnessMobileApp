package ast;

import java.io.PrintStream;

public abstract class Stmt extends StmtList {

    //must implement if and if-else, and return(?) and <stmtList>

    //statements have an expression
    final Expr expr;

    //construct a statement with an expression
    public Stmt(Expr expr, Location loc){
        super(loc);
        this.expr = expr;
    }

    //get expression from statement
    public Expr getExpr() {
        return expr;
    }

    //give the espression of the statement to a print stream COPIED FROM Program.java
    public void println(PrintStream ps){
        ps.println(expr);
    }
}