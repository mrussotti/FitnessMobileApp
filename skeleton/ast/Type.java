//<int>
package ast;

import java.io.PrintStream;

public class Type extends ASTNode {

// A program uses any of the non-heap, non-mutation, non-concurrency functionality (i.e., the functionality in
// the default color in Section 8.1) except function definition lists, formal declaration lists, expression lists, and
// call expressions.
    public static final int INT = 1;
    public static final int REF = 2;
    public static final int Q = 3;


    //a variable declaration will have data type followed by a string beginning with a character as a variable name
    final int type;


    //construct a program with an expression
    public Type(int type, Location loc) {
        super(loc);
        this.type = type;
    }

    // public Type(Ref type, Location loc) {
    //     super(loc);
    //     this.type = type;
    // }

    // public Type(Q type, Location loc) {
    //     super(loc);
    //     this.type = type;
    // }
    
}