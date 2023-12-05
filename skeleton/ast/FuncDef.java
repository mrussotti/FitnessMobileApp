//must implement <varDecl>, <stmtList>, since we don't have the funcDefList yet
package ast;

import java.io.PrintStream;

public class FuncDef extends Program {

// A program uses any of the non-heap, non-mutation, non-concurrency functionality (i.e., the functionality in
// the default color in Section 8.1) except function definition lists, formal declaration lists, expression lists, and
// call expressions.



    //a function definition will have variable declarations followed by a list of statements
    final VarDecl varDecl;
    final FormalDeclList formalDeclList;
    final StatementList stmtList;

    //construct a program with an expression
    public FuncDef(VarDecl varDecl, FormalDeclList formalDeclList, StatementList stmtList, Location loc) {
        super(loc);
        this.varDecl = varDecl;
        this.formalDeclList = formalDeclList;
        this.stmtList = stmtList;
    }
    public FuncDef(Location loc){
        super(loc);
        this.varDecl = null;
        this.formalDeclList = null;
        this.stmtList = null;
    }

    public VarDecl getVarDecl(){
        return varDecl;
    }

    public FormalDeclList getFormalDeclList(){//
        return formalDeclList;
    }

    public StatementList getStmtList(){
        return stmtList;
    }
    
}
