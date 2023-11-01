package ast;

import java.io.PrintStream;

public class CallExpr extends Expr {

    //a variable declaration will have data type followed by a string beginning with a character as a variable name
    final IDENT ident;
    final ExprList exprList;

    //construct a program with an expression
    public CallExpr(IDENT ident, ExprList exprList, Location loc) {
        super(loc);
        this.ident = ident;
        this.exprList = exprList;
    }

    public IDENT getIdent() {
        return ident;
    }

    public ExprList getExprList(){
        return exprList;
    }
    
    // get format for funciton searching
    public String getFormat(){
        return ident.toString()
    }
}