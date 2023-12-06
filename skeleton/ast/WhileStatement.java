package ast;

public class WhileStatement extends Statement{

    final Condition c;
    final Statement s;

    public WhileStatement(Condition c, Statement s, Location loc){
        super(loc);
        this.s=s;
        this.c=c;
    }

    public Statement getBody(){
        return s;
    }

    public Condition getCond(){
        return c;
    }
    
}
