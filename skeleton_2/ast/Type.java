package ast;

import java.io.PrintStream;

public class Type extends ASTNode {

    public static final int INT = 1;
    public static final int REF = 2;
    public static final int Q = 3;
    public static final int NIL = 4;

    final int type;

    public Type(int type, Location loc) {
        super(loc);
        this.type = type;
    }

    public int getType() {
        return type;
    }

}
