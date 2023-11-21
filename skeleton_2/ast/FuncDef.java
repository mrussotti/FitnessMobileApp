package ast;

import java.io.PrintStream;

public class FuncDef extends ASTNode {

    final VarDecl varDecl;
    final FormalDeclList formalDeclList;
    final StmtList stmtList;

    public FuncDef(VarDecl v, FormalDeclList dl, StmtList sl, Location loc) {
        super(loc);
        this.varDecl = v;
        this.formalDeclList = dl;
        this.stmtList = sl;
    }

    public VarDecl getVarDecl() {
        return varDecl;
    }

    public FormalDeclList getFormalDeclList() {
        return formalDeclList;
    }

    public StmtList getStmtList() {
        return stmtList;
    }

}
