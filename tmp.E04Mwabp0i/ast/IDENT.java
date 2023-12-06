package ast;

public class IDENT extends Expr {

    final String value;

    public IDENT(String value, Location loc) {
        super(loc);
        this.value = value;
    }

    public String getIdent() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
