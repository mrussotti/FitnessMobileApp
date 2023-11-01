package ast;

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

    public void fillArgs(HashMap<String, String> args, NeFormalDeclList neFormalDeclList, HashMap<String, String> scope){
        // get formal parameter variables and assign values from the exprList to those var names to pass into scope
            args.put(neFormalDeclList.getVarDecl().getIdent(), evaluateExpr(expr, args, scope).toString());//I want to put this into the new scope
            NeFormalDeclList next = neFormalDeclList.getNeFormalDeclList()
            if(neExprList != null && next !=null){
                // the function can take more args
                neExprList.fillArgs(args, next, scope);
            }
            //here args should be filled, however no error will be thrown for incorrect number of args.....
    }




    
}