package ast;

public class PrintStatement extends Statement{

    final Expr e;

    public PrintStatement(Expr e, Location loc){
        super(loc);
        this.e=e;
    }

    public Expr getExpr(){
        return e;
    }
    
}
