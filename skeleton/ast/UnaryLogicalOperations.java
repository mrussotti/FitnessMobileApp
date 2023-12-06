package ast;

public class UnaryLogicalOperations extends Condition {

    public static final int NOT = 1;


    final Condition cond1;
    final int operator;

    public UnaryLogicalOperations(Condition cond1, int operator, Location loc) {
        super(loc);
        this.cond1 = cond1;
        this.operator = operator;
    }

    public Condition getCond() {
        return cond1;
    }

    public int getOperator() {
        return operator;
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
