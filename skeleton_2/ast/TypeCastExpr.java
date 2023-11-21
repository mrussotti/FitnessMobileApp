package ast;

public class TypeCastExpr extends Expr {

    final Type type;
    final Expr expr;

    public TypeCastExpr(Type t, Expr e, Location loc) {
        super(loc);
        this.type = t;
        this.expr = e;
    }

    public Expr getExpr() {
        return this.expr;
    }

    public Type getType() {
        return this.type;
    }

    public int getRawType() {
        return this.type.getType();
    }

}
