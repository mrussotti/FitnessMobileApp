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

    Object executeRoot(Program astRoot, long arg) {
        populateFunctionMap(astRoot.getFuncDefList());
        Map<String, Q> mainArgs = new HashMap<String, Q>();
        FuncDef main = funcDefMap.get("main");
        if(main != null){
            mainArgs.put(main.getFormalDeclList().getNeFormalDeclList().getVarDecl().getIdent(), new Q(arg));
            return runFunc(main, mainArgs);
        }else{
            fatalError("no main method found", 0);
        }
        return null;
    }


void populateFunctionMap(FuncDefList list) {
    FuncDefList current = list;
    do {
        FuncDef definition = current.getFuncDef();
        String name = definition.getVarDecl().getIdent();
        boolean isDuplicate = funcDefMap.containsKey(name);
        if (isDuplicate) {
            fatalError("Repeated function names detected: " + name, 5);
        } else {
            funcDefMap.put(name, definition);
        }
        current = current.getNextFuncDef();
    } while (current != null);
}


    
public boolean isInScopeStack(List<Map<String, Q>> stack, String identifier){
    return stack.stream().anyMatch(scope -> scope.containsKey(identifier));
}


    public Q findInScopeStack(List<Map<String, Q>> stack, String identifier) {
    for (Map<String, Q> scope : stack) {
        Q item = scope.getOrDefault(identifier, null);
        if (item != null) {
            return item.inte != null ? new Q(item.inte.value) : (item.heap != Ref.NIL ? new Q(item.heap) : new Q());
        }
    }
    return null;
}



    public void updateScopeStack(List<Map<String, Q>> scopeList, String name, Q q) {
        for (int i = scopeList.size() - 1; i >= 0; i--) {
            if (scopeList.get(i).containsKey(name)) {
                scopeList.get(i).replace(name, q);
            }
        }
    }

    Q runFunc(FuncDef func, Map<String, Q> funcArgs){
        List<Map<String, Q>> scopeList = new ArrayList<Map<String, Q>>();
        Map<String, Q> scope = new HashMap<String, Q>();
        StatementList l = func.getStmtList();
        Statement s;

        scopeList.add(funcArgs);
        scopeList.add(scope);
        while(l != null){


            s = executeStmt(l.getStmt(), scopeList);
            if(s instanceof  DeclarationStatement){
                DeclarationStatement stmt2 = (DeclarationStatement)s;
                String name = stmt2.getName();


                scope.put(name, evaluateExpr(stmt2.getInitExpression(), scopeList));
                if(!scopeList.contains(scope)){
                    scopeList.add(scope);
                }
            }else if(s instanceof ReturnStatement){
                return evaluateExpr(s.getExpr(), scopeList);
            }
            l = l.getStmtList();
        }
        return null;
    }

    Q evaluateExpr(Expr expr, List<Map<String, Q>> scopeList) {
        if (expr instanceof NilExpr) {
            return handleNilExpr();
        } else if (expr instanceof ConstExpr) {
            return handleConstExpr((ConstExpr) expr);
        } else if (expr instanceof IDENT) {
            return handleIdentExpr((IDENT) expr, scopeList);
        } else if (expr instanceof BinaryExpr) {
            return handleBinaryExpr((BinaryExpr) expr, scopeList);
        } else if (expr instanceof CallExpr) {
            return handleCallExpr((CallExpr) expr, scopeList);
        } else if (expr instanceof TypeCast) {
            return handleTypeCastExpr((TypeCast) expr, scopeList);
        } else if (expr instanceof ConcurrentExpression) {
            return handleConcurrentExpression((ConcurrentExpression) expr, scopeList);
        } else {
            throw new RuntimeException("Unhandled Expr type");
        }
    }

    Q handleNilExpr() {
        return new Q(Ref.NIL);
    }

    Q handleConstExpr(ConstExpr expr) {
        return expr.getValue();
    }

    Q handleIdentExpr(IDENT expr, List<Map<String, Q>> scopeList) {
        if (isInScopeStack(scopeList, expr.getIdent())) {
            return findInScopeStack(scopeList, expr.getIdent());
        } else {
            throw new RuntimeException("var doesn't exist: " + expr.getIdent());
        }
    }

    Q handleBinaryExpr(BinaryExpr expr, List<Map<String, Q>> scopeList) {
            BinaryExpr binaryExpr = (BinaryExpr)expr;
            switch (binaryExpr.getOperator()) {
                case BinaryExpr.PLUS: return new Q(evaluateExpr(binaryExpr.getLeftExpr(), scopeList).getINT().value + evaluateExpr(binaryExpr.getRightExpr(), scopeList).getINT().value);
                case BinaryExpr.MINUS: return new Q(evaluateExpr(binaryExpr.getLeftExpr(), scopeList).getINT().value - evaluateExpr(binaryExpr.getRightExpr(), scopeList).getINT().value);
                case BinaryExpr.TIMES: return new Q(evaluateExpr(binaryExpr.getLeftExpr(), scopeList).getINT().value * evaluateExpr(binaryExpr.getRightExpr(), scopeList).getINT().value); //multiplication for proj1
                case BinaryExpr.DOT: return new Q(new Ref(evaluateExpr(binaryExpr.getLeftExpr(), scopeList), evaluateExpr(binaryExpr.getRightExpr(), scopeList)));
                    
                default: throw new RuntimeException("Unhandled operator");
            }
    }

    Q handleCallExpr(CallExpr expr, List<Map<String, Q>> scopeList) {
        String functionIdentifier = expr.getIdent();
        Q ret, val;
        Ref r;

        switch (functionIdentifier) {
            case "randomInt":
                return new Q(ThreadLocalRandom.current().nextLong(
                        evaluateExpr(expr.getExprList().getNeExprList().getExpr(), scopeList).getINT().getValue()));

            case "left":
            case "right":
                ret = evaluateExpr(expr.getExprList().getNeExprList().getExpr(), scopeList);
                return functionIdentifier.equals("left") ? ret.getRef().getLeft() : ret.getRef().getRight();

            case "isAtom":
            case "isNil":
                return processIsAtomOrIsNil(expr, scopeList, functionIdentifier);

            case "setLeft":
            case "setRight":
                ret = evaluateExpr(expr.getExprList().getNeExprList().getExpr(), scopeList);
                val = evaluateExpr(expr.getExprList().getNeExprList().getNeExprList().getExpr(), scopeList);
                if (functionIdentifier.equals("setLeft")) {
                    ret.heap.setLeft(val);
                } else {
                    ret.heap.setRight(val);
                }
                return new Q((long)1);

            case "acq":
            case "rel":
                r = evaluateExpr(expr.getExprList().getNeExprList().getExpr(), scopeList).heap;
                if (functionIdentifier.equals("acq")) {
                    while (!r.tryLock()) {
                        System.out.print("");
                    }
                } else {
                    r.release();
                }
                return new Q((long)1);

            default:
                return processFuncDef(expr, scopeList, functionIdentifier);
        }
    }

    private Q processIsAtomOrIsNil(CallExpr expr, List<Map<String, Q>> scopeList, String functionIdentifier) {
        Q ret = evaluateExpr(expr.getExprList().getNeExprList().getExpr(), scopeList);
        if (ret.getINT() == null && ret.getRef() == null) {
            ret.heap = Ref.NIL;
        }
        boolean isAtom = functionIdentifier.equals("isAtom");
        boolean condition = (isAtom && (ret.getRef() == null && ret.getINT() != null)) ||
                            (!isAtom && (ret.getINT() != null || ret.getRef() == null || !ret.getRef().isNil()));
        return new Q(condition ? (long)1 : (long)0);
    }

private Q processFuncDef(CallExpr expr, List<Map<String, Q>> scopeList, String functionIdentifier) {
    FuncDef funcDef = funcDefMap.get(functionIdentifier);
    validateParameters(funcDef, expr);
    Map<String, Q> args = new HashMap<>();

    NeFormalDeclList declList = funcDef.getFormalDeclList().getNeFormalDeclList();
    NeExprList exprList = expr.getExprList().getNeExprList();

    while (declList != null && exprList != null) {
        VarDecl varDecl = declList.getVarDecl();
        Expr exprItem = exprList.getExpr();
        args.put(varDecl.getIdent(), evaluateExpr(exprItem, scopeList));

        declList = declList.getNeFormalDeclList();
        exprList = exprList.getNeExprList();
    }

    return runFunc(funcDef, args);
}


    private void validateParameters(FuncDef funcDef, CallExpr expr) {
        FormalDeclList formalDeclList = funcDef.getFormalDeclList();
        ExprList exprList = expr.getExprList();
        if ((formalDeclList != null && exprList == null) || (formalDeclList == null && exprList != null) ||
            (formalDeclList != null && exprList != null && formalDeclList.getNeFormalDeclList().length() != exprList.getNeExprList().length())) {
            fatalError("Incorrect number of parameters passed to method: " + funcDef.getVarDecl().getIdent(), 0);
        }
    }

    

    Q handleTypeCastExpr(TypeCast expr, List<Map<String, Q>> scopeList) {
        return evaluateExpr(expr.getCastExpr(), scopeList);
    }

    Q handleConcurrentExpression(ConcurrentExpression expr, List<Map<String, Q>> scopeList) {
         ConcurrentExpression concurrentExpression = (ConcurrentExpression) expr;
            BinaryExpr binaryExpr = concurrentExpression.getBinaryExpr();

            ExecutorService executor = Executors.newFixedThreadPool(4);

            Future<Q> f1 = executor.submit(new ConcurrentExpr(binaryExpr.getLeftExpr(), scopeList));
            Future<Q> f2 = executor.submit(new ConcurrentExpr(binaryExpr.getRightExpr(), scopeList));

            Q r1 = null;
            Q r2 = null;
            try {
                r1 = f1.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } 

            try {
                r2 = f2.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } 

            executor.shutdown();

            switch (binaryExpr.getOperator()) {
                case BinaryExpr.PLUS: return new Q(r1.getINT().getValue() + r2.getINT().getValue());
                case BinaryExpr.MINUS: return new Q(r1.getINT().getValue() - r2.getINT().getValue());
                case BinaryExpr.TIMES: return new Q(r1.getINT().getValue() * r2.getINT().getValue()); //multiplication for proj1
                case BinaryExpr.DOT: return new Q(new Ref(r1, r2));
                default: throw new RuntimeException("Unhandled operator");
            }

        }



    boolean evaluateCond(Condition condition, List<Map<String, Q>> scopeList){
    if (condition instanceof BinaryComparison){
                BinaryComparison bcomp = (BinaryComparison) condition;
                Long left = evaluateExpr(bcomp.getLeftExpr(), scopeList).getINT().getValue();
                Long right = evaluateExpr(bcomp.getRightExpr(), scopeList).getINT().getValue();
                System.out.println("Evaluating condition: BinaryComparison"+left +" "+ right );

                switch (bcomp.getOperator()){
                    case BinaryComparison.LEQ: return left <= right;
                    case BinaryComparison.GEQ: return left >= right;
                    case BinaryComparison.EQ: return left == right;
                    case BinaryComparison.NEQ: return left != right;
                    case BinaryComparison.LT: return left < right;
                    case BinaryComparison.GT: return left > right;
                    default: throw new AssertionError("you forgot a relational operator ");
                }
            } else if (condition instanceof BinaryLogicalOperations){
                System.out.println("Evaluating condition: BinaryLogicalOperations" );
                BinaryLogicalOperations boolcond = (BinaryLogicalOperations) condition;
                switch(boolcond.getOperator()){
                    case BinaryLogicalOperations.AND:
                        return evaluateCond(boolcond.getLeftCond(), scopeList) && evaluateCond(boolcond.getRightCond(), scopeList);
                    case BinaryLogicalOperations.OR:
                        return evaluateCond(boolcond.getLeftCond(), scopeList) || evaluateCond(boolcond.getRightCond(), scopeList);
                    default: throw new AssertionError ("You forgot a boolean operator");
                }
            }else if (condition instanceof UnaryLogicalOperations){
                System.out.println("Evaluating condition: UnaryLogicalOperations" );
                return ! evaluateCond(((UnaryLogicalOperations) condition).getCond(), scopeList);
            }else {
                throw new AssertionError("you forgot a condition ");
            }
        //return false;
    }

	public static void fatalError(String message, int processReturnCode) {
        System.out.println(message);
        System.exit(processReturnCode);
	}


    // handle executing the statement
    Statement executeStmt(Statement stmt, List<Map<String, Q>> scopeList){
        System.out.println("Executing statement: " + stmt);
        if (stmt instanceof ReturnStatement){
            // Object returnValue = evaluate(((ReturnStatement) stmt).getExpr(), scopeList);
            // System.out.println("Returning: " + returnValue);
            return stmt;
        }else if (stmt instanceof IfStatement){
            System.out.println("If Statement: ");
            IfStatement ifstmt = (IfStatement) stmt;
            //idk
            if(evaluateCond(ifstmt.getCond(), scopeList)){
                    return executeStmt(ifstmt.getBody(), scopeList);
                }
            return stmt;
        }else if (stmt instanceof IfElseStatement){
            System.out.println("If Else Statement ");
            IfElseStatement ifstmt = (IfElseStatement) stmt;
            //check 
            if(evaluateCond(ifstmt.getCond(), scopeList)){
                    return executeStmt(ifstmt.getThenBody(), scopeList);
                }else{
                    return executeStmt(ifstmt.getElseBody(), scopeList);
                }
        }else if (stmt instanceof BlockStatement){
                   BlockStatement blkstmt = (BlockStatement) stmt;                
                Map<String, Q> currentScope = new HashMap<String, Q>();
                scopeList.add(currentScope);
                Statement s;
                StatementList l = blkstmt.getStmtList();
                while(l != null){
                    s = executeStmt(l.getStmt(), scopeList);
                    if(s instanceof  DeclarationStatement){
                        DeclarationStatement stmt2 = (DeclarationStatement)s;
                        String name = stmt2.getName();


                        Q output = evaluateExpr(stmt2.getInitExpression(), scopeList);
                        currentScope.put(name, output);

                        if(!scopeList.contains(currentScope)){
                            scopeList.add(currentScope);
                        }
                    }else if(stmt instanceof ReturnStatement){
                        return stmt;
                    }
                    l = l.getStmtList();
                }
                scopeList.remove(currentScope);
                return stmt;

        }else if (stmt instanceof DeclarationStatement){
            System.out.println("DeclarationStatement ");
            return stmt;
        }else if (stmt instanceof PrintStatement){

            System.out.println(evaluateExpr(stmt.getExpr(), scopeList));
            return stmt;
        }else if (stmt instanceof FreeStatement){
            System.out.println("Free Statement, get fucked");
            return stmt;
        }else if (stmt instanceof AssignmentStatement){
                AssignmentStatement as = (AssignmentStatement) stmt;
                String ident = as.getIdentity();
                Q value = evaluateExpr(as.getExpr(), scopeList);
                updateScopeStack(scopeList, ident, value);
                return stmt;
        }else if (stmt instanceof WhileStatement){
            WhileStatement whileStatement = (WhileStatement)stmt;
             Statement run = stmt;
                while(evaluateCond(whileStatement.getCond(), scopeList)){
                    run = executeStmt(whileStatement.getBody(), scopeList);
                    if(run instanceof ReturnStatement){
                        return run;
                    }
                }
                return run;
        }else if (stmt instanceof CallStatement){
                CallStatement cs = (CallStatement) stmt;
                String i = cs.getIdentity();
                ExprList exprList = cs.getExprList();
                CallExpr call = new CallExpr(i, exprList, null);
                evaluateExpr(call, scopeList);
                return stmt;
        }else {
            throw new AssertionError("You forgot statement type ");
        }

    }


    public class ConcurrentExpr implements Callable<Q> {
        private Expr expr;
        private List<Map<String, Q>> scopeList;

        public ConcurrentExpr(Expr expr, List<Map<String, Q>> scopeList) {
            this.expr = expr;
            this.scopeList = scopeList;
        }

        public Q call() {
            return evaluateExpr(expr, scopeList);
        }
    }



        
    
}