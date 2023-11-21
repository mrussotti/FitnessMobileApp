package interpreter;
import java.io.*;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import parser.ParserWrapper;
import ast.*;
import java.util.HashMap;
import java.util.Map;

public class Interpreter {

    // Process return codes
    public static final int EXIT_SUCCESS = 0;
    public static final int EXIT_PARSING_ERROR = 1;
    public static final int EXIT_STATIC_CHECKING_ERROR = 2;
    public static final int EXIT_DYNAMIC_TYPE_ERROR = 3;
    public static final int EXIT_NIL_REF_ERROR = 4;
    public static final int EXIT_QUANDARY_HEAP_OUT_OF_MEMORY_ERROR = 5;
    public static final int EXIT_DATA_RACE_ERROR = 6;
    public static final int EXIT_NONDETERMINISM_ERROR = 7;

    static private Interpreter interpreter;

    public static Interpreter getInterpreter() {
        return interpreter;
    }

    public static void main(String[] args) {
        String gcType = "NoGC"; // default for skeleton, which only supports NoGC
        long heapBytes = 1 << 14;
        int i = 0;
        String filename;
        long quandaryArg;
        try {
            for (; i < args.length; i++) {
                String arg = args[i];
                if (arg.startsWith("-")) {
                    if (arg.equals("-gc")) {
                        gcType = args[i + 1];
                        i++;
                    } else if (arg.equals("-heapsize")) {
                        heapBytes = Long.valueOf(args[i + 1]);
                        i++;
                    } else {
                        throw new RuntimeException("Unexpected option " + arg);
                    }
                } else {
                    if (i != args.length - 2) {
                        throw new RuntimeException("Unexpected number of arguments");
                    }
                    break;
                }
            }
            filename = args[i];
            quandaryArg = Long.valueOf(args[i + 1]);
        } catch (Exception ex) {
            System.out.println("Expected format: quandary [OPTIONS] QUANDARY_PROGRAM_FILE INTEGER_ARGUMENT");
            System.out.println("Options:");
            System.out.println("  -gc (MarkSweep|Explicit|NoGC)");
            System.out.println("  -heapsize BYTES");
            System.out.println("BYTES must be a multiple of the word size (8)");
            return;
        }

        Program astRoot = null;
        Reader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        try {
            astRoot = ParserWrapper.parse(reader);
        } catch (Exception ex) {
            ex.printStackTrace();
            Interpreter.fatalError("Uncaught parsing error: " + ex, Interpreter.EXIT_PARSING_ERROR);
        }
        //astRoot.println(System.out);
        interpreter = new Interpreter(astRoot);
        interpreter.initMemoryManager(gcType, heapBytes);
        String returnValueAsString = interpreter.executeRoot(astRoot, quandaryArg).toString();
        System.out.println("Interpreter returned " + returnValueAsString);
    }

    final Program astRoot;
    final Random random;
    final Map<String, FuncDef> funcDefMap;

    private Interpreter(Program astRoot) {
        this.astRoot = astRoot;
        this.random = new Random();
        this.funcDefMap = new HashMap<String, FuncDef>();
    }

    void initMemoryManager(String gcType, long heapBytes) {
        if (gcType.equals("Explicit")) {
            throw new RuntimeException("Explicit not implemented");            
        } else if (gcType.equals("MarkSweep")) {
            throw new RuntimeException("MarkSweep not implemented");            
        } else if (gcType.equals("RefCount")) {
            throw new RuntimeException("RefCount not implemented");            
        } else if (gcType.equals("NoGC")) {
            // Nothing to do
        }
    }

    //must evaluate on expression, so need to cover the case that a program was constructed with a statement by getting the expression from the statement
    Object executeRoot(Program astRoot, long arg) {
        //builds into global map
        fillMap(astRoot.getFuncDefList());
        Map<String, Q> mainArgs = new HashMap<String, Q>();
        FuncDef main = funcDefMap.get("main");
        if(main != null){
            //get command line args from main as Identifier, value
            mainArgs.put(main.getFormalDeclList().getNeFormalDeclList().getVarDecl().getIdent(), new Q(arg));
            // run main
            return runFunc(main, mainArgs);
        }else{
            fatalError("no main method found", 0);
        }
        return null;
    }


    //method for placing functions in the global map
    Void fillMap(FuncDefList funcDefList){
        if(funcDefList != null){
            FuncDef funcDef = funcDefList.getFuncDef();
            String funcName = funcDef.getVarDecl().getIdent();
            System.out.println("filling: " + funcName);
            if(!funcDefMap.containsKey(funcName)){
                funcDefMap.put(funcName, funcDef);
            } else {
                fatalError("Duplicate function names found", 0);
            }
            //step
            fillMap(funcDefList.getNextFuncDef());
        }
        return null;
    }
    
    
    //method to execute functions
    Q runFunc(FuncDef func, Map<String, Q> funcArgs){
        //for functions the only things in scope so far are the args at this point, we will add to the below scope, but check args as well
        Map<String, Q> scope = new HashMap<String, Q>();
        StmtList l = func.getStmtList();
        Stmt s;
        //we iterate until the list is empty
        while(l != null){
            //take statement
            s = executeStmt(l.getStmt(), scope, funcArgs);
            //variable declaration
            if(s.getType() ==  2){
                String name = s.getVarDecl().getIdent();
                //Don't allow duplicate var names
                if(scope.containsKey(name)){
                    fatalError("Var name taken", 0);
                }
                //must check parent scope as well
                if(funcArgs.containsKey(name)){
                    fatalError("Var name taken", 0);
                }
                //not duplicate can add to scope
                scope.put(name, new Q(evaluateExpr(s.getExpr(), scope, funcArgs).getValue()));
            //handled delcaration, now handle return
            }else if(s.getType() == 1){
                return evaluateExpr(s.getExpr(), scope, funcArgs);
            }
            //pull off stmt we worked with
            l = l.getStmtList();
        }
        return null;
    }

    //method to evaluate expressions, takes expression, needs maps for scopeing, 
    Q evaluateExpr(Expr expr, Map<String, Q> scope, Map<String, Q> parScope){
        if(expr instanceof ConstExpr){
            return((ConstExpr) expr).getValue();
        // case for identifiers
        } else if(expr instanceof IDENT){
            // identifier is available in the current scope
            if(scope.containsKey(((IDENT) expr).getIdent())){
                return Long.parseLong(scope.get(((IDENT) expr).getIdent()));
            // identifier is available in the parent scope
            } else if(parScope.containsKey(((IDENT) expr).getIdent())){
                return Long.parseLong(parScope.get(((IDENT) expr).getIdent()));
            // past this the identifier is not available in scope
            } else {
                throw new RuntimeException("var doesn't exist");
            }
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr binaryExpr = (BinaryExpr)expr;
            switch (binaryExpr.getOperator()) {
                case BinaryExpr.PLUS: return new Q(evaluateExpr(binaryExpr.getLeftExpr(), scope, parScope).getValue() + evaluateExpr(binaryExpr.getRightExpr(), scope, parScope).getValue());
                case BinaryExpr.MINUS: return new Q(evaluateExpr(binaryExpr.getLeftExpr(), scope, parScope).getValue() - evaluateExpr(binaryExpr.getRightExpr(), scope, parScope).getValue());
                case BinaryExpr.TIMES: return new Q(evaluateExpr(binaryExpr.getLeftExpr(), scope, parScope).getValue() * evaluateExpr(binaryExpr.getRightExpr(), scope, parScope).getValue()); //multiplication for proj1
                case BinaryExpr.DOT: return new Q(new Ref(evaluateExpr(binaryExpr.getLeftExpr(), scope, parScope), evaluateExpr(binaryExpr.getRightExpr(), scope, parScope)));
                    
                default: throw new RuntimeException("Unhandled operator");
            }
        }
        //calls appear here, but first we need to implement FuncDefList and change runFunc to accept args as exprList
         else if(expr instanceof CallExpr){
            CallExpr callExpr = (CallExpr)expr;
            Expr temp = callExpr.getExprList().getNeExprList().getExpr();
            if(callExpr.getIdent().equals("randomInt")){
                //using ThreadLocalRandom to generate a long as Random's nextLong doesn't take parameters
                return new Q(ThreadLocalRandom.current().nextLong((evaluateExpr(temp, scope, parScope).getValue())));
            }
            if(callExpr.getIdent().equals("left")){
                // return left child of Ref
            }
            if(callExpr.getIdent().equals("right")){
                // return right child of Ref
            }
            if(callExpr.getIdent().equals("isAtom")){
                // return 1 if Q  is a nil Ref or an int, 0 otherwise
            }
            if(callExpr.getIdent().equals("isNil")){
                // return 1 if Q is nil, 0 otherwise
            }
            if(callExpr.getIdent().equals("setLeft")){
                // set left field of the Ref r to the Q value
            }
            if(callExpr.getIdent().equals("setRight")){
                // set right field of the Ref r to the Q value
            }
            FuncDef funcDef = funcDefMap.get(callExpr.getIdent());
            
            FormalDeclList formalDeclList= funcDef.getFormalDeclList();
            ExprList exprList = callExpr.getExprList();
            //should make sure the above lists are the same length. will fail also if only one list is null
            if(formalDeclList != null && exprList != null){
                if(formalDeclList.getNeFormalDeclList().length() != exprList.getNeExprList().length()){
                    fatalError("Incorrect number of parameters passed to method: " + funcDef.getVarDecl().getIdent(), 0);
                }
            }else if((formalDeclList == null && exprList != null) || (formalDeclList != null && exprList == null)){
                fatalError("Incorrect number of parameters passed to method: " + funcDef.getVarDecl().getIdent(), 0);
            }
            Map<String, Q> args = new HashMap<String, Q>();
            if(formalDeclList != null && exprList != null){
                // only runs if there are args to fill, will runFunc handle empty map ok?
                fillArgs(args, scope, formalDeclList.getNeFormalDeclList(), exprList.getNeExprList(), parScope);
            }
            return runFunc(funcDef, args);
        } else if(expr instanceof TypeCast){
            TypeCast typeCast = (TypeCast) expr;
            //what type are we casting from
            switch(typeCast.getCastExpr().getType()){
                case 1:
                    //From Int to Q
                    return evaluateExpr(typeCast.getCastExpr(), scope, parScope);
                case 2:
                    //From Ref to Q
                    return evaluateExpr(typeCast.getCastExpr(), scope, parScope);;
                case 3:
                    if(typeCast.getCastType().getType() == 1){
                        return new Int(evaluateExpr(typeCast.getCastExpr(), scope, parScope).getValue());
                    }else if(typeCast.getCastType().getType() == 2){
                        return new Ref(evaluateExpr(typeCast.getCastExpr(), scope, parScope).getRef().getLeft(), evaluateExpr(typeCast.getCastExpr(), scope, parScope).getRef().getRight());
                        
                    }
                    return null;
                default: throw new RuntimeException("unknown type");

            }
        }
         else {
            throw new RuntimeException("Unhandled Expr type");
        }
    }

    //method for placing args in the map
    Void fillArgs(Map<String, Q> funcArgs, Map<String, Q> scopeArgs, NeFormalDeclList declList, NeExprList exprList, Map<String, Q> parScope){
        if(declList != null && exprList != null){
            VarDecl varDecl = declList.getVarDecl();
            Expr expr = exprList.getExpr();
            funcArgs.put(varDecl.getIdent(), evaluateExpr(expr, scopeArgs, parScope));
            //step
            fillArgs(funcArgs, scopeArgs, declList.getNeFormalDeclList(), exprList.getNeExprList(), parScope);
        }
        return null;
    }

    //method to handle condition evaluation
    // calls evaluate expression, passing forward scopeVars
    boolean evaluateCond(Cond cond, Map<String, Q> scope, Map<String, Q> parScope){
        switch (cond.getOperator()) {
            case 1:
                // Handle less than or equal to
                return evaluateExpr(cond.getE1(), scope, parScope).getValue().value <= evaluateExpr(cond.getE2(), scope, parScope).getValue().value;
            case 2:
                // Handle greater than or equal to
                return  evaluateExpr(cond.getE1(), scope, parScope).getValue().value >=  evaluateExpr(cond.getE2(), scope, parScope).getValue().value;
            case 3:
                // Handle equals
                return ( evaluateExpr(cond.getE1(), scope, parScope).getValue().value).equals( evaluateExpr(cond.getE2(), scope, parScope).getValue().value);
            case 4:
                // Handle not equals
                return  evaluateExpr(cond.getE1(), scope, parScope).getValue().value !=  evaluateExpr(cond.getE2(), scope, parScope).getValue().value;
            case 5:
                // Handle less than
                return  evaluateExpr(cond.getE1(), scope, parScope).getValue().value <  evaluateExpr(cond.getE2(), scope, parScope).getValue().value;
            case 6:
                // Handle greater than
                return  evaluateExpr(cond.getE1(), scope, parScope).getValue().value >  evaluateExpr(cond.getE2(), scope, parScope).getValue().value;
            case 7:
                // Handle logical AND
                return evaluateCond(cond.getC1(), scope, parScope) && evaluateCond(cond.getC2(), scope, parScope);
            case 8:
                // Handle logical OR
                return evaluateCond(cond.getC1(), scope, parScope) || evaluateCond(cond.getC2(), scope, parScope);
            case 9:
                // Handle logical NOT
                return ! evaluateCond(cond.getC1(), scope, parScope);
            default:
                // Handle default case
                fatalError("operator doesn't exist", 0);
        //Need to handle parentheses? Operator types for this don't seem to work
        }
        return false;
    }

	public static void fatalError(String message, int processReturnCode) {
        System.out.println(message);
        System.exit(processReturnCode);
	}


    // handle executing the statement
    Stmt executeStmt(Stmt s, Map<String, Q> scope, Map<String, Q> parScope){
        //handle different types of statements
        switch (s.getType()) {
            case 1:
                // Handle return statement
                return s;
            case 2:
                // Handle equals statement for variable declaration
                // want to store variables in a variable map where they're paired with the function they were created in for scoping
                return s;
            case 3:
                // Handle if statement
                if(evaluateCond(s.getCond(), scope, parScope)){
                    return executeStmt(s.getStmt1(), scope, parScope);
                }
                return s;
            case 4:
                // Handle if-else
                if(evaluateCond(s.getCond(), scope, parScope)){
                    return executeStmt(s.getStmt1(), scope, parScope);
                }else{
                    return executeStmt(s.getStmt2(), scope, parScope);
                }
            case 5:
                // Handle print statement
                System.out.println(evaluateExpr(s.getExpr(), scope, parScope));
                return s;
            case 6:
                // Handle statement block
                // update scope for the specific statement list
                Map<String, Q> updateParScope = new HashMap<String, Q>(parScope);
                //move current scope into the new parent scope
                updateParScope.putAll(scope);
                //now create current scope to add stuff to for the list
                Map<String, Q> currentScope = new HashMap<String, Q>();
                Stmt stmt;
                StmtList l = s.getStmtList();
                //now iterate through statement list
                while(l != null){
                    //take stmt to execute
                    stmt = executeStmt(l.getStmt(), currentScope, updateParScope);
                    //if we are doing a declaration
                    if(stmt.getType() ==  2){
                        String name = stmt.getVarDecl().getIdent();
                        //Don't allow duplicate var names
                        if(currentScope.containsKey(name)){
                            fatalError("Var name taken", 0);
                        }
                        //must check parent scope as well
                        if(updateParScope.containsKey(name)){
                            fatalError("Var name taken", 0);
                        }
                        //not duplicate can add to scope
                        currentScope.put(name, evaluateExpr(stmt.getExpr(), currentScope, updateParScope));
                    //handled delcaration, now handle return
                    }else if(stmt.getType() == 1){
                        Expr build = new ConstExpr(evaluateExpr(stmt.getExpr(), currentScope, updateParScope).getValue(), null); 
                        Stmt out = new Stmt(Stmt.RETURN, build, null);
                        return out;
                    }
                    //pulls off statement we worked with
                    l = l.getStmtList();
                }
                return s;
            case 7:
                //assignment
                String ident = s.getIdent();
                //types?
                Q value = evaluateExpr(s.getExpr(), scope, parScope);
                scope.replace(ident, value);
                return s;
            case 8:
                //while
                while(evaluateCond(s.getCond(), scope, parScope)){
                    Stmt run = executeStmt(s.getStmt1(), scope, parScope);
                    if(run.getType() == 1){
                        return run;
                    }
                }
                return s;
            case 9:
                //Call
                String i = s.getIdent();
                ExprList exprList = s.getExprList();
                //how to construct outside of parser?
                CallExpr call = new CallExpr(i, exprList, null);
                evaluateExpr(call, scope, parScope);
                return s;
            default:
                // Handle default case
        }
        return s;
    }
}
