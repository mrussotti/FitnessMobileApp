package ast;

import java.io.PrintStream;

public class VarDecl extends ASTNode {

    final boolean isMutable;
    final Type type;
    final String identifier;

    public VarDecl(boolean isMutable, Type t, Object ident, Location loc) {
        super(loc);
        this.isMutable = isMutable;
        this.type = t;
        this.identifier = ident.toString();
    }

    public VarDecl(boolean isMutable, Type t, String ident, Location loc) {
        super(loc);
        this.isMutable = isMutable;
        this.type = t;
        this.identifier = ident;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Type getType() {
        return type;
    }

    public int getRawType() {
        return type.getType();
    }

    public boolean isMutable() {
        return isMutable;
    }

}
