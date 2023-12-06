package ast;

public class IfElseStatement extends Statement{

    final Condition c;
    final Statement thenBody, elseBody;

    public IfElseStatement(Condition c, Statement thenBody,Statement elseBody, Location loc){
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

    public Condition getCond(){
        return c;
    }
    
}
