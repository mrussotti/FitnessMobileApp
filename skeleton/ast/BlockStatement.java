package ast;

public class BlockStatement extends Statement{

    final StatementList body;

    public BlockStatement(StatementList s, Location loc){
        super(loc);
        this.body=s;
    }

    public StatementList getStmtList(){
        return body;
    }


    
}
