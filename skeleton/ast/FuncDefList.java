package ast;
import java.util.HashMap;
import java.util.Map;
import java.io.PrintStream;

public class FuncDefList extends Program {

    //a function definition will have variable declarations followed by a list of statements
    final FuncDef funcDef;
    final FuncDefList funcDefList;

    //construct a program with an expression
    public FuncDefList(FuncDef funcDef, FuncDefList funcDefList, Location loc) {
        super(loc);
        this.funcDef = funcDef;
        this.funcDefList = funcDefList;
    }
    public FuncDefList(Location loc){
        super(loc);
        this.funcDef = null;
        this.funcDefList = null;
    }

    public FuncDef getFuncDef(){
        return funcDef;
    }

    // public FuncDefList getFuncDefList(){
    //     return funcDefList;
    // }
    
    //method for placing functions in the map
    public void fill(Map<String, FuncDef> funcDefMap){
        funcDefMap.put(funcDef.getVarDecl().getIdent(), funcDef);
        if(funcDefList != null){
            funcDefList.fill(funcDefMap);
        }
    }
}
