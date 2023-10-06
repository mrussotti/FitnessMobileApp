package ast;

import java.io.PrintStream;

public class Cond extends ASTNode {
    public static final int LTE = 1;
    public static final int GTE = 2;
    public static final int EQ = 3;
    public static final int NOTEQ = 4;
    public static final int LT = 5;
    public static final int GT = 6;
    public static final int AND = 7;
    public static final int OR = 8;
    public static final int NOT = 9;

    final Expr e1;
    final Expr e2;
    final int operator;
    final Cond c1;
    final Cond c2;

    public Cond(Expr e1, int operator, Expr e2, Location loc){
        super(loc);
        this.e1 = e1;
        this.e2 = e2;
        this.operator = operatorl;
        this.c1 = null;
        this.c2 = null;
    }

    public Cond(Cond c1, int operator, Cond c2, Location loc){
        super(loc);
        this.e1 = null;
        this.e2 = null;
        this.operator = operatorl;
        this.c1 = c1;
        this.c2 = c2;
    }

    public Cond(int operator, Cond c, Location loc){
        super(loc);
        this.e1 = null;
        this.e2 = null;
        this.operator = operatorl;
        this.c1 = c;
        this.c2 = null;
    }
}