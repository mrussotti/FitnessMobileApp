package ast;

public class IfStatement extends Statement{

    final Condition c;
    final Statement s;

    public IfStatement(Condition c, Statement s, Location loc){
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
