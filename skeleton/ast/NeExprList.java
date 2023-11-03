package ast;
import java.util.HashMap;
import java.util.Map;
import java.io.PrintStream;

public class NeExprList extends ExprList {

    //a variable declaration will have data type followed by a string beginning with a character as a variable name
    final Expr expr;
    final NeExprList neExprList;

    //construct a program with an expression
    public NeExprList(Expr expr, NeExprList neExprList, Location loc) {
        super(loc);
        this.expr = expr;
        this.neExprList = neExprList;
    }

    public NeExprList(Expr expr, Location loc){
        super(loc);
        this.expr = expr;
        this.neExprList = null;
    }

    public Expr getExpr(){
        return expr;
    }

    public NeExprList getNeExprList() {
        return neExprList;
    }
    
    public int length(){
        if(this.neExprList!=null){
            return 1 + this.neExprList.length();
        }else{
            return 1;
        }
    }
}