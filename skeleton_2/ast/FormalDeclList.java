package ast;

import java.io.PrintStream;

import ast.ASTNode;

public class FormalDeclList extends ASTNode {

    final VarDecl varDecl;
    final FormalDeclList formalDeclList;

    public FormalDeclList(VarDecl varDecl, FormalDeclList formalDeclList, Location loc) {
        super(loc);
        this.varDecl = varDecl;
        this.formalDeclList = formalDeclList;
    }

    public VarDecl getVarDecl() {
        return varDecl;
    }

    public FormalDeclList getFormalDeclList() {
        return formalDeclList;
    }

    public int length() {
        if (this.formalDeclList != null) {
            return 1 + this.formalDeclList.length();
        } else {
            return 1;
        }
    }
}
