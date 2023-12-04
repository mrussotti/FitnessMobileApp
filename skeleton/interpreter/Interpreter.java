package interpreter;
import java.io.*;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;


import parser.ParserWrapper;
import ast.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

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
                fatalError("Duplicate function names found" + funcName, 0);
            }
            //step
            System.out.println(funcDefList.getNextFuncDef());
            fillMap(funcDefList.getNextFuncDef());
        }
        return null;
    }
    
    public boolean withinScopeStack(List<Map<String, Q>> scopeStack, String name){
        for (Map<String, Q> map : scopeStack){
            if(map.containsKey(name)){
                return true;
            }
        }
        return false;
    }

    public Q getFromScopeStack(List<Map<String, Q>> scopeStack, String name){
        for (Map<String, Q> map : scopeStack){
            if(map.containsKey(name)){
                Q obj = map.get(name);
                if(obj.inte != null){
                    return new Q(obj.inte.value);
                }
                if(obj.heap != Ref.NIL){
                    return new Q(obj.heap);
                }
                return new Q();
            }
        }
        return null;
    }

    public void addToScopeStack(List<Map<String, Q>> scopeStack, String name, Q q){
        for(int i = scopeStack.size()-1; i>=0; i--){
            if(!scopeStack.get(i).containsKey(name)){
                scopeStack.get(i).put(name, q);
            }
        }
    }

    public void updateScopeStack(List<Map<String, Q>> scopeStack, String name, Q q){
        for(int i = scopeStack.size()-1; i>=0; i--){
            if(scopeStack.get(i).containsKey(name)){
                // if(q.inte != null){
                //     scopeStack.get(i).replace(name, new Q(q.inte.value));
                // }
                // if(q.heap != Ref.NIL){
                //     scopeStack.get(i).replace(name, new Q(q.heap));
                // }
                scopeStack.get(i).replace(name, q);
            }
        }
    }

    //method to execute functions
    Q runFunc(FuncDef func, Map<String, Q> funcArgs){
        //for functions the only things in scope so far are the args at this point, we will add to the below scope, but check args as well
        Map<String, Q> scope = new HashMap<String, Q>();
        List<Map<String, Q>> scopeStack = new ArrayList<Map<String, Q>>();
        scopeStack.add(funcArgs);
        scopeStack.add(scope);
        StmtList l = func.getStmtList();
        Stmt s;
        //we iterate until the list is empty
        while(l != null){
            //take statement
            s = executeStmt(l.getStmt(), scopeStack);
            //variable declaration
            if(s.getType() ==  2){
                String name = s.getVarDecl().getIdent();
                //Don't allow duplicate var names
                if(withinScopeStack(scopeStack, name)){
                    fatalError("Var name taken (original stmtList)", 0);
                }
                //not duplicate can add to scope
                //System.out.println(name);
                scope.put(name, evaluateExpr(s.getExpr(), scopeStack));
                if(!scopeStack.contains(scope)){
                    scopeStack.add(scope);
                }
            //handled delcaration, now handle return
            }else if(s.getType() == 1){
                return evaluateExpr(s.getExpr(), scopeStack);
            }
            //pull off stmt we worked with
            l = l.getStmtList();
        }
        return null;
    }

    //method to evaluate expressions, takes expression, needs maps for scopeing, 
    Q evaluateExpr(Expr expr, List<Map<String, Q>> scopeStack){
        if(expr instanceof NilExpr){
            return new Q(Ref.NIL);
        }else if(expr instanceof ConstExpr){
            return((ConstExpr) expr).getValue();
        // case for identifiers
        } else if(expr instanceof IDENT){
            // identifier is available in the current scope
            if(withinScopeStack(scopeStack, ((IDENT) expr).getIdent())){
                return getFromScopeStack(scopeStack, ((IDENT) expr).getIdent());
            // past this the identifier is not available in scope
            } else {
                throw new RuntimeException("var doesn't exist: " + ((IDENT)expr).getIdent());
            }
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr binaryExpr = (BinaryExpr)expr;
            switch (binaryExpr.getOperator()) {
                case BinaryExpr.PLUS: return new Q(evaluateExpr(binaryExpr.getLeftExpr(), scopeStack).getINT().value + evaluateExpr(binaryExpr.getRightExpr(), scopeStack).getINT().value);
                case BinaryExpr.MINUS: return new Q(evaluateExpr(binaryExpr.getLeftExpr(), scopeStack).getINT().value - evaluateExpr(binaryExpr.getRightExpr(), scopeStack).getINT().value);
                case BinaryExpr.TIMES: return new Q(evaluateExpr(binaryExpr.getLeftExpr(), scopeStack).getINT().value * evaluateExpr(binaryExpr.getRightExpr(), scopeStack).getINT().value); //multiplication for proj1
                case BinaryExpr.DOT: return new Q(new Ref(evaluateExpr(binaryExpr.getLeftExpr(), scopeStack), evaluateExpr(binaryExpr.getRightExpr(), scopeStack)));
                    
                default: throw new RuntimeException("Unhandled operator");
            }
        }
        //calls appear here, but first we need to implement FuncDefList and change runFunc to accept args as exprList
         else if(expr instanceof CallExpr){
            CallExpr callExpr = (CallExpr)expr;
            if(callExpr.getIdent().equals("randomInt")){
                //using ThreadLocalRandom to generate a long as Random's nextLong doesn't take parameters
                return new Q(ThreadLocalRandom.current().nextLong((evaluateExpr(callExpr.getExprList().getNeExprList().getExpr(), scopeStack).getINT().getValue())));
            }
            if(callExpr.getIdent().equals("left")){
                // return left child of Ref
                Q ret = evaluateExpr(callExpr.getExprList().getNeExprList().getExpr(), scopeStack);
                return ret.getRef().getLeft();
            }
            if(callExpr.getIdent().equals("right")){
                // return right child of Ref
                Q ret = evaluateExpr(callExpr.getExprList().getNeExprList().getExpr(), scopeStack);
                return ret.getRef().getRight();
            }
            if(callExpr.getIdent().equals("isAtom")){
                // return 1 if Q is a nil Ref or an int, 0 otherwise
                Q ret = evaluateExpr(callExpr.getExprList().getNeExprList().getExpr(), scopeStack);
                if(ret.getINT() == null && ret.getRef()== null){
                    ret.heap = Ref.NIL;
                }
                if(ret.getRef() == null && ret.getINT() != null){
                    return new Q((long) 1);
                }
                if(ret.getRef().isNil()){
                    return new Q((long) 1);
                }
                return new Q((long) 0);
            }
            if(callExpr.getIdent().equals("isNil")){
                // return 1 if Q is nil, 0 otherwise
                Q ret = evaluateExpr(callExpr.getExprList().getNeExprList().getExpr(), scopeStack);
                if(ret.getINT() == null && ret.getRef()== null){
                    ret.heap = Ref.NIL;
                }
                if(ret.getINT() != null || ret.getRef() == null || !ret.getRef().isNil()){
                    //System.out.println("not nil");
                    
                        return new Q((long)0);
                    
                }
                //System.out.println("nil");
                return new Q((long)1);
            }
            if(callExpr.getIdent().equals("setLeft")){
                // set left field of the Ref r to the Q value
                Q ret = evaluateExpr(callExpr.getExprList().getNeExprList().getExpr(), scopeStack);
                Q val= evaluateExpr(callExpr.getExprList().getNeExprList().getNeExprList().getExpr(), scopeStack);
                ret.heap.setLeft(val);
                return new Q((long)1 );
            }
            if(callExpr.getIdent().equals("setRight")){
                // set right field of the Ref r to the Q value
                Q ret = evaluateExpr(callExpr.getExprList().getNeExprList().getExpr(), scopeStack);
                Q val= evaluateExpr(callExpr.getExprList().getNeExprList().getNeExprList().getExpr(), scopeStack);
                ret.heap.setRight(val);
                return new Q((long)1 );
            }
            if (callExpr.getIdent().equals("acq")){
                // aquire lock of object referenced by r, return 1
                Ref r = evaluateExpr(callExpr.getExprList().getNeExprList().getExpr(), scopeStack).heap;
                int count = 0;
                while(r.isLocked()){
                    //System.out.println("Waiting for unlock on: " + r.toString());
                    System.out.print("");
                    // if(count > 10000){
                    //     break;
                    // }
                    count++;
                }
                //synchronized(r){
                    //if(!r.isLocked()){
                        r.lock();
                    //}

                //}
                return new Q((long)1 );
            }
            if (callExpr.getIdent().equals("rel")){
                // release lock of object referenced by r, return1
                Ref r = evaluateExpr(callExpr.getExprList().getNeExprList().getExpr(), scopeStack).heap;
                //synchronized(r){
                    //if(!r.isLocked()){
                        r.release();
                    //}else{
                        //System.out.println("release error");
                    //}
                //}
                return new Q((long)1 );
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
                fillArgs(args, scopeStack, formalDeclList.getNeFormalDeclList(), exprList.getNeExprList());
            }
            return runFunc(funcDef, args);
        } else if(expr instanceof TypeCast){
            TypeCast typeCast = (TypeCast) expr;
            //what type are we casting from doesn't matter
            return evaluateExpr(typeCast.getCastExpr(), scopeStack);
        } else if( expr instanceof ConcurrentExpression){
            ConcurrentExpression concurrentExpression = (ConcurrentExpression) expr;
            BinaryExpr binaryExpr = concurrentExpression.getBinaryExpr();

            // Create a ThreadPoolExecutor with 2 threads
            ExecutorService executor = Executors.newFixedThreadPool(4);

            // Create and submit the Callable objects directly
            Future<Q> future1 = executor.submit(new ConcurrentExpr(binaryExpr.getLeftExpr(), scopeStack));
            Future<Q> future2 = executor.submit(new ConcurrentExpr(binaryExpr.getRightExpr(), scopeStack));

            // Wait for all computations to complete
            Q result1 = null;
            Q result2 = null;
            System.out.println("we're waiting on results now");
            try {
                System.out.println("waiting on thread 1 result");
                result1 = future1.get();
                System.out.println("waiting on thread 2 result");
                result2 = future2.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            // Shutdown the executor
            executor.shutdown();

            // Use the results to create a new binaryExpression object and pass it into evaluate expr
            switch (binaryExpr.getOperator()) {
                case BinaryExpr.PLUS: return new Q(result1.getINT().getValue() + result2.getINT().getValue());
                case BinaryExpr.MINUS: return new Q(result1.getINT().getValue() - result2.getINT().getValue());
                case BinaryExpr.TIMES: return new Q(result1.getINT().getValue() * result2.getINT().getValue()); //multiplication for proj1
                case BinaryExpr.DOT: return new Q(new Ref(result1, result2));

                default: throw new RuntimeException("Unhandled operator");
            }

        }
         else {
            throw new RuntimeException("Unhandled Expr type");
        }
    }

    //method for placing args in the map
    Void fillArgs(Map<String, Q> funcArgs, List<Map<String, Q>> scopeStack, NeFormalDeclList declList, NeExprList exprList){
        if(declList != null && exprList != null){
            VarDecl varDecl = declList.getVarDecl();
            Expr expr = exprList.getExpr();
            funcArgs.put(varDecl.getIdent(), evaluateExpr(expr, scopeStack));
            //step
            fillArgs(funcArgs, scopeStack, declList.getNeFormalDeclList(), exprList.getNeExprList());
        }
        return null;
    }

    //method to handle condition evaluation
    // calls evaluate expression, passing forward scopeVars
    boolean evaluateCond(Cond cond, List<Map<String, Q>> scopeStack){
        switch (cond.getOperator()) {
            case 1:
                // Handle less than or equal to
                return evaluateExpr(cond.getE1(), scopeStack).getINT().getValue() <= evaluateExpr(cond.getE2(), scopeStack).getINT().getValue();
            case 2:
                // Handle greater than or equal to
                return  evaluateExpr(cond.getE1(), scopeStack).getINT().getValue() >=  evaluateExpr(cond.getE2(), scopeStack).getINT().getValue();
            case 3:
                // Handle equals
                return ( evaluateExpr(cond.getE1(), scopeStack).getINT().getValue()).equals( evaluateExpr(cond.getE2(), scopeStack).getINT().getValue());
            case 4:
                // Handle not equals
                return  evaluateExpr(cond.getE1(), scopeStack).getINT().getValue() !=  evaluateExpr(cond.getE2(), scopeStack).getINT().getValue();
            case 5:
                // Handle less than
                return  evaluateExpr(cond.getE1(), scopeStack).getINT().getValue() <  evaluateExpr(cond.getE2(), scopeStack).getINT().getValue();
            case 6:
                // Handle greater than
                return  evaluateExpr(cond.getE1(), scopeStack).getINT().getValue() >  evaluateExpr(cond.getE2(), scopeStack).getINT().getValue();
            case 7:
                // Handle logical AND
                return evaluateCond(cond.getC1(), scopeStack) && evaluateCond(cond.getC2(), scopeStack);
            case 8:
                // Handle logical OR
                return evaluateCond(cond.getC1(), scopeStack) || evaluateCond(cond.getC2(), scopeStack);
            case 9:
                // Handle logical NOT
                return ! evaluateCond(cond.getC1(), scopeStack);
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
    Stmt executeStmt(Stmt s, List<Map<String, Q>> scopeStack){
        //handle different types of statements
        switch (s.getType()) {
            case 1:
                // Handle return statement
                return s;
            case 2:
                // Handle equals statement for variable declaration
                // want to store variables in a variable map where they're paired with the function they were created in for scoping
                // String ide = s.getVarDecl().getIdent();
                // if(!withinScopeStack(scopeStack, ide)){
                //     int len = scopeStack.size();
                //     scopeStack.get(len-1).put(ide, evaluateExpr(s.getExpr(), scopeStack));

                // }
                return s;
            case 3:
                // Handle if statement
                if(evaluateCond(s.getCond(), scopeStack)){
                    return executeStmt(s.getStmt1(), scopeStack);
                }
                return s;
            case 4:
                // Handle if-else
                if(evaluateCond(s.getCond(), scopeStack)){
                    return executeStmt(s.getStmt1(), scopeStack);
                }else{
                    return executeStmt(s.getStmt2(), scopeStack);
                }
            case 5:
                // Handle print statement
                System.out.println(evaluateExpr(s.getExpr(), scopeStack));
                return s;
            case 6:
                // Handle statement block
                
                //now create current scope to add stuff to for the list
                Map<String, Q> currentScope = new HashMap<String, Q>();
                //System.out.println("stack: " + scopeStack.size());
                scopeStack.add(currentScope);
                //System.out.println(scopeStack.size());
                Stmt stmt;
                StmtList l = s.getStmtList();
                //now iterate through statement list
                while(l != null){
                    //take stmt to execute
                    stmt = executeStmt(l.getStmt(), scopeStack);
                    //if we are doing a declaration
                    if(stmt.getType() ==  2){
                        String name = stmt.getVarDecl().getIdent();
                        //Don't allow duplicate var names
                        if(withinScopeStack(scopeStack, name)){
                            fatalError("Var name taken (stmt -> stmtList)", 0);
                        }
                        //not duplicate can add to scope
                        //System.out.print("Var Added: " + name);
                        Q output = evaluateExpr(stmt.getExpr(), scopeStack);
                        //System.out.println(" Value: " + output.toString());
                        currentScope.put(name, output);
                        //System.out.println(currentScope.containsKey(name));
                       
                        //System.out.println(withinScopeStack(scopeStack, name));
                        if(!scopeStack.contains(currentScope)){
                            scopeStack.add(currentScope);
                        }
                    //handled delcaration, now handle return
                    }else if(stmt.getType() == 1){
                        return stmt;
                    }
                    //pulls off statement we worked with
                    l = l.getStmtList();
                }
                scopeStack.remove(currentScope);
                return s;
            case 7:
                //assignment
                String ident = s.getIdent();
                //types?
                Q value = evaluateExpr(s.getExpr(), scopeStack);
                //System.out.println("ident: " + ident + " value: " + value.toString());
                updateScopeStack(scopeStack, ident, value);
                return s;
            case 8:
                //while
                Stmt run = s;
                while(evaluateCond(s.getCond(), scopeStack)){
                    run = executeStmt(s.getStmt1(), scopeStack);
                    if(run.getType() == 1){
                        return run;
                    }
                }
                return run;
            case 9:
                //Call
                String i = s.getIdent();
                ExprList exprList = s.getExprList();
                //how to construct outside of parser?
                CallExpr call = new CallExpr(i, exprList, null);
                evaluateExpr(call, scopeStack);
                return s;
            case 10:
                //Handle Free
                return s;
            default:
                // Handle default case
        }
        return s;
    }


    public class ConcurrentExpr implements Callable<Q> {
        private Expr expr;
        private List<Map<String, Q>> variableScope;

        public ConcurrentExpr(Expr expr, List<Map<String, Q>> variableScope) {
            this.expr = expr;
            this.variableScope = variableScope;
        }

        public Q call() {
            Q out = null;
            try {
                out = evaluateExpr(expr, variableScope);
            } catch (Exception e) {
                System.out.println("Exception occurred during execution: " + e.getMessage());
                e.printStackTrace();
            }
            return out;
        }
    }



        
    
}