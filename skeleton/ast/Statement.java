package ast;
import java.io.PrintStream;

public class Statement extends ASTNode {

    
    final Expr expr;

    public Statement(Expr expr2, Location loc) {
        super(loc);
        this.expr = expr2;
    }
    
    public Statement(Location loc) {
        super(loc);
        this.expr = null;
    }

    public Expr getExpr() {
        return expr;
    }

    
    public void println(PrintStream ps) {
        ps.print(expr);

    }
}
