package ast;

public class IdentExpr extends Expr {

    final String indetifier;

    public IdentExpr(Object i, Location loc) {
        super(loc);
        this.indetifier = i.toString();
    }

    public String getIndetifier() {
        return indetifier;
    }
}
