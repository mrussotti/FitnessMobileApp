package ast;

import java.io.PrintStream;

public class Cond extends ASTNode {

    public static final int MOREEQUAL = 1;
    public static final int LESSEQUAL = 2;
    public static final int EQUALITY = 3;
    public static final int NOTEQUAL = 4;
    public static final int LESS = 5;
    public static final int MORE = 6;
    public static final int AND = 7;
    public static final int OR = 8;
    public static final int NOT = 9;

    final int typeOfCond;
    final Expr expr1;
    final Expr expr2;
    final Cond cond1;
    final Cond cond2;

    public Cond(int typeOfCond, Expr e1, Expr e2, Location loc) {
        super(loc);
        this.expr1 = e1;
        this.expr2 = e2;
        this.cond1 = null;
        this.cond2 = null;
        this.typeOfCond = typeOfCond;
    }

    public Cond(int typeOfCond, Cond c1, Cond c2, Location loc) {
        super(loc);
        this.expr1 = null;
        this.expr2 = null;
        this.cond1 = c1;
        this.cond2 = c2;
        this.typeOfCond = typeOfCond;
    }

    public Cond(int typeOfCond, Cond c1, Location loc) {
        super(loc);
        this.expr1 = null;
        this.expr2 = null;
        this.cond1 = c1;
        this.cond2 = null;
        this.typeOfCond = typeOfCond;
    }

    public int getTypeOfCond() {
        return typeOfCond;
    }

    public Cond getCond1() {
        return cond1;
    }

    public Cond getCond2() {
        return cond2;
    }

    public Expr getExpr1() {
        return expr1;
    }

    public Expr getExpr2() {
        return expr2;
    }

    @Override
    public String toString() {

        return "(" + expr1 + " " + typeOfCond + " " + expr2 + ")";
    }
}
