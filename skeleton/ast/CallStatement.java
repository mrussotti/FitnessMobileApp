package ast;

public class CallStatement extends Statement{

    private final String id;
    private ExprList e;

    public CallStatement(String id, ExprList e, Location loc){
        super(loc);
        this.e=e;
        this.id = id;
    }

    public ExprList getExprList(){
        return e;
    }
    public String getIdentity(){
        return id;
    }
    
}
