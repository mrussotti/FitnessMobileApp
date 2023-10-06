//<type> IDENT or mutable <type> IDENT
package ast;

import java.io.PrintStream;

public class VarDecl extends FuncDef {

// A program uses any of the non-heap, non-mutation, non-concurrency functionality (i.e., the functionality in
// the default color in Section 8.1) except function definition lists, formal declaration lists, expression lists, and
// call expressions.



    //a variable declaration will have data type followed by a string beginning with a character as a variable name
    final Type type;
    final String ident;

    //construct a program with an expression
    public VarDecl(Type type, String ident, Location loc) {
        super(loc);
        this.type = type;
        this.ident = ident;
    }

    public String getIdent(){
        return ident;
    }

    
}