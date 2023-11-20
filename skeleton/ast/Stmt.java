package ast;

import java.io.PrintStream;

public class Stmt extends StmtList {


    public static final int RETURN = 1;
    public static final int EQUALS = 2;
    public static final int IF = 3;
    public static final int ELSE = 4;
    public static final int PRINT = 5;
    public static final int STMTBLOCK = 6;
    public static final int ASSIGN = 7;
    public static final int WHILE = 8;
    public static final int CALL = 9;
    //must implement if and if-else, and return(?) and <stmtList>

    //statements have an expression
    final Expr expr;
    final VarDecl varDecl;
    // if/else need to handle conditions
    final Cond cond;
    final Stmt stmt1;
    final Stmt stmt2;
    // for Statement Block we need to hold a StmtList
    final StmtList stmtList;
    final int type;
    final String ident;
    final ExprList exprList;

    public Stmt(int type, VarDecl v, Expr expr, Location loc){
        super(loc);
        this.expr = expr;
        this.varDecl = v;
        this.type = type;
        this.cond = null;
        this.stmt1 = null;
        this.stmt2 = null;
        this.stmtList = null;
        this.ident = null;
        this.exprList = null;
    }
    //both returns and prints
    public Stmt(int type, Expr expr, Location loc){
        super(loc);
        this.expr = expr;
        this.varDecl = null;
        this.type = type;
        this.cond = null;
        this.stmt1 = null;
        this.stmt2 = null;
        this.stmtList = null;
        this.ident = null;
        this.exprList = null;
    }

    public Stmt(int type, Cond c, Stmt stmt, Location loc){
        super(loc);
        this.expr = null;
        this.varDecl = null;
        this.type = type;
        this.cond = c;
        this.stmt1 = stmt;
        this.stmt2 = null;
        this.stmtList = null;
        this.ident = null;
        this.exprList = null;
    }

    public Stmt(int type, Cond c, Stmt stmt1, Stmt stmt2, Location loc){
        super(loc);
        this.expr = null;
        this.varDecl = null;
        this.type = type;
        this.cond = c;
        this.stmt1 = stmt1;
        this.stmt2 = stmt2;
        this.stmtList = null;
        this.ident = null;
        this.exprList = null;
    }

    public Stmt(int type, StmtList s, Location loc){
        super(loc);
        this.expr = null;
        this.varDecl = null;
        this.type = type;
        this.cond = null;
        this.stmt1 = null;
        this.stmt2 = null;
        this.stmtList = s;
        this.ident = null;
        this.exprList = null;
    }

    public Stmt(int type, String ident, Expr expr, Location loc){
        super(loc);
        this.expr = expr;
        this.varDecl = null;
        this.type = type;
        this.cond = null;
        this.stmt1 = null;
        this.stmt2 = null;
        this.stmtList = null;
        this.ident = ident;
        this.exprList = null;
    }

    public Stmt(int type, String ident, ExprList exprList, Location loc){
        super(loc);
        this.expr = null;
        this.varDecl = null;
        this.type = type;
        this.cond = null;
        this.stmt1 = null;
        this.stmt2 = null;
        this.stmtList = null;
        this.ident = ident;
        this.exprList = exprList;
    }

    public Stmt(Location loc){
        super(loc);
        this.expr = null;
        this.varDecl = null;
        this.type = 0;
        this.cond = null;
        this.stmt1 = null;
        this.stmt2 = null;
        this.stmtList = null;
        this.ident = null;
        this.exprList = null;
    }

    public int getType(){
        return type;
    }

    public VarDecl getVarDecl(){
        return varDecl;
    }

    public Cond getCond(){
        return cond;
    }

    public Stmt getStmt1(){
        return stmt1;
    }

    public Stmt getStmt2(){
        return stmt2;
    }

    public StmtList getStmtList(){
        return stmtList;
    }

    //get expression from statement
    public Expr getExpr() {
        return expr;
    }
    
    public ExprList getExprList(){
        return exprList;
    }

    public String getIdent(){
        return ident;
    }
    //give the espression of the statement to a print stream COPIED FROM Program.java
    public void println(PrintStream ps){
        ps.println(expr);
    }
}