package ast;

import java.io.PrintStream;

public class Stmt extends ASTNode {

    public static final int DECL = 1;
    public static final int IF = 2;
    public static final int IFELSE = 3;
    public static final int PRINT = 4;
    public static final int RETURN = 5;
    public static final int STMTGRP = 6;
    public static final int ASSIGN = 7;
    public static final int CALL = 8;
    public static final int WHILE = 9;
    public static final int FREE = 10;
    public static final int CURSED = 11;

    final int typeOfStmt;
    final String var;
    final VarDecl varDecl;
    final Expr expr;
    final Cond cond;
    final Stmt stmt1;
    final Stmt stmt2;
    final StmtList stmtList;
    final ExprList exprList;

    // If or While
    public Stmt(int typeOfStmt, Cond c, Stmt s, Location loc) {
        super(loc);
        this.var = null;
        this.varDecl = null;
        this.cond = c;
        this.expr = null;
        this.stmt1 = s;
        this.stmt2 = null;
        this.stmtList = null;
        this.exprList = null;
        this.typeOfStmt = typeOfStmt;
    }

    // If Else stmt
    public Stmt(int typeOfStmt, Cond c, Stmt s1, Stmt s2, Location loc) {
        super(loc);
        this.var = null;
        this.varDecl = null;
        this.cond = c;
        this.expr = null;
        this.stmt1 = s1;
        this.stmt2 = s2;
        this.stmtList = null;
        this.exprList = null;
        this.typeOfStmt = typeOfStmt;
    }

    // Declaration
    public Stmt(int typeOfStmt, VarDecl v, Expr e, Location loc) {
        super(loc);
        this.var = null;
        this.varDecl = v;
        this.cond = null;
        this.expr = e;
        this.stmt1 = null;
        this.stmt2 = null;
        this.stmtList = null;
        this.exprList = null;
        this.typeOfStmt = typeOfStmt;
    }

    // Print or Return or Free or Expr wrapper
    public Stmt(int typeOfStmt, Expr e, Location loc) {
        super(loc);
        this.var = null;
        this.varDecl = null;
        this.cond = null;
        this.expr = e;
        this.stmt1 = null;
        this.stmt2 = null;
        this.stmtList = null;
        this.exprList = null;
        this.typeOfStmt = typeOfStmt;
    }

    // Stmt group
    public Stmt(int typeOfStmt, StmtList sl, Location loc) {
        super(loc);
        this.var = null;
        this.varDecl = null;
        this.cond = null;
        this.expr = null;
        this.stmt1 = null;
        this.stmt2 = null;
        this.stmtList = sl;
        this.exprList = null;
        this.typeOfStmt = typeOfStmt;
    }

    // Assignment
    public Stmt(int typeOfStmt, Object i, Expr e, Location loc) {
        super(loc);
        this.var = i.toString();
        this.varDecl = null;
        this.cond = null;
        this.expr = e;
        this.stmt1 = null;
        this.stmt2 = null;
        this.stmtList = null;
        this.exprList = null;
        this.typeOfStmt = typeOfStmt;
    }

    // Call
    public Stmt(int typeOfStmt, Object i, ExprList el, Location loc) {
        super(loc);
        this.var = i.toString();
        this.varDecl = null;
        this.cond = null;
        this.expr = null;
        this.stmt1 = null;
        this.stmt2 = null;
        this.stmtList = null;
        this.exprList = el;
        this.typeOfStmt = typeOfStmt;
    }

    public Stmt getStmt1() {
        return stmt1;
    }

    public Cond getCond() {
        return cond;
    }

    public Stmt getStmt2() {
        return stmt2;
    }

    public VarDecl getVarDecl() {
        return varDecl;
    }

    public Expr getExpr() {
        return expr;
    }

    public StmtList getStmtList() {
        return stmtList;
    }

    public ExprList getExprList() {
        return exprList;
    }

    public String getVar() {
        return var;
    }

    public int getTypeOfStmt() {
        return typeOfStmt;
    }

    public void println(PrintStream ps) {
        ps.println(expr);
    }
}
