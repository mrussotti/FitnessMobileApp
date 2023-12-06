package ast;

public class FreeStatement extends Statement{

    final Expr e;

    public FreeStatement(Expr e, Location loc){
        super(loc);
        this.e=e;
    }

    public Expr getExpr(){
        return e;
    }
    
}
