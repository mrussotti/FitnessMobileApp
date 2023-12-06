package ast;

import java.io.PrintStream;

public class TypeCast extends Expr {

    //a variable declaration will have data type followed by a string beginning with a character as a variable name
    final Type type;
    final Expr expr;

    //construct a program with an expression
    public TypeCast(Type type, Expr expr, Location loc) {
        super(loc);
        this.type = type;
        this.expr = expr;
    }

    public Type getCastType() {
        return type;
    }

    public Expr getCastExpr(){
        return expr;
    }
}