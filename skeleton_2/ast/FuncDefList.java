package ast;

import java.io.PrintStream;

import ast.ASTNode;
import ast.Location;

public class FuncDefList extends ASTNode {

    final FuncDef funcDef;
    final FuncDefList funcDefList;

    public FuncDefList(FuncDef funcDef, FuncDefList funcDefList, Location loc) {
        super(loc);
        this.funcDef = funcDef;
        this.funcDefList = funcDefList;
    }

    public FuncDef getFuncDef() {
        return funcDef;
    }

    public FuncDefList getFuncDefList() {
        return funcDefList;
    }

    public int length() {
        if (this.funcDefList != null) {
            return 1 + this.funcDefList.length();
        } else {
            return 1;
        }
    }
}
