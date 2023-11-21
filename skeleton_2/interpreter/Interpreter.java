package interpreter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.List;

import parser.ParserWrapper;
import ast.*;

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
            // gets last num in cmdline args
            quandaryArg = Long.valueOf(args[i + 1]); // TODO: change to more than one arg & different types?
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
        // astRoot.println(System.out);
        interpreter = new Interpreter(astRoot, gcType, heapBytes);
        // removed initMemManager line and changed constructor to fit
        String returnValueAsString = interpreter.executeRoot(astRoot, quandaryArg).toString();
        System.out.println("Interpreter returned " + returnValueAsString);
    }

    final Program astRoot;
    final Random random;
    // mem stuff
    final RawMemory mem;
    final Long heapSize;
    // note; the only thing you can put in heap mem are Refs
    long numOfAllocatedObjects;

    private Interpreter(Program astRoot, String gcType, Long heapBytes) {
        this.astRoot = astRoot;
        // initialize raw mem and start mem manager
        this.heapSize = heapBytes;
        this.mem = new RawMemory(8, heapBytes);
        this.initMemoryManager(gcType, heapBytes);
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

    Object executeRoot(Program astRoot, long arg) {
        Data progData = executeProgram(astRoot, arg);
        if (progData.rawType == Type.REF) {
            return readObjectAtAsString(progData.value);
        } else {
            return progData.value;
        }
    }

    // Loop through funcDefList, track funcDefs and find and execute main() with
    // given cmdLineArgs
    Data executeProgram(Program prgm, long arg) {
        // TODO: add support for mulitple args
        Map<String, Data> cmdLineArgs = new HashMap<String, Data>();
        // keep track of functions in program
        Map<String, FuncDef> funcList = new HashMap<String, FuncDef>();
        // keep track of main()
        FuncDef main = null;

        FuncDefList remainingFuncDefs = prgm.getFuncDefList();
        FuncDef currFunc;
        String funcName;
        // find main() in funcDefList and populate funcList
        while (remainingFuncDefs != null) {
            // get next funcDef
            currFunc = remainingFuncDefs.getFuncDef();
            funcName = currFunc.getVarDecl().getIdentifier();
            // save if main
            if (funcName.equals("main")) {
                funcList.put(funcName, currFunc);
                main = currFunc;
            } else if (!funcList.containsKey(funcName)) { // if not in funcList; add
                funcList.put(funcName, currFunc);
            } else { // if name is not main and is in already funcList
                fatalError("Oops! Something went wrong with funDefList.", 453);
            }
            remainingFuncDefs = remainingFuncDefs.getFuncDefList();
        }

        // call main() to begin program execution
        // return main's return value
        if (main != null) {
            // TODO: add support for multiple args
            // Read main's formalDeclList to set arg var names
            cmdLineArgs.put(main.getFormalDeclList().getVarDecl().getIdentifier(),
                    new Data(Type.INT, false, arg));
            boolean isMainMutable = main.getVarDecl().isMutable();
            return executeFunction(main, cmdLineArgs, funcList, isMainMutable);
        } else {
            fatalError("Oops! You forgot main() dumdum!", 1);
        }
        return null;
    }

    // Proj_4: MAJOR CHANGE
    // params/scopeVars/etc are now (String,Stmt) maps
    // so variable values are not known until evaluateExpr (lazy evaluation)
    // But memory is initialized on DECLARATION (Refs are 24 bytes)? // TODO: not
    // sure how to handle this
    // assignment to a Ref/Q will change it's "pointer" value, not the object
    // changes to object in heap must be made with built in functions
    // built in functions are mutable functions
    // Only mutable functions can call mutable functions
    // So everything must know in what function it is in

    // Object structure is as follows
    // addr+0 = left field; addr+8 = right field; addr+16 = extra

    // When calling this function read the funcDef's formalDeclList first to
    // assemble an args map to pass
    // returns the evaluated expr in a return stmt
    Data executeFunction(FuncDef func, Map<String, Data> params, Map<String, FuncDef> funcList,
            boolean isInMutableFunc) {
        Map<String, Data> inScopeVars = new HashMap<String, Data>();

        // TODO: return type?

        // begin evaluation of stmtlist; the function body
        StmtList remainingLines = func.getStmtList();
        Stmt currentLine;
        // only procede if function body is not empty
        while (remainingLines != null) {
            // get current stmt line
            currentLine = evaluateStmt(remainingLines.getStmt(), inScopeVars, params, funcList, isInMutableFunc);
            // get current line type
            int stmtType = currentLine.getTypeOfStmt();
            // if stmt is a var assignment
            if (stmtType == Stmt.DECL || stmtType == Stmt.ASSIGN) {
                if (stmtType == Stmt.DECL) {
                    VarDecl varDecl = currentLine.getVarDecl();
                    String varName = currentLine.getVarDecl().getIdentifier();
                    // if var is already in inScopeVars; throw error
                    if (inScopeVars.containsKey(varName)) {
                        fatalError("Duplicate variable", 123);
                    } else if (params.containsKey(varName)) {
                        fatalError("Duplicate variable; named same as an param", 123);
                    } else { // else add to inScopeVars
                        Data rawValue = evaluateExpr(currentLine.getExpr(), inScopeVars, params, funcList,
                                isInMutableFunc);
                        inScopeVars.put(varName,
                                new Data(varDecl.getRawType(), varDecl.isMutable(), rawValue.value));
                    }
                } else if (stmtType == Stmt.ASSIGN) {
                    String varName = currentLine.getVar();
                    Expr newVarExpr = currentLine.getExpr();
                    Data rawNewVarData = evaluateExpr(newVarExpr, inScopeVars, params, funcList,
                            isInMutableFunc);
                    if (inScopeVars.containsKey(varName)) {
                        Data oldVarData = inScopeVars.get(varName);
                        if (oldVarData.isMutable) {

                            inScopeVars.replace(varName,
                                    new Data(oldVarData.rawType, oldVarData.isMutable, rawNewVarData.value));
                        } else {
                            fatalError("Gosh! You can't assign to an immutable variable you dummy!", 123);
                        }
                    } else if (params.containsKey(varName)) {
                        Data oldVarData = params.get(varName);
                        if (oldVarData.isMutable) {

                            params.replace(varName,
                                    new Data(oldVarData.rawType, oldVarData.isMutable, rawNewVarData.value));
                        } else {
                            fatalError("Gosh! You can't assign to an immutable variable you dummy!", 123);
                        }
                    } else {
                        fatalError(
                                "Oops! You tried to assign to a variable that hasn't been declared yet you dummy!",
                                123);
                    }
                }

            } else if (stmtType == Stmt.RETURN) {
                // TODO: add error handling for when return does not match return type?
                return evaluateExpr(currentLine.getExpr(), inScopeVars, params, funcList, isInMutableFunc);
            }
            remainingLines = remainingLines.getStmtList();
        }
        // return null if function body was empty / reached empty without return
        // TODO: add error handling for when return does not match return type?
        return null;
    }

    // evaluate stmt contents then
    // Returns passed stmt unmodified
    Stmt evaluateStmt(Stmt stmt, Map<String, Data> scopeVars, Map<String, Data> parentScopeVars,
            Map<String, FuncDef> funcList, boolean isInMutableFunc) {
        // get stmt type
        int stmtType = stmt.getTypeOfStmt();
        switch (stmtType) {
            case Stmt.DECL: // handle varDecl
                // if it is an declaration
                // return to imdiate parent function so it can update scope variables
                return stmt;
            case Stmt.ASSIGN: // handle assignment
                // if it is an assignment
                // return to imdiate parent function so it can update scope variables
                return stmt;
            case Stmt.IF: // If stmt
                if (evaluateCond(stmt.getCond(), scopeVars, parentScopeVars, funcList, isInMutableFunc)) {
                    return evaluateStmt(stmt.getStmt1(), scopeVars, parentScopeVars, funcList, isInMutableFunc);
                } // else do nothing and continue
                return stmt;
            case Stmt.IFELSE: // if else stmt
                if (evaluateCond(stmt.getCond(), scopeVars, parentScopeVars, funcList, isInMutableFunc)) {
                    return evaluateStmt(stmt.getStmt1(), scopeVars, parentScopeVars, funcList, isInMutableFunc);
                } else { // else evaluate other statment
                    return evaluateStmt(stmt.getStmt2(), scopeVars, parentScopeVars, funcList, isInMutableFunc);
                }
            case Stmt.PRINT: // print
                // evaluate expr and print result
                Data d = evaluateExpr(stmt.getExpr(), scopeVars, parentScopeVars, funcList, isInMutableFunc);
                System.out.println(d.rawType == Type.REF ? readObjectAtAsString(d.value) : d.value);
                return stmt;
            case Stmt.RETURN: // return
                // return to parent function to return
                return stmt;
            case Stmt.STMTGRP: // stmt group (if/else body) ; note stmt bodys have their own scope
                // create merged "parent" scope
                Map<String, Data> newParentScopeVars = new HashMap<String, Data>();
                // squish the scope vars
                newParentScopeVars.putAll(parentScopeVars);
                newParentScopeVars.putAll(scopeVars); // merge scopes
                // start stmt body scope; any assignments made here stay here
                Map<String, Data> subScopeVars = new HashMap<String, Data>();
                // loop through stmt body; evaluate each stmt
                StmtList nestedBody = stmt.getStmtList();
                Stmt currLine;
                while (nestedBody != null) {

                    currLine = evaluateStmt(nestedBody.getStmt(), subScopeVars, newParentScopeVars, funcList,
                            isInMutableFunc);
                    // unsquish the scope vars
                    for (String k : newParentScopeVars.keySet()) {
                        if (scopeVars.containsKey(k)) {
                            scopeVars.replace(k, newParentScopeVars.get(k));
                        }
                        if (parentScopeVars.containsKey(k)) {
                            parentScopeVars.replace(k, newParentScopeVars.get(k));
                        }
                    }
                    if (currLine.getTypeOfStmt() == Stmt.DECL || currLine.getTypeOfStmt() == Stmt.ASSIGN) {
                        if (currLine.getTypeOfStmt() == Stmt.DECL) {
                            VarDecl varDecl = currLine.getVarDecl();
                            String varName = currLine.getVarDecl().getIdentifier();
                            int varType = currLine.getVarDecl().getRawType(); // TODO: type checking?
                            // if var is already in inScopeVars; throw error
                            if (subScopeVars.containsKey(varName)) {
                                fatalError("Duplicate variable", 123);
                            } else if (newParentScopeVars.containsKey(varName)) {
                                fatalError("Duplicate variable; named same as an param", 123);
                            } else { // else add to inScopeVars
                                Data rawValue = evaluateExpr(currLine.getExpr(), subScopeVars, newParentScopeVars,
                                        funcList,
                                        isInMutableFunc);
                                subScopeVars.put(varName,
                                        new Data(varDecl.getRawType(), varDecl.isMutable(),
                                                rawValue.value));
                            }
                        } else if (currLine.getTypeOfStmt() == Stmt.ASSIGN) {

                            String varName = currLine.getVar();
                            Expr newVarExpr = currLine.getExpr();
                            Data rawNewVarData = evaluateExpr(newVarExpr, subScopeVars, newParentScopeVars, funcList,
                                    isInMutableFunc);
                            if (subScopeVars.containsKey(varName)) {
                                Data oldVarData = subScopeVars.get(varName);
                                if (oldVarData.isMutable) {
                                    subScopeVars.replace(varName,
                                            new Data(oldVarData.rawType, oldVarData.isMutable, rawNewVarData.value));
                                } else {
                                    fatalError("Gosh! You can't assign to an immutable variable you dummy!", 123);
                                }
                            } else if (newParentScopeVars.containsKey(varName)) {
                                // if an assignment is made to a parent scope, the changes must propagate up
                                if (scopeVars.containsKey(varName)) {
                                    Data oldVarData = newParentScopeVars.get(varName);
                                    if (oldVarData.isMutable) {
                                        newParentScopeVars.replace(varName,
                                                new Data(oldVarData.rawType, oldVarData.isMutable,
                                                        rawNewVarData.value));
                                        scopeVars.replace(varName,
                                                new Data(oldVarData.rawType, oldVarData.isMutable,
                                                        rawNewVarData.value));
                                    } else {
                                        fatalError("Gosh! You can't assign to an immutable variable you dummy!", 123);
                                    }
                                } else if (parentScopeVars.containsKey(varName)) {

                                    Data oldVarData = newParentScopeVars.get(varName);
                                    if (oldVarData.isMutable) {
                                        newParentScopeVars.replace(varName,
                                                new Data(oldVarData.rawType, oldVarData.isMutable,
                                                        rawNewVarData.value));
                                        parentScopeVars.replace(varName,
                                                new Data(oldVarData.rawType, oldVarData.isMutable,
                                                        rawNewVarData.value));
                                    } else {
                                        fatalError("Gosh! You can't assign to an immutable variable you dummy!", 123);
                                    }
                                }
                            } else {
                                fatalError(
                                        "Oops! You tried to assign to a variable that hasn't been declared yet you dummy!",
                                        123);
                            }
                        }

                    } else if (currLine.getTypeOfStmt() == Stmt.RETURN) {
                        // return could be returning a variable only in subScope
                        // package as a stmt before returning and let parent eval
                        Stmt retVar = new Stmt(Stmt.RETURN, currLine.getExpr(), null);
                        return retVar;
                    }
                    nestedBody = nestedBody.getStmtList();
                }
                return stmt;
            case Stmt.CALL:
                CallExpr wrappedStmtCall = new CallExpr(stmt.getVar(), stmt.getExprList(), null);
                evaluateExpr(wrappedStmtCall, scopeVars, parentScopeVars, funcList, isInMutableFunc);
                return stmt;
            case Stmt.WHILE:
                while (evaluateCond(stmt.getCond(), scopeVars, parentScopeVars, funcList, isInMutableFunc)) {
                    Stmt body = evaluateStmt(stmt.getStmt1(), scopeVars, parentScopeVars, funcList, isInMutableFunc);
                    if (body.getTypeOfStmt() == Stmt.RETURN) {
                        return body;
                    }
                }
                return stmt;
            default:
                fatalError("Oops! Something went wrong when processing a statment. :(", 404);
                return null;
        }
    }

    Data evaluateExpr(Expr expr, Map<String, Data> scopeVars, Map<String, Data> parentScopeVars,
            Map<String, FuncDef> funcList, boolean isInMutableFunc) {
        // if expr is an IdentExpr return the value associated with it
        if (expr == null) {
            return new Data(Type.NIL, false, 0);
        } else if (expr instanceof IdentExpr) {

            // TODO: what to do when we get a ref or q

            // check (this) scope before checking parent scope

            if (scopeVars.containsKey(((IdentExpr) expr).getIndetifier())) {
                Data varData = scopeVars.get(((IdentExpr) expr).getIndetifier());
                return varData;
            } else if (parentScopeVars.containsKey(((IdentExpr) expr).getIndetifier())) {
                Data varData = parentScopeVars.get(((IdentExpr) expr).getIndetifier());
                return varData;
            } else {
                throw new RuntimeException("Attempted to read uninitialized variable");
            }
        } else if (expr instanceof TypeCastExpr) {
            TypeCastExpr e = (TypeCastExpr) expr;
            Data rawOldData = evaluateExpr(e.getExpr(), scopeVars, parentScopeVars, funcList, isInMutableFunc);
            return new Data(e.getRawType(), rawOldData.isMutable,
                    rawOldData.value);

        } else if (expr instanceof ConstExpr) {
            return new Data(Type.INT, false, (Long) ((ConstExpr) expr).getValue());
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr binaryExpr = (BinaryExpr) expr;
            switch (binaryExpr.getOperator()) {
                case BinaryExpr.PLUS:
                    return new Data(Type.INT, false,
                            (evaluateExpr(binaryExpr.getLeftExpr(), scopeVars, parentScopeVars, funcList,
                                    isInMutableFunc).value
                                    + evaluateExpr(binaryExpr.getRightExpr(), scopeVars, parentScopeVars,
                                            funcList,
                                            isInMutableFunc).value));
                case BinaryExpr.MINUS:
                    return new Data(Type.INT, false,
                            (evaluateExpr(binaryExpr.getLeftExpr(), scopeVars, parentScopeVars, funcList,
                                    isInMutableFunc).value
                                    - evaluateExpr(binaryExpr.getRightExpr(), scopeVars, parentScopeVars,
                                            funcList,
                                            isInMutableFunc).value));
                case BinaryExpr.MULTIPLY:
                    return new Data(Type.INT, false,
                            (evaluateExpr(binaryExpr.getLeftExpr(), scopeVars, parentScopeVars, funcList,
                                    isInMutableFunc).value
                                    * evaluateExpr(binaryExpr.getRightExpr(), scopeVars, parentScopeVars,
                                            funcList,
                                            isInMutableFunc).value));
                case BinaryExpr.DOT:
                    // note: all expressions eventually eval to either a nil, var, or int
                    // note: each dot is a new allocation of 24bytes
                    // TODO: what do we return here? addr? to what?
                    Data left = evaluateExpr(binaryExpr.getLeftExpr(), scopeVars, parentScopeVars, funcList,
                            isInMutableFunc);

                    Data right = evaluateExpr(binaryExpr.getRightExpr(), scopeVars, parentScopeVars, funcList,
                            isInMutableFunc);

                    long addr = getNextAvailableAddr();
                    initObjectAt(addr);
                    setObjectAtTo(addr, left, right);
                    return new Data(Type.REF, false, addr);
                default:
                    throw new RuntimeException("Unhandled operator");
            }
        } else if (expr instanceof CallExpr) {
            CallExpr callExpr = (CallExpr) expr; // cast once to save time
            Long ref; // addr var
            Long oldVal;
            Long newVal;
            Data callParamData = evaluateExpr(callExpr.getExprList().getExpr(), scopeVars, parentScopeVars, funcList,
                    isInMutableFunc);
            Data callParamData2;
            // check if calling a built in function
            switch (callExpr.getIndetifier()) {
                case "randomInt":
                    return new Data(Type.INT, false, ThreadLocalRandom.current().nextLong(
                            callParamData.value));
                case "left":
                    if (getTypeOfLeftField(callParamData.value) == Type.NIL) {
                        return new Data(Type.NIL, false, 0);
                    }
                    return new Data(getTypeOfLeftField(callParamData.value), callParamData.isMutable,
                            getObjectAtLeftField(callParamData.value));

                case "right":
                    if (getTypeOfRightField(callParamData.value) == Type.NIL) {
                        return new Data(Type.NIL, false, 0);
                    }
                    return new Data(getTypeOfRightField(callParamData.value), callParamData.isMutable,
                            getObjectAtRightField(callParamData.value));
                case "isAtom":
                    return new Data(Type.INT, false,
                            callParamData.rawType == Type.REF && callParamData.value != 0 ? 0 : 1);
                case "isNil":
                    return new Data(Type.INT, false, callParamData.rawType == Type.INT
                            || (callParamData.rawType == Type.REF && callParamData.value == 0)
                            || callParamData.rawType == Type.NIL ? 1 : 0);
                case "setLeft":
                    callParamData2 = evaluateExpr(callExpr.getExprList().getExprList().getExpr(), scopeVars,
                            parentScopeVars, funcList, isInMutableFunc);
                    if (isInMutableFunc) {
                        long oldRightValue = getObjectAtRightField(callParamData.value);
                        setObjectAtTo(callParamData.value,
                                new Data(callParamData2.rawType, callParamData2.isMutable, callParamData2.value),
                                new Data(getTypeOfRightField(callParamData.value), callParamData.isMutable,
                                        oldRightValue));
                        return null;
                    } else {
                        fatalError(
                                "Dont you know that only mutable functions can call mutable functions? You silly goose. ;)",
                                123);
                    }
                    break;
                case "setRight":
                    callParamData2 = evaluateExpr(callExpr.getExprList().getExprList().getExpr(), scopeVars,
                            parentScopeVars, funcList, isInMutableFunc);
                    if (isInMutableFunc) {
                        long oldLeftValue = getObjectAtLeftField(callParamData.value);
                        setObjectAtTo(callParamData.value,
                                new Data(getTypeOfLeftField(callParamData.value), callParamData.isMutable,
                                        oldLeftValue),
                                new Data(callParamData2.rawType, callParamData2.isMutable, callParamData2.value));
                        return null;
                    } else {
                        fatalError(
                                "Dont you know that only mutable functions can call mutable functions? You silly goose. ;)",
                                123);
                    }
                    break;

                default:
                    break;
            }

            // Heres how this works ---
            // get the funcDef and exprList from the callExpr
            // match the value of an expr in the exprList to a varDecl in the formalDeclList
            // in the funcDef
            FuncDef funcDef = funcList.get(callExpr.getIndetifier());
            ExprList eList = callExpr.getExprList();
            FormalDeclList varList = funcDef.getFormalDeclList();
            // num of params; make sure length of exprList matches length of formalDeclList
            if ((eList == null && varList != null) || (eList != null && varList == null)) {
                fatalError("Oops! A function call was made with the incorrect num of params.", 123);
            } else if ((eList != null && varList != null) && (eList.length() != varList.length())) {
                fatalError("Oops! A function call was made with the incorrect num of params.", 123);
            }
            // get parameters
            Map<String, Data> params = new HashMap<String, Data>();
            Expr currE;
            VarDecl currVar;
            while (eList != null && varList != null) {
                currE = eList.getExpr();
                currVar = varList.getVarDecl();
                Data rawVarData = evaluateExpr(currE, scopeVars, parentScopeVars, funcList, isInMutableFunc);
                // TODO: type checking here can make sure order of params is correct
                params.put(currVar.getIdentifier(),
                        new Data(currVar.getRawType(), currVar.isMutable(), rawVarData.value));
                eList = eList.getExprList();
                varList = varList.getFormalDeclList();
            }
            return executeFunction(funcDef, params, funcList, isInMutableFunc);
        } else {
            throw new RuntimeException("Unhandled Expr type");
        }
    }

    // evaluate a condition
    // return whether the condition is true
    Boolean evaluateCond(Cond c, Map<String, Data> scopeVars, Map<String, Data> parentScopeVars,
            Map<String, FuncDef> funcList, boolean isInMutableFunc) {
        Long e1 = null;
        Long e2 = null;
        if (c.getExpr1() != null && c.getExpr2() != null) {
            e1 = evaluateExpr(c.getExpr1(), scopeVars, parentScopeVars, funcList, isInMutableFunc).value;
            e2 = evaluateExpr(c.getExpr2(), scopeVars, parentScopeVars, funcList, isInMutableFunc).value;
        }
        switch (c.getTypeOfCond()) {
            case Cond.MOREEQUAL:
                return ((long) e1) >= ((long) e2);
            case Cond.LESSEQUAL:
                return ((long) e1) <= ((long) e2);
            case Cond.EQUALITY:
                return ((long) e1) == ((long) e2);
            case Cond.NOTEQUAL:
                return ((long) e1) != ((long) e2);
            case Cond.LESS:
                return ((long) e1) < ((long) e2);
            case Cond.MORE:
                return ((long) e1) > ((long) e2);
            case Cond.AND:
                return (evaluateCond(c.getCond1(), scopeVars, parentScopeVars, funcList, isInMutableFunc)
                        && evaluateCond(c.getCond2(), scopeVars, parentScopeVars, funcList, isInMutableFunc));
            case Cond.OR:
                return (evaluateCond(c.getCond1(), scopeVars, parentScopeVars, funcList, isInMutableFunc)
                        || evaluateCond(c.getCond2(), scopeVars, parentScopeVars, funcList, isInMutableFunc));
            case Cond.NOT:
                return !evaluateCond(c.getCond1(), scopeVars, parentScopeVars, funcList, isInMutableFunc);
            default:
                fatalError("Oops! Something went wrong when evaluating a condition. :(", 0451);
                return null;
        }
    }

    public static void fatalError(String message, int processReturnCode) {
        System.out.println(message);
        System.exit(processReturnCode);
    }

    long getNextAvailableAddr() {
        long addr = this.mem.getStartAddr() + (24 * numOfAllocatedObjects);
        if (addr >= this.mem.getEndAddr()) {
            fatalError("Oh jeez. Looks like you ran out of memory. That's rough.", EXIT_DATA_RACE_ERROR);
        }
        return addr;
    }

    void initObjectAt(long addr) {
        numOfAllocatedObjects += 1;
    }

    int getObjectTypeData(long addr) {
        return (int) this.mem.load(addr + 16);
    }

    String readObjectAtAsString(long addr) {
        if (addr == 0) {
            // nil ref
            return "nil";
        }
        int typeDataValue = (int) this.mem.load(addr + 16);
        switch (typeDataValue) {
            case 11:
                return "(" + this.mem.load(addr) + " . " + this.mem.load(addr + 8) + ")";
            case 12:
                return "(" + this.mem.load(addr) + " . " + readObjectAtAsString(this.mem.load(addr + 8))
                        + ")";
            case 13:
                return "(" + this.mem.load(addr) + " . " + this.mem.load(addr + 8) + ")";
            case 14:
                return "(" + this.mem.load(addr) + " . " + "nil" + ")";
            case 21:
                return "(" + readObjectAtAsString(this.mem.load(addr)) + " . " + this.mem.load(addr + 8)
                        + ")";
            case 22:
                return "(" + readObjectAtAsString(this.mem.load(addr)) + " . "
                        + readObjectAtAsString(this.mem.load(addr + 8)) + ")";
            case 23:
                return "(" + readObjectAtAsString(this.mem.load(addr)) + " . " + this.mem.load(addr + 8)
                        + ")";
            case 24:
            case 31:
                return "(" + this.mem.load(addr) + " . " + this.mem.load(addr + 8) + ")";
            case 32:
                return "(" + this.mem.load(addr) + " . " + readObjectAtAsString(this.mem.load(addr + 8))
                        + ")";
            case 33:
                return "(" + this.mem.load(addr) + " . " + this.mem.load(addr + 8) + ")";
            case 34:
                return "(" + this.mem.load(addr) + " . " + "nil" + ")";
            case 41:
                return "(" + "nil" + " . " + this.mem.load(addr + 8) + ")";
            case 42:
                return "(" + "nil" + " . " + readObjectAtAsString(this.mem.load(addr + 8)) + ")";
            case 43:
                return "(" + "nil" + " . " + this.mem.load(addr + 8) + ")";
            case 44:
                return "(" + "nil" + " . " + "nil" + ")";
            default:
                System.out.println(addr + 16);
                System.out.println(this.mem.load(addr + 16));
                fatalError("lord help me", typeDataValue);
                return null;
        }
    }

    long getObjectAtLeftField(long addr) {
        return this.mem.load(addr);
    }

    long getObjectAtRightField(long addr) {
        return this.mem.load(addr + 8);
    }

    long getAddrOfLeftField(long addr) {
        return addr;
    }

    long getAddrOfRightField(long addr) {
        return addr + 8;
    }

    int getTypeOfRightField(long addr) {
        return (int) this.mem.load(addr + 16) % 10;
    }

    int getTypeOfLeftField(long addr) {
        return (int) this.mem.load(addr + 16) / 10;
    }

    void setObjectAtTo(long addr, Data left, Data right) {
        long leftValueField = addr;
        long rightValueField = addr + 8;
        long typeDataField = addr + 16;

        int typeData = left.rawType * 10 + right.rawType;
        long[] data = { left.value, right.value };

        this.mem.store(typeDataField, typeData);
        switch (typeData) {
            case 11:
                this.mem.store(leftValueField, data[0]);
                this.mem.store(rightValueField, data[1]);
                break;
            case 12:
                this.mem.store(leftValueField, data[0]);
                this.mem.store(rightValueField, data[1]);
                break;
            case 13:
                this.mem.store(leftValueField, data[0]);
                this.mem.store(rightValueField, data[1]);
                break;
            case 14:
                this.mem.store(leftValueField, data[0]);
                this.mem.store(rightValueField, 0);
                break;
            case 21:
                this.mem.store(leftValueField, data[0]);
                this.mem.store(rightValueField, data[1]);
                break;
            case 22:
                this.mem.store(leftValueField, data[0]);
                this.mem.store(rightValueField, data[1]);
                break;
            case 23:
                this.mem.store(leftValueField, data[0]);
                this.mem.store(rightValueField, data[1]);
                break;
            case 24:
                this.mem.store(leftValueField, data[0]);
                this.mem.store(rightValueField, 0);
                break;
            case 31:
                this.mem.store(leftValueField, data[0]);
                this.mem.store(rightValueField, data[1]);
                break;
            case 32:
                this.mem.store(leftValueField, data[0]);
                this.mem.store(rightValueField, data[1]);
                break;
            case 33:
                this.mem.store(leftValueField, data[0]);
                this.mem.store(rightValueField, data[1]);
                break;
            case 34:
                this.mem.store(leftValueField, data[0]);
                this.mem.store(rightValueField, 0);
                break;
            case 41:
                this.mem.store(leftValueField, 0);
                this.mem.store(rightValueField, data[1]);
                break;
            case 42:
                this.mem.store(leftValueField, 0);
                this.mem.store(rightValueField, data[1]);
                break;
            case 43:
                this.mem.store(leftValueField, 0);
                this.mem.store(rightValueField, data[1]);
                break;
            case 44:
                this.mem.store(leftValueField, 0);
                this.mem.store(rightValueField, 0);
                break;
            default:
                break;
        }
    }

}

// types
// 1 = INT
// 2 = REF
// 3 = Q
// 4 = NIL

// 3rd word field of an object contains type info
// examples below
// 11 = (int . int)
// 21 = (ref . int)
// 12 = (int . ref)