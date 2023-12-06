package ast;

import java.io.PrintStream;

public class FormalDeclList extends FuncDef {

    //a variable declaration will have data type followed by a string beginning with a character as a variable name
    final NeFormalDeclList neFormalDeclList;

    public FormalDeclList(NeFormalDeclList neFormalDeclList, Location loc) {
        super(loc);
        this.neFormalDeclList = neFormalDeclList;
    }

    public FormalDeclList(Location loc){
        super(loc);
        this.neFormalDeclList = null;
    }

    public NeFormalDeclList getNeFormalDeclList() {
        return neFormalDeclList;
    }


    
}