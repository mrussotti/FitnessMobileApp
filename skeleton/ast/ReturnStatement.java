package ast;

public class ReturnStatement extends Statement{

    final Expr e;

    public ReturnStatement(Expr e, Location loc){
        super(loc);
        this.e=e;
    }

    public Expr getExpr(){
        return e;
    }
    
}
