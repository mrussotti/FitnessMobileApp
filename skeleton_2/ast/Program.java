package ast;

import java.io.PrintStream;

public class Program extends ASTNode {

    final FuncDefList funcDefList;

    public Program(FuncDefList funcDefList, Location loc) {
        super(loc);
        this.funcDefList = funcDefList;
    }

    public FuncDefList getFuncDefList() {
        return this.funcDefList;
    }

}
