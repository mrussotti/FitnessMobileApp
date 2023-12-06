package ast;

public class BinaryComparison extends Condition {

    public static final int GEQ = 1;
    public static final int LEQ = 2;
    public static final int EQ = 3;
    public static final int NEQ = 4;
    public static final int LT = 5;
    public static final int GT = 6;


    final Expr expr1;
    final int operator;
    final Expr expr2;

    public BinaryComparison(Expr expr1, int operator, Expr expr2, Location loc) {
        super(loc);
        this.expr1 = expr1;
        this.operator = operator;
        this.expr2 = expr2;
    }

    public Expr getLeftExpr() {
        return expr1;
    }

    public int getOperator() {
        return operator;
    }
    
    public Expr getRightExpr() {
        return expr2;
    }

    // @Override
    // public String toString() {
    //     String s = null;
    //     switch (operator) {
    //         case PLUS:  s = "+"; break;
    //         case MINUS: s = "-"; break;
    //         case TIMES: s = "*"; break;
    //     }
    //     return "(" + expr1 + " " + s + " " + expr2 + ")";
    // }
}
