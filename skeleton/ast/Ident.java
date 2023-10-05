package ast;

public class Ident extends Expr {

    final Object value;

    public Ident(String value, Location loc) {
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
