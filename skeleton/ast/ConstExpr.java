package ast;

public class ConstExpr extends Expr {

    final Q value;

    public ConstExpr(long value, Location loc) {
        super(loc);
        this.value = new Q(value);
    }

    public Q getValue() {
        return value;
    }

    //@Override
    public String toString() {
        return value.toString();
    }
}
