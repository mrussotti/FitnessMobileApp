package interpreter;

import java.io.*;
import java.util.Random;

import parser.ParserWrapper;
import ast.*;
import java.util.HashMap;

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

    private Interpreter(Program astRoot) {
        this.astRoot = astRoot;
        this.random = new Random();
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
        Map<String, String> mainArgs = new Map<String, String>();
        //get command line args from main
        mainArgs.put(astRoot.getFuncDef().getVarDecl().getIdent(), String.valueOf(arg));
        // WILL CALL EXECUTE FUNCTION
        return evaluate(astRoot.getExpr() != null ? astRoot.getExpr() : astRoot.getStmt().getExpr());
    }
    
    //method to execute functions

    //method to evaluate expressions, takes expression, needs maps for scopeing, 
    Object evaluateExpr(Expr expr, Map<String, String> scope, Map<String, String> parScope){
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
            }
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr binaryExpr = (BinaryExpr)expr;
            switch (binaryExpr.getOperator()) {
                case BinaryExpr.PLUS: return (Long)evaluateExpr(binaryExpr.getLeftExpr(), scope, parScope) + (Long)evaluateExpr(binaryExpr.getRightExpr(), scope, parScope);
                case BinaryExpr.MINUS: return (Long)evaluateExpr(binaryExpr.getLeftExpr(), scope, parScope) - (Long)evaluateExpr(binaryExpr.getRightExpr(), scope, parScope);
                case BinaryExpr.TIMES: return (Long)evaluateExpr(binaryExpr.getLeftExpr(), scope, parScope) * (Long)evaluateExpr(binaryExpr.getRightExpr(), scope, parScope); //multiplication for proj1
                default: throw new RuntimeException("Unhandled operator");
            }
        } else {
            throw new RuntimeException("Unhandled Expr type");
        }
    }

    //method to handle condition evaluation
    // calls evaluate expression, passing forward scopeVars
    public static boolean evaluateCond(Cond cond){
        switch (cond.getOperator()) {
            case 1:
                // Handle less than or equal to
                return Long.parseLong(evaluateExpr(cond.getE1())) <= Long.parseLong(evaluateExpr(cond.getE2()));
            case 2:
                // Handle greater than or equal to
                return Long.parseLong(evaluateExpr(cond.getE1())) >= Long.parseLong(evaluateExpr(cond.getE2()));
            case 3:
                // Handle equals
                return Long.parseLong(evaluateExpr(cond.getE1())) == Long.parseLong(evaluateExpr(cond.getE2()));
            case 4:
                // Handle not equals
                return Long.parseLong(evaluateExpr(cond.getE1())) != Long.parseLong(evaluateExpr(cond.getE2()));
            case 5:
                // Handle less than
                return Long.parseLong(evaluateExpr(cond.getE1())) < Long.parseLong(evaluateExpr(cond.getE2()));
            case 6:
                // Handle greater than
                return Long.parseLong(evaluateExpr(cond.getE1())) > Long.parseLong(evaluateExpr(cond.getE2()));
            case 7:
                // Handle logical AND
                return evaluateCond(cond.getC1()) && evaluateCond(cond.getC2());
            case 8:
                // Handle logical OR
                return evaluateCond(cond.getC1()) || evaluateCond(cond.getC2());
            case 9:
                // Handle logical NOT
                return ! evaluateCond(cond.getC1());
            default:
                // Handle default case
        //Need to handle parentheses? Operator types for this don't seem to work
        }
        return false;
    }

	public static void fatalError(String message, int processReturnCode) {
        System.out.println(message);
        System.exit(processReturnCode);
	}


    //must execute a statement from a list of statements
    Object executeStmtList(StmtList s, Stmt t, long l){
        // when s is not null there are elements left, but when s is null there could still be a statement in t that we need to account for
        if(s != null){
            executeStmt(t, l);
            // recursive call without the element we've executed
            return executeStmtList(s.getStmtList(), s.getStmt(), l);
        } else {
            executeStmt(t, l);
        }
        return 0;
    }

    // handle executing the statement
    Object executeStmt(Stmt s, long l){
        //handle different types of statements
        switch (s.getType()) {
            case 1:
                // Handle return statement
                return evaluate(s.getExpr());
            case 2:
                // Handle equals statement for variable declaration
                // want to store variables in a variable map where they're paired with the function they were created in for scoping
                return s;
            case 3:
                // Handle if statement
                if(evaluateCond(s.getCond())){
                    return executeStmt(s.getStmt1(), l);
                }
                return s
            case 4:
                // Handle else statement
                //how is this different?
                break;
            case 5:
                // Handle print statement
                // simply print s??
                System.out.println(s);
                break;
            case 6:
                // Handle statement block
                // s.getStmt() will be null in this case?
                return executeStmtList(s.getStmtList(), s.getStmt1(), l);
                break;
            default:
                // Handle default case
        }
        return 0;
    }
}
