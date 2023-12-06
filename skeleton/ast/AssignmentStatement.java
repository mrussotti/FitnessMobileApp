package ast;

public class AssignmentStatement extends Statement{

    private String id;
    private Expr e;

    public AssignmentStatement(String id, Expr e, Location loc){
        super(loc);
        this.e=e;
        this.id=id;
    }

    public Expr getExpr(){
        return e;
    }

    public String getIdentity(){
        return id;
    }
    
}
