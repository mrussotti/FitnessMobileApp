package ast;

public class IfStatement extends Statement{

    final Cond c;
    final Statement s;

    public IfStatement(Cond c, Statement s, Location loc){
        super(loc);
        this.s=s;
        this.c=c;
    }

    public Statement getBody(){
        return s;
    }

    public Cond getCond(){
        return c;
    }
    
}
