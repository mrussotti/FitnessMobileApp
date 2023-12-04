package ast;

import java.io.PrintStream;

public class ExprList extends Stmt {

    //a variable declaration will have data type followed by a string beginning with a character as a variable name
    final NeExprList neExprList;

    //construct a program with an expression
    public ExprList(NeExprList neExprList, Location loc) {
        super(loc);
        this.neExprList = neExprList;
    }

    public ExprList(Location loc){
        super(loc);
        this.neExprList = null;
    }

    public NeExprList getNeExprList() {
        return neExprList;
    }




    
}