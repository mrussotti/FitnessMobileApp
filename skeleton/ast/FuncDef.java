//must implement <varDecl>, <stmtList>, since we don't have the funcDefList yet
package ast;

import java.io.PrintStream;

public class FuncDef extends Program {

// A program uses any of the non-heap, non-mutation, non-concurrency functionality (i.e., the functionality in
// the default color in Section 8.1) except function definition lists, formal declaration lists, expression lists, and
// call expressions.



    //a function definition will have variable declarations followed by a list of statements
    final VarDecl varDecl;
    final StmtList stmtList;

    //construct a program with an expression
    public FuncDef(VarDecl varDecl, StmtList stmtList, Location loc) {
        super(loc);
        this.varDecl = varDecl;
        this.stmtList = stmtList;
    }
    public FuncDef(Location loc){
        super(loc);
        this.varDecl = null;
        this.stmtList = null;
    }

    public VarDecl getVarDecl(){
        return varDecl;
    }
    
}
