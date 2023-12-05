package ast;

public class DeclarationStatement extends Statement{

    private VarDecl v;
    private Expr initExpression;

    public DeclarationStatement(VarDecl v, Expr initExpression, Location loc){
        super(loc);
        this.v=v;
        this.initExpression=initExpression;
    }

    public String getName(){
        return v.getIdent();
    }

    public Expr getInitExpression(){
        return initExpression;
    }

    public Type getType() {
        return v.getType();
    }
    
    public VarDecl getVarDecl(){
        return v;
    }
}
