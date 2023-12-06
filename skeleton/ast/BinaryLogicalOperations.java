package ast;

public class BinaryLogicalOperations extends Condition {

    public static final int AND = 1;
    public static final int OR = 2;


    

    final Condition cond1;
    final int operator;
    final Condition cond2;

    public BinaryLogicalOperations(Condition cond1, int operator, Condition cond2, Location loc) {
        super(loc);
        this.cond1 = cond1;
        this.operator = operator;
        this.cond2 = cond2;
    }

    public Condition getLeftCond() {
        return cond1;
    }

    public int getOperator() {
        return operator;
    }
    
    public Condition getRightCond() {
        return cond2;
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
