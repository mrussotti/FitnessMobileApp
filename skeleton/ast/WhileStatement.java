package ast;

public class WhileStatement extends Statement{

    final Cond c;
    final Statement s;

    public WhileStatement(Cond c, Statement s, Location loc){
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
