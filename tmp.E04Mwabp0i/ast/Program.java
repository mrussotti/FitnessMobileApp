package ast;

import java.io.PrintStream;

public class Program extends ASTNode {

// A program uses any of the non-heap, non-mutation, non-concurrency functionality (i.e., the functionality in
// the default color in Section 8.1) except function definition lists, formal declaration lists, expression lists, and
// call expressions.



    //a program will have a list of function definitions, but for this part of the project we need only one function Main
    final FuncDefList funcDefList;

    // final Expr expr;
    // final Stmt stmt;


    //construct a program with a function definition
    public Program(FuncDefList funcDefList, Location loc){
        super(loc);
        this.funcDefList = funcDefList;
        // this.expr = null;
        // this.stmt = null;
    }
    public Program(Location loc){
        super(loc);
        this.funcDefList = null;
        // this.expr = null;
        // this.stmt = null;
    }

    //get a program's function definition
    public FuncDefList getFuncDefList(){
        return funcDefList;
    }


    // //old code from prev proj
    // //construct a program with an expression
    // public Program(Expr expr, Location loc) {
    //     super(loc);
    //     this.expr = expr;
    //     this.stmt = null;
    //     this.funcDef = null;
    // }

    // //construct a program with a statement
    // public Program(Stmt stmt, Location loc){
    //     super(loc);
    //     this.expr = null;
    //     this.stmt = stmt;
    //     this.funcDef = null;
    // }

    //get for a program's expression
    // public Expr getExpr() {
    //     return expr;
    // }

    // //get for a program's statement
    // public Stmt getStmt() {
    //     return stmt;
    // }

    // //print for the expr/stmt of a program
    // public void println(PrintStream ps) {
    //     ps.println(expr == null ? stmt : expr);
    // }
}
