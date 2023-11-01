package ast;

import java.io.PrintStream;

public class NeFormalDeclList extends FuncDef {

    //a variable declaration will have data type followed by a string beginning with a character as a variable name
    final VarDecl varDecl;
    final NeFormalDeclList neFormalDeclList;

    //construct a program with an expression
    public NeFormalDeclList(VarDecl varDecl, NeFormalDeclList neFormalDeclList, Location loc) {
        super(loc);
        this.varDecl = varDecl;
        this.neFormalDeclList = neFormalDeclList;
    }

    public NeFormalDeclList(VarDecl varDecl, Location loc){
        super(loc);
        this.varDecl = varDecl;
        this.neFormalDeclList = null;
    }

    //get for a stmtLists's list
    public VarDecl getVarDecl() {
        return varDecl;
    }

    //get for a stmtList's statement
    public NeFormalDeclList getNeFormalDeclList() {
        return neFormalDeclList;
    }


    
}