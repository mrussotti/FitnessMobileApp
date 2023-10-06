package ast;

public class IDENT extends Expr {

    final Object value;

    public IDENT(String value, Location loc) {
        super(loc);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
