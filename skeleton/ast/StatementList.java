package ast;

public class StatementList extends ASTNode{
    final Statement s;
    final StatementList sl;

    public StatementList(Statement s,StatementList sl, Location loc){//
        super(loc);
        this.sl=sl;
        this.s = s;
    }

    public Statement getStmt(){
        return s;
    }

    public StatementList getStmtList(){
        return sl;//may be null
    }
}
