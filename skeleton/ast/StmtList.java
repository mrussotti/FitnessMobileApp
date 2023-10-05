//<stmt><stmtList> or empty
package ast;

import java.io.PrintStream;

public abstract class StmtList extends FuncDef {

// A program uses any of the non-heap, non-mutation, non-concurrency functionality (i.e., the functionality in
// the default color in Section 8.1) except function definition lists, formal declaration lists, expression lists, and
// call expressions.



    //a variable declaration will have data type followed by a string beginning with a character as a variable name
    final Stmt stmt;
    final StmtList stmtList;

    //construct a program with an expression
    public StmtList(Stmt stmt, StmtList stmtList, Location loc) {
        super(loc);
        this.Stmt = stmt;
        this.StmtList = stmtList;
    }

    
}