package ast;

public class IfElseStatement extends Statement{

    final Cond c;
    final Statement thenBody, elseBody;

    public IfElseStatement(Cond c, Statement thenBody,Statement elseBody, Location loc){
        super(loc);
        this.c=c;
        this.thenBody=thenBody;
        this.elseBody=elseBody;
    }
    public Statement getThenBody(){
        return thenBody;
    }
    public Statement getElseBody(){
        return elseBody;
    }

    public Cond getCond(){
        return c;
    }
    
}
