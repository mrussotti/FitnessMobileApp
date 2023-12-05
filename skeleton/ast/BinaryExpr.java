package ast;

public class BinaryExpr extends Expr {

    //constants
    public static final int PLUS = 1;
    public static final int MINUS = 2;
    public static final int TIMES = 3; //multiplication for proj1
    public static final int DOT = 4;

    final Expr expr1;
    final int operator;
    final Expr expr2;

    //constructor for a Binary Expression with 2 expressions and an operator
    public BinaryExpr(Expr expr1, int operator, Expr expr2, Location loc) {
        super(loc);
        this.expr1 = expr1;
        this.operator = operator;
        this.expr2 = expr2;
    }

    //get left expression from object
    public Expr getLeftExpr() {
        return expr1;
    }

    //get operator from object
    public int getOperator() {
        return operator;
    }
    
    //get right expression from object
    public Expr getRightExpr() {
        return expr2;
    }

    //@Override
    //get string representation of object
    public String toString() {
        String s = null;
        switch (operator) {
            case PLUS:  s = "+"; break;
            case MINUS: s = "-"; break;
            case TIMES: s = "*"; break; //multiplication for proj 1
            case DOT: s = "."; break;
        }
        return "(" + expr1 + " " + s + " " + expr2 + ")";
    }
}
