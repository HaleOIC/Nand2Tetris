import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CompilationEngine {
    private JackTokenizer jt;
    private VMWriter writer;
    private String className;
    private String subroutineName;
    private SymbolTable classLevelSymbolTable;      // class level symbol table
    private SymbolTable subroutineLevelSymbolTable; // subroutine level symbol table
    private OpStack ops;
    private int labelIndex = 0;

    private static final Set<String> operators = new HashSet<>();

    static {
        // Initialize the set with operators
        operators.add("+");
        operators.add("-");
        operators.add("*");
        operators.add("/");
        operators.add("&");
        operators.add("|");
        operators.add("<");
        operators.add(">");
        operators.add("=");
    }

    private boolean writeSymbol(String symbol, boolean pushOrPop) {
        String kind; 
        int index;
        if (subroutineLevelSymbolTable.contains(symbol)) {
            kind = subroutineLevelSymbolTable.kindOf(symbol);
            index = subroutineLevelSymbolTable.indexOf(symbol);
        } else if (classLevelSymbolTable.contains(symbol)) {
            kind = classLevelSymbolTable.kindOf(symbol);
            index = classLevelSymbolTable.indexOf(symbol);
        } else if (symbol.equals("this")) {
            kind = "pointer";
            index = 0;
        } else {
            return false;
        }
        // according to each 
        String segment;
        switch (kind) {
            case "argument":
                segment = "argument";
                break;
            case "var":
                segment = "local";
                break;
            case "static":
                segment = "static";
                break;
            case "field":
                segment = "this";
                break;
            case "pointer":
                segment = "pointer";
                break;
            default:
                return false;
        }
        if (pushOrPop) {
            writer.writePush(segment, index);
        } else {
            writer.writePop(segment, index);
        }
        return true;
    }

    /// Constructor
    public CompilationEngine(JackTokenizer jt, String outputFile) throws IOException {
        this.jt = jt;
        this.writer = new VMWriter(outputFile);
        this.className = "";
        this.subroutineName = "";
        this.classLevelSymbolTable = new SymbolTable();
        this.subroutineLevelSymbolTable = new SymbolTable();
        this.ops = new OpStack(writer);
    }

    /// Compiles a complete class.
    public void compileClass() throws IOException {
        try {
            // class
            jt.advance();

            // className 
            String className = jt.identifier();
            jt.advance();

            // set className
            this.className = className;
            
            // { symbol begin 
            jt.advance();

            // compile classVarDec 
            while (compileClassVarDec());

            // compile subroutineDec
            while (compileSubroutine());

            // } symbol end
            jt.advance();

            // end class 
        } catch (IllegalArgumentException e) {
            System.err.println(e);
        }
        writer.close();
    }

    /// Compiles a static declaration or a field declaration.
    private boolean compileClassVarDec() throws IOException {
        // static | field keyword
        String keyword = jt.keyWord();
        if (!keyword.equals("static") && !keyword.equals("field")) {
            return false;
        }
        jt.advance();

        try {
            // type
            String type = jt.type();
            jt.advance();
            // varName
            String varName = jt.identifier();
            jt.advance();

            // write the var variable into symboltable
            classLevelSymbolTable.define(varName, type, keyword);

            // maybe more varName
            while (true) {
                try {
                    String comma = jt.symbol();
                    jt.advance();
                    if (comma.equals(";")) {
                        break;
                    }
                    String varNameMore = jt.identifier();
                    jt.advance();

                    // write more var variable into symboltable
                    classLevelSymbolTable.define(varNameMore, type, keyword);

                } catch (IllegalArgumentException e) {
                    break;
                }
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    /// Compiles a complete method, function, or constructor.
    private boolean compileSubroutine() throws IOException {
        // constructor | function | method
        String keyword;
        try {
            keyword = jt.keyWord();
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (!keyword.equals("constructor") && !keyword.equals("function") && !keyword.equals("method")) {
            return false;
        }
        jt.advance();

        // judge its subroutine types
        subroutineLevelSymbolTable.clear();

        // First case, constructor
        String returnType = jt.routineType();  
        jt.advance();
        subroutineName = jt.identifier();
        jt.advance();
        jt.advance(); // (
        compileParameterList();
        jt.advance(); // )
        jt.advance(); // {

        int varCnt = 0, temp = 0;
        while ((temp = compileVarDec()) > 0) {
            varCnt += temp;
        }
        writer.writePlainText(String.format("function %s.%s %d\n", className, subroutineName, varCnt));
        if (keyword.equals("constructor")) {
            // calling memory.alloc n to allocate memory for arguments
            // and set pointer 0 to alloc's return value
            writer.writePush("constant", classLevelSymbolTable.varCount("field"));
            writer.writePlainText("call Memory.alloc 1");
            writer.writePop("pointer", 0);

            // compile main body
            compileSubroutineBody();

            // return pointer 0
            writer.writePush("pointer", 0);
            writer.writeReturn();
        } else if (keyword.equals("method")) {
            subroutineLevelSymbolTable.define("this", className, "argument");

            // set pointer 0 to argument 0
            writer.writePush("argument", 0);
            writer.writePop("pointer", 0);

            // compile main body
            compileSubroutineBody();

            // if void, return 0;
            if (returnType.equals("void")) {
                writer.writePush("constant", 0);
                writer.writeReturn();
            }
        } else if (keyword.equals("function")) {
            // compile main body
            compileSubroutineBody();

            // if void, return 0;
            if (returnType.equals("void")) {
                writer.writePush("constant", 0);
                writer.writeReturn();
            }
        }


        return true;
    }

    /// Compiles a (possibly empty) parameter list, not including the enclosing "()".
    private int compileParameterList() throws IOException {
        int number = 0;
        try {
            // type and name
            String type = jt.type();
            jt.advance();
            String varName = jt.identifier();
            jt.advance();
            subroutineLevelSymbolTable.define(varName, type, "argument");
            number++;

            // make sure whether additional var name
            while (true) {
                try {
                    String comma = jt.symbol();
                    if (comma.equals(",")) {
                        jt.advance();
                    } else {
                        break;
                    }

                    type = jt.type();
                    jt.advance();

                    varName = jt.identifier();
                    jt.advance();
                    subroutineLevelSymbolTable.define(varName, type, "argument");
                    number++;
                } catch (IllegalArgumentException e) {
                    break;
                }
            }
        } catch (IllegalArgumentException e) {
        }
        return number;
    }

    private void compileSubroutineBody() throws IOException {
        try {
            // compile statements
            compileStatements();

            // }
            String symbol = jt.symbol();
            if (!symbol.equals("}")) {
                throw new IllegalArgumentException("Current token is not }");
            }
            jt.advance(); 
        } catch (IllegalArgumentException e) {
        }
    }

    /// Compiles a var declaration.
    private int compileVarDec() throws IOException {
        int localCnt = 0;
        // var keyword
        String keyword = jt.keyWord();
        if (!keyword.equals("var")) {
            return localCnt;
        }
        jt.advance();

        // type
        String type = jt.type();
        jt.advance();

        // varName
        String varName = jt.identifier();
        jt.advance();
        subroutineLevelSymbolTable.define(varName, type, "var");
        localCnt++;

        // maybe more varName
        while (true) {
            try {
                String comma = jt.symbol();
                if (comma.equals(";")) {
                    jt.advance();
                    break;
                }
                jt.advance();
                String varNameMore = jt.identifier();
                jt.advance();
                subroutineLevelSymbolTable.define(varNameMore, type, "var");
                localCnt++;
            } catch (IllegalArgumentException e) {
                break;
            }
        }
        return localCnt;
    }

    /// Compiles a sequence of statements, not including the enclosing "{}".
    private void compileStatements() throws IOException {
        while (true) {
            try {
                String keyword = jt.keyWord();
                if (!"let if while do return".contains(keyword)) {
                    break;
                }
                if (keyword.equals("let")) {
                    compileLet();
                } else if (keyword.equals("if")) {
                    compileIf();
                } else if (keyword.equals("while")) {
                    compileWhile();
                } else if (keyword.equals("do")) {
                    compileDo();
                } else if (keyword.equals("return")) {
                    compileReturn();
                }
            } catch (IllegalArgumentException e) {
                break;
            }
        }
    }

    /// method that compiles a let statement
    private void compileLet() throws IOException {
        // let keyword
        String keyword = jt.keyWord();
        if (!keyword.equals("let")) {
            return;
        }
        jt.advance();

        // varName
        String varName = jt.identifier();
        jt.advance();


        String symbol = jt.symbol();
        // Case 1: with []
        if (symbol.equals("[")) {
            jt.advance();
            ops.push("[");
            writeSymbol(varName, true);
            compileExpression();
            writer.writePlainText("add");
            symbol = jt.symbol(); // ]
            jt.advance();
            
            // =
            symbol = jt.symbol();
            if (!symbol.equals("=")) {
                throw new IllegalArgumentException("Current token is not =");
            }
            jt.advance();

            // right hand expression
            compileExpression();

            // fixed four sentences
            writer.writePop("temp", 0);
            writer.writePop("pointer", 1);
            writer.writePush("temp", 0);
            writer.writePop("that", 0);
        } else if (symbol.equals("=")) {
            // Case 2: without []
            jt.advance();
            compileExpression();
            writeSymbol(varName, false);
        } else {
            throw new IllegalArgumentException("unkown symbol");
        }

        // ;
        symbol = jt.symbol();
        if (!symbol.equals(";")) {
            throw new IllegalArgumentException("Current token is not ;");
        }
        jt.advance();
    }

    /// method that compiles an if statement, possibly with a trailing else clause
    private void compileIf() throws IOException {
        int index = labelIndex++;
        // if keyword
        String keyword = jt.keyWord();
        if (!keyword.equals("if")) {
            throw new IllegalArgumentException("Current token is not if");
        }
        jt.advance();

        // (
        String symbol = jt.symbol();
        if (!symbol.equals("(")) {
            throw new IllegalArgumentException("Current token is not (");
        }
        jt.advance();

        // expression
        compileExpression();
        writer.writePlainText("not");
        writer.writeIf("IF_TRUE" + index);

        // )
        symbol = jt.symbol();
        if (!symbol.equals(")")) {
            throw new IllegalArgumentException("Current token is not )");
        }
        jt.advance();

        // {
        symbol = jt.symbol();
        if (!symbol.equals("{")) {
            throw new IllegalArgumentException("Current token is not {");
        }
        jt.advance();

        // statements
        compileStatements();
        writer.writeGoto("IF_FALSE" + index);

        // }
        symbol = jt.symbol();
        if (!symbol.equals("}")) {
            throw new IllegalArgumentException("Current token is not }");
        }
        jt.advance();
        writer.writeLabel("IF_TRUE" + index);

        // maybe else
        try {
            keyword = jt.keyWord();
            if (!keyword.equals("else")) {
                throw new IllegalArgumentException("Current token is not else");
            }
            jt.advance();
        
            // {
            symbol = jt.symbol();
            if (!symbol.equals("{")) {
                throw new IllegalArgumentException("Current token is not {");
            }
            jt.advance();

            // statements
            compileStatements();

            // }
            symbol = jt.symbol();
            if (!symbol.equals("}")) {
                throw new IllegalArgumentException("Current token is not }");
            }
            jt.advance();
        } catch (IllegalArgumentException e) {
        }
        writer.writeLabel("IF_FALSE" + index);
    }

    /// method that compiles a while statement
    private void compileWhile() throws IOException {
        // while keyword
        int index = labelIndex++;
        String keyword = jt.keyWord();
        if (!keyword.equals("while")) {
            throw new IllegalArgumentException("Current token is not while");
        }
        jt.advance();
        writer.writeLabel("whileLabel1" + index);

        // (
        String symbol = jt.symbol();
        if (!symbol.equals("(")) {
            throw new IllegalArgumentException("Current token is not (");
        }
        jt.advance();

        // expression
        compileExpression();
        writer.writePlainText("not");
        writer.writeIf("whileLabel2" + index);

        // )
        symbol = jt.symbol();
        if (!symbol.equals(")")) {
            throw new IllegalArgumentException("Current token is not )");
        }
        jt.advance();

        // {
        symbol = jt.symbol();
        if (!symbol.equals("{")) {
            throw new IllegalArgumentException("Current token is not {");
        }
        jt.advance();

        // statements
        compileStatements();
        writer.writeGoto("whileLabel1" + index);

        // }
        symbol = jt.symbol();
        if (!symbol.equals("}")) {
            throw new IllegalArgumentException("Current token is not }");
        }
        jt.advance();

        writer.writeLabel("whileLabel2" + index);
    }

    /// method that compiles a do statement
    public void compileDo() throws IOException {
        // do keyword
        String keyword = jt.keyWord();
        if (!keyword.equals("do")) {
            throw new IllegalArgumentException("Current token is not do");
        }
        jt.advance();

        // subroutineCall
        compileSubroutineCall();

        // ;
        String symbol = jt.symbol();
        if (!symbol.equals(";")) {
            throw new IllegalArgumentException("Current token is not ;");
        }
        jt.advance();
        writer.writePop("temp", 0);
    }


    /// method that compiles a return statement
    private void compileReturn() throws IOException {
        // return keyword
        String keyword = jt.keyWord();
        if (!keyword.equals("return")) {
            throw new IllegalArgumentException("Current token is not return");
        }
        jt.advance();

        // maybe expression
        if (!jt.stringVal().equals(";")) {
            compileExpression();
        } else {
            writer.writePush("constant", 0);
        }

        // ;
        String symbol = jt.symbol();
        if (!symbol.equals(";")) {
            throw new IllegalArgumentException("Current token is not ;");
        }
        jt.advance();
        writer.writeReturn();
    }

    /// Compiles an expression.
    private void compileExpression() throws IOException {
        // compile the first term
        compileTerm();

        // maybe more term
        while (true) {
            String symbol = jt.symbol().trim();
            // maybe this symbol is ]
            if (symbol.equals("]")) {
                ops.popUntilLastBracket();
                return;
            }
            // maybe this symbol is )

            if (symbol.equals(")")) {
                if (!ops.contains("(")) {
                    break;
                }
                ops.popUntil();
                jt.advance();
                continue;
            }

            // if the next string is not an operation
            symbol = jt.symbol().trim();
            if (!operators.contains(symbol)) {
                break;
            } else {
                ops.push(symbol);
            }
            jt.advance();

            // compile the next term
            compileTerm();
        }

        // pop all the operations
        ops.popUntil();
    }


    /// Compiles a term. 
    private void compileTerm() throws IOException {
        try {
            String tokenType = jt.tokenType();
            switch (tokenType) {
                case "integerConstant":
                    writer.writePush("constant", Integer.parseInt(jt.stringVal()));
                    jt.advance();
                    break;
                case "stringConstant":
                    String str = jt.stringVal();
                    writer.writePush("constant", str.length());
                    writer.writeCall("String.new", 1);
                    for (int i = 0; i < str.length(); i++) {
                        writer.writePush("constant", (int)str.charAt(i));
                        writer.writeCall("String.appendChar", 2);
                    }
                    jt.advance();
                    break;
                case "keyword":
                    String keyword = jt.keyWord();
                    switch (keyword) {
                        case "true":
                            writer.writePush("constant", 0);
                            writer.writeArithmetic("not");
                            break;
                        case "false":
                        case "null":
                            writer.writePush("constant", 0);
                            break;
                        case "this":
                            writeSymbol(keyword, true);
                            break;
                    }
                    jt.advance();
                    break;
                case "identifier":
                    String identifier = jt.identifier();
                    if (jt.nextString().equals("[")) {
                        ops.push("[");
                        writeSymbol(identifier, true);
                        jt.advance(); jt.advance();
                        compileExpression();
                        writer.writeArithmetic("add");
                        writer.writePop("pointer", 1);
                        writer.writePush("that", 0);
                        jt.advance();
                    } else if (jt.nextString().equals("(") || jt.nextString().equals(".")) {
                        compileSubroutineCall(); // Assume this starts right after identifier
                    } else {
                        writeSymbol(identifier, true);
                        jt.advance();
                    }
                    break;
                case "symbol":
                    String symbol = jt.symbol();
                    if (symbol.equals("(")) {
                        jt.advance();
                        ops.push("(");
                        compileTerm();
                    } else if (symbol.equals("~") || symbol.equals("-")) {
                        jt.advance();
                        if (symbol.equals("~")) {
                            ops.push("~");
                        } else {
                            ops.push("neg");
                        }
                        compileTerm();
                    }
                    break;
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Error compiling term: " + e.getMessage());
        }
    }


    /// Compiles a subroutine call
    private void compileSubroutineCall() throws IOException {
        try {
            // subroutineName | className | varName
            String identifier = jt.identifier();
            jt.advance();

            boolean addOne = false; 
            // maybe more
            try {
                String symbol = jt.symbol();
                if (symbol.equals(".")) {
                    jt.advance();

                    // push first identifier into stack
                    if (classLevelSymbolTable.contains(identifier) || subroutineLevelSymbolTable.contains(identifier)) {
                        addOne = true;
                        writeSymbol(identifier, true);
                        if (classLevelSymbolTable.contains(identifier)) {
                            identifier = classLevelSymbolTable.typeOf(identifier);
                        } else {
                            identifier = subroutineLevelSymbolTable.typeOf(identifier);
                        }
                    }

                    // subroutineName
                    String subroutineName = jt.identifier();
                    identifier += "." + subroutineName;
                    jt.advance();
                }
            } catch (IllegalArgumentException e) {
            }

            if (!identifier.contains(".")) {
                addOne = true;
                writer.writePush("pointer", 0);
                identifier = className + "." + identifier;
            }

            // (
            String symbol = jt.symbol();
            if (!symbol.equals("(")) {
                throw new IllegalArgumentException("Current token is not (");
            }
            jt.advance();

            OpStack temp = this.ops;
            this.ops = new OpStack(writer);
            // expressionList
            int nargs = compileExpressionList();

            // )
            symbol = jt.symbol();
            if (!symbol.equals(")")) {
                throw new IllegalArgumentException("Current token is not )");
            }
            jt.advance();
            this.ops = temp;
            if (addOne) {
                nargs += 1;
            }
            writer.writeCall(identifier, nargs);
        } catch (IllegalArgumentException e) {
        }
    }

    /// Compiles an expression list
    private int compileExpressionList() throws IOException {
        if (jt.stringVal().equals(")")) {
            return 0;
        }
        int cnt = 0;
        compileExpression();
        cnt += 1;
        try {
            while (true) {
                try {
                    String symbol = jt.symbol();
                    if (!symbol.equals(",")) {
                        break;
                    }
                    jt.advance();

                    // compile expression
                    compileExpression();
                    cnt += 1;
                } catch (IllegalArgumentException e) {
                    break;
                }
            }
        } catch (IllegalArgumentException e) {
        }
        return cnt;
    }
}
