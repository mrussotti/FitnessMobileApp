//must implement <varDecl>, <stmtList>, since we don't have the funcDefList yet
package ast;

import java.io.PrintStream;

public class FuncDef extends Program {

// A program uses any of the non-heap, non-mutation, non-concurrency functionality (i.e., the functionality in
// the default color in Section 8.1) except function definition lists, formal declaration lists, expression lists, and
// call expressions.



    //a function definition will have variable declarations followed by a list of statements
    final VarDecl varDecl1;
    final VarDecl varDecl2;
    final StmtList stmtList;

    //construct a program with an expression
    public FuncDef(VarDecl varDecl1, VarDecl varDecl2, StmtList stmtList, Location loc) {
        super(loc);
        this.varDecl1 = varDecl1;
        this.varDecl2 = varDecl2;
        this.stmtList = stmtList;
    }
    public FuncDef(Location loc){
        super(loc);
        this.varDecl1 = null;
        this.varDecl2 = null;
        this.stmtList = null;
    }

    public VarDecl getVarDecl1(){
        return varDecl1;
    }

    public VarDecl getVarDecl2(){
        return varDecl2;
    }

    public StmtList getStmtList(){
        return stmtList;
    }
    
}
