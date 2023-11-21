package ast;

import java.io.PrintStream;

import ast.ASTNode;
import ast.Stmt;

public class StmtList extends ASTNode {

    final Stmt stmt;
    final StmtList stmtList;

    public StmtList(Stmt stmt, StmtList stmtList, Location loc) {
        super(loc);
        this.stmt = stmt;
        this.stmtList = stmtList;
    }

    public Stmt getStmt() {
        return stmt;
    }

    public StmtList getStmtList() {
        return stmtList;
    }

    public int length() {
        if (this.stmtList != null) {
            return 1 + this.stmtList.length();
        } else {
            return 1;
        }
    }
}
